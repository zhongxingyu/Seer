 /**
  * Parent Management
  *
  * Created by Gloria
  */
 
 package com.parent.management;
 
 import java.io.File;
 import java.util.HashMap;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
 import android.os.Environment;
 import android.os.SystemClock;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 import com.parent.management.monitor.Monitor;
 import com.parent.management.monitor.Monitor.Type;
 import com.parent.management.receiver.AppUsedMonitorReceiver;
 import com.parent.management.receiver.ManagementReceiver;
 
 public class ManagementApplication extends android.app.Application {
 	
 	/**
      * Log tag for this application.
      */
     protected static String mApplicationTag = "PM";
     private static String mInternalPath = null;
     
     public static final boolean DEBUG = true;
     
     public static HashMap<Type, Monitor> commonMonitorList = null;
     public static HashMap<Type, Monitor> specialMonitorList = null;
     
     public static String MANAGEMENT_RECEIVER_FILTER_ACTIONS_COMMON = "common_upload";
     public static String MANAGEMENT_RECEIVER_FILTER_ACTIONS_SPECIAL = "special_upload";
 	
     /**
      * The application context
      */
     protected static Context mContext = null;
     /**
      * Application configuration
      */
     private static ManagementConfiguration mConfiguration = null;
     
     private static PendingIntent mCommonPendingIntent = null;
     private static PendingIntent mSpecialPendingIntent = null;
     
     /**
      * @return the application tag (used in application logs)
      */
     public static String getApplicationTag() {
         return mApplicationTag;
     }
     
     /**
      * Gets the configuration
      * @return the current configuration
      */
     public static ManagementConfiguration getConfiguration() {
         return mConfiguration;
     }
     
     /**
      * Gets the PendingIntent
      * @return the current PendingIntent
      */
     public static PendingIntent getPendingIntent(String action) {
         if (action.equals(MANAGEMENT_RECEIVER_FILTER_ACTIONS_COMMON)) {
             return mCommonPendingIntent;
         } else {
             return mSpecialPendingIntent;
         }
     }
     
     public static Context getContext() {
         return mContext;
     }
     
     @Override
     public void onCreate() {
         super.onCreate();
         
         mContext = this;
         
         // Clean installed apk file automatically
         final File apk = new File(Environment.getExternalStorageDirectory() + "/Download/management.apk");
         if (apk.exists()) {
             // Found update apk in storage, delete it
             Log.i(mApplicationTag, "Cleaning existing update file " 
                 + apk.getAbsolutePath());
             apk.delete();
         } 
         
         // Configuration
         mConfiguration = new ManagementConfiguration(mContext);
         mInternalPath = mContext.getDir(".management",Context.MODE_PRIVATE).getAbsolutePath();
 
         Intent commonIntent = new Intent(mContext, ManagementReceiver.class);
         commonIntent.setAction(MANAGEMENT_RECEIVER_FILTER_ACTIONS_COMMON);
         mCommonPendingIntent = PendingIntent.getBroadcast(
                 mContext, 0, commonIntent, PendingIntent.FLAG_CANCEL_CURRENT);
         
         Intent specialIntent = new Intent(mContext, ManagementReceiver.class);
         specialIntent.setAction(MANAGEMENT_RECEIVER_FILTER_ACTIONS_SPECIAL);
         mSpecialPendingIntent = PendingIntent.getBroadcast(
                 mContext, 0, specialIntent, PendingIntent.FLAG_CANCEL_CURRENT);
         
         mConfiguration.registerPreferenceChangeListener(this.mSettingsListener);
     }
     
     /**
      * Gets the internal storage path
      * @return the internal storage path
      */
     public static String getInternalPath() {
         return mInternalPath;
     }
     
     /**
      * Gets the IMEI
      */
     public static String getIMEI() {
         TelephonyManager tm = (TelephonyManager)mContext.getSystemService(TELEPHONY_SERVICE);
         return tm.getDeviceId();
     }
     
     /**
     * Gets the mac when there's no avaliable imei
     */
    public static String getMAC() {
        WifiManager wifi = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);  
        WifiInfo info = wifi.getConnectionInfo();  
        return info.getMacAddress();
    }
    
    /**
      * Gets the IMEI
      */
     public static String getIMSI() {
         TelephonyManager tm = (TelephonyManager)mContext.getSystemService(TELEPHONY_SERVICE);
         return tm.getSubscriberId();
     }
     
     public static void setAppUsedMonitorAlarm() {
         AlarmManager mAlarmManager = (AlarmManager)ManagementApplication.getContext().
                 getSystemService("alarm");
         
         PendingIntent mPendingIntent = PendingIntent.getBroadcast(
                 mContext, 0, new Intent(mContext, AppUsedMonitorReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
         
         mAlarmManager.setRepeating(
                 AlarmManager.ELAPSED_REALTIME_WAKEUP, 
                 5000L + SystemClock.elapsedRealtime(), 
                 1000,
                 mPendingIntent);
     }
     
     private final OnSharedPreferenceChangeListener mSettingsListener = 
             new OnSharedPreferenceChangeListener() {
                 /* (non-Javadoc)
                  * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
                  */
                 @Override
                 public void onSharedPreferenceChanged(
                         final SharedPreferences sharedPreferences, final String key) {
                     ManagementApplication.getConfiguration();
                     if (key.equals(ManagementConfiguration.PREFERENCE_KEY_COMMON_INTERVAL_TIME)) {
                         Log.d(mApplicationTag, "----> common interval time changed.");
                         AlarmManager mAlarmManager = (AlarmManager)ManagementApplication.getContext().
                                 getSystemService("alarm");
                         mAlarmManager.cancel(ManagementApplication.getPendingIntent(
                                 ManagementApplication.MANAGEMENT_RECEIVER_FILTER_ACTIONS_COMMON));
                         mAlarmManager.setRepeating(
                                 AlarmManager.ELAPSED_REALTIME_WAKEUP, 
                                 ManagementApplication.getConfiguration().getCommonIntervalTime() + 
                                         SystemClock.elapsedRealtime(), 
                                 ManagementApplication.getConfiguration().getCommonIntervalTime(),
                                 ManagementApplication.getPendingIntent(
                                         ManagementApplication.MANAGEMENT_RECEIVER_FILTER_ACTIONS_COMMON));
                     }
                     if (key.equals(ManagementConfiguration.PREFERENCE_KEY_SPECIAL_INTERVAL_TIME)) {
                         Log.d(mApplicationTag, "----> special interval time changed.");
                         AlarmManager mAlarmManager = (AlarmManager)ManagementApplication.getContext().
                                 getSystemService("alarm");
                         mAlarmManager.cancel(ManagementApplication.getPendingIntent(
                                 ManagementApplication.MANAGEMENT_RECEIVER_FILTER_ACTIONS_SPECIAL));
                         mAlarmManager.setRepeating(
                                 AlarmManager.ELAPSED_REALTIME_WAKEUP, 
                                 ManagementApplication.getConfiguration().getSpecialIntervalTime() + 
                                         SystemClock.elapsedRealtime(), 
                                 ManagementApplication.getConfiguration().getSpecialIntervalTime(),
                                 ManagementApplication.getPendingIntent(
                                         ManagementApplication.MANAGEMENT_RECEIVER_FILTER_ACTIONS_SPECIAL));
                     }
                 }
             };
 
 }
