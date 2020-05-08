 package edu.rit.csh.androidwebnews;
 
 import java.util.ArrayList;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 
 public class RecentActivity extends FragmentActivity implements ActivityInterface {
 	InvalidApiKeyDialog dialog;
 	ProgressDialog p;
 	RecentFragment rf;
 	HttpsConnector hc;
 	NewsgroupListMenu newsgroupListMenu;
 	SharedPreferences sharedPref;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		newsgroupListMenu = new NewsgroupListMenu(this);
 		newsgroupListMenu.checkEnabled();
 		
     
 	    hc = new HttpsConnector( this);	    	    
 	    dialog = new InvalidApiKeyDialog(this);
 	    
 		setContentView(R.layout.activity_recent);
 		rf = (RecentFragment)getSupportFragmentManager().findFragmentById(R.id.recent_fragment);
 		
 		if (sharedPref.getString("newsgroups_json_string", "") != "") {
 			newsgroupListMenu.update(hc.getNewsGroupFromString(sharedPref.getString("newsgroups_json_string", "")));	
 		} else {
 			hc.getNewsGroups();
 		}
 		
 		Intent intent = new Intent(this, UpdaterService.class);
 		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
 		AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
 
 		// if the run service is selected, an alarm is started to repeat over given time
 		if (sharedPref.getBoolean("run_service", false)) {
 			alarm.cancel(pintent);
 			alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Integer.valueOf(sharedPref.getString("time_between_checks", "15")) * 60000, pintent);
 			Log.d("jddebug", "alarm set");
 		} else {
 			alarm.cancel(pintent);
 		}
 		
 	}
 	
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_default, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			startActivity(new Intent(this, SettingsActivity.class));
 			return true;
 		
 		case R.id.menu_refresh:
 			hc.getNewest(true);
 			hc.getNewsGroups();
 			return true;
 			
 		case R.id.menu_about:
 			startActivity(new Intent(this, InfoActivity.class));
 			return true;
 		case R.id.menu_search:
 			startActivity(new Intent(this, SearchActivity.class));
 		}
 		return false;
 	}
 
 	public void onThreadSelected(final PostThread thread) {
 		
 	    String apiKey = sharedPref.getString("api_key", "");	    
	    //HttpsConnector hc = new HttpsConnector(this);
 		//hc.getNewsgroupThreads(thread.newsgroup, 20);
 		Intent myIntent = new Intent(RecentActivity.this, DisplayThreadsActivity.class);
 		myIntent.putExtra("SELECTED_NEWSGROUP", thread.newsgroup);
 		startActivity(myIntent);
 	}
 
 	@Override
 	public void update(String jsonString) {
 		try {
 			JSONObject obj = new JSONObject(jsonString);
 			if (obj.has("error")) { // invalid api key
 				if (!dialog.isShowing()) {
 					dialog.show();
 				}
 			} else if (obj.has("activity")) { // recent
 				rf.update(hc.getNewestFromString(jsonString));
 			} else {  // newsgroups
 				SharedPreferences.Editor editor = sharedPref.edit();
 				editor.putString("newsgroups_json_string", jsonString);
 				editor.commit();
 				Log.d("jddebugcache", "update cache1");
 				newsgroupListMenu.update(hc.getNewsGroupFromString(jsonString));
 			}
 		} catch (JSONException e) {
 		}
 	}
 	public void onNewsgroupSelected(final String newsgroupName) {
 		Intent myIntent = new Intent(RecentActivity.this, DisplayThreadsActivity.class);
 		myIntent.putExtra("SELECTED_NEWSGROUP", newsgroupName);
 		startActivity(myIntent);
 	}
 	
 	@Override
 	public void onResume()
 	{
 		super.onResume();
		//hc.getNewsGroups();
		
 		if(newsgroupListMenu.newsgroupAdapter != null)
 		{
 			newsgroupListMenu.newsgroupAdapter.clear();
 			for(Newsgroup ng : newsgroupListMenu.newsgroupList)
 				newsgroupListMenu.newsgroupAdapter.add(ng);
 			newsgroupListMenu.newsgroupAdapter.notifyDataSetChanged();
 		}
 	}
 
 }
