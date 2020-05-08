 package ru.peppers;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 
 public class SettingsActivity extends Activity {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.settings);
 
         //Bundle bundle = getIntent().getExtras();
        // int id = bundle.getInt("id");
 
         CheckBox box = (CheckBox) findViewById(R.id.checkBox3);
         SharedPreferences settings = getSharedPreferences(PozivnoiActivity.PREFS_NAME, 0);
         boolean checked = settings.getBoolean("isPassword", false);
         box.setChecked(checked);
         box.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
 
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 
                 SharedPreferences settings = getSharedPreferences(PozivnoiActivity.PREFS_NAME, 0);
                 SharedPreferences.Editor editor = settings.edit();
                 editor.putBoolean("isPassword", isChecked);
 
                 // Commit the edits!
                 editor.commit();
 
             }
 
         });
 
         final EditText passwordEditText = (EditText) findViewById(R.id.editText1);
        passwordEditText.setText(settings.getString("password", ""));
         passwordEditText.addTextChangedListener(new TextWatcher() {
             public void afterTextChanged(Editable s) {
                 SharedPreferences settings = getSharedPreferences(PozivnoiActivity.PREFS_NAME, 0);
                 SharedPreferences.Editor editor = settings.edit();
                editor.putString("password", passwordEditText.getText().toString());
                 editor.commit();
                Log.d("My_tag", "password" + passwordEditText.getText().toString());
             }
 
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {
             }
 
             public void onTextChanged(CharSequence s, int start, int before, int count) {
             }
         });
     }
 }
