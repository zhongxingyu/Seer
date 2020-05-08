 package se.chalmers.dat255.sleepfighter.activities;
 
 import se.chalmers.dat255.sleepfighter.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
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
 
 	private int alarmId;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Turn and/or Keep screen on.
 		this.setScreenFlags();
 
 		this.setContentView(R.layout.activity_alarm_prechallenge);
 
 		// Fetch alarm Id.
 		this.readAlarmId();
 		if ( this.getAlarmId() < 1 ) {
 			this.finish();
 		}
 
 		// Do stuff.
 		this.work();
 	}
 
 	protected void onPause() {
 		super.onPause();
 
 		// Release the wake-lock acquired in AlarmReceiver!
 		AlarmWakeLocker.release();
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
 
 	/**
 	 * Computes screen flags based on preferences.
 	 *
 	 * @return screen flags.
 	 */
 	private int computeScreenFlags() {
 		// TODO: actually read from preferences!
 
 		int flags = 0;
 
 		if ( this.turnScreenOn ) {
 			flags |= WINDOW_FLAGS_SCREEN_ON;
 		}
 
 		if ( this.bypassLockscreen ) {
 			flags |= WINDOW_FLAGS_LOCKSCREEN;
 		}
 
 		return flags;
 	}
 
 	/**
 	 * Reads the alarmId and stores it.
 	 */
 	private void readAlarmId() {
 		Intent intent = this.getIntent();
 		Bundle extras = intent.getExtras();
 		this.alarmId = extras.getInt( EXTRA_ALARM_ID );
 	}
 
 	/**
 	 * Returns the alarmId, must be called after {@link #readAlarmId()}.
 	 *
 	 * @return the alarmId.
 	 */
 	private int getAlarmId() {
 		return this.alarmId;
 	}
 
 	private void work() {
 		Log.d( "AlarmActivity", "work#1" );
 		// TODO: do something useful.
 		Toast.makeText(this, "Alarm ringing, get up! Alarm #" + this.getAlarmId(), Toast.LENGTH_LONG).show();
 	}
 	
 	public void button(View view) {
 		Intent intent = new Intent(this, ChallengeActivity.class);
		startActivityForResult(intent, CHALLENGE_REQUEST_CODE);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// Check if result is from a challenge
		if(requestCode == CHALLENGE_REQUEST_CODE) {
 			if(resultCode == Activity.RESULT_OK) {
 				Toast.makeText(this, "Challenge completed", Toast.LENGTH_LONG)
 						.show();
 			} else {
 				Toast.makeText(this, "Returned from uncompleted challenge",
 						Toast.LENGTH_LONG).show();
 			}
 			
 		} else {
 			super.onActivityResult(requestCode, resultCode, data);
 		}
 	}
 }
