 package com.allplayers.android;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import org.jasypt.util.text.BasicTextEncryptor;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Login extends Activity
 {
 	private Context context;
 	
 	/** called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		context = this.getBaseContext();
 		
 		String storedUser = LocalStorage.readUserName(context);
 		String storedPassword = LocalStorage.readPassword(context);
 		String storedSecretKey = LocalStorage.readSecretKey(context);
 		
		if(storedSecretKey == null || storedSecretKey.equals(""))
 		{
 			LocalStorage.writeSecretKey(context);
 			storedSecretKey = LocalStorage.readSecretKey(context);
 		}
 		
 		Globals.secretKey = storedSecretKey;
 		
 		if(storedUser != null && !storedUser.equals("") && storedPassword != null && !storedPassword.equals(""))
 		{
 			String result = APCI_RestServices.validateLogin(LocalStorage.readUserName(context), LocalStorage.readPassword(context));
 
 			try
 			{
 				JSONObject jsonResult = new JSONObject(result);
 				APCI_RestServices.user_id = jsonResult.getJSONObject("user").getString("uuid");
 
 				Intent intent = new Intent(Login.this, MainScreen.class);
 				startActivity(intent);
 				finish();
 			}
 			catch(JSONException ex)
 			{
 				System.err.println("Login/user_id/" + ex);
 
 				Toast invalidLogin = Toast.makeText(getApplicationContext(), "Invalid Login", Toast.LENGTH_LONG);
 				invalidLogin.show();
 			}
 		}
 		
 		final Button button = (Button)findViewById(R.id.loginButton);
 		button.setOnClickListener(new View.OnClickListener()
 		{
 			public void onClick(View v)
 			{
 				EditText usernameEditText = (EditText)findViewById(R.id.usernameField);     
 				EditText passwordEditText = (EditText)findViewById(R.id.passwordField);
 
 				String username = usernameEditText.getText().toString();
 				String password = passwordEditText.getText().toString();;
 
 				BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
 				textEncryptor.setPassword(LocalStorage.readSecretKey(context));
 				String encryptedPassword = textEncryptor.encrypt(password);
 				
 				LocalStorage.writeUserName(context, username);
 				LocalStorage.writePassword(context, password);
 				
 				String result = APCI_RestServices.validateLogin(username, encryptedPassword);
 
 				try
 				{
 					JSONObject jsonResult = new JSONObject(result);
 					APCI_RestServices.user_id = jsonResult.getJSONObject("user").getString("uuid");
 
 					Intent intent = new Intent(Login.this, MainScreen.class);
 					startActivity(intent);
 					finish();
 				}
 				catch(JSONException ex)
 				{
 					System.err.println("Login/user_id/" + ex);
 
 					Toast invalidLogin = Toast.makeText(getApplicationContext(), "Invalid Login", Toast.LENGTH_LONG);
 					invalidLogin.show();
 				}
 			}
 		});
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)
 	{
 		if(keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_MENU)
 		{
 			startActivity(new Intent(Login.this, FindGroupsActivity.class));
 		}
 		
 		return super.onKeyUp(keyCode, event);
 	}
 }
