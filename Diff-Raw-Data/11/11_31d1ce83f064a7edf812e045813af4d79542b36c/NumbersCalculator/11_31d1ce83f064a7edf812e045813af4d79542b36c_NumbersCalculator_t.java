 package com.example.numberscalculator;
 
 import java.util.Vector;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class NumbersCalculator extends Activity {
 	private String TAG = "NumbersCalculatorDebug";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_numbers_calculator);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.numbers_calculator, menu);
 		return true;
 	}
 
 	public void onDigitButtonClicked(View view) {
 		Button btn = (Button)view;
 		TextView resultedText = (TextView)findViewById(R.id.ResultView);
 		if (btn != null && resultedText != null) {
 			if (resultedText.getText().charAt(0) == '0' ) {
 				resultedText.setText(btn.getText());
 			} else {
 				resultedText.append(btn.getText());
 			}
 		}
 	}
 
 	public void onOperationButtonClicked(View view) {
 		Button btn = (Button)view;
 		TextView resultedText = (TextView)findViewById(R.id.ResultView);
 		if (btn != null && resultedText != null) {
 			CharSequence str = resultedText.getText();
 			switch(str.charAt(str.length() - 1)) {
 			case '+':
 			case '-':
 			case '/':
 			case '*':
 			{
 				// new operation is entered. Override last operation
 				str = str.subSequence(0, str.length() - 1);
 				resultedText.setText(str);
 				resultedText.append(btn.getText());
 				break;
 			}
 			default: // if last character was a digit we can simply append an operation.
 				resultedText.append(btn.getText());
 			}
 		}
 	}
 
 	public void onEraseButtonClicked(View view) {
 		TextView resultedText = (TextView)findViewById(R.id.ResultView);
 		if (resultedText != null) {
 			CharSequence str = resultedText.getText();
 			if (str.charAt(0) != '0') {
 				str = str.subSequence(0, str.length() - 1);
 				if (str.length() == 0 ) {
 					str = "0";
 				}
 				resultedText.setText(str);
 			}
 		}
 	}
 
 	public void onEqualButtonPressed(View view) {
 		TextView resultedText = (TextView)findViewById(R.id.ResultView);
 		if (resultedText != null) {
 			Vector<String> numbers = new Vector<String>();
 			Vector<String> operations = new Vector<String>();
 			String expressionStr = resultedText.getText().toString();
 			StringBuilder number = new StringBuilder();
 			while (expressionStr.length() > 0) {
 				switch (expressionStr.charAt(0)) {
 				case '0':
 				case '1':
 				case '2':
 				case '3':
 				case '4':
 				case '5':
 				case '6':
 				case '7':
 				case '8':
 				case '9':
 				{
 					Log.d(TAG, "char is " + expressionStr.charAt(0));
 					number.append(expressionStr.charAt(0));
 					Log.d(TAG, "new expression str is " + expressionStr);
 					break;
 				}
 				default:
 				{
 					if (number.length() > 0) {
 						numbers.add(number.toString());
 						number = new StringBuilder();
 						if (!operations.isEmpty() &&
 								numbers.size() >= 2) {
 							// Let's count a result for two last numbers
 							// if * or / were previous operations.
 							if (operations.lastElement().startsWith("*")){
 								operations.removeElementAt(operations.size() - 1);
 								int result = Integer.parseInt(numbers.lastElement());
 								numbers.removeElementAt(numbers.size() - 1);
 								result *= Integer.parseInt(numbers.lastElement());
 								numbers.removeElementAt(numbers.size() - 1);
 								numbers.add(String.valueOf(result));
 							} else if (operations.lastElement().startsWith("/") ) {
 								operations.removeElementAt(operations.size() - 1);
 								int result = Integer.parseInt(numbers.lastElement());
 								numbers.removeElementAt(numbers.size() - 1);
 								result /= Integer.parseInt(numbers.lastElement());
 								numbers.removeElementAt(numbers.size() - 1);
 								numbers.add(String.valueOf(result));
 							}
 						}
 					}
 					operations.add(String.valueOf(expressionStr.charAt(0)));					
 					break;
 				}
 				}//switch
 				expressionStr = expressionStr.substring(1);
 			}
 			if (number.length() > 0) {
 				numbers.add(number.toString());
 			}
 			// now let's count resulted value
 			while (!operations.isEmpty() &&
 					!numbers.isEmpty()) {
 				int result = 0;
 				switch(operations.firstElement().charAt(0)) {
 				case '+':
 					operations.removeElementAt(0);
 					result = Integer.parseInt(numbers.firstElement());
 					numbers.removeElementAt(0);
 					result += Integer.parseInt(numbers.firstElement());
 					numbers.removeElementAt(0);
					numbers.insertElementAt(String.valueOf(result), 0);
 					break;
 				case '-':
 					operations.removeElementAt(0);
 					result = Integer.parseInt(numbers.firstElement());
 					numbers.removeElementAt(0);
 					result -= Integer.parseInt(numbers.firstElement());
 					numbers.removeElementAt(0);
					numbers.insertElementAt(String.valueOf(result), 0);
 					break;
 				case '*':
 					operations.removeElementAt(0);
 					result = Integer.parseInt(numbers.firstElement());
 					numbers.removeElementAt(0);
 					result *= Integer.parseInt(numbers.firstElement());
 					numbers.removeElementAt(0);
					numbers.insertElementAt(String.valueOf(result), 0);
 					break;
 				case '/':
 					operations.removeElementAt(0);
 					result = Integer.parseInt(numbers.firstElement());
 					numbers.removeElementAt(0);
 					result /= Integer.parseInt(numbers.firstElement());
 					numbers.removeElementAt(0);
					numbers.insertElementAt(String.valueOf(result), 0);
 					break;
 				default:
 					Log.e(TAG, "ERRROR, WRONG OPERATION CHARACTER");
 					break;
 				} //switch
 			} //while
 			if (numbers.isEmpty()) {
 				Log.e(TAG, "NUMBERS VECTOR IS EMPTY!");
 			} else {
 				resultedText.setText(numbers.firstElement());
 			}
 		}
 	}
 }
