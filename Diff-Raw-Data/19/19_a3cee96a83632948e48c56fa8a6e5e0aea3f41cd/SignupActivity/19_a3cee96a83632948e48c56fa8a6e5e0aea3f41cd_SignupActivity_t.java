 package com.allen.mapple;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 
 
 public class SignupActivity extends Activity {
 
     private ImageButton loginButton;
     private ImageButton registerButton;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_signup);
 
         loginButton = (ImageButton) findViewById(R.id.login_button);
         registerButton = (ImageButton) findViewById(R.id.register_button);
 
         final Context context = this;
         registerButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(context, RegistrationActivity.class);
                startActivityForResult(intent, 0);
             }
         });
         loginButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(context, LoginActivity.class);
                startActivityForResult(intent, 0);
             }
         });
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            setResult(resultCode, data);
            finish();
        }
     }
 }
