 /*
  * Copyright (C) 2011 Vodafone Group Duesseldorf
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.vodafone.twitter.service;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.HashMap;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 
 import android.util.Config;
 import android.util.Log;
 import android.app.Service;
 import android.app.IntentService;
 import android.app.PendingIntent;
 import android.app.Notification;
 import android.app.AlarmManager;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.BroadcastReceiver;
 import android.widget.RemoteViews;
 import android.text.format.Time;
 import android.text.Html;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.wifi.WifiManager;
 import android.net.Uri;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.Binder;
 import android.os.IBinder;
 import android.os.BatteryManager;
 import android.os.PowerManager;
 import android.os.Vibrator;
 
 import twitter4j.*;
 import twitter4j.conf.*;
 import twitter4j.auth.*;
 
 import org.timur.glticker.EntryTopic;
 import org.timur.glticker.TickerServiceAbstract;
 
 public class TwitterService extends TickerServiceAbstract {
 
   static final String LOGTAG = "TwitterService";
   static final String PREFS_NAME = "com.vodafone.twitterservice";
 
   static boolean unpluggedWifiWakeupActivated = false;
   static Boolean wifiEnabled = null;
   static Boolean powerPlugged = null;
  static java.util.LinkedList<EntryTopic> messageList = new java.util.LinkedList<EntryTopic>();
   static int totalNumberOfQueuedMessages = 0;
   static volatile int numberOfQueuedMessagesSinceLastClientActivity = 0;
   static volatile boolean activityPaused = false;
   static int maxQueueMessages = 40;
   static PowerManager powerManager = null;
   static PowerManager.WakeLock wakeLockNoScreen = null;
   static Vibrator vibrator = null;
   static SharedPreferences preferences = null;
   static PendingIntent pendingIntent = null;
   static AlarmManager alarmManager = null;
   static BroadcastReceiver onWifiChanged = null;
   static BroadcastReceiver onBatteryChanged = null;
   static Intent tickerNewEntryBroadcastIntent = null;
   static volatile ConnectThread connectThread = null;
   static twitter4j.Twitter twitter = null;
   static RequestToken requestToken = null;
   static AccessToken accessToken = null;
   static TwitterStream twitterStream = null;
   static UserStreamListener statusListener = null;
   static volatile String errMsg = null;
   static volatile int totalNumberOfBrodcastsSend = 0;
   static volatile ActivityUpdateThread activityUpdateThread = null;
   static volatile boolean activityUpdatePending = false;
   static boolean fetchHomeTimelineDone = false;
   static boolean linkifyMessages = false;
   //static UserStream userStream = null;
   private String messageLink = null;
 
   @Override 
   public void onCreate() {
     super.onCreate(); 
     if(Config.LOGD) Log.i(LOGTAG, "onCreate()");
     powerManager = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
     wakeLockNoScreen = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOGTAG);
     preferences = getSharedPreferences(PREFS_NAME, MODE_WORLD_WRITEABLE);
 
     // retrieve wifiEnabled state
     ConnectivityManager connectivityManager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
     if(connectivityManager!=null) {
       int networkType=0; // mobile=0, wifi=1
       NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
       if(networkInfo!=null) {
         boolean newWifiEnabled = false;
         if(networkInfo.getType()==ConnectivityManager.TYPE_WIFI)
           newWifiEnabled = true;
         if(wifiEnabled==null || newWifiEnabled!=wifiEnabled) {
           wifiEnabled = new Boolean(newWifiEnabled);
           if(wifiEnabled) {
             if(Config.LOGD) Log.i(LOGTAG, "onCreate() WIFI_STATE_ENABLED");
           }
           else {
             if(Config.LOGD) Log.i(LOGTAG, "onCreate() WIFI_STATE_DISABLED");
           }
           checkRunningOnUnpluggedWifi();
         }
       }
     }
 
     // we need event handlers for onWifiChanged and onBatteryChanged
     // so we'll know when we run on battery + wifi, in which case we must manually wake the device
     // as soon as we run on battery + wifi, checkRunningOnUnpluggedWifi() will setup RepeatingAlarmReceiver
     // RepeatingAlarmReceiver will wake the device partially every 8 minutes for a few seconds
     onWifiChanged = new BroadcastReceiver() { 
       public void onReceive(Context context, Intent intent) { 
         int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1); 
         switch(state) {
           case WifiManager.WIFI_STATE_DISABLED: 
             if(Config.LOGD) Log.i(LOGTAG, "onReceive() WIFI_STATE_DISABLED");
             wifiEnabled = new Boolean(false);
             checkRunningOnUnpluggedWifi();
             break; 
           
           case WifiManager.WIFI_STATE_ENABLED: 
             if(Config.LOGD) Log.i(LOGTAG, "onReceive() WIFI_STATE_ENABLED");
             wifiEnabled = new Boolean(true);
             checkRunningOnUnpluggedWifi();
             break; 
           
           case WifiManager.WIFI_STATE_UNKNOWN : 
             if(Config.LOGD) Log.i(LOGTAG, "onReceive() WIFI_STATE_UNKNOWN");
             //wifiEnabled = false;
             break; 
         }
       }
     };
 
     IntentFilter intentFilter = new IntentFilter();
     intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
     intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
     intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
     registerReceiver(onWifiChanged, intentFilter);
 
     onBatteryChanged = new BroadcastReceiver() { 
       public void onReceive(Context context, Intent intent) { 
         int plugged = intent.getIntExtra("plugged", -1); 
         boolean newPowerPlugged = false;
         if(plugged==BatteryManager.BATTERY_PLUGGED_AC || plugged==BatteryManager.BATTERY_PLUGGED_USB)
           newPowerPlugged = true;
         if(powerPlugged==null || newPowerPlugged!=powerPlugged) {
           powerPlugged = new Boolean(newPowerPlugged);
           //if(Config.LOGD) Log.i(LOGTAG, "onBatteryChanged powerPlugged="+powerPlugged);
           checkRunningOnUnpluggedWifi();
         }
       } 
     };
     registerReceiver(onBatteryChanged, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
 
     // sample code: check hasVibrator
     // PackageManager pm = getPackageManager();
     // boolean hasCompass = pm.hasSystemFeature(PackageManager.FEATURE_VIBRATOR_SERVICE);
     // vibrator = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
 
     numberOfQueuedMessagesSinceLastClientActivity = 0;
   }
 
   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
     // called by the system every time a client explicitly starts the service by calling startService(Intent),
     connectThread = null;
     numberOfQueuedMessagesSinceLastClientActivity = 0;
 
     // store consumer key + secret in system properties for twitter4j to use
     System.setProperty("twitter4j.oauth.consumerKey", intent.getStringExtra("consumerKey"));
     System.setProperty("twitter4j.oauth.consumerSecret", intent.getStringExtra("consumerSecret"));
 
     linkifyMessages = false;
     String linkifySwitch = intent.getStringExtra("linkify");
     if(linkifySwitch!=null && linkifySwitch.equals("true"))
       linkifyMessages = true;
 
     // create a specific BroadcastIntent based on intentFilter string, given via intent, to notify activity of new data
     String tickerNewEntryBroadcastIntentString = intent.getStringExtra("intentFilter");
     tickerNewEntryBroadcastIntent = new Intent(tickerNewEntryBroadcastIntentString);
     if(Config.LOGD) Log.i(LOGTAG, String.format("onStartCommand() flags=%d startId=%d isConnected=%b tickerNewEntryBroadcastIntent=%s",
                                                 flags,startId,isConnected(),tickerNewEntryBroadcastIntentString));
 
     (connectThread = new ConnectThread(this)).start();
 
     return START_NOT_STICKY;
   }
   
   @Override 
   public void onLowMemory() {
     if(Config.LOGD) Log.i(LOGTAG, "onLowMemory()");
     synchronized(messageList) {
       while(messageList.size()>maxQueueMessages/2) {
         messageList.removeLast();
       }
     }
   }
 
   @Override 
   public void onDestroy() {
     super.onDestroy(); 
 
     if(twitterStream!=null) {
       if(Config.LOGD) Log.i(LOGTAG, "onDestroy() twitterStream.cleanUp()");
       twitterStream.cleanUp();
       if(Config.LOGD) Log.i(LOGTAG, "onDestroy() twitterStream.shutdown()");
       twitterStream.shutdown();
       twitterStream = null;
     }
 
     if(onWifiChanged!=null)
       unregisterReceiver(onWifiChanged);
 
     if(onBatteryChanged!=null)
       unregisterReceiver(onBatteryChanged);
   }
 
   public class LocalBinder extends TickerServiceAbstract.LocalBinder {
     public TwitterService getService() {
       return TwitterService.this;
     }
   }
 
   final Binder localBinder = new LocalBinder();
 
   @Override
   public IBinder onBind(Intent intent) {
     return localBinder;
   }
 
   public void onPause() {
     activityPaused = true;
   }
 
   public void onResume() {
     activityPaused = false;
     numberOfQueuedMessagesSinceLastClientActivity = 0;
 
     if(!isConnected()) {
       if(connectThread==null) {
         if(Config.LOGD) Log.i(LOGTAG, "onResume() not connected - new ConnectThread()...");
         (connectThread = new ConnectThread(this)).start();
       } else {
         if(Config.LOGD) Log.i(LOGTAG, "onResume() not connected - another ConnectThread() is still in process - do nothing");
       }
     }
   }
 
   public boolean isConnected() {
     if(connectThread!=null && twitter!=null && twitterStream!=null)
       return true;
     return false;
   }
 
   public boolean isConnecting() { // actually this is isConnectingOrConnected()
     if(connectThread!=null)
       return true;
     return false;
   }
 
   public String getErrMsg() {
     String retStr = errMsg;
     errMsg = null;
     return retStr;
   }
 
   public synchronized List<EntryTopic> getMessageListLatestAfterMS(long lastNewestMessageMS, int maxCount) {
    List<EntryTopic> retlist = null;
     int count=0;
     synchronized(messageList) {
       while(count<maxCount && count<messageList.size() && messageList.get(count).createTimeMs > lastNewestMessageMS)
         count++;
      retlist = Collections.synchronizedList(messageList.subList(0,count));
     }
    if(Config.LOGD) Log.i(LOGTAG, String.format("getMessageListLatestAfterMS(%d) count=%d messageList.size()=%d",lastNewestMessageMS,count,messageList.size()));
     return retlist;
   }
 
   public synchronized List<EntryTopic> getMessageListLatest(int maxCount) {
    List<EntryTopic> retlist=null;
     synchronized(messageList) {
       if(maxCount>messageList.size())
         maxCount = messageList.size();
      retlist = Collections.synchronizedList(messageList.subList(0,maxCount));
     }
    if(Config.LOGD) Log.i(LOGTAG, String.format("getMessageListLatest() maxCount=%d connected=%b",maxCount,isConnected()));
     return retlist;
   }
 
   private boolean checkRunningOnUnpluggedWifi() {
     boolean newUnpluggedWifiWakeupActivated = false;
     if(wifiEnabled!=null && wifiEnabled && powerPlugged!=null && !powerPlugged)
       newUnpluggedWifiWakeupActivated = true;
 
     if(newUnpluggedWifiWakeupActivated!=unpluggedWifiWakeupActivated) {
       unpluggedWifiWakeupActivated=newUnpluggedWifiWakeupActivated;
       if(unpluggedWifiWakeupActivated) {
         // device has gone into: WIFI + UNPLUGGED, we need to activate automatic "unplugged-Wifi" wakeups
         if(Config.LOGD) Log.i(LOGTAG, "checkRunningOnUnpluggedWifi() NOW RUNNING IN UNPLUGGED+WIFI MODE");
 
         if(wakeLockNoScreen==null) {
           Log.e(LOGTAG, "checkRunningOnUnpluggedWifi() wakeLockNoScreen NOT AVAILABLE, cannot create RepeatingAlarmReceiver");
         } else {
           Intent intent = new Intent(TwitterService.this, RepeatingAlarmReceiver.class);
           pendingIntent = PendingIntent.getBroadcast(TwitterService.this, 0, intent, 0);
           if(alarmManager==null)
             alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
           alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 
                                     System.currentTimeMillis() + (600*1000),        // 1st wakeup in 10 minutes (=600*1000) from now
                                     480*1000,                                       // followed up every 8 minutes (=480*1000)
                                     pendingIntent);
 
           if(Config.LOGD) Log.i(LOGTAG, "checkRunningOnUnpluggedWifi() ACTIVATED unpluggedWifiWakeup");
         }
       } else {
         // just now, the device has gone OUT of: WIFI + UNPLUGGED  (it's now either: 3G+unplugged, or WiFi+plugged, or 3G+plugged)
         if(Config.LOGD) Log.i(LOGTAG, "checkRunningOnUnpluggedWifi() WE DON'T RUN IN UNPLUGGED+WIFI ANYMORE");
 
         // deactivate unplugged-Wifi-Wakeup
         if(alarmManager!=null) {
           if(pendingIntent!=null) {
             alarmManager.cancel(pendingIntent);
             pendingIntent=null;
             if(Config.LOGD) Log.i(LOGTAG, "checkRunningOnUnpluggedWifi() DE-ACTIVATED unpluggedWifiWakeup");
           } else
             if(Config.LOGD) Log.i(LOGTAG, "checkRunningOnUnpluggedWifi() DE-ACTIVATED unpluggedWifiWakeup pendingIntent==null");
         } else
           if(Config.LOGD) Log.i(LOGTAG, "checkRunningOnUnpluggedWifi() DE-ACTIVATED unpluggedWifiWakeup alarmManager==null");
       }
     }
 
     return unpluggedWifiWakeupActivated;
   }
 
   public static class RepeatingAlarmReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(Context context, Intent intent) {
       // device partly and briefly waking up from sleep
       Date date = new Date();
       SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
       if(wakeLockNoScreen!=null) {
         // we need to stay awake enough time to let wifi re-connect
         if(Config.LOGD) Log.i(LOGTAG, "RepeatingAlarmReceiver UBPLUGGED+WIFI KEEP WAKE 10 secs starting at "+simpleDateFormat.format(date));
         wakeLockNoScreen.acquire(10000l);    
       } else {
         if(Config.LOGD) Log.i(LOGTAG, "RepeatingAlarmReceiver no wakeLockNoScreen, cannot activateWiFiConnection "+simpleDateFormat.format(date));
       }
     }
   }
 
   public String linkify(String msgString, String linkName, boolean twitterMode) {
 	  // parse through 'msgString' and convert any [http://....] to [<a href="http://...">http://...</a>]
 	  int startOffset = 0;
 	  int numberOfCharactersTillNextSpace = 0;
 	  String search = "http://";
     messageLink = null;
     String link2 = null;
 
 	  int offset = msgString.indexOf(search);
 	  while(offset>=0) {
 	    // search pattern starts on the beginning of the string or it has a leading whitespace
 	    messageLink = msgString.substring(offset);
 	    if(messageLink==null)
 	      break;
 	    numberOfCharactersTillNextSpace = messageLink.indexOf(" ");
       //if(Config.LOGD) Log.i(LOGTAG, String.format("linkify() offset=%d link=%s numberOfCharactersTillNextSpace=%d",offset,messageLink,numberOfCharactersTillNextSpace));
 	    if(numberOfCharactersTillNextSpace>0)
 	      messageLink = messageLink.substring(0,numberOfCharactersTillNextSpace);
 	    else
 		    numberOfCharactersTillNextSpace = messageLink.length();    // java.lang.NullPointerException
 
       int idxTwitpic = messageLink.indexOf("//twitpic.com/");
       if(idxTwitpic>=0) {
         String id = messageLink.substring(idxTwitpic+14);
         String markup = "<img width='300' height='300' style='margin-left:auto;margin-right:auto;display:block;' onload='window.onImageLoad(this)' src='http://twitpic.com/show/thumb/" + id + "' />";    // oiginal size = 150x150
         //Log.i(LOGTAG,"linkify() twitpic markup="+markup);
         msgString = msgString.substring(0,offset) + markup;
 	      startOffset = offset + markup.length();
       } else {
         String displayLink = messageLink;
         if(linkName!=null)
           displayLink = linkName;
 
         // todo: java.lang.StringIndexOutOfBoundsException:
         String markup2 = "<a href=\"" + messageLink + "\">" + displayLink + "</a> ";
         if(offset+numberOfCharactersTillNextSpace<msgString.length())
           markup2 += msgString.substring(offset+numberOfCharactersTillNextSpace);
         msgString = msgString.substring(0,offset) + markup2;
 	      startOffset = offset + markup2.length();
 	    }
 
 	    int nextOffset = msgString.substring(startOffset).indexOf(search);  // todo: java.lang.StringIndexOutOfBoundsException
 		  if(nextOffset<0)
 			  break;
 	    offset = startOffset + nextOffset;
 	  }
 
 	  search = "https://";
 	  offset = msgString.indexOf(search);
 	  while(offset>=0) {
 	    // search pattern starts on the beginning of the string or it has a leading whitespace
 	    messageLink = msgString.substring(offset);
 	    numberOfCharactersTillNextSpace = messageLink.indexOf(" ");
 	    if(numberOfCharactersTillNextSpace>0)
 	      messageLink = messageLink.substring(0,numberOfCharactersTillNextSpace);
 	    else
 		    numberOfCharactersTillNextSpace = messageLink.length();
 
       String displayLink = messageLink;
       if(linkName!=null)
         displayLink = linkName;
       msgString = msgString.substring(0,offset) + "<a href=\"" + messageLink + "\">" + displayLink + "</a> "+msgString.substring(offset+numberOfCharactersTillNextSpace);
 	    startOffset = offset + 9 + numberOfCharactersTillNextSpace + 2 + displayLink.length() + 5;
 
       //Log.i(LOGTAG,"http2link msgString="+msgString+" length="+msgString.length()+" startOffset="+startOffset+" search="+search);
 	    int nextOffset = msgString.substring(startOffset).indexOf(search);
       //Log.i(LOGTAG,"http2link numberOfCharactersTillNextSpace="+numberOfCharactersTillNextSpace+" offset="+offset+" startOffset="+startOffset+" nextOffset="+nextOffset+" link="+messageLink);
 		  if(nextOffset<0)
 			  break;
 	    offset = startOffset + nextOffset;
 	  }
 
     if(twitterMode) {	
       // linkify "#...."
 	    search = "#";
 	    offset = msgString.indexOf(search);
 	    while(offset>=0) {
 	      link2 = msgString.substring(offset);
 	      numberOfCharactersTillNextSpace = link2.indexOf(" ");
 	      if(numberOfCharactersTillNextSpace>0)
 	        link2 = link2.substring(0,numberOfCharactersTillNextSpace);
 	      else
 		      numberOfCharactersTillNextSpace = link2.length();
 
         msgString = msgString.substring(0,offset) + "<a href=\"http://twitter.com/search?q=%23" + link2 + "\">" + link2 + "</a> "+msgString.substring(offset+numberOfCharactersTillNextSpace);
 	      startOffset = offset + 40 + numberOfCharactersTillNextSpace + 2 + numberOfCharactersTillNextSpace + 5;
 
 	      int nextOffset = msgString.substring(startOffset).indexOf(search);
         //Log.i(LOGTAG,"http2link # numberOfCharactersTillNextSpace="+numberOfCharactersTillNextSpace+" offset="+offset+" startOffset="+startOffset+" nextOffset="+nextOffset+" link2="+link2);
 		    if(nextOffset<0)
 			    break;
 	      offset = startOffset + nextOffset;
 	    }
 	    if(messageLink==null && link2!=null)
 	      messageLink=link2;
 
       // linkify "@...."
 	    search = "@";
 	    offset = msgString.indexOf(search);
 	    while(offset>=0) {
 	      link2 = msgString.substring(offset);
 	      numberOfCharactersTillNextSpace = link2.indexOf(" ");
 	      if(numberOfCharactersTillNextSpace>0)
 	        link2 = link2.substring(0,numberOfCharactersTillNextSpace);
 	      else
 		      numberOfCharactersTillNextSpace = link2.length();
 
         msgString = msgString.substring(0,offset) + "<a href=\"http://twitter.com/" + link2 + "\">" + link2 + "</a> "+msgString.substring(offset+numberOfCharactersTillNextSpace);
 	      startOffset = offset + 28 + numberOfCharactersTillNextSpace + 2 + numberOfCharactersTillNextSpace + 5;
 
 	      int nextOffset = msgString.substring(startOffset).indexOf(search);
         //Log.i(LOGTAG,"http2link @ numberOfCharactersTillNextSpace="+numberOfCharactersTillNextSpace+" offset="+offset+" startOffset="+startOffset+" nextOffset="+nextOffset+" link2="+link2);
 		    if(nextOffset<0)
 			    break;
 	      offset = startOffset + nextOffset;
 	    }
 	    if(messageLink==null && link2!=null)
 	      messageLink=link2;
     }
 
     return msgString;
   }
 
   public String linkifyLink() {
     return messageLink;
   }
 
   public synchronized void twitterLogin(String pin) {
     // called by OAuthActivity
     if(Config.LOGD) Log.i(LOGTAG, String.format("twitterLogin pin=%s",pin));
 
     if(pin==null || pin.length()==0) {
       // pin-entry was aborted or failed for other reason
       if(Config.LOGD) Log.i(LOGTAG, "twitterLogin pin-entry was aborted");
       connectThread = null;
       return;
     }
 
     accessToken = null;
     if(twitter!=null) {
       try {
         if(pin.length() > 0 && requestToken!=null) {
           if(Config.LOGD) Log.i(LOGTAG, String.format("twitterLogin .getOAuthAccessToken("+requestToken+","+pin+")"));
           accessToken = twitter.getOAuthAccessToken(requestToken, pin);
         }
         else {
           if(Config.LOGD) Log.i(LOGTAG, String.format("twitterLogin .getOAuthAccessToken()"));
           accessToken = twitter.getOAuthAccessToken();
         }
         if(Config.LOGD) Log.i(LOGTAG, "twitter.verifyCredentials().getId()="+twitter.verifyCredentials().getId());
       } catch(twitter4j.TwitterException twex) {
         accessToken = null;
       }
     } 
 
     if(accessToken==null) {
       if(Config.LOGD) Log.e(LOGTAG, String.format("twitterLogin pin=%s accessToken==null ###############",pin));
       errMsg = "no accessToken";
       connectThread = null;
       return;
     }
 
     // store accessToken + secret in preferences (read by ConnectThread run() from now on)
     if(Config.LOGD) Log.i(LOGTAG, String.format("twitterLogin storePrefs() accessToken=%s / %s",accessToken.getToken(),accessToken.getTokenSecret()));
     SharedPreferences.Editor editor = preferences.edit();
     editor.putString("oauth.accessToken", accessToken.getToken());
     editor.putString("oauth.accessTokenSecret", accessToken.getTokenSecret());
     editor.commit();
 
     (connectThread = new ConnectThread(this)).start();
   }
 
   private int findIdxOfMsgWithSameTimeMs(long timeMs) {
     int idx=0;
     synchronized(messageList) {
       while(idx<messageList.size() && messageList.get(idx).createTimeMs != timeMs)
         idx++;
       if(idx>=messageList.size())
         idx=-1;
     }
     return idx;
   }
 
   private EntryTopic msgWithSameId(long id) {
     EntryTopic retEntryTopic = null;
     int idx=0;
     synchronized(messageList) {
       while(idx<messageList.size() && messageList.get(idx).id != id)
         idx++;
       if(idx<messageList.size())
         retEntryTopic = messageList.get(idx);
     }
     return retEntryTopic;
   }
 
   private int findIdxOfFirstOlderMsg(EntryTopic msgObject) {
     int idx=0;
     synchronized(messageList) {
       while(idx<messageList.size() && messageList.get(idx).createTimeMs > msgObject.createTimeMs)
         idx++;
     }
     return idx;
   }
 
   private boolean processStatus(Status status) {
     if(msgWithSameId(status.getId())!=null) {
       if(Config.LOGD) Log.i(LOGTAG, "processStatus() found msgWithSameId "+status.getId()+" - don't process");
       return false;
     }
 
     boolean newMsgReceived = false;
     User user = status.getUser();
     String channelImageString = null;
     try {
       java.net.URI uri = user.getProfileImageURL().toURI();
       channelImageString = uri.toString();
     } catch(java.net.URISyntaxException uriex) {
       Log.e(LOGTAG, String.format("ConnectThread processStatus() URISyntaxException %s ex=%s",user.getProfileImageURL().toString(),uriex));
       errMsg = uriex.getMessage();
     }
     
     String title = status.getText();
     if(linkifyMessages) {
       title = linkify(title,null,true); 
       // messageLink will contain the link-url
     }
 
     long timeMs = status.getCreatedAt().getTime();     
     // make timeMs unique in our messageList
     while(findIdxOfMsgWithSameTimeMs(timeMs)>=0)
       timeMs++;
 
     EntryTopic feedEntry =  new EntryTopic(0,
                                            0,
                                            user.getName(),
                                            title,
                                            null,
                                            messageLink,
                                            timeMs,
                                            status.getId(),
                                            channelImageString);
     synchronized(messageList)
     {
       // messageList is always sorted with the newest items on top
       int findIdxOfFirstOlder = findIdxOfFirstOlderMsg(feedEntry);
       if(findIdxOfFirstOlder<maxQueueMessages) {
         messageList.add(findIdxOfFirstOlder,feedEntry);
         newMsgReceived = true;
         totalNumberOfQueuedMessages++;
         if(activityPaused)
           numberOfQueuedMessagesSinceLastClientActivity++;
 
                 // debug: for every regular msg, create 5 additional dummy messages
                 //for(int i=1; i<=5; i++) {
                 //  feedEntry = new EntryTopic(0,                                   // region
                 //                             0,                                   // prio
                 //                             "dummy",
                 //                             "test message "+i,
                 //                             null,                                // description
                 //                             messageLink,
                 //                             timeMs+i*100,
                 //                             status.getId()+i,                    // todo: make sure the id was ot yet stored in messageList
                 //                             channelImageString);                 // todo: must make use of this in MyWebView/JsObject/script.js
                 //  messageList.add(findIdxOfFirstOlder,feedEntry);
                 //  totalNumberOfQueuedMessages++;
                 //  if(activityPaused)
                 //    numberOfQueuedMessagesSinceLastClientActivity++;
                 //}
 
         // if there are now more than 'maxQueueMessages' entrys in the queue, remove the oldest...
         while(messageList.size()>maxQueueMessages)
           messageList.removeLast();
       } else {
         if(Config.LOGD) Log.i(LOGTAG, "processStatus() not findIdxOfFirstOlder<maxQueueMessages - don't process");
       }
     }
     return newMsgReceived;
   }
 
   private void notifyClient() {
     if(activityPaused) {
       if(numberOfQueuedMessagesSinceLastClientActivity >= maxQueueMessages-10) {
         if(Config.LOGD) Log.i(LOGTAG, "notifyClient() numberOfQueuedMessagesSinceLastClientActivity="+numberOfQueuedMessagesSinceLastClientActivity+" BUZZ ##########");
         if(vibrator!=null) {
           vibrator.vibrate(600);
         }
         numberOfQueuedMessagesSinceLastClientActivity=0;
       }
     } else {
       //if(Config.LOGD) Log.i(LOGTAG, "ConnectThread before sendBroadcast() numberOfQueuedMessagesSinceLastClientActivity="+numberOfQueuedMessagesSinceLastClientActivity+" activityUpdatePending="+activityUpdatePending);
       //sendBroadcast(tickerNewEntryBroadcastIntent); // this ends up in ServiceClient BroadcastReceiver() onReceive()
       // this broadcast better not be sent more often than once per second - therefor we use ActivityUpdateThread to delay the broadcast...
       if(!activityUpdatePending)
       {
         activityUpdatePending = true; 
         if(Config.LOGD) Log.i(LOGTAG, "notifyClient() new ActivityUpdateThread()");
         activityUpdateThread = new ActivityUpdateThread();
         activityUpdateThread.start();
       }
     }
   }
 
   private int fetchHomeTimeline() {
     // retrieve the latest up to maxQueueMessages twt-msgs
     int count=0, countProcessed=0;
     if(twitter!=null) {
       List<Status> statuses = null;
       try {
         int messageListSize = 0;
 //        synchronized(messageList) {
 //          messageListSize = messageList.size();
 //        }
 
         Paging paging = new Paging();
         paging.setCount(maxQueueMessages);
         if(Config.LOGD) Log.i(LOGTAG, String.format("fetchHomeTimeline() twitter.getHomeTimeline() maxQueueMessages=%d",maxQueueMessages));
         statuses = twitter.getHomeTimeline(paging); // java.lang.IllegalStateException + android.os.NetworkOnMainThreadException
         
         if(statuses!=null) {
           count = statuses.size();
 //          if(Config.LOGD) Log.i(LOGTAG, String.format("fetchHomeTimeline() count=%d, messageList.size()=%d",count,messageListSize));
 
           for(Status status : statuses) {
             if(processStatus(status))
               countProcessed++;
           }
 
           synchronized(messageList) {
             messageListSize = messageList.size();
           }
           if(Config.LOGD) Log.i(LOGTAG, String.format("fetchHomeTimeline() countProcessed=%d, messageList.size()=%d done",countProcessed,messageListSize));
 
           if(countProcessed>0)
             notifyClient();
         } else {
           if(Config.LOGD) Log.i(LOGTAG, "fetchHomeTimeline() no statuses");
         }
       } catch(TwitterException twex) {
         Log.e(LOGTAG, "fetchHomeTimeline() failed to get twitter timeline: "+twex);
         errMsg = twex.getMessage();
       } catch(java.lang.IllegalStateException illstaex) {
         Log.e(LOGTAG, "fetchHomeTimeline() IllegalStateException illstaex="+illstaex);
         errMsg = "IllegalStateException on .getHomeTimeline()";
       }
     }
     return count;
   }
 
   private class ConnectThread extends Thread {
     Context context = null;
 
     ConnectThread(Context context) {
       this.context = context;
       if(Config.LOGD) Log.i(LOGTAG, "ConnectThread constructor..");
     }
 
     public void run() {
       if(Config.LOGD) Log.i(LOGTAG, "ConnectThread run() ...");
 
       if(twitterStream!=null) {
         if(Config.LOGD) Log.i(LOGTAG, "connectStream() TwitterStreamFactory().shutdown()");
         twitterStream.cleanUp();
         twitterStream.shutdown();
         twitterStream = null;
       }
 
       // create accessToken from preferences "oauth.accessToken" + "oauth.accessTokenSecret"
       String oauthAccessToken = preferences.getString("oauth.accessToken", "");
       if(oauthAccessToken!=null && oauthAccessToken.length()>0) {
         String oauthAccessTokenSecret = preferences.getString("oauth.accessTokenSecret", "");
         if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread run() preferences oauth.accessTokenSecret=%s",oauthAccessTokenSecret));
         if(oauthAccessTokenSecret!=null && oauthAccessTokenSecret.length()>0) {
           accessToken = new AccessToken(oauthAccessToken,oauthAccessTokenSecret);
         }
       }
 
       if(accessToken==null) {
         if(Config.LOGD) Log.i(LOGTAG, "ConnectThread run() UNAUTHORIZED TwitterFactory().getInstance() ...");
         twitter = new TwitterFactory().getInstance();
         if(twitter==null) {
           if(Config.LOGD) Log.e(LOGTAG, "ConnectThread run() failed to instantiate TwitterFactory()");
           errMsg = "no twitter object from factory without accessToken";
           connectThread = null;
           return;
         }
 
         // startActivity OAuthActivity:  open oauthAuthorizationURL in an embedded browser and let the user login
         if(Config.LOGD) Log.i(LOGTAG, "ConnectThread run() twitter.getOAuthRequestToken()....");
         try {
           requestToken = twitter.getOAuthRequestToken();
           if(requestToken!=null) {
             String oauthLoginActivityName = "com.vodafone.twitter.service.OAuthActivity";
             if(Config.LOGD) Log.i(LOGTAG, "ConnectThread run() requestToken.getAuthorizationURL() ...");
             String oauthAuthorizationURL = requestToken.getAuthorizationURL();
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread run() start %s url=%s",oauthLoginActivityName,oauthAuthorizationURL));
             Intent intent = new Intent(getBaseContext(), Class.forName(oauthLoginActivityName));
             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
             intent.putExtra("argAuthorizationURL", oauthAuthorizationURL);
             getApplication().startActivity(intent); // -> OAuthActivity -> twitterLogin() -> new ConnectThread(this).start()
             if(Config.LOGD) Log.i(LOGTAG, "ConnectThread run() OAuthActivity to call us back via twitterLogin(pin) ...");
           } else {
             Log.e(LOGTAG, "ConnectThread run() got no requestToken = twitter.getOAuthRequestToken()");
             connectThread = null;
           }
         } catch(twitter4j.TwitterException twex) {
           Log.e(LOGTAG, "ConnectThread run() TwitterException="+twex);
           // "Received authentication challenge is null" - thrown if the server replies with a 401 - most likely due to clock setting
           errMsg = twex.getMessage() + " - please check your system clock";
           connectThread = null;
         } catch(java.lang.ClassNotFoundException cnfex) {
           Log.e(LOGTAG, "ConnectThread run() ClassNotFoundException="+cnfex);
           errMsg = cnfex.getMessage();
           connectThread = null;
         } catch(java.lang.IllegalStateException istex) {
           Log.e(LOGTAG, "ConnectThread run() IllegalStateException="+istex);
           errMsg = istex.getMessage();
           connectThread = null;
         } catch(Exception ex) {
           Log.e(LOGTAG, "ConnectThread run() Exception="+ex);
           errMsg = ex.getMessage();
           connectThread = null;
         }
         return;
       }  
 
       // got our accessToken alright
       if(Config.LOGD) Log.i(LOGTAG, "ConnectThread run() TwitterFactory().getInstance(accessToken) ...");
       twitter = new TwitterFactory().getInstance(accessToken); // may take a little time...
       if(twitter==null) {
         if(Config.LOGD) Log.e(LOGTAG, "ConnectThread run() failed to instantiate TwitterFactory()");
         errMsg = "no twitter object from factory with accessToken="+accessToken;
         connectThread = null;
         return;
       }
 
       if(!fetchHomeTimelineDone) {
         // fetch HomeTimeline only once after the service has established a connection
         int count = fetchHomeTimeline();
         fetchHomeTimelineDone = true;
         if(Config.LOGD) Log.i(LOGTAG, "ConnectThread run() received count="+count+" from fetchHomeTimeline()");
       }
 
       numberOfQueuedMessagesSinceLastClientActivity = 0;
       connectStream();
     }
 
     void connectStream() {
       if(Config.LOGD) Log.i(LOGTAG, "connectStream()...");
 
       if(statusListener==null) {
         statusListener = new UserStreamListener() {
           public void onStatus(Status status) {
             if(Config.LOGD) Log.i(LOGTAG, String.format("onStatus() twt=%s: %s activityUpdatePending=%b activityPaused=%b",status.getUser().getName(),status.getText(),activityUpdatePending,activityPaused));
             if(processStatus(status)) {
               notifyClient();
             } else {
               if(Config.LOGD) Log.i(LOGTAG, "onStatus() msg not processed");
             }
           }
 
           public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onDeletionNotice"));
           }
 
           public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener limit notice trigger numberOfLimitedStatuses=%d",numberOfLimitedStatuses));
           }
 
           public void onScrubGeo(long userId, long upToStatusId) {
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onScrubGeo"));
           }
 
           public void onException(Exception ex) {
             int statusCode = ((TwitterException)ex).getStatusCode();
             switch(statusCode) {
               case -1: // Stream closed
                 Log.e(LOGTAG, "ConnectThread StatusListener onException -1: Stream closed");
                 errMsg = "stream closed";
 //                // be patient, don't overload twitter service
 //                try { Thread.sleep(20000); } catch(Exception ex2) {};
                 fetchHomeTimelineDone=false;
                 break;
 
               case 401: // Authentication credentials were missing or incorrect
                 Log.e(LOGTAG, "ConnectThread StatusListener onException 401: Authentication credentials were missing or incorrect");
                 errMsg = "Authentication credentials were missing or incorrect (401)";
                 try { Thread.sleep(3000); } catch(Exception ex2) {};
                 connectStream();
                 // todo: it won't be enough to start a new conectStream() ???
                 break;
 
               case 420: // The number of requests you have made exceeds the quota afforded by your assigned rate limit
                 Log.e(LOGTAG, "ConnectThread StatusListener onException "+ex);
                 errMsg = "number of requests (420)";
                 try { Thread.sleep(20000); } catch(Exception ex2) {};
                 // todo: ???
                 break;
 
               default:
                 Log.e(LOGTAG, "ConnectThread StatusListener onException "+ex);
                 // todo: ???
                 break;
             }
 
             Log.e(LOGTAG, String.format("ConnectThread StatusListener onException statusCode=%d done",statusCode));
           }
 
           public void onBlock(User source, User blockedUser) { 
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onBlock"));
           };
                    
           public void onDeletionNotice(long directMessageId, long userId) { 
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onDeletionNotice"));
           };
                    
           public void onDirectMessage(DirectMessage directMessage) { 
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onDirectMessage"));
           };
                    
           public void onFavorite(User source, User target, Status favoritedStatus) { 
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onFavorite"));
           };
                    
           public void onFollow(User source, User followedUser) { 
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onFollow"));
           };
                    
           public void onFriendList(long[] friendIds) { 
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onFriendList"));
           };
                    
           public void onRetweet(User source, User target, Status retweetedStatus) { 
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onRetweet"));
           };
                    
           public void onUnblock(User source, User unblockedUser) { 
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onUnblock"));
           };
                    
           public void onUnfavorite(User source, User target, Status unfavoritedStatus) { 
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onUnfavorite"));
           };
                    
           public void onUserListCreation(User listOwner, UserList list) { 
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onUserListCreation"));
           };
                    
           public void onUserListDeletion(User listOwner, UserList list) { 
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onUserListDeletion"));
           };
                    
           public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) { 
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onUserListMemberAddition"));
           };
                    
           public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) { 
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onUserListMemberDeletion"));
           };
                    
           public void onUserListSubscription(User subscriber, User listOwner, UserList list) { 
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onUserListSubscription"));
           };
                    
           public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) { 
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onUserListUnsubscription"));
           };
                    
           public void onUserListUpdate(User listOwner, UserList list) { 
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onUserListUpdate"));
           };
                    
           public void onUserProfileUpdate(User updatedUser) { 
             if(Config.LOGD) Log.i(LOGTAG, String.format("ConnectThread StatusListener onUserProfileUpdate"));
           };
         };
       }
 
       if(Config.LOGD) Log.i(LOGTAG, "ConnectThread connectStream() TwitterStreamFactory().getInstance(accessToken)");
       twitterStream = new TwitterStreamFactory().getInstance(accessToken);
       if(twitterStream!=null) {
         if(Config.LOGD) Log.i(LOGTAG, "ConnectThread connectStream() got twitterStream");
         twitterStream.addListener(statusListener);
         if(Config.LOGD) Log.i(LOGTAG, "ConnectThread connectStream() StatusListener added");
 
         try {
           if(Config.LOGD) Log.i(LOGTAG, "ConnectThread connectStream() twitterStream.getUserStream() ...");
         //userStream = twitterStream.getUserStream(); // simply doesn't work
           twitterStream.user();
       //} catch(TwitterException twitterException) {
       //  Log.e(LOGTAG, "ConnectThread connectStream() TwitterException twitterException="+twitterException);
         } catch(java.lang.IllegalStateException illstaex) {
           Log.e(LOGTAG, "ConnectThread connectStream() IllegalStateException illstaex="+illstaex);
           errMsg = "IllegalStateException on .user()";
           connectThread = null;
           twitterStream = null;
         }
       } else {
         Log.e(LOGTAG, "ConnectThread connectStream() got no twitterStream");
         errMsg = "no twitterStream instance";
         connectThread = null;
       }
     }
   }
 
   private class ActivityUpdateThread extends Thread {
     // this helper thread sends delayed broadcasts to our activity
     // the delay is used to prevent multiple broadcasts invoked by multiple tweets being received 'at once'
     int delayMS = 1200;
 
     ActivityUpdateThread() {
     }
 
     void setDelayMS(int delayMS) {
       this.delayMS = delayMS;
     }
 
     public void run() {
       int broadcastNumber=totalNumberOfBrodcastsSend;
       if(totalNumberOfBrodcastsSend>0) {
         try { Thread.sleep(delayMS); } catch(java.lang.InterruptedException iex) { }
       }
 
       // this will arrive in the activity's BroadcastReceiver onReceive()
       sendBroadcast(tickerNewEntryBroadcastIntent); 
 
       totalNumberOfBrodcastsSend++;
       activityUpdatePending = false;
     }
   }
 }
 
