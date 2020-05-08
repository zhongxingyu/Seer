 package de.hszigr.mobileapps.questionnaire.client.util;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 public final class Settings {
 
     public static final String NAME = "QuestionnaireClientSettings";
 
     public static final String DEFAULT_BASE_URL = "http://192.168.2.200:8080/questionnaire";
     public static final String BASE_URL = "BaseURL";
 
     public static String getBaseUrl(Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(BASE_URL, 0);
 
         return preferences.getString(BASE_URL, DEFAULT_BASE_URL);
     }
 
     private Settings() {
 
     }
 
 }
