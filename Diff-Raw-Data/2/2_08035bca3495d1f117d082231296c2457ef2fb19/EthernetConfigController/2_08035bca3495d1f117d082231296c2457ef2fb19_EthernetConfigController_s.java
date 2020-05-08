 package com.dbstar.settings.ethernet;
 
 import java.util.List;
 
 import com.dbstar.settings.R;
 import com.dbstar.settings.base.PageManager;
 import com.dbstar.settings.utils.SettingsCommon;
 import com.dbstar.settings.utils.Utils;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.NetworkUtils;
 import android.net.DhcpInfo;
 import android.net.ethernet.EthernetManager;
 import android.net.ethernet.EthernetDevInfo;
 import android.net.ethernet.EthernetStateTracker;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.inputmethod.InputMethodManager;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EthernetConfigController {
 	private final String TAG = "EthernetConfigController";
 
 	public static final int MSG_NETWORK_CONNECT = 0;
 	public static final int MSG_NETWORK_DISCONNECT = 1;
 
 	public static final String DefaultEthernetDeviceName = "eth0";
 	private View mDhcpSwitchButton;
 	private CheckBox mDhcpSwitchIndicator;
 	private TextView mDhcpConnectState, mDhcpSwitchTitle;
 
 	private View mManualSwitchButton;
 	private CheckBox mManualSwitchIndicator;
 	private TextView mManualConnectState, mManualSwitchTitle;
 
 	private EditText mIpaddr;
 	private EditText mDns, mBackupDns;
 	private EditText mGw;
 	private EditText mMask;
 
 	Button mOkButton, mPrevButton;
 
 	private EthernetManager mEthManager;
 	private EthernetDevInfo mEthInfo;
 
 	private IntentFilter mEthIntentFilter, mConnectIntentFilter;
 	private Handler mHandler;
 
 	private boolean mEnablePending;
 
 	private Context mContext;
 	private Activity mActivity;
 
 	ConnectivityManager mConnectManager;
 
 	String mDev = null;
 	
 	boolean mIsEthHWConnected = false;
 
 	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			int state = intent.getIntExtra(EthernetManager.EXTRA_ETH_STATE,
 					EthernetStateTracker.EVENT_HW_DISCONNECTED);
 
 			Log.d(TAG, " recv state=" + state);
 
 			if (state == EthernetStateTracker.EVENT_HW_CONNECTED) {
 				handleEthStateChanged(true);
 			} else if (state == EthernetStateTracker.EVENT_HW_DISCONNECTED) {
 				handleEthStateChanged(false);
 			}
 		}
 	};
 
 	private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
 
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
 				return;
 
 			boolean noConnectivity = intent.getBooleanExtra(
 					ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
 
 			Log.d(TAG, "noConnectivity = " + noConnectivity);
 			if (noConnectivity) {
 				// There are no connected networks at all
 				handleNetConnected();
 				return;
 			}
 
 			// case 1: attempting to connect to another network, just wait for
 			// another broadcast
 			// case 2: connected
 			// NetworkInfo networkInfo = (NetworkInfo) intent
 			// .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
 
 			NetworkInfo networkInfo = mConnectManager.getActiveNetworkInfo();
 
 			if (networkInfo != null) {
 				Log.d(TAG, "getTypeName() = " + networkInfo.getTypeName());
 				Log.d(TAG, "isConnected() = " + networkInfo.isConnected());
 
 				if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET
 						&& networkInfo.isConnected()) {
 					handleNetConnected();
 				}
 			}
 		}
 
 	};
 
 	private void reqisterConnectReceiver() {
 		mActivity.registerReceiver(mNetworkReceiver, mConnectIntentFilter);
 	}
 
 	private void unregisterConnectReceiver() {
 		mActivity.unregisterReceiver(mNetworkReceiver);
 	}
 
 	public boolean isNetworkConnected() {
 		NetworkInfo networkInfo = mConnectManager.getActiveNetworkInfo();
 		return networkInfo != null
 				&& networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET
 				&& networkInfo.isConnected();
 	}
 
 	void handleNetConnected() {
 
 		mHandler.post(new Runnable() {
 			public void run() {
 				setConnectionStatus(isNetworkConnected());
 			}
 		});
 	}
 
 	void handleEthStateChanged(boolean ethHWConnected) {
 		mIsEthHWConnected = ethHWConnected;
 		mHandler.post(new Runnable() {
 			public void run() {
 				setConnectionStatus(mIsEthHWConnected);
 			}
 		});
 	}
 
 	void setConnectionStatus(boolean connected) {
 
 		Log.d(TAG, " =================== network connected =  " + connected);
 
 		if (connected) {
 			if (mDhcpSwitchIndicator.isChecked()) {
 				mDhcpConnectState.setVisibility(View.VISIBLE);
 				
 				updateDhcpInfo();
 			}
 
 			if (mManualSwitchIndicator.isChecked()) {
 				mManualConnectState.setVisibility(View.VISIBLE);
 			}
 
 		} else {
 			if (mDhcpSwitchIndicator.isChecked()) {
 				mDhcpConnectState.setVisibility(View.INVISIBLE);
 				
				updateDhcpInfo();
 			}
 
 			if (mManualSwitchIndicator.isChecked()) {
 				mManualConnectState.setVisibility(View.INVISIBLE);
 			}
 		}
 	}
 
 	public EthernetConfigController(Activity activity,
 			EthernetManager ethManager) {
 		mActivity = activity;
 		mEthManager = ethManager;
 		mContext = activity;
 
 		mEthIntentFilter = new IntentFilter(
 				EthernetManager.ETH_STATE_CHANGED_ACTION);
 
 		mConnectIntentFilter = new IntentFilter(
 				ConnectivityManager.CONNECTIVITY_ACTION);
 
 		mConnectManager = (ConnectivityManager) mActivity
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 
 		mHandler = new Handler();
 
 		buildDialogContent(activity);
 		enableAfterConfig();
 
 	}
 
 	public void resume() {
 		getContext().registerReceiver(mReceiver, mEthIntentFilter);
 		reqisterConnectReceiver();
 
 		setConnectionStatus(isNetworkConnected());
 	}
 
 	public void pause() {
 		getContext().unregisterReceiver(mReceiver);
 		unregisterConnectReceiver();
 	}
 
 	public Context getContext() {
 		return mContext;
 	}
 
 	private static String getAddress(int addr) {
 		return NetworkUtils.intToInetAddress(addr).getHostAddress();
 	}
 
 	public int buildDialogContent(Context context) {
 		mDhcpSwitchButton = (View) mActivity
 				.findViewById(R.id.dhcp_switch_button);
 		mDhcpSwitchIndicator = (CheckBox) mActivity
 				.findViewById(R.id.dhcp_switch_indicator);
 
 		mDhcpSwitchTitle = (TextView) mActivity
 				.findViewById(R.id.dhcp_switch_title);
 
 		mDhcpConnectState = (TextView) mActivity
 				.findViewById(R.id.dhcp_conncetion_state);
 
 		mManualSwitchButton = (View) mActivity
 				.findViewById(R.id.manual_switch_button);
 		mManualSwitchIndicator = (CheckBox) mActivity
 				.findViewById(R.id.manual_switch_indicator);
 		mManualSwitchTitle = (TextView) mActivity
 				.findViewById(R.id.manaul_switch_title);
 		mManualConnectState = (TextView) mActivity
 				.findViewById(R.id.manual_conncetion_state);
 
 		mOkButton = (Button) mActivity.findViewById(R.id.okbutton);
 		mPrevButton = (Button) mActivity.findViewById(R.id.prevbutton);
 
 		mDhcpSwitchButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				enableDhcp(true);
 			}
 		});
 
 		mManualSwitchButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				enableManual(true);
 			}
 		});
 
 		mDhcpSwitchButton.setOnFocusChangeListener(mFocusChangeListener);
 		mManualSwitchButton.setOnFocusChangeListener(mFocusChangeListener);
 
 		mDhcpSwitchButton.requestFocus();
 
 		mIpaddr = (EditText) mActivity.findViewById(R.id.eth_ip);
 		mMask = (EditText) mActivity.findViewById(R.id.eth_mask);
 		mDns = (EditText) mActivity.findViewById(R.id.eth_dns);
 		mBackupDns = (EditText) mActivity.findViewById(R.id.eth_backup_dns);
 		mGw = (EditText) mActivity.findViewById(R.id.eth_gateway);
 
 		enableDhcp(true);
 
 		String[] Devs = mEthManager.getDeviceNameList();
 		
 		if (Devs != null) {
 			Log.d(TAG, "Devices = " + Devs + " count " + Devs.length);
 			if (mEthManager.isEthConfigured()) {
 				mEthInfo = mEthManager.getSavedEthConfig();
 
 				mDev = mEthInfo.getIfName();
 				mIpaddr.setText(mEthInfo.getIpAddress());
 				mGw.setText(mEthInfo.getRouteAddr());
 				mDns.setText(mEthInfo.getDnsAddr());
 				mMask.setText(mEthInfo.getNetMask());
 
 				if (mEthInfo.getConnectMode().equals(
 						EthernetDevInfo.ETH_CONN_MODE_DHCP)) {
 
 					enableDhcp(true);
 
 //					updateDhcpInfo();
 
 				} else {
 					enableManual(true);
 				}
 			} else {
 				getEthernetDevice(Devs);
 			}
 		}
 		return 0;
 	}
 	
 	void updateDhcpInfo() {
 		DhcpInfo dhcpInfo = mEthManager.getDhcpInfo();
 		if (dhcpInfo != null) {
 			mIpaddr.setText(getAddress(dhcpInfo.ipAddress));
 			mMask.setText(getAddress(dhcpInfo.netmask));
 			mGw.setText(getAddress(dhcpInfo.gateway));
 			mDns.setText(getAddress(dhcpInfo.dns1));
 			mBackupDns.setText(getAddress(dhcpInfo.dns2));
 		}
 	}
 
 	View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
 
 		@Override
 		public void onFocusChange(View v, boolean hasFocus) {
 			if (hasFocus == true) {
 				if (v.getId() == R.id.dhcp_switch_button) {
 					mDhcpSwitchTitle.setTextColor(0xFFFFCC00);
 				} else if (v.getId() == R.id.manual_switch_button) {
 					mManualSwitchTitle.setTextColor(0xFFFFCC00);
 				} else if (v instanceof EditText) {
 					EditText textView = (EditText) v;
 					textView.setSelection(0);
 				}
 			} else {
 				if (v.getId() == R.id.dhcp_switch_button) {
 					mDhcpSwitchTitle.setTextColor(0xFF000000);
 				} else if (v.getId() == R.id.manual_switch_button) {
 					mManualSwitchTitle.setTextColor(0xFF000000);
 				}
 			}
 
 		}
 	};
 
 	private void enableDhcp(boolean enable) {
 		mDhcpSwitchIndicator.setChecked(enable);
 		mManualSwitchIndicator.setChecked(!enable);
 
 		mIpaddr.setEnabled(!enable);
 		mDns.setEnabled(!enable);
 		mBackupDns.setEnabled(!enable);
 		mGw.setEnabled(!enable);
 		mMask.setEnabled(!enable);
 
 		mIpaddr.setFocusable(!enable);
 		mDns.setFocusable(!enable);
 		mBackupDns.setFocusable(!enable);
 		mGw.setFocusable(!enable);
 		mMask.setFocusable(!enable);
 
 		if (isNetworkConnected()) {
 			mDhcpConnectState.setVisibility(View.VISIBLE);
 			mManualConnectState.setVisibility(View.GONE);
 		}
 
 		mManualSwitchButton.setNextFocusDownId(R.id.prevbutton);
 
 		mPrevButton.setNextFocusUpId(R.id.manual_switch_button);
 		mOkButton.setNextFocusUpId(R.id.manual_switch_button);
 	}
 
 	private void enableManual(boolean enable) {
 		mDhcpSwitchIndicator.setChecked(!enable);
 		mManualSwitchIndicator.setChecked(enable);
 
 		mIpaddr.setEnabled(enable);
 		mDns.setEnabled(enable);
 		mBackupDns.setEnabled(enable);
 		mGw.setEnabled(enable);
 		mMask.setEnabled(enable);
 
 		mIpaddr.setFocusable(enable);
 		mDns.setFocusable(enable);
 		mBackupDns.setFocusable(enable);
 		mGw.setFocusable(enable);
 		mMask.setFocusable(enable);
 
 		mIpaddr.setNextFocusLeftId(R.id.gateway_serialnumber);
 		mDns.setNextFocusLeftId(R.id.gateway_serialnumber);
 		mBackupDns.setNextFocusLeftId(R.id.gateway_serialnumber);
 		mGw.setNextFocusLeftId(R.id.gateway_serialnumber);
 		mMask.setNextFocusLeftId(R.id.gateway_serialnumber);
 
 		if (isNetworkConnected()) {
 			mDhcpConnectState.setVisibility(View.GONE);
 			mManualConnectState.setVisibility(View.VISIBLE);
 		}
 
 		mManualSwitchButton.setNextFocusDownId(R.id.eth_ip);
 		mOkButton.setNextFocusUpId(R.id.eth_backup_dns);
 		mPrevButton.setNextFocusUpId(R.id.eth_backup_dns);
 	}
 
 	private void getEthernetDevice(String[] Devs) {
 		for (int i = 0; i < Devs.length; i++) {
 			if (Devs[i].equalsIgnoreCase(DefaultEthernetDeviceName)) {
 				mDev = Devs[i];
 				Log.d(TAG, " device = " + mDev);
 				break;
 			}
 		}
 	}
 
 	public void saveConfigure() {
 		Log.d(TAG, "device name = " + mDev);
 
 		if (mDev == null || mDev.isEmpty())
 			return;
 
 		if (mEthInfo == null) {
 			if (mEthManager.isEthConfigured()) {
 				mEthInfo = mEthManager.getSavedEthConfig();
 			}
 		}
 
 		if (mEthInfo != null) {
 			boolean isDhcp = mEthInfo.getConnectMode().equals(
 					EthernetDevInfo.ETH_CONN_MODE_DHCP);
 			if (isDhcp && mDhcpSwitchIndicator.isChecked()) {
 				// if current configure is Dhcp, and user choose it again,
 				// it doesn't need to save it.
 				return;
 			}
 		}
 
 		EthernetDevInfo info = new EthernetDevInfo();
 		info.setIfName(mDev);
 
 		if (mDhcpSwitchIndicator.isChecked()) {
 			info.setConnectMode(EthernetDevInfo.ETH_CONN_MODE_DHCP);
 			info.setIpAddress(null);
 			info.setRouteAddr(null);
 			info.setDnsAddr(null);
 			info.setNetMask(null);
 		} else {
 			String ip = mIpaddr.getText().toString();
 			String mask = mMask.getText().toString();
 			String gateway = mGw.getText().toString();
 			String dns = mDns.getText().toString();
 
 			boolean valid = true;
 
 			if (ip.isEmpty() || !isIpAddress(ip)) {
 				valid = false;
 			}
 
 			if (mask.isEmpty() || !isIpAddress(mask)) {
 				valid = false;
 			}
 
 			if (!gateway.isEmpty() && !isIpAddress(gateway)) {
 				valid = false;
 			}
 
 			if (!dns.isEmpty() && !isIpAddress(dns)) {
 				valid = false;
 			}
 
 			if (!valid) {
 				Toast.makeText(mContext, R.string.eth_settings_error,
 						Toast.LENGTH_LONG).show();
 				return;
 			}
 
 			info.setConnectMode(EthernetDevInfo.ETH_CONN_MODE_MANUAL);
 			info.setIpAddress(ip);
 			info.setRouteAddr(gateway);
 			info.setDnsAddr(dns);
 			info.setNetMask(mask);
 		}
 
 		mEthManager.updateEthDevInfo(info);
 		if (mEnablePending) {
 			if (mEthManager.getEthState() == mEthManager.ETH_STATE_ENABLED) {
 				mEthManager.setEthEnabled(true);
 			}
 			mEnablePending = false;
 		}
 	}
 
 	private boolean isIpAddress(String value) {
 		int start = 0;
 		int end = value.indexOf('.');
 		int numBlocks = 0;
 
 		while (start < value.length()) {
 			if (end == -1) {
 				end = value.length();
 			}
 
 			try {
 				int block = Integer.parseInt(value.substring(start, end));
 				if ((block > 255) || (block < 0)) {
 					return false;
 				}
 			} catch (NumberFormatException e) {
 				return false;
 			}
 
 			numBlocks++;
 
 			start = end + 1;
 			end = value.indexOf('.', start);
 		}
 		return numBlocks == 4;
 	}
 
 	public void enableAfterConfig() {
 		mEnablePending = true;
 	}
 }
