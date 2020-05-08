 package de.da_sense.moses.client;
 
 import java.io.IOException;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.ActionBar.TabListener;
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.app.FragmentTransaction;
 import android.app.ListFragment;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.provider.Settings.Secure;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import de.da_sense.moses.client.abstraction.HardwareAbstraction;
 import de.da_sense.moses.client.abstraction.apks.ExternalApplication;
 import de.da_sense.moses.client.abstraction.apks.HistoryExternalApplicationsManager;
 import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplication;
 import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
 import de.da_sense.moses.client.abstraction.apks.InstalledStateMonitor;
 import de.da_sense.moses.client.preferences.MosesPreferences;
 import de.da_sense.moses.client.service.MosesService;
 import de.da_sense.moses.client.service.MosesService.LocalBinder;
 import de.da_sense.moses.client.service.helpers.Executable;
 import de.da_sense.moses.client.service.helpers.ExecutableForObject;
 import de.da_sense.moses.client.service.helpers.HookTypesEnum;
 import de.da_sense.moses.client.service.helpers.MessageTypesEnum;
 import de.da_sense.moses.client.userstudy.UserstudyNotificationManager;
 import de.da_sense.moses.client.util.Log;
 
 /**
  * This activity shows a login field to the user if necessary and is 
  * responsible for the main application view.
  * It's the first activity a user sees when he starts our App.
  * 
  * @author Jaco Hofmann, Sandra Amend, Wladimir Schmidt
  * @author Zijad Maksuti 
  *
  */
 public class WelcomeActivity extends FragmentActivity {
 
 	/** Invalid tab. Constant for the tab selection logic. */
 	protected final static int TAB_INVALID = -1;
 	/** Available user studies tab. Constant for the tab selection logic. */
 	protected final static int TAB_AVAILABLE = 0;
 	/** Running user studies tab. Constant for the tab selection logic. */
 	protected final static int TAB_RUNNING = 1;
 	/** History of user studies tab. Constant for the tab selection logic. */
 	protected final static int TAB_HISTORY = 2;
 	
 	/** Store the currently active tab. */
 	private int mActiveTab = TAB_AVAILABLE;
 	/** Stores the tab in case we have a special call */
 	private int firstTabPreference = TAB_INVALID;
 	/** The current instance is saved in here. */
 	private static WelcomeActivity thisInstance = null;
 
 	/** This Object represents the underlying service. */
     private static MosesService mService;
     
     /** Set to true if this Activity is waiting for the result of another activity */
 	private static boolean waitingForResult = false;
 	
 	/** check if necessary */
 	private String onLoginCompleteShowUserStudy = null;
 	/** reference to the InstalledStateMonitor */
 	private static InstalledStateMonitor installedStateMonitor = null;
 	
 	/** If this variable is true the activity is connected to the service. **/
 	private static boolean mBound = false;
 	/** Stores an APK ID to update the APK. **/
 	public static final String EXTRA_UPDATE_APK_ID = "update_arrived_apkid";
 	
 	private static final String LOG_TAG = WelcomeActivity.class.getName();
 	
 	
 	/**
 	 * @return the current instance (singleton)
 	 */
 	public static WelcomeActivity getInstance() {
 		return thisInstance;
 	}
 	
 	/**
 	 * @return the active tab
 	 */
 	public int getActiveTab() {
 		return mActiveTab;
 	}
 
 	/**
 	 * Helper method to set the active tab, when another tab gets selected.
 	 * @param mActiveTab the tab to set active
 	 */
 	public void setActiveTab(int mActiveTab) {
 		this.mActiveTab = mActiveTab;
 	}
 	
 	/** A hook that gets executed after a successful login. */
 	private Executable postLoginSuccessHook = new Executable() {
 		@Override
 		public void execute() {
 			Log.d("MoSeS.ACTIVITY", "PostLoginSuccessHook");
 			((TextView) findViewById(R.id.success)).setText(getString(R.string.online));
 		}
 	};
 
 	/** A hook that gets executed after a failed login. */
 	private Executable postLoginFailureHook = new Executable() {
 		@Override
 		public void execute() {
 			Log.d("MoSeS.ACTIVITY", "PostLoginFailureHook");
 			((TextView) findViewById(R.id.success))
 			.setText(getString(R.string.login_error));
 			((ProgressBar) findViewById(R.id.main_spinning_progress_bar))
 			.setVisibility(View.GONE);
 		}
 	};
 
 	/** A hook that gets executed when Moses starts a login. */
 	private Executable loginStartHook = new Executable() {
 		@Override
 		public void execute() {
 			Log.d("MoSeS.ACTIVITY", "LoginStartHook");
 			((ProgressBar) findViewById(R.id.main_spinning_progress_bar))
 			.setVisibility(View.VISIBLE);
 		}
 	};
 
 	/** A hook that gets executed when a login ends. */
 	private Executable loginEndHook = new Executable() {
 		@Override
 		public void execute() {
 			Log.d("MoSeS.ACTIVITY", "LoginEndHook");
 			((ProgressBar) findViewById(R.id.main_spinning_progress_bar))
 			.setVisibility(View.GONE);
 			((TextView) findViewById(R.id.success)).setText(getString(R.string.connected));
 		}
 	};
 
 	/** A hook that gets executed after a successful logout. */
 	private Executable postLogoutHook = new Executable() {
 		@Override
 		public void execute() {
 			Log.d("MoSeS.ACTIVITY", "postLogoutHook");
 			((TextView) findViewById(R.id.success)).setText(getString(R.string.offline));
 		}
 	};
 
 	/** A hook that gets executed when a text field gets changed. */
 	private ExecutableForObject changeTextFieldHook = new ExecutableForObject() {
 		@Override
 		public void execute(final Object o) {
 			if (o instanceof String) {
 				((TextView) findViewById(R.id.success)).setText((String) o);
 			}
 		}
 	};
 	
 	/**
 	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
 	 */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         thisInstance = this;
 		Log.d("MainActivity", "onCreate called");
         
         // Moses got called to view a UserStudy
         boolean isShowUserStudyCall = getIntent()
         		.getStringExtra(ViewUserStudyActivity
         				.EXTRA_USER_STUDY_APK_ID) != null;
 		
         if (isShowUserStudyCall) {
 			onLoginCompleteShowUserStudy = getIntent()
 					.getStringExtra(ViewUserStudyActivity
 							.EXTRA_USER_STUDY_APK_ID);
 		}
         
 		if (!isLoginInformationComplete(this) && !waitingForResult) {
 			// Here, the activity is called to display the login screen, and,
 			// when filled in, redirect the user to the user study that was
 			// meant to be displayed originally
 			waitingForResult = true;
 			// set flag that on login credentials arrival show a user study
 			
 			// set the deviceID in the SharedPreferences before attempting to login
 			String theDeviceID = Secure.getString(getContentResolver(), Secure.ANDROID_ID); 
 			PreferenceManager.getDefaultSharedPreferences(this).edit().putString(MosesPreferences.PREF_DEVICEID, theDeviceID).commit();
 			Intent loginDialog = new Intent(WelcomeActivity.this, LoginActivity.class);
 			startActivityForResult(loginDialog, 1);
 		}
 
 		if (HistoryExternalApplicationsManager.getInstance() == null) {
 			HistoryExternalApplicationsManager.init(this);
 		}
         if (InstalledExternalApplicationsManager.getInstance() == null) {
 			InstalledExternalApplicationsManager.init(this);
 		}
 		if (UserstudyNotificationManager.getInstance() == null) {
 			UserstudyNotificationManager.init(this);
 		}
 
 		// initialize the UI elements
 		setContentView(R.layout.activity_main);		
 		initControls(savedInstanceState);
     }
 
     /**
      * @see com.actionbarsherlock.app.SherlockFragmentActivity#onCreateOptionsMenu(android.view.Menu)
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
         return true;
     }
 
     /**
 	 * Sets the menu item to "disconnect" if service is logged in, otherwise
 	 * sets it to "connect". 
 	 */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		
 		Log.d("MainActivity", "options menu Logged in state gets set");
 		/*
 		if (mBound) {
 			if (mService.isLoggedIn())
 				menu.findItem(R.id.Menu_Connect)
 				.setTitle(R.string.menu_disconnect);
 			else
 				menu.findItem(R.id.Menu_Connect)
 				.setTitle(R.string.menu_connect);
 		}*/
 		return true;
 	}
 
 	/**
      * Handles all of the item selections in the ActionBar Menu.
      * @see com.actionbarsherlock.app.SherlockFragmentActivity#onOptionsItemSelected(android.view.MenuItem)
      */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		boolean result= false;
 		switch (item.getItemId()) {
 
         case R.id.Menu_Settings:
             // Settings entry in menu clicked
         	Log.d("MainActivity", "Settings in menu clicked");
         	// make an intent between this activity and MosesPreferences to show the setting screen
             Intent settings = new Intent(this, MosesPreferences.class);
             // switch screen to settings 
             startActivityForResult(settings, 0);
             result = true;
             break;
             
         case R.id.Menu_Logout:
         	// Logout entry in menu clicked
         	Log.d("MainActivity", "Logout in menu clicked");
         	PreferenceManager.getDefaultSharedPreferences(this).edit()
         	.remove(MosesPreferences.PREF_EMAIL).remove(MosesPreferences.PREF_PASSWORD).commit();
         	waitingForResult = true;
         	
         	// stop the service
         	if(mBound){
         		if(mService.isLoggedIn())
         			mService.logout();
         	}
         	
         	Intent mainDialog = new Intent(WelcomeActivity.this, LoginActivity.class);
         	startActivityForResult(mainDialog, 1);
         	result = true;
         	break;
         default:
         	result = super.onOptionsItemSelected(item);
         }
 		return result;
 	}
 
 	/**
 	 * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
 	 */
 	@Override
 	protected void onSaveInstanceState(Bundle savedInstanceState) {
 		super.onSaveInstanceState(savedInstanceState);
 		// save the currently active tab
 		savedInstanceState.putInt("activeTab", getActiveTab());
 		Log.d("MainActivity", "onSaveInstanceState called with activeTab="+getActiveTab());	
 	}
 
 	/**
 	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
 	 */
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 //		// restore the active tab
 //		setActiveTab(savedInstanceState.getInt("activeTab", TAB_AVAILABLE));
 //		initControls(savedInstanceState);
 //		Log.d("MainActivity", "onRestoreInstanceState called with activeTab=" + getmActiveTab());
 	}
     
     /**
      * This object handles connection and disconnection of the service
      */
     private ServiceConnection mConnection = new ServiceConnection() {
         /**
          * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
          */
     	@Override
         public void onServiceConnected(ComponentName className, IBinder service) {
         	// We've bound to LocalService, cast the IBinder and get
         	// LocalService instance
         	LocalBinder binder = (LocalBinder) service;
         	mService = binder.getService();
         	mBound = true;
 
         	// Add hooks
         	mService.registerHook(HookTypesEnum.POST_LOGIN_SUCCESS,
         			MessageTypesEnum.ACTIVITY_PRINT_MESSAGE, postLoginSuccessHook);
 
         	mService.registerHook(HookTypesEnum.POST_LOGIN_FAILED,
         			MessageTypesEnum.ACTIVITY_PRINT_MESSAGE, postLoginFailureHook);
 
         	mService.registerHook(HookTypesEnum.POST_LOGIN_START,
         			MessageTypesEnum.ACTIVITY_PRINT_MESSAGE, loginStartHook);
 
         	mService.registerHook(HookTypesEnum.POST_LOGIN_END,
         			MessageTypesEnum.ACTIVITY_PRINT_MESSAGE, loginEndHook);
 
         	mService.registerHook(HookTypesEnum.POST_LOGOUT,
         			MessageTypesEnum.ACTIVITY_PRINT_MESSAGE, postLogoutHook);
 
         	mService.registerChangeTextFieldHook(changeTextFieldHook);
 
         	mService.setActivityContext(WelcomeActivity.this);
 
         	if (mService.isLoggedIn()) {
         		((TextView) findViewById(R.id.success)).setText("Online");
         	} else {
         		((TextView) findViewById(R.id.success)).setText("Offline"); 
         	}
 
         	if (PreferenceManager.getDefaultSharedPreferences(
         			WelcomeActivity.this).getBoolean("first_start", true)
         			&& !waitingForResult) {
         		mService.startedFirstTime(WelcomeActivity.this);
         	}
 
         	// only use installedStateMonitor when the service is running to
         	// avoid unsent messages
         	installedStateMonitor = InstalledStateMonitor.getDefault();
         	checkInstalledStatesOfApks();
         }
         
     	/**
     	 * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
     	 */
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
 			mService.unregisterHook(HookTypesEnum.POST_LOGIN_SUCCESS,
 					postLoginSuccessHook);
 			mService.unregisterHook(HookTypesEnum.POST_LOGIN_FAILED,
 					postLoginFailureHook);
 			mService.unregisterHook(HookTypesEnum.POST_LOGIN_START, 
 					loginStartHook);
 			mService.unregisterHook(HookTypesEnum.POST_LOGIN_END, 
 					loginEndHook);
 			mService.unregisterHook(HookTypesEnum.POST_LOGOUT, 
 					postLogoutHook);
 			mService.unregisterChangeTextFieldHook(changeTextFieldHook);
 			mService.setActivityContext(null);
 
 			// only use InstalledStateManager when the service is running to
 			// avoid unsent messages
 			installedStateMonitor = null;
 			mBound = false;
         }
     };
     
 	/**
      * Start and bind the Moses service.
      */
     private void startAndBindService() {
     	
         Intent intent = new Intent(this, MosesService.class);
         if (null == startService(intent)) {
             stopService(intent);
             startService(intent);
         }
         bindService(intent, mConnection, 0);
     }
     
     /**
      * When first started this activity starts a Task that keeps the connection
      * with the service alive and restarts it if necessary.
      */
     @Override
     protected void onStart() {
         super.onStart();
         ((ProgressBar) findViewById(R.id.main_spinning_progress_bar))
         .setVisibility(View.GONE);
         /*
          * If the device id is not set in the shared preferences,
          * it means that this is the first time the client has started on this device.
          * to the device 
          */
         
         startAndBindService();
     }
     
     /**
 	 * If the MoSeS Service is running, this checks the consistency of installed
 	 * applications and the installed apps local database.
 	 * 
 	 * @return null if the MosesService was not running or any other
 	 *         circumstance prevented successful checking; returns true for a
 	 *         valid database and false for a database that was invalid but has
 	 *         been made valid (refresh of aAPK list necessary).
 	 */
 	public static Boolean checkInstalledStatesOfApks() {
 		if (MosesService.getInstance() != null && 
 				 installedStateMonitor != null) {
 			Log.d("MoSeS.APK",
 					"synchronizing installed applications with internal " +
 					"installed app database");
 			return installedStateMonitor.checkForValidState(MosesService
 					.getInstance());
 		} else {
 			Log.d("MoSeS.APK",
 					"Wanted to check state of installed apks, but service " +
 					"was not started yet or some other failure");
 		}
 		return null;
 	}
 
 	/**
 	 * @see android.app.Activity#onWindowFocusChanged(boolean)
 	 */
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus);
 		if (hasFocus && MosesService.getInstance() != null) {
 			MosesService.getInstance().setActivityContext(this);
 		}
 		
 		// inform the fragments
 		AvailableFragment fragment = (AvailableFragment) getFragmentManager().findFragmentById(R.id.availableApkListFragment);
 		
 		if (hasFocus && fragment != null) {
 			fragment.onWindowFocusChangedFragment(hasFocus);
 		}
 	}
 
 	/**
 	 * Disconnect from the service if it is connected and stop logged in check.
 	 */
 	private void disconnectService() {
 		if (mBound) {
 			unbindService(mConnection);
 		}
 	}
 
 	/**
 	 * Checks if is MoSeS service running.
 	 * 
 	 * @return true, if Moses service is running
 	 */
 	private boolean isMosesServiceRunning() {
 		ActivityManager manager = (ActivityManager) 
 				getSystemService(ACTIVITY_SERVICE);
 		for (RunningServiceInfo service : 
 			 manager.getRunningServices(Integer.MAX_VALUE)) {
 			String serviceName = service.service.getClassName();
 			Log.d(LOG_TAG, "service name: "+serviceName); 
 			if (MosesService.class.getName()
 					.equals(service.service.getClassName())) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * User comes back from another activity.
 	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d("MainActivity", "onActivityResult called with requestCode " + requestCode);
 		if (!isMosesServiceRunning())
 			startAndBindService();
 		if (requestCode == 1) { // Login activity
 			waitingForResult = false;
 			switch (resultCode) {
 			case Activity.RESULT_OK:
 				SharedPreferences.Editor e = PreferenceManager
 						.getDefaultSharedPreferences(this).edit();
 				String username = data.getStringExtra(MosesPreferences.PREF_EMAIL);
 				String password = data.getStringExtra(MosesPreferences.PREF_PASSWORD);
 				String deviceName = data.getStringExtra(MosesPreferences.PREF_DEVICENAME);
 				Log.d("MoSeS.ACTIVITY", username);
 				Log.d("MoSeS.ACTIVITY", password);
 				e.putString(MosesPreferences.PREF_EMAIL, username);
 				e.putString(MosesPreferences.PREF_PASSWORD, password);
 				String deviceNameAlreadyStored = HardwareAbstraction.extractDeviceNameFromSharedPreferences(); 
 				if(deviceNameAlreadyStored == null){
 					// only set the deviceName sent by the server if the client does not know his name
 					if(deviceName != null){ // the server may not know the name of the device, so check if the response contained the name
 							e.putString(MosesPreferences.PREF_DEVICENAME, deviceName);
 						}
 					else{
 						// the server does not know the deviceName either, set the the device's model name as the device name
 						e.putString(MosesPreferences.PREF_DEVICENAME, Build.MODEL);
 					}
 				}
				e.apply();
 				
 				if (onLoginCompleteShowUserStudy != null) {
 					// if a user study is to be displayed
 					UserstudyNotificationManager.displayUserStudyContent(
 							onLoginCompleteShowUserStudy,
 							this.getApplicationContext());
 					onLoginCompleteShowUserStudy = null;
 				}
 				break;
 			case Activity.RESULT_CANCELED:
 				finish();
 				break;
 			}
 		}
 	}
 	
 	/**
 	 * Initialize the UI.
 	 */
 	private void initControls(Bundle savedInstanceState) {
 		// first check if we have a saved instance
 		if (savedInstanceState == null) {
 			Log.d("MainActivity", "savedInstanceState == null");
 			savedInstanceState = new Bundle();
 			savedInstanceState.putInt("activeTab", TAB_AVAILABLE);
 		}
 
 		// did we get a Bundle? if not use savedInstanceState
 		Bundle bundle = getIntent().getExtras();
 		if (bundle == null) {
 			Log.d("MainActivity", "bundle == null");
 			bundle = savedInstanceState;
 		}
 
 		// get the selected Tab
 		setActiveTab(bundle.getInt("activeTab"));
 
 		Log.d("MainActivity", "initControls after getInt -> activeTab = "
 				+ getActiveTab());		
 
 		// now check if it is a user study call
 		boolean isShowUserStudyCall = getIntent().getStringExtra(
 				ViewUserStudyActivity.EXTRA_USER_STUDY_APK_ID) != null;
 		// or an update call
 		boolean isShowUpdateCall = getIntent().getStringExtra(
 				EXTRA_UPDATE_APK_ID) != null;
 
 		if (isShowUserStudyCall && isLoginInformationComplete()) {
 			// firstTabPreference = TAB_TAG_AVAILABLE_USER_STUDIES;
 			if (getActiveTab() != TAB_AVAILABLE) {
 				Log.d("MainActivity - initControls", "WARNING: active Tab "
 						+ "is going to change because of UserStudy-Call. ACTIVE TAB SET "
 						+ "FROM " + getActiveTab() + " TO AVAILABLE (0)");
 			}
 			firstTabPreference = TAB_AVAILABLE; // available user studies
 		}
 		if (isShowUpdateCall) {
 			// firstTabPreference = TAB_TAG_INSTALLED_APPS;
 			if (getActiveTab() != TAB_RUNNING) {
 				Log.d("MainActivity - initControls", "WARNING: active Tab "
 						+ "changed because of ShowUpdate-Call. ACTIVE TAB SET "
 						+ "FROM " + getActiveTab() + " TO RUNNING (1)");
 			}
 			firstTabPreference = TAB_RUNNING; // show the running user studies
 
 			// old TODO: maybe more; display some ui magic to show the update or
 			// whatever
 		}
 		
 		// if firstTabPreference was set, we set this tab as active
 		if (firstTabPreference != TAB_INVALID) {
 			setActiveTab(firstTabPreference);
 		}
 
 		// booleans for the tab selection
 		boolean availableSelected;
 		boolean runningSelected;
 		boolean historySelected;
 
 		switch (getActiveTab()) {
 		case TAB_AVAILABLE: // standard case (available tab)
 			availableSelected = true;
 			runningSelected = false;
 			historySelected = false;
 			break;
 
 		case TAB_RUNNING: // running tab
 			availableSelected = false;
 			runningSelected = true;
 			historySelected = false;
 			break;
 
 		case TAB_HISTORY: // history tab
 			availableSelected = false;
 			runningSelected = false;
 			historySelected = true;
 			break;
 
 		default:
 			availableSelected = true;
 			runningSelected = false;
 			historySelected = false;
 			break;
 		}
 
 		// get ActionBar and set NavigationMode
 		ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		
 		// add Tabs to the ActionBar
 		Tab availabletab = actionBar
 				.newTab()
 				.setText(getString(R.string.tab_available))
 				.setTabListener(
 						new MosesTabListener<AvailableFragment>(this,
 								"available", AvailableFragment.class));
 		
 		// with parameter if tab is selected
 		actionBar.addTab(availabletab, availableSelected);
 
 		Tab runningtab = actionBar
 				.newTab()
 				.setText(getString(R.string.tab_running))
 				.setTabListener(
 						new MosesTabListener<RunningFragment>(this, 
 								"running", RunningFragment.class));
 		actionBar.addTab(runningtab, runningSelected);
 
 		Tab historytab = actionBar
 				.newTab()
 				.setText(getString(R.string.tab_history))
 				.setTabListener(
 						new MosesTabListener<HistoryFragment>(this, 
 								"history", HistoryFragment.class));
 		actionBar.addTab(historytab, historySelected);
 		
 		
 
 		if (isShowUserStudyCall && isLoginInformationComplete()) {
 			// if a User study has to be shown, and email and password are
 			// set, redirect this
 			UserstudyNotificationManager.displayUserStudyContent(
 					onLoginCompleteShowUserStudy, this);
 		}
 	}
 	
 	/**
 	 * Disconnect service so android won't get angry.
 	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onStop()
 	 */
 	@Override
 	protected void onStop() {
 		super.onStop();
 		try {
 			InstalledExternalApplicationsManager.getInstance().saveToDisk(thisInstance);
 			HistoryExternalApplicationsManager.getInstance().saveToDisk(thisInstance);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		disconnectService();
 	}
 	
 	
 
 	/**
 	 * @see android.support.v4.app.FragmentActivity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		checkInstalledStatesOfApks();
 	}
 	
 	/**
 	 * Tests if the login information is complete in the shared preferences.
 	 * @return true when the information which is required for the service to
 	 *         properly log-in is complete.
 	 */
 	public static boolean isLoginInformationComplete(Context c) {
 		SharedPreferences sps = PreferenceManager.getDefaultSharedPreferences(c);
 		boolean result = !(sps.getString(MosesPreferences.PREF_EMAIL, "").equals("") ||
 				sps.getString(MosesPreferences.PREF_PASSWORD, "").equals(""));
 		
 		return result;
 	}
 	
 	/**
 	 * Tests if the login information is complete in the shared preferences.
 	 * @return whether the information that is required for the service to
 	 *         properly log-in is complete.
 	 */
 	private static boolean isLoginInformationComplete() {
 		return isLoginInformationComplete(MosesService.getInstance());
 	}
 
 	/**
 	 * Workaround for update calls.
 	 * @param app
 	 * @param baseActivity
 	 * @param installAppClickAction
 	 * @param cancelClickAction
 	 */
 	public void showAvailableDetails(ExternalApplication app, Activity baseActivity, 
 			final Runnable installAppClickAction,
 			final Runnable cancelClickAction) {
 		AvailableFragment fragment = (AvailableFragment) getInstance().getFragmentManager().findFragmentByTag("available");
 		fragment.showDetails(app, baseActivity, installAppClickAction, cancelClickAction);
 	}
 	
 	/**
 	 * For update calls.
 	 * @param app
 	 * @param baseActivity
 	 * @param installAppClickAction
 	 * @param cancelClickAction
 	 */
 	@Deprecated
 	public void showRunningDetails(InstalledExternalApplication app, Activity baseActivity, 
 			final Runnable AppClickAction,
 			final Runnable cancelClickAction) {
 		RunningFragment fragment = (RunningFragment) getInstance().getFragmentManager().findFragmentByTag("running");
 		fragment.showDetails(app, baseActivity, AppClickAction, cancelClickAction);
 	}
 	
 	/**
 	 * Moved this into the MainActivity. Also stores a Bundle.
 	 */
 	private static class MosesTabListener<T extends ListFragment> implements TabListener {
 		
 		/** the Fragment of the tab */
 		private ListFragment mFragment;
 		/** the Activity of the tab */
 		private final FragmentActivity mActivity;
 		/** the tag of the tab */
 		private final String mTag;
 		/** the class of the tab */
 		private final Class<T> mClass;
 		/** a bundle to check if we already have a fragment for this tab */
 		private final Bundle mArgs;
 
 		/**
 		 * Constructor for the TabListener.
 		 * @param activity the activity of the tab
 		 * @param tag the tag for the tab
 		 * @param clz the class of the tab
 		 */
 		private MosesTabListener(FragmentActivity activity, String tag, Class<T> clz) {
             this(activity, tag, clz, null);
         }
 
 		/**
 		 * Constructor for the TabListener.
 		 * @param activity
 		 * @param tag
 		 * @param clz
 		 * @param args
 		 */
         private MosesTabListener(FragmentActivity activity, String tag, Class<T> clz, Bundle args) {
             mActivity = activity;
             mTag = tag;
             mClass = clz;
             mArgs = args;
 
             // Check to see if we already have a fragment for this tab, probably
             // from a previously saved state.  If so, deactivate it, because our
             // initial state is that a tab isn't shown.
             mFragment = (ListFragment) mActivity
             		.getFragmentManager().findFragmentByTag(mTag);
             if (mFragment != null && !mFragment.isDetached()) {
                 FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
                 ft.detach(mFragment);
                 ft.commit();
             }
         }
 		
 		/**
 		 * Callback Methods for com.actionbarsherlock.app.ActionBar.TabListener
 		 * @see com.actionbarsherlock.app.ActionBar.TabListener#onTabSelected(com.actionbarsherlock.app.ActionBar.Tab, android.support.v4.app.FragmentTransaction)
 		 */
 		@Override
 		public void onTabSelected(Tab tab, FragmentTransaction ft) {
 			// check if the Fragment is already initialized
 			ListFragment preInitializedFragment = (ListFragment) mActivity.getFragmentManager().findFragmentByTag(mTag);
 			
 			// set the view of the tab
 			mActivity.setContentView(R.layout.activity_main);
 			
 			if (mFragment == null && preInitializedFragment == null) {
 				// instantiate the Fragment and add it
 				Log.d("TabListener", "onTabSelected: mActivity = " 
 						+ mActivity + " mClass = " + mClass.getName());
 				mFragment = (ListFragment) ListFragment
 						.instantiate(mActivity, mClass.getName(), mArgs);
 				mFragment.setRetainInstance(true); // XXX: inserted because of the getActivty NullPointer
 				
 				Log.d("TabListener", "onTabSelected - R.id.listView = " 
 						+ mActivity.findViewById(R.id.listView));
 				ft.add(R.id.listView, mFragment, mTag);
 			} else if (mFragment != null) {
 				// the Fragment already exists so just attach it
 				Log.d("TabListener", 
 						"Fragment already exists, so just attached it: "
 								+ mFragment.getTag());
 				ft.attach(mFragment);
 			} else if (preInitializedFragment != null) {
 				Log.d("TabListener", 
 						"preInitializedFragment, so just attached it: " 
 								+ preInitializedFragment.getTag());
 		        ft.attach(preInitializedFragment);
 		        mFragment = preInitializedFragment;
 		    }
 			
 			// set the active tab in the MainActivity
 			// solves the overlying of the tabs content on orientation change 
 			Log.d("TabListener", "mTag = " + mTag + " mActivity = " + mActivity
 					.getLocalClassName());
 			if (mTag.equals("available")) {
 				((WelcomeActivity) mActivity).setActiveTab(WelcomeActivity.TAB_AVAILABLE);
 				Log.d("TabListener", "activeTab set to 0");
 			} else if (mTag.equals("running")) {
 				((WelcomeActivity) mActivity).setActiveTab(WelcomeActivity.TAB_RUNNING);
 				Log.d("TabListener", "activeTab set to 1");
 			} else if (mTag.equals("history")) {
 				((WelcomeActivity) mActivity).setActiveTab(WelcomeActivity.TAB_HISTORY);
 				Log.d("TabListener", "activeTab set to 2");
 			}
 		}
 
 		/**
 		 * If unselected fragment not null, detach it.
 		 * @see com.actionbarsherlock.app.ActionBar.TabListener#onTabUnselected(com.actionbarsherlock.app.ActionBar.Tab, android.support.v4.app.FragmentTransaction)
 		 */
 		@Override
 		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 			Log.d("MosesTabListener", mFragment.getTag() + "Fragment detached");
 			if (mFragment != null) {
 				ft.detach(mFragment);
 			}
 		}
 		
 		/**
 		 * @see com.actionbarsherlock.app.ActionBar.TabListener#onTabReselected(com.actionbarsherlock.app.ActionBar.Tab, android.support.v4.app.FragmentTransaction)
 		 */
 		@Override
 		public void onTabReselected(Tab tab, FragmentTransaction ft) {
 			// nothing to do here
 		}
 	}
 
 }
