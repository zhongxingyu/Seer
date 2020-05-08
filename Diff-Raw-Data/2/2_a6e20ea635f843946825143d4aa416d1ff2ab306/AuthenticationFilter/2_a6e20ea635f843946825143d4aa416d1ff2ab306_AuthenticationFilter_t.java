 /*
  * Copyright (C) 2013 InventIt Inc.
  * 
  * See https://github.com/inventit/moat-iot-highcharts-example
  */
 package com.yourinventit.moat.gae.hcjsexample.controllers;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.appengine.api.utils.SystemProperty.Environment;
 import com.google.appengine.api.utils.SystemProperty.Environment.Value;
 import com.yourinventit.moat.gae.hcjsexample.Constants;
 
 /**
  * 
  * @author dbaba@yourinventit.com
  * 
  */
 public class AuthenticationFilter implements Filter {
 
 	/**
 	 * {@link Logger}
 	 */
 	private static final Logger LOGGER = Logger
 			.getLogger(AuthenticationFilter.class.getName());
 
 	static final String LOGIN_PATH = "/login.jsp";
 
 	/**
 	 * The constant value of the authenticated user id.
 	 */
 	private static final String GOOGLE_USER_ID = Constants.getInstance()
 			.getGoogleUserId();
 
 	/**
 	 * Whether or not the runtime is development mode.
 	 */
 	private static final boolean DEVELOPMENT = Value.Development
 			.equals(Environment.environment.value());
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
 	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
 	 */
 	@Override
 	public void doFilter(ServletRequest servletRequest,
 			ServletResponse servletResponse, FilterChain filterChain)
 			throws IOException, ServletException {
 
 		final HttpServletRequest request = (HttpServletRequest) servletRequest;
 		final HttpServletResponse response = (HttpServletResponse) servletResponse;
 
 		if (DEVELOPMENT) {
 			LOGGER.info("[AUTH_SKIPPED] DEVELOPMENT MODE....");
 
 		} else {
 			final String pathInfo = request.getPathInfo();
			if (pathInfo != null && !pathInfo.startsWith(LOGIN_PATH)) {
 				final UserService userService = UserServiceFactory
 						.getUserService();
 				final User user = userService.getCurrentUser();
 				if (userService.isUserLoggedIn() == false) {
 					LOGGER.warning("[AUTH_ERROR] UserService.isUserLoggedIn() => false, User => "
 							+ user);
 					request.getRequestDispatcher(LOGIN_PATH).forward(request,
 							response);
 					return;
 				}
 				if (user == null) {
 					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 					LOGGER.warning("[BAD_REQUEST] User => null");
 					return;
 				}
 				if (!GOOGLE_USER_ID.equals(user.getEmail())) {
 					LOGGER.warning("[AUTH_ERROR] User Email mismatch, userEmail => "
 							+ user.getEmail());
 					request.getRequestDispatcher(LOGIN_PATH).forward(request,
 							response);
 					return;
 				}
 			}
 		}
 
 		filterChain.doFilter(request, response);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
 	 */
 	@Override
 	public void init(FilterConfig filterConfig) throws ServletException {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see javax.servlet.Filter#destroy()
 	 */
 	@Override
 	public void destroy() {
 	}
 }
