 package org.tamanegi.parasiticalarm;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.EnumSet;
 import java.util.Random;
 
 import android.app.AlarmManager;
 import android.app.IntentService;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.SystemClock;
 
 public class AlarmService extends IntentService
 {
     public static final String ACTION_SETUP =
         "org.tamanegi.parasiticalarm.action.SETUP";
     public static final String ACTION_ALARM =
         "org.tamanegi.parasiticalarm.action.ALARM";
     public static final String ACTION_CANCEL_SNOOZE =
         "org.tamanegi.parasiticalarm.action.CANCEL_SNOOZE";
 
     private static final String EXTRA_ALARM_START =
         "org.tamanegi.parasiticalarm.extra.ALARM_START";
     private static final String EXTRA_ALARM_ALERT =
         "org.tamanegi.parasiticalarm.extra.ALARM_ALERT";
 
     static final String EXTRA_ALARM_ID =
         "org.tamanegi.parasiticalarm.extra.ALARM_ID";
     static final String EXTRA_SNOOZE_ENABLED =
         "org.tamanegi.parasiticalarm.extra.SNOOZE_ENABLED";
     static final String EXTRA_ALERT_AUDIO =
         "org.tamanegi.parasiticalarm.extra.ALERT_AUDIO";
     static final String EXTRA_ALERT_IMAGE =
         "org.tamanegi.parasiticalarm.extra.ALERT_IMAGE";
     static final String EXTRA_ALERT_MESSAGE =
         "org.tamanegi.parasiticalarm.extra.ALERT_MESSAGE";
     static final String EXTRA_AFTER_AUDIO =
         "org.tamanegi.parasiticalarm.extra.AFTER_AUDIO";
     static final String EXTRA_AFTER_IMAGE =
         "org.tamanegi.parasiticalarm.extra.AFTER_IMAGE";
     static final String EXTRA_BACKGROUND =
         "org.tamanegi.parasiticalarm.extra.BACKGROUND";
     static final String EXTRA_VIBRATION_ENABLED =
         "org.tamanegi.parasiticalarm.extra.VIBRATION_ENABLED";
 
     public AlarmService()
     {
         super("AlarmService");
     }
 
     @Override
     protected void onHandleIntent(Intent intent)
     {
         if(intent.getAction().equals(ACTION_SETUP)) {
             setup();
         }
         else if(intent.getAction().equals(ACTION_ALARM)) {
             alarm(intent);
             AlertWakeLock.release(); // acquired at AlarmReceiver#onReceive
         }
         else if(intent.getAction().equals(ACTION_CANCEL_SNOOZE)) {
             cancelSnooze(intent);
         }
     }
 
     private void setup()
     {
         AlarmSettings settings = new AlarmSettings(this);
 
         for(int i = 0; i < AlarmSettings.SETTING_COUNT; i++) {
             setup(settings, i);
         }
     }
 
     private void setup(AlarmSettings settings, int index)
     {
         long at = getNextAlarmTime(settings, index);
         PendingIntent intent = getAlarmIntent(index, at, at);
 
         if(settings.getOnOff(index)) {
             setAlarm(at, intent);
         }
         else {
             cancelAlarm(intent);
         }
     }
 
     private void alarm(Intent intent)
     {
         int index = getAlarmIndex(intent);
         if(index < 0) {
             return;
         }
 
         AlarmSettings settings = new AlarmSettings(this);
 
         // load alarms
         String[] alarms = settings.getAlarms(index);
         ArrayList<AlarmData> list = new ArrayList<AlarmData>();
         for(String alarm : alarms) {
             AlarmData d = AlarmData.unflattenFromString(this, alarm);
             if(d == null) {
                 continue;
             }
 
             list.add(d);
         }
         if(list.size() == 0) {
             return;
         }
 
         // select alarm
         Random random = new Random();
         AlarmData data = list.get(random.nextInt(list.size()));
 
         // build intent
         Intent activityIntent = new Intent(this, AlertActivity.class)
             .putExtra(EXTRA_ALARM_ID, index)
             .putExtra(EXTRA_SNOOZE_ENABLED, settings.isSnoozeEnabled(index))
             .putExtra(EXTRA_ALERT_AUDIO, data.getAlertAudio())
             .putExtra(EXTRA_ALERT_IMAGE,
                       getRandomElement(random, data.getAlertImage()))
             .putExtra(EXTRA_ALERT_MESSAGE, data.getAlertMessage())
             .putExtra(EXTRA_AFTER_AUDIO,
                       getRandomElement(random, data.getAfterAudio()))
             .putExtra(EXTRA_AFTER_IMAGE,
                       getRandomElement(random, data.getAfterImage()))
             .putExtra(EXTRA_BACKGROUND,
                       getRandomElement(random, data.getBackground()))
             .putExtra(EXTRA_VIBRATION_ENABLED, settings.getVibration(index))
             .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
         // start alert activity
         AlertWakeLock.acquire(this); // release at AlertActivity#onDestroy
         startActivity(activityIntent);
 
         // disable if repeat is `once` and snooze is off
         if(settings.getDay(index).isEmpty() &&
            ! settings.isSnoozeEnabled(index)) {
             settings.setOnOff(index, false);
             return;
         }
 
         // set next alarm: snooze or next cycle
         boolean isSnooze = settings.isSnoozeEnabled(index);
         long interval = settings.getSnoozeInterval(index) * 60L * 1000L;
         long timeout = settings.getSnoozeTimeout(index) * 60L * 1000L;
         long startAt = intent.getLongExtra(EXTRA_ALARM_START, -1);
         long alertAt = intent.getLongExtra(EXTRA_ALARM_ALERT, -1);
         if(startAt < 0 || alertAt < 0) {
             return;
         }
         if(isSnooze && startAt + timeout < alertAt + interval) {
             cancelSnooze(index);
             isSnooze = false;
         }
 
         long nextStartAt, nextAlertAt;
         if(isSnooze) {
             nextStartAt = startAt;
             nextAlertAt = alertAt + interval;
         }
         else {
             nextStartAt = getNextAlarmTime(settings, index);
             nextAlertAt = nextStartAt;
         }
 
         PendingIntent alarmIntent =
             getAlarmIntent(index, nextStartAt, nextAlertAt);
         setAlarm(nextAlertAt, alarmIntent);
     }
 
     private void cancelSnooze(Intent intent)
     {
         int index = intent.getIntExtra(EXTRA_ALARM_ID, -1);
         if(index < 0) {
             return;
         }
 
         cancelSnooze(index);
     }
 
     private void cancelSnooze(int index)
     {
         AlarmSettings settings = new AlarmSettings(this);
 
         // disable if repeat is `once`
         if(settings.getDay(index).isEmpty()) {
             settings.setOnOff(index, false);
         }
 
         setup(settings, index);
     }
 
     private long getNextAlarmTime(AlarmSettings settings, int index)
     {
         Calendar now = Calendar.getInstance();
         Calendar cal = (Calendar)now.clone();
         cal.set(Calendar.HOUR_OF_DAY, settings.getTimeHour(index));
         cal.set(Calendar.MINUTE, settings.getTimeMinute(index));
         cal.set(Calendar.SECOND, 0);
 
         EnumSet<AlarmSettings.DayOfWeek> days = settings.getDay(index);
         if(days.isEmpty()) {
             if(cal.before(now)) {
                 cal.add(Calendar.DAY_OF_MONTH, 1);
             }
         }
         else {
             Calendar next = null;
             for(AlarmSettings.DayOfWeek day : days) {
                 Calendar c = (Calendar)cal.clone();
                 c.set(Calendar.DAY_OF_WEEK, day.getCalendarValue());
                 if(c.before(now)) {
                     c.add(Calendar.WEEK_OF_MONTH, 1);
                 }
 
                 if(next == null || c.before(next)) {
                     next = c;
                 }
             }
 
             cal = next;
         }
 
         return cal.getTime().getTime();
     }
 
     private PendingIntent getAlarmIntent(int index, long startAt, long alertAt)
     {
         Intent intent = new Intent(this, AlarmReceiver.class)
             .setAction(AlarmService.ACTION_ALARM)
             .setData(new Uri.Builder()
                      .scheme(AlarmReceiver.ALARM_SCHEME)
                      .opaquePart(String.valueOf(index))
                      .build())
             .putExtra(EXTRA_ALARM_START, startAt)
             .putExtra(EXTRA_ALARM_ALERT, alertAt);
        return PendingIntent.getBroadcast(this, 0, intent, 0);
     }
 
     private void setAlarm(long at, PendingIntent intent)
     {
         long cur = System.currentTimeMillis();
         if((at - cur) < 0) {
             return;
         }
 
         long erAt = (at - cur) + SystemClock.elapsedRealtime();
         AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
         am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, erAt, intent);
     }
 
     private void cancelAlarm(PendingIntent intent)
     {
         AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
         am.cancel(intent);
     }
 
     private int getAlarmIndex(Intent intent)
     {
         Uri uri = intent.getData();
         if(uri == null) {
             return -1;
         }
 
         int val;
         try {
             val = Integer.parseInt(uri.getSchemeSpecificPart());
         }
         catch(NumberFormatException e) {
             e.printStackTrace();
             return -1;
         }
 
         if(val < 0 || val >= AlarmSettings.SETTING_COUNT) {
             return -1;
         }
 
         return val;
     }
 
     private static <T> T getRandomElement(Random random, T[] array)
     {
         if(array != null) {
             return array[random.nextInt(array.length)];
         }
         else {
             return null;
         }
     }
 }
