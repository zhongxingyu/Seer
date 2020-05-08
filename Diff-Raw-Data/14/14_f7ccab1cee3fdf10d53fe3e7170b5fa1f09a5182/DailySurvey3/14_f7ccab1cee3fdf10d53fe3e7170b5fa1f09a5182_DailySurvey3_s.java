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
 import android.widget.CheckBox;
 
 
 public class DailySurvey3 extends Activity implements OnClickListener{
 
 	Button finish;
 	CheckBox option1,option2,option3,option4,option5,option6,option7,option8;
 	ArrayList<String> words=new ArrayList<String>();
 	ArrayList<String> checkListResult = new ArrayList<String>();
 	ArrayList<CheckBox> optionList=new ArrayList<CheckBox>();
 	Intent goToAssessment;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);// full screen
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.dailysurvey3);
 		finish=(Button)findViewById(R.id.bDS3Finish);
 		finish.setOnClickListener(this);
 		option1=(CheckBox)findViewById(R.id.cbDS3Box1);
 		option2=(CheckBox)findViewById(R.id.cbDS3Box2);
 		option3=(CheckBox)findViewById(R.id.cbDS3Box3);
 		option4=(CheckBox)findViewById(R.id.cbDS3Box4);
 		option5=(CheckBox)findViewById(R.id.cbDS3Box5);
 		option6=(CheckBox)findViewById(R.id.cbDS3Box6);
 		option7=(CheckBox)findViewById(R.id.cbDS3Box7);
 		option8=(CheckBox)findViewById(R.id.cbDS3Box8);
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
		case R.id.bDS2Finish:
 			for (int x=0;x<optionList.size();x++){
 				if(optionList.get(x).isChecked()){
 					checkListResult.add(optionList.get(x).getText().toString());
 				}
 				if(!optionList.get(x).isChecked()){
 					checkListResult.add("null");
 				}
 			}
			//pass intent with this^
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
