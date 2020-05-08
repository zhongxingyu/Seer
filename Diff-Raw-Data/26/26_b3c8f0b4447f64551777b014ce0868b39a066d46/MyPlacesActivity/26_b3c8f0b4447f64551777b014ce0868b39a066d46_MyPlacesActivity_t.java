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
 
 import edu.fiu.cs.seniorproject.data.MbGuideDB;
 
 public class MyPlacesActivity extends ListActivity {
 
 	MbGuideDB mb = new MbGuideDB(this); 
 	ArrayList<String> pList = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.listPlaceNames();
     
        if ( pList != null ) {
	        setListAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, pList));
        }
         
     }
 
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_my_places, menu);
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
  
             case R.id.delete_all_from_calendar:
             	
             	if(pList != null)
             	{
 	            	this.onDeleteAllEventsFromCalendarAndDBClick2(item);
 	            	return true;
             	}
         }
         return super.onOptionsItemSelected(item);
     }
     
     public void onSettingsClick(MenuItem view) {
     	Intent intent = new Intent(this, SettingsActivity.class);
     	this.startActivity(intent);
     }
 
     
     public void onDeleteAllEventsFromCalendarAndDBClick2(MenuItem view) { 	  	
     	
     	
     	if(pList!=null)
     	{
 	    	deleteAllPlacesFromDB();
 	    	pList=null;
 	    	Intent intent = new Intent(this, MyPlacesActivity.class);
 	    	this.startActivity(intent);
 	    	
     	}
     	else
     	{
     		
     	}
     }    
     
     
     private void listPlaceNames()
     {    	 
     	try
     	{
     		mb.openDatabase();
     		pList = mb.listPlaceNames(); 
     	    mb.closeDatabase();
     	}catch(SQLException e)
     	 {
    		pList = null;
     		e.printStackTrace();
     	 }    	
     	
     }
     
     
     private void deleteAllPlacesFromDB()
     {    	 
     	try
     	{
     		mb.openDatabase();
     		mb.deleteAllPlaces();
     	    mb.closeDatabase();
     	}catch(SQLException e)
     	 {
     		e.printStackTrace();
     	 }    	
     	
     }
 }
