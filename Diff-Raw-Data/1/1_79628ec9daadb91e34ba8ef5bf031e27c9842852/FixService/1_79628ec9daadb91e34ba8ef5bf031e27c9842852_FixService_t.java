 /*
  * Copyright 2011 Matthew Precious
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.mattprecious.smsfix.library;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Date;
 import java.util.TimeZone;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 /**
  * Service to fix all incoming text messages
  * 
  * How it works:
  * The service listens to the SMS database for any changes. Once notified, the
  * service loops through the database in descending order and checks each
  * message ID against the last known ID. If the ID is greater, then we can
  * assume it is a new message and alter its time stamp. Once we reach a message
  * that is not new, stop the loop.
  * 
  * Foreground service code taken from the Android API Demos:
  * ForegroundService.java
  * 
  * @author Matthew Precious
  * 
  */
 public class FixService extends Service {
     private SharedPreferences settings;
 
     // The content://sms URI does not notify when a thread is deleted, so
     // instead we use the content://mms-sms/conversations URI for observing.
     // This provider, however, does not play nice when looking for and editing
     // the existing messages. So, we use the original content://sms URI for our
     // editing
     private Uri observingURI = Uri.parse("content://mms-sms/conversations");
     private Uri editingURI = Uri.parse("content://sms");
     private Cursor observingCursor;
     private Cursor editingCursor;
     private FixServiceObserver observer = new FixServiceObserver();
 
     private TelephonyManager telephonyManager;
 
     // notification variables
     private static NotificationManager nm;
     private static Notification notif;
 
     private static boolean running = false;
 
     public long lastSMSId = 0; // the ID of the last message we've
                                // altered
 
     private static final Class<?>[] setForegroundSignature = new Class[] { boolean.class };
     private static final Class<?>[] startForegroundSignature = new Class[] { int.class, Notification.class };
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
 
         running = true;
         
         settings = PreferenceManager.getDefaultSharedPreferences(this);
 
         telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 
         // set up everything we need for the running notification
         nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         
         int icon = R.drawable.icon;
         if (settings.getString("notify_icon", "grey").equals("grey")) {
             icon = R.drawable.icon_bw;
         }
         
         notif = new Notification(icon, null, 0);
        notif.flags |= Notification.FLAG_ONGOING_EVENT;
 
         PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, SMSFix.class), 0);
         notif.setLatestEventInfo(this, getString(R.string.app_name), getString(R.string.notify_message), contentIntent);
 
         // set up the query we'll be observing
         // we only need the ID and the date
         String[] columns = { "_id", "date" };
         observingCursor = getContentResolver().query(observingURI, columns, null, null, null);
         editingCursor = getContentResolver().query(editingURI, columns, "type=?", new String[] { "1" }, "_id DESC");
 
         // if the observingCursor is null, fall back and try getting a cursor
         // using the editingCursor
         if (observingCursor == null) {
             observingCursor = getContentResolver().query(editingURI, columns, null, null, null);
         }
 
         // register the observer
         observingCursor.registerContentObserver(observer);
 
         // get the current last message ID
         lastSMSId = getLastMessageId();
 
         setupForegroundVars();
 
         Log.i(getClass().getSimpleName(), "SMS messages now being monitored");
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
 
         // Make sure our notification is gone.
         stopForegroundCompat(R.string.notify_message);
 
         Log.i(getClass().getSimpleName(), "SMS messages are no longer being monitored. Good-bye.");
     }
 
     // This is the old onStart method that will be called on the pre-2.0
     // platform. On 2.0 or later we override onStartCommand() so this
     // method will not be called.
     @Override
     public void onStart(Intent intent, int startId) {
         if (settings.getBoolean("notify", false)) {
             startNotify();
         }
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         if (settings.getBoolean("notify", false)) {
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
             throw new IllegalStateException("OS doesn't have Service.startForeground OR Service.setForeground!");
         }
     }
 
     public void startNotify() {
         startForegroundCompat(R.string.notify_message, notif);
     }
 
     /**
      * This is a wrapper around the new startForeground method, using the older
      * APIs if it is not available.
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
         nm.notify(id, notification);
     }
 
     /**
      * This is a wrapper around the new stopForeground method, using the older
      * APIs if it is not available.
      */
     void stopForegroundCompat(int id) {
         // If we have the new stopForeground API, then use it.
         if (stopForeground != null) {
             stopForegroundArgs[0] = Boolean.TRUE;
             try {
                 stopForeground.invoke(this, stopForegroundArgs);
             } catch (InvocationTargetException e) {
                 // Should not happen.
                 Log.w("ApiDemos", "Unable to invoke stopForeground", e);
             } catch (IllegalAccessException e) {
                 // Should not happen.
                 Log.w("ApiDemos", "Unable to invoke stopForeground", e);
             }
             return;
         }
 
         // Fall back on the old API. Note to cancel BEFORE changing the
         // foreground state, since we could be killed at that point.
         nm.cancel(id);
         setForegroundArgs[0] = Boolean.FALSE;
         invokeMethod(setForeground, setForegroundArgs);
     }
 
     void invokeMethod(Method method, Object[] args) {
         try {
             method.invoke(this, args);
         } catch (InvocationTargetException e) {
             // Should not happen.
             Log.w(getClass().getSimpleName(), "Unable to invoke method", e);
         } catch (IllegalAccessException e) {
             // Should not happen.
             Log.w(getClass().getSimpleName(), "Unable to invoke method", e);
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
      * Returns the ID of the most recent message
      * 
      * @return long
      */
     private long getLastMessageId() {
         long ret = -1;
 
         // if there are any messages at our cursor
         if (editingCursor.getCount() > 0) {
             // get the first one
             editingCursor.moveToFirst();
 
             // grab its ID
             ret = editingCursor.getLong(editingCursor.getColumnIndexOrThrow("_id"));
         }
 
         return ret;
     }
 
     /**
      * Updates the time stamp on any messages that have come in
      */
     private void fixLastMessage() {
         // if there are any messages
         if (editingCursor.getCount() > 0) {
             // move to the first one
             editingCursor.moveToFirst();
 
             // get the message's ID
             long id = editingCursor.getLong(editingCursor.getColumnIndexOrThrow("_id"));
 
             // keep the current last changed ID
             long oldLastChanged = lastSMSId;
 
             // update our counter
             lastSMSId = id;
 
             // while the new ID is still greater than the last altered message
             // loop just in case messages come in quick succession
             while (id > oldLastChanged) {
                 // alter the time stamp
                 alterMessage(id);
 
                 // base case, handle there being no more messages and break out
                 if (editingCursor.isLast()) {
                     break;
                 }
 
                 // move to the next message
                 editingCursor.moveToNext();
 
                 // grab its ID
                 id = editingCursor.getLong(editingCursor.getColumnIndexOrThrow("_id"));
             }
         } else {
             // there aren't any messages, reset the id counter
             lastSMSId = -1;
         }
     }
 
     /**
      * Get the desired offset change based on the user's preferences
      * 
      * @return long
      */
     private long getOffset() {
         long offset = 0;
 
         // if the user wants us to auto-determine the offset use the negative of
         // their GMT offset
         String method = settings.getString("offset_method", "manual");
         if (method.equals("automatic") || method.equals("neg_automatic")) {
             offset = TimeZone.getDefault().getRawOffset();
 
             // account for DST
             if (TimeZone.getDefault().useDaylightTime() && TimeZone.getDefault().inDaylightTime(new Date())) {
                 offset += 3600000;
             }
 
             if (method.equals("automatic")) {
                 offset *= -1;
             }
 
             // otherwise, use the offset the user has specified
         } else {
             offset = Integer.parseInt(settings.getString("offset_hours", "0")) * 3600000;
             offset += Integer.parseInt(settings.getString("offset_minutes", "0")) * 60000;
         }
 
         return offset;
     }
 
     /**
      * Alter the time stamp of the message with the given ID
      * 
      * @param id
      *            - the ID of the message to be altered
      */
     private void alterMessage(long id) {
         Log.i(getClass().getSimpleName(), "Adjusting timestamp for message: " + id);
 
         // grab the date assigned to the message
         long longdate = editingCursor.getLong(editingCursor.getColumnIndexOrThrow("date"));
 
         // if the user has asked for the Future Only option, make sure the
         // message
         // time is greater than the phone time, giving a 5 second grace
         // period
 
         // keeping the preference name as cdma so when users upgrade it uses
         // their current value
         if (!settings.getBoolean("cdma", false) || (longdate - (new Date()).getTime() > 5000)) {
             // if the user wants to use the phone's time, use the current date
             if (settings.getString("offset_method", "manual").equals("phone")) {
                 longdate = (new Date()).getTime();
             } else {
                 longdate = longdate + getOffset();
             }
         }
 
         // update the message with the new time stamp
         ContentValues values = new ContentValues();
         values.put("date", longdate);
         getContentResolver().update(editingURI, values, "_id = " + id, null);
     }
 
     /**
      * Checks if the roaming condition is met. Returns true if the user doesn't
      * care about roaming, or if the user does but isn't currently roaming
      * 
      * @return boolean
      */
     private boolean roamingConditionMet() {
         boolean onlyRoaming = settings.getBoolean("roaming", false);
         boolean isRoaming = telephonyManager.isNetworkRoaming();
 
         boolean roamingCondition = !(onlyRoaming && isRoaming);
 
         return roamingCondition;
     }
 
     /**
      * ContentObserver to handle updates to the SMS database
      * 
      * @author Matthew Precious
      * 
      */
     private class FixServiceObserver extends ContentObserver {
 
         public FixServiceObserver() {
             super(null);
         }
 
         @Override
         public void onChange(boolean selfChange) {
             super.onChange(selfChange);
 
             // if the change wasn't self inflicted
             // TODO: make this boolean actually work...
             if (!selfChange && roamingConditionMet()) {
                 Log.i(getClass().getSimpleName(), "SMS database altered, checking...!");
                 // requery the database to get the latest messages
                 editingCursor.requery();
 
                 // fix them
                 fixLastMessage();
             }
         }
     }
 
 }
