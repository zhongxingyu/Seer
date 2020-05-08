 package com.bixito;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.provider.Settings.Secure;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.bixito.station.BikeStation;
 
 public class MainActivity extends SherlockFragmentActivity implements
 		ActionBar.TabListener, ListViewFragment.ShareStationList {
 
 	private ArrayList<BikeStation> stationList = null;
 	private ListViewFragment listViewFragment;
 	private MapViewFragment mapViewFragment;
 	private String deviceId = null;
 	private boolean deviceIsTablet;
 
 	/**
 	 * The serialization (saved instance state) Bundle key representing the
 	 * current tab position.
 	 */
 	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_main);
 
 		if (findViewById(R.id.container) != null) {
 			// We're in phone mode
 			deviceIsTablet = false;
 			Log.d("DEBUG", "This device is a phone.");
 
 			// crate ListView and MapView fragments
 			listViewFragment = new ListViewFragment();
 			mapViewFragment = new MapViewFragment();
 
 			deviceId = Secure.getString(getBaseContext().getContentResolver(),
 					Secure.ANDROID_ID);
 			if (deviceId == null)
 				deviceId = "e" + Math.random();
 
 			// Setup action bar to show tabs
 			final ActionBar actionBar = getSupportActionBar();
 			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 			// For each of the sections in the app, add a tab to the action bar.
 			actionBar.addTab(actionBar.newTab()
 					.setText(R.string.title_section1).setTabListener(this));
 			actionBar.addTab(actionBar.newTab()
 					.setText(R.string.title_section2).setTabListener(this));
 
 			// setup FragmentTransaction used for initial creation of Fragments
 			android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
 			android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager
 					.beginTransaction();
 
 			// add both fragments to Activity
 			fragmentTransaction.add(R.id.container, mapViewFragment);
 			fragmentTransaction.add(R.id.container, listViewFragment, getString(R.string.list_view_fragment_tag)).commit();
 
 		} else {
 			deviceIsTablet = true;
 			Log.d("DEBUG", "This device is a tablet.");
 			mapViewFragment = (MapViewFragment) getSupportFragmentManager().findFragmentById(R.id.map_view_fragment);
			listViewFragment = (ListViewFragment) getSupportFragmentManager().findFragmentById(R.id.list_view_fragment);
 		}
 
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		// Restore the previously serialized current tab position.
 		if (findViewById(R.id.container) != null
 				&& savedInstanceState
 						.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
 			getSupportActionBar().setSelectedNavigationItem(
 					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		// Serialize the current tab position.
 		if (findViewById(R.id.container) != null)
 			outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
 					getSupportActionBar().getSelectedNavigationIndex());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
 		// When the given tab is selected, show the tab contents in the
 		// container view.
 		// tab position 0 references the LIST tab
 		if (tab.getPosition() == 0) {
 			// Display list fragment
 			if (listViewFragment == null) {
 				// hide map fragment and re-create list fragment
 				fragmentTransaction.hide(mapViewFragment);
 				fragmentTransaction.add(R.id.container, listViewFragment,
 						getString(R.string.list_view_fragment_tag));
 
 			} else {
 				// hide map fragment and show list fragment
 				fragmentTransaction.hide(mapViewFragment);
 				fragmentTransaction.show(listViewFragment);
 			}
 			
 			//pop all existing instances of listViewFragment existing in the back stack 
 			android.support.v4.app.FragmentManager fManager = getSupportFragmentManager();
 			fManager.popBackStack(null, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
 		}
 		// tab position 1 references the PAM tab
 		else {
 
 			// check if mapViewFragment is intact
 			if (mapViewFragment == null) {
 
 				// ------------- MAPS INITIAL SETUP -----------//
 				// Send in the station list to the map view fragment
 				Bundle bundle = new Bundle();
 				if (stationList == null)
 					Log.w("WARNING",
 							"Warning: Passing in a null station list to the map view fragment");
 				bundle.putParcelableArrayList("stationList", stationList);
 				Log.d("DEBUG", "Bundle size is: " + bundle.size());
 				mapViewFragment.setArguments(bundle);
 				Log.d("DEBUG", "Size of bundle once set is: "
 						+ mapViewFragment.getArguments().size());
 				// --------------------------------------------//
 
 				// hide list view fragment and re-create map view fragment
 				fragmentTransaction.hide(listViewFragment);
 				fragmentTransaction.add(R.id.container, mapViewFragment,
 						getString(R.string.map_view_fragment_tag));
 			} else {
 
 				// hide list fragment and show map fragment
 				fragmentTransaction.hide(listViewFragment);
 				fragmentTransaction.show(mapViewFragment);
 			}
 		}
 
 	}
 	
 	/* ----------- method for swapping between the LIST and MAP fragments ----------- */
 	//-> still in testing phase, use at your own discretion
 	public void swapBetweenListAndMapFragments(){
 		//setup FragmentTransaction for hide/show of fragments
 		android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
 		android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager
 				.beginTransaction();
 		
 		if (listViewFragment.isHidden()){
 			//hide map view, show list view
 			fragmentTransaction.hide(mapViewFragment);
 			fragmentTransaction.show(listViewFragment).commit();
 		}
 		else if (mapViewFragment.isHidden()){
 			//hide list view, show map view
 			fragmentTransaction.hide(listViewFragment);
 			fragmentTransaction.show(mapViewFragment).commit();
 		}
 	}
 
 	public void onTabUnselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	public void onTabReselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	/**
 	 * A dummy fragment representing a section of the app, but that simply
 	 * displays dummy text.
 	 */
 	public static class DummySectionFragment extends Fragment {
 		/**
 		 * The fragment argument representing the section number for this
 		 * fragment.
 		 */
 		public static final String ARG_SECTION_NUMBER = "section_number";
 
 		public DummySectionFragment() {
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			// Create a new TextView and set its text to the fragment's section
 			// number argument value.
 			TextView textView = new TextView(getActivity());
 			textView.setGravity(Gravity.CENTER);
 			textView.setText(Integer.toString(getArguments().getInt(
 					ARG_SECTION_NUMBER)));
 			return textView;
 		}
 	}
 
 	@Override
 	public void shareList(ArrayList<BikeStation> stationList) {
 		//save passed station list
 		this.stationList = stationList;
 		
 		Log.d("DEBUG", "Got back: " + stationList.size()
 				+ " stations from ListViewFragment.");
 
 		if (mapViewFragment != null) {
 			// Call a method to pass in the station list
 			mapViewFragment.updateStationList(stationList);
 		} else {
 			Log.d("DEBUG",
 					"Could not find the map view fragment when updating the station list.");
 		}
 	}
 	
 	@Override
 	public void shareSelectedStation(BikeStation selectedStation){
 
 		//check if device is a phone, if it is - change the selected tab to "Maps"
 		if (!deviceIsTablet){
 			//getSupportActionBar().setSelectedNavigationItem(1);
 			
 			//setup FragmentTransaction
 			android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
 			android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager
 					.beginTransaction();
 			
 			//hide list view, show map view
 			fragmentTransaction.hide(listViewFragment);
 			fragmentTransaction.addToBackStack(getString(R.string.list_view_fragment_tag));
 			fragmentTransaction.show(mapViewFragment).commit();
 
 		}
 		
 		//animate the position of the map fragment to the desired station's location
 		if(mapViewFragment == null)
 			Log.w("WARNING", "MapViewFragment is null");
 		else
 			mapViewFragment.animateMapLocation(selectedStation);
 	}
 	
 	@Override
 	public void selectMapTab(){
 		getSupportActionBar().setSelectedNavigationItem(1);
 	}
 	
 	@Override
 	public void selectListTab(){
 		getSupportActionBar().setSelectedNavigationItem(0);
 	}
 	
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.refresh:
 			// Refresh the list/map
 			if (listViewFragment != null)
 				listViewFragment.loadStationList();
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	public String getDeviceId(){
 		return deviceId;
 	}
 
 }
