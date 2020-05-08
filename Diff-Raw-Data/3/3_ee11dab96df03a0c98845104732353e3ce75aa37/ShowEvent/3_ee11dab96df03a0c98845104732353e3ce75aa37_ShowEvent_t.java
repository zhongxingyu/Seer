 package edu.upenn.cis350;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.parse.FindCallback;
 import com.parse.GetCallback;
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 import com.parse.PushService;
 import com.parse.SaveCallback;
 
 /* Displays all information related to a particular Event as well as messages 
  * related to that event. Upon clicking on a message, the user will see
  * a new view where they can comment on a particular message.
  */
 public class ShowEvent extends Activity {
 
 	private ParseObject event;
 	private ProgressDialog dialog;
 	private List<ListItem> items = new ArrayList<ListItem>();
 
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.show_event);
 		Parse.initialize(this, "FWyFNrvpkliSb7nBNugCNttN5HWpcbfaOWEutejH", "SZoWtHw28U44nJy8uKtV2oAQ8suuCZnFLklFSk46");
 		Bundle extras = this.getIntent().getExtras();
 		if(extras != null){
 			ParseQuery query = new ParseQuery("Event");
 
 			final Toast toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
 			dialog = ProgressDialog.show(this, "", 
 					"Loading. Please wait...", true);
 			dialog.setCancelable(true);
 			final ListView list = (ListView) findViewById(R.id.messagesList);
 			query.getInBackground(extras.getString("eventKey"), new GetCallback() {
 
 				@Override
 				public void done(ParseObject event1, ParseException e) {
 					if (event1 == null) {
 						dialog.cancel();
 						toast.setText(e.getMessage());
 						toast.show();
 					} else {
 						event = event1;
 
 						items.add(new ListItem(event, false, ItemType.EVENT));
 						items.add(new ListItem(event, false, ItemType.MESSAGEBOX));
 						populateMessages();
 					}
 				}
 
 			});
 		}
 	}
 
 	/**
 	 * Populate Messages List in Bottom Half of Activity from ParseDB
 	 */
 	public void populateMessages() {
 		final ListView msgList = (ListView) findViewById(R.id.messagesList);
 		ParseQuery query = new ParseQuery("Message");
 		query.orderByAscending("timestamp");
 		query.whereEqualTo("event", event.getObjectId());
 		query.findInBackground(new FindCallback() {
 
 			@Override
 			public void done(List<ParseObject> messages, ParseException e) {
 				if(e == null){
 
 					for(ParseObject obj : messages){
 						items.add(new ListItem(obj, false, ItemType.MESSAGE));
 					}
 					msgList.setAdapter(new NewListAdapter(getApplicationContext(), 
 							items));
 					dialog.cancel();
 				} else {
 					Toast.makeText(getApplicationContext(), 
 							"Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
 				}
 
 			}
 
 		});
 		//LinearLayout messagesPane = (LinearLayout) findViewById(R.id.messagesPane);
 		//messagesPane.removeAllViews();
 		//messagesPane.addView(messageFrame);
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
 		inflater.inflate(R.menu.show_event_menu, menu);
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
 			finish();
 			startActivity(i);
 			return true;
 		}
 		else if(item.getItemId() == R.id.markComplete){
 			event.put("type", "Resolved");
 			event.saveInBackground(new SaveCallback(){
 
 				@Override
 				public void done(ParseException arg0) {
 					PushUtils.createEventResolvedPush(event.getObjectId(), event);
 					Toast temp = Toast.makeText(getApplicationContext(), "Marked as Resolved", Toast.LENGTH_SHORT);
 					temp.show();
 				}
 
 			});
 			return true;
 		} else if (item.getItemId() == R.id.eventSubscribe) {
 			if (!PushService.getSubscriptions(this).contains("push_" + event.getObjectId())) {
 				PushService.subscribe(this, "push_" + event.getObjectId(), Login.class);
 			}
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
 				if(e == null){
 					success.show();
 					Context context = getApplicationContext();
 					if (!PushService.getSubscriptions(context).contains("push_" + msg.getObjectId())) {
 						PushService.subscribe(context, "push_" + msg.getObjectId(), Login.class);
 					}
 					PushUtils.createMessagePush(event, msg);
 					i.putExtra("eventKey", event.getObjectId());
 					//TODO(kuyumcu): Replace starting the activity over with just refetching the messages.
 					finish();
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
 	 * Adapter for formatting the Events into ListView items
 	 * 
 	 * @author JMow
 	 * 
 	 */
 	private class NewListAdapter extends ArrayAdapter<ListItem> {
 
 		private List<ListItem> events;
 
 		public NewListAdapter(Context context, List<ListItem> events) {
 			super(context, 0, events);
 			this.events = events;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
			// if not null, it has already been populated - helps it from being slow
			if(convertView != null)
				return convertView;
 			LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 			final ListItem item = events.get(position);
 
 			if (item != null) {
 				// Not used right now
 				if (item.isSection()) {
 					/* This is a section header */
 					String title = (String) item.getData();
 					v = vi.inflate(R.layout.list_divider, null);
 
 					v.setOnClickListener(null);
 					v.setOnLongClickListener(null);
 					v.setLongClickable(false);
 
 					final TextView sectionView = (TextView) v.findViewById(R.id.list_item_section_text);
 					sectionView.setText(title);
 
 				} else if (item.getType().equals(ItemType.MESSAGE)){
 					/* This is a real list item */
 					final ParseObject message = (ParseObject) item.getData();
 					v = vi.inflate(R.layout.message_list_item, null);
 
 					TextView temp = (TextView) v.findViewById(R.id.listMessageText);
 					if (temp != null) {
 						temp.setText(message.getString("text"));
 					}
 
 					temp = (TextView) v.findViewById(R.id.listMessageAuthor);
 					if (temp != null) {
 						ParseUser author;
 						try {
 							author = (ParseUser) message.getParseUser("author").fetch();
 							temp.setText(author.getString("fullName"));
 							temp.setTypeface(Typeface.DEFAULT_BOLD);
 						} catch (ParseException e) {
 							e.printStackTrace();
 						}
 					}
 					temp = (TextView) v.findViewById(R.id.listMessageTimestamp);
 					if (temp != null) {
 						Long time = message.getLong("timestamp");
 						final SimpleDateFormat formatter = new SimpleDateFormat("MMMM d 'at' h:mm a ");
 						temp.setText(formatter.format(new Date(time)));
 					}
 					temp = (TextView) v.findViewById(R.id.listMessageCommentCounter);
 					if (temp != null) {
 						int noOfComments = message.getInt("count");
 						if(noOfComments > 0)
 							temp.setText(noOfComments + " comment" + (noOfComments == 1 ? "" : "s") + '\n');
 						else
 							temp.setText("" + '\n');
 					}
 
 					v.setOnClickListener(new OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							Intent i = new Intent(getApplicationContext(), ShowMessage.class);
 							i.putExtra("messageID", message.getObjectId());
 							startActivity(i);
 						}
 					});
 				} else if (item.getType().equals(ItemType.EVENT)){
 					/* This is a real list item */
 					final ParseObject event = (ParseObject) item.getData();
 					v = vi.inflate(R.layout.event_description_item, null);
 					v.setOnClickListener(null);
 					v.setOnLongClickListener(null);
 					v.setLongClickable(false);
 
 					TextView temp = (TextView) v.findViewById(R.id.eventTitleText);
 					if (temp != null) {
 						temp.setText(event.getString("title"));
 					}
 
 					temp = (TextView) v.findViewById(R.id.eventDescText);
 					if (temp != null) {
 						temp.setText("\n" + event.getString("description") + "\n");
 					}
 
 					temp = (TextView)v.findViewById(R.id.startDateDisplay2);
 					SimpleDateFormat formatter = new SimpleDateFormat();
 					if(temp != null){
 						Date date1 = new Date(event.getLong("startDate"));
 						temp.setText(formatter.format(date1));
 						
 					}
 					
 					temp = (TextView)v.findViewById(R.id.endDateDisplay2);
 					if(temp != null){
 						Date date2 = new Date(event.getLong("endDate"));
 						temp.setText(formatter.format(date2));
 					}
 					
 					temp = (TextView)v.findViewById(R.id.affilsText);
 					if(temp != null){
 						List<String> affilList = event.getList("groups");
 						StringBuilder affilText = new StringBuilder();
 						if(affilList != null){
 							for(String s : affilList){
 								affilText.append(s + "\t");
 							}
 							temp.setText(affilText.toString());
 						}
 					}
 					
 					temp = (TextView)v.findViewById(R.id.systemsText);
 					if(temp != null){
 						List<String> systemList = event.getList("systems");
 						StringBuilder systemText = new StringBuilder();
 						if(systemList != null){
 							for(String s : systemList){
 								systemText.append(s + "\t");
 							}
 							temp.setText(systemText.toString());
 						}
 					}
 
 					temp = (TextView)v.findViewById(R.id.personText1);
 					if(temp != null){
 						temp.setText(event.getString("contact1"));
 						temp.setTextColor(Color.WHITE);
 					}
 
 					temp = (TextView)v.findViewById(R.id.personText2);
 					if(temp != null){
 						temp.setText(event.getString("contact2"));
 						temp.setTextColor(Color.WHITE);
 					}
 
 					temp = (TextView)v.findViewById(R.id.severityText);
 					if(temp != null){
 						temp.setBackgroundColor(event.getInt("severity"));
 					}
 					
 					temp = (TextView)v.findViewById(R.id.typeText);
 					if(temp != null){
 						temp.setText(event.getString("type"));
 					}
 				} else if (item.getType().equals(ItemType.MESSAGEBOX)){
 					v = vi.inflate(R.layout.post_message_item, null);
 					EditText temp = (EditText) v.findViewById(R.id.newMessageText);
 					//we need to update adapter once we finish with editing
 
 					if (temp != null) {
 						temp.setFocusable(true);
 					}
 				}
 			}
 			return v;
 		}
 	}
 }
