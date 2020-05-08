 package net.kevxu.senselib;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.util.Log;
 
 public class OrientationService implements SensorEventListener {
 
 	private static final String TAG = "SensorService";
 
 	private Context mContext;
 	private SensorManager mSensorManager;
 	private List<OrientationServiceListener> mOrientationServiceListeners;
 
 	private Sensor mGravitySensor;
 	private Sensor mMagneticFieldSensor;
 
 	private OrientationSensorThread mOrientationSensorThread;
 
 	public interface OrientationServiceListener {
 
 		public void onRotationMatrixChanged(float[] R, float[] I);
 
 		public void onMagneticFieldChanged(float[] values);
 
 	}
 
 	protected OrientationService(Context context) throws SensorNotAvailableException {
 		this(context, null);
 	}
 
 	protected OrientationService(Context context, OrientationServiceListener orientationServiceListener) throws SensorNotAvailableException {
 		mContext = context;
 		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
 
 		List<Sensor> gravitySensors = mSensorManager.getSensorList(Sensor.TYPE_GRAVITY);
 		List<Sensor> magneticFieldSensor = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
 
 		if (gravitySensors.size() == 0) {
 			throw new SensorNotAvailableException(Sensor.TYPE_GRAVITY);
 		} else {
 			// Assume the first in the list is the default sensor
 			// Assumption may not be true though
 			mGravitySensor = gravitySensors.get(0);
 		}
 
 		if (magneticFieldSensor.size() == 0) {
 			throw new SensorNotAvailableException(Sensor.TYPE_MAGNETIC_FIELD);
 		} else {
 			// Assume the first in the list is the default sensor
 			// Assumption may not be true though
 			mMagneticFieldSensor = magneticFieldSensor.get(0);
 		}
 
 		mOrientationServiceListeners = new ArrayList<OrientationServiceListener>();
 
 		if (orientationServiceListener != null) {
 			mOrientationServiceListeners.add(orientationServiceListener);
 		}
 	}
 
 	/**
 	 * Call this when resume.
 	 */
 	protected void start() {
 		if (mOrientationSensorThread == null) {
 			mOrientationSensorThread = new OrientationSensorThread();
 			mOrientationSensorThread.start();
 			Log.i(TAG, "OrientationSensorThread started.");
 		}
 
 		if (mSensorManager == null) {
 			mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
 		}
 
 		mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_GAME);
 		Log.i(TAG, "Gravity sensor registered.");
 
 		mSensorManager.registerListener(this, mMagneticFieldSensor, SensorManager.SENSOR_DELAY_GAME);
 		Log.i(TAG, "Magnetic field sensor registered.");
 
 		Log.i(TAG, "OrientationService started.");
 	}
 
 	/**
 	 * Call this when pause.
 	 */
 	protected void stop() {
 		mOrientationSensorThread.terminate();
 		Log.i(TAG, "Waiting for OrientationSensorThread to stop.");
 		try {
 			mOrientationSensorThread.join();
 		} catch (InterruptedException e) {
			Log.e(TAG, e.getMessage(), e);
 		}
 		Log.i(TAG, "OrientationSensorThread stopped.");
 		mOrientationSensorThread = null;
 
 		mSensorManager.unregisterListener(this);
 		Log.i(TAG, "Sensors unregistered.");
 
 		Log.i(TAG, "OrientationService stopped.");
 	}
 
 	@SuppressWarnings("unused")
 	private final class OrientationSensorThread extends AbstractSensorWorkerThread {
 
 		private float[] R;
 		private float[] I;
 		private float[] gravity;
 		private float[] geomagnetic;
 
 		public OrientationSensorThread() {
 			this(DEFAULT_INTERVAL);
 		}
 
 		public OrientationSensorThread(long interval) {
 			super(interval);
 
 			R = new float[9];
 			I = new float[9];
 		}
 
 		public synchronized void pushGravity(float[] values) {
 			if (gravity == null) {
 				gravity = new float[3];
 			}
 
 			System.arraycopy(values, 0, gravity, 0, 3);
 		}
 
 		public synchronized void pushGeomagnetic(float[] values) {
 			if (geomagnetic == null) {
 				geomagnetic = new float[3];
 			}
 
 			System.arraycopy(values, 0, geomagnetic, 0, 3);
 		}
 
 		public synchronized float[] getGravity() {
 			return this.gravity;
 		}
 
 		public synchronized float[] getGeomagnetic() {
 			return this.geomagnetic;
 		}
 
 		@Override
 		public void run() {
 			while (!isTerminated()) {
 				if (getGravity() != null && getGeomagnetic() != null) {
 					SensorManager.getRotationMatrix(R, I, getGravity(), getGeomagnetic());
 				}
 
 				for (OrientationServiceListener listener : mOrientationServiceListeners) {
 					listener.onRotationMatrixChanged(R, I);
 
 					if (getGeomagnetic() != null) {
 						listener.onMagneticFieldChanged(getGeomagnetic());
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
 
 	public void addListener(OrientationServiceListener orientationServiceListener) {
 		if (orientationServiceListener != null) {
 			mOrientationServiceListeners.add(orientationServiceListener);
 		} else {
 			throw new NullPointerException("OrientationServiceListener is null.");
 		}
 	}
 
 	protected void removeListeners() {
 		mOrientationServiceListeners.clear();
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		synchronized (this) {
 			if (mOrientationSensorThread != null) {
 				Sensor sensor = event.sensor;
 				int type = sensor.getType();
 				if (type == Sensor.TYPE_GRAVITY) {
 					mOrientationSensorThread.pushGravity(event.values);
 				} else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
 					mOrientationSensorThread.pushGeomagnetic(event.values);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// Not implemented
 	}
 }
