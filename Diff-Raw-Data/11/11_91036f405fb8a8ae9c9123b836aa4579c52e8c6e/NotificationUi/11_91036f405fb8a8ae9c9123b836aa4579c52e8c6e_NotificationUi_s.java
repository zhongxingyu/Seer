 package com.github.tdudziak.gps_lock_lock;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 
 class NotificationUi
 {
     private static final String TAG = "NotificationUi";
     private static final int NOTIFICATION_ID = 1;
 
     private Notification mNotification;
     private NotificationManager mNotificationManager;
     private PendingIntent mIntent;
     private BroadcastReceiver mReceiver;
     private Service mService;
     private boolean mServiceIsForeground = false;
 
     public NotificationUi(Service service) {
         mService = service;
 
         int icon = R.drawable.ic_stat_notification;
         CharSequence ticker = mService.getText(R.string.notification_ticker);
         mNotificationManager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
         Intent notificationIntent = new Intent(mService, ControlActivity.class);
         mIntent = PendingIntent.getActivity(mService, 0, notificationIntent, 0);
         long now = System.currentTimeMillis();
         mNotification = new Notification(icon, ticker, now);
 
         mReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 int remaining = intent.getIntExtra(LockService.EXTRA_TIME_LEFT, -1);
                 int last_fix = intent.getIntExtra(LockService.EXTRA_LAST_FIX, -1);
                 redraw(remaining, last_fix);
             }
         };
 
         IntentFilter filter = new IntentFilter(LockService.ACTION_UI_UPDATE);
         LocalBroadcastManager.getInstance(mService).registerReceiver(mReceiver, filter);
     }
 
     public void enable() {
         IntentFilter filter = new IntentFilter(LockService.ACTION_UI_UPDATE);
         LocalBroadcastManager.getInstance(mService).registerReceiver(mReceiver, filter);
         Log.i(TAG, "enable()");
     }
 
     public void disable() {
         LocalBroadcastManager.getInstance(mService).unregisterReceiver(mReceiver);
         mServiceIsForeground = false;
         Log.i(TAG, "disable()");
     }
 
     private void redraw(int remaining, int last_fix) {
         String title, text;
 
         if(remaining <= 0) {
             // This *must* be the last message; hide the notification.
             mNotificationManager.cancel(NOTIFICATION_ID);
             return;
         }
 
         if(last_fix < 0) {
             title = mService.getString(R.string.notification_title_nofix);
         } else if(last_fix > 0) {
             title = String.format(mService.getString(R.string.notification_title), last_fix);
         } else {
             title = mService.getString(R.string.notification_title_1minfix);
         }
 
         text = String.format(mService.getString(R.string.notification_text), remaining);
         mNotification.setLatestEventInfo(mService, title, text, mIntent);
 
         if(mServiceIsForeground) {
             mNotificationManager.notify(NOTIFICATION_ID, mNotification);
         } else {
             Log.v(TAG, "First update after enable(); calling startForeground()");
             mService.startForeground(NOTIFICATION_ID, mNotification);
             mServiceIsForeground = true;
         }
     }
 }
