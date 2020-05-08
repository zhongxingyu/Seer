 package com.nullninjadev.tipify;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class Configuration extends Activity {
 	private EditText et_minTipRate;
 	private EditText et_maxTipRate;
 	private ToggleButton tb_deductions;
 	private ToggleButton tb_tax;
 	private final String TIP_RATES_INVALID = "The minimum tip rate can not be larger than the maximum tip rate!";
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.config);
         
        this.setTitle("Tipify - Configuration");
        
         initUI();
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event)  {
         if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
             if(DataHelper.getMaxTipRate() >= DataHelper.getMinTipRate()){
             	this.finish();
             }
             else{
             	Toast.makeText(this, TIP_RATES_INVALID, Toast.LENGTH_LONG).show();
             }
             return true;
         }
 
         return super.onKeyDown(keyCode, event);
     }
     
     /**
      * Returns a String in 0-100% format without the percent symbol
      * @param percent is the decimal percentage to be transformed
      * @return percent as a String in the 0-100% format without the percent symbol
      */
     public String getPercentInstanceWithoutSymbol(float percent){
     	percent *= 100;
     	return Float.toString(percent);
     }
     
     /**
      * Initializes the EditTexts, TextViews and ToggleButtons associated with this Activity.
      */
     private void initUI(){
     	et_minTipRate = (EditText)findViewById(R.id.txtbox_min_tip);
     	et_minTipRate.setText(getPercentInstanceWithoutSymbol(DataHelper.getMinTipRate()));
     	et_minTipRate.setSelectAllOnFocus(true);
     	et_minTipRate.addTextChangedListener(new TextWatcher(){
 			@Override
 			public void afterTextChanged(Editable arg0) {
 				if(et_minTipRate.length() >= 1)
     				DataHelper.setMinTipRate(Float.parseFloat(et_minTipRate.getText().toString())/100);
     			else
     				DataHelper.setMinTipRate(0/100);
 			}
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {}
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {}
     	});
     	
     	et_maxTipRate = (EditText)findViewById(R.id.txtbox_max_tip);
     	et_maxTipRate.setText(getPercentInstanceWithoutSymbol(DataHelper.getMaxTipRate()));
     	et_maxTipRate.setSelectAllOnFocus(true);
     	et_maxTipRate.addTextChangedListener(new TextWatcher(){
 			@Override
 			public void afterTextChanged(Editable arg0) {
 				if(et_maxTipRate.length() >= 1)
     				DataHelper.setMaxTipRate(Float.parseFloat(et_maxTipRate.getText().toString())/100);
     			else
     				DataHelper.setMaxTipRate(100/100);
 			}
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {}
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {}
     	});
     	
     	tb_deductions = (ToggleButton)findViewById(R.id.btn_deduction_toggle);
     	tb_deductions.setChecked(ComponentInclusion.isDeductionIncluded());
     	tb_deductions.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				ComponentInclusion.setDeductionInclusion(isChecked);
 			}
     	});
     	
     	tb_tax = (ToggleButton)findViewById(R.id.btn_tax_toggle);
     	tb_tax.setChecked(ComponentInclusion.isTaxIncluded());
     	tb_tax.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				ComponentInclusion.setTaxInclusion(isChecked);
 			}
     	});
     }
 }
