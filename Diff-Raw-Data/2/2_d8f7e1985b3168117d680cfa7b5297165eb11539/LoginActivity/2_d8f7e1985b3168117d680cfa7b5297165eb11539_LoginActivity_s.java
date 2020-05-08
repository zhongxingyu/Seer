 package com.axprint.official;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.axprint.official.XMLParsers.LoginXMLParser;
 import com.axprint.official.backgroundUtilites.XMLFetcher;
 
 public class LoginActivity extends Activity {
 	DefaultHttpClient httpclient = new DefaultHttpClient();
	static final String LOGIN_URL = "http://www.AxPrint.Com/GeneralControls/Service.asmx/Login";
 	static final String TAG = "UserName";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		Button button = (Button) findViewById(R.id.login_button);
 		button.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				LoginXMLParser parser = new LoginXMLParser();
 				EditText mail = (EditText) findViewById(R.id.login_email);
 				EditText pass = (EditText) findViewById(R.id.login_password);
 				List<NameValuePair> data = new ArrayList<NameValuePair>(2);
 				data.add(new BasicNameValuePair("email", mail.getText()
 						.toString()));
 				data.add(new BasicNameValuePair("password", pass.getText()
 						.toString()));
 				data.add(new BasicNameValuePair("remember", "false"));
 				data.add(new BasicNameValuePair("redirectUrl","/"));
 				XMLFetcher f = new XMLFetcher(parser, LOGIN_URL, data,
 						httpclient) {
 					@Override
 					protected void onPostExecute(Integer res) {
 						Log.d("MYTAP","In postexec");
 						Log.d("MYTAP","value is " + res.intValue());
 						switch (res.intValue()) {
 						case XMLFetcher.EXCEPTION:
 							Log.d("MYTAP","exception toast");
 							// XXX: broken fix!
 							Toast.makeText(LoginActivity.this, "Exception",
 									Toast.LENGTH_SHORT).show();
 							break;
 						case LoginXMLParser.LOGIN_ERROR:
 							Log.d("MYTAP","error tost");
 							Toast.makeText(LoginActivity.this,
 									R.string.login_error_message,
 									Toast.LENGTH_SHORT).show();
 						case LoginXMLParser.LOGIN_FAILED:
 							Log.d("MYTAP","failed toast");
 							Toast.makeText(LoginActivity.this,
 									R.string.login_failed_message,
 									Toast.LENGTH_SHORT).show();
 							break;
 						case LoginXMLParser.LOGIN_OK:
 							Log.d("MYTAP","LOGIN OK");
 							String MESSAGE = "Navid";
 							Intent nextActivity = new Intent(
 									LoginActivity.this, MainPageActivity.class);
 							nextActivity.putExtra(TAG, MESSAGE);
 							startActivity(nextActivity);
 						}
 					}
 				};
 				Log.d("MYTAP","execiting post request");
 				f.execute(XMLFetcher.METHOD_POST);
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_login, menu);
 		return true;
 	}
 
 }
