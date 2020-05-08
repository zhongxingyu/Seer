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
 
   
   //Constructors ****************************************************
   
   /**
    * Constructs an instance of the chat client.
    *
    * @param host The server to connect to.
    * @param port The port number to connect on.
    * @param clientUI The interface type variable.
    */
   
   public ChatClient(String host, int port, ChatIF clientUI) 
     throws IOException 
   {
     super(host, port); //Call the superclass constructor
     this.clientUI = clientUI;
     openConnection();
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
 	Pattern login = Pattern.compile("^#login$");
 	Matcher matcherLogin = login.matcher(message);
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
				//System.out.println("asdf");
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
 			this.setPort(Integer.parseInt(matcherSetHost.group(1)));
 		} else if(matcherLogin.find()){ //open connection
 			if(!this.isConnected()){
 				try {
 					this.openConnection();
 				} catch (IOException e) {
 					connectionException(e);
 				}
 				
 			} else {
 				System.out.println("Cannot login host while connected!");
 			}
 		}
 		
 		
	}
 	//changed for E50 RM
	if(this.isConnected()){
 		
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
	} else {
		clientUI.display("Not connected to server!!");
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
   
 //**** Changed for E49 RM
   /**
    * This method overrides the abstract method, is called when an
    * error with the connection occrs
    */
   public void connectionException(Exception e){
 	  System.out.println("The connection has been closed unexpectedly.");
 	  System.exit(0);
   }
   
 }
 //End of ChatClient class
