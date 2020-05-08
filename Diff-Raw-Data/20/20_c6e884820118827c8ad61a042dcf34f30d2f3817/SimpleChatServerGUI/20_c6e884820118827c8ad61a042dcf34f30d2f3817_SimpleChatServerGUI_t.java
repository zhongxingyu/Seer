 package chat;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 import chat.common.ChatUser;
 import chat.common.ChatUserDB;
 import chat.common.SimpleChatMessage;
 import chat.common.SimpleChatMessage.messageType;
 
 import com.lloseng.ocsf.server.AbstractServer;
 import com.lloseng.ocsf.server.ConnectionToClient;
 
 /**
  * This class overrides some of the methods in the abstract superclass in order
  * to give more functionality to the server.
  * 
  * @author Mike Snow
  * @author Mike Gustafson
  * @version February 2010
  */
 public class SimpleChatServerGUI extends AbstractServer {
 	/**
 	 * The default port to listen on.
 	 */
 	final public static int DEFAULT_PORT = 5555;
 
 	// GUI stuff
 	JFrame frame = new JFrame("Chat server");
 	private DefaultListModel listData = new DefaultListModel();
 	// the list that holds the lines of data
 	private JList list = new JList(listData);
 	private JScrollPane scrollPane = new JScrollPane(list);
 
 	private JTextField portField = new JTextField(Integer
 			.toString(DEFAULT_PORT), 5);
 	private JButton btnStart = new JButton("Start");
 	private JButton btnStop = new JButton("Stop");
 	private JPanel panel = new JPanel();
 	private JLabel portLabel = new JLabel("Port number:");
 	private JFileChooser fc;
 
 	// menu stuff
 	JMenuBar menuBar = new JMenuBar();
 	JMenu menuAction = new JMenu("Logging");
 	ButtonGroup btnGroup = new ButtonGroup();
 	JRadioButtonMenuItem menuEnableLogging = new JRadioButtonMenuItem(
 			"Enable logging", false);
 	JRadioButtonMenuItem menuDisableLogging = new JRadioButtonMenuItem(
 			"Disable logging", true);
 
 	// Class variables *************************************************
 
 	// Instance variables *************************************************
 
 	// List of all the registered users to this server
 	private ChatUserDB chatUserDB;
 
 	// log file
 	private FileWriter logFile;
 
 	// Constructors ****************************************************
 
 	/**
 	 * Constructs an instance of the echo server.
 	 * 
 	 * @param port
 	 *            The port number to connect on.
 	 */
 	public SimpleChatServerGUI(int port) {
 		super(port);
 		try {
 			close();
 		} catch (IOException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		}
 
 		btnStart.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				try {
 					setPort(Integer.parseInt(portField.getText()));
 					listen();
					btnStart.setEnabled(false);
					btnStop.setEnabled(true);
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
					writeToLog("Error: could not start server");
 				} // Start listening for connections

 			}
 		});
 
 		btnStop.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				try {
 					close();
					btnStart.setEnabled(true);
					btnStop.setEnabled(false);
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				} // Start listening for connections

 			}
 		});
 
 		menuEnableLogging.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				fc = new JFileChooser();
 
 				int returnVal = fc.showDialog(frame, "Set Log File");
 
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					try {
 						logFile = new FileWriter(fc.getSelectedFile(), true);
 					} catch (IOException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 				} else {
 					menuEnableLogging.setSelected(false);
 					menuDisableLogging.setSelected(true);
 				}
 
 			}
 		});
 
 		menuDisableLogging.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (logFile != null) {
 					try {
 
 						logFile.flush();
 						logFile.close();
 
 					} catch (IOException e1) {
 
 						e1.printStackTrace();
 					}
 				}
 				logFile = null;
 
 			}
 		});
 
 		frame.setSize(600, 600);
 		frame.setLayout(new BorderLayout());
 		panel.setLayout(new FlowLayout());
 		panel.add(portLabel);
 		panel.add(portField);
 		panel.add(btnStart);
 		panel.add(btnStop);
 		frame.add(scrollPane, BorderLayout.CENTER);
 		frame.add(panel, BorderLayout.SOUTH);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		btnGroup.add(menuEnableLogging);
 		btnGroup.add(menuDisableLogging);
 
 		menuBar.add(menuAction);
 
 		menuAction.add(menuEnableLogging);
 		menuAction.add(menuDisableLogging);
 		frame.setJMenuBar(menuBar);
 
 		btnStop.setEnabled(false);
 		// frame.pack();
 		frame.setVisible(true);
 	}
 
 	// Instance methods ************************************************
 
 	/**
 	 * This method handles reception of all new clients that connect to the
 	 * server.
 	 * 
 	 * @param client
 	 *            the connection connected to the client.
 	 */
 	protected void clientConnected(ConnectionToClient client) {
 		writeToLog("Client connected to server: " + client.toString());
 	}
 
 	/**
 	 * This method handles events that occur when a clients disconnects from the
 	 * server.
 	 * 
 	 * @param client
 	 *            the connection with the client.
 	 */
 	synchronized protected void clientDisconnected(ConnectionToClient client) {
 		writeToLog("Client disconnected from server: " + client.toString());
 	}
 
 	/**
 	 * This method handles any messages received from the client.
 	 * 
 	 * @param msg
 	 *            The message received from the client.
 	 * @param client
 	 *            The connection from which the message originated.
 	 */
 	public void handleMessageFromClient(Object msg, ConnectionToClient client) {
 		// TODO: move all the checks for user logged into to one place
 		SimpleChatMessage msg1 = (SimpleChatMessage) msg;
 		writeToLog(client + " " + msg1.messageId + " " + msg1.username + " "
 				+ msg1.message);
 		switch (msg1.messageId) {
 		case CHATROOM_MESSAGE:
 			if (client.getInfo("loggedIn").equals(true)) {
 				this.sendToAllClients(msg1);
 			}
 			break;
 		case USERDB_LIST:
 			if (client.getInfo("loggedIn").equals(true)) {
 				msg1.message = whoIsOnline();
 				try {
 					client.sendToClient(msg1);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			break;
 		case LOGIN_ATTEMPT:
 			// First see if the user has already logged in, only allow one
 			// connection for now
 			if (!checkUserLoggedIn(msg1.username)) {
 				ChatUser loginUser = chatUserDB.getChatUser(msg1.username);
 				if (loginUser != null) {
 					if ((msg1.message).equals((loginUser.getPassword()).trim())) {
 						client.setInfo("userDBobj", loginUser);
 						client.setInfo("loggedIn", true);
 						msg1.messageId = messageType.LOGIN_SUCCESSFUL;
 						msg1.message = "Everything good";
 
 						// auto update list of online users (nice for GUI
 						// client)
 						SimpleChatMessage tempMsg = new SimpleChatMessage();
 						tempMsg.messageId = messageType.USERDB_LIST;
 						tempMsg.message = whoIsOnline();
 
 						sendToAllClients(tempMsg);
 
 					} else {
 						client.setInfo("loggedIn", false);
 						msg1.messageId = messageType.LOGIN_FAILED;
 						msg1.message = "Bad password";
 					}
 				} else {
 					client.setInfo("loggedIn", false);
 					msg1.messageId = messageType.LOGIN_FAILED;
 					msg1.message = "Bad username";
 				}
 			} else {
 				client.setInfo("loggedIn", false);
 				msg1.messageId = messageType.LOGIN_FAILED;
 				msg1.message = "User is already logged in.";
 			}
 			msg1.username = "Server";
 			try {
 				client.sendToClient(msg1);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			break;
 		case PEER_MESSAGE: {
 			// writeToLog(msg1.user);
 			if (!checkUserLoggedIn(msg1.username)) {
 				break;
 			}
 			SimpleChatMessage newMsg = new SimpleChatMessage();
 
 			// extract peerName from the message
 			String[] messageArray = msg1.message.split("\\s", 2);
 			String peerName = messageArray[0];
 			String realMessage = messageArray[1];
 
 			// convert peerName to particular thread/client
 
 			ConnectionToClient peerClient = null;
 			Thread[] clientThreadList = getClientConnections();
 
 			for (int i = 0; i < clientThreadList.length; i++) {
 				peerClient = (ConnectionToClient) clientThreadList[i];
 				Boolean loggedIn = (Boolean) (peerClient).getInfo("loggedIn");
 
 				ChatUser chatUser = (ChatUser) (peerClient)
 						.getInfo("userDBobj");
 
 				if (loggedIn && chatUser.getUserName().trim().equals(peerName)) {
 					newMsg.messageId = SimpleChatMessage.messageType.PEER_MESSAGE;
 					newMsg.username = msg1.username;
 					newMsg.message = realMessage;
 
 					try {
 						peerClient.sendToClient(newMsg);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					break;
 				}
 
 			}
 			break;
 
 		}
 		default:
 			// ut oh, unidentified message
 			break;
 		}
 	}
 
 	/**
 	 * This method overrides the one in the superclass. Called when the server
 	 * starts listening for connections.
 	 */
 	protected void serverStarted() {
 		writeToLog("Server listening for connections on port " + getPort());
 		chatUserDB = new ChatUserDB();
 	}
 
 	/**
 	 * This method overrides the one in the superclass. Called when the server
 	 * stops listening for connections.
 	 */
 	protected void serverStopped() {
 		writeToLog("Server has stopped listening for connections.");
 	}
 
 	/**
 	 * This method overrides the one in the superclass. Sends a message to every
 	 * client connected to the server except for the client that originated the
 	 * message. Any exception thrown while sending the message to a particular
 	 * client is ignored.
 	 * 
 	 * @param msg
 	 *            Object The message to be sent
 	 */
 	public void sendToAllClients(Object msg) {
 		SimpleChatMessage msg1 = (SimpleChatMessage) msg;
 		Thread[] clientThreadList = getClientConnections();
 
 		for (int i = 0; i < clientThreadList.length; i++) {
 			try {
 				Boolean loggedIn = (Boolean) ((ConnectionToClient) clientThreadList[i])
 						.getInfo("loggedIn");
 				ChatUser chatUser = (ChatUser) ((ConnectionToClient) clientThreadList[i])
 						.getInfo("userDBobj");
 				String cun = chatUser.getUserName().trim();
 				// Verify that the recipient is logged in and it is not the
 				// originator of the message
 				if (loggedIn && !cun.equals(msg1.username)) {
 					((ConnectionToClient) clientThreadList[i])
 							.sendToClient(msg);
 				}
 			} catch (Exception ex) {
 			}
 		}
 	}
 
 	/**
 	 * This method overrides the one in the superclass. Sends a message to every
 	 * client connected to the server except for the client that originated the
 	 * message. Any exception thrown while sending the message to a particular
 	 * client is ignored.
 	 * 
 	 * @param msg
 	 *            Object The message to be sent
 	 */
 	public String whoIsOnline() {
 		Thread[] clientThreadList = getClientConnections();
 		String listing = "";
 
 		for (int i = 0; i < clientThreadList.length; i++) {
 			try {
 				Boolean loggedIn = (Boolean) ((ConnectionToClient) clientThreadList[i])
 						.getInfo("loggedIn");
 				// Verify that the recipient is logged in and it is not the
 				// originator of the message
 				if (loggedIn) {
 					ChatUser chatUser = (ChatUser) ((ConnectionToClient) clientThreadList[i])
 							.getInfo("userDBobj");
 					listing += chatUser.getUserName().trim() + " ";
 				}
 			} catch (Exception ex) {
 			}
 		}
 		return listing;
 	}
 
 	/**
 	 * This method overrides the one in the superclass. Sends a message to every
 	 * client connected to the server except for the client that originated the
 	 * message. Any exception thrown while sending the message to a particular
 	 * client is ignored.
 	 * 
 	 * @param uname
 	 *            String The username to check against
 	 * @return Boolean true if user is already logged in and false otherwise
 	 */
 	public Boolean checkUserLoggedIn(String uname) {
 		Thread[] clientThreadList = getClientConnections();
 		Boolean loggedIn = false;
 		int numClients = clientThreadList.length;
 
 		writeToLog("Username is: " + uname);
 
 		for (int i = 0; i < numClients; i++) {
 			try {
 				loggedIn = (Boolean) ((ConnectionToClient) clientThreadList[i])
 						.getInfo("loggedIn");
 				ChatUser chatUser = (ChatUser) ((ConnectionToClient) clientThreadList[i])
 						.getInfo("userDBobj");
 				String cun = chatUser.getUserName().trim();
 
 				// If the user is already logged in
 				if (loggedIn && cun.equals(uname)) {
 					i = numClients;
 				} else {
 					loggedIn = false;
 				}
 			} catch (Exception ex) {
 				// Comes into here for the client thread just trying to connect
 				// because they have not been fully setup
 				// But this is ok, because we are looking for another thread
 				// that exists for the user
 				loggedIn = false;
 			}
 		}
 		return loggedIn;
 	}
 
 	// Class methods ***************************************************
 
 	/**
 	 * This method is responsible for the creation of the server instance (there
 	 * is no UI in this phase).
 	 * 
 	 * @param args
 	 *            [0] The port number to listen on. Defaults to 5555 if no
 	 *            argument is entered.
 	 */
 	public static void main(String[] args) {
 
 		SimpleChatServerGUI sv = new SimpleChatServerGUI(DEFAULT_PORT);
 
 		// sv.listen(); // Start listening for connections
 
 	}
 
 	public void writeToLog(String message) {
 		if (logFile != null) {
 			try {
 				logFile.write(message + System.getProperty("line.separator"));
 				logFile.flush();
 			} catch (IOException e) {
				writeToLog("Error: could not write to log file");
 			}
 		} else {
 			listData.addElement(message);
 			list.ensureIndexIsVisible(listData.getSize() - 1);
 			list.repaint();
 		}
 
 	}
 }
