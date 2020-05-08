 package it.giacomos.android.osmer.network;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 
 public class NetworkStatusMonitor extends BroadcastReceiver 
 {
 	public NetworkStatusMonitor(NetworkStatusMonitorListener networkStatusMonitorListener)
 	{
 		m_networkStatusMonitorListener = networkStatusMonitorListener;
 	}
 	
 	public  void onReceive(Context context, Intent intent)
 	{
		ConnectivityManager cm =
		        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo currentNetworkInfo = cm.getActiveNetworkInfo();
		
		m_isConnected = currentNetworkInfo != null && currentNetworkInfo.isConnectedOrConnecting();
		
 		if(m_isConnected)
 			m_networkStatusMonitorListener.onNetworkBecomesAvailable();
 		else 
 			m_networkStatusMonitorListener.onNetworkBecomesUnavailable();
 	}
 
 	public boolean isConnected() { return m_isConnected; }
 	
 	private NetworkStatusMonitorListener m_networkStatusMonitorListener;
 	
 	private boolean m_isConnected;
 
 }
