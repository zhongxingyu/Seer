 // This file contains material supporting section 3.7 of the textbook:
 // "Object Oriented Software Engineering" and is issued under the open-source
 // license found at www.lloseng.com 
 
 import java.io.*;
 import client.*;
 import common.*;
 
 /**
  * This class constructs the UI for a chat client.  It implements the
  * chat interface in order to activate the display() method.
  * Warning: Some of the code here is cloned in ServerConsole 
  *
  * @author Fran&ccedil;ois B&eacute;langer
  * @author Dr Timothy C. Lethbridge  
  * @author Dr Robert Lagani&egrave;re
  * @version July 2000
  */
 public class ClientConsole implements ChatIF 
 {
   //Class variables *************************************************
   
   /**
    * The default port to connect on.
    */
   final public static int DEFAULT_PORT = 5555;
   
   //Instance variables **********************************************
   
   /**
    * The instance of the client that created this ConsoleChat.
    */
   ChatClient client;
 
   
   //Constructors ****************************************************
 
   /**
    * Constructs an instance of the ClientConsole UI.
    *
    * @param host The host to connect to.
    * @param port The port to connect on.
    */
   public ClientConsole(String loginID, String host, int port) 
   {
     try 
     {
       client= new ChatClient(loginID, host, port, this);
     } 
     catch(IOException exception) 
     {
       System.out.println("Error: Can't setup connection!"
                 + " Terminating client. " + exception);
       System.exit(1);
     }
   }
 
   
   //Instance methods ************************************************
   
   /**
    * This method waits for input from the console.  Once it is 
    * received, it sends it to the client's message handler.
    */
   public void accept() 
   {
     try
     {
       BufferedReader fromConsole = 
         new BufferedReader(new InputStreamReader(System.in));
       String message;
 
       while (true)  // **** Changed for E49 RM
       {
         message = fromConsole.readLine();
         client.handleMessageFromClientUI(message);
       }
     } 
     catch (Exception ex) 
     {
       System.out.println
         ("Unexpected error while reading from console!");
     }
     if(!client.isConnected()){ // **** Changed for E49 RM
     	try{
     		client.closeConnection();
     	}
     	catch (Exception e) {
     		client.connectionException(e);
     	}
     }
   }
 
   /**
    * This method overrides the method in the ChatIF interface.  It
    * displays a message onto the screen.
    *
    * @param message The string to be displayed.
    */
   public void display(String message) 
   {
     System.out.println(message);
   }
 
   
   //Class methods ***************************************************
   
   /**
    * This method is responsible for the creation of the Client UI.
    *
    * @param args[0] The host to connect to.
    * @param args[1] The port to connect to.
    */
   public static void main(String[] args) 
   {
     String host = "";
     int port = 0;  //The port number
     String loginID = "";
 
     try
     {
       loginID = args[0];
       host = args[1];
       port = Integer.parseInt(args[2]); // **** Changed for E49 RM
     }
     catch(ArrayIndexOutOfBoundsException e)
     {
       host = "localhost";
       port = DEFAULT_PORT;
     }
   	ClientConsole chat= new ClientConsole(loginID, host, DEFAULT_PORT);
     chat.accept();  //Wait for console data
   }
 }
 //End of ConsoleChat class
