 package com.teamsexy.helloTabs;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class SpotsActivity extends FragmentActivity {
 
 	/* Spot database updates */
 	private FelixDbAdapter spotDbHelper;
 	private ListView spotsview;
 	
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Initialize database helper
         spotDbHelper = new FelixDbAdapter(this);
         spotDbHelper.open();
         
         // Initialize View
         spotsview = new ListView(this);
         spotsview.setOnItemClickListener(new OnItemClickListener () 
         {
         	public void onItemClick(AdapterView<?> parent, View v, int position, long id)
         	{
         		launchSpotEditActivity ();
         	}
         });
         
         // Fetch spots data
         getAllSpotsData();
     }
     
     /**
      * getAllSpotsData
      * 
      * Retrieve data for all spots from db adapter with a cursor. 
      */
     public void getAllSpotsData() {    	
     	Cursor spotsCursor = spotDbHelper.getAllSpotEntries();
     	List<String> spotNames = new ArrayList<String>();
     	spotNames.add("Create a Spot");
     	
     	// If query returns spots, display them in Spots Tab
     	// Might want to add ordering query so that most recent
     	// spots display first...
     	if (spotsCursor.getCount() > 0) {
     		spotsCursor.moveToFirst();
     		while (!spotsCursor.isAfterLast()) {
     			spotNames.add(spotsCursor.getString(1));
     			spotsCursor.moveToLast();
     		}
     		
     	}
     	
     	// Close cursor
     	spotsCursor.close();
     	spotsview.setAdapter(new ArrayAdapter<String>(this, 
 				android.R.layout.simple_list_item_1, spotNames));
     }
     
     /**
      * launchSpotEditActivity
      */
     public void launchSpotEditActivity ()
     {
     	Intent i = new Intent(this, SpotEditActivity.class);
 		startActivity(i);
     }
 }
