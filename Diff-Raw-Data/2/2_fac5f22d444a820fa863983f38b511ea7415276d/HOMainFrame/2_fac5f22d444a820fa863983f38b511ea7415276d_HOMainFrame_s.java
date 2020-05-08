 // %3852537837:de.hattrickorganizer.gui%
 package de.hattrickorganizer.gui;
 
 import gui.UserParameter;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.sql.Timestamp;
 import java.text.NumberFormat;
 import java.util.Locale;
 import java.util.Properties;
 
 import javax.swing.InputMap;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JTabbedPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.event.ChangeListener;
 
 import plugins.ISpieler;
 import de.hattrickorganizer.database.DBZugriff;
 import de.hattrickorganizer.gui.arenasizer.ArenaSizerPanel;
 import de.hattrickorganizer.gui.exporter.XMLExporter;
 import de.hattrickorganizer.gui.info.InformationsPanel;
 import de.hattrickorganizer.gui.injury.InjuryDialog;
 import de.hattrickorganizer.gui.keepertool.KeeperToolDialog;
 import de.hattrickorganizer.gui.league.LigaTabellePanel;
 import de.hattrickorganizer.gui.lineup.AufstellungsAssistentPanel;
 import de.hattrickorganizer.gui.lineup.AufstellungsPanel;
 import de.hattrickorganizer.gui.matches.SpielePanel;
 import de.hattrickorganizer.gui.model.UserColumnController;
 import de.hattrickorganizer.gui.notepad.NotepadDialog;
 import de.hattrickorganizer.gui.playeranalysis.SpielerAnalyseMainPanel;
 import de.hattrickorganizer.gui.playeroverview.SpielerUebersichtsPanel;
 import de.hattrickorganizer.gui.statistic.StatistikMainPanel;
 import de.hattrickorganizer.gui.transferscout.TransferScoutPanel;
 import de.hattrickorganizer.gui.utils.OnlineWorker;
 import de.hattrickorganizer.model.FormulaFactors;
 import de.hattrickorganizer.model.HOVerwaltung;
 import de.hattrickorganizer.model.User;
 import de.hattrickorganizer.net.MyConnector;
 import de.hattrickorganizer.tools.HOLogger;
 import de.hattrickorganizer.tools.HelperWrapper;
 import de.hattrickorganizer.tools.backup.BackupHelper;
 import de.hattrickorganizer.tools.extension.ExtensionListener;
 import de.hattrickorganizer.tools.extension.FileExtensionManager;
 import de.hattrickorganizer.tools.updater.UpdateController;
 
 /**
  * Das Hauptfenster
  */
 public final class HOMainFrame extends JFrame
 	implements Refreshable, WindowListener, ActionListener, ChangeListener {
 	//~ Static fields/initializers -----------------------------------------------------------------
 
 	private static final long serialVersionUID = -6333275250973872365L;
 
 	/** TODO Missing Parameter Documentation */
 	public static final double VERSION = 1.420d;
 
 	/** TODO Missing Parameter Documentation */
 	private static final boolean DEVELOPMENT = false;
 
 	/** TODO Missing Parameter Documentation */
 	public static final int SPRACHVERSION = 2;
 	private static HOMainFrame m_clHOMainFrame;
 
 	/** TODO Missing Parameter Documentation */
 	private static final boolean LIMITED = false;
 
 	/** TODO Missing Parameter Documentation */
 	private static final String LIMITED_DATE = "2004-09-01 00:00:00.0";
 	private static java.util.Vector m_vPlugins = new java.util.Vector();
 
 	//---------Konstanten----------------------
 
 	/** TODO Missing Parameter Documentation */
 	public static final int SPIELERUEBERSICHT = 0;
 
 	/** TODO Missing Parameter Documentation */
 	public static final int AUFSTELLUNG = 1;
 
 	/** TODO Missing Parameter Documentation */
 	public static final int LIGATABELLE = 2;
 
 	/** TODO Missing Parameter Documentation */
 	public static final int SPIELE = 3;
 
 	/** TODO Missing Parameter Documentation */
 	public static final int SPIELERANALYSE = 4;
 
 	/** TODO Missing Parameter Documentation */
 	public static final int STATISTIK = 5;
 
 	/** TODO Missing Parameter Documentation */
 	public static final int TRANSFERSCOUT = 6;
 
 	/** TODO Missing Parameter Documentation */
 	public static final int ARENASIZER = 7;
 
 	/** TODO Missing Parameter Documentation */
 	public static final int INFORMATIONEN = 8;
 
 	public static final int BUSY = 0;
 	public static final int READY = 1;
 
 	private static int status = READY;
 	//~ Instance fields ----------------------------------------------------------------------------
 
 	private ArenaSizerPanel m_jpArenaSizer;
 	private AufstellungsPanel m_jpAufstellung;
 	private InfoPanel m_jpInfoPanel;
 
 	//    private TrainingsPanel          m_jpTrainingshelfer     =   null;
 	private InformationsPanel m_jpInformation;
 	private InjuryDialog injuryTool;
 	private final JMenu m_jmAbout =
 		new JMenu(HOVerwaltung.instance().getResource().getProperty("About"));
 	private final JMenu m_jmDatei =
 		new JMenu(HOVerwaltung.instance().getResource().getProperty("Datei"));
 	// disabled HOFriendly, aik, 05.03.2008
 //	private final JMenu m_jmHoFriendlyMenu =
 //		new JMenu(HOVerwaltung.instance().getResource().getProperty("HoFriendly"));
 	private final JMenu m_jmPluginMenu =
 		new JMenu(HOVerwaltung.instance().getResource().getProperty("Plugins"));
 	private final JMenu m_jmPluginsRefresh =
 		new JMenu(HOVerwaltung.instance().getResource().getProperty("Plugins"));
 	private final JMenu m_jmToolsMenu =
 		new JMenu(HOVerwaltung.instance().getResource().getProperty("Tools"));
 	private final JMenu m_jmUpdating =
 		new JMenu(HOVerwaltung.instance().getResource().getProperty("Refresh"));
 	private final JMenu m_jmVerschiedenes =
 		new JMenu(HOVerwaltung.instance().getResource().getProperty("Funktionen"));
 
 	//----Menue--------------------------------
 	private final JMenuBar m_jmMenuBar = new JMenuBar();
 	private final JMenuItem m_jmBeendenItem =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Beenden"));
 	private final JMenuItem m_jmCreditsItem =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Credits"));
 	private final JMenuItem m_jmDownloadItem =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Download"));
 	private final JMenuItem m_jmForumItem =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Forum"));
 	private final JMenuItem m_jmHattrickItem =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Hattrick"));
 //	private final JMenuItem m_jmHoFriendly =
 //		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("HoFriendly"));
 	private final JMenuItem m_jmHomepageItem =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Homepage"));
 //	private final JMenuItem m_jmIPAdresse =
 //		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("IP"));
 
 	//private JMenuItem               m_jmFixturesItem        =   new JMenuItem( model.HOVerwaltung.instance ().getResource ().getProperty("FixturesDownload") );
 	private final JMenuItem m_jmImportItem =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("HRFImportieren"));
 	private final JMenuItem m_jmOptionen =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Optionen"));
 	private final JMenuItem m_jmPluginsHomepage =
 		new JMenuItem("HO! " + HOVerwaltung.instance().getResource().getProperty("Plugins"));
 //	private final JMenuItem m_jmRatingItem = new JMenuItem("Friendly-Rating");
 	private final JMenuItem m_jmTraining =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("SubskillsBerechnen"));
 	private final JMenuItem m_jmiArena =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("ArenaSizer"));
 	private final JMenuItem m_jmiAufstellung =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Aufstellung"));
 	private final JMenuItem m_jmiFlags =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Flaggen"));
 	private final JMenuItem m_jmiHO = new JMenuItem("HO!");
 	private final JMenuItem m_jmiEPV = new JMenuItem("EPV");
 	private final JMenuItem m_jmiRatings = new JMenuItem("Ratings");
 
 	private final JMenuItem m_jmiInjuryCalculator =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("InjuryCalculator"));
 	private final JMenuItem m_jmiKeeperTool =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("KeeperTool"));
 	private final JMenuItem m_jmiNotepad =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Notizen"));
 	private final JMenuItem m_jmiExporter = new JMenuItem("XML Exporter");
 
 	private final JMenuItem m_jmiLanguages =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Sprachdatei"));
 	private final JMenuItem m_jmiLigatabelle =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Ligatabelle"));
 	private final JMenuItem m_jmiPluginsDelete =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("loeschen"));
 	private final JMenuItem m_jmiPluginsLibrary =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Libraries"));
 	private final JMenuItem m_jmiPluginsNormal =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Normal"));
 	private final JMenuItem m_jmiSpiele =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Spiele"));
 	private final JMenuItem m_jmiSpieleranalyse =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("SpielerAnalyse"));
 	private final JMenuItem m_jmiSpieleruebersicht =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Spieleruebersicht"));
 	private final JMenuItem m_jmiStatistik =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Statistik"));
 	private final JMenuItem m_jmiTransferscout =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("TransferScout"));
 	private final JMenuItem m_jmiVerschiedenes =
 		new JMenuItem(HOVerwaltung.instance().getResource().getProperty("Verschiedenes"));
 
 	//----Komponenten--------------------------
 	private JTabbedPane m_jtpTabbedPane;
 	private KeeperToolDialog keeperTool;
 
 	//    private SkillAenderungsPanel    m_jpSkillAenderungsPanel=   null;
 	private LigaTabellePanel m_jpLigaTabelle;
 
 	// private JMenuItem               m_jmChatItem            =   new JMenuItem( model.HOVerwaltung.instance ().getResource ().getProperty("Chat") );
 	//eventuell Menuitem Vector fr Plugins anlegen
 	//-----------------------------------------
 	private OnlineWorker m_clOnlineWorker = new OnlineWorker();
 	private SpielePanel m_jpSpielePanel;
 	private SpielerAnalyseMainPanel m_jpSpielerAnalysePanel;
 	private SpielerUebersichtsPanel m_jpSpielerUebersicht;
 	private StatistikMainPanel m_jpStatistikPanel;
 	private String m_sToRemoveTabName;
 	private TransferScoutPanel m_jpTransferScout;
 	private java.util.Vector m_vOptionPanelNames = new java.util.Vector();
 	private java.util.Vector m_vOptionPanels = new java.util.Vector();
 
 	//~ Constructors -------------------------------------------------------------------------------
 
 	/**
 	 * Singelton
 	 */
 	private HOMainFrame() {
 		RefreshManager.instance().registerRefreshable(this);
 
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		setDefaultFont(UserParameter.instance().schriftGroesse);
 
 		setTitle("HO! - Hattrick Organizer " + getVersionString());
 		this.setIconImage(de.hattrickorganizer.tools.Helper.loadImage("gui/bilder/Logo-16px.png"));
 
 		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		addWindowListener(this);
 		
 		// Log Operating System
 		HOLogger.instance().debug(getClass(), "Operating system found: "+System.getProperty("os.name"));
 		
 		// Catch Apple-Q for MacOS
 		if (System.getProperty("os.name").toLowerCase().indexOf("mac") != -1)
 			addMacOSListener();
 
 		initProxy();
 		initComponents();
 		initMenue();
 
 		RefreshManager.instance().doRefresh();
 	}
 
 	//~ Methods ------------------------------------------------------------------------------------
 
 	/**
 	 * This method creates a MacOS specific listener for the quit operation ("Command-Q")
 	 * 
 	 * We need to use reflections here, because the com.apple.eawt.* classes are Apple specific
 	 * 
 	 * @author flattermann <flattermannHO@gmail.com>
 	 */
 	private void addMacOSListener() {
 		HOLogger.instance().debug(getClass(), "Mac OS detected. Activating specific listeners...");
 		try {
 			// Create the Application
 			Class applicationClass = Class.forName("com.apple.eawt.Application");
 			Object appleApp = applicationClass.newInstance();
 
 			// Create the ApplicationListener
 			Class applicationListenerClass = Class.forName("com.apple.eawt.ApplicationListener");
 			Object appleListener = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { applicationListenerClass },
 				new InvocationHandler() {
 					public Object invoke (Object proxy, Method method, Object[] args) {
 						if (method.getName().equals("handleQuit")) {
 							HOLogger.instance().debug(getClass(), "ApplicationListener.handleQuit() fired! Quitting MacOS Application!");
 							beenden();
 						}
 						return null;
 					}
 			});
 			
 			// Register the ApplicationListener
 			Method addApplicationListenerMethod = applicationClass.getDeclaredMethod("addApplicationListener", new Class[] { applicationListenerClass });
 			addApplicationListenerMethod.invoke(appleApp, new Object[] { appleListener });
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private String getVersionString() {
 		NumberFormat nf = NumberFormat.getInstance(Locale.US);
 		nf.setMinimumFractionDigits(3);
 		String txt = nf.format(VERSION);
 
 		if (isDevelopment()) {
 			txt = txt + " DEV";
 		}
 		return txt;
 	}
 
 	public static boolean isDevelopment() {
 		return DEVELOPMENT;
 	}
 
 	/**
 	 * Singelton
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public static HOMainFrame instance() {
 		if (m_clHOMainFrame == null) {
 			m_clHOMainFrame = new HOMainFrame();
 		}
 
 		return m_clHOMainFrame;
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @param spielerid TODO Missing Method Parameter Documentation
 	 */
 	public void setActualSpieler(int spielerid) {
 		getAufstellungsPanel().getAufstellungsTabelle().setSpieler(spielerid);
 		getAufstellungsPanel().getAufstellungsNamensTabelle().setSpieler(spielerid);
 		getSpielerUebersichtPanel().getSpielerUebersichtTable().setSpieler(spielerid);
 		getSpielerUebersichtPanel().getSpielerUebersichtNamenTable().setSpieler(spielerid);
 		getSpielerUebersichtPanel().newSelectionInform();
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public ArenaSizerPanel getArenaSizerPanel() {
 		return m_jpArenaSizer;
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public AufstellungsPanel getAufstellungsPanel() {
 		return m_jpAufstellung;
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public InfoPanel getInfoPanel() {
 		return m_jpInfoPanel;
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public InformationsPanel getInformationsPanel() {
 		return m_jpInformation;
 	}
 
 	/**
 	 * Gibt den Vector mit den Gestarteten Plugins zurck
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public static java.util.Vector getPlugins() {
 		return m_vPlugins;
 	}
 
 	//    public  TrainingsPanel getTrainingsPanel()
 	//    {
 	//        return m_jpTrainingshelfer;
 	//    }
 	public SpielerAnalyseMainPanel getSpielerAnalyseMainPanel() {
 		return m_jpSpielerAnalysePanel;
 	}
 
 	//------------------Get-----------------------------------------
 	public SpielerUebersichtsPanel getSpielerUebersichtPanel() {
 		return m_jpSpielerUebersicht;
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public StatistikMainPanel getStatistikMainPanel() {
 		return m_jpStatistikPanel;
 	}
 
 	//------Getter------------------------------------------
 	public JTabbedPane getTabbedPane() {
 		return m_jtpTabbedPane;
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public static int getWetter() {
 		if (m_clHOMainFrame == null) {
 			return ISpieler.LEICHTBEWOELKT;
 		}
 
 		return HOMainFrame
 			.instance()
 			.getAufstellungsPanel()
 			.getAufstellungsAssitentPanel()
 			.getWetter();
 
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public OnlineWorker getOnlineWorker() {
 		return m_clOnlineWorker;
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public TransferScoutPanel getTransferScoutPanel() {
 		return m_jpTransferScout;
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @param actionEvent TODO Missing Method Parameter Documentation
 	 */
 	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
 		HOMainFrame.setHOStatus(HOMainFrame.BUSY);
 		final Object source = actionEvent.getSource();
 		//HRF Import
 		if (source.equals(m_jmImportItem)) {
 			new de.hattrickorganizer.gui.menu.HRFImport(this);
 		}
 		//HRF Download
 		else if (source.equals(m_jmDownloadItem)) {
 			//new gui.login.LoginDialog( this );
 			//m_clOnlineWorker.getHrf ();
 			new de.hattrickorganizer.gui.menu.DownloadDialog();
 		}
 		//Optionen
 		else if (source.equals(m_jmOptionen)) {
 			new de.hattrickorganizer.gui.menu.option.OptionenDialog(this).setVisible(true);
 		}
 		//Training
 		else if (source.equals(m_jmTraining)) {
 			//Training komplett neu berechnen
 			if (JOptionPane
 				.showConfirmDialog(
 					this,
 					"Depending on database volume this process takes several minutes. Start recalculation ?",
 					"Subskill Recalculation",
 					JOptionPane.YES_NO_OPTION)
 				== JOptionPane.OK_OPTION) {
 				HOVerwaltung.instance().recalcSubskills(true, null);
 
 				//tools.Helper.showMessage ( this, model.HOVerwaltung.instance ().getResource ().getProperty( "NeustartErforderlich" ), "", JOptionPane.INFORMATION_MESSAGE );
 			}
 		}
 		//Beenden
 		else if (source.equals(m_jmBeendenItem)) {
 			//CloseEvent feuern, damit alle Listener (Plugins) informiert werden
 			this.processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
 		}
 		//Spieleruebersicht
 		else if (source.equals(m_jmiSpieleruebersicht)) {
 			showTab(de.hattrickorganizer.gui.HOMainFrame.SPIELERUEBERSICHT);
 		}
 		//Aufstellung
 		else if (source.equals(m_jmiAufstellung)) {
 			showTab(de.hattrickorganizer.gui.HOMainFrame.AUFSTELLUNG);
 		}
 		//Ligatabelle
 		else if (source.equals(m_jmiLigatabelle)) {
 			showTab(de.hattrickorganizer.gui.HOMainFrame.LIGATABELLE);
 		}
 		//Spiele
 		else if (source.equals(m_jmiSpiele)) {
 			showTab(de.hattrickorganizer.gui.HOMainFrame.SPIELE);
 		}
 		//SpielerAnalyse
 		else if (source.equals(m_jmiSpieleranalyse)) {
 			showTab(de.hattrickorganizer.gui.HOMainFrame.SPIELERANALYSE);
 		}
 		//Statistik
 		else if (source.equals(m_jmiStatistik)) {
 			showTab(de.hattrickorganizer.gui.HOMainFrame.STATISTIK);
 		}
 		//Transferscout
 		else if (source.equals(m_jmiTransferscout)) {
 			showTab(de.hattrickorganizer.gui.HOMainFrame.TRANSFERSCOUT);
 		}
 		//Arena
 		else if (source.equals(m_jmiArena)) {
 			showTab(de.hattrickorganizer.gui.HOMainFrame.ARENASIZER);
 		}
 		//Verschiedenes
 		else if (source.equals(m_jmiVerschiedenes)) {
 			showTab(de.hattrickorganizer.gui.HOMainFrame.INFORMATIONEN);
 		}
 		//Credits
 		else if (source.equals(m_jmCreditsItem)) {
 			//credits starten
 			new de.hattrickorganizer.credits.VAPCredits(this);
 		}
 		//Homepage
 		else if (source.equals(m_jmHomepageItem)) {
 			HelperWrapper.instance().openUrlInUserBRowser(MyConnector.getHOSite());
 		}
 		// Plugins Homepage
 		else if (source.equals(m_jmPluginsHomepage)) {
 			HelperWrapper.instance().openUrlInUserBRowser(UpdateController.PLUGINS_HOMEPAGE);
 		}
 		//Forum
 		else if (source.equals(m_jmForumItem)) {
 			HelperWrapper.instance().openUrlInUserBRowser("http://forum.hattrickorganizer.net/index.php");
 		}
 		//        //chat
 		//        else if ( actionEvent.getSource().equals( m_jmChatItem ) )
 		//        {
 		//            try
 		//            {
 		//                tools.BrowserLauncher.openURL( "http://chat.uin4d.de/" );
 		//            }
 		//            catch ( java.io.IOException ioex )
 		//            {
 		//            }
 		//        }
 		//Hattrick
 		else if (source.equals(m_jmHattrickItem)) {
 			HelperWrapper.instance().openUrlInUserBRowser("http://www.hattrick.org");
 		}
 		//HOFriendly Server
 //		else if (source.equals(m_jmHoFriendly)) {
 //			final ServerTeam team =
 //				HOVerwaltung.instance().getModel().getVerein().erstelleServerTeam();
 //
 //			if (team.getAnzAufgestellteSpieler() < 8) {
 //				de.hattrickorganizer.tools.Helper.showMessage(
 //					this,
 //					HOVerwaltung.instance().getResource().getProperty("ZuWenigSpieler"),
 //					HOVerwaltung.instance().getResource().getProperty("Fehler"),
 //					JOptionPane.ERROR_MESSAGE);
 //				HOMainFrame.setHOStatus(HOMainFrame.READY);
 //				return;
 //			}
 //
 //			final de.hattrickorganizer.gui.hoFriendly.RMIDialog rmiDialog =
 //				new de.hattrickorganizer.gui.hoFriendly.RMIDialog(this);
 //
 //			if (!rmiDialog.isAbgebrochen()) {
 //				if (rmiDialog.isServer()) {
 //					final de.hattrickorganizer.model.Server server =
 //						new de.hattrickorganizer.model.Server(
 //							rmiDialog.getServerIP(),
 //							rmiDialog.getPort());
 //
 //					if (server.noError()) {
 //						de.hattrickorganizer.net.rmiHOFriendly.HOServerImp hoServerImp = null;
 //
 //						//Server Screen erzeugen
 //						final de.hattrickorganizer.gui.hoFriendly.HOFriendlyDialog soms =
 //							new de.hattrickorganizer.gui.hoFriendly.HOFriendlyDialog(this, true);
 //						final de.hattrickorganizer.net.rmiHOFriendly.NetMatchScreen nms =
 //							new de.hattrickorganizer.net.rmiHOFriendly.NetMatchScreen(soms);
 //						String info = null;
 //
 //						//String[]    find        ={ " " };
 //						//String[]    replace =   { "%20%" };
 //						//Werte bergeben
 //						server.setScreen1(nms);
 //
 //						//Server Team noch setzen
 //						server.setHeimTeam(team);
 //
 //						//Socket Server anlegen
 //						hoServerImp =
 //							new de.hattrickorganizer.net.rmiHOFriendly.HOServerImp(server);
 //						server.setHOServer(hoServerImp);
 //						soms.setChat(hoServerImp);
 //
 //						//Info String vorbereiten
 //						info =
 //							team.getTeamName()
 //								+ " ("
 //								+ team.getManagerName()
 //								+ ")  "
 //								+ HOVerwaltung.instance().getModel().getLiga().getLiga();
 //						info = info.trim();
 //
 //						//tools.MyHelper.replaceSubString( find, replace, request );
 //						try {
 //							info = java.net.URLEncoder.encode(info, "UTF-8");
 //
 //							//             HOLogger.instance().log(HOMainFrame.class,"Register request : " + info );
 //						} catch (Exception e) {
 //						}
 //
 //						//Socket starten
 //						hoServerImp.createServer(
 //							rmiDialog.isInternetServer(),
 //							rmiDialog.getServerIP(),
 //							rmiDialog.getPort(),
 //							info);
 //
 //						//man knnte mit server.getSpielbericht () nach Ende den Spielbericht abfragen...
 //					} else {
 //						//Fehlermeldung
 //					}
 //
 //					//Client
 //				} else {
 //					de.hattrickorganizer.net.rmiHOFriendly.HoClientImp hoClientImp = null;
 //
 //					//Client Screen erzeugen
 //					final de.hattrickorganizer.gui.hoFriendly.HOFriendlyDialog soms =
 //						new de.hattrickorganizer.gui.hoFriendly.HOFriendlyDialog(this, false);
 //					final de.hattrickorganizer.net.rmiHOFriendly.NetMatchScreen nms =
 //						new de.hattrickorganizer.net.rmiHOFriendly.NetMatchScreen(soms);
 //
 //					hoClientImp = new de.hattrickorganizer.net.rmiHOFriendly.HoClientImp(nms);
 //					soms.setChat(hoClientImp);
 //
 //					if (hoClientImp.connect2Server(rmiDialog.getClientIP(), rmiDialog.getPort())) {
 //						//Spiel starten
 //						hoClientImp.sendStarteFriendly(team);
 //
 //						//man knnte mit hoClientImp.getSpielbericht () nach Ende den Spielbericht abfragen...
 //					} else {
 //						//Fehlermeldung
 //						de.hattrickorganizer.tools.Helper.showMessage(
 //							this,
 //							HOVerwaltung.instance().getResource().getProperty("KeinServer")
 //								+ rmiDialog.getServerIP()
 //								+ ":"
 //								+ rmiDialog.getPort(),
 //							HOVerwaltung.instance().getResource().getProperty("Fehler"),
 //							JOptionPane.ERROR_MESSAGE);
 //
 //						//Dialog shutdown
 //						soms.setVisible(false);
 //						soms.dispose();
 //
 //						if (hoClientImp != null) {
 //							hoClientImp.shutdown();
 //							hoClientImp = null;
 //						}
 //					}
 //				}
 //			}
 //		} else if (source.equals(m_jmIPAdresse)) {
 //			try {
 //				de.hattrickorganizer.tools.BrowserLauncher.openURL(
 //					MyConnector.getHOSite()+"/IPAdresse.html");
 //			} catch (java.io.IOException ioex) {
 //			}
 //		} else if (source.equals(m_jmRatingItem)) {
 //			try {
 //				de.hattrickorganizer.tools.BrowserLauncher.openURL(
 //					"http://tooldesign.ch/ho/ranking.php");
 //			} catch (java.io.IOException ioex) {
 //			}
 //		}
 		else if (source.equals(m_jmiKeeperTool)) {
 			keeperTool.reload();
 			keeperTool.setVisible(true);
 		} else if (source.equals(m_jmiNotepad)) {
 			NotepadDialog notepad =
 				new NotepadDialog(
 					this,
 					HOVerwaltung.instance().getResource().getProperty("Notizen"));
 			notepad.setVisible(true);
 		} else if (source.equals(m_jmiExporter)) {
 			XMLExporter exporter = new XMLExporter();
 			exporter.doExport();
 		} else if (source.equals(m_jmiInjuryCalculator)) {
 			injuryTool.reload();
 			injuryTool.setVisible(true);
 		} else if (source.equals(m_jmiLanguages)) {
 			UpdateController.showLanguageUpdateDialog();
 		} else if (source.equals(m_jmiFlags)) {
 			UpdateController.updateFlags();
 		} else if (source.equals(m_jmiHO)) {
 			UpdateController.check4update();
 		} else if (source.equals(m_jmiEPV)) {
 			UpdateController.check4EPVUpdate();
 		} else if (source.equals(m_jmiRatings)) {
 			UpdateController.check4RatingsUpdate();
 		} else if (source.equals(m_jmiPluginsDelete)) {
 			UpdateController.showDeletePluginDialog();
 		} else if (source.equals(m_jmiPluginsNormal)) {
 			UpdateController.showPluginUpdaterNormal();
 		} else if (source.equals(m_jmiPluginsLibrary)) {
 			UpdateController.showPluginUpdaterLibraries();
 		}
 		HOMainFrame.setHOStatus(HOMainFrame.READY);
 	}
 
 	/**
 	 * Fr Plugins zur Info
 	 *
 	 * @param listener TODO Missing Constructuor Parameter Documentation
 	 */
 	public void addMainFrameListener(WindowListener listener) {
 		addWindowListener(listener);
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @param menu TODO Missing Method Parameter Documentation
 	 */
 	public void addMenu(JMenu menu) {
 		m_jmPluginMenu.add(menu);
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @param menu TODO Missing Method Parameter Documentation
 	 */
 	public void addTopLevelMenu(JMenu menu) {
 		m_jmMenuBar.add(menu);
 	}
 
 	/**
 	 * Beendet HO
 	 */
 	public void beenden() {
 		HOLogger.instance().debug(getClass(), "Shutting down HO!");
 		
 		//Keine Sicherheitsabfrage mehr
 		//int value = JOptionPane.showConfirmDialog( this, model.HOVerwaltung.instance ().getResource ().getProperty("BeendenMeldung"), model.HOVerwaltung.instance ().getResource ().getProperty("BeendenTitel"), JOptionPane.YES_NO_OPTION);
 		//        int value = JOptionPane.OK_OPTION; //Doof aber schnell zu schreiben!
 		//        if ( value == JOptionPane.OK_OPTION )
 		//aktuelle UserParameter speichern
 		saveUserParameter();
 
 		//Scoutliste speichern
 		m_jpTransferScout.saveScoutListe();
 
 		//Faktoren saven
 		FormulaFactors.instance().save();
 
 		//Disconnect
 		de.hattrickorganizer.database.DBZugriff.instance().disconnect();
 
 		//Ausloggen
 		try {
 			if ((UserParameter.instance().logoutOnExit)
 				&& (de.hattrickorganizer.net.MyConnector.instance().isAuthenticated())) {
 				de.hattrickorganizer.net.MyConnector.instance().logout();
 			}
 		} catch (Exception e) {
 		}
 
 		//Dispose fhrt zu einem windowClosed, sobald alle windowClosing (Plugins) durch sind
 		try {
 			dispose();
 		} catch (Exception e) {
 		}
 	}
 
 	/**
 	 * Checked die Sprachdatei oder Fragt nach einer passenden
 	 *
 	 * @param dateiname TODO Missing Constructuor Parameter Documentation
 	 */
 	public static void checkSprachFile(String dateiname) {
 		try {
 			//java.net.URL resource = new gui.vorlagen.ImagePanel().getClass().getClassLoader().getResource( "sprache/"+dateiname+".properties" );
 			final java.io.File sprachdatei =
 				new java.io.File("sprache/" + dateiname + ".properties");
 
 			if (sprachdatei.exists()) {
 				double sprachfileversion = 0;
 				final java.util.Properties temp = new java.util.Properties();
 				temp.load(new java.io.FileInputStream(sprachdatei));
 
 				try {
 					sprachfileversion = Double.parseDouble(temp.getProperty("Version"));
 				} catch (Exception e) {
 					HOLogger.instance().log(HOMainFrame.class, "not use " + sprachdatei.getName());
 				}
 
 				if (sprachfileversion >= de.hattrickorganizer.gui.HOMainFrame.SPRACHVERSION) {
 					HOLogger.instance().log(HOMainFrame.class, "use " + sprachdatei.getName());
 
 					//Alles ok!!
 					return;
 				}
 				//Nicht passende Version
 				else {
 					HOLogger.instance().log(HOMainFrame.class, "not use " + sprachdatei.getName());
 				}
 			}
 		} catch (Exception e) {
 			HOLogger.instance().log(HOMainFrame.class, "not use " + e);
 		}
 
 		//Irgendein Fehler -> neue Datei aussuchen!
 		//new gui.menue.optionen.InitOptionsDialog();
 		gui.UserParameter.instance().sprachDatei = "English";
 	}
 
 	/**
 	 * Frame aufbauen
 	 */
 	public void initComponents() {
 		final Properties properties = HOVerwaltung.instance().getResource();
 		javax.swing.ToolTipManager.sharedInstance().setDismissDelay(5000);
 
 		setContentPane(new de.hattrickorganizer.gui.templates.ImagePanel());
 		getContentPane().setLayout(new BorderLayout());
 
 		m_jtpTabbedPane = new JTabbedPane();
 
 		//Hinzufgen der Tabs nur, wenn von der Userparameter gewnscht
 		//Spieler
 		m_jpSpielerUebersicht = new SpielerUebersichtsPanel();
 
 		if (!gui.UserParameter.instance().tempTabSpieleruebersicht) {
 			m_jtpTabbedPane.addTab(
 				properties.getProperty("Spieleruebersicht"),
 				m_jpSpielerUebersicht);
 		}
 
 		//Aufstellung
 		m_jpAufstellung = new AufstellungsPanel();
 
 		if (!gui.UserParameter.instance().tempTabAufstellung) {
 			m_jtpTabbedPane.addTab(properties.getProperty("Aufstellung"), m_jpAufstellung);
 		}
 
 		//Tabelle
 		m_jpLigaTabelle = new LigaTabellePanel();
 
 		if (!gui.UserParameter.instance().tempTabLigatabelle) {
 			m_jtpTabbedPane.addTab(properties.getProperty("Ligatabelle"), m_jpLigaTabelle);
 		}
 
 		//Spiele
 		m_jpSpielePanel = new SpielePanel();
 
 		if (!gui.UserParameter.instance().tempTabSpiele) {
 			m_jtpTabbedPane.addTab(properties.getProperty("Spiele"), m_jpSpielePanel);
 		}
 
 		//SpielerAnalyse
 		m_jpSpielerAnalysePanel = new SpielerAnalyseMainPanel();
 
 		if (!gui.UserParameter.instance().tempTabSpieleranalyse) {
 			m_jtpTabbedPane.addTab(
 				properties.getProperty("SpielerAnalyse"),
 				m_jpSpielerAnalysePanel);
 		}
 
 		//        //Training
 		//        m_jtpTraining = new JTabbedPane();
 		//        //Trainingshilfe
 		//        m_jpTrainingshelfer = new TrainingsPanel();
 		//        m_jtpTraining.addTab ( model.HOVerwaltung.instance ().getResource ().getProperty("Training"), m_jpTrainingshelfer );
 		//        //SkillAenderung
 		//        m_jpSkillAenderungsPanel = new SkillAenderungsPanel();
 		//        m_jtpTraining.addTab ( model.HOVerwaltung.instance ().getResource ().getProperty("Training") + " 2", m_jpSkillAenderungsPanel );
 		//        //Adden
 		//        m_jtpTabbedPane.addTab ( model.HOVerwaltung.instance ().getResource ().getProperty("Training"), m_jtpTraining );
 		//
 		//Transferscout
 		m_jpTransferScout = new TransferScoutPanel();
 
 		if (!gui.UserParameter.instance().tempTabTransferscout) {
 			m_jtpTabbedPane.addTab(properties.getProperty("TransferScout"), m_jpTransferScout);
 		}
 
 		//Arena
 		m_jpArenaSizer = new ArenaSizerPanel();
 
 		if (!gui.UserParameter.instance().tempTabArenasizer) {
 			m_jtpTabbedPane.addTab(properties.getProperty("ArenaSizer"), m_jpArenaSizer);
 		}
 
 		//Sonstiges
 		m_jpInformation = new InformationsPanel();
 
 		if (!gui.UserParameter.instance().tempTabInformation) {
 			m_jtpTabbedPane.addTab(properties.getProperty("Verschiedenes"), m_jpInformation);
 		}
 
 		//Statistiken
 		m_jpStatistikPanel = new StatistikMainPanel();
 
 		if (!gui.UserParameter.instance().tempTabStatistik) {
 			m_jtpTabbedPane.addTab(properties.getProperty("Statistik"), m_jpStatistikPanel);
 		}
 
 		//Matchpaneltest
 		/*
 		   logik.matchEngine.TeamData a = new logik.matchEngine.TeamData("Team1",new logik.matchEngine.TeamRatings(8, 18, 15, 13, 12, 8, 13),plugins.IMatchDetails.TAKTIK_KONTER,10);
 		   logik.matchEngine.TeamData b = new logik.matchEngine.TeamData("Team2",new logik.matchEngine.TeamRatings(12, 10, 12, 12, 10, 8, 10),plugins.IMatchDetails.TAKTIK_NORMAL,0);
 		   //gui.matchprediction.MatchEnginePanel enginepanel    =   new gui.matchprediction.MatchEnginePanel( a, b );
 
 		   JDialog dialog = new JDialog( );
 		   dialog.getContentPane().setLayout( new BorderLayout() );
 		   dialog.getContentPane().add( model.HOMiniModel.instance ().getGUI().createMatchPredictionPanel( a, b ), BorderLayout.CENTER );
 		   dialog.setSize( 800, 600 );
 		   dialog.setVisible( true );
 		 */
 		getContentPane().add(m_jtpTabbedPane, BorderLayout.CENTER);
 
 		m_jtpTabbedPane.addChangeListener(this);
 
 		m_jpInfoPanel = new InfoPanel();
 		getContentPane().add(m_jpInfoPanel, BorderLayout.SOUTH);
 
 		injuryTool = new InjuryDialog(this);
 		keeperTool = new KeeperToolDialog(this);
 		setLocation(
 			UserParameter.instance().hoMainFrame_PositionX,
 			UserParameter.instance().hoMainFrame_PositionY);
 		setSize(
 			UserParameter.instance().hoMainFrame_width,
 			UserParameter.instance().hoMainFrame_height);
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 */
 	public void initMenue() {
 		//Kein F10!
 		((InputMap) UIManager.get("Table.ancestorInputMap")).remove(
 			KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
 
 		// Falsch ( (InputMap)UIManager.get("Menu.ancestorInputMap") ).remove( KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_F10, 0 ) );
 		//m_jmMenuBar.getInputMap().remove( KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_F10, 0 ) );
 		//Datei
 		//Download HRF
 		m_jmDownloadItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F11, 0));
 		m_jmDownloadItem.addActionListener(this);
 		m_jmDatei.add(m_jmDownloadItem);
 
 		//Import HRF
 		m_jmImportItem.addActionListener(this);
 		m_jmDatei.add(m_jmImportItem);
 
 		// 		Updating Menu
 		m_jmiLanguages.addActionListener(this);
 		m_jmiPluginsDelete.addActionListener(this);
 		m_jmiFlags.addActionListener(this);
 		m_jmiHO.addActionListener(this);
 		m_jmiEPV.addActionListener(this);
 		m_jmiRatings.addActionListener(this);
 		m_jmiPluginsNormal.addActionListener(this);
 		m_jmiPluginsLibrary.addActionListener(this);
 
 		m_jmPluginsRefresh.add(m_jmiPluginsNormal);
 		m_jmPluginsRefresh.add(m_jmiPluginsLibrary);
 		m_jmPluginsRefresh.add(m_jmiPluginsDelete);
 
 		m_jmUpdating.add(m_jmPluginsRefresh);
 		m_jmUpdating.add(m_jmiHO);
 		m_jmUpdating.add(m_jmiEPV);
 		m_jmUpdating.add(m_jmiRatings);
 		m_jmUpdating.add(m_jmiLanguages);
 		m_jmUpdating.add(m_jmiFlags);
 
 		m_jmDatei.add(m_jmUpdating);
 
 		//Download Spielplan
 		//m_jmFixturesItem.addActionListener ( this );
 		//m_jmDatei.add ( m_jmFixturesItem );
 		m_jmDatei.addSeparator();
 
 		//Training
 		m_jmTraining.addActionListener(this);
 		m_jmDatei.add(m_jmTraining);
 
 		m_jmDatei.addSeparator();
 
 		//Optionen
 		m_jmOptionen.addActionListener(this);
 		m_jmDatei.add(m_jmOptionen);
 
 		m_jmDatei.addSeparator();
 
 		//Beenden
 		m_jmBeendenItem.addActionListener(this);
 		m_jmDatei.add(m_jmBeendenItem);
 
 		m_jmMenuBar.add(m_jmDatei);
 
 		/////
 		//Verschiedenes
 		//Spieleruebersicht
 		m_jmiSpieleruebersicht.setAccelerator(
 			KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
 		m_jmiSpieleruebersicht.addActionListener(this);
 		m_jmVerschiedenes.add(m_jmiSpieleruebersicht);
 
 		//Aufstellung
 		m_jmiAufstellung.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
 		m_jmiAufstellung.addActionListener(this);
 		m_jmVerschiedenes.add(m_jmiAufstellung);
 
 		//Ligatabelle
 		m_jmiLigatabelle.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
 		m_jmiLigatabelle.addActionListener(this);
 		m_jmVerschiedenes.add(m_jmiLigatabelle);
 
 		//Spiele
 		m_jmiSpiele.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, 0));
 		m_jmiSpiele.addActionListener(this);
 		m_jmVerschiedenes.add(m_jmiSpiele);
 
 		//Spieleranalyse
 		m_jmiSpieleranalyse.setAccelerator(
 			KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
 		m_jmiSpieleranalyse.addActionListener(this);
 		m_jmVerschiedenes.add(m_jmiSpieleranalyse);
 
 		//Statistik
 		m_jmiStatistik.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, 0));
 		m_jmiStatistik.addActionListener(this);
 		m_jmVerschiedenes.add(m_jmiStatistik);
 
 		//Transferscout
 		m_jmiTransferscout.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F7, 0));
 		m_jmiTransferscout.addActionListener(this);
 		m_jmVerschiedenes.add(m_jmiTransferscout);
 
 		//ArenaSizer
 		m_jmiArena.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F8, 0));
 		m_jmiArena.addActionListener(this);
 		m_jmVerschiedenes.add(m_jmiArena);
 
 		//Verschiedenes
 		m_jmiVerschiedenes.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, 0));
 		m_jmiVerschiedenes.addActionListener(this);
 		m_jmVerschiedenes.add(m_jmiVerschiedenes);
 
 		m_jmMenuBar.add(m_jmVerschiedenes);
 
 		/////
 		//HOFriendly
 //		m_jmHoFriendly.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, 0));
 //		m_jmHoFriendly.addActionListener(this);
 //		m_jmHoFriendlyMenu.add(m_jmHoFriendly);
 //
 //		m_jmHoFriendlyMenu.addSeparator();
 //
 //		m_jmIPAdresse.addActionListener(this);
 //		m_jmHoFriendlyMenu.add(m_jmIPAdresse);
 //
 //		m_jmRatingItem.addActionListener(this);
 //		m_jmHoFriendlyMenu.add(m_jmRatingItem);
 //
 //		m_jmMenuBar.add(m_jmHoFriendlyMenu);
 
 		//About
 		m_jmHomepageItem.addActionListener(this);
 		m_jmAbout.add(m_jmHomepageItem);
 
 		m_jmForumItem.addActionListener(this);
 		m_jmAbout.add(m_jmForumItem);
 
 		m_jmPluginsHomepage.addActionListener(this);
 		m_jmAbout.add(m_jmPluginsHomepage);
 
 		//        m_jmChatItem.addActionListener( this );
 		//        m_jmAbout.add ( m_jmChatItem );
 		m_jmHattrickItem.addActionListener(this);
 		m_jmAbout.add(m_jmHattrickItem);
 
 		m_jmAbout.addSeparator();
 
 		m_jmCreditsItem.addActionListener(this);
 		m_jmAbout.add(m_jmCreditsItem);
 
 		m_jmMenuBar.add(m_jmAbout);
 
 		//		Tool Menu
 		m_jmiKeeperTool.addActionListener(this);
 		m_jmToolsMenu.add(m_jmiKeeperTool);
 
 		m_jmiInjuryCalculator.addActionListener(this);
 		m_jmToolsMenu.add(m_jmiInjuryCalculator);
 
 		m_jmiExporter.addActionListener(this);
 		m_jmToolsMenu.add(m_jmiExporter);
 
 		m_jmiNotepad.addActionListener(this);
 		m_jmToolsMenu.add(m_jmiNotepad);
 
 		m_jmMenuBar.add(m_jmToolsMenu);
 
 		//		Plugin Menu
 		m_jmMenuBar.add(m_jmPluginMenu);
 
 		SwingUtilities.updateComponentTreeUI(m_jmMenuBar);
 
 		//Adden
 		this.setJMenuBar(m_jmMenuBar);
 	}
 
 	/**
 	 * Proxyeinstellungen
 	 */
 	public void initProxy() {
 		if (gui.UserParameter.instance().ProxyAktiv) {
 			de.hattrickorganizer.net.MyConnector.instance().setProxyHost(
 				gui.UserParameter.instance().ProxyHost);
 			de.hattrickorganizer.net.MyConnector.instance().setUseProxy(
 				gui.UserParameter.instance().ProxyAktiv);
 			de.hattrickorganizer.net.MyConnector.instance().setProxyPort(
 				gui.UserParameter.instance().ProxyPort);
 			de.hattrickorganizer.net.MyConnector.instance().setProxyAuthentifactionNeeded(
 				gui.UserParameter.instance().ProxyAuthAktiv);
 			de.hattrickorganizer.net.MyConnector.instance().setProxyUserName(
 				gui.UserParameter.instance().ProxyAuthName);
 			de.hattrickorganizer.net.MyConnector.instance().setProxyUserPWD(
 				gui.UserParameter.instance().ProxyAuthPassword);
 			de.hattrickorganizer.net.MyConnector.instance().enableProxy();
 		}
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public java.util.Vector getOptionPanelNames() {
 		return m_vOptionPanelNames;
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public java.util.Vector getOptionPanels() {
 		return m_vOptionPanels;
 	}
 
 	/**
 	 * OptionsPanels fr Plugins
 	 *
 	 * @param name TODO Missing Constructuor Parameter Documentation
 	 * @param optionpanel TODO Missing Constructuor Parameter Documentation
 	 */
 	public void addOptionPanel(String name, javax.swing.JPanel optionpanel) {
 		m_vOptionPanels.add(optionpanel);
 		m_vOptionPanelNames.add(name);
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 */
 	public void reInit() {
 		//Die Whrung auf die aus dem HRF setzen
 		try {
 			float faktorgeld =
 				(float) HOVerwaltung.instance().getModel().getXtraDaten().getCurrencyRate();
 
 			if (faktorgeld > -1) {
 				gui.UserParameter.instance().faktorGeld = faktorgeld;
 			}
 		} catch (Exception e) {
 			HOLogger.instance().log(HOMainFrame.class, "Whrungsanpassung gescheitert!");
 		}
 
 		//Tabs prfen
 		checkTabs();
 	}
 
 	//------Refreshfunktionen-------------------------------
 
 	/**
 	 * Wird bei einer Datennderung aufgerufen
 	 */
 	public void refresh() {
 		//nix?
 	}
 
 	/**
 	 * Fr Plugins zur Info
 	 *
 	 * @param listener TODO Missing Constructuor Parameter Documentation
 	 */
 	public void removeMainFrameListener(WindowListener listener) {
 		removeWindowListener(listener);
 	}
 
 	//--------------------------------------------------------------
 	public void showMatch(int matchid) {
 		showTab(de.hattrickorganizer.gui.HOMainFrame.SPIELE);
 
 		m_jpSpielePanel.showMatch(matchid);
 	}
 
 	//----------------Hilfsmethoden---------------------------------
 
 	/**
 	 * Zeigt das Tab an (Nicht Index, sondern Konstante benutzen!
 	 *
 	 * @param tabnumber TODO Missing Constructuor Parameter Documentation
 	 */
 	public void showTab(int tabnumber) {
 		//Erstmal weg damit
 		m_jtpTabbedPane.removeChangeListener(this);
 
 		Component component;
 		String titel;
 		boolean temporaer = false;
 		String removeTabName = null;
 
 		switch (tabnumber) {
 			case SPIELERUEBERSICHT :
 				component = m_jpSpielerUebersicht;
 				titel = HOVerwaltung.instance().getResource().getProperty("Spieleruebersicht");
 				temporaer = gui.UserParameter.instance().tempTabSpieleruebersicht;
 				break;
 
 			case AUFSTELLUNG :
 				component = m_jpAufstellung;
 				titel = HOVerwaltung.instance().getResource().getProperty("Aufstellung");
 				temporaer = gui.UserParameter.instance().tempTabAufstellung;
 				break;
 
 			case LIGATABELLE :
 				component = m_jpLigaTabelle;
 				titel = HOVerwaltung.instance().getResource().getProperty("Ligatabelle");
 				temporaer = gui.UserParameter.instance().tempTabLigatabelle;
 				break;
 
 			case SPIELE :
 				component = m_jpSpielePanel;
 				titel = HOVerwaltung.instance().getResource().getProperty("Spiele");
 				temporaer = gui.UserParameter.instance().tempTabSpiele;
 				break;
 
 			case SPIELERANALYSE :
 				component = m_jpSpielerAnalysePanel;
 				titel = HOVerwaltung.instance().getResource().getProperty("SpielerAnalyse");
 				temporaer = gui.UserParameter.instance().tempTabSpieleranalyse;
 				break;
 
 			case STATISTIK :
 				component = m_jpStatistikPanel;
 				titel = HOVerwaltung.instance().getResource().getProperty("Statistik");
 				temporaer = gui.UserParameter.instance().tempTabStatistik;
 				break;
 
 			case TRANSFERSCOUT :
 				component = m_jpTransferScout;
 				titel = HOVerwaltung.instance().getResource().getProperty("TransferScout");
 				temporaer = gui.UserParameter.instance().tempTabTransferscout;
 				break;
 
 			case ARENASIZER :
 				component = m_jpArenaSizer;
 				titel = HOVerwaltung.instance().getResource().getProperty("ArenaSizer");
 				temporaer = gui.UserParameter.instance().tempTabArenasizer;
 				break;
 
 			case INFORMATIONEN :
 				component = m_jpInformation;
 				titel = HOVerwaltung.instance().getResource().getProperty("Verschiedenes");
 				temporaer = gui.UserParameter.instance().tempTabInformation;
 				break;
 
 			default :
 				return;
 		}
 
 		//Wenn Temp, dann jetzt Hinzufgen
 		int index = m_jtpTabbedPane.indexOfTab(titel);
 
 		//Hinzufgen, aber spter per ChangeListener lschen
 		if (index < 0) {
 			m_jtpTabbedPane.addTab(titel, component);
 			index = m_jtpTabbedPane.indexOfTab(titel);
 
 			//Name von Tab merken, um ihn nachher zu entfernen
 			if (temporaer) {
 				removeTabName = titel;
 			}
 		}
 
 		//Und Listener wieder hinzu
 		m_jtpTabbedPane.addChangeListener(this);
 
 		//Tab markieren
 		if (m_jtpTabbedPane.getTabCount() > index) {
 			m_jtpTabbedPane.setSelectedIndex(index);
 		}
 
 		//Damit das setSelectedIndex nicht sofort das neue Tab killt erst hier den Namen setzen, wenn temp
 		m_sToRemoveTabName = removeTabName;
 	}
 
 	/////////////////////////////////////////////////////////////////////////////////////////////////77
 	//helper
 	/////////////////////////////////////////////////////////////////////////////////////////////////77
 	public void startPluginModuls(
 		de.hattrickorganizer.gui.utils.InterruptionWindow interuptionWindow) {
 		try {
 			//Den Ordner mit den Plugins holen
 			final java.io.File folder = new java.io.File("hoplugins");
 			HOLogger.instance().log(
 				HOMainFrame.class,
 				folder.getAbsolutePath() + " " + folder.exists() + " " + folder.isDirectory());
 
 			//Filter, nur class-Datein in dem Ordner interessant
 			final de.hattrickorganizer.gui.utils.ExampleFileFilter filter =
 				new de.hattrickorganizer.gui.utils.ExampleFileFilter();
 			filter.addExtension("class");
 			filter.setDescription("Java Class File");
 			filter.setIgnoreDirectories(true);
 
 			//Alle class-Dateien in den Ordner holen
 			final java.io.File[] files = folder.listFiles(filter);
 
 			//Libs -> Alle Dateien durchlaufen
 			for (int i = 0;(files != null) && (i < files.length); i++) {
 				try {
 					//Name der Klasse erstellen und Class-Object erstellen
 					final String name =
 						"hoplugins."
 							+ files[i].getName().substring(0, files[i].getName().lastIndexOf('.'));
 					final Class fileclass = Class.forName(name);
 					//Das Class-Object definiert kein Interface ...
 					if (!fileclass.isInterface()) {
 						//... und ist von ILib abgeleitet
 						if (plugins.ILib.class.isAssignableFrom(fileclass)) {
 							//Object davon erstellen und starten
 							final plugins.IPlugin modul = (plugins.IPlugin) fileclass.newInstance();
 
 							//Plugin im Vector gespeichert
 							m_vPlugins.add(modul);
 							HOLogger.instance().log(
 								HOMainFrame.class,
 								" Starte " + files[i].getName() + "  (init MiniModel)");
 							interuptionWindow.setInfoText("Start Plugin: " + modul.getName());
 							modul.start(de.hattrickorganizer.model.HOMiniModel.instance());
 
 							HOLogger.instance().log(
 								HOMainFrame.class,
 								"+ " + files[i].getName() + " gestartet als lib");
 						} else {
 							HOLogger.instance().log(
 								HOMainFrame.class,
 								"- " + files[i].getName() + " nicht von ILib abgeleitet");
 						}
 					} else {
 						HOLogger.instance().log(
 							HOMainFrame.class,
 							"- " + files[i].getName() + " ist Interface");
 					}
 				} catch (Throwable e2) {
 					HOLogger.instance().log(
 						HOMainFrame.class,
 						"- " + files[i].getName() + " wird bersprungen: " + e2.toString());
 
 					//HOLogger.instance().log(HOMainFrame.class,e2);
 				}
 			}
 
 			//Plugins -> Alle Dateien durchlaufen
 			for (int i = 0;(files != null) && (i < files.length); i++) {
 				try {
 					//Name der Klasse erstellen und Class-Object erstellen
 					final String name =
 						"hoplugins."
 							+ files[i].getName().substring(0, files[i].getName().lastIndexOf('.'));
 					final Class fileclass = Class.forName(name);
 					//Das Class-Object definiert kein Interface ...
 					if (!fileclass.isInterface()) {
 						//... und ist von IPlugin abgeleitet, nicht die Libs nochmal starten!
 						if (plugins.IPlugin.class.isAssignableFrom(fileclass)
 							&& !plugins.ILib.class.isAssignableFrom(fileclass)) {
 							//Object davon erstellen und starten
 							final plugins.IPlugin modul = (plugins.IPlugin) fileclass.newInstance();
 
 							//Plugin im Vector gespeichert
 							m_vPlugins.add(modul);
 							HOLogger.instance().log(
 								HOMainFrame.class,
 								" Starte " + files[i].getName() + "  (init MiniModel)");
 							interuptionWindow.setInfoText("Start Plugin: " + modul.getName());
 							modul.start(de.hattrickorganizer.model.HOMiniModel.instance());
 
 							HOLogger.instance().log(
 								HOMainFrame.class,
 								"+ " + files[i].getName() + " gestartet");
 						} else {
 							HOLogger.instance().log(
 								HOMainFrame.class,
 								"- " + files[i].getName() + " nicht von IPlugin abgeleitet");
 						}
 					} else {
 						HOLogger.instance().log(
 							HOMainFrame.class,
 							"- " + files[i].getName() + " ist Interface");
 					}
 				} catch (Throwable e2) {
 					HOLogger.instance().log(
 						HOMainFrame.class,
 						"- " + files[i].getName() + " wird bersprungen: " + e2.toString());
 
 					//HOLogger.instance().log(HOMainFrame.class,e2);
 				}
 			}
 		} catch (Exception e) {
 			HOLogger.instance().log(HOMainFrame.class, e);
 		}
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @param changeEvent TODO Missing Method Parameter Documentation
 	 */
 	public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
 		//Wenn ein Tab als Temp gespeichert wurde dieses entfernen
 		if (m_sToRemoveTabName != null) {
 			final int index = m_jtpTabbedPane.indexOfTab(m_sToRemoveTabName);
 
 			if ((index >= 0) && (m_jtpTabbedPane.getTabCount() > index)) {
 				// hier wegen rekursion
 				m_sToRemoveTabName = null;
 				m_jtpTabbedPane.removeTabAt(index);
 			} else {
 				HOLogger.instance().log(
 					HOMainFrame.class,
 					"Fehler Tabremove: "
 						+ m_sToRemoveTabName
 						+ " "
 						+ index
 						+ "/"
 						+ m_jtpTabbedPane.getTabCount());
 				m_sToRemoveTabName = null;
 			}
 		}
 	}
 
 	//----------------Unused Listener----------------------------
 	public void windowActivated(java.awt.event.WindowEvent windowEvent) {
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @param windowEvent TODO Missing Method Parameter Documentation
 	 */
 	public void windowClosed(java.awt.event.WindowEvent windowEvent) {
 		//Wird von dem dispose im beenden aufgerufen, wenn alle windowClosing durch sind
 		System.exit(0);
 	}
 
 	//----------------Listener--------------------------------------
 
 	/**
 	 * Beenden von HO
 	 *
 	 * @param windowEvent TODO Missing Constructuor Parameter Documentation
 	 */
 	public void windowClosing(java.awt.event.WindowEvent windowEvent) {
 		beenden();
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @param windowEvent TODO Missing Method Parameter Documentation
 	 */
 	public void windowDeactivated(java.awt.event.WindowEvent windowEvent) {
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @param windowEvent TODO Missing Method Parameter Documentation
 	 */
 	public void windowDeiconified(java.awt.event.WindowEvent windowEvent) {
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @param windowEvent TODO Missing Method Parameter Documentation
 	 */
 	public void windowIconified(java.awt.event.WindowEvent windowEvent) {
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @param windowEvent TODO Missing Method Parameter Documentation
 	 */
 	public void windowOpened(java.awt.event.WindowEvent windowEvent) {
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @param groesse TODO Missing Method Parameter Documentation
 	 */
 	private void setDefaultFont(int groesse) {
 		try {
 			//com.jgoodies.plaf.plastic.Plastic3DLookAndFeel laf = new com.jgoodies.plaf.plastic.Plastic3DLookAndFeel();
 			final javax.swing.plaf.metal.MetalLookAndFeel laf =
 				new javax.swing.plaf.metal.MetalLookAndFeel();
 			laf.setCurrentTheme(
 				new de.hattrickorganizer.gui.utils.HOTheme(
 					UserParameter.instance().schriftGroesse));
 			javax.swing.UIManager.setLookAndFeel(laf);
 			SwingUtilities.updateComponentTreeUI(this);
 		} catch (Exception e) {
 			HOLogger.instance().log(HOMainFrame.class, e);
 		}
 	}
 
 	/**
 	 * Alle temporren Tabs entfernen
 	 */
 	private void checkTabs() {
 		int index;
 
 		m_jtpTabbedPane.removeChangeListener(this);
 
 		if (gui.UserParameter.instance().tempTabSpieleruebersicht) {
 			index =
 				m_jtpTabbedPane.indexOfTab(
 					HOVerwaltung.instance().getResource().getProperty("Spieleruebersicht"));
 
 			if ((index > 0) && (m_jtpTabbedPane.getTabCount() > index)) {
 				m_jtpTabbedPane.removeTabAt(index);
 			}
 		}
 
 		if (gui.UserParameter.instance().tempTabAufstellung) {
 			index =
 				m_jtpTabbedPane.indexOfTab(
 					HOVerwaltung.instance().getResource().getProperty("Aufstellung"));
 
 			if ((index > 0) && (m_jtpTabbedPane.getTabCount() > index)) {
 				m_jtpTabbedPane.removeTabAt(index);
 			}
 		}
 
 		if (gui.UserParameter.instance().tempTabLigatabelle) {
 			index =
 				m_jtpTabbedPane.indexOfTab(
 					HOVerwaltung.instance().getResource().getProperty("Ligatabelle"));
 
 			if ((index > 0) && (m_jtpTabbedPane.getTabCount() > index)) {
 				m_jtpTabbedPane.removeTabAt(index);
 			}
 		}
 
 		if (gui.UserParameter.instance().tempTabSpiele) {
 			index =
 				m_jtpTabbedPane.indexOfTab(
 					HOVerwaltung.instance().getResource().getProperty("Spiele"));
 
 			if ((index > 0) && (m_jtpTabbedPane.getTabCount() > index)) {
 				m_jtpTabbedPane.removeTabAt(index);
 			}
 		}
 
 		if (gui.UserParameter.instance().tempTabSpieleranalyse) {
 			index =
 				m_jtpTabbedPane.indexOfTab(
 					HOVerwaltung.instance().getResource().getProperty("SpielerAnalyse"));
 
 			if ((index > 0) && (m_jtpTabbedPane.getTabCount() > index)) {
 				m_jtpTabbedPane.removeTabAt(index);
 			}
 		}
 
 		if (gui.UserParameter.instance().tempTabStatistik) {
 			index =
 				m_jtpTabbedPane.indexOfTab(
 					HOVerwaltung.instance().getResource().getProperty("Statistik"));
 
 			if ((index > 0) && (m_jtpTabbedPane.getTabCount() > index)) {
 				m_jtpTabbedPane.removeTabAt(index);
 			}
 		}
 
 		if (gui.UserParameter.instance().tempTabTransferscout) {
 			index =
 				m_jtpTabbedPane.indexOfTab(
 					HOVerwaltung.instance().getResource().getProperty("TransferScout"));
 
 			if ((index > 0) && (m_jtpTabbedPane.getTabCount() > index)) {
 				m_jtpTabbedPane.removeTabAt(index);
 			}
 		}
 
 		if (gui.UserParameter.instance().tempTabArenasizer) {
 			index =
 				m_jtpTabbedPane.indexOfTab(
 					HOVerwaltung.instance().getResource().getProperty("ArenaSizer"));
 
 			if ((index > 0) && (m_jtpTabbedPane.getTabCount() > index)) {
 				m_jtpTabbedPane.removeTabAt(index);
 			}
 		}
 
 		if (gui.UserParameter.instance().tempTabInformation) {
 			index =
 				m_jtpTabbedPane.indexOfTab(
 					HOVerwaltung.instance().getResource().getProperty("Verschiedenes"));
 
 			if ((index > 0) && (m_jtpTabbedPane.getTabCount() > index)) {
 				m_jtpTabbedPane.removeTabAt(index);
 			}
 		}
 
 		m_jtpTabbedPane.addChangeListener(this);
 	}
 
 	/**
 	 * Holt die Parameter aus den Dialogen und speichert sie in der DB
 	 */
 	private void saveUserParameter() {
 		UserParameter parameter = UserParameter.instance();
 
 		final int[] sup = m_jpSpielerUebersicht.getDividerLocations();
 		final int[] ap = m_jpAufstellung.getDividerLocations();
 		final int[] sp = m_jpSpielePanel.getDividerLocations();
 		final int spa = m_jpSpielerAnalysePanel.getDividerLocation();
 		final AufstellungsAssistentPanel aap = m_jpAufstellung.getAufstellungsAssitentPanel();
 		final int tsp = m_jpTransferScout.getDividerLocation();
 
 		final int locx = Math.max(getLocation().x, 0);
 		final int locy = Math.max(getLocation().y, 0);
 		parameter.hoMainFrame_PositionX = locx;
 		parameter.hoMainFrame_PositionY = locy;
 		parameter.hoMainFrame_width =
 			Math.min(getSize().width, getToolkit().getScreenSize().width - locx);
 		parameter.hoMainFrame_height =
 			Math.min(getSize().height, getToolkit().getScreenSize().height - locy);
 		parameter.bestPostWidth =
 			Math.max(m_jpSpielerUebersicht.getBestPosWidth(), m_jpAufstellung.getBestPosWidth());
 
 		parameter.aufstellungsAssistentPanel_gruppe = aap.getGruppe();
 		parameter.aufstellungsAssistentPanel_reihenfolge = aap.getReihenfolge();
 		parameter.aufstellungsAssistentPanel_not = aap.isNotGruppe();
 		parameter.aufstellungsAssistentPanel_cbfilter = aap.isGruppenFilter();
 		parameter.aufstellungsAssistentPanel_idealPosition = aap.isIdealPositionZuerst();
 		parameter.aufstellungsAssistentPanel_form = aap.isFormBeruecksichtigen();
 		parameter.aufstellungsAssistentPanel_verletzt = aap.isVerletztIgnorieren();
 		parameter.aufstellungsAssistentPanel_gesperrt = aap.isGesperrtIgnorieren();
 		parameter.aufstellungsAssistentPanel_notLast = aap.isExcludeLastMatch();
 
 		//      SpielerbersichtsPanel
 		parameter.spielerUebersichtsPanel_horizontalLeftSplitPane = sup[0];
 		parameter.spielerUebersichtsPanel_horizontalRightSplitPane = sup[1];
 		parameter.spielerUebersichtsPanel_verticalSplitPane = sup[2];
 
 		//AufstellungsPanel
 		parameter.aufstellungsPanel_verticalSplitPaneLow = ap[0];
 		parameter.aufstellungsPanel_horizontalLeftSplitPane = ap[1];
 		parameter.aufstellungsPanel_horizontalRightSplitPane = ap[2];
 		parameter.aufstellungsPanel_verticalSplitPane = ap[3];
 
 		//SpielePanel
 		parameter.spielePanel_horizontalLeftSplitPane = sp[0];
 		parameter.spielePanel_verticalSplitPane = sp[1];
 
 		//SpielerAnalyse
 		parameter.spielerAnalysePanel_horizontalSplitPane = spa;
 
 		//      TransferScoutPanel
 		parameter.transferScoutPanel_horizontalSplitPane = tsp;
 
 		DBZugriff.instance().saveUserParameter();
 
 		m_jpSpielerUebersicht.saveColumnOrder();
 		m_jpSpielePanel.saveColumnOrder();
 		m_jpAufstellung.saveColumnOrder();
 		m_jpSpielerAnalysePanel.saveColumnOrder();
 
 	}
 
 	public static void main(String[] args) {
 		final long start = System.currentTimeMillis();
 
 		//Schnauze!
 		// nur wenn Kein Debug
 		if ((args != null) && (args.length > 0)) {
 			String debugLvl = args[0].trim();
 
 			if (debugLvl.equalsIgnoreCase("INFO")) {
 				HOLogger.instance().setLogLevel(HOLogger.INFORMATION);
 			} else if (debugLvl.equalsIgnoreCase("DEBUG")) {
 				HOLogger.instance().setLogLevel(HOLogger.DEBUG);
 			} else if (debugLvl.equalsIgnoreCase("WARNING")) {
 				HOLogger.instance().setLogLevel(HOLogger.WARNING);
 			}
 		}
 
 		// Set HOE file
 		FileExtensionManager.createDirFile();
 
 		// Usermanagement Login-Dialog
 		try {
 			if (!User.getCurrentUser().isSingleUser()) {
 				JComboBox comboBox = new JComboBox(User.getAllUser().toArray());
 				int choice =
 					JOptionPane.showConfirmDialog(
 						null,
 						comboBox,
 						"Login",
 						JOptionPane.OK_CANCEL_OPTION,
 						JOptionPane.QUESTION_MESSAGE);
 
 				if (choice == JOptionPane.OK_OPTION) {
 					User.INDEX = comboBox.getSelectedIndex();
 				} else {
 					System.exit(0);
 				}
 			}
 		} catch (Exception ex) {
 			HOLogger.instance().log(HOMainFrame.class, ex);
 		}
 
 		////Spoofing test
 		try {
 			final java.io.BufferedReader buffy =
 				new java.io.BufferedReader(new java.io.FileReader("ident.txt"));
 			final java.util.Vector ids = new java.util.Vector();
 			String tmp = "";
 
 			while (buffy.ready()) {
 				tmp = buffy.readLine();
 
 				if (!tmp.startsWith("#") && !tmp.trim().equals("")) {
 					ids.add(tmp);
 				}
 			}
 
 			buffy.close();
 
 			if (ids.size() > 0) {
 				//Math.floor(Math.random()*10)
 				de.hattrickorganizer.net.MyConnector.m_sIDENTIFIER =
 					ids.get((int) Math.floor(Math.random() * ids.size())).toString();
 			}
 		} catch (Exception e) {
 		}
 
 		if (LIMITED) {
 			final java.sql.Timestamp datum = new java.sql.Timestamp(System.currentTimeMillis());
 
 			if (datum.after(Timestamp.valueOf(LIMITED_DATE))) {
 				javax.swing.JOptionPane.showMessageDialog(
 					null,
 					"Download new Version at "+ MyConnector.getHOSite(),
 					"Update required",
 					javax.swing.JOptionPane.ERROR_MESSAGE);
 				System.exit(1);
 			}
 		}
 
 		//Startbild
 		final de.hattrickorganizer.gui.utils.InterruptionWindow interuptionsWindow =
 			new de.hattrickorganizer.gui.utils.InterruptionWindow(
 				new de.hattrickorganizer.gui.templates.ImagePanel());
 
 		// Backup
 		if (User.getCurrentUser().isHSQLDB()) {
 			interuptionsWindow.setInfoText("Backup Database");
 			BackupHelper.backup(new File(User.getCurrentUser().getDBPath()));
 		}
 
 		//Standardparameter aus der DB holen
 		interuptionsWindow.setInfoText("Initialize Database");
 		de.hattrickorganizer.database.DBZugriff.instance().loadUserParameter();
 
 		//Init!
 		interuptionsWindow.setInfoText("Initialize Data-Administration");
 
 		//.setArgs ( args );
 		HOVerwaltung.instance();
 
 		//Beim ersten Start Sprache erfragen
 		if (de.hattrickorganizer.database.DBZugriff.instance().isFirstStart()) {
 			interuptionsWindow.setVisible(false);
 			new de.hattrickorganizer.gui.menu.option.InitOptionsDialog();
 			JOptionPane.showMessageDialog(
 				null,
 				"Remember: You have to enter the Securitycode in the loginscreen, NOT the password.",
 				"Securitycode",
 				JOptionPane.INFORMATION_MESSAGE);
 			interuptionsWindow.setVisible(true);
 		}
 
 		//Check -> Sprachdatei in Ordnung?
 		interuptionsWindow.setInfoText("Check Languagefiles");
 		checkSprachFile(UserParameter.instance().sprachDatei);
 
 		final ClassLoader loader =
 			new de.hattrickorganizer.gui.templates.ImagePanel().getClass().getClassLoader();
 
 		//java.net.URL    url     =   loader.getResource ("sprache/"+UserParameter.instance ().sprachDatei+".properties");
 		HOVerwaltung.instance().setResource(UserParameter.instance().sprachDatei, loader);
 		interuptionsWindow.setInfoText("Load latest Data");
 		HOVerwaltung.instance().loadLatestHoModel();
 		interuptionsWindow.setInfoText("Load  XtraDaten");
 
 		// TableColumn
 		UserColumnController.instance().load();
 
 		//Die Whrung auf die aus dem HRF setzen
 		float faktorgeld =
 			(float) HOVerwaltung.instance().getModel().getXtraDaten().getCurrencyRate();
 
 		if (faktorgeld > -1) {
 			gui.UserParameter.instance().faktorGeld = faktorgeld;
 		}
 
 		//Training
 		interuptionsWindow.setInfoText("Initialize Training");
 
 		//Training erstellen -> dabei Trainingswochen berechnen auf Grundlage der manuellen DB Eintrge
 		de.hattrickorganizer.logik.TrainingsManager.instance().calculateTrainings(
 			de.hattrickorganizer.database.DBZugriff.instance().getTrainingsVector());
 
 		//INIT + Dann Pluginsstarten , sonst endlos loop da instance() sich selbst aufruft!
 		interuptionsWindow.setInfoText("Starting Plugins");
 		HOMainFrame.instance().startPluginModuls(interuptionsWindow);
 
 		HOMainFrame
 			.instance()
 			.getAufstellungsPanel()
 			.getAufstellungsPositionsPanel()
 			.exportOldLineup(
 			"Actual");
 		FileExtensionManager.extractLineup("Actual");
 		//Anzeigen
 		interuptionsWindow.setInfoText("Prepare to show");
 		HOMainFrame.instance().setVisible(true);
 
 		//Startbild weg
 		interuptionsWindow.setVisible(false);
 
 		if (de.hattrickorganizer.logik.GebChecker.checkTWGeb()) {
 			new de.hattrickorganizer.gui.birthday.GebDialog(
 				HOMainFrame.instance(),
 				"gui/bilder/tw.jpg");
 		}
 
 		if (de.hattrickorganizer.logik.GebChecker.checkVFGeb()) {
 			new de.hattrickorganizer.gui.birthday.GebDialog(
 				HOMainFrame.instance(),
 				"gui/bilder/vf.jpg");
 		}
 
 		new ExtensionListener().run();
 		HOLogger.instance().log(HOMainFrame.class, "Zeit:" + (System.currentTimeMillis() - start));
 
 	}
 
 	public static int getHOStatus() {
 		return status;
 	}
 
 	public static void setHOStatus(int i) {
 		status = i;
 	}
 
 }
