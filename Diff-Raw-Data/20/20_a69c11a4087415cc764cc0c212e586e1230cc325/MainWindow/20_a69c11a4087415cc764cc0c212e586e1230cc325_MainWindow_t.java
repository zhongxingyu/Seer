 package client.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.*;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.*;
 import javax.swing.text.BadLocationException;
 
 import client.*;
 
 public class MainWindow extends JFrame implements ActionListener{
 
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
     
     public MainWindow() {
         menuBar = new JMenuBar();
         file = new JMenu("File");
         getHistory = new JMenuItem("Chat History");
         logout = new JMenuItem("Logout");
         allUsers = new DefaultListModel();
         allRooms = new DefaultListModel();
         connectedRoomsHistory = new HashMap<String,ChatRoomClient>();
         connectedRoomsCurrent = new HashMap<String, ChatRoomClient>();
         
         menuBar.add(file);
         file.add(getHistory);
         file.add(logout);
         this.setJMenuBar(menuBar);
         
         tabs = new JTabbedPane();
         mainTab = new MainTab(this);
         tabs.addTab("Main Window", mainTab);
         this.add(tabs);
         
         getHistory.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 HistoryTab t = new HistoryTab();
                 addCloseableTab("History", t);
             }    
         });
         
         logout.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 
             }
         });
     }
     
     public void addCloseableTab(String tabName, JPanel tab) {
         tabs.addTab(tabName, tab);
         int i = tabs.indexOfComponent(tab);
         if (i != -1) {
             tabs.setTabComponentAt(i, new ChatTabComponent(tabs));
         }
     }
     
     /**
      * So I want to make a tab that I can close with a button, so I need
      * to make a new Component to represent that tab.
      *
      */
     private class ChatTabComponent extends JPanel {
         private final JTabbedPane pane;
         
         private ChatTabComponent(final JTabbedPane pane) {
             if (pane == null) {
                 throw new NullPointerException("Tabbed Pane is null");   
             }
             this.pane = pane;
             setOpaque(false);
             
             JLabel name = new JLabel() {
                 public String getText() {
                     int i = pane.indexOfTabComponent(ChatTabComponent.this);
                     if (i != -1) {
                         return pane.getTitleAt(i);
                     }
                     return null;
                 }
             };
             name.setPreferredSize(new Dimension(60, 15));
             
             add(name);
             
             JButton exit = new JButton("x");
             exit.setContentAreaFilled(false);
             exit.setPreferredSize(new Dimension(17, 17));
             exit.setFocusable(false);
             exit.setForeground(Color.RED);
             exit.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     int i = pane.indexOfTabComponent(ChatTabComponent.this);
                     if (i != -1) {
                         //TODO: appropriate disconnect room from server stuff
                         pane.remove(i);
                     }
                 }
             });
             add(exit);
         }
     }
     
     public void setClient(Client c) {
         client = c;
         mainTab.setClient(c);
         //mainTab.setListModels(c.getRoomModel(), c.getUsersModel());
     }
     
     public void addRooms(Object[] ChatRooms) {
         mainTab.addRooms(ChatRooms);
     }
     
     public void actionPerformed(ActionEvent e) {
         String input = e.getActionCommand();
         String newLine = "(\\r?\\n)";
         String messageText = "(\\p{Print}+)";
         String name = "(\\p{Graph}+)";
         String nameList = "((" + name + " )*" + name + ")";
         String mess = "(message " + name + " " + name + " " + messageText + ")";
         String xYList = "(((clientRoomList)|(chatUserList)) " + name + " " + nameList + ")";
         String listRX = "(((serverUserList)|(serverRoomList)) " + nameList + ")";
         String roomCD = "(((connectedRoom)|(disconnectedRoom)) " + name + ")";
         String regex = "((disconnectedServerSent)|" + roomCD + "|" + listRX + "|" + 
                         xYList + "|" + mess + ")" + newLine;
         
         Pattern p = Pattern.compile(regex);
         Matcher m = p.matcher(input);
         
         int firstSpaceIndex = input.indexOf(' ');
         String command = input.substring(0, firstSpaceIndex);
         if(command.equals("disconnectedServerSent")) {
             // TODO Make good action of disconnecting server
         } else if(command.equals("message")) {
             int secondSpaceIndex = input.indexOf(' ', firstSpaceIndex+1);
             int thirdSpaceIndex = input.indexOf(' ', secondSpaceIndex+1);
             String chatRoomName = input.substring(firstSpaceIndex + 1, secondSpaceIndex);
             String userName = input.substring(secondSpaceIndex + 1, thirdSpaceIndex);
             String message = input.substring(thirdSpaceIndex + 1);
             if(connectedRoomsCurrent.containsKey(chatRoomName)){
                 ChatRoomClient roomCurrent = connectedRoomsCurrent.get(chatRoomName);
                 try {
                     roomCurrent.addMessage(new Message(userName, message));
                 } catch (BadLocationException e1) {
                     // TODO Auto-generated catch block
                     e1.printStackTrace();
                 }
             }
             
             // TODO Make good based on message and chatroom
         } else {
             String[] list = input.substring(firstSpaceIndex+1).split(" ");
             if(command.equals("serverUserList")) {
                 allUsers.clear();
                 for(int i = 0; i < list.length; i++) {
                     allUsers.add(i, list[i]);
                 }
                 
                 // TODO Make good server user list update action here
             } else if(command.equals("serverRoomList")) {
                 allRooms.clear();
                 for(int i = 0; i < list.length; i++) {
                     allRooms.add(i, list[i]);
                 }
                 
                 // TODO Make good server room list update action here
             } else if(command.equals("chatUserList")) {
                 
                 // TODO Make good update of users in particular chat
             } else if(command.equals("clientRoomList")) {
                 String user = list[0];
                 for(int i = 1; i < list.length; i++) {
                     if(!connectedRoomsCurrent.containsKey(list[i])) {
                         client.send("disconnect " + user);
                     }
                 }
                 for(String s : connectedRoomsCurrent.keySet()) {
                     
                 }
                 
                 // TODO Perform update of rooms client is connected to
             } else if(command.equals("connectedRoom")) {
                 String roomName = list[0];
                 if(connectedRoomsHistory.containsKey(roomName)) {
                     if(connectedRoomsCurrent.containsKey(roomName)) {
                     } else {
                         ChatRoomClient chat = connectedRoomsHistory.get(roomName);
                         connectedRoomsCurrent.put(roomName, chat);
                        addCloseableTab(roomName, new ChatTab(roomName, client, this));
                     }
                 } else {
                     if(connectedRoomsHistory.containsKey(roomName)) {
                     } else {
                         ChatRoomClient chat = new ChatRoomClient(roomName, client.getUsername());
                         connectedRoomsCurrent.put(roomName, chat);
                        addCloseableTab(roomName, new ChatTab(roomName, client, this));
                     }
                 }
                 // TODO Perform connection of room here aka make a new tab
             } else if(command.equals("disconnectedRoom")) {
                 // TODO Perform disconnection of room here aka make sure tab is closed
             } else {
                 // Should not arrive here, dead code
             }
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
     
     public static void main(final String[] args) {
         
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 MainWindow main = new MainWindow();
                 Client client = null;
                 try {
                     client = new Client("user2", "127.0.0.1", 10000);
                 } catch (IOException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 ChatTab test1 = new ChatTab("Test1", client, main);
                 main.addCloseableTab("Test1", test1);
 
                 main.pack();
                 main.setLocationRelativeTo(null);
                 main.setVisible(true);
                 ChatTab test2 = new ChatTab("ReallyLongTestNameBecauseYeah2", client, main);
                 main.addCloseableTab("ReallyLongTestNameBecauseYeah2", test2);
             }
         });
     }
 }
