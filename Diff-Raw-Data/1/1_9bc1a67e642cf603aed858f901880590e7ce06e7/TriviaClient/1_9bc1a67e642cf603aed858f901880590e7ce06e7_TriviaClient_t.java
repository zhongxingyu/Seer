 package net.bubbaland.trivia.client;
 
 import java.awt.Rectangle;
 import java.awt.Window;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 // imports for RMI
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 
 import net.bubbaland.trivia.Round;
 import net.bubbaland.trivia.Trivia;
 import net.bubbaland.trivia.TriviaChartFactory;
 import net.bubbaland.trivia.TriviaInterface;
 import net.bubbaland.trivia.UserList.Role;
 
 /**
  * Provides the root functionality for connecting to the trivia server and creating the associated GUI.
  * 
  * @author Walter Kolczynski
  * 
  */
 public class TriviaClient implements WindowListener {
 
 	// URL for RMI server
 	final static private String						TRIVIA_SERVER_URL	= "rmi://www.bubbaland.net:1099/TriviaInterface";
 	// URL for Wiki
 	final static protected String					WIKI_URL			= "https://sites.google.com/a/kneedeepintheses.org/information/Home";
 	// URL for the IRC client
 	final static protected String					IRC_CLIENT_URL		= "http://webchat.freenode.net/";
 	// IRC channel to join on connection to IRC server
 	final static protected String					IRC_CHANNEL			= "%23kneedeeptrivia";
 	// File name to store window positions
 	final static private String						DEFAULTS_FILENAME	= ".trivia-defaults";
 	// File name to store window positions
 	final static private String						SETTINGS_FILENAME	= ".trivia-settings";
 
 	/**
 	 * Setup properties
 	 */
 	final static public Properties					PROPERTIES			= new Properties();
 	static {
 		/**
 		 * Default properties
 		 */
 		loadDefaults();
 
 		/**
 		 * Load saved properties from file
 		 */
 		final File file = new File(System.getProperty("user.home") + "/" + SETTINGS_FILENAME);
 		try {
 			final BufferedReader fileBuffer = new BufferedReader(new FileReader(file));
 			PROPERTIES.load(fileBuffer);
 		} catch (IOException e) {
 			System.out.println("Couldn't load properties file, may not exist yet.");
 		}
 
 		TriviaChartFactory.loadProperties(PROPERTIES);
 	}
 
 	final private ArrayList<TriviaFrame>			windowList;
 	final static private Hashtable<String, String>	TAB_DESCRIPTION_HASH;
 	static {
 		TAB_DESCRIPTION_HASH = new Hashtable<String, String>(0);
 		TAB_DESCRIPTION_HASH.put("Workflow", "Main tab with summary information, open questions, and answer queue.");
 		TAB_DESCRIPTION_HASH.put("Current", "Tab showing question data for the current round.");
 		TAB_DESCRIPTION_HASH.put("History", "Tab that can show question data for any round.");
 		TAB_DESCRIPTION_HASH.put("By Round", "Tab that displays score information for every round.");
 		TAB_DESCRIPTION_HASH.put("Place Chart", "Chart showing the team's place in time");
 		TAB_DESCRIPTION_HASH.put("Score Chart", "Chart showing the team's score in each round.");
 		TAB_DESCRIPTION_HASH.put("Cumul. Score Chart", "Chart showing the team's total score in time.");
 		TAB_DESCRIPTION_HASH.put("Team Comparison", "Chart comparing each team's score to our score in time.");
 		TAB_DESCRIPTION_HASH.put("*Open Questions", "List of current open questions");
 		TAB_DESCRIPTION_HASH.put("*Answer Queue", "The proposed answer queue for the current round.");
 	}
 
 	// The user's name
 	private volatile String							user;
 	// The user's role
 	private volatile Role							role;
 	// Hashtable of active users and roles
 	private volatile Hashtable<String, Role>		activeUserHash;
 	// Hashtable of idle users and roles
 	private volatile Hashtable<String, Role>		passiveUserHash;
 
 	// The remote server
 	private final TriviaInterface					server;
 	// The local trivia object holding all contest data
 	private volatile Trivia							trivia;
 
 	/**
 	 * Creates a new trivia client GUI
 	 * 
 	 * @param server
 	 *            The RMI Server
 	 */
 	private TriviaClient(TriviaInterface server, boolean useFX) {
 
 		this.server = server;
 		this.windowList = new ArrayList<TriviaFrame>(0);
 
 		// Grab a copy of the current Trivia data structure from the server in the background
 		final TriviaFetcher fetcher = new TriviaFetcher(server, this);
 		fetcher.execute();
 
 		// Create a prompt requesting the user name
 		new UserLoginDialog(this);
 
 		this.setRole(Role.RESEARCHER);
 
 		// Create startup frames
 		for (int f = 0; PROPERTIES.getProperty("Window" + f) != null; f++) {
 			new TriviaFrame(this, PROPERTIES.getProperty("Window" + f).replaceAll("[\\[\\]]", "").split(", "), useFX);
 			useFX = false; // Only put IRC in one window
 		}
 
 		// Wait for trivia object to finish downloading
 		while (this.trivia == null) {
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException exception) {
 			}
 		}
 		// Create timer that will poll server for changes
 		final Timer refreshTimer = new Timer();
 		refreshTimer.scheduleAtFixedRate(new RefreshTask(this), 0,
 				Integer.parseInt(PROPERTIES.getProperty("RefreshRate")));
 
 		// Post welcome to status bar
 		this.log("Welcome " + this.user);
 	}
 
 	/**
 	 * Display disconnected dialog box and prompt for action
 	 */
 	public synchronized void disconnected() {
 
 		final String message = "Communication with server failed!";
 
 		final Object[] options = { "Retry", "Exit" };
 		final int option = JOptionPane.showOptionDialog(null, message, "Disconnected", JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.ERROR_MESSAGE, null, options, options[1]);
 		if (option == 1) {
 			// Exit the client
 			System.exit(0);
 		}
 
 	}
 
 	/**
 	 * Return the local Trivia object. When updating the GUI, always get the current Trivia object first to ensure the
 	 * most recent data is used. Components should always use this local version to read data to limit server traffic.
 	 * 
 	 * @return The local Trivia object
 	 */
 	public Trivia getTrivia() {
 		return this.trivia;
 	}
 
 	/**
 	 * Gets the user name.
 	 * 
 	 * @return The user name
 	 */
 	public String getUser() {
 		return this.user;
 	}
 
 	/**
 	 * Get the hash of active users and roles.
 	 * 
 	 * @return The hashtable of users and roles
 	 */
 	public Hashtable<String, Role> getActiveUserHash() {
 		return this.activeUserHash;
 	}
 
 	/**
 	 * Get the hash of idle users and roles.
 	 * 
 	 * @return The hashtable of users and roles
 	 */
 	public Hashtable<String, Role> getPassiveUserHash() {
 		return this.passiveUserHash;
 	}
 
 	/**
 	 * Display message in the status bar and in console
 	 * 
 	 * @param message
 	 *            Message to log
 	 */
 	public void log(String message) {
 		for (TriviaFrame panel : this.windowList) {
 			// Display message in status bar
 			panel.log(message);
 		}
 		// Print message to console
 		System.out.println("LOG: " + message);
 	}
 
 	/**
 	 * Change the user's role.
 	 * 
 	 * @param role
 	 */
 	protected void setRole(Role role) {
 		int tryNumber = 0;
 		boolean success = false;
 		while (tryNumber < Integer.parseInt(PROPERTIES.getProperty("MaxRetries")) && success == false) {
 			tryNumber++;
 			try {
 				this.trivia = this.server.getTrivia();
 				this.server.setRole(this.user, role);
 				success = true;
 			} catch (final RemoteException e) {
 				this.log("Couldn't change role server (try #" + tryNumber + ").");
 			}
 		}
 
 		// Show disconnected dialog if we could not retrieve the Trivia data
 		if (!success || this.trivia == null) {
 			this.disconnected();
 			return;
 		}
 
 		this.role = role;
 
 	}
 
 	public Role getRole() {
 		return this.role;
 	}
 
 	/**
 	 * Sets the user name
 	 * 
 	 * @param user
 	 *            The new user name
 	 */
 	public void setUser(String user) {
 		int tryNumber = 0;
 		boolean success = false;
 		while (tryNumber < Integer.parseInt(PROPERTIES.getProperty("MaxRetries")) && success == false) {
 			tryNumber++;
 			try {
 				if (this.user == null) {
 					this.server.login(user);
 				} else {
 					this.server.changeUser(this.user, user);
 				}
 				success = true;
 			} catch (final RemoteException e) {
 				this.log("Couldn't change user name on server (try #" + tryNumber + ").");
 			}
 		}
 
 		if (!success) {
 			this.disconnected();
 			return;
 		}
 
 		this.user = user;
 	}
 
 
 	@Override
 	public void windowActivated(WindowEvent e) {
 	}
 
 	@Override
 	public void windowClosed(WindowEvent e) {
 	}
 
 	@Override
 	public void windowClosing(WindowEvent e) {
 		Window window = e.getWindow();
 		savePosition(window);
 		if (window instanceof TriviaFrame) {
 			( (TriviaFrame) window ).saveProperties();
 
 			if (this.windowList.size() == 1) {
 				endProgram();
 			} else {
 				// Remove window from the list
 				this.windowList.remove(window);
 			}
 		}
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent e) {
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent e) {
 	}
 
 	@Override
 	public void windowIconified(WindowEvent e) {
 	}
 
 	@Override
 	public void windowOpened(WindowEvent e) {
 	}
 
 	public TriviaInterface getServer() {
 		return this.server;
 	}
 
 	/**
 	 * Create and show the GUI.
 	 */
 	private static void createAndShowGUI(boolean useFX) {
 		try {
 			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())) {
 					UIManager.setLookAndFeel(info.getClassName());
 				}
 			}
 		} catch (Exception e) {
 
 		}
 
 		// Initialize server variable
 		TriviaInterface triviaServer = null;
 
 		// Initiate connection to RMI server
 		int tryNumber = 0;
 		boolean success = false;
 		while (tryNumber < Integer.parseInt(PROPERTIES.getProperty("MaxRetries")) && success == false) {
 			tryNumber++;
 			try {
 				// Connect to RMI server
 				triviaServer = (TriviaInterface) Naming.lookup(TRIVIA_SERVER_URL);
 				success = true;
 			} catch (MalformedURLException | NotBoundException | RemoteException e) {
 				// Connection failed
 				System.out.println("Initial connection to server failed (try #" + tryNumber + ")");
 
 				if (tryNumber == Integer.parseInt(PROPERTIES.getProperty("MaxRetries"))) {
 					// Maximum retries reached, pop up disconnected dialog
 					final String message = "Could not connect to server.";
 
 					final Object[] options = { "Retry", "Exit" };
 					final int option = JOptionPane.showOptionDialog(null, message, "Disconnected",
 							JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[1]);
 
 					if (option == JOptionPane.OK_OPTION) {
 						// Retry connection
 						tryNumber = 0;
 					} else {
 						// Exit
 						System.exit(0);
 					}
 				}
 			}
 		}
 
 		System.out.println("Connected to trivia server (" + TRIVIA_SERVER_URL + ").");
 
 		new TriviaClient(triviaServer, useFX);
 	}
 
 	public void registerWindow(TriviaFrame frame) {
 		frame.addWindowListener(this);
 		this.windowList.add(frame);
 	}
 
 	public void unregisterWindow(TriviaFrame frame) {
 		frame.removeWindowListener(this);
 		this.windowList.remove(frame);
 	}
 
 	public int getNTriviaWindows() {
 		return this.windowList.size();
 	}
 
 	public TriviaPanel getTab(TriviaFrame frame, String tabName) {
 		TriviaPanel panel = null;
 		switch (tabName) {
 			case "Workflow":
 				panel = new WorkflowPanel(frame, this);
 				break;
 			case "Current":
 				panel = new RoundPanel(this);
 				break;
 			case "History":
 				panel = new HistoryPanel(this);
 				break;
 			case "By Round":
 				panel = new ScoreByRoundPanel(this);
 				break;
 			case "Place Chart":
 				panel = new PlaceChartPanel(this);
 				break;
 			case "Score Chart":
 				panel = new ScoreByRoundChartPanel(this);
 				break;
 			case "Cumul. Score Chart":
 				panel = new CumulativePointsChartPanel(this);
 				break;
 			case "Team Comparison":
 				panel = new TeamComparisonPanel(this);
 				break;
 			case "Open Questions":
 				panel = new OpenQuestionsPanel(this);
 				break;
 			case "Answer Queue":
 				panel = new AnswerQueuePanel(frame, this);
 				break;
 		}
 		return panel;
 	}
 
 	public Set<String> getTabNames() {
 		return TriviaClient.TAB_DESCRIPTION_HASH.keySet();
 	}
 
 	public String getTabDescription(String tabName) {
 		return TriviaClient.TAB_DESCRIPTION_HASH.get(tabName);
 	}
 
 	/**
 	 * Load the saved position and size of the window from file. If none found, use preferred size of components.
 	 * 
 	 * @param window
 	 *            The window whose position and size is to be loaded
 	 */
 	public static void loadPosition(Window window) {
 		try {
 			final String frameID = window.getName();
 
 			final int x = Integer.parseInt(PROPERTIES.getProperty(frameID + ".X"));
 			final int y = Integer.parseInt(PROPERTIES.getProperty(frameID + ".Y"));
 			final int width = Integer.parseInt(PROPERTIES.getProperty(frameID + ".Width"));
 			final int height = Integer.parseInt(PROPERTIES.getProperty(frameID + ".Height"));
 
 			window.setBounds(x, y, width, height);
 
 		} catch (NumberFormatException e) {
 			System.out.println("Couldn't load window position, may not exist yet.");
 			window.pack();
 			window.setLocationRelativeTo(null);
 		}
 	}
 
 	/**
 	 * Entry point for the client application. Only the first argument is used. If the first argument is "useFX", the
 	 * client will include an IRC client panel.
 	 * 
 	 * @param args
 	 *            Command line arguments; only "useFX" is recognized as an argument
 	 * 
 	 */
 	public static void main(String[] args) {
 		boolean useFX = false;
 		// Schedule a job to create and show the GUI
 		if (args.length > 0 && args[0].equals("useFX")) {
 			useFX = true;
 		}
 		SwingUtilities.invokeLater(new TriviaRunnable(useFX));
 	}
 
 	/**
 	 * Convert a cardinal number into its ordinal counterpart.
 	 * 
 	 * @param cardinal
 	 *            The number to convert to ordinal form
 	 * @return String with the ordinal representation of the number (e.g., 1st, 2nd, 3rd, etc.)
 	 * 
 	 */
 	public static String ordinalize(int cardinal) {
 		// Short-circuit for teen numbers that don't follow normal rules
 		if (10 < cardinal % 100 && cardinal % 100 < 14) return cardinal + "th";
 		// Ordinal suffix depends on the ones digit
 		final int modulus = cardinal % 10;
 		switch (modulus) {
 			case 1:
 				return cardinal + "st";
 			case 2:
 				return cardinal + "nd";
 			case 3:
 				return cardinal + "rd";
 			default:
 				return cardinal + "th";
 		}
 	}
 
 	/**
 	 * Save the position and size of the window to file.
 	 * 
 	 * @param window
 	 *            The window whose size and position is to be saved
 	 * 
 	 */
 	public static void savePosition(Window window) {
 		final Rectangle r = window.getBounds();
 		final int x = (int) r.getX();
 		final int y = (int) r.getY();
 		final int width = (int) r.getWidth();
 		final int height = (int) r.getHeight();
 
 		final String frameID = window.getName();
 
 		PROPERTIES.setProperty(frameID + ".X", x + "");
 		PROPERTIES.setProperty(frameID + ".Y", y + "");
 		PROPERTIES.setProperty(frameID + ".Width", width + "");
 		PROPERTIES.setProperty(frameID + ".Height", height + "");
 	}
 
 	private static void savePropertyFile() {
 		final File file = new File(System.getProperty("user.home") + "/" + SETTINGS_FILENAME);
 		try {
 			final BufferedWriter outfileBuffer = new BufferedWriter(new FileWriter(file));
 			PROPERTIES.store(outfileBuffer, "Trivia");
 			outfileBuffer.close();
 		} catch (final IOException e) {
 			System.out.println("Error saving properties.");
 		}
 	}
 
 	/**
 	 * Clear all saved data from file.
 	 * 
 	 */
 	public static void loadDefaults() {
 		PROPERTIES.clear();
 		final InputStream defaults = TriviaClient.class.getResourceAsStream(DEFAULTS_FILENAME);
 		try {
 			PROPERTIES.load(defaults);
 		} catch (IOException e) {
 			System.out.println("Couldn't load default properties file, aborting!");
 			System.exit(-1);
 		}
 	}
 
 	public void loadProperties() {
 		for (TriviaFrame frame : this.windowList) {
 			frame.loadProperties();
 		}
 	}
 
 	public void resetProperties() {
 		loadDefaults();
 		loadProperties();
 	}
 
 	/**
 	 * Private class to handle background downloading of Trivia object from server.
 	 * 
 	 */
 	private class TriviaFetcher extends SwingWorker<Void, Void> {
 
 		final TriviaInterface	server;
 		final TriviaClient		client;
 
 		public TriviaFetcher(TriviaInterface server, TriviaClient client) {
 			super();
 			this.server = server;
 			this.client = client;
 		}
 
 		@Override
 		protected Void doInBackground() throws Exception {
 			/**
 			 * Create a local copy of the Trivia object
 			 */
 			int tryNumber = 0;
 			boolean success = false;
 			while (tryNumber < Integer.parseInt(PROPERTIES.getProperty("MaxRetries")) && success == false) {
 				tryNumber++;
 				try {
 					this.client.trivia = this.server.getTrivia();
 					success = true;
 				} catch (final RemoteException e) {
 					this.client.log("Couldn't retrive trivia data from server (try #" + tryNumber + ").");
 				}
 			}
 
 			// Show disconnected dialog if we could not retrieve the Trivia data
 			if (!success || this.client.trivia == null) {
 				this.client.disconnected();
 				return null;
 			}
 			return null;
 
 		}
 
 	}
 
 	private static class RefreshTask extends TimerTask {
 
 		final TriviaClient	client;
 
 		public RefreshTask(TriviaClient client) {
 			this.client = client;
 		}
 
 		@Override
 		public void run() {
 			Round[] newRounds = null;
 			final int[] oldVersions = client.trivia.getVersions();
 			int currentRound = 0;
 
 			int userListWindow = Integer.parseInt(PROPERTIES.getProperty("UserList.ActiveWindow"));
 			int userListTimeout = Integer.parseInt(PROPERTIES.getProperty("UserList.Timeout"));
 
 			// Synchronize the local Trivia data to match the server
 			int tryNumber = 0;
 			boolean success = false;
 			while (tryNumber < Integer.parseInt(PROPERTIES.getProperty("MaxRetries")) && success == false) {
 				tryNumber++;
 				try {
 					newRounds = client.server.getChangedRounds(client.getUser(), oldVersions);
 					currentRound = client.server.getCurrentRound();
 					client.activeUserHash = client.server.getActiveUsers(userListWindow, userListTimeout);
 					client.passiveUserHash = client.server.getIdleUsers(userListWindow, userListTimeout);
 					success = true;
 				} catch (final RemoteException e) {
 					client.log("Couldn't retrive trivia data from server (try #" + tryNumber + ").");
 				}
 			}
 
 			if (!success) {
 				client.disconnected();
 				return;
 			}
 
 			client.trivia.updateRounds(newRounds);
 			client.trivia.setCurrentRound(currentRound);
 
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					client.update();
 				}
 			});
 		}
 	}
 
 	public void update() {
 		for (TriviaFrame frame : this.windowList) {
 			frame.update(false);
 		}
 	}
 
 	/**
 	 * Custom Runnable class to allow passing of command line argument into invokeLater.
 	 * 
 	 */
 	private static class TriviaRunnable implements Runnable {
 		private final boolean	useFX;
 
 		public TriviaRunnable(boolean useFX) {
 			this.useFX = useFX;
 		}
 
 		@Override
 		public void run() {
 			createAndShowGUI(this.useFX);
 		}
 	}
 
 	protected void endProgram() {
 		// Remove previously saved windows
 		for (int f = 0; PROPERTIES.getProperty("Window" + f) != null; f++) {
 			PROPERTIES.remove("Window" + f);
 		}
 		// Save tabs in all windows
 		for (int f = 0; f < this.getNTriviaWindows(); f++) {
 			String[] tabNames = this.windowList.get(f).getTabbedPane().getTabNames();
 			PROPERTIES.setProperty("Window" + f, Arrays.toString(tabNames));
			this.windowList.get(f).saveProperties();
 			savePosition(this.windowList.get(f));
 		}
 		TriviaClient.savePropertyFile();
 		System.exit(0);
 	}
 
 }
