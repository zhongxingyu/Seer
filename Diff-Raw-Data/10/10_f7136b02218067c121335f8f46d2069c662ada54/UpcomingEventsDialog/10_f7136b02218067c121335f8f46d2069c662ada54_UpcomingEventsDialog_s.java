 /*
  * Copyright (C) 2013 CampusUB1 Development Team
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
 
 package com.dev.campus.util;
 
 import com.dev.campus.CampusUB1App;
 import com.dev.campus.R;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 /**
  * Utility dialog for setting the value of upcoming months for which
 * the user wants to check for events
  * @author CampusUB1 Development Team
  *
  */
 public class UpcomingEventsDialog extends Dialog 
 	implements Button.OnClickListener, SeekBar.OnSeekBarChangeListener {
 
 	private SeekBar mSeekBar;
 	private TextView mMinLabel, mMaxLabel, mCurrentLabel;
 	private String[] mMonthsStrings;
 	private int[] mMonthsValues;
 	private int mSeekBarPos;
 	private Button mButton;
 
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
		setTitle(R.string.upcoming_events_title);
 
 		int timeUnit = CampusUB1App.persistence.getUpcomingEventsRange();
 		mMonthsStrings = CampusUB1App.getInstance().getResources().getStringArray(R.array.upcoming_events_strings);
 		mMonthsValues = CampusUB1App.getInstance().getResources().getIntArray(R.array.upcoming_events_array);
 		mSeekBarPos = -1;
			
 		int value = Persistence.MAX_UPCOMING_MONTHS;
 		while (value != timeUnit && mSeekBarPos < (mMonthsValues.length - 1)) {
 			mSeekBarPos++;
 			value = mMonthsValues[mSeekBarPos];
 		}
 		
 		setContentView(R.layout.dialog_layout);
 		mMinLabel = (TextView) findViewById(R.id.dlg_min_label);
 		mMaxLabel = (TextView) findViewById(R.id.dlg_max_label);
 		mCurrentLabel = (TextView) findViewById(R.id.dlg_timeout_label);
 		mMinLabel.setText(mMonthsStrings[0]);
 		mMaxLabel.setText(mMonthsStrings[mMonthsStrings.length - 1]);
 		mCurrentLabel.setText(mMonthsStrings[mSeekBarPos]);
 
 		mButton = (Button) findViewById(R.id.dlg_btn_done);
 		mButton.setOnClickListener(this); 
 
 		mSeekBar = (SeekBar) findViewById(R.id.dlg_seekbar);
 		mSeekBar.setMax(mMonthsValues.length - 1);
 		mSeekBar.setProgress(mSeekBarPos);
 		mSeekBar.setOnSeekBarChangeListener(this); 
 		mSeekBar.requestFocus();
 	}
 
 	public UpcomingEventsDialog(Context context) {
 		super(context);
 	}
 
 	@Override
 	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 		mCurrentLabel.setText(mMonthsStrings[progress]);
 		CampusUB1App.persistence.setUpcomingEventsRange(mMonthsValues[progress]);
 	}
 
 	@Override
 	public void onClick(View view) {
 		dismiss();
 	}
 	
 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar) {
 		
 	}
 
 	@Override
 	public void onStopTrackingTouch(SeekBar seekBar) {
 		
 	}
 }
