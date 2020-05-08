 package com.nullninjadev.tipify;
 
 import java.text.NumberFormat;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.EditText;
 import android.widget.RatingBar;
 import android.widget.RatingBar.OnRatingBarChangeListener;
 import android.widget.TextView;
 
 public class Tipify extends Activity {
 	private boolean isInitialized = false;
 	
 	protected EditText et_numGuests;
 	protected EditText et_billTotal;
 	protected EditText et_deductions;
 	protected EditText et_tax;
 
 	protected TextView tv_tipRate;
 	protected TextView tv_totalTip;
 	protected TextView tv_individualTip;
 	protected TextView tv_tipAndBillTotal;
 	
 	protected RatingBar rb_servQuality;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         this.initVars();
         DataHelper.initVars();
         updateUI();
     }
 
     /**
      * Initializes the TextViews, EditTexts and the RatingBar that are displayed on
      * the Tipify screen.
      */
     private void initVars(){
     	tv_tipRate = (TextView)findViewById(R.id.txt_tip_rate);
     	tv_totalTip = (TextView)findViewById(R.id.txt_total_tip);
     	tv_individualTip = (TextView)findViewById(R.id.txt_indiv_tip);
     	tv_tipAndBillTotal = (TextView)findViewById(R.id.txt_total);
     	
     	et_numGuests = (EditText)findViewById(R.id.txtbox_num_guests);
     	et_billTotal = (EditText)findViewById(R.id.txtbox_total);
     	et_deductions = (EditText)findViewById(R.id.txtbox_deductions);
     	et_tax = (EditText)findViewById(R.id.txtbox_tax);
     	
     	et_numGuests.addTextChangedListener(new TextWatcher(){
     		@Override
     		public void afterTextChanged(Editable s) {
     			String input = et_numGuests.getText().toString();
     			if(input.length() > 0){
     				int numGuests = Integer.parseInt(input);
     				if(numGuests == 0){
     					et_numGuests.setText("");
     					numGuests = 1;
     				}
     				DataHelper.setNumGuests(numGuests);
     				DataHelper.calculateTotalTip();
     				updateUI();
     			}
     		}
     		@Override
     		public void beforeTextChanged(CharSequence s, int start, int count,
     				int after) {}
     		@Override
     		public void onTextChanged(CharSequence s, int start, int before,
     				int count) {}
         });
     	
     	et_billTotal.addTextChangedListener(new TextWatcher(){
     		@Override
     		public void afterTextChanged(Editable s) {
     			if(et_billTotal.length() >= 1)
     				DataHelper.setBillTotal(Float.parseFloat((et_billTotal.getText().toString())));
     			else
     				DataHelper.setBillTotal(0); // set bill total to $0 if et_billTotal is empty
     			DataHelper.calculateTotalTip();
     			updateUI(); // update the UI to reflect changes
     		}
     		@Override
     		public void beforeTextChanged(CharSequence s, int start, int count,
     				int after) {}
     		@Override
     		public void onTextChanged(CharSequence s, int start, int before,
     				int count) {}
         });
     	
     	et_deductions.addTextChangedListener(new TextWatcher(){
     		@Override
     		public void afterTextChanged(Editable s) {
     			if(et_deductions.length() >= 1)
     				DataHelper.setDeductions(Float.parseFloat(et_deductions.getText().toString()));
     			else
     				DataHelper.setDeductions(0);
     			DataHelper.calculateTotalTip();
     		}
     		@Override
     		public void beforeTextChanged(CharSequence s, int start, int count,
     				int after) {}
     		@Override
     		public void onTextChanged(CharSequence s, int start, int before,
     				int count) {}	
         });
     	
     	et_tax.addTextChangedListener(new TextWatcher(){
     		@Override
     		public void afterTextChanged(Editable s) {
     			if(et_tax.length() >= 1)
     				DataHelper.setTax(Float.parseFloat(et_tax.getText().toString()));
     			else
     				DataHelper.setTax(0);
     			DataHelper.calculateTotalTip();
     		}
     		@Override
     		public void beforeTextChanged(CharSequence s, int start, int count,
     				int after) {}
     		@Override
     		public void onTextChanged(CharSequence s, int start, int before,
     				int count) {}	
         });
     	
     	rb_servQuality = (RatingBar)findViewById(R.id.ratingbar_quality);
     	rb_servQuality.setOnRatingBarChangeListener(new OnRatingBarChangeListener(){
 			@Override
 			public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
 				float maxRating = rb_servQuality.getNumStars();
 				float maxTip = DataHelper.getMaxTipRate();
 				float minTip = DataHelper.getMinTipRate();
 				
 				DataHelper.setTipRate(minTip + ((rating/maxRating)*(maxTip-minTip)));
 				DataHelper.calculateTotalTip();
 				updateUI();
 			}
     	});
     	
     	isInitialized = true;
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu){
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.tipify_menu, menu);
     	return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item){
     	switch(item.getItemId()){
     	case R.id.tip_tailoring:
     		loadTipTailoring();
     		return true;
     	case R.id.configuration:
     		loadConfiguration();
     		return true;
     	default:
     		return super.onOptionsItemSelected(item);
     	}
     }
     
     /**
      * Starts up the TipTailoring Activity.
      */
     private void loadTipTailoring(){
     	Intent intent = new Intent(this, TipTailor.class);
     	startActivity(intent);
     }
     
     /**
      * Starts up the Configuration Activity.
      */
     private void loadConfiguration(){
     	Intent intent = new Intent(this, Configuration.class);
     	startActivity(intent);
     }
     
     /**
      * Updates the display according to the current tip rate, total tip
      * individual tip and tip/bill total. 
      */
     public void updateUI(){
     	tv_tipRate.setText(NumberFormat.getPercentInstance().format(DataHelper.getTipRate()));
     	tv_totalTip.invalidate();
     	tv_totalTip.setText(NumberFormat.getCurrencyInstance().format(DataHelper.getTotalTip()));
     	tv_totalTip.invalidate();
     	tv_individualTip.setText(NumberFormat.getCurrencyInstance().format(DataHelper.getIndividualTip()));
     	tv_tipAndBillTotal.setText(NumberFormat.getCurrencyInstance().format((DataHelper.getBillTotal() + DataHelper.getTotalTip())));
     }
     
     @Override
     public void onStart(){
     	
     	if(isInitialized){
     		DataHelper.calculateTotalTip();
     		updateUI();
     	}
     	super.onStart();
     }
 }
