 package edu.mit.moneyManager.view;
 
 import edu.mit.moneyManager.R;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.InputType;
 import android.text.SpannableString;
 import android.text.style.UnderlineSpan;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.PopupWindow;
 import android.widget.TextView;
 
 /**
  * This activity is where the user logs in.
  * 
  * Login
  *
  */
 public class LoginActivity extends Activity {
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.login);
         
         Button buttonLogin = (Button) findViewById(R.id.login_button);
         EditText username = (EditText) findViewById(R.id.login_username);
         username.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
         
         TextView switchToRegister = (TextView) findViewById(R.id.register_help);
        SpannableString contentUnderline1 = new SpannableString("New to MoneyManager? Register!");  
         contentUnderline1.setSpan(new UnderlineSpan(), 0, contentUnderline1.length(), 0);
         switchToRegister.setText(contentUnderline1);
         
         TextView forgotten = (TextView) findViewById(R.id.login_forgotten);
         SpannableString contentUnderline2 = new SpannableString("Forgot your username or password?");  
         contentUnderline2.setSpan(new UnderlineSpan(), 0, contentUnderline2.length(), 0);
         forgotten.setText(contentUnderline2);
         
         buttonLogin.setOnClickListener(new View.OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(v.getContext(), MMTabWidget.class);
                 startActivity(intent);
                 
             }
         });
         
         switchToRegister.setOnClickListener(new View.OnClickListener() {
             
             @Override
             public void onClick(View v){
                 Intent intent = new Intent(v.getContext(), RegisterActivity.class);
                 startActivity(intent);
                 
             }
             
         });
         forgotten.setOnClickListener(new View.OnClickListener() {
             
             @Override
             public void onClick(View v){
                 
                 
             }
             
         });
         
     }
 }
