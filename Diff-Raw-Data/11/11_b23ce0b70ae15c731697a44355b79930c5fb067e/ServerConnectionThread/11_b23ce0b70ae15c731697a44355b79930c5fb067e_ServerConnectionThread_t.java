 /*
  * ServerConnectionThread.java
  *
  * Created on 13 September 2003, 18:38
  */
 
 /*
     Copyright (C) 2003,2004 Ken Barber
  
     This file is part of Gob Online Chat.
 
     Gob Online Chat is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 2 of the License, or
     any later version.
 
     Gob Online Chat is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Gob Online Chat; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
 package sh.bob.gob.client.network;
 
 import sh.bob.gob.shared.communication.*;
 import sh.bob.gob.shared.configuration.*;
 import sh.bob.gob.client.controllers.*;
 import sh.bob.gob.client.*;
 
 import javax.swing.*;
 import java.io.*;
 import java.net.*;
 import java.nio.*;
 import java.nio.channels.*;
 import java.nio.charset.*;
 import java.util.*;
 import java.util.regex.*;
import java.util.logging.*;
 import java.beans.*;
 
 /**
  * This class is a thread class intended to connect to the Gob server and 
  * manage all communication.
  *
  * @author  Ken Barber
  */
 public class ServerConnectionThread implements Runnable {
     
     private GUIControl guiControl;
     private ConnectionInfo connectionInfo;
     
     /* The channel that we will use for connecting */
     private SocketChannel channel;
     
     private MessageBox mbox;
     
     private SplitBuffer splitBuffer;
         
     /* The interrupt state */
     private boolean interruptState;    
     
     /** Creates a new instance of ServerConnectionThread */
     public ServerConnectionThread(GUIControl gc, ConnectionInfo ci) {
         guiControl = gc;
         connectionInfo = ci;
 
         splitBuffer = new SplitBuffer();
         splitBuffer.setSplitBuffer(null);
         
         Network nw = new Network();
         nw.setMaxBufferSize(8092);
         nw.setMaxObjectSize(1024);
         nw.setMaxObjectsInBuffer(8);
         nw.setSplitBufferTimeout(300);
         
         mbox = new MessageBox(nw);
     }
                 
     /** 
      * Setup the thread to be interrupted.
      */
     public void setInterrupt() {
         interruptState = true;
     }
     
     /** 
      * Test if this thread has been set to be interrupted.
      */
     public boolean getInterrupt() {
         boolean tempState = interruptState;
         interruptState = false;
         return tempState;
     }
     
     /**
      * Sends a communication Bean of data to the server.
      *
      * @param obj Bean to send
      */
     public void sendData(Object obj) {
         
         try {
             mbox.sendData(channel, obj);
         } catch (IOException ex) {
             /* Disconnect the channel ... */
             guiControl.statusMessage("Problem with sending command: " + ex);
         }
     }
     
     public void run() {
         
         System.out.println("Starting connection thread.");
         
         /* Thread is not to be interrupted. */
         interruptState = false;
 
         /* Inform the user we are in the process of connecting */
         guiControl.setConnected(GUIControl.CONNECTING);
 
         /*
          * The following code prepares the network connectivity 
          */
         
         /* This is the character encoding parts required for the networking */
         Charset charset = Charset.forName("ISO-8859-1");
         CharsetDecoder decoder = charset.newDecoder();
         CharsetEncoder encoder = charset.newEncoder();
         
         /* Allocate buffers for receiving */
         ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);
         CharBuffer readCharBuffer = CharBuffer.allocate(1024);
         
         /* The selector for reception of server messages */
         Selector selector = null;
 
         //guiControl.statusMessage("About to create InetAddress");
         
         /* Create an InetAddress */
         InetAddress inetAddress;
         try {
             inetAddress = InetAddress.getByName(connectionInfo.getServer());
         } catch (Exception e) {
             guiControl.statusMessage("Problem resolving host: " + e);
             
             /* Update the control tab, we are disconnected */
             guiControl.setConnected(guiControl.DISCONNECTED, "Problem resolving host");
             
             return;
         }            
             
         
         /* Set the IP and port to connect to */
         InetSocketAddress sockAddress = new InetSocketAddress(inetAddress, 6666);
         
         /*
          * Now open a connection, signup and register it with a selector.
          */
         
         /* Open a connection and wait till it is connected */
         try {
             
             channel = SocketChannel.open();
             channel.configureBlocking(false);
             
             channel.connect(sockAddress);
             
 
             for(int loop = 0; loop < 30; loop++) {
                 
                 // Wait if the channel is connected
                 if(channel.finishConnect()) {
                     break;
                 } else {
                     Thread.sleep(1000);
                 }
             }
             
         } catch (Exception e) {
             guiControl.statusMessage("Problem connecting: " + e);
             
             /* Update the control tab, we are disconnected */
             guiControl.setConnected(guiControl.DISCONNECTED, "Problem connecting");
             
             return;
         }
 
         SignOn so = null;
         try {
             so = new SignOn (
                 connectionInfo.getUsername()
                 );
         } catch (Exception ex) {
             guiControl.statusMessage("Problem creating SignOn data bean: " + ex);
 
             /* Update the control tab, we are disconnected */
             guiControl.setConnected(guiControl.DISCONNECTED, "Problem signing up");
             
             return;
         }
         
         /* Now send the data */
         sendData (so);        
         
         /* Now register this channel with a selector */
         try {
             selector = Selector.open();
             channel.register(selector, SelectionKey.OP_READ);
         } catch (Exception e) {
             guiControl.statusMessage("Problem with registering selector: " + e);
             
             /* Update the control tab, we are disconnected */
             guiControl.setConnected(guiControl.DISCONNECTED, "Selector error");
             
             return;
         }
 
         /* Inform the user we are connected */
         guiControl.setConnected(guiControl.CONNECTED);
             
         /* Todo: Need to have a better means of interrupting, or at least 
          * clean up this one */
         
         /* 
          * This loop is responsible for receiving the server messages
          * and updating the GUI.
          */
         while(getInterrupt() != true) {
             
             int selectResponse = 0;
             
             try {
                 /* Wait until there is something to read */
                 selectResponse = selector.select(1000);
             } catch (Exception e) {
                 guiControl.statusMessage("Problem with select: " + e);
             }
             
             if(selectResponse > 0) {
                 /* Obtain set of actions */
                 Set readyKeys = selector.selectedKeys();
                 Iterator readyItor = readyKeys.iterator();
 
                 /* Deal with each action */
                 while(readyItor.hasNext()) {
 
                     /* Get the action key */
                     SelectionKey key = (SelectionKey)readyItor.next();
 
                     /* Remove this key from the set */
                     readyItor.remove();
                     
                     if (key.isReadable()) {  
                             
                         /* Resets buffers */
                         readBuffer.clear();                        
                         readCharBuffer.clear();
                             
                         /* Get channel */
                         SocketChannel socketchannel = (SocketChannel)key.channel();
                                         
                         if(!socketchannel.isConnected()) {
                             guiControl.statusMessage("Socket not connected");
                         }
                         
                         Object databean[] = null;
                         
                         /* Read any pending data into a buffer */
                         try {
                             databean = mbox.receiveData(socketchannel, splitBuffer);
                         } catch(IOException ex) {
                            Logger.getLogger("sh.bob.gob.client").severe("Exception when receiving data, channel has been closed: " + ex);
                            guiControl.statusMessage("Exception when receiving data, channel has been closed: " + ex);
                            /* Need to shutdown */
                            setInterrupt();
                            break;
                         }
 
                         if(databean == null) {
                             guiControl.statusMessage("Databean is null");
                             continue;
                         }
                         
                         splitBuffer = (SplitBuffer)databean[0];
                         
                         for(int i = 1; i < databean.length; i++) {
                             String objectName = databean[i].getClass().getName();
                             Object dataBean = databean[i];
                             String objectShortName = objectName.replaceAll("sh.bob.gob.shared.communication.","");
                        
                             if(objectShortName.equals("SignOn")) {
                                 /* This should only occur if the signon has failed */
                                 SignOn signOnDB = (SignOn)dataBean;
                                 
                                 if(signOnDB.getError() != null) {
                                     /* There was a signon error ... bail out 
                                      * No need to signoff, we aren't signed in */
                                     guiControl.statusMessage(signOnDB.getError());
                                     
                                     setInterrupt();
                                     break;
                                 } else {
                                     /* Print a status message */
                                     guiControl.statusMessage("New user \"" + ((SignOn)dataBean).getUserName() + "\"");
                                 }
                             } else if(objectShortName.equals("ServerMessage")) {
                                 /* Send any greetings as a status message */
                                 guiControl.statusMessage(((ServerMessage)dataBean).getMessage());
                             } else if(objectShortName.equals("RoomList")) {
                                 /* Load the list control with the list of rooms */
                                   
                                 RoomItem ri[] = ((RoomList)dataBean).getRooms();
                                 String roomlist[] = new String[ri.length];
                                 for(int i2 = 0; i2 < ri.length; i2++) {
                                     roomlist[i2] = ri[i2].getRoomName();
                                 }
 
                                 /* Load the array into the list control in the Room List tab */
                                 guiControl.setRoomList(roomlist);
                             } else if(objectShortName.equals("RoomJoin")) {
                                     
                                 if(((RoomJoin)dataBean).getUserName().equals(connectionInfo.getUsername())) {
                                     /* If the user is us, we have joined a room, create a new group panel */
                                     guiControl.getGroupTabControl().addGroup(((RoomJoin)dataBean).getRoomName());
                                 } else {
                                     /* If the user is someone else, they have joined the room, update
                                      * the rooms user list */
                                     guiControl.getGroupTabControl().addUser(((RoomJoin)dataBean).getRoomName(), 
                                         ((RoomJoin)dataBean).getUserName());
                                     guiControl.getGroupTabControl().writeStatusMessage(((RoomJoin)dataBean).getRoomName(), "User \"" 
                                         + ((RoomJoin)dataBean).getUserName() + "\" has joined the room.");
                                 }
                             } else if(objectShortName.equals("RoomUserList")) {
                                 UserItem ui[] = ((RoomUserList)dataBean).getUsers();

                                 String userlist[] = new String[ui.length];
                                 for(int i2 = 0; i2 < ui.length; i2++) {
                                     userlist[i2] = ui[i2].getUserName();
                                 }
                                     
                                 /* Reset the user list for the group */
                                 guiControl.getGroupTabControl().resetUserList(((RoomUserList)dataBean).getRoomName(), userlist);
                                     
                             } else if(objectShortName.equals("SignOff")) {
                                 /* I want to check what the username is */
                                 if(((SignOff)dataBean).getUserName().equals(connectionInfo.getUsername())) {
                                     /* I am the user, need to return to the main screen log off and all that */
                                     guiControl.getGroupTabControl().removeAllGroups();
                                     guiControl.getPrivTabControl().removeAllUsers();
                                     setInterrupt();
                                 } else {
                                     /* Ge the details */
                                     String roomname = ((SignOff)dataBean).getRoomName();
                                     String username = ((SignOff)dataBean).getUserName();
                                     String reason = ((SignOff)dataBean).getMessage();
                                 
                                     /* Remove the user from the group */
                                     guiControl.getGroupTabControl().deleteUser(roomname, username);
                                     guiControl.getGroupTabControl().writeStatusMessage(roomname, "User \"" + username + "\" has quit because \"" + reason + "\".");
                                 }
                             } else if(objectShortName.equals("RoomSend")) {
                                 /* Split the params room:user:message */
                                 String user = ((RoomSend)dataBean).getUserName();
                                 String room = ((RoomSend)dataBean).getRoomName();
                                 String message = ((RoomSend)dataBean).getMessage();
                                     
                                 guiControl.getGroupTabControl().writeUserMessage(room, user, message);
                             } else if(objectShortName.equals("UserMessage")) {
                                 /* Split the params usersrc:userdst:message */
                                 String usersrc = ((UserMessage)dataBean).getUserNameSrc();
                                 String userdst = ((UserMessage)dataBean).getUserNameDst();
                                 String message = ((UserMessage)dataBean).getMessage();
                                     
                                 /* Send the message */
                                 if(usersrc.equals(connectionInfo.getUsername())) {
                                     if(guiControl.getPrivTabControl().isUser(userdst) != true) {
                                         guiControl.getPrivTabControl().addUser(userdst);
                                     }
                                     guiControl.getPrivTabControl().writeUserMessage(userdst, usersrc, message);
                                 } else if(userdst.equals(connectionInfo.getUsername())) {
                                     if(guiControl.getPrivTabControl().isUser(usersrc) != true) {
                                         guiControl.getPrivTabControl().addUser(usersrc);
                                     }
                                     guiControl.getPrivTabControl().writeUserMessage(usersrc, usersrc, message);
                                 }
                             } else if(objectShortName.equals("RoomPart")) {
                                 /* Split the params room:user */
                                 String room = ((RoomPart)dataBean).getRoomName();
                                 String user = ((RoomPart)dataBean).getUserName();
                                  
                                 /* If the user is me, remove the group */
                                 if(connectionInfo.getUsername().equals(user)) {
                                     guiControl.getGroupTabControl().removeGroup(room);
                                 } else {
                                     /* If the user isn't me, remove the user */
                                     guiControl.getGroupTabControl().deleteUser(room, user);
                                     guiControl.getGroupTabControl().writeStatusMessage(room, "User \"" + user + "\" has left the room.");
                                 }
                             } else if(objectShortName.equals("NameChange")) {
                                 /* Split the params oldName:newName */
                                 String oldname = ((NameChange)dataBean).getUserNameOld();
                                 String newname = ((NameChange)dataBean).getUserNameNew();
                                    
                                 /* Check if the user is me */
                                 if(connectionInfo.getUsername().equals(oldname)) {
                                     guiControl.setUsername(newname);
                                     connectionInfo.setUsername(newname);
                                 }
                                     
                                 /* Now change the username in every room */
                                 guiControl.getGroupTabControl().renameUser(oldname, newname);
                                     
                             } else if(objectShortName.equals("Ping")) {
                                 /* Alright, now respond with a ping */
                                 ((Ping)dataBean).setSuccess(true);
                                 try {
                                     mbox.sendData(socketchannel, dataBean);
                                 } catch (Exception ex) {
                                 }
                                 
                                 /* Log this */
                                 guiControl.statusMessage("Ping!");
                                 
                             } else {
                                 /* Just send any received messages to the textarea for now */
 //                                    guiControl.statusMessage(serverMsg[loop]);
                             }
                         }
                     }
                 }
             }
         }
 
         /* Attempt to close the connection, wait till it is closed */
         try {
             selector.close();
 //            xmlEncoder.close();
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
             guiControl.statusMessage("Problem disconnecting: " + e);
         }
         
         /* Put a message in the text area, stating we have disconnected */
         guiControl.statusMessage("Disconnected\n");
         
         /* Update the control tab, we are disconnected */
         guiControl.setConnected(guiControl.DISCONNECTED, "User disconnected");
         
     }
     
 }
