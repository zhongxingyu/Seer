 package edu.upenn.cis350;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
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
 
 /* Displays all information related to a particular Event as well as messages 
  * related to that event. Upon clicking on a message, the user will see
  * a new view where they can comment on a particular message.
  */
 public class ShowEvent extends Activity {
 
 	private String uname;
 	private ParseObject event;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.show_event);
 		Parse.initialize(this, "FWyFNrvpkliSb7nBNugCNttN5HWpcbfaOWEutejH", "SZoWtHw28U44nJy8uKtV2oAQ8suuCZnFLklFSk46");
 		Bundle extras = this.getIntent().getExtras();
 		if(extras != null){
 			//event = (EventPOJO)extras.get("eventPOJO");
 			//uname = extras.getString("user");
 			ParseQuery query = new ParseQuery("Event");
 
 			final Toast toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
 			query.getInBackground(extras.getString("eventKey"), new GetCallback() {
 
 				@Override
 				public void done(ParseObject event1, ParseException e) {
 					if (event1 == null) {
 						toast.setText(e.getMessage());
 						toast.show();
 					} else {
 						event = event1;
 						TextView temp = (TextView)findViewById(R.id.eventTitleText);
 						temp.setText(event.getString("title"));
 						temp = (TextView)findViewById(R.id.eventDescText);
 						temp.setText("\n" + event.getString("description") + "\n");
 						//temp = (TextView)findViewById(R.id.eventActionsText);
 						//temp.setText(event.getString("actionItems" + "\n"));
 						temp = (TextView)findViewById(R.id.startDateDisplay2);
 						SimpleDateFormat formatter = new SimpleDateFormat();
 						Date date1 = new Date(event.getLong("startDate"));
 						temp.setText(formatter.format(date1));
 						temp = (TextView)findViewById(R.id.endDateDisplay2);
 						Date date2 = new Date(event.getLong("endDate"));
 						temp.setText(formatter.format(date2));
 						temp = (TextView)findViewById(R.id.affilsText);
 
 						List<String> affilList = event.getList("affils");
 						StringBuilder affilText = new StringBuilder();
 						if(affilList != null){
 							for(String s : affilList){
 								affilText.append(s + "\t");
 							}
 							temp.setText(affilText.toString());
 						}
 						temp = (TextView)findViewById(R.id.systemsText);
 
 						List<String> systemList = event.getList("systems");
 						StringBuilder systemText = new StringBuilder();
 						if(systemList != null){
 							for(String s : systemList){
 								systemText.append(s + "\t");
 							}
 							temp.setText(systemText.toString());
 						}
 
 						temp = (TextView)findViewById(R.id.personText1);
 						temp.setText(event.getString("contact1"));
 						temp.setTextColor(Color.WHITE);
 
 						temp = (TextView)findViewById(R.id.personText2);
 						temp.setText(event.getString("contact2"));
 						temp.setTextColor(Color.WHITE);
 
 						temp = (TextView)findViewById(R.id.severityText);
 						temp.setBackgroundColor(event.getInt("severity"));
 						temp = (TextView)findViewById(R.id.typeText);
 						temp.setText(event.getString("type"));
 						populateMessages();
 					}
 				}
 
 			});
 
 			/*
         	CharSequence[] temp2 = extras.getCharSequenceArray("affils");
         	boolean[] temp3 = extras.getBooleanArray("affilsChecked");
         	StringBuilder affilText = new StringBuilder();
         	if(temp2 != null && temp3 != null){
         		for(int i = 0; i < temp2.length; i++){
         			if(temp3[i])
         				affilText.append(temp2[i] + "\t");
         		}
         	}
 			 */
 
 			/*
         	temp2 = extras.getCharSequenceArray("systems");
         	temp3 = extras.getBooleanArray("systemsChecked");
         	StringBuilder systemText = new StringBuilder();
         	if(temp2 != null && temp3 != null){
         		for(int i = 0; i < temp2.length; i++){
         			if(temp3[i])
         				systemText.append(temp2[i] + "\t");
         		}
         	}
 			 */
 
 
 
 
 
 
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		Intent i = new Intent(this, Agenda.class);
 		/*
        if(event != null)
     	   i.putExtra("eventPOJO", event);
        i.putExtra("user", uname);
 		 */
 		startActivity(i);
 	}
 
 	// onClick function of backToAgenda button (deprected)
 	public void onBackToAgendaClick(View view){
 		Intent i = new Intent(this, Agenda.class);
 		startActivity(i);
 	}
 
 	// populates the messages in the bottom half of the view from the DB
 	public void populateMessages() {
 
 		final LinearLayout messagesPane = (LinearLayout) findViewById(R.id.messagesPane);
 		messagesPane.removeAllViews();
 		final Toast toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
 		ParseQuery query = new ParseQuery("Message");
 		query.orderByAscending("timestamp");
 		query.whereEqualTo("event", event.getObjectId());
 		query.findInBackground(new FindCallback() {
 
 			@Override
 			public void done(List<ParseObject> arg0, ParseException arg1) {
 				if(arg1 == null){
 					for(ParseObject obj : arg0){
 						LinearLayout messageFrame = getMessageFrame(obj);
 
 						messagesPane.addView(messageFrame);
 						toast.setText("Retrieved " + arg0.size() + " messages");
 						toast.show();
 					}
 				}
 				else {
 					toast.setText("Error: " + arg1.getMessage());
 					toast.show();
 				}
 
 			}
 
 		});
 		//LinearLayout messagesPane = (LinearLayout) findViewById(R.id.messagesPane);
 		//messagesPane.removeAllViews();
 		//messagesPane.addView(messageFrame);
 	}
 
 	public LinearLayout getMessageFrame(final ParseObject obj){
 		LinearLayout messageFrame = new LinearLayout(this);
 		messageFrame.setOrientation(1);
 		messageFrame.setPadding(1, 1, 1, 1);
 
 		LinearLayout header = new LinearLayout(this);
 		header.setOrientation(0);
 
 		TextView posted = new TextView(this);
 		posted.setText("Posted by ");
 
 		final TextView author = new TextView(this);
 		obj.getParseUser("author").fetchIfNeededInBackground(new GetCallback(){
 
 			@Override
 			public void done(ParseObject arg0, ParseException arg1) {
 				// TODO Auto-generated method stub
 				ParseUser user = (ParseUser)arg0;
 				author.setText(user.getString("fullName"));
 				author.setTypeface(Typeface.DEFAULT_BOLD);
 			}
 
 		});
 		//author.setText(user.getUsername());
 		//author.setTypeface(Typeface.DEFAULT_BOLD);
 
 		TextView timestamp = new TextView(this);
 		Long time = obj.getLong("timestamp");
 		SimpleDateFormat formatter = new SimpleDateFormat();
 		timestamp.setText(" at " + formatter.format(new Date(time)));
 
 		TextView comments = new TextView(this);
 		int noOfComments = obj.getInt("count");
 		if(noOfComments > 0)
 			comments.setText(noOfComments + " comment" + (noOfComments == 1 ? "" : "s") + '\n');
 		else
 			comments.setText("No comments" + '\n');
 
 		header.addView(posted);
 		header.addView(author);
 		header.addView(timestamp);
 
 		TextView messageText = new TextView(this);
 		messageText.setText(obj.getString("text"));
 		messageText.setTypeface(Typeface.DEFAULT_BOLD);
 
 		messageFrame.addView(messageText);
 		messageFrame.addView(header);
 		messageFrame.addView(comments);
 
 		final Intent i = new Intent(this, ShowMessage.class);
 
 		messageFrame.setOnClickListener(new LinearLayout.OnClickListener() {  
 			public void onClick(View v){
 				i.putExtra("messageID", obj.getObjectId());
 				startActivity(i);
 			}
 		});
 
 		return messageFrame;
 	}
 
 	/**
 	 * On Click Function of contact1 textView
 	 */
 	public void goToContact1(View view){
 		Intent i = new Intent(this, ShowContact.class);
 		i.putExtra("contactID", event.getString("contact1ID"));
 		startActivity(i);
 	}
 
 	/**
 	 * On Click Function of contact2 textView
 	 */
 	public void goToContact2(View view){
 		Intent i = new Intent(this, ShowContact.class);
 		i.putExtra("contactID", event.getString("contact2ID"));
 		startActivity(i);
 	}
 
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.showeventmenu, menu);
 		return true;
 	}
 
 	/**
 	 * Method that gets called when the menuitem is clicked
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if(item.getItemId() == R.id.editEvent){
 			Intent i = new Intent(this, EditEvent.class);
 			i.putExtra("eventKey", event.getObjectId());
 			startActivity(i);
 			return true;
 		}
 		else if(item.getItemId() == R.id.markComplete){
 			event.put("type", "Resolved");
 			event.saveInBackground(new SaveCallback(){
 
 				@Override
 				public void done(ParseException arg0) {
 					createEventUpdatePush(event.getObjectId(), event);
 					Toast temp = Toast.makeText(getApplicationContext(), "Marked as Resolved", Toast.LENGTH_SHORT);
 					temp.show();
 				}
 
 			});
 			return true;
 		} else if (item.getItemId() == R.id.eventSubscribe) {
 			PushService.subscribe(getApplicationContext(), "push_" + event.getObjectId(), Login.class);
 			Toast.makeText(this, "Subscribed to event", Toast.LENGTH_SHORT).show();
 			return true;
 		} else if (item.getItemId() == R.id.eventUnsubscribe) {
 			PushService.unsubscribe(getApplicationContext(), "push_" + event.getObjectId());
 			Toast.makeText(this, "Unsubscribed from event", Toast.LENGTH_SHORT).show();
 		}
 		return false;
 	}
 
 	/**
 	 * Posts a message
 	 * @param view
 	 */
 	public void onPostClick(View view){
 		TextView tv = (TextView)findViewById(R.id.newMessageText);
 		if (tv.getText().toString().equals("")) {
 			Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		final ParseObject msg = new ParseObject("Message");
 		msg.put("author", ParseUser.getCurrentUser());
 		msg.put("text", tv.getText().toString());
 		msg.put("timestamp", System.currentTimeMillis());
 		msg.put("event", event.getObjectId());
 		msg.put("count", 0);
 		final Toast success = Toast.makeText(this, "Message posted.", Toast.LENGTH_SHORT);
 		final Toast failure = Toast.makeText(this, "Message NOT posted.", Toast.LENGTH_SHORT);
 
 		final Intent i = new Intent(this, ShowEvent.class);
 
 		msg.saveInBackground(new SaveCallback(){
 
 			@Override
 			public void done(ParseException e) {
 				// TODO Auto-generated method stub
 				if(e == null){
 					success.show();
					PushService.subscribe(getApplicationContext(), "push_" + msg.getObjectId(), Login.class);
 					createMessagePush(event, msg);
 					i.putExtra("eventKey", event.getObjectId());
 					startActivity(i);
 				}
 				else{
 					failure.setText(e.getMessage());
 					failure.show();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Creates a push notification for this message
 	 * 
 	 * @param eventId The messageID that this message is posted on
 	 * @param message The message parse object.
 	 */
 	public void createMessagePush(ParseObject event, ParseObject message) {
 		ParsePush pushMessage = new ParsePush();
 		ParseUser user = ParseUser.getCurrentUser();
 		pushMessage.setChannel("push_" + event.getObjectId());
		pushMessage.setMessage(user.getString("fullName") + " posted: \"" + message.getString("text") + "\" on " +
 				"the event \"" + event.getString("title") + "\"");
 		// expire after 5 days
 		pushMessage.setExpirationTimeInterval(432000);
 		pushMessage.sendInBackground();
 	}
 
 	/**
 	 * Creates a push notification for Resolving event
 	 * @param eventId Event ID for event that is Resolved
 	 * @param event Event that is resolved
 	 */
 	public void createEventUpdatePush(String eventId, ParseObject event) {
 		ParsePush pushMessage = new ParsePush();
 		ParseUser user = ParseUser.getCurrentUser();
 		pushMessage.setChannel("push_" + eventId);
 		pushMessage.setMessage(user.getString("fullName") + " marked \"" + event.getString("title") + "\" as Resolved.");
 		// expire after 5 days
 		pushMessage.setExpirationTimeInterval(432000);
 		pushMessage.sendInBackground();
 	}
 }
