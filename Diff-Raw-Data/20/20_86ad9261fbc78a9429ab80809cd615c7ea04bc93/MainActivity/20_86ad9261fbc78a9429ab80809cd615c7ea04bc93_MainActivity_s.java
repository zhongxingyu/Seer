 package com.example.digitalmeasuringtape;
 
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements Runnable, SensorEventListener{
 
 	private static String pi_string;
 	private static TextView tv;
 	private boolean activeThread = true;
 	private SensorManager mSensorManager;
 	private Sensor mAccelerometer, mOrientation;
 	private PhysicsManager physics;
 	public SharedPreferences sPrefs;
 	public TailLinkedList measurements;
 	public float greatestX, greatestY, greatestZ, lastAzimuth, firstAzimuth;
 	public CountDownLatch gate; //things call gate.await(), and get blocked.
 								//things become unblocked when gate.countDown()
 								//is called enough times, which will be 1
 	
 	protected void onExit()
 	{
 		if(mSensorManager != null)
 			mSensorManager.unregisterListener(this);
 		onDestroy();
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		tv = (TextView) this.findViewById(R.id.text1);
 		tv.setText("--");
 		
 		sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 		physics = new PhysicsManager(this);
 		
 		//setting up sensor managers
 		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);		
 		
 		//hookup button
 		Button button = (Button)findViewById(R.id.button1);
 		button.setOnTouchListener(myListener);
 		
 		SharedPreferences.Editor editor = sPrefs.edit();
 		editor.putBoolean("MeasureX", true);
 		editor.putBoolean("MeasureY", false);
 		editor.putBoolean("MeasureZ", false);
 		
 		editor.putBoolean("Eulers", false);
 		editor.putBoolean("ImprovedEulers", false);
 		editor.putBoolean("Simpsons", true);
 		
 		editor.putBoolean("PathMode", false);
 		editor.commit();
 		//-----
 		
 		
 		//check if calibrated
 		iCalibrate();
 	}
 	
 	public void iCalibrate()
 	{
 		//check if Calibrated is true
 		boolean CALIBRATED = sPrefs.getBoolean("calibrated_pref_check", false);
 
 		if (!CALIBRATED){
 			//popup and say we need to calibrate
 			final Intent i = new Intent(this,Calibrate.class);		
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setPositiveButton("Ok. Calibrate me!", new DialogInterface.OnClickListener(){
 				@Override
 				public void onClick(DialogInterface dialog, int id){
 					//continue
 					dialog.dismiss();
 					startActivity(i);
 				}
 			});
 			builder.setMessage("We can't measure without Calibrating! Place the phone somewhere level and give us a few seconds to calibrate.").setTitle("WAIT!");
 			AlertDialog dialog = builder.create();
 			dialog.show();			
 		}
 	}
 	
 	@Override
 	protected void onRestart(){
 		super.onRestart();
 		
 		iCalibrate();
 		
 		tv = (TextView) this.findViewById(R.id.text1);
 		tv.setText("--");		
 	}
 	
 	//temporary to make sure this app isn't the one draining my battery...
 	@Override
 	protected void onStop(){
 		super.onStop();
 		onDestroy();
 //		TODO dialog.dismiss();
 	}
 	
 	private OnTouchListener myListener = new OnTouchListener(){
 	    @Override
 		public boolean onTouch(View v, MotionEvent event) {
 	        if(event.getAction() == MotionEvent.ACTION_DOWN) {
 	            //start recording
 	        	System.out.println("DOWN");
 	        	start_distance_process();
 	        	
 	        } else if (event.getAction() == MotionEvent.ACTION_UP) {
 	        	System.out.println("UP");
 	        	//kill thread on release of button
 				activeThread = false;
 				if(gate!=null)
 	            	gate.countDown(); 	
 	        }
 	        return true;
 	    }
 	};
 	
 	//connected to button's onClick
 	public void start_distance_process(){
 		//start thread
 		System.out.println("Started distance process.");
 		Thread thread = new Thread(this);
 		thread.start();
 	}
 	
 /************menu stuff**************/
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 //		Intent intent = new Intent(this, Settings.class);
 //		startActivity(intent);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item){
 		switch (item.getItemId()){
 //		case R.id.about_menuitem:
 //			startActivity(new Intent(this, About.class));
 		case R.id.settings_menuitem:
 			startActivity(new Intent(this, Settings.class));
 		}
 		return true;
 	}
 	
 /***********end menu stuff***********/	
 	
 	//main method for thread
 	@Override
 	public void run()
 	{		
 		Measure();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void Measure(){
 		
 		System.out.println("Calling Measure");
 		
 		Collect();
 		
 		pi_string = "calculating";
 		handler.sendEmptyMessage(0);
 		
 		measurements.unravel();
 		
 		//correcting for if phone rotated about Z at any point
 		physics.Straighten(measurements.xData, measurements.azimuthData);
 		
 		//saving data		
 		String xString = measurements.listToString(measurements.xData, "x");
 		String yString = measurements.listToString(measurements.yData, "y");
 		String tString = measurements.listToString(measurements.tData, "t");
 		measurements.writeGraph("graphs.csv", xString, yString, tString);
 		
 		//TRIPLE SMOOTH
 //		ArrayList<Float> xSmooth = measurements.smooth(measurements.xData);
 //		ArrayList<Float> xSoSmooth = measurements.smooth(xSmooth);
 //		ArrayList<Float> xSoSoSmooth = measurements.smooth(xSoSmooth);
 //		String xSmoothString = measurements.listToString(xSmooth, "xS");
 //		measurements.writeGraph("x_smooth.csv", xString, xSmoothString, tString);
 		//end saving data
 		
 		double d;
 		d = 0;
 		if (!sPrefs.getBoolean("MeasureY",false))
 		{
 			physics.RemoveGravity(	measurements.xData );
 			 d = physics.Distance(	measurements.xData,
 					 				measurements.tData);
 		}
 		else if(!sPrefs.getBoolean("MeasureZ", false))
 		{
 			physics.RemoveGravity(	measurements.xData,
 									measurements.yData);
 			 d = physics.Distance(	measurements.xData,
 					 				measurements.yData,
 					 				measurements.tData);
 		}
 		else
 		{
 			physics.RemoveGravity(	measurements.xData,
 									measurements.yData,
 									measurements.zData);
 			 d = physics.Distance(	measurements.xData,
 					 				measurements.yData,
 					 				measurements.zData,
 					 				measurements.tData);
 		}
 		
 		//d.toString(), then truncate to two decimal places
 		String truncate;
 		if(d == -1.0) truncate = "-1.0"; 
 		if(d == 0) truncate = "0.0";
 		else
 		{
 			String d_str = Double.valueOf(d).toString(); 
 			truncate = d_str.substring(0, d_str.indexOf('.') + 3);
 		}
 		pi_string = truncate;
 		
 		//get shared setting for measurement units
 		SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 		String y = sPrefs.getString("meas_units", "0");
 		int UNITS = Integer.valueOf(y);
 		System.out.println("UNITS INT: " + UNITS);
 		if (UNITS == 0)
 		{
 			//convert to feet
 			System.out.println("pi_string: " + pi_string);
 			double x = Double.parseDouble(pi_string) * 3.28084;
 			
 			//round value
 			double range = 0.04;
 			int factor = (int) Math.round(x/range);
 			double result = factor * range;
 			
 			System.out.println("double pi_string: " + result);
 			pi_string = Double.toString(result) + " ft";
 		}
 		else
 		{
 			pi_string = pi_string + " m";
 		}
 		
 		handler.sendEmptyMessage(0);
 		//pd.dismiss();
 		System.out.println(truncate);
 		System.out.println("returning from Measure()");
 		}
 	
 	//returns nothing, but results in "measurements" containing measured accels and angles
 	public void Collect(){
 		
 		System.out.println("Calling Collect()");
 		
 		measurements = new TailLinkedList();
 		firstAzimuth = -1f;
 		lastAzimuth = -1f;
 		gate = new CountDownLatch(1);
 		
 		boolean worked = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);	
 		boolean worked2 = mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_FASTEST);
 		
 		System.out.println("Return from registerlistener: " + worked + " and " + worked2 );
 		List<Sensor> l = mSensorManager.getSensorList(Sensor.TYPE_ALL);
 		for(Sensor s : l)
 			System.out.println(s.getName());
 		
 		try {
 			gate.await();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		//stop measuring
 		mSensorManager.unregisterListener(this, mAccelerometer);
 		mSensorManager.unregisterListener(this, mOrientation);
 		
 		System.out.println("returning from Collect()");
 		}
 	
 	
 	// manages user touching the screen
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         System.out.println("onTouchEvent fired");
     	
         if (activeThread && event.getAction() == MotionEvent.ACTION_DOWN) {
             // we set the activeThread boolean to false,
             // forcing the loop from the Thread to end
             activeThread = false;
             if(gate!=null)
             	gate.countDown(); 	//causes the thread's "run" method to continue.
             						//"opens the gate"
         }
         
         return super.onTouchEvent(event);
     }
     
 	// manages user touching the screen
     public boolean stopMeasuring(MotionEvent event) {
         
         if (activeThread && event.getAction() == MotionEvent.ACTION_DOWN) {
             // we set the activeThread boolean to false,
             // forcing the loop from the Thread to end
             activeThread = false;
             gate.countDown(); //causes the thread's "run" method to contine.
             					//"opens the gate"
         }
         
         return super.onTouchEvent(event);
     }
 	
 	//Receive thread messages, interpret them and act as needed
 	@SuppressLint("HandlerLeak")
 	private static Handler handler = new Handler(){
 		@Override
 		public void handleMessage(Message mg){
 			//pd.dismiss();
 			tv.setText(pi_string);
 		}
 	};
 	
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		
 		switch(event.sensor.getType())
 		{
 		case Sensor.TYPE_ACCELEROMETER:
 			if(lastAzimuth == -1f)
 				break;
 			System.out.println("Measure: Accel Sensor Changed");
 			float x=0;
 			float y=0;
 			float z=0;
 			long  t=0;
 			
 			t = event.timestamp;
 			x = event.values[0]; 
 			if (sPrefs.getBoolean("MeasureY", false)) y = event.values[1];
 			if (sPrefs.getBoolean("MeasureZ", false)) z = event.values[2];
 			
 			if(x > greatestX) greatestX = x;
 			if(y > greatestY) greatestY = y;
 			if(z > greatestZ) greatestZ = z;
 			
 			
 			pi_string = "collecting";
 			handler.sendEmptyMessage(0);
 			
 			if (!sPrefs.getBoolean("MeasureY", false)) measurements.add(t, lastAzimuth, x);
 				else if (!sPrefs.getBoolean("MeasureZ", false)) measurements.add(t, lastAzimuth, x, y);
 				else measurements.add(t, lastAzimuth, x, y, z);
 			break;
 		case Sensor.TYPE_ORIENTATION:
 			if(firstAzimuth == -1f) 
 				firstAzimuth = event.values[0] / 360f * (float)(2 * Math.PI);
 			lastAzimuth = event.values[0]/ 360f * (float)(2 * Math.PI) - firstAzimuth;
 			break;
 		
 		}
 			
 	}
 	
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 		//System.out.println("onAccuracyChanged fired");
 		
 	}
 
 }
