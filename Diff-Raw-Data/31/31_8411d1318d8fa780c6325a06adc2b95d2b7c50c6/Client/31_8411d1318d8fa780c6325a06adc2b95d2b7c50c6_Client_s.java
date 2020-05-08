 package com.maspain.chitchat;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 import java.awt.Color;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 
 import javax.swing.JTextArea;
 import java.awt.GridBagConstraints;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.InputMap;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 import javax.swing.JSplitPane;
 
 public class Client extends JFrame implements Runnable {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private String version = "1.3";
 	private String title = "ChitChat Client v" + version + " | Channel: ";
 	private int portOffset = 50000;
 	private String arrivedMessage = " has entered the room";
 	private String leavingMessage = " has left the room";
 	
 	private int green = 0x07BA07;
 	
 	private JPanel contentPane;
 	private String name, address;
 	private int port;
 	private JScrollPane scrollPaneHistory;
 	//private JTextArea txtHistory;	// JTextArea does not support font styling
 	private ColorPane txtHistory;
 	private JTextArea txtOnlineUsers;
 	private JButton btnSend;
 	
 	// The Socket connection us to the server
 	private Socket socket;
 	
 	// The streams we communicate to the server; these come from the socket
 	private DataInputStream din;
 	private DataOutputStream dout;
 	private JComboBox<String> channelList;
 	private JScrollPane scrollPaneOnlineUsers;
 	private JLabel lblOnline;
 	private JSplitPane splitPane;
 	private JTextArea txtMessage;
 	
 	
 	public Client(String name, String address, int port) {
 
 		this.name = name;
 		this.address = address;
 		this.port = port;
 		
 		int channel = port - portOffset + 1;
 		
 		setTitle(title + channel);
 
 		createWindow();
 		connectToServer();
 
 		// Add new functionality by adding a new key binding to "shift ENTER"
 		// We want to be able to press "shift ENTER" to generate a new line in the message
 		Action newLineOnShiftEnter = new AbstractAction() {
 			
 			private static final long serialVersionUID = 1L;
 			
 			public void actionPerformed(ActionEvent e) {
 				txtMessage.append("\n");
 			}
 		};
 		String newLineKeyStrokeAndKey = "shift ENTER";
 		KeyStroke newLineKeyStroke = KeyStroke.getKeyStroke(newLineKeyStrokeAndKey);
 		txtMessage.getInputMap(JComponent.WHEN_FOCUSED).put(newLineKeyStroke, newLineKeyStrokeAndKey);
 		txtMessage.getActionMap().put(newLineKeyStrokeAndKey, newLineOnShiftEnter);
 		
 		// Replace the functionality of the "ENTER" key stroke so that it sends the message
 		// rather than generate a new line in the message to be sent
 		Action sendOnEnter = new AbstractAction() {
 			
 			private static final long serialVersionUID = 1L;
 			
 			public void actionPerformed(ActionEvent e) {
 				try {
 					processMessage(txtMessage.getText());
 				}
 				catch (NullPointerException ne) {
 					System.out.println("message is empty. exception: " + ne);
 				}
 			}
 		};
 		KeyStroke sendKeyStroke = KeyStroke.getKeyStroke("ENTER");
 		InputMap im = txtMessage.getInputMap(JComponent.WHEN_FOCUSED);
 		txtMessage.getActionMap().put(im.get(sendKeyStroke), sendOnEnter);
 		
 		// Attach the send action to the send button
 		btnSend.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				try {
 					processMessage(txtMessage.getText());
 				}
 				catch (NullPointerException ne) {
 					System.out.println("message is empty. exception: " + ne);
 				}
 			}
 		});
 		
 		Action changeChannel = new AbstractAction() {
 			
 			private static final long serialVersionUID = 1L;
 
 			public void actionPerformed(ActionEvent e) {
 				switchToPort(channelList.getSelectedIndex() + portOffset);
 			}
 		};
 		
 		channelList.addActionListener(changeChannel);
 		
 
 	}
 	
 	private void connectToServer() {
 		try {

 			socket = new Socket(InetAddress.getByName(address), port);

 			// We got a connection! Tell the world
 			System.out.println("connected to " + socket);
 			
			// Let's grab the streams and create DataInput/Output streams from
			// them
 			din = new DataInputStream(socket.getInputStream());
 			dout = new DataOutputStream(socket.getOutputStream());
 			
 			// Start a background thread for receiving messages
 			new Thread(this).start();
 			
 			// Prepare the data packet for sending as a new connection 
 			Packet packet = new Packet(Packet.CONNECT, name, arrivedMessage);
 
 			// Let everyone know you're here!
 			dout.writeUTF(packet.getData());
 			
 		}
 		catch (IOException ie) {
 			ie.printStackTrace();
 		}
 	}
 	
 	private void switchToPort(int port) {
 		leave();
 		dispose();
 		new Client(this.name, this.address, port);
 	}
 	
 	// Gets called when the user types something
 	private void processMessage(String message) {
 		try {
 			// Prepare the data packet for sending as a message
 			Packet packet = new Packet(Packet.MESSAGE, name, message);
 			
 			// Send the message with the time stamp and the name of the sender to the server
 			dout.writeUTF(packet.getData());
 			
 			// Clear out the text input field
 			txtMessage.setText("");
 		}
 		catch (IOException ie) {
 			System.out.println(ie);
 		}
 	}
 	
 	private String getTimeStamp() {
 		// Create a time stamp for the current message, and attach it to the message
 		String timeStamp = new SimpleDateFormat("hh:mm:ss aaa").format(Calendar.getInstance().getTime());
 		return timeStamp;
 	}
 	
 	private void leave() {
 		Packet packet = new Packet(Packet.DISCONNECT, name, leavingMessage);
 		try {
 			dout.writeUTF(packet.getData());
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	// Background thread runs this: show messages from other window
 	public void run() {
 		try {
 			
 			// Receive messages one-by-one, forever
 			while (true) {
 				
 				// Get the next data string
 				String data = din.readUTF();
 				
 				Packet packet = new Packet(data);
 				
 				if (packet.getMessage() == null) {
 					continue;
 				}
 				
 				// Print a time stamp for the current message, then print the name of the sender
 				txtHistory.append(Color.darkGray, "(" + getTimeStamp() + ") ");
 				txtHistory.append(Color.blue, packet.getSender());
 				
 				if (packet.getCommand().equals(Packet.CONNECT)) {
 					// Print welcome message to our text window
 					txtHistory.append(new Color(green), arrivedMessage + "\n");
 					
 					// Refresh the list of online users
 					txtOnlineUsers.setText(packet.getMessage());
 					
 					// Play hello sound
 					Sound.sound_beeps.play();
 				}
 				else if (packet.getCommand().equals(Packet.DISCONNECT)) {
 					// Print goodbye message to our text window
 					txtHistory.append(Color.red, leavingMessage + "\n");
 					
 					// Refresh the list of online users
 					txtOnlineUsers.setText(packet.getMessage());
 					
 					// Play goodbye sound
 					Sound.sound_click.play();
 				}
 				else if (packet.getCommand().equals(Packet.MESSAGE)) {		
 					// Append colon for message styling
 					txtHistory.append(Color.blue, ":  ");
 					
 					// Print message to our text window
 					txtHistory.append(Color.black, packet.getMessage() + "\n");
 					
 					// Play a sound to indicate that a message has been received
 					Sound.sound_pop.play();
 				}
 				
 				// Set the caret position to the bottom of the message history field
 				txtHistory.setCaretPosition(txtHistory.getDocument().getLength());
 			}
 		}
 		catch (IOException ie) {
 			ie.printStackTrace();
 		}
 	}
 	
 	private void createWindow() {
 		
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		}
 
 		
 		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		WindowListener exitListener = new WindowListener() {
 			public void windowClosing(WindowEvent e) {
 				leave();
 				dispose();
 				System.exit(0);
 			}
 
 			public void windowOpened(WindowEvent e) {}
 			public void windowClosed(WindowEvent e) {}
 			public void windowIconified(WindowEvent e) {}
 			public void windowDeiconified(WindowEvent e) {}
 			public void windowActivated(WindowEvent e) {}
 			public void windowDeactivated(WindowEvent e) {}
 		};
 		addWindowListener(exitListener);
 		
 		setSize(600, 530);
 		setLocationRelativeTo(null);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		
 		GridBagLayout gbl_contentPane = new GridBagLayout();
 		gbl_contentPane.columnWidths = new int[] {0, 0, 0};
 		gbl_contentPane.rowHeights = new int[] {0, 0};
 		gbl_contentPane.columnWeights = new double[] { 1.0, 0.0, 0.0 };
 		gbl_contentPane.rowWeights = new double[] { 1.0, 0.0 };
 		contentPane.setLayout(gbl_contentPane);
 		
 		splitPane = new JSplitPane();
 		splitPane.setBorder(BorderFactory.createLineBorder(Color.black));
 		GridBagConstraints gbc_splitPane = new GridBagConstraints();
 		gbc_splitPane.gridwidth = 3;
 		gbc_splitPane.insets = new Insets(10, 10, 5, 10);
 		gbc_splitPane.fill = GridBagConstraints.BOTH;
 		gbc_splitPane.gridx = 0;
 		gbc_splitPane.gridy = 0;
 		contentPane.add(splitPane, gbc_splitPane);
 		scrollPaneHistory = new JScrollPane();
 		splitPane.setLeftComponent(scrollPaneHistory);
 		
 		// for using JTextArea (does not support font styles)
 		//txtHistory = new JTextArea();
 		//txtHistory.setTabSize(4);
 		//txtHistory.setWrapStyleWord(true);
 		//txtHistory.setLineWrap(true);
 		txtHistory = new ColorPane();
 		scrollPaneHistory.setViewportView(txtHistory);
 		txtHistory.setEditable(false);
 		txtHistory.setFont(new Font("Helvetica Neue", Font.PLAIN, 16));
 		
 		scrollPaneOnlineUsers = new JScrollPane();
 		splitPane.setRightComponent(scrollPaneOnlineUsers);
 		
 		txtOnlineUsers = new JTextArea();
 		txtOnlineUsers.setTabSize(4);
 		txtOnlineUsers.setFont(new Font("Helvetica Neue", Font.PLAIN, 16));
 		txtOnlineUsers.setEditable(false);
 		txtOnlineUsers.setBackground(Color.WHITE);
 		scrollPaneOnlineUsers.setViewportView(txtOnlineUsers);
 		
 		lblOnline = new JLabel("Online");
 		lblOnline.setFont(new Font("Helvetica Neue", Font.BOLD, 16));
 		scrollPaneOnlineUsers.setColumnHeaderView(lblOnline);
 		lblOnline.setHorizontalAlignment(SwingConstants.CENTER);
 		lblOnline.setBorder(BorderFactory.createLineBorder(new Color(green)));
 		
 		txtMessage = new JTextArea();
 		txtMessage.setBorder(BorderFactory.createLineBorder(Color.black));
 		txtMessage.setWrapStyleWord(true);
 		txtMessage.setTabSize(4);
 		txtMessage.setLineWrap(true);
 		txtMessage.setFont(new Font("Helvetica Neue", Font.PLAIN, 16));
 		GridBagConstraints gbc_txtMessage = new GridBagConstraints();
 		gbc_txtMessage.insets = new Insets(5, 10, 10, 5);
 		gbc_txtMessage.fill = GridBagConstraints.BOTH;
 		gbc_txtMessage.gridx = 0;
 		gbc_txtMessage.gridy = 1;
 		contentPane.add(txtMessage, gbc_txtMessage);
 		
 		btnSend = new JButton("Send");
 		GridBagConstraints gbc_btnSend = new GridBagConstraints();
 		gbc_btnSend.fill = GridBagConstraints.BOTH;
 		gbc_btnSend.insets = new Insets(5, 5, 10, 5);
 		gbc_btnSend.gridx = 1;
 		gbc_btnSend.gridy = 1;
 		contentPane.add(btnSend, gbc_btnSend);
 		
 		channelList = new JComboBox<String>();
 		channelList.setModel(new DefaultComboBoxModel<String>(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}));
 		channelList.setSelectedIndex(port - portOffset);
 		GridBagConstraints gbc_channelList = new GridBagConstraints();
 		gbc_channelList.fill = GridBagConstraints.BOTH;
 		gbc_channelList.insets = new Insets(5, 5, 10, 10);
 		gbc_channelList.gridx = 2;
 		gbc_channelList.gridy = 1;
 		contentPane.add(channelList, gbc_channelList);
 
 		setVisible(true);
 		int pos = txtMessage.getSize().width + (splitPane.getDividerSize() / 2);
 		splitPane.setDividerLocation(pos);
 	}
 }
