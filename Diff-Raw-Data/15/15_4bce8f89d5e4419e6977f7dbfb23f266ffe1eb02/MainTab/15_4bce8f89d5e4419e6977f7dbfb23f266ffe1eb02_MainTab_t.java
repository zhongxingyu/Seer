 package client.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.*;
 import java.util.regex.Pattern;
 
 import javax.swing.*;
 
 /**
  * The Main tab of our GuiChat. Contains a list of all chatrooms, a list of all users and a button
  * for creating a new chat. Clicking on the New ChatRoom button will trigger a popup prompting the
  * user for a chatroom name. Submitting an empty string as a name will cause the popup to continue 
  * triggering until the user submits a non-empty chatname or they click on the cancel/exit button
  * of the popup. An invalid/taken chatroom name will cause another popup to trigger with the 
  * appropriate error message. Double clicking on a chatroom in the chatroom list will cause the
  * user to join the chatroom and open up a new tab containing that chat. Trying to join a room that
  * the user has already joined will cause an error popup to occur. The list of users will update to
  * match the server, but cannot be interacted with.
  * 
  * Can only be added to a MainWindow and not any other JFrames. Takes in the MainWindow it is a part 
  * of as an argument.
  *
  * NOTE: Test to perform includes creating a new chat and clicking okay (makes a chat tab), and 
  * creating a new chat and clicking cancel (does not make a new chat tab).
  */
 public class MainTab extends JPanel{
 	
 	private static final long serialVersionUID = 1L;
 	private final JLabel uiChat;
 	private final JLabel welcome;
     private final JButton makeChat;
     private final JLabel roomLabel;
     private final JList chatRoomList;
     private final JLabel userLabel;
     private final JList userList;
     private final MainWindow main;
     
     public MainTab(MainWindow main) {
         Font TitleFont = new Font("SANS_SERIF", Font.BOLD, 24);
         Font LabelFont = new Font("SANS_SERIF", Font.BOLD, 16);
         uiChat = new JLabel("Complete Chat");
         uiChat.setFont(TitleFont);
         makeChat = new JButton("New ChatRoom");
         roomLabel = new JLabel(" Chatrooms");
         roomLabel.setFont(LabelFont);
         chatRoomList = new JList(new DefaultListModel());
         userLabel = new JLabel(" Users");
         userLabel.setFont(LabelFont);
         userList = new JList(new DefaultListModel());
         setName("Main Window");
         this.main = main;
         main.setListModels(userList, chatRoomList);
         welcome = new JLabel();
         
         chatRoomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         JScrollPane chatScroll = new JScrollPane (chatRoomList);
         chatScroll.setPreferredSize(new Dimension(700, 500));
         userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         JScrollPane userScroll = new JScrollPane (userList);
         userScroll.setPreferredSize(new Dimension(250, 500));
         
         makeChat.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 String newChat = JOptionPane.showInputDialog(MainTab.this, "Specify a name for your new chatroom:", 
                         "Create New Chatroom", JOptionPane.PLAIN_MESSAGE);
                 if (newChat.length() > 0) {
                     if (isValidChatname(newChat)) {
                         MainTab.this.main.getClient().send("make " + newChat);
                     }
                     else {
                         JOptionPane.showMessageDialog(MainTab.this, "Error: Chatroom name cannot exceed 40 characters " +
                                 "or contain any whitespace", "Error", JOptionPane.WARNING_MESSAGE);
                     }
                 }
                 else {
                     makeChat.doClick();
                 }
             }
         });
         
         chatRoomList.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 if (e.getClickCount() == 2 && chatRoomList.getSelectedValue() != null) {
                     String chatName = (String)chatRoomList.getSelectedValue();
                     MainTab.this.main.getClient().send("join " + chatName);
                 }
             }
         });
         
         //defining the layout
         GroupLayout layout = new GroupLayout(this);
         this.setLayout(layout);
         
         //setting some margins around our components
         layout.setAutoCreateGaps(true);
         layout.setAutoCreateContainerGaps(true);
         
         //organizing components
         layout.setVerticalGroup(layout.createSequentialGroup()
                 .addComponent(uiChat)
                 .addComponent(welcome)
                 .addComponent(makeChat)
                 .addGroup(layout.createParallelGroup()
                         .addComponent(userLabel)
                         .addComponent(roomLabel))
                 .addGroup(layout.createParallelGroup()
                         .addComponent(userScroll)
                         .addComponent(chatScroll))
                 );
         layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                 .addComponent(uiChat)
                 .addComponent(welcome)
                 .addComponent(makeChat)
                 .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup()
                                 .addComponent(roomLabel)
                                 .addComponent(chatScroll))
                         .addGroup(layout.createParallelGroup()
                                 .addComponent(userLabel)
                                 .addComponent(userScroll)
                                 )
                         )
                 );       
     }
     
     //Checks to make sure a chatname is valid
     private boolean isValidChatname(String Chatname) {
         String regex = "\\p{Graph}+";
         if (Pattern.matches(regex, Chatname) && Chatname.length() < 40) {
             return true;
         }
         return false;
     }
     
     public void setUsername(String username) {
         Font welcomeFont = new Font("MONOSPACED", Font.PLAIN, 13);
         welcome.setText("Have fun chatting " + username);
         welcome.setFont(welcomeFont);
         welcome.setForeground(Color.MAGENTA);
     }
 }
