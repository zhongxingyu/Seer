 package com.incidentlocator.client;
 
 import android.util.Log;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.app.ProgressDialog;
 import android.widget.Toast;
 
 import java.net.URL;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.io.OutputStream;
 import java.io.InputStream;
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.lang.CharSequence;
 
 import org.json.JSONObject;
 import org.json.JSONException;
 
 import com.incidentlocator.client.IncidentLocator;
 
 public class HttpRest {
     private static final String TAG = "IncidentLocator::HttpRest";
     private static final String PREFS = "IncidentLocatorPreferences";
 
     private String host = "http://10.0.2.2:3000/";
     private Context context = null;
     private SharedPreferences settings;
 
     private String cookie = null;
     private String csrf = null;
 
     public HttpRest(Context c) {
         context = c;
     }
 
     protected String getCookie() {
         if (cookie == null || cookie.equals("")) {
             settings = context.getSharedPreferences(PREFS, 0);
             cookie = settings.getString("cookie", "");
         }
         return cookie;
     }
 
     protected String getCsrf() {
         if (csrf == null || csrf.equals("")) {
             settings = context.getSharedPreferences(PREFS, 0);
             csrf = settings.getString("csrf", "");
         }
         return csrf;
     }
 
     public void setHost(String h) {
         host = h;
     }
 
     public void login(Map data) {
         new RestLogin().execute(data);
     }
 
     public void profile() {
         new RestProfile().execute();
     }
 
     public void report(Map data) {
         new RestReport().execute(data);
     }
 
     // -----------------------------------------------------------------------
     // helper methods
     // -----------------------------------------------------------------------
 
     protected String readStream(InputStream is) {
         try {
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             int i = is.read();
             while(i != -1) {
                 out.write(i);
                 i = is.read();
             }
             return out.toString();
         } catch (IOException e) {
             return "{}";
         }
     }
 
     protected void settingsSetBoolean(String key, boolean value) {
         settings = context.getSharedPreferences(PREFS, 0);
         SharedPreferences.Editor editor = settings.edit();
         editor.putBoolean(key, value);
         editor.commit();
     }
 
     protected void settingsSetString(String key, String value) {
         settings = context.getSharedPreferences(PREFS, 0);
         SharedPreferences.Editor editor = settings.edit();
         editor.putString(key, value);
         editor.commit();
     }
 
     protected String parseHttpHeader(HttpURLConnection conn, String field) {
         // conn.getHeaderFields() returns a map of the type:
         // Map<String, List<String>>
         List inner_list = conn.getHeaderFields().get(field);
 
         if (!inner_list.isEmpty()) {
             String value = inner_list.get(0).toString();
             return value;
         } else {
             return null;
         }
     }
 
     // extract the useful cookie part from the cookie value
     protected String getCookieFromHeaderValue(String cookie) {
         String[] cookie_parts = cookie.split(";");
         return cookie_parts[0];
     }
 
     // -----------------------------------------------------------------------
     // http async tasks
     // -----------------------------------------------------------------------
 
     private class RestLogin extends AsyncTask <Map, Void, Boolean> {
 
         ProgressDialog dialog = new ProgressDialog(context);
 
         @Override
         protected void onPreExecute() {
             dialog.setMessage("Sign in...");
             dialog.setIndeterminate(true);
             dialog.setCancelable(false);
             dialog.show();
         }
 
         @Override
         protected Boolean doInBackground(Map... data) {
             // default return value on errors
             String str_response = new String("{}");
 
             // hold response from api here
             String msg = new String("");
 
             JSONObject json_response = null;
 
             try {
                 JSONObject json_data = new JSONObject(data[0]);
                 byte[] byte_data = json_data.toString().getBytes("UTF-8");
 
                 URL url = new URL(host + "api/signin");
                 HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
 
                 OutputStream output = null;
                 try {
                     // make a POST request
                     urlConnection.setDoOutput(true);
 
                     urlConnection.setRequestProperty("Content-Type", "application/json");
                     urlConnection.setRequestProperty("Accept", "application/json");
                     urlConnection.setFixedLengthStreamingMode(byte_data.length);
 
                     output = urlConnection.getOutputStream();
                     output.write(byte_data);
                     output.flush();
                 } finally {
                     if (output != null) {
                         try {
                             output.close();
                         } catch (IOException logOrIgnore) {
                             Log.d(TAG, "io error");
                         }
                     }
                 }
 
                 // parse and store response header values
                 csrf = parseHttpHeader(urlConnection, "X-Csrf-Token");
                 String cookie_value = parseHttpHeader(urlConnection, "Set-Cookie");
                 cookie = getCookieFromHeaderValue(cookie_value);
                 settingsSetString("csrf", csrf);
                 settingsSetString("cookie", cookie);
 
                 // parse response body
                 InputStream response = new BufferedInputStream(urlConnection.getInputStream());
                 str_response = readStream(response);
                 json_response = new JSONObject(str_response);
 
             } catch (Exception e) {
                 Log.d(TAG, e.getLocalizedMessage());
                 Log.d(TAG, str_response);
                 return false;
             }
 
             try {
                 msg = json_response.getString("msg");
             } catch (JSONException e) {
                 Log.d(TAG, "could not get 'msg' from response");
                 Log.d(TAG, str_response);
                 return false;
             }
 
             Log.d(TAG, msg);
             return true;
         }
 
         protected void onPostExecute(Boolean result) {
             dialog.dismiss();
             settingsSetBoolean("logged_in", result.booleanValue());
 
             if (result == true) {
                 Log.d(TAG, "change view");
                 Intent main = new Intent(context, IncidentLocator.class);
                 context.startActivity(main);
             } else {
                 int duration = Toast.LENGTH_SHORT;
                 CharSequence text = "Cannot login to service";
                 Toast toast = Toast.makeText(context, text, duration);
                 toast.show();
             }
         }
     }
 
     private class RestProfile extends AsyncTask <Void, Void, JSONObject> {
 
         ProgressDialog dialog = new ProgressDialog(context);
 
         @Override
         protected void onPreExecute() {
            dialog.setMessage("Fetching user data...");
             dialog.setIndeterminate(true);
             dialog.setCancelable(false);
             dialog.show();
         }
 
         @Override
         protected JSONObject doInBackground(Void... arg0) {
             JSONObject json_response = null;
             InputStream response = null;
 
             try {
                 URL url = new URL(host + "api/profile");
 
                 HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                 urlConnection.setRequestMethod("GET");
                 urlConnection.setRequestProperty("Accept", "application/json");
                 urlConnection.setRequestProperty("Cookie", getCookie());
                 urlConnection.setRequestProperty("X-Csrt-Token", getCsrf());
 
                 response = new BufferedInputStream(urlConnection.getInputStream());
                 String str_response = readStream(response);
                 json_response = new JSONObject(str_response);
 
             } catch (Exception e) {
                 Log.d(TAG, e.getLocalizedMessage());
                 return json_response;
             } finally {
                 try {
                     if (response != null) {
                         response.close();
                     }
                 } catch (IOException logOrIgnore) {
                     Log.d(TAG, "io error");
                 }
             }
 
             Log.d(TAG, json_response.toString());
             return json_response;
         }
 
         protected void onPostExecute(JSONObject result) {
             dialog.dismiss();
 
             boolean success = false;
             if (result != null && result.length() > 0 && result.has("email") ) {
                 try {
                     success = true;
                     String email = result.getString("email");
                     //TODO set to UI
                 } catch (JSONException e) { }
             }
 
             if (!success) {
                 int duration = Toast.LENGTH_SHORT;
                 CharSequence text = "Cannot get user info";
                 Toast toast = Toast.makeText(context, text, duration);
                 toast.show();
             }
         }
     }
 
     private class RestReport extends AsyncTask <Map, Void, String> {
 
         ProgressDialog dialog = new ProgressDialog(context);
 
         @Override
         protected void onPreExecute() {
             dialog.setMessage("Sending report...");
             dialog.setIndeterminate(true);
             dialog.setCancelable(true);
             dialog.show();
         }
 
         @Override
         protected String doInBackground(Map... data) {
             // default return value on errors
             String str_response = new String("{}");
 
             // hold response from api here
             String msg = new String("");
 
             JSONObject json_response = null;
 
             try {
                 JSONObject json_data = new JSONObject(data[0]);
                 byte[] byte_data = json_data.toString().getBytes("UTF-8");
 
                 URL url = new URL(host + "api/report");
                 HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
 
                 OutputStream output = null;
                 try {
                     // make a POST request
                     urlConnection.setDoOutput(true);
 
                     urlConnection.setRequestProperty("Content-Type", "application/json");
                     urlConnection.setRequestProperty("Accept", "application/json");
                     urlConnection.setRequestProperty("Cookie", cookie);
                     urlConnection.setRequestProperty("X-Csrt-Token", csrf);
                     urlConnection.setFixedLengthStreamingMode(byte_data.length);
 
                     output = urlConnection.getOutputStream();
                     output.write(byte_data);
                     output.flush();
                 } finally {
                     if (output != null) {
                         try {
                             output.close();
                         } catch (IOException logOrIgnore) {
                             Log.d(TAG, "io error");
                         }
                     }
                 }
 
                 InputStream response = new BufferedInputStream(urlConnection.getInputStream());
                 str_response = readStream(response);
                 json_response = new JSONObject(str_response);
 
             } catch (Exception e) {
                 Log.d(TAG, e.getLocalizedMessage());
                 return str_response;
             }
 
             try {
                 msg = json_response.getString("msg");
             } catch (JSONException e) {
                 Log.d(TAG, "could not get 'msg' from response");
                 return str_response;
             }
 
             Log.d(TAG, msg);
             return msg;
         }
 
         protected void onPostExecute(String result) {
             dialog.dismiss();
         }
     }
 
 }
