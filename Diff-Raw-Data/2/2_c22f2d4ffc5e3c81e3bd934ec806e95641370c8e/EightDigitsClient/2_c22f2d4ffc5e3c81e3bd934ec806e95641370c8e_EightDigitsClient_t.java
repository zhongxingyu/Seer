 package com.eightdigits.sdk;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.eightdigits.sdk.utils.UniqIdentifier;
 
 import android.app.Activity;
 import android.content.Context;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Display;
 import android.view.WindowManager;
 import android.widget.ImageView;
 
 public class EightDigitsClient {
 
   private Activity                activity;
   private Context                 context;
   private String                  urlPrefix;
   private String                  trackingCode;
   private String                  visitorCode;
   private String                  authToken;
   private String                  sessionCode;
   private String                  hitCode;
   private String                  username;
   private String                  password;
 
   private static Boolean          authRequestSent   = false;
   private static Boolean          newHitRequestSent = false;
   public static EightDigitsClient instance          = null;
 
   public static synchronized EightDigitsClient getInstance() {
     if (instance != null)
       return instance;
     return null;
   }
 
   public static synchronized EightDigitsClient createInstance(
       Object application, String urlPrefix, String trackingCode) {
     
     Activity activity = null;
     Context context = null;
     
     if(application instanceof Activity) {
       activity = (Activity) application;
     } else if(application instanceof Context) {
       context = (Context) application;
     }
       
     if (instance == null) {
       if(activity != null)
         instance = new EightDigitsClient(activity, urlPrefix, trackingCode);
       else if (context != null)
         instance = new EightDigitsClient(context, urlPrefix, trackingCode);
     }
       
 
     return instance;
   }
 
   private EightDigitsClient(Object application, String urlPrefix,
       String trackingCode) {
     this.setUrlPrefix(urlPrefix);
     this.setTrackingCode(trackingCode);
     
     if(application instanceof Activity)
       this.setActivity((Activity) application);
     else if(application instanceof Context)
       this.setContext((Context) context);
 
    String visitorCode = UniqIdentifier.id(trackingCode, this.activity != null ? this.activity.getApplicationContext() : this.getContext());
     this.setVisitorCode(visitorCode);
     
     Runnable apiRequestQueueRunnable = new EightDigitsApiRequestQueue(this);
     new Thread(apiRequestQueueRunnable).start();
   }
 
   /**
    * Authenticates client with username and password, returns authToken for api
    * calls.
    * 
    * @param username Your 8digits username
    * @param password Your 8digits password
    * @return
    */
   public void authWithUsername(String username, String password) {
 
     this.setUsername(username);
     this.setPassword(password);
 
     Map<String, String> params = new HashMap<String, String>(2);
     params.put(Constants.USERNAME, this.getUsername());
     params.put(Constants.PASSWORD, this.getPassword());
 
     EightDigitsResultListener callback = new EightDigitsResultListener() {
       @Override
       public void handleResult(JSONObject result) {
         try {
           String authToken = result.getJSONObject(Constants.DATA).getString(Constants.AUTH_TOKEN);
           EightDigitsClient.getInstance().setAuthToken(authToken);
         } catch (JSONException e) {
           logError(e.getMessage());
         }
       }
     };
 
     EightDigitsClient.authRequestSent = true;
     this.api("/api/auth", params, callback, EightDigitsApiRequestQueue.FIRST_PRIORITY);
   }
 
   /**
    * Calls authWithUsername method with existing username and password values
    */
   public void reAuth() {
     this.authWithUsername(this.getUsername(), this.getPassword());
   }
 
   /**
    * Call this method when your application Auth token should be created for
    * using this method. API returns hitCode and sessionCode. Method sets
    * sessionCode for later use, returns hitCode to you. If API call fails method
    * returns null.
    * 
    * @param title Title of your visit.
    * @param path Path for your application. Example : /home, /list
    * @return Returns hitCode for other events on screen.
    */
   public void newVisit(String title, String path) {
     // int systemVersion = android.os.Build.VERSION.SDK_INT;
     String model = "Linux";
     String userAgent = "Mozilla/5.0 (" + model + "; U; " + "Android "
         + android.os.Build.VERSION.RELEASE + "; " + android.os.Build.MODEL;
 
     /*
      * if (systemVersion >= 10) { userAgent += " " + android.os.Build.SERIAL; }
      */
 
     userAgent += " like Mac OS X; en-us) AppleWebKit (KHTML, like Gecko) Mobile/8A293 Safari";
     
     int width = 0;
     int height = 0;
     
     if(this.getActivity() != null) {
       DisplayMetrics metrics = new DisplayMetrics();
       activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
       width = metrics.widthPixels;
       height = metrics.heightPixels;
     } else if(this.getContext() != null) {
       WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
       Display d = wm.getDefaultDisplay();
       width = d.getWidth();
       height = d.getHeight();
     }
     
     String language = Locale.getDefault().getDisplayLanguage();
 
     Map<String, String> params = new HashMap<String, String>();
     params.put(Constants.AUTH_TOKEN, this.getAuthToken());
     params.put(Constants.TRACKING_CODE, this.getTrackingCode());
     params.put(Constants.VISITOR_CODE, this.getVisitorCode());
     params.put(Constants.PAGE_TITLE, title);
     params.put(Constants.PATH, path);
     params.put(Constants.SCREEN_WIDTH, Integer.valueOf(width).toString());
     params.put(Constants.SCREEN_HEIGHT, Integer.valueOf(height).toString());
     params.put(Constants.COLOR, "24");
     params.put(Constants.ACCEPT_LANG, language);
     params.put(Constants.FLASH_VERSION, "0.0.0");
     params.put(Constants.JAVA_ENABLED, "false");
     params.put(Constants.USER_AGENT, userAgent);
     params.put(Constants.DEVICE, android.os.Build.MANUFACTURER);
     params.put(Constants.VENDOR, android.os.Build.BRAND);
     params.put(Constants.MODEL, android.os.Build.MODEL);
 
     EightDigitsResultListener callback = new EightDigitsResultListener() {
       @Override
       public void handleResult(JSONObject result) {
         EightDigitsClient instance = EightDigitsClient.getInstance();
 
         JSONObject data;
         try {
           data = result.getJSONObject(Constants.DATA);
           instance.setHitCode(data.getString(Constants.HIT_CODE));
           instance.setSessionCode(data.getString(Constants.SESSION_CODE));
         } catch (JSONException e) {
           Log.e(Constants.EIGHT_DIGITS_SDK, e.getMessage());
         }
       }
     };
 
     EightDigitsClient.newHitRequestSent = true;
     this.api("/api/visit/create", params, callback, EightDigitsApiRequestQueue.SECOND_PRIORITY);
   }
 
   /**
    * You should call newScreen method in every activity except main activity.
    * (because newVisit method creates hitCode). Auth token should be created for
    * using this method.
    * 
    * @param title Title of your activity
    * @param path Path for your application. Example : /home, /list
    * @return
    */
   public void newScreen(String title, String path) {
     Map<String, String> params = new HashMap<String, String>();
     params.put(Constants.AUTH_TOKEN, this.getAuthToken());
     params.put(Constants.TRACKING_CODE, this.getTrackingCode());
     params.put(Constants.VISITOR_CODE, this.getVisitorCode());
     params.put(Constants.SESSION_CODE, this.getSessionCode());
     params.put(Constants.PAGE_TITLE, title);
     params.put(Constants.PATH, path);
 
     EightDigitsResultListener callback = new EightDigitsResultListener() {
 
       @Override
       public void handleResult(JSONObject result) {
         try {
           JSONObject data = result.getJSONObject(Constants.DATA);
           EightDigitsClient.getInstance().setHitCode(data.getString(Constants.HIT_CODE));
         } catch (JSONException e) {
           logError(e.getMessage());
         }
       }
     };
 
     EightDigitsClient.newHitRequestSent = true;
     this.api("/api/hit/create", params, callback, EightDigitsApiRequestQueue.SECOND_PRIORITY);
   }
 
   /**
    * Creates new event. This method makes async request to app. Auth token and
    * hitCode should be created for using this method.
    * 
    * @param key Key of your event
    * @param value Value of your event
    * @param hitCode hitCode for activity which you get from newScreen or new
    *          Visit method.
    */
   public void newEvent(String key, String value) {
     if (!EightDigitsClient.authRequestSent
         || !EightDigitsClient.newHitRequestSent) {
       String message = "Please authanticate and create a hit before sending new event.";
       logError(message);
     } else {
       Map<String, String> params = new HashMap<String, String>();
       params.put(Constants.AUTH_TOKEN, this.getAuthToken());
       params.put(Constants.TRACKING_CODE, this.getTrackingCode());
       params.put(Constants.VISITOR_CODE, this.getVisitorCode());
       params.put(Constants.SESSION_CODE, this.getSessionCode());
       params.put(Constants.HIT_CODE, this.getHitCode());
       params.put(Constants.KEY, key);
       params.put(Constants.VALUE, value);
       this.api("/api/event/create", params, null, EightDigitsApiRequestQueue.THIRD_PRIORITY);
     }
   }
 
   /**
    * Returns score for visitor. Auth token should be created for using this
    * method.
    * 
    * @return
    */
   public void score(EightDigitsResultListener callback) {
     if (!EightDigitsClient.authRequestSent) {
       String errorMessage = "Please authenticate before calling score method";
       Integer errorCode = -1001;
       callCallbackWithError(errorMessage, errorCode, callback);
       logError(errorMessage);
     } else {
       Map<String, String> params = new HashMap<String, String>();
       params.put(Constants.AUTH_TOKEN, this.getAuthToken());
       params.put(Constants.TRACKING_CODE, this.getTrackingCode());
       params.put(Constants.VISITOR_CODE, this.getVisitorCode());
       this.api("/api/visitor/score", params, callback, EightDigitsApiRequestQueue.THIRD_PRIORITY);
     }
   }
 
   /**
    * Returns badges of current user. If no badges found returns null. Auth token
    * should be created for using this method.
    * 
    * @return List of badge id's
    */
   public void badges(EightDigitsResultListener callback) {
     if (!EightDigitsClient.authRequestSent) {
       String errorMessage = "Please authenticate before calling badges method";
       Integer errorCode = -1002;
       callCallbackWithError(errorMessage, errorCode, callback);
       logError(errorMessage);
     } else {
       Map<String, String> params = new HashMap<String, String>();
       params.put(Constants.AUTH_TOKEN, this.getAuthToken());
       params.put(Constants.TRACKING_CODE, this.getTrackingCode());
       params.put(Constants.VISITOR_CODE, this.getVisitorCode());
       this.api("/api/visitor/badges", params, callback, EightDigitsApiRequestQueue.THIRD_PRIORITY);
     }
 
   }
 
   /**
    * End screen hit for current hit. You should call this method in onDestroy of
    * activity. This method makes async request to api therefore does not return
    * result.
    * 
    */
   public void endScreen() {
     Map<String, String> params = new HashMap<String, String>();
 
     if (!EightDigitsClient.authRequestSent
         || !EightDigitsClient.newHitRequestSent) {
       String errorMessage = "Please authenticate and create hit before calling endScreen method";
       logError(errorMessage);
     } else {
       params.put(Constants.AUTH_TOKEN, this.getAuthToken());
       params.put(Constants.TRACKING_CODE, this.getTrackingCode());
       params.put(Constants.VISITOR_CODE, this.getVisitorCode());
       params.put(Constants.SESSION_CODE, this.getSessionCode());
       params.put(Constants.HIT_CODE, this.getHitCode());
       this.api("/api/hit/end", params, null, EightDigitsApiRequestQueue.THIRD_PRIORITY);
     }
   }
 
   /**
    * End visit of user. You should call this method when user close your
    * application. Makes async api request. Auth token and sessionCode should be
    * created for using this method.
    */
   public void endVisit() {
     if (!EightDigitsClient.authRequestSent
         || !EightDigitsClient.newHitRequestSent) {
       String errorMessage = "Please authenticate and create hit before calling endVisit method";
       logError(errorMessage);
     } else {
       Map<String, String> params = new HashMap<String, String>();
       params.put(Constants.AUTH_TOKEN, this.getAuthToken());
       params.put(Constants.TRACKING_CODE, this.getTrackingCode());
       params.put(Constants.VISITOR_CODE, this.getVisitorCode());
       params.put(Constants.SESSION_CODE, this.getSessionCode());
       this.api("/api/visit/end", params, null, EightDigitsApiRequestQueue.THIRD_PRIORITY);
     }
   }
 
   /**
    * Fills content of ImageView with badge image for given badge ID
    * 
    * @param iv ImageView object for displaying image
    * @param badgeId Id of badge. You can find this ids in result of badges
    *          method.
    */
   public void badgeImage(ImageView iv, String badgeId) {
     String imageUrl = this.getUrlPrefix() + "/api/badge/image/" + badgeId;
     new DownloadImageTask(iv).execute(imageUrl);
   }
 
   /**
    * Main API request method
    * 
    * @param path Path of requested method
    * @param params Map of params
    * @return
    */
   private void api(String path, Map<String, String> params,
       EightDigitsResultListener callback, Integer priority) {
     String url = this.getUrlPrefix() + path;
     List<NameValuePair> pairs = this.generatePairsFromParams(params);
     EightDigitsApiRequestQueue.push(url, pairs, callback, priority);
   }
 
   /**
    * Generates key-value pairs for Http Request entity
    * 
    * @param params
    * @return
    */
   private List<NameValuePair> generatePairsFromParams(Map<String, String> params) {
     List<NameValuePair> pairs = new ArrayList<NameValuePair>();
 
     for (Map.Entry<String, String> entry : params.entrySet()) {
       pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
     }
     return pairs;
   }
 
   /**
    * Calls callback with error info
    * 
    * @param errorMessage
    * @param errorCode
    * @param callback
    */
   private void callCallbackWithError(String errorMessage, Integer errorCode,
       EightDigitsResultListener callback) {
     JSONObject result = new JSONObject();
     try {
       result.put(Constants.ERROR, true);
       result.put(Constants.ERROR_MESSAGE, errorMessage);
       result.put(Constants.ERROR_CODE, errorCode);
     } catch (JSONException e) {
       log(e.getMessage());
     }
     callback.handleResult(result);
   }
 
   /**
    * Creates new hit for activity at onResume event
    * 
    * @param title
    * @param path
    */
   public void onRestart(String title, String path) {
     this.setHitCode(null);
     EightDigitsClient.setNewHitRequestSent(false);
     this.newScreen(title, path);
   }
 
   public static void logError(String message) {
     Log.e(Constants.EIGHT_DIGITS_SDK, message);
   }
 
   public static void log(String message) {
     Log.d(Constants.EIGHT_DIGITS_SDK, message);
   }
 
   public Activity getActivity() {
     return activity;
   }
 
   public void setActivity(Activity activity) {
     this.activity = activity;
   }
 
   public String getUsername() {
     return username;
   }
 
   public void setUsername(String username) {
     this.username = username;
   }
 
   public String getPassword() {
     return password;
   }
 
   public void setPassword(String password) {
     this.password = password;
   }
 
   public String getAuthToken() {
     return authToken;
   }
 
   public void setAuthToken(String authToken) {
     this.authToken = authToken;
   }
 
   public String getSessionCode() {
     return sessionCode;
   }
 
   public void setSessionCode(String sessionCode) {
     this.sessionCode = sessionCode;
   }
 
   public String getUrlPrefix() {
     return urlPrefix;
   }
 
   public void setUrlPrefix(String urlPrefix) {
     this.urlPrefix = formatUrlPrefix(urlPrefix);
   }
 
   public String getTrackingCode() {
     return trackingCode;
   }
 
   public void setTrackingCode(String trackingCode) {
     this.trackingCode = trackingCode;
   }
 
   public String getVisitorCode() {
     return visitorCode;
   }
 
   public void setVisitorCode(String visitorCode) {
     this.visitorCode = visitorCode;
   }
 
   public String getHitCode() {
     return hitCode;
   }
 
   public void setHitCode(String hitCode) {
     this.hitCode = hitCode;
   }
 
   public static Boolean getAuthRequestSent() {
     return authRequestSent;
   }
 
   public static void setAuthRequestSent(Boolean authRequestSent) {
     EightDigitsClient.authRequestSent = authRequestSent;
   }
 
   public static Boolean getNewHitRequestSent() {
     return newHitRequestSent;
   }
 
   public static void setNewHitRequestSent(Boolean newHitRequestSent) {
     EightDigitsClient.newHitRequestSent = newHitRequestSent;
   }
   
   public Context getContext() {
     return context;
   }
 
   public void setContext(Context context) {
     this.context = context;
   }
   
   /**
    * Formats url
    * 
    * Remo
    * 
    * @param urlPrefix
    * @return
    */
   private String formatUrlPrefix(String urlPrefix) {
     if(!urlPrefix.startsWith(Constants.HTTP))
       urlPrefix = Constants.HTTP + urlPrefix;
     
     if(urlPrefix.endsWith(Constants.BACKSLASH))
       urlPrefix = urlPrefix.substring(0, urlPrefix.length() - 1);
     
     if(urlPrefix.endsWith(Constants.API))
       urlPrefix = urlPrefix.substring(0, urlPrefix.length() - (Constants.API.length() + 1));
      
     return urlPrefix;
   }
 
 }
