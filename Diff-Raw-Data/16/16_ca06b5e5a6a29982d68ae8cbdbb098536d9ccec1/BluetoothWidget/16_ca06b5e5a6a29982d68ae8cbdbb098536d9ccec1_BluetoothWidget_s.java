 package com.mridang.bluetooth;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
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
	private SRChangeReceiver objBluetoothReciver;
 
 	/*
 	 * This class is the receiver for getting bluetooth toggle events
 	 */
	private class SRChangeReceiver extends BroadcastReceiver {
 
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
 
		objBluetoothReciver = new SRChangeReceiver();
		registerReceiver(objBluetoothReciver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		registerReceiver(objBluetoothReciver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
		registerReceiver(objBluetoothReciver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
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
 				edtInformation.visible(true);
 				edtInformation.clickIntent(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
 				edtInformation.status(BluetoothAdapter.getDefaultAdapter().getName());
 				edtInformation.expandedBody(getString(arg0 == BluetoothAdapter.STATE_CONNECTED ? R.string.connected : R.string.disconnected));
 
 			} else {
 				Log.d("BluetoothWidget", "Bluetooth is off");
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
 		Log.d("BluetoothWidget", "Destroyed");
 		BugSenseHandler.closeSession(this);
 
 	}
 
 }
