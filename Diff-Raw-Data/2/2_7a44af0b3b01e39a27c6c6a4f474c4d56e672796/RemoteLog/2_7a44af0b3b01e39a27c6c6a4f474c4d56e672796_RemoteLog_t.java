 package com.scurab.android.rlw;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Application;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.os.Handler;
 import android.text.Html;
 import android.text.TextUtils;
 import android.widget.Toast;
 import com.google.android.gcm.GCMRegistrar;
 import com.google.gson.*;
 import com.scurab.android.KillAppException;
 import com.scurab.android.KnowsActiveActivity;
 import com.scurab.android.settings.Update;
 import com.scurab.gwt.rlw.shared.model.*;
 
 import javax.net.ssl.TrustManager;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.net.MalformedURLException;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Stack;
 
 /**
  * Base RemoteLog class<br/>
  * For every usage you have to call at least
  * {@link #init(Application, String, String, AsyncCallback)} to create first
  * registration on RemoteLog Server.<br/>
  * <br/>
  * There are few <b>optional pre-init methods</b> to set some variables before
  * initialization<br/>
  * {@link #registerForPushNotifications(String)} <br/>
  * {@link #resendRegistration()}<br/>
  * {@link #setDeviceDataProvider(DeviceDataProvider)}<br/>
  * {@link #setLogMode(int)}<br/>
  * {@link #setGson(Gson)}<br/>
  * {@link #setCredentials(String, String)}<br/>
  * {@link #setTrustManager(TrustManager)}<br/>
  *
  * @author Jiri Bruchanov
  */
 public final class RemoteLog {
 
     public static final String SETTINGS_R_LOG = "RLog";
     public static final String SETTINGS_SHOW_UPDATE_TOAST = "ShowUpdateToast";
     public static final String SETTINGS_LATEST_BUILD = "LatestBuild";
     //    private static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS");
     private static Gson sGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
 
     public static String UNCOUGHT_ERROR = "UNCOUGHT_ERROR";
     public static String PUSH_TOKEN = "PUSH_TOKEN";
 
     /**
      * Registration thread *
      */
     private static Thread sRegDeviceThread = null;
 
     /* RLog neccessary vars */
     private String mAppVersion;
     private String mAppBuild;
     private String mAppName;
     private int mDeviceID;
 
     /* Working stuff */
     private ServiceConnector mConnector;
     private SharedPreferences mPreferences;
     private SettingsResponse mSettings;
     private Application mApplication;
 
     private String mServerLocation;
 
     private static LogSender sLogSender;
 
     /* Settings constants */
     private static final String DEVICE_ID = "DEVICE_ID";
     public static final String PUSH = "PUSH";
 
     private static final RemoteLog sSelf = new RemoteLog();
 
     private RemoteLog() {
     }
 
     /**
      * @return reference only if RemoteLog was initialized by init method
      */
     public static RemoteLog getInstance() {
         return sLogSender != null ? sSelf : null;
     }
 
     /**
      * To override default Gson instance, i.g. different datetime format<br/>
      * For different formats don't forget to change to same value on server side!
      * <br/>
      * Don't call it after {@link #init(Context, String, String, AsyncCallback)}
      *
      * @param gson
      */
     public static void setGson(Gson gson) {
         if (RemoteLog.sLogSender != null) {
             throw new IllegalStateException("You can't set new Gson reference after initialization!");
         }
         sGson = gson;
     }
 
     private static boolean sResend = false;
 
     /**
      * Resend registration to RLogServer if we are already registered already
      * have ID from server
      */
     public static void resendRegistration() {
         sResend = true;
     }
 
     private static int sLogMode = RLog.EXCEPTION;
 
     /**
      * Set initial log mode to catch any event during registration<br/>
      * By default is {@link RLog#EXCEPTION}
      *
      * @param logMode
      */
     public static void setLogMode(int logMode) {
         sLogMode = logMode;
     }
 
     private static String sPushProjectId = null;
 
     /**
      * Set a project ID for registration to Google Cloud Messages service for
      * activating push notifications<br/>
      * <p/>
      * You can check GCM registration by
      * {@link GCMRegistrar#isRegisteredOnServer(Context)}
      *
      * @param projectId
      */
     public static void registerForPushNotifications(String projectId) {
         sPushProjectId = projectId;
     }
 
     private static TrustManager mTrustManager = null;
 
     /**
      * Set any of yours trust manager to handle SSL certificates<br/>
      * If you have self-signes certificate on your server, just use {@link FakeTrustManager} for it
      *
      * @param manager
      */
     public static void setTrustManager(TrustManager manager) {
         mTrustManager = manager;
     }
 
     private static String mOwner = null;
 
     /**
      * Set/Override owner detectable from device
      * @param value
      */
     public static void setOwner(String value){
         mOwner = value;
     }
 
     private static String mUsername;
     private static String mPassword;
 
     /**
      * Set website credentials
      *
      * @param username
      * @param password
      * @throws IllegalArgumentException if username or password is null
      */
     public static void setCredentials(String username, String password) {
         if (username == null || password == null) {
             throw new IllegalArgumentException("Null username or password");
         }
 
         mUsername = username;
         mPassword = password;
     }
 
     private static DeviceDataProvider mDeviceDataProvider;
 
     public static void setDeviceDataProvider(DeviceDataProvider provider) {
         mDeviceDataProvider = provider;
     }
 
     /**
      * Register RemoteLog for usage
      *
      * @param c
      * @param appName        - your appName
      * @param serverLocation - location of server side
      * @param finishListener - optional listener called when registration finished
      * @throws NameNotFoundException
      * @throws MalformedURLException
      * @throws NoSuchAlgorithmException
      * @throws KeyManagementException
      */
     public static void init(final Application c, String appName,
                             String serverLocation, final AsyncCallback<RemoteLog> finishListener)
             throws NameNotFoundException, MalformedURLException, KeyManagementException, NoSuchAlgorithmException {
 
         if (c == null) {
             throw new IllegalArgumentException("Context is null!");
         }
         if (TextUtils.isEmpty(appName)) {
             throw new IllegalArgumentException("appName must be set");
         }
         if (TextUtils.isEmpty(serverLocation)) {
             throw new IllegalArgumentException("serverLocation must be set");
         }
 
         if (sRegDeviceThread != null) {
             throw new IllegalStateException(
                     "Already running registration process!");
         }
 
         // set log mode
         RLog.setMode(sLogMode);
 
 
         sSelf.mApplication = c;
 
         // set appname
         sSelf.mAppName = appName;
 
         sSelf.mServerLocation = serverLocation;
         // create server connector
         sSelf.mConnector = new ServiceConnector(sSelf.mServerLocation, mTrustManager, mUsername, mPassword);
 
         // init preferences
         sSelf.mPreferences = c.getSharedPreferences(
                 RemoteLog.class.getSimpleName(), Context.MODE_PRIVATE);
 
         // if devId == 0 not registered yet
         sSelf.mDeviceID = sSelf.mPreferences.getInt(DEVICE_ID, 0);
 
         // keep somewhere, that we need send pushToken to our server,
         // if there will be any fail
         if (sPushProjectId != null) {
             sSelf.mPreferences.edit().putString(PUSH, sPushProjectId).commit();
         }
 
         PackageInfo pInfo = c.getPackageManager().getPackageInfo(
                 c.getPackageName(), 0);
 
         // init base log info
         sSelf.mAppVersion = pInfo.versionName;
         sSelf.mAppBuild = String.valueOf(pInfo.versionCode);
         // send registration to server
         sRegDeviceThread = new Thread(new Runnable() {
             @Override
             public void run() {
                 try {
                     registerImpl(c, finishListener);
                     //remove it if it's unnecessary for future
                     sSelf.mApplication = null;
                 } catch (Exception e) {
                     if (sSelf.mDeviceID != 0) {
                         RLog.e(sSelf, e);
                         e.printStackTrace();
                     }
                 }
                 // set reg thread to null to tell we are done
                 sRegDeviceThread = null;
                 // notify listener we are done
                 if (finishListener != null) {
                     finishListener.call(sSelf);
                 }
             }
         }, "RemoteLogRegistrationThread");
 
         catchUncaughtErrors(sRegDeviceThread);
         sRegDeviceThread.start();
     }
 
     /**
      * Register implementation Must be started in new thread
      *
      * @param finishListener
      * @throws IOException
      */
     private static void registerImpl(Context c,
                                      AsyncCallback<RemoteLog> finishListener) throws IOException {
 
         final int devId = sSelf.mDeviceID;
         Device device = null;
         // REGISTRATION TO RLW SERVER
         // (re)send registration to RemoteLogWeb server
         if (sSelf.mDeviceID == 0 || sResend) {
             device = sSelf.sendDeviceToServer(c);
             if (device != null) {
                 sSelf.mDeviceID = device.getDeviceID();
             }
         }
 
         //fails only if we didn't receive any ID ever => no fail when resend failed
         if (sSelf.mDeviceID == 0 && devId == 0) {
             // only if request to server was sucesfull, but respond doesn't have
             // an ID
             sSelf.mConnector = null;
             throw new IllegalStateException("Unable to register device");
         }
 
         // create log sender here => so we can log after sucessful registration
         sLogSender = new LogSender(sSelf.mConnector);
 
         // GCM REGISTRAION
         String projectId = sSelf.mPreferences.getString(PUSH, null);
         // if there is pending GCM registration and we have a device
         // ID from server aswell
         boolean gcmRegistered = GCMRegistrar.isRegisteredOnServer(c) && !TextUtils.isEmpty(GCMRegistrar.getRegistrationId(c));
         if (projectId != null) {
             if (!gcmRegistered) {// if we dont have push
                 // registration
                 GCMRegistrar.register(c, projectId);// register at
                 // google GCM
                 // service
             } else {
                 String token = GCMRegistrar.getRegistrationId(c);
                 String savedToken = sSelf.mPreferences.getString(PUSH_TOKEN, "");
                 //sending everytime, possible improvement to send only when is changed
                 // if we are registered, we need to send it to our
                 // server as well
                 if (!savedToken.equals(token) || (device != null && !token.equals(device.getPushID()))) {
                     sSelf.updatePushToken(GCMRegistrar.getRegistrationId(c));
                 }
             }
         } else {
             // maybe someone registered push notifiaction before
             sSelf.updatePushToken(GCMRegistrar.getRegistrationId(c));
         }
 
         // SETTINGS
         sSelf.mSettings = sSelf.mConnector.loadSettings(sSelf.mDeviceID,
                 sSelf.mAppName);
         sSelf.onSettings(sSelf.mSettings, c);
 
         //UNCAUGHT Exception
         sSelf.checkSavedUncoughtException();
     }
 
     private void checkSavedUncoughtException() {
         String ueStack = mPreferences.getString(UNCOUGHT_ERROR, "");
         if (!TextUtils.isEmpty(ueStack)) {
             LogItemBlobRequest libr = new LogItemBlobRequest(
                     LogItemBlobRequest.MIME_TEXT_PLAIN,
                     "fatalerror.txt", ueStack.getBytes());
             libr.setIsUncoughtError(true);
             RLog.send(this, "UncaughtException", "History stack trace", libr);
         }
     }
 
     /**
      * Load settings from server<br/>
      * It's blocking => call it from nonMainThread
      *
      * @param callback
      * @throws IllegalStateException
      */
     public void loadSettings(AsyncCallback<SettingsResponse> callback)
             throws IllegalStateException {
         if (mDeviceID == 0) {
             throw new IllegalStateException(
                     "Device is not registered on server!");
         }
         if (mAppName == null) {
             throw new IllegalStateException("Not initialized!");
         }
         try {
             mSettings = mConnector.loadSettings(mDeviceID, mAppName);
             if (mSettings != null) {
                 onSettings(mSettings, null);
                 if (callback != null) {
                     callback.call(getSettings());
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     protected void onSettings(SettingsResponse resp, final Context c) {
         try {
             if (resp.getCount() > 0) {
                 boolean rlog = false;
                 Settings[] ss = resp.getContext();
                 // going from end, where should be device specific
                 for (int i = ss.length - 1; i >= 0; i--) {
                     String json = ss[i].getJsonValue();
                     if(TextUtils.isEmpty(json)){
                         continue;
                     }
 
                     JsonObject je = new JsonParser().parse(json).getAsJsonObject();
                     if(je.has(SETTINGS_R_LOG)){
                         //settings use only first found
                         if (!rlog) {
                             onSettingsRLog(c, je);
                             rlog = true;
                         }
                     }else if (je.has(Update.class.getSimpleName())){
                         onSettingsUpdate(c, je);
                     }
                 }
             }
 
         } catch (Exception e) {
             RLog.e(this, e);
             // ignore any error and let the code continue
             e.printStackTrace();
         }
     }
 
     private void onSettingsRLog(final Context c, JsonObject je){
         String logMode = String.valueOf(je.get(SETTINGS_R_LOG).getAsString());
         int parsed = RLog.getMode(logMode);
         if (parsed != -1) {
             RLog.setMode(parsed);
         }
     }
 
     private void onSettingsUpdate(final Context c, JsonObject je) {
         if(c == null){
             //context is necessary for every journey here
             return;
         }
         //show update notification
         final Update update = new Gson().fromJson(je.get(Update.class.getSimpleName()), Update.class);
         if (!(TextUtils.isEmpty(update.build) && TextUtils.isEmpty(update.type))) {
             //get builds
             int latestbuild = Integer.parseInt(update.build);
             int appbuild = Integer.parseInt(mAppBuild);
             final CharSequence msg = TextUtils.isEmpty(update.message) ? "Update available on google play!" : Html.fromHtml(update.message);
             //compare it
             if (latestbuild > appbuild) {
                 final Handler mHandler = new Handler(c.getMainLooper());
                 //we have to run it in main thread
                 mHandler.post(new Runnable() {
                     @Override
                     public void run() {
                         try {
                             if (Update.TYPE_TOAST.equalsIgnoreCase(update.type)) {
                                 //show just a toast
                                 Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
                             } else if (Update.TYPE_DIALOG.equalsIgnoreCase(update.type)) {
                                 //show dialog
                                 Context o = c.getApplicationContext();
                                 //dialog must have activity context
                                 if (o instanceof KnowsActiveActivity) {
                                     Activity a = ((KnowsActiveActivity) o).getCurrentActivity();
                                     //if a is null it means app is not running now
                                     if (a != null) {
                                         AlertDialog.Builder b = new AlertDialog.Builder(a);
                                         b.setTitle("Update").setMessage(msg)
                                                 .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                                     @Override
                                                     public void onClick(DialogInterface dialog, int which) {
                                                         try{
                                                             Intent i = new Intent(Intent.ACTION_VIEW);
                                                             i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                             i.setData(Uri.parse("market://details?id=" + c.getApplicationContext().getPackageName()));
                                                             c.startActivity(i);
                                                         }catch(Exception e){
                                                             e.printStackTrace();
                                                             RLog.e(RemoteLog.this, e);
                                                         }
                                                     }
                                                 });
                                         b.create().show();
                                     } else {
                                         RLog.d(RemoteLog.this, "Dialog update, current activity is null => can't show it");
                                     }
                                 } else {
                                     RLog.d(RemoteLog.this, "Dialog update, Application is not KnowsActiveActivity => can't show it");
                                 }
                             }
                         } catch (Exception e) {
                             RLog.e(RemoteLog.this, e);
                         }
                     }
                 });
             }
         }
     }
 
     /**
      * Must bu runned in nonMainThread
      *
      * @param c
      * @throws IOException
      */
     private Device sendDeviceToServer(Context c) {
         Device result = null;
         try {
             // get device
             if (mDeviceDataProvider == null) {
                 mDeviceDataProvider = new DeviceDataProvider();
             }
             Device d = mDeviceDataProvider.getDevice(c);
             d.setApp(mAppName);
             d.setAppVersion(mAppVersion);
            if(!TextUtils.isEmpty(mOwner)){
                 d.setOwner(mOwner);
             }
             // save it
             int resultId = 0;
             DeviceResponse dr = mConnector.saveDevice(d);
             if (dr == null) {
                 RLog.e(this, "SaveDevice response is null");
             } else {
                 if (dr.hasError()) {
                     RLog.e(this, "SaveDevice has error:" + dr.getMessage());
                 } else {
                     result = dr.getContext();
                     resultId = result.getDeviceID();
                 }
             }
             // save id to shared preferences
             int savedId = mPreferences.getInt(DEVICE_ID, 0);
             if (savedId == 0 || resultId != savedId) {
                 Editor e = mPreferences.edit();
                 e.putInt(DEVICE_ID, resultId).commit();
             }
             return result;
         } catch (Exception e) {
             // catch anything here => if we already did registration so it's not
             // big problem about resending
             // for first installation exception is thrown little later
             e.printStackTrace();
         }
         return result;
     }
 
     /**
      * Active wait to finish registration process
      */
     public static void waitForDeviceRegistration() {
         waitForDeviceRegistration(Integer.MIN_VALUE);
     }
 
     /**
      * @param timeOut in milis
      */
     public static void waitForDeviceRegistration(int timeOut) {
         if (sRegDeviceThread != null) {
             try {
                 for (int i = 0, n = timeOut / 50; i < n; i++) {
                     Thread.sleep(50);
                 }
             } catch (InterruptedException e) {
                 e.printStackTrace();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     public boolean isDeviceRegistered() {
         return mPreferences.getInt(DEVICE_ID, 0) > 0;
     }
 
     public int getDeviceId() {
         return mDeviceID;
     }
 
     /**
      * Create new logItem prefilled by deviceId, appBuild, appName, appVersion
      *
      * @return null if RemoteLog is not properly initialized
      */
     public static LogItem createLogItem() {
         if (sLogSender == null) {
             return null;
         }
         LogItem li = new LogItem();
         li.setDeviceID(sSelf.mDeviceID);
         li.setAppBuild(sSelf.mAppBuild);
         li.setApplication(sSelf.mAppName);
         li.setAppVersion(sSelf.mAppVersion);
         li.setDate(new Date());
         return li;
     }
 
     protected ServiceConnector getConnector() {
         return mConnector;
     }
 
     /**
      * @param checkReg if true, registration is checked and if it's not valid it will try again, re-registration is async => even if it's restarted return will be null
      * @return object if device is registered, otherwise null
      */
     protected static LogSender getLogSender(boolean checkReg) {
         if (checkReg && sLogSender == null && sRegDeviceThread == null) {
             //- we gonna to try re-registration before it failed before
             //- here is potential problem with sSelf.mApplication, it should be null only if reg was sucesfull => this
             //  should never happened
             //- this is case for no sucessful registration at all
             try {
                 init(sSelf.mApplication, sSelf.mAppName, sSelf.mServerLocation, null);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return sLogSender;
     }
 
     /**
      * Set default handler for any thread
      */
     public static void catchUncaughtErrors() {
         Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(Thread.getDefaultUncaughtExceptionHandler()));
     }
     /**
      * Override default UncaughtExceptionHandler for particular thread
      *
      * @param t
      */
     public static void catchUncaughtErrors(Thread t) {
         t.setUncaughtExceptionHandler(new UncaughtExceptionHandler(t.getUncaughtExceptionHandler()));
     }
 
     private static void saveStack(Throwable t, String stack){
         String ce = sSelf.mPreferences.getString(UNCOUGHT_ERROR, "");
         String prefix = String.format("V:%s B:%s Date:%s", sSelf.mAppVersion, sSelf.mAppBuild, new Date().toGMTString());
         ce = String.format("%s \n%s\n\n%s", prefix, t.getMessage(), stack, ce);
         sSelf.mPreferences.edit().putString(UNCOUGHT_ERROR, ce).commit();
     }
 
     public static String getStackTrace(Throwable ex) {
         return getStackTrace(ex, null);
     }
 
     /**
      * Get more informative stacktrace
      *
      * @param ex
      * @param outT output param for reason
      * @return
      */
     public static String getStackTrace(Throwable ex, Throwable[] outT) {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
 
         Stack<Throwable> subStack = new Stack<Throwable>();
         Throwable t = ex;
 
         for (int i = 0; i < 5 && t != null; i++) {
             subStack.push(t);
             t = t.getCause();
         }
         if (outT != null && outT.length > 0) {
             outT[0] = subStack.peek();
         }
 
         for (int i = 0; i < subStack.size(); i++) {
             t = subStack.pop();
             t.printStackTrace(pw);
             pw.println();
         }
         return sw.toString();
     }
 
     /**
      * Get server settings
      *
      * @return
      */
     public SettingsResponse getSettings() {
         return mSettings;
     }
 
     /**
      * Update push token<br/>
      * Don't forget to run it in nonMainThread
      *
      * @param pushToken
      */
     public void updatePushToken(String pushToken) {
         if (mDeviceID == 0) {
             throw new IllegalStateException(
                     "Device is not registered on server!");
         }
         try {
             mConnector.updatePushToken(mDeviceID, pushToken);
             // set null to PUSH => we sucessfuly send pushToken to server
             mPreferences.edit().putString(PUSH, null).commit();
         } catch (Exception e) {
             RLog.e(this, e);
             e.printStackTrace();
         }
     }
 
     public static Gson getGson() {
         return sGson;
     }
 
     public void clearUncoughtException() {
         mPreferences.edit().putString(UNCOUGHT_ERROR, null).commit();
     }
 
     public static boolean isInitialized() {
         return sSelf.sLogSender != null;
     }
 
     private static class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
 
         private Thread.UncaughtExceptionHandler mOldHandler;
 
         private UncaughtExceptionHandler(Thread.UncaughtExceptionHandler oldHandler) {
             mOldHandler = oldHandler;
         }
 
         @Override
         public void uncaughtException(Thread thread, Throwable ex) {
             Throwable[] ts = new Throwable[1];
             String stack = getStackTrace(ex, ts);
 
             if (sLogSender == null) {
                 // not initialized => unable to send it
                 saveStack(ts[0], stack);
             } else if ((RLog.getMode() & RLog.ERROR) == RLog.ERROR) {
                 LogItemBlobRequest libr = new LogItemBlobRequest(
                         LogItemBlobRequest.MIME_TEXT_PLAIN,
                         "fatalerror.txt", stack.getBytes());
 
                 boolean isKillApp = (ex instanceof KillAppException);
                 if (!isKillApp) {
                     libr.setIsUncoughtError(true);
                     saveStack(ts[0], stack);
                 }
 
                 RLog.send(this,
                         isKillApp ? "KillApp"
                                 : "UncaughtException", ts[0].getMessage(),
                         libr);
 
                 sLogSender.waitForEmptyQueue();
             }
             mOldHandler.uncaughtException(thread, ex);
         }
     }
 }
