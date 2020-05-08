 /*
  *  Copyright (c) 2011-12, The Linux Foundation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *        * Redistributions of source code must retain the above copyright
  *          notice, this list of conditions and the following disclaimer.
  *        * Redistributions in binary form must reproduce the above copyright
  *          notice, this list of conditions and the following disclaimer in the
  *          documentation and/or other materials provided with the distribution.
  *        * Neither the name of The Linux Foundation nor
  *          the names of its contributors may be used to endorse or promote
  *          products derived from this software without specific prior written
  *          permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NON-INFRINGEMENT ARE DISCLAIMED.    IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.android.le.GattClientTestApp;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 import android.app.Activity;
 import android.bluetooth.BluetoothDevicePicker;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.Looper;
 import android.os.Message;
 import android.text.Editable;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class mainScreen extends Activity {
 
 	public static HashMap<String, DeviceInfo> addrConnectedDeviceMap = null;
 
 	public final String TAG = "mainScreen";
 
 	public static Context mainContext = null;
 
 	public static DeviceInfo selectedLEDevice = null;
 
 	public static final String USER_DEFINED = "UserDefined";
 
 	private static final String THERMOMETER_PROFILE = "THERMOMETER_PROFILE";
 
 	/*
 	 * Make sure that the following are always in sync
 	 */
 	public static final String[] StringServicesDescription = {
 			THERMOMETER_PROFILE, // 1 health and device service
 			USER_DEFINED }; // UserDefined should always be last
 	public static final String[] StringServicesUUID = { "THERMOMETER_PROFILE",
 			USER_DEFINED }; // UserDefined should always be last
 
 	public static HashMap<String, List<String>> profileUUIDMap;
 
 	public static List<String> thermometerProfileUUID = Arrays.asList(
 			"0000180900001000800000805f9b34fb",
 			"0000180a00001000800000805f9b34fb");
 
 	public static List<UUID> selectedUUID = null;
 
 	public static ListView mListView = null;
 
 	public static String UserDefinedUUID = null;
 
 	public static final int DEVICE_SELECTED = 10;
 
 	public static final int SERVICE_FOUND = 11;
 
 	public static EditText maxIntEdit = null;
 
 	public static EditText latencyEdit = null;
 
 	public static EditText timeoutEdit = null;
 
 	public static EditText minIntEdit = null;
 
 	public HandlerThread mHandlerThread = null;
 
 	public EventHandler evHandler = null;
 
 	private IntentFilter inFilter = null;
 
 	private LEThermometerReceiver receiver = null;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.gattclientmain);
 
 		mainContext = this.getApplicationContext();
 		// LEDevice = new DeviceInfo(mainContext);
 
 		if (mHandlerThread == null) {
 			Log.d(TAG, "Create handler thread for batch ");
 			mHandlerThread = new HandlerThread("Bt MNS Transfer Handler",
 					android.os.Process.THREAD_PRIORITY_BACKGROUND);
 			mHandlerThread.start();
 			evHandler = new EventHandler(mHandlerThread.getLooper());
 		}
 		profileUUIDMap = new HashMap<String, List<String>>();
 		profileUUIDMap.put(THERMOMETER_PROFILE, thermometerProfileUUID);
 		selectedUUID = new ArrayList<UUID>();
 
 		final Button buttonConnect = (Button) findViewById(R.id.buttonConnect);
 		buttonConnect.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				populateSelectedUuid();
 				OnClickGetBTDevice();
 			}
 		});
 
 		final Button buttonUpdateTemp = (Button) findViewById(R.id.buttonConnectedDevices);
 		buttonUpdateTemp.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				populateSelectedUuid();
 				Intent in = new Intent();
 				in.setClass(mainScreen.mainContext,
 						com.android.le.GattClientTestApp.ListOfConnectedDevices.class);
 				in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				mainScreen.mainContext.startActivity(in);
 			}
 		});
 
 		final Button buttonConfigure = (Button) findViewById(R.id.buttonConfigure);
 		buttonConfigure.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				int min = Integer.valueOf(minIntEdit.getText().toString());
 				int max = Integer.valueOf(maxIntEdit.getText().toString());
 				int latency = Integer.valueOf(latencyEdit.getText().toString());
 				int timeout = Integer.valueOf(timeoutEdit.getText().toString());
 				if (selectedLEDevice != null) {
 					Log.d(TAG, "Calling setConnectionParams for device : "
 							+ selectedLEDevice.BDevice.getAddress());
 					Log.d(TAG, "Calling setConnectionParams min : " + min
 							+ " max : " + max + " latency : " + latency
 							+ " timeout : " + timeout);
 					/*boolean setConnParamResult = selectedLEDevice.BDevice
 							.setLEConnectionParams(min, max, latency, timeout);
 					Log.d(TAG, "Set connection parameter result : "
 							+ setConnParamResult);*/
 				}
 
 			}
 		});
 
 		mListView = (ListView) findViewById(R.id.someList);
 
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_multiple_choice);
 
 		for (int i = 0; i < StringServicesDescription.length; i++) {
 			adapter.add(StringServicesDescription[i]);
 		}
 
 		mListView.setAdapter(adapter);
 		mListView.setItemsCanFocus(false);
 		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 		mListView.setItemChecked(0, true);
 
 		TextView minIntText = (TextView) findViewById(R.id.textMinInt);
 		minIntEdit = (EditText) findViewById(R.id.editMinInt);
 		minIntEdit.setText("40");
 		TextView maxIntText = (TextView) findViewById(R.id.textMaxInt);
 		maxIntEdit = (EditText) findViewById(R.id.editMaxInt);
 		maxIntEdit.setText("56");
 		TextView latencyText = (TextView) findViewById(R.id.textLatency);
 		latencyEdit = (EditText) findViewById(R.id.editLatency);
 		latencyEdit.setText("0");
 		TextView timeoutText = (TextView) findViewById(R.id.textTimeout);
 		timeoutEdit = (EditText) findViewById(R.id.editTimeout);
 		timeoutEdit.setText("500");
 
 		inFilter = new IntentFilter();
		inFilter.addAction("android.bluetooth.device.action.GATT");
 		this.receiver = new LEThermometerReceiver();
 		Log.d(TAG, "Registering the receiver");
 		this.registerReceiver(this.receiver, inFilter);
 
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.d(TAG, "onDestroy");
 		if (this.receiver != null) {
 			try {
 				this.unregisterReceiver(this.receiver);
 			} catch (Exception e) {
 				Log.e(TAG, "Error while unregistering the receiver");
 			}
 		}
 	}
 
 	public Handler getHandler() {
 		// return evHandler.get;
 		return null;
 	}
 
 	public void OnClickGetBTDevice() {
 		Intent in1 = new Intent(BluetoothDevicePicker.ACTION_LAUNCH);
 		in1.putExtra(BluetoothDevicePicker.EXTRA_NEED_AUTH, false);
 		in1.putExtra(BluetoothDevicePicker.EXTRA_FILTER_TYPE,
 				BluetoothDevicePicker.FILTER_TYPE_ALL);
 		in1.putExtra(BluetoothDevicePicker.EXTRA_LAUNCH_PACKAGE,
 				"com.android.le.GattClientTestApp");
 		in1.putExtra(BluetoothDevicePicker.EXTRA_LAUNCH_CLASS,
 				LEThermometerReceiver.class.getName());
 
 		in1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		this.startActivity(in1);
 	}
 
 	private void populateSelectedUuid() {
 		int checkedItemPos = mListView.getCheckedItemPosition();
 		Log.d(TAG, " Position of CHECKED ITEM ======== " + checkedItemPos);
 		String uuidString = StringServicesUUID[checkedItemPos];
 
 		if (uuidString.equals(mainScreen.USER_DEFINED)) {
 			EditText et = (EditText) findViewById(R.id.uuidText);
 			Editable etText = et.getText();
 			uuidString = etText.toString();
 			Log.d(TAG, "User Id : " + etText);
 			uuidString = "0000" + uuidString + "00001000800000805f9b34fb";
 			UUID selUUID = convertUUIDStringToUUID(uuidString);
 			if (!(selectedUUID.contains(selUUID))) {
 				selectedUUID.add(selUUID);
 			}
 		} else if (uuidString.equals(mainScreen.THERMOMETER_PROFILE)) {
 			for (String uuidStr : thermometerProfileUUID) {
 				UUID selUUID = convertUUIDStringToUUID(uuidStr);
 				if (!(selectedUUID.contains(selUUID))) {
 					selectedUUID.add(selUUID);
 				}
 			}
 		}
 		for (UUID selUuid : selectedUUID) {
 			Log.d(TAG, "selUuid : " + selUuid.toString());
 		}
 	}
 
 	public static UUID convertUUIDStringToUUID(String UUIDStr) {
 		if (UUIDStr.length() != 32) {
 			return null;
 		}
 		String uuidMsB = UUIDStr.substring(0, 16);
 		String uuidLsB = UUIDStr.substring(16, 32);
 
 		if (uuidLsB.equals("800000805f9b34fb")) {
 			// TODO Long is represented as two complement. Fix this later.
 			UUID uuid = new UUID(Long.valueOf(uuidMsB, 16), 0x800000805f9b34fbL);
 			return uuid;
 		} else {
 			UUID uuid = new UUID(Long.valueOf(uuidMsB, 16),
 					Long.valueOf(uuidLsB));
 			return uuid;
 		}
 	}
 
 	public class EventHandler extends Handler {
 		public EventHandler(Looper looper) {
 			super(looper);
 		}
 
 		@Override
 		public void handleMessage(Message msg) {
 			Log.d(TAG, " Handle Message " + msg.what);
 			switch (msg.what) {
 			case DEVICE_SELECTED:
 				break;
 			case SERVICE_FOUND:
 
 				break;
 			}
 		}
 	}
 }
