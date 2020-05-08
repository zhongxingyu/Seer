 /**
  * Copyright (C) 2012 by Luis Ángel Fernández Fernández <la@zzsoft.es>
  * 
  * This file is part of MathGame.
  * 
  * MathGame is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * MathGame is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.ww.gnu.org/licenses/>.
  */
 
 package zzsoft.la.mathgame;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Random;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.PorterDuff.Mode;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 
 /**
  * Main activity.
  * @author Luis Ángel Fernández Fernández
  */
 public class MainActivity extends Activity {
 
 	private ArrayList<ArrayList<Integer>> m_randomDigits;
 	private ArrayList<ArrayList<Character>> m_randomOperators;
 	private ArrayList<Integer> m_results;
 	private ArrayList<Integer> m_resultButtonsIDs;
 	private Button m_buttonClicked;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         // I'll initialize the board.
         initializeBoard();
         checkResults(null);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     @Override
     protected void onDestroy() {
     	clearMemberLists();
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	if (item.getItemId() == R.id.menu_new) {
     		clearMemberLists();
     		initializeBoard();
     		checkResults(null);
     	}
     	if (item.getItemId() == R.id.menu_solve) {
     		fillBoardWithSolution();
     	}
     	return super.onOptionsItemSelected(item);
     }
     
     /**
     * 
      */
     private void fillBoardWithSolution() {
       TableLayout tl = (TableLayout)findViewById(R.id.MainLayout);
       int row = 0, column = 0;
       for (int i=0 ; i<tl.getChildCount() ; i++) {
       	LinearLayout ll = (LinearLayout)tl.getChildAt(i);
       	for (int j=0 ; j<ll.getChildCount() ; j++) {
       		Button button = (Button)ll.getChildAt(j);
       		//Resources res = getResources();
       		if (button.isClickable()) {
       			Integer number = m_randomDigits.get(row).get(column);
       			button.setText(number.toString());
       			if (column == m_randomDigits.get(row).size() - 1) {
       				row += 2;
       				column = 0;
       			}
       			else {
       				++column;
       			}
       		}
       	} // for j
       } // for i
     	// This will set all result buttons background to ok_color
     	checkResults(m_results);
     }
     
     /**
      * This method will return one of the operators in the array array_operators.
      * @see array_operators
      * @return a Character with the random sign from array_operators.
      */
     private String getAnOperator() {    	  	
     	Resources res = getResources();
     	Random randomGenerator = getRandomizeGenerator();
     	
     	String[] strArray = res.getStringArray(R.array.array_operators);
     	int randomNumber = randomGenerator.nextInt(strArray.length);
     	
     	return strArray[randomNumber];
     }
     
     /**
      * This method will return a random Integer between 0 and 9.
      * @return an Integer between 0 and 9.
      */
     private Integer getADigit() {
     	Random randomGenerator = getRandomizeGenerator();
     	
     	return randomGenerator.nextInt(10);
     }
     
     /**
      * This method will initialize the board.
      */
     public void initializeBoard() {
       m_buttonClicked = null;
       m_resultButtonsIDs = new ArrayList<Integer>();
       m_resultButtonsIDs.add(R.id.Button_result_row1);
       m_resultButtonsIDs.add(R.id.Button_result_row2);
       m_resultButtonsIDs.add(R.id.Button_result_row3);
       m_resultButtonsIDs.add(R.id.Button_result_column1);
       m_resultButtonsIDs.add(R.id.Button_result_column2);
       m_resultButtonsIDs.add(R.id.Button_result_column3);
     	
       TableLayout tl = (TableLayout)findViewById(R.id.MainLayout);
       ArrayList<ArrayList<Character>> listOfOperators= new ArrayList<ArrayList<Character>>();
       ArrayList<ArrayList<Integer>> listOfDigits = new ArrayList<ArrayList<Integer>>();
       for (int i=0 ; i<tl.getChildCount() ; i++) {
       	LinearLayout ll = (LinearLayout)tl.getChildAt(i);
     		ArrayList<Character> rowOfOperators = new ArrayList<Character>();
     		ArrayList<Integer> rowOfDigits = new ArrayList<Integer>();
       	for (int j=0 ; j<ll.getChildCount() ; j++) {
       		Button button = (Button)ll.getChildAt(j);
       		//Resources res = getResources();
       		if (!button.isClickable()) {
       			// The button contains a sign label
       			CharSequence charSeq = button.getText();
       			String str = charSeq.toString();
       			if (str != null && str.length() > 0 && isAnOperator(str.charAt(0))) {
       				String strAux = getAnOperator();
       				rowOfOperators.add(strAux.charAt(0));
       				button.setText(strAux);
       			}
       		}
       		else { // isClickable()
       			// The button will contain a number, so let's generate the number
       			// that will be part of the solution (at least one of them if there
       			// is more than one).
       			rowOfDigits.add(getADigit());
       			button.setText("");
       		}
       	} // for j
       	listOfOperators.add(rowOfOperators);
       	listOfDigits.add(rowOfDigits);
       } // for i
       
       // Here I'll have a list with the digits
       // and a list with the signs I'll need to
       // do the math.
       
       // Let's fill the result cells.
       fillResultCells(listOfDigits, listOfOperators);
       if (m_randomDigits == null) m_randomDigits = new ArrayList<ArrayList<Integer>>();
       m_randomDigits.addAll(listOfDigits);
       if (m_randomOperators == null) m_randomOperators = new ArrayList<ArrayList<Character>>();
       m_randomOperators.addAll(listOfOperators);
     }
     
     /**
     * 
     * @return
      */
     public ArrayList<ArrayList<Integer>> getNumbersFromBoard() {
       TableLayout tl = (TableLayout)findViewById(R.id.MainLayout);
       ArrayList<ArrayList<Integer>> listOfDigits = new ArrayList<ArrayList<Integer>>();
       for (int i=0 ; i<tl.getChildCount() ; i++) {
       	LinearLayout ll = (LinearLayout)tl.getChildAt(i);
     		ArrayList<Integer> rowOfDigits = new ArrayList<Integer>();
       	for (int j=0 ; j<ll.getChildCount() ; j++) {
       		Button button = (Button)ll.getChildAt(j);
       		//Resources res = getResources();
       		if (button.isClickable()) {
       			CharSequence buttonText = button.getText();
       			if ((buttonText != null) && (buttonText.length() > 0)) {
       				String buttonTextAsString = buttonText.toString();
       				rowOfDigits.add(Integer.parseInt(buttonTextAsString));
       			}
       			else {
       				rowOfDigits.add(Integer.valueOf(0));
       			}
       		}
       	} // for j
       	listOfDigits.add(rowOfDigits);
       } // for i
 
       return listOfDigits;
     }
     
     /**
      * This method will return a initialized random number generator.
      * @return A random number generator.
      */
     private Random getRandomizeGenerator() {
     	String pattern = new String("S"); // milliseconds
     	SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
     	int milliseconds = Integer.parseInt(dateFormat.format(new Date()));
     	Random randomGenerator = new Random(milliseconds*milliseconds);
 
     	return randomGenerator;
     }
     
     /**
      * This method will fill the buttons that will contain the results with the 
      * results values calculated.
      * @param digits the digits that will be part of the calculus.
      * @param operators the signs/operators used to do the math.
      */
     private void fillResultCells(ArrayList<ArrayList<Integer>> digits, 
     		ArrayList<ArrayList<Character>> operators) {
     	
     	m_results = doTheMath(digits, operators);
     	
     	for (int i=0 ; i<m_resultButtonsIDs.size() ; i++) {
       	Button resultButton = (Button)findViewById(m_resultButtonsIDs.get(i));
       	resultButton.setText(m_results.get(i).toString());    		
     	}
     }
     
     /**
      * A method to clear member lists.
      */
     private void clearMemberLists() {    
     	if (m_randomDigits != null) {
     		Iterator<ArrayList<Integer>> itr = m_randomDigits.iterator();
     		while (itr.hasNext()) {
     			Iterator<ArrayList<Integer>> itrAux = itr;
     	    itr.next();
     	    itrAux.remove();
     		}
     		m_randomDigits = null;
     	}
     	if (m_randomOperators != null) {
     		Iterator<ArrayList<Character>> itr = m_randomOperators.iterator();
     		while (itr.hasNext()) {
     			Iterator<ArrayList<Character>> itrAux = itr;
     	    itr.next();
     	    itrAux.remove();
     		}
     		m_randomOperators = null;
     	}
     	if (m_results != null) {
     		m_results.clear();
     		m_results = null;
     	}
     	if (m_resultButtonsIDs != null) {
     		m_resultButtonsIDs.clear();
     		m_resultButtonsIDs = null;
     	}
     }
     
     /**
      * This method will check whether or not a Character is an operator.
      * @param ch the Character to check.
      * @return true if ch is an operator, false otherwise.
      */
     private Boolean isAnOperator(Character ch) {
     	Resources res = getResources();
     	
     	String[] strArray = res.getStringArray(R.array.array_operators);
     	for (int i=0 ; i<strArray.length ; i++) {
     		if (strArray[i].contains(ch.toString())) {
     			return true;
     		}
     	}
     	if (ch == res.getString(R.string.button_label_operator_init).charAt(0)) {
     		return true;
     	}
     	
     	return false;
     }
     
     /**
      * This method will do the math.
      * @param digits the digits it'll use to do the math.
      * @param operators the operators it'll use to do the math.
      * @return an ArrayList of Integer with the results. First the results for
      * the rows (indexes 0, 1 and 2) then the columns (indexes 3, 4 and 5).
      */
     private ArrayList<Integer> doTheMath(ArrayList<ArrayList<Integer>> digits, 
     		ArrayList<ArrayList<Character>> operators) {
     	
     	ArrayList<Integer> results = new ArrayList<Integer>();
     	Integer result = Integer.valueOf(0);
     	
     	// First the rows
     	for (int i=0 ; i<digits.size() ; i++) {
     		ArrayList<Integer> list = digits.get(i);
     		if (list.size() != 0) {
 	  			result = list.get(0);
 	    		for (int j=0 ; j<list.size()-1 ; j++) {
 	    			Integer m = list.get(j+1);
 	    			if (operators.get(i).get(j) == '+') {
 	    				result += m; 
 	    			}
 	    			else if (operators.get(i).get(j) == '-') {
 	    				result -= m;
 	    			}
 	    			else if (operators.get(i).get(j) == '×') {
 	    				result *= m;
 	    			}
 	    		}
 	    		results.add(result);
     		}
     	}
     	// Now the columns
     	ArrayList<Integer> columnsResults = new ArrayList<Integer>(3);
   		columnsResults.add(digits.get(0).get(0));
   		columnsResults.add(digits.get(0).get(1));
   		columnsResults.add(digits.get(0).get(2));
     	for (int i=1 ; i<digits.size()-1 ; i++) {
     		ArrayList<Integer> list = digits.get(i);
     		if (list.size() != 0) {
 	    		for (int j=0 ; j<list.size() ; j++) {
 	    			Integer m = list.get(j);
 	    			if (operators.get(i-1).get(j) == '+') {
 	    				result = columnsResults.get(j);
 	    				result += m;
 	    				columnsResults.set(j, result);
 	    			}
 	    			else if (operators.get(i-1).get(j) == '-') {
 	    				result = columnsResults.get(j);
 	    				result -= m;
 	    				columnsResults.set(j, result);
 	    			}
 	    			else if (operators.get(i-1).get(j) == '×') {
 	    				result = columnsResults.get(j);
 	    				result *= m;
 	    				columnsResults.set(j, result);
 	    			}
 	    		}
     		}
     	}
     	
     	results.addAll(columnsResults);
     	
     	return results;
     }
     
     /**
      * On click event handler.
      * @param v the view sending the event.
      */
     public void onClick(View v) {
     	m_buttonClicked = (Button)v;
     	Intent intent = new Intent(this, KeypadActivity.class);
     	this.startActivityForResult(intent, 2);
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if(resultCode == 2){
         	String keyPressed = data.getExtras().get("key_pressed").toString();
         	m_buttonClicked.setText(keyPressed);
         	ArrayList<ArrayList<Integer>> numbers = new ArrayList<ArrayList<Integer>>();
         	numbers = getNumbersFromBoard();
         	ArrayList<Integer> results = doTheMath(numbers, m_randomOperators);
         	// So now I have the results, let's compare them with the randomly
         	// generated ones.
         	checkResults(results);
         }
     }
     
     private void checkResults(ArrayList<Integer> results) {
     	int errorColor = getResources().getColor(R.color.error_color);
     	int okColor = getResources().getColor(R.color.ok_color);
     	
     	for (int i=0 ; i<m_resultButtonsIDs.size() ; i++) {
       	Button resultButton = (Button)findViewById(m_resultButtonsIDs.get(i));
      	if (results != null && m_results.get(i) == results.get(i)) {
       		resultButton.getBackground().setColorFilter(okColor, Mode.MULTIPLY);
       	}
       	else {
       		resultButton.getBackground().setColorFilter(errorColor, Mode.MULTIPLY);
       	}        		
     	}
     }
 }
