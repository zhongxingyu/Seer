 package com.mymed.controller.core.requesthandler;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.gson.JsonSyntaxException;
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.authentication.AuthenticationManager;
 import com.mymed.controller.core.manager.authentication.IAuthenticationManager;
 import com.mymed.model.data.session.MAuthenticationBean;
 import com.mymed.model.data.user.MUserBean;
 
 /**
  * Servlet implementation class AuthenticationRequestHandler
  */
 public class AuthenticationRequestHandler extends AbstractRequestHandler {
 	/* --------------------------------------------------------- */
 	/* Attributes */
 	/* --------------------------------------------------------- */
 	private static final long serialVersionUID = 1L;
 	
 	private IAuthenticationManager authenticationManager;
      
 	/* --------------------------------------------------------- */
 	/* Constructors */
 	/* --------------------------------------------------------- */
     /**
      * @see HttpServlet#HttpServlet()
      */
     public AuthenticationRequestHandler() throws ServletException {
     	super();
 		try {
 			this.authenticationManager = new AuthenticationManager();
 		} catch (InternalBackEndException e) {
 			throw new ServletException("AuthenticationManager is not accessible because: " + e.getMessage());
 		}
     }
 
     /* --------------------------------------------------------- */
 	/* extends HttpServlet */
 	/* --------------------------------------------------------- */
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		try {
 			/** Get the parameters */
 			Map<String, String> parameters = getParameters(request);
 
 			/** Get the method code */
 			RequestCode code = requestCodeMap.get(parameters.get("code"));
 
 			/** handle the request */
 			String login, password;
 			if((login = parameters.get("login"))== null || (password = parameters.get("password"))== null){
 				handleError(new InternalBackEndException("missing argument!"), response);
 				return;
 			}
 			switch (code) {
 			case READ:
 				MUserBean userBean;
 				try {
 					userBean = authenticationManager.read(login, password);
 					setResponseText(getGson().toJson(userBean));
 				} catch (IOBackEndException e) {
 					handleError(e, response);
 					return;
 				}
 				break;
 			default:
 				handleError(new InternalBackEndException("ProfileRequestHandler.doGet(" + code + ") not exist!"), response);
 				return;
 			}
 			super.doGet(request, response);
 		} catch (InternalBackEndException e) {
 			e.printStackTrace();
 			handleError(e, response);
 			return;
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		try {
 			/** Get the parameters */
 			Map<String, String> parameters = getParameters(request);
 
 			/** Get the method code */
 			RequestCode code = requestCodeMap.get(parameters.get("code"));
 
 			/** handle the request */
 			String authentication;
 			if((authentication = parameters.get("authentication"))== null){
 				handleError(new InternalBackEndException("missing authentication argument!"), response);
 				return;
 			}
 			switch (code) {
 			case CREATE:
 				String user;
 				if((user = parameters.get("user"))== null){
 					handleError(new InternalBackEndException("missing user argument!"), response);
 					return;
 				}
 				MUserBean userBean = null;
 				try {
 					userBean = getGson().fromJson(user,
 							MUserBean.class);
 				} catch (JsonSyntaxException e) {
 					handleError(new InternalBackEndException("user jSon format is not valid"), response);
 					return;
 				}
 				MAuthenticationBean authenticationBean = null;
 				try {
 					authenticationBean = getGson().fromJson(authentication,
 							MAuthenticationBean.class);
 				} catch (JsonSyntaxException e) {
 					handleError(new InternalBackEndException("authentication jSon format is not valid"), response);
 					return;
 				}
 				System.out.println("\nINFO: trying to create a new user:\n" + userBean);
 				userBean = authenticationManager.create(userBean, authenticationBean);
 				System.out.println("\nINFO: User created!");
 				setResponseText(getGson().toJson(userBean));
 				break;
 			case UPDATE:
 				try {
 					authenticationBean = getGson().fromJson(authentication,
 							MAuthenticationBean.class);
 				} catch (JsonSyntaxException e) {
 					handleError(new InternalBackEndException("authentication jSon format is not valid"), response);
 					return;
 				}
 				String id;
 				if((id = parameters.get("id"))== null){
 					handleError(new InternalBackEndException("missing user argument!"), response);
 					return;
 				}
 				System.out.println("\nINFO: trying to update authentication:\n" + authenticationBean);
 				authenticationManager.update(id, authenticationBean);
 				System.out.println("\nINFO: authentication updated!");
 				break;
 			default:
 				handleError(new InternalBackEndException("ProfileRequestHandler.doPost(" + code + ") not exist!"), response);
 				return;
 			}
 			super.doPost(request, response);
 		} catch (InternalBackEndException e) {
 			e.printStackTrace();
 			handleError(e, response);
 			return;
 		} catch (IOBackEndException e) {
 			e.printStackTrace();
 			handleError(e, response);
 		}
 	}
 
 }
