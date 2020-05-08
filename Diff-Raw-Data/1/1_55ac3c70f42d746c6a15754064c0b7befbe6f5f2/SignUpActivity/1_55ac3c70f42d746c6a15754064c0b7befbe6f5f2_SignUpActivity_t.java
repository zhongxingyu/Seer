 package com.example.android.beam;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.nfc.NfcAdapter;
 import android.os.Bundle;
 import android.os.Looper;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class SignUpActivity extends Activity {
 	Thread t;
 	private SharedPreferences mPreferences;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.signup);
         mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
         
         NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
         if (mNfcAdapter == null) {
         } else {
             mNfcAdapter.setNdefPushMessageCallback(null, this);
             mNfcAdapter.setOnNdefPushCompleteCallback(null, this);
         }
     }
     
     public void processSignUp(View view)
     {
     	t = new Thread() {
 			public void run() {
 				Looper.prepare();
 				if(signUp())
 				{
 			    	Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
 			    	startActivity(intent);
 				}
 				else
 				{
 					Toast.makeText(getApplicationContext(), "Sign up failed. Please try again.", Toast.LENGTH_LONG).show();
 				}
 				Looper.loop();
 			}
 		};
 		t.start();
     }
     
     public boolean signUp()
     {
 		try
 		{
 	    	EditText mEmailField = (EditText) findViewById(R.id.email);
 			EditText mPasswordField = (EditText) findViewById(R.id.password);
 			EditText mNameField = (EditText) findViewById(R.id.name);
 	 
 			String email = mEmailField.getText().toString();
 			String password = mPasswordField.getText().toString();
 			String name = mNameField.getText().toString();
 	 
 			DefaultHttpClient client = new DefaultHttpClient();
 			HttpPost post = new HttpPost(getString(R.string.api_base)+"/users");
 			
 			List<NameValuePair> params = new ArrayList<NameValuePair>(3);
 			params.add(new BasicNameValuePair("user[email]", email));
 			params.add(new BasicNameValuePair("user[password]", password));
 			params.add(new BasicNameValuePair("user[name]", name));
 			System.out.println(email);
 			System.out.println(password);
 			System.out.println(name);
 
 			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
 			post.setHeader("Accept", "application/json");
 			
 			String response = null;
 			ResponseHandler<String> responseHandler = new BasicResponseHandler();
 			response = client.execute(post, responseHandler);
 			
 			JSONObject jObject = new JSONObject(response);
 			JSONObject sessionObject = jObject.getJSONObject("user");
 			SharedPreferences.Editor editor = mPreferences.edit();
 			editor.putString("auth_token", sessionObject.getString("authentication_token"));
 			editor.putString("email", sessionObject.getString("email"));
 			editor.putString("name", sessionObject.getString("name"));
 			editor.putInt("balance", sessionObject.getInt("balance"));
			editor.commit();
 			
 			return true;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			return false;
 		}
     }
 }
