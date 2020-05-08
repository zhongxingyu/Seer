 package no.hig.level;
 
 
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.view.Menu;
 import android.widget.TextView;
import android.widget.Toast;
 
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 
 
 public class MainActivity extends Activity implements SensorEventListener {
     private SensorManager mSensorManager;
     private Sensor mSensor;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
     	mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
     	mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
     	
    	
    	if(mSensor == null){
    		Toast.makeText(this, R.string.sensor_not_found , Toast.LENGTH_LONG).show();
    	}
    	
     }
 
 
     protected void onResume() {
         super.onResume();
        if(mSensor != null){
        	mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
        }
     }
 
     protected void onPause() {
         super.onPause();
        if(mSensor != null){
        	mSensorManager.unregisterListener(this);
        }
     }
 
     public void onAccuracyChanged(Sensor sensor, int accuracy) {
     }
 
     public void onSensorChanged(SensorEvent event) {
     	
     	TextView label = (TextView) findViewById(R.id.textView1);
     	label.setText( Float.toString(event.values[0]) );
     	
     	label = (TextView) findViewById(R.id.textView2);
     	label.setText( Float.toString(event.values[1]) );
     	
     	label = (TextView) findViewById(R.id.textView3);
     	label.setText( Float.toString(event.values[2]) );
     }
     
     
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 }
