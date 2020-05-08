 package com.doLast.doGRT;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.view.ViewConfiguration;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockDialogFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.doLast.doGRT.database.DatabaseSchema.StopTimesColumns;
 import com.doLast.doGRT.database.DatabaseSchema.UserBusStopsColumns;
 
 public class RoutesActivity extends SherlockFragmentActivity {
 	// For choosing between different view from other activities
 	public static final String STOP_ID = "stop_id";
 	public static final String STOP_NAME = "stop_name";
 	public static final String STOP_TITLE = "stop_title";
 	public static final String SCHEDULE_TYPE = "schedule_type";
 	public static final int SCHEDULE_MIXED = 0;
 	public static final int SCHEDULE_SELECT = 1;
 	public static final int SCHEDULE_SINGLE = 2;
 	public static final int NUM_TABS = 2;
 			
 	// Stop id, name and title
 	private String stop_id = null;
 	private String stop_name = null;
 	private String stop_title = null;
 	
 	// Tab listener
 	private TabListener<ScheduleListFragment> tab_listener = null;		
 	
 	// Option menu
 	private Menu option_menu = null;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);   
         
         /* If no data is given in the Intent that started this Activity, then this Activity
          * was started when the intent filter matched a MAIN action. We should use the default
          * provider URI.
          */
         // Gets the intent that started this Activity.
         Intent intent = getIntent();
 
         // If there is no data associated with the Intent, sets the data to the default URI, which
         // accesses a list of notes.
         if (intent.getData() == null) {
             intent.setData(StopTimesColumns.CONTENT_URI);
         }
         
         // Set content view and find list view
         setContentView(R.layout.schedule);                
         
         // Retrieve extra data
         Bundle extras = intent.getExtras();
         if (extras != null) {
         	stop_id = extras.getString(STOP_ID);
         	stop_name = extras.getString(STOP_NAME);
         	stop_title = extras.getString(STOP_TITLE);
         }                
         
         // Use the "navigate up" button
         ActionBar action_bar = getSupportActionBar();
         action_bar.setDisplayHomeAsUpEnabled(true);
         action_bar.setTitle(stop_title);
         action_bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         
        // Setup tab swipe(view pager)
         
         
        // Setup the tabs
        tab_listener = new TabListener<ScheduleListFragment>(this, "Tab Listener", ScheduleListFragment.class);
         // Check if tabs are already created
         if (action_bar.getTabCount() == 0) {
         	// Mixed schedule tab
 	        Tab tab = action_bar.newTab()
 	        		.setText(R.string.mixed_schedule)
 	        		.setTag(SCHEDULE_MIXED)
 	        		.setTabListener(tab_listener);
 	        action_bar.addTab(tab); 
 	        // Route selection tab
 	        tab = action_bar.newTab()
 	        		.setText(R.string.route_select)
 	        		.setTag(SCHEDULE_SELECT)
 	        		.setTabListener(tab_listener);
 	        action_bar.addTab(tab);
         }
         
         // Forcing the overflow menu (3 dots menu)
         try {
             ViewConfiguration config = ViewConfiguration.get(this);
             java.lang.reflect.Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
             if(menuKeyField != null) {
                 menuKeyField.setAccessible(true);
                 menuKeyField.setBoolean(config, false);
             }
         } catch (Exception ex) {
             // Ignore
         }        
    }	

 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
         // Check if stop_id already exist, do not display add button
 		if (stop_id != null) {
 	        String[] projection = { UserBusStopsColumns.USER_ID };
 	        String selection = UserBusStopsColumns.STOP_ID + " = " + stop_id;
 	        Cursor user = managedQuery(UserBusStopsColumns.CONTENT_URI, projection, selection, null, null);
 	        if (user.getCount() > 0) {
 	        	menu.removeItem(R.id.add_option);
 	        } else {
 	        	menu.removeItem(R.id.delete_option);
 	        }
 			//user.close();
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Always create a new menu
 		menu.clear();
 		new MenuInflater(this).inflate(R.menu.schedule_option_menu, menu);
 		option_menu = menu;
 		
 		return super.onCreateOptionsMenu(menu);
 	}	
 	
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 		Intent main_intent = new Intent(this, MainActivity.class);
         switch (item.getItemId()) {
         case R.id.add_option:        	
 			// Switch to main activity
 			// Pack the stop id and name with the intent
 			main_intent.putExtra(MainActivity.ADD_STOP, stop_id);
 			main_intent.putExtra(MainActivity.ADD_STOP_NAME, stop_name); 
         case android.R.id.home:
         	main_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         	startActivity(main_intent);
         	return true;
         case R.id.locate:
         	Intent map_intent = new Intent(this, GMapsActivity.class);
         	map_intent.putExtra(GMapsActivity.LOCATE, stop_id);
         	map_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         	startActivity(map_intent);            
         	return true;
         case R.id.delete_option:
             SherlockDialogFragment newFragment = MyDialogFragment.newInstance(MyDialogFragment.DELETE_DIALOG_ID, stop_id, stop_title);
             newFragment.show(getSupportFragmentManager(), String.valueOf(MyDialogFragment.DELETE_DIALOG_ID));
         	return true;
         }
  
         return super.onOptionsItemSelected(item);
     }        
     
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		updateOptionMenu();
 		super.onConfigurationChanged(newConfig);
 	}
 
 	@Override
 	public void onBackPressed() {
 		int type = (Integer)getSupportActionBar().getSelectedTab().getTag();
 		if (!tab_listener.isSingleRouteDisplayed(type)) {
 			super.onBackPressed();
 		}
 	}
 	
 	public void updateOptionMenu() {
 		onCreateOptionsMenu(option_menu);
 		onPrepareOptionsMenu(option_menu);
 	}
 
 	public String getStopId() { return stop_id; }
 	public String getStopName() { return stop_name; }
 	public String getStopTitle() { return stop_title; }	
 
     private class TabListener<T extends SherlockListFragment> implements ActionBar.TabListener {
         private ScheduleListFragment ScheduleFragments[];
         private final SherlockFragmentActivity mActivity;
         private final String mTag;
         private final Class<T> mClass;
     	
         /** Constructor used each time a new tab is created.
          * @param activity  The host Activity, used to instantiate the fragment
          * @param tag  The identifier tag for the fragment
          * @param clz  The fragment's Class, used to instantiate the fragment
          */
         public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz) {
         	mActivity = activity;
         	mTag = tag;
         	mClass = clz;
         	ScheduleFragments = new ScheduleListFragment[NUM_TABS];
         }
     	
 		@Override
 		public void onTabSelected(Tab tab, FragmentTransaction ft) {
 			// TODO Auto-generated method stub
 			int type = (Integer)tab.getTag();
 			// Check if the fragment exist
 			if (ScheduleFragments[type] == null) {
 				// If not, create and add it
 				ScheduleFragments[type] = ScheduleListFragment.newInstance(type);
				ft.add(android.R.id.content, ScheduleFragments[type], mTag);
 			} else {
 				// or just simply attach it
 				ft.attach(ScheduleFragments[type]);
 			}			
 		}
 	
 		@Override
 		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 			int type = (Integer)tab.getTag();	
 			ft.detach(ScheduleFragments[type]);
 		}
 	
 		@Override
 		public void onTabReselected(Tab tab, FragmentTransaction ft) {
 			// Nothing to do here
 		}
 		
 		/**
 		 * Ask if the a single route is being displayed
 		 * If yes, back press is changed to display routes
 		 * 
 		 * */
 		private boolean isSingleRouteDisplayed(int type) {
 			boolean single_route = ScheduleFragments[type].isSingleRouteDisplayed();
 			if (single_route) ScheduleFragments[type].backKeyPressed();
 			
 			return single_route;
 		}
     }	
 }
