 package com.epam.growthhub.sso.web;
 
  
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.epam.growthhub.authentication.domain.UserInfo;
 import com.epam.growthhub.domain.Application;
 import com.epam.growthhub.sso.service.SsoService;
 import com.google.gson.Gson;
 
 /**
  * Servlet implementation class AuthenticationServlet
  */
 public class AuthenticationServlet extends HttpServlet {
     private static final long serialVersionUID = 1L;
     private final Logger LOG = Logger.getLogger(AuthenticationServlet.class
 	    .getName());
     private SsoService authService;
 
     private static final String ERROR_MESSAGE_TRUE = "true";
     private static final String error_parameter = "&error=true";
 
     /**
      * @throws IOException
      * @see HttpServlet#HttpServlet()
      */
     public AuthenticationServlet() throws IOException {
 	super();
 	try {
 	    Context context = new InitialContext();
 	    authService = (SsoService) context.lookup("osgi:service/"
 		    + SsoService.class.getName());
 	} catch (NamingException e) {
 	    LOG.log(Level.FINER, e.toString());
 	}
     }
 
     public void setAuthenticationService(SsoService authService) {
 	this.authService = authService;
 
     }
 
     /**
      * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
      *      response)
      */
     protected void doGet(HttpServletRequest request,
 	    HttpServletResponse response) throws ServletException, IOException {
 	String errorMessage = (String) request.getParameter("error");
 
 	LOG.info("SSO get request!");
 	LOG.log(Level.CONFIG, "SSO sessionId = " + request.getSession().getId());
 
 	if (authService.generateToken()!=null){
 	String tempToken = authService.generateToken();
 
 	// add tempToken to map
 	authService.addTempToken(tempToken, request.getSession().getId());
 
 	// check current userInfo
 	if (authService.getUserInfo(request.getSession().getId()) != null) {
 	    LOG.log(Level.CONFIG, "SSO find userInfo, send request with tempToken to successUrl");
 
 	    response.sendRedirect(request.getParameter("successUrl")
 		    + "?temptoken=" + tempToken);
 	} else {
 	    LOG.log(Level.CONFIG, "SSO userInfo not founded");
 	    LOG.log(Level.CONFIG, "SSO tempToken not founded, send request with tempToken to loginUrl");
 
 	    String onlyTempTokenURL = request.getParameter("loginUrl")
 		    + "?temptoken=" + tempToken;
 
 	    if (errorMessage != null && errorMessage.equals(ERROR_MESSAGE_TRUE)) {
 		response.sendRedirect(onlyTempTokenURL + error_parameter);
 	    } else {
 		response.sendRedirect(onlyTempTokenURL);
 	    }
 	}
 	}
     }
 
     /**
      * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
      *      response)
      */
     protected void doPost(HttpServletRequest request,
 	    HttpServletResponse response) throws ServletException, IOException {
 
 	String login = request.getParameter("login");
 	String password = request.getParameter("password");
 	String temptoken = request.getParameter("temptoken");
 	String appName = request.getParameter("app");
 
 	LOG.info("SSO application: " + appName);
 	Application app = authService.getApplication(appName);
 	LOG.info("SSO application id: " + app.getId());
 	Gson gson = new Gson();
 
 	String userId = authService.getUserIdByTempToken(temptoken);
 	String securedToken = authService.generateToken();
 
 	UserInfo userInfo = authService.getUserInfo(userId);
 
 	if (userId != null && userInfo != null) {
 	    LOG.info("SSO user already logined");
 
 	    // add securedToken
 	    authService.addSecuredToken(securedToken, userId);
 	    response.setCharacterEncoding("UTF-8");
 	    response.addHeader("username", gson.toJson(userInfo));
 	    response.addHeader("securedtoken", securedToken);
 	} else {
 
 	    UserInfo existingUserInfo = authService.checkUserCredentials(login,
 		    password, appName);
 
 	    if (existingUserInfo.isAuthenticated()) {
 		LOG.info("SSO user credentials checked");
 
 		// add securedToken
 		authService.addSecuredToken(securedToken, userId);
 
 		// add userInfo for current user
 		authService.addUserInfo(userId, existingUserInfo);
 
 		// temporary hot fix (ArrayIndexOutOfBoundsException)
 		existingUserInfo.setAttributes(null);
 
 		response.setCharacterEncoding("UTF-8");
 		response.addHeader("username", gson.toJson(existingUserInfo));
 		response.addHeader("securedtoken", securedToken);
 
 		// remove old tempToken
 		authService.removeTempToken(userId);
 
 	    } else {
 		LOG.info("SSO Can't find user");
 	    }
 
 	    return;
 
 	}
 
     }
 }
