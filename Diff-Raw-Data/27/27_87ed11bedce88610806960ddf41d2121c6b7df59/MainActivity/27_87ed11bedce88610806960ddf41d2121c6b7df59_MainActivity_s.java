 package com.bixito;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
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
 import com.bixito.station.BikeStation;
 
 public class MainActivity extends SherlockFragmentActivity implements
 		ActionBar.TabListener,ListViewFragment.ShareStationList {
 	
 	private ArrayList<BikeStation> stationList = null;
 	private ListViewFragment listViewFragment;
 	private MapViewFragment mapViewFragment;
 
 	/**
 	 * The serialization (saved instance state) Bundle key representing the
 	 * current tab position.
 	 */
 	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.activity_main);
 		
 		
 		if(findViewById(R.id.container) != null){
 			//We're in phone mode
 			Log.d("DEBUG", "This device is a phone.");
 			
 			//crate ListView and MapView fragments
 			listViewFragment = new ListViewFragment();
 			mapViewFragment = new MapViewFragment();
 			
 			//Setup action bar to show tabs
 			final ActionBar actionBar = getSupportActionBar();
 			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 			// For each of the sections in the app, add a tab to the action bar.
 			actionBar.addTab(actionBar.newTab().setText(R.string.title_section1)
 					.setTabListener(this));
 			actionBar.addTab(actionBar.newTab().setText(R.string.title_section2)
 					.setTabListener(this));
 			
 			
 			
 			//setup FragmentTransaction used for initial creation of Fragments
 			android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
 			android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 			
 			
 			/*------------- MAPS SETUP -----------//
 			//Send in the station list to the map view fragment
 			Bundle bundle = new Bundle();
 			if(stationList == null)
 				Log.w("WARNING", "Warning: Passing in a null station list to the map view fragment");
 			bundle.putParcelableArrayList("stationList", stationList);
 			Log.d("DEBUG", "Bundle size is: " + bundle.size());
 			mapViewFragment.setArguments(bundle);
 			Log.d("DEBUG", "Size of bundle once set is: " + mapViewFragment.getArguments().size());
 			//-------------------------------------*/
 			
 			
 			//add both fragments to Activity
 			fragmentTransaction.add(R.id.container, mapViewFragment);
 			fragmentTransaction.add(R.id.container, listViewFragment).commit();
 
 		}
 		else{
 			Log.d("DEBUG", "This device is a tablet.");
 			
 			//ListViewFragment listFrag = new ListViewFragment();
 			//MapViewFragment mapFrag = new MapViewFragment();
 			//getSupportFragmentManager().beginTransaction().add(R.id.list_view_fragment, listFrag).add(R.id.map_view_fragment, mapFrag).commit();
 		}
 
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		// Restore the previously serialized current tab position.
 		if (findViewById(R.id.container) != null && savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
 			getSupportActionBar().setSelectedNavigationItem(
 					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		// Serialize the current tab position.
 		if(findViewById(R.id.container) != null)
 			outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getSupportActionBar()
 				.getSelectedNavigationIndex());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	/*
 	 * @Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	}
 	 */
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
 		// When the given tab is selected, show the tab contents in the
 		// container view.
 		/*Fragment fragment = new DummySectionFragment();
 		Bundle args = new Bundle();
 		args.putInt(DummySectionFragment.ARG_SECTION_NUMBER,
 				tab.getPosition() + 1);
 		fragment.setArguments(args);
 		getSupportFragmentManager().beginTransaction()
 				.replace(R.id.container, fragment).commit();*/
 		
 		//tab position 0 references the LIST tab
 		
 		if(tab.getPosition() == 0){
 			//Display list fragment
 			//fragmentTransaction.replace(R.id.container, listViewFragment, getString(R.string.list_view_fragment_tag));
 			
 			if (listViewFragment == null) {
 				
 				//hide map fragment and re-create list fragment
 				fragmentTransaction.hide(mapViewFragment);
 				fragmentTransaction.add(R.id.container, listViewFragment, getString(R.string.list_view_fragment_tag));
 				
             } 
 			else {
 				
 				//hide map fragment and show list fragment
             	fragmentTransaction.hide(mapViewFragment);
     			fragmentTransaction.show(listViewFragment);
             }
 			
 			
 			
 		}
 		//tab position 1 references the PAM tab
 		else{
 			
 			//check if mapViewFragment is intact
 			if (mapViewFragment == null){
 				
 				//------------- MAPS INITIAL SETUP -----------//
 				//Send in the station list to the map view fragment
 				Bundle bundle = new Bundle();
 				if(stationList == null)
 					Log.w("WARNING", "Warning: Passing in a null station list to the map view fragment");
 				bundle.putParcelableArrayList("stationList", stationList);
 				Log.d("DEBUG", "Bundle size is: " + bundle.size());
 				mapViewFragment.setArguments(bundle);
 				Log.d("DEBUG", "Size of bundle once set is: " + mapViewFragment.getArguments().size());
 				//--------------------------------------------//
 				
 				//hide list view fragment and re-create map view fragment
 				fragmentTransaction.hide(listViewFragment);
 				fragmentTransaction.add(R.id.container, mapViewFragment, getString(R.string.map_view_fragment_tag));
 			}
 			else{
 				
 				//hide list fragment and show map fragment
 				fragmentTransaction.hide(listViewFragment);
 				fragmentTransaction.show(mapViewFragment);
 			}
 			
 			
 			//getSupportFragmentManager().beginTransaction().replace(R.id.container, mapViewFragment).commit();
 			
 			//MapFragment mapFragment = new MapFragment();
 			//fragmentTransaction.replace(R.id.container, mapFragment);
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
 		this.stationList = stationList;
 		Log.d("DEBUG", "Got back: " + stationList.size() + " stations from ListViewFragment.");
 		//MapViewFragment mapViewFragment = (MapViewFragment) getFragmentManager().findFragmentByTag(getString(R.string.map_view_fragment_tag));
 		
		if(getSupportFragmentManager().findFragmentById(R.id.map_view_fragment) != null){
 			//Call a method to pass in the station list
			MapViewFragment mapViewFragment = (MapViewFragment) getSupportFragmentManager().findFragmentById(R.id.map_view_fragment);
 			mapViewFragment.updateStationList(stationList);
 			//(MapViewFragment) findViewById(R.id.map_view_fragment).updateStationList(stationList);
 		}
 		else{
			Log.d("DEBUG", "");
 		}
 	}
 
 
 }
