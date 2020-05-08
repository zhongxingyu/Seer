 package com.cs301w01.meatload.authentication.activities;
 
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import com.cs301w01.meatload.R;
 import com.cs301w01.meatload.activities.Skindactivity;
 import com.cs301w01.meatload.authentication.Controllers.UserManager;
 import com.cs301w01.meatload.authentication.Model.Patient;
 import com.cs301w01.meatload.authentication.Model.Specialist;
 import com.cs301w01.meatload.authentication.Model.User;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Derek
  * Date: 3/26/12
  * Time: 2:32 PM
  * To change this template use File | Settings | File Templates.
  */
 public class LoginActivity extends Skindactivity {
 
     private EditText usernameField;
     private EditText passwordField;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.login);
         
         usernameField = (EditText) findViewById(R.id.usernameField);
         passwordField = (EditText) findViewById(R.id.passwordField);
 
         createListeners();
     }
 
     public void createListeners() {
         final Button loginButton = (Button) findViewById(R.id.loginButton);
         loginButton.setOnClickListener(new View.OnClickListener() {
            //@Override
             public void onClick(View view) {
                 login();
             }
         });
         
         final Button signupButton = (Button) findViewById(R.id.signupButton);
         signupButton.setOnClickListener(new View.OnClickListener() {
            //@Override
             public void onClick(View view) {
                 signup();
             }
         });
         
     }
 
     private void signup() {
 
         Intent myIntent = new Intent();
         myIntent.setClassName("com.cs301w01.meatload",
                 "com.cs301w01.meatload.authentication.activities.SignupActivity");
         startActivity(myIntent);
 
     }
 
     private void login() {
 
         String username = usernameField.getText().toString();
         String pw = passwordField.getText().toString();
         
         UserManager um = new UserManager(this);
         User u = um.authenticateUser(username, pw);
 
         if(u.getClass() == Patient.class) {
 
             Intent myIntent = new Intent();
             myIntent.setClassName("com.cs301w01.meatload",
                     "com.cs301w01.meatload.Skindex");
             myIntent.putExtra("user", u);
 
             startActivity(myIntent);
 
         } else if(u.getClass() == Specialist.class) {
 
             Intent myIntent = new Intent();
             myIntent.setClassName("com.cs301w01.meatload",
                     "com.cs301w01.meatload.authentication.activities.ViewPatientsActivity");
             myIntent.putExtra("user", u);
 
             startActivity(myIntent);
 
         } else {
 
             //TODO: Add invalid credentials alert
 
         }
         
     }
     
     
     @Override
     public void update(Object model) {
 
     }
 }
