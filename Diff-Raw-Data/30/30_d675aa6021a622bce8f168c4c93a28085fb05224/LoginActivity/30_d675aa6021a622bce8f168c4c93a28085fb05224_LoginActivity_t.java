 package autobahn.android;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 import com.example.autobahn.R;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 
 
 public class LoginActivity extends Activity implements  View.OnClickListener {
 
     Button loginButton;
     EditText usernameField;
     EditText passwordField;
     AutobahnClient client;
 
 
     public final TextWatcher watcher = new TextWatcher() {
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
 
     /*
         stores at client the host and the port
      */
     private void initClient() {
         client=AutobahnClient.getInstance();
         client.setContext(this);
 
     }
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         initClient();
         setContentView(R.layout.login);
 
         loginButton = (Button) findViewById(R.id.loginButton);
         usernameField = (EditText) findViewById(R.id.username);
         passwordField = (EditText) findViewById(R.id.password);
 
         usernameField.addTextChangedListener(watcher);
         passwordField.addTextChangedListener(watcher);
 
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
         usernameField.getText().append(prefs.getString("username","") );
         passwordField.getText().append(prefs.getString("password","") );
 
 
         loginButton.setOnClickListener(this);
 
 
     }
 
     public void checkFields(){
 
         String username = usernameField.getText().toString();
         String password = passwordField.getText().toString();
 
         if(username.equals("") || password.equals(""))
             loginButton.setEnabled(false);
         else
             loginButton.setEnabled(true);
     }
 
 
     public void onClick(View view) {
         loginButton.setEnabled(false);
 
         String username = usernameField.getText().toString();
         String password = passwordField.getText().toString();
 
         client.setUserName(username);
         client.setPassword(password);
 
         try {
             client.logIn();
         } catch (AutobahnClientException e) {
             Toast toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
             loginButton.setEnabled(true);
             toast.show();
             return ;
         }
 
 
        //loginButton.setEnabled(true);
 
         Intent menuActivity = new Intent();
         menuActivity.setClass(getApplicationContext(),MainMenu.class);
         startActivity(menuActivity);
 
     }
 }
