 package com.example.TipCalculator;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class TipCalculator extends Activity{
 
     private static final String BILL_TOTAL = "BILL_TOTAL";
     private static final String CUSTOM_PERCENT = "CUSTOM_PERCENT";
 
     private double currentBillTotal; //use value from user input
     private int currentCustomPercent; // %tip , from SeekBar
     private EditText billEditText; //user input
     private EditText tip10EditText;
     private EditText tip15EditText;
     private EditText tip20EditText;
     private EditText total10EditText;
     private EditText total15EditText;
     private EditText total20EditText;
     private EditText tipCustomEditText;
     private EditText totalCustomEditText;
     private TextView customTipTextView;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
        //TODO:add method to save bill and percent

         if(savedInstanceState == null){     //if first run
             currentBillTotal = 0.0;
             currentCustomPercent = 18;
         }
         else{ //get values from saved data
             currentBillTotal = savedInstanceState.getDouble(BILL_TOTAL);
             currentCustomPercent = savedInstanceState.getInt(CUSTOM_PERCENT);
         }
 
         tip10EditText = (EditText) findViewById(R.id.tip10EditText);
         total10EditText = (EditText) findViewById(R.id.total10EditText);
 
         tip15EditText = (EditText) findViewById(R.id.tip15EditText);
         total15EditText = (EditText) findViewById(R.id.total15EditText);
 
         tip20EditText = (EditText) findViewById(R.id.tip20EditText);
         total20EditText = (EditText) findViewById(R.id.total20EditText);
 
         tipCustomEditText = (EditText) findViewById(R.id.tipCustomEditText);
         totalCustomEditText = (EditText) findViewById(R.id.totalCustomEditText);
         customTipTextView = (TextView) findViewById(R.id.customTipTextView);
 
         billEditText = (EditText) findViewById(R.id.billEditText);
         billEditText.addTextChangedListener(billEditTextWatcher);
         //TODO:create billEditTextWatcher object (nested anonymous class)
 
         SeekBar customSeekBar = (SeekBar) findViewById(R.id.customSeekBar);
         customSeekBar.setOnSeekBarChangeListener(customSeekBarListener);
 
 
     }
 
     private void updateStandard(){
 
         double tenPercentTip = currentBillTotal * 0.10;
         double tenPercentTotal = currentBillTotal + tenPercentTip;
 
        tip10EditText.setText(String.format("%0.2f", tenPercentTip));
        total10EditText.setText(String.format("%0.2f", tenPercentTotal));
 
         double fifteenPercentTip = currentBillTotal * 0.15;
         double fifteenPercentTotal = currentBillTotal + fifteenPercentTip;
 
        tip15EditText.setText(String.format("%0.2f", fifteenPercentTip));
        total15EditText.setText(String.format("%0.2f", fifteenPercentTotal));
 
         double twentyPercentTip = currentBillTotal * 0.20;
         double twentyPercentTotal = currentBillTotal + twentyPercentTip;
 
        tip20EditText.setText(String.format("%0.2f", twentyPercentTip));
        total20EditText.setText(String.format("%0.2f", twentyPercentTotal));
 
 
     }
 
     private void updateCustom(){
 
         customTipTextView.setText(currentCustomPercent + " %"); //set custom % value text
 
         //calc custom tip
        double customTipAmount = currentBillTotal * currentCustomPercent * 0.1;
 
         //calc total custom bill
         double customTotalAmount = currentBillTotal + customTipAmount;
 
         //set calculated values to fields
         tipCustomEditText.setText(String.format(" %.02f", customTipAmount));
         totalCustomEditText.setText(String.format(" %.02f", customTotalAmount));
 
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
         outState.putDouble(BILL_TOTAL, currentBillTotal);
         outState.putDouble(CUSTOM_PERCENT, currentCustomPercent);
     }
 
     private OnSeekBarChangeListener customSeekBarListener = new OnSeekBarChangeListener() {
         @Override
         public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 
             currentCustomPercent = seekBar.getProgress();
             updateCustom();
         }
 
         @Override
         public void onStartTrackingTouch(SeekBar seekBar) {
 
         }
 
         @Override
         public void onStopTrackingTouch(SeekBar seekBar) {
 
         }
     };
 
     private TextWatcher billEditTextWatcher = new TextWatcher() {
         @Override
         public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
 
         }
 
         @Override
         public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
             try{
                 currentBillTotal = Double.parseDouble(charSequence.toString());
             }
             catch (NumberFormatException e){
                 currentBillTotal = 0.0;
             }
             updateStandard();
             updateCustom();
 
         }
 
         @Override
         public void afterTextChanged(Editable editable) {
 
         }
     };
 }
