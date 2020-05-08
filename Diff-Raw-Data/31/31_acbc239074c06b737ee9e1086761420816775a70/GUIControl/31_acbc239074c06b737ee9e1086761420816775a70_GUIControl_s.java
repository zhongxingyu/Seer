 /*
  * GUIControl.java
  *
  * Created on 11 January 2003, 22:13
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
 
 package sh.bob.gob.client.controllers;
 
 import sh.bob.gob.client.components.*;
 import sh.bob.gob.client.panels.*;
 
 import javax.swing.*;
 import java.util.*;
 
 /**
  * This class builds the GUI and provides an interface for control.
  *
  * @author  Ken Barber
  */
 public class GUIControl {
     
     private String hostname;
     private ListControl roomListControl;
     private MsgAreaControl saControl;
     private JTabbedPane tbMain;
     private ControlPanel pControl;
     private StatusPanel pStatus;
     private GroupChatPanel pLobby;
     private RoomListPanel pRoomList;
     private ClientConnectionControl clientConnectionControl;
     private GroupTabControl groupTabControl;
     private PrivTabControl privTabControl;
     
     private int connectionStatus;
     
     public static int CONNECTING = 1;
     
     public static int CONNECTED = 2;
     
     public static int DISCONNECTING = 3;
     
     public static int DISCONNECTED = 4;
     
     /** 
      * Creates a new instance of GUIControl 
      *
      * @param tbm Main tabbed pane provided in the applet
      * @param hn Hostname to connect to
      */
     public GUIControl(JTabbedPane tbm, String hn) {
         tbMain = tbm;
         hostname = hn;
 //        statusMessage(hn); //DEBUG
         
     }
     
     /**
      * This method will start the display of the GUI
      */
     public void displayGUI() {
         
         /* Add a control and status panel */
         pControl = new ControlPanel(this, clientConnectionControl, hostname);
         pStatus = new StatusPanel();
         pRoomList = new RoomListPanel(this, clientConnectionControl);
         
         tbMain.addTab("Control", null, pControl, "For connecting and changing username");
         tbMain.addTab("Status", null, pStatus, "A log of any server or client error messages");
                 
         /* Setup both controls for the ChatArea and UserList */
         saControl = new MsgAreaControl(pStatus.taErrorOutput);
         roomListControl = new ListControl(pRoomList.lRooms);
         
         /* Create a new GroupTabControl for creation of groups */
         groupTabControl = new GroupTabControl(this,  clientConnectionControl, tbMain);
         
         /* Create a new PrivTabControl for creation of users */
         privTabControl = new PrivTabControl(this, clientConnectionControl, tbMain);
         
     }
     
     /**
      * Set connection status
      *
      * @param status Status (from GUIControl static methods) to change to
      */
     public void setConnected(int status) {
         setConnected(status, "No reason");
     }
     
     /**
      * Set connection status
      *
      * @param status Status (from GUIControl static methods) to change to
      * @param reason Reason for status change
      */
     public void setConnected(int status, String reason) {
         
         connectionStatus = status;
         
         switch(status) {
             case 1: // Connecting
                 pControl.bConnect.setEnabled(false);
                 pControl.bDisconnect.setEnabled(true);                
                 pControl.lConnectionStatus.setText("Connecting ...");
                 
                 /* And disable changing the username field */
                 pControl.tfUserName.setEditable(false);
                 break;
             case 2: // Connected
                 pControl.bConnect.setEnabled(false);
                 pControl.bDisconnect.setEnabled(true);     
                 pControl.lConnectionStatus.setText("Connected");
                 
                 /* Allow renaming of user */
                 pControl.bRename.setEnabled(true);
                 pControl.tfNewUserName.setEditable(true);
                 
                 /* Add the lobby tab to the tabbed pain */
                 tbMain.addTab("Room List", null, pRoomList, "A list of all open rooms");
                 
                 /* Now open the lobby tab */
                 tbMain.setSelectedIndex(2);
                 
                 break;
             case 3: // Disconnecting   
                 pControl.bConnect.setEnabled(true);
                 pControl.bDisconnect.setEnabled(false);
                 pControl.lConnectionStatus.setText("Disconnecting ...");
                 break;
             case 4: // Disconnected
                 pControl.bConnect.setEnabled(true);
                 pControl.bDisconnect.setEnabled(false);
                 
                 /* Disallow renaming of user */
                 pControl.bRename.setEnabled(false);
                 pControl.tfNewUserName.setEditable(false);
                 
                 /* Set a reason for disconnection */
                 pControl.lConnectionStatus.setText("Disconnected: " + reason);
                 
                 /* And enable the username field */
                 pControl.tfUserName.setEditable(true);
                 
                 /* And now request focus if this tab is open */
                 pControl.tfUserName.requestFocusInWindow();
                 
                 /* Now remove the lobby */
                 if (tbMain.getTabCount() > 2) {
                     tbMain.removeTabAt(2);
                 }
                 break;
         }
     }
     
     /**
      * Obtain the connection status
      */
     public int getConnected() {
         return connectionStatus;
     }
     
     /**
      * Send a status message 
      *
      * @param output Message to send
      */
     public void statusMessage(String output) {
         saControl.statusMessage(output);
     }
     
     /**
      * Clear the rooms list in the room list panel.
      */
     public void setRoomList(String[] rooms) {
         //ulControl.clearList();
         /* grab pRoomList */
         roomListControl.replaceList(rooms);
     }
     
     /**
      * Return the GroupTabControl
      */
     public GroupTabControl getGroupTabControl() {
         return groupTabControl;
     }
     
     /**
      * Return the PrivTabControl
      */
     public PrivTabControl getPrivTabControl() {
         return privTabControl;
     }    
     
     /**
      * Set the username of the user in the ControlPanel.
      *
      * @param username The username to change to
      */
     public void setUsername(String username) {
         pControl.tfUserName.setText(username);
     }
     
     /** Getter for property clientConnectionControl.
      * @return Value of property clientConnectionControl.
      *
      */
     public ClientConnectionControl getClientConnectionControl() {
         return this.clientConnectionControl;
     }
     
     /** Setter for property clientConnectionControl.
      * @param clientConnectionControl New value of property clientConnectionControl.
      *
      */
     public void setClientConnectionControl(ClientConnectionControl clientConnectionControl) {
         this.clientConnectionControl = clientConnectionControl;
     }
     
 }
