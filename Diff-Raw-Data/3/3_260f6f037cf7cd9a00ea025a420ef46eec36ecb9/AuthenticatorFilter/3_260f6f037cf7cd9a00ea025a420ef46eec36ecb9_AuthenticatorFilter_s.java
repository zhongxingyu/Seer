 /**
  * Copyright 2010 Tristan Tarrant
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.dataforte.doorkeeper.filter;
 
 import java.io.IOException;
 import java.util.regex.Pattern;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import net.dataforte.commons.slf4j.LoggerFactory;
 import net.dataforte.commons.web.URLUtils;
 import net.dataforte.doorkeeper.Doorkeeper;
 import net.dataforte.doorkeeper.User;
 import net.dataforte.doorkeeper.authenticator.AccessDeniedException;
 import net.dataforte.doorkeeper.authenticator.Authenticator;
 import net.dataforte.doorkeeper.authenticator.AuthenticatorException;
 import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;
 import net.dataforte.doorkeeper.authorizer.Authorizer;
 
 import org.slf4j.Logger;
 
 
 public class AuthenticatorFilter implements Filter {
 	private static final Logger log = LoggerFactory.make();
 	private static final String SESSION_USER = User.class.getName();
 	private Doorkeeper doorkeeper;
 	private Pattern skipRegex = Pattern.compile(".+\\.(gif|png|jpg|jpeg|swf|js|css)$");
 	private String accessDeniedRedirectURL;
 
 	@Override
 	public void init(FilterConfig filterConfig) throws ServletException {
 		if (log.isInfoEnabled()) {
 			log.info("Initializing AuthenticatorFilter...");
 		}
 		setDoorkeeper(Doorkeeper.getInstance(filterConfig.getServletContext()));
 	}
 
 	public Doorkeeper getDoorkeeper() {
 		return doorkeeper;
 	}
 
 	public void setDoorkeeper(Doorkeeper doorkeeper) {
 		this.doorkeeper = doorkeeper;
 		this.doorkeeper.applyConfiguration("filter", this);
 	}
 	
 	public String getSkipRegex() {
 		return skipRegex.pattern();
 	}
 
 	public void setSkipRegex(String skipRegex) {
 		this.skipRegex = Pattern.compile(skipRegex);
 	}
 
 	public String getAccessDeniedRedirectURL() {
 		return accessDeniedRedirectURL;
 	}
 
 	public void setAccessDeniedRedirectURL(String accessDeniedRedirectURL) {
 		this.accessDeniedRedirectURL = accessDeniedRedirectURL;
 	}
 
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 		final HttpServletRequest req = (HttpServletRequest) request;
 		final HttpServletResponse res = (HttpServletResponse) response;
 		
 		// Do not try to authenticate/authorize resources matching the skipFilterRegex pattern
 		if(skipRegex.matcher(req.getRequestURI()).matches()) {
 			chain.doFilter(request, response);
 			return;
 		}
 
 		/*** AUTHENTICATION PHASE ***/
 		// Get the session only if it exists already
 		HttpSession session = req.getSession(false);
 
 		User user = null;
 
 		if (session != null) {
 			// Attempt to get user from session
 			user = (User) session.getAttribute(SESSION_USER);
 		}
 		// We still don't have a user
 		if (user == null) {
 			for (Authenticator auth : doorkeeper.getAuthenticatorChain("filter")) {
 				AuthenticatorToken token = auth.negotiate(req, res);
 				switch (token.getState()) {
 
 				case AUTHENTICATED:
 					// The authenticator has obtained a principal and has
 					// authenticated, so we just need to get the user's profile
 					session = req.getSession(true);
 					try {
 						user = doorkeeper.getAccountManager().load(token);						
 						session.setAttribute(SESSION_USER, user);
 					} catch (AuthenticatorException e) {
 						// Authentication failed, restart it
 						auth.restart(req, res);
 						return;
 					}
 					break;
 				case NEGOTIATING:
 					// if the authenticator requires more steps to complete,
 					// return immediately
 					return;
 				case ACQUIRED:
 					// The authenticator has obtained principal and credentials
 					// but does not know how to validate them. We do it here
 					String principalName = token.getPrincipalName();
 					if (principalName != null) {
 						try {
 							user = doorkeeper.getAccountManager().authenticate(token);
 						} catch (AuthenticatorException e) {
 							// Authentication failed, restart it
 							auth.restart(req, res);
 							return;
 						}
 
 						session = req.getSession(true);
 						if (log.isDebugEnabled()) {
 							log.debug("User = " + principalName);
 						}
 						session.setAttribute(SESSION_USER, user);
 						auth.complete(req, res);
 						return;
 					} else {
 						// Authentication failed, restart it
 						auth.restart(req, res);
 						return;
 					}
 				case REJECTED:
 					auth.restart(req, res);
 					return;
 				}
 				
 			}
 		}
 		
 		/*** AUTHORIZATION PHASE ***/
 		for (Authorizer auth : doorkeeper.getAuthorizerChain("filter")) {
 			try {
 				if (!auth.authorize(user, req.getServletPath())) {
 					throw new AccessDeniedException(req.getServletPath());					
 				}		
 			} catch (AuthenticatorException e) {
 				if(accessDeniedRedirectURL!=null) {
 					res.sendRedirect(URLUtils.urlRewrite(req, accessDeniedRedirectURL));
 				} else {
 					res.sendError(HttpServletResponse.SC_FORBIDDEN);
 				}
 				return;
 			}
 		}
 
 		chain.doFilter(new AuthenticatorRequestWrapper(req, user), response);
 	}
 
 	@Override
 	public void destroy() {
 		if (log.isInfoEnabled()) {
 			log.info("Shutting down AuthenticatorFilter...");
 		}
 	}
 
 }
