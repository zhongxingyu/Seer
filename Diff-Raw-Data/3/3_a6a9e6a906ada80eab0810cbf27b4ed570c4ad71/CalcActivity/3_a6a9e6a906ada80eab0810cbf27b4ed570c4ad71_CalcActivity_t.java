 package com.app.numconv;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.app.numconv.ClearableEditText.OnClearListener;
 import com.app.numconv.NumberPickerView.OnChangeListener;
 
 import android.app.Dialog;
 import android.content.SharedPreferences;
 import android.inputmethodservice.KeyboardView;
 import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Html;
 import android.text.InputType;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.View.OnKeyListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class CalcActivity extends BarActivity {
 	
 	private static enum Operations {
 		PLUS, MINUS, CROSS, DIVIDE
 	}
 	
 	private class OperationButton implements OnClickListener {
 		private Button _operations[] = new Button[4];
 		private Operations _operation;
 		
 		public OperationButton() {
 			_operations[0] = (Button) findViewById(R.id.operation_plus);
 			_operations[1] = (Button) findViewById(R.id.operation_minus);
 			_operations[2] = (Button) findViewById(R.id.operation_cross);
 			_operations[3] = (Button) findViewById(R.id.operation_divide);
 			
 			for(Button button : _operations) {
 				button.setOnClickListener(this);
 			}
 			
 			_operations[0].setEnabled(false);
 			_operation = Operations.PLUS;
 		}
 
 		public void onClick(View v) {
 			for(Button button : _operations) {
 				button.setEnabled(v != button);
 			}
 			
 			if(v == _operations[0]) _operation = Operations.PLUS;
 			else if(v == _operations[1]) _operation = Operations.MINUS;
 			else if(v == _operations[2]) _operation = Operations.CROSS;
 			else if(v == _operations[3]) _operation = Operations.DIVIDE;
 			
 			updateResult();
 		}
 		
 		public Operations getOperation() {
 			return _operation;
 		}
 		
 		public int getOperationInt() {
 			switch(_operation) {
 			case PLUS: return 1;
 			case MINUS: return 2;
 			case CROSS: return 3;
 			case DIVIDE: return 4;
 			}
 			return 0;
 		}
 		
 		public void setOperation(Operations operation) {
 			_operation = operation;
 			
 			for(Button button : _operations) {
 				button.setEnabled(true);
 			}
 			
 			switch(_operation) {
 			case PLUS: _operations[0].setEnabled(false); break;
 			case MINUS: _operations[1].setEnabled(false); break;
 			case CROSS: _operations[2].setEnabled(false); break;
 			case DIVIDE: _operations[3].setEnabled(false); break;
 			}
 		}
 		
 		public void setOperation(int operation) {
 			if(operation >= 1 && operation <= 4)
 				setOperation(Operations.values()[operation - 1]);
 		}
 	}
 	
 	private NumberPickerView _fromView1;
 	private ClearableEditText _numberView1;
 	private NumberPickerView _fromView2;
 	private ClearableEditText _numberView2;
 	private NumberPickerView _toView;
 	private EditText _resultView;
 	private OperationButton _operation;
 	
 	private KeyboardView _keyboardView;
 	
 	OnChangeListener onNumberChangeListener = new OnChangeListener() {
 		public void onChange(View v, int oldValue, int newValue) {
 			if(oldValue == newValue) return;
 			
 			Log.d("changeVal", newValue + " " + (v == _fromView1) + _numberView1.isFocused() + " " +
 					(v == _fromView2) + _numberView2.isFocused() + " " + _numberView1.isSelected());
 			
 			if(_keyboardView != null && ((v == _fromView1 && _numberView1.isFocused()) ||
 					(v == _fromView2 && _numberView2.isFocused()))) {
 				ThisApplication app = (ThisApplication) getApplication();
 				_keyboardView.setKeyboard(app.getKeyboard(newValue));
 				_keyboardView.invalidateAllKeys();
 			}
 			
 			updateResult();
 		}
 	};
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.calc);
         
         //findViewById(R.id.calculatorButton).setVisibility(View.GONE);
         
         _fromView1 = (NumberPickerView)findViewById(R.id.from1);
         _numberView1 = (ClearableEditText)findViewById(R.id.number1);
         _fromView2 = (NumberPickerView)findViewById(R.id.from2);
         _numberView2 = (ClearableEditText)findViewById(R.id.number2);
 		_toView = (NumberPickerView)findViewById(R.id.to);
 		_resultView = (EditText)findViewById(R.id.result);
 		
 		_operation = new OperationButton();
 		
 		_toView.setSolidRightStyle(true);
 		_resultView.setBackgroundResource(R.drawable.left_edittext_background);
 		_resultView.setSingleLine();
 		
 		_toView.setRange(2, 36);
 		_toView.select(2);
 		
 		_toView.setOnChangeListener(onNumberChangeListener);
 		
 		prepareViews(_numberView1, _fromView1);
 		prepareViews(_numberView2, _fromView2);
 		
 		_resultView.setOnFocusChangeListener(new OnFocusChangeListener() {
 			public void onFocusChange(View v, boolean hasFocus) {
 				if(hasFocus) {
 					//_fromView1.addHot();
 					//_fromView2.addHot();
 					//_toView.addHot();
 				}
 			}
 		});
 		
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		_keyboardView = (KeyboardView) findViewById(R.id.keyboard);
 		if(!preferences.getBoolean("use_app_keyboard", true)) {
 			_keyboardView.setVisibility(View.GONE);
 			_keyboardView = null;
 		}
 		
 		if(_keyboardView == null) {
 			_numberView1.setInputType(InputType.TYPE_CLASS_TEXT);
 			_numberView2.setInputType(InputType.TYPE_CLASS_TEXT);
 			_resultView.requestFocus();
 		} else {
 			_keyboardView.setOnKeyboardActionListener(new OnKeyboardActionListener() {
 	
 				public void onKey(int primaryCode, int[] keyCodes) {
 					getCurrentFocus().onKeyDown(primaryCode, 
 							new KeyEvent(KeyEvent.ACTION_DOWN, primaryCode));
 				}
 	
 				public void onPress(int primaryCode) { }
 				public void onRelease(int primaryCode) { }
 				public void onText(CharSequence text) { }
 				public void swipeDown() { }
 				public void swipeLeft() { }
 				public void swipeRight() { }
 				public void swipeUp() { }
 			});
 			
 			ThisApplication app = (ThisApplication) getApplication();
 			
 			_keyboardView.setKeyboard(app.getKeyboard(_fromView1.getNumber()));
 			_keyboardView.setVisibility(View.GONE);
 			_keyboardView.invalidateAllKeys();
 		}
 	}
 	
 	private void prepareViews(ClearableEditText number, final NumberPickerView np) {
 		np.setRange(2, 36);
 		np.select(10);
 		np.setSolidRightStyle(true);
 		number.setSolidLeftStyle(true);
 		
 		np.setOnChangeListener(onNumberChangeListener);
 		
 		number.setOnKeyListener(new OnKeyListener() {
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				updateResult();
 				return false;
 			}
 		});
 		
 		number.setOnClearListener(new OnClearListener() {
 			public void onClear() {
 				_resultView.setText("");
 			}
 		});
 		
 		number.setOnFocusChangeListener(new OnFocusChangeListener() {
 			public void onFocusChange(View v, boolean hasFocus) {
 				if(_keyboardView == null) return;
 				_keyboardView.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
 				if(hasFocus) {					
 					ThisApplication app = (ThisApplication) getApplication();
 					_keyboardView.setKeyboard(app.getKeyboard(np.getNumber()));
 				}
 			}
 		});
 	}
 	
 	private void updateResult() {
 		String text1 = _numberView1.getText().toString();
 		String text2 = _numberView2.getText().toString();
 		if(text1.length() == 0 || text2.length() == 0) _resultView.setText("");
 		else try {
 			BigDecimal value1 = new BigDecimal(Converter.convert(text1, _fromView1.getNumber(), 10, true));
 			BigDecimal value2 = new BigDecimal(Converter.convert(text2, _fromView2.getNumber(), 10, true));
 			BigDecimal result = BigDecimal.ZERO;
 			
 			switch(_operation.getOperation()) {
 			case PLUS: result = value1.add(value2); break;
 			case MINUS: result = value1.subtract(value2); break;
 			case CROSS: result = value1.multiply(value2); break;
 			case DIVIDE: 
				result = value2.compareTo(BigDecimal.ZERO) == 0 ? 
						BigDecimal.ZERO : value1.divide(value2, 2, RoundingMode.HALF_UP); 
 				break;
 			default: result = value1;
 			}
 			
 			boolean minus_sign = result.compareTo(BigDecimal.ZERO) < 0;
 			if(minus_sign) result = result.abs();
 			
 			DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
 			formatSymbols.setDecimalSeparator('.');
 			formatSymbols.setNaN("0");
 			DecimalFormat format = new DecimalFormat();
 			format.setDecimalFormatSymbols(formatSymbols);
 			format.setMaximumFractionDigits(20);
 			format.setGroupingUsed(false);
 			
 			if(Double.isInfinite(result.doubleValue())) _resultView.setText(R.string.infinity);
 			else _resultView.setText((minus_sign ? "-" : "") + 
 					Converter.convert(format.format(result), 10, _toView.getNumber()));
 		} catch(NumberFormatException e) {
 			Log.w("Calc", "NFE: " + e.getMessage());
 			_resultView.setText(R.string.change_number_system_error);
 		}
 	}
 	
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putInt("from1", _fromView1.getNumber());
 		outState.putInt("from2", _fromView2.getNumber());
 		outState.putString("number1", _numberView1.getText().toString());
 		outState.putString("number2", _numberView2.getText().toString());
 		outState.putInt("to", _toView.getNumber());
 		outState.putInt("operation", _operation.getOperationInt());
 	}
 	
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		int from1 = savedInstanceState.getInt("from1", 0);
 		int from2 = savedInstanceState.getInt("from2", 0);
 		String number1 = savedInstanceState.getString("number1");
 		String number2 = savedInstanceState.getString("number2");
 		int to = savedInstanceState.getInt("to", 0);
 		int operation = savedInstanceState.getInt("operation");
 		
 		if(from1 != 0) _fromView1.select(from1);
 		if(from2 != 0) _fromView2.select(from2);
 		if(to != 0) _toView.select(to);
 		if(number1 != null) _numberView1.setText(number1);
 		if(number2 != null) _numberView2.setText(number2);	
 		_operation.setOperation(operation);
 		
 		updateResult();
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.calc_menu, menu);
        return super.onCreateOptionsMenu(menu);
     }
 		
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.solutionButton:
 			showSolution();
 			break;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 	
 	private void showSolution() {
 		String text1 = _numberView1.getText().toString();
 		String text2 = _numberView2.getText().toString();
 		
 		int from1 = _fromView1.getNumber();
 		int from2 = _fromView2.getNumber();
 		int to = _toView.getNumber();
 		
 		if(text1.length() == 0 || text2.length() == 0) return;
 		
 		String decimal1, decimal2, operationResult, result;
 		try {
 			decimal1 = Converter.convert(text1, from1, 10);
 			decimal2 = Converter.convert(text2, from2, 10);
 			
 			BigDecimal value1 = new BigDecimal(decimal1);
 			BigDecimal value2 = new BigDecimal(decimal2);
 			
 			switch(_operation.getOperation()) {
 			case PLUS: operationResult = value1.add(value2).toString(); break;
 			case MINUS: operationResult = value1.subtract(value2).toString(); break;
 			case CROSS: operationResult = value1.multiply(value2).toString(); break;
 			case DIVIDE: 
 				operationResult = value2.compareTo(BigDecimal.ZERO) == 0 ? "0" : value1.divide(value2, 2, RoundingMode.HALF_UP).toString(); 
 				break;
 			default: operationResult = value1.toString();
 			}
 			
 			result = Converter.convert(operationResult, 10, to);
 		} catch(Exception e) {
 			return;
 		}
 		
 		Dialog dialog = new Dialog(this);
 		dialog.setContentView(R.layout.solution);
 		dialog.setTitle(R.string.solution);
 		TextView solutionView = (TextView) dialog.findViewById(R.id.solution);
 		
 		StringBuilder sb = new StringBuilder();
 		
 		if(from1 != 10) {
 			Converter converter = new Converter(text1, from1);
 			
 			sb.append(getString(R.string.solution_from));
 			sb.append(" ");
 			sb.append(from1);
 			sb.append(" ");
 			sb.append(getString(R.string.solution_to_dec));
 			sb.append("<br>");
 			
 			sb.append(text1);
 			sb.append("<sub>");
 			sb.append(from1);
 			sb.append("</sub>");
 			sb.append(" = ");
 			sb.append(converter.getSolution(Converter.SolutionStep.TO_DEC_FIRST));
 			sb.append(" = ");
 			sb.append(converter.getSolution(Converter.SolutionStep.TO_DEC_SECOND));
 			sb.append(" = ");
 			sb.append(decimal1);
 			sb.append("<sub>10</sub><br>");
 		}
 		
 		if(from2 != 10) {
 			Converter converter = new Converter(text2, from2);
 			
 			sb.append(getString(R.string.solution_from));
 			sb.append(" ");
 			sb.append(from2);
 			sb.append(" ");
 			sb.append(getString(R.string.solution_to_dec));
 			sb.append("<br>");
 			
 			sb.append(text2);
 			sb.append("<sub>");
 			sb.append(from2);
 			sb.append("</sub>");
 			sb.append(" = ");
 			sb.append(converter.getSolution(Converter.SolutionStep.TO_DEC_FIRST));
 			sb.append(" = ");
 			sb.append(converter.getSolution(Converter.SolutionStep.TO_DEC_SECOND));
 			sb.append(" = ");
 			sb.append(decimal2);
 			sb.append("<sub>10</sub><br>");
 		}
 		
 		sb.append(getString(R.string.solution_operation));
 		sb.append("<br>");
 		sb.append(decimal1);
 		sb.append(getOperationSign());
 		sb.append(decimal2);
 		sb.append(" = ");
 		sb.append(operationResult);
 		sb.append("<br>");
 		
 		if(to != 10) {
 			sb.append("<br>");
 			sb.append(getString(R.string.solution_from_dec_to));
 			sb.append(" ");
 			sb.append(to);
 			sb.append("<br>");
 			if(operationResult.charAt(0) == '-')
 				sb.append(Converter.getSolution(operationResult.substring(1), to));
 			else
 				sb.append(Converter.getSolution(operationResult, to));
 		}
 		
 		sb.append("<br>");
 		sb.append(getString(R.string.solution_answer));
 		sb.append("<br>");
 		sb.append(text1);
 		sb.append("<sub>");
 		sb.append(from1);
 		sb.append("</sub>");
 		sb.append(getOperationSign());
 		sb.append(text2);
 		sb.append("<sub>");
 		sb.append(from2);
 		sb.append("</sub> = ");
 		
 		if(from1 != 10 && from2 != 10) {
 			sb.append(decimal1);
 			sb.append("<sub>10</sub>");
 			sb.append(getOperationSign());
 			sb.append(decimal2);
 			sb.append("<sub>10</sub> = ");
 		}
 		
 		sb.append(result);
 		sb.append("<sub>");
 		sb.append(to);
 		sb.append("</sub>");
 		
 		solutionView.setText(Html.fromHtml(sb.toString()));
 		
 		dialog.show();
 	}
 	
 	private String getOperationSign() {
 		switch(_operation.getOperation()) {
 		case PLUS: return " + ";
 		case MINUS: return " - ";
 		case CROSS: return Converter.dot;
 		case DIVIDE: return " / ";
 		default: return " ? ";
 		}
 	}
 }
