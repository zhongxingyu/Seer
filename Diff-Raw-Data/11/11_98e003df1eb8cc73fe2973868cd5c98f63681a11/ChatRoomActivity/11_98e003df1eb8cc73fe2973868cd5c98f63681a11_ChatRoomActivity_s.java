 package realtalk.activities;
 
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import realtalk.controller.ChatController;
 import realtalk.util.ChatRoomInfo;
 import realtalk.util.CommonUtilities;
 import realtalk.util.MessageInfo;
 import realtalk.util.ChatManager;
 import realtalk.util.PullMessageResultSet;
 import realtalk.util.RequestResultSet;
 import realtalk.util.UserInfo;
 
 import com.realtalk.R;
 
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Activity for a chat room, where the user can send/recieve messages.
  * 
  * @author Jordan Hazari
  *
  */
 @TargetApi(Build.VERSION_CODES.HONEYCOMB)
 public class ChatRoomActivity extends Activity {
 	ChatRoomInfo chatroominfo;
 	UserInfo userinfo;
 	private ProgressDialog progressdialog;
 	List<MessageInfo> rgmessageinfo = new ArrayList<MessageInfo>();
 	List<String> rgstDisplayMessage;
 	MessageAdapter adapter;
 	private ChatController chatController = ChatController.getInstance();
 	
 	/**
 	 * Sets up the chat room activity and loads the previous
 	 * messages from the chat room
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_chat_room);
 		
 		Bundle extras = getIntent().getExtras();
 		userinfo = ChatController.getInstance().getUser();
 		chatroominfo = extras.getParcelable("ROOM");
 		
 		new RoomCreator(this, chatroominfo).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 		
 		rgstDisplayMessage = new ArrayList<String>();
 
 		ListView listview = (ListView) findViewById(R.id.list);
 		adapter = new MessageAdapter(this, R.layout.list_item, rgmessageinfo);
 		listview.setAdapter(adapter);
 	}
 	
 	@Override
 	protected void onStart() {
 	    super.onStart();
 	    registerReceiver(handleNewMessageAlertReceiver,
 	            new IntentFilter(CommonUtilities.NEW_MESSAGE_ALERT));
 	}
 	
 	@Override
 	protected void onResume() {
 	    super.onResume();
 	    registerReceiver(handleNewMessageAlertReceiver,
                 new IntentFilter(CommonUtilities.NEW_MESSAGE_ALERT));
 	}
 	
 	@Override
 	protected void onPause() {
 	    super.onPause();
 	    unregisterReceiver(handleNewMessageAlertReceiver);
 	}
 	
 	@Override
 	protected void onRestart() {
 	    super.onRestart();
 	    registerReceiver(handleNewMessageAlertReceiver,
                 new IntentFilter(CommonUtilities.NEW_MESSAGE_ALERT));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.chat_room, menu);
 		return true;
 	}
 	
 	@Override
     public void onBackPressed() {
 	    // TODO: warn user that we are leaving room? Also is this how we want to leave rooms.
 	    new RoomLeaver(chatroominfo).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 	    super.onBackPressed();
 	}
 	
 	/**
 	 * Method that loads messages to adapter. Prepares the chat view to use GCM thereafter.
 	 */
 	public void GCMMessageLoader() {
 	    new GCMMessageLoader(this, chatroominfo).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 	}
 	
 	/**
 	 * Creates and sends a message typed by the user.
 	 * 
 	 * @param view
 	 */
 	public void createMessage(View view) {
 		EditText edittext = (EditText)findViewById(R.id.message);
 		String stValue = edittext.getText().toString();
 		
 		MessageInfo message = new MessageInfo
 				(stValue, userinfo.stUserName(), new Timestamp(System.currentTimeMillis()));
 		
 		new MessageSender(message, chatroominfo).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 		edittext.setText("");
 	}
 	
 	/**
 	 * AsyncTask that leaves the room.
 	 * 
 	 * @author Colin Kho
 	 *
 	 */
 	class RoomLeaver extends AsyncTask<String, String, Boolean> {
 	    private ChatRoomInfo chatroominfo;
 	    public RoomLeaver(ChatRoomInfo roominfo) {
 	        chatroominfo = roominfo;
 	    }
 	    
 	    @Override
 	    protected void onPreExecute() {
 	        super.onPreExecute();
             progressdialog = new ProgressDialog(ChatRoomActivity.this);
             progressdialog.setMessage("Leaving room. Please wait...");
             progressdialog.setIndeterminate(false);
             progressdialog.setCancelable(true);
             progressdialog.show();
 	    }
 	    
         @Override
         protected Boolean doInBackground(String... params) {
             Boolean leaveRoomSuccess = chatController.leaveRoom(chatroominfo);
             return leaveRoomSuccess;
         }
         
         @Override
         protected void onPostExecute(Boolean success) {
            progressdialog.dismiss();
         }    
 	}
 	
 	/**
 	 * Posts a message to the room's database
 	 * 
 	 * @author Jordan Hazari
 	 *
 	 */
 	class MessageSender extends AsyncTask<String, String, RequestResultSet> {
 		private MessageInfo messageinfo;
 		private ChatRoomInfo chatroominfo;
 		
 		/**
 		 * Constructs a MessageSender object
 		 * 
 		 * @param message the message to be sent
 		 * @param chatroominfo the room to post the message to
 		 */
 		public MessageSender(MessageInfo message, ChatRoomInfo chatroominfo) {
 			this.messageinfo = message;
 			this.chatroominfo = chatroominfo;
 		}
 
 		/**
 		 * Posts the message to the room
 		 */
 		@Override
 		protected RequestResultSet doInBackground(String... params) {
 			return ChatManager.rrsPostMessage(userinfo, chatroominfo, messageinfo);
 		}
 	}
 	
 	/**
 	 * Retrieves the message log of a chat room
 	 * 
 	 * @author Jordan Hazari
 	 *
 	 */
 	class MessageLoader extends AsyncTask<String, String, PullMessageResultSet> {
 		private ChatRoomActivity chatroomactivity;
 		private ChatRoomInfo chatroominfo;
 		
 		/**
 		 * Constructs a MessageLoader object
 		 * 
 		 * @param chatroomactivity the activity context
 		 * @param chatroominfo the chat room to retrieve the chat log from
 		 */
 		public MessageLoader(ChatRoomActivity chatroomactivity, ChatRoomInfo chatroominfo) {
 			this.chatroomactivity = chatroomactivity;
 			this.chatroominfo = chatroominfo;
 		}
 
 		/**
 		 * Retrieves and displays the chat log, constantly updating
 		 */
 		@Override
 		protected PullMessageResultSet doInBackground(String... params) {
 			while (true) {
 				PullMessageResultSet pmrsRecent = ChatManager.pmrsChatRecentChat
 						(chatroominfo, new Timestamp(System.currentTimeMillis()-1000000000));
 				
 				rgmessageinfo = pmrsRecent.rgmessage;
 				
 				chatroomactivity.runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						adapter.clear();
 						
 						for (int i = 0; i < rgmessageinfo.size(); i++)
 							adapter.add(rgmessageinfo.get(i));
 					}
 				});
 			}
 		}
 	}
 	
 	/**
 	 * Loads messages from chat controller which is prepared for GCM.
 	 * 
 	 * @author Colin Kho
 	 *
 	 */
 	class GCMMessageLoader extends AsyncTask<String, String, Boolean> {
 	    private Activity activity;
 	    private ChatRoomInfo chatroominfo;
 	    
 	    public GCMMessageLoader(Activity activity, ChatRoomInfo chatroominfo) {
 	        this.activity = activity;
 	        this.chatroominfo = chatroominfo;
 	    }
         @Override
         protected Boolean doInBackground(String... params) {
             rgmessageinfo = chatController.getMessagesFromChatRoom(chatroominfo.stId());
             activity.runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     adapter.clear();
                     
                     for (int i = 0; i < rgmessageinfo.size(); i++)
                         adapter.add(rgmessageinfo.get(i));
                 }
             });
             return true;
         }	    
 	}
 	
 	/**
 	 * Joins a chat room
 	 * 
 	 * @author Jordan Hazari
 	 *
 	 */
 	class RoomCreator extends AsyncTask<String, String, Boolean> {
 		private ChatRoomInfo chatroominfo;
 		private Activity activity;
 		
 		/**
 		 * Constructs a RoomCreator object
 		 * 
 		 * @param chatroominfo the room to create/join
 		 */
 		public RoomCreator(Activity activity, ChatRoomInfo chatroominfo) {
 			this.chatroominfo = chatroominfo;
 			this.activity = activity;
 		}
 		
 		/**
 		 * Displays a popup dialogue while joining the room
 		 */
 	    @Override
         protected void onPreExecute() {
             super.onPreExecute();
             progressdialog = new ProgressDialog(ChatRoomActivity.this);
             progressdialog.setMessage("Joining room. Please wait...");
             progressdialog.setIndeterminate(false);
             progressdialog.setCancelable(true);
             progressdialog.show();
         }
 
 	    /**
 	     * Adds the room, or joins it if it already exists
 	     * @return 
 	     */
 		@Override
 		protected Boolean doInBackground(String... params) {
 			return ChatController.getInstance().joinRoom(chatroominfo);
 		}
 		
 		/**
 		 * Closes the popup dialogue
 		 */
 		@Override
         protected void onPostExecute(Boolean success) {
             progressdialog.dismiss();
             if (!success) {
                 Toast serverToast = Toast.makeText(activity, "Failed to join room. Please try again.", Toast.LENGTH_LONG);
                 serverToast.show();
             } else {
                 ChatRoomActivity.this.GCMMessageLoader();
             }
 		}
 	}
 	
 	private class MessageAdapter extends ArrayAdapter<MessageInfo> {
 
         private List<MessageInfo> rgmessageinfo;
 
         public MessageAdapter(Context context, int textViewResourceId, List<MessageInfo> rgmessageinfo) {
             super(context, textViewResourceId, rgmessageinfo);
             this.rgmessageinfo = rgmessageinfo;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View view = convertView;
             if (view == null) {
                 LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 view = vi.inflate(R.layout.list_item, null);
             }
             MessageInfo messageinfo = rgmessageinfo.get(position);
             if (messageinfo != null) {
                 TextView textviewTop = (TextView) view.findViewById(R.id.toptext);
                 TextView textviewBottom = (TextView) view.findViewById(R.id.bottomtext);
                 if (textviewTop != null) {
                     textviewTop.setText(messageinfo.stSender() + ": " + messageinfo.stBody());
                 }
                 if(textviewBottom != null) {
                 	SimpleDateFormat simpledateformat = new SimpleDateFormat("hh:mm a, M/dd/yyyy", Locale.US);
                 	simpledateformat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
                     textviewBottom.setText("\t" + simpledateformat.format(messageinfo.timestampGet()));
                 }
             }
             return view;
         }
 	}
 	
 	private final BroadcastReceiver handleNewMessageAlertReceiver = 
 	        new BroadcastReceiver() {
 
                 @Override
                 public void onReceive(Context context, Intent intent) {
                     Log.v("Handling Message Receiver", "Received Message");
                     String stRoomId = intent.getExtras().getString(CommonUtilities.ROOM_ID);
                     
                     // Retrieve messages from controller.
                     List<MessageInfo> rgmessageinfo = chatController.getMessagesFromChatRoom(stRoomId);
                     
                     // Update adapter to have new messages.
                     adapter.clear();
                     
                     for (int iMsgIndex = 0; iMsgIndex < rgmessageinfo.size(); iMsgIndex++)
                         adapter.add(rgmessageinfo.get(iMsgIndex));
                 }    
 	};
 }
