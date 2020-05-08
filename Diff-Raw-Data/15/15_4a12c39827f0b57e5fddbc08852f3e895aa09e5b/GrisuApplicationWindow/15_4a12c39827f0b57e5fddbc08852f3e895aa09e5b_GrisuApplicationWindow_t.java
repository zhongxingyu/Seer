 package grisu.frontend.view.swing;
 
 import grisu.X;
 import grisu.control.ServiceInterface;
 import grisu.frontend.control.login.LoginManager;
 import grisu.frontend.model.events.ApplicationEventListener;
 import grisu.frontend.view.swing.jobcreation.JobCreationPanel;
 import grisu.frontend.view.swing.login.LoginPanel;
 import grisu.frontend.view.swing.login.ServiceInterfaceHolder;
 import grisu.frontend.view.swing.utils.AdvancedSettingsPanel;
 import grisu.jcommons.utils.HttpProxyPanel;
 import grisu.model.dto.GridFile;
 import grisu.settings.ClientPropertiesManager;
 import grith.gridsession.GridClient;
 import grith.gridsession.GridSessionCred;
 import grith.jgrith.cred.GridLoginParameters;
 
 import java.awt.AWTEvent;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.swing.InputMap;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 
 import org.apache.commons.lang.StringUtils;
 import org.jdesktop.swingx.JXFrame;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableList;
 import com.jgoodies.common.base.SystemUtils;
 import com.jgoodies.looks.Options;
 
 public abstract class GrisuApplicationWindow extends GridClient implements
 WindowListener, ServiceInterfaceHolder {
 
 	static final Logger myLogger = LoggerFactory
 			.getLogger(GrisuApplicationWindow.class.getName());
 	
 	public static String PANEL_TO_PRELOAD = null;
 
 	private ServiceInterface si;
 
 	private GrisuMainPanel mainPanel;
 
 	private LoginPanel lp;
 	protected final GrisuMenu menu;
 
 	private JXFrame frame;
 
 	private final HttpProxyPanel httpProxyPanel = new HttpProxyPanel();
 
 	private final Set<ServiceInterfacePanel> configPanels;
 
 	public GrisuApplicationWindow() throws Exception {
 		this((ServiceInterfacePanel) null);
 	}
 	
 	public GrisuApplicationWindow(ServiceInterfacePanel panel) throws Exception {
 		super(new GridLoginParameters());
 		initialize();
 
 		menu = new GrisuMenu(this);
 		getFrame().setJMenuBar(menu);
 
 		if (panel == null) {
 			this.configPanels = new HashSet<ServiceInterfacePanel>();
 			this.configPanels.add(new AdvancedSettingsPanel());
 			addSettingsPanel("Advanced", this.configPanels.iterator().next()
 					.getPanel());
 		} else {
 			this.configPanels = new HashSet<ServiceInterfacePanel>();
 			this.configPanels.add(panel);
 			for (final ServiceInterfacePanel p : configPanels) {
 				addSettingsPanel(p.getPanelTitle(), p.getPanel());
 			}
 		}
 		addSettingsPanel("Http proxy settings", httpProxyPanel);	
 		
 	}
 
 	/**
 	 * Launch the application.
 	 * 
 	 * @throws Exception
 	 */
 	public GrisuApplicationWindow(Set<ServiceInterfacePanel> configpanels)
 			throws Exception {
 		super(new GridLoginParameters());
 		initialize();
 
 		menu = new GrisuMenu(this);
 		getFrame().setJMenuBar(menu);
 
 		if (configpanels == null) {
 			this.configPanels = new HashSet<ServiceInterfacePanel>();
 			this.configPanels.add(new AdvancedSettingsPanel());
 			addSettingsPanel("Advanced", this.configPanels.iterator().next()
 					.getPanel());
 		} else {
 			this.configPanels = configpanels;
 			for (final ServiceInterfacePanel panel : configPanels) {
 				addSettingsPanel(panel.getPanelTitle(), panel.getPanel());
 			}
 		}
 		addSettingsPanel("Http proxy settings", httpProxyPanel);
 					
 
 	}
 
 	public void addGroupFileListPanel(List<GridFile> left, List<GridFile> right) {
 		mainPanel.addGroupFileListPanel(left, right);
 	}
 
 	public void addSettingsPanel(String name, JPanel panel) {
 		menu.addSettingsPanel(name, panel);
 	}
 
 	abstract public boolean displayAppSpecificMonitoringItems();
 	
 	abstract public boolean displayBatchJobsCreationPane();
 
 	abstract public boolean displaySingleJobsCreationPane();
 	
 	abstract public boolean displayAllJobsMonitoringItem();
 
 	public void exit(boolean deleteSession) {
 		
 		if ( deleteSession ) {
 			
 			try {
 				GridSessionCred cred = new GridSessionCred(this);
 				cred.destroy();
 			} catch (Exception e) {
 				myLogger.error("Could not destroy session.", e);
 			}
 			
 			
 		}
 		
 		exit();
 	}
 	
 	public void exit() {
 		try {
 
 			if (si != null) {
 				final String temp = si.logout();
 				X.p(temp);
 			}
 
 		} finally {
 			WindowSaver.saveSettings();
 			System.exit(0);
 		}
 	}
 
 	public Set<String> getApplicationsToMonitor() {
 
 		final Set<String> result = new TreeSet<String>();
 		for (final JobCreationPanel panel : getJobCreationPanels()) {
 			result.add(panel.getSupportedApplication());
 		}
 		return result;
 	}
 
 	public JFrame getFrame() {
 		return frame;
 	}
 
 	abstract public JobCreationPanel[] getJobCreationPanels();
 
 	abstract public String getName();
 
 	public ServiceInterface getServiceInterface() {
 		return si;
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 
 		LoginManager.initEnvironment();
 
 		new ApplicationEventListener();
 
 		final Toolkit tk = Toolkit.getDefaultToolkit();
 		tk.addAWTEventListener(WindowSaver.getInstance(),
 				AWTEvent.WINDOW_EVENT_MASK);
 
 		try {
 			myLogger.debug("Setting look and feel.");
 
 			UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
 			Options.setDefaultIconSize(new Dimension(18, 18));
 
 			String lafName = null;
 			if (SystemUtils.IS_OS_WINDOWS) {
 				lafName = Options.JGOODIES_WINDOWS_NAME;
 			} else {
 				lafName = UIManager.getSystemLookAndFeelClassName();
 			}
 
 			try {
 				myLogger.debug("Look and feel name:" + lafName);
 				UIManager.setLookAndFeel(lafName);
 			} catch (Exception e) {
 				System.err.println("Can't set look & feel:" + e);
 			}
 
 			// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (final Exception e) {
 			myLogger.error(e.getLocalizedMessage(), e);
 		}
 
 		frame = new JXFrame();
 		String title = null;
 		final String clientVersion = LoginManager.getClientVersion();
 		if (StringUtils.containsIgnoreCase(clientVersion, "snapshot")) {
 			title = getName() + "  (DEVELOPMENT VERSION)";
 		} else {
 			// title = getName() + "v" + clientVersion;
 			title = getName();
 		}
 		frame.setTitle(title);
 		frame.addWindowListener(this);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		frame.getContentPane().setLayout(new BorderLayout());
 
 		final boolean singleJobs = displaySingleJobsCreationPane();
 		//final boolean batchJobs = displayBatchJobsCreationPane();
 		final boolean batchJobs = false;
 
 		final boolean displayAppSpecificMonitoringItems = displayAppSpecificMonitoringItems();
 		final boolean displayAllJobsMontorintItems = displayAllJobsMonitoringItem();
 
 			mainPanel = new GrisuMainPanel(singleJobs, displayAllJobsMontorintItems, displayAppSpecificMonitoringItems,
 					getApplicationsToMonitor(), batchJobs, displayAllJobsMontorintItems, displayAppSpecificMonitoringItems,
 					getApplicationsToMonitor());
 
 
 		final List<ServiceInterfaceHolder> siHolders = ImmutableList
 				.of((ServiceInterfaceHolder) this);
 
 		final LoginPanel lp = new LoginPanel(mainPanel, siHolders);
 		lp.setSessionClient(this);
 		frame.getContentPane().add(lp, BorderLayout.CENTER);
 		
 //		  int condition = JComponent.WHEN_FOCUSED;
 ////		  InputMap iMap = getFrame().getRootPane().getInputMap(condition);
 //		  InputMap iMap = (InputMap)UIManager.get("Button.focusInputMap");
 //		  iMap.put( KeyStroke.getKeyStroke( "ENTER" ), "pressed" );
 //		  iMap.put( KeyStroke.getKeyStroke( "released ENTER" ), "released" );
 //		  ActionMap aMap = getFrame().getRootPane().getActionMap();
 //
 //		  String enter = "enter";
 //		  iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), enter);
 //		  aMap.put(enter, lp.getLoginPanel().getAction());		
 		  
 		  getFrame().getRootPane().setDefaultButton(lp.getLoginPanel().getLoginButton());
 		  
 		  JButton button = lp.getLoginPanel().getLoginButton();
 		  InputMap im = button.getInputMap();
 		  im.put( KeyStroke.getKeyStroke( "ENTER" ), "pressed" );
 		  im.put( KeyStroke.getKeyStroke( "released ENTER" ), "released" );
 
 	}
 
 	abstract protected void initOptionalStuff(ServiceInterface si);
 
 	public void refreshJobCreationPanels() {
 
 		mainPanel.removeAlJobCreationPanelsl();
 		for (final JobCreationPanel panel : getJobCreationPanels()) {
 			mainPanel.addJobCreationPanel(panel);
 		}
 
 	}
 
 	public void setServiceInterface(ServiceInterface si) {
 
 		this.si = si;
 		this.menu.setServiceInterface(si);
 		this.mainPanel.setServiceInterface(si);
 		initOptionalStuff(si);
 		refreshJobCreationPanels();
 
 		for (final ServiceInterfacePanel panel : configPanels) {
 			panel.setServiceInterface(si);
 		}
 		
 		String lastPanel = PANEL_TO_PRELOAD;
 
 		if ( StringUtils.isBlank(lastPanel)) {
 			lastPanel = ClientPropertiesManager.getProperty("lastCreatePanel"); 
 		}
 		if ( StringUtils.isNotBlank(lastPanel)) {
 			String[] command = lastPanel.split(",");
 			this.mainPanel.getGrisuNavigationPanel().setNavigationCommand(command);
 		}
		
		getFrame().getRootPane().setDefaultButton(null);
 
 	}
 
 	public void setServiceInterfaceExternal(ServiceInterface si) {
 
 		if (lp == null) {
 			throw new IllegalStateException("LoginPanel not initialized.");
 		}
 
 		if (si == null) {
 			throw new NullPointerException("ServiceInterface can't be null");
 		}
 		// this.si = si;
 		lp.setServiceInterface(si);
		
		getFrame().getRootPane().setDefaultButton(null);
 
 	}
 
 	public void setVisible(boolean visible) {
 		frame.setVisible(visible);
 	}
 
 	public void windowActivated(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void windowClosed(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void windowClosing(WindowEvent arg0) {
 		exit();
 	}
 
 	public void windowDeactivated(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void windowDeiconified(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void windowIconified(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void windowOpened(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
