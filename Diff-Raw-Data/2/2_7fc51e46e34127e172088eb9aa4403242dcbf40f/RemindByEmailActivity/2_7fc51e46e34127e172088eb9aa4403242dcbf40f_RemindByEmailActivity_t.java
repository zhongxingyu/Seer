 package com.bobbyware.reminbyemail;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
public class RemindByEmailActivity extends Activityx {
 
     Button buttonSend;
     EditText textTo;
     EditText textSubject;
     EditText textMessage;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         buttonSend = (Button) findViewById(R.id.buttonSend);
         //textTo = (EditText) findViewById(R.id.editTextTo);
         //textSubject = (EditText) findViewById(R.id.editTextSubject);
         textMessage = (EditText) findViewById(R.id.editTextMessage);
         
         buttonSend.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				//String to = textTo.getText().toString();
 				//String subject = textSubject.getText().toString();
 				
 				String to = "renloe@k2si.com";
 				String subject = "Reminder";
 				
 				String message = textMessage.getText().toString();
 				
 				Intent email = new Intent(Intent.ACTION_SEND);
 				email.putExtra(Intent.EXTRA_EMAIL, new String[] {to});
 				email.putExtra(Intent.EXTRA_SUBJECT, subject);
 				email.putExtra(Intent.EXTRA_TEXT, message);
 				
 				email.setType("message/rfc822");
 				
 				startActivity(Intent.createChooser(email, "Choose and Email client : "));
 			}
 		});
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 }
