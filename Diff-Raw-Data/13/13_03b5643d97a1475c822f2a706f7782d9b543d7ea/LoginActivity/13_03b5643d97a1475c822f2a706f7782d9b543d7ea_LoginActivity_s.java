 package com.baggers.bagboy;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class LoginActivity extends Activity{
 
     public void onCreate(Bundle savedInstanceState) {
     	
         super.onCreate(savedInstanceState);
         
         // set default screen to login GUI
         setContentView(R.layout.activity_login);
        
      //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         
         Button loginButton = (Button) findViewById(R.id.loginButton);
         Button registerButton = (Button) findViewById(R.id.registerButton);
         
         //Sets listener for login button 
         loginButton.setOnClickListener(
 	           
         		new View.OnClickListener() {
 	                
 	                
                 private String username;
                 private String password;
 
                 public void onClick(View v) {
                 	
                         EditText usernameEdit = (EditText)findViewById(R.id.username);
                         EditText passwordEdit = (EditText)findViewById(R.id.password);
                         
                         username = usernameEdit.getText().toString();
                         password = passwordEdit.getText().toString();
                         
                         //options the user has from this point
                         //1. invalid user name/password
                         //2. correct user name/password in which case it goes to the home screen 
                         
                         //check userID and password
                         if (LoginManager.checkLogin(username, password)) {
 	                        Intent i = new Intent(LoginActivity.this, HomeActivity.class);
 	                    	startActivity(i); 
                         }
                         else {
                         	//message stating that the username password are incorrect
                         	LoginManager.getError();
                         	
                         	//TODO: error messages 
                         	//Toast.makeText(HomeActivity.this, "Yay box checked", Toast.LENGTH_LONG).show();
                         }
               
                 }
         		});
 
         //listener for the register button
         registerButton.setOnClickListener(
     	        
         		new View.OnClickListener() {
 
                 public void onClick(View v) {
                 	
                 	//takes the user to the register screen
                 	Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                 	startActivity(i);
                      
                 }
         		});
     }
     
 }
