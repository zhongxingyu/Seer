 /********************************************************************************
  *******************************PROJECT INFORMATION*******************************
 
  Project:		Programming Calculator
  Author: 		Taylor Carrington
  Created: 		August 3, 2013
  Repository: 	https://github.com/tcarrington/android-calculator
  Description:	Programming Calculator - Do calculations and convert numbers in binary, octal, decimal, hexadecimal.
  Notes:			This is my first Java and Android project.  Lots of mistakes but learning lots! : )
  Goal:			Post on Google Play!
 
  ********************************************************************************/
 
 package programming.calculator;
 
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
 
 	int INT_MAX = 2147483647;
 	int INT_MIN = -2147483648;
 	
 	//global variables
 	String operand1;
 	String operation;
 	String operand2;
 	String displayValue;
     String holdRecentEqualOperand;
 	
 	boolean recentEqualFlag = false;
 	boolean divZeroFlag = false;
 	
 	int currentBase;
 	int currentBaseStr;
 
 	
 	//cannot switch on string, using numbers
 	/*
 	 * 1: "+"
 	 * 2: "-"
 	 * 3: "*"
 	 * 4: "/"
 	 * 5: "%"
 	 */
 	int previousOperation;
 	
 	
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
     	plusminus_button.setEnabled(true);
     	bin_select.setTextColor(Color.parseColor("#FFFFFF"));
     	oct_select.setTextColor(Color.parseColor("#FFFFFF"));
     	dec_select.setTextColor(Color.parseColor("#33B5E5"));
     	hex_select.setTextColor(Color.parseColor("#FFFFFF"));
     	
     	((TextView) findViewById(R.id.display_main)).setTextSize(35);
     	((TextView) findViewById(R.id.display_secondary)).setTextSize(35);
     	
 		//required assignment for leading zero at onCreate()
     	operand1 = "0";
     	operand2 = "0";
 		displayValue = "0";
         operation="";
         holdRecentEqualOperand="0";
 		currentBase = 10;
 		recentEqualFlag = false;
 
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		//getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	//base conversion
 	public void bin_select(View view){
 		
 		//button locking
 		((Button) findViewById(R.id.d_button)).setEnabled(false);
 		((Button) findViewById(R.id.e_button)).setEnabled(false);
 		((Button) findViewById(R.id.f_button)).setEnabled(false);
 		((Button) findViewById(R.id.a_button)).setEnabled(false);
 		((Button) findViewById(R.id.b_button)).setEnabled(false);
 		((Button) findViewById(R.id.c_button)).setEnabled(false);
 		((Button) findViewById(R.id.button_7)).setEnabled(false);
 		((Button) findViewById(R.id.button_8)).setEnabled(false);
 		((Button) findViewById(R.id.button_9)).setEnabled(false);
 		((Button) findViewById(R.id.button_4)).setEnabled(false);
 		((Button) findViewById(R.id.button_5)).setEnabled(false);
 		((Button) findViewById(R.id.button_6)).setEnabled(false);
 		((Button) findViewById(R.id.button_1)).setEnabled(true);
 		((Button) findViewById(R.id.button_2)).setEnabled(false);
 		((Button) findViewById(R.id.button_3)).setEnabled(false);
 		((Button) findViewById(R.id.button_0)).setEnabled(true);
 
 		((Button) findViewById(R.id.bin_select)).setTextColor(Color.parseColor("#33B5E5"));
     	((Button) findViewById(R.id.oct_select)).setTextColor(Color.parseColor("#FFFFFF"));
     	((Button) findViewById(R.id.dec_select)).setTextColor(Color.parseColor("#FFFFFF"));
     	((Button) findViewById(R.id.hex_select)).setTextColor(Color.parseColor("#FFFFFF"));
     	
     	((TextView) findViewById(R.id.display_main)).setTextSize(13);
     	((TextView) findViewById(R.id.display_secondary)).setTextSize(13);
     	
     	//text_display conversion
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 		
     	displayValue = Integer.toBinaryString(Long.valueOf(displayValue, currentBase).intValue());
     	operand1 = Integer.toBinaryString(Long.valueOf(operand1, currentBase).intValue());
     	operand2 = Integer.toBinaryString(Long.valueOf(operand2, currentBase).intValue());
         holdRecentEqualOperand = Integer.toBinaryString(Long.valueOf(holdRecentEqualOperand, currentBase).intValue());
     	
     	display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
     	
     	
     	if((!operand1.equals("0")) && (!recentEqualFlag)) {
     		display_secondary.setText(operand1.toUpperCase(Locale.ENGLISH));
     	}
     	
     	
     	
     	currentBase = 2;
     	currentBaseStr = 32;
 	}
 	public void oct_select(View view){
 		
 		//button locking
 		((Button) findViewById(R.id.d_button)).setEnabled(false);
 		((Button) findViewById(R.id.e_button)).setEnabled(false);
 		((Button) findViewById(R.id.f_button)).setEnabled(false);
 		((Button) findViewById(R.id.a_button)).setEnabled(false);
 		((Button) findViewById(R.id.b_button)).setEnabled(false);
 		((Button) findViewById(R.id.c_button)).setEnabled(false);
 		((Button) findViewById(R.id.button_7)).setEnabled(true);
 		((Button) findViewById(R.id.button_8)).setEnabled(false);
 		((Button) findViewById(R.id.button_9)).setEnabled(false);
 		((Button) findViewById(R.id.button_4)).setEnabled(true);
 		((Button) findViewById(R.id.button_5)).setEnabled(true);
 		((Button) findViewById(R.id.button_6)).setEnabled(true);
 		((Button) findViewById(R.id.button_1)).setEnabled(true);
 		((Button) findViewById(R.id.button_2)).setEnabled(true);
 		((Button) findViewById(R.id.button_3)).setEnabled(true);
 		((Button) findViewById(R.id.button_0)).setEnabled(true);
 
 		((Button) findViewById(R.id.bin_select)).setTextColor(Color.parseColor("#FFFFFF"));
     	((Button) findViewById(R.id.oct_select)).setTextColor(Color.parseColor("#33B5E5"));
     	((Button) findViewById(R.id.dec_select)).setTextColor(Color.parseColor("#FFFFFF"));
     	((Button) findViewById(R.id.hex_select)).setTextColor(Color.parseColor("#FFFFFF"));
     	
     	((TextView) findViewById(R.id.display_main)).setTextSize(35);
     	((TextView) findViewById(R.id.display_secondary)).setTextSize(35);
     	
     	//text_display conversion
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 		
     	displayValue = Integer.toOctalString(Long.valueOf(displayValue, currentBase).intValue());
     	operand1 = Integer.toOctalString(Long.valueOf(operand1, currentBase).intValue());
     	operand2 = Integer.toOctalString(Long.valueOf(operand2, currentBase).intValue());
         holdRecentEqualOperand = Integer.toOctalString(Long.valueOf(holdRecentEqualOperand, currentBase).intValue());
     	
     	display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
     	
     	if((!operand1.equals("0")) && (!recentEqualFlag)) {
     		display_secondary.setText(operand1.toUpperCase(Locale.ENGLISH));
     	}
 
     	
     	currentBase = 8;
     	currentBaseStr = 11;
 	}
 	public void dec_select(View view){
 		
 		
 		//button locking
 		((Button) findViewById(R.id.d_button)).setEnabled(false);
 		((Button) findViewById(R.id.e_button)).setEnabled(false);
 		((Button) findViewById(R.id.f_button)).setEnabled(false);
 		((Button) findViewById(R.id.a_button)).setEnabled(false);
 		((Button) findViewById(R.id.b_button)).setEnabled(false);
 		((Button) findViewById(R.id.c_button)).setEnabled(false);
 		((Button) findViewById(R.id.button_7)).setEnabled(true);
 		((Button) findViewById(R.id.button_8)).setEnabled(true);
 		((Button) findViewById(R.id.button_9)).setEnabled(true);
 		((Button) findViewById(R.id.button_4)).setEnabled(true);
 		((Button) findViewById(R.id.button_5)).setEnabled(true);
 		((Button) findViewById(R.id.button_6)).setEnabled(true);
 		((Button) findViewById(R.id.button_1)).setEnabled(true);
 		((Button) findViewById(R.id.button_2)).setEnabled(true);
 		((Button) findViewById(R.id.button_3)).setEnabled(true);
 		((Button) findViewById(R.id.button_0)).setEnabled(true);
 
 		((Button) findViewById(R.id.bin_select)).setTextColor(Color.parseColor("#FFFFFF"));
     	((Button) findViewById(R.id.oct_select)).setTextColor(Color.parseColor("#FFFFFF"));
     	((Button) findViewById(R.id.dec_select)).setTextColor(Color.parseColor("#33B5E5"));
     	((Button) findViewById(R.id.hex_select)).setTextColor(Color.parseColor("#FFFFFF"));
     	
     	((TextView) findViewById(R.id.display_main)).setTextSize(35);
     	((TextView) findViewById(R.id.display_secondary)).setTextSize(35);
 
     	//text_display conversion
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 		
     	displayValue = Integer.toString(Long.valueOf(displayValue, currentBase).intValue());
     	operand1 = Integer.toString(Long.valueOf(operand1, currentBase).intValue());
     	operand2 = Integer.toString(Long.valueOf(operand2, currentBase).intValue());
         holdRecentEqualOperand = Integer.toString(Long.valueOf(holdRecentEqualOperand, currentBase).intValue());
     	
     	
     	display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
 	    
     	if((!operand1.equals("0")) && (!recentEqualFlag)) {
     		display_secondary.setText(operand1.toUpperCase(Locale.ENGLISH));
     	}
     	
     	
     	currentBase = 10;
 	}
 	public void hex_select(View view){
 		
 		//button locking
 		((Button) findViewById(R.id.d_button)).setEnabled(true);
 		((Button) findViewById(R.id.e_button)).setEnabled(true);
 		((Button) findViewById(R.id.f_button)).setEnabled(true);
 		((Button) findViewById(R.id.a_button)).setEnabled(true);
 		((Button) findViewById(R.id.b_button)).setEnabled(true);
 		((Button) findViewById(R.id.c_button)).setEnabled(true);
 		((Button) findViewById(R.id.button_7)).setEnabled(true);
 		((Button) findViewById(R.id.button_8)).setEnabled(true);
 		((Button) findViewById(R.id.button_9)).setEnabled(true);
 		((Button) findViewById(R.id.button_4)).setEnabled(true);
 		((Button) findViewById(R.id.button_5)).setEnabled(true);
 		((Button) findViewById(R.id.button_6)).setEnabled(true);
 		((Button) findViewById(R.id.button_1)).setEnabled(true);
 		((Button) findViewById(R.id.button_2)).setEnabled(true);
 		((Button) findViewById(R.id.button_3)).setEnabled(true);
 		((Button) findViewById(R.id.button_0)).setEnabled(true);
 
 		((Button) findViewById(R.id.bin_select)).setTextColor(Color.parseColor("#FFFFFF"));
     	((Button) findViewById(R.id.oct_select)).setTextColor(Color.parseColor("#FFFFFF"));
     	((Button) findViewById(R.id.dec_select)).setTextColor(Color.parseColor("#FFFFFF"));
     	((Button) findViewById(R.id.hex_select)).setTextColor(Color.parseColor("#33B5E5"));
     	
     	((TextView) findViewById(R.id.display_main)).setTextSize(35);
     	((TextView) findViewById(R.id.display_secondary)).setTextSize(35);
     	
     	//text_display conversion
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 		
     	displayValue = Integer.toHexString(Long.valueOf(displayValue, currentBase).intValue());
     	operand1 = Integer.toHexString(Long.valueOf(operand1, currentBase).intValue());
     	operand2 = Integer.toHexString(Long.valueOf(operand2, currentBase).intValue());
         holdRecentEqualOperand = Integer.toHexString(Long.valueOf(holdRecentEqualOperand, currentBase).intValue());
     	
     	
     	display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
     	
     	if((!operand1.equals("0")) && (!recentEqualFlag)) {
     		display_secondary.setText(operand1.toUpperCase(Locale.ENGLISH));
     	}
     	
     	currentBase = 16;
     	currentBaseStr = 8;
 	}
 	
 	//input buttons
 	public void send_0(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		String buttonValue = "0";
 		if(recentEqualFlag) { //reset values if "clr" is not pressed
 			displayValue = "0";
 			recentEqualFlag = false;
 		}
 		if(displayValue.equals("0"))
 			displayValue = buttonValue;
 		else {
 			try {
 				if(currentBase == 10) { 
 					if((Integer.valueOf(displayValue + buttonValue, currentBase) >= INT_MIN) && 
 							(Integer.valueOf(displayValue + buttonValue, currentBase) <= INT_MAX))
 						displayValue += buttonValue;
 				}
 				else if((currentBase == 2) || (currentBase == 16)) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX) && 
 							(displayValue.length() < currentBaseStr))
 						displayValue += buttonValue;
 				}
 				else if(currentBase == 8) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX)) {
 						if((displayValue.length() == currentBaseStr - 1) && (Integer.valueOf(displayValue.substring(0, 1)) <= 3))
 							displayValue += buttonValue;
 						else if(displayValue.length() < (currentBaseStr - 1))
 							displayValue += buttonValue;
 					}
 				}
 			}
 			catch(Exception e) {}
 		}
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));;
 	}
 	public void send_1(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		String buttonValue = "1";
 		if(recentEqualFlag) {
 			displayValue = "0";
 			recentEqualFlag = false;
 		}
 		if(displayValue.equals("0"))
 			displayValue = buttonValue;
 		else {
 			try {
 				if(currentBase == 10) { 
 					if((Integer.valueOf(displayValue + buttonValue, currentBase) >= INT_MIN) && 
 							(Integer.valueOf(displayValue + buttonValue, currentBase) <= INT_MAX))
 						displayValue += buttonValue;
 				}
 				else if((currentBase == 2) || (currentBase == 16)) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX) && 
 							(displayValue.length() < currentBaseStr))
 						displayValue += buttonValue;
 				}
 				else if(currentBase == 8) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX)) {
 						if((displayValue.length() == currentBaseStr - 1) && (Integer.valueOf(displayValue.substring(0, 1)) <= 3))
 							displayValue += buttonValue;
 						else if(displayValue.length() < (currentBaseStr - 1))
 							displayValue += buttonValue;
 					}
 				}
 
 			}
 			catch(Exception e) {}
 		}
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));;
 	}
 	public void send_2(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		String buttonValue = "2";
 		if(recentEqualFlag) {
 			displayValue = "0";
 			recentEqualFlag = false;
 		}
 		if(displayValue.equals("0"))
 			displayValue = buttonValue;
 		else {
 			try {
 				if(currentBase == 10) { 
 					if((Integer.valueOf(displayValue + buttonValue, currentBase) >= INT_MIN) && 
 							(Integer.valueOf(displayValue + buttonValue, currentBase) <= INT_MAX))
 						displayValue += buttonValue;
 				}
 				else if(currentBase == 16) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX) && 
 							(displayValue.length() < currentBaseStr))
 						displayValue += buttonValue;
 				}
 				else if(currentBase == 8) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX)) {
 						if((displayValue.length() == currentBaseStr - 1) && (Integer.valueOf(displayValue.substring(0, 1)) <= 3))
 							displayValue += buttonValue;
 						else if(displayValue.length() < (currentBaseStr - 1))
 							displayValue += buttonValue;
 					}
 				}
 			}
 			catch(Exception e) {}
 		}
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));;
 	}	
 	public void send_3(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		String buttonValue = "3";
 		if(recentEqualFlag) {
 			displayValue = "0";
 			recentEqualFlag = false;
 		}
 		if(displayValue.equals("0"))
 			displayValue = buttonValue;
 		else {
 			try {
 				if(currentBase == 10) { 
 					if((Integer.valueOf(displayValue + buttonValue, currentBase) >= INT_MIN) && 
 							(Integer.valueOf(displayValue + buttonValue, currentBase) <= INT_MAX))
 						displayValue += buttonValue;
 				}
 				else if(currentBase == 16) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX) && 
 							(displayValue.length() < currentBaseStr))
 						displayValue += buttonValue;
 				}
 				else if(currentBase == 8) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX)) {
 						if((displayValue.length() == currentBaseStr - 1) && (Integer.valueOf(displayValue.substring(0, 1)) <= 3))
 							displayValue += buttonValue;
 						else if(displayValue.length() < (currentBaseStr - 1))
 							displayValue += buttonValue;
 					}
 				}
 			}
 			catch(Exception e) {}
 		}
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));;
 	}	
 	
 	public void send_4(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		String buttonValue = "4";
 		if(recentEqualFlag) {
 			displayValue = "0";
 			recentEqualFlag = false;
 		}
 		if(displayValue.equals("0"))
 			displayValue = buttonValue;
 		else {
 			try {
 				if(currentBase == 10) { 
 					if((Integer.valueOf(displayValue + buttonValue, currentBase) >= INT_MIN) && 
 							(Integer.valueOf(displayValue + buttonValue, currentBase) <= INT_MAX))
 						displayValue += buttonValue;
 				}
 				else if(currentBase == 16) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX) && 
 							(displayValue.length() < currentBaseStr))
 						displayValue += buttonValue;
 				}
 				else if(currentBase == 8) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX)) {
 						if((displayValue.length() == currentBaseStr - 1) && (Integer.valueOf(displayValue.substring(0, 1)) <= 3))
 							displayValue += buttonValue;
 						else if(displayValue.length() < (currentBaseStr - 1))
 							displayValue += buttonValue;
 					}
 				}
 			}
 			catch(Exception e) {}
 		}
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));;
 	}
 	public void send_5(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		String buttonValue = "5";
 		if(recentEqualFlag) {
 			displayValue = "0";
 			recentEqualFlag = false;
 		}
 		if(displayValue.equals("0"))
 			displayValue = buttonValue;
 		else {
 			try {
 				if(currentBase == 10) { 
 					if((Integer.valueOf(displayValue + buttonValue, currentBase) >= INT_MIN) && 
 							(Integer.valueOf(displayValue + buttonValue, currentBase) <= INT_MAX))
 						displayValue += buttonValue;
 				}
 				else if(currentBase == 16) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX) && 
 							(displayValue.length() < currentBaseStr))
 						displayValue += buttonValue;
 				}
 				else if(currentBase == 8) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX)) {
 						if((displayValue.length() == currentBaseStr - 1) && (Integer.valueOf(displayValue.substring(0, 1)) <= 3))
 							displayValue += buttonValue;
 						else if(displayValue.length() < (currentBaseStr - 1))
 							displayValue += buttonValue;
 					}
 				}
 			}
 			catch(Exception e) {}
 		}
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));;
 	}
 	public void send_6(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		String buttonValue = "6";
 		if(recentEqualFlag) {
 			displayValue = "0";
 			recentEqualFlag = false;
 		}
 		if(displayValue.equals("0"))
 			displayValue = buttonValue;
 		else {
 			try {
 				if(currentBase == 10) { 
 					if((Integer.valueOf(displayValue + buttonValue, currentBase) >= INT_MIN) && 
 							(Integer.valueOf(displayValue + buttonValue, currentBase) <= INT_MAX))
 						displayValue += buttonValue;
 				}
 				else if(currentBase == 16) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX) && 
 							(displayValue.length() < currentBaseStr))
 						displayValue += buttonValue;
 				}
 				else if(currentBase == 8) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX)) {
 						if((displayValue.length() == currentBaseStr - 1) && (Integer.valueOf(displayValue.substring(0, 1)) <= 3))
 							displayValue += buttonValue;
 						else if(displayValue.length() < (currentBaseStr - 1))
 							displayValue += buttonValue;
 					}
 				}
 			}
 			catch(Exception e) {}
 		}
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));;
 	}
 
 	public void send_7(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		String buttonValue = "7";
 		if(recentEqualFlag) {
 			displayValue = "0";
 			recentEqualFlag = false;
 		}
 		if(displayValue.equals("0"))
 			displayValue = buttonValue;
 		else {
 			try {
 				if(currentBase == 10) { 
 					if((Integer.valueOf(displayValue + buttonValue, currentBase) >= INT_MIN) && 
 							(Integer.valueOf(displayValue + buttonValue, currentBase) <= INT_MAX))
 						displayValue += buttonValue;
 				}
 				else if(currentBase == 16) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX) && 
 							(displayValue.length() < currentBaseStr))
 						displayValue += buttonValue;
 				}
 				else if(currentBase == 8) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX)) {
 						if((displayValue.length() == currentBaseStr - 1) && (Integer.valueOf(displayValue.substring(0, 1)) <= 3))
 							displayValue += buttonValue;
 						else if(displayValue.length() < (currentBaseStr - 1))
 							displayValue += buttonValue;
 					}
 				}
 			}
 			catch(Exception e) {}
 		}
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));;
 	}
 	public void send_8(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		String buttonValue = "8";
 		if(recentEqualFlag) {
 			displayValue = "0";
 			recentEqualFlag = false;
 		}
 		if(displayValue.equals("0"))
 			displayValue = buttonValue;
 		else {
 			try {
 				if(currentBase == 10) { 
 					if((Integer.valueOf(displayValue + buttonValue, currentBase) >= INT_MIN) && 
 							(Integer.valueOf(displayValue + buttonValue, currentBase) <= INT_MAX))
 						displayValue += buttonValue;
 				}
 				else if(currentBase == 16) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX) && 
 							(displayValue.length() < currentBaseStr))
 						displayValue += buttonValue;
 				}
 			}
 			catch(Exception e) {}
 		}
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));;
 	}
 	public void send_9(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		String buttonValue = "9";
 		if(recentEqualFlag) {
 			displayValue = "0";
 			recentEqualFlag = false;
 		}
 		if(displayValue.equals("0"))
 			displayValue = buttonValue;
 		else {
 			try {
 				if(currentBase == 10) { 
 					if((Integer.valueOf(displayValue + buttonValue, currentBase) >= INT_MIN) && 
 							(Integer.valueOf(displayValue + buttonValue, currentBase) <= INT_MAX))
 						displayValue += buttonValue;
 				}
 				else if(currentBase == 16) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX) && 
 							(displayValue.length() < currentBaseStr))
 						displayValue += buttonValue;
 				}
 			}
 			catch(Exception e) {}
 		}
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));;
 	}	
 
 	public void send_a(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		String buttonValue = "A";
 		if(recentEqualFlag) {
 			displayValue = "0";
 			recentEqualFlag = false;
 		}
 		if(displayValue.equals("0"))
 			displayValue = buttonValue;
 		else {
 			try {
 				if(currentBase == 16) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX) && 
 							(displayValue.length() < currentBaseStr))
 						displayValue += buttonValue;
 				}
 			}
 			catch(Exception e) {}
 		}
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));;
 	}
 	public void send_b(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		String buttonValue = "B";
 		if(recentEqualFlag) {
 			displayValue = "0";
 			recentEqualFlag = false;
 		}
 		if(displayValue.equals("0"))
 			displayValue = buttonValue;
 		else {
 			try {
 				if(currentBase == 16) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX) && 
 							(displayValue.length() < currentBaseStr))
 						displayValue += buttonValue;
 				}
 			}
 			catch(Exception e) {}
 		}
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));;
 	}
 	public void send_c(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		String buttonValue = "C";
 		if(recentEqualFlag) {
 			displayValue = "0";
 			recentEqualFlag = false;
 		}
 		if(displayValue.equals("0"))
 			displayValue = buttonValue;
 		else {
 			try {
 				if(currentBase == 16) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX) && 
 							(displayValue.length() < currentBaseStr))
 						displayValue += buttonValue;
 				}
 			}
 			catch(Exception e) {}
 		}
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));;
 	}
 	
 	public void send_d(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		String buttonValue = "D";
 		if(recentEqualFlag) {
 			displayValue = "0";
 			recentEqualFlag = false;
 		}
 		if(displayValue.equals("0"))
 			displayValue = buttonValue;
 		else {
 			try {
 				if(currentBase == 16) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX) && 
 							(displayValue.length() < currentBaseStr))
 						displayValue += buttonValue;
 				}
 			}
 			catch(Exception e) {}
 		}
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));;
 	}
 	public void send_e(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		String buttonValue = "E";
 		if(recentEqualFlag) {
 			displayValue = "0";
 			recentEqualFlag = false;
 		}
 		if(displayValue.equals("0"))
 			displayValue = buttonValue;
 		else {
 			try {
 				if(currentBase == 16) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX) && 
 							(displayValue.length() < currentBaseStr))
 						displayValue += buttonValue;
 				}
 			}
 			catch(Exception e) {}
 		}
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));;
 	}
 	public void send_f(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		String buttonValue = "F";
 		if(recentEqualFlag) {
 			displayValue = "0";
 			recentEqualFlag = false;
 		}
 		if(displayValue.equals("0"))
 			displayValue = buttonValue;
 		else {
 			try {
 				if(currentBase == 16) {
 					if((Long.valueOf(displayValue + buttonValue, currentBase).intValue() >= INT_MIN) && 
 							(Long.valueOf(displayValue + buttonValue, currentBase).intValue() <= INT_MAX) && 
 							(displayValue.length() < currentBaseStr))
 						displayValue += buttonValue;
 				}
 			}
 			catch(Exception e) {}
 		}
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
 	}
 
 	//control buttons
 	public void send_delete(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		if(displayValue.length() > 0) {
 			displayValue = displayValue.substring(0, displayValue.length() - 1);
 			if(displayValue.length() == 0){
 				displayValue = "0";
 			}
 		}
 		operand1 = displayValue;
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
 	}
 	public void send_clear(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_operation = (TextView) findViewById(R.id.display_operation);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 		//button unlocking
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
 		Button plusminus_button = (Button) findViewById(R.id.plusminus_button);
 		Button equal_button = (Button) findViewById(R.id.equal_button);
 		Button mod_button = (Button) findViewById(R.id.mod_button);
 		Button div_button = (Button) findViewById(R.id.div_button);
 		Button mult_button = (Button) findViewById(R.id.mult_button);
 		Button sub_button = (Button) findViewById(R.id.sub_button);
 		Button add_button = (Button) findViewById(R.id.add_button);
 		Button bin_select = (Button) findViewById(R.id.bin_select);
 		Button oct_select = (Button) findViewById(R.id.oct_select);
 		Button dec_select = (Button) findViewById(R.id.dec_select);
 		Button hex_select = (Button) findViewById(R.id.hex_select);
 		Button delete_button = (Button) findViewById(R.id.delete_button);
 		plusminus_button.setEnabled(true);
     	add_button.setEnabled(true);
     	sub_button.setEnabled(true);
     	mult_button.setEnabled(true);
     	div_button.setEnabled(true);
     	mod_button.setEnabled(true);
     	equal_button.setEnabled(true);
     	bin_select.setEnabled(true);
     	oct_select.setEnabled(true);
     	dec_select.setEnabled(true);
     	hex_select.setEnabled(true);
     	button_1.setEnabled(true);
     	button_0.setEnabled(true);
     	delete_button.setEnabled(true);
     	
     	switch(currentBase) {
     		case 8: 
     	    	button_7.setEnabled(true);
     	    	button_6.setEnabled(true);
     	    	button_5.setEnabled(true);
     	    	button_4.setEnabled(true);
     	    	button_3.setEnabled(true);
     	    	button_2.setEnabled(true);
     	    	break;
     		case 10:
     	    	button_9.setEnabled(true);
     	    	button_8.setEnabled(true);
     	    	button_7.setEnabled(true);
     	    	button_6.setEnabled(true);
     	    	button_5.setEnabled(true);
     	    	button_4.setEnabled(true);
     	    	button_3.setEnabled(true);
     	    	button_2.setEnabled(true);
     	    	break;
     		case 16:
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
     	}
 
 		displayValue = display_main.getText().toString();
         holdRecentEqualOperand = "0";
 		recentEqualFlag = false;
 		displayValue = "0";
 		operand1 = "0";
 		operand2 = "0";
         operation = "";
 		display_operation.setText("");
 		display_main.setText(displayValue);
 		display_secondary.setText("");
 		
 	}
 	public void send_plusminus(View view) {
 		//conversion from hex -> decimal gives inverse values if +/- is pushed on a negative hex value
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		long holdInverse = 0;
 		if((Long.valueOf(displayValue, currentBase).intValue() > INT_MIN) && (Long.valueOf(displayValue, currentBase).intValue() <= INT_MAX)) {
 			holdInverse = ~Long.valueOf(displayValue, currentBase).intValue() + 1;
 		}
 		else if(Integer.valueOf(displayValue, currentBase) == 0) {
 			holdInverse = 0;
 		}
 		switch (currentBase) {
 			case 2:
 				displayValue = Integer.toBinaryString(Long.valueOf(holdInverse).intValue());
 				break;
 			case 8:
 				displayValue = Integer.toOctalString(Long.valueOf(holdInverse).intValue());
 				break;
 			case 10:
 				displayValue = Integer.toString(Long.valueOf(holdInverse).intValue());
 				break;
 			case 16:
 				displayValue = Integer.toHexString(Long.valueOf(holdInverse).intValue());
 				break;
 	}
 		display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
 	}
 	public void send_equal(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 		TextView display_operation = (TextView) findViewById(R.id.display_operation);
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
 		Button plusminus_button = (Button) findViewById(R.id.plusminus_button);
 		Button equal_button = (Button) findViewById(R.id.equal_button);
 		Button mod_button = (Button) findViewById(R.id.mod_button);
 		Button div_button = (Button) findViewById(R.id.div_button);
 		Button mult_button = (Button) findViewById(R.id.mult_button);
 		Button sub_button = (Button) findViewById(R.id.sub_button);
 		Button add_button = (Button) findViewById(R.id.add_button);
 		Button bin_select = (Button) findViewById(R.id.bin_select);
 		Button oct_select = (Button) findViewById(R.id.oct_select);
 		Button dec_select = (Button) findViewById(R.id.dec_select);
 		Button hex_select = (Button) findViewById(R.id.hex_select);
 		Button delete_button = (Button) findViewById(R.id.delete_button);
 
         if(operation != "")
 		    operand2 = displayValue;
         if(recentEqualFlag){
             operand2 =  holdRecentEqualOperand;
         }
 
 
 		long solutionInt = 0;
 		
 		if(operation == "+") {
 			solutionInt = Long.valueOf(operand1, currentBase).intValue() + Long.valueOf(operand2, currentBase).intValue();
 		}
 		else if(operation == "-") {
 			solutionInt = Long.valueOf(operand1, currentBase).intValue() - Long.valueOf(operand2, currentBase).intValue();
 		}
 		else if(operation == "*") {
 			solutionInt = Long.valueOf(operand1, currentBase).intValue() * Long.valueOf(operand2, currentBase).intValue();
 		}
 		else if(operation == "/") {
			if(operand2 == "0") {
 				display_secondary.setText("ERROR:");
 				display_main.setText("DIV BY ZERO");
 				display_operation.setText(null);
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
 		    	button_1.setEnabled(false);
 		    	button_0.setEnabled(false);
 		    	plusminus_button.setEnabled(false);
 		    	add_button.setEnabled(false);
 		    	sub_button.setEnabled(false);
 		    	mult_button.setEnabled(false);
 		    	div_button.setEnabled(false);
 		    	mod_button.setEnabled(false);
 		    	equal_button.setEnabled(false);
 		    	bin_select.setEnabled(false);
 		    	oct_select.setEnabled(false);
 		    	dec_select.setEnabled(false);
 		    	hex_select.setEnabled(false);
 		    	delete_button.setEnabled(false);
 		    	divZeroFlag = true;
 			}
 			else
 				solutionInt = Long.valueOf(operand1, currentBase).intValue() / Long.valueOf(operand2, currentBase).intValue();
 		}
 		else if(operation == "%") {
 			if(operand2 == "0") {
 				display_secondary.setText("ERROR:");
 				display_main.setText("MOD BY ZERO");
 				display_operation.setText(null);
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
 		    	button_1.setEnabled(false);
 		    	button_0.setEnabled(false);
 		    	plusminus_button.setEnabled(false);
 		    	add_button.setEnabled(false);
 		    	sub_button.setEnabled(false);
 		    	mult_button.setEnabled(false);
 		    	div_button.setEnabled(false);
 		    	mod_button.setEnabled(false);
 		    	equal_button.setEnabled(false);
 		    	bin_select.setEnabled(false);
 		    	oct_select.setEnabled(false);
 		    	dec_select.setEnabled(false);
 		    	hex_select.setEnabled(false);
 		    	delete_button.setEnabled(false);
 		    	divZeroFlag = true;
 			}
 			else
 				solutionInt = Long.valueOf(operand1, currentBase).intValue() % Long.valueOf(operand2, currentBase).intValue();
 		}
 
         if (operation != ""){
             switch (currentBase) {
                 case 2:
                     displayValue = Integer.toBinaryString(Long.valueOf(solutionInt).intValue());
                     break;
                 case 8:
                     displayValue = Integer.toOctalString(Long.valueOf(solutionInt).intValue());
                     break;
                 case 10:
                     displayValue = Integer.toString(Long.valueOf(solutionInt).intValue());
                     break;
                 case 16:
                     displayValue = Integer.toHexString(Long.valueOf(solutionInt).intValue());
                     break;
             }
 
             if(!divZeroFlag) {
                 holdRecentEqualOperand = operand2;
                 operand1 = displayValue;
                 display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
                 display_secondary.setText("");
                 display_operation.setText("");
             }
             recentEqualFlag = true;
             divZeroFlag = false;
         }
 	}
 	
 	//operations
 	public void send_add(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_operation = (TextView) findViewById(R.id.display_operation);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);		
 		long solutionInt = 0;
 		operation = "+";
 		if(!operand1.equals("0")) {
 			operand2 = displayValue;
 		}
 		else {
 			operand1 = displayValue;
 		}
 		display_secondary.setText(operand1.toUpperCase(Locale.ENGLISH));
 		if((!operand1.equals("0")) && (!operand2.equals("0")) && !recentEqualFlag) {
 			switch (previousOperation) {
 				case 1:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() + Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 2:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() - Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 3:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() * Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 4:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() / Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 5:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() % Long.valueOf(operand2, currentBase).intValue();
 					break;
 			}
 			switch (currentBase) {
 				case 2:
 					displayValue = Integer.toBinaryString(Long.valueOf(solutionInt).intValue());
 					break;
 				case 8:
 					displayValue = Integer.toOctalString(Long.valueOf(solutionInt).intValue());
 					break;
 				case 10:
 					displayValue = Integer.toString(Long.valueOf(solutionInt).intValue());
 					break;
 				case 16:
 					displayValue = Integer.toHexString(Long.valueOf(solutionInt).intValue());
 					break;
 			}
 			display_secondary.setText(displayValue.toUpperCase(Locale.ENGLISH));
 			operand1 = displayValue;
 			operand2 = "0";
 			displayValue = "0";
 			display_main.setText(displayValue);
 			display_operation.setText(operation);
 		}
 		else {
 			displayValue = "0";
 			display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
 			display_operation.setText(operation);
 			recentEqualFlag = false;
 		}
 		previousOperation = 1;
 	}
 	public void send_sub(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_operation = (TextView) findViewById(R.id.display_operation);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 		int solutionInt = 0;
 		operation = "-";
 		if(!operand1.equals("0")) {
 			operand2 = display_main.getText().toString();
 		}
 		else {
 			operand1 = display_main.getText().toString();
 		}
 		display_secondary.setText(operand1.toUpperCase(Locale.ENGLISH));
 		if((!operand1.equals("0")) && (!operand2.equals("0")) && !recentEqualFlag) {
 			switch (previousOperation) {
 				case 1:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() + Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 2:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() - Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 3:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() * Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 4:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() / Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 5:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() % Long.valueOf(operand2, currentBase).intValue();
 					break;
 			}
 			switch (currentBase) {
 				case 2:
 					displayValue = Integer.toBinaryString(Long.valueOf(solutionInt).intValue());
 					break;
 				case 8:
 					displayValue = Integer.toOctalString(Long.valueOf(solutionInt).intValue());
 					break;
 				case 10:
 					displayValue = Integer.toString(Long.valueOf(solutionInt).intValue());
 					break;
 				case 16:
 					displayValue = Integer.toHexString(Long.valueOf(solutionInt).intValue());
 					break;
 			}
 			display_secondary.setText(displayValue.toUpperCase(Locale.ENGLISH));
 			operand1 = displayValue;
 			operand2 = "0";
 			displayValue = "0";
 			display_main.setText(displayValue);
 			display_operation.setText(operation);
 		}
 		else {
 			displayValue = "0";
 			display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
 			display_operation.setText(operation);
 			recentEqualFlag = false;
 		}
 		previousOperation = 2;
 	}
 	public void send_mult(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_operation = (TextView) findViewById(R.id.display_operation);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 		int solutionInt = 0;
 		operation = "*";
 		
 		if(!operand1.equals("0")) {
 			operand2 = displayValue;
 		}
 		else {
 			operand1 = displayValue;
 		}
 		display_secondary.setText(operand1.toUpperCase(Locale.ENGLISH));
 		if((!operand1.equals("0")) && (!operand2.equals("0")) && !recentEqualFlag) {
 			switch (previousOperation) {
 				case 1:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() + Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 2:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() - Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 3:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() * Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 4:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() / Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 5:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() % Long.valueOf(operand2, currentBase).intValue();
 					break;
 			}
 			switch (currentBase) {
 				case 2:
 					displayValue = Integer.toBinaryString(Long.valueOf(solutionInt).intValue());
 					break;
 				case 8:
 					displayValue = Integer.toOctalString(Long.valueOf(solutionInt).intValue());
 					break;
 				case 10:
 					displayValue = Integer.toString(Long.valueOf(solutionInt).intValue());
 					break;
 				case 16:
 					displayValue = Integer.toHexString(Long.valueOf(solutionInt).intValue());
 					break;
 			}
 			display_secondary.setText(displayValue.toUpperCase(Locale.ENGLISH));
 			operand1 = displayValue;
 			operand2 = "0";
 			displayValue = "0";
 			display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
 			display_operation.setText(operation);
 		}
 		else {
 			displayValue = "0";
 			display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
 			display_operation.setText(operation);
 			recentEqualFlag = false;
 		}
 		previousOperation = 3;
 	}
 	public void send_div(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_operation = (TextView) findViewById(R.id.display_operation);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 		int solutionInt = 0;
 		operation = "/";
 		
 		if(!operand1.equals("0")) {
 			operand2 = displayValue;
 		}
 		else {
 			operand1 = displayValue;
 		}
 		display_secondary.setText(operand1.toUpperCase(Locale.ENGLISH));
 		if((!operand1.equals("0")) && (!operand2.equals("0")) && !recentEqualFlag) {
 			switch (previousOperation) {
 				case 1:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() + Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 2:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() - Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 3:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() * Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 4:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() / Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 5:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() % Long.valueOf(operand2, currentBase).intValue();
 					break;
 			}
 			switch (currentBase) {
 				case 2:
 					displayValue = Integer.toBinaryString(Long.valueOf(solutionInt).intValue());
 					break;
 				case 8:
 					displayValue = Integer.toOctalString(Long.valueOf(solutionInt).intValue());
 					break;
 				case 10:
 					displayValue = Integer.toString(Long.valueOf(solutionInt).intValue());
 					break;
 				case 16:
 					displayValue = Integer.toHexString(Long.valueOf(solutionInt).intValue());
 					break;
 			}
 			display_secondary.setText(displayValue.toUpperCase(Locale.ENGLISH));
 			operand1 = displayValue;
 			operand2 = "0";
 			displayValue = "0";
 			display_main.setText(displayValue);
 			display_operation.setText(operation);
 		}
 		else {
 			displayValue = "0";
 			display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
 			display_operation.setText(operation);
 			recentEqualFlag = false;
 		}
 		previousOperation = 4;
 	}
 	public void send_mod(View view) {
 		TextView display_main = (TextView) findViewById(R.id.display_main);
 		TextView display_operation = (TextView) findViewById(R.id.display_operation);
 		TextView display_secondary = (TextView) findViewById(R.id.display_secondary);
 		int solutionInt = 0;
 		operation = "%";
 		
 		if(!operand1.equals("0")) {
 			operand2 = displayValue;
 		}
 		else {
 			operand1 = displayValue;
 		}
 		display_secondary.setText(operand1.toUpperCase(Locale.ENGLISH));
 		if((!operand1.equals("0")) && (!operand2.equals("0")) && !recentEqualFlag) {
 			switch (previousOperation) {
 				case 1:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() + Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 2:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() - Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 3:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() * Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 4:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() / Long.valueOf(operand2, currentBase).intValue();
 					break;
 				case 5:
 					solutionInt = Long.valueOf(operand1, currentBase).intValue() % Long.valueOf(operand2, currentBase).intValue();
 					break;
 			}
 			switch (currentBase) {
 				case 2:
 					displayValue = Integer.toBinaryString(Long.valueOf(solutionInt).intValue());
 					break;
 				case 8:
 					displayValue = Integer.toOctalString(Long.valueOf(solutionInt).intValue());
 					break;
 				case 10:
 					displayValue = Integer.toString(Long.valueOf(solutionInt).intValue());
 					break;
 				case 16:
 					displayValue = Integer.toHexString(Long.valueOf(solutionInt).intValue());
 					break;
 			}
 			display_secondary.setText(displayValue.toUpperCase(Locale.ENGLISH));
 			operand1 = displayValue;
 			operand2 = "0";
 			displayValue = "0";
 			display_main.setText(displayValue.toUpperCase(Locale.ENGLISH));
 			display_operation.setText(operation);
 		}
 		else {
 			displayValue = "0";
 			display_main.setText(displayValue);
 			display_operation.setText(operation);
 			recentEqualFlag = false;
 		}
 		previousOperation = 5;
 	}
 	
 }
 /****************************************************************
 New features list:
 
  Swipe to clear!
  More color options
  More operations
 
 
 Bug:
 
  division by zero does not work when switching bases
 ****************************************************************/
