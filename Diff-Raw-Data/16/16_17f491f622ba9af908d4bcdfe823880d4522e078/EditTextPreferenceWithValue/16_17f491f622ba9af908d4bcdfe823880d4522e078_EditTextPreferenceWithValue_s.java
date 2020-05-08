 /**
  * Copyright 2010 Eric Taix
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  * 
  */
 package com.bigpupdev.synodroid.preference;
 
 import com.bigpupdev.synodroid.R;
 import com.bigpupdev.synodroid.utils.UIUtils;
 
 import android.content.Context;
 import android.preference.EditTextPreference;
 import android.text.InputType;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * A preference which shows the current value
  * 
  * @author Eric Taix (eric.taix at gmail.com)
  */
 public class EditTextPreferenceWithValue extends EditTextPreference implements PreferenceWithValue {
 
 	// The textview used in the preference
 	private TextView valueView;
 	// The input type ot use in the dialog's EditText
 	private Integer inputType = null;
 	private boolean doTrim = false;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param context
 	 * @param attrs
 	 */
 	public EditTextPreferenceWithValue(Context context, AttributeSet attrs, boolean trim) {
 		super(context, attrs);
 		if (UIUtils.isHoneycombTablet(context)){
 			setLayoutResource(R.layout.preference_with_value_padded);
 		}
 		else{
 			setLayoutResource(R.layout.preference_with_value);
 		}
 		setSingleLine(true);
 		doTrim = trim;
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param context
 	 */
 	public EditTextPreferenceWithValue(Context context, boolean trim) {
 		super(context);
		if (UIUtils.isTablet(context)){
 			setLayoutResource(R.layout.preference_with_value_padded);
 		}
 		else{
 			setLayoutResource(R.layout.preference_with_value);
 		}
 		setSingleLine(true);
 		doTrim = trim;
 
 	}
 
 	/**
 	 * Set the input type for the dialog's EditText. Useful to set password, int, or other specific type of input
 	 * 
 	 * @param inputTypeP
 	 */
 	public EditTextPreferenceWithValue setInputType(int inputTypeP) {
 		inputType = inputTypeP;
 		EditText text = getEditText();
 		if (text != null) {
 			text.setInputType(inputType);
 		}
 		return this;
 	}
 
 	/**
 	 * Set the EditText to be a single line
 	 * 
 	 * @param singleP
 	 * @return
 	 */
 	public EditTextPreferenceWithValue setSingleLine(boolean singleP) {
 		EditText text = getEditText();
 		if (text != null) {
 			text.setSingleLine(singleP);
 		}
 		return this;
 	}
 
 	/**
 	 * Binds the view to the data for this preference
 	 */
 	@Override
 	protected void onBindView(View view) {
 		super.onBindView(view);
 		valueView = (TextView) view.findViewById(R.id.preference_value);
 		updateValueInPreference(getText());
 	}
 
 	/**
 	 * Set the text
 	 */
 	@Override
 	public void setText(String text) {
 		if (doTrim && text != null) {
 			super.setText(text.trim());
 		} else {
 			super.setText(text);
 		}
 		updateValueInPreference(getText());
 	}
 
 	/**
 	 * Update the value showned in the preference
 	 * 
 	 * @param valueP
 	 */
 	private void updateValueInPreference(String valueP) {
 		// Prevent from showing a password
 		EditText editText = getEditText();
 		if (valueP != null && editText != null && (editText.getInputType() & InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) {
 			valueP = "*********************************************************************************************************************".substring(0, valueP.length());
 		}
 		// Show the value
 		if (valueView != null) {
 			valueView.setText(valueP);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.preference.PreferenceWithValue#getValue()
 	 */
 	public String getPrintableValue() {
 		return getText();
 	}
 
 	/**
 	 * Convenient method to create an instance of EditTextPreference
 	 * 
 	 * @param keyP
 	 * @param titleP
 	 * @param summaryP
 	 * @return
 	 */
 	public static EditTextPreferenceWithValue create(Context contextP, String keyP, int titleP, int summaryP, boolean trim) {
 		EditTextPreferenceWithValue pref = new EditTextPreferenceWithValue(contextP, trim);
 		pref.setKey(keyP);
 		pref.setTitle(titleP);
 		pref.setSummary(summaryP);
 		pref.setDialogTitle(titleP);
 		pref.setDialogMessage(summaryP);
 		return pref;
 	}
 
 }
