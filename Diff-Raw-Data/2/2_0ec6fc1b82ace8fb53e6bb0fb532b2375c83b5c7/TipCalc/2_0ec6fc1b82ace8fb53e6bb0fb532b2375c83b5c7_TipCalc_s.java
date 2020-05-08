 package com.example.tipcalc;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 public class TipCalc extends Activity {
 	
 	// Constants used when saving and restoring
 
 	private static final String TOTAL_BILL = "TOTAL_BILL";
 	private static final String CURRENT_TIP = "CURRENT_TIP";
 	private static final String BILL_WITHOUT_TIP = "BILL_WITHOUT_TIP";
 	
 	private double billBeforeTip;
 	private double tipAmount;
 	private double finalBill;
 	
 	EditText billBeforeTipEditText;
 	EditText tipAmountEditText;
 	EditText finalBillEditText;
 	
 	private SeekBar tipSeekBar;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_tip_calc); // Initiate the GUI
 		
 		// Check if app just started, or if it is being restored
 		
 		if(savedInstanceState == null) {
 			
 			// Just started
 			
 			billBeforeTip = 0.0;
 			tipAmount = .15;
 			finalBill = 0.0;
 			
 		} else {
 			
 			// App is being restored
 			
 			billBeforeTip = savedInstanceState.getDouble(BILL_WITHOUT_TIP);
 			tipAmount = savedInstanceState.getDouble(CURRENT_TIP);
 			finalBill = savedInstanceState.getDouble(TOTAL_BILL);
 			
 		}
 		
 		// Initialize the EditTexts
 		
 		billBeforeTipEditText = (EditText) findViewById(R.id.billEditText);
 		tipAmountEditText = (EditText) findViewById(R.id.tipEditText);
 		finalBillEditText = (EditText) findViewById(R.id.finalBillEditText);
 		
 		tipSeekBar = (SeekBar) findViewById(R.id.changeTipSeekBar);
 		
 		tipSeekBar.setOnSeekBarChangeListener(tipSeekBarListener);
 		
 		// Add change listener for when the bill before tip is changed
 		
 		billBeforeTipEditText.addTextChangedListener(billBeforeTipListener);
 		
 	}
 	
 	private TextWatcher billBeforeTipListener = new TextWatcher() {
 
 		@Override
 		public void afterTextChanged(Editable s) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count,
 				int after) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void onTextChanged(CharSequence s, int start, int before,
 				int count) {
 			
 			try {
 				
 				billBeforeTip = Double.parseDouble(s.toString());
 				
 			} catch(NumberFormatException e) {
 				
 				billBeforeTip = 0.0;
 				
 			}
 			
 			updateTipAndFinalBill();
 			
 		}
 		
 	};
 	
 	private void updateTipAndFinalBill() {
 		
 		double tipAmount = Double.parseDouble(tipAmountEditText.getText().toString());
 		double finalBill = billBeforeTip + (billBeforeTip * tipAmount);
 		
 		finalBillEditText.setText(String.format("%.02f", finalBill));
 		
 	}
 	
 	protected void onSaveInstanceState(Bundle outState) {
 		
 		super.onSaveInstanceState(outState);
 		
 		outState.putDouble(TOTAL_BILL, finalBill);
 		outState.putDouble(CURRENT_TIP, tipAmount);
 		outState.putDouble(BILL_WITHOUT_TIP, billBeforeTip);
 	
 	}
 	
 	private OnSeekBarChangeListener tipSeekBarListener = new OnSeekBarChangeListener() {
 
 		@Override
 		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
 			
 			tipAmount = (tipSeekBar.getProgress()) * .01;
 			
			tipAmountEditText.setText(String.format("%0.02f", tipAmount));
 			
 			updateTipAndFinalBill();
 			
 		}
 
 		@Override
 		public void onStartTrackingTouch(SeekBar seekBar) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void onStopTrackingTouch(SeekBar seekBar) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	};
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.tip_calc, menu);
 		return true;
 	}
 
 }
