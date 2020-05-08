 package edu.cs319.client;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import edu.cs319.client.customcomponents.JChatPanel;
 import edu.cs319.client.customcomponents.JDocTabPanel;
 import edu.cs319.client.customcomponents.JRoomListPanel;
 import edu.cs319.connectionmanager.clientside.Proxy;
 import edu.cs319.dataobjects.DocumentSubSection;
 import edu.cs319.server.CoLabPrivilegeLevel;
 import edu.cs319.util.NotYetImplementedException;
 import edu.cs319.util.Util;
 
 /**
  * 
  * @author Amelia Gee
  * @author Justin Nelson
  * 
  */
 public class WindowClient extends JFrame implements IClient {
 
 	private Proxy proxy;
 
 	private WindowJoinCoLab colabRoomFrame;
 
 	private String userName;
 	private String roomName;
 
 	private JPanel roomPanel;
 	private JTabbedPane documentPane;
 	private JRoomListPanel roomMemberListPanel;
 	private JChatPanel chatPanel;
 
 	private JMenuItem openDocument;
 	private JMenuItem logIn;
 	private JMenuItem joinCoLabRoom;
 	private JMenuItem disconnect;
 	private JMenuItem exitCoLab;
 	private final JCheckBox showRoomMembers = new JCheckBox("Display Room Members Window");
 	private final JCheckBox showChat = new JCheckBox("Display Chat Window");
 	private JMenuItem about;
 
 	public WindowClient() {
 		// setLookAndFeel();
 		setTitle("CoLab");
 		setSize(new Dimension(900, 500));
 		setJMenuBar(createMenuBar());
 		setListeners();
 
 		roomMemberListPanel = new JRoomListPanel();
 		documentPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
 		documentPane.addTab("panel1", new JDocTabPanel());
 		documentPane.addTab("panel2", new JDocTabPanel());
 		chatPanel = new JChatPanel();
 
 		JPanel panel = new JPanel(new BorderLayout(10, 10));
 		panel.add(roomMemberListPanel, BorderLayout.WEST);
 		panel.add(documentPane, BorderLayout.CENTER);
 		panel.add(chatPanel, BorderLayout.EAST);
 		add(panel);
 	}
 
 	private JMenuBar createMenuBar() {
 		JMenuBar mainMenu = new JMenuBar();
 		JMenu file = new JMenu("File");
 		JMenu view = new JMenu("View");
 		JMenu help = new JMenu("Help");
 		openDocument = new JMenuItem("Add New Document");
 		logIn = new JMenuItem("Log In");
 		joinCoLabRoom = new JMenuItem("Join CoLab Room");
 		disconnect = new JMenuItem("Disconnect");
 		exitCoLab = new JMenuItem("Exit CoLab");
 		about = new JMenuItem("About");
 
 		file.setMnemonic(KeyEvent.VK_F);
 		view.setMnemonic(KeyEvent.VK_V);
 		help.setMnemonic(KeyEvent.VK_H);
 		openDocument.setMnemonic(KeyEvent.VK_O);
 		logIn.setMnemonic(KeyEvent.VK_L);
 		joinCoLabRoom.setMnemonic(KeyEvent.VK_J);
 		disconnect.setMnemonic(KeyEvent.VK_D);
 		exitCoLab.setMnemonic(KeyEvent.VK_X);
 		showRoomMembers.setMnemonic(KeyEvent.VK_R);
 		showChat.setMnemonic(KeyEvent.VK_C);
 		about.setMnemonic(KeyEvent.VK_A);
 
 		file.add(openDocument);
 		file.add(logIn);
 		file.add(joinCoLabRoom);
 		file.add(disconnect);
 		file.add(exitCoLab);
 		view.add(showChat);
 		view.add(showRoomMembers);
 
 		setDisconnected();
 		showChat.setSelected(true);
 		showRoomMembers.setSelected(true);
 
 		mainMenu.add(file);
 		mainMenu.add(view);
 		mainMenu.add(help);
 		return mainMenu;
 	}
 
 	private void setListeners() {
 		// FILE menu items
 		openDocument.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 		logIn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				proxy = WindowLogIn.showLoginWindow(WindowClient.this, WindowClient.this);
				if (proxy != null) {
					colabRoomFrame = new WindowJoinCoLab(WindowClient.this, proxy.getServer());
					setLogIn();
				}
 			}
 		});
 		joinCoLabRoom.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				colabRoomFrame.setVisible(true);
 			}
 		});
 		disconnect.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				proxy.getServer().leaveCoLabRoom(userName, roomName);
 			}
 		});
 		exitCoLab.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
				if (disconnect.isEnabled()) {
 					proxy.getServer().leaveCoLabRoom(userName, roomName);
 				}
 				WindowClient.this.processWindowEvent(new WindowEvent(WindowClient.this,
 						WindowEvent.WINDOW_CLOSING));
 			}
 		});
 
 		// VIEW menu items
 		showRoomMembers.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				roomPanel.setVisible(showRoomMembers.isSelected());
 			}
 		});
 		showChat.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				chatPanel.setVisible(showChat.isSelected());
 			}
 		});
 
 		// HELP menu items
 		about.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 	}
 
 	/**
 	 * Set menu items enabled/disabled for when user is logged in.
 	 */
 	private void setLogIn() {
 		openDocument.setEnabled(false);
 		logIn.setEnabled(false);
 		joinCoLabRoom.setEnabled(true);
 		disconnect.setEnabled(true);
 	}
 
 	/**
 	 * Set menu items enabled/disabled for when user has joined a CoLab Room.
 	 */
 	private void setJoinedRoom() {
 		openDocument.setEnabled(true);
 		logIn.setEnabled(false);
 		joinCoLabRoom.setEnabled(false);
 		disconnect.setEnabled(true);
 	}
 
 	/**
 	 * Set menu items enabled/disabled for when user is disconnected.
 	 */
 	private void setDisconnected() {
 		openDocument.setEnabled(false);
 		logIn.setEnabled(true);
 		joinCoLabRoom.setEnabled(false);
 		disconnect.setEnabled(false);
 	}
 
 	@Override
 	public boolean allCoLabRooms(Collection<String> roomNames) {
 		if (colabRoomFrame == null) {
 			if (Util.DEBUG) {
 				System.out.println("Client was sent list of all rooms before frame was created");
 			}
 			return false;
 		}
 		colabRoomFrame.roomsUpdated(roomNames);
 		return true;
 	}
 
 	@Override
 	public boolean allUsersInRoom(Collection<String> usernames) {
 		roomMemberListPanel.updateList(usernames);
 		return true;
 	}
 
 	@Override
 	public boolean changeUserPrivilege(String username, CoLabPrivilegeLevel newPriv) {
 		throw new NotYetImplementedException();
 	}
 
 	@Override
 	public boolean coLabRoomMemberArrived(String username) {
		chatPanel.newChatMessage("Server", "<New Chat member '" + username + "'>");
 		return roomMemberListPanel.addUser(username);
 	}
 
 	@Override
 	public boolean coLabRoomMemberLeft(String username) {
 		return roomMemberListPanel.removeUser(username);
 	}
 
 	@Override
 	public boolean newChatMessage(String usernameSender, String message) {
 		chatPanel.newChatMessage(usernameSender, message);
 		return true;
 	}
 
 	@Override
 	public boolean newChatMessage(String usernameSender, String message, String recipiant) {
 		chatPanel.newChatMessage(usernameSender, message, recipiant);
 		return true;
 	}
 
 	@Override
 	public boolean newSubSection(String username, String sectionID, String documentName,
 			DocumentSubSection section, int idx) {
 		throw new NotYetImplementedException();
 	}
 
 	@Override
 	public boolean subsectionLocked(String usernameSender, String documentName, String sectionID) {
 		throw new NotYetImplementedException();
 	}
 
 	@Override
 	public boolean subsectionUnLocked(String usernameSender, String documentName, String sectionID) {
 		throw new NotYetImplementedException();
 	}
 
 	@Override
 	public boolean subSectionRemoved(String username, String sectionID, String documentName) {
 		throw new NotYetImplementedException();
 	}
 
 	@Override
 	public boolean updateAllSubsections(String documentId, List<DocumentSubSection> allSections) {
 		throw new NotYetImplementedException();
 	}
 
 	@Override
 	public boolean updateSubsection(String usernameSender, String documentname,
 			DocumentSubSection section, String sectionID) {
 		throw new NotYetImplementedException();
 	}
 
 	@Override
 	public String getUserName() {
 		return userName;
 	}
 
 	public void setUserName(String un) {
 		userName = un;
 	}
 
 	public String getRoomName() {
 		return roomName;
 	}
 
 	public void setRoomName(String rn) {
 		roomName = rn;
 	}
 
 	public void chatLogin() {
 		try {
 			chatPanel.connect(proxy.getServer(), userName, roomName);
 		} catch (IOException e) {
 			if (Util.DEBUG)
 				e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Sets the look and feel of an application to that of the system it is running on. (Java's
 	 * default looks bad)
 	 */
 	private static void setLookAndFeel() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		}
 	}
 }
