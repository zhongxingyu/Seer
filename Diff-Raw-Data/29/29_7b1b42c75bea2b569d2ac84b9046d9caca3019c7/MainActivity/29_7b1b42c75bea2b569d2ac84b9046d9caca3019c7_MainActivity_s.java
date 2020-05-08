 package com.nomath4u.values;
 
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.Toast;
 import android.widget.CompoundButton.OnCheckedChangeListener;
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
 	private LinearLayout bigLayout;
 	private ArrayList<TextView> mTextViews = new ArrayList<TextView>();
 	private final boolean ON = true;
 	private final boolean OFF = false;
 	private Float eventValone;
 	private Float eventValtwo;
 	private Float eventValthree;
 	
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         adView = new AdView(this, AdSize.BANNER, "a150f4a1207af4e");
         linearLayout = (LinearLayout)findViewById(R.id.stuffz);
         bigLayout = (LinearLayout)findViewById(R.id.stuffzbig);
         
         
         AdRequest adRequest = new AdRequest();
         //White-list the emulator and the test device to receive test ads.
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
         
         
         //Get checkbox preference
         SharedPreferences settings = getSharedPreferences("MyPrefsFile",0);
         boolean checked = settings.getBoolean("dialogPop", true);
       
         
         if(/*started &&*/ checked){
         	View checkBoxView = View.inflate(this, R.layout.checkbox, null);
         	final CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
         	checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
         	    @Override
         	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 
         	        // Save to shared preferences
         	    }
         	});
         
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setView(checkBoxView);
         builder.setCancelable(true);
         builder.setInverseBackgroundForced(true);
         builder.setMessage(R.string.warning_pop);
         builder.setPositiveButton("OK",
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog,
                             int which) {
                     	//started = false;
                     	checkCheckBox(checkBox);
                         dialog.dismiss();
                     }
                 });
         AlertDialog alert = builder.create();
         alert.show();
         }
         //started = false;
         
         
     }
     
     @Override
     protected void onResume(){
     	super.onResume();
     	manageListeners(ON);
     }
     
     @Override
     protected void onRestart(){
     	super.onRestart();
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
             case R.id.intro_button:
                 //showHelp();
                 return true;
             case R.id.contact_button:
 			Intent intent = null;
 			try {
 				intent = Intent.parseUri("mailto:harperc@onid.orst.edu", Intent.URI_INTENT_SCHEME);
 			} catch (URISyntaxException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 				intent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.subject);
             	startActivity(intent);
             	return true;
             case R.id.reset_button:
             	SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
             	SharedPreferences.Editor editor = settings.edit();
             	editor.putBoolean("dialogPop", true);
             	editor.commit();
             	Toast toast = Toast.makeText(getApplicationContext(), "Warning will be displayed on next run", Toast.LENGTH_SHORT);
             	toast.show();
             	return true;
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
     					// attempt 1
     					eventValone = event.values[0];
     					eventValtwo = event.values[1];
     					eventValthree = event.values[2];
     					 
     					 tView.setText(thisSensor.getName() + ": \n" +
     							eventValone.toString() + "\n" +
     							eventValtwo.toString() + "\n" +
     							eventValthree.toString() +"\n\n" 
     							);
     					 tView.setTextColor(getResources().getColor(R.color.indiglo));
     					
     				}
     				@Override
     				public void onAccuracyChanged(Sensor sensor, int accuracy){
     				
     				}
     			};
     			
     			mSensorManager.registerListener(tmp,sensor, 1000000000);	
     			mListOfSensorListeners.add(tmp);
     			mTextViews.add(tView);
     			//}
     		}
     	}
    
     else{
     	for(SensorEventListener listener : mListOfSensorListeners){
     		mSensorManager.unregisterListener(listener);
     	}
     }
     }
     void disableListeners(){
     		for (SensorEventListener listener : mListOfSensorListeners){
     			mSensorManager.unregisterListener(listener);
     		}
     		for (TextView tView: mTextViews){
     			linearLayout.removeView(tView);
     		}
     }
     	/*if	(register && !started){
     		for (Sensor sensor : deviceSensors){
     				SensorEventListener tmp = new SensorEventListener(){
         				@Override
         				public void onSensorChanged(SensorEvent event){
         					// attempt 1
         					eventValone = event.values[0];
         					eventValtwo = event.values[1];
         					eventValthree = event.values[2];
         					
         				}
         				@Override
         				public void onAccuracyChanged(Sensor sensor, int accuracy){
         				
         				}
     				};
     		}	
     				mSensorManager.registerListener(tmp, sensor, 1000000000);
     			
     	}*/	
     
     void registerViews(){
     	for(TextView view : mTextViews){
     		linearLayout.addView(view);
     	}
    	bigLayout.addView(adView);
     }
     
     void showAbout(){
     	AlertDialog builder = new AlertDialog.Builder(this).create();
     	builder.setTitle("About");
     	builder.setMessage("Sensor Values \n\nCreated by: Christopher Harper \n\nArtist: Pamela Kent \n\n2013");
     	builder.setCancelable(true);
     	builder.setInverseBackgroundForced(true);
     	builder.show();
     }
     
     void checkCheckBox(CheckBox checkbox){
     	if(checkbox.isChecked()){
     		SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
     		SharedPreferences.Editor editor = settings.edit();
     		editor.putBoolean("dialogPop", false);
     		editor.commit();
     	}
     	
     }
 }
 
 
