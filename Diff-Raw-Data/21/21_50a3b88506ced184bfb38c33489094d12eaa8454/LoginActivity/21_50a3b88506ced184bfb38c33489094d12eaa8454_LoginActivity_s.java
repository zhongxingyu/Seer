 package autobahn.android;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.example.autobahn.R;
 
 
 public class LoginActivity extends Activity implements View.OnClickListener {
     public static final String MSG = "";
     public static final int LOGIN_AND_GO_BACK = 1;
     private final TextWatcher watcher = new TextWatcher() {
 		@Override
 		public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
			//To change body of implemented methods use File | Settings | File Templates.
 		}
 
 		@Override
 		public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
			//To change body of implemented methods use File | Settings | File Templates.
 		}
 
 		@Override
 		public void afterTextChanged(Editable editable) {
 			checkFields();
 		}
 	};
     private Button loginButton;
     private EditText usernameField;
     private EditText passwordField;
    private boolean goBack = false;
     private AutobahnClientException exception = null;
 
 	private void afterLogIn() {
 
 		if (exception != null) {
 			Toast toast = Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG);
 			loginButton.setEnabled(true);
 			toast.show();
 			return;
 		}
 
         setResult(RESULT_OK);
         finish();
     }
 
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.login);
 
 		loginButton = (Button) findViewById(R.id.loginButton);
 		usernameField = (EditText) findViewById(R.id.username);
 		passwordField = (EditText) findViewById(R.id.password);
 
 		usernameField.addTextChangedListener(watcher);
 		passwordField.addTextChangedListener(watcher);
 
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
 		usernameField.getText().append(prefs.getString("username", ""));
 		passwordField.getText().append(prefs.getString("password", ""));
 
 
 		loginButton.setOnClickListener(this);
 
 		Bundle bundle = getIntent().getExtras();
 
 		if (bundle != null ) {
             String s=bundle.getString(MSG,"");
 			if(!s.isEmpty()) {
 			    Toast toast = Toast.makeText(this, s, Toast.LENGTH_LONG);
                 toast.show();
             }
 		}
 	}
 
 	public void checkFields() {
 
 		String username = usernameField.getText().toString();
 		String password = passwordField.getText().toString();
 
 		if (username.equals("") || password.equals(""))
 			loginButton.setEnabled(false);
 		else
 			loginButton.setEnabled(true);
 	}
 
 	public void onClick(View view) {
 		loginButton.setEnabled(false);
 
 		String username = usernameField.getText().toString();
 		String password = passwordField.getText().toString();
 
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 		settings.edit().putString(PreferencesActivity.USERNAME_PREFERENCE_KEY, username).commit();
 		settings.edit().putString(PreferencesActivity.PASSWORD_PREFERENCE_KEY, password).commit();
 //		client.setUserName(username);
 //		client.setPassword(password);
 		AutobahnClient.getInstance().setUserName(username);
         AutobahnClient.getInstance().setPassword(password);
 
 		LoginTask task = new LoginTask();
 		task.execute();
 	}
 
 
 
     @Override
     public void onBackPressed() {
         super.onBackPressed();
         this.finish();
     }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 
 		inflater.inflate(R.menu.action_bar, menu);
 
 		return true;
 	}
 
 	// Called when an options item is clicked
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.preferences:
 				startActivity(new Intent(this, PreferencesActivity.class));
 				break;
 		}
 		return true;
 	}
 
 	private class LoginTask extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... type) {
 			exception = null;
 			try {
 				AutobahnClient.getInstance().logIn();
 			} catch (AutobahnClientException e) {
 				exception = e;
 			}
 			return null;
 		}
 
 		@Override
 		protected void onProgressUpdate(Void... progress) {
 			super.onProgressUpdate(progress);
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			afterLogIn();
 		}
 
 
 	}
 }
