 package mobile.team.reservations;
 
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 public class MagicEightBallActivity extends Activity implements SensorEventListener {
 	private SensorManager sensorMgr;
 	private String weightPref;
 	private boolean isShaken = false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
 		sensorMgr.registerListener(this, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(Menu.NONE, 0, Menu.NONE, "Edit Preferences").setIcon(android.R.drawable.ic_menu_info_details).setAlphabeticShortcut('e');
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case 0:
 				startActivity(new Intent(this, EditPreferencesActivity.class));
 				return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		weightPref = prefs.getString("weightName", "normal");
 		sensorMgr.registerListener(this, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		sensorMgr.unregisterListener(this);
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
 			panicDetection(event);
 		}
 	}
 
 	private void panicDetection(SensorEvent event) {
 		float[] accelerometerVals = event.values;
 
 		float x = accelerometerVals[0];
 		float y = accelerometerVals[1];
 		float z = accelerometerVals[2];
 
 		float acceleration = (x*x + y*y + z*z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
 		if (acceleration >= 2) {
 			if (isShaken) {
 				TextView messageTV = (TextView) findViewById(R.id.responseTextView);
 				TextView helpTV = (TextView) findViewById(R.id.helpTextView);
 				String[] responses = getResources().getStringArray(R.array.answers);
 				Random generator = new Random();
 				int key;
 				if (weightPref.equals("normal")) {
 					key = generator.nextInt(responses.length);
				} else if (weightPref.equals("good")){
					long range = (long)8 - (long)0 + 1;
 					long fraction = (long)(range * generator.nextDouble());
 					key = (int)fraction;
 				} else {
					long range = (long)18 - (long)14 + 1;
					long fraction = (long)(range * generator.nextDouble());
 					key = (int)fraction;
				}
				
 				messageTV.setText(responses[key]);
 				helpTV.setText(R.string.help);
 				isShaken = false;
 			} else {
 				TextView messageTV = (TextView) findViewById(R.id.responseTextView);
 				TextView helpTV = (TextView) findViewById(R.id.helpTextView);
 				
 				helpTV.setText("");
 				messageTV.setText(R.string.defaultTxt);
 				isShaken = true;
 			}
 		}
 	}
 }
 
 
 
 
