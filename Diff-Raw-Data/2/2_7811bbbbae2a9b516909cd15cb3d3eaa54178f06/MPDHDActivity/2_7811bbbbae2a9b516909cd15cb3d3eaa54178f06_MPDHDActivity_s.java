 package com.blklb.mpdhd;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.Fragment;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 import com.blklb.mpdhd.R;
 import com.blklb.mpdhd.fragments.DatabaseFragmentTab;
 import com.blklb.mpdhd.fragments.NowPlayingFragmentTab;
 import com.blklb.mpdhd.fragments.PlaylistsFragmentTab;
 import com.blklb.mpdhd.fragments.QueueFragmentTab;
 import com.blklb.mpdhd.fragments.SearchFragmentTab;
 import com.blklb.mpdhd.tasks.NetworkAndUITask;
 import com.blklb.mpdhd.tools.JMPDHelper2;
 import com.blklb.mpdhd.tools.TimerHelper;
 
 public class MPDHDActivity extends Activity {
 
 	//private final String tag = "MPDHDActivity";
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		setupTabbedActionBar();
 
 		// This forces landscape. It will still re orient but not in portrait.
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
 
 		// SharedPreferences prefs =
 		// PreferenceManager.getDefaultSharedPreferences(this);
 		PreferenceManager.setDefaultValues(this, R.xml.prefrences, false);
 
 		// Schedule network tasks to start running
 		TimerHelper.getInstance().scheduleTask(new NetworkAndUITask(this), 100);
 	}
 
 	/**
 	 * Called when the user clicks the device's Menu button the first time for
 	 * this Activity. Android passes in a Menu object that is populated with
 	 * items.
 	 * 
 	 * Sets up a menu that provides the Insert option plus a list of alternative
 	 * actions for this Activity. Other applications that want to handle notes
 	 * can "register" themselves in Android by providing an intent filter that
 	 * includes the category ALTERNATIVE and the mimeTYpe
 	 * NotePad.Notes.CONTENT_TYPE. If they do this, the code in
 	 * onCreateOptionsMenu() will add the Activity that contains the intent
 	 * filter to its list of options. In effect, the menu will offer the user
 	 * other applications that can handle notes.
 	 * 
 	 * @param menu
 	 *            A Menu object, to which menu items should be added.
 	 * @return True, always. The menu should be displayed.
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate menu from XML resource
 		MenuInflater inflater = getMenuInflater();
 
 		inflater.inflate(R.menu.list_options_menu, menu);
 
 		// Generate any additional actions that can be performed on the
 		// overall list. In a normal install, there are no additional
 		// actions found here, but this allows other applications to extend
 		// our menu with their own actions.
 		Intent intent = new Intent(null, getIntent().getData());
 		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
 		menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
 				new ComponentName(this, MPDHDActivity.class), null, intent, 0,
 				null);
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	/**
 	 * This method is called when the user selects an option from the menu, but
 	 * no item in the list is selected. If the option was INSERT, then a new
 	 * Intent is sent out with action ACTION_INSERT. The data from the incoming
 	 * Intent is put into the new Intent. In effect, this triggers the
 	 * NoteEditor activity in the NotePad application.
 	 * 
 	 * If the item was not INSERT, then most likely it was an alternative option
 	 * from another application. The parent method is called to process the
 	 * item.
 	 * 
 	 * @param item
 	 *            The menu item that was selected by the user
 	 * @return True, if the INSERT menu item was selected; otherwise, the result
 	 *         of calling the parent method.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
		case R.id.settings:
 			/*
 			 * Launches a new Activity using an Intent. The intent filter for
 			 * the Activity has to have action ACTION_INSERT. No category is
 			 * set, so DEFAULT is assumed. In effect, this starts the NoteEditor
 			 * Activity in NotePad.
 			 */
 			startActivity(new Intent(getApplicationContext(),
 					SettingsActivity.class));
 			return true;
 
 			/*
 			 * case R.id.menu_forceConnect: //Grey out if there is no
 			 * server information or if there is // no data connection
 			 * Log.i(tag, "Connect Hit"); return true;
 			 */
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// Suppress the use of the volume keys unless we are currently listening
 		// to the stream
 		
 		switch(keyCode) {
 		case KeyEvent.KEYCODE_VOLUME_UP:
 			new Thread(new Runnable() {
 				@Override
 				public void run() {
 					JMPDHelper2.getInstance().volumeUp();
 				}
 			}).start();
 			return true;
 
 		case KeyEvent.KEYCODE_VOLUME_DOWN:
 			new Thread(new Runnable() {
 				@Override
 				public void run() {
 					JMPDHelper2.getInstance().volumeDown();
 				}
 			}).start();
 			return true;
 			
 		default:
 			return super.onKeyDown(keyCode, event);
 		}
 	}
 
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		TimerHelper.getInstance().scheduleTask(
 				new NetworkAndUITask(this), 300);
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		TimerHelper.getInstance().cancelScheduledTasks();
 	}
 	
 	/**
      * 
      */
 	private void setupTabbedActionBar() {
 		ActionBar bar = getActionBar();
 		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		// Maybe see if I can force the tabs to a specific area ?
 
 		ActionBar.Tab nowPlayingTab = bar.newTab().setText("Now Playing");
 		ActionBar.Tab queueTab = bar.newTab().setText("Queue");
 		ActionBar.Tab databaseTab = bar.newTab().setText("Database");
 		ActionBar.Tab searchTab = bar.newTab().setText("Search");
 		ActionBar.Tab playlistsTab = bar.newTab().setText("Playlists");
 
 		Fragment nowPlayingFragment = new NowPlayingFragmentTab();
 		Fragment queueFragment = new QueueFragmentTab();
 		Fragment databaseFragment = new DatabaseFragmentTab();
 		Fragment searchFragment = new SearchFragmentTab();
 		Fragment playlistsFragment = new PlaylistsFragmentTab();
 
 		nowPlayingTab.setTabListener(new MyTabsListener(nowPlayingFragment));
 		queueTab.setTabListener(new MyTabsListener(queueFragment));
 		databaseTab.setTabListener(new MyTabsListener(databaseFragment));
 		searchTab.setTabListener(new MyTabsListener(searchFragment));
 		playlistsTab.setTabListener(new MyTabsListener(playlistsFragment));
 
 		bar.addTab(nowPlayingTab);
 		bar.addTab(queueTab);
 		bar.addTab(databaseTab);
 		bar.addTab(searchTab);
 		bar.addTab(playlistsTab);
 	}
 }
