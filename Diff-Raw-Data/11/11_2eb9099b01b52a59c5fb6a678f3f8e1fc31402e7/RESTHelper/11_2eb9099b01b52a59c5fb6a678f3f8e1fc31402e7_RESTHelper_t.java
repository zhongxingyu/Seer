 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.swareg.onsite.net;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 
 import com.swareg.onsite.crypto.CryptoHelper;
 import com.swareg.onsite.db.Registration;
 import com.swareg.onsite.db.RegistrationLevel;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import javax.ws.rs.core.MultivaluedMap;
 
 import net.sf.json.JSONObject;
 
 /**
  *
  * @author mmain
  */
 public class RESTHelper {
     protected static Client client = new Client();
     protected static ObjectMapper mapper = new ObjectMapper();
     protected static String url;
     protected static String eventId;
     protected static String apiKey;
     
     public static void setUrl(String url) {
         RESTHelper.url = url;
     }
     
     public static void setEventId(String eventId) {
         RESTHelper.eventId = eventId;
     }
     
     public static void setApiKey(String apiKey) {
         RESTHelper.apiKey = apiKey;
     }
     
     public static boolean sendRegistration(Registration registration) {
         HashMap<String, Object> data = new HashMap<String, Object>();
         HashMap<String, Object> jsonRet;
         
        data.put("hash", CryptoHelper.hmacMD5(apiKey.getBytes(), (eventId + registration.emailAddress + registration.registrationLevel).getBytes()).toString());
         data.put("email", registration.emailAddress);
         data.put("level", registration.registrationLevel);
         
         JSONObject jsonData = new JSONObject();
         jsonData.putAll(data);
         
         System.out.println("JSON: " + jsonData.toString());
         
         String s;
         
         try {
             WebResource webResource = client.resource(url + "JS/registerForEventOnsite.json");
 
             MultivaluedMap queryParams = new MultivaluedMapImpl();
             queryParams.add("eventId", eventId);
             
             s = webResource.queryParams(queryParams).put(String.class, jsonData.toString());
         } catch (Exception e) {
             return false;
         }
             
         System.out.println("RETURN: " + s);
         
         try {
             jsonRet = mapper.readValue(s, HashMap.class);
         } catch (Exception e) {
             System.out.println("Was expecting JSON data...");
             return false;
         }
 
         if (!jsonRet.get("status").equals("success")) {
             System.out.println("ERROR: " + jsonRet.get("message"));
             return false;
         }
         
         return true;
     }
     
     public static boolean testConnection() {
         try {
             WebResource webResource = client.resource(url + "JS/getRegistrationLevels.json");
 
             MultivaluedMap queryParams = new MultivaluedMapImpl();
             queryParams.add("eventId", eventId);
 
             String s = webResource.queryParams(queryParams).get(String.class);
             return true;
         } catch (Exception e) {
             return false;
         }
     }
     
     public static ArrayList<RegistrationLevel> getRegistrationLevels() {
         HashMap<String, Object> jsonRet;
         ArrayList<RegistrationLevel> levels = new ArrayList<RegistrationLevel>();
         String s;
 
         try {
             WebResource webResource = client.resource(url + "JS/getRegistrationLevels.json");
 
             MultivaluedMap queryParams = new MultivaluedMapImpl();
             queryParams.add("eventId", eventId);
 
             s = webResource.queryParams(queryParams).get(String.class);
         } catch (Exception e) {
             return null;
         }
 
         System.out.println("RETURN: " + s);
 
         try {
             jsonRet = mapper.readValue(s, HashMap.class);
         } catch (Exception e) {
             System.out.println("Was expecting JSON data...");
             return null;
         }
 
         if (!jsonRet.get("status").equals("success")) {
             System.out.println("ERROR: " + jsonRet.get("message"));
             return null;
         }
         else {
             ArrayList<HashMap<String, Object> > jsonLevels = (ArrayList)jsonRet.get("levels");
 
             for (HashMap<String, Object> level : jsonLevels) {
                 System.out.println("LEVEL: " + level);
 
                 RegistrationLevel rLevel = new RegistrationLevel((String)level.get("name"), (String)level.get("price"), (Integer)level.get("id"));
                 rLevel.save();
                 levels.add(rLevel);
             }
         }
 
         return levels;
     }
 }
