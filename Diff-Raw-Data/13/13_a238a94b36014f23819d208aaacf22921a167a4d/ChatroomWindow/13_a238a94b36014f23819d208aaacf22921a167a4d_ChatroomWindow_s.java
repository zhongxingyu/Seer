 package chatroom.client;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JFrame;
 import javax.swing.GroupLayout;
 import javax.swing.JOptionPane;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JButton;
 import javax.swing.JTextArea;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.JList;
 import javax.swing.JLabel;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.net.SocketException;
 import java.util.List;
 
 import javax.swing.JScrollPane;
 
 import chatroom.server.MsgObj;
 
 public class ChatroomWindow {
 
 	private JFrame frmChattingIn;
 	private ClientManager client;
 	private JTextArea chatHistory;
 	private JTextArea chatBox;
 	private JList userList;
 	private DefaultListModel userListModel;
 
 	/**
 	 * Create the application.
 	 */
 	public ChatroomWindow(ClientManager client) {
 		this.client = client;
 		initialize();
 	}
 
 	public JFrame getFrame(){
 		return frmChattingIn;
 	}
 
 	public JTextArea getChatHistory(){
 		return this.chatHistory;
 	}
 
 	public DefaultListModel getUserListModel(){
 		return this.userListModel;				
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frmChattingIn = new JFrame();
 		//		frmChattingIn.addWindowListener(new DisconnectListener(this.client.getOos()));
 		frmChattingIn.setResizable(false);
 		frmChattingIn.setBounds(100, 100, 551, 395);
 		frmChattingIn.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		JScrollPane messageScrollPane = new JScrollPane();
 
 		JScrollPane userListScroll = new JScrollPane();
 
 		JScrollPane chatHistoryScroll = new JScrollPane();
 
 		JButton btnSend = new JButton("Send");
 		btnSend.setToolTipText("Send your message");
 		btnSend.addActionListener(new SendListener());
 
 		JLabel lblUsers = new JLabel("Users");
 		lblUsers.setFont(new Font("Tahoma", Font.BOLD, 13));
 
 		JLabel lblMessages = new JLabel("Messages");
 		lblMessages.setFont(new Font("Tahoma", Font.BOLD, 13));
 
 		JButton btnLeaveRoom = new JButton("Leave Room");
 		btnLeaveRoom.setToolTipText("Leave this room");
 		btnLeaveRoom.addActionListener(new LeaveRoomListener());
 
 		GroupLayout groupLayout = new GroupLayout(frmChattingIn.getContentPane());
 		groupLayout.setHorizontalGroup(
 				groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addGroup(groupLayout.createSequentialGroup()
 										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 												.addComponent(lblUsers, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
 												.addComponent(userListScroll, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE))
 												.addPreferredGap(ComponentPlacement.RELATED)
 												.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 														.addComponent(lblMessages)
 														.addComponent(chatHistoryScroll, GroupLayout.PREFERRED_SIZE, 410, GroupLayout.PREFERRED_SIZE)))
 														.addGroup(groupLayout.createSequentialGroup()
 																.addComponent(messageScrollPane, GroupLayout.PREFERRED_SIZE, 323, GroupLayout.PREFERRED_SIZE)
 																.addPreferredGap(ComponentPlacement.RELATED)
 																.addComponent(btnSend, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE)
 																.addGap(10)
 																.addComponent(btnLeaveRoom)))
 																.addContainerGap())
 				);
 		groupLayout.setVerticalGroup(
 				groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblUsers, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblMessages, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 								.addPreferredGap(ComponentPlacement.RELATED)
 								.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 										.addComponent(userListScroll, GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
 										.addComponent(chatHistoryScroll, GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE))
 										.addPreferredGap(ComponentPlacement.UNRELATED)
 										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
 												.addComponent(btnLeaveRoom, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 												.addComponent(btnSend, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 												.addComponent(messageScrollPane, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE))
 												.addGap(18))
 				);
 
 		this.chatBox = new JTextArea();
 		chatBox.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		messageScrollPane.setViewportView(chatBox);
 
 		this.chatHistory = new JTextArea();
 		chatHistory.setEditable(false);
 		chatHistory.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		chatHistoryScroll.setViewportView(chatHistory);
 
 		this.userListModel = new DefaultListModel();		
 		this.userList = new JList(userListModel);
 		userListScroll.setViewportView(userList);
 		frmChattingIn.getContentPane().setLayout(groupLayout);
 	}
 
 	/**
 	 * Listener class for sending messages to the server. Creates a thread to handle
 	 * the actual sending
 	 */
 	class SendListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 
 			MsgObj message = new MsgObj();	
 			if (!chatBox.getText().equals("")){ //do nothing with an empty string
 				message.addToPayload(chatBox.getText());
 				message.addToPayload(client.getUser());
 
 				if (userList.getSelectedIndex() != -1) { // this is a whisper
 					// add list of targets. 1-inf selected
 					List<String> whisperList = client.makeList(userList.getSelectedValues());
 					for (String whisper : whisperList){
 						String [] extract = whisper.split(" ");
						whisperList.remove(whisper);
						whisperList.add(extract[0]);
 					}
					if (whisperList.contains(client.getUser())){
						//remove ourselves
						whisperList.remove(client.getUser());
						if (whisperList.isEmpty()){
 							//removing ourselves makes the list empty
 							whisperList = null; // null it
						}
 					}
 					message.addToPayload(whisperList);
 					byte type = 4;
 					message.setType(type);
 				} else {
 					// Post to the room
 					byte type = 3;
 					message.setType(type);
 				}
 
 				// Send the message
 				try {
 					client.getOos().writeObject(message);
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 				// disable the button for sending if the text is empty..
 				chatBox.setText("");
 			}
 		}	
 	}
 
 	/**
 	 * Listener for the **Leave Room** button on the
 	 * Chat Room window
 	 */
 	class LeaveRoomListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 
 			MsgObj message = new MsgObj();
 			byte type = 1;
 			message.setType(type);
 
 			// Send message
 			try {
 				// this is all we need to do
 				client.getOos().writeObject(message);
 			} catch (SocketException ex){
 				JOptionPane.showMessageDialog(frmChattingIn, "The server has crashed / is not responsive.");
 				ex.printStackTrace();
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 			frmChattingIn.setVisible(false);
 			chatHistory.setText(""); // clear the chat history
 			client.roomSelectionWindow();
 		}
 	}
 }
