 
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
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.text.Editable;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.view.View.OnClickListener;
 
 public class ChatRoom extends Activity  {
 
 	
 	XMPP_setting xmpp = new XMPP_setting();
 	MultiUserChat muc; 
 	Message message;
 	ArrayList<String> messages = new ArrayList<String>();
 	private Handler mHandler = new Handler();
 	
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_chat_room);       
         Button button = (Button) findViewById(R.id.post_btn);
 					
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
 				
 				//Setting up message listener
 				muc  = new MultiUserChat(xmpp.connection, xmpp.RECIPIENT);
 			    try{
 				muc.join(xmpp.USERNAME);//To include preferred user name(constrains may apply)
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
 		button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				System.out.println("Button pressed event occured");
 
 				String message = ((EditText) findViewById(R.id.post_text)).getText().toString();//getting message body from user input
 				System.out.println("Text from the text field: " + message);
				
				//if text fiend is not empty sends the message to the server in for of extended message(timestamp and uresname are included in the message body)
				if (message!="")
 				{
					try 
					{
						sendMessage(xmpp.extendedMessage(message)); 
						((EditText) findViewById(R.id.post_text)).setText("");
					} 
					catch (XMPPException e) 
					{
						System.out.println(e.toString());
					}
 				}
 
 			}
 		});
 
 	
     //base for filtering using search
         EditText searchWords = (EditText) findViewById(R.id.search_text);
         searchWords.addTextChangedListener(new TextWatcher() {
 
         public void afterTextChanged(Editable s) {}
 
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 
 		public void onTextChanged(CharSequence s, int start,int before, int count) {
 			
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
     	final ListView mlist = (ListView) findViewById(R.id.messagesListView);
     	mlist.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
     	mlist.setStackFromBottom(true);
 	    ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, messages);
 		mlist.setAdapter(adapter);
 		adapter.notifyDataSetChanged();
 
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
     
     //Multi-User Chat listener class, necessary for packet(message type in this case) retrieval from the server. Adds messages received to the "messages" array
     class ConsumerMUCMessageListener implements PacketListener {
  
 
 		public void processPacket(Packet packet) {
             if ( packet instanceof Message) {
             	message = (Message) packet;
             	
    				messages.add(message.getBody());
                 System.out.println(message.getFrom() +": " + message.getBody());
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
 
