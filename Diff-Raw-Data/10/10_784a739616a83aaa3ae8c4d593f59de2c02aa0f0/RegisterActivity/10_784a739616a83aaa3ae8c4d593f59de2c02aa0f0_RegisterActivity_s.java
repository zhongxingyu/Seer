 package com.rogoapp.auth;
 
 import java.util.ArrayList;
 //for ServerClient class
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.accounts.Account;
 import android.accounts.AccountAuthenticatorActivity;
 import android.accounts.AccountManager;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.rogoapp.CacheClient;
 import com.rogoapp.MainScreenActivity;
 import com.rogoapp.R;
 import com.rogoapp.ServerClient;
 
 public class RegisterActivity extends AccountAuthenticatorActivity{
 
 	private Button register;
 	private Button login;
 	private EditText username;
 	private EditText email;
 	private EditText password;
 
 
 	private boolean remove;
 	private static boolean token;
 
 	AccountManager am;
 	private CacheClient cache;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.register);
 		
 		register = (Button) this.findViewById(R.id.btnRegister);
 		login = (Button) this.findViewById(R.id.link_to_login);
 		login.setBackgroundColor(Color.TRANSPARENT);
 
 		username = (EditText) this.findViewById(R.id.reg_username);
 		email = (EditText) this.findViewById(R.id.reg_email);
 		password = (EditText) this.findViewById(R.id.reg_password);
 		if(token)
 			register.setText("Register and Store Login");
 		
 		am = AccountManager.get(this);
 		cache = new CacheClient(this);
 
 		this.remove = this.getIntent().getBooleanExtra("remove", false);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main_screen, menu);
 		return true;
 	}
 
 
 
 	public void onRegister(View v) {
 
 		String mUsername = username.getText().toString();
 		String pass = password.getText().toString();
 		String mEmail = email.getText().toString();
 
 		if(mUsername == "" || pass == "" || mEmail == ""){
 			showMessage("Please fill in all fields");
 			return;
 		}
 		if(hasErrors(mEmail, pass))
 			return;
 
 		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 		nameValuePairs.add(new BasicNameValuePair("username", mUsername));
 		nameValuePairs.add(new BasicNameValuePair("email", mEmail));
 
 		nameValuePairs.add(new BasicNameValuePair("password", AccountAuthenticator.hashPassword(pass)));
 
 		ServerClient sc = new ServerClient();
 		JSONObject jObj = sc.genericPostRequest("register", nameValuePairs);
 		String uid = null;
 		String status = null;
 		String session = "";
 		String secret = "";
 		try{
 			//uid = sc.getLastResponse().getString("uid");
 			status = jObj.getString("status");
			if(status == "error")
 				status = jObj.getString("data");
 			showMessage(status);
 			session = jObj.getJSONObject("data").getString("session");
 			secret = jObj.getJSONObject("data").getString("secret");
 		}catch(JSONException e){
 			System.err.print(e);
 			return;
 		}
 		System.out.println("status = " + status + ", uid = " + uid);
 
 		if(remove){
 			RogoAuthenticatorActivity.logout();
 		}
 
 
 		clear();
 
		if(status.equals("success")){
 			
 			Account account = this.login(mEmail, pass);
 			if(account == null)
 				return;
 
 			if(getToken())
 				am.setAuthToken(account, RogoAuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, secret);
 			cache.saveFile(CacheClient.SESSION_CACHE, session);
 
 			final Intent start = new Intent(this, MainScreenActivity.class);
 			start.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(start);
 		}
 	}
 
 	public void openLoginScreen(View v){
 		final Context context = this;
 		this.finish();
 		Intent intent = new Intent(context, RogoAuthenticatorActivity.class);
 		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		intent.putExtra("reset", true);
 		startActivity(intent);
 	}
 
 	private void clear(){
 		username.setText("");
 		email.setText("");
 		password.setText("");
 	}
 	private Boolean hasErrors(String user, String pass){
 
 		EmailValidator validate = new EmailValidator();
 
 		boolean hasErrors = false;
 
 		if (!validate.validate(user)) {  
 			hasErrors = true;
 
 			showMessage("Invalid Email Address");
 		}  
		else if (password.length() <= 6) {  
 			hasErrors = true;
 
 			showMessage("Password must be at least 6 characters");
 		}  
 		return hasErrors;
 	}
 
 	public void onRememberMe(View V){
 
 		//When the user has chosen to be remembered, sets the Login to store an auth-token
 		//and changes the Login button appropriately to reflect their choice
 
 		if(getToken()){
 			this.setToken(false);
 		}
 		else this.setToken(true);
 
 		if(getToken()){
 			register.setText("Register and Store Login");
 		}
 		else register.setText("Register and Login"); 
 	}
 
 	private Account login(String user, String pass){
 		Account[] accounts = am.getAccountsByType(RogoAuthenticatorActivity.PARAM_AUTHTOKEN_TYPE);
 
 		if(accounts.length != 0 && !accounts[0].name.equalsIgnoreCase(user)){
 			showMessage("Only one user is allowed on this phone.\nPlease log out before continuing.");
 			return null;
 		}
 		else if(accounts.length == 1 && accounts[0].name.equalsIgnoreCase(user)){
 			user = accounts[0].name;
 		}
 
 		// This is the magic that adds the account to the Android Account Manager  
 		final Account account = new Account(user, RogoAuthenticatorActivity.PARAM_AUTHTOKEN_TYPE);
 		am.addAccountExplicitly(account, pass, null);  
 		//		am.addAccount(PARAM_AUTHTOKEN_TYPE, PARAM_AUTHTOKEN_TYPE, null, null, this, null, null);
 
 		// Now we tell our caller, could be the Android Account Manager or even our own application  
 		// that the process was successful  
 
 		final Intent intent = new Intent();
 
 		intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, user);  
 		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, RogoAuthenticatorActivity.PARAM_AUTHTOKEN_TYPE);
 		intent.putExtra(AccountManager.KEY_PASSWORD, pass);
 
 		this.setAccountAuthenticatorResult(intent.getExtras());  
 		this.setResult(RESULT_OK, intent);
 
 		return account;
 	}
 
 
 private void showMessage(final String msg) {
 	if (TextUtils.isEmpty(msg))
 		return;
 
 	runOnUiThread(new Runnable() {
 		@Override
 		public void run() {
 			Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
 		}
 	});
 }
 
 public boolean getToken() {
 	return token;
 }
 
 public void setToken(boolean token) {
 	RegisterActivity.token = token;
 }
 
 }
