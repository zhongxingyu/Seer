 package com.barefoot.pocketshake;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.barefoot.pocketshake.data.EarthQuake;
 import com.barefoot.pocketshake.service.FeedSynchronizer;
 import com.barefoot.pocketshake.storage.EarthQuakeDataWrapper;
 
 public class PocketShake extends ListActivity {
 	
 	private QuakeCustomAdapter messageListAdapter = null;
 	private EarthQuakeDataWrapper dbWrapper;
 	private ProgressDialog dialog;
 	final private static String LOG_TAG = "Pocket Shake";
 	
 	private final static int SETTINGS = 1;
 	private final static int ABOUT = 2;
 	private final static int CANCEL = 3;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	Log.i(LOG_TAG, "Creating an instance of activity");
         setContentView(R.layout.main);
         this.dialog = new ProgressDialog(this);
         this.dialog.setMessage("Fetching...");
         this.dialog.show();
         dbWrapper = new EarthQuakeDataWrapper(this);
         updateQuakeFeed();
         this.dialog.dismiss();
     }
     
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Log.i(LOG_TAG, "List item clicked at position :: " + position);
 		Intent intent = new Intent(PocketShake.this, QuakeMapView.class);
 		intent.putExtra("POS", position);
         startActivity(intent);
 	}
     
     @Override
 	public void onResume() {
     	Log.i(LOG_TAG, "Resuming activity, registering with background service for broadcasts");
 		super.onResume();
 		registerReceiver(receiver, new IntentFilter(FeedSynchronizer.BROADCAST_ACTION));
 		registerReceiver(refreshReceiver , new IntentFilter(QuakePrefrences.BROADCAST_ACTION));
 	}
 
 	@Override
 	public void onPause() {
 		Log.i(LOG_TAG, "Pausing activity, unregistering with background service for broadcasts");
 		super.onPause();
 		unregisterReceiver(receiver);
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		unregisterReceiver(refreshReceiver);
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, SETTINGS, 0, "Settings").setIcon(R.drawable.settings);
 	    menu.add(0, ABOUT, 0, "About").setIcon(R.drawable.about);
 	    menu.add(0, CANCEL, 0, "Quit").setIcon(R.drawable.cancel);
 	    return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	    case SETTINGS:
 	    	Intent newIntent = new Intent(this, QuakePrefrences.class);
 	    	startActivity(newIntent);
 	        return true;
 	    case ABOUT:
 	    	return true;
 	    case CANCEL:
 	        this.finish();
 	    }
 	    return false;
 	}
 	
 	private void updateQuakeFeed() {
 		Log.i(LOG_TAG, "Updating Quake Feed");
 		dbWrapper.refreshFeedCache(true);
 		messageListAdapter = new QuakeCustomAdapter(this, R.layout.quake, fetchLatestFeeds()); 
 		setListAdapter(messageListAdapter);
 		Log.i(LOG_TAG, "Message List Adapter fetched and set");
//		Commenting it out, as it is creating problem with some error right now. Needs to be fixed and uncommented
//		showUpdateIndication();
 	}
 	
 	private void showUpdateIndication() {
 		Log.i(LOG_TAG, "Showing update indication");
		Toast updateNotification = Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT);
 		updateNotification.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
 		updateNotification.show();
 	}
 		
 	private BroadcastReceiver receiver=new BroadcastReceiver() {
 		public void onReceive(Context context, Intent intent) {
 			Log.i(LOG_TAG, "Broadcast received ::" + intent.getAction());
 			updateQuakeFeed();
 		}
 	};
 	
 	private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Log.i(LOG_TAG, "Refresh notification received, will refresh screen contents now");
 			updateQuakeFeed();		
 		}
 	};
 	
 	private ArrayList<EarthQuake> fetchLatestFeeds() {
 		Log.i(LOG_TAG, "Fetching string representation of quakes");
 		return dbWrapper.getEarthQuakes();
 	}
 }
