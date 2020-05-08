 package com.race604.camera;
 
 import com.race604.image.filter.IFilter;
 import com.race604.image.filter.LomoFilter;
 import com.race604.image.filter.ReliefFilter;
 import com.race604.image.filter.SingleColorFilter;
 import com.race604.image.filter.SpherizeFilter;
 import com.race604.image.filter.SunshineFilter;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.PixelFormat;
 import android.hardware.Camera.ShutterCallback;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 
 public class CameraActivity extends Activity implements OnClickListener {
 
 	private static final String TAG = CameraActivity.class.getName();
 
 	private FilterSurfaceView mSvCameraView;
 	private ImageButton mCaptureBtn;
 
 	private float mAccelerometer[] = new float[3];
 	private float mMagnetic[] = new float[3];
 	private float mDegree;
 
 	private SensorManager mSensorManager;
 	private Sensor aSensor;
 	private Sensor mSensor;
 
 	private static final int MENU_FILER_SINGLE_COLOR = 11;
 	private static final int MENU_FILER_LOMO = 12;
 	private static final int MENU_FILER_SHRINK = 13;
 	private static final int MENU_FILER_RELIEF = 14;
 	private static final int MENU_FILER_SUNSHINE = 15;
 
 	private SensorEventListener mSensorEventListener = new SensorEventListener() {
 
 		@Override
 		public void onSensorChanged(SensorEvent sensorEvent) {
 			switch (sensorEvent.sensor.getType()) {
 			case Sensor.TYPE_MAGNETIC_FIELD:
 				mMagnetic = sensorEvent.values;
 				break;
 			case Sensor.TYPE_ACCELEROMETER:
 				mAccelerometer = sensorEvent.values;
 				break;
 			default:
 				break;
 			}
 			
 			calculateOrientation();
 
 		}
 
 		@Override
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		}
 	};
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		menu.clear();
 		MenuItem mi = null;
 
 		mi = menu.add(Menu.NONE, MENU_FILER_SINGLE_COLOR, Menu.NONE, "One Color");
 		mi = menu.add(Menu.NONE, MENU_FILER_LOMO, Menu.NONE, "Lomo");
 		mi = menu.add(Menu.NONE, MENU_FILER_SHRINK, Menu.NONE, "Shrink");
 		mi = menu.add(Menu.NONE, MENU_FILER_RELIEF, Menu.NONE, "Relief");
		mi = menu.add(Menu.NONE, MENU_FILER_SUNSHINE, Menu.NONE, "Sunshine");
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case MENU_FILER_SINGLE_COLOR: {
 			IFilter filter = new SingleColorFilter();
 			mJpegCallback.setFilter(filter);
 			mSvCameraView.setFilter(filter);
 			break;
 		}
 		case MENU_FILER_LOMO: {
 			IFilter filter = new LomoFilter();
 			mJpegCallback.setFilter(filter);
 			mSvCameraView.setFilter(filter);
 			break;
 		}
 		case MENU_FILER_SHRINK: {
             IFilter filter = new SpherizeFilter();
             mJpegCallback.setFilter(filter);
             mSvCameraView.setFilter(filter);
             break;
         }
 		case MENU_FILER_RELIEF: {
             IFilter filter = new ReliefFilter();
             mJpegCallback.setFilter(filter);
             mSvCameraView.setFilter(filter);
             break;
         }
 		case MENU_FILER_SUNSHINE: {
             IFilter filter = new SunshineFilter();
             mJpegCallback.setFilter(filter);
             mSvCameraView.setFilter(filter);
             break;
         }
 		default:
 			break;
 		}
 
 		return true;
 	}
 
 	private ShutterCallback mShutterCallback = new ShutterCallback() {
 		@Override
 		public void onShutter() {
 			// TODO
 		}
 	};
 
 	private PhotoHandler mJpegCallback = null;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getWindow().setFormat(PixelFormat.TRANSLUCENT);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		setContentView(R.layout.camera_layout);
 
 		mCaptureBtn = (ImageButton) findViewById(R.id.btn_capture);
 		mSvCameraView = (FilterSurfaceView) findViewById(R.id.sv_camera_preview);
 
 		mJpegCallback = new PhotoHandler(this);
 
 		IFilter filter = new ReliefFilter();
 		mJpegCallback.setFilter(filter);
 		mSvCameraView.setFilter(filter);
 
 		mCaptureBtn.setOnClickListener(this);
 
 		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		aSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
 
 	}
 
 	private void calculateOrientation() {
 		float[] values = new float[3];
 		float[] R = new float[9];
 		if (SensorManager.getRotationMatrix(R, null, mAccelerometer, mMagnetic) ) {
 			SensorManager.getOrientation(R, values);
 			mDegree = (float) Math.toDegrees(values[1]);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		mSensorManager.registerListener(mSensorEventListener, aSensor,
 				SensorManager.SENSOR_DELAY_NORMAL);
 		mSensorManager.registerListener(mSensorEventListener, mSensor,
 				SensorManager.SENSOR_DELAY_NORMAL);
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		mSensorManager.unregisterListener(mSensorEventListener);
 		super.onPause();
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.btn_capture: {
 			mJpegCallback.setOritention(mDegree);
 			mSvCameraView.takePicture(mShutterCallback, null, mJpegCallback);
 			break;
 		}
 		default:
 			break;
 		}
 
 	}
 }
