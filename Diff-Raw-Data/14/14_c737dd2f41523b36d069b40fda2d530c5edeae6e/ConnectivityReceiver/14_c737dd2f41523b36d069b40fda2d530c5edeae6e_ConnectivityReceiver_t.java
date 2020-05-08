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
 
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Log;
 
 import com.commonsware.cwac.wakeful.WakefulIntentService;
 
 public class ConnectivityReceiver extends BroadcastReceiver {
     private static final String TAG = "ConnectivityReceiver";
 
     private static final boolean mNoLog = true;
 
     /* (non-Javadoc)
      * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
      */
    @SuppressWarnings("unused")
     @Override
     public void onReceive(Context context, Intent intent) {
         if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
             if (mNoLog == false) Log.d(TAG, "ConnectivityReceiver invoked...");
             boolean noConnectivity = intent.getBooleanExtra(
                         ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
             if (!noConnectivity) {
                 ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                 NetworkInfo netInfo = cm.getActiveNetworkInfo();
                 // only when connected or while connecting...
                 if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                    // if we have mobile or wifi connectivity...
                    if ((netInfo.getType() == ConnectivityManager.TYPE_MOBILE)
                       || (netInfo.getType() == ConnectivityManager.TYPE_WIFI)) {
                       if (mNoLog == false) Log.d(TAG, "We have internet, start update check and disable receiver!");
                       // Start service with wakelock by using WakefulIntentService
                       Intent backgroundIntent = new Intent(context, UpdateService.class);
                       WakefulIntentService.sendWakefulWork(context, backgroundIntent);
                       // disable receiver after we started the service
                       disableReceiver(context);
                    }
                 }
             }
         }
 
     }
 
     /**
      * Enables ConnectivityReceiver
      *
      * @param context
      */
     public static void enableReceiver(Context context) {
         ComponentName component = new ComponentName(context, ConnectivityReceiver.class);
         context.getPackageManager().setComponentEnabledSetting(component,
                 PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
     }
 
     /**
      * Disables ConnectivityReceiver
      *
      * @param context
      */
     public static void disableReceiver(Context context) {
         ComponentName component = new ComponentName(context, ConnectivityReceiver.class);
         context.getPackageManager().setComponentEnabledSetting(component,
                 PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
     }
 }
