 
 package org.fourdnest.androidclient.ui;
 
 import java.util.List;
 
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.EggManager;
 import org.fourdnest.androidclient.EggTimeComparator;
 import org.fourdnest.androidclient.FourDNestApplication;
 import org.fourdnest.androidclient.R;
 import org.fourdnest.androidclient.Util;
 import org.fourdnest.androidclient.services.RouteTrackService;
 import org.fourdnest.androidclient.services.StreamReaderService;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 /**
  * The starting activity of the application. Displays a list of eggs present on
  * the server. Also provides functionality to access the create view and for
  * toggling route tracking.
  */
 public class ListStreamActivity extends NestSpecificActivity {
 	public static final String TAG = ListStreamActivity.class.getSimpleName();
 	public static final String PREFS_NAME = "ourPrefsFile";
 	private EggManager streamManager;
 	private ListView streamListView;
 	private LocalBroadcastManager mLocalBroadcastManager;
 	private BroadcastReceiver mReceiver;
 
 	/** Called when this Activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		this.application = (FourDNestApplication) getApplication();
 
 		setContentView(R.layout.list_stream_view);
 
 		this.streamManager = this.application.getStreamEggManager();
 		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
 		IntentFilter filter = new IntentFilter();
         filter.addAction(StreamReaderService.ACTION_STREAM_UPDATED);
         this.mReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
             	Log.d(TAG, "BroadcastReceiver.onReceive");
             	if (intent.getAction().equals(StreamReaderService.ACTION_STREAM_UPDATED)) {
             		refreshStreamList();
             	}
             }
         };
         Log.d(TAG, "Registering the broadcast receiver");
         mLocalBroadcastManager.registerReceiver(mReceiver, filter);
         
 		initializeTrackButton((ToggleButton) findViewById(R.id.route_tracker_button));
 
 		initializeCreateButton((Button) findViewById(R.id.create_button));
 
 		this.streamListView = (ListView) findViewById(R.id.egg_list);
 		initializeStreamList(this.streamManager, this.streamListView);
 
 		super.onCreate(savedInstanceState);
 
 	}
 	
 	@Override
 	public void onResume() {
		super.onResume();
 		/*
 		 * Following lines check if the 'kiosk' mode is on. If Kiosk mode is on,
 		 * start new egg activity and FINISH this one (prevents the back button
 		 * problem).
 		 */
 
 		if (this.application.getKioskModeEnabled()) {
 			Intent intent = new Intent(this, NewEggActivity.class);
 			this.startActivity(intent);
 			finish();
 		}
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.d(TAG, "UnRegistering the broadcast receiver");
 		mLocalBroadcastManager.unregisterReceiver(this.mReceiver);
 	}
 
 	/**
 	 * Initializes the listing of Eggs appearing in egg_list
 	 * 
 	 * @param manager
 	 *            The Egg manager responsible for fetching the right Eggs
 	 * @param streamListView
 	 *            Reference to the ListView that is responsible for displaying
 	 *            the Stream Listing
 	 */
 	private void initializeStreamList(EggManager manager,
 			ListView streamListView) {
 		EggAdapter adapter = new EggAdapter(streamListView,
 				R.layout.egg_element_large, manager.listEggs());
 		streamListView.setAdapter(adapter);
 		((EggAdapter)streamListView.getAdapter()).sort(new EggTimeComparator());
 		((EggAdapter)streamListView.getAdapter()).notifyDataSetChanged();
 		streamListView.setOnItemClickListener(new EggItemOnClickListener(
 				streamListView));
 	}
 
 	/**
 	 * Initializes the Create Button. The Create Button switches the active
 	 * activity to (i.e. moves to) NewEggActivity.
 	 * 
 	 * @param createButton
 	 *            The Create button
 	 * @see NewEggActivity
 	 */
 	private void initializeCreateButton(Button createButton) {
 		createButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Intent intent = new Intent(v.getContext(), NewEggActivity.class);
 				v.getContext().startActivity(intent);
 
 			}
 		});
 	}
 
 	/**
 	 * Initializes the Track Button that toggles GPS tracking.
 	 * 
 	 * @param view
 	 *            The view which the Track Button belongs in.
 	 * @param trackButton
 	 *            The ToggleButton responsible for toggling GPS tracking on and
 	 *            off.
 	 */
 	private void initializeTrackButton(ToggleButton trackButton) {
 		trackButton.setChecked(Util.isServiceRunning(getApplicationContext(),
 				RouteTrackService.class));
 
 		trackButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Intent intent = new Intent(v.getContext(),
 						RouteTrackService.class);
 				if (Util.isServiceRunning(v.getContext(),
 						RouteTrackService.class)) {
 					v.getContext().stopService(intent);
 				} else {
 					v.getContext().startService(intent);
 				}
 			}
 		});
 	}
 
 
 	/**
 	 * Creates the options menu on the press of the Menu button.
 	 * 
 	 * @param menu
 	 *            The menu to inflate
 	 * @return Boolean indicating success of creating the menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.stream_menu, menu);
 		return true;
 	}
 
 	/**
 	 * Specifies the action to perform when a menu item is pressed.
 	 * 
 	 * @param item
 	 *            The MenuItem that was pressed
 	 * @return Boolean indicating success of identifying the item
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_stream_pref:
 			startActivity(new Intent(this, PrefsActivity.class));
 			return true;
 		case R.id.menu_stream_help:
 			return true;
 		//case R.id.menu_stream_nests:
 		//	return true;
 		case R.id.menu_stream_drafts:
 			startActivity(new Intent(this, ListDraftEggsActivity.class));
 			return true;
 		case R.id.menu_stream_refresh:
 			Toast.makeText(getApplicationContext(),
 					getText(R.string.stream_list_refreshing_toast), 1).show();
 			// The reply comes through broadcast
 			StreamReaderService.requestUpdate(this);
 			return true;
 		}
 		return false;
 	}
 
 	private void refreshStreamList() {
 		Log.d(TAG, "refreshStreamList");
 		EggAdapter streamListViewAdapter = (EggAdapter) this.streamListView
 				.getAdapter();
 		List<Egg> newEggList = this.streamManager.listEggs();
 		streamListViewAdapter.clear();
 		for (Egg current : newEggList) {
 			streamListViewAdapter.add(current);
 		}
 		streamListViewAdapter.sort(new EggTimeComparator());
 		streamListViewAdapter.notifyDataSetChanged();
 		Toast.makeText(getApplicationContext(),
 				getText(R.string.stream_list_refreshed_toast), 1).show();
 	}
 
 }
