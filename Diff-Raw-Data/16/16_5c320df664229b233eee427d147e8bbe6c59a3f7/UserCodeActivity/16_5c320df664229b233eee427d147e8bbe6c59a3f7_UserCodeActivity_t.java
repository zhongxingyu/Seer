 package ovgu.gruppe1.ehskapp;
 
 import java.io.FileNotFoundException;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class UserCodeActivity extends Activity {
 
 	EditText textField1, textField2, textField3, textField4, textField5;
 	String text1, text2, text3, text4, text5;
 	Button submitUserCodeButton;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.user_code_layout);
 
 		textField1 = (EditText) findViewById(R.id.editText1);
 		textField2 = (EditText) findViewById(R.id.EditText01);
 		textField3 = (EditText) findViewById(R.id.EditText02);
 		textField4 = (EditText) findViewById(R.id.EditText03);
 		textField5 = (EditText) findViewById(R.id.EditText04);
 
		submitUserCodeButton = (Button) findViewById(R.id.btn_save_code);
		submitUserCodeButton.setOnClickListener(new OnClickListener() { //Error: Null-Pointer-Exception
 
 			@Override
 			public void onClick(View v) {
 				text1 = textField1.getText().toString();
 				text2 = textField2.getText().toString();
 				text3 = textField3.getText().toString();
 				text4 = textField4.getText().toString();
 				text5 = textField5.getText().toString();
 
 				if (text1.equals("") || text2.equals("") || text3.equals("")
 						|| text4.equals("") || text5.equals("")) {
 					showErrorMessager("Fehlende Eingabe");
 				} else {
 					finishActivity();
 				}
 			}
 		});
 
 	}
 
 	private void showErrorMessager(String message) {
 		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
 	}
 
 	/**
 	 * closes the activity
 	 */
 	private void finishActivity() {
 		String code = text1 + text2 + text3 + text4 + text5;
 		code = code.toLowerCase(Locale.getDefault());
 		SharedPreferences preferences = PreferenceManager
 				.getDefaultSharedPreferences(this.getBaseContext());
 		Editor edit = preferences.edit();
 		edit.putString("usercode", code);
 		edit.commit();
 
 		try {
 			CSVWriter.writeLine(null, code + ".csv");
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Toast.makeText(this, "Usercode saved", 3000).show();
 		this.finish();
 	}
 }
