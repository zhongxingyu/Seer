 package net.kevxu.senselib;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.util.Log;
 
 public class StepDetector implements SensorEventListener {
 
 	private static final String TAG = "StepDetector";
 
	private static final int POOL_SIZE = 500;
 
 	private Context mContext;
 	private SensorManager mSensorManager;
 	private List<StepListener> mStepListeners;
 
 	private Sensor mLinearAccelSensor;
 	private Sensor mGravitySensor;
 
 	private StepDetectorDataPool mDataPool;
 
 	private StepDetectorCalculationThread mStepDetectorCalculationThread;
 
 	public interface StepListener {
 		public void onStep();
 	}
 
 	public StepDetector(Context context) throws SensorNotAvailableException {
 		this(context, null);
 	}
 
 	public StepDetector(Context context, StepListener stepListener) throws SensorNotAvailableException {
 		mContext = context;
 		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
 
 		List<Sensor> liearAccelSensors = mSensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
 		List<Sensor> gravitySensors = mSensorManager.getSensorList(Sensor.TYPE_GRAVITY);
 
 		if (liearAccelSensors.size() == 0) {
 			throw new SensorNotAvailableException(Sensor.TYPE_LINEAR_ACCELERATION);
 		} else {
 			mLinearAccelSensor = liearAccelSensors.get(0);
 		}
 
 		if (gravitySensors.size() == 0) {
 			throw new SensorNotAvailableException(Sensor.TYPE_GRAVITY);
 		} else {
 			mGravitySensor = gravitySensors.get(0);
 		}
 
 		mSensorManager.registerListener(this, mLinearAccelSensor, SensorManager.SENSOR_DELAY_GAME);
 		mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_GAME);
 
 		mStepListeners = new ArrayList<StepListener>();
 
 		if (stepListener != null) {
 			mStepListeners.add(stepListener);
 		}
 
 		mDataPool = new StepDetectorDataPool(POOL_SIZE);
 
 		mStepDetectorCalculationThread = new StepDetectorCalculationThread();
 		mStepDetectorCalculationThread.start();
 	}
 
 	private class StepDetectorCalculationThread extends Thread {
 
 		private volatile boolean stopped;
 
 		private long interval;
 
 		public StepDetectorCalculationThread() {
 			stopped = false;
 			interval = 100;
 		}
 
 		public void terminate() {
 			stopped = true;
 		}
 
 		public float getAccelInGravityDirection(float[] linearAccel, float[] gravity) {
 			float gravityScalar = SensorManager.GRAVITY_EARTH;
 			float dotProduct = linearAccel[0] * gravity[0] + linearAccel[1]
 					* gravity[1] + linearAccel[2] * gravity[2];
 
 			float aigd = (float) (dotProduct / gravityScalar);
 
			Log.v(TAG, "l2: " + linearAccel[2] + "\tg2: " + gravity[2]);
 
 			return aigd;
 		}
 
 		@Override
 		public void run() {
 			while (!stopped) {
 				if (mDataPool.getSize(Sensor.TYPE_LINEAR_ACCELERATION) > 0
 						&& mDataPool.getSize(Sensor.TYPE_GRAVITY) > 0) {
 					float[] linearAccel = mDataPool.getLatest(Sensor.TYPE_LINEAR_ACCELERATION);
 					float[] gravity = mDataPool.getLatest(Sensor.TYPE_GRAVITY);
 
 					float accelInGravityDirection = getAccelInGravityDirection(linearAccel, gravity);
 
 					for (StepListener listener : mStepListeners) {
 						// TODO
 					}
 				}
 
 				try {
 					Thread.sleep(interval);
 				} catch (InterruptedException e) {
 					Log.w(TAG, e.getMessage(), e);
 				}
 			}
 		}
 
 	}
 
 	public void addListener(StepListener stepListener) {
 		mStepListeners.add(stepListener);
 	}
 
 	public void addListeners(List<StepListener> stepListeners) {
 		if (stepListeners.size() > 0) {
 			mStepListeners.addAll(stepListeners);
 		}
 	}
 
 	public void removeListeners() {
 		mStepListeners.clear();
 	}
 
 	/**
 	 * Call this when pause.
 	 */
 	public void close() {
 		mStepDetectorCalculationThread.terminate();
 		Log.i(TAG, "Waiting for StepDetectorCalculationThread to terminate.");
 		try {
 			mStepDetectorCalculationThread.join();
 		} catch (InterruptedException e) {
 			Log.w(TAG, e.getMessage(), e);
 		}
 		Log.i(TAG, "StepDetectorCalculationThread terminated.");
 		mStepDetectorCalculationThread = null;
 
 		mSensorManager.unregisterListener(this);
 	}
 
 	/**
 	 * Call this when resume.
 	 */
 	public void reload() {
 		if (mStepDetectorCalculationThread == null) {
 			mStepDetectorCalculationThread = new StepDetectorCalculationThread();
 			mStepDetectorCalculationThread.start();
 			Log.i(TAG, "StepDetectorCalculationThread reloaded.");
 		}
 
 		mSensorManager.registerListener(this, mLinearAccelSensor, SensorManager.SENSOR_DELAY_GAME);
 		mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_GAME);
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// Not implemented
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
		synchronized (this) {
			Sensor sensor = event.sensor;
			mDataPool.addData(sensor.getType(), event.values);
			// Log.v(TAG, mDataPool.toString());
		}
 	}
 
 }
