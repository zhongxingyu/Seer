 package com.twitter.streaming;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TimeZone;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.Credentials;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.Service;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.Html;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 	public class TwitterStreamingProjectActivity extends Activity {
 		
 		/** Constants **/
 		//Activity Lifecycle Restoration Constant
 		public static final String PREFS_NAME = "MyPrefsFile";
 		private static final String NAMETEXT_LISTVIEW = "NameText";
 		private static final String DATETIME_LISTVIEW = "DateTime";
 		private static final String PREF_CURRSEARCH = "currentSearchWord";
 		private static final String NOTHING_STRING = "<nothing>!";
 		/** Global variables **/
 		//UI Variables
 		private TextView uiTextView_CurrentSearchWord,uiTextView_SearchEmpty,uiTextView_NoTweetIndicator;
 		private EditText uiEditText;
 		private Button uiButton_StartStop,uiButton_Search;
 		private ProgressBar uiProgressBar;
 		
 		//ListView Related Variables
 		private List<HashMap<String,String>> mTweetList = new ArrayList<HashMap<String,String>>();
 		private SimpleAdapter mSimpleAdapter;
 				
 		//StreamTask Related Variables
 		private DefaultHttpClient mClient = null;
 		private HttpGet mHttpGet = null;
 		StreamTask mStreamTask = null;
 		
 		//Other Variables
 		private boolean mContinueFlag = false;
 		private String mSearchString = "";
 		private boolean properlyClosedFlag = true;
 		
 		@Override
 		public void onCreate(Bundle savedInstanceState) {
 		    super.onCreate(savedInstanceState);
 		    setContentView(R.layout.main);
 		    
 		    /** Setup the UI components accordingly **/
 		    //Obtain ui components from XML
 		    uiTextView_CurrentSearchWord = (TextView)findViewById(R.id.xmlCurrentSearchWord);
 		    uiTextView_SearchEmpty = (TextView)findViewById(R.id.xmlEditTextDefault);
 	    	uiTextView_NoTweetIndicator = (TextView)findViewById(R.id.xmlNoTweetIndicator);
 	    	uiButton_Search = (Button)findViewById(R.id.xmlSearchButton);
 		    uiButton_StartStop = (Button)findViewById(R.id.xmlStartStopButton);
 		    
 		    //Toggle the buttons' alpha & background when pressed
 		    uiButton_Search.setOnTouchListener(new OnTouchListener(){
 		    	   public boolean onTouch(View v, MotionEvent e){
 		    		   switch(e.getAction())
 		    		   {
 		    		   case MotionEvent.ACTION_DOWN:
 		    			   uiButton_Search.setTextColor(Color.GRAY);
 		    			   uiButton_Search.setBackgroundResource(R.drawable.ddw_blue);
 		    			   break;
 		    		   case MotionEvent.ACTION_UP:
 		    			   uiButton_Search.setTextColor(Color.WHITE);
 		    			   uiButton_Search.setBackgroundResource(R.drawable.ddw_black);
 		    			   break;
 		    		   default:
 		    			   break;
 		    		   }
 		    	     return false;}});
 		    uiButton_StartStop.setOnTouchListener(new OnTouchListener(){
 		    	   public boolean onTouch(View v, MotionEvent e){
 		    		   switch(e.getAction())
 		    		   {
 		    		   case MotionEvent.ACTION_DOWN:
 		    			   uiButton_StartStop.setTextColor(Color.GRAY);
 		    			   uiButton_StartStop.setBackgroundResource(R.drawable.ddw_blue);
 		    			   break;
 		    		   case MotionEvent.ACTION_UP:
 		    			   uiButton_StartStop.setTextColor(Color.WHITE);
 		    			   uiButton_StartStop.setBackgroundResource(R.drawable.ddw_black);
 		    			   break;
 		    		   default:
 		    			   break;
 		    		   }
 		    	     return false;}});
 		    uiProgressBar = (ProgressBar)findViewById(R.id.xmlProgressBar);
    			uiProgressBar.setVisibility(ProgressBar.INVISIBLE);
    			
     		//Toggle visibility of "Type Something Here" string depends on EditText is empty/not-empty
     		uiEditText = ((EditText)findViewById(R.id.xmlSearchText));
 	    	TextWatcher redoWatcher = new TextWatcher() 
 	    		{
 		    	@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 			    @Override public void afterTextChanged(Editable s) {}
 			    @Override public void onTextChanged(CharSequence s, int start, int before,int count) 
 			    	{if((uiEditText.getText().toString().length()==0)){uiTextView_SearchEmpty.setVisibility(EditText.VISIBLE);}
 		    		 else {uiTextView_SearchEmpty.setVisibility(EditText.INVISIBLE);}
 			    	}
 			    };
 		    uiEditText.addTextChangedListener(redoWatcher);
 		    
 		    //Retrieve the last-pulled tweetList if exist
 		    if (getLastNonConfigurationInstance()!=null)
 		    {mTweetList=(ArrayList<HashMap<String,String>>)getLastNonConfigurationInstance();}
 		    
 		    //Setup the ListView
 		    mSimpleAdapter = new mySimpleAdapter(this, mTweetList, R.layout.row, new String[] {NAMETEXT_LISTVIEW,DATETIME_LISTVIEW}, new int[] {R.id.xmlRowText,R.id.xmlRowDateTime});
 		    ((ListView)findViewById(R.id.xmlListView)).setAdapter(mSimpleAdapter);
 			//--Display "No Tweets yet" text, when ListView is empty
 		    if(mTweetList.size()>0)
 			{uiTextView_NoTweetIndicator.setVisibility(TextView.INVISIBLE);}
 			else {uiTextView_NoTweetIndicator.setVisibility(TextView.VISIBLE);}
 		    mSimpleAdapter.notifyDataSetChanged();
 			
 			//Hide the soft keyboard by default
 			this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 			
 		}//End of onCreate()
 		
 		/** Handle Screen Rotation/ Temporarily going somewhere else, eg. home button pressed **/
 		@Override
 		public Object onRetainNonConfigurationInstance()
 		{return(mTweetList);}
 
 		/** Restore the Last Used Search Word **/
 		@Override
 		protected void onResume() {
 			super.onResume();
 
 		    /* Restore the last-used current-search-word */
 		    SharedPreferences tempRestoreSharedPreferences = getSharedPreferences(PREFS_NAME, 0);
 		    //get last-used current-search-word from SharedPreference permanent storage
 		    mSearchString = tempRestoreSharedPreferences.getString(PREF_CURRSEARCH, NOTHING_STRING);
 		    //if no last-used current-search-word is found, set the string to "" (nothing), else, set to the restored last-used current-search-word 
 		    if (mSearchString.equals(NOTHING_STRING))
 		    {uiTextView_CurrentSearchWord.setText("");}
 		    else {uiTextView_CurrentSearchWord.setText(mSearchString.replaceAll("%20", " "));}
 		}//End of onResume()
 		
 		@Override
 		protected void onPause() {
 			super.onPause();
 
 			/* Save the current-search-word before going off */
 			SharedPreferences tempSharedPreferences = getSharedPreferences(PREFS_NAME, 0);
 			SharedPreferences.Editor tempEditor = tempSharedPreferences.edit();
 			tempEditor.putString(PREF_CURRSEARCH, mSearchString);
 			tempEditor.commit();
 	        
 			/* Stop the connection & stop updating the ListView before leaving the screen */
 			//when mContinueFlag is false, parseTweets() will exit from do-while loop, and mClient will shutdown() the connection, IMPORTANT! else 3g quota will be drained fast!
 			mContinueFlag = false;
 			
 			//update the UI accordingly
 			uiButton_StartStop.setText("Play");
 		    uiProgressBar.setVisibility(ProgressBar.INVISIBLE);
 	     }//End of onPause()
 
 		/** Search Button Listener **/
 		public void onNewSearchClick( View v ) {
 			Editable tempEditable = uiEditText.getText();
 			
 			if( tempEditable.toString().length() > 0 ) {
 				//update the CurrentSearchWord UI
 				mSearchString = tempEditable.toString();
 				uiTextView_CurrentSearchWord.setText(mSearchString.replaceAll("%20", " "));
 				//update the mSearchString
 				mSearchString = mSearchString.replaceAll(" ", "%20");
 				//change the StartStop button to "Play"
 				uiButton_StartStop.setText("Play");
 				//show the ProgressBar
 			    uiProgressBar.setVisibility(ProgressBar.INVISIBLE);
 			    //Hide the keyboard
 			    InputMethodManager imm = (InputMethodManager)this.getSystemService(Service.INPUT_METHOD_SERVICE);
 			    imm.hideSoftInputFromWindow(uiEditText.getWindowToken(), 0);
 			    //Clear the EditText
 			    uiEditText.setText("");
 			    
 			    //update the mContinueFlag to false, so that do-while in parseTweet() will exit
 				mContinueFlag = false;
 				//cancel the AsyncTask
 //				if(mStreamTask!=null){mStreamTask.cancel(true);}

 			}
 			else {
 				Toast.makeText(this, "You must fill in a search term", Toast.LENGTH_SHORT).show();
 				}
 			
 			Log.d("Twitter","New Search Term : "+mSearchString);
 		}//End of onNewSearchClick()
 		
 		/** Start/Stop Button Listener **/
 		public void startStop( View v ) {
 				
 			if(((Button)v).getText().equals("Play") ) {
 				if (mSearchString.equals(NOTHING_STRING))
 					{Toast.makeText(this, "You must fill in a search term", Toast.LENGTH_SHORT).show();}
 				else if( mSearchString.length() > 0 ) 
 					{
 		    		((Button)v).setText("Pause");
 				    uiProgressBar.setVisibility(ProgressBar.VISIBLE);
 					mStreamTask = new StreamTask();
 		    		mStreamTask.execute();
 					}
 				else 
 					{Toast.makeText(this, "You must fill in a search term", Toast.LENGTH_SHORT).show();}
 				}
 			else {
 				mContinueFlag = false;
 //				if(mStreamTask!=null){mStreamTask.cancel(true);}
 				((Button)v).setText("Play");
 			    uiProgressBar.setVisibility(ProgressBar.INVISIBLE);
 				}
 			}
 		/** static function to parse twitter time stamp **/
 		public static String parseTwitterTimeStamp(String timeString) throws ParseException {
 			  final String TWITTER="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
 			  final String resultTWITTER="yyyy-MM-dd HH:mm";
 			  SimpleDateFormat sf = new SimpleDateFormat(TWITTER,Locale.ENGLISH);
 			  SimpleDateFormat resultSF = new SimpleDateFormat(resultTWITTER,Locale.ENGLISH);
 			  sf.setLenient(true);
 			  Date tempDate = sf.parse(timeString);
 			  resultSF.setTimeZone(TimeZone.getDefault());
 			  return resultSF.format(tempDate);
 			  }
 		
 	    /** Inner Class for AsyncTask **/
 	    private class StreamTask extends AsyncTask<Integer, Integer, Integer> {
 	    	
 	    	private String mUrl = "https://stream.twitter.com/1/statuses/filter.json?track=";
 	    	//public InputStream in=null;
 	    	@Override
 	    	protected Integer doInBackground(Integer... params) {
 			try {
 				mClient = new DefaultHttpClient();
 				Credentials creds = new UsernamePasswordCredentials("nogetjr", "temptemp1234");
 				mClient.getCredentialsProvider().setCredentials( new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);
 				mHttpGet = new HttpGet();
 				mHttpGet.setURI(new URI("https://stream.twitter.com/1/statuses/filter.json?track=" + mSearchString));
 				HttpResponse response = mClient.execute(mHttpGet);
 				InputStream in = response.getEntity().getContent();
 				BufferedReader reader = new BufferedReader( new InputStreamReader(in) );
 
 				//properlyClosedFlag is set to false because connection is opened now
 				properlyClosedFlag = false;
 				parseTweets(reader);
 				in.close();
 //				catch (SocketException e){properlyClosedFlag=true;}
 
 				} 
 			catch (Exception e) {Log.e("Twitter", "doInBackground_" + e.toString());}
 			finally{
 //				mHttpGet.abort();
 //				 if (in != null) {
 //			            try {in.close();}
 //			            catch(IOException e) {Log.e("Twitter", "in.close():" + e.toString());}
 //			        }
 				 
 				
 			}
 			return new Integer(1);
 	    	}//End of doInBackground
 			
 	    	private void parseTweets( BufferedReader reader ) {
 			try {
 				String line = "";
 				mContinueFlag = true;
				do{
 						line = reader.readLine();
 						Log.d("Twitter", "mContinueFlag: " + mContinueFlag + ", Line: " + line);
 						JSONObject tweet = new JSONObject(line);
 						HashMap<String, String> tweetMap = new HashMap<String, String>();
 						if (tweet.has("text")) {
 							tweetMap.put(NAMETEXT_LISTVIEW, "<font color=\"#000066\"><b>"+(tweet.getJSONObject("user").getString("screen_name"))+"</b></font>"+" - "+tweet.getString("text"));
 							tweetMap.put(DATETIME_LISTVIEW,parseTwitterTimeStamp(tweet.getString("created_at")));
 							mTweetList.add(0, tweetMap);
 							if (mTweetList.size() >= 1000) {
 								mTweetList.remove(mTweetList.size() - 1);
 								}
 							publishProgress(1);
 							//prevent the tweets from flowing in too fast, it will cause array-adapter(ListView) out of sync and throw exception 
 							Thread.sleep(300);
 						}
 				} while (mContinueFlag && line.length() > 0);
 				
 				//close connection whenever mContinueFlag is stopped
 				mClient.getConnectionManager().shutdown();
				
 				}
			catch (Exception e) {}
 			}//End of parseTweets
 	    	
 	    	@Override
 			protected void onProgressUpdate(Integer... progress) {
 	    		runOnUiThread(new Runnable() {
 	    		    public void run() {
 	    				mSimpleAdapter.notifyDataSetChanged();
 	    		    }
 	    		});
 //				mSimpleAdapter.notifyDataSetChanged();
 
 				//show that there is currently no tweets found
 				if(mTweetList.size()>0)
 				{uiTextView_NoTweetIndicator.setVisibility(TextView.INVISIBLE);}
 				else {uiTextView_NoTweetIndicator.setVisibility(TextView.VISIBLE);}
 			}
 			
 			@Override protected void onPostExecute(Integer i) {}
 	    	
 	    	} //End of AsyncTask Inner Class
 	    
 	    public class mySimpleAdapter extends SimpleAdapter{
 	    	public mySimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to)
 	    	{super(context,data, resource,from, to);}
 	    	
 	    	@Override
 	    	public View getView(int position, View convertView, ViewGroup parent) {
 	    		View row=super.getView(position, convertView, parent);
 	    		TextView temp =(TextView)row.findViewById(R.id.xmlRowText);
 	    		temp.setText(Html.fromHtml(temp.getText().toString()));
 	    		return row;
 	    	}
 	    }//End of mySimpleAdapter
 	}//End of TwitterStreamingProjectActivity Class
