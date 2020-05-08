 package com.mro.bluetooth;
 
 import java.io.IOException;
 import java.util.UUID;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.util.Log;
 
 public class BluetoothClient extends Thread {
 
 	public final static String TAG = BluetoothClient.class.getSimpleName();
 
 	private BluetoothSocket clientSocket;
 	private BluetoothSocketHandler handler;
 	private BluetoothAdapter adapter;
 
 	public BluetoothClient(BluetoothAdapter adapter, BluetoothDevice device,
 			UUID uuid, BluetoothSocketHandler handler) {

 		this.handler = handler;
 
 		try {
 			clientSocket = device.createRfcommSocketToServiceRecord(uuid);
 		} catch (IOException e) {
 			Log.d(TAG, "Failed to create socket", e);
 		}
 	}
 
 	public void run() {
 
 		adapter.cancelDiscovery();
 
 		try {
 			clientSocket.connect();
 		} catch (IOException e) {
 			Log.e(TAG, "Could not connect to a server", e);
 
 			return;
 		}
 
 		handler.handleSocket(clientSocket);
 	}
 
 	public void stopClient() {
 		try {
 			clientSocket.close();
 		} catch (IOException e) {
 			Log.e(TAG, "", e);
 		}
 	}
 }
