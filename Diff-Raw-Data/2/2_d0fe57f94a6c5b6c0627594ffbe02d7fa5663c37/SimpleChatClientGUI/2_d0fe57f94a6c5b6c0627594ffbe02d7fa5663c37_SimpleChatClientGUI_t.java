 package chat;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.util.LinkedList;
 import java.util.ListIterator;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import chat.common.AeSimpleSHA1;
 import chat.common.ChatClientBase;
 import chat.common.ChatIF;
 import chat.common.SimpleChatMessage;
 import chat.common.SimpleChatMessage.messageType;
 
 /**
  * This class constructs the UI for a chat client. It implements the chat
  * interface in order to activate the display() method. Warning: Some of the
  * code here is cloned in ServerConsole
  * 
  * @author Mike Snow
  * @author Mike Gustafson
  * @version February 2010
  */
 public class SimpleChatClientGUI implements ChatIF {
 	JMenu lookFeelMenu = new JMenu("Look and Feel");
 	// Class variables *************************************************
 
 	/**
 	 * The default port to connect on.
 	 */
 	final public static int DEFAULT_PORT = 5555;
 
 	// Instance variables **********************************************
 
 	/**
 	 * The instance of the client that created this GUIChat.
 	 */
 	ChatClientBase client;
 
 	// the main frame of the application
 	private JFrame frame = new JFrame();
 	// data that the list will hold
 	private DefaultListModel listData = new DefaultListModel();
 	// the list that holds the lines of data
 	private JList list = new JList(listData);
 	// the place where the user types their message
 	private JTextField inputText = new JTextField(20);
 	// scrollbox that holds the list
 	private JScrollPane scrollPane = new JScrollPane(list);
 	private JButton btnSend = new JButton("Send");
 	private String userName; // the user's name
 	boolean loggedIn = false;
 	boolean loginResponse = false;
 	private DefaultListModel userListData = new DefaultListModel();
 	// the list that holds the lines of data
 	private JList userList = new JList(userListData);
 	private JScrollPane scrollUserList = new JScrollPane(userList);
 	private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 			scrollPane, scrollUserList);
 
 	// linked list of one-to-one windows
 	LinkedList<SimpleChatOneToOne> windowsList = new LinkedList<SimpleChatOneToOne>();
 
 	// menu stuff
 	JMenuBar menuBar = new JMenuBar();
 	JMenu menuAction = new JMenu("Action");
 	JMenuItem menuConnect = new JMenuItem("Connect", KeyEvent.VK_C);
 	JMenuItem menuDisconnect = new JMenuItem("Disconnect", KeyEvent.VK_D);
 	String host;
 	int port;
 	SimpleChatConnect connectWindow;
 
 	// Constructors ****************************************************
 
 	/**
 	 * Constructs an instance of the ClientConsole UI.
 	 * 
 	 * @param host
 	 *            The host to connect to.
 	 * @param port
 	 *            The port to connect on.
 	 */
 	public SimpleChatClientGUI(String host, int port) {
 		this.host = host;
 		this.port = port;
 		frame.setLayout(new BorderLayout()); // layout manager
 		JPanel p = new JPanel(new BorderLayout()); // panel inside the frame
 
 		// add items to the panel
 		p.add(inputText, BorderLayout.CENTER);
 		p.add(btnSend, BorderLayout.EAST);
 		btnSend.setEnabled(false);
 
 		// add items to the frame
 		// frame.add(scrollPane, BorderLayout.CENTER);
 		// frame.add(scrollUserList, BorderLayout.EAST);
 		splitPane.setDividerLocation(450);
 		// splitPane.setResizeWeight(0.75);
 
 		frame.add(splitPane, BorderLayout.CENTER);
 
 		frame.add(p, BorderLayout.SOUTH);
 
 		frame.setTitle("Chat client");
 		frame.setSize(600, 600);
 
 		menuAction.setMnemonic(KeyEvent.VK_A);
 		menuBar.add(menuAction);
 		menuAction.add(menuConnect);
 		menuAction.add(menuDisconnect);
 
		// code adapted from Sun tutorial
 		UIManager.LookAndFeelInfo[] lookFeelTypes = UIManager
 				.getInstalledLookAndFeels();
 		for (int i = 0; i < lookFeelTypes.length; i++) {
 			final UIManager.LookAndFeelInfo lookAndFeelInfo = lookFeelTypes[i];
 			JMenuItem item = new JMenuItem(lookAndFeelInfo.getName());
 			item.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					try {
 						UIManager
 								.setLookAndFeel(lookAndFeelInfo.getClassName());
 						SwingUtilities.updateComponentTreeUI(frame);
 						ListIterator<SimpleChatOneToOne> it = windowsList.listIterator();
 						SimpleChatOneToOne w;
 						while (it.hasNext()) {
 							w = it.next();
 							SwingUtilities.updateComponentTreeUI(w.getFrame());
 						}
 					} catch (ClassNotFoundException e1) {
 						e1.printStackTrace();
 					} catch (InstantiationException e1) {
 						e1.printStackTrace();
 					} catch (IllegalAccessException e1) {
 						e1.printStackTrace();
 					} catch (UnsupportedLookAndFeelException e1) {
 						e1.printStackTrace();
 					}
 				}
 			});
 			lookFeelMenu.add(item);
 		}
 		menuBar.add(lookFeelMenu);
 		frame.setJMenuBar(menuBar);
 
 		menuConnect.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				connectMenu();
 			}
 		});
 
 		menuDisconnect.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				disconnectMenu();
 			}
 		});
 
 		// when the users closes the window, exit the program
 		frame.addWindowListener(new WindowAdapter() {
 
 			@Override
 			public void windowClosing(WindowEvent e) {
 				System.exit(0);
 			}
 		});
 
 		inputText.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				accept();
 			}
 		});
 
 		// when the user clicks the send button, send the data to the server
 		btnSend.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				accept();
 			}
 		});
 
 		userList.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2)
 					openPeerWindow(e);
 			}
 		});
 
 		frame.setVisible(true); // make the window visible
 
 		// find out the user's name
 		// userName = JOptionPane.showInputDialog(frame,
 		// "What is your name?");
 
 	}
 
 	private void connectMenu() {
 		try {
 			if (connectWindow == null)
 				connectWindow = new SimpleChatConnect(frame, "Connect", true);
 			else {
 				connectWindow.setLocationRelativeTo(frame);
 				connectWindow.setVisible(true);
 			}
 			if (connectWindow.isConnectClicked == false)
 				return;
 			connectWindow.isConnectClicked = false;
 
 			host = connectWindow.getTxtServer();
 			port = connectWindow.getTxtPort();
 
 			client = new ChatClientBase(host, port, this);
 			// addLine("Successfully connected to the server!");
 			btnSend.setEnabled(true);
 
 			SimpleChatLogin loginWindow = new SimpleChatLogin(frame, "Login",
 					true);
 
 			loginWindow.addWindowListener(new WindowAdapter() {
 				public void windowClosing(WindowEvent e) {
 					loggedIn = false;
 					loginResponse = false;
 
 					disconnectMenu();
 
 				}
 			});
 			while (!loggedIn) {
 				loginWindow.setVisible(true);
 				loginResponse = false;
 				userName = loginWindow.txtName.getText();
 				String message = new String(loginWindow.txtPass.getPassword());
 
 				SimpleChatMessage loginMsg = new SimpleChatMessage();
 				loginMsg.messageId = messageType.LOGIN_ATTEMPT;
 				loginMsg.username = userName;
 				try {
 					loginMsg.message = AeSimpleSHA1.SHA1(message);
 				} catch (NoSuchAlgorithmException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				client.handleMessageFromClientUI(loginMsg);
 
 				while (!loginResponse) {
 					;
 				}
 				loginWindow.txtPass.setText("");
 			}
 			loginWindow.dispose();
 
 			inputText.requestFocusInWindow();
 
 		} catch (IOException exception) {
 			JOptionPane.showMessageDialog(frame, "Can't setup connection!",
 					"Error", JOptionPane.ERROR_MESSAGE);
 			System.out.println("Error: Can't setup connection!");
 
 		}
 	}
 
 	private void disconnectMenu() {
 		try {
 			userListData.clear();
 			btnSend.setEnabled(false);
 
 			if (client != null && client.isConnected())
 				client.closeConnection();
 			client = null;
 			loggedIn = false;
 			loginResponse = false;
 
 			// remove all peer2peer windows
 			ListIterator<SimpleChatOneToOne> it = windowsList.listIterator();
 			SimpleChatOneToOne w;
 			while (it.hasNext()) {
 				w = it.next();
 				w.dispose();
 
 				// not sure if these 2 lines are needed
 				w = null;
 				it.remove();
 			}
 			windowsList.clear();
 
 			addLine("Disconnected to the server");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	// Instance methods ************************************************
 
 	/**
 	 * This method takes input from the GUI. Once it is received, it sends it to
 	 * the client's message handler.
 	 */
 	public void accept() {
 		SimpleChatMessage msg = new SimpleChatMessage();
 
 		// update whoIsOnline list.
 		// this should probably be moved to its own thread
 		msg.messageId = messageType.USERDB_LIST;
 		client.handleMessageFromClientUI(msg);
 
 		// errors without this
 		msg = new SimpleChatMessage();
 
 		msg.messageId = messageType.CHATROOM_MESSAGE;
 		msg.username = userName;
 		msg.message = inputText.getText();
 
 		client.handleMessageFromClientUI(msg);
 
 		// server doesn't send back the message
 		addLine("<html></b><font color=blue>" + userName + "</font>" + ": "
 				+ inputText.getText());
 
 		inputText.setText("");
 	}
 
 	/**
 	 * This method overrides the method in the ChatIF interface. It displays a
 	 * message onto the screen.
 	 * 
 	 * @param message
 	 *            The string to be displayed.
 	 */
 	public void display(Object message) {
 		SimpleChatMessage msg = (SimpleChatMessage) message;
 		switch (msg.messageId) {
 		case CONNECTION_ESTABLISHED:
 			addLine(msg.message);
 			break;
 		case LOGIN_SUCCESSFUL:
 			loggedIn = true;
 			loginResponse = true;
 			// addLine(msg.message);
 			addLine("Successfully logged in as " + userName);
 			break;
 		case LOGIN_FAILED:
 			loggedIn = false;
 			loginResponse = true;
 			addLine(msg.message);
 			break;
 		case USERDB_LIST: {
 			String[] uList = msg.message.split("\\s");
 			userListData.clear();
 			for (int x = 0; x < uList.length; x++)
 				userListData.addElement(uList[x]);
 			break;
 		}
 		case CHATROOM_MESSAGE:
 			addLine("<html><font color=red>" + msg.username + "</font>: "
 					+ msg.message);
 			break;
 		case PEER_MESSAGE: {
 			SimpleChatOneToOne peerWindow = getWindowByName(msg.username);
 			peerWindow.peerMessage(msg.message);
 			peerWindow.getFrame().setVisible(true);
 			peerWindow.getFrame().requestFocus();
 			// addLine(msg.username + ": " + msg.message);
 			break;
 		}
 		}
 
 		// uncomment to add debug messages
 		// addLine(msg.messageId + " " + msg.username + " " + msg.message);
 		loginResponse = true;
 	}
 
 	/**
 	 * Add a line of data to the list
 	 * 
 	 * @param msg
 	 *            the text to add
 	 */
 	public void addLine(String msg) {
 		listData.addElement(msg);
 		list.ensureIndexIsVisible(listData.getSize() - 1); // auto scroll the
 		// list
 		list.repaint();
 	}
 
 	// Class methods ***************************************************
 
 	/**
 	 * This method is responsible for the creation of the Client UI.
 	 * 
 	 * @param args
 	 *            [0] The host to connect to.
 	 */
 	public static void main(String[] args) {
 		String host = "";
 		int port = 0; // The port number
 		try {
 			host = args[0];
 		} catch (ArrayIndexOutOfBoundsException e) {
 			host = "localhost";
 		}
 		try {
 			port = Integer.parseInt(args[1]);
 		} catch (ArrayIndexOutOfBoundsException e) {
 			port = DEFAULT_PORT;
 		}
 
 		SimpleChatClientGUI chat = new SimpleChatClientGUI(host, port);
 	}
 
 	public void sendToPeer(String peerName, String message) {
 		SimpleChatMessage msg = new SimpleChatMessage();
 
 		msg.messageId = messageType.PEER_MESSAGE;
 		msg.username = userName;
 		msg.message = message;
 
 		client.handleMessageFromClientUI(msg);
 
 	}
 
 	private void openPeerWindow(MouseEvent e) {
 		int index = userList.locationToIndex(e.getPoint());
 		String peerName = (String) userListData.get(index);
 
 		SimpleChatOneToOne w = getWindowByName(peerName);
 		w.getFrame().setVisible(true);
 		w.getFrame().requestFocus();
 	}
 
 	private SimpleChatOneToOne getWindowByName(String peerName) {
 		ListIterator<SimpleChatOneToOne> it = windowsList.listIterator();
 		SimpleChatOneToOne w;
 		while (it.hasNext()) {
 			w = it.next();
 
 			if (peerName.equals(w.getPeerName())) {
 				return w;
 			}
 		}
 
 		// couldn't find the window
 		w = new SimpleChatOneToOne(userName, peerName, this);
 		windowsList.add(w);
 
 		w.getFrame().setVisible(true);
 		w.getFrame().requestFocus();
 
 		return w;
 	}
 }
 // End of ConsoleChat class
