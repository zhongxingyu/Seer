 package edu.cs319.client;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Scanner;
 
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import edu.cs319.client.customcomponents.JChatPanel;
 import edu.cs319.client.customcomponents.JDocTabPanel;
 import edu.cs319.client.customcomponents.JRoomListPanel;
 import edu.cs319.connectionmanager.clientside.Proxy;
 import edu.cs319.dataobjects.DocumentSubSection;
 import edu.cs319.dataobjects.SectionizedDocument;
 import edu.cs319.dataobjects.impl.DocumentInfoImpl;
 import edu.cs319.dataobjects.impl.DocumentSubSectionImpl;
 import edu.cs319.server.CoLabPrivilegeLevel;
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
 
 	private String userName, roomName;
 
 	private JTabbedPane tabbedDocumentPane;
 	private Map<String, JDocTabPanel> documentTabs;
 
 	private JRoomListPanel roomMemberListPanel;
 	private JChatPanel chatPanel;
 
 	private JMenuItem logIn, joinCoLabRoom;
 	private JMenuItem disconnect;
 	private JMenuItem exitCoLab;
 	private JMenuItem newDocument, openDocument, removeDocument, saveDocument;
 	private JMenuItem addSection, deleteSection, splitSection, mergeSection;
 	private final JCheckBox showRoomMembers = new JCheckBox("Display Room Members Window");
 	private final JCheckBox showChat = new JCheckBox("Display Chat Window");
 	private JMenuItem about;
 
 	public WindowClient() {
 		// setLookAndFeel();
 		setTitle("CoLab");
 		setSize(new Dimension(1000, 500));
 		setJMenuBar(createMenuBar());
 		setListeners();
 
 		roomMemberListPanel = new JRoomListPanel(this, null);
 		tabbedDocumentPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
 		documentTabs = new HashMap<String, JDocTabPanel>();
 		chatPanel = new JChatPanel();
 
 		JPanel panel = new JPanel(new BorderLayout(10, 10));
 		panel.add(roomMemberListPanel, BorderLayout.WEST);
 		panel.add(tabbedDocumentPane, BorderLayout.CENTER);
 		panel.add(chatPanel, BorderLayout.EAST);
 		add(panel);
 	}
 
 	/**
 	 * Creates a menu bar for the window.
 	 * 
 	 * @return a JMenuBar for the window client
 	 */
 	private JMenuBar createMenuBar() {
 		JMenuBar mainMenu = new JMenuBar();
 		JMenu file = new JMenu("File");
 		JMenu connect = new JMenu("Connect");
 		JMenu doc = new JMenu("Document");
 		JMenu view = new JMenu("View");
 		JMenu help = new JMenu("Help");
 		logIn = new JMenuItem("Log In");
 		joinCoLabRoom = new JMenuItem("Join CoLab Room");
 		disconnect = new JMenuItem("Disconnect");
 		exitCoLab = new JMenuItem("Exit CoLab");
 		newDocument = new JMenuItem("Add New Document");
 		openDocument = new JMenuItem("Open Document From File");
 		removeDocument = new JMenuItem("Remove Document");
 		saveDocument = new JMenuItem("Save Document");
 		addSection = new JMenuItem("Add Section");
 		deleteSection = new JMenuItem("Delete Section");
 		splitSection = new JMenuItem("Split Section");
 		mergeSection = new JMenuItem("Merge Section");
 		about = new JMenuItem("About CoLab");
 
 		file.setMnemonic(KeyEvent.VK_F);
 		connect.setMnemonic(KeyEvent.VK_C);
 		doc.setMnemonic(KeyEvent.VK_D);
 		view.setMnemonic(KeyEvent.VK_V);
 		help.setMnemonic(KeyEvent.VK_H);
 		logIn.setMnemonic(KeyEvent.VK_L);
 		joinCoLabRoom.setMnemonic(KeyEvent.VK_J);
 		disconnect.setMnemonic(KeyEvent.VK_D);
 		exitCoLab.setMnemonic(KeyEvent.VK_X);
 		newDocument.setMnemonic(KeyEvent.VK_N);
 		openDocument.setMnemonic(KeyEvent.VK_O);
 		removeDocument.setMnemonic(KeyEvent.VK_R);
 		saveDocument.setMnemonic(KeyEvent.VK_S);
 		addSection.setMnemonic(KeyEvent.VK_A);
 		deleteSection.setMnemonic(KeyEvent.VK_D);
 		splitSection.setMnemonic(KeyEvent.VK_S);
 		mergeSection.setMnemonic(KeyEvent.VK_M);
 		showRoomMembers.setMnemonic(KeyEvent.VK_R);
 		showChat.setMnemonic(KeyEvent.VK_C);
 		about.setMnemonic(KeyEvent.VK_A);
 
 		file.add(connect);
 		connect.add(logIn);
 		connect.add(joinCoLabRoom);
 		file.add(disconnect);
 		file.addSeparator();
 		file.add(exitCoLab);
 		doc.add(newDocument);
 		doc.add(openDocument);
 		doc.add(removeDocument);
 		doc.add(saveDocument);
 		doc.addSeparator();
 		doc.add(addSection);
 		doc.add(deleteSection);
 		doc.add(splitSection);
 		doc.add(mergeSection);
 		view.add(showChat);
 		view.add(showRoomMembers);
 		help.add(about);
 
 		setMenusForUserDisconnected();
 		showChat.setSelected(true);
 		showRoomMembers.setSelected(true);
 
 		mainMenu.add(file);
 		mainMenu.add(doc);
 		mainMenu.add(view);
 		mainMenu.add(help);
 		return mainMenu;
 	}
 
 	/**
 	 * Sets listeners for the menu items.
 	 */
 	private void setListeners() {
 		// FILE menu items
 		// CONNECT menu items
 		logIn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Random r = new Random();
 				String username = r.nextInt(1000) + "";
 				/*
 				 * proxy = ConnectionFactory.getLocalInstance().connect("", 0, WindowClient.this,
 				 * username); setUserName(username);
 				 */
 				proxy = WindowLogIn.showLoginWindow(WindowClient.this, WindowClient.this);
 				if (proxy != null) {
 					colabRoomFrame = new WindowJoinCoLab(WindowClient.this, proxy.getServer());
 					setMenusForUserLoggedIn();
 				}
 			}
 		});
 		joinCoLabRoom.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int result = colabRoomFrame.showRoomDialogue();
 				if (result == WindowJoinCoLab.ROOM_JOINED) {
 					setMenusForUserJoinedRoom();
 				}
 				if (getPrivLevel() == CoLabPrivilegeLevel.OBSERVER) {
 					setMenusForUserObserver();
 				}
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
 				WindowClient.this.processWindowEvent(new WindowEvent(WindowClient.this,
 						WindowEvent.WINDOW_CLOSING));
 			}
 		});
 
 		// DOCUMENT menu items
 		newDocument.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (!JDocTabPanel.hasPermission(WindowClient.this))
 					return;
 				String docName = JOptionPane.showInputDialog(WindowClient.this,
 						"Enter the name of the document:");
 				if (docName == null)
 					return;
 				String secName = JOptionPane.showInputDialog(WindowClient.this,
 						"Enter the name of the subsection:");
 				if (docName == null || secName == null)
 					return;
 				proxy.getServer().newDocument(userName, roomName, docName);
 				DocumentSubSection section = new DocumentSubSectionImpl(secName);
 				section.setLocked(true, userName);
 				proxy.getServer().newSubSection(userName, roomName, docName, secName, 0);
 				proxy.getServer().subSectionLocked(userName, roomName, docName, secName);
 				System.out.println("WindowClient New Blank Document: Username: " + userName
 						+ " DocumentName: " + docName + " SectionName: " + secName
 						+ " LockHolder: " + section.lockedByUser());
 				proxy.getServer().subSectionUpdated(userName, roomName, docName, secName, section);
 				if (getPrivLevel() != CoLabPrivilegeLevel.OBSERVER) {
 					setMenusForRoomWithDocumentsOpen();
 				} else {
 					setMenusForUserObserver();
 				}
 			}
 		});
 
 		openDocument.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (!JDocTabPanel.hasPermission(WindowClient.this))
 					return;
 				JFileChooser choose = new JFileChooser();
 				int choice = choose.showOpenDialog(WindowClient.this);
 				if (choice != JFileChooser.APPROVE_OPTION)
 					return;
 				String docName = JOptionPane.showInputDialog(WindowClient.this,
 						"Enter the name of the document:");
 				String secName = JOptionPane.showInputDialog(WindowClient.this,
 						"Enter the name of the subsection:");
 				if (docName == null || secName == null)
 					return;
 				File choiceF = choose.getSelectedFile();
 				proxy.getServer().newDocument(userName, roomName, docName);
 				DocumentSubSection section = new DocumentSubSectionImpl(secName);
 				section.setLocked(true, userName);
 				proxy.getServer().newSubSection(userName, roomName, docName, secName, 0);
 				proxy.getServer().subSectionLocked(userName, roomName, docName, secName);
 				try {
 					section.setText(userName, new Scanner(choiceF).useDelimiter("//Z").next());
 				} catch (FileNotFoundException e1) {
 					if (Util.DEBUG)
 						e1.printStackTrace();
 					JOptionPane.showMessageDialog(null, "Shit, file could not be opened!!!!");
 					return;
 				}
 				System.out.println("WindowClient Upload Document: Username: " + userName
 						+ " DocumentName: " + docName + " SectionName: " + secName
 						+ " LockHolder: " + section.lockedByUser());
 				proxy.getServer().subSectionUpdated(userName, roomName, docName, secName, section);
 				if (getPrivLevel() != CoLabPrivilegeLevel.OBSERVER) {
 					setMenusForRoomWithDocumentsOpen();
 				} else {
 					setMenusForUserObserver();
 				}
 			}
 		});
 		removeDocument.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (!JDocTabPanel.hasPermission(WindowClient.this))
 					return;
 				SectionizedDocument doc = ((JDocTabPanel) tabbedDocumentPane.getSelectedComponent())
 						.getSectionizedDocument();
 				proxy.getServer().documentRemoved(userName, roomName, doc.getName());
 
 				if (documentTabs.size() == 0) {
 					if (getPrivLevel() == CoLabPrivilegeLevel.OBSERVER) {
 						setMenusForUserObserver();
 					} else {
 						setMenusForUserJoinedRoom();
 					}
 				}
 			}
 		});
 		saveDocument.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser choose = new JFileChooser();
 				int choice = choose.showSaveDialog(WindowClient.this);
 				if (choice != JFileChooser.APPROVE_OPTION)
 					return;
 				File f = choose.getSelectedFile();
 				PrintStream out = null;
 				try {
 					out = new PrintStream(f);
 				} catch (FileNotFoundException e1) {
 					if (Util.DEBUG)
 						e1.printStackTrace();
 					JOptionPane.showMessageDialog(WindowClient.this, "Oops, error",
 							"The file could not be saved.  Try again maybe??",
 							JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				SectionizedDocument selectedTab = ((JDocTabPanel) tabbedDocumentPane
 						.getSelectedComponent()).getSectionizedDocument();
 				String docName = selectedTab.getName();
 				out.println(documentTabs.get(docName).getSectionizedDocument().getFullText());
 			}
 		});
 		addSection.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (!JDocTabPanel.hasPermission(WindowClient.this))
 					return;
 				String name = JOptionPane.showInputDialog(WindowClient.this,
 						"Enter a name for the new SubSection");
 				if (name == null)
 					return;
 				JDocTabPanel selectedTab = (JDocTabPanel) tabbedDocumentPane.getSelectedComponent();
 				selectedTab.newSubSection(name);
 			}
 		});
 		deleteSection.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (!JDocTabPanel.hasPermission(WindowClient.this))
 					return;
 				JDocTabPanel selectedTab = (JDocTabPanel) tabbedDocumentPane.getSelectedComponent();
 				selectedTab.deleteSubSection();
 			}
 		});
 		splitSection.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (!JDocTabPanel.hasPermission(WindowClient.this))
 					return;
 				JDocTabPanel selectedTab = (JDocTabPanel) tabbedDocumentPane.getSelectedComponent();
 				selectedTab.splitSubSection();
 			}
 		});
 		mergeSection.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (!JDocTabPanel.hasPermission(WindowClient.this))
 					return;
 				JDocTabPanel selectedTab = (JDocTabPanel) tabbedDocumentPane.getSelectedComponent();
 				selectedTab.mergeSubSection();
 			}
 		});
 
 		// VIEW menu items
 		showRoomMembers.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				roomMemberListPanel.setVisible(showRoomMembers.isSelected());
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
 				// TODO make info window
 
 			}
 		});
 
 		this.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				if (disconnect.isEnabled()) {
 					proxy.getServer().leaveCoLabRoom(userName, roomName);
 					proxy.getServer().logOut(userName);
 					try {
 						proxy.close();
 					} catch (IOException io) {
 						io.printStackTrace();
 					}
 				}
 				chatPanel.shutdownTray();
 			}
 		});
 	}
 
 	/**
 	 * Set menu items enabled/disabled for when user is logged in.
 	 */
 	private void setMenusForUserLoggedIn() {
 		logIn.setEnabled(false);
 		joinCoLabRoom.setEnabled(true);
 		disconnect.setEnabled(true);
 		newDocument.setEnabled(false);
 		openDocument.setEnabled(false);
 		removeDocument.setEnabled(false);
 		saveDocument.setEnabled(false);
 		addSection.setEnabled(false);
 		deleteSection.setEnabled(false);
 		splitSection.setEnabled(false);
 		mergeSection.setEnabled(false);
 		this.roomMemberListPanel.setUser(userName);
 		if (userName != null)
 			setTitle("CoLab - " + getUserName());
 	}
 
 	/**
 	 * Set menu items enabled/disabled for an observer user
 	 */
 	private void setMenusForUserObserver() {
 		logIn.setEnabled(false);
 		joinCoLabRoom.setEnabled(false);
 		disconnect.setEnabled(true);
 		newDocument.setEnabled(false);
 		openDocument.setEnabled(false);
 		removeDocument.setEnabled(false);
 		if (documentTabs.size() == 0) {
 			saveDocument.setEnabled(false);
 		} else {
 			saveDocument.setEnabled(true);
 		}
 		addSection.setEnabled(false);
 		deleteSection.setEnabled(false);
 		splitSection.setEnabled(false);
 		mergeSection.setEnabled(false);
 		this.roomMemberListPanel.setRoom(roomName);
 		this.roomMemberListPanel.setServer(proxy.getServer());
 		if (roomName != null)
 			setTitle("CoLab - " + getUserName() + " - " + getRoomName());
 	}
 
 	/**
 	 * Set menu items enabled/disabled for when user has joined a CoLab Room or when all documents
 	 * are removed from CoLab Room.
 	 */
 	private void setMenusForUserJoinedRoom() {
 		logIn.setEnabled(false);
 		joinCoLabRoom.setEnabled(false);
 		disconnect.setEnabled(true);
 		newDocument.setEnabled(true);
 		openDocument.setEnabled(true);
 		removeDocument.setEnabled(false);
 		saveDocument.setEnabled(false);
 		addSection.setEnabled(false);
 		deleteSection.setEnabled(false);
 		splitSection.setEnabled(false);
 		mergeSection.setEnabled(false);
 		this.roomMemberListPanel.setRoom(roomName);
 		this.roomMemberListPanel.setServer(proxy.getServer());
 		if (roomName != null)
 			setTitle("CoLab - " + getUserName() + " - " + getRoomName());
 	}
 
 	/**
 	 * Set menu items enabled/disabled for when documents are open in a CoLab Room.
 	 */
 	private void setMenusForRoomWithDocumentsOpen() {
 		logIn.setEnabled(false);
 		joinCoLabRoom.setEnabled(false);
 		disconnect.setEnabled(true);
 		newDocument.setEnabled(true);
 		openDocument.setEnabled(true);
 		removeDocument.setEnabled(true);
 		saveDocument.setEnabled(true);
 		addSection.setEnabled(true);
 		deleteSection.setEnabled(true);
 		splitSection.setEnabled(true);
 		mergeSection.setEnabled(true);
 		if (roomName != null)
 			setTitle("CoLab - " + getUserName() + " - " + getRoomName());
 	}
 
 	/**
 	 * Set menu items enabled/disabled for when user is disconnected.
 	 */
 	private void setMenusForUserDisconnected() {
 		logIn.setEnabled(true);
 		joinCoLabRoom.setEnabled(false);
 		disconnect.setEnabled(false);
 		newDocument.setEnabled(false);
 		openDocument.setEnabled(false);
 		removeDocument.setEnabled(false);
 		saveDocument.setEnabled(false);
 		addSection.setEnabled(false);
 		deleteSection.setEnabled(false);
 		splitSection.setEnabled(false);
 		mergeSection.setEnabled(false);
 		if (userName != null)
 			setTitle("CoLab - " + getUserName());
 	}
 
 	/**
 	 * Returns the privilege level of the user signed into this client.
 	 * 
 	 * @return the privilege level of the user signed into this client.
 	 */
 	public CoLabPrivilegeLevel getPrivLevel() {
 		return roomMemberListPanel.getMember(userName).getPriv();
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
 	public boolean allUsersInRoom(List<String> usernames, List<CoLabPrivilegeLevel> privs) {
 		roomMemberListPanel.updateList(usernames, privs);
 		return true;
 	}
 
 	@Override
 	public boolean changeUserPrivilege(String username, CoLabPrivilegeLevel newPriv) {
 		if (username.equals(this.userName)) {
 			if (newPriv == CoLabPrivilegeLevel.OBSERVER) {
 				setMenusForUserObserver();
 			} else {
 				if (documentTabs.size() == 0) {
 					// Sets menus enabled for user in room with no documents
 					setMenusForUserJoinedRoom();
 				} else {
 					setMenusForRoomWithDocumentsOpen();
 				}
 			}
 		}
 		return this.roomMemberListPanel.setUserPrivledge(username, newPriv);
 	}
 
 	@Override
 	public boolean coLabRoomMemberArrived(String username) {
 		chatPanel.newChatMessage("Server", "<New Chat Member '" + username + "'>");
 		boolean add = roomMemberListPanel.addUser(username);
 		if (username.equals(this.userName) && add) {
 			setMenusForUserObserver();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean coLabRoomMemberLeft(String username) {
 		chatPanel.newChatMessage("Server", "<Chat Member Left '" + username + "'>");
 		if(username.equals(getUserName())) {
 			setMenusForUserDisconnected();
 			chatPanel.clearChatPanel();
 			tabbedDocumentPane.removeAll();
 			roomMemberListPanel.clearList();
 		}
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
 	public boolean newSubSection(final String username, final String documentName,
 			String sectionID, final DocumentSubSection section, final int idx) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				documentTabs.get(documentName).subSectionCreated(section, idx);
 
 			}
 		});
 		return true;
 	}
 
 	@Override
 	public boolean newDocument(String username, final String documentName) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				JDocTabPanel doc = documentTabs.get(documentName);
 				if (doc != null) {
 					throw new IllegalStateException("Two documents cannot have the same name");
 				}
 				doc = new JDocTabPanel(new DocumentInfoImpl(proxy.getServer(), roomName,
 						documentName, userName), WindowClient.this);
 				documentTabs.put(documentName, doc);
 				tabbedDocumentPane.add(documentName, doc);
 				// TODO keep update? documents.get(documentName).updateDocumentView();
 				if (getPrivLevel() != CoLabPrivilegeLevel.OBSERVER) {
 					setMenusForRoomWithDocumentsOpen();
 				} else {
 					setMenusForUserObserver();
 				}
 			}
 		});
 		return true;
 
 	}
 
 	@Override
 	public boolean removeDocument(final String username, final String documentName) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				JDocTabPanel doc = documentTabs.get(documentName);
 				if (doc == null) {
 					throw new IllegalStateException("This document does not exist");
 				}
 				documentTabs.remove(documentName);
 				System.out.println("Document Removed from client " + username);
 				tabbedDocumentPane.remove(doc);
 				if (documentTabs.size() == 0) {
 					if (getPrivLevel() == CoLabPrivilegeLevel.OBSERVER) {
 						setMenusForUserObserver();
 					} else {
 						setMenusForUserJoinedRoom();
 					}
 				}
 			}
 		});
 		return true;
 	}
 
 	@Override
 	public boolean subsectionLocked(final String usernameSender, final String documentName,
 			final String sectionId) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				documentTabs.get(documentName).subSectionLocked(sectionId, usernameSender);
 			}
 		});
 		return true;
 	}
 
 	@Override
 	public boolean subsectionUnLocked(final String usernameSender, final String documentName,
 			final String sectionId) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				documentTabs.get(documentName).subSectionUnlocked(sectionId, usernameSender);
 			}
 		});
 		return true;
 	}
 
 	@Override
 	public boolean subsectionFlopped(final String usernameSender, final String documentName,
 			final String sectionIDMoveUp, final String sectionIDMoveDown) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				documentTabs.get(documentName)
 						.subSectionFlopped(sectionIDMoveUp, sectionIDMoveDown);
 			}
 		});
 		return true;
 	}
 
 	@Override
 	public boolean subSectionRemoved(String username, final String sectionId,
 			final String documentName) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				documentTabs.get(documentName).subSectionRemoved(sectionId);
 			}
 		});
 		return true;
 	}
 
 	@Override
 	public boolean updateAllSubsections(final String documentId,
 			final List<DocumentSubSection> allSections) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				documentTabs.get(documentId).subSectionsRefreshed(allSections);
 			}
 		});
 		return true;
 	}
 
 	@Override
 	public boolean updateSubsection(final String usernameSender, final String documentname,
 			final DocumentSubSection section, final String sectionID) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				documentTabs.get(documentname)
 						.subSectionUpdated(sectionID, usernameSender, section);
 
 			}
 		});
 		return true;
 	}
 
 	@Override
 	public boolean subSectionSplit(final String username, final String documentName,
 			final String oldSecName, final String newName1, final String newName2, final int idx) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				documentTabs.get(documentName).subSectionSplit(oldSecName, newName1, newName2, idx,
 						username);
 			}
 		});
 		return true;
 	}
 
 	@Override
 	public boolean subSectionCombined(String username, final String documentName,
 			final String sectionA, final String sectionB, final String newSection) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				documentTabs.get(documentName).subSectionMerged(sectionA, sectionB, newSection);
 			}
 		});
 		return true;
 	}
 
 	@Override
 	public String getUserName() {
 		return userName;
 	}
 
 	@Override
 	public void setUserName(String un) {
 		userName = un;
 	}
 
 	/**
 	 * Returns the name of the room this user is in.
 	 * 
 	 * @return the name of the room this user is in
 	 */
 	public String getRoomName() {
 		return roomName;
 	}
 
 	/**
 	 * Sets the name of the room this user has joined.
 	 * 
 	 * @param rn -
 	 *            room name
 	 */
 	public void setRoomName(String rn) {
 		roomName = rn;
 	}
 
 	public void chatLogin() {
 		chatPanel.connect(proxy.getServer(), userName, roomName);
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
