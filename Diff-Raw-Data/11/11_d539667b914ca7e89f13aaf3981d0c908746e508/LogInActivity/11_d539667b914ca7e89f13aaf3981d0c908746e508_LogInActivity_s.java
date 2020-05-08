 package com.jinheyu.lite_mms;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.jinheyu.lite_mms.data_structures.User;
 import com.jinheyu.lite_mms.netutils.BadRequest;
 import com.jinheyu.lite_mms.netutils.ValidationError;
 
 import org.json.JSONException;
 
 import java.io.IOException;
 
 public class LogInActivity extends Activity {
 
 	private EditText editTextUsername = null;
 	private Button buttonLogin = null;
 	private EditText editTextPassword = null;
     private TextView textViewError;
 
     @Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
         User user = Utils.readUserPrefs(this);
         if (user != null) {
             Intent detailIntent = new Intent(this, user.getDefaultActivity());
             MyApp.setCurrentUser(user);
             startActivity(detailIntent);
             finish();
             return;
         }
 
         setContentView(R.layout.activity_log_in);
 
 		this.editTextUsername = (EditText) findViewById(R.id.editTextUserName);
 		this.editTextPassword = (EditText) findViewById(R.id.editTextPassword);
         this.textViewError = (TextView) findViewById(R.id.textViewError);
 		this.buttonLogin = (Button) findViewById(R.id.buttonLogin);
 		this.buttonLogin.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 String username = editTextUsername.getText().toString().trim();
                 String password = editTextPassword.getText().toString().trim();
                 if (Utils.isEmptyString(username)) {
                     LogInActivity.this.textViewError.setText("*" + getString(R.string.username_empty_error));
                     return;
                 }
                 if (Utils.isEmptyString(password)) {
                     LogInActivity.this.textViewError.setText("*" + getString(R.string.password_empty_error));
                     return;
                 }
                 ProgressDialog pd = new ProgressDialog(LogInActivity.this);
                 pd.setMessage("正在登录");
                 pd.show();
                 new LoginTask(username, password, pd).execute();
             }
         });
 	}
 
 
 
     class LoginTask extends AsyncTask<Void, Void, User> {
 
         private final String username;
         private final String password;
         private final ProgressDialog pd;
         private Exception ex;
 
         public LoginTask(String username, String password, ProgressDialog pd) {
             this.username = username;
             this.password = password;
             this.pd = pd;
         }
 
 
         @Override
         protected User doInBackground(Void... voids) {
 
             try {
                 return MyApp.getWebServieHandler().login(username, password);
             } catch (IOException e) {
                 e.printStackTrace();
                 ex = e;
             } catch (JSONException e) {
                 e.printStackTrace();
                 ex = e;
             } catch (BadRequest badRequest) {
                 badRequest.printStackTrace();
                 ex = badRequest;
             } catch (ValidationError validationError) {
                 validationError.printStackTrace();
                 ex = validationError;
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(User user) {
             pd.cancel();
             if (user != null) {
                 // we save the username password
                 Utils.storeUserToken(user, LogInActivity.this);
                 MyApp.setCurrentUser(user);
                 // go to the work activity
                 Intent detailIntent = new Intent(LogInActivity.this, user.getDefaultActivity());
                 startActivity(detailIntent);
                 LogInActivity.this.finish();
             } else {
                 if (ex instanceof ValidationError) {
                     textViewError.setText("*" + ex.getMessage());
                 } else {
                     Utils.displayError(LogInActivity.this, ex);
                 }
             }
         }
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuItem menuItem = menu.add("设置");
         menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getTitle().equals("设置")) {
                 Intent intent = new Intent(LogInActivity.this, MyPreferenceActivity.class);
                 startActivity(intent);
         }
         return super.onOptionsItemSelected(item);
     }
 }
