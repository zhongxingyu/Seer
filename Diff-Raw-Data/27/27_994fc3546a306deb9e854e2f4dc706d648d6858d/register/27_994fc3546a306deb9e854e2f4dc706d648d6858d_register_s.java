 package com.designs_1393.connactiv;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.EditText;
 import android.graphics.Typeface;
 import android.widget.Button;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.util.Log;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.content.SharedPreferences;
 
 // authentication
 // TODO: prune
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.params.DefaultedHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.CoreProtocolPNames;
 import android.net.http.AndroidHttpClient;
 import java.io.ByteArrayOutputStream;
 
 // cookies
 import android.webkit.CookieSyncManager;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.client.CookieStore;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.protocol.BasicHttpContext;
 
 import android.content.Intent;
 import android.content.Context;
 
 import android.widget.Toast;
 
 // threading
 import android.os.AsyncTask;
 import java.lang.Void;
 import java.util.concurrent.locks.ReentrantLock;
 
 public class register extends Activity
 {
 	private EditText email, fName, lName, city, zip, phone, bio, pass, cPass;
 	private Button next;
 	private SharedPreferences prefs;
 	ProgressBar pb;
 
 	final String TAG = "ConnActiv";
 	Context ctx;
 	ReentrantLock regLock = new ReentrantLock();
 
 	String httpResponse = "";
 	BasicHttpContext httpCtx = new BasicHttpContext();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.register);
 		ctx = getApplicationContext();
 		pb = (ProgressBar)findViewById(R.id.login_progress);
 
 		email = (EditText)findViewById(R.id.reg_email);
 		email.setTypeface(Typeface.DEFAULT);
 		fName = (EditText)findViewById(R.id.reg_first_name);
 		fName.setTypeface(Typeface.DEFAULT);
 		lName = (EditText)findViewById(R.id.reg_last_name);
 		lName.setTypeface(Typeface.DEFAULT);
 		city = (EditText)findViewById(R.id.reg_city);
 		city.setTypeface(Typeface.DEFAULT);
 		zip = (EditText)findViewById(R.id.reg_zip);
 		zip.setTypeface(Typeface.DEFAULT);
 		phone = (EditText)findViewById(R.id.reg_phone_number);
 		phone.setTypeface(Typeface.DEFAULT);
 		bio = (EditText)findViewById(R.id.reg_bio);
 		bio.setTypeface(Typeface.DEFAULT);
 		pass = (EditText)findViewById(R.id.reg_password);
 		pass.setTypeface(Typeface.DEFAULT);
 		cPass = (EditText)findViewById(R.id.reg_password_confirm);
 		cPass.setTypeface(Typeface.DEFAULT);
 
 		next = (Button)findViewById(R.id.next_button);
 
 		next.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v)
 			{
 				if( regLock.tryLock() )
 				{
 					/*if(email.getText().toString().compareTo("") == 0 || pw.getText().toString().compareTo("") == 0)
 						Toast.makeText(ctx, "All fields are required", Toast.LENGTH_SHORT).show();
 					else
 					{*/
 						pb.setVisibility(View.VISIBLE); // show spinner
 						regTask task = new regTask();
 						task.execute(email.getText().toString(), fName.getText().toString(), lName.getText().toString(),
 							city.getText().toString(), zip.getText().toString(), phone.getText().toString(), 
 							bio.getText().toString(), pass.getText().toString(), cPass.getText().toString());
 
 						try {
 							regLock.unlock();
 						} catch (Exception e){
 							Log.i(TAG, "ERROR: " +e.toString());
 						}
 					//}
 					/* NOTE: it hides so quickly that it never actually
 					* appears, even on a 3G connection.  We protect against
 					* ANR's, so this can remain to provide a user confirmation
 					* that "the computer is thinking." */
 
 				}
 			}
 		});
 
 	}
 
 	private class regTask extends AsyncTask<String, Void, Integer>
 	{
 		protected Integer doInBackground(String... strings)
 		{
 			try {
				HttpPost post = new HttpPost("http://connactiv.com/test/android/register.php");
 				post.getParams().setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.FALSE);
 				final DefaultHttpClient client = new DefaultHttpClient(post.getParams());
 
 				List<NameValuePair> postParams = new ArrayList<NameValuePair>(9);
 				postParams.add(new BasicNameValuePair("username", strings[0]));
 				postParams.add(new BasicNameValuePair("firstName", strings[1]));
 				postParams.add(new BasicNameValuePair("lastName", strings[2]));
 				postParams.add(new BasicNameValuePair("city", strings[3]));
 				postParams.add(new BasicNameValuePair("zip", strings[4]));
 				postParams.add(new BasicNameValuePair("phone", strings[5]));
 				postParams.add(new BasicNameValuePair("interests", strings[6]));
 				postParams.add(new BasicNameValuePair("password", strings[7]));
 				postParams.add(new BasicNameValuePair("confirm", strings[8]));
 
 				post.setEntity( new UrlEncodedFormEntity(postParams) );
 				BasicResponseHandler brh = new BasicResponseHandler();
 				HttpResponse response = client.execute( post, httpCtx );
 				final String resp = brh.handleResponse( response );				
 
 				runOnUiThread( new Runnable() {
 					public void run() {
 						pb.setVisibility(View.INVISIBLE); // hide spinner
 						if( resp.contains("You did not fill"))
 							Toast.makeText(ctx, resp, Toast.LENGTH_SHORT).show();
 						else if(resp.contains("This email has already been registered"))
 							Toast.makeText(ctx, resp, Toast.LENGTH_SHORT).show();
 						else if(resp.contains("The passwords do not match, please re-enter your information"))
 							Toast.makeText(ctx, resp, Toast.LENGTH_SHORT).show();
 						else
 						{
 							prefs = getSharedPreferences("connactivPrefs", Activity.MODE_PRIVATE);
 							SharedPreferences.Editor editor = prefs.edit();
 							editor.putString("userId", resp);
 							editor.commit();
 
 							startActivity(new Intent(getApplicationContext(), stream.class));
 							Toast.makeText(getApplicationContext(), "Please subscribe to networks on the website at ConnActiv.com", Toast.LENGTH_SHORT).show();
 							finish();
 						}
 					}
 				});
 
 			} catch (Exception e) {
 				Log.i(TAG, "ERROR: " +e.toString());
 			}
 			return 0;
 		}
 	}
 
 }
