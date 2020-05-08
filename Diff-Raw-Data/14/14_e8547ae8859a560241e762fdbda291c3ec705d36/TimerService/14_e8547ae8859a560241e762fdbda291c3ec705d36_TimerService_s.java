 package com.jeffthefate.buzztimer;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.graphics.BitmapFactory;
 import android.os.Binder;
 import android.os.CountDownTimer;
 import android.os.IBinder;
 import android.os.Vibrator;
 import android.support.v4.app.NotificationCompat;
 
 /**
  * Service that runs in the background.  Registers receivers for actions that
  * the app will respond to.  Also, handles starting the widget updates.
  * 
  * @author Jeff
  */
 public class TimerService extends Service {
     
     private boolean loop = true;
     private boolean isRunning = false;
     private Timer timer;
     private Notification notification;
     private NotificationCompat.Builder nBuilder;
     private Resources res;
     private NotificationManager nManager;
     
     public interface UiCallback {
         public void updateTime(int mSecs, boolean setLocal);
     }
     
     private UiCallback uiCallback;
     
     public class CancelReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction().equals(Constants.ACTION_STOP_TIMER)) {
 				stopTimer();
 				if (uiCallback != null)
 					uiCallback.updateTime(ApplicationEx.getMsecs(), false);
 			}
 		}
     }
     
     private CancelReceiver cancelReceiver;
     
     @Override
     public void onCreate() {
         super.onCreate();
         res = ApplicationEx.getApp().getResources();
         nManager = (NotificationManager) getSystemService(
         		Context.NOTIFICATION_SERVICE);
         cancelReceiver = new CancelReceiver();
     }
     
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         super.onStartCommand(intent, flags, startId);
         if (ApplicationEx.getMsecs() < 0)
         	ApplicationEx.setMsecs(ApplicationEx.dbHelper.getTime());
         if (timer != null)
             timer.cancel();
         timer = new Timer(ApplicationEx.getMsecs(), 100, loop);
         isRunning = false;
         registerReceiver(cancelReceiver,
         		new IntentFilter(Constants.ACTION_STOP_TIMER));
         return Service.START_STICKY_COMPATIBILITY;
     }
     
     @Override
     public void onDestroy() {
     	unregisterReceiver(cancelReceiver);
         stopForeground(true);
         super.onDestroy();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return mBinder;
     }
     
     public void setUiCallback(UiCallback uiCallback) {
         this.uiCallback = uiCallback;
     }
     
     public void startTimer() {
         isRunning = true;
         if (timer != null) {
             timer.cancel();
             timer.start();
         }
         else {
             timer = new Timer(ApplicationEx.getMsecs(), 100, loop);
             timer.start();
         }
         makeText(ApplicationEx.getMsecs());
         showNotification(true);
     }
     
     private String text = "";
     private String ticker;
     
     private void makeText(int milliseconds) {
     	makeTicker(ApplicationEx.getMsecs());
         if (loop)
         	text = ticker + " Repeating";
         else
         	text = ticker;
     }
     
     private void makeTicker(int milliseconds) {
     	int mins = (int)((milliseconds-(milliseconds%60000))/60000);
         int secs = (int)((milliseconds%60000)/1000);
         ticker = Integer.toString(mins) + ":" + (secs >= 10 ?
         		Integer.toString(secs) : "0" + secs);
     }
     
     private void showNotification(boolean showTicker) {
         Intent notificationIntent = new Intent(ApplicationEx.getApp(),
         		ActivityMain.class);
         notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
         		Intent.FLAG_ACTIVITY_SINGLE_TOP);
         PendingIntent pendingIntent = PendingIntent.getActivity(
         		ApplicationEx.getApp(), 0, notificationIntent,
         		Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
         nBuilder = new NotificationCompat.Builder(ApplicationEx.getApp());
         nBuilder.setLargeIcon(BitmapFactory.decodeResource(res,
         		R.drawable.ic_launcher)).
     		setSmallIcon(R.drawable.ic_stat_notification).
     		setWhen(System.currentTimeMillis()).
     		setContentTitle(text).
     		setContentText("Buzz Timer").
     		setContentIntent(pendingIntent).
    		addAction(R.drawable.ic_menu_close_clear_cancel, "Cancel",
     				PendingIntent.getBroadcast(ApplicationEx.getApp(), 0,
     						new Intent(Constants.ACTION_STOP_TIMER), 0));
         if (showTicker)
         	nBuilder.setTicker(ticker);
         notification = nBuilder.build();
         startForeground(Constants.NOTIFICATION_RUNNING, notification);
     }
     
     public void stopTimer() {
         if (timer != null)
             timer.cancel();
         isRunning = false;
         stopForeground(true);
     }
     
     public boolean isTimerRunning() {
         return isRunning;
     }
     
     private final IBinder mBinder = new TimerBinder();
     
     public class TimerBinder extends Binder {
         public TimerService getService() {
             return TimerService.this;
         }
     }
     
     public void setTime(boolean loop) {
         this.loop = loop;
         if (timer != null)
             timer.cancel();
         timer = new Timer(ApplicationEx.getMsecs(), 100, loop);
         isRunning = false;
     }
     
     private int lastMillis = 0;
     
     private class Timer extends CountDownTimer {
         private boolean loop = false;
         
         public Timer(long millisInFuture, long countDownInterval,
                 boolean loop) {
             super(millisInFuture, countDownInterval);
             this.loop = loop;
             if (uiCallback != null)
                 uiCallback.updateTime((int)millisInFuture, false);
         }
 
         @Override
         public void onTick(long millisUntilFinished) {
             if (uiCallback != null)
                 uiCallback.updateTime((int)millisUntilFinished, false);
             makeText((int) millisUntilFinished);
         }
         
         @Override
         public void onFinish() {
             if (uiCallback != null)
                 uiCallback.updateTime((int)ApplicationEx.getMsecs(), false);
             Vibrator v = (Vibrator) ApplicationEx.getApp().getSystemService(
                     Context.VIBRATOR_SERVICE);
             v.vibrate(new long[] {0, 500, 500, 1000}, -1);
             if (loop) {
             	showNotification(true);
                 this.start();
             }
             else
             	stopForeground(true);
         }
     }
 }
