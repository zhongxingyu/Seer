 package com.pawhub.lostandfound;
 
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
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
 import android.view.View.OnClickListener;
 import android.widget.TableRow;
 
 public class Home extends ActionBarActivity {
 
 	private DrawerLayout mDrawerLayout;
 	private ActionBarDrawerToggle mDrawerToggle;
 
 	//Change the numbers to the actions name like btn_alerts
 	private TableRow btn_1;
 	private TableRow btn_2;
 	private TableRow btn_3;
 	private TableRow btn_4;
 	private TableRow btn_5;
 	private TableRow btn_6; 
 	private TableRow btn_7;
 
 	private final int SCREEN_1 = 0;
 	private final int SCREEN_2 = 1;
 	private final int SCREEN_3 = 2;
 	private final int SCREEN_4 = 3;
 	private final int SCREEN_5 = 4;
 	private final int SCREEN_6 = 5;
 	private final int SCREEN_7 = 6;
 
 	private int CURRENT_SCREEN = 0;
 	private String currentTitle;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_home);
         
         initSlidingMenu();
         initViews();
         showScreen(0);
     }
 
     private void initSlidingMenu() {
     	
     	final ActionBar actionBar=getSupportActionBar();
 
 		currentTitle = "";
 
 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,GravityCompat.START);
 	
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setHomeButtonEnabled(true);
 		actionBar.setIcon(R.drawable.app_name2);
 		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#B71C4E")));
 		actionBar.setTitle(currentTitle);
 
 		mDrawerToggle = new ActionBarDrawerToggle(this, 
 		mDrawerLayout,R.drawable.ic_drawer,R.string.drawer_open, R.string.drawer_close
 		) {
 			public void onDrawerClosed(View view) {
 				actionBar.setTitle(currentTitle);
 				supportInvalidateOptionsMenu();
 			}
 
 			public void onDrawerOpened(View drawerView) {
 				actionBar.setTitle(currentTitle);
 				supportInvalidateOptionsMenu();
 			}
 		};
 		mDrawerLayout.setDrawerListener(mDrawerToggle);
 	}
     
     private void initViews() {
 		btn_1 = (TableRow) findViewById(R.id.entry_1);
 		btn_2 = (TableRow) findViewById(R.id.entry_2);
 		btn_3 = (TableRow) findViewById(R.id.entry_3);
 		btn_4 = (TableRow) findViewById(R.id.entry_4);
 		btn_5 = (TableRow) findViewById(R.id.entry_5);
 		btn_6 = (TableRow) findViewById(R.id.entry_6);
 		btn_7 = (TableRow) findViewById(R.id.entry_7);
 
 		MenuListener listener = new MenuListener();
 		btn_1.setOnClickListener(listener);
 		btn_2.setOnClickListener(listener);
 		btn_3.setOnClickListener(listener);
 		btn_4.setOnClickListener(listener);
 		btn_5.setOnClickListener(listener);
 		btn_6.setOnClickListener(listener);
 		btn_7.setOnClickListener(listener);
 	}
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.home, menu);
         return true;
     }
     
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (mDrawerToggle.onOptionsItemSelected(item)) {
 			return true;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		mDrawerToggle.syncState();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		mDrawerToggle.onConfigurationChanged(newConfig);
 	}
     
     private void showScreen(int id) {
 
 		Fragment fragment = null;
 
 		switch (id) {
 		case R.id.entry_1:
 			fragment = new CasesListFragment();
 			currentTitle="";
 			CURRENT_SCREEN = SCREEN_1;
 			break;
 		case R.id.entry_2:
			fragment = new CasesListFragment();
 			CURRENT_SCREEN = SCREEN_2;
 			break;
 		case R.id.entry_3:
			fragment = new CasesListFragment();
 			CURRENT_SCREEN = SCREEN_3;
 			break;
 		case R.id.entry_4:
			fragment = new CasesListFragment();
 			CURRENT_SCREEN = SCREEN_4;
 			break;
 		case R.id.entry_5:
			fragment = new CasesListFragment();
 			CURRENT_SCREEN = SCREEN_5;
 			break;
 		case R.id.entry_6:
			fragment = new CasesListFragment();	
 			CURRENT_SCREEN = SCREEN_6;
 			break;
 		case R.id.entry_7:
			fragment = new FragmentCasesMap();
 			CURRENT_SCREEN = SCREEN_7;
 			break;
 		default:
 			fragment = new CasesListFragment();
 			currentTitle="";
 			CURRENT_SCREEN = SCREEN_1;
 			break;
 		}
 
 		FragmentManager fragmentManager = getSupportFragmentManager();
 		fragmentManager.beginTransaction()
 				.replace(R.id.content_frame, fragment).commit();
 		mDrawerLayout.closeDrawers();
 	}
     
     @Override
 	public void onBackPressed() {
 		if (CURRENT_SCREEN == SCREEN_1)
 			finish();
 		else
 			showScreen(R.id.entry_1);
 	}
     
     private class MenuListener implements OnClickListener {
 		@Override
 		public void onClick(View v) {
 			int id = v.getId();
 			showScreen(id);
 		}
 	}
     
 }
