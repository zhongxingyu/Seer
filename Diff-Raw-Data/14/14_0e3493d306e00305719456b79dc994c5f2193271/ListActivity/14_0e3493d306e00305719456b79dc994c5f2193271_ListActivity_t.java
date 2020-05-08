 package com.example.listapplication;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ListView;
 
 public class ListActivity extends Activity {
 	
 	TimeTrackerAdapter timeTrackerAdapter;
 	
 	private final int ADD_TIME_ACTIVITY = 1;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_list);
         ListView listView = (ListView)findViewById(R.id.times_list);
         timeTrackerAdapter = new TimeTrackerAdapter();
         //Attach Adapter to the ListView
         listView.setAdapter(timeTrackerAdapter);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.list, menu);
         return true;
     }
     
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
     	if(item.getItemId() == R.id.add_time_menu_item){
     		Intent intent = new Intent(this , AddTimeActivity.class);
     		startActivityForResult(intent, ADD_TIME_ACTIVITY);
     		return true;
     	}
    	return true;//super.onMenuItemSelected(featureId, item);
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	// TODO Auto-generated method stub
    	if(requestCode == ADD_TIME_ACTIVITY && resultCode == RESULT_OK){
     		String time = data.getStringExtra("time");
     		
     		String note = data.getStringExtra("note");
     		
     		
     		timeTrackerAdapter.addTimeRecord(new TimeRecord(time,note));
     		
     		timeTrackerAdapter.notifyDataSetChanged();
     		
     	}
     	
     	
     	super.onActivityResult(requestCode, resultCode, data);
     }
     
 }
