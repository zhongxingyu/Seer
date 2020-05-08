 package patidar.sagar.ideablock;
 
 import java.sql.Date;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Login extends Activity {
 
 	private EditText inputUsername;
 	private EditText inputPassword;
 	
 	private Button loginButton;
 	
 	private TextView forgotID ;
 	private TextView forgotPass ;
 	
 	private static ProgressDialog pDialog;
 	private static HttpClient httpClient;
 	private static HttpPost httppost;
 	private Intent intent ;
 	
 	private static JSONObject jObj ;
 	
 	public static ArrayList<HashMap<String, String>> arrlistContacts = new ArrayList<HashMap<String,String>>();
 	public static ArrayList<HashMap<String, String>> arrlistTransactions = new ArrayList<HashMap<String,String>>();
 	public static ArrayList<HashMap<String, String>> arrlistNotes = new ArrayList<HashMap<String,String>>();
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		
 		inputUsername = (EditText) findViewById(R.id.loginUsername);
 		inputPassword = (EditText) findViewById(R.id.loginPassword);
 		loginButton = (Button) findViewById(R.id.loginbutton);
 		forgotID = (TextView) findViewById(R.id.loginForgotEmail);
 		forgotPass = (TextView) findViewById(R.id.loginForgotPassword);
 		
 		Constants.setTextViewFontStyle(getAssets(), this.forgotID,this.forgotPass);
 		Constants.setButtonFontStyle(getAssets(), this.loginButton);
 		Constants.setEditTextFontStyle(getAssets(), inputPassword,inputUsername);
 		
 		loginButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				new signIn().execute();
 			}
 		});
 		
 	}
	
 	class signIn extends AsyncTask<String, String, String>{
 
 	  	@Override
     	protected void onPreExecute(){
     		super.onPreExecute();
     		pDialog = new ProgressDialog(Login.this);
     		pDialog.setMessage("Verifying Details...");
     		pDialog.setIndeterminate(false);
     		pDialog.setCancelable(false);
     		pDialog.show();
     	}
     		
 		@Override
 		protected String doInBackground(String... params) {
 			String username = inputUsername.getText().toString();
 			String passwd = inputPassword.getText().toString();
 			List<NameValuePair> param = new ArrayList<NameValuePair>();
 			param.add(new BasicNameValuePair("loginUsername", username));
 			param.add(new BasicNameValuePair("passwd", passwd));
 			
 			
 			try{
 				httpClient = new DefaultHttpClient();
 				httppost = new HttpPost(Constants.URL_SERVER_PROJECT+"login_get_all.php");
 				httppost.setEntity(new UrlEncodedFormEntity(param));
 				//httpResponse = httpClient.execute(httppost);
 				
 				
 				ResponseHandler<String> responseHandler = new BasicResponseHandler();
 				final String httpResponse = httpClient.execute(httppost, responseHandler);
 				
 				Log.d("OUTPUT", httpResponse);
 				jObj = new JSONObject(httpResponse);
 				int success = jObj.getInt("success");
 				String message = jObj.getString("message");
 				Log.d("LOGIN STATUS", message);
 				final JSONObject contacts = jObj.getJSONObject("contacts_detail");
 				final JSONObject user_detail = jObj.getJSONObject("user_detail");
 				final JSONObject notes_detail = jObj.getJSONObject("notes_detail");
 				final JSONObject trans_detail = jObj.getJSONObject("trans_detail");
 				
 				intent = new Intent(Login.this,Home.class);
 				intent.putExtra("user_name", user_detail.getString("user_name"));
 				intent.putExtra("user_id", user_detail.getString("user_id"));
 				intent.putExtra("user_balance", user_detail.getString("user_balance"));
 				
 				if(success==1){
 				
 				runOnUiThread(new Runnable() {
 					public void run() {
 						try {
 							
 							JSONArray arr = contacts.getJSONArray("contacts");
 							  int length = arr.length();
 							  if(length>0){
 								  for(int i=0;i<length;i++){
 									  HashMap<String, String> hMap = new HashMap<String, String>();
 									  JSONObject row = arr.getJSONObject(i);
 									  hMap.put(Contacts.CONTACT_NAME, row.getString("name"));
 									  hMap.put(Contacts.CONTACT_ID, row.getString("id"));
 									  arrlistContacts.add(hMap);
 								  }
 							  }
 
 						} catch (JSONException e) {
 							e.printStackTrace();
 							Log.d("ERROR Login.php", "IN CONTACTS THREAD");
 						}
 					}
 				});
 				
 				runOnUiThread(new Runnable() {
 					public void run() {
 						try {
 							
 							JSONArray transactions = trans_detail.getJSONArray("trans");
 							  int length = transactions.length();
 							  if(length>0){
 									for(int i=0;i<length;i++){
 										JSONObject row  = transactions.getJSONObject(i);
 										HashMap<String, String> hMap = new HashMap<String, String>();
 										Timestamp time = new Timestamp(row.getInt("timestamp")); 
 										Date date = new Date(time.getTime());
 										int dt = date.getDate();
 										Log.d("Date", dt+" "+row.getString("month")+", "+row.getString("year"));
 										hMap.put(Transactions.PAYMENT_AMOUNT, row.getString("amount"));
 										hMap.put(Transactions.PAYMENT_DATE, row.getString("date"));
 										hMap.put(Transactions.PAYMENT_TIME, row.getString("time"));
 										hMap.put(Transactions.PAYMENT_MONTH, row.getString("month"));
 										hMap.put(Transactions.PAYMENT_YEAR, row.getString("year"));
 										
 										String id1 = row.getString("from_id");
 										String id2 = row.getString("to_id");
 										if(id1.equals(user_detail.getString("user_id"))){
 											hMap.put(Transactions.PAYMENT_ID, id2);
 										}
 										else{
 											hMap.put(Transactions.PAYMENT_ID, id1);
 										}
 										arrlistTransactions.add(hMap);
 									}
 							  }
 
 						} catch (JSONException e) {
 							e.printStackTrace();
 							Log.d("ERROR Login.php", "IN TRANSACTION THREAD");
 						}
 					}
 				});
 
 				runOnUiThread(new Runnable() {
 					public void run() {
 						try {
 							
 							JSONArray notes = notes_detail.getJSONArray("notes");
 							  int length = notes.length();
 							  if(length>0){
 									for(int i=0;i<length;i++){
 										JSONObject row  = notes.getJSONObject(i);
 										HashMap<String, String> hMap = new HashMap<String, String>();
 										hMap.put(Notes.NOTE_SUBJECT, row.getString("subject"));
 										hMap.put(Notes.NOTE_MESSAGE, row.getString("note"));
 										hMap.put(Notes.NOTE_TIME, row.getString("time"));
 										hMap.put(Notes.NOTE_DATE, row.getString("date"));
 										arrlistNotes.add(hMap);
 									}
 							  }
 
 						} catch (JSONException e) {
 							e.printStackTrace();
 							Log.d("ERROR Login.php", "IN NOTES THREAD");
 						}
 					}
 				});				
 				
 				
 				
 				}
 				else{
 					Log.d("LOGIN UNSUCCESSFUL", "FORGOT ID ? | FORGOT PASSWORD");
 					
 				}
 
 			}
 			catch(Exception e){
 				e.printStackTrace();
 				Log.d("LOGIN.PHP ERROR", "SOME ERROR OCCURRED");
 			}
 			
 			
 			return null;
 		}
 		
 	   /**
          * After completing background task Dismiss the progress dialog
          * **/
         protected void onPostExecute(String file_url) {
             // dismiss the dialog once done
             pDialog.dismiss();
 			startActivity(intent);
         }
     	
 
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_login, menu);
 		
 		return true;
 	}
 	
 	
 
 }
