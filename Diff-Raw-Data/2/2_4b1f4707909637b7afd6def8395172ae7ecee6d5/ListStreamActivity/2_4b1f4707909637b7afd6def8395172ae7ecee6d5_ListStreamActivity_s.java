 package org.fourdnest.androidclient.ui;
 
 import org.fourdnest.androidclient.EggManager;
 import org.fourdnest.androidclient.FourDNestApplication;
 import org.fourdnest.androidclient.R;
 import org.fourdnest.androidclient.Util;
 import org.fourdnest.androidclient.services.RouteTrackService;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.ToggleButton;
 
 /**
  * The starting activity of the application. Displays a list of eggs present on
  * the server. Also provides functionality to access the create view and for
  * toggling route tracking.
  */
 public class ListStreamActivity extends NestSpecificActivity {
 	public static final String PREFS_NAME = "ourPrefsFile";
 	private EggManager streamManager;
 
 	/** Called when this Activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		this.streamManager = ((FourDNestApplication) getApplication())
 				.getStreamEggManager();
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View getContentLayout(View view) {
 
 		initializeTrackButton(view,
 				(ToggleButton) view.findViewById(R.id.route_tracker_button));
 
 		initializeCreateButton((Button) view.findViewById(R.id.create_button));
 
 		initializeStreamList(this.streamManager,
 				(ListView) view.findViewById(R.id.egg_list));
 
 		return view;
 
 	}
 
 	/**
 	 * Initializes the listing of Eggs appearing in egg_list
 	 * 
 	 * @param manager
 	 *            The Egg manager responsible for fetching the right Eggs
 	 * @param streamList
 	 *            Reference to the ListView that is responsible for displaying
 	 *            the Stream Listing
 	 */
 	private void initializeStreamList(EggManager manager, ListView streamList) {
 		EggReaderAdapter adapter = new EggReaderAdapter(streamList);
 		adapter.setEggs(manager.listEggs());
 		streamList.setAdapter(adapter);
 		streamList.setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				Intent intent = new Intent(arg1.getContext(),
 						ViewEggActivity.class);
 				arg0.getContext().startActivity(intent);
 
 			}
 		});
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
 	private void initializeTrackButton(View view, ToggleButton trackButton) {
 		trackButton.setChecked(Util.isServiceRunning(view.getContext(),
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
 
 	@Override
 	public int getLayoutId() {
 
 		/*
 		 * Following lines check if the 'kiosk' mode is on. If Kiosk mode is on,
 		 * start new egg activity and FINISH this one (prevents the back button
 		 * problem).
 		 */
 
 		super.application.getKioskModeEnabled();
 
		if (super.application.getKioskModeEnabled();) {
 			Intent intent = new Intent(this, NewEggActivity.class);
 			this.startActivity(intent);
 			finish();
 		}
 
 		return R.layout.list_stream_view;
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
 		case R.id.menu_stream_nests:
 			return true;
 		case R.id.menu_stream_drafts:
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void setNestSpecificOnClickListener(Button nestButton) {
 		return;
 	}
 
 }
