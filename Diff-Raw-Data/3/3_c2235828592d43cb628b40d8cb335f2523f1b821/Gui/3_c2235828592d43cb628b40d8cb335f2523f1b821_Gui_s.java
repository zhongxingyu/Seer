 package nl.minicom.evenexus.gui;
 
 
 import java.awt.Dimension;
 import java.awt.GraphicsEnvironment;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 import javax.inject.Singleton;
 import javax.swing.GroupLayout;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JTabbedPane;
 import javax.swing.UIManager;
 
 import nl.minicom.evenexus.gui.icons.Icon;
 import nl.minicom.evenexus.gui.panels.accounts.AccountsPanel;
 import nl.minicom.evenexus.gui.panels.dashboard.DashboardPanel;
 import nl.minicom.evenexus.gui.panels.journals.JournalsPanel;
 import nl.minicom.evenexus.gui.panels.marketorders.MarketOrdersPanel;
 import nl.minicom.evenexus.gui.panels.profit.InventoryProgressPanel;
 import nl.minicom.evenexus.gui.panels.profit.ProfitPanel;
 import nl.minicom.evenexus.gui.panels.transactions.TransactionsPanel;
 import nl.minicom.evenexus.gui.settings.SettingsDialog;
 import nl.minicom.evenexus.gui.utils.GuiListener;
 import nl.minicom.evenexus.gui.utils.dialogs.AboutDialog;
 import nl.minicom.evenexus.gui.utils.dialogs.ExportDatabaseDialog;
 import nl.minicom.evenexus.gui.utils.dialogs.ImportDatabaseDialog;
 import nl.minicom.evenexus.utils.SettingsManager;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Singleton
 public class Gui extends JFrame {
 
 	private static final long serialVersionUID = 4615845773792192362L;
 	
 	private static final Logger LOG = LoggerFactory.getLogger(Gui.class);
 	
 	private static final int MIN_HEIGHT = 400;
 	private static final int MIN_WIDTH = 700;
 	private static final int HEIGHT = 550;
 	private static final int WIDTH = 850;
 	
 	private final SettingsManager settingsManager;
 	
 	private final DashboardPanel dashboardPanel;
 	private final JournalsPanel journalsPanel;
 	private final TransactionsPanel transactionPanel;
 	private final MarketOrdersPanel marketOrderPanel;
 	private final ProfitPanel profitPanel;
 	private final AccountsPanel accountsPanel;
 	
 	private final InventoryProgressPanel state;
 	
 	private final GuiListener guiListener;
 	
 	private final Provider<ImportDatabaseDialog> importDatabaseDialogProvider;
 	private final Provider<ExportDatabaseDialog> exportDatabaseDialogProvider;
 	private final Provider<SettingsDialog> settingsDialogProvider;
 	private final Provider<AboutDialog> aboutDialogProvider;
 
 	@Inject
 	public Gui(SettingsManager settingsManager,
 			DashboardPanel dashboardPanel,
 			JournalsPanel journalsPanel,
 			TransactionsPanel transactionPanel,
 			MarketOrdersPanel marketOrderPanel,
 			ProfitPanel profitPanel,
 			AccountsPanel accountsPanel,
 			InventoryProgressPanel state,
 			GuiListener guiListener,
 			Provider<ImportDatabaseDialog> importDatabaseDialogProvider,
 			Provider<ExportDatabaseDialog> exportDatabaseDialogProvider,
 			Provider<SettingsDialog> settingsDialogProvider,
 			Provider<AboutDialog> aboutDialogProvider) {
 		
 		this.settingsManager = settingsManager;
 		this.dashboardPanel = dashboardPanel;
 		this.journalsPanel = journalsPanel;
 		this.transactionPanel = transactionPanel;
 		this.marketOrderPanel = marketOrderPanel;
 		this.profitPanel = profitPanel;
 		this.accountsPanel = accountsPanel;
 		
 		this.state = state;
 		
 		this.guiListener = guiListener;
 		
 		this.importDatabaseDialogProvider = importDatabaseDialogProvider;
 		this.exportDatabaseDialogProvider = exportDatabaseDialogProvider;
 		this.settingsDialogProvider = settingsDialogProvider;
 		this.aboutDialogProvider = aboutDialogProvider;
 	}
 	
 	public void initialize() {
 		dashboardPanel.initialize();
 		journalsPanel.initialize();
 		transactionPanel.initialize();
 		marketOrderPanel.initialize();
 		profitPanel.initialize();
 		accountsPanel.initialize();
 		
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		setTitle(getClass().getPackage().getSpecificationTitle() + " - EVE Online trading overview");
 		setIconImage(Icon.getImage("/img/other/logo.png"));
 		setSizeAndPosition();
 		setLookAndFeel();
 		
 		if (settingsManager.loadBoolean(SettingsManager.APPLICATION_MAXIMIZED, false)) {
 			setExtendedState(MAXIMIZED_BOTH);
 		}
 		
 		guiListener.setGui(this);
 		addComponentListener(guiListener);
 		addWindowStateListener(guiListener);
 		addWindowListener(guiListener);
 		
 		createGUI();
 		setVisible(true);
 	}
 
 	public void setSizeAndPosition() {
 		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		int xPos = (int) (env.getMaximumWindowBounds().getWidth() - WIDTH) / 2;
 		int yPos = (int) (env.getMaximumWindowBounds().getHeight() - HEIGHT) / 2;
 		
 		int x = settingsManager.loadInt(SettingsManager.APPLICATION_X, xPos);
 		int y = settingsManager.loadInt(SettingsManager.APPLICATION_Y, yPos);
 		int width = settingsManager.loadInt(SettingsManager.APPLICATION_WIDTH, WIDTH);
 		int height = settingsManager.loadInt(SettingsManager.APPLICATION_HEIGHT, HEIGHT);
 		
 		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
 		setBounds(x, y, width, height);
 	}
 
 	private void createGUI() {
 		setJMenuBar(createMenu());
 		
 		JTabbedPane pane = new JTabbedPane();
 		pane.setFocusable(false);
 		
 		pane.addTab("Dashboard", dashboardPanel);
 		pane.addTab("Journals", journalsPanel);
 		pane.addTab("Transactions", transactionPanel);
 		pane.addTab("Market orders", marketOrderPanel);
 		pane.addTab("Profits", profitPanel);
 		pane.addTab("Accounts", accountsPanel);
 		
 		GroupLayout layout = new GroupLayout(getContentPane());
         setLayout(layout);        
         layout.setHorizontalGroup(
         	layout.createSequentialGroup()
         		.addGap(7)
         		.addGroup(layout.createParallelGroup()
         				.addComponent(pane)
         				.addComponent(state)
         		)
         		.addGap(6)
     	);
     	layout.setVerticalGroup(
     		layout.createSequentialGroup()
 	    		.addGap(7)
 	    		.addComponent(pane)
 	    		.addComponent(state)
     	);
 	}
 
 	private JMenuBar createMenu() {
 		JMenuBar bar = new JMenuBar();
 		JMenu applicationMenu = new JMenu("Application");
 		
 		JMenuItem importMenu = new JMenuItem("Import database", Icon.getIcon("/img/16/database_down.png"));
 		applicationMenu.add(importMenu);
 		importMenu.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				importDatabaseDialogProvider.get().initialize();
 			}
 		});
 		
 		JMenuItem exportMenu = new JMenuItem("Export database", Icon.getIcon("/img/16/database_next.png"));
 		applicationMenu.add(exportMenu);
 		exportMenu.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				exportDatabaseDialogProvider.get().initialize();
 			}
 		});
 		
 		applicationMenu.addSeparator();
 		
 		JMenuItem proxyMenu = new JMenuItem("Settings", Icon.getIcon("/img/16/process.png"));
 		applicationMenu.add(proxyMenu);
 		proxyMenu.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				settingsDialogProvider.get().initialize();
 			}
 		});
 		
 		applicationMenu.addSeparator();
 		
 		JMenuItem exitMenu = new JMenuItem("Exit", Icon.getIcon("/img/16/remove.png"));
 		applicationMenu.add(exitMenu);
 		exitMenu.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				System.exit(0);
 			}
 		});
 		
 		bar.add(applicationMenu);
 		
 		JMenu helpMenu = new JMenu("Help");
 		
 		JMenuItem aboutMenu = new JMenuItem("About", Icon.getIcon("/img/16/info.png"));
 		helpMenu.add(aboutMenu);
 		aboutMenu.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				aboutDialogProvider.get().initialize();
 			}
 		});
 		
 		bar.add(helpMenu);
 		
 		return bar;
 	}
 
 	public static void setLookAndFeel() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (Exception e) {
 			LOG.error(e.getLocalizedMessage(), e);
 		}
 	}
 
 	public void reload() {
 		dashboardPanel.reloadTab();
 		journalsPanel.reloadTab();
 		transactionPanel.reloadTab();
 		marketOrderPanel.reloadTab();
 		profitPanel.reloadTab();
 		accountsPanel.reloadTab();
 	}
 	
 }
