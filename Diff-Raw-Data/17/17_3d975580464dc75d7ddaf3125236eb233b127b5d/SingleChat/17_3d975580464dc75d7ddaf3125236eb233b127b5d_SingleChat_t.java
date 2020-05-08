 package com.ingloriouscoders.sheep;
 
 import java.util.List;
 import java.util.Vector;
 
 import com.ingloriouscoders.chatbackend.ChatContext;
 import com.ingloriouscoders.chatbackend.Contact;
 import com.ingloriouscoders.chatbackend.Conversation;
 import com.ingloriouscoders.chatbackend.Message;
 import com.ingloriouscoders.chatbackend.OnContactDataChanged;
 import com.ingloriouscoders.chatbackend.OnNewMessageListener;
 import com.ingloriouscoders.chatbackend.OnServiceNewMessageListener;
 import com.ingloriouscoders.chatbackend.ServiceChatContext;
 import com.ingloriouscoders.chatbackend.ServiceConversation;
 
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.ActionBar.Tab;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.support.v4.view.ViewPager;
 import android.text.Editable;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.ListView	;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.pm.FeatureInfo;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 
 import android.view.Window;
 
 import android.widget.ImageView;
 
 import android.view.Menu;
 import android.view.MenuInflater;
 
 import com.ingloriouscoders.chatbackend.Conversation;
 
 import com.ingloriouscoders.util.Debug;
 
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView.OnEditorActionListener;
 
 import android.widget.Toast;
 
 public class SingleChat extends FragmentActivity {
     /** Called when the activity is first created. */
 	private ServiceConversation mConversation;
 	private OnServiceNewMessageListener mListener;
 	
 	protected ChatService mChatService;
 	protected ServiceChatContext mServiceChatContext;
 	
 	private ContactStated mOpposite;
 	
 	final static int default_incomingColor = Color.rgb(50,166,166);
 	final static int default_outgoingColor = Color.rgb(109,217,110);
 
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         requestWindowFeature(Window.FEATURE_ACTION_BAR);
         
         ActionBar ab = getActionBar();
         ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg));
         ab.setDisplayHomeAsUpEnabled(true);
           
         
         setContentView(R.layout.singlechat);
         
         Intent startIntent = this.getIntent();
         Bundle extras = startIntent.getExtras();
         
         mOpposite = extras.getParcelable("contact");
         ab.setTitle("Chat mit " + mOpposite.getShowname());   
 
         
         
                                
                 
         final MessageView listview = (MessageView) findViewById(R.id.messageslistview);
         listview.smoothScrollBy(listview.getCount() * 500, 1000);
         listview.setDivider(null);
         
         final MessageAdapter msga = listview.getMessageAdapter();
         //final MessageAdapter msga = new MessageAdapter(this);
         //listview.setAdapter(msga);
         this.mOpposite.resetUnreadCount();
         this.mOpposite.setOpenState(true);
         startChatService();
    }
         
 public void onServiceConnected()
 {
 	mConversation = mOpposite.getConversation();
 	final MessageView listview = (MessageView) findViewById(R.id.messageslistview);
 	final MessageAdapter msga = listview.getMessageAdapter();
 	
         try
         {
         	List<Message> history = mConversation.getHistory();
         	for (int i=0;i<history.size();i++)
         	{
         		Message msg = history.get(i);
         		if (msg.getIncoming())
         		{
         			msg.setColor(default_incomingColor);
         		}
         		else 
         		{
         			msg.setColor(default_outgoingColor);
         		}
         		msga.addMessage(msg);
         	}
         	if (history.size() == 0)
         	{
         		Message first = new Message();
         		msga.addMessage(first);
        		//msga.removeMessage(first);
         	}
         }
         catch ( RemoteException e)
         {
         	Log.v("SingleChat","Der Verlauf konnte nicht geladen werden. Serviceverbindungsproblem");
         }
         
         
         
         mListener = new OnServiceNewMessageListener.Stub()
         {
 			@Override
 			public void onNewMessage(ServiceConversation _conversation,
 					Message recieved_message) throws RemoteException {
 				Log.v("SingleChat","Message recieved");
 				recieved_message.setColor(default_incomingColor);
 				msga.addScrolledMessage(recieved_message, listview);
 				SingleChat.this.mOpposite.resetUnreadCount();
 				
 			}
         	
         };
 		try
 		{
         	mConversation.addOnServiceNewMessageListener(mListener);
 		}
 		catch (RemoteException e)
 		{
 			Log.v("SingleChat","Fehler, der OnServiceNewMessageListener konnte nicht geaddet werden.");
 		}
 		
         
         final Activity ownact = this;
 
         
         
         Button sendbutton = (Button)findViewById(R.id.send_button);
         sendbutton.setOnClickListener(new Button.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				SingleChat myact = ((SingleChat)ownact);
 				ServiceConversation myconv = myact.mConversation;
 				if (myconv == null)
 				{
 					return;
 				}
 				EditText sendfield = (EditText)myact.findViewById(R.id.msg_field);
 				Editable content = sendfield.getText();
 				sendfield.setText("");
 				if (content.toString().equals("") )
 				
 				{
 					return;
 				}
 				Message msg = new Message();
 				try
 				{
 					msg = myconv.prepareMessage();
 				}
 				catch (RemoteException e)
 				{
 					Log.v("SingleChat","Fehler beim Vorbereiten der Nachricht ( lokal )");
 				}
 				finally
 				{
 					
 					msg.setColor(default_outgoingColor);
 					msg.setMessageText(content.toString());
 					
 					try
 					{
 						myconv.sendMessage(msg);
 					}
 					catch (RemoteException e)
 					{
 						Log.v("SingleChat","Fehler beim Senden der Nachricht ( lokal )");
 					}
 					
 					msga.addScrolledMessage(msg, listview);
 				}
 			}
 		});
         EditText sendfield = (EditText)findViewById(R.id.msg_field);
         sendfield.setOnEditorActionListener(new OnEditorActionListener() {
 			
 			@Override
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				SingleChat myact = ((SingleChat)ownact);
 				Button sendbutton = (Button)myact.findViewById(R.id.send_button);
 				sendbutton.callOnClick();
 				return false;
 			}
 		});
 
    }
 
     public void onDestroy()
     {
     	super.onDestroy();
     	unbindService(mConnection);
     }
 		    
 		    
 		    
 
 
 	
     @Override
     public void onPause() {
         super.onPause();
         
        
         this.mOpposite.setOpenState(false);       
         overridePendingTransition(R.anim.enterfromleft, R.anim.leavetoright);
         
    }
     
     @Override
     public void onResume() {
     	super.onResume();
     	this.mOpposite.setOpenState(true);
     	
     }
     
    @Override
    public void onStop() {
 	   super.onStop();
 	   this.mOpposite.setOpenState(false);
    }
    public void startChatService()
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	Intent serviceIntent = new Intent(this, ChatService.class);
    	this.startService(serviceIntent);
    	
    	
    	bindService(serviceIntent, mConnection,0);
    	
    }
    private ServiceConnection mConnection = new ServiceConnection() {
 
 		public void onServiceConnected(ComponentName className, IBinder service) {
 		    mServiceChatContext = ServiceChatContext.Stub.asInterface(service);
 		    if (mServiceChatContext == null)
 		    {
 		    	Log.v("","ChatContext = 0");
 		    	return;
 		    }
 		    SingleChat.this.mOpposite.setContext(mServiceChatContext);
 		    SingleChat.this.onServiceConnected();
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			mChatService = null;
 			mServiceChatContext = null;
 		}
 	};
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
     	switch (item.getItemId()) {
         	case android.R.id.home:
         		finish();
         		return true;
         	default:
         		return super.onOptionsItemSelected(item);
     	}
    }
 }
