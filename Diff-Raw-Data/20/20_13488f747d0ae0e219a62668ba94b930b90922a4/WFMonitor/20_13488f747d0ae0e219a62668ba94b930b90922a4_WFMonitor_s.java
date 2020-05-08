 /*	    Wifi Fixer for Android
     Copyright (C) 2010-2013  David Van de Ven
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see http://www.gnu.org/licenses
  */
 
 package org.wahtod.wififixer;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.wahtod.wififixer.prefs.PrefConstants;
 import org.wahtod.wififixer.prefs.PrefConstants.Pref;
 import org.wahtod.wififixer.prefs.PrefUtil;
 import org.wahtod.wififixer.ui.LogFragment;
 import org.wahtod.wififixer.ui.WifiFixerActivity;
 import org.wahtod.wififixer.utility.BroadcastHelper;
 import org.wahtod.wififixer.utility.FifoList;
 import org.wahtod.wififixer.utility.HostMessage;
 import org.wahtod.wififixer.utility.Hostup;
 import org.wahtod.wififixer.utility.LogService;
 import org.wahtod.wififixer.utility.NotifUtil;
 import org.wahtod.wififixer.utility.ScreenStateDetector;
 import org.wahtod.wififixer.utility.ScreenStateDetector.OnScreenStateChangedListener;
 import org.wahtod.wififixer.utility.ServiceAlarm;
 import org.wahtod.wififixer.utility.StatusDispatcher;
 import org.wahtod.wififixer.utility.StatusMessage;
 import org.wahtod.wififixer.utility.StopWatch;
 import org.wahtod.wififixer.utility.StringUtil;
 import org.wahtod.wififixer.utility.ThreadHandler;
 import org.wahtod.wififixer.utility.WFConfig;
 import org.wahtod.wififixer.utility.WakeLock;
 import org.wahtod.wififixer.utility.WifiLock;
 import org.wahtod.wififixer.widget.WidgetReceiver;
 
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
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.format.Formatter;
 
 /*
  * Handles all interaction 
  * with WifiManager
  */
public class WFMonitor extends Object implements
		OnScreenStateChangedListener {
 	private static final int DEFAULT_DBM_FLOOR = -90;
 	private String accesspointIP;
 	private static String appname;
 	protected static WeakReference<Context> ctxt;
 	private WakeLock wakelock;
 	private WifiLock wifilock;
 	boolean screenstate;
 
 	/*
 	 * For ongoing status notification, widget, and Status fragment
 	 */
 	protected StatusDispatcher _statusdispatcher;
 
 	// flags
 	private static boolean shouldrepair = false;
 	private static boolean pendingscan = false;
 	private static boolean pendingreconnect = false;
 	private static boolean repair_reset = false;
 	private static boolean _connected = false;
 	private static boolean _signalhopping = false;
 
 	// IDs For notifications
 	private static final int ERR_NOTIF = 7972;
 
 	/*
 	 * Supplicant State FIFO for pattern matching
 	 */
 	private FifoList _supplicantFifo;
 	private static final int FIFO_LENGTH = 10;
 
 	// Sleep Check Intent for Alarm
 	public static final String SLEEPCHECKINTENT = "org.wahtod.wififixer.SLEEPCHECK";
 
 	// Wifi Connect Intent
 	public static final String CONNECTINTENT = "org.wahtod.wififixer.CONNECT";
 	public static final String NETWORKNAME = "net#";
 
 	// User Event Intent
 	public static final String REASSOCIATE_INTENT = "org.wahtod.wififixer.USEREVENT";
 	private static final String COLON = ":";
 	private static final String NEWLINE = "\n";
 
 	// ms for signalhop check
 	private static final long SIGNAL_CHECK_INTERVAL = 30000;
 	// ms for network checks
 	private final static int REACHABLE = 6000;
 	// ms for main loop sleep
 	private final static int LOOPWAIT = 20000;
 	// ms for sleep loop check
 	private final static long SLEEPWAIT = 120000;
 	private static final int SHORTWAIT = 1500;
 	// just long enough to avoid sleep bug with handler posts
 	private static final int REALLYSHORTWAIT = 500;
 
 	// Last Scan
 	private StopWatch _scantimer;
 	private static final int SCAN_WATCHDOG_DELAY = 25000;
 	private static final int NORMAL_SCAN_DELAY = 20000;
 
 	// various
 	private static final int NULLVAL = -1;
 	private static int lastAP = NULLVAL;
 
 	private WFConfig connectee;
 	private static volatile Hostup hostup;
 	private List<WFConfig> knownbysignal;
 	private SupplicantState lastSupplicantState;
 	private boolean wifistate;
 
 	/*
 	 * Constants for wifirepair values
 	 */
 	private static final int W_REASSOCIATE = 0;
 	private static final int W_RECONNECT = 1;
 	private static final int W_REPAIR = 2;
 
 	private static int wifirepair = W_REASSOCIATE;
 
 	private static long _signalCheckTime;
 
 	/*
 	 * For Supplicant ASSOCIATING bug
 	 */
 	private static int supplicant_associating = 0;
 	private static final int SUPPLICANT_ASSOC_THRESHOLD = 10;
 
 	/*
 	 * Supplicant State triggers Have to use string because SupplicantState
 	 * enums change over Android versions
 	 */
 	private static final String SSTATE_ASSOCIATING = "ASSOCIATING";
 	private static final String SSTATE_ASSOCIATED = "ASSOCIATED";
 	private static final String SSTATE_INVALID = "INVALID";
 
 	/*
 	 * For connectToAP sticking
 	 */
 	private static int connecting = 0;
 	private static WifiManager wm_;
 	private static WeakReference<WFMonitor> self;
 	private static final int CONNECTING_THRESHOLD = 5;
 	private static final long CWDOG_DELAY = 10000;
 
 	private static volatile Handler handler = new Handler();
 	private static volatile ThreadHandler _nethandler;
 	private static volatile boolean isUp;
 
 	protected static Runnable NetCheckRunnable = new Runnable() {
 		@Override
 		public void run() {
 			/*
 			 * First check if wifi is current network
 			 */
 			if (!getIsOnWifi(ctxt.get())) {
 				log(ctxt.get(), (R.string.wifi_not_current_network));
 				self.get()
 						.clearConnectedStatus(
 								ctxt.get().getString(
 										R.string.wifi_not_current_network));
 			} else {
 
 				isUp = networkUp(ctxt.get());
 
 				if (isUp && wifirepair != W_REASSOCIATE)
 					wifirepair = W_REASSOCIATE;
 			}
 			handler.post(PostExecuteRunnable);
 		}
 	};
 
 	protected static Runnable PostExecuteRunnable = new Runnable() {
 		private static final int STATUS_UPDATE_DELAY = 3000;
 
 		@Override
 		public void run() {
 			/*
 			 * Notify state
 			 */
 			if (self.get().screenstate) {
 				StatusMessage m = new StatusMessage();
 				if (isUp)
 					m.setStatus(ctxt.get().getString(R.string.passed));
 				else
 					m.setStatus(ctxt.get().getString(R.string.failed));
 				StatusMessage.send(ctxt.get(), m);
 			}
 			self.get().handlerWrapper(new PostNetCheckRunnable(isUp));
 			self.get().handlerWrapper(rStatusUpdate, STATUS_UPDATE_DELAY);
 		}
 	};
 
 	/*
 	 * Processes intent message
 	 */
 	protected static class IntentRunnable implements Runnable {
 		Bundle d;
 
 		@Override
 		public void run() {
 			self.get().dispatchIntent(ctxt.get(), d);
 		}
 
 		public IntentRunnable(Bundle b) {
 			this.d = b;
 		}
 	};
 
 	/*
 	 * Signal Check and Network Result handler for AsyncTask postExcecute
 	 */
 	protected static class PostNetCheckRunnable implements Runnable {
 		Boolean state;
 
 		@Override
 		public void run() {
 			self.get().checkSignal(ctxt.get());
 			self.get().handleNetworkResult(state);
 		}
 
 		public PostNetCheckRunnable(boolean b) {
 			this.state = b;
 		}
 	};
 
 	/*
 	 * Sends status update
 	 */
 	protected static Runnable rStatusUpdate = new Runnable() {
 		public void run() {
 			StatusMessage.send(
 					ctxt.get(),
 					StatusMessage.getNew().setStatus(
 							getSupplicantState().name()));
 		}
 	};
 
 	/*
 	 * Runs first time supplicant nonresponsive
 	 */
 	protected static Runnable rReconnect = new Runnable() {
 		public void run() {
 			if (!getWifiManager(ctxt.get()).isWifiEnabled()) {
 				log(ctxt.get(), R.string.wifi_off_aborting_reconnect);
 				return;
 			}
 			if (getKnownAPsBySignal(ctxt.get()) > 0
 					&& self.get().connectToBest(ctxt.get()) != NULLVAL) {
 				pendingreconnect = false;
 			} else {
 				wifirepair = W_REASSOCIATE;
 				self.get().handlerWrapper(rScanWatchDog, SHORTWAIT);
 				log(ctxt.get(),
 						R.string.exiting_supplicant_fix_thread_starting_scan);
 			}
 		}
 	};
 
 	/*
 	 * Runs second time supplicant nonresponsive
 	 */
 	protected static Runnable rRepair = new Runnable() {
 		public void run() {
 			if (!getWifiManager(ctxt.get()).isWifiEnabled()) {
 				log(ctxt.get(), R.string.wifi_off_aborting_repair);
 				return;
 			}
 
 			if (getKnownAPsBySignal(ctxt.get()) > 0
 					&& self.get().connectToBest(ctxt.get()) != NULLVAL) {
 				pendingreconnect = false;
 			} else if (!repair_reset) {
 				pendingreconnect = true;
 				toggleWifi();
 				repair_reset = true;
 				log(ctxt.get(), R.string.toggling_wifi);
 
 			}
 			/*
 			 * If repair_reset is true we should be in normal scan mode until
 			 * connected
 			 */
 			log(ctxt.get(), R.string.scan_mode);
 		}
 	};
 
 	/*
 	 * Main tick
 	 */
 	protected static Runnable rMain = new Runnable() {
 		public void run() {
 			/*
 			 * Check for disabled state
 			 */
 			if (PrefUtil.getFlag(Pref.DISABLE_KEY))
 				log(ctxt.get(), R.string.shouldrun_false_dying);
 			else {
 				// Queue next run of main runnable
 				self.get().handlerWrapper(rMain, LOOPWAIT);
 				/*
 				 * First check if we should manage then do wifi checks
 				 */
 				if (shouldManage(ctxt.get())) {
 					// Check Supplicant
 					if (getisWifiEnabled(ctxt.get(), false)
 							&& !getWifiManager(ctxt.get()).pingSupplicant()) {
 						log(ctxt.get(),
 								R.string.supplicant_nonresponsive_toggling_wifi);
 						toggleWifi();
 					} else if (self.get().screenstate)
 						/*
 						 * Check wifi
 						 */
 						if (getisWifiEnabled(ctxt.get(), false)) {
 							self.get().checkWifi();
 						}
 				}
 			}
 		}
 	};
 
 	/*
 	 * Handles non-supplicant wifi fixes.
 	 */
 	protected static Runnable rWifiTask = new Runnable() {
 		public void run() {
 			switch (wifirepair) {
 
 			case W_REASSOCIATE:
 				// Let's try to reassociate first..
 				getWifiManager(ctxt.get()).reassociate();
 				log(ctxt.get(), R.string.reassociating);
 				wifirepair++;
 				notifyWrap(ctxt.get(),
 						ctxt.get().getString(R.string.reassociating));
 				break;
 
 			case W_RECONNECT:
 				// Ok, now force reconnect..
 				getWifiManager(ctxt.get()).reconnect();
 				log(ctxt.get(), R.string.reconnecting);
 				wifirepair++;
 				notifyWrap(ctxt.get(),
 						ctxt.get().getString(R.string.reconnecting));
 				break;
 
 			case W_REPAIR:
 				// Start Scan
 				getWifiManager(ctxt.get()).disconnect();
 				self.get().handlerWrapper(rScanWatchDog, SHORTWAIT);
 				/*
 				 * Reset state
 				 */
 				wifirepair = W_REASSOCIATE;
 				log(ctxt.get(), R.string.repairing);
 				notifyWrap(ctxt.get(), ctxt.get().getString(R.string.repairing));
 				break;
 			}
 			/*
 			 * Remove wake lock if there is one
 			 */
 			self.get().wakelock.lock(false);
 
 			log(ctxt.get(),
 					new StringBuilder(ctxt.get().getString(
 							R.string.fix_algorithm)).append(wifirepair)
 							.toString());
 		}
 	};
 
 	/*
 	 * Sleep tick if wifi is enabled and screenpref
 	 */
 	protected static Runnable rSleepcheck = new Runnable() {
 		public void run() {
 			if (shouldManage(ctxt.get())) {
 				/*
 				 * This is all we want to do.
 				 */
 
 				if (getisWifiEnabled(ctxt.get(), true)) {
 					self.get().checkWifi();
 				}
 			}
 
 		}
 	};
 
 	/*
 	 * Scanner runnable
 	 */
 	protected static Runnable rScan = new Runnable() {
 		public void run() {
 			/*
 			 * Start scan
 			 */
 			if (supplicantInterruptCheck(ctxt.get())) {
 				self.get().startScan(true);
 				log(ctxt.get(), R.string.wifimanager_scan);
 				self.get().handlerWrapper(rScanWatchDog, SCAN_WATCHDOG_DELAY);
 			} else {
 				log(ctxt.get(), R.string.scan_interrupt);
 			}
 
 		}
 	};
 
 	/*
 	 * SignalHop runnable
 	 */
 	protected static Runnable rSignalhop = new Runnable() {
 		public void run() {
 			/*
 			 * Remove all posts first
 			 */
 			if (!self.get().screenstate)
 				self.get().wakelock.lock(true);
 			self.get().clearQueue();
 			/*
 			 * run the signal hop check
 			 */
 			self.get().signalHop();
 			self.get().wakelock.lock(false);
 
 		}
 
 	};
 
 	/*
 	 * Makes sure scan happens every X seconds
 	 */
 	protected static Runnable rScanWatchDog = new Runnable() {
 		@Override
 		public void run() {
 			scanwatchdog();
 
 		}
 	};
 
 	/*
 	 * Watches association with AP and resets wifi if it takes too long
 	 */
 	protected static Runnable rN1Fix = new Runnable() {
 
 		@Override
 		public void run() {
 			n1Fix();
 
 		}
 	};
 
 	/*
 	 * Watches association with AP and resets wifi if it takes too long
 	 */
 	protected static Runnable rAssocWatchDog = new Runnable() {
 
 		@Override
 		public void run() {
 			checkAssociateState();
 
 		}
 	};
 
 	/*
 	 * Demotes networks we fail to connect to for one reason or another
 	 */
 
 	protected static Runnable rDemoter = new Runnable() {
 		@Override
 		public void run() {
 			int n = getNetworkID();
 			if (n != -1)
 				demoteNetwork(ctxt.get(), n);
 		}
 	};
 
 	private BroadcastReceiver receiver = new BroadcastReceiver() {
 		public void onReceive(final Context context, final Intent intent) {
 			handleBroadcast(context, intent);
 		}
 	};
 
 	private BroadcastReceiver localreceiver = new BroadcastReceiver() {
 		public void onReceive(final Context context, final Intent intent) {
 			handleBroadcast(context, intent);
 		}
 	};
 
 	private void handleBroadcast(final Context context, final Intent intent) {
 		/*
 		 * Dispatches the broadcast intent to the handler for processing
 		 */
 		Bundle data = new Bundle();
 		data.putString(PrefUtil.INTENT_ACTION, intent.getAction());
 		if (intent.getExtras() != null)
 			data.putAll(intent.getExtras());
 		IntentRunnable i = new IntentRunnable(data);
 		handler.post(i);
 	}
 
 	public WFMonitor(final Context context) {
 		_scantimer = new StopWatch();
 		self = new WeakReference<WFMonitor>(this);
 		_supplicantFifo = new FifoList(FIFO_LENGTH);
 		_statusdispatcher = new StatusDispatcher(context, handler);
 		ScreenStateDetector.setOnScreenStateChangedListener(this);
 		appname = LogService.getLogTag(context);
 		screenstate = ScreenStateDetector.getScreenState(context);
 		knownbysignal = new ArrayList<WFConfig>();
 		/*
 		 * Cache Context from service
 		 */
 		ctxt = new WeakReference<Context>(context);
 		/*
 		 * Set IP
 		 */
 		if (getIsOnWifi(context))
 			icmpCache(context);
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
 		 * Set up system Intent filters
 		 */
 		IntentFilter filter = new IntentFilter(
 				WifiManager.WIFI_STATE_CHANGED_ACTION);
 		// Supplicant State filter
 		filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
 		// Network State filter
 		filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
 		// wifi scan results available callback
 		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
 		// Sleep Check
 		filter.addAction(SLEEPCHECKINTENT);
 		BroadcastHelper.registerReceiver(context, receiver, filter, false);
 		/*
 		 * Local Intent filters
 		 */
 		// Connect intent
 		filter = new IntentFilter(CONNECTINTENT);
 		// User Event
 		filter.addAction(REASSOCIATE_INTENT);
 		BroadcastHelper.registerReceiver(context, localreceiver, filter, true);
 		// Initialize WakeLock
 		wakelock = new WakeLock(context) {
 
 			@Override
 			public void onAcquire() {
 				log(context, R.string.acquiring_wake_lock);
 				super.onAcquire();
 			}
 
 			@Override
 			public void onRelease() {
 				log(context, R.string.releasing_wake_lock);
 				super.onRelease();
 			}
 
 		};
 
 		wifilock = new WifiLock(context) {
 			@Override
 			public void onAcquire() {
 				log(context, R.string.acquiring_wifi_lock);
 				super.onAcquire();
 			}
 
 			@Override
 			public void onRelease() {
 				log(context, R.string.releasing_wifi_lock);
 				super.onRelease();
 			}
 		};
 
 		/*
 		 * acquire wifi lock if should
 		 */
 		if (PrefUtil.getFlag(Pref.WIFILOCK_KEY))
 			wifilock.lock(true);
 
 		/*
 		 * Start status notification if should
 		 */
 		if (screenstate && PrefUtil.getFlag(Pref.STATENOT_KEY))
 			setStatNotif(true);
 
 		/*
 		 * Instantiate network checker
 		 */
 		hostup = new Hostup(context);
 		_nethandler = new ThreadHandler(
 				context.getString(R.string.netcheckthread));
 	}
 
 	private void clearHandler() {
 		handler.removeCallbacksAndMessages(null);
 		/*
 		 * Also clear all relevant flags
 		 */
 		shouldrepair = false;
 		pendingreconnect = false;
 	}
 
 	private void clearMessage(Runnable r) {
 		handler.removeCallbacks(r);
 	}
 
 	private void clearConnectedStatus(final String state) {
 		StatusMessage.send(ctxt.get(),
 				StatusMessage.getNew().setSSID(StatusMessage.EMPTY)
 						.setSignal(0).setStatus(state));
 	}
 
 	private void checkSignal(final Context context) {
 		int signal = getWifiManager(context).getConnectionInfo().getRssi();
 		if (statNotifCheck()) {
 			StatusMessage m = new StatusMessage().setSSID(getSSID());
 			m.setSignal(WifiManager.calculateSignalLevel(signal, 5));
 			StatusMessage.send(context, m);
 		}
 
 		if (_signalCheckTime < System.currentTimeMillis()
 				&& Math.abs(signal) > Math.abs(getSignalThreshold(context))) {
 			notifyWrap(context, context.getString(R.string.signal_poor));
 			getWifiManager(ctxt.get()).startScan();
 			_signalhopping = true;
 			_signalCheckTime = System.currentTimeMillis()
 					+ SIGNAL_CHECK_INTERVAL;
 		}
 		log(context,
 				(new StringBuilder(context.getString(R.string.current_dbm))
 						.append(String.valueOf(signal))).toString());
 	}
 
 	private static int getSignalThreshold(final Context context) {
 		int detected = DEFAULT_DBM_FLOOR;
 		try {
 			detected = Integer.valueOf(PrefUtil.readString(context,
 					context.getString(R.string.dbmfloor_key)));
 		} catch (NumberFormatException e) {
 			/*
 			 * pref is null, that's ok we have the default
 			 */
 		}
 		return detected;
 	}
 
 	private void connectToAP(final Context context, final String ssid) {
 
 		if (!getWifiManager(ctxt.get()).isWifiEnabled())
 			return;
 		/*
 		 * Back to explicit connection
 		 */
 		int n = getNetworkfromSSID(context, ssid);
 
 		if (n == -1)
 			return;
 
 		WifiConfiguration target = getNetworkByNID(context, n);
 		/*
 		 * Create sparse WifiConfiguration with details of desired connectee
 		 */
 		connectee = new WFConfig();
 		connectee.wificonfig = target;
 		target.status = WifiConfiguration.Status.CURRENT;
 		getWifiManager(context).updateNetwork(target);
 		getWifiManager(context).enableNetwork(target.networkId, false);
 		getWifiManager(context).disconnect();
 		/*
 		 * Remove all posts to handler
 		 */
 		clearHandler();
 		/*
 		 * Connect
 		 */
 		getWifiManager(ctxt.get()).enableNetwork(
 				connectee.wificonfig.networkId, true);
 
 		log(context,
 				new StringBuilder(context
 						.getString(R.string.connecting_to_network)).append(
 						connectee.wificonfig.SSID).toString());
 	}
 
 	private int connectToBest(final Context context) {
 		/*
 		 * Make sure knownbysignal is populated first
 		 */
 		if (knownbysignal.isEmpty()) {
 			log(context, R.string.signalhop_no_result);
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
 						log(context, R.string.connection_threshold_exceeded);
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
 		WifiConfiguration network = getNetworkByNID(context, n);
 		if (network == null)
 			return;
 		if (network.priority > -1) {
 			network.priority--;
 			getWifiManager(context).updateNetwork(network);
 
 			if (PrefUtil.getFlag(Pref.LOG_KEY)) {
 				StringBuilder out = new StringBuilder(
 						(context.getString(R.string.demoting_network)));
 				out.append(network.SSID);
 				out.append(context.getString(R.string._to_));
 				out.append(String.valueOf(network.priority));
 				log(context, out.toString());
 			}
 		} else {
 			log(context,
 					new StringBuilder(context
 							.getString(R.string.network_at_priority_floor))
 							.append(network.SSID).toString());
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
 			handleNetworkAction(data);
 		else if (iAction.equals(CONNECTINTENT))
 			handleConnectIntent(context, data);
 		else if (iAction.equals(REASSOCIATE_INTENT))
 			handleReassociateEvent();
 		else if (iAction.equals(SLEEPCHECKINTENT)) {
 			handlerWrapper(rSleepcheck, REALLYSHORTWAIT);
 		} else
 			log(context, (iAction.toString()));
 
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
 				log(context,
 						(context.getString(R.string.reenablenetwork) + wfresult.SSID));
 
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
 			 * NetworkInfo can return null
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
 
 		if (self.get().wifistate) {
 			if (log)
 				log(context, R.string.wifi_is_enabled);
 		} else {
 			if (log)
 				log(context, R.string.wifi_is_disabled);
 		}
 		return self.get().wifistate;
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
 		List<ScanResult> scanResults = getWifiManager(ctxt.get())
 				.getScanResults();
 		/*
 		 * Catch null if scan results fires after wifi disabled or while wifi is
 		 * in intermediate state
 		 */
 		if (scanResults == null) {
 			log(context, R.string.null_scan_results);
 			return NULLVAL;
 		}
 		/*
 		 * Known networks from supplicant.
 		 */
 		List<WifiConfiguration> wifiConfigs = getWifiManager(ctxt.get())
 				.getConfiguredNetworks();
 		/*
 		 * Iterate the known networks over the scan results, adding found known
 		 * networks.
 		 */
 
 		log(context, R.string.parsing_scan_results);
 
 		int index;
 		for (ScanResult sResult : scanResults) {
 			/*
 			 * Look for scan result in our known list
 			 */
 			index = containsSSID(sResult.SSID, wifiConfigs);
 			if (index > -1) {
 				WifiConfiguration wfResult = getNetworkByNID(context, index);
 				/*
 				 * Ignore if disabled
 				 */
 				if (PrefUtil.getNetworkState(context, wfResult.networkId)) {
 					/*
 					 * Log network
 					 */
 					if (PrefUtil.getFlag(Pref.LOG_KEY)) {
 						logScanResult(context, sResult, wfResult);
 					}
 					/*
 					 * Add result to knownbysignal Using containsBSSID to avoid
 					 * dupes if results bugged
 					 */
 					if (!containsBSSID(sResult.BSSID, self.get().knownbysignal))
 						self.get().knownbysignal.add(new WFConfig(sResult,
 								wfResult));
 					else {
 						/*
 						 * Update signal level
 						 */
 						for (WFConfig config : self.get().knownbysignal) {
 							if (config.wificonfig.BSSID.equals(sResult.BSSID)) {
 								self.get().knownbysignal
 										.get(self.get().knownbysignal
 												.indexOf(config)).level = sResult.level;
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
 		for (WFConfig network : self.get().knownbysignal) {
 			if (!scancontainsBSSID(network.wificonfig.BSSID, scanResults))
 				/*
 				 * Mark for removal
 				 */
 				toremove.add(network);
 		}
 
 		if (!toremove.isEmpty()) {
 			for (WFConfig marked : toremove) {
 				self.get().knownbysignal.remove(marked);
 			}
 		}
 		pruneKnown(wifiConfigs);
 		log(context,
 				new StringBuilder(context.getString(R.string.number_of_known))
 						.append(String.valueOf(self.get().knownbysignal.size()))
 						.toString());
 		/*
 		 * Sort by ScanResult.level which is signal
 		 */
 		Collections.sort(self.get().knownbysignal, new SortBySignal());
 
 		return self.get().knownbysignal.size();
 	}
 
 	private static void pruneKnown(final List<WifiConfiguration> configs) {
 		List<WFConfig> toremove = new ArrayList<WFConfig>();
 		for (WFConfig w : self.get().knownbysignal) {
 			boolean found = false;
 			for (WifiConfiguration c : configs) {
 				if (c.SSID.equals(w.wificonfig.SSID)) {
 					found = true;
 					break;
 				}
 			}
 			if (!found)
 				toremove.add(w);
 		}
 		for (WFConfig w2 : toremove) {
 			self.get().knownbysignal.remove(w2);
 		}
 	}
 
 	private static int getNetworkID() {
 		WifiInfo wi = getWifiManager(ctxt.get()).getConnectionInfo();
 		if (wi != null)
 			return wi.getNetworkId();
 		else
 			return -1;
 	}
 
 	private static WifiConfiguration getNetworkByNID(Context context,
 			final int network) {
 		List<WifiConfiguration> configs = getWifiManager(context)
 				.getConfiguredNetworks();
		for (WifiConfiguration w : configs) {
			if (w.networkId == network)
				return w;
 		}
 		return null;
 	}
 
 	private static String getSSID() {
 		String s = (getWifiManager(ctxt.get()).getConnectionInfo().getSSID());
 		if (s != null)
 			return s;
 		else
 			return ("    ");
 	}
 
 	private static SupplicantState getSupplicantState() {
 		WifiInfo i = getWifiManager(ctxt.get()).getConnectionInfo();
 		if (i != null)
 			return i.getSupplicantState();
 		else
 			return SupplicantState.INVALID;
 	}
 
 	private static String getSupplicantStateString(final SupplicantState sstate) {
 		if (SupplicantState.isValidState(sstate))
 			return (sstate.name());
 		else
 			return (SSTATE_INVALID);
 	}
 
 	public static WifiManager getWifiManager(final Context context) {
 		/*
 		 * Cache WifiManager
 		 */
 		if (wm_ == null) {
 			wm_ = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 		}
 		return wm_;
 	}
 
 	private void handleConnect() {
 		if (connectee.wificonfig.SSID.contains(getSSID().toString())) {
 			log(ctxt.get(),
 					new StringBuilder(ctxt.get().getString(
 							R.string.connected_to_network)).append(
 							connectee.wificonfig.SSID).toString());
 		} else {
 			log(ctxt.get(), R.string.connect_failed);
 
 			if (supplicantInterruptCheck(ctxt.get()))
 				toggleWifi();
 			else
 				return;
 		}
 		connectee = null;
 	}
 
 	private void handleConnectIntent(Context context, Bundle data) {
 		connectToAP(ctxt.get(), data.getString(NETWORKNAME));
 	}
 
 	private void handleNetworkAction(final Bundle data) {
 		NetworkInfo info = data.getParcelable(WifiManager.EXTRA_NETWORK_INFO);
 		/*
 		 * This action means network connectivty has changed but, we only want
 		 * to run this code for wifi
 		 */
 		if (info.getType() == ConnectivityManager.TYPE_WIFI) {
 			if (info.getState().equals(NetworkInfo.State.CONNECTED))
 				onNetworkConnected();
 			else if (info.getState().equals(NetworkInfo.State.DISCONNECTED)
 					&& !info.isAvailable())
 				clearConnectedStatus(ctxt.get().getString(
 						R.string.wifi_is_disabled));
 		}
 	}
 
 	private void handleReassociateEvent() {
 		if (getNetworkID() != -1) {
 			getWifiManager(ctxt.get()).reassociate();
 			log(ctxt.get(), R.string.repairing);
 		} else
 			NotifUtil.showToast(ctxt.get(), R.string.not_connected);
 	}
 
 	private static void logScanResult(final Context context,
 			final ScanResult sResult, final WifiConfiguration wfResult) {
 		StringBuilder out = new StringBuilder(
 				context.getString(R.string.found_ssid));
 		out.append(sResult.SSID);
 		out.append(NEWLINE);
 		out.append(context.getString(R.string.capabilities));
 		out.append(sResult.capabilities);
 		out.append(NEWLINE);
 		out.append(context.getString(R.string.signal_level));
 		out.append(String.valueOf(sResult.level));
 		out.append(NEWLINE);
 		out.append(context.getString(R.string.priority));
 		out.append(String.valueOf(wfResult.priority));
 		log(context, out.toString());
 	}
 
 	private static boolean networkUp(final Context context) {
 		/*
 		 * hostup.getHostup does all the heavy lifting
 		 */
 		log(context, R.string.network_check);
 
 		/*
 		 * Launches ICMP/HTTP HEAD check threads which compete for successful
 		 * state return.
 		 * 
 		 * If we fail the first check, try again with hostup default to be sure
 		 */
 		HostMessage out = hostup.getHostup(REACHABLE, context,
 				self.get().accesspointIP);
 		log(context, out.status.toString());
 		if (!out.state) {
 			/*
 			 * Try #2
 			 */
 			out = hostup.getHostup(REACHABLE, context, null);
 			log(context, out.status.toString());
 		}
 		if (!out.state)
 			return false;
 		else
 			return true;
 	}
 
 	private static void log(final Context c, final int message) {
 		/*
 		 * handle live logging fragment
 		 */
 		if (PrefUtil.getFlag(Pref.LOG_KEY)
 				|| PrefUtil.readBoolean(c, LogFragment.HAS_LOGFRAGMENT))
 			log(c, c.getString(message));
 	}
 
 	private static void log(final Context c, final String message) {
 		/*
 		 * handle live logging fragment
 		 */
 		if (PrefUtil.readBoolean(c, LogFragment.HAS_LOGFRAGMENT)) {
 			Intent i = new Intent(LogFragment.LOG_MESSAGE_INTENT);
 			i.putExtra(LogFragment.LOG_MESSAGE, message);
 			BroadcastHelper.sendBroadcast(c, i, true);
 		}
 
 		if (PrefUtil.getFlag(Pref.LOG_KEY))
 			LogService.log(c, appname, message);
 	}
 
 	@SuppressWarnings("deprecation")
 	private static void icmpCache(final Context context) {
 		/*
 		 * Caches DHCP gateway IP for ICMP check
 		 */
 		DhcpInfo info = getWifiManager(ctxt.get()).getDhcpInfo();
 		self.get().accesspointIP = (Formatter.formatIpAddress(info.gateway));
 		log(context, new StringBuilder(context.getString(R.string.cached_ip))
 				.append(self.get().accesspointIP).toString());
 	}
 
 	private static void logBestNetwork(final Context context,
 			final WFConfig best) {
 
 		if (PrefUtil.getFlag(Pref.LOG_KEY)) {
 			StringBuilder output = new StringBuilder(
 					context.getString(R.string.best_signal_ssid));
 			output.append(best.wificonfig.SSID);
 			output.append(COLON);
 			output.append(best.wificonfig.BSSID);
 			output.append(NEWLINE);
 			output.append(context.getString(R.string.signal_level));
 			output.append(String.valueOf(best.level));
 			output.append(NEWLINE);
 			output.append(context.getString(R.string.nid));
 			output.append(String.valueOf(best.wificonfig.networkId));
 			log(context, output.toString());
 		}
 	}
 
 	private static void notifyWrap(final Context context, final String string) {
 
 		if (PrefUtil.getFlag(Pref.NOTIF_KEY)) {
 			NotifUtil.show(context,
 					context.getString(R.string.wifi_connection_problem)
 							+ string, string, ERR_NOTIF, PendingIntent
 							.getActivity(context, 0, new Intent(context,
 									WifiFixerActivity.class), 0));
 		}
 	}
 
 	private static void checkAssociateState() {
 		supplicant_associating++;
 		if (supplicant_associating > SUPPLICANT_ASSOC_THRESHOLD) {
 			/*
 			 * Reset supplicant, it's stuck
 			 */
 			toggleWifi();
 			supplicant_associating = 0;
 			log(ctxt.get(), R.string.supplicant_associate_threshold_exceeded);
 		} else
 			self.get().handlerWrapper(rAssocWatchDog, SHORTWAIT);
 	}
 
 	private void checkWifi() {
 		if (getIsSupplicantConnected(ctxt.get())) {
 			if (!screenstate)
 				wakelock.lock(true);
 			/*
 			 * Starts network check AsyncTask
 			 */
 			_nethandler.get().post(NetCheckRunnable);
 		} else {
 			/*
 			 * Make sure scan happens in a reasonable amount of time
 			 */
 			handlerWrapper(rScanWatchDog, SHORTWAIT);
 		}
 	}
 
 	protected void handleNetworkResult(final boolean state) {
 		if (!state) {
 			_connected = false;
 			shouldrepair = true;
 			wifiRepair();
 		}
 		wakelock.lock(false);
 	}
 
 	public void cleanup() {
 		_nethandler.getLooper().quit();
 		ctxt.get().unregisterReceiver(receiver);
 		clearQueue();
 		clearHandler();
 		setStatNotif(false);
 		_statusdispatcher.unregister();
 		wifilock.lock(false);
 		hostup.finish();
 	}
 
 	private void clearQueue() {
 		pendingscan = false;
 		pendingreconnect = false;
 		shouldrepair = false;
 	}
 
 	private void prepareConnect() {
 		/*
 		 * Flush queue if connected
 		 * 
 		 * Also clear any error notifications
 		 */
 		if (connectee != null) {
 			handleConnect();
 		}
 		clearQueue();
 		pendingscan = false;
 		pendingreconnect = false;
 		lastAP = getNetworkID();
 	}
 
 	/*
 	 * Lets us control duplicate posts and odd handler behavior when screen is
 	 * off
 	 */
 	private boolean handlerWrapper(Runnable r) {
 		if (handler.hasMessages(r.hashCode()))
 			return false;
 		Message out = Message.obtain(handler, r);
 		out.what = r.hashCode();
 		if (screenstate)
 			return handler.sendMessage(out);
 		else
 			return handler.sendMessageDelayed(out, REALLYSHORTWAIT);
 	}
 
 	private boolean handlerWrapper(Runnable r, final long delay) {
 		if (handler.hasMessages(r.hashCode()))
 			return false;
 		Message out = Message.obtain(handler, r);
 		out.what = r.hashCode();
 		return handler.sendMessageDelayed(out, delay);
 	}
 
 	private void handleScanResults() {
 		/*
 		 * Reset timer: we've successfully scanned
 		 */
 		_scantimer.start();
 		/*
 		 * Scan results received. Remove Scan Watchdog.
 		 */
 		clearMessage(rScanWatchDog);
 		/*
 		 * Sanity check
 		 */
 		if (!getWifiManager(ctxt.get()).isWifiEnabled())
 			return;
 		else if (_signalhopping) {
 			_signalhopping = false;
 			handlerWrapper(rSignalhop);
 		} else if (!pendingscan) {
 			if (getIsOnWifi(ctxt.get())) {
 				/*
 				 * Signalhop code out
 				 */
 				return;
 			} else {
 				/*
 				 * Parse scan and connect if any known networks discovered
 				 */
 				if (supplicantInterruptCheck(ctxt.get())) {
 					if (getKnownAPsBySignal(ctxt.get()) > 0)
 						connectToBest(ctxt.get());
 				}
 			}
 		} else if (!pendingreconnect) {
 			/*
 			 * Service called the scan: dispatch appropriate runnable
 			 */
 			pendingscan = false;
 			handlerWrapper(rRepair);
 			log(ctxt.get(), R.string.repairhandler);
 		} else {
 			pendingscan = false;
 			handlerWrapper(rReconnect);
 			log(ctxt.get(), R.string.reconnecthandler);
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
 		if (supplicantPatternCheck())
 			return;
 
 		/*
 		 * Check for auth error
 		 */
 		if (data.containsKey(WifiManager.EXTRA_SUPPLICANT_ERROR))
 			NotifUtil.showToast(ctxt.get(), R.string.authentication_error);
 
 		/*
 		 * Supplicant state-specific logic
 		 */
 		handleSupplicantState(sState);
 	}
 
 	private static boolean supplicantPatternCheck() {
 		self.get()._supplicantFifo.add(self.get().lastSupplicantState);
 		List<SupplicantState> pattern = self.get()._supplicantFifo
 				.containsPatterns(SupplicantPatterns.SCAN_BOUNCE_CLUSTER);
 		if (pattern != null) {
 			log(ctxt.get(), pattern.getClass().getSimpleName());
 			self.get()._supplicantFifo.clear();
 			toggleWifi();
 			return true;
 		} else
 			return false;
 	}
 
 	private void handleSupplicantState(final SupplicantState sState) {
 		if (!getWifiManager(ctxt.get()).isWifiEnabled())
 			return;
 		/*
 		 * Disconnect check
 		 */
 		if (_connected && !sState.equals(SupplicantState.COMPLETED)
 				&& !sState.equals(SupplicantState.FOUR_WAY_HANDSHAKE)
 				&& !sState.equals(SupplicantState.GROUP_HANDSHAKE)
 				&& !sState.equals(SupplicantState.SCANNING))
 			onNetworkDisconnected();
 		/*
 		 * Check for ASSOCIATING bug but first clear check if not ASSOCIATING
 		 */
 		if (!(sState.name().equals(SSTATE_ASSOCIATING))) {
 			supplicant_associating = 0;
 			clearMessage(rAssocWatchDog);
 		}
 		/*
 		 * Status notification updating supplicant state
 		 */
 		if (statNotifCheck()) {
 			StatusMessage.send(ctxt.get(),
 					new StatusMessage().setStatus(sState.name()));
 		}
 		/*
 		 * Log new supplicant state
 		 */
 		if (PrefUtil.getFlag(Pref.LOG_KEY) && screenstate)
 			log(ctxt.get(),
 					new StringBuilder(ctxt.get().getString(
 							R.string.supplicant_state)).append(
 							String.valueOf(sState)).toString());
 		/*
 		 * Supplicant State triggers
 		 */
 		if (sState.name().equals(SSTATE_ASSOCIATED))
 			prepareConnect();
 		else if (sState.name().equals(SSTATE_INVALID))
 			supplicantFix();
 		else if (sState.name().equals(SSTATE_ASSOCIATING)) {
 			handlerWrapper(rAssocWatchDog, SHORTWAIT);
 			if (!_connected)
 				onNetworkConnecting();
 		}
 	}
 
 	private void handleWifiState(final Bundle data) {
 		// What kind of state change is it?
 		int state = data.getInt(WifiManager.EXTRA_WIFI_STATE,
 				WifiManager.WIFI_STATE_UNKNOWN);
 		switch (state) {
 		case WifiManager.WIFI_STATE_ENABLED:
 			log(ctxt.get(), R.string.wifi_state_enabled);
 			onWifiEnabled();
 			break;
 		case WifiManager.WIFI_STATE_ENABLING:
 			log(ctxt.get(), R.string.wifi_state_enabling);
 			break;
 		case WifiManager.WIFI_STATE_DISABLED:
 			log(ctxt.get(), R.string.wifi_state_disabled);
 			onWifiDisabled();
 			break;
 		case WifiManager.WIFI_STATE_DISABLING:
 			log(ctxt.get(), R.string.wifi_state_disabling);
 			break;
 		case WifiManager.WIFI_STATE_UNKNOWN:
 			log(ctxt.get(), R.string.wifi_state_unknown);
 			break;
 		}
 	}
 
 	private static void n1Fix() {
 		/*
 		 * Nexus One Sleep Fix duplicating widget function
 		 */
 		if (getWifiManager(ctxt.get()).isWifiEnabled()
 				&& !self.get().screenstate) {
 			toggleWifi();
 		}
 	}
 
 	private void onNetworkDisconnected() {
 		_connected = false;
 		clearConnectedStatus((ctxt.get().getString(R.string.disconnected)));
 	}
 
 	private void onNetworkConnecting() {
 
 		/*
 		 * Check for Android 2.x disabled network bug WifiConfiguration state
 		 * won't match stored state
 		 * 
 		 * Checking onConnecting because auth may complete but IP allocation may
 		 * not, want to bounce to another network if that's the case
 		 */
 		fixDisabledNetworks(ctxt.get(), getWifiManager(ctxt.get())
 				.getConfiguredNetworks());
 		handlerWrapper(rDemoter, CWDOG_DELAY);
 	}
 
 	private void onNetworkConnected() {
 		/*
 		 * Disable watchdog, we've connected
 		 */
 		clearMessage(rDemoter);
 		/*
 		 * If this was a bad network before, it's good now.
 		 */
 		int n = getNetworkID();
 		restoreNetworkPriority(ctxt.get(), n);
 		icmpCache(ctxt.get());
 		_connected = true;
 		StatusMessage.send(ctxt.get(), new StatusMessage().setSSID(getSSID()));
 		_statusdispatcher.refreshWidget(new StatusMessage().setSSID(getSSID()));
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
 		sleepCheck(!screenstate);
 
 		/*
 		 * Clear any error/new network notifications
 		 */
 		NotifUtil.cancel(ctxt.get(), ERR_NOTIF);
 		/*
 		 * Log Non-Managed network
 		 */
 		if (!shouldManage(ctxt.get()))
 			log(ctxt.get(),
 					new StringBuilder(ctxt.get().getString(
 							R.string.not_managing_network)).append(getSSID())
 							.toString());
 
 		/*
 		 * Log connection
 		 */
 		log(ctxt.get(),
 				new StringBuilder(ctxt.get().getString(
 						R.string.connected_to_network)).append(getSSID())
 						.toString());
 	}
 
 	private void onScreenOff() {
 		/*
 		 * Clear StatusDispatcher
 		 */
 		_statusdispatcher.clearQueue();
 
 		/*
 		 * Disable Sleep check
 		 */
 		if (PrefUtil.getFlag(Pref.SCREEN_KEY))
 			sleepCheck(true);
 		else {
 
 			if (PrefUtil.getFlag(Pref.WIFILOCK_KEY))
 				wifilock.lock(false);
 		}
 
 		/*
 		 * Schedule N1 fix
 		 */
 		if (PrefUtil.getFlag(Pref.N1FIX2_KEY)) {
 			handlerWrapper(rN1Fix, REACHABLE);
 			log(ctxt.get(), R.string.scheduling_n1_fix);
 		}
 		log(ctxt.get(), R.string.screen_off_handler);
 	}
 
 	private void onScreenOn() {
 		/*
 		 * Re-enable lock if it's off
 		 */
 		if (PrefUtil.getFlag(Pref.WIFILOCK_KEY))
 			wifilock.lock(true);
 
 		sleepCheck(false);
 		log(ctxt.get(), R.string.screen_on_handler);
 
 		/*
 		 * Notify current state on resume
 		 */
 		if (PrefUtil.getFlag(Pref.STATENOT_KEY) && statNotifCheck())
 			setStatNotif(true);
 		_statusdispatcher.refreshWidget(null);
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
 		clearConnectedStatus(ctxt.get().getString(R.string.wifi_is_disabled));
 		_statusdispatcher.refreshWidget(new StatusMessage().setStatus(ctxt
 				.get().getString(R.string.wifi_is_disabled)));
 		if (PrefUtil.getFlag(Pref.LOG_KEY))
 			LogService.setLogTS(ctxt.get(), false, 0);
 	}
 
 	private void onWifiEnabled() {
 		clearConnectedStatus(ctxt.get().getString(R.string.wifi_is_enabled));
 		wifistate = true;
 		handlerWrapper(rMain, LOOPWAIT);
 
 		if (PrefUtil.getFlag(Pref.STATENOT_KEY) && screenstate)
 			setStatNotif(true);
 
 		if (PrefUtil.getFlag(Pref.LOG_KEY))
 			LogService.setLogTS(ctxt.get(), true, SHORTWAIT);
 
 		/*
 		 * Remove wifi state lock
 		 */
 		if (PrefUtil.readBoolean(ctxt.get(), PrefConstants.WIFI_STATE_LOCK))
 			PrefUtil.writeBoolean(ctxt.get(), PrefConstants.WIFI_STATE_LOCK,
 					false);
 	}
 
 	public static boolean removeNetwork(final Context context, final int network) {
 		boolean state = getWifiManager(context).removeNetwork(network);
 		getWifiManager(context).saveConfiguration();
 		return state;
 	}
 
 	private static void restoreNetworkPriority(final Context context,
 			final int n) {
 		WifiConfiguration network = getNetworkByNID(context, n);
 		if (network != null) {
 			network.priority = 2;
 			getWifiManager(context).updateNetwork(network);
 		}
 	}
 
 	private static boolean scancontainsBSSID(final String bssid,
 			final List<ScanResult> results) {
 		for (ScanResult sResult : results) {
 			if (sResult.BSSID.equals(bssid))
 				return true;
 		}
 		return false;
 	}
 
 	public static void scanwatchdog() {
 		if (getWifiManager(ctxt.get()).isWifiEnabled()
 				&& !getIsOnWifi(ctxt.get())
 				&& self.get()._scantimer.getElapsed() > SCAN_WATCHDOG_DELAY) {
 			/*
 			 * Reset and log
 			 */
 			toggleWifi();
 			StringBuilder scanfail = new StringBuilder(ctxt.get().getString(
 					R.string.scan_failed));
 			scanfail.append(":");
 			scanfail.append(String.valueOf(self.get()._scantimer.getElapsed()));
 			scanfail.append(ctxt.get().getString(R.string.ms));
 			log(ctxt.get(), scanfail.toString());
 		}
 		if (self.get().screenstate)
 			self.get().handlerWrapper(rScan, NORMAL_SCAN_DELAY);
 		else
 			self.get().handlerWrapper(rScan, SLEEPWAIT);
 	}
 
 	protected void setStatNotif(final boolean state) {
 		if (!getWifiManager(ctxt.get()).isWifiEnabled()
 				|| !getIsOnWifi(ctxt.get()))
 			clearConnectedStatus(ctxt.get().getString(R.string.not_connected));
 		StatusMessage sm = StatusMessage.getNew().setStatus(
 				getSupplicantStateString(getSupplicantState()));
 		if (state)
 			sm.setShow(1);
 		else
 			sm.setShow(-1);
 
 		StatusMessage.send(ctxt.get(), sm);
 	}
 
 	private static boolean shouldManage(final Context ctx) {
 		if (PrefUtil.readManagedState(ctx, getNetworkID()))
 			return false;
 		else
 			return true;
 	}
 
 	private static boolean statNotifCheck() {
 		if (self.get().screenstate
 				&& getWifiManager(ctxt.get()).isWifiEnabled())
 			return true;
 		else
 			return false;
 	}
 
 	private void signalHop() {
 		/*
 		 * Connect To best will always find best signal/availability
 		 */
 		if (!getisWifiEnabled(ctxt.get(), false))
 			return;
 		/*
 		 * Switch to best
 		 */
 		int bestap = NULLVAL;
 		int numKnown = getKnownAPsBySignal(ctxt.get());
 		if (numKnown > 1) {
 			bestap = connectToBest(ctxt.get());
 			log(ctxt.get(),
 					new StringBuilder(ctxt.get().getString(R.string.hopping))
 							.append(String.valueOf(bestap)).toString());
 			log(ctxt.get(), new StringBuilder(ctxt.get()
 					.getString(R.string.nid)).append(String.valueOf(lastAP))
 					.toString());
 		} else {
 			log(ctxt.get(), R.string.signalhop_no_result);
 			wifiRepair();
 		}
 	}
 
 	private void sleepCheck(final boolean state) {
 		Intent i = new Intent(SLEEPCHECKINTENT);
 		PendingIntent p = PendingIntent.getBroadcast(ctxt.get(), 0, i,
 				PendingIntent.FLAG_CANCEL_CURRENT);
 		if (state && getisWifiEnabled(ctxt.get(), false)) {
 			/*
 			 * Start sleep check Using alarm here because some devices seem to
 			 * not fire handlers in deep sleep.
 			 */
 			clearMessage(rMain);
 			ServiceAlarm.addAlarm(ctxt.get().getApplicationContext(),
 					SHORTWAIT, true, SLEEPWAIT, p);
 		} else {
 			/*
 			 * Screen is on, remove any posts
 			 */
 			ServiceAlarm.unsetAlarm(ctxt.get(), p);
 			clearMessage(rSleepcheck);
 			/*
 			 * Check state
 			 */
 			handlerWrapper(rMain, REALLYSHORTWAIT);
 		}
 	}
 
 	public void startScan(final boolean pending) {
 		pendingscan = pending;
 		// We want a wakelock during scan
 		if (!screenstate)
 			wakelock.lock(true);
 		if (getWifiManager(ctxt.get()).startScan()) {
 			log(ctxt.get(), R.string.initiating_scan);
 			getWifiManager(ctxt.get()).startScan();
 		} else {
 			/*
 			 * Reset supplicant, log
 			 */
 			toggleWifi();
 			log(ctxt.get(), R.string.scan_failed);
 		}
 		wakelock.lock(false);
 	}
 
 	private void supplicantFix() {
 		// Toggling wifi resets the supplicant
 		toggleWifi();
 
 		log(ctxt.get(), R.string.running_supplicant_fix);
 	}
 
 	private static boolean supplicantInterruptCheck(final Context context) {
 
 		SupplicantState sstate = getSupplicantState();
 		/*
 		 * First, make sure this won't interrupt anything
 		 */
 		if (sstate.name().equals(SSTATE_ASSOCIATING)
 				|| sstate.name().equals(SSTATE_ASSOCIATED)
 				|| sstate.equals(SupplicantState.COMPLETED)
 				|| sstate.equals(SupplicantState.GROUP_HANDSHAKE)
 				|| sstate.equals(SupplicantState.FOUR_WAY_HANDSHAKE))
 			return false;
 		else
 			return true;
 	}
 
 	private static void toggleWifi() {
 		/*
 		 * Send Toggle request to broadcastreceiver
 		 */
 		log(ctxt.get(), R.string.toggling_wifi);
 		ctxt.get().sendBroadcast(new Intent(WidgetReceiver.TOGGLE_WIFI));
 		self.get().clearConnectedStatus(
 				ctxt.get().getString(R.string.toggling_wifi));
 	}
 
 	private void wifiRepair() {
 		if (!shouldrepair)
 			return;
 
 		if (screenstate) {
 			/*
 			 * Start Wifi Task
 			 */
 			handlerWrapper(rWifiTask);
 			log(ctxt.get(), R.string.running_wifi_repair);
 		} else {
 			/*
 			 * if screen off, try wake lock then resubmit to handler
 			 */
 			wakelock.lock(true);
 			handlerWrapper(rWifiTask);
 			log(ctxt.get(), R.string.wifi_repair_post_failed);
 		}
 		shouldrepair = false;
 	}
 
 	public void wifiLock(final boolean state) {
 		wifilock.lock(state);
 	}
 
 	private static void restoreandReset(final Context context,
 			final WFConfig network) {
 		/*
 		 * Enable bugged disabled networks, reset
 		 */
 		fixDisabledNetworks(context, getWifiManager(context)
 				.getConfiguredNetworks());
 		toggleWifi();
 		connecting = 0;
 	}
 
 }
