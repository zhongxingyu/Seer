 package de.dhbw.container;
 
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.MenuInflater;
 import de.dhbw.achievement.AchievementFragment;
 import de.dhbw.contents.LiveTrackingFragment;
 import de.dhbw.contents.TotalEvaluationFragment;
 
 public class MenuContainerActivity extends SherlockFragmentActivity {
 
 
 	DrawerLayout mDrawerLayout;
 	ListView mDrawerList;
 	ActionBarDrawerToggle mDrawerToggle;
 	SherlockFragment tracking_from_menu;
 
 	// private CharSequence mDrawerTitle;
 	// private CharSequence mTitle;
 	private String[] mNavigationTitles;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.menu_container);
 
 		mNavigationTitles = getResources().getStringArray(
 				R.array.navigation_array);
 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 		mDrawerList = (ListView) findViewById(R.id.left_drawer);
 
 		getSupportActionBar().setTitle(R.string.app_name);
 
 		// set a custom shadow that overlays the main content when the drawer
 		// opens
 		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
 				GravityCompat.START);
 		// set up the drawer's list view with items and click listener
 		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
 				R.layout.menu_list_item, mNavigationTitles));
 		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
 		// enable ActionBar app icon to behave as action to toggle nav drawer
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		getSupportActionBar().setHomeButtonEnabled(true);
 
 		// ActionBarDrawerToggle ties together the the proper interactions
 		// between the sliding drawer and the action bar app icon
 		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
 		mDrawerLayout, /* DrawerLayout object */
 		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
 		R.string.drawer_open, /* "open drawer" description for accessibility */
 		R.string.drawer_close /* "close drawer" description for accessibility */
 		) {
 			public void onDrawerClosed(View view) {
 				// getSupportActionBar().setTitle(mTitle);
 				invalidateOptionsMenu(); // creates call to
 											// onPrepareOptionsMenu()
 			}
 
 			public void onDrawerOpened(View drawerView) {
 				// getSupportActionBar().setTitle(mDrawerTitle);
 				invalidateOptionsMenu(); // creates call to
 											// onPrepareOptionsMenu()
 			}
 		};
 		mDrawerLayout.setDrawerListener(mDrawerToggle);
 
 		if (savedInstanceState == null) {
 			selectItem(0);
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.container, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(
 			com.actionbarsherlock.view.MenuItem item) {
 
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			if (mDrawerLayout.isDrawerOpen(mDrawerList))
 				mDrawerLayout.closeDrawer(mDrawerList);
 			else
 				mDrawerLayout.openDrawer(mDrawerList);
 			break;
 
 		case R.id.action_music_player: // Call default music player
 
 			/*
 			 * Deprecated code for music player call Intent intent = new
 			 * Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
 			 * startActivity(intent);
 			 */
 
 			Intent intent = new Intent();
 			intent.setAction("android.intent.action.MAIN");
 			intent.addCategory("android.intent.category.APP_MUSIC");
 			startActivity(intent);
 			break;
 
 		case R.id.action_achievements:
 			getSupportFragmentManager().beginTransaction()
 					.replace(R.id.currentFragment, new AchievementFragment())
 					.commit();
 			break;
 
 		default:
 			;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	// The click listener for ListView in the navigation drawer
 	private class DrawerItemClickListener implements
 			ListView.OnItemClickListener {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			selectItem(position);
 		}
 	}
 	
 	@Override
     public void onBackPressed() {
 		((LiveTrackingFragment) tracking_from_menu).onBackPressed();
     	//super.onBackPressed();
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
 
 	private void selectItem(int position) {
 
 		switch (position) {
 
 		case 0:
 			tracking_from_menu = new LiveTrackingFragment();
 
 			if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
 				getSupportFragmentManager().popBackStack();
 			}
 			getSupportFragmentManager().beginTransaction()
 					.replace(R.id.currentFragment, tracking_from_menu).commit();
 			break;
 		case 1:
 			getSupportFragmentManager()
 					.beginTransaction()
 					.replace(R.id.currentFragment,
 							new TotalEvaluationFragment()).commit();
 			break;
 		default:
 			SherlockFragment tracking_at_launch = new LiveTrackingFragment();
 			getSupportFragmentManager().beginTransaction()
 					.add(R.id.currentFragment, tracking_at_launch).commit();
 		}
 
 		mDrawerLayout.closeDrawer(mDrawerList);
 	}
 
 }
