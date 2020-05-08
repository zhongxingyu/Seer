 package com.lpuente.java2_w3;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class SelectedContact extends Activity implements SelectedFrag.SecondInterface{
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.selectedview);		//setting to selectedfrag.xml
 		
 		//Getting id for TextView to display full name
 				TextView fullName = (TextView) findViewById(R.id.full_name);
 				
 		//Getting id for TextView to display Phone Number
 				final TextView phone = (TextView) findViewById(R.id.phone_digits);
 				
 		//creating the button
 				Button callButt = (Button)findViewById(R.id.call_button);
 				
 		//setting first and last name in one text view
 				fullName.setText(getIntent().getExtras().getString("list_name")); //passed key is in quotes
 		//setting phone number
 				phone.setText(getIntent().getExtras().getString("phoneText"));   //passed key is in quotes
 				
 				
 				//listener for callButt button
 				callButt.setOnClickListener(new OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						
 					//Adding the typed in cell number to call. Must have tel: at beginning of string
 					//I'm sure there's a cleaner better way to do this
 						String phoneNum = "tel:" + phone.getText().toString();
 						
 					//Calling the ACTION_CALL to use the typed number for the phone. NEEDS PERMISSON CALL_PHONE IN MANIFEST
 						Intent impIntent = new Intent(Intent.ACTION_CALL, Uri.parse(phoneNum));
 						
 					//Code to Start intent
 						startActivity(impIntent);
 					}
 				});
 	}
 
 	
 	//here's where i got stuck last time. Will look into it later.
 	
 	@Override
 	public void onMakeCall() {
 		// TODO Auto-generated method stub
 		
 	}
 }
