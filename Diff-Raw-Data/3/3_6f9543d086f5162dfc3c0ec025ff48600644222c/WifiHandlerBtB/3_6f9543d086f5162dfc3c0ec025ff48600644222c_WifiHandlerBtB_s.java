 package it.chalmers.tendu.network.wifip2p;
 
 import it.chalmers.tendu.defaults.Constants;
 import it.chalmers.tendu.gamemodel.GameId;
 import it.chalmers.tendu.network.NetworkHandler;
 import it.chalmers.tendu.tbd.C;
 import it.chalmers.tendu.tbd.C.Msg;
 import it.chalmers.tendu.tbd.C.Tag;
 import it.chalmers.tendu.tbd.EventBus;
 import it.chalmers.tendu.tbd.EventMessage;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.annotation.TargetApi;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.net.wifi.p2p.WifiP2pConfig;
 import android.net.wifi.p2p.WifiP2pDevice;
 import android.net.wifi.p2p.WifiP2pDeviceList;
 import android.net.wifi.p2p.WifiP2pGroup;
 import android.net.wifi.p2p.WifiP2pInfo;
 import android.net.wifi.p2p.WifiP2pManager;
 import android.net.wifi.p2p.WifiP2pManager.ActionListener;
 import android.net.wifi.p2p.WifiP2pManager.Channel;
 import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
 import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
 import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
 import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
 import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
 import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Handler;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryonet.Client;
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.Listener;
 import com.esotericsoftware.kryonet.Server;
 
 /** Handles the Wifi connection
  * 
  * @author johnpetersson
  *
  */
 public class WifiHandlerBtB extends NetworkHandler implements WifiP2pManager.ConnectionInfoListener {
 	public static final String TAG = "WifiHandler";
 
 	private static final int MAX_KRYO_BLOCKING_TIME = 5000;
 	private static final int TCP_PORT = 54555;
 	private Client client;
 	private Server server;
 
 	// Client/Server logic gets loaded prematurely when listeners fire on startup
 	// this flag should prevent this
 	private boolean isReadyToConnect = false; 
 
 	WifiP2pManager mManager;
 	Channel mChannel;
 	//BroadcastReceiver mReceiver;
 
 	IntentFilter mIntentFilter;
 
 	private Handler mHandler = new Handler();
 
 	public WifiHandlerBtB(Context ctx) {
 		super(ctx);
 
 		mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
 		mChannel = mManager.initialize(context, context.getMainLooper(), null);
 		// mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, context);
 
 		mIntentFilter = new IntentFilter();
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
 
 		context.registerReceiver(mReceiver, mIntentFilter); // Not necessary when we start calling onResume()
 
 		forgetAnyExistingWifiGroup();
 
 		notfiyIfApiLevelTooLow();
 
 	}
 
 	@Override
 	public void hostSession() {
 		isReadyToConnect = true;
 		//discoverPeers();
 		createNewWifiGroup();
 
 		startRegistration();
 	}
 
 	@Override
 	public void joinGame() {
 		isReadyToConnect = true;
 
 		// TODO Check if already connected by wifi and if so start kryo connection
 		mManager.requestConnectionInfo(mChannel, this);
 		//discoverPeers();
 		//connectToFirstAvailable();
 
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
 		unregisterBroadcastReceiver();
 		resetNetwork();
 	}
 
 	@Override
 	public void onResume() {
 		/* register the broadcast receiver with the intent values to be matched */
 		context.registerReceiver(mReceiver, mIntentFilter);
 	}
 
 	@Override
 	public void onPause() {
 		unregisterBroadcastReceiver();
 	}
 
 	@Override
 	public void testSendMessage() {
 		EventMessage message = new EventMessage(C.Tag.TEST, C.Msg.TEST);
 		broadcastMessageOverNetwork(message);
 	}
 
 	@Override
 	public String getMacAddress() {
 		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 		WifiInfo wInfo = wifiManager.getConnectionInfo();
 		String macAddress = wInfo.getMacAddress(); 
 		return macAddress;
 	}
 
 	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
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
 				Log.d(TAG, "P2P peers changed");
 				if (mManager != null) {
 					mManager.requestPeers(mChannel, myPeerListListener);
 				}
 			} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
 				// Respond to new connection or disconnections
 				Log.d(TAG, "Connection changed");
 				if (mManager == null) {
 					return;
 				}
 
 
 				NetworkInfo networkInfo = (NetworkInfo) intent
 						.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
 
 				if (networkInfo.isConnected()) {
 					//Log.d(TAG, "Connected to: " + networkInfo.getDetailedState());
 					// We are connected with the other device, request connection
 					// info to find group owner IP
 					mManager.requestConnectionInfo(mChannel, WifiHandlerBtB.this); // (This is done once in join() already)
 				}
 			} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
 				Log.d(TAG, "This device's wifi state changed");
 				// Respond to this device's wifi state changing
 			}
 		}
 	};
 
 	private void unregisterBroadcastReceiver() {
 		/* unregister the broadcast receiver */
 		if (mReceiver != null) {
 			try {
 				context.unregisterReceiver(mReceiver);				
 			} catch(IllegalArgumentException e) {
 				Log.d(TAG, "Receiver not registered, can't be deleted");
 			}
 		}
 	}
 
 	@Override
 	public void onConnectionInfoAvailable(WifiP2pInfo info) {
 		if (!isReadyToConnect) {
 			// Ignore this method call if starting the app 
 			Log.d(TAG, "Not ready to connect");
 			return;
 		}
 		// InetAddress from WifiP2pInfo struct.
 		String groupOwnerAddress = null;
 		if (info.groupOwnerAddress != null) {
 			groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
 		}
 
 
 		// After the group negotiation, we can determine the group owner.
 		if (info.groupFormed && info.isGroupOwner) {
 			// Do whatever tasks are specific to the group owner.
 			// One common case is creating a server thread and accepting
 			// incoming connections.
 			Log.d(TAG, "Acting as server");
 			Toast.makeText(context, "Acting as server", Toast.LENGTH_SHORT).show();
 
 			if (server == null) {
 				startKryoNetServer();
 			}
 
 			// Let unit know it's host
 			sendToEventBus(new EventMessage(C.Tag.NETWORK_NOTIFICATION, C.Msg.YOU_ARE_HOST));
 		} else if (info.groupFormed) {
 			// The other device acts as the host. In this case,
 			// you'll want to create a client thread that connects to the group
 			// owner.
 			Log.d(TAG, "Acting as client");
 			Toast.makeText(context, "Acting as Client", Toast.LENGTH_SHORT).show();
 
 			new StartKryoNetClientTask().execute(groupOwnerAddress); // Has to be run in another thread for now
 
 			// Let unit know it's a client
 			sendToEventBus(new EventMessage(C.Tag.NETWORK_NOTIFICATION, C.Msg.YOU_ARE_CLIENT));
 		} else { 
 			// No group is formed, wait a while and then connect to the first unit available
 			Log.d(TAG, "No group formed, doing discovery/connect");
 			discoverService();
 			//discoverPeers();
 			//connectToFirstAvailable();
 
 		}
 	}
 
 	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
 	private PeerListListener myPeerListListener = new PeerListListener() {
 
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
 
 	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
 	private void resetConnection() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
 			mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
 
 				@Override
 				public void onSuccess() {
 					// do nothing
 				}
 
 				@Override
 				public void onFailure(int reason) {
 					Log.d(TAG, "Couldn't stop peer deiscovery: " + translateErrorCodeToMessage(reason));
 				}
 			});
 
 		}
 	}
 
 	private void discoverPeers() { 
 		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
 			@Override
 			public void onSuccess() {
 				Log.d(TAG, "Initiated discovery");
 			}
 
 			@Override
 			public void onFailure(int reasonCode) {
 				Log.d(TAG, "Discovery failed: " + translateErrorCodeToMessage(reasonCode));
 
 			}
 		}); 
 	}
 
 	private WifiP2pDevice findFirstEligibleDevice(List<WifiP2pDevice> peers) {
 		for (WifiP2pDevice device: peers) {
 			return device;
 		}
 		return null; 
 	}
 
 	private void connectToFirstAvailable() {
 		// Wait a minute while available units are discovered
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
 
 	private void connectToDevice(final WifiP2pDevice device) {
 		WifiP2pConfig config = new WifiP2pConfig();
 		config.deviceAddress = device.deviceAddress;
 
 		mManager.connect(mChannel, config, new ActionListener() {
 
 			@Override
 			public void onSuccess() {
 				// WiFiDirectBroadcastReceiver will notify us. Ignore for now.
 				Log.d(TAG, "Connection initiated to: " + device.deviceName);
 				// TODO It may be too early to broadcast mac-address here
 				EventBus.INSTANCE.broadcast(new EventMessage(Tag.NETWORK_NOTIFICATION, Msg.PLAYER_CONNECTED, device.deviceAddress));
 			}
 
 			@Override
 			public void onFailure(int reason) {
 				Log.d(TAG, "Could not connect to: " + device.deviceName + ": " + translateErrorCodeToMessage(reason));
 				toastMessage("Connection failed. Retry");
 			}
 		});
 	}
 
 	private void resetNetwork() {
 		removeWifiGroup();
 		clearServices();
 
 		if (server != null) {
 			server.stop();
 			server.close();
 		}
 		if (client != null) {
 			client.stop();
 			client.close();
 		}
 	}
 
 	private void removeWifiGroup() {
 		Log.d(TAG, "Removing wifi group");
 		mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
 
 			@Override
 			public void onFailure(int reason) {
 				Log.d(TAG, "Failed to remove group: " + translateErrorCodeToMessage(reason));
 				//				if (reason == WifiP2pManager.BUSY) {
 				//					removeWifiGroup();
 				//				}
 			}
 
 			@Override
 			public void onSuccess() {
 				// Do nothing
 			}
 		});		
 	}
 
 	private void createNewWifiGroup() {
 		mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
 
 			@Override
 			public void onSuccess() {
 				// Do nothing
 
 			}
 			@Override
 			public void onFailure(int reason) {
 				Log.d(TAG, "Group creation failed: " + translateErrorCodeToMessage(reason));
 				if (reason == WifiP2pManager.BUSY) {
 					createNewWifiGroup();
 				}
 			}
 		});
 	}
 
 	private void forgetAnyExistingWifiGroup() {
 		Log.d(TAG, "Requesting group info to forget");
 		mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
 
 			@Override
 			public void onGroupInfoAvailable(WifiP2pGroup group) {
 				if (group != null) {
 					removeWifiGroup();
 				}
 
 			}
 		});
 	}
 
 	// ********************* Service Handling  *********************
 	// This all requires Api level 16 - Jelly Bean
 	// More or less Copy and pasted from developer.android.com 
 
 	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
 	private void startRegistration() {
 		//  Create a string map containing information about your service.
 		Map<String, String> record = new HashMap<String, String>();
 		record.put("name", Constants.SERVER_NAME);
 
 		// Service information.  Pass it an instance name, service type
 		// _protocol._transportlayer , and the map containing
 		// information other devices will want once they connect to this one.
 		WifiP2pDnsSdServiceInfo serviceInfo =
 				WifiP2pDnsSdServiceInfo.newInstance("Temp instance", "temp protocol name", record);
 
 
 		// Add the local service, sending the service info, network channel,
 		// and listener that will be used to indicate success or failure of
 		// the request.
 		mManager.addLocalService(mChannel, serviceInfo, new ActionListener() {
 			@Override
 			public void onSuccess() {
 				// Command successful!
 			}
 
 			@Override
 			public void onFailure(int reason) {
 				Log.d(TAG, "Service adding failed: " + translateErrorCodeToMessage(reason));
 			}
 		});
 	}
 
 	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
 	private void discoverService() {
 		DnsSdTxtRecordListener txtListener = new DnsSdTxtRecordListener() {
 			@Override
 			/* Callback includes:
 			 * record: TXT record dta as a map of key/value pairs.
 			 * device: The device running the advertised service.
 			 */
 
 			public void onDnsSdTxtRecordAvailable(
 					String fullDomain, Map record, WifiP2pDevice device) {
 				Log.d(TAG, "DnsSdTxtRecord available -" + record.toString());
 				//buddies.put(device.deviceAddress, (String) record.get("name"));
 
 				if (record.get("name").equals(Constants.SERVER_NAME)) {
 					// Connect to the service found if eligible 
 					connectToDevice(device);
 				}
 			}
 		};
 
 		DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {
 			@Override
 			public void onDnsSdServiceAvailable(String instanceName, String registrationType,
 					WifiP2pDevice resourceType) {
 				// Not used
 			}
 		};
 
 		mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);
 
 		WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
 		mManager.addServiceRequest(mChannel,
 				serviceRequest,
 				new ActionListener() {
 			@Override
 			public void onSuccess() {
 				// Success!
 			}
 
 			@Override
 			public void onFailure(int reason) {
 				Log.d(TAG, "Service request failed: " + translateErrorCodeToMessage(reason));
 			}
 		});
 
 		mManager.discoverServices(mChannel, new ActionListener() {
 
 			@Override
 			public void onSuccess() {
 				// Success!
 			}
 
 			@Override
 			public void onFailure(int reason) {
 				Log.d(TAG, "Service discovery failed: " + translateErrorCodeToMessage(reason));
 			}
 		});
 	}
 
 	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
 	private void clearServices() {
 		mManager.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener() {
 
 			@Override
 			public void onFailure(int reason) {
 				Log.d(TAG, "Service clearing failed: " + translateErrorCodeToMessage(reason));				
 			}
 
 			@Override
 			public void onSuccess() {
 			}
 		});
 
 	}
 
 	private void notfiyIfApiLevelTooLow() {
 		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
 			toastMessage("Api version too low. Need Jelly Bean (Api level 16)");
 		}
 	}
 
 	// ********************** Kryo *********************************
 
 	private void startKryoNetServer() {
 		server = new Server();
 		Kryo kryo = server.getKryo();
 		registerKryoClasses(kryo);
 		server.start();
 		try {
 			server.bind(TCP_PORT); //, 54777); // other figure is for UDP
 			Log.d(TAG, "Kryonet server started");
 		} catch (IOException e) {
 			Log.d(TAG, "KryoNet Server creation failure");
 			e.printStackTrace();
 		}
 
 		server.addListener(new Listener() {
 			@Override
 			public void received (Connection connection, Object object) {
 				if (object instanceof EventMessage) {
 					EventMessage request = (EventMessage)object;
 					Log.d(TAG, "Received: " + request.toString());
 					toastMessage(request);
 				}
 			}
 			@Override
 			public void disconnected(Connection connection) {
 				connection.close();
 				EventBus.INSTANCE.broadcast(new EventMessage(Tag.NETWORK_NOTIFICATION, Msg.CONNECTION_LOST));
 				//resetNetwork();
 				//server.close();
 			}
 		});
 	}
 
 	private class StartKryoNetClientTask extends AsyncTask<String, Void, Object> {
 		@Override
 		protected Object doInBackground(String... addresses) {
 			String address = addresses[0];
 			client = new Client();
 			Kryo kryo = client.getKryo();
 			registerKryoClasses(kryo);
			client.start();
 			try {
 				Log.d(TAG, "KryoNet will now connct to address: " + address);
 				client.connect(MAX_KRYO_BLOCKING_TIME, address, TCP_PORT);//, 54777); other figure is for UDP
 			} catch (IOException e) {
 				Log.d(TAG, "Error in connecting via KryoNet");
 				e.printStackTrace();
 			}
 
 			client.setKeepAliveTCP(100);
 			client.addListener(new Listener() {
 				@Override
 				public void received(com.esotericsoftware.kryonet.Connection connection, Object object) {
 					if (object instanceof EventMessage) {
 						EventMessage request = (EventMessage)object;
 						Log.d(TAG, "Received: " + request.toString());
 						toastMessage(request);
 					}
 				}
 				@Override
 				public void disconnected(Connection connection) {
 					connection.close();
 					client.stop();
 					EventBus.INSTANCE.broadcast(new EventMessage(Tag.NETWORK_NOTIFICATION, Msg.CONNECTION_LOST));
 					resetNetwork();
 					//client.close();
 				}
 			});
 			return null;
 		}
 	}
 
 	/** Register the classes we want to send over the network */
 	private void registerKryoClasses(Kryo kryo) {
 		kryo.register(EventMessage.class);
 		kryo.register(GameId.class);
 		kryo.register(C.class);
 		kryo.register(C.Msg.class);
 		kryo.register(C.Tag.class);
 	}	
 }
