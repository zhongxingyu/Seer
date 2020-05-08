 package co.mwater.clientapp;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.cordova.CordovaWebView;
 import org.apache.cordova.api.CordovaInterface;
 import org.apache.cordova.api.CordovaPlugin;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Application;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 public class MainActivity extends SherlockActivity implements CordovaInterface, ActionBarPluginActivity {
 	private static String TAG = MainActivity.class.getSimpleName();
 	private final ExecutorService threadPool = Executors.newCachedThreadPool();
 	String initialHash;
 
 	boolean debugMode;
 
 	@Override
 	protected void onPause() {
 		Log.d(TAG, "onPause");
 		super.onPause();
 		
 		// Save state
 	}
 
 	@Override
 	protected void onStop() {
 		Log.d(TAG, "onStop");
 		super.onStop();
 	}
 
 	@Override
 	protected void onRestart() {
 		Log.d(TAG, "onRestart");
 		super.onRestart();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		Log.d(TAG, "onSaveInstanceState");
 		super.onSaveInstanceState(outState);
 		
 		String url = cwv.getUrl();
 		if (url != null && url.contains("#")) {
 			String hash = url.substring(url.indexOf('#') + 1);
 			outState.putString("hash", hash);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		Log.d(TAG, "onResume");
 		super.onResume();
 	}
 
 	@Override
 	protected void onStart() {
 		Log.d(TAG, "onStart");
 		super.onStart();
 	}
 
 	ActionBarPlugin actionBarPlugin;
 
 	private CordovaPlugin activityResultCallback;
 
 	private Object activityResultKeepRunning;
 
 	private boolean keepRunning;
 
 	ProgressDialog progressDialog;
 	CordovaWebView cwv;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.d(TAG, "onCreate");
 
 		if (savedInstanceState != null) {
 			initialHash = savedInstanceState.getString("hash");
 		}
 
 		setContentView(R.layout.activity_main);
 		cwv = (CordovaWebView) findViewById(R.id.mainView);
 		SpecialChromeClient client = new SpecialChromeClient(this, cwv);
 		cwv.setWebChromeClient(client);
 
 		debugMode = ( 0 != ( getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ) );
 
 		// TODO Clear cache
 		// cwv.clearCache(true);
 
 		// TODO Display splash screen
 		//View rootView = findViewById(android.R.id.content);
 		//rootView.setBackgroundResource(R.drawable.mwater); // ###
 
 		// Launch thread to start app
 		new StartAppTask().execute(getApplicationContext());
 
 		progressDialog = ProgressDialog.show(this, "", "Loading mWater...");
 	}
 
 	private class StartAppTask extends AsyncTask<Context, Void, String> {
 		volatile IOException ex;
 
 		@Override
 		protected void onPostExecute(String url) {
 			if (url == null) {
 				final IOException fex = ex;
 				AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
 				alertDialog.setTitle("Error loading application");
 				alertDialog.setMessage(ex.getLocalizedMessage());
 				alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						MainActivity.this.finish();
 						throw new RuntimeException(fex);
 					}
 				});
 				alertDialog.show();
 			}
 			Log.d(TAG, "Launching url " + url);
 			cwv.loadUrl(url);
 			
 			if (!debugMode) {
 				// Launch thread to download updates
 				AppUpdater.downloadUpdates(getApplicationContext());
 			}
 		}
 
 		@Override
 		protected String doInBackground(Context[] contexts) {
 			Context ctx = contexts[0];
 			
 			// Special case for debugging
 			if (debugMode) {
 				// Setup actionbar plugin
 				ActionBarPlugin.baseIconPath = "file:///android_asset/app";
 
 				String overrideUrl = "file:///android_asset/app/index.html?cordova=true";
 				if (initialHash != null)
 					return overrideUrl + "#" + initialHash;
 				return overrideUrl;
 			}
 
 			// Launch thread to start app
 			try {
 				int appVersion = AppUpdater.getLatestInstalledVersion(ctx);
 				Log.d(TAG, "Found app version " + appVersion);
 				
 				// Get base version
 				String[] assets = ctx.getAssets().list("");
 				int baseVersion = 0;
 				for (String asset : assets) {
 					if (asset.endsWith(".zip")) {
 						try {
 							baseVersion = Integer.parseInt(asset.substring(0, asset.length() - 4));
 						} catch (NumberFormatException nbex) {
 							// Do nothing
 						}
 					}
 				}
 				
 				if (baseVersion == 0) {
 					throw new IOException("Base version missing");
 				}
 				
 				if (appVersion < baseVersion) {
 					// Unzip base html app version 
 					AppUpdater.installAppUpdate(ctx, ctx.getAssets().open(baseVersion + ".zip"));
 					appVersion = AppUpdater.getLatestInstalledVersion(ctx);
 					if (appVersion == 0)
 						throw new IOException("Failed to install base app");
 					Log.d(TAG, "Installed base version " + appVersion);
 				}
 				String installedPath = AppUpdater.getInstalledPath(ctx, appVersion);
 				
 				// Delete old version
 				int oldAppVersion = AppUpdater.getEarliestInstalledVersion(ctx);
 				if (oldAppVersion > 0 && oldAppVersion < appVersion)
 					AppUpdater.deleteInstalledVersion(ctx, oldAppVersion);
 				
 				// Setup actionbar plugin
 				ActionBarPlugin.baseIconPath = installedPath;
 				
 				String url = new File(installedPath).toURI().toURL().toExternalForm();
 				
 				PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
 				url += "index.html?cordova=true&app_version=" + pInfo.versionCode;
 				
 				if (initialHash != null)
 					url += "#" + initialHash;
 				return url;
 			} catch (IOException ex) {
 				this.ex = ex;
 				return null;
 			} catch (NameNotFoundException e) {
 				this.ex = ex;
 				return null;
 			}
 		}
 
 	}
 
 	public void reloadMenu(ActionBarPlugin actionBarPlugin) {
 		Log.d(TAG, "reloadMenu");
 		this.actionBarPlugin = actionBarPlugin;
 		this.supportInvalidateOptionsMenu();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		Log.d(TAG, "createMenu: " + ((actionBarPlugin != null) ? "ok" : "notready"));
 
 		if (actionBarPlugin != null)
 			actionBarPlugin.createMenu(menu);
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == android.R.id.home) {
 			actionBarPlugin.homeClicked();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void cancelLoadUrl() {
 		// TODO Auto-generated method stub
 		Log.d("MainActivity", "cancelLoad");
 	}
 
 	public Activity getActivity() {
 		return this;
 	}
 
 	public Context getContext() {
 		return this;
 	}
 
 	/**
 	 * Called when a message is sent to plugin.
 	 * 
 	 * @param id
 	 *            The message id
 	 * @param data
 	 *            The message data
 	 * @return Object or null
 	 */
 	public Object onMessage(String id, Object data) {
 		if (id == "spinner")
 			progressDialog.dismiss();
 
 		Log.d(TAG, "onMessage(" + id + "," + data + ")");
 		return null;
 	}
 
 	public void setActivityResultCallback(CordovaPlugin plugin) {
 		this.activityResultCallback = plugin;
 	}
 
 	@Override
 	/**
 	 * Called when an activity you launched exits, giving you the requestCode you started it with,
 	 * the resultCode it returned, and any additional data from it.
 	 *
 	 * @param requestCode       The request code originally supplied to startActivityForResult(),
 	 *                          allowing you to identify who this result came from.
 	 * @param resultCode        The integer result code returned by the child activity through its setResult().
 	 * @param data              An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
 	 */
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		super.onActivityResult(requestCode, resultCode, intent);
 		CordovaPlugin callback = this.activityResultCallback;
 		if (callback != null) {
 			callback.onActivityResult(requestCode, resultCode, intent);
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		Log.d(TAG, "onDestroy");
 		super.onDestroy();
 	}
 
 	/**
 	 * Launch an activity for which you would like a result when it finished.
 	 * When this activity exits, your onActivityResult() method will be called.
 	 * 
 	 * @param command
 	 *            The command object
 	 * @param intent
 	 *            The intent to start
 	 * @param requestCode
 	 *            The request code that is passed to callback to identify the
 	 *            activity
 	 */
 	public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
 		this.activityResultCallback = command;
 		this.activityResultKeepRunning = this.keepRunning;
 
 		// If multitasking turned on, then disable it for activities that return
 		// results
 		if (command != null) {
 			this.keepRunning = false;
 		}
 
 		// Start activity
 		super.startActivityForResult(intent, requestCode);
 	}
 
 	public ExecutorService getThreadPool() {
 	    return threadPool;
 	}
 }
