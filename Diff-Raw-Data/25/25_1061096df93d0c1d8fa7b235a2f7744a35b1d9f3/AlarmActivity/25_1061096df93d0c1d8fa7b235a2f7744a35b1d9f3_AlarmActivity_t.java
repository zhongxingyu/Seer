 package se.chalmers.dat255.sleepfighter.activity;
 
 import org.joda.time.DateTime;
 
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.SFApplication;
 import se.chalmers.dat255.sleepfighter.service.AlarmPlannerService;
 import se.chalmers.dat255.sleepfighter.service.AlarmPlannerService.Command;
 import se.chalmers.dat255.sleepfighter.utils.android.AlarmWakeLocker;
 import se.chalmers.dat255.sleepfighter.utils.android.IntentUtils;
 import se.chalmers.dat255.sleepfighter.utils.debug.Debug;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.AlarmTimestamp;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Toast;
 
 /**
  * The activity for when an alarm rings/occurs.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Sep 20, 2013
  */
 public class AlarmActivity extends Activity {
 	
 	public static final String EXTRA_ALARM_ID = "alarm_id";
 	
 	private static final int WINDOW_FLAGS_SCREEN_ON =
 			WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
 			WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
 			WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
 
 	private static final int WINDOW_FLAGS_LOCKSCREEN = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
 
 	private static final int CHALLENGE_REQUEST_CODE = 1;
 
 	// TODO move to settings!
 	private boolean turnScreenOn = true;
 	private boolean bypassLockscreen = true;
 
 	private Alarm alarm;
 
 	private static final String TURN_SCREEN_ON = "pref_alarm_turn_screen_on";
 	private static final String BYPASS_LOCK_SCREEN = "pref_alarm_bypass_lock_screen";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Turn and/or Keep screen on.
 		this.setScreenFlags();
 
 		this.setContentView(R.layout.activity_alarm_prechallenge);
 
 		// Fetch alarm Id.
 		int alarmId = new IntentUtils( this.getIntent() ).getAlarmId();
 		this.alarm = SFApplication.get().getPersister().fetchAlarmById( alarmId );
 
 		// Do stuff.
 		this.work();
 	}
 
 	protected void onPause() {
 		super.onPause();
 
 		// Release the wake-lock acquired in AlarmReceiver!
 		AlarmWakeLocker.release();
 	}
 
 	private void performRescheduling() {
 		SFApplication app = SFApplication.get();
 
 		// Disable alarm if not repeating.
 		if ( !this.alarm.isRepeating() ) {
 			this.alarm.setActivated( false );
 			app.getPersister().updateAlarm( this.alarm );
		} else {
			// Reschedule earliest alarm (if any).
			AlarmTimestamp at = app.getAlarms().getEarliestAlarm( new DateTime().getMillis() );
			if ( at != AlarmTimestamp.INVALID ) {
				AlarmPlannerService.call( app, Command.CREATE, at.getAlarm().getId() );
			}
 		}
 	}
 
 
 	/**
 	 * Sets screen related flags, reads from preferences.
 	 */
 	private void setScreenFlags() {
 		int flags = this.computeScreenFlags();
 
 		if ( flags == 0 ) {
 			return;
 		}
 
 		this.getWindow().addFlags( flags );
 	}
 
 	private void readPreferences() {
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 	
 		this.turnScreenOn = sharedPref.getBoolean(TURN_SCREEN_ON, true);
 		this.bypassLockscreen = sharedPref.getBoolean(BYPASS_LOCK_SCREEN, true);
 	
 		Debug.d("turn screen on :" + this.turnScreenOn);
 		Debug.d("bypass lock screen :" + this.bypassLockscreen);
 	}
 
 	/**
 	 * Computes screen flags based on preferences.
 	 *
 	 * @return screen flags.
 	 */
 	private int computeScreenFlags() {
 		readPreferences();
 		
 		int flags = 0;
 
 		if ( this.turnScreenOn ) {
 			flags |= WINDOW_FLAGS_SCREEN_ON;
 		}
 
 		if ( this.bypassLockscreen ) {
 			flags |= WINDOW_FLAGS_LOCKSCREEN;
 		}
 
 		return flags;
 	}
 
 	private void work() {
 		Log.d( "AlarmActivity", "alarm #id: " + Integer.toString( this.alarm.getId() ) );
 
 		Log.d( "AlarmActivity", "work#1" );
 		// TODO: do something useful.
 		Toast.makeText(this, "Alarm ringing, get up! Alarm #" + this.alarm.getId(), Toast.LENGTH_LONG).show();
 	}
 	
 	public void button(View view) {
 //		Intent intent = new Intent(this, ChallengeActivity.class);
 		
 		//Intent intent = new Intent(this, MemoryActivity.class);
 		
 		Intent intent = new Intent(this, SimpleMathActivity.class);
 		startActivityForResult(intent, CHALLENGE_REQUEST_CODE);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// Check if result is from a challenge
 		if(requestCode == CHALLENGE_REQUEST_CODE) {
 			if(resultCode == Activity.RESULT_OK) {
 				Toast.makeText(this, "Challenge completed", Toast.LENGTH_LONG)
 						.show();
				stopAlarm();
 			} else {
 				Toast.makeText(this, "Returned from uncompleted challenge",
 						Toast.LENGTH_LONG).show();
 			}
 			
 		} else {
 			super.onActivityResult(requestCode, resultCode, data);
 		}
 	}

	// TODO move out of class
	public void stopAlarm() {
		// TODO more here
		this.performRescheduling();
	}
 }
