 package com.mimee.ui;
 
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.mimee.R;
 
 public class MainActivity extends ActionBarActivity {
 	
 	private DrawerLayout mDrawerLayout;
 	private ListView mDrawer;
 	private ActionBarDrawerToggle mDrawerToggle;
 	private ActionBarHelper mActionBarHelper;
 	private String[] mDrawerTitles;
 	private int mCurrPosition;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 		mDrawer = (ListView) findViewById(R.id.left_drawer);
 		
 		mDrawerLayout.setDrawerListener(new DrawerListener());
 		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
 		
 		mDrawerTitles = getResources().getStringArray(R.array.nav_drawer_titles);
 		mDrawer.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mDrawerTitles)); //android.R.layout.simple_list_item_1
 		mDrawer.setOnItemClickListener(new DrawerItemClickListener());
 		
 		mActionBarHelper = new ActionBarHelper();
 		mActionBarHelper.init();
 		
 		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 
 				R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close);
 		
 		if(savedInstanceState == null) {
 			// select first item
 			selectItem(0, null);
 		}
 	}
 	
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		
 		// Sync the toggle state after onRestoreInstanceState has occurred.
         	mDrawerToggle.syncState();
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		/*
 		 * The action bar home/up action should open or close the drawer.
 		 * mDrawerToggle will take care of this.
 		 */
 		if (mDrawerToggle.onOptionsItemSelected(item)) {
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		mDrawerToggle.onConfigurationChanged(newConfig);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	/**
 	 * select a drawer item
 	 * @param position
 	 * @param text
 	 */
 	private void selectItem(int position, String text) {
 //		// reset actionBar
 //		mActionBarHelper.init();
 		
 		Fragment fragment = getReplaceFragment(position, text);
 		if (fragment != null) {
 			FragmentManager fragmentManager = getSupportFragmentManager();
 			fragmentManager.beginTransaction()
 					.replace(R.id.content_frame, fragment).commit();
 		}
 
 		mDrawer.setItemChecked(position, true);
 		mActionBarHelper.setTitle(mDrawerTitles[position]);
 		mDrawerLayout.closeDrawer(mDrawer);
 	}
 	
 	private Fragment getReplaceFragment(int position, CharSequence name) {
 		Fragment f = null;
 		switch (position) {
 		case 0:
 			f = new IndexFragment2();
//			f = new IndexFragment();
 			break;
 
 		default:
 			f = StubFragment.newInstance(name.toString());
 			break;
 		}
 		
 		return f;
 	}
 	
 	
 	/**
 	 * This list item click listener implements very simple view switching by
 	 * changing the primary content text. The drawer is closed when a selection
 	 * is made.
 	 */
 	private class DrawerItemClickListener implements ListView.OnItemClickListener {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			if(mCurrPosition == position) {
 				mDrawerLayout.closeDrawer(mDrawer);
 			} else {
 				mCurrPosition = position;
 				CharSequence name = ((TextView) view).getText();
 				selectItem(position, name.toString());
 			}
 		}
 	}
     
 	/**
      * A drawer listener can be used to respond to drawer events such as becoming
      * fully opened or closed. You should always prefer to perform expensive operations
      * such as drastic relayout when no animation is currently in progress, either before
      * or after the drawer animates.
      *
      * When using ActionBarDrawerToggle, all DrawerLayout listener methods should be forwarded
      * if the ActionBarDrawerToggle is not used as the DrawerLayout listener directly.
      */
 	private class DrawerListener implements DrawerLayout.DrawerListener {
 
 		@Override
 		public void onDrawerClosed(View drawerView) {
 			mDrawerToggle.onDrawerClosed(drawerView);
 			mActionBarHelper.onDrawerClosed();
 		}
 
 		@Override
 		public void onDrawerOpened(View drawerView) {
 			mDrawerToggle.onDrawerOpened(drawerView);
 			mActionBarHelper.onDrawerOpened();
 		}
 
 		@Override
 		public void onDrawerSlide(View drawerView, float slideOffset) {
 			mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
 		}
 
 		@Override
 		public void onDrawerStateChanged(int newState) {
 			mDrawerToggle.onDrawerStateChanged(newState);
 		}
 	}
 	
 	
 	/**
      * Action bar helper for use on ICS and newer devices.
      */
     private class ActionBarHelper {
         private final ActionBar mActionBar;
         private CharSequence mDrawerTitle;
         private CharSequence mTitle;
 
         ActionBarHelper() {
             mActionBar = getSupportActionBar();
         }
 
 		public void init() {
 //			mActionBar.setDisplayHomeAsUpEnabled(true);
 //			mActionBar.setHomeButtonEnabled(true);
 			mTitle = mDrawerTitle = getTitle();
 			
 			int flag = ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP;
 			mActionBar.setDisplayOptions(flag, flag);
 			mActionBar.removeAllTabs();
 			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
 		}
 
         /**
          * When the drawer is closed we restore the action bar state reflecting
          * the specific contents in view.
          */
         public void onDrawerClosed() {
             mActionBar.setTitle(mTitle);
         }
 
         /**
          * When the drawer is open we set the action bar to a generic title.
          * The action bar should only contain data relevant at the top level of
          * the nav hierarchy represented by the drawer, as the rest of your content
          * will be dimmed down and non-interactive.
          */
         public void onDrawerOpened() {
             mActionBar.setTitle(mDrawerTitle);
         }
 
         public void setTitle(CharSequence title) {
             mTitle = title;
         }
     }
 
 }
