 package com.reddit.worddit;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 
 public class WordditHome extends Activity {
 	public static final String TAG = "WordditHome";
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		setup();
 		
		findViewById(R.id.login_button_login)
			.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
				doButtonClick(v);
 			}
 		});
 		
		findViewById(R.id.login_check_new)
			.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
				doNewChecked(v);
 			}
 		});
 	}
 	
 	public Dialog onCreateDialog(int id) {
 		switch(id) {
 		default:
 			Resources r = getResources();
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			String msg = "(null)";
 			try { msg = r.getString(id); }
 			catch(Exception e) { msg = String.format(r.getString(R.string.msg_not_found), id); }
 			
 			return builder.setMessage(msg)
 				.setTitle(R.string.app_name)
 				.setCancelable(true)
 				.setNeutralButton(R.string.label_ok, null)
 				.show();
 		}
 	}
 	
 	private void setup() {
 		// Force confirm password field to reflect default state of checkbox
 		doNewChecked(null);
 	}
 	
 	public void doNewChecked(View v) {
 		CheckBox check = (CheckBox) findViewById(R.id.login_check_new);
 		EditText confirm = (EditText) findViewById(R.id.login_input_confirmpassword);
 		
 		if(check.isChecked()) {
 			confirm.setVisibility(View.VISIBLE);
 		} else {
 			confirm.setVisibility(View.GONE);
 		}
 	}
 	
 	public void doButtonClick(View v) {
 		CheckBox check = (CheckBox) findViewById(R.id.login_check_new);
 		if(check.isChecked()) {
 			doCreate(v);
 		}
 		else {
 			doLogin(v);
 		}
 	}
 	
 	public void doLogin(View v) {
 		doLogin(getEmailField(), getPasswordField());
 	}
 	
 	public void doCreate(View v) {
 		String email = getEmailField(), password = getPasswordField(), confirm = getConfirmField();
 		doCreate(email,password,confirm);
 	}
 	
 	protected void doLogin(String email, String password) {
 		boolean fail = false;
 		int msg = 0;
 		
 		if(email.length() == 0 || password.length() == 0) {
 			msg = R.string.msg_required_fields_missing;
 			fail = true;
 		}
 		
 		if(fail) {
 			showDialog(msg);
 		}
 		else {
 			// TODO: Do login stuff.
 		}
 	}
 	
 	protected void doCreate(String email, String password, String confirm) {
 		boolean fail = false;
 		int msg = 0;
 		
 		if(email.length() == 0 || password.length() == 0 || confirm.length() == 0) {
 			msg = R.string.msg_required_fields_missing;
 			fail = true;
 		}
 		else if(password.equals(confirm) == false) {
 			msg = R.string.msg_confirm_password;
 			fail = true;
 		}
 		
 		if(fail) {
 			showDialog(msg);
 		}
 		else {
 			// TODO: Do create account stuff.
 		}
 	}
 	
 	protected String getEmailField() {
 		EditText emailField = (EditText) this.findViewById(R.id.login_input_email);
 		return emailField.getText().toString();
 	}
 	
 	protected String getPasswordField() {
 		EditText passwordField = (EditText) this.findViewById(R.id.login_input_password);
 		return passwordField.getText().toString();
 	}
 	
 	protected String getConfirmField() {
 		EditText confirmField = (EditText) this.findViewById(R.id.login_input_confirmpassword);
 		return confirmField.getText().toString();
 	}
 }
