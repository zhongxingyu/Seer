 
 package edu.fsu.cs.contextprovider;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import edu.fsu.cs.contextprovider.data.ContextConstants;
 import edu.fsu.cs.contextprovider.monitor.DerivedMonitor;
 import edu.fsu.cs.contextprovider.monitor.MovementMonitor;
 
 import android.app.Activity;
 import android.app.KeyguardManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnClickListener;
 import android.media.AudioManager;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.SeekBar;
 import android.widget.Toast;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 
 public class ContextAccuracyActivity extends Activity implements View.OnClickListener {
 	private static final String TAG = "ContextAccuracyActivity";
 	private static final boolean DEBUG = true;	
 
 	private Ringtone tone;
 	private AudioManager volume;
 	private AudioManager audio;
 	private Vibrator vibrate;
 	
 	SharedPreferences prefs;
 	private boolean accuracyAudioEnabled;
 	private boolean accuracyVibrateEnabled;
 	private String accuracyDismissDelay;
 	private int dismissDelay;
 	
 	SeekBar placeBar = null;
 	SeekBar movementBar = null;
 	SeekBar activityBar = null;
 //	SeekBar shelterBar = null;
 //	SeekBar onPersonBar = null;
 	RadioGroup shelterGroup = null;
 	RadioGroup onPersonGroup = null;
 	RadioButton shelterCorrect = null;
 	RadioButton onPersonCorrect = null;
 	
 	EditText placeText = null;
 	EditText movementText = null;
 	EditText activityText = null;
 	EditText shelterText = null;
 	EditText onPersonText = null;
 	
 	Button submitBtn = null;
 	Button resetBtn = null;
 	
 	final int INDEX_PLACE = 1;
 	final int INDEX_MOVEMENT = 2;
 	final int INDEX_ACTIVITY = 3;
 	final int INDEX_SHELTER = 4;
 	final int INDEX_ONPERSON = 5;
 	
 	private PowerManager.WakeLock wakelock;
 
 	private static Timer timer = new Timer(); 
 //	private Activity ctx;
 	private int backCount = 0;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 //		getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);		
 		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "ContextAccuracyActivity");
 		wakelock.acquire();
 		KeyguardManager km = (KeyguardManager) getSystemService (KEYGUARD_SERVICE); 
 		KeyguardManager.KeyguardLock keylock = km.newKeyguardLock(TAG); 
 		keylock.disableKeyguard(); 
 		
 		getPrefs();
 		setContentView(R.layout.accuracy);
 		
 		vibrate = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		volume = (AudioManager) getSystemService(Context.AUDIO_SERVICE);		
 
 		placeBar = (SeekBar) findViewById(R.id.place);
 		movementBar = (SeekBar) findViewById(R.id.movement);
 		activityBar = (SeekBar) findViewById(R.id.activity);
 //		shelterBar = (SeekBar) findViewById(R.id.shelter);
 //		onPersonBar = (SeekBar) findViewById(R.id.onPerson);
 		shelterGroup = (RadioGroup) findViewById(R.id.shelter);
 		shelterCorrect = (RadioButton) findViewById(R.id.shelterCorrect);
 	
 		onPersonGroup = (RadioGroup) findViewById(R.id.onPerson);
 		onPersonCorrect = (RadioButton) findViewById(R.id.onPersonCorrect);
 		
 		placeText = (EditText) findViewById(R.id.editPlace);
 		movementText = (EditText) findViewById(R.id.editMovement);
 		activityText = (EditText) findViewById(R.id.editActivity);
 		shelterText = (EditText) findViewById(R.id.editShelter);
 		onPersonText = (EditText) findViewById(R.id.editOnPerson);
 		
 		placeText.setText(DerivedMonitor.getPlace());
 		movementText.setText(MovementMonitor.getMovementState());
 		activityText.setText(DerivedMonitor.getActivity());
 		shelterText.setText(DerivedMonitor.getShelterString());
 		onPersonText.setText(DerivedMonitor.getOnPersonString());
 		
 		submitBtn = (Button) findViewById(R.id.SubmitButton);
 		resetBtn = (Button) findViewById(R.id.ResetButton);
 		
 		submitBtn.setOnClickListener(this);
 		resetBtn.setOnClickListener(this);
 		
 		resetBars();
 		
         if (accuracyAudioEnabled)
         	tone.play();
         if (accuracyVibrateEnabled)
         	startVibrate();
         
         timer = new Timer();
         timer.schedule(new ContextDismissTask(), (dismissDelay * 1000));
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		wakelock.release();
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 	
 	@Override
 	public void onBackPressed() {
 		if (backCount++ < 1) {
 			Toast.makeText(this, "Press Back again to submit", Toast.LENGTH_SHORT).show();		
 		} else {		
 		timer.cancel();
 		sendAccuracy(true);
 		finish();
 		}
 	}
 	
 	private void getPrefs() {
 		prefs = getSharedPreferences(ContextConstants.CONTEXT_PREFS, MODE_PRIVATE);
 //		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 		accuracyAudioEnabled = prefs.getBoolean(ContextConstants.PREFS_ACCURACY_POPUP_AUDIO_ENABLED, false);
 		accuracyVibrateEnabled = prefs.getBoolean(ContextConstants.PREFS_ACCURACY_POPUP_VIBRATE_ENABLED, true);
 //		accuracyDismissDelay = prefs.getInt(ContextConstants.PREFS_ACCURACY_POPUP_DISMISS_FREQ, 5);
		accuracyDismissDelay = prefs.getString(ContextConstants.PREFS_ACCURACY_POPUP_DISMISS_FREQ, "15");
 		dismissDelay = Integer.parseInt(accuracyDismissDelay);
 		
 		if (DEBUG) {
 			Log.d(TAG, "accuracyDismissDelay: " + accuracyDismissDelay + "  dismissDelay: " + dismissDelay);
 		}
 		
 		if (accuracyAudioEnabled)
 			setRingtone();
 	}
 	
 	private void resetBars() {
 		initBar(placeBar, INDEX_PLACE);
 		initBar(movementBar, INDEX_MOVEMENT);
 		initBar(activityBar, INDEX_ACTIVITY);
 //		initBar(shelterBar, INDEX_SHELTER);
 //		initBar(onPersonBar, INDEX_ONPERSON);
 	}
 	
 
 	private void initBar(SeekBar bar, final int stream) {
 		bar.setMax(10);
 		bar.setProgress(10);
 
 		bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 			public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
 			}
 
 			public void onStartTrackingTouch(SeekBar bar) {
 			}
 
 			public void onStopTrackingTouch(SeekBar bar) {
 			}
 		});
 	}
 
 	
 	@Override
 	public void onClick(View v) {
 			if (v == resetBtn) {
 				Toast.makeText(this, "Reset defaults", Toast.LENGTH_SHORT).show();		
 				resetBars();
 			} else if (v == submitBtn) {
 				timer.cancel();
 				sendAccuracy(true);
 				finish();
 			}
 	}
 	
 	
     private class ContextDismissTask extends TimerTask
     { 
         public void run() 
         {
         	sendAccuracy(false);	
         	finish();
         }
     }   
     
     private void sendAccuracy(boolean response) {
     	Intent intent = new Intent(ContextConstants.CONTEXT_STORE_INTENT);
     	
     	intent.putExtra(ContextConstants.PLACE_ACCURATE, (int) placeBar.getProgress());
   		intent.putExtra(ContextConstants.MOVEMENT_ACCURATE, (int) movementBar.getProgress());
  		intent.putExtra(ContextConstants.ACTIVITY_ACCURATE, (int) activityBar.getProgress());
 //    	intent.putExtra(ContextConstants.SHELTER_ACCURATE, (int) shelterBar.getProgress());
 //    	intent.putExtra(ContextConstants.ONPERSON_ACCURATE, (int) onPersonBar.getProgress());
  		if (shelterCorrect.isChecked()) {
  			intent.putExtra(ContextConstants.SHELTER_ACCURATE, true);
  		} else {
  			intent.putExtra(ContextConstants.SHELTER_ACCURATE, false);
  		}
  		if (onPersonCorrect.isChecked()) {
  			intent.putExtra(ContextConstants.ONPERSON_ACCURATE, true);
  		} else {
  			intent.putExtra(ContextConstants.ONPERSON_ACCURATE, false);
  		}
  		
     	intent.putExtra(ContextConstants.DERIVED_RESPONSE, response);
     	
     	sendBroadcast(intent);    		
     }
         
 	private void setRingtone() {
 		Uri ringUri;
 		String ringtone = prefs.getString(ContextConstants.PREFS_ACCURACY_POPUP_AUDIO, "Default");
 		
 		if(ringtone.equalsIgnoreCase("default"))
 			ringUri = Settings.System.DEFAULT_RINGTONE_URI;
 		else
 			ringUri = Uri.parse(ringtone);
 		
 		tone = RingtoneManager.getRingtone(this, ringUri);
 	}
 	
 	private void startVibrate() {
 		long[] pattern = {500, 300, 800, 300};
 		vibrate.vibrate(pattern, -1);
 	}
 
 }
