 package com.app.numconv;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.app.numconv.ClearableEditText.OnClearListener;
 import com.app.numconv.NumberPickerView.OnChangeListener;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.inputmethodservice.KeyboardView;
 import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 
 import android.text.Html;
 import android.text.InputType;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.view.View.OnKeyListener;
 import android.view.View.OnTouchListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class MainActivity extends BarActivity {
 	private NumberPickerView _fromView;
 	private NumberPickerView _toView;
 	private EditText _resultView;
 	private ClearableEditText _numberView;
 	
 	private KeyboardView _keyboardView;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
 		PreferencesActivity.setDefaultValues(this);
 		
 		int versionCode = 0;
 		try {
 			versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
 		} catch (NameNotFoundException e) { }
 		
 		if(PreferenceManager.getDefaultSharedPreferences(this).getInt("run", 0) < versionCode) {
 			PreferenceManager.getDefaultSharedPreferences(this).edit()
 				.putInt("run", versionCode)
 				.commit();
 			
 			startActivity(new Intent(this, NewsActivity.class));
 		}
 		
 		/*findViewById(R.id.calculatorButton).setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				startActivity(new Intent(MainActivity.this, CalcActivity.class));
 			}
 		});*/
 		
 		_fromView = (NumberPickerView)findViewById(R.id.from);
 		_toView = (NumberPickerView)findViewById(R.id.to);
 		_resultView = (EditText)findViewById(R.id.result);
 		_numberView = (ClearableEditText)findViewById(R.id.number);
 		_resultView.setSingleLine();
 		
 		_fromView.setRange(2, 36);
 		_fromView.select(10);
 		_toView.setRange(2, 36);
 		_toView.select(2);
 		
 		OnChangeListener onNumberChangeListener = new OnChangeListener() {
 			public void onChange(View v, int oldValue, int newValue) {
 				if(oldValue == newValue) return;
 				
 				if(v == _fromView && _keyboardView != null) {
 					ThisApplication app = (ThisApplication) getApplication();
 					_keyboardView.setKeyboard(app.getKeyboard(newValue));
 					_keyboardView.invalidateAllKeys();
 				}
 				
 				String text = _numberView.getText().toString();
 				if(text.length() == 0) _resultView.setText("");
 				else try {
 					_resultView.setText(Converter.convert(text, _fromView.getNumber(), _toView.getNumber()));
 				} catch(NumberFormatException e) {
 					_resultView.setText(R.string.change_number_system_error);
 				}
 			}
 		};
 		
 		_fromView.setOnChangeListener(onNumberChangeListener);
 		_toView.setOnChangeListener(onNumberChangeListener);
 
 		_numberView.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if(_keyboardView != null) { 
 					InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(
 							Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(_numberView.getWindowToken(), 0);
 				}
 				
 				return true;
 			}
 		});
 		
 		_numberView.setOnKeyListener(new OnKeyListener() {
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				String text = _numberView.getText().toString();
 				if(text.length() == 0) _resultView.setText("");
 				else try {
 					_resultView.setText(Converter.convert(text, _fromView.getNumber(), _toView.getNumber()));
 				} catch(NumberFormatException e) {
 					_resultView.setText("Change system or remove symbols");
 				}
 				return false;
 			}
 		});
 		
 		_numberView.setOnClearListener(new OnClearListener() {
 			public void onClear() {
 				_resultView.setText("");
 			}
 		});
 		
 		_numberView.setOnFocusChangeListener(new OnFocusChangeListener() {
 			public void onFocusChange(View v, boolean hasFocus) {
 				if(_keyboardView == null) return;
 				
 				_keyboardView.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
 			}
 		});
 		
 		_resultView.setOnFocusChangeListener(new OnFocusChangeListener() {
 			public void onFocusChange(View v, boolean hasFocus) {
 				if(hasFocus) {
 					//_fromView.addHot();
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
 			_numberView.setInputType(InputType.TYPE_CLASS_TEXT);
 			_resultView.requestFocus();
 		} else {
 			_keyboardView.setOnKeyboardActionListener(new OnKeyboardActionListener() {
 	
 				public void onKey(int primaryCode, int[] keyCodes) {
 					getCurrentFocus().onKeyDown(primaryCode, 
 							new KeyEvent(KeyEvent.ACTION_DOWN, primaryCode));
 				}
 	
 				public void onPress(int primaryCode) { }
 				public void onRelease(int primaryCode) { }
 				public void onText(CharSequence text) {	}
 				public void swipeDown() { }
 				public void swipeLeft() { }
 				public void swipeRight() { }
 				public void swipeUp() { }
 			});
 			
 			ThisApplication app = (ThisApplication) getApplication();
 			
 			_keyboardView.setKeyboard(app.getKeyboard(_fromView.getNumber()));
 			_keyboardView.setVisibility(View.GONE);
 			_keyboardView.invalidateAllKeys();
 		}
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
     }
 		
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.calculatorButton:
 			startActivityForResult(new Intent(this, CalcActivity.class), 0);
 			break;
 		case R.id.solutionButton:
 			showSolution();
 			break;
 		case R.id.preferencesButton:
 			startActivity(new Intent(this, PreferencesActivity.class));
 			break;
 		case android.R.id.home:
 			startActivity(new Intent(this, NewsActivity.class));
 			return true;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		_resultView.requestFocus();
 		_numberView.requestFocus();
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 	
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putInt("from", _fromView.getNumber());
 		outState.putInt("to", _toView.getNumber());
 		outState.putString("number", _numberView.getText().toString());
 	}
 	
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		int from = savedInstanceState.getInt("from", 0);
 		int to = savedInstanceState.getInt("to", 0);
 		String number = savedInstanceState.getString("number");
 		
 		if(from != 0) _fromView.select(from);
 		if(to != 0) _toView.select(to);
 		if(number != null) {
 			_numberView.setText(number);
 			
 			if(number.length() == 0) _resultView.setText("");
 			else try {
 				_resultView.setText(Converter.convert(number, _fromView.getNumber(), _toView.getNumber()));
 			} catch(NumberFormatException e) {
 				_resultView.setText("Change system or remove symbols");
 			}
 		}
 	}
 	
 	private void showSolution() {
 		String input = _numberView.getText().toString();
 		int from = _fromView.getNumber();
 		int to = _toView.getNumber();
 		
 		if(input.length() == 0) return;
 		
 		String decimal, result;
 		try {
			decimal = Converter.convert(input, from, 10, true);
 			result = Converter.convert(input, from, to);
 		} catch(Exception e) {
 			return;
 		}
 		
 		Dialog dialog = new Dialog(this);
 		dialog.setContentView(R.layout.solution);
 		dialog.setTitle(R.string.solution);
 		TextView solutionView = (TextView) dialog.findViewById(R.id.solution);
 		
 		StringBuilder sb = new StringBuilder();
 		
 		if(from != 10) {
 			Converter converter = new Converter(input, from);
 			
 			sb.append(getString(R.string.solution_from));
 			sb.append(" ");
 			sb.append(from);
 			sb.append(" ");
 			sb.append(getString(R.string.solution_to_dec));
 			sb.append("<br>");
 			
 			sb.append(input);
 			sb.append("<sub>");
 			sb.append(from);
 			sb.append("</sub>");
 			sb.append(" = ");
 			sb.append(converter.getSolution(Converter.SolutionStep.TO_DEC_FIRST));
 			sb.append(" = ");
 			sb.append(converter.getSolution(Converter.SolutionStep.TO_DEC_SECOND));
 			sb.append(" = ");
 			sb.append(decimal);
 			sb.append("<sub>10</sub><br>");
 		}
 		
 		if(to != 10) {
 			sb.append("<br>");
 			sb.append(getString(R.string.solution_from_dec_to));
 			sb.append(" ");
 			sb.append(to);
 			sb.append("<br>");
 			sb.append(Converter.getSolution(decimal, to));
 		}
 		
 		sb.append("<br>");
 		sb.append(getString(R.string.solution_answer));
 		sb.append("<br>");
 		sb.append(input);
 		sb.append("<sub>");
 		sb.append(from);
 		sb.append("</sub> = ");
 		
 		if(from != 10 && to != 10) {
 			sb.append(decimal);
 			sb.append("<sub>10</sub> = ");
 		}
 		
 		sb.append(result);
 		sb.append("<sub>");
 		sb.append(to);
 		sb.append("</sub>");
 		
 		solutionView.setText(Html.fromHtml(sb.toString()));
 		
 		dialog.show();
 	}
 }
