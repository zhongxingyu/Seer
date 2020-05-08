 package com.nomath4u.values;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 
 import com.google.ads.*;
 
 public class MainActivity extends Activity {
 	
 	private AdView adView;
 	private SensorManager mSensorManager;
 	private List<Sensor> deviceSensors;
 	private ArrayList<SensorEventListener> mListOfSensorListeners = new ArrayList<SensorEventListener>();
 	private LinearLayout linearLayout;
 	private ArrayList<TextView> mTextViews = new ArrayList<TextView>();
 	private final boolean ON = true;
 	private final boolean OFF = false;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         adView = new AdView(this, AdSize.BANNER, "a150f4a1207af4e");
         linearLayout = (LinearLayout)findViewById(R.id.stuffz);
         
         
         AdRequest adRequest = new AdRequest();
         // White-list the emulator and the test device to receive test ads.
         adRequest.addTestDevice(AdRequest.TEST_EMULATOR); // Emulator
         adRequest.addTestDevice("8BF2B0C6E393BA041BC521468BC8A5F8");        // Test Android Device
  	
         // Initiate a generic request to load it with an ad.
         adView.loadAd(adRequest);
        
         	 
         
         // Get an instance of the SensorManager
         mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
       
         
         //Get all Sensors
         deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
         
         
         manageListeners(ON);
         registerViews();
         
         
     }
     
     @Override
     protected void onResume(){
     	super.onResume();
     	manageListeners(ON);
     }
     
     @Override
     protected void onPause(){
     	super.onPause();
     	manageListeners(OFF);
     }
     
     @Override
     protected void onDestroy(){
     	super.onDestroy();
     	manageListeners(OFF);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.menu_about:
                 showAbout();
                 return true;
             /*case R.id.help:
                 showHelp();
                 return true;*/
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     private void manageListeners(boolean register){
     	//Fill List with Listeners
     	if(register){
     		for ( Sensor sensor : deviceSensors){
     			final TextView tView = new TextView(this);
     			tView.setLayoutParams(new ViewGroup.LayoutParams(
     					ViewGroup.LayoutParams.MATCH_PARENT,
     					ViewGroup.LayoutParams.WRAP_CONTENT));
     			final Sensor thisSensor = sensor;
     			SensorEventListener tmp = new SensorEventListener(){
     				@Override
     				public void onSensorChanged(SensorEvent event){
     					float[] values = event.values;
     					tView.setText(thisSensor.getName() + ": \n");
     					for (float number : values){
     						//Add each value to the text view
     						tView.setText(tView.getText() + String.format("%.5g%n", number) + " ");
     					}
     				}
     				@Override
     				public void onAccuracyChanged(Sensor sensor, int accuracy){
     				
     				}
     			};
     			mSensorManager.registerListener(tmp,sensor,SensorManager.SENSOR_DELAY_UI);	
     			mListOfSensorListeners.add(tmp);
     			mTextViews.add(tView);
     	
     		}
     	}
     	else{
     		for (SensorEventListener listener : mListOfSensorListeners){
     			mSensorManager.unregisterListener(listener);
     			
     		}
     	}
     }
     
     void registerViews(){
     	for(TextView view : mTextViews){
     		linearLayout.addView(view);
     	}
    	linearLayout.addView(adView);
     }
     
     void showAbout(){
     	AlertDialog builder = new AlertDialog.Builder(this).create();
     	builder.setTitle("About");
     	builder.setMessage("Sensor Values \n Created by: Christopher Harper \n 2013");
     	builder.setCancelable(true);
     	builder.setInverseBackgroundForced(true);
     	builder.show();
     }
 }
 
 
