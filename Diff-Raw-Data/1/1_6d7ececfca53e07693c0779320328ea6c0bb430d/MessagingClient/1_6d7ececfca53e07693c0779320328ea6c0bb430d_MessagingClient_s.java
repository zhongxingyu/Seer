 package edu.cs319.client.messageclient;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import edu.cs319.client.IClient;
 import edu.cs319.client.customcomponents.JRoomMemberList;
 import edu.cs319.connectionmanager.NotYetImplementedException;
 import edu.cs319.connectionmanager.clientside.ConnectionFactory;
 import edu.cs319.connectionmanager.clientside.Proxy;
 import edu.cs319.server.CoLabPrivilegeLevel;
 import edu.cs319.server.IServer;
 import edu.cs319.util.Util;
 
 public class MessagingClient extends JFrame implements IClient {
 
 	private JTextArea topText;
 	private JTextField bottomText;
 	private JRoomMemberList membersInRoom;
 
 	private String clientID;
 	private String roomName;
 
 	private Proxy proxy;
 	private ActionListener joinExistingRoomAction = new ActionListener() {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			String roomName = JOptionPane.showInputDialog(MessagingClient.this,
 					"Enter th eroom to join:");
 			proxy.getServer().joinCoLabRoom(clientID, roomName, null);
 		}
 	};
 
 	public MessagingClient() {
 		super("CoLabMessaging");
 		membersInRoom = new JRoomMemberList();
 		membersInRoom.setPreferredSize(new Dimension(100, 210));
 		Dimension pref = new Dimension(200, 200);
 		topText = new JTextArea();
 		topText.setEditable(false);
 		topText.setPreferredSize(pref);
 		JScrollPane topScroll = new JScrollPane(topText);
 		bottomText = new JTextField();
 		bottomText.addKeyListener(enterpressedL);
 		JPanel splitter = new JPanel(new BorderLayout());
 		splitter.add(topScroll, BorderLayout.CENTER);
 		splitter.add(bottomText, BorderLayout.SOUTH);
 		this.add(membersInRoom, BorderLayout.WEST);
 		this.add(splitter, BorderLayout.CENTER);
 		setJMenuBar(createMenuBar());
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		pack();
 		setVisible(true);
 	}
 
 	private JMenuBar createMenuBar() {
 		JMenuBar ret = new JMenuBar();
 		JMenu file = new JMenu("File");
 		JMenuItem logInToServer = new JMenuItem("Connect to Server");
 		logInToServer.addActionListener(connectTOServerAction);
 		JMenuItem showCoLabRooms = new JMenuItem("Show CoLabRooms");
 		showCoLabRooms.addActionListener(roomsAction);
 		JMenuItem joinExistingRoom = new JMenuItem("Join Existing Room");
 		joinExistingRoom.addActionListener(joinExistingRoomAction);
 		file.add(logInToServer);
 		file.add(showCoLabRooms);
 		file.add(joinExistingRoom);
 		ret.add(file);
 		return ret;
 	}
 
 	public boolean connectToServer(String host, String clientName) {
 		try {
 			proxy = ConnectionFactory.connect(host, 4444, this, clientName);
 		} catch (UnknownHostException e) {
 			if (Util.DEBUG) {
 				e.printStackTrace();
 			}
 			return false;
 		} catch (IOException e) {
 			if (Util.DEBUG) {
 				e.printStackTrace();
 			}
 			return false;
 		}
 		clientID = clientName;
 		return true;
 	}
 
 	@Override
 	public boolean coLabRoomMemberArrived(String username) {
 		return membersInRoom.getModel().addNewMember(username);
 	}
 
 	@Override
 	public boolean coLabRoomMemberLeft(String username) {
 		return membersInRoom.getModel().removeMember(username);
 	}
 
 	@Override
 	public String getName() {
 		return clientID;
 	}
 
 	@Override
 	public boolean newChatMessage(String usernameSender, String message) {
 		String fullTExt = usernameSender + ": " + message + "\n";
 		topText.append(fullTExt);
 		return true;
 	}
 
 	private ActionListener roomsAction = new ActionListener() {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			// TODO Auto-generated method stub
 			new CoLabRoomsPane(MessagingClient.this, proxy.getServer(),
 					MessagingClient.this.clientID);
 		}
 	};
 
 	private ActionListener connectTOServerAction = new ActionListener() {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			String host = JOptionPane.showInputDialog(MessagingClient.this,
 					"please enter the host to connect to.");
 			String clientName = JOptionPane.showInputDialog(MessagingClient.this,
 					"Please enter the name you would like to use:");
 			if (host == null || clientName == null)
 				return;
 			System.out.println(MessagingClient.this.connectToServer(host, clientName));
 		}
 	};
 
 	private KeyListener enterpressedL = new KeyAdapter() {
 		@Override
 		public void keyPressed(KeyEvent e) {
 			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 				proxy.getServer().newChatMessage(clientID, roomName, bottomText.getText());
 				bottomText.setText("");
 			}
 		}
 	};
 
 	@Override
 	public boolean newChatMessage(String usernameSender, String message, String recipiant) {
 		throw new NotYetImplementedException();
 	}
 
 	@Override
 	public boolean changeUserPrivilege(String username, CoLabPrivilegeLevel newPriv) {
 		throw new NotYetImplementedException();
 	}
 
 	@Override
 	public boolean textChanged(int posStart, int posEnd, String text) {
 		throw new NotYetImplementedException();
 	}
 
 	@Override
 	public boolean textHighlighted(int posStart, int posEnd) {
 		throw new NotYetImplementedException();
 	}
 
 	@Override
 	public boolean textInserted(int pos, String text) {
 		throw new NotYetImplementedException();
 	}
 
 	@Override
 	public boolean textRemoved(int posStart, int posEnd) {
 		throw new NotYetImplementedException();
 	}
 
 	@Override
 	public boolean textUnHighlighted(int posStart, int posEnd) {
 		throw new NotYetImplementedException();
 	}
 
 	public static void main(String[] args) {
 		new MessagingClient();
 	}
 
 	public void setRoomName(String text) {
 		roomName = text;
 	}
 }
 
 class CoLabRoomsPane extends JDialog {
 
 	private JList listOfRooms;
 	private JButton joinSelectedRoom;
 	private JTextField newRoomName;
 	private JButton createNewRoom;
 	private IServer server;
 	private String username;
 	private MessagingClient parent;
 
 	public CoLabRoomsPane(MessagingClient parent, IServer server, String username) {
 		super(parent, "Available CoLab Rooms");
 		this.server = server;
 		this.username = username;
 		this.parent = parent;
 		// Collection<String> allRooms = server.getAllCoLabRoomNames(username);
 		// listOfRooms = new JList(allRooms.toArray());
 		// listOfRooms.setPreferredSize(new Dimension(200, 200));
 		joinSelectedRoom = new JButton("Join Selected Room");
 		newRoomName = new JTextField(15);
 		newRoomName.addKeyListener(enterPressed);
 		createNewRoom = new JButton("Create Room");
 		createNewRoom.addActionListener(createNewRoomA);
 		JPanel mainPane = new JPanel();
 		mainPane.add(newRoomName);
 		mainPane.add(createNewRoom);
 		add(mainPane);
 		setModal(true);
 		pack();
 		setVisible(true);
 	}
 
 	private KeyListener enterPressed = new KeyAdapter() {
 
 		@Override
 		public void keyPressed(KeyEvent e) {
 			// TODO Auto-generated method stub
 			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 				createNewRoom.doClick();
 			}
 		}
 	};
 
 	private ActionListener createNewRoomA = new ActionListener() {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			server.addNewCoLabRoom(username, newRoomName.getText(), null);
 			server.joinCoLabRoom(username, newRoomName.getText(), null);
 			parent.setRoomName(newRoomName.getText());
 			CoLabRoomsPane.this.dispose();
 		}
 	};
 }
