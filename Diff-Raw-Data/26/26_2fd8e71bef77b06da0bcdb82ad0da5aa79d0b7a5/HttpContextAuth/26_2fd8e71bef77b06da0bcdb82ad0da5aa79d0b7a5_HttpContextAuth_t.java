 package org.paxle.gui.impl;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.framework.Bundle;
 import org.osgi.service.http.HttpContext;
 import org.osgi.service.useradmin.Authorization;
 import org.osgi.service.useradmin.User;
 import org.osgi.service.useradmin.UserAdmin;
 import org.osgi.util.tracker.ServiceTracker;
 
 /*
  * User authentication here. Please read
  * http://www2.osgi.org/javadoc/r4/index.html to see how this could
  * be used in combination with the OSGI User Admin Service
  * (http://www2.osgi.org/javadoc/r4/org/osgi/service/useradmin/package-summary.html)
  */
 public class HttpContextAuth implements HttpContext {
 	public static final String USER_HTTP_PASSWORD = "http.password";
 	public static final String USER_HTTP_LOGIN = "http.login";
 
 	/**
 	 * For logging
 	 */
 	private static Log logger = LogFactory.getLog(HttpContextAuth.class);
 
 	private final Bundle bundle;
 
 	private final ServiceTracker uAdminTracker;
 
 	public HttpContextAuth(Bundle b, ServiceTracker uAdminTracker) {
 		this.bundle = b;
 		this.uAdminTracker = uAdminTracker;
 	}
 
 	public String getMimeType( String name) {
 		return null;
 	}
 
 	public URL getResource( String name) {
 		return bundle.getResource( name);
 	}
 
 	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		// check if there is any http-auth header
 		String httpAuth = request.getHeader("Authorization");
 		
 		// Get the session
 		HttpSession session = request.getSession(true);
 
 		// check if we are already authenticated
 		Object done = session.getAttribute("logon.isDone");  // marker object
 		
 		// determine if http-auth can be done
 		if (done == null) {
 			UserAdmin uAdmin = (UserAdmin) this.uAdminTracker.getService();
 			
 			// auth. user
 			User user = httpAuth(uAdmin, request, httpAuth);			
 			if (user != null) {
 				done = Boolean.TRUE;
 				session.setAttribute("logon.isDone", done);
 				
 				// set user-data into the session
 				session.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);
 				session.setAttribute(HttpContext.AUTHORIZATION, uAdmin.getAuthorization(user));
 				session.setAttribute(HttpContext.REMOTE_USER, user);				
 			}
 		}
 		
 		// if we are still not authenticated, try to use form-based login
 		if (done == null) {
 			// No logon.isDone means he hasn't logged in.
 			// Save the request URL as the true target and redirect to the login page.
 			session.setAttribute("login.target", request.getRequestURL().toString());
 			response.sendRedirect(String.format(
 					"%s://%s:%d/login",
 					request.getScheme(),
 					request.getServerName(),
 					Integer.valueOf(request.getServerPort())
 			));
 			return false;
 		} 
 
 		// according to the OSGi Spec. we need to set this.
 		request.setAttribute(HttpContext.AUTHENTICATION_TYPE, session.getAttribute(HttpContext.AUTHENTICATION_TYPE));
 		request.setAttribute(HttpContext.AUTHORIZATION, session.getAttribute(HttpContext.AUTHORIZATION));
 		request.setAttribute(HttpContext.REMOTE_USER, session.getAttribute(HttpContext.REMOTE_USER));
 
 		return true;
 	}
 	
 	public static User httpAuth(final UserAdmin userAdmin, final HttpServletRequest request, String auth) throws UnsupportedEncodingException {		
 		if (auth == null || auth.length() <= "Basic ".length()) {
 			return null;
 		}
 
 		// base64 decode and get username + password
 		byte[] authBytes = Base64.decodeBase64(auth.substring("Basic ".length()).getBytes("UTF-8"));
 		auth = new String(authBytes,"UTF-8");		
 		String[] authData = auth.split(":");
 		if (authData.length == 0) {
 			logger.info(String.format("[%s] No user-authentication data found to access '%s'.", request.getRemoteHost(), request.getRequestURI()));
 			return null;
 		}
 		
 		String userName = authData[0];
 		String password = authData.length==1?"":authData[1];
 
 		if (userAdmin == null) {
 			logger.info(String.format("[%s] OSGi UserAdmin service not found", request.getRemoteHost()));
 			return null;
 		}
 		
		return authenticatedAs(userAdmin, request, userName, password);
 	}
 	
	public static User authenticatedAs(final UserAdmin userAdmin, final HttpServletRequest request, final String userName, String password) throws UnsupportedEncodingException {
 		if (userAdmin == null) {
 			logger.info(String.format("[%s] OSGi UserAdmin service not found", request.getRemoteHost()));
 			return null;
 		}
 
 		User user = userAdmin.getUser(USER_HTTP_LOGIN,userName);
 		if( user == null ) {
 			logger.info(String.format("[%s] No user found for username '%s'.", request.getRemoteHost(), userName));	
 			return null;
 		}
 
 		if(!user.hasCredential(USER_HTTP_PASSWORD, password)) {
 			logger.info(String.format("[%s] Wrong password for username '%s'.", request.getRemoteHost(), userName));
 			return null;
 		}
 
 		Authorization authorization = userAdmin.getAuthorization(user);
 		if(authorization == null) {
 			logger.info(String.format("[%s] No authorization found for username '%s'.", request.getRemoteHost(), userName));
 			return null;
 		}
 		
 		if (!authorization.hasRole("Administrators")) {
 //			this.logger.warn(String.format(""))
 		}
 
 		return user;		
 	}	
 }
