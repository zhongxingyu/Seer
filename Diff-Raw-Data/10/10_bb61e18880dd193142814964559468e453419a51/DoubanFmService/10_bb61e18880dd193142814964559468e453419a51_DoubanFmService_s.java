 package com.saturdaycoder.easydoubanfm;
 import android.app.Notification;
 import android.media.*;
 import android.os.Handler;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.AsyncTask;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.IBinder;
 import android.telephony.TelephonyManager;
 import android.text.format.DateFormat;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 
 import org.apache.http.*;
 import org.apache.http.client.*;
 import org.apache.http.impl.*;
 import org.apache.http.impl.client.*;
 import org.apache.http.message.*;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.apache.http.client.methods.*;
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.*;
 import android.media.MediaPlayer;
 import org.json.*;
 import android.media.AudioManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Message;
 import org.apache.http.params.*;
 
 import android.media.MediaScannerConnection.*;
 
 public class DoubanFmService extends Service implements IDoubanFmService {
 	private static final String BROADCAST_PREFIX = "com.saturdaycoder.easydoubanfm";
 
 	// actions for player
 	public static final String ACTION_PLAYER_SKIP = BROADCAST_PREFIX + ".action.PLAYER_SKIP";
 	public static final String ACTION_PLAYER_PLAYPAUSE = BROADCAST_PREFIX + ".action.PLAYER_PLAYPAUSE";
 	public static final String ACTION_PLAYER_PAUSE = BROADCAST_PREFIX + ".action.PLAYER_PAUSE";
 	public static final String ACTION_PLAYER_RESUME = BROADCAST_PREFIX + ".action.PLAYER_RESUME";
 	public static final String ACTION_PLAYER_ON = BROADCAST_PREFIX + ".action.PLAYER_ON";
 	public static final String ACTION_PLAYER_OFF = BROADCAST_PREFIX + ".action.PLAYER_OFF";
 	public static final String ACTION_PLAYER_ONOFF = BROADCAST_PREFIX + ".action.PLAYER_ONOFF";
 	public static final String ACTION_PLAYER_RATE = BROADCAST_PREFIX + ".action.PLAYER_RATE";
 	public static final String ACTION_PLAYER_UNRATE = BROADCAST_PREFIX + ".action.PLAYER_UNRATE";
 	public static final String ACTION_PLAYER_RATEUNRATE = BROADCAST_PREFIX + ".action.PLAYER_RATEUNRATE";
 	public static final String ACTION_PLAYER_TRASH = BROADCAST_PREFIX + ".action.PLAYER_TRASH";
 	public static final String ACTION_PLAYER_SELECT_CHANNEL = BROADCAST_PREFIX + ".action.PLAYER_SELECT_CHANNEL";
 	// actions for downloader
 	public static final String ACTION_DOWNLOADER_DOWNLOAD = BROADCAST_PREFIX + ".action.DOWNLOADER_DOWNLOAD";
 	public static final String ACTION_DOWNLOADER_CANCEL = BROADCAST_PREFIX + ".action.DOWNLOADER_CANCEL";
 	public static final String ACTION_DOWNLOADER_CLEAR_NOTIFICATION = BROADCAST_PREFIX + ".action.DOWNLOADER_CLEAR_NOTIFICATION";
 	// extra for other, i.e. ui, etc.
 	public static final String ACTION_WIDGET_UPDATE = BROADCAST_PREFIX + ".action.UPDATE_WIDGET";
 	public static final String ACTION_NULL = BROADCAST_PREFIX + ".action.NULL";
 	// extra for player
 	public static final String EXTRA_MUSIC_URL = "extra.MUSIC_URL";
 	public static final String EXTRA_PICTURE_URL = "extra.MUSIC_URL";
 	
 	//extra for downloader
 	public static final String EXTRA_DOWNLOADER_DOWNLOAD_FILENAME = "extra.DOWNLOAD_FILENAME";
 	
 	// service status
 	public static final String EVENT_PLAYER_MUSIC_PREPARE_PROGRESS = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_PREPARE_PROGRESS";
 	public static final String EVENT_PLAYER_POWER_STATE_CHANGED = BROADCAST_PREFIX + ".event.PLAYER_POWER_STATE_CHANGED";
 	public static final String EVENT_PLAYER_MUSIC_STATE_CHANGED = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_STATE_CHANGED";
 	public static final String EVENT_PLAYER_MUSIC_PROGRESS = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_PROGRESS";
 	public static final String EVENT_PLAYER_PICTURE_STATE_CHANGED = BROADCAST_PREFIX + ".event.PLAYER_PICTURE_STATE_CHANGED";
 	public static final String EVENT_DOWNLOADER_STATE_CHANGED = BROADCAST_PREFIX + ".event.DOWNLOADER_STATE_CHANGED";
 	public static final String EVENT_DOWNLOADER_PROGRESS = BROADCAST_PREFIX + ".event.DOWNLOADER_PROGRESS";
 	public static final String EVENT_CHANNEL_CHANGED = BROADCAST_PREFIX + ".event.CHANNEL_CHANGED";
 	public static final String EVENT_LOGIN_STATE_CHANGED = BROADCAST_PREFIX + ".event.LOGIN_STATE_CHANGED";
 	public static final String EVENT_PLAYER_MUSIC_RATED = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_RATED";
 	public static final String EVENT_PLAYER_MUSIC_UNRATED = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_UNRATED";
 	public static final String EVENT_PLAYER_MUSIC_BANNED = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_BANNED";
 	
 	
 	public static final String EXTRA_STATE = "extra.STATE";
 	public static final String EXTRA_PROGRESS = "extra.PROGRESS";
 	public static final String EXTRA_DOWNLOAD_SESSION = "extra.DOWNLOAD_SESSION";
 	public static final String EXTRA_MUSIC_TITLE = "extra.MUSIC_TITLE";
 	public static final String EXTRA_MUSIC_ARTIST = "extra.MUSIC_ARTIST";
 	public static final String EXTRA_MUSIC_ISRATED = "extra.MUSIC_ISRATED";
 	public static final String EXTRA_CHANNEL = "extra.CHANNEL";
 	//public static final String EXTRA_RATED = "extra.RATED";
 	public static final String EXTRA_REASON = "extra.REASON";
 	
 	
 	//public static final int LOGIN_ON = 0;
 	//public static final int LOGIN_OFF = 1;
 	//public static final int LOGIN_PREPARE = 2;
 	
 	public static final int STATE_PREPARE = 0;
 	public static final int STATE_STARTED = 1;	
 	public static final int STATE_FINISHED = 2;
 	public static final int STATE_FAILED = 3;
 	public static final int STATE_CANCELLED = 4;
 	public static final int STATE_IDLE = 5;
 	public static final int STATE_ERROR = 6;
 	
 	public static final int STATE_MUSIC_SKIPPED = 7;
 	public static final int STATE_MUSIC_PAUSED = 8;
 	public static final int STATE_MUSIC_RESUMED = 9;
 	
 	public static final int INVALID_STATE = -1;
 	
 	//public static final int PLAYER_POWER_ON = 0;
 	//public static final int PLAYER_POWER_OFF = 1;
 	//public static final int PLAYER_POWER_PREPARE = 2;
 	
 	
 	//public static final int PLAYER_PICTURE_STARTED = 0;
 	//public static final int PLAYER_PICTURE_FINISHED = 1;
 	//public static final int PLAYER_PICTURE_FAILED = 2;
 	
 	//public static final int PLAYER_MUSIC_STARTED = 0;
 	//public static final int PLAYER_MUSIC_FINISHED = 1;
 	//public static final int PLAYER_MUSIC_FAILED = 2;
 
 
 	
 	//public static final int DOWNLOADER_STARTED = 0;
 	//public static final int DOWNLOADER_FINISHED = 1;
 	//public static final int DOWNLOADER_FAILED = 2;
 	//public static final int DOWNLOADER_CANCELLED = 3;
 	
 	//public static final int REASON_NETWORK_UNAVAILABLE = 0;
 	//public static final int REASON_NETWORK_SERVER_DOWN = 1;
 	//public static final int REASON_NETWORK_SERVER_NO_RESPONSE = 2;
 	public static final int REASON_NETWORK_IO_ERROR = 3;
 	public static final int REASON_NETWORK_INVALID_URL = 4;
 	
 	public static final int REASON_MUSIC_FINISHED = 5;
 	public static final int REASON_MUSIC_BANNED = 6;
 	public static final int REASON_MUSIC_RATED = 7;
 	public static final int REASON_MUSIC_UNRATED = 8;
 	public static final int REASON_MUSIC_SKIPPED = 9;
 	public static final int REASON_MUSIC_NEW_LIST = 10;
 	public static final int REASON_MUSIC_LIST_EMPTY = 11;
 	
 	
 	
 	public static final int REASON_DOWNLOAD_STORAGE_INVALID = 12;
 	public static final int REASON_DOWNLOAD_STORAGE_FULL = 13;
 	public static final int REASON_DOWNLOAD_STORAGE_IO_ERROR = 14;
 	public static final int REASON_DOWNLOAD_INVALID_FILENAME = 15;
 	
 	public static final int REASON_API_REQUEST_ERROR = 16;
 	
 
 
 	
 
 
 	
 	
 
 
 	public static final int SERVICE_NOTIFICATION_ID = 1;
 	public static final int DOWNLOAD_DEFAULT_SESSIONID = 2;
 	
 	
 	
 	
 	
 	public static final int QUICKACT_NEXT_MUSIC = 0;
 	public static final int QUICKACT_NEXT_CHANNEL = 1;
 	public static final int QUICKACT_NEXT_DOWNLOAD_MUSIC = 2;
 	public static final int QUICKACT_NEXT_PLAY_PAUSE = 3;
 	
 	private final IBinder mBinder = new LocalBinder();
 	//private GetPictureTask picTask = null;
 	
 	
 	// SYSTEM SERVICES
 	private WidgetUpdater widgetUpdater;
 	private NotificationManager notificationManager;
 	//private AudioManager audioManager;
 	//private MediaScannerConnection.MediaScannerConnectionClient scannerClient;
 
 
 	HttpParams httpParameters;
 	//private char lastStopReason;
 
 	//private Downloader downloader;
 	
 	// variables that need synchronize
 	//private volatile boolean isFmOn;
 	//private boolean isDownloaderOn;
 	
 	
 	// Event Listeners
 	//private DownloadReceiver downloadListener;
 	private PhoneCallListener phoneCallListener;
 	private PhoneControlListener phoneControlListener;
 	private ShakeDetector shakeDetector;
 	//private DoubanFmControlReceiver controlListener;
 	
 	
 	// for service foreground notification
 
 	private Notification fgNotification;
 	
 	
 	// 
 	DoubanFmPlayer dPlayer;
 	DoubanFmDownloader dDownloader;
 	
 	private static final int IDLE_DELAY = 60000;
     private Handler mDelayedStopHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             // Check again to make sure nothing is playing right now
         	Debugger.info("SERVICE is handle DelayStop");
             if (dPlayer.isOpen() || dDownloader.isOpen()) {
                 return;
             }
 
             Debugger.warn("SERVICE is stopped by DelayStopHandler");
             closeService();
         }
     };
 	
 	public class LocalBinder extends Binder {
 		
 		DoubanFmService getService() {
 			return DoubanFmService.this;
 		}
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		Debugger.debug("service onCreate");
 		
 	
 		//lastStopReason = DoubanFmApi.TYPE_NEW;
 
 		dPlayer = new DoubanFmPlayer(this);
 		dDownloader = new DoubanFmDownloader(this);
  
 		// work-around on gingerbread restart bug: onStartCommand will not execute,
 		// so update widgets here
 		if (android.os.Build.VERSION.SDK_INT == 9 
 				|| android.os.Build.VERSION.SDK_INT == 10) {
 			updateWidgets();
 		}
 		
 		phoneCallListener = new PhoneCallListener();
 		IntentFilter phoneFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
 		registerReceiver(phoneCallListener, phoneFilter);
 		
 		
 		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		httpParameters = new BasicHttpParams();
 		int timeoutConnection = Preference.getConnectTimeout(this);
 		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
 		int timeoutSocket = Preference.getSocketTimeout(this);
 		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
 		
 		DoubanFmApi.setHttpParameters(httpParameters);
 		
 		
 		// If the service was idle, but got killed before it stopped itself, the
         // system will relaunch it. Make sure it gets stopped again in that case.
         Message msg = mDelayedStopHandler.obtainMessage();
         mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Debugger.warn("SERVICE ONSTARTCOMMAND");
 		
 		mDelayedStopHandler.removeCallbacksAndMessages(null);
 		
 		if (intent == null) { // it tells us the service was killed by system.
 			//popNotify(getResources().getString(R.string.text_killed_for_memory));
 			Debugger.warn("null intent");
 			updateWidgets();
 	        mDelayedStopHandler.removeCallbacksAndMessages(null);
 	        Message msg = mDelayedStopHandler.obtainMessage();
 	        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
 			return START_NOT_STICKY;
 		}
 		
 		
 		Debugger.warn("Intent action=\"" + intent.getAction() + "\" flags=" + flags + " startId=" + startId);
 		String action = intent.getAction();
         if (action.equals(ACTION_PLAYER_ON)) {
         	Debugger.info("Douban service starts with ON command");
        		openPlayer();
         }        
         if (action.equals(ACTION_PLAYER_OFF)) {        	
         	Debugger.info("Douban service starts with OFF command");
       		closePlayer();
         }	
         if (action.equals(ACTION_PLAYER_ONOFF)) {
         	Debugger.info("Douban service starts with ON/OFF command");
         	if (!dPlayer.isOpen()) {
         		openPlayer();
         	}
         	else {
         		closePlayer();
         	}	
         }
         if (action.equals(ACTION_PLAYER_SKIP)) {
         	Debugger.info("Douban service starts with SKIP command");
         	nextMusic();
         }
         if (action.equals(ACTION_PLAYER_TRASH)) {
         	Debugger.info("Douban service starts with TRASH command");
         	banMusic();
         }
         if (action.equals(ACTION_PLAYER_RATE)) {
         	Debugger.info("Douban service starts with RATE command");
        		rateMusic();
         }
         if (action.equals(ACTION_PLAYER_UNRATE)) {
         	Debugger.info("Douban service starts with UNRATE command");
        		unrateMusic();
         }
         if (action.equals(ACTION_PLAYER_RATEUNRATE)) {
         	Debugger.info("Douban service starts with RATE/UNRATE command");
         	if (!dPlayer.isMusicRated())
         		rateMusic();
         	else 
         		unrateMusic();
         }
         if (action.equals(ACTION_PLAYER_PLAYPAUSE)) {
         	Debugger.info("Douban service starts with PLAY/PAUSE command");
         	playPauseMusic();
         }
         if (action.equals(ACTION_PLAYER_PAUSE)) {
         	Debugger.info("Douban service starts with PAUSE command");
         	pauseMusic();
         }
         if (action.equals(ACTION_PLAYER_RESUME)) {
         	Debugger.info("Douban service starts with RESUME command");
         	resumeMusic();
         }
         if (action.equals(ACTION_PLAYER_SELECT_CHANNEL)) {
         	int chann = intent.getIntExtra(EXTRA_CHANNEL, 0);
         	Debugger.info("Douban service starts with SELECT_CHANNEL: " + chann);
         	selectChannel(chann);
         }
         
         
         if (action.equals(ACTION_DOWNLOADER_DOWNLOAD)) {
         	//String artist = intent.getStringExtra(EXTRA_MUSIC_ARTIST);
         	//String title = intent.getStringExtra(EXTRA_MUSIC_TITLE);
         	String url = intent.getStringExtra(EXTRA_MUSIC_URL);
         	String filename = intent.getStringExtra(EXTRA_DOWNLOADER_DOWNLOAD_FILENAME);
         	Debugger.info("Douban service starts with DOWNLOAD command");
         	openDownloader();
         	downloadMusic(url, filename);
         }
         if (action.equals(ACTION_DOWNLOADER_CANCEL)) {
         	String url = intent.getStringExtra(EXTRA_MUSIC_URL);
         	Debugger.info("Douban service starts with CANCEL DOWNLOAD command");
         	openDownloader();
         	cancelDownload(url);
         }
         if (action.equals(ACTION_DOWNLOADER_CLEAR_NOTIFICATION)) {
         	String url = intent.getStringExtra(EXTRA_MUSIC_URL);
         	Debugger.info("Douban service starts with CLEAR NOTIFICATION command");
         	openDownloader();
         	clearDownloadNotification(url);
         }
 
 
         if (action.equals(DoubanFmService.ACTION_WIDGET_UPDATE)) {
         	Debugger.info("Douban service starts with UPDATE_WIDGET");
         	updateWidgets();
         }
         
         // make sure the service will shut down on its own if it was
         // just started but not bound to and nothing is playing
         mDelayedStopHandler.removeCallbacksAndMessages(null);
         Message msg = mDelayedStopHandler.obtainMessage();
         mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
         return START_STICKY;
 		
 		
 	}
 	
 	@Override
 	public void onDestroy() {
 		Debugger.warn("SERVICE ONDESTROY");
        // make sure there aren't any other messages coming
         mDelayedStopHandler.removeCallbacksAndMessages(null);
         
         try {
         	if (phoneCallListener != null)
         		unregisterReceiver(phoneCallListener);
         } catch (Exception e) {
         	
         }
         //mMediaplayerHandler.removeCallbacksAndMessages(null);
 		super.onDestroy();
 	
 	}
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		mDelayedStopHandler.removeCallbacksAndMessages(null);
 		return mBinder;
 	}
 	@Override
 	public void onLowMemory() {
 		Debugger.warn("SERVICE ONLOWMEMORY");
 		closePlayer();
 		closeDownloader();
 	}	
 	private void banMusic() {
 
     }
 
 	//@Override
 	private boolean login(String email, String passwd) {
 		return false;
 	}
 
 	//@Override
 	private void logout() {
 		
 	}
 	
 
 
 	private void openDownloader() {
 		dDownloader.open();
 	}
 	
 	private void closeDownloader() {
 		dDownloader.close();
 	}
 
 	private void openPlayer() {
 		phoneControlListener = new PhoneControlListener();
 		IntentFilter mfilter = new IntentFilter();
 		mfilter.addAction(Intent.ACTION_MEDIA_BUTTON);
 		mfilter.addAction(Intent.ACTION_CAMERA_BUTTON);
 		registerReceiver(phoneControlListener, mfilter);
 		
 		Debugger.verbose("DoubanFm Control Service registered");
 		
 		shakeDetector = new ShakeDetector(this);
 		
 		shakeDetector.registerOnShakeListener(phoneControlListener);
 		
 		try {
 			shakeDetector.start();
 		} catch (Exception e) {
 			Debugger.error("no shake sensor: " + e.toString());
 		}
 
 		// start foreground with notification
 		Notification fgNotification = new Notification(R.drawable.icon,
 				getResources().getString(R.string.app_name),
 		        System.currentTimeMillis());
 		fgNotification.flags |= Notification.FLAG_NO_CLEAR;
 		Intent it = new Intent(DoubanFmService.ACTION_NULL);
 		PendingIntent pi = PendingIntent.getBroadcast(this, 0, it, 	0);
 		fgNotification.setLatestEventInfo(this, "", "", pi);		
 		startForeground(DoubanFmService.SERVICE_NOTIFICATION_ID, fgNotification);
 		
 		
 		dPlayer.open();
 		
 
 	}
 	
 	
 	private void closePlayer() {
 		stopForeground(true);
 		
 		dPlayer.close();
 		
 		try {
 			unregisterReceiver(phoneControlListener);
 		} catch (IllegalArgumentException e) {
 			Debugger.error("media button listener not registered, skip unregistering");
 		}
 		
 		if (shakeDetector != null ) {
 			shakeDetector.unregisterOnShakeListener(phoneControlListener);
 			shakeDetector.stop();
 		}
 		
 		Debugger.debug("DoubanFm Control Service unregistered");
 	}
 	
 	private void closeService() {
 		Debugger.info("closeService");
 		
 		stopSelf();
 	}
 	
 	private void resumeMusic() {
 		if (dPlayer.isOpen()) {
 			// start foreground with notification
 			Notification fgNotification = new Notification(R.drawable.icon,
 					getResources().getString(R.string.app_name),
 			        System.currentTimeMillis());
 			fgNotification.flags |= Notification.FLAG_NO_CLEAR;
 			Intent it = new Intent(DoubanFmService.ACTION_NULL);
 			PendingIntent pi = PendingIntent.getBroadcast(this, 0, it, 	0);
 			fgNotification.setLatestEventInfo(this, "", "", pi);		
 			startForeground(DoubanFmService.SERVICE_NOTIFICATION_ID, fgNotification);
 			
 			dPlayer.resumeMusic();
 		}
 	}
 	
 	private void pauseMusic() {
 		if (dPlayer.isOpen()) {
 			stopForeground(true);
 			dPlayer.pauseMusic();
 		}
 
 	}
 	
 	private void playPauseMusic() {
 		if (dPlayer.isOpen()) {
 			if (dPlayer.isPlaying()) {
 				pauseMusic();
 			}
 			else {
 				resumeMusic();
 			}
 		}
 	}
 	
 	
 
 	
 	//@Override
 	private void selectChannel(int id) {
 
 	}
 	
 	private LoginSession session;
 	
 	private void rateMusic() {
 		if (dPlayer.isOpen()) {
 			dPlayer.rateMusic();
 		}
 	}
 	
 	private void unrateMusic() {
 		if (dPlayer.isOpen()) {
 			dPlayer.unrateMusic();
 		}
 
 	}
 
 	private void nextMusic() {
 		if (dPlayer.isOpen()) {
 			dPlayer.skipMusic();
 		}
 	}
 	
 
 	
 
     
     //@Override
     private void downloadMusic(String url, String filename) {
     	MusicInfo curMusic = null;
     	if (dPlayer.isOpen()) {
     		curMusic = dPlayer.getCurMusic();
    	}
     	
     	if (url == null || url.equals("")) {
     		url = curMusic.musicUrl;
     	}
     	
     	if (filename == null || filename.equals(""))  {
     		filename = Utility.getUnixFilename(curMusic.artist, curMusic.title, url);
     		if (filename == null || filename.equals("")) {
 	    		Debugger.error("can't generate valid file name, abort");
 	    		return;
     		}
     	}
     	Debugger.verbose("download filename should be \"" + filename + "\"");
     	
     	
     	Debugger.verbose("download filename is \"" + filename + "\"");
 
     	if (dDownloader.isOpen()) {
     		dDownloader.download(url, filename);
     	}
     	//pDownloader.download(0, url, filename);
     }
     
     private void cancelDownload(String url) {
     	if (dDownloader.isOpen()) {
     		dDownloader.cancel(url);
     	}
     }
     
     private void clearDownloadNotification(String url) {
     	
     }
     
 
     
     private void popNotify(String msg)
     {
         Toast.makeText(DoubanFmService.this, msg,
                 Toast.LENGTH_LONG).show();
     }
     
     private void updateWidgets() {
     	int selChan = Preference.getSelectedChannel(DoubanFmService.this);
     	WidgetContent content = EasyDoubanFmWidget.getContent(this);
     	MusicInfo curMusic = dPlayer.getCurMusic();
     	Bitmap curPic = dPlayer.getCurPic();
     	Database db = new Database(this);
     	if (dPlayer.isOpen()) {
     		
     		FmChannel chan = db.getChannelInfo(selChan);
     		if (chan != null) {
     			
 		    	String chanName = chan.name;
 	    		
 		    	Debugger.verbose("update widget channel");
 
 		    	content.channel = chanName;
     		}
     	} else {
 
     		content.channel = getResources().getString(R.string.text_channel_unselected);
     	}
     	
     	if (dPlayer.isOpen() && curMusic != null) {
     		Debugger.debug("updating music info");
 
     		content.rated = (curMusic.like.equals("0")? false: true);
     		content.picture = (curPic == null)? 
     				BitmapFactory.decodeResource(getResources(), R.drawable.default_album)
     				: curPic;
     		content.artist = curMusic.artist;
     		content.title = curMusic.title;
     	} else {
 
     		content.rated = false;
 	    	MusicInfo mi = new MusicInfo();
 	    	mi.artist = mi.title = "";
 
 	    	content.picture = BitmapFactory.decodeResource(getResources(), R.drawable.default_album);
     		content.artist = "";
     		content.title = "";
     	}
     	EasyDoubanFmWidget.updateContent(this, content, null);
     }
     
 
 	
 	
 	//private Map<Integer, Notification> notifications = new HashMap<Integer, Notification>(); 
 	
 	public class PhoneCallListener extends BroadcastReceiver {
 		boolean pausedByCall = false;
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
 			
 			if (tm == null) {
 				Debugger.error("null TelephonyManager");
 				return;
 			}
 			if (intent == null) {
 				Debugger.error("null Intent");
 				return;
 			}
 			
 			String action = intent.getAction();
 			if (action == null) {
 				Debugger.error("null Intent Action");
 				return;
 			}
 			
 			if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
 				int state = tm.getCallState();
 				
 				switch(state) {
 				case TelephonyManager.CALL_STATE_IDLE: {
 					Debugger.info("Call state idle");
 					//if (pausedByCall) {
 						pausedByCall = false;
 						resumeMusic();
 						
 					//}
 					
 					break;
 				}
 				case TelephonyManager.CALL_STATE_OFFHOOK: {
 					Debugger.info("Offhook call!");
 					if (dPlayer.isPlaying()) {
 						pausedByCall = true;
 						pauseMusic();
 					}
 					break;
 				}
 				case TelephonyManager.CALL_STATE_RINGING: {
 					Debugger.info("Incoming call!");
 					if (dPlayer.isPlaying()) {
 						pausedByCall = true;
 						pauseMusic();
 					}
 					break;
 				}
 				default:
 					Debugger.error("Invalid call state: " + state);
 					break;
 				}
 				
 
 			}
 		}
 	}
 	   
 }
