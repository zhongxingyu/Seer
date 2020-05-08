 package client.gui;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import javax.swing.*;
 import javax.swing.event.*;
 
 import client.ChatRoomClient;
 
 /**
  * Class representing the history of a user's chat. Contains a list with all chatrooms
  * the user has joined prior to the opening of a the history tab. In order to view the 
  * history of a chat joined after the tab has been opened, a new History Tab must be
  * made. Selecting a chatroom on the list causes the text pane to display the history
  * of that chatroom up until the user closed the chatroom. If the user is still in the
  * chatroom, the history will also update itself to remain consistent with the open 
  * chatroom.
  *
  */
 public class HistoryTab extends JPanel{
 	private static final long serialVersionUID = 1L;
 	private final JLabel history;
     private final JTextPane convoHistory;
     private final JList pastChats;
     
     /**
      * Constructor for the History Tab.
      * @param connectedRoomsHistory A hashmap of chatroom names to the matching ChatRoomClient.
      * Should contain all chatrooms connected to ever during this user session.
      */
     public HistoryTab(DefaultListModel pastChatModel) {
         Font TitleFont = new Font("SANS_SERIF", Font.BOLD, 18);
         history = new JLabel("History");
         history.setFont(TitleFont);
         convoHistory = new JTextPane();
         pastChats = new JList(pastChatModel);
         setName("History");
         
         
         convoHistory.setEditable(false);
         JScrollPane convoScroll = new JScrollPane (convoHistory);
         convoScroll.setPreferredSize(new Dimension(700, 550));
         JScrollPane chatScroll = new JScrollPane (pastChats);
         chatScroll.setPreferredSize(new Dimension(250, 550));
         pastChats.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         
         pastChats.addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent e) {
                 if (!e.getValueIsAdjusting() && pastChats.getSelectedValue() != null) {
                     ChatRoomClient chatroom = (ChatRoomClient) pastChats.getSelectedValue();
                     convoHistory.setStyledDocument(chatroom.getDoc());
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
                 .addComponent(history)
                 .addGroup(layout.createParallelGroup()
                         .addComponent(convoScroll)
                         .addComponent(chatScroll)));
         
         layout.setHorizontalGroup(layout.createParallelGroup()
                 .addGroup(layout.createSequentialGroup()
                         .addContainerGap(15, 22)
                         .addComponent(history))
                 .addGroup(layout.createSequentialGroup()
                         .addComponent(convoScroll)
                         .addComponent(chatScroll)));
     }
 }
