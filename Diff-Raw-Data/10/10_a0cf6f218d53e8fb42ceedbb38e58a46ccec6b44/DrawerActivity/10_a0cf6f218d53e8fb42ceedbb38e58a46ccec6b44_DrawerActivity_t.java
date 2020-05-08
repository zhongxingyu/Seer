 package com.example.gauge;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.widget.DrawerLayout;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import com.example.gauge.R;
 
 public class DrawerActivity extends Activity {
 
     private DrawerLayout mDrawerLayout;
     private ListView drawerList;
     private ListView adminDrawerList;
     private ActionBarDrawerToggle mDrawerToggle;
 
     private CharSequence mTitle;
     private String[] menuTitles;
     private String[] adminMenuTitles;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_drawer);
         
         mTitle = getTitle();
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         menuTitles = getResources().getStringArray(R.array.menu_array);
         adminMenuTitles = getResources().getStringArray(R.array.second_menu_array);
         drawerList = (ListView) findViewById(R.id.left_drawer);
         adminDrawerList = (ListView) findViewById(R.id.left_drawer_admin);
 
         // enable ActionBar app icon to behave as action to toggle nav drawer
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         mDrawerToggle = new ActionBarDrawerToggle(
                 this,
                 mDrawerLayout,
                 R.drawable.ic_drawer,
                 R.string.drawer_open,
                 R.string.drawer_close  
                 ) {
             public void onDrawerClosed(View view) {
                 getActionBar().setTitle(mTitle);
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
 
             public void onDrawerOpened(View drawerView) {
                 getActionBar().setTitle(R.string.app_name);
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
         };
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 
         if (savedInstanceState == null) {
             selectItem(0);
         }
     }
     
     public void buildSideNavigation(int layoutResId) {
     	setContentView(layoutResId);
         generateLists();
     }
 
 	public void buildSideNavigation(View resultsView) {
     	setContentView(resultsView);
         generateLists();		
 	}
 
 	private void generateLists() {
 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         drawerList = (ListView) findViewById(R.id.left_drawer);
         drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, menuTitles));
         drawerList.setOnItemClickListener(new DrawerItemClickListener());
         
 
         adminDrawerList = (ListView) findViewById(R.id.left_drawer_admin);
         adminDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.admin_drawer_list_item, adminMenuTitles));
         //adminDrawerList.setOnItemClickListener(new DrawerItemClickListener());
                 
         mDrawerToggle = new ActionBarDrawerToggle(
                 this,
                 mDrawerLayout,
                 R.drawable.ic_drawer,
                 R.string.drawer_open,
                 R.string.drawer_close
                 );
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 	}
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
          // The action bar home/up action should open or close the drawer.
          // ActionBarDrawerToggle will take care of this.
         if (mDrawerToggle.onOptionsItemSelected(item)) {
             return true;
         }
         // Handle action buttons
         return super.onOptionsItemSelected(item);
         
     }
 
     public class DrawerItemClickListener implements ListView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             selectItem(position);
         }
     }
 
     private void selectItem(int position) {
     	Intent intent;
     	
     	switch(position) {
 			case 1: {
 				intent = new Intent(DrawerActivity.this, CalculateActivity.class);
 				startActivity(intent);
 				break; }
     		case 2: {
     			intent = new Intent(DrawerActivity.this, CompareActivity.class);
     			startActivity(intent);
     			break; }
     		case 3: {
    			intent = new Intent(DrawerActivity.this, MainActivity.class);
     			startActivity(intent);
     			break; }
     	}
     	
         drawerList.setItemChecked(position, true);
         //setTitle(menuTitles[position]);
         mDrawerLayout.closeDrawers();
     }
 
     @Override
     public void setTitle(CharSequence title) {
         mTitle = title;
         getActionBar().setTitle(mTitle);
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
         // Pass any configuration change to the drawer toggles
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
 
 	public void handleResponse(GaugeHttpResponse results) { }
 }
