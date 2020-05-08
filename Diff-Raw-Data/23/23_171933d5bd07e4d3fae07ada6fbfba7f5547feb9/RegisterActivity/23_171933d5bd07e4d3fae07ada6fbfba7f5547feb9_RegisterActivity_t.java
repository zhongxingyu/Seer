 package com.js.mobile;
 
 import android.app.Activity;
import android.content.Intent;
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
                String username = ((EditText)findViewById(R.id.txt_username)).getText().toString();
                String password = ((EditText)findViewById(R.id.txt_password)).getText().toString();
                String fname = ((EditText)findViewById(R.id.txt_fname)).getText().toString();
                String lname = ((EditText)findViewById(R.id.txt_lname)).getText().toString();
                String email = ((EditText)findViewById(R.id.txt_email)).getText().toString();

                DataHandler.doRegistration(username, password, fname, lname, email);
                Intent myIntent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(myIntent);
             }
         });
     }
 }
