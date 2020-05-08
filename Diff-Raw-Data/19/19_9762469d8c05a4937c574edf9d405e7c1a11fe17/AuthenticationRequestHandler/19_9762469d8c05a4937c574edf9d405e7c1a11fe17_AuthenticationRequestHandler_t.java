 /*
  * Copyright 2012 INRIA
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mymed.controller.core.requesthandler.v2;
 
 import static com.mymed.utils.MiscUtils.json_decode;
 
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.mymed.controller.core.exception.AbstractMymedException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.authentication.AuthenticationManager;
 import com.mymed.controller.core.manager.authentication.IAuthenticationManager;
 import com.mymed.controller.core.manager.profile.IProfileManager;
 import com.mymed.controller.core.manager.profile.ProfileManager;
 import com.mymed.controller.core.manager.session.ISessionManager;
 import com.mymed.controller.core.manager.session.SessionManager;
 import com.mymed.controller.core.requesthandler.message.JsonMessageOut;
 import com.mymed.model.data.session.MSessionBean;
 import com.mymed.utils.HashFunction;
 
 /**
  * Servlet implementation class AuthenticationRequestHandler
  */
 @WebServlet("/v2/AuthenticationRequestHandler")
 public class AuthenticationRequestHandler extends AbstractRequestHandler {
 
 	/**
 	 * Generated serial ID.
 	 */
 	private static final long serialVersionUID = 8762837510508354509L;
 
 	private IAuthenticationManager authenticationManager;
 	private ISessionManager sessionManager;
 	private IProfileManager profileManager;
 
 	/**
 	 * JSON 'login' attribute.
 	 */
 	private static final String JSON_LOGIN = JSON.get("json.login");
 
 	/**
 	 * JSON 'password' attribute.
 	 */
 	private static final String JSON_PASSWORD = JSON.get("json.password");
 
 	/**
 	 * JSON 'authentication' attribute.
 	 */
 	private static final String JSON_AUTH = JSON.get("json.authentication");
 
 	/**
 	 * JSON 'oldPassword' attribute.
 	 */
 	private static final String JSON_OLD_PWD = JSON.get("json.old.password");
 
 	/**
 	 * JSON 'oldLogin' attribute.
 	 */
 	private static final String JSON_OLD_LOGIN = JSON.get("json.old.login");
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public AuthenticationRequestHandler() throws ServletException {
 		super();
 
 		try {
 			authenticationManager = new AuthenticationManager();
 			sessionManager = new SessionManager();
 			profileManager = new ProfileManager();
 		} catch (final InternalBackEndException e) {
 			LOGGER.debug("AuthenticationManager not accessible!", e);
 			throw new ServletException(
 					"AuthenticationManager is not accessible because: "
 							+ e.getMessage()); // NOPMD
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.mymed.controller.core.requesthandler.AbstractRequestHandler#doGet
 	 * (javax.servlet.http.HttpServletRequest,
 	 * javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	public void doGet(final HttpServletRequest request,
 			final HttpServletResponse response) throws ServletException {
 		final JsonMessageOut<Object> message = new JsonMessageOut<Object>(200, this
 				.getClass().getName());
 
 		try {
 			final Map<String, String> parameters = getParameters(request);
 
 			final RequestCode code = REQUEST_CODE_MAP.get(parameters
 					.get(JSON_CODE));
 
 			switch (code) {
 			case READ:
 				throw new InternalBackEndException(" do Post READ...");
 			case DELETE:
 				throw new InternalBackEndException("not implemented yet...");
 			default:
 				throw new InternalBackEndException(
 						"AuthenticationRequestHandler(" + code + ") not exist!");
 			}
 		} catch (final AbstractMymedException e) {
 			LOGGER.debug("Error in doGet operation", e);
 			message.setStatus(e.getStatus());
 			message.setDescription(e.getMessage());
 		}
 
 		printJSonResponse(message, response);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.mymed.controller.core.requesthandler.AbstractRequestHandler#doPost
 	 * (javax.servlet.http.HttpServletRequest,
 	 * javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	public void doPost(final HttpServletRequest request,
 			final HttpServletResponse response) throws ServletException {
 		final JsonMessageOut<Object> message = new JsonMessageOut<Object>(200, this
 				.getClass().getName());
 
 		try {
 			final Map<String, String> parameters = getParameters(request);
 			final RequestCode code = REQUEST_CODE_MAP.get(parameters
 					.get(JSON_CODE));
 			final String authentication = parameters.get(JSON_AUTH);
 			final String user = parameters.get(JSON_USER);
 			final String login = parameters.get(JSON_LOGIN);
 			final String password = parameters.get(JSON_PASSWORD);
 			final String oldPassword = parameters.get(JSON_OLD_PWD);
 			final String oldLogin = parameters.get(JSON_OLD_LOGIN);
 			final String application = parameters.get(JSON_APPLICATION);
 
 			switch (code) {
 			case CREATE:
 				message.setMethod(JSON_CODE_CREATE);
 
 				// Finalize the registration
 				String accessToken = parameters.get(JSON_ACCESS_TKN);
 				if (accessToken != null) {
 					//registrationManager.read(application, accessToken);
 					Map<String, String> auth = 
 			        		authenticationManager.readSimple("_to_validate_" + accessToken);
 					
 					Map<String, String> usr = profileManager.readSimple(auth.get("user"));
 					profileManager.update(usr.get("id"), "socialNetworkID", SOCIAL_NET_ID);
 					
 					Map<String, String> auth2 = authenticationManager.readSimple(
 							auth.get("login"));
 
 					if (auth2.size()<3){ // only if the user does not exist
 						authenticationManager.updateSimple(auth.get("login"), auth, "_to_validate_" + accessToken);
 						message.setDescription("user profile created");
 						break;
 					}
 					message.setStatus(AuthenticationManager.ERROR_CONFLICT);
 					message.setDescription("The login already exist");
 					break;
 					
 				} else if (authentication == null) {
 					throw new InternalBackEndException(
 							"authentication argument missing!");
 				} else if (user == null) {
 					throw new InternalBackEndException("user argument missing!");
 				} else {
 					// Launch the registration procedure
 
 					Map<String, String> usr = json_decode(user);
 					usr.put("socialNetworkID", SOCIAL_NET_ID + "_not_validated");
         		    usr.put("socialNetworkName", SOCIAL_NET_NAME);
 					
 					Map<String, String> auth = json_decode(authentication);
         		    
         		    LOGGER.info("Trying to create a new user:\n {}",
 							usr);
         		    LOGGER.info("Trying to create a new auth:\n {}",
 							auth);
         		    
         		    Map<String, String> auth2 = authenticationManager.readSimple(
 							auth.get("login"));
         		    
         		    if (auth2.size()<3){ //the login does not exist yet
         		    	
         		    	// We use the APP_NAME as the epsilon for the hash function
 				        final HashFunction hashFunc = new HashFunction(application);
 				        final String accToken = hashFunc.SHA1ToString(usr.get("login") 
 				        		+ System.currentTimeMillis());
 						
 				        LOGGER.info("there....."+accToken);
 						authenticationManager.updateSimple("_to_validate_" + accToken, auth, null);
 						LOGGER.info("ok....."+usr.get("id"));
 						profileManager.update(usr.get("id"), usr);//TODO check user contains id
 						
 						/* send confirmation mail   */  
 						authenticationManager.sendRegistrationEmailSimple( application, usr, accToken);
 						
 						LOGGER.info("registration email sent");
 						message.setDescription("registration email sent");
         		    }  
 
 				}
 				break;
 			case READ:
 				message.setMethod(JSON_CODE_READ);
 				if (login == null) {
 					throw new InternalBackEndException(
 							"login argument missing!");
 				} else if (password == null) {
 					throw new InternalBackEndException(
 							"password argument missing!");
 				} else {
 					String usrId = authenticationManager.readSimple(login, password);
 
 					message.setDescription("Successfully authenticated");
 					// TODO Remove this parameter
 					message.addDataObject(JSON_USER, usrId);
 
 					final MSessionBean sessionBean = new MSessionBean();
 					sessionBean.setIp(request.getRemoteAddr());
 					sessionBean.setUser(usrId);
 					sessionBean.setCurrentApplications("");
 					sessionBean.setP2P(false);
 					// TODO Use The Cassandra Timeout mechanism
 					sessionBean.setTimeout(System.currentTimeMillis());
 					final HashFunction h = new HashFunction(SOCIAL_NET_NAME);
 					accessToken = h.SHA1ToString(login + password
 							+ sessionBean.getTimeout());
 					sessionBean.setAccessToken(accessToken);
 					sessionBean.setId(accessToken);
 					sessionManager.create(sessionBean);
 
 					// Update the profile with the new session
 					profileManager.update(usrId, "session", accessToken);
 
 					LOGGER.info("Session {} created -> LOGIN", accessToken);
 					final StringBuffer urlBuffer = new StringBuffer(40);
 					urlBuffer.append(SERVER_PROTOCOL);
 					urlBuffer.append(SERVER_URI);
 
 					urlBuffer.trimToSize();
 
 					message.addDataObject("url", urlBuffer.toString());
 					message.addDataObject(JSON_ACCESS_TKN, accessToken);
 				}
 				break;
 			case UPDATE:
 				if (authentication == null) {
 					throw new InternalBackEndException(
 							"Missing authentication argument!");
 				} else if (oldLogin == null) {
 					throw new InternalBackEndException(
 							"oldLogin argument missing!");
 				} else if (oldPassword == null) {
 					throw new InternalBackEndException(
 							"oldPassword argument missing!");
 				} else {
 					
 					Map<String, String> auth = json_decode(authentication);
         		    
         		    authenticationManager.read(
 							auth.get("login"),
 							auth.get("password"));
         		    
         		    // no exception = update the Authentication
 					LOGGER.info("Trying to update authentication:\n {}",auth);
 					authenticationManager.updateSimple(auth.get("login"), auth, oldLogin); // @TODO check for login
 					LOGGER.info("Authentication updated!");
 					
 				}
 				break;
 			default:
 				throw new InternalBackEndException(
 						"AuthenticationRequestHandler(" + code + ") not exist!");
 			}
 		} catch (final AbstractMymedException e) {
 			LOGGER.debug("Error in doPost operation", e);
 			message.setStatus(e.getStatus());
 			message.setDescription(e.getMessage());
 		}
 
 		printJSonResponse(message, response);
 	}
 }
