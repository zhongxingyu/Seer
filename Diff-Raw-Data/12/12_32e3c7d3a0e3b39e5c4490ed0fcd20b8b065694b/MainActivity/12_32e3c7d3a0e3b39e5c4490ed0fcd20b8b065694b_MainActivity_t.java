 package me.ingeniando.conditionalalarm;
 
 import java.util.List;
 import java.util.Random;
 import java.util.ArrayList;
 
 import android.util.Log;
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.content.Intent;
 import android.widget.Button;
 
 public class MainActivity extends ListActivity {
   
 	private AlarmsDataSource datasource;
 	private ArrayAdapter<Alarm> alarmsAdapter;
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_main);
 
     datasource = new AlarmsDataSource(this);
     datasource.open();
 
     ArrayList<Alarm> alarmList = datasource.getAllAlarms();
 
     // Use the SimpleCursorAdapter to show the elements in a ListView
     alarmsAdapter = new ArrayAdapter<Alarm>(this,
         android.R.layout.simple_list_item_1, alarmList);
     //Log.w("alarmsAdapter: ", alarmsAdapter.toString());
     setListAdapter(alarmsAdapter);
     
     // TO TEST THE ALARMS!
     // Save the new alarm to the database
     //Alarm alarm = new Alarm();
     //alarm = datasource.createAlarm("Test text", 07, 15, 1, 1);
     //alarmsAdapter.add(alarm);
   }
   
   @Override
   protected void onListItemClick(ListView l, View v, int position, long id) {	
 	  super.onListItemClick(l, v, position, id);
 	  Alarm alarm = alarmsAdapter.getItem(position);
 	  long alarmId = alarm.getId();
 	  /*Log.w("onListemItemClick: ", alarm.toString());
 	  Toast.makeText(getApplicationContext(), 
               "Alarm Id: " + alarmId + "", Toast.LENGTH_LONG).show();*/
 	  Intent i = new Intent(getApplicationContext(), ViewSingleAlarmActivity.class);
       // sending data to new activity
 	  String alarmIdString = ""+alarmId;
       i.putExtra("alarmId", alarmIdString);
       startActivity(i);
 	}
   
   // Will be called via the onClick attribute
   // of the buttons in main.xml
   public void onClick(View view) {
     @SuppressWarnings("unchecked")
     ArrayAdapter<Alarm> adapter = (ArrayAdapter<Alarm>) getListAdapter();
     Alarm alarm = null;
     switch (view.getId()) {
     case R.id.add_alarm:
   	  Intent i = new Intent(getApplicationContext(), CreateAlarmActivity.class);
       // sending data to new activity
 	  //String alarmIdString = ""+alarmId;
       i.putExtra("alarmId", "TEST");
       startActivity(i);
       //String[] alarms = new String[] { "07:55", "08:30", "09:00" };
       //int nextInt = new Random().nextInt(3);
       // Save the new alarm to the database
       //alarm = datasource.createAlarm(alarms[nextInt]);
       //adapter.add(alarm);
       break;
     case R.id.delete_alarm:
       if (getListAdapter().getCount() > 0) {
         alarm = (Alarm) getListAdapter().getItem(0);
         datasource.deleteAlarm(alarm);
         adapter.remove(alarm);
       }
       break;
     }
   }
 
   @Override
   protected void onResume() {
     datasource.open();
     Alarm alarm = new Alarm();
    try {
    	alarm = datasource.getLastAlarmById();
    	alarmsAdapter.add(alarm);
    	alarmsAdapter.notifyDataSetChanged();
    } catch (Exception e){
    	Log.w("onResume: ", "NO ALARMS ALREADY");
    }
     super.onResume();
   }
 
   @Override
   protected void onPause() {
     datasource.close();
     super.onPause();
   }
 
 } 
