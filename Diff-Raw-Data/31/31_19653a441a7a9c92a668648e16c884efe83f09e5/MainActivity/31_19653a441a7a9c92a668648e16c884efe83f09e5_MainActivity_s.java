 package com.dreamhack.dhcc;
 
 import android.R.string;
 
 import android.net.Uri;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.support.v4.app.NavUtils;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import oauth.signpost.OAuthConsumer;
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 import oauth.signpost.exception.OAuthCommunicationException;
 import oauth.signpost.exception.OAuthExpectationFailedException;
 import oauth.signpost.exception.OAuthMessageSignerException;
 
 import org.apache.http.HttpVersion;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.format.Time;
 import android.text.method.HideReturnsTransformationMethod;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnClickListener {
 	public static final String TAG = "DHCC";
 
 	
 	private CheckBox mCB;
 	private EditText mEditor;
 	private Button mButton;
 	private TextView mUser;
 	private TextView mLast;
 	
 	public static final String URL_CURRENT_USER = "http://api.crew.dreamhack.se/1/user/get.json";
 
 	ProgressDialog postDialog = null;
 
 	private OAuthConsumer mConsumer = null;
 	
 	public String mToken;
 	public String mSecret;
 	
 	SharedPreferences mSettings;
 	
 	
 	HttpClient mClient;
 	
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         //getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 		
 		HttpParams parameters = new BasicHttpParams();
 		HttpProtocolParams.setVersion(parameters, HttpVersion.HTTP_1_1);
 		HttpProtocolParams.setContentCharset(parameters, HTTP.DEFAULT_CONTENT_CHARSET);
 		HttpProtocolParams.setUseExpectContinue(parameters, false);
 		HttpConnectionParams.setTcpNoDelay(parameters, true);
 		HttpConnectionParams.setSocketBufferSize(parameters, 8192);
 		
 		SchemeRegistry schReg = new SchemeRegistry();
 		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 		ClientConnectionManager tsccm = new ThreadSafeClientConnManager(parameters, schReg);
 		mClient = new DefaultHttpClient(tsccm, parameters);
 		
 		
 		mCB = (CheckBox) this.findViewById(R.id.enable);
 		mCB.setChecked(false);
 		mEditor = (EditText) this.findViewById(R.id.editor);
 		mButton = (Button) this.findViewById(R.id.post);
 		mUser = (TextView) this.findViewById(R.id.user);
 		mLast = (TextView) this.findViewById(R.id.last);
 		mButton.setOnClickListener(this);
 		mCB.setOnClickListener(this);
 		mSettings = getSharedPreferences(OAuthHelper.PREFS, Context.MODE_PRIVATE);
 		mConsumer = new CommonsHttpOAuthConsumer(
 				Keys.DHCC_CONSUMER_KEY, 
 				Keys.DHCC_CONSUMER_SECRET);
 		
 		//mAA = new MyArrayAdapter(this, android.R.layout.two_line_list_item, android.R.id.text1, mHomeStatus);
 		//this.setListAdapter(mAA);
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		// We look for saved user keys
 		if(mSettings.contains(OAuthHelper.USER_TOKEN) && mSettings.contains(OAuthHelper.USER_SECRET)) {
 			Log.d(TAG,"testar");
 			mToken = mSettings.getString(OAuthHelper.USER_TOKEN, null);
 			mSecret = mSettings.getString(OAuthHelper.USER_SECRET, null);
 			if(!(mToken == null || mSecret == null)) {
 				mConsumer.setTokenWithSecret(mToken, mSecret);
 			}
 		}
 		new GetCredentialsTask().execute();
 	}
 	
 	protected void onFinish() {
 		mClient.getConnectionManager().shutdown();
 	}
 	
 	@Override
 	public void onClick(View v) {
 		if(mCB.equals(v)) {
 			if(mCB.isChecked()) {
 				Toast.makeText(this, "Dags att logga in...", Toast.LENGTH_SHORT).show();
 				Intent i = new Intent(this, OAuthHelper.class);
 				startActivity(i);
 			} else {
 				OAuthHelper.saveAuthInformation(mSettings, null, null);
 				mButton.setEnabled(false);
 				mEditor.setEnabled(false);
 				mCB.setChecked(false);
 				mUser.setText("");
 			}
 			mCB.setChecked(false); // the oauth callback will set it to the proper state
 		} else if(mButton.equals(v)) {
 			String postString = mEditor.getText().toString();
 			if (postString.length() == 0) {
 				Toast.makeText(this, "You have to enter something", Toast.LENGTH_SHORT).show();
 			} else {
 				new PostTask(this).execute(postString);
 			}
 		}
 	}
 	
 	
 
 	
 	//----------------------------
 	// This task is run on every onResume(), to make sure the current credentials are valid.
 	// This is probably overkill for a non-educational program
 	public class GetCredentialsTask extends AsyncTask<Void, Void, JSONObject> {
  
 		ProgressDialog authDialog;
  
 		@Override
 		protected void onPreExecute() {
 			authDialog = ProgressDialog.show(MainActivity.this, 
 				getText(R.string.auth_progress_title), 
 				getText(R.string.auth_progress_text), 
 				true,	// indeterminate duration
 				false); // not cancel-able
 		}
  
 		@Override
 		protected JSONObject doInBackground(Void... arg0) {
 			JSONObject jso = null;
 	    	HttpGet get = new HttpGet(URL_CURRENT_USER);
 	    	try {
 				mConsumer.sign(get);
 				String response = mClient.execute(get, new BasicResponseHandler());
 				jso = new JSONObject(response);
 				Log.d(TAG, "authenticatedQuery: " + jso.toString(2));
 			} catch (OAuthMessageSignerException e) {
 				e.printStackTrace();
 			} catch (OAuthExpectationFailedException e) {
 				e.printStackTrace();
 			} catch (OAuthCommunicationException e) {
 				e.printStackTrace();
 			} catch (JSONException e) {
 				e.printStackTrace();
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return jso;
 		}
  
 		// This is in the UI thread, so we can mess with the UI
 		protected void onPostExecute(JSONObject jso) {
 			authDialog.dismiss();
 			
 			if ( jso.has("error") ) {
 				mUser.setText(jso.optString("error", getString(R.string.bad_value)));
 				mLast.setText("Nooo");
 				
 				mButton.setEnabled(false);
 				mEditor.setEnabled(false);
 				mCB.setChecked(false);
 			} else {
 				mCB.setChecked(true);
 				mButton.setEnabled(true);
 				mEditor.setEnabled(true);
 				mUser.setText(getUserName(jso));
 				mLast.setText("");
 			}
 			/*if(jso != null) {
 				TimelineSelector ss = new TimelineSelector(HOME_TIMELINE_URL_STRING);
 				new GetTimelineTask().execute(ss);
 			}*/
 		}
 	}
 	
 	private class PostTask extends AsyncTask<String, Void, JSONObject> {
 		
 			
 		ProgressDialog postDialog;
 		private Activity activity;
  
 		PostTask(Activity activity){
 			this.activity = activity;
 		}
 		
 		@Override
 		protected void onPreExecute() {
 			postDialog = ProgressDialog.show(MainActivity.this, 
 					getText(R.string.search_progress_title), 
 					getText(R.string.search_progress_text), 
 					true,	// indeterminate duration
 					false); // not cancel-able
 		}
  
 		@Override
 		protected JSONObject doInBackground(String... params) {
  
 			JSONObject jso = null;
 			try {
 				String query = URLEncoder.encode(mEditor.getText().toString(), "utf-8");
 				HttpPost post = new HttpPost("http://api.crew.dreamhack.se/1/user/search/"+query+".json");
 				
 				LinkedList<BasicNameValuePair> out = new LinkedList<BasicNameValuePair>();
 				out.add(new BasicNameValuePair("status", params[0]));
 				post.setEntity(new UrlEncodedFormEntity(out, HTTP.UTF_8));
 				post.setParams(getParams());
 				// sign the request to authenticate
 				mConsumer.sign(post);
 				String response = mClient.execute(post, new BasicResponseHandler());
 				jso = new JSONObject(response);
 				
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			} catch (OAuthMessageSignerException e) {
 				e.printStackTrace();
 			} catch (OAuthExpectationFailedException e) {
 				e.printStackTrace();
 			} catch (OAuthCommunicationException e) {
 				e.printStackTrace();
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (JSONException e) {
 				e.printStackTrace();
 			} finally {
  
 			}
 			return jso;
 		}
  
 		// This is in the UI thread, so we can mess with the UI
 		protected void onPostExecute(JSONObject jso) {
 			postDialog.dismiss();
 
 			if(jso != null) { // authorization succeeded, the json object contains the user information
 						
 				mLast.setText("");
 
 				// Prepare an Array list of todo items
 				ArrayList<String> listTODO = new ArrayList<String>();
 				try {
 					JSONArray resultList = jso.getJSONArray("result");
 					/*
 					for(int i = 0 ; i<resultList.length();i++){
 						listTODO.add(resultList.getJSONObject(i).getString("username"));
 					}
 					*/
 					List<Map<String, String>> data = new ArrayList<Map<String, String>>();
 					for(int i = 0 ; i<resultList.length();i++){
 
 						JSONObject row = resultList.getJSONObject(i);
 					    Map<String, String> datum = new HashMap<String, String>(2);
 					    datum.put("username", row.getString("username"));
 					    datum.put("fullname", row.getString("firstname")+" "+row.getString("lastname"));
 					    datum.put("uid", row.getString("uid"));
 					    datum.put("picture", row.optString("badge_picture","x"));
 
 
 					    data.add(datum);
 					}
 					
 					final ListView lv = (ListView) findViewById(R.id.list);
 					
 					
 					OnItemClickListener av = new OnItemClickListener() {
 						@Override
 						 public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
 							
 						      HashMap<String ,String > listItem = (HashMap<String, String>) lv.getItemAtPosition(position);
 						      Log.d(TAG,listItem.toString());
 
 						      	//Toast.makeText(getApplicationContext(), "selected Item Name is "+listItem.toString(), Toast.LENGTH_LONG).show();
 								Intent i = new Intent(getApplicationContext(), ShowUserActivity.class);
 							
 								
 								TextView v = (TextView) view.findViewById(android.R.id.text1);
 								TextView v2 = (TextView) view.findViewById(android.R.id.text2);
 								
 								Log.d(TAG,v.getText().toString());
 								Log.d(TAG,v2.getText().toString());
 								Log.d(TAG,listItem.get("uid"));
 
 								i.putExtra("user", listItem);
 	
 								startActivity(i);
 
 						   } 
 					};
 					
 					lv.setOnItemClickListener(av);
 													
 					lv.setAdapter(new SimpleAdapter(activity,data,
 							android.R.layout.simple_list_item_2,
 							new String[] {"username", "fullname","uid","picture"},
 							new int[] { android.R.id.text1, android.R.id.text2}
 							));
 								
 					if ( resultList.length() > 1 ) {
 						mLast.setText("Found "+resultList.length()+" users");
 					} else {
 						mLast.setText("Found "+resultList.length()+" user");
 					}
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 						
 				
 				//Hide the softkeyboard (the easy way??? )
 				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 				imm.hideSoftInputFromWindow(mEditor.getWindowToken(), 0);
 			
 			} else {
 				mLast.setText(getText(R.string.tweet_error));
 			}
 		}
 	}
 	
 	
 	public HttpParams getParams() {
 		// Tweak further as needed for your app
 		HttpParams params = new BasicHttpParams();
 		// set this to false, or else you'll get an Expectation Failed: error
 		HttpProtocolParams.setUseExpectContinue(params, false);
 		return params;
 	}
 	
 	// Get stuff from the two types of Twitter JSONObject we deal with: credentials and status
 	private String getCurrentTweet(String status) {
 		return status;
 	}
 	
 	private String getUserName(JSONObject credentials) {
 		if ( credentials.has("error") ) {
 			return credentials.optString("error", getString(R.string.bad_value));
 		} else
 			return credentials.optString("username", getString(R.string.bad_value));
 	}
 	
 	private String getLastTweet(JSONObject credentials) {
 		try {
 			String status = credentials.getString("lastname");
 			return getCurrentTweet(status);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return getString(R.string.tweet_error);
 		}
 	}
 }
