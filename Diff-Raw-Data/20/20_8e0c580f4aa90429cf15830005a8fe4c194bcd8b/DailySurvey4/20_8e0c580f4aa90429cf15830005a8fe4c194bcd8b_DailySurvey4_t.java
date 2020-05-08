 package com.example.drinkingapp;
 
 import java.util.ArrayList;
 
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
 
 public class DailySurvey4 extends Activity implements OnClickListener, OnCheckedChangeListener{
 
 	Button finish;
 	RadioButton option1,option2,option3,option4,option5,option6,option7,option8;
 	RadioGroup radioGroup;
 	EditText other;
 	TextView hiddeText;
 	ArrayList<String> words=new ArrayList();
 	ArrayList<RadioButton> optionList=new ArrayList();
 	String radioResult;
 	Intent goToAssessment;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);// full screen
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.dailysurvey4);
 		finish=(Button)findViewById(R.id.bDS3Finish);
 		finish.setOnClickListener(this);
 		radioGroup=(RadioGroup)findViewById(R.id.rgDS4);
 		option1=(RadioButton)findViewById(R.id.cbDS3Box1);
 		option2=(RadioButton)findViewById(R.id.cbDS3Box2);
 		option3=(RadioButton)findViewById(R.id.cbDS3Box3);
 		option4=(RadioButton)findViewById(R.id.cbDS3Box4);
 		option5=(RadioButton)findViewById(R.id.cbDS3Box5);
 		option6=(RadioButton)findViewById(R.id.cbDS3Box6);
 		option7=(RadioButton)findViewById(R.id.cbDS3Box7);
 		option8=(RadioButton)findViewById(R.id.cbDS3Box8);
 		other=(EditText)findViewById(R.id.etDS4Other);
 		hiddeText=(TextView)findViewById(R.id.tvDS4Hidden);
 		optionList.add(option1);
 		optionList.add(option2);
 		optionList.add(option3);
 		optionList.add(option4);
 		optionList.add(option5);
 		optionList.add(option6);
 		optionList.add(option7);
 		optionList.add(option8);
 		
 	}
 
 	
 
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 		switch(arg0.getId()){
 		/*
 		case R.id.bDS2Record:
 			break;
 			
 			*/
 		//checks which checklist is check and input 
 		//that value in a string to pass onto the database function to parse
		case R.id.bDS3Finish:
		/*
			if (optionList != null){
				for (int x=0;x<optionList.size();x++){
					if(optionList.get(x).isChecked()){
						words.add(optionList.get(x).getText().toString());
					}
 				}
			}*/
 			//pass intent with this^
			//goToAssessment=new Intent(this,Assessment.class);
			//startActivity(goToAssessment);
			finish();
 			break;
 			
 		}
 		
 	}
 
 	@Override
 	public void onCheckedChanged(RadioGroup arg0, int checkedId ) {
 		// TODO Auto-generated method stub
 		switch(checkedId){
 			case R.id.rDS4radio8:
 				hiddeText.setText(other.getText().toString());
 				radioResult=other.getText().toString();
 				break;
 			default:
 				hiddeText.setText(((RadioButton)findViewById(checkedId)).getText().toString());
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
