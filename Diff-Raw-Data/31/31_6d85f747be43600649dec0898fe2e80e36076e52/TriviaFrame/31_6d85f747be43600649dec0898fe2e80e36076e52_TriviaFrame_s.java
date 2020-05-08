 package net.bubbaland.trivia.client;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
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
  * 
  * 
  * @author Walter Kolczynski
  * 
  */
 public class TriviaFrame extends JFrame implements ChangeListener, ActionListener {
 
 	private static final long	serialVersionUID	= -3639363131235278472L;
 
 	// The Hide Closed menu item
 	private volatile boolean	hideClosed, hideDuplicates;
 
 	// Queue sort option
 	public static enum QueueSort {
 		TIMESTAMP_ASCENDING, QNUMBER_ASCENDING, STATUS_ASCENDING, TIMESTAMP_DESCENDING, QNUMBER_DESCENDING, STATUS_DESCENDING
 	}
 
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
 
 	public TriviaFrame(TriviaClient client, DropTargetDropEvent a_event) {
 		this(client, false);
 		this.book.convertTab(this.book.getTabTransferData(a_event), this.book.getTargetTabIndex(a_event.getLocation()));
 		TriviaClient.loadPosition(this);
 		this.book.addChangeListener(this);
 	}
 
 	public TriviaFrame(TriviaClient client, String[] initialTabs, boolean showIRC) {
 		this(client, showIRC);
 		for (String tabName : initialTabs) {
 			this.book.addTab(tabName, client.getTab(this, tabName));
 		}
 		this.book.setSelectedIndex(this.book.indexOfTab(initialTabs[0]));
 		TriviaClient.loadPosition(this);
 		this.book.addChangeListener(this);
 	}
 
 	public TriviaFrame(TriviaClient client, boolean showIRC) {
 		super();
 
 		int nWindows = client.getNTriviaWindows();
 		String title;
 		if (nWindows == 0) {
 			title = "Trivia";
 		} else {
 			title = "Trivia (" + ( nWindows ) + ")";
 		}
 		this.setTitle(title);
 		this.setName(title);
 
 		final TriviaPanel panel = new TriviaPanel() {
 			private static final long	serialVersionUID	= -3431542881790392652L;
 
 			@Override
 			public void update(boolean forceUpdate) {
 			}
 
 			protected void loadProperties() {
 			}
 		};
 
 		this.client = client;
 		this.client.registerWindow(this);
 
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
 			hideClosedMenuItem.setMnemonic(KeyEvent.VK_H);
 			hideClosedMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
 			hideClosedMenuItem.setSelected(this.hideClosed);
 			hideClosedMenuItem.setActionCommand("Hide Closed");
 			hideClosedMenuItem.addActionListener(this);
 			menu.add(hideClosedMenuItem);
 
 			this.hideDuplicatesMenuItem = new JCheckBoxMenuItem("Hide duplicate answers");
 			hideDuplicatesMenuItem.setMnemonic(KeyEvent.VK_D);
 			hideDuplicatesMenuItem.setDisplayedMnemonicIndex(5);
 			hideDuplicatesMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
 			hideDuplicatesMenuItem.setSelected(this.hideDuplicates);
 			hideDuplicatesMenuItem.setActionCommand("Hide Duplicates");
 			hideDuplicatesMenuItem.addActionListener(this);
 			menu.add(hideDuplicatesMenuItem);
 
 			final JMenu sortMenu = new JMenu("Sort by...");
 			sortMenu.setMnemonic(KeyEvent.VK_S);
 
 			final JMenu timestampSort = new JMenu("Timestamp");
 			timestampSort.setMnemonic(KeyEvent.VK_T);
 			sortMenu.add(timestampSort);
 
 			final ButtonGroup sortOptions = new ButtonGroup();
 			sortTimestampAscendingMenuItem = new JRadioButtonMenuItem("Ascending");
 			sortTimestampAscendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
 			sortTimestampAscendingMenuItem.setMnemonic(KeyEvent.VK_A);
 			sortTimestampAscendingMenuItem.setActionCommand("Sort Timestamp Ascending");
 			sortTimestampAscendingMenuItem.addActionListener(this);
 			if (this.queueSort == QueueSort.TIMESTAMP_ASCENDING) {
 				sortTimestampAscendingMenuItem.setSelected(true);
 			}
 			sortOptions.add(sortTimestampAscendingMenuItem);
 			timestampSort.add(sortTimestampAscendingMenuItem);
 
 			sortTimestampDescendingMenuItem = new JRadioButtonMenuItem("Descending");
 			sortTimestampDescendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK
 					+ ActionEvent.SHIFT_MASK));
 			sortTimestampDescendingMenuItem.setMnemonic(KeyEvent.VK_D);
 			sortTimestampDescendingMenuItem.setActionCommand("Sort Timestamp Descending");
 			sortTimestampDescendingMenuItem.addActionListener(this);
 			if (this.queueSort == QueueSort.TIMESTAMP_DESCENDING) {
 				sortTimestampDescendingMenuItem.setSelected(true);
 			}
 			sortOptions.add(sortTimestampDescendingMenuItem);
 			timestampSort.add(sortTimestampDescendingMenuItem);
 
 			final JMenu qNumberSort = new JMenu("Question Number");
 			qNumberSort.setMnemonic(KeyEvent.VK_Q);
 			sortMenu.add(qNumberSort);
 
 			sortQNumberAscendingMenuItem = new JRadioButtonMenuItem("Ascending");
 			sortQNumberAscendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
 			sortQNumberAscendingMenuItem.setMnemonic(KeyEvent.VK_A);
 			sortQNumberAscendingMenuItem.setActionCommand("Sort Question Number Ascending");
 			sortQNumberAscendingMenuItem.addActionListener(this);
 			if (this.queueSort == QueueSort.QNUMBER_ASCENDING) {
 				sortQNumberAscendingMenuItem.setSelected(true);
 			}
 			sortOptions.add(sortQNumberAscendingMenuItem);
 			qNumberSort.add(sortQNumberAscendingMenuItem);
 
 			sortQNumberDescendingMenuItem = new JRadioButtonMenuItem("Descending");
 			sortQNumberDescendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK
 					+ ActionEvent.SHIFT_MASK));
 			sortQNumberDescendingMenuItem.setMnemonic(KeyEvent.VK_D);
 			sortQNumberDescendingMenuItem.setActionCommand("Sort Question Number Descending");
 			sortQNumberDescendingMenuItem.addActionListener(this);
 			if (this.queueSort == QueueSort.QNUMBER_DESCENDING) {
 				sortQNumberDescendingMenuItem.setSelected(true);
 			}
 			sortOptions.add(sortQNumberDescendingMenuItem);
 			qNumberSort.add(sortQNumberDescendingMenuItem);
 
 			final JMenu statusSort = new JMenu("Status");
 			statusSort.setMnemonic(KeyEvent.VK_S);
 			sortMenu.add(statusSort);
 
 			sortStatusAscendingMenuItem = new JRadioButtonMenuItem("Ascending");
 			sortStatusAscendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
 			sortStatusAscendingMenuItem.setMnemonic(KeyEvent.VK_A);
 			sortStatusAscendingMenuItem.setActionCommand("Sort Status Ascending");
 			sortStatusAscendingMenuItem.addActionListener(this);
 			if (this.queueSort == QueueSort.STATUS_ASCENDING) {
 				sortStatusAscendingMenuItem.setSelected(true);
 			}
 			sortOptions.add(sortStatusAscendingMenuItem);
 			statusSort.add(sortStatusAscendingMenuItem);
 
 			sortStatusDescendingMenuItem = new JRadioButtonMenuItem("Descending");
 			sortStatusDescendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK
 					+ ActionEvent.SHIFT_MASK));
 			sortStatusDescendingMenuItem.setMnemonic(KeyEvent.VK_D);
 			sortStatusDescendingMenuItem.setActionCommand("Sort Status Descending");
 			sortStatusDescendingMenuItem.addActionListener(this);
 			if (this.queueSort == QueueSort.STATUS_DESCENDING) {
 				sortStatusDescendingMenuItem.setSelected(true);
 			}
 			sortOptions.add(sortStatusDescendingMenuItem);
 			statusSort.add(sortStatusDescendingMenuItem);
 
 			menu.add(sortMenu);
 
 			// Make Info Menu
 			final JMenu infoMenu = new JMenu("Info");
 			infoMenu.setMnemonic(KeyEvent.VK_I);
 			menuItem = new JMenuItem("Open Wiki (broswer)", KeyEvent.VK_W);
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
 		// int height = Integer.parseInt(loadProperty("StatusBar.height"));
 		// float fontSize = Float.parseFloat(loadProperty("StatusBar.fontSize"));
 		this.statusBar = panel.enclosedLabel("", 0, 0, this.getForeground(), this.getBackground(), constraints, 0,
 				SwingConstants.LEFT, SwingConstants.CENTER);
 
 		this.book = new DnDTabbedPane(this, client);
 
 
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
 			panel.add(splitPane, constraints);
 		} else {
 			panel.add(this.book, constraints);
 		}
 
 		this.add(panel);
 		this.setVisible(true);
 
		loadAllProperties();
 	}
 
 	public TriviaClient getClient() {
 		return this.client;
 	}
 
 	private String loadProperty(String id, String propertyName) {
 		return TriviaClient.PROPERTIES.getProperty(id + "." + propertyName,
 				TriviaClient.PROPERTIES.getProperty(propertyName));
 	}
 
	private void loadAllProperties() {
		loadAllProperties(this.getTitle());
 	}
 
	private void loadAllProperties(String id) {
 		this.hideClosed = Boolean.parseBoolean(loadProperty(id, "HideClosed"));
 		this.hideClosedMenuItem.setSelected(this.hideClosed);
 		this.hideDuplicates = Boolean.parseBoolean(loadProperty(id, "HideDuplicates"));
 		this.hideDuplicatesMenuItem.setSelected(this.hideClosed);
 
 		switch (loadProperty(id, "QueueSort")) {
 			case "Sort Timestamp Ascending":
 				this.queueSort = QueueSort.TIMESTAMP_ASCENDING;
 				this.sortTimestampAscendingMenuItem.setSelected(true);
 				break;
 			case "Sort Question Number Ascending":
 				this.queueSort = QueueSort.QNUMBER_ASCENDING;
 				this.sortQNumberAscendingMenuItem.setSelected(true);
 				break;
 			case "Sort Status Ascending":
 				this.queueSort = QueueSort.STATUS_ASCENDING;
 				this.sortStatusAscendingMenuItem.setSelected(true);
 				break;
 			case "Sort Timestamp Descending":
 				this.queueSort = QueueSort.TIMESTAMP_DESCENDING;
 				this.sortTimestampDescendingMenuItem.setSelected(true);
 				break;
 			case "Sort Question Number Descending":
 				this.queueSort = QueueSort.QNUMBER_DESCENDING;
 				this.sortTimestampDescendingMenuItem.setSelected(true);
 				break;
 			case "Sort Status Descending":
 				this.queueSort = QueueSort.STATUS_DESCENDING;
 				this.sortTimestampDescendingMenuItem.setSelected(true);
 				break;
 			default:
 				this.queueSort = QueueSort.TIMESTAMP_ASCENDING;
 				this.sortTimestampAscendingMenuItem.setSelected(true);
 				break;
 		}
 
 		int height = Integer.parseInt(loadProperty(id, "StatusBar.Height"));
 		float fontSize = Float.parseFloat(loadProperty(id, "StatusBar.FontSize"));
 		this.statusBar.getParent().setPreferredSize(new Dimension(0, height));
 		this.statusBar.setFont(this.statusBar.getFont().deriveFont(fontSize));
 		this.researcherMenuItem.setForeground(new Color(Integer.parseInt(
 				TriviaClient.PROPERTIES.getProperty("UserList.Researcher.Color"), 16)));
 		this.callerMenuItem.setForeground(new Color(Integer.parseInt(
 				TriviaClient.PROPERTIES.getProperty("UserList.Caller.Color"), 16)));
 		this.typistMenuItem.setForeground(new Color(Integer.parseInt(
 				TriviaClient.PROPERTIES.getProperty("UserList.Typist.Color"), 16)));
 
 		for (String tabName : this.book.getTabNames()) {
 			int index = this.book.indexOfTab(tabName);
 			Component component = this.book.getComponentAt(index);
 			if (component instanceof TriviaPanel) {
 				( (TriviaPanel) this.book.getComponentAt(index) ).loadProperties();
 			}
 		}
 	}
 
 	protected void saveProperties() {
 		String id = this.getTitle();
 		TriviaClient.PROPERTIES.setProperty(id + "." + "HideClosed", this.hideClosed + "");
 		TriviaClient.PROPERTIES.setProperty(id + "." + "HideDuplicates", this.hideDuplicates + "");
 		int height = this.statusBar.getPreferredSize().getSize().height;
 		float fontSize = this.statusBar.getFont().getSize2D();
 		TriviaClient.PROPERTIES.setProperty(id + "." + "StatusBar.Height", height + "");
 		TriviaClient.PROPERTIES.setProperty(id + "." + "StatusBar.FontSize", fontSize + "");
 		TriviaClient.PROPERTIES.setProperty(id + "." + "OpenTabs", this.book.getTabNames().toString());
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
 	 * Load the saved position and size of the window from file. If none found, use preferred size of components.
 	 * 
 	 */
 	public void loadPosition() {
 		TriviaClient.loadPosition(this);
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
 
 	/**
 	 * Get the answer queue sort method.
 	 * 
 	 * @return The sort method
 	 */
 	public QueueSort getQueueSort() {
 		return this.queueSort;
 	}
 
 	public boolean hideClosed() {
 		return this.hideClosed;
 	}
 
 	public boolean hideDuplicates() {
 		return this.hideDuplicates;
 	}
 
 	@Override
 	public void stateChanged(ChangeEvent e) {
 		if (e.getSource().equals(this.book)) {
 			if (this.book.getTabCount() == 1) {
 				// If there are no tabs left, hide the frame
 				this.setVisible(false);
 				// Wait 100 ms to see if the tab is added back, then close if there are still no tabs
 				Timer timer = new Timer(100, new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						if (!TriviaFrame.this.isVisible()) {
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
 				setSort(QueueSort.TIMESTAMP_ASCENDING);
 				this.update(true);
 				break;
 			case "Sort Question Number Ascending":
 				// Triggered by Question Number Sort menu item
 				setSort(QueueSort.QNUMBER_ASCENDING);
 				this.update(true);
 				break;
 			case "Sort Status Ascending":
 				// Triggered by Status Sort menu item
 				setSort(QueueSort.STATUS_ASCENDING);
 				this.update(true);
 				break;
 			case "Sort Timestamp Descending":
 				// Triggered by Timestamp Sort menu item
 				setSort(QueueSort.TIMESTAMP_DESCENDING);
 				this.update(true);
 				break;
 			case "Sort Question Number Descending":
 				// Triggered by Question Number Sort menu item
 				setSort(QueueSort.QNUMBER_DESCENDING);
 				this.update(true);
 				break;
 			case "Sort Status Descending":
 				// Triggered by Status Sort menu item
 				setSort(QueueSort.STATUS_DESCENDING);
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
				TriviaClient.loadDefaults();
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
 				this.client.endProgram();
 				break;
 		}
 	}
 
 	public void update(boolean forceUpdate) {
 		Role role = this.client.getRole();
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
 		for (String tabName : this.book.getTabNames()) {
 			int index = this.book.indexOfTab(tabName);
 			Component component = this.book.getComponentAt(index);
 			if (component instanceof TriviaPanel) {
 				( (TriviaPanel) this.book.getComponentAt(index) ).update(forceUpdate);
 			}
 		}
 	}
 
 
 }
