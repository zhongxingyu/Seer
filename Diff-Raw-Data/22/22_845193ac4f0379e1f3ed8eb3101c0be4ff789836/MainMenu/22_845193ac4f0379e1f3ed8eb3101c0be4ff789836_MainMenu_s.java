 package com.jobhound.activities;
 
 
 import com.jobhound.R;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
 
 public class MainMenu extends RoboActivity implements OnClickListener  {
 	
 	@InjectView(R.id.buttonSearch) Button search;
 	@InjectView(R.id.buttonDiary) Button diary;
 	@InjectView(R.id.buttonProfile) Button profile;
 	@InjectView(R.id.buttonSaved) Button saved;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_menu);
 		
 		search.setOnClickListener(this);
 		diary.setOnClickListener(this);
 		profile.setOnClickListener(this);
 		saved.setOnClickListener(this);
 	}
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 		
 		switch (arg0.getId()){
 
 		case R.id.buttonSearch:
 			Intent openSearchMenu = new Intent("android.intent.action.SEARCH_MENU");
 	    	finish();
 			startActivity(openSearchMenu);
 			 overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
			
			
 			break;
 			
 		case R.id.buttonProfile:
 			break;
 			
 		case R.id.buttonDiary:
 			Intent openDiary = new Intent("android.intent.action.DIARY_LAYOUT");
 	    	finish();
 			startActivity(openDiary);
 			 overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
 			break;
 			
 		case R.id.buttonSaved:
 			break;
 		
 	}
 
 	}
 	
 }
