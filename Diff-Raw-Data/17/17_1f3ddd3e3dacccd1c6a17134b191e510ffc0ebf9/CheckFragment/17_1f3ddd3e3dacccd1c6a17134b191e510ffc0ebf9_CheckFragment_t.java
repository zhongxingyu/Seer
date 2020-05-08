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
 package uk.co.jwlawson.nof1.fragments;
 
 import uk.co.jwlawson.nof1.R;
 import android.app.Activity;
 import android.content.res.TypedArray;
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.TextView;
 
 public class CheckFragment extends QuestionFragment {
 
 	private static final String TAG = "CheckFragment";
 	private static final boolean DEBUG = true;
 
 	public static final String ARGS_TEXT = "argsText";
 	public static final String ARGS_DEFAULT = "argsDefault";
 
 	private CheckBox mCheck;
 
 	public CheckFragment() {
 	}
 
 	public static CheckFragment newInstance(String questionText, boolean def) {
 
 		CheckFragment frag = new CheckFragment();
 
 		Bundle args = new Bundle();
 		args.putString(ARGS_TEXT, questionText);
 		args.putBoolean(ARGS_DEFAULT, def);
 
 		frag.setArguments(args);
 
 		if (DEBUG) Log.d(TAG, "New instance created");
 
 		return frag;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
 		View view = inflater.inflate(R.layout.row_layout_data_checkbox, container, false);
 
 		Bundle args = getArguments();
 		if (DEBUG && args == null) Log.d(TAG, "CHeckFragment view created with null args");
 
 		mCheck = (CheckBox) view.findViewById(R.id.data_input_checkbox_chk);
 		if (args != null) mCheck.setChecked(args.getBoolean(ARGS_DEFAULT, false));
 
 		TextView txt = (TextView) view.findViewById(R.id.data_input_checkbox_txt_question);
 		if (args != null) txt.setText(args.getString(ARGS_TEXT));
 
 		if (DEBUG) Log.d(TAG, "View created");
 
 		return view;
 	}
 
 	@Override
 	public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
 		super.onInflate(activity, attrs, savedInstanceState);
 		if (DEBUG) Log.d(TAG, "CheckFragment inflated");
 
		if (getArguments() == null) {
			Bundle args = new Bundle();
 
			TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.CheckFragmentArguments);
			args.putString(ARGS_TEXT, (String) a.getText(R.styleable.CheckFragmentArguments_android_label));
			args.putBoolean(ARGS_DEFAULT, a.getBoolean(R.styleable.CheckFragmentArguments_android_checked, false));
 
			a.recycle();
 
			setArguments(args);
		}
 	}
 
 	@Override
 	public int getResult() {
 		if (mCheck.isChecked()) return 1;
 		else return 0;
 	}
 
 }
