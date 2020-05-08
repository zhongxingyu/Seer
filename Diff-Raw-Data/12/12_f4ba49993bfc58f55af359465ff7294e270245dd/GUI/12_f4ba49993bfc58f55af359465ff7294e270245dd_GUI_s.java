 package be.ac.ua.comp.scarletnebula.gui;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.Collection;
 import java.util.Random;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 import javax.swing.OverlayLayout;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.compute.VmState;
 
 import be.ac.ua.comp.scarletnebula.core.CloudManager;
 import be.ac.ua.comp.scarletnebula.core.CloudProvider;
 import be.ac.ua.comp.scarletnebula.core.Server;
 import be.ac.ua.comp.scarletnebula.core.ServerChangedObserver;
 import be.ac.ua.comp.scarletnebula.core.ServerDisappearedException;
 import be.ac.ua.comp.scarletnebula.core.ServerLinkUnlinkObserver;
 import be.ac.ua.comp.scarletnebula.gui.ServerListModel.CreateNewServerServer;
 import be.ac.ua.comp.scarletnebula.gui.addproviderwizard.AddProviderWizard;
 import be.ac.ua.comp.scarletnebula.gui.addserverwizard.AddServerWizard;
 import be.ac.ua.comp.scarletnebula.gui.addserverwizard.AddServerWizardDataRecorder;
 import be.ac.ua.comp.scarletnebula.gui.welcomewizard.WelcomeWizard;
 import be.ac.ua.comp.scarletnebula.misc.Utils;
 
 public class GUI extends JFrame implements ListSelectionListener,
 		ServerChangedObserver, ServerLinkUnlinkObserver
 {
 	private static Log log = LogFactory.getLog(GUI.class);
 
 	private static final long serialVersionUID = 1L;
 	private ServerList serverList;
 	private ServerListModel serverListModel;
 
 	private Statusbar statusbar = new Statusbar();
 
 	private final JPanel searchPanel = new JPanel(new FlowLayout());
 
 	private final JTextField filterTextField = new JTextField(15);
 
 	public GUI()
 	{
 		chooseLookAndFeel();
 
 		addToolbar();
 
 		final JPanel mainPanel = getMainPanel();
 
 		getContentPane().add(mainPanel);
 
 		setTitle("Scarlet Nebula");
 		setSize(700, 400);
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		ImageIcon icon = new ImageIcon(getClass().getResource(
 				"/images/icon48.png"));
 		setIconImage(icon.getImage());
 
 		addMenubar();
 		setKeyboardAccelerators();
 
 		// Last but not least, we construct a cloudmanager object, which will
 		// cause it to load all providers and thus servers.
 		// We also register ourselves as an observer for linking and unlinking
 		// servers so we can update the serverlist.
 		CloudManager.get().addServerLinkUnlinkObserver(this);
 		try
 		{
 			CloudManager.get().loadAllLinkedServers();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 		// If there are no linked cloudproviders, start the new provider wizard
 		if (CloudManager.get().getLinkedCloudProviders().size() == 0)
 		{
			Thread t = new Thread()
 			{
 				@Override
				public void run()
 				{
 					new WelcomeWizard(GUI.this);
 				}
 			};
 
			t.start();
 		}
 	}
 
 	private JPanel getMainPanel()
 	{
 		JPanel mainPanel = new JPanel()
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public boolean isOptimizedDrawingEnabled()
 			{
 				return false;
 			}
 		};
 		mainPanel.setLayout(new OverlayLayout(mainPanel));
 
 		final JPanel serverListPanel = getServerListPanel();
 		final JPanel overlayPanel = getOverlayPanel();
 
 		mainPanel.add(overlayPanel);
 		mainPanel.add(serverListPanel);
 		return mainPanel;
 	}
 
 	private JPanel getOverlayPanel()
 	{
 		final JPanel overlayPanel = new JPanel();
 		overlayPanel.setOpaque(false);
 		overlayPanel.setLayout(new GridBagLayout());
 		filterTextField.addKeyListener(new KeyListener()
 		{
 			@Override
 			public void keyTyped(KeyEvent e)
 			{
 
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e)
 			{
 			}
 
 			@Override
 			public void keyPressed(KeyEvent e)
 			{
 				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
 					hideFilter();
 			}
 		});
 		filterTextField.getDocument().addDocumentListener(
 				new DocumentListener()
 				{
 					@Override
 					public void removeUpdate(DocumentEvent e)
 					{
 						textChanged();
 
 					}
 
 					@Override
 					public void insertUpdate(DocumentEvent e)
 					{
 						textChanged();
 					}
 
 					@Override
 					public void changedUpdate(DocumentEvent e)
 					{
 
 					}
 
 					private void textChanged()
 					{
 						serverListModel.filter(filterTextField.getText());
 					}
 				});
 
 		SearchField searchField = new SearchField(filterTextField);
 
 		ImageIcon closeIcon = (ImageIcon) Utils.icon("cross16.png");
 		JButton closeButton = new JButton(closeIcon);
 		closeButton.setBounds(10, 10, closeIcon.getIconWidth(),
 				closeIcon.getIconHeight());
 		closeButton.setMargin(new Insets(0, 0, 0, 0));
 		closeButton.setOpaque(false);
 		closeButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
 		closeButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				hideFilter();
 			}
 		});
 
 		searchPanel.add(searchField);
 		searchPanel.add(closeButton);
 		// searchPanel.setBorder(BorderFactory
 		// .createBevelBorder(BevelBorder.RAISED));
 		searchPanel.setBorder(BorderFactory.createEtchedBorder());
 		searchPanel.setVisible(false);
 		searchPanel.setAlignmentX(RIGHT_ALIGNMENT);
 
 		GridBagConstraints c = new GridBagConstraints();
 		c.anchor = GridBagConstraints.LAST_LINE_END;
 		c.fill = GridBagConstraints.NONE;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1.0;
 		c.weighty = 1.0;
 		c.insets = new Insets(3, 3, 3, 3);
 		overlayPanel.add(searchPanel, c);
 		return overlayPanel;
 	}
 
 	protected void hideFilter()
 	{
 		searchPanel.setVisible(false);
 		filterTextField.setText("");
 	}
 
 	private void showFilter()
 	{
 		searchPanel.setVisible(true);
 		filterTextField.requestFocusInWindow();
 	}
 
 	private void chooseLookAndFeel()
 	{
 		try
 		{
 			boolean found = false;
 			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
 			{
 				if ("Nimbus".equals(info.getName()))
 				{
 					UIManager.setLookAndFeel(info.getClassName());
 					found = true;
 					break;
 				}
 			}
 			if (!found)
 				UIManager.setLookAndFeel(UIManager
 						.getSystemLookAndFeelClassName());
 		}
 		catch (Exception e)
 		{
 			log.error("Cannot set look and feel", e);
 		}
 	}
 
 	private void setKeyboardAccelerators()
 	{
 		serverList.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
 				KeyStroke.getKeyStroke("control F"), "search");
 		serverList.getActionMap().put("search", new AbstractAction()
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				showFilter();
 			}
 		});
 	}
 
 	private void addToolbar()
 	{
 		JToolBar toolbar = new JToolBar();
 
 		Icon addIcon = Utils.icon("add16.png");
 
 		JButton addButton = new JButton(addIcon);
 		addButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				startAddServerWizard();
 			}
 		});
 		addButton.setBounds(10, 10, addIcon.getIconWidth(),
 				addIcon.getIconHeight());
 
 		Icon refreshIcon = Utils.icon("refresh16.png");
 		JButton refreshButton = new JButton(refreshIcon);
 		refreshButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				refreshSelectedServers();
 			}
 		});
 		refreshButton.setBounds(10, 10, refreshIcon.getIconWidth(),
 				refreshIcon.getIconHeight());
 
 		Icon searchIcon = Utils.icon("search16.png");
 		JButton searchButton = new JButton(searchIcon);
 		searchButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				showFilter();
 			}
 		});
 		searchButton.setBounds(10, 10, searchIcon.getIconWidth(),
 				searchIcon.getIconHeight());
 
 		toolbar.add(addButton);
 		toolbar.add(refreshButton);
 		toolbar.add(searchButton);
 		toolbar.setFloatable(false);
 		add(toolbar, BorderLayout.PAGE_START);
 	}
 
 	private void adjustStatusbar()
 	{
 		statusbar.setText("x servers running");
 	}
 
 	private void addMenubar()
 	{
 		JMenuBar menuBar = new JMenuBar();
 		JMenu providerMenu = getProviderMenu();
 		JMenu serverMenu = getServerMenu();
 		JMenu helpMenu = getHelpMenu();
 
 		menuBar.add(providerMenu);
 		menuBar.add(serverMenu);
 
 		menuBar.add(helpMenu);
 
 		setJMenuBar(menuBar);
 	}
 
 	private JMenu getServerMenu()
 	{
 		JMenu serverMenu = new JMenu("Servers");
 		serverMenu.setMnemonic(KeyEvent.VK_S);
 
 		JMenuItem startServerItem = new JMenuItem("Start new server",
 				Utils.icon("add16.png"));
 		startServerItem.setMnemonic(KeyEvent.VK_S);
 		startServerItem.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				startAddServerWizard();
 			}
 		});
 		serverMenu.add(startServerItem);
 
 		JMenuItem searchServerItem = new JMenuItem("Filter servers",
 				Utils.icon("search16.png"));
 		searchServerItem.setMnemonic(KeyEvent.VK_F);
 		searchServerItem.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				showFilter();
 			}
 		});
 		serverMenu.add(searchServerItem);
 		return serverMenu;
 	}
 
 	private JMenu getHelpMenu()
 	{
 		JMenu helpMenu = new JMenu("Help");
 		helpMenu.setMnemonic(KeyEvent.VK_H);
 
 		// Pick a random message to display in the help menu
 		String messages[] = { "(You won't find any help here)",
 				"(Nobody can help you)", "(Keep on lookin' if you need help)",
 				"(Heeeeelp!)", "(You might want to try google for help)",
 				"(Try yelling loudly if you need help)" };
 
 		Random generator = new Random(System.currentTimeMillis());
 
 		JMenuItem noHelpItem = new JMenuItem(
 				messages[generator.nextInt(messages.length)]);
 		noHelpItem.setEnabled(false);
 		helpMenu.add(noHelpItem);
 
 		JMenuItem aboutItem = new JMenuItem("About...");
 		aboutItem.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				openAboutBox();
 			}
 		});
 
 		helpMenu.add(aboutItem);
 		return helpMenu;
 	}
 
 	private JMenu getProviderMenu()
 	{
 		JMenu providerMenu = new JMenu("Providers");
 		providerMenu.setMnemonic(KeyEvent.VK_P);
 		providerMenu.getAccessibleContext().setAccessibleDescription(
 				"Managing cloud providers.");
 
 		JMenuItem manageProvidersItem = new JMenuItem("Manage Providers");
 		manageProvidersItem.setMnemonic(KeyEvent.VK_M);
 		manageProvidersItem.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				new ManageProvidersWindow(GUI.this);
 			}
 		});
 
 		providerMenu.add(manageProvidersItem);
 
 		JMenuItem detectAllUnlinkedInstances = new JMenuItem(
 				"Link/Unlink Instances");
 		detectAllUnlinkedInstances.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				new LinkUnlinkWindow(GUI.this);
 				// detectAllUnlinkedInstances();
 			}
 		});
 		providerMenu.add(detectAllUnlinkedInstances);
 		return providerMenu;
 	}
 
 	protected void clearSelection()
 	{
 		serverList.clearSelection();
 		// fillRightPartition();
 	}
 
 	/**
 	 * The method you should call when you want to keep refreshing until Server
 	 * "server" has state "state".
 	 * 
 	 * TODO Keep some kind of a map for each state, which can be checked whem
 	 * manually refreshing. Suppose a server is refreshed and it's state is
 	 * PAUSED. The user can then resume. Later, the server's timer that checks
 	 * server state will fire, and the state will show as RUNNING. The timer
 	 * will keep on firing until the count is high enough and it gives up, which
 	 * sucks. Therefore, when manually refreshing a server S, all timers for
 	 * that server should be checked. If there's a timer waiting for S's current
 	 * state, that timer should be cancelled.
 	 * 
 	 * @param server
 	 * @param state
 	 */
 	private void refreshUntilServerHasState(final Server server,
 			final VmState state)
 	{
 		refreshUntilServerHasState(server, state, 1);
 	}
 
 	private void refreshUntilServerHasState(final Server server,
 			final VmState state, final int attempt)
 	{
 		if (server.getStatus() == state || attempt > 20)
 			return;
 
 		try
 		{
 			server.refresh();
 		}
 		catch (InternalException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (CloudException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (ServerDisappearedException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		if (server.getStatus() == state)
 			return;
 
 		// If the server's state still isn't the one we want it to be, try
 		// again, but only after waiting
 		// a logarithmic amount of time.
 		double wait = 15.0 * (Math.log10(attempt) + 1.0);
 
 		java.util.Timer timer = new java.util.Timer();
 		timer.schedule(new java.util.TimerTask()
 		{
 			@Override
 			public void run()
 			{
 				refreshUntilServerHasState(server, state, attempt + 1);
 				log.debug("Refreshing state for server "
 						+ server.getFriendlyName()
 						+ " because timer fired, waiting for state "
 						+ state.toString());
 				cancel();
 			}
 		}, (long) (wait * 1000));
 
 	}
 
 	// TODO: Remove this ftion?
 	protected void detectAllUnlinkedInstances()
 	{
 		Collection<CloudProvider> providers = CloudManager.get()
 				.getLinkedCloudProviders();
 
 		for (final CloudProvider prov : providers)
 		{
 			try
 			{
 				Collection<Server> unlinkedServers = prov.listUnlinkedServers();
 				log.debug("Provider " + prov.getName() + " has "
 						+ unlinkedServers.size() + " unlinked instances.");
 
 				for (Server server : unlinkedServers)
 				{
 					prov.linkUnlinkedServer(server);
 					addServer(server);
 				}
 			}
 			catch (Exception e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		// fillRightPartition();
 	}
 
 	private void openAboutBox()
 	{
 		AboutWindow aboutWindow = new AboutWindow(this);
 		aboutWindow.setVisible(true);
 	}
 
 	protected void terminateSelectedServers()
 	{
 		Collection<Server> servers = getSelectedServers();
 
 		for (Server server : servers)
 		{
 			try
 			{
 				server.terminate();
 				refreshUntilServerHasState(server, VmState.TERMINATED);
 			}
 			catch (CloudException e)
 			{
 				e.printStackTrace();
 			}
 			catch (InternalException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private JPanel getServerListPanel()
 	{
 		// Create the list and put it in a scroll pane.
 		serverListModel = new ServerListModel(
 				CreateNewServerServer.DISPLAY_NEW_SERVER);
 		serverList = new ServerList(serverListModel);
 		// serverList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 		serverList.addMouseListener(new ServerListMouseListener(this,
 				serverListModel));
 		serverList.addListSelectionListener(this);
 		serverList.requestFocusInWindow();
 
 		final JScrollPane serverScrollPane = new JScrollPane(serverList);
 
 		/*
 		 * JTextField searchField = new JTextField(10); SearchFieldListener
 		 * searchFieldListener = new SearchFieldListener( searchField,
 		 * serverListModel); searchField.addActionListener(searchFieldListener);
 		 * searchField.getDocument().addDocumentListener(searchFieldListener);
 		 * 
 		 * JPanel topLeftPane = new JPanel(); topLeftPane.setLayout(new
 		 * BoxLayout(topLeftPane, BoxLayout.LINE_AXIS));
 		 * topLeftPane.add(searchField);
 		 */
 
 		JPanel leftPanel = new JPanel();
 		leftPanel.setLayout(new BorderLayout());
 
 		// leftPanel.add(topLeftPane, BorderLayout.PAGE_START);
 		leftPanel.add(serverScrollPane, BorderLayout.CENTER);
 
 		return leftPanel;
 	}
 
 	protected void refreshSelectedServers()
 	{
 		int indices[] = serverList.getSelectedIndices();
 
 		Collection<Server> servers = serverListModel
 				.getVisibleServersAtIndices(indices);
 
 		// fillRightPartition();
 
 		for (Server server : servers)
 		{
 			try
 			{
 				server.refresh();
 			}
 			catch (InternalException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (CloudException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (ServerDisappearedException e)
 			{
 				log.info(e.toString());
 				removeServer(server);
 			}
 		}
 	}
 
 	@Override
 	public void valueChanged(ListSelectionEvent e)
 	{
 		if (e.getValueIsAdjusting() == false)
 		{
 			// fillRightPartition();
 		}
 	}
 
 	/*
 	 * private void fillRightPartition() { int selectedIndex =
 	 * serverList.getSelectedIndex();
 	 * 
 	 * // When nothing is selected, return
 	 * 
 	 * log.debug("Filling right partition"); Collection<Server> selectedServers
 	 * = new ArrayList<Server>();
 	 * 
 	 * if (selectedIndex >= 0) selectedServers.add(serverListModel
 	 * .getVisibleServerAtIndex(selectedIndex));
 	 * 
 	 * updateOverviewTab(selectedServers);
 	 * updateCommunicationTab(selectedServers); }
 	 */
 
 	void startAddServerWizard()
 	{
 		if (CloudManager.get().getLinkedCloudProviders().size() == 0)
 		{
 			JOptionPane
 					.showMessageDialog(
 							this,
 							"A CloudProvider account is required to add a new server, \n"
 									+ "and there don't seem to be any. Please add one before continuing.",
 							"No CloudProvider accounts found",
 							JOptionPane.ERROR_MESSAGE);
 
 			AddProviderWizard wiz = new AddProviderWizard();
 			wiz.startModal(this);
 			return;
 		}
 		new AddServerWizard(this, this);
 	}
 
 	public static void main(String[] args)
 	{
 		SwingUtilities.invokeLater(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				GUI ex = new GUI();
 				ex.setVisible(true);
 			}
 		});
 	}
 
 	public void error(String errorMessage)
 	{
 		JOptionPane.showMessageDialog(this, errorMessage,
 				"An unexpected error occured.", JOptionPane.ERROR_MESSAGE);
 	}
 
 	public void addServerWizardClosed(AddServerWizardDataRecorder rec)
 	{
 		final String instancename = rec.instanceName;
 		final String instancesize = rec.instanceSize;
 		final String image = rec.image;
 		final CloudProvider provider = rec.provider;
 		final Collection<String> tags = rec.tags;
 
 		if (Server.exists(instancename))
 		{
 			JOptionPane.showMessageDialog(this,
 					"A server with this name already exists.",
 					"Server already exists", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		try
 		{
 			Server server = provider.startServer(instancename, instancesize,
 					image, tags);
 			refreshUntilServerHasState(server, VmState.RUNNING);
 		}
 		catch (InternalException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (CloudException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void addServer(Server server)
 	{
 		serverListModel.addServer(server);
 		server.addServerChangedObserver(this);
 	}
 
 	@Override
 	public void serverChanged(Server server)
 	{
 		// Update the list on the left
 		serverListModel.refreshServer(server);
 		// fillRightPartition();
 	}
 
 	public void pauseSelectedServers()
 	{
 		Collection<Server> selectedServers = getSelectedServers();
 
 		try
 		{
 			for (Server server : selectedServers)
 			{
 				server.pause();
 				refreshUntilServerHasState(server, VmState.PAUSED);
 			}
 		}
 		catch (CloudException e)
 		{
 			error(e.getMessage());
 		}
 		catch (InternalException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void rebootSelectedServers()
 	{
 		Collection<Server> selectedServers = getSelectedServers();
 
 		try
 		{
 			for (Server server : selectedServers)
 				server.reboot();
 		}
 		catch (CloudException e)
 		{
 			e.printStackTrace();
 		}
 		catch (InternalException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	Collection<Server> getSelectedServers()
 	{
 		int indices[] = serverList.getSelectedIndices();
 		return serverListModel.getVisibleServersAtIndices(indices);
 	}
 
 	public void unlinkSelectedServers()
 	{
 		Collection<Server> selectedServers = getSelectedServers();
 
 		for (Server server : selectedServers)
 		{
 			// Server will be automatically removed from the view on the left
 			// because of the hooked observers
 			server.unlink();
 		}
 
 		// fillRightPartition();
 	}
 
 	private void removeServer(Server server)
 	{
 		serverListModel.removeServer(server);
 	}
 
 	@Override
 	public void serverLinked(CloudProvider cloudProvider, Server srv)
 	{
 		addServer(srv);
 	}
 
 	@Override
 	public void serverUnlinked(CloudProvider cloudProvider, Server srv)
 	{
 		removeServer(srv);
 	}
 }
