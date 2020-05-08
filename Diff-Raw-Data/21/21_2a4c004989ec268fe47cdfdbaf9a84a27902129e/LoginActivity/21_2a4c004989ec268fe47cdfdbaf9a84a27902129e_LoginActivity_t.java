 package org.rosedu.examplanner;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
 
 public class LoginActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_login);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_login, menu);
         return true;
     }
    
    public void userLogin(View v) {
    	EditText usernameField = (EditText) findViewById(R.id.editText1);
    	EditText passwordField = (EditText) findViewById(R.id.editText2);
    	
    	String username = usernameField.getText().toString();
    	String password = passwordField.getText().toString();
    	
    	SendData(username, password);
    }
    
    public void SendData(String username, String password) {
    	//TODO: Connect to server and send data
    	
    	Toast.makeText(this, "Username: " + username + "\n" 
    				+ "Password: " + password, Toast.LENGTH_LONG).show();
    }
 }
