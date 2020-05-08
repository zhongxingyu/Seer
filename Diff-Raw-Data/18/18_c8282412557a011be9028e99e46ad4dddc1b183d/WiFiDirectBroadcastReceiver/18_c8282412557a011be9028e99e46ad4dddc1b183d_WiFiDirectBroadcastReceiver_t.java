 package fr.eurecom.messaging;
 
 
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.NetworkInfo;
 import android.net.wifi.p2p.WifiP2pDevice;
 import android.net.wifi.p2p.WifiP2pDeviceList;
 import android.net.wifi.p2p.WifiP2pManager;
 import android.net.wifi.p2p.WifiP2pManager.Channel;
 import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
 import android.util.Log;
 import android.widget.Toast;
 import fr.eurecom.cardify.Lobby;
 
 public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
 
 	private WifiP2pManager mManager;
 	private Channel mChannel;
 	private Lobby lobby;
 
 	public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, Lobby activity) {
 		super();
 		this.mManager = manager;
 		this.mChannel = channel;
 		this.lobby = activity;
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		CardPeerListListener cardPeerListListener = new CardPeerListListener();
 		String action = intent.getAction();
 		
 		Log.d("WifiDirectBroadcastReciever.onRecieve()", "In method. Action: " + action);
 		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
 			Log.d("WifiDirectBroadcastReciever.onRecieve()", "wifi enabled?");
 			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
 	        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
 	        	Log.d("WifiDirectBroadcastReciever.onRecieve()", "wifi p2p is enabled");
 	        	Toast.makeText(context, "WiFi Direct is enabled :)", Toast.LENGTH_LONG).show();
 	        } else {
 	        	Log.d("WifiDirectBroadcastReciever.onRecieve()", "wifi p2p is not enabled");
 	        	Toast.makeText(context, "WiFi direct is not enabled. Turn on WiFi Direct!", Toast.LENGTH_LONG).show();
 	        }
 		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
 			Log.d("WifiDirectBroadcastReciever.onRecieve()", "peers changed");
 		    if (mManager != null) {
 		        mManager.requestPeers(mChannel, cardPeerListListener);
 		    }
 		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
 			Log.d("WifiDirectBroadcastReciever.onRecieve()", "Connection changed");
 			NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
 			if (networkInfo.isConnected()) {
                 mManager.requestConnectionInfo(mChannel, lobby);
                 Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
                 lobby.dismissProgressDialog();
             } else {
             	Toast.makeText(context, "Disonnected", Toast.LENGTH_SHORT).show();
             	lobby.removeStartButton();
//            	lobby.removeClient();
             }
 		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
 			Toast.makeText(context, "WiFi direct this device changed", Toast.LENGTH_SHORT).show();
 		} else {
 			Log.e("WifiDirectBroadcastReciever.onRecieve()", "Something else happened in onreceive. Action: " + action);
 		}
 	}
 	
 	private class CardPeerListListener implements PeerListListener {
 		@Override
 		public void onPeersAvailable(WifiP2pDeviceList peers) {
 			Log.d("WifiDirectBroadcastReciever.onRecieve()", "Found Peers!");
 
 			for (WifiP2pDevice peer : peers.getDeviceList()) {
 				Log.d("WifiDirectBroadcastReciever.onRecieve()", "Peer Name: " + peer.deviceName);
 			}
 			lobby.setPeers(peers);
 		}		
 	}
 	
 }
