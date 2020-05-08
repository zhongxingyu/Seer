 package com.final_proj.flores;
 
 import java.util.LinkedList;
 import java.util.Random;
 
 import oauth.signpost.OAuthConsumer;
 
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.LightingColorFilter;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Songs extends FragmentActivity {
 	public static final String TAG = Songs.class.toString();
 
 	private TextView how;
 	private EditText tweet;
 	private TextView confirmText;
 	private OAuthConsumer mConsumer = null;
 	// stations
 	private Station i93;
 	private Station beat;
 	private Station now;
 	private Station k104;
 	private Station kiss;
 	private Station chosen;
 	// buttons
 	private Button mButton;
 	private Button callButton;
 	private TextView callhow;
 	private Boolean called;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.request_confirm);
 
 		// getting user's oauth creds
 		mConsumer = ((App) getApplication()).getOAuthConsumer();
 
 		called = false;
 		// creating station objects
 		populateStations();
 		Bundle extras = this.getIntent().getExtras();
 
 		String stationName = extras.getString("stationName");
 		String songName = extras.getString("songName");
 
 		// finding views
 		tweet = (EditText) this.findViewById(R.id.tweetfield);
 		how = (TextView) this.findViewById(R.id.how);
 		confirmText = (TextView) this.findViewById(R.id.confirm);
 		callhow = (TextView) this.findViewById(R.id.calltext);
 
 		// populates the tweetfield
 		populateRequestField(stationName, songName);
 
 		// Buttons for calling and tweeting
 		mButton = (Button) this.findViewById(R.id.button1);
 		mButton.setOnClickListener(new PostButtonClickListener());
 		mButton.getBackground().setColorFilter(
 				new LightingColorFilter(0xFFFFFFFF, 0xFFAA0000));
 		callButton = (Button) this.findViewById(R.id.callbutton);
 		callButton.setOnClickListener(new CallButtonClickListener());
 		callButton.getBackground().setColorFilter(
 				new LightingColorFilter(0xFFFFFFFF, 0xFFAA0000));
 
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		// check if user called
 		if (called == true) {
 			finish();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	// Station Object- used to store info
 	class Station extends Object {
 		public String name; // station's name
 		public String twitter; // station's twitter
 		public String phone; // station's phone number
 
 		// for station's that don't have a listed request line
 		public Station(String n, String t) {
 			name = n;
 			twitter = t;
 		}
 
 		public Station(String n, String t, String p) {
 			name = n;
 			twitter = t;
 			phone = p;
 		}
 	}
 
 	public void populateStations() {
 
 		i93 = new Station("i93 Hits", "@i93dallas", "2147871933");
 		beat = new Station("97.9 The Beat", "@979beat", "2147871945");
 		now = new Station("102.9 Now", "@102NOW", "2147871029");
 		k104 = new Station("K104", "@K104FM", "2147871104");
 		kiss = new Station("106.1 KISS FM", "@1061KISSFMDFW", "2147871061");
 	}
 
 	/*
 	 * populates request tweet with a station's name and a generic and random
 	 * greeting also: populating chosen with station
 	 */
 	public void populateRequestField(String sn, String song) {
 
 		String begin = " " + generateBegining() + " ";
 		if (sn.equals("i93 Hits")) {
			tweet.setText(i93.twitter + begin + song + " by Beyoncé");
 			chosen = i93;
 		} else if (sn.equals("97.9 The Beat")) {
			tweet.setText(beat.twitter + begin + song + " by Beyoncé");
 			chosen = beat;
 		} else if (sn.equals("102.9 Now")) {
			tweet.setText(now.twitter + begin + song + " by Beyoncé");
 			chosen = now;
 		} else if (sn.equals("K104")) {
			tweet.setText(k104.twitter + begin + song + " by Beyoncé");
 			chosen = k104;
 		} else if (sn.equals("106.1 KISS FM")) {
			tweet.setText(kiss.twitter + begin + song + " by Beyoncé");
 			chosen = kiss;
 		}
 
 	}
 
 	// randomly sends back a item from generic list of greetings
 	public String generateBegining() {
 		// pulling from strings.xml array
 		String[] b = this.getResources().getStringArray(R.array.requestB);
 		// returning random item
 		return b[new Random().nextInt(b.length)];
 	}
 
 	class PostButtonClickListener implements OnClickListener {
 		@Override
 		public void onClick(View v) {
 			String postString = tweet.getText().toString();
 			if (postString.length() == 0) {
 				Toast.makeText(Songs.this, getText(R.string.tweet_empty),
 						Toast.LENGTH_SHORT).show();
 			} else {
 				new PostTask().execute(postString);
 			}
 		}
 
 	}
 
 	class CallButtonClickListener implements OnClickListener {
 		@Override
 		public void onClick(View v) {
 			// changing the call boolean
 			called = true;
 			Intent callIntent = new Intent(Intent.ACTION_CALL);
 			callIntent.setData(Uri.parse("tel:" + chosen.phone));
 			startActivity(callIntent);
 		}
 	}
 
 	// These parameters are needed to talk to the messaging service
 	public HttpParams getParams() {
 		HttpParams params = new BasicHttpParams();
 		// set to false to avoid an Expectation Failed: error
 		HttpProtocolParams.setUseExpectContinue(params, false);
 		return params;
 	}
 
 	// This task posts a message to your message queue on the service.
 	class PostTask extends AsyncTask<String, Void, JSONObject> {
 
 		ProgressDialogFragment mDialog;
 		DefaultHttpClient mClient = new DefaultHttpClient();
 
 		@Override
 		protected void onPreExecute() {
 			mDialog = ProgressDialogFragment
 					.newInstance(R.string.tweet_progress_title,
 							R.string.tweet_progress_text);
 			mDialog.show(getSupportFragmentManager(), "auth");
 		}
 
 		@Override
 		protected JSONObject doInBackground(String... params) {
 
 			JSONObject jso = null;
 			try {
 
 				HttpPost post = new HttpPost(App.STATUSES_URL_STRING);
 				LinkedList<BasicNameValuePair> out = new LinkedList<BasicNameValuePair>();
 				out.add(new BasicNameValuePair("status", params[0]));
 				post.setEntity(new UrlEncodedFormEntity(out, HTTP.UTF_8));
 				post.setParams(getParams());
 				// sign the request to authenticate
 				mConsumer.sign(post);
 				String response = mClient.execute(post,
 						new BasicResponseHandler());
 				jso = new JSONObject(response);
 			} catch (Exception e) {
 				Log.e(TAG, "Post Task Exception", e);
 			}
 			return jso;
 		}
 
 		// This is in the UI thread, so we can mess with the UI
 		protected void onPostExecute(JSONObject jso) {
 			how.setVisibility(8);
 			tweet.setVisibility(8);
 			mButton.setVisibility(8);
 			confirmText.setVisibility(8);
 			callhow.setVisibility(0);
 			// dismisses dialog popup
 			mDialog.dismiss();
 
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.twitter:
 			Intent twitter = new Intent(Songs.this, GetAccess.class);
 			startActivity(twitter);
 			finish();
 			return true;
 		case R.id.logout:
 			SharedPreferences mSettings = PreferenceManager
 					.getDefaultSharedPreferences(this);
 			App.saveAuthInformation(mSettings, null, null);
 			Intent logout = new Intent(Songs.this, MainActivity.class);
 			startActivity(logout);
 			finish();
 			return true;
 		case R.id.request:
 			Intent request = new Intent(Songs.this, Radio.class);
 			startActivity(request);
 			finish();
 			return true;
 		case R.id.tumblr:
 			Intent tum = new Intent(Songs.this, TumblrViewer.class);
 			startActivity(tum);
 			finish();
 			return true;
 		case R.id.about:
 			Toast.makeText(getBaseContext(), "Developer: Heberth Flores",
 					Toast.LENGTH_SHORT).show();
 		default:
 			return false;
 		}
 	}
 }
