 package com.chess.saldo;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.SystemClock;
 import android.util.Log;
 
/**
 * Created by bjorncs on 08.06.13.
 */
 public class ConnectivityBroadcastReceiver extends BroadcastReceiver {
 
     private volatile static PendingIntent alarmIntent;
 
     public static void setUpdateAlarm(Context context) {
         int updateFreq = new SettingsManager(context).getUpdateFrequency();
 
         AlarmManager amng = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
         if (alarmIntent != null) {
             Log.d("CHESS_SALDO", "Alarm was already registered, canceling the old one");
             amng.cancel(alarmIntent);
         }
 
         Intent intent = new Intent(context, UpdateService.class);
         intent.putExtra(UpdateService.SHOW_TOAST, false);
         alarmIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
         Log.d("CHESS_SALDO", "Registering an alarm with update frequency: " + updateFreq);
         amng.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + updateFreq, updateFreq, alarmIntent);
     }
 
     @Override
     public void onReceive(Context context, Intent intent) {
         if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
             boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
             boolean shouldUpdate = shouldUpdate(context);
             Log.d("CHESS_SALDO", String.format("ConnectivityManager.CONNECTIVITY_ACTION: noConnectivity=%b, shouldUpdate=%b",
                     noConnectivity, shouldUpdate));
             if (!noConnectivity && shouldUpdate) {
                 ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                 NetworkInfo info = conMgr.getActiveNetworkInfo();
                 Log.d("CHESS_SALDO",
                         info == null ? "NetworkInfo = null" :
                                 String.format("NetworkInfo: type=%s, connectedOrConnecting=%b, connected=%b",
                                         info.getTypeName(), info.isConnectedOrConnecting(), info.isConnected()));
 
                 if (info != null && info.isConnectedOrConnecting()) {
                     Log.d("CHESS_SALDO", "Invoking update service from wifi listener");
                     Intent newIntent = new Intent(context, UpdateService.class);
                     newIntent.putExtra(UpdateService.SHOW_TOAST, false);
                     context.startService(newIntent);
                 }
 
             }
         }
     }
 
     private boolean shouldUpdate(Context context) {
         SettingsManager mgr = new SettingsManager(context);
         long current = System.currentTimeMillis();
         long last = mgr.getLastUpdate();
         long difference = current - last;
         int updateFreq = mgr.getUpdateFrequency();
         boolean widgetEnabled = existsAnyWidget(context);
         boolean result = widgetEnabled && difference >= updateFreq;
         Log.d("CHESS_SALDO",
                 String.format("ShouldUpdate() = %b (difference=%d, updateFreq=%d, widgetEnabled=%b)",
                         result, difference, updateFreq, widgetEnabled));
         return result;
     }
 
     private static boolean existsAnyWidget(Context context) {
         return WidgetProviders.hasWidgets(context);
     }
 
 }
