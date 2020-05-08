 package com.example.lufteapp;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.Menu;
 import android.widget.ArrayAdapter;
 
 public class ActivityLog extends ListActivity {
 	/*
 	 *  @author http://stackoverflow.com/questions/4540754/add-dynamically-elements-to-a-listview-android
 	 */
 	
 	
 	SQLiteDatabase db;
 	ArrayList<String> list = new ArrayList<String>();
 	ArrayAdapter<String> adapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_activity_log);
 		
 		adapter=new ArrayAdapter<String>(this,
 	            android.R.layout.simple_list_item_1,
 	            list);
	    
 		setListAdapter(adapter);
 		
 		
 		db = openOrCreateDatabase("gpsDataDB", MODE_PRIVATE,null);
 		
		Cursor cursor = db.rawQuery("SELECT * FROM gpsDataa", null);

 		while(cursor.moveToNext())
 		{
 			String lat = cursor.getString(0);
 			String lon = cursor.getString(1);
			list.add(lat + " " + lon);
 			adapter.notifyDataSetChanged();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_log, menu);
 		return true;
 	}
 
 }
