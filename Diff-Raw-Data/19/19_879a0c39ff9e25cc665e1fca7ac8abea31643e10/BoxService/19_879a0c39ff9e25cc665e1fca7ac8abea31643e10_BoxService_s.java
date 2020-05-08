 package com.geniatech.client_box;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.util.List;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.LinkProperties;
 import android.net.NetworkInfo;
 import android.net.NetworkUtils;
 import android.net.ethernet.EthernetManager;
 import android.net.wifi.SupplicantState;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.net.wifi.WifiConfiguration.AuthAlgorithm;
 import android.net.wifi.WifiConfiguration.IpAssignment;
 import android.net.wifi.WifiConfiguration.KeyMgmt;
 import android.net.wifi.WifiConfiguration.ProxySettings;
 import android.net.wifi.WifiManager.MulticastLock;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 
 public class BoxService extends Service{
 	public static final String TAG = "BoxService";
 	public static final int MSG_REMOTE_INFO = 0;
 	public static final int MSG_LOCAL_BOOTUP = 1;
 	public static final int MSG_WAIT_WIFI_STATE = 2;
 	public static final int MSG_TIMER_1 = 3;
 	public static final String BROAD_ACTION = "BoxService_broadcatst_action";
 	private IntentFilter mFilter;
 	public static boolean mIsNetThreadRun=true;
 	private MulticastLock mMultiLock;
 	
 	private WifiManager mWifiManager;
 	ConnectivityManager mConnectManager;
 	private WifiUtils mWifiUtils;
 	private WifiManager.Channel mChanel;
 	private WifiManager.ActionListener mConnectListener;
 	
 	private String cmd_ssid = null;
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
 		mConnectManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
 		mWifiUtils = new WifiUtils(mWifiManager);
 		mChanel = mWifiManager.initialize(this, getMainLooper(), null);
 		//mWifiManager.setWifiEnabled(true);
 		//disableEthernet();
 		mConnectListener = new WifiManager.ActionListener() {
 	        public void onSuccess() {
 	        }
 	        public void onFailure(int reason) {
 	             startAp(Config.CMD_BOX_SSID, Config.CMD_BOX_PASSWORD, 1);
 	        }
 	    };
 				
 		mMultiLock=mWifiManager.createMulticastLock("multiLock");
 		mMultiLock.acquire();
 		registerReceiver();
 		
 		mBoxServiceHandle.sendEmptyMessageDelayed(MSG_LOCAL_BOOTUP, 2000);
 	}
 	@Override
 	public void onDestroy() {
 		mMultiLock.release();
 		mIsNetThreadRun = false;
 		unregisterReceiver(mReceiver);
 		
 		if(Config.DEBUG) Log.i(TAG,"--=-=-=-=-=-=-->>>>>>>>>>>>onDestroy()");
 		super.onDestroy();
 	}
 	public void multicastListen(){
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 		        MulticastListener ml = new MulticastListener(Config.MULTICAST_HOST, Config.MULTICAST_PORT);
 		        while(mIsNetThreadRun) {
 		            String str = ml.listen();
 		            if(Config.DEBUG) Log.i(TAG,"=============== multicastListen ===close==== "+str);
 		            if(str == null) continue;
 		            MsgUtils msgutil = new MsgUtils(str);
 		            Message msg = Message.obtain(mBoxServiceHandle, MSG_REMOTE_INFO, msgutil);
 		            mBoxServiceHandle.sendMessage(msg);
 		        }
 			}
 		}).start();
 	}
 	public void cmdListen(){
 		new Thread(new Runnable() {
 			public void run() {
 				while(mIsNetThreadRun){
 					ServerSocket s = null;
 			        Socket socket = null;
 			        BufferedReader br = null;
 			        PrintWriter pw = null;
 			        try {
 			            s = new ServerSocket(Config.DATA_PORT);
 			            s.setSoTimeout(Config.LISTEN_TIME_OUT);
 			            
 			            socket = s.accept();
 			            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 			            String str = br.readLine(); 
 			            //while(str!=null){
 				            MsgUtils msgutil = new MsgUtils(str);
 				            Message msg = Message.obtain(mBoxServiceHandle, MSG_REMOTE_INFO, msgutil);
 				            mBoxServiceHandle.sendMessage(msg);
 				            //str = br.readLine();
 			            //}
 			            
 			        } catch (SocketTimeoutException e) {
 			            
 			        }catch(Exception e){
 			        	e.printStackTrace();  
 			        }finally{  
 			        	if(Config.DEBUG) Log.i(TAG,"============cmdListen!!====close...=============="); 
 			            try {
 			                if(br!=null) br.close();
 			                //pw.close();
 			                if(socket!=null) socket.close();
 			                if(s!=null) s.close();
 			            } catch (Exception e2) {
 			                e2.printStackTrace();
 			            }
 			        }
 				}
 			}
 		}).start();
 	}
 	public void sendMulticast(String info){
 		final String str = info;
 		new Thread(new Runnable() {
 			Multicastsender ms = new Multicastsender(str, WifiUtils.getBroadcastIp(), Config.MULTICAST_PORT);
 			@Override
 			public void run() {
 		        ms.send();
 			}
 		}).start();
 	}
 	public void bootupWorks(){
 		mWifiManager.setWifiApEnabled(null, false);
 		mWifiManager.setWifiEnabled(true);
 		int state = mWifiManager.getWifiState();
 		while(state != WifiManager.WIFI_STATE_ENABLED){ /* wait wifi enabled then we can get the configures.*/
 			try{
 				Thread.sleep(1000);
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 			state = mWifiManager.getWifiState();
 		}
 		List<WifiConfiguration> wifiConfs = mWifiManager.getConfiguredNetworks();
 		if(wifiConfs==null || wifiConfs.size()==0){ /* if no configures goto AP state */
 			enableAp();
 			if(Config.DEBUG) Log.i(TAG,"=====================   wifiConfs is null!!   =========");
 			return;
 		}
 		
 		if(Config.DEBUG) Log.i(TAG,"===================wifiConfs.size()===========>>"+wifiConfs.size());
 		WifiConfiguration wifiConf = wifiConfs.get(0);
 		for(WifiConfiguration conf:wifiConfs){
 			if(Config.DEBUG) Log.i(TAG,"==============================>>wifiConf:"+conf.SSID);
 			if(conf.priority > wifiConf.priority){
 				mWifiManager.removeNetwork(wifiConf.networkId);
 				wifiConf = conf;
 			}
 		}
 		/* now we know wifi had configured,so just open wifi and goto wait wifi opend. */
 		Message msg =  mBoxServiceHandle.obtainMessage();
 		msg.what = MSG_WAIT_WIFI_STATE;
 		msg.arg1 = 0;
 		mBoxServiceHandle.sendMessageDelayed(msg,1000);
 		
 	}
 	private void waitWifiState(Message msg){
 		WifiInfo winfo = mWifiManager.getConnectionInfo();
 		SupplicantState supState = winfo.getSupplicantState();
 		Log.i(TAG,"==============================>>wifi SupplicantState--->"+supState);
 		
 		if(winfo.getIpAddress()!=0){ /* ok,wifi have connected.*/
 			return;
 		}
 		if(supState.equals(SupplicantState.INACTIVE)||msg.arg1>Config.WAIT_WIFI_TIME){
 			enableAp();
 			return;
 		}
 		 /* continue wait.*/
 		Message msgt =  mBoxServiceHandle.obtainMessage();
 		msgt.what = MSG_WAIT_WIFI_STATE;
 		msgt.arg1 = msg.arg1 + 1;
 		mBoxServiceHandle.sendMessageDelayed(msgt, 1000);
 		
 	}
 	private void enableAp(){
         Message msg = Message.obtain(mBoxServiceHandle, MSG_REMOTE_INFO, MsgUtils.getDefWifi2ApMsg());
 		mBoxServiceHandle.sendMessage(msg);
 	}
 	public boolean isCorrectWifi(){
 		String ssid = WifiUtils.getActiveSSID();
 		if(cmd_ssid == null) return true;
 		if(ssid != null && cmd_ssid.contains(ssid)){
 			return true;
 		}else{
 			return false;
 		}
 	}
 	private void startTimer1(){
 		Message msg = mBoxServiceHandle.obtainMessage(MSG_TIMER_1, 0, 0);
 		mBoxServiceHandle.sendMessage(msg);
 	}
 	private void stopTimer1(){
 		mBoxServiceHandle.removeMessages(MSG_TIMER_1);
 	}
     private Handler mBoxServiceHandle = new Handler(){
         public void handleMessage(Message msg) {
             if(MSG_REMOTE_INFO==msg.what){
             	MsgUtils msgutil = (MsgUtils)msg.obj;
             	handleRemoteInfo(msgutil);
             }else if(MSG_LOCAL_BOOTUP==msg.what){
             	bootupWorks();
         		multicastListen();
         		cmdListen();
             }else if(MSG_WAIT_WIFI_STATE == msg.what){
             	if(Config.DEBUG) Log.i(TAG,"======================>>msg MSG_WAIT_WIFI_STATE");
             	waitWifiState(msg);
             }else if(MSG_TIMER_1 == msg.what){
             	if(Config.DEBUG) Log.i(TAG,"======================MSG_TIMER_1:"+msg.arg1);
             	
         		
             	if(!isCorrectWifi()){
             		if(msg.arg1 > Config.WAIT_WIFI_TIME){
             			startAp(Config.DEF_AP_SSID,Config.DEF_AP_PASSWORD,Config.DEF_AP_SEC_TYPE);
             			return;
             		}
             		Message m = obtainMessage();
             	m.arg1 = msg.arg1 + 1;
             	m.what = MSG_TIMER_1;
         		sendMessageDelayed(m, 1000);
             	}else{
             		cmd_ssid = null;
             	}
             	
             }
         }
     };
     private void handleRemoteInfo(MsgUtils msgutil){
     	String cmd = msgutil.getCmd();
     	if(cmd==null) return;
     	if(Config.DEBUG) Log.i(TAG,"===============handleRemoteInfo=======>>msg cmd:"+msgutil.toString());
     	if(cmd.equals(Config.CMD_BOX_WIFI_2_AP)){
     		String ssid = msgutil.getInfo(Config.CMD_BOX_SSID);
     		String password = msgutil.getInfo(Config.CMD_BOX_PASSWORD);
     		String secure = msgutil.getInfo(Config.CMD_BOX_SEC_TYPE);
     		if(secure == null){
     			startAp(Config.DEF_AP_SSID,Config.DEF_AP_PASSWORD,Config.DEF_AP_SEC_TYPE);
     			return;
     		}
     		int secureType = Integer.valueOf(secure);
     		startAp(ssid,password,secureType);
     	}else if(cmd.equals(Config.CMD_BOX_AP_2_WIFI)){
     		String ssid = msgutil.getInfo(Config.CMD_BOX_SSID);
     		String password = msgutil.getInfo(Config.CMD_BOX_PASSWORD);
     		String secure = msgutil.getInfo(Config.CMD_BOX_SEC_TYPE);
     		if(secure == null){
     			//startAp(Config.DEF_AP_SSID,Config.DEF_AP_PASSWORD,Config.DEF_AP_SEC_TYPE);
     			return;
     		}
     		int secureType = Integer.valueOf(secure);
     		startWifi(ssid,password,secureType);
     	}else if(cmd.equals(Config.CMD_PHONE_REQUEST_ID)){
     		reportBoxInfo();
     	}
     }
     private void removeConfigs(){
     	List<WifiConfiguration> wifiConfs = mWifiManager.getConfiguredNetworks();
 		if(wifiConfs==null || wifiConfs.size()==0){
 			return;
 		}
 		for(WifiConfiguration conf:wifiConfs){
 			mWifiManager.removeNetwork(conf.networkId);
 		}
     }
     private void registerReceiver(){
     	mFilter = new IntentFilter();
     	mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
         mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
         mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
         mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
         mFilter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
         mFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
         mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
         mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
         registerReceiver(mReceiver, mFilter);
     }
     private void disableEthernet(){
     	EthernetManager ethManager = (EthernetManager)getSystemService(Context.ETH_SERVICE);
     	ethManager.setEthEnabled(false);
     }
     private void startAp(String ssid,String password,int type){
     	mWifiManager.setWifiEnabled(false);
     	WifiConfiguration wc = getApConfig(ssid, password, type);
     	mWifiManager.setWifiApEnabled(wc, true);
     	if(Config.DEBUG) Log.i(TAG,"======startAp--->ssid:"+ssid+"--password:"+password+"----type:"+type);
     }
     private void startWifi(String ssid,String password,int type){
     	mWifiManager.setWifiApEnabled(null, false);
 		mWifiManager.setWifiEnabled(true);
 		
 		WifiConfiguration wc = getWifiConfig(ssid, password, type);
 		int state = mWifiManager.getWifiState();
 		while(state != WifiManager.WIFI_STATE_ENABLED){ /* wait wifi enabled then we can get the configures.*/
 			try{
 				Thread.sleep(1000);
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 			state = mWifiManager.getWifiState();
 		}
 		mWifiManager.connect(mChanel, wc, mConnectListener);
 		cmd_ssid = ssid;
 		startTimer1();
     	if(Config.DEBUG) Log.i(TAG,"======start WIFI--->ssid:"+ssid+"--password:"+password+"----type:"+type);
     }
     public WifiConfiguration getApConfig(String ssid,String password,int type) {
         if(ssid == null) return null;
         WifiConfiguration config = new WifiConfiguration();
         config.SSID = ssid;
         switch (type) {
             case 0:
                 config.allowedKeyManagement.set(KeyMgmt.NONE);
                 return config;
             case 1:
                 config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                 config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                 if (password.length() != 0) {
                     String passwordd = password;
                     config.preSharedKey = passwordd;
                 }
                 return config;
             case 2:
                 config.allowedKeyManagement.set(KeyMgmt.WPA2_PSK);
                 config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                 if (password.length() != 0) {
                     String passwordd = password;
                     config.preSharedKey = passwordd;
                 }
                 return config;
         }
         return null;
     }
     static String convertToQuotedString(String string) {
         return "\"" + string + "\"";
     }
     static WifiConfiguration getWifiConfig(String ssid,String password,int type){
     	WifiConfiguration wc = new WifiConfiguration();
     	//password = "qwer1234";
 		wc.SSID = convertToQuotedString(ssid);
 		wc.allowedKeyManagement.set(type);
 		if(password == null) return null;
 		password = password.replace("\"", "");
 		 if (password.matches("[0-9A-Fa-f]{64}")) {
 			 wc.preSharedKey = password;
          } else {
         	 wc.preSharedKey = '"' + password + '"';
          }
 		//wc.preSharedKey = "qwer1234";
 		wc.proxySettings = ProxySettings.UNASSIGNED;
 		wc.ipAssignment = IpAssignment.UNASSIGNED;
 		wc.linkProperties = new LinkProperties();
 		wc.networkId = -1;
 		return wc;
     }
     private BroadcastReceiver mReceiver = new BroadcastReceiver() {
         public void onReceive(Context context, Intent intent) {
         	String action = intent.getAction();
             if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                 //updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_UNKNOWN));
                 Log.i(TAG,"---------------------RSSI_CHANGED_ACTION-----------------------------"
                         +intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_UNKNOWN));
             } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action) ||
                     WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION.equals(action) ||
                     WifiManager.LINK_CONFIGURATION_CHANGED_ACTION.equals(action)) {
                     //updateAccessPoints();
                     Log.i(TAG,"---------------------SCAN_RESULTS_AVAILABLE_ACTION-----------------------------"
                             +intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_UNKNOWN));
             } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                 SupplicantState state = (SupplicantState) intent.getParcelableExtra(
                         WifiManager.EXTRA_NEW_STATE);
                 //if (!mConnected.get() && SupplicantState.isHandshakeState(state)) {
                 //    updateConnectionState(WifiInfo.getDetailedStateOf(state));
                 //}
                 Log.i(TAG,"---------------------SUPPLICANT_STATE_CHANGED_ACTION-----------------------------"
                         +intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_UNKNOWN));
             } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                 NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                         WifiManager.EXTRA_NETWORK_INFO);
                 //updateAccessPoints();
                 //updateConnectionState(info.getDetailedState());
                 Log.i(TAG,"---------------------NETWORK_STATE_CHANGED_ACTION-----------------------------"+info.getDetailedState());
             } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
             	Log.i(TAG,"---------------------RSSI_CHANGED_ACTION-----------------------------");
             }
         }
     };
     private String getBoxID(){
     	return Config.DEF_AP_SSID;
     }
     private String getBoxIp(){
     	WifiInfo winfo = mWifiManager.getConnectionInfo();
     	InetAddress addr = NetworkUtils.intToInetAddress(winfo.getIpAddress());
     	return addr.getHostAddress();
     }
     private void reportBoxInfo(){
     	if(getBoxIp().equals("0.0.0.0")) return;
     	String reportStr = Config.CMD_BOX_REPORT_ID + "#"
 				+Config.CMD_BOX_ID + getBoxID() + "#"
 				+Config.CMD_BOX_IP + getBoxIp();
     	sendMulticast(reportStr);
     }
 }
