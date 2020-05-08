 package com.simple.calculator;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	/**
 	 * Project Simple Calculator : Main Activity
 	 * This class is main activity class for Simple Calculator project
 	 * In this class is portrait mode for calculator interface found in activity_main.xml
 	 * This is only interface class and all calculations are done in different class
 	 * Class tries to be smart about what inputs are valid and what are not and that way prevent user errors
 	 */
 	public ArrayList<String> calculate = new ArrayList<String>();		//This ArrayList holds calculation
 	public String buffer = null;										//This String is buffer for adding numbers to the calculate Sting ArrayList
 	public String ans = "0";											//This Sting holds last answer that is calculated and it has default value of 0
 	/*
 	 * Here is interface TextView
 	 */
 	TextView screen;
 	
 	/*
 	 * Hear is few static variables for some important chars
 	 */
 	public static String POTENS = "²";
 	public static String SQROOT = "√";
 	public static String OBRACKET = "(";
 	public static String CBRACKET = ")";
 	public static String DIVISION = "÷";
 	public static String MULTIPLY = "x";
 	public static String PLUS = "+";
 	public static String MINUS = "-";
 	
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		screen = (TextView) findViewById(R.id.view);
 		
 	}
 	public void updScreen(){
 		/**
 		 * updScreen() is method for updating TextView called screen for giving user feedback
 		 * screen shows calculation that is entered 
 		 */
 		if (this.calculate.size() == 0){ 
 			// Set number 0 for screen if no calculation has been given
 			this.screen.setText("0");
 			return;
 		}
 		// Idea is show user everything that has been set for ArrayList calculate by getting all Strings and adding them into one and setting that string text for TextView screen
 		String tmp = "";
 		for (String s : this.calculate) tmp = tmp + s;
 		this.screen.setText(tmp);
 	}
 	public void don(View v){
 		/**
 		 * don() is method used as button listener for number buttons
 		 */
 		if (this.buffer == null){ 
 			if (calculate.size() == 0){
 				// if calculate size is 0 and ans is pushed then ans value is set to buffer and calculate
 				if ("ans".equals((String) v.getTag())){
 					buffer = ans;
 					calculate.add(buffer);
 				}
 				// if calculate size is 0 and number button is pushed then that number is set to buffer and calculate
 				else{
 					buffer = (String) v.getTag();
 					calculate.add(this.buffer);
 				}
 			}
 			// if calculate size is one or more and last symbol is potens it is replaced by number
 			else if (calculate.get(this.calculate.size()-1).equals(POTENS) && calculate.size() != 0){
 				calculate.remove(calculate.size()-1);
 				buffer = calculate.get(calculate.size()-1);
 				buffer  = buffer + ( (String) v.getTag());
 				calculate.set(calculate.size()-1, buffer);
 			}
 			// if calculate size is one or more and last symbol is closing bracket nothing will be done
 			else if (calculate.get(this.calculate.size()-1).equals(CBRACKET)) return;
 			// if calculate size is one or more and last symbol isn't potens or closing bracket then number of tag is added to calculator
 			else {
 				if ("ans".equals((String) v.getTag())){
 					buffer = ans;
 					calculate.add(buffer);
 				}
 				else {
 					this.buffer = (String) v.getTag();
 					this.calculate.add(this.buffer);
 			
 				}
 			}
 		}
 		else {
 			// if point or ans is given then nothing will be done
 			if ( ((String) v.getTag()).equals(".") &&  buffer.contains(".")) return;
 			if ( ((String) v.getTag()).equals("ans")) return;
 			// In other case number is add to buffer and calulate is updated
 			this.buffer  = this.buffer + ( (String) v.getTag() );
 			this.calculate.set(this.calculate.size()-1, this.buffer);
 		}
 		this.updScreen();
 	}
 	public void doact(View v){
 		/**
 		 * doact() is used button listener for actions/symbol (like +, - or x) buttons like
 		 */
 		// symbol is get from component tag witch is found from View
 		if (calculate.size() == 0){
 			// if calculate size is 0 then ans is added to calculate and after that symbol is add to calculate
 			calculate.add(ans);
 			this.calculate.add((String) v.getTag());
 		}
 		else if (this.buffer != null){
 			// if buffer isn't empty symbol is added to calculate and buffer is emptied
 			this.calculate.add((String) v.getTag());
 			buffer = null;
 			this.updScreen();
 			return;
 		}
 		else {
 			String tmp = this.calculate.get(this.calculate.size()-1);
 			// if buffer is empty and if last symbol in calculate is potens or closing bracket then symbol is added to calculate
 			if (tmp.equals(POTENS) || tmp.equals(CBRACKET)){
 				calculate.add((String) v.getTag());
 			}
 			// if buffer is empty and last symbol is square root nothing will be done
 			else if (tmp.equals(SQROOT)) return;
 			// if buffer is empty and last symbol isn't potens, square root or closing bracket then symbol is added to calculate in way that it replaces last symbol
 			else {
 				this.calculate.set(calculate.size()-1, (String) v.getTag());
 			}
 		}
 		this.updScreen();
 	}
 	public void clear(View v){
 		/**
 		 * clear() is button listener method for clear button and it clear buffer and calculate ArrayList
 		 */
 		this.calculate = new ArrayList<String>();
 		this.buffer = null;
 		this.updScreen();
 	}
 	public void erase(View v){
 		/**
 		 * erase() is button listener method for erasing one char or number from TextView screen
 		 */
 		// if calculate size is 0 then nothing will be done
 		if (calculate.size() == 0) return;
  		if (buffer != null){
  			// If buffer isn't empty and buffer is longer than 1 char
  			// Then last char from buffer is removed and change is updated to calculate
  			if (buffer.length() != 1){
  				buffer = buffer.substring(0, buffer.length()-1);
  				calculate.set(calculate.size()-1, buffer);
  			}
  			// In other case (buffer isn't empty and buffer has only 1 char) buffer is emptied and last string (number) is removed from calculate
  			else {
  				calculate.remove(calculate.size()-1);
  				buffer = null;
  			}
 		}
 		else {
 			String tmp = this.calculate.get(this.calculate.size()-1);
 			// if buffer is empty and last symbol is square root then square root is removed
 			if (tmp.equals(SQROOT)){
 				calculate.remove(calculate.size()-1);
 			}
 			// if buffer is empty and last symbol is opening bracket then opening bracket is removed
 			else if (tmp.equals(OBRACKET)){
 				calculate.remove(calculate.size()-1);
 			}
 			// In other case last symbol is removed and if next to last string is number string then it will be set to buffer
 			else {
 				calculate.remove(calculate.size()-1);
 				tmp = this.calculate.get(this.calculate.size()-1);
 				if (tmp.equals(POTENS)) ;
 				else if (tmp.equals(CBRACKET)) ;
 				else buffer = tmp;
 			}
 		}
 		this.updScreen();
 	}
 	public void calc(View v){
 		/**
 		 * calc() is button listener for "=" symbol and does the calculating. calc() calls Calculate.java with does calculating in this application
 		 */
 		//if calculate size is 1 then nothing will be done
 		if (this.calculate.size() == 0) return;	
 		String tmp = this.calculate.get(this.calculate.size()-1);
 		//if last symbol in calculate is of the following [ +, -, x, ÷, √, ( ] then last symbol will be removed from calculate because it would cause error
 		if (tmp.equals(SQROOT) || tmp.equals(MULTIPLY) || tmp.equals(MINUS) || tmp.equals(PLUS) || tmp.equals(DIVISION) || tmp.equals(OBRACKET)){
 			// if only symbol in calculate is "(" then calculate will be initialized and nothing else will be done
 			if (this.calculate.size() == 1 && tmp.equals(OBRACKET)){
 				this.calculate = new ArrayList<String>();
 				this.updScreen();
 				return;
 			}
 			else if (tmp.equals(OBRACKET)){
 				// if last symbol is "(" and calculate is longer than 1 then last two symbol are removed from calculate
 				this.calculate.remove(this.calculate.size()-1);
 				this.calculate.remove(this.calculate.size()-1);
 			}
 			else{
 				// in other cases last symbol will be removed
 				this.calculate.remove(this.calculate.size()-1);
 			}
 		}
 		int open = 0;
 		for (int i = 0; i < this.calculate.size(); i++){
 			// This for loop has two purposes:
 			// 1. count how many open brackets are in calculate
 			// 2. change "x" symbols to "*" symbols
 			if (this.calculate.get(i).equals(OBRACKET)) open++;
 			else if (this.calculate.get(i).equals(CBRACKET)) open--;
 			else if (this.calculate.get(i).equals(MULTIPLY)) this.calculate.set(i, "*");
 		}
 		while (open > 0){
 			// This while loop will close all open brackets
 			this.calculate.add(CBRACKET);
 			open--;
 		}
 		this.updScreen();
 		try {
 			// Try Catch is used to ensure that if some illegal calculate is give for Calculate.java then application don't crash and gives user error message
 			// First in this try calculate we call Calculate.java and give calculate for it
 			new Calculate(this.calculate);
 			// Then answer from calculation is saved to ans
 			this.ans = Calculate.getResult();
 			// Then ans will be simplified if possible by using double and integer variables 
 			double test = Double.parseDouble(this.ans);
 			if (test%1==0){
				int tt = (int) test;
				this.ans = Integer.toString(tt);
 			}
 			// Last ans will be set for screen
 			String lastText = (String) this.screen.getText();
 			this.screen.setText(lastText + "=\n"+this.ans);
 		}
 		catch(java.lang.Exception e) {
 			// if there is error or exception in try bloc and error message will be given for user
 			this.screen.setText("ERROR");
 			//System.out.print(e.toString());
 			this.ans = "0";
 		}
 		// Buffer is emptied and if calculate is initialize
 		this.calculate = new ArrayList<String>();
 		this.buffer = null;
 	}
 	public void brac(View v){
 		/**
 		 * brac() is button listener method for brackets button and tries to be smart for adding brackets
 		 */
 		//if calculate size is 0 then "(" will be added 
 		if (calculate.size() == 0){					
 			calculate.add(OBRACKET);
 		}
 		else {
 			int open = 0;							//if calculate size is not 0 then we count "("	and ")" in calculate
 			int close = 0;
 			for (String st: calculate){				//bracket count is done with for loop
 				if (st.equals(OBRACKET)) open ++;
 				else if (st.equals(CBRACKET)) close++;
 			}
 			String tmp = calculate.get(calculate.size()-1);
 			if (buffer == null && tmp.compareTo(POTENS) != 0){							//if buffer is empty and last symbol is not potens symbol then:
 				if (close < open && tmp.equals(CBRACKET)) calculate.add(CBRACKET);		//	-if there are open brackets and last symbol is closing bracket then closing bracket will be added 
 				else if (close == open && tmp.equals(CBRACKET)) return;					//	-if there are no open brackets and last symbol is closing bracket then nothing will be done
 				else calculate.add(OBRACKET);											//	-in all other cases we will add opening bracket
 			}
 			else if (tmp.equals(POTENS) && close < open) calculate.add(CBRACKET);
 			else if (buffer != null && close < open){									//if buffer isn't empty and there are open brackets then buffer will be emptied and closing bracket 
 				buffer = null;
 				calculate.add(CBRACKET);
 			}
 		}
 		this.updScreen();
 	}
 	public void tosecond(View v){
 		/**
 		 * tosecond() is button listener method for potency button
 		 */
 		if (this.buffer == null ){											//if buffer is empty and if last symbol is closing bracket then potens will be added
 			if (calculate.size() == 0) return;
 			if (calculate.get(calculate.size()-1).equals(CBRACKET)){
 				calculate.add(POTENS);
 			}
 			else return;
 		}
 		else {																//if buffer isn't empty then buffer is emptied and potens symbol will be added
 			buffer = null;
 			calculate.add(POTENS);
 		}
 		this.updScreen();
 	}
 	public void squeroot(View v){
 		/**
 		 * squeroot() is button listener for square root button
 		 */
 		if (this.buffer != null) return;									//if buffer isn't null then nothing will be done
 		if (calculate.size() != 0){										
 			if (calculate.get(calculate.size()-1).equals(POTENS)) return;
 			else if (calculate.get(calculate.size()-1).equals(SQROOT)){
 				calculate.add(OBRACKET);
 				calculate.add(SQROOT);
 			}
 			else calculate.add(SQROOT);
 		}
 		else calculate.add(SQROOT);												//if last symbol is not potens then square root will be added
 		this.updScreen();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
