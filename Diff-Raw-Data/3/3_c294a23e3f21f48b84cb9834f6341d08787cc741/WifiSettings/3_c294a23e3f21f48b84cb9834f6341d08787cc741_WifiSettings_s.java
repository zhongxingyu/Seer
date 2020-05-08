 package com.dbstar.settings.wifi;
 
 import com.dbstar.settings.R;
 import com.dbstar.settings.wifi.WifiEnabler;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.NetworkInfo.DetailedState;
 import android.net.wifi.ScanResult;
 import android.net.wifi.SupplicantState;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiConfiguration.KeyMgmt;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.content.BroadcastReceiver;
 import android.graphics.Bitmap;
 import android.util.Log;
 
 //invisible
 import com.android.internal.util.AsyncChannel;
 
 import android.security.Credentials;
 import android.security.KeyStore;
 import android.net.wifi.WpsResult;
 
 import com.dbstar.settings.utils.Utils;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;
 
 public class WifiSettings {
 	private static final String TAG = "WifiSettings";
 
 	// Instance state keys
 	private static final String SAVE_DIALOG_EDIT_MODE = "edit_mode";
 	private static final String SAVE_DIALOG_ACCESS_POINT_STATE = "wifi_ap_state";
 	// Combo scans can take 5-6s to complete - set to 10s.
 	private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;
 	private static final int WIFI_DIALOG_ID = 1;
 
 	private final IntentFilter mFilter;
 	private final BroadcastReceiver mReceiver;
 	private final Scanner mScanner;
 
 	private WifiManager mWifiManager;
 
 	private DetailedState mLastState;
 	private WifiInfo mLastInfo;
 	private AtomicBoolean mConnected = new AtomicBoolean(false);
 	private int mKeyStoreNetworkId = INVALID_NETWORK_ID;
 
 	ArrayList<AccessPoint> mAccessPointList = new ArrayList<AccessPoint>();
 	AccessPointsAdapter mAPAdapter;
 	ListView mAccessPointListView;
 
 	private WifiDialog mDialog;
 
 	private AccessPoint mSelectedAccessPoint;
 
 	// Save the dialog details
 	private AccessPoint mDlgAccessPoint;
 	private Bundle mAccessPointSavedState;
 	private Activity mActivity;
 
 	public WifiSettings(Activity activity) {
 		mActivity = activity;
 
 		mFilter = new IntentFilter();
 		mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
 		mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
 		mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
 		mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
 		mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
 		mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
 
 		mFilter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
 		mFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
 		mFilter.addAction(WifiManager.ERROR_ACTION);
 
 		mReceiver = new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				handleEvent(context, intent);
 			}
 		};
 
 		mScanner = new Scanner();
 	}
 
 	public void onActivityCreated(Bundle savedInstanceState) {
 
 		mWifiManager = (WifiManager) mActivity
 				.getSystemService(Context.WIFI_SERVICE);
 
 		mWifiManager.asyncConnect(mActivity, new WifiServiceHandler());
 
 		mAccessPointListView = (ListView) mActivity
 				.findViewById(R.id.wifi_aplist);
 
 		mAPAdapter = new AccessPointsAdapter(mActivity);
 		mAPAdapter.setDataSet(mAccessPointList);
 		mAccessPointListView.setAdapter(mAPAdapter);
 
 		mAccessPointListView.setOnItemClickListener(mOnAPSelectedListener);
 		mAccessPointListView.setOnItemSelectedListener(mItemSelectedListener);
 
 		// mAccessPointListView.requestFocus();
 	}
 
 	public void onResume() {
 
 		mActivity.registerReceiver(mReceiver, mFilter);
 		if (mKeyStoreNetworkId != INVALID_NETWORK_ID
 				&& KeyStore.getInstance().state() == KeyStore.State.UNLOCKED) {
 			mWifiManager.connectNetwork(mKeyStoreNetworkId);
 		}
 		mKeyStoreNetworkId = INVALID_NETWORK_ID;
 
 		updateAccessPoints();
 	}
 
 	public void onPause() {
 		mActivity.unregisterReceiver(mReceiver);
 		mScanner.pause();
 	}
 
 	/**
 	 * Shows the latest access points available with supplimental information
 	 * like the strength of network and the security for it.
 	 */
 	private void updateAccessPoints() {
 		final int wifiState = mWifiManager.getWifiState();
 
 		Log.d(TAG, "wifiState = " + wifiState);
 
 		switch (wifiState) {
 		case WifiManager.WIFI_STATE_ENABLED:
 			// AccessPoints are automatically sorted with TreeSet.
 			final Collection<AccessPoint> accessPoints = constructAccessPoints();
 			mAccessPointList.clear();
 			for (AccessPoint accessPoint : accessPoints) {
 				mAccessPointList.add(accessPoint);
 			}
 
 			mAPAdapter.notifyDataSetChanged();
 			break;
 
 		case WifiManager.WIFI_STATE_ENABLING:
 			mAccessPointList.clear();
 			mAPAdapter.notifyDataSetChanged();
 			break;
 
 		case WifiManager.WIFI_STATE_DISABLING:
 			mAccessPointList.clear();
 			mAPAdapter.notifyDataSetChanged();
 			// mWifiSwitchTitle.setText(R.string.wifi_stopping);
 			break;
 
 		case WifiManager.WIFI_STATE_DISABLED:
 			mAccessPointList.clear();
 			mAPAdapter.notifyDataSetChanged();
 			// mWifiSwitchTitle.setText(R.string.wifi_empty_list_wifi_off);
 			break;
 		}
 	}
 
 	/** Returns sorted list of access points */
 	private List<AccessPoint> constructAccessPoints() {
 		ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
 		/**
 		 * Lookup table to more quickly update AccessPoints by only considering
 		 * objects with the correct SSID. Maps SSID -> List of AccessPoints with
 		 * the given SSID.
 		 */
 		Multimap<String, AccessPoint> apMap = new Multimap<String, AccessPoint>();
 
 		final List<WifiConfiguration> configs = mWifiManager
 				.getConfiguredNetworks();
 		if (configs != null) {
 			for (WifiConfiguration config : configs) {
 				AccessPoint accessPoint = new AccessPoint(mActivity, config);
 				accessPoint.update(mLastInfo, mLastState);
 				accessPoints.add(accessPoint);
 				apMap.put(accessPoint.ssid, accessPoint);
 			}
 		}
 
 		final List<ScanResult> results = mWifiManager.getScanResults();
 		if (results != null) {
 			for (ScanResult result : results) {
 				// Ignore hidden and ad-hoc networks.
 				if (result.SSID == null || result.SSID.length() == 0
 						|| result.capabilities.contains("[IBSS]")) {
 					continue;
 				}
 
 				boolean found = false;
 				for (AccessPoint accessPoint : apMap.getAll(result.SSID)) {
 					if (accessPoint.update(result))
 						found = true;
 				}
 				if (!found) {
 					AccessPoint accessPoint = new AccessPoint(mActivity, result);
 					accessPoints.add(accessPoint);
 					apMap.put(accessPoint.ssid, accessPoint);
 				}
 			}
 		}
 
 		// Pre-sort accessPoints to speed preference insertion
 		Collections.sort(accessPoints);
 		return accessPoints;
 	}
 
 	/** A restricted multimap for use in constructAccessPoints */
 	private class Multimap<K, V> {
 		private HashMap<K, List<V>> store = new HashMap<K, List<V>>();
 
 		/** retrieve a non-null list of values with key K */
 		List<V> getAll(K key) {
 			List<V> values = store.get(key);
 			return values != null ? values : Collections.<V> emptyList();
 		}
 
 		void put(K key, V val) {
 			List<V> curVals = store.get(key);
 			if (curVals == null) {
 				curVals = new ArrayList<V>(3);
 				store.put(key, curVals);
 			}
 			curVals.add(val);
 		}
 	}
 
 	private void handleEvent(Context context, Intent intent) {
 		String action = intent.getAction();
 		if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
 			updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
 					WifiManager.WIFI_STATE_UNKNOWN));
 		} else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)
 				|| WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION
 						.equals(action)
 				|| WifiManager.LINK_CONFIGURATION_CHANGED_ACTION.equals(action)) {
 			updateAccessPoints();
 		} else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
 			// Ignore supplicant state changes when network is connected
 			// TODO: we should deprecate SUPPLICANT_STATE_CHANGED_ACTION and
 			// introduce a broadcast that combines the supplicant and network
 			// network state change events so the apps dont have to worry about
 			// ignoring supplicant state change when network is connected
 			// to get more fine grained information.
 			if (!mConnected.get()) {
 				updateConnectionState(WifiInfo
 						.getDetailedStateOf((SupplicantState) intent
 								.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
 			}
 
 		} else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
 			NetworkInfo info = (NetworkInfo) intent
 					.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
 			mConnected.set(info.isConnected());
 			updateAccessPoints();
 			updateConnectionState(info.getDetailedState());
 		} else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
 			updateConnectionState(null);
 		} else if (WifiManager.ERROR_ACTION.equals(action)) {
 			int errorCode = intent.getIntExtra(WifiManager.EXTRA_ERROR_CODE, 0);
 			switch (errorCode) {
 			case WifiManager.WPS_OVERLAP_ERROR:
 				Toast.makeText(context, R.string.wifi_wps_overlap_error,
 						Toast.LENGTH_SHORT).show();
 				break;
 			}
 		}
 	}
 
 	private void updateConnectionState(DetailedState state) {
 		/* sticky broadcasts can call this when wifi is disabled */
 		if (!mWifiManager.isWifiEnabled()) {
 			mScanner.pause();
 			return;
 		}
 
 		if (state == DetailedState.OBTAINING_IPADDR) {
 			mScanner.pause();
 		} else {
 			mScanner.resume();
 		}
 
 		mLastInfo = mWifiManager.getConnectionInfo();
 		if (state != null) {
 			mLastState = state;
 		}
 
 		for (int i = mAccessPointList.size() - 1; i >= 0; --i) {
 			final AccessPoint accessPoint = mAccessPointList.get(i);
 			accessPoint.update(mLastInfo, mLastState);
 		}
 
 		mAPAdapter.notifyDataSetChanged();
 
 	}
 
 	private void updateWifiState(int state) {
 		switch (state) {
 		case WifiManager.WIFI_STATE_ENABLED:
 			mScanner.resume();
 			// mEthConfigView.setVisibility(View.GONE);
 			// mWifiConfigView.setVisibility(View.VISIBLE);
 			return;
 
 		case WifiManager.WIFI_STATE_ENABLING:
 			// mWifiSwitchTitle.setText(R.string.wifi_starting);
 			break;
 
 		case WifiManager.WIFI_STATE_DISABLED:
 			// mWifiSwitchTitle.setText(R.string.wifi_empty_list_wifi_off);
 			break;
 		}
 
 		mAccessPointList.clear();
 		mAPAdapter.notifyDataSetChanged();
 
 		mLastInfo = null;
 		mLastState = null;
 		mScanner.pause();
 	}
 
 	private class Scanner extends Handler {
 		private int mRetry = 0;
 
 		void resume() {
 			if (!hasMessages(0)) {
 				sendEmptyMessage(0);
 			}
 		}
 
 		void forceScan() {
 			removeMessages(0);
 			sendEmptyMessage(0);
 		}
 
 		void pause() {
 			mRetry = 0;
 			removeMessages(0);
 		}
 
 		@Override
 		public void handleMessage(Message message) {
 			if (mWifiManager.startScanActive()) {
 				mRetry = 0;
 			} else if (++mRetry >= 3) {
 				mRetry = 0;
 				Toast.makeText(mActivity, R.string.wifi_fail_to_scan,
 						Toast.LENGTH_LONG).show();
 				return;
 			}
 			sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
 		}
 	}
 
 	private class WifiServiceHandler extends Handler {
 
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case AsyncChannel.CMD_CHANNEL_HALF_CONNECTED:
 				if (msg.arg1 == AsyncChannel.STATUS_SUCCESSFUL) {
 					// AsyncChannel in msg.obj
 				} else {
 					// AsyncChannel set up failure, ignore
 					Log.e(TAG, "Failed to establish AsyncChannel connection");
 				}
 				break;
 			case WifiManager.CMD_WPS_COMPLETED:
 				WpsResult result = (WpsResult) msg.obj;
 				if (result == null)
 					break;
 				AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity)
 						.setTitle(R.string.wifi_wps_setup_title)
 						.setPositiveButton(android.R.string.ok, null);
 				switch (result.status) {
 				case FAILURE:
 					dialog.setMessage(R.string.wifi_wps_failed);
 					dialog.show();
 					break;
 				case IN_PROGRESS:
 					dialog.setMessage(R.string.wifi_wps_in_progress);
 					dialog.show();
 					break;
 				default:
 					if (result.pin != null) {
 						dialog.setMessage(mActivity.getResources().getString(
 								R.string.wifi_wps_pin_output, result.pin));
 						dialog.show();
 					}
 					break;
 				}
 				break;
 			// TODO: more connectivity feedback
 			default:
 				// Ignore
 				break;
 			}
 		}
 	}
 
 	View mLastSelectedView = null, mSelectedView = null;
 	OnItemSelectedListener mItemSelectedListener = new OnItemSelectedListener() {
 
 		@Override
 		public void onItemSelected(AdapterView<?> parent, View view,
 				int position, long id) {
 
 			mLastSelectedView = mSelectedView;
 			mSelectedView = view;
 
 			if (mLastSelectedView != null) {
 				setItemSelected(mLastSelectedView, false);
 			}
 
 			setItemSelected(mSelectedView, true);
 		}
 
 		@Override
 		public void onNothingSelected(AdapterView<?> arg0) {
 		}
 
 	};
 
 	void setItemSelected(View v, boolean selected) {
 		AccessPointsAdapter.ViewHolder holder = (AccessPointsAdapter.ViewHolder) v
 				.getTag();
 
 		if (selected) {
 			holder.mTitleView.setVisibility(View.GONE);
 			holder.mSummaryView.setVisibility(View.GONE);
 			holder.mHighlightTileView.setVisibility(View.VISIBLE);
 			holder.mHighlightSummaryView.setVisibility(View.VISIBLE);
 		} else {
 			holder.mTitleView.setVisibility(View.VISIBLE);
 			holder.mSummaryView.setVisibility(View.VISIBLE);
 			holder.mHighlightTileView.setVisibility(View.GONE);
 			holder.mHighlightSummaryView.setVisibility(View.GONE);
 		}
 	}
 
 	OnItemClickListener mOnAPSelectedListener = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> parent, View v, int position,
 				long id) {
 			onAccessPointSelected(position);
 		}
 
 	};
 
 	void onAccessPointSelected(int index) {
 		mSelectedAccessPoint = mAccessPointList.get(index);
 
 		if (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
 			WifiConfiguration config = mSelectedAccessPoint.getConfig();
 
 			if (config.status == WifiConfiguration.Status.DISABLED
 					&& config.disableReason == WifiConfiguration.DISABLED_AUTH_FAILURE) {
 				showConfigUi(mSelectedAccessPoint);
 				return;
 			}
 
 			if (!requireKeyStore(mSelectedAccessPoint.getConfig())) {
 				mWifiManager.connectNetwork(mSelectedAccessPoint.networkId);
 			}
		} else if (mSelectedAccessPoint.security == AccessPoint.SECURITY_NONE
				|| mSelectedAccessPoint.networkId == INVALID_NETWORK_ID) {
 			/** Bypass dialog for unsecured, unsaved networks */
 			mSelectedAccessPoint.generateOpenNetworkConfig();
 			mWifiManager.connectNetwork(mSelectedAccessPoint.getConfig());
 		} else {
 			// configure access point
 			showConfigUi(mSelectedAccessPoint);
 		}
 	}
 
 	/**
 	 * Shows an appropriate Wifi configuration component. Called when a user
 	 * clicks "Add network" preference or one of available networks is selected.
 	 */
 	private void showConfigUi(AccessPoint accessPoint) {
 		mDialog = WifiDialog.newInstance(mActivity, mConnectClickListener,
 				accessPoint);
 		mDialog.show();
 	}
 
 	private View.OnClickListener mConnectClickListener = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			submit(mDialog.getController());
 			mDialog.dismiss();
 			mDialog = null;
 		}
 	};
 
 	private boolean requireKeyStore(WifiConfiguration config) {
 		if (WifiConfigController.requireKeyStore(config)
 				&& KeyStore.getInstance().state() != KeyStore.State.UNLOCKED) {
 			mKeyStoreNetworkId = config.networkId;
 			Credentials.getInstance().unlock(mActivity);
 			return true;
 		}
 		return false;
 	}
 
 	/* package */void forget() {
 		mWifiManager.forgetNetwork(mSelectedAccessPoint.networkId);
 
 		if (mWifiManager.isWifiEnabled()) {
 			mScanner.resume();
 		}
 		updateAccessPoints();
 	}
 
 	void submit(WifiConfigController configController) {
 		final WifiConfiguration config = configController.getConfig();
 
 		if (config == null) {
 			if (mSelectedAccessPoint != null
 					&& !requireKeyStore(mSelectedAccessPoint.getConfig())
 					&& mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
 				mWifiManager.connectNetwork(mSelectedAccessPoint.networkId);
 			}
 		} else {
 			mWifiManager.saveNetwork(config);
 			mWifiManager.connectNetwork(config);
 		}
 
 		if (mWifiManager.isWifiEnabled()) {
 			mScanner.resume();
 		}
 
 		updateAccessPoints();
 	}
 
 	private class AccessPointsAdapter extends BaseAdapter {
 
 		public class ViewHolder {
 			TextView mTitleView;
 			TextView mSummaryView;
 			TextView mHighlightTileView;
 			TextView mHighlightSummaryView;
 		}
 
 		private ArrayList<AccessPoint> mDataSet = null;
 
 		public AccessPointsAdapter(Context context) {
 		}
 
 		public void setDataSet(ArrayList<AccessPoint> dataSet) {
 			mDataSet = dataSet;
 		}
 
 		@Override
 		public int getCount() {
 			int count = 0;
 			if (mDataSet != null) {
 				count = mDataSet.size();
 			}
 
 			return count;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			ViewHolder holder = null;
 			if (null == convertView) {
 				LayoutInflater inflater = mActivity.getLayoutInflater();
 				convertView = inflater.inflate(R.layout.wifi_list_item, parent,
 						false);
 
 				holder = new ViewHolder();
 				holder.mTitleView = (TextView) convertView
 						.findViewById(R.id.title);
 				holder.mSummaryView = (TextView) convertView
 						.findViewById(R.id.summary);
 				holder.mHighlightSummaryView = (TextView) convertView
 						.findViewById(R.id.highlight_summary);
 				holder.mHighlightTileView = (TextView) convertView
 						.findViewById(R.id.highlight_title);
 
 				convertView.setTag(holder);
 			}
 
 			mDataSet.get(position).bindView(convertView);
 			mDataSet.get(position).refresh();
 
 			return convertView;
 		}
 	}
 }
