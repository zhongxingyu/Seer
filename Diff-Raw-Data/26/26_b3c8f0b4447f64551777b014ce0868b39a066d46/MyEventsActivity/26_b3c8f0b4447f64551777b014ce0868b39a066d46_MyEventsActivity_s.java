 package edu.fiu.cs.seniorproject;
 
 import java.util.ArrayList;
 
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.SQLException;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import edu.fiu.cs.seniorproject.data.MbGuideDB;
 
 public class MyEventsActivity extends ListActivity {
 	
 	MbGuideDB mb = new MbGuideDB(this); 
 	ArrayList<String> eList = null;
 	ArrayList<String> eList2 = null;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getEventNames();
         eList2 = new ArrayList<String>();
         eList2.add("Joe");
         eList2.add("Joe2");
         if(eList != null)
         {
 	        setListAdapter(new ArrayAdapter<String>(this ,
 					android.R.layout.simple_dropdown_item_1line, eList));
 
         }
         else
         {
         	TextView tv = (TextView)findViewById(android.R.id.text1);
        	tv.setText("No events");
         }
         
     }
 
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_my_events, menu);
         return true;
     }
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
             case R.id.menu_settings:
                 this.onSettingsClick(item);
                 return true;
  
             case R.id.delete_from_calendar:
             	this.onDeleteAllEventsFromCalendarAndDBClick2(item);
             	return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     public void onSettingsClick(MenuItem view) {
     	Intent intent = new Intent(this, SettingsActivity.class);
     	this.startActivity(intent);
     }
 
     
     public void onDeleteAllEventsFromCalendarAndDBClick2(MenuItem view) { 	  	
     	
     	// Syncronize this to delete the event from both, DB and Calendar
     	//deleteAllEventsFromCalendarAndDB();
     }
     
     
     
     private void getEventNames()
     {
     	 
     	try
     	{
     		mb.openDatabase();
     		eList = mb.listEventNames(); 
     	    mb.closeDatabase();
     	}catch(SQLException e)
     	 {
     		e.printStackTrace();
     	 }
     	
     	
     }
     
     
     
 }
