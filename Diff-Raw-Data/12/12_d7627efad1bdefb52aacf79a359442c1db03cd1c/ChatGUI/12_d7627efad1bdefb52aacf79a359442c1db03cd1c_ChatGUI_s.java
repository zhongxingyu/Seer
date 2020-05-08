 package main;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import javax.swing.*;
 
 /**
  * Testing Strategy:
  *
  * Connect to Chat Server
  * Create a username
  * Create a new room
  * Type message into room
  * Leave room
  * ---
  * This test should show the message displayed next to the specified username, the new chat room should be in the list
  * of available chat rooms. The user should then be able to leave the room.
  *
  * Connect to Chat Server
  * Create a username
  * Create a new room
  * Type message into room
  * Leaves room
  * Other user connects to chat server
  * Other user creates a username
  * Other user creates a new room
  * Other user types message into new room
  * ---
  * This test should show the message displayed next to the first specified username, the new chat room should be in the list
  * of available chat rooms, the other user should be able to create a new username and login to the chat server and not see the 
  * other user's chat room in the list, the new chat room that the second user creates should join the list of available chat
  * rooms, the message the second user types should be only displayed in the second user's chat room next to the second user's
  * name.
  *
  * Connect to Chat Server
  * Create a username
  * Create a new room
  * Type message into room
  * Other user connects to chat server
  * Other user creates a username
  * Other user joins existing room
  * Other user types message into new room
  * First user types message into room
  * First user leaves room
  * Second user leaves room
  * ---
  * This test should show the message displayed next to the first specified username, the new chat room should be in the list
  * of available chat rooms, the other user should be able to create a new username and login to the chat server and see the 
  * other user's chat room in the list. The second user should be able to join the first user's room. The first user should be
  * notified that the second user joined. The second user should not be able to see the first user's previous message. The 
  * second user should be able to type in a message, which will be displayed next to their username. The first user should
  * see this new message. The first user should be able to type in a message, which will be displayed next to their username.
  * The second user should see this new message. The second user should be notified that the first user left. The second user
  * should be able to leave.
  * 
  * Connect to Chat Server
  * Create a username
  * Create a new room
  * Type message into room
  * Other user connects to chat server
  * Other user creates a username
  * Other user joins existing room
  * Other user types delayed message into new room
  * First user types message into room
  * First user leaves room
  * Second user leaves room
  * ---
  * This test should show the message displayed next to the first specified username, the new chat room should be in the list
  * of available chat rooms, the other user should be able to create a new username and login to the chat server and see the 
 * other user's chat room in the list. The second user should be able to join the first user's room. The first user should be
  * notified that the second user joined. The second user should not be able to see the first user's previous message. The 
 * second user should be able to type in a delayed message, which will be displayed next to their username. The first user
 * should be able to type in a message before the second user's message shows up, which will be displayed next to the first 
 * username. The second user's delayed message should show up before the first user's message. Both users should be able to 
 * see both messages.The second user should be notified that the first user left. The second user should be able to leave.
  * 
  * Other tests:
  * One user, multiple chat rooms
  * Multiple users, one chat room
  * Multiple users, multiple chat rooms
  * 
  */
 
 public class ChatGUI extends JFrame {
 
     //GUI variable declarations: DO NOT TOUCH
     private JScrollPane jScrollPane1, jScrollPane2, jScrollPane3;
     private JTextArea inputTextArea, chatTextArea, chatRoomList;
     private JLabel avaiableChatRoomsLabel, messageLabel;
     private JButton sendButton;
     private JMenuBar menuBar;
     private JMenu fileMenu, helpMenu;
     private JMenuItem jMenuItem1, jMenuItem2, jMenuItem3, jMenuItem4, jMenuItem5, jMenuItem6;
     //END OF VARIABLE DECLARATION
     
     /**
      * ChatGUI constructor
      * 
      * calls initComponents() to create gui
      */
     public ChatGUI() {
         initComponents();
     }
 
     private void initComponents() {
         //init items
         jScrollPane1 = new JScrollPane();
         inputTextArea = new JTextArea();
         jScrollPane2 = new JScrollPane();
         chatTextArea = new JTextArea();
         sendButton = new JButton();
         jScrollPane3 = new JScrollPane();
         chatRoomList = new JTextArea();
         avaiableChatRoomsLabel = new JLabel();
         messageLabel = new JLabel();
         menuBar = new JMenuBar();
         fileMenu = new JMenu();
         helpMenu = new JMenu();
         jMenuItem1 = new JMenuItem();
         jMenuItem2 = new JMenuItem();
         jMenuItem3 = new JMenuItem();
         jMenuItem4 = new JMenuItem();
         jMenuItem5 = new JMenuItem();
         jMenuItem6 = new JMenuItem();
 
         //set close and title
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         setTitle("Chat Client");
 
         //box to write message in
         inputTextArea.setColumns(20);
         inputTextArea.setLineWrap(true);
         inputTextArea.setRows(5);
         inputTextArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "submitMessage");
         //NOTE: do the following for textarea events. DON'T USE KEYLISTENERS. bad practice.
         inputTextArea.getActionMap().put("submitMessage", new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 sendButtonHandler(e);
             }
         });
         jScrollPane1.setViewportView(inputTextArea);
 
         //area to display messages
         chatTextArea.setColumns(20);
         chatTextArea.setEditable(false);
         chatTextArea.setFont(new Font("Times New Roman", 0, 16));
         chatTextArea.setLineWrap(true);
         chatTextArea.setRows(5);
         jScrollPane2.setViewportView(chatTextArea);
 
         //display messages label
         messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
         messageLabel.setText("Chat Window"); //dynamically set this
 
         //send button
         sendButton.setText("Send");
         sendButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 sendButtonHandler(e);
             }
         });
 
         //list of open rooms. NOTE: rooms that you are connected
         //to will be starred or put in bold.
         //They will also have a number next to the name if there
         //are unread messages
         chatRoomList.setEditable(false);
         chatRoomList.setColumns(20);
         chatRoomList.setRows(5);
         jScrollPane3.setViewportView(chatRoomList);
 
         //Open chat rooms label
         avaiableChatRoomsLabel.setHorizontalAlignment(SwingConstants.CENTER);
         avaiableChatRoomsLabel.setText("Chat Rooms");
 
         //Menu bar
         fileMenu.setText("File");
         helpMenu.setText("Help");
         jMenuItem1.setText("Settings");
         jMenuItem1.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 //TODO
                 //Open a settings window or something
                 System.out.println("Settings pressed");
             }
         });
         jMenuItem2.setText("Connect to Chat Server");
         jMenuItem2.setMnemonic(KeyEvent.VK_T); //example of mnemonic
         jMenuItem2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK)); //example of accelerator
         jMenuItem2.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 //TODO
                 //Open a dialog prompting user for name or something
                 //idea of preferences file
                 System.out.println("connect to server pressed");
             }
         });
         jMenuItem3.setText("Disconnect from Chat Server");
         jMenuItem3.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 //TODO
                 //kill all chat windows
                 //clear text areas
                 //print disconnected to screen
                 System.out.println("disconnect pressed");
             }
         });
         jMenuItem4.setText("Connect to Room");
         jMenuItem4.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 //TODO
                 //open dialog asking user for the name of the room they would like to join
                 System.out.println("connect to room pressed");
             }
         });
         jMenuItem5.setText("Create Room");
         jMenuItem5.setMnemonic(KeyEvent.VK_N); //example of mnemonic
         jMenuItem5.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())); //example of accelerator
         jMenuItem5.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 //TODO
                 //open dialog asking user for the name of the room they would like to create
                 System.out.println("create room pressed");
             }
         });
         jMenuItem6.setText("Documentation");
         jMenuItem6.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 //open up a documentation in browser
                 // open the default web browser for the HTML page
                 try {
                     Desktop.getDesktop().browse(new URI("https://github.com/jholliman/guichat/blob/master/README.md"));
                 } catch (IOException e1) {
                     e1.printStackTrace();
                 } catch (URISyntaxException e1) {
                     e1.printStackTrace();
                 }
             }
         });
         fileMenu.add(jMenuItem5);
         fileMenu.add(jMenuItem4);
         fileMenu.addSeparator();
         fileMenu.add(jMenuItem2);
         fileMenu.add(jMenuItem3);
         fileMenu.addSeparator();
         fileMenu.add(jMenuItem1);
         helpMenu.add(jMenuItem6);
         menuBar.add(fileMenu);
         menuBar.add(helpMenu);
         setJMenuBar(menuBar);
 
         //set window layout
         GroupLayout layout = new GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                     .addComponent(messageLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                         .addGap(4, 4, 4)
                         .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 336, GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(sendButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addComponent(jScrollPane2, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 419, GroupLayout.PREFERRED_SIZE)
                     .addGroup(layout.createSequentialGroup()
                         .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                         .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                         .addGap(18, 18, 18)))
                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                     .addComponent(avaiableChatRoomsLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jScrollPane3))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                     .addComponent(messageLabel)
                     .addComponent(avaiableChatRoomsLabel))
                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 261, GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                             .addComponent(sendButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)))
                     .addComponent(jScrollPane3))
                 .addContainerGap())
         );
 
         //size window properly
         pack();
     }
 
     private void sendButtonHandler(ActionEvent e) {
         String nothing = "";
         String username = "jholliman"; //temporary var for testing
         String messageText = inputTextArea.getText();
         if ((inputTextArea.getText()).equals(nothing)) { //check for text
             inputTextArea.setText("");
             inputTextArea.requestFocus();
         } else {
             //TODO
             //send message to server here
             chatTextArea.append(username + ": " + messageText + "\n");
             inputTextArea.setText("");
             inputTextArea.requestFocus();
         }
 
         inputTextArea.setText("");
         inputTextArea.requestFocus();
     }
 
     public static void main(final String[] args) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 ChatGUI main = new ChatGUI();
                 main.setVisible(true);
             }
         });
     }
 }
