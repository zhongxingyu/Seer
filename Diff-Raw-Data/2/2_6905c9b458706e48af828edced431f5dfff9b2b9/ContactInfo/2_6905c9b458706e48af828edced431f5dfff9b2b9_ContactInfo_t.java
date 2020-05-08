 package com.hifive.beam;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.telephony.PhoneNumberFormattingTextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 /**
  * A simple example app to let you enter a number and start the contacts app
  * showing the contact with that number.
  */
public class ContactInfo extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // Initialize activity
         setContentView(R.layout.main);
 
         // Add formatting to number field
         final TextView number = (TextView) findViewById(R.id.number);
         number.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
 
         // Handle button clicks by searching
         ((Button) findViewById(R.id.search)).setOnClickListener(new OnClickListener() {
             public void onClick(View arg0) {
                 Uri uri = Uri.fromParts("tel", number.getText().toString(), "");
                 Log.d("ContactPicker", "uri for intent: [" + uri.toString() + "]");
                 startActivity(new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT, uri));
             }
         });
     }
 }
