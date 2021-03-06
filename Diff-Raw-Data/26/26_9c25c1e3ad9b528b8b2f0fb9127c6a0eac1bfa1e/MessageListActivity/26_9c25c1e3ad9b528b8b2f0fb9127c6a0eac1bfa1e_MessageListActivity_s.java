 package edu.berkeley.cs169.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.widget.ArrayAdapter;
 import edu.berkeley.cs169.BlindlyMessenger;
 import edu.berkeley.cs169.R;
 import edu.berkeley.cs169.model.ContactModel;
 import edu.berkeley.cs169.model.ConversationModel;
 import edu.berkeley.cs169.model.MessageModel;
 import edu.berkeley.cs169.util.NavigationKeyInterpreter;
 import edu.berkeley.cs169.util.NavigationKeyInterpreter.NavigationKeyInterpreterResultListener;
 
 public class MessageListActivity extends ListActivity implements
 		NavigationKeyInterpreterResultListener {
 
 	BlindlyMessenger app;
 	private NavigationKeyInterpreter keyInterpreter;
 	ArrayList<MessageModel> messageList;
 	ArrayList<ConversationModel> conversationList;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		app = (BlindlyMessenger) getApplication();
 
 		setContentView(R.layout.list);
 		// ListView list = (ListView) findViewById(R.id.list);
 
 		// Register handler for UI elements
 		keyInterpreter = new NavigationKeyInterpreter(this, 200, 5);
 
 		// populate the lists
 		messageList = new ArrayList<MessageModel>();
 		conversationList = new ArrayList<ConversationModel>();
 
 		populateMessageList();
 
		Log.d("test", "Populated message list");
 
 		populateConversationList();
 
 		ArrayList<String> items = new ArrayList<String>();
 		for (ConversationModel model : conversationList) {
 			items.add(model.toString());
 		}
 
 		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, items));
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		// TODO vibrate shortcode and speak greeting
 
 		// Utils.blankScreen(this);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyInterpreter.onKeyDown(keyCode, event)) {
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		if (keyInterpreter.onKeyUp(keyCode, event)) {
 			return true;
 		}
 		return super.onKeyUp(keyCode, event);
 	}
 
 	public void onNavKeyInterpreterResult(ResultCode code) {
 		switch (code) {
 		case UP:
 		case UP_REPEAT:
 			dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
 					KeyEvent.KEYCODE_DPAD_UP));
 			dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
 					KeyEvent.KEYCODE_DPAD_UP));
 			break;
 		case UP_REPEAT_LONG:
 			for (int i = 0; i < keyInterpreter.getKeyRepeatLongThreshold(); i++) {
 				dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
 						KeyEvent.KEYCODE_DPAD_UP));
 				dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
 						KeyEvent.KEYCODE_DPAD_UP));
 			}
 			break;
 		case DOWN:
 		case DOWN_REPEAT:
 			dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
 					KeyEvent.KEYCODE_DPAD_DOWN));
 			dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
 					KeyEvent.KEYCODE_DPAD_DOWN));
 			break;
 		case DOWN_REPEAT_LONG:
 			for (int i = 0; i < keyInterpreter.getKeyRepeatLongThreshold(); i++) {
 				dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
 						KeyEvent.KEYCODE_DPAD_DOWN));
 				dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
 						KeyEvent.KEYCODE_DPAD_DOWN));
 			}
 			break;
 		case UP_AND_DOWN:
 			readMessage();
 			break;
 		case UP_AND_DOWN_LONG:
 			starthelp();
 			break;
 		}
 	}
 
 	private void starthelp() {
 		String alert = getResources().getString(R.string.message_input_help);
 
 		app.output(alert);
 	}
 
 	private void populateMessageList() {
 		messageList.clear();
 		int counter = 0;
 		ContactModel from;
 		ContactModel me;
 		MessageModel messageModel;
 		Cursor cursor = getContentResolver().query(Uri.parse("content://sms"),
 				null, null, null, null);
 		startManagingCursor(cursor);
 		while (cursor.moveToNext() && (counter < 200)) {
 			String number = cursor.getString(cursor.getColumnIndex("address"));
 			String body = cursor
 					.getString(cursor.getColumnIndexOrThrow("body"));
 			String type = cursor
 					.getString(cursor.getColumnIndexOrThrow("type"));
 			String name = app.getNameForNumber(number);
 
 			// received
 			if (type.equals("1")) {
 				from = new ContactModel(name, number);
 				me = app.getMyContact();
 				messageModel = new MessageModel(body, from, me);
 				messageList.add(messageModel);
 				// Log.d("test", messageModel.toString());
 				// sent
 			} else if (type.equals("2")) {
 				me = new ContactModel(name, number);
 				from = app.getMyContact();
 				messageModel = new MessageModel(body, from, me);
 				messageList.add(messageModel);
 				// Log.d("test", messageModel.toString());
 			} else if (type.equals("3")) {
 
 			}
 
 			counter++;
 
 		}
 
 	}
 
 	private void populateConversationList() {
 		conversationList.clear();
 
 		for (int i = 0; i < messageList.size(); i++) {
 			MessageModel message = messageList.get(i);
 			ContactModel other;
			if (message.getFrom().equals(app.getMyContact())) {
 				other = message.getTo();
 			} else {
 				other = message.getFrom();
 			}
 			boolean inserted = false;
 			for (int j = 0; j < conversationList.size(); j++) {
 				ConversationModel conversation = conversationList.get(j);
				if (conversation.getOther().equals(other)) {
 					// it belongs here
					conversation.getMessages().add(message);
 					Log.d("test",
 							"Put into existing: " + conversation.toString());
 					inserted = true;
 					break;
 				}
 			}
 
 			if (!inserted) {
 				// add new conversation to list
 				List<MessageModel> messages = new ArrayList<MessageModel>();
 				messages.add(message);
 				ConversationModel conversation = new ConversationModel(
 						messages, other);
 				conversationList.add(conversation);
				Log.d("test", "Put into new: " + conversation.toString());
 			}
 		}
 	}
 
 	private void readMessage() {
 
 	}
 
 }
