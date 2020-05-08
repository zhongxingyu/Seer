 package net.fififox.dailycalendaralarm;
 
 import java.util.Calendar;
 
 import android.app.AlarmManager;
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.database.Cursor;
 import android.media.AudioManager;
 import android.media.RingtoneManager;
 import android.provider.CalendarContract.Instances;
 
 /**
  * An service which periodically checks for events in the calendar and rings before the first one each day.
  * @author Joan Rieu
  */
 public class AlarmService extends IntentService {
 
     /**
      * The id used to create and cancel the alarm {@link Notification}.
      */
     public static final int ALARM_NOTIFICATION_ID = 0;
 
     /**
      * The request code used to schedule search {@link Intent}s in the {@link AlarmManager}.
      */
     private static final int SEARCH_RQ = 0;
 
     /**
      * The request code used to schedule alarm {@link Intent}s in the {@link AlarmManager}.
      */
     private static final int RUN_RQ = 1;
 
     /**
      * The request code used to start the {@link AlarmActivity}.
      */
     private static final int ALARM_ACTIVITY_RQ = 2;
 
     /**
      * The time interval between search {@link Intent}s in the {@link AlarmManager}.
      */
     private static final long SEARCH_INTERVAL = AlarmManager.INTERVAL_HOUR;
 
     /**
      * The preferences shared with the {@link SetupActivity}.
      */
     private Settings mSettings;
 
     /**
      * Describes a calendar event with helper methods for alarm times.
      */
     private static class Alarm {
 
         /**
          * The name of the calendar event.
          */
         private final String mEventTitle;
 
         /**
          * The date and time that the calendar event will begin at.
          */
         private final long mEventTime;
 
         /**
          * Fills in a calendar event's data and logs it.
          * @param eventTitle The name of the event.
          * @param eventTime The date and time of the event.
          */
         public Alarm(String eventTitle, long eventTime) {
             mEventTitle = eventTitle;
             mEventTime = eventTime;
         }
 
         /**
          * Gets the name of the calendar event.
          * @return The name of the event.
          */
         public String getEventTitle() {
             return mEventTitle;
         }
 
         /**
          * Gets the date and time that the calendar event will begin at.
          * @return The date and time of the calendar event's beginning.
          */
         public long getEventTime() {
             return mEventTime;
         }
 
         /**
          * Gets the date and time that the alarm for this event should ring at.
          * @param offset The time to subtract from the event's start time.
          * @return The date and time the alarm should ring at.
          */
         public long getAlarmTime(long offset) {
             return mEventTime - offset;
         }
 
     }
 
     /**
      * Creates and names the worker thread.
      */
     public AlarmService() {
         super(AlarmService.class.getName());
     }
 
     /**
      * Analyzes received {@link Intent}s from {@link SetupActivity} and calls the appropriate functions.
      * @param intent The received {@link Intent}.
      */
     @Override
     protected void onHandleIntent(Intent intent) {
 
         mSettings = new Settings(this);
 
         if (intent.getAction() == Intent.ACTION_CONFIGURATION_CHANGED) {
             updateSearchSchedule();
         } else if (intent.getAction() == Intent.ACTION_RUN) {
             runAlarm();
             updateNextAlarm();
         } else if (intent.getAction() == Intent.ACTION_SEARCH) {
             updateNextAlarm();
         }
 
         mSettings = null;
 
     }
 
     /**
      * Defines or cancels a repeating event search {@link Intent}, depending on {@link Settings#isEnabled()}.
      */
     private void updateSearchSchedule() {
 
         AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 
         Intent searchIntent = new Intent(this, AlarmService.class);
         searchIntent.setAction(Intent.ACTION_SEARCH);
         PendingIntent pendingSearch = PendingIntent.getService(this, SEARCH_RQ, searchIntent, 0);
 
         if (mSettings.isEnabled()) {
 
             // Search ON.
 
             alarmManager.setInexactRepeating(
                     AlarmManager.RTC_WAKEUP,
                     0,
                     SEARCH_INTERVAL,
                     pendingSearch
             );
 
         } else {
 
             // Search OFF.
 
             alarmManager.cancel(pendingSearch);
             updateNextAlarm();
 
         }
     }
 
     /**
      * Sets up an alarm if a calendar event is found matching the app's requirements.
      */
     private void updateNextAlarm() {
 
         AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 
         Intent runIntent = new Intent(this, AlarmService.class);
         runIntent.setAction(Intent.ACTION_RUN);
         PendingIntent pendingRun = PendingIntent.getService(this, RUN_RQ, runIntent, 0);
 
         if (mSettings.isEnabled()) {
 
             Alarm alarm = getNextAlarm();
 
             if (alarm != null) {
 
                 // Alarm ON.
 
                 long time = alarm.getAlarmTime(mSettings.getOffsetInMillis());
                 alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingRun);
 
                 mSettings.setNextAlarmTitle(alarm.getEventTitle());
                 mSettings.setNextAlarmTime(time);
 
             } else {
 
                 // Alarm OFF (no event).
 
                 alarmManager.cancel(pendingRun);
                 mSettings.clearNextAlarm();
 
             }
 
         } else {
 
             // Alarm OFF (disabled).
 
             alarmManager.cancel(pendingRun);
             mSettings.clearNextAlarm();
 
         }
 
     }
 
     /**
      * Searches for events in the calendar matching all of the following criteria:
      * <ul>
      *     <li>the event is the first of its day (either today or tomorrow);</li>
      *     <li>the event doesn't last all day;</li>
      *     <li>the event's alarm hasn't already rung.</li>
      * </ul>
      * @return
      */
     private Alarm getNextAlarm() {
 
         Alarm alarm;
 
         Calendar calendar = Calendar.getInstance();
         final long now = calendar.getTimeInMillis();
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         final long today = calendar.getTimeInMillis();
         calendar.roll(Calendar.DATE, true);
         final long tomorrow = calendar.getTimeInMillis();
         calendar.roll(Calendar.DATE, true);
         final long afterTomorrow = calendar.getTimeInMillis();
 
        String[] projection = new String[] { Instances.TITLE, Instances.BEGIN };
        Cursor cursor = Instances.query(getContentResolver(), projection, today, tomorrow);
 
         if (cursor.moveToFirst()) {
             // Found an event today.
             alarm = new Alarm(cursor.getString(0), cursor.getLong(1));
         } else {
             // No event in calendar for today.
             alarm = null;
         }
 
         cursor.close();
 
         if (alarm == null
                 || alarm.getEventTime() < now
                 || mSettings.getLastAlarmTime() >= alarm.getAlarmTime(mSettings.getOffsetInMillis())) {
 
             // Checking tomorrow (no event || we're past the event || the alarm already rang).
 
            cursor = Instances.query(getContentResolver(), projection, tomorrow, afterTomorrow);
 
             if (cursor.moveToFirst()) {
                 // Found an event tomorrow.
                 alarm = new Alarm(cursor.getString(0), cursor.getLong(1));
             } else {
                 // No event found tomorrow.
                 alarm = null;
             }
 
             cursor.close();
 
             if (alarm != null
                     && mSettings.getLastAlarmTime() >= alarm.getAlarmTime(mSettings.getOffsetInMillis())) {
                 // The alarm already rang.
                 alarm = null;
             }
 
         }
 
         return alarm;
 
     }
 
     /**
      * Rings the alarm.
      * It uses a Notification with:
      * <ul>
      *     <li>the event's title;</li>
      *     <li>the event's date and time;</li>
      *     <li>the default alarm ringtone;</li>
      *     <li>a custom light pattern;</li>
      *     <li>the default notification vibration pattern;</li>
      *     <li>the highest priority.</li>
      * </ul>
      */
     @SuppressWarnings("deprecation")
     private void runAlarm() {
 
         Alarm alarm = new Alarm(
                 mSettings.getNextAlarmTitle(),
                 mSettings.getNextAlarmTime() + mSettings.getOffsetInMillis()
         );
 
         mSettings.setLastAlarmTime(System.currentTimeMillis());
         mSettings.clearNextAlarm();
 
         Intent intent = new Intent(this, AlarmActivity.class);
         intent.putExtra("title", alarm.getEventTitle());
         intent.putExtra("time", alarm.getEventTime());
 
         PendingIntent pending = PendingIntent.getActivity(
                 this,
                 ALARM_ACTIVITY_RQ,
                 intent,
                 PendingIntent.FLAG_CANCEL_CURRENT
         );
 
         Notification.Builder builder = new Notification.Builder(this)
                 .setSmallIcon(R.drawable.ic_launcher)
                 .setContentTitle(alarm.getEventTitle())
                 .setContentText(getText(R.string.app_name))
                 .setWhen(alarm.getEventTime())
                 .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                         AudioManager.STREAM_ALARM)
                 .setLights(0xFFFFFFFF, 200, 200)
                 .setDefaults(Notification.DEFAULT_VIBRATE)
                 .setAutoCancel(true)
                 .setFullScreenIntent(pending, true);
 
         Notification notification = builder.getNotification();
         notification.flags |= Notification.FLAG_INSISTENT;
 
         NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         notificationManager.notify(ALARM_NOTIFICATION_ID, notification);
 
     }
 
 }
