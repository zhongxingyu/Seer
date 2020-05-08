 package pl.jacbar.runner;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class Login extends Activity {
 
 	private SharedPreferences preferences;
 	
 	
 	/** Called when the activity is first created. */
 	@TargetApi(9)
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.login);
 	    
         preferences = getSharedPreferences(Config.RUNNER_PREFERENCES, Activity.MODE_PRIVATE);
         if(!preferences.getString("username", "").equals("") && !preferences.getString("password", "").equals("")){
         	Intent runnerIntent = new Intent(getApplicationContext(), Runner.class);
 			startActivity(runnerIntent);
         }
 	    
 	    
 	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
 	    StrictMode.setThreadPolicy(policy); 
 	    
 	    Button loginBtn = (Button) findViewById(R.id.loginBtn);
 	    loginBtn.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				EditText usernameTE = (EditText) findViewById(R.id.userNameTE);
 				EditText passwordTE = (EditText) findViewById(R.id.passwordTE);
 				
 				String username = usernameTE.getText().toString();
 				String password = passwordTE.getText().toString();
 				
 				LoginResult result =  LoginHelper.Login(username, password);
 				
 				if(result.getHttpStatus() == 200){
 					SharedPreferences.Editor preferencesEditor = preferences.edit();
 					preferencesEditor.putString("username", username.toLowerCase());
 					preferencesEditor.putString("password", password);
 					preferencesEditor.putString("token", result.getToken());
 					preferencesEditor.commit();
 		        	Intent runnerIntent = new Intent(getApplicationContext(), Runner.class);
 					startActivity(runnerIntent);
 				} else  if(result.getHttpStatus() == 401){
 					new AlertDialog.Builder(Login.this)
 				    .setTitle("Login")
 					.setMessage("Unauthorized access")
 					.setNeutralButton("Ok",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,int which) {}
 					}).show();
 				} else {
 					new AlertDialog.Builder(Login.this)
 				    .setTitle("Login")
 					.setMessage("Connection error")
 					.setNeutralButton("Ok",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,int which) {}
 					}).show();
 				}
 				
 			}
 		});
 	    
 	    
 
 	}
     public void onDestroy(){
     	System.exit(0);
     }
        
    public void onStop(){
    	System.exit(0);
    }
 }
