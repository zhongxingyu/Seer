 package edu.uiuc.whosinline;
 
 import edu.uiuc.whosinline.fragments.FavoriteFragment;
 import edu.uiuc.whosinline.fragments.NearbyFragment;
 import edu.uiuc.whosinline.fragments.RecentFragment;
 import edu.uiuc.whosinline.listeners.HomeTabsListener;
 
 import android.os.Bundle;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.view.Menu;
 
 public class HomeActivity extends Activity {
 
 	private FragmentManager fragmentManager;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		fragmentManager = getFragmentManager();
 		
 		// set up the action bar with tabs
 		setUpActionBar();
 		
 		// set the view for this activity
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	// setUpActionBar()
 	// Description: sets up the action bar for this activity by creating three tabs
 	// Parameters: none
 	// Returns: nothing
 	private void setUpActionBar(){
		
 		// get rid of the icon and app title in the action bar
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayShowHomeEnabled(false);
 		actionBar.setDisplayShowTitleEnabled(false);
 		
 		// set up tabs
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		
 		// initiate the three tabs
 		ActionBar.Tab nearbyTab = actionBar.newTab().setText("Nearby");
 		nearbyTab.setIcon(R.drawable.ic_action_tab_nea);
 		ActionBar.Tab recentTab = actionBar.newTab().setText("Recent");
 		recentTab.setIcon(R.drawable.ic_action_tab_rec);
 		ActionBar.Tab favoriteTab = actionBar.newTab().setText("Favorite");
 		favoriteTab.setIcon(R.drawable.ic_action_tab_fav);
 
 		// create fragments to display each tab
 		Fragment nearbyFragment = new NearbyFragment();
 		Fragment recentFragment = new RecentFragment();
 		Fragment favoriteFragment = new FavoriteFragment();
 		
 		// set the tab listeners to listen for clicks
 		nearbyTab.setTabListener(new HomeTabsListener(fragmentManager,
 				nearbyFragment, R.id.fragment_container));
 		recentTab.setTabListener(new HomeTabsListener(fragmentManager,
 				recentFragment, R.id.fragment_container));
 		favoriteTab.setTabListener(new HomeTabsListener(fragmentManager,
 				favoriteFragment, R.id.fragment_container));
 		
 		// add tabs to the action bar
 		actionBar.addTab(nearbyTab);
 		actionBar.addTab(recentTab);
 		actionBar.addTab(favoriteTab);
 	}
 }
