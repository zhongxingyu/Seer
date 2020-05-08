 package it.chalmers.tendu.network.wifip2p;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import com.badlogic.gdx.backends.android.AndroidApplication;
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryonet.Client;
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.Listener;
 import com.esotericsoftware.kryonet.Server;
 
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.NetworkInfo;
 import android.net.wifi.WpsInfo;
 import android.net.wifi.p2p.WifiP2pConfig;
 import android.net.wifi.p2p.WifiP2pDevice;
 import android.net.wifi.p2p.WifiP2pDeviceList;
 import android.net.wifi.p2p.WifiP2pInfo;
 import android.net.wifi.p2p.WifiP2pManager;
 import android.net.wifi.p2p.WifiP2pManager.ActionListener;
 import android.net.wifi.p2p.WifiP2pManager.Channel;
 import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
 import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.util.Log;
 import android.widget.Toast;
 import it.chalmers.tendu.defaults.Constants;
 import it.chalmers.tendu.network.INetworkHandler;
 import it.chalmers.tendu.tbd.C;
 import it.chalmers.tendu.tbd.EventMessage;
 import it.chalmers.tendu.tbd.EventBusListener;
 
 /** Handles the Wifi connection
  * 
  * @author johnpetersson
  *
  */
 public class WifiHandler implements INetworkHandler, EventBusListener, WifiP2pManager.ConnectionInfoListener {
 	public static final String TAG = "WifiHandler";
 
 	private static final int CONNECTION_DELAY = 5000;
 
 	private static final int MAX_KRYO_BLOCKING_TIME = 5000;
 	private static final int TCP_PORT = 54555;
 	private Client client;
 	private Server server;
 
 	private Context context;
 
 	WifiP2pManager mManager;
 	Channel mChannel;
 	//BroadcastReceiver mReceiver;
 
 	IntentFilter mIntentFilter;
 	PeerListListener myPeerListListener;
 	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
 
 	private Handler mHandler = new Handler();
 	//private Kryo kryo;
 
 	public WifiHandler(Context ctx) {
 		context = ctx;
 
 		mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
 		mChannel = mManager.initialize(context, context.getMainLooper(), null);
 		// mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, context);
 
 		mIntentFilter = new IntentFilter();
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
 
 		context.registerReceiver(mReceiver, mIntentFilter); // Not necessary when we start calling onResume()
 
 		myPeerListListener = new PeerListListener() {
 
 			@Override
 			public void onPeersAvailable(WifiP2pDeviceList peerList) {
 				peers.clear();
 				peers.addAll(peerList.getDeviceList());
 				// Log.d(TAG, peers.toString());			
 				if (peers.size() == 0) {
 					Log.d(TAG, "No devices found");
 					return;
 				}
 			}
 		};
 
 	}
 
 	@Override
 	public void hostSession() {
 		discoverPeers();
 
 	}
 
 	@Override
 	public void joinGame() {
 		discoverPeers();
 		mHandler.postDelayed(new Runnable() {
 
 			@Override
 			public void run() {
 				WifiP2pDevice device = findFirstEligibleDevice(peers);
 				if (device != null) {
 					Log.d(TAG, "Will now try and connect to: " + device.deviceName);
 					connectToDevice(device);
 				} else {
 					Log.d(TAG, "No device to connect to");
 				}
 			}
 		}, CONNECTION_DELAY);
 
 	}
 
 	@Override
 	public void broadcastMessageOverNetwork(EventMessage message) {
 		if (client != null) {
 			client.sendTCP(message);
 		}
 		if (server != null) {
 			server.sendToAllTCP(message);
 		}
 
 	}
 
 	@Override
 	public void destroy() {
 		context.unregisterReceiver(mReceiver);
 
 	}
 
 	//	@Override
 	public void onResume() {
 		/* register the broadcast receiver with the intent values to be matched */
 		context.registerReceiver(mReceiver, mIntentFilter);
 	}
 
 	//	@Override
 	protected void onPause() {
 		/* unregister the broadcast receiver */
 		context.unregisterReceiver(mReceiver);
 	}
 
 	@Override
 	public void testStuff() {
 		EventMessage message = new EventMessage(C.Tag.TEST, C.Msg.TEST);
 		broadcastMessageOverNetwork(message);
 	}
 
 	@Override
 	public String getMacAddress() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		//		public void onReceive(Context context, Intent intent) {
 		//			String action = intent.getAction();
 		//			// When discovery finds a device
 		//			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
 		//				// Get the BluetoothDevice object from the Intent
 		//				BluetoothDevice device = intent
 		//						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 		//				if (D)
 		//					Log.v(TAG, "Device found: " + device.getName() + "Adress: "
 		//							+ device.getAddress());
 		//				// Add the device to a list
 		//				availableDevices.add(device);
 		//			}
 		//		}
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 
 			String action = intent.getAction();
 			if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
 
 				int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
 				if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
 					Log.d(TAG, "Wifi p2p enabled");
 				} else {
 					Log.d(TAG, "Wifi p2p NOT enabled");
 				}
 
 			} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
 				// request available peers from the wifi p2p manager. This is an
 				// asynchronous call and the calling activity is notified with a
 				// callback on PeerListListener.onPeersAvailable()
 				if (mManager != null) {
 					mManager.requestPeers(mChannel, myPeerListListener);
 				}
 				Log.d(TAG, "P2P peers changed");
 			} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
 				// Respond to new connection or disconnections
 				Log.d(TAG, "Connected or disconnected with someone");
 				if (mManager == null) {
 					return;
 				}
 
 				NetworkInfo networkInfo = (NetworkInfo) intent
 						.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
 
 				if (networkInfo.isConnected()) {
 
 					// We are connected with the other device, request connection
 					// info to find group owner IP
 
 					mManager.requestConnectionInfo(mChannel, WifiHandler.this);
 				}
 			} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
 				// Respond to this device's wifi state changing
 			}
 		}
 	};
 
 	private void discoverPeers() { 
 		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
 			@Override
 			public void onSuccess() {
 				Log.d(TAG, "Initiated discovery");
 			}
 
 			@Override
 			public void onFailure(int reasonCode) {
 				Log.d(TAG, "Error in discovery: " + reasonCode);
 			}
 		}); 
 	}
 
 	private WifiP2pDevice findFirstEligibleDevice(List<WifiP2pDevice> peers) {
 		for (WifiP2pDevice device: peers) {
 			return device;
 			//			if (device.deviceName.contains(Constants.SERVER_NAME)) {
 			//				Log.d(TAG, "Device name contains " + Constants.SERVER_NAME);
 			//				return device;
 			//			} else {
 			//				Log.d(TAG, "Device name is ineligible");
 			//			}
 		}
 		return null; 
 	}
 
 
 	private void connectToDevice(final WifiP2pDevice device) {
 		//obtain a peer from the WifiP2pDeviceList
 		WifiP2pConfig config = new WifiP2pConfig();
 		config.deviceAddress = device.deviceAddress;
 		//config.wps.setup = WpsInfo.PBC;
 		mManager.connect(mChannel, config, new ActionListener() {
 
 			@Override
 			public void onSuccess() {
 				// WiFiDirectBroadcastReceiver will notify us. Ignore for now.
 				Log.d(TAG, "Connection initiated to: " + device.deviceName);
 			}
 
 			@Override
 			public void onFailure(int reason) {
 				Log.d(TAG, "Could not connect to: " + device.deviceName);
 			}
 		});
 	}
 
 	@Override
 	public void onConnectionInfoAvailable(WifiP2pInfo info) {
 		// InetAddress from WifiP2pInfo struct.
 		String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
 
 		// After the group negotiation, we can determine the group owner.
 		if (info.groupFormed && info.isGroupOwner) {
 			// Do whatever tasks are specific to the group owner.
 			// One common case is creating a server thread and accepting
 			// incoming connections.
 			Log.d(TAG, "Acting as server");
 			Toast.makeText(context, "Group Owner", Toast.LENGTH_SHORT).show();
 			new StartKryoNetServerTask().execute(); 
 			//startKryoNetServer();
 
 		} else if (info.groupFormed) {
 			// The other device acts as the client. In this case,
 			// you'll want to create a client thread that connects to the group
 			// owner.
 			Log.d(TAG, "Acting as client");
 			Toast.makeText(context, "Client", Toast.LENGTH_SHORT).show();
 			//startKryoNetClient(groupOwnerAddress);
 			new StartKryoNetClientTask().execute(groupOwnerAddress);
 		}
 
 	}
 
 	private class StartKryoNetServerTask extends AsyncTask<Object, Object, Object> {
 		@Override
 		protected Object doInBackground(Object... params) {
 			server = new Server();
 			server.start();
			registerKryoClasses(server.getKryo());
 			try {
				server.bind(TCP_PORT); //, 54777); // UDP
 			} catch (IOException e) {
 				Log.d(TAG, "KryoNet Server creation failure");
 				e.printStackTrace();
 			}
 
 			server.addListener(new Listener() {
 				public void received (Connection connection, Object object) {
 					if (object instanceof EventMessage) {
 						EventMessage request = (EventMessage)object;
 						Log.d(TAG, "Received: " + request.toString());
 					}
 				}
 			});
 			return null;
 		}
 	}
 
 	private class StartKryoNetClientTask extends AsyncTask<String, Void, Object> {
 		@Override
 		protected Object doInBackground(String... addresses) {
 			String address = addresses[0];
 			client = new Client();
 			client.start();
			registerKryoClasses(client.getKryo());
 			try {
 				Log.d(TAG, "KryoNet will now connct to address: " + address);
				client.connect(MAX_KRYO_BLOCKING_TIME, address, TCP_PORT);//, 54777); // UDP
 			} catch (IOException e) {
 				Log.d(TAG, "Error in connecting via KryoNet");
 				e.printStackTrace();
 			}
 
 			client.addListener(new Listener() {
 				public void received(com.esotericsoftware.kryonet.Connection connection, Object object) {
 					if (object instanceof EventMessage) {
 						EventMessage request = (EventMessage)object;
 						Log.d(TAG, "Received: " + request.toString());
 					}
 				}
 			});
 			return null;
 		}
 	}
 
 	/** Register the classes we want to send over the network */
 	private void registerKryoClasses(Kryo kryo) {
 		kryo.register(EventMessage.class);
 	}
 
 	@Override
 	public void onBroadcast(EventMessage message) {
 		// TODO Hook up to event bus	
 	}
 
 	
 }
