 /*
  * Copyright (c) 2011 Imaginea Technologies Private Ltd.
  * Hyderabad, India
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.imaginea.mongodb.controllers;
 
 import com.imaginea.mongodb.domain.ConnectionDetails;
 import com.imaginea.mongodb.domain.MongoConnectionDetails;
 import com.imaginea.mongodb.exceptions.ApplicationException;
 import com.imaginea.mongodb.exceptions.ErrorCodes;
 import com.imaginea.mongodb.services.impl.DatabaseServiceImpl;
 import com.mongodb.Mongo;
 import org.apache.log4j.Logger;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Authenticates User to Mongo Db by checking the user in <system.users>
  * collection of admin database.
  * <p/>
  * Here we also create a map of a mongo configuration which is mongo host and
  * mongoPort provided by user to a mongo Instance. This mongo Instance is used
  * for requests made to this database configuration. Also stores a map of active
  * users on a given mongo configuration for closing the mongo instance at time
  * of disconnect.
  *
  * @author Rachit Mittal
  * @since 10 July 2011
  */
 
 @Path("/login")
 public class LoginController extends BaseController {
 
     private static Logger logger = Logger.getLogger(LoginController.class);
 
     /**
      * Authenticates User by verifying Mongo config details against admin
      * database and authenticating user to that Db. A facility for guest login
      * is also allowed when both fields username and password are empty.
      * <p/>
      * Also stores a mongo instance based on database configuration.
      *
      * @param request   Request made by user for authentication
      * @param user      Name of user as in admin database in mongo
      * @param password  password of user as in admin database in mongo
      * @param host      mongo host to connect to
      * @param mongoPort mongo Port to connect to
      */
 
     @POST
     @Produces(MediaType.APPLICATION_JSON)
     public String authenticateUser(final @FormParam("username") String user, @FormParam("password") final String password, final @FormParam("host") String host, @FormParam("port") final String mongoPort,
                                    @FormParam("databases") final String databases, @Context final HttpServletRequest request) {
 
         String response = ErrorTemplate.execute(logger, new ResponseCallback() {
             public Object execute() throws Exception {
                 if ("".equals(host) || "".equals(mongoPort)) {
                     ApplicationException e = new ApplicationException(ErrorCodes.MISSING_LOGIN_FIELDS, "Missing Login Fields");
                     return formErrorResponse(logger, e);
                 }
                 HttpSession session = request.getSession();
                 Set<String> existingConnectionIdsInSession = (Set<String>) session.getAttribute("existingConnectionIdsInSession");
 
                 ConnectionDetails connectionDetails = new ConnectionDetails(host, Integer.parseInt(mongoPort), user, password, databases);
                 String connectionId = authService.authenticate(connectionDetails, existingConnectionIdsInSession);
                 if (existingConnectionIdsInSession == null) {
                     existingConnectionIdsInSession = new HashSet<String>();
                     session.setAttribute("existingConnectionIdsInSession", existingConnectionIdsInSession);
                 }
                 existingConnectionIdsInSession.add(connectionId);
                JSONObject tempResult = new JSONObject();
                JSONObject jsonResponse = new JSONObject();
                 try {
                    tempResult.put("result", "Login Success");
                    tempResult.put("connectionId", connectionId);
                    jsonResponse.put("response", tempResult);
                 } catch (JSONException e) {
                     logger.error(e);
                 }
                return jsonResponse;
             }
        }, false);
         return response;
     }
 
     @Path("/details")
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public String getConnectionDetails(@QueryParam("connectionId") final String connectionId, @Context final HttpServletRequest request) {
         String response = new ResponseTemplate().execute(logger, connectionId, request, new ResponseCallback() {
             public Object execute() throws Exception {
                 MongoConnectionDetails mongoConnectionDetails = authService.getMongoConnectionDetails(connectionId);
                 ConnectionDetails connectionDetails = mongoConnectionDetails.getConnectionDetails();
                 JSONObject jsonResponse = new JSONObject();
                 try {
                     jsonResponse.put("username", connectionDetails.getUsername());
                     jsonResponse.put("host", connectionDetails.getHostIp());
                     jsonResponse.put("port", connectionDetails.getHostPort());
                     jsonResponse.put("dbNames", new DatabaseServiceImpl(connectionId).getDbList());
                     jsonResponse.put("authMode", connectionDetails.isAuthMode());
                     jsonResponse.put("hasAdminLoggedIn", connectionDetails.isAdminLogin());
                 } catch (JSONException e) {
                     logger.error(e);
                 }
                 return jsonResponse;
             }
         });
         return response;
     }
 }
