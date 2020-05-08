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
 import android.widget.EditText;
 import android.widget.CheckBox;
  
 public class CreateAlarmActivity extends Activity{
 	
 	private AlarmsDataSource datasource;
 		
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.setContentView(R.layout.new_alarm_activity);
         
         datasource = new AlarmsDataSource(this);
         datasource.open();          
     }
     
     public void onClick(View view) {
     	TimePicker time_picker = (TimePicker) findViewById(R.id.time_picker);
     	EditText edit_text = (EditText) findViewById(R.id.edit_text);
     	String alarm_text = edit_text.getText().toString();
     	CheckBox check_box = (CheckBox) findViewById(R.id.check_box);
     	int check_box_active = 0;
     	if (check_box.isChecked()){
     		check_box_active = 1;
     	}
     	//Alarm alarm = null;
         //Log.w("onClick: ", "save_alarm button before clicked");
         switch (view.getId()) {
 	        case R.id.save_alarm:
 	    	//Log.w("onClick: ", "save_alarm button clicked");
 	  	  	// Save the new alarm to the database
 	  	  	int AM = 1;
 	  	  	int hour = time_picker.getCurrentHour();
 	        if (hour > 12) {
 	  	  		AM = 0;
 	  	  		hour = hour -12;
 	  	  	}
 	  	    Alarm alarm = new Alarm();
 	  	    alarm = datasource.createAlarm(alarm_text, hour, time_picker.getCurrentMinute(), AM, check_box_active);
 	  	    Toast.makeText(getApplicationContext(), "Alarm saved!", Toast.LENGTH_LONG).show();
	  	    Log.w("createAlarm", ""+alarm.getId());
         }
         finish();
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
