 package jp.srgtndr.akaiosorani.android.cartain.controller;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.location.LocationManager;
 import android.os.Build;
 import android.provider.Settings;
 import android.util.Log;
 
 public class GpsController {
    // for judgement API level
     private static final int GINGERBREAD = 9;
 
     // LocationManager.PROVIDERS_CHANGED_ACTION defined from API 9 (2.3)
     private static final String PROVIDERS_CHANGED = "android.location.PROVIDERS_CHANGED";
 
    // check interval (ms)
     private static final int STATUS_CHECK_INTERVAL = 2000;
 
     private static Timer timer;
 
     private static boolean enabledGps = false;
 
     public static LocationManager getManager(Context context)
     {
         return (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
     }
 
     public static void callSetting(Context context)
     {
         context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
     }
     public static boolean isGpsDevice(Context context) 
     {
         LocationManager manager = getManager(context);
         return manager.getAllProviders().contains(LocationManager.GPS_PROVIDER);
     }
 
     public static boolean isGpsEnabled(Context context)
     {
         LocationManager manager = getManager(context);
         return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
     }
 
     public static IntentFilter getFilter()
     {
         return new IntentFilter(PROVIDERS_CHANGED);
     }
 
     public static void startStatusCheck(final Context context)
     {
         if(Build.VERSION.SDK_INT >= GINGERBREAD || !isGpsDevice(context)) {
             return;
         }
         synchronized (PROVIDERS_CHANGED) {
            if (timer != null) 
            {
                return;
            }
             enabledGps = isGpsEnabled(context);
             // start timer for Gps Enabled/Disabled
             timer = new Timer(true);
             timer.scheduleAtFixedRate(new TimerTask() {
                 @Override
                 public void run() {
                     boolean current = isGpsEnabled(context);
                     Log.d("cartain", String.format("schedule tasked %b %b", enabledGps, current));
                     if (current == enabledGps) 
                     {
                         return;
                     }
                     Log.d("cartain", "Gps status changed!!");
                     Intent broadcast = new Intent(PROVIDERS_CHANGED);
                     context.sendBroadcast(broadcast);
                     enabledGps = !enabledGps;
                 }
             }, 0, STATUS_CHECK_INTERVAL);
         }
     }
 
     public static void stopStatusCheck()
     {
         synchronized (PROVIDERS_CHANGED) {
             if (timer == null)
             {
                 return;
             }
            Log.d("cartain", "timer canceled");
             timer.cancel();
             timer = null;
         }
     }
 }
