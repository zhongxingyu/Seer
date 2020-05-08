 package com.disrupt.conservo;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 /**
  *  @author Skylar Castator
  */
 public class LocationDataActivity extends Activity {
 	Button mButton;
 	EditText mName;
 	EditText mAddress;
 	EditText mState;
 	EditText mZipCode;
 	
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.locationdata);
         
         mButton = (Button)findViewById(R.id.m_submitButton);
        mName   = (EditText)findViewById(R.id.edittext);
 
         mButton.setOnClickListener(
             new View.OnClickListener()
             {
                 public void onClick(View view)
                 {
                     Log.v("EditText", mName.getText().toString());
                 }
             });
     }
 }
