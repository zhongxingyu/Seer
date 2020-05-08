 package com.mamewo.malarm24;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
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
 import android.view.KeyEvent;
 
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
 	public String PLAYSTOP_ACTION = PACKAGE_NAME + ".PLAYSTOP_ACTION";
 	final static
 	public String PLAYNEXT_ACTION = PACKAGE_NAME + ".PLAYNEXT_ACTION";
 	final static
 	public String STOP_MUSIC_ACTION = PACKAGE_NAME + ".STOP_MUSIC_ACTION";
 	final static
 	public String LOAD_PLAYLIST_ACTION = PACKAGE_NAME + ".LOAD_PLAYLIST_ACTION";
 	final static
 	public String UNPLUGGED_ACTION = PACKAGE_NAME + ".UNPLUGGED_ACTION";
 	final static
 	public String MEDIA_BUTTON_ACTION = PACKAGE_NAME + ".MEDIA_BUTTON_ACTION";
 	final static
 	public String WAKEUP_PLAYLIST_FILENAME = "wakeup.m3u";
 	final static
 	public String SLEEP_PLAYLIST_FILENAME = "sleep.m3u";
 	final static
 	private int NOTIFY_PLAYING_ID = 1;
 	final private Class<MalarmActivity> userClass_ = MalarmActivity.class;
 
 	//error code from base/include/media/stagefright/MediaErrors.h
 	final static
 	private int MEDIA_ERROR_BASE = -1000;
 	final static
 	private int ERROR_ALREADY_CONNECTED = MEDIA_ERROR_BASE;
 	final static
 	private int ERROR_NOT_CONNECTED = MEDIA_ERROR_BASE - 1;
 	final static
 	private int ERROR_UNKNOWN_HOST = MEDIA_ERROR_BASE - 2;
 	final static
 	private int ERROR_CANNOT_CONNECT = MEDIA_ERROR_BASE - 3;
 	final static
 	private int ERROR_IO = MEDIA_ERROR_BASE - 4;
 	final static
 	private int ERROR_CONNECTION_LOST = MEDIA_ERROR_BASE - 5;
 	final static
 	private int ERROR_MALFORMED = MEDIA_ERROR_BASE - 7;
 	final static
 	private int ERROR_OUT_OF_RANGE = MEDIA_ERROR_BASE - 8;
 	final static
 	private int ERROR_BUFFER_TOO_SMALL = MEDIA_ERROR_BASE - 9;
 	final static
 	private int ERROR_UNSUPPORTED = MEDIA_ERROR_BASE - 10;
 	final static
 	private int ERROR_END_OF_STREAM = MEDIA_ERROR_BASE - 11;
 	// Not technically an error.
 	final static
 	private int INFO_FORMAT_CHANGED = MEDIA_ERROR_BASE - 12;
 	final static
 	private int INFO_DISCONTINUITY = MEDIA_ERROR_BASE - 13;
 	
 	final static
 	private String TAG = "malarm";
 	final static
 	private long VIBRATE_PATTERN[] =
 		{ 10, 1500, 500, 1500, 500, 1500, 500, 1500, 500 };
 	private final IBinder binder_ = new LocalBinder();
 	private Playlist currentPlaylist_;
 	private String currentMusicName_;
 	private MediaPlayer player_;
 	private UnpluggedReceiver receiver_;
 	private int iconId_ = 0;
 	private ComponentName mediaButtonReceiver_;
 
 	static
 	public M3UPlaylist wakeupPlaylist_ = null;
 	static
 	public M3UPlaylist sleepPlaylist_ = null;
 	private Ringtone tone_ = null;
 	private List<PlayerStateListener> listenerList_;
 	private String currentNoteTitle_ = "";
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId){
		String action = null;
		if(null != intent){
			action = intent.getAction();
		}
		if(action == null) {
			return START_STICKY;
		}
 		if(START_WAKEUP_SERVICE_ACTION.equals(action)){
			Log.d(TAG, "onStartCommand: wakeup");
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
 				if (null == tone_) {
 					uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
 					tone_ = RingtoneManager.getRingtone(this, uri);
 				}
 				if (null != tone_) {
 					tone_.play();
 				}
 			}
 			else {
 				currentNoteTitle_ = getString(R.string.notify_wakeup_text);
 				mgr.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
 				Log.d(TAG, "onStartCommand: playMusic");
 				playMusic(wakeupPlaylist_, false);
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
 		}
 		else if (PLAYSTOP_ACTION.equals(action)) {
 			if (isPlaying()) {
 				pauseMusic();
 			}
 			else {
 				if (null == currentPlaylist_) {
 					loadPlaylist();
 					currentPlaylist_ = wakeupPlaylist_;
 				}
 				playMusic(true);
 			}
 		}
 		else if (PLAYNEXT_ACTION.equals(action)) {
 			if (isPlaying()) {
 				playNext();
 			}
 		}
 		else if (LOAD_PLAYLIST_ACTION.equals(action)) {
 			Log.d(TAG, "LOAD_PLAYLIST_ACTION");
 			loadPlaylist();
 		}
 		else if (UNPLUGGED_ACTION.equals(action)) {
 			SharedPreferences pref =
 					PreferenceManager.getDefaultSharedPreferences(this);
 			boolean stop = pref.getBoolean("stop_on_unplugged", true);
 			if (stop) {
 				pauseMusic();
 			}
 		}
 		else if (MEDIA_BUTTON_ACTION.equals(action)) {
 			KeyEvent event = intent.getParcelableExtra("event");
 			Log.d(TAG, "SERVICE: Received media button: " + event.getKeyCode());
 			if (event.getAction() != KeyEvent.ACTION_UP) {
 				return START_STICKY;
 			}
 			switch(event.getKeyCode()) {
 			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
 				if (player_.isPlaying()){
 					pauseMusic();
 				}
 				else {
 					playMusic(true);
 				}
 				break;
 			case KeyEvent.KEYCODE_MEDIA_NEXT:
 				if (player_.isPlaying()) {
 					playNext();
 				}
 				break;
 			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
 				//rewind...
 				playMusic();
 				break;
 			default:
 				break;
 			}
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
 
 	//TODO: show notification if it is shown previous
 	public void playNext() {
 		if (isPlaying()) {
 			stopMusic();
 		}
 		int pos = currentPlaylist_.getCurrentPosition();
 		pos = (pos + 1) % currentPlaylist_.size();
 		currentPlaylist_.setPosition(pos);
 		playMusic();
 	}
 
 	public void onCompletion(MediaPlayer mp) {
 		playNext();
 	}
 
 	private String ErrorCode2String(int err) {
 		String result;
 		switch(err){
 		//TODO: localize?
 		case ERROR_ALREADY_CONNECTED:
 			result = "Already Connected";
 			break;
 		case ERROR_NOT_CONNECTED:
 			result = "Not Connected";
 			break;
 		case ERROR_UNKNOWN_HOST:
 			result = "Unknown Host";
 			break;
 		case ERROR_CANNOT_CONNECT:
 			result = "Cannot Connect";
 			break;
 		case ERROR_IO:
 			result = "I/O Error";
 			break;
 		case ERROR_CONNECTION_LOST:
 			result = "Connection Lost";
 			break;
 		case ERROR_MALFORMED:
 			result = "Malformed Media";
 			break;
 		case ERROR_OUT_OF_RANGE:
 			result = "Out of Range";
 			break;
 		case ERROR_BUFFER_TOO_SMALL:
 			result = "Buffer too Small";
 			break;
 		case ERROR_UNSUPPORTED:
 			result = "Unsupported Media";
 			break;
 		case ERROR_END_OF_STREAM:
 			result = "End of Stream";
 			break;
 		case INFO_FORMAT_CHANGED:
 			result = "Format Changed";
 			break;
 		case INFO_DISCONTINUITY:
 			result = "Info Discontinuity";
 			break;
 		default:
 			result = "Unknown error: " + err;
 			break;
 		}
 		return result;
 	}
 
 	// This method is not called when DRM error occurs
 	public boolean onError(MediaPlayer mp, int what, int extra) {
 		String error = ErrorCode2String(extra);
 		Log.i(TAG, "onError is called, cannot play this media: " + error);
 		MalarmActivity.showMessage(this, error);
 		playNext();
 		//onCompletion is not called
 		return true;
 	}
 
 	public void showNotification(String title, String description, int iconId) {
 		iconId_ = iconId;
 		currentNoteTitle_ = title;
 		Notification note =
 				new Notification(iconId, title, 0);
 		Intent ni = new Intent(this, userClass_);
 		PendingIntent npi = PendingIntent.getActivity(this, 0, ni, 0);
 		note.setLatestEventInfo(this, title, description, npi);
 		startForeground(NOTIFY_PLAYING_ID, note);
 	}
 
 	public void showNotification(String title, String description) {
 		showNotification(title, description, R.drawable.img);
 	}
 
 	public void clearNotification() {
 		stopForeground(true);
 		iconId_ = 0;
 	}
 
 	public void setCurrentPlaylist(Playlist list) {
 		currentPlaylist_ = list;
 	}
 	
 	public Playlist getCurrentPlaylist() {
 		return currentPlaylist_;
 	}
 	
 	/**
 	 * play given playlist from beginning.
 	 * 
 	 * @param playlist playlist to play
 	 * @return true if playlist is played, false if it fails.
 	 */
 	public boolean playMusic(Playlist playlist, boolean notify) {
 		return playMusic(playlist, 0, notify);
 	}
 
 	public boolean playMusic(Playlist playlist, int pos, boolean notify) {
 		currentPlaylist_ = playlist;
 		if(null == playlist){
 			return false;
 		}
 		playlist.setPosition(pos);
 		return playMusic(notify);
 	}
 	
 	public boolean playMusic(boolean playingNotification) {
 		if(playingNotification) {
 			iconId_ = R.drawable.playing;
 			currentNoteTitle_ = getString(R.string.playing);
 		}
 		return playMusic();
 	}
 	
 	public boolean playMusic() {
 		if (null == currentPlaylist_ || currentPlaylist_.isEmpty()) {
 			Log.i(TAG, "playMusic: playlist is null");
 			return false;
 		}
 		//TODO: remove this check
 		if (player_.isPlaying()) {
 			player_.stop();
 		}
 		String path = "";
 		//skip unsupported files filtering by filename ...
 		for (int i = 0; i < 10; i++) {
 			path = currentPlaylist_.getURL();
 			if (path.startsWith("http://")) {
 				break;
 			}
 			File f = new File(path);
 			// ... m4p is protected audio file
 			if ((!path.endsWith(".m4p")) && f.exists()) {
 				break;
 			}
 			int pos = currentPlaylist_.getCurrentPosition();
 			pos = (pos + 1) % currentPlaylist_.size();
 			currentPlaylist_.setPosition(pos);
 		}
 		Log.i(TAG, "playMusic: " + path);
 		//TODO: get title from file
 		currentMusicName_ = (new File(path)).getName();
 		try {
 			player_.reset();
 			player_.setDataSource(path);
 			player_.prepare();
 			player_.start();
 			for (PlayerStateListener listener: listenerList_) {
 				listener.onStartMusic(currentMusicName_);
 			}
 			if (iconId_ != 0) {
 				//TODO: modify notification title
 				showNotification(currentNoteTitle_, currentMusicName_, iconId_);
 			}
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
 			for (PlayerStateListener listener: listenerList_) {
 				listener.onStopMusic();
 			}
 		}
 		//umm...
 		if(iconId_ == R.drawable.playing) {
 			clearNotification();
 		}
 		//clear music title
 		if (iconId_ == R.drawable.img) {
 			showNotification(currentNoteTitle_, "");
 		}
 	}
 
 	public void pauseMusic() {
 		if(! player_.isPlaying()) {
 			return;
 		}
 		player_.pause();
 		for (PlayerStateListener listener: listenerList_) {
 			listener.onStopMusic();
 		}
 		//umm...
 		if(iconId_ == R.drawable.playing) {
 			clearNotification();
 		}
 		//clear music title
 		if (iconId_ == R.drawable.img) {
 			showNotification(currentNoteTitle_, "");
 		}
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
 		listenerList_ = new ArrayList<PlayerStateListener>();
 		tone_ = null;
 		currentMusicName_ = null;
 		loadPlaylist();
 		currentPlaylist_ = wakeupPlaylist_;
 		player_ = new MediaPlayer();
 		player_.setOnCompletionListener(this);
 		player_.setOnErrorListener(this);
 		receiver_ = new UnpluggedReceiver();
 		registerReceiver(receiver_, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
 		AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		mediaButtonReceiver_ = new ComponentName(getPackageName(), UnpluggedReceiver.class.getName());
 		manager.registerMediaButtonEventReceiver(mediaButtonReceiver_);
 	}
 	
 	@Override
 	public void onDestroy(){
 		AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		manager.unregisterMediaButtonEventReceiver(mediaButtonReceiver_);
 		unregisterReceiver(receiver_);
 		player_ = null;
 		currentPlaylist_ = null;
 		wakeupPlaylist_ = null;
 		sleepPlaylist_ = null;
 		super.onDestroy();
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
 	
 	final static
 	public class UnpluggedReceiver
 		extends BroadcastReceiver
 	{
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			if(null == action) {
 				return;
 			}
 			if(Intent.ACTION_HEADSET_PLUG.equals(action)) {
 				if(intent.getIntExtra("state", 1) == 0){
 					Intent i = new Intent(context, MalarmPlayerService.class);
 					i.setAction(UNPLUGGED_ACTION);
 					context.startService(i);
 				}
 			}
 			else if(Intent.ACTION_MEDIA_BUTTON.equals(action)){
 				Log.d(TAG, "media button");
 				KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
 				if (null == event) {
 					return;
 				}
 				Intent i = new Intent(context, MalarmPlayerService.class);
 				i.setAction(MEDIA_BUTTON_ACTION);
 				i.putExtra("event", event);
 				context.startService(i);
 			}
 		}
 	}
 	
 	public void addPlayerStateListener(PlayerStateListener listener) {
 		listenerList_.add(listener);
 	}
 	
 	public void removePlayerStateListener(PlayerStateListener listener) {
 		listenerList_.remove(listener);
 	}
 
 	public interface PlayerStateListener {
 		public void onStartMusic(String title);
 		public void onStopMusic();
 	}
 }
