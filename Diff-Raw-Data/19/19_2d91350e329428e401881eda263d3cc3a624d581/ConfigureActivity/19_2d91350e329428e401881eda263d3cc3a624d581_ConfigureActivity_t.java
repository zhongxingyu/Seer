 package com.rj.memomatic;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 
 public class ConfigureActivity extends Activity {
 	
 	TextView enableText;
 	TextView descriptionText;
 	TextView lastRan;
 	Typeface robotoThin;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_configure);
 		robotoThin = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
 		enableText = (TextView)findViewById(R.id.enabledisable);
 		descriptionText = (TextView)findViewById(R.id.description);
 		lastRan = (TextView)findViewById(R.id.lastphoto);
 		enableText.setTypeface(robotoThin);
 		descriptionText.setTypeface(robotoThin);
 		lastRan.setTypeface(robotoThin);
 
 		updateUi();
 	}
 	
 	
 	void updateText() {
 		final boolean enabled = SchedulerReciever.isEnabled(this);
 		
 		if (enabled) {
 			enableText.setText(R.string.config_on);
 			enableText.setTextColor(getResources().getColor(R.color.state_green));
 		} else {
 			enableText.setText(R.string.config_off);
 			enableText.setTextColor(getResources().getColor(R.color.state_red));
 		}
 		
 		
 		final Context context = this;
 		enableText.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (enabled) {
 					Log.d("MemoMatic - Configure", "UnScheduling Picture!");
 			        SchedulerReciever.actionDisable(context);
 				} else {
 					Log.d("MemoMatic - Configure", "Scheduling Picture!");
 			        SchedulerReciever.actionEnable(context);
 				}
 				updateUi();
 			}
 		});
 		
 	}
 	
 	void updateLastRan() {
 		long now = System.currentTimeMillis();
 		long nextScan = SchedulerReciever.getNextScan(this);
 		long diff = (nextScan-now)/1000; //in seconds
 		if (diff < 60) {
 			lastRan.setText("Next photo in "+diff+" seconds");
 			return;
 		} 
 		diff = diff / 60;
 		if (diff < 60) {
 			lastRan.setText("Next photo in "+diff+" minutes");
 			return;
 		}
 		diff = diff / 60;
 		lastRan.setText("Next photo in "+diff+" hours");
 	}
 	
 	void updateUi() {
 		updateText();
 		updateLastRan();
 	}
 
	
	@Override
	protected void onStart() {
		super.onStart();
		updateUi();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateUi();
	}
 }
