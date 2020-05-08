 /**
  *  OpenAlarm - an extensible alarm for Android
  *  Copyright (C) 2010 Liu Yen-Liang (Josh)
  *
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * @file   Alarms.java
  * @author  <yenliangl@gmail.com>
  * @date   Wed Sep 30 16:47:42 2009
  *
  * @brief  Utility class.
  *
  *
  */
 package org.startsmall.openalarm;
 
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.provider.BaseColumns;
 import android.provider.Settings;
 import android.net.Uri;
 import android.util.Log;
 import android.text.format.DateUtils;
 import android.text.TextUtils;
 import android.widget.Toast;
 
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.text.DateFormat;
 import java.util.Iterator;
 import java.util.List;
 import java.text.SimpleDateFormat;
 
 public class Alarms {
     private static final String TAG = "Alarms";
 
     /**
      * Authority of this application.
      * <p>Value: org.startsmall.openalarm</p>
      */
     public static final String CONTENT_URI_AUTH = "org.startsmall.openalarm";
 
     /**
      * Alarm alert action string.
      * <p>Value: org.startsmall.openalarm.HANDLE_ALARM</p>
      */
     public static final String ACTION_HANDLE_ALARM = CONTENT_URI_AUTH + ".action.HANDLE_ALARM";
 
     public static final String ACTION_SCHEDULE_ALARM = CONTENT_URI_AUTH + ".action.SCHEDULE_ALARM";
 
 
     /**
      * Action used to launch ActionDispatcher receiver.
      * <p>Value: org.startsmall.openalarm.DISPATCH_ACTION</p>
      */
     // public static final String DISPATCH_ACTION = CONTENT_URI_AUTH + ".DISPATCH_ACTION";
 
     /**
      * Content URI of this application.
      *
      * <p>Value: content://org.startsmall.openalarm</p>
      */
     public static final String CONTENT_URI = "content://" + CONTENT_URI_AUTH;
 
     /**
      * Content URI for all alarms
      *
      * <p>Value: content://org.startsmall.openalarm/alarms</p>
      */
     public static final String CONTENT_URI_PATH = "alarms";
     public static final String CONTENT_URI_ALL_ALARMS =
         CONTENT_URI + "/" + CONTENT_URI_PATH;
 
     /**
      * Content URI for a single alarm
      *
      * <p>Value: content://org.startsmall.openalarm/alarms/#</p>
      */
     public static final String CONTENT_URI_SINGLE_ALARM =
         CONTENT_URI_ALL_ALARMS + "/#";
 
     /**
      * Convenient java.util.Calendar instance.
      *
      */
     private static final Calendar CALENDAR = Calendar.getInstance();
     public static Calendar getCalendarInstance() {
         CALENDAR.setTimeInMillis(System.currentTimeMillis());
         return CALENDAR;
     }
 
     public static final String PREFERENCE_FILE_FOR_SNOOZED_ALARM = "snoozed_alarm";
 
     private static final String USER_APK_DIR = "/data/app";
 
     public static final int ERROR_NO_DAYS_SET = 1;
     public static final int ERROR_NO_HANDLER_SET = 2;
 
     /*****************************************************************
      * Constants used in content provider and SQLiteDatabase.    *
      *****************************************************************/
     public static class AlarmColumns implements BaseColumns {
 
         /// Default sort order
         public static final String DEFAULT_SORT_ORDER = "_id ASC";
 
         //// Inherited fields : _ID
 
         /// Label of this alarm
         public static final String LABEL = "label";
 
         /// Hour in 24-hour (0 - 23)
         public static final String HOUR = "hour";
 
         /// Minutes (0 - 59)
         public static final String MINUTES = "minutes";
 
         /// Go off time in milliseconds
         public static final String AT_TIME_IN_MILLIS = "time";
 
         /// Days this alarm works in this week.
         public static final String REPEAT_DAYS = "repeat_days";
 
         /// Whether or not this alarm is active currently
         public static final String ENABLED = "enabled";
 
         /// Audio to play when alarm triggers.
         public static final String HANDLER = "handler";
 
         /// Audio to play when alarm triggers.
         public static final String EXTRA = "extra";
 
         /**
          * Columns that will be pulled from a row. These should
          * be in sync with PROJECTION indexes.
          *
          */
         public static final String[] QUERY_COLUMNS = {
             _ID, LABEL, HOUR, MINUTES, AT_TIME_IN_MILLIS, REPEAT_DAYS,
             ENABLED, HANDLER, EXTRA};
 
         /**
          *
          */
         public static final int PROJECTION_ID_INDEX = 0;
         public static final int PROJECTION_LABEL_INDEX = 1;
         public static final int PROJECTION_HOUR_INDEX = 2;
         public static final int PROJECTION_MINUTES_INDEX = 3;
         public static final int PROJECTION_AT_TIME_IN_MILLIS_INDEX = 4;
         public static final int PROJECTION_REPEAT_DAYS_INDEX = 5;
         public static final int PROJECTION_ENABLED_INDEX = 6;
         public static final int PROJECTION_HANDLER_INDEX = 7;
         public static final int PROJECTION_EXTRA_INDEX = 8;
     }
 
     /**
      * Suppress default constructor for noninstantiability.
      */
     private Alarms() {}
 
     /******************************************************************
      * Encoder/Decode repeat days.
      ******************************************************************/
     public static class RepeatWeekdays {
         /**
          * 0x01 Calendar.SUNDAY
          * 0x02 Calendar.MONDAY
          * 0x04 Calendar.TUESDAY
          * 0x08 Calendar.WEDNESDAY
          * 0x10 Calendar.THURSDAY
          * 0x20 Calendar.FRIDAY
          * 0x40 Calendar.SATURDAY
          *
          * 0x7F On everyday
          */
 
         /**
          * Suppress default constructor for noninstantiability.
          */
         private RepeatWeekdays() {}
 
         public static boolean isSet(int code, int day) {
             return (code & encode(day)) > 0;
         }
 
         public static int set(int code, int day, boolean enabled) {
             if(enabled) {
                 code = code | encode(day);
             } else {
                 code = code & ~encode(day);
             }
             return code;
         }
 
         private static int encode(int day) {
             if(day < Calendar.SUNDAY || day > Calendar.SATURDAY) {
                 throw new IllegalArgumentException(
                     "Weekday must be among SUNDAY to SATURDAY");
             }
             return (1 << (day - 1));
         }
 
         public static String toString(int code, String everyday, String notSet) {
             String result = "";
             if(code > 0) {
                 if(code == 0x7F) { // b1111111
                     result = everyday;
                 } else {
                     for(int i = 1; i < 8; i++) { // From SUNDAY to SATURDAY
                         if(isSet(code, i)) {
                             result =
                                 result +
                                 DateUtils.getDayOfWeekString(
                                     i,
                                     DateUtils.LENGTH_MEDIUM) +
                                 " ";
                         }
                     }
                 }
             } else {
                 result = notSet;
             }
             return result;
         }
 
         public static List<String> toStringList(int code, String everyday, String notSet) {
             return Arrays.asList(
                 toString(code, everyday, notSet).split(" "));
         }
     }
 
     /**
      * Return Uri of an alarm with id given.
      *
      * @param alarmId ID of the alarm. If -1 is given, it returns Uri representing all alarms.
      *
      * @return Uri of the alarm.
      */
     public static Uri getAlarmUri(final long alarmId) {
         if(alarmId == -1) {
             return Uri.parse(CONTENT_URI_ALL_ALARMS);
         } else {
             return Uri.parse(CONTENT_URI_ALL_ALARMS + "/" + alarmId);
         }
     }
 
     /**
      *
      *
      * @param context
      * @param alarmUri
      *
      * @return
      */
     public synchronized static Cursor getAlarmCursor(Context context,
                                                      Uri alarmUri) {
         return
             context.getContentResolver().query(
                 alarmUri,
                 AlarmColumns.QUERY_COLUMNS,
                 null,
                 null,
                 AlarmColumns.DEFAULT_SORT_ORDER);
     }
 
     /**
      * Listener interface that is used to report settings for every alarm
      * existed on the database.
      *
      */
     public static interface OnVisitListener {
         void onVisit(final Context context,
                      final int id,
                      final String label,
                      final int hour,
                      final int minutes,
                      final long atTimeInMillis,
                      final int repeatOnDaysCode,
                      final boolean enabled,
                      final String handler,
                      final String extra);
     }
 
     /**
      * Helper class that stores settings of an alarm in public
      * feilds for outsider to access.
      *
      */
     public static class GetAlarmSettings implements OnVisitListener {
         public int id;
         public String label;
         public int hour;
         public int minutes;
         public long atTimeInMillis;
         public int repeatOnDaysCode;
         public boolean enabled;
         public String handler;
         public String extra;
 
         @Override
         public void onVisit(final Context context,
                             final int id,
                             final String label,
                             final int hour,
                             final int minutes,
                             final long atTimeInMillis,
                             final int repeatOnDaysCode,
                             final boolean enabled,
                             final String handler,
                             final String extra) {
             this.id = id;
             this.label = label;
             this.hour = hour;
             this.minutes = minutes;
             this.atTimeInMillis = atTimeInMillis;
             this.repeatOnDaysCode = repeatOnDaysCode;
             this.enabled = enabled;
             this.handler = handler;
             this.extra = extra;
         }
     }
 
     /**
      * Iterate alarms.
      *
      * @param context
      * @param alarmUri
      * @param listener
      */
     public static void forEachAlarm(final Context context,
                                     final Uri alarmUri,
                                     final OnVisitListener listener) {
         Cursor cursor = getAlarmCursor(context, alarmUri);
         if(cursor.moveToFirst()) {
             do {
                 final int id =
                     cursor.getInt(AlarmColumns.PROJECTION_ID_INDEX);
                 final String label =
                     cursor.getString(AlarmColumns.PROJECTION_LABEL_INDEX);
                 final int hour =
                     cursor.getInt(AlarmColumns.PROJECTION_HOUR_INDEX);
                 final int minutes =
                     cursor.getInt(AlarmColumns.PROJECTION_MINUTES_INDEX);
                 final long atTimeInMillis =
                     cursor.getLong(AlarmColumns.PROJECTION_AT_TIME_IN_MILLIS_INDEX);
                 final int repeatOnDaysCode =
                     cursor.getInt(AlarmColumns.PROJECTION_REPEAT_DAYS_INDEX);
                 final boolean enabled =
                     cursor.getInt(AlarmColumns.PROJECTION_ENABLED_INDEX) == 1;
                 final String handler =
                     cursor.getString(AlarmColumns.PROJECTION_HANDLER_INDEX);
                 final String extra =
                     cursor.getString(AlarmColumns.PROJECTION_EXTRA_INDEX);
 
                 if(listener != null) {
                     listener.onVisit(context, id, label, hour, minutes,
                                      atTimeInMillis, repeatOnDaysCode,
                                      enabled, handler, extra);
                 }
             } while(cursor.moveToNext());
         }
         cursor.close();
     }
 
     /**
      * Insert a new alarm record into database.
      *
      * @param context Context this is calling from.
      *
      * @return Uri of the newly inserted alarm.
      */
     public synchronized static Uri newAlarm(Context context) {
         return context.getContentResolver().insert(
             Uri.parse(CONTENT_URI_ALL_ALARMS), null);
     }
 
     /**
      * Delete an alarm from content database.
      *
      * @param context Context object
      * @param alarmId Id of an alarm.
      *
      * @return
      */
     public synchronized static int deleteAlarm(Context context,
                                                int alarmId) {
         Uri alarmUri = Alarms.getAlarmUri(alarmId);
         return context.getContentResolver().delete(alarmUri, null, null);
     }
 
     /**
      * Update alarm's settings in content database
      *
      * @param context Context object
      * @param alarmId Alarm id.
      * @param newValues New values of settings.
      *
      * @return
      */
     public synchronized static int updateAlarm(final Context context,
                                                final Uri alarmUri,
                                                final ContentValues newValues) {
         if(newValues == null) {
             return -1;
         }
 
         return context.getContentResolver().update(
             alarmUri, newValues, null, null);
     }
 
     /**
      * Enable/disable an alarm by activating/disactivating this
      * alarm in AlarmManager and update its enabled state in
      * database.
      *
      * @param context Context this method is called.
      * @param alarmUri Alarm uri.
      * @param enabled Enable or disable this alarm.
      */
     public static synchronized int setAlarmEnabled(final Context context,
                                                    final Uri alarmUri,
                                                    final boolean enabled) {
         Log.d(TAG, "===> setAlarmEnabled(): alarmUri=" + alarmUri + ", enabled=" + enabled);
 
         // Fetch alarm's settings in the content database.
         GetAlarmSettings settings = new GetAlarmSettings();
         forEachAlarm(context, alarmUri, settings);
 
         // Check the integrity of the alarm's settings.
         if (TextUtils.isEmpty(settings.handler)) {
             return ERROR_NO_HANDLER_SET;
         } else if (settings.repeatOnDaysCode == 0) {
             return ERROR_NO_DAYS_SET;
         }
 
         ContentValues newValues = new ContentValues();
         newValues.put(AlarmColumns.ENABLED, enabled ? 1 : 0);
 
         // Enable the alarm in system.
         if (enabled) {
             long timeInMillis =
                 calculateAlarmAtTimeInMillis(settings.hour,
                                              settings.minutes,
                                              settings.repeatOnDaysCode);
             enableAlarm(context, settings.id, settings.label, timeInMillis,
                         settings.repeatOnDaysCode, settings.handler,
                         settings.extra);
             showToast(context, settings.label, timeInMillis);
 
             newValues.put(AlarmColumns.AT_TIME_IN_MILLIS, timeInMillis);
         } else {
             disableAlarm(context, settings.id, settings.handler);
         }
 
         // Update settings of an alarm (enabled and newTimeInMillis)
         updateAlarm(context, alarmUri, newValues);
 
         // Show notification on the status bar to indicate that
         // an alarm is setup or cancel notification if no alarms are setup.
         setNotification(context, enabled);
 
         // Update alarm in system settings
         updateSystemSetting(context);
 
         return 0;
     }
 
     /**
      * Snooze an alarm
      *
      */
     public static void snoozeAlarm(final Context context,
                                    final int alarmId,
                                    final String label,
                                    final int repeatOnDays,
                                    final String handlerClassName,
                                    final String extraData,
                                    final int minutesLater) {
         // Cancel the old alert.
         disableAlarm(context, alarmId, handlerClassName);
 
         // Arrange new time for snoozed alarm from current date
         // and time.
         Calendar calendar = getCalendarInstance();
         calendar.add(Calendar.MINUTE, minutesLater);
         int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
         int minutes = calendar.get(Calendar.MINUTE);
 
         long newAtTimeInMillis = calendar.getTimeInMillis();
         enableAlarm(context, alarmId, label, newAtTimeInMillis, repeatOnDays,
                     handlerClassName, extraData);
 
         // Put info into SharedPreferences for the snoozed alarm.
         SharedPreferences preferences =
             context.getSharedPreferences(PREFERENCE_FILE_FOR_SNOOZED_ALARM,
                                          0);
         SharedPreferences.Editor preferenceEditor = preferences.edit();
         preferenceEditor.putLong(AlarmColumns.AT_TIME_IN_MILLIS,
                                  newAtTimeInMillis);
         preferenceEditor.putInt(AlarmColumns._ID, alarmId);
         preferenceEditor.putString(AlarmColumns.LABEL, label);
         // The handler is persisted because it is needed when we
         // want to cancel the snoozed alarm.
         preferenceEditor.putString(AlarmColumns.HANDLER, handlerClassName);
         preferenceEditor.commit();
     }
 
     /**
      * Cancel an snoozed alarm.
      *
      */
     public static void cancelSnoozedAlarm(final Context context,
                                           final int alarmId) {
 
         SharedPreferences preferences =
             context.getSharedPreferences(PREFERENCE_FILE_FOR_SNOOZED_ALARM, 0);
         final int persistedAlarmId = preferences.getInt(AlarmColumns._ID, -1);
 
         if (alarmId != -1 &&     // no checking on alarmId
             persistedAlarmId != alarmId) {
             // The alarmId was not snoozed before. No need to
             // cancel it.
             return;
         }
 
         final String handler =
             preferences.getString(AlarmColumns.HANDLER, null);
         if (!TextUtils.isEmpty(handler)) {
 
             Log.d(TAG, "===> Snoozed alarm with id " + alarmId + " has handler set to '" + handler + "'");
             disableAlarm(context, persistedAlarmId, handler);
             // Remove _ID to indicate that the snoozed alert is cancelled.
             preferences.edit().remove(AlarmColumns._ID).commit();
         }
     }
 
     /**
      * Enable an alarm in AlarmManager.
      *
      */
     public static void enableAlarm(final Context context,
                                    final int alarmId,
                                    final String label,
                                    final long atTimeInMillis,
                                    final int repeatOnDays,
                                    final String handlerClassName,
                                    final String extraData) {
         Intent i = new Intent(ACTION_HANDLE_ALARM, getAlarmUri(alarmId));
         try {
             Class<?> handlerClass = getHandlerClass(handlerClassName);
 
             // @note Note that the contextPackagename is not the
             // same as Java packate. We should always place
             // receivers in the root package directory, for
             // instance, org.startsmall.openalarm instead of
             // org.startsmall.openalarm.receiver.
             String contextPackageName = handlerClass.getPackage().getName();
             i.setClassName(contextPackageName, handlerClassName);
         } catch (ClassNotFoundException e) {
             Log.d(TAG, "===> Handler is not set for this alarm");
             return;
         }
         i.addCategory(Intent.CATEGORY_ALTERNATIVE);
         i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_USER_ACTION);
 
         // Alarm ID is always necessary for its operations.
         i.putExtra(AlarmColumns._ID, alarmId);
         i.putExtra(AlarmColumns.LABEL, label);
 
         // Extract hourOfDay and minutes
         Calendar c = getCalendarInstance();
         c.setTimeInMillis(atTimeInMillis);
         final int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
         final int minutes = c.get(Calendar.MINUTE);
         i.putExtra(AlarmColumns.HOUR, hourOfDay);
         i.putExtra(AlarmColumns.MINUTES, minutes);
         i.putExtra(AlarmColumns.REPEAT_DAYS, repeatOnDays);
         i.putExtra(AlarmColumns.HANDLER, handlerClassName);
 
         if (!TextUtils.isEmpty(extraData)) {
             i.putExtra(AlarmColumns.EXTRA, extraData);
         }
 
         AlarmManager alarmManager =
             (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
         alarmManager.set(AlarmManager.RTC_WAKEUP,
                          atTimeInMillis,
                          PendingIntent.getBroadcast(
                              context, 0, i,
                              PendingIntent.FLAG_CANCEL_CURRENT));
     }
 
     /**
      * @return true if clock is set to 24-hour mode
      */
     public static boolean is24HourMode(final Context context) {
         return android.text.format.DateFormat.is24HourFormat(context);
     }
 
     /**
      * Disable an alarm in AlarmManager.
      *
      */
     public static void disableAlarm(final Context context,
                                     final int alarmId,
                                     final String handlerClassName) {
         Uri alarmUri = getAlarmUri(alarmId);
 
         Intent i = new Intent(ACTION_HANDLE_ALARM, alarmUri);
         i.setClassName(context, handlerClassName);
         i.addCategory(Intent.CATEGORY_ALTERNATIVE);
 
         AlarmManager alarmManager =
             (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 
         alarmManager.cancel(PendingIntent.getBroadcast(
                                 context, 0, i,
                                 PendingIntent.FLAG_CANCEL_CURRENT));
     }
 
     /**
      * Set the nearest alarm in system settings.
      *
      */
     public static void setAlarmInSystemSettings(final Context context,
                                                 final Calendar calendar) {
         String timeString = "";
         if (calendar != null) {
             timeString =
                 DateUtils.formatDateTime(
                     context,
                     calendar.getTimeInMillis(),
                     DateUtils.FORMAT_SHOW_TIME|DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_CAP_AMPM|DateUtils.FORMAT_SHOW_WEEKDAY|DateUtils.FORMAT_SHOW_YEAR);
         }
 
         Settings.System.putString(context.getContentResolver(),
                                   Settings.System.NEXT_ALARM_FORMATTED,
                                   timeString);
     }
 
     /**
      * Return the formatted date and time string.
      *
      */
     public static String formatTime(String pattern,
                                     Calendar calendar) {
         SimpleDateFormat dateFormatter = new SimpleDateFormat(pattern);
         return dateFormatter.format(calendar.getTime());
     }
 
     /*
      * Calculate the next alarm time.
      *
      */
     public static long calculateAlarmAtTimeInMillis(final int hourOfDay,
                                                     final int minutes,
                                                     final int repeatOnCode) {
        // Start with current date and time.
         Calendar calendar = getCalendarInstance();
        int nowHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int nowMinutes = calendar.get(Calendar.MINUTE);

        // If (hourOfDay, minutes) points to a time at the past,
        // we move calendar to the (hourOfDay, minutes)
        // tomorrow. The worst case is that (hourOfDay, minutes)
        // is the next time of the alarm.
        if((hourOfDay < nowHourOfDay) ||
           ((hourOfDay == nowHourOfDay) && (minutes < nowMinutes))) {
             calendar.add(Calendar.DAY_OF_YEAR, 1);
         }

        // Align calendar's time with this alarm.
         calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
         calendar.set(Calendar.MINUTE, minutes);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
 
         // Try to shift calendar by days in order to find the
         // nearest alarm.
         while(true) {
             if(RepeatWeekdays.isSet(repeatOnCode,
                                     calendar.get(Calendar.DAY_OF_WEEK))) {
                 break;
             }
             calendar.add(Calendar.DAY_OF_YEAR, 1);
         }
 
         return calendar.getTimeInMillis();
     }
 
     // Put an notification on the status bar or remove
     // notification if no alarm are enabled.
     public static void setNotification(final Context context,
                                        final boolean enabled) {
         if (enabled) {
             broadcastAlarmChanged(context, true);
         } else {
             // If there are more than 2 alarms are still enabled,
             // don't remove notification
             final int numberOfEnabledAlarms = getNumberOfEnabledAlarms(context);
             if (numberOfEnabledAlarms == 0) {
                 broadcastAlarmChanged(context, false);
             }
         }
     }
 
     // Return the handler class object.
     public static Class<?> getHandlerClass(final String handlerClassName)
         throws ClassNotFoundException {
         final int lastDotPos = handlerClassName.lastIndexOf('.');
         final String apkPaths =
             USER_APK_DIR + "/" + "org.startsmall.openalarm.apk:" + // myself
             // handlers defined by other developers
             USER_APK_DIR + "/" + handlerClassName.substring(0, lastDotPos) + ".apk";
 
         dalvik.system.PathClassLoader classLoader =
             new dalvik.system.PathClassLoader(
                 apkPaths,
                 ClassLoader.getSystemClassLoader());
 
         return Class.forName(handlerClassName, true, classLoader);
     }
 
     // Get ActivityInfo object of a handler.
     public static ActivityInfo getHandlerInfo(final PackageManager pm,
                                               final String handlerClassName)
         throws PackageManager.NameNotFoundException {
 
         Intent i = new Intent(ACTION_HANDLE_ALARM);
         i.addCategory(Intent.CATEGORY_ALTERNATIVE);
 
         // Search all receivers that can handle my alarms.
         Iterator<ResolveInfo> infoObjs = queryAlarmHandlers(pm).iterator();
         while (infoObjs.hasNext()) {
             ActivityInfo activityInfo = infoObjs.next().activityInfo;
             if (activityInfo.name.equals(handlerClassName)) {
                 return activityInfo;
             }
         }
         throw new PackageManager.NameNotFoundException(
             "BroadcastReceiver " + handlerClassName + " not found");
     }
 
     public static List<ResolveInfo> queryAlarmHandlers(final PackageManager pm) {
         Intent i = new Intent(ACTION_HANDLE_ALARM);
         i.addCategory(Intent.CATEGORY_ALTERNATIVE);
         return pm.queryBroadcastReceivers(i, 0);
     }
 
 
     private synchronized static int getNumberOfEnabledAlarms(Context context) {
         Cursor c =
             context.getContentResolver().query(
                 getAlarmUri(-1),
                 new String[]{AlarmColumns._ID, AlarmColumns.ENABLED},
                 AlarmColumns.ENABLED + "=1",
                 null,
                 AlarmColumns.DEFAULT_SORT_ORDER);
         final int count = c.getCount();
         c.close();
         return count;
     }
 
     private static void broadcastAlarmChanged(Context context,
                                               boolean enabled) {
         final String ACTION_ALARM_CHANGED = "android.intent.action.ALARM_CHANGED";
         Intent i = new Intent(ACTION_ALARM_CHANGED);
         i.putExtra("alarmSet", enabled);
         context.sendBroadcast(i);
     }
 
     private static void showToast(final Context context,
                                   final String label,
                                   final long atTimeInMillis) {
         String dateTimeString =
             DateUtils.formatDateTime(
                 context,
                 atTimeInMillis,
                 DateUtils.FORMAT_SHOW_TIME|DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_CAP_AMPM|
                 DateUtils.FORMAT_SHOW_WEEKDAY|DateUtils.FORMAT_SHOW_YEAR);
 
         String text =
             context.getString(R.string.alarm_notification_toast_text, label, dateTimeString);
         Toast.makeText(context, text, Toast.LENGTH_LONG).show();
     }
 
     // Iterate all alarms and find out the nearest alarm.
     private static void updateSystemSetting(final Context context) {
         class GetNearestAlarmTime implements OnVisitListener {
             public long timeInMillis = Long.MAX_VALUE;
 
             @Override
             public void onVisit(final Context context,
                                 final int id,
                                 final String label,
                                 final int hour,
                                 final int minutes,
                                 final long atTimeInMillis,
                                 final int repeatOnDaysCode,
                                 final boolean enabled,
                                 final String handler,
                                 final String extra) {
                 if (enabled &&
                     atTimeInMillis < timeInMillis) {
                     timeInMillis = atTimeInMillis;
                 }
             }
         }
 
         GetNearestAlarmTime getNearestAlarm = new GetNearestAlarmTime();
         forEachAlarm(context, getAlarmUri(-1), getNearestAlarm);
 
         if (getNearestAlarm.timeInMillis != Long.MAX_VALUE) {
             Calendar calendar = getCalendarInstance();
             calendar.setTimeInMillis(getNearestAlarm.timeInMillis);
             setAlarmInSystemSettings(context, calendar);
         } else {
             setAlarmInSystemSettings(context, null);
         }
     }
 }
