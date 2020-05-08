 package org.fruct.oss.tourme;
 
 import android.app.ActionBar;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewSwitcher.ViewFactory;
 
 
 public class MainActivity extends FragmentActivity implements
 		ActionBar.OnNavigationListener, ViewFactory {
 
 	public static Context context = null;
 	
 	private ListView drawer;
 	private ListView drawerService;
	private ArrayAdapter<String> drawerServiceAdapter;
 	ActionBarDrawerToggle drawerToggle;
 	DrawerLayout drawerLayout;
 	
 	SharedPreferences sh;
 	SharedPreferences.Editor ed;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_fragment_container);
 		context = getApplicationContext();
 		
         if (findViewById(R.id.fragment_container) != null) {
 
             if (savedInstanceState != null) {
                 return;
             }
 
             // Home screen fragment
             HomeFragment firstFragment = new HomeFragment();
 
             // In case this activity was started with special instructions from an Intent,
             // pass the Intent's extras to the fragment as arguments
             //firstFragment.setArguments(getIntent().getExtras());
 
             // Add the fragment to the 'fragment_container' FrameLayout
             getFragmentManager().beginTransaction()
                     .add(R.id.fragment_container, firstFragment).commit();
         }
 		
 		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 		drawerToggle = new ActionBarDrawerToggle (this,	drawerLayout, R.drawable.ic_drawer,
 				R.string.drawer_open, R.string.drawer_close) {
 			// Do nothing with title
 		};
 		
 		drawerLayout.setDrawerListener(drawerToggle);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		getActionBar().setHomeButtonEnabled(true);
 
 		// Fill in the drawer with string array
 		drawer = (ListView) findViewById(R.id.left_drawer_list);
 		drawerService = (ListView) findViewById(R.id.left_drawer_list_service);
 
 		String[] drawerItems = getResources().getStringArray(R.array.drawer_items);
 		String[] drawerItemsService = getResources().getStringArray(R.array.drawer_items_service);
 		
 		drawer.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerItems));
		drawerServiceAdapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item_service, drawerItemsService);
		drawerService.setAdapter(drawerServiceAdapter);
 		
 		drawer.setOnItemClickListener(new ListView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				drawerItemSwitch(position, view);
 			}
 		});
 		
 		drawerService.setOnItemClickListener(new ListView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				ListView lv = (ListView) parent;
 				lv.setItemChecked(position, false);
				drawerItemSwitch(position + 100, view);
 			}
 		});
 
 		
 		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);		
 		
 		sh = getSharedPreferences(ConstantsAndTools.SHARED_PREFERENCES, 0);
 		
 		
 		// Good practice to show drawer at first launch
 		Boolean firstLaunch = sh.getBoolean(ConstantsAndTools.IS_FIRST_LAUNCH, true);
 		if (firstLaunch) {
 			drawerLayout.openDrawer(Gravity.LEFT);
 			ed = sh.edit();
 			//ed.putBoolean(ConstantsAndTools.IS_FIRST_LAUNCH, false); // TODO
 			ed.commit();
 			
 			// At first launch, show welcome screen\prepare mode
 			Intent i = new Intent (this, PrepareActivity.class);
 			startActivity(i);
 		}
 	}
 
 	
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         drawerToggle.syncState(); // FIXME app crashes when orientation changes
     }
     
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         drawerToggle.onConfigurationChanged(newConfig);
     }
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		//getMenuInflater().inflate(R.menu.activity_main, menu);		
 		//return super.onCreateOptionsMenu(menu);
 		
 		return false;
 	}
 
 	/**
 	 * Replace fragment in this main activity by drawer position
 	 * @author alexander
 	 * @param id - number in drawer (clicked)
 	 * 
 	 */
	public void drawerItemSwitch(int id, View view) {
 		FragmentManager fm = null;
 		Fragment f = null;
 		FragmentTransaction ft = null;
 		Intent i = null;
 		
 		// I know, it's kinda bicycle, but I dunno how to do better
 		switch(id) {
 			// Goto Main
 			case(0):
 				f = new HomeFragment();
 				break;
 			case(1):
 				Toast.makeText(context, "I told you to FIX that!..", Toast.LENGTH_SHORT).show(); // TODO
 				break;
 			case(2):
 				f = new NearbyFragment();
 				break;
 			case(3):
 				f = new MapFragment();							
 				break;
 			case(4):
 				Toast.makeText(context, "Opening practical info...", Toast.LENGTH_SHORT).show(); // TODO
 				break;
 			case(5):
 				f = new FavouritesFragment();
 				break;
 			case(6):
 				f = new TravellogFragment();
 				break;
 				
 			// Bottom drawer's ListView handler	
 			case(100):
 				Toast.makeText(context, "Going to plan new trip...", Toast.LENGTH_SHORT).show(); // TODO
 				break;
 			case(101):
 				ListView lv = (ListView) findViewById(R.id.left_drawer_list_service);
				TextView netMode = (TextView) view;

 				// TODO: somebody fix this please (doesn't work)
 				ed = sh.edit();
 				
 				if (sh.getBoolean("ONLINE_MODE", false) == true) {
 					ed.putBoolean(ConstantsAndTools.ONLINE_MODE, false);
 					netMode.setText(getResources().getString(R.string.map_menu_on));
					drawerService.invalidateViews();
 					Log.e("123", netMode.getText()+"");
					drawerService.invalidateViews();
 				} else {
 					ed.putBoolean(ConstantsAndTools.ONLINE_MODE, true);		
 					netMode.setText(getResources().getString(R.string.map_menu_off));
					drawerService.invalidateViews();
 					Log.e("123", netMode.getText()+"");
					drawerService.invalidateViews();
 				} // TODO: if map fragment, do something				
				
 				ed.commit();
 				
 				break;			
 			case(102):
 				i = new Intent(this, SettingsActivity.class);
 			default:
 				break;
 		}
 		
 		if (f != null) {
 			fm = getFragmentManager();
 			ft = fm.beginTransaction();
 			ft.replace(R.id.fragment_container, f);
 			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 			ft.commit();
 			drawerLayout.closeDrawers();
 			return;
 		}
 		
 		if (i != null) {
 			startActivity(i);
 		}
 		
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		if (drawerToggle.onOptionsItemSelected(item)) {
 	          return true;
 		}
 		
 /*		Intent i = null;
 		
 		switch(item.getItemId()) {
 			case(R.id.menu_settings):
 				i = new Intent(this, SettingsActivity.class);
 				break;
 			case (R.id.add_data):
 				Toast.makeText(context, "Nothing implemented yet", Toast.LENGTH_SHORT).show(); // TODO
 				break;
 			case(R.id.menu_onoff_online_mode):
 				// Turn on\off online mode (save to shared preferences)
 				// Check for current state and update
 				ed = sh.edit();
 				if (sh.getBoolean("ONLINE_MODE", false) == true)		
 					ed.putBoolean(ConstantsAndTools.ONLINE_MODE, false);
 				else
 					ed.putBoolean(ConstantsAndTools.ONLINE_MODE, true);
 				
 				ed.commit();
 				break;
 			default:
 				break;
 		}
 		
 		if (i != null)
 			startActivity(i);*/
 
 		return super.onOptionsItemSelected(item);		
 		
 	}
 
 	
 	// ViewSwitcher
 	@Override
 	public View makeView() {
 
 		return null;
 	}
 
 	// No actionbar
 	@Override
 	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
 		return false;
 	}
 
 
 }
