 /*Copyright [2010] [David Van de Ven]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
 */
 
 package org.wahtod.wififixer;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 //For old http check method
 /*import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpHead;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;*/
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.ConnectivityManager;
 import android.net.DhcpInfo;
 import android.net.NetworkInfo;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.PowerManager;
 import android.preference.PreferenceManager;
 import android.provider.Settings.SettingNotFoundException;
 import android.widget.Toast;
 
 public class WifiFixerService extends Service {
 
 	//Constants
 	//Hey, if you're poking into this, and have the brains to figure out my code,
 	//you can afford to donate.  I don't need a fancy auth scheme. 
 	public static final String FIXWIFI = "FIXWIFI";
 	public static final String AUTHSTRING="31415927";
 	//http://www.jerkcity.com  
 	private static final String AUTHEXTRA ="DICKS.ETC";
 	private static final String AUTH="AUTH";
 	//Runnable Constants for handler
 	private static final int MAIN = 0;
 	private static final int REPAIR = 1;
 	private static final int RECONNECT = 2;
 	private static final int WIFITASK = 3;
 	private static final int TEMPLOCK_ON = 4;
 	private static final int TEMPLOCK_OFF = 5;
 	private static final int WIFI_OFF = 6;
 	private static final int WIFI_ON = 7;
 	
 	//ID For notification
 	private static final int NOTIFID=31337;
 	
 	//Supplicant Constants
 	private static final String SCANNING="SCANNING";
 	private static final String DISCONNECTED="DISCONNECTED";
 	private static final String INACTIVE = "INACTIVE";
 	
 	//Target for header check
 	private static final String H_TARGET="http://google.com";
 	
 	//Logging Intent
 	private static final String LOGINTENT="org.wahtod.wififixer.LogService.LOG";
 	
 	//For wifi state
 	public static boolean WIFI_ENABLED=false;
 	// ms for IsReachable
 	final static int REACHABLE = 2500;
 	// ms for main loop sleep
 	final static int LOOPWAIT=5000;
 	//ms to wait after trying to connect
 	private static final int CONNECTWAIT = 10000;
 	// Enable logging
 	public static boolean LOGGING = false;
 	// *****************************
 	public final static String APP_NAME = "WifiFixerService";
 	public boolean CLEANUP = false;
 	public boolean HASLOCK = false;
 	public boolean LOCKPREF = false;
 	public boolean NOTIFPREF = false;
 	public boolean HTTPPREF = true;
 	public boolean RUNPREF = false;
 	public boolean SCREENPREF= false;
 	public boolean WIDGETPREF=false;
 	public boolean PREFSCHANGED = false;
 	public boolean WIFISHOULDBEON = false;
 	public boolean HASWAKELOCK=false;
 	//Locks and such
 	public boolean TEMPLOCK = false;
 	public static boolean SCREENISOFF = false;
 	public boolean SHOULDRUN = true;
 	//various
 	public int WIFIREPAIR = 0;
 	public int LASTNID = -1;
 
 	//flags
 	public boolean PENDINGSCAN=false;
 	public boolean PENDINGWIFITOGGLE=false;
 	public boolean PENDINGRECONNECT=false;
 	public boolean sCONNECTED=false;
 	//misc types
 	public String LASTSSID=" ";
 	public int VERSION=0;
 	 //Public Utilities!
 	//We do this to avoid GC thrash
 	 public  WifiManager wm;
 	 public  WifiInfo myWifi;
 	 public WifiManager.WifiLock lock;
 	 public SharedPreferences settings;
 	 public ScanResult sResult;
 	 public List<ScanResult> wifiList;
 	 PowerManager.WakeLock wakelock; 
 	
     
 	private  Handler hMain = new Handler(){
 		@Override
         public void handleMessage(Message message) {
             switch(message.what){
             
             case MAIN:
             hMain.post(rMain);	
             break;
             
             case REPAIR:
             hMain.post(rRepair);
             break;
             
             case RECONNECT:
             hMain.post(rReconnect);	
             break;
             
             case WIFITASK:
             hMain.post(rWifiTask);	
             break;
             
             case TEMPLOCK_ON:
             TEMPLOCK=true;
             if(LOGGING)
             	wfLog(APP_NAME,"Setting Temp Lock");
             break;
             
             case TEMPLOCK_OFF:
             TEMPLOCK=false;
             if(LOGGING)
             	wfLog(APP_NAME,"Removing Temp Lock");
             break;
             
             case WIFI_OFF:
             hMain.post(rWifiOff);
             break;
             
             case WIFI_ON:
             hMain.post(rWifiOn);
             break;
             	
             }
 		}
 	  };
 
 	 Runnable rRepair = new Runnable() {
 		public void run() {
 			if(!WIFI_ENABLED){
 				hMainWrapper(TEMPLOCK_OFF);
 				return;
 			}
 			
 	    if(isKnownAPinRange()){
 	    	if(connectToAP(LASTNID,true) && (getNetworkID() != -1)){
 	    	 PENDINGRECONNECT=false;
 	    	 if(LOGGING)
 	    		 wfLog(APP_NAME,"Connected to Network:"+getNetworkID());
 			}
 		    else{
 		    PENDINGRECONNECT=true;
 		    toggleWifi();
 		    if(LOGGING)
 				wfLog(APP_NAME,"Toggling Wifi.");
 			}
 						  
 	  }
 	  else
 		  hMainWrapper(TEMPLOCK_OFF);
 	  
 	  
 	}	
 
 	};
 	
 	 Runnable rReconnect = new Runnable() {
 		public void run() {
 			if(!WIFI_ENABLED){
 				hMainWrapper(TEMPLOCK_OFF);
 				return;
 			}
 			isKnownAPinRange();		  //Crazy but should work. 
 			if(connectToAP(LASTNID,true) && (getNetworkID() != -1)){
 					  PENDINGRECONNECT=false;
 					  if(LOGGING)
 				    		 wfLog(APP_NAME,"Connected to Network:"+getNetworkID());
 					}
 					  else{
 					 WIFIREPAIR=0;
 					 PENDINGSCAN=false;
 					 hMainWrapper(TEMPLOCK_OFF);
 					 if(LOGGING)
 							wfLog(APP_NAME,"Exiting N1 Fix thread.");
 					  }
 	
 					
 	}	
 
 	};
 
 	 Runnable rMain = new Runnable() {
 		public void run() {
 			//Queue next run of main runnable
 			hMainWrapper(MAIN, LOOPWAIT);
 			//Watchdog
 		    if(!WIFI_ENABLED)
 		    	checkWifiState();
 		    
 		    //Check Supplicant
 		    if(!wm.pingSupplicant() && WIFI_ENABLED){
 		    	if(LOGGING)
 		    		wfLog(APP_NAME,"Supplicant Nonresponsive, toggling wifi");
 		    	toggleWifi();
 		    }
 		    else	
 			if (!TEMPLOCK && !SCREENISOFF)
 					fixWifi();
 			
 		    if (PREFSCHANGED)
 					checkLock(lock);
 			
 			if(!SHOULDRUN){	
 			if (LOGGING)
 				wfLog(APP_NAME, "SHOULDRUN false, dying.");
 			// Cleanup
 			cleanup();
 			}
 			
 		}
 	};
 
 	 Runnable rWifiTask = new Runnable(){
 		public void run() {
 			// dispatch appropriate level
 			switch (WIFIREPAIR) {
 
 			case 0:
 				// Let's try to reassociate first..
 				wm.reassociate();
 				if (LOGGING)
 					wfLog(APP_NAME, "Reassociating");
 				tempLock(REACHABLE);
 				WIFIREPAIR++;
 				notifyWrap("Reassociating");
 				break;
 
 			case 1:
 				// Ok, now force reconnect..
 				wm.reconnect();
 				if (LOGGING)
 					wfLog(APP_NAME, "Reconnecting");
 				tempLock(REACHABLE);
 				WIFIREPAIR++;
 				notifyWrap("Reconnecting");
 				break;
 
 			case 2:
 				// Start Scan
 				PENDINGSCAN=true;
 				startScan();
 			    WIFIREPAIR=0;
 				if (LOGGING)
 					wfLog(APP_NAME, "Repairing");
 				notifyWrap("Repairing");
 				break;
 			}
 
 			if (LOGGING) {
 				wfLog(APP_NAME,"Reconnecting: Algorithm "
 						+ Integer.toString(WIFIREPAIR) + ":Last NID:"
 						+ Integer.toString(LASTNID));
 			}
 		}
 	};;
 	
 	 Runnable rWifiOff = new Runnable() {
 			public void run() {
 			wm.setWifiEnabled(false);
 		}	
 
 		};
 
 	 Runnable rWifiOn = new Runnable() {
 				public void run() {
 				wm.setWifiEnabled(true);
 				PENDINGWIFITOGGLE=false;
 			    WIFISHOULDBEON=true;
 			    if(HASWAKELOCK)
 			    	wakeLock(false);
 			    deleteNotification(NOTIFID);
 			}	
 
 			};
 
 		
 	private  BroadcastReceiver receiver = new BroadcastReceiver() {
 		public void onReceive(Context context, Intent intent) {
 
 			
             //We want this to run first
 			String iAction = intent.getAction();
 
 			if ((iAction.equals(Intent.ACTION_SCREEN_ON)) || (iAction.equals(Intent.ACTION_SCREEN_OFF)))
 					handleScreenAction(iAction);
 			else
 			if(iAction.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
 					handleWifiState(intent);
 			else
 			if(iAction.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION))
 					handleSupplicantIntent(intent);
 			else
 			if(iAction.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
 					handleWifiResults();
 			
 		}
 
 		
 	};
 	
 	
 	 void checkLock(WifiManager.WifiLock lock) {
 		if (!PREFSCHANGED) {
 			// Yeah, first run. Ok, if LOCKPREF true, acquire lock.
 			if (LOCKPREF) {
 				lock.acquire();
 				HASLOCK = true;
 				if (LOGGING)
 					wfLog(APP_NAME, "Acquiring Wifi Lock");
 			}
 		} else {
 			// ok, this is when prefs have changed, soo..
 			PREFSCHANGED = false;
 			if (LOCKPREF && !HASLOCK) {
 				// generate new lock
 				lock.acquire();
 				HASLOCK = true;
 				if (LOGGING)
 					wfLog(APP_NAME, "Acquiring Wifi Lock");
 			} else {
 				if (HASLOCK && !LOCKPREF) {
 					lock.release();
 					HASLOCK = false;
 					if (LOGGING)
 						wfLog(APP_NAME, "Releasing Wifi Lock");
 				}
 			}
 		}
 	}
 	
 	
 	
 	 void cleanup() {
 		
 		 if (!CLEANUP){
 			 
 			if (HASLOCK && lock.isHeld())
 				lock.release();
 			unregisterReceiver(receiver);
 			hMain.removeMessages(MAIN);
 			cleanupPosts();
 			CLEANUP=true;
 		 }
 		stopSelf();
 }
 	
 	 
 	 void cleanupPosts() {
 		 	hMain.removeMessages(RECONNECT);
 			hMain.removeMessages(REPAIR);
 			hMain.removeMessages(WIFITASK);
 			hMain.removeMessages(TEMPLOCK_ON);
 			hMain.removeMessages(TEMPLOCK_OFF);
 	 }
 	 
 	 boolean checkWifi() {
 		boolean hostup = false;
 		// Check out this tricky switch
 		if (HTTPPREF) {
 			hostup = httpHostup("google.com");
 			if (!hostup)
 				HTTPPREF = false;
 			else
 				WIFIREPAIR = 0;
 		} else {
 			//grab dhcp gateway addy
 			DhcpInfo info = wm.getDhcpInfo();
 			if (icmpHostup(intToIp(info.gateway))) {
 				hostup = true;
 				WIFIREPAIR = 0;
 				if(httpHostup("google.com"))
 					HTTPPREF=true;
 			} else
 				HTTPPREF = true;
 		}
 
 		return hostup;
 	}
 	 
 	void checkWifiState() {
 		if(!WIFI_ENABLED && WIFISHOULDBEON){
 			hMainWrapper(WIFI_ON);
 		}
 	}
 	
 	 boolean connectToAP(int AP, boolean disableOthers){
 		if (LOGGING)
 			wfLog(APP_NAME,"Connecting to Network:"+AP);
 		tempLock(CONNECTWAIT);
 		return wm.enableNetwork(AP, disableOthers);
 	}
 	
 	 
 	 void deleteNotification(int id) {
 		 NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		 nm.cancel(id);
 	 }
 	 
 	 void doWidgetAction() {
 		if (WIFI_ENABLED) {
 			if(WIDGETPREF){
 				Toast.makeText(WifiFixerService.this, "Toggling Wifi", Toast.LENGTH_LONG).show();
 				toggleWifi();
 			}else{
 			Toast.makeText(WifiFixerService.this, "Reassociating",
 					Toast.LENGTH_LONG).show();
 			WIFIREPAIR = 0;
 			wifiRepair();
 			}
 		} else
 			Toast.makeText(WifiFixerService.this, "Wifi Is Disabled",
 					Toast.LENGTH_LONG).show();
 	}
 
 	 void fixWifi() {
 		if (getIsWifiEnabled()) {
 			if (getSupplicantState()=="ASSOCIATED" || getSupplicantState()=="COMPLETED"){
 				if(!checkWifi()) {
 					wifiRepair();
 				}
 			}
 			else{
 				PENDINGSCAN=true;
 				tempLock(CONNECTWAIT);
 			}
 
 		}
 
 	}
     
 	boolean getHttpHeaders( String uri ) throws IOException {
 		
 		//dead simple, we'll see how this does
 		
 		boolean isup=false;
 		
 		try 
 		    {    
 		      URL url = new URL(uri);
 		      URLConnection conn = url.openConnection();
 		      conn.getHeaderField(1);
 		      isup=true;
 		      
 		    } 
 		    catch (Exception e) {
 		       isup=false;
 		    }
 			
 			return isup;
     }
 	 
 	 boolean getIsOnWifi(){
 	    boolean wifi=false;
 	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo ni = cm.getActiveNetworkInfo();
 	    //Null check, this can be null, so NPE
 	    if(ni != null && ni.getType()==ConnectivityManager.TYPE_WIFI)
 	    	wifi=true;
 		return wifi;
 	}
 	
 	
 	 boolean getIsWifiEnabled() {
 		boolean enabled = false;
 
 		if (WIFI_ENABLED) {
 			if (LOGGING)
 				wfLog(APP_NAME, "Wifi is Enabled");
 			enabled = true;
 		} else {
 			if (LOGGING)
 				wfLog(APP_NAME, "Wifi not Enabled");
 		}
 
 		
 
 		return enabled;
 	}
 	
 	 int getNetworkID() {
 		myWifi = wm.getConnectionInfo();
 		int id = myWifi.getNetworkId();
 		if (!(id == -1)) {
 			LASTNID = id;
 			LASTSSID = myWifi.getSSID();
 		}
 		return id;
 	}
 	
 	 void getPackageInfo() {
 		PackageManager pm = getPackageManager();
         try {
             //---get the package info---
             PackageInfo pi =  
                 pm.getPackageInfo("org.wahtod.wififixer", 0);
             //---display the versioncode--           
            VERSION=pi.versionCode;
         } catch (NameNotFoundException e) {
            //hurf ding
         }
 	}
 	
 	 String getSupplicantState() {
 		    myWifi = wm.getConnectionInfo();
 		 	return myWifi.getSupplicantState().toString();
 	}
 	
 		ArrayList<String> getWifiConfigurations() {
 		
 		List<WifiConfiguration> conflist=wm.getConfiguredNetworks();
 		WifiConfiguration wfCon;
 		ArrayList<String> myList = new ArrayList<String>();
 		for (int i=0;i < conflist.size(); i++){
 			wfCon=conflist.get(i);
 			myList.add(i,wfCon.SSID);
 		}
 		if(LOGGING)
 			wfLog(APP_NAME,"Configured Networks:"+myList.toString());
 		return myList;
 	}
 	
 	WifiManager getWifiManager() {
 		return (WifiManager) getSystemService(Context.WIFI_SERVICE);
 	}
 	
 	 void handleAuth(Intent intent){
 		if(intent.getStringExtra(AUTHEXTRA).contains(AUTHSTRING))
 		{
 			if (LOGGING)
 				wfLog(APP_NAME, "Yep, we're authed!");
 			//Ok, do the auth
 			settings = PreferenceManager.getDefaultSharedPreferences(this);
 			boolean ISAUTHED = settings.getBoolean("ISAUTHED", false);
 			if (!ISAUTHED){
 				SharedPreferences.Editor editor = settings.edit();
 				editor.putBoolean("ISAUTHED", true);
 				editor.commit();
 				showNotification("Thank you for your donation.", "Authorized", ISAUTHED);
 			}
 			
 		}
 	}
 
 
 	 void handleScreenAction(String iAction) {
         
 		if(SCREENPREF)
 			return;
 		
 		if (iAction.equals(Intent.ACTION_SCREEN_OFF)) {
 			SCREENISOFF = true;
 			if (LOGGING){
 				wfLog(APP_NAME, "SCREEN_OFF handler");
 				wfLog(LogService.SCREEN_OFF,null);
 			}
 		} else {
 			if (LOGGING){
 				wfLog(APP_NAME, "SCREEN_ON handler");
 				wfLog(LogService.SCREEN_ON,null);
 			}
 			SCREENISOFF=false;
 		}
 
 	}
 
 	 void handleStart(Intent intent) {
 		
 		
 		// Handle NPE
 		
 			try {
 				if (intent.hasExtra(FIXWIFI)) {
 					if (intent.getBooleanExtra(FIXWIFI, false)){
 						doWidgetAction();
 					}
 					if (LOGGING)
 						wfLog(APP_NAME, "Called by Widget");
 				} else {
 					
 					String iAction=intent.getAction();
 					//Looking for auth intent
 					if (iAction.contains(AUTH)){
 						handleAuth(intent);
 						return;
 					}
 					else{
 						loadPrefs();
 						PREFSCHANGED = true;
 						if (LOGGING)
 							wfLog(APP_NAME, "Normal Startup or reload");
 				}
 				}
 			} catch (NullPointerException e) {
 				if(LOGGING)
 					wfLog(APP_NAME,"Pesky Google and their Null Intent");
 			}
 		
 	}
 	 
 	 private void handleSupplicantIntent( Intent intent) {
 			//supplicant fixes
 			
 			String sState=intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE).toString();
 			
 			handleSupplicantState(sState);
 			
 	}
 	 
 	void handleSupplicantState(String sState){
 		
 		if(LOGGING)
 			logSupplicant(sState);
 		
 		if(!WIFI_ENABLED || SCREENISOFF)
 			return;
 		
 		if(sState==SCANNING)
 		{
 			PENDINGSCAN=true;
 			
 			
 			
 		}
 		else if(sState==DISCONNECTED){
 			PENDINGSCAN=true;
 			startScan();
 			notifyWrap(sState);
 		}
 		else if(sState==INACTIVE){
 		    supplicantFix(true);
 		    notifyWrap(sState);
 		}
 	}
 	
 	private void handleWifiResults(){
 		hMainWrapper(TEMPLOCK_OFF);
 		if(!WIFI_ENABLED)
 			return;	
 				
 		if (!PENDINGSCAN){
 			if (LOGGING)
 				wfLog(APP_NAME,"No Pending Scan.");
 			return;
 		}
 		
 		
 		if (!PENDINGRECONNECT){
 		
 		PENDINGSCAN=false;
 		hMainWrapper(REPAIR);
 		if (LOGGING)
 			wfLog(APP_NAME,"Scan Results Acquired:Running Repair_Handler");
 		}
 		else
 		{
 			PENDINGSCAN=false;
 			hMainWrapper(RECONNECT);
 			if (LOGGING)
 				wfLog(APP_NAME,"Scan Results Acquired:Running Reconnect_Handler");
 		}
 		
 	}
 	
 	void handleWifiState(Intent intent) {
 		// What kind of state change is it?
 		int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
 				WifiManager.WIFI_STATE_UNKNOWN);
 		switch (state) {
 		case WifiManager.WIFI_STATE_ENABLED:
 			if (LOGGING)
 				wfLog(APP_NAME, "WIFI_STATE_ENABLED");
 			hMainWrapper(TEMPLOCK_OFF,LOOPWAIT);
 			WIFI_ENABLED=true;
 			WIFISHOULDBEON=false;
 			break;
 		case WifiManager.WIFI_STATE_ENABLING:
 			if (LOGGING)
 				wfLog(APP_NAME, "WIFI_STATE_ENABLING");
 			break;
 		case WifiManager.WIFI_STATE_DISABLED:
 			if (LOGGING)
 				wfLog(APP_NAME, "WIFI_STATE_DISABLED");
 			hMainWrapper(TEMPLOCK_ON);
 			WIFI_ENABLED=false;
 			break;
 		case WifiManager.WIFI_STATE_DISABLING:
 			if (LOGGING)
 				wfLog(APP_NAME, "WIFI_STATE_DISABLING");
 			WIFI_ENABLED=false;
 			break;
 		case WifiManager.WIFI_STATE_UNKNOWN:
 			if (LOGGING)
 				wfLog(APP_NAME, "WIFI_STATE_UNKNOWN");
 			WIFI_ENABLED=false;
 			break;
 		}
 	}
 	
 	//Why do we do this? Because race can occur
 	//in the queue. 
 	void hMainWrapper(int hmain){
 		hMain.removeMessages(hmain);
 		if(hMainCheck(hmain))
 			hMain.sendEmptyMessage(hmain);
 	}
 	//whee overloading methods, <3 java
 	void hMainWrapper(int hmain,long delay){
 		hMain.removeMessages(hmain);
 		if(hMainCheck(hmain))
 				hMain.sendEmptyMessageDelayed(hmain,delay);
 	}
 	
 	boolean hMainCheck(int hmain) {
 		if (TEMPLOCK){
 			//prevent running posts during lock
 			if(hmain==RECONNECT || hmain==REPAIR || hmain==WIFITASK)
 				return false;
 		}
 		return true;
 	}
 
 	 boolean httpHostup(String host) {
 		boolean isUp = false;
 		//how's this for minimalist?
 		try {
 			isUp=getHttpHeaders(H_TARGET);
 		} catch (IOException e) {
 			wfLog(APP_NAME,"HTTP I/O Exception");
 		}
 		if (LOGGING)
 			wfLog(APP_NAME, "HTTP Method");
 		return isUp;
 	}
 	/*
 		//Oddly, we used to have our own implementation
 		//But using Google's APIs in this one over time causes less GC and 
 		//uses less memory
 	boolean httpHostup(String host) {
 		boolean isUp = true;
 	    DefaultHttpClient httpClient = new DefaultHttpClient();
 	    HttpParams my_httpParams = new BasicHttpParams();
         HttpConnectionParams.setConnectionTimeout(my_httpParams,REACHABLE);
         HttpConnectionParams.setSoTimeout(my_httpParams,REACHABLE);
 		try {
 			httpClient.execute(new HttpHead("http://" + host));
 		} catch (ClientProtocolException e) {
 			isUp = false;
 			if (LOGGING)
 				wfLog(APP_NAME, "httpHostup:ClientProtocolException");
 
 		} catch (IOException e) {
 			isUp = false;
 			if (LOGGING)
 				wfLog(APP_NAME, "httpHostup:IOException");
 		}
 		if (LOGGING)
 			wfLog(APP_NAME, "HTTP Method");
 		return isUp;
 	}*/
 
 
 	 boolean icmpHostup(String host) {
 		boolean isUp = false;
 		try {
 			if (InetAddress.getByName(host).isReachable(REACHABLE)) {
 				isUp = true;
 			}
 		} catch (UnknownHostException e) {
 			//hurf
 		} catch (IOException e) {
 			//burf
 		}
 		if (LOGGING)
 			wfLog(APP_NAME, "ICMP Method");
 		return isUp;
 	}
 
 	 String intToIp(int i) {
 		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
 				+ "." + ((i >> 24) & 0xFF);
 	}
 	
 	 boolean isKnownAPinRange() {
 		boolean state=false;
 		wifiList=wm.getScanResults();
 		//Bet you're wondering why this is here, aren't you?
 		//Because sometimes, rarely, this can run after wifi was disabled
 		//Which could mean the scan results are null
 		 if (wifiList == null){
 			if (LOGGING)
 				wfLog(APP_NAME,"Null Scan Results");
 			return false;
 		}
 	    //OK, this is butt ugly, but it works.  
 		ArrayList<String> wifiConfigs=getWifiConfigurations();
 		for (int i=0;i < (wifiList.size()); i++){
 			sResult=wifiList.get(i);
 			for (int i2=0;i2 < wifiConfigs.size(); i2++){
 			if(wifiConfigs.get(i2).toString().contains(sResult.SSID)){
 				if(LOGGING){
 					wfLog(APP_NAME,"Found SSID:"+sResult.SSID);
 					wfLog(APP_NAME,"Capabilities:"+sResult.capabilities);
 				}
 				  LASTNID=(i2);
 				  LASTSSID=sResult.SSID;
 				  state=true;
 				  return state;
 		  }
 		}
 		}
 		return state;
 	}
 	
 	 void loadPrefs() {
 		settings = PreferenceManager.getDefaultSharedPreferences(this);
 		LOCKPREF = settings.getBoolean("WiFiLock", false);
 		NOTIFPREF = settings.getBoolean("Notifications", false);
 		RUNPREF = settings.getBoolean("Disable", false);
 		SCREENPREF = settings.getBoolean("SCREEN", false);
 		WIDGETPREF = settings.getBoolean("WidgetBehavior", false);
 		String PERFORMANCE = settings.getString("Performance", "0");
 		//Kill the Log Service if it's up
 		if (LOGGING && !settings.getBoolean("SLOG", false))
 			wfLog(LogService.DIE,null);
 		LOGGING=settings.getBoolean("SLOG", false);
 		// Check RUNPREF and set SHOULDRUN
 		//Make sure Main loop restarts if this is a change
 		if (RUNPREF){
 			SHOULDRUN = false;
 		}
 		else {
 			if (!SHOULDRUN){
 				SHOULDRUN=true;
 			}
 		}
 		//Setting defaults if performance not set
 	    if(PERFORMANCE=="0" && !LOCKPREF){
 	    	SharedPreferences.Editor edit = settings.edit();
 	    	edit.putString("Performance","2");
 	    	edit.putBoolean("WiFiLock", true);
 	    	edit.commit();
 	    	LOCKPREF=true;
 	    }
 	    
 		if (LOGGING) {
 			wfLog(APP_NAME, "Loading Settings");
 			if (LOCKPREF)
 				wfLog(APP_NAME, "LOCKPREF");
 
 			if (NOTIFPREF)
 				wfLog(APP_NAME, "NOTIFPREF");
 
 			if (RUNPREF)
 				wfLog(APP_NAME, "RUNPREF");
 			
 			if (SCREENPREF)
 				wfLog(APP_NAME, "SCREENPREF");
 
 		}
 		
 		
 		// Here we go, checking for Wifi network notification
 		// Notify user if this setting is true
 		try {
 			int iNotif = android.provider.Settings.Secure
 					.getInt(
 							getContentResolver(),
 							android.provider.Settings.Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON);
 			if (iNotif == 1) {
 				showNotification("Please disable Wifi Network Notification.",
 						"Attention!", true);
 			}
 
 		} catch (SettingNotFoundException e) {
 			// bweep
 			if(LOGGING)
 				wfLog(APP_NAME,"Whoops! Obeselete!");
 		}
 	}
 
      void logSupplicant(String state){
     	
 		wfLog(APP_NAME,"Supplicant State:"+state);
 		if (wm.pingSupplicant()){
 			wfLog(APP_NAME,"Supplicant Responded");
 		}
 		else
 		{
 			wfLog(APP_NAME,"Supplicant Nonresponsive");
 			
 		}
 		wfLog(APP_NAME,"SSID:"+LASTSSID);
 		
     }
 
 	 void notifyWrap(String message) {
 		if (NOTIFPREF) {
 			showNotification("Wifi Connection Problem:" + message, message,
 					false);
 		}
 
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		if (LOGGING)
 			wfLog(APP_NAME, "OnBind:Intent:" + intent.toString());
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 	 
 	    wm = getWifiManager();
 	    WIFI_ENABLED=wm.isWifiEnabled();
 	    getPackageInfo();
 	    if(LOGGING){
 	    	wfLog(APP_NAME,"WifiFixerService Build:"+VERSION);
 	    }
 		loadPrefs();
 		
 		
 	    //Setup, formerly in Run thread
 		setup();
 		hMain.sendEmptyMessage(MAIN);
 	   	
 		if (LOGGING)
 			wfLog(APP_NAME, "OnCreate");
 		
 		
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		cleanup();
 	}
 
 	@Override
 	public void onStart(Intent intent, int startId) {
 		
 				handleStart(intent);
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		
 			handleStart(intent);
 			
 		return START_STICKY;
 	}
 	
 	void setup() {
 		//Yeah, so the constant WIFI_MODE_FULL wasn't obvious
 		//It makes a huge difference, may make default
 		lock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL,"WFLock");
 		checkLock(lock);
 		IntentFilter myFilter = new IntentFilter();
 		myFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
 		// Catch power events for battery savings
 		myFilter.addAction(Intent.ACTION_SCREEN_OFF);
 		myFilter.addAction(Intent.ACTION_SCREEN_ON);
 		//Supplicant State filter
 		myFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
 		//wifi scan results available callback 
 	    myFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
 	    registerReceiver(receiver, myFilter);
 		
 	}
 	
 	 void showNotification(String message, String tickerText, boolean bSpecial) {
 
 		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 	
 		CharSequence from = "Wifi Fixer";
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 				new Intent(), 0);
 		if (bSpecial) {
 			contentIntent = PendingIntent.getActivity(this, 0, new Intent(
 					android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
 		}
 
 		
 		Notification notif = new Notification(R.drawable.icon, tickerText,
 				System.currentTimeMillis());
 
 		notif.setLatestEventInfo(this, from, message, contentIntent);
 		notif.flags = Notification.FLAG_AUTO_CANCEL;
 		//unique ID
 		nm.notify(4144, notif);
 
 	}
 	
 	void showNotification(String message, String tickerText, int id){
 		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 		
 		CharSequence from = "Wifi Fixer";
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 				new Intent(), 0);
 		
 		Notification notif = new Notification(R.drawable.icon, tickerText,
 				System.currentTimeMillis());
 
 		notif.setLatestEventInfo(this, from, message, contentIntent);
 		notif.flags = Notification.FLAG_AUTO_CANCEL;
 		//unique ID
 		nm.notify(id, notif);
 	}
 
 	 void startScan() {
 		 //We want a lock after a scan 
 		wm.startScan();
 		tempLock(LOOPWAIT);
 	}
     
 	 void supplicantFix(boolean wftoggle){
 	 //Toggling wifi fixes the supplicant	
 			PENDINGSCAN=true;
 			if (wftoggle)
 				toggleWifi();
 			startScan();
 		    if (LOGGING)
 				wfLog(APP_NAME, "Running Supplicant Fix");
 	}
 	
 	 void tempLock(int time){
 		
 		 hMainWrapper(TEMPLOCK_ON);
         //Queue for later
 		hMainWrapper(TEMPLOCK_OFF, time);
 	}
 	
 	 void toggleWifi() {
 		if(PENDINGWIFITOGGLE)
 			return;
 		
 		PENDINGWIFITOGGLE=true;
 		cleanupPosts();
 		tempLock(CONNECTWAIT);
 		//Wake lock
 		wakeLock(true);
 		showNotification("Toggling Wifi", "Toggling Wifi", NOTIFID);
 		hMainWrapper(WIFI_OFF);
 		hMainWrapper(WIFI_ON, LOOPWAIT);
 	}
 
 	void wakeLock(boolean state){
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		if(state){
 			wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WFWakeLock");
 			wakelock.acquire();
 			HASWAKELOCK=true;
 		}
 		else{
 			wakelock.release();
 			HASWAKELOCK=false;
 		}
 		
 	}
 	 
 	 void wifiRepair() {
 		 hMainWrapper(WIFITASK);
 		if (LOGGING)
 			wfLog(APP_NAME,"Running Wifi Repair");
 	}
     
 	 void wfLog(String APP_NAME, String Message) {
 		Intent sendIntent = new Intent(LOGINTENT);
 		sendIntent.putExtra(LogService.APPNAME, APP_NAME);
 		sendIntent.putExtra(LogService.Message,Message);
 		startService(sendIntent);
 	}
 	
 	
 }
