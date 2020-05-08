 package ch.unibe.ese.shopnote.activities;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Locale;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.ActionMode;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 import ch.unibe.ese.shopnote.R;
 import ch.unibe.ese.shopnote.adapters.ShoppingListAdapter;
 import ch.unibe.ese.shopnote.core.AlarmReceiver;
 import ch.unibe.ese.shopnote.core.BaseActivity;
 import ch.unibe.ese.shopnote.core.ListManager;
 import ch.unibe.ese.shopnote.core.ShoppingList;
 import ch.unibe.ese.shopnote.share.SyncManager;
 
 /**
  *	The main activity of the app, which displays an overview of the shopping lists
  */
 public class HomeActivity extends BaseActivity {
 
 	private ListManager listmanager;
 	private SyncManager syncmanager;
 	private List<ShoppingList> shoppingLists;
 	private List<ShoppingList> shoppingListsNotArchived;
 	private ShoppingListAdapter shoppingListAdapter;
 	private Activity homeActivity = this;
 	private PendingIntent pendingIntent;
 
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
         //create xml-settings and set values
         PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
         SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
         updateLanguage(sharedPrefs);
         
         //set chosen color theme
 		setContentView(R.layout.activity_home);
 		RelativeLayout rl = (RelativeLayout) findViewById(R.id.RelativeLayoutHome);
 		RelativeLayout rlDrawer = (RelativeLayout) findViewById(R.id.drawer_Linearlayout);
 //		ListView lv = (ListView) findViewById(R.id.left_drawer);
 		updateTheme(rl, getActionBar(), rlDrawer);
 
 		
 		// Create drawer menu
 		createDrawerMenu();
 		createDrawerToggle();
 		
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         //create Managers for local lists and synch lists
 		listmanager = getListManager();
 		syncmanager = getSyncManager();
 		getFriendsManager();
 		updateAdapter();	
 	}
 	
 	private void updateAdapter() {
 		// Get List from manager
 		shoppingLists = listmanager.getShoppingLists();
 		shoppingListsNotArchived = new ArrayList<ShoppingList>();
 		
 		// separate archived lists
 		for (ShoppingList list: shoppingLists)
 			if (!list.isArchived())
 				shoppingListsNotArchived.add(list);
 
 		shoppingListAdapter = new ShoppingListAdapter(this,
 				R.layout.shopping_list_item, shoppingListsNotArchived);
 		
 		// calculate boughtItems/totalItems count
 		calculateItemCount(shoppingListsNotArchived, shoppingListAdapter);
 
 		ListView listView = (ListView) findViewById(R.id.ShoppingListView);
 		listView.setAdapter(shoppingListAdapter);
 		updateThemeListView(listView);
 		
 		addListener(listView);
 		
		setDueDateNotifications();
 		
 		// hide welcome message
 		RelativeLayout welcome = (RelativeLayout) findViewById(R.id.home_welcome);
 		if (shoppingListAdapter.isEmpty())
 			welcome.setVisibility(View.VISIBLE);
 		else
 			welcome.setVisibility(View.GONE);
 	}
 
 	private void addListener(ListView listView) {
 		// Add long click Listener
 		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			// TODO: Merge those 2 Listeners together, because we dont need two
 			// seperate listeners...
 			@Override
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				ShoppingList selectedList = shoppingListAdapter.getItem(arg2);
 				HomeActivity.this.startActionMode(new ShoppingListActionMode(
 						HomeActivity.this.listmanager, selectedList,
 						HomeActivity.this.shoppingListAdapter,
 						HomeActivity.this));
 				return true;
 			}
 		});
 
 		// Add click Listener
 		listView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				Intent intent = new Intent(homeActivity, ViewListActivity.class);
 				
 				// get position in listmanager
 				ShoppingList list = shoppingListsNotArchived.get(position);
 				
 				intent.putExtra(EXTRAS_LIST_ID, list.getId());
 				homeActivity.startActivity(intent);
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.home, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
         // Pass the event to ActionBarDrawerToggle, if it returns
         // true, then it has handled the app icon touch event
         if (drawerToggle.onOptionsItemSelected(item))
         	return true;
                 
 		// Handle presses on the action bar items
 		switch (item.getItemId()) {
 
 			case R.id.action_refresh:
 				if(isOnline()) {
 					syncmanager.synchronise(this);
 					Toast.makeText(this, this.getString(R.string.synchronizing),
 							Toast.LENGTH_SHORT).show();
 				} else {
 					Toast.makeText(this, this.getString(R.string.no_connection),
 							Toast.LENGTH_SHORT).show();
 				}
 				return true;
 			case R.id.action_new:
 				Intent intent = new Intent(this, CreateListActivity.class);
 				this.startActivity(intent);
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	@Override
 	public void onResume(){
     	super.onResume();
     	updateAdapter();
     }
 	
 	@Override
 	public void onActionModeFinished(ActionMode mode) {
 		super.onActionModeFinished(mode);
 		updateAdapter();
 	}
 
 	@Override
 	public void refresh() {
 		updateAdapter();
 	}
 	
 	@Override
 	public boolean onKeyDown(int keycode, KeyEvent e) {
 	    switch(keycode) {
 	        case KeyEvent.KEYCODE_MENU:
 	        	// TODO: open drawer
 	            //drawMenu.openDrawer(drawMenu);
 	            return true;
 	    }
 
 	    return super.onKeyDown(keycode, e);
 	}
 	
 	/**
 	 * Sets the language which is chosen by the user in the xml.preferences file
 	 * @param sharedPreferences
 	 */
 	private void updateLanguage(SharedPreferences sharedPreferences) {
 		String newLanguage = sharedPreferences.getString("language", null);
 		Configuration config = new Configuration();
 		String languages[] = getResources().getStringArray(R.array.choosable_languages_keys);
 		
 		Locale locale;
 
 		if(newLanguage.equals(languages[0]))
 			locale = Locale.ENGLISH; 
 		else if(newLanguage.equals(languages[1])) 
 			locale = Locale.GERMANY;
 		else if(newLanguage.equals(languages[2])) 
 			locale = Locale.FRANCE;
 		
 		// detect system language if not set by user
 		else
 			locale = Locale.getDefault();
 			
 		Locale.setDefault(locale);
 		config.locale = locale;
 		getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
 	}
 	
 	private void setDueDateNotifications() {
 		
 		shoppingLists = listmanager.getShoppingLists();
 		Intent myIntent = new Intent(HomeActivity.this, AlarmReceiver.class);
 		pendingIntent = PendingIntent.getBroadcast(HomeActivity.this, 0, myIntent,0);
 		AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
 		
 		
 		for( ShoppingList shoppingList: shoppingLists){
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(shoppingList.getDueDate());
 			// time of notification on dueDate
 			cal.set(Calendar.HOUR_OF_DAY, 9);
 			cal.set(Calendar.MINUTE, 0);
 			cal.set(Calendar.SECOND, 0);
 			cal.set(Calendar.AM_PM,Calendar.AM);
 			
 			alarmManager.set(AlarmManager.RTC, cal.getTimeInMillis(), pendingIntent);
 		}
 		
 	 
 	  
 	  }
 	
 }
