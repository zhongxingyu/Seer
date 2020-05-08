 package com.code_pig.pocketfma;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.net.wifi.WifiManager;
 import android.net.wifi.WifiManager.WifiLock;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Service which handles media playback.
  */
 public class MusicService extends Service implements OnCompletionListener,
 		OnPreparedListener, OnErrorListener, MusicFocusable,
 		MusicRetrieverTask.MusicRetrieverPreparedListener {
 	final static String TAG = "MusicService"; // for debugging
 
 	// Intent Actions
 	public static final String ACTION_TOGGLE_PLAYBACK = "com.example.android.musicplayer.action.TOGGLE_PLAYBACK";
 	public static final String ACTION_PLAY = "com.example.android.musicplayer.action.PLAY";
 	public static final String ACTION_PAUSE = "com.example.android.musicplayer.action.PAUSE";
 	public static final String ACTION_STOP = "com.example.android.musicplayer.action.STOP";
 	public static final String ACTION_SKIP = "com.example.android.musicplayer.action.SKIP";
 	public static final String ACTION_REWIND = "com.example.android.musicplayer.action.REWIND";
 	public static final String ACTION_QUERY = "com.example.android.musicplayer.action.QUERY";
 
 	public static final float DUCK_VOLUME = 0.1f;
 
 	final int NOTIFICATION_ID = 1;
 
 	private WifiLock wifiLock;
 	private MusicRetriever retriever;
 	private MediaPlayer player = null;
 	private AudioFocusHelper audioFocusHelper = null;
 	private String currentTrack = null;
 	private String nextTrack = null;
 	
     boolean startPlayingAfterRetrieve = false;
     String whatToPlayAfterRetrieve = null;
 
 	// Service states
 	enum State {
 		Retrieving, Stopped, Preparing, Playing, Paused
 	}
 
 	// Pause reasons
 	enum PauseReason {
 		UserRequest, FocusLoss
 	}
 
 	// Audio focus status
 	enum AudioFocus {
 		NoFocusNoDuck, NoFocusDoDuck, Focused
 	}
 
 	State state = State.Retrieving;
 	PauseReason pauseReason = PauseReason.UserRequest;
 	AudioFocus audioFocus = AudioFocus.NoFocusNoDuck;
 
 	String currentSongTitle = "";
 
 	/**
 	 * Creates or resets media player.
 	 */
 	void createOrResetMediaPlayer() {
 		Log.i(TAG, "createOrResetMediaPlayer() called");
 		if (player == null) {
 			Log.i(TAG, "MediaPlayer created");
 			player = new MediaPlayer();
 			player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
 			player.setOnPreparedListener(this);
 			player.setOnCompletionListener(this);
 			player.setOnErrorListener(this);
 		} else {
 			Log.i(TAG, "MediaPlayer reset");
 			player.reset();
 		}
 	}
 	
 	/**
 	 * Create the Service. Called on instantiation of this class.
 	 */
 	@Override
 	public void onCreate() {
 		Log.i(TAG, "onCreate() called");
 		// We want the airwaves!
 		wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
 		retriever = new MusicRetriever();
 		// Set audio focus helper if possible
 		if (android.os.Build.VERSION.SDK_INT >= 8) {
 			audioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
 		} else {
 			audioFocus = AudioFocus.Focused;
 		}
 	}
 	
 	/**
 	 * Intent processing.
 	 */
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.i(TAG, "onStartCommand() called, intent = " + intent.getAction());
 		String action = intent.getAction();
         if (action.equals(ACTION_TOGGLE_PLAYBACK)) {
         	processTogglePlaybackRequest();
         } else if (action.equals(ACTION_PLAY)) {
         	processPlayRequest();
         } else if (action.equals(ACTION_PAUSE)) {
         	processPauseRequest();
         } else if (action.equals(ACTION_SKIP)) {
         	processSkipRequest();
         } else if (action.equals(ACTION_STOP)) {
         	processStopRequest();
         } else if (action.equals(ACTION_REWIND)) {
         	processRewindRequest();
         } else if (action.equals(ACTION_QUERY)) {
         	processQueryRequest(intent);
         }
         return START_NOT_STICKY;
 	}
 
 	/**
 	 * Logic for playback request
 	 */
 	private void processTogglePlaybackRequest() {
 		Log.i(TAG, "processTogglePlaybackRequest() called");
 		if (state == State.Paused || state == State.Stopped) {
 			processPlayRequest();
 		} else {
 			processPauseRequest();
 		}	
 	}
 	
 	/**
 	 * Play track
 	 */
 	void processPlayRequest() {
 		Log.i(TAG, "processPlayRequest() called");
 		if (state == State.Retrieving) {
 			startPlayingAfterRetrieve = true;
 			return;
 		}
 		tryToGetAudioFocus();
 		if (state == State.Stopped) {
 			playNextTrack(null);
 		}
 		else if (state == State.Paused) {
 			state = State.Playing;
 			configureAndStartMediaPlayer();
 		}
 	}
 	
 	/**
 	 * Pause track
 	 */
 	void processPauseRequest() {
 		Log.i(TAG, "processPauseRequest() called");
 		if (state == State.Playing) {
 			state = State.Paused;
 			player.pause();
 			relaxResources(false);
 		}
 	}
 	
 	/**
 	 * Rewind track
 	 */
     void processRewindRequest() {
     	Log.i(TAG, "processRewindRequest() called");
         if (state == State.Playing || state == State.Paused)
             player.seekTo(0);
     }
     
     /**
      * Skip track
      */
     void processSkipRequest() {
     	Log.i(TAG, "processSkipRequest() called");
         if (state == State.Playing || state == State.Paused) {
             tryToGetAudioFocus();
             //TODO this is a downvote
             playNextTrack(null);
         }
     }
 
     /**
      * Stop playback
      */
 	void processStopRequest() {
 		Log.i(TAG, "processStopRequest() called");
 		state = State.Stopped;
 		if (player != null) {
 			player.reset();
 			player.release();
 			player = null;
 		}
 	}
 	
 	/**
 	 * Process user input to be queried
 	 * @param intent intent carrying raw user input in String format
 	 */
 	private void processQueryRequest(Intent intent) {
 		Log.i(TAG, "processQueryRequest() called, intent = " + intent.getAction());
 		String query = intent.getExtras().get("query").toString().toLowerCase().trim().replace(' ', '+');
 		if (state == State.Retrieving) {
 			whatToPlayAfterRetrieve = query;
 			startPlayingAfterRetrieve = true;
 		} else if (state == State.Playing || state == State.Paused || state == State.Stopped);
 		Log.i(TAG, "Query entered :: " + query);
 		tryToGetAudioFocus();
 		(new MusicRetrieverTask(retriever,this)).execute(query);
 	//	playNextTrack(query);
 	}
 	
 	/**
 	 * Play the retrieved MP3 URL
 	 */
     private void playNextTrack(String manualUrl) {
     	Log.i(TAG, "playNextTrack() called, url = " + manualUrl);
 		state = State.Stopped;
 		relaxResources(false);
 		nextTrack = currentTrack;
 		setNextTrack();
 		Log.i(TAG, "playing from :: " + nextTrack);
 		try {
 			if (nextTrack != null) {
 				createOrResetMediaPlayer();
 				player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				FileInputStream fis = new FileInputStream(nextTrack);
				player.setDataSource(fis.getFD());
 				Log.i(TAG, "player.setDataSource called, track = " + nextTrack);
 			} else {
 				Toast.makeText(this, "Error: null URL",  Toast.LENGTH_LONG).show();
 				return;
 			}
 			// Prep to play
 			state = State.Preparing;
 			player.prepareAsync();
 			wifiLock.acquire();
 		} catch (IOException e) {
 			Log.e("MusicService", "IOException encountered in playNextSong: " + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	private void setNextTrack() {
 		Log.i(TAG, "setNextTrack() called");
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Release consumed resources
 	 * @param releaseMediaPlayer
 	 */
 	void relaxResources(boolean releaseMediaPlayer) {
 		Log.i(TAG, "relaxResources() called, releaseMediaPlayer = " + releaseMediaPlayer);
         stopForeground(true);
         if (releaseMediaPlayer && player != null) {
             player.reset();
             player.release();
             player = null;
         }
         if (wifiLock.isHeld()) wifiLock.release();
     }
 	
 	/**
 	 * Request audio focus
 	 */
 	private void tryToGetAudioFocus() {
 		Log.i(TAG, "tryToGetAudioFocus called");
 		if (audioFocus != AudioFocus.Focused && audioFocusHelper != null && audioFocusHelper.requestFocus()) {
 			audioFocus = AudioFocus.Focused;
 			Log.i(TAG, "audio focus obtained.");
 		}
 		
 	}
 	
 	/**
 	 * Set preferences for ducking and focus and start MediaPlayer
 	 */
 	private void configureAndStartMediaPlayer() {
 		Log.i(TAG, "configureAndStartMediaPlayer() called");
 		if (audioFocus == AudioFocus.NoFocusNoDuck) {
 			// Pause if no focus and cannot duck.
 			if(player.isPlaying()) {
 				player.pause();
 			}
 		} else if (audioFocus == AudioFocus.NoFocusDoDuck) {
 			// Duck if possible
 			player.setVolume(DUCK_VOLUME, DUCK_VOLUME);
 		} else {
 			// No ducking, normal volume level
 			player.setVolume(1.0f, 1.0f);
 		}
 		// Lastly, start media player if needed
 		if (!player.isPlaying()) {
 			player.start();
 			Log.i(TAG, "player.start() called");
 		}
 	}
 
 	@Override
 	public boolean onError(MediaPlayer mp, int what, int extra) {
 		Log.i(TAG, "onError() called, what = " + what + ", extra = " + extra);
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	/**
 	 * What to do when MediaPlayer has finished preparing
 	 */
 	@Override
 	public void onPrepared(MediaPlayer mp) {
 		Log.i(TAG, "onPrepared() called");
         state = State.Playing;
         configureAndStartMediaPlayer();
 	}
 
 	/**
 	 * Called when track playback is completed
 	 */
 	@Override
 	public void onCompletion(MediaPlayer mp) {
 		Log.i(TAG, "onCompletion() called");
 		playNextTrack(null);
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		Log.i(TAG, "onBind() called, intent = " + intent.getAction());
 		return null;
 	}
 
 	@Override
 	public void onGainedAudioFocus() {
 		Log.i(TAG, "onGainedAudioFocus() called");
 		Toast.makeText(getApplicationContext(), "gained audio focus.", Toast.LENGTH_SHORT).show();
         audioFocus = AudioFocus.Focused;
         if (state == State.Playing) {
             configureAndStartMediaPlayer();
         }
 	}
 
 	@Override
 	public void onLostAudioFocus(boolean canDuck) {
 		Log.i(TAG, "onLostAudioFocus() called");
         Toast.makeText(getApplicationContext(), "lost audio focus." + (canDuck ? "can duck" : "no duck"), Toast.LENGTH_SHORT).show();
             audioFocus = canDuck ? AudioFocus.NoFocusDoDuck : AudioFocus.NoFocusNoDuck;
             if (player != null && player.isPlaying()) {
                 configureAndStartMediaPlayer();
             }
 	}
 
 	/**
 	 * Called when MusicRetrieverTask is completed
 	 */
 	@Override
 	public void onMusicRetrieverPrepared() {
 		Log.i(TAG, "onMusicRetrieverPrepared() called");
 		state = State.Stopped;
 		currentTrack = retriever.getPlayURL();
 		if(startPlayingAfterRetrieve) {
 			tryToGetAudioFocus();
 			playNextTrack(currentTrack);
 		}
 	}
 }
