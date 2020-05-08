 // This file contains material supporting section 3.7 of the textbook:
 // "Object Oriented Software Engineering" and is issued under the open-source
 // license found at www.lloseng.com 
 
 package client;
 
 import java.util.regex.*;
 import ocsf.client.*;
 import common.*;
 import java.io.*;
 
 /**
  * This class overrides some of the methods defined in the abstract
  * superclass in order to give more functionality to the client.
  *
  * @author Dr Timothy C. Lethbridge
  * @author Dr Robert Lagani&egrave;
  * @author Fran&ccedil;ois B&eacute;langer
  * @version July 2000
  */
 public class ChatClient extends AbstractClient
 {
   //Instance variables **********************************************
   
   /**
    * The interface type variable.  It allows the implementation of 
    * the display method in the client.
    */
   ChatIF clientUI; 
   
   /**
    * Stores the login ID
    * Required for client to login to the server
    * Provided as command line argument
    */
   private String loginID; //changed by RM for E51
 
   
   //Constructors ****************************************************
   
   /**
    * Constructs an instance of the chat client.
    *
    * @param host The server to connect to.
    * @param port The port number to connect on.
    * @param clientUI The interface type variable.
    */
   
   public ChatClient(String loginID, String host, int port, ChatIF clientUI) 
     throws IOException 
   {
     super(host, port); //Call the superclass constructor
     //****  changed for E51 RM
   /*  if(loginID == null || loginID == ""){
     	throw new IOException("Requires a loginID to connect!");
     }
  */   
     this.clientUI = clientUI;
     this.loginID = loginID;
     try {
     	openConnection();
     }
     catch (Exception e)
     {
     	System.out.println("Failed to open connection");
     }
     //*** changed for E51 RM
     
 	try
 	{
 		sendToServer("#login " + loginID);
 	}
 	catch(IOException e)
 	{
 		clientUI.display
 		("Could not send message to server.  Awaiting command.");
 	}
 	
   }
 
   
   //Instance methods ************************************************
     
   /**
    * This method handles all data that comes in from the server.
    *
    * @param msg The message from the server.
    */
   public void handleMessageFromServer(Object msg) 
   {
     clientUI.display(msg.toString());
   }
 
   /**
    * This method handles all data coming from the UI            
    *
    * @param message The message from the UI.    
    */
   //changed for E50 RM
   public void handleMessageFromClientUI(String message)
   {
 	//look for commands
 	Pattern command = Pattern.compile("^#.*?$");
 	Matcher matcher = command.matcher(message);
 	
 	//commands
	Pattern setHost = Pattern.compile("^#sethost (\\w*?)$");
 	Matcher matcherSetHost = setHost.matcher(message);
 	Pattern logoff = Pattern.compile("^#logoff$");
 	Matcher matcherLogoff = logoff.matcher(message);
 	Pattern quit = Pattern.compile("^#quit$");
 	Matcher matcherQuit = quit.matcher(message);
 	Pattern setPort = Pattern.compile("^#setport (\\d*?)$");
 	Matcher matcherSetPort = setPort.matcher(message);
 	Pattern login = Pattern.compile("^#login (\\w*?)$");
 	Matcher matcherLogin = login.matcher(message);
 	Pattern getHost = Pattern.compile("^#gethost$");
 	Matcher matcherGetHost = getHost.matcher(message);
 	Pattern getPort = Pattern.compile("^#getport$");
 	Matcher matcherGetPort = getPort.matcher(message);
 
 
 	if(matcher.find()){ //is a command
 		
 		//look for which commands
 		if(matcherSetHost.find()){  //set host
 			if(!this.isConnected()){
 				this.setHost(matcherSetHost.group(1));
 			} else {
 				System.out.println("Cannot change host while connected!");
 			}
 			
 		} else if(matcherLogoff.find()){ //log off
 			try{
 				closeConnection();
 			} catch (IOException e){
 				connectionException(e);
 			}
 		} else if(matcherQuit.find()){ //quit
 			try{
 				closeConnection();
 				quit();
 			} catch (IOException e){
 				connectionException(e);
 			}
 		} else if(matcherSetPort.find()){ //set port
 			if(!this.isConnected()){
 				this.setPort(Integer.parseInt(matcherSetPort.group(1)));
 			} else {
 				clientUI.display("Cannot change port while connected!");
 			}
 			
 		} else if(matcherLogin.find()){ //open connection, must also set loginID and send to server
 			if(!this.isConnected()){
 				try {
 					this.setLoginID(matcherLogin.group(1));
 					this.openConnection();				
 					sendToServer(message);
 				} catch (IOException e) {
 					connectionException(e);
 				}
 				
 			} else {
 				System.out.println("Cannot login host while connected!");
 			}
 		} else if(matcherGetHost.find()) {
 			clientUI.display(this.getHost());
 		} else if(matcherGetPort.find()) {
 			clientUI.display(Integer.toString(this.getPort()));
 		} 
 	} else {
 	
 	
 	//changed for E50 RM
 
 		
 		try
 		{
 			sendToServer(message);
 		}
 		catch(IOException e)
 		{
 			clientUI.display
 			("Could not send message to server.  Terminating client.");
 			quit();
 		}
 	}
 	  
   }
   
   /**
    * This method terminates the client.
    */
   public void quit()
   {
     try
     {
       closeConnection();
     }
     catch(IOException e) {}
     System.exit(0);
   }
   
   /**
    * This method sets the loginID
    * @param lID the new client loginID
    */
   public void setLoginID(String lID){
 	  loginID = lID;
   }
   
 //**** Changed for E49 RM
   /**
    * This method overrides the abstract method, is called when an
    * error with the connection occurs
    */
   public void connectionException(Exception e){
 	  System.out.println("The connection has been closed unexpectedly. Awaiting Commands.");
   }
   
   /**
    * Gets this client's login ID
    * @return loginID
    */
   public String getLoginID(){
 	  return loginID;
   }
   
 }
 //End of ChatClient class
