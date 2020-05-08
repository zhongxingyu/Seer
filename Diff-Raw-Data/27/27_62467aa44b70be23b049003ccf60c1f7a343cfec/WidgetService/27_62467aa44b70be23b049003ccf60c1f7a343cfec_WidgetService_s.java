 package ru.shutoff.caralarm;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.IBinder;
 import android.os.PowerManager;
 
 public class WidgetService extends Service {
 
     static final int UPDATE_INTERVAL = 5 * 60 * 1000;
 
     static final String ACTION_STOP = "ru.shutoff.caralarm.WIDGET_STOP";
     static final String ACTION_UPDATE = "ru.shutoff.caralarm.WIDGET_UPDATE";
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     PowerManager powerMgr;
     AlarmManager alarmMgr;
 
     PendingIntent pi;
     BroadcastReceiver br;
 
     @Override
     public void onCreate() {
         super.onCreate();
        State.setExceptionHandler();
         powerMgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
         alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         pi = PendingIntent.getService(this, 0, new Intent(this, WidgetService.class), 0);
         br = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SCREEN_ON)) {
                     stopTimer();
                     startTimer(true);
                 }
                 if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SCREEN_OFF)) {
                     stopTimer();
                 }
             }
         };
         IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
         filter.addAction(Intent.ACTION_SCREEN_OFF);
         registerReceiver(br, filter);
     }
 
     @Override
     public void onDestroy() {
         unregisterReceiver(br);
         super.onDestroy();
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         if (intent != null) {
             String action = intent.getAction();
             if (action != null) {
                 if (action.equals(ACTION_STOP)) {
                     stopTimer();
                     stopSelf();
                     return START_STICKY;
                 }
                 if (action.equals(ACTION_UPDATE)) {
                     stopTimer();
                     if (powerMgr.isScreenOn())
                         startTimer(false);
                     return START_STICKY;
                 }
             }
         }
         Intent i = new Intent(this, StatusService.class);
         startService(i);
         return START_STICKY;
     }
 
     void startTimer(boolean now) {
         alarmMgr.setInexactRepeating(AlarmManager.RTC,
                 System.currentTimeMillis() + (now ? 0 : UPDATE_INTERVAL), UPDATE_INTERVAL, pi);
     }
 
     void stopTimer() {
         alarmMgr.cancel(pi);
     }
 }
