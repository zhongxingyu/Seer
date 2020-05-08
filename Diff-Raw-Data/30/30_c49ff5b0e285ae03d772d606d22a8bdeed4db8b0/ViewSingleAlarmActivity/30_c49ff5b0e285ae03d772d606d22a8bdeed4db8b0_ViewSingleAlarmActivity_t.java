 package me.ingeniando.conditionalalarm;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.TextView;
  
 public class ViewSingleAlarmActivity extends Activity{
 	
 	private AlarmsDataSource datasource;
 		
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.setContentView(R.layout.single_alarm_activity);
         
         datasource = new AlarmsDataSource(this);
         datasource.open();
          
         TextView txtAlarm = (TextView) findViewById(R.id.alarm_id);
          
         Intent i = getIntent();
         // getting attached intent data
         String alarmId = i.getStringExtra("alarmId");
         Alarm selectedAlarm = datasource.getAlarmById(alarmId);
        // displaying selected alarm
         txtAlarm.setText(selectedAlarm.toString());
        //txtAlarm.setText("alarmId");
          
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
