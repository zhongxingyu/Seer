 package com.example.homeautomation;
 
 import org.json.JSONObject;
 
 import com.example.homeautomation.utility.PostRequestTask;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class LoginActivity extends TemplateActivity {
 	
 	private EditText username, password,ipAddress;
 	
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		
 		/** 
 		 * Tags must be set for each superclass.
 		 */
 		
 		tag = "LoginActivity";
 		initializeVariables(); 
 		setContentView(R.layout.activity_login);
 		username = (EditText)findViewById(R.id.username);
 		username.setText(sharedPref.getString("user",""));
         password = (EditText)findViewById(R.id.password);
         password.setText(sharedPref.getString("pass", ""));
         ipAddress = (EditText)findViewById(R.id.ipaddress);
         ipAddress.setText(sharedPref.getString("ip",""));
         test = "Modified in Login";
 	}
 	
 	//Button was pressed -- action listener.
 	public void loginPress(View view) throws Exception {
 		editor.putString("user", username.getText().toString().trim());
 		editor.putString("pass", password.getText().toString().trim());
 		editor.putString("ip",ipAddress.getText().toString().trim());
 		
 		/* Sets the shared ipAddress */
 		ip = ipAddress.getText().toString().trim();
 		ip = ip.trim();
 		Toast toast = Toast.makeText(getApplicationContext(), ip, Toast.LENGTH_SHORT);
 		toast.show();
		ip = "http://" + ip;
 		
 		String[] info = new String[2];
 		info[0] = username.getText().toString();
 		info[1] = password.getText().toString();
 		sendPost(info);
     }
 	
 	private void sendPost(String[] info) throws Exception {
 		Log.i(tag,"Sending Post Request");
 		
     	JSONObject jsonObject = new PostRequestTask(tag, ip, client, localContext).execute(info).get();
     	
     	/**
     	 *  Calls the next activity. Need to make sure information is passed???
     	 */
     	
     	if(jsonObject.get("message").equals("okay")){
     		editor.commit();
     		Intent intent = new Intent(this, MainActivity.class);
             startActivity(intent);
             finish();
     	}
     	else if(jsonObject.get("message").equals("fail")){
     		Context context = getApplicationContext();
     		CharSequence text = "Login failed.";
     		int duration = Toast.LENGTH_SHORT;
     		
     		Toast toast = Toast.makeText(context, text, duration);
     		toast.show();
     	}
     	else{
     		System.out.println("JSONObject Format Error");
     	}
 	}	
 }
