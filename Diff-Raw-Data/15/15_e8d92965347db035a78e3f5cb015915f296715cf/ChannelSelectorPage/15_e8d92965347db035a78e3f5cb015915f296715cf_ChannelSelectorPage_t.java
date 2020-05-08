 package com.dbstar.settings.network;
 
 import java.io.FileOutputStream;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.TextView;
 
 import com.dbstar.settings.R;
 import com.dbstar.settings.base.BaseFragment;
 import com.dbstar.settings.utils.SettingsCommon;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.net.wifi.WifiManager;
 import android.net.ethernet.EthernetManager;
 
 public class ChannelSelectorPage extends BaseFragment {
 
 	private static final String TAG = "ChannelSelectorPage";
 	View mEthernetSwitchButton;
 	TextView mEthSwitchTitle;
 	CheckBox mEthernetSwitchIndicator;
 
 	View mWifiSwitchButton;
 	TextView mWifiSwitchTitle;
 	CheckBox mWifiSwitchIndicator;
 
 	Button mNextButton, mPrevButton;
 
 	private WifiManager mWifiManager;
 	private EthernetManager mEthManager;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.network_channel_selectorview,
 				container, false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		initializeView();
 
 		mWifiManager = (WifiManager) mActivity
 				.getSystemService(Context.WIFI_SERVICE);
 		mEthManager = (EthernetManager) mActivity
 				.getSystemService(Context.ETH_SERVICE);
 	}
 
 	void initializeView() {
 		mEthernetSwitchButton = (View) mActivity
 				.findViewById(R.id.cable_check_button);
 		mEthSwitchTitle = (TextView) mActivity
 				.findViewById(R.id.cable_check_title);
 		mEthernetSwitchIndicator = (CheckBox) mActivity
 				.findViewById(R.id.cable_check_indicator);
 
 		mWifiSwitchButton = (View) mActivity
 				.findViewById(R.id.wireless_check_button);
 		mWifiSwitchTitle = (TextView) mActivity
 				.findViewById(R.id.wireless_check_title);
 		mWifiSwitchIndicator = (CheckBox) mActivity
 				.findViewById(R.id.wireless_check_indicator);
 
 		mNextButton = (Button) mActivity.findViewById(R.id.nextbutton);
 		mPrevButton = (Button) mActivity.findViewById(R.id.prevbutton);
 
 		mEthernetSwitchButton.setOnClickListener(mOnClickListener);
 		mWifiSwitchButton.setOnClickListener(mOnClickListener);
 
 		mEthernetSwitchButton.setOnFocusChangeListener(mFocusChangeListener);
 		mWifiSwitchButton.setOnFocusChangeListener(mFocusChangeListener);
 
 		mNextButton.setOnClickListener(mOnClickListener);
 		mPrevButton.setOnClickListener(mOnClickListener);
 
 		mEthernetSwitchButton.requestFocus();
 
 		SharedPreferences settings = mActivity.getSharedPreferences(
 				NetworkCommon.PREF_NAME_NETWORK, 0);
 		String channel = settings.getString(NetworkCommon.KeyChannel,
 				NetworkCommon.ChannelEthernet);
 		if (channel.equals(NetworkCommon.ChannelEthernet)) {
 			setChannelEthernet();
 		} else {
 			setChannelWireless();
 		}
 	}
 
 	boolean mIsEthernetSelected = false;
 	boolean mIsWirelessSelected = false;
 
 	private void onWifiChecked() {
 		mEthManager.setEthEnabled(true);
 
 		int wifiApState = mWifiManager.getWifiApState();
 		if ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING)
 				|| (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED)) {
 			mWifiManager.setWifiApEnabled(null, false);
 		}
 
 		mWifiManager.setWifiEnabled(true);
 	}
 
 	private void onEthernetChecked() {
 		mWifiManager.setWifiEnabled(false);
 		mEthManager.setEthEnabled(true);
 	}
 
 	private void setChannel() {
 		String channelValues = null;
 		if (mIsEthernetSelected) {
 			channelValues = NetworkCommon.ChannelEthernet;
 		} else {
 			channelValues = NetworkCommon.ChannelBoth;
 		}
 
 		SharedPreferences settings = mActivity.getSharedPreferences(
 				NetworkCommon.PREF_NAME_NETWORK, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString(NetworkCommon.KeyChannel, channelValues);
 		editor.commit();
 	}
 
 	void setChannelEthernet() {
 		mIsEthernetSelected = true;
 		mIsWirelessSelected = false;
 		mEthernetSwitchIndicator.setChecked(true);
 		mWifiSwitchIndicator.setChecked(false);
 	}
 
 	void setChannelWireless() {
 		mIsWirelessSelected = true;
 		mIsEthernetSelected = false;
 		mWifiSwitchIndicator.setChecked(true);
 		mEthernetSwitchIndicator.setChecked(false);
 	}
 
 	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			if (v.getId() == R.id.cable_check_button) {
				if (mEthernetSwitchIndicator.isChecked())
					return;
				
				mIsEthernetSelected = true;
				mIsWirelessSelected = false;
 				mEthernetSwitchIndicator.setChecked(mIsEthernetSelected);
 				mWifiSwitchIndicator.setChecked(mIsWirelessSelected);
 			} else if (v.getId() == R.id.wireless_check_button) {
				if (mWifiSwitchIndicator.isChecked()) {
					return;
				}
				mIsWirelessSelected = true;
				mIsEthernetSelected = false;
 				mWifiSwitchIndicator.setChecked(mIsWirelessSelected);
 				mEthernetSwitchIndicator.setChecked(mIsEthernetSelected);
 			} else if (v.getId() == R.id.nextbutton) {
 				setChannel();
 				if (mIsEthernetSelected) {
 					onEthernetChecked();
 					mManager.nextPage(SettingsCommon.PAGE_CHANNELSELECTOR,
 							SettingsCommon.PAGE_ETHERNET);
 				} else {
 					onWifiChecked();
 					mManager.nextPage(SettingsCommon.PAGE_CHANNELSELECTOR,
 							SettingsCommon.PAGE_WIFI);
 				}
 			} else if (v.getId() == R.id.prevbutton) {
 				mManager.prevPage(SettingsCommon.PAGE_CHANNELSELECTOR);
 			}
 		}
 	};
 
 	View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
 
 		@Override
 		public void onFocusChange(View v, boolean hasFocus) {
 			Log.d("####", " ========== focus changed ==== " + v.getId() + " "
 					+ hasFocus);
 			if (hasFocus == true) {
 				if (v.getId() == R.id.cable_check_button) {
 					mEthSwitchTitle.setTextColor(0xFFFFCC00);
 				} else if (v.getId() == R.id.wireless_check_button) {
 					mWifiSwitchTitle.setTextColor(0xFFFFCC00);
 				}
 
 			} else {
 				if (v.getId() == R.id.cable_check_button) {
 					mEthSwitchTitle.setTextColor(0xFF000000);
 				} else if (v.getId() == R.id.wireless_check_button) {
 					mWifiSwitchTitle.setTextColor(0xFF000000);
 				}
 			}
 
 		}
 	};
 }
