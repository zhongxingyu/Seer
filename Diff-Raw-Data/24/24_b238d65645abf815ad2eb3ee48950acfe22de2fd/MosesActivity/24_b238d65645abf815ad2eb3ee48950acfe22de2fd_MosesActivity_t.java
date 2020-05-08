 package moses.client;
 
 import moses.client.abstraction.HardwareAbstraction;
 import moses.client.abstraction.apks.InstalledExternalApplicationsManager;
 import moses.client.preferences.MosesPreferences;
 import moses.client.service.MosesService;
 import moses.client.service.MosesService.LocalBinder;
 import moses.client.service.helpers.Executor;
 import moses.client.service.helpers.ExecutorWithObject;
 import moses.client.userstudy.UserstudyNotificationManager;
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.app.Dialog;
 import android.app.TabActivity;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.TabHost;
 import android.widget.TextView;
 
 /**
  * This activity shows a login field to the user.
  * 
  * It's the first activity a user sees who starts our app.
  * 
  * @author Jaco
  * 
  */
 public class MosesActivity extends TabActivity {
 
 	private static boolean showsplash = true;
 
 	public enum results {
 		RS_DONE, RS_CLOSE, RS_LOGGEDOUT
 	};
 
 	/**
 	 * Login hooks
 	 */
 	Executor postLoginSuccessHook = new Executor() {
 		@Override
 		public void execute() {
 			Log.d("MoSeS.ACTIVITY", "PostLoginSuccessHook");
 			((TextView) findViewById(R.id.success)).setText("Online");
 		}
 	};
 
 	Executor postLoginFailureHook = new Executor() {
 		@Override
 		public void execute() {
 			Log.d("MoSeS.ACTIVITY", "PostLoginFailureHook");
 			((TextView) findViewById(R.id.success))
 					.setText("Error while logging in.");
 		}
 	};
 
 	Executor loginStartHook = new Executor() {
 		@Override
 		public void execute() {
 			Log.d("MoSeS.ACTIVITY", "LoginStartHook");
 			((ProgressBar) findViewById(R.id.main_spinning_progress_bar))
 					.setVisibility(View.VISIBLE);
 		}
 	};
 
 	Executor loginEndHook = new Executor() {
 		@Override
 		public void execute() {
 			Log.d("MoSeS.ACTIVITY", "LoginEndHook");
 			((ProgressBar) findViewById(R.id.main_spinning_progress_bar))
 					.setVisibility(View.GONE);
 			((TextView) findViewById(R.id.success)).setText("Connected");
 		}
 	};
 
 	Executor postLogoutHook = new Executor() {
 
 		@Override
 		public void execute() {
 			Log.d("MoSeS.ACTIVITY", "postLogoutHook");
 			((TextView) findViewById(R.id.success)).setText("Offline");
 		}
 	};
 
 	ExecutorWithObject changeTextFieldHook = new ExecutorWithObject() {
 
 		@Override
 		public void execute(Object o) {
 			if (o instanceof String) {
 				((TextView) findViewById(R.id.success)).setText((String) o);
 			}
 		}
 	};
 
 	/** This Object represents the underlying service. **/
 	public MosesService mService;
 
 	/** If this variable is true the activity is connected to the service. **/
 	public static boolean mBound = false;
 
 	/** This object handles connection and disconnection of the service **/
 	private ServiceConnection mConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			// We've bound to LocalService, cast the IBinder and get
 			// LocalService instance
 			LocalBinder binder = (LocalBinder) service;
 			mService = binder.getService();
 			mBound = true;
 
 			// Add hooks
 			mService.registerPostLoginSuccessHook(postLoginSuccessHook);
 
 			mService.registerPostLoginFailureHook(postLoginFailureHook);
 
 			mService.registerLoginStartHook(loginStartHook);
 
 			mService.registerLoginEndHook(loginEndHook);
 
 			mService.registerPostLogoutHook(postLogoutHook);
 
 			mService.registerChangeTextFieldHook(changeTextFieldHook);
 
 			mService.setActivityContext(MosesActivity.this);
 
 			if (mService.isLoggedIn()) {
 				((TextView) findViewById(R.id.success)).setText("Online");
 			} else {
 				((TextView) findViewById(R.id.success)).setText("Offline");
 			}
 
 			if (PreferenceManager.getDefaultSharedPreferences(
					MosesActivity.this).getBoolean("first_start", true) && !waitingForResult) {
 				mService.startedFirstTime(MosesActivity.this);
 			}
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
 			mService.unregisterPostLoginSuccessHook(postLoginSuccessHook);
 
 			mService.unregisterPostLoginFailureHook(postLoginFailureHook);
 
 			mService.unregisterLoginStartHook(loginStartHook);
 
 			mService.unregisterLoginEndHook(loginEndHook);
 
 			mService.unregisterPostLogoutHook(postLogoutHook);
 
 			mService.unregisterChangeTextFieldHook(changeTextFieldHook);
 
 			mService.setActivityContext(null);
 
 			mBound = false;
 		}
 	};
 
	private boolean waitingForResult = false;

 	/**
 	 * Connect to the server and save (changed) settings
 	 */
 	private void connect() {
 		// Login if not already or just start logged in view if present
 		Log.d("MoSeS.ACTIVITY", "Connect button pressed.");
 		if (mService != null) {
 			if (!mService.isLoggedIn()) {
 				mService.login();
 			}
 		}
 	}
 
 	public void onWindowFocusChanged(boolean f) {
 		super.onWindowFocusChanged(f);
 		if (MosesService.getInstance() != null) {
 			MosesService.getInstance().setActivityContext(this);
 		} else {
 			MosesService.getInstance().setActivityContext(null);
 		}
 	}
 
 	/**
 	 * Disconnect from the service if it is connected and stop logged in check
 	 */
 	private void disconnectService() {
 		if (mBound) {
 			unbindService(mConnection);
 		}
 	}
 
 	/**
 	 * Checks if is MoSeS service running.
 	 * 
 	 * @return true, if is moses service running
 	 */
 	private boolean isMosesServiceRunning() {
 		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
 		for (RunningServiceInfo service : manager
 				.getRunningServices(Integer.MAX_VALUE)) {
 			if ("moses.client.service.MosesService".equals(service.service
 					.getClassName())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * User comes back from another activity
 	 */
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (!isMosesServiceRunning())
 			startAndBindService();
 		if (requestCode == 1) { // Login activity
 			switch (resultCode) {
 			case Activity.RESULT_OK:
 				SharedPreferences.Editor e = PreferenceManager
 						.getDefaultSharedPreferences(this).edit();
 				String username = data.getStringExtra("username_pref");
 				String password = data.getStringExtra("password_pref");
 				Log.d("MoSeS.ACTIVITY", username);
 				Log.d("MoSeS.ACTIVITY", password);
 				e.putString("username_pref", username);
 				e.putString("password_pref", password);
 				e.commit();
				waitingForResult = false;
 				break;
 			case Activity.RESULT_CANCELED:
 				finish();
 				break;
 			}
 		}
 	}
 
 	/**
 	 * We're back, so get everything going again.
 	 */
 	public void onAttachedToWindow() {
 		super.onAttachedToWindow();
 		startAndBindService();
 	}
 
 	/**
 	 * Called when the activity is first created.
 	 * 
 	 * @param savedInstanceState
 	 *            the saved instance state
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (PreferenceManager.getDefaultSharedPreferences(this)
 				.getString("username_pref", "").equals("")
 				|| PreferenceManager.getDefaultSharedPreferences(this)
						.getString("password_pref", "").equals("") && !waitingForResult ) {
			waitingForResult = true;
 			Intent mainDialog = new Intent(MosesActivity.this,
 					MosesLoginActivity.class);
 			startActivityForResult(mainDialog, 1);
 		}
 
 		if (showsplash
 				&& PreferenceManager.getDefaultSharedPreferences(this)
						.getBoolean("splashscreen_pref", true) && !waitingForResult) {
 			showSplashScreen();
 			showsplash = false;
 		}
 
 		setContentView(R.layout.main);
 
 		if (InstalledExternalApplicationsManager.getInstance() == null) {
 			InstalledExternalApplicationsManager.init(this);
 		}
 		if (UserstudyNotificationManager.getInstance() == null) {
 			UserstudyNotificationManager.init(this);
 		}
 
 		initControls();
 	}
 
 	private Dialog mSplashDialog;
 
 	private void showSplashScreen() {
 		mSplashDialog = new Dialog(this, R.style.SplashScreen);
 		mSplashDialog.setContentView(R.layout.splashscreen);
 		mSplashDialog.setCancelable(false);
 		try {
 			((TextView) mSplashDialog.findViewById(R.id.versiontextview))
 					.setText(getPackageManager().getPackageInfo(
 							getPackageName(), 0).versionName);
 		} catch (NameNotFoundException e) {
 			Log.d("MoSeS", "There's no MoSeS around here.");
 		}
 		mSplashDialog.show();
 
 		final Handler handler = new Handler();
 		handler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if (mSplashDialog != null) {
 					mSplashDialog.dismiss();
 					mSplashDialog = null;
 				}
 			}
 		}, 1500);
 	}
 
 	private void initControls() {
 		Resources res = getResources(); // Resource object to get Drawables
 		TabHost tabHost = getTabHost(); // The activity TabHost
 		TabHost.TabSpec spec; // Resusable TabSpec for each tab
 		Intent intent; // Reusable Intent for each tab
 
 		intent = new Intent().setClass(this,
 				ViewInstalledApplicationsActivity.class);
 		spec = tabHost
 				.newTabSpec("installedApps")
 				.setIndicator("Installed apps",
 						res.getDrawable(R.drawable.ic_menu_agenda))
 				.setContent(intent);
 		tabHost.addTab(spec);
 
 		intent = new Intent().setClass(this, ViewAvailableApkActivity.class);
 		spec = tabHost
 				.newTabSpec("availableApps")
 				.setIndicator("Install apps from MoSeS",
 						res.getDrawable(R.drawable.ic_menu_add))
 				.setContent(intent);
 		tabHost.addTab(spec);
 
 		// activate installed apps tab if there is actually one installed app
 		// else show the available apps tab
 		if (InstalledExternalApplicationsManager.getInstance().getApps().size() > 0) {
 			tabHost.setCurrentTab(0);
 		} else {
 			tabHost.setCurrentTab(1);
 		}
 	}
 
 	/**
 	 * When first started this activity stars a Task that keeps the connection
 	 * with the service alive and restarts it if necessary.
 	 */
 	@Override
 	protected void onStart() {
 		super.onStart();
 		((ProgressBar) findViewById(R.id.main_spinning_progress_bar))
 				.setVisibility(View.GONE);
 		startAndBindService();
 	}
 
 	/**
 	 * Disconnect service so android won't get angry.
 	 */
 	@Override
 	protected void onStop() {
 		super.onStop();
 		disconnectService();
 	}
 
 	/**
 	 * Start and bind service.
 	 */
 	private void startAndBindService() {
 		Intent intent = new Intent(this, MosesService.class);
 		if (null == startService(intent)) {
 			stopService(intent);
 			startService(intent);
 		}
 		bindService(intent, mConnection, 0);
 	}
 
 	public void settings() {
 		Intent mainDialog = new Intent(MosesActivity.this,
 				MosesPreferences.class);
 		startActivityForResult(mainDialog, 0);
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		return true;
 	}
 
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		if (mBound) {
 			if (mService.isLoggedIn())
 				menu.findItem(R.id.item_connect).setTitle(R.string.menu_disconnect);
 			else
 				menu.findItem(R.id.item_connect).setTitle(R.string.menu_connect);
 		}
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.item_connect:
 			if (mBound) {
 				if (mService.isLoggedIn())
 					mService.logout();
 				else
 					connect();
 				break;
 			}
 		case R.id.item_settings:
 			settings();
 			break;
 		case R.id.item_exit:
 			finish();
 			break;
 		case R.id.item_hardware_info:
 			new HardwareAbstraction(this).getHardwareParameters();
 			break;
 		case R.id.item_logout:
 			PreferenceManager.getDefaultSharedPreferences(this).edit()
 					.remove("username_pref").remove("password_pref").commit();
			waitingForResult = true;
 			Intent mainDialog = new Intent(MosesActivity.this,
 					MosesLoginActivity.class);
 			startActivityForResult(mainDialog, 1);
 			break;
 		}
 		return true;
 	}
 
 }
