 
 package com.mobileindustrycast;
 
 import java.util.ArrayList;
 
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 
 import com.mobileindustrycast.R;
 import com.mobileindustrycast.XMPP_setting;
 
 
 import android.text.TextWatcher;
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.Editable;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.ToggleButton;
 import android.view.View.OnClickListener;
 
 public class ChatRoom extends Activity implements BroadcastDialog.NoticeDialogListener {
 
 	
 	XMPP_setting xmpp = new XMPP_setting();
 	MultiUserChat muc; 
 	Message message;
 	ArrayList<CustomListMessage> msg = new ArrayList<CustomListMessage>();
 	private Handler mHandler = new Handler();
 	String USERNAME="Boris";
 	String userLocation = "Soviet Russia";
 	String userStatus = "Buyer";
 
 	String filter ="";
     
 	ExtendedArrayAdapter adapter;
 	
 	BroadcastDialog dialog = new BroadcastDialog();
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_chat_room);  
        
     	
         adapter = new ExtendedArrayAdapter(this, msg);
         
         Button post_button = (Button) findViewById(R.id.post_btn);
         Button broadcast_button = (Button) findViewById(R.id.broadcast_btn);
         
         final ToggleButton buyer = (ToggleButton) findViewById(R.id.toggle_buyer);
         final ToggleButton seller = (ToggleButton) findViewById(R.id.toggle_seller);
         final ToggleButton trade = (ToggleButton) findViewById(R.id.toggle_trade);
         final ToggleButton info = (ToggleButton) findViewById(R.id.toggle_info);
 
         
         buyer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 if (isChecked) {
                     adapter.buyerSelected=true;
                     adapter.getFilter().filter(filter);
                 } else {
                 	adapter.buyerSelected=false;
                 	adapter.getFilter().filter(filter);
                 }
             }
         });
         
         seller.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 if (isChecked) {
                 	adapter.sellerSelected=true;
                 	adapter.getFilter().filter(filter);
                 } else {
                 	adapter.sellerSelected=false;
                 	adapter.getFilter().filter(filter);
                 }
             }
         });
         
         trade.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 if (isChecked) {
                 	adapter.tradeSelected=true;
                 	adapter.getFilter().filter(filter);
                 } else {
                 	adapter.tradeSelected=false;
                 	adapter.getFilter().filter(filter);
                 }
             }
         });
         
         info.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 if (isChecked) {
                 	adapter.infoSelected=true;
                 	adapter.getFilter().filter(filter);
                 } else {
                 	adapter.infoSelected=false;
                 	adapter.getFilter().filter(filter);
                 }
             }
         });
 
        
 					
 		//Working Thread that's associated with networking function login()
 		new Thread(new Runnable() {
 			public void run() {
 				try 
 				{
 					xmpp.connect();
 					xmpp.login();
 				} catch (XMPPException ex) {
 					System.out.println(ex.toString());
 				}
 				
 				//Setting up message listener, listener parameters for message packets require adapter to be used in UI Thread
 				muc  = new MultiUserChat(xmpp.connection, xmpp.RECIPIENT);
 			    try{
 				muc.join(USERNAME);//To include preferred user name(constrains may apply)
 		        ConsumerMUCMessageListener listener = new ConsumerMUCMessageListener();
 		        muc.addMessageListener(listener);
 			    } catch (XMPPException ex) {
 					System.out.println(ex.toString());
 				}
 		        
 		        //sending presence packet, showing your availability to the server
 				Presence presence = new Presence(Presence.Type.available);
 				xmpp.connection.sendPacket(presence);
 
 			}
 		}).start();
 		
         createListAdapter();
 
 		//Post button event. Sends message with user defined body to the Openfire Server
 		post_button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 
 				String body = ((EditText) findViewById(R.id.post_text)).getText().toString();//getting message body from user input
 				CustomListMessage message = new CustomListMessage(USERNAME, body, userLocation);
 			
 
 				//if text fiend is not empty sends the message to the server in for of extended message(timestamp and username are included in the message body)
 				if (body!=null && body.length()!=0)
 				{
 					System.out.println("body: "+body);
 					try 
 					{
 						sendMessage(messageContsructor(message)); 
 						((EditText) findViewById(R.id.post_text)).setText("");
 					} 
 					catch (XMPPException e) 
 					{
 						System.out.println(e.toString());
 					}
 				}
 
 			}
 		});
 		
 		broadcast_button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 
 				String body = ((EditText) findViewById(R.id.post_text)).getText().toString();
 				
 				if (body.length()!=0 && body!=null)
 				{
 				showBroadcastDialog();
 				}
 				
 			}
 		});
 
 	
     //base for filtering using search
         EditText searchWords = (EditText) findViewById(R.id.search_text);
         searchWords.addTextChangedListener(new TextWatcher() {
 
         @Override
         public void afterTextChanged(Editable s) {}
 
         @Override
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
         
         @Override
 		public void onTextChanged(CharSequence s, int start,int before, int count) {
         	filter = s.toString();
         	adapter.getFilter().filter(filter);
 		}
 		});}
             
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         //Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_chat_room, menu);
         return true;
     }
     
     public void createListAdapter()
     {  	
     	final ListView mlist =(ListView) findViewById(R.id.messagesListView);
     	mlist.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
     	mlist.setStackFromBottom(true);
     	mlist.setAdapter(adapter);
 		adapter.notifyDataSetChanged();
	//	adapter.getFilter().filter(filter);
     }
 
   //sending message to the Multi-User Chat
     public void sendMessage(String message_body) throws XMPPException {
     	System.out.println("XMPP_setting class' function 'sendMessage' started");
         
             try
             {
                 muc.sendMessage(message_body);
             }
             catch (XMPPException e)
             {
                 System.out.println(e.getMessage());
             }
 
 
     }
     
     public static String messageContsructor(CustomListMessage msg)
     {
     	String message = "#timestamp "+msg.getTimestamp()+" timestamp#"
     			+"#userName "+msg.getUserName()+" userName#"
     			+"#message "+msg.getBody()+" message#"
     			+"#location "+msg.getLocation()+" location#"
     			+"#status "+msg.getStatus()+" status#"
     			+"#messageType "+msg.getMessageType()+" messageType#";
     	return message;
     }
     
     public static CustomListMessage messageDeConstructor(String message)
     {
     	CustomListMessage msg = new CustomListMessage(message.substring(message.indexOf("#userName ")+"#userName ".length(),message.indexOf(" userName#")),
     			message.substring(message.indexOf("#message ")+"#message ".length(),message.indexOf(" message#")),
     			message.substring(message.indexOf("#location ")+"#location ".length(),message.indexOf(" location#")),
     			message.substring(message.indexOf("#status ")+"#status ".length(),message.indexOf(" status#")),
     			message.substring(message.indexOf("#messageType ")+"#messageType ".length(),message.indexOf(" messageType#")));
     	
     	msg.setTimestamp(message.substring(message.indexOf("#timestamp ")+"#timestamp ".length(),message.indexOf(" timestamp#")));
     	return msg;
     }
     
     //broadcast dialog supporting functions
     public void showBroadcastDialog() {
         // Create an instance of the dialog fragment and show it
         DialogFragment dialog = new BroadcastDialog();
         dialog.show(getFragmentManager(), "Broadcast");
     }
     
     @Override
     public void onDialogPositiveClick(DialogFragment dialog) {
         // User touched the buy button
     	userStatus="Buyer";
     	sendBroadcastMessage();
     }
 
     @Override
     public void onDialogNegativeClick(DialogFragment dialog) {
         // User touched the trade button
       userStatus="Trade";
   	  sendBroadcastMessage();
     }
     
     @Override
     public void onDialogNeutralClick(DialogFragment dialog) {
         // User touched the sell button
     	userStatus="Seller";
     	sendBroadcastMessage();
       
     }
     
     public void sendBroadcastMessage()
     {
     	String body = ((EditText) findViewById(R.id.post_text)).getText().toString();//getting message body from user input
 		CustomListMessage message = new CustomListMessage(USERNAME, body, userLocation, userStatus, "broadcast");
 	
 
 		//if text fiend is not empty sends the message to the server in for of extended message(timestamp and uresname are included in the message body)
 			try 
 			{
 				sendMessage(messageContsructor(message)); 
 				((EditText) findViewById(R.id.post_text)).setText("");
 			} 
 			catch (XMPPException e) 
 			{
 				System.out.println(e.toString());
 			}
     }
     
     //Multi-User Chat listener class, necessary for packet(message type in this case) retrieval from the server. Adds messages received to the "messages" array
     class ConsumerMUCMessageListener implements PacketListener {
  
 
 		public void processPacket(Packet packet) {
             if ( packet instanceof Message) {
             	message = (Message) packet;
             	
    				msg.add(messageDeConstructor(message.getBody()));
                 System.out.println(message.getFrom() +": " + messageDeConstructor(message.getBody()).messageToString());
                 // Add the incoming message to the list view(crashes the application currently)
                 mHandler.post(new Runnable() {
                     public void run() {
                   	createListAdapter();              	
                     }
                   });
             }
         }
 
     }
 
     protected void onDestroy()
     {
     	xmpp.disconnect();
     }
 }
 
