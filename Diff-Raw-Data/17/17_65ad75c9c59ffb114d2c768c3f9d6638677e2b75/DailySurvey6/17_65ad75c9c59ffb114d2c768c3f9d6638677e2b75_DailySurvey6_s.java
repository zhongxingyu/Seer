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
 
 public class DailySurvey6 extends Activity implements OnClickListener{
 
 	Button finish;
 	SeekBar foodAmountSeekBar,foodHealthySeekBar;
 	TextView test1,test2;
 	String seekbarResult1="0",seekbarResult2="0";
 	Intent goToAssessment;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);// full screen
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.dailysurvey6);
 		//record=(Button)findViewById(R.id.bDS2Record);
 		finish=(Button)findViewById(R.id.bDS6Finish);
 		test1=(TextView)findViewById(R.id.tvDS6Test1);
 		test2=(TextView)findViewById(R.id.tvDS6Test2);
 		foodAmountSeekBar=(SeekBar)findViewById(R.id.sbDS6bar1);
 		foodHealthySeekBar=(SeekBar)findViewById(R.id.sbDS6bar2);
 		//record.setOnClickListener(this);
 		finish.setOnClickListener(this);
 		foodAmountSeekBar.setProgress(0);
 		foodAmountSeekBar.setMax(100);
 		
 		foodHealthySeekBar.setProgress(0);
 		foodHealthySeekBar.setMax(100);
 		
 		initializeSeekBar();
 	}
 
 	private void initializeSeekBar() {
 		// TODO Auto-generated method stub
 		foodAmountSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				// TODO Auto-generated method stub
 				test1.setText(String.valueOf(progress));
 				seekbarResult1=String.valueOf(progress);
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
 		foodHealthySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				// TODO Auto-generated method stub
 				test2.setText(String.valueOf(progress));
 				seekbarResult2=String.valueOf(progress);
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
 		case R.id.bDS6Finish:
			goToAssessment=new Intent(this,Assessment.class);
			startActivity(goToAssessment);
 			break;
 			
 		}
 		
 	}
 
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		finish();
 	}
 }
