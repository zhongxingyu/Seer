 package com.hackkrk.githubdetector.api;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 import java.net.MalformedURLException;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Configuration {
 
  public static Map<String, String> backendToUrl = new HashMap<String, String>() {
    {
      backendToUrl
          .put("J2EE", "http://10.0.1.74:8080/com.hackkrk.githubdetector/rest/gd");
      backendToUrl
          .put("Ruby", "http://10.0.1.74:8080/com.hackkrk.githubdetector/rest/gd");
      backendToUrl.put("Python",
          "http://10.0.1.74:8080/com.hackkrk.githubdetector/rest/gd");
      backendToUrl.put("Node.js",
          "http://10.0.1.74:8080/com.hackkrk.githubdetector/rest/gd");
    }
  };
 
   public static String getServerURL(Context context) throws MalformedURLException {
     return backendToUrl.get(getBackend(context));
   }
 
   private static String getBackend(Context context) {
     SharedPreferences defaultSharedPreferences = PreferenceManager
         .getDefaultSharedPreferences(context);
     String token = defaultSharedPreferences.getString(Preferences.BACKEND, null);
     return token;
   }
 }
