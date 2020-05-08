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
 
 package com.github.notizklotz.derbunddownloader;
 
 import android.app.DownloadManager;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.Uri;
 import android.net.wifi.WifiManager;
 import android.os.IBinder;
 
 public class IssueDownloadService extends Service {
 
     public static final String DAY = "day";
     public static final String MONTH = "month";
     public static final String YEAR = "year";
 
     private Intent intent;
     private WifiManager.WifiLock myWifiLock;
 
     private final AutomaticIssueDownloadCompletedReceiver automaticIssueDownloadCompletedReceiver = new AutomaticIssueDownloadCompletedReceiver();
 
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         this.intent = intent;
 
         WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
         myWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "IssueDownloadWifilock");
         myWifiLock.acquire();
 
         registerReceiver(automaticIssueDownloadCompletedReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
 
         if (!(intent.hasExtra(DAY) && intent.hasExtra(MONTH) && intent.hasExtra(YEAR))) {
             throw new IllegalArgumentException("Intent is missing extras");
         }
 
         startDownload(this, intent.getIntExtra(DAY, 0), intent.getIntExtra(MONTH, 0), intent.getIntExtra(YEAR, 0));
 
         return START_STICKY;
     }
 
     public static long startDownload(Context context, int day, int month, int year) {
         String dayString = String.format("%02d", day);
         String monthString = String.format("%02d", month);
         String yearString = Integer.toString(year);
 
        String url = "http://epaper.derbund.ch/getAll.asp?d=" + dayString + monthString + yearString;
 
         DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
         DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url)).
                 setTitle("derbund-" + yearString + monthString + dayString).
                 setDescription("Der Bund ePaper " + dayString + "." + monthString + "." + yearString).
                 setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED).
                 setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
         return downloadManager.enqueue(request);
     }
 
     @Override
     public void onDestroy() {
         if (myWifiLock != null) {
             myWifiLock.release();
         }
 
         unregisterReceiver(automaticIssueDownloadCompletedReceiver);
 
         if (intent != null) {
             AutomaticIssueDownloadAlarmReceiver.completeWakefulIntent(intent);
         }
     }
 }
