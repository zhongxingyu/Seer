 /**
  * 
  */
 package com.haubey.tangent;
 
 import android.app.Activity;
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.widget.TextView;
 
 /**
  * @author descartes.holland
  *
  */
 public class AccelerometerTest extends Activity implements SensorEventListener
 {
 	
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
 	//Sensors.enableSensor(Sensors.SENSOR_ACCELEROMETER);
 	TextView textView;
 	StringBuilder builder = new StringBuilder();
 	
 	float[] locations = new float[2];
 	String[] directions = {"NA", "NA"};
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 //        setContentView(R.layout.activity_main);
 
 		super.onCreate(savedInstanceState);
 		textView = new TextView(this);
 		setContentView(textView);
 		
 		SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
 		manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
 	}
 	
 	@Override
 	public void onSensorChanged(SensorEvent event)
 	{
		//Get change in location:
		float deltaX = locations[0] - event.values[0];
		float deltaY = locations[1] - event.values[1];
 		
 		//Change initial location to current location:
 		locations[0] = event.values[0];
 		locations[1] = event.values[1];
 		
 		//Update Direction Strings
		if (deltaX > 2)
 			directions[0] = "LEFT";
		else if (deltaX < -2)
 			directions[0] = "RIGHT";
 		
		if (deltaY > 2)
 			directions[1] = "DOWN";
		else if (deltaY < -2)
 			directions[1] = "UP";
 		
 		builder.setLength(0);
         builder.append("X: ");
         builder.append(directions[0]);
         builder.append(" Y: ");
         builder.append(directions[1]);
 
         textView.setText(builder.toString());
 	}
 	
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1)
 	{
 		//Nothing
 	}
 }
