 //Patrick Dickey
 //Sean Jergensen
 //P3 CS 345
 
 package client;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.IOException;
 import java.util.Observable;
 import java.util.Observer;
 import javax.swing.*;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
 
 import drawpad.DrawPad;
 import drawpad.OpenDrawPad;
 
 public class ClientGUI implements Observer{
 
 	//Class variables *************************************************
 
 	/**
 	 * The default port to connect on.
 	 */
 	final public static int DEFAULT_PORT = 5555;
 
 	//Instance Variables***********************************************
 
 	ClientGUI self;
 	ChatClient client;
 
 	JFrame chatFrame;
 	private javax.swing.JToggleButton Availability;
 	private javax.swing.JButton BlockButton;
 	private javax.swing.JTextField ChannelToSetTo;
 	private javax.swing.JScrollPane ChatDisplayScroll;
 	private javax.swing.JPanel ChatWindow;
 	private javax.swing.JButton DrawPadButton;
 	private javax.swing.JButton ForwardButton;
 	private javax.swing.JTextField ForwardTo;
 	private javax.swing.JButton GetHost;
 	private javax.swing.JButton GetPort;
 	private javax.swing.JTextField HostInput;
 	private javax.swing.JTabbedPane MainWindowTabs;
 	private javax.swing.JTextArea MessageDisplay;
 	private javax.swing.JTextArea MessageInput;
 	private javax.swing.JLabel MessageLabel;
 	private javax.swing.JScrollPane MessageScroll;
 	private javax.swing.JTextField PortInput;
 	private javax.swing.JTextArea PrivateInput;
 	private javax.swing.JDialog PrivateMessageDialog;
 	private javax.swing.JButton PrivateMsg;
 	private javax.swing.JPanel PvtMsgPanel;
 	private javax.swing.JTextField PvtUserId;
 	private javax.swing.JLabel RecipientLabel;
 	private javax.swing.JButton Send;
 	private javax.swing.JButton SendPrivate;
 	private javax.swing.JButton SetChannelButton;
 	private javax.swing.JButton SetHostButton;
 	private javax.swing.JButton SetPortButton;
 	private javax.swing.JPanel SettingsWindow;
 	private javax.swing.JButton UnblockButton;
 	private javax.swing.JTextField UserToChangeBlock;
 	private javax.swing.JButton WhoBlocksMeButton;
 	private javax.swing.JButton WhoIBlockButton;
 	private javax.swing.JPanel blockPanel;
 	private javax.swing.JPanel networkPanel;
 	private javax.swing.JPanel settingsChatPanel;	
 	private ChatDrawPad drawPad;
 
 	//Constructors ****************************************************
 
 	/**
 	 * Creates new form GUI
 	 */
 	public ClientGUI(String loginID, String password, String host, int port) {
 		try 
 		{
 			client= new ChatClient(loginID, password, host, port, this);
 		} catch(IOException exception) 
 		{
 			System.out.println("Cannot open connection. Awaiting command.");
 			//System.exit(1);
 		}
 
		try {
			client.addObserver(this);
		}catch (Exception e) {
			//server not started
			JOptionPane.showMessageDialog(null, "Error: Could not connect to server.  Make sure server is started and try again");
			System.exit(0);
		}
 		SetupUI(loginID);
 		self = this;
 	}
 
 	private void SetupUI(String loginID) {
 		chatFrame = new JFrame("Chat " + loginID);
 		PrivateMessageDialog = new JDialog(chatFrame,true);
 		PvtMsgPanel = new javax.swing.JPanel();
 		SendPrivate = new javax.swing.JButton();
 		PrivateInput = new javax.swing.JTextArea();
 		PvtUserId = new javax.swing.JTextField();
 		RecipientLabel = new javax.swing.JLabel();
 		MessageLabel = new javax.swing.JLabel();
 		MainWindowTabs = new javax.swing.JTabbedPane();
 		ChatWindow = new javax.swing.JPanel();
 		ChatDisplayScroll = new javax.swing.JScrollPane();
 		MessageDisplay = new javax.swing.JTextArea();
 		MessageScroll = new javax.swing.JScrollPane();
 		MessageInput = new javax.swing.JTextArea();
 		Send = new javax.swing.JButton();
 		PrivateMsg = new javax.swing.JButton();
 		DrawPadButton = new javax.swing.JButton();
 		Availability = new javax.swing.JToggleButton();
 		SettingsWindow = new javax.swing.JPanel();
 		settingsChatPanel = new javax.swing.JPanel();
 		SetChannelButton = new javax.swing.JButton();
 		ChannelToSetTo = new javax.swing.JTextField();
 		ForwardTo = new javax.swing.JTextField();
 		ForwardButton = new javax.swing.JButton();
 		blockPanel = new javax.swing.JPanel();
 		WhoIBlockButton = new javax.swing.JButton();
 		BlockButton = new javax.swing.JButton();
 		WhoBlocksMeButton = new javax.swing.JButton();
 		UnblockButton = new javax.swing.JButton();
 		UserToChangeBlock = new javax.swing.JTextField();
 		networkPanel = new javax.swing.JPanel();
 		PortInput = new javax.swing.JTextField();
 		SetPortButton = new javax.swing.JButton();
 		HostInput = new javax.swing.JTextField();
 		SetHostButton = new javax.swing.JButton();
 		GetHost = new javax.swing.JButton();
 		GetPort = new javax.swing.JButton();
 
 		//Customize Form
 		Send.setBackground(Color.decode("#16A085"));
 		SendPrivate.setBackground(Color.decode("#16A085"));
 		Availability.setBackground(Color.decode("#2ECC71"));
 		MessageDisplay.setEditable(false);
 		PrivateMessageDialog.setSize(405, 335);
 
 
 		chatFrame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 		chatFrame.setBackground(new java.awt.Color(52, 73, 94));
 
 		MainWindowTabs.setBackground(new java.awt.Color(44, 62, 80));
 		MainWindowTabs.setOpaque(true);
 
 		//Netbeans
 		PvtMsgPanel.setBackground(new java.awt.Color(52, 73, 94));
 		SendPrivate.setText("Send");
 		PrivateInput.setBackground(new java.awt.Color(127, 140, 141));
 		PrivateInput.setColumns(20);
 		PrivateInput.setFont(new java.awt.Font("Arial", 0, 13)); 
 		PrivateInput.setLineWrap(true);
 		PrivateInput.setRows(5);
 		PvtUserId.setBackground(new java.awt.Color(127, 140, 141));
 		RecipientLabel.setForeground(new java.awt.Color(240, 240, 240));
 		RecipientLabel.setText("Recipient Id:");
 		MessageLabel.setForeground(new java.awt.Color(240, 240, 240));
 		MessageLabel.setText("Message:");
 
 		javax.swing.GroupLayout PvtMsgPanelLayout = new javax.swing.GroupLayout(PvtMsgPanel);
 		PvtMsgPanel.setLayout(PvtMsgPanelLayout);
 		PvtMsgPanelLayout.setHorizontalGroup(
 				PvtMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(PvtMsgPanelLayout.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(PvtMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
 								.addComponent(PvtUserId)
 								.addGroup(PvtMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
 										.addComponent(SendPrivate, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
 										.addGroup(PvtMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
 												.addComponent(PrivateInput, javax.swing.GroupLayout.Alignment.LEADING)
 												.addGroup(PvtMsgPanelLayout.createSequentialGroup()
 														.addComponent(RecipientLabel)
 														.addGap(310, 310, 310))))
 														.addComponent(MessageLabel))
 														.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 				);
 		PvtMsgPanelLayout.setVerticalGroup(
 				PvtMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(PvtMsgPanelLayout.createSequentialGroup()
 						.addContainerGap()
 						.addComponent(RecipientLabel)
 						.addGap(1, 1, 1)
 						.addComponent(PvtUserId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 						.addComponent(MessageLabel)
 						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						.addComponent(PrivateInput, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
 						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						.addComponent(SendPrivate, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
 						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 				);
 
 		javax.swing.GroupLayout PrivateMessageDialogLayout = new javax.swing.GroupLayout(PrivateMessageDialog.getContentPane());
 		PrivateMessageDialog.getContentPane().setLayout(PrivateMessageDialogLayout);
 		PrivateMessageDialogLayout.setHorizontalGroup(
 				PrivateMessageDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addComponent(PvtMsgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				);
 		PrivateMessageDialogLayout.setVerticalGroup(
 				PrivateMessageDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(PrivateMessageDialogLayout.createSequentialGroup()
 						.addComponent(PvtMsgPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 						.addGap(0, 3, Short.MAX_VALUE))
 				);
 
 		MainWindowTabs.setBackground(new java.awt.Color(52, 73, 94));
 		ChatWindow.setBackground(new java.awt.Color(52, 73, 94));
 		ChatWindow.setPreferredSize(new java.awt.Dimension(430, 600));
 		ChatWindow.setVerifyInputWhenFocusTarget(false);
 
 		ChatDisplayScroll.setAutoscrolls(true);
 		MessageDisplay.setEditable(false);
 		MessageDisplay.setBackground(new java.awt.Color(127, 140, 141));
 		MessageDisplay.setColumns(20);
 		MessageDisplay.setFont(new java.awt.Font("Arial", 0, 13));
 		MessageDisplay.setLineWrap(true);
 		MessageDisplay.setRows(5);
 		ChatDisplayScroll.setViewportView(MessageDisplay);
 
 		MessageInput.setBackground(new java.awt.Color(127, 140, 141));
 		MessageInput.setColumns(2);
 		MessageInput.setLineWrap(true);
 		MessageInput.setRows(3);
 		MessageScroll.setViewportView(MessageInput);
 
 		Send.setText("Send");
 		PrivateMsg.setText("Private Message");
 		DrawPadButton.setText("Draw Pad");
 		Availability.setText("Available");
 
 		javax.swing.GroupLayout ChatWindowLayout = new javax.swing.GroupLayout(ChatWindow);
 		ChatWindow.setLayout(ChatWindowLayout);
 		ChatWindowLayout.setHorizontalGroup(
 				ChatWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(ChatWindowLayout.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(ChatWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 								.addComponent(ChatDisplayScroll, javax.swing.GroupLayout.Alignment.TRAILING)
 								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ChatWindowLayout.createSequentialGroup()
 										.addComponent(MessageScroll)
 										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 										.addComponent(Send, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
 										.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ChatWindowLayout.createSequentialGroup()
 												.addComponent(DrawPadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
 												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 												.addComponent(PrivateMsg, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
 												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 												.addComponent(Availability, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)))
 												.addContainerGap())
 				);
 		ChatWindowLayout.setVerticalGroup(
 				ChatWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(ChatWindowLayout.createSequentialGroup()
 						.addGap(7, 7, 7)
 						.addComponent(ChatDisplayScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
 						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						.addGroup(ChatWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 								.addComponent(MessageScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
 								.addComponent(Send, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 								.addGroup(ChatWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 										.addComponent(DrawPadButton)
 										.addComponent(PrivateMsg)
 										.addComponent(Availability))
 										.addContainerGap())
 				);
 
 		MainWindowTabs.addTab("Chat", ChatWindow);
 		SettingsWindow.setBackground(new java.awt.Color(52, 73, 94));
 		settingsChatPanel.setBackground(new java.awt.Color(52, 73, 94));
 		settingsChatPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED), "Chat", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(240, 240, 240)));
 
 		SetChannelButton.setText("Set Channel");
 		SetChannelButton.setBorderPainted(false);
 		ChannelToSetTo.setBackground(new java.awt.Color(127, 140, 141));
 		ForwardTo.setBackground(new java.awt.Color(127, 140, 141));
 
 		ForwardButton.setText("Forward");
 		ForwardButton.setBorderPainted(false);
 
 		javax.swing.GroupLayout settingsChatPanelLayout = new javax.swing.GroupLayout(settingsChatPanel);
 		settingsChatPanel.setLayout(settingsChatPanelLayout);
 		settingsChatPanelLayout.setHorizontalGroup(
 				settingsChatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(settingsChatPanelLayout.createSequentialGroup()
 						.addContainerGap(80, Short.MAX_VALUE)
 						.addGroup(settingsChatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
 								.addGroup(javax.swing.GroupLayout.Alignment.LEADING, settingsChatPanelLayout.createSequentialGroup()
 										.addComponent(ChannelToSetTo, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
 										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 										.addComponent(SetChannelButton))
 										.addGroup(javax.swing.GroupLayout.Alignment.LEADING, settingsChatPanelLayout.createSequentialGroup()
 												.addComponent(ForwardTo, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
 												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 												.addComponent(ForwardButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
 												.addContainerGap(68, Short.MAX_VALUE))
 				);
 		settingsChatPanelLayout.setVerticalGroup(
 				settingsChatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(settingsChatPanelLayout.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(settingsChatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 								.addComponent(ChannelToSetTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 								.addComponent(SetChannelButton))
 								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 								.addGroup(settingsChatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 										.addComponent(ForwardButton)
 										.addComponent(ForwardTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
 										.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 				);
 
 		blockPanel.setBackground(new java.awt.Color(52, 73, 94));
 		blockPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED), "Block", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(240, 240, 240)));
 		blockPanel.setForeground(new java.awt.Color(255, 255, 255));
 
 		WhoIBlockButton.setText("My Blocks");
 		BlockButton.setBorderPainted(false);
 		BlockButton.setText("Block");
 		WhoBlocksMeButton.setText("Blocks Me");
 		UnblockButton.setText("Unblock");
 		UnblockButton.setActionCommand("Unblock");
 		UnblockButton.setBorderPainted(false);
 
 		UserToChangeBlock.setBackground(new java.awt.Color(127, 140, 141));	
 
 		javax.swing.GroupLayout blockPanelLayout = new javax.swing.GroupLayout(blockPanel);
 		blockPanel.setLayout(blockPanelLayout);
 		blockPanelLayout.setHorizontalGroup(
 				blockPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, blockPanelLayout.createSequentialGroup()
 						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addComponent(UserToChangeBlock, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
 						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						.addGroup(blockPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 								.addGroup(blockPanelLayout.createSequentialGroup()
 										.addComponent(BlockButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
 										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 										.addComponent(WhoIBlockButton))
 										.addGroup(blockPanelLayout.createSequentialGroup()
 												.addComponent(UnblockButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
 												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 												.addComponent(WhoBlocksMeButton)))
 												.addGap(23, 23, 23))
 				);
 		blockPanelLayout.setVerticalGroup(
 				blockPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(blockPanelLayout.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(blockPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 								.addGroup(blockPanelLayout.createSequentialGroup()
 										.addGroup(blockPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 												.addComponent(BlockButton)
 												.addComponent(WhoIBlockButton))
 												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 												.addGroup(blockPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 														.addComponent(UnblockButton)
 														.addComponent(WhoBlocksMeButton)))
 														.addGroup(blockPanelLayout.createSequentialGroup()
 																.addGap(13, 13, 13)
 																.addComponent(UserToChangeBlock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
 																.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 				);
 
 		networkPanel.setBackground(new java.awt.Color(52, 73, 94));
 		networkPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED), "Network", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(240, 240, 240)));
 
 		PortInput.setBackground(new java.awt.Color(127, 140, 141));
 		SetPortButton.setText("Set Port");
 		HostInput.setBackground(new java.awt.Color(127, 140, 141));
 		SetHostButton.setText("Set Host");
 		GetHost.setText("Get Host");
 		GetPort.setText("Get Port");
 
 		javax.swing.GroupLayout networkPanelLayout = new javax.swing.GroupLayout(networkPanel);
 		networkPanel.setLayout(networkPanelLayout);
 		networkPanelLayout.setHorizontalGroup(
 				networkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, networkPanelLayout.createSequentialGroup()
 						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addGroup(networkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
 								.addComponent(PortInput)
 								.addComponent(HostInput, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
 								.addGroup(networkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
 										.addGroup(networkPanelLayout.createSequentialGroup()
 												.addGap(6, 6, 6)
 												.addComponent(SetHostButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 												.addGroup(networkPanelLayout.createSequentialGroup()
 														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 														.addComponent(SetPortButton, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)))
 														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 														.addGroup(networkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
 																.addComponent(GetPort, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 																.addComponent(GetHost))
 																.addGap(25, 25, 25))
 				);
 		networkPanelLayout.setVerticalGroup(
 				networkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(networkPanelLayout.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(networkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 								.addComponent(PortInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 								.addComponent(SetPortButton)
 								.addComponent(GetPort))
 								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 								.addGroup(networkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 										.addComponent(HostInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 										.addComponent(SetHostButton)
 										.addComponent(GetHost))
 										.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 				);
 
 		javax.swing.GroupLayout SettingsWindowLayout = new javax.swing.GroupLayout(SettingsWindow);
 		SettingsWindow.setLayout(SettingsWindowLayout);
 		SettingsWindowLayout.setHorizontalGroup(
 				SettingsWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(SettingsWindowLayout.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(SettingsWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 								.addComponent(networkPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 								.addComponent(settingsChatPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 								.addComponent(blockPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 								.addContainerGap())
 				);
 		SettingsWindowLayout.setVerticalGroup(
 				SettingsWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(SettingsWindowLayout.createSequentialGroup()
 						.addGap(20, 20, 20)
 						.addComponent(networkPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 						.addGap(33, 33, 33)
 						.addComponent(blockPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 						.addGap(31, 31, 31)
 						.addComponent(settingsChatPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 						.addContainerGap(34, Short.MAX_VALUE))
 				);
 
 		MainWindowTabs.addTab("Settings", SettingsWindow);
 
 		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(chatFrame.getContentPane());
 		chatFrame.getContentPane().setLayout(layout);
 		layout.setHorizontalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 						.addComponent(MainWindowTabs, javax.swing.GroupLayout.PREFERRED_SIZE, 413, javax.swing.GroupLayout.PREFERRED_SIZE)
 						.addGap(0, 0, Short.MAX_VALUE))
 				);
 		layout.setVerticalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addComponent(MainWindowTabs, javax.swing.GroupLayout.PREFERRED_SIZE, 439, javax.swing.GroupLayout.PREFERRED_SIZE)
 				);
 
 		//##################### Listeners #####################
 
 		//display scrollbar vertical update
 		ChatDisplayScroll.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {  
 			public void adjustmentValueChanged(AdjustmentEvent event) {  
 				event.getAdjustable().setValue(event.getAdjustable().getMaximum());  
 			}
 		});
 
 		MessageInput.addKeyListener( new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				int keyCode = arg0.getKeyCode();
 				if(keyCode == 10){ //enter key
 					String message = MessageInput.getText();
 					if(message != null && message.length() > 0){
 						client.handleMessageFromClientUI(message);
 						MessageInput.setText("");
 						arg0.consume();
 					}
 				}
 			}
 			@Override
 			public void keyTyped(KeyEvent arg0) {}
 			@Override
 			public void keyReleased(KeyEvent arg0) {}
 		});
 
 		SetChannelButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String command = SetChannelButton.getActionCommand();
 				if(command == "Set Channel"){
 					//get new channel
 					String channel = ChannelToSetTo.getText();
 					if(channel.length() > 0 ){
 						client.handleMessageFromClientUI("#setchannel "+ channel);
 						ChannelToSetTo.setText("");
 						MainWindowTabs.setSelectedIndex(0);
 					}	
 				}
 			}
 		});
 
 		SetPortButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String command = SetPortButton.getActionCommand();
 				if(command == "Set Port"){
 					//get new port
 					String port = PortInput.getText();
 					if(port.length() > 0 ){
 						client.connectionClosed();
 						client.handleMessageFromClientUI("#setport " + port);
 						MainWindowTabs.setSelectedIndex(0);
 						PortInput.setText("");
 					}
 				}
 			}
 		});
 
 		GetPort.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String command = GetPort.getActionCommand();
 				if(command == "Get Port"){
 					client.handleMessageFromClientUI("#getport");
 					MainWindowTabs.setSelectedIndex(0);
 				}
 			}
 		});
 
 		SetHostButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String command = SetHostButton.getActionCommand();
 				if(command == "Set Host"){
 					//get new host
 					String host = HostInput.getText();
 					if(host.length() > 0 ){
 						client.connectionClosed();
 						client.handleMessageFromClientUI("#sethost " + host);
 						MainWindowTabs.setSelectedIndex(0);
 						HostInput.setText("");
 					}
 				}
 			}
 		});
 
 		GetHost.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String command = GetHost.getActionCommand();
 				if(command == "Get Host"){
 					client.handleMessageFromClientUI("#gethost");
 					MainWindowTabs.setSelectedIndex(0);
 				}
 			}
 		});
 
 
 		WhoIBlockButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String command = WhoIBlockButton.getActionCommand();
 				if(command == "My Blocks"){
 					client.handleMessageFromClientUI("#whoiblock");
 					MainWindowTabs.setSelectedIndex(0);
 				}
 			}
 		});
 
 		WhoBlocksMeButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String command = WhoBlocksMeButton.getActionCommand();
 				if(command == "Blocks Me"){
 					client.handleMessageFromClientUI("#whoblocksme");
 					MainWindowTabs.setSelectedIndex(0);
 				}
 			}
 		});
 
 		BlockButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String command = BlockButton.getActionCommand();
 				if(command == "Block"){
 					//get blockee
 					String blockee = UserToChangeBlock.getText();
 					if(blockee.length() > 0 ){
 						client.handleMessageFromClientUI("#block "+ blockee);
 						UserToChangeBlock.setText("");
 						MainWindowTabs.setSelectedIndex(0);
 					}
 				}
 			}
 		});
 
 		UnblockButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String command = UnblockButton.getActionCommand();
 				if(command == "Unblock"){
 					//get unblockee
 					String unblockee = UserToChangeBlock.getText();
 					client.handleMessageFromClientUI("#unblock "+ unblockee);
 					UserToChangeBlock.setText("");
 					MainWindowTabs.setSelectedIndex(0);
 				}
 			}
 		});
 
 		ForwardButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String command = ForwardButton.getActionCommand();
 				if(command == "Forward"){
 					//get new monitor
 					String monitor = ForwardTo.getText();
 					if(monitor.length() > 0 ){				
 						client.handleMessageFromClientUI("#forward "+ monitor);
 						ForwardTo.setText("");
 						MainWindowTabs.setSelectedIndex(0);
 					}
 				} else if (command == "Stop Forwarding"){
 					client.handleMessageFromClientUI("#endforward");
 				}
 			}
 		});
 
 		Availability.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				if(Availability.isSelected()){
 					client.handleMessageFromClientUI("#notavailable");
 					Availability.setText("Unavailable");
 					Availability.setBackground(Color.decode("#E74C3C"));
 					ForwardButton.setEnabled(false);
 					MessageInput.setEnabled(false);
 					DrawPadButton.setEnabled(false);
 					PrivateMsg.setEnabled(false);
 					Send.setEnabled(false);
 				} else {
 					client.handleMessageFromClientUI("#available");
 					Availability.setText("Available");
 					Availability.setBackground(Color.decode("#2ECC71"));
 					ForwardButton.setEnabled(true);
 					MessageInput.setEnabled(true);
 					DrawPadButton.setEnabled(true);
 					PrivateMsg.setEnabled(true);
 					Send.setEnabled(true);
 				}
 			}
 		});
 
 		Send.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String command = Send.getActionCommand();
 				if(command == "Send"){
 					String message = MessageInput.getText();
 					if(message != null && message.length() > 0){
 						client.handleMessageFromClientUI(message);
 						MessageInput.setText("");
 					}
 				}
 			}
 		});
 
 		DrawPadButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String command = DrawPadButton.getActionCommand();
 				if(command == "Draw Pad"){
 					drawPad = new ChatDrawPad();
 					new OpenDrawPad(drawPad, self);
 				}
 			}
 		});
 
 		PrivateMsg.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String command = PrivateMsg.getActionCommand();
 				if(command == "Private Message"){					
 					PrivateMessageDialog.setVisible(true);	
 				}
 			}
 		});
 
 		SendPrivate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String command = SendPrivate.getActionCommand();
 				if(command == "Send"){					
 					String recipId = PvtUserId.getText();
 					String msg = PrivateInput.getText();
 					if( recipId.length() > 0 && msg.length() > 0 )
 						client.handleMessageFromClientUI("#private " + recipId + " " + msg);
 					PvtUserId.setText("");
 					PrivateInput.setText("");
 					PrivateMessageDialog.setVisible(false);					
 				}
 			}
 		});
 
 		//Display the window.
 		chatFrame.pack();
 		chatFrame.setVisible(true);
 	}
 
 	/**
 	 * @param args the command line arguments
 	 */
 	public static void main(String args[]) {
 		final String[] arguments = args;
 		/* Set the Nimbus look and feel */
 		try {
 			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())) {
 					javax.swing.UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 		} catch (Exception e) {} // use Default look/feel
 		/* Create and display the form */
 		java.awt.EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				createAndShowGUI(arguments);
 			}
 		});
 	}
 
 	protected static void createAndShowGUI(String[] args) {
 		@SuppressWarnings("unused")
		LoginGUI login = new LoginGUI();
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		if(arg1 instanceof String){
 			String message = arg1.toString();
 			if(message.startsWith("Forwarding: ")){
 				ForwardButton.setText("Stop Forwarding");
 				SetChannelButton.setEnabled(false);
 				BlockButton.setEnabled(false);
 				Send.setEnabled(false);
 				MessageInput.setEnabled(false);
 				MessageDisplay.setEnabled(false);
 				Availability.setEnabled(false);
 			} else if (message.startsWith("End Forwarding:")){
 				ForwardButton.setText("Forward");
 				SetChannelButton.setEnabled(true);
 				BlockButton.setEnabled(true);
 				Send.setEnabled(true);
 				MessageInput.setEnabled(true);
 				MessageDisplay.setEnabled(true);
 				Availability.setEnabled(true);
 			} else if (message.startsWith("#send"))
 			{
 				try {
 					client.sendToServer(message.substring(6));
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				return;
 			} else if (message.startsWith("#linedraw")){
 				drawPad.updateDraw( message);
 				return;
 			}
 
 			if(!message.substring( message.length()-1, message.length()).equals("\n"))
 				message += "\n";
 
 			//Update UI display safely
 			final String msg = message;
 			SwingUtilities.invokeLater(new Runnable() 
 			{
 				public void run()
 				{
 					MessageDisplay.append(msg) ;
 				}
 			});
 		}
 	}
 }
