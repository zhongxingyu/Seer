 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.deskclock;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Parcel;
 import android.os.PowerManager.WakeLock;
 import android.text.TextUtils;
 
 import com.android.deskclock.provider.Alarm;
 
 import java.util.Calendar;
 
 /**
  * Glue class: connects AlarmAlert IntentReceiver to AlarmAlert
  * activity.  Passes through Alarm ID.
  */
 public class AlarmReceiver extends BroadcastReceiver {
 
     /** If the alarm is older than STALE_WINDOW, ignore.  It
         is probably the result of a time or timezone change */
     private final static int STALE_WINDOW = 30 * 60 * 1000;
 
     @Override
     public void onReceive(final Context context, final Intent intent) {
         final PendingResult result = goAsync();
         final WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
         wl.acquire();
         AsyncHandler.post(new Runnable() {
             @Override public void run() {
                 handleIntent(context, intent);
                 result.finish();
                 wl.release();
             }
         });
     }
 
     private void handleIntent(Context context, Intent intent) {
         if (Alarms.ALARM_KILLED.equals(intent.getAction())) {
             // The alarm has been killed, update the notification
             updateNotification(context, (Alarm)
                     intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA),
                     intent.getIntExtra(Alarms.ALARM_KILLED_TIMEOUT, -1));
             return;
         } else if (Alarms.CANCEL_SNOOZE.equals(intent.getAction())) {
             Alarm alarm = null;
             if (intent.hasExtra(Alarms.ALARM_INTENT_EXTRA)) {
                 // Get the alarm out of the Intent
                 alarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
             }
 
             if (alarm != null) {
                 Alarms.disableSnoozeAlert(context, alarm.id);
                 Alarms.setNextAlert(context);
             } else {
                 // Don't know what snoozed alarm to cancel, so cancel them all.  This
                 // shouldn't happen
                 Log.wtf("Unable to parse Alarm from intent.");
                 Alarms.saveSnoozeAlert(context, Alarm.INVALID_ID, -1);
             }
             // Inform any active UI that alarm snooze was cancelled
             context.sendBroadcast(new Intent(Alarms.ALARM_SNOOZE_CANCELLED));
             return;
         } else if (!Alarms.ALARM_ALERT_ACTION.equals(intent.getAction())) {
             // Unknown intent, bail.
             return;
         }
 
         Alarm alarm = null;
         // Grab the alarm from the intent. Since the remote AlarmManagerService
         // fills in the Intent to add some extra data, it must unparcel the
         // Alarm object. It throws a ClassNotFoundException when unparcelling.
         // To avoid this, do the marshalling ourselves.
         final byte[] data = intent.getByteArrayExtra(Alarms.ALARM_RAW_DATA);
         if (data != null) {
             Parcel in = Parcel.obtain();
             in.unmarshall(data, 0, data.length);
             in.setDataPosition(0);
             alarm = Alarm.CREATOR.createFromParcel(in);
         }
 
         if (alarm == null) {
             Log.wtf("Failed to parse the alarm from the intent");
             // Make sure we set the next alert if needed.
             Alarms.setNextAlert(context);
             return;
         }
 
         // Disable the snooze alert if this alarm is the snooze.
         Alarms.disableSnoozeAlert(context, alarm.id);
         // Disable this alarm if it does not repeat.
         if (!alarm.daysOfWeek.isRepeating()) {
             Alarms.enableAlarm(context, alarm.id, false);
         } else {
             // Enable the next alert if there is one. The above call to
             // enableAlarm will call setNextAlert so avoid calling it twice.
             Alarms.setNextAlert(context);
         }
 
         // Intentionally verbose: always log the alarm time to provide useful
         // information in bug reports.
         long now = System.currentTimeMillis();
         long alarmTime = alarm.calculateAlarmTime();
         Log.v("Received alarm set for id=" + alarm.id + " " + Log.formatTime(alarmTime) + " "
                 + alarm.label);
 
         // Always verbose to track down time change problems.
         if (now > alarmTime + STALE_WINDOW) {
             Log.v("Ignoring stale alarm");
             return;
         }
 
         // Maintain a cpu wake lock until the AlarmAlert and AlarmKlaxon can
         // pick it up.
         AlarmAlertWakeLock.acquireCpuWakeLock(context);
 
         /* Close dialogs and window shade */
         Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
         context.sendBroadcast(closeDialogs);
 
         // Decide which activity to start based on the state of the keyguard.
         Class c = AlarmAlertFullScreen.class;
         /*
         KeyguardManager km = (KeyguardManager) context.getSystemService(
                 Context.KEYGUARD_SERVICE);
         if (km.inKeyguardRestrictedInputMode()) {
             // Use the full screen activity for security.
             c = AlarmAlertFullScreen.class;
         }
         */
 
         // Play the alarm alert and vibrate the device.
         Intent playAlarm = new Intent(Alarms.ALARM_ALERT_ACTION);
        playAlarm.setClass(context,AlarmKlaxon.class);
         playAlarm.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
         context.startService(playAlarm);
 
         // Trigger a notification that, when clicked, will show the alarm alert
         // dialog. No need to check for fullscreen since this will always be
         // launched from a user action.
         // NEW: Embed the full-screen UI here. The notification manager will
         // take care of displaying it if it's OK to do so.
         Intent alarmAlert = new Intent(context, c);
         alarmAlert.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
         alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                 | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
         // Make sure to use FLAG_CANCEL_CURRENT or the notification manager will just
         // use the older intent if it has the same alarm.id
         PendingIntent pendingIntent = PendingIntent.getActivity(context, (int)alarm.id, alarmAlert,
                 PendingIntent.FLAG_UPDATE_CURRENT);
 
         // These two notifications will be used for the action buttons on the notification.
         Intent snoozeIntent = new Intent(Alarms.ALARM_SNOOZE_ACTION);
         snoozeIntent.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
         PendingIntent pendingSnooze = PendingIntent.getBroadcast(context,
                 (int)alarm.id, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         Intent dismissIntent = new Intent(Alarms.ALARM_DISMISS_ACTION);
         dismissIntent.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
         PendingIntent pendingDismiss = PendingIntent.getBroadcast(context,
                 (int)alarm.id, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 
         final Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(alarmTime);
         String alarmTimeLabel = Alarms.formatTime(context, cal);
 
         // Use the alarm's label or the default label main text of the notification.
         String label = alarm.getLabelOrDefault(context);
 
         Notification n = new Notification.Builder(context)
         .setContentTitle(label)
         .setContentText(alarmTimeLabel)
         .setSmallIcon(R.drawable.stat_notify_alarm)
         .setOngoing(true)
         .setAutoCancel(false)
         .setPriority(Notification.PRIORITY_MAX)
         .setDefaults(Notification.DEFAULT_LIGHTS)
         .setWhen(0)
         .addAction(R.drawable.stat_notify_alarm,
                 context.getResources().getString(R.string.alarm_alert_snooze_text),
                 pendingSnooze)
         .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                 context.getResources().getString(R.string.alarm_alert_dismiss_text),
                 pendingDismiss)
         .build();
         n.contentIntent = pendingIntent;
         n.fullScreenIntent = pendingIntent;
 
         // Send the notification using the alarm id to easily identify the
         // correct notification.
         NotificationManager nm = getNotificationManager(context);
         nm.cancel((int)alarm.id);
         nm.notify((int)alarm.id, n);
     }
 
     private NotificationManager getNotificationManager(Context context) {
         return (NotificationManager)
                 context.getSystemService(Context.NOTIFICATION_SERVICE);
     }
 
     private void updateNotification(Context context, Alarm alarm, int timeout) {
         NotificationManager nm = getNotificationManager(context);
 
         // If the alarm is null, just cancel the notification.
         if (alarm == null) {
             if (Log.LOGV) {
                 Log.v("Cannot update notification for killer callback");
             }
             return;
         }
 
         // Launch AlarmClock when clicked.
         Intent viewAlarm = new Intent(context, DeskClock.class);
         viewAlarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         viewAlarm.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
         viewAlarm.putExtra(DeskClock.SELECT_TAB_INTENT_EXTRA, DeskClock.ALARM_TAB_INDEX);
         PendingIntent intent = PendingIntent.getActivity(context, (int)alarm.id, viewAlarm,
                 PendingIntent.FLAG_UPDATE_CURRENT);
 
         // Update the notification to indicate that the alert has been
         // silenced.
         String title = context.getString(R.string.alarm_missed_title);
         String label = alarm.label;
         String alarmTime = Alarms.formatTime(context, alarm.calculateAlarmCalendar());
         // If the label is null, just show the alarm time. If not, show "time - label".
         String text = TextUtils.isEmpty(label)? alarmTime :
             context.getString(R.string.alarm_missed_text, alarmTime, label);
 
         Notification n = new Notification.Builder(context)
         .setContentTitle(title)
         .setContentText(text)
         .setSmallIcon(R.drawable.stat_notify_alarm)
         .setAutoCancel(true)
         .setPriority(Notification.PRIORITY_HIGH)
         .setDefaults(Notification.DEFAULT_ALL)
         .build();
         n.contentIntent = intent;
         // We have to cancel the original notification since it is in the
         // ongoing section and we want the "killed" notification to be a plain
         // notification.
         nm.cancel((int)alarm.id);
         nm.notify((int)alarm.id, n);
     }
 }
