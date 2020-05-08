 package edu.upenn.cis350;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.parse.FindCallback;
 import com.parse.GetCallback;
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParsePush;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 import com.parse.PushService;
 import com.parse.SaveCallback;
 
 /* This activity displays the comments related to a particular message.
  * Each message has its own comments related to it.
  */
 public class ShowMessage extends Activity {
 
 	private String msgId;
 	private ParseObject message;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.showcomments);
 		Parse.initialize(this, "FWyFNrvpkliSb7nBNugCNttN5HWpcbfaOWEutejH", "SZoWtHw28U44nJy8uKtV2oAQ8suuCZnFLklFSk46");
 		
 		Bundle extras = this.getIntent().getExtras();
 
 		if (extras == null){
 			Toast.makeText(this, "Could not load event.", Toast.LENGTH_LONG);
 			return;
 		} else {
 			msgId = extras.getString("messageID");
 			ParseQuery msgQuery = new ParseQuery("Message");
 			final Toast toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
 			msgQuery.getInBackground(msgId, new GetCallback() {
 
 				@Override
 				public void done(ParseObject msg, ParseException e) {
 					if (msg == null) {
 						toast.setText("Error: " + e.getMessage());
 						toast.show();
 						return;
 					} else {
 						message = msg;
 						LinearLayout layout = (LinearLayout) findViewById(R.id.commentMessagePane);
 						layout.setBackgroundColor(Color.GRAY);
 
 						TextView temp = (TextView)findViewById(R.id.messageText);
 						temp.setTextColor(Color.WHITE);
 						temp.setText(msg.getString("text"));
 
 						final TextView authorView = (TextView) findViewById(R.id.messageAuthor);
 						authorView.setTextColor(Color.WHITE);
 						//TODO(kuyumcu): change to ParseUser
 						msg.getParseUser("author").fetchIfNeededInBackground(new GetCallback(){
 
 							@Override
 							public void done(ParseObject arg0, ParseException arg1) {
 								// TODO Auto-generated method stub
 								ParseUser user = (ParseUser)arg0;
 								String author = user.getUsername();
 								authorView.setText("Posted by " + author + " at ");
 							}
 
 						});
 
 						temp = (TextView) findViewById(R.id.messageTimestamp);
 						temp.setTextColor(Color.WHITE);
 						SimpleDateFormat formatter = new SimpleDateFormat();
 						temp.setText(formatter.format(new Date(msg.getLong("timestamp"))));
 
 						getComments(msg);
 					}
 				}
 
 			});
 
 		}
 	}
 	
 	
 	/**
 	 * Post a new comment to this message
 	 * 
 	 * @param view
 	 */
 	public void onPostComment(View view) {
 		final ParseObject comment = new ParseObject("Comment");
 
 		final EditText commentText = (EditText) findViewById(R.id.newCommentText);
 		if (commentText.getText().toString().equals("")) {
 			Toast.makeText(this, "Please enter a comment.", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		message.increment("count");
 		comment.put("text", commentText.getText().toString());
 		comment.put("message", message);
 		comment.put("author", ParseUser.getCurrentUser());
 		comment.put("timestamp", System.currentTimeMillis());
 
 		final Toast toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
 		
 		comment.saveInBackground(new SaveCallback() {
 
 			@Override
 			public void done(ParseException e) {
 				if (e != null) {
 					toast.setText("Error: " + e.getMessage());
 					toast.show();
 					return;
 				} else {
					message.saveInBackground();
 					toast.setText("Comment posted");
 					toast.show();
 					createPush(message.getObjectId().toString(), comment);
 					commentText.setText("");
 					PushService.subscribe(getApplicationContext(), message.getObjectId().toString(), Login.class);
 					getComments(message);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Creates a push notification for this comment
 	 */
 	public void createPush(String messageId, ParseObject comment) {
 		ParsePush pushMessage = new ParsePush();
 		ParseUser user = ParseUser.getCurrentUser();
 		pushMessage.setChannel(messageId);
 		pushMessage.setMessage(user.getString("fullName") + " just commented on a message.");
 		// expire after 5 days
 		pushMessage.setExpirationTimeInterval(432000);
 		pushMessage.sendInBackground();
 	}
 
 	/**
 	 * Creates a UI frame to display comments
 	 * 
 	 * @param comment A ParseObject representing a comment to be displayed
 	 * @return LinearLayout representing the comment
 	 */
 	public LinearLayout createCommentFrame(ParseObject comment) {
 		LinearLayout commentFrame = new LinearLayout(this);
 		commentFrame.setOrientation(1);
 		commentFrame.setPadding(1, 1, 1, 1);
 
 		LinearLayout header = new LinearLayout(this);
 		header.setOrientation(0);
 
 		final TextView author = new TextView(this);
 		comment.getParseUser("author").fetchIfNeededInBackground(new GetCallback(){
 
 			@Override
 			public void done(ParseObject arg0, ParseException arg1) {
 				// TODO Auto-generated method stub
 				ParseUser user = (ParseUser)arg0;
 				author.setText(user.getUsername());
 				author.setTypeface(Typeface.DEFAULT_BOLD);
 			}
 
 		});
 
 		TextView timestamp = new TextView(this);
 		long time = comment.getLong("timestamp");
 		SimpleDateFormat formatter = new SimpleDateFormat();
 		timestamp.setText(" at " + formatter.format(new Date(time)));
 
 		header.addView(author);
 		header.addView(timestamp);
 
 		TextView commentText = new TextView(this);
 		commentText.setText(comment.getString("text"));
 
 		commentFrame.addView(header);
 		commentFrame.addView(commentText);
 
 		return commentFrame;
 	}
 
 	/**
 	 * Get list of comments for this message
 	 * 
 	 * @param message ParseObject representing the message to get comments for
 	 * @return List of ParseObjects representing comments
 	 */
 	public void getComments(ParseObject message) {
 		ParseQuery commentQuery = new ParseQuery("Comment");
 		commentQuery.addAscendingOrder("timestamp");
 		commentQuery.whereEqualTo("message", message);
 
 		final Toast toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
 		commentQuery.findInBackground(new FindCallback() {
 
 			@Override
 			public void done(List<ParseObject> comments, ParseException e) {
 				if (e != null) {
 					toast.setText("Error: " + e.getMessage());
 					toast.show();
 					return;
 				} else {
 					LinearLayout commentsPane = (LinearLayout) findViewById(R.id.commentsPane);
 					commentsPane.removeAllViews();
 					for (ParseObject c : comments) {
 						LinearLayout commentFrame = createCommentFrame(c);
 						commentsPane.addView(commentFrame);
 					}
 				}
 			}
 
 		});
 	}
 
 }
