 package edu.ucsb.cs290.friendappmatcher;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 import com.facebook.android.Util;
 
 public class UploadApps extends Activity {
 	Facebook facebook = new Facebook("458513954190761");
 	AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
 	private static final String URL_STRING = "http://ec2-107-20-28-194.compute-1.amazonaws.com/";
 	private ListView l;
 	private List<String> appNames;
 	private SharedPreferences mPrefs;
 	private boolean testing = false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_upload_apps);
 		mPrefs = getPreferences(MODE_PRIVATE);
 		String access_token = mPrefs.getString("access_token", null);
 		long expires = mPrefs.getLong("access_expires", 0);
 		if (access_token != null) {
 			facebook.setAccessToken(access_token);
 		}
 		if (expires != 0) {
 			facebook.setAccessExpires(expires);
 		}
 		/*
 		 * Only call authorize if the access_token has expired.
 		 */
 		if (!facebook.isSessionValid()) {
 
 			facebook.authorize(this, new String[] {}, new DialogListener() {
 				@Override
 				public void onComplete(Bundle values) {
 					SharedPreferences.Editor editor = mPrefs.edit();
 					editor.putString("access_token", facebook.getAccessToken());
 					editor.putLong("access_expires",
 							facebook.getAccessExpires());
 					editor.commit();
 				}
 
 				@Override
 				public void onFacebookError(FacebookError error) {
 					Log.w("fama", "Failed authing to facebook", error);
 					if(error.getMessage().startsWith("Android key mismatch. Your key"));
 						testing = true;
 				}
 
 				@Override
 				public void onError(DialogError e) {
 				}
 
 				@Override
 				public void onCancel() {
 				}
 			});
 		}
 		PackageManager pm = getPackageManager();
 		List<ApplicationInfo> packages = pm
 				.getInstalledApplications(PackageManager.GET_META_DATA);
 		appNames = new ArrayList<String>(packages.size());
 		for (ApplicationInfo applicationInfo : packages) {
 			if (!applicationInfo.packageName.startsWith("com.android")) {
 				appNames.add(applicationInfo.packageName);
 			}
 		}
 		l = (ListView) findViewById(R.id.listView1);
 		l.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 		l.setAdapter(new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_multiple_choice, appNames));
 	}
 
 	public void submit(View v) {
 		SparseBooleanArray s = l.getCheckedItemPositions();
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < s.size(); i++) {
 			if (s.get(i)) {
 				sb.append(appNames.get(i)).append("\n");
 			}
 		}
 		final Activity thiscontext = this;
 		new AsyncTask<String, Void, HttpResponse>() {
 
 			@Override
 			protected HttpResponse doInBackground(String... params) {
 				try {
 					final List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 					nameValuePairs
 							.add(new BasicNameValuePair("apps", params[0]));
 
 					String userId = "charlesmunger";
 					if(!testing) {
 						JSONObject json = Util.parseJson(facebook.request("me"));
 						userId = json.getString("id");
 					}
 					
					nameValuePairs.add(new BasicNameValuePair("user", userId));
 					HttpPost httppost = new HttpPost(URL_STRING);
 					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
 							HTTP.UTF_8));
 					HttpClient httpclient = new DefaultHttpClient();
 					return httpclient.execute(httppost);
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				return null;
 			}
 
 			protected void onPostExecute(HttpResponse r) {
 				if(r == null) {
 					Log.e("fama", "Failed authing to facebook");
 					Toast.makeText(thiscontext, "Failed authenticating to Facebook", Toast.LENGTH_SHORT);
 					thiscontext.finish();
 					return;
 				}
 				Log.d("fama", r.toString());
 				Log.v("fama", Arrays.deepToString(r.getAllHeaders()));
 				Toast.makeText(thiscontext, "Posted apps!", Toast.LENGTH_SHORT)
 						.show();
 				thiscontext.finish();
 			}
 
 		}.execute(sb.toString());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_upload_apps, menu);
 		return true;
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		facebook.authorizeCallback(requestCode, resultCode, data);
 	}
 
 	public void onResume() {
 		super.onResume();
 		facebook.extendAccessTokenIfNeeded(this, null);
 	}
 }
