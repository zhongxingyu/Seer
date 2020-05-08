 /*
  * Copyright (C) 2009 The Android Open Source Project
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
 
 package it.chalmers.tendu.network.clicklinkcompete;
 
 import it.chalmers.tendu.network.NetworkMessage;
 import it.chalmers.tendu.network.clicklinkcompete.Connection.OnConnectionLostListener;
 import it.chalmers.tendu.network.clicklinkcompete.Connection.OnIncomingConnectionListener;
 import it.chalmers.tendu.network.clicklinkcompete.Connection.OnMaxConnectionsReachedListener;
 import it.chalmers.tendu.network.clicklinkcompete.Connection.OnMessageReceivedListener;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.UUID;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.content.Context;
 import android.content.Intent;
 import android.os.RemoteException;
 import android.util.Log;
 
 import com.badlogic.gdx.backends.android.AndroidApplication;
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryo.io.Input;
 import com.esotericsoftware.kryo.io.Output;
 
 /**
  * Service for simplifying the process of establishing Bluetooth connections and
  * sending data in a way that is geared towards multi-player games.
  */
 
 public class ConnectionService {
 	public static final String TAG = "net.clc.bt.ConnectionService";
 
 	private ArrayList<UUID> mUuid;
 
 	private ConnectionService mSelf;
 
 	private String mApp; // Assume only one app can use this at a time; may
 
 	// change this later
 
 	//private IConnectionCallback mCallback;
 
 	private ArrayList<BluetoothDevice> mBtDevices;
 
 	private HashMap<String, BluetoothSocket> mBtSockets;
 
 	private HashMap<String, Thread> mBtStreamWatcherThreads;
 
 	private BluetoothAdapter mBtAdapter;
 
 
 
 	//private OnConnectionServiceReadyListener mOnConnectionServiceReadyListener;
 
 	private OnIncomingConnectionListener mOnIncomingConnectionListener;
 
 	private OnMaxConnectionsReachedListener mOnMaxConnectionsReachedListener;
 
 	private OnMessageReceivedListener mOnMessageReceivedListener;
 
 	private OnConnectionLostListener mOnConnectionLostListener;
 
 	private Context context;
 
 	/** Kryo Variables*/
 	private Kryo mKryo;
 
 	private Output out;
 
 
 	public ConnectionService(Context context) {
 		mSelf = this;
 		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
 		mApp = "";
 		mBtSockets = new HashMap<String, BluetoothSocket>();
 		mBtDevices = new ArrayList<BluetoothDevice>();
 		mBtStreamWatcherThreads = new HashMap<String, Thread>();
 		mUuid = new ArrayList<UUID>();
 		// Allow up to 7 devices to connect to the server
 		mUuid.add(UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c666"));
 		mUuid.add(UUID.fromString("503c7430-bc23-11de-8a39-0800200c9a66"));
 		mUuid.add(UUID.fromString("503c7431-bc23-11de-8a39-0800200c9a66"));
 		mUuid.add(UUID.fromString("503c7432-bc23-11de-8a39-0800200c9a66"));
 		mUuid.add(UUID.fromString("503c7433-bc23-11de-8a39-0800200c9a66"));
 		mUuid.add(UUID.fromString("503c7434-bc23-11de-8a39-0800200c9a66"));
 		mUuid.add(UUID.fromString("503c7435-bc23-11de-8a39-0800200c9a66"));
 		this.context = context;
 		initializeKryoSerializer();
 	}
 
 
 	private void initializeKryoSerializer() {
 		mKryo = new Kryo();
 
 		// Register the classes we want to send over the network
 		mKryo.register(NetworkMessage.class);
 	}
 
 
 	private class BtStreamWatcher implements Runnable {
 		private String address;
 		private BluetoothDevice device;	
 		private Input in;
 
 
 		//private Handler handler = new Handler(Looper.getMainLooper());
 
 		public BtStreamWatcher(BluetoothDevice device) {
 			this.device = device;
 			address = device.getAddress();
 			mBtSockets.get(address);
 
 			try {
 				in = new Input(mBtSockets.get(address).getInputStream());
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 
 		public void run() {
 			
 			Object receivedObject;
 
 			//TODO: Break loop when someone disconnects
 			while (true) {
 				receivedObject = mKryo.readClassAndObject(in);
 				
 				if(receivedObject instanceof NetworkMessage){
 					mOnMessageReceivedListener.OnMessageReceived(device, (NetworkMessage)receivedObject);
 				}
 			}
 			
 //			mBtDevices.remove(address);
 //			mBtSockets.remove(address);
 //			mBtStreamWatcherThreads.remove(address);
 //			mOnConnectionLostListener.OnConnectionLost(device);
 			
 		}
 	}
 	private class ConnectionWaiter implements Runnable {
 		private String srcApp;
 
 		private int maxConnections;
 
 		public ConnectionWaiter(String theApp, int connections) {
 			srcApp = theApp;
 			maxConnections = connections;
 		}
 
 		public void run() {
 			try {
 				for (int i = 0; i < Connection.MAX_SUPPORTED && maxConnections > 0; i++) {
 					BluetoothServerSocket myServerSocket = mBtAdapter
 							.listenUsingRfcommWithServiceRecord(srcApp, mUuid.get(i));
 					BluetoothSocket myBSock = myServerSocket.accept();
 					myServerSocket.close(); // Close the socket now that the
 					// connection has been made.
 
 					String address = myBSock.getRemoteDevice().getAddress();
 					BluetoothDevice device = myBSock.getRemoteDevice();
 
 					mBtSockets.put(address, myBSock);
 					mBtDevices.add(device);
 					Thread mBtStreamWatcherThread = new Thread(new BtStreamWatcher(device));
 					mBtStreamWatcherThread.start();
 					mBtStreamWatcherThreads.put(address, mBtStreamWatcherThread);
 					maxConnections = maxConnections - 1;
 					if (mOnIncomingConnectionListener != null) {
 						mOnIncomingConnectionListener.OnIncomingConnection(device);
 					}
 				}
 				if (mOnMaxConnectionsReachedListener != null) {
 					mOnMaxConnectionsReachedListener.OnMaxConnectionsReached();
 				}
 			} catch (IOException e) {
 				Log.i(TAG, "IOException in ConnectionService:ConnectionWaiter", e);
 			}
 		}
 	}
 
 	private BluetoothSocket getConnectedSocket(BluetoothDevice myBtServer, UUID uuidToTry) {
 		BluetoothSocket myBSock;
 		try {
 			myBSock = myBtServer.createRfcommSocketToServiceRecord(uuidToTry);
 			myBSock.connect();
 			return myBSock;
 		} catch (IOException e) {
 			Log.i(TAG, "IOException in getConnectedSocket", e);
 		}
 		return null;
 	}
 	public int startServer(String srcApp, int maxConnections, OnIncomingConnectionListener oicListener, 
 			OnMaxConnectionsReachedListener omcrListener, OnMessageReceivedListener omrListener, 
 			OnConnectionLostListener oclListener) throws RemoteException {
 		if (mApp.length() > 0) {
 			return Connection.FAILURE;
 		}
 
 		mOnIncomingConnectionListener = oicListener;
 		mOnMaxConnectionsReachedListener = omcrListener;
 		mOnMessageReceivedListener = omrListener;
 		mOnConnectionLostListener = oclListener;
 
 		mApp = srcApp;
 		(new Thread(new ConnectionWaiter(srcApp, maxConnections))).start();
 
 		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
 		//discoverableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		((AndroidApplication)context).startActivity(discoverableIntent);
 		return Connection.SUCCESS;
 	}
 
 	public int connect(String srcApp, BluetoothDevice device, OnMessageReceivedListener omrListener,
 			OnConnectionLostListener oclListener) throws RemoteException {
 		if (mApp.length() > 0) {
 			return Connection.FAILURE;
 		}
 
 		mOnMessageReceivedListener = omrListener;
 		mOnConnectionLostListener = oclListener;
 
 		mApp = srcApp;
 		BluetoothDevice myBtServer = mBtAdapter.getRemoteDevice(device.getAddress());
 		BluetoothSocket myBSock = null;
 
 		for (int i = 0; i < Connection.MAX_SUPPORTED && myBSock == null; i++) {
 			for (int j = 0; j < 3 && myBSock == null; j++) {
 				myBSock = getConnectedSocket(myBtServer, mUuid.get(i));
 				if (myBSock == null) {
 					try {
 						Thread.sleep(200);
 					} catch (InterruptedException e) {
 						Log.e(TAG, "InterruptedException in connect", e);
 					}
 				}
 			}
 		}
 		if (myBSock == null) {
 			return Connection.FAILURE;
 		}
 
 		mBtSockets.put(device.getAddress(), myBSock);
 		mBtDevices.add(device);
 		Thread mBtStreamWatcherThread = new Thread(new BtStreamWatcher(device));
 		mBtStreamWatcherThread.start();
 		mBtStreamWatcherThreads.put(device.getAddress(), mBtStreamWatcherThread);
 		return Connection.SUCCESS;
 	}
 
 	public int broadcastMessage(String srcApp, NetworkMessage message) throws RemoteException {
 		if (!mApp.equals(srcApp)) {
 			return Connection.FAILURE;
 		}
 		for (int i = 0; i < mBtDevices.size(); i++) {
 			sendMessage(srcApp, mBtDevices.get(i), message);
 		}
 		return Connection.SUCCESS;
 	}
 
 	public String getConnections(String srcApp) throws RemoteException {
 		if (!mApp.equals(srcApp)) {
 			return "";
 		}
 		String connections = "";
 		for (int i = 0; i < mBtDevices.size(); i++) {
 			connections = connections + mBtDevices.get(i) + ",";
 		}
 		return connections;
 	}
 
 	public int sendMessage(String srcApp, BluetoothDevice destination, NetworkMessage message)
 			throws RemoteException {
 		if (!mApp.equals(srcApp)) {
 			return Connection.FAILURE;
 		}
 		
 		try {
			out = new Output(mBtSockets.get(destination.getAddress()).getOutputStream());
 		} catch (IOException e1) {
 			Log.i(TAG, "IOException in sendMessage - Dest:" + destination.getName() + ", Msg:" + message, e1);
 			return Connection.FAILURE;
 		}
 		
 		mKryo.writeObject(out, message);
 		
 		return Connection.SUCCESS;
 	}
 
 	public void shutdown(String srcApp) throws RemoteException {
 		try {
 			for (int i = 0; i < mBtDevices.size(); i++) {
 				BluetoothSocket myBsock = mBtSockets.get(mBtDevices.get(i));
 				myBsock.close();
 			}
 			mBtSockets = new HashMap<String, BluetoothSocket>();
 			mBtStreamWatcherThreads = new HashMap<String, Thread>();
 			mBtDevices = new ArrayList<BluetoothDevice>();
 			mApp = "";
 		} catch (IOException e) {
 			Log.i(TAG, "IOException in shutdown", e);
 		}
 	}
 
 	public String getAddress() throws RemoteException {
 		return mBtAdapter.getAddress();
 	}
 
 	public String getName() throws RemoteException {
 		return mBtAdapter.getName();
 	}
 
 
 
 }
 
