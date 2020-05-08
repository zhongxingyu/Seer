 /*
  * Copyright (C) 2008 The Android Open Source Project
  * Patched by Sven Dawitz; Copyright (C) 2011 CyanogenMod Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.systemui.statusbar.policy;
 
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.ContentObserver;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.Message;
 import android.os.RemoteException;
 import android.os.SystemProperties;
 import android.provider.Settings;
 import android.provider.Telephony;
 import android.telephony.PhoneStateListener;
 import android.telephony.ServiceState;
 import android.telephony.SignalStrength;
 import android.telephony.TelephonyManager;
 import android.util.Slog;
 import android.view.View;
 
 import com.android.internal.app.IBatteryStats;
 import com.android.internal.telephony.IccCard;
 import com.android.internal.telephony.TelephonyIntents;
 import com.android.internal.telephony.TelephonyProperties;
 import com.android.internal.telephony.cdma.EriInfo;
 import com.android.server.am.BatteryStatsService;
 import com.android.systemui.R;
 import android.net.wimax.WimaxManagerConstants;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This class contains all of the policy about which icons are installed in the status
  * bar at boot time.  It goes through the normal API for icons, even though it probably
  * strictly doesn't need to.
  */
 public class NetworkController {
     private static final String TAG = "NetworkController";
 
     private static final int INET_CONDITION_THRESHOLD = 50;
 
     private final Context mContext;
     private final IBatteryStats mBatteryStats;
 
     // phone
     private TelephonyManager mPhone;
     public int mPhoneSignalIconId;
     public int mWifiSignalIconId;
     public int mDataSignalIconId;
     public int mDataDirection;
     public int mWimaxSignalIconId;
     private final Handler mHandler;
     public static final int PHONE_SIGNAL_IS_AIRPLANE_MODE = 1;
     public static final int PHONE_SIGNAL_IS_NULL = 2;
     public static final int PHONE_SIGNAL_IS_NORMAL = 0;
 
     //***** Signal strength icons
     //GSM/UMTS
     private static final int[][] sSignalImages = {
         { R.drawable.ic_qs_signal_0,
           R.drawable.ic_qs_signal_1,
           R.drawable.ic_qs_signal_2,
           R.drawable.ic_qs_signal_3,
           R.drawable.ic_qs_signal_4 },
         { R.drawable.ic_qs_signal_full_0,
           R.drawable.ic_qs_signal_full_1,
           R.drawable.ic_qs_signal_full_2,
           R.drawable.ic_qs_signal_full_3,
           R.drawable.ic_qs_signal_full_4 }
     };
     private static final int[][] sSignalImages_r = {
         { R.drawable.ic_qs_signal_0,
           R.drawable.ic_qs_signal_1,
           R.drawable.ic_qs_signal_2,
           R.drawable.ic_qs_signal_3,
           R.drawable.ic_qs_signal_4 },
         { R.drawable.ic_qs_signal_full_0,
           R.drawable.ic_qs_signal_full_1,
           R.drawable.ic_qs_signal_full_2,
           R.drawable.ic_qs_signal_full_3,
           R.drawable.ic_qs_signal_full_4 }
     };
     private static final int[] sRoamingIndicatorImages_cdma = new int[] {
         R.drawable.stat_sys_roaming_cdma_0, //Standard Roaming Indicator
         // 1 is Standard Roaming Indicator OFF
         // TODO T: image never used, remove and put 0 instead?
         R.drawable.stat_sys_roaming_cdma_0,
 
         // 2 is Standard Roaming Indicator FLASHING
         // TODO T: image never used, remove and put 0 instead?
         R.drawable.stat_sys_roaming_cdma_0,
 
         // 3-12 Standard ERI
         R.drawable.stat_sys_roaming_cdma_0, //3
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
 
         // 13-63 Reserved for Standard ERI
         R.drawable.stat_sys_roaming_cdma_0, //13
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
 
         // 64-127 Reserved for Non Standard (Operator Specific) ERI
         R.drawable.stat_sys_roaming_cdma_0, //64
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0, //83
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0,
         R.drawable.stat_sys_roaming_cdma_0 //239
 
         // 240-255 Reserved
     };
 
     //***** Data connection icons
     private int[] mDataIconList = sDataNetType_g[0];
     //roaming
     private static final int[][] sDataNetType_r = {
             { R.drawable.ic_qs_signal_r,
               R.drawable.ic_qs_signal_r,
               R.drawable.ic_qs_signal_r,
               R.drawable.ic_qs_signal_r },
             { R.drawable.ic_qs_signal_full_r,
               R.drawable.ic_qs_signal_full_r,
               R.drawable.ic_qs_signal_full_r,
               R.drawable.ic_qs_signal_full_r }
         };
     //GSM/UMTS
     private static final int[][] sDataNetType_g = {
             { R.drawable.ic_qs_signal_g,
               R.drawable.ic_qs_signal_g,
               R.drawable.ic_qs_signal_g,
               R.drawable.ic_qs_signal_g },
             { R.drawable.ic_qs_signal_full_g,
               R.drawable.ic_qs_signal_full_g,
               R.drawable.ic_qs_signal_full_g,
               R.drawable.ic_qs_signal_full_g }
         };
     private static final int[][] sDataNetType_3g = {
             { R.drawable.ic_qs_signal_3g,
               R.drawable.ic_qs_signal_3g,
               R.drawable.ic_qs_signal_3g,
               R.drawable.ic_qs_signal_3g },
             { R.drawable.ic_qs_signal_full_3g,
               R.drawable.ic_qs_signal_full_3g,
               R.drawable.ic_qs_signal_full_3g,
               R.drawable.ic_qs_signal_full_3g }
         };
     private static final int[][] sDataNetType_e = {
             { R.drawable.ic_qs_signal_e,
               R.drawable.ic_qs_signal_e,
               R.drawable.ic_qs_signal_e,
               R.drawable.ic_qs_signal_e },
             { R.drawable.ic_qs_signal_full_e,
               R.drawable.ic_qs_signal_full_e,
               R.drawable.ic_qs_signal_full_e,
               R.drawable.ic_qs_signal_full_e }
         };
     //3.5G
     private static final int[][] sDataNetType_h = {
             { R.drawable.ic_qs_signal_h,
               R.drawable.ic_qs_signal_h,
               R.drawable.ic_qs_signal_h,
               R.drawable.ic_qs_signal_h },
             { R.drawable.ic_qs_signal_full_h,
               R.drawable.ic_qs_signal_full_h,
               R.drawable.ic_qs_signal_full_h,
               R.drawable.ic_qs_signal_full_h }
     };
     //CDMA
     // Use 3G icons for EVDO data and 1x icons for 1XRTT data
     private static final int[][] sDataNetType_1x = {
             { R.drawable.ic_qs_signal_1x,
               R.drawable.ic_qs_signal_1x,
               R.drawable.ic_qs_signal_1x,
               R.drawable.ic_qs_signal_1x },
             { R.drawable.ic_qs_signal_full_1x,
               R.drawable.ic_qs_signal_full_1x,
               R.drawable.ic_qs_signal_full_1x,
               R.drawable.ic_qs_signal_full_1x }
             };
 
     //LTE, + stuff like HSPAP+, which is still
     //3.5G but carriers like to pretend it's 4G
     private static final int[][] sDataNetType_4g = {
             { R.drawable.ic_qs_signal_4g,
               R.drawable.ic_qs_signal_4g,
               R.drawable.ic_qs_signal_4g,
               R.drawable.ic_qs_signal_4g },
             { R.drawable.ic_qs_signal_full_4g,
               R.drawable.ic_qs_signal_full_4g,
               R.drawable.ic_qs_signal_full_4g,
               R.drawable.ic_qs_signal_full_4g }
     };
 
 
     // Assume it's all good unless we hear otherwise.  We don't always seem
     // to get broadcasts that it *is* there.
     IccCard.State mSimState = IccCard.State.READY;
     int mPhoneState = TelephonyManager.CALL_STATE_IDLE;
     int mDataState = TelephonyManager.DATA_DISCONNECTED;
     int mDataActivity = TelephonyManager.DATA_ACTIVITY_NONE;
     ServiceState mServiceState;
     SignalStrength mSignalStrength;
 
     // flag for signal strength behavior
     private boolean mAlwaysUseCdmaRssi;
 
     // data connection
     private boolean mDataIconVisible;
     private boolean mHspaDataDistinguishable;
 
     // wifi
     private static final int[][] sWifiSignalImages = {
             { R.drawable.ic_qs_wifi_1,
               R.drawable.ic_qs_wifi_2,
               R.drawable.ic_qs_wifi_3,
               R.drawable.ic_qs_wifi_4 },
             { R.drawable.ic_qs_wifi_full_1,
               R.drawable.ic_qs_wifi_full_2,
               R.drawable.ic_qs_wifi_full_3,
               R.drawable.ic_qs_wifi_full_4 }
         };
     private static final int sWifiTemporarilyNotConnectedImage =
             R.drawable.ic_qs_wifi_no_network;
 
     private int mLastWifiSignalLevel = -1;
     public boolean mIsWifiConnected = false;
     public boolean mMobileDataEnable = false;
     public String mWifiSsid;
 
     //4G
     private static final int[][] sWimaxSignalImages = {
             { R.drawable.stat_sys_data_wimax_signal_0,
               R.drawable.stat_sys_data_wimax_signal_1,
               R.drawable.stat_sys_data_wimax_signal_2,
               R.drawable.stat_sys_data_wimax_signal_3 },
             { R.drawable.stat_sys_data_wimax_signal_0_fully,
               R.drawable.stat_sys_data_wimax_signal_1_fully,
               R.drawable.stat_sys_data_wimax_signal_2_fully,
               R.drawable.stat_sys_data_wimax_signal_3_fully }
         };
     private static final int sWimaxDisconnectedImg =
             R.drawable.stat_sys_data_wimax_signal_disconnected;
     private static final int sWimaxIdleImg = R.drawable.stat_sys_data_wimax_signal_idle;
     private boolean mIsWimaxEnabled = false;
     private int mWimaxSignal = 0;
     private int mWimaxState = 0;
     private int mWimaxExtraState = 0;
     final WifiManager mWifiManager;
     private boolean mShowFourG;
 
     // state of inet connection - 0 not connected, 100 connected
     private int mInetCondition = 0;
 
     ArrayList<NetworkSignalChangedCallback> mSignalsChangedCallbacks =
             new ArrayList<NetworkSignalChangedCallback>();
 
     public interface NetworkSignalChangedCallback {
         public void onWifiSignalChanged(boolean mIsWifiConnected, int mWifiSignalIconId, String wifiDesc);
         public void onMobileDataSignalChanged(boolean mMobileDataEnable, int mPhoneSignalIconId, int mDataDirection, int mDataSignalIconId);
     }
 
     public void addNetworkSignalChangedCallback(NetworkSignalChangedCallback cb) {
         mSignalsChangedCallbacks.add(cb);
         notifySignalsChangedCallbacks(cb);
     }
 
     private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) ||
                     action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION) ||
                     action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
                 updateWifi(intent);
                 refreshViews();
             } else if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
                 updateSimState(intent);
                 refreshViews();
             } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION) ||
                      action.equals(ConnectivityManager.INET_CONDITION_ACTION)) {
                 // TODO - stop using other means to get wifi/mobile info
                 updateConnectivity(intent);
                 refreshViews();
             } else if (action.equals(WimaxManagerConstants.WIMAX_ENABLED_STATUS_CHANGED) ||
                      action.equals(WimaxManagerConstants.SIGNAL_LEVEL_CHANGED_ACTION) ||
                      action.equals(WimaxManagerConstants.WIMAX_STATE_CHANGED_ACTION) ||
                      action.equals(WimaxManagerConstants.NETWORK_STATE_CHANGED_ACTION) ||
                      action.equals(WimaxManagerConstants.WIMAX_ENABLED_CHANGED_ACTION) ||
                      action.equals(WimaxManagerConstants.RSSI_CHANGED_ACTION)) {
                 updateWiMAX(intent);
                 refreshViews();
            } else if (Intent.ACTION_BATTERY_CHANGED.equals(action) ||
                 Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
                 refreshViews();
             }
         }
     };
 
     class SettingsObserver extends ContentObserver {
         SettingsObserver(Handler handler) {
             super(handler);
         }
 
         void observe() {
             ContentResolver resolver = mContext.getContentResolver();
             resolver.registerContentObserver(Settings.System
                     .getUriFor(Settings.System.STATUS_BAR_FOURG), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.AIRPLANE_MODE_ON), false, this);
             onChange(true);
         }
 
         @Override 
         public void onChange(boolean selfChange) {
             updateSettings();
         }
     }
 
     public NetworkController(Context context) {
         mContext = context;
         mHandler = new Handler();
         mSignalStrength = new SignalStrength();
        mBatteryStats = BatteryStatsService.getService();
 
         SettingsObserver settingsObserver = new SettingsObserver(mHandler);
         settingsObserver.observe();
         updateSettings();
 
         // phone_signal
         mPhone = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
         mPhoneSignalIconId = R.drawable.ic_qs_signal_no_signal;
         mAlwaysUseCdmaRssi = mContext.getResources().getBoolean(
             com.android.internal.R.bool.config_alwaysUseCdmaRssi);
 
         // register for phone state notifications.
         ((TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE))
                 .listen(mPhoneStateListener,
                           PhoneStateListener.LISTEN_SERVICE_STATE
                         | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                         | PhoneStateListener.LISTEN_CALL_STATE
                         | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                         | PhoneStateListener.LISTEN_DATA_ACTIVITY);
 
         // data_connection
         mDataSignalIconId = R.drawable.ic_qs_signal_g;
         mDataDirection = 0;
 
         // wifi
         mWifiSignalIconId = sWifiSignalImages[0][0];
         // wifi will get updated by the sticky intents
         mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 
         // wimax
         //enable/disable wimax depending on the value in config.xml
         boolean isWimaxEnabled = mContext.getResources().getBoolean(
                 com.android.internal.R.bool.config_wimaxEnabled);
         if (isWimaxEnabled) {
             mWimaxSignalIconId = sWimaxDisconnectedImg;
         }
 
         IntentFilter filter = new IntentFilter();
 
         // Register for Intent broadcasts for...
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
         filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
         filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
         filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
         filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
         filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
         filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
         filter.addAction(Telephony.Intents.SPN_STRINGS_UPDATED_ACTION);
         filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
         filter.addAction(ConnectivityManager.INET_CONDITION_ACTION);
         filter.addAction(WimaxManagerConstants.WIMAX_STATE_CHANGED_ACTION);
         filter.addAction(WimaxManagerConstants.SIGNAL_LEVEL_CHANGED_ACTION);
         filter.addAction(WimaxManagerConstants.WIMAX_ENABLED_STATUS_CHANGED);
         filter.addAction(WimaxManagerConstants.WIMAX_ENABLED_CHANGED_ACTION);
         filter.addAction(WimaxManagerConstants.NETWORK_STATE_CHANGED_ACTION);
         filter.addAction(WimaxManagerConstants.RSSI_CHANGED_ACTION);
         mContext.registerReceiver(mIntentReceiver, filter, null, mHandler);
 
         // load config to determine if to distinguish Hspa data icon
         try {
             mHspaDataDistinguishable = mContext.getResources().getBoolean(
                     R.bool.config_hspa_data_distinguishable);
         } catch (Exception e) {
             mHspaDataDistinguishable = false;
         }
     }
 
     public void notifySignalsChangedCallbacks(NetworkSignalChangedCallback cb) {
         String wifiDesc = mIsWifiConnected ?
                 mWifiSsid : null;
         // only show wifi in the cluster if connected or if wifi-only
         cb.onWifiSignalChanged(mIsWifiConnected, mWifiSignalIconId, wifiDesc);
         cb.onMobileDataSignalChanged(mMobileDataEnable, mPhoneSignalIconId, mDataDirection, mDataSignalIconId);
     }
 
     private void updateConnectivity(Intent intent) {
         NetworkInfo info = (NetworkInfo)(intent.getParcelableExtra(
                 ConnectivityManager.EXTRA_NETWORK_INFO));
         int connectionStatus = intent.getIntExtra(ConnectivityManager.EXTRA_INET_CONDITION, 0);
 
         int inetCondition = (connectionStatus > INET_CONDITION_THRESHOLD ? 1 : 0);
 
         switch (info.getType()) {
         case ConnectivityManager.TYPE_MOBILE:
             mInetCondition = inetCondition;
             updateDataNetType(info.getSubtype());
             updateDataIcon();
             updateSignalStrength(); // apply any change in connectionStatus
             break;
         case ConnectivityManager.TYPE_WIFI:
             mInetCondition = inetCondition;
             if (info.isConnected()) {
                 mIsWifiConnected = true;
                 int iconId = sWifiSignalImages[mInetCondition][0];
                 if (mLastWifiSignalLevel == -1) {
                     iconId = sWifiSignalImages[mInetCondition][0];
                 } else {
                     iconId = sWifiSignalImages[mInetCondition][mLastWifiSignalLevel];
                 }
                 mWifiSignalIconId = iconId;
             } else {
                 mLastWifiSignalLevel = -1;
                 mIsWifiConnected = false;
                 mWifiSignalIconId = sWifiSignalImages[0][0];
             }
             updateSignalStrength(); // apply any change in mInetCondition
             break;
         case ConnectivityManager.TYPE_WIMAX:
             mInetCondition = inetCondition;
             updateWiMAX(intent);
             break;
         }
     }
 
     private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
         @Override
         public void onSignalStrengthsChanged(SignalStrength signalStrength) {
             mSignalStrength = signalStrength;
             updateSignalStrength();
             updateSettings();
         }
 
         @Override
         public void onServiceStateChanged(ServiceState state) {
             mServiceState = state;
             updateSignalStrength();
             updateCdmaRoamingIcon(state);
             updateDataIcon();
             updateSettings();
         }
 
         @Override
         public void onCallStateChanged(int state, String incomingNumber) {
             // In cdma, if a voice call is made, RSSI should switch to 1x.
             if (isCdma()) {
                 updateSignalStrength();
             }
             updateSettings();
         }
 
         @Override
         public void onDataConnectionStateChanged(int state, int networkType) {
             mDataState = state;
             updateDataNetType(networkType);
             updateDataIcon();
             updateSettings();
         }
 
         @Override
         public void onDataActivity(int direction) {
             mDataActivity = direction;
             updateDataIcon();
             updateSettings();
         }
     };
 
     private final void updateSimState(Intent intent) {
         String stateExtra = intent.getStringExtra(IccCard.INTENT_KEY_ICC_STATE);
         if (IccCard.INTENT_VALUE_ICC_ABSENT.equals(stateExtra)) {
             mSimState = IccCard.State.ABSENT;
         }
         else if (IccCard.INTENT_VALUE_ICC_READY.equals(stateExtra)) {
             mSimState = IccCard.State.READY;
         }
         else if (IccCard.INTENT_VALUE_ICC_LOCKED.equals(stateExtra)) {
             final String lockedReason = intent.getStringExtra(IccCard.INTENT_KEY_LOCKED_REASON);
             if (IccCard.INTENT_VALUE_LOCKED_ON_PIN.equals(lockedReason)) {
                 mSimState = IccCard.State.PIN_REQUIRED;
             }
             else if (IccCard.INTENT_VALUE_LOCKED_ON_PUK.equals(lockedReason)) {
                 mSimState = IccCard.State.PUK_REQUIRED;
             }
             else {
                 mSimState = IccCard.State.NETWORK_LOCKED;
             }
         } else {
             mSimState = IccCard.State.UNKNOWN;
         }
         updateDataIcon();
     }
 
     private boolean isCdma() {
         return (mSignalStrength != null) && !mSignalStrength.isGsm();
     }
 
     private boolean isEvdo() {
         return ( (mServiceState != null)
                  && ((mServiceState.getRadioTechnology()
                         == ServiceState.RADIO_TECHNOLOGY_EVDO_0)
                      || (mServiceState.getRadioTechnology()
                         == ServiceState.RADIO_TECHNOLOGY_EVDO_A)
                      || (mServiceState.getRadioTechnology()
                         == ServiceState.RADIO_TECHNOLOGY_EVDO_B)));
     }
 
     private boolean hasService() {
         if (mServiceState != null) {
             switch (mServiceState.getState()) {
                 case ServiceState.STATE_OUT_OF_SERVICE:
                 case ServiceState.STATE_POWER_OFF:
                     return false;
                 default:
                     return true;
             }
         } else {
             return false;
         }
     }
 
     private final void updateSignalStrength() {
         int iconLevel = -1;
         int[] iconList;
 
         // Display signal strength while in "emergency calls only" mode
         if (mServiceState == null || (!hasService() && !mServiceState.isEmergencyOnly())) {
             if (Settings.System.getInt(mContext.getContentResolver(),
                     Settings.System.AIRPLANE_MODE_ON, 0) == 1) {
                 mPhoneSignalIconId = R.drawable.ic_qs_airplane_on;
                 updateSignalStrengthDbm(PHONE_SIGNAL_IS_AIRPLANE_MODE);
             } else {
                 mPhoneSignalIconId = R.drawable.ic_qs_signal_no_signal;
                 updateSignalStrengthDbm(PHONE_SIGNAL_IS_NULL);
             }
             return;
         }
 
         // calculate and update the dBm value of the signal strength
         updateSignalStrengthDbm(PHONE_SIGNAL_IS_NORMAL);
 
         if (!isCdma()) {
             int asu = mSignalStrength.getGsmSignalStrength();
 
             // ASU ranges from 0 to 31 - TS 27.007 Sec 8.5
             // asu = 0 (-113dB or less) is very weak
             // signal, its better to show 0 bars to the user in such cases.
             // asu = 99 is a special case, where the signal strength is unknown.
             if (asu <= 2 || asu == 99) iconLevel = 0;
             else if (asu >= 12) iconLevel = 4;
             else if (asu >= 8)  iconLevel = 3;
             else if (asu >= 5)  iconLevel = 2;
             else iconLevel = 1;
 
             // Though mPhone is a Manager, this call is not an IPC
             if (mPhone.isNetworkRoaming()) {
                 iconList = sSignalImages_r[mInetCondition];
             } else {
                 iconList = sSignalImages[mInetCondition];
             }
         } else {
             iconList = sSignalImages[mInetCondition];
 
             // If 3G(EV) and 1x network are available than 3G should be
             // displayed, displayed RSSI should be from the EV side.
             // If a voice call is made then RSSI should switch to 1x.
 
             // Samsung CDMA devices handle signal strength display differently
             // relying only on cdmaDbm - thanks Adr0it for the assistance here
             if (SystemProperties.get("ro.ril.samsung_cdma").equals("true")) {
                 final int cdmaDbm = mSignalStrength.getCdmaDbm();
                 if (cdmaDbm >= -75) iconLevel = 4;
                 else if (cdmaDbm >= -85) iconLevel = 3;
                 else if (cdmaDbm >= -95) iconLevel = 2;
                 else if (cdmaDbm >= -100) iconLevel = 1;
                 else iconLevel = 0;
             } else {
                 if ((mPhoneState == TelephonyManager.CALL_STATE_IDLE) && isEvdo()
                     && !mAlwaysUseCdmaRssi) {
                     iconLevel = getEvdoLevel();
                     if (false) {
                         Slog.d(TAG, "use Evdo level=" + iconLevel + " to replace Cdma Level=" + getCdmaLevel());
                     }
                 } else {
                     if ((mPhoneState == TelephonyManager.CALL_STATE_IDLE) && isEvdo()){
                         iconLevel = getEvdoLevel();
                         if (false) {
                             Slog.d(TAG, "use Evdo level=" + iconLevel + " to replace Cdma Level=" + getCdmaLevel());
                         }
                     } else {
                         iconLevel = getCdmaLevel();
                     }
                 }
             }
         }
         mPhoneSignalIconId = iconList[iconLevel];
     }
 
     private int getCdmaLevel() {
         final int cdmaDbm = mSignalStrength.getCdmaDbm();
         final int cdmaEcio = mSignalStrength.getCdmaEcio();
         int levelDbm = 0;
         int levelEcio = 0;
 
         if (cdmaDbm >= -75) levelDbm = 4;
         else if (cdmaDbm >= -85) levelDbm = 3;
         else if (cdmaDbm >= -95) levelDbm = 2;
         else if (cdmaDbm >= -100) levelDbm = 1;
         else levelDbm = 0;
 
         // Ec/Io are in dB*10
         if (cdmaEcio >= -90) levelEcio = 4;
         else if (cdmaEcio >= -110) levelEcio = 3;
         else if (cdmaEcio >= -130) levelEcio = 2;
         else if (cdmaEcio >= -150) levelEcio = 1;
         else levelEcio = 0;
 
         return (levelDbm < levelEcio) ? levelDbm : levelEcio;
     }
 
     private int getEvdoLevel() {
         int evdoDbm = mSignalStrength.getEvdoDbm();
         int evdoSnr = mSignalStrength.getEvdoSnr();
         int levelEvdoDbm = 0;
         int levelEvdoSnr = 0;
 
         if (evdoDbm >= -65) levelEvdoDbm = 4;
         else if (evdoDbm >= -75) levelEvdoDbm = 3;
         else if (evdoDbm >= -90) levelEvdoDbm = 2;
         else if (evdoDbm >= -105) levelEvdoDbm = 1;
         else levelEvdoDbm = 0;
 
         if (evdoSnr >= 7) levelEvdoSnr = 4;
         else if (evdoSnr >= 5) levelEvdoSnr = 3;
         else if (evdoSnr >= 3) levelEvdoSnr = 2;
         else if (evdoSnr >= 1) levelEvdoSnr = 1;
         else levelEvdoSnr = 0;
 
         return (levelEvdoDbm < levelEvdoSnr) ? levelEvdoDbm : levelEvdoSnr;
     }
 
     public void updateSignalStrengthDbm(int phoneSignalStatus) {
         int dBm = -1;
 
         if(!mSignalStrength.isGsm()) {
             dBm = mSignalStrength.getCdmaDbm();
         } else {
             int gsmSignalStrength = mSignalStrength.getGsmSignalStrength();
             int asu = (gsmSignalStrength == 99 ? -1 : gsmSignalStrength);
             if (asu != -1) {
                 dBm = -113 + 2*asu;
             }
         }
 
         Intent dbmIntent = new Intent(Intent.ACTION_SIGNAL_DBM_CHANGED);
         dbmIntent.putExtra("dbm", dBm);
         dbmIntent.putExtra("signal_status", phoneSignalStatus);
         mContext.sendBroadcast(dbmIntent);
     }
 
     private final void updateDataNetType(int net) {
         ContentResolver resolver = mContext.getContentResolver();
 
         mShowFourG = (Settings.System.getInt(resolver,
                 Settings.System.STATUS_BAR_FOURG, 0) == 1);
 
         switch (net) {
         case TelephonyManager.NETWORK_TYPE_EDGE:
             mDataIconList = sDataNetType_e[mInetCondition];
             break;
         case TelephonyManager.NETWORK_TYPE_UMTS:
             if (mShowFourG) {
                 mDataIconList = sDataNetType_4g[mInetCondition];
             } else {
                 mDataIconList = sDataNetType_3g[mInetCondition];
             }
             break;
         case TelephonyManager.NETWORK_TYPE_HSDPA:
         case TelephonyManager.NETWORK_TYPE_HSUPA:
         case TelephonyManager.NETWORK_TYPE_HSPA:
             if (mHspaDataDistinguishable) {
                 if (mShowFourG) {
                     mDataIconList = sDataNetType_4g[mInetCondition];
                 } else {
                     mDataIconList = sDataNetType_h[mInetCondition];
                 }
             } else {
                 if (mShowFourG) {
                     mDataIconList = sDataNetType_4g[mInetCondition];
                 } else {
                     mDataIconList = sDataNetType_3g[mInetCondition];
                 }
             }
             break;
         case TelephonyManager.NETWORK_TYPE_CDMA:
             // display 1xRTT for IS95A/B
             mDataIconList = sDataNetType_1x[mInetCondition];
             break;
         case TelephonyManager.NETWORK_TYPE_1xRTT:
             mDataIconList = sDataNetType_1x[mInetCondition];
             break;
         case TelephonyManager.NETWORK_TYPE_EVDO_0: //fall through
         case TelephonyManager.NETWORK_TYPE_EVDO_A:
         case TelephonyManager.NETWORK_TYPE_EVDO_B:
         case TelephonyManager.NETWORK_TYPE_EHRPD:
             if (mShowFourG) {
                 mDataIconList = sDataNetType_4g[mInetCondition];
             } else {
                 mDataIconList = sDataNetType_3g[mInetCondition];
             }
             break;
         case TelephonyManager.NETWORK_TYPE_LTE:
         case TelephonyManager.NETWORK_TYPE_HSPAP:
             mDataIconList = sDataNetType_4g[mInetCondition];
             break;
         default:
             mDataIconList = sDataNetType_g[mInetCondition];
         break;
         }
 
         if (isCdma()) {
             if (isCdmaEri()) {
                 mDataIconList = sDataNetType_r[mInetCondition];
             }
         } else if (mPhone.isNetworkRoaming()) {
                 mDataIconList = sDataNetType_r[mInetCondition];
         }
     }
 
     private final void updateDataIcon() {
         int iconId;
         boolean visible = true;
 
         if (!isCdma()) {
             // GSM case, we have to check also the sim state
             if (mSimState == IccCard.State.READY || mSimState == IccCard.State.UNKNOWN) {
                 if (hasService() && mDataState == TelephonyManager.DATA_CONNECTED) {
                     switch (mDataActivity) {
                         case TelephonyManager.DATA_ACTIVITY_IN:
                             iconId = mDataIconList[1];
                             mDataDirection = R.drawable.ic_qs_signal_in;
                             break;
                         case TelephonyManager.DATA_ACTIVITY_OUT:
                             iconId = mDataIconList[2];
                             mDataDirection = R.drawable.ic_qs_signal_out;
                             break;
                         case TelephonyManager.DATA_ACTIVITY_INOUT:
                             iconId = mDataIconList[3];
                             mDataDirection = R.drawable.ic_qs_signal_inout;
                             break;
                         default:
                             iconId = mDataIconList[0];
                             mDataDirection = 0;
                             break;
                     }
                     mDataSignalIconId = iconId;
                 } else {
                     visible = false;
                 }
             } else {
                 mDataSignalIconId = R.drawable.stat_sys_no_sim;
                 mDataDirection = 0;
             }
         } else {
             // CDMA case, mDataActivity can be also DATA_ACTIVITY_DORMANT
             if (hasService() && mDataState == TelephonyManager.DATA_CONNECTED) {
                 switch (mDataActivity) {
                     case TelephonyManager.DATA_ACTIVITY_IN:
                         iconId = mDataIconList[1];
                         mDataDirection = R.drawable.ic_qs_signal_in;
                         break;
                     case TelephonyManager.DATA_ACTIVITY_OUT:
                         iconId = mDataIconList[2];
                         mDataDirection = R.drawable.ic_qs_signal_out;
                         break;
                     case TelephonyManager.DATA_ACTIVITY_INOUT:
                         iconId = mDataIconList[3];
                         mDataDirection = R.drawable.ic_qs_signal_inout;
                         break;
                     case TelephonyManager.DATA_ACTIVITY_DORMANT:
                     default:
                         iconId = mDataIconList[0];
                         mDataDirection = 0;
                         break;
                 }
                 mDataSignalIconId = iconId;
             } else {
                 visible = false;
             }
         }
 
         long ident = Binder.clearCallingIdentity();
         try {
             mBatteryStats.notePhoneDataConnectionState(mPhone.getNetworkType(), visible);
         } catch (RemoteException e) {
         } finally {
             Binder.restoreCallingIdentity(ident);
         }
 
         mMobileDataEnable = visible;
     }
 
     private final void updateWifi(Intent intent) {
 
         final String action = intent.getAction();
 
         if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
             final boolean enabled = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                     WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED;
         } else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
             final boolean enabled = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED,
                                                            false);
         } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
             final NetworkInfo networkInfo = (NetworkInfo)
                     intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
             boolean wasConnected = mIsWifiConnected;
             mIsWifiConnected = networkInfo != null && networkInfo.isConnected();
             // If we just connected, grab the inintial signal strength and ssid
             if (mIsWifiConnected && !wasConnected) {
                 // try getting it out of the intent first
                 WifiInfo info = (WifiInfo) intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                 if (info == null) {
                     info = mWifiManager.getConnectionInfo();
                 }
                 if (info != null) {
                     mWifiSsid = huntForSsid(info);
                 } else {
                     mWifiSsid = null;
                 }
             } else if (!mIsWifiConnected) {
                 mWifiSsid = null;
             }
         } else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
             int iconId;
             final int newRssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -200);
             int newSignalLevel = WifiManager.calculateSignalLevel(newRssi, sWifiSignalImages[0].length);
             if (newSignalLevel != mLastWifiSignalLevel) {
                 mLastWifiSignalLevel = newSignalLevel;
                 if (mIsWifiConnected) {
                     iconId = sWifiSignalImages[mInetCondition][newSignalLevel];
                 } else {
                     iconId = sWifiTemporarilyNotConnectedImage;
                 }
                 mWifiSignalIconId = iconId;
             }
         }
     }
 
     private final void updateWiMAX(Intent intent) {
         final String action = intent.getAction();
         int iconId = sWimaxDisconnectedImg;
 
         if (action.equals(WimaxManagerConstants.WIMAX_ENABLED_STATUS_CHANGED)) {
             int wimaxStatus = intent.getIntExtra(WimaxManagerConstants.EXTRA_WIMAX_STATUS,
                     WimaxManagerConstants.WIMAX_STATUS_DISABLED);
             switch(wimaxStatus) {
                 case WimaxManagerConstants.WIMAX_STATUS_ENABLED:
                     mIsWimaxEnabled = true;
                     break;
                 case WimaxManagerConstants.WIMAX_STATUS_DISABLED:
                     mIsWimaxEnabled = false;
                     break;
             }
         } else if (action.equals(WimaxManagerConstants.WIMAX_ENABLED_CHANGED_ACTION)) {
             int wimaxStatus = intent.getIntExtra(WimaxManagerConstants.CURRENT_WIMAX_ENABLED_STATE,
                     WimaxManagerConstants.WIMAX_ENABLED_STATE_UNKNOWN);
             mIsWimaxEnabled = (wimaxStatus == WimaxManagerConstants.WIMAX_ENABLED_STATE_ENABLED);
         } else if (action.equals(WimaxManagerConstants.SIGNAL_LEVEL_CHANGED_ACTION)) {
             mWimaxSignal = intent.getIntExtra(WimaxManagerConstants.EXTRA_NEW_SIGNAL_LEVEL, 0);
         } else if (action.equals(WimaxManagerConstants.RSSI_CHANGED_ACTION)) {
             int rssi = intent.getIntExtra(WimaxManagerConstants.EXTRA_NEW_RSSI_LEVEL, -200);
             Slog.d(TAG, "WiMAX RSSI: " + rssi);
             if (rssi >= 3) {
                 mWimaxSignal = 3;
             } else if (rssi <= 0) {
                 mWimaxSignal = 0;
             } else {
                 mWimaxSignal = rssi;
             }
         } else if (action.equals(WimaxManagerConstants.WIMAX_STATE_CHANGED_ACTION)) {
             mWimaxState = intent.getIntExtra(WimaxManagerConstants.EXTRA_WIMAX_STATE,
                     WimaxManagerConstants.WIMAX_STATE_UNKNOWN);
             mWimaxExtraState = intent.getIntExtra(
                     WimaxManagerConstants.EXTRA_WIMAX_STATE_DETAIL,
                     WimaxManagerConstants.WIMAX_DEREGISTRATION);
         } else if (action.equals(WimaxManagerConstants.NETWORK_STATE_CHANGED_ACTION)) {
             final NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WimaxManagerConstants.EXTRA_NETWORK_INFO);
             if (networkInfo != null && networkInfo.isConnected()) {
                 mWimaxState = WimaxManagerConstants.WIMAX_STATE_CONNECTED;
                 mWimaxExtraState = WimaxManagerConstants.WIMAX_STATE_UNKNOWN;
             } else if (networkInfo != null && networkInfo.isAvailable()) {
                 mWimaxState = WimaxManagerConstants.WIMAX_STATE_CONNECTED;
                 mWimaxExtraState = WimaxManagerConstants.WIMAX_IDLE;
             } else {
                 mWimaxState = WimaxManagerConstants.WIMAX_STATE_DISCONNECTED;
                 mWimaxExtraState = WimaxManagerConstants.WIMAX_STATE_UNKNOWN;
             }
         }
         switch(mWimaxState) {
             case WimaxManagerConstants.WIMAX_STATE_DISCONNECTED:
                 iconId = sWimaxDisconnectedImg;
                 break;
             case WimaxManagerConstants.WIMAX_STATE_CONNECTED:
                 if(mWimaxExtraState == WimaxManagerConstants.WIMAX_IDLE) {
                     iconId = sWimaxIdleImg;
                 } else {
                     iconId = sWimaxSignalImages[mInetCondition][mWimaxSignal];
                 }
                 break;
         }
         if (mIsWimaxEnabled) mWimaxSignalIconId = iconId;
     }
 
     private boolean isCdmaEri() {
         if (mServiceState != null) {
             final int iconIndex = mServiceState.getCdmaEriIconIndex();
             if (iconIndex != EriInfo.ROAMING_INDICATOR_OFF) {
                 final int iconMode = mServiceState.getCdmaEriIconMode();
                 if (iconMode == EriInfo.ROAMING_ICON_MODE_NORMAL
                         || iconMode == EriInfo.ROAMING_ICON_MODE_FLASH) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     private final void updateCdmaRoamingIcon(ServiceState state) {
         if (!hasService()) {
             return;
         }
 
         if (!isCdma()) {
             return;
         }
 
         int[] iconList = sRoamingIndicatorImages_cdma;
         int iconIndex = state.getCdmaEriIconIndex();
         int iconMode = state.getCdmaEriIconMode();
 
         if (iconIndex == -1) {
             Slog.e(TAG, "getCdmaEriIconIndex returned null, skipping ERI icon update");
             return;
         }
 
         if (iconMode == -1) {
             Slog.e(TAG, "getCdmeEriIconMode returned null, skipping ERI icon update");
             return;
         }
 
         if (iconIndex == EriInfo.ROAMING_INDICATOR_OFF) {
             if (false) Slog.v(TAG, "Cdma ROAMING_INDICATOR_OFF, removing ERI icon");
             return;
         }
 
         switch (iconMode) {
             case EriInfo.ROAMING_ICON_MODE_NORMAL:
                 if (iconIndex >= iconList.length) {
                     Slog.e(TAG, "unknown iconIndex " + iconIndex + ", skipping ERI icon update");
                     return;
                 }
                 break;
             case EriInfo.ROAMING_ICON_MODE_FLASH:
                 break;
 
         }
     }
 
     private String huntForSsid(WifiInfo info) {
         String ssid = info.getSSID();
         if (ssid != null) {
             return ssid;
         }
         // OK, it's not in the connectionInfo; we have to go hunting for it
         List<WifiConfiguration> networks = mWifiManager.getConfiguredNetworks();
         for (WifiConfiguration net : networks) {
             if (net.networkId == info.getNetworkId()) {
                 return net.SSID;
             }
         }
         return null;
     }
 
     public void refreshViews() {
         for (NetworkSignalChangedCallback cb : mSignalsChangedCallbacks) {
              notifySignalsChangedCallbacks(cb);
         }
     }
 
     private void updateSettings() {
         ContentResolver resolver = mContext.getContentResolver();
         mShowFourG = (Settings.System.getInt(resolver,
                 Settings.System.STATUS_BAR_FOURG, 0) == 1);
         refreshViews();
     }
 }
