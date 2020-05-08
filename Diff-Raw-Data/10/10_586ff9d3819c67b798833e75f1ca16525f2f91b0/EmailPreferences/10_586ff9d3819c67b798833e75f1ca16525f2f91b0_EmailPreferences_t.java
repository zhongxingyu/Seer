 package ca.ualberta.cmput301f12t05.ufill;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.database.Cursor;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 public class EmailPreferences extends Activity {
 
 	EmailAddressDbAdapter emailAdapter;
 	Spinner spinner;
 	EditText newEmail, modifyEmail;
 	String address;
 	BinController binController;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_email_preferences);
 		emailAdapter = new EmailAddressDbAdapter(this);
 		newEmail = (EditText) findViewById(R.id.EmailNamePrefText);
 		modifyEmail = (EditText) findViewById(R.id.ModifyEmailPrefText);
 		binController = new BinController(this);
 		spinner = (Spinner) findViewById(R.id.EmailSpinner);
 
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		spinner.setAdapter(binController.addItemsToSpinner());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_email_preferences, menu);
 		return true;
 	}
 
 	public void addNewEmail(View v) {
 
 		binController.addNewEmail(newEmail.getText().toString(), newEmail);
		newEmail.setText("");
 		onStart();
 
 	}
 
 	public void modifyEmail(View v) {
 		binController.modifyEmail(spinner.getSelectedItem().toString(),modifyEmail.getText().toString());
 		modifyEmail.setText("");
 		onStart();
 	}
 
 }
