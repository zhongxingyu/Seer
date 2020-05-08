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
 
 import kimononet.geo.GeoDevice;
 import kimononet.geo.GeoLocation;
 import kimononet.geo.GeoLocationException;
 import kimononet.geo.GeoVelocity;
 import kimononet.geo.GeoMap;
 import kimononet.geo.RandomWaypointGeoDevice;
 import kimononet.net.routing.QualityOfService;
 import kimononet.net.transport.DataPacket;
 import kimononet.peer.Peer;
 import kimononet.peer.PeerAddress;
 import kimononet.peer.PeerAddressException;
 import kimononet.peer.PeerAgent;
 import kimononet.peer.PeerEnvironment;
 import kimononet.stat.MasterStatMonitor;
 import kimononet.stat.StatMonitor;
 import kimononet.stat.StatResults;
 
 import javax.swing.JList;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.border.BevelBorder;
 import javax.swing.SwingConstants;
 import java.awt.Color;
 
 public class Simulation {
 
 	private final String[] properties = {	"Name",				// Row 0
 											"Address",			// Row 1
 											"Longitude ()",	// Row 2
 											"Latitude ()",		// Row 3
 											"Accuracy",			// Row 4
 											"Speed (m/s)",		// Row 5
 											"Bearing ()"};		// Row 6
 
 	private ArrayList<PeerAgent> arrayListPeerAgents = new ArrayList<PeerAgent>();
 	private boolean bSimRunning = false;
 	private boolean bh4x0r = false;
 	private DefaultListModel listModelPeers;
 	private PropertyTableModel tableModelPeerProps;
 	private PropertyTableModel tableModelPeerEnvProps;
 	private JButton btnStartStop, btnAddPeer, btnDeletePeer, btnClearAll, /*btnApplyPeerProps, btnApplyEnvProps,*/ btnh4x0r, btnCopy, btnClear;
 	private JFrame frame; 
 	private JLabel lblSimStatusDisplay;
 	private JList listPeers;
 	private JMenuItem mntmStartStopSim, mntmEditMapDim;
 	private JTable tablePeerProps;
 	private JTable tablePeerEnvProps;
 	private JTextArea textAreaStats;
 	private int peerIndex = 0;	// This is just for peer names, e.g. Peer-0, Peer-1, etc.
 	private GeoMap mapDim = new GeoMap(	new GeoLocation(-0.01, 0.01, 0f),		// Upper left
 										new GeoLocation(0.01, -0.01, 0f));	// Lower right
 	private PeerEnvironment peerEnv = new PeerEnvironment();
 	private StatMonitor statMon = new MasterStatMonitor();
 	private StatResults results;
 	private Timer timer;
 	private int timerRefreshRate = 1000;	// in ms
 
 	private void addPeer() {
 		GeoLocation location = GeoLocation.generateRandomGeoLocation(mapDim);
 		GeoVelocity velocity = new GeoVelocity(100, GeoLocation.generateRandomBearing());
 		GeoDevice geoDevice = new RandomWaypointGeoDevice(location, velocity);
 
 		// Create new peer with random address, location, velocity.
 		Peer peer = new Peer(PeerAddress.generateRandomAddress(), location, velocity);
 		peer.setName("Peer-" + peerIndex++);
 
 		// Create new PeerAgent to represent peer and add it to ArrayList.
 		PeerAgent agent = new PeerAgent(peer, peerEnv, geoDevice);
 		agent.setStatMonitor(statMon);
 		arrayListPeerAgents.add(agent);
 
 		// Add peer to list.
 		listModelPeers.add(listPeers.getModel().getSize(), peer.getName() + " (" + peer.getAddress().toString() + ")");
 
 		refresh();
 	}
 
 	private void deleteCurrentPeer() {
 		int i = getCurrentPeerIndex();
 		if (i < 0)
 			return;
 		listModelPeers.remove(i);
 		arrayListPeerAgents.remove(i);
 		refresh();
 	}
 
 	private void deleteAllPeers() {
 		listModelPeers.clear();
 		arrayListPeerAgents.clear();
 		refresh();
 	}
 
 	private void startStopSim() {
 		if (arrayListPeerAgents.size() < 2) {
 			JOptionPane.showMessageDialog(frame, "Please at least 2 peers.");
 			return;
 		}
 
 		// Start/stop services of each peer.
 		for (int i = 0; i < arrayListPeerAgents.size(); i++) {
 			PeerAgent agent = arrayListPeerAgents.get(i);
 			Peer peer = agent.getPeer();
 			if (!bSimRunning) {
 				// This is to prevent the UAVs from jumping a large distance if
 				// the simulation was not started right after adding the peers.
 				peer.getLocation().setTimestamp(agent.getTimeProvider().getTime());
 
 				agent.startServices();
 			}
 			else
 				agent.shutdownServices();
 		}
 
 		bSimRunning = !bSimRunning;
 
 		if (!bSimRunning) {
 			// If we are stopping the simulation, then cleanup and display the
 			// statistics.
 
 			// Wait for all the threads to die out/shutdown.
 			//sleep(KiNCoL.SHUTDOWN_DELAY);
 			
 			results = null;
 
 			/*System.out.println("#########BEACON SERVICE RESULTS#########");
 			for(PeerAgent agent : agents){
 				System.out.println("Agent: \t " + agent.getPeer().getName() + 
 						           " # of peers: \t " + agent.getPeers().size() + 
 						           " # of peers2: \t " + agent.getPeers2().size());
 			}
 			System.out.println("#################DONE###################");*/
 		}
 		else {
 			// We are starting the simulation.
 			results = new StatResults();
 		}
 
 		// Start/stop repaint timer.
 		if (bSimRunning)
 			timer.start();
 		else
 			timer.stop();
 
 		// Enable/disable buttons and stuff. Note that some buttons are enabled
 		// disabled through refresh() -- these are the ones that will also be
 		// enabled/disabled depending on other factors, e.g. if there are is a
 		// peer selected, not just whether or not the simulation is running.
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
 	}
 
 	private PeerAgent getRandomPeerAgent() {
 		return (arrayListPeerAgents.isEmpty() ? null : arrayListPeerAgents.get((int)(Math.random() * arrayListPeerAgents.size())));
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
 
 	public void setCurrentPeerIndex(int i) {
 		if (listPeers == null)
 			return;
 		listPeers.getSelectionModel().setSelectionInterval(i, i);	
 	}
 
	public void updateCurrentPeerAgent(GeoLocation location) {
		updateCurrentPeerAgent(location, getCurrentPeer().getVelocity());		
	}

	public void updateCurrentPeerAgent(GeoLocation location, GeoVelocity velocity) {
		getCurrentPeer().setLocation(location);
		getCurrentPeer().setVelocity(velocity);
		arrayListPeerAgents.set(getCurrentPeerIndex(), new PeerAgent(getCurrentPeer(), peerEnv, new RandomWaypointGeoDevice(location, velocity)));		
	}

 	public Peer getPeerAt(int index) {
 		if (index < 0)
 			return null;
 		return arrayListPeerAgents.get(index).getPeer();
 	}
 
 	public JFrame getFrame() {
 		return frame;
 	}
 
 	public void refresh() {
 		if (tableModelPeerProps != null)
 			tableModelPeerProps.getDataVector().removeAllElements();
 
 		if (listModelPeers != null && !listModelPeers.isEmpty()) {
 			if (btnClearAll != null)
 				btnClearAll.setEnabled(!bSimRunning);
 		}
 		else {
 			if (btnClearAll != null)
 				btnClearAll.setEnabled(false);
 		}
 
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
 				tableModelPeerProps.addRow(new Object[] {properties[6], Double.toString(Math.toDegrees(getCurrentPeer().getVelocity().getBearing()))});
 			}
 
 			if (btnDeletePeer != null)
 				btnDeletePeer.setEnabled(!bSimRunning);
 			/*if (btnApplyPeerProps != null)
 				btnApplyPeerProps.setEnabled(!bSimRunning);
 			if (btnApplyEnvProps != null)
 				btnApplyEnvProps.setEnabled(!bSimRunning);*/
 		}
 		else {
 			if (btnDeletePeer != null)
 				btnDeletePeer.setEnabled(false);
 			/*if (btnApplyPeerProps != null)
 				btnApplyPeerProps.setEnabled(false);
 			if (btnApplyEnvProps != null)
 				btnApplyEnvProps.setEnabled(false);*/
 		}
 
 		if (textAreaStats != null && results != null) {
 			textAreaStats.setText(results.toString());
 			textAreaStats.setCaretPosition(0);
 		}
 
 		if (textAreaStats.getText().isEmpty()) {
 			btnClear.setEnabled(false);
 			btnCopy.setEnabled(false);
 		}
 		else {
 			btnClear.setEnabled(!bSimRunning);
 			btnCopy.setEnabled(!bSimRunning);
 		}
 
 		if (frame != null)
 			frame.repaint();
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
 
 		//JPanel panel = new JPanel();
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
 		gbc_lblSimStatusDisplay.gridwidth = 3;
 		gbc_lblSimStatusDisplay.gridx = 1;
 		gbc_lblSimStatusDisplay.gridy = 0;
 		gbc_lblSimStatusDisplay.insets = new Insets(0, 0, 5, 5);
 
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
 		gbc_btnStartStop.gridx = 4;
 		gbc_btnStartStop.gridy = 0;
 		gbc_btnStartStop.insets = new Insets(0, 0, 5, 0);
 				
 		frame.getContentPane().add(btnStartStop, gbc_btnStartStop);
 		
 		// Add separator. /////////////////////////////////////////////////////
 
 		JSeparator separatorSimStatus = new JSeparator();
 
 		GridBagConstraints gbc_separatorSimStatus = new GridBagConstraints();
 		gbc_separatorSimStatus.fill = GridBagConstraints.HORIZONTAL;
 		gbc_separatorSimStatus.gridwidth = 4;
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
 		gbc_btnDeletePeer.insets = new Insets(0, 0, 5, 5);
 
 		frame.getContentPane().add(btnDeletePeer, gbc_btnDeletePeer);
 		
 		// Add "Clear All" button. ////////////////////////////////////////////
 
 		btnClearAll = new JButton("Clear All");
 		btnClearAll.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				deleteAllPeers();
 			}
 		});
 		
 		GridBagConstraints gbc_btnClearAll = new GridBagConstraints();
 		gbc_btnClearAll.gridx = 4;
 		gbc_btnClearAll.gridy = 2;
 		gbc_btnClearAll.insets = new Insets(0, 0, 5, 0);
 		
 		frame.getContentPane().add(btnClearAll, gbc_btnClearAll);
 
 		// Add peer list. /////////////////////////////////////////////////////
 
 		JScrollPane scrollPanePeers = new JScrollPane();
 
 		GridBagConstraints gbc_scrollPanePeers = new GridBagConstraints();
 		gbc_scrollPanePeers.fill = GridBagConstraints.BOTH;
 		gbc_scrollPanePeers.gridwidth = 4;
 		gbc_scrollPanePeers.gridx = 1;
 		gbc_scrollPanePeers.gridy = 3;
 		gbc_scrollPanePeers.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(scrollPanePeers, gbc_scrollPanePeers);
 
 		listModelPeers = new DefaultListModel();
 		listPeers = new JList(listModelPeers);
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
 		gbc_separatorPeers.gridwidth = 4;
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
 
 		/*btnApplyPeerProps = new JButton("Apply");
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
 		gbc_btnApplyPeerProps.gridx = 4;
 		gbc_btnApplyPeerProps.gridy = 5;
 		gbc_btnApplyPeerProps.insets = new Insets(0, 0, 5, 0);
 		
 		frame.getContentPane().add(btnApplyPeerProps, gbc_btnApplyPeerProps);*/
 
 		// Add peer properties table. /////////////////////////////////////////
 
 		JScrollPane scrollPanePeerProps = new JScrollPane();
 
 		GridBagConstraints gbc_scrollPanePeerProps = new GridBagConstraints();
 		gbc_scrollPanePeerProps.fill = GridBagConstraints.BOTH;
 		gbc_scrollPanePeerProps.gridwidth = 4;
 		gbc_scrollPanePeerProps.gridx = 1;
 		gbc_scrollPanePeerProps.gridy = 6;
 		gbc_scrollPanePeerProps.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(scrollPanePeerProps, gbc_scrollPanePeerProps);
 
 		tableModelPeerProps = new PropertyTableModel();
 		tablePeerProps = new JTable(tableModelPeerProps);
 		tableModelPeerProps.addColumn("Property");
 		tableModelPeerProps.addColumn("Value");
 		tableModelPeerProps.addTableModelListener(new TableModelListener() {
 			public void tableChanged(TableModelEvent tme) {
 				if (tme.getType() == TableModelEvent.UPDATE) {
 					Peer peer = getCurrentPeer();
 					if (peer != null) {
 						try {
 							String name = (String)tableModelPeerProps.getValueAt(0, 1);
 							PeerAddress address = new PeerAddress((String)tableModelPeerProps.getValueAt(1, 1));
 							double longitude = Double.parseDouble((String)tableModelPeerProps.getValueAt(2, 1));
 							double latitude = Double.parseDouble((String)tableModelPeerProps.getValueAt(3, 1));
 							float accuracy = Float.parseFloat((String)tableModelPeerProps.getValueAt(4, 1));
 							float speed = Float.parseFloat((String)tableModelPeerProps.getValueAt(5, 1));
 							float bearing = (float)Math.toRadians(Double.parseDouble((String)tableModelPeerProps.getValueAt(6, 1)));
 
 							GeoLocation location = new GeoLocation(longitude, latitude, accuracy);
 							GeoVelocity velocity = new GeoVelocity(speed, bearing); 
 
 							peer.setName(name);
 							peer.setAddress(address);
							updateCurrentPeerAgent(location, velocity);
 						} catch (NumberFormatException nfe) {
 							JOptionPane.showMessageDialog(frame, "Please a valid number.");
 						} catch (GeoLocationException gle) {
 							JOptionPane.showMessageDialog(frame, gle.getMessage());
 						} catch (PeerAddressException pae) {
 							JOptionPane.showMessageDialog(frame, "Please a valid address.");
 						}
 						refresh();
 					}
 				}
 			}
 		});
 
 		scrollPanePeerProps.setViewportView(tablePeerProps);
 
 		// Add separator. /////////////////////////////////////////////////////
 
 		JSeparator separatorPeerProps = new JSeparator();
 
 		GridBagConstraints gbc_separatorPeerProps = new GridBagConstraints();
 		gbc_separatorPeerProps.fill = GridBagConstraints.HORIZONTAL;
 		gbc_separatorPeerProps.gridwidth = 4;
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
 
 		/*btnApplyEnvProps = new JButton("Apply");
 		
 		GridBagConstraints gbc_btnApplyEnvProps = new GridBagConstraints();
 		gbc_btnApplyEnvProps.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnApplyEnvProps.gridx = 4;
 		gbc_btnApplyEnvProps.gridy = 8;
 		gbc_btnApplyEnvProps.insets = new Insets(0, 0, 5, 0);
 				
 		frame.getContentPane().add(btnApplyEnvProps, gbc_btnApplyEnvProps);*/
 
 		// Add peer environment properties table. /////////////////////////////
 
 		JScrollPane scrollPaneEnvProps = new JScrollPane();
 
 		GridBagConstraints gbc_scrollPaneEnvProps = new GridBagConstraints();
 
 		gbc_scrollPaneEnvProps.fill = GridBagConstraints.BOTH;
 		gbc_scrollPaneEnvProps.gridwidth = 4;
 		gbc_scrollPaneEnvProps.gridx = 1;
 		gbc_scrollPaneEnvProps.gridy = 9;
 		gbc_scrollPaneEnvProps.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(scrollPaneEnvProps, gbc_scrollPaneEnvProps);
 
 		tableModelPeerEnvProps = new PropertyTableModel();
 		tablePeerEnvProps = new JTable(tableModelPeerEnvProps);
 		tableModelPeerEnvProps.addColumn("Property");
 		tableModelPeerEnvProps.addColumn("Value");
 
 		scrollPaneEnvProps.setViewportView(tablePeerEnvProps);
 		
 		// Add separator. /////////////////////////////////////////////////////
 
 		JSeparator separatorBottom = new JSeparator();
 
 		GridBagConstraints gbc_separatorBottom = new GridBagConstraints();
 		gbc_separatorBottom.fill = GridBagConstraints.HORIZONTAL;
 		gbc_separatorBottom.gridwidth = 5;
 		gbc_separatorBottom.gridx = 0;
 		gbc_separatorBottom.gridy = 10;
 		gbc_separatorBottom.insets = new Insets(0, 0, 5, 0);
 
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
 		gbc_scrollPaneStats.gridwidth = 5;
 		gbc_scrollPaneStats.gridx = 0;
 		gbc_scrollPaneStats.gridy = 12;
 
 		frame.getContentPane().add(scrollPaneStats, gbc_scrollPaneStats);
 
 		textAreaStats = new JTextArea();
 		textAreaStats.setFont(new Font("Monospaced", Font.PLAIN, 13));
 		textAreaStats.setEditable(false);
 		scrollPaneStats.setViewportView(textAreaStats);
 
 		// Add "h4x0r" button. /////////////////////////////////////////////////
 
 		btnh4x0r = new JButton("1337 Mode");
 		btnh4x0r.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				bh4x0r = !bh4x0r;
 				if (bh4x0r) {
 					textAreaStats.setForeground(Color.GREEN);
 					textAreaStats.setBackground(Color.BLACK);
 				}
 				else {
 					textAreaStats.setForeground(Color.BLACK);
 					textAreaStats.setBackground(Color.WHITE);
 				}
 				refresh();
 			}
 		});
 		
 		GridBagConstraints gbc_btnh4x0r = new GridBagConstraints();
 		gbc_btnh4x0r.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnh4x0r.gridx = 2;
 		gbc_btnh4x0r.gridy = 11;
 		gbc_btnh4x0r.insets = new Insets(0, 0, 5, 0);
 		
 		frame.getContentPane().add(btnh4x0r, gbc_btnh4x0r);
 
 		// Add "Copy" button. /////////////////////////////////////////////////
 
 		btnCopy = new JButton("Copy");
 		btnCopy.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				textAreaStats.selectAll();
 				textAreaStats.copy();
 				textAreaStats.setCaretPosition(0);
 				refresh();
 			}
 		});
 		
 		GridBagConstraints gbc_btnCopy = new GridBagConstraints();
 		gbc_btnCopy.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnCopy.gridx = 3;
 		gbc_btnCopy.gridy = 11;
 		gbc_btnCopy.insets = new Insets(0, 0, 5, 0);
 		
 		frame.getContentPane().add(btnCopy, gbc_btnCopy);
 
 		// Add "Clear" button. /////////////////////////////////////////////////
 
 		btnClear = new JButton("Clear");
 		btnClear.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				textAreaStats.setText(null);
 				refresh();
 			}
 		});
 		
 		GridBagConstraints gbc_btnClear = new GridBagConstraints();
 		gbc_btnClear.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnClear.gridx = 4;
 		gbc_btnClear.gridy = 11;
 		gbc_btnClear.insets = new Insets(0, 0, 5, 0);
 		
 		frame.getContentPane().add(btnClear, gbc_btnClear);
 
 		refresh();
 
 		timer = new Timer(timerRefreshRate, new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//Account for agents exploding in hostile environments.
 				/*for(PeerAgent agent : agents){
 					if(Math.random() < hostilityFactor){
 						killAgent(agent);
 					}
 				}*/
 
 				// Get a random sender.
 				PeerAgent source = getRandomPeerAgent();
 				PeerAgent destination;
 
 				// Find a random receiver that is not the sender. 
 				while ((destination = getRandomPeerAgent()) == source) { }
 
 				// Send packet from source to destination.
 				byte[] payload = new byte[] {0x01, 0x02};
 				source.sendDataPacket(new DataPacket(source, destination.getPeer(), QualityOfService.REGULAR, payload));			
 
 				// Update statistics.
 				results.combine(statMon.getStats().getStatResults(source.getPeer().getAddress(), destination.getPeer().getAddress()));
 
 				// Refresh the UI.
 				refresh();
 			}
 		});
 	}
 
 }
