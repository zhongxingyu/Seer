 package com.sndurkin.notificationcheck;
 
 import android.app.ActivityManager;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.AudioManager;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import java.util.*;
 
 // This is where most of the logic for the application lives; when the SCREEN_ON event
 // is fired, the phone vibrates if there are any notifications.
 public class ScreenOnReceiver extends BroadcastReceiver {
 
     public static final String NOTIFICATION_POSTED_INTENT = "com.sndurkin.notificationcheck.NOTIFICATION_POSTED_INTENT";
     public static final String NOTIFICATION_REMOVED_INTENT = "com.sndurkin.notificationcheck.NOTIFICATION_REMOVED_INTENT";
 
     private boolean missedPhoneCall = false;
 
     private class NotificationModel {
         public String packageName;
         public int id;
     }
     private List<NotificationModel> notifications = new ArrayList<NotificationModel>();
 
     // Singleton pattern
     private static ScreenOnReceiver instance = new ScreenOnReceiver();
     public static ScreenOnReceiver getInstance() {
         return instance;
     }
     private ScreenOnReceiver() {
         super();
     }
 
     @Override
     public synchronized void onReceive(Context context, Intent intent) {
         String action = intent.getAction();
         if(NOTIFICATION_POSTED_INTENT.equals(action)) {
             NotificationModel notification = new NotificationModel();
             notification.packageName = intent.getStringExtra("packageName");
             notification.id = intent.getIntExtra("id", -1);
             notifications.add(notification);
         }
         else if(NOTIFICATION_REMOVED_INTENT.equals(action)) {
             String packageName = intent.getStringExtra("packageName");
             int id = intent.getIntExtra("id", -1);
             Iterator<NotificationModel> iter = notifications.iterator();
             while(iter.hasNext()) {
                 NotificationModel notification = iter.next();
                 if(notification.id == id && notification.packageName.equals(packageName)) {
                     iter.remove();
                 }
             }
         }
         else if(Intent.ACTION_SCREEN_OFF.equals(action)) {
             final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
             final int prefNotificationType = Integer.parseInt(preferences.getString("pref_notification_type", "0"));
             if(!missedPhoneCall) {
                 requestToClearNotifications(prefNotificationType);
             }
             else {
                 missedPhoneCall = false;
             }
             //Log.d("NotificationCheck", "SCREEN_OFF received at " + SystemClock.uptimeMillis());
         }
         else if(Intent.ACTION_SCREEN_ON.equals(action)) {
             ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
             List<ActivityManager.RunningTaskInfo> appProcesses = activityManager.getRunningTasks(1);
             Log.d("Notification Check", "Foreground Package: " + appProcesses.get(0).topActivity.getPackageName());
 
             final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
             final int prefNotificationType = Integer.parseInt(preferences.getString("pref_notification_type", "0"));
             if(!preferences.getBoolean("pref_active", false)) {
                 requestToClearNotifications(prefNotificationType);
                 return;
             }
 
             if(!shouldCheckForNotifications(context, preferences)) {
                 requestToClearNotifications(prefNotificationType);
                 return;
             }
 
             final PhoneCallListener phoneCallListener = PhoneCallListener.getInstance();
             if(phoneCallListener.isPhoneRinging()) {
                 // If the screen is turned on because the phone is ringing,
                 // the user probably isn't aware of it because the ringer
                 // isn't on, so we defer vibration to the next time
                 // the screen is turned on (unless the call is answered).
                 phoneCallListener.addObserver(new PhoneCallListener.Observer() {
                     @Override
                     public void onCallMissed() {
                         missedPhoneCall = true;
                         phoneCallListener.removeObserver(this);
                     }
 
                     @Override
                     public void onCallAnswered() {
                         requestToClearNotifications(prefNotificationType);
                         phoneCallListener.removeObserver(this);
                     }
                 });
                 return;
             }
 
             boolean atLeastOneNotification = !notifications.isEmpty();
             boolean vibrateForNotifications;
 
             int prefWhatToCheck = Integer.parseInt(preferences.getString("pref_what", "0"));
             if(prefWhatToCheck == SettingsActivity.FilterByApp.ALL_APPS.ordinal()) {
                 //Log.d("NotificationCheck", "Will vibrate for notifications because we're monitoring all notifications");
                 vibrateForNotifications = true;
             }
             else {
                 vibrateForNotifications = false;
                 List<String> selectedPackages = NotificationListPreference.extractListFromPref(preferences.getString("pref_notifications", ""));
                 for(NotificationModel notification : notifications) {
                     if(selectedPackages.contains(notification.packageName)) {
                         if(prefWhatToCheck == SettingsActivity.FilterByApp.ONLY_SELECTED_APPS.ordinal()) {
                             //Log.d("NotificationCheck", "Will vibrate for notifications because " + notification.packageName + " was among those selected");
                             vibrateForNotifications = true;
                         }
                     }
                     else {
                         if(prefWhatToCheck == SettingsActivity.FilterByApp.ALL_BUT_SELECTED_APPS.ordinal()) {
                             //Log.d("NotificationCheck", "Will vibrate for notifications because " + notification.packageName + " was NOT among those selected");
                             vibrateForNotifications = true;
                         }
                     }
                 }
             }
 
             requestToClearNotifications(prefNotificationType);
 
             if(vibrateForNotifications && atLeastOneNotification) {
                 //Log.d("NotificationCheck", "Vibrating");
                 Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                 int prefVibrationPattern = Integer.parseInt(preferences.getString("pref_vibration_pattern", "0"));
                 if(prefVibrationPattern == SettingsActivity.VibrationPattern.SHORT.ordinal()) {
                     v.vibrate(SettingsActivity.SHORT_VIBRATION_LENGTH);
                 }
                 else if(prefVibrationPattern == SettingsActivity.VibrationPattern.LONG.ordinal()) {
                     v.vibrate(SettingsActivity.LONG_VIBRATION_LENGTH);
                 }
                 else if(prefVibrationPattern == SettingsActivity.VibrationPattern.TWO_SHORT.ordinal()) {
                     v.vibrate(new long[] { 0, SettingsActivity.SHORT_VIBRATION_LENGTH, 200, SettingsActivity.SHORT_VIBRATION_LENGTH }, -1);
                 }
                 else if(prefVibrationPattern == SettingsActivity.VibrationPattern.TWO_LONG.ordinal()) {
                     v.vibrate(new long[] { 0, SettingsActivity.LONG_VIBRATION_LENGTH, 200, SettingsActivity.LONG_VIBRATION_LENGTH }, -1);
                 }
                 else {
                     throw new RuntimeException("The vibration pattern preference has an invalid value: " + prefVibrationPattern);
                 }
             }
         }
     }
 
     // This method is used to ignore notifications that occurred before the screen shut off. On Android < 4.3,
     // we have to assume the user has seen these because there's no way to tell when a notification has been
     // dismissed by the user.
     //
     // If we're working with Android 4.3+, we can tell when notifications are dismissed, so the user dictates
     // whether notifications are ignored by Notification Check via the When To Vibrate preference.
     private void requestToClearNotifications(int prefNotificationType) {
         if(prefNotificationType == SettingsActivity.NotificationType.NEW_NOTIFICATIONS.ordinal()
                 || prefNotificationType == SettingsActivity.NotificationType.NEW_NON_PERSISTED_NOTIFICATIONS.ordinal()) {
             notifications.clear();
         }
     }
 
     // When the screen is turned on, this method is called to determine if we even need to check for notifications.
     // If the phone ringer doesn't align with the preferences, we don't bother checking.
     private boolean shouldCheckForNotifications(Context context, SharedPreferences preferences) {
         AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
         String prefPhoneRinger = preferences.getString("pref_phone_ringer_v2", "0" + MultiSelectListPreference.SEPARATOR + "1");
         List<Integer> prefPhoneRingerValues = MultiSelectListPreference.getValuesFromString(prefPhoneRinger);
         switch(am.getRingerMode()) {
             case AudioManager.RINGER_MODE_SILENT:
                 if(prefPhoneRingerValues.contains(SettingsActivity.PhoneRinger.SILENT.ordinal())) {
                     return true;
                 }
                 break;
             case AudioManager.RINGER_MODE_VIBRATE:
                 if(prefPhoneRingerValues.contains(SettingsActivity.PhoneRinger.VIBRATE.ordinal())) {
                     return true;
                 }
                 break;
             case AudioManager.RINGER_MODE_NORMAL:
                 if(prefPhoneRingerValues.contains(SettingsActivity.PhoneRinger.NORMAL.ordinal())) {
                     return true;
                 }
                 break;
         }
 
         return false;
     }
 
 }
