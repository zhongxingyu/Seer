 /*******************************************************************************
  * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
  * Copyright (C) 2012  WMG, University of Warwick
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You may obtain a copy of the GNU General Public License at 
  * <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     John Lawson - initial API and implementation
  ******************************************************************************/
 package uk.co.jwlawson.nof1.preferences;
 
 import uk.co.jwlawson.nof1.BuildConfig;
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.preference.DialogPreference;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 import android.widget.TimePicker;
 
 /**
  * @author John Lawson
  * 
  */
 public class TimePreference extends DialogPreference {
 
 	private static final String TAG = "Time Preference";
 	private static final boolean DEBUG = true && BuildConfig.DEBUG;
 
 	private int mLastHour = 0;
 	private int mLastMinute = 0;
 
 	private TimePicker mPicker;
 
 	public static int getHour(String time) {
 		String[] pieces = time.split(":");
 
 		return (Integer.parseInt(pieces[0]));
 	}
 
 	public static int getMinute(String time) {
 		String[] pieces = time.split(":");
 
 		return (Integer.parseInt(pieces[1]));
 	}
 
 	public TimePreference(Context ctxt) {
 		this(ctxt, null, 0);
 	}
 
 	public TimePreference(Context ctxt, AttributeSet attrs) {
		this(ctxt, attrs, 0);
 	}
 
 	public TimePreference(Context ctxt, AttributeSet attrs, int defStyle) {
 		super(ctxt, attrs, defStyle);
 
 		setPositiveButtonText("Set");
 		setNegativeButtonText("Cancel");
 
 		if (DEBUG) Log.d(TAG, "TimePref created");
 	}
 
 	@Override
 	protected View onCreateDialogView() {
 		// Fill dialog with time picker
 		mPicker = new TimePicker(getContext());
 
 		return (mPicker);
 	}
 
 	@Override
 	protected void onBindDialogView(View v) {
 		super.onBindDialogView(v);
 
 		mPicker.setCurrentHour(mLastHour);
 		mPicker.setCurrentMinute(mLastMinute);
 	}
 
 	@Override
 	protected void onDialogClosed(boolean positiveResult) {
 		super.onDialogClosed(positiveResult);
 
 		if (positiveResult) {
 			// Get time
 			mLastHour = mPicker.getCurrentHour();
 			mLastMinute = mPicker.getCurrentMinute();
 
 			String time = String.valueOf(mLastHour) + ":" + String.valueOf(mLastMinute);
 
 			if (callChangeListener(time)) {
 				// Save preference
 				persistString(time);
 			}
 			if (DEBUG) Log.d(TAG, "TimePref closed. Time set: " + time);
 		} else if (DEBUG) Log.d(TAG, "TimePref closed. Time not set");
 	}
 
 	@Override
 	protected Object onGetDefaultValue(TypedArray a, int index) {
 		return (a.getString(index));
 	}
 
 	@Override
 	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
 		String time = null;
 
 		if (restoreValue) {
 			// Get the stored value from preferences
 			if (defaultValue == null) {
 				time = getPersistedString("12:00");
 			} else {
 				time = getPersistedString(defaultValue.toString());
 			}
 		} else {
 			// Use default value
 			time = defaultValue.toString();
 		}
 
 		mLastHour = getHour(time);
 		mLastMinute = getMinute(time);
 	}
 }
