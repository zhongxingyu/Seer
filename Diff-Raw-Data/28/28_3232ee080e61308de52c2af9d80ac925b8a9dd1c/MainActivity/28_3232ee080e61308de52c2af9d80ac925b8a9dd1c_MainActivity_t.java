 package edu.rosehulman.onegoodgpsreading_solution;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.PowerManager;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.LinearLayout;
import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 import edu.rosehulman.me435.FieldGps;
 import edu.rosehulman.me435.FieldGpsListener;
 import edu.rosehulman.me435.FieldOrientation;
 import edu.rosehulman.me435.FieldOrientationListener;
 import edu.rosehulman.me435.NavUtils;
 import edu.rosehulman.me435.SpeechAccessoryActivity;
 
 public class MainActivity extends SpeechAccessoryActivity implements
 		FieldGpsListener, FieldOrientationListener {
 
 	public static final String ROBOT_NAME = "Moxom";
 	private int mVoiceCommandAngle, mVoiceCommandDistance;
 
 	@Override
 	protected void onVoiceCommand(int angle, int distance) {
 		super.onVoiceCommand(angle, distance);
 		mVoiceCommandAngle = angle;
 		mVoiceCommandDistance = distance;
 		setState(State.RUNNING_VOICE_COMMAND);
 	}
 
 	private void useVoiceCommand() {
 		Toast.makeText(
 				this,
 				"Voice command for angle " + mVoiceCommandAngle + " distance "
 						+ mVoiceCommandDistance, Toast.LENGTH_LONG).show();
 
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				setState(State.WAITING_FOR_PICKUP);
 			}
 		}, 5000);
 	}
 
 	public enum State {
 		READY_FOR_MISSION, RED_FIGURE_8_SCRIPT, BLUE_FIGURE_8_SCRIPT, RED_HALF_CIRCLE_SCRIPT, BLUE_HALF_CIRCLE_SCRIPT, OUT_AND_BACK_SCRIPT, LAME_SCRIPT, WAITING_FOR_GPS, DRIVING_HOME, WAITING_FOR_PICKUP, RUNNING_VOICE_COMMAND, SEEKING_HOME, STOPPED, CORRECTIVE_SCRIPT, STRAIGHT_FOR_GPS
 	}
 
 	private State mState = State.READY_FOR_MISSION;
 	private long mStateStartTime = 0;
 
 	// Various constants and member variable names.
 	private static final String TAG = "OneGoodGps";
 	private static final double NO_HEADING_KNOWN = 360.0;
 	private TextView mCurrentStateTextView, mStateTimeTextView,
 			mGpsInfoTextView, mSensorOrientationTextView;
 	private int mGpsCounter = 0;
 	private double mCurrentGpsX, mCurrentGpsY, mCurrentGpsHeading;
 	private double mCurrentSensorHeading;
 	private Handler mCommandHandler = new Handler();
 	protected Timer mTimer;
 	private PowerManager.WakeLock mWakeLock;
 	private FieldOrientation mFieldOrientation;
 	private FieldGps mFieldGps;
 	private LinearLayout mScrollWindow;
	private ScrollView mScroll;
 
 	public static final int LOOP_INTERVAL_MS = 100;
 
 	public void loop() {
 		mStateTimeTextView.setText("" + getStateTimeMs() / 1000);
 
 		// Note you have access to the most recent readings here.
 		// Log.d(TAG, "GPS (" + mCurrentGpsX + "," + mCurrentGpsY + ") " +
 		// mCurrentGpsHeading + "   Sensor heading " + mCurrentSensorHeading);
 
 		switch (mState) {
 		case WAITING_FOR_PICKUP:
 			if (getStateTimeMs() > 2000) {
 				// I did not get picked up. Seek some more.
 				setState(State.SEEKING_HOME);
 			}
 			break;
 		case SEEKING_HOME:
 			if (getStateTimeMs() > 6000) {
 				// Done moving. Stop moving to try for pickup.
 				setState(State.WAITING_FOR_PICKUP);
 			}
 			break;
 		case WAITING_FOR_GPS:
 			// if (getStateTimeMs() > 8000) {
 			// setState(State.SEEKING_HOME); // Give up on GPS.
 			// }
 		default:
 			// Other states don't need to do anything, but could.
 			break;
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
 				| PowerManager.ON_AFTER_RELEASE, "Unused TAG");
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 		mFieldGps = new FieldGps(this);
 		mFieldOrientation = new FieldOrientation(this);
 
 		mCurrentStateTextView = (TextView) findViewById(R.id.current_state_textview);
 		mStateTimeTextView = (TextView) findViewById(R.id.state_time_textview);
 		mGpsInfoTextView = (TextView) findViewById(R.id.gps_info_textview);
 		mSensorOrientationTextView = (TextView) findViewById(R.id.orientation_textview);
 		mScrollWindow = (LinearLayout) findViewById(R.id.serial_messages);
		mScroll = (ScrollView) findViewById(R.id.scrollView1);
 		setState(State.READY_FOR_MISSION);
 	}
 
 	@Override
 	public void onSensorChanged(double fieldHeading, float[] orientationValues) {
 		mCurrentSensorHeading = fieldHeading;
 		mSensorOrientationTextView.setText(getString(R.string.degrees_format,
 				fieldHeading));
 		// Note:
 		// Azimuth is orientationValues[0]
 		// Pitch is orientationValues[1]
 		// Roll is orientationValues[2]
 	}
 
 	public void handleRedFigure8(View view) {
 		// Toast.makeText(this, "Go Red Team", Toast.LENGTH_SHORT).show();
 		setState(State.RED_FIGURE_8_SCRIPT);
 	}
 
 	public void handleRedHalfCircle(View view) {
 		// Toast.makeText(this, "Go Red Team", Toast.LENGTH_SHORT).show();
 		setState(State.RED_HALF_CIRCLE_SCRIPT);
 	}
 
 	public void handleBlueFigure8(View view) {
 		// Toast.makeText(this, "Go Blue Team", Toast.LENGTH_SHORT).show();
 		setState(State.BLUE_FIGURE_8_SCRIPT);
 	}
 
 	public void handleBlueHalfCircle(View view) {
 		// Toast.makeText(this, "Go Red Team", Toast.LENGTH_SHORT).show();
 		setState(State.BLUE_HALF_CIRCLE_SCRIPT);
 	}
 
 	public void handleOutBack(View view) {
 		// Toast.makeText(this, "Go Red Team", Toast.LENGTH_SHORT).show();
 		setState(State.OUT_AND_BACK_SCRIPT);
 	}
 
 	public void handleLameScript(View view) {
 		// Toast.makeText(this, "Go Red Team", Toast.LENGTH_SHORT).show();
 		setState(State.LAME_SCRIPT);
 	}
 
 	public void handleFakeGps(View view) {
 		// Toast.makeText(this, "Send fake GPS signal",
 		// Toast.LENGTH_SHORT).show();
 		onLocationChanged(40, 10, 135, null);
 	}
 	
 	public void setOrigin(View view) {
 		Toast.makeText(this, "TODO: Set Origin", Toast.LENGTH_SHORT).show();
 	}
 	
 	public void kill(View view) {
		mCommandHandler.removeCallbacksAndMessages(null);
 		setState(State.STOPPED);
 	}
 
 	public void handleMissionComplete(View view) {
 		// Toast.makeText(this, "Mission Complete", Toast.LENGTH_SHORT).show();
 		setState(State.READY_FOR_MISSION);
 	}
 
 	@Override
 	public void onLocationChanged(double x, double y, double heading,
 			Location location) {
 		mGpsCounter++;
 		mCurrentGpsX = x;
 		mCurrentGpsY = y;
 		mCurrentGpsHeading = NO_HEADING_KNOWN;
 		String gpsInfo = getString(R.string.xy_format, x, y);
 		// Toast.makeText(this, "" + heading, Toast.LENGTH_SHORT).show();
 		if (heading <= 180.0 && heading > -180.0) {
 			gpsInfo += " " + getString(R.string.degrees_format, heading);
 			mCurrentGpsHeading = heading;
 			if (mState == State.WAITING_FOR_GPS) {
 				setState(State.DRIVING_HOME);
 			}
 		} else {
 			gpsInfo += " ---";
 		}
 		gpsInfo += "    " + mGpsCounter;
 		mGpsInfoTextView.setText(gpsInfo);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		mWakeLock.acquire();
 		mTimer = new Timer();
 		mTimer.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				runOnUiThread(new Runnable() {
 					public void run() {
 						loop();
 					}
 				});
 			}
 		}, 0, LOOP_INTERVAL_MS);
 		mFieldGps.requestLocationUpdates(this);
 		mFieldOrientation.registerListener(this);
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		mTimer.cancel();
 		mTimer = null;
 		mFieldGps.removeUpdates();
 		mFieldOrientation.unregisterListener();
 		setState(State.READY_FOR_MISSION);
 		mWakeLock.release();
 	}
 
 	protected long getStateTimeMs() {
 		return System.currentTimeMillis() - mStateStartTime;
 	}
 
 	/**
 	 * Setup any initial work for the state.
 	 * 
 	 * @param newState
 	 *            State we are transistioning to NOW.
 	 */
 	private void setState(State newState) {
 		mStateStartTime = System.currentTimeMillis();
 		switch (newState) {
 		case READY_FOR_MISSION:
 			mCurrentStateTextView.setText("READY_FOR_MISSION");
 			// Do nothing until the button is pressed.
 			break;
 		case RED_FIGURE_8_SCRIPT:
 			mCurrentStateTextView.setText("RED_FIGURE_8_SCRIPT");
 			mGpsInfoTextView.setText("---"); // Clear GPS display
 			runScriptRedFigure8();
 			break;
 		case RED_HALF_CIRCLE_SCRIPT:
 			mCurrentStateTextView.setText("RED_HALF_CIRCLE_SCRIPT");
 			mGpsInfoTextView.setText("---"); // Clear GPS display
 			runScriptRedHalfCircle();
 			break;
 		case BLUE_FIGURE_8_SCRIPT:
 			mCurrentStateTextView.setText("BLUE_FIGURE_8_SCRIPT");
 			mGpsInfoTextView.setText("---"); // Clear GPS display
 			runScriptBlueFigure8();
 			break;
 		case BLUE_HALF_CIRCLE_SCRIPT:
 			mCurrentStateTextView.setText("BLUE_HALF_CIRCLE_SCRIPT");
 			mGpsInfoTextView.setText("---"); // Clear GPS display
 			runScriptBlueHalfCircle();
 			break;
 		case OUT_AND_BACK_SCRIPT:
 			mCurrentStateTextView.setText("OUT_AND_BACK_SCRIPT");
 			mGpsInfoTextView.setText("---"); // Clear GPS display
 			runScriptOutandBack();
 			break;
 		case LAME_SCRIPT:
 			mCurrentStateTextView.setText("LAME_SCRIPT");
 			mGpsInfoTextView.setText("---"); // Clear GPS display
 			runScriptLame();
 			break;
 		case WAITING_FOR_GPS:
 			mCurrentStateTextView.setText("WAITING_FOR_GPS");
 			// Do nothing until a GPS reading with a heading is received.
 			sendADKCommand("WHEEL SPEED BRAKE 0 BRAKE 0");
 			break;
 		case DRIVING_HOME:
 			mCurrentStateTextView.setText("DRIVING_HOME");
 			useArcRadiusToGetHome();
 			break;
 		case WAITING_FOR_PICKUP:
 			mCurrentStateTextView.setText("WAITING_FOR_PICKUP");
 			sendADKCommand("WHEEL SPEED BRAKE 0 BRAKE 0");
 			startListening("Give " + ROBOT_NAME + " a command");
 			if (getStateTimeMs() > 8000) {
 				setState(State.SEEKING_HOME);
 			}
 			break;
 		case RUNNING_VOICE_COMMAND:
 			mCurrentStateTextView.setText("RUNNING_VOICE_COMMAND");
 			// Just received a voice command.
 			// Can't get here until voice control is implemented.
 			// Toast.makeText(this,
 			// "Just received a voice command (impossible)",
 			// Toast.LENGTH_SHORT).show();
 
 			useVoiceCommand();
 			break;
 		case SEEKING_HOME:
 			mCurrentStateTextView.setText("SEEKING_HOME");
 			// Use the GPS and FieldOrientation sensor to find home.
 			// Note, you could also drive in a spiral hoping you are close.
 			Toast.makeText(this, "Seeking home", Toast.LENGTH_SHORT).show();
 			break;
 		case STOPPED:
 			mCurrentStateTextView.setText("STOPPED");
 			break;
 		case STRAIGHT_FOR_GPS:
 			mCurrentStateTextView.setText("STRAIGHT_FOR_GPS");
 			break;
 		case CORRECTIVE_SCRIPT:
 			mCurrentStateTextView.setText("CORRECTIVE_SCRIPT");
 			break;
 		}
 		mState = newState;
 	}
 
 	private void useArcRadiusToGetHome() {
 		double[] arcResults = new double[2];
 		NavUtils.calculateArc(mCurrentGpsX, mCurrentGpsY, mCurrentGpsHeading,
 				0, 0, arcResults);
 		double turnRadius = arcResults[0];
 		double arcLength = arcResults[1];
 		// call driveArc passing in the radius and arcLength
 		driveArc(turnRadius, arcLength);
 	}
 
 	/**
 	 * Performs the command for the Drive Arc state of the FSM. Receives the
 	 * turn radius and arc length that were calculated from the GPS reading and
 	 * converts them into a command for the Wild Thumper and a stop time.
 	 * 
 	 * @param turnRadius
 	 *            Radius of the pretend circle to drive along. Negative values
 	 *            indicate a turn to the left. Positive values indicate a turn
 	 *            to the right.
 	 * @param arcLength
 	 *            Length of the arc to drive.
 	 */
 	private void driveArc(double turnRadius, double arcLength) {
 		int leftDutyCycle = 255;
 		int rightDutyCycle = 255;
 		int timeToStopMs = 1000;
 		int ftPerSec = 3; // ft per sec
 		leftDutyCycle = (int) Math.round(80.74 * Math.log(turnRadius) - 67.346);
 		rightDutyCycle = (int) Math
 				.round(68.152 * Math.log(turnRadius) - 62.169);
 		timeToStopMs = (int) Math.round(arcLength / ftPerSec);
 		Toast.makeText(this,
 				"Left " + leftDutyCycle + " Right " + rightDutyCycle,
 				Toast.LENGTH_SHORT).show();
 		sendADKCommand("WHEEL SPEED FORWARD " + leftDutyCycle + " FORWARD "
 				+ rightDutyCycle);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED BRAKE 0 BRAKE 0");
 				setState(State.WAITING_FOR_PICKUP);
 			}
 		}, timeToStopMs);
 	}
 
 	private void runScriptRedFigure8() {
 		Toast.makeText(this, "Red Figure Eight", Toast.LENGTH_SHORT).show();
 		
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				setState(State.WAITING_FOR_GPS);
 			}
 		}, 4000);
 	}
 
 	private void runScriptOutandBack() {
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED REVERSE 218 REVERSE 218");
 			}
 		}, 401);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED REVERSE 249 REVERSE 249");
 			}
 		}, 538);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED REVERSE 255 REVERSE 243");
 			}
 		}, 6691);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED REVERSE 255 REVERSE 216");
 			}
 		}, 6830);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED REVERSE 255 REVERSE 186");
 			}
 		}, 7169);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED REVERSE 249 REVERSE 249");
 			}
 		}, 7508);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED REVERSE 238 REVERSE 255");
 			}
 		}, 7946);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED REVERSE 235 REVERSE 255");
 			}
 		}, 8084);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED REVERSE 172 REVERSE 255");
 			}
 		}, 8222);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED REVERSE 142 REVERSE 255");
 			}
 		}, 8361);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED REVERSE 134 REVERSE 255");
 			}
 		}, 8499);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED REVERSE 137 REVERSE 255");
 			}
 		}, 8637);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED REVERSE 249 REVERSE 249");
 			}
 		}, 8877);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED BRAKE 0 BRAKE 0");
 			}
 		}, 12724);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 255");
 			}
 		}, 13262);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 252");
 			}
 		}, 13702);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 213");
 			}
 		}, 13840);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 207");
 			}
 		}, 13978);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 233");
 			}
 		}, 14618);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 137 FORWARD 255");
 			}
 		}, 14756);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 128 FORWARD 255");
 			}
 		}, 14895);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 67 FORWARD 255");
 			}
 		}, 15034);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 255");
 			}
 		}, 15172);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 240 FORWARD 255");
 			}
 		}, 16112);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 150 FORWARD 255");
 			}
 		}, 16250);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 255");
 			}
 		}, 16390);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 95 FORWARD 255");
 			}
 		}, 16829);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 255");
 			}
 		}, 16966);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 255");
 			}
 		}, 17106);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 217 FORWARD 255");
 			}
 		}, 17244);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 104 FORWARD 255");
 			}
 		}, 17382);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 255");
 			}
 		}, 17521);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 126 FORWARD 255");
 			}
 		}, 17760);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 93 FORWARD 255");
 			}
 		}, 17898);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 255");
 			}
 		}, 18036);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 239");
 			}
 		}, 18476);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 200");
 			}
 		}, 18614);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 178");
 			}
 		}, 18752);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 255");
 			}
 		}, 18992);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 238 FORWARD 255");
 			}
 		}, 20132);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 232 FORWARD 255");
 			}
 		}, 20271);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 255");
 			}
 		}, 20409);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 129 FORWARD 255");
 			}
 		}, 20548);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 150 FORWARD 255");
 			}
 		}, 20686);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 255");
 			}
 		}, 20825);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 203 FORWARD 255");
 			}
 		}, 22066);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 151 FORWARD 255");
 			}
 		}, 22204);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 255 FORWARD 255");
 			}
 		}, 22343);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 209 FORWARD 255");
 			}
 		}, 22582);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 196 FORWARD 255");
 			}
 		}, 22720);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 247 FORWARD 255");
 			}
 		}, 22858);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				sendADKCommand("WHEEL SPEED FORWARD 239 FORWARD 255");
 			}
 		}, 23098);
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				setState(State.WAITING_FOR_GPS);
 			}
 		}, 26845);
 	}
 
 	private void runScriptLame() {
 		Toast.makeText(this, "Lame Script", Toast.LENGTH_SHORT).show();
 		
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				setState(State.WAITING_FOR_GPS);
 			}
 		}, 4000);
 	}
 
 	private void runScriptBlueHalfCircle() {
 		Toast.makeText(this, "Blue Half Circle", Toast.LENGTH_SHORT).show();
 		
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				setState(State.WAITING_FOR_GPS);
 			}
 		}, 4000);
 	}
 
 	private void runScriptBlueFigure8() {
 		Toast.makeText(this, "Blue Figure Eight", Toast.LENGTH_SHORT).show();
 		
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				setState(State.WAITING_FOR_GPS);
 			}
 		}, 4000);
 	}
 
 	private void runScriptRedHalfCircle() {
 		Toast.makeText(this, "Red Half Circle", Toast.LENGTH_SHORT).show();
 		
 		mCommandHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				setState(State.WAITING_FOR_GPS);
 			}
 		}, 4000);
 	}
 
 	private void sendADKCommand(String command) {
 		TextView update = new TextView(this);
 		update.setText(command);
		mScrollWindow.addView(update);
		mScroll.fullScroll(View.FOCUS_DOWN);
 		sendCommand(command);
 	}
 }
