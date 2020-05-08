 package com.example.androidApp;
 
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import com.example.androidApp.api.ApiResponse;
 import com.example.androidApp.api.ApiResponseCallbacks;
 import com.google.inject.Inject;
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
 
 public class HomeActivity extends RoboActivity {
 
     public
 
     @InjectView(R.id.loginEditText)
     EditText loginEditText;
 
     @InjectView(R.id.passwordEditText)
     EditText passwordEditText;
 
     @InjectView(R.id.signInButton)
     Button signInButton;
 
     @InjectView(R.id.signInResultTextView)
     TextView signInResultTextView;
 
     private
 
     @Inject
     AuthenticationGateway authenticationGateway;
 
     View.OnFocusChangeListener emptyEditTextOnFocusListener = new View.OnFocusChangeListener() {
         public void onFocusChange(View view, boolean focus) {
             if (focus) {
                 EditText editText = (EditText) view;
                 editText.setText("");
             }
         }
     };
 
     View.OnClickListener signInButtonClickListener = new View.OnClickListener() {
         public void onClick(View view) {
             String username = loginEditText.getText().toString();
             String password = passwordEditText.getText().toString();
 
             authenticationGateway.signIn(username, password, new ApiResponseCallbacks() {
                 public void onSuccess(ApiResponse response) {
                     signInResultTextView.setText(R.string.successSignInResultText);
                 }
 
                 public void onFailure(ApiResponse response) {
                     signInResultTextView.setText(R.string.errorSignInResultText);
                 }
 
                 public void onComplete(ApiResponse response) { }
             });
         }
     };
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         loginEditText.setOnFocusChangeListener(emptyEditTextOnFocusListener);
         passwordEditText.setOnFocusChangeListener(emptyEditTextOnFocusListener);
         signInButton.setOnClickListener(signInButtonClickListener);
     }
 }
