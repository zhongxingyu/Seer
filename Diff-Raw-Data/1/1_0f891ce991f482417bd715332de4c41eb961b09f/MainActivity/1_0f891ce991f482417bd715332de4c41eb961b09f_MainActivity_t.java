 package com.example.calculator;
 
 import java.util.Locale;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	
 	// TODO: fix UI to better define col/row
 	
 	//global variables
 	String operand1;
 	String operation;
 	String operand2;
 	String displayValue;
 	String baseLength;
 	boolean focusFlag = false;
 	boolean operationFlag = false;
 	int currentBase;
 	
 	//start of main code
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		//calculate metrics
 		DisplayMetrics metrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		
 		int width = metrics.widthPixels;
 		int height = metrics.heightPixels;
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 		TextView display_operation = (TextView) findViewById(R.id.display_operation);
 		TextView delete_button = (TextView) findViewById(R.id.delete_button);
 		Button bin_select = (Button) findViewById(R.id.bin_select);
 		Button oct_select = (Button) findViewById(R.id.oct_select);
 		Button dec_select = (Button) findViewById(R.id.dec_select);
 		Button hex_select = (Button) findViewById(R.id.hex_select);
 		Button d_button = (Button) findViewById(R.id.d_button);
 		Button e_button = (Button) findViewById(R.id.e_button);
 		Button f_button = (Button) findViewById(R.id.f_button);
 		Button clear_button = (Button) findViewById(R.id.clear_button);
 		Button a_button = (Button) findViewById(R.id.a_button);
 		Button b_button = (Button) findViewById(R.id.b_button);
 		Button c_button = (Button) findViewById(R.id.c_button);
 		Button div_button = (Button) findViewById(R.id.div_button);
 		Button button_7 = (Button) findViewById(R.id.button_7);
 		Button button_8 = (Button) findViewById(R.id.button_8);
 		Button button_9 = (Button) findViewById(R.id.button_9);
 		Button mult_button = (Button) findViewById(R.id.mult_button);
 		Button button_4 = (Button) findViewById(R.id.button_4);
 		Button button_5 = (Button) findViewById(R.id.button_5);
 		Button button_6 = (Button) findViewById(R.id.button_6);
 		Button sub_button = (Button) findViewById(R.id.sub_button);
 		Button button_1 = (Button) findViewById(R.id.button_1);
 		Button button_2 = (Button) findViewById(R.id.button_2);
 		Button button_3 = (Button) findViewById(R.id.button_3);
 		Button add_button = (Button) findViewById(R.id.add_button);
 		Button button_0 = (Button) findViewById(R.id.button_0);
 		Button plusminus_button = (Button) findViewById(R.id.plusminus_button);
 		Button equal_button = (Button) findViewById(R.id.equal_button);
 		Button mod_button = (Button) findViewById(R.id.mod_button);
 		//width formatting
 		display_operation.setWidth((width/4)-16);
 		display_secondary.setWidth(((width*3)/4)-16);
 		display_main.setWidth(((width*3)/4)-16);
 		delete_button.setWidth((width/4)-16);
 		bin_select.setWidth((width/4)-16);
 		oct_select.setWidth((width/4)-16);
 		dec_select.setWidth((width/4)-16);
 		hex_select.setWidth((width/4)-16);
 		d_button.setWidth((width/4)-16);
 		e_button.setWidth((width/4)-16);
 		f_button.setWidth((width/4)-16);
 		clear_button.setWidth((width/4)-16);
 		a_button.setWidth((width/4)-16);
 		b_button.setWidth((width/4)-16);
 		c_button.setWidth((width/4)-16);
 		div_button.setWidth((width/4)-16);
 		button_7.setWidth((width/4)-16);
 		button_8.setWidth((width/4)-16);
 		button_9.setWidth((width/4)-16);
 		mult_button.setWidth((width/4)-16);
 		button_4.setWidth((width/4)-16);
 		button_5.setWidth((width/4)-16);
 		button_6.setWidth((width/4)-16);
 		sub_button.setWidth((width/4)-16);
 		button_1.setWidth((width/4)-16);
 		button_2.setWidth((width/4)-16);
 		button_3.setWidth((width/4)-16);
 		add_button.setWidth((width/4)-16);
 		mult_button.setWidth((width/4)-16);
 		button_0.setWidth((width/4)-16);
 		plusminus_button.setWidth((width/4)-16);
 		equal_button.setWidth((width/4)-16);
 		mod_button.setWidth((width/4)-16);
 		//height formatting
 		display_operation.setHeight((height/9)-16);
 		display_secondary.setHeight((height/9)-16);
 		display_main.setHeight((height/9)-16);
 		delete_button.setHeight((height/9)-16);
 		bin_select.setHeight((height/9)-16);
 		oct_select.setHeight((height/9)-16);
 		dec_select.setHeight((height/9)-16);
 		hex_select.setHeight((height/9)-16);
 		d_button.setHeight((height/9)-16);
 		e_button.setHeight((height/9)-16);
 		f_button.setHeight((height/9)-16);
 		clear_button.setHeight((height/9)-16);
 		a_button.setHeight((height/9)-16);
 		b_button.setHeight((height/9)-16);
 		c_button.setHeight((height/9)-16);
 		div_button.setHeight((height/9)-16);
 		button_7.setHeight((height/9)-16);
 		button_8.setHeight((height/9)-16);
 		button_9.setHeight((height/9)-16);
 		mult_button.setHeight((height/9)-16);
 		button_4.setHeight((height/9)-16);
 		button_5.setHeight((height/9)-16);
 		button_6.setHeight((height/9)-16);
 		sub_button.setHeight((height/9)-16);
 		button_1.setHeight((height/9)-16);
 		button_2.setHeight((height/9)-16);
 		button_3.setHeight((height/9)-16);
 		add_button.setHeight((height/9)-16);
 		mult_button.setHeight((height/9)-16);
 		button_0.setHeight((height/9)-16);
 		plusminus_button.setHeight((height/9)-16);
 		equal_button.setHeight((height/9)-16);
 		mod_button.setHeight((height/9)-16);
 		//set defaults for base select buttons
     	f_button.setEnabled(false);
     	e_button.setEnabled(false);
     	d_button.setEnabled(false);
     	c_button.setEnabled(false);
     	b_button.setEnabled(false);
     	a_button.setEnabled(false);
     	button_9.setEnabled(false);
     	button_8.setEnabled(false);
     	button_7.setEnabled(false);
     	button_6.setEnabled(false);
     	button_5.setEnabled(false);
     	button_4.setEnabled(false);
     	button_3.setEnabled(false);
     	button_2.setEnabled(false);
     	button_1.setEnabled(true);
     	button_0.setEnabled(true);
     	plusminus_button.setEnabled(false);
     	bin_select.setTextColor(Color.parseColor("#33B5E5"));
     	oct_select.setTextColor(Color.parseColor("#FFFFFF"));
     	dec_select.setTextColor(Color.parseColor("#FFFFFF"));
     	hex_select.setTextColor(Color.parseColor("#FFFFFF"));
     	
     	
 		//required assignment for leading zero at onCreate()
     	operand1 = "0";
     	operand2 = "0";
 		displayValue = "0";
 		currentBase = 2;
 
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	//base conversion
 	// TODO: fix rare issue with binary numbers getting stuck
 	public void bin_select(View view){
 		
 		//button locking
 		Button d_button = (Button) findViewById(R.id.d_button);
 		Button e_button = (Button) findViewById(R.id.e_button);
 		Button f_button = (Button) findViewById(R.id.f_button);
 		Button a_button = (Button) findViewById(R.id.a_button);
 		Button b_button = (Button) findViewById(R.id.b_button);
 		Button c_button = (Button) findViewById(R.id.c_button);
 		Button button_7 = (Button) findViewById(R.id.button_7);
 		Button button_8 = (Button) findViewById(R.id.button_8);
 		Button button_9 = (Button) findViewById(R.id.button_9);
 		Button button_4 = (Button) findViewById(R.id.button_4);
 		Button button_5 = (Button) findViewById(R.id.button_5);
 		Button button_6 = (Button) findViewById(R.id.button_6);
 		Button button_1 = (Button) findViewById(R.id.button_1);
 		Button button_2 = (Button) findViewById(R.id.button_2);
 		Button button_3 = (Button) findViewById(R.id.button_3);
 		Button button_0 = (Button) findViewById(R.id.button_0);
     	f_button.setEnabled(false);
     	e_button.setEnabled(false);
     	d_button.setEnabled(false);
     	c_button.setEnabled(false);
     	b_button.setEnabled(false);
     	a_button.setEnabled(false);
     	button_9.setEnabled(false);
     	button_8.setEnabled(false);
     	button_7.setEnabled(false);
     	button_6.setEnabled(false);
     	button_5.setEnabled(false);
     	button_4.setEnabled(false);
     	button_3.setEnabled(false);
     	button_2.setEnabled(false);
     	button_1.setEnabled(true);
     	button_0.setEnabled(true);
 
     	Button bin_select = (Button) findViewById(R.id.bin_select);
     	Button oct_select = (Button) findViewById(R.id.oct_select);
     	Button dec_select = (Button) findViewById(R.id.dec_select);
     	Button hex_select = (Button) findViewById(R.id.hex_select);
     	bin_select.setTextColor(Color.parseColor("#33B5E5"));
     	oct_select.setTextColor(Color.parseColor("#FFFFFF"));
     	dec_select.setTextColor(Color.parseColor("#FFFFFF"));
     	hex_select.setTextColor(Color.parseColor("#FFFFFF"));
     	
     	//text_display conversion
     	if(displayValue != "0") {
 	    	TextView display_main = (TextView) findViewById(R.id.display_main);
 	    	displayValue = Long.toBinaryString(Long.parseLong(displayValue, currentBase));
 	    	display_main.setText(displayValue);
     	}
     	if((operand1 != "0") && (!focusFlag)) {
 	    	TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 	    	operand1 = Long.toBinaryString(Long.parseLong(operand1, currentBase));
 	    	display_secondary.setText(operand1);
     	}
     	
     	
     	currentBase = 2;
 	}
 	public void oct_select(View view){
 		
 		
 		//button locking
 		Button d_button = (Button) findViewById(R.id.d_button);
 		Button e_button = (Button) findViewById(R.id.e_button);
 		Button f_button = (Button) findViewById(R.id.f_button);
 		Button a_button = (Button) findViewById(R.id.a_button);
 		Button b_button = (Button) findViewById(R.id.b_button);
 		Button c_button = (Button) findViewById(R.id.c_button);
 		Button button_7 = (Button) findViewById(R.id.button_7);
 		Button button_8 = (Button) findViewById(R.id.button_8);
 		Button button_9 = (Button) findViewById(R.id.button_9);
 		Button button_4 = (Button) findViewById(R.id.button_4);
 		Button button_5 = (Button) findViewById(R.id.button_5);
 		Button button_6 = (Button) findViewById(R.id.button_6);
 		Button button_1 = (Button) findViewById(R.id.button_1);
 		Button button_2 = (Button) findViewById(R.id.button_2);
 		Button button_3 = (Button) findViewById(R.id.button_3);
 		Button button_0 = (Button) findViewById(R.id.button_0);
     	f_button.setEnabled(false);
     	e_button.setEnabled(false);
     	d_button.setEnabled(false);
     	c_button.setEnabled(false);
     	b_button.setEnabled(false);
     	a_button.setEnabled(false);
     	button_9.setEnabled(false);
     	button_8.setEnabled(false);
     	button_7.setEnabled(true);
     	button_6.setEnabled(true);
     	button_5.setEnabled(true);
     	button_4.setEnabled(true);
     	button_3.setEnabled(true);
     	button_2.setEnabled(true);
     	button_1.setEnabled(true);
     	button_0.setEnabled(true);
 
     	Button bin_select = (Button) findViewById(R.id.bin_select);
     	Button oct_select = (Button) findViewById(R.id.oct_select);
     	Button dec_select = (Button) findViewById(R.id.dec_select);
     	Button hex_select = (Button) findViewById(R.id.hex_select);
     	bin_select.setTextColor(Color.parseColor("#FFFFFF"));
     	oct_select.setTextColor(Color.parseColor("#33B5E5"));
     	dec_select.setTextColor(Color.parseColor("#FFFFFF"));
     	hex_select.setTextColor(Color.parseColor("#FFFFFF"));
     	
     	//text_display conversion
     	if(displayValue != "0") {
 	    	TextView display_main = (TextView) findViewById(R.id.display_main);
 	    	displayValue = Long.toOctalString(Long.parseLong(displayValue, currentBase));
 	    	display_main.setText(displayValue);
     	}
     	if((operand1 != "0") && (!focusFlag)) {
 	    	TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 	    	operand1 = Long.toOctalString(Long.parseLong(operand1, currentBase));
 	    	display_secondary.setText(operand1);
     	}
     	
     	currentBase = 8;
 	}
 	public void dec_select(View view){
 		
 		
 		//button locking
 		Button d_button = (Button) findViewById(R.id.d_button);
 		Button e_button = (Button) findViewById(R.id.e_button);
 		Button f_button = (Button) findViewById(R.id.f_button);
 		Button a_button = (Button) findViewById(R.id.a_button);
 		Button b_button = (Button) findViewById(R.id.b_button);
 		Button c_button = (Button) findViewById(R.id.c_button);
 		Button button_7 = (Button) findViewById(R.id.button_7);
 		Button button_8 = (Button) findViewById(R.id.button_8);
 		Button button_9 = (Button) findViewById(R.id.button_9);
 		Button button_4 = (Button) findViewById(R.id.button_4);
 		Button button_5 = (Button) findViewById(R.id.button_5);
 		Button button_6 = (Button) findViewById(R.id.button_6);
 		Button button_1 = (Button) findViewById(R.id.button_1);
 		Button button_2 = (Button) findViewById(R.id.button_2);
 		Button button_3 = (Button) findViewById(R.id.button_3);
 		Button button_0 = (Button) findViewById(R.id.button_0);
     	f_button.setEnabled(false);
     	e_button.setEnabled(false);
     	d_button.setEnabled(false);
     	c_button.setEnabled(false);
     	b_button.setEnabled(false);
     	a_button.setEnabled(false);
     	button_9.setEnabled(true);
     	button_8.setEnabled(true);
     	button_7.setEnabled(true);
     	button_6.setEnabled(true);
     	button_5.setEnabled(true);
     	button_4.setEnabled(true);
     	button_3.setEnabled(true);
     	button_2.setEnabled(true);
     	button_1.setEnabled(true);
     	button_0.setEnabled(true);
 
     	Button bin_select = (Button) findViewById(R.id.bin_select);
     	Button oct_select = (Button) findViewById(R.id.oct_select);
     	Button dec_select = (Button) findViewById(R.id.dec_select);
     	Button hex_select = (Button) findViewById(R.id.hex_select);
     	bin_select.setTextColor(Color.parseColor("#FFFFFF"));
     	oct_select.setTextColor(Color.parseColor("#FFFFFF"));
     	dec_select.setTextColor(Color.parseColor("#33B5E5"));
     	hex_select.setTextColor(Color.parseColor("#FFFFFF"));
     	
     	
     	//text_display conversion
     	if(displayValue != "0") {
 	    	TextView display_main = (TextView) findViewById(R.id.display_main);
 	    	displayValue = Long.toString(Long.parseLong(displayValue, currentBase));
 	    	display_main.setText(displayValue);
     	}
     	
     	if((operand1 != "0") && (!focusFlag)) {
 	    	TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 	    	operand1 = Long.toString(Long.parseLong(operand1, currentBase));
 	    	display_secondary.setText(operand1);
     	}
     	
     	currentBase = 10;
 	}
 	public void hex_select(View view){
 		
 		
 		//button locking
 		Button d_button = (Button) findViewById(R.id.d_button);
 		Button e_button = (Button) findViewById(R.id.e_button);
 		Button f_button = (Button) findViewById(R.id.f_button);
 		Button a_button = (Button) findViewById(R.id.a_button);
 		Button b_button = (Button) findViewById(R.id.b_button);
 		Button c_button = (Button) findViewById(R.id.c_button);
 		Button button_7 = (Button) findViewById(R.id.button_7);
 		Button button_8 = (Button) findViewById(R.id.button_8);
 		Button button_9 = (Button) findViewById(R.id.button_9);
 		Button button_4 = (Button) findViewById(R.id.button_4);
 		Button button_5 = (Button) findViewById(R.id.button_5);
 		Button button_6 = (Button) findViewById(R.id.button_6);
 		Button button_1 = (Button) findViewById(R.id.button_1);
 		Button button_2 = (Button) findViewById(R.id.button_2);
 		Button button_3 = (Button) findViewById(R.id.button_3);
 		Button button_0 = (Button) findViewById(R.id.button_0);
     	f_button.setEnabled(true);
     	e_button.setEnabled(true);
     	d_button.setEnabled(true);
     	c_button.setEnabled(true);
     	b_button.setEnabled(true);
     	a_button.setEnabled(true);
     	button_9.setEnabled(true);
     	button_8.setEnabled(true);
     	button_7.setEnabled(true);
     	button_6.setEnabled(true);
     	button_5.setEnabled(true);
     	button_4.setEnabled(true);
     	button_3.setEnabled(true);
     	button_2.setEnabled(true);
     	button_1.setEnabled(true);
     	button_0.setEnabled(true);
 
     	Button bin_select = (Button) findViewById(R.id.bin_select);
     	Button oct_select = (Button) findViewById(R.id.oct_select);
     	Button dec_select = (Button) findViewById(R.id.dec_select);
     	Button hex_select = (Button) findViewById(R.id.hex_select);
     	bin_select.setTextColor(Color.parseColor("#FFFFFF"));
     	oct_select.setTextColor(Color.parseColor("#FFFFFF"));
     	dec_select.setTextColor(Color.parseColor("#FFFFFF"));
     	hex_select.setTextColor(Color.parseColor("#33B5E5"));
     	
     	//text_display conversion
     	if(displayValue != "0"){
     	TextView display_main = (TextView) findViewById(R.id.display_main);
 	    	displayValue = Long.toHexString(Long.parseLong(displayValue, currentBase));
 	    	display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
     	}
     	if((operand1 != "0") && (!focusFlag)) {
 	    	TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 	    	operand1 = Long.toHexString(Long.parseLong(operand1, currentBase));
 	    	display_secondary.setText(operand1.toUpperCase(Locale.ENGLISH));
     	}
     	
     	currentBase = 16;
 	}
 	
 	//input buttons
 	public void send_0(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(focusFlag) {
 			displayValue = "0";
 			focusFlag = false;
 		}
 		if(displayValue == "0")
 			displayValue = "0";
 		else {
 			displayValue = display_main.getText().toString();
 			displayValue += "0";
 		}
 		display_main.setText(displayValue);
 	}
 	public void send_1(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(focusFlag) {
 			displayValue = "0";
 			focusFlag = false;
 		}
 		if(displayValue == "0")
 			displayValue = "1";
 		else {
 			displayValue = display_main.getText().toString();
 			displayValue += "1";
 		}
 		display_main.setText(displayValue);
 	}
 	public void send_2(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(focusFlag) {
 			displayValue = "0";
 			focusFlag = false;
 		}
 		if(displayValue == "0")
 			displayValue = "2";
 		else {
 			displayValue = display_main.getText().toString();
 			displayValue += "2";
 		}
 		
 		display_main.setText(displayValue);
 	}	
 	public void send_3(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(focusFlag) {
 			displayValue = "0";
 			focusFlag = false;
 		}
 		if(displayValue == "0")
 			displayValue = "3";
 		else {
 			displayValue = display_main.getText().toString();
 			displayValue += "3";
 		}
 		display_main.setText(displayValue);
 	}	
 	
 	public void send_4(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(focusFlag) {
 			displayValue = "0";
 			focusFlag = false;
 		}
 		if(displayValue == "0")
 			displayValue = "4";
 		else {
 			displayValue = display_main.getText().toString();
 			displayValue += "4";
 		}
 		display_main.setText(displayValue);
 	}
 	public void send_5(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(focusFlag) {
 			displayValue = "0";
 			focusFlag = false;
 		}
 		if(displayValue == "0")
 			displayValue = "5";
 		else {
 			displayValue = display_main.getText().toString();
 			displayValue += "5";
 		}
 		display_main.setText(displayValue);
 	}
 	public void send_6(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(focusFlag) {
 			displayValue = "0";
 			focusFlag = false;
 		}
 		if(displayValue == "0")
 			displayValue = "6";
 		else {
 			displayValue = display_main.getText().toString();
 			displayValue += "6";
 		}
 		display_main.setText(displayValue);
 	}
 
 	public void send_7(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(focusFlag) {
 			displayValue = "0";
 			focusFlag = false;
 		}
 		if(displayValue == "0")
 			displayValue = "7";
 		else {
 			displayValue = display_main.getText().toString();
 			displayValue += "7";
 		}
 		display_main.setText(displayValue);
 	}
 	public void send_8(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(focusFlag) {
 			displayValue = "0";
 			focusFlag = false;
 		}
 		if(displayValue == "0")
 			displayValue = "8";
 		else {
 			displayValue = display_main.getText().toString();
 			displayValue += "8";
 		}
 		display_main.setText(displayValue);
 	}
 	public void send_9(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(focusFlag) {
 			displayValue = "0";
 			focusFlag = false;
 		}
 		if(displayValue == "0")
 			displayValue = "9";
 		else {
 			displayValue = display_main.getText().toString();
 			displayValue += "9";
 		}
 		display_main.setText(displayValue);
 	}	
 
 	public void send_a(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(focusFlag) {
 			displayValue = "0";
 			focusFlag = false;
 		}
 		if(displayValue == "0")
 			displayValue = "A";
 		else {
 			displayValue = display_main.getText().toString();
 			displayValue += "A";
 		}
 		display_main.setText(displayValue);
 	}
 	public void send_b(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(focusFlag) {
 			displayValue = "0";
 			focusFlag = false;
 		}
 		if(displayValue == "0")
 			displayValue = "B";
 		else {
 			displayValue = display_main.getText().toString();
 			displayValue += "B";
 		}
 		display_main.setText(displayValue);
 	}
 	public void send_c(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(focusFlag) {
 			displayValue = "0";
 			focusFlag = false;
 		}
 		if(displayValue == "0")
 			displayValue = "C";
 		else {
 			displayValue = display_main.getText().toString();
 			displayValue += "C";
 		}
 		display_main.setText(displayValue);
 	}
 	
 	public void send_d(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(focusFlag) {
 			displayValue = "0";
 			focusFlag = false;
 		}
 		if(displayValue == "0")
 			displayValue = "D";
 		else {
 			displayValue = display_main.getText().toString();
 			displayValue += "D";
 		}
 		display_main.setText(displayValue);
 	}
 	public void send_e(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(focusFlag) {
 			displayValue = "0";
 			focusFlag = false;
 		}
 		if(displayValue == "0")
 			displayValue = "E";
 		else {
 			displayValue = display_main.getText().toString();
 			displayValue += "E";
 		}
 		display_main.setText(displayValue);
 	}
 	public void send_f(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(focusFlag) {
 			displayValue = "0";
 			focusFlag = false;
 		}
 		if(displayValue == "0")
 			displayValue = "F";
 		else {
 			displayValue = display_main.getText().toString();
 			displayValue += "F";
 		}
 		display_main.setText(displayValue);
 	}
 
 	//control buttons
 	public void send_delete(View view) {
 		// TODO: work on delete logic
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(displayValue.length() > 0){
 			displayValue = displayValue.substring(0, displayValue.length() - 1);
 			if(displayValue.length() == 0){
 				displayValue = "0";
 			}
 		}
 		display_main.setText(displayValue);
 	}
 	public void send_clear(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_operation = (TextView) findViewById(R.id.display_operation);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 		displayValue = display_main.getText().toString();
 		// TODO: fix to only adjust required values
 		focusFlag = false;
 		displayValue = "0";
 		operand1 = "0";
 		operand2 = "0";
 		display_operation.setText("");
 		display_main.setText(displayValue);
 		display_secondary.setText("");
 		
 	}
 	public void send_plusminus(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		int holdNegative;
 		
 		switch (currentBase) {
 		case 2:
 			
 			holdNegative = Integer.parseInt(operand1, currentBase);
 			displayValue = Integer.toBinaryString(holdNegative);
 			break;
 		case 8:
 			holdNegative = Integer.parseInt(operand1, currentBase);
 			displayValue = Integer.toOctalString(holdNegative);
 			break;
 		case 10:
 			holdNegative = Integer.parseInt(operand1, currentBase);
 			displayValue = Integer.toString(holdNegative);
 			break;
 		case 16:
 			holdNegative = Integer.parseInt(operand1, currentBase);
 			displayValue = Integer.toHexString(holdNegative);
 			break;
 	}
 
 		displayValue = display_main.getText().toString();
 		displayValue += "+/-";
 		display_main.setText(displayValue);
 	}
 	public void send_equal(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 		TextView display_operation = (TextView) findViewById(R.id.display_operation);
 		operand2 = display_main.getText().toString();
 
 		long solutionInt = 0;
 		if(currentBase == 2) {
 			
 		}
 		else if(currentBase == 8) {
 			
 		}
 		else if(currentBase == 10) {
 			
 		}
 		else if(currentBase == 16) {
 			
 		}
 		if(operation == "+") {
 			switch (currentBase) {
 				case 2:
 					solutionInt = Long.parseLong(operand1, currentBase) + Long.parseLong(operand2, currentBase);
 					displayValue = Long.toBinaryString(solutionInt);
 					break;
 				case 8:
 					solutionInt = Long.parseLong(operand1, currentBase) + Long.parseLong(operand2, currentBase);
 					displayValue = Long.toOctalString(solutionInt);
 					break;
 				case 10:
 					solutionInt = Long.parseLong(operand1, currentBase) + Long.parseLong(operand2, currentBase);
 					displayValue = Long.toString(solutionInt);
 					break;
 				case 16:
 					solutionInt = Long.parseLong(operand1, currentBase) + Long.parseLong(operand2, currentBase);
 					displayValue = Long.toHexString(solutionInt);
 					break;
 			}
 			display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
 			display_secondary.setText("");
 			display_operation.setText("");
 		}
 		else if(operation == "-") {
 			switch (currentBase) {
 				case 2:
 					solutionInt = Long.parseLong(operand1, currentBase) - Long.parseLong(operand2, currentBase);
 					displayValue = Long.toBinaryString(solutionInt);
 					break;
 				case 8:
 					solutionInt = Long.parseLong(operand1, currentBase) - Long.parseLong(operand2, currentBase);
 					displayValue = Long.toOctalString(solutionInt);
 					break;
 				case 10:
 					solutionInt = Long.parseLong(operand1, currentBase) - Long.parseLong(operand2, currentBase);
 					displayValue = Long.toString(solutionInt);
 					break;
 				case 16:
 					solutionInt = Long.parseLong(operand1, currentBase) - Long.parseLong(operand2, currentBase);
 					displayValue = Long.toHexString(solutionInt);
 					break;
 			}
 			display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
 			display_secondary.setText("");
 			display_operation.setText("");
 		}
 		else if(operation == "*") {
 			switch (currentBase) {
 				case 2:
 					solutionInt = Long.parseLong(operand1, currentBase) * Long.parseLong(operand2, currentBase);
 					displayValue = Long.toBinaryString(solutionInt);
 					break;
 				case 8:
 					solutionInt = Long.parseLong(operand1, currentBase) * Long.parseLong(operand2, currentBase);
 					displayValue = Long.toOctalString(solutionInt);
 					break;
 				case 10:
 					solutionInt = Long.parseLong(operand1, currentBase) * Long.parseLong(operand2, currentBase);
 					displayValue = Long.toString(solutionInt);
 					break;
 				case 16:
 					solutionInt = Long.parseLong(operand1, currentBase) * Long.parseLong(operand2, currentBase);
 					displayValue = Long.toHexString(solutionInt);
 					break;
 			}
 			display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
 			display_secondary.setText("");
 			display_operation.setText("");
 		}
 		else if(operation == "/") {
 			if(operand2 == "0") {
 				display_secondary.setText("ERROR:");
 				display_main.setText("DIV BY ZERO");
 				display_operation.setText(null);
 			}
 			else {
 				switch (currentBase) {
 					case 2:
 						solutionInt = Long.parseLong(operand1, currentBase) / Long.parseLong(operand2, currentBase);
 						displayValue = Long.toBinaryString(solutionInt);
 						break;
 					case 8:
 						solutionInt = Long.parseLong(operand1, currentBase) / Long.parseLong(operand2, currentBase);
 						displayValue = Long.toOctalString(solutionInt);
 						break;
 					case 10:
 						solutionInt = Long.parseLong(operand1, currentBase) / Long.parseLong(operand2, currentBase);
 						displayValue = Long.toString(solutionInt);
 						break;
 					case 16:
 						solutionInt = Long.parseLong(operand1, currentBase) / Long.parseLong(operand2, currentBase);
 						displayValue = Long.toHexString(solutionInt);
 						break;
 				}
 				display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
 				display_secondary.setText("");
 				display_operation.setText("");
 			}
 		}
 		else if(operation == "%") {
 			switch (currentBase) {
 				case 2:
 					solutionInt = Integer.parseInt(operand1, currentBase) % Integer.parseInt(operand2, currentBase);
 					displayValue = Long.toBinaryString(solutionInt);
 					break;
 				case 8:
 					solutionInt = Integer.parseInt(operand1, currentBase) % Integer.parseInt(operand2, currentBase);
 					displayValue = Long.toOctalString(solutionInt);
 					break;
 				case 10:
 					solutionInt = Integer.parseInt(operand1, currentBase) % Integer.parseInt(operand2, currentBase);
 					displayValue = Long.toString(solutionInt);
 					break;
 				case 16:
 					solutionInt = Integer.parseInt(operand1, currentBase) % Integer.parseInt(operand2, currentBase);
 					displayValue = Long.toHexString(solutionInt);
 					break;
 			}
 			display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
 			display_secondary.setText("");
 			display_operation.setText("");
 		}
 		// TODO: need default cases for switch statements
 
 		
 		operand1 = displayValue;
		operand2 = "0";
 		focusFlag = true;
 	}
 	
 	//operations
 	// TODO: add serial operations
 	public void send_add(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_operation = (TextView) findViewById(R.id.display_operation);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);		
 		long solutionInt = 0;
 		operation = "+";
 		// TODO: add logic for serial operations
 		if(operand1 != "0") {
 			operand2 = display_main.getText().toString();
 		}
 		else {
 			operand1 = display_main.getText().toString();
 		}
 		display_secondary.setText(operand1);
 		if((operand1 != "0") && (operand2 != "0")) {
 			switch (currentBase) {
 				case 2:
 					solutionInt = Long.parseLong(operand1, currentBase) + Long.parseLong(operand2, currentBase);
 					displayValue = Long.toBinaryString(solutionInt);
 					break;
 				case 8:
 					solutionInt = Long.parseLong(operand1, currentBase) + Long.parseLong(operand2, currentBase);
 					displayValue = Long.toOctalString(solutionInt);
 					break;
 				case 10:
 					solutionInt = Long.parseLong(operand1, currentBase) + Long.parseLong(operand2, currentBase);
 					displayValue = Long.toString(solutionInt);
 					break;
 				case 16:
 					solutionInt = Long.parseLong(operand1, currentBase) + Long.parseLong(operand2, currentBase);
 					displayValue = Long.toHexString(solutionInt);
 					break;
 				default:
 					displayValue = "error default";
 					break;
 			}
 			display_secondary.setText(displayValue);
 			operand1 = displayValue;
 			operand2 = "0";
 			displayValue = "0";
 			display_main.setText(displayValue);
 			display_operation.setText(operation);
 		}
 		else {
 			displayValue = "0";
 			display_operation.setText(operation);
 		}
 
 	}
 	public void send_sub(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_operation = (TextView) findViewById(R.id.display_operation);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 		long solutionInt = 0;
 		operation = "-";
 		// TODO: add logic for serial operations
 		if(operand1 != "0") {
 			operand2 = display_main.getText().toString();
 		}
 		else {
 			operand1 = display_main.getText().toString();
 		}
 		display_secondary.setText(operand1);
 		if((operand1 != "0") && (operand2 != "0")) {
 			switch (currentBase) {
 				case 2:
 					solutionInt = Long.parseLong(operand1, currentBase) - Long.parseLong(operand2, currentBase);
 					displayValue = Long.toBinaryString(solutionInt);
 					break;
 				case 8:
 					solutionInt = Long.parseLong(operand1, currentBase) - Long.parseLong(operand2, currentBase);
 					displayValue = Long.toOctalString(solutionInt);
 					break;
 				case 10:
 					solutionInt = Long.parseLong(operand1, currentBase) - Long.parseLong(operand2, currentBase);
 					displayValue = Long.toString(solutionInt);
 					break;
 				case 16:
 					solutionInt = Long.parseLong(operand1, currentBase) - Long.parseLong(operand2, currentBase);
 					displayValue = Long.toHexString(solutionInt);
 					break;
 				default:
 					displayValue = "error default";
 					break;
 			}
 			display_secondary.setText(displayValue);
 			operand1 = displayValue;
 			operand2 = "0";
 			displayValue = "0";
 			display_main.setText(displayValue);
 			display_operation.setText(operation);
 		}
 		else {
 			displayValue = "0";
 			display_operation.setText(operation);
 		}
 	}
 	public void send_mult(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_operation = (TextView) findViewById(R.id.display_operation);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 		long solutionInt = 0;
 		operation = "*";
 		// TODO: add logic for serial operations
 		if(operand1 != "0") {
 			operand2 = display_main.getText().toString();
 		}
 		else {
 			operand1 = display_main.getText().toString();
 		}
 		display_secondary.setText(operand1);
 		if((operand1 != "0") && (operand2 != "0")) {
 			switch (currentBase) {
 				case 2:
 					solutionInt = Long.parseLong(operand1, currentBase) * Long.parseLong(operand2, currentBase);
 					displayValue = Long.toBinaryString(solutionInt);
 					break;
 				case 8:
 					solutionInt = Long.parseLong(operand1, currentBase) * Long.parseLong(operand2, currentBase);
 					displayValue = Long.toOctalString(solutionInt);
 					break;
 				case 10:
 					solutionInt = Long.parseLong(operand1, currentBase) * Long.parseLong(operand2, currentBase);
 					displayValue = Long.toString(solutionInt);
 					break;
 				case 16:
 					solutionInt = Long.parseLong(operand1, currentBase) * Long.parseLong(operand2, currentBase);
 					displayValue = Long.toHexString(solutionInt);
 					break;
 				default:
 					displayValue = "error default";
 					break;
 			}
 			display_secondary.setText(displayValue);
 			operand1 = displayValue;
 			operand2 = "0";
 			displayValue = "0";
 			display_main.setText(displayValue);
 			display_operation.setText(operation);
 		}
 		else {
 			displayValue = "0";
 			display_operation.setText(operation);
 		}
 	}
 	public void send_div(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_operation = (TextView) findViewById(R.id.display_operation);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 		long solutionInt = 0;
 		operation = "/";
 		// TODO: add logic for serial operations
 		if(operand1 != "0") {
 			operand2 = display_main.getText().toString();
 		}
 		else {
 			operand1 = display_main.getText().toString();
 		}
 		display_secondary.setText(operand1);
 		if((operand1 != "0") && (operand2 != "0")) {
 			switch (currentBase) {
 				case 2:
 					solutionInt = Long.parseLong(operand1, currentBase) / Long.parseLong(operand2, currentBase);
 					displayValue = Long.toBinaryString(solutionInt);
 					break;
 				case 8:
 					solutionInt = Long.parseLong(operand1, currentBase) / Long.parseLong(operand2, currentBase);
 					displayValue = Long.toOctalString(solutionInt);
 					break;
 				case 10:
 					solutionInt = Long.parseLong(operand1, currentBase) / Long.parseLong(operand2, currentBase);
 					displayValue = Long.toString(solutionInt);
 					break;
 				case 16:
 					solutionInt = Long.parseLong(operand1, currentBase) / Long.parseLong(operand2, currentBase);
 					displayValue = Long.toHexString(solutionInt);
 					break;
 				default:
 					displayValue = "error default";
 					break;
 			}
 			display_secondary.setText(displayValue);
 			operand1 = displayValue;
 			operand2 = "0";
 			displayValue = "0";
 			display_main.setText(displayValue);
 			display_operation.setText(operation);
 		}
 		else {
 			displayValue = "0";
 			display_operation.setText(operation);
 		}
 	}
 	public void send_mod(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_operation = (TextView) findViewById(R.id.display_operation);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 		long solutionInt = 0;
 		operation = "%";
 		// TODO: add logic for serial operations
 		if(operand1 != "0") {
 			operand2 = display_main.getText().toString();
 		}
 		else {
 			operand1 = display_main.getText().toString();
 		}
 		display_secondary.setText(operand1);
 		if((operand1 != "0") && (operand2 != "0")) {
 			switch (currentBase) {
 				case 2:
 					solutionInt = Long.parseLong(operand1, currentBase) % Long.parseLong(operand2, currentBase);
 					displayValue = Long.toBinaryString(solutionInt);
 					break;
 				case 8:
 					solutionInt = Long.parseLong(operand1, currentBase) % Long.parseLong(operand2, currentBase);
 					displayValue = Long.toOctalString(solutionInt);
 					break;
 				case 10:
 					solutionInt = Long.parseLong(operand1, currentBase) % Long.parseLong(operand2, currentBase);
 					displayValue = Long.toString(solutionInt);
 					break;
 				case 16:
 					solutionInt = Long.parseLong(operand1, currentBase) % Long.parseLong(operand2, currentBase);
 					displayValue = Long.toHexString(solutionInt);
 					break;
 				default:
 					displayValue = "error default";
 					break;
 			}
 			display_secondary.setText(displayValue);
 			operand1 = displayValue;
 			operand2 = "0";
 			displayValue = "0";
 			display_main.setText(displayValue);
 			display_operation.setText(operation);
 		}
 		else {
 			displayValue = "0";
 			display_operation.setText(operation);
 		}
 	}
 }
 
