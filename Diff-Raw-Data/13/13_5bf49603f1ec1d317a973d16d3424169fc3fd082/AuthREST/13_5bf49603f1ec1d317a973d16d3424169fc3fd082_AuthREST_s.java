 /*
  * Copyright 2013 - Elian ORIOU
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
 
 package fr.webmarket.backend.rest;
 
 import fr.webmarket.backend.auth.ClientSessionManager;
 import fr.webmarket.backend.datasource.DataSourcesBundle;
 import fr.webmarket.backend.log.LoggerBundle;
 import fr.webmarket.backend.model.ResponseWrapper;
 import fr.webmarket.backend.model.Session;
 import fr.webmarket.backend.model.User;
 import fr.webmarket.backend.utils.DigestUtils;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import java.util.UUID;
 
 @Path("/auth")
 public class AuthREST {
 
     @POST
     @Path("login")
     @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
     public Session login(@FormParam("username") String username,
                          @FormParam("pwd") String pwd) {
 
         // Auth result
         Session session = new Session();
 
         // Retrieve the user
         User u = DataSourcesBundle.getDefaultDataSource().getUsers()
                 .get(username);
         if (u == null) {
             LoggerBundle.getDefaultLogger().warn("Unrecognized user : '{}' !", username);
             return session;
         }
        session.setUser(u);
 
         // Check the password (compare MD5)
         String md5Pwd = DigestUtils.computeMD5(pwd);
         if (!u.getPwd().equals(md5Pwd)) {
            LoggerBundle.getDefaultLogger().warn("Wrong auth with for user : '{}' & pwd : '{}' !", username, pwd);
             return session;
         }
 
         LoggerBundle.getDefaultLogger().info("User '{}' successfuly logged in !", username);
 
         // Check if the session doesn't exists yet
         UUID sessionID = ClientSessionManager.getInstance().getSessionID(u);
         if (sessionID != null) {
             return session.setId(sessionID.toString());
         }
 
         return session.setId(ClientSessionManager.getInstance()
                 .createSession(u).toString());
     }
 
     @POST
     @Path("logout")
     public Session logout(@FormParam("username") String username) {
 
         Session session = new Session();
         User u = DataSourcesBundle.getDefaultDataSource().getUsers()
                 .get(username);
 
         // If the user is not found : error
         if (u == null) {
             return session;
         }
 
         // Destroy the session
         ClientSessionManager.getInstance().destroySession(u);
 
         return session;
     }
 
     @GET
     @Path("check")
     public ResponseWrapper checkSession(@QueryParam("sessionID") String sessionID) {
 
         boolean isValid = ClientSessionManager.getInstance().
                 checkSession(UUID.fromString(sessionID));
 
         return new ResponseWrapper().setStatus(isValid);
     }
 
 }
