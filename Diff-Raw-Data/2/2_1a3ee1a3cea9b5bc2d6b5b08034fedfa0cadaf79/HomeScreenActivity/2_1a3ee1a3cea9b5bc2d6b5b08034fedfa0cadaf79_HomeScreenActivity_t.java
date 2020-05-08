 package com.chaseit.activities;
 
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.widget.DrawerLayout;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBar.Tab;
 import android.support.v7.app.ActionBar.TabListener;
 import android.support.v7.app.ActionBarActivity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 
 import com.chaseit.R;
 import com.chaseit.fragments.MyHuntsFragment;
 import com.chaseit.fragments.NewsFeedFragment;
 import com.chaseit.fragments.RecentHuntsFragment;
 
 public class HomeScreenActivity extends ActionBarActivity implements TabListener {
 
 	// Views related to Navigation Drawer
 	private DrawerLayout mDrawerLayout;
     private ListView mDrawerList;
     private ActionBarDrawerToggle mDrawerToggle;
     private CharSequence mDrawerTitle;
     private CharSequence mTitle;
     
     /* Activity Lifecycle */
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_news_feed);
 		setupNavigationDrawer();
 		setupNavigationTabs();
 	}
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         mDrawerToggle.syncState();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
 
     /* ActionBar Options Menu */
     
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		// If the nav drawer is open, hide action items related to the content view
         boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.create_hunt).setVisible(!drawerOpen);
         return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 
       // Pass the event to ActionBarDrawerToggle, if it returns
       // true, then it has handled the app icon touch event
       if (mDrawerToggle.onOptionsItemSelected(item)) {
         return true;
       }
       // Handle your other action bar items...
 
       return super.onOptionsItemSelected(item);
 	}
 
 	/* Private Methods */
 	
 	private void setupNavigationDrawer() {
 		
 		mTitle = mDrawerTitle = getTitle();
 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerList = (ListView) findViewById(R.id.left_drawer);
         mDrawerToggle = new ActionBarDrawerToggle(
                 this,                  /* host Activity */
                 mDrawerLayout,         /* DrawerLayout object */
                 R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                 R.string.drawer_open,  /* "open drawer" description */
                 R.string.drawer_close  /* "close drawer" description */
                 ) {
 
             /** Called when a drawer has settled in a completely closed state. */
             public void onDrawerClosed(View view) {
                 getActionBar().setTitle(mTitle);
                 invalidateOptionsMenu();
             }
 
             /** Called when a drawer has settled in a completely open state. */
             public void onDrawerOpened(View drawerView) {
                 getActionBar().setTitle(mDrawerTitle);
                 invalidateOptionsMenu();
             }
         };
 
         // Set the drawer toggle as the DrawerListener
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         getSupportActionBar().setHomeButtonEnabled(true);
         
         // TODO: add stuff in the navigation drawer
 	}
 
 	private void setupNavigationTabs() {
 		ActionBar actionBar = getSupportActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		
 		Tab tabHome = actionBar.newTab().setText("All Hunts")
 				.setTag("HomeTimelineFragment").setTabListener(this);
 
 		Tab tabRecent = actionBar.newTab().setText("Recent")
 				.setTag("RecentFragment").setTabListener(this);
 		Tab tabMine = actionBar.newTab().setText("Mine")
 				.setTag("MyHuntsFragment").setTabListener(this);
 
 		actionBar.addTab(tabHome);
 		actionBar.addTab(tabMine);
 		actionBar.addTab(tabRecent);
 		actionBar.selectTab(tabHome);
 	}
 
 	private void onProfileSelected(View v){
 		Intent i = new Intent(HomeScreenActivity.this, UserDetailsActivity.class);
 		startActivity(i);
 	}
 	
 	/* TabListener Methods */
 
 	@Override
 	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {	
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		if (tab.getTag() == "HomeTimelineFragment") {
 			ft.replace(R.id.flNewsFeedFragment, new NewsFeedFragment());
 		} else if (tab.getTag() == "RecentHuntsFragment") {
 			ft.replace(R.id.flNewsFeedFragment, new RecentHuntsFragment());
 		} else {
 			ft.replace(R.id.flNewsFeedFragment, new MyHuntsFragment());
 		}
 	}
 
 	@Override
 	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {	
 	}
 }
