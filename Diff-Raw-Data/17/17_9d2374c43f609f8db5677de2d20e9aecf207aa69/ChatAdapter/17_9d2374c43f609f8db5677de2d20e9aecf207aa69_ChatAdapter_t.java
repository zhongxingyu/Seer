 package my.b1701.SB.ChatService;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import my.b1701.SB.ChatClient.IMessageListener;
 import my.b1701.SB.ChatClient.SBChatMessage;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.MessageListener;
 import org.jivesoftware.smack.XMPPException;
 
 import android.os.RemoteCallbackList;
 import android.os.RemoteException;
 import android.util.Log;
 
 /***
  * There is a chatAdapter for every chat which stores a list of all chat msgs for this chat
  * + a list of all msgs sent but not yet delivered in a hashmap.
  * the processMessage method here is the first one to receive any incoming msg and it prcesses the 
  * message to refresh its list of sent/delivered msg and calls back listeners of chatwindow(IMessageListeners)
  * i.e. processMessage of chatwindow in IMessageListener.Stub is called 
  * @author arpit87
  *
  */
  class ChatAdapter extends IChatAdapter.Stub{
 
 	private static final int HISTORY_MAX_SIZE = 50;
     private static final String TAG = "my.b1701.SB.ChatService.ChatAdapter";
    private Boolean mIsOpen = false;
     private final Chat mSmackChat;
     private final String mParticipant;    
     private final List<Message> mMessages; 
     private final HashMap<Long,Message> mSentNotDeliveredMsgHashSet;
     private SBChatManager mChatManager;
     SBMsgListener mMsgListener = new SBMsgListener();
     private final RemoteCallbackList<IMessageListener> mRemoteListeners = new RemoteCallbackList<IMessageListener>();
     private int participantID = 0; //for notification
     
     //small chat participant should be complete as to is overridden inside sendMsg by smack to participant
     public ChatAdapter(final Chat chat,SBChatManager chatManager ) {
     	mSmackChat = chat;
     	mParticipant = chat.getParticipant();
     	mMessages = new LinkedList<Message>();
     	mSmackChat.addMessageListener(mMsgListener);
     	mChatManager = chatManager; 
     	participantID = mChatManager.numChats()+1;
     	mSentNotDeliveredMsgHashSet = new HashMap<Long,Message>();
         }
     
 	@Override
 	public void sendMessage(Message msg) throws RemoteException{
 		org.jivesoftware.smack.packet.Message msgToSend = new org.jivesoftware.smack.packet.Message();		
 		String msgBody = msg.getBody();		
 		Log.w(TAG, "message sending to " + msg.getTo());
 		msgToSend.setBody(msgBody);
 		msgToSend.setSubject(msg.getSubject());
 		msgToSend.setProperty(Message.UNIQUEID, msg.getUniqueMsgIdentifier());		
 		msgToSend.setProperty(Message.SBMSGTYPE, Message.MSG_TYPE_CHAT);
 		try { 
 			mSmackChat.sendMessage(msgToSend);
 			msg.setStatus(SBChatMessage.SENT);
 			mMessages.add(msg);			
 			mSentNotDeliveredMsgHashSet.put(msg.getUniqueMsgIdentifier(), msg);
 		} catch (XMPPException e) {
 			//TODO retry sending msg?
 			msg.setStatus(SBChatMessage.SENDING_FAILED);
 			mMessages.add(msg);						
 		    e.printStackTrace();
 		}
 		//we do a callback to update this msg status to sent or sending failed
 		//no user might have switched chat after sending msg, in that case we wont get a 
 		//message in current chat window with this unique identifier. But later when it fetches all
 		// msgs it ll get status as sent/failed
 		if(isOpen())
 			callListeners(msg);
 		
 	}
 	
 	
 	 private void addMessage(Message msg) {
 			if (mMessages.size() == HISTORY_MAX_SIZE)
 			    mMessages.remove(0);
 			mMessages.add(msg);
 	 }
 		
 	@Override
 	public void setOpen(boolean value) throws RemoteException {
		mIsOpen = value;
 		
 	}
 
 	@Override
 	public void addMessageListener(IMessageListener listener)
 			throws RemoteException {
 		mRemoteListeners.register(listener);
 		
 	}
 	
 	@Override
     public void removeMessageListener(IMessageListener listen) {
 	if (listen != null) {
 	    mRemoteListeners.unregister(listen);
 	}
     }
 	
 	private class SBMsgListener implements MessageListener {		
 
 		//first of all the msg comes here
 		// we have different msg types
 		
 		@Override
 		public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
 		    Message msg = new Message(message);		    
 		    Log.d(TAG, "new msg " + msg.getBody());	
		    Log.d(TAG, "chat is open?"+mIsOpen) ;
 		    //if broadcast message from new user then do getMatch req
 		    if(msg.getType() == Message.MSG_TYPE_NEWUSER_BROADCAST)
 		    {
 		    	//TODO getMatch and notify user
 		    	return;
 		    }
 
 		    if(msg.getType() == Message.MSG_TYPE_ACK)
 		    {
 		    	//ack has same unique id as the msg
 		    	//we are doing so as we cant send long in body
 		    	if(msg.getInitiator()=="" || msg.getBody() =="")
 					   return;
 		    	//do not add ack to list
 		    	//ack msgs have time in body	
 		    	Message origMsg = mSentNotDeliveredMsgHashSet.get(msg.getUniqueMsgIdentifier());
 		    	if(origMsg!=null)
 		    	{
 		    		Log.i(TAG, "got ack for msg: "+origMsg.getBody()) ;
 		    		origMsg.setStatus(SBChatMessage.DELIVERED);
 		    		mSentNotDeliveredMsgHashSet.remove(origMsg);
 		    	}
 		    	else
 		    	{
 		    		Log.d(TAG, "got ack but not msg uniqid: "+msg.getUniqueMsgIdentifier()) ;
 		    	}
		    	if(mIsOpen)
 			    {
 			       Log.i(TAG, "chat is open,sending ack to window ") ;
 				   callListeners(msg);
 			    }
 		    }
 		    
 		    //this is chat coming from outside,send ack to this msg
 		   if(msg.getType() == Message.MSG_TYPE_CHAT)
 		   {
 			    sendAck(msg.getUniqueMsgIdentifier());			    
 			    if (mMessages.size() == HISTORY_MAX_SIZE)
 				    mMessages.remove(0);				
 		    	mMessages.add(msg);
			    if(mIsOpen)
 			    {
 			    	Log.i(TAG, "chat is open") ;
 				    callListeners(msg);
 			    }
 			    else
 			    {
 			    	Log.i(TAG, "chat not open,Sending notification") ;
 			    	String sub = msg.getSubject();
 			    	if(sub == "")
 			    		sub = "Unknown";		    		
 			    	mChatManager.notifyChat(participantID,msg.getInitiator(),sub);
 			    	
 			    }
 		    	
 		    }		
 		}
 		
 		    
 		}
 	private void sendAck(long uniqueID)
 	{		
 		org.jivesoftware.smack.packet.Message msgToSend = new org.jivesoftware.smack.packet.Message();
 		//msg type is overritten by smack so add property
 		//msgToSend.setType(org.jivesoftware.smack.packet.Message.Type.headline);
 		msgToSend.setProperty(Message.UNIQUEID, uniqueID);
 		msgToSend.setProperty(Message.SBMSGTYPE, Message.MSG_TYPE_ACK);
 		try { 
 			mSmackChat.sendMessage(msgToSend);
 		} catch (XMPPException e) {
 			//TODO retry sending msg?
 			Log.e(TAG, "couldnt send ack");					
 		    e.printStackTrace();
 		}
 	}
 	private void callListeners(Message msg)
 	{
 		int n = mRemoteListeners.beginBroadcast();
 	    for (int i = 0; i < n; i++) {
 		IMessageListener listener = mRemoteListeners.getBroadcastItem(i);
 		try {
 		    if (listener != null)
 			listener.processMessage(ChatAdapter.this, msg);
 		} catch (RemoteException e) {
 		    Log.w(TAG, "Error while diffusing message to listener", e);
 		}
 	    }
 	    mRemoteListeners.finishBroadcast();
 	}
 
 	@Override
 	public boolean isOpen() throws RemoteException {
		return mIsOpen;		
 	}
 
 	@Override
 	public String getParticipant() throws RemoteException {
 		
 		return mParticipant;
 	}
 
 	@Override
 	public List<Message> getMessages() throws RemoteException {		
 		return mMessages;
 	}
 
 	
 
 }
