 package com.example.myhealthapp.conn;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.TimeUnit;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
 import android.content.Intent;
import android.content.IntentFilter;
 import android.os.AsyncTask;
 import android.util.Log;
import android.widget.ArrayAdapter;
 import android.widget.Toast;
 
 public class BluetoothHandler extends AsyncTask<Void, Void, Void> {
 	Activity activity;
	ArrayAdapter<String> mArrayAdapter;
 	BluetoothAdapter mBluetoothAdapter;
 	BluetoothServerSocket mBluetoothSocket;
 	LinkedList<String> bluetoothresults;
 	UUID UUID_RFCOMM_GENERIC = UUID
 			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
 
 	public BluetoothHandler(Activity a) {
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		bluetoothresults = new LinkedList<String>();
 		activity = a;
			
 		try {
 			mBluetoothSocket = mBluetoothAdapter
 					.listenUsingRfcommWithServiceRecord("Bluetooth service",
 							UUID_RFCOMM_GENERIC);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 	
 	/*
 	 * Needs to be overridden
 	 */
 	public boolean setData(String s){
 		return false;
 	}
 	
 	public void addToBluetoothResults(String result){
 		bluetoothresults.add(result);		
 	}
 	
 	public boolean hasNewResult(){
 		if(bluetoothresults.isEmpty()){
 			return false;
 		}
 		return true;
 	}
 	
 	public String getNewResult(){
 		return bluetoothresults.pop();		
 	}
 	
 	public Boolean DeviceHasBluetooth() {
 		Boolean b = true;
 
 		if (mBluetoothAdapter == null) {
 			b = false;
 		}
 		return b;
 	}
 
 	public void Accept() throws IOException {
 		mBluetoothSocket.accept();
 	}
 
 	public void EnableBluetooth() {
 		if (!mBluetoothAdapter.isEnabled()) {
 			Intent enableBtIntent = new Intent(
 					BluetoothAdapter.ACTION_REQUEST_ENABLE);
 			activity.startActivityForResult(enableBtIntent, 0);
 		}
 	}
 	
 	public void DisableBluetooth(){
 		if (mBluetoothAdapter.isEnabled()) {
 		    mBluetoothAdapter.disable(); 
 		} 
 	}
 
 	public void MakeDiscoverable(Activity a) {
 		Intent discoverableIntent = new Intent(
 				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 		discoverableIntent.putExtra(
 				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
 		activity.startActivity(discoverableIntent);
 	}
 
 	public BluetoothAdapter getAdapter() {
 		return this.mBluetoothAdapter;
 	}
 
 	public Set<BluetoothDevice> GetDevices(BluetoothAdapter adapter) {
 		Set<BluetoothDevice> devices = adapter.getBondedDevices();
 		return devices;
 	}
 
 	@Override
 	protected Void doInBackground(Void... params) {
 		return null;
 	}
 
 	public void manageConnectedSocket(BluetoothSocket socket) {
 
 	}
 	
 	public void cancelConnection(){
 		
 	}
 
 	class ConnectedThread extends Thread {
 
 		private final BluetoothSocket mmSocket;
 		private final InputStream mmInput;
 		private final OutputStream mmOutput;
 
 		public ConnectedThread(BluetoothSocket socket) {
 			mmSocket = socket;
 			InputStream tmpIn = null;
 			OutputStream tmpOut = null;
 
 			try {
 				tmpIn = socket.getInputStream();
 				tmpOut = socket.getOutputStream();
 			} catch (IOException e) {
 			}
 
 			mmInput = tmpIn;
 			mmOutput = tmpOut;
 		}
 
 		public void run() {
 			byte[] buffer = new byte[1024];
 			int bytes;
 
 			while (true) {
 				Log.i("DEBUG", "Trying to read");
 				try {
 					bytes = mmInput.read(buffer);
					Log.i("DEBUG", "bytes: "+bytes);
 					String string = new String(buffer);
					string = string.substring(0, bytes);
 					Log.d("DEBUG", "Received : " + string);
 					
 					addToBluetoothResults(string);
 				}
 
 				catch (IOException e) {
 					break;
 				}
 			}
 		}
 
 		public void write(byte[] bytes) {
 			Log.i("DEBUG", "starting sending shit");
 			try {
 				mmOutput.write(bytes);
				
				Log.i("DEBUG", new String(bytes) + "Sent");
				mmOutput.flush();				
 			}
 
 			catch (IOException e) {
 				Log.i("DEBUG", "Bytes Not Sent");
 				cancel();
 			}
 		}
 
 		public void cancel() {
 			try {
 				Log.i("DEBUG", "Attempting to Close ConnectedThread Socket");
 	            mmInput.close();
 	            mmOutput.close();
 				mmSocket.close();
 				Log.i("DEBUG", "ConnectedThread Socket Closed");
 			} catch (IOException e) {
 				Log.i("DEBUG", "ConnectedThread Failed To Close");
 			}
 		}
 
 	}
 
 }
