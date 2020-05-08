 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package riddimon.android.asianetautologin;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import riddimon.android.asianetautologin.LoginService.LoginBinder;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.support.v4.content.LocalBroadcastManager;
 import android.text.TextUtils;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class CredentialActivity extends Activity implements OnClickListener {
 	private AlertDialog mElsewhereDialog;
 	private Button mLogin;
 	private Button mLogout;
 	private Button mSave;
 	private CheckBox mShowNotification;
 	private EditText mUsername;
 	private EditText mPassword;
 	private Spinner mSsid;
 	private ProgressDialog mProgressDialog;
 	private Spinner mStaySignedInFor;
 
 	private boolean mBound = false;
 	private boolean mRegistered = false;
 	private boolean mLoggedIn = false;
 	private boolean mLoggedInElsewhere = false;
 	private int[] mStaySignedInNumbers = null;
 	private List<String> mStaySignedInItems;
 	private LoginService mLoginService;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.credential_screen);
 
 		mStaySignedInNumbers = getResources().getIntArray(R.array.signed_in_for);
 		mStaySignedInItems = new ArrayList<String>();
 		for (int s : mStaySignedInNumbers) {
 			mStaySignedInItems.add(s + " hour" + ((s > 1) ? "s" : ""));
 		}
 		mStaySignedInItems.add(getString(R.string.forever));
 		// get views
 		mUsername = (EditText) findViewById(R.id.et_username);
 		mPassword = (EditText) findViewById(R.id.et_password);
 		mSsid = (Spinner) findViewById(R.id.sp_ssid);
 		mShowNotification = (CheckBox) findViewById(R.id.cb_show);
 		mStaySignedInFor = (Spinner) findViewById(R.id.sp_signedinfor);
 		mLogin = (Button) findViewById(R.id.bt_login);
 		mLogout = (Button) findViewById(R.id.bt_logout);
 		mSave = (Button) findViewById(R.id.bt_save);
 
 		mStaySignedInFor.setAdapter(new ArrayAdapter<String>(this, android.R
 				.layout.simple_spinner_dropdown_item, mStaySignedInItems));
 		mStaySignedInFor.setSelection(mStaySignedInNumbers.length);
 
 		mLogin.setOnClickListener(this);
 		mLogout.setOnClickListener(this);
 		mSave.setOnClickListener(this);
 		refreshNetworkSpinnerIfNecessary();
 
 		String username = SettingsManager.getString(this, SettingsManager
 				.USERNAME, null);
 		String password = SettingsManager.getString(this, SettingsManager
 				.PASSWORD, null);
 		String ssid = SettingsManager.getString(this, SettingsManager
 				.SSID, null);
 		boolean showNotif = SettingsManager.getBoolean(this, SettingsManager
 				.SHOW_NOTIF, true);
 		int staySignedInFor = SettingsManager.getInt(this, SettingsManager
 				.KEEP_ALIVE, -1);
 
 		if (!TextUtils.isEmpty(username)) {
 			mUsername.setText(username);
 		}
 		if (!TextUtils.isEmpty(password)) {
 			mPassword.setText(password);
 		}
 		if (!TextUtils.isEmpty(ssid)) {
 			if (mSsids.size() > 0) {
 				int index = mSsids.indexOf(ssid);
 				mSsid.setSelection(index == -1 ? 0 : index);
 			}
 		}
 
 		int selection = 0;
 		if (staySignedInFor == -1) selection = mStaySignedInNumbers.length;
 		else {
 			for (int i = 0; i < mStaySignedInNumbers.length; i++) {
 				if (mStaySignedInNumbers[i] == staySignedInFor) {
 					selection = i;
 				}
 			}
 		}
 		mStaySignedInFor.setSelection(selection);
 
 		mShowNotification.setChecked(showNotif);
 		enableBroadcastReceiver(true);
 
 		mProgressDialog = new ProgressDialog(this);
 		mProgressDialog.setTitle(R.string.status);
 		mProgressDialog.setMessage(getString(R.string.checking_status));
 		mProgressDialog.show();
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		Intent intent = new Intent(this, LoginService.class);
 		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
 	}
 
     @Override
     protected void onStop() {
         super.onStop();
         // Unbind from the service
         if (isFinishing()) {
         	closeProgressDialog();
         	enableBroadcastReceiver(false);
 	        if (mBound) {
 	            unbindService(mConnection);
 	            mBound = false;
 	        }
         }
     }
 
     @Override
     protected void onDestroy() {
     	super.onDestroy();
     	closeProgressDialog();
     	enableBroadcastReceiver(false);
     }
 
     /** Defines callbacks for service binding, passed to bindService() */
     private ServiceConnection mConnection = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className,
                 IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             LoginBinder binder = (LoginBinder) service;
             mLoginService = binder.getService();
             mBound = true;
             runOnUiThread(new Runnable() {
             	@Override
             	public void run() {
                     checkIfLoggedInAndUpdate();
             	}
             });
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             mBound = false;
         }
     };
 
     private void refreshNetworkSpinnerIfNecessary() {
     	if (mSsids != null && mSsids.size() != 0) return;
 		//get wifi networks
 		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 		List<WifiConfiguration> lwc = wm.getConfiguredNetworks();
 		WifiInfo wi = wm.getConnectionInfo();
 		String wssid = null;
 		if (wi != null && wm.isWifiEnabled()) {
			wssid = wi.getSSID().replace("\"", "");
 		}
 
 		mSsids = new ArrayList<String>();
 		int selection = 0;
 		if (lwc != null) {
 			int i = -1;
 			for (WifiConfiguration wc : lwc) {
 				i++;
 				String ssid = TextUtils.isEmpty(wc.SSID) ? ""
 						: wc.SSID.replace("\"", "");
 				mSsids.add(ssid);
 				if (!TextUtils.isEmpty(wssid) && ssid.equals(wssid)) {
 					selection = i; 
 				}
 			}
 		}
 
 		mSsid.setAdapter(new ArrayAdapter<String>(this, android.R
 				.layout.simple_spinner_dropdown_item, mSsids));
		mSsid.setSelection(selection);
     }
 
     private void enableBroadcastReceiver(boolean enable) {
     	if (enable) {
     		IntentFilter broadcastFilter = new IntentFilter();
     		broadcastFilter.addAction(LoginService.ACTION_LOGIN);
     		broadcastFilter.addAction(LoginService.ACTION_LOGOUT);
     		broadcastFilter.addAction(LoginService.ACTION_KEEP_ALIVE);
 
     		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver
     				, broadcastFilter);
 
     		broadcastFilter = new IntentFilter();
     		broadcastFilter.addAction(ServiceStarter.ACTION_STATE_CHANGE);
     		registerReceiver(mWifiReceiver, broadcastFilter);
     	} else if (mRegistered) {
     		LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
     		unregisterReceiver(mWifiReceiver);
     	}
 		mRegistered = enable;
     }
 
     private void updateLoggedInMode() {
 //    	mUsername.setEnabled(!mLoggedIn);
 //    	mPassword.setEnabled(!mLoggedIn);
 //    	mSsid.setEnabled(!mLoggedIn);
     	mLogin.setEnabled(!mLoggedIn);
     	mLogout.setEnabled(mLoggedIn && !TextUtils.isEmpty(SettingsManager
     			.getString(this, SettingsManager.URL, null)));
 
     	if (mLoggedInElsewhere) {
     		showElsewhereDialog();
     	}
 //    	mSave.setEnabled(!mLoggedIn);
     }
 
     private void showElsewhereDialog() {
     	if (mElsewhereDialog == null || !mElsewhereDialog.isShowing()) {
 			mElsewhereDialog = new AlertDialog.Builder(this).setTitle("Another device")
 					.setMessage("You are already logged in from another device."
 					+ "\n\nPlease logout from all other devices before continuing.")
 					.setNeutralButton(android.R.string.ok
 					, new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 						}
 			}).create();
 	    	mElsewhereDialog.show();
     	}
     }
 
     private void closeProgressDialog() {
     	if (mProgressDialog != null && mProgressDialog.isShowing()) {
     		mProgressDialog.dismiss();
     	}
     }
 
     private void checkIfLoggedInAndUpdate() {
     	if (mBound && mLoginService != null) {
     		(new Thread(new Runnable() {
     			@Override
     			public void run() {
     				mLoggedIn = mLoginService.isLoggedIn();
     				runOnUiThread(new Runnable() {
     					@Override
     					public void run() {
     						updateLoggedInMode();
     						if (!CredentialActivity.this.isFinishing()) {
     							closeProgressDialog();
     						}
     					}
     				});
     			}
     		})).start();
     	}
     }
 
 	private void showLogoutDialog() {
 		DialogInterface.OnClickListener oncl = new DialogInterface
 				.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				if (which == DialogInterface.BUTTON_POSITIVE) {
 					SettingsManager.putBoolean(CredentialActivity.this
 							, SettingsManager.LOG_IN, false);
 					Intent intent = new Intent(LoginService.ACTION_LOGOUT);
 					intent.setClass(CredentialActivity.this, LoginService.class);
 					startService(intent);
 				}
 				dialog.dismiss();
 			}
 		};
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(R.string.logout).setMessage(R.string.logout_message)
 				.setPositiveButton(android.R.string.yes, oncl)
 				.setNegativeButton(android.R.string.no, oncl)
 				.show();
 	}
 
 	private void getAndSaveCredentials() {
 		String un = mUsername.getEditableText().toString();
 		String pwd = mPassword.getEditableText().toString();
 		String ssid = (String) mSsid.getSelectedItem();
 		boolean showNotif = mShowNotification.isChecked();
 		//mStaySignedInFor.getSelectedItem()
 
 		SettingsManager.putString(this, SettingsManager.USERNAME, un);
 		SettingsManager.putString(this, SettingsManager.PASSWORD, pwd);
 		SettingsManager.putString(this, SettingsManager.SSID, ssid);
 		SettingsManager.putBoolean(this, SettingsManager.SHOW_NOTIF, showNotif);
 	}
 
 	private void initiateLogin() {
 		SettingsManager.putBoolean(this, SettingsManager.LOG_IN, true);
 		SettingsManager.putLong(this, SettingsManager.LOG_IN_TIME
 				, System.currentTimeMillis());
 
 		int selection = mStaySignedInFor.getSelectedItemPosition();
 		if (selection == mStaySignedInNumbers.length) selection = -1;
 		else selection = mStaySignedInNumbers[selection];
 		SettingsManager.putInt(this, SettingsManager.KEEP_ALIVE, selection);
 
 		// show login dialog
 		mProgressDialog = new ProgressDialog(this);
 		mProgressDialog.setTitle(R.string.login);
 		mProgressDialog.setMessage(getString(R.string.logging_in));
 		mProgressDialog.show();
 
 		Intent intent = new Intent(LoginService.ACTION_LOGIN);
 		intent.setClass(this, LoginService.class);
 		startService(intent);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()) {
 		case R.id.bt_login:
 			getAndSaveCredentials();
 			initiateLogin();
 			break;
 		case R.id.bt_logout:
 			showLogoutDialog();
 			break;
 		case R.id.bt_save:
 			getAndSaveCredentials();
 			Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT)
 					.show();
 			break;
 		}
 	}
 
 	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent != null) {
 				String action = intent.getAction();
 				final boolean status = intent.getBooleanExtra(LoginService.EX_STATUS
 						, false);
 
 				mLoggedInElsewhere = false;
 				if (action.equals(LoginService.ACTION_LOGIN)) {
 					mLoggedIn = status;
 					mLoggedInElsewhere = intent.getBooleanExtra(LoginService
 								.EX_LOGGED_IN_ELSEWHERE, false);
 				} else if (action.equals(LoginService.ACTION_LOGOUT)) {
 					mLoggedIn = !status;
 				} else if (action.equals(LoginService.ACTION_KEEP_ALIVE)) {
 					mLoggedIn = status;
 				}
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						updateLoggedInMode();
 						closeProgressDialog();
 					}
 				});
 			}
 		}
 	};
 
 	private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction().equals(ServiceStarter.ACTION_STATE_CHANGE)) {
 				NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager
 						.EXTRA_NETWORK_INFO);
 				if (networkInfo.isConnected()) {
 					if (!CredentialActivity.this.isFinishing()) {
 						runOnUiThread(new Runnable() {
 							@Override
 							public void run() {
 								// refresh network spinner
 								refreshNetworkSpinnerIfNecessary();
 							}
 						});
 					}
 				}
 			}
 		}
 	};
 	private List<String> mSsids;
 
 }
