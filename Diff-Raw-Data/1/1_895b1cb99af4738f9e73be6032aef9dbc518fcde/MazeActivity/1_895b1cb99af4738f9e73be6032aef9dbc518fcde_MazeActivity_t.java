 package com.github.elixiroflife4u;
 
 import android.app.Activity;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 public class MazeActivity extends Activity implements OnTouchListener, SensorEventListener{
 
 	private MazeView mazeview;
 	private SensorManager mSensorManager;
     private Sensor mAccelerometer;
     private WakeLock mWakeLock;
    private GestureDetector gd;
     
 	@Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
         mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
         if(mAccelerometer == null){ //should die here.
         	Log.v("Error", "Could not find an accelarometer. Should have failed here.");
         }
         
         // Get an instance of the PowerManager
         PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
         // Create a bright wake lock
         mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());
         
 		Bundle extras = getIntent().getExtras();
 		int nx = 4, ny = 7;
 		if (extras != null)
 		{
 			String difficulty = extras.getString("level");
 			if (difficulty.equalsIgnoreCase("medium")) {
 				nx = 6;
 				ny = 11;
 			}
 			else if (difficulty.equalsIgnoreCase("difficult")) {
 				nx = 8;
 				ny = 14;
 			}
 		}
 		
     	mazeview = new MazeView(this);
 		mazeview.setBackgroundColor(0xFFFFFFFF);
 		mazeview.setGridDim(nx,ny);
 		mazeview.setBallPosition(0, 0);
 		setContentView(mazeview);
 
 		gd = new GestureDetector(this, new MyGestureListener()); 
 
 		mazeview.setOnTouchListener(this);
 	}
 	
 	public boolean onTouch(View v, MotionEvent event)
 	{
 		return this.onTouchEvent(v, event);
 	}
 	public boolean onTouchEvent(View v, MotionEvent event) {
 		System.err.println("touched");
 		//gd.onTouchEvent(event);
 		//return super.onTouchEvent(event);
 		if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
 			float x = event.getX();
 			float y = event.getY();
 			Log.v("maze", "touch x="+x+" y="+y);
 			
 			if (y < 200.f)
 				mazeview.shiftBallUp(8.f);
 			else if (y > 500.f)
 				mazeview.shiftBallDown(8.f);
 			else if (x < 200.f)
 				mazeview.shiftBallLeft(8.f);
 			else
 				mazeview.shiftBallRight(8.f);
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		mSensorManager.unregisterListener(this);
 		mWakeLock.release();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
 		mWakeLock.acquire();
 	}
 
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		// TODO Auto-generated method stub
 		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
             return;
 //		long curTime = System.currentTimeMillis();
 		// only allow one update every 50ms, otherwise updates come way too fast
 //		if (lastSensorUpdate == -1 || (curTime - lastSensorUpdate) > 5) {
 //			lastSensorUpdate = curTime;
 //			mSensorTimeStamp = event.timestamp;
 //			mCpuTimeStamp = System.nanoTime();
 			System.out.println("x: "+ (event.values[0]));
 			System.out.println("y: "+ (event.values[1]));
 			System.out.println("z: "+ (event.values[2]));
 			float accx = event.values[0];
 			float accy = event.values[1];
 			
 			//final float shiftAmt = 4.f;
 			//final float thresh = 1.2f;
 			
 //			if(accx > thresh)
 //				mazeview.shiftBallLeft(shiftAmt);
 //			if(accx < -thresh)
 //				mazeview.shiftBallRight(shiftAmt);
 //			if(accy > thresh)
 //				mazeview.shiftBallDown(shiftAmt);
 //			if(accy < -thresh)
 //				mazeview.shiftBallUp(shiftAmt);
 			
 			mazeview.ballDeltaV(-accx, accy);
 			
 			
 //		}
 		
 	}
 	
 	
 	
 	// GESTURE CLASS:
 	class MyGestureListener extends GestureDetector.SimpleOnGestureListener{
 		public GestureDetector detector; 
 		
 		 @Override  
 	     public boolean onDoubleTap(MotionEvent e)  
 	     {
 			 	mazeview.setMagnify();
 			 	System.err.println("DOUBLE TAP");
 			 	return true;
 	     }
 		 @Override
 		 public boolean onSingleTapConfirmed(MotionEvent event)
 		 {
 			 System.err.println("SINGLE TAP?");
 			 if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
 					float x = event.getX();
 					float y = event.getY();
 					Log.v("maze", "touch x="+x+" y="+y);
 					
 					if (y < 200.f)
 						mazeview.shiftBallUp(8.f);
 					else if (y > 500.f)
 						mazeview.shiftBallDown(8.f);
 					else if (x < 200.f)
 						mazeview.shiftBallLeft(8.f);
 					else
 						mazeview.shiftBallRight(8.f);
 					return true;
 				}
 				return false;
 		 }
 	}
 	
 }
