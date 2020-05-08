 package com.derpicons.gshelf;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.SearchView;
 
 public class Base_Activity extends Activity 
 {
 
 	private final String TAG = "Base_Activity";
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) 
 	{
 		// Inflate the menu
 		getMenuInflater().inflate(R.menu.main_menu, menu);
 		//SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
 		//return super.onCreateOptionsMenu(menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 		case R.id.action_search:
 			Log.i(TAG, "Action Search Clicked");
 			
 			Intent i = new Intent(getApplicationContext(), SearchActivity.class);
 			startActivity(i);
 
 			return true;
 			
 		case R.id.action_settings:
 			Log.i(TAG, "Action Settings Clicked");
 			return true;
 			
 		case R.id.action_logout:
 			Log.i(TAG, "Action Logout Clicked");
 			
 			//delete shared preferences
			SharedPreferences settings = getSharedPreferences("GSHELF_LOGIN", 0);
 			SharedPreferences.Editor editor = settings.edit();
 				editor.remove("username");
 				editor.remove("password");
 				editor.commit();
 			
 			//intent return to login
 			Intent j = new Intent(getApplicationContext(), Login.class);
 			j.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(j);
 			
 			return true;
 			
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	//return true;	
 	}
 	
 	
 }
