 package kimononet.simulation;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 
 import javax.imageio.ImageIO;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.Timer;
 
 import java.awt.EventQueue;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import kimononet.geo.DefaultGeoDevice;
 import kimononet.geo.GeoDevice;
 import kimononet.geo.GeoLocation;
 import kimononet.geo.GeoVelocity;
 import kimononet.geo.GeoMap;
 import kimononet.geo.RandomWaypointGeoDevice;
 import kimononet.peer.DefaultPeerEnvironment;
 import kimononet.peer.Peer;
 import kimononet.peer.PeerAddress;
 import kimononet.peer.PeerAgent;
 import kimononet.peer.PeerEnvironment;
 import kimononet.stat.MasterStatMonitor;
 import kimononet.stat.StatMonitor;
 import kimononet.stat.StatResults;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.border.BevelBorder;
 import javax.swing.SwingConstants;
 import java.awt.Color;
 
 public class Simulation {
 
 	private ArrayList<PeerAgent> agents = new ArrayList<PeerAgent>();
 	private boolean bSimRunning = false, bh4x0r = false;
 	private float hostilityFactor;
 	private EnvironmentPropertiesTable envTable;
 	private GeoMap mapDim = new GeoMap(	new GeoLocation(-0.0025, 0.0025, 0f),	// Upper left
 										new GeoLocation(0.0025, -0.0025, 0f));	// Lower right
 	private int peerIndex = 0;	// This is just for peer names, e.g. Peer-0, Peer-1, etc.
 	private JButton btnStartStop, btnAddPeer, btnDeletePeer, btnClearAll, btnSetAsRecv, btnh4x0r, btnCopy, btnClear;
 	private JFrame frame; 
 	private JLabel lblSimStatusDisplay;
 	private JMenuItem mntmStartStopSim, mntmChangeHostility, mntmEditMapDim;
 	private StatDisplay statDisplay;
 	private PeerAgent source, destination;
 	private PeerEnvironment env = new DefaultPeerEnvironment();
 	private PeerList peerList;
 	private PeerPropertiesTable peerPropTable;
 	private SimulationPanel panel; 
 	private StatMonitor monitor;
 	private StatResults results;
 	private Timer timer;
 	private int timerRefreshRate = 1;	// in ms
 	private Timer timerEnableButton;
 	private int timerEnableButtonRefreshRate = 2000 + Integer.parseInt(env.get("beacon-service-timeout"));	// in ms
 
 	public ArrayList<PeerAgent> getPeerAgents() {
 		return agents;
 	}
 
 	public boolean isReceiver(PeerAgent agent) {
 		return (destination == agent);
 	}
 
 	public boolean isSender(PeerAgent agent) {
 		return (source == agent);
 	}
 
 	public boolean isSimulationRunning() {
 		return bSimRunning;
 	}
 
 	public int getCurrentPeerAgentIndex() {
 		return peerList.getCurrentlySelectedItemIndex();	
 	}
 
 	/**
 	 * @return If an error occurred, returns -2.
 	 *         If successful, returns -1.
 	 *         If successful AND there was a previous receiver, returns index of that receiver.
 	 */
 	public int setAsReceiver(int i) {
 		if (i < 0 || i >= agents.size() || (destination == getPeerAgentAt(i)))
 			return -2;
 
 		int ret = -1;
 
 		if (destination != null) {
 			// Restore previous receiver's GeoDevice to random waypoint.
 			int j = agents.indexOf(destination);
 			agents.set(j, new PeerAgent(destination.getPeer(), env, new RandomWaypointGeoDevice(destination.getPeer().getLocation(), destination.getPeer().getVelocity(), mapDim)));
 			peerList.refresh(j);
 			ret = j;
 		}
 
 		// Make new receiver's GeoDevice stationary.
 		agents.set(i, new PeerAgent(getPeerAgentAt(i).getPeer(), env, new DefaultGeoDevice(getPeerAgentAt(i).getPeer().getLocation(), getPeerAgentAt(i).getPeer().getVelocity())));
 		destination = getPeerAgentAt(i);
 
 		refresh();
 
 		return ret;
 	}
 
 	public JFrame getFrame() {
 		return frame;
 	}
 
 	public PeerAgent getCurrentPeerAgent() {
 		return getPeerAgentAt(getCurrentPeerAgentIndex());
 	}
 
 	public PeerAgent getPeerAgentAt(int i) {
 		if (i < 0 || i >= agents.size())
 			return null;
 		return agents.get(i);
 	}
 
 	public PeerAgent getRandomPeerAgent() {
 		return (agents.isEmpty() ? null : agents.get((int)(Math.random() * agents.size())));
 	}
 
 	public PeerAgent getReceiver() {
 		return destination;
 	}
 
 	public PeerAgent getSender() {
 		return source;
 	}
 
 	public PeerEnvironment getPeerEnvironment() {
 		return env;
 	}
 
 	public SimulationPanel getSimulationPanel() {
 		return panel;
 	}
 
 	public StatMonitor getStatMonitor() {
 		return monitor;
 	}
 
 	public StatResults getStatResults() {
 		return results;
 	}
 
 	public void addPeerAgent() {
 		String name = "Peer-" + peerIndex++;
 		PeerAddress address = PeerAddress.generateRandomAddress();
 		GeoLocation location = GeoLocation.generateRandomGeoLocation(mapDim);
 		GeoVelocity velocity = new GeoVelocity(100, GeoLocation.generateRandomBearing());
 		GeoDevice device = new RandomWaypointGeoDevice(location, velocity, mapDim);
 
 		// Create new peer with random address, location, velocity.
 		Peer peer = new Peer(address, location, velocity);
 		peer.setName(name);
 
 		// Create new PeerAgent to represent peer and add it to ArrayList.
 		PeerAgent agent = new PeerAgent(peer, env, device);
 		agents.add(agent);
 
 		// Add peer to list.
 		peerList.append(agent);
 
 		refresh();
 	}
 
 	public void deleteAllPeerAgents() {
 		destination = null;
 		peerList.clear();
 		agents.clear();
 		refresh();
 	}
 
 	public void deleteCurrentPeerAgent() {
 		deletePeerAgentAt(getCurrentPeerAgentIndex());
 	}
 
 	public void deletePeerAgentAt(int i) {
 		if (i < 0 || i >= agents.size())
 			return;
 		if (getPeerAgentAt(i) == destination)
 			destination = null;
 		peerList.deleteItemAt(i);
 		if (bSimRunning)
 			agents.get(i).shutdownServices();
 		agents.remove(i);
 		refresh();
 	}
 
 	public void setCurrentPeerIndex(int i) {
 		if (i < 0 || i >= agents.size())
 			return;
 		peerList.setCurrentlySelectedItemIndex(i);
 		refresh();
 	}
 
 	public void setSender(PeerAgent agent) {
 		source = agent;
 	}
 
 	public void startStopSim() {
 		if (!bSimRunning) {
 			/*****************************************************************
 			 * About to START simulation.
 			 *****************************************************************/
 
 			// Check if there are enough peers.
 			if (agents.size() < 2) {
 				JOptionPane.showMessageDialog(frame, "Please at least 2 peers.");
 				return;
 			}
 
 			// Check if there is a receiver.
 			if (destination == null) {
 				JOptionPane.showMessageDialog(frame, "Please designate a peer as the receiver.");
 				return;
 			}
 
 			monitor = new MasterStatMonitor();
 			results = new StatResults();
 
 			// Start peer services.
 			for (PeerAgent agent : agents) {
 				try {
 					agent.setStatMonitor(monitor);
 					agent.getPeer().getLocation().setTimestamp(agent.getTimeProvider().getTime());
 					agent.startServices();
 				}
 				catch (Exception e) {
 					JOptionPane.showMessageDialog(frame, "Cannot start the simulation. Please ensure all parameters are set correctly.");
 					return;
 				}
 			}
 
 			(new SimulationThread(this, hostilityFactor)).start();
 			timer.start();
 			bSimRunning = true;
 		}
 		else {
 			/*****************************************************************
 			 * About to STOP simulation.
 			 *****************************************************************/
 
 			bSimRunning = false;	// This automatically stops the SimulationThread.
 			timer.stop();
 
 			// Stop peer services.
 			for (PeerAgent agent : agents)
 				agent.shutdownServices();
 
 			btnStartStop.setEnabled(false);
 			mntmStartStopSim.setEnabled(false);
 			timerEnableButton.start();
 		}
 
 		btnAddPeer.setEnabled(!bSimRunning);
 		btnStartStop.setText(bSimRunning ? "Stop" : "Start");
 		envTable.setEnabled(!bSimRunning);
 		lblSimStatusDisplay.setBackground(bSimRunning ? Color.GREEN : Color.RED);
 		lblSimStatusDisplay.setText(bSimRunning ? "Simulation RUNNING" : "Simulation STOPPED");
		mntmChangeHostility.setEnabled(!bSimRunning);
 		mntmEditMapDim.setEnabled(!bSimRunning);
 		mntmStartStopSim.setText(bSimRunning ? "Stop Simulation" : "Start Simulation");
 		peerPropTable.setEnabled(!bSimRunning);
 
 		refresh();
 	}
 
 	public void updateCurrentPeerAgent(GeoLocation location) {
 		if (location == null || getCurrentPeerAgent() == null)
 			return;
 		updateCurrentPeerAgent(location, getCurrentPeerAgent().getPeer().getVelocity());
 		refresh();
 	}
 
 	public void updateCurrentPeerAgent(GeoLocation location, GeoVelocity velocity) {
 		if (location == null || velocity == null || getCurrentPeerAgent() == null)
 			return;
 		boolean bReceiver = isReceiver(getCurrentPeerAgent());
 		getCurrentPeerAgent().getPeer().setLocation(location);
 		getCurrentPeerAgent().getPeer().setVelocity(velocity);
 		if (!bReceiver)
 			agents.set(getCurrentPeerAgentIndex(), new PeerAgent(getCurrentPeerAgent().getPeer(), env, new RandomWaypointGeoDevice(location, velocity, mapDim)));
 		else {
 			agents.set(getCurrentPeerAgentIndex(), new PeerAgent(getCurrentPeerAgent().getPeer(), env, new DefaultGeoDevice(location, velocity)));
 			destination = getCurrentPeerAgent();
 		}
 		refresh();
 	}
 
 	public void refresh() {
 		// Refresh peer list and property tables.
 		peerList.refresh();
 		peerPropTable.refresh();
 		envTable.refresh();
 
 		// Enable disable buttons.
 		btnClearAll.setEnabled(!peerList.isEmpty() && !bSimRunning);
 		btnDeletePeer.setEnabled((getCurrentPeerAgent() != null) && !bSimRunning);
 		btnSetAsRecv.setEnabled((getCurrentPeerAgent() != null) && !bSimRunning);
 		btnClear.setEnabled((results != null) && !bSimRunning);
 		btnCopy.setEnabled((results != null) && !bSimRunning);
 
 		// Refresh statistics.
 		statDisplay.refresh(results);
 
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
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] {0, 0, 0, 0, 0, 0};
 		gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gridBagLayout.columnWeights = new double[] {1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE, 0.0};
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
 
 		mntmChangeHostility = new JMenuItem("Change Hostility Factor...");
 		mntmChangeHostility.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				String string = JOptionPane.showInputDialog(frame, "Please enter a new hostility factor.", hostilityFactor);
 				if (string != null) {
 					try {
 						float newHostilityFactor = Float.parseFloat(string);
 						if (newHostilityFactor < 0 || newHostilityFactor > 1)
 							throw new NumberFormatException();
 						hostilityFactor = newHostilityFactor;
 						if (hostilityFactor > 0.5)
 							JOptionPane.showMessageDialog(frame, "Warning: You have specified a very hostile environment!\n\nClick OK to continue.");
 					} catch (NumberFormatException e) {
 						JOptionPane.showMessageDialog(frame, "Invalid hostility factor. It must be in the range [0, 1].");
 					}
 					refresh();
 				}
 			}
 		});
 		mnSim.add(mntmChangeHostility);
 
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
 				JOptionPane.showMessageDialog(frame, "KimonoNet Simulator\n\nCopyright (C) 2012. All rights reserved.\n\nEric Bollens\nJames Hung\nZorayr Khalapyan\nWade Norris");
 			}
 		});
 		mnHelp.add(mntmAbout);
 
 		/*********************************************************************
 		 * Panel in which to draw the UAVs and stuff.
 		 *********************************************************************/
 
 		BufferedImage imageUAV = null, imageUAVxplod = null, imageUAVdest = null, imageUAVsending = null;
 
 		try {
 			imageUAV = ImageIO.read(new File("uav.png"));
 			imageUAVxplod = ImageIO.read(new File("uavxplod.png"));
 			imageUAVdest = ImageIO.read(new File("uavdest.png"));
 			imageUAVsending = ImageIO.read(new File("uavsend.png"));
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog(frame, "Error loading image.");
 		}
 
 		panel = new SimulationPanel(imageUAV, imageUAVxplod, imageUAVdest, imageUAVsending, mapDim, this);
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
 		gbc_lblSimStatusDisplay.gridwidth = 4;
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
 		gbc_btnStartStop.gridx = 5;
 		gbc_btnStartStop.gridy = 0;
 		gbc_btnStartStop.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(btnStartStop, gbc_btnStartStop);
 
 		// Add separator. /////////////////////////////////////////////////////
 
 		JSeparator separatorSimStatus = new JSeparator();
 
 		GridBagConstraints gbc_separatorSimStatus = new GridBagConstraints();
 		gbc_separatorSimStatus.fill = GridBagConstraints.HORIZONTAL;
 		gbc_separatorSimStatus.gridwidth = 5;
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
 				addPeerAgent();
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
 				deleteCurrentPeerAgent();
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
 				deleteAllPeerAgents();
 			}
 		});
 
 		GridBagConstraints gbc_btnClearAll = new GridBagConstraints();
 		gbc_btnClearAll.gridx = 4;
 		gbc_btnClearAll.gridy = 2;
 		gbc_btnClearAll.insets = new Insets(0, 0, 5, 5);
 
 		frame.getContentPane().add(btnClearAll, gbc_btnClearAll);
 
 		// Add "Set As Receiver" button. ////////////////////////////////////////////
 
 		btnSetAsRecv = new JButton("Set As Receiver");
 		btnSetAsRecv.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				setAsReceiver(getCurrentPeerAgentIndex());
 			}
 		});
 
 		GridBagConstraints gbc_btnSetAsRecv = new GridBagConstraints();
 		gbc_btnSetAsRecv.gridx = 5;
 		gbc_btnSetAsRecv.gridy = 2;
 		gbc_btnSetAsRecv.insets = new Insets(0, 0, 5, 5);
 
 		frame.getContentPane().add(btnSetAsRecv, gbc_btnSetAsRecv);
 
 		// Add peer list. /////////////////////////////////////////////////////
 
 		JScrollPane scrollPanePeers = new JScrollPane();
 
 		GridBagConstraints gbc_scrollPanePeers = new GridBagConstraints();
 		gbc_scrollPanePeers.fill = GridBagConstraints.BOTH;
 		gbc_scrollPanePeers.gridwidth = 5;
 		gbc_scrollPanePeers.gridx = 1;
 		gbc_scrollPanePeers.gridy = 3;
 		gbc_scrollPanePeers.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(scrollPanePeers, gbc_scrollPanePeers);
 
 		peerList = new PeerList(this);
 		scrollPanePeers.setViewportView(peerList);
 
 		// Add separator. /////////////////////////////////////////////////////
 
 		JSeparator separatorPeers = new JSeparator();
 
 		GridBagConstraints gbc_separatorPeers = new GridBagConstraints();
 		gbc_separatorPeers.fill = GridBagConstraints.HORIZONTAL;
 		gbc_separatorPeers.gridwidth = 5;
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
 		gbc_lblPeerProps.gridwidth = 5;
 		gbc_lblPeerProps.anchor = GridBagConstraints.WEST;
 		gbc_lblPeerProps.gridx = 1;
 		gbc_lblPeerProps.gridy = 5;
 		gbc_lblPeerProps.insets = new Insets(0, 0, 5, 5);
 
 		frame.getContentPane().add(lblPeerProps, gbc_lblPeerProps);
 
 		// Add peer properties table. /////////////////////////////////////////
 
 		JScrollPane scrollPanePeerProps = new JScrollPane();
 
 		GridBagConstraints gbc_scrollPanePeerProps = new GridBagConstraints();
 		gbc_scrollPanePeerProps.fill = GridBagConstraints.BOTH;
 		gbc_scrollPanePeerProps.gridwidth = 5;
 		gbc_scrollPanePeerProps.gridx = 1;
 		gbc_scrollPanePeerProps.gridy = 6;
 		gbc_scrollPanePeerProps.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(scrollPanePeerProps, gbc_scrollPanePeerProps);
 
 		peerPropTable = new PeerPropertiesTable(this);
 		scrollPanePeerProps.setViewportView(peerPropTable);
 
 		// Add separator. /////////////////////////////////////////////////////
 
 		JSeparator separatorPeerProps = new JSeparator();
 
 		GridBagConstraints gbc_separatorPeerProps = new GridBagConstraints();
 		gbc_separatorPeerProps.fill = GridBagConstraints.HORIZONTAL;
 		gbc_separatorPeerProps.gridwidth = 5;
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
 		gbc_lblPeerEnv.gridwidth = 5;
 		gbc_lblPeerEnv.anchor = GridBagConstraints.WEST;
 		gbc_lblPeerEnv.gridx = 1;
 		gbc_lblPeerEnv.gridy = 8;
 		gbc_lblPeerEnv.insets = new Insets(0, 0, 5, 5);
 
 		frame.getContentPane().add(lblPeerEnv, gbc_lblPeerEnv);
 
 		// Add peer environment properties table. /////////////////////////////
 
 		JScrollPane scrollPaneEnvProps = new JScrollPane();
 
 		GridBagConstraints gbc_scrollPaneEnvProps = new GridBagConstraints();
 		gbc_scrollPaneEnvProps.fill = GridBagConstraints.BOTH;
 		gbc_scrollPaneEnvProps.gridwidth = 5;
 		gbc_scrollPaneEnvProps.gridx = 1;
 		gbc_scrollPaneEnvProps.gridy = 9;
 		gbc_scrollPaneEnvProps.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(scrollPaneEnvProps, gbc_scrollPaneEnvProps);
 
 		envTable = new EnvironmentPropertiesTable(this);
 		scrollPaneEnvProps.setViewportView(envTable);
 
 		// Add separator. /////////////////////////////////////////////////////
 
 		JSeparator separatorBottom = new JSeparator();
 
 		GridBagConstraints gbc_separatorBottom = new GridBagConstraints();
 		gbc_separatorBottom.fill = GridBagConstraints.HORIZONTAL;
 		gbc_separatorBottom.gridwidth = 6;
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
 
 		// Add "Clear" button. /////////////////////////////////////////////////
 
 		btnClear = new JButton("Clear");
 		btnClear.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				results = null;
 				refresh();
 			}
 		});
 
 		GridBagConstraints gbc_btnClear = new GridBagConstraints();
 		gbc_btnClear.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnClear.gridx = 5;
 		gbc_btnClear.gridy = 11;
 		gbc_btnClear.insets = new Insets(0, 0, 5, 0);
 
 		frame.getContentPane().add(btnClear, gbc_btnClear);
 
 		// Add "Copy" button. /////////////////////////////////////////////////
 
 		btnCopy = new JButton("Copy");
 		btnCopy.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				statDisplay.copyToClipboard();
 				refresh();
 			}
 		});
 
 		GridBagConstraints gbc_btnCopy = new GridBagConstraints();
 		gbc_btnCopy.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnCopy.gridx = 4;
 		gbc_btnCopy.gridy = 11;
 		gbc_btnCopy.insets = new Insets(0, 0, 5, 5);
 
 		frame.getContentPane().add(btnCopy, gbc_btnCopy);
 
 		// Add "h4x0r" button. /////////////////////////////////////////////////
 
 		btnh4x0r = new JButton("Invert Colors");
 		btnh4x0r.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				bh4x0r = !bh4x0r;
 				if (bh4x0r) {
 					statDisplay.setForeground(Color.GREEN);
 					statDisplay.setBackground(Color.BLACK);
 				}
 				else {
 					statDisplay.setForeground(Color.BLACK);
 					statDisplay.setBackground(Color.WHITE);
 				}
 				refresh();
 			}
 		});
 
 		GridBagConstraints gbc_btnh4x0r = new GridBagConstraints();
 		gbc_btnh4x0r.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnh4x0r.gridx = 3;
 		gbc_btnh4x0r.gridy = 11;
 		gbc_btnh4x0r.insets = new Insets(0, 0, 5, 5);
 
 		frame.getContentPane().add(btnh4x0r, gbc_btnh4x0r);
 
 		// Add statistics text area. //////////////////////////////////////////
 
 		JScrollPane scrollPaneStats = new JScrollPane();
 
 		GridBagConstraints gbc_scrollPaneStats = new GridBagConstraints();
 		gbc_scrollPaneStats.fill = GridBagConstraints.BOTH;
 		gbc_scrollPaneStats.gridwidth = 6;
 		gbc_scrollPaneStats.gridx = 0;
 		gbc_scrollPaneStats.gridy = 12;
 
 		frame.getContentPane().add(scrollPaneStats, gbc_scrollPaneStats);
 
 		statDisplay = new StatDisplay();
 		scrollPaneStats.setViewportView(statDisplay);
 
 		refresh();
 
 		timer = new Timer(timerRefreshRate, new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				// Refresh the UI.
 				panel.incrementClock();
 				refresh();
 			}
 		});
 
 		timerEnableButton = new Timer(timerEnableButtonRefreshRate, new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				btnStartStop.setEnabled(true);
 				mntmStartStopSim.setEnabled(true);
 				refresh();
 				timerEnableButton.stop();
 			}
 		});
 	}
 
 }
