 /*=========================================================================
  *
  *  PROJECT:  SlimRoms
  *            Team Slimroms (http://www.slimroms.net)
  *
  *  COPYRIGHT Copyright (C) 2013 Slimroms http://www.slimroms.net
  *            All rights reserved
  *
  *  LICENSE   http://www.gnu.org/licenses/gpl-2.0.html GNU/GPL
  *
  *  AUTHORS:     fronti90, mnazim, tchaari, kufikugel
  *  DESCRIPTION: SlimOTA keeps our rom up to date
  *
  *=========================================================================
  */
 
 package com.slim.ota.updater;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Log;
 
 import com.commonsware.cwac.wakeful.WakefulIntentService;
 import com.commonsware.cwac.wakeful.WakefulIntentService.AlarmListener;
 import com.slim.ota.settings.Settings;
 
 import java.util.Calendar;
 
 public class UpdateListener implements AlarmListener {
     private static final String TAG = "UpdateListener";
 
     private static final String LAST_INTERVAL = "lastInterval";
 
     public static long interval = AlarmManager.INTERVAL_HALF_DAY;
 
     private static boolean mNoLog = true;
 
     /* (non-Javadoc)
      * @see com.commonsware.cwac.wakeful.WakefulIntentService.AlarmListener#scheduleAlarms(android.app.AlarmManager, android.app.PendingIntent, android.content.Context)
      */
     @Override
     public void scheduleAlarms(AlarmManager mgr, PendingIntent pi, Context ctx) {
         Calendar calendar = Calendar.getInstance();
         calendar.setTimeInMillis(System.currentTimeMillis());
 
         SharedPreferences prefs = ctx.getSharedPreferences(LAST_INTERVAL, 0);
         long value = prefs.getLong(LAST_INTERVAL,0);
         if (value == 0) {
             interval = AlarmManager.INTERVAL_HALF_DAY;
             prefs.edit().putLong(LAST_INTERVAL, interval).apply();
        } else if (value != 1) {
             interval = value;
         }
 
         mgr.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                     interval, pi);
     }
 
     /* (non-Javadoc)
      * @see com.commonsware.cwac.wakeful.WakefulIntentService.AlarmListener#sendWakefulWork(android.content.Context)
      */
     @Override
     public void sendWakefulWork(Context context) {
         ConnectivityManager cm = (ConnectivityManager) context
                 .getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo netInfo = cm.getActiveNetworkInfo();
         if (mNoLog == false) Log.d(TAG, "sendWakefulWork called!");
         // only when connected or while connecting...
         if (netInfo != null && netInfo.isConnectedOrConnecting()) {
             // if we have mobile or wifi connectivity...
             if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE
                     || netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                 if (mNoLog == false) Log.d(TAG, "We have internet, start update check directly now!");
                 Intent backgroundIntent = new Intent(context, UpdateService.class);
                 WakefulIntentService.sendWakefulWork(context, backgroundIntent);
             } else {
                 if (mNoLog == false) Log.d(TAG, "We have no internet, enable ConnectivityReceiver!");
                 // enable receiver to schedule update when internet is available!
                 ConnectivityReceiver.enableReceiver(context);
             }
         } else {
             if (mNoLog == false) Log.d(TAG, "We have no internet, enable ConnectivityReceiver!");
             // enable receiver to schedule update when internet is available!
             ConnectivityReceiver.enableReceiver(context);
         }
     }
 
     /* (non-Javadoc)
      * @see com.commonsware.cwac.wakeful.WakefulIntentService.AlarmListener#getMaxAge()
      */
     @Override
     public long getMaxAge() {
         // TODO Auto-generated method stub
         return (AlarmManager.INTERVAL_DAY + 60 * 1000);
     }
 
 }
