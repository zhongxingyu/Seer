 package com.cs301w01.meatload.activities;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import com.cs301w01.meatload.R;
 import com.cs301w01.meatload.model.Picture;
 
 import java.util.Collection;
 
 public class SendEmailActivity extends Skindactivity {
 
     Button sendButton;
     EditText textTo;
     EditText textSubject;
     EditText textMessage;
 
     private Picture picture;
 
     @Override
     /**
      * Adopted from: http://www.mkyong.com/android/how-to-send-email-in-android/
      */
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         //picture = (Picture) getIntent().getExtras().getSerializable("picture");
 
         sendButton = (Button) findViewById(R.id.buttonSend);
         textTo = (EditText) findViewById(R.id.editTextTo);
         textSubject = (EditText) findViewById(R.id.editTextSubject);
         textMessage = (EditText) findViewById(R.id.editTextMessage);
 
         //handle logic for when user chooses to send an email
         sendButton.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View v) {
 
                 String recipient = textTo.getText().toString();
                 String subject = textSubject.getText().toString();
                 String message = textMessage.getText().toString();
 
                 Intent email = new Intent(Intent.ACTION_SEND);
                 email.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
                 //email.putExtra(Intent.EXTRA_CC, new String[]{ recipient});
                 //email.putExtra(Intent.EXTRA_BCC, new String[]{recipient});
 
                // email.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + picture.getPath()));
 
                 //add multiple photos
 //                for(Picture picture: pictureAttachments){
 //
 //                    email.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + picture.getPath()));
 //
 //
 //                }
                 
                 //add subject and additional message
                 email.putExtra(Intent.EXTRA_SUBJECT, subject);
                 email.putExtra(Intent.EXTRA_TEXT, message);
 
                 //need this to prompts email client only
                 email.setType("message/rfc822");
 
                 startActivity(Intent.createChooser(email, "Choose an Email client :"));
 
             }
         });
     }
 
     @Override
     public void update(Object model) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 }
 
