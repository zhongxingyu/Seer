 package com.dinosaurwithakatana.jobhackcareerbuilder;
 
 import java.util.concurrent.ExecutionException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 
 public class LoginActivity extends SherlockActivity{
 	private static final String TAG = CreateAccountActivity.class.getSimpleName();                                                                                                                  
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 
 		final EditText username = (EditText)findViewById(R.id.txtUsernameInput);
 		final EditText password = (EditText)findViewById(R.id.txtPasswordInput);
 
 		Button login = (Button) findViewById(R.id.btnLogin);
 		login.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				String loginUsername = username.getText().toString();
 				String loginPassword = password.getText().toString();
 
 				Log.v(TAG,"Logging in with "+ loginUsername);
 				try {
 					String responseJSON = new GetLoginAcct().execute(loginUsername).get();
 					JSONArray o = new JSONArray(responseJSON);
 					Log.v(TAG,o.toString(1));
 					int responseLength = o.length();
 					Log.v(TAG,""+responseLength);
 
 					JSONObject responseObject = o.getJSONObject(0);
 					Log.v(TAG,responseObject.toString(1));
 
 					if(responseObject.getString("username").equals(loginUsername)){
 						if(responseObject.getString("password").equals(loginPassword)){
 							System.out.println("Login Successful!");
 							Log.d(TAG,"Login Successful!");
 							Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
 							Intent i = new Intent(LoginActivity.this,MainActivity.class);
 							startActivity(i);
 						}
 						else{
 							System.out.println("Bad password");
 							Log.d(TAG,"Bad Password");
 							Toast.makeText(getApplicationContext(), "Bad Password!",Toast.LENGTH_SHORT).show();
 						}
 					}
 					else{
 						Log.d(TAG,"Login Unsuccessful");
 						Toast.makeText(getApplicationContext(), "Bad Username!", Toast.LENGTH_SHORT).show();
 					}
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (ExecutionException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 
 			}
 		});

		Button createAccountButton = (Button)findViewById(R.id.btnCreateAcct);
		createAccountButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(LoginActivity.this, CreateAccountActivity.class);
				startActivity(i);

			}
		});
 	}
}
