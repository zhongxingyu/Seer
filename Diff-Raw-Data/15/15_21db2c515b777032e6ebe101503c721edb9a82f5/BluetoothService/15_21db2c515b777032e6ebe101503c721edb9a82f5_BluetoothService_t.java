 package com.sw802f12.bt;
 
 import java.io.IOException;
 import java.util.UUID;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.content.Context;
 import android.util.Log;
 import android.widget.Toast;
 
 public class BluetoothService {
 	private static final String TAG = "SW8.bt";
 	private static final UUID MY_UUID = UUID.fromString("1fc95650-9836-11e1-a8b0-0800200c9a66");
 	
 	private Context context;
 	private BluetoothAdapter mBluetoothAdapter;
 	
 	public BluetoothService(Context context, BluetoothAdapter btAdapter) {
 		this.context = context;
 		mBluetoothAdapter = btAdapter;
 	}
 	
 	public synchronized void start() {
 		AcceptThread aThread = new AcceptThread();
 		aThread.start();
 	}
 	
 	public synchronized void connect(BluetoothDevice bd) {
 		ConnectThread cThread = new ConnectThread(bd);
 		cThread.start();
 		cThread.cancel();
 	}
 
 	private class AcceptThread extends Thread {
 	    private final BluetoothServerSocket mmServerSocket;
 	 
 	    public AcceptThread() {
 	        // Use a temporary object that is later assigned to mmServerSocket,
 	        // because mmServerSocket is final
 	        BluetoothServerSocket tmp = null;
 	        try {
 	            // MY_UUID is the app's UUID string, also used by the client code
 	            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("ServerTest", MY_UUID);
 	        } catch (IOException e) { }
 	        mmServerSocket = tmp;
 	    }
 	 
 	    public void run() {
 	    	Log.d(TAG, "Starting server thread.");
 	        BluetoothSocket socket = null;
 	        // Keep listening until exception occurs or a socket is returned
 	        while (true) {
 	            try {
 	                socket = mmServerSocket.accept();
 	            } catch (IOException e) {
	            	Log.d(TAG, "Socket exception: " + e.getMessage());
 	                break;
 	            }
	            
 	            // If a connection was accepted
 	            if (socket != null) {
	            	Log.d(TAG, "BT connection OK. Starting worker.");
 	                // Do work to manage the connection (in a separate thread)
 	                manageConnectedSocket(socket);
 	                try {
	                	Log.d(TAG, "Closing connection...");
 						mmServerSocket.close();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 	                break;
 	            }
 	        }
 	    }
 	 
 	    private void manageConnectedSocket(BluetoothSocket socket) {
 			// TODO Auto-generated method stub
 	    	Toast.makeText(context, "Jeg er server.", Toast.LENGTH_SHORT).show();
 		}
 
 		/** Will cancel the listening socket, and cause the thread to finish */
 	    public void cancel() {
 	        try {
 	            mmServerSocket.close();
 	        } catch (IOException e) { }
 	    }
 	}
 
 	private class ConnectThread extends Thread {
 	    private final BluetoothSocket mmSocket;
 	    private final BluetoothDevice mmDevice;
 	 
 	    public ConnectThread(BluetoothDevice device) {
 	        // Use a temporary object that is later assigned to mmSocket,
 	        // because mmSocket is final
 	        BluetoothSocket tmp = null;
 	        mmDevice = device;
 	 
 	        // Get a BluetoothSocket to connect with the given BluetoothDevice
 	        try {
 	            // MY_UUID is the app's UUID string, also used by the server code
 	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
 	        } catch (IOException e) { }
 	        mmSocket = tmp;
 	    }
 	 
 	    public void run() {
 	    	Log.d(TAG, "Starting client thread.");
 	        // Cancel discovery because it will slow down the connection
 	        mBluetoothAdapter.cancelDiscovery();
 	 
 	        try {
 	            // Connect the device through the socket. This will block
 	            // until it succeeds or throws an exception
 	            mmSocket.connect();
 	        } catch (IOException connectException) {
	        	Log.d(TAG, "Failed to connect: " + connectException.getMessage());
 	            // Unable to connect; close the socket and get out
 	            try {
	            	Log.d(TAG, "Closing connection.");
 	                mmSocket.close();
 	            } catch (IOException closeException) { }
 	            return;
 	        }
	        
	        Log.d(TAG, "Managing connection, yes...");
 	        // Do work to manage the connection (in a separate thread)
 	        manageConnectedSocket(mmSocket);
 	    }
 	 
 	    private void manageConnectedSocket(BluetoothSocket mmSocket2) {
 			// TODO Auto-generated method stub
 	    	Toast.makeText(context, "Jeg er client.", Toast.LENGTH_SHORT).show();
 		}
 
 		/** Will cancel an in-progress connection, and close the socket */
 	    public void cancel() {
 	        try {
 	            mmSocket.close();
 	        } catch (IOException e) { }
 	    }
 	}
 }
