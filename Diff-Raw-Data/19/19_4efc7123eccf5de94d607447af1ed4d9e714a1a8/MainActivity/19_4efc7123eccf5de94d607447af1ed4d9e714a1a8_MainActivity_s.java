 /*
  * project		Twittest
  * package		com.recarlin.twittest
  * author		Russell Carlin
  * date			Apr 22, 2013
  */
 package com.recarlin.twittest;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import TweetBuilder.AddNReadTweets;
 import TweetBuilder.FileSystemActions;
 import TweetBuilder.SendRequest;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.text.InputType;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	Boolean connected = false;
 	EditText nameEntry;
 	EditText tweetEntry;
 	LinearLayout lay;
 	ScrollView scroll;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//Checks to see if there is a stored zip code on the file system. If so, it loads the info for that zip code.
 		try{
 			String myZip = FileSystemActions.readFile(MainActivity.this, "zip", false);
			getTimelineURL(myZip);
 		} catch(Exception e) {
 			Log.e("STORED ZIP", "There is no stored zip!");
 		}
 		//Sets the linear layout up.
 		lay = new LinearLayout(this);
 		lay.setOrientation(LinearLayout.VERTICAL);
 		LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
 		lay.setLayoutParams(par);
 		//Sets up a title text view.
 		TextView title = new TextView(this);
 		title.setText("3-Day Forecast");
 		title.setTextSize(40);
 		title.setGravity(Gravity.CENTER);
 		
 		LinearLayout.LayoutParams par2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
 		//Button that will send the request, as long as you are connected to the Internet. If you aren't, you get an error.
 		Button saveTweet = new Button(this);
 		saveTweet.setText("Get Forecast");
 		saveTweet.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				connected = SendRequest.getConnected(MainActivity.this);
 				if(connected) {
 					String username = tweetEntry.getText().toString();
 					getTimelineURL(username);
 					FileSystemActions.storeFile(MainActivity.this, "zip", tweetEntry.getText().toString(), false);
 				} else {
 					Log.i("CONNECTION", "You are not connected to the Internet!");
 				}
 			}
 		});
 		//Sets up a EditText and TextView for users to type their zip code.
 		LinearLayout tweet = new LinearLayout(this);
 		tweet.setOrientation(LinearLayout.HORIZONTAL);
 		tweet.setLayoutParams(par2);
 		par2 = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
 		tweetEntry = new EditText(this);
 		tweetEntry.setInputType(InputType.TYPE_CLASS_NUMBER);
 		tweetEntry.setHint("Enter Zip Code");
 		tweetEntry.setLayoutParams(par2);
 		tweet.addView(tweetEntry);
 		tweet.addView(saveTweet);
 		
 		scroll = new ScrollView(this);
 		scroll.setLayoutParams(par);
 		
 		//Here we add all of the views to the main view.
 		lay.addView(title);
 		lay.addView(tweet);
 		lay.addView(scroll);
 		setContentView(lay);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	//This builds a URL for the request, based on what you pass into it from the EditText, and executes the getTimeline instance.
 	private void getTimelineURL(String username) {
 		try {
 			URL userTimelineURL = new URL("http://api.wunderground.com/api/137996d2b3a91dcf/forecast/q/" + username + ".json");
 			getTimeline gtl = new getTimeline();
 			gtl.execute(userTimelineURL);
 		} catch(MalformedURLException e) {
 			Log.e("MALFORMED URL", "URL is incorrect!");
 		}
 	}
 	//Performs the URL request, reads the data, puts it into a TextView, and adds it to the main view.
 	private class getTimeline extends AsyncTask<URL, Void, String>{
 		@Override
 		protected String doInBackground(URL...urls) {
 			String response = "";
 			for (URL url: urls) {
 				response = SendRequest.getResponse(url);
 			}
 			return response;
 		}
 		
 		@Override
 		protected void onPostExecute(String result) {
 			try {
 				JSONObject resultJSON = new JSONObject(result);
 				JSONArray results = resultJSON.getJSONObject("forecast").getJSONObject("txt_forecast").getJSONArray("forecastday");
 				String post = AddNReadTweets.ReadDefaults(results);
 				TextView resultsTextView = AddNReadTweets.NewTweetView(post, MainActivity.this);
 				scroll.addView(resultsTextView);
 			} catch(JSONException e) {
 				Log.e("JSON ERROR", "Your JSON is incorrect!");
 			}
 			
 		}
 	}
 }
