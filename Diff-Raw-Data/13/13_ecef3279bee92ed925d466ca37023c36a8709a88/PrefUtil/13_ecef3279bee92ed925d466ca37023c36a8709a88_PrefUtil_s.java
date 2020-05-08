 /*Copyright [2010-2012] [David Van de Ven]
 
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
 
 package org.wahtod.wififixer.prefs;
 
 import java.lang.ref.WeakReference;
 import java.util.HashMap;
 import java.util.List;
 
 import org.wahtod.wififixer.R;
 import org.wahtod.wififixer.legacy.EditorDetector;
 import org.wahtod.wififixer.prefs.PrefConstants.NetPref;
 import org.wahtod.wififixer.prefs.PrefConstants.Pref;
 import org.wahtod.wififixer.utility.LogService;
 
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.provider.Settings.SettingNotFoundException;
 
 public class PrefUtil extends Object {
 	private static WeakReference<PrefUtil> self;
 	private static final String COLON = ":";
 	/*
 	 * Intent Constants
 	 */
 	private static final String VALUE_CHANGED_ACTION = "ACTION.PREFS.VALUECHANGED";
 	private static final String NETVALUE_CHANGED_ACTION = "ACTION.NETPREFS.VALUECHANGED";
 	private static final String NET_KEY = "NETKEY";
 	private static final String DATA_KEY = "DATA_KEY";
 	public static final String VALUE_KEY = "VALUE_KEY";
 	private static final String INT_KEY = "INTKEY";
 	private static final String NETPREFIX = "n_";
 	/*
 	 * Actions for handler message bundles
 	 */
 	public static final String INTENT_ACTION = "INTENT_ACTION";
 
 	/*
 	 * Fields
 	 */
 	private static boolean[] keyVals;
 	private static WeakReference<Context> context;
 	private static WifiManager wm_;
 	private static HashMap<String, int[]> netprefs;
 
	private static BroadcastReceiver changeReceiver = new BroadcastReceiver() {
 		public void onReceive(final Context context, final Intent intent) {
 			String valuekey = intent.getStringExtra(VALUE_KEY);
 			Message message = receiverExecutor.obtainMessage();
 			Bundle data = new Bundle();
 			data.putString(VALUE_KEY, valuekey);
 			data.putString(INTENT_ACTION, intent.getAction());
 			if (intent.getAction().equals(VALUE_CHANGED_ACTION)) {
 				data.putBoolean(DATA_KEY,
 						intent.getBooleanExtra(DATA_KEY, false));
 			} else if (intent.getAction().equals(NETVALUE_CHANGED_ACTION)) {
 				data.putInt(INT_KEY, intent.getIntExtra(INT_KEY, 0));
 				data.putString(NET_KEY, intent.getStringExtra(NET_KEY));
 			}
 			message.setData(data);
 			receiverExecutor.sendMessage(message);
 		}
 	};
 
 	private static Handler receiverExecutor = new Handler() {
 		@Override
 		public void handleMessage(Message message) {
 			Bundle data = message.getData();
 			String action = data.getString(INTENT_ACTION);
 			if (action.equals(VALUE_CHANGED_ACTION))
 				self.get().handlePrefChange(
 						Pref.get(data.getString(VALUE_KEY)),
 						data.getBoolean(DATA_KEY));
 			else if (action.equals(NETVALUE_CHANGED_ACTION)) {
 				self.get().handleNetPrefChange(
 						NetPref.get(data.getString(VALUE_KEY)),
 						data.getString(NET_KEY), data.getInt(INT_KEY));
 			}
 		}
 	};
 
 	public static SharedPreferences getSharedPreferences(final Context c) {
 		return PreferenceManager.getDefaultSharedPreferences(c
 				.getApplicationContext());
 	}
 
 	public PrefUtil(final Context c) {
 		self = new WeakReference<PrefUtil>(this);
 		context = new WeakReference<Context>(c);
 		keyVals = new boolean[Pref.values().length];
 		IntentFilter filter = new IntentFilter(VALUE_CHANGED_ACTION);
 		filter.addAction(NETVALUE_CHANGED_ACTION);
 		c.registerReceiver(changeReceiver, filter);
 		netprefs = new HashMap<String, int[]>();
 	}
 
 	public void putnetPref(final NetPref pref, final String network,
 			final int value) {
 		int[] intTemp = netprefs.get(network);
 		if (intTemp == null) {
 			intTemp = new int[PrefConstants.NUMNETPREFS];
 		}
 		intTemp[pref.ordinal()] = value;
 
 		if (getFlag(Pref.LOG_KEY)) {
 			StringBuilder logstring = new StringBuilder();
 			logstring.append(pref.key());
 			logstring.append(COLON);
 			logstring.append(network);
 			logstring.append(COLON);
 			logstring.append(intTemp[pref.ordinal()]);
			LogService.log(context.get(), LogService.getLogTag(context.get()), logstring);
 		}
 
 		netprefs.put(network, intTemp);
 	}
 
 	public int getnetPref(final Context context, final NetPref pref,
 			final StringBuilder network) {
 		int ordinal = pref.ordinal();
 		if (!netprefs.containsKey(network)) {
 			int[] intarray = new int[PrefConstants.NUMNETPREFS];
 			intarray[ordinal] = readNetworkPref(context, network, pref);
 			netprefs.put(network.toString(), intarray);
 			return intarray[ordinal];
 		} else
 			return netprefs.get(network)[ordinal];
 	}
 
 	public void loadPrefs() {
 
 		/*
 		 * Pre-prefs load
 		 */
 		preLoad();
 		/*
 		 * Load
 		 */
 		for (Pref prefkey : Pref.values()) {
 			handleLoadPref(prefkey);
 		}
 		specialCase();
 	}
 
 	void handleLoadPref(final Pref p) {
 		setFlag(p, readBoolean(context.get(), p.key()));
 	}
 
 	void handlePrefChange(final Pref p, final boolean flagval) {
 		/*
 		 * Before value changes from loading
 		 */
 		preValChanged(p);
 		/*
 		 * Setting the value from prefs
 		 */
 		setFlag(p, flagval);
 		/*
 		 * After value changes from loading
 		 */
 		postValChanged(p);
 	}
 
 	void handleNetPrefChange(final NetPref np, final String network,
 			final int newvalue) {
 		putnetPref(np, network, newvalue);
 	}
 
 	public static void notifyPrefChange(final Context c, final String pref,
 			boolean b) {
 		Intent intent = new Intent(VALUE_CHANGED_ACTION);
 		intent.putExtra(VALUE_KEY, pref);
 		intent.putExtra(DATA_KEY, b);
 		c.sendBroadcast(intent);
 	}
 
 	public static void notifyNetPrefChange(final Context c,
 			final NetPref netpref, final StringBuilder netstring,
 			final int value) {
 		Intent intent = new Intent(NETVALUE_CHANGED_ACTION);
 		intent.putExtra(VALUE_KEY, netpref.key());
 		intent.putExtra(NET_KEY, netstring.toString());
 		intent.putExtra(INT_KEY, value);
 		c.sendBroadcast(intent);
 	}
 
 	public void preLoad() {
 
 		/*
 		 * Pre-Pref load
 		 */
 
 	}
 
 	public void preValChanged(final Pref p) {
 		switch (p) {
 		/*
 		 * Pre Value Changed here
 		 */
 		}
 
 	}
 
 	public void postValChanged(final Pref p) {
 	}
 
 	public static StringBuilder getnetworkSSID(final Context context,
 			final int network) {
 		WifiManager wm = (WifiManager) context
 				.getSystemService(Context.WIFI_SERVICE);
 		if (!wm.isWifiEnabled())
 			return new StringBuilder(context.getString(R.string.none));
 		else
 			return getSafeFileName(context,
 					getSSIDfromNetwork(context, network));
 	}
 
 	public static String getSSIDfromNetwork(final Context context,
 			final int network) {
 		final List<WifiConfiguration> wifiConfigs = ((WifiManager) context
 				.getSystemService(Context.WIFI_SERVICE))
 				.getConfiguredNetworks();
 		for (WifiConfiguration w : wifiConfigs) {
 			if (w.networkId == network)
 				return w.SSID;
 		}
 		return null;
 	}
 
 	public static WifiConfiguration getNetworkByNID(Context context,
 			final int network) {
 		List<WifiConfiguration> configs = getWifiManager(context)
 				.getConfiguredNetworks();
 		for (WifiConfiguration w : configs) {
 			if (w.networkId == network)
 				return w;
 		}
 		return null;
 	}
 
 	public static StringBuilder getSafeFileName(final Context ctxt,
 			String filename) {
 		if (filename == null)
 			filename = ctxt.getString(R.string.none);
 
 		return new StringBuilder(filename.replaceAll("[^a-zA-Z0-9]", ""));
 	}
 
 	public static int readNetworkPref(final Context ctxt,
 			final StringBuilder netstring, final NetPref pref) {
 		String key = NETPREFIX + netstring + pref.key();
 		if (getSharedPreferences(ctxt).contains(key))
 			return getSharedPreferences(ctxt).getInt(key, 0);
 		else
 			return 0;
 	}
 
 	public static void writeNetworkPref(final Context ctxt,
 			final StringBuilder netstring, final NetPref pref, final int value) {
 		/*
 		 * Check for actual changed value if changed, notify
 		 */
 		if (value != readNetworkPref(ctxt, netstring, pref)) {
 			/*
 			 * commit changes
 			 */
 			SharedPreferences.Editor editor = getSharedPreferences(ctxt).edit();
 			editor.putInt(NETPREFIX + netstring + pref.key(), value);
 			EditorDetector.commit(editor);
 			/*
 			 * notify
 			 */
 			notifyNetPrefChange(ctxt, pref, netstring, value);
 		}
 	}
 
 	public static boolean readBoolean(final Context ctxt, final String key) {
 		return getSharedPreferences(ctxt).getBoolean(key, false);
 	}
 
 	public static void writeBoolean(final Context ctxt, final String key,
 			final boolean value) {
 		SharedPreferences.Editor editor = getSharedPreferences(ctxt).edit();
 		editor.putBoolean(key, value);
 		EditorDetector.commit(editor);
 		;
 	}
 
 	public static String readString(final Context ctxt, final String key) {
 		return getSharedPreferences(ctxt).getString(key, null);
 	}
 
 	public static void writeString(final Context ctxt, final String key,
 			final String value) {
 		SharedPreferences.Editor editor = getSharedPreferences(ctxt).edit();
 		editor.putString(key, value);
 		EditorDetector.commit(editor);
 	}
 
 	public static int readInt(final Context ctxt, final String key) {
 		return getSharedPreferences(ctxt).getInt(key, -1);
 	}
 
 	public static void writeInt(final Context ctxt, final String key,
 			final int value) {
 		SharedPreferences.Editor editor = getSharedPreferences(ctxt).edit();
 		editor.putInt(key, value);
 		EditorDetector.commit(editor);
 	}
 
 	public static void removeKey(final Context ctxt, final String key) {
 		SharedPreferences.Editor editor = getSharedPreferences(ctxt).edit();
 		editor.remove(key);
 		EditorDetector.commit(editor);
 	}
 
 	public void specialCase() {
 		/*
 		 * Any special case code here
 		 */
 
 	}
 
 	public void log() {
 
 	}
 
 	public static boolean getFlag(final Pref pref) {
 		return keyVals[pref.ordinal()];
 	}
 
 	public static void setFlag(final Pref pref, final boolean flag) {
 		keyVals[pref.ordinal()] = flag;
 	}
 
 	public void unRegisterReciever() {
 		context.get().unregisterReceiver(changeReceiver);
 	}
 
 	public static void setPolicyfromSystem(final Context context) {
 		/*
 		 * Handle Wifi Sleep Policy
 		 */
 		ContentResolver cr = context.getContentResolver();
 		try {
 			int wfsleep = android.provider.Settings.System.getInt(cr,
 					android.provider.Settings.System.WIFI_SLEEP_POLICY);
 			PrefUtil.writeString(context, PrefConstants.SLPOLICY_KEY,
 					String.valueOf(wfsleep));
 		} catch (SettingNotFoundException e) {
 			/*
 			 * Don't need a catch, all clients are >= 1.5 per manifest market
 			 * restriction
 			 */
 		}
 	}
 
 	public static void setPolicy(final Context context, final int policy) {
 		/*
 		 * Set Wifi Sleep Policy
 		 */
 		ContentResolver cr = context.getContentResolver();
 		android.provider.Settings.System.putInt(cr,
 				android.provider.Settings.System.WIFI_SLEEP_POLICY, policy);
 
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
 
 	public static boolean getNetworkState(final Context context,
 			final int network) {
 		WifiConfiguration w = getNetworkByNID(context, network);
 		if (!getWifiManager(context).isWifiEnabled())
 			return !readNetworkState(context, network);
 		else if (w != null && w.status == WifiConfiguration.Status.DISABLED)
 			return false;
 		else
 			return true;
 	}
 
 	public static void writeNetworkState(final Context context,
 			final int network, final boolean state) {
 		StringBuilder netstring = getnetworkSSID(context, network);
 		if (state)
 			PrefUtil.writeNetworkPref(context, netstring, NetPref.DISABLED_KEY,
 					1);
 		else
 			PrefUtil.writeNetworkPref(context, netstring, NetPref.DISABLED_KEY,
 					0);
 	}
 
 	public static boolean readManagedState(final Context context,
 			final int network) {
 
 		if (PrefUtil.readNetworkPref(context, getnetworkSSID(context, network),
 				NetPref.NONMANAGED_KEY) == 1)
 			return true;
 		else
 			return false;
 	}
 
 	public static void writeManagedState(final Context context,
 			final int network, final boolean state) {
 		StringBuilder netstring = getnetworkSSID(context, network);
 		if (state)
 			PrefUtil.writeNetworkPref(context, netstring,
 					NetPref.NONMANAGED_KEY, 1);
 		else
 			PrefUtil.writeNetworkPref(context, netstring,
 					NetPref.NONMANAGED_KEY, 0);
 	}
 
 	public static boolean readNetworkState(final Context context,
 			final int network) {
 		if (PrefUtil.readNetworkPref(context, getnetworkSSID(context, network),
 				NetPref.DISABLED_KEY) == 1)
 			return true;
 		else
 			return false;
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
 
 }
