 /*Copyright [2010-2011] [David Van de Ven]
 
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
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.wahtod.wififixer.prefs.PrefConstants;
 import org.wahtod.wififixer.prefs.PrefUtil;
 import org.wahtod.wififixer.prefs.PrefConstants.NetPref;
 import org.wahtod.wififixer.prefs.PrefConstants.Pref;
 import org.wahtod.wififixer.utility.FifoList;
 import org.wahtod.wififixer.utility.Hostup;
 import org.wahtod.wififixer.utility.LogService;
 import org.wahtod.wififixer.utility.NotifUtil;
 import org.wahtod.wififixer.utility.ScreenStateDetector;
 import org.wahtod.wififixer.utility.StatusDispatcher;
 import org.wahtod.wififixer.utility.StatusMessage;
 import org.wahtod.wififixer.utility.StringUtil;
 import org.wahtod.wififixer.utility.WFConfig;
 import org.wahtod.wififixer.utility.WakeLock;
 import org.wahtod.wififixer.utility.WifiLock;
 import org.wahtod.wififixer.utility.ScreenStateDetector.OnScreenStateChangedListener;
 import org.wahtod.wififixer.widget.WidgetHandler;
 
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.DhcpInfo;
 import android.net.NetworkInfo;
 import android.net.wifi.ScanResult;
 import android.net.wifi.SupplicantState;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.SystemClock;
 import android.text.format.Formatter;
 
 /*
  * Handles all interaction 
  * with WifiManager
  */
 public class WFConnection extends Object implements
 		OnScreenStateChangedListener {
 	private static String accesspointIP;
 	private static String appname;
 	private static PrefUtil prefs;
 	private static Context ctxt;
 	private WakeLock wakelock;
 	private WifiLock wifilock;
 	static boolean screenstate;
 	private static StatusDispatcher statusdispatcher;
 
 	// flags
 	private static boolean shouldrepair = false;
 	private static boolean pendingscan = false;
 	private static boolean pendingreconnect = false;
 	private static boolean repair_reset = false;
 	private static boolean _connected = false;
 
 	// IDs For notifications
 	private static final int ERR_NOTIF = 7972;
 
 	/*
 	 * Supplicant State FIFO for pattern matching
 	 */
 	private static FifoList _supplicantFifo;
 	private static final int FIFO_LENGTH = 10;
 	private static final String INVALID = "INVALID";
 
 	// For blank SSIDs
 	private static final String NULL_SSID = "None";
 
 	// Wifi Connect Intent
 	public static final String CONNECTINTENT = "org.wahtod.wififixer.CONNECT";
 	public static final String NETWORKNAME = "net#";
 
 	// User Event Intent
 	public static final String USEREVENT = "org.wahtod.wififixer.USEREVENT";
 
 	// Empty string
 	private static final String EMPTYSTRING = "";
 	private static final String COLON = ":";
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
 
 	// ms for network checks
 	private final static int REACHABLE = 4000;
 	// ms for main loop sleep
 	private final static int LOOPWAIT = 10000;
 	// ms for sleep loop check
 	private final static long SLEEPWAIT = 60000;
 	// ms for lock delays
 	private final static int LOCKWAIT = 5000;
 	private static final int SHORTWAIT = 1500;
 	// just long enough to avoid sleep bug with handler posts
 	private static final int REALLYSHORTWAIT = 200;
 
 	// Last Scan
 	private static long _last_scan_request;
 	private static final int SCAN_WATCHDOG_DELAY = 15000;
 	private static final int NORMAL_SCAN_DELAY = 15000;
 
 	// for Dbm
 	private static final int DBM_FLOOR = -90;
 
 	// various
 	private static final int NULLVAL = -1;
 	private static int lastAP = NULLVAL;
 
 	private static WFConfig connectee;
 	private static Hostup hostup;
 	private static List<WFConfig> knownbysignal;
 	private static SupplicantState lastSupplicantState;
 	private static int signalcache;
 	private static boolean wifistate;
 
 	// deprecated
 	static boolean templock = false;
 
 	/*
 	 * Constants for wifirepair values
 	 */
 	private static final int W_REASSOCIATE = 0;
 	private static final int W_RECONNECT = 1;
 	private static final int W_REPAIR = 2;
 
 	private static int wifirepair = W_REASSOCIATE;
 
 	/*
 	 * For Supplicant ASSOCIATING bug
 	 */
 	private static int supplicant_associating = 0;
 	private static final int SUPPLICANT_ASSOC_THRESHOLD = 10;
 
 	/*
 	 * For connectToAP sticking
 	 */
 	private static int connecting = 0;
 	private static WifiManager wm_;
 	private static final int CONNECTING_THRESHOLD = 3;
 	private static final long CWDOG_DELAY = 10000;
 
 	// Runnable Constants for handler
 	private static final int INTENT = 37;
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
 	private static final int UPDATESTATUS = 13;
 	private static final int SCANWATCHDOG = 14;
 	private static final int ASSOCWATCHDOG = 15;
 	private static final int CONNECTWATCHDOG = 16;
 
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message message) {
 			switch (message.what) {
 
 			case INTENT:
 				dispatchIntent(ctxt, message.getData());
 				break;
 
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
 				if (prefs.getFlag(Pref.LOG_KEY))
 					LogService.log(ctxt, appname,
 							ctxt.getString(R.string.setting_temp_lock));
 				break;
 
 			case TEMPLOCK_OFF:
 				templock = false;
 				if (prefs.getFlag(Pref.LOG_KEY))
 					LogService.log(ctxt, appname,
 							ctxt.getString(R.string.removing_temp_lock));
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
 
 			case UPDATESTATUS:
 				handler.post(rUpdateStatus);
 				break;
 
 			case SCANWATCHDOG:
 				scanwatchdog();
 				break;
 
 			case ASSOCWATCHDOG:
 				checkAssociateState();
 				break;
 			case CONNECTWATCHDOG:
				demoteNetwork(ctxt, getWifiManager(ctxt).getConnectionInfo()
						.getNetworkId());

 			}
 		}
 	};
 
 	/*
 	 * Runs first time supplicant nonresponsive
 	 */
 	private Runnable rReconnect = new Runnable() {
 		public void run() {
 			if (!getWifiManager(ctxt).isWifiEnabled()) {
 				handlerWrapper(TEMPLOCK_OFF);
 				if (prefs.getFlag(Pref.LOG_KEY))
 					LogService.log(ctxt, appname, ctxt
 							.getString(R.string.wifi_off_aborting_reconnect));
 				return;
 			}
 			if (getKnownAPsBySignal(ctxt) > 0 && connectToBest(ctxt) != NULLVAL) {
 				pendingreconnect = false;
 			} else {
 				wifirepair = W_REASSOCIATE;
 				requestScan();
 				if (prefs.getFlag(Pref.LOG_KEY))
 					LogService
 							.log(ctxt,
 									appname,
 									ctxt.getString(R.string.exiting_supplicant_fix_thread_starting_scan));
 			}
 
 		}
 
 	};
 
 	/*
 	 * Runs second time supplicant nonresponsive
 	 */
 	private Runnable rRepair = new Runnable() {
 		public void run() {
 			if (!getWifiManager(ctxt).isWifiEnabled()) {
 				handlerWrapper(TEMPLOCK_OFF);
 				if (prefs.getFlag(Pref.LOG_KEY))
 					LogService.log(ctxt, appname,
 							ctxt.getString(R.string.wifi_off_aborting_repair));
 				return;
 			}
 
 			if (getKnownAPsBySignal(ctxt) > 0 && connectToBest(ctxt) != NULLVAL) {
 				pendingreconnect = false;
 			} else if (!repair_reset) {
 				pendingreconnect = true;
 				toggleWifi();
 				repair_reset = true;
 				if (prefs.getFlag(Pref.LOG_KEY))
 					LogService.log(ctxt, appname,
 							ctxt.getString(R.string.toggling_wifi));
 
 			}
 			/*
 			 * If repair_reset is true we should be in normal scan mode until
 			 * connected
 			 */
 			else if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(ctxt, appname,
 						ctxt.getString(R.string.scan_mode));
 
 		}
 
 	};
 
 	/*
 	 * Main tick
 	 */
 	private Runnable rMain = new Runnable() {
 		public void run() {
 			/*
 			 * Check for disabled state
 			 */
 			if (prefs.getFlag(Pref.DISABLE_KEY)) {
 				if (prefs.getFlag(Pref.LOG_KEY)) {
 					LogService.log(ctxt, appname,
 							ctxt.getString(R.string.shouldrun_false_dying));
 				}
 			} else {
 				// Queue next run of main runnable
 				handlerWrapper(MAIN, LOOPWAIT);
 				/*
 				 * Schedule update of status
 				 */
 				if (statNotifCheck())
 					handlerWrapper(UPDATESTATUS, SHORTWAIT);
 
 				/*
 				 * First check if we should manage then do wifi checks
 				 */
 				if (shouldManage(ctxt)) {
 					// Check Supplicant
 					if (getisWifiEnabled(ctxt, false)
 							&& !getWifiManager(ctxt).pingSupplicant()) {
 						if (prefs.getFlag(Pref.LOG_KEY))
 							LogService
 									.log(ctxt,
 											appname,
 											ctxt.getString(R.string.supplicant_nonresponsive_toggling_wifi));
 						toggleWifi();
 					} else if (!templock && screenstate)
 						/*
 						 * Check wifi
 						 */
 						if (getisWifiEnabled(ctxt, false)) {
 							checkWifi();
 						}
 
 				}
 			}
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
 				tempLock(SHORTWAIT);
 				getWifiManager(ctxt).reassociate();
 				if (prefs.getFlag(Pref.LOG_KEY))
 					LogService.log(ctxt, appname,
 							ctxt.getString(R.string.reassociating));
 				wifirepair++;
 				notifyWrap(ctxt, ctxt.getString(R.string.reassociating));
 				break;
 
 			case W_RECONNECT:
 				// Ok, now force reconnect..
 				tempLock(SHORTWAIT);
 				getWifiManager(ctxt).reconnect();
 				if (prefs.getFlag(Pref.LOG_KEY))
 					LogService.log(ctxt, appname,
 							ctxt.getString(R.string.reconnecting));
 				wifirepair++;
 				notifyWrap(ctxt, ctxt.getString(R.string.reconnecting));
 				break;
 
 			case W_REPAIR:
 				// Start Scan
 				tempLock(SHORTWAIT);
 				getWifiManager(ctxt).disconnect();
 				requestScan();
 				/*
 				 * Reset state
 				 */
 				wifirepair = W_REASSOCIATE;
 				if (prefs.getFlag(Pref.LOG_KEY))
 					LogService.log(ctxt, appname,
 							ctxt.getString(R.string.repairing));
 				notifyWrap(ctxt, ctxt.getString(R.string.repairing));
 				break;
 			}
 			/*
 			 * Remove wake lock if there is one
 			 */
 			wakelock.lock(false);
 
 			if (prefs.getFlag(Pref.LOG_KEY)) {
 				LogService.log(
 						ctxt,
 						appname,
 						ctxt.getString(R.string.fix_algorithm)
 								+ Integer.toString(wifirepair));
 			}
 		}
 	};
 
 	/*
 	 * Sleep tick if wifi is enabled and screenpref
 	 */
 	private Runnable rSleepcheck = new Runnable() {
 		public void run() {
 			if (shouldManage(ctxt)) {
 				/*
 				 * This is all we want to do.
 				 */
 
 				if (!templock && getisWifiEnabled(ctxt, true)) {
 					wakelock.lock(true);
 					checkWifi();
 				}
 				/*
 				 * Post next run
 				 */
 			}
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
 			 * Start scan
 			 */
 			if (supplicantInterruptCheck(ctxt)) {
 				startScan(true);
 				_last_scan_request = SystemClock.elapsedRealtime();
 				if (prefs.getFlag(Pref.LOG_KEY))
 					LogService.log(ctxt, appname,
 							ctxt.getString(R.string.wifimanager_scan));
 				handlerWrapper(SCANWATCHDOG, SCAN_WATCHDOG_DELAY);
 			} else {
 				if (prefs.getFlag(Pref.LOG_KEY))
 					LogService.log(ctxt, appname,
 							ctxt.getString(R.string.scan_interrupt));
 			}
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
 
 	/*
 	 * Status update runnable
 	 */
 	private Runnable rUpdateStatus = new Runnable() {
 		public void run() {
 			notifStatus = getSupplicantStateString(lastSupplicantState);
 			/*
 			 * Indicate managed status by text
 			 */
 			boolean should = shouldManage(ctxt);
 			if (!should)
 				NotifUtil.setSsidStatus(NotifUtil.SSID_STATUS_UNMANAGED);
 			else
 				NotifUtil.setSsidStatus(NotifUtil.SSID_STATUS_MANAGED);
 
 			statusdispatcher.sendMessage(ctxt, new StatusMessage(notifSSID,
 					notifStatus, notifSignal, true));
 		}
 
 	};
 
 	private BroadcastReceiver receiver = new BroadcastReceiver() {
 		public void onReceive(final Context context, final Intent intent) {
 
 			/*
 			 * Dispatches the broadcast intent to the handler for processing
 			 */
 			Message message = handler.obtainMessage();
 			Bundle data = new Bundle();
 			message.what = INTENT;
 			data.putString(PrefUtil.INTENT_ACTION, intent.getAction());
 			if (intent.getExtras() != null)
 				data.putAll(intent.getExtras());
 			message.setData(data);
 			handler.sendMessage(message);
 		}
 
 	};
 
 	public WFConnection(final Context context, PrefUtil p) {
 		_supplicantFifo = new FifoList(FIFO_LENGTH);
 		prefs = p;
 		statusdispatcher = new StatusDispatcher(context, p);
 		ScreenStateDetector.setOnScreenStateChangedListener(this);
 		appname = LogService.getLogTag(context);
 		screenstate = ScreenStateDetector.getScreenState(context);
 		knownbysignal = new ArrayList<WFConfig>();
 		/*
 		 * Cache Context from consumer
 		 */
 		ctxt = context;
 
 		/*
 		 * Set current AP int
 		 */
 		lastAP = getNetworkID();
 
 		/*
 		 * Set current supplicant state
 		 */
 		lastSupplicantState = getSupplicantState();
 		/*
 		 * Set current wifi radio state
 		 */
 		wifistate = getWifiManager(context).isWifiEnabled();
 
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
 		filter.addAction(ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED);
 
 		// Connect intent
 		filter.addAction(CONNECTINTENT);
 
 		// User Event
 		filter.addAction(USEREVENT);
 
 		context.registerReceiver(receiver, filter);
 
 		// Initialize WakeLock
 		wakelock = new WakeLock(context) {
 
 			@Override
 			public void onAcquire() {
 				if (prefs.getFlag(Pref.LOG_KEY))
 					LogService.log(context, appname,
 							context.getString(R.string.acquiring_wake_lock));
 				super.onAcquire();
 			}
 
 			@Override
 			public void onRelease() {
 				if (prefs.getFlag(Pref.LOG_KEY))
 					LogService.log(context, appname,
 							context.getString(R.string.releasing_wake_lock));
 				super.onRelease();
 			}
 
 		};
 
 		// Initialize PhoneTutorial
 		wifilock = new WifiLock(context) {
 			@Override
 			public void onAcquire() {
 				if (prefs.getFlag(Pref.LOG_KEY))
 					LogService.log(context, appname,
 							context.getString(R.string.acquiring_wifi_lock));
 				super.onAcquire();
 			}
 
 			@Override
 			public void onRelease() {
 				if (prefs.getFlag(Pref.LOG_KEY))
 					LogService.log(context, appname,
 							context.getString(R.string.releasing_wifi_lock));
 				super.onRelease();
 			}
 
 		};
 
 		/*
 		 * acquire wifi lock if should
 		 */
 		if (prefs.getFlag(Pref.WIFILOCK_KEY))
 			wifilock.lock(true);
 
 		/*
 		 * Start status notification if should
 		 */
 		if (screenstate && prefs.getFlag(Pref.STATENOT_KEY))
 			setStatNotif(true);
 
 		/*
 		 * Start Main tick
 		 */
 		handlerWrapper(MAIN);
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
 			PrefUtil.writeBoolean(context, Pref.DISABLE_KEY.key(), true);
 
 			context.sendBroadcast(new Intent(
 					IntentConstants.ACTION_WIFI_SERVICE_DISABLE));
 		}
 	}
 
 	private void clearHandler() {
 		clearMessage(MAIN);
 		clearMessage(REPAIR);
 		clearMessage(RECONNECT);
 		clearMessage(WIFITASK);
 		clearMessage(SLEEPCHECK);
 		clearMessage(SCAN);
 		clearMessage(N1CHECK);
 		clearMessage(SIGNALHOP);
 		clearMessage(SCANWATCHDOG);
 		/*
 		 * Also clear all relevant flags
 		 */
 		shouldrepair = false;
 		pendingreconnect = false;
 	}
 
 	private void clearMessage(int m) {
 		if (handler.hasMessages(m))
 			handler.removeMessages(MAIN);
 	}
 
 	private static void clearConnectedStatus(final String state) {
 		notifStatus = state;
 		notifSignal = 0;
 		notifSSID = ctxt.getString(R.string.null_ssid);
 	}
 
 	private static boolean checkNetwork(final Context context) {
 		boolean isup = false;
 
 		/*
 		 * First check if wifi is current network
 		 */
 
 		if (!getIsOnWifi(context)) {
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(context, appname,
 						context.getString(R.string.wifi_not_current_network));
 			notifSignal = 0;
 			return false;
 		}
 
 		/*
 		 * Check for network connectivity
 		 * 
 		 * First with router, then with google
 		 */
 
 		isup = networkUp(context);
 		if (isup && wifirepair != W_REASSOCIATE)
 			wifirepair = W_REASSOCIATE;
 
 		/*
 		 * Signal check
 		 */
 
 		checkSignal(context);
 
 		/*
 		 * Notify state
 		 */
 		if (screenstate) {
 			if (isup)
 				notifStatus = context.getString(R.string.passed);
 			else
 				notifStatus = context.getString(R.string.failed);
 
 			statusdispatcher.sendMessage(context, new StatusMessage(notifSSID,
 					notifStatus, notifSignal, true));
 		}
 
 		return isup;
 	}
 
 	private static void checkSignal(final Context context) {
 		int signal = getWifiManager(ctxt).getConnectionInfo().getRssi();
 
 		if (statNotifCheck()) {
 			notifSignal = WifiManager.calculateSignalLevel(signal, 5);
 			if (signalcache == 0)
 				signalcache = notifSignal;
 			else if (signalcache != notifSignal) {
 				/*
 				 * Update status notification with new signal value
 				 */
 				statusdispatcher.sendMessage(context, new StatusMessage(
 						notifSSID,
 						context.getString(R.string.checking_network),
 						notifSignal, true));
 			}
 
 		}
 
 		if (signal < DBM_FLOOR) {
 			notifyWrap(context, context.getString(R.string.signal_poor));
 			getWifiManager(ctxt).startScan();
 		}
 
 		if (prefs.getFlag(Pref.LOG_KEY))
 			LogService.log(context, appname,
 					context.getString(R.string.current_dbm) + signal);
 	}
 
 	private void connectToAP(final Context context, final String ssid) {
 
 		if (!getWifiManager(ctxt).isWifiEnabled())
 			return;
 		/*
 		 * Back to explicit connection
 		 */
 		int n = getNetworkfromSSID(context, ssid);
 
 		if (n == -1)
 			return;
 
 		WifiConfiguration target = getWifiManager(ctxt).getConfiguredNetworks()
 				.get(n);
 		/*
 		 * Create sparse WifiConfiguration with details of desired connectee
 		 */
 		connectee = new WFConfig();
 		connectee.wificonfig = target;
 		target.status = WifiConfiguration.Status.CURRENT;
 		getWifiManager(context).updateNetwork(target);
 		getWifiManager(context).enableNetwork(target.networkId, false);
 		getWifiManager(context).reconnect();
 		/*
 		 * Remove all posts to handler
 		 */
 		clearHandler();
 		/*
 		 * Connect
 		 */
 		getWifiManager(ctxt)
 				.enableNetwork(connectee.wificonfig.networkId, true);
 
 		if (prefs.getFlag(Pref.LOG_KEY))
 			LogService.log(context, appname,
 					context.getString(R.string.connecting_to_network)
 							+ connectee.wificonfig.SSID);
 
 	}
 
 	private int connectToBest(final Context context) {
 		/*
 		 * Make sure knownbysignal is populated first
 		 */
 		if (knownbysignal.isEmpty()) {
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService
 						.log(context,
 								appname,
 								context.getString(R.string.knownbysignal_empty_exiting));
 			return NULLVAL;
 		}
 		/*
 		 * Check for connectee (explicit connection)
 		 */
 		if (connectee != null) {
 			for (WFConfig network : knownbysignal) {
 				if (network.wificonfig.SSID.equals(connectee.wificonfig.SSID)) {
 					logBestNetwork(context, network);
 					connecting++;
 					if (connecting >= CONNECTING_THRESHOLD) {
 						LogService
 								.log(context,
 										appname,
 										context.getString(R.string.connection_threshold_exceeded));
 						restoreandReset(context, network);
 					} else
 						connectToAP(context, connectee.wificonfig.SSID);
 					return network.wificonfig.networkId;
 				}
 			}
 		}
 		/*
 		 * Select by best available
 		 */
 
 		WFConfig best = knownbysignal.get(0);
 		/*
 		 * Until BSSID blacklisting is implemented, no point
 		 */
 		connectToAP(context, best.wificonfig.SSID);
 		logBestNetwork(context, best);
 
 		return best.wificonfig.networkId;
 	}
 
 	private static int containsSSID(final String ssid,
 			final List<WifiConfiguration> wifiConfigs) {
 		for (WifiConfiguration sResult : wifiConfigs) {
 			if (StringUtil.removeQuotes(sResult.SSID).equals(ssid))
 				return sResult.networkId;
 		}
 		return -1;
 	}
 
 	private static boolean containsBSSID(final String bssid,
 			final List<WFConfig> results) {
 		for (WFConfig sResult : results) {
 			if (sResult.wificonfig.BSSID.equals(bssid))
 				return true;
 		}
 		return false;
 	}
 
 	private static void demoteNetwork(final Context context, final int n) {
 		if (!getWifiManager(context).isWifiEnabled())
 			return;
 		WifiConfiguration network = getWifiManager(context)
 				.getConfiguredNetworks().get(n);
 		if (network.priority > -1) {
 			network.priority--;
 			getWifiManager(context).updateNetwork(network);
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(
 						context,
 						appname,
 						context.getString(R.string.demoting_network)
 								+ network.SSID
 								+ context.getString(R.string._to_)
 								+ network.priority);
 		} else {
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(context, appname,
 						context.getString(R.string.network_at_priority_floor)
 								+ network.SSID);
 		}
 	}
 
 	private void dispatchIntent(final Context context, final Bundle data) {
 
 		String iAction = data.getString(PrefUtil.INTENT_ACTION);
 		if (iAction.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
 			/*
 			 * Wifi state, e.g. on/off
 			 */
 			handleWifiState(data);
 		else if (iAction.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION))
 			/*
 			 * Supplicant events
 			 */
 			handleSupplicantIntent(data);
 		else if (iAction.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
 			/*
 			 * Scan Results
 			 */
 			handleScanResults();
 		else if (iAction
 				.equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION))
 			/*
 			 * IP connectivity established
 			 */
 			handleNetworkAction();
 		else if (iAction
 				.equals(android.net.ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED))
 			checkBackgroundDataSetting(context);
 		else if (iAction.equals(CONNECTINTENT))
 			handleConnectIntent(context, data);
 		else if (iAction.equals(USEREVENT))
 			handleUserEvent();
 	}
 
 	private static void fixDisabledNetworks(final Context context,
 			List<WifiConfiguration> wflist) {
 		for (WifiConfiguration wfresult : wflist) {
 			/*
 			 * Check for Android 2.x disabled network bug WifiConfiguration
 			 * state won't match stored state
 			 */
 			if (wfresult.status == WifiConfiguration.Status.DISABLED
 					&& !PrefUtil.readNetworkState(context, wfresult.networkId)) {
 				/*
 				 * bugged, enable
 				 */
 				PrefUtil.setNetworkState(context, wfresult.networkId, true);
 				getWifiManager(context)
 						.enableNetwork(wfresult.networkId, false);
 				if (prefs.getFlag(Pref.LOG_KEY))
 					LogService.log(context, appname,
 							context.getString(R.string.reenablenetwork)
 									+ wfresult.SSID);
 
 			}
 		}
 	}
 
 	private static int getNetworkfromSSID(final Context context,
 			final String ssid) {
 		if (ssid == null)
 			return -1;
 		final List<WifiConfiguration> wifiConfigs = getWifiManager(context)
 				.getConfiguredNetworks();
 		for (WifiConfiguration w : wifiConfigs) {
 			if (w.SSID != null
 					&& StringUtil.removeQuotes(w.SSID).equals(
 							StringUtil.removeQuotes(ssid)))
 				return w.networkId;
 		}
 		/*
 		 * Not found
 		 */
 		return -1;
 	}
 
 	private static boolean getIsOnWifi(final Context context) {
 		ConnectivityManager cm = (ConnectivityManager) context
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		try {
 			NetworkInfo ni = cm.getActiveNetworkInfo();
 			if (ni.isConnectedOrConnecting())
 				if (ni.getType() == ConnectivityManager.TYPE_WIFI
 						&& !(ni.getState() == NetworkInfo.State.CONNECTING))
 					return true;
 		} catch (NullPointerException e) {
 			/*
 			 * Need this catch because NetworkInfo can return null
 			 */
 		}
 		return false;
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
 
 	private static boolean getisWifiEnabled(final Context context, boolean log) {
 
 		if (wifistate) {
 			if (log && prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(context, appname,
 						context.getString(R.string.wifi_is_enabled));
 		} else {
 			if (log && prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(context, appname,
 						context.getString(R.string.wifi_is_disabled));
 		}
 		return wifistate;
 	}
 
 	private static int getKnownAPsBySignal(final Context context) {
 
 		/*
 		 * Comparator class for sorting results
 		 */
 		class SortBySignal implements Comparator<WFConfig> {
 			@Override
 			public int compare(WFConfig o2, WFConfig o1) {
 				/*
 				 * Sort by signal and priority
 				 */
 				if (o1.level < o2.level
 						|| o1.wificonfig.priority < o2.wificonfig.priority)
 					return -1;
 				else if (o1.level > o2.level
 						|| o1.wificonfig.priority > o2.wificonfig.priority)
 					return 1;
 				else
 					return 0;
 			}
 		}
 
 		/*
 		 * Acquire scan results
 		 */
 		List<ScanResult> scanResults = getWifiManager(ctxt).getScanResults();
 		/*
 		 * Catch null if scan results fires after wifi disabled or while wifi is
 		 * in intermediate state
 		 */
 		if (scanResults == null) {
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(context, appname,
 						context.getString(R.string.null_scan_results));
 			return NULLVAL;
 		}
 		/*
 		 * Known networks from supplicant.
 		 */
 		List<WifiConfiguration> wifiConfigs = getWifiManager(ctxt)
 				.getConfiguredNetworks();
 		/*
 		 * Iterate the known networks over the scan results, adding found known
 		 * networks.
 		 */
 
 		if (prefs.getFlag(Pref.LOG_KEY))
 			LogService.log(context, appname,
 					context.getString(R.string.parsing_scan_results));
 
 		int index;
 		for (ScanResult sResult : scanResults) {
 			/*
 			 * Look for scan result in our known list
 			 */
 			index = containsSSID(sResult.SSID, wifiConfigs);
 			if (index > -1) {
 				WifiConfiguration wfResult = wifiConfigs.get(index);
 				/*
 				 * Ignore if disabled
 				 */
 				if (PrefUtil.getNetworkState(context, wfResult.networkId)) {
 
 					/*
 					 * Log network
 					 */
 					if (prefs.getFlag(Pref.LOG_KEY)) {
 						StringBuilder out = new StringBuilder();
 						out.append(context.getString(R.string.found_ssid));
 						out.append(sResult.SSID);
 						out.append(NEWLINE);
 						out.append(context.getString(R.string.capabilities));
 						out.append(sResult.capabilities);
 						out.append(NEWLINE);
 						out.append(context.getString(R.string.signal_level));
 						out.append(sResult.level);
 						out.append(NEWLINE);
 						out.append(context.getString(R.string.priority));
 						out.append(wfResult.priority);
 						LogService.log(context, appname, out.toString());
 					}
 
 					/*
 					 * Add result to knownbysignal containsBSSID to avoid dupes
 					 */
 					if (!containsBSSID(sResult.BSSID, knownbysignal))
 						knownbysignal.add(new WFConfig(sResult, wfResult));
 					else {
 						/*
 						 * Update signal level
 						 */
 						for (WFConfig config : knownbysignal) {
 							if (config.wificonfig.BSSID.equals(sResult.BSSID)) {
 								knownbysignal
 										.get(knownbysignal.indexOf(config)).level = sResult.level;
 							}
 						}
 					}
 				}
 			}
 		}
 
 		/*
 		 * Prune non-scanned BSSIDs
 		 */
 		List<WFConfig> toremove = new ArrayList<WFConfig>();
 		for (WFConfig network : knownbysignal) {
 			if (network != null
 					&& !scancontainsBSSID(network.wificonfig.BSSID, scanResults))
 				/*
 				 * Mark for removal
 				 */
 				toremove.add(network);
 		}
 
 		if (!toremove.isEmpty()) {
 			for (WFConfig marked : toremove) {
 				knownbysignal.remove(marked);
 			}
 		}
 
 		if (prefs.getFlag(Pref.LOG_KEY))
 			LogService.log(
 					context,
 					appname,
 					context.getString(R.string.number_of_known)
 							+ knownbysignal.size());
 
 		/*
 		 * Sort by ScanResult.level which is signal
 		 */
 		Collections.sort(knownbysignal, new SortBySignal());
 
 		return knownbysignal.size();
 	}
 
 	private static int getNetworkID() {
 		return getWifiManager(ctxt).getConnectionInfo().getNetworkId();
 	}
 
 	private static String getSSID() {
 		String ssid = getWifiManager(ctxt).getConnectionInfo().getSSID();
 		if (ssid != null)
 			return ssid;
 		else
 			return NULL_SSID;
 	}
 
 	private static SupplicantState getSupplicantState() {
 		return getWifiManager(ctxt).getConnectionInfo().getSupplicantState();
 	}
 
 	private static String getSupplicantStateString(final SupplicantState sstate) {
 		if (SupplicantState.isValidState(sstate))
 			return sstate.name();
 		else
 			return INVALID;
 	}
 
 	public static WifiManager getWifiManager(final Context context) {
 		/*
 		 * Cache WifiManager
 		 */
 		if (wm_ == null) {
 			wm_ = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(context, LogService.getLogTag(context),
 						context.getString(R.string.cachewfinst));
 		}
 		return wm_;
 	}
 
 	private void handleConnect() {
 		if (connectee.wificonfig.SSID.contains(getSSID())) {
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(ctxt, appname,
 						ctxt.getString(R.string.connected_to_network)
 								+ connectee.wificonfig.SSID);
 		} else {
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(ctxt, appname,
 						ctxt.getString(R.string.connect_failed));
 
 			if (supplicantInterruptCheck(ctxt))
 				toggleWifi();
 			else
 				return;
 		}
 		connectee = null;
 	}
 
 	private void handleConnectIntent(Context context, Bundle data) {
 		if (prefs.getFlag(Pref.LOG_KEY))
 			LogService.log(
 					context,
 					appname,
 					context.getString(R.string.connecting_to_network)
 							+ data.getString(NETWORKNAME));
 		connectToAP(ctxt, data.getString(NETWORKNAME));
 	}
 
 	private void handleNetworkAction() {
 		/*
 		 * This action means network connectivty has changed but, we only want
 		 * to run this code for wifi
 		 */
 		if (!getWifiManager(ctxt).isWifiEnabled() || !getIsOnWifi(ctxt))
 			return;
 		else
 			onNetworkConnected();
 	}
 
 	private void handleUserEvent() {
 		/*
 		 * Connect to last known valid network entry
 		 */
 		if (connectee == null)
 			return;
 
 		connectee.wificonfig = getWifiManager(ctxt).getConfiguredNetworks()
 				.get(lastAP);
 
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
 
 	private static boolean networkUp(final Context context) {
 		boolean isUp = false;
 		/*
 		 * Instantiate hostup if it's not already instantiated
 		 */
 		if (hostup == null)
 			hostup = new Hostup();
 		/*
 		 * hostup.getHostup does all the heavy lifting
 		 */
 		if (prefs.getFlag(Pref.LOG_KEY))
 			LogService.log(context, appname,
 					context.getString(R.string.network_check));
 
 		/*
 		 * Launches ICMP/HTTP HEAD check threads which compete for successful
 		 * state return.
 		 */
 		isUp = hostup.getHostup(REACHABLE, context,
 				context.getString(R.string.http) + accesspointIP,
 				prefs.getFlag(Pref.LOG_KEY));
 		if (!isUp)
 			isUp = hostup.getHostup(REACHABLE, context, null,
 					prefs.getFlag(Pref.LOG_KEY));
 
 		return isUp;
 	}
 
 	private static void icmpCache(final Context context) {
 		/*
 		 * Caches DHCP gateway IP for ICMP check
 		 */
 		DhcpInfo info = getWifiManager(ctxt).getDhcpInfo();
 		accesspointIP = Formatter.formatIpAddress(info.gateway);
 		if (prefs.getFlag(Pref.LOG_KEY))
 			LogService.log(context, appname,
 					context.getString(R.string.cached_ip) + accesspointIP);
 	}
 
 	private static void logBestNetwork(final Context context,
 			final WFConfig best) {
 		if (prefs.getFlag(Pref.LOG_KEY)) {
 			StringBuilder output = new StringBuilder();
 			output.append(context.getString(R.string.best_signal_ssid));
 			output.append(best.wificonfig.SSID);
 			output.append(COLON);
 			output.append(best.wificonfig.BSSID);
 			output.append(NEWLINE);
 			output.append(context.getString(R.string.signal_level));
 			output.append(best.level);
 			output.append(NEWLINE);
 			output.append(context.getString(R.string.nid));
 			output.append(best.wificonfig.networkId);
 			LogService.log(context, appname, output.toString());
 		}
 	}
 
 	private static void logSupplicant(final Context context, final String state) {
 
 		LogService.log(context, appname,
 				context.getString(R.string.supplicant_state) + state);
 	}
 
 	private static void notifyWrap(final Context context, final String message) {
 		if (prefs.getFlag(Pref.NOTIF_KEY)) {
 			NotifUtil.show(context,
 					context.getString(R.string.wifi_connection_problem)
 							+ message, message, ERR_NOTIF,
 					PendingIntent.getActivity(context, 0, new Intent(), 0));
 		}
 
 	}
 
 	private void checkAssociateState() {
 		supplicant_associating++;
 		if (supplicant_associating > SUPPLICANT_ASSOC_THRESHOLD) {
 			/*
 			 * Reset supplicant, it's stuck
 			 */
 			toggleWifi();
 			supplicant_associating = 0;
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService
 						.log(ctxt,
 								appname,
 								ctxt.getString(R.string.supplicant_associate_threshold_exceeded));
 		} else
 			handlerWrapper(ASSOCWATCHDOG, SHORTWAIT);
 	}
 
 	private void checkWifi() {
 		if (getIsSupplicantConnected(ctxt)) {
 			if (!checkNetwork(ctxt)) {
 				_connected = false;
 				handlerWrapper(TEMPLOCK_OFF);
 				handlerWrapper(SCAN);
 				shouldrepair = true;
 				wifiRepair();
 			}
 		} else {
 			/*
 			 * start scan, we know we're disconnected.
 			 */
 			requestScan();
 		}
 	}
 
 	public void cleanup() {
 		ctxt.unregisterReceiver(receiver);
 		clearQueue();
 		clearHandler();
 		wifilock.lock(false);
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
 		/*
 		 * Scan results received. Remove Scan Watchdog.
 		 */
 		handler.removeMessages(SCANWATCHDOG);
 		/*
 		 * Sanity checks
 		 */
 		if (!getWifiManager(ctxt).isWifiEnabled())
 			return;
 		if (!pendingscan) {
 			if (getIsOnWifi(ctxt)) {
 				/*
 				 * Signalhop code out
 				 */
 				return;
 			} else {
 				/*
 				 * Parse scan and connect if any known networks discovered
 				 */
 				if (supplicantInterruptCheck(ctxt)) {
 					if (getKnownAPsBySignal(ctxt) > 0)
 						connectToBest(ctxt);
 				}
 			}
 		} else if (!pendingreconnect) {
 			/*
 			 * Service called the scan: dispatch appropriate runnable
 			 */
 			pendingscan = false;
 			handlerWrapper(TEMPLOCK_OFF);
 			handlerWrapper(REPAIR);
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(ctxt, appname,
 						ctxt.getString(R.string.repairhandler));
 		} else {
 			pendingscan = false;
 			handlerWrapper(RECONNECT);
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(ctxt, appname,
 						ctxt.getString(R.string.reconnecthandler));
 		}
 
 	}
 
 	private void handleSupplicantIntent(final Bundle data) {
 
 		/*
 		 * Get Supplicant New State but first make sure it's new
 		 */
 		SupplicantState sState = data
 				.getParcelable(WifiManager.EXTRA_NEW_STATE);
 		if (sState.equals(lastSupplicantState))
 			return;
 		lastSupplicantState = sState;
 		/*
 		 * Supplicant state pattern wedge detection
 		 */
 		if (wedgeCheck())
 			return;
 
 		/*
 		 * Set disconnected
 		 */
 		if (!sState.equals(SupplicantState.COMPLETED)
 				&& !sState.equals(SupplicantState.FOUR_WAY_HANDSHAKE)
 				&& !sState.equals(SupplicantState.GROUP_HANDSHAKE))
 			_connected = false;
 
 		/*
 		 * Check for auth error
 		 */
 		if (data.containsKey(WifiManager.EXTRA_SUPPLICANT_ERROR))
 			NotifUtil.show(ctxt, ctxt.getString(R.string.authentication_error),
 					ctxt.getString(R.string.authentication_error), 2432, null);
 
 		/*
 		 * Status notification updating supplicant state
 		 */
 		if (statNotifCheck()) {
 			if (sState.equals(SupplicantState.COMPLETED)) {
 				notifStatus = SupplicantState.COMPLETED.name();
 				notifSSID = getSSID();
 			} else if (!getIsOnWifi(ctxt))
 				clearConnectedStatus(sState.name());
 
 			statusdispatcher.sendMessage(ctxt, new StatusMessage(notifSSID,
 					notifStatus, notifSignal, true));
 		}
 		if (sState.equals(SupplicantState.COMPLETED) && !_connected) {
 			onNetworkConnecting();
 		}
 
 		/*
 		 * Check for ASSOCIATING bug but first clear check if not ASSOCIATING
 		 */
 		else if (!sState.equals(SupplicantState.ASSOCIATING)) {
 			supplicant_associating = 0;
 			handler.removeMessages(ASSOCWATCHDOG);
 		} else if (sState.equals(SupplicantState.ASSOCIATING)) {
 			handlerWrapper(ASSOCWATCHDOG, SHORTWAIT);
 
 		}
 		/*
 		 * Flush queue if connected
 		 * 
 		 * Also clear any error notifications
 		 */
 		else if (sState.equals(SupplicantState.ASSOCIATED)
 				|| sState.equals(SupplicantState.COMPLETED)) {
 
 			if (connectee != null) {
 				handleConnect();
 			}
 			clearQueue();
 			pendingscan = false;
 			pendingreconnect = false;
 			lastAP = getNetworkID();
 			return;
 		} else if (prefs.getFlag(Pref.STATENOT_KEY)) {
 			notifStatus = EMPTYSTRING;
 			notifSignal = 0;
 		}
 
 		/*
 		 * Supplicant un-wedging
 		 */
 		handleSupplicantState(sState.name());
 	}
 
 	private static boolean wedgeCheck() {
 		_supplicantFifo.add(lastSupplicantState);
 		if (!_supplicantFifo.containsPatterns(
 				SupplicantPatterns.SCAN_BOUNCE_CLUSTER).isEmpty()) {
 			LogService.log(ctxt, appname, ctxt.getString(R.string.scan_bounce));
 			_supplicantFifo.clear();
 			toggleWifi();
 			return true;
 		} else
 			return false;
 	}
 
 	private void handleSupplicantState(final String sState) {
 
 		/*
 		 * Dispatches appropriate supplicant fix
 		 */
 
 		if (!getWifiManager(ctxt).isWifiEnabled()) {
 			return;
 		} else if (!screenstate && !prefs.getFlag(Pref.SCREEN_KEY))
 			return;
 		else if (sState == SupplicantState.DISCONNECTED.name()) {
 			requestScan();
 			notifyWrap(ctxt, sState);
 		} else if (sState == INVALID) {
 			supplicantFix();
 			notifyWrap(ctxt, sState);
 		}
 
 		if (prefs.getFlag(Pref.LOG_KEY) && screenstate)
 			logSupplicant(ctxt, sState);
 	}
 
 	private void handleWifiState(final Bundle data) {
 		// What kind of state change is it?
 		int state = data.getInt(WifiManager.EXTRA_WIFI_STATE,
 				WifiManager.WIFI_STATE_UNKNOWN);
 		switch (state) {
 		case WifiManager.WIFI_STATE_ENABLED:
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(ctxt, appname,
 						ctxt.getString(R.string.wifi_state_enabled));
 			onWifiEnabled();
 			break;
 		case WifiManager.WIFI_STATE_ENABLING:
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(ctxt, appname,
 						ctxt.getString(R.string.wifi_state_enabling));
 			break;
 		case WifiManager.WIFI_STATE_DISABLED:
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(ctxt, appname,
 						ctxt.getString(R.string.wifi_state_disabled));
 			onWifiDisabled();
 			break;
 		case WifiManager.WIFI_STATE_DISABLING:
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(ctxt, appname,
 						ctxt.getString(R.string.wifi_state_disabling));
 			break;
 		case WifiManager.WIFI_STATE_UNKNOWN:
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(ctxt, appname,
 						ctxt.getString(R.string.wifi_state_unknown));
 			break;
 		}
 	}
 
 	private void n1Fix() {
 		/*
 		 * Nexus One Sleep Fix duplicating widget function
 		 */
 		if (getWifiManager(ctxt).isWifiEnabled() && !screenstate) {
 			toggleWifi();
 		}
 	}
 
 	private void onNetworkConnecting() {
 
 		/*
 		 * Check for Android 2.x disabled network bug WifiConfiguration state
 		 * won't match stored state
 		 * 
 		 * Checking onConnecting because auth may complete but IP allocation may
 		 * not, want to bounce to another network if that's the case
 		 */
 		fixDisabledNetworks(ctxt, getWifiManager(ctxt).getConfiguredNetworks());
 		handlerWrapper(CONNECTWATCHDOG, CWDOG_DELAY);
 	}
 
 	private void onNetworkConnected() {
 		/*
 		 * Disable watchdog, we've connected
 		 */
 		handler.removeMessages(CONNECTWATCHDOG);
 		/*
 		 * If this was a bad network before, it's good now.
 		 */
		restoreNetworkPriority(ctxt, getWifiManager(ctxt).getConnectionInfo()
				.getNetworkId());
 		icmpCache(ctxt);
 		_connected = true;
 		notifSSID = getSSID();
 
 		/*
 		 * Make sure connectee is null
 		 */
 		connectee = null;
 		/*
 		 * Reset supplicant associate check
 		 */
 		supplicant_associating = 0;
 
 		/*
 		 * Reset repair_reset flag to false
 		 */
 		repair_reset = false;
 		/*
 		 * restart the Main tick
 		 */
 		if (screenstate)
 			handlerWrapper(MAIN, REALLYSHORTWAIT);
 		else
 			handlerWrapper(SLEEPCHECK, SLEEPWAIT);
 
 		/*
 		 * Clear any error/new network notifications
 		 */
 		NotifUtil.cancel(ctxt, ERR_NOTIF);
 		/*
 		 * Log Non-Managed network
 		 */
 		if (!shouldManage(ctxt) && prefs.getFlag(Pref.LOG_KEY))
 			LogService.log(ctxt, appname,
 					ctxt.getString(R.string.not_managing_network) + notifSSID);
 
 		/*
 		 * Log connection
 		 */
 		if (prefs.getFlag(Pref.LOG_KEY))
 			LogService.log(ctxt, appname,
 					ctxt.getString(R.string.connected_to_network) + getSSID());
 	}
 
 	private void onScreenOff() {
 		/*
 		 * Clear StatusDispatcher
 		 */
 		statusdispatcher.clearQueue();
 		/*
 		 * Disable Sleep check
 		 */
 		if (prefs.getFlag(Pref.SCREEN_KEY))
 			sleepCheck(true);
 		else if (prefs.getFlag(Pref.WIFILOCK_KEY))
 			wifilock.lock(false);
 		/*
 		 * Schedule N1 fix
 		 */
 		if (prefs.getFlag(Pref.N1FIX2_KEY)) {
 			handlerWrapper(N1CHECK, REACHABLE);
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(ctxt, appname,
 						ctxt.getString(R.string.scheduling_n1_fix));
 		}
 
 		if (prefs.getFlag(Pref.LOG_KEY)) {
 			LogService.log(ctxt, appname,
 					ctxt.getString(R.string.screen_off_handler));
 		}
 	}
 
 	private void onScreenOn() {
 		/*
 		 * Re-enable lock if it's off
 		 */
 		if (prefs.getFlag(Pref.WIFILOCK_KEY))
 			wifilock.lock(true);
 
 		sleepCheck(false);
 		if (prefs.getFlag(Pref.LOG_KEY)) {
 			LogService.log(ctxt, appname,
 					ctxt.getString(R.string.screen_on_handler));
 		}
 		/*
 		 * Notify current state on resume
 		 */
 		if (prefs.getFlag(Pref.STATENOT_KEY) && statNotifCheck())
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
 		wifistate = false;
 		clearHandler();
 		clearConnectedStatus(ctxt.getString(R.string.wifi_is_disabled));
 		statusdispatcher.sendMessage(
 				ctxt,
 				new StatusMessage(NULL_SSID, ctxt
 						.getString(R.string.wifi_is_disabled), 0, true));
 
 		if (prefs.getFlag(Pref.LOG_KEY))
 			LogService.setLogTS(ctxt, false, 0);
 		setWifiState(ctxt, false);
 	}
 
 	private void onWifiEnabled() {
 		wifistate = true;
 		handlerWrapper(MAIN, LOOPWAIT);
 		if (prefs.getFlag(Pref.STATENOT_KEY) && screenstate)
 			setStatNotif(true);
 
 		if (prefs.getFlag(Pref.LOG_KEY))
 			LogService.setLogTS(ctxt, true, SHORTWAIT);
 
 		/*
 		 * Remove wifi state lock
 		 */
 		if (PrefUtil.readBoolean(ctxt, PrefConstants.WIFI_STATE_LOCK))
 			PrefUtil.writeBoolean(ctxt, PrefConstants.WIFI_STATE_LOCK, false);
 		setWifiState(ctxt, true);
 	}
 
 	public static boolean removeNetwork(final Context context, final int network) {
 		boolean state = getWifiManager(context).removeNetwork(network);
 		getWifiManager(context).saveConfiguration();
 		return state;
 	}
 
 	private static void restoreNetworkPriority(final Context context,
 			final int n) {
 		WifiConfiguration network = getWifiManager(context)
 				.getConfiguredNetworks().get(n);
 		network.priority = 2;
 		getWifiManager(context).updateNetwork(network);
 	}
 
 	private void requestScan() {
 		handlerWrapper(SCAN);
 	}
 
 	private static boolean scancontainsBSSID(final String bssid,
 			final List<ScanResult> results) {
 		for (ScanResult sResult : results) {
 			if (sResult.BSSID.equals(bssid))
 				return true;
 		}
 		return false;
 	}
 
 	public void scanwatchdog() {
 		if (getWifiManager(ctxt).isWifiEnabled()
 				&& !getIsOnWifi(ctxt)
 				&& (SystemClock.elapsedRealtime() - _last_scan_request) > SCAN_WATCHDOG_DELAY) {
 			/*
 			 * Reset Wifi, scan didn't succeed.
 			 */
 			toggleWifi();
 		}
 		if (prefs.getFlag(Pref.LOG_KEY))
 			LogService.log(
 					ctxt,
 					appname,
 					ctxt.getString(R.string.scan_failed)
 							+ ":"
 							+ String.valueOf(SystemClock.elapsedRealtime()
 									- _last_scan_request) + "ms");
 		if (screenstate)
 			handlerWrapper(SCAN, NORMAL_SCAN_DELAY);
 		else
 			handlerWrapper(SCAN, SLEEPWAIT);
 	}
 
 	private static void setWifiState(final Context context, final boolean state) {
 		/*
 		 * Set Wifi State on honeycomb notification
 		 */
 		if (prefs.getFlag(Pref.STATENOT_KEY)) {
 			statusdispatcher.sendMessage(context, new StatusMessage(notifSSID,
 					notifStatus, notifSignal, true));
 		}
 	}
 
 	protected void setStatNotif(final boolean state) {
 		if (state) {
 			notifStatus = getSupplicantStateString(lastSupplicantState);
 			notifSSID = getSSID();
 			statusdispatcher.sendMessage(ctxt, new StatusMessage(notifSSID,
 					notifStatus, notifSignal, true));
 		} else {
 			statusdispatcher.sendMessage(ctxt, new StatusMessage(false));
 		}
 	}
 
 	private static boolean shouldManage(final Context ctx) {
 		String ssid = PrefUtil.getSafeFileName(ctx, notifSSID);
 		if (ssid == NULL_SSID)
 			return true;
 		else if (prefs.getnetPref(ctxt, NetPref.NONMANAGED_KEY, ssid) == 1)
 			return false;
 		else
 			return true;
 	}
 
 	private static boolean statNotifCheck() {
 		if (screenstate && getWifiManager(ctxt).isWifiEnabled())
 			return true;
 		else
 			return false;
 	}
 
 	private void signalHop() {
 		/*
 		 * Connect To best will always find best signal/availability
 		 */
 
 		if (getisWifiEnabled(ctxt, false))
 			if (getIsSupplicantConnected(ctxt))
 				if (checkNetwork(ctxt)) {
 					/*
 					 * Network is fine
 					 */
 					return;
 				}
 		/*
 		 * Switch to best
 		 */
 		int bestap = NULLVAL;
 		if (getKnownAPsBySignal(ctxt) > 1) {
 			bestap = connectToBest(ctxt);
 
 			if (bestap == NULLVAL) {
 				if (prefs.getFlag(Pref.LOG_KEY))
 					LogService.log(ctxt, appname,
 							ctxt.getString(R.string.signalhop_no_result));
 				handlerWrapper(TEMPLOCK_OFF);
 				wifiRepair();
 				return;
 			} else {
 				if (prefs.getFlag(Pref.LOG_KEY)) {
 					LogService.log(ctxt, appname,
 							ctxt.getString(R.string.hopping) + bestap);
 					LogService.log(ctxt, appname, ctxt.getString(R.string.nid)
 							+ lastAP);
 				}
 			}
 			return;
 		}
 
 		if (prefs.getFlag(Pref.LOG_KEY))
 			LogService.log(ctxt, appname,
 					ctxt.getString(R.string.signalhop_nonetworks));
 		handlerWrapper(TEMPLOCK_OFF);
 		if (connectee == null) {
 			shouldrepair = true;
 			wifiRepair();
 		}
 
 	}
 
 	private void sleepCheck(final boolean state) {
 		if (state && getisWifiEnabled(ctxt, false)) {
 			/*
 			 * Start sleep check
 			 */
 			handlerWrapper(SLEEPCHECK, SLEEPWAIT);
 			handler.removeMessages(MAIN);
 
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
 
 	public void startScan(final boolean pending) {
 		pendingscan = pending;
 		// We want a wakelock during scan, broadcastreceiver for results
 		// gets its own wake lock
 		wakelock.lock(true);
 		if (getWifiManager(ctxt).startScan()) {
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(ctxt, appname,
 						ctxt.getString(R.string.initiating_scan));
 			tempLock(LOCKWAIT);
 		} else {
 			/*
 			 * Reset supplicant, log
 			 */
 			toggleWifi();
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(ctxt, appname,
 						ctxt.getString(R.string.scan_failed));
 		}
 		wakelock.lock(false);
 	}
 
 	private void supplicantFix() {
 		// Toggling wifi resets the supplicant
 		toggleWifi();
 
 		if (prefs.getFlag(Pref.LOG_KEY))
 			LogService.log(ctxt, appname,
 					ctxt.getString(R.string.running_supplicant_fix));
 	}
 
 	private static boolean supplicantInterruptCheck(final Context context) {
 
 		SupplicantState sstate = getSupplicantState();
 		/*
 		 * First, make sure this won't interrupt anything
 		 */
 		if (sstate.equals(SupplicantState.ASSOCIATING)
 				|| sstate.equals(SupplicantState.ASSOCIATED)
 				|| sstate.equals(SupplicantState.COMPLETED)
 				|| sstate.equals(SupplicantState.GROUP_HANDSHAKE)
 				|| sstate.equals(SupplicantState.FOUR_WAY_HANDSHAKE))
 			return false;
 		else
 			return true;
 	}
 
 	private void tempLock(final int time) {
 
 		handlerWrapper(TEMPLOCK_ON);
 		// Queue for later
 		handlerWrapper(TEMPLOCK_OFF, time);
 	}
 
 	private static void toggleWifi() {
 		/*
 		 * Send Toggle request to broadcastreceiver
 		 */
 		if (prefs.getFlag(Pref.LOG_KEY))
 			LogService.log(ctxt, appname,
 					ctxt.getString(R.string.toggling_wifi));
 		ctxt.sendBroadcast(new Intent(WidgetHandler.TOGGLE_WIFI));
 		statusdispatcher.sendMessage(
 				ctxt,
 				new StatusMessage(NULL_SSID, ctxt
 						.getString(R.string.toggling_wifi), 0, true));
 	}
 
 	private void wifiRepair() {
 		if (!shouldrepair)
 			return;
 
 		if (screenstate) {
 			/*
 			 * Start Wifi Task
 			 */
 			handlerWrapper(WIFITASK);
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(ctxt, appname,
 						ctxt.getString(R.string.running_wifi_repair));
 		} else {
 			/*
 			 * if screen off, try wake lock then resubmit to handler
 			 */
 			wakelock.lock(true);
 			handlerWrapper(WIFITASK);
 			if (prefs.getFlag(Pref.LOG_KEY))
 				LogService.log(ctxt, appname,
 						ctxt.getString(R.string.wifi_repair_post_failed));
 		}
 
 		shouldrepair = false;
 
 	}
 
 	public void wifiLock(final boolean state) {
 		wifilock.lock(state);
 	}
 
 	private static void restoreandReset(final Context ctxt,
 			final WFConfig network) {
 		/*
 		 * Enable bugged disabled networks, reset
 		 */
 		fixDisabledNetworks(ctxt, getWifiManager(ctxt).getConfiguredNetworks());
 		toggleWifi();
 		connecting = 0;
 	}
 
 }
