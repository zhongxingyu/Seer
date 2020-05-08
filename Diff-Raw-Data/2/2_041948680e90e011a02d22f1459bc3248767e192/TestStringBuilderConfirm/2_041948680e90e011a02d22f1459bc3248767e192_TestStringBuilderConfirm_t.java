 package uw.changecapstone.tweakthetweet;
 
 import java.io.File;
 
 import twitter4j.GeoLocation;
 import twitter4j.StatusUpdate;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
 import twitter4j.conf.ConfigurationBuilder;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.telephony.SmsManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 public class TestStringBuilderConfirm extends CustomWindow {
 
 	private EditText test_tweet, add_details, add_time, add_source, add_contact;
 	private TextView char_count;
 	private String category, tweet;
 	int crntLength;
 	private final String TIME_TAG = "#time";
 	private final String SOURCE_TAG = "#src";
 	private final String CONTACT_TAG = "#cont";	
 	
 	// Constants for accessing the consumer key
 	private static String TWITTER_CONSUMER_KEY = "twitterconsumerkey"; 
 	private static String TWITTER_CONSUMER_SECRET = "twitterconsumersecret"; 
 	
 	// Constants for accessing preference
 	private static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
 	private static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
 	
 	// short code to sms tweet to
 	private static final String SHORT_CODE = "40404";
 
 	// Fields for use in composing tweet object to sent
 	private SharedPreferences pref;
 	private boolean geoLocation;
 	private String twitterConsumerKey;
 	private String twitterConsumerSecret;
 	private String photoPath;
 	private boolean hasPhoto;
 	private double lat;
 	private double longitude;
 	public final static String GPSLAT = "uw.changecapstone.tweakthetweet.gpslat";
 	public final static String GPSLONG = "uw.changecapstone.tweakthetweet.gpslong";
 	private Context context;
 	
 	/*private final TextWatcher charCountWatcher = new TextWatcher() {
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 			char_count.setText(String.valueOf(140 - tweet.length()) + " characters left in tweet");
 		}
 	
 		public void onTextChanged(CharSequence s, int start, int before, int count) {
 			final_tweet = tweet + " " + s;
 			char_count.setText(String.valueOf(140 - final_tweet.length()) + " characters left");
 			test_tweet.setText(final_tweet);
 		}
 	
 		@Override
 		public void afterTextChanged(Editable arg0) {
 			// TODO Auto-generated method stub
 		}
 	
 	}; */
 	
 	private final TextWatcher charCountWatcher = new TextWatcher() {
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 			/*
 			if(crntLength != 1){
 				char_count.setText(String.valueOf(140 - tweet.length()) + " characters left in tweet");
 			}else{
 				char_count.setText(String.valueOf(140 - tweet.length()) + " character left in tweet");
 			}
 			*/
 		}
 	
 		public void onTextChanged(CharSequence s, int start, int before, int count) {
 			String crntTweet = test_tweet.getText().toString();
 			crntLength = 140 - crntTweet.length();
 			if(crntLength < 0){
 				char_count.setTextColor(Color.RED);
 			}else{
 				char_count.setTextColor(Color.BLACK);
 			}
 			
 			if(crntLength != 1){
 				char_count.setText(String.valueOf(crntLength) + " characters left");
 			}else{
 				char_count.setText(String.valueOf(crntLength) + " character left");
 			}
 			
 			//tweet = test_tweet.getText().toString();
 		}
 	
 		@Override
 		public void afterTextChanged(Editable arg0) {
 			// TODO Auto-generated method stub
 		}
 	
 	};
 	
 	private final TextWatcher addCategoryText = new TextWatcher() {
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 		}
 	
 		public void onTextChanged(CharSequence s, int start, int before, int count) {
 				String[] splitTweet = tweet.split(category);
 				
 				if(!tweet.contains(category)){
 					//if the tweet does not contain the category tag, just append it to the end of the string
 					tweet = tweet + " " + category + " " + s;
 				}
 				
 				if(splitTweet.length==1){
 					//If the tweet does not contain a category tag (like if the user deleted/edited it),
 					//append it and the details to the end of the string
 					tweet = tweet + category + " " + s;
 				}else{
 					if(splitTweet[1].contains("#")){
 						//If the tweet contains another hash tag, replace the current category text with the predefined tag that comes first
 						int indexOfTime = splitTweet[1].indexOf(TIME_TAG);
 						int indexOfSource = splitTweet[1].indexOf(SOURCE_TAG);
 						int indexOfContact = splitTweet[1].indexOf(CONTACT_TAG);
 						int indexOfNextTag = 0;
 						
 						//Find the index of the next predefined tag (or -1 if none)
 						if((indexOfTime * indexOfSource * indexOfContact) == -1){
 							//If you multiply all indices together and -1 is the result, all indices are -1 (since 2 indices can't both be at 1)
 							indexOfNextTag = -1;
 						}else if((indexOfTime * indexOfSource * indexOfContact)<-1){
 							//If you multiply all indices together and a negative number less than -1 is the result, you know only 1 index is -1
 							if(indexOfTime==-1){
 								indexOfNextTag = Math.min(indexOfSource, indexOfContact);
 							}else if(indexOfSource==-1){
 								indexOfNextTag = Math.min(indexOfTime, indexOfContact);
 							}else if(indexOfContact==-1){
 								indexOfNextTag = Math.min(indexOfTime, indexOfSource);
 							}
 						}else{
 							//If you multiply all indices together and a positive number is the result, you know that 2 indices are -1 or all 3 indices are positive
 							if(indexOfTime==-1){
 								indexOfNextTag = Math.max(indexOfSource, indexOfContact);
 							}else if(indexOfSource==-1){
 								indexOfNextTag = Math.max(indexOfTime, indexOfContact);
 							}else if(indexOfContact==-1){
 								indexOfNextTag = Math.max(indexOfTime, indexOfSource);
 							}else{
 								indexOfNextTag = Math.min(indexOfTime, Math.min(indexOfSource, indexOfContact));
 							}
 						}
 						
 						if(indexOfNextTag != -1){
 							tweet = splitTweet[0] + category + " " + s + " " + splitTweet[1].substring(indexOfNextTag);
 						}else{
 							//If the tweet contains another # character but not any of the predefined tags, still replace the string after the category tag
 							tweet = splitTweet[0] + category + " " + s + splitTweet[1];
 						}
 					}else{
 						//If it does not contain another actual hash tag, just replace the string after category tag
 						tweet = splitTweet[0] + category + " " + s;					
 					}
 					test_tweet.setText(tweet);
 				}
 		}
 	
 		@Override
 		public void afterTextChanged(Editable arg0) {		
 			
 		}
 	
 	};
 	
 	/**
 	 * A method that handles changes in detail boxes for optional tags
 	 * @param startTag a String that equals the main tag whose details are being replaced
 	 * @param secondTag a String that equals the second out of 3 required tags
 	 * @param thirdTag a String that equals the third out of 3 required tags
 	 * @param s a CharSequence (from onTextChanged)
 	 */
 	private void changeTextBasedOnOptTag(String startTag, String secondTag, String thirdTag, CharSequence s){
 		String[] splitTweet = tweet.split(startTag);
 		
 		if(!tweet.contains(startTag)){
 			//if the tweet does not contain the category tag, just append it to the end of the string
 			tweet = tweet + " " + startTag + " " + s;
 		}else{
 			if(splitTweet[1].contains("#")){
 				//If the tweet contains another hash tag, replace the current category text with the predefined tag that comes first
 				int indexOfSecond = splitTweet[1].indexOf(secondTag);
 				int indexOfThird = splitTweet[1].indexOf(thirdTag);
 				int indexOfNextTag = 0;
 				
 				//Find the index of the next predefined tag (or -1 if none)
 				if((indexOfSecond * indexOfThird) == 1){
 					//If you multiply all indices together and -1 is the result, all indices are -1 (since 2 indices can't both be at 1)
 					indexOfNextTag = -1;
 				}else if((indexOfSecond * indexOfThird)<-1){
 					//If you multiply all indices together and a negative number less than -1 is the result, you know only 1 index is -1
 					if(indexOfSecond==-1){
 						indexOfNextTag = indexOfThird;
 					}else{
 						indexOfNextTag = indexOfSecond;
 					}
 				}else{
 					//If you multiply all indices together and a positive number (not 1) is the result, you know that 2 indices are both present		
 					indexOfNextTag = Math.min(indexOfSecond, indexOfThird);
 				}
 				
 				if(indexOfNextTag != -1){
 					tweet = splitTweet[0] + startTag + " " + s + " " + splitTweet[1].substring(indexOfNextTag);
 				}else{
 					//If the tweet contains another # character but not any of the predefined tags, still replace the string after the category tag
 					tweet = splitTweet[0] + startTag + " " + s + splitTweet[1];
 				}
 			}else{
 				//If it does not contain another actual hash tag, just replace the string after category tag
 				tweet = splitTweet[0] + startTag + " " + s;					
 			}
 		}
 		test_tweet.setText(tweet);
 	}
 	
 	private final TextWatcher addTimeText = new TextWatcher() {
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 		}
 	
 		public void onTextChanged(CharSequence s, int start, int before, int count) {
 			changeTextBasedOnOptTag(TIME_TAG, SOURCE_TAG, CONTACT_TAG, s);			
 		}
 	
 		@Override
 		public void afterTextChanged(Editable arg0) {
 		}
 	
 	};
 	 	
 	private final TextWatcher addSourceText = new TextWatcher() {
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 		}
 	
 		public void onTextChanged(CharSequence s, int start, int before, int count) {
 			changeTextBasedOnOptTag(SOURCE_TAG, TIME_TAG, CONTACT_TAG, s);	
 		}
 	
 		@Override
 		public void afterTextChanged(Editable arg0) {
 		}
 	
 	};
 	
 	private final TextWatcher addContactText = new TextWatcher() {
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 		}
 	
 		public void onTextChanged(CharSequence s, int start, int before, int count) {
 			changeTextBasedOnOptTag(CONTACT_TAG, TIME_TAG, SOURCE_TAG, s);	
 		}
 	
 		@Override
 		public void afterTextChanged(Editable arg0) {
 		}
 	
 	};
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_test_string_builder_confirm);
 		this.title.setText("#more info");
 		context = this;
 		// get information (tweet,  lat, long, category) for 
 		// building tweet
 		Bundle bundle = getIntent().getExtras();
 		category = bundle.getString("category");
 		tweet = bundle.getString("tweet");
 		lat = bundle.getDouble(GPSLAT);
 		longitude = bundle.getDouble(GPSLONG);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
 		//Set up main tweet text box
 		test_tweet = (EditText) findViewById(R.id.test_tweet);
 		test_tweet.setText(tweet);
 		test_tweet.setHorizontallyScrolling(false);
 		test_tweet.setMaxLines(Integer.MAX_VALUE);		
 		test_tweet.addTextChangedListener(charCountWatcher);
 		test_tweet.setOnEditorActionListener(new OnEditorActionListener() {        
 		    @Override
 		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 		        if(actionId==EditorInfo.IME_ACTION_DONE){
 		            //Clear focus when the done button is clicked 
 		            test_tweet.clearFocus();
 		        }
 		    return false;
 		    }
 		});
 		
 		//Set up char count
 		char_count = (TextView) findViewById(R.id.char_count);
 		crntLength = 140 - tweet.length();
 		char_count.setText(String.valueOf(crntLength) + " characters left in tweet");
 		
 		//Set up add details text box
 		add_details = (EditText) findViewById(R.id.additional_details);	
 		add_details.addTextChangedListener(addCategoryText);
 		
 		//Set up add time text box
 		add_time = (EditText) findViewById(R.id.time_text);
 		add_time.addTextChangedListener(addTimeText);
 		
 		//Set up add source text box
 		add_source = (EditText) findViewById(R.id.source_text);
 		add_source.addTextChangedListener(addSourceText);
 		
 		//Set up add contact text box
 		add_contact = (EditText) findViewById(R.id.contact_text);
 		add_contact.addTextChangedListener(addContactText);
 		
 		//Set instruction text based on category
 		TextView instructionText = (TextView) findViewById(R.id.add_more_details_text);
 		instructionText.setText("Add more details for your " + category + " category");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.test_string_builder_confirm, menu);
 		return true;
 	}
 	
 	public void nextViewSent(View view) throws NameNotFoundException {
 		if(crntLength<0){
 			Toast.makeText(getApplicationContext(), "Your tweet is longer than 140 characters, please shorten it.", Toast.LENGTH_SHORT).show();
 		}else{
 			// determine if there are gps coordinates to add to a tweet
 			geoLocation = (lat == 0.0) && (longitude == 0.0);
 			hasPhoto = false;
 			photoPath = "";
 			
 			//Set the tweet value to what is in the final tweet text box
 			tweet = test_tweet.getText().toString();
 			
 			// See if data access is available
 			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 			boolean data = pref.getBoolean("data", true);
 			if (data) {
 				dataTweet();
 			} else {
 				smsTweet();
 			}
 		}
 	}
 
 	public void nextViewPhoto(View view) {
 		Intent i = new Intent(this, PhotoActivity.class);
 		startActivity(i);
 	}
 	
 	private void dataTweet() {
 
 		try {
 			// get the consumer keys from metadate
 			ApplicationInfo ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
 			Bundle metadata = ai.metaData;
 			twitterConsumerKey = metadata.getString(TWITTER_CONSUMER_KEY);
 			twitterConsumerSecret = metadata.getString(TWITTER_CONSUMER_SECRET);
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 		}
 		// Send the tweet in a new thread
 		UpdateTwitterStatus updateTask = new UpdateTwitterStatus();
  		updateTask.execute(new String[] {tweet, ((Double)lat).toString(), ((Double)longitude).toString(), photoPath});
 	}
 	
 	private void smsTweet() {
 		// send a text message of just the tweet text if 
 		// only sms is available
 		SmsManager smsManager = SmsManager.getDefault();
 		smsManager.sendTextMessage(SHORT_CODE, null, tweet, null, null);
 
 	}
 	
 	
 	
 	/*
 	 * Http requests must be done on separate thread 
 	 */
 	
 	private class UpdateTwitterStatus extends AsyncTask<String, String, String> {
 
 		
 		protected String doInBackground(String... args) {
 			// retrieve the information to build the tweet
 			String status = args[0];
 			double lat = Double.parseDouble(args[1]);
 			double longitude = Double.parseDouble(args[2]);
 			try	{
 				ConfigurationBuilder builder = new ConfigurationBuilder();
 				builder.setOAuthConsumerKey(twitterConsumerKey);
 				builder.setOAuthConsumerSecret(twitterConsumerSecret);
 				
 				// make a twitter instance// Get the access credentials 
 				String access_token = pref.getString(PREF_KEY_OAUTH_TOKEN, "");
 				String access_token_secret = pref.getString(PREF_KEY_OAUTH_SECRET, "");
 				
 				
 				StatusUpdate newStatus = new StatusUpdate(status);
 				
 				// add geolocation if it is present
 				if (geoLocation) {
 					GeoLocation location = new GeoLocation(lat, longitude);
 					newStatus.setLocation(location);
 				}
 				
 				// add a photo if it is present
 				if (hasPhoto) {
 					String filePath = args[3];
 					File photo = new File(filePath);
 					newStatus.setMedia(photo);
 				}
 				// create an access token object based on credentials
 				AccessToken accessToken = new AccessToken(access_token, access_token_secret);
 				Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
 				
 				// Update status
 				twitter4j.Status response = twitter.updateStatus(newStatus);
 				
 				
 			} catch (TwitterException e) {
 				// Error in updating status
 				Log.d("Twitter Update Error", e.getMessage());
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						Toast.makeText(getApplicationContext(),
 								"Tweet Failed", Toast.LENGTH_SHORT)
 								.show();
 						
 					}
 				});
 			}
 			return null;
 		}
 		
 		protected void onPostExecute(String file_url) {
 			// Redirect to the tweet complete page
 			Intent i = new Intent(context, TestStringBuilderTweetSent.class);
 			i.putExtra("tweet", tweet);
 			startActivity(i);
 			
 			
 		}
 	}
 }
