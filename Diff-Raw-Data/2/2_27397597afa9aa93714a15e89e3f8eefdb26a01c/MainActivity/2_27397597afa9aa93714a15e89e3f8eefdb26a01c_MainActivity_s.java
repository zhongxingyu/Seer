 package com.pps.sleepcalc;
 
 import java.util.Calendar;
 
 import com.pps.sleepcalc.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.Switch;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements TimePicker.OnTimeChangedListener, SeekBar.OnSeekBarChangeListener{
 	
 	private Intent sensorService;
 
 	private int wakeupHours;
 	private int wakeupMinutes;
 	
 	private static final String SHARED_PREF_NAME = "SleepClock Prefs";
 
 	private EditText triggerDelay;
 	private EditText gyroSensorTrigger;
 	private EditText kalmanRnoise;
 	private EditText kalmanQnoise;
 	
 
 	private SeekBar wakeupDeltaBar;
 	private TextView wakeupDelta;
 	private Switch sensorPrecisionSwitch;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
         | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
         
         
         //setContentView(R.layout.activity_main);
         //set up view
         setContentView(R.layout.test);
         
         //add gui elements
         final TimePicker timepick = (TimePicker) findViewById(R.id.timePicker);
         timepick.setOnTimeChangedListener(this);
         
         wakeupDelta = (TextView) findViewById(R.id.wakeupDelta);
         wakeupDeltaBar = (SeekBar) findViewById(R.id.wakupDeltaBar);
         
         triggerDelay = (EditText) findViewById(R.id.triggerDelay);
         gyroSensorTrigger = (EditText) findViewById(R.id.gyroSensorTrigger);
         kalmanRnoise = (EditText) findViewById(R.id.kalmanRnoise);
         kalmanQnoise = (EditText) findViewById(R.id.kalmanQnoise);
 
         
         sensorPrecisionSwitch = (Switch) findViewById(R.id.sensorPrecisionSwitch);
         
         
         //load default values for each UI element in settings
 		SharedPreferences settings = getSharedPreferences(SHARED_PREF_NAME,0);
 		triggerDelay.setText(settings.getString("triggerDelay", "1000"));
		gyroSensorTrigger.setText(settings.getString("gyroSensorTrigger", "0.005"));
 		kalmanRnoise.setText(settings.getString("kalmanRnoise", "80.0"));
 		kalmanQnoise.setText(settings.getString("kalmanQnoise", "0.0125"));
 
 		sensorPrecisionSwitch.setChecked(settings.getBoolean("sensorPrecisionSwitch", false));
 		
         
         //save settings button
         Button saveSettings = (Button) findViewById(R.id.saveSettings);
         saveSettings.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				//Log.e("SleepCalcTag", Long.toString(wakeupDeltaBar.getProgress()));
 				
 				//save all settings to SHARED_PREF_NAME
 				SharedPreferences settings = getSharedPreferences(SHARED_PREF_NAME,0);
 				SharedPreferences.Editor editor = settings.edit();
 				editor.putString("triggerDelay", triggerDelay.getText().toString());
 				editor.putString("gyroSensorTrigger", gyroSensorTrigger.getText().toString());
 				editor.putString("kalmanRnoise", kalmanRnoise.getText().toString());
 				editor.putString("kalmanQnoise", kalmanQnoise.getText().toString());
 				editor.putBoolean("sensorPrecisionSwitch", sensorPrecisionSwitch.isChecked());
 				editor.commit();
 			}
 		});
         
         
         //add tabbing
         TabHost tabHost = (TabHost) findViewById(R.id.TabHost);
         tabHost.setup();
         
         TabSpec specMain = tabHost.newTabSpec("Main");
         specMain.setContent(R.id.tabMain);
         specMain.setIndicator("Start");
         
         TabSpec specSettings = tabHost.newTabSpec("Settings");
         specSettings.setContent(R.id.tabSettings);
         specSettings.setIndicator("Settings");
         
         tabHost.addTab(specMain);
         tabHost.addTab(specSettings);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         //getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     public void startClicked(View view){
     	//create and start service
     	sensorService = new Intent(this, sensorService.class);
     	sensorService.putExtra("wakeupHours", wakeupHours);
     	sensorService.putExtra("wakeupMinutes", wakeupMinutes);
     	
     	//extras from settings
     	sensorService.putExtra("triggerDelay", Integer.valueOf(triggerDelay.getText().toString()));
     	sensorService.putExtra("gyroSensorTrigger", Float.valueOf(gyroSensorTrigger.getText().toString()));
     	sensorService.putExtra("kalmanRnoise", Float.valueOf(kalmanRnoise.getText().toString()));
     	sensorService.putExtra("kalmanQnoise", Float.valueOf(kalmanQnoise.getText().toString()));
     	
     	sensorService.putExtra("sensorPrecisionSwitch", Boolean.valueOf(sensorPrecisionSwitch.isChecked()));
 
     
     	startService(sensorService);
     	
     	//Toast and log output for user/developper notification
     	Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
     	Log.e("SleepCalcTag", "start clicked");
     }
     
     public void stopClicked(View view){
     	//stop service
     	stopService(sensorService);
     	
     	//Toast and log output for user/developper notification
     	Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
     	Log.e("SleepCalcTag", "stop clicked");
     }
     
     public void resetClicked(View view){
     	//reset fields
     	triggerDelay.setText("1000");
     	gyroSensorTrigger.setText("0.03");
     	sensorPrecisionSwitch.setChecked(false);
     	
     	//update config
 		SharedPreferences settings = getSharedPreferences(SHARED_PREF_NAME,0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString("triggerDelay", triggerDelay.getText().toString());
 		editor.putString("gyroSensorTrigger", gyroSensorTrigger.getText().toString());
 		editor.putString("kalmanRnoise", kalmanRnoise.getText().toString());
 		editor.putString("kalmanQnoise", kalmanQnoise.getText().toString());
 		editor.putBoolean("sensorPrecisionSwitch", sensorPrecisionSwitch.isChecked());
 		editor.commit();  	
     	Log.e("SleepCalcTag", "reset clicked");
     }
 
 
 	@Override
 	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
 		Log.e("SleepCalcTag", "Hour: "+hourOfDay+" minute: "+minute);
 		wakeupHours=hourOfDay;
 		wakeupMinutes=minute;
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void onProgressChanged(SeekBar seekBar, int progress,
 			boolean fromUser) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void onStopTrackingTouch(SeekBar seekBar) {
 		// TODO Auto-generated method stub
 		
 	}
 
     
 }
