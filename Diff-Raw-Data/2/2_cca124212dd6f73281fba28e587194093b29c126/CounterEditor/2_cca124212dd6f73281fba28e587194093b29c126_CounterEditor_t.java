 package com.example.knitknit;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.TextView;
 
 public class CounterEditor extends Activity {
    private static final String TAG = "knitknit-CounterEditor";
     private Counter mCounter;
     private Resources mResources;
 
     // UI Elements
     private EditText mNameText;
     private RadioButton mUpdownUp;
     private RadioButton mUpdownDown;
     private CheckBox mPatternCheckBox;
     private EditText mPatternNumber;
     private TextView mPatternEndText;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         getActionBar().setDisplayHomeAsUpEnabled(true);
 
         setContentView(R.layout.countereditor);
 
         mCounter = CountingLand.selectedCounter;
 
         // If counter ID is still null, there is a problem and we need
         // to get out of here since we can't do anything
         if (mCounter == null) {
             finish();
         }
 
         mResources = getResources();
 
         setupUI();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case android.R.id.home:
                 //Intent intent = new Intent(this, CountingLand.class);
                 //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 //startActivity(intent);
                 finish();
                 return true;
             default:
                 return false;
         }
     }
 
     @Override
     protected void onPause() {
         Log.w(TAG, "in onPause");
         super.onPause();
         saveState();
     }
 
     private void saveState() {
         mCounter.setPatternLength(Integer.parseInt(mPatternNumber.getText().toString()));
         mCounter.setName(mNameText.getText().toString());
     }
 
     private void setupUI() {
         // Find counter name box
         mNameText = (EditText) findViewById(R.id.countereditor_name_text);
 
         Log.w(TAG, "mNameText: " + mNameText);
 
         // Fill name box with counter's name
         if (mCounter.getName() != null) {
             mNameText.append(mCounter.getName());
         }
 
         // Create an OnClickListener for the updown radio buttons
         OnClickListener updown_listener = new OnClickListener() {
             public void onClick(View v) {
                 Log.w(TAG, "clicked radiobutton");
 
                 RadioButton rb = (RadioButton) v;
                 if (rb.getId() == R.id.countereditor_updown_up) {
                     Log.w(TAG, "clicked UP");
                     mCounter.setCountUp(true);
                 } else {
                     Log.w(TAG, "clicked DOWN");
                     mCounter.setCountUp(false);
                 }
             }
         };
 
         // Find updown RadioButtons
         mUpdownUp = (RadioButton) findViewById(R.id.countereditor_updown_up);
         mUpdownDown = (RadioButton) findViewById(R.id.countereditor_updown_down);
 
         // Attach OnClickListeners
         mUpdownUp.setOnClickListener(updown_listener);
         mUpdownDown.setOnClickListener(updown_listener);
 
         // Select the correct radiobutton based on the current setting
         if (mCounter.getCountUp()) {
             mUpdownUp.setChecked(true);
         } else {
             mUpdownDown.setChecked(true);
         }
 
 
         // Find pattern checkbox
         mPatternCheckBox = (CheckBox) findViewById(R.id.countereditor_pattern_checkbox);
 
         // Check/uncheck the checkbox based on the current setting
         mPatternCheckBox.setChecked(mCounter.getPatternEnabled());
 
         // Callback function for when the checkbox changes
         mPatternCheckBox.setOnCheckedChangeListener(
             new CheckBox.OnCheckedChangeListener() {
                 @Override
                 public void onCheckedChanged(CompoundButton v, boolean checked) {
                     Log.w(TAG, "checkbox changed");
                     
                     // Make text normal/dim
                     refreshPattern();
 
                     // Enable/disable pattern mode
                     mCounter.setPatternEnabled(checked);
                 }
             });
 
 
         // Find pattern length number
         mPatternNumber = (EditText) findViewById(R.id.countereditor_pattern_number);
 
         // Set the number based on the current setting
         mPatternNumber.append(Long.toString(mCounter.getPatternLength()));
 
         // Find end of pattern text (" rows")
         mPatternEndText = (TextView) findViewById(R.id.countereditor_pattern_endtext);
 
         refreshPattern();
     }
 
     private void refreshPattern() {
         // Set color to normal or dim depending on the toggle state
         int color = mResources.getColor(
             mPatternCheckBox.isChecked() ?
             R.color.countereditor_text :
             R.color.countereditor_disabled);
 
         // Set color of checkbox text
         mPatternCheckBox.setTextColor(color);
 
         // Enable/disable pattern number EditText
         mPatternNumber.setEnabled(mPatternCheckBox.isChecked());
         
         // Set color of end text
         mPatternEndText.setTextColor(color);
     }
 }
