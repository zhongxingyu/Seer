 package com.example.drinkingapp;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class DailySurveySleep extends Activity implements OnClickListener,OnCheckedChangeListener{
 
 	Button finish;
 	SeekBar sleepQualityBar;
 	TextView test;
 	RadioGroup sleepGroup,coffeeGroup;
 	RadioButton yes1,yes2,no1,no2;
 	String radioResult1,seekbarResult="0",radioResult2;
 	Intent goToAssessment;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);// full screen
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.dailysurveysleep);
 
 		finish=(Button)findViewById(R.id.bDSSleepFinish);
 		test=(TextView)findViewById(R.id.tvDSSleepTest);
 		sleepQualityBar=(SeekBar)findViewById(R.id.sbDSSleep);
 		
 		
 
 		finish.setOnClickListener(this);
 		sleepQualityBar.setProgress(0); //this is line 19
 		sleepQualityBar.setMax(100);
 		
 		initializeSeekBar();
 	}
 
 	private void initializeSeekBar() {
 		// TODO Auto-generated method stub
 		sleepQualityBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				// TODO Auto-generated method stub
 				test.setText(String.valueOf(progress));
 				seekbarResult=String.valueOf(progress);
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				
			}			
 		});
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 		switch(arg0.getId()){
 		/*
 		case R.id.bDS2Record:
 			break;
 			
 			*/
 		case R.id.bDSSleepFinish:
			finish();
 			break;
 			
 		}
 		
 	}
 
 	@Override
 	public void onCheckedChanged(RadioGroup group, int checkedId) {
 		// TODO Auto-generated method stub
 		switch(group.getId()){
 			case R.id.rgDSSleep1:
 				radioResult1=((RadioButton)findViewById(checkedId)).getText().toString();
 				break;
 			case R.id.rgDSSleep2:
 				radioResult2=((RadioButton)findViewById(checkedId)).getText().toString();
 				break;
 		}
 	}
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		finish();
 	}
 
 }
