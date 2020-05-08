 package com.ricex.rpi.server.player;
 
 import java.awt.Dimension;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JTabbedPane;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import com.ricex.rpi.server.RPIServer;
 import com.ricex.rpi.server.RemoteServer;
 import com.ricex.rpi.server.client.RPIClient;
 
 /** The RPIPlayer
  * 
  *   Launches the PlayerUI as well as the servers
  *   Uses Java Swing for th UI
  *   
  * @author Mitchell
  *
  */
 
 public class RPIPlayer extends JFrame {
 
 	/** The singleton instance of this class */
 	private static RPIPlayer _instance;
 	
 	public static RPIPlayer getInstance() {
 		if (_instance == null) {
 			_instance = new RPIPlayer();
 		}
 		return _instance;
 	}
 	
 	/** Starts the RPIPlayer and the servers
 	 * 
 	 * @param args the command line arguments.
 	 */
 	
 	public static void main(String[] args) {
 		RPIPlayer player = getInstance();
 		player.setVisible(true);
 	}	
 	
 	/** The tabbed pane to display the different content */
 	private JTabbedPane tabbedPane;
 	
 	/** The view for displaying the tree of videos */
 	private VideoTreeView videoTreeView;
 	
 	/** The view for displaying playlists */
 	private PlaylistView playlistView;
 	
 	/** The list view for displaying the connected clients */
 	private ClientListView clientListView;
 	
 	/** The thread for the RPI client server to run in */
 	private Thread clientServerThread;
 	
 	/** The thread for the remote server to run in */
 	private Thread remoteServerThread;
 	
 	/** The currently active client */
 	private RPIClient activeClient;
 	
 	/** The list of active client listeners */
 	private List<ActiveClientListener> activeClientListeners;
 	
 	/** Creates a new instance of RPI Player
	 * 
 	 */
 	
 	private RPIPlayer() {
		startServers();		
 		setTitle("RPI Player -- Swing Build");
 		setPreferredSize(new Dimension(800, 600));
 		pack();
 		setResizable(false);
 		
 		//setSystemLookAndFeel();
 		
 		tabbedPane = new JTabbedPane();
 		
 		videoTreeView = new VideoTreeView();
 		playlistView = new PlaylistView();
 		clientListView = new ClientListView();
 		
 		tabbedPane.add("Videos", videoTreeView);
 		tabbedPane.add("Playlists", playlistView);
 		tabbedPane.add("Clients", clientListView);
 		
 		setContentPane(tabbedPane);
 	}
 	
 	/** Adds the shutdown hook for the program, that will shutdown both servers
 	 * 
 	 */
 	
 	private void addShutdownHook() {
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			
 			public void run() {
 				RPIServer.getInstance().shutdown();
 				RemoteServer.getInstance().shutdown();
 			}
 			
 		});
 	}
 	
 	/** Sets the look and feel to the System Look and Feel
 	 * 
 	 */
 	
 	private void setSystemLookAndFeel() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
 			System.out.println("Could not set the look and feel to System look and feel");
 		}
 	}
 	
 	/** Start the servers
 	 * 
 	 */
 	
 	private void startServers() {
 		clientServerThread = new Thread(RPIServer.getInstance());
 		remoteServerThread = new Thread(RemoteServer.getInstance());
 		
 		clientServerThread.setDaemon(true);
 		remoteServerThread.setDaemon(true);
 		
 		clientServerThread.start();
 		remoteServerThread.start();	
 	}
 	
 	/** Sets the active client to the given client
 	 * 
 	 * @param activeClient The new active client
 	 */
 	
 	public void setActiveClient(RPIClient activeClient) {
 		this.activeClient = activeClient;
 		notifyListeners(activeClientExists());
 	}
 	
 	/** 
 	 * @return The active client
 	 */
 	
 	public RPIClient getActiveClient() {
 		return activeClient;
 	}
 	
 	/** 
 	 * @return whether or not an active client exists
 	 */
 	
 	public boolean activeClientExists() {
 		return activeClient != null;
 	}
 	
 	/** Adds the given active client listener
 	 * 
 	 * @param listener The listener to add
 	 */
 	
 	public void addActiveClientListener(ActiveClientListener listener) {
 		activeClientListeners.add(listener);
 	}
 	
 	/** Removes the given active client lsitener
 	 * 
 	 * @param listener The active client listener to remove
 	 */
 	
 	public void removeActiveClientListener(ActiveClientListener listener) {
 		activeClientListeners.remove(listener);
 	}	
 	
 	/** Notifies the active client listeners that the active client has been changed
 	 * 
 	 * @param activeClientChanged whether or not the active client has changed, false means removed
 	 */
 	
 	private void notifyListeners(boolean activeClientChanged) { 
 		//the active client was changedm notify changed
 		if (activeClientChanged) {
 			for (ActiveClientListener listener : activeClientListeners) {
 				listener.activeClientChanged(activeClient);
 			}
 		}
 		//the client was removed, notify removed
 		else {
 			for (ActiveClientListener listener : activeClientListeners) {
 				listener.activeClientRemoved();
 			}
 		}		
 	}
 	
 }
