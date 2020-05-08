 package dk.aau.sw802f12.proto3;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import dk.aau.sw802f12.proto3.R.id;
 
 public class MainActivity extends Activity {
 	public static String tag = "SW8PLAYER";
 	private SeekBar volumeBar;
 	private ProgressBar progressBar;
 	private AudioManager am;
 	
 	private PlayService player;
 	
 	private TextView currentSongText;
 	private TextView song1;
 	private TextView song2;
 	private TextView song3;
 	private TextView song4;
 	private Button toggleButton;
     
 	private Intent intent = new Intent();
 	
 	private boolean playerBound = false;
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 		Intent serviceIntent = new Intent(this, PlayService.class);
 		startService(serviceIntent);
 		bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
 		
 		registerReceiver(updateInterface, new IntentFilter(PlayService.UPDATESTATE));
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		if (playerBound) {
 			unbindService(mConnection);
 		}
 		
 		unregisterReceiver(updateInterface);
 	}
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.player);
         
         am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
         volumeBar = (SeekBar) findViewById(R.id.volume);
         progressBar = (ProgressBar) findViewById(R.id.songprogressbar);
         
         SeekbarListener sbl = new SeekbarListener();
         volumeBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
 		volumeBar.setProgress(am.getStreamVolume(AudioManager.STREAM_MUSIC));
 		volumeBar.setOnSeekBarChangeListener(sbl);
 		
 		toggleButton = (Button) findViewById(id.toggleplayerstatus);
 		toggleButton.setOnClickListener(toggleListener);
 		
 		currentSongText = (TextView) findViewById(id.trackName);
 		song1 = (TextView) findViewById(id.song1_text);
 		song2 = (TextView) findViewById(id.song2_text);
 		song3 = (TextView) findViewById(id.song3_text);
 		song4 = (TextView) findViewById(id.song4_text);
     }
     
     public void onResume(Bundle savedInstanceState) {
     	super.onResume();
     }
     
     public void onPause(Bundle savedInstanceState) {
     	super.onPause();
     }
     
     private class SeekbarListener implements SeekBar.OnSeekBarChangeListener {
 		public void onStopTrackingTouch(SeekBar seekBar) { }
 		
 		public void onStartTrackingTouch(SeekBar seekBar) { }
 		
 		public void onProgressChanged(SeekBar seekBar, int progress,
 				boolean fromUser) {
 			if (fromUser) {
 				Log.d(tag, "Changing volume to " + progress);
 				am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
 			}
 		}
 	};
 	
 	private ServiceConnection mConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			player = ((PlayService.LocalBinder) service).getService();
 			if (player != null) playerBound = true;
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			// TODO Auto-generated method stub
 			
 		}
 	};
 	
 	private BroadcastReceiver updateInterface = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Toast.makeText(getApplicationContext(), "Received broadcast", Toast.LENGTH_SHORT).show();
 			if (intent.getAction() != PlayService.UPDATESTATE) return;
 			
 			Toast.makeText(getApplicationContext(), "Received Update broadcast", Toast.LENGTH_SHORT).show();
 			
 			try {
 				currentSongText.setText(intent.getStringExtra("current"));
 				song1.setText(intent.getStringExtra("song1"));
 				song2.setText(intent.getStringExtra("song2"));
 				song3.setText(intent.getStringExtra("song3"));
 				song4.setText(intent.getStringExtra("song4"));
 			} catch (NullPointerException e) {
 				currentSongText.setText("");
 				song1.setText("");
 				song2.setText("");
 				song3.setText("");
 				song4.setText("");
 			}
 		}
 	};
 	
 	private OnClickListener toggleListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			if (playerBound) player.toggle();
 			else Toast.makeText(getApplicationContext(), "Player not bound yet", Toast.LENGTH_SHORT);
 		}
 	};
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		switch (keyCode) {
 			case KeyEvent.KEYCODE_VOLUME_UP:
 				am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
 				volumeBar.setProgress(am.getStreamVolume(AudioManager.STREAM_MUSIC));
				return true;
 			case KeyEvent.KEYCODE_VOLUME_DOWN:
 				am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
 				volumeBar.setProgress(am.getStreamVolume(AudioManager.STREAM_MUSIC));
				return true;
			default:
				return super.onKeyDown(keyCode, event);
		}		
 	}
 }
