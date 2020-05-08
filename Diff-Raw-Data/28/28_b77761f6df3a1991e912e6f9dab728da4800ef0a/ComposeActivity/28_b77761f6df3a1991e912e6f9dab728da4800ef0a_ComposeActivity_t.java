 package com.codepath.apps.twitterclient.ui.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.codepath.apps.twitterclient.R;
 import com.codepath.apps.twitterclient.models.Tweet;
 import com.codepath.apps.twitterclient.network.RestClientApp;
 import com.codepath.apps.twitterclient.network.TwitterClient;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 import org.json.JSONObject;
 
 /**
  * Created by rhu on 10/19/13.
  */
 public class ComposeActivity extends Activity {
 
 	Tweet replyToTweet;
 	String text;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_compose);
 
 	    Long tweetId = getIntent().getLongExtra("tweetId", 0);
 
 
 	    EditText etValue = (EditText) findViewById(R.id.editText);
 	    etValue.addTextChangedListener(new TextWatcher() {
		    int TOTAL_COUNT = 140;

 		    @Override
 	        public void onTextChanged(CharSequence s, int start, int before, int count) {
 
 		    }
 
 		    @Override
 		    public void beforeTextChanged(CharSequence s, int start, int before, int count) {

 		    }
 
 		    @Override
 		    public void afterTextChanged(Editable s) {
 			    TextView charCount = (TextView) findViewById(R.id.charCount);
			    charCount.setText(String.valueOf(TOTAL_COUNT - s.length()));
 		    }
 	    });

	    if (tweetId != 0) {
		    replyToTweet = Tweet.byId(tweetId);

		    Log.d("debug", "Composing to " + replyToTweet.getUser().getTwitterHandle());
		    EditText tweet = (EditText) findViewById(R.id.editText);
		    tweet.append("@" + replyToTweet.getUser().getTwitterHandle() + " ");
	    }

     }
 
     public void cancelCompose(View v) {
         finish();
     }
 
     public void sendTweet(View v) {
         EditText tweet = (EditText) findViewById(R.id.editText);
 
         if (tweet.getText() == null) {
             Toast.makeText(getBaseContext(), "Blank tweet", Toast.LENGTH_SHORT).show();
             return;
         }
 
         text = tweet.getText().toString();
 
         TwitterClient client = RestClientApp.getRestClient();
 
 	    Long replyToId = null;
 
 	    if (replyToTweet != null) {
 		    replyToId = replyToTweet.getPostId();
 		    Log.d("debug", "Sending in_reply_to " + replyToId);
 	    }
 
         client.postTweet(text, replyToId, new JsonHttpResponseHandler() {
 
             @Override
             public void onSuccess(JSONObject response) {
                 Toast.makeText(getBaseContext(), "Tweet posted!", Toast.LENGTH_SHORT).show();
                 Intent data = new Intent();
                 data.putExtra("response", response.toString());
                 setResult(RESULT_OK, data);
                 finish();
             }
 
             public void onFailure(Throwable e, JSONObject json) {
                 e.printStackTrace();
             }
         });
     }
 
 }
