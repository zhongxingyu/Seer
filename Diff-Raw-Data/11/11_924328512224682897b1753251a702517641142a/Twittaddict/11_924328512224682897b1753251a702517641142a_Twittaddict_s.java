 package com.rushdevo.twittaddict;
 
 import static com.rushdevo.twittaddict.constants.Twitter.CONSUMER;
 import static com.rushdevo.twittaddict.constants.Twitter.PROVIDER;
 import oauth.signpost.AbstractOAuthConsumer;
 import oauth.signpost.OAuth;
 import oauth.signpost.exception.OAuthCommunicationException;
 import oauth.signpost.exception.OAuthExpectationFailedException;
 import oauth.signpost.exception.OAuthMessageSignerException;
 import oauth.signpost.exception.OAuthNotAuthorizedException;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.rushdevo.twittaddict.data.TwittaddictData;
 import com.rushdevo.twittaddict.twitter.TwitterUser;
 
 public class Twittaddict extends Activity implements Runnable, OnClickListener {
 	
 	private static String CALLBACK_URL = "twittaddict://twitterauth";
 	private static final int INIT_MESSAGE = 0;
 	private static final int TIMER_MESSAGE = 1;
 	private static final int RESTART_MESSAGE = 2;
 	private static final int GAME_LENGTH = 60;
 	private TwittaddictData db;
 	private Integer userId;
 	private Game game;
 	private boolean initializing = false;
 	private ProgressDialog progressDialog;
 	
 	// Timer
 	TextView timerLabel;
 	
 	// User Question views
 	private View userLayout;
 	private ImageView userView;
 	private TextView userName;
 	private TextView tweet1View;
 	private TextView tweet2View;
 	private TextView tweet3View;
 	// Tweet Question views
 	private View tweetLayout;
 	private TextView tweetView;
 	private View user1Container;
 	private ImageView user1View;
 	private TextView user1Name;
 	private View user2Container;
 	private ImageView user2View;
 	private TextView user2Name;
 	private View user3Container;
 	private ImageView user3View;
 	private TextView user3Name;
 	// Score area views
 	private TextView scoreContainer;
 	private ImageView correctMarker;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         db = new TwittaddictData(this);
         setContentView(R.layout.main);
         // Init view elements
         timerLabel = (TextView)findViewById(R.id.timer);
         tweetLayout = (LinearLayout)findViewById(R.id.tweet_question_container);
 		userLayout = (LinearLayout)findViewById(R.id.user_question_container);
         userView = (ImageView)findViewById(R.id.user);
         userName = (TextView)findViewById(R.id.userName);
         tweet1View = (TextView)findViewById(R.id.tweet1_container);
         tweet1View.setOnClickListener(this);
         tweet2View = (TextView)findViewById(R.id.tweet2_container);
         tweet2View.setOnClickListener(this);
         tweet3View = (TextView)findViewById(R.id.tweet3_container);
         tweet3View.setOnClickListener(this);
         tweetView = (TextView)findViewById(R.id.tweet_container);
     	user1View = (ImageView)findViewById(R.id.user1);
     	user1Container = findViewById(R.id.user1Container);
     	user1Container.setOnClickListener(this);
     	user1Name = (TextView)findViewById(R.id.user1Name);
     	user2View = (ImageView)findViewById(R.id.user2);
     	user2Container = findViewById(R.id.user2Container);
     	user2Container.setOnClickListener(this);
     	user2Name = (TextView)findViewById(R.id.user2Name);
     	user3View = (ImageView)findViewById(R.id.user3);
     	user3Container = findViewById(R.id.user3Container);
     	user3Container.setOnClickListener(this);
     	user3Name = (TextView)findViewById(R.id.user3Name);
     	scoreContainer = (TextView)findViewById(R.id.score_container);
     	correctMarker = (ImageView)findViewById(R.id.correct_marker);
     }
     
     @Override
     public void onDestroy() {
     	super.onDestroy();
     	if (db != null) db.close();
     }
     
     @Override
     public void onPause() {
     	super.onPause();
     	if (progressDialog != null) progressDialog.dismiss();
     }
     
     @Override
     public void onResume() {
     	super.onResume();
     	if (game != null && game.isComplete()) {
     		restartGame();
     	} else {
 	    	Uri uri = this.getIntent().getData();  
 	    	if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {  
 	    	    String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);  
 	    	    // this will populate token and token_secret in consumer
 	    	    try {
 					PROVIDER.retrieveAccessToken(CONSUMER, verifier);
 					saveTokenAndSecret(CONSUMER);
 					startGame();
 				} catch (OAuthMessageSignerException e) {
 					errorAlert(getString(R.string.oauth_failure));
 					debug(e);
 				} catch (OAuthNotAuthorizedException e) {
 					errorAlert(getString(R.string.oauth_failure));
 					debug(e);
 				} catch (OAuthExpectationFailedException e) {
 					errorAlert(getString(R.string.oauth_failure));
 					debug(e);
 				} catch (OAuthCommunicationException e) {
 					errorAlert(getString(R.string.oauth_failure));
 					debug(e);
 				}
 	    	} else {
 	    		authorizeOrStartGame();
 	    	}
     	}
     }
     
     public void onClick(View v) {
 		switch(v.getId()) {
 		case R.id.tweet1_container:
 			setAnswer(1, v);
 			break;
 		case R.id.tweet2_container:
 			setAnswer(2, v);
 			break;
 		case R.id.tweet3_container:
 			setAnswer(3, v);
 			break;
 		case R.id.user1Container:
 			setAnswer(1, v);
 			break;
 		case R.id.user2Container:
 			setAnswer(2, v);
 			break;
 		case R.id.user3Container:
 			setAnswer(3, v);
 			break;
 		}
 	}
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.menu, menu);
     	return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     	case R.id.exit:
     		finish();
     		return true;
     	}
     	return false;
     }
     
     @Override
 	public void run() {
     	boolean starting = game == null || game.isComplete();
     	if (starting) {
     		// Start the game
         	if (game == null) {
         		game = new Game(this);
         		if (game.getSuccess()) {
         			game.start();
         		} else if (game.wasDeauthorized()) {
         			game = null;
         			authorize();
         		}
         		handler.sendEmptyMessage(INIT_MESSAGE);
         	} else if (game.isComplete()){
         		if (game.getSuccess())  {
         			handler.sendEmptyMessage(RESTART_MESSAGE);
         			game.restart();
         		}
         	}
         	// Start the other thread to generate additional questions
         	if (game == null || !game.getSuccess()) return; // Bail if we don't have a successful game
         	Thread thread = new Thread(this);
 	    	thread.start();
         	// Deal with the timer
     		long timeStamp = System.currentTimeMillis() / 1000;
     		int seconds = GAME_LENGTH;
     		while (seconds > 0) {
     			// Update timer
     			int diff = GAME_LENGTH - (int)((System.currentTimeMillis() / 1000) - timeStamp);
     			if (diff < 0) diff = 0;
     			if (diff != seconds) {
     				seconds = diff;
     				Message msg = new Message();
             		msg.what = TIMER_MESSAGE;
             		msg.arg1 = seconds;
             		handler.sendMessage(msg);
     			}
     		}
     		if (game.getSuccess()) {
 	    		game.finish();
 	    		saveScore();
 	    		Intent intent = new Intent(this, GameOverScreen.class);
 	    		intent.putExtra("score", game.getScore());
 	    		intent.putExtra("user", game.getUser().getScreenName());
 	    		TwitterUser bff = db.getBFF(game);
 	    		intent.putExtra("bff", (bff == null) ? "" : bff.getScreenName());
 	    		intent.putExtra("bffAvatar", (bff == null) ? "" : bff.getAvatar());
 	    		startActivity(intent);
 	    		finish();
     		}
     	} else {
     		while (game.getSuccess() && game.isInPlay()) {
     			// Keep generating additional questions in the question queue so they are pre-loaded
     			// (Will maintain at most 5 in the queue, and then will do nothing on this call)
     			game.generateNextQuestion();
     		}
     	}
 	}
     
     public void startGame() {
     	if (!initializing) {
 	    	initializing = true;
 	    	String title = getResources().getString(R.string.loading_title);
 	    	String message = getResources().getString(R.string.loading_message);
 	    	progressDialog = ProgressDialog.show(this, title, message, true);
 	    	Thread thread = new Thread(this);
 	    	thread.start();
     	}
     }
     
     public void restartGame() {
     	Thread thread = new Thread(this);
     	thread.start();
     }
     
     public void showNextQuestion() {
     	if (game.isInPlay()) {
     		Question question = game.getNextQuestion();
     		if (question instanceof TweetQuestion) {
     			displayTweetQuestion((TweetQuestion)question);
     		} else if (question instanceof UserQuestion) {
     			displayUserQuestion((UserQuestion)question);
    		} // Else - TODO: what do we do...
    	} // Else - TODO: what do we do...
     }
     
     private void setAnswer(int index, View v) {
     	if (game != null && game.isInPlay() && game.setChoiceForQuestion(index)) {
     		if (game.currentAnswerIsCorrect()) {
     			// Show green checkmark
     			showCorrect(index, v);
     		} else {
     			// Show red x
     			showIncorrect(index, v);
     		}
     		updateScore();
     		updateFriendStats();
     		showNextQuestion();
     	}
     }
     
     private void showCorrect(int index, View v) {
     	correctMarker.setImageDrawable(getResources().getDrawable(R.drawable.correct));
     }
     
     private void showIncorrect(int index, View v) {
     	correctMarker.setImageDrawable(getResources().getDrawable(R.drawable.wrong));
     }
     
     private void updateScore() {
     	scoreContainer.setText(game.getScore().toString());
     }
     
     private void updateFriendStats() {
     	TwitterUser friend = game.getCurrentQuestionUser();
     	Cursor cursor = db.getFriendStat(friend);
     	if (cursor.moveToNext()) {
     		db.updateFriendStat(cursor.getLong(0), cursor.getInt(3), cursor.getInt(4), game.currentAnswerIsCorrect());
     	} else {
     		db.createFriendStat(friend, this.userId, game.currentAnswerIsCorrect());
     	}
     }
     
     private void saveScore() {
     	db.saveHighScore(this.userId, game.getScore());
     }
     
     private void displayTweetQuestion(TweetQuestion question) {
     	// Set the tweet text
     	tweetView.setText(question.getStatus().getText());
     	// Display Possible User Avatars
     	Drawable drawable = getOrLoadAvatar(question.getUser1());
     	user1View.setImageDrawable(drawable);
     	user1Name.setText(question.getUser1().getScreenName());
     	drawable = getOrLoadAvatar(question.getUser2());
     	user2View.setImageDrawable(drawable);
     	user2Name.setText(question.getUser2().getScreenName());
     	drawable = getOrLoadAvatar(question.getUser3());
     	user3View.setImageDrawable(drawable);
     	user3Name.setText(question.getUser3().getScreenName());
     	// Display the container
     	tweetLayout.setVisibility(LinearLayout.VISIBLE);
 		userLayout.setVisibility(LinearLayout.GONE);
     }
     
     private void displayUserQuestion(UserQuestion question) {
     	// Set the three tweet texts
     	tweet1View.setText(question.getStatus1().getText());
     	tweet2View.setText(question.getStatus2().getText());
     	tweet3View.setText(question.getStatus3().getText());
     	// Display User Avatar
     	Drawable drawable = getOrLoadAvatar(question.getUser());
     	userView.setImageDrawable(drawable);
     	userName.setText(question.getUser().getScreenName());
     	// Display the container
 		userLayout.setVisibility(LinearLayout.VISIBLE);
 		tweetLayout.setVisibility(LinearLayout.GONE);
     }
     
     private Drawable getOrLoadAvatar(TwitterUser user) {
     	Drawable d = user.getAvatarImage();
     	if (d == null) d = getResources().getDrawable(R.drawable.default_avatar);
     	return d;
     }
     
     private void authorizeOrStartGame() {
     	if (authorized(CONSUMER)) {
         	if (game == null || !game.getSuccess()) startGame();
         } else {
         	authorize();  
         }
     }
     
     private void authorize() {
     	// Get authorization
     	String authUrl;
 		try {
 			authUrl = PROVIDER.retrieveRequestToken(CONSUMER, CALLBACK_URL);
 			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
 		} catch (OAuthMessageSignerException e) {
 			errorAlert(getString(R.string.oauth_failure));
 			debug(e);
 		} catch (OAuthNotAuthorizedException e) {
 			errorAlert(getString(R.string.oauth_failure));
 			debug(e);
 		} catch (OAuthExpectationFailedException e) {
 			errorAlert(getString(R.string.oauth_failure));
 			debug(e);
 		} catch (OAuthCommunicationException e) {
 			errorAlert(getString(R.string.oauth_failure));
 			debug(e);
 		}
     }
     
     private boolean authorized(AbstractOAuthConsumer consumer) {
     	if (consumer.getToken() != null && consumer.getTokenSecret() != null) {
     		return true;
     	} else {
     		return setupTokenAndSecretFromSavedValues(consumer);
     	}
     }
     
     private boolean setupTokenAndSecretFromSavedValues(AbstractOAuthConsumer consumer) {
     	Cursor cursor = db.getUsers();
     	if (cursor.moveToNext()) {
     		// We have the user saved
     		this.userId = cursor.getInt(0);
     		String token = cursor.getString(1);
     		String tokenSecret = cursor.getString(2);
     		if (this.userId == null || token == null || tokenSecret == null) {
     			// We have crappy data somehow
     			return false;
     		} else {
     			// We have the token and secret set aside, use it
     			consumer.setTokenWithSecret(token, tokenSecret);
     			return true;
     		}
     	} else {
     		// No user records
     		return false;
     	}
     }
     
     private void saveTokenAndSecret(AbstractOAuthConsumer consumer) {
     	int id = db.addOrUpdateUser(consumer.getToken(), consumer.getTokenSecret());
     	if (id != -1) {
     		this.userId = id;
     	}
     }
     
     private Handler handler = new Handler() {
     	@Override
     	public void handleMessage(Message message) {
     		switch(message.what) {
     		case INIT_MESSAGE:
     			progressDialog.dismiss();
         		initializing = false;
         		if (game == null) {
         			// Deauth problem, game got destroyed
         			break;
         		} else if (!game.getSuccess()) {
         			// Something went wrong during game initialization
         			errorAlert(game.getFormattedMessage());
         		} else {
         			showNextQuestion();
         		}
         		break;
     		case TIMER_MESSAGE:
     			timerLabel.setText(Integer.toString(message.arg1));
     			break;
     		case RESTART_MESSAGE:
     			timerLabel.setText(Integer.toString(GAME_LENGTH));
         		correctMarker.setImageDrawable(null);
         		scoreContainer.setText("0");
         		break;
     		}
     	}
     };
     
     private void errorAlert(String message) {
     	AlertDialog alertDialog = new AlertDialog.Builder(this).create();
     	alertDialog.setTitle(getString(R.string.error_alert_title));
     	alertDialog.setMessage(message);
     	alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
     	   public void onClick(DialogInterface dialog, int which) { }
     	});
     	alertDialog.show();
     }
     
     private void debug(String message) {
     	Log.d("Twittaddict", message);
     }
     
     private void debug(Exception e) {
     	debug(e.getClass().toString() + " error: " + e.getMessage());
     }
 }
