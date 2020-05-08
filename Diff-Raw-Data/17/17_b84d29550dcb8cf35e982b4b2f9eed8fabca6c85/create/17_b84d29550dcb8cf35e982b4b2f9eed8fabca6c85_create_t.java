 package com.lghs.stutor;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class create extends Activity {
 
 	// Progress Dialog
 	private ProgressDialog pDialog;
 
 	JSONParser jsonParser = new JSONParser();
 	EditText inputusername, inputpassword, inputemail;
 
	// URL to create new Account
 	private static String url_create_account = "http://devpump.dyndns.org/stutor/create_account.php";
 
 	// JSON Node names
 	private static final String TAG_SUCCESS = "success";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_create);
 
 		// Edit Text
 		inputusername = (EditText) findViewById(R.id.create_edittext_username);
 		inputpassword = (EditText) findViewById(R.id.create_edittext_password);
 		inputemail = (EditText) findViewById(R.id.create_edittext_email);
 	}
 
 	public void onBackPressed()
 	{
 		Toast msg = Toast.makeText(this, "GO BACK!", 3);
 		msg.show();
 	}
 	
 	public void create_button_create_click(View v) {
 		new CreateNewAccount().execute();
 	}
 
 	class CreateNewAccount extends AsyncTask<String, String, String> {
 
 		/**
 		 * Before starting background thread Show Progress Dialog
 		 * */
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			pDialog = new ProgressDialog(create.this);
 			pDialog.setMessage("Creating Account..");
 			pDialog.setIndeterminate(false);
 			pDialog.setCancelable(true);
 			pDialog.show();
 		}
 
 		/**
 		 * Creating Account
 		 * */
 		protected String doInBackground(String... args) {
 			String username = inputusername.getText().toString();
 			String password = inputpassword.getText().toString();
 			String email = inputemail.getText().toString();
 
 			// Building Parameters
 			List<NameValuePair> params = new ArrayList<NameValuePair>();
 			params.add(new BasicNameValuePair("username", username));
 			params.add(new BasicNameValuePair("password", password));
 			params.add(new BasicNameValuePair("email", email));
 
			// Getting JSON Object
 			// Note that create Account url accepts POST method
 			JSONObject json = jsonParser.makeHttpRequest(url_create_account,
 					"POST", params);
 
 			// Check log cat for response
 			Log.d("Create Response", json.toString());
 
			// Check for success tag
 			try {
 				int success = json.getInt(TAG_SUCCESS);
 
 				if (success == 1) {
					// Successfully created Account
 					Intent i = new Intent(getApplicationContext(), login.class);
 					startActivity(i);
 
					// Closing this screen
 					finish();
 				} else {
					// Failed to create Account
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 
 			return null;
 		}
 
 		/**
 		 * After completing background task Dismiss the progress dialog
 		 * **/
 		protected void onPostExecute(String file_url) {
 			// dismiss the dialog once done
 			pDialog.dismiss();
 		}
 
 	}
 }
