 package com.group5.android.fd;
 
 import java.io.Serializable;
 
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.IBinder;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.group5.android.fd.activity.FdPreferenceActivity;
 import com.group5.android.fd.activity.NewSessionActivity;
 import com.group5.android.fd.activity.TaskListActivity;
 import com.group5.android.fd.activity.dialog.LoginDialog;
 import com.group5.android.fd.entity.AbstractEntity;
 import com.group5.android.fd.entity.TableEntity;
 import com.group5.android.fd.entity.TaskEntity;
 import com.group5.android.fd.entity.UserEntity;
 import com.group5.android.fd.helper.HttpHelper;
 import com.group5.android.fd.helper.HttpRequestAsyncTask;
 import com.group5.android.fd.helper.LoginRequestHelper;
 import com.group5.android.fd.helper.PreferencesHelper;
 import com.group5.android.fd.helper.ScanHelper;
 import com.group5.android.fd.helper.SyncHelper;
 import com.group5.android.fd.helper.UriStringHelper;
 import com.group5.android.fd.service.TaskUpdaterService;
 import com.group5.android.fd.service.TaskUpdaterServiceReceiver;
import com.group5.android.fd.service.TaskUpdaterService.TaskUpdaterBinder;
 
 /**
  * The first activity / screen of the app. This will check for user identity,
  * allow user to navigate around. Nothing fancy here.
  * 
  * @author Nguyen Huu Ha
  * 
  */
 public class Main extends Activity implements OnClickListener,
 		OnDismissListener, OnCancelListener,
 		HttpRequestAsyncTask.OnHttpRequestAsyncTaskCaller,
 		SyncHelper.SyncHelperCaller,
 		android.content.DialogInterface.OnClickListener, ServiceConnection {
 
 	final public static String EXTRA_DATA_NAME_USER_OBJ = "userObj";
 
 	final protected static int DIALOG_LOGIN_ID = 1;
 
 	protected Button m_vwNewSession;
 	protected Button m_vwTasks;
 	protected DbAdapter m_dbAdapter;
 	protected UserEntity m_user = new UserEntity();
 
 	protected boolean m_triedAutoLogin = false;
 	protected Dialog m_loginDialog = null;
 	protected boolean m_loginDialogLoggedIn = false;
 	protected boolean m_loginDialogCanceled = false;
 	protected Dialog m_zxingAlertDialog = null;
 	protected Dialog m_syncDialog = null;
 
 	protected BroadcastReceiver m_broadcastReceiverForNewTask = null;
 	protected HttpRequestAsyncTask m_hrat = null;
 	protected SyncHelper m_sh = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		initLayout();
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		requireLoggedIn();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 
 		if (m_hrat != null) {
 			m_hrat.dismissProgressDialog();
 		}
 
 		if (m_sh != null) {
 			m_sh.dismissProgressDialog();
 		}
 
 		unbindService(this);
 
 		if (m_broadcastReceiverForNewTask != null) {
 			unregisterReceiver(m_broadcastReceiverForNewTask);
 			m_broadcastReceiverForNewTask = null;
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putSerializable(Main.EXTRA_DATA_NAME_USER_OBJ, m_user);
 
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 
 		Serializable tmp = savedInstanceState
 				.getSerializable(Main.EXTRA_DATA_NAME_USER_OBJ);
 		if (tmp != null && tmp instanceof UserEntity) {
 			m_user = (UserEntity) tmp;
 		}
 	}
 
 	/**
 	 * Initiates the layout (inflate from a layout resource named
 	 * activity_main). And then maps all the object properties with their view
 	 * instance. Finally, initiates required listeners on those views.
 	 */
 	protected void initLayout() {
 		setContentView(R.layout.activity_main);
 
 		m_vwNewSession = (Button) findViewById(R.id.btnNewSession);
 		m_vwTasks = (Button) findViewById(R.id.btnTasks);
 
 		m_vwNewSession.setOnClickListener(this);
 		m_vwTasks.setOnClickListener(this);
 	}
 
 	/**
 	 * Notify user if SD card is not available Images will not display
 	 */
 
 	protected void isExternalStorageAvailable() {
 		boolean isExternalStorageAvailable = Environment.MEDIA_MOUNTED
 				.equals(Environment.getExternalStorageState());

 		if (!isExternalStorageAvailable) {
 			AlertDialog.Builder b = new AlertDialog.Builder(this);
 			b.setPositiveButton(R.string.ok, this);
 			b.setCancelable(false);
 			b.setMessage(R.string.imagehelper_sdcard_unavailable);
 			b.show();
 		}
 	}
 
 	/**
 	 * Enables and disables views based on user's permissions
 	 */
 	protected void setLayoutEnabled() {
 		m_vwNewSession.setEnabled(m_user.canNewOrder);
 		m_vwTasks.setEnabled(m_user.canUpdateTask);
 
 		// reset the Tasks button
 		m_vwTasks.setText(R.string.tasks);
 		m_vwTasks
 				.setCompoundDrawablesWithIntrinsicBounds(getResources()
 						.getDrawable(android.R.drawable.star_big_off), null,
 						null, null);
 
 		if (m_user.canUpdateTask && m_broadcastReceiverForNewTask == null) {
 
 			// start our service
 			Intent service = new Intent(this, TaskUpdaterService.class);
 			bindService(service, this, Context.BIND_AUTO_CREATE);
 
 			// register the receiver to update the Tasks button
 			m_broadcastReceiverForNewTask = new TaskUpdaterServiceReceiver(this) {
 
 				protected int m_newTasks = 0;
 
 				@Override
 				protected void onReceive(Context context, TaskEntity task) {
 					m_vwTasks.setCompoundDrawablesWithIntrinsicBounds(
 							getResources().getDrawable(
 									android.R.drawable.star_big_on), null,
 							null, null);
 					m_vwTasks.setText(getString(R.string.tasks) + " ("
 							+ ++m_newTasks + ")");
 				}
 			};
 		}
 	}
 
 	/**
 	 * Starts the synchronize process. This method makes use of
 	 * {@link SyncHelper} (which actually is an <code>AsyncTask</code>). The
 	 * task will get executed immediately.
 	 */
 	protected void sync() {
 		new SyncHelper(this).execute();
 	}
 
 	/**
 	 * Checks database and display a friendly dialog to ask user to synchronize
 	 * data if he / she hasn't done that before. This method uses a simple
 	 * <code>AlertDialog</code> to do its job. The created dialog has a single
 	 * positive button and it's binded to this object also.
 	 * 
 	 * @see #onClick(DialogInterface, int)
 	 */
 	protected void syncSuggestion() {
 		if (SyncHelper.needSync(this)) {
 			AlertDialog.Builder b = new AlertDialog.Builder(this);
 			b.setPositiveButton(R.string.sync_data, this);
 			b.setTitle(R.string.sync_data);
 			b.setMessage(R.string.sync_data_now);
 
 			m_syncDialog = b.show();
 		}
 	}
 
 	/**
 	 * Tries to login automatically with the information set in preferences
 	 * before. This method also tries to out-smart itself by checking a flag to
 	 * make sure it only runs once (otherwise, it will try to auto login again
 	 * and again and again). When the auto login request finishes, it will call
 	 * {@link #requireLoggedIn()} to validate the information.
 	 * 
 	 * @return true if an auto login request is being sent in the background or
 	 *         false if it didn't do anything
 	 */
 	protected boolean doAutoLogin() {
 		if (m_triedAutoLogin) {
 			// only try to auto login once
 			return false;
 		}
 		m_triedAutoLogin = true;
 
 		boolean prefAutoLogin = PreferencesHelper.getBoolean(this,
 				R.string.pref_auto_login);
 		String prefUsername = PreferencesHelper.getString(this,
 				R.string.pref_username);
 		String prefPassword = PreferencesHelper.getString(this,
 				R.string.pref_password);
 
 		if (prefAutoLogin) {
 			// the auto login feature is enabled
 			if (prefUsername != null && prefUsername.length() > 0
 					&& prefPassword != null && prefPassword.length() > 0) {
 				// valid username and password are found
 
 				new LoginRequestHelper(this, prefUsername, prefPassword) {
 
 					@Override
 					protected void onLoginSuccess(JSONObject jsonObject) {
 						requireLoggedIn();
 					}
 
 					@Override
 					protected void onLoginError(JSONObject jsonObject) {
 						createErrorDialog(m_errorMessage).show();
 					}
 
 					@Override
 					public void onDismiss(DialogInterface dialog) {
 						if (m_errorMessage != null
 								&& m_errorMessage
 										.equals(HttpHelper.ERROR_MESSAGE_CONNECT_TIMEOUT)) {
 							// we got a timeout message, shouldn't trigger
 							// requireLoggedIn() anymore
 						} else {
 							requireLoggedIn();
 						}
 					}
 				}.execute();
 
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Does various things to make sure user is logged in and has the proper
 	 * permissions. At first it will check for previously logged in information.
 	 * And then it tries to do an auto login. If the auto login request is sent,
 	 * it will also stop (the auto login procedure will trigger another call to
 	 * this method once it's done). If the auto login request wasn't sent, a
 	 * verification request will set sent to entry-point/user-info. If the
 	 * server validates our information, that's good news. Otherwise, it will
 	 * display and error message and / or show a {@link LoginDialog} and let
 	 * user identify himself / herself.
 	 * 
 	 * @see #doAutoLogin()
 	 */
 	protected void requireLoggedIn() {
 		// setup the buttons correctly
 		setLayoutEnabled();
 
 		if (m_user.isLoggedIn()) {
 			// the user is logged in, nothing to do here...
 			return;
 		}
 
 		if (doAutoLogin()) {
 			// wait for auto login...
 			// it should call us back soon
 			return;
 		}
 
		new HttpRequestAsyncTask(this, UriStringHelper
				.buildUriString("user-info")) {
 
 			@Override
 			protected void onSuccess(JSONObject jsonObject, Object processed) {
 				try {
 					JSONObject user = jsonObject.getJSONObject("user");
 					m_user.parse(user);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 				if (!m_user.isLoggedIn()) {
 					// display the login dialog again!
 					showLoginDialog();
 				} else {
 					// logged in
 					Toast.makeText(Main.this,
 							getString(R.string.welcome_back, m_user.username),
 							Toast.LENGTH_SHORT).show();
 
 					// check if SD card is inserted
 					isExternalStorageAvailable();
 
 					// setup the buttons
 					setLayoutEnabled();
 
 					// suggest sync
 					syncSuggestion();
 				}
 			}
 
 			@Override
 			protected void onError(JSONObject jsonObject, String message) {
 				showLoginDialog();
 			}
 		}.execute();
 	}
 
 	/**
 	 * Wrapper method: only trigger a {@link LoginDialog} if the dialog hasn't
 	 * been canceled before. This's a little bit tricky to understand but...
 	 * please try to wrap your head arond it. It makes sense.
 	 */
 	protected void showLoginDialog() {
 		// only if user hasn't canceled it before
 		if (!m_loginDialogCanceled) {
 			showDialog(Main.DIALOG_LOGIN_ID);
 		}
 	}
 
 	/**
 	 * Sends a logout request and re-validates the user after that.
 	 * 
 	 * @see #requireLoggedIn()
 	 */
 	protected void doLogout() {
 		String logoutUri = UriStringHelper.buildUriString("logout", "index");
 
 		new HttpRequestAsyncTask(this, logoutUri, m_user.csrfToken, null) {
 
 			@Override
 			protected void onSuccess(JSONObject jsonObject, Object preProcessed) {
 				m_loginDialogCanceled = false;
 				m_user.resetEverything();
 				requireLoggedIn();
 			}
 		}.execute();
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.btnNewSession:
 			Intent intentNewSession = new Intent(this, NewSessionActivity.class);
 			intentNewSession.putExtra(Main.EXTRA_DATA_NAME_USER_OBJ, m_user);
 			startActivity(intentNewSession);
 			break;
 		case R.id.btnTasks:
 			Intent intentTask = new Intent(this, TaskListActivity.class);
 			intentTask.putExtra(Main.EXTRA_DATA_NAME_USER_OBJ, m_user);
 			startActivity(intentTask);
 			break;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		MenuItem mniLogin = menu.findItem(R.id.menu_main_login);
 		if (m_user.isLoggedIn()) {
 			mniLogin.setTitle(getString(R.string.logout) + ": "
 					+ m_user.username);
 		} else {
 			mniLogin.setTitle(R.string.login);
 		}
 
 		menu.findItem(R.id.menu_main_sync).setEnabled(m_user.isLoggedIn());
 		menu.findItem(R.id.menu_main_scan).setEnabled(m_user.isLoggedIn());
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem arg0) {
 		switch (arg0.getItemId()) {
 		case R.id.menu_main_login:
 			if (!m_user.isLoggedIn()) {
 				showDialog(Main.DIALOG_LOGIN_ID);
 			} else {
 				doLogout();
 			}
 
 			return true;
 		case R.id.menu_main_sync:
 			sync();
 			return true;
 		case R.id.menu_main_preferences:
 			Intent preferencesIntent = new Intent(this,
 					FdPreferenceActivity.class);
 			startActivity(preferencesIntent);
 
 			return true;
 		case R.id.menu_main_scan:
 			m_zxingAlertDialog = IntentIntegrator.initiateScan(this);
 			return true;
 		}
 
 		return false;
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog = null;
 
 		switch (id) {
 		case DIALOG_LOGIN_ID:
 			dialog = new LoginDialog(this);
 			dialog.setOnDismissListener(this);
 			dialog.setOnCancelListener(this);
 
 			m_loginDialog = dialog;
 			break;
 		}
 
 		return dialog;
 	}
 
 	@Override
 	public void onDismiss(DialogInterface dialog) {
 		if (dialog == m_loginDialog) {
 			LoginDialog loginDialog = (LoginDialog) dialog;
 
 			if (loginDialog.isLoggedIn()) {
 				// if the login dialog annouces logged in
 				// we will do an additional check, just to make sure
 				m_loginDialogLoggedIn = true;
 				requireLoggedIn();
 			} else {
 				// it looks like the user canceled the login dialog
 				// display a "friendly" reminder
 				Toast.makeText(this, R.string.you_must_login_to_use_this_app,
 						Toast.LENGTH_SHORT);
 			}
 		}
 	}
 
 	@Override
 	public void onCancel(DialogInterface dialog) {
 		if (dialog == m_loginDialog) {
 			m_loginDialogCanceled = true;
 		}
 	}
 
 	@Override
 	public void onClick(DialogInterface dialog, int which) {
 		if (dialog == m_syncDialog) {
 			sync();
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (m_zxingAlertDialog == null) {
 			// only check for scan result if no alert dialog was issued the last
 			// time we initiated it
 			new ScanHelper(this, requestCode, resultCode, data,
 					new Class[] { TableEntity.class }) {
 
 				@Override
 				protected void onMatched(AbstractEntity entity) {
 					TableEntity table = (TableEntity) entity;
 
 					Intent newSessionIntent = new Intent(Main.this,
 							NewSessionActivity.class);
 					newSessionIntent
 							.putExtra(
 									NewSessionActivity.EXTRA_DATA_NAME_TABLE_OBJ,
 									table);
 					newSessionIntent.putExtra(Main.EXTRA_DATA_NAME_USER_OBJ,
 							m_user);
 					newSessionIntent.putExtra(
 							NewSessionActivity.EXTRA_DATA_NAME_USE_SCANNER,
 							true);
 
 					startActivity(newSessionIntent);
 				}
 
 				@Override
 				protected void onInvalid() {
 					// TODO Auto-generated method stub
 
 				}
 			};
 		}
 	}
 
 	@Override
 	public void addHttpRequestAsyncTask(HttpRequestAsyncTask hrat) {
 		if (m_hrat != null && m_hrat != hrat) {
 			m_hrat.dismissProgressDialog();
 		}
 
 		m_hrat = hrat;
 	}
 
 	@Override
 	public void removeHttpRequestAsyncTask(HttpRequestAsyncTask hrat) {
 		if (m_hrat == hrat) {
 			m_hrat = null;
 		}
 	}
 
 	@Override
 	public void addSyncHelper(SyncHelper sh) {
 		if (m_sh != null && m_sh != sh) {
 			m_sh.dismissProgressDialog();
 		}
 
 		m_sh = sh;
 	}
 
 	@Override
 	public void removeSyncHelper(SyncHelper sh) {
 		if (m_sh == sh) {
 			m_sh = null;
 		}
 	}
 
 	@Override
 	public void onServiceConnected(ComponentName name, IBinder service) {
 		if (service instanceof TaskUpdaterService.TaskUpdaterBinder) {
 			TaskUpdaterService.TaskUpdaterBinder binder = (TaskUpdaterBinder) service;
 			binder.getService().startWorking(null, 0,
 					FdConfig.NEW_TASK_INTERVAL_SLOWER);
 		}
 	}
 
 	@Override
 	public void onServiceDisconnected(ComponentName arg0) {
 		// TODO Auto-generated method stub
 
 	}
 }
