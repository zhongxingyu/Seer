 /*
  * Copyright 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.browsertophone.c2dm.server;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.jdo.JDOObjectNotFoundException;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 /**
  * Stores config information related to data messaging.
  * 
  */
 class C2DMConfigLoader {
     private final PersistenceManagerFactory PMF;
     private static final Logger log = Logger.getLogger(C2DMConfigLoader.class.getName());
 
     String currentToken;
     String c2dmUrl;
     
     C2DMConfigLoader(PersistenceManagerFactory pmf) {
         this.PMF = pmf;
     }
 
     /**
      * Update the token. 
      * 
      * Called on "Update-Client-Auth" or when admins set a new token.
      * @param token
      */
     public void updateToken(String token) {
         if (token != null) {
             currentToken = token;
             PersistenceManager pm = PMF.getPersistenceManager();
             try {
                 getDataMessagingConfig(pm).setAuthToken(token);
             } finally {
                 pm.close();
             }
         }
     }
     
     /** 
      * Token expired
      */
     public void invalidateCachedToken() {
         currentToken = null;
     }
 
     /**
      * Return the auth token from the database. Should be called 
      * only if the old token expired.
      *
      * @return
      */
     public String getToken() {
         if (currentToken == null) {
             currentToken = getDataMessagingConfig().getAuthToken();
         } 
         return currentToken;
     }
     
     public String getC2DMUrl() {
         if (c2dmUrl == null) {
             c2dmUrl = getDataMessagingConfig().getC2DMUrl();
         }
         return c2dmUrl;
     }
     
     public C2DMConfig getDataMessagingConfig() {
         PersistenceManager pm = PMF.getPersistenceManager();
         try {
             C2DMConfig dynamicConfig = getDataMessagingConfig(pm);
             return dynamicConfig;
         } finally {
             pm.close();
         }
     }
 
     private C2DMConfig getDataMessagingConfig(PersistenceManager pm) {
         Key key = KeyFactory.createKey(C2DMConfig.class.getSimpleName(), 1);
         C2DMConfig dmConfig = null;
         try {
             dmConfig = pm.getObjectById(C2DMConfig.class, key);
         } catch (JDOObjectNotFoundException e) {
             // Create a new JDO object
             dmConfig = new C2DMConfig();
             dmConfig.setKey(key);
             // Must be in classpath, before sending. Do not checkin !
             try {
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("dataMessagingToken.txt");
                 if (is != null) {
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                     String token = reader.readLine();
                     dmConfig.setAuthToken(token);
                 }
             } catch (Throwable t) {
                 log.log(Level.SEVERE, 
                         "Can't load initial token, use admin console", t);
             }
             
             
             pm.makePersistent(dmConfig);
         }
         return dmConfig;
     }
 }
