 /*
  * Copyright 2011, The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
  
 package com.afjk01.tool.btnfctagwriter;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.IntentFilter.MalformedMimeTypeException;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.Ndef;
 import android.nfc.tech.NdefFormatable;
 import android.os.ParcelUuid;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.Set;
 import java.util.UUID;
 
 public class MainActivity extends Activity {
     private static final String TAG = "btnfctagwriter";
     private boolean mWriteMode = false;
     NfcAdapter mNfcAdapter;
 //    EditText mNote;
 
     PendingIntent mNfcPendingIntent;
     IntentFilter[] mWriteTagFilters;
     IntentFilter[] mNdefExchangeFilters;
 
     // Return Intent extra
     public static String EXTRA_DEVICE_ADDRESS = "device_address";
 
     // Member fields
     private BluetoothAdapter mBtAdapter;
     private ArrayAdapter<String> mPairedDevicesArrayAdapter;
     private ArrayAdapter<String> mNewDevicesArrayAdapter;
    
     private BluetoothDevice mBluetoothDevice;
 	private AlertDialog			m_dlg;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 //		setContentView(R.layout.activity_main);
 
 		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

         // Handle all of our received NFC intents in this activity.
         mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                 new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
 
         // Intent filters for reading a note from a tag or exchanging over p2p.
         IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
         try {
             ndefDetected.addDataType("text/plain");
         } catch (MalformedMimeTypeException e) { }
         mNdefExchangeFilters = new IntentFilter[] { ndefDetected };
 
         // Intent filters for writing to a tag
         IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
         mWriteTagFilters = new IntentFilter[] { tagDetected };
         
        
         //-- from bluetooth chat
 
         // Setup the window
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.device_list);
 
         // Set result CANCELED in case the user backs out
         setResult(Activity.RESULT_CANCELED);
 
         // Initialize array adapters. One for already paired devices and
         // one for newly discovered devices
         mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
         mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
 
         // Find and set up the ListView for paired devices
         ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
         pairedListView.setAdapter(mPairedDevicesArrayAdapter);
         pairedListView.setOnItemClickListener(mDeviceClickListener);
 
         // Find and set up the ListView for newly discovered devices
         
         // Register for broadcasts when a device is discovered
         IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
         this.registerReceiver(mReceiver, filter);
 
         // Register for broadcasts when discovery has finished
         filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
         this.registerReceiver(mReceiver, filter);
 
         // Get the local Bluetooth adapter
         mBtAdapter = BluetoothAdapter.getDefaultAdapter();
 
         // Bluetoothが無効な場合、有効にする。
         if( !mBtAdapter.isEnabled() ){
        	Toast.makeText(this, "Bluetooth設定をONにしてアプリを立ち上げてください", Toast.LENGTH_SHORT).show();
         	this.finish();
         }
         
         // Get a set of currently paired devices
         Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
 
         // If there are paired devices, add each one to the ArrayAdapter
         if (pairedDevices.size() > 0) {
             findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
             for (BluetoothDevice device : pairedDevices) {
                 mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
             }
         } else {
             String noDevices = getResources().getText(R.string.none_paired).toString();
             mPairedDevicesArrayAdapter.add(noDevices);
         }
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 //		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
 
         // Make sure we're not doing discovery anymore
         if (mBtAdapter != null) {
             mBtAdapter.cancelDiscovery();
         }
 
         // Unregister broadcast listeners
         this.unregisterReceiver(mReceiver);
     }
  
     // The on-click listener for all devices in the ListViews
     private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
 
 
 		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
             // Cancel discovery because it's costly and we're about to connect
             mBtAdapter.cancelDiscovery();
 
             // Get the device MAC address, which is the last 17 chars in the View
             String info = ((TextView) v).getText().toString();
             String address = info.substring(info.length() - 17);
             
             writeTagOfAddressDevice(address);
 
         }
 
 		private void writeTagOfAddressDevice(String address) {
 
 	        // Get a set of currently paired devices
 	        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
 
 	        // If there are paired devices, add each one to the ArrayAdapter
 	        if (pairedDevices.size() > 0) {
 	            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
 	            
 	            mBluetoothDevice = mBtAdapter.getRemoteDevice(address);
                 disableNdefExchangeMode();
                 enableTagWriteMode();
 
                 // for debug start
                 String deviceStr = "";
                 deviceStr += "Name: " + mBluetoothDevice.getName();
                 deviceStr += "\nAddress: " + mBluetoothDevice.getAddress();
 //                deviceStr += "\nClass: " + mBluetoothDevice.getBluetoothClass().getClass();
                 deviceStr += "\nClass: 0x" + Integer.toHexString(mBluetoothDevice.getBluetoothClass().getDeviceClass());
                 deviceStr += "\nMajorClass: 0x" + Integer.toHexString(mBluetoothDevice.getBluetoothClass().getMajorDeviceClass());
                 ParcelUuid[] uuids = mBluetoothDevice.getUuids();
                 ByteBuffer uuidByteBuf = ByteBuffer.allocate(uuids.length*2);
                 for( int i=0; i<uuids.length;i++){
                 	UUID uuid = uuids[i].getUuid();
                 	deviceStr += "\nUUID " + i + ": " + uuid.toString();
                 }
                 // for debug end
                 
                 m_dlg = new AlertDialog.Builder(MainActivity.this).setTitle(getResources().getText(R.string.write_to_tag).toString())
                 		.setMessage(deviceStr+getServices())
                         .setOnCancelListener(new DialogInterface.OnCancelListener() {
                             @Override
                             public void onCancel(DialogInterface dialog) {
                                 disableTagWriteMode();
                                 enableNdefExchangeMode();
                             }
                         }).create();
                 m_dlg.show();
 	     
 	        }	
 		}
     };
 
 
     // The BroadcastReceiver that listens for discovered devices and
     // changes the title when discovery is finished
     private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
 
             // When discovery finds a device
             if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                 // Get the BluetoothDevice object from the Intent
                 BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                 // If it's already paired, skip it, because it's been listed already
                 if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                     mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                 }
             // When discovery is finished, change the Activity title
             } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                 setProgressBarIndeterminateVisibility(false);
                 setTitle(R.string.select_device);
                 if (mNewDevicesArrayAdapter.getCount() == 0) {
                     String noDevices = getResources().getText(R.string.none_found).toString();
                     mNewDevicesArrayAdapter.add(noDevices);
                 }
             }
         }
     };
     @Override
     protected void onResume() {
         super.onResume();
         // Sticky notes received from Android
         if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
             setIntent(new Intent()); // Consume this intent.
         }
         enableNdefExchangeMode();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         mNfcAdapter.disableForegroundNdefPush(this);
     }
     byte[] toBytes(int a) {
         byte[] bs = new byte[4];
         bs[3] = (byte) (0x000000ff & (a));
         bs[2] = (byte) (0x000000ff & (a >>> 8));
         bs[1] = (byte) (0x000000ff & (a >>> 16));
         bs[0] = (byte) (0x000000ff & (a >>> 24));
         return bs;
     }
     @Override
     protected void onNewIntent(Intent intent) {
         // NDEF exchange mode
         if (!mWriteMode && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
             NdefMessage[] msgs = getNdefMessages(intent);
 //            promptForContent(msgs[0]);
         }
 
         // Tag writing mode
         if (mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
             Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
             
             
             // bluetoothDeviceからの情報取得
             byte[] oobOptionalDataLength ={(byte)0x21,(byte)0x00};
             //---Bluetooth Device Address
             String addressStr = mBluetoothDevice.getAddress();
             String[] addressBuff = addressStr.split(":");
             ByteBuffer addressByteBuf = ByteBuffer.allocate(6);
             for( int i = 5;i >= 0; i--){
             	addressByteBuf.put((byte)Integer.parseInt(addressBuff[i],16));
             }
             byte[] Address = addressByteBuf.array();
             //---Local Name
             byte[] EIR_DataType1 = {(byte)0x09};
             byte[] LocalName = mBluetoothDevice.getName().getBytes();
             byte[] EIR_DataLength1  = { (byte)(LocalName.length+1) };
             //---Class of Device
             int servicesVal = getServiesVal() & 0xFFFF0000;
             servicesVal = servicesVal >> 16;
             int majoreClass = mBluetoothDevice.getBluetoothClass().getMajorDeviceClass();
             int minorClass = mBluetoothDevice.getBluetoothClass().getDeviceClass() & 0xFF;
             majoreClass = majoreClass >> 8;
             
             byte[] EIR_DataLength2 = {(byte)0x04};
             byte[] EIR_DataType2 = {(byte)0x0D};
 //            byte[] DeviceClass = {(byte)0x04,(byte)0x04,(byte)0x20};//0x20:ServiceClass=Audio 0x04:Major Device class = Audio/Video 0x04:Minor Device Class = Wearable Handset Device
             byte[] DeviceClass = {(byte)minorClass,(byte)majoreClass,(byte)servicesVal};//0x20:ServiceClass=Audio 0x04:Major Device class = Audio/Video 0x04:Minor Device Class = Wearable Handset Device
             //---UUID List
             byte[] EIR_DataType3 = {(byte)0x03};
 //            byte[] UUID_list = {(byte)0x1E,(byte)0x11,(byte)0x0B,(byte)0x11};
             ParcelUuid[] uuids = mBluetoothDevice.getUuids();
 
             ByteBuffer uuidByteBuf = ByteBuffer.allocate(uuids.length*2);
             for( int i=0; i<uuids.length;i++){
             	UUID uuid = uuids[i].getUuid();
             	String uuidStr = uuid.toString().substring(6,8);
             	uuidByteBuf.put((byte)Integer.parseInt(uuidStr,16));
             	uuidStr = uuid.toString().substring(4,6);
             	uuidByteBuf.put((byte)Integer.parseInt(uuidStr,16));
             }
             
             byte[] UUID_list = uuidByteBuf.array();
             byte[] EIR_DataLength3 = {(byte)(UUID_list.length+1)};
             
             int oobLen = oobOptionalDataLength.length+Address.length+EIR_DataLength1.length+EIR_DataType1.length+LocalName.length+
             		EIR_DataLength2.length+EIR_DataType2.length+DeviceClass.length+EIR_DataLength3.length+EIR_DataType3.length+UUID_list.length;
             ByteBuffer byteBuf = ByteBuffer.allocate(oobLen);
             
             //--OOB全体長再計算
             byte[] oobLenBytes = toBytes(oobLen);
             oobOptionalDataLength[0] = oobLenBytes[3];
             oobOptionalDataLength[1] = oobLenBytes[2];
             byteBuf.put(oobOptionalDataLength);
             byteBuf.put(Address);
             byteBuf.put(EIR_DataLength1);
             byteBuf.put(EIR_DataType1);
             byteBuf.put(LocalName);
             byteBuf.put(EIR_DataLength2);
             byteBuf.put(EIR_DataType2);
             byteBuf.put(DeviceClass);
             byteBuf.put(EIR_DataLength3);
             byteBuf.put(EIR_DataType3);
             byteBuf.put(UUID_list);
             
             NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "application/vnd.bluetooth.ep.oob".getBytes(),
                     new byte[] {}, byteBuf.array());
             
             NdefMessage ndefMessage = new NdefMessage(new NdefRecord[] {
                 textRecord
             });
            
             writeTag(ndefMessage, detectedTag);
         }
     }
 
     private View.OnClickListener mTagWriter = new View.OnClickListener() {
         @Override
         public void onClick(View arg0) {
             // Write to a tag for as long as the dialog is shown.
             disableNdefExchangeMode();
             enableTagWriteMode();
 
             new AlertDialog.Builder(MainActivity.this).setTitle("Touch tag to write")
                     .setOnCancelListener(new DialogInterface.OnCancelListener() {
                         @Override
                         public void onCancel(DialogInterface dialog) {
                             disableTagWriteMode();
                             enableNdefExchangeMode();
                         }
                     }).create().show();
         }
     };
 
     NdefMessage[] getNdefMessages(Intent intent) {
         // Parse the intent
         NdefMessage[] msgs = null;
         String action = intent.getAction();
         if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                 || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
             Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
             if (rawMsgs != null) {
                 msgs = new NdefMessage[rawMsgs.length];
                 for (int i = 0; i < rawMsgs.length; i++) {
                     msgs[i] = (NdefMessage) rawMsgs[i];
                 }
             } else {
                 // Unknown tag type
                 byte[] empty = new byte[] {};
                 NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                 NdefMessage msg = new NdefMessage(new NdefRecord[] {
                     record
                 });
                 msgs = new NdefMessage[] {
                     msg
                 };
             }
         } else {
             Log.d(TAG, "Unknown intent.");
             finish();
         }
         return msgs;
     }
 
     private void enableNdefExchangeMode() {
         mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
     }
 
     private void disableNdefExchangeMode() {
         mNfcAdapter.disableForegroundNdefPush(this);
         mNfcAdapter.disableForegroundDispatch(this);
     }
 
     private void enableTagWriteMode() {
         mWriteMode = true;
         IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
         mWriteTagFilters = new IntentFilter[] {
             tagDetected
         };
         mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
     }
 
     private void disableTagWriteMode() {
         mWriteMode = false;
         mNfcAdapter.disableForegroundDispatch(this);
     }
 
     boolean writeTag(NdefMessage message, Tag tag) {
     	byte[] msg = message.toByteArray();
         int size = message.toByteArray().length;
         m_dlg.dismiss();
         try {
             Ndef ndef = Ndef.get(tag);
             if (ndef != null) {
                 ndef.connect();
 
                 if (!ndef.isWritable()) {
                     toast("Tag is read-only.");
                     return false;
                 }
                 if (ndef.getMaxSize() < size) {
                     toast("Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size
                             + " bytes.");
                     return false;
                 }
 
                 ndef.writeNdefMessage(message);
                 toast("Wrote message to pre-formatted tag.");
                 return true;
             } else {
                 NdefFormatable format = NdefFormatable.get(tag);
                 if (format != null) {
                     try {
                         format.connect();
                         format.format(message);
                         toast("Formatted tag and wrote message");
                         return true;
                     } catch (IOException e) {
                         toast("Failed to format tag.");
                         return false;
                     }
                 } else {
                     toast("Tag doesn't support NDEF.");
                     return false;
                 }
             }
         } catch (Exception e) {
             toast("Failed to write tag");
         }
 
         return false;
     }
 
     private void toast(String text) {
         Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
     }
     
     private int getServiesVal(){
     	int serviceVal = 0;
     	String result = "\nServices:";
     	
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.AUDIO)){
     		result += "\nAUDIO";
     		serviceVal += android.bluetooth.BluetoothClass.Service.AUDIO;
     	}
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.CAPTURE)){
     		result += "\nCAPTURE";
     		serviceVal += android.bluetooth.BluetoothClass.Service.CAPTURE;
     	}
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.INFORMATION)){
     		result += "\nINFORMATION";
     		serviceVal += android.bluetooth.BluetoothClass.Service.INFORMATION;
     	}
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.LIMITED_DISCOVERABILITY)){
     		result += "\nLIMITED_DISCOVERABILITY";
     		serviceVal += android.bluetooth.BluetoothClass.Service.LIMITED_DISCOVERABILITY;
     	}
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.NETWORKING)){
     		result += "\nNETWORKING";
     		serviceVal += android.bluetooth.BluetoothClass.Service.NETWORKING;
     	}
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.OBJECT_TRANSFER)){
     		result += "\nOBJECT_TRANSFER";
     		serviceVal += android.bluetooth.BluetoothClass.Service.OBJECT_TRANSFER;
     	}
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.POSITIONING)){
     		result += "\nPOSITIONING";
     		serviceVal += android.bluetooth.BluetoothClass.Service.POSITIONING;
     	}
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.RENDER)){
     		result += "\nRENDER";
     		serviceVal += android.bluetooth.BluetoothClass.Service.RENDER;
     	}
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.TELEPHONY)){
     		result += "\nTELEPHONY";
     		serviceVal += android.bluetooth.BluetoothClass.Service.TELEPHONY;
     	}
     	
     	return serviceVal;
     }
     
     private String getServices(){
     	int serviceVal = 0;
     	String result = "\nServices:";
     	
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.AUDIO)){
     		result += "\nAUDIO";
     		serviceVal += android.bluetooth.BluetoothClass.Service.AUDIO;
     	}
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.CAPTURE)){
     		result += "\nCAPTURE";
     		serviceVal += android.bluetooth.BluetoothClass.Service.CAPTURE;
     	}
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.INFORMATION)){
     		result += "\nINFORMATION";
     		serviceVal += android.bluetooth.BluetoothClass.Service.INFORMATION;
     	}
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.LIMITED_DISCOVERABILITY)){
     		result += "\nLIMITED_DISCOVERABILITY";
     		serviceVal += android.bluetooth.BluetoothClass.Service.LIMITED_DISCOVERABILITY;
     	}
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.NETWORKING)){
     		result += "\nNETWORKING";
     		serviceVal += android.bluetooth.BluetoothClass.Service.NETWORKING;
     	}
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.OBJECT_TRANSFER)){
     		result += "\nOBJECT_TRANSFER";
     		serviceVal += android.bluetooth.BluetoothClass.Service.OBJECT_TRANSFER;
     	}
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.POSITIONING)){
     		result += "\nPOSITIONING";
     		serviceVal += android.bluetooth.BluetoothClass.Service.POSITIONING;
     	}
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.RENDER)){
     		result += "\nRENDER";
     		serviceVal += android.bluetooth.BluetoothClass.Service.RENDER;
     	}
     	if(mBluetoothDevice.getBluetoothClass().hasService(android.bluetooth.BluetoothClass.Service.TELEPHONY)){
     		result += "\nTELEPHONY";
     		serviceVal += android.bluetooth.BluetoothClass.Service.TELEPHONY;
     	}
     	
     	return result + "\n0x" + Integer.toHexString(serviceVal)+ "\nb" + Integer.toBinaryString(serviceVal);
     }
 
 }
