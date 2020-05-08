 package com.trydish.main;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 public class LoginHome extends Activity {
 	
 	boolean nocheck = true;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 				
 		setContentView(R.layout.activity_login_home);
 		
 		ActionBar actionBar = getActionBar();
 		actionBar.hide();
 	}
 	
 	public void loginCheck(View view) {
 		if (nocheck) {
 			try {
				checkLogin(new JSONObject("{\"status\": 1"));
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			EditText userText = (EditText)findViewById(R.id.login_username);
 			EditText passText = (EditText)findViewById(R.id.login_password);
 			ProgressBar progress = (ProgressBar)findViewById(R.id.login_progressbar);
 			
 			progress.setVisibility(View.VISIBLE);
 			LoginTask checkLogin = new LoginTask();
 			checkLogin.execute(userText.getText().toString(), passText.getText().toString());
 		}
 	}
 	
 	public void signupButton(View view) {
 		Intent intent = new Intent(this, SignupHome.class);
 		startActivity(intent);
 		overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
 	}
 	
 	@Override
 	public void onBackPressed() {
 	    super.onBackPressed();
 	    overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_right );
 	}
 	
 	
 	
 	private class LoginTask extends AsyncTask<String, Void, JSONObject> {
 
 		@Override
 		protected JSONObject doInBackground(String... params) {
 			String url = "http://trydish.pythonanywhere.com/login";
 			String responseString;
 			JSONObject result;
 
 			HttpClient httpclient = new DefaultHttpClient();
 			
 			HttpPost post = new HttpPost(url);
 			try {
 				List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 				postParameters.add(new BasicNameValuePair("username", params[0]));
 				postParameters.add(new BasicNameValuePair("password", params[1]));
 				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
 				post.setEntity(entity);
 	            HttpResponse response = httpclient.execute(post);
 	            
 	            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
 	                ByteArrayOutputStream out = new ByteArrayOutputStream();
 	                response.getEntity().writeTo(out);
 	                out.close();
 	                responseString = out.toString();
 	                result = new JSONObject(responseString);
 	            } else {
 	                //Closes the connection.
 	                response.getEntity().getContent().close();
 	                return null;
 	            }
 	        } catch (Exception e) {
 	        	return null;
 	        }
 			return result;
     	}
     
 		@Override
 		protected void onPostExecute(JSONObject login) {
 				checkLogin(login);
 		}
     	
     }
 	
 	private void checkLogin(JSONObject login) {
 		ProgressBar progress = (ProgressBar)findViewById(R.id.login_progressbar);
 		progress.setVisibility(View.INVISIBLE);
 		
 		try {
 			global.userID = login.getInt("status");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
 		if (global.userID != -1) {
 			Toast toast = Toast.makeText(this, "Thank you for logging in!", Toast.LENGTH_SHORT);
 			toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 400);
 			toast.show();
 			
 			Intent intent = new Intent(this, PostLoginHome.class);
 	    	startActivity(intent);
 	    	overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
 		} else {
 			Toast toast = Toast.makeText(this, "Username or password is incorrect.", Toast.LENGTH_LONG);
 			toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 400);
 			toast.show();
 			
 			EditText password = (EditText)findViewById(R.id.login_password);
 			password.setText("");
 		}
 	}
 }
