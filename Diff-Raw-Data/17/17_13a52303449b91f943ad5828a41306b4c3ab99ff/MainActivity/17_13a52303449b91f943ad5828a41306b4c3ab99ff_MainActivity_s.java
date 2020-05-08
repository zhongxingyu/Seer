 package com.main.calculate;
 
 import java.text.DecimalFormat;
 import android.app.Activity;
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.Context;
 import android.graphics.LightingColorFilter;
 import android.os.Bundle;
 import android.text.InputType;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 import com.main.androidcalculator.R;
 
 public class MainActivity extends Activity {
 
 	Thread thread = new Thread();
 
 	private Button plusButton, minusButton, timesButton, divideButton, equalsButton, deleteButton, clearButton, decimalButton, button1, button2, button3, button4, button5, button6, button7, button8, button9, button0;
 	private EditText textField;
 	private float result;
 	private boolean resultDisplayed = false;
 	private DecimalFormat decimalFormat = new DecimalFormat("#.#########");
// random comment to test
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		// requesting to turn the title OFF
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		// set layout file for this activity
 		setContentView(R.layout.main);
 		thread.start();
 		// get id's
 		plusButton = (Button) findViewById(R.id.plusButton);
 		minusButton = (Button) findViewById(R.id.minusButton);
 		timesButton = (Button) findViewById(R.id.timesButton);
 		divideButton = (Button) findViewById(R.id.divideButton);
 		equalsButton = (Button) findViewById(R.id.equalsButton);
 		deleteButton = (Button) findViewById(R.id.deleteButton);
 		clearButton = (Button) findViewById(R.id.clearButton);
 		decimalButton = (Button) findViewById(R.id.decimalButton);
 		button0 = (Button) findViewById(R.id.button0);
 		button1 = (Button) findViewById(R.id.button1);
 		button2 = (Button) findViewById(R.id.button2);
 		button3 = (Button) findViewById(R.id.button3);
 		button4 = (Button) findViewById(R.id.button4);
 		button5 = (Button) findViewById(R.id.button5);
 		button6 = (Button) findViewById(R.id.button6);
 		button7 = (Button) findViewById(R.id.button7);
 		button8 = (Button) findViewById(R.id.button8);
 		button9 = (Button) findViewById(R.id.button9);
 		textField = (EditText) findViewById(R.id.textField);
 		textField.setInputType(InputType.TYPE_NULL);
 		registerForContextMenu(textField);
 
 		// set button colours
 		plusButton.getBackground().setColorFilter(new LightingColorFilter(0xFF303030, 0xFF303030));
 		minusButton.getBackground().setColorFilter(new LightingColorFilter(0xFF303030, 0xFF303030));
 		timesButton.getBackground().setColorFilter(new LightingColorFilter(0xFF303030, 0xFF303030));
 		divideButton.getBackground().setColorFilter(new LightingColorFilter(0xFF303030, 0xFF303030));
 		equalsButton.getBackground().setColorFilter(new LightingColorFilter(0xFF303030, 0xFF303030));
 		deleteButton.getBackground().setColorFilter(new LightingColorFilter(0xFF303030, 0xFF303030));
 		clearButton.getBackground().setColorFilter(new LightingColorFilter(0xFF303030, 0xFF303030));
 
 		button0.getBackground().setColorFilter(new LightingColorFilter(0xFF6E6E6E, 0xFF6E6E6E));
 		button1.getBackground().setColorFilter(new LightingColorFilter(0xFF6E6E6E, 0xFF6E6E6E));
 		button2.getBackground().setColorFilter(new LightingColorFilter(0xFF6E6E6E, 0xFF6E6E6E));
 		button3.getBackground().setColorFilter(new LightingColorFilter(0xFF6E6E6E, 0xFF6E6E6E));
 		button4.getBackground().setColorFilter(new LightingColorFilter(0xFF6E6E6E, 0xFF6E6E6E));
 		button5.getBackground().setColorFilter(new LightingColorFilter(0xFF6E6E6E, 0xFF6E6E6E));
 		button6.getBackground().setColorFilter(new LightingColorFilter(0xFF6E6E6E, 0xFF6E6E6E));
 		button7.getBackground().setColorFilter(new LightingColorFilter(0xFF6E6E6E, 0xFF6E6E6E));
 		button8.getBackground().setColorFilter(new LightingColorFilter(0xFF6E6E6E, 0xFF6E6E6E));
 		button9.getBackground().setColorFilter(new LightingColorFilter(0xFF6E6E6E, 0xFF6E6E6E));
 
 		// button listeners
 		plusButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				checkLastOp("+");
 				resultDisplayed = false;
 			}
 		});
 		minusButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				checkLastOp("-");
 				resultDisplayed = false;
 			}
 		});
 		timesButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				checkLastOp("*");
 				resultDisplayed = false;
 			}
 		});
 		divideButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				checkLastOp("");
 				resultDisplayed = false;
 			}
 		});
 		equalsButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				if ((textField.getText().toString().contains("+") || textField.getText().toString().contains("-") || textField.getText().toString().contains("") || textField.getText().toString().contains("*")) || (textField.getText().toString().contains("+") && textField.getText().toString().contains("-") || textField.getText().toString().contains("-") && textField.getText().toString().contains("-") || textField.getText().toString().contains("*") && textField.getText().toString().contains("-") || textField.getText().toString().contains("") && textField.getText().toString().contains("-"))) {
 					if (Character.isDigit(textField.getText().charAt(textField.getText().length() - 1))) {
 						calculate();
 						resultDisplayed = true;
 					}
 				}
 			}
 		});
 		decimalButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				addToOutput(".");
 			}
 		});
 		deleteButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				delete();
 			}
 		});
 		clearButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				clear();
 			}
 		});
 		button0.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				checkResultDisplayed(0);
 			}
 		});
 		button1.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				checkResultDisplayed(1);
 			}
 		});
 		button2.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				checkResultDisplayed(2);
 			}
 		});
 		button3.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				checkResultDisplayed(3);
 			}
 		});
 		button4.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				checkResultDisplayed(4);
 			}
 		});
 		button5.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				checkResultDisplayed(5);
 			}
 		});
 		button6.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				checkResultDisplayed(6);
 			}
 		});
 		button7.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				checkResultDisplayed(7);
 			}
 		});
 		button8.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				checkResultDisplayed(8);
 			}
 		});
 		button9.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				checkResultDisplayed(9);
 			}
 		});
 		textField.setOnLongClickListener(new OnLongClickListener() {
 			@Override
 			public boolean onLongClick(View v) {
 				return false;
 			}
 		});
 	}
 
 	// checks if last character is one of the operands (not -)
 	public void checkLastOp(String op) {
 		if (textField.length() == 0 & op.equals("-")) {
 			addToOutput("-");
 		} else if (textField.length() > 0) {
 			if (op.equals("+")) {
 				if (textField.getText().toString().contains("+") || textField.getText().toString().contains("-") || textField.getText().toString().contains("*") || textField.getText().toString().contains("")) {
 					if (Character.isDigit(textField.getText().charAt(textField.length() - 1))) {
 						calculate();
 					} else {
						if (!Character.isDigit(textField.getText().charAt(textField.length() - 1))) {
							textField.getText().delete(textField.getText().length() - 1, textField.getText().length());
						}
 					}
 				}
 				addToOutput("+");
 			} else if (op.equals("-")) {
 				if (textField.getText().toString().contains("+") || textField.getText().toString().contains("-") || textField.getText().toString().contains("*") || textField.getText().toString().contains("")) {
 					if (Character.isDigit(textField.getText().charAt(textField.length() - 1))) {
 						calculate();
 					} else if (textField.getText().charAt(textField.length() - 1) == '+' || textField.getText().charAt(textField.length() - 1) == '-') {
 						textField.getText().delete(textField.getText().length() - 1, textField.getText().length());
 					}
 				}
 				addToOutput("-");
 			} else if (op.equals("*")) {
 				if (textField.getText().toString().contains("+") || textField.getText().toString().contains("-") || textField.getText().toString().contains("*") || textField.getText().toString().contains("")) {
 					if (Character.isDigit(textField.getText().charAt(textField.length() - 1))) {
 						calculate();
 					} else {
 						textField.getText().delete(textField.getText().length() - 1, textField.getText().length());
 					}
 				}
 				addToOutput("*");
 			} else if (op.equals("")) {
 				if (textField.getText().toString().contains("+") || textField.getText().toString().contains("-") || textField.getText().toString().contains("*") || textField.getText().toString().contains("")) {
 					if (Character.isDigit(textField.getText().charAt(textField.length() - 1))) {
 						calculate();
 					} else {
 						textField.getText().delete(textField.getText().length() - 1, textField.getText().length());
 					}
 				}
 				addToOutput("");
 			}
 		}
 	}
 
 	// calculation logic
 	public void calculate() {
 		for (int i = 0; i < textField.length(); i++) {
 			if (textField.getText().charAt(i) == '+') {
 				result = Float.parseFloat(textField.getText().toString().substring(0, i)) + Float.parseFloat(textField.getText().toString().substring(i + 1, textField.length()));
 				returnAnswer(decimalFormat.format(result));
			} else if (textField.getText().charAt(i) == '-') {
				if (textField.getText().charAt(0) != '-') {
					result = Float.parseFloat(textField.getText().toString().substring(0, i)) - Float.parseFloat(textField.getText().toString().substring(i + 1, textField.length()));
					returnAnswer(decimalFormat.format(result));
				}
 			} else if (textField.getText().charAt(i) == '*') {
 				result = Float.parseFloat(textField.getText().toString().substring(0, i)) * Float.parseFloat(textField.getText().toString().substring(i + 1, textField.length()));
 				returnAnswer(decimalFormat.format(result));
 			} else if (textField.getText().charAt(i) == '') {
 				result = Float.parseFloat(textField.getText().toString().substring(0, i)) / Float.parseFloat(textField.getText().toString().substring(i + 1, textField.length()));
 				returnAnswer(decimalFormat.format(result));
 			}
 		}
 		resultDisplayed = true;
 	}
 
 	// set's text field to the returned calculation
 	public void returnAnswer(String output) {
 		textField.setText(output);
 	}
 
 	// adds numbers to the output field
 	public void addToOutput(int i) {
 		textField.append(Integer.toString(i));
 	}
 
 	// adds operands to the output field
 	public void addToOutput(String string) {
 		textField.append(string);
 	}
 
 	// deletes last character added
 	public void delete() {
 		if (textField.getText().length() > 0) {
 			textField.getText().delete(textField.getText().length() - 1, textField.getText().length());
 		}
 	}
 
 	// clears text field
 	public void clear() {
 		textField.setText("");
 	}
 
 	public void checkResultDisplayed(int i) {
 		if (resultDisplayed) {
 			clear();
 			addToOutput(i);
 			resultDisplayed = false;
 		} else {
 			addToOutput(i);
 		}
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.add(0, v.getId(), 0, "Copy");
 		menu.add(0, v.getId(), 0, "Cut");
 		menu.add(0, v.getId(), 0, "Paste");
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		if (item.getTitle().equals("Copy")) {
 			copy();
 		} else if (item.getTitle().equals("Cut")) {
 			cut();
 		} else if (item.getTitle().equals("Paste")) {
 			paste();
 		}
 		return true;
 	}
 
 	public void copy() {
 		if (textField.length() > 0) {
 			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
 			ClipData clip = ClipData.newPlainText("result", textField.getText().toString());
 			clipboard.setPrimaryClip(clip);
 			Toast.makeText(this, "Result Copied", Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	public void cut() {
 		if (textField.length() > 0) {
 			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
 			ClipData clip = ClipData.newPlainText("result", textField.getText().toString());
 			clipboard.setPrimaryClip(clip);
 			clear();
 			Toast.makeText(this, "Result Cut", Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	public void paste() {
 		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 		ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
 		// Gets the clipboard as text.
 		addToOutput(item.getText().toString());
 		Toast.makeText(this, "Pasted from Clipboard", Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		super.onSaveInstanceState(savedInstanceState);
 		// Save state to the savedInstanceState
 		savedInstanceState.putString("TextField", textField.getText().toString());
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		// Restore state from savedInstanceState
 		textField.setText(savedInstanceState.getString("TextField"));
 	}
 
 }
