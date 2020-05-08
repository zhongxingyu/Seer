 package com.mobileindustrycast;
 
 
 
 import android.app.Activity;
 
 
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.SASLAuthentication;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.Message;
 
 
 
 
 
 /**
  * Gather the xmpp settings and create an XMPPConnection
  */
 public class XMPP_setting extends Activity {
     //for final server use
	public  final String HOST = "Velington-PC";//"http://velington-pc"
 	public  final int PORT_NUMBER = 5222 ;
 	public  final String SERVICE = "conference.velington-pc";
 	public  final String RECIPIENT = "industrycast@conference.velington-pc";
 	public  final String LOGIN="boris";
 	public  final String PASSWORD="malkoy666";
 
 
 
 	Message message;
 
 
 	XMPPConnection connection;	  
 	
 		
 	public void connect()
 	{	
 		
 		//Configuring connection parameters with the setting predefined by team or user's input if available
 		ConnectionConfiguration config = new ConnectionConfiguration(HOST,PORT_NUMBER, SERVICE);
 		
 		// config.setDebuggerEnabled(true); <--helpful if you are encountering a problem on the XMPP side(LogCat output)
 
 
 		connection = new XMPPConnection(config);
 
 		//Connecting to selected host and service
 		try {
 			SASLAuthentication.supportSASLMechanism("PLAIN", 0);
 			connection.connect();
 		} catch (XMPPException ex) {
 			System.out.println("Exception at connect");
 			System.out.println(ex.toString());
 		}
 	}
 	
     //Set of functions necessary to perform login(from user point perspective)
 	public void login() throws XMPPException {
 
 		try {
 			//user login. Needs to match the data from the DB associated with OpenFire Server
 			connection.login(LOGIN, PASSWORD);						
 		} catch (XMPPException ex) {
 			System.out.println(ex.toString());
 		}
 	}
     
     
     
     //Function used to disconnect from the server connection(may be necessary if the server doesn't kick idle users) to be used in onDestruct()
     public void disconnect() {
         connection.disconnect();
     }
     
 
 }
