 /*
  *  This file is a part of GPS Lock-Lock Android application.
  *  Copyright (C) 2011 Tomasz Dudziak
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
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.github.tdudziak.gps_lock_lock;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 
 public class LockService extends Service implements LocationListener
 {
     private final String TAG = "LockService";
     private final long GPS_MIN_TIME = 0; // 20000; FIXME FIXME
     private static final int HANDLER_WHAT = 0; // "what" value used in handler messages
 
     public final static String ACTION_SHUTDOWN = "com.github.tdudziak.gps_lock_lock.LockService.ACTION_SHUTDOWN";
     public final static String ACTION_RESTART = "com.github.tdudziak.gps_lock_lock.LockService.ACTION_RESTART";
     public final static String ACTION_UI_UPDATE = "com.github.tdudziak.gps_lock_lock.LockService.ACTION_UI_UPDATE";
 
     public static final String EXTRA_TIME_LEFT = "com.github.tdudziak.gps_lock_lock.LockService.EXTRA_TIME_LEFT";
     public static final String EXTRA_LAST_FIX = "com.github.tdudziak.gps_lock_lock.LockService.EXTRA_LAST_FIX";
 
     public static final long LOCK_LOCK_MINUTES = 5;
 
     private boolean mIsActive = false;
     private long mStartTime;
     private long mLastFixTime = 0;
     private LocationManager mLocationManager;
     private NotificationUi mNotificationUi;
 
     private Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             super.handleMessage(msg);
 
             if(!mIsActive) {
                 Log.e(TAG, "handleMessage() called with mIsActive == false");
                 return;
             }
 
             // Re-request location updates every minute or so. Some phones seem to stop searching for
             // GPS fix after a while if no satellites are visible (probably to conserve energy) but this
             // behavior doesn't seem to be documented anywhere.
             mLocationManager.removeUpdates(LockService.this);
             mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME, 0, LockService.this);
 
             if(getRemainingTime() <= 0) {
                 disable();
             } else {
                 broadcastUiUpdateMessage(false);
 
                 // Schedule another update at the beginning of next minute + 0.5 second.
                 long min_elapsed = (System.currentTimeMillis() - mStartTime) / (60*1000);
                 long next_update_time = mStartTime + 60*1000*(min_elapsed + 1) + 500;
                 long next_update_delta = next_update_time - System.currentTimeMillis();
                 removeMessages(HANDLER_WHAT);
                 sendEmptyMessageDelayed(HANDLER_WHAT, next_update_delta);
             }
         }
     };
 
     private long getRemainingTime() {
         long minutes = (System.currentTimeMillis() - mStartTime)/(1000*60);
         return LOCK_LOCK_MINUTES - minutes;
     }
 
     private void broadcastUiUpdateMessage(boolean last_message) {
         long remaining = getRemainingTime();
         long last_fix = (System.currentTimeMillis() - mLastFixTime)/(1000*60);
 
         if(mLastFixTime == 0) last_fix = -1; // special value meaning "never"
         if(remaining <= 0 || last_message) remaining = 0; // special value meaning "no time left"
 
         Intent intent = new Intent(ACTION_UI_UPDATE);
         intent.putExtra(EXTRA_TIME_LEFT, (int) remaining);
         intent.putExtra(EXTRA_LAST_FIX, (int) last_fix);
 
         Log.v(TAG, String.format("broadcastUiUpdateMessage(); EXTRA_TIME_LEFT=%d; EXTRA_LAST_FIX=%d", remaining, last_fix));
 
         LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(LockService.this);
         lbm.sendBroadcast(intent);
     }
 
     public void requestUiUpdate() {
         mHandler.removeMessages(HANDLER_WHAT);
         mHandler.sendEmptyMessage(HANDLER_WHAT);
     }
 
     @Override
     public void onCreate() {
         mNotificationUi = new NotificationUi(this);
     }
 
     @Override
     public void onDestroy() {
         if(mIsActive) {
             Log.e(TAG, "Destroying despite ongoing tracking.");
             disable();
         }
     }
 
     private void enable() {
         mStartTime = System.currentTimeMillis();
         mNotificationUi.enable(); // setup UI
         requestUiUpdate(); // start broadcasting UI update intents
 
         // Setup GPS listening.
         mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME, 0, this);
 
         mIsActive = true;
 
         Log.i(TAG, "enable()");
     }
 
     private void restart() {
         mStartTime = System.currentTimeMillis();
         requestUiUpdate(); // synchronize broadcasts
         Log.i(TAG, "restart()");
     }
 
     private void disable() {
         broadcastUiUpdateMessage(true);
         mHandler.removeMessages(HANDLER_WHAT);
         mLocationManager.removeUpdates(this);
         stopForeground(true);
         mNotificationUi.disable();
         mIsActive = false;
         stopSelf();
         Log.i(TAG, "disable()");
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         if(intent == null) {
             Log.e(TAG, "onStartCommand() with null intent -- this shouldn't happen");
             return START_NOT_STICKY;
         }
 
         if(ACTION_SHUTDOWN.equals(intent.getAction())) {
             if(mIsActive) {
                 disable();
             } else {
                 Log.e(TAG, "ACTION_SHUTDOWN intent received by already inactive service");
                 enable();
             }
             return START_NOT_STICKY;
         }
 
         if(ACTION_RESTART.equals(intent.getAction())) {
             if(mIsActive) {
                 restart();
             } else {
                 Log.e(TAG, "ACTION_RESTART intent received by inactive service");
             }
             return START_NOT_STICKY;
         }
 
         if(ACTION_UI_UPDATE.equals(intent.getAction())) {
             if(mIsActive) {
                 requestUiUpdate();
             } else {
                 Log.e(TAG, "ACTION_UI_UPDATE intent received by inactive service");
             }
             return START_NOT_STICKY;
         }
 
         // by default just start the service
         if(mIsActive) {
             Log.e(TAG, "Trying to start an already active service.");
         } else {
             enable();
         }
         return START_NOT_STICKY;
     }
 
     @Override
     public void onLocationChanged(Location location) {
         Log.v(TAG, "onLocationChanged()");
         mLastFixTime = System.currentTimeMillis();
     }
 
     @Override
     public void onProviderDisabled(String provider) {
         // GPS explicitly turned off. The user obviously does not want a GPS fix.
         disable();
     }
 
     @Override
     public void onProviderEnabled(String provider) {
         // This probably should never happen. If it does, ignore.
         Log.v(TAG, "onProviderEnabled()");
     }
 
     @Override
     public void onStatusChanged(String provider, int status, Bundle extras) {
         Log.v(TAG, "onStatusChanged(); status=" + status);
         requestUiUpdate();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null; // no support for binding
     }
 }
