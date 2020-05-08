 package my.b1701.SB.ChatClient;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import my.b1701.SB.R;
 import my.b1701.SB.ChatService.IChatAdapter;
 import my.b1701.SB.ChatService.IChatManager;
 import my.b1701.SB.ChatService.IXMPPAPIs;
 import my.b1701.SB.ChatService.Message;
 import my.b1701.SB.ChatService.SBChatService;
 import my.b1701.SB.FacebookHelpers.FacebookConnector;
 import my.b1701.SB.HelperClasses.AlertDialogBuilder;
 import my.b1701.SB.HelperClasses.ProgressHandler;
 import my.b1701.SB.HelperClasses.ThisUserConfig;
 import my.b1701.SB.HelperClasses.ToastTracker;
 import my.b1701.SB.HttpClient.GetMatchingNearbyUsersRequest;
 import my.b1701.SB.HttpClient.SBHttpClient;
 import my.b1701.SB.HttpClient.SBHttpRequest;
 import my.b1701.SB.Server.ServerConstants;
 import my.b1701.SB.Users.CurrentNearbyUsers;
 import my.b1701.SB.Users.NearbyUser;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class ChatWindow extends Activity{
 	
 	private static final String CHAT_SERVER_IP = "54.243.171.212";
 	private static String TAG = "my.b1701.SB.ChatClient.ChatWindow";
 	private IXMPPAPIs xmppApis = null;
 	private TextView mContactNameTextView;
    // private ImageView mContactPicFrame;	 
     private TextView mContactDestination;	
     //private ImageView mContactPic;   
     private ListView mMessagesListView;
     private EditText mInputField;
     private Button mSendButton;
     private IChatAdapter chatAdapter;
     private IChatManager mChatManager;
     private IChatManagerListener mChatManagerListener = new ChatManagerListener();
     private IMessageListener mMessageListener = new SBOnChatMessageListener();
     private ISBChatConnAndMiscListener mCharServiceConnMiscListener = new SBChatServiceConnAndMiscListener();
     private final ChatServiceConnection mChatServiceConnection = new ChatServiceConnection();
     private String mParticipantFBID = "";    
     private SBChatBroadcastReceiver mSBBroadcastReceiver = new SBChatBroadcastReceiver();
     Handler mHandler = new Handler();
     private SBChatListViewAdapter mMessagesListAdapter = new SBChatListViewAdapter();
     private boolean mBinded = false;
     private String mThiUserChatUserName = "";
     private String mThisUserChatPassword = "";
     private String mThisUserChatFullName =  "";
 	private ProgressDialog progressDialog;
 	private FacebookConnector fbconnect; // required if user not logged in
     
 		    
 	    @Override
 		public void onCreate(Bundle savedInstanceState) {	    	
 		super.onCreate(savedInstanceState);			
 		setContentView(R.layout.chatwindow);
 		//this.registerReceiver(mSBBroadcastReceiver, new IntentFilter(SBBroadcastReceiver.SBCHAT_CONNECTION_CLOSED));
 	    mContactNameTextView = (TextView) findViewById(R.id.chat_contact_name);
 	   // mContactPicFrame = (ImageView) findViewById(R.id.chat_contact_pic_frame);
 	    mContactDestination = (TextView) findViewById(R.id.chat_contact_destination);	    
 	   // mContactPic = (ImageView) findViewById(R.id.chat_contact_pic);
 	    mMessagesListView = (ListView) findViewById(R.id.chat_messages);
 	    mMessagesListView.setAdapter(mMessagesListAdapter);
 	    mInputField = (EditText) findViewById(R.id.chat_input);		
 		mInputField.requestFocus();
 		mSendButton = (Button) findViewById(R.id.chat_send_message);
 		mSendButton.setOnClickListener(new OnClickListener() {
 		    @Override
 		    public void onClick(View v) {
 			sendMessage();
 		    }
 		});
 		
 		mThiUserChatUserName = ThisUserConfig.getInstance().getString(ThisUserConfig.CHATUSERID);
 		mThisUserChatPassword = ThisUserConfig.getInstance().getString(ThisUserConfig.CHATPASSWORD);
 		mThisUserChatFullName = ThisUserConfig.getInstance().getString(ThisUserConfig.FB_FIRSTNAME);
 		
 }
 
 @Override
 public void onResume() {
 	super.onResume();
 	//set participant before binding
 	String oldParticipant = mParticipantFBID;
 	mParticipantFBID = getIntent().getStringExtra("participant");
 	if(mParticipantFBID == "")
 		mParticipantFBID = oldParticipant;	
 	//mContactNameTextView.setText(mReceiver);
 	getParticipantInfoFromFBID(mParticipantFBID);
 	if (!mBinded) 
 		bindToService();
 	else
 		try {
 			changeCurrentChat(mParticipantFBID);
 		} catch (RemoteException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	
 		
 		//String fromMessage = getIntent().getStringExtra("frommessage");
 		//mMessagesListAdapter.addMessage(new SBChatMessage(from, from, fromMessage, false, new Date().toString()));    
 		//mMessagesListAdapter.notifyDataSetChanged();		
 	
 	//setTitle(getString(R.string.conversation_name) +": " +jid);
 	
 }
 	
     
     @Override
     protected void onPause() {
 	super.onPause();
 	
 	    if (chatAdapter != null) {
 	    	try {
 				chatAdapter.setOpen(false);
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}	  
 	    }    
 	   
     }
     
     @Override
     public void onDestroy() {
     	
     	super.onDestroy();
     	if (mBinded) {
     		releaseService();
     	    mBinded = false;
     	}
     	xmppApis = null;	
     	chatAdapter = null;
     	mChatManager = null;    	
     }
     
     
     @Override
     protected void onNewIntent(Intent intent) {
 	super.onNewIntent(intent);
 	setIntent(intent);	
     }
     
     private void bindToService() {
             Log.d( TAG, "binding chat to service" );        
         	
            Intent i = new Intent(getApplicationContext(),SBChatService.class);
           
            getApplicationContext().bindService(i, mChatServiceConnection, BIND_AUTO_CREATE);	  
            mBinded = true;
         
    }
     
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         fbconnect.authorizeCallback(requestCode, resultCode, data);
     }
 	    
 	    private void releaseService() {
     		if(mChatServiceConnection != null) {
     			getApplicationContext().unbindService(mChatServiceConnection);    			   			
     			Log.d( TAG, "chat Service released from chatwindow" );
     		} else {
     			 ToastTracker.showToast("Cannot unbind - service not bound", Toast.LENGTH_SHORT);
     		}
 	    }
 	    
 	    public void getParticipantInfoFromFBID(String fbid)
 	    {
 	    	NearbyUser thisNearbyUser = CurrentNearbyUsers.getInstance().getNearbyUserWithFBID(fbid);
 	    	if(thisNearbyUser != null)
 	    	{
 	    		String travelInfo = "";
 		    	mContactNameTextView.setText(thisNearbyUser.getUserFBInfo().getName());
 		    	//SBImageLoader.getInstance().displayImageElseStub(thisNearbyUser.getUserFBInfo().getImageURL(), mContactPic,R.drawable.userpicicon);
 		    	travelInfo = thisNearbyUser.getUserLocInfo().getUserSrcLocality() +" to "+thisNearbyUser.getUserLocInfo().getUserDstLocality() ;
 		    	mContactDestination.setText(travelInfo);
 		    	//if(thisNearbyUser.getUserOtherInfo().isOfferingRide())
 		    	//	mContactPicFrame.setImageResource(R.drawable.list_frame_green_new);
 		    	//else
 		    	//	mContactPicFrame.setImageResource(R.drawable.list_frame_blue_new);
 		    	//mContactStatusMsgTextView.setText("Status message if any");		    	
 	    	}
 	    	else
 	    	{
 	    		//some new user not yet visible to this user has initiated chat
 	    		//so we call server to get nearby user which should have this user
 	    		ProgressHandler.showInfiniteProgressDialoge(this, "Please wait..", "");
 	    		this.registerReceiver(mSBBroadcastReceiver, new IntentFilter(ServerConstants.NEARBY_USER_UPDATED));
 	    		SBHttpRequest getNearbyUsersRequest = new GetMatchingNearbyUsersRequest();
 	    	    SBHttpClient.getInstance().executeRequest(getNearbyUsersRequest);	    		
 	    	}
 	    }
 	    
 	    public String getParticipantFBID() {
 			return mParticipantFBID;
 		}
 
 		private void sendMessage() {
 		final String inputContent = mInputField.getText().toString();	
 		SBChatMessage lastMessage = null;
 		if(!"".equals(inputContent))
 		{
 			Message newMessage = new Message(mParticipantFBID,Message.MSG_TYPE_CHAT);
 			newMessage.setBody(inputContent);
 			newMessage.setFrom(mThiUserChatUserName+"@"+CHAT_SERVER_IP);
 			newMessage.setSubject(mThisUserChatFullName);			
 			newMessage.setUniqueMsgIdentifier(System.currentTimeMillis());		 
 			
 		 	//now update on our view
 		 	//sbchatmsg (from,to,...		    
 		   /* if (mMessagesListAdapter.getCount() != 0)
 		    	lastMessage = (SBChatMessage) mMessagesListAdapter.getItem(mMessagesListAdapter.getCount() - 1);
 
 		    if (lastMessage != null && lastMessage.getInitiator().equals(mThiUserChatUserName)) {
 		    	lastMessage.setMessage(lastMessage.getMessage().concat("\n" + inputContent));
 		    	lastMessage.setTimestamp(new Date().toString());
 		    	lastMessage.setStatus(ChatMsgStatus.SENDING); // sendin 0;
 			mMessagesListAdapter.setMessage(mMessagesListAdapter.getCount() - 1, lastMessage);
 		    } else{
 		    mMessagesListAdapter.addMessage(new SBChatMessage(mThiUserChatUserName, mParticipantFBID, inputContent, false, new Date().toString(),ChatMsgStatus.SENDING));
 		   }			    
 		    mMessagesListAdapter.notifyDataSetChanged();*/
 			
 			mMessagesListAdapter.addMessage(new SBChatMessage(mThiUserChatUserName, mParticipantFBID,inputContent, false, new Date().toString(),
 					                                          SBChatMessage.SENDING,newMessage.getUniqueMsgIdentifier()));
 			mMessagesListAdapter.notifyDataSetChanged();
 			
 		  //send msg to xmpp
 			 try {
 				if (chatAdapter == null) {
 										
 					chatAdapter = mChatManager.createChat(mParticipantFBID, mMessageListener);
 					chatAdapter.setOpen(true);
 				}
 				chatAdapter.sendMessage(newMessage);
 			    } catch (RemoteException e) {
 			    	lastMessage = (SBChatMessage) mMessagesListAdapter.getItem(mMessagesListAdapter.getCount() - 1);
 			    	lastMessage.setStatus(SBChatMessage.SENDING_FAILED); 
 			    	mMessagesListAdapter.setMessage(mMessagesListAdapter.getCount() - 1, lastMessage);
 			    	mMessagesListAdapter.notifyDataSetChanged();
 				Log.e(TAG, e.getMessage());
 			    }
 		   
 		}			   
 		    mInputField.setText(null);
 		}
 	    
 	    
 	  	private void loginWithProgress() 
 	    {	    	
 	    	try {
 				if(mThiUserChatUserName != "" && mThisUserChatPassword != "")
 				{
 					progressDialog = ProgressDialog.show(ChatWindow.this, "Logging in", "Please wait..", true);
 			    	Log.d(TAG,"logging in chat window  with username,pass:" + mThiUserChatUserName + ","+mThisUserChatPassword);
 					xmppApis.loginWithCallBack(mThiUserChatUserName, mThisUserChatPassword,mCharServiceConnMiscListener);
 				}
 				else
 				{									
 					//AlertDialogBuilder.showOKDialog(this,"FB login required", "You need to login one time to FB to chat with user");	
 					fbconnect.loginToFB();
 				}
 			} catch (RemoteException e) {
 				progressDialog.dismiss();				
 				AlertDialogBuilder.showOKDialog(this,"Error", "Problem logging,try later");
 				//ToastTracker.showToast("Error loggin,try later");
 				e.printStackTrace();
 			}
 	    }
 	    //in already open chatWindow this function switches chats
 	    private void changeCurrentChat(String participant) throws RemoteException {
 	    	
 	    	chatAdapter = mChatManager.getChat(participant);
 	    	if (chatAdapter != null) {
 	    		chatAdapter.setOpen(true);
 	    		chatAdapter.addMessageListener(mMessageListener);
 	    	    
 	    	}
 	    	getParticipantInfoFromFBID(participant);
 	    	//mContactNameTextView.setText(participant);
 	    	fetchPastMsgsIfAny();
 	        }
 	    
 	    /**
 	     * Get all messages from the current chat and refresh the activity with them.
 	     * @throws RemoteException If a Binder remote-invocation error occurred.
 	     */
 	    private void fetchPastMsgsIfAny() throws RemoteException {
 	    	mMessagesListAdapter.clearList();
 		if (chatAdapter != null) {
 			List<Message> chatMessages = chatAdapter.getMessages();
 			if(chatMessages.size()>0)
 			{
 			    List<SBChatMessage> msgList = convertMessagesList(chatMessages);
 			    mMessagesListAdapter.addAllToList(msgList);
 			    mMessagesListAdapter.notifyDataSetChanged();
 			}
 		}
 	    }
 
 	    /**
 	     * Convert a list of Message coming from the service to a list of MessageText that can be displayed in UI.
 	     * @param chatMessages the list of Message
 	     * @return a list of message that can be displayed.
 	     */
 	    private List<SBChatMessage> convertMessagesList(List<Message> chatMessages) {
 		List<SBChatMessage> result = new ArrayList<SBChatMessage>(chatMessages.size());		
 		SBChatMessage lastMessage = null;		
 		for (Message m : chatMessages) {
 		    		    
 		    if (m.getType() == Message.MSG_TYPE_CHAT) {	
 			
 			if (m.getBody() != null) {
 			    if (lastMessage == null ) {
 				lastMessage = new SBChatMessage(m.getInitiator(), m.getReceiver(), m.getBody(), false, m.getTimestamp(),m.getStatus(),m.getUniqueMsgIdentifier());
 				result.add(lastMessage);
 			    } else {
 			    	if(m.getInitiator().equals(lastMessage.getInitiator()))
 			    	{
 			    		lastMessage.setMessage(lastMessage.getMessage().concat("\n" + m.getBody()));
 			    		lastMessage.setStatus(m.getStatus());
			    		lastMessage.setTimestamp(m.getTimestamp());
 			    	}
 			    	else
 			    	{			    		
 			    		lastMessage = new SBChatMessage(m.getInitiator(), m.getReceiver(), m.getBody(), false, m.getTimestamp(),m.getStatus(),m.getUniqueMsgIdentifier());
 			    		result.add(lastMessage);
 			    	}
 			    }
 			}
 		    }
 		    
 		}
 		return result;
 	    }
 	    
 	    public void initializeChatWindow() {
 	    	
 	           	
 	    	if(mChatManager == null)
 			{
 				try {
 					mChatManager = xmppApis.getChatManager();
     			if (mChatManager != null) {
     				Log.d(TAG, "Chat manager got");
     				chatAdapter = mChatManager.createChat(mParticipantFBID, mMessageListener);
     				if(chatAdapter!=null)
     				{
 						chatAdapter.setOpen(true);
 						fetchPastMsgsIfAny();
     				}
     			   // mChatManager.addChatCreationListener(mChatManagerListener);
     			    //changeCurrentChat(thisUserID);
     			}
     			else
     			{	Log.d(TAG, "Chat manager not got,will try login");
     				loginWithProgress();
     			}
     		    } catch (RemoteException e) {
     			Log.e(TAG, e.getMessage());
     		    }   
 			}		
 	    	
 	          
 	    	
 	    }
 	    
 	    
 	    private final class ChatServiceConnection implements ServiceConnection{
 	    	
 	    	@Override
 	    	public void onServiceConnected(ComponentName className, IBinder boundService) {
 	    		ToastTracker.showToast("onServiceConnected called", Toast.LENGTH_SHORT);
 	    		Log.d(TAG,"onServiceConnected called");
 	    		xmppApis = IXMPPAPIs.Stub.asInterface((IBinder)boundService);
 	    		initializeChatWindow();    	
 	    		Log.d(TAG,"service connected");
 	    	}
 
 	    	@Override
 	    	public void onServiceDisconnected(ComponentName arg0) {
 	    		ToastTracker.showToast("onService disconnected", Toast.LENGTH_SHORT);
 	    		xmppApis = null;	    		
 	    	    try {	    		
 	    		mChatManager.removeChatCreationListener(mChatManagerListener);
 	    	    } catch (RemoteException e) {
 	    		Log.e(TAG, e.getMessage());
 	    	    }
 	    		Log.d(TAG,"service disconnected");
 	    	}
 
 	    } 
 	    
  
 //this is callback method executed on client when ChatService receives a message	
 private class SBOnChatMessageListener extends IMessageListener.Stub {
 	//this method appends to current chat, we open new chat only on notification tap or user taps on list
 	//i.e. we open new chat window only on intent
 	@Override
 	public void processMessage(final IChatAdapter chatAdapter, final Message msg)
 			throws RemoteException {
 		
 		mHandler.post(new Runnable() {
 	 		
 		    @Override
 		    public void run() {
 		   //this means chat switched before callback but this should not happen as we are chking isOpen inchatadapter
 		  // if(chatAdapter.getParticipant()!=mParticipantFBID){
 		//	   Log.d(TAG,"chat callback on non open chat!!!shouldnt happen as we chking isopen in adapter");
 		 //   	   return;
 		 //  }
 		   
 		  if(msg.getType() == Message.MSG_TYPE_ACK)
 		  {
 			  //here we should receive acks of only open chats
 			  //non open chats ack update msgs in list of theie respective chatAdapter and user when next opens them
 			  //he fetches all the msgs which have been updated in adapter.
 			  mMessagesListAdapter.updateMessageStatusWithUniqueID(msg.getUniqueMsgIdentifier(), SBChatMessage.DELIVERED);
 		  }
 		  else if(msg.getType() == Message.MSG_TYPE_CHAT)
 		  {
 			  //here we can get two type of chat msg
 			  //1) self msg after status change to sent/sending failed
 			  //2) incoming msg from other user
 			  
 			  //handle 1)
 			  if(msg.getStatus() == SBChatMessage.SENT || msg.getStatus() == SBChatMessage.SENDING_FAILED)
 			  {
 				  mMessagesListAdapter.updateMessageStatusWithUniqueID(msg.getUniqueMsgIdentifier(), msg.getStatus());
 			  }
 			  else if (msg.getBody() != null) {
 				    SBChatMessage lastMessage = null;
 				    
 				    if (mMessagesListAdapter.getCount() != 0)
 				    	lastMessage = (SBChatMessage) mMessagesListAdapter.getItem(mMessagesListAdapter.getCount()-1);
 
 				    if (lastMessage != null && !lastMessage.getInitiator().equals(mThiUserChatUserName)) {
 				    	lastMessage.setMessage(lastMessage.getMessage().concat("\n" + msg.getBody()));
 				    	lastMessage.setTimestamp(msg.getTimestamp());					    
 				    	mMessagesListAdapter.setMessage(mMessagesListAdapter.getCount() - 1, lastMessage);
 				    
 				    } else if (msg.getBody() != null){
 				    	mMessagesListAdapter.addMessage(new SBChatMessage(msg.getInitiator(), msg.getReceiver(), msg.getBody(),false, msg.getTimestamp(),msg.getStatus(),msg.getUniqueMsgIdentifier()));
 				    }	   
 				
 			    }
 		  }			  
 		  		   
 		 	
 				    	 mMessagesListAdapter.notifyDataSetChanged();				    
 				}});
 		    
 	    
 	}
 }
 
 //this is the callback class to track chatmanger on ChatService
 private class ChatManagerListener extends IChatManagerListener.Stub {
 
 	@Override
 	public void chatCreated(IChatAdapter chat, boolean locally) {
 	    if (locally)
 		return;
 	    try {
 	    	mParticipantFBID = chat.getParticipant();
 	    	//changeCurrentChat(mParticipant);
 		//String chatJid = chat.getParticipant().getJIDWithRes();
 		
 		    if (chatAdapter != null) {
 		    	chatAdapter.setOpen(false);
 		    	chatAdapter.removeMessageListener(mMessageListener);
 		    }
 		    chatAdapter = chat;
 		    chatAdapter.setOpen(true);
 		    chatAdapter.addMessageListener(mMessageListener);		   
 		
 	    } catch (RemoteException ex) {
 		Log.e(TAG, "A remote exception occurs during the creation of a chat", ex);
 	    }
 	}
     }
 
 private class SBChatServiceConnAndMiscListener extends ISBChatConnAndMiscListener.Stub{
 
 	@Override
 	public void loggedIn() throws RemoteException {
 		Log.d(TAG, "Chat window login call back");
 		if(progressDialog.isShowing())
 		{
 			progressDialog.dismiss();			
 		}
 		if(mChatManager == null)
 			mChatManager = xmppApis.getChatManager();
 		
 		if(mChatManager == null)
 		{
 			ToastTracker.showToast("chatwindow login callback,loggedin but still chatmanager null!!");	
 			Log.d(TAG, "Chat window login call back,logged in but still didnt find chat manager");
 		}
 		else
 			Log.d(TAG, "Chat window login call back,logged in and found chat manager");
 	}
 
 	@Override
 	public void connectionClosed() throws RemoteException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void connectionClosedOnError() throws RemoteException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void reconnectingIn(int seconds) throws RemoteException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void reconnectionFailed() throws RemoteException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void reconnectionSuccessful() throws RemoteException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void connectionFailed(String errorMsg) throws RemoteException {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
 
 
 
 	    
 }
