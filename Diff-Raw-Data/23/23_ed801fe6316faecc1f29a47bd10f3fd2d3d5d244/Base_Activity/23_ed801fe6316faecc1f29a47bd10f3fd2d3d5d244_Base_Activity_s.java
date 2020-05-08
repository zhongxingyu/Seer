 package com.derpicons.gshelf;
 
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.SearchView;
 
 public class Base_Activity extends Activity 
 {
 
 	private final String TAG = "Base_Activity";
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) 
 	{
 		// Inflate the menu
 		getMenuInflater().inflate(R.menu.main_menu, menu);
//		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
//		return super.onCreateOptionsMenu(menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 		case R.id.action_search:
 			Log.i(TAG, "Action Search Clicked");
 			return true;
 			
 		case R.id.action_settings:
 			Log.i(TAG, "Action Settings Clicked");
 			return true;
 			
 		case R.id.action_logout:
 			Log.i(TAG, "Action Logout Clicked");
 			return true;
 			
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	//return true;	
 	}
 	
 	
 }
