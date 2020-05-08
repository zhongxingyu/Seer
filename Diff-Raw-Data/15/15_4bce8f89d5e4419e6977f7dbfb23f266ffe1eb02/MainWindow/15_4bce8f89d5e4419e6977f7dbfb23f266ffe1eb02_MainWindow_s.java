 package client.gui;
 
import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.*;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 
 import javax.swing.*;
 import javax.swing.text.BadLocationException;
 
 import client.*;
 
 /**
  * Class representing the JFrame that holds all of the tab components. Has a TabBar that
  * contains a permanent maintab, where the list of chatrooms and usernames is, and any number
  * of chat tabs/history tabs that can be closed. Also contains a menu with two options, Chat 
  * History and Logout. Choosing Chat History causes a history tab to be created, and choosing
  * Logout causes the application to close.
  * 
  * The MainWindow is also the home of all the models and contains appropriate methods to modify
  * those methods on the EDT by using invokeLater.
  *
  */
 public class MainWindow extends JFrame {
 	private static final long serialVersionUID = 1L;
 	private final JTabbedPane tabs;
     private final JMenuBar menuBar;
     private final JMenu file;
     private final JMenuItem getHistory;
     private final JMenuItem logout;
     private final MainTab mainTab;
     private Client client = null;
     
     private final DefaultListModel allUsers;
     private final HashMap<String, ChatRoomClient> connectedRoomsHistory;
     private final HashMap<String, ChatRoomClient> connectedRoomsCurrent;
     private final DefaultListModel allRooms;
     private final DefaultListModel historyRooms;
     
     public MainWindow() {
         menuBar = new JMenuBar();
         file = new JMenu("File");
         getHistory = new JMenuItem("Chat History");
         logout = new JMenuItem("Logout");
         allUsers = new DefaultListModel();
         allRooms = new DefaultListModel();
         connectedRoomsHistory = new HashMap<String,ChatRoomClient>();
         connectedRoomsCurrent = new HashMap<String, ChatRoomClient>();
         historyRooms = new DefaultListModel();
         this.setTitle("Complete Chat");
         
         menuBar.add(file);
         file.add(getHistory);
         file.add(logout);
         setJMenuBar(menuBar);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         
         tabs = new JTabbedPane();
         mainTab = new MainTab(this);
         tabs.addTab("Main Window", mainTab);
         add(tabs);
         
         getHistory.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 HistoryTab t = new HistoryTab(historyRooms);
                 addCloseableTab("History", t);
             }    
         });
         
         logout.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 client.send("disconnect " + client.getUsername());
                 quit();
             }
         });
     }
     /**
      * Adds a tab to the tab bar. Except this is a special tab that can be
      * closed with a button.
      * 
      * @param tabName Name of the tab to be added
      * @param tab JPanel that will be displayed by the tab
      */
     public void addCloseableTab(String tabName, JPanel tab) {
         tabs.addTab(tabName, tab);
         int i = tabs.indexOfComponent(tab);
         if (i != -1) {
             ChatRoomClient chatroom = connectedRoomsCurrent.get(tabName);
             if (chatroom == null) {
                 if (tabName == "History") {
                     tabs.setTabComponentAt(i, new ChatTabComponent(null));
                 }
                 else {
                     System.err.println("Should never arrive here unless we have concurrency problems");
                 }
             }
             tabs.setTabComponentAt(i, new ChatTabComponent(chatroom));
         }
     }
     
     /**
      * Custom component needed to create a custom tab containing a close button.
      */
     private class ChatTabComponent extends JPanel {
 
 		private static final long serialVersionUID = 1L;
 		private final JLabel name;
         private final ChatRoomClient chatroom;
         private final String tabName;
         
         private ChatTabComponent(ChatRoomClient chatroom) {
             this.chatroom = chatroom;
             if (chatroom == null) {
                 tabName = "History";
             }
             else {
                 this.tabName = chatroom.getChatRoomName();
             }
             if (tabs == null) {
                 throw new NullPointerException("Tabbed Pane is null");   
             }
             setOpaque(false);
             
             //Making the label
             name = new JLabel() {
 				private static final long serialVersionUID = 1L;
 
 				public String getText() {
                     return ChatTabComponent.this.tabName;
                 }
             };
             name.setPreferredSize(new Dimension(60, 15));
             
             add(name);
             
             //making the button
             ImageIcon close = makeIcon("crossoff.png");
             ImageIcon rolloverClose = makeIcon("crosson.png");
             
             System.out.println(close);
             JButton exit = new JButton(close);
             exit.setContentAreaFilled(false);
             exit.setPreferredSize(new Dimension(12, 12));
             exit.setFocusable(false);
             exit.setBorder(BorderFactory.createEtchedBorder());
             exit.setBorderPainted(false);
             exit.setRolloverIcon(rolloverClose);
             exit.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     closeTab(ChatTabComponent.this, ChatTabComponent.this.chatroom);
                 }
             });
             add(exit);
         }
     }
     
     //Makes the icon for the button
     private ImageIcon makeIcon(String path) {
         URL imgURL = getClass().getResource(path);
         if (imgURL != null) {
             return new ImageIcon(imgURL);
         } else {
             System.err.println("path couldn't be found: " + path);
             return null;
         }
     }
     
     //Sets the window's client
     //Also gets username from client to set a label in maintab
     public void setClient(Client c) {
         client = c;
         mainTab.setUsername(c.getUsername());
     }
     
     /**
      * Returns the particular tab given the string for the
      * chat name
      * @param chatName
      * @return The JPanel Tab with the given chatroom name
      */
     private JPanel findTab(String chatName) {
         for (int i = 0; i<tabs.getTabCount(); i++) {
             String tabName = tabs.getComponentAt(i).getName();
             if (tabName == chatName) {
                 return (JPanel) tabs.getComponentAt(i);
             }
         }
         return null;
     }
     
 
     /**
      * Adds a Message object to the appropriate ChatRoomClient, which will result in that
      * chatroom object's conversation being updated appropriately.
      * @param chatRoomName name of the chatroom the message is to be sent to
      * @param userName the user sending the message
      * @param message the actual message itself
      */
     public void updateConversation(String chatRoomName, String userName, String message) {
         final String c = chatRoomName;
         final String u = userName;
         final String m = message;
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 if(connectedRoomsCurrent.containsKey(c)){
                     ChatRoomClient roomCurrent = connectedRoomsCurrent.get(c);
                     try {
                         roomCurrent.addMessage(new Message(u, m));
                     } catch (BadLocationException e1) {
                         e1.printStackTrace();
                     }
                 }
             }
         });
     }
     
     /**
      * Causes a JDialog to popup
      * @param errorMessage the error message to be displayed inside of the popup
      */
     public void displayErrorMessage(String errorMessage) {
         JOptionPane.showMessageDialog(this, errorMessage.toString(), "Error", JOptionPane.WARNING_MESSAGE);
     }
     
     /**
      * Updates the DefaultListModel for the list of all users on the server
      * @param users Array of users to update the model with
      */
     public void updateMainUserList(String[] users) {
         final String[] list = users;
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 allUsers.clear();
                 for(int i = 0; i < list.length; i++) {
                     allUsers.addElement(list[i]);
                 }
             }
         });
     }
     
     /**
      * Updates the DefaultListModel for the list of all chatrooms on the server
      * @param chats Array of chats to update the model wiht
      */
     public void updateMainChatList(String[] chats) {
         final String[] list = chats;
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 System.out.println("Updating serverRoomList");
                 allRooms.clear();
                 for(int i = 0; i < list.length; i++) {
                     System.out.println("Adding room: " + list[i]);
                     allRooms.addElement(list[i]);
                 }
             }
         });
     }
     
     /**
      * Updates the list of users inside of a chatroom
      * @param chatname Name of the chatroom to update
      * @param users ArrayList of users to update the chatroom with
      */
     public void updateChatUserList(String chatname, ArrayList<String> users) {
         final String c = chatname;
         final ArrayList<String> list = users;
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 if (connectedRoomsCurrent.containsKey(c)) {
                     ChatRoomClient roomCurrent = connectedRoomsCurrent.get(c);
                     roomCurrent.updateUsers(list);
                 }
             }
         });
     }
     /**
      * Makes sure that the local list of chats the user is a part of is the same as
      * the list of chats on the server. If the server returns a chatroom that the user
      * is not a part of, the client will send a message telling the server to disconnect
      * the user from that chatroom. If the server does not return a chatroom that the user
      * says they are connected to, a message will be sent to the server telling them to 
      * connect the user.
      * 
      * TODO: compare both ways, not just check if a room is in c
      * @param username The user who this list of chats corresponds to
      * @param chats The list of all chats the server thinks the user is connected to
      */
     public void updateUserChatList(String username, String[] chats) {
         final String u = username;
         final String[] list = chats;
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 if (u.equals(client.getUsername())) {
                     System.out.println("Checking that chatlist is up to date");
                     for(int i = 0; i < list.length; i++) {
                         Set<String> localRooms= connectedRoomsCurrent.keySet();
                         localRooms.toArray(new String[0]);
                         if(!connectedRoomsCurrent.containsKey(list[i])) {
                             client.send("disconnect " + u);
                         }
                     }
                 }
             }
         });
     }
     
     /**
      * Joins the appropriate chat by opening up a tab for it and adding a ChatRoomClient
      * object to connectedRoomsCurrent. If the room has been joined before, that previous
      * ChatRoomClient object is added. Otherwise, a new ChatRoomClient is created.
      * @param chatname The name of the chatroom to be joined
      */
     public void joinChat(String chatname) {
         final String c = chatname;
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 if(connectedRoomsHistory.containsKey(c)) {
                     if(connectedRoomsCurrent.containsKey(c)) {
                     } else {
                         ChatRoomClient chat = connectedRoomsHistory.get(c);
                         connectedRoomsCurrent.put(c, chat);
                         addCloseableTab(c, new ChatTab(c, MainWindow.this));
                     }
                 } else {
                         ChatRoomClient chat = new ChatRoomClient(c, client.getUsername());
                         connectedRoomsCurrent.put(c, chat);
                         connectedRoomsHistory.put(c, chat);
                         historyRooms.addElement(chat);
                         addCloseableTab(c, new ChatTab(c, MainWindow.this));
                 }
             }
         });
     }
     
     /**
      * Kicks the user out of a chatroom. If the user isn't in the room, nothing
      * happens.
      * @param chatname The name of the chatroom
      */
     public void leaveChat(String chatname) {
         final String c = chatname;
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 if (connectedRoomsCurrent.containsKey(c)) {
                     connectedRoomsCurrent.remove(c);
                     JPanel removedRoom = findTab(c);
                     if (removedRoom != null) {
                         int tabIndex = tabs.indexOfComponent(removedRoom);
                         tabs.remove(tabIndex);
                     }
                 } 
             }
         });
     }
     
     /**
      * Closes the application.
      */
     public void quit() {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 dispose();
             }
         });
     }
     
     /**
      * Sets the list models to the models inside of MainWindow.
      * @param userList The list that will get allUsers as a model
      * @param chatList The list that will get allRooms as a model
      */
     public void setListModels (JList userList, JList chatList) {
         userList.setModel(allUsers);
         chatList.setModel(allRooms);
     }
     
     /**
      * This method closes a particular tab in the GUI.  It takes
      * in the tab component to close and the ChatRoomClient object
      * that contains the information of the messages of the chatroom.
      * 
      * @param t The component associated with that tab
      * @param chatroom The ChatRoomClient object associated with that tab.
      * If the tab is a history tab, chatroom is null
      */
     private void closeTab(Component t, ChatRoomClient chatroom) {
         int i = tabs.indexOfTabComponent(t);
         if (i != -1) {
             if (chatroom != null) {
                 client.send("exit " + chatroom.getChatRoomName());
                 connectedRoomsCurrent.remove(chatroom.getChatRoomName());
                 chatroom.getUserListModel().clear();
             }
             tabs.remove(i);
         }
     }
 
     public DefaultListModel getRoomModel() {
         return allRooms;
     }
 
     public DefaultListModel getUsersModel() {
         return allUsers;
     }
     
     public ChatRoomClient getCurrentRoom(String name) {
         return connectedRoomsCurrent.get(name);
     }
     
     public ChatRoomClient getHistoryRoom(String name) {
         return connectedRoomsHistory.get(name);
     }
     
     public Client getClient() {
         return client;
     }
 }
