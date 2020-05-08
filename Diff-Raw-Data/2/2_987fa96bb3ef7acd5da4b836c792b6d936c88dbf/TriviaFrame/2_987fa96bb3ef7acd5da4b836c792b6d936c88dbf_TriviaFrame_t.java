 package net.bubbaland.trivia.client;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.Point;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Properties;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.ButtonGroup;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JSplitPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingConstants;
 import javax.swing.Timer;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import net.bubbaland.trivia.UserList.Role;
 
 /**
  * Creates a top-level window for displaying the trivia GUI.
  * 
  * @author Walter Kolczynski
  * 
  */
 public class TriviaFrame extends JFrame implements ChangeListener, ActionListener {
 
 	private static final long	serialVersionUID	= -3639363131235278472L;
 
 	// The Hide Closed menu item
 	private volatile boolean	hideClosed, hideDuplicates;
 
 	// Sort menu items
 	final private JRadioButtonMenuItem	researcherMenuItem, callerMenuItem, typistMenuItem;
 
 	final private JCheckBoxMenuItem		hideClosedMenuItem;
 	final private JCheckBoxMenuItem		hideDuplicatesMenuItem;
 	final private JMenuItem				sortTimestampAscendingMenuItem;
 	final private JMenuItem				sortTimestampDescendingMenuItem;
 	final private JMenuItem				sortQNumberAscendingMenuItem;
 	final private JMenuItem				sortQNumberDescendingMenuItem;
 	final private JMenuItem				sortStatusAscendingMenuItem;
 	final private JMenuItem				sortStatusDescendingMenuItem;
 	// The status bar at the bottom
 	final private JLabel				statusBar;
 
 	final private TriviaClient			client;
 
 	private final DnDTabbedPane			book;
 	// Sort method for the queue
 	private volatile QueueSort			queueSort;
 
 	/**
 	 * Creates a new frame based on a drag-drop event from the tabbed pane in another frame. This is done when a tab is
 	 * dragged outside of all other TriviaFrames.
 	 * 
 	 * @param client
 	 *            The root client
 	 * @param a_event
 	 *            The drag-drop event
 	 */
 	public TriviaFrame(TriviaClient client, DropTargetDropEvent a_event, Point location) {
 		this(client, false);
 		this.book.convertTab(this.book.getTabTransferData(a_event), this.book.getTargetTabIndex(a_event.getLocation()));
 		this.book.setSelectedIndex(0);
 		this.pack();
 		this.setLocation(location);
 		this.book.addChangeListener(this);
 		setCursor(null);
 	}
 
 	/**
 	 * Creates a new frame with specified tabs.
 	 * 
 	 * @param client
 	 *            The root client
 	 * @param initialTabs
 	 *            Tabs to open initially
 	 * @param showIRC
 	 *            Whether the frame should include a browser pane for the web IRC client
 	 */
 	public TriviaFrame(TriviaClient client, String[] initialTabs, boolean showIRC) {
 		this(client, showIRC);
 		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 		for (final String tabName : initialTabs) {
 			this.book.addTab(tabName, client.getTab(this, tabName));
 		}
 		this.book.setSelectedIndex(0);
 		this.book.addChangeListener(this);
 		loadPosition();
 		setCursor(null);
 	}
 
 	/**
 	 * Internal constructor containing code common to the public constructors.
 	 * 
 	 * @param client
 	 *            The root client
 	 * @param showIRC
 	 *            Whether the frame should include a browser pane for the web IRC client
 	 */
 	private TriviaFrame(TriviaClient client, boolean showIRC) {
 		super();
 		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
 		this.client = client;
 
 		final String title = client.nextWindowName();
 		this.setTitle(title);
 		this.setName(title);
 		loadPosition();
 
 		// Notify the client this frame exists
 		this.client.registerWindow(this);
 
 		// Create a new panel to hold all GUI elements for the frame
 		final TriviaMainPanel panel = new TriviaMainPanel() {
 			private static final long	serialVersionUID	= -3431542881790392652L;
 
 			@Override
 			public void update(boolean forceUpdate) {
 			}
 
 			@Override
 			protected void loadProperties(Properties properties) {
 			}
 		};
 
 
 		/**
 		 * Setup the menus
 		 */
 		{
 			final JMenuBar menuBar = new JMenuBar();
 
 			// Make Trivia Menu
 			JMenu menu = new JMenu("Trivia");
 			menu.setMnemonic(KeyEvent.VK_T);
 			menuBar.add(menu);
 
 			final JMenu roleMenu = new JMenu("Change Role...");
 			roleMenu.setMnemonic(KeyEvent.VK_R);
 
 			final ButtonGroup roleOptions = new ButtonGroup();
 			this.researcherMenuItem = new JRadioButtonMenuItem("Researcher");
 			this.researcherMenuItem.setActionCommand("Researcher");
 			this.researcherMenuItem.setMnemonic(KeyEvent.VK_R);
 			this.researcherMenuItem.addActionListener(this);
 			this.researcherMenuItem.setForeground(UserListPanel.researcherColor);
 			roleOptions.add(this.researcherMenuItem);
 			roleMenu.add(this.researcherMenuItem);
 
 			this.callerMenuItem = new JRadioButtonMenuItem("Caller");
 			this.callerMenuItem.setActionCommand("Caller");
 			this.callerMenuItem.setMnemonic(KeyEvent.VK_C);
 			this.callerMenuItem.addActionListener(this);
 			this.callerMenuItem.setSelected(false);
 			this.callerMenuItem.setForeground(UserListPanel.callerColor);
 			roleOptions.add(this.callerMenuItem);
 			roleMenu.add(this.callerMenuItem);
 
 			this.typistMenuItem = new JRadioButtonMenuItem("Typist");
 			this.typistMenuItem.setActionCommand("Typist");
 			this.typistMenuItem.setMnemonic(KeyEvent.VK_T);
 			this.typistMenuItem.addActionListener(this);
 			this.typistMenuItem.setSelected(false);
 			this.typistMenuItem.setForeground(UserListPanel.typistColor);
 			roleOptions.add(this.typistMenuItem);
 			roleMenu.add(this.typistMenuItem);
 
 			menu.add(roleMenu);
 
 			JMenuItem menuItem = new JMenuItem("Change Name", KeyEvent.VK_N);
 			menuItem.setDisplayedMnemonicIndex(7);
 			menuItem.setActionCommand("Change name");
 			menuItem.addActionListener(this);
 			menu.add(menuItem);
 
 			menuItem = new JMenuItem("Load Default Settings");
 			menuItem.setActionCommand("Load Default Settings");
 			menuItem.setMnemonic(KeyEvent.VK_D);
 			menuItem.setDisplayedMnemonicIndex(5);
 			menuItem.addActionListener(this);
 			menu.add(menuItem);
 
 			menuItem = new JMenuItem("Exit");
 			menuItem.setActionCommand("Exit");
 			menuItem.setMnemonic(KeyEvent.VK_X);
 			menuItem.addActionListener(this);
 			menu.add(menuItem);
 
 			// Make Queue Menu
 			menu = new JMenu("Queue");
 			menu.setMnemonic(KeyEvent.VK_Q);
 			menuBar.add(menu);
 
 			this.hideClosedMenuItem = new JCheckBoxMenuItem("Hide answers to closed questions");
 			this.hideClosedMenuItem.setMnemonic(KeyEvent.VK_H);
 			this.hideClosedMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
 			this.hideClosedMenuItem.setSelected(this.hideClosed);
 			this.hideClosedMenuItem.setActionCommand("Hide Closed");
 			this.hideClosedMenuItem.addActionListener(this);
 			menu.add(this.hideClosedMenuItem);
 
 			this.hideDuplicatesMenuItem = new JCheckBoxMenuItem("Hide duplicate answers");
 			this.hideDuplicatesMenuItem.setMnemonic(KeyEvent.VK_D);
 			this.hideDuplicatesMenuItem.setDisplayedMnemonicIndex(5);
 			this.hideDuplicatesMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
 			this.hideDuplicatesMenuItem.setSelected(this.hideDuplicates);
 			this.hideDuplicatesMenuItem.setActionCommand("Hide Duplicates");
 			this.hideDuplicatesMenuItem.addActionListener(this);
 			menu.add(this.hideDuplicatesMenuItem);
 
 			final JMenu sortMenu = new JMenu("Sort by...");
 			sortMenu.setMnemonic(KeyEvent.VK_S);
 
 			final JMenu timestampSort = new JMenu("Timestamp");
 			timestampSort.setMnemonic(KeyEvent.VK_T);
 			sortMenu.add(timestampSort);
 
 			final ButtonGroup sortOptions = new ButtonGroup();
 			this.sortTimestampAscendingMenuItem = new JRadioButtonMenuItem("Ascending");
 			this.sortTimestampAscendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
 					ActionEvent.CTRL_MASK));
 			this.sortTimestampAscendingMenuItem.setMnemonic(KeyEvent.VK_A);
 			this.sortTimestampAscendingMenuItem.setActionCommand("Sort Timestamp Ascending");
 			this.sortTimestampAscendingMenuItem.addActionListener(this);
 			if (this.queueSort == QueueSort.TIMESTAMP_ASCENDING) {
 				this.sortTimestampAscendingMenuItem.setSelected(true);
 			}
 			sortOptions.add(this.sortTimestampAscendingMenuItem);
 			timestampSort.add(this.sortTimestampAscendingMenuItem);
 
 			this.sortTimestampDescendingMenuItem = new JRadioButtonMenuItem("Descending");
 			this.sortTimestampDescendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
 					ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
 			this.sortTimestampDescendingMenuItem.setMnemonic(KeyEvent.VK_D);
 			this.sortTimestampDescendingMenuItem.setActionCommand("Sort Timestamp Descending");
 			this.sortTimestampDescendingMenuItem.addActionListener(this);
 			if (this.queueSort == QueueSort.TIMESTAMP_DESCENDING) {
 				this.sortTimestampDescendingMenuItem.setSelected(true);
 			}
 			sortOptions.add(this.sortTimestampDescendingMenuItem);
 			timestampSort.add(this.sortTimestampDescendingMenuItem);
 
 			final JMenu qNumberSort = new JMenu("Question Number");
 			qNumberSort.setMnemonic(KeyEvent.VK_Q);
 			sortMenu.add(qNumberSort);
 
 			this.sortQNumberAscendingMenuItem = new JRadioButtonMenuItem("Ascending");
 			this.sortQNumberAscendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
 					ActionEvent.CTRL_MASK));
 			this.sortQNumberAscendingMenuItem.setMnemonic(KeyEvent.VK_A);
 			this.sortQNumberAscendingMenuItem.setActionCommand("Sort Question Number Ascending");
 			this.sortQNumberAscendingMenuItem.addActionListener(this);
 			if (this.queueSort == QueueSort.QNUMBER_ASCENDING) {
 				this.sortQNumberAscendingMenuItem.setSelected(true);
 			}
 			sortOptions.add(this.sortQNumberAscendingMenuItem);
 			qNumberSort.add(this.sortQNumberAscendingMenuItem);
 
 			this.sortQNumberDescendingMenuItem = new JRadioButtonMenuItem("Descending");
 			this.sortQNumberDescendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
 					ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
 			this.sortQNumberDescendingMenuItem.setMnemonic(KeyEvent.VK_D);
 			this.sortQNumberDescendingMenuItem.setActionCommand("Sort Question Number Descending");
 			this.sortQNumberDescendingMenuItem.addActionListener(this);
 			if (this.queueSort == QueueSort.QNUMBER_DESCENDING) {
 				this.sortQNumberDescendingMenuItem.setSelected(true);
 			}
 			sortOptions.add(this.sortQNumberDescendingMenuItem);
 			qNumberSort.add(this.sortQNumberDescendingMenuItem);
 
 			final JMenu statusSort = new JMenu("Status");
 			statusSort.setMnemonic(KeyEvent.VK_S);
 			sortMenu.add(statusSort);
 
 			this.sortStatusAscendingMenuItem = new JRadioButtonMenuItem("Ascending");
 			this.sortStatusAscendingMenuItem.setAccelerator(KeyStroke
 					.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
 			this.sortStatusAscendingMenuItem.setMnemonic(KeyEvent.VK_A);
 			this.sortStatusAscendingMenuItem.setActionCommand("Sort Status Ascending");
 			this.sortStatusAscendingMenuItem.addActionListener(this);
 			if (this.queueSort == QueueSort.STATUS_ASCENDING) {
 				this.sortStatusAscendingMenuItem.setSelected(true);
 			}
 			sortOptions.add(this.sortStatusAscendingMenuItem);
 			statusSort.add(this.sortStatusAscendingMenuItem);
 
 			this.sortStatusDescendingMenuItem = new JRadioButtonMenuItem("Descending");
 			this.sortStatusDescendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
 					ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
 			this.sortStatusDescendingMenuItem.setMnemonic(KeyEvent.VK_D);
 			this.sortStatusDescendingMenuItem.setActionCommand("Sort Status Descending");
 			this.sortStatusDescendingMenuItem.addActionListener(this);
 			if (this.queueSort == QueueSort.STATUS_DESCENDING) {
 				this.sortStatusDescendingMenuItem.setSelected(true);
 			}
 			sortOptions.add(this.sortStatusDescendingMenuItem);
 			statusSort.add(this.sortStatusDescendingMenuItem);
 
 			menu.add(sortMenu);
 
 			// Make Info Menu
 			final JMenu infoMenu = new JMenu("Info");
 			infoMenu.setMnemonic(KeyEvent.VK_I);
			menuItem = new JMenuItem("Open Wiki (browser)", KeyEvent.VK_W);
 			menuItem.setActionCommand("Open wiki");
 			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
 			menuItem.addActionListener(this);
 
 			infoMenu.add(menuItem);
 			menuBar.add(infoMenu);
 
 			// Make Admin Menu pinned to the right
 			menuBar.add(Box.createHorizontalGlue());
 			menu = new JMenu("Admin");
 			menu.setMnemonic(KeyEvent.VK_A);
 			menuBar.add(menu);
 
 			menuItem = new JMenuItem("Load State", KeyEvent.VK_L);
 			menuItem.setActionCommand("Load state");
 			menuItem.addActionListener(this);
 			menu.add(menuItem);
 
 			this.setJMenuBar(menuBar);
 		}
 
 		// Set up layout constraints
 		final GridBagConstraints constraints = new GridBagConstraints();
 		constraints.fill = GridBagConstraints.BOTH;
 
 		// Put the status bar at the bottom and do not adjust the size of the status bar
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		constraints.weightx = 0.0;
 		constraints.weighty = 0.0;
 
 		/**
 		 * Setup status bar at bottom
 		 */
 		// Create status bar
 		this.statusBar = panel.enclosedLabel("", 0, 0, this.getForeground(), this.getBackground(), constraints, 0,
 				SwingConstants.LEFT, SwingConstants.CENTER);
 
 		// Create drag & drop tabbed pane
 		this.book = new DnDTabbedPane(this, client);
 
 		// Setup layout constraints
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		constraints.weightx = 1.0;
 		constraints.weighty = 1.0;
 
 		if (showIRC) {
 			/**
 			 * Create browser pane for IRC web client
 			 */
 			// Create panel that contains web browser for IRC
 			final String url = TriviaClient.IRC_CLIENT_URL + "?nick=" + client.getUser() + "&channels="
 					+ TriviaClient.IRC_CHANNEL;
 			final BrowserPanel browser = new BrowserPanel(url);
 			browser.setPreferredSize(new Dimension(0, 204));
 
 			/**
 			 * Create the split pane separating the tabbed pane and the broswer pane
 			 */
 			// Put the tabbed pane and browser panel in an adjustable vertical split pane
 			final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.book, browser);
 			splitPane.setResizeWeight(1.0);
 			splitPane.setBorder(BorderFactory.createEmptyBorder());
 			panel.add(splitPane, constraints);
 		} else {
 			// Add the tabbed pane to the panel
 			panel.add(this.book, constraints);
 		}
 
 		// Add the panel to the frame and display the frame
 		this.add(panel);
 		this.setVisible(true);
 
 		// Load the properties
 		this.loadProperties();
 	}
 
 	/**
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		final String command = e.getActionCommand();
 		switch (command) {
 			case "Change name":
 				// Triggered by change name, prompt for new name
 				new UserLoginDialog(this.client);
 				break;
 			case "Load state":
 				// Triggered by change state, prompt for save file
 				new LoadStateDialog(this.client);
 				break;
 			case "Hide Closed":
 				// Triggered by change to Hide Closed menu item
 				this.hideClosed = ( (JCheckBoxMenuItem) e.getSource() ).isSelected();
 				this.update(true);
 				break;
 			case "Hide Duplicates":
 				// Triggered by change to Hide Closed menu item
 				this.hideDuplicates = ( (JCheckBoxMenuItem) e.getSource() ).isSelected();
 				this.update(true);
 				break;
 			case "Sort Timestamp Ascending":
 				// Triggered by Timestamp Sort menu item
 				this.setSort(QueueSort.TIMESTAMP_ASCENDING);
 				this.update(true);
 				break;
 			case "Sort Question Number Ascending":
 				// Triggered by Question Number Sort menu item
 				this.setSort(QueueSort.QNUMBER_ASCENDING);
 				this.update(true);
 				break;
 			case "Sort Status Ascending":
 				// Triggered by Status Sort menu item
 				this.setSort(QueueSort.STATUS_ASCENDING);
 				this.update(true);
 				break;
 			case "Sort Timestamp Descending":
 				// Triggered by Timestamp Sort menu item
 				this.setSort(QueueSort.TIMESTAMP_DESCENDING);
 				this.update(true);
 				break;
 			case "Sort Question Number Descending":
 				// Triggered by Question Number Sort menu item
 				this.setSort(QueueSort.QNUMBER_DESCENDING);
 				this.update(true);
 				break;
 			case "Sort Status Descending":
 				// Triggered by Status Sort menu item
 				this.setSort(QueueSort.STATUS_DESCENDING);
 				this.update(true);
 				break;
 			case "Caller":
 				// Triggered by Caller Role menu item
 				this.client.setRole(Role.CALLER);
 				break;
 			case "Typist":
 				// Triggered by Typist Role menu item
 				this.client.setRole(Role.TYPIST);
 				break;
 			case "Researcher":
 				// Triggered by Researcher Role menu item
 				this.client.setRole(Role.RESEARCHER);
 				break;
 			case "Load Default Settings":
 				// Triggered by Reset window positions menu item
 				this.client.resetProperties();
 				break;
 			case "Open wiki":
 				// Triggered by Open wiki menu item
 				try {
 					Desktop.getDesktop().browse(new URI(TriviaClient.WIKI_URL));
 				} catch (IOException | URISyntaxException exception) {
 					this.log("Couldn't open a browser window");
 				}
 				break;
 			case "Exit":
 				// Tell client to exit the program
 				this.client.endProgram();
 				break;
 		}
 	}
 
 	/**
 	 * Get the root client.
 	 * 
 	 * @return The root client
 	 */
 	public TriviaClient getClient() {
 		return this.client;
 	}
 
 	/**
 	 * Get the answer queue sort method.
 	 * 
 	 * @return The sort method
 	 */
 	public QueueSort getQueueSort() {
 		return this.queueSort;
 	}
 
 	/**
 	 * Get the tabbed content pane.
 	 * 
 	 * @return The tabbed content pane
 	 */
 	public DnDTabbedPane getTabbedPane() {
 		return this.book;
 	}
 
 	/**
 	 * Get whether answers to closed questions should be hidden in the answer queue.
 	 * 
 	 * @return Whether answers to closed questions should be hidden
 	 */
 	public boolean hideClosed() {
 		return this.hideClosed;
 	}
 
 	/**
 	 * Get whether duplicate answers should be hidden in the answer queue.
 	 * 
 	 * @return Whether duplicate answers should be hidden
 	 */
 	public boolean hideDuplicates() {
 		return this.hideDuplicates;
 	}
 
 	/**
 	 * Load all of the properties from the client and apply them.
 	 */
 	public void loadProperties() {
 		final String id = this.getTitle();
 
 		// Load hide options
 		this.hideClosed = Boolean.parseBoolean(this.loadProperty(id, "HideClosed"));
 		this.hideClosedMenuItem.setSelected(this.hideClosed);
 		this.hideDuplicates = Boolean.parseBoolean(this.loadProperty(id, "HideDuplicates"));
 		this.hideDuplicatesMenuItem.setSelected(this.hideClosed);
 
 		// Load queue sort method
 		switch (this.loadProperty(id, "QueueSort")) {
 			case "Sort Timestamp Ascending":
 				this.setSort(QueueSort.TIMESTAMP_ASCENDING);
 				break;
 			case "Sort Question Number Ascending":
 				this.setSort(QueueSort.QNUMBER_ASCENDING);
 				break;
 			case "Sort Status Ascending":
 				this.setSort(QueueSort.STATUS_ASCENDING);
 				break;
 			case "Sort Timestamp Descending":
 				this.setSort(QueueSort.TIMESTAMP_DESCENDING);
 				break;
 			case "Sort Question Number Descending":
 				this.setSort(QueueSort.QNUMBER_DESCENDING);
 				break;
 			case "Sort Status Descending":
 				this.setSort(QueueSort.STATUS_DESCENDING);
 				break;
 			default:
 				this.setSort(QueueSort.TIMESTAMP_ASCENDING);
 				break;
 		}
 
 		// Apply to status bar
 		final int height = Integer.parseInt(this.loadProperty(id, "StatusBar.Height"));
 		final float fontSize = Float.parseFloat(this.loadProperty(id, "StatusBar.FontSize"));
 		this.statusBar.getParent().setPreferredSize(new Dimension(0, height));
 		this.statusBar.setFont(this.statusBar.getFont().deriveFont(fontSize));
 
 		// Apply colors to role menu items
 		this.researcherMenuItem.setForeground(new Color(new BigInteger(TriviaClient.PROPERTIES
 				.getProperty("UserList.Researcher.Color"), 16).intValue()));
 		this.callerMenuItem.setForeground(new Color(new BigInteger(TriviaClient.PROPERTIES
 				.getProperty("UserList.Caller.Color"), 16).intValue()));
 		this.typistMenuItem.setForeground(new Color(new BigInteger(TriviaClient.PROPERTIES
 				.getProperty("UserList.Typist.Color"), 16).intValue()));
 
 		// Tell all of the tabs to reload the properties
 		for (final String tabName : this.book.getTabNames()) {
 			final int index = this.book.indexOfTab(tabName);
 			final Component component = this.book.getComponentAt(index);
 			if (component instanceof TriviaMainPanel) {
 				( (TriviaMainPanel) this.book.getComponentAt(index) ).loadProperties(TriviaClient.PROPERTIES);
 			}
 		}
 	}
 
 	/**
 	 * Display message in the status bar and in console
 	 * 
 	 * @param message
 	 *            Message to log
 	 */
 	public void log(String message) {
 		// Display message in status bar
 		this.statusBar.setText(message);
 		// Print message to console
 		// System.out.println("LOG: " + message);
 	}
 
 	/**
 	 * Set the answer queue sort method and save to the settings file.
 	 * 
 	 * @param newSort
 	 *            The new sort method
 	 */
 	public void setSort(QueueSort newSort) {
 		this.queueSort = newSort;
 		switch (newSort) {
 			case TIMESTAMP_ASCENDING:
 				this.sortTimestampAscendingMenuItem.setSelected(true);
 				break;
 			case TIMESTAMP_DESCENDING:
 				this.sortTimestampDescendingMenuItem.setSelected(true);
 				break;
 			case QNUMBER_ASCENDING:
 				this.sortQNumberAscendingMenuItem.setSelected(true);
 				break;
 			case QNUMBER_DESCENDING:
 				this.sortQNumberDescendingMenuItem.setSelected(true);
 				break;
 			case STATUS_ASCENDING:
 				this.sortStatusAscendingMenuItem.setSelected(true);
 				break;
 			case STATUS_DESCENDING:
 				this.sortStatusDescendingMenuItem.setSelected(true);
 				break;
 		}
 	}
 
 	@Override
 	public void stateChanged(ChangeEvent e) {
 		if (e.getSource().equals(this.book)) {
 			if (this.book.getTabCount() == 1) {
 				// If there are no tabs left, hide the frame
 				this.setVisible(false);
 				// Wait 100 ms to see if the tab is added back, then close if there are still no tabs
 				final Timer timer = new Timer(100, new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						if (!TriviaFrame.this.isVisible()) {
 							DnDTabbedPane.unregisterTabbedPane(TriviaFrame.this.book);
 							TriviaFrame.this.client.unregisterWindow(TriviaFrame.this);
 							TriviaFrame.this.dispose();
 						}
 					}
 				});
 				timer.setRepeats(false);
 				timer.start();
 			} else {
 				this.setVisible(true);
 			}
 		}
 	}
 
 	public void update(boolean forceUpdate) {
 		// Update role
 		final Role role = this.client.getRole();
 		switch (role) {
 			case RESEARCHER:
 				this.researcherMenuItem.setSelected(true);
 				break;
 			case CALLER:
 				this.callerMenuItem.setSelected(true);
 				break;
 			case TYPIST:
 				this.typistMenuItem.setSelected(true);
 				break;
 			default:
 				break;
 		}
 		// Propagate update to tabs
 		for (final String tabName : this.book.getTabNames()) {
 			final int index = this.book.indexOfTab(tabName);
 			final Component component = this.book.getComponentAt(index);
 			if (component instanceof TriviaMainPanel) {
 				( (TriviaMainPanel) this.book.getComponentAt(index) ).update(forceUpdate);
 			}
 		}
 	}
 
 	/**
 	 * Save properties.
 	 */
 	protected void saveProperties() {
 		final String id = this.getTitle();
 		TriviaClient.PROPERTIES.setProperty(id + "." + "HideClosed", this.hideClosed + "");
 		TriviaClient.PROPERTIES.setProperty(id + "." + "HideDuplicates", this.hideDuplicates + "");
 		final int height = this.statusBar.getPreferredSize().getSize().height;
 		final float fontSize = this.statusBar.getFont().getSize2D();
 		TriviaClient.PROPERTIES.setProperty(id + "." + "StatusBar.Height", height + "");
 		TriviaClient.PROPERTIES.setProperty(id + "." + "StatusBar.FontSize", fontSize + "");
 		TriviaClient.PROPERTIES.setProperty(id + "." + "OpenTabs", this.book.getTabNames().toString());
 	}
 
 	/**
 	 * Load property for this window name. First looks for property specific to this iteration of TriviaFrame, then
 	 * looks to the default version.
 	 * 
 	 * @param id
 	 *            The frame's name
 	 * @param propertyName
 	 *            The property name
 	 * @return The property requested
 	 */
 	private String loadProperty(String id, String propertyName) {
 		return TriviaClient.PROPERTIES.getProperty(id + "." + propertyName,
 				TriviaClient.PROPERTIES.getProperty(propertyName));
 	}
 
 	// Queue sort option
 	public static enum QueueSort {
 		TIMESTAMP_ASCENDING, QNUMBER_ASCENDING, STATUS_ASCENDING, TIMESTAMP_DESCENDING, QNUMBER_DESCENDING, STATUS_DESCENDING
 	}
 
 	/**
 	 * Load the saved position and size of the window from file. If none found, use preferred size of components.
 	 * 
 	 */
 	protected void loadPosition() {
 		try {
 			final String frameID = this.getName();
 
 			final int x = Integer.parseInt(TriviaClient.PROPERTIES.getProperty(frameID + ".X"));
 			final int y = Integer.parseInt(TriviaClient.PROPERTIES.getProperty(frameID + ".Y"));
 			final int width = Integer.parseInt(TriviaClient.PROPERTIES.getProperty(frameID + ".Width"));
 			final int height = Integer.parseInt(TriviaClient.PROPERTIES.getProperty(frameID + ".Height"));
 
 			this.setBounds(x, y, width, height);
 
 		} catch (final NumberFormatException e) {
 			// System.out.println("Couldn't load window position, may not exist yet.");
 			if (this.book == null) {
 				this.setSize(600, 600);
 			} else {
 				this.pack();
 			}
 			this.setLocationRelativeTo(null);
 		}
 	}
 
 
 }
