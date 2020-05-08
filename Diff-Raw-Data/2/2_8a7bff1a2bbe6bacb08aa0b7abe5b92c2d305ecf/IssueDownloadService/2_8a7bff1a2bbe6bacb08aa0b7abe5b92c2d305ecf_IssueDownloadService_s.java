 /*
  * Der Bund ePaper Downloader - App to download ePaper issues of the Der Bund newspaper
  * Copyright (C) 2013 Adrian Gygax
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see {http://www.gnu.org/licenses/}.
  */
 
 package com.github.notizklotz.derbunddownloader.download;
 
 import android.app.*;
 import android.content.*;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.net.wifi.WifiManager;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import com.github.notizklotz.derbunddownloader.BuildConfig;
 import com.github.notizklotz.derbunddownloader.DebugConstants;
 import com.github.notizklotz.derbunddownloader.R;
 import com.github.notizklotz.derbunddownloader.common.DateFormatterUtils;
 import com.github.notizklotz.derbunddownloader.main.MainActivity;
 import com.github.notizklotz.derbunddownloader.settings.Settings;
 import org.springframework.util.Assert;
 import org.springframework.util.LinkedMultiValueMap;
 import org.springframework.web.client.RestClientException;
 import org.springframework.web.client.RestTemplate;
 
 import java.util.concurrent.CountDownLatch;
 
 public class IssueDownloadService extends IntentService {
 
     private static final int WIFI_RECHECK_WAIT_MILLIS = 5 * 1000;
     private static final int WIFI_CHECK_MAX_MILLIS = 30 * 1000;
 
     public static final String EXTRA_DAY = "day";
     public static final String EXTRA_MONTH = "month";
     public static final String EXTRA_YEAR = "year";
     private static final String LOG_TAG = IssueDownloadService.class.getSimpleName();
 
     public IssueDownloadService() {
         super("IssueDownloadService");
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
         if (!(intent.hasExtra(EXTRA_DAY) && intent.hasExtra(EXTRA_MONTH) && intent.hasExtra(EXTRA_YEAR))) {
             throw new IllegalArgumentException("Intent is missing extras");
         }
 
         Log.d(LOG_TAG, "Handling download intent");
 
         WifiManager.WifiLock myWifiLock;
         WifiManager wm;
         //noinspection UnusedAssignment
         boolean connected = false;
         //noinspection PointlessBooleanExpression,ConstantConditions
         if(!DebugConstants.DISABLE_WIFI_ENFORCEMENT) {
             Log.d(LOG_TAG, "Making sure Wifi is enabled");
             wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
             //WIFI_MODE_FULL was not enough on Xperia Tablet Z Android 4.2 to reconnect to the AP if Wifi was enabled but connection
             //was lost
             myWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "IssueDownloadWifilock");
             myWifiLock.acquire();
 
             //Wait for Wifi coming up
             long firstCheckMillis = System.currentTimeMillis();
             ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
             if(!wm.isWifiEnabled()) {
                notifyUser(getText(R.string.download_connection_failed), getText(R.string.download_connection_failed_text), R.drawable.ic_stat_error);
             } else {
                 do {
                     NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                     assert networkInfo != null;
                     connected = networkInfo.isConnected();
 
                     if (!connected) {
                         Log.d(LOG_TAG, "Wifi connection is not yet ready. Wait and recheck");
 
                         if(System.currentTimeMillis() - firstCheckMillis > WIFI_CHECK_MAX_MILLIS) {
                             notifyUser(getText(R.string.download_connection_failed), getText(R.string.download_connection_failed_text), R.drawable.ic_stat_error);
                             break;
                         }
 
                         try {
                             Thread.sleep(WIFI_RECHECK_WAIT_MILLIS);
                         } catch (InterruptedException e) {
                             Log.wtf(LOG_TAG, "Interrupted while waiting for Wifi connection", e);
                         }
                     }
                 } while (!connected);
             }
         }
 
          Log.d(LOG_TAG, "Connected: " + connected);
 
         //noinspection PointlessBooleanExpression
         if(connected || DebugConstants.DISABLE_WIFI_ENFORCEMENT) {
             if (!checkUserAccount()) {
                 notifyUser(getText(R.string.download_login_failed), getText(R.string.download_login_failed_text), R.drawable.ic_stat_error);
             } else {
                 //Download
                 final CountDownLatch downloadDoneSignal = new CountDownLatch(1);
                 final DownloadCompletedBroadcastReceiver receiver = new DownloadCompletedBroadcastReceiver(downloadDoneSignal);
                 registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                 String title = startDownload(this, intent.getIntExtra(EXTRA_DAY, 0), intent.getIntExtra(EXTRA_MONTH, 0), intent.getIntExtra(EXTRA_YEAR, 0));
 
                 try {
                     downloadDoneSignal.await();
                     notifyUser(title, getString(R.string.download_completed), R.drawable.ic_stat_av_download);
                 } catch (InterruptedException e) {
                     Log.wtf(LOG_TAG, "Interrupted while waiting for the downloadDoneSignal");
                 }
                 unregisterReceiver(receiver);
             }
         }
 
         //noinspection PointlessBooleanExpression,ConstantConditions
         if(!DebugConstants.DISABLE_WIFI_ENFORCEMENT) {
             myWifiLock.release();
         }
 
         AutomaticIssueDownloadAlarmReceiver.completeWakefulIntent(intent);
     }
 
     private void notifyUser(CharSequence contentTitle, CharSequence contentText, int icon) {
         Notification.Builder mBuilder =
                 new Notification.Builder(getApplicationContext())
                         .setSmallIcon(icon)
                         .setContentTitle(contentTitle)
                         .setContentText(contentText)
                         .setTicker(contentTitle)
                         .setAutoCancel(true);
 
         //http://developer.android.com/guide/topics/ui/notifiers/notifications.html
         // The stack builder object will contain an artificial back stack for thestarted Activity.
         // This ensures that navigating backward from the Activity leads out of your application to the Home screen.
         mBuilder.setContentIntent(android.support.v4.app.TaskStackBuilder.create(getApplicationContext()).
                 addParentStack(MainActivity.class).
                 addNextIntent(new Intent(getApplicationContext(), MainActivity.class)).
                 getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));
 
         NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         //noinspection deprecation
         mNotifyMgr.notify(1, mBuilder.getNotification());
     }
 
     private String startDownload(Context context, int day, int month, int year) {
         DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
 
         String url = "http://epaper.derbund.ch/getFile.php?ausgabe=" + DateFormatterUtils.toDDMMYYYYString(day, month, year);
         String title = "Der Bund ePaper " + DateFormatterUtils.toDD_MM_YYYYString(day, month, year);
         DownloadManager.Request pdfDownloadRequest = new DownloadManager.Request(Uri.parse(url))
                 .setTitle(title)
                 .setDescription(DateFormatterUtils.toDD_MM_YYYYString(day, month, year))
                 .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                 .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, title + ".pdf");
         //noinspection PointlessBooleanExpression,ConstantConditions
         if(!DebugConstants.DISABLE_WIFI_ENFORCEMENT) {
             pdfDownloadRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
         }
         downloadManager.enqueue(pdfDownloadRequest);
 
         if(BuildConfig.DEBUG) {
             Log.d(LOG_TAG, "Download enqueued");
         }
         return title;
     }
 
     @SuppressWarnings("BooleanMethodIsAlwaysInverted")
     private boolean checkUserAccount() {
         if(BuildConfig.DEBUG) {
             Log.d(LOG_TAG, "Checking user account validity");
         }
 
         try {
             final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
             final String username = sharedPref.getString(Settings.KEY_USERNAME, "");
             final String password = sharedPref.getString(Settings.KEY_PASSWORD, "");
 
             RestTemplate restTemplate = new RestTemplate(true);
             LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
             form.add("user", username);
             form.add("password", password);
             form.add("dologin", "1");
             form.add("t", "");
 
             String response = restTemplate.postForObject("http://epaper.derbund.ch", form, String.class);
             boolean loginSuccessful = response.contains("flashcontent");
             Log.d(LOG_TAG, "Login successful? " + loginSuccessful);
             return loginSuccessful;
         } catch (RestClientException e) {
             Log.e(LOG_TAG, "Error while trying to login", e);
             return false;
         }
     }
 
     private static class DownloadCompletedBroadcastReceiver extends BroadcastReceiver {
 
         private final CountDownLatch downloadDoneSignal;
 
         private DownloadCompletedBroadcastReceiver(CountDownLatch downloadDoneSignal) {
             Assert.notNull(downloadDoneSignal);
             this.downloadDoneSignal = downloadDoneSignal;
         }
 
         @Override
         public void onReceive(Context context, Intent intent) {
             downloadDoneSignal.countDown();
         }
     }
 }
