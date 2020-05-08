 package com.example.lufteapp;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.PictureDrawable;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.caverock.androidsvg.SVG;
 import com.caverock.androidsvg.SVGParseException;
 
 public class MainActivity extends Activity {
 	
 	SensorManager mySensorManager;
     Sensor myProximitySensor;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 									//finds the right view for .svg element to populated
 		ImageView  imageView = (ImageView) findViewById(R.id.pictureMain);
 	    imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
 	    try {	  
 	    							//fetching file from drawable resource, rendering, setting and populating
 	    	SVG svg = SVG.getFromResource(this, R.drawable.applogo);
 	    	Drawable drawable = new PictureDrawable(svg.renderToPicture());
 	    	imageView.setImageDrawable(drawable);
 	    }
 	    catch(SVGParseException e){
 	    	
 	    }
 		
 	    mySensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
 	    myProximitySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
 	    
 	    if (myProximitySensor == null){
 	    	Toast.makeText(getApplicationContext(), 
 				     "No proximity sensor!",
 				     Toast.LENGTH_LONG).show();
 	        } else {
 	        	//ProximitySensor.setText(myProximitySensor.getName());
 	        	//ProximityMax.setText("Maximum Range: "
 	        	//  + String.valueOf(myProximitySensor.getMaximumRange()));
 	        	mySensorManager.registerListener(proximitySensorEventListener,
 	        		 myProximitySensor,
 	        		 SensorManager.SENSOR_DELAY_NORMAL);
 	        }		
 	}
 	
 	SensorEventListener proximitySensorEventListener = new SensorEventListener(){
 
 		@Override
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {
 			// TODO Auto-generated method stub
 		}
 
 		@Override
 		public void onSensorChanged(SensorEvent event) {
 
 			if(event.sensor.getType()==Sensor.TYPE_PROXIMITY){
 				
 				if(event.values[0] == 0) {
 				
 					Intent startMain = new Intent(Intent.ACTION_MAIN);
 				
 					startMain.addCategory(Intent.CATEGORY_HOME);
 		        	startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		        	startActivity(startMain);
 				}
				//ProximityReading.setText("Proximity Sensor Reading:"
				//+ String.valueOf(event.values[0]));
 			}
 		}
     };
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	public void openMap(View view) {
 		Intent intent = new Intent(this, ViewMap.class);
 		startActivity(intent);
 	}
 	
 	public void openLog(View view) {
 		Intent intent = new Intent(this, ActivityLog.class);
 		startActivity(intent);
 	}
 	
 	public void checkIn(View view) {
 		Intent intent = new Intent(this, GpsData.class);
 		startActivity(intent);
 	}
 	
 	public void accelerometer(View view) {
 		Intent intent = new Intent(this, accelerometer.class);
 		startActivity(intent);
 	}
 }
