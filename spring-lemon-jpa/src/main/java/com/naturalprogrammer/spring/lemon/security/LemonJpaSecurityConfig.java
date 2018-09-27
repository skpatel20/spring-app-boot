package com.naturalprogrammer.spring.lemon.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commonsweb.security.LemonCommonsWebSecurityConfig;
import com.naturalprogrammer.spring.lemon.commonsweb.security.LemonTokenAuthenticationFilter;

/**
 * Security configuration class. Extend it in the
 * application, and make a configuration class. Override
 * protected methods, if you need any customization.
 * 
 * @author Sanjay Patel
 */
public class LemonJpaSecurityConfig extends LemonCommonsWebSecurityConfig {
	
	private static final Log log = LogFactory.getLog(LemonJpaSecurityConfig.class);

	private LemonProperties properties;
	private UserDetailsService userDetailsService;
	private LemonAuthenticationSuccessHandler authenticationSuccessHandler;
	private AuthenticationFailureHandler authenticationFailureHandler;
	private LemonOidcUserService oidcUserService;
	private LemonOAuth2UserService<?, ?> oauth2UserService;
	private OAuth2AuthenticationSuccessHandler<?> oauth2AuthenticationSuccessHandler;
	private OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler;
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	public void createLemonSecurityConfig(LemonProperties properties, UserDetailsService userDetailsService,
			LemonAuthenticationSuccessHandler authenticationSuccessHandler, AuthenticationFailureHandler authenticationFailureHandler,
			LemonOidcUserService oidcUserService,
			LemonOAuth2UserService<?, ?> oauth2UserService,
			OAuth2AuthenticationSuccessHandler<?> oauth2AuthenticationSuccessHandler,
			OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler,
			PasswordEncoder passwordEncoder) {

		this.properties = properties;
		this.userDetailsService = userDetailsService;
		this.authenticationSuccessHandler = authenticationSuccessHandler;
		this.authenticationFailureHandler = authenticationFailureHandler;
		this.oidcUserService = oidcUserService;
		this.oauth2UserService = oauth2UserService;
		this.oauth2AuthenticationSuccessHandler = oauth2AuthenticationSuccessHandler;
		this.oauth2AuthenticationFailureHandler = oauth2AuthenticationFailureHandler;
		this.passwordEncoder = passwordEncoder;
		
		log.info("Created");
	}

	/**
	 * Security configuration, calling protected methods
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		super.configure(http);
		login(http); // authentication
		exceptionHandling(http); // exception handling
		oauth2Client(http);
	}

	
	/**
	 * Configuring authentication.
	 */
	protected void login(HttpSecurity http) throws Exception {
		
		http
		.formLogin() // form login
			.loginPage(loginPage())
			
			/******************************************
			 * Setting a successUrl would redirect the user there. Instead,
			 * let's send 200 and the userDto along with an Authorization token.
			 *****************************************/
			.successHandler(authenticationSuccessHandler)
			
			/*******************************************
			 * Setting the failureUrl will redirect the user to
			 * that url if login fails. Instead, we need to send
			 * 401. So, let's set failureHandler instead.
			 *******************************************/
        	.failureHandler(authenticationFailureHandler);
	}

	
	/**
	 * Override this to change login URL
	 * 
	 * @return
	 */
	protected String loginPage() {
		
		return "/api/core/login";
	}

	
	protected void oauth2Client(HttpSecurity http) throws Exception {
		
		http.oauth2Login()
			.authorizationEndpoint()
				.authorizationRequestRepository(new HttpCookieOAuth2AuthorizationRequestRepository(properties)).and()
			.successHandler(oauth2AuthenticationSuccessHandler)
			.failureHandler(oauth2AuthenticationFailureHandler)
			.userInfoEndpoint()
				.oidcUserService(oidcUserService)
				.userService(oauth2UserService);
	}	

	
	/**
	 * Needed for configuring JwtAuthenticationProvider
	 */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
  
    	super.configure(auth);
        auth.userDetailsService(userDetailsService)
        	.passwordEncoder(passwordEncoder);
    }
}
