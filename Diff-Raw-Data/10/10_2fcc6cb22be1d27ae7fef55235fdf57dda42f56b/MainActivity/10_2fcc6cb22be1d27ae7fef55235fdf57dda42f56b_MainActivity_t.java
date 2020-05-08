 package com.uauker.apps.tremrio.activities;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.support.v7.app.ActionBarActivity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.uauker.apps.tremrio.R;
 import com.uauker.apps.tremrio.fragments.MetroMapFragment;
 import com.uauker.apps.tremrio.fragments.RamalFragment;
 import com.uauker.apps.tremrio.fragments.TelephoneFragment;
 import com.uauker.apps.tremrio.fragments.TicketFragment;
 import com.uauker.apps.tremrio.fragments.TrainMapFragment;
 import com.uauker.apps.tremrio.helpers.ConfigHelper;
 import com.uauker.apps.tremrio.helpers.SharedPreferencesHelper;
 
 @SuppressLint("NewApi")
 public class MainActivity extends ActionBarActivity {
 
 	private DrawerLayout mDrawerLayout;
 	private ListView mDrawerList;
 	private ActionBarDrawerToggle mDrawerToggle;
 
 	private String[] menuNames;
 
 	public final static String SELECTED_MENU_ROW = "selectedMenuRow";
 
 	public SharedPreferencesHelper sharedPreferences;
 
 	public Fragment[] fragments = { new RamalFragment(), new TicketFragment(),
 			new TrainMapFragment(), new MetroMapFragment(),
 			new TelephoneFragment() };
 
 	@SuppressLint("ResourceAsColor")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		this.setupBugSense();
 
 		setContentView(R.layout.activity_main);
 
 		menuNames = getResources().getStringArray(R.array.array_menu);
 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layoutt);
 		mDrawerList = (ListView) findViewById(R.id.left_drawer);
 
 		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
 				GravityCompat.START);
 
 		MenuRowAdapter adapter = new MenuRowAdapter(this);
 
 		for (String name : this.menuNames) {
 			adapter.add(name);
 		}
 
 		mDrawerList.setAdapter(adapter);
 
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		getSupportActionBar().setHomeButtonEnabled(true);
 		getSupportActionBar().setBackgroundDrawable(
 				new ColorDrawable(0xfff06f2f));
 
 		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
 				R.drawable.ic_drawer, R.string.drawer_open,
 				R.string.drawer_close);
 
 		mDrawerLayout.setDrawerListener(mDrawerToggle);
 
 		if (savedInstanceState == null) {
 			switchContentByPosition(0);
 
 			cleanMenuSelectedItem();
 		}
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		@SuppressWarnings("unused")
 		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public void finish() {
 		super.finish();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (mDrawerToggle.onOptionsItemSelected(item)) {
 			return true;
 		}
 
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			super.onBackPressed();
 			return true;
 		case R.id.menu_settings:
 			Intent intent = new Intent(this, SettingsActivity.class);
 			startActivity(intent);
 			return true;
 		}
 
 		return super.onOptionsItemSelected(item);
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
 		// Pass any configuration change to the drawer toggls
 		mDrawerToggle.onConfigurationChanged(newConfig);
 	}
 
 	@SuppressWarnings("unused")
 	private class DrawerItemClickListener implements
 			ListView.OnItemClickListener {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			switchContentByPosition(position);
 		}
 	}
 
 	private void switchContentByPosition(int position) {
 		Fragment fragment = getFragmentByPosition(position);
 
 		FragmentManager fragmentManager = getSupportFragmentManager();
 		fragmentManager.beginTransaction()
 				.replace(R.id.content_frame, fragment).commit();
 
 		mDrawerList.setItemChecked(position, true);
 		mDrawerLayout.closeDrawer(mDrawerList);
 	}
 
 	private void cleanMenuSelectedItem() {
 		sharedPreferences = SharedPreferencesHelper
 				.getInstance(getApplicationContext());
 
 		sharedPreferences.setString(SELECTED_MENU_ROW, null);
 	}
 
 	private Fragment getFragmentByPosition(int position) {
 		return this.fragments[position];
 	}
 
 	private void setupBugSense() {
 		if (ConfigHelper.isProduction) {
 			BugSenseHandler.initAndStartSession(MainActivity.this, "d0325253");
 		}
 	}
 
 	public class MenuRowAdapter extends ArrayAdapter<String> {
 
 		public MenuRowAdapter(Context context) {
 			super(context, 0);
 		}
 
 		public View getView(final int position, View contentView,
 				ViewGroup parent) {
 			if (contentView == null) {
 				ViewGroup noRootView = null;
 				contentView = LayoutInflater.from(getContext()).inflate(
 						R.layout.item_list_menu, noRootView);
 			}
 
			sharedPreferences = SharedPreferencesHelper
					.getInstance(getApplicationContext());

 			String selectedRowName = sharedPreferences.getString(
 					SELECTED_MENU_ROW, menuNames[0]);
 
 			String menuItem = menuNames[position];
 
 			TextView menuTitle = (TextView) contentView
 					.findViewById(R.id.item_list_menu_name);
 			menuTitle.setText(menuItem);
 
 			TextView menuSelectedIndicator = (TextView) contentView
 					.findViewById(R.id.item_list_menu_selected_indicator);
 
 			if (menuItem.equalsIgnoreCase(selectedRowName)) {
 				contentView.setBackgroundColor(getResources().getColor(
 						R.color.menu_selected_cell));
 				menuSelectedIndicator.setVisibility(View.VISIBLE);
 				menuTitle.setTextColor(getResources().getColor(R.color.white));
 			} else {
 				contentView.setBackgroundColor(getResources().getColor(
 						R.color.white));
 				menuSelectedIndicator.setVisibility(View.GONE);
 				menuTitle.setTextColor(getResources().getColor(
 						R.color.menu_text_color_unselected));
 			}
 
 			contentView.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 
 					sharedPreferences.setString(SELECTED_MENU_ROW,
 							menuNames[position]);
 
 					switchContentByPosition(position);
 
 					notifyDataSetChanged();
 				}
 			});
 
 			return contentView;
 		}
 
 	}
 }
