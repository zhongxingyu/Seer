 /*
  * Copyright (C) 2013 Thomas Le
  * 
  * This file is part of Simple CryptoPass.
  *
  * Simple CryptoPass is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Simple CryptoPass is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public license
  * along with Simple CryptoPass. If not, see <http://www.gnu.org/licenses/>.
  */
 package no.haitech.simplecryptopass;
 
 import java.security.GeneralSecurityException;
 
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ClipboardManager;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 /**
  * This application is based on Chrome extension, CryptoPass.
  * Using PBKDF2 with SHA-256, 5000 iterations.
  * 
  * @author Thomas Le
  * @see https://github.com/haitech/SimpleCryptoPass for Android.
  * @see https://github.com/dchest/cryptopass for Chrome extension
  */
 public class MainActivity extends Activity {
     private EditText etSecret;
     private EditText etUsername;
     private EditText etUrl;
     private EditText etPasswordLength;
     private SeekBar sbPasswordLength;
     private TextView tvResult;
     private SharedPreferences prefs;
    private String hashedPassword = "";
 
     
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         // Initialize PreferenceManager
         prefs = PreferenceManager.getDefaultSharedPreferences(this);
         
         // Makes the window fullscreen, with titlebar.
         getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
         
         // Initialize Views
         etSecret = (EditText) findViewById(R.id.etSecret1);
         etUsername = (EditText) findViewById(R.id.etUsername1);
         etUrl = (EditText) findViewById(R.id.etUrl1);
         etPasswordLength = (EditText) findViewById(R.id.etResultLength1);
         sbPasswordLength = (SeekBar) findViewById(R.id.sbResultLength1);
         tvResult = (TextView) findViewById(R.id.tvResult1);
         
         // Handlers
         sbPasswordLength.setOnSeekBarChangeListener(
                 sbResultLengthChangeListener());
         etPasswordLength.addTextChangedListener(
                 etResultLengthTextChangedListener());
         
         // Check if the Remember preference is enabled. If so, auto fill.
         // TODO: ^^^^
         
         // Sets Seekbar to EditBox Password value.
         try {
             sbPasswordLength.setProgress(
                     Integer.parseInt(etPasswordLength.getText().toString()));
         } catch (NumberFormatException e) {
             updateResultLength(25);
             Toast.makeText(this, "Something went wrong with the Seekbar.",
                     Toast.LENGTH_LONG).show();
         }
     }
 
     
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Intent intent = new Intent(this, SettingsActivity.class);
         startActivity(intent);
         return true;
     }
     
     
     
     /**
      * OnClick for MakePassword button.
      * Make password and prints it to Result TextView.
      * 
      * @param view
      */
     public void onClickMakePassword(View view) {
         // Checks if Secret is filled out.
         if(isEmpty(etSecret)) {
             Toast.makeText(this, "Secret is empty.", Toast.LENGTH_LONG).show();
         } else {
             // Combinating username and url.
             String salt = etUsername.getText().toString() + "@" + etUrl.getText().toString();
             
             // Check if the preferences for background process is enabled.
             if(prefs.getBoolean("pref_bgProcess", true)) {
                 // Runs an AsyncTask
                 new PasswordMakeTask(etSecret.getText().toString(), salt).execute();
             } else {
                 try {
                    hashedPassword = new Password().make(
                            etSecret.getText().toString(), salt);
                    tvResult.setText(hashedPassword);
                 } catch (GeneralSecurityException e) {
                     tvResult.setText(null);
                 }
             }
         }
     }
     
     
     
     /**
      * OnClick for TextView Result. Copies the text into "Copy".
      * The method also checks for API level 8 (HONEYCOMB) or higher, and uses
      * {@link ClipboardManager} (android.content). If lower then uses 
      * {@link android.text.ClipboardManager} (android.text).
      * SupressWarning and SupressLint is handled by checking API level.
      * 
      * @param view
      */
     @SuppressWarnings("deprecation")
     @SuppressLint("NewApi")
     public void onClickResult(View view) {
         Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_LONG).show();
         
         
         // Checks for API Level.
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
              android.content.ClipboardManager clipboard =  
                      (android.content.ClipboardManager) 
                      getSystemService(CLIPBOARD_SERVICE); 
              android.content.ClipData clip = android.content.ClipData
                      .newPlainText("Password", tvResult.getText().toString());
              clipboard.setPrimaryClip(clip);
         } else{
             android.text.ClipboardManager clipboard = 
                     (android.text.ClipboardManager) 
                     getSystemService(CLIPBOARD_SERVICE); 
             clipboard.setText(tvResult.getText().toString());
         }
         Toast.makeText(this, "Copied to clipboard", 
                 Toast.LENGTH_SHORT).show();
         
     }
     
     
     
     /**
      * Checks if a EditText has empty string. Empty space is also checked.
      * 
      * @param et a {@code EditText}
      * @return true if EditText is empty otherwise false.
      */
     private boolean isEmpty(EditText et) {
         return (et.getText().toString().trim().length() > 0) ? false : true;
     }
     
     
     
     /**
      * Seekbar change listener for updating EditText on dragging the seek.
      * 
      * @return {@link OnSeekBarChangeListener}
      */
     private OnSeekBarChangeListener sbResultLengthChangeListener() {
         return new OnSeekBarChangeListener() {
             
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) { }
             
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) { }
             
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress,
                     boolean fromUser) {
                 if(fromUser) {
                     etPasswordLength.setText(Integer.toString(progress));
                    if(hashedPassword.trim().length() > 0) {
                        tvResult.setText(hashedPassword.subSequence(0, progress));
                    }
                 }
             }
             
         };
     }
     
     
     
     /**
      * Updates the result/password SeekBar when changing value in EditBox.
      * @return {@link TextWatcher}
      */
     private TextWatcher etResultLengthTextChangedListener() {
         return new TextWatcher() {
             
             @Override
             public void onTextChanged(CharSequence s, int start, int before, 
                     int count) {
             }
             
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count,
                     int after) {
             }
             
             @Override
             public void afterTextChanged(Editable s) {
                 if(!isEmpty(etPasswordLength)) {
                     int value = Integer.parseInt(
                             etPasswordLength.getText().toString());
                    if(value <= 44) { 
                        sbPasswordLength.setProgress(value);
                        if(hashedPassword.trim().length() > 0) {
                            tvResult.setText(hashedPassword.subSequence(0, value));
                        }
                    } else { updateResultLength(44); }
                 } else {
                     updateResultLength(0);
                 }
             }
         };
     }
     
     
     
     /**
      * Updated the Seekbar and EditText for password/result length.
      * @param value int value to be updated to.
      */
     private void updateResultLength(int value)  {
         etPasswordLength.setText(Integer.toString(value));
         sbPasswordLength.setProgress(value);
        
        if(hashedPassword.trim().length() > 0) {
            tvResult.setText(hashedPassword.subSequence(0, value));
        }
     }
     
     
     /*
      * An AsyncTask for making the password.
      */
     private class PasswordMakeTask extends AsyncTask<Void, Void, String> {
         private ProgressDialog dialog;
         private String secret, salt;
         
         
         
         protected PasswordMakeTask(String secret, String salt) {
             this.secret = secret;
             this.salt = salt;
             
             dialog = new ProgressDialog(MainActivity.this);
         }
         
         
         
         protected void onPreExecute() {
             dialog.setMessage("Making password...");
             dialog.show();
         }
         
         
         
         protected String doInBackground(Void... params) {
             try {
                 return new Password().make(secret, salt);
             } catch (GeneralSecurityException e) {
                 return null;
             }
             
         }
         
         
         
         protected void onPostExecute(String result) {
             if (dialog.isShowing()) {
                 dialog.dismiss();
             }
            hashedPassword = result; 
            tvResult.setText(hashedPassword.substring(0, Integer.parseInt(
                    etPasswordLength.getText().toString())));
         }
     }
     
 
 }
