 package edu.cs319.client;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FocusTraversalPolicy;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.GregorianCalendar;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.EmptyBorder;
 
 import edu.cs319.server.IServer;
 import edu.cs319.util.Util;
 
 /**
  * The window where the user chooses to create their own CoLab Room or to join an already existing
  * CoLab Room.
  * 
  * @author Amelia Gee
  * @author Justin Nelson
  * 
  */
 public class WindowJoinCoLab extends JDialog {
 
 	public static final int NO_ROOM = 32142;
 	public static final int ROOM_JOINED = -234;
 	private JList roomList;
 	private JList persistingRoomList;
 	private JTextField createField = new JTextField();
 	private JButton joinButton = new JButton("Join");
 	private JButton createButton = new JButton("Create");
 	private JButton cancelButton = new JButton("Cancel");
 	private JButton refreshRoomsButton = new JButton("Refresh");
 	private JButton openButton = new JButton("Open");
 	private JButton refreshPersistingRoomsButton = new JButton("Refresh");
 	private JLabel refreshRoomsTimeStamp = new JLabel("Click Refresh for a List of Available Rooms");
 	private JLabel refreshPersistingRoomsTimeStamp = new JLabel("Click Refresh for a List of Persisting Rooms");
 
 	private WindowClient parent;
 	private IServer server;
 
 	private int closeStatus;
 
 	public WindowJoinCoLab(WindowClient parent, IServer server) {
 		super(parent, "Join a CoLab Room");
 		setLocation(new Point(parent.getLocation().x + 50, parent.getLocation().y + 50));
 		setModal(true);
 		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		if (Util.DEBUG) {
 			createField.setText("New Room");
 		}
 		if (server == null) {
 			if (Util.DEBUG) {
 				System.out.println("Null server passed into a join colab room frame");
 			}
 			throw new IllegalArgumentException("The server must not be null");
 		}
 		this.parent = parent;
 		this.server = server;
 		this.setSize(800, 520);
 		this.setResizable(false);
 		setUpAppearance();
 		setUpListeners();
 		fillCoLabRoomList();
 		fillPersistingRoomList();
 		this.repaint();
 	}
 
 	/**
 	 * Sets up the appearance of the window by initializing/placing the window components.
 	 */
 	private void setUpAppearance() {
 		roomList = new JList();
 		roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		roomList.setSelectedIndex(0);
 		roomList.setVisibleRowCount(8);
 		JScrollPane roomListScroll = new JScrollPane(roomList);
 		
 		persistingRoomList = new JList();
 		persistingRoomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		persistingRoomList.setSelectedIndex(0);
 		persistingRoomList.setVisibleRowCount(4);
 		JScrollPane persistingRoomListScroll = new JScrollPane(persistingRoomList);
 
 		JLabel listLabel = new JLabel("Existing CoLab Rooms:");
 		JLabel createLabel = new JLabel("Make a New CoLab Room:");
 		JLabel openLabel = new JLabel("Open a persisting CoLab Room:");
 		JLabel cancelLabel = new JLabel("Join a CoLab Room at another time:");
 
 		createField.setPreferredSize(new Dimension(260, 25));
 		
 		Dimension buttonSize = new Dimension(80, 25);
 		joinButton.setPreferredSize(buttonSize);
 		createButton.setPreferredSize(buttonSize);
 		cancelButton.setPreferredSize(buttonSize);
 		openButton.setPreferredSize(buttonSize);
 		refreshRoomsButton.setPreferredSize(buttonSize);
 
 		joinButton.setMnemonic(KeyEvent.VK_J);
 		createButton.setMnemonic(KeyEvent.VK_C);
 		cancelButton.setMnemonic(KeyEvent.VK_N);
 		openButton.setMnemonic(KeyEvent.VK_O);
 		refreshRoomsButton.setMnemonic(KeyEvent.VK_R);
 
 		JPanel listLabelPanel = new JPanel(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		Insets labelInsets = new Insets(0, 0, 10, 0);
 		c.gridx = 0;
 		c.gridy = 0;
 		c.insets = labelInsets;
 		c.anchor = GridBagConstraints.LINE_START;
 		listLabelPanel.add(listLabel, c);
 		c.gridy = 1;
 		listLabelPanel.add(refreshRoomsTimeStamp, c);
 		c.gridy = 2;
 		c.anchor = GridBagConstraints.CENTER;
 		listLabelPanel.add(refreshRoomsButton, c);
 		
 		JPanel persistingListLabelPanel = new JPanel(new GridBagLayout());
 		c.gridx = 0;
 		c.gridy = 0;
 		c.anchor = GridBagConstraints.LINE_START;
 		persistingListLabelPanel.add(openLabel, c);
 		c.gridy = 1;
 		persistingListLabelPanel.add(refreshPersistingRoomsTimeStamp, c);
 		c.gridy = 2;
 		c.anchor = GridBagConstraints.CENTER;
 		persistingListLabelPanel.add(refreshPersistingRoomsButton, c);
 		
 
 		JPanel stuffPanel = new JPanel(new GridBagLayout());
 		stuffPanel.setBorder(new EmptyBorder(5, 5, 15, 5));
 		Insets gridInsets = new Insets(5, 10, 5, 10);
 		c.gridx = 0;
 		c.gridy = 0;
 		c.insets = gridInsets;
 		c.anchor = GridBagConstraints.LINE_START;
 		stuffPanel.add(createLabel, c);
 		c.gridx = 1;
 		stuffPanel.add(createField, c);
 		c.gridx = 2;
 		stuffPanel.add(createButton, c);
 		c.gridx = 0;
 		c.gridy = 1;
 		stuffPanel.add(listLabelPanel, c);
 		c.gridx = 1;
 		stuffPanel.add(roomListScroll, c);
 		c.gridx = 2;
 		stuffPanel.add(joinButton, c);
 		c.gridx = 0;
 		c.gridy = 2;
 		stuffPanel.add(persistingListLabelPanel, c);
 		c.gridx = 1;
 		stuffPanel.add(persistingRoomListScroll, c);
 		c.gridx = 2;
 		stuffPanel.add(openButton, c);
 		c.gridx = 0;
 		c.gridy = 3;
 		c.gridwidth = 2;
 		stuffPanel.add(cancelLabel, c);
 		c.gridx = 2;
 		c.gridwidth = 1;
 		stuffPanel.add(cancelButton, c);
 		
 		JLabel topLabel = new JLabel("Please create a new CoLab Room,");
 		JLabel middleLabel = new JLabel("choose an existing CoLab Room to join, or");
 		JLabel bottomLabel = new JLabel("choose a CoLab Room to reopen.");
 		Insets borderInsets = new Insets(10, 10, 0, 10);
 		JPanel labelPanel = new JPanel(new GridBagLayout());
 		c.gridx = 0;
 		c.gridy = 0;
 		c.insets = borderInsets;
 		labelPanel.add(topLabel, c);
 		c.gridy = 1;
 		c.anchor = GridBagConstraints.CENTER;
 		labelPanel.add(middleLabel, c);
 		c.gridy = 2;
 		c.anchor = GridBagConstraints.LINE_END;
 		labelPanel.add(bottomLabel, c);
 		
 		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));;
 		mainPanel.add(labelPanel, BorderLayout.WEST);
 		mainPanel.add(stuffPanel, BorderLayout.SOUTH);
 		this.add(mainPanel);
 
 		Vector<Component> order = new Vector<Component>(8);
 		order.add(createField);
 		order.add(createButton);
 		order.add(refreshRoomsButton);
 		order.add(roomList);
 		order.add(joinButton);
 		order.add(persistingRoomList);
 		order.add(openButton);
 		order.add(cancelButton);
 		setFocusTraversalPolicy(new JoinCoLabFocusTraversalPolicy(order));
 	}
 
 	public int showRoomDialogue() {
 		setVisible(true);
 		return closeStatus;
 	}
 
 	public void roomsUpdated(Collection<String> roomNames) {
 		roomList.setListData(roomNames.toArray());
 		Calendar c = GregorianCalendar.getInstance();
 		refreshRoomsTimeStamp.setText(c.getTime().toString());
 	}
 	
 	public void persistingRoomsUpdated(Collection<String> roomNames) {
 		persistingRoomList.setListData(roomNames.toArray());
 		Calendar c = GregorianCalendar.getInstance();
 		refreshPersistingRoomsTimeStamp.setText(c.getTime().toString());
 	}
 
 	private void fillCoLabRoomList() {
 		// ask for all room members
 		server.getAllCoLabRoomNames(parent.getUserName());
 	}
 	
 	private void fillPersistingRoomList() {
 		// ask for all persisting rooms for this user
 		server.getAllRoomsPersisted(parent.getUserName());
 	}
 
 	/**
 	 * Sets up window component listeners.
 	 */
 	private void setUpListeners() {
 		createButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (createField.getText().length() == 0)
 					return;
 				if (server.addNewCoLabRoom(parent.getUserName(), createField.getText(), null)) {
 					parent.setRoomName(createField.getText());
 					parent.chatLogin();
 					server.getClientsCurrentlyInRoom(parent.getUserName(), createField.getText());
 					closeStatus = ROOM_JOINED;
 					dispose();
 				}
 			}
 		});
 
 		refreshRoomsButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				fillCoLabRoomList();
 			}
 		});
 
 		joinButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				String joinName = (String) roomList.getSelectedValue();
 				server.joinCoLabRoom(parent.getUserName(), joinName, null);
 				parent.setRoomName(joinName);
 				parent.chatLogin();
 				server.getClientsCurrentlyInRoom(parent.getUserName(), joinName);
 				closeStatus = ROOM_JOINED;
 				dispose();
 			}
 		});
 		
 		openButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				String joinName = (String) persistingRoomList.getSelectedValue();
 				server.joinCoLabRoom(parent.getUserName(), joinName, null);
 				parent.setRoomName(joinName);
 				parent.chatLogin();
 				server.getClientsCurrentlyInRoom(parent.getUserName(), joinName);
 				closeStatus = ROOM_JOINED;
 				dispose();
 			}
 		});
 		
 		refreshPersistingRoomsButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				fillPersistingRoomList();
 			}
 		});
 
 		cancelButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				closeStatus = NO_ROOM;
 				dispose();
 			}
 		});
 	}
 
 	/**
 	 * The FocusTraversalPolicy for the WindowJoinCoLab class.
 	 * 
 	 * @author Amelia Gee
 	 * 
 	 */
 	private static class JoinCoLabFocusTraversalPolicy extends FocusTraversalPolicy {
 
 		Vector<Component> order;
 
 		public JoinCoLabFocusTraversalPolicy(Vector<Component> ord) {
 			order = new Vector<Component>(ord.size());
 			order.addAll(ord);
 		}
 
 		@Override
 		public Component getComponentAfter(Container arg0, Component arg1) {
 			int idx = (order.indexOf(arg1) + 1) % order.size();
 			return order.get(idx);
 		}
 
 		@Override
 		public Component getComponentBefore(Container arg0, Component arg1) {
 			int idx = (order.indexOf(arg1) - 1) % order.size();
 			return order.get(idx);
 		}
 
 		@Override
 		public Component getDefaultComponent(Container arg0) {
 			return order.get(0);
 		}
 
 		@Override
 		public Component getFirstComponent(Container arg0) {
 			return order.get(0);
 		}
 
 		@Override
 		public Component getLastComponent(Container arg0) {
 			return order.lastElement();
 		}
 
 	}
 }
