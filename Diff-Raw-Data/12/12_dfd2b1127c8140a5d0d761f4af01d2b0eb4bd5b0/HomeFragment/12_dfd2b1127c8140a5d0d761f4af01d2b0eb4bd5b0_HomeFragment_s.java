 /*
  * Copyright (C) 2011 The Android Open Source Project
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
 
 package com.barcamppenang2013.tabfragment;
 
 import java.util.Date;
 
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.barcamppenang2013.MainActivity;
 import com.barcamppenang2013.R;
 
 public class HomeFragment extends Fragment implements TabInterface {
 	public static final String TITLE = "  Home";
 	private TextView mTextViewDay, mTextViewHour;
 	private Button mImageViewClickMe;
 	private ScrollView scrollView;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 	}
 
 	@Override
 	public void onResume(){
 		super.onResume();
 		ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
 		actionBar.setTitle(TITLE);
 		actionBar.setDisplayHomeAsUpEnabled(false);
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		if (scrollView != null ){
 	        ViewGroup parent = (ViewGroup) scrollView.getParent();
 	        if (parent != null)
 	            parent.removeView(scrollView);
 		}
 		scrollView = (ScrollView) inflater.inflate(
 				R.layout.home_layout, container, false);
 		LinearLayout linearLayout = (LinearLayout) scrollView
 				.findViewById(R.id.home_linear_layout);
 		// linearLayout.setBackgroundColor(Color.WHITE);
 		mTextViewDay = (TextView) linearLayout
 				.findViewById(R.id.count_down_day);
 		mTextViewHour = (TextView) linearLayout
 				.findViewById(R.id.count_down_hour);
 		mImageViewClickMe = (Button) linearLayout.findViewById(R.id.map_fullscreen_button);
 		mImageViewClickMe.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (getActivity() == null)
 					return;
 				MainActivity fca = (MainActivity) getActivity();
 				fca.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 					fca.switchContent(new MapFragment());
 			}
 		});
 		// mTextView.setBackgroundResource(R.drawable.info_countdown);
		Date date = new Date(113, 6, 26); // 2013/July/26
 		// year the year, 0 is 1900.
 		// month the month, 0 - 11.
 		// day the day of the month, 1 - 31.
 		long dtMili = System.currentTimeMillis();
 		Date dateNow = new Date(dtMili);
 		long remain = date.getTime() - dateNow.getTime();
 
 		new CountDownTimer(remain, 1000) {
 
 			public void onTick(long millisUntilFinished) {
 				// mTextView.setText("seconds remaining: " + millisUntilFinished
 				// / 1000);
 				mTextViewDay.setText(timeDay(millisUntilFinished / 1000));
 				mTextViewHour.setText(timeHour(millisUntilFinished / 1000));
 			}
 
 			public void onFinish() {
 				// mTextView.setText("done!");
 			}
 		}.start();
 
 		return scrollView;
 	}
 
 	public String timeCalculate(long ttime) {
 		long days, hours, minutes, seconds;
 		String daysT = "", restT = "";
 
 		days = (Math.round(ttime) / 86400);
 		hours = (Math.round(ttime) / 3600) - (days * 24);
 		minutes = (Math.round(ttime) / 60) - (days * 1440) - (hours * 60);
 		seconds = Math.round(ttime) % 60;
 
 		if (days == 1)
 			daysT = String.format("%d day ", days);
 		if (days > 1)
 			daysT = String.format("%d days ", days);
 
 		restT = String.format("%02d:%02d:%02d", hours, minutes, seconds);
 
 		return daysT + restT;
 	}
 
 	public String timeDay(long ttime) {
 		long days, hours, minutes, seconds;
 		String daysT = "";
 
 		days = (Math.round(ttime) / 86400);
 		hours = (Math.round(ttime) / 3600) - (days * 24);
 		minutes = (Math.round(ttime) / 60) - (days * 1440) - (hours * 60);
 		seconds = Math.round(ttime) % 60;
 		if (days == 0)
 			daysT = "0";// String.format("%d", days);
 		if (days > 1)
 			daysT = String.format("%d", days);
 		return daysT;
 	}
 
 	public String timeHour(long ttime) {
 		long days, hours, minutes, seconds;
 		String restT = "";
 
 		days = (Math.round(ttime) / 86400);
 		hours = (Math.round(ttime) / 3600) - (days * 24);
 		minutes = (Math.round(ttime) / 60) - (days * 1440) - (hours * 60);
 		seconds = Math.round(ttime) % 60;
 
 		// restT = String.format("%02d", hours);
 		restT = String.format("%02d:%02d:%02d", hours, minutes, seconds);
 
 		return restT;
 	}
 
 	@Override
 	public String printTitle() {
 		return HomeFragment.TITLE;
 	}
 }
