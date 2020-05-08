 /*
  * Wifi Fixer for Android
  *     Copyright (C) 2010-2013  David Van de Ven
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see http://www.gnu.org/licenses
  */
 
 package org.wahtod.wififixer;
 
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.DhcpInfo;
 import android.net.NetworkInfo;
 import android.net.wifi.*;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.format.Formatter;
 import org.wahtod.wififixer.prefs.PrefConstants;
 import org.wahtod.wififixer.prefs.PrefConstants.Pref;
 import org.wahtod.wififixer.prefs.PrefUtil;
 import org.wahtod.wififixer.ui.LogFragment;
 import org.wahtod.wififixer.ui.MainActivity;
 import org.wahtod.wififixer.utility.*;
 import org.wahtod.wififixer.utility.ScreenStateDetector.OnScreenStateChangedListener;
 import org.wahtod.wififixer.widget.WidgetReceiver;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 /*
  * Handles all interaction 
  * with WifiManager
  *
  * "fix" part of Wifi Fixer.
  */
 public class WFMonitor implements OnScreenStateChangedListener {
     // Sleep Check Intent for Alarm
     public static final String SLEEPCHECKINTENT = "org.wahtod.wififixer.SLEEPCHECK";
     // Wifi Connect Intent
     public static final String CONNECTINTENT = "org.wahtod.wififixer.CONNECT";
     public static final String NETWORKNAME = "net#";
     // User Event Intent
     public static final String REASSOCIATE_INTENT = "org.wahtod.wififixer.USEREVENT";
     private static final int DEFAULT_DBM_FLOOR = -90;
     // IDs For notifications
     private static final int ERR_NOTIF = 7972;
     private static final int FIFO_LENGTH = 10;
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
     private static final int SCAN_WATCHDOG_DELAY = 25000;
     private static final int NORMAL_SCAN_DELAY = 20000;
     // various
     private static final int NULLVAL = -1;
     /*
      * Constants for mRepairLevel values
      */
     private static final int W_REASSOCIATE = 0;
     private static final int W_RECONNECT = 1;
     private static final int W_REPAIR = 2;
     private static final long SUPPLICANT_ASSOC_THRESHOLD = 15000;
     /*
      * Supplicant State triggers Have to use string because some SupplicantState
      * enums aren't available in some Android versions
      */
     private static final String SSTATE_ASSOCIATING = "ASSOCIATING";
     private static final String SSTATE_ASSOCIATED = "ASSOCIATED";
     private static final String SSTATE_INVALID = "INVALID";
     private static final int CONNECTING_THRESHOLD = 5;
     private static final long CWDOG_DELAY = 10000;
     private static final int SLEEP_CHECK_CHOKE = 60000;
     protected static WeakReference<Context> ctxt;
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
 
                 if (isUp)
                     mRepairLevel = W_REASSOCIATE;
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
             if (!PrefUtil.getWifiManager(ctxt.get()).isWifiEnabled()) {
                 log(ctxt.get(), R.string.wifi_off_aborting_reconnect);
                 return;
             }
             if (getKnownAPsBySignal(ctxt.get()) > 0
                     && self.get().connectToBest(ctxt.get()) != NULLVAL) {
                 pendingreconnect = false;
             } else {
                 mRepairLevel = W_REASSOCIATE;
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
             if (!PrefUtil.getWifiManager(ctxt.get()).isWifiEnabled()) {
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
                             && !PrefUtil.getWifiManager(ctxt.get()).pingSupplicant()) {
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
             switch (mRepairLevel) {
 
                 case W_REASSOCIATE:
                     // Let's try to reassociate first..
                     PrefUtil.getWifiManager(ctxt.get()).reassociate();
                     log(ctxt.get(), R.string.reassociating);
                     mRepairLevel++;
                     notifyWrap(ctxt.get(),
                             ctxt.get().getString(R.string.reassociating));
                     break;
 
                 case W_RECONNECT:
                     // Ok, now force reconnect..
                     PrefUtil.getWifiManager(ctxt.get()).reconnect();
                     log(ctxt.get(), R.string.reconnecting);
                     mRepairLevel++;
                     notifyWrap(ctxt.get(),
                             ctxt.get().getString(R.string.reconnecting));
                     break;
 
                 case W_REPAIR:
                     // Start Scan
                     PrefUtil.getWifiManager(ctxt.get()).disconnect();
                     self.get().handlerWrapper(rScanWatchDog, SHORTWAIT);
                 /*
                  * Reset state
 				 */
                     mRepairLevel = W_REASSOCIATE;
                     log(ctxt.get(), R.string.repairing);
                     notifyWrap(ctxt.get(), ctxt.get().getString(R.string.repairing));
                     break;
             }
             self.get().wakelock.lock(false);
             log(ctxt.get(),
                     new StringBuilder(ctxt.get().getString(
                             R.string.fix_algorithm)).append(mRepairLevel)
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
 
             self.get().clearQueue();
             /*
              * run the signal hop check
 			 */
             self.get().signalHop();
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
     protected static Runnable rDemoter = new Runnable() {
         @Override
         public void run() {
             int n = getNetworkID();
             if (n != -1)
                 demoteNetwork(ctxt.get(), n);
         }
     };
     // flags
     private static boolean shouldrepair = false;
     private static boolean pendingscan = false;
     private static boolean pendingreconnect = false;
     private static boolean repair_reset = false;
     private static boolean _connected = false;
     private static boolean _signalhopping = false;
     private static int lastAP = NULLVAL;
     private static volatile Hostup hostup;
     private static int mRepairLevel = W_REASSOCIATE;
     private static long _signalCheckTime;
     /*
      * For Supplicant ASSOCIATING bug
      */
     private static StopWatch _assoc_watchdog = new StopWatch();
     /*
      * For connectToAP sticking
      */
     private static int connecting = 0;
     private static WeakReference<WFMonitor> self;
     private static volatile Handler handler = new Handler();
     private static volatile ThreadHandler _nethandler;
     private static volatile boolean isUp;
     /*
      * For ongoing status notification, widget, and Status fragment
      */
     protected StatusDispatcher _statusdispatcher;
     boolean screenstate;
     private String accesspointIP;
     private WakeLock wakelock;
     private WifiLock wifilock;
     private WifiInfo mLastConnectedNetwork;
     /*
      * Supplicant State FIFO for pattern matching
      */
     private FifoList _supplicantFifo;
     // Last Scan
     private StopWatch _scantimer;
     private WFConfig connectee;
     private List<WFConfig> knownbysignal;
     private SupplicantState lastSupplicantState;
     private boolean wifistate;
     private BroadcastReceiver receiver = new BroadcastReceiver() {
         public void onReceive(Context context, Intent intent) {
             handleBroadcast(context, intent);
         }
     };
     /*
      * Demotes networks we fail to connect to for one reason or another
      */
     private BroadcastReceiver localreceiver = new BroadcastReceiver() {
         public void onReceive(Context context, Intent intent) {
             handleBroadcast(context, intent);
         }
     };
     private boolean mSleepCheckChoke = false;
     private Runnable SleepCheckChoke = new Runnable() {
         @Override
         public void run() {
             mSleepCheckChoke = false;
         }
     };
 
     public WFMonitor(final Context context) {
         _scantimer = new StopWatch();
         self = new WeakReference<WFMonitor>(this);
         _supplicantFifo = new FifoList(FIFO_LENGTH);
         _statusdispatcher = new StatusDispatcher(context, handler);
         ScreenStateDetector.setOnScreenStateChangedListener(this);
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
         wifistate = PrefUtil.getWifiManager(context).isWifiEnabled();
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
 		/*
 		 * Initialize WakeLock and WifiLock
 		 */
         wakelock = new WakeLock(context) {
 
             @Override
             public void onAcquire() {
                 LogService.log(ctxt.get(), R.string.acquiring_wake_lock);
                 super.onAcquire();
             }
 
             @Override
             public void onRelease() {
                 LogService.log(ctxt.get(), R.string.releasing_wake_lock);
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
         if (PrefUtil.readBoolean(context, Pref.STATENOT_KEY.key()))
             setStatNotif(true);
 
 		/*
 		 * Instantiate network checker
 		 */
         hostup = new Hostup(context);
         _nethandler = new ThreadHandler(
                 context.getString(R.string.netcheckthread));
     }
 
     private static int getSignalThreshold(Context context) {
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
 
     private static int containsSSID(String ssid,
                                     List<WifiConfiguration> wifiConfigs) {
         for (WifiConfiguration sResult : wifiConfigs) {
             if (StringUtil.removeQuotes(sResult.SSID).equals(ssid))
                 return sResult.networkId;
         }
         return -1;
     }
 
     private static boolean containsBSSID(String bssid,
                                          List<WFConfig> results) {
         for (WFConfig sResult : results) {
             if (sResult.wificonfig.BSSID.equals(bssid))
                 return true;
         }
         return false;
     }
 
     private static void demoteNetwork(Context context, int n) {
         WifiConfiguration network = getNetworkByNID(context, n);
         if (network == null)
             return;
         if (network.priority > -1) {
             network.priority--;
             PrefUtil.getWifiManager(context).updateNetwork(network);
 
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
 
     private static void fixDisabledNetworks(Context context,
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
                 PrefUtil.getWifiManager(context)
                         .enableNetwork(wfresult.networkId, false);
                 log(context,
                         (context.getString(R.string.reenablenetwork) + wfresult.SSID));
 
             }
         }
     }
 
     private static boolean getIsOnWifi(Context context) {
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
 
     private static boolean getIsSupplicantConnected(Context context) {
         SupplicantState sstate = getSupplicantState();
         if (sstate == null)
             return false;
         else return sstate == SupplicantState.ASSOCIATED
                 || sstate == SupplicantState.COMPLETED;
     }
 
     private static boolean getisWifiEnabled(Context context, boolean log) {
         return self.get().wifistate;
     }
 
     private static int getKnownAPsBySignal(Context context) {
 
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
         List<ScanResult> scanResults = PrefUtil.getWifiManager(ctxt.get())
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
         List<WifiConfiguration> wifiConfigs = PrefUtil.getWifiManager(ctxt.get())
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
 
     private static void pruneKnown(List<WifiConfiguration> configs) {
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
         WifiInfo wi = PrefUtil.getWifiManager(ctxt.get()).getConnectionInfo();
         if (wi != null)
             return wi.getNetworkId();
         else
             return -1;
     }
 
     private static WifiConfiguration getNetworkByNID(Context context,
                                                      int network) {
         List<WifiConfiguration> configs = PrefUtil.getWifiManager(context)
                 .getConfiguredNetworks();
         if (configs != null) {
             for (WifiConfiguration w : configs) {
                 if (w.networkId == network)
                     return w;
             }
         }
         return null;
     }
 
     private static String getSSID() {
         String s = (PrefUtil.getWifiManager(ctxt.get()).getConnectionInfo().getSSID());
         if (s != null)
             return s;
         else
             return ("    ");
     }
 
     private static SupplicantState getSupplicantState() {
         WifiInfo i = PrefUtil.getWifiManager(ctxt.get()).getConnectionInfo();
         if (i != null)
             return i.getSupplicantState();
         else
             return SupplicantState.INVALID;
     }
 
     private static String getSupplicantStateString(SupplicantState sstate) {
         if (SupplicantState.isValidState(sstate))
             return (sstate.name());
         else
             return (SSTATE_INVALID);
     }
 
     private static void logScanResult(Context context,
                                       ScanResult sResult, WifiConfiguration wfResult) {
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
 
     private static boolean networkUp(Context context) {
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
         log(context, out.status);
         if (!out.state) {
 			/*
 			 * Try #2
 			 */
             out = hostup.getHostup(REACHABLE, context, null);
             log(context, out.status);
         }
         return out.state;
     }
 
     private static void log(Context c, int message) {
 		/*
 		 * handle live logging fragment
 		 */
         if (PrefUtil.getFlag(Pref.LOG_KEY)
                 || PrefUtil.readBoolean(c, LogFragment.HAS_LOGFRAGMENT))
             log(c, c.getString(message));
     }
 
     private static void log(Context c, String message) {
 		/*
 		 * handle live logging fragment
 		 */
         if (PrefUtil.readBoolean(c, LogFragment.HAS_LOGFRAGMENT)) {
             Intent i = new Intent(LogFragment.LOG_MESSAGE_INTENT);
             i.putExtra(LogFragment.LOG_MESSAGE, message);
             BroadcastHelper.sendBroadcast(c, i, true);
         }
 
         if (PrefUtil.getFlag(Pref.LOG_KEY))
             LogService.log(c, message);
     }
 
     @SuppressWarnings("deprecation")
     private static void icmpCache(Context context) {
 		/*
 		 * Caches DHCP gateway IP for ICMP check
 		 */
         DhcpInfo info = PrefUtil.getWifiManager(ctxt.get()).getDhcpInfo();
         self.get().accesspointIP = (Formatter.formatIpAddress(info.gateway));
         log(context, new StringBuilder(context.getString(R.string.cached_ip))
                 .append(self.get().accesspointIP).toString());
     }
 
     private static void logBestNetwork(Context context,
                                        WFConfig best) {
 
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
 
     private static void notifyWrap(Context context, String string) {
 
         if (PrefUtil.getFlag(Pref.NOTIF_KEY)) {
             NotifUtil.show(context,
                     context.getString(R.string.wifi_connection_problem)
                             + string, string, ERR_NOTIF, PendingIntent
                     .getActivity(context, 0, new Intent(context,
                             MainActivity.class), 0));
         }
     }
 
     private static void checkAssociateState() {
 
         if (_assoc_watchdog.getElapsed() > SUPPLICANT_ASSOC_THRESHOLD) {
 			/*
 			 * Reset supplicant, it's stuck
 			 */
             toggleWifi();
             log(ctxt.get(), R.string.supplicant_associate_threshold_exceeded);
         } else
             self.get().handlerWrapper(rAssocWatchDog, SHORTWAIT);
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
 
     private static void n1Fix() {
 		/*
 		 * Nexus One Sleep Fix duplicating widget function
 		 */
         if (PrefUtil.getWifiManager(ctxt.get()).isWifiEnabled()
                 && !self.get().screenstate) {
             toggleWifi();
         }
     }
 
     public static boolean removeNetwork(Context context, int network) {
         boolean state = PrefUtil.getWifiManager(context).removeNetwork(network);
         PrefUtil.getWifiManager(context).saveConfiguration();
         return state;
     }
 
     private static void restoreNetworkPriority(Context context,
                                                int n) {
         WifiConfiguration network = getNetworkByNID(context, n);
         if (network != null) {
             network.priority = 2;
             PrefUtil.getWifiManager(context).updateNetwork(network);
         }
     }
 
     private static boolean scancontainsBSSID(String bssid,
                                              List<ScanResult> results) {
         for (ScanResult sResult : results) {
             if (sResult.BSSID.equals(bssid))
                 return true;
         }
         return false;
     }
 
     public static void scanwatchdog() {
         if (PrefUtil.getWifiManager(ctxt.get()).isWifiEnabled()
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
 
     private static boolean shouldManage(Context ctx) {
         return !PrefUtil.readManagedState(ctx, getNetworkID());
     }
 
     private static boolean statNotifCheck() {
         return self.get().screenstate
                 && PrefUtil.getWifiManager(ctxt.get()).isWifiEnabled();
     }
 
     private static boolean supplicantInterruptCheck(Context context) {
 
         SupplicantState sstate = getSupplicantState();
 		/*
 		 * First, make sure this won't interrupt anything
 		 */
         return !(sstate.name().equals(SSTATE_ASSOCIATING)
                 || sstate.name().equals(SSTATE_ASSOCIATED)
                 || sstate.equals(SupplicantState.COMPLETED)
                 || sstate.equals(SupplicantState.GROUP_HANDSHAKE)
                 || sstate.equals(SupplicantState.FOUR_WAY_HANDSHAKE));
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
 
     private static void restoreandReset(Context context,
                                         WFConfig network) {
 		/*
 		 * Enable bugged disabled networks, reset
 		 */
         fixDisabledNetworks(context, PrefUtil.getWifiManager(context)
                 .getConfiguredNetworks());
         toggleWifi();
         connecting = 0;
     }
 
     private void handleBroadcast(Context context, Intent intent) {
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
 
     private void clearConnectedStatus(String state) {
         StatusMessage.send(
                 ctxt.get(),
                 StatusMessage.getNew().setSSID(StatusMessage.EMPTY)
                         .setSignal(0).setStatus(state)
                         .setLinkSpeed("0")
                         .setShow(1));
     }
 
     private void checkSignal(Context context) {
         WifiInfo ci = PrefUtil.getWifiManager(context).getConnectionInfo();
         int signal = ci.getRssi();
 
         if (statNotifCheck()) {
             StatusMessage m = new StatusMessage().setSSID(getSSID());
             m.setSignal(WifiManager.calculateSignalLevel(signal, 5));
             m.setLinkSpeed(String.valueOf(ci.getLinkSpeed()));
             StatusMessage.send(context, m);
         }
 
         if (_signalCheckTime < System.currentTimeMillis()
                 && Math.abs(signal) > Math.abs(getSignalThreshold(context))) {
             notifyWrap(context, context.getString(R.string.signal_poor));
             PrefUtil.getWifiManager(ctxt.get()).startScan();
             _signalhopping = true;
             _signalCheckTime = System.currentTimeMillis()
                     + SIGNAL_CHECK_INTERVAL;
         }
         log(context,
                 (new StringBuilder(context.getString(R.string.current_dbm))
                         .append(String.valueOf(signal))).toString());
     }
 
     private void connectToAP(Context context, String ssid) {
 
         if (!PrefUtil.getWifiManager(ctxt.get()).isWifiEnabled())
             return;
 		/*
 		 * Back to explicit connection
 		 */
         int n = PrefUtil.getNidFromSsid(context, ssid);
 
         if (n == -1)
             return;
 
         WifiConfiguration target = getNetworkByNID(context, n);
 		/*
 		 * Create sparse WifiConfiguration with details of desired connectee
 		 */
         connectee = new WFConfig();
         connectee.wificonfig = target;
         target.status = WifiConfiguration.Status.CURRENT;
         PrefUtil.getWifiManager(context).updateNetwork(target);
         PrefUtil.getWifiManager(context).enableNetwork(target.networkId, false);
         PrefUtil.getWifiManager(context).disconnect();
 		/*
 		 * Remove all posts to handler
 		 */
         clearHandler();
 		/*
 		 * Connect
 		 */
         PrefUtil.getWifiManager(ctxt.get()).enableNetwork(
                 connectee.wificonfig.networkId, true);
 
         log(context,
                 new StringBuilder(context
                         .getString(R.string.connecting_to_network)).append(
                         connectee.wificonfig.SSID).toString());
     }
 
     private int connectToBest(Context context) {
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
 
     private void dispatchIntent(Context context, Bundle data) {
 
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
         else if (iAction.equals(SLEEPCHECKINTENT) && !mSleepCheckChoke) {
             /*
              * alarms get delayed due to deep sleep, causing a "storm" of
              * alarms: only respond to one every SLEEP_CHECK_CHOKE ms
              */
             handlerWrapper(rSleepcheck, REALLYSHORTWAIT);
             handlerWrapper(SleepCheckChoke, SLEEP_CHECK_CHOKE);
         } else
             log(context, (iAction.toString()));
 
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
 
     private void handleNetworkAction(Bundle data) {
         NetworkInfo info = data.getParcelable(WifiManager.EXTRA_NETWORK_INFO);
 		/*
 		 * This action means network connectivty has changed but, we only want
 		 * to run this code for wifi
 		 */
         if (info.getType() == ConnectivityManager.TYPE_WIFI) {
             if (info.getState().equals(NetworkInfo.State.CONNECTED))
                 onNetworkConnected(PrefUtil.getWifiManager(ctxt.get())
                         .getConnectionInfo());
             else if (info.getState().equals(NetworkInfo.State.DISCONNECTED))
                 onNetworkDisconnected();
         }
     }
 
     private void handleReassociateEvent() {
         if (getNetworkID() != -1) {
             PrefUtil.getWifiManager(ctxt.get()).reassociate();
             log(ctxt.get(), R.string.repairing);
         } else
             NotifUtil.showToast(ctxt.get(), R.string.not_connected);
     }
 
     private void checkWifi() {
         if (getIsSupplicantConnected(ctxt.get())) {
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
 
     protected void handleNetworkResult(boolean state) {
         if (!state) {
             wakelock.lock(true);
             _connected = false;
             shouldrepair = true;
             wifiRepair();
         }
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
 
     private boolean handlerWrapper(Runnable r, long delay) {
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
         if (!PrefUtil.getWifiManager(ctxt.get()).isWifiEnabled())
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
 
     private void handleSupplicantIntent(Bundle data) {
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
 
     private void handleSupplicantState(SupplicantState sState) {
         if (!PrefUtil.getWifiManager(ctxt.get()).isWifiEnabled())
             return;
 
 		/*
 		 * Check for ASSOCIATING bug but first clear check if not ASSOCIATING
 		 */
         if (!(sState.name().equals(SSTATE_ASSOCIATING))) {
             clearMessage(rAssocWatchDog);
         } else {
             _assoc_watchdog.start();
             handlerWrapper(rAssocWatchDog, SHORTWAIT);
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
         if (sState.equals(SupplicantState.INACTIVE)) {
 			/*
 			 * DHCP bug?
 			 */
             if (PrefUtil.getWatchdogPolicy(ctxt.get())) {
 
             }
 
         } else if (sState.name().equals(SSTATE_ASSOCIATED))
             prepareConnect();
         else if (sState.name().equals(SSTATE_INVALID))
             supplicantFix();
         else if (sState.name().equals(SSTATE_ASSOCIATING)) {
             handlerWrapper(rAssocWatchDog, SHORTWAIT);
             if (!_connected)
                 onNetworkConnecting();
         }
     }
 
     private void handleWifiState(Bundle data) {
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
 
     private void onNetworkDisconnected() {
         if (_connected) {
             log(ctxt.get(), mLastConnectedNetwork.getSSID()
                     + ctxt.get().getString(R.string.network_disconnected));
             _connected = false;
             if (getisWifiEnabled(ctxt.get(), false))
                 clearConnectedStatus((ctxt.get().getString(R.string.disconnected)));
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
         fixDisabledNetworks(ctxt.get(), PrefUtil.getWifiManager(ctxt.get())
                 .getConfiguredNetworks());
         handlerWrapper(rDemoter, CWDOG_DELAY);
         StatusMessage message = _statusdispatcher.getStatusMessage()
                 .setSSID(getSSID()).setStatus(ctxt.get(), R.string.connecting);
         StatusMessage.send(ctxt.get(), message);
         _statusdispatcher.refreshWidget(null);
     }
 
     private void onNetworkConnected(WifiInfo wifiInfo) {
         mLastConnectedNetwork = wifiInfo;
 		/*
 		 * Disable Demoter, we've connected
 		 */
         clearMessage(rDemoter);
 		/*
 		 * If this was a bad network before, it's good now.
 		 */
         int n = getNetworkID();
         restoreNetworkPriority(ctxt.get(), n);
         icmpCache(ctxt.get());
         _connected = true;
 
         StatusMessage.send(ctxt.get(), _statusdispatcher.getStatusMessage().setSSID(getSSID())
                 .setStatus(ctxt.get(), R.string.connected_to_network).setSignal(0));
         _statusdispatcher.refreshWidget(null);
 		/*
 		 * Make sure connectee is null
 		 */
         connectee = null;
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
         if (PrefUtil.getFlag(Pref.STATENOT_KEY)) {
             if (!getIsOnWifi(ctxt.get()))
                 clearConnectedStatus(getSupplicantStateString(getSupplicantState()));
             else
                 StatusMessage.send(ctxt.get(), _statusdispatcher.getStatusMessage().setShow(1));
 
             _statusdispatcher.refreshWidget(null);
         }
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
         _statusdispatcher.refreshWidget(null);
     }
 
     private void onWifiEnabled() {
         clearConnectedStatus(ctxt.get().getString(R.string.wifi_is_enabled));
         _statusdispatcher.refreshWidget(new StatusMessage().setStatus(ctxt
                 .get().getString(R.string.wifi_is_enabled)));
         wifistate = true;
         handlerWrapper(rMain, LOOPWAIT);
 
         if (PrefUtil.getFlag(Pref.STATENOT_KEY) && screenstate)
             setStatNotif(true);
 
 		/*
 		 * Remove wifi state lock
 		 */
         if (PrefUtil.readBoolean(ctxt.get(), PrefConstants.WIFI_STATE_LOCK))
             PrefUtil.writeBoolean(ctxt.get(), PrefConstants.WIFI_STATE_LOCK,
                     false);
     }
 
     protected void setStatNotif(boolean state) {
         if (!getisWifiEnabled(ctxt.get(), false)
                 || !getIsOnWifi(ctxt.get()))
             clearConnectedStatus(ctxt.get().getString(R.string.not_connected));
         StatusMessage sm = StatusMessage.getNew().setStatus(
                 getSupplicantStateString(getSupplicantState())).setSSID(getSSID());
         if (state) {
             sm.setShow(1);
             onNetworkConnected(PrefUtil.getWifiManager(ctxt.get()).getConnectionInfo());
         } else
             sm.setShow(-1);
 
         StatusMessage.send(ctxt.get(), sm);
         _statusdispatcher.refreshWidget(null);
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
             shouldrepair = true;
             wifiRepair();
         }
     }
 
     private void sleepCheck(boolean state) {
         Intent i = new Intent(SLEEPCHECKINTENT);
         PendingIntent p = PendingIntent.getBroadcast(ctxt.get(), 0, i,
                 PendingIntent.FLAG_CANCEL_CURRENT);
         if (state && getisWifiEnabled(ctxt.get(), false)) {
 			/*
 			 * Start sleep check Using alarm here because some devices seem to
 			 * not fire handlers in deep sleep.
 			 */
             clearMessage(rMain);
            if (!ServiceAlarm.alarmExists(ctxt.get()))
                 ServiceAlarm.addAlarm(ctxt.get(),
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
 
     public void startScan(boolean pending) {
         pendingscan = pending;
         if (PrefUtil.getWifiManager(ctxt.get()).startScan()) {
             log(ctxt.get(), R.string.initiating_scan);
         } else {
 			/*
 			 * Reset supplicant, log
 			 */
             toggleWifi();
             log(ctxt.get(), R.string.scan_failed);
         }
     }
 
     private void supplicantFix() {
         // Toggling wifi resets the supplicant
         toggleWifi();
 
         log(ctxt.get(), R.string.running_supplicant_fix);
     }
 
     private void wifiRepair() {
         if (!shouldrepair)
             return;
         handlerWrapper(rWifiTask);
         log(ctxt.get(), R.string.running_wifi_repair);
         shouldrepair = false;
     }
 
     public void wifiLock(boolean state) {
         wifilock.lock(state);
     }
 
     /*
          * Processes intent message
          */
     protected static class IntentRunnable implements Runnable {
         Bundle d;
 
         public IntentRunnable(Bundle b) {
             this.d = b;
         }
 
         @Override
         public void run() {
             self.get().dispatchIntent(ctxt.get(), d);
         }
     }
 
     /*
          * Signal Check and Network Result handler for AsyncTask postExcecute
          */
     protected static class PostNetCheckRunnable implements Runnable {
         Boolean state;
 
         public PostNetCheckRunnable(boolean b) {
             this.state = b;
         }
 
         @Override
         public void run() {
             self.get().checkSignal(ctxt.get());
             self.get().handleNetworkResult(state);
         }
     }
 }
