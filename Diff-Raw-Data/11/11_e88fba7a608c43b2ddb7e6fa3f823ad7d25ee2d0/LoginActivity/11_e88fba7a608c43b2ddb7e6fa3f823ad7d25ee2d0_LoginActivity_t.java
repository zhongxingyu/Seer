 package com.example.homeautomation;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 //import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class LoginActivity extends Activity{
 	
 	//private String user, pass;
 	private EditText username, password,ipAddress;
 	private String ip;
 	private SharedPreferences sharedPref;
 	private SharedPreferences.Editor editor;
 	private static final String tag = "LoginActivity";
     private CookieStore cookieStore = new BasicCookieStore();
     private HttpContext localContext = new BasicHttpContext();
     private HttpClient client = new DefaultHttpClient();
 	
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
         sharedPref = this.getPreferences(Context.MODE_PRIVATE);
         editor = sharedPref.edit();
 		username = (EditText)findViewById(R.id.username);
 		username.setText(sharedPref.getString("user",""));
         password = (EditText)findViewById(R.id.password);
         password.setText(sharedPref.getString("pass", ""));
         ipAddress = (EditText)findViewById(R.id.ipaddress);
         ipAddress.setText(sharedPref.getString("ip",""));
         localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
 	}
 	public void loginPress(View view) throws Exception {
 		// is this the right place in app flow to Save to the shared prefs?
		editor.putString("user", username.getText().toString().trim());
		editor.putString("pass", password.getText().toString().trim());
		editor.putString("ip",ipAddress.getText().toString().trim());
		ip = ipAddress.getText().toString().trim();
 		String[] info = new String[2];
 		info[0] = username.getText().toString();
 		info[1] = password.getText().toString();
 		sendPost(info);
     }
 	
 	private void sendPost(String[] info) throws Exception {
 		Log.i(tag,"Sending Post Request");
     	JSONObject jsonObject = new PostRequestTask().execute(info).get();
     	
     	if(jsonObject.get("message").equals("okay")){
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
 	
 	private class PostRequestTask extends AsyncTask<String, Void, JSONObject>{
     	protected JSONObject doInBackground(String...info){
    		String url =  "http://" + ip + "/auth/login/";
     		Log.i(tag,"url set");
     		HttpPost postRequest = null;
     		try{
         	postRequest = new HttpPost(url);
         	postRequest.setHeader("Accept","application/json");
         	postRequest.setHeader("Content-type","application/json");
     		}catch(Exception e){
     			Log.e(tag,e.toString());
     		}
         	Log.i(tag,"Post Request Created");
         	
         	JSONObject jsonObject = null;
         	
         	try{
         	JSONObject posted = new JSONObject();
         	posted.put("username",info[0]);
         	posted.put("password", info[1]);
         	posted.put("message","");
         	posted.put("status","");
         	Log.i(tag,"JSONObject: "+posted.toString());
         	postRequest.setEntity(new StringEntity(posted.toString()));
         	
         	HttpResponse response = client.execute(postRequest, localContext);
         	
         	Log.i(tag,"POST Request sent.");
         	Log.i(tag,"Post parameters: "+postRequest.getEntity());
         	Log.i(tag,"Response Code: "+response.getStatusLine().getStatusCode());
         	
         	BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
         	StringBuffer result = new StringBuffer();
         	String line = "";
         	while((line = reader.readLine())!=null)
         		result.append(line);
         	jsonObject = new JSONObject(result.toString());
         	}catch(Exception e){
         		Log.e(tag,e.toString());
         	}
             
             return jsonObject;            
     	}
     }	
 	
 }
