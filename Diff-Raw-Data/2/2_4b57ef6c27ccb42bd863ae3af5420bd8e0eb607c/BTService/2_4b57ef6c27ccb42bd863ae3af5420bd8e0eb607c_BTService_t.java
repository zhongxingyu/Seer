 package fi.local.social.network.btservice;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.UUID;
 
 
 
 import android.app.Service;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.content.BroadcastReceiver;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 
 public class BTService extends Service{
 
 
 	public static final int MSG_REGISTER_CLIENT = 1;
 	public static final int MSG_UNREGISTER_CLIENT = 2;
 	public static final int MSG_SEND_EVENT = 3;
 	public static final int MSG_REC_EVENT = 4;
 	public static final int MSG_NEW_ADDR = 5;
 	public static final int MSG_START_DISCOVERY = 6;
 	public static final int MSG_REC_MESSAGE = 7; 
 	public static final int MSG_START_CONNCETION = 8;
 
 	private BluetoothAdapter mBluetoothAdapter = null;
 
 	private ArrayList<String> devicesAddr;
 
 	/**
 	 * Target we publish for clients to send messages to IncomingHandler.
 	 */
 	final Messenger mMessenger = new Messenger(new IncomingHandler());
 	/** Keeps track of all current registered clients. */
 	public static ArrayList<Messenger> mClients = new ArrayList<Messenger>();
 	
 	private String TAG = "btservice";
 	private IntentFilter intFilter;
 	
 	private static final String NAME = "MobileNeighbour";
 
 	// TODO: Change this
 	// Unique UUID for this application
 	private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
 
 	private int mState = -1;
 	private AcceptThread mAcceptThread;
 	private ConnectThread mConnectThread;
 	private ConnectedThread mConnectedThread;
 	
 	private static final boolean D = true;
 	
 	// Constants that indicate the current connection state
 	public static final int STATE_NONE = 0;       // we're doing nothing
 	public static final int STATE_LISTEN = 1;     // now listening for incoming connections
 	public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
 	public static final int STATE_CONNECTED = 3;  // now connected to a remote device
 
 	/**
 	 * Handler of incoming messages from clients.
 	 */
 	class IncomingHandler extends Handler {
 
 
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_REGISTER_CLIENT:
 				mClients.add(msg.replyTo);
 				break;
 			case MSG_UNREGISTER_CLIENT:
 				mClients.remove(msg.replyTo);
 				break;
 			case MSG_SEND_EVENT:
 				// TODO send to device
 				break;
 			case MSG_START_DISCOVERY:
 				doDiscovery();
 			case MSG_START_CONNCETION:
 				Bundle data = msg.getData();
 				String address = data.getString("address");
 				BluetoothDevice b = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
 				connect(b);
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	}
 	
 	
 	
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mMessenger.getBinder();
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 
 		// is the bluetooth turned on?
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		// turn bt on if it not turned on
 		if(!mBluetoothAdapter.isEnabled())
 			mBluetoothAdapter.enable();
 
 
 		mBluetoothAdapter.startDiscovery();
 		
 
 		// define filter for broadcast
 		intFilter = new IntentFilter();
 		intFilter.addAction(BluetoothDevice.ACTION_FOUND);
 		intFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
 		intFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
 
 
 		// Register for broadcasts when a device is discovered
 		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
 		this.registerReceiver(broadCastReceiver, intFilter);
 		start();
 	}
 
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		mBluetoothAdapter.cancelDiscovery();
 	}
 
 	/**
 	 * Start device discover with the BluetoothAdapter
 	 */
 	private void doDiscovery() {
 		Log.d(TAG , "doDiscovery()");
 
 
 
 		// If we're already discovering, stop it
 		if (mBluetoothAdapter.isDiscovering()) {
 			mBluetoothAdapter.cancelDiscovery();
 		}
 
 		// Request discover from BluetoothAdapter
 		mBluetoothAdapter.startDiscovery();
 	}
 
 	// search for devices binded and not binded
 	private static final BroadcastReceiver broadCastReceiver = new BroadCastReceiverDevices();
 	
 	
 	// send the founded devices to the peopleactivity
 	public static void sendAddrToPeopleActivity(String addr) {
 		System.err.println("addres: " + addr);
 		for(int i = 0; i < mClients.size() ; i++)
 		{
 			Messenger client = mClients.get(i);
 			
 			Bundle b = new Bundle();
 			b.putString("address", addr);
 			Message msg = Message.obtain(null, MSG_NEW_ADDR);
 			msg.setData(b);
 			try {
 				client.send(msg);
 			} catch (RemoteException e) {
 				// TODO mClients.remove(i);?? 
 				e.printStackTrace();
 			}
 		}
 
     }
 	
 	// Cool stuff
 	
 	public synchronized void start() {
 		if (D) Log.d(TAG, "start");
 
 		// Cancel any thread attempting to make a connection
 		if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
 
 		// Cancel any thread currently running a connection
 		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
 
 		// Start the thread to listen on a BluetoothServerSocket
 		if (mAcceptThread == null) {
 			mAcceptThread = new AcceptThread();
 			mAcceptThread.start();
 		}
 		setState(STATE_LISTEN);
 	}
 	
 	
 	private void connectionLost() {
 		setState(STATE_LISTEN);
 
 		// Send a failure message back to the Activity
 		// TODO send to activity
 	}
 	
 	private synchronized void setState(int state) {
 		if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
 		mState = state;
 
 		// Give the new state to the Handler so the UI Activity can update
 		// TODO send to activity
 	}
 	
 	private void connectionFailed() {
 		setState(STATE_LISTEN);
 
 		// Send a failure message back to the Activity
 		// TODO send to activity
 	}
 
 	/**
 	 * Indicate that the connection was lost and notify the UI Activity.
 	 */
 
 	
 	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
 		if (D) Log.d(TAG, "connected");
 
 		// Cancel the thread that completed the connection
 		if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
 
 		// Cancel any thread currently running a connection
 		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
 
 		// Cancel the accept thread because we only want to connect to one device
 		if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
 
 		// Start the thread to manage the connection and perform transmissions
 		mConnectedThread = new ConnectedThread(socket);
 		mConnectedThread.start();
 
 		// Send the name of the connected device back to the UI Activity
 		// TODO send to activity
 
 		setState(STATE_CONNECTED);
 	}
 	
 	public synchronized void connect(BluetoothDevice device) {
 		if (D) Log.d(TAG, "connect to: " + device);
 
 		// Cancel any thread attempting to make a connection
 		if (mState == STATE_CONNECTING) {
 			if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
 		}
 
 		// Cancel any thread currently running a connection
 		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
 
 		// Start the thread to connect with the given device
 		mConnectThread = new ConnectThread(device);
 		mConnectThread.start();
 		setState(STATE_CONNECTING);
 	}
 	
 	public synchronized void stop() {
 		if (D) Log.d(TAG, "stop");
 		if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
 		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
 		if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
 		setState(STATE_NONE);
 	}
 
 	/**
 	 * Write to the ConnectedThread in an unsynchronized manner
 	 * @param out The bytes to write
 	 * @see ConnectedThread#write(byte[])
 	 */
 	public void write(byte[] out) {
 		// Create temporary object
 		ConnectedThread r;
 		// Synchronize a copy of the ConnectedThread
 		synchronized (this) {
 			if (mState != STATE_CONNECTED) return;
 			r = mConnectedThread;
 		}
 		// Perform the write unsynchronized
 		r.write(out);
 	}
 	
 	private class ConnectThread extends Thread {
 		private final BluetoothSocket mmSocket;
 		private final BluetoothDevice mmDevice;
 
 		public ConnectThread(BluetoothDevice device) {
 			mmDevice = device;
 			BluetoothSocket tmp = null;
 
 			// Get a BluetoothSocket for a connection with the
 			// given BluetoothDevice
 			try {
 				tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
 			} catch (IOException e) {
 				Log.e(TAG, "create() failed", e);
 			}
 			mmSocket = tmp;
 		}
 
 		public void run() {
 			Log.i(TAG, "BEGIN mConnectThread");
 			setName("ConnectThread");
 
 			// Always cancel discovery because it will slow down a connection
 			mBluetoothAdapter.cancelDiscovery();
 
 			// Make a connection to the BluetoothSocket
 			try {
 				// This is a blocking call and will only return on a
 				// successful connection or an exception
 				mmSocket.connect();
 			} catch (IOException e) {
 				connectionFailed();
 				// Close the socket
 				try {
 					mmSocket.close();
 				} catch (IOException e2) {
 					Log.e(TAG, "unable to close() socket during connection failure", e2);
 				}
 				// Start the service over to restart listening mode
 				BTService.this.start();
 				return;
 			}
 
 			// Reset the ConnectThread because we're done
 			synchronized (BTService.this) {
 				mConnectThread = null;
 			}
 
 			// Start the connected thread
 			connected(mmSocket, mmDevice);
 		}
 
 		public void cancel() {
 			try {
 				mmSocket.close();
 			} catch (IOException e) {
 				Log.e(TAG, "close() of connect socket failed", e);
 			}
 		}
 	}
 	
 	/**
 	 * This thread runs while listening for incoming connections. It behaves
 	 * like a server-side client. It runs until a connection is accepted
 	 * (or until cancelled).
 	 */
 	private class AcceptThread extends Thread {
 		// The local server socket
 		private final BluetoothServerSocket mmServerSocket;
 
 		public AcceptThread() {
 			BluetoothServerSocket tmp = null;
 
 			// Create a new listening server socket
 			try {
 				tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID);
 			} catch (IOException e) {
 				Log.e(TAG, "listen() failed", e);
 			}
 			mmServerSocket = tmp;
 		}
 
 		public void run() {
 			if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);
 			setName("AcceptThread");
 			BluetoothSocket socket = null;
 
 			// Listen to the server socket if we're not connectedeplyTo
 			while (mState != STATE_CONNECTED) {
 				try {
 					// This is a blocking call and will only return on a
 					// successful connection or an exception
 					socket = mmServerSocket.accept();
 				} catch (IOException e) {
 					Log.e(TAG, "accept() failed", e);
 					break;
 				}
 
 				// If a connection was accepted
 				if (socket != null) {
 					synchronized (BTService.this) {
 						switch (mState) {
 						case STATE_LISTEN:
 						case STATE_CONNECTING:
 							// Situation normal. Start the connected thread.
 							connected(socket, socket.getRemoteDevice());
 							break;
 						case STATE_NONE:
 						case STATE_CONNECTED:
 							// Either not ready or already connected. Terminate new socket.
 							try {
 								socket.close();
 							} catch (IOException e) {
 								Log.e(TAG, "Could not close unwanted socket", e);
 							}
 							break;
 						}
 					}
 				}
 			}
 			if (D) Log.i(TAG, "END mAcceptThread");
 		}
 
 		public void cancel() {
 			if (D) Log.d(TAG, "cancel " + this);
 			try {
 				mmServerSocket.close();
 			} catch (IOException e) {
 				Log.e(TAG, "close() of server failed", e);
 			}
 		}
 	}
 	
 	/**
 	 * This thread runs during a connection with a remote device.
 	 * It handles all incoming and outgoing transmissions.
 	 */
 	private class ConnectedThread extends Thread {
 		private final BluetoothSocket mmSocket;
 		private final InputStream mmInStream;
 		private final OutputStream mmOutStream;
 
 		public ConnectedThread(BluetoothSocket socket) {
 			Log.d(TAG, "create ConnectedThread");
 			mmSocket = socket;
 			InputStream tmpIn = null;
 			OutputStream tmpOut = null;
 
 			// Get the BluetoothSocket input and output streams
 			try {
 				tmpIn = socket.getInputStream();
 				tmpOut = socket.getOutputStream();
 			} catch (IOException e) {
 				Log.e(TAG, "temp sockets not created", e);
 			}
 
 			mmInStream = tmpIn;
 			mmOutStream = tmpOut;
 		}
 
 		public void run() {
 			Log.i(TAG, "BEGIN mConnectedThread");
 			byte[] buffer = new byte[1024];
 			int bytes;
 
 			// Keep listening to the InputStream while connected
 			while (true) {
 				try {
 					// Read from the InputStream
 					bytes = mmInStream.read(buffer);
 
 					// Send the obtained bytes to the UI Activity
 					//mHandler.obtainMessage(BTActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
					Message message = Message.obtain(null, BTService.MSG_REC_EVENT);
 					Bundle b = new Bundle();
 					b.putString("str1", buffer.toString());
 					message.setData(b);
 					mClients.get(0).send(message);
 					Log.i(TAG, buffer.toString());
 				} catch (Exception e) {
 					Log.e(TAG, "disconnected", e);
 					connectionLost();
 					break;
 				} // TODO add more excptions
 			}
 		}
 
 		/**
 		 * Write to the connected OutStream.
 		 * @param buffer  The bytes to write
 		 */
 		public void write(byte[] buffer) {
 			try {
 				mmOutStream.write(buffer);
 
 				// Share the sent message back to the UI Activity
 				//mHandler.obtainMessage(BTActivity.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
 				// TODO: todo
 				Log.i(TAG, buffer.toString());
 			} catch (IOException e) {
 				Log.e(TAG, "Exception during write", e);
 			}
 		}
 		
 
 		public void cancel() {
 			try {
 				mmSocket.close();
 			} catch (IOException e) {
 				Log.e(TAG, "close() of connect socket failed", e);
 			}
 		}
 	}
 
 
 
 
 
 }
