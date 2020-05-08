 package com.code_pig.pocketfma;
 
import java.io.BufferedInputStream;
import java.io.FileInputStream;
 import java.io.IOException;
 
 import android.app.Notification;
 import android.app.NotificationManager;
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
 	final static String TAG = "RandomMusicPlayer"; // for debugging
 
 	// Intent Actions //TODO ensure all are used
 	public static final String ACTION_TOGGLE_PLAYBACK = "com.example.android.musicplayer.action.TOGGLE_PLAYBACK";
 	public static final String ACTION_PLAY = "com.example.android.musicplayer.action.PLAY";
 	public static final String ACTION_PAUSE = "com.example.android.musicplayer.action.PAUSE";
 	public static final String ACTION_STOP = "com.example.android.musicplayer.action.STOP";
 	public static final String ACTION_SKIP = "com.example.android.musicplayer.action.SKIP";
 	public static final String ACTION_REWIND = "com.example.android.musicplayer.action.REWIND";
 	public static final String ACTION_URL = "com.example.android.musicplayer.action.URL";
 
 	public static final float DUCK_VOLUME = 0.1f;
 	final int NOTIFICATION_ID = 1;
 
 	private WifiLock wifiLock;
 	private MusicRetriever retriever;
 	private MediaPlayer player = null;
 	private AudioFocusHelper audioFocusHelper = null;
 	private String currentTrack = null;
 	private String nextTrack = null;
 	
     boolean startPlayingAfterRetrieve = false;
 
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
 		if (player == null) {
 			player = new MediaPlayer();
 			player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
 
 			// Set up event notifications
 			player.setOnPreparedListener(this);
 			player.setOnCompletionListener(this);
 			player.setOnErrorListener(this);
 		} else {
 			player.reset();
 		}
 	}
 	
 	/**
 	 * Create the Service. Called on instantiation of this class.
 	 */
 	@Override
 	public void onCreate() {
 		Log.i(TAG, "MusicService.onCreate() called.");
 		
 		// We want the airwaves!
 		wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
 				.createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
	
 		retriever = new MusicRetriever();
 		(new MusicRetrieverTask(retriever,this)).execute();
 		
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
         }
         
         return START_NOT_STICKY;
 	}
 
 	private void processTogglePlaybackRequest() {
 		if (state == State.Paused || state == State.Stopped) {
 			processPlayRequest();
 		} else {
 			processPauseRequest();
 		}	
 	}
 	
 	void processPlayRequest() {
 		if (state == State.Retrieving) {
 			startPlayingAfterRetrieve = true;
 			return;
 		}
 		tryToGetAudioFocus();
 		
 		if (state == State.Stopped) {
 			playNextTrack();
 		}
 		
 		else if (state == State.Paused) {
 			state = State.Playing;
 			configureAndStartMediaPlayer();
 		}
 	}
 	
 	void processPauseRequest() {
 		if (state == State.Playing) {
 			state = State.Paused;
 			player.pause();
 			relaxResources(false);
 		}
 	}
 	
     void processRewindRequest() {
         if (state == State.Playing || state == State.Paused)
             player.seekTo(0);
     }
     
     void processSkipRequest() {
         if (state == State.Playing || state == State.Paused) {
             tryToGetAudioFocus();
             //TODO this is a downvote
             playNextTrack();
         }
     }
 
 	void processStopRequest() {
 		state = State.Stopped;
 		if (player != null) {
 			player.reset();
 			player.release();
 			player = null;
 		}
 	}
 	
 //	void processAddRequest(Intent intent) {
 //		Log.i(TAG, "MusicService.processAddRequest(" + intent + ") called.");
 //		tryToGetAudioFocus();
 //		playNextTrack(intent.getData().toString());
 //	}
 
     private void playNextTrack() {
 		state = State.Stopped;
 		relaxResources(false);
 		nextTrack = currentTrack;
		Log.i(TAG, "playing from :: " + nextTrack);
 		try {
 			if (nextTrack != null) {
 				createOrResetMediaPlayer();
 				player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				BufferedInputStream inputStream = new BufferedInputStream(nextTrack);
				player.setDataSource(inputStream.getFD());
 			} else {
 				Toast.makeText(this,
                         "Error: null URL",
                         Toast.LENGTH_LONG).show();
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
     
 	void relaxResources(boolean releaseMediaPlayer) {
         stopForeground(true);
         
         if (releaseMediaPlayer && player != null) {
             player.reset();
             player.release();
             player = null;
         }
         
         if (wifiLock.isHeld()) wifiLock.release();
     }
 	
 	private void tryToGetAudioFocus() {
 		if (audioFocus != AudioFocus.Focused && audioFocusHelper != null 
 				&& audioFocusHelper.requestFocus()) {
 			audioFocus = AudioFocus.Focused;
 		}
 		
 	}
 	
 	private void configureAndStartMediaPlayer() {
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
 			try {
 				player.setDataSource(currentTrack);
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SecurityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalStateException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			player.start();
 		}
 	}
 
 	@Override
 	public boolean onError(MediaPlayer mp, int what, int extra) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void onPrepared(MediaPlayer mp) {
         state = State.Playing;
         configureAndStartMediaPlayer();
 	}
 
 	@Override
 	public void onCompletion(MediaPlayer mp) {
 		playNextTrack();
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public void onGainedAudioFocus() {
 		Toast.makeText(getApplicationContext(), "gained audio focus.", Toast.LENGTH_SHORT).show();
         audioFocus = AudioFocus.Focused;
         if (state == State.Playing) {
             configureAndStartMediaPlayer();
         }
 	}
 
 	@Override
 	public void onLostAudioFocus(boolean canDuck) {
         Toast.makeText(getApplicationContext(), "lost audio focus." + (canDuck ? "can duck" :
                 "no duck"), Toast.LENGTH_SHORT).show();
             audioFocus = canDuck ? AudioFocus.NoFocusDoDuck : AudioFocus.NoFocusNoDuck;
            
             if (player != null && player.isPlaying()) {
                 configureAndStartMediaPlayer();
             }
 	}
 
 	@Override
 	public void onMusicRetrieverPrepared() {
 		state = State.Stopped;
 		currentTrack = retriever.getPlayURL();
 		if(startPlayingAfterRetrieve) {
 			tryToGetAudioFocus();
 			playNextTrack();
 		}
 	}
 }
