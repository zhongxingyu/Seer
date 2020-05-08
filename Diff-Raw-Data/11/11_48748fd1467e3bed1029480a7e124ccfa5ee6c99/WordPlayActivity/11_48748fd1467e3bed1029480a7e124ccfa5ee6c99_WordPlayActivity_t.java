 package com.ppp.wordplayadvlib.activities;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.Fragment;
 import android.support.v4.widget.DrawerLayout;
 import android.support.v7.app.ActionBar;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.ppp.wordplayadvlib.Constants;
 import com.ppp.wordplayadvlib.R;
 import com.ppp.wordplayadvlib.WordPlayApp;
 import com.ppp.wordplayadvlib.appdata.History;
 import com.ppp.wordplayadvlib.appdata.JudgeHistory;
 import com.ppp.wordplayadvlib.database.WordlistDatabase;
 import com.ppp.wordplayadvlib.database.schema.DatabaseInfo;
 import com.ppp.wordplayadvlib.dialogs.AppErrDialog;
 import com.ppp.wordplayadvlib.fragments.WebViewFragment;
 import com.ppp.wordplayadvlib.fragments.dialog.DbInstallDialog;
 import com.ppp.wordplayadvlib.fragments.dialog.DbInstallDialog.DbInstallDialogListener;
 import com.ppp.wordplayadvlib.fragments.dialog.FreeDialog;
 import com.ppp.wordplayadvlib.fragments.dialog.FreeDialog.FreeDialogListener;
 import com.ppp.wordplayadvlib.fragments.hosts.AboutHostFragment;
 import com.ppp.wordplayadvlib.fragments.hosts.AnagramsHostFragment;
 import com.ppp.wordplayadvlib.fragments.hosts.CrosswordsHostFragment;
 import com.ppp.wordplayadvlib.fragments.hosts.DictionaryHostFragment;
 import com.ppp.wordplayadvlib.fragments.hosts.HistoryHostFragment;
 import com.ppp.wordplayadvlib.fragments.hosts.HostFragment;
 import com.ppp.wordplayadvlib.fragments.hosts.ThesaurusHostFragment;
 import com.ppp.wordplayadvlib.fragments.hosts.WordJudgeHostFragment;
 import com.ppp.wordplayadvlib.utils.Debug;
 import com.ppp.wordplayadvlib.utils.Utils;
 
 @SuppressLint("ValidFragment")
 public class WordPlayActivity extends HostActivity
 	implements
 		OnItemClickListener,
 		DbInstallDialogListener,
 		FreeDialogListener
 {
 
 	private static final int RestartNotificationId = 1;
 
 	private static final int UserPrefsActivity = 1;
 
     private DrawerLayout menuDrawer;
     private ActionBarDrawerToggle drawerToggle;
 	private ListView menuListView;
 	private MenuAdapter menuAdapter;
 
     private String lastItemTitle = null;
     private boolean drawerSeen = false;
     private List<DrawerMenuItem> menuItems;
 
 	private static boolean notificationIconEnabled = false;
 
 	//
 	// Activity Methods
 	//
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 
 		super.onCreate(savedInstanceState);
 
 	    setContentView(R.layout.menu_drawer);
 
 	    ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBar.setDisplayShowTitleEnabled(true);
         actionBar.setTitle(getString(R.string.app_name));
 
         menuItems = new ArrayList<DrawerMenuItem>(5);
         DrawerMenuItem anagramsItem =
         	new DrawerMenuItem(getString(R.string.Anagrams), R.drawable.ic_tab_anagrams, AnagramsHostFragment.class);
         menuItems.add(anagramsItem);
         menuItems.add(new DrawerMenuItem(getString(R.string.WordJudge), R.drawable.ic_tab_wordjudge, WordJudgeHostFragment.class));
         menuItems.add(new DrawerMenuItem(getString(R.string.Dictionary), R.drawable.ic_tab_dictionary, DictionaryHostFragment.class));
         menuItems.add(new DrawerMenuItem(getString(R.string.Thesaurus), R.drawable.ic_tab_thesaurus, ThesaurusHostFragment.class));
         menuItems.add(new DrawerMenuItem(getString(R.string.Crosswords), R.drawable.ic_tab_crosswords, CrosswordsHostFragment.class));
         menuItems.add(new DrawerMenuItem(Constants.BLANK, 0, null));
         menuItems.add(new DrawerMenuItem(getString(R.string.showhistory_menu_str), 0, HistoryHostFragment.class));
         menuItems.add(new DrawerMenuItem(getString(R.string.showabout_menu_str), 0, AboutHostFragment.class));
 
         // Clear and load history
         History.getInstance().loadHistory(this);
         JudgeHistory.getInstance().loadJudgeHistory(this);
 
         // Create and show the initial fragment or the last
         // fragment seen
         if (savedInstanceState == null)  {
             lastItemTitle = anagramsItem.title;
             replaceStack(getInitialFragment(), true);
         }
         else
             lastItemTitle = savedInstanceState.getString("lastItemTitle");
  
         // Get the DrawerLayout
         menuDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
         menuDrawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);
 
         // Get the ListView for the DrawerLayout and populate it with
         // the adapter of DrawerMenuItems
         menuListView = (ListView) findViewById(R.id.left_drawer);
         menuAdapter = new MenuAdapter(menuItems);
         menuListView.setAdapter(menuAdapter);
         menuListView.setScrollingCacheEnabled(false);
         menuListView.setOnItemClickListener(this);
 
         // Create the drawer toggle so that we can do special shit
         // like show the normal drawer icon instead of the ActionBar
         // back icon
         drawerToggle = new ActionBarDrawerToggle(this, menuDrawer, R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {      
 
             public void onDrawerOpened(View drawerView) {}
             
             public void onDrawerClosed(View drawerView)
             {
                 if (!drawerSeen)  {
                     drawerSeen = true;
                     SharedPreferences.Editor edit = getPreferences(Context.MODE_PRIVATE).edit();
                     edit.putBoolean("drawerSeen", true);
                     edit.commit();
                 }
             }
 
         };
         menuDrawer.setDrawerListener(drawerToggle);
 
         // Initialize the drawer seen state
         drawerSeen = getPreferences(Context.MODE_PRIVATE).getBoolean("drawerSeen", false);
 
         // Initialize the notification icon
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		notificationIconEnabled = prefs.getBoolean("notification_bar", false);
 		if (notificationIconEnabled)  {
 	    	Intent intent = new Intent(this, getClass());
 	    	addRestartNotification(intent);
 		}
 		else
 			removeNotification();
 
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState)
     {
         super.onPostCreate(savedInstanceState);
         drawerToggle.syncState();
     }
     
     @Override
     protected void onResume()
     {
 
         super.onResume();
 
         refreshHomeIcon();
 
         // We want to show the drawer after we've shown all of
         // the startup dialogs so quit early if we showed any dialog
         if (initUi())
         	return;
 
         // If the drawer has not been seen, show it
         if (!drawerSeen)
         	showDrawer();
 //            menuDrawer.postDelayed(new Runnable() {
 //                @Override
 //                public void run() { menuDrawer.openDrawer(menuListView); }
 //            }, 500);
 
     }
 
     @Override
 	public void onStop()
     {
 
     	super.onStop();
 
     	History.getInstance().saveHistory(this);
     	JudgeHistory.getInstance().saveJudgeHistory(this);
 
     }
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState)
 	{
 		super.onSaveInstanceState(savedInstanceState);
 		savedInstanceState.putBoolean("drawerSeen", drawerSeen);
 		savedInstanceState.putString("lastItemTitle", lastItemTitle);
 	}
 
     @Override
     public void onBackPressed()
     {
 
         if (menuDrawer.isDrawerOpen(menuListView))
             menuDrawer.closeDrawer(menuListView);
         else
             super.onBackPressed();
         
         refreshHomeIcon();
 
     }
 
 	@Override
     public void onActivityResult(int requestCode, int resultCode, Intent data)
     {
 
     	switch (requestCode)  {
 		
 	    	case UserPrefsActivity:
 	
 	    		// We've returned from setting preferences.  Apply the only one we
 	    		// know about now by adding or removing the notification icon.
 	    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 	        	boolean newNotificationSetting = prefs.getBoolean("notification_bar", false);
 	        	if (newNotificationSetting != notificationIconEnabled)  {
 	        		if (newNotificationSetting)  {
 	        	    	Intent intent = new Intent(this, getClass());
 	        	    	addRestartNotification(intent);
 	        		}
 	        		else
 	        			removeNotification();
 	        		notificationIconEnabled = newNotificationSetting;
 	        	}
 	        	break;
 
     	}
     	
     }
 
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position,	long id)
     {
 
         DrawerMenuItem it = (DrawerMenuItem) parent.getAdapter().getItem(position);
 
         if (it.itemClass != null)  {
 	        lastItemTitle = it.title;
 	        parent.setSelection(position);
 	        switchToFragment(it.itemClass, true);
 	        menuListView.setItemChecked(position, true);
 	        menuDrawer.closeDrawer(menuListView);
 			getSupportActionBar().setSubtitle(it.title);
         }
 
     }
 
 	private boolean initUi() 
 	{
 
 		// App may have been killed while in the background.
         if ((lastAddedTag != null) && (lastAdded == null))
         	lastAdded = getSupportFragmentManager().findFragmentByTag(lastAddedTag);
 		
         // As a last-resort, default to Browse.
 		if (lastAdded == null)
 			switchToFragment(AnagramsHostFragment.class, true);
 
         // Set the subtitle to the currently selected tab item
 		getSupportActionBar().setSubtitle(lastItemTitle);
 
 		// If this the free version, check to see if we need to show
 		// the free dialog
 		if (WordPlayApp.getInstance().isFreeMode())
 			if (showFreeDialog())
 				return true;
 
 		// Create the database
 		return createOrUpgradeDatabase();
 
 	}
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
     	getMenuInflater().inflate(R.menu.search_menu, menu);
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu)
     {
 
     	MenuItem item = null;
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
     	// Close the drawer
     	if (isDrawerOpen())
     		menuDrawer.closeDrawer(menuListView);
 
     	// If there is no help, don't show it
     	if (lastAdded != null)  {
         	HostFragment host = (HostFragment) lastAdded;
        	if (host.getFragmentHelp() == 0)  {
        		item = menu.findItem(R.id.showhelp_menu);
        		if (item != null)
        			menu.findItem(R.id.showhelp_menu).setVisible(false);
        	}
     	}
 
     	// Let child fragments turn on the "Clear History" item
     	item = menu.findItem(R.id.clearhistory_menu);
    	if (item != null)
    		item.setVisible(false);
 
     	// If the notification bar is turned off, don't show "Exit"
     	item = menu.findItem(R.id.exit_menu);
     	if (item != null)
     		item.setVisible(prefs.getBoolean("notification_bar", false));
 
     	return super.onPrepareOptionsMenu(menu);
 
     }
 
     @Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 
     	switch (item.getItemId())  {
 
 			// Respond to the action bar's Up/Home button
 		    case android.R.id.home:
 
 		    	if (isDrawerOpen() && !drawerToggle.isDrawerIndicatorEnabled())
 		    		menuDrawer.closeDrawer(menuListView);
 		    	
 		    	else if (!drawerToggle.isDrawerIndicatorEnabled()) 
 		    		if (lastAdded.getChildFragmentManager() != null)
 		    			popBackStackPlus(lastAdded);
 		    	
 		    	refreshHomeIcon();
 
 		}
 
         // Menu drawer
     	if (drawerToggle.onOptionsItemSelected(item))
             return true;
     	
 		// Help
 		else if (item.getItemId() == R.id.showhelp_menu)
 			showHelp();
 
 		// Preferences
     	else if (item.getItemId() == R.id.settings_menu)  {
 			Intent intent = new Intent(this, UserPreferenceActivity.class);
 			try {
 				startActivityForResult(intent, UserPrefsActivity);
 			}
 			catch (Exception e) {
 				Debug.e("User Prefs Startup Failed: " + e);
 			}
 		}
     	
 		// Reinstall Dictionary
 		else if (item.getItemId() == R.id.dictionary_reinstall_menu)
 			startDatabaseInstall();
 	
 		// Exit
 		else if (item.getItemId() == R.id.exit_menu)  {
 			removeNotification();
 			finish();
 		}
 		
 		return super.onOptionsItemSelected(item);
 		
 	}
 
     //
     // Menu Helpers
     //
 
     private void showHelp()
     {
 
     	if (lastAdded == null)
     		return;
 
     	HostFragment host = (HostFragment) lastAdded;
     	int helpId = host.getFragmentHelp();
     	if (helpId != 0)  {
 
     		Bundle args = new Bundle();
     		args.putString("content", Utils.getHelpText(this, "Release Notes", helpId));
 
     		WebViewFragment fragment = new WebViewFragment();
     		fragment.setArguments(args);
     		host.pushToStack(fragment);
 
     	}
 
     }
 
     //
     // Dialogs
     //
 
     private void showInstallDbDialog(boolean upgrade)
     {
 
     	DbInstallDialog dbInstallDialog;
 
     	// If we are already showing the dialog, don't do so again
     	dbInstallDialog = (DbInstallDialog) getSupportFragmentManager().findFragmentByTag("InstallDbDialog");
     	if (dbInstallDialog != null)
     		return;
 
     	// Create and show it
     	dbInstallDialog = DbInstallDialog.newInstance(upgrade);
 		dbInstallDialog.show(getSupportFragmentManager(), "InstallDbDialog");
 
     }
 
     private boolean showFreeDialog()
     {
 
     	FreeDialog freeDialog;
 
     	// If we are already showing the diaog, don't do so again
     	freeDialog = (FreeDialog) getSupportFragmentManager().findFragmentByTag("FreeDialog");
     	if (freeDialog != null)
     		return true;
 
     	// Check the preference that tells us we've shown it before
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	boolean hasShown = prefs.getBoolean("freeDialogShown", false);
     	if (hasShown)
     		return false;
 
     	// Create and show it
     	freeDialog = FreeDialog.newInstance();
     	freeDialog.show(getSupportFragmentManager(), "FreeDialog");
 
     	return true;
 
     }
 
     //
     // Free Dialog
     //
 
 	@Override
 	public void onFreeDialogDimissed()
 	{
 
 		// Update the preferences to indicate we've show the dialog
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		SharedPreferences.Editor edit = prefs.edit();
 		edit.putBoolean("freeDialogShown", true);
 		edit.commit();
 
 		// Create or upgrade the database
 		if (createOrUpgradeDatabase())
 			return;
 
 		// All dialogs have been shown so we can now show
 		// the drawer if it hasn't been seen before
 		if (!drawerSeen)
 			showDrawer();
 
 	}
 
     //
     // Database Installation
     //
 
     @Override
     public void startDatabaseInstall()
     {
 		new DatabaseWaitTask(this).execute();    	
     }
 
     private boolean createOrUpgradeDatabase()
     {
 
     	WordlistDatabase db =
     		(WordlistDatabase) new WordlistDatabase(this).openReadOnly();
 
     	// If the database is old or missing, the version will be -1
     	int dbVersion = db.getDatabaseVersion();
 		if (dbVersion == DatabaseInfo.INVALID_DB_VERSION)  {
 			Debug.e("bad db version " + dbVersion);
 			showInstallDbDialog(false);
 			return true;
 		}
 		else if (dbVersion != DatabaseInfo.CURRENT_DB_VERSION)  {
 			Debug.e("old db version " + dbVersion);
 			showInstallDbDialog(true);
 			return true;
 		}
 
 		db.close();
 
 		return false;
 
     }
 
     private class DatabaseWaitTask extends AsyncTask<Void, Void, Void> {
 
     	private Context context = null;
 
     	private Exception exception = null;
     	private ProgressDialog progressDialog = null;
 
     	public DatabaseWaitTask(Context ctx)
     	{
     		context = ctx;
     	}
 
     	protected void onPreExecute()
     	{
 
     		if (!isFinishing())  {
     			progressDialog = new ProgressDialog(context);
     			String installLocStr = WordlistDatabase.dbInstallsOnExternalStorage() ?
 						getString(R.string.dictionary_on_external_storage) :
 						getString(R.string.dictionary_on_internal_storage);
 				String message = String.format(getString(R.string.dictionary_progress_dialog_text), installLocStr);
 				progressDialog.setMessage(message);
     			progressDialog.setCancelable(false);
     			progressDialog.show();
     		}
 
     	}
 
 		@Override
 		protected Void doInBackground(Void... params)
 		{
 			try {
 				WordlistDatabase.deleteDatabaseFile(WordPlayActivity.this);
 				WordlistDatabase.createDatabaseFile(WordPlayActivity.this);
 			}
 			catch (Exception e) { exception = e; }
 			return null;
 		}
 
 		protected void onPostExecute(Void result)
 		{
 
 			// Dismiss the dialog
 			if (progressDialog != null)
 				if (!isFinishing())
 					progressDialog.dismiss();
 
 			// If there was an exception during install,
 			// report it
 			if (exception != null)
 				createDatabaseExceptionDialog(context, exception);
 
 			// If we haven't already shown the drawer, show it now
 			if (!drawerSeen)
 				showDrawer();
 
 		}
 
     }
 
     private void createDatabaseExceptionDialog(Context context, Exception exception)
     {
 
 		StringBuilder builder = new StringBuilder();
 
 		File file = Environment.getDataDirectory();
 		builder.append(file.getPath());
 		builder.append(" ");
 		builder.append(Utils.getFreeSpaceForFile(context, file));
 		builder.append("\n");
 
 		file = Environment.getExternalStorageDirectory();
 		builder.append(file.getPath());
 		builder.append(" ");
 		builder.append(Utils.getFreeSpaceForFile(context, file));
 		builder.append("\n");
 
 		builder.append("Installs on " +
 						(WordlistDatabase.dbInstallsOnExternalStorage() ?
 								getString(R.string.dictionary_on_external_storage) :
 									getString(R.string.dictionary_on_internal_storage)) + " storage");
 		builder.append("\n");
 
 		if (!isFinishing())
 			new AppErrDialog(context, exception, builder.toString()).show();
 
     }
 
     //
     // Notification Bar Icon Support
     //
 
     public void addRestartNotification(Intent startIntent)
     {
 
     	int icon = 0;
     	Activity activity = this;
     	NotificationManager manager = (NotificationManager)activity.getSystemService(Context.NOTIFICATION_SERVICE);
 
     	// Get the notification parameters
     	if (WordPlayApp.getInstance().isFreeMode())
 			icon = R.drawable.ic_launcher_wordplay_assistant_free;
 		else
 			icon = R.drawable.ic_launcher_wordplay_assistant;
 		String tickerText = getString(R.string.notify_ticker_text);
 		String title = getString(R.string.app_name);
 		String content = getString(R.string.notify_description);
 
 		// Set the flags required to restart the app in the Intent we
 		// received
 		startIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 
 		// Create the PendingIntent that is fired when the notification is selected
 		// by the user
 		PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, startIntent, 0);
 
 		// Create the notification
 		Notification notification = new Notification(icon, tickerText, System.currentTimeMillis());
 		notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;   
 		notification.setLatestEventInfo(activity, title, content, pendingIntent);
 
 		// Give it to the notification manager for display
 		manager.notify(RestartNotificationId, notification);
 
     }
 
     private void removeNotification()
     {
     	NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
     	manager.cancel(RestartNotificationId);
     }
 
     //
     // Tab Support Classes
     //
 
     private static final class DrawerMenuItem {
 
         public String title;
         public int iconResource;
         public Class<?> itemClass;
 
         DrawerMenuItem(String title, int iconResource, Class<?> itemClass)
         {
             this.title = title;
             this.iconResource = iconResource;
             this.itemClass = itemClass;
         }
  
     }
 
     private DrawerMenuItem findDrawerItemByTag(String tag)
     {
         for (DrawerMenuItem item : menuItems)
             if (item.itemClass != null && tag.equals(item.itemClass.getName()))
                 return item;
         return null;
     }
 
     private DrawerMenuItem findDrawerItemByTitle(String title)
     {
         for (DrawerMenuItem item : menuItems)
             if (title.equals(item.title))
                 return item;
         return null;
     }
 
     private DrawerMenuItem findDrawerItemByClass(Class<?> cls)
     {
     	for (DrawerMenuItem item : menuItems)
     		if (cls.equals(item.itemClass))
     			return item;
     	return null;
     }
 
 	@Override
 	public boolean isDrawerOpen()
 	{
 		if ((menuDrawer != null) && (menuListView != null))
 			return menuDrawer.isDrawerOpen(menuListView);
 		return false;
 	}
 	
 	public void closeDrawer()
 	{
 		if ((menuDrawer != null) && (menuListView != null))
 			menuDrawer.closeDrawer(menuListView);
 	}
 
 	private void showDrawer()
 	{
         menuDrawer.postDelayed(new Runnable() {
             @Override
             public void run()
             {
                 menuDrawer.openDrawer(menuListView);   
             }
         }, 500);
 	}
 
     private class MenuAdapter extends BaseAdapter {
 
         private List<DrawerMenuItem> items;
 
         public MenuAdapter(List<DrawerMenuItem> items)
         {
             this.items = items;
         }
 
         @Override
         public int getCount()
         {
             if (items != null)
                 return items.size();
             return 0;
         }
 
         @Override
         public Object getItem(int position) { return items.get(position); }
 
         @Override
         public long getItemId(int position) { return position; }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent)
         {
 
             DrawerMenuItem item = items.get(position);
 
             if (convertView == null)
                 convertView = getLayoutInflater().inflate(R.layout.menu_row_item, null);
 
             TextView tv = (TextView) convertView.findViewById(R.id.title);
             ImageView iv = (ImageView) convertView.findViewById(R.id.icon);
 
             tv.setText(item.title);
             if (item.iconResource != 0)  {
                 iv.setVisibility(View.VISIBLE);
                 iv.setImageResource(item.iconResource);
             }
             else
                 iv.setVisibility(View.INVISIBLE);
 
             return convertView;
 
         }
 
     }
 
     //
     // Fragment Management
     //
 
     protected int getFragmentContainer() { return R.id.content; }
 
     protected Fragment getInitialFragment() { return new AnagramsHostFragment(); }
     
     public ActionBarDrawerToggle getDrawerToggle() { return drawerToggle; }
     public DrawerLayout getDrawerLayout() { return menuDrawer; }
     
     //
     // Home Icon
     //
 
     private void refreshHomeIcon()
     {
     	menuDrawer.postDelayed(new Runnable() {
             @Override
             public void run()
             {
             	if (lastAdded != null) {
                 	if (lastAdded.getChildFragmentManager().getBackStackEntryCount() > 0) 
                 		drawerToggle.setDrawerIndicatorEnabled(false);
                 	else 
                 		drawerToggle.setDrawerIndicatorEnabled(true);
                 }
             }
         }, 500);
     }
 
 }
