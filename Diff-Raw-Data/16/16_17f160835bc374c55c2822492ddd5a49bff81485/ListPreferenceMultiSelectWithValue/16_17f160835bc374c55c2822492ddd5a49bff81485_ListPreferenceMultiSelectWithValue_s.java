 /**
  * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language governing permissions and limitations under the
  * License.
  */
 package com.bigpupdev.synodroid.preference;
 
 import com.bigpupdev.synodroid.R;
 import com.bigpupdev.synodroid.utils.UIUtils;
 
 import android.content.Context;
 import android.preference.Preference;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.TextView;
 
 /**
  * A preference which shows the current value
  * 
  * @author Eric Taix (eric.taix at gmail.com)
  */
 public class ListPreferenceMultiSelectWithValue extends ListPreferenceMultiSelect implements PreferenceWithValue {
 
 	private TextView value;
 	// The current value
 	private String currentValue;
 	// External OnPreferenceChangeListener
 	private OnPreferenceChangeListener listener = null;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param context
 	 * @param attrs
 	 */
 	public ListPreferenceMultiSelectWithValue(Context context, AttributeSet attrs) {
 		super(context, attrs);
		if (UIUtils.isTablet(context)){
 			setLayoutResource(R.layout.preference_with_value_padded);
 		}
 		else{
 			setLayoutResource(R.layout.preference_with_value);
 		}
 		initInternalChangeListener();
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param context
 	 */
 	public ListPreferenceMultiSelectWithValue(Context context) {
 		super(context);
 		if (UIUtils.isTablet(context)){
 			setLayoutResource(R.layout.preference_with_value_padded);
 		}
 		else{
 			setLayoutResource(R.layout.preference_with_value);
 		}
 		initInternalChangeListener();
 	}
 
 	/**
 	 * Binds the view to the data for this preference
 	 */
 	@Override
 	protected void onBindView(View view) {
 		super.onBindView(view);
 		value = (TextView) view.findViewById(R.id.preference_value);
 		if (value != null) {
 			String val = getPrintableText(getValue());
 			if (val != null) {
 				value.setText(val);
 			}
 			currentValue = getPrintableValue();
 		}
 	}
 
 	/**
 	 * Init the internal listener to be able to update the new value
 	 */
 	private void initInternalChangeListener() {
 		// Call the super method as we overrided it
 		super.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				String val = getPrintableText(newValue.toString());
 				if (val != null) {
 					value.setText(val);
 					currentValue = val;
 				}
 				// By default update the preference's state
 				boolean result = true;
 				// If exist call the listener
 				if (listener != null) {
 					result = listener.onPreferenceChange(preference, newValue);
 				}
 				return result;
 			}
 		});
 	}
 
 	/**
 	 * Return a printable text (with comma separator) from a value with separator
 	 * 
 	 * @param valueWithSeparatorP
 	 * @return
 	 */
 	private String getPrintableText(String valueWithSeparatorP) {
 		String[] ssids = parseStoredValue(valueWithSeparatorP);
 		String result = null;
 		// For each SSID
 		if (ssids != null) {
 			for (String ssid : ssids) {
 				int index = -1;
 				try{
 					index = findIndexOfValue(ssid);
 				}catch (Exception e){}
 				if (index != -1) {
 					if (result == null) {
 						result = getEntries()[index].toString();
 					} else {
 						result = result + ", " + getEntries()[index].toString();
 					}
 				}
 				
 			}
 		}
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.preference.PreferenceWithValue#getPrintableValue()
 	 */
 	public String getPrintableValue() {
 		return getPrintableText(getValue());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.preference.Preference#setOnPreferenceChangeListener(android.preference .Preference.OnPreferenceChangeListener)
 	 */
 	@Override
 	public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
 		listener = onPreferenceChangeListener;
 	}
 
 	/**
 	 * Return the current value. This value is NOT the value stored in the SharedPreference but the current value in the view editor of this preference. Useful when you want to retrieve the new value before the preference is updated
 	 * 
 	 * @return
 	 */
 	public String getCurrentValue() {
 		String result = currentValue;
 		// If not set then return the current state
 		if (result == null) {
 			result = getPrintableValue();
 		}
 		return currentValue;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.preference.ListPreference#setValue(java.lang.String)
 	 */
 	@Override
 	public void setValue(String value) {
 		super.setValue(value);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.preference.ListPreference#setEntryValues(java.lang.CharSequence[])
 	 */
 	@Override
 	public void setEntryValues(CharSequence[] entryValuesP) {
 		super.setEntryValues(entryValuesP);
 	}
 
 	/**
 	 * Convenient method to create an instance of EditTextPreference
 	 * 
 	 * @param keyP
 	 * @param titleP
 	 * @param summaryP
 	 * @return
 	 */
 	public static ListPreferenceMultiSelectWithValue create(Context contextP, String keyP, int titleP, int summaryP, String[] versions, String def_value) {
 		ListPreferenceMultiSelectWithValue pref = new ListPreferenceMultiSelectWithValue(contextP);
 		pref.setKey(keyP);
 		pref.setTitle(titleP);
 		pref.setSummary(summaryP);
 		pref.setDialogTitle(titleP);
 		if (versions != null && versions.length > 0) {
 			pref.setEntries(versions);
 			pref.setEntryValues(versions);
 			pref.setValue(def_value);
 		}
 		return pref;
 	}
 
 }
