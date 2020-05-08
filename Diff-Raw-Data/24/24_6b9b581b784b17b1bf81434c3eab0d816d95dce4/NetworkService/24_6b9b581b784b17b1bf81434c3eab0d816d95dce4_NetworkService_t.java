 package dk.aau.sw802f12.proto3.network;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.UUID;
 
 import dk.aau.sw802f12.proto3.util.MusicRegistry;
 import dk.aau.sw802f12.proto3.util.User;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.content.Context;
 import android.util.Log;
 import android.widget.Toast;
 
 public class NetworkService {
 	private static final String TAG = "sw8.BT";
 	private static final String DISC = "Disconnect";
 	static final UUID MY_UUID = UUID
 			.fromString("99E67F40-9849-11E1-A8B0-0800200C9A66");
 	private BluetoothAdapter mBluetoothAdapter;
 	private String lastFMUser;
 	Context mContext;
 	private ConnectedThread ct;
 	private boolean isClient;
 	
 
 	public NetworkService(Context context, BluetoothAdapter btAdapter) {
 		mBluetoothAdapter = btAdapter;
 		mContext = context;
 	}
 
 	public synchronized void start(String name, String user) {
 		AcceptThread aThread = new AcceptThread();
 		aThread.start();
 		MusicRegistry mr;
 		try {
 			mr = MusicRegistry.getInstance();
 			User u = mr.createUser("local", user);
 			mr.updateDB(u);
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		Toast.makeText(mContext, "Started server with name: " + name,
 				Toast.LENGTH_SHORT);
 		isClient = false;
 	}
 
 	public synchronized void connect(BluetoothDevice bd, String user) {
 		ConnectThread cThread = new ConnectThread(bd);
 		lastFMUser = user;
 		Log.d(TAG, "Starting thread.");
 		cThread.start();
 		isClient = true;
 	}
 
 	public void stop() {
 		if (!isClient) {
 			ct.cancel();
 			Toast.makeText(mContext, "Stopped server.", Toast.LENGTH_SHORT);
 		}
 	}
 
 	public void disc() {
 		if (isClient) {
 			String text = DISC;
 			ct.write(text.getBytes());
 			ct.cancel();
 			Toast.makeText(mContext, "Disconnected.", Toast.LENGTH_SHORT);
 		}
 	}
 
 	private class AcceptThread extends Thread {
 		private final BluetoothServerSocket mmServerSocket;
 
 		public AcceptThread() {
 			// Use a temporary object that is later assigned to mmServerSocket,
 			// because mmServerSocket is final
 			Log.d(TAG, "Acceptthread created");
 
 			BluetoothServerSocket tmp = null;
 			try {
 				// MY_UUID is the app's UUID string, also used by the client
 				// code
 				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
 						"BTserviceTest", MY_UUID);
 			} catch (IOException e) {
 			}
 			mmServerSocket = tmp;
 		}
 
 		public void run() {
 			mBluetoothAdapter.cancelDiscovery();
 			Log.d(TAG, "AcceptThread started");
 			BluetoothSocket socket = null;
 			// Keep listening until exception occurs or a socket is returned
 			while (true) {
 				try {
 					Log.d(TAG, "Trying to accept connection");
 					socket = mmServerSocket.accept();
 				} catch (IOException e) {
 					Log.d(TAG, "Caught exception - breaking");
 					break;
 				}
 				// If a connection was accepted
 				if (socket != null) {
 					Log.d(TAG, "Calling manageconnection");
 
 					// Do work to manage the connection (in a separate thread)
 					manageConnectedSocket(socket);
 					try {
 						mmServerSocket.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 					break;
 				}
 			}
 		}
 
 		private void manageConnectedSocket(BluetoothSocket socket) {
 			Log.d(TAG, "Connection as server OK - transmitting data");
 			ct = new ConnectedThread(socket);
 			ct.start();
 		}
 
 		/** Will cancel the listening socket, and cause the thread to finish */
 		public void cancel() {
 			try {
 				mmServerSocket.close();
 			} catch (IOException e) {
 			}
 		}
 	}
 
 	private class ConnectThread extends Thread {
 		private final BluetoothSocket mmSocket;
 
 		public ConnectThread(BluetoothDevice device) {
 			Log.d(TAG, "ConnectThread started.");
 			// Use a temporary object that is later assigned to mmSocket,
 			// because mmSocket is final
 			BluetoothSocket tmp = null;
 			// Get a BluetoothSocket to connect with the given BluetoothDevice
 			try {
 				// MY_UUID is the app's UUID string, also used by the server
 				// code
 				Log.d(TAG, "Trying to create socket.");
 				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
 			} catch (IOException e) {
 				Log.d(TAG, "Could not create socket.");
 			}
 			mmSocket = tmp;
 			Log.d(TAG, "CONNECTHREAD created.");
 		}
 
 		public void run() {
 			// Cancel discovery because it will slow down the connection
 			mBluetoothAdapter.cancelDiscovery();
 			try {
 				// Connect the device through the socket. This will block
 				// until it succeeds or throws an exception
 				Log.d(TAG, "Connecting to BT peer.");
 				mmSocket.connect();
 				Log.d(TAG, "Connection OK");
 			} catch (IOException connectException) {
 				Log.d(TAG, "Failed to connect" + connectException.getMessage());
 				// Unable to connect; close the socket and get out
 				try {
 					mmSocket.close();
 				} catch (IOException closeException) {
 				}
 				return;
 			}
 			// Do work to manage the connection (in a separate thread)
 			manageConnectedSocket(mmSocket);
 		}
 
 		private void manageConnectedSocket(BluetoothSocket socket) {
 			Log.d(TAG, "Connected.");
 			ct = new ConnectedThread(socket);
 			ct.start();
 			ct.write(lastFMUser.getBytes());
 			Log.d(TAG, "Client wrote " + lastFMUser);
 		}
 
 		/** Will cancel an in-progress connection, and close the socket */
 		public void cancel() {
 			try {
 				mmSocket.close();
 			} catch (IOException e) {
 			}
 		}
 	}
 
 	private class ConnectedThread extends Thread {
 		private final BluetoothSocket mmSocket;
 		private final InputStream mmInStream;
 		private final OutputStream mmOutStream;
 
 		public ConnectedThread(BluetoothSocket socket) {
 			mmSocket = socket;
 			InputStream tmpIn = null;
 			OutputStream tmpOut = null;
 			// Get the input and output streams, using temp objects because
 			// member streams are final
 			try {
 				tmpIn = socket.getInputStream();
 				tmpOut = socket.getOutputStream();
 			} catch (IOException e) {
 			}
 			mmInStream = tmpIn;
 			mmOutStream = tmpOut;
 		}
 
 		public void run() {
 			byte[] buffer = new byte[1024]; // buffer store for the stream
 			int bytes; // bytes returned from read()
 			// Keep listening to the InputStream until an exception occurs
 			while (true) {
 				try {
 					// Read from the InputStream
 					bytes = mmInStream.read(buffer);
 					if ((new String(buffer, 0, bytes)).equals(DISC)) {
 						try {
 							MusicRegistry mr = MusicRegistry.getInstance();
 							mr.removeUser(mmSocket.getRemoteDevice()
 									.getAddress());
 						} catch (InstantiationException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					} else {
 						try {
 							MusicRegistry mr = MusicRegistry.getInstance();
 							User u = mr.createUser(mmSocket.getRemoteDevice()
 									.getAddress());
 							u.setLastfmName(new String(buffer, 0, bytes));
 							mr.updateDB(u);
 						} catch (InstantiationException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 					Log.d(TAG, "Server read: " + new String(buffer, 0, bytes));
 				} catch (IOException e) {
 					break;
 				}
 			}
 		}
 
 		/* Call this from the main activity to send data to the remote device */
 		public void write(byte[] bytes) {
 			try {
 				mmOutStream.write(bytes);
 			} catch (IOException e) {
 			}
 		}
 
 		/* Call this from the main activity to shutdown the connection */
 		public void cancel() {
 			try {
 				mmSocket.close();
 			} catch (IOException e) {
 			}
 		}
 	}
 
 }
