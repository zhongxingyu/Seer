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
 package com.mymed.controller.core.requesthandler;
 
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.gson.JsonSyntaxException;
 import com.mymed.controller.core.exception.AbstractMymedException;
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.authentication.AuthenticationManager;
 import com.mymed.controller.core.manager.authentication.IAuthenticationManager;
 import com.mymed.controller.core.manager.profile.IProfileManager;
 import com.mymed.controller.core.manager.profile.ProfileManager;
 import com.mymed.controller.core.manager.registration.IRegistrationManager;
 import com.mymed.controller.core.manager.registration.RegistrationManager;
 import com.mymed.controller.core.manager.session.ISessionManager;
 import com.mymed.controller.core.manager.session.SessionManager;
 import com.mymed.controller.core.requesthandler.message.JsonMessage;
 import com.mymed.model.data.session.MAuthenticationBean;
 import com.mymed.model.data.session.MSessionBean;
 import com.mymed.model.data.user.MUserBean;
 import com.mymed.utils.HashFunction;
 
 /**
  * Servlet implementation class AuthenticationRequestHandler
  */
 public class AuthenticationRequestHandler extends AbstractRequestHandler {
 
     /**
      * Generated serial ID.
      */
     private static final long serialVersionUID = 8762837510508354508L;
 
     private IAuthenticationManager authenticationManager;
     private ISessionManager sessionManager;
     private IProfileManager profileManager;
     private IRegistrationManager registrationManager;
 
     /**
      * JSON 'login' attribute.
      */
     private static final String JSON_LOGIN = JSON.get("json.login");
 
     /**
      * JSON 'password' attribute.
      */
     private static final String JSON_PASSWORD = JSON.get("json.password");
 
     /**
      * JSON 'warning' attribute.
      */
     private static final String JSON_WARNING = JSON.get("json.warning");
 
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
             registrationManager = new RegistrationManager();
         } catch (final InternalBackEndException e) {
             LOGGER.debug("AuthenticationManager not accessible!", e);
            throw new ServletException("AuthenticationManager is not accessible because: " + e.getMessage()); // NOPMD
         }
     }
 
     /*
      * (non-Javadoc)
      * @see com.mymed.controller.core.requesthandler.AbstractRequestHandler#doGet
      * (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
     @Override
     public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
         final JsonMessage message = new JsonMessage(200, this.getClass().getName());
 
         try {
             final Map<String, String> parameters = getParameters(request);
 
             final RequestCode code = REQUEST_CODE_MAP.get(parameters.get(JSON_CODE));
             final String login = parameters.get(JSON_LOGIN);
             final String password = parameters.get(JSON_PASSWORD);
 
             switch (code) {
                 case READ :
                     message.setMethod(JSON_CODE_READ);
                     if (login == null) {
                         throw new InternalBackEndException("login argument missing!");
                     } else if (password == null) {
                         throw new InternalBackEndException("password argument missing!");
                     } else {
                         message.addData(JSON_WARNING, "METHOD DEPRECATED - POST method should be used instead of GET!");
                         final MUserBean userBean = authenticationManager.read(login, password);
                         message.setDescription("Successfully authenticated");
                         message.addData(JSON_USER, getGson().toJson(userBean));
                     }
                     break;
                 case DELETE :
                     throw new InternalBackEndException("not implemented yet...");
                 default :
                     throw new InternalBackEndException("AuthenticationRequestHandler(" + code + ") not exist!");
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
      * @see com.mymed.controller.core.requesthandler.AbstractRequestHandler#doPost
      * (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
     @Override
     public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
         final JsonMessage message = new JsonMessage(200, this.getClass().getName());
 
         try {
             final Map<String, String> parameters = getParameters(request);
             final RequestCode code = REQUEST_CODE_MAP.get(parameters.get(JSON_CODE));
             final String authentication = parameters.get(JSON_AUTH);
             final String user = parameters.get(JSON_USER);
             final String login = parameters.get(JSON_LOGIN);
             final String password = parameters.get(JSON_PASSWORD);
             final String oldPassword = parameters.get(JSON_OLD_PWD);
             final String oldLogin = parameters.get(JSON_OLD_LOGIN);
 
             switch (code) {
                 case CREATE :
                     message.setMethod(JSON_CODE_CREATE);
 
                     // Finalize the registration
                     String accessToken = parameters.get(JSON_ACCESS_TKN);
                     if (accessToken != null) {
                         registrationManager.read(accessToken);
                         message.setDescription("user profile created");
                     } else if (authentication == null) {
                         throw new InternalBackEndException("authentication argument missing!");
                     } else if (user == null) {
                         throw new InternalBackEndException("user argument missing!");
                     } else {
                         // Launch the registration procedure
                         try {
                             final MUserBean userBean = getGson().fromJson(user, MUserBean.class);
                             userBean.setSocialNetworkID(SOCIAL_NET_ID);
                             userBean.setSocialNetworkName(SOCIAL_NET_NAME);
 
                             final MAuthenticationBean authenticationBean = getGson().fromJson(authentication,
                                             MAuthenticationBean.class);
 
                             LOGGER.info("Trying to create a new user:\n {}", userBean.toString());
 
                             // Check if the login already exist
                             boolean loginAlreadyExist = true;
                             try {
                                 authenticationManager.read(authenticationBean.getLogin(),
                                                 authenticationBean.getPassword());
                             } catch (final IOBackEndException loginTestException) {
                                 if (loginTestException.getStatus() == 404) { // the login does not exist
                                     registrationManager.create(userBean, authenticationBean);
                                     LOGGER.info("registration email sent");
                                     message.setDescription("registration email sent");
                                     loginAlreadyExist = false;
                                 }
                             }
                             if (loginAlreadyExist) {
                                 throw new IOBackEndException("The login already exist!", 409);
                             }
                         } catch (final JsonSyntaxException e) {
                            LOGGER.debug("JSON format is not valid", e);
                            throw new InternalBackEndException("User/Authentication jSon format is not valid"); // NOPMD
                         }
                     }
                     break;
                 case READ :
                     message.setMethod(JSON_CODE_READ);
                     if (login == null) {
                         throw new InternalBackEndException("login argument missing!");
                     } else if (password == null) {
                         throw new InternalBackEndException("password argument missing!");
                     } else {
                         final MUserBean userBean = authenticationManager.read(login, password);
                         message.setDescription("Successfully authenticated");
                         // TODO Remove this parameter
                         message.addData(JSON_USER, getGson().toJson(userBean));
 
                         final MSessionBean sessionBean = new MSessionBean();
                         sessionBean.setIp(request.getRemoteAddr());
                         sessionBean.setUser(userBean.getId());
                         sessionBean.setCurrentApplications("");
                         sessionBean.setP2P(false);
                         // TODO Use The Cassandra Timeout mechanism
                         sessionBean.setTimeout(System.currentTimeMillis());
                         final HashFunction h = new HashFunction(SOCIAL_NET_NAME);
                         accessToken = h.SHA1ToString(login + password + sessionBean.getTimeout());
                         sessionBean.setAccessToken(accessToken);
                         sessionBean.setId(accessToken);
                         sessionManager.create(sessionBean);
 
                         // Update the profile with the new session
                         userBean.setSession(accessToken);
                         profileManager.update(userBean);
 
                         LOGGER.info("Session {} created -> LOGIN", accessToken);
                         final StringBuffer urlBuffer = new StringBuffer(40);
                         urlBuffer.append(SERVER_PROTOCOL);
                         urlBuffer.append(SERVER_URI);
 
                         urlBuffer.trimToSize();
 
                         message.addData("url", urlBuffer.toString());
                         message.addData(JSON_ACCESS_TKN, accessToken);
                     }
                     break;
                 case UPDATE :
                     if (authentication == null) {
                         throw new InternalBackEndException("Missing authentication argument!");
                     } else if (oldLogin == null) {
                         throw new InternalBackEndException("oldLogin argument missing!");
                     } else if (oldPassword == null) {
                         throw new InternalBackEndException("oldPassword argument missing!");
                     } else {
                         try {
                             final MAuthenticationBean authenticationBean = getGson().fromJson(authentication,
                                             MAuthenticationBean.class);
 
                             // verify the oldPassword
                             authenticationManager.read(oldLogin, oldPassword);
 
                             // no exception = update the Authentication
                             LOGGER.info("Trying to update authentication:\n {}", authenticationBean.toString());
                             authenticationManager.update(oldLogin, authenticationBean);
                             LOGGER.info("Authentication updated!");
                         } catch (final JsonSyntaxException e) {
                            LOGGER.debug("JSON format is not valid", e);
                            throw new InternalBackEndException("Authentication jSon format is not valid"); // NOPMD
                         }
                     }
                     break;
                 default :
                     throw new InternalBackEndException("AuthenticationRequestHandler(" + code + ") not exist!");
             }
         } catch (final AbstractMymedException e) {
             LOGGER.debug("Error in doPost operation", e);
             message.setStatus(e.getStatus());
             message.setDescription(e.getMessage());
         }
 
         printJSonResponse(message, response);
     }
 }
