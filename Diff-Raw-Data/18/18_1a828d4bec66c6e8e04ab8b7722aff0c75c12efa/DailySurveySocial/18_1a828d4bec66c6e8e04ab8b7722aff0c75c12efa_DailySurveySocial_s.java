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
 
 public class DailySurveySocial extends Activity implements OnClickListener,OnCheckedChangeListener{
 
 	Button finish;
 	SeekBar socialQualityBar;
 	TextView test;
 	RadioGroup socialGroup;
 	RadioButton yes,no;
 	String radioResult,seekbarResult="0";
 	Intent goToAssessment;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);// full screen
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.dailysurveysocial);
 
 		finish=(Button)findViewById(R.id.bDSSocialFinish);
 		test=(TextView)findViewById(R.id.tvDSSocialTest);
 		socialQualityBar=(SeekBar)findViewById(R.id.sbDSSocial);
 		
 		
 
 		finish.setOnClickListener(this);
 		socialQualityBar.setProgress(0); //this is line 19
 		socialQualityBar.setMax(100);
 		
 		initializeSeekBar();
 	}
 
 	private void initializeSeekBar() {
 		// TODO Auto-generated method stub
 		socialQualityBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 
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
			goToAssessment=new Intent(this,Assessment.class);
			startActivity(goToAssessment);
			//pass sleepResult coffeeResults via intent.
 			break;
 			
 		}
 		
 	}
 
 	@Override
 	public void onCheckedChanged(RadioGroup group, int checkedId) {
 		// TODO Auto-generated method stub
 		switch(group.getId()){
 			case R.id.rgDSExercise:
 				radioResult=((RadioButton)findViewById(checkedId)).getText().toString();
 				break;
 		}
 	}
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		finish();
 	}
 
 }
