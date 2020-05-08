 package edu.depaul.snotg_android.Chat;
 
 import java.util.ArrayList;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.ChatManager;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.filter.MessageTypeFilter;
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.util.StringUtils;
 
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.MessageListener;
 import org.jivesoftware.smack.Roster;
 import org.jivesoftware.smack.RosterEntry;
 
 import edu.depaul.snotg_android.R;
 
  
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class XMPPClient extends Activity {
     private ArrayList<String> messages = new ArrayList<String>();
     private Handler mHandler = new Handler();
     private EditText mRecipient;
     private EditText mSendText;
     private ListView mList;
     //private XMPPConnection connection;
     private JabberSmackAPI c;
     private String username = "milad0207";
     private String password = "lebanon333";
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle icicle) {
 
         super.onCreate(icicle);
         Log.i("XMPPClient", "onCreate called");
        setContentView(R.layout.main);
 
         mRecipient = (EditText) this.findViewById(R.id.recipient);
         Log.i("XMPPClient", "mRecipient = " + mRecipient);
         mSendText = (EditText) this.findViewById(R.id.sendText);
         Log.i("XMPPClient", "mSendText = " + mSendText);
         mList = (ListView) this.findViewById(R.id.listMessages);
         Log.i("XMPPClient", "mList = " + mList);
         setListAdapter();
         // Dialog for getting the xmpp settings
         c = new JabberSmackAPI(this);
         try {
 			c.login(username, password);
 		} catch (XMPPException e) {
 			// TODO Auto-generated catch block
 
             Log.i("XMPPClient", "Failed to login as "+username+": " + e.getMessage());
 		}
         
         // Set a listener to send a chat text message
         Button send = (Button) this.findViewById(R.id.send);
         send.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 String to = mRecipient.getText().toString();
                 String text = mSendText.getText().toString();
 
                 Log.i("XMPPClient", "Sending text [" + text + "] to [" + to + "]");
                 try {
                 	if (text != null)
                 	{
                 		c.sendMessage(text, to);
                 		Log.i("XMPPClient", "Sent text [" + text + "] to [" + to + "]");
                 		messages.add(username + "@gmail.com:");
                         messages.add(text);
                         setListAdapter();
                 	}
                 	else
                 		Log.i("XMPPClient", "Message is emtpy");
 				} catch (XMPPException e) {
 	                Log.i("XMPPClient", "Failed Sending text [" + text + "] to [" + to + "]:" + e.getMessage());
 				}
                
             }
         });
     
     }
 
    
     private void setListAdapter
             () {
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                 R.layout.multi_line_list_item,
                 messages);
         mList.setAdapter(adapter);
     }
     
     public void getMessage (String incomingMSG,String from)
     {
         Log.i("XMPPClient", "Got text [" + incomingMSG + "] from [" + from + "]");
     	if (incomingMSG != null) {
             String fromName = StringUtils.parseBareAddress(from);
             Log.i("XMPPClient", "Got text [" + incomingMSG + "] from [" + fromName + "]");
             messages.add(fromName + ":");
             messages.add(incomingMSG);
             // Add the incoming message to the list view
             mHandler.post(new Runnable() {
                 public void run() {
                     setListAdapter();
                 }
             });
         }
     }
 }
