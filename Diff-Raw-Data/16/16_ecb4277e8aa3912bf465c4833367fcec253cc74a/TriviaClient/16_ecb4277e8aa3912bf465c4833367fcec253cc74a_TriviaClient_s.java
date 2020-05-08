 package net.bubbaland.trivia.client;
 
 //imports for GUI
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 import net.bubbaland.trivia.*;
 
 import java.net.MalformedURLException;
 //imports for RMI
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
import java.util.Properties;
 
 // TODO: Auto-generated Javadoc
 /**
  * This class provides the root function for connecting to the trivia server and creating the associated GUI.
  *
  * @author Walter Kolczynski 
  * 
  */
 public class TriviaClient extends TriviaPanel  implements ActionListener {
 
 	// The Constant serialVersionUID. 
 	private static final long	serialVersionUID	= 5464403297756091690L;
 
 	// The refresh rate of the GUI elements (in milliseconds) 
 	final private static int	REFRESH_RATE		= 100;
 	// The maximum number of retries the client will make when failing in communication with the server
 	protected static final int	MAX_RETRIES			= 10;
 	// Height of the status bar at the bottom of the GUI
 	final static private int	STATUS_HEIGHT		= 14;
 	// Font size of the status text
 	final static private float	STATUS_FONT_SIZE	= 12.0f;
 	
 	// URL for RMI server
	final static private String	TRIVIA_SERVER_URL	= "rmi://www.bubbaland.net:1099/TriviaInterface";
 	
 	// URL for the IRC server
 	final static private String	IRC_CLIENT_URL		= "http://webchat.freenode.net/";
 	// IRC channel to join on connection to IRC server
 	final static private String	IRC_CHANNEL			= "%23kneedeeptrivia";
 	
 	private volatile Trivia trivia;
 	private TriviaInterface server;
 
 	// The tabbed pane
 	private JTabbedPane		book;
 	// Individual pages in the tabbed pane
 	private TriviaPanel[]	pages;
 	// The status bar at the bottom
 	private JLabel			statusBar;
 	// User name
 	private String			user;
 	
 	/**
 	 * Creates a new trivia client GUI
 	 *
 	 * @param server RMI Server
 	 */
 	private TriviaClient( JFrame parent, TriviaInterface server ) {
 
 		// Call parent constructor and use a GridBagLayout
 		super( new GridBagLayout() );
 		
 		// Create a prompt requesting the user name
 		new UserLogin( this );
 		
 		this.server = server;
 		
 		// Create a local copy of the Trivia object
 		int tryNumber = 0;
 		boolean success = false;
 		while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
 			tryNumber++;
 			try {
 				this.trivia = server.getTrivia();
 				success = true;
 			} catch ( RemoteException e ) {
 				this.log( "Couldn't retrive trivia data from server (try #" + tryNumber + ")." );
 				e.printStackTrace();
 			}
 		}
 
		if ( !success ) {
 			this.disconnected();
 			return;
 		}
 		
 		JMenuBar menuBar = new JMenuBar();
 		
 		JMenu menu = new JMenu("User");
 		JMenuItem menuItem = new JMenuItem("Change name", KeyEvent.VK_N);
 		menuItem.setActionCommand("Change name");
 		menuItem.addActionListener(this);		
 		menu.add(menuItem);
 		menuBar.add(menu);
 		
 		menuBar.add(Box.createHorizontalGlue());
 		menu = new JMenu("Admin");
 		menuBar.add(menu);
 		
 		menuItem = new JMenuItem("Load state", KeyEvent.VK_L);
 		menuItem.setActionCommand("Load state");
 		menuItem.addActionListener(this);		
 		menu.add(menuItem);
 				
 		parent.setJMenuBar(menuBar);
 		
 		// Set up layout constraints
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.BOTH;
 		
 		// Create tabbed pane
 		this.book = new JTabbedPane();
 		
 		// Create panel that contains web browser for IRC
 		String url = IRC_CLIENT_URL + "?nick=" + user + "&channels=" + IRC_CHANNEL;
 		BrowserPanel browser = new BrowserPanel( url );
 		browser.setPreferredSize( new Dimension( 0, 204 ) );
 		
 		// Put the tabbed pane and browser panel in an adjustable vertical split pane
 		JSplitPane splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, this.book, browser );
 		// Put the split pane at the top of the window
 		c.gridx = 0;		c.gridy = 0;	
 		// When the window resizes, adjust the split pane size
 		c.weightx = 1.0;	c.weighty = 1.0;
 		splitPane.setResizeWeight(1.0);
 		add( splitPane, c );
 		
 		// Put the status bar at the bottom and do not adjust the size of the status bar
 		c.gridx = 0;			c.gridy = 1;
 		c.weightx = 0.0;		c.weighty = 0.0;
 				
 		// Create status bar
 		this.statusBar = enclosedLabel( "", 0, STATUS_HEIGHT, book.getForeground(), book.getBackground(), c,
 				STATUS_FONT_SIZE, JLabel.LEFT, JLabel.CENTER );
 		
 		
 		// The individual tabs
 			pages = new TriviaPanel[7];
 
 			// Create content for workflow tab
 			pages[0] = new WorkflowPanel( server, this );
 			book.addTab( "Workflow", pages[0] );
 
 			// Create content for current round tab
 			pages[1] = new RoundPanel( server, this );
 			book.addTab( "Current", pages[1] );
 
 			// Create content for history tab
 			pages[2] = new HistoryPanel( server, this );
 			book.addTab( "History", pages[2] );
 
 			// Create content for Score by Round tab
 			pages[3] = new ScoreByRoundPanel( server, this );
 			book.addTab( "By Round", pages[3] );
 
 			// Create place chart
 			pages[4] = new PlaceChartPanel( this );
 			book.addTab( "Place Chart", pages[4] );
 
 			// Create score by round chart
 			pages[5] = new ScoreByRoundChartPanel( this );
 			book.addTab( "Score Chart", pages[5] );
 
 			// Create cumulative score chart
 			pages[6] = new CumulativePointsChartPanel( this );
 			book.addTab( "Cumulative Score Chart", pages[6] );
 
 		
 		// Create timer that will poll server for changes		
 		Timer refreshTimer = new Timer( REFRESH_RATE, this );
 		refreshTimer.setActionCommand("Timer");
 		refreshTimer.start();
 		
 		// Post welcome to status bar
 		this.log( "Welcome " + this.user );
 
 	}
 
 	/**
 	 * Creates and shows the GUI.
 	 */
 	private static void createAndShowGUI() {
 		// Create the application window
		System.out.println("Creating Frame");
 		JFrame frame = new JFrame( "Trivia" );
 		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
 		// Initialize server variable
 		TriviaInterface triviaServer = null;
 		
		Properties props = System.getProperties();
		props.put("sun.rmi.transport.proxy.connectTimeout", 3000);		
		props.put("sun.rmi.transport.tcp.handshakeTimeout", 3000);		
		props.put("sun.rmi.transport.tcp.responseTimeout", 3000);		
		System.setProperties(props);

 		// Initiate connection to RMI server
 		int tryNumber = 0;
 		boolean success = false;
 		while ( tryNumber < MAX_RETRIES && success == false ) {
 			tryNumber++;
 			try {
 				// Connect to RMI server
 				triviaServer = (TriviaInterface)Naming.lookup( TRIVIA_SERVER_URL );
 			} catch ( MalformedURLException | NotBoundException | RemoteException e ) {
 				// Connection failed
 				System.out.println("Initial connection to server failed (try #" + tryNumber + ")");	
 				
 				if(tryNumber == MAX_RETRIES) {
 					// Maximum retries reached, pop up disconnected dialog
 					String message = "Could not connect to server.";
 	
 					Object[] options = { "Retry", "Exit" };
 					int option = JOptionPane.showOptionDialog( null, message, "Disconnected", JOptionPane.OK_CANCEL_OPTION,
 							JOptionPane.ERROR_MESSAGE, null, options, options[1] );
 					
 					if ( option == JOptionPane.OK_OPTION ) {
 						// Retry connection
 						tryNumber = 0;
 					} else {
 						// Exit
 						System.exit(0);
 					}
 				}
 			}
 		}
 		System.out.println( "Connected to trivia server (" + TRIVIA_SERVER_URL + ")." );
 				
 		// Initialize GUI and place in window
 		try {
 			frame.add( new TriviaClient( frame, triviaServer ), BorderLayout.CENTER );
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 		
 		// Display the window.
 		frame.pack();
 		frame.setVisible( true );
 		
 		
 		
 	}
 
 	/**
 	 * Entry point for the client application
 	 *
 	 * @param args Not used
 	 */
 	public static void main(String[] args) {
 		// Schedule a job to create and show the GUI
 		SwingUtilities.invokeLater( new Runnable() {
 			@Override
 			public void run() {
 				createAndShowGUI();
 			}
 		} );
 	}
 
 	/**
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String command = e.getActionCommand();
 		switch(command) {
 		
 		case "Timer":
 			this.update();
 			break;
 		case "Change name":
 			new UserLogin(this);
 			break;
 		case "Load state":
 			new LoadStatePrompt(this.server, this);
 			break;
 		case "Set team number":
 //			new TeamNumberPrompt(this);
 			break;
 		}
 	}
 
 	/**
 	 * Gets the user name
 	 *
 	 * @return the user name
 	 */
 	public String getUser() {
 		return this.user;
 	}
 
 	/**
 	 * Sets the user name
 	 *
 	 * @param user the new user name
 	 */
 	public void setUser(String user) {
 		this.user = user;
 	}
 
 	/**
 	 * Display message in the status bar and console
 	 *
 	 * @param message message to log
 	 */
 	public void log(String message) {
 		// Display message in status bar
 		this.statusBar.setText( message );
 		// Print message to console
 		System.out.println( "LOG: " + message );
 	}
 
 	/**
 	 * Display disconnected dialog box and prompt for action
 	 */
 	public synchronized void disconnected() {
 
 		String message = "Communication with server failed!";
 
 		Object[] options = { "Retry", "Exit" };
 		int option = JOptionPane.showOptionDialog( null, message, "Disconnected", JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.ERROR_MESSAGE, null, options, options[1] );
 		if ( option == JOptionPane.CANCEL_OPTION ) {
 			// Exit the client
 			WindowEvent wev = new WindowEvent( (JFrame)this.book.getParent(), WindowEvent.WINDOW_CLOSING );
 			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent( wev );
 			System.exit(0);
 		}
 
 	}
 	
 	public Trivia getTrivia() {
 		return this.trivia;
 	}
 
 	/**
 	 * Convert a cardinal number into its ordinal counterpart
 	 *
 	 * @param ordinal The number to convert to ordinal form
 	 * @return String with the ordinal representation of the number (e.g., 1st, 2nd, 3rd, etc.)
 	 */
 	public static String ordinalize(int ordinal) {
 		// Short-circuit for teen numbers that don't follow normal rules
 		if ( 10 < ordinal % 100 && ordinal % 100 < 14 ) { return ordinal + "th"; }
 		// Ordinal suffix depends on the ones digit
 		int modulus = ordinal % 10;
 		switch ( modulus ) {
 			case 1:
 				return ordinal + "st";
 			case 2:
 				return ordinal + "nd";
 			case 3:
 				return ordinal + "rd";
 			default:
 				return ordinal + "th";
 
 		}
 	}
 
 	/**
 	 * Update the GUI
 	 */
 	@Override
 	public synchronized void update() {
 		
 		int tryNumber = 0;
 		boolean success = false;
 		while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
 			tryNumber++;
 			try {
 				this.trivia = server.getTrivia();
 				success = true;
 			} catch ( RemoteException e ) {
 				this.log( "Couldn't retrive trivia data from server (try #" + tryNumber + ")." );
 			}
 		}
 
 		if ( !success ) {
 			this.disconnected();
 			return;
 		}
 		
 		// Update each individual tab in the GUI		
 		for ( TriviaPanel page : pages ) {
 			page.update();
 		}		
 	}
 
 }
