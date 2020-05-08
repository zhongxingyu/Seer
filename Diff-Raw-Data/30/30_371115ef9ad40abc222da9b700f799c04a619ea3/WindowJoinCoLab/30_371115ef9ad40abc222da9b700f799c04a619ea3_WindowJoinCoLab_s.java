 package edu.cs319.client;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FocusTraversalPolicy;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
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
  * 
  * @author Amelia Gee
  * @author Justin Nelson
  * 
  */
 public class WindowJoinCoLab extends JDialog {
 
 	public static final int NO_ROOM = 32142;
 	public static final int ROOM_JOINED = -234;
 	private JList roomList;
 	private JTextField createField = new JTextField();
 	private JButton joinButton = new JButton("Join");
 	private JButton createButton = new JButton("Create");
 	private JButton cancelButton = new JButton("Cancel");
 	private JButton refreshButton = new JButton("Refresh");
 	private JLabel refreshTimeStamp = new JLabel("Click Refresh for a List of Available Rooms");
 
 	private WindowClient parent;
 	private IServer server;
 
 	private int closeStatus;
 
 	public WindowJoinCoLab(WindowClient parent, IServer server) {
 		super(parent, "Join a CoLab Room");
 		setLocation(parent.getLocation());
 		setModal(true);
 		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		if (Util.DEBUG){
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
 		this.setSize(500, 400);
 		this.setMinimumSize(new Dimension(500, 400));
 		setUpAppearance();
 		setUpListeners();
 		fillCoLabRoomList();
 		this.repaint();
 	}
 
 	private void setUpAppearance() {
 		Insets borderInsets = new Insets(0, 0, 20, 0);
 		roomList = new JList();
 		roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		roomList.setSelectedIndex(0);
 		roomList.setVisibleRowCount(8);
 		JScrollPane listScrollPane = new JScrollPane(roomList);
 
 		JLabel listLabel = new JLabel("Existing CoLab Rooms:");
		JLabel createLabel = new JLabel("Create a New CoLab:");
 		JLabel cancelLabel = new JLabel("Join a CoLab Room at another time:");
 		createField.setPreferredSize(new Dimension(200, 25));

 		refreshButton.setSize(100, 25);
 
 		JPanel listLabelPanel = new JPanel(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		Insets labelInsets = new Insets(0, 0, 10, 0);
 		c.gridx = 0;
 		c.gridy = 0;
 		c.insets = labelInsets;
 		c.anchor = GridBagConstraints.LINE_START;
 		listLabelPanel.add(listLabel, c);
 		c.gridy = 1;
 		listLabelPanel.add(refreshTimeStamp, c);
 		c.gridy = 2;
 		c.anchor = GridBagConstraints.CENTER;
 		listLabelPanel.add(refreshButton, c);
 
 		JPanel westPanel = new JPanel(new BorderLayout(10, 10));
 		westPanel.add(listLabelPanel, BorderLayout.NORTH);
 		westPanel.add(createLabel, BorderLayout.SOUTH);
 		JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
 		centerPanel.add(listScrollPane, BorderLayout.CENTER);
 		centerPanel.add(createField, BorderLayout.SOUTH);
 		JPanel eastPanel = new JPanel(new BorderLayout(10, 10));
 		eastPanel.add(joinButton, BorderLayout.NORTH);
 		eastPanel.add(createButton, BorderLayout.SOUTH);
 		JPanel southPanel = new JPanel(new BorderLayout(10, 10));
 		southPanel.add(cancelLabel, BorderLayout.WEST);
 		southPanel.add(cancelButton, BorderLayout.EAST);
 
 		JLabel topLabel = new JLabel("Please choose an "
 				+ "existing CoLab to join or create a new CoLab.");
 		topLabel.setBorder(new EmptyBorder(borderInsets));
 		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
 		mainPanel.add(topLabel, BorderLayout.NORTH);
 		mainPanel.add(westPanel, BorderLayout.WEST);
 		mainPanel.add(centerPanel, BorderLayout.CENTER);
 		mainPanel.add(eastPanel, BorderLayout.EAST);
 		mainPanel.add(southPanel, BorderLayout.SOUTH);
 		mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
 		this.add(mainPanel);
 		
 		Vector<Component> order = new Vector<Component>(6);
 		order.add(createField);
 		order.add(createButton);
 		order.add(cancelButton);
 		order.add(refreshButton);
 		order.add(roomList);
 		order.add(joinButton);
 		setFocusTraversalPolicy(new JoinCoLabFocusTraversalPolicy(order));
 		//fillCoLabRoomList();
 	}
 
 	public int showRoomDialogue() {
 		setVisible(true);
 		return closeStatus;
 	}
 
 	public void roomsUpdated(Collection<String> roomNames) {
 		roomList.setListData(roomNames.toArray());
 		Calendar c = GregorianCalendar.getInstance();
 		refreshTimeStamp.setText(c.getTime().toString());
 	}
 
 	private void fillCoLabRoomList() {
 		// ask for all room members
 		server.getAllCoLabRoomNames(parent.getUserName());
 	}
 
 	private void setUpListeners() {
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
 
 		refreshButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				fillCoLabRoomList();
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
 	
 	public static class JoinCoLabFocusTraversalPolicy extends FocusTraversalPolicy {
 		
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
