 /*
  * Copyright 2012 Matthew Precious
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.mattprecious.smsfix.library;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 import com.mattprecious.smsfix.library.util.SmsMmsDbHelper;
 
 /**
  * Service to fix all incoming text messages
  * 
  * How it works: The service listens to the SMS database for any changes. Once notified, the service
  * loops through the database in descending order and checks each message ID against the last known
  * ID. If the ID is greater, then we can assume it is a new message and alter its time stamp. Once
  * we reach a message that is not new, stop the loop.
  * 
  * Foreground service code taken from the Android API Demos: ForegroundService.java
  * 
  * @author Matthew Precious
  * 
  */
 public class FixService extends Service {
     private final static String TAG = "FixService";
 
     private SharedPreferences settings;
 
     // The SMS and MMS URIs do not notify when a thread is deleted, so instead we use the MMS/SMS
     // URI for observing.
     // This provider, however, does not play nice when looking for and editing the existing
     // messages. So, we use the original SMS and MMS URIs for our editing
     private final Uri SMS_URI = SmsMmsDbHelper.getSmsUri();
     private final Uri MMS_URI = SmsMmsDbHelper.getMmsUri();
     private final Uri MMS_SMS_URI = SmsMmsDbHelper.getMmsSmsUri();
 
     private Cursor smsCursor;
     private Cursor mmsCursor;
     private Cursor mmsSmsCursor;
 
     private FixServiceObserver smsObserver;
     private FixServiceObserver mmsObserver;
     private FixServiceObserver mmsSmsObserver;
 
     private TelephonyManager telephonyManager;
 
     // notification variables
     private static NotificationManager notificationManager;
     private static Notification notification;
 
     private static boolean running = false;
 
     private long lastSmsId = -1; // the ID of the last sms message we've altered
     private long lastMmsId = -1; // the ID of the last mms message we've altered
 
     private static final Class<?>[] setForegroundSignature = new Class[] { boolean.class };
     private static final Class<?>[] startForegroundSignature = new Class[] { int.class,
             Notification.class };
     private static final Class<?>[] stopForegroundSignature = new Class[] { boolean.class };
 
     private Method setForeground;
     private Method startForeground;
     private Method stopForeground;
     private Object[] setForegroundArgs = new Object[1];
     private Object[] startForegroundArgs = new Object[2];
     private Object[] stopForegroundArgs = new Object[1];
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
 
         Log.d(TAG, "FixService starting");
 
         running = true;
 
         settings = PreferenceManager.getDefaultSharedPreferences(this);
 
         telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 
         // set up everything we need for the running notification
         notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 
         int icon = R.drawable.icon;
         if (settings.getString("notify_icon", "grey").equals("grey")) {
             icon = R.drawable.icon_bw;
         }
 
         NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
         notificationBuilder.setSmallIcon(icon);
         notificationBuilder.setOngoing(true);
         notificationBuilder.setPriority(Notification.PRIORITY_MIN);
         notificationBuilder.setContentTitle(getString(R.string.app_name));
         notificationBuilder.setContentText(getString(R.string.notify_message));
 
         PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                 SMSFix.class), 0);
         notificationBuilder.setContentIntent(contentIntent);
 
         notification = notificationBuilder.build();
 
         // shit's broken... throw my own exception so I don't have to read stack traces
         if (SMS_URI == null) {
             Log.e(TAG, "SMS URI is null");
             throw new RuntimeException("SMS URI is null");
         }
 
         // MMS URI is null, but this isn't vital... log it.
         if (MMS_URI == null) {
             Log.w(TAG, "MMS URI is null");
         } else {
             lastMmsId = SmsMmsDbHelper.getLastMessageId(this, MMS_URI);
             Log.d(TAG, "lastMmsId initialized to " + lastMmsId);
         }
         
         // get the current last message ID
         lastSmsId = SmsMmsDbHelper.getLastMessageId(this, SMS_URI);
         Log.d(TAG, "lastSmsId initialized to " + lastSmsId);
 
         try {
             mmsSmsObserver = new FixServiceObserver(FixServiceObserver.TYPE_MMS_SMS);
             mmsSmsCursor = SmsMmsDbHelper.getInboxCursor(this, MMS_SMS_URI, null, null);
             mmsSmsCursor.registerContentObserver(mmsSmsObserver);
         } catch (NullPointerException e) {
             smsObserver = new FixServiceObserver(FixServiceObserver.TYPE_SMS);
             smsCursor = SmsMmsDbHelper.getInboxCursor(this, SMS_URI, null, null);
             smsCursor.registerContentObserver(smsObserver);
 
             if (MMS_URI != null && settings.getBoolean("mms", true)) {
                 mmsObserver = new FixServiceObserver(FixServiceObserver.TYPE_MMS);
                 mmsCursor = SmsMmsDbHelper.getInboxCursor(this, MMS_URI, null, null);
                 mmsCursor.registerContentObserver(mmsObserver);
             }
         }
 
         setupForegroundVars();
 
         Log.d(TAG, "FixService initialization complete. Now monitoring SMS messages");
     }
 
     @Override
     public void onDestroy() {
         // Make sure our notification is gone.
         stopForegroundCompat(R.string.notify_message);
 
         if (smsCursor != null) {
             smsCursor.close();
         }
 
         if (mmsCursor != null) {
             mmsCursor.close();
         }
 
         if (mmsSmsCursor != null) {
             mmsSmsCursor.close();
         }
 
         running = false;
         Log.d(TAG, "FixService destroy");
 
         super.onDestroy();
     }
 
     // This is the old onStart method that will be called on the pre-2.0
     // platform. On 2.0 or later we override onStartCommand() so this
     // method will not be called.
     @Override
     public void onStart(Intent intent, int startId) {
         if (settings.getBoolean("notify", true)) {
             startNotify();
         }
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         if (settings.getBoolean("notify", true)) {
             startNotify();
         }
 
         // We want this service to continue running until it is explicitly
         // stopped, so return sticky.
         return START_STICKY;
     }
 
     private void setupForegroundVars() {
         try {
             startForeground = getClass().getMethod("startForeground", startForegroundSignature);
             stopForeground = getClass().getMethod("stopForeground", stopForegroundSignature);
             return;
         } catch (NoSuchMethodException e) {
             // Running on an older platform.
             startForeground = stopForeground = null;
         }
 
         try {
             setForeground = getClass().getMethod("setForeground", setForegroundSignature);
         } catch (NoSuchMethodException e) {
             throw new IllegalStateException(
                     "OS doesn't have Service.startForeground OR Service.setForeground!");
         }
     }
 
     public void startNotify() {
         startForegroundCompat(R.string.notify_message, notification);
     }
 
     /**
      * This is a wrapper around the new startForeground method, using the older APIs if it is not
      * available.
      */
     void startForegroundCompat(int id, Notification notification) {
         // If we have the new startForeground API, then use it.
         if (startForeground != null) {
             startForegroundArgs[0] = Integer.valueOf(id);
             startForegroundArgs[1] = notification;
             invokeMethod(startForeground, startForegroundArgs);
             return;
         }
 
         // Fall back on the old API.
         setForegroundArgs[0] = Boolean.TRUE;
         invokeMethod(setForeground, setForegroundArgs);
         notificationManager.notify(id, notification);
     }
 
     /**
      * This is a wrapper around the new stopForeground method, using the older APIs if it is not
      * available.
      */
     void stopForegroundCompat(int id) {
         // If we have the new stopForeground API, then use it.
         if (stopForeground != null) {
             stopForegroundArgs[0] = Boolean.TRUE;
             try {
                 stopForeground.invoke(this, stopForegroundArgs);
             } catch (InvocationTargetException e) {
                 // Should not happen.
                 Log.w(TAG, "Unable to invoke stopForeground", e);
             } catch (IllegalAccessException e) {
                 // Should not happen.
                 Log.w(TAG, "Unable to invoke stopForeground", e);
             }
             return;
         }
 
         // Fall back on the old API. Note to cancel BEFORE changing the
         // foreground state, since we could be killed at that point.
         notificationManager.cancel(id);
         setForegroundArgs[0] = Boolean.FALSE;
         invokeMethod(setForeground, setForegroundArgs);
     }
 
     void invokeMethod(Method method, Object[] args) {
         try {
             method.invoke(this, args);
         } catch (InvocationTargetException e) {
             // Should not happen.
             Log.w(TAG, "Unable to invoke method", e);
         } catch (IllegalAccessException e) {
             // Should not happen.
             Log.w(TAG, "Unable to invoke method", e);
         }
     }
 
     /**
      * Returns true if the service is running
      * 
      * @return boolean
      */
     public static boolean isRunning() {
         return running;
     }
 
     /**
      * Checks if the roaming condition is met. Returns true if the user doesn't care about roaming,
      * or if the user does but isn't currently roaming
      * 
      * @return boolean
      */
     private boolean roamingConditionMet() {
         boolean onlyRoaming = settings.getBoolean("roaming", false);
         boolean isRoaming = telephonyManager.isNetworkRoaming();
 
         Log.d(TAG, "onlyRoaming: " + Boolean.toString(onlyRoaming));
         Log.d(TAG, "isRoaming: " + Boolean.toString(isRoaming));
 
         boolean roamingCondition = !onlyRoaming || isRoaming;
 
         return roamingCondition;
     }
 
     /**
      * ContentObserver to handle updates to the SMS database
      * 
      * @author Matthew Precious
      * 
      */
     private class FixServiceObserver extends ContentObserver {
         private final static int TYPE_SMS = 0;
         private final static int TYPE_MMS = 1;
         private final static int TYPE_MMS_SMS = 2;
 
         private final int type;
 
         public FixServiceObserver(int type) {
             super(null);
 
             switch (type) {
                 case TYPE_SMS:
                 case TYPE_MMS:
                 case TYPE_MMS_SMS:
                     break;
                 default:
                     throw new IllegalArgumentException();
             }
 
             this.type = type;
         }
 
         @Override
         public synchronized void onChange(boolean selfChange) {
             super.onChange(selfChange);
 
             String typeStr;
             switch (type) {
                 case TYPE_SMS:
                     typeStr = "SMS";
                     break;
                 case TYPE_MMS:
                     typeStr = "MMS";
                     break;
                 case TYPE_MMS_SMS:
                     typeStr = "MMS/SMS";
                     break;
                 default:
                     typeStr = "";
                     break;
             }
 
             Log.d(TAG, typeStr + " database altered, checking...");
 
             boolean roamingConditionMet = roamingConditionMet();
             boolean adjustMms = settings.getBoolean("mms", true);
 
             Log.d(TAG, "selfChange: " + Boolean.toString(selfChange));
             Log.d(TAG, "roamingConditionMet: " + Boolean.toString(roamingConditionMet));
 
             // TODO: make the selfChange boolean actually work...
             if (!selfChange && roamingConditionMet) {
                 Log.d(TAG, "Adjusting the message(s)");
 
                 // fix them
                 if (type == TYPE_SMS || type == TYPE_MMS_SMS) {
                     lastSmsId = SmsMmsDbHelper.fixMessages(getApplicationContext(), SMS_URI,
                             lastSmsId);
                 }
 
                if (adjustMms && type == TYPE_MMS || type == TYPE_MMS_SMS) {
                     lastMmsId = SmsMmsDbHelper.fixMessages(getApplicationContext(), MMS_URI,
                             lastMmsId);
                 }
             }
         }
     }
 
 }
