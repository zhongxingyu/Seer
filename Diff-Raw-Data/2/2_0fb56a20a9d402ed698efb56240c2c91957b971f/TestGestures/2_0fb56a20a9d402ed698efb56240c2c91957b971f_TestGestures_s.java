 package com.example.testgestureapp;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.RemoteException;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.TextView;
 
 public class TestGestures extends Activity implements SensorEventListener {
 	
 	Button start, stop;
 	TextView results;
 	GridView resultsList;
 	
 	SensorManager sensorManager;
 	Sensor accelerometerSensor;
 	private ArrayList<Acceleration> accelerationList;
 	Acceleration acceletationObject;
 	private float accelX, accelY, accelZ;
 	private float oldAccelX, oldAccelY, oldAccelZ = 0;
 	private static CountDownTimer timer;
 	int foundSame = 0;
 	double minDistance = -1;
 	boolean buttonPressed = false;
 	
 	private GestureDataBase gestureDataBase;
     static ArrayList<String> listItems = new ArrayList<String>();
     static ArrayAdapter<String> adapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.test_gestures);
 		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
 		
 		start = (Button) findViewById(R.id.start);
 		start = (Button) findViewById(R.id.stop);
 		results = (TextView) findViewById(R.id.resultsView);
 		resultsList = (GridView) findViewById(R.id.resultsList);
 		
 		resultsList.setAdapter(adapter);
 		
 		//setting up database for acceleration recording
 		gestureDataBase = new GestureDataBase(this);
 	    gestureDataBase.openReadable();
 	    
 	    //Array list to hold sensor data
 	    accelerationList = new ArrayList<Acceleration>();
 
 		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 	    accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
 	    
	    timer = new CountDownTimer(10000, 50) {
 	        public void onTick(long millisUntilFinished) {
         		if(accelX != oldAccelX && accelY != oldAccelY && accelZ != oldAccelZ) {
         			acceletationObject =  new Acceleration(accelX, accelY, accelZ);
 		    		accelerationList.add(acceletationObject);
         		}
         		else
         			foundSame++;
     			oldAccelX = accelX;
         		oldAccelY = accelY;
         		oldAccelZ = accelZ;
     		}
 
 	        public void onFinish() {
 	            processData();
 	            foundSame = 0;
 	            oldAccelX = 0;
         		oldAccelY = 0;
         		oldAccelZ = 0;
         		// Clear the old readings from the list
         		accelerationList.clear();
 	        }
 	     };
 	}
 	
 	protected void processData() {
 		//Process only when there is new data recorded to test
 		if(!accelerationList.isEmpty()) {
 			//Get the list of all the gestures stored in Database
 			ArrayList<Gesture_Object> savedGestures = gestureDataBase.getAllGestures();
 			
 			if(savedGestures.size() > 0)
 				results.setText("Results");
 			else
 				results.setText("No saved gestures in database, record gestures first!");
 			
 			//Create the gesture object for newly recorded gesture
 			String name = "Test Gesture";
 			Gesture_Object testGesture = createGesureObject(name, accelerationList);
 			
 			//Compare the newly gesture object with all saved gestures
 			for (int i = 0; i < savedGestures.size(); i++)
 			{
 				minDistance = (double) DynamicTimeWarping.calcDistance(savedGestures.get(i), testGesture);
 				addItem(savedGestures.get(i).getName());
 				addItem("" + minDistance);
 			}
 		}
 		else
 			results.setText("Nothing recorded, press Start to record data.");
 	}
 
 	protected void onResume() {
         super.onResume();
         sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
         gestureDataBase.openReadable();
     }
 
     protected void onPause() {
         super.onPause();
         sensorManager.unregisterListener(this);
         gestureDataBase.close();
     }
 	
 	public void onClickStart(View v) {
 		// Start Recording Data & send for Testing
 		results.setText("Testing...");
 		clearItems();
 		timer.start();
 		
 		try {
 			MainActivity.recognitionService.startClassificationMode(MainActivity.GESTURES_RECORDED);
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void onClickStop(View v) {
 		// Stop Testing and add results to the resultsList
 		if (timer != null) {
 			timer.cancel();
 			timer.onFinish();
 		}
 		
 		try {
 			MainActivity.recognitionService.stopClassificationMode();
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public static void addItem(String value) {
 		listItems.add(value);
 		adapter.notifyDataSetChanged();
 	}
 	
 	public void clearItems() {
 		listItems.clear();
 		adapter.notifyDataSetChanged();
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		// TODO Auto-generated method stub
 		if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
 			accelX = event.values[0];
 			accelY = event.values[1];
 			accelZ = event.values[2];
 		}
 	}
 	
 	public Gesture_Object createGesureObject(String name, ArrayList<Acceleration> accelerationList) {
 		int size =  accelerationList.size();
 		Acceleration [] accelerationArray = new Acceleration[size] ;
 		for (int i = 0; i < size; i++) {
 			accelerationArray[i] = accelerationList.get(i);
 		}
 		 
 		Gesture_Object gestureObject =  new  Gesture_Object(name, accelerationArray);
 		return gestureObject;
 	}
 	
 	
 	public boolean dispatchKeyEvent(KeyEvent event) {
 	    int action = event.getAction();
 	    int keyCode = event.getKeyCode();
         switch (keyCode) {
         case KeyEvent.KEYCODE_VOLUME_UP:
             if (action == KeyEvent.ACTION_DOWN) {
             	if(!buttonPressed) {
             		buttonPressed = true;
             		results.setText("Testing...");
             		clearItems();
             		timer.start();
             		try {
             			MainActivity.recognitionService.startClassificationMode(MainActivity.GESTURES_RECORDED);
             		} catch (RemoteException e) {
             			// TODO Auto-generated catch block
             			e.printStackTrace();
             		}
             	}
             }
             if (action == KeyEvent.ACTION_UP) {
             	if (timer != null) {
         			timer.cancel();
         			timer.onFinish();
         		}
             	try {
         			MainActivity.recognitionService.stopClassificationMode();
         		} catch (RemoteException e) {
         			// TODO Auto-generated catch block
         			e.printStackTrace();
         		}
 				buttonPressed = false;
             }
             return true;
         case KeyEvent.KEYCODE_VOLUME_DOWN:
             if (action == KeyEvent.ACTION_DOWN) {
             	if(!buttonPressed) {
             		buttonPressed = true;
             		results.setText("Testing...");
             		clearItems();
             		timer.start();
             		try {
             			MainActivity.recognitionService.startClassificationMode(MainActivity.GESTURES_RECORDED);
             		} catch (RemoteException e) {
             			// TODO Auto-generated catch block
             			e.printStackTrace();
             		}
             	}
             }
             if (action == KeyEvent.ACTION_UP) {
             	if (timer != null) {
         			timer.cancel();
         			timer.onFinish();
         		}
             	try {
         			MainActivity.recognitionService.stopClassificationMode();
         		} catch (RemoteException e) {
         			// TODO Auto-generated catch block
         			e.printStackTrace();
         		}
 				buttonPressed = false;
             }
             return true;
         default:
             return super.dispatchKeyEvent(event);
         }
 	}
 }
