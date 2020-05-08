 package com.example.strangers.controler;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 
 import com.example.strangers.R;
 import com.example.strangers.model.User;
 import com.example.strangers.utilities.ObjectAndString;
 
 public class Login extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.login_menu_layout, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		/*case android.R.id.home:
 			NavUtils.navigateUpFromSameTask(this);
 			return true;*/
 		case R.id.newAccount:
 			Intent intent = new Intent(this, Inscription.class);
 	    	startActivity(intent);
 	    	return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	//Action du bouton de connexion
     public void login(View v) {
     	
     	EditText loginInput = (EditText) findViewById(R.id.loginInput);
 		EditText passwordInput = (EditText) findViewById(R.id.passwordInput);
 		
 		//create user
 		String login = loginInput.getText().toString();
 		String password = passwordInput.getText().toString();
 		User user = new User(1, login, password);
 		
 		//TODO Check user existance in database and load accounts
 		
 		//store user
 		SharedPreferences stockPreferences = getSharedPreferences("strangers", Activity.MODE_PRIVATE);
 
 		Editor editor = stockPreferences.edit();
 		editor.putString("registeredUser", ObjectAndString.objectToString(user));
 		editor.apply();
 		
 		//switch to main activity
 		Bundle bundle = new Bundle();
 		bundle.putParcelable("com.example.strangers.model.User", user);
 		Intent intent = new Intent(this, NumberSearch.class);
 		intent.putExtra("currentUserBundle", bundle);
     	startActivity(intent);
 		
     }
 
 }
