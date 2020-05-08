 package com.codepath.dealsapp;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.ActionBar.TabListener;
 import android.app.FragmentTransaction;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 import com.codepath.dealsapp.fragments.DealsListFragment;
 import com.codepath.dealsapp.fragments.DealsListFragment.OnDealSelectedListener;
 import com.codepath.dealsapp.fragments.DealsMapFragment;
 import com.nostra13.universalimageloader.core.DisplayImageOptions;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
 import com.nostra13.universalimageloader.core.assist.ImageScaleType;
 
 public class DealsHomeActivity extends FragmentActivity implements TabListener, OnDealSelectedListener {
 	public class DrawerItemClickListener implements OnItemClickListener {
 
 		@Override
 		public void onItemClick(AdapterView parent, View view, int position, long id) {
 	        Log.d("DEBUG", "category position:"+position);
 	        setCategoryID(position);
 	    }
 	}
 
 	EditText etLocation;
 	private String[] categories;
 	private DrawerLayout mDrawerLayout;
     private ListView mDrawerList;
     private ActionBarDrawerToggle mDrawerToggle;
     Tab tabList;
     Tab tabMap;
     int categoryID = 0;
     DealsListFragment listFragment;
     DealsMapFragment mapFragment;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Create global configuration and initialize ImageLoader with this
 		// configuration
 		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
 				.cacheInMemory().cacheOnDisc().imageScaleType(ImageScaleType.IN_SAMPLE_INT)
 				.showImageForEmptyUri(R.drawable.ic_launcher)
 			//	.displayer(new RoundedBitmapDisplayer(20))
 				.build();
 		
 		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
 				getApplicationContext()).defaultDisplayImageOptions(
 				defaultOptions).build();
 		ImageLoader.getInstance().init(config);
 
 		setContentView(R.layout.activity_deals_home);
 		setupNavigationTabs();
 		setupNavigationDrawer();
         
         StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
         StrictMode.setThreadPolicy(policy);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.deals_home, menu);
 		final MenuItem locationMenuItem = menu.findItem(R.id.action_location);
 		
 		etLocation = (EditText) locationMenuItem.getActionView().findViewById(R.id.etLocation);
 		
 		/** Setting an action listener */
 		etLocation.setOnEditorActionListener(new OnEditorActionListener() {
  
             @Override
             public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                 Toast.makeText(getBaseContext(), "Deals around Zip: " + v.getText(), Toast.LENGTH_SHORT).show();
                 locationMenuItem.collapseActionView();
                 return false;
             }
         });
 		
 	    return true;
 	}
 
 	private void setupNavigationDrawer() {
 		categories = getResources().getStringArray(R.array.categories_array);
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerList = (ListView) findViewById(R.id.left_drawer);
 
         // set a custom shadow that overlays the main content when the drawer opens
         mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
         // set up the drawer's list view with items and click listener
         mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                 R.layout.drawer_list_item, categories));
         mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
         mDrawerList.setItemChecked(0, true);
 
         // enable ActionBar app icon to behave as action to toggle nav drawer
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setDisplayShowHomeEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         // ActionBarDrawerToggle ties together the the proper interactions
         // between the sliding drawer and the action bar app icon
         mDrawerToggle = new ActionBarDrawerToggle(
                 this,                  /* host Activity */
                 mDrawerLayout,         /* DrawerLayout object */
                 R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                 R.string.drawer_open,  /* "open drawer" description for accessibility */
                 R.string.drawer_close  /* "close drawer" description for accessibility */
                 ) {
             public void onDrawerClosed(View view) {
               //  getActionBar().setTitle(mTitle);
               //  invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
 
             public void onDrawerOpened(View drawerView) {
               //  getActionBar().setTitle(mDrawerTitle);
                // invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
         };
         mDrawerLayout.setDrawerListener(mDrawerToggle);
         mDrawerToggle.syncState();
 	}
 
     /**
      * When using the ActionBarDrawerToggle, you must call it during
      * onPostCreate() and onConfigurationChanged()...
      */
	@Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         mDrawerToggle.syncState();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         // Pass any configuration change to the drawer toggls
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Pass the event to ActionBarDrawerToggle, if it returns
         // true, then it has handled the app icon touch event
         if (mDrawerToggle.onOptionsItemSelected(item)) {
           return true;
         }
         // Handle your other action bar items...
 
         return super.onOptionsItemSelected(item);
     }
 
 	private void setCategoryID(int position) {
 		mDrawerLayout.closeDrawer(mDrawerList);
 
 		if(categoryID != position) {
 			categoryID = position;
 			getActionBar().selectTab(tabList);
 			listFragment.loadDeals(categoryID);
 		}
     }
 	
 	private void setupNavigationTabs() {
 		ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		actionBar.setDisplayShowTitleEnabled(true);
 		tabList = actionBar.newTab().setText("List")
 				.setTag("DealsListFragment").setIcon(R.drawable.ic_action_list)
 				.setTabListener(this);
 
 		tabMap = actionBar.newTab().setText("Map")
 				.setTag("DealsMapFragment").setIcon(R.drawable.ic_action_map)
 				.setTabListener(this);
 
 		actionBar.addTab(tabList);
 		actionBar.addTab(tabMap);
 		actionBar.selectTab(tabList);
 		
 	}
 
 	@Override
 	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		Log.d("DEBUG", "tab selected:"+tab.getTag()+" categoryID:"+categoryID);
 		
 		FragmentManager manager = getSupportFragmentManager();
 		android.support.v4.app.FragmentTransaction fts = manager
 				.beginTransaction();
 
 		Bundle args = new Bundle();
         args.putInt("categoryID", categoryID);
         
 		if (tab.getTag() == "DealsListFragment") {
 			listFragment = new DealsListFragment();
 			listFragment.setArguments(args);
 			fts.replace(R.id.frame_container, listFragment);
 		} else {
 			mapFragment = new DealsMapFragment();
 			mapFragment.setArguments(args);
 			fts.replace(R.id.frame_container, mapFragment);
 		}
 
 		fts.commit();
 	}
 
 	@Override
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		Log.d("DEBUG", "tab unselected:"+tab.getTag());
 	}
 
 	@Override
 	public void onDealSelected(int position) {
 		Log.d("DEBUG", "onDealSelected:"+position);
 	}
 }
