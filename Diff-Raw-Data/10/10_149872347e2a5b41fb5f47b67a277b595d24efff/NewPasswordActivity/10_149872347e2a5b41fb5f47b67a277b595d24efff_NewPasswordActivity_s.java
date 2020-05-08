 package by.bsuir.passgen.activities;
 
 import by.bsuir.passgen.AboutDialog;
 import by.bsuir.passgen.Generator;
 import by.bsuir.passgen.R;
 import by.bsuir.passgen.utilities.Alphabet;
 import by.bsuir.passgen.utilities.Constants;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.NumberPicker;
 import android.widget.NumberPicker.OnValueChangeListener;
 
 public class NewPasswordActivity extends Activity implements OnValueChangeListener {
 
 	private EditText password;
 	private NumberPicker passwordLen;
 	private SharedPreferences preferences;
 	
 	private CheckBox upperCase, lowerCase, numbers, symbols;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.new_password_layout);
 		
 		password = (EditText) findViewById(R.id.password);
 		
 		passwordLen = (NumberPicker) findViewById(R.id.password_len);
 		passwordLen.setMinValue(Constants.MIN_PWD_LEN);
 		passwordLen.setMaxValue(Constants.MAX_PWD_LEN);
 		
 		passwordLen.setOnValueChangedListener(this);
 		
 		upperCase = (CheckBox) findViewById(R.id.upper_case);
 		lowerCase = (CheckBox) findViewById(R.id.lower_case);
 		numbers = (CheckBox) findViewById(R.id.numbers);
 		symbols = (CheckBox) findViewById(R.id.symbols);
 	}
 
 	private void restorePreferences() {
 		
 		upperCase.setChecked(preferences.getBoolean(Alphabet.UPPER_CASE.toString(), false));
 		lowerCase.setChecked(preferences.getBoolean(Alphabet.LOWER_CASE.toString(), false));
 		numbers.setChecked(preferences.getBoolean(Alphabet.NUMBERS.toString(), false));
 		symbols.setChecked(preferences.getBoolean(Alphabet.SYMBOLS.toString(), false));
 		
 		next(new View(getApplicationContext()));
 		
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();	
 		preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 		restorePreferences();
 	}
 
 	public void onCheck(View view) {
 		
 		CheckBox checkBox = (CheckBox) view;
 		
 		Editor editor = preferences.edit();
 		
 		switch (checkBox.getId()) {
 
 		case R.id.upper_case:
 			editor.putBoolean(Alphabet.UPPER_CASE.toString(), checkBox.isChecked()).commit();
 			break;
 		case R.id.lower_case:
 			editor.putBoolean(Alphabet.LOWER_CASE.toString(), checkBox.isChecked()).commit();
 			break;
 		case R.id.numbers:
 			editor.putBoolean(Alphabet.NUMBERS.toString(), checkBox.isChecked()).commit();
 			break;
 		case R.id.symbols:
 			editor.putBoolean(Alphabet.SYMBOLS.toString(), checkBox.isChecked()).commit();
 			break;
 		}
		
 		password.setText(Generator.generatePassword(passwordLen.getValue(), preferences));
 	}
 
 	public void onValueChange(NumberPicker arg0, int arg1, int arg2) {
 		password.setText(Generator.generatePassword(arg2, preferences));			
 	}
 	
 	public void next(View view) {
 		password.setText(Generator.generatePassword(passwordLen.getValue(), preferences));
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 
 		case R.id.menu_about:
 
 			DialogFragment about = new AboutDialog();
 			about.show(getFragmentManager(), "about");
 			return true;
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 }
