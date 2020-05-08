 package com.android.tencere.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.filter.MessageTypeFilter;
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smack.util.StringUtils;
 
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.content.Context;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 
 
 // Main class
 public class Menu extends Activity {
     private ArrayList<String> messages = new ArrayList();
     private Handler mHandler = new Handler();
     private EditText mSendText;
     private ListView mList;
     private XMPPConnection connection;
     public static String server = "buluruzbirsey@appspot.com";
     public String partner;
     public Boolean is_started = false;
     public ProgressDialog dialog;
     
     public String myName = android.os.SystemClock.currentThreadTimeMillis()*5 + " Bey";
     public String myAge = android.os.SystemClock.currentThreadTimeMillis()/3 + "";
     public String mySex = "1";
    
     public String partnerName = "Stranger"; //initially not known
     public String partnerSex = null; // initially not known
     public String partnerAge = null; //initially not known
     public String partnerLocation = null; //initially not known
     
 
     public Button end;
     public Button newConversation;
     
     
     public LocationManager locmgr = null;
    public String myLocation;
 	    
     // Function to send a message
     public void sendMessage(String to, String text){
         Log.e("XMPPClient", "Sending text [" + text + "] to [" + to +"]");
         Message msg = new Message(to, Message.Type.chat);
         msg.setBody(text);
         connection.sendPacket(msg);
     }
     // End of sendMessage Function
     
     //Function to update Message List
     public void updateMessages(){
     	mHandler.post(new Runnable() {
             public void run() {
                 setListAdapter(); 
             }
         });
     }
     // End of updateMessages Function
     
     //Function to request a conversation
     public void requestConversation(){
     	
     	double[] location = getGPS();
     	myLocation = Double.toString(location[0]) + ":" + Double.toString(location[1]);
         
     	//Send a PENDING_CONVERSATION
         sendMessage(server, custom_messages.PENDING_CONVERSATION + ":" + myLocation);
         dialog = ProgressDialog.show(Menu.this, "", "Waiting for a match...", true);
     }
     // End of requestConversation Function
     
     
     
     //Function to pick your own info from the trade message coming from the server
     public String infoPicker (String msg, String myinfo) {
 
 		String info1 = msg.split(":")[1]; //get the the info1
 		String info2 = msg.split(":")[2]; //get the the info2
 		
 		if (info1.equals(myinfo)) //info1 is mine
 			return info2;
 		else // info2 is mine
 			return info1;
 				
     
     }
     // End of infoPicker Function
     
     //Function to handle server messages
     public void handleCustomMessage(String msg){
     	//messages.add("******message came: " + msg);  updateMessages(); //DEBUG
     	String command = msg.split(":")[0];	
     	//messages.add("******hence the command is: " + command);  updateMessages(); //DEBUG
 
     	Log.i("XMPPClient",command);
 
     	
         // START_CONVERSATION
     	if (command.equals(custom_messages.START_CONVERSATION)){
     		
     		partner = msg.split(":")[1];
     		is_started = true;
     		dialog.dismiss();
     		
             messages.add("---Conversation Started---");
       
            // end.setVisibility(View.VISIBLE); //end is visible
            // newConversation.setVisibility(View.GONE); //new is invisible
             
             updateMessages();
     	}
     	//
 
         // DELETE_CONVERSATION
     	if (command.equals(custom_messages.DELETE_CONVERSATION)){
             is_started = false;
             messages.add("---Disconnected---");
         //  end.setVisibility(View.INVISIBLE); //end is invisible
         //  newConversation.setVisibility(View.VISIBLE); //new is visible
             
             updateMessages();
 
     	}
     	//
 
     	 // TRADE_NAME
     	if (command.equals(custom_messages.TRADE_NAME)){
     		//messages.add("MESSAGE THAT CAME TO ME: " + msg); updateMessages(); //DEBUG
     		//messages.add("my name was: " + myName); updateMessages(); //DEBUG
     		String name = infoPicker(msg, myName); //find whichever one belongs to the partner   		
             messages.add("Your Partner's name is: " + name);
             partnerName = name; //update partnerName
             
             updateMessages();
             
 
     	}
     	//
 
         // TRADE_SEX
     	if (command.equals(custom_messages.TRADE_SEX)){
     		  		
     		String sex = infoPicker(msg, mySex); //find whichever one belongs to the partner
     		
     		if (sex.equals("1")){
     			messages.add("Your Partner is a man");
     			partnerSex = "M"; //update partnerSex
     		}
     		if (sex.equals("2")){
     			messages.add("Your Partner is a woman");
     			partnerSex = "F"; //update partnerSex
     		}
     		
             updateMessages();
             
             //****************************************************************
     		//TODO: handle cases other than 1 & 2
     		//TODO: string for sex? integer? char?
         
     	}
     	//
 
         // TRADE_AGE
     	if (command.equals(custom_messages.TRADE_AGE)){
     		
     		String age = infoPicker(msg, myAge); //find whichever one belongs to the partner
     		messages.add("Your Partner is " + age + " years old.");
     		partnerAge = age; //update partnerAge
             updateMessages();
             
     	}
     	//
 
         // TRADE_LOCATION
     	if (command.equals(custom_messages.TRADE_LOCATION)){
     		
     		String location = infoPicker(msg, myLocation); //find whichever one belongs to the partner
     		messages.add("Your Partner is from " + location);
     		partnerLocation = location; //update partnerLocation
             updateMessages();
             
     	}
     	//
     	
 
     	
     }
     // End of handleCustomMessage function
     
     
     // Button Functions
     
     //End button
     public void endClick(View view) {
 
 			sendMessage(server,custom_messages.DELETE_CONVERSATION);            
 			is_started = false; //make us note of it
             messages.add("---Disconnected---");
             updateMessages();
            
          // end.setVisibility(View.INVISIBLE); //end is invisible
          // newConversation.setVisibility(View.VISIBLE); //new is visible
             
 	}
     
     
     
     //NewConversation button
     public void newconversationClick(View view) {
     	
        // end.setVisibility(View.VISIBLE); //end is visible
        // newConversation.setVisibility(View.INVISIBLE); //new is invisible
     	
 		requestConversation();
 		
     }
     //
     //Send button
     public void sendClick(View view) {
     	
         String text = mSendText.getText().toString();
 
         if (is_started && !text.equals("")) {
         	sendMessage(partner,text);
         	messages.add("You: " + text);
             setListAdapter();
             
         }
         else{
         	//sendMessage(server,text);
         }
         
     	mSendText.setText("");
     	
     }
     //
     
     //Name button
     public void nameClick(View view) {
     	sendMessage(server,custom_messages.TRADE_NAME + ":" + myName);
     }
     //
     
     //Age button
     public void ageClick(View view) {              
     	sendMessage(server,custom_messages.TRADE_AGE + ":" + myAge);
 	}
     //
     
     //Sex button
 	public void sexClick(View view) {
 		int sexForServer;
 		if (mySex=="M") sexForServer = 1;
 		else sexForServer = 2;
     	sendMessage(server,custom_messages.TRADE_SEX + ":" + sexForServer);                
 	}
 	//
 	
 	//Location button
 	public void locationClick(View view) {
     	sendMessage(server,custom_messages.TRADE_LOCATION + ":" + myLocation);                
 	}
 	//
 		
 	
 	
        
     //For connecting to server
     public void connectServer(){
     	dialog = ProgressDialog.show(Menu.this, "", "Connecting to server...", true);
     	// Connection Settings
         String host = "mageroya.com";
         String port = "5222";
         String service = "mageroya.com";
   
         // Create a connection
         ConnectionConfiguration connConfig = new ConnectionConfiguration(host, Integer.parseInt(port), service);
         final XMPPConnection connection = new XMPPConnection(connConfig);
 
         try {
             connection.connect();
             Log.i("XMPPClient", "[SettingsDialog] Connected to " + connection.getHost());
         } catch (XMPPException ex) {
             Log.e("XMPPClient", "[SettingsDialog] Failed to connect to " + connection.getHost());
             Log.e("XMPPClient", ex.toString());
             setConnection(null);
         }
         try {
         	connection.loginAnonymously();
         	Log.i("XMPPClient", "Logged in as " + connection.getUser());
             // Set the status to available
             Presence presence = new Presence(Presence.Type.available);        	
             connection.sendPacket(presence);
 
             setConnection(connection);
             
         } catch (XMPPException ex) {
             Log.e("XMPPClient", "[SettingsDialog] Failed to log in as anonymous" );
             Log.e("XMPPClient", ex.toString());
             setConnection(null);
         }
         
         dialog.dismiss();
     }
     // End of connectServer function
     
     // Called on the activity creation.
     @Override
     public void onCreate(Bundle icicle) {
     	
     	//check for internet connection?
 
     	
         super.onCreate(icicle);
         setContentView(R.layout.main);
         
         mSendText = (EditText) this.findViewById(R.id.sendText);
         mList = (ListView) this.findViewById(R.id.listMessages);
         end = (Button) this.findViewById(R.id.end);
 
     	newConversation = (Button) this.findViewById(R.id.newconversation);
     	locmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
     	  
     	
         setListAdapter();        
         
         connectServer();
         requestConversation();
 
     }
     // End of onCreate function
 
     // Called on the activity destroy.
     @Override
     public void onDestroy() {
     	sendMessage(server,custom_messages.DELETE_CONVERSATION);                
     }
     // End of destroy function
 
     
     /**
      * Called when a connection is established with the XMPP server
      * @param connection
      */
     public void setConnection (final XMPPConnection connection) {
         this.connection = connection;
         if (connection != null) {
             // Add a packet listener to get messages sent to us
             PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
             connection.addPacketListener(new PacketListener() {
                 public void processPacket(Packet packet) {
                     Message message = (Message) packet;
                     if (message.getBody() != null) {
                         String fromName = StringUtils.parseBareAddress(message.getFrom());
                         Log.i("XMPPClient", "Got text [" + message.getBody() + "] from [" + fromName +"]");
                     	String msg = message.getBody();
                         
                         if (fromName.equals(server)){ //If this message is from conversation server
                         	handleCustomMessage(msg); //Handle it with this function
                         }
                         else{ // If this is a regular message
                         	if (partnerSex==null && partnerAge==null) { //nothing known besides name
                                 messages.add(partnerName + ": " + msg); //display only the name (with the message)
                                 updateMessages();		
                         	}
                         	else { //at least one extra thing is known
                         		if (partnerSex==null) { //only partnerAge known
                         		     messages.add(partnerName + " (" + partnerAge +")" + ": " + msg);
                                      updateMessages();		
                         		}
                         		else if (partnerAge==null) { //only partnerSex known
                         			 messages.add(partnerName + " (" + partnerSex +")" + ": " + msg);
                                      updateMessages();		
                         		}
                         			 else {//both known
                             			 messages.add(partnerName + " (" + partnerSex + ", " + partnerAge + ")" + ": " + msg);
                                          updateMessages();                    				 
                         		     }
                         		
                         	}
 
                         }
                     }
                 }
             }, filter);
         }
     }
     // End of setConnection function
     
     //Function to add messages to list
     private void setListAdapter() {
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.multi_line_list_item, messages);
         mList.setAdapter(adapter);
     }
     // End of setListAdapter function
     LocationListener onLocationChange=new LocationListener() {
         public void onLocationChanged(Location loc) {
             //sets and displays the lat/long when a location is provided
             String latlong = loc.getLatitude() + ":" + loc.getLongitude();   
             myLocation = latlong;
         }
          
         public void onProviderDisabled(String provider) {
         // required for interface, not used
         }
          
         public void onProviderEnabled(String provider) {
         // required for interface, not used
         }
          
         public void onStatusChanged(String provider, int status,
         Bundle extras) {
         // required for interface, not used
         }
     };
     
     //pauses listener while app is inactive
     @Override
     public void onPause() {
         super.onPause();
         locmgr.removeUpdates(onLocationChange);
     }
     
     //reactivates listener when app is resumed
     @Override
     public void onResume() {
         super.onResume();
         locmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,10000.0f,onLocationChange);
     }
 
     
     private double[] getGPS() {
    	 LocationManager lm = (LocationManager) getSystemService(
    	  Context.LOCATION_SERVICE);
    	 List<String> providers = lm.getProviders(true);
 
    	 Location l = null;
    	 
    	 for (int i=providers.size()-1; i>=0; i--) {
    	  l = lm.getLastKnownLocation(providers.get(i));
    	  if (l != null) break;
    	 }
    	 
    	 double[] gps = new double[2];
    	 if (l != null) {
    	  gps[0] = l.getLatitude();
    	  gps[1] = l.getLongitude();
    	 }
 
    	 return gps;
    	}
 }
 // End of class Menu
