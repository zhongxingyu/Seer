 package ru.peppers;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class PasswordActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.password);
 
         Button passwordButton = (Button) findViewById(R.id.passwordButton);
         passwordButton.setOnClickListener(new Button.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 SharedPreferences settings = getSharedPreferences(PozivnoiActivity.PREFS_NAME, 0);
                Log.d("My_tag", settings.getString("passwordApp", ""));
 
                 EditText passwordEditText = (EditText) findViewById(R.id.passwordEditText);
 
                if (settings.getString("passwordApp", "").equals(passwordEditText.getText().toString())) {
                     Bundle extras = getIntent().getExtras();
                     // int id = extras.getInt("id");
 
                     Intent intent = new Intent(PasswordActivity.this, MainListActivity.class);
 
                     // Bundle bundle = new Bundle();
                     // bundle.putInt("id", id);
                     // intent.putExtras(bundle);
                     startActivity(intent);
                     finish();
                 } else {
                     onErrorPassword();
                 }
             }
 
         });
 
         EditText passwordEditText = (EditText) findViewById(R.id.passwordEditText);
 
         passwordEditText.setOnKeyListener(new EditText.OnKeyListener() {
 
             @Override
             public boolean onKey(View v, int keyCode, KeyEvent event) {
 
                 EditText passwordEditText = (EditText) v;
 
                 if (keyCode == EditorInfo.IME_ACTION_SEARCH || keyCode == EditorInfo.IME_ACTION_DONE
                         || event.getAction() == KeyEvent.ACTION_DOWN
                         && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
 
                     if (!event.isShiftPressed() && passwordEditText.getText().toString().length() != 0) {
                         SharedPreferences settings = getSharedPreferences(PozivnoiActivity.PREFS_NAME, 0);
                        Log.d("My_tag", settings.getString("passwordApp", ""));
                        if (settings.getString("passwordApp", "").equals(passwordEditText.getText().toString())) {
                             Bundle extras = getIntent().getExtras();
                             // int id = extras.getInt("id");
 
                             Intent intent = new Intent(PasswordActivity.this, MainListActivity.class);
 
                             // Bundle bundle = new Bundle();
                             // bundle.putInt("id", id);
                             startActivity(intent);
                             finish();
                         } else {
                             onErrorPassword();
                         }
                     }
 
                 }
                 return false; // pass on to other listeners.
             }
 
         });
     }
 
     private void onErrorPassword() {
         new AlertDialog.Builder(PasswordActivity.this).setTitle("")
                 .setMessage(" .  ").setNeutralButton("", null)
                 .show();
     }
 }
