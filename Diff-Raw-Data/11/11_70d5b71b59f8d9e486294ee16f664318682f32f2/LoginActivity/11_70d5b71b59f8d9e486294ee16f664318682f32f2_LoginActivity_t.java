 package ch.epfl.unison.ui;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Html;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import ch.epfl.unison.LibraryService;
 import ch.epfl.unison.R;
 import ch.epfl.unison.UnisonApp;
 import ch.epfl.unison.api.JsonStruct;
 import ch.epfl.unison.api.UnisonAPI;
 import ch.epfl.unison.api.UnisonAPI.Error;
 
 import com.actionbarsherlock.app.SherlockActivity;
 
 public class LoginActivity extends SherlockActivity {
 
    private static final String TAG = "ch.epfl.unison.LoginActivity";

     private Button loginBtn;
     private TextView signupTxt;
 
     private EditText email;
     private EditText password;
 
     private UnisonApp app;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.setContentView(R.layout.login);
 
         this.app = (UnisonApp) this.getApplication();
 
         this.email = (EditText) findViewById(R.id.email);
         this.password = (EditText) findViewById(R.id.password);
 
         this.signupTxt = (TextView) findViewById(R.id.signupTxt);
         this.signupTxt.setText(Html.fromHtml("New to GroupStreamer? <a href=\"#\">Sign up</a>."));
 
         this.loginBtn = (Button) findViewById(R.id.loginBtn);
         this.loginBtn.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 final String email = LoginActivity.this.email.getText().toString();
                 final String password = LoginActivity.this.password.getText().toString();
                 LoginActivity.this.login(email, password);
             }
         });
     }
 
     @Override
     public void onStart() {
         super.onStart();
 
         Bundle extras = this.getIntent().getExtras();
         if (extras != null && extras.getBoolean("logout")) {
             // Remove e-mail and password from the preferences.
             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
             SharedPreferences.Editor editor = prefs.edit();
             editor.remove("email");
             editor.remove("password");
             editor.remove("uid");
             editor.remove("lastupdate");
             editor.commit();
 
             // Truncate the library.
             this.startService(new Intent(LibraryService.ACTION_TRUNCATE));
 
         } else {
             // Try to login from the saved preferences.
             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
             String email = prefs.getString("email", null);
             String password = prefs.getString("password", null);
 
             if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                 this.login(email, password);
             }
         }
         this.fillEmailPassword();
     }
 
     private void fillEmailPassword() {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         this.email.setText(prefs.getString("email", null));
         this.password.setText(prefs.getString("password", null));
     }
 
     private void storeInfo(String email, String password, String nickname, Long uid, Long rid) {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         SharedPreferences.Editor editor = prefs.edit();
 
         editor.putString("email", email);
         editor.putString("password", password);
         editor.putString("nickname", nickname);
         editor.putLong("uid", uid != null ? uid : -1);
         editor.commit();
 
         this.app.setCurrentRoom(rid);
     }
 
     private void nextActivity() {
         if (this.app.getCurrentRoom() != null) {
             this.startActivity(new Intent(this, MainActivity.class));
         } else {
             this.startActivity(new Intent(this, RoomsActivity.class));
         }
     }
 
     private void login(final String email, final String password) {
         final ProgressDialog dialog = ProgressDialog.show(LoginActivity.this, null, "Signing in...");
         UnisonAPI api = new UnisonAPI(email, password);
         api.login(new UnisonAPI.Handler<JsonStruct.User>() {
 
             public void callback(JsonStruct.User struct) {
                 LoginActivity.this.storeInfo(email, password,
                         struct.nickname, struct.uid, struct.rid);
                 LoginActivity.this.nextActivity();
                 dialog.dismiss();
             }
 
             public void onError(Error error) {
                if (error.hasJsonError()) {
                    Toast.makeText(LoginActivity.this, error.jsonError.message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "error.", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Unknown error", error.error);
                }
                 dialog.dismiss();
             }
         });
     }
 }
