 /*
  * GroupTabControl.java
  *
  * Created on 12 October 2003, 18:57
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
 
 package sh.bob.gob.client.components;
 
 import sh.bob.gob.client.panels.GroupChatPanel;
 import sh.bob.gob.client.controllers.GUIControl;
 import sh.bob.gob.client.controllers.ClientConnectionControl;
 
 import java.util.HashMap;
 import java.util.Set;
 import java.util.Iterator;
 import java.awt.Component;
 import javax.swing.JTabbedPane;
 import javax.swing.JPanel;
 
 /**
  * This object is responsible for keeping track of the open group chat tabs
  * in the main TabbedPain object.
  * 
  * This control is responsible for opening and closing these tabs, and querying
  * them.
  *
  * Messages sent to the groups are sent through this interface.
  *
  * @author  Ken Barber
  */
 public class GroupTabControl {
     
     private HashMap grouptabs;
     private GUIControl guicontrol;
     private ClientConnectionControl concontrol;
     private JTabbedPane tbMain;
     
     /** 
      * Creates a new instance of GroupTabControl.
      *
      * @param gui GUIControl
      * @param ccc ClientConnectionControl
      * @param tb The JTabbedPane
      */
     public GroupTabControl(GUIControl gui, ClientConnectionControl ccc, JTabbedPane tb) {
         grouptabs = new HashMap();
         guicontrol = gui;
         concontrol = ccc;
         tbMain = tb;
     }
     
     /**
      * Add the group chat tab to the main tabbed pain.
      *
      * @param name The name of the group to create.
      */
     public void addGroup(String name) {
         if(isGroup(name) == false) {
             /* Create a new groupchatpanel */
             GroupChatPanel gcp = new GroupChatPanel(guicontrol, concontrol, name);
             
             /* Add the group to the hashmap */
             grouptabs.put(name, gcp);
             
             /* Add the group to the JTabbedPane */
             tbMain.addTab(name, gcp);
             
             /* Now focus on the tab */
             tbMain.setSelectedComponent(gcp);
            
            /* Now focus on the TextField */
            gcp.tfSendPrep.requestFocusInWindow();
         }
     }
 
     public void addUser(String room, String name) {
         ((GroupChatPanel)grouptabs.get(room)).addUser(name);
     }
     
     /** 
      * Check to see if the user exists in the group.
      *
      * @param room 
      * @param username
      * @return Returns a boolean, true if the user exists, false if not.
      */
     public boolean checkUser(String room, String username) {
         return ((GroupChatPanel)grouptabs.get(room)).checkUser(username);
     }
     
     public void renameUser(String oldname, String newname) {
         Object[] rooms = getGroupList();
         
         /* Check the user in each room */
         for(int i = 0; i < rooms.length; i++) {
             String room = (String)rooms[i];
             
             if(checkUser(room, oldname)) {
                 /* Rename the user in the room */
                 ((GroupChatPanel)grouptabs.get(room)).deleteUser(oldname);
                 ((GroupChatPanel)grouptabs.get(room)).addUser(newname);
                 
                 /* Write a message to the room */
                 ((GroupChatPanel)grouptabs.get(room)).writeStatusMessage("User \"" 
                     + oldname + "\" is now named \"" + newname + "\"");
             }
         }
     }
     
     /**
      * Remove the group chat tab from the main tabbed pane.
      *
      * @param name The name of the group to remove
      */
     public void removeGroup(String name) {
         if(isGroup(name) == true) {
             /* Now remove the tab from the tabbedpain */
             tbMain.remove((JPanel)grouptabs.get(name));
             
             /* Remove the group from the hashmap */
             grouptabs.remove(name);
         }
     }
     
     /**
      * Remove all groups from the main tabbed pane.
      */
     public void removeAllGroups() {
         Set groupset = grouptabs.keySet();
         Iterator groupsit = groupset.iterator();
         String groupname;
         
         /* Remove all panels from the JTabbedPane */
         while(groupsit.hasNext()) {
             groupname = (String)groupsit.next();
 //        while((groupname = (String)groupsit.next()).equals(null) == false) {
             tbMain.remove((JPanel)grouptabs.get(groupname));
         }
 
         /* Now clear the HashMap */
         grouptabs.clear();
         
     }
     
     /**
      * Check weither the group exists already.
      *
      * @param name The name of the group to check
      * @return A boolean, true if the group exists, false if not
      */
     public boolean isGroup(String name) {
         return grouptabs.containsKey(name);
     }
     
     /**
      * Resets the user list for a group.
      *
      * @param room Name of room
      * @param users Array of users
      */
     public void resetUserList(String room, String users[]) {
         ((GroupChatPanel)grouptabs.get(room)).resetUserList(users);
     }
 
     /**
      * Deletes the user from a room.
      *
      * @param room Name of room
      * @param name Name of user
      */
     public void deleteUser(String room, String name) {
         ((GroupChatPanel)grouptabs.get(room)).deleteUser(name);
         
     }
     
     /**
      * Write a user message to the room panel
      *
      * @param room Name of room
      * @param name Name of the user
      * @param message Message to be sent
      */
     public void writeUserMessage(String room, String name, String message) {
         ((GroupChatPanel)grouptabs.get(room)).writeUserMessage(name,message);
     }
     
     /**
      * Write a status message to the room panel.
      *
      * @param room Name of room
      * @param message Message to be sent
      */
     public void writeStatusMessage(String room, String message) {
         ((GroupChatPanel)grouptabs.get(room)).writeStatusMessage(message);
     }
     
     /** 
      * Return a list of groups.
      *
      * @return An array of objects (strings) representing a list of all groups.
      */
     public Object[] getGroupList() {
         return grouptabs.keySet().toArray();
     }
 }
