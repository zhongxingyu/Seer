 package kimononet.simulation;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 
 import javax.imageio.ImageIO;
 import javax.swing.DefaultListModel;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.Timer;
 
 import java.awt.EventQueue;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.Font;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import kimononet.geo.GeoLocation;
 import kimononet.geo.GeoVelocity;
 import kimononet.geo.GeoMap;
 import kimononet.geo.RandomWaypointGeoDevice;
 import kimononet.peer.Peer;
 import kimononet.peer.PeerAddress;
 import kimononet.peer.PeerAddressException;
 import kimononet.peer.PeerAgent;
 import kimononet.peer.PeerEnvironment;
 import kimononet.time.SystemTimeProvider;
 
 import javax.swing.JList;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.border.BevelBorder;
 import javax.swing.SwingConstants;
 import java.awt.Color;
 
 public class Simulation {
 
 	private final String[] properties = {	"Name",			// Row 0
 											"Address",		// Row 1
 											"Longitude",	// Row 2
 											"Latitude",		// Row 3
 											"Accuracy",		// Row 4
 											"Speed",		// Row 5
 											"Bearing"};		// Row 6
 
 	private ArrayList<PeerAgent> arrayListPeerAgents = new ArrayList<PeerAgent>();
 	private boolean bSimRunning = false;
	private DefaultListModel<String> listModelPeers;
 	private DefaultTableModel tableModelPeerProps;
 	private DefaultTableModel tableModelPeerEnvProps;
 	private JButton btnStartStop, btnAddPeer, btnDeletePeer, btnApplyPeerProps, btnApplyEnvProps;
 	private JFrame frame; 
 	private JLabel lblSimStatusDisplay;
	private JList<String> listPeers;
 	private JMenuItem mntmStartStopSim, mntmEditMapDim;
 	private JTable tablePeerProps;
 	private JTable tablePeerEnvProps;
 	private GeoMap mapDim = new GeoMap(	new GeoLocation(-0.25, 0.25, 0f),	// Upper left
 										new GeoLocation(0.25, -0.25, 0f));	// Lower right
 	private PeerEnvironment peerEnv = new PeerEnvironment();
 	private Timer timer;
 	private int timerRefreshRate = 1;	// in ms
 
 	private void refresh() {
 		if (tableModelPeerProps != null)
 			tableModelPeerProps.getDataVector().removeAllElements();
 
 		if (listModelPeers != null && getCurrentPeerIndex() >= 0 && getCurrentPeer() != null) {
 			// Refresh peer list item display.
 			listModelPeers.set(getCurrentPeerIndex(), getCurrentPeer().getName() + " (" + getCurrentPeer().getAddress().toString() + ")");
 
 			// Refresh peer properties table display.
 			String longitude = new String();
 			String latitude = new String();
 			String accuracy = new String();
 			GeoLocation location = getCurrentPeer().getLocation();
 			if (location != null) {
 				longitude = Double.toString(location.getLongitude());
 				latitude = Double.toString(location.getLatitude());
 				accuracy = Float.toString(location.getAccuracy());
 			}
 			else {
 				longitude = "n/a";
 				latitude = "n/a";
 				accuracy = "n/a";
 			}
 			if (tableModelPeerProps != null) {
 				tableModelPeerProps.addRow(new Object[] {properties[0], getCurrentPeer().getName()});
 				tableModelPeerProps.addRow(new Object[] {properties[1], getCurrentPeer().getAddress().toString()});
 				tableModelPeerProps.addRow(new Object[] {properties[2], longitude});
 				tableModelPeerProps.addRow(new Object[] {properties[3], latitude});
 				tableModelPeerProps.addRow(new Object[] {properties[4], accuracy});
 				tableModelPeerProps.addRow(new Object[] {properties[5], Float.toString(getCurrentPeer().getVelocity().getSpeed())});
 				tableModelPeerProps.addRow(new Object[] {properties[6], Float.toString(getCurrentPeer().getVelocity().getBearing())});
 			}
 
 			if (btnDeletePeer != null)
 				btnDeletePeer.setEnabled(!bSimRunning);
 			if (btnApplyPeerProps != null)
 				btnApplyPeerProps.setEnabled(!bSimRunning);
 			if (btnApplyEnvProps != null)
 				btnApplyEnvProps.setEnabled(!bSimRunning);
 		}
 		else {
 			if (btnDeletePeer != null)
 				btnDeletePeer.setEnabled(false);
 			if (btnApplyPeerProps != null)
 				btnApplyPeerProps.setEnabled(false);
 			if (btnApplyEnvProps != null)
 				btnApplyEnvProps.setEnabled(false);
 		}
 
 		if (frame != null)
 			frame.repaint();
 	}
 
 	private void addPeer() {
 		GeoLocation location = GeoLocation.generateRandomGeoLocation(mapDim.getUpperLeft(), mapDim.getLowerRight());
 		GeoVelocity velocity = new GeoVelocity(343, (float)Math.toRadians(20));
 
 		// Create new peer with random address.
 		Peer peer = new Peer(PeerAddress.generateRandomAddress(), location, velocity);
 
 		// Create new PeerAgent to represent peer and add it to ArrayList.
 		arrayListPeerAgents.add(new PeerAgent(peer, peerEnv, new RandomWaypointGeoDevice(location, velocity, new SystemTimeProvider(), 0.001, 0)));
 
 		// Add peer to list.
 		listModelPeers.add(listPeers.getModel().getSize(), peer.getName() + " (" + peer.getAddress().toString() + ")");
 		frame.repaint();
 	}
 
 	private void deleteCurrentPeer() {
 		int i = getCurrentPeerIndex();
 		if (i < 0)
 			return;
 		listModelPeers.remove(i);
 		arrayListPeerAgents.remove(i);
 		refresh();
 	}
 
 	private void startStopSim() {
 		if (arrayListPeerAgents.isEmpty()) {
 			JOptionPane.showMessageDialog(frame, "Please add a peer node.");
 			return;
 		}
 
 		// Start/stop services of each peer.
 		for (int i = 0; i < arrayListPeerAgents.size(); i++) {
 			if (!bSimRunning)
 				arrayListPeerAgents.get(i).startServices();
 			else
 				arrayListPeerAgents.get(i).shutdownServices();
 		}
 
 		bSimRunning = !bSimRunning;
 
 		// Enable/disable buttons and stuff.
 		if (btnAddPeer != null)
 			btnAddPeer.setEnabled(!bSimRunning);
 		if (mntmStartStopSim != null)
 			mntmStartStopSim.setText(bSimRunning ? "Stop Simulation" : "Start Simulation");
 		if (btnStartStop != null)
 			btnStartStop.setText(bSimRunning ? "Stop" : "Start");
 		if (lblSimStatusDisplay != null) {
 			lblSimStatusDisplay.setText(bSimRunning ? "Simulation RUNNING" : "Simulation STOPPED");
 			lblSimStatusDisplay.setBackground(bSimRunning ? Color.GREEN : Color.RED);
 		}
 		if (mntmEditMapDim != null)
 			mntmEditMapDim.setEnabled(!bSimRunning);
 		if (tablePeerProps != null)
 			tablePeerProps.setEnabled(!bSimRunning);
 		if (tablePeerEnvProps != null)
 			tablePeerEnvProps.setEnabled(!bSimRunning);
 
 		refresh();
 
 		// Start/stop repaint timer.
 		if (bSimRunning)
 			timer.start();
 		else
 			timer.stop();
 	}
 
 	public ArrayList<PeerAgent> getPeerAgents() {
 		return arrayListPeerAgents;
 	}
 
 	public Peer getCurrentPeer() {
 		return getPeerAt(getCurrentPeerIndex());
 	}
 
 	public int getCurrentPeerIndex() {
 		if (listPeers == null)
 			return -1;
 		return listPeers.getSelectionModel().getMinSelectionIndex();	
 	}
 
 	public Peer getPeerAt(int index) {
 		if (index < 0)
 			return null;
 		return arrayListPeerAgents.get(index).getPeer();
 	}
 
 	public JFrame getFrame() {
 		return frame;
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public Simulation() {
 		initialize();
 	}
 
 	public void start() {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Simulation window = new Simulation();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					JOptionPane.showMessageDialog(null, "Error displaying main window.");
 				}
 			}
 		});		
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		// TODO: hard-coded for now !!
 		peerEnv.set("max-transmission-range", "1337");
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] {0, 0, 0, 0, 0};
 		gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gridBagLayout.columnWeights = new double[] {1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 
 		frame = new JFrame("KimonoNet Simulator");
 		frame.getContentPane().setLayout(gridBagLayout);
 		frame.setBounds(100, 100, 1024, 768);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
 
 		/*********************************************************************
 		 * Menu.
 		 *********************************************************************/
 
 		JMenuBar menuBar = new JMenuBar();
 		frame.setJMenuBar(menuBar);
 
 		JMenu mnSim = new JMenu("Simulation");
 		menuBar.add(mnSim);
 
 		mntmStartStopSim = new JMenuItem("Start Simulation");
 		mntmStartStopSim.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				startStopSim();
 			}
 		});
 		mnSim.add(mntmStartStopSim);
 
 		mntmEditMapDim = new JMenuItem("Edit Map Dimensions...");
 		mntmEditMapDim.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				try {
 					MapDimensionsDialog dialog = new MapDimensionsDialog(mapDim);
 					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 					dialog.setVisible(true);
 					refresh();
 				} catch (Exception e) {
 					JOptionPane.showMessageDialog(null, "Error displaying map dimensions dialog.");
 				}
 			}
 		});
 		mnSim.add(mntmEditMapDim);
 
 		JSeparator mnSeparator = new JSeparator();
 		mnSim.add(mnSeparator);
 
 		JMenuItem mntmExit = new JMenuItem("Exit");
 		mntmExit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				frame.dispose();
 			}
 		});
 		mnSim.add(mntmExit);
 
 		JMenu mnHelp = new JMenu("Help");
 		menuBar.add(mnHelp);
 
 		JMenuItem mntmAbout = new JMenuItem("About...");
 		mntmAbout.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				JOptionPane.showMessageDialog(frame, "KimonoNet Simulator\n\nCopyright  2012. All rights reserved.\n\nEric Bollens\nJames Hung\nZorayr Khalapyan\nWade Norris");
 			}
 		});
 		mnHelp.add(mntmAbout);
 
 		/*********************************************************************
 		 * Panel in which to draw the UAVs and stuff.
 		 *********************************************************************/
 
 		BufferedImage imageUAV = null;
 
 		try {
 			imageUAV = ImageIO.read(new File("uav.png"));
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog(frame, "Error loading image.");
 		}
 
 		SimulationPanel panel = new SimulationPanel(imageUAV, mapDim, this);
 		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.fill = GridBagConstraints.BOTH;
 		gbc_panel.gridheight = 10;
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 0;
 		gbc_panel.insets = new Insets(0, 0, 5, 5);
 
 		frame.getContentPane().add(panel, gbc_panel);
 
 		/*********************************************************************
 		 * Simulation status display (started, stopped).
 		 *********************************************************************/
 
 		lblSimStatusDisplay = new JLabel("Simulation STOPPED");
 		lblSimStatusDisplay.setBackground(Color.RED);
 		lblSimStatusDisplay.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 		lblSimStatusDisplay.setHorizontalAlignment(SwingConstants.CENTER);
 		lblSimStatusDisplay.setOpaque(true);
 
 		GridBagConstraints gbc_lblSimStatusDisplay = new GridBagConstraints();
 		gbc_lblSimStatusDisplay.fill = GridBagConstraints.HORIZONTAL;
 		gbc_lblSimStatusDisplay.gridwidth = 2;
 		gbc_lblSimStatusDisplay.gridx = 1;
 		gbc_lblSimStatusDisplay.gridy = 0;
 		gbc_lblSimStatusDisplay.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(lblSimStatusDisplay, gbc_lblSimStatusDisplay);
 
 		// Add "Start/Stop" button. ///////////////////////////////////////////
 
 		btnStartStop = new JButton("Start");
 		btnStartStop.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				startStopSim();
 			}
 		});
 
 		GridBagConstraints gbc_btnStartStop = new GridBagConstraints();
 		gbc_btnStartStop.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnStartStop.gridx = 3;
 		gbc_btnStartStop.gridy = 0;
 		gbc_btnStartStop.insets = new Insets(0, 0, 5, 5);
 
 		frame.getContentPane().add(btnStartStop, gbc_btnStartStop);
 		
 		// Add separator. /////////////////////////////////////////////////////
 
 		JSeparator separatorSimStatus = new JSeparator();
 
 		GridBagConstraints gbc_separatorSimStatus = new GridBagConstraints();
 		gbc_separatorSimStatus.fill = GridBagConstraints.HORIZONTAL;
 		gbc_separatorSimStatus.gridwidth = 3;
 		gbc_separatorSimStatus.gridx = 1;
 		gbc_separatorSimStatus.gridy = 1;
 		gbc_separatorSimStatus.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(separatorSimStatus, gbc_separatorSimStatus);
 
 		/*********************************************************************
 		 * Peers list.
 		 *********************************************************************/
 
 		// Add static text label. /////////////////////////////////////////////
 
 		JLabel lblPeers = new JLabel("Peers:");
 
 		GridBagConstraints gbc_lblPeers = new GridBagConstraints();
 		gbc_lblPeers.anchor = GridBagConstraints.WEST;
 		gbc_lblPeers.gridx = 1;
 		gbc_lblPeers.gridy = 2;
 		gbc_lblPeers.insets = new Insets(0, 0, 5, 5);
 
 		frame.getContentPane().add(lblPeers, gbc_lblPeers);
 
 		// Add "Add Peer" button. /////////////////////////////////////////////
 
 		btnAddPeer = new JButton("Add Peer");
 		btnAddPeer.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				addPeer();
 			}
 		});
 
 		GridBagConstraints gbc_btnAddPeer = new GridBagConstraints();
 		gbc_btnAddPeer.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnAddPeer.gridx = 2;
 		gbc_btnAddPeer.gridy = 2;
 		gbc_btnAddPeer.insets = new Insets(0, 0, 5, 5);
 
 		frame.getContentPane().add(btnAddPeer, gbc_btnAddPeer);
 		
 		// Add "Delete Peer" button. //////////////////////////////////////////
 
 		btnDeletePeer = new JButton("Delete Peer");
 		btnDeletePeer.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				deleteCurrentPeer();
 			}
 		});
 
 		GridBagConstraints gbc_btnDeletePeer = new GridBagConstraints();
 		gbc_btnDeletePeer.gridx = 3;
 		gbc_btnDeletePeer.gridy = 2;
 		gbc_btnDeletePeer.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(btnDeletePeer, gbc_btnDeletePeer);
 
 		// Add peer list. /////////////////////////////////////////////////////
 
 		JScrollPane scrollPanePeers = new JScrollPane();
 
 		GridBagConstraints gbc_scrollPanePeers = new GridBagConstraints();
 		gbc_scrollPanePeers.fill = GridBagConstraints.BOTH;
 		gbc_scrollPanePeers.gridwidth = 3;
 		gbc_scrollPanePeers.gridx = 1;
 		gbc_scrollPanePeers.gridy = 3;
 		gbc_scrollPanePeers.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(scrollPanePeers, gbc_scrollPanePeers);
 
		listModelPeers = new DefaultListModel<String>();
		listPeers = new JList<String>(listModelPeers);
 		listPeers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		(listPeers.getSelectionModel()).addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent event) {
 				ListSelectionModel listSelectionModel = (ListSelectionModel)event.getSource();
 				if (!listSelectionModel.isSelectionEmpty())
 					refresh();
 			}
 		});
 
 		scrollPanePeers.setViewportView(listPeers);
 
 		// Add separator. /////////////////////////////////////////////////////
 
 		JSeparator separatorPeers = new JSeparator();
 
 		GridBagConstraints gbc_separatorPeers = new GridBagConstraints();
 		gbc_separatorPeers.fill = GridBagConstraints.HORIZONTAL;
 		gbc_separatorPeers.gridwidth = 3;
 		gbc_separatorPeers.gridx = 1;
 		gbc_separatorPeers.gridy = 4;
 		gbc_separatorPeers.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(separatorPeers, gbc_separatorPeers);
 		
 		/*********************************************************************
 		 * Peer properties table.
 		 *********************************************************************/
 
 		// Add static text label. /////////////////////////////////////////////
 
 		JLabel lblPeerProps = new JLabel("Peer Properties:");
 
 		GridBagConstraints gbc_lblPeerProps = new GridBagConstraints();
 		gbc_lblPeerProps.anchor = GridBagConstraints.WEST;
 		gbc_lblPeerProps.gridx = 1;
 		gbc_lblPeerProps.gridy = 5;
 		gbc_lblPeerProps.insets = new Insets(0, 0, 5, 5);
 
 		frame.getContentPane().add(lblPeerProps, gbc_lblPeerProps);
 
 		// Add "Apply" button. ////////////////////////////////////////////////
 
 		btnApplyPeerProps = new JButton("Apply");
 		btnApplyPeerProps.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				Peer peer = getCurrentPeer();
 				if (peer != null) {
 					peer.setName((String)tableModelPeerProps.getValueAt(0, 1));
 					try {
 						double longitude = Double.parseDouble((String)tableModelPeerProps.getValueAt(2, 1));
 						double latitude = Double.parseDouble((String)tableModelPeerProps.getValueAt(3, 1));
 						float accuracy = Float.parseFloat((String)tableModelPeerProps.getValueAt(4, 1));
 						peer.setLocation(new GeoLocation(longitude, latitude, accuracy));
 					} catch (NumberFormatException e) {
 						
 					}
 					try {
 						peer.setAddress(new PeerAddress((String)tableModelPeerProps.getValueAt(1, 1)));
 					} catch (PeerAddressException e) {
 						
 					}
 					refresh();
 				}
 			}
 		});
 		
 		GridBagConstraints gbc_btnApplyPeerProps = new GridBagConstraints();
 		gbc_btnApplyPeerProps.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnApplyPeerProps.gridx = 3;
 		gbc_btnApplyPeerProps.gridy = 5;
 		gbc_btnApplyPeerProps.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(btnApplyPeerProps, gbc_btnApplyPeerProps);
 
 		// Add peer properties table. /////////////////////////////////////////
 
 		JScrollPane scrollPanePeerProps = new JScrollPane();
 
 		GridBagConstraints gbc_scrollPanePeerProps = new GridBagConstraints();
 		gbc_scrollPanePeerProps.fill = GridBagConstraints.BOTH;
 		gbc_scrollPanePeerProps.gridwidth = 3;
 		gbc_scrollPanePeerProps.gridx = 1;
 		gbc_scrollPanePeerProps.gridy = 6;
 		gbc_scrollPanePeerProps.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(scrollPanePeerProps, gbc_scrollPanePeerProps);
 
 		tableModelPeerProps = new DefaultTableModel();
 		tablePeerProps = new JTable(tableModelPeerProps);
 		tableModelPeerProps.addColumn("Property");
 		tableModelPeerProps.addColumn("Value");
 
 		scrollPanePeerProps.setViewportView(tablePeerProps);
 
 		// Add separator. /////////////////////////////////////////////////////
 
 		JSeparator separatorPeerProps = new JSeparator();
 
 		GridBagConstraints gbc_separatorPeerProps = new GridBagConstraints();
 		gbc_separatorPeerProps.fill = GridBagConstraints.HORIZONTAL;
 		gbc_separatorPeerProps.gridwidth = 3;
 		gbc_separatorPeerProps.gridx = 1;
 		gbc_separatorPeerProps.gridy = 7;
 		gbc_separatorPeerProps.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(separatorPeerProps, gbc_separatorPeerProps);
 		
 		/*********************************************************************
 		 * Peer environment properties table.
 		 *********************************************************************/
 
 		// Add static text label. /////////////////////////////////////////////
 
 		JLabel lblPeerEnv = new JLabel("Global Peer Environment Properties:");
 
 		GridBagConstraints gbc_lblPeerEnv = new GridBagConstraints();
 		gbc_lblPeerEnv.anchor = GridBagConstraints.WEST;
 		gbc_lblPeerEnv.gridx = 1;
 		gbc_lblPeerEnv.gridy = 8;
 		gbc_lblPeerEnv.insets = new Insets(0, 0, 5, 5);
 
 		frame.getContentPane().add(lblPeerEnv, gbc_lblPeerEnv);
 		
 		// Add "Apply" button. ////////////////////////////////////////////////
 
 		btnApplyEnvProps = new JButton("Apply");
 
 		GridBagConstraints gbc_btnApplyEnvProps = new GridBagConstraints();
 		gbc_btnApplyEnvProps.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnApplyEnvProps.gridx = 3;
 		gbc_btnApplyEnvProps.gridy = 8;
 		gbc_btnApplyEnvProps.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(btnApplyEnvProps, gbc_btnApplyEnvProps);
 
 		// Add peer environment properties table. /////////////////////////////
 
 		JScrollPane scrollPaneEnvProps = new JScrollPane();
 
 		GridBagConstraints gbc_scrollPaneEnvProps = new GridBagConstraints();
 
 		gbc_scrollPaneEnvProps.fill = GridBagConstraints.BOTH;
 		gbc_scrollPaneEnvProps.gridwidth = 3;
 		gbc_scrollPaneEnvProps.gridx = 1;
 		gbc_scrollPaneEnvProps.gridy = 9;
 		gbc_scrollPaneEnvProps.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(scrollPaneEnvProps, gbc_scrollPaneEnvProps);
 
 		tableModelPeerEnvProps = new DefaultTableModel();
 		tablePeerEnvProps = new JTable(tableModelPeerEnvProps);
 		tableModelPeerEnvProps.addColumn("Property");
 		tableModelPeerEnvProps.addColumn("Value");
 
 		scrollPaneEnvProps.setViewportView(tablePeerEnvProps);
 		
 		// Add separator. /////////////////////////////////////////////////////
 
 		JSeparator separatorBottom = new JSeparator();
 
 		GridBagConstraints gbc_separatorBottom = new GridBagConstraints();
 		gbc_separatorBottom.fill = GridBagConstraints.HORIZONTAL;
 		gbc_separatorBottom.gridwidth = 4;
 		gbc_separatorBottom.gridx = 0;
 		gbc_separatorBottom.gridy = 10;
 		gbc_separatorBottom.insets = new Insets(0, 0, 5, 5);
 
 		frame.getContentPane().add(separatorBottom, gbc_separatorBottom);
 
 		/*********************************************************************
 		 * Statistics.
 		 *********************************************************************/
 
 		// Add static text label. /////////////////////////////////////////////
 
 		JLabel lblStatistics = new JLabel("Statistics:");
 
 		GridBagConstraints gbc_lblStatistics = new GridBagConstraints();
 
 		gbc_lblStatistics.anchor = GridBagConstraints.WEST;
 		gbc_lblStatistics.gridx = 0;
 		gbc_lblStatistics.gridy = 11;
 		gbc_lblStatistics.insets = new Insets(0, 0, 5, 5);
 
 		frame.getContentPane().add(lblStatistics, gbc_lblStatistics);
 
 		// Add statistics text area. //////////////////////////////////////////
 
 		JScrollPane scrollPaneStats = new JScrollPane();
 
 		GridBagConstraints gbc_scrollPaneStats = new GridBagConstraints();
 		gbc_scrollPaneStats.fill = GridBagConstraints.BOTH;
 		gbc_scrollPaneStats.gridwidth = 4;
 		gbc_scrollPaneStats.gridx = 0;
 		gbc_scrollPaneStats.gridy = 12;
 
 		frame.getContentPane().add(scrollPaneStats, gbc_scrollPaneStats);
 
 		JTextArea textAreaStats = new JTextArea();
 		textAreaStats.setFont(new Font("Monospaced", Font.PLAIN, 13));
 		textAreaStats.setEditable(false);
 
 		scrollPaneStats.setViewportView(textAreaStats);
 
 		refresh();
 
 		timer = new Timer(timerRefreshRate, new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frame.repaint();
 			}
 		});
 	}
 
 }
