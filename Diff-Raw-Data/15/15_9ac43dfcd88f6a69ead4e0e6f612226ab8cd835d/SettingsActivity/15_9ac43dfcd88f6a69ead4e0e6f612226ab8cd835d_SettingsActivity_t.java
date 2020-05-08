 
 package the.umbautologin;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import the.umbautologin.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.*;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.*;
 
 public class SettingsActivity extends Activity
 {
     private SharedPreferences prefs;
     private CheckBox          successChbx, errorChbx, loggedinChbx;
     private EditText          urlField;
     private EditText          usernameField;
     private EditText          passwdField;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.settings);
         prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
 
         successChbx = (CheckBox) findViewById(R.id.prefs_checkbox_success);
         errorChbx = (CheckBox) findViewById(R.id.prefs_checkbox_error);
         loggedinChbx = (CheckBox) findViewById(R.id.prefs_checkbox_already_logged);
         urlField = (EditText) findViewById(R.id.prefs_url);
 
         usernameField = (EditText) findViewById(R.id.prefs_username);
         passwdField = (EditText) findViewById(R.id.prefs_passwd);
         
         Button save = (Button) findViewById(R.id.prefs_save);
         save.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view)
             {
             	
             	String username = usernameField.getText().toString();
             	String passwd = passwdField.getText().toString();
             	
                 String url = urlField.getText().toString();
                 if(!checkURL(url))
                 {
                     AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                     builder.setMessage("Invalid URL").setCancelable(false)
                             .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int id)
                                 {
                                     dialog.cancel();
                                 }
                             });
                     AlertDialog alert = builder.create();
                     alert.show();
                 } else
                 {
                     SharedPreferences.Editor editor = prefs.edit();
                     editor.putBoolean(Constants.PREF_KEY_NOTIFY_WHEN_SUCCESS, successChbx.isChecked());
                     editor.putBoolean(Constants.PREF_KEY_NOTIFY_WHEN_ERROR, errorChbx.isChecked());
                     editor.putBoolean(Constants.PREF_KEY_NOTIFY_WHEN_ALREADY_LOGGED_IN, loggedinChbx.isChecked());
                     editor.putString(Constants.PREF_KEY_URL, url);
                     editor.putString(Constants.PREF_KEY_USERNAME, username);
                     editor.putString(Constants.PREF_KEY_PASSWORD, passwd);
                     
                     editor.commit();
                     Toast.makeText(getApplicationContext(), R.string.conf_save, Toast.LENGTH_SHORT).show();
                     finish();
                 }
             }
         });
        
        successChbx.setChecked(prefs.getBoolean(Constants.PREF_KEY_NOTIFY_WHEN_SUCCESS, true));
        errorChbx.setChecked(prefs.getBoolean(Constants.PREF_KEY_NOTIFY_WHEN_ERROR, true));
        loggedinChbx.setChecked(prefs.getBoolean(Constants.PREF_KEY_NOTIFY_WHEN_ALREADY_LOGGED_IN, false));
        urlField.setText(prefs.getString(Constants.PREF_KEY_URL, getString(R.string.defaulturl)));
        usernameField.setText(prefs.getString(Constants.PREF_KEY_USERNAME, getString(R.string.defaultusername)));
        passwdField.setText(prefs.getString(Constants.PREF_KEY_PASSWORD, getString(R.string.defaultpasswd)));
     }
 
     private boolean checkURL(String s)
     {
         try
         {
             new URL(s);
             return true;
         } catch(MalformedURLException mex)
         {
             return false;
         }
     }
 
     @Override
     public void onResume()
     {
         super.onResume();
 
        
     }
 
 }
