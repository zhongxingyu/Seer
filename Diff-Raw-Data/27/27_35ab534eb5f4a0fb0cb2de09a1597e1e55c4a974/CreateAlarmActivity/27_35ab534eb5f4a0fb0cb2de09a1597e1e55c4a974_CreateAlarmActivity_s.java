 package me.ingeniando.conditionalalarm;
 
 import android.util.Log;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Button;
 import android.widget.Toast;
  
 public class CreateAlarmActivity extends Activity{
 	
	final TimePicker time_picker = (TimePicker) findViewById(R.id.time_picker);
 	private AlarmsDataSource datasource;
 		
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.setContentView(R.layout.new_alarm_activity);
         
         datasource = new AlarmsDataSource(this);
         datasource.open();         
         //Intent i = getIntent();
         // getting attached intent data
         //String alarmId = i.getStringExtra("alarmId");
         //Alarm selectedAlarm = datasource.getAlarmById(alarmId);
         // displaying selected alarm
         //txtAlarm.setText(selectedAlarm.toString());
         //txtAlarm.setText("alarmId"); 
     }
     
     // Will be called via the onClick attribute
     // of the buttons in main.xml
     public void onClick(View view) {
        Alarm alarm = null;
         switch (view.getId()) {
         case R.id.save_alarm:
     	Log.w("onClick: ", "save_alarm button clicked");
    	String strTime = time_picker.getCurrentHour() + ":" + time_picker.getCurrentMinute();
   	  	Toast.makeText(getApplicationContext(), 
                "TIME: " + strTime + "", Toast.LENGTH_LONG).show();
     
         }
     }
     
     @Override
     protected void onResume() {
       datasource.open();
       super.onResume();
     }
 
     @Override
     protected void onPause() {
       datasource.close();
       super.onPause();
     }
 }
