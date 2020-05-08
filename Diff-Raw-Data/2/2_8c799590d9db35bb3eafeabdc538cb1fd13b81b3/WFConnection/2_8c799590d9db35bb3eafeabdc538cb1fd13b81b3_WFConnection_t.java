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
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpHead;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.wahtod.wififixer.R;
 import org.wahtod.wififixer.PrefConstants.Pref;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.DhcpInfo;
 import android.net.wifi.ScanResult;
 import android.net.wifi.SupplicantState;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 import android.os.Handler;
 import android.os.Message;
 import android.text.format.Formatter;
 
 /*
  * Handles all interaction 
  * with WifiManager
  */
 public class WFConnection extends Object {
     private static WifiManager wm;
     private static String cachedIP;
     private static String appname;
     private static PrefUtil prefs;
     private static Context ctxt;
     private WakeLock wakelock;
     static boolean screenstate;
 
     // flags
     private static boolean pendingwifitoggle = false;
     private static boolean shouldrepair = false;
     private static boolean pendingscan = false;
     private static boolean pendingreconnect = false;
 
     // IDs For notifications
     private static final int NETNOTIFID = 8236;
     private static final int ERR_NOTIF = 7972;
 
     // Supplicant Constants
     private static final String DISCONNECTED = "DISCONNECTED";
     private static final String INACTIVE = "INACTIVE";
     private static final String COMPLETED = "COMPLETED";
     private static final String CONNECTED = "CONNECTED";
 
     // For blank SSIDs
     private static final String NULL_SSID = "None";
 
     // Wifi Lock tag
     private static final String WFLOCK_TAG = "WFLock";
 
     // Wifi Connect Intent
     public static final String CONNECTINTENT = "org.wahtod.wififixer.CONNECT";
     public static final String NETWORKNUMBER = "net#";
 
     // User Event Intent
     public static final String USEREVENT = "org.wahtod.wififixer.USEREVENT";
 
     // Empty string
     private static final String EMPTYSTRING = "";
     private static final String NEWLINE = "\n";
 
     /*
      * Status Notification Strings
      */
     private static String notifSSID = EMPTYSTRING;
     private static String notifStatus = EMPTYSTRING;
     /*
      * Int for status notification signal
      */
     private static int notifSignal = R.drawable.signal0;
 
     // Target for header check
     private static final String H_TARGET = "http://www.google.com";
     private static URI headURI;
 
     // ms for IsReachable
     private final static int REACHABLE = 4000;
     private final static int HTTPREACH = 8000;
     // ms for main loop sleep
     private final static int LOOPWAIT = 10000;
     // ms for sleep loop check
     private final static long SLEEPWAIT = 60000;
     // ms for lock delays
     private final static int LOCKWAIT = 5000;
     // ms to wait after trying to connect
     private static final int CONNECTWAIT = 8000;
     private static final int SHORTWAIT = 1500;
     private static final int REALLYSHORTWAIT = 200;
 
     // for Dbm
     private static final int DBM_FLOOR = -90;
 
     // For priority
     /*
      * WHAT DOES THE SCOUT SAY?
      */
     private static final int OVER9000 = 100001;
 
     // various
     private static final int NULLVAL = -1;
     private static int lastAP = NULLVAL;
 
     private static WifiConfiguration connectee;
     private static WifiManager.WifiLock lock;
     private static DefaultHttpClient httpclient;
     private static HttpParams httpparams;
     private static HttpHead head;
     private static HttpResponse response;
     private static List<WFConfig> knownbysignal = new ArrayList<WFConfig>();
 
     // deprecated
     static boolean templock = false;
     static boolean logging = false;
 
     /*
      * Constants for wifirepair values
      */
     private static final int W_REASSOCIATE = 0;
     private static final int W_RECONNECT = 1;
     private static final int W_REPAIR = 2;
 
     private static int wifirepair = W_REASSOCIATE;
 
     // Runnable Constants for handler
     private static final int MAIN = 0;
     private static final int REPAIR = 1;
     private static final int RECONNECT = 2;
     private static final int WIFITASK = 3;
     private static final int TEMPLOCK_ON = 4;
     private static final int TEMPLOCK_OFF = 5;
     private static final int SLEEPCHECK = 8;
     private static final int SCAN = 9;
     private static final int N1CHECK = 10;
     private static final int SIGNALHOP = 12;
 
     private Handler handler = new Handler() {
 	@Override
 	public void handleMessage(Message message) {
 	    switch (message.what) {
 
 	    case MAIN:
 		handler.post(rMain);
 		break;
 
 	    case REPAIR:
 		handler.post(rRepair);
 		break;
 
 	    case RECONNECT:
 		handler.post(rReconnect);
 		break;
 
 	    case WIFITASK:
 		handler.post(rWifiTask);
 		break;
 
 	    case TEMPLOCK_ON:
 		templock = true;
 		if (logging)
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.setting_temp_lock));
 		break;
 
 	    case TEMPLOCK_OFF:
 		templock = false;
 		if (logging)
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.removing_temp_lock));
 		break;
 
 	    case SLEEPCHECK:
 		handler.post(rSleepcheck);
 		break;
 
 	    case SCAN:
 		handler.post(rScan);
 		break;
 
 	    case N1CHECK:
 		n1Fix();
 		break;
 
 	    case SIGNALHOP:
 		handler.post(rSignalhop);
 		break;
 
 	    }
 	}
     };
 
     /*
      * Runs second time supplicant nonresponsive
      */
     private Runnable rRepair = new Runnable() {
 	public void run() {
 	    if (!wm.isWifiEnabled()) {
 		handlerWrapper(TEMPLOCK_OFF);
 		if (logging)
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.wifi_off_aborting_repair));
 		return;
 	    }
 
 	    if (getKnownAPsBySignal(ctxt) > 0 && connectToBest(ctxt) != NULLVAL) {
 		pendingreconnect = false;
 	    } else {
 		pendingreconnect = true;
 		toggleWifi();
 		if (logging)
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.toggling_wifi));
 
 	    }
 	}
 
     };
 
     /*
      * Runs first time supplicant nonresponsive
      */
     private Runnable rReconnect = new Runnable() {
 	public void run() {
 	    if (!wm.isWifiEnabled()) {
 		handlerWrapper(TEMPLOCK_OFF);
 		if (logging)
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.wifi_off_aborting_reconnect));
 		return;
 	    }
 	    if (getKnownAPsBySignal(ctxt) > 0 && connectToBest(ctxt) != NULLVAL) {
 		pendingreconnect = false;
 	    } else {
 		wifirepair = W_REASSOCIATE;
 		startScan(true);
 		if (logging)
 		    LogService
 			    .log(
 				    ctxt,
 				    appname,
 				    ctxt
 					    .getString(R.string.exiting_supplicant_fix_thread_starting_scan));
 	    }
 
 	}
 
     };
 
     /*
      * Main tick
      */
     private Runnable rMain = new Runnable() {
 	public void run() {
 
 	    // Check Supplicant
 	    if (wm.isWifiEnabled() && !wm.pingSupplicant()) {
 		if (logging)
 		    LogService
 			    .log(
 				    ctxt,
 				    appname,
 				    ctxt
 					    .getString(R.string.supplicant_nonresponsive_toggling_wifi));
 		toggleWifi();
 	    } else if (!templock && screenstate)
 		checkWifi();
 
 	    if (prefs.getFlag(Pref.DISABLE_KEY)) {
 		if (logging) {
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.shouldrun_false_dying));
 		    // stopSelf();
 		}
 	    } else
 		// Queue next run of main runnable
 		handlerWrapper(MAIN, LOOPWAIT);
 
 	}
     };
 
     /*
      * Handles non-supplicant wifi fixes.
      */
     private Runnable rWifiTask = new Runnable() {
 	public void run() {
 
 	    switch (wifirepair) {
 
 	    case W_REASSOCIATE:
 		// Let's try to reassociate first..
 		clearHandler();
 		wm.reassociate();
 		if (logging)
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.reassociating));
 		handlerWrapper(MAIN, REACHABLE);
 		wifirepair++;
 		notifyWrap(ctxt, ctxt.getString(R.string.reassociating));
 		break;
 
 	    case W_RECONNECT:
 		// Ok, now force reconnect..
 		clearHandler();
 		wm.reconnect();
 		if (logging)
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.reconnecting));
 		handlerWrapper(MAIN, REACHABLE);
 		wifirepair++;
 		notifyWrap(ctxt, ctxt.getString(R.string.reconnecting));
 		break;
 
 	    case W_REPAIR:
 		// Start Scan
 		startScan(true);
 		wifirepair = W_REASSOCIATE;
 		if (logging)
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.repairing));
 		notifyWrap(ctxt, ctxt.getString(R.string.repairing));
 		break;
 	    }
 	    /*
 	     * Remove wake lock if there is one
 	     */
 	    wakelock.lock(false);
 
 	    if (logging) {
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.fix_algorithm)
 			+ Integer.toString(wifirepair));
 	    }
 	}
     };
 
     /*
      * Sleep tick if wifi is enabled and screenpref
      */
     private Runnable rSleepcheck = new Runnable() {
 	public void run() {
 	    /*
 	     * This is all we want to do.
 	     */
 	    wakelock.lock(true);
 	    checkWifi();
 	    /*
 	     * Post next run
 	     */
 	    handlerWrapper(SLEEPCHECK, SLEEPWAIT);
 	    wakelock.lock(false);
 	}
 
     };
 
     /*
      * Scanner runnable
      */
     private Runnable rScan = new Runnable() {
 	public void run() {
 	    /*
 	     * Start scan if nothing is holding a temp lock
 	     */
 	    if (!templock) {
 		startScan(false);
 	    } else
 		handlerWrapper(SCAN, CONNECTWAIT);
 
 	}
 
     };
 
     /*
      * SignalHop runnable
      */
     private Runnable rSignalhop = new Runnable() {
 	public void run() {
 	    /*
 	     * Remove all posts first
 	     */
 	    wakelock.lock(true);
 	    clearQueue();
 	    handler.removeMessages(TEMPLOCK_OFF);
 	    /*
 	     * Set Lock
 	     */
 	    handlerWrapper(TEMPLOCK_ON, SHORTWAIT);
 	    /*
 	     * run the signal hop check
 	     */
 	    signalHop();
 	    /*
 	     * Then restore main tick
 	     */
 	    handler.sendEmptyMessageDelayed(TEMPLOCK_OFF, SHORTWAIT);
 	    wakelock.lock(false);
 	}
 
     };
 
     private BroadcastReceiver receiver = new BroadcastReceiver() {
 	public void onReceive(final Context context, final Intent intent) {
 
 	    /*
 	     * Dispatches the broadcast intent to the appropriate handler method
 	     */
 
 	    String iAction = intent.getAction();
 
 	    if (iAction.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
 		handleWifiState(intent);
 	    else if (iAction
 		    .equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION))
 		handleSupplicantIntent(intent);
 	    else if (iAction.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
 		handleScanResults();
 	    else if (iAction
 		    .equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION))
 		handleNetworkAction(context);
 	    else if (iAction
 		    .equals(android.net.ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED))
 		checkBackgroundDataSetting(context);
 	    else if (iAction.equals(CONNECTINTENT))
 		handleConnectIntent(context, intent);
 	    else if (iAction.equals(USEREVENT))
 		handleUserEvent();
 	}
 
     };
 
     public WFConnection(final Context context, PrefUtil p) {
 	wm = getWifiManager(context);
 	prefs = p;
 	appname = LogService.getLogTag(context);
 	screenstate = ScreenStateHandler.getScreenState(context);
 	logging = prefs.getFlag(Pref.LOG_KEY);
 	/*
 	 * Cache Context from consumer
 	 */
 	ctxt = context;
 
 	/*
 	 * Set current AP int
 	 */
 	lastAP = getNetworkID();
 
 	/*
 	 * Set up Intent filters
 	 */
 	IntentFilter filter = new IntentFilter(
 		WifiManager.WIFI_STATE_CHANGED_ACTION);
 	// Supplicant State filter
 	filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
 
 	// Network State filter
 	filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
 
 	// wifi scan results available callback
 	filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
 
 	// Background Data enable/disable
 	filter
 		.addAction(ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED);
 
 	// Connect intent
 	filter.addAction(CONNECTINTENT);
 
 	// User Event
 	filter.addAction(USEREVENT);
 
 	context.registerReceiver(receiver, filter);
 	/*
 	 * Acquire wifi lock WIFI_MODE_FULL should p. much always be used
 	 * acquire lock if pref says we should
 	 */
 	lock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, WFLOCK_TAG);
 	if (prefs.getFlag(Pref.WIFILOCK_KEY)) {
 	    lock.acquire();
 	    if (logging)
 		LogService.log(context, appname, context
 			.getString(R.string.acquiring_wifi_lock));
 	}
 
 	// Initialize WakeLock
 	wakelock = new WakeLock(context) {
 
 	    @Override
 	    public void onAcquire() {
 		if (logging)
 		    LogService.log(context, appname, context
 			    .getString(R.string.acquiring_wake_lock));
 		super.onAcquire();
 	    }
 
 	    @Override
 	    public void onRelease() {
 		if (logging)
 		    LogService.log(context, appname, context
 			    .getString(R.string.releasing_wake_lock));
 		super.onRelease();
 	    }
 
 	};
 
 	if (prefs.getFlag(Pref.STATENOT_KEY))
 	    setStatNotif(true);
 
 	/*
 	 * Start Main tick
 	 */
 	handler.sendEmptyMessage(MAIN);
     }
 
     public static void checkBackgroundDataSetting(final Context context) {
 	ConnectivityManager cm = (ConnectivityManager) context
 		.getSystemService(Context.CONNECTIVITY_SERVICE);
 	if (cm.getBackgroundDataSetting() == false) {
 	    /*
 	     * Background data has been disabled. Notify the user and disable
 	     * service
 	     */
 	    NotifUtil.show(context, context.getString(R.string.bdata_nag),
 		    context.getString(R.string.bdata_ticker), ERR_NOTIF,
 		    PendingIntent.getActivity(context, 0, new Intent(), 0));
 	    PrefUtil.writeBoolean(context, Pref.DISABLE_KEY, true);
 
 	    context.sendBroadcast(new Intent(
 		    IntentConstants.ACTION_WIFI_SERVICE_DISABLE));
 	    PrefUtil.notifyPrefChange(context, Pref.DISABLE_KEY);
 	}
     }
 
     private static boolean checkNetwork(final Context context) {
 	boolean isup = false;
 
 	/*
 	 * First check if wifi is current network
 	 */
 
 	if (!getIsOnWifi(context)) {
 	    if (logging)
 		LogService.log(context, appname, context
 			.getString(R.string.wifi_not_current_network));
 	    return false;
 	}
 
 	/*
 	 * Failover switch
 	 */
 	isup = icmpHostup(context);
 	if (!isup) {
 	    isup = httpHostup(context);
 	    if (isup)
 		wifirepair = W_REASSOCIATE;
 	} else
 	    wifirepair = W_REASSOCIATE;
 
 	/*
 	 * Signal check
 	 */
 
 	checkSignal(context);
 
 	return isup;
     }
 
     private void clearHandler() {
 	if (handler.hasMessages(MAIN))
 	    handler.removeMessages(MAIN);
 	else if (handler.hasMessages(REPAIR))
 	    handler.removeMessages(REPAIR);
 	else if (handler.hasMessages(RECONNECT))
 	    handler.removeMessages(RECONNECT);
 	else if (handler.hasMessages(WIFITASK))
 	    handler.removeMessages(WIFITASK);
 	else if (handler.hasMessages(SLEEPCHECK))
 	    handler.removeMessages(SLEEPCHECK);
 	else if (handler.hasMessages(SCAN))
 	    handler.removeMessages(SCAN);
 	else if (handler.hasMessages(N1CHECK))
 	    handler.removeMessages(N1CHECK);
 	else if (handler.hasMessages(SIGNALHOP))
 	    handler.removeMessages(SIGNALHOP);
 	/*
 	 * Also clear all relevant flags
 	 */
 	shouldrepair = false;
 	pendingreconnect = false;
     }
 
     private static void checkSignal(final Context context) {
 	int signal = wm.getConnectionInfo().getRssi();
 
 	if (prefs.getFlag(Pref.STATENOT_KEY) && screenstate) {
 	    int adjusted = WifiManager.calculateSignalLevel(signal, 5);
 	    switch (adjusted) {
 	    case 4:
 		notifSignal = R.drawable.signal4;
 		break;
 	    case 3:
 		notifSignal = R.drawable.signal3;
 		break;
 	    case 2:
 		notifSignal = R.drawable.signal2;
 		break;
 
 	    case 1:
 		notifSignal = R.drawable.signal1;
 		break;
 
 	    case 0:
 		notifSignal = R.drawable.signal0;
 		break;
 	    }
 	    NotifUtil.addStatNotif(ctxt, notifSSID, notifStatus, notifSignal);
 	}
 
 	if (signal < DBM_FLOOR) {
 	    notifyWrap(context, context.getString(R.string.signal_poor));
 	    wm.startScan();
 	}
 
 	if (logging)
 	    LogService.log(context, appname, context
 		    .getString(R.string.current_dbm)
 		    + signal);
     }
 
     private void connectToAP(final Context context, final int network) {
 
 	if (!wm.isWifiEnabled())
 	    return;
 	/*
 	 * New code. Using priority to specify connection AP.
 	 */
 	connectee = wm.getConfiguredNetworks().get(network);
 	int priority = connectee.priority;
 	connectee.priority = OVER9000;
 	wm.updateNetwork(connectee);
 	connectee.priority = priority;
 	/*
 	 * Remove all posts to handler
 	 */
 	clearHandler();
 	wm.disconnect();
 	wm.startScan();
 	if (logging)
 	    LogService.log(context, appname, context
 		    .getString(R.string.connecting_to_network)
 		    + connectee.SSID);
 
     }
 
     private int connectToBest(final Context context) {
 	/*
 	 * Make sure knownbysignal is populated first
 	 */
 	if (knownbysignal.size() == 0)
 	    return NULLVAL;
 
 	int bestnid = NULLVAL;
 	WFConfig best = knownbysignal.get(0);
 	bestnid = best.wificonfig.networkId;
 	wm.updateNetwork(WFConfig.sparseConfig(best.wificonfig));
 	connectToAP(context, best.wificonfig.networkId);
 	if (logging)
 	    LogService.log(context, appname, context
 		    .getString(R.string.best_signal_ssid)
 		    + best.wificonfig.SSID
 		    + context.getString(R.string.signal_level)
 		    + best.level
 		    + context.getString(R.string.nid) + bestnid);
 	return bestnid;
     }
 
     private static boolean containsBSSID(final String bssid,
 	    final List<WFConfig> results) {
 	for (WFConfig sResult : results) {
 	    if (sResult.wificonfig.BSSID.equals(bssid))
 		return true;
 	}
 	return false;
     }
 
     private static boolean getHttpHeaders(final Context context)
 	    throws IOException, URISyntaxException {
 
 	/*
 	 * Performs HTTP HEAD request and returns boolean success or failure
 	 */
 
 	boolean isup = false;
 	int status = NULLVAL;
 
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
 	    HttpConnectionParams.setLinger(httpparams, REPAIR);
 	    HttpConnectionParams.setStaleCheckingEnabled(httpparams, true);
 	    httpclient.setParams(httpparams);
 	    if (logging)
 		LogService.log(context, appname, context
 			.getString(R.string.instantiating_httpclient));
 	}
 	/*
 	 * The next two lines actually perform the connection since it's the
 	 * same, can re-use.
 	 */
 	response = httpclient.execute(head);
 	status = response.getStatusLine().getStatusCode();
 	if (status != NULLVAL)
 	    isup = true;
 	if (logging) {
 	    LogService.log(context, appname, context
 		    .getString(R.string.http_status)
 		    + status);
 	}
 
 	return isup;
     }
 
     private static boolean getIsOnWifi(final Context context) {
 	boolean wifi = false;
 	ConnectivityManager cm = (ConnectivityManager) context
 		.getSystemService(Context.CONNECTIVITY_SERVICE);
 	if (cm.getActiveNetworkInfo() != null
 		&& cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI)
 	    wifi = true;
 	return wifi;
     }
 
     private static boolean getIsSupplicantConnected(final Context context) {
 	SupplicantState sstate = getSupplicantState();
 	if (sstate == null)
 	    return false;
 	else if (sstate == SupplicantState.ASSOCIATED
 		|| sstate == SupplicantState.COMPLETED)
 	    return true;
 	else
 	    return false;
     }
 
     private static boolean getisWifiEnabled(final Context context) {
 	boolean enabled = false;
 
 	if (wm.isWifiEnabled()) {
 	    if (logging)
 		LogService.log(context, appname, context
 			.getString(R.string.wifi_is_enabled));
 	    enabled = true;
 	} else {
 	    if (logging)
 		LogService.log(context, appname, context
 			.getString(R.string.wifi_is_disabled));
 	}
 
 	return enabled;
     }
 
     private static int getKnownAPsBySignal(final Context context) {
 	List<ScanResult> scanResults = wm.getScanResults();
 	/*
 	 * Catch null if scan results fires after wifi disabled or while wifi is
 	 * in intermediate state
 	 */
 	if (scanResults == null) {
 	    if (logging)
 		LogService.log(context, appname, context
 			.getString(R.string.null_scan_results));
 	    return NULLVAL;
 	}
 
 	knownbysignal.clear();
 
 	class SortBySignal implements Comparator<WFConfig> {
 	    @Override
 	    public int compare(WFConfig o2, WFConfig o1) {
 		/*
 		 * Sort by signal
 		 */
 		return (o1.level < o2.level ? -1 : (o1.level == o2.level ? 0
 			: 1));
 	    }
 	}
 	/*
 	 * Known networks from supplicant.
 	 */
 	final List<WifiConfiguration> wifiConfigs = wm.getConfiguredNetworks();
 
 	/*
 	 * Iterate the known networks over the scan results, adding found known
 	 * networks.
 	 */
 
 	if (logging)
 	    LogService.log(context, appname, context
 		    .getString(R.string.parsing_scan_results));
 
 	for (ScanResult sResult : scanResults) {
 	    for (WifiConfiguration wfResult : wifiConfigs) {
 		/*
 		 * Using .contains to find sResult.SSID in doublequoted string
 		 * 
 		 * containsBSSID filters out duplicate MACs in broken scans
 		 * (yes, that happens)
 		 */
 		try {
 		    if (wfResult.SSID.contains(sResult.SSID)
 			    && !containsBSSID(sResult.BSSID, knownbysignal)
 			    && getNetworkState(context, wfResult.networkId)) {
 			if (logging) {
 			    LogService.log(context, appname, context
 				    .getString(R.string.found_ssid)
 				    + sResult.SSID);
 			    LogService.log(context, appname, context
 				    .getString(R.string.capabilities)
 				    + sResult.capabilities);
 			    LogService.log(context, appname, context
 				    .getString(R.string.signal_level)
 				    + sResult.level);
 			}
 			/*
 			 * Add result to knownbysignal
 			 */
 			knownbysignal.add(new WFConfig(sResult, wfResult));
 
 		    }
 		} catch (NullPointerException e) {
 		    if (logging) {
 			if (wfResult.SSID == null)
 			    LogService.log(context, appname, context
 				    .getString(R.string.wfresult_null));
 			else if (sResult.SSID == null)
 			    LogService.log(context, appname, context
 				    .getString(R.string.sresult_null));
 		    }
 		}
 	    }
 	}
 
 	if (logging)
 	    LogService.log(context, appname, context
 		    .getString(R.string.number_of_known)
 		    + knownbysignal.size());
 
 	/*
 	 * Sort by ScanResult.level which is signal
 	 */
 	Collections.sort(knownbysignal, new SortBySignal());
 
 	return knownbysignal.size();
     }
 
     private static int getNetworkID() {
 	return wm.getConnectionInfo().getNetworkId();
     }
 
     private static String getSSID() {
 	if (wm.getConnectionInfo().getSSID() != null)
 	    return wm.getConnectionInfo().getSSID();
 	else
 	    return NULL_SSID;
     }
 
     private static SupplicantState getSupplicantState() {
 	return wm.getConnectionInfo().getSupplicantState();
     }
 
     private static String getSupplicantStateString() {
 	SupplicantState sstate = wm.getConnectionInfo().getSupplicantState();
 	if (sstate == SupplicantState.COMPLETED)
	    return CONNECTED;
 	else
 	    return sstate.name();
     }
 
     private static WifiManager getWifiManager(final Context context) {
 	return (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
     }
 
     private void handleConnect() {
 	if (connectee.SSID.contains(getSSID())) {
 	    if (logging)
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.connected_to_network)
 			+ connectee.SSID);
 	} else {
 	    if (logging)
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.connect_failed));
 	    toggleWifi();
 	}
 	handlerWrapper(MAIN, LOOPWAIT);
 	wm.updateNetwork(connectee);
 	connectee = null;
     }
 
     private void handleConnectIntent(Context context, Intent intent) {
 
 	connectToAP(ctxt, intent.getIntExtra(NETWORKNUMBER, -1));
     }
 
     private static void handleNetworkAction(final Context context) {
 	/*
 	 * This action means network connectivty has changed but, we only want
 	 * to run this code for wifi
 	 */
 	if (!wm.isWifiEnabled() || !getIsOnWifi(context))
 	    return;
 
 	icmpCache(context);
     }
 
     private void handleUserEvent() {
 	connectee = wm.getConfiguredNetworks().get(lastAP);
 	clearHandler();
     }
 
     private static boolean handlerCheck(final int hmain) {
 	if (templock) {
 	    /*
 	     * Check if is appropriate post and if lock exists
 	     */
 	    if (hmain == RECONNECT || hmain == REPAIR || hmain == WIFITASK)
 		return false;
 	}
 	return true;
     }
 
     private static boolean httpHostup(final Context context) {
 	boolean isUp = false;
 	/*
 	 * getHttpHeaders() does all the heavy lifting
 	 */
 	if (logging)
 	    LogService.log(context, appname, context
 		    .getString(R.string.http_method));
 
 	try {
 	    isUp = getHttpHeaders(context);
 	} catch (IOException e) {
 	    try {
 		/*
 		 * Second try
 		 */
 		isUp = getHttpHeaders(context);
 	    } catch (IOException e1) {
 		if (logging)
 		    LogService.log(context, appname, context
 			    .getString(R.string.httpexception));
 	    } catch (URISyntaxException e1) {
 		if (logging)
 		    LogService.log(context, appname, context
 			    .getString(R.string.http_method));
 	    }
 
 	} catch (URISyntaxException e) {
 	    if (logging)
 		LogService.log(context, appname, context
 			.getString(R.string.urlexception));
 	}
 
 	return isUp;
     }
 
     private static void icmpCache(final Context context) {
 	/*
 	 * Caches DHCP gateway IP for ICMP check
 	 */
 	DhcpInfo info = wm.getDhcpInfo();
 	cachedIP = Formatter.formatIpAddress(info.gateway);
 	if (prefs.getFlag(Pref.LOG_KEY))
 	    LogService.log(context, appname, context
 		    .getString(R.string.cached_ip)
 		    + cachedIP);
     }
 
     private static boolean icmpHostup(final Context context) {
 	boolean isUp = false;
 	/*
 	 * If IP hasn't been cached yet cache it
 	 */
 	if (cachedIP == null)
 	    icmpCache(context);
 
 	if (logging)
 	    LogService.log(context, appname, context
 		    .getString(R.string.icmp_method)
 		    + cachedIP);
 
 	try {
 	    if (InetAddress.getByName(cachedIP).isReachable(REACHABLE)) {
 		isUp = true;
 		if (logging)
 		    LogService.log(context, appname, context
 			    .getString(R.string.icmp_success));
 	    }
 	} catch (UnknownHostException e) {
 	    if (logging)
 		LogService.log(context, appname, context
 			.getString(R.string.unknownhostexception));
 	} catch (IOException e) {
 	    if (logging)
 		LogService.log(context, appname, context
 			.getString(R.string.ioexception));
 	}
 	return isUp;
     }
 
     private static void logSupplicant(final Context context, final String state) {
 
 	LogService.log(context, appname, context
 		.getString(R.string.supplicant_state)
 		+ state);
     }
 
     private static void networkNotify(final Context context) {
 	final int NUM_SSIDS = 3;
 	final int SSID_LENGTH = 10;
 	final List<ScanResult> wifiList = wm.getScanResults();
 	String ssid = EMPTYSTRING;
 	String signal = EMPTYSTRING;
 	int n = 0;
 	for (ScanResult sResult : wifiList) {
 	    if (sResult.capabilities.length() == W_REASSOCIATE && n < NUM_SSIDS) {
 		if (sResult.SSID.length() > SSID_LENGTH)
 		    ssid = ssid + sResult.SSID.substring(0, SSID_LENGTH)
 			    + NEWLINE;
 		else
 		    ssid = ssid + sResult.SSID + NEWLINE;
 
 		signal = signal + sResult.level + NEWLINE;
 		n++;
 	    }
 	}
 	NotifUtil.addNetNotif(context, ssid, signal);
     }
 
     private static void notifyWrap(final Context context, final String message) {
 	if (prefs.getFlag(Pref.NOTIF_KEY)) {
 	    NotifUtil.show(context, context
 		    .getString(R.string.wifi_connection_problem)
 		    + message, message, ERR_NOTIF, PendingIntent.getActivity(
 		    context, 0, new Intent(), 0));
 	}
 
     }
 
     private void checkWifi() {
 	if (getisWifiEnabled(ctxt)) {
 	    if (getIsSupplicantConnected(ctxt)) {
 		if (!checkNetwork(ctxt)) {
 		    shouldrepair = true;
 		    handlerWrapper(TEMPLOCK_OFF);
 		    handlerWrapper(SCAN);
 		}
 	    } else {
 		if (!screenstate)
 		    startScan(true);
 		else
 		    pendingscan = true;
 	    }
 
 	}
 
     }
 
     public void cleanup() {
 	ctxt.unregisterReceiver(receiver);
 	clearQueue();
 	handler.removeMessages(MAIN);
     }
 
     private void cleanupPosts() {
 	handler.removeMessages(RECONNECT);
 	handler.removeMessages(REPAIR);
 	handler.removeMessages(WIFITASK);
 	handler.removeMessages(TEMPLOCK_ON);
     }
 
     private void clearQueue() {
 	handler.removeMessages(RECONNECT);
 	handler.removeMessages(REPAIR);
 	handler.removeMessages(WIFITASK);
 	pendingscan = false;
 	pendingreconnect = false;
 	shouldrepair = false;
     }
 
     /*
      * Lets us control duplicate posts and odd handler behavior when screen is
      * off
      */
     private boolean handlerWrapper(final int hmain) {
 	if (handlerCheck(hmain)) {
 	    handler.removeMessages(hmain);
 	    if (screenstate)
 		return handler.sendEmptyMessage(hmain);
 	    else
 		return handler.sendEmptyMessageDelayed(hmain, REALLYSHORTWAIT);
 
 	} else {
 	    handler.removeMessages(hmain);
 	    return handler.sendEmptyMessageDelayed(hmain, REACHABLE);
 	}
     }
 
     private boolean handlerWrapper(final int hmain, final long delay) {
 	if (handlerCheck(hmain)) {
 	    handler.removeMessages(hmain);
 	    return handler.sendEmptyMessageDelayed(hmain, delay);
 	} else {
 	    handler.removeMessages(hmain);
 	    return handler.sendEmptyMessageDelayed(hmain, delay + REACHABLE);
 	}
     }
 
     private void handleScanResults() {
 	if (!wm.isWifiEnabled() || connectee != null)
 	    return;
 
 	if (!pendingscan) {
 	    if (getIsOnWifi(ctxt)) {
 		/*
 		 * We're on wifi, so we want to check for better signal
 		 */
 		handlerWrapper(SIGNALHOP);
 		return;
 	    } else {
 		/*
 		 * Network notification check
 		 */
 		if (prefs.getFlag(Pref.NETNOT_KEY)) {
 		    if (logging)
 			LogService.log(ctxt, appname, ctxt
 				.getString(R.string.network_notification_scan));
 		    networkNotify(ctxt);
 		}
 		/*
 		 * Standard Scan
 		 */
 		if (getKnownAPsBySignal(ctxt) > 0)
 		    connectToBest(ctxt);
 	    }
 	} else if (!pendingreconnect) {
 	    /*
 	     * Service called the scan: dispatch appropriate runnable
 	     */
 	    pendingscan = false;
 	    handlerWrapper(TEMPLOCK_OFF);
 	    handlerWrapper(REPAIR);
 	    if (logging)
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.repairhandler));
 	} else {
 	    pendingscan = false;
 	    handlerWrapper(RECONNECT);
 	    if (logging)
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.reconnecthandler));
 	}
 
     }
 
     private void handleSupplicantIntent(final Intent intent) {
 
 	/*
 	 * Get Supplicant New State
 	 */
 	String sState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)
 		.toString();
 
 	if (sState == null)
 	    sState = INACTIVE;
 
 	if (prefs.getFlag(Pref.STATENOT_KEY) && screenstate) {
 	    notifSSID = getSSID();
 	    if (sState.equals(COMPLETED))
 		notifStatus = CONNECTED;
 	    else {
 		notifStatus = sState;
 		notifSignal = R.drawable.signal0;
 	    }
 	    NotifUtil.addStatNotif(ctxt, notifSSID, notifStatus, notifSignal);
 	}
 	/*
 	 * Flush queue if connected
 	 * 
 	 * Also clear any error notifications
 	 */
 	if (sState == COMPLETED) {
 
 	    if (connectee != null) {
 		handleConnect();
 	    }
 	    clearQueue();
 	    NotifUtil.cancel(ERR_NOTIF, ctxt);
 	    NotifUtil.cancel(NETNOTIFID, ctxt);
 	    pendingscan = false;
 	    pendingreconnect = false;
 	    lastAP = getNetworkID();
 	    return;
 	} else if (prefs.getFlag(Pref.STATENOT_KEY))
 	    notifStatus = EMPTYSTRING;
 
 	/*
 	 * New setting disabling supplicant fixes
 	 */
 	if (prefs.getFlag(Pref.SUPFIX_KEY))
 	    return;
 
 	/*
 	 * The actual meat of the supplicant fixes
 	 */
 	handleSupplicantState(sState);
 
     }
 
     private void handleSupplicantState(final String sState) {
 
 	/*
 	 * Dispatches appropriate supplicant fix
 	 */
 
 	if (!wm.isWifiEnabled()) {
 	    return;
 	} else if (!screenstate && !prefs.getFlag(Pref.SCREEN_KEY))
 	    return;
 	else if (sState == DISCONNECTED) {
 	    startScan(true);
 	    notifyWrap(ctxt, sState);
 	} else if (sState == INACTIVE) {
 	    supplicantFix(true);
 	    notifyWrap(ctxt, sState);
 	}
 
 	if (logging && screenstate)
 	    logSupplicant(ctxt, sState);
     }
 
     private void handleWifiState(final Intent intent) {
 	// What kind of state change is it?
 	int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
 		WifiManager.WIFI_STATE_UNKNOWN);
 	switch (state) {
 	case WifiManager.WIFI_STATE_ENABLED:
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.wifi_state_enabled));
 	    onWifiEnabled();
 	    break;
 	case WifiManager.WIFI_STATE_ENABLING:
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.wifi_state_enabling));
 	    break;
 	case WifiManager.WIFI_STATE_DISABLED:
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.wifi_state_disabled));
 	    onWifiDisabled();
 	    break;
 	case WifiManager.WIFI_STATE_DISABLING:
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.wifi_state_disabling));
 	    break;
 	case WifiManager.WIFI_STATE_UNKNOWN:
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.wifi_state_unknown));
 	    break;
 	}
     }
 
     private void n1Fix() {
 	/*
 	 * Nexus One Sleep Fix duplicating widget function
 	 */
 	if (wm.isWifiEnabled() && !screenstate) {
 	    toggleWifi();
 	}
     }
 
     private void onScreenOff() {
 	/*
 	 * Disable Sleep check
 	 */
 	if (prefs.getFlag(Pref.SCREEN_KEY))
 	    sleepCheck(true);
 	/*
 	 * Schedule N1 fix
 	 */
 	if (prefs.getFlag(Pref.N1FIX2_KEY)) {
 	    handlerWrapper(N1CHECK, REACHABLE);
 	    if (logging)
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.scheduling_n1_fix));
 	}
 
 	if (logging) {
 	    LogService.log(ctxt, appname, ctxt
 		    .getString(R.string.screen_off_handler));
 	}
     }
 
     private void onScreenOn() {
 
 	sleepCheck(false);
 	if (logging) {
 	    LogService.log(ctxt, appname, ctxt
 		    .getString(R.string.screen_on_handler));
 	}
 
 	/*
 	 * Set current state on resume
 	 */
 
 	if (prefs.getFlag(Pref.STATENOT_KEY))
 	    setStatNotif(true);
     }
 
     public void onScreenStateChanged(boolean state) {
 	screenstate = state;
 
 	if (state)
 	    onScreenOn();
 	else
 	    onScreenOff();
     }
 
     private void onWifiDisabled() {
 	clearHandler();
 	if (prefs.getFlag(Pref.STATENOT_KEY))
 	    setStatNotif(false);
     }
 
     private void onWifiEnabled() {
 	handlerWrapper(MAIN, LOOPWAIT);
 	if (prefs.getFlag(Pref.STATENOT_KEY) && screenstate)
 	    setStatNotif(true);
     }
 
     public static boolean setNetworkState(final Context context,
 	    final int network, final boolean state) {
 	WifiManager w = getWifiManager(context);
 	if (state)
 	    w.enableNetwork(network, false);
 	else
 	    w.disableNetwork(network);
 	return w.saveConfiguration();
     }
 
     protected void setStatNotif(final boolean state) {
 	if (state) {
 	    notifStatus = getSupplicantStateString();
 	    NotifUtil.addStatNotif(ctxt, getSSID(), notifStatus, notifSignal);
 	} else {
 	    NotifUtil.addStatNotif(ctxt, NotifUtil.CANCEL, EMPTYSTRING, 0);
 	}
     }
 
     public static boolean getNetworkState(final Context context,
 	    final int network) {
 	if (getWifiManager(context).getConfiguredNetworks().get(network).status == WifiConfiguration.Status.DISABLED)
 	    return false;
 	else
 	    return true;
     }
 
     private void signalHop() {
 	/*
 	 * Need to re-implement best-network-by-signal-and-availability
 	 */
 
 	if (getisWifiEnabled(ctxt))
 	    if (getIsSupplicantConnected(ctxt))
 		if (checkNetwork(ctxt)) {
 		    /*
 		     * Network is fine
 		     */
 		    return;
 		}
 
 	int bestap = NULLVAL;
 	int numKnownAPs = getKnownAPsBySignal(ctxt);
 	if (numKnownAPs > 1) {
 	    bestap = connectToBest(ctxt);
 
 	    if (bestap == NULLVAL) {
 		if (logging)
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.signalhop_no_result));
 		handlerWrapper(TEMPLOCK_OFF);
 		wifiRepair();
 		return;
 	    } else {
 		if (logging) {
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.hopping)
 			    + bestap);
 		    LogService.log(ctxt, appname, ctxt.getString(R.string.nid)
 			    + lastAP);
 		}
 	    }
 	    return;
 	}
 
 	if (logging)
 	    LogService.log(ctxt, appname, ctxt
 		    .getString(R.string.signalhop_nonetworks));
 	handlerWrapper(TEMPLOCK_OFF);
 	if (connectee == null) {
 	    shouldrepair = true;
 	    wifiRepair();
 	}
 
     }
 
     private void sleepCheck(final boolean state) {
 	if (state && wm.isWifiEnabled()) {
 	    /*
 	     * Start sleep check
 	     */
 	    handlerWrapper(SLEEPCHECK, SLEEPWAIT);
 
 	} else {
 	    /*
 	     * Screen is on, remove any posts
 	     */
 	    handler.removeMessages(SLEEPCHECK);
 	    /*
 	     * Check state
 	     */
 	    handlerWrapper(MAIN, SHORTWAIT);
 	}
 
     }
 
     private void startScan(final boolean pending) {
 	// We want a lock after a scan
 	pendingscan = pending;
 	wm.startScan();
 	if (logging)
 	    LogService.log(ctxt, appname, ctxt
 		    .getString(R.string.initiating_scan));
 	tempLock(LOCKWAIT);
     }
 
     private void supplicantFix(final boolean wftoggle) {
 	// Toggling wifi fixes the supplicant
 	if (wftoggle)
 	    toggleWifi();
 	startScan(true);
 	if (logging)
 	    LogService.log(ctxt, appname, ctxt
 		    .getString(R.string.running_supplicant_fix));
     }
 
     private void tempLock(final int time) {
 
 	handlerWrapper(TEMPLOCK_ON);
 	// Queue for later
 	handlerWrapper(TEMPLOCK_OFF, time);
     }
 
     private void toggleWifi() {
 	if (pendingwifitoggle)
 	    return;
 
 	pendingwifitoggle = true;
 	cleanupPosts();
 	tempLock(CONNECTWAIT);
 	/*
 	 * Send Toggle request to broadcastreceiver
 	 */
 	if (logging)
 	    LogService.log(ctxt, appname, ctxt
 		    .getString(R.string.toggling_wifi));
 	PrefUtil.writeBoolean(ctxt, PrefConstants.WIFI_STATE_LOCK, true);
 	ctxt.sendBroadcast(new Intent(WidgetHandler.TOGGLE_WIFI));
     }
 
     private void wifiRepair() {
 	if (!shouldrepair)
 	    return;
 
 	if (screenstate) {
 	    /*
 	     * Start Wifi Task
 	     */
 	    handlerWrapper(WIFITASK);
 	    if (logging)
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.running_wifi_repair));
 	} else {
 	    /*
 	     * if screen off, try wake lock then resubmit to handler
 	     */
 	    wakelock.lock(true);
 	    handlerWrapper(WIFITASK);
 	    if (logging)
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.wifi_repair_post_failed));
 	}
 
 	shouldrepair = false;
 
     }
 
     public void wifiLock(final Boolean state) {
 	if (state)
 	    lock.acquire();
 	else
 	    lock.release();
     }
 
     public boolean wifiLockHeld() {
 	return lock.isHeld();
     }
 
 }
