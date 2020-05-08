 package com.example.multimodal2;
 
 import multimodal.schedule.Booking;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class MeetingConfirmation  extends Activity  {
 	Button yesButton;
 	Button noButton;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.bookingconfirmation);
 		 Booking b = (Booking) getIntent().getSerializableExtra("booking");
		 TextView startTimeView = (TextView) findViewById(R.id.start_time);
		 TextView endTimeView = (TextView) findViewById(R.id.end_time);
		 TextView roomView = (TextView) findViewById(R.id.confirmationroom);
 		 yesButton = (Button) findViewById(R.id.confirmyes);
 		 noButton = (Button) findViewById(R.id.confirmno);
 		 startTimeView.setText(b.getStartTime().toLocaleString());		 
 		 endTimeView.setText(b.getEndTime().toLocaleString());
 		 roomView.setText(b.getRoom().getName());
 		 
 		 yesButton.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				setResult(RESULT_OK); 
 				 finish();
 				
 			}
 			 
 		 });
 		 
 		 noButton.setOnClickListener(new OnClickListener(){
 
 				@Override
 				public void onClick(View v) {
 					
 					// this.putExtra("result", false);
 					 setResult(RESULT_CANCELED);  
 					 finish();
 					
 				}
 				 
 			 });
 	}
 
 	
 }
