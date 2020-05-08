 package it.unipd.fast.broadcast.wifi_connection;
 
 import it.unipd.fast.broadcast.GuiHandlerInterface;
 import it.unipd.fast.broadcast.wifi_connection.connectionmanager.ConnectionManagerFactory;
 import it.unipd.fast.broadcast.wifi_connection.message.MessageBuilder;
 import it.unipd.fast.broadcast.wifi_connection.receiver.DataReceiverServiceInterface;
 import it.unipd.fast.broadcast.wifi_connection.receiver.DataReceiverService;
 import it.unipd.fast.broadcast.wifi_connection.receiver.DataReceiverService.DataReceiverBinder;
 import it.unipd.fast.broadcast.wifi_connection.receiver.DataReceiverService.IDataCollectionHandler;
 import it.unipd.fast.broadcast.wifi_connection.receiver.FastBroadcastReceiver;
 import it.unipd.fast.broadcast.wifi_connection.transmissionmanager.TransmissionManagerFactory;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.net.wifi.WpsInfo;
 import android.net.wifi.p2p.WifiP2pConfig;
 import android.net.wifi.p2p.WifiP2pDevice;
 import android.net.wifi.p2p.WifiP2pDeviceList;
 import android.net.wifi.p2p.WifiP2pManager;
 import android.net.wifi.p2p.WifiP2pManager.ActionListener;
 import android.net.wifi.p2p.WifiP2pManager.Channel;
 import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
 import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 
 public class WiFiConnectionController {
 	protected final String TAG = "it.unipd.fast.broadcast";
 	public static final String MAC_ADDRESS = null;
 
 	private Handler guiHandler;
 	private ServiceConnection serviceConnection = new DataServiceConnection();
 	private IDataCollectionHandler collectionHandler = new CollectionHandler();
 	private DataReceiverServiceInterface dataInterface = null;
 	private WifiP2pManager manager;
 	private Channel channel;
 	private BroadcastReceiver receiver;
 	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
 	private Map<String,String> peer_id_ip_map;
 	private Context context;
 	private IntentFilter intent_filter;
 	private ConnectionInfoListener connectionInfoListener = ConnectionManagerFactory.getInstance().getConnectionManager();
 
 	private class DataServiceConnection implements ServiceConnection {
 
 		@Override
 		public void onServiceConnected(ComponentName arg0, IBinder binder) {
 			dataInterface = ((DataReceiverBinder)binder).getService();
 			dataInterface.registerHandler(collectionHandler);
 			Log.d(TAG, this.getClass().getSimpleName()+": Servizio ricezione dati binded");
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
 			Log.d(TAG, this.getClass().getSimpleName()+": Service lost");
 		}
 
 	}
 
 	/**
 	 * PeerListListener implementation
 	 * 
 	 */
 	private PeerListListener peer_listener = new PeerListListener() {
 		public void onPeersAvailable(WifiP2pDeviceList peers_list) {
 			Log.d(TAG, this.getClass().getSimpleName()+": Peers Added to the List");
 			peers.clear();
 			peers.addAll(peers_list.getDeviceList());
 			Message msg = new Message();
 			msg.obj = peers;
 			msg.what = GuiHandlerInterface.UPDATE_PEERS;
 			guiHandler.sendMessage(msg);
 		}
 
 	};
 
 
 
 	/**
 	 * Constructor
 	 * 
 	 * @param context
 	 */
 	public WiFiConnectionController(Context context, GuiHandlerInterface guiHandlerInterface) {
 		this.context = context;
 		this.guiHandler = guiHandlerInterface.getGuiHandler();
 		collectionHandler.setWiFiController(this);
 		// manager and channel initialization
 		manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
 		channel = manager.initialize(context, context.getMainLooper(), null);
 		// Register intent filter to receive specific intents
 		intent_filter = new IntentFilter();
 		intent_filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
 		intent_filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
 		intent_filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
 		intent_filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
 
 		Log.d(TAG, this.getClass().getSimpleName()+": Bindo il servizio di ricezione dati");
 		Intent locService = new Intent(context, DataReceiverService.class);
 		context.startService(locService);
 		context.bindService(locService, serviceConnection, Context.BIND_AUTO_CREATE);
 	}
 
 	/**
 	 * Manages connection to the given device
 	 * 
 	 * @param device
 	 */
 	public void connect(WifiP2pDevice device){
 		WifiP2pConfig config = new WifiP2pConfig();
 		config.deviceAddress = device.deviceAddress;
 		config.wps.setup = WpsInfo.PBC;
 		config.groupOwnerIntent = 15;
 		manager.connect(channel,config, new ActionListener() {
 
 			public void onSuccess() {
 				showToast("Richiesta di connessione effettuata");
 			}
 
 			public void onFailure(int reason) {
 				showToast("Impossibil Connettersi reason = "+reason);
 			}
 		});
 
 
 	}
 
 	/**
 	 * Tell whether register/unregister FastBroadcastReceiver
 	 * 
 	 * @param registered
 	 */
 	public void setFastBroadCastReceiverRegistered(boolean registered){
 		if(registered) {
 			receiver = new FastBroadcastReceiver(manager, channel,peer_listener,this.connectionInfoListener);
 			context.registerReceiver(receiver, intent_filter);
 		}else{
 			context.unregisterReceiver(receiver);
 		}
 	}
 
 	/**
 	 * Registers BroadcastReceiver and requests DataReceiverService
 	 */
 	public void aquireResources() {
 		receiver = new FastBroadcastReceiver(manager, channel,peer_listener,this.connectionInfoListener);
 		context.registerReceiver(receiver, intent_filter);
 	}
 
 	/**
 	 * Unregisters BroadcastReceiver and releases DataReceiverService
 	 */
 	public void releaseResources() {
 		context.unregisterReceiver(receiver);
 	}
 
 	/**
 	 * Setter method for the map
 	 * 
 	 * @param map
 	 */
 	protected void setPeersIdIPmap(Map<String,String> map){
 		if(peer_id_ip_map == null) peer_id_ip_map = map;
 		else peer_id_ip_map.putAll(map);
 		String s = "Map updated! \n";
 		for(String k : peer_id_ip_map.keySet()){
 			s += k + "  " + peer_id_ip_map.get(k)+"\n";
 		}
 		showToast(s);
 	}
 
 	public Map<String,String> getPeersMap(){
 		return peer_id_ip_map;
 	}
 
 	/**
 	 * Starts peers discovering
 	 * 
 	 */
 	public void discoverPeers(){
 		manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
 			public void onSuccess() {
 				Log.d(TAG, this.getClass().getSimpleName()+": Discover Peers onSuccess called");
 			}
 
 			public void onFailure(int reasonCode) {
 				Log.d(TAG, this.getClass().getSimpleName()+": Discover Peers ERROR: "+reasonCode);
 			}
 		});
 	}
 
 
 
 	/**
 	 * Disconnects the peer
 	 * 
 	 */
 	public void disconnect() {
 		manager.removeGroup(channel, new ActionListener(){
 			public void onSuccess() {
 				Log.d(TAG, this.getClass().getSimpleName()+": Group removed");
 			}
 			public void onFailure(int reason) {
 				Log.d(TAG, this.getClass().getSimpleName()+": Failed to remove group, reason = "+reason);
 			}
 		});
		context.unbindService(serviceConnection);
 		dataInterface.unregisterHandler(collectionHandler);
 
 	}
 
 	/**
 	 * Method used to send broadcast an XML message
 	 * 
 	 * @param msg
 	 */
 	public void sendBroadcast(final String msg) {
 		if(peer_id_ip_map != null && !peer_id_ip_map.isEmpty()){
 			TransmissionManagerFactory.getInstance().getTransmissionManager().send(
 				new ArrayList<String>(peer_id_ip_map.keySet()), 
 				MessageBuilder.getInstance().getMessage(msg)
 			);
 		}
 	}
 
 	/**
 	 * Calls connect for each device found
 	 * 
 	 */
 	public void connectToAll() {
 		for(WifiP2pDevice device : peers){
 			connect(device);
 		}
 	}
 
 	/**
 	 * Utility funtion unsed to post string messages
 	 * @param string
 	 */
 	private void showToast(String string) {
 		Message msg = new Message();
 		msg.obj = string;
 		msg.what = GuiHandlerInterface.SHOW_TOAST_MSG;
 		guiHandler.sendMessage(msg);
 	}
 
 }
