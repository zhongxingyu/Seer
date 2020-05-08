 package com.aquasoup.aquasparrow;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.os.CountDownTimer;
 import android.os.IBinder;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 
 import ioio.lib.api.DigitalOutput;
 import ioio.lib.api.exception.ConnectionLostException;
 import ioio.lib.util.BaseIOIOLooper;
 import ioio.lib.util.IOIOLooper;
 import ioio.lib.util.android.IOIOService;
 
 public class SparrowService extends IOIOService {
     private static final int PIN = 6;
     private static final long TIME_TO_FINISH = 5000; // 5s
     private static final long TIME_TICK = 1000;

     private NotificationCompat.Builder notificationBuilder;
     private Context ctx = SparrowService.this;
     private Resources res;
     private DigitalOutput valvePin;
 
     private BroadcastReceiver SmsReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             sendLogToUI(res.getString(R.string.command_obtained));
             TimerTickToCloseValve.start();
         }
     };
 
     private CountDownTimer TimerTickToCloseValve = new CountDownTimer(TIME_TO_FINISH, TIME_TICK) {
 
         @Override
         public void onTick(long millisUntilFinished) {
            if (millisUntilFinished == TIME_TO_FINISH) {
                openValve();
            }
         }
 
         @Override
         public void onFinish() {
             closeValve();
         }
     };
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onStart(Intent intent, int startId) {
         super.onStart(intent, startId);
         res = getResources();
         Notification notice = createNotification();
         notice.flags |= Notification.FLAG_NO_CLEAR;
         startForeground(SparrowConstants.NOTIFICATION_ID, notice);
         RegisterLogsReceiver();
         sendLogToUI(res.getString(R.string.service_started));
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         stopForeground(true);
         unregisterReceiver(SmsReceiver);
         sendLogToUI(res.getString(R.string.service_destroyed));
     }
 
     @Override
     protected IOIOLooper createIOIOLooper() {
         return new BaseIOIOLooper() {
 
             @Override
             protected void setup() throws ConnectionLostException, InterruptedException {
                 valvePin = ioio_.openDigitalOutput(PIN, false);
             }
 
             @Override
             public void loop() throws ConnectionLostException, InterruptedException {
 
             }
         };
     }
 
     private Notification createNotification() {
         String notificationTitle = res.getString(R.string.notification_title);
         String notificationText = res.getString(R.string.notification_text);
 
         Intent resultIntent = new Intent(ctx, SparrowActivity.class);
         resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 
         TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
         stackBuilder.addParentStack(SparrowActivity.class);
         stackBuilder.addNextIntent(resultIntent);
         PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
 
         notificationBuilder = new NotificationCompat.Builder(ctx);
         notificationBuilder.setWhen(System.currentTimeMillis());
         notificationBuilder.setContentIntent(resultPendingIntent);
         notificationBuilder.setContentTitle(notificationTitle);
         notificationBuilder.setContentText(notificationText);
         notificationBuilder.setOngoing(true);
         notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
 
         return notificationBuilder.build();
     }
 
     private void sendLogToUI(String log) {
         Intent intent = new Intent();
         intent.setAction(SparrowConstants.ACTION_LOG);
         intent.putExtra(SparrowConstants.STRING_ID, log);
         sendBroadcast(intent);
     }
 
     private void openValve() {
         sendLogToUI(res.getString(R.string.open_valve));
         // valvePin.write(true);
     }
 
     private void closeValve() {
         sendLogToUI(res.getString(R.string.close_valve));
         // valvePin.write(false);
     }
 
     private void RegisterLogsReceiver() {
         IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
         registerReceiver(SmsReceiver, filter);
     }
 
 }
