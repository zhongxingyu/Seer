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
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class DailySurvey2 extends Activity implements OnClickListener{
 
 	Button finish;
 	SeekBar happySeekBar;
 	TextView test;
 	EditText word1,word2,word3;
 	String seekbarResult,wordResult1,wordResult2,wordResult3;
 	Intent goToAssessment;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);// full screen
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.dailysurvey2);
 		//record=(Button)findViewById(R.id.bDS2Record);
 		word1=(EditText)findViewById(R.id.etDS2Word1);
 		word2=(EditText)findViewById(R.id.etDS2Word2);
 		word3=(EditText)findViewById(R.id.etDS2Word3);
 		finish=(Button)findViewById(R.id.bDS2Finish);
 		test=(TextView)findViewById(R.id.tvDS2Test);
 		happySeekBar=(SeekBar)findViewById(R.id.sbDS2);
 		//record.setOnClickListener(this);
 		finish.setOnClickListener(this);
 		happySeekBar.setProgress(0); //this is line 19
 		happySeekBar.setMax(100);
 		
 		initializeSeekBar();
 	}
 
 	private void initializeSeekBar() {
 		// TODO Auto-generated method stub
 		happySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 
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
 		case R.id.bDS2Finish:
 			
 			wordResult1=word1.getText().toString();
 			wordResult2=word2.getText().toString();
 			wordResult3=word3.getText().toString();
			finish();
 			break;
 			
 		}
 		
 	}
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		finish();
 	}
 
 }
