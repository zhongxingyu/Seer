 package com.barleysoft.blitzn;
 
 import com.barleysoft.despiker.RunningMedianGenerator;
 import com.barleysoft.despiker.NotEnoughData;
 
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.util.Log;
 
 public class PitchFlipListener implements SensorEventListener {
 	// Aggregate State can be UP, DOWN, SIDE
 	public static final int UP = 0;
 	public static final int DOWN = 1;
 	public static final int SIDE = 9;
 
 	// Pitch can be PITCH_FORWARD, PITCH_UP, PITCH_BACKWARDS, PITCH_DOWN
 	public static final int PITCH_FORWARD = 2;
 	public static final int PITCH_UP = 3;
 	public static final int PITCH_BACKWARDS = 4;
 	public static final int PITCH_DOWN = 5;
 
 	// Roll can be ROLL_UPRIGHT, ROLL_RIGHT, ROLL_CAPSIZED, ROLL_LEFT
 	public static final int ROLL_LEVEL = 6;
 	public static final int ROLL_RIGHT = 7;
 	public static final int ROLL_LEFT = 8;
 
 	// Parameters
 	// 25 was empirically derived on an HTC Incredible
 	// 10 was too sensitive and 25 was too laggy
 	private static final int WINDOW_SIZE = 15;
 
 	// These are separate so that we can make UP or DOWN more sensitive
 	private static final int ANGLE_THRESHOLD = 45; // degrees
 
 	private int mState = UP;
 	// private MovingAverageGenerator mPitchWindow = new
 	// MovingAverageGenerator(WINDOW_SIZE);
 	private RunningMedianGenerator mPitchWindow = new RunningMedianGenerator(
 			WINDOW_SIZE);
 
 	private RunningMedianGenerator mRollWindow = new RunningMedianGenerator(
 			WINDOW_SIZE);
 
 	private Context mContext;
 	private OnPitchFlipListener mPitchFlipListener;
 	private Sensor mOrientSensor;
 	private SensorManager mSensorMgr;
 
 	public interface OnPitchFlipListener {
 		public void onPitchFlip(int state);
 	}
 
 	public PitchFlipListener(Context context) {
 		mContext = context;
 		resume();
 	}
 
 	public void setOnPitchFlipListener(OnPitchFlipListener listener) {
 		mPitchFlipListener = listener;
 	}
 
 	public void resume() {
 		mSensorMgr = (SensorManager) mContext
 				.getSystemService(Context.SENSOR_SERVICE);
 		if (mSensorMgr == null) {
 			throw new UnsupportedOperationException("Sensors not supported");
 		}
 
 		mOrientSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION);
 		boolean supported = mSensorMgr.registerListener(this, mOrientSensor,
 				SensorManager.SENSOR_DELAY_GAME);
 
 		if (!supported) {
 			mSensorMgr.unregisterListener(this, mOrientSensor);
 			throw new UnsupportedOperationException("Orientation not supported");
 		}
 	}
 
 	public void pause() {
 		if (mSensorMgr != null) {
 			mSensorMgr.unregisterListener(this, mOrientSensor);
 			mSensorMgr = null;
 			mOrientSensor = null;
 		}
 	}
 
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public String getPrettyState(int state) {
 		switch (state) {
 		case UP:
 			return "UP";
 		case DOWN:
 			return "DOWN";
 		case SIDE:
 			return "SIDE";
 		case PITCH_FORWARD:
 			return "PITCH_FORWARD";
 		case PITCH_UP:
 			return "PITCH_UP";
 		case PITCH_BACKWARDS:
 			return "PITCH_BACKWARDS";
 		case PITCH_DOWN:
 			return "PITCH_DOWN";
 		case ROLL_LEVEL:
 			return "ROLL_LEVEL";
 		case ROLL_LEFT:
 			return "ROLL_LEFT";
 		case ROLL_RIGHT:
 			return "ROLL_RIGHT";
 		default:
 			return "UNKNOWN";
 		}
 	}
 
 	private int computeCurrentPitchState(float pitch) {
 		final float FORWARD = 0.0f;
 		final float UP = -90.0f;
 		final float DOWN = 90.0f;
 
 		float smoothedPitch;
 		try {
 			smoothedPitch = mPitchWindow.addAndCompute(pitch);
 		} catch (NotEnoughData e) {
 			// Return while we accrue readings
 			return -1;
 		}
 
		Log.i("PitchFlipListner", "smoothedPitch=" +
				String.valueOf(smoothedPitch));
 		int state;
 		if (Math.abs(FORWARD - smoothedPitch) < ANGLE_THRESHOLD)
 			state = PITCH_FORWARD;
		else if (Math.abs(UP - smoothedPitch) <= ANGLE_THRESHOLD)
 			state = PITCH_UP;
		else if (Math.abs(DOWN - smoothedPitch) <= ANGLE_THRESHOLD)
 			state = PITCH_DOWN;
 		else
 			state = PITCH_BACKWARDS;
 
 		return state;
 	}
 
 	private int computeCurrentRollState(float roll) {
 		final float RIGHT = -90.0f;
 		final float LEFT = 90.0f;
 
 		float smoothedRoll;
 		try {
 			smoothedRoll = mRollWindow.addAndCompute(roll);
 		} catch (NotEnoughData e) {
 			// Return while we accrue readings
 			return -1;
 		}
 
 		int state;
 		if (Math.abs(RIGHT - smoothedRoll) < ANGLE_THRESHOLD)
 			state = ROLL_RIGHT;
 		else if (Math.abs(LEFT - smoothedRoll) < ANGLE_THRESHOLD)
 			state = ROLL_LEFT;
 		else
 			state = ROLL_LEVEL;
 
 		return state;
 	}
 
 	public void onSensorChanged(SensorEvent event) {
 		// Pitch
 		float pitch = event.values[1];
 		int curPitchState = computeCurrentPitchState(pitch);
 		// NOTE(sirp): should this be a try/except
 		if (curPitchState == -1)
 			return;
 
 		// Roll
 		float roll = event.values[2];
 		int curRollState = computeCurrentRollState(roll);
 		// NOTE(sirp): should this be a try/except
 		if (curRollState == -1)
 			return;
 
 		boolean pitchLevel = (curPitchState == PITCH_FORWARD)
 				|| (curPitchState == PITCH_BACKWARDS);
 		boolean rollLevel = (curRollState == ROLL_LEVEL);
 
 		// Aggregate state basically says if the either the phone is pitched
 		// up or rolled, then consider that on it's side. Otherwise, we're
 		// UP if pitch forward and DOWN pitch backwards
 		int aggregateState;
 		if (pitchLevel && rollLevel) {
 			aggregateState = (curPitchState == PITCH_FORWARD) ? UP : DOWN;
 		} else {
 			aggregateState = SIDE;
 		}
 
 		boolean flip = false;
 		if ((mState == DOWN) && (aggregateState == UP)) {
 			// DOWN -> UP
 			flip = true;
 			mState = UP;
 		} else if ((mState == UP) && (aggregateState == DOWN)) {
 			// UP -> DOWN
 			flip = true;
 			mState = DOWN;
 		}
 
 		String logMsg = "mState=" + getPrettyState(mState);
 		logMsg += " aggregateState=" + getPrettyState(aggregateState);
 		logMsg += " curPitchState=" + getPrettyState(curPitchState);
 		logMsg += " curRollState=" + getPrettyState(curRollState);
 		logMsg += " pitch=" + String.valueOf(pitch);
 		logMsg += " roll=" + String.valueOf(roll);
 
 		Log.i("PitchFlipListener", logMsg);
 
 		if (flip) {
 			Log.i("PitchFlipListener", "pitch flipped");
 			mPitchFlipListener.onPitchFlip(mState);
 		}
 
 	}
 
 }
