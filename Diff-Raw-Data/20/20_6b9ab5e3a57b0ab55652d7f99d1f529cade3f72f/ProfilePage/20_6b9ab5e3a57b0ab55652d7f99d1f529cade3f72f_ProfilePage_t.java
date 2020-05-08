 package com.jobhound.activities;
 
 import com.jobhound.R;
 
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
 
 public class ProfilePage extends RoboActivity implements OnClickListener {
 	
 	@InjectView(R.id.titleProfile) TextView titleProfile;
 	@InjectView(R.id.job1) EditText job1;
 	@InjectView(R.id.jobText1) TextView jobText1;
 	@InjectView(R.id.job2) EditText job2;
 	@InjectView(R.id.jobText2) TextView jobText2;
 	@InjectView(R.id.job3) EditText job3;
 	@InjectView(R.id.jobText3) TextView jobText3;
 	@InjectView(R.id.contractType) TextView contractType;
 	@InjectView(R.id.spinner1) Spinner spinner1;
 	@InjectView(R.id.pb_horizontal) ProgressBar pbHorizontal;
 	@InjectView(R.id.jsaProgress) TextView jsaPreogress;
 	@InjectView(R.id.tv_progress_horizontal) TextView tv_progress_horizontal;
 	@InjectView(R.id.btn_go) Button btn_go;
 	@InjectView(R.id.rl_progress_bar_set) RelativeLayout rl_progress_bar_set;
	//@InjectView(R.id.btn_save) Button btn_save;

	
 	
 	
 	private int progress = 0;  
 
 	
 //	@InjectView(R.id.workHours) TextView workHours;
 //	@InjectView(R.id.spinner2) Spinner spinner2;
 	
 	
 	
 	@Override
 	public void onBackPressed() {
 		// TODO Auto-generated method stub
 		super.onBackPressed();
		Intent openProfile = new Intent("android.intent.action.MAIN_MENU");
     	finish();
 		startActivity(openProfile);
 		overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_right);
 	}
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.profile_page);
 
 		Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
 		        R.array.contract_array, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner1.setAdapter(adapter);	
 		
 		btn_go.setOnClickListener(new ButtonsListener());
 	}
 	
 	 public class ButtonsListener implements OnClickListener, android.view.View.OnClickListener {  
           public void onClick(View v) {  
         	  rl_progress_bar_set.setVisibility(View.VISIBLE);  
         	 
         	  
               switch (v.getId()) {  
     
               case R.id.btn_go:  
                   
 				if(progress  < pbHorizontal.getMax()) {  
                       progress = progress + 10;    // update progress  
                       postProgress(progress);  
     
                   } else {  
 					rl_progress_bar_set.setVisibility(View.GONE);  
                       progress = 0;  
                       Toast.makeText(ProfilePage.this, "Finish", Toast.LENGTH_SHORT).show();  
                   }  
                   break;  
     
               default:  
                   return;  
               }  
 	 }
 
           private void postProgress(int progress) {  
               pbHorizontal.setProgress(progress);  
               
               /* update secondary progress of horizontal progress bar */  
         		                if(progress == 0) {  
                   pbHorizontal.setSecondaryProgress(0);  
               } else {  
                   pbHorizontal.setSecondaryProgress(progress + 5);  
               }  
           }
           
 		@Override
 		public void onClick(DialogInterface arg0, int arg1) {
 			// TODO Auto-generated method stub
 			
 		}  
 	 }
 
 	@Override
 	public void onClick(DialogInterface dialog, int which) {
 		// TODO Auto-generated method stub
 		
 	}
 }
