 package ru.romanov.schedule.src;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.HTTP;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import ru.romanov.schedule.utils.*;
 import ru.romanov.schedule.R;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.ContactsContract.RawContacts.Entity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebIconDatabase.IconListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class IScheduleActivity extends Activity {
 
 	private SharedPreferences mSharedPreferences;
 	private EditText loginEditText;
 	private EditText passEditText;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mSharedPreferences = getSharedPreferences(
 				StringConstants.SCHEDULE_SHARED_PREFERENCES, MODE_PRIVATE);
 		if (mSharedPreferences.getString(StringConstants.SHARED_LOGIN, null) == null
 				|| mSharedPreferences.getString(StringConstants.SHARED_PASS,
 						null) == null
 				|| mSharedPreferences.getString(StringConstants.TOKEN, null) == null) {
 			Editor editor = mSharedPreferences.edit();
 			editor.putString(StringConstants.SHARED_LOGIN, null);
 			editor.putString(StringConstants.SHARED_PASS, null);
 			editor.putString(StringConstants.TOKEN, null);
 			editor.commit();
 			setContentView(R.layout.main);
 		} else {
 			// NEXT ACTIVITY
 			//setContentView(R.layout.entering_layout);
 			Toast.makeText(this,
 					"Всё пучком! Следующая активити...", Toast.LENGTH_LONG).show();
 			startMainTabActivity();
 		}
 		//TODO: remove it
 
 	}
 
 	@Override
 	protected void onStart() {
 		this.loginEditText = (EditText) findViewById(R.id.loginEText);
 		this.passEditText = (EditText) findViewById(R.id.passEText);
 		Button loginButton = (Button) findViewById(R.id.logInButton);
 		loginButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				PostRequestAuthManager pram = new PostRequestAuthManager(
 						loginEditText.getText().toString(), passEditText
 								.getText().toString());
 				pram.execute(null);
 
 			}
 		});
 		super.onStart();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	private void startMainTabActivity() {
 		Intent intent = new Intent(this, MainTabActivity.class);
 		startActivity(intent);
 		finish();
 	}
 	
 	@SuppressWarnings("unused")
 	private class PostRequestAuthManager extends
 			AsyncTask<Void, Void, HttpResponse> {
 
 		private String login;
 		private String pass;
 		private String token;
 		private String name;
 		private String email;
 		private String phone;
 		private ProgressDialog dialog;
 		
 		public PostRequestAuthManager(String login, String pass) {
 			this.login = login;
 			this.pass = pass;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			dialog = ProgressDialog.show(IScheduleActivity.this, "", getString(R.string.loading), true);
 		}
 		@Override
 		protected HttpResponse doInBackground(Void... params) {
 			HttpClient client = new DefaultHttpClient();
 			String reqString = RequestStringsCreater.createAuthRequestSting(
 					login, pass);
 			HttpResponse responce = null;
 			try {
 				HttpPost request = new HttpPost(StringConstants.MY_URI);
 				StringEntity entity = new StringEntity(reqString, "UTF-8");
 				request.setHeader(HTTP.CONTENT_TYPE, "text/xml");
 				request.setEntity(entity);
 				responce = client.execute(request);
 
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return responce;
 		}
 
 		@Override
 		protected void onPostExecute(HttpResponse result) {
 			// TODO Auto-generated method stub
 			super.onPostExecute(result);
 			if (result != null) {
 				HttpEntity ent = result.getEntity();
 				try {
 					BufferedReader r = new BufferedReader(
 							new InputStreamReader(ent.getContent()));
 					StringBuilder total = new StringBuilder();
 					String line;
 					while ((line = r.readLine()) != null) {
 						total.append(line);
 					}
 					Map<String, String> resultMap = XMLParser.parseAuthResponse(total.toString());
 					if(resultMap.get(XMLParser.STATUS).equals(XMLParser.OK)) {
 						Toast.makeText(IScheduleActivity.this,
 								getString(R.string.auth_success), Toast.LENGTH_LONG).show();
 						this.token = resultMap.get(XMLParser.TOKEN);
 						this.name = resultMap.get(XMLParser.NAME);
 						this.phone = resultMap.get(XMLParser.PHONE);
 						this.email = resultMap.get(XMLParser.EMAIL);
 						saveSessionData();
 						startMainTabActivity();
 						
 					} else {
 						Toast.makeText(IScheduleActivity.this,
 								getString(R.string.login_error), Toast.LENGTH_LONG).show();
 					}
 				} catch (IllegalStateException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			} else {
 				Toast.makeText(IScheduleActivity.this, "Не получилось соединиться с серверм.", Toast.LENGTH_LONG).show();
 			}
 			dialog.dismiss();
 		}
 		
 		private void saveSessionData() {
 			Editor editor = IScheduleActivity.this.mSharedPreferences.edit();
 			editor.putString(StringConstants.SHARED_LOGIN, this.login);
 			editor.putString(StringConstants.SHARED_PASS, this.pass);
 			editor.putString(StringConstants.TOKEN, this.token);
 			editor.putString(StringConstants.SHARED_NAME, this.name);
			editor.putString(StringConstants.SHARED_PHONE, this.phone);
 			editor.putString(StringConstants.SHARED_EMAIL, this.email);
 			editor.commit();
 		}
 		
 
 	}
 }
