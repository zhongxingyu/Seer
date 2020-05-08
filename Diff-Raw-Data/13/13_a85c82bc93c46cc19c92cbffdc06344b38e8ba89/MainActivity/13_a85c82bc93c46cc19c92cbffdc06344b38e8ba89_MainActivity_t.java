 package com.emergency.codeblue;
 
 import org.apache.http.NameValuePair;
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.BufferedHttpEntity;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import com.google.android.gms.gcm.GoogleCloudMessaging;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.Settings.Secure;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.telephony.TelephonyManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 
 public class MainActivity extends Activity implements View.OnClickListener {
 
 	// Basic Login UI Initialization -----------------------------------
    private static final String DEFAULT_LOGIN = "gillian";
    private static final String DEFAULT_PASSWORD = "gillou";
 
     private Button loginButton;
     private Button registerButton;
     private EditText loginEdit;
     private EditText passwordEdit;
     JSONObject userInfo = new JSONObject();
     public String uuid;
 
     // -----------------------------------------------------------------
 
     
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
         // UI stuff
         loginEdit = (EditText) findViewById(R.id.loginEdit);
         passwordEdit = (EditText) findViewById(R.id.passwordEdit);
         
         SharedPreferences prefs = getPreferences(MODE_PRIVATE); 
         String restoredUserName = prefs.getString("LoginUser", DEFAULT_LOGIN);
         String restoredUserPW = prefs.getString("LoginPW", DEFAULT_PASSWORD);
 
         
         loginEdit.setText(restoredUserName);
         passwordEdit.setText(restoredUserPW);
         loginButton = (Button) findViewById(R.id.loginButton);
         registerButton = (Button) findViewById(R.id.registerButton);
         loginButton.setOnClickListener(this);
         registerButton.setOnClickListener(this);
      
 		//TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
         //uuid = tManager.getDeviceId();
         //System.out.println("------------>"+uuid);
 
 
  
 	}
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.splash, menu);
 		return true;
 	}
 
     @Override
     public boolean onOptionsItemSelected(MenuItem theMenu)
     {
     	if (theMenu.getItemId() == R.id.testing)
     	{
 			 Intent intent = new Intent(this, PhysicianActivity.class);
              startActivity(intent);
     	}
     	return false;
     }  
 
 	@Override
 	public void onClick(View v) {
         switch (v.getId()) {
             case R.id.loginButton:
 				 Intent intent = new Intent(this, Session.class);
 				 intent.putExtra("username",loginEdit.getText().toString());
 				 intent.putExtra("password",passwordEdit.getText().toString());
 
 	             startActivity(intent);
 	             finish();
                 break;
             case R.id.registerButton:
 				 Intent intent2 = new Intent(this, Session.class);
 	             startActivity(intent2);
                 break;
         }
 		
 	}
 	
 	
       
 	
 
 }
