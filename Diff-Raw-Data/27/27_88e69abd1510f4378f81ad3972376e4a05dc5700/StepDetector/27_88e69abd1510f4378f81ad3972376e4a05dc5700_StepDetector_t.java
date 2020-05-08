 package net.kevxu.senselib;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.kevxu.senselib.OrientationService.OrientationServiceListener;
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.util.Log;
 
 public class StepDetector implements SensorEventListener, OrientationServiceListener {
 
 	private static final String TAG = "StepDetector";
 
 	private Context mContext;
 	private SensorManager mSensorManager;
 	private List<StepListener> mStepListeners;
 
 	private Sensor mLinearAccelSensor;
 	private Sensor mGravitySensor;
 
 	private OrientationService mOrientationService;
 
 	private StepDetectorCalculationThread mStepDetectorCalculationThread;
 
 	public interface StepListener {
 
 		public void onStep();
 
 		// In world's coordinate system, where is tangential to the ground at
 		// the device's current location and points towards the magnetic North
 		// Pole, Z points towards the sky and is perpendicular to the ground,
 		// and X is defined as the vector product YZ
 		public void onMovement(float[] values);
 
 	}
 
 	protected StepDetector(Context context, OrientationService orientationService) throws SensorNotAvailableException {
 		this(context, orientationService, null);
 	}
 
 	protected StepDetector(Context context, OrientationService orientationService, StepListener stepListener) throws SensorNotAvailableException {
 		mContext = context;
 		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
 
 		mOrientationService = orientationService;
 		mOrientationService.addListener(this);
 
 		List<Sensor> liearAccelSensors = mSensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
 		List<Sensor> gravitySensors = mSensorManager.getSensorList(Sensor.TYPE_GRAVITY);
 
 		if (liearAccelSensors.size() == 0) {
 			throw new SensorNotAvailableException(Sensor.TYPE_LINEAR_ACCELERATION);
 		} else {
 			// Assume the first in the list is the default sensor
 			// Assumption may not be true though
 			mLinearAccelSensor = liearAccelSensors.get(0);
 		}
 
 		if (gravitySensors.size() == 0) {
 			throw new SensorNotAvailableException(Sensor.TYPE_GRAVITY);
 		} else {
 			// Assume the first in the list is the default sensor
 			// Assumption may not be true though
 			mGravitySensor = gravitySensors.get(0);
 		}
 
 		mStepListeners = new ArrayList<StepListener>();
 
 		if (stepListener != null) {
 			mStepListeners.add(stepListener);
 		}
 	}
 
 	/**
 	 * Call this when resume.
 	 */
 	protected void start() {
 		if (mStepDetectorCalculationThread == null) {
 			mStepDetectorCalculationThread = new StepDetectorCalculationThread();
 			mStepDetectorCalculationThread.start();
 			Log.i(TAG, "StepDetectorCalculationThread started.");
 		}
 
 		if (mSensorManager == null) {
 			mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
 		}
 
 		mSensorManager.registerListener(this, mLinearAccelSensor, SensorManager.SENSOR_DELAY_GAME);
 		Log.i(TAG, "Linear acceleration sensor registered.");
 
 		mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_GAME);
 		Log.i(TAG, "Gravity sesnor registered.");
 
 		Log.i(TAG, "StepDetector started.");
 	}
 
 	/**
 	 * Call this when pause.
 	 */
 	protected void stop() {
 		mStepDetectorCalculationThread.terminate();
 		Log.i(TAG, "Waiting for StepDetectorCalculationThread to stop.");
 		try {
 			mStepDetectorCalculationThread.join();
 		} catch (InterruptedException e) {
			Log.w(TAG, e.getMessage(), e);
 		}
 		Log.i(TAG, "StepDetectorCalculationThread stopped.");
 		mStepDetectorCalculationThread = null;
 
 		mSensorManager.unregisterListener(this);
 		Log.i(TAG, "Sensors unregistered.");
 
 		Log.i(TAG, "StepDetector stopped.");
 	}
 
 	@SuppressWarnings("unused")
 	private final class StepDetectorCalculationThread extends AbstractSensorWorkerThread {
 
 		private static final long DEFAULT_INTERVAL = 80;
 		private static final float DEFAULT_LIMIT = 1.0F;
 
 		private final float limit;
 
 		private float[] linearAccel;
 		private float[] gravity;
 		private float[] rotationMatrix;
 
 		public StepDetectorCalculationThread() {
 			this(DEFAULT_INTERVAL, DEFAULT_LIMIT);
 		}
 
 		public StepDetectorCalculationThread(long interval) {
 			this(interval, DEFAULT_LIMIT);
 		}
 
 		public StepDetectorCalculationThread(long interval, float limit) {
 			super(interval);
 
 			this.limit = limit;
 		}
 
 		public synchronized void pushLinearAccel(float[] values) {
 			if (linearAccel == null) {
 				linearAccel = new float[3];
 			}
 
 			System.arraycopy(values, 0, linearAccel, 0, 3);
 			// for (int i = 0; i < 3; i++) {
 			// linearAccel[i] = values[i];
 			// }
 		}
 
 		public synchronized void pushGravity(float[] values) {
 			if (gravity == null) {
 				gravity = new float[3];
 			}
 
 			System.arraycopy(values, 0, gravity, 0, 3);
 			// for (int i = 0; i < 3; i++) {
 			// gravity[i] = values[i];
 			// }
 		}
 
 		public synchronized void pushRotationMatrix(float[] R) {
 			if (rotationMatrix == null) {
 				rotationMatrix = new float[9];
 			}
 
 			System.arraycopy(R, 0, rotationMatrix, 0, 9);
 			// for (int i = 0; i < 9; i++) {
 			// rotationMatrix[i] = R[i];
 			// }
 		}
 
 		public synchronized float[] getLinearAccel() {
 			return linearAccel;
 		}
 
 		public synchronized float[] getGravity() {
 			return gravity;
 		}
 
 		public synchronized float[] getRotationMatrix() {
 			return rotationMatrix;
 		}
 
 		private float getAccelInGravityDirection(float[] linearAccel, float[] gravity) {
 			// float gravityScalar = SensorManager.GRAVITY_EARTH;
 			float gravityScalar = (float) Math.sqrt(gravity[0] * gravity[0]
 					+ gravity[1] * gravity[1] + gravity[2] * gravity[2]);
 			float dotProduct = linearAccel[0] * gravity[0] + linearAccel[1]
 					* gravity[1] + linearAccel[2] * gravity[2];
 
 			float aigd = (float) (dotProduct / gravityScalar);
 
 			return aigd;
 		}
 
 		private void getAccelInWorldCoordinateSystem(float[] aiwcs, float[] linearAccel, float[] rotationMatrix) {
 			aiwcs[0] = linearAccel[0] * rotationMatrix[0] + linearAccel[1]
 					* rotationMatrix[1] + linearAccel[2] * rotationMatrix[2];
 			aiwcs[1] = linearAccel[0] * rotationMatrix[3] + linearAccel[1]
 					* rotationMatrix[4] + linearAccel[2] * rotationMatrix[5];
 			aiwcs[2] = linearAccel[0] * rotationMatrix[6] + linearAccel[1]
 					* rotationMatrix[7] + linearAccel[2] * rotationMatrix[8];
 		}
 
 		@Override
 		public void run() {
 			boolean readyForStep = false;
 			float previousForReadyValue = 0.0F;
 
 			float[] aiwcs = new float[3];
 
 			while (!isTerminated()) {
 				if (getGravity() != null && getLinearAccel() != null) {
 					// if (getLinearAccel() != null) {
 					boolean step = false;
 
 					float[] linearAccel = getLinearAccel();
 					// float[] gravity = getGravity();
 					float[] rotationMatrix = getRotationMatrix();
 					getAccelInWorldCoordinateSystem(aiwcs, linearAccel, rotationMatrix);
 
 					float accelInGravityDirection = getAccelInGravityDirection(linearAccel, gravity);
 					// float accelInGravityDirection = aiwcs[2];
 
 					if (!readyForStep) {
 						if (Math.abs(accelInGravityDirection) > limit) {
 							previousForReadyValue = accelInGravityDirection;
 							readyForStep = true;
 						}
 					} else {
 						if ((previousForReadyValue < 0 && accelInGravityDirection > limit)
 								|| (previousForReadyValue > 0 && accelInGravityDirection < -limit)) {
 							step = true;
 							readyForStep = false;
 						}
 					}
 
 					for (StepListener listener : mStepListeners) {
 						if (step) {
 							listener.onStep();
 						}
 						listener.onMovement(aiwcs);
 					}
 				}
 
 				try {
 					Thread.sleep(getInterval());
 				} catch (InterruptedException e) {
 					Log.w(TAG, e.getMessage(), e);
 				}
 			}
 		}
 
 	}
 
 	public void addListener(StepListener stepListener) {
 		if (stepListener != null) {
 			mStepListeners.add(stepListener);
 		} else {
 			throw new NullPointerException("StepListener is null.");
 		}
 	}
 
 	protected void removeListeners() {
 		mStepListeners.clear();
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// Not implemented
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		synchronized (this) {
 			if (mStepDetectorCalculationThread != null) {
 				Sensor sensor = event.sensor;
 				if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
 					mStepDetectorCalculationThread.pushLinearAccel(event.values);
 				} else if (sensor.getType() == Sensor.TYPE_GRAVITY) {
 					mStepDetectorCalculationThread.pushGravity(event.values);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onRotationMatrixChanged(float[] R, float[] I) {
 		synchronized (this) {
 			if (mStepDetectorCalculationThread != null) {
 				mStepDetectorCalculationThread.pushRotationMatrix(R);
 			}
 		}
 	}
 
 	@Override
 	public void onMagneticFieldChanged(float[] values) {
 		// Not implemented.
 
 	}
 
 }
