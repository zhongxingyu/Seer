 package postpc.musica;
 
 import java.util.Collection;
 
 import android.net.wifi.p2p.WifiP2pDevice;
 import android.net.wifi.p2p.WifiP2pManager;
 import android.net.wifi.p2p.WifiP2pManager.Channel;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.IntentFilter;
 import android.view.Menu;
 
 
 
 public class WelcomeActivity extends Activity {
 	WifiP2pManager mManager;
 	Channel mChannel;
 	BroadcastReceiver mReceiver;
 	IntentFilter mIntentFilter;
 	Collection<WifiP2pDevice> currentDevices;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_welcome);
 		mIntentFilter = new IntentFilter();
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
 		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
 		mChannel = mManager.initialize(this, getMainLooper(), null);
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.welcome, menu);
 		return true;
 	}
 	
 	/* register the broadcast receiver with the intent values to be matched */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
 		registerReceiver(mReceiver, mIntentFilter);
 	}
 
 	/* unregister the broadcast receiver */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(mReceiver);
 	}
 	
 }
