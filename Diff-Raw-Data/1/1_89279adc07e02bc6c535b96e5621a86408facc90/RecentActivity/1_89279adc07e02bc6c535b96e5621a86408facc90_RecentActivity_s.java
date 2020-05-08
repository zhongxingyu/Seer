 /**
 See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  This code is licensed
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */		
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
 	FirstTimeDialog ftd;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		newsgroupListMenu = new NewsgroupListMenu(this);
 		newsgroupListMenu.checkEnabled();
 		hc = new HttpsConnector( this);	    	    
 		dialog = new InvalidApiKeyDialog(this);
 		ftd = new FirstTimeDialog(this);
 		setContentView(R.layout.activity_recent);
 
 		rf = (RecentFragment)getSupportFragmentManager().findFragmentById(R.id.recent_fragment);
 
 		if (!sharedPref.getBoolean("first_time", true)) {
 
 			if (sharedPref.getString("newsgroups_json_string", "") != "") {
 				newsgroupListMenu.update(hc.getNewsGroupFromString(sharedPref.getString("newsgroups_json_string", "")));
 				hc.startUnreadCountTask();
 				Log.d("newdebug", "3");
 			} else {
 				hc.getNewsGroups();
 				Log.d("newdebug", "4");
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
 		} else {
 
 			ftd.show();
 			SharedPreferences.Editor editor = sharedPref.edit();
 			editor.putBoolean("first_time", false);
 			editor.commit();
 
 
 		}
 
 		setTitle("Recent Posts");
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
 
 		case R.id.menu_mark_all_read:
 			hc.markRead();
 			hc.getNewest(false);
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
 
 	/**
 	 * This is called to by the async task when there is an fragment to update.
 	 * This then updates the correct fragment based on what is given. The options
 	 * are: show an error dialog, update recent posts, update newgroups, and update
 	 * unread counts. When unread counts is updated and is different from what it used
 	 * to be, then it updates the newsgroups to get the correct display.
 	 * @param jsonString - a string representing the json object of the data
 	 */
 	@Override
 	public void update(String jsonString) {
 		try {
 			JSONObject obj = new JSONObject(jsonString);
 			if (obj.has("error")) { // invalid api key
 				if (!dialog.isShowing()) {
 					if (!ftd.isShowing()) {
 						dialog.show();
 					}
 				}
 			} else if (obj.has("activity")) { // recent
 				Log.d("string", hc.getNewestFromString(jsonString).toString());
 				rf.update(hc.getNewestFromString(jsonString));
 			} else if (obj.has("unread_counts")) {  // unread count
 				int unread = hc.getUnreadCountFromString(jsonString)[0];
 				int groupUnread = 0;
 				for (Newsgroup group : hc.getNewsGroupFromString(sharedPref.getString("newsgroups_json_string", ""))) {
 					groupUnread += group.unreadCount;
 				}
 				if (unread != groupUnread) {
 					hc.getNewsGroups();
 					Log.d("jddebug - RecentActivity-update", "newsgroups updated");
 				} else {
 					newsgroupListMenu.update(hc.getNewsGroupFromString(sharedPref.getString("newsgroups_json_string", "")));
 				}
 				Log.d("jddebug-RecentActivity", unread + " " + groupUnread);
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
 		hc.startUnreadCountTask();
 		if (sharedPref.getString("newsgroups_json_string", "") != "") {
 			Log.d("newdebug", "1");
 			newsgroupListMenu.update(hc.getNewsGroupFromString(sharedPref.getString("newsgroups_json_string", "")));
 			hc.startUnreadCountTask();
 		} else {
 			Log.d("newdebug", "2");
 			hc.getNewsGroups();
 		}
 		/*hc.getNewsGroups();
 		
 		if(newsgroupListMenu.newsgroupAdapter != null)
 		{
 			newsgroupListMenu.newsgroupAdapter.clear();
 			for(Newsgroup ng : newsgroupListMenu.newsgroupList)
 				newsgroupListMenu.newsgroupAdapter.add(ng);
 			newsgroupListMenu.newsgroupAdapter.notifyDataSetChanged();
 		}*/
 	}
 
 }
