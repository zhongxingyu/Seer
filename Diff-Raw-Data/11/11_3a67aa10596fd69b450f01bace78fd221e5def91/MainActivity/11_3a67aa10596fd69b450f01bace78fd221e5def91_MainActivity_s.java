 package com.example.pointerapp;
 
 //import junit.framework.Assert;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.app.Activity;
 import android.app.Fragment;
 import android.content.Context;
 import android.database.Cursor;
 import android.gesture.Gesture;
 import android.gesture.GestureLibraries;
 import android.gesture.GestureLibrary;
 import android.gesture.GestureOverlayView;
 import android.gesture.Prediction;
 import android.gesture.GestureOverlayView.OnGesturePerformedListener;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class MainActivity extends Activity{
 	//	private final float NOISE = (float) 2.0;
 	//
 	//	private GestureLibrary mLib;
 	private SensorManager mManager;
 	private Sensor mSensor;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		//populate activity with appropriate data
 		//		populate();
 
 		//		//gesture library
 		//		GestureOverlayView gestureView = new GestureOverlayView(this);
 		//		View inflate = getLayoutInflater().inflate(R.layout.activity_main, null); //null Viewgroup
 		//		gestureView.addView(inflate);
 		//		gestureView.addOnGesturePerformedListener(this);
 		//		mLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
 		//		if (!mLib.load()) {
 		//			finish();
 		//		}
 		//setContentView(gestureView);
 
 		//display finger cursor on single canvas
 		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), 
 				R.drawable.cursor);
		MousePointerView pointerView = new MousePointerView(this, bitmap);
 		setContentView(pointerView);
 
 		//		//fragment
 		//		if (findViewById(R.id.fragment_container) != null) {
 		//			if (savedInstanceState != null) {
 		//				return;
 		//			}
 		//			Fragment frag = new Fragment();
 		//			frag.setArguments(getIntent().getExtras());
 		//			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
 		//			transaction.add(R.id.fragment_container, frag).commit();
 		//		}
 
 		registerAccelerometer();
 	}
 	private void registerAccelerometer() {
 		mManager = (SensorManager)getSystemService(SENSOR_SERVICE);
 		mSensor = mManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
 		mManager.registerListener(mListener,mSensor,mManager.SENSOR_DELAY_NORMAL);
 	}
 
 	//	private void populate() {
 	//		Cursor query = getContentResolver().query(ContactsContract.AUTHORITY_URI,
 	//				new String[] {ContactsContract.PRIMARY_ACCOUNT_NAME}, null, null, null);
 	//		
 	//	}
 
 
 	// add helper method to access and manip control to connect remotely
 	//	@Override
 	//	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
 	//		ArrayList<Prediction> predictions = mLib.recognize(gesture);
 	//		Log.v("performed", "performed");
 	//		//at least one prediction
 	//		if (predictions.size() > 0) {
 	//			Prediction prediction = predictions.get(0);
 	//			if (prediction.score > 1.0) {
 	//				String action = predictions.get(0).name;
 	//				if (action.equalsIgnoreCase("action_RIGHT")) {
 	//					//test1
 	//				} else if (action.equalsIgnoreCase("action_LEFT")) {
 	//					//test2
 	//				} else if (action.equalsIgnoreCase("action_REFRESH")) {
 	//					//test3
 	//				}
 	//					
 	//			}
 	//		}
 	//	}
 	//	public static int getDrawable(Context context, String name) {
 	//		return context.getResources().getIdentifier(name, "drawable",
 	//				context.getPackageName());
 	//	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	private SensorEventListener mListener = new SensorEventListener() {
 		@Override
 		public void onAccuracyChanged(Sensor arg0, int arg1) {
 			return;
 		}
 
 		private boolean FLAG_INIT = false;
 
 		@Override
 		public void onSensorChanged(SensorEvent event) {
 			int mType = event.sensor.getType();
			if (mType == Sensor.TYPE_LINEAR_ACCELERATION && FLAG_INIT==false) {
 				integrate(event);
 				//redraw position of cursor change
				FLAG_INIT = true;
 			}
 		}
 		private float[] Accl = new float[3];
 		private float[] mData = new float[3];
 		private float[] Vel = new float[3];
 		private float[] iVel = new float[3];
 		private float[] Disp = new float[3];
 		public void integrate(SensorEvent event) {
 			float dt =(float)(System.currentTimeMillis() - event.timestamp)/1000000000f;
 			mData = event.values;
 			for(int i = 0; i < 3; i++){
 				Accl[i] += mData[i];
 				Vel[i] = iVel[i] + (Accl[i] * dt);
 				iVel[i] = Vel[i];
 				Disp[i] = 0 + Vel[i] * dt;
 			}
 		}
 	};
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mManager.registerListener(mListener,
 				mManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
 				SensorManager.SENSOR_DELAY_NORMAL);
 	}
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (mSensor != null) {
 			mManager.unregisterListener(mListener);
 		}
 	}
 
 
 
 }
