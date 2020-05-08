 // Copyright (c) 2012 Tim Niblett. All Rights Reserved.
 //
 // File:        PersonaFindUser.java  (02-Oct-2012)
 // Author:      tim
 //
 // Copyright in the whole and every part of this source file belongs to
 // Tim Niblett (the Author) and may not be used, sold, licenced, 
 // transferred, copied or reproduced in whole or in part in 
 // any manner or form or in or on any media to any person other than 
 // in accordance with the terms of The Author's agreement
 // or otherwise without the prior written consent of The Author.  All
 // information contained in this source file is confidential information
 // belonging to The Author and as such may not be disclosed other
 // than in accordance with the terms of The Author's agreement, or
 // otherwise, without the prior written consent of The Author.  As
 // confidential information this source file must be kept fully and
 // effectively secure at all times.
 //
 
 
 package com.cilogi.shiro.gae;
 
 import com.google.appengine.api.urlfetch.*;
import com.google.appengine.api.utils.SystemProperty;
 import com.google.common.base.Charsets;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Logger;
 
 
 public class PersonaFindUser {
     static final Logger LOG = Logger.getLogger(PersonaFindUser.class.getName());
 
     private static final String PERSONA_VERIFY_URL = "https://verifier.login.persona.org/verify";
 
     private final JSONObject json;
 
     public PersonaFindUser(String token) {
        json = doPersonaCheck(token, hostName());
     }
 
     public String doFind() {
         if (isOkay()) {
             UserDAO dao = new UserDAO();
             String email = userEmail();
             if (email != null) {
                 GaeUser user = dao.findUser(email);
                 if (user == null) {
                     dao.saveUser(new GaeUser(email), true);
                 }
                 return email;
             }
         }
         return null;
     }
 
     private boolean isOkay() {
         try {
             return json.has("status") && "okay".equals(json.getString("status"));
         } catch (JSONException e) {
             LOG.warning("Error with JSON parse of reply from persona (status): " + e.getMessage());
             return false;
         }
     }
 
     private String userEmail() {
         try {
             if (json.has("email")) {
                 return json.getString("email");
             }
             return null;
         } catch (JSONException e) {
             LOG.warning("Error with JSON parse of reply from persona (email): " + e.getMessage());
             return null;
         }
 
     }
 
 
     private static JSONObject doPersonaCheck(String token, String host) {
         URLFetchService service = URLFetchServiceFactory.getURLFetchService();
         try {
             HTTPRequest request = new HTTPRequest(personaCheckURL(token, host), HTTPMethod.POST);
             HTTPResponse response = service.fetch(request);
             if (response.getResponseCode() == 200) {
                 byte[] data = response.getContent();
                 String jsonString = new String(data, Charsets.UTF_8);
                 JSONObject obj = new JSONObject(jsonString);
                 return obj;
             } else {
                 return new JSONObject();
             }
         } catch (Exception e) {
             return new JSONObject();
         }
     }
 
     private static URL personaCheckURL(String token, String host) throws MalformedURLException {
         return new URL(PERSONA_VERIFY_URL + "?assertion=" + token + "&audience=" + host);
     }
 
    private static String hostName() {
        return isDevelopmentServer() ? "localhost:8080" : "personashiro.appspot.com";
    }

    private static boolean isDevelopmentServer() {
        SystemProperty.Environment.Value server = SystemProperty.environment.value();
        return server == SystemProperty.Environment.Value.Development;
    }

 }
