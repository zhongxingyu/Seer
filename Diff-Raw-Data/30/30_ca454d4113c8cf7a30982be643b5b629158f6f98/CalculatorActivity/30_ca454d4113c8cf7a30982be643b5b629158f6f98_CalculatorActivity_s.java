 package mengwu.project.calculator;
 
 import java.text.DecimalFormat;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CalculatorActivity extends Activity {
 	TextView inputText = null;
 	Button figure0Btn = null;
 	Button figure1Btn = null;
 	Button figure2Btn = null;
 	Button figure3Btn = null;
 	Button figure4Btn = null;
 	Button figure5Btn = null;
 	Button figure6Btn = null;
 	Button figure7Btn = null;
 	Button figure8Btn = null;
 	Button figure9Btn = null;
 	Button signPlusBtn = null;
 	Button signMinusBtn = null;
 	Button signMultipleBtn = null;
 	Button signDivieBtn = null;
 	Button pointBtn = null;
 	Button equalBtn = null;
 	Button deleteBtn = null;
 	Button clearBtn = null;
 
 	private static final String TAG = "CALCULATOR_ACTIVITY";
 	private double result = 0.0; // calculation result
 	private String currentStr = ""; // current string
 	boolean isLocked = false; // locked state when clicking operator button
 	boolean isPoint = false; // whether existed single point or not
 	char operator = ' '; // current operator
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		findView();
 		setOnclickListener();
 	}
 
 	private void findView() {
 		inputText = (TextView) findViewById(R.id.inputText);
 		figure0Btn = (Button) findViewById(R.id.figure_0);
 		figure1Btn = (Button) findViewById(R.id.figure_1);
 		figure2Btn = (Button) findViewById(R.id.figure_2);
 		figure3Btn = (Button) findViewById(R.id.figure_3);
 		figure4Btn = (Button) findViewById(R.id.figure_4);
 		figure5Btn = (Button) findViewById(R.id.figure_5);
 		figure6Btn = (Button) findViewById(R.id.figure_6);
 		figure7Btn = (Button) findViewById(R.id.figure_7);
 		figure8Btn = (Button) findViewById(R.id.figure_8);
 		figure9Btn = (Button) findViewById(R.id.figure_9);
 		signPlusBtn = (Button) findViewById(R.id.plus_sign);
 		signMinusBtn = (Button) findViewById(R.id.minus_sign);
 		signMultipleBtn = (Button) findViewById(R.id.multiple_sign);
 		signDivieBtn = (Button) findViewById(R.id.division_sign);
 		deleteBtn = (Button) findViewById(R.id.delete);
 		pointBtn = (Button) findViewById(R.id.point);
 		equalBtn = (Button) findViewById(R.id.equal);
 		deleteBtn = (Button) findViewById(R.id.delete);
 		clearBtn = (Button) findViewById(R.id.clear);
 	}
 
 	private void setOnclickListener() {
 		figure0Btn.setOnClickListener(new figure0BtnListener());
 		figure1Btn.setOnClickListener(new figure1BtnListener());
 		figure2Btn.setOnClickListener(new figure2BtnListener());
 		figure3Btn.setOnClickListener(new figure3BtnListener());
 		figure4Btn.setOnClickListener(new figure4BtnListener());
 		figure5Btn.setOnClickListener(new figure5BtnListener());
 		figure6Btn.setOnClickListener(new figure6BtnListener());
 		figure7Btn.setOnClickListener(new figure7BtnListener());
 		figure8Btn.setOnClickListener(new figure8BtnListener());
 		figure9Btn.setOnClickListener(new figure9BtnListener());
 		signPlusBtn.setOnClickListener(new signPlusBtnListener());
 		signMinusBtn.setOnClickListener(new signMinusBtnListener());
 		signMultipleBtn.setOnClickListener(new signMultipleBtnListener());
 		signDivieBtn.setOnClickListener(new signDivieBtnListener());
 		pointBtn.setOnClickListener(new pointBtnListener());
 		equalBtn.setOnClickListener(new equalBtnListener());
 		deleteBtn.setOnClickListener(new deleteBtnListener());
 		clearBtn.setOnClickListener(new clearBtnListener());
 	}
 
 	class figure0BtnListener implements OnClickListener {
 		public void onClick(View v) {
 			inputText.setText(addStr('0'));
 			isLocked = false;
 		}
 	}
 
 	class figure1BtnListener implements OnClickListener {
 		public void onClick(View v) {
 			inputText.setText(addStr('1'));
 			isLocked = false;
 		}
 	}
 
 	class figure2BtnListener implements OnClickListener {
 		public void onClick(View v) {
 			inputText.setText(addStr('2'));
 			isLocked = false;
 		}
 	}
 
 	class figure3BtnListener implements OnClickListener {
 		public void onClick(View v) {
 			inputText.setText(addStr('3'));
 			isLocked = false;
 		}
 	}
 
 	class figure4BtnListener implements OnClickListener {
 		public void onClick(View v) {
 			inputText.setText(addStr('4'));
 			isLocked = false;
 		}
 	}
 
 	class figure5BtnListener implements OnClickListener {
 		public void onClick(View v) {
 			inputText.setText(addStr('5'));
 			isLocked = false;
 		}
 	}
 
 	class figure6BtnListener implements OnClickListener {
 		public void onClick(View v) {
 			inputText.setText(addStr('6'));
 			isLocked = false;
 		}
 	}
 
 	class figure7BtnListener implements OnClickListener {
 		public void onClick(View v) {
 			inputText.setText(addStr('7'));
 			isLocked = false;
 		}
 	}
 
 	class figure8BtnListener implements OnClickListener {
 		public void onClick(View v) {
 			inputText.setText(addStr('8'));
 			isLocked = false;
 		}
 	}
 
 	class figure9BtnListener implements OnClickListener {
 		public void onClick(View v) {
 			inputText.setText(addStr('9'));
 			isLocked = false;
 		}
 	}
 
 	class pointBtnListener implements OnClickListener {
 		public void onClick(View v) {
 			if (!isPoint) {
					Log.d(TAG, "opertor = " + operator);
				if (0 == inputText.getText().length() || ' ' != operator) {
 					addStr('0');
 					inputText.setText(addStr('.'));
 				} else {
					Log.d(TAG, "opertor = " + operator);
					if (' ' == operator) {
						addStr('0');
						inputText.setText(addStr('.'));
					} else
						inputText.setText(addStr('.'));
 				}
 			}
 			isLocked = false;
 			isPoint = true;
 		}
 	}
 
 	class signPlusBtnListener implements OnClickListener {
 		public void onClick(View v) {
 			currentStr = inputText.getText().toString();
 			if (!isLocked) {
 				if (0 == currentStr.length()) {
 					operator = '+';
 				} else {
 					if (' ' == operator) {
 						operator = '+';
 						result = Double.parseDouble(currentStr);
 						currentStr = "";
 					} else {
 						double currentShowValue = Double.parseDouble(currentStr);
 						result = calculator(result, currentShowValue, operator);
 						inputText.setText(formatResult(result));
 						operator = '+';
 						currentStr = ""; // clear current string
 					}
 				}
 				isLocked = true;
 			}
 			operator = '+';
 			currentStr = "";
 			isPoint = false;
 		}
 	}
 
 	class signMinusBtnListener implements OnClickListener {
 		public void onClick(View v) {
 			currentStr = inputText.getText().toString();
 			Log.d(TAG, currentStr);
 			if (!isLocked) {
 				if (0 == currentStr.length()) {
 					operator = '-';
 					// inputText.setText(addStr('-'));
 					Log.d(TAG, currentStr);
 				} else {
 					if (' ' == operator) {
 						operator = '-';
 						result = Double.parseDouble(currentStr);
 						currentStr = "";
 					} else {
 						Log.d(TAG, currentStr);
 						double currentShowValue = Double.parseDouble(currentStr);
 						result = calculator(result, currentShowValue, operator);
 						inputText.setText(formatResult(result));
 						operator = '-';
 						currentStr = ""; // clear current string
 					}
 				}
 				isLocked = true;
 			}
 			operator = '-';
 			currentStr = "";
 			isPoint = false;
 		}
 	}
 
 	class signMultipleBtnListener implements OnClickListener {
 		public void onClick(View v) {
 			currentStr = inputText.getText().toString();
 			if (!isLocked) {
 				if (0 == currentStr.length()) {
 					Toast.makeText(CalculatorActivity.this, "wrong operator", Toast.LENGTH_SHORT).show();
 					Log.d(TAG, "opt = " + operator);
 					Log.d(TAG, "" + currentStr.length());
 				} else {
 					if (' ' == operator) {
 						operator = '*';
 						Log.d(TAG, "opt = " + operator);
 						result = Double.parseDouble(currentStr);
 						currentStr = "";
 					} else {
 						double currentShowValue = Double.parseDouble(currentStr);
 						Log.d(TAG, "opt = " + operator);
 						result = calculator(result, currentShowValue, operator);
 						inputText.setText(formatResult(result));
 						operator = '-';
 						currentStr = ""; // clear current string
 					}
 				}
 				isLocked = true;
 			}
 			operator = '*';
 			currentStr = "";
 			isPoint = false;
 		}
 	}
 
 	class signDivieBtnListener implements OnClickListener {
 		public void onClick(View v) {
 			currentStr = inputText.getText().toString();
 			if (!isLocked) {
 				if (0 == currentStr.length()) {
 					Toast.makeText(CalculatorActivity.this, "wrong operator", Toast.LENGTH_SHORT).show();
 				} else {
 					if (' ' == operator) {
 						operator = '/';
 						Log.d(TAG, "opt = " + operator);
 						result = Double.parseDouble(currentStr);
 						currentStr = "";
 					} else {
 						double currentShowValue = Double.parseDouble(currentStr);
 						Log.d(TAG, "opt = " + operator);
 						result = calculator(result, currentShowValue, operator);
 						inputText.setText(formatResult(result));
 						operator = '-';
 						currentStr = ""; // clear current string
 					}
 				}
 				isLocked = true;
 			}
 			operator = '/';
 			currentStr = "";
 			isPoint = false;
 		}
 	}
 
 	class equalBtnListener implements OnClickListener {
 		public void onClick(View v) {
 			currentStr = inputText.getText().toString();
 			if (!isLocked) {
 				if (0 != currentStr.length()) {
 					if (' ' != operator) {
 						Log.d(TAG, currentStr);
 						double currentShowValue = Double.parseDouble(currentStr);
 						result = calculator(result, currentShowValue, operator);
 						inputText.setText(formatResult(result));
 						currentStr = "";
 					}
 				}
 				isLocked = true;
 			}
 			operator = ' ';
 			isPoint = false;
 		}
 	}
 
 	class deleteBtnListener implements OnClickListener {
 		public void onClick(View v) {
 			currentStr = inputText.getText().toString();
			if (!isLocked) {
 				if (currentStr.length() > 0) {
 					currentStr = currentStr.substring(0, currentStr.length() - 1);
 					inputText.setText(currentStr);
 				}
			}
			currentStr = "";
 		}
 	}
 
 	class clearBtnListener implements OnClickListener {
 		public void onClick(View v) {
 			result = 0.0;
 			isLocked = true;
 			isPoint = false;
 			operator = ' ';
 			currentStr = "";
 			inputText.setText(currentStr);
 		}
 	}
 
 	private double calculator(double a, double b, char opt) {
 		DecimalFormat df = new DecimalFormat("0.0000000000");
 		// Log.d(TAG, "a = " + a);
 		// Log.d(TAG, "b = " + b);
 		// Log.d(TAG, "opt = " + opt);
 		switch (opt) {
 		case '+':
 			result = Double.parseDouble(df.format(a + b));
 			break;
 		case '-':
 			result = Double.parseDouble(df.format(a - b));
 			break;
 		case '*':
 			result = Double.parseDouble(df.format(a * b));
 			break;
 		case '/':
 			if (0 == b) {
 				Toast.makeText(CalculatorActivity.this, "Divisor can not be zero, enter again",
 						Toast.LENGTH_SHORT).show();
 			} else {
 				result = Double.parseDouble(df.format(a / b));
 			}
 			break;
 		default:
 			Log.d(TAG, "opt = " + opt);
 			break;
 		}
 		Log.d(TAG, result + "");
 		return result;
 	}
 
 	private String addStr(char btnValue) {
 		currentStr += btnValue;
 		Log.i(TAG, "Current str is " + currentStr);
 		return currentStr;
 	}
 	
 	// cut off zero & point from the number of double-type
 	private String formatResult(double result) {
 		String covertTo = "";
 		covertTo = Double.toString(result);
 		covertTo = covertTo.replaceAll("0+$", "").replaceAll("[.]$","");
 		return covertTo;
 	}
 }
