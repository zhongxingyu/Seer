 package com.mamewo.malarm24;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import android.app.Notification;
 import android.app.PendingIntent;
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
 	implements MediaPlayer.OnCompletionListener,
 	MediaPlayer.OnErrorListener
 {
 	final static
 	public String PACKAGE_NAME = MalarmActivity.class.getPackage().getName();
 	final static
 	public String WAKEUP_ACTION = PACKAGE_NAME + ".WAKEUP_ACTION";
 	final static
 	public String START_WAKEUP_SERVICE_ACTION = PACKAGE_NAME + ".START_WAKEUP_SERVICE_ACTION";
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
 	private int NOTIFY_PLAYING_ID = 1;
 	final private Class<MalarmActivity> userClass_ = MalarmActivity.class;
 	
 	final static
 	private String TAG = "malarm";
 	final static
 	private long VIBRATE_PATTERN[] =
 		{ 10, 1500, 500, 1500, 500, 1500, 500, 1500, 500 };
 	private final IBinder binder_ = new LocalBinder();
 	private Playlist currentPlaylist_;
 	private MediaPlayer player_;
 
 	static
 	public M3UPlaylist wakeupPlaylist_;
 	static
 	public M3UPlaylist sleepPlaylist_;
 	private Ringtone tone_;
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId){
 		String action = intent.getAction();
 		if(START_WAKEUP_SERVICE_ACTION.equals(action)){
 			Log.d(TAG, "onStartCommand: wakeup!: " + wakeupPlaylist_);
 			loadPlaylist();
 			SharedPreferences pref =
 					PreferenceManager.getDefaultSharedPreferences(this);
 			int volume = Integer.valueOf(pref.getString(MalarmPreference.PREFKEY_WAKEUP_VOLUME,
 					MalarmPreference.DEFAULT_WAKEUP_VOLUME));
 			AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 			stopMusic();
 			if(null == wakeupPlaylist_){
 				//TODO: test volume
 				mgr.setStreamVolume(AudioManager.STREAM_RING, volume, AudioManager.FLAG_SHOW_UI);
 				Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
 				tone_ = RingtoneManager.getRingtone(this, uri);
				tone_.play();
 			}
 			else {
 				mgr.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
 				Log.d(TAG, "onStartCommand: playMusic");
 				playMusic(wakeupPlaylist_);
 			}
 			boolean vibrate =
 					pref.getBoolean(MalarmPreference.PREFKEY_VIBRATE, MalarmPreference.DEFAULT_VIBRATION);
 			if (vibrate) {
 				startVibrator();
 			}
 			Intent activityIntent = new Intent(this, MalarmActivity.class);
 			activityIntent.setAction(WAKEUP_ACTION);
 			activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			startActivity(activityIntent);
 		}
 		else if (STOP_MUSIC_ACTION.equals(action)) {
 			stopMusic();
 			//TODO: stop if activity is dead / stop activity
 		}
 		else if (LOAD_PLAYLIST_ACTION.equals(action)) {
 			Log.d(TAG, "LOAD_PLAYLIST_ACTION");
 			loadPlaylist();
 		}
 		return START_STICKY;
 	}
 	
 	public void loadPlaylist() {
 		SharedPreferences pref =
 				PreferenceManager.getDefaultSharedPreferences(this);
 		String playlistPath = pref.getString(MalarmPreference.PREFKEY_PLAYLIST_PATH,
 				MalarmPreference.DEFAULT_PLAYLIST_PATH.getAbsolutePath());
 		Log.d(TAG, "loadPlaylist is called:" + playlistPath);
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
 		if (isPlaying()) {
 			stopMusic();
 		}
 		playMusic();
 	}
 
 	public void onCompletion(MediaPlayer mp) {
 		playNext();
 	}
 
 	// This method is not called when DRM error occurs
 	public boolean onError(MediaPlayer mp, int what, int extra) {
 		//TODO: show error message to GUI
 		Log.i(TAG, "onError is called, cannot play this media");
 		//TODO: call playNext if error occurred while playing music
 		playNext();
 		return true;
 	}
 
 	public void showNotification(String title, String description) {
 		Notification note =
 				new Notification(R.drawable.img, title, 0);
 		Intent ni = new Intent(this, userClass_);
 		PendingIntent npi = PendingIntent.getActivity(this, 0, ni, 0);
 		//TODO: localize
 		note.setLatestEventInfo(this, title, description, npi);
 		startForeground(NOTIFY_PLAYING_ID, note);
 	}
 	
 	public void clearNotification() {
 		stopForeground(true);
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
		Log.d(TAG, "playMusic playlist: playMusic");
		return playMusic();
 	}
 
 	public boolean playMusic(Playlist playlist, int pos) {
 		currentPlaylist_ = playlist;
 		if(null == playlist){
 			return false;
 		}
 		playlist.setPosition(pos);
 		Log.d(TAG, "playMusic playlist: playMusic pos");
 		return playMusic();
 	}
 	
 	public boolean playMusic() {
 		if (null == currentPlaylist_ || currentPlaylist_.isEmpty()) {
 			Log.i(TAG, "playMusic: playlist is null");
 			return false;
 		}
 		//TODO: remove this check
 		if (player_.isPlaying()) {
			return false;
 		}
 		String path = "";
 		//skip unsupported files filtering by filename ...
 		for (int i = 0; i < 10; i++) {
 			Log.d(TAG, "checking: " + path);
 			path = currentPlaylist_.next();
 			if (path.startsWith("http://")) {
 				break;
 			}
 			File f = new File(path);
 			// ... m4p is protected audio file
 			if ((!path.endsWith(".m4p")) && f.exists()) {
 				break;
 			}
 		}
 		Log.i(TAG, "playMusic: " + path);
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
 		if(player_.isPlaying()){
 			player_.stop();
 		}
 	}
 
 	public void pauseMusic() {
 		if(player_.isPlaying()){
 			player_.pause();
 		}
 		//clearNotification();
 	}
 	
 	public void startVibrator() {
 		Vibrator vibrator = 
 				(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		if (null == vibrator) {
 			return;
 		}
 		vibrator.vibrate(VIBRATE_PATTERN, 1);
 	}
 	
 	public void stopVibrator() {
 		Vibrator vibrator =
 				(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		if (null == vibrator) {
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
 		player_.setOnCompletionListener(this);
 		player_.setOnErrorListener(this);
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
 				Intent i = new Intent(context, MalarmPlayerService.class);
 				i.setAction(START_WAKEUP_SERVICE_ACTION);
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
