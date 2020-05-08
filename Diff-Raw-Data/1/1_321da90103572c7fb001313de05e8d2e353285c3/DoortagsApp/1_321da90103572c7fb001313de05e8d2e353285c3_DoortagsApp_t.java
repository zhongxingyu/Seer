 package io.doortags.android;
 
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import io.doortags.android.api.DoortagsApiClient;
 import io.doortags.android.api.DoortagsApiException;
 
 import java.io.IOException;
 
 public class DoortagsApp extends Application {
     private SharedPreferences prefs;
     private DoortagsApiClient client;
 
     private static final String TOKEN_KEY = "auth_token";
 
     @Override
     public void onCreate() {
         super.onCreate();
 
         prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
         // initialize API client
         String authToken = prefs.getString(TOKEN_KEY, null);
         if (authToken == null) {
             client = null;
         } else {
             client = DoortagsApiClient.fromAuthToken(authToken);
         }
     }
 
     public SharedPreferences getPrefs() {
         return prefs;
     }
 
     public DoortagsApiClient getClient() {
         return client;
     }
 
     public void initClient(String email, String password)
             throws IOException, DoortagsApiException {
         client = DoortagsApiClient.authorize(email, password);
         SharedPreferences.Editor prefEditor = prefs.edit();
         prefEditor.putString(TOKEN_KEY, client.getAuthToken());
        prefEditor.commit();
     }
 }
