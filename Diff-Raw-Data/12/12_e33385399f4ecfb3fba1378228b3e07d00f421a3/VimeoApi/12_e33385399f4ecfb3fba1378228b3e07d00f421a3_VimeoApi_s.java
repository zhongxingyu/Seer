 /**
  * 
  */
 package org.vimeoid.connection;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.security.NoSuchAlgorithmException;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.Uri;
 import android.util.Log;
 
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
 import oauth.signpost.exception.OAuthCommunicationException;
 import oauth.signpost.exception.OAuthExpectationFailedException;
 import oauth.signpost.exception.OAuthMessageSignerException;
 import oauth.signpost.exception.OAuthNotAuthorizedException;
 
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.vimeoid.VimeoConfig;
 import org.vimeoid.util.ApiParams;
 import org.vimeoid.util.Dialogs;
 
 /**
  * <dl>
  * <dt>Project:</dt> <dd>vimeoid</dd>
  * <dt>Package:</dt> <dd>org.vimeoid</dd>
  * </dl>
  *
  * <code>VimeoApi</code>
  *
  * <p>Description</p>
  *
  * @author Ulric Wilfred <shaman.sir@gmail.com>
  * @date Aug 25, 2010 9:30:14 PM 
  *
  */
 public class VimeoApi {
     
     private final static String TAG = "VimeoApi";
     
     private static final String ADV_API_NAMESPACE = "vimeo";
     
     public static final String RESPONSE_FORMAT = "json";
     public static final int ITEMS_PER_PAGE = 20;
     public static final int MAX_NUMBER_OF_PAGES = 3;
     
     public static final String WEBVIEW_USER_AGENT = null; // default
     
     public static final Uri OAUTH_CALLBACK_URL = Uri.parse("vimeoid://oauth.done");
     public static final String OAUTH_API_PREFERENCES_ID = "org.vimeoid.vimeoauth";
     
     private static final String OAUTH_TOKEN_PARAM = "user_oauth_public";
     private static final String OAUTH_TOKEN_SECRET_PARAM = "user_oauth_secret";
     private static final String USER_ID_PARAM = "user_id";
     private static final String USERNAME_PARAM = "username";
     
     private static final String PLAYER_URL = "http://player.vimeo.com/video/";
     
     public static class UserLogin {
         public long id;
         public String username;
     }
     
     private VimeoApi() { };
 
     /* ====================== General methods =============================== */
     
     public static boolean connectedToWeb(Context context) {
         Log.d(TAG, "Testing connection to web");
         //ConnectivityManager connection =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
         
         /* return (connection.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||  
                    connection.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED); */
         
         return true; /* connection.getActiveNetworkInfo().isConnectedOrConnecting(); */
     }
     
     public static boolean vimeoSiteReachable(Context context) {
         Log.d(TAG, "Testing connection to Vimeo site");
         
         /* TODO: Requires android.permission.CHANGE_NETWORK_STATE
         ConnectivityManager connection =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
         final int vimeoHost = Utils.lookupHost("vimeo.com");
         return connection.requestRouteToHost(ConnectivityManager.TYPE_WIFI, vimeoHost) ||
         	   connection.requestRouteToHost(ConnectivityManager.TYPE_MOBILE, vimeoHost) ||
         	   connection.requestRouteToHost(ConnectivityManager.TYPE_WIMAX, vimeoHost)*/
         	   
         return true;
         	   
     }
     
     /* ====================== OAuth Helpers ================================= */
     
     public static boolean weKnowUser(Context context) {
         final SharedPreferences storage = context.getSharedPreferences(OAUTH_API_PREFERENCES_ID, Context.MODE_PRIVATE);
         return storage.contains(OAUTH_TOKEN_PARAM) && storage.contains(OAUTH_TOKEN_SECRET_PARAM);
     }
     
     public static void forgetCredentials(Context context) {
         final SharedPreferences storage = context.getSharedPreferences(OAUTH_API_PREFERENCES_ID, Context.MODE_PRIVATE);
         Editor editor = storage.edit();  
         editor.remove(OAUTH_TOKEN_PARAM);
         editor.remove(OAUTH_TOKEN_SECRET_PARAM);
         editor.remove(USER_ID_PARAM);
         editor.remove(USERNAME_PARAM);
         editor.commit();
     }
     
     public static boolean ensureOAuth(Context context) {
         if (!JsonOverHttp.use().isOAuthInitialized()) {
             JsonOverHttp.use().subscribeOAuth(
                     new CommonsHttpOAuthConsumer(VimeoConfig.VIMEO_API_KEY, 
                                                  VimeoConfig.VIMEO_SHARED_SECRET), 
                     new CommonsHttpOAuthProvider(VimeoConfig.VIMEO_OAUTH_API_ROOT + "/request_token", 
                                                  VimeoConfig.VIMEO_OAUTH_API_ROOT + "/access_token", 
                                                  VimeoConfig.VIMEO_OAUTH_API_ROOT + "/authorize"));
             // check if we already have token
             final SharedPreferences storage = context.getSharedPreferences(OAUTH_API_PREFERENCES_ID, Context.MODE_PRIVATE);
             final String oToken = storage.getString(OAUTH_TOKEN_PARAM, null);
             final String oTokenSecret = storage.getString(OAUTH_TOKEN_SECRET_PARAM, null);
             if ((oToken != null) && (oTokenSecret != null)) {
                 JsonOverHttp.use().iKnowTokens(oToken, oTokenSecret);
                 return true;
             } else return false;
         } return true;
     }
         
     public static Uri requestForOAuthUri() throws OAuthMessageSignerException, OAuthNotAuthorizedException, 
                                            OAuthExpectationFailedException, OAuthCommunicationException {
        return JsonOverHttp.use().retreiveOAuthRequestToken(OAUTH_CALLBACK_URL);
     }
     
     public static void ensureOAuthCallbackAndSaveToken(Context context, Uri callbackUri) throws 
                                                        OAuthMessageSignerException, OAuthNotAuthorizedException, 
                                                        OAuthExpectationFailedException, OAuthCommunicationException, 
                                                        IllegalCallbackUriException {
         if (callbackUri.toString().startsWith(OAUTH_CALLBACK_URL.toString())) {
             final JsonOverHttp joh = JsonOverHttp.use(); 
             joh.retreiveOAuthAccessToken(callbackUri);
             
             final SharedPreferences storage = context.getSharedPreferences(OAUTH_API_PREFERENCES_ID, Context.MODE_PRIVATE);
             Editor editor = storage.edit();  
             editor.putString(OAUTH_TOKEN_PARAM, joh.getOAuthToken());
             editor.putString(OAUTH_TOKEN_SECRET_PARAM, joh.getOAuthTokenSecret());
             editor.commit();
             
         } else throw new IllegalCallbackUriException("Illegal callback Uri passed");
     }
     
     /* ====================== Advanced API: Calls =========================== */
     
     public static JSONObject advancedApi(final String method) 
                                                         throws ClientProtocolException, NoSuchAlgorithmException, 
                                                                OAuthMessageSignerException, OAuthExpectationFailedException, 
                                                                OAuthCommunicationException, JSONException, IOException, 
                                                                URISyntaxException, AdvancedApiCallError {
         
         return advancedApi(method, new ApiParams());
     }
     
     public static JSONObject advancedApi(final String method, ApiParams params) 
                                                                     throws ClientProtocolException, NoSuchAlgorithmException, 
                                                                            OAuthMessageSignerException, OAuthExpectationFailedException, 
                                                                            OAuthCommunicationException, JSONException, IOException, 
                                                                            URISyntaxException, AdvancedApiCallError {
         params.add("method", ADV_API_NAMESPACE + "." + method);
         params.add("format", RESPONSE_FORMAT);
         JSONObject result = JsonOverHttp.use().signedAskForObject(new URI(VimeoConfig.VIMEO_ADVANCED_API_ROOT), params);
         if (!"ok".equals(result.getString("stat"))) {
             final JSONObject errObj = result.getJSONObject("err");
             throw new AdvancedApiCallError(errObj.getInt("code"), errObj.getString("msg"));
         }
         return result;
     }
     
     public static void saveUserLoginData(Context context, UserLogin loginData) {
         final SharedPreferences storage = context.getSharedPreferences(OAUTH_API_PREFERENCES_ID, Context.MODE_PRIVATE);
         Editor editor = storage.edit();  
         editor.putLong(USER_ID_PARAM, loginData.id);
         editor.putString(USERNAME_PARAM, loginData.username);
         editor.commit();        
     }
     
     public static UserLogin getUserLoginData(Context context) {
         final UserLogin login = new UserLogin(); 
         final SharedPreferences storage = context.getSharedPreferences(OAUTH_API_PREFERENCES_ID, Context.MODE_PRIVATE);
         login.id = storage.getLong(USER_ID_PARAM, -1);
         login.username = storage.getString(USERNAME_PARAM, "");
         return login;        
     }
     
     /* ====================== Advanced Api: Exceptions / Errors ============= */
     
     public static void handleApiError(Context context, AdvancedApiCallError error) {
         Log.e(TAG, error.getLocalizedMessage());
         Dialogs.makeExceptionToast(context, "API Call error", error);
     }
     
     @SuppressWarnings("serial")
     public static class IllegalCallbackUriException extends Exception {
 
         public IllegalCallbackUriException(String description) {
             super(description);
         }
         
     }
     
     @SuppressWarnings("serial")
     public static class AdvancedApiCallError extends Exception {
         
         public final int code;
         public final String message;
         
         public AdvancedApiCallError(int code, String message) {
             super(code + ": " + message);
             
             this.code = code;
             this.message = message;
         }
         
     }
     
     public static class ApiError {
         
         public static int INVALID_SIGNATURE = 96;
         public static int MISSING_SIGNATURE = 97;
         public static int LOGIN_FAILED_OR_INVALID_TOKEN = 98;
         public static int INSUFFICIENT_PERMISSIONS = 99;
         
         public static int SERVICE_UNAVAILABLE = 105;
         
         public static int FORMAT_NOT_FOUND = 111;
         public static int METHOD_NOT_FOUND = 112;
         
         public static int INVALID_CONSUMER_KEY = 301;
         public static int INVALID_OR_EXPIRED_TOKEN = 302;
         public static int INVALID_OAUTH_SIGNATURE = 303;
         public static int INVALID_OAUTH_NONCE = 304;
         public static int INVALID_OAUTH_SIGNATURE2 = 305;        
         public static int UNSUPPORTED_SIGNATURE_METHOD = 306;
         
         public static int MISSING_REQUIRED_PARAMETER = 307;
         public static int DUPLICATE_PARAMETER = 308;
         
         public static int RATE_LIMIT_EXCEEDED = 999;
         
     }
 
     /* ====================== Helpers ======================================= */    
     
     public static String getPlayerUrl(long videoId, int height) {
         Log.d(TAG, "Construction player URL for video " + videoId);
         return PLAYER_URL + videoId + "?title=0&byline=0&portrait=0&js_api=1&fp_version=10&height=" + height;
     }
 
 }
