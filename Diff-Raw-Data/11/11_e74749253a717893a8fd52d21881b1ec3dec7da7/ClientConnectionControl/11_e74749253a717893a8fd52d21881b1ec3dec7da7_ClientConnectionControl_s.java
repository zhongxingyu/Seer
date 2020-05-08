 /*
  * ConnectionControl.java
  *
  * Created on 11 January 2003, 21:39
  */
 
 package client;
 
 import java.io.*;
 import java.net.*;
 import java.nio.*;
 import java.nio.channels.*;
 import java.nio.charset.*;
 import java.util.*;
 import java.util.regex.*;
 
 /**
  *
  * @author  Ken Barber
  */
 public class ClientConnectionControl extends Thread {
     
     // Pointers to GUIControl and Connection info
     private ConnectionInfo conInfo;
     private GUIControl guiControl;
     
     // The channel that we will use for connecting
     private SocketChannel channel;
     
     // The interrupt state
     private boolean interruptState;
         
     /** Creates a new instance of ConnectionControl */
     public ClientConnectionControl(ConnectionInfo ci, GUIControl gui) {
         conInfo = ci;
         guiControl = gui;
         
     }
     
     /** Setup the thread to be interrupted */
     public void setInterrupt() {
         interruptState = true;
     }
     
     /** Is this thread ready to be interrupted? */
     public boolean getInterrupt() {
         boolean tempState = interruptState;
         interruptState = false;
         return tempState;
     }
     
     public void sendMessage(String message) {
         // This is the character encoding parts required
         Charset charset = Charset.forName("ISO-8859-1");
         CharsetDecoder decoder = charset.newDecoder();
         CharsetEncoder encoder = charset.newEncoder();
         
         // Allocate buffers for receiving
         ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);
         CharBuffer readCharBuffer = CharBuffer.allocate(1024);
         
         try {
             channel.write(encoder.encode(CharBuffer.wrap("send:" + message + "\n")));
         } catch(Exception e) {
             guiControl.printError("Problem with sending message: " + e);
         }
     }
         
           
     /* The main part of the thread */
     public void run() {
         
         /* Thread is not to be interrupted. */
         interruptState = false;
         
         // This is the character encoding parts required
         Charset charset = Charset.forName("ISO-8859-1");
         CharsetDecoder decoder = charset.newDecoder();
         CharsetEncoder encoder = charset.newEncoder();
         
         // Allocate buffers for receiving
         ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);
         CharBuffer readCharBuffer = CharBuffer.allocate(1024);
         
         // Inform the user we are in the process of connecting
         guiControl.setConnected(guiControl.CONNECTING);
         
         // The selector
         Selector selector = null;
 
         // Set the IP and port to connect to
         InetSocketAddress sockAddress = new InetSocketAddress(conInfo.getServer(), 6666);
         
         // Open a connection and wait till it is connected
         try {
             
             channel = SocketChannel.open();
             channel.configureBlocking(false);
             
             channel.connect(sockAddress);
 
             for(int loop = 0; loop < 30; loop++) {
                 
                 // Wait if the channel is connected
                 if(channel.finishConnect()) {
                     break;
                 } else {
                     Thread.sleep(500);
                 }
             }
             
         } catch (Exception e) {
             guiControl.printError("Problem connecting: " + e);
         }
         
         // Inform the user we are connected
         guiControl.setConnected(guiControl.CONNECTED);
         
         // Now signup
         try {
             channel.write(encoder.encode(CharBuffer.wrap("signup:" + conInfo.getUsername() + "\n")));
             channel.write(encoder.encode(CharBuffer.wrap("list:*\n")));
         } catch (Exception e) {
             guiControl.printError("Problem with signup: " + e);
         }
         
         // Now register this channel with a selector
         try {
             selector = Selector.open();
             channel.register(selector, SelectionKey.OP_READ);
         } catch (Exception e) {
             guiControl.printError("Problem with registering selector: " + e);
         }
         
         // Loop infinitely (or at least until this thread is interrupted)
         while(getInterrupt() != true) {
             
             int selectResponse = 0;
             
             try {
                 // Wait until there is something to read
                 selectResponse = selector.select(500);
             } catch (Exception e) {
                 guiControl.printError("Problem with select: " + e);
             }
             
             if(selectResponse > 0) {
                 // Obtain set of actions
                 Set readyKeys = selector.selectedKeys();
                 Iterator readyItor = readyKeys.iterator();
 
                 // Deal with each action
                 while(readyItor.hasNext()) {
 
                     // Get the action key
                     SelectionKey key = (SelectionKey)readyItor.next();
 
                     // Remove this key from the set
                     readyItor.remove();
                     
                     if (key.isReadable()) {  
                             
                         // Rseets buffers
                         readBuffer.clear();                        
                         readCharBuffer.clear();
                             
                         // Get channel
                         SocketChannel socketchannel = (SocketChannel)key.channel();
                                         
                         if(!socketchannel.isConnected()) {
                             guiControl.printError("Socket not connected");
                         }
                             
                         // Read in the buffer
                         try {
                             if(socketchannel.read(readBuffer) == -1) {
                                 // Deregister the socketchannel if end-of-stream
                                 // Do something which makes sense for clients
                                 socketchannel.close();
                                 continue;
                             }
                         } catch(Exception e) {
                             guiControl.printError("Error receiving data on network: " + e);
                         }
 
                         readBuffer.flip();
                         decoder.decode(readBuffer, readCharBuffer,false);
                         readCharBuffer.flip();
                                                
                         String readString = readCharBuffer.toString();
                             
                         /* Ensure strings are of reasonable length */
                         if(readString.length() < 6) {
                             // If this happens, the server has sent an invalid command.
                             // Deal with it better than this!!
                             continue;
                         }
 
                         // Remove line-break
                         readString = readString.substring(0, readString.length() -1);
                         
                         // Remove carriage-return if there is one
                         if(readString.substring(readString.length() -1).equals("\r")) {
                             readString = readString.substring(0, readString.length() -1);
                         }
                         
                         // Break up commands, because there may be many in the buffer
                         String serverMsg[] = readString.split("\n");
                         
                         // Cycle through each server message
                         for(int loop = 0; loop < serverMsg.length; loop++) {
                                                 
                             // Break up string into command and value
                             String command[] = serverMsg[loop].split(":", 3);
                             
                             if(command[0].equals("GOB")) {
                                 if(command[1].equals("signup")) {
                                     // Add this new user to the userlist
                                     guiControl.addUser(command[2]);
                                     guiControl.statusMessage("New user \"" + command[2] + "\"");
                                 } else if(command[1].equals("quit")) {
                                     String params[] = command[2].split(",", 2);
                                     guiControl.statusMessage("User \"" + params[0] + "\" has disconnected because \"" + params[1] + "\"");
                                     guiControl.deleteUser(params[0]);
                                 } else if(command[1].equals("list")) {
                                     String tempString = command[2];
                                    tempString = tempString.substring(0, tempString.length() -1);
                                     String users[] = tempString.split(",");
                                     guiControl.clearList();
                                     
                                     for(int ind = 0; ind < users.length; ind++) {
                                         guiControl.addUser(users[ind]);
                                     }
                                 } else if(command[1].equals("greeting")) {
                                     guiControl.statusMessage(command[2]);
                                 } else if(command[1].equals("send")) {
                                     String param[] = command[2].split(",", 2);
                                     guiControl.userMessage(param[0], param[1]);
                                 } else {
                                     // Just send any received messages to the textarea for now
                                     guiControl.statusMessage(serverMsg[loop]);
                                 }
                             }
                         }
                     }
                 }
             }
         }
 
         /*
          * This is a fix for a race condition. Because setting the interrupt
          * will make the script fall through, sending a Thread interrupt needs to have 
          * something it interrupts. Otherwise, it will interrupt the disconnction
          * below.
          *
          * I need to have a closer look at the way we do this, because this seems
          * ugly.
          */
         try {
             sleep(30000);
         } catch(Exception e) {
             guiControl.printError("Problem with sleep: " + e);
         }
         
         /* Attempt to close the connection, wait till it is closed */
         try {
             selector.close();
             channel.close();
             
             /* Inform the user we are disconnecting */
             guiControl.setConnected(guiControl.DISCONNECTING);
             
             for(int loop = 0; loop < 120; loop++) {
                 if(channel.isConnected()) {
                     Thread.sleep(500);
                 } else {
                     break;
                 }
             }
         } catch(Exception e) {
             guiControl.printError("Problem disconnecting: " + e);
         }
         
         /* Update the control tab, we are disconnected */
         guiControl.setConnected(guiControl.DISCONNECTED);
         
         /* Put a message in the text area, stating we have disconnected */
         guiControl.statusMessage("Disconnected");
     
     } /* End of run() */
 }
