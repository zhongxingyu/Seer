 package az.his.android;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.wifi.WifiManager;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import az.his.android.hisapi.ApiListener;
 import az.his.android.hisapi.ApiProvider;
 import az.his.android.persist.DbHelper;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class SyncService extends Service implements ApiListener {
     public static final String LOGTAG = "HIS-Sync-Service";
     private Timer timer;
     private TimerTask task;
     private DbHelper dbHelper;
     private static boolean started = false;
     private boolean oldWifiEnabled;
 
     private static SyncService self = null;
 
     public static boolean isStarted() {
         return started;
     }
 
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onCreate() {
         Log.d(LOGTAG, "Service created");
         started = true;
         self = this;
         setTimer();
     }
 
     private void setTimer() {
         SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 
         if (timer != null) {
             timer.cancel();
         }
         Log.d(LOGTAG, "Creating timer");
         timer = new Timer();
         task = new TimerTask() {
             @Override
             public void run() {
                 timer = null;
                 sync();
             }
         };
 
         if (!sharedPref.getBoolean("ck_sync", true)) {
             Log.i(LOGTAG, "Auto-sync disabled");
             if (timer != null) {
                 Log.i(LOGTAG, "Stopping timer");
                 timer.cancel();
                 timer = null;
             }
             return;
         }
 
         long syncStamp = sharedPref.getLong("time_sync", 0);
         Calendar cal = Calendar.getInstance();
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
 
         cal.add(Calendar.MILLISECOND, (int) syncStamp);
 
         Log.d(LOGTAG, "Setting timer to " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE));
 
         if (cal.before(Calendar.getInstance())) {
             Log.d(LOGTAG, "  ... of tomorrow");
             cal.add(Calendar.DATE, 1);
         }
 
         Date time = cal.getTime();
         timer.schedule(task, time);
         Log.d(LOGTAG, "Timer set to " + time);
     }
 
     private void sync() {
         Log.i(LOGTAG, "Synchronization started!");
         SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 
         if (dbHelper == null) dbHelper = new DbHelper(getApplicationContext());
         if (dbHelper.getTransactionNum() < 1) {
             setTimer();
             return;
         }
 
         WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
         oldWifiEnabled = wifiManager.isWifiEnabled();
         if (!oldWifiEnabled) {
             Log.i(LOGTAG, "Turning WiFi on...");
             wifiManager.setWifiEnabled(true);
             try {
                 Thread.sleep(30000, 0);
             } catch (InterruptedException ignored) {
             }
         }
 
         String ssid = wifiManager.getConnectionInfo().getSSID();
 
         if (ssid == null || ssid.equals("")) {
             Log.i(LOGTAG, "No WiFi connection.");
             // TODO Strings to res
             makeNotification("Синхронизация не удалась", "Нет WiFi соединения");
             resetTimer();
             return;
         }
 
        ssid = ssid.replaceAll("^\"|\"$", "");
 
         String confSsid = sharedPref.getString("str_ssid", null);
         if (confSsid != null && !confSsid.equals("") && !confSsid.equals(ssid)) {
             Log.i(LOGTAG, "Not home WiFi network: " + ssid);
             // TODO Strings to res
             makeNotification("Синхронизация не удалась", "Не та сеть. " + ssid + ", а должна быть " + confSsid);
             resetTimer();
             return;
         }
 
         ApiProvider.postTransactions(this, this, sharedPref.getInt("int_userid", -1), dbHelper.getTransactions(), false);
     }
 
     @Override
     public void handleApiResult(Object result) {
         if (result == Boolean.TRUE) {
             // TODO Strings to res
             makeNotification("Синхронизация удалась", "Транзакции отправлены на сервер");
 
             dbHelper.cleanTransactions();
         } else {
             makeNotification("Синхронизация не удалась", "Сервер не доступен");
         }
 
         resetTimer();
     }
 
     private void makeNotification(String tickerText, String contentText) {
         PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, EnterTransactionActivity.class), 0);
         // TODO correct intent
 
         Notification notification = new Notification(
                 android.R.drawable.stat_notify_sync,
                 tickerText,
                 System.currentTimeMillis());
 
         notification.setLatestEventInfo(this, "HIS Mobile", tickerText + ":" + contentText, intent);
 
         NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         notificationManager.notify(1, notification);
     }
 
     private void resetTimer() {
         if (!oldWifiEnabled) {
             Log.i(LOGTAG, "Turning WiFi off.");
             ((WifiManager) this.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(false);
         }
 
         setTimer();
         dbHelper = null;
     }
 
     public static void checkState(Context context) {
         if (isStarted()) self.setTimer();
         else context.startService(new Intent(context, SyncService.class));
     }
 }
