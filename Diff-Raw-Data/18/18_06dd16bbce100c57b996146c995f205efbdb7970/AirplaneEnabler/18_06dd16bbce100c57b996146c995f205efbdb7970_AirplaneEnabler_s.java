 /*
  * Copyright (C) 2007 The Android Open Source Project
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
 
 package com.android.settings.airplane;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.ContentObserver;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.Settings;
 import android.widget.CompoundButton;
 import android.widget.Switch;
 import android.os.SystemProperties;

 
 import com.android.internal.telephony.PhoneStateIntentReceiver;
 import com.android.internal.telephony.TelephonyProperties;
 
 public class AirplaneEnabler implements CompoundButton.OnCheckedChangeListener {
 	private final Context mContext;
 	private Switch mSwitch;
 	private boolean mStateMachineEvent;
 
 	private PhoneStateIntentReceiver mPhoneStateReceiver;
 
 	private static final int EVENT_SERVICE_STATE_CHANGED = 3;
 
 	public AirplaneEnabler(Context context, Switch switch_) {
 		mContext = context;
 		mSwitch = switch_;
 
 		mPhoneStateReceiver = new PhoneStateIntentReceiver(mContext, mHandler);
 		mPhoneStateReceiver.notifyServiceState(EVENT_SERVICE_STATE_CHANGED);
 	}
 
 	private Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case EVENT_SERVICE_STATE_CHANGED:
 				onAirplaneModeChanged();
 				break;
 			}
 		}
 	};
 
 	private ContentObserver mAirplaneModeObserver = new ContentObserver(
 			new Handler()) {
 		@Override
 		public void onChange(boolean selfChange) {
 			onAirplaneModeChanged();
 		}
 	};
 
 	/**
 	 * Called when we've received confirmation that the airplane mode was set.
 	 * TODO: We update the checkbox summary when we get notified that mobile
 	 * radio is powered up/down. We should not have dependency on one radio
 	 * alone. We need to do the following: - handle the case of wifi/bluetooth
 	 * failures - mobile does not send failure notification, fail on timeout.
 	 */
 	private void onAirplaneModeChanged() {
 		mSwitch.setChecked(isAirplaneModeOn(mContext));
 	}
 
 	public static boolean isAirplaneModeOn(Context context) {
		return Settings.System.getInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;
 	}
 
 	public void setSwitch(Switch _switch){
 		mSwitch = _switch;
 		mSwitch.setOnCheckedChangeListener(null);
 		mSwitch.setChecked(isAirplaneModeOn(mContext));
 		mSwitch.setOnCheckedChangeListener(this);
 	}
 
 	public void resume() {
 		mSwitch.setChecked(isAirplaneModeOn(mContext));
 		mPhoneStateReceiver.registerIntent();
 		mSwitch.setOnCheckedChangeListener(this);
 		mContext.getContentResolver().registerContentObserver(
				Settings.System.getUriFor(Settings.System.AIRPLANE_MODE_ON),
 				true, mAirplaneModeObserver);
 	}
 
 	public void pause() {
 		mPhoneStateReceiver.unregisterIntent();
 		mSwitch.setOnCheckedChangeListener(null);
 		mContext.getContentResolver().unregisterContentObserver(
 				mAirplaneModeObserver);
 	}
 
 	private void setAirplaneModeOn(boolean enabling) {
 		// Change the system setting
		Settings.System.putInt(mContext.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, enabling ? 1 : 0);
 		// Update the UI to reflect system setting
 		mSwitch.setChecked(enabling);
 
 		// Post the intent
 		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
 		intent.putExtra("state", enabling);
		mContext.sendBroadcast(intent);
 	}
 
 	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 		if (Boolean.parseBoolean(SystemProperties
 				.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
 			// In ECM mode, do not update database at this point
 		} else {
 			setAirplaneModeOn((Boolean) isChecked);
 		}
 	}
 
 	public void setAirplaneModeInECM(boolean isECMExit, boolean isAirplaneModeOn) {
 		if (isECMExit) {
 			// update database based on the current checkbox state
 			setAirplaneModeOn(isAirplaneModeOn);
 		} else {
 			// update summary
 			onAirplaneModeChanged();
 		}
 	}
 	
 }
