 /*
  * Copyright (c) 2004-2013 Universidade do Porto - Faculdade de Engenharia
  * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
  * All rights reserved.
  * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
  *
  * This file is part of Neptus, Command and Control Framework.
  *
  * Commercial Licence Usage
  * Licencees holding valid commercial Neptus licences may use this file
  * in accordance with the commercial licence agreement provided with the
  * Software or, alternatively, in accordance with the terms contained in a
  * written agreement between you and Universidade do Porto. For licensing
  * terms, conditions, and further information contact lsts@fe.up.pt.
  *
  * European Union Public Licence - EUPL v.1.1 Usage
  * Alternatively, this file may be used under the terms of the EUPL,
  * Version 1.1 only (the "Licence"), appearing in the file LICENCE.md
  * included in the packaging of this file. You may not use this work
  * except in compliance with the Licence. Unless required by applicable
  * law or agreed to in writing, software distributed under the Licence is
  * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
  * ANY KIND, either express or implied. See the Licence for the specific
  * language governing permissions and limitations at
  * https://www.lsts.pt/neptus/licence.
  *
  * For more information please see <http://lsts.fe.up.pt/neptus>.
  *
  * Author: Paulo Dias
  * 2009/09/12
  */
 package pt.up.fe.dceg.neptus.util.logdownload;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dialog.ModalityType;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.Vector;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.GroupLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.SortOrder;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.PoolingClientConnectionManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.util.EntityUtils;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.Node;
 import org.dom4j.io.SAXReader;
 import org.jdesktop.swingx.JXCollapsiblePane;
 import org.jdesktop.swingx.JXLabel;
 import org.jdesktop.swingx.JXPanel;
 import org.jdesktop.swingx.painter.CompoundPainter;
 import org.jdesktop.swingx.painter.GlossPainter;
 import org.jdesktop.swingx.painter.RectanglePainter;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.servlet.Context;
 import org.mortbay.jetty.servlet.ServletHolder;
 import org.xml.sax.SAXException;
 
 import pt.up.fe.dceg.neptus.NeptusLog;
 import pt.up.fe.dceg.neptus.colormap.ColorMap;
 import pt.up.fe.dceg.neptus.colormap.ColorMapFactory;
 import pt.up.fe.dceg.neptus.colormap.InterpolationColorMap;
 import pt.up.fe.dceg.neptus.doc.NeptusDoc;
 import pt.up.fe.dceg.neptus.gui.MiniButton;
 import pt.up.fe.dceg.neptus.gui.NudgeGlassPane;
 import pt.up.fe.dceg.neptus.gui.swing.MessagePanel;
 import pt.up.fe.dceg.neptus.i18n.I18n;
 import pt.up.fe.dceg.neptus.mra.NeptusMRA;
 import pt.up.fe.dceg.neptus.util.FileUtil;
 import pt.up.fe.dceg.neptus.util.GuiUtils;
 import pt.up.fe.dceg.neptus.util.ImageUtils;
 import pt.up.fe.dceg.neptus.util.MathMiscUtils;
 import pt.up.fe.dceg.neptus.util.conf.ConfigFetch;
 import foxtrot.AsyncTask;
 import foxtrot.AsyncWorker;
 
 /**
  * @author pdias
  *
  */
 @NeptusDoc(ArticleFilename = "logs-downloader/logs-downloader.html#worker", ArticleTitle = "Logs Downloader Worker", Section = "Logs")
 public class LogsDownloaderWorker {
 	
     private static final String DEFAULT_TITLE = I18n.text("Download Log Files");
     private boolean dummyLists = false;
 	private String dummyListFx = "srcTests/pt/up/fe/dceg/neptus/util/logdownload/listBig";
 	private String dummyListXmlFx = "srcTests/pt/up/fe/dceg/neptus/util/logdownload/listBig.xml";
 
 //	private static Image ICON = GuiUtils.getImage("images/neptus-icon.png");
 
 	public static final ImageIcon ICON_DOWNLOAD_FOLDERS = ImageUtils.getScaledIcon("images/downloader/folder_download.png", 32, 32);
 	public static final ImageIcon ICON_DOWNLOAD_FILES = ImageUtils.getScaledIcon("images/downloader/file_down.png", 32, 32);
 	public static final ImageIcon ICON_DOWNLOAD_LIST = ImageUtils.getScaledIcon("images/downloader/sync-list.png", 32, 32);
 	public static final ImageIcon ICON_SETTINGS = ImageUtils.getScaledIcon("images/settings.png", 32, 32);
 	public static final ImageIcon ICON_DELETE_FOLDERS = ImageUtils.getScaledIcon("images/downloader/folder_delete1.png", 32, 32);
 	public static final ImageIcon ICON_DELETE_FILES = ImageUtils.getScaledIcon("images/downloader/file_delete1.png", 32, 32);
 	public static final ImageIcon ICON_HELP = ImageUtils.getScaledIcon("images/downloader/help.png", 32, 32);
     public static final ImageIcon ICON_RESET = ImageUtils.getScaledIcon("images/buttons/redo.png", 32, 32);
     public static final ImageIcon ICON_STOP = ImageUtils.getScaledIcon("images/downloader/stop.png", 32, 32);
 	
 	private static final ColorMap diskFreeColorMap = ColorMapFactory.createInvertedColorMap((InterpolationColorMap) 
 			ColorMapFactory.createRedYellowGreenColorMap());
 
     protected static final long DELTA_TIME_TO_CLEAR_DONE = 5000;
     protected static final long DELTA_TIME_TO_CLEAR_NOT_WORKING = 45000;
 
     private int totalConnectionsAllowed = 200;
     private int connectionsPerRouteAllowed = 1; //4;
     private int connectionTimeout = 30000;
     private int connectionTimeout4List = 5000;
 	
 	private HttpClient client, client4List;
 	private PoolingClientConnectionManager httpConnectionManager, httpConnectionManager4List;
 	private HashSet<HttpRequestBase> listActiveGetMethods = new HashSet<HttpRequestBase>();
 
 	private String host = "127.0.0.1";
 	private int port = 8080;
 	private String basePath = "/dune/logs/"; //= "/images/";
 	private String deleteUriCommand = "delete/";
     // private String baseAgentPath = "/dune/agent";
 
 	private String dirBaseToStoreFiles = "log/downloaded";
 	/// This should be a word with no spaces
 	private String logLabel = I18n.text("unknown");
 
     private boolean frameIsExternalControlled = false;
 
 	//Actions
 	private AbstractAction downloadListAction = null;
 	private AbstractAction downloadSelectedLogDirsAction = null;
 	private AbstractAction downloadSelectedLogFilesAction = null;
 	private AbstractAction deleteSelectedLogFoldersAction = null;
 	private AbstractAction deleteSelectedLogFilesAction = null;
 	private AbstractAction toggleConfPanelAction = null;
 	private AbstractAction toggleExtraInfoPanelAction = null;
 	private AbstractAction helpAction = null;
 	private AbstractAction resetAction = null;
 	private AbstractAction stopAllAction = null;
 	
 	// UI
 	private JFrame frame = null;
 	private JXPanel frameCompHolder = null;
 	private JTextField hostField = null;
 	private JTextField portField = null;
 	private JTextField baseUriField = null;
 	private JTextField logLabelField = null;
 	private JLabel hostLabel = null;
 	private JLabel portLabel = null;
 	private JLabel baseUriLabel = null;
 	private JLabel logLabelLabel = null;
 	private MessagePanel msgPanel = null;
 	private JXLabel logFoldersListLabel = null;
 	private JXLabel logFilesListLabel = null;
 	//private DefaultListModel downloadWorkersListModel = null;
 	private JPanel downloadWorkersHolder = null;
 	private JScrollPane downloadWorkersScroll = null;
 	private LogFolderInfoList logFolderList = null;
 	private JScrollPane logFolderScroll = null;
 	private LogFileInfoList logFilesList = null;
 	private JScrollPane logFilesScroll = null;
 	
 	private JXLabel diskFreeLabel = null;
 	
 	private MiniButton downloadListButton = null;
 	private MiniButton downloadSelectedLogDirsButton = null;
 	private MiniButton downloadSelectedLogFilesButton = null;
 	private MiniButton deleteSelectedLogFoldersButton = null;
 	private MiniButton deleteSelectedLogFilesButton = null;
 
 	private MiniButton toggleConfPanelButton = null;
 	private MiniButton toggleExtraInfoPanelButton = null;
 
 	private MiniButton helpButton = null;
 	private MiniButton resetButton = null;
 	private MiniButton stopAllButton = null;
 	private DownloaderHelp downHelpDialog = null;
 
 	private JXPanel configHolder = null;
 	private JXCollapsiblePane configCollapsiblePanel = null;
 	private JXCollapsiblePane extraInfoCollapsiblePanel = null;
 
 	private JProgressBar listHandlingProgressBar = null;
 
 	//Background Painter Stuff
     private RectanglePainter rectPainter;
     private CompoundPainter<JXPanel> compoundBackPainter;
 
 	
 	private Timer timer = null;
 	private TimerTask ttaskLocalDiskSpace = null;
 
 	/**
 	 * This will create a panel and a frame to control the logs downloading.
 	 * Use {@link #setVisible(boolean)} to show the frame.
 	 */
 	public LogsDownloaderWorker() {
 	    this(null);
 	}
 
 	/**
 	 * If a parent frame is given, it only be used for parent dialogs and related,
 	 * the created panel will not be added to it (use in this case {@link #getContentPanel()}
 	 * to get the content panel).
 	 * <br>
 	 * The {@link #setVisible(boolean)} will work the same. 
 	 */
 	public LogsDownloaderWorker(JFrame parentFrame) {
 	    if (parentFrame != null) {
 	        frame = parentFrame;
 	        frameIsExternalControlled = true;
 	    }
 	    
 		initializeComm();
 		initialize();
 	}
 
 	private void initializeComm() {
 		SchemeRegistry schemeRegistry = new SchemeRegistry();
         schemeRegistry.register(
                 new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
         schemeRegistry.register(
                 new Scheme("https", 443, PlainSocketFactory.getSocketFactory()));
 		httpConnectionManager = new PoolingClientConnectionManager(schemeRegistry); // to change to PoolingClientConnectionManager
 		httpConnectionManager.setMaxTotal(totalConnectionsAllowed);
 		httpConnectionManager.setDefaultMaxPerRoute(connectionsPerRouteAllowed);
 
 		httpConnectionManager4List = new PoolingClientConnectionManager(schemeRegistry);
 		httpConnectionManager4List.setMaxTotal(totalConnectionsAllowed);
 		httpConnectionManager4List.setDefaultMaxPerRoute(1);
 
 		HttpParams params = new BasicHttpParams();
 		HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
 		client = new DefaultHttpClient(httpConnectionManager, params);
 		
 		params = new BasicHttpParams();
         HttpConnectionParams.setConnectionTimeout(params, connectionTimeout4List);
         client4List = new DefaultHttpClient(httpConnectionManager4List, params);
 	}
 
 	/**
 	 * 
 	 */
 	private void initialize() {
 		initializeActions();
 
 		if (frame == null) {
 		    frame = new JFrame();
 		    frame.setSize(900, 560);
 		    frame.setIconImages(ConfigFetch.getIconImagesForFrames());
 		    frame.setTitle(DEFAULT_TITLE + " - " + logLabel);
 		}
 
 		hostLabel = new JLabel(I18n.text("Host: ")); 
 		hostField = new JTextField(20);
 		hostField.setText(host);
 		portLabel = new JLabel(I18n.text("Port: ")); 
 		portField = new JTextField(5);
 		//portField = new JFormattedTextField(NumberFormat.getInstance());
 		portField.setText(""+port);
 		baseUriLabel = new JLabel(I18n.text("Base URI Path: ")); 
 		baseUriField = new JTextField(40);
 		baseUriField.setText(basePath);
 		logLabelLabel = new JLabel(I18n.text("System Label: ")); 
 		logLabelField = new JTextField(40);
 		logLabelField.setText(logLabel);
         logLabelField.setToolTipText(I18n.text("This will dictate the directory where the logs will go."));
 		
 		msgPanel = new MessagePanel();
 		msgPanel.showButtons(false);
 
 		logFoldersListLabel = new JXLabel("<html><b>" + I18n.text("Log Folders"), JLabel.CENTER);
 		logFilesListLabel = new JXLabel("<html><b>" + I18n.text("Log Files"), JLabel.CENTER);
 
 		diskFreeLabel = new JXLabel("<html><b>?", JLabel.CENTER);
 		diskFreeLabel.setBackgroundPainter(getCompoundBackPainter());
 
 		resetButton = new MiniButton();
 		resetButton.setToolTipText(I18n.text("Reset the interface"));
 		resetButton.setIcon(ICON_RESET);
 		resetButton.addActionListener(resetAction);
 
 		stopAllButton = new MiniButton();
 		stopAllButton.setToolTipText(I18n.text("Stop all log downloads"));
 		stopAllButton.setIcon(ICON_STOP);
 		stopAllButton.addActionListener(stopAllAction);
 
 		
 		downloadWorkersHolder = new JPanel();
 		downloadWorkersHolder.setLayout(new BoxLayout(downloadWorkersHolder, BoxLayout.Y_AXIS));
 		downloadWorkersHolder.setBackground(Color.WHITE);
 		
 		downloadWorkersScroll = new JScrollPane();
 		downloadWorkersScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		downloadWorkersScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		//downloadWorkersScroll.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
 		//downloadWorkersScroll.setPreferredSize(new Dimension(800,600));
 		downloadWorkersScroll.setViewportView(downloadWorkersHolder);
 
 		logFolderList = new LogFolderInfoList();
 		//logFolderList.setVisibleRowCount(10);
 		logFolderList.setSortable(true); // logFolderList.setFilterEnabled(true); // Changed from swingx 1.6.+ !!!!
 		logFolderList.setAutoCreateRowSorter(true);
 		logFolderList.setSortOrder(SortOrder.DESCENDING);
 		logFolderList.addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				//System.out.println(""+e.getValueIsAdjusting());
 				if (e.getValueIsAdjusting())
 					return;
 //				SwingUtilities.invokeLater(new Runnable() {
 //					@Override
 //					public void run() {
 //						updateFilesListGUIForFolderSelected();
 //					}
 //				});
 				AsyncTask task = new AsyncTask() {
                     @Override
                     public Object run() throws Exception {
                         updateFilesListGUIForFolderSelected();
                         return null;
                     }
                     /* (non-Javadoc)
                      * @see foxtrot.AsyncTask#finish()
                      */
                     @Override
                     public void finish() {
                         logFilesList.setValueIsAdjusting(false);
                         logFilesList.invalidate();
                         logFilesList.validate();
                         logFilesList.setEnabled(true);
                     }
 				};
 				AsyncWorker.getWorkerThread().postTask(task);
 			}
 		});
 		
         logFolderList.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 if (e.getButton() == MouseEvent.BUTTON3) {
                    LogFolderInfo selectedFolder = (LogFolderInfo) logFolderList.getSelectedValue();
 
                    if (selectedFolder.getState() == LogFolderInfo.State.SYNC
                            || selectedFolder.getState() == LogFolderInfo.State.LOCAL) {
                         new Thread(new Runnable() {
                             @Override
                             public void run() {
                                 JFrame mra = new NeptusMRA();
                                 mra.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                                String baseFxPath = dirBaseToStoreFiles + "/" + getLogLabel() + "/"
                                        + logFolderList.getSelectedValue() + "/";
                                File fx = new File(baseFxPath + "Data.lsf");
                                if (!fx.exists())
                                    fx = new File(baseFxPath + "Data.lsf.gz");
                                 ((NeptusMRA) mra).openLog(fx);
                             }
                         }).run();
                     }
                     else {
                        warnMsg(I18n.text("Log folder not synchronized... Can't open MRA"));
                         return;
                     }
                 }
             }
         });
 		
 		logFolderScroll = new JScrollPane();
 		logFolderScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		logFolderScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		//logFolderScroll.setBorder(BorderFactory.createEmptyBorder(3,5,3,5));
 		logFolderScroll.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
 		//logFolderScroll.setPreferredSize(new Dimension(800,600));
 		logFolderScroll.setViewportView(logFolderList);
 
 		logFilesList = new LogFileInfoList();
 		//logFilesList.setVisibleRowCount(12);
 		logFilesList.setSortable(true); // logFilesList.setFilterEnabled(true); // Changed from swingx 1.6.+ !!!!
 		logFilesList.setAutoCreateRowSorter(true);
 		logFilesList.setSortOrder(SortOrder.DESCENDING);
 
 		logFilesScroll = new JScrollPane();
 		logFilesScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		logFilesScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		//logFilesScroll.setBorder(BorderFactory.createEmptyBorder(3,5,3,5));
 		logFilesScroll.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
 		//logFolderScroll.setPreferredSize(new Dimension(800,600));
 		logFilesScroll.setViewportView(logFilesList);
 
 		downloadListButton = new MiniButton();
 		downloadListButton.setToolTipText(I18n.text("Synchronize List of Log Folders"));
 		//downloadListButton.setActionCommand("get list");
 		downloadListButton.setIcon(ICON_DOWNLOAD_LIST);
 		downloadListButton.addActionListener(downloadListAction);
 		
 		downloadSelectedLogDirsButton = new MiniButton();
 		downloadSelectedLogDirsButton.setToolTipText(I18n.text("Synchronize Selected Log Folders"));
 		//downloadSelectedLogDirsButton.setActionCommand("get list");
 		downloadSelectedLogDirsButton.setIcon(ICON_DOWNLOAD_FOLDERS);
 		downloadSelectedLogDirsButton.addActionListener(downloadSelectedLogDirsAction);
 
 		
 		downloadSelectedLogFilesButton = new MiniButton();
 		downloadSelectedLogFilesButton.setToolTipText(I18n.text("Synchronize Selected Log Files"));
 		// downloadSelectedLogFilesButton.setActionCommand("");
 		downloadSelectedLogFilesButton.setIcon(ICON_DOWNLOAD_FILES);
 		//downloadSelectedLogFilesButton.setEnabled(false);
 		downloadSelectedLogFilesButton.addActionListener(downloadSelectedLogFilesAction);
 		
 		
 		deleteSelectedLogFoldersButton = new MiniButton();
 		deleteSelectedLogFoldersButton.setToolTipText(I18n.text("Delete Selected Log Folders"));
 		// deleteSelectedLogFoldersButton("");
 		deleteSelectedLogFoldersButton.setIcon(ICON_DELETE_FOLDERS);
 		//deleteSelectedLogFoldersButton.setEnabled(false);
 		deleteSelectedLogFoldersButton.addActionListener(deleteSelectedLogFoldersAction);
 
 		deleteSelectedLogFilesButton = new MiniButton();
 		deleteSelectedLogFilesButton.setToolTipText(I18n.text("Delete Selected Log Files"));
 		// deleteSelectedLogFilesButton("");
 		deleteSelectedLogFilesButton.setIcon(ICON_DELETE_FILES);
 //		deleteSelectedLogFilesButton.setEnabled(false);
 		deleteSelectedLogFilesButton.addActionListener(deleteSelectedLogFilesAction);
 //		deleteSelectedLogFilesButton.setVisible(false);
 
 		
 		//Config Panel Setup
 		configCollapsiblePanel = new JXCollapsiblePane();
 		configCollapsiblePanel.setLayout(new BorderLayout());
 		configHolder = new JXPanel();
 		configHolder.setBorder(new TitledBorder(I18n.text("Configuration")));
 		configCollapsiblePanel.add(configHolder, BorderLayout.CENTER);
 		GroupLayout layoutCfg = new GroupLayout(configHolder);
 		configHolder.setLayout(layoutCfg);
 		layoutCfg.setAutoCreateGaps(true);
 		layoutCfg.setAutoCreateContainerGaps(false);
 		layoutCfg.setHorizontalGroup(
 			layoutCfg.createParallelGroup(GroupLayout.Alignment.CENTER)
     			.addGroup(layoutCfg.createSequentialGroup()
 	  				.addComponent(hostLabel)
 	  				.addComponent(hostField)
 	  				.addComponent(portLabel)
 	  				.addComponent(portField)
 	  			)
     			.addGroup(layoutCfg.createSequentialGroup()
 	  				.addComponent(baseUriLabel)
 	  				.addComponent(baseUriField)
 	  			)
     			.addGroup(layoutCfg.createSequentialGroup()
 	  				.addComponent(logLabelLabel)
 	  				.addComponent(logLabelField)
 	  			)
 		);
 		layoutCfg.setVerticalGroup(
 				layoutCfg.createSequentialGroup()
     			.addGroup(layoutCfg.createParallelGroup(GroupLayout.Alignment.LEADING)
     	  				.addComponent(hostLabel)
     	  				.addComponent(hostField)
     	  				.addComponent(portLabel)
     	  				.addComponent(portField)
     	  			)
         			.addGroup(layoutCfg.createParallelGroup(GroupLayout.Alignment.LEADING)
     	  				.addComponent(baseUriLabel)
     	  				.addComponent(baseUriField)
     	  			)
         			.addGroup(layoutCfg.createParallelGroup(GroupLayout.Alignment.LEADING)
     	  				.addComponent(logLabelLabel)
     	  				.addComponent(logLabelField)
     	  			)
 			);
 		layoutCfg.linkSize(SwingConstants.VERTICAL, hostLabel, hostField,
 				portLabel, portField, baseUriLabel, baseUriField,
 				logLabelLabel, logLabelField);
 		layoutCfg.linkSize(SwingConstants.HORIZONTAL, baseUriLabel, 
 				logLabelLabel, hostLabel);
 
 		// This is called here (After the group layout configuration) because of an IllegalStateException during collape redraw
 		configCollapsiblePanel.setCollapsed(true);  
 		
 //		//Extra Info Panel Setup
 //		extraInfoCollapsiblePanel = new JXCollapsiblePane();
 //		extraInfoCollapsiblePanel.setCollapsed(false);
 //		//extraInfoCollapsiblePanel.setLayout(new BorderLayout());
 //		extraInfoHolder = new JXPanel();
 //		extraInfoHolder.setLayout(new BoxLayout(extraInfoHolder, BoxLayout.Y_AXIS));
 //		extraInfoHolder.add(msgPanel);
 //		extraInfoHolder.add(downloadWorkersScroll);
 //		extraInfoCollapsiblePanel.add(extraInfoHolder);//, BorderLayout.CENTER);
 		
 		
 		//Collapsible Panel Show/Hide buttons
 		toggleConfPanelButton = new MiniButton();
 		toggleConfPanelButton.setToolTipText(I18n.text("Show/Hide Configuration Panel"));
 		toggleConfPanelButton.setIcon(ICON_SETTINGS);
 		toggleConfPanelButton.addActionListener(toggleConfPanelAction);
 
 		toggleExtraInfoPanelButton = new MiniButton();
 		toggleExtraInfoPanelButton.setToolTipText(I18n.text("Show/Hide Download Panel"));
 		toggleExtraInfoPanelButton.setIcon(ICON_SETTINGS);
 		toggleExtraInfoPanelButton.addActionListener(toggleExtraInfoPanelAction);
 
 
 		helpButton = new MiniButton();
 		helpButton.setToolTipText(I18n.text("Show Help"));
 		helpButton.setIcon(ICON_HELP);
 		helpButton.addActionListener(helpAction);
 
 		listHandlingProgressBar = new JProgressBar(JProgressBar.HORIZONTAL);
 		listHandlingProgressBar.setIndeterminate(false);
 		listHandlingProgressBar.setStringPainted(true);
 		listHandlingProgressBar.setString("");
 		
 		//Setup main content panel
 		JPanel contentPanel = new JPanel();
 		GroupLayout layout = new GroupLayout(contentPanel);
 		contentPanel.setLayout(layout);
 		layout.setAutoCreateGaps(true);
         layout.setAutoCreateContainerGaps(true);
         layout.setHorizontalGroup(
     		layout.createParallelGroup(GroupLayout.Alignment.CENTER)
     			.addGroup(layout.createSequentialGroup()
         			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
             			.addComponent(logFoldersListLabel)
 	    				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
 	    					.addComponent(logFolderScroll)
 	    					.addGroup(layout.createSequentialGroup()
 	    						.addComponent(downloadListButton, 34, 34, 34)
 	    						.addGap(10)
 	    						.addComponent(downloadSelectedLogDirsButton, 34, 34, 34)
 	    						.addComponent(downloadSelectedLogFilesButton, 34, 34, 34)
 	    						.addGap(10)
 	    						.addComponent(deleteSelectedLogFoldersButton, 34, 34, 34)
 	    						.addComponent(deleteSelectedLogFilesButton, 34, 34, 34)
 	    						.addGap(10)
                                 .addComponent(stopAllButton, 34, 34, 34)
                                 .addGap(10)
 	    						.addComponent(toggleConfPanelButton, 34, 34, 34)
 //	    						.addComponent(toggleExtraInfoPanelButton, 25, 25, 25)
 	    						.addGap(10)
 	    						.addComponent(resetButton, 34, 34, 34)
 	    						.addComponent(helpButton, 34, 34, 34)
 	    						.addComponent(diskFreeLabel, 60, 80, 120)
 	    					)
 	    				)
 	    			)
     				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
     						.addComponent(logFilesListLabel)
     						.addComponent(logFilesScroll)
     				)
     			)
 //	  			.addComponent(msgPanel)
     			.addComponent(listHandlingProgressBar)
 	  			.addComponent(downloadWorkersScroll)
 		);
         layout.setVerticalGroup(
     		layout.createSequentialGroup()
     			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
         				.addGroup(layout.createSequentialGroup()
         					.addComponent(logFoldersListLabel)
         					.addGroup(layout.createSequentialGroup()
 	        					.addComponent(logFolderScroll, 180, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
 	        					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
 	    	    					.addComponent(downloadListButton, 34, 34, 34)
 	        						.addComponent(downloadSelectedLogDirsButton, 34, 34, 34)
 	        						.addComponent(downloadSelectedLogFilesButton, 34, 34, 34)
 	        						.addComponent(deleteSelectedLogFoldersButton, 34, 34, 34)
 	        						.addComponent(deleteSelectedLogFilesButton, 34, 34, 34)
                                     .addComponent(stopAllButton, 34, 34, 34)
 		    						.addComponent(toggleConfPanelButton, 34, 34, 34)
 //		    						.addComponent(toggleExtraInfoPanelButton, 25, 25, 25)
 		    						.addComponent(resetButton, 34, 34, 34)
 		    						.addComponent(helpButton, 34, 34, 34)
 	    	    					.addComponent(diskFreeLabel, 34, 34, 34)
 	        					)
         					)
         				)
     				.addGroup(layout.createSequentialGroup()
     						.addComponent(logFilesListLabel)
     	    				.addComponent(logFilesScroll, 200, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
     				)
     			)
 //	  			.addComponent(msgPanel, 80, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
     			.addComponent(listHandlingProgressBar)
 	  			.addComponent(downloadWorkersScroll, 80, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 		);
         //layout.linkSize(SwingUtilities.VERTICAL, msgPanel, downloadWorkersScroll, logFilesScroll);
         //layout.linkSize(SwingConstants.HORIZONTAL, logFolderScroll, foldersListLabel);
         //layout.linkSize(SwingConstants.HORIZONTAL, logFilesScroll, filesListLabel);
 
         
         //Setup of the Frame Content
 		frameCompHolder = new JXPanel();
 		frameCompHolder.setLayout(new BorderLayout());
 		frameCompHolder.add(configCollapsiblePanel, BorderLayout.NORTH);
 		frameCompHolder.add(contentPanel, BorderLayout.CENTER);
 		//frameCompHolder.add(extraInfoCollapsiblePanel, BorderLayout.SOUTH);
 		
 		if (!frameIsExternalControlled) {
 		    frame.setLayout(new BorderLayout());
 		    frame.add(frameCompHolder, BorderLayout.CENTER);
 		}
 
 		downHelpDialog = new DownloaderHelp(frame);
 		
         setEnableLogLabel(false);
 		setVisibleBasePath(false);
 		
 		setEnableHost(true);
         
 		if (!frameIsExternalControlled)
 		    GuiUtils.centerOnScreen(frame);
         
         timer = new Timer("LogsDownloadWorker");
         ttaskLocalDiskSpace = getTimerTaskLocalDiskSpace();
         timer.scheduleAtFixedRate(ttaskLocalDiskSpace, 500, 5000);
 	}
 
 
 	/**
 	 * 
 	 */
 	@SuppressWarnings("serial")
 	private void initializeActions() {
 		downloadListAction = new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (!validateAndSetUI()) {
 					popupErrorConfigurationDialog();
 					return;
 				}
 				AsyncTask task = new AsyncTask() {
 					@Override
 					public Object run() throws Exception {
 						//long time = System.currentTimeMillis();
 						//System.out.println(".......downloadListAction");
 						listHandlingProgressBar.setValue(0);
 						listHandlingProgressBar.setString(I18n.text("Starting..."));
 
 						downloadListButton.setEnabled(false);
 						//logFolderList.setEnabled(false);
 						logFolderList.setValueIsAdjusting(true);
 						//logFilesList.setEnabled(false);
 						
 						//->Getting txt list of logs from server
 						listHandlingProgressBar.setValue(10);
 						listHandlingProgressBar.setIndeterminate(true);
 						listHandlingProgressBar.setString(I18n.text("Connecting to remote system for log list update..."));
 //						long timeD1 = System.currentTimeMillis();
 						LinkedHashSet<String> retList = getLogsListFromServer();
 						//System.out.println(".......get list from server " + (System.currentTimeMillis()-timeD1));
 						if (retList == null) {
 						    msgPanel.writeMessageTextln(I18n.text("Done"));
 							return null;
 						}
 						msgPanel.writeMessageTextln(I18n.textf("Log Folders: %numberoffolders", retList.size()));
 						
 						if (retList.size() == 0) {
 							//TODO
 							return null;
 						}
 						
 						//->Removing from already existing LogFolders to LOCAL state
 						listHandlingProgressBar.setValue(20);
 						listHandlingProgressBar.setIndeterminate(false);
 						listHandlingProgressBar.setString(I18n.text("Filtering list..."));
 						//long timeC1 = System.currentTimeMillis();
 						Object[] objArray = new Object[logFolderList.myModel.size()];
 						logFolderList.myModel.copyInto(objArray);
 						for (Object comp : objArray) {
 							try {
 								LogFolderInfo log = (LogFolderInfo) comp;
 								if (!retList.contains(log.getName())) {
 									//retList.remove(log.getName());
 									for (LogFileInfo lfx : log.logFiles) {
 										lfx.setState(LogFolderInfo.State.LOCAL);
 									}
 									log.setState(LogFolderInfo.State.LOCAL);
 								}
 							}
 							catch (Exception e) {
 							}
 						}
 						//System.out.println(".......Removing from already existing LogFolders to LOCAL state " + (System.currentTimeMillis()-timeC1));
 						
 						//->Adding new LogFolders
 						LinkedList<LogFolderInfo> existenteLogFoldersFromServer = new LinkedList<LogFolderInfo>();
 						LinkedList<LogFolderInfo> newLogFoldersFromServer = new LinkedList<LogFolderInfo>();
 						for (String newLogName : retList) {
 							final LogFolderInfo newLogDir = new LogFolderInfo(newLogName);
 							if (logFolderList.containsFolder(newLogDir)) {
 								existenteLogFoldersFromServer.add(logFolderList.getFolder((newLogDir.getName())));
 							}
 							else {
 								newLogFoldersFromServer.add(newLogDir);
 								SwingUtilities.invokeAndWait(new Runnable() {
                                     @Override
                                     public void run() {
                                         logFolderList.addFolder(newLogDir);
                                     }
                                 });
 							}
 						}
 						//msgPanel.writeMessageTextln("Logs Folders: " + logFolderList.myModel.size());
 
 						
 						//->Getting Log files list from server
 						listHandlingProgressBar.setValue(30);
 						listHandlingProgressBar.setIndeterminate(true);
 						listHandlingProgressBar.setString(I18n.text("Contacting remote system for complete log file list..."));
 //						long timeD2 = System.currentTimeMillis();
 						String listXhtml = getLogsFileListFromServer();
 						//System.out.println(".......get list.xml from server " + (System.currentTimeMillis()-timeD2));
 						listHandlingProgressBar.setValue(40);
 						listHandlingProgressBar.setIndeterminate(false);
 						listHandlingProgressBar.setString(I18n.text("Processing log list..."));
 						if (listXhtml == null)
 							return null;
 						//msgPanel.writeMessageTextln("listXhtml: " + (listXhtml.charAt(2)));
 						Document docList = loadXhtmlToDocument(listXhtml);
 						//msgPanel.writeMessageTextln("docList: " + (docList==null));
 						objArray = new Object[logFolderList.myModel.size()];
 						logFolderList.myModel.copyInto(objArray);
 						//msgPanel.writeMessageTextln("objArray: " + objArray.length);
 						LinkedList<LogFolderInfo> tmpLogFolderList = getLogListAsTemporaryStructureFromDOM(docList, retList);
 						listHandlingProgressBar.setValue(70);
 						listHandlingProgressBar.setIndeterminate(false);
 						listHandlingProgressBar.setString(I18n.text("Updating logs info..."));
 						//Testing for log files from each log folder 
 						//long timeF1 = System.currentTimeMillis();
 						for (Object comp : objArray) {
 							try {
 								LogFolderInfo logFolder = (LogFolderInfo) comp;
 								
 								//LinkedHashMap<String, String> res = filterHrefLogFilesList(docList, logFolder.getName());
 								int indexLFolder = tmpLogFolderList.indexOf(logFolder);
 								LinkedHashSet<LogFileInfo> logFilesTmp = (indexLFolder != -1) ? tmpLogFolderList
 										.get(indexLFolder).logFiles
 										: new LinkedHashSet<LogFileInfo>();
 								//for (String fxStr : res.keySet()) {
 								for (LogFileInfo logFx : logFilesTmp) {
 									//LogFileInfo logFx = new LogFileInfo(fxStr);
 									//long size = filterHrefLogFileSize(docList, logFx.getName());
 									if (!logFolder.getLogFiles().contains(logFx)) {
 										//logFx.setUriPartial(res.get(fxStr));
 										//logFx.setSize(size);
 										logFolder.addFile(logFx);
 //										long sizeD = getDiskSizeFromLocal(logFx);
 //										System.out.println("Size: " + size + "  "+sizeD);
 //										if (size == sizeD) {
 //											logFx.setState(LogFolderInfo.State.SYNC);
 //										}
 //										else {
 //											logFx.setState(LogFolderInfo.State.INCOMPLETE);
 //										}
 									}
 									else {
 										LogFileInfo lfx = logFolder.getLogFile(logFx.getName()/*fxStr*/);
 										if (lfx.getSize() == -1) {
 											lfx.setSize(logFx.getSize()/*size*/);
 										}
 										else if (lfx.getSize() != logFx.getSize()/*size*/) {
 											if (lfx.getState() == LogFolderInfo.State.SYNC)
 												lfx.setState(LogFolderInfo.State.INCOMPLETE);
 											else if (lfx.getState() == LogFolderInfo.State.LOCAL)
 												lfx.setState(LogFolderInfo.State.INCOMPLETE);
 											lfx.setSize(logFx.getSize()/*size*/);
 										}										
 										else if (lfx.getSize() == logFx.getSize()/*size*/) {
 											if (lfx.getState() == LogFolderInfo.State.LOCAL)
 												lfx.setState(LogFolderInfo.State.SYNC);
 										}
 										if (!getFileTarget(lfx.getName()).exists()) {
 											if (lfx.getState() != LogFolderInfo.State.NEW)
 												lfx.setState(LogFolderInfo.State.INCOMPLETE);
 										}
 									}
 								}
 								
 								//Put LOCAL state on files not in server
 								LinkedHashSet<LogFileInfo> toDelFL = new LinkedHashSet<LogFileInfo>();
 								for (LogFileInfo lfx : logFolder.getLogFiles()) {
 									if (!logFilesTmp.contains(lfx)
 											/*!res.keySet().contains(lfx.getName())*/) {
 										lfx.setState(LogFolderInfo.State.LOCAL);
 										if (!getFileTarget(lfx.getName()).exists()) {
 											toDelFL.add(lfx);
 											//logFolder.getLogFiles().remove(lfx); //This cannot be done here
 										}
 									}
 								}
 								for (LogFileInfo lfx : toDelFL)
 									logFolder.getLogFiles().remove(lfx);
 							}
 							catch (Exception e) {
 							}
 						}
 						//System.out.println(".......Testing for log files from each log folder " + (System.currentTimeMillis()-timeF1));
 
 						//long timeF2 = System.currentTimeMillis();
 						testNewReportedLogFoldersForLocalCorrespondent(newLogFoldersFromServer);
 						for (LogFolderInfo logFolder : existenteLogFoldersFromServer) {
 							updateLogFolderState(logFolder);
 						}
 						//System.out.println(".......Updating LogFolders State " + (System.currentTimeMillis()-timeF2));
 						
 						//long timeF3 = System.currentTimeMillis();
 						//updateFilesListGUIForFolderSelected();
 						new Thread() {
 						    public void run() {
 						        updateFilesListGUIForFolderSelected();
 						    };
 						}.start();
 						//System.out.println(".......updateFilesListGUIForFolderSelected " + (System.currentTimeMillis()-timeF3));
 						
 						listHandlingProgressBar.setValue(90);
 						listHandlingProgressBar.setIndeterminate(false);
 						listHandlingProgressBar.setString(I18n.text("Updating GUI..."));
 						logFolderList.invalidate();
 						logFolderList.revalidate();
 						logFolderList.repaint();
 						logFolderList.setEnabled(true);
 						//logFilesList.invalidate();
 						//logFilesList.revalidate();
 						//logFilesList.repaint();
 						logFilesList.setEnabled(true);
 						
 						//System.out.println(".......downloadListAction " + (System.currentTimeMillis()-time));
 						listHandlingProgressBar.setValue(100);
 						listHandlingProgressBar.setIndeterminate(false);
 						listHandlingProgressBar.setString(I18n.text("Done"));
 						return true;
 					}
 
 					@Override
 					public void finish() {
 						logFolderList.setValueIsAdjusting(false);
 						logFolderList.invalidate();
 						logFolderList.revalidate();
 						logFolderList.repaint();
 						logFolderList.setEnabled(true);
 						//logFilesList.invalidate();
 						//logFilesList.revalidate();
 						//logFilesList.repaint();
 						listHandlingProgressBar.setValue(0);
 						listHandlingProgressBar.setIndeterminate(false);
 						listHandlingProgressBar.setString("");
 						logFilesList.setEnabled(true);
 						downloadListButton.setEnabled(true);
 						try {
 							this.getResultOrThrow();
 						}
 						catch (Exception e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				};
 
 				AsyncWorker.getWorkerThread().postTask(task);
 			}
 		};
 		
 		downloadSelectedLogDirsAction = new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (!validateAndSetUI()) {
 					popupErrorConfigurationDialog();
 					return;
 				}
 				AsyncTask task = new AsyncTask() {
 					@Override
 					public Object run() throws Exception {
 						downloadSelectedLogDirsButton.setEnabled(false);
 						for (Object comp : logFolderList.getSelectedValues()) {
 							try {
 								//System.out.println("... updateFilesForFolderSelected");
 								LogFolderInfo logFd = (LogFolderInfo) comp;
 								for (LogFileInfo lfx : logFd.logFiles) {
 									singleLogFileDownloadWorker(lfx, logFd);
 								}
 							}
 							catch (Exception e) {
 							}
 						}
 						return true;
 					}
 
 					@Override
 					public void finish() {
 						downloadSelectedLogDirsButton.setEnabled(true);
 						try {
 							this.getResultOrThrow();
 						} catch (Exception e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				};
 				AsyncWorker.getWorkerThread().postTask(task);
 			}
 		};
 		
 		downloadSelectedLogFilesAction = new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (!validateAndSetUI()) {
 					popupErrorConfigurationDialog();
 					return;
 				}
 				AsyncTask task = new AsyncTask() {
 					@Override
 					public Object run() throws Exception {
 						downloadSelectedLogFilesButton.setEnabled(false);
 
 						for (Object comp :logFilesList.getSelectedValues()) {
 							try {
 								//System.out.println("... updateFilesForFolderSelected");
 								//FIXME Find out LogFolderInfo for LogFileInfo
 								LogFileInfo lfx = (LogFileInfo) comp;
 								singleLogFileDownloadWorker(lfx, findLogFolderInfoForFile(lfx));
 							}
 							catch (Exception e) {
 							}
 						}
 						return true;
 					}
 
 					@Override
 					public void finish() {
 						downloadSelectedLogFilesButton.setEnabled(true);
 						try {
 							this.getResultOrThrow();
 						} catch (Exception e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				};
 
 				AsyncWorker.getWorkerThread().postTask(task);
 			}
 		};
 		
 		deleteSelectedLogFoldersAction = new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (!validateAndSetUI()) {
 					popupErrorConfigurationDialog();
 					return;
 				}
 				AsyncTask task = new AsyncTask() {
 					@Override
 					public Object run() throws Exception {
 						deleteSelectedLogFoldersButton.setEnabled(false);
 						//logFolderList.setEnabled(false);
 						//logFilesList.setEnabled(false);
 
 						Object[] objArray = logFolderList.getSelectedValues();
 						if (objArray.length == 0)
 							return null;
 
 						JOptionPane jop = new JOptionPane(
 								I18n.text("Are you sure you want to delete "
 										+ "selected log folders from remote system?"),
 								JOptionPane.QUESTION_MESSAGE,
 								JOptionPane.YES_NO_OPTION);
 						JDialog dialog = jop.createDialog(frameCompHolder, I18n.text("Remote Delete Confirmation"));
 						dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
 						dialog.setVisible(true);
 						Object userChoice = jop.getValue();
 						//System.out.println("user option: "+userChoice);
 						try {
 							if (((Integer)userChoice) != JOptionPane.YES_OPTION) {
 								return null;
 							}
 						}
 						catch (Exception e2) {
 							//e2.printStackTrace();
 							return null;
 						}
 						//System.out.println("user option: "+userChoice);
 						deleteSelectedLogFoldersButton.setEnabled(true);
 						for (Object comp : objArray) {
 							try {
 								LogFolderInfo logFd = (LogFolderInfo) comp;
 								boolean resDel = deleteLogFolderFromServer(logFd);
 								if (resDel) {
 									logFd.setState(LogFolderInfo.State.LOCAL);
 									LinkedHashSet<LogFileInfo> logFiles = logFd.logFiles;
 
 									LinkedHashSet<LogFileInfo> toDelFL = updateLogFilesStateDeleted(logFiles);
 									for (LogFileInfo lfx : toDelFL)
 										logFd.getLogFiles().remove(lfx);
 								}
 							}
 							catch (Exception e) {
 							}
 						}
 						updateFilesListGUIForFolderSelected();
 						return true;
 					}
 
 
 					@Override
 					public void finish() {
 						deleteSelectedLogFoldersButton.setEnabled(true);
 						logFilesList.revalidate();
 						logFilesList.repaint();
 						logFilesList.setEnabled(true);
 						logFolderList.revalidate();
 						logFolderList.repaint();
 						logFolderList.setEnabled(true);
 						try {
 							this.getResultOrThrow();
 						} catch (Exception e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				};
 
 				AsyncWorker.getWorkerThread().postTask(task);
 			}
 		};
 		
 		deleteSelectedLogFilesAction = new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 			    if (!validateAndSetUI()) {
                     popupErrorConfigurationDialog();
                     return;
                 }
                 AsyncTask task = new AsyncTask() {
                     @Override
                     public Object run() throws Exception {
                         deleteSelectedLogFilesButton.setEnabled(false);
 			    
                         Object[] objArray = logFilesList.getSelectedValues();
                         if (objArray.length == 0)
                             return null;
 
                         JOptionPane jop = new JOptionPane(
                                 I18n.text("Are you sure you want to delete selected log files from remote system?"),
                                 JOptionPane.QUESTION_MESSAGE,
                                 JOptionPane.YES_NO_OPTION);
                         JDialog dialog = jop.createDialog(frameCompHolder, I18n.text("Remote Delete Confirmation"));
                         dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
                         dialog.setVisible(true);
                         Object userChoice = jop.getValue();
                         //System.out.println("user option: "+userChoice);
                         try {
                             if (((Integer)userChoice) != JOptionPane.YES_OPTION) {
                                 return null;
                             }
                         }
                         catch (Exception e2) {
                             //e2.printStackTrace();
                             return null;
                         }
                         deleteSelectedLogFoldersButton.setEnabled(true);
                         
                         LinkedHashSet<LogFileInfo> logFiles = new LinkedHashSet<LogFileInfo>();
                         for (Object comp : objArray) {
                             try {
                                 LogFileInfo lfx = (LogFileInfo) comp;
                                 if (deleteLogFileFromServer(lfx))
                                     logFiles.add(lfx);
                             }
                             catch (Exception e) {
                             }
                         }
                         updateLogFilesStateDeleted(logFiles);
                         
                         updateFilesListGUIForFolderSelected();
                         return true;
                     }
 
                     @Override
                     public void finish() {
                         deleteSelectedLogFilesButton.setEnabled(true);
                         logFilesList.revalidate();
                         logFilesList.repaint();
                         logFilesList.setEnabled(true);
                         logFolderList.revalidate();
                         logFolderList.repaint();
                         logFolderList.setEnabled(true);
                         try {
                             this.getResultOrThrow();
                         } catch (Exception e) {
                             // TODO Auto-generated catch block
                             e.printStackTrace();
                         }
                     }
                 };
                 
                 AsyncWorker.getWorkerThread().postTask(task);
 			}
 		};
 		
 		toggleConfPanelAction = new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				
 				configCollapsiblePanel.getActionMap().get(
 						JXCollapsiblePane.TOGGLE_ACTION).actionPerformed(e);
 			}
 		};
 		
 		toggleExtraInfoPanelAction = new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				extraInfoCollapsiblePanel.getActionMap().get(
 						JXCollapsiblePane.TOGGLE_ACTION).actionPerformed(e);
 			}
 		};
 		
 		helpAction = new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				GuiUtils.centerOnScreen(downHelpDialog.getDialog());
 				downHelpDialog.getDialog().setIconImage(ICON_HELP.getImage());
 				downHelpDialog.getDialog().setVisible(true);
 			}
 		};
 		
 		resetAction = new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				resetButton.setEnabled(false);
 				doReset(false);
 				resetButton.setEnabled(true);
 			}
 		};
 
 		stopAllAction = new AbstractAction() {
 		    @Override
 		    public void actionPerformed(ActionEvent e) {
 		        stopAllButton.setEnabled(false);
 		        doReset(true);
 		        stopAllButton.setEnabled(true);
 		    }
 		};
 
 	}
 
 	/**
      * @return the content panel. Use it only if you give an external frame in the
      * constructor. 
      */
     public JXPanel getContentPanel() {
         return frameCompHolder;
     }
 	
 	/**
 	 * @return
 	 */
 	private TimerTask getTimerTaskLocalDiskSpace() {
 		if (ttaskLocalDiskSpace == null) {
 			ttaskLocalDiskSpace = new TimerTask() {
 				@Override
 				public void run() {
 					//System.out.println("Usable space: " + MathMiscUtils.parseToEngineeringRadix2Notation(new File(logFetcher.dirBaseToStoreFiles).getUsableSpace(),2)+"B");
 					try {
 						File fxD = new File(dirBaseToStoreFiles);
 						long tspace = fxD.getTotalSpace();
 						long uspace = fxD.getUsableSpace();
 						if (tspace != 0 /*&& uspace != 0*/) {
 							String tSpStr = MathMiscUtils.parseToEngineeringRadix2Notation(tspace, 2) + "B";
 							String uSpStr = MathMiscUtils.parseToEngineeringRadix2Notation(uspace, 2) + "B";
 							double pFree = 1.0*(tspace - uspace) / tspace;
 							//System.out.println("Free Space: "+(tspace - uspace)+" "+((tspace - uspace)*1.0 / tspace)+" "+pFree);
 							diskFreeLabel.setText("<html><b>"+uSpStr );
                             diskFreeLabel.setToolTipText(I18n.textf("Local free disk space %usedspace of %totalspace", uSpStr, tSpStr));
 							updateDiskFreeLabelBackColor(diskFreeColorMap.getColor(pFree));
 							return;
 						}
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					diskFreeLabel.setText("<html><b>?");
 					diskFreeLabel.setToolTipText(I18n.text("Unknown local disk free space"));
 					updateDiskFreeLabelBackColor(Color.LIGHT_GRAY);
 				}
 			};
 		}
 		return ttaskLocalDiskSpace;
 	}
 
 	/**
 	 * This is used to clean and dispose safely of this component
 	 */
 	public void cleanup() {
 		if (client != null) {
 		}
         if (client4List != null) {
         }
 		if (httpConnectionManager != null) {
 			httpConnectionManager.shutdown();
 		}
         if (httpConnectionManager4List != null) {
             httpConnectionManager4List.shutdown();
         }
 		if (ttaskLocalDiskSpace != null) {
 			ttaskLocalDiskSpace.cancel();
 			ttaskLocalDiskSpace = null;
 		}
 		if (timer != null) {
 			timer.cancel();
 			timer = null;
 		}
 		if (frame != null) {
 		    if (!frameIsExternalControlled) {
 	            SwingUtilities.invokeLater(new Runnable() {                
 	                @Override
 	                public void run() {
 	                    frame.dispose();
 	                    frame = null;
 	                }
 	            }); 
 		    }
 		    else
 		        frame = null;
 		}
 		if (downHelpDialog != null)
 		    downHelpDialog.dispose();
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#finalize()
 	 */
 	@Override
 	protected void finalize() throws Throwable {
 		super.finalize();
 		cleanup();
 	}
 	
 	/**
 	 * @return the rectPainter
 	 */
 	private RectanglePainter getRectPainter() {
 		if (rectPainter == null) {
 	        rectPainter = new RectanglePainter(0,0,0,0, 10,10);
 	        rectPainter.setFillPaint(Color.LIGHT_GRAY);
 	        rectPainter.setBorderPaint(Color.LIGHT_GRAY.darker().darker().darker());
 	        rectPainter.setStyle(RectanglePainter.Style.BOTH);
 	        rectPainter.setBorderWidth(2);
 	        rectPainter.setAntialiasing(true);//RectanglePainter.Antialiasing.On);
 		}
 		return rectPainter;
 	}
 	
 	/**
 	 * @return the compoundBackPainter
 	 */
 	private CompoundPainter<JXPanel> getCompoundBackPainter() {
 		compoundBackPainter = new CompoundPainter<JXPanel>(
 					//new MattePainter(Color.BLACK),
 					getRectPainter(), new GlossPainter());
 		return compoundBackPainter;
 	}
 	
 	/**
 	 * @param color
 	 */
 	private void updateDiskFreeLabelBackColor(Color color) {
 		getRectPainter().setFillPaint(color);
 		getRectPainter().setBorderPaint(color.darker());
 
 		diskFreeLabel.setBackgroundPainter(getCompoundBackPainter());
 	}
 
 
 	/**
 	 * 
 	 */
 	private void popupErrorConfigurationDialog() {
 		// JOptionPane.showMessageDialog(
 		// frame,
 		// "Some of the configuration parameters are not correct!",
 		// "Error on configuration",
 		// JOptionPane.ERROR_MESSAGE);
 		JOptionPane jop = new JOptionPane(I18n.text("Some of the configuration parameters are not correct!"), JOptionPane.ERROR_MESSAGE);
 		JDialog dialog = jop.createDialog(frameCompHolder, I18n.text("Error on configuration"));
 		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
 		dialog.setVisible(true);
 	}
 
 
 	/**
 	 * @return
 	 */
 	public boolean validateConfiguration() {
 		if ("".equalsIgnoreCase(hostField.getText())) {
 			return false;
 		}
 		if ("".equalsIgnoreCase(portField.getText())) {
 			return false;
 		}
 		else {
 			try {
 				Integer.parseInt(portField.getText());
 			} catch (NumberFormatException e) {
 				return false;
 			}
 		}
 		if ("".equalsIgnoreCase(logLabelField.getText()))
 				return false;
 		return true;
 	}
 
 	/**
 	 * @return
 	 */
 	private boolean validateAndSetUI() {
 		int iPort = 8080;
 		if ("".equalsIgnoreCase(hostField.getText())) {
 			return false;
 		}
 		if ("".equalsIgnoreCase(portField.getText())) {
 			return false;
 		}
 		else {
 			try {
 				iPort = Integer.parseInt(portField.getText());
 			} catch (NumberFormatException e) {
 				return false;
 			}
 		}
 		if ("".equalsIgnoreCase(logLabelField.getText()))
 				return false;
 		
 		String bs = baseUriField.getText().replace("\\", "/");
 		if (!bs.startsWith("/"))
 			bs = "/"+bs;
 		if (!bs.endsWith("/"))
 			bs = bs + "/";
 		bs.replace("\\", "").replaceAll("/", "").replaceAll("\\s", "");
 		
 		host = hostField.getText();
 		port = iPort;
 		basePath = bs;
 		logLabel = logLabelField.getText();
 		if ("".equalsIgnoreCase(logLabel))
 		    logLabel = I18n.text("unknown");
 		return true;
 	}
 
 	public String getHost() {
 		return host;
 	}
 	
 	/**
 	 * @param host
 	 */
 	public void setHost(String host) {
 		this.host = host;
 		hostField.setText(host);
 	}
 	
 	/**
 	 * @return
 	 */
 	public int getPort() {
 		return port;
 	}
 	
 	/**
 	 * @param port
 	 */
 	public void setPort(int port) {
 		this.port = port;
 		portField.setText(""+port);
 	}
 	
 	/**
 	 * @return
 	 */
 	public String getBasePath() {
 		return basePath;
 	}
 	
 	/**
 	 * @param basePath
 	 */
 	public void setBasePath(String basePath) {
 		this.basePath = basePath;
 		baseUriField.setText(basePath);
 	}
 	
 	public String getLogLabel() {
 		return logLabel;
 	}
 	
 	/**
 	 * @param logLabel
 	 */
 	public void setLogLabel(String logLabel) {
 		this.logLabel = logLabel;
 		logLabelField.setText(logLabel);
 		if (!frameIsExternalControlled)
 		    frame.setTitle(DEFAULT_TITLE + " - " + logLabel);
 	}
 	
 	/**
      * @return the connectionsPerRouteAllowed
      */
     public int getMaxConnectionsAllowed() {
         return connectionsPerRouteAllowed;
     }
     
     /**
      * @param connectionsPerRouteAllowed the connectionsPerRouteAllowed to set
      */
     public void setMaxConnectionsAllowed(int connections) {
         this.connectionsPerRouteAllowed = connections;
         if (httpConnectionManager != null) {
             httpConnectionManager.setDefaultMaxPerRoute(connectionsPerRouteAllowed);
         }
     }
 
 	/**
 	 * @param show
 	 */
 	public void setVisible(boolean show) {
 		frame.setVisible(show);
 		if (show)
 		    frame.setState(Frame.NORMAL);
 	}
 		
 	/**
 	 * @param lfx
 	 * @return
 	 */
 	private LogFolderInfo findLogFolderInfoForFile(LogFileInfo lfx) {
 		for (Object comp : logFolderList.getSelectedValues()) {
 			try {
 				LogFolderInfo logFd = (LogFolderInfo) comp;
 				if (logFd.logFiles.contains(lfx))
 					return logFd;
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
     
 	
 	private boolean isUpdatingFileList = false;
 	private boolean exitRequest = false;
 	private final Object lock = new Object();
 	/**
 	 * 
 	 */
 	private void updateFilesListGUIForFolderSelected() {
 	    if (isUpdatingFileList)
 	        exitRequest = true;
 	    //System.err.println(".................. " +isUpdatingFileList+" "+exitRequest);
 	    synchronized (lock) {
 	        isUpdatingFileList = true;
 	        exitRequest = false;
 
 //	        long start = System.currentTimeMillis();
 	        //System.out.println("updateFilesForFolderSelected");
 //	        logFilesList.setEnabled(false);
 	        logFilesList.setValueIsAdjusting(true);
 
 	        final LinkedHashSet<LogFileInfo> validFiles = new LinkedHashSet<LogFileInfo>();
 	        for (Object comp : logFolderList.getSelectedValues()) {
 	            try {
 	                //System.out.println("... updateFilesForFolderSelected");
 	                LogFolderInfo log = (LogFolderInfo) comp;
 	                // System.out.println("LogFolder Sel: " + log.getName());
 	                for (LogFileInfo lgfl : log.logFiles) {
 	                    validFiles.add(lgfl);
 
 	                    if (exitRequest)
 	                        break;
 	                }
 	            }
 	            catch (Exception e) {
 	            }
 
 	            if (exitRequest)
                     break;
 	        }
 
 	        logFilesList.setIgnoreRepaint(true);
 	        try {
 	            if (SwingUtilities.isEventDispatchThread()) {
 	                if (exitRequest)
                         return;
                     logFilesList.myModel.clear();
 	            }
 	            else {
 	                SwingUtilities.invokeAndWait(new Runnable() {
 	                    @Override
 	                    public void run() {
 	                        if (exitRequest)
 	                            return;
 	                        logFilesList.myModel.clear();
 	                    }
 	                });
 	            }
 	            for (final LogFileInfo fxS : validFiles) {
 	                if (SwingUtilities.isEventDispatchThread()) {
 	                    if (exitRequest)
                             return;
                         logFilesList.addFile(fxS);
 	                }
 	                else {
 	                    SwingUtilities.invokeAndWait(new Runnable() {
 	                        @Override
 	                        public void run() {
 	                            if (exitRequest)
 	                                return;
 	                            logFilesList.addFile(fxS);
 	                        }
 	                    });
 	                }
                     if (exitRequest)
                         break;
 	            }
 	        }
 	        catch (Exception e) {
 	            //e.printStackTrace();
 	        }
 	        logFilesList.setIgnoreRepaint(false);
 	        
 	        logFilesList.setValueIsAdjusting(false);
 	        logFilesList.invalidate();
 	        logFilesList.validate();
 //	        logFilesList.setEnabled(true);
 	        //System.out.println("end  updateFilesForFolderSelected");
 //	        System.out.println("end  updateFilesForFolderSelected " + System.currentTimeMillis() +
 //	                "        " + (System.currentTimeMillis() - start) + "ms " + logFilesList.myModel.size() +
 //	                " fxs");
 	        isUpdatingFileList = false;
 	    }
 	}
 	
 	
 	/**
 	 * @param newLogFoldersFromServer
 	 */
 	private void testNewReportedLogFoldersForLocalCorrespondent(
 			LinkedList<LogFolderInfo> newLogFoldersFromServer) {
 		for (LogFolderInfo lf : newLogFoldersFromServer) {
 			File testFile = new File(getDirTarget(), lf.getName());
 			if (testFile.exists()) {
 				lf.setState(LogFolderInfo.State.UNKNOWN);
 				for (LogFileInfo lfx : lf.logFiles) {
 					File testFx = new File(getDirTarget(), lfx.getName());
 					if (testFx.exists()) {
 						lfx.setState(LogFolderInfo.State.UNKNOWN);
 						long sizeD = getDiskSizeFromLocal(lfx);
 						//System.out.println("Size: " + lfx.getSize() + "  "+sizeD);
 						if (lfx.getSize() == sizeD) {
 							lfx.setState(LogFolderInfo.State.SYNC);
 						}
 						else {
 							lfx.setState(LogFolderInfo.State.INCOMPLETE);
 						}
 					}
 				}
 				updateLogFolderState(lf);
 			}
 			else {
 				lf.setState(LogFolderInfo.State.NEW);
 			}
 		}
 	}
 
 	/**
 	 * @param host
 	 * @param port
 	 * @param path
 	 * @return
 	 */
 	private String createURI(String host, int port, String path) {
 	    String pathEscaped;
         try {
             pathEscaped = URLEncoder.encode(path, "UTF-8").replaceAll("%2F", "/");
             //not the best way but handles better the URL with "to escape" chars
         }
         catch (UnsupportedEncodingException e) {
             e.printStackTrace();
             pathEscaped = path;
         }
 	    //System.out.println(pathEscaped);
 		return "http://" + host + (port != 80 ? ":" + port : "")
 				+ (pathEscaped.startsWith("/") ? pathEscaped : "/" + pathEscaped);
 	}
 
 	/**
 	 * @param partial
 	 * @return
 	 */
 	private String getFullPartialPath(String partial) {
 		return basePath + partial;
 	}
 	
 
 	/**
 	 * @param host
 	 * @param port
 	 * @param path
 	 * @return A size 2 String array. the position '0' with the content returned or empty
 	 *            and the position '1' with the return  HttpStatus code (-1 for internal)
 	 */
 	private String[] getPageAsString(String host, int port, String path) {
 		String uri = createURI(host, port, path);
 		HttpGet get = new HttpGet(uri);
 //		get.setFollowRedirects(true);
 		
 		synchronized (listActiveGetMethods) {
 			listActiveGetMethods.add(get);
 		}
 
 		try {
 //			int iGetResultCode = client.executeMethod(get);
 			HttpResponse iGetResultCode = client4List.execute(get);
 			if (iGetResultCode.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
 				return new String[] {"", ""+iGetResultCode};
 //			String strGetResponseBody = get.getResponseBodyAsString();
 			String strGetResponseBody = EntityUtils.toString(iGetResultCode.getEntity());
 			return new String[] {strGetResponseBody, ""+iGetResultCode.getStatusLine().getStatusCode()};
 		} catch (Exception ex) {
 			//ex.printStackTrace();
 			NeptusLog.pub().info(LogsDownloaderWorker.class.getName()
 					+ ": getPageAsString;" + ex.getMessage());
 			return new String[] {"", ""+-1};
 		} finally {
 //			get.releaseConnection();
 			get.abort();
 			synchronized (listActiveGetMethods) {
 				listActiveGetMethods.remove(get);
 			}
 		}
 	}
 
 
 	/**
 	 * @param fx
 	 * @return Negative values for errors (HTTP like returns).
 	 */
 	private long getDiskSizeFromLocal(LogFileInfo fx) {
 		File fileTarget = getFileTarget(fx.getName());
 		if (fileTarget == null)
 			return -1;
 		else if (fileTarget.exists()) {
 			if (fileTarget.isFile()) {
 				return fileTarget.length();
 			}
 			else
 				return -500;
 		}
 		else if (!fileTarget.exists()) {
 			return -400;
 		}
 		return -500;
 	}
 	
 	/**
 	 * @return
 	 */
 	private LinkedHashSet<String> getLogsListFromServer() {
 		final LinkedHashSet<String> tmpLogs = new  LinkedHashSet<String>();
     	String[] retStr = (!dummyLists)?getPageAsString(host, port, basePath+"list"):null;
     	if (dummyLists) {
     		retStr = new String[] {FileUtil.getFileAsString(dummyListFx), "200"};
     	}
     	if ("-1".equalsIgnoreCase(retStr[1])) {
 			warnMsg(I18n.text("Error connecting with the remote system!"));
     		return null;
     	}
     	else if (!"200".equalsIgnoreCase(retStr[1])) {
     		int codeR;
 			try {
 				codeR = Integer.parseInt(retStr[1]);
 			} catch (NumberFormatException e) {
 				codeR = -1;
 			}
 			warnMsg(I18n.textf("Remote system return %httpdcode code!",
 					(codeR==-1?retStr[1]:codeR/*HttpStatus.getStatusText(codeR)*/)));
 			return null;
     	}
     	String res = retStr[0].toLowerCase();
         if (res.contains("<html>") || res.contains("<xml>")) {
             warnMsg(I18n.text("Remote system return unexpected log list format!"));
             return null;
         }
         res = retStr[0];
     	for(String r1 : res.split("[\\r\\n]"   /*"\\s"*/  /*"[\\r\\n\\t\\f ]"*/)) {
     		if (!"".equalsIgnoreCase(r1))
     			tmpLogs.add(r1);
     	}
     	if (tmpLogs.size() > 0) {
 	    	//Inverse sort
 	    	String[] sort = new String[tmpLogs.size()];
 	    	tmpLogs.toArray(sort);
 	    	Arrays.sort(sort);
 	    	tmpLogs.clear();
 	    	//for (int i = sort.length - 1; i > 0; i--) {
 	    	//	tmpLogs.add(sort[i]);
 	    	//}
 	    	for (int i = 0; i < sort.length; i++) {
 	    		tmpLogs.add(sort[i]);
 	    	}
     	}
     	return tmpLogs;
 	}
 	
 	/**
 	 * @return
 	 */
 	private String getLogsFileListFromServer() {
 		//String xhtmlLogFilesList = getPageAsString(host, port, basePath+"list.html");
     	String[] retStr = (!dummyLists)?getPageAsString(host, port, basePath+"list.xml"):null; 
     	if (dummyLists) {
     		retStr = new String[] {FileUtil.getFileAsString(dummyListXmlFx), "200"};
     	}
 
     	if ("-1".equalsIgnoreCase(retStr[1])) {
 			warnMsg(I18n.text("Error connecting with the remote system!"));
     		return null;
     	}
     	else if (!"200".equalsIgnoreCase(retStr[1])) {
     		int codeR;
 			try {
 				codeR = Integer.parseInt(retStr[1]);
 			} catch (NumberFormatException e) {
 				codeR = -1;
 			}
 			warnMsg(I18n.textf("Remote system return %httpdcode code!",
                     (codeR==-1?retStr[1]:codeR/*HttpStatus.getStatusText(codeR)*/)));
 			return null;
     	}
 		String xhtmlLogFilesList = retStr[0];
 		return xhtmlLogFilesList;
 	}
 
 	
 	/**
 	 * @param lfx
 	 * @param logFd
 	 */
 	private void singleLogFileDownloadWorker(LogFileInfo lfx, LogFolderInfo logFd) {
 		if (lfx.getState() == LogFolderInfo.State.SYNC
 				|| lfx.getState() == LogFolderInfo.State.DOWNLOADING
 				|| lfx.getState() == LogFolderInfo.State.LOCAL) {
 			return;
 		}
 		DownloaderPanel workerD = new DownloaderPanel(client, 
 				lfx.getName(), 
 				createURI(host, port, getFullPartialPath(lfx.getUriPartial())),
 				getFileTarget(lfx.getName()));
 		
 		Component[] components = downloadWorkersHolder.getComponents();
 		for (Component cp : components) {
 			try {
 				DownloaderPanel dpp = (DownloaderPanel) cp;
 				//System.out.println("........... "+dpp.getName());
 				if (workerD.getName().equals(dpp.getName())) {
 					//System.out.println("...........");
 					workerD = dpp;
 					if (workerD.getState() == DownloaderPanel.State.ERROR ||
 							workerD.getState() == DownloaderPanel.State.IDLE ||
 							workerD.getState() == DownloaderPanel.State.TIMEOUT ||
                             workerD.getState() == DownloaderPanel.State.NOT_DONE) {
 						workerD.actionDownload();
 					}
 					break;
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		final LogFolderInfo lfdfinal = logFd;
 		final LogFileInfo lfxfinal = lfx;
 		final DownloaderPanel workerDFinal = workerD;
 		workerD.addStateChangeListener(new DownloadStateListener() {
 			LogFileInfo fxLog = lfxfinal;
 			@Override
 			public void downloaderStateChange(DownloaderPanel.State newState, DownloaderPanel.State oldState) {
 				if (fxLog.getState() != LogFolderInfo.State.LOCAL) {
 					if (newState == DownloaderPanel.State.DONE)
 						fxLog.setState(LogFolderInfo.State.SYNC);
 					else if (newState == DownloaderPanel.State.ERROR)
 						fxLog.setState(LogFolderInfo.State.ERROR);
 					else if (newState == DownloaderPanel.State.WORKING)
 						fxLog.setState(LogFolderInfo.State.DOWNLOADING);
 					else if (newState == DownloaderPanel.State.NOT_DONE)
 						fxLog.setState(LogFolderInfo.State.INCOMPLETE);
 					else if (newState == DownloaderPanel.State.IDLE)
 						;//fxLog.setState(LogFolderInfo.State.ERROR);
 					
 					if (logFilesList.containsFile(fxLog)) {
 						logFilesList.revalidate();
 						logFilesList.repaint();
 					}
 					
 					updateLogFolderState(lfdfinal);
 				}
 
 				
 				if (newState == DownloaderPanel.State.DONE) {
 					TimerTask task = new TimerTask() {
                         @Override
                         public void run() {
                             try {
                                 if (workerDFinal.getState() == DownloaderPanel.State.DONE) {
                                     downloadWorkersHolder.remove(workerDFinal);
                                     downloadWorkersHolder.revalidate();
                                     downloadWorkersHolder.repaint();
                                 }
                             }
                             catch (Exception e) {
                                 e.printStackTrace();
                             }
                         }
                     };
 					timer.schedule(task, DELTA_TIME_TO_CLEAR_DONE);
 				}
 				else if (newState != DownloaderPanel.State.TIMEOUT) { //FIXME VERIFICAR SE OK OU TIRAR
 					TimerTask task = new TimerTask() {
 					    @Override
 					    public void run() {
 					        try {
 	                            if (workerDFinal.getState() != DownloaderPanel.State.WORKING) {
 	                                downloadWorkersHolder.remove(workerDFinal);
 	                                downloadWorkersHolder.revalidate();
 	                                downloadWorkersHolder.repaint();
 	                            }
                             }
                             catch (Exception e) {
                                 e.printStackTrace();
                             }
 					    }
 					};
 					timer.schedule(task, DELTA_TIME_TO_CLEAR_NOT_WORKING);
 				}
 			}
 		});
 		downloadWorkersHolder.add(workerD);
 		workerD.actionDownload();
 	}
 
 	
 	/**
 	 * @param logFolder
 	 */
 	private void updateLogFolderState(LogFolderInfo logFolder) {
 		LogFolderInfo.State lfdState = logFolder.getState();
 		LogFolderInfo.State lfdStateTmp = LogFolderInfo.State.UNKNOWN;
 		long nTotal = 0, nDownloading = 0, nError = 0, nNew = 0, nIncomplete = 0,
 				nLocal = 0, nSync = 0, nUnknown = 0;
 		for (LogFileInfo tlfx : logFolder.logFiles) {
 			nTotal++;
 			if (tlfx.getState() == LogFolderInfo.State.DOWNLOADING) {
 				nDownloading++;
 			}
 			else if (tlfx.getState() == LogFolderInfo.State.ERROR) {
 				nError++;
 			}
 			else if (tlfx.getState() == LogFolderInfo.State.NEW) {
 				nNew++;
 			}
 			else if (tlfx.getState() == LogFolderInfo.State.SYNC) {
 				nSync++;
 			}
 			else if (tlfx.getState() == LogFolderInfo.State.INCOMPLETE) {
 				nIncomplete++;
 			}
 			else if (tlfx.getState() == LogFolderInfo.State.UNKNOWN) {
 				nUnknown++;
 			}
 			else if (tlfx.getState() == LogFolderInfo.State.LOCAL) {
 				nLocal++;
 			}
 		}
 		
 		if (nDownloading > 0) {
 			logFolder.setState(LogFolderInfo.State.DOWNLOADING);
 		}
 		else if (nError > 0) {
 			logFolder.setState(LogFolderInfo.State.ERROR);
 		}
 		else if (nSync == nTotal) {
 			logFolder.setState(LogFolderInfo.State.SYNC);
 		}
 		else if (nNew+nLocal == nTotal) {
 			logFolder.setState(LogFolderInfo.State.NEW);
 		}
 		else if (nSync+nIncomplete+nUnknown+nNew+nLocal == nTotal) {
 			logFolder.setState(LogFolderInfo.State.INCOMPLETE);
 		}
 		else if (nLocal == nTotal) {
 			logFolder.setState(LogFolderInfo.State.LOCAL);
 		}
 		else if (nNew == nTotal) {
 			logFolder.setState(LogFolderInfo.State.NEW);
 		}
 		else {
 			logFolder.setState(LogFolderInfo.State.UNKNOWN);
 		}
 		lfdStateTmp = logFolder.getState();
 
 		
 		if (lfdState != lfdStateTmp) {
 			if (logFolderList.containsFolder(logFolder)) {
 				logFolderList.revalidate();
 				logFolderList.repaint();
 			}
 		}
 	}
 
 	/**
 	 * @param logFd
 	 * @return
 	 */
 	private boolean deleteLogFolderFromServer(LogFolderInfo logFd) {
 		String path = logFd.getName();
 		return deleteLogFolderFromServer(path);
 	}
 
 	/**
 	 * @param logFx
 	 * @return
 	 */
 	private boolean deleteLogFileFromServer(LogFileInfo logFx) {
 		String path = logFx.getName();
 		return deleteLogFolderFromServer(path);
 	}
 
 	/**
 	 * @param path
 	 * @return
 	 */
 	private boolean deleteLogFolderFromServer(String path) {
 		path = getFullPartialPath(deleteUriCommand+path);
 		String uri = createURI(host, port, path);
 		//System.out.println(uri);
 		HttpGet get = new HttpGet(uri);
 //		get.setFollowRedirects(true);
 
 		synchronized (listActiveGetMethods) {
 			listActiveGetMethods.add(get);
 		}
 
 		try {
 //			int iGetResultCode = client.executeMethod(get);
 			HttpResponse iGetResultCode = client.execute(get);
 			if (iGetResultCode.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
 				return false;
 //			String strGetResponseBody = get.getResponseBodyAsString();
 			String strGetResponseBody = EntityUtils.toString(iGetResultCode.getEntity());
 			if ("OK".equalsIgnoreCase(strGetResponseBody))
 				return true;
 			return false;
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			return false;
 		} finally {
 //			get.releaseConnection();
 			get.abort();
 			synchronized (listActiveGetMethods) {
 				listActiveGetMethods.remove(get);
 			}
 		}
 	}
 
 	/**
 	 * @param xhtmlLogFilesList
 	 * @return
 	 */
 	private Document loadXhtmlToDocument(String xhtmlLogFilesList) {
 		//long time = System.currentTimeMillis();
 		//System.out.println(".......loadXhtmlToDocument");
 		Document docList;
 		try {
 			//docList = DocumentHelper.parseText(htmlLogFilesList);
 			SAXReader xmlReader = new SAXReader();
 			xmlReader.setValidation(false);
 			xmlReader.setIncludeExternalDTDDeclarations(false);
 			xmlReader.setIncludeInternalDTDDeclarations(false);
 			xmlReader.setIgnoreComments(true);
 			try {
 				// Only works for Xerces parser, but is needed to the parser not load external DTDs (to work without Internet access)
 				xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
 			} catch (SAXException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			ByteArrayInputStream bais = new ByteArrayInputStream(xhtmlLogFilesList.getBytes());
 		    docList = xmlReader.read(bais);
 		} catch (DocumentException e) {
             NeptusLog.pub()
                     .warn(LogsDownloaderWorker.class.getName() + ": loadXhtmlToDocument;" + e.getMessage(), e);
             msgPanel.writeMessageTextln(I18n.textf("Error reading page. Error: %errormessage", e.getMessage()), MessagePanel.ERROR);
 			return null;
 		}
 		//System.out.println(".......loadXhtmlToDocument " + (System.currentTimeMillis()-time));
 		return docList;
 	}
 
 	
 	/**
 	 * @param docList
 	 * @param logsDirList
 	 * @return
 	 */
 	private LinkedList<LogFolderInfo> getLogListAsTemporaryStructureFromDOM(Document docList, LinkedHashSet<String> logsDirList) {
 		//long time = System.currentTimeMillis();
 		//System.out.println(".......getLogListAsTemporaryStructureFromDOM");
 		
 	    if (docList == null || logsDirList.size() == 0)
 	        return new LinkedList<LogFolderInfo>();
 	    
 		LinkedList<LogFolderInfo> tmpLogFolders = new LinkedList<LogFolderInfo>();
 		for (String ldir : logsDirList) {
 			LogFolderInfo lFolder = new LogFolderInfo(ldir);
 			tmpLogFolders.add(lFolder);
 		}
         LinkedList<LogFolderInfo> retLogFolders = new LinkedList<LogFolderInfo>();
 
 		List<?> logNodeList = docList.selectNodes("//log");
     	ListIterator<?> lstIt = logNodeList.listIterator();
     	msgPanel.writeMessageTextln(I18n.textf("%numberoflogs found", logNodeList.size()), MessagePanel.INFO);
 		while (lstIt.hasNext()) {
 			Element elem = (Element) lstIt.next();
 			Node nodeLink = elem.selectSingleNode("./@link");
 			Node nodeName = elem.selectSingleNode("./@name");
 			if (nodeLink != null && nodeName != null) {
 				String name = nodeName.getText();
 				String uriPartial = nodeLink.getText();
 				LogFolderInfo lfolder = null;
 				for (LogFolderInfo lf : tmpLogFolders) {
 					if (name.startsWith(lf.getName())) {
 						lfolder = lf;
 						break;
 					}
 				}
 				if (lfolder == null) {
 					continue;
 				}
 				long size = -1;
 				Node sel = elem.selectSingleNode("./@size");
 				if (sel == null)
 					size = -1;
 				else {
 					String tsize = sel.getText();
 					// \D Nondigit, [^0-9].
 					tsize = tsize.replaceAll("\\D", "");
 					try {
 						size = Long.parseLong(tsize);
 					} catch (NumberFormatException e) {
 						size = -1;
 					}
 				}
 				LogFileInfo logFileTmp = new LogFileInfo(name);
 				logFileTmp.setUriPartial(uriPartial);
 				logFileTmp.setSize(size);
 				lfolder.addFile(logFileTmp);
 				retLogFolders.add(lfolder);
 			}
 		}
 		//System.out.println(".......getLogListAsTemporaryStructureFromDOM " + (System.currentTimeMillis()-time));
 		return retLogFolders;
 	}
 
 	/**
 	 * @param name
 	 * @return
 	 */
 	private File getFileTarget(String name) {
 		File outFile = new File(getDirTarget(), name);
         // outFile.getParentFile().mkdirs(); Taking this out to not create empty folders
 		return outFile;
 	}
 	/**
 	 * @return
 	 */
 	private File getDirTarget() {
 		File dirToStore = new File(dirBaseToStoreFiles);
 		dirToStore.mkdirs();
 		File dirTarget = new File(dirToStore, logLabel);
         // dirTarget.mkdirs(); Taking this out to not create empty folders
 		return dirTarget;
 	}
 	
 	
 	//--------------------------------------------------------------
 
 	/**
 	 * 
 	 */
 	private void cleanInterface() {
 		logFilesList.myModel.clear();
 		logFolderList.myModel.clear();
 		downloadWorkersHolder.removeAll();
 	}
 
 	//--------------------------------------------------------------
 
 	/**
 	 * @param visible
 	 */
 	public void setVisibleBasePath (boolean visible) {
 		baseUriField.setVisible(visible);
 		baseUriLabel.setVisible(visible);
 	}
 
 	/**
 	 * @param visible
 	 */
 	public void setVisibleHost (boolean visible) {
 		hostField.setVisible(visible);
 		hostLabel.setVisible(visible);
 	}
 
 	/**
 	 * @param visible
 	 */
 	public void setVisiblePort (boolean visible) {
 		portField.setVisible(visible);
 		portLabel.setVisible(visible);
 	}
 
 	/**
 	 * @param visible
 	 */
 	public void setVisibleLogLabel (boolean visible) {
 		logLabelField.setVisible(visible);
 		logLabelLabel.setVisible(visible);
 	}
 	
 	/**
 	 * @param visible
 	 */
 	public void setConfigPanelVisible(boolean visible) {
 		configCollapsiblePanel.setCollapsed(!visible);
 	}
 
 	/**
 	 * @param enable
 	 */
 	public void setEnableBasePath (boolean enable) {
 		baseUriField.setEnabled(enable);
 	}
 
 	/**
 	 * @param enable
 	 */
 	public void setEnableHost (boolean enable) {
 		hostField.setEnabled(enable);
 	}
 
 	/**
 	 * @param enable
 	 */
 	public void setEnablePort (boolean enable) {
 		portField.setEnabled(enable);
 	}
 
 	/**
 	 * @param enable
 	 */
 	public void setEnableLogLabel (boolean enable) {
 		logLabelField.setEnabled(enable);
 	}
 
 	//--------------------------------------------------------------
 
     private void warnMsg(String message) {
         NudgeGlassPane.nudge(frameCompHolder.getRootPane(), (frameIsExternalControlled ? getLogLabel() + " > " : "")
                 + message, 2);
     }
 
     private void warnLongMsg(String message) {
         NudgeGlassPane.nudge(frameCompHolder.getRootPane(), (frameIsExternalControlled ? getLogLabel() + " > " : "")
                 + message, 6);
     }
 
 	//--------------------------------------------------------------
 	// Public interface methods
 
 	public boolean doUpdateListFromServer() {
 		downloadListButton.doClick(100);
 		return true;
 	}
 
 	/**
 	 * @param logList
 	 * @return
 	 */
 	public boolean doDownloadLogFoldersFromServer(String... logList) {
 		return doDownloadOrDeleteLogFoldersFromServer(true, logList);
 	}
 
 	/**
 	 * @param logList
 	 * @return
 	 */
 	public boolean doDeleteLogFoldersFromServer(String... logList) {
 		return doDownloadOrDeleteLogFoldersFromServer(false, logList);
 	}
 
 	/**
 	 * @param downloadOrDelete
 	 * @param logList
 	 * @return
 	 */
 	private boolean doDownloadOrDeleteLogFoldersFromServer(final boolean downloadOrDelete,
 			String... logList) {
 		if (logList == null)
 			return false;
 		else if (logList.length == 0)
 			return false;
 		final LinkedList<LogFolderInfo> folders = new LinkedList<LogFolderInfo>();
 		for (String str : logList) {
 			if (logFolderList.containsFolder(new LogFolderInfo(str)))
 				folders.add(logFolderList.getFolder(str));
 		}
 		if (folders.size() == 0)
 			return false;
 		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
 			@Override
 			protected Void doInBackground()
 					throws Exception {
 				logFolderList.setEnabled(false);
 				logFolderList.clearSelection();
 				logFolderList.setValueIsAdjusting(true);
 				for (LogFolderInfo logFd : folders) {
 					//logFolderList.setSelectedValue(logFd, false);
 					int iS = logFolderList.myModel.indexOf(logFd);
 					iS = logFolderList.convertIndexToView(iS);
 					logFolderList.addSelectionInterval(iS, iS);
 				}
 				logFolderList.setValueIsAdjusting(false);
 				if (downloadOrDelete)
 					downloadSelectedLogDirsButton.doClick(100);
 				else
 					deleteSelectedLogFoldersButton.doClick(100);
 				return null;
 			}
 			@Override
 			protected void done() {
 				super.done();
 				logFolderList.setEnabled(true);
 			}
 		};
 		worker.execute();
 		return true;
 	}
 
 
 	/**
 	 * @param logList Use it if you want specific log folder state info., if 
 	 *                 not present gives all.
 	 * @return
 	 */
 	public LinkedHashMap<String, LogFolderInfo.State> doGiveStateOfLogFolders(String... logList) {
 		LinkedHashMap<String, LogFolderInfo.State> res = new LinkedHashMap<String, LogFolderInfo.State>();
 		Vector<String> filter = null;
 		if (logList != null) {
 			if (logList.length > 0) {
 				filter = new Vector<String>();
 				for (String str : logList) {
 					filter.add(str);
 				}
 			}
 		}
 		for (Enumeration<?> iterator = logFolderList.myModel.elements(); iterator
 				.hasMoreElements();) {
 			LogFolderInfo lfd = (LogFolderInfo) iterator.nextElement();
 			if (filter == null)
 				res.put(lfd.getName(), lfd.getState());
 			else {
 				if (filter.size() == 0)
 					break;
 				if (filter.contains(lfd.getName())) {
 					res.put(lfd.getName(), lfd.getState());
 					filter.remove(lfd.getName());
 				}
 			}
 		}
 		return res;
 	}
 
 	/**
 	 * @return
 	 */
 	public String[] doGiveListOfLogFolders() {
 		LinkedList<String> list = new LinkedList<String>();
 		for (Enumeration<?> iterator = logFolderList.myModel.elements(); iterator
 				.hasMoreElements();) {
 			LogFolderInfo lfd = (LogFolderInfo) iterator.nextElement();
 			list.add(lfd.getName());
 		}
 		return list.toArray(new String[0]);
 	}
 
 	/**
 	 * @param logFolder
 	 * @return
 	 */
 	public String[] doGiveListOfLogFolderFiles(String logFolder) {
 		LinkedList<String> list = new LinkedList<String>();
 		for (Enumeration<?> iterator = logFolderList.myModel.elements(); iterator
 				.hasMoreElements();) {
 			LogFolderInfo lfd = (LogFolderInfo) iterator.nextElement();
 			if (lfd.getName().equalsIgnoreCase(logFolder)) {
 				for (LogFileInfo lfx : lfd.getLogFiles()) {
 					list.add(lfx.getName());
 				}
 				break;
 			}
 		}
 		return list.toArray(new String[0]);
 	}
 
 	/**
 	 * @param logFolder
 	 * @return
 	 */
 	public LinkedHashMap<String, LogFolderInfo.State> doGiveStateOfLogFolderFiles(String logFolder) {
 		LinkedHashMap<String, LogFolderInfo.State> res = new LinkedHashMap<String, LogFolderInfo.State>();
 		for (Enumeration<?> iterator = logFolderList.myModel.elements(); iterator
 				.hasMoreElements();) {
 			LogFolderInfo lfd = (LogFolderInfo) iterator.nextElement();
 			if (lfd.getName().equalsIgnoreCase(logFolder)) {
 				for (LogFileInfo lfx : lfd.getLogFiles()) {
 					res.put(lfx.getName(), lfx.getState());
 				}
 				break;
 			}
 		}
 		return res;
 	}
 
 
 	/**
 	 * 
 	 */
 	private void doStopLogFoldersDownloads(String... logList) {
 		boolean stopAll = true;
 		if (logList != null)
 			if (logList.length > 0)
 				stopAll = false;
 		Component[] components = downloadWorkersHolder.getComponents();
 		for (Component cp : components) {
 			try {
 				DownloaderPanel workerD = (DownloaderPanel) cp;
 				if (workerD.getState() == DownloaderPanel.State.WORKING) {
 					if (!stopAll) {
 						for (String prefix : logList) {
 							if (workerD.getName().startsWith(prefix)) {
 								workerD.actionStop();
 								break;
 							}
 						}
 						continue;
 					}
 					workerD.actionStop();
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	
 	public synchronized boolean doReset(boolean justStopDownloads) {
 		try {
 			if (!justStopDownloads)
 			    warnLongMsg(I18n.text("Resetting... Wait please..."));
 			doStopLogFoldersDownloads();
 			closeGetConnections();
 			if (!justStopDownloads)
 			    cleanInterface();
 		} catch (Exception e) {
 			e.printStackTrace();
 			warnLongMsg(I18n.textf("Error couth on resetting: %errormessage", e.getMessage()));
 			return false;
 		}
 		return true;
 	}
 	
 	private void closeGetConnections() {
 		synchronized (listActiveGetMethods) {
 			for (HttpRequestBase get : listActiveGetMethods.toArray(new HttpRequestBase[0])) {
 				if (get != null) {
 					get.abort();
 //					get.releaseConnection();
 				}
 			}
 			listActiveGetMethods.clear();
 		}
 	}
 
 	/**
 	 * Used to update the just deleted files from {@link #deleteSelectedLogFoldersAction}
 	 * or {@link #deleteSelectedLogFilesAction}.
      * @param logFiles
      * @return
      */
     private LinkedHashSet<LogFileInfo> updateLogFilesStateDeleted(LinkedHashSet<LogFileInfo> logFiles) {
         LinkedHashSet<LogFileInfo> toDelFL = new LinkedHashSet<LogFileInfo>(); 
         for (LogFileInfo lfx : logFiles) {
         	lfx.setState(LogFolderInfo.State.LOCAL);
         	DownloaderPanel workerD = new DownloaderPanel(client, 
         			lfx.getName(), 
         			createURI(host, port, getFullPartialPath(lfx.getUriPartial())),
         			getFileTarget(lfx.getName()));
         	
         	Component[] components = downloadWorkersHolder.getComponents();
         	for (Component cp : components) {
         		try {
         			DownloaderPanel dpp = (DownloaderPanel) cp;
         			//System.out.println("........... "+dpp.getName());
         			if (workerD.getName().equals(dpp.getName())) {
         				//System.out.println("...........");
         				workerD = dpp;
         				if (workerD.getState() == DownloaderPanel.State.WORKING) {
         					workerD.addStateChangeListener(null);
         					workerD.actionStop();
         					final DownloaderPanel workerDFinal = workerD;
         					SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
         						@Override
         						protected Void doInBackground()
         								throws Exception {
         							if (workerDFinal.getState() == DownloaderPanel.State.IDLE) {
         								downloadWorkersHolder.remove(workerDFinal);
         								downloadWorkersHolder.revalidate();
         								downloadWorkersHolder.repaint();
         							}
         							return null;
         						}
         					};
         					worker.execute();
         				}
         				break;
         			}
         		} catch (Exception e) {
         			e.printStackTrace();
         		}
         	}
         	if (!getFileTarget(lfx.getName()).exists()) {
         		toDelFL.add(lfx);
         		//logFd.getLogFiles().remove(lfx); //This cannot be done here
         	}
         	lfx.setState(LogFolderInfo.State.LOCAL);
         }
         return toDelFL;
     }
 
     /**
 	 * @param args
 	 */
 	@SuppressWarnings("serial")
     public static void main(String[] args) {
 		GuiUtils.setLookAndFeel();
 		
 		final LogsDownloaderWorker logFetcher = new LogsDownloaderWorker();
 
 		logFetcher.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 //		logFetcher.setHost("192.168.106.30");
 //		logFetcher.setPort(8080);
 //		logFetcher.setBasePath("/dune/logs/");
 		
 		logFetcher.setHost("192.168.56.101");
 		//logFetcher.setHost("127.0.0.1");
 		logFetcher.setHost("192.168.106.30");
 		logFetcher.setHost("127.0.0.1");
 		//logFetcher.setHost("192.168.106.34");
 		//logFetcher.setPort(8080);
 		//logFetcher.setBasePath("/images/");
 
 		logFetcher.setLogLabel("lauv_test");
 		
 		logFetcher.setVisibleBasePath(true);
 		//logFetcher.setVisibleHost(false);
 		//logFetcher.setVisiblePort(false);
 		//logFetcher.setVisibleLogLabel(false);
 		logFetcher.setEnableLogLabel(true);
 		logFetcher.setVisible(true);
 		
 		System.out.println(" doGiveListOfLogFolders");
 		for (String str : logFetcher.doGiveListOfLogFolders()) {
 			System.out.println("  ->"+str);
 		}
 		
 		
 		logFetcher.doUpdateListFromServer();
 		try {
 			Thread.sleep(5000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		logFetcher.doDownloadLogFoldersFromServer("20090918/174214", "20090916/152804");
 
 //		System.out.println(" doGiveListOfLogFolders");
 //		for (String str : logFetcher.doGiveListOfLogFolders()) {
 //			System.out.println("  ->"+str);
 //		}
 //
 //		logFetcher.setVisible(false);
 //
 //		try {
 //			Thread.sleep(3000);
 //		} catch (InterruptedException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 //
 //		LinkedHashMap<String, LogFolderInfo.State> states = logFetcher.doGiveStateOfLogFolders();
 //		System.out.println(" doGiveStateOfLogFolders no filter");
 //		for (String str : states.keySet()) {
 //			System.out.println("  .....>"+str+"  "+states.get(str));
 //		}
 //
 //		states = logFetcher.doGiveStateOfLogFolders("20090918/174214");
 //		System.out.println(" doGiveStateOfLogFolders with filter");
 //		for (String str : states.keySet()) {
 //			System.out.println("  .....>"+str+"  "+states.get(str));
 //		}
 //
 //		logFetcher.doDeleteLogFoldersFromServer("20090918/174214");
 //		
 //		
 //		try {
 //			Thread.sleep(10000);
 //		} catch (InterruptedException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 //		logFetcher.setVisible(true);
 
 //		System.out.println("Usable space: " + MathMiscUtils.parseToEngineeringRadix2Notation(new File(logFetcher.dirBaseToStoreFiles).getUsableSpace(),2)+"B");
 //		System.out.println("Free   space: " + MathMiscUtils.parseToEngineeringRadix2Notation(new File(logFetcher.dirBaseToStoreFiles).getFreeSpace(),2)+"B");
 //		System.out.println("Total  space: " + MathMiscUtils.parseToEngineeringRadix2Notation(new File(logFetcher.dirBaseToStoreFiles).getTotalSpace(),2)+"B");
 //		System.out.println("Usable space: " + MathMiscUtils.parseToEngineeringRadix2Notation(new File("d:\\").getUsableSpace(),2)+"B");
 //		System.out.println("Usable space: " + MathMiscUtils.parseToEngineeringRadix2Notation(new File(".").getUsableSpace(),2)+"B");
 //		System.out.println("Usable space: " + MathMiscUtils.parseToEngineeringRadix2Notation(new File("sdd").getUsableSpace(),2)+"B");
 //		System.out.println("Usable space: " + MathMiscUtils.parseToEngineeringRadix2Notation(new File("k:\\").getUsableSpace(),2)+"B");
 
 	
 //		try {
 //			Thread.sleep(10000);
 //		} catch (InterruptedException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 //		logFetcher.doReset();
 
 		logFetcher.dummyLists = true;
 		
         int port = 8080;
 		Server server = new Server(port);
 		
 		server = new Server(port);
         Context root = new Context(server, "/", Context.SESSIONS);
         HttpServlet serv = new HttpServlet() {
             @Override
             protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1)
                     throws ServletException, IOException {
                 arg1.setContentType("text/html");
                 arg1.getWriter().write("<html><head><title>Neptus Web Services</title></head><body><h1>Neptus Web Services</h1>");
                 
                 try { Thread.sleep(60000*2); } catch (InterruptedException e) { }
                 
                 arg1.getWriter().write("</body></html>");
                 arg1.getWriter().close();
             }
 
         };
         root.addServlet(new ServletHolder(serv), "/*");
         try {
             server.start();
         }
         catch (Exception e) {
             e.printStackTrace();
         }
 
         
         // Test 2 sharing a frame
         JFrame frame = new JFrame("Logs Downloader");
         frame.setSize(900, 560);
         frame.setIconImages(ConfigFetch.getIconImagesForFrames());
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         
         LogsDownloaderWorker logF1 = new LogsDownloaderWorker(frame);
         LogsDownloaderWorker logF2 = new LogsDownloaderWorker(frame);
 
         logF1.dummyLists = true;
         logF2.dummyLists = true;
         
         logF1.setHost("192.168.106.30");
         logF2.setHost("192.168.106.31");
         
         logF1.setLogLabel("lauv_test_1");
         logF2.setLogLabel("lauv_test_2");
 
         JTabbedPane tabbledPane = new JTabbedPane();
         tabbledPane.addTab(logF1.getLogLabel(), logF1.getContentPanel());
         tabbledPane.addTab(logF2.getLogLabel(), logF2.getContentPanel());
         tabbledPane.setSelectedComponent(logF2.getContentPanel());
         
         frame.setLayout(new BorderLayout());
         frame.add(tabbledPane);
         frame.setVisible(true);
 	}
 }
