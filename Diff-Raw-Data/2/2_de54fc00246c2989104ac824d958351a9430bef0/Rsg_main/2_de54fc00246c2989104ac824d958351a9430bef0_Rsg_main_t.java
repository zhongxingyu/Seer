 package com.google.ruvolof.randomsequencegenerator;
 
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.TextView;
 import android.app.Activity;
 
 public class Rsg_main extends Activity implements OnClickListener, OnCheckedChangeListener {
 	
 	View range_layout_1;
 	View range_layout_2;
 	View manual_layout;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_rsg_main);
     	this.range_layout_1 = (View)findViewById(R.id.dlu_range);
     	this.range_layout_2 = (View)findViewById(R.id.s_range);
     	this.manual_layout = (View)findViewById(R.id.manual_layout);
         Button create = (Button)findViewById(R.id.button_create);
         create.setOnClickListener(this);
         RadioGroup rg = (RadioGroup)findViewById(R.id.radio_group);
         rg.setOnCheckedChangeListener(this);
     }
     
     public void onCheckedChanged (RadioGroup rg, int newchecked) {
     	switch (newchecked) {
     		case R.id.class_radio:
     			this.range_layout_1.setVisibility(View.VISIBLE);
     			this.range_layout_2.setVisibility(View.VISIBLE);		
     			this.manual_layout.setVisibility(View.GONE);
     			break;
     		case R.id.manual_radio:
     			this.manual_layout.setVisibility(View.VISIBLE);
     			this.range_layout_1.setVisibility(View.GONE);
     			this.range_layout_2.setVisibility(View.GONE);	
     			break;
     		default:
     			this.manual_layout.setVisibility(View.GONE);
     			this.range_layout_1.setVisibility(View.GONE);
     			this.range_layout_2.setVisibility(View.GONE);
     	}
     }
     
     public void onClick (View v) {
     	int clicked = v.getId();
     	switch (clicked) {
     		case R.id.button_create:
    	    	String chars = "";
     	    	String result = "";
     			
     			RadioGroup rg = (RadioGroup)findViewById(R.id.radio_group);
     	    	int selected = rg.getCheckedRadioButtonId();
     	    	
     	    	switch (selected) {
     	    		case R.id.binary_radio:
     	    			chars = chars.concat("01");
     	    			break;
     	    			
     	    		case R.id.hex_radio:
     	    			chars = chars.concat("0123456789ABCDEF");
     	    			break;
     	    			
     	    		case R.id.class_radio:
     	    			CheckBox digit = (CheckBox)findViewById(R.id.range_digit);
     	    			CheckBox lowercase = (CheckBox)findViewById(R.id.range_lowercase);
     	    			CheckBox uppercase = (CheckBox)findViewById(R.id.range_uppercase);
     	    			CheckBox special = (CheckBox)findViewById(R.id.range_special);
     	    			if (digit.isChecked()) {
     	    				chars = chars.concat("0123456789");
     	    			}
     	    			if (lowercase.isChecked()) {
     	    				chars = chars.concat("qwertyuiopasdfghjklzxcvbnm");
     	    			}
     	    			if (uppercase.isChecked()) {
     	    				chars = chars.concat("QWERTYUIOPASDFGHJKLZXCVBNM");
     	    			}
     	    			if (special.isChecked()) {
     	    				chars = chars.concat("$%&/()=?@#<>_-£[]*");
     	    			}
     	    			break;
     	    			
     	    		case R.id.manual_radio:
     	    			TextView manual = (TextView)findViewById(R.id.manual);
     	    			String chars_to_add = manual.getText().toString();
     	    			chars = chars.concat(chars_to_add);
     	    			break;
     	    		
     	    		default:
     	    			break;
     	    	}
     	  
     	    	int chars_last_index = chars.length() - 1;
     	    	TextView length_textview = (TextView)findViewById(R.id.string_length);
     	    	String length_as_string = length_textview.getText().toString();
     	    	int selected_length = Integer.parseInt(length_as_string, 10);
     	    	for (int i = selected_length; i > 0; i--) {
     	    		long random = Math.round(Math.random()*chars_last_index);
     	    		int index = (int) random;
     	    		String to_concat = String.valueOf(chars.charAt(index));
     	    		result = result.concat(to_concat);
     	    	}
     	    	
     	    	TextView output = (TextView)findViewById(R.id.output_textview);
     	    	output.setText(chars);
     	    	break;
     	    
     	    default:
     	    	break;
     	}
     }
 }
