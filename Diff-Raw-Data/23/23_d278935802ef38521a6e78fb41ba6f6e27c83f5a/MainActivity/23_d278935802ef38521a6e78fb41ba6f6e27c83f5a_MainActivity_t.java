 package com.sportsfire.exposure;
 
import com.WeiGu.SporysFireExposure.R;

 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class MainActivity extends ListActivity  {
 
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) { 
 		super.onCreate(savedInstanceState);
 		String[] MENUITEMS = getResources().getStringArray(R.array.menu_array);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 			android.R.layout.simple_list_item_1, MENUITEMS);
 		setListAdapter(adapter);
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 	  Intent myIntent;
 	  switch(position) {
 	  case 0:  myIntent = new Intent(MainActivity.this, SportsFireExposure.class);
               break; 
 	  
 	  case 1:  myIntent = new Intent(MainActivity.this, About.class);
 	  break; 
 	  
 	  case 2:  myIntent = new Intent(MainActivity.this, Quit.class);
 	  		  break; 
 	  		  
 	  
 	  
 	  default: 	myIntent = new Intent(MainActivity.this, MainActivity.class);
 			break; 			
 }
        
 		 
 	  MainActivity.this.startActivity(myIntent);
 	}
 
 
 }
