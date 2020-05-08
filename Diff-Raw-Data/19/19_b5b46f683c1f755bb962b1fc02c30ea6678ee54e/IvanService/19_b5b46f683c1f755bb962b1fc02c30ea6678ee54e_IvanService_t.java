 package org.aivan.androitest1;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.os.IBinder;
 import android.util.Log;
 
 public class IvanService extends Service {
 
  private static final String PREFERENCES_LEVEL = "level";

private static final String PREFERENCES_DATE = "date";

static final String TAG = IvanService.class.getName();
 
   private static final int ANDRO_BAT_ID = 1;
 
   /**
    * 12 hours (in ms)
    */
   protected static final long MAX_MS_FOR_ONE_PERCENT = 1000 * 60 * 60 * 12;
 
   @Override
   public IBinder onBind(Intent intent) {
     // TODO Auto-generated method stub
     Log.d(TAG, "onBind");
     return null;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see android.app.Service#onCreate()
    */
   @Override
   public void onCreate() {
     // TODO Auto-generated method stub
     super.onCreate();
     Log.d(TAG, "onCreate");
 
     // AlarmManager alarmManager = (AlarmManager)
     // getSystemService(Context.ALARM_SERVICE);
     // alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 5000, 10000,
     // PendingIntent.getService(this, 0 , new Intent(this,
     // IvanService.class),
     // PendingIntent.FLAG_UPDATE_CURRENT ));
 
     registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
 
     updateNotificationIcon();
 
   }
 
   private void updateNotificationIcon() {
     String ns = Context.NOTIFICATION_SERVICE;
     NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
 
     int icon = R.drawable.bat_level_list;
     CharSequence tickerText = null;
     long when = System.currentTimeMillis();
 
     Notification notification = new Notification(icon, tickerText, when);
 
     Context context = getApplicationContext();
     CharSequence contentTitle = getResources().getText(R.string.app_name);
     CharSequence contentText = "Current battery level: " + lastLevel + " %";
     ;
     Intent notificationIntent = new Intent(this, MainActivity.class);
     PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
 
     notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
     notification.flags |= Notification.FLAG_ONGOING_EVENT;
     // notification.number = (int) lastLevel;
     notification.iconLevel = (int) lastLevel;
 
     mNotificationManager.notify(ANDRO_BAT_ID, notification);
   }
 
   private void cancelNotificationIcon() {
     String ns = Context.NOTIFICATION_SERVICE;
     NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
     mNotificationManager.cancelAll();
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see android.app.Service#onStart(android.content.Intent, int)
    */
   @Override
   public void onStart(Intent intent, int startId) {
     // TODO Auto-generated method stub
     super.onStart(intent, startId);
 
     Log.d(TAG, "onStart");
 
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see
    * android.app.Service#onConfigurationChanged(android.content.res.Configuration
    * )
    */
   @Override
   public void onConfigurationChanged(Configuration newConfig) {
     // TODO Auto-generated method stub
     super.onConfigurationChanged(newConfig);
     Log.d(TAG, "onConfigurationChanged");
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see android.app.Service#onDestroy()
    */
   @Override
   public void onDestroy() {
     // TODO Auto-generated method stub
     super.onDestroy();
     Log.d(TAG, "onDestroy");
 
     // AlarmManager alarmManager = (AlarmManager)
     // getSystemService(Context.ALARM_SERVICE);
     // alarmManager.cancel( PendingIntent.getService(this, 0 , new
     // Intent(this,
     // IvanService.class), PendingIntent.FLAG_UPDATE_CURRENT ));
 
     cancelNotificationIcon();
 
     unregisterReceiver(this.mBatInfoReceiver);
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see android.app.Service#onLowMemory()
    */
   @Override
   public void onLowMemory() {
     // TODO Auto-generated method stub
     super.onLowMemory();
     Log.d(TAG, "onLowMemory");
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see android.app.Service#onRebind(android.content.Intent)
    */
   @Override
   public void onRebind(Intent intent) {
     // TODO Auto-generated method stub
     super.onRebind(intent);
     Log.d(TAG, "onRebind");
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
    */
   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
     // TODO Auto-generated method stub
     Log.d(TAG, "onStartCommand");
     return super.onStartCommand(intent, flags, startId);
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see android.app.Service#onUnbind(android.content.Intent)
    */
   @Override
   public boolean onUnbind(Intent intent) {
     // TODO Auto-generated method stub
     Log.d(TAG, "onUnbind");
     return super.onUnbind(intent);
   }
 
   static long lastLevel = Long.MIN_VALUE;
 
   private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
 
     @Override
     public void onReceive(Context context, Intent intent) {
       Log.d("mBatInfoReceiver", "onReceive");
      int level = intent.getIntExtra(PREFERENCES_LEVEL, 0);
       Log.d("mBatInfoReceiver", "level is " + level);
       long time = System.currentTimeMillis();
 
       if (level != lastLevel || (lastLevel == Long.MIN_VALUE && levelNotSame(context, time, level))) {
         // Ok, maybe we have same level in preferences:
 
         // Write new battery level only if it has changed since last one
         // written to the database
         new HistoryDAO(context).addHistoryRecord(time, level);
         // Record last level in a static field
         lastLevel = level;
         // Also record level in preferences
         SharedPreferences prefs = context.getSharedPreferences("IvanService", Context.MODE_PRIVATE);
         SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(PREFERENCES_DATE, time);
        editor.putInt(PREFERENCES_LEVEL, level);
         editor.commit();
 
         updateNotificationIcon();
 
       }
 
       // TODO: this is debug stuff
       // Toast.makeText(context,
       // "Added battery level "+level+" into the DB!",
       // Toast.LENGTH_SHORT).show();
 
     }
 
     private boolean levelNotSame(Context context, long time, int level) {
       boolean result = false;
       SharedPreferences prefs = context.getSharedPreferences("IvanService", Context.MODE_PRIVATE);
      long oldTime = prefs.getLong(PREFERENCES_DATE, 0);
      int oldLevel = prefs.getInt(PREFERENCES_LEVEL, -1);
 
       if (oldTime == 0 || oldLevel < 0 || oldLevel != level || (time - oldTime > MAX_MS_FOR_ONE_PERCENT)) {
         // Ok, we have some old value but it's not the same as the new one or
         // the time range is too long
         // so this is probably some new reading and store it as new:
         result = true;
       }
 
       return result;
     }
   };
 
 }
