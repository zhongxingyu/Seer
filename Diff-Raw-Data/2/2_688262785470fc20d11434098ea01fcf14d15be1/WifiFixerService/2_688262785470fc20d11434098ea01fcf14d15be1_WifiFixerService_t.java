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
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpHead;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 
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
 import android.net.wifi.SupplicantState;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Build;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.PowerManager;
 import android.preference.PreferenceManager;
 import android.text.format.Formatter;
 import android.widget.Toast;
 
 public class WifiFixerService extends Service {
 
     /*
      * Hey, if you're poking into this, and have the brains to figure out my
      * code, you can afford to donate. I don't need a fancy auth scheme.
      */
 
     // Constants
     public static final String FIXWIFI = "FIXWIFI";
     public static final String AUTHSTRING = "31415927";
     // http://www.jerkcity.com
     private static final String AUTHEXTRA = "DICKS.ETC";
     private static final String AUTH = "AUTH";
     // Runnable Constants for handler
     private static final int MAIN = 0;
     private static final int REPAIR = 1;
     private static final int RECONNECT = 2;
     private static final int WIFITASK = 3;
     private static final int TEMPLOCK_ON = 4;
     private static final int TEMPLOCK_OFF = 5;
     private static final int WIFI_OFF = 6;
     private static final int WIFI_ON = 7;
 
     // Preference key constants
     private static final String WIFILOCK_KEY = "WiFiLock";
     private static final String NOTIF_KEY = "Notifications";
     private static final String SCREEN_KEY = "SCREEN";
     private static final String DISABLE_KEY = "Disable";
     private static final String WIDGET_KEY = "WidgetBehavior";
     private static final String PERFORMANCE_KEY = "Performance";
     private static final String LOG_KEY = "SLOG";
     private static final String SUPFIX_KEY = "SUPFIX";
     private static final String SUPFIX_DEFAULT = "SPFDEF";
 
     // ID For notification
     private static final int NOTIFID = 31337;
 
     // Supplicant Constants
     private static final String SCANNING = "SCANNING";
     private static final String DISCONNECTED = "DISCONNECTED";
     private static final String INACTIVE = "INACTIVE";
 
     // Target for header check
     private static final String H_TARGET = "http://www.google.com";
     private static URI headURI;
 
     // Logging Intent
     private static final String LOGINTENT = "org.wahtod.wififixer.LogService.LOG";
 
     // ms for IsReachable
     final static int REACHABLE = 3000;
     final static int HTTPREACH = 8000;
     // ms for main loop sleep
     final static int LOOPWAIT = 10000;
     // ms for lock delays
     final static int LOCKWAIT = 5000;
     // ms to wait after trying to connect
     private static final int CONNECTWAIT = 10000;
 
     // Enable logging
     public static boolean logging = false;
     // *****************************
     public final static String APP_NAME = "WifiFixerService";
     public boolean cleanup = false;
     public boolean haslock = false;
     public boolean lockpref = false;
     public boolean notifpref = false;
     public boolean runpref = false;
     public boolean supfix = false;
     public boolean screenpref = false;
     public boolean widgetpref = false;
     public boolean prefschanged = false;
     public boolean wifishouldbeon = false;
     public boolean haswakelock = false;
 
     // Locks and such
     public boolean templock = false;
     public static boolean screenisoff = false;
     public boolean shouldrun = true;
     // various
     public int wifirepair = 0;
     private static final int HTTP_NULL = -1;
     public int lastnid = HTTP_NULL;
     private String cachedIP;
 
     // flags
     public boolean pendingscan = false;
     public boolean pendingwifitoggle = false;
     public boolean pendingreconnect = false;
     public boolean sconnected = false;
     // Switch for network check type
     public boolean httppref = false;
     // http://bash.org/?924453
 
     // misc types
     public String lastssid = " ";
     public int version = 0;
     // Public Utilities
     public WifiManager wm;
     public WifiInfo myWifi;
     public WifiManager.WifiLock lock;
     public SharedPreferences settings;
     public ScanResult sResult;
     public List<ScanResult> wifiList;
     private PowerManager.WakeLock wakelock;
     private DefaultHttpClient httpclient;
     private HttpParams httpparams;
     private HttpHead head;
     private HttpResponse response;
 
     private Handler hMain = new Handler() {
 	@Override
 	public void handleMessage(Message message) {
 	    switch (message.what) {
 
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
 		templock = true;
 		if (logging)
 		    wfLog(APP_NAME, "Setting Temp Lock");
 		break;
 
 	    case TEMPLOCK_OFF:
 		templock = false;
 		if (logging)
 		    wfLog(APP_NAME, "Removing Temp Lock");
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
 	    if (!getIsWifiEnabled()) {
 		hMainWrapper(TEMPLOCK_OFF);
 		return;
 	    }
 
 	    if (isKnownAPinRange()) {
 		if (connectToAP(lastnid, true) && (getNetworkID() != HTTP_NULL)) {
 		    pendingreconnect = false;
 		    if (logging)
 			wfLog(APP_NAME, "Connected to Network:"
 				+ getNetworkID());
 		} else {
 		    pendingreconnect = true;
 		    toggleWifi();
 		    if (logging)
 			wfLog(APP_NAME, "Toggling Wifi.");
 		}
 
 	    } else
 		hMainWrapper(TEMPLOCK_OFF);
 
 	}
 
     };
 
     Runnable rReconnect = new Runnable() {
 	public void run() {
 	    if (!getIsWifiEnabled()) {
 		hMainWrapper(TEMPLOCK_OFF);
 		return;
 	    }
 	    isKnownAPinRange(); // Crazy but should work.
 	    if (connectToAP(lastnid, true) && (getNetworkID() != HTTP_NULL)) {
 		pendingreconnect = false;
 		if (logging)
 		    wfLog(APP_NAME, "Connected to Network:" + getNetworkID());
 	    } else {
 		wifirepair = 0;
 		pendingscan = false;
 		hMainWrapper(TEMPLOCK_OFF);
 		if (logging)
 		    wfLog(APP_NAME, "Exiting N1 Fix thread.");
 	    }
 
 	}
 
     };
 
     Runnable rMain = new Runnable() {
 	public void run() {
 	    // Queue next run of main runnable
 	    hMainWrapper(MAIN, LOOPWAIT);
 	    // Watchdog
 	    if (!getIsWifiEnabled())
 		checkWifiState();
 
 	    // Check Supplicant
 	    if (!wm.pingSupplicant() && getIsWifiEnabled()) {
 		if (logging)
 		    wfLog(APP_NAME, "Supplicant Nonresponsive, toggling wifi");
 		toggleWifi();
 	    } else if (!templock && !screenisoff)
 		fixWifi();
 
 	    if (prefschanged)
 		checkLock(lock);
 
 	    if (!shouldrun) {
 		if (logging) {
 		    wfLog(APP_NAME, "SHOULDRUN false, dying.");
 		}
 		// Cleanup
 		cleanup();
 	    }
 
 	}
     };
 
     Runnable rWifiTask = new Runnable() {
 	public void run() {
 	    // dispatch appropriate level
 	    switch (wifirepair) {
 
 	    case 0:
 		// Let's try to reassociate first..
 		wm.reassociate();
 		if (logging)
 		    wfLog(APP_NAME, "Reassociating");
 		tempLock(REACHABLE);
 		wifirepair++;
 		notifyWrap("Reassociating");
 		break;
 
 	    case 1:
 		// Ok, now force reconnect..
 		wm.reconnect();
 		if (logging)
 		    wfLog(APP_NAME, "Reconnecting");
 		tempLock(REACHABLE);
 		wifirepair++;
 		notifyWrap("Reconnecting");
 		break;
 
 	    case 2:
 		// Start Scan
 		pendingscan = true;
 		startScan();
 		wifirepair = 0;
 		if (logging)
 		    wfLog(APP_NAME, "Repairing");
 		notifyWrap("Repairing");
 		break;
 	    }
 
 	    if (logging) {
 		wfLog(APP_NAME, "Reconnecting: Algorithm "
 			+ Integer.toString(wifirepair) + ":Last NID:"
 			+ Integer.toString(lastnid));
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
 	    pendingwifitoggle = false;
 	    wifishouldbeon = true;
 	    if (haswakelock)
 		wakeLock(false);
 	    deleteNotification(NOTIFID);
 	}
 
     };
 
     private BroadcastReceiver receiver = new BroadcastReceiver() {
 	public void onReceive(Context context, Intent intent) {
 
 	    /*
 	     * Dispatches the broadcast intent to the appropriate handler method
 	     */
 
 	    String iAction = intent.getAction();
 
 	    if ((iAction.equals(Intent.ACTION_SCREEN_ON))
 		    || (iAction.equals(Intent.ACTION_SCREEN_OFF)))
 		handleScreenAction(iAction);
 	    else if (iAction.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
 		handleWifiState(intent);
 	    else if (iAction
 		    .equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION))
 		handleSupplicantIntent(intent);
 	    else if (iAction.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
 		handleWifiResults();
 	    else if (iAction
 		    .equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION))
 		handleNetworkAction(intent);
 
 	}
 
     };
 
     void checkLock(WifiManager.WifiLock lock) {
 	if (!prefschanged) {
 	    // Yeah, first run. Ok, if LOCKPREF true, acquire lock.
 	    if (lockpref) {
 		lock.acquire();
 		haslock = true;
 		if (logging)
 		    wfLog(APP_NAME, "Acquiring Wifi Lock");
 	    }
 	} else {
 	    // ok, this is when prefs have changed, soo..
 	    prefschanged = false;
 	    if (lockpref && !haslock) {
 		// generate new lock
 		lock.acquire();
 		haslock = true;
 		if (logging)
 		    wfLog(APP_NAME, "Acquiring Wifi Lock");
 	    } else {
 		if (haslock && !lockpref) {
 		    lock.release();
 		    haslock = false;
 		    if (logging)
 			wfLog(APP_NAME, "Releasing Wifi Lock");
 		}
 	    }
 	}
     }
 
     void cleanup() {
 
 	if (!cleanup) {
 
 	    if (haslock && lock.isHeld())
 		lock.release();
 	    unregisterReceiver(receiver);
 	    hMain.removeMessages(MAIN);
 	    cleanupPosts();
 	    cleanup = true;
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
 
     boolean checkNetwork() {
 	boolean isup = false;
 	/*
 	 * Failover switch
 	 */
 	isup = hostup();
 	if (!isup) {
 	    switchHostMethod();
 	    isup = hostup();
 	    if (!isup)
 		switchHostMethod();
 	} else
 	    wifirepair = 0;
 
 	return isup;
     }
 
     void checkWifiState() {
 	if (!getIsWifiEnabled() && wifishouldbeon) {
 	    hMainWrapper(WIFI_ON);
 	}
     }
 
     boolean connectToAP(int AP, boolean disableOthers) {
 	if (logging)
 	    wfLog(APP_NAME, "Connecting to Network:" + AP);
 	tempLock(CONNECTWAIT);
 	return wm.enableNetwork(AP, disableOthers);
     }
 
     void deleteNotification(int id) {
 	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 	nm.cancel(id);
     }
 
     void doWidgetAction() {
 	if (getIsWifiEnabled()) {
 	    if (widgetpref) {
 		Toast.makeText(WifiFixerService.this, "Toggling Wifi",
 			Toast.LENGTH_LONG).show();
 		toggleWifi();
 	    } else {
 		Toast.makeText(WifiFixerService.this, "Reassociating",
 			Toast.LENGTH_LONG).show();
 		wifirepair = 0;
 		wifiRepair();
 	    }
 	} else
 	    Toast.makeText(WifiFixerService.this, "Wifi Is Disabled",
 		    Toast.LENGTH_LONG).show();
     }
 
     void fixWifi() {
 	if (getIsWifiEnabled(true)) {
 	    if (getSupplicantState() == SupplicantState.ASSOCIATED
 		    || getSupplicantState() == SupplicantState.COMPLETED) {
 		if (!checkNetwork()) {
 		    wifiRepair();
 		}
 	    } else {
 		pendingscan = true;
 		tempLock(CONNECTWAIT);
 	    }
 
 	}
 
     }
 
     boolean getHttpHeaders() throws IOException, URISyntaxException {
 
 	// Turns out the old way was better
 	// I just wasn't doing it right.
 
 	boolean isup = false;
 	int status = HTTP_NULL;
 
 	/*
 	 * Reusing our Httpclient, only initializing first time
 	 */
 
 	if (httpclient == null) {
 	    httpclient = new DefaultHttpClient();
 	    headURI = new URI(H_TARGET);
 	    head = new HttpHead(headURI);
 	    httpparams = new BasicHttpParams();
 	    HttpConnectionParams.setConnectionTimeout(httpparams, HTTPREACH);
 	    HttpConnectionParams.setSoTimeout(httpparams, HTTPREACH);
 	    HttpConnectionParams.setLinger(httpparams, 1);
 	    HttpConnectionParams.setStaleCheckingEnabled(httpparams, false);
 	    httpclient.setParams(httpparams);
 	}
 	/*
 	 * The next two lines actually perform the connection since it's the
 	 * same, can re-use.
 	 */
 	response = httpclient.execute(head);
 	status = response.getStatusLine().getStatusCode();
 	if (status != HTTP_NULL)
 	    isup = true;
 	if (logging) {
 	    wfLog(APP_NAME, "HTTP STATUS:" + status);
 	}
 
 	return isup;
     }
 
     boolean getIsOnWifi() {
 	boolean wifi = false;
 	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	NetworkInfo ni = cm.getActiveNetworkInfo();
 	// Null check, this can be null, so NPE
 	if (ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI)
 	    wifi = true;
 	return wifi;
     }
 
     boolean getIsWifiEnabled() {
 	boolean enabled = false;
 
 	if (wm.isWifiEnabled()) {
 	    enabled = true;
 	} else {
 	   /*
 	    * it's false
 	    */
 	}
 
 	return enabled;
     }
     
     boolean getIsWifiEnabled(boolean log) {
 	boolean enabled = false;
 
 	if (wm.isWifiEnabled()) {
 	    if (logging)
 		wfLog(APP_NAME, "Wifi is Enabled");
 	    enabled = true;
 	} else {
 	    if (logging)
 		wfLog(APP_NAME, "Wifi not Enabled");
 	}
 
 	return enabled;
     }
 
     int getNetworkID() {
 	myWifi = wm.getConnectionInfo();
 	int id = myWifi.getNetworkId();
 	if (!(id == HTTP_NULL)) {
 	    lastnid = id;
 	    lastssid = myWifi.getSSID();
 	}
 	return id;
     }
 
     void getPackageInfo() {
 	PackageManager pm = getPackageManager();
 	try {
 	    // ---get the package info---
 	    PackageInfo pi = pm.getPackageInfo("org.wahtod.wififixer", 0);
 	    // ---display the versioncode--
 	    version = pi.versionCode;
 	} catch (NameNotFoundException e) {
 	    /*
 	     * If own package isn't found, something is horribly wrong.
 	     */
 	}
     }
 
     SupplicantState getSupplicantState() {
 	myWifi = wm.getConnectionInfo();
 	return myWifi.getSupplicantState();
     }
 
     ArrayList<String> getWifiConfigurations() {
 
 	List<WifiConfiguration> conflist = wm.getConfiguredNetworks();
 	WifiConfiguration wfCon;
 	ArrayList<String> myList = new ArrayList<String>();
 	for (int i = 0; i < conflist.size(); i++) {
 	    wfCon = conflist.get(i);
 	    myList.add(i, wfCon.SSID);
 	}
 	if (logging)
 	    wfLog(APP_NAME, "Configured Networks:" + myList.toString());
 	return myList;
     }
 
     WifiManager getWifiManager() {
 	return (WifiManager) getSystemService(Context.WIFI_SERVICE);
     }
 
     void handleAuth(Intent intent) {
 	if (intent.getStringExtra(AUTHEXTRA).contains(AUTHSTRING)) {
 	    if (logging)
 		wfLog(APP_NAME, "Yep, we're authed!");
 	    // Ok, do the auth
 	    settings = PreferenceManager.getDefaultSharedPreferences(this);
 	    boolean ISAUTHED = settings.getBoolean("ISAUTHED", false);
 	    if (!ISAUTHED) {
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putBoolean("ISAUTHED", true);
 		editor.commit();
 		showNotification("Thank you for your donation.", "Authorized",
 			ISAUTHED);
 	    }
 
 	}
     }
 
     private void handleNetworkAction(Intent intent) {
 	/*
 	 * This action means network connectivty has changed but, we only want
 	 * to run this code for wifi
 	 */
 	if (!getIsWifiEnabled() || !getIsOnWifi())
 	    return;
 
 	icmpCache();
     }
 
     void handleScreenAction(String iAction) {
 
 	if (screenpref)
 	    return;
 
 	if (iAction.equals(Intent.ACTION_SCREEN_OFF)) {
 	    screenisoff = true;
 	    if (logging) {
 		wfLog(APP_NAME, "SCREEN_OFF handler");
 		wfLog(LogService.SCREEN_OFF, null);
 	    }
 	} else {
 	    if (logging) {
 		wfLog(APP_NAME, "SCREEN_ON handler");
 		wfLog(LogService.SCREEN_ON, null);
 	    }
 	    screenisoff = false;
 	}
 
     }
 
     void handleStart(Intent intent) {
 
 	// Handle NPE
 
 	try {
 	    if (intent.hasExtra(FIXWIFI)) {
 		if (intent.getBooleanExtra(FIXWIFI, false)) {
 		    doWidgetAction();
 		}
 		if (logging)
 		    wfLog(APP_NAME, "Called by Widget");
 	    } else {
 
 		String iAction = intent.getAction();
 		// Looking for auth intent
 		if (iAction.contains(AUTH)) {
 		    handleAuth(intent);
 		    return;
 		} else {
 		    loadPrefs();
 		    prefschanged = true;
 		    if (logging)
 			wfLog(APP_NAME, "Normal Startup or reload");
 		}
 	    }
 	} catch (NullPointerException e) {
 	    if (logging)
 		wfLog(APP_NAME, "Tickled");
 	}
 
     }
 
     private void handleSupplicantIntent(Intent intent) {
 	/*
 	 * New setting disabling supplicant fixes
 	 */
 	if (supfix)
 	    return;
 	// supplicant fixes
 
 	String sState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)
 		.toString();
 
 	handleSupplicantState(sState);
 
     }
 
     void handleSupplicantState(String sState) {
 
 	/*
 	 * Dispatches appropriate supplicant fix
 	 */
 
	if (!getIsWifiEnabled())
 	    return;
 	else if (sState == SCANNING) {
 	    pendingscan = true;
 
 	} else if (sState == DISCONNECTED) {
 	    pendingscan = true;
 	    startScan();
 	    notifyWrap(sState);
 	} else if (sState == INACTIVE) {
 	    supplicantFix(true);
 	    notifyWrap(sState);
 	}
 
 	if (logging && !screenisoff)
 	    logSupplicant(sState);
     }
 
     private void handleWifiResults() {
 	hMainWrapper(TEMPLOCK_OFF);
 	if (!getIsWifiEnabled())
 	    return;
 
 	if (!pendingscan) {
 	    if (logging)
 		wfLog(APP_NAME, "No Pending Scan.");
 	    return;
 	}
 
 	if (!pendingreconnect) {
 
 	    pendingscan = false;
 	    hMainWrapper(REPAIR);
 	    if (logging)
 		wfLog(APP_NAME, "Scan Results Acquired:Running Repair_Handler");
 	} else {
 	    pendingscan = false;
 	    hMainWrapper(RECONNECT);
 	    if (logging)
 		wfLog(APP_NAME,
 			"Scan Results Acquired:Running Reconnect_Handler");
 	}
 
     }
 
     void handleWifiState(Intent intent) {
 	// What kind of state change is it?
 	int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
 		WifiManager.WIFI_STATE_UNKNOWN);
 	switch (state) {
 	case WifiManager.WIFI_STATE_ENABLED:
 	    if (logging)
 		wfLog(APP_NAME, "WIFI_STATE_ENABLED");
 	    hMainWrapper(TEMPLOCK_OFF, LOCKWAIT);
 	    wifishouldbeon = false;
 	    break;
 	case WifiManager.WIFI_STATE_ENABLING:
 	    if (logging)
 		wfLog(APP_NAME, "WIFI_STATE_ENABLING");
 	    break;
 	case WifiManager.WIFI_STATE_DISABLED:
 	    if (logging)
 		wfLog(APP_NAME, "WIFI_STATE_DISABLED");
 	    hMainWrapper(TEMPLOCK_ON);
 	    break;
 	case WifiManager.WIFI_STATE_DISABLING:
 	    if (logging)
 		wfLog(APP_NAME, "WIFI_STATE_DISABLING");
 	    break;
 	case WifiManager.WIFI_STATE_UNKNOWN:
 	    if (logging)
 		wfLog(APP_NAME, "WIFI_STATE_UNKNOWN");
 	    break;
 	}
     }
 
     // Why do we do this? Because race can occur
     // in the queue.
     void hMainWrapper(int hmain) {
 	hMain.removeMessages(hmain);
 	if (hMainCheck(hmain))
 	    hMain.sendEmptyMessage(hmain);
     }
 
     // whee overloading methods, <3 java
     void hMainWrapper(int hmain, long delay) {
 	hMain.removeMessages(hmain);
 	if (hMainCheck(hmain))
 	    hMain.sendEmptyMessageDelayed(hmain, delay);
     }
 
     boolean hMainCheck(int hmain) {
 	if (templock) {
 	    // prevent running posts during lock
 	    if (hmain == RECONNECT || hmain == REPAIR || hmain == WIFITASK)
 		return false;
 	}
 	return true;
     }
 
     boolean httpHostup() {
 	boolean isUp = false;
 	/*
 	 * getHttpHeaders() does all the heavy lifting
 	 */
 	try {
 	    isUp = getHttpHeaders();
 	} catch (IOException e) {
 	    if (logging)
 		wfLog(APP_NAME, "HTTP I/O Exception");
 	} catch (URISyntaxException e) {
 	    if (logging)
 		wfLog(APP_NAME, "URL Syntax Exception");
 	}
 	if (logging)
 	    wfLog(APP_NAME, "HTTP Method");
 	return isUp;
     }
 
     boolean hostup() {
 
 	if (httppref)
 	    return httpHostup();
 	else
 	    return icmpHostup();
 
     }
 
     boolean icmpHostup() {
 	boolean isUp = false;
 	/*
 	 * If IP hasn't been cached yet cache it
 	 */
 	if (cachedIP == null)
 	    icmpCache();
 
 	try {
 	    if (InetAddress.getByName(cachedIP).isReachable(REACHABLE)) {
 		isUp = true;
 	    }
 	} catch (UnknownHostException e) {
 	    if (logging)
 		wfLog(APP_NAME, "UnknownHostException");
 	} catch (IOException e) {
 	    if (logging)
 		wfLog(APP_NAME, "IOException");
 	}
 	if (logging)
 	    wfLog(APP_NAME, "ICMP Method:" + cachedIP);
 	return isUp;
     }
 
     private void icmpCache() {
 	/*
 	 * Caches DHCP gateway IP for ICMP check
 	 */
 	DhcpInfo info = wm.getDhcpInfo();
 	cachedIP = intToIp(info.gateway);
 	if (logging)
 	    wfLog(APP_NAME, "Cached IP:" + cachedIP);
     }
 
     String intToIp(int i) {
 	return Formatter.formatIpAddress(i);
     }
 
     boolean isKnownAPinRange() {
 	boolean state = false;
 	wifiList = wm.getScanResults();
 	/*
 	 * Catch null if scan results fires after wifi disabled or while wifi is
 	 * in intermediate state
 	 */
 	if (wifiList == null) {
 	    if (logging)
 		wfLog(APP_NAME, "Null Scan Results");
 	    return false;
 	}
 	// OK, this is butt ugly, but it works.
 	ArrayList<String> wifiConfigs = getWifiConfigurations();
 	for (int i = 0; i < (wifiList.size()); i++) {
 	    sResult = wifiList.get(i);
 	    for (int i2 = 0; i2 < wifiConfigs.size(); i2++) {
 		if (wifiConfigs.get(i2).toString().contains(sResult.SSID)) {
 		    if (logging) {
 			wfLog(APP_NAME, "Found SSID:" + sResult.SSID);
 			wfLog(APP_NAME, "Capabilities:" + sResult.capabilities);
 		    }
 		    lastnid = (i2);
 		    lastssid = sResult.SSID;
 		    state = true;
 		    return state;
 		}
 	    }
 	}
 	return state;
     }
 
     void loadPrefs() {
 	settings = PreferenceManager.getDefaultSharedPreferences(this);
 	lockpref = settings.getBoolean(WIFILOCK_KEY, false);
 	notifpref = settings.getBoolean(NOTIF_KEY, false);
 	runpref = settings.getBoolean(DISABLE_KEY, false);
 	screenpref = settings.getBoolean(SCREEN_KEY, false);
 	widgetpref = settings.getBoolean(WIDGET_KEY, false);
 	supfix = settings.getBoolean(SUPFIX_KEY, false);
 	String PERFORMANCE = settings.getString(PERFORMANCE_KEY, "0");
 	// Kill the Log Service if it's up
 	if (logging && !settings.getBoolean(LOG_KEY, false))
 	    wfLog(LogService.DIE, null);
 	logging = settings.getBoolean(LOG_KEY, false);
 	// Check RUNPREF and set SHOULDRUN
 	// Make sure Main loop restarts if this is a change
 	if (runpref) {
 	    ServiceAlarm.unsetAlarm(this);
 	    shouldrun = false;
 	} else {
 	    if (!shouldrun) {
 		shouldrun = true;
 	    }
 	    ServiceAlarm.setAlarm(this, true);
 	}
 	// Setting defaults if performance not set
 	if (PERFORMANCE == "0" && !lockpref) {
 	    SharedPreferences.Editor edit = settings.edit();
 	    edit.putString(PERFORMANCE_KEY, "2");
 	    edit.putBoolean(WIFILOCK_KEY, true);
 	    edit.commit();
 	    lockpref = true;
 	}
 
 	/*
 	 * Sets default for Supplicant Fix pref on < 2.0 to true
 	 */
 
 	if (!settings.getBoolean(SUPFIX_DEFAULT, false)) {
 	    SharedPreferences.Editor edit = settings.edit();
 	    edit.putBoolean(SUPFIX_DEFAULT, true);
 	    int ver;
 	    try {
 		ver = Integer.valueOf(Build.VERSION.RELEASE.substring(0, 1));
 	    } catch (NumberFormatException e) {
 		ver = 0;
 	    }
 	    if (logging)
 		wfLog(APP_NAME, "Version:" + ver);
 	    if (ver < 2) {
 		edit.putBoolean(SUPFIX_KEY, true);
 	    }
 
 	    edit.commit();
 
 	}
 
 	if (logging) {
 	    wfLog(APP_NAME, "Loading Settings");
 	    if (lockpref)
 		wfLog(APP_NAME, "LOCKPREF");
 
 	    if (notifpref)
 		wfLog(APP_NAME, "NOTIFPREF");
 
 	    if (runpref)
 		wfLog(APP_NAME, "RUNPREF");
 
 	    if (screenpref)
 		wfLog(APP_NAME, "SCREENPREF");
 
 	    if (supfix)
 		wfLog(APP_NAME, "SUPPREF");
 
 	}
 
     }
 
     void logSupplicant(String state) {
 
 	wfLog(APP_NAME, "Supplicant State:" + state);
 	if (wm.pingSupplicant()) {
 	    wfLog(APP_NAME, "Supplicant Responded");
 	} else {
 	    wfLog(APP_NAME, "Supplicant Nonresponsive");
 
 	}
 	wfLog(APP_NAME, "SSID:" + lastssid);
 
     }
 
     void notifyWrap(String message) {
 	if (notifpref) {
 	    showNotification("Wifi Connection Problem:" + message, message,
 		    false);
 	}
 
     }
 
     @Override
     public IBinder onBind(Intent intent) {
 	if (logging)
 	    wfLog(APP_NAME, "OnBind:Intent:" + intent.toString());
 	return null;
     }
 
     @Override
     public void onCreate() {
 
 	wm = getWifiManager();
 	getPackageInfo();
 
 	if (logging) {
 	    wfLog(APP_NAME, "WifiFixerService Build:" + version);
 	}
 	loadPrefs();
 
 	// Setup, formerly in Run thread
 	setup();
 	hMain.sendEmptyMessage(MAIN);
 
 	if (logging)
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
 	// WIFI_MODE_FULL should p. much always be used
 	lock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "WFLock");
 	checkLock(lock);
 	IntentFilter myFilter = new IntentFilter();
 
 	// Wifi State filter
 	myFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
 
 	// Catch power events for battery savings
 	myFilter.addAction(Intent.ACTION_SCREEN_OFF);
 	myFilter.addAction(Intent.ACTION_SCREEN_ON);
 
 	// Supplicant State filter
 
 	myFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
 
 	// Network State filter
 	myFilter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
 
 	// wifi scan results available callback
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
 	// unique ID
 	nm.notify(4144, notif);
 
     }
 
     void showNotification(String message, String tickerText, int id) {
 	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 	CharSequence from = "Wifi Fixer";
 	PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 		new Intent(), 0);
 
 	Notification notif = new Notification(R.drawable.icon, tickerText,
 		System.currentTimeMillis());
 
 	notif.setLatestEventInfo(this, from, message, contentIntent);
 	notif.flags = Notification.FLAG_AUTO_CANCEL;
 	// unique ID
 	nm.notify(id, notif);
     }
 
     void startScan() {
 	// We want a lock after a scan
 	wm.startScan();
 	tempLock(LOCKWAIT);
     }
 
     void switchHostMethod() {
 	if (httppref)
 	    httppref = false;
 	else
 	    httppref = true;
     }
 
     void supplicantFix(boolean wftoggle) {
 	// Toggling wifi fixes the supplicant
 	pendingscan = true;
 	if (wftoggle)
 	    toggleWifi();
 	startScan();
 	if (logging)
 	    wfLog(APP_NAME, "Running Supplicant Fix");
     }
 
     void tempLock(int time) {
 
 	hMainWrapper(TEMPLOCK_ON);
 	// Queue for later
 	hMainWrapper(TEMPLOCK_OFF, time);
     }
 
     void toggleWifi() {
 	if (pendingwifitoggle)
 	    return;
 
 	pendingwifitoggle = true;
 	cleanupPosts();
 	tempLock(CONNECTWAIT);
 	// Wake lock
 	wakeLock(true);
 	showNotification("Toggling Wifi", "Toggling Wifi", NOTIFID);
 	hMainWrapper(WIFI_OFF);
 	hMainWrapper(WIFI_ON, LOCKWAIT);
     }
 
     void wakeLock(boolean state) {
 	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 	if (state) {
 	    wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
 		    "WFWakeLock");
 	    wakelock.acquire();
 	    haswakelock = true;
 	} else {
 	    wakelock.release();
 	    haswakelock = false;
 	}
 
     }
 
     void wifiRepair() {
 	hMainWrapper(WIFITASK);
 	if (logging)
 	    wfLog(APP_NAME, "Running Wifi Repair");
     }
 
     void wfLog(String APP_NAME, String Message) {
 	Intent sendIntent = new Intent(LOGINTENT);
 	sendIntent.putExtra(LogService.APPNAME, APP_NAME);
 	sendIntent.putExtra(LogService.Message, Message);
 	startService(sendIntent);
     }
 
 }
