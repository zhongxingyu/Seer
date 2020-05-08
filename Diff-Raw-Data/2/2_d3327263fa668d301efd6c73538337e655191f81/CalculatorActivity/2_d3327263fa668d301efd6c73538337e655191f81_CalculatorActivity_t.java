 package com.codepath.tipcalculator;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class CalculatorActivity extends Activity {
 	private EditText etTransaction;
 	private TextView tvTipValue;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_calculator);
         etTransaction = (EditText) findViewById(R.id.etTransaction);
         tvTipValue = (TextView) findViewById(R.id.tvTipValue);
         
         etTransaction.setImeOptions(EditorInfo.IME_ACTION_DONE);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.calculator, menu);
         return true;
     }
     
     public void showTip(View v) {
     	float transaction;
     	
     	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);    	
     	
     	try {
     		transaction = Float.parseFloat(etTransaction.getText().toString());
     	} catch (NumberFormatException e) {
     		transaction = 0;
     	}
     	
     	float percent = Float.parseFloat(v.getTag().toString());
     	tvTipValue.setText("$" + String.format("%.2f", transaction * percent));
     }
     
 }
