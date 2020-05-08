 /**
  * File: KahunaSdk.java
  * Created: 4/3/13
  * Author: Viacheslav Panasenko
  */
 package com.breezy.android.sample;
 
 import android.content.Context;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.os.Build;
 import android.provider.Settings;
 import android.util.Log;
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 /**
  * KahunaSdk
  * Represents Kahuna (http://usekahuna.com) SDK helper class. See documentation
  * at http://app.usekahuna.com/tap/getstarted/androidweb/ for more details.
  */
 public class KahunaSdk
 {
     private static final String TAG = KahunaSdk.class.getSimpleName();
     private static final String ANDROID_OS = "Android";
     private static final String KAHUNA_ENDPOINT = "https://tap-nexus.appspot.com/log";
 
     private static final String API_KEY_FIELD = "key";
    private static final String DEVICE_ID_FIELD = "dev_Id";
     private static final String USERNAME_FIELD = "username";
     private static final String USER_EMAIL_FIELD = "user_email";
     private static final String FIRST_NAME_FIELD = "first_name";
     private static final String LAST_NAME_FIELD = "last_name";
     private static final String APP_NAME_FIELD = "app_name";
     private static final String APP_VERSION_FIELD = "app_ver";
     private static final String OS_NAME_FIELD = "os_name";
     private static final String OS_VERSION_FIELD = "os_version";
     private static final String DEVICE_MODEL_NAME_FIELD = "dev_name";
     private static final String EVENT_FIELD = "event";
 
     public interface KahunaEvents
     {
         static final String START = "start";
 
         // TODO: add your list of events here
         static final String KAHUNA_SAMPLE_ACTIVITY = "KahunaSampleActivity";
     }
 
     private static KahunaSdk _instance;
 
     private AsyncHttpClient mHttpClient;
 
     // Required fields
     private String mApiKey;
     private String mDeviceId;
 
     // Optional, but highly recommended
     private String mUsername;
     private String mUserEmail;
     private String mFirstName;
     private String mLastName;
     private String mAppName;
     private String mAppVersion;
     private String mOsName = ANDROID_OS;
     private String mOsVersion;
     private String mDeviceModelName;
 
     private boolean mIsInitialized;
 
     /**
      * Returns an instance of the KahunaSdk helper class.
      * @return instance of the KahunaSdk helper class.
      */
     public static KahunaSdk getInstance()
     {
         if (_instance == null)
         {
             _instance = new KahunaSdk();
         }
 
         return _instance;
     }
 
     /**
      * Initialize the sdk with minimum required parameters.
      * @param context Application context used to retrieve unique device id.
      * @param apiKey Your Account's Secret Key (API key).
      */
     public void initSdk(Context context, String apiKey)
     {
         if (apiKey == null || apiKey.equals("") || context == null)
         {
             mIsInitialized = false;
             throw new IllegalArgumentException("API key and device ID should not be empty!");
         }
 
         mApiKey = apiKey;
         mDeviceId = getDeviceId(context);
         mIsInitialized = true;
     }
 
     /**
      * Sets user details for Kahuna SDK helper.
      * @param username Username.
      * @param userEmail User email.
      */
     public void setUserDetails(String username, String userEmail)
     {
         mUsername = username;
         mUserEmail = userEmail;
     }
 
     /**
      * Sets application and OS details (version, name) using passed context.
      * @param appContext An instance of the application context.
      * @return true if successful, false if errors occurred.
      */
     public boolean setAppAndOsDetails(Context appContext)
     {
         boolean isSuccessful = true;
 
         try
         {
             PackageManager pm = appContext.getPackageManager();
             PackageInfo info = pm.getPackageInfo(appContext.getPackageName(), 0);
             mAppVersion = info.versionName;
             if (info.applicationInfo != null)
             {
                 mAppName = pm.getApplicationLabel(info.applicationInfo).toString();
             }
             else
             {
                 mAppName = appContext.getString(R.string.app_name);
             }
 
         } catch (PackageManager.NameNotFoundException e)
         {
             Log.e(TAG, "Error retrieving package info: ", e);
             isSuccessful = false;
         }
 
         mOsVersion = Build.VERSION.RELEASE;
         mDeviceModelName = Build.MODEL;
         return isSuccessful;
     }
 
     /**
      * Sends given event with previously assigned data.
      * @param event Event to be sent to Kahuna SDK.
      */
     public void sendEvent(String event)
     {
         doSendEvent(event);
     }
 
     /**
      * Sets username to be used with event posts to the Kahuna sdk.
      * @param username Username.
      */
     public void setUsername(String username)
     {
         mUsername = username;
     }
 
     /**
      * Sets user email to be used with event posts to the Kahuna sdk.
      * @param userEmail User email.
      */
     public void setUserEmail(String userEmail)
     {
         mUserEmail = userEmail;
     }
 
     /**
      * Sets user first name to be used with event posts to the Kahuna sdk.
      * @param firstName User first name.
      */
     public void setFirstName(String firstName)
     {
         mFirstName = firstName;
     }
 
     /**
      * Sets user last name to be used with event posts to the Kahuna sdk.
      * @param lastName User last name.
      */
     public void setLastName(String lastName)
     {
         mLastName = lastName;
     }
 
     /**
      * Sets app name to be used with event posts to the Kahuna sdk.
      * @param appName Application name.
      */
     public void setAppName(String appName)
     {
         mAppName = appName;
     }
 
     /**
      * Sets application version to be used with event posts to the Kahuna sdk.
      * @param appVersion Application version.
      */
     public void setAppVersion(String appVersion)
     {
         mAppVersion = appVersion;
     }
 
     /**
      * Sets OS name to be used with event posts to the Kahuna sdk.
      * @param osName OS name.
      */
     public void setOsName(String osName)
     {
         mOsName = osName;
     }
 
     /**
      * Sets OS version to be used with event posts to the Kahuna sdk.
      * @param osVersion OS version.
      */
     public void setOsVersion(String osVersion)
     {
         mOsVersion = osVersion;
     }
 
     /**
      * Sets device model name to be used with event posts to the Kahuna sdk.
      * @param deviceModelName Device model name.
      */
     public void setDeviceModelName(String deviceModelName)
     {
         mDeviceModelName = deviceModelName;
     }
 
     /**
      * Default constructor (hidden).
      */
     private KahunaSdk()
     {
         mHttpClient = new AsyncHttpClient();
     }
 
     /**
      * Actually forms and sends POST request to Kahuna backend.
      * @param event Event to be sent to the backend.
      */
     private void doSendEvent(String event)
     {
         if (!mIsInitialized)
         {
             throw new IllegalStateException("Kahuna SDK should be initialized with API key " +
                     "and device id before sending events!");
         }
 
         RequestParams params = new RequestParams();
         params.put(API_KEY_FIELD, mApiKey);
         params.put(DEVICE_ID_FIELD, mDeviceId);
         params.put(USERNAME_FIELD, mUsername);
         params.put(USER_EMAIL_FIELD, mUserEmail);
         params.put(FIRST_NAME_FIELD, mFirstName);
         params.put(LAST_NAME_FIELD, mLastName);
         params.put(APP_NAME_FIELD, mAppName);
         params.put(APP_VERSION_FIELD, mAppVersion);
         params.put(OS_NAME_FIELD, mOsName);
         params.put(OS_VERSION_FIELD, mOsVersion);
         params.put(DEVICE_MODEL_NAME_FIELD, mDeviceModelName);
         params.put(EVENT_FIELD, event);
 
         mHttpClient.post(KAHUNA_ENDPOINT, params, new AsyncHttpResponseHandler() {
 
             @Override
             public void onFailure(Throwable throwable, String s)
             {
                 super.onFailure(throwable, s);
                 Log.d(TAG, "Failed to send request: " + s, throwable);
             }
 
             @Override
             public void onSuccess(String s)
             {
                 // IMPORTANT NOTE: onSuccess called when the post is successful, which doesn't
                 // always mean the server processed the post successfully, e.g. sending an empty
                 // API key results in the following message:
                 // {"error_message": "A Server Error Has Occurred", "success": false}
                 Log.d(TAG, "Successfully sent request: " + s);
             }
         });
     }
 
     /**
      * Retrieves a unique device installation id.
      * @param context Context instance used to retrieve data from the OS.
      * @return A string with unique device ID.
      */
     private String getDeviceId(Context context)
     {
         // See http://android-developers.blogspot.com/2011/03/identifying-app-installations.html
         String result = Settings.Secure.getString(context.getContentResolver(),
                 Settings.Secure.ANDROID_ID);
         if (result == null || result.equals(""))
         {
             result = Build.SERIAL;
         }
 
         return result;
     }
 
 }
