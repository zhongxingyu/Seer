 package edu.wisc.myrotsens;
 
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class RotSensActivity extends Activity implements SensorEventListener {
 
 	private SensorManager mSensorManager;
 	private Sensor mRotVect;
 	private Sensor mAccel;
 	private Sensor mGyro;
 
 	private RotVectMSensor rotVect;
 	private MSensor accel;
 	private MSensor gyro;
 
 	// display Rotation Vector Sensor Data
 	private TextView mXView;
 	private TextView mYView;
 	private TextView mZView;
 	private TextView mAccuView;
 
 	// display Accelerometer data
 	private TextView mAccelXView;
 	private TextView mAccelYView;
 	private TextView mAccelZView;
 	private TextView mAccelAccuView;
 
 	// display Gyroscope data
 	private TextView mGyroXView;
 	private TextView mGyroYView;
 	private TextView mGyroZView;
 	private TextView mGyroAccuView;
 
 	// calibrate variables
 	private Button calibButton;
 	private Button wrFileButton;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_rot_sens);
 
 		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 		// grabbing the composite sensors. data may be averaged or filtered
 		// use getSensorList for grabbing raw sensors
 		mRotVect = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
 		mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
 		registerSens();
 
 		// initialize display
 		mXView = (TextView) findViewById(R.id.textViewXVal);
 		mYView = (TextView) findViewById(R.id.textViewYVal);
 		mZView = (TextView) findViewById(R.id.textViewZVal);
 		mAccuView = (TextView) findViewById(R.id.textViewAccuVal);
 		mAccelXView = (TextView) findViewById(R.id.textViewAccelXVal);
 		mAccelYView = (TextView) findViewById(R.id.textViewAccelYVal);
 		mAccelZView = (TextView) findViewById(R.id.textViewAccelZVal);
 		mAccelAccuView = (TextView) findViewById(R.id.textViewAccelAccuVal);
 		mGyroXView = (TextView) findViewById(R.id.textViewGyroXVal);
 		mGyroYView = (TextView) findViewById(R.id.textViewGyroYVal);
 		mGyroZView = (TextView) findViewById(R.id.textViewGyroZVal);
 		mGyroAccuView = (TextView) findViewById(R.id.textViewGyroAccuVal);
 
 		// create my sensors for accel,gyro and rotVect
 		this.rotVect = new RotVectMSensor(this.getBaseContext(), mRotVect,
 				mXView, mYView, mZView, mAccuView);
 		this.accel = new MSensor(this.getBaseContext(), mAccel, mAccelXView,
 				mAccelYView, mAccelZView, mAccelAccuView);
 		this.gyro = new MSensor(this.getBaseContext(), mGyro, mGyroXView,
 				mGyroYView, mGyroZView, mGyroAccuView);
 
 		this.calibButton = (Button) this.findViewById(R.id.calibrateButton);
 		this.calibButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// wait 1s before calibrating to cancel the effect of pressing
 				// button
 				SystemClock.sleep(1000);
 				rotVect.startCalibrate();
 				accel.startCalibrate();
 				gyro.startCalibrate();
 				Log.i("RotSensActivity", "START CALIBRATION");
 			}
 		});
 
 		this.wrFileButton = (Button) this.findViewById(R.id.wrFileButton);
 		final Context curContext = this.getBaseContext();
 		this.wrFileButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// wait 1s before calibrating to cancel the effect of pressing
 				// button
 				SystemClock.sleep(1000);
 				rotVect.newWrToFile();
 				accel.newWrToFile();
 				gyro.newWrToFile();
 				Toast.makeText(curContext, "start saving to file",
 						Toast.LENGTH_LONG).show();
 				Log.i("RotSensActivity", "START WRITING TO FILE");
 			}
 		});
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		// unregister sensor listeners to prevent the activity from draining the
 		// device's battery.
 		mSensorManager.unregisterListener(this);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		// unregister sensor listeners to prevent the activity from draining the
 		// device's battery.
 		mSensorManager.unregisterListener(this);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		// restore the sensor listeners
 		registerSens();
 	}
 
 	/**
 	 * register needed sensor listeners before using it set the sampling as fast
 	 * as possible
 	 */
 	private void registerSens() {
 		mSensorManager.registerListener(this,
 				mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
 				SensorManager.SENSOR_DELAY_FASTEST);
 		mSensorManager.registerListener(this,
 				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 				SensorManager.SENSOR_DELAY_FASTEST);
 		mSensorManager.registerListener(this,
 				mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
 				SensorManager.SENSOR_DELAY_FASTEST);
 	}
 
 	/**
 	 * implement SensorEventListener interface
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.rot_sens, menu);
 		return true;
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		if (sensor.equals(rotVect.getSensor())) {
 			rotVect.setSensorAccuracy(accuracy);
 			rotVect.updateAccuracyDisplay();
 		} else if (sensor.equals(accel.getSensor())) {
 			accel.setSensorAccuracy(accuracy);
 			accel.updateAccuracyDisplay();
 		} else if (sensor.equals(gyro.getSensor())) {
 			gyro.setSensorAccuracy(accuracy);
 			gyro.updateAccuracyDisplay();
 		}
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		switch (event.sensor.getType()) {
 		case Sensor.TYPE_ROTATION_VECTOR:
 			// calibration
 			if (rotVect.inCalibrate()) {
 				rotVect.calibrate(event.values);
 			} else {
 				// rotation vector gives back 3 values
 				// (x*sin(theta/2),y*sin(theta/2),z*sin(theta/2))
 				// get initial sampled data, which is a rotation vector
 				rotVect.setSampleVals(event.values);
 				// convert the rotation vector to rotation matrix
 				rotVect.rotVect2Mat();
 				// calculate angle changes from the rotation matrix
 				rotVect.rotMat2AngChange();
 				if (rotVect.inWrToFile()) {
 					rotVect.writeToFile();
 				}
 				rotVect.updateSampleDisplay();
 			}
 			break;
 		case Sensor.TYPE_ACCELEROMETER:
 			// calibration
 			if (accel.inCalibrate()) {
 				accel.calibrate(event.values);
 			} else {
 				for (int component = 0; component <= 2; component++) {
 					accel.setSampleVals(event.values);
 				}
 				if (accel.inWrToFile()) {
 					accel.writeToFile();
 				}
 				accel.updateSampleDisplay();
 			}
 			break;
 		case Sensor.TYPE_GYROSCOPE:
 			// calibration
 			if (gyro.inCalibrate()) {
 				gyro.calibrate(event.values);
 			} else {
 				for (int component = 0; component <= 2; component++) {
 					gyro.setSampleVals(event.values);
 				}
 				if (gyro.inWrToFile()) {
 					gyro.writeToFile();
 				}
 				gyro.updateSampleDisplay();
 			}
 			break;
 		}
 	}
 }
