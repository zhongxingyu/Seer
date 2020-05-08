 package cz.cvut.localtrade;
 
 import android.app.ActionBar;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.EditText;
 import cz.cvut.localtrade.dao.UsersDAO;
 
 public class RegistrationActivity extends BaseActivity {
 	
 	private UsersDAO userDao;
 	private EditText username;
 	private EditText firstname;
 	private EditText lastname;
 	private EditText email;
 	private EditText password;
 	private EditText passwordCheck;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.registration_activity_layout);
 
 		userDao = new UsersDAO();
 		userDao.open();
 
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setTitle(getString(R.string.registration_title));
 
 		username = (EditText) findViewById(R.id.usernameInput);
 		firstname = (EditText) findViewById(R.id.firstnameInput);
 		lastname = (EditText) findViewById(R.id.lastnameInput);
 		email = (EditText) findViewById(R.id.emailInput);
 		password = (EditText) findViewById(R.id.passwordInput);
 		passwordCheck = (EditText) findViewById(R.id.checkPasswordInput);
 	}
 
 	@Override
 	protected void onResume() {
 		userDao.open();
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		userDao.close();
 		super.onPause();
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.registration_activity_menu, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			Intent intent = new Intent(this, LoginActivity.class);
 			// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void submitForm(MenuItem item) {
 		if (!isValid()) {
 			return;
 		}
 		showDialog(LOADING_DIALOG);
 		userDao.createUser(new RegistrationResponseImpl(), username.getText()
 				.toString(), firstname.getText().toString(), lastname.getText()
 				.toString(), email.getText().toString(), password.getText()
 				.toString());
 	}
 
 	private boolean isValid() {
 		return passwordCheck() && usernameCheck();
 	}
 
 	private boolean usernameCheck() {
 		String usernameStr = username.getText().toString();
 
 		if (usernameStr.isEmpty()) {
 			showToast(R.string.empty_username);
 			return false;
 		}
 		return true;
 	}
 
 	private boolean passwordCheck() {
 		String pswd = password.getText().toString();
 
 		if (pswd.isEmpty()) {
 			showToast(R.string.empty_password);
 			return false;
 		}
 
 		if (!pswd.equals(passwordCheck.getText().toString())) {
 			showToast(R.string.different_password);
 			return false;
 		}
 
 		return true;
 	}
 
 	public void cancelRegistration(MenuItem item) {
 		Intent intent = new Intent(RegistrationActivity.this,
 				LoginActivity.class);
 		startActivity(intent);
 	}
 
 	class RegistrationResponseImpl implements UsersDAO.RegistrationResponse {
 
 		@Override
 		public void onRegistered() {
 			Intent intent = new Intent(RegistrationActivity.this,
 					LoginActivity.class);
 			startActivity(intent);
 			loadingDialog.dismiss();
 		}
 
 		@Override
		public void onRegisterFail() {
			showToast(getString(R.string.connection_error));
 			loadingDialog.dismiss();
 		}
 
 	}
 
 }
