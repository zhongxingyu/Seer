 package com.mamewo.malarm24;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.IBinder;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 /**
  * @author Takashi Masuyama <mamewotoko@gmail.com>
  * http://www002.upp.so-net.ne.jp/mamewo/
  */
 public class MalarmPlayerService
 	extends Service
 {
 	final static
 	public String PACKAGE_NAME = MalarmActivity.class.getPackage().getName();
 	final static
 	public String WAKEUP_ACTION = PACKAGE_NAME + ".WAKEUP_ACTION";
 	final static
 	public String WAKEUPAPP_ACTION = PACKAGE_NAME + ".WAKEUPAPP_ACTION";
 	final static
 	public String SLEEP_ACTION = PACKAGE_NAME + ".SLEEP_ACTION";
 	final static
 	public String STOP_MUSIC_ACTION = PACKAGE_NAME + ".STOP_MUSIC_ACTION";
 	final static
 	public String LOAD_PLAYLIST_ACTION = PACKAGE_NAME + ".LOAD_PLAYLIST_ACTION";
 	final static
 	public String WAKEUP_PLAYLIST_FILENAME = "wakeup.m3u";
 	final static
 	public String SLEEP_PLAYLIST_FILENAME = "sleep.m3u";
 
 	final static
 	private String TAG = "malarm";
 	final static
 	private long VIBRATE_PATTERN[] =
 		{ 10, 1500, 500, 1500, 500, 1500, 500, 1500, 500 };
 	private final IBinder binder_ = new LocalBinder();
 	private Playlist currentPlaylist_;
 	private MediaPlayer player_;
 
 	//TODO: umm...
 	static
 	public M3UPlaylist wakeupPlaylist_;
 	static
 	public M3UPlaylist sleepPlaylist_;
 	private Ringtone tone_;
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId){
 		String action = intent.getAction();
 		if(WAKEUPAPP_ACTION.equals(action)){
 			Log.i(TAG, "onStartCommand: wakeup!: " + wakeupPlaylist_);
 			loadPlaylist();
 			SharedPreferences pref =
 					PreferenceManager.getDefaultSharedPreferences(this);
 			int volume = Integer.valueOf(pref.getString("wakeup_volume", "5"));
 			AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 			stopMusic();
 			mgr.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
 			if(null == wakeupPlaylist_){
 				//TODO: test volume
 				Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
 				tone_ = RingtoneManager.getRingtone(this, uri);
 				tone_.play();
 			}
 			else {
 				playMusic(wakeupPlaylist_);
 			}
 			boolean vibrate =
 					pref.getBoolean("vibrate", MalarmPreference.DEFAULT_VIBRATION);
 			if (vibrate) {
 				startVibrator();
 			}
 			Intent activityIntent = new Intent(this, MalarmActivity.class);
 			activityIntent.setAction(WAKEUP_ACTION);
 			activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			startActivity(activityIntent);
 		}
 		else if (STOP_MUSIC_ACTION.equals(action)) {
 			//TODO: support native player
 			stopMusic();
 			//TODO: quit service
 		}
 		else if (LOAD_PLAYLIST_ACTION.equals(action)) {
 			Log.i(TAG, "LOAD_PLAYLIST_ACTION");
 			loadPlaylist();
 		}
 		return START_STICKY;
 	}
 	
 	public void loadPlaylist() {
 		SharedPreferences pref =
 				PreferenceManager.getDefaultSharedPreferences(this);
 		String playlistPath = pref.getString("playlist_path", "/sdcard/music");
 		Log.i(TAG, "loadPlaylist is called:" + playlistPath);
 		try {
 			wakeupPlaylist_ = new M3UPlaylist(playlistPath, WAKEUP_PLAYLIST_FILENAME);
 		}
 		catch (FileNotFoundException e) {
 			Log.i(TAG, "wakeup playlist is not found: " + WAKEUP_PLAYLIST_FILENAME);
 			wakeupPlaylist_ = null;
 		}
 		catch (IOException e) {
 			Log.i(TAG, "wakeup playlist cannot be load: " + WAKEUP_PLAYLIST_FILENAME);
 			wakeupPlaylist_ = null;
 		}
 		try {
 			sleepPlaylist_ = new M3UPlaylist(playlistPath, SLEEP_PLAYLIST_FILENAME);
 		}
 		catch (FileNotFoundException e) {
 			Log.i(TAG, "sleep playlist is not found: " + SLEEP_PLAYLIST_FILENAME);
 			sleepPlaylist_ = null;
 		}
 		catch (IOException e) {
 			Log.i(TAG, "sleep playlist cannot be load: " + WAKEUP_PLAYLIST_FILENAME);
 			sleepPlaylist_ = null;
 		}
 	}
 	
 	public boolean isPlaying() {
 		return player_.isPlaying();
 	}
 
 	//not used..
 	public void playMusicNativePlayer(Context context, File f) {
 		Intent i = new Intent();
 		i.setAction(Intent.ACTION_VIEW);
 		i.setDataAndType(Uri.fromFile(f), "audio/*");
 		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		context.startActivity(i);
 	}
 
 	public void playNext() {
 		Log.i(TAG, "playNext is called: ");
 		if (isPlaying()) {
 			stopMusic();
 		}
		playMusic(currentPlaylist_);
 	}
 
 	public class MusicCompletionListener
 	implements MediaPlayer.OnCompletionListener,
 				MediaPlayer.OnErrorListener
 	{
 		public void onCompletion(MediaPlayer mp) {
 			Log.i(TAG, "onCompletion listener is called");
 			playNext();
 		}
 
 		// This method is not called when DRM error occurs
 		public boolean onError(MediaPlayer mp, int what, int extra) {
 			//TODO: show error message to GUI
 			Log.i(TAG, "onError is called, cannot play this media");
 			playNext();
 			return true;
 		}
 	}
 
 	/**
 	 * play given playlist from beginning.
 	 * 
 	 * @param playlist playlist to play
 	 * @return true if playlist is played, false if it fails.
 	 */
 	public boolean playMusic(Playlist playlist) {
 		currentPlaylist_ = playlist;
 		if(null == playlist){
 			return false;
 		}
 		playlist.reset();
 		return playMusic();
 	}
 
 	public boolean playMusic() {
 		Log.i(TAG, "playMusic");
 		if (currentPlaylist_ == null || currentPlaylist_.isEmpty()) {
 			Log.i(TAG, "playMusic: playlist is null");
 			return false;
 		}
 		if (player_.isPlaying()) {
 			return false;
 		}
 		String path = "";
 		//skip unsupported files filtering by filename ...
 		for (int i = 0; i < 10; i++) {
 			path = currentPlaylist_.next();
 			File f = new File(path);
 			// ... m4p is protected audio file
 			if ((!path.endsWith(".m4p")) && f.exists()) {
 				break;
 			}
 		}
 		try {
 			player_.reset();
 			player_.setDataSource(path);
 			player_.prepare();
 			player_.start();
 		}
 		catch (IOException e) {
 			return false;
 		}
 		return true;
 	}
 
 	public void stopMusic() {
 		if(null != tone_){
 			tone_.stop();
 		}
 		player_.stop();
 	}
 
 	public void pauseMusic() {
 		Log.i(TAG, "pause music is called");
 		try {
 			player_.pause();
 		}
 		catch (Exception e) {
 			//do nothing
 		}
 		//clearNotification();
 	}
 	
 	public void quit(){
 		stopSelf();
 	}
 
 	public void startVibrator() {
 		final Vibrator vibrator = 
 				(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		if (vibrator == null) {
 			return;
 		}
 		vibrator.vibrate(VIBRATE_PATTERN, 1);
 	}
 	
 	public void stopVibrator() {
 		final Vibrator vibrator =
 				(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		if (vibrator == null) {
 			return;
 		}
 		vibrator.cancel();
 	}
 	
 	public class LocalBinder
 		extends Binder
 	{
 		public MalarmPlayerService getService() {
 			return MalarmPlayerService.this;
 		}
 	}
 	
 	@Override
 	public void onCreate(){
 		super.onCreate();
 		tone_ = null;
 
 		loadPlaylist();
 		currentPlaylist_ = wakeupPlaylist_;
 		player_ = new MediaPlayer();
 		MusicCompletionListener l = new MusicCompletionListener();
 		player_.setOnCompletionListener(l);
 		player_.setOnErrorListener(l);
 	}
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return binder_;
 	}
 
 	final static
 	public class Receiver
 		extends BroadcastReceiver
 	{
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			Log.i(TAG, "onReceive: " + action);
 			if(null == action){
 				return;
 			}
 			if(WAKEUP_ACTION.equals(action)){
 				Log.i(TAG, "onReceive is called(malarm24): action: " + action);
 				Intent i = new Intent(context, MalarmPlayerService.class);
 				i.setAction(WAKEUPAPP_ACTION);
 				context.startService(i);
 			}
 			else if(SLEEP_ACTION.equals(action)){
 				//TODO: support native player
 				Intent i = new Intent(context, MalarmPlayerService.class);
 				i.setAction(STOP_MUSIC_ACTION);
 				context.startService(i);
 			}
 		}
 	}
 }
