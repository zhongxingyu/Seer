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
 
 import org.apache.log4j.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import java.util.Set;
 
 /**
  * Listens at a disconnect Request made by the user and destroys user id from the
  * the mappings in LoginController class and also from the session. The corresponding
  * mongo instance is also destroyed when all the tokenId corresponding to its
  * user mapping are destroyed.
  *
  * @author Rachit Mittal
  * @since 11 July 2011
  */
 
 @Path("/disconnect")
 public class LogoutController extends BaseController {
 
     private static final long serialVersionUID = 1L;
 
     /**
      * Define Logger for this class
      */
     private static Logger logger = Logger.getLogger(LogoutController.class);
 
     /**
      * Listens to a disconnect reuest made by user to end his session from mViewer.
      *
      * @param connectionId Mongo Db Configuration provided by user to connect to.
      * @return Logout status
      */
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public String doGet(@QueryParam("connectionId") final String connectionId, @Context final HttpServletRequest request) {
        String response = new ResponseTemplate().execute(logger, connectionId, request, new ResponseCallback() {
             public Object execute() throws Exception {
                 authService.disconnectConnection(connectionId);
                 HttpSession session = request.getSession();
                 Set<String> existingConnectionIdsInSession = (Set<String>) session.getAttribute("existingConnectionIdsInSession");
                 if (existingConnectionIdsInSession != null) {
                     existingConnectionIdsInSession.remove(connectionId);
                 }
                 String status = "User Logged Out";
                 return status;
             }
         });
         return response;
     }
 }
