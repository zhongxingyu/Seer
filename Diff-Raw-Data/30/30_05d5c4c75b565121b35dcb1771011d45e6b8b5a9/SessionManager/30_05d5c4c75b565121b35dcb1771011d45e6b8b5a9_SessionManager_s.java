 package ch.adorsaz.loungeDroid.servercom;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.ConnectException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import ch.adorsaz.loungeDroid.activities.SettingsActivity;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * SessionManager is the class which manage connection with server, check if
  * always logged and execute a request.
  * */
 public final class SessionManager {
     /**
      * User login for rssLounge server.
      * */
     private static String mLogin = null;
     /**
      * User password for rssLounge server.
      * */
     private static String mPassword = null;
     /**
      * Url of the rssLounge server.
      * */
     private static String mServerUrl = null;
     /**
      * Session cookie received from server to keep session active.
      * */
     private static String mSessionCookie = null;
     /**
      * The unique instance of this class.
      * */
     private static SessionManager mSessionManager = null;
     /**
      * Application context, to get user prefrences.
      * */
     private static Context mApplicationContext = null;
 
     /**
      * Good response status as defined by HTTP.
      * */
     private static final int GOOD_RESPONSE_HTTP = 200;
     /**
      * login page of the rssLounge server.
      * */
     private static final String LOGIN_PAGE_RSSLOUNGE = "/index/login";
     /**
      * HttpGet parameter to give username.
      * */
     private static final String LOGIN_GET_RSSLOUNGE = "username";
     /**
      * HttpGet parameter to give password.
      * */
     private static final String PASSWORD_GET_RSSLOUNGE = "password";
     /**
      * HttpGet assignement to get json responses.
      * */
     protected static final String JSON_GET_RSSLOUNGE = "json=true";
 
     /**
      * Session cookie name.
      * */
     protected static final String SESSION_COOKIE_NAME = "PHPSESSID";
     /**
      * Seession cookie key to save cookie in shared preferences.
      * */
     protected static final String SESSION_COOKIE_SETTINGS =
             "session_cookie_settings";
 
     /**
      * Server tag for logcat.
      * */
     protected static final String LOG_SERVER = "loungeDroid.server";
     /**
      * Error to show when incorrect username/password.
      * */
     protected static final String BAD_LOGIN_ERROR =
             "Authentication failed : bad login and/or password.";
     /**
      * Error when malformed URL.
      * */
     protected static final String MALFORMED_URL =
             "There's a typo in url. Please check url : ";
     /**
      * Error when you don't have any response from the server.
      * */
     protected static final String NO_RESPONSE_SERVER_ERROR =
             "No server response. Check your settings.";
     /**
      * Error when a response wasn't expected as this.
      * */
     protected static final String UNEXPECTED_RESPONSE_ERROR =
             "There was a bug while connecting to server, please report it : ";
 
     /**
      * Instance getter.
      * @return actual SessionManager instance.
      * @param context context of the application to get preferences.
      * */
     public static SessionManager getInstance(final Context context) {
         if (mSessionManager == null) {
             mSessionManager = new SessionManager();
             mApplicationContext = context;
             mSessionManager.getPreferences();
         }
 
         return mSessionManager;
     }
 
     /**
      * Public method to do a server request.
      * @param pageUrl page to fetch.
      * @param httpParameters parameters to give to the server.
      * @return JSON response from the server.
      * @throws ConnectException if error occured during connection to the
      *             server.
      * */
     protected JSONObject serverRequest(
             final String pageUrl,
             final String httpParameters) throws ConnectException {
         if (mSessionCookie == null || mSessionCookie.length() == 0) {
             loginLounge();
         }
 
         return doRequest(pageUrl, httpParameters);
     }
 
     /**
      * Permits to delete session cookie.
      * */
     protected static void deleteSessionCookie() {
         mSessionCookie = null;
         Editor editor =
                 mApplicationContext.getSharedPreferences(
                         SettingsActivity.SHARED_PREFERENCES,
                         Activity.MODE_PRIVATE).edit();
         editor.remove(SessionManager.SESSION_COOKIE_SETTINGS);
         editor.commit();
     }
 
     /**
      * fetch server and try to log in.
      * @throws ConnectException if an error occurred during login.
      * */
     private void loginLounge() throws ConnectException {
         try {
             String urlParameters =
                     LOGIN_GET_RSSLOUNGE
                             + "="
                             + URLEncoder.encode(mLogin, "UTF-8")
                             + "&"
                             + PASSWORD_GET_RSSLOUNGE
                             + "="
                             + URLEncoder.encode(mPassword, "UTF-8")
                             + "&"
                             + JSON_GET_RSSLOUNGE;
 
             JSONObject jsonResponse =
                     doRequest(LOGIN_PAGE_RSSLOUNGE, urlParameters);
             if (jsonResponse != null) {
                 if (jsonResponse.getBoolean("success")) {
                     Log.d(LOG_SERVER, "Logged to the server.");
                     setCookiePref(mSessionCookie);
                 } else {
                     errorDisplayAndSettings(BAD_LOGIN_ERROR);
                     setCookiePref(null);
                     throw new ConnectException(BAD_LOGIN_ERROR);
                 }
             } else {
                 throw new ConnectException(NO_RESPONSE_SERVER_ERROR);
             }
         } catch (UnsupportedEncodingException e) {
             throw new ConnectException(UNEXPECTED_RESPONSE_ERROR
                     + " unsupported encoding exception.");
         } catch (JSONException e) {
             throw new ConnectException(UNEXPECTED_RESPONSE_ERROR
                     + " server didn't response success on login.");
         }
     }
 
     /**
      * Private constructor to do first instance.
      * */
     private SessionManager() {
     }
 
     /**
      * Get user preferences.
      * */
     private void getPreferences() {
         if (mApplicationContext != null) {
             SharedPreferences pref =
                     mApplicationContext.getSharedPreferences(
                             SettingsActivity.SHARED_PREFERENCES,
                             Activity.MODE_PRIVATE);
 
             mServerUrl = pref.getString(SettingsActivity.URL_SERVER_PREF, "");
 
             if (!mServerUrl.startsWith("http://")
                     && !mServerUrl.startsWith("https://")) {
                 mServerUrl = "http://" + mServerUrl;
             }
 
             mLogin = pref.getString(SettingsActivity.USER_SERVER_PREF, "");
             mPassword =
                     pref.getString(SettingsActivity.PASSWORD_SERVER_PREF, "");
 
             if (pref.contains(SESSION_COOKIE_SETTINGS)) {
                 mSessionCookie = pref.getString(SESSION_COOKIE_SETTINGS, "");
             }
         }
     }
 
     /**
      * Read completely buffer and pass it to a String.
      * @param inputStream stream to read.
      * @throws IOException if error occurred reading the buffer.
      * @return the String with buffer content.
      * */
     private String streamToString(final InputStream inputStream)
         throws IOException {
         BufferedReader br =
                 new BufferedReader(new InputStreamReader(inputStream));
 
         String result = br.readLine();
         String line = result;
 
         while (line != null) {
             result = result + "\n" + line;
             line = br.readLine();
         }
 
         return result;
     }
 
     /**
      * Performs the request on the server.
      * @param pageUrl @see serverRequest method
      * @param httpParameters @see serverRequest method
      * @return @see serverRequest method
      * @throws ConnectException @see serverRequest method
      * */
     private synchronized JSONObject doRequest(
             final String pageUrl,
             final String httpParameters) throws ConnectException {
         JSONObject jsonResponse = null;
         HttpURLConnection urlConnection = null;
 
         Log.d(LOG_SERVER, "Try to connect to "
                 + pageUrl
                 + " with cookie : "
                 + mSessionCookie);
         try {
             urlConnection =
                     (HttpURLConnection) new URL(mServerUrl + pageUrl)
                             .openConnection();
             Log.d(LOG_SERVER, "urlConnection successfully created");
             initiateUrlConnection(urlConnection, httpParameters);
             jsonResponse = treatHttpResponse(urlConnection);
         } catch (MalformedURLException e) {
             errorDisplayAndSettings(MALFORMED_URL + pageUrl);
         } catch (IOException e) {

            throw new ConnectException(UNEXPECTED_RESPONSE_ERROR
                    + " input/output ("
                    + e.getLocalizedMessage()
                    + ") exception for page request : "
                    + pageUrl);
         } catch (JSONException e) {
             throw new ConnectException(UNEXPECTED_RESPONSE_ERROR
                     + " unable to read json response"
                     + " correctly for page request "
                     + pageUrl);
         } finally {
             if (urlConnection != null) {
                 urlConnection.disconnect();
             }
         }
 
         if (jsonResponse == null) {
             Log.e(LOG_SERVER, "jsonResponse is null ! Cannot access page "
                     + pageUrl);
         }
         return jsonResponse;
     }
 
     /**
      * Initiate a connection to the page pageUrl with parameters httpParameters
      * on the given urlConnection.
      * @param urlConnection HttpURLConnection where to connect.
      * @param httpParameters http parameters to give to the server.
      * @throws IOException if there was an error while connecting to the server.
      * */
     private void initiateUrlConnection(
             final HttpURLConnection urlConnection,
             final String httpParameters) throws IOException {
         urlConnection.setDoOutput(true);
         urlConnection.setChunkedStreamingMode(0);
 
         if (mSessionCookie != null && !(mSessionCookie.length() == 0)) {
             urlConnection.setRequestProperty("Cookie", mSessionCookie);
         }
 
         DataOutputStream out =
                 new DataOutputStream(urlConnection.getOutputStream());
         out.writeBytes(httpParameters);
         out.flush();
         out.close();
         Log.d(LOG_SERVER, "urlConnection output stream successfully created");
 
         Log.d(
                 LOG_SERVER,
                 "Received response with status code "
                         + urlConnection.getResponseCode());
         Log.d(
                 LOG_SERVER,
                 "Received response with message "
                         + urlConnection.getResponseMessage());
     }
 
     /**
      * treat HttpResponse from an urlConnection.
      * @param urlConnection URL connection where to read the response.
      * @return a JSON response
      * @throws IOException if it was unable to read the response.
      * @throws JSONException if it was unable to translate response in JSON.
      * */
     private JSONObject treatHttpResponse(final HttpURLConnection urlConnection)
         throws IOException,
         JSONException {
         JSONObject jsonResponse = null;
         if (urlConnection.getResponseCode() == GOOD_RESPONSE_HTTP) {
             if (urlConnection.getHeaderField("Set-Cookie") != null) {
                 String cookie = urlConnection.getHeaderField("Set-Cookie");
                 if (cookie.startsWith(SESSION_COOKIE_NAME)) {
                     mSessionCookie =
                             SESSION_COOKIE_NAME
                                     + cookie.substring(
                                             cookie.indexOf('='),
                                             cookie.indexOf(';') + 1);
                     Log.d(LOG_SERVER, "Received cookie : " + mSessionCookie);
                 }
             }
 
             InputStream responseInput = urlConnection.getInputStream();
             jsonResponse = new JSONObject(streamToString(responseInput));
             responseInput.close();
         }
         return jsonResponse;
     }
 
     /**
      * Cookie saver.
      * @param cookie cookie to save.
      * */
     private void setCookiePref(final String cookie) {
         Editor editor =
                 mApplicationContext.getSharedPreferences(
                         SettingsActivity.SHARED_PREFERENCES,
                         Activity.MODE_PRIVATE).edit();
         editor.putString(SESSION_COOKIE_SETTINGS, cookie);
         mSessionCookie = cookie;
         editor.commit();
     }
 
     /**
      * Display a Toast with error message and call settings activity to check
      * them.
      * @param message error message.
      * */
     private void errorDisplayAndSettings(final String message) {
         Log.e(LOG_SERVER, message);
         Toast.makeText(mApplicationContext, message, Toast.LENGTH_LONG).show();
 
         SettingsActivity.setWantToEdit();
 
         Intent intent = new Intent(mApplicationContext, SettingsActivity.class);
         mApplicationContext.startActivity(intent);
     }
 }
