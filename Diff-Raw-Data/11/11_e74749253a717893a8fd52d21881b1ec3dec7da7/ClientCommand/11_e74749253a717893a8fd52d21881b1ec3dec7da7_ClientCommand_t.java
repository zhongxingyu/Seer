 /*
  * ClientCommand.java
  *
  * Created on 7 December 2002, 23:40
  */
 
 package server;
 
 import java.io.*;
 import java.net.*;
 import java.nio.*;
 import java.nio.channels.*;
 import java.nio.charset.*;
 import java.util.*;
 import java.util.regex.*;
 
 /**
  * This object deals with all client commands.
  *
  * @author  Ken Barber
  */
 public class ClientCommand {
     
     /* A pointer to the passed UserData object. */
     private UserData userData;
     
     /* These will be used for network commands. See the constructor. */
     private Charset charset;
     private CharsetDecoder decoder;
     private CharsetEncoder encoder;
     
     /** 
      * Creates a new instance of ClientCommand.
      */
     public ClientCommand(UserData ud) {
         /* Keep a local pointer to the user data object */
         userData = ud;
 
         /* Setup charset,decoder and encoder for use by networking commands*/
         charset = Charset.forName("ISO-8859-1");
         decoder = charset.newDecoder();
         encoder = charset.newEncoder();
     }
 
     /** 
      * Responsible for the proper signup of new clients. 
      */
     public void clientSignup(String un, SocketChannel sc) {
         /* Check that the username is valid */
         if(Pattern.matches("[a-zA-Z0-9_+-]{3,15}", un) == false) {
             /* Name is invalid, give error */
             returnError("I\'m sorry, usernames must be between 3 and 15 characters and only alphanumeric", sc);
             
             /* Notify on terminal */
             Main.consoleOutput("Attempt to sign-in with invalid username (not shown)");
         } else {
             /* See if the user registration is valid */
             if(userData.insertData(un, sc)) {
                 /* Send a message to all users about the new user */
                 messageAll("signup", un);
 
                 /* Notify on the terminal that new user has signed up */
                 Main.consoleOutput("New user signed in: \"" + un + "\"" + " from " + userData.getHostIP(sc));
             } else {
                 /* Let the user know that there was an error with signup. */
                 returnError("Already registered, or username taken", sc);
 
                 /* Notify on the terminal about the new user */
                 Main.consoleOutput("Attempt to sign in with duplicate username: " + un);            
             }
         }
     }
 
     /**
      * This method is responsible for the proper terminate of a user
      * when a quit command is submitted.
      */
     public void clientQuit(String pa, SocketChannel sc) {
         
         /* Make sure the quit reason is ... err ... reasonable */
         if(Pattern.matches("[ \ta-zA-Z0-9!\"#$%&'()*+,-./:;<=>?@\\^_`{|}~]{0,64}", pa) == false) {
             /* Reason doesn't match correct criteria, just clear it */
             pa = "";
         }
         
         if(userData.isRegistered(sc)) {
             /* Get the username using the given SocketChannel */
             String userName = userData.getName(sc);
 
             /* Remove the users entry from the UserData object */
             userData.deleteEntry(sc);
         
             /* Let everyone know that the user has quit */
             messageAll("quit", userName + "," + pa);
         
             /* Notify on the terminal that the user has quit */
             Main.consoleOutput("User quit: \"" + userName + "\" because \"" + pa + "\"");
             
         } else {
             /*The user never logged in, just log to console */
             Main.consoleOutput("An unknown user quit because " + pa);
         }
 
         try {
             /* Close the channel */
             sc.close();
         } catch(IOException e) {
             Main.programExit("Error closing socket: " + e);
         }
 
     }
     
     /**
      * Deal with a request to see the list of users.
      */
     public void clientList(String se, SocketChannel sc) {
         
         if(se.equals("*")) {
         
             /* Obtain an array which contains a list of the currently
              * logged in users */
             Object[] users = userData.listNames();
 
             /* Parse the array of users, and create a single comma delimited
              * string */
             String list = new String();
             for(int loop = 0; loop <= (users.length -1); loop++) {
                list = list + users[loop];
                if(loop < (users.length -1)) {
                    list = list + ",";
                }
             }           
         
             /* Return the list of users to the user who requested it */
             message("list", list, sc);
         } else {
             returnError("List parameter not supported", sc);
         }
     }
     
     /**
      * Deal with requests for a user message to be sent.
      */
     public void clientSend(String se, SocketChannel sc) {
         /* Make sure the message to send is using good characters */
         if(Pattern.matches("[ \ta-zA-Z0-9!\"#$%&'()*+,-./:;<=>?@\\^_`{|}~]{0,512}", se) == false) {
             /* Reason doesn't match correct criteria, just clear it */
             returnError("The message you have sent has invalid characters or is too long.", sc);
         } else {
             /* Send the message to all users */
             messageAll("send", userData.getName(sc) + "," + se);
         }
     }
     
     /**
      * This method is generic and will return an error to a user.
      */
     public void returnError(String err, SocketChannel sc) {
         /* Send the error message to the requested SocketChannel */
         message("fail", err, sc);
     }
     
     /** 
      * This method will send a message to a SocketChannel in the GOB protocol 
      * format.
      */
     private void message(String type, String msg, SocketChannel sc) {
         try {
             /* Write the message to the SocketChannel */
             sc.write(encoder.encode(CharBuffer.wrap("GOB:" + type + ":" + msg + "\n")));
         } catch (IOException e) {    /* Is the SocketChannel closed? */
             /* Force a client quit */
             clientQuit("Error messaging users socket.", sc);
         } 
     }
 
     /**
      * This method is an extended form of message. It will notify _all_ signed in
      * users.
      */
     private void messageAll(String type, String msg) {
         /* Obtain a list of all SocketChannels */
         Object[] socketchannels = userData.listSockets();
             
         /* Cycle through each SocketChannel, and message each one in turn */
         for(int loop = 0; loop <= (socketchannels.length -1); loop++) {
             message(type, msg, (SocketChannel)socketchannels[loop]);
         }
     }
 }
