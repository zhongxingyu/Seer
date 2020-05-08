 package com.js.mobile;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 /**
  * Created by IntelliJ IDEA.
  * User: JohnGyselinck
  * Date: 1/18/12
  * Time: 11:27 PM
  * To change this template use File | Settings | File Templates.
  */
 public class RegisterActivity extends Activity {
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle icicle){
         super.onCreate(icicle);
 
         //Load up the layout
         setContentView(R.layout.register);
 
         Button regButton = (Button)findViewById(R.id.register_submit_button);
         
         regButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                EditText username = (EditText)findViewById(R.id.txt_username);
                EditText password = (EditText)findViewById(R.id.txt_password);
                EditText fname = (EditText)findViewById(R.id.txt_fname);
                EditText lname = (EditText)findViewById(R.id.txt_lname);
                EditText email = (EditText)findViewById(R.id.txt_email);
             }
         });
     }
 }
