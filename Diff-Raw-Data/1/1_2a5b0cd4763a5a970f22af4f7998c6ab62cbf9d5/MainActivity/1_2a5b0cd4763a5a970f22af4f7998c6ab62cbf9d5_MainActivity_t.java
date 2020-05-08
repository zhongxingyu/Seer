 package com.studiosh.balata.fm;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.database.ContentObserver;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.studiosh.balata.fm.SongInfoService.LocalBinder;
 
 public class MainActivity extends Activity {
 	private static final String TAG = "MainActivity";
     public static final String PREFS_NAME = "BalataPrefs";
 
 	private static SongInfoService mSongInfoService;
 	private static Boolean mServiceStarted = false;
 	private Boolean mBound = false;
 	
 	private Intent mServiceIntent;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Handle the Start/Stop Button
 		ToggleButton btnPlayStop = (ToggleButton) findViewById(R.id.btn_play_stop);
 		btnPlayStop.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				ToggleButton tb = (ToggleButton) v;
 				Boolean isChecked = tb.isChecked(); 
 				
 				BalataStreamer streamer = mSongInfoService.getStreamer();
 				
 				if (!isChecked) {
 					if (streamer.isStreamStarted()) {
 						streamer.stop();
 					}
 				} else {
 					if (!streamer.isStreamStarted()) {
 						streamer.play();
 					}
 				}
 				
 	            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 	            SharedPreferences.Editor editor = settings.edit();
 	            editor.putBoolean("is_playing", streamer.isStreamStarted());
 	            editor.commit();				
 			}
 		});
 
 		// Set the volume seek bar
 		final SeekBar sbVolume = (SeekBar) findViewById(R.id.sb_volume);	
 		final AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		
 		// First let's handle the seek bar
 		sbVolume.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
 		sbVolume.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
 		sbVolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 			public void onStopTrackingTouch(SeekBar seekBar) {}
 			public void onStartTrackingTouch(SeekBar seekBar) {}
 			
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				if (fromUser) {
 					audio.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
 				}
 			}
 		});
 		
 		// Monitor system changes for volume change
 		this.getApplicationContext().getContentResolver().registerContentObserver(
 			android.provider.Settings.System.CONTENT_URI, true,
 			new ContentObserver(new Handler()) {
 				public void onChange(boolean selfChange) {
 					super.onChange(selfChange);
 					sbVolume.setProgress(audio
 							.getStreamVolume(AudioManager.STREAM_MUSIC));
 				}
 			}
 		);
 	}
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 			
 		// Start the updates service
 		if (mServiceStarted == false) {
 			mServiceIntent = new Intent(this, SongInfoService.class);
 			startService(mServiceIntent);
 			mServiceStarted = true;
 			
 			// Set the custom font for the text areas
 			TextView tvSongInfo = (TextView) findViewById(R.id.tv_song_info); 
 			tvSongInfo.setText(R.string.retrieveing_song_details);
 		}
 		
         Intent intent = new Intent(this, SongInfoService.class);
         bindService(intent, mConnection, Context.BIND_AUTO_CREATE);        
 	}
 	
 	@Override
 	protected void onStop() {
 		super.onStop();
         // Unbind from the service
         if (mBound) {
             unbindService(mConnection);
             mBound = false;            
         }
         
 		if (mServiceStarted && !mSongInfoService.getStreamer().isStreamStarted()) {
 			stopService(new Intent(this, SongInfoService.class));
 			mServiceStarted = false;
 		}
 	}
 	
 	protected void onResume() {
 		super.onResume();		
 		registerReceiver(mSongDetailsReciever, new IntentFilter(BalataNotifier.SONG_DETAILS_ACTION));           
 	}
 	
 	protected void noPause() {
 		super.onPause();
 		unregisterReceiver(mSongDetailsReciever);
 	}
 
 	public void updateUI (Intent intent) {
 		TextView tvSongInfo = (TextView) findViewById(R.id.tv_song_info);
 		ToggleButton btnPlayStop = (ToggleButton) findViewById(R.id.btn_play_stop);
 		ImageView imgBalataLogo = (ImageView) findViewById(R.id.balata_logo);
 		ProgressBar pbBuffering = (ProgressBar) findViewById(R.id.buffering);
 
 		String songArtist = intent.getStringExtra("song_artist");
 		String songTitle = intent.getStringExtra("song_title");
 		Boolean playing = intent.getBooleanExtra("playing", false);
 		Boolean buffering = intent.getBooleanExtra("buffering", false);
 		
 		btnPlayStop.setChecked(playing);
 		
 		if (buffering) {
 			tvSongInfo.setText(getString(R.string.buffering));
 			Animation animFade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.balata_logo_fades);
 			imgBalataLogo.startAnimation(animFade);
 			pbBuffering.setVisibility(View.VISIBLE);
 		} else {
 			if (songArtist != null && songTitle != null) {
 				tvSongInfo.setText(songArtist + "\n" + songTitle);
 			}
 			
 			imgBalataLogo.setAnimation(null);
 			pbBuffering.setVisibility(View.INVISIBLE);			
 		}
 	}
 	
 	private BroadcastReceiver mSongDetailsReciever = new BroadcastReceiver() {
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {		
 			updateUI(intent);
 		}
 	};
 	
     /** Defines callbacks for service binding, passed to bindService() */
     private ServiceConnection mConnection = new ServiceConnection() {
         @Override
         public void onServiceConnected(ComponentName className,
                 IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             LocalBinder binder = (LocalBinder) service;
             mSongInfoService = binder.getService();
             BalataNotifier notifier = mSongInfoService.getNotifier();
             BalataStreamer streamer = mSongInfoService.getStreamer();
             
             notifier.setMainActivity(MainActivity.this);
             
             mBound = true;
             
             // Start stream if settings says we should
             if (streamer.isStreamStarted() != true) {            	
        	       SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        	       boolean is_playing = settings.getBoolean("is_playing", false);
        	       if (is_playing) {
        	    	   streamer.play();
        	       }
             }            
             
             // Update the UI from the service
             notifier.updateUI();
         }
         
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             mBound = false;
             mSongInfoService.getNotifier().clearMainActivity();
             
     		if (mServiceStarted && !mSongInfoService.getStreamer().isStreamStarted()) {
     			stopService(mServiceIntent);
     		}            
         }
     };	
 }
