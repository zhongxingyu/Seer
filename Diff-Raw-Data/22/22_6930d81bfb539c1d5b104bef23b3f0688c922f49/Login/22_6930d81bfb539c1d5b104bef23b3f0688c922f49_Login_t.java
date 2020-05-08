 package tasktracker.view;
 
 import tasktracker.controller.DatabaseAdapter;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class Login extends Activity {
 
 	private DatabaseAdapter _dbHelper;
 	private Cursor _cursor;
 
 	EditText _loginUsername;
 	EditText _loginPassword;
 
 	EditText _newUsername;
 	EditText _newEmail;
 	EditText _newPassword;
 	EditText _newPasswordConfirm;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 
 		_loginUsername = (EditText) findViewById(R.id.username);
 		_loginPassword = (EditText) findViewById(R.id.password);
 
 		_newUsername = (EditText) findViewById(R.id.new_username);
 		_newEmail = (EditText) findViewById(R.id.new_email);
 		_newPassword = (EditText) findViewById(R.id.new_password);
 		_newPasswordConfirm = (EditText) findViewById(R.id.new_password_confirm);
 
 		_dbHelper = new DatabaseAdapter(this);
 		setupLogin();
 		setupCreateAccount();
 		setupDebugStuff();
 	}
 
 	/**
 	 * Skip database checks and use app with username "Debugger"
 	 */
 	void setupDebugStuff() {
 		Button debug = (Button) findViewById(R.id.button_debug);
 		debug.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				proceedToHomePage("Debugger");
 			}
 
 		});
 	}
 
 	private void setupLogin() {
 		Button login = (Button) findViewById(R.id.button_login);
 		login.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				_dbHelper.open();
 				_cursor = _dbHelper.fetchUser(_loginUsername.getText()
 						.toString(), _loginPassword.getText().toString());
 
 				if (_cursor.moveToFirst()) {
 
 					String user = _cursor.getString(_cursor
 							.getColumnIndex(DatabaseAdapter.USER));
 					_cursor.close();
 					_dbHelper.close();
 
 					proceedToHomePage(user);
 
 				} else {
 					// User not found in database.
					shortToast("Invalid username/password combination.");
 				}
 			}
 
 		});
 
 	}
 
 	private void setupCreateAccount() {
 
 		Button create = (Button) findViewById(R.id.button_create_account);
 		create.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 
 				String username = _newUsername.getText().toString();
 				String email = _newEmail.getText().toString();
 				String password = _newPassword.getText().toString();
 				String passwordConfirm = _newPasswordConfirm.getText()
 						.toString();
 
 				if (usernameTaken(username)) {
 					shortToast("This username is unavailable.");
 					return;
 				}
 
 				if (!isValidEmail(email)) {
 					shortToast("Please supply a valid email address.");
 					return;
 				}
 
 				if (!password.equals(passwordConfirm)) {
 					shortToast("Your passwords do not match.");
 					return;
 				}
 
 				_dbHelper.open();
 				_dbHelper.createUser(username, email, password);
 				_dbHelper.close();
 
 				longToast("Creation successful!");
 				proceedToHomePage(username);
 			}
 
 		});
 	}
 
 	/**
 	 * Checks if a username is available in the database.
 	 * 
 	 * @return True if the username is available in the database; otherwise
 	 *         false.
 	 */
 	private boolean usernameTaken(String username) {
 
 		_dbHelper.open();
 		_cursor = _dbHelper.fetchUser(username);
 		boolean nameTaken = _cursor.moveToFirst();
 		_cursor.close();
 		_dbHelper.close();
 
 		return nameTaken;
 
 	}
 
 	private void proceedToHomePage(String user) {
 		longToast("Welcome, " + user + "!");
 		
 		Intent intent = new Intent(getApplicationContext(), TaskListView.class);
 		intent.putExtra("USER", user);
 		startActivity(intent);
 	}
 
 	/**
 	 * from
 	 * http://stackoverflow.com/questions/1819142/how-should-i-validate-an-e
 	 * -mail-address-on-android
 	 */
 	public final static boolean isValidEmail(CharSequence target) {
 		if (target == null) {
 			return false;
 		} else {
 			return android.util.Patterns.EMAIL_ADDRESS.matcher(target)
 					.matches();
 		}
 	}
 
 	private void shortToast(String message) {
 		Toast toast = Toast.makeText(getApplicationContext(), message,
 				Toast.LENGTH_SHORT);
 		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
 		toast.show();
 	}
 	
 	private void longToast(String message){
 		Toast toast = Toast.makeText(getApplicationContext(), message,
 				Toast.LENGTH_LONG);
 		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
 		toast.show();
 	}
 }
