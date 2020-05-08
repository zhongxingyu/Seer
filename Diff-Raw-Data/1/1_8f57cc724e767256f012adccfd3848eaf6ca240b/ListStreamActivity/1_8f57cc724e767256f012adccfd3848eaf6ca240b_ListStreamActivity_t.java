 package org.fourdnest.androidclient.ui;
 
 import java.util.List;
 
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.EggManager;
 import org.fourdnest.androidclient.EggTimeComparator;
 import org.fourdnest.androidclient.FourDNestApplication;
 import org.fourdnest.androidclient.R;
 import org.fourdnest.androidclient.Util;
 import org.fourdnest.androidclient.services.RouteTrackService;
 
 import android.content.Intent;
 import android.os.Bundle;
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
 	public static final String PREFS_NAME = "ourPrefsFile";
 	private EggManager streamManager;
 	private ListView streamListView;
 
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
 
 		this.streamListView = (ListView) view.findViewById(R.id.egg_list);
 		initializeStreamList(this.streamManager, this.streamListView);
 		return view;
 
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
 
 		if (super.application.getKioskModeEnabled()) {
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
 			startActivity(new Intent(this, ListDraftEggsActivity.class));
 			return true;
 		case R.id.menu_stream_refresh:
 			refreshStreamList();
 			Toast.makeText(getApplicationContext(),
 					getText(R.string.stream_list_refreshed_toast), 1).show();
 			return true;
 		}
 		return false;
 	}
 
 	private void refreshStreamList() {
 		EggAdapter streamListViewAdapter = (EggAdapter) this.streamListView
 				.getAdapter();
 		streamListViewAdapter.clear();
 		List<Egg> newEggList = this.streamManager.listEggs();
 		for (Egg current : newEggList) {
 			streamListViewAdapter.add(current);
 		}
 		streamListViewAdapter.sort(new EggTimeComparator());
 		streamListViewAdapter.notifyDataSetChanged();
 	}
 
 	@Override
 	public void setNestSpecificOnClickListener(Button nestButton) {
 		return;
 	}
 
 }
