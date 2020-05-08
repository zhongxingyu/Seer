 package com.example.helloname;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
 
 public class MainActivity extends Activity
 {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     }

    public void sendName(View view)
    {
    	TextView dn = (TextView)findViewById(R.id.displayName);
    	dn.setText("Hello "+((EditText)findViewById(R.id.enteredName)).getText().toString());
    }

 }
