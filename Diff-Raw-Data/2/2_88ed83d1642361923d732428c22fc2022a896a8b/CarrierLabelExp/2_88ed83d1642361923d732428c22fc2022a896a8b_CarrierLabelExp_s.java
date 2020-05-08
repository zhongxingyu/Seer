 /*
  * Copyright (C) 2006 The Android Open Source Project
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
 
 package com.android.systemui.statusbar.carrierlabels;
 
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.ContentObserver;
 import android.os.Handler;
 import android.os.SystemProperties;
 import android.provider.CmSystem;
 import android.provider.Settings;
 import android.provider.Telephony;
 import android.util.AttributeSet;
 import android.util.Slog;
 import android.util.TypedValue;
 import android.content.res.Resources;
 import android.util.DisplayMetrics;
 import android.widget.TextView;
 
 import com.android.internal.R;
 import com.android.internal.telephony.TelephonyProperties;
 
 /**
  * This widget display the current network status or registered PLMN, and/or
  * SPN if available.
  */
 public class CarrierLabelExp extends TextView {
     private boolean mAttached;
 
     private boolean mShowSpn;
     private String mSpn;
     private boolean mShowPlmn;
     private boolean mAirplaneOn;
     private String mPlmn;
     private int mCarrierColor;
 
     private static final int TYPE_DEFAULT = 0;
 
     Handler mHandler;
 
     private class SettingsObserver extends ContentObserver {
         SettingsObserver(Handler handler) {
             super(handler);
         }
 
         void observe() {
             ContentResolver resolver = mContext.getContentResolver();
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.CARRIER_LABEL_TYPE),
                     false, this);
             resolver.registerContentObserver(Settings.System.getUriFor(
                     Settings.System.STATUS_BAR_CARRIERCOLOR), false, this);
             resolver.registerContentObserver(Settings.System
                     .getUriFor(Settings.System.AIRPLANE_MODE_ON), false, this);
             onChange(true);
         }
 
         @Override
         public void onChange(boolean selfChange) {
             updateSettings();
             updateNetworkName(mShowSpn, mSpn, mShowPlmn, mPlmn);
         }
     }
 
     public CarrierLabelExp(Context context) {
         this(context, null);
     }
 
     public CarrierLabelExp(Context context, AttributeSet attrs) {
         this(context, attrs, 0);
     }
 
     public CarrierLabelExp(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
 
         mHandler = new Handler();
         SettingsObserver settingsObserver = new SettingsObserver(mHandler);
         settingsObserver.observe();
 
         updateSettings();
         updateNetworkName(false, null, false, null);
     }
 
     @Override
     protected void onAttachedToWindow() {
         super.onAttachedToWindow();
 
         if (!mAttached) {
             mAttached = true;
             IntentFilter filter = new IntentFilter();
             filter.addAction(Telephony.Intents.SPN_STRINGS_UPDATED_ACTION);
             getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
         }
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
             if (Telephony.Intents.SPN_STRINGS_UPDATED_ACTION.equals(action)) {
                 updateNetworkName(intent.getBooleanExtra(Telephony.Intents.EXTRA_SHOW_SPN, false),
                         intent.getStringExtra(Telephony.Intents.EXTRA_SPN),
                         intent.getBooleanExtra(Telephony.Intents.EXTRA_SHOW_PLMN, false),
                         intent.getStringExtra(Telephony.Intents.EXTRA_PLMN));
             }
         }
     };
 
     void updateSettings() {
         ContentResolver resolver = mContext.getContentResolver();
         int defValuesColor = mContext.getResources().getInteger(com.android.internal.R.color.color_default_cyanmobile);
         mAirplaneOn = (Settings.System.getInt(mContext.getContentResolver(),
                     Settings.System.AIRPLANE_MODE_ON, 0) == 1);
         mCarrierColor = (Settings.System.getInt(resolver,
                 Settings.System.STATUS_BAR_CARRIERCOLOR, defValuesColor));
     }
 
     void updateNetworkName(boolean showSpn, String spn, boolean showPlmn, String plmn) {
         if (false) {
             Slog.d("CarrierLabelExp", "updateNetworkName showSpn=" + showSpn + " spn=" + spn
                     + " showPlmn=" + showPlmn + " plmn=" + plmn);
         }
 
         final String str;
         final boolean plmnValid = (showPlmn && plmn != null);
         final boolean spnValid = (showSpn && spn != null);
         final boolean haveSignal = plmnValid || spnValid;
 
         if (!haveSignal) {
             if (mAirplaneOn) {
                 str = "Airplane Mode";
             } else {
                 str = mContext.getResources().getString(com.android.internal.R.string.lockscreen_carrier_default);
             }
         } else {
             if (plmnValid && spnValid) {
                 str = plmn + "|" + spn;
             } else if (plmnValid) {
                 str = plmn;
             } else if (spnValid) {
                 str = spn;
             } else {
                str = "";
             }
         }
 
         setText(str);
         setTextColor(mCarrierColor);
     }
 
 }
