 package com.mridang.bluetooth;
 
 import java.util.Random;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 import android.provider.Settings;
 import android.util.Log;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 
 /*
  * This class is the main class that provides the widget
  */
 public class BluetoothWidget extends DashClockExtension{
 
 	/* This is the instance of the receiver that deals with bluetooth status */
 	private ToggleReceiver objBluetoothReciver;
 
 	/*
 	 * This class is the receiver for getting bluetooth toggle events
 	 */
 	private class ToggleReceiver extends BroadcastReceiver {
 
 		/*
 		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
 		 */
 		@Override
 		public void onReceive(Context ctxContext, Intent ittIntent) {
 
 			if (ittIntent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
 
 				if (ittIntent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0) == BluetoothAdapter.STATE_ON) {
 
 					Log.v("BluetoothWidget", "Bluetooth enabled");
 					onUpdateData(BluetoothAdapter.STATE_ON);
 					return;
 
 				}
 
 				if (ittIntent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0) == BluetoothAdapter.STATE_OFF) {
 
 					Log.v("BluetoothWidget", "Bluetooth disabled");
 					onUpdateData(BluetoothAdapter.STATE_OFF);
 					return;
 
 				}
 
 			}
 
 			if (ittIntent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
 
 				if (ittIntent.getAction().equals(BluetoothAdapter.STATE_CONNECTED)) {
 
 					Log.v("BluetoothWidget", "Bluetooth connected");
 					onUpdateData(BluetoothAdapter.STATE_CONNECTED);
 					return;
 
 				}
 
 				if (ittIntent.getAction().equals(BluetoothAdapter.STATE_DISCONNECTED)) {
 
 					Log.v("BluetoothWidget", "Bluetooth disconnected");
 					onUpdateData(BluetoothAdapter.STATE_DISCONNECTED);
 					return;
 
 				}
 
 			}
 
 		}
 
 	}
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onInitialize(boolean)
 	 */
 	@Override
 	protected void onInitialize(boolean booReconnect) {
 
 		super.onInitialize(booReconnect);
 
 		if (objBluetoothReciver != null) {
 			try {
 
 				Log.d("BluetoothWidget", "Unregistered any existing status receivers");
 				unregisterReceiver(objBluetoothReciver);
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		}
 
 		IntentFilter itfIntents = new IntentFilter((BluetoothAdapter.ACTION_STATE_CHANGED));
 		itfIntents.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
 		itfIntents.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
 
 		objBluetoothReciver = new ToggleReceiver();
 		registerReceiver(objBluetoothReciver, itfIntents);
 		Log.d("BluetoothWidget", "Registered the status receivers");
 
 	}
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
 	 */
 	public void onCreate() {
 
 		super.onCreate();
 		Log.d("BluetoothWidget", "Created");
 		BugSenseHandler.initAndStartSession(this, "17259530");
 
 	}
 
 	/*
 	 * @see
 	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
 	 * (int)
 	 */
 	@Override
	protected void onUpdateData(int arg0) {
 
 		Log.d("BluetoothWidget", "Fetching bluetooth connectivity information");
 		ExtensionData edtInformation = new ExtensionData();
		edtInformation.visible(false);
 
 		try {
 
 			Log.d("BluetoothWidget", "Checking if the bluetooth is on");
 			if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
 
 				Log.d("BluetoothWidget", "Bluetooth is on");
				edtInformation.visible(arg0 == BluetoothAdapter.
 						STATE_CONNECTED ? true : PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("always", true));
 				edtInformation.clickIntent(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
 				edtInformation.status(BluetoothAdapter.getDefaultAdapter().getName());
				edtInformation.expandedBody(getString(arg0 == BluetoothAdapter.
 						STATE_CONNECTED ? R.string.connected : R.string.disconnected));
 
 			} else {
 				Log.d("BluetoothWidget", "Bluetooth is off");
 			}
 
 			if (new Random().nextInt(5) == 0) {
 
 				PackageManager mgrPackages = getApplicationContext().getPackageManager();
 
 				try {
 
 					mgrPackages.getPackageInfo("com.mridang.donate", PackageManager.GET_META_DATA);
 
 				} catch (NameNotFoundException e) {
 
 					Integer intExtensions = 0;
 
 					for (PackageInfo pkgPackage : mgrPackages.getInstalledPackages(0)) {
 
 						intExtensions = intExtensions + (pkgPackage.applicationInfo.packageName.startsWith("com.mridang.") ? 1 : 0); 
 
 					}
 
 					if (intExtensions > 1) {
 
 						edtInformation.visible(true);
 						edtInformation.clickIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=com.mridang.donate")));
 						edtInformation.expandedTitle("Please consider a one time purchase to unlock.");
 						edtInformation.expandedBody("Thank you for using " + intExtensions + " extensions of mine. Click this to make a one-time purchase or use just one extension to make this disappear.");
 						setUpdateWhenScreenOn(true);
 
 					}
 
 				}
 
 			} else {
 				setUpdateWhenScreenOn(false);
 			}
 
 		} catch (Exception e) {
 			Log.e("BluetoothWidget", "Encountered an error", e);
 			BugSenseHandler.sendException(e);
 		}
 
 		edtInformation.icon(R.drawable.ic_dashclock);
 		publishUpdate(edtInformation);
 		Log.d("BluetoothWidget", "Done");
 
 	}
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
 	 */
 	public void onDestroy() {
 
 		super.onDestroy();
 
 		if (objBluetoothReciver != null) {
 
 			try {
 
 				Log.d("BluetoothWidget", "Unregistered the status receiver");
 				unregisterReceiver(objBluetoothReciver);
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		}
 
 		Log.d("BluetoothWidget", "Destroyed");
 		BugSenseHandler.closeSession(this);
 
 	}
 
 }
