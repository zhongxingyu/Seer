 package postpc.musica;
 
 import java.util.Collection;
 
 import android.net.wifi.p2p.WifiP2pDevice;
 import android.net.wifi.p2p.WifiP2pManager;
 import android.net.wifi.p2p.WifiP2pManager.ActionListener;
 import android.net.wifi.p2p.WifiP2pManager.Channel;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.IntentFilter;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.view.Menu;
 
 
 
 public class WelcomeActivity extends ParentActivity {
 	WifiP2pManager mManager;
 	Channel mChannel;
 	BroadcastReceiver mReceiver;
 	IntentFilter mIntentFilter;
 	Collection<WifiP2pDevice> currentDevices;
 	private CommunicationBinder myCom;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_welcome);
 		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
 		mChannel = mManager.initialize(this, getMainLooper(), null);
 		mManager.removeGroup(mChannel, new RemoveGroupListener());
 		myCom = (CommunicationBinder) getApplication();
 		myCom.mChannel = mChannel;
 		myCom.mManager = mManager;
 		myCom.mReceiver = mReceiver;
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
 		if (mReceiver != null){
 			mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
 			registerReceiver(mReceiver, mIntentFilter);
 		}
 	}
 
 	/* unregister the broadcast receiver */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(mReceiver);
 	}
 	private class RemoveGroupListener implements ActionListener{
 
 		@Override
 		public void onFailure(int arg0) {
 			mManager.cancelConnect(mChannel, new CancelConnectionsListener());
 		}
 
 		@Override
 		public void onSuccess() {
 			mManager.cancelConnect(mChannel, new CancelConnectionsListener());
 			
 			
 		}
 		
 	}
 	private class CancelConnectionsListener implements ActionListener{
 
 		@Override
 		public void onFailure(int arg0) {
 			startWifiActivity();
 		}
 
 		@Override
 		public void onSuccess() {
 			startWifiActivity();
 		}	
 	}
 	
 	
 	
 	private void startWifiActivity (){
 		mIntentFilter = new IntentFilter();
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
 		mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
 		registerReceiver(mReceiver, mIntentFilter);
 	}
 	
 	public WelcomeActivity getAction() {
 		return this;
 	}
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 	}
 }
