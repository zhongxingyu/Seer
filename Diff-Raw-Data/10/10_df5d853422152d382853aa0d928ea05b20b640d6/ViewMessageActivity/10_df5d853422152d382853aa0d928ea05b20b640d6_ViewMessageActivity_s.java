 package edu.utsa.cs.smsmessenger.activity;
 
 import java.text.SimpleDateFormat;
 
 import edu.utsa.cs.smsmessenger.R;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.FloatMath;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.TableLayout;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 import edu.utsa.cs.smsmessenger.model.ContactContainer;
 import edu.utsa.cs.smsmessenger.model.MessageContainer;
 import edu.utsa.cs.smsmessenger.util.ContactsUtil;
 import edu.utsa.cs.smsmessenger.util.SmsMessageHandler;
 
 public class ViewMessageActivity extends Activity {
 
 	private TableLayout rootTable;
 	private TextView txtMsgBody; // Where the body of the message being view is
 									// shown
 	private MessageContainer currentMessage; // Message structure that is being
 												// displayed in the View Message
 												// activity
 	private ContactContainer currentContact; // The contact associated with the
 												// current message being viewed
 	private SmsMessageHandler smsMessageHandler; // /handler used to the delete
 													// the message if chosen
 	private Context context; // Context - this - used for deleting messages
 
 	private String contactURI; // string used to get the contact image from the
 								// contacts app
 
 	// Touch event related variables
 	int touchState;
 	final int IDLE = 0; // no fingers touching
 	final int TOUCH = 1; // one finger touching
 	final int PINCH = 2; // two fingers touching
 	float dist0, distCurrent; // The distances used to figure out how much to
 								// zoom
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.conversation_to_message_item);
 
 		Bundle extras = getIntent().getExtras();
 
 		currentMessage = new MessageContainer();
 
 		// Fill in the message container with information from the message
 		// clicked
 		currentMessage.setBody(extras.getString("msgBody"));
 		currentMessage.setDate(extras.getLong("timeAndDate"));
 		currentMessage.setContactId(extras
 				.getInt(SmsMessageHandler.COL_NAME_CONTACT_ID));
 		currentMessage.setPhoneNumber(extras
 				.getString(SmsMessageHandler.COL_NAME_PHONE_NUMBER));
 		currentMessage.setType(extras.getString("msgType"));
 		currentMessage.setId(extras.getLong("msgID"));
 
 		currentContact = ContactsUtil.getContactByPhoneNumber(
 				this.getContentResolver(), currentMessage.getPhoneNumber());
 
 		updateUI();
 
 		distCurrent = 1; // Dummy default distance
 		dist0 = 1; // Dummy default distance
 
 		rootTable.setOnTouchListener(ZoomListener);
 
 		touchState = IDLE;
 
		context = this;
 
 		Log.d("ViewMessageActivity",
 				"view Message Activity: " + currentMessage.getBody() + " from "
 						+ currentMessage.getContactId());
 
 		return;
 	}
 
 	private void updateUI() {
 
 		setTitle(currentMessage.getType()
 				.equals(SmsMessageHandler.MSG_TYPE_OUT) ? getResources()
 				.getString(R.string.self_reference) : (currentContact
 				.getDisplayName() != null ? currentContact.getDisplayName()
 				: currentMessage.getPhoneNumber()));
 
 		txtMsgBody = (TextView) findViewById(R.id.msgBodyTextView);
 		txtMsgBody.setText(currentMessage.getBody());
 
		SimpleDateFormat sdf = new SimpleDateFormat(context.getResources()
				.getString(R.string.date_time_format), context.getResources()
 				.getConfiguration().locale);
 		TextView txtTimeAndDate = (TextView) findViewById(R.id.msgDateTextView);
 		txtTimeAndDate.setText(sdf.format(currentMessage.getDate()));
 
 		ImageView contactImageView = (ImageView) findViewById(R.id.msgImageView);
 
 		if (currentMessage.getType().equals(SmsMessageHandler.MSG_TYPE_IN)) {
 
 			if (currentContact.getPhotoUri() != null) {
 				contactImageView.setImageURI(Uri.parse(currentContact
 						.getPhotoUri()));
 				if (contactImageView.getDrawable() == null) {
 					contactImageView.setImageResource(R.drawable.hg_contact);
 				}
 			} else {
 				contactImageView.setImageResource(R.drawable.hg_contact);
 			}
 		} else {
 			contactImageView.setImageResource(R.drawable.me_icon);
 		}
 
 		rootTable = (TableLayout) findViewById(R.id.viewMsgTable);
 		// tableRow1
 
 		// Find the root view
 		View root = rootTable.getRootView();
 
 		if (currentMessage.getType().equals(SmsMessageHandler.MSG_TYPE_IN)) {
 			root.setBackgroundColor(getResources().getColor(
 					R.color.backroundColor));
 		} else {
 			root.setBackgroundColor(getResources().getColor(R.color.RowColor1));
 		}
 
 		return;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.view_msg_menu_list, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		Toast msg;
 
 		switch (item.getItemId()) {
 		case R.id.action_forward:
 			msg = Toast.makeText(this, R.string.forward_toast,
 					Toast.LENGTH_LONG);
 			msg.show();
 
 			Intent forwardIntent = new Intent(this,// Create intent for forward
 													// - the message body will
 													// already be filled
 					NewConversationActivity.class);
 
 			forwardIntent.putExtra("fwdBody", currentMessage.getBody());
 
 			startActivity(forwardIntent);
 
 			break;
 		case R.id.action_reply:
 			msg = Toast.makeText(this, R.string.reply_toast, Toast.LENGTH_LONG);
 			msg.show();
 
 			Intent replyIntent = new Intent(this, NewConversationActivity.class); // Create
 																					// intent
 																					// for
 																					// reply
 																					// -
 																					// the
 																					// number
 																					// will
 																					// already
 																					// be
 																					// filled
 
 			replyIntent.putExtra("replyContact",
 					currentMessage.getPhoneNumber());
 			startActivity(replyIntent);
 			break;
 		case R.id.action_delete:
 			try {
 
 				DialogInterface.OnClickListener DeleteOnClick = new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						// TODO Auto-generated method stub
 						switch (which) {
 						case DialogInterface.BUTTON_POSITIVE:
 							deleteMessage();
 							break;
 						case DialogInterface.BUTTON_NEGATIVE:
 							break;
 
 						}
 					}
 				};
 
 				AlertDialog.Builder b = new AlertDialog.Builder(this);
 				b.setTitle(R.string.delete_msg_confirmation);
 				b.setPositiveButton(R.string.acknowlege_descision,
 						DeleteOnClick);
 
 				b.setNegativeButton(R.string.decline_desicion, DeleteOnClick);
 				b.create().show();
 
 			} catch (Exception x) {
 				Log.d("ViewMessageActivity",
 						"Threw Error on delete:" + x.getMessage());
 			}
 
 			break;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void deleteMessage() {
 		Toast msg = Toast.makeText(this, R.string.delete_toast,
 				Toast.LENGTH_LONG);
 		msg.show();
 
 		MessageContainer[] msgArr = { currentMessage };
 		DeleteMessageFromDbTask deleteThread = new DeleteMessageFromDbTask();
 		deleteThread.execute(msgArr);
 
 		Intent converListIntent = new Intent(this,
 				ConversationsListActivity.class); // Create intent for the root
 													// screen
 		converListIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Should
 																	// make it
 																	// so that
 																	// you can't
 																	// go back
 																	// with back
 																	// button
 		startActivity(converListIntent);
 	}
 
 	private void updateFontSize() {
 		float newsize;
 		float curScale = distCurrent / dist0;
 		if (curScale < 0.1) {
 			curScale = 0.1f;
 		}
 
 		newsize = (12 * curScale); // 12 is the baseline font size - all
 									// alterations will be relative to this
 
 		if (newsize < 9) {
 			newsize = 9;
 		}
 
 		if (newsize > 56) {
 			newsize = 56;
 		}
 
 		txtMsgBody.setTextSize(newsize);
 	}
 
 	OnTouchListener ZoomListener = new OnTouchListener() {
 
 		@Override
 		public boolean onTouch(View view, MotionEvent event) {
 			// TODO Auto-generated method stub
 
 			float distx, disty;
 
 			switch (event.getAction() & MotionEvent.ACTION_MASK) {
 			case MotionEvent.ACTION_DOWN:
 				// A pressed gesture has started, the motion contains the
 				// initial starting location.
 				touchState = TOUCH;
 				break;
 			case MotionEvent.ACTION_POINTER_DOWN:
 				// A non-primary pointer has gone down.
 				touchState = PINCH;
 
 				// Get the distance when the second pointer touch
 				distx = event.getX(0) - event.getX(1);
 				disty = event.getY(0) - event.getY(1);
 				dist0 = FloatMath.sqrt(distx * distx + disty * disty);
 
 				break;
 			case MotionEvent.ACTION_MOVE:
 				// A change has happened during a press gesture (between
 				// ACTION_DOWN and ACTION_UP).
 
 				if (touchState == PINCH) {
 					// Get the current distance
 					distx = event.getX(0) - event.getX(1);
 					disty = event.getY(0) - event.getY(1);
 					distCurrent = FloatMath.sqrt(distx * distx + disty * disty);
 
 					updateFontSize();
 				}
 
 				break;
 			case MotionEvent.ACTION_UP:
 				// A pressed gesture has finished.
 
 				touchState = IDLE;
 				break;
 			case MotionEvent.ACTION_POINTER_UP:
 				// A non-primary pointer has gone up.
 				touchState = TOUCH;
 				break;
 			}
 
 			return true;
 		}
 
 	};
 
 	private class DeleteMessageFromDbTask extends
 			AsyncTask<MessageContainer, Void, Void> {
 		@Override
 		protected Void doInBackground(MessageContainer... objects) {
 			if (context != null) {
 				for (MessageContainer msg : objects) {
 					getSmsMessageHandler().deleteMessage(msg);
 				}
 				getSmsMessageHandler().close();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			Intent newMsgIntent = new Intent(
 					SmsMessageHandler.UPDATE_MSG_INTENT);
 			context.sendBroadcast(newMsgIntent);
 		}
 
 	}
 
 	private SmsMessageHandler getSmsMessageHandler() {
 		if (smsMessageHandler == null)
 			smsMessageHandler = new SmsMessageHandler(context);
 		return smsMessageHandler;
 	}
 
 }
