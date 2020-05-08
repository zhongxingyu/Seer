 /*
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as
  * published by the Free Software Foundation.
  */
 
 package com.squeezecontrol;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiManager;
 
 public class ServiceUtils {
    private static int TYPE_ETHERNET;
    private static boolean ethernetSupported;

    static {
        try {
            // ethernet connections available from API version 13
            TYPE_ETHERNET = ConnectivityManager.class.getField("TYPE_ETHERNET").getInt(null);
            ethernetSupported = true;
        } catch (IllegalAccessException e) {
            ethernetSupported = false;
        } catch (NoSuchFieldException e) {
            ethernetSupported = false;
        }
    }
 
 
     public static boolean bindToService(Context context,
                                         SqueezeServiceConnection serviceConnection) {
         serviceConnection.onServiceConnected(SqueezeService.getInstance());
         return true;
     }
 
     public static void unbindFromService(Context context) {
     }
 
     public static Dialog createWaitScreen(Context context) {
         ProgressDialog dialog = new ProgressDialog(context);
         dialog.setTitle("Connecting");
         dialog.setMessage("Please wait while connecting to SqueezeCenter...");
         dialog.setIndeterminate(true);
         dialog.setCancelable(true);
         return dialog;
 
     }
 
     /**
      * Are we connected to either WiFi or Ethernet?
      * 
      * TODO Figure out what TYPE_DUMMY is.
      * TODO Look for a better test for "are we on the emulator".  Maybe it
      * should be "is this a dev build"?
      * XXX??? Testing for emulator makes it hard to verify that the wifi
      * test is working.
      * 
      * @return true if the network is valid, or if we appear to be running
      * on the emulator.
      */
     public static boolean validNetworkAvailable(Context context) {
     	if ("sdk".equals(android.os.Build.PRODUCT)) {
     		// Running on the emulator
     		return true;
     	}
     	ConnectivityManager cmgr = 
     		(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo activeNetwork = cmgr.getActiveNetworkInfo();
         return null != activeNetwork &&
         	(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI ||
             (ethernetSupported && activeNetwork.getType() == TYPE_ETHERNET));
     }
     
     /**
      * If we're not connected to a valid network, complain and allow the user
      * to turn on WiFi.
      *
      * FIXME This should really allow the user to connect ethernet as well...
      */
     public static void requireValidNetworkOrFinish(final Activity context) {
         if (! validNetworkAvailable(context)) {
             AlertDialog.Builder dialog = new AlertDialog.Builder(context);
             dialog.setMessage(R.string.wlan_required);
             dialog.setTitle("WLAN required");
             dialog.setPositiveButton("Yes", new OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     ((WifiManager) context
                             .getSystemService(Context.WIFI_SERVICE))
                             .setWifiEnabled(true);
                 }
             });
             dialog.setNegativeButton("No", new OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     context.finish();
                 }
             });
             dialog.create().show();
         }
     }
 }
