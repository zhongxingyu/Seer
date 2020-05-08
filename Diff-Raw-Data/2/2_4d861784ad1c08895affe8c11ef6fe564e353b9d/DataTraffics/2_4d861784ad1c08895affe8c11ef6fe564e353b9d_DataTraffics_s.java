 /*
  * Copyright (C) 2006 The Android Open Source Project
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
 
 package com.android.systemui.statusbar;
 
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.ContentObserver;
 import android.net.ConnectivityManager;
 import android.net.TrafficStats;
 import android.os.Handler;
 import android.provider.Settings;
 import android.telephony.PhoneStateListener;
 import android.telephony.ServiceState;
 import android.telephony.SignalStrength;
 import android.telephony.TelephonyManager;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.TextView;
 
 import java.text.DecimalFormat;
 
 /**
  * This widget display an analogic clock with two hands for hours and
  * minutes.
  */
 public class DataTraffics extends TextView {
     private boolean mAttached;
     private boolean mAirplaneOn;
     private int mCarrierColor;
     private String mDataFormatString;
     private long gMtx;
     private long gMrx;
     private long gOldMtx;
     private long gOldMrx;
     private DecimalFormat mDecimalFormater;
     private double BYTE_TO_KILOBIT = 0.0078125;
     private double KILOBIT_TO_MEGABIT = 0.0009765625;
    private int EXPECTED_SIZE_IN_BYTES = 1048;
 
     Handler mHandler;
 
     class SettingsObserver extends ContentObserver {
         SettingsObserver(Handler handler) {
             super(handler);
         }
 
         void observe() {
             ContentResolver resolver = mContext.getContentResolver();
             resolver.registerContentObserver(Settings.System.getUriFor(
                     Settings.System.STATUS_BAR_CARRIERCOLOR), false, this);
             resolver.registerContentObserver(Settings.System
                     .getUriFor(Settings.System.AIRPLANE_MODE_ON), false, this);
         }
 
         @Override public void onChange(boolean selfChange) {
             updateSettings();
         }
     }
 
     public DataTraffics(Context context) {
         this(context, null);
     }
 
     public DataTraffics(Context context, AttributeSet attrs) {
         this(context, attrs, 0);
     }
 
     public DataTraffics(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
 
         mHandler = new Handler();
         mDecimalFormater=new DecimalFormat("##.#");
         SettingsObserver settingsObserver = new SettingsObserver(mHandler);
         settingsObserver.observe();
 
         ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE))
                 .listen(mPhoneStateListener,
                           PhoneStateListener.LISTEN_SERVICE_STATE
                         | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                         | PhoneStateListener.LISTEN_CALL_STATE
                         | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                         | PhoneStateListener.LISTEN_DATA_ACTIVITY);
         updateSettings();
     }
 
     @Override
     protected void onAttachedToWindow() {
         super.onAttachedToWindow();
 
         if (!mAttached) {
             mAttached = true;
             IntentFilter filter = new IntentFilter();
 
             filter.addAction(Intent.ACTION_TIME_TICK);
             filter.addAction(Intent.ACTION_TIME_CHANGED);
             filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
             filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
             filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
             filter.addAction(ConnectivityManager.INET_CONDITION_ACTION);
             getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
         }
         updateDataTraffics();
         updateSettings();
     }
 
     @Override
     protected void onDetachedFromWindow() {
         super.onDetachedFromWindow();
         if (mAttached) {
             getContext().unregisterReceiver(mIntentReceiver);
             mAttached = false;
         }
     }
 
     private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             if (action.equals(Intent.ACTION_TIME_TICK) ||
                     action.equals(Intent.ACTION_TIME_CHANGED) ||
                     action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                     action.equals(Intent.ACTION_CONFIGURATION_CHANGED) ||
                     action.equals(ConnectivityManager.CONNECTIVITY_ACTION) ||
                     action.equals(ConnectivityManager.INET_CONDITION_ACTION)) {
                 updateDatas();
                 updateSettings();
             }
         }
     };
 
     final void updateDataTraffics() {
         long txBytes = gMtx - gOldMtx;
         long rxBytes = gMrx - gOldMrx;
         setText("Rx: "+getDatas(rxBytes)+" Tx: "+getDatas(txBytes));
         setTextColor(mCarrierColor);
     }
 
     private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
         @Override
         public void onSignalStrengthsChanged(SignalStrength signalStrength) {
         }
 
         @Override
         public void onServiceStateChanged(ServiceState state) {
             updateDatas();
         }
 
         @Override
         public void onCallStateChanged(int state, String incomingNumber) {
         }
 
         @Override
         public void onDataConnectionStateChanged(int state, int networkType) {
             updateDatas();
         }
 
         @Override
         public void onDataActivity(int direction) {
             updateDatas();
         }
     };
 
     private String getDatas(long what) {
         int y,z;
         String result;
         y = (int)what;
         z = (int)(y * BYTE_TO_KILOBIT);
         result = mDecimalFormater.format(z)+"KB/s";
         if (z > EXPECTED_SIZE_IN_KILOBIT) {
           z = (int)(y * BYTE_TO_KILOBIT * KILOBIT_TO_MEGABIT);
           result = mDecimalFormater.format(z)+"MB/s";
         }
         return result;
     }
 
     private boolean getDataState(Context context) {
         ConnectivityManager cm = (ConnectivityManager) context
             .getSystemService(Context.CONNECTIVITY_SERVICE);
         return cm.getMobileDataEnabled();
     }
 
     private long getTxBytes() {
         return TrafficStats.getTotalRxBytes();
     }
 
     private long getRxBytes() {
         return TrafficStats.getTotalTxBytes();
     }
 
     private void updateDatas() {
         if (!mAttached) return;
 
         gOldMtx = gMtx;
         gOldMrx = gMrx;
 
         gMtx = getTxBytes();
         gMrx = getRxBytes();
         updateDataTraffics();
     }
 
     private void updateSettings(){
         int defValuesColor = mContext.getResources().getInteger(com.android.internal.R.color.color_default_cyanmobile);
         mAirplaneOn = (Settings.System.getInt(mContext.getContentResolver(),
                     Settings.System.AIRPLANE_MODE_ON, 0) == 1);
         mCarrierColor = (Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.STATUS_BAR_CARRIERCOLOR, defValuesColor));
 
         if(getDataState(mContext))
             setVisibility(mAirplaneOn ? View.GONE : View.VISIBLE);
         else
             setVisibility(View.GONE);
     }
 }
 
