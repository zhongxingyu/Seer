 /*
  * wePoker: Play poker with your friends, wherever you are!
  * Copyright (C) 2012, The AmbientTalk team.
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 2 of the License, or (at your
  * option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package edu.vub.at.nfcpoker.ui;
 
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.ActivityInfo;
 import android.net.NetworkInfo;
 import android.net.NetworkInfo.State;
 import android.net.Uri;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.nfc.NdefMessage;
 import android.nfc.NfcAdapter;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import edu.vub.at.commlib.CommLib;
 import edu.vub.at.nfcpoker.Constants;
 import edu.vub.at.nfcpoker.QRNFCFunctions;
 import edu.vub.at.nfcpoker.R;
 import edu.vub.at.nfcpoker.settings.Settings;
 
 public class QRJoinerActivity extends Activity {
 	
 	IntentFilter intentFilter;
 
 	protected boolean currentlyJoining = false;
 	
 	private BroadcastReceiver wifiChangeReceiver = new BroadcastReceiver() {
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			Log.d("wePoker - QRJoiner", "Received intent " + intent);
 			WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
 
 			if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
 				NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
 				WifiInfo wifiInfo = wm.getConnectionInfo();
 				if (netInfo == null || wifiInfo == null || !currentlyJoining)
 					return;
 				if (netInfo.getState() == State.CONNECTED) {
 					if (wifiName.equals(wifiInfo.getSSID())) {
 						publishProgress("Joining game!");
 						startClientActivity();
 					} else {
 						attemptToJoin(wm);
 					}
 				}
 			}
 			if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
 				int new_wifi_status = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
 				
 				if (new_wifi_status == WifiManager.WIFI_STATE_DISABLED) {
 					publishProgress("Enabling Wireless...");
 					wm.setWifiEnabled(true);
 				}
 				if (new_wifi_status == WifiManager.WIFI_STATE_ENABLED && currentlyJoining) {
 					attemptToJoin(wm);
 				}
 			}
 		}
 
 		public void attemptToJoin(WifiManager wm) {
 			publishProgress("Connecting to network...");
 			
 			// SSIDs are stored as "\"Network Name\""
 			String mangledName = '"' + wifiName + '"';
 			
 			for (WifiConfiguration config : wm.getConfiguredNetworks()) {
 				if (config.SSID.equals(mangledName)) {
 					Log.d("wePoker - QRJoiner", "Found preconfigured network " + wifiName);
 					wm.enableNetwork(config.networkId, true);
 					return;
 				}
 			}
 			
 			if (wifiPassword != null && (!wifiPassword.isEmpty()) && !wifiPassword.equals("********")) {
 				// If we have the password
 				WifiConfiguration config = new WifiConfiguration();
 				config.SSID = mangledName;
 				config.preSharedKey = '"' + wifiPassword + '"';
 				config.hiddenSSID = true;
 				config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
 				config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
 				config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
 				config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
 				config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
 				config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
 
 				int id = wm.addNetwork(config);
 				Log.d("wePoker - QRJoiner", "Connecting to network " + wifiName + ": " + id);
 				boolean ret = wm.enableNetwork(id, true);
 				Log.d("wePoker - QRJoiner", "enableNetwork returned " + ret);
 			} else {
 				Log.d("wePoker - QRJoiner", "Asking user to connect manually");
 				new AlertDialog.Builder(QRJoinerActivity.this)
 				    .setMessage("Please connect to the network '" + wifiName + "' manually and scan the barcode again.")
 				    .setCancelable(false)
 				    .show();
 			}
 		}
 	};
 
 	protected String wifiServerIp = "";
 	protected int wifiPort = CommLib.SERVER_PORT;
 	protected String wifiName = "";
 	protected String wifiPassword = "";
 	protected boolean wifiIsDedicated = false;
 	protected boolean wifiIsServer = false;
 	protected String wifiBroadcast = "";
 	protected boolean wifiWifiDirect = false;
 	
 	protected Uri lastScannedNfcUri;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		Settings.loadSettings(this);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         setContentView(R.layout.activity_splash);
         
         findViewById(R.id.CreateGame).setVisibility(View.GONE);
         findViewById(R.id.JoinGame).setVisibility(View.GONE);
         
         handleIntent(getIntent());
         
         intentFilter = new IntentFilter();
 		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
 		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
 
         String joinText = getResources().getString(R.string.qr_code_join_confirmation, wifiName);
         publishProgress(joinText);
 		
         joinServer();
     }
     
     private void joinServer() {
 		currentlyJoining = true;
 		registerReceiver(wifiChangeReceiver, intentFilter);
     }
     
 
     protected void publishProgress(String string) {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.ConnectionInformationProgressBar);
        progressBar.setVisibility(View.VISIBLE);
     	TextView progressTxt = (TextView) findViewById(R.id.ConnectionInformation);
     	progressTxt.setText(string);
    	Log.d("WePoker - QRJoiner", "Progress update: " + string);
 	}
 
 	protected void startClientActivity() {
 		Intent i = new Intent(this, ClientActivity.class);
 		i.putExtra(Constants.INTENT_SERVER_IP, wifiServerIp);
 		i.putExtra(Constants.INTENT_PORT, wifiPort);
 		i.putExtra(Constants.INTENT_WIFI_NAME, wifiName);
 		i.putExtra(Constants.INTENT_WIFI_PASSWORD, wifiPassword);
 		i.putExtra(Constants.INTENT_IS_DEDICATED, wifiIsDedicated);
 		i.putExtra(Constants.INTENT_IS_SERVER, wifiIsServer);
 		i.putExtra(Constants.INTENT_BROADCAST, wifiBroadcast);
 		i.putExtra(Constants.INTENT_WIFI_DIRECT, wifiWifiDirect);
 		startActivity(i);
 		finish();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(wifiChangeReceiver );
 	}
 	
 	private void handleIntent(Intent intent) {
 		NfcAdapter mAdapter = NfcAdapter.getDefaultAdapter(this);
 		Uri uri = null;
 		if ((mAdapter != null) && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
 				Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
 		        // only one message sent during the beam
 		        NdefMessage msg = (NdefMessage) rawMsgs[0];
 		        uri = QRNFCFunctions.getUriFromNdefMessage(msg);
 		} else {
 			uri = getIntent().getData();
 		}
 		
 		if (uri == null) {
 			Log.v("wePoker - QRJoiner", "URI is null");
 			return;
 		}
 		
 	    wifiName = uri.getQueryParameter(Constants.INTENT_WIFI_NAME);
 	    wifiServerIp = uri.getQueryParameter(Constants.INTENT_SERVER_IP);
 	    if (uri.getQueryParameter(Constants.INTENT_PORT) != null) {
 	    	try {
 	    		wifiPort = Integer.parseInt(uri.getQueryParameter(Constants.INTENT_PORT));
 	    	} catch (Exception e) {}
 	    }
 	    wifiName = uri.getQueryParameter(Constants.INTENT_WIFI_NAME);
 	    wifiPassword = uri.getQueryParameter(Constants.INTENT_WIFI_PASSWORD);
 	    if (uri.getQueryParameter(Constants.INTENT_IS_DEDICATED) != null) {
 	    	wifiIsDedicated = uri.getQueryParameter(Constants.INTENT_IS_DEDICATED).equals("true");
 	    }
 	    if (uri.getQueryParameter(Constants.INTENT_IS_SERVER) != null) {
 	    	wifiIsServer = uri.getQueryParameter(Constants.INTENT_IS_SERVER).equals("true");
 	    }
 	    wifiBroadcast = uri.getQueryParameter(Constants.INTENT_BROADCAST);
 	    if (uri.getQueryParameter(Constants.INTENT_WIFI_DIRECT) != null) {
 	    	wifiWifiDirect = uri.getQueryParameter(Constants.INTENT_WIFI_DIRECT).equals("true");
 	    }
 	    Log.v("wePoker - QRJoiner", "New wifi details. ip: "+wifiServerIp+" port: "+wifiPort+" name:"+wifiName+" dedicated:"+wifiIsDedicated);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		registerReceiver(wifiChangeReceiver, intentFilter);
 		handleIntent(getIntent());
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_qrjoiner, menu);
         return true;
     }
 	
 }
