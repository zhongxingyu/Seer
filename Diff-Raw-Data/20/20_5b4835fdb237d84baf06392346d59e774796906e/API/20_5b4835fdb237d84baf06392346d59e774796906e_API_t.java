 package by.fksis.schedule;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.HashMap;
 
 public final class API {
     private static String username = null, accessKey = null;
     private static final String SHARED_PREFERENCES_NAME = "credentials";
     private static final String API_ROOT_URL = "http://fksis.bsuir.by/wps/schedule/api";
 
     public static boolean credentialsPresent() {
         return username != null;
     }
 
     public static void loadCredentials(Context context) {
         SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
         username = sharedPreferences.getString("username", null);
         accessKey = sharedPreferences.getString("accessKey", null);
     }
 
     public static void updateCredentials(Context context, String username, String accessKey) {
         SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
         sharedPreferences.edit()
                 .putString("username", username)
                 .putString("accessKey", accessKey)
                 .commit();
         loadCredentials(context);
     }
    public static void clearCredentials(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .remove("username")
                .remove("accessKey")
                .commit();
        loadCredentials(context);
    }
 
     public static String getUsername() {
         return username;
     }
 
     @SuppressWarnings("deprecation")
     public static String query(HashMap<String, String> params) throws IOException {
         try {
             String url = API_ROOT_URL + "?_";
             for (String key : params.keySet())
                 url += "&" + URLEncoder.encode(key) + "=" + URLEncoder.encode(params.get(key));
             String data = "", nextLine;
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
             while ((nextLine = bufferedReader.readLine()) != null)
                 data += nextLine;
             return data;
         } catch (MalformedURLException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     public static JSONObject queryUserData() throws IOException, JSONException {
         HashMap<String, String> params = new HashMap();
         params.put("info", "1");
         params.put("auth_login", username);
         params.put("auth_key", accessKey);
         return new JSONObject(query(params));
     }
 
     public static JSONArray queryClasses() throws IOException, JSONException {
         HashMap<String, String> params = new HashMap();
         params.put("classes", "1");
         return new JSONArray(query(params));
     }
 
     public static JSONArray queryBroadcasts() throws IOException, JSONException {
         HashMap<String, String> params = new HashMap();
         params.put("broadcasts", "1");
         return new JSONArray(query(params));
     }
 
     public static void queryAddBroadcasts(String text, String date) throws IOException, JSONException {
         HashMap<String, String> params = new HashMap();
         params.put("addBroadcast", "1");
         params.put("auth_login", username);
         params.put("auth_key", accessKey);
         params.put("text", text);
         params.put("expires", date);
         query(params);
     }
 }
