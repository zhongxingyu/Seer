 package com.tufts.wmfo;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.BitmapFactory;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnInfoListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.net.ConnectivityManager;
 import android.net.wifi.WifiManager;
 import android.net.wifi.WifiManager.WifiLock;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import android.widget.Toast;
 
 public class AudioService extends Service implements AudioManager.OnAudioFocusChangeListener, OnPreparedListener{
 
 	//Service-wide tools
 	WifiLock wifiLock;
 	MediaPlayer mediaPlayer;
 	Timer updateCurrentTimer;
 	public static Boolean isRunning;
 	SharedPreferences appPreferences;
 	SongInfo CurrentSong;
 	long lastSawInternet;
 	boolean connectedOK;
 	boolean switching;
 	String currentConnection;
 	String mediaSourceURL;
 	public static Boolean isLive;
 	NotificationManager ourNotificationManager;
 
 	ConnectivityManager connManager;
 
 	final static String TAG = "WMFO:SERVICE";
 
 	@Override
 	public void onCreate() {
 		Log.d(TAG, "onCreate");
 		isRunning = true;
 		isLive = false;
 		ourNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 		appPreferences = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
 		updateCurrentTimer = new Timer();
 		//registerReceiver(networkBroadCastReciever, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
 		connectedOK = true;
 		switching = false;
 		if (connManager != null){
 			currentConnection = connManager.getActiveNetworkInfo().getTypeName();
 		}
 		Log.d("WMFO:SERVICE", "Connected to " + currentConnection);
 		this.lastSawInternet = System.currentTimeMillis();
 	}
 
 	@Override
 	public void onDestroy() {
 		isRunning = false;
 		Log.d(TAG, "onDestroy");
 		if (updateCurrentTimer != null){
 			updateCurrentTimer.cancel();
 		}
 		if (mediaPlayer != null){
 			if (mediaPlayer.isPlaying()){
 				mediaPlayer.stop();
 			}
 			mediaPlayer.release();
 			mediaPlayer = null;
 		}
 		
 		if (ourNotificationManager != null){
			Log.d(TAG, "Cancelling all notifications");
			ourNotificationManager.cancelAll();
 		}
 
 		if (wifiLock != null && wifiLock.isHeld()){
 			wifiLock.release();
 		}
 	}
 
 	@Override
 	public void onStart(Intent intent, int startid) {
 		Log.d(TAG, "onStart");
 		if (intent == null){
 			Log.e("AudioService", "Intent passed to audio service was null!");
 			this.stopSelf();
 		} else {
 			Bundle extras = intent.getExtras();
 			if (extras.containsKey("source")){
 				if (extras.getString("source").equals(getString(R.string.WMFO_STREAM_URL_HQ))){
 					this.isLive = true;
 					if (connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected() || !appPreferences.getBoolean("dropQuality", false)) {
 						Log.d(TAG, "Wifi On or no fallback");
 						String qualityLevel = appPreferences.getString("qualityLevel", "256");
 						if (qualityLevel.equals("256")){
 							this.mediaSourceURL = getString(R.string.WMFO_STREAM_URL_HQ);
 						} else if (qualityLevel.equals("128")){
 							this.mediaSourceURL = getString(R.string.WMFO_STREAM_URL_MQ);
 						} else if (qualityLevel.equals("64")){
 							this.mediaSourceURL = getString(R.string.WMFO_STREAM_URL_LQ);
 						} else {
 							//dafuq
 							this.mediaSourceURL = getString(R.string.WMFO_STREAM_URL_HQ);
 						}
 					} else {
 						Log.d(TAG, "Wifi Off and fallback");
 						String qualityLevel = appPreferences.getString("qualityFallbackLevel", "128");
 						if (qualityLevel.equals("256")){
 							this.mediaSourceURL = getString(R.string.WMFO_STREAM_URL_HQ);
 						} else if (qualityLevel.equals("128")){
 							this.mediaSourceURL = getString(R.string.WMFO_STREAM_URL_MQ);
 						} else if (qualityLevel.equals("64")){
 							this.mediaSourceURL = getString(R.string.WMFO_STREAM_URL_LQ);
 						} else {
 							//dafuq
 							this.mediaSourceURL = getString(R.string.WMFO_STREAM_URL_HQ);
 						}
 					}
 				} else {
 					this.mediaSourceURL = extras.getString("source");
 				}
 
 			} else {
 				this.isLive = false;
 			}
 			initMediaPlayer();
 		}
 	}
 
 	public void onPrepared(MediaPlayer mp) {
 		mp.start();
 		connectedOK = true;
 		switching = false;
 		currentConnection = connManager.getActiveNetworkInfo().getTypeName();
 		Log.d("WMFO:SERVICE", "Prepared on " + currentConnection);
 		updateCurrentTimer.scheduleAtFixedRate(new TimerTask(){
 			@Override
 			public void run() {
 				updateNowPlaying();
 			}}, 0, 5000);
 		Log.d(TAG, "onPrepared");
 	}
 
 	public void setNotification(SongInfo... nowPlaying){
 
 		NotificationCompat.Builder notificationBuilder =
 				new NotificationCompat.Builder(this)
 		.setSmallIcon(R.drawable.ic_notification)
 		.setOngoing(true);
 
 		if (nowPlaying.length > 0 && (nowPlaying[0].artwork_large != null || nowPlaying[0].artwork_medium != null)){
 			Log.d("WMFO", "Song has art - adding");
 			if (nowPlaying[0].artwork_large!= null) {
 				notificationBuilder.setLargeIcon(nowPlaying[0].artwork_large);
 			} else {
 				notificationBuilder.setLargeIcon(nowPlaying[0].artwork_medium);
 			}
 		} else {
 			notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.white_icon));
 		}
 
 		if (mediaPlayer != null && mediaPlayer.isPlaying()){
 			notificationBuilder.setContentTitle((nowPlaying.length > 0) ? nowPlaying[0].artist : "Now Listening To WMFO");
 			notificationBuilder.setContentText((nowPlaying.length > 0) ? nowPlaying[0].title : "Currently streaming");
			if (nowPlaying.length > 0){
				notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
		         .bigText(nowPlaying[0].title + " by " + nowPlaying[0].artist + "\n" + "From " + nowPlaying[0].album + "\n" + "Spun by " + nowPlaying[0].DJ + " on " + nowPlaying[0].showName));
			}
			
 		} else {
 			notificationBuilder.setTicker("Loading WMFO Stream...");
 			notificationBuilder.setContentTitle("Loading WMFO Stream...");
 			notificationBuilder.setContentText("Buffering...");
 		}
 
 		notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0,
 				new Intent(this, MainActivity.class), 0));
 
 		if (ourNotificationManager != null){
 			ourNotificationManager.notify(R.id.WMFO_NOTIFICATION_ID, notificationBuilder.build());
 		}
 
 	}
 
 	public void showStreamError(){
 		Notification notification = new Notification();
 		notification.icon = R.drawable.white_icon;
 		notification.flags |= Notification.FLAG_ONGOING_EVENT;
 
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 				new Intent(this, MainActivity.class), 0);
 		notification.tickerText = "WMFO Streaming Error";
 		notification.setLatestEventInfo(this, "Stream Error" , "Could not play the stream", contentIntent);
 		startForeground(R.id.WMFO_NOTIFICATION_ID, notification);
 	}
 
 	public void initMediaPlayer(){
 		switching = true;
 		//Lock WIFI on
 		wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
 		wifiLock.acquire();
 
 		mediaPlayer = new MediaPlayer();
 		mediaPlayer.setOnErrorListener(new OnErrorListener(){
 			@Override
 			public boolean onError(MediaPlayer mp, int what, int extra) {
 				Log.e("WMFO:MEDIA", "Media player has crashed! Code " + what + " Extra: " + extra);
 				updateCurrentTimer.cancel();
 				showStreamError();
				if (mediaPlayer != null){
					mediaPlayer.release();
					mediaPlayer = null;
				}
 				AudioService.this.stopSelf();
 				return true;
 			}});
 		mediaPlayer.setOnPreparedListener(this);
 		mediaPlayer.setOnInfoListener(new OnInfoListener(){
 			@Override
 			public boolean onInfo(MediaPlayer mp, int what, int extra) {
 				Log.d("WMFO:MEDIA", "Info available: " + what + ", extra: " + extra);
 				if(what == 703){
 					Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_buffering), Toast.LENGTH_LONG).show();
 					return true;
 				}
 				return false;
 			}});
 		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 
 		try {
 			mediaPlayer.setDataSource(this.mediaSourceURL);
 			mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
 			mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
 			setNotification();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}       
 		//To nullify the player, use mediaPlayer.release();
 	}
 
 	public void onAudioFocusChange(int focusChange) {
 		switch (focusChange) {
 		case AudioManager.AUDIOFOCUS_GAIN:
 			// resume playback
 			if (mediaPlayer == null) initMediaPlayer();
 			else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
 			mediaPlayer.setVolume(1.0f, 1.0f);
 			break;
 
 		case AudioManager.AUDIOFOCUS_LOSS:
 			// Lost focus for an unbounded amount of time: stop playback and release media player
 			if (mediaPlayer.isPlaying()) mediaPlayer.stop();
 			mediaPlayer.release();
 			mediaPlayer = null;
 			break;
 
 		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
 			// Lost focus for a short time, but we have to stop
 			// playback. We don't release the media player because playback
 			// is likely to resume
 			if (mediaPlayer.isPlaying()) mediaPlayer.pause();
 			break;
 
 		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
 			// Lost focus for a short time, but it's ok to keep playing
 			// at an attenuated level
 			if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
 			break;
 		}
 	}
 
 	void updateNowPlaying(){
 		new Thread(new Runnable(){
 			@Override
 			public void run() {
 				String SpinInfo = null;
 				try {
 					//SpinInfo = executeGET("http://wmfo-duke.orgs.tufts.edu:8000/7.html");
 					SpinInfo = Network.getURL("http://spinitron.com/public/newestsong.php?station=wmfo");
 				} catch (ClientProtocolException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (URISyntaxException e) {
 					e.printStackTrace();
 				}
 				if (SpinInfo != null && SpinInfo != ""){
 					AudioService.this.connectedOK=true;
 					SongInfo nowPlaying = new SongInfo(SpinInfo, true);
 
 					if (CurrentSong == null || !CurrentSong.equals(nowPlaying)){
 						SongInfo oldSong = CurrentSong;
 						CurrentSong = nowPlaying;
 
 						if (oldSong != null){
 							Log.d("WMFO:SERVICE", "Old song (" + oldSong.title + ") over, now scrobbling it and playing " + nowPlaying.title);
 							if (appPreferences.getBoolean("lastFMScrobble", false)){
 								
 								if (isLive) { 
 									Log.i("WMFO:SCROBBLE", "Enabled, scrobbling");
 									new ScrobbleRequest(AudioService.this, oldSong).send(); 
 								} else {
 									Log.d("WMFO:SCROBBLE", "Enabled but not live - not scrobbling");
 								}
 							} else {
 								Log.i("WMFO:SCROBBLE", "Not enabled, not scrobbling");
 							}
 						}
 
 						/*
 						 * Try and fetch album art from last.fm
 						 */
 						if (isLive) {
 							Log.d("WMFO:ART", "Detected track change, trying to get art");
 							JSONObject albumInfo = LastFM.getAlbumInfo(AudioService.this, nowPlaying.artist, nowPlaying.album);
 							if (albumInfo.has("album")){
 								try {
 									albumInfo = albumInfo.getJSONObject("album");
 									if (albumInfo.has("image")){
 										JSONArray images = albumInfo.getJSONArray("image");
 										nowPlaying.parseLastFMAlbumArt(images);
 									}
 								} catch (JSONException e) {
 									e.printStackTrace();
 								}
 							}
 						}
 					}
 
 					if (isLive) { 
 						setNotification(CurrentSong);
 					} else {
 						nowPlaying.title = "Playing archives";
 						nowPlaying.artist = "WMFO";
 						setNotification(nowPlaying);
 					}
 
					if (isLive && appPreferences.getBoolean("lastFMScrobble", false)) { new LastFMNowPlayingRequest(AudioService.this, nowPlaying).send(); }
 
 				} else {
 					AudioService.this.connectedOK=false;
 				}
 			}}).start();
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	//	private BroadcastReceiver networkBroadCastReciever =
 	//			new BroadcastReceiver() {
 
 	//		@Override
 	//		public void onReceive(Context context, Intent intent) {
 	//			if(intent.getExtras()!=null) {
 	//				NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
 	//				if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED) {
 	//					Log.i("WMFO:NET","Network "+ni.getTypeName()+" connected");
 	//					if (!AudioService.this.connectedOK && (System.currentTimeMillis() - AudioService.this.lastSawInternet) < 15000){
 	//						Log.d("WMFO:NET", "Less than 15 seconds since we lost data, reconnect");
 	//						AudioService.this.mediaPlayer.release();
 	//						AudioService.this.mediaPlayer = null;
 	//						initMediaPlayer();
 	//					} else if (ni.getTypeName().equals("WIFI") && !AudioService.this.switching){
 	//						if (!AudioService.this.currentConnection.equals("WIFI")){
 	//							Log.d("WMFO:NET", "Found WIFI! Connect to it");
 	//							AudioService.this.lastSawInternet = System.currentTimeMillis();
 	//							AudioService.this.mediaPlayer.release();
 	//							AudioService.this.mediaPlayer = null;
 	//							initMediaPlayer();
 	//						} else {
 	//							//Do nothing
 	//						}
 	//					} else if (ni.getTypeName().equals("mobile_mms")){
 	//						//Ignore
 	//					} else if ((System.currentTimeMillis() - AudioService.this.lastSawInternet) > 15000){
 	//						Log.d("WMFO:NET", "Too much time since we last had internet (" + (System.currentTimeMillis() - AudioService.this.lastSawInternet) + ") - stop");
 	//						updateCurrentTimer.cancel();
 	//						showStreamError();
 	//						AudioService.this.stopSelf();
 	//					}
 	//				}
 	//			}
 	//			if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
 	//				Log.d("WMFO:NET", "Setting last saw audio time to " + System.currentTimeMillis());
 	//				AudioService.this.lastSawInternet = System.currentTimeMillis();
 	//			}
 	//		}
 
 	//	};
 
 
 }
