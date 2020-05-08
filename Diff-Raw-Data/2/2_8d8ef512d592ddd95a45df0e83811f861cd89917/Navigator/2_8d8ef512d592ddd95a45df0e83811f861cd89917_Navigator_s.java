 package Sirius.navigator;
 
 import Sirius.navigator.connection.Connection;
 import Sirius.navigator.connection.ConnectionFactory;
 import Sirius.navigator.connection.ConnectionSession;
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.connection.proxy.ConnectionProxy;
 import Sirius.navigator.event.CatalogueActivationListener;
 import Sirius.navigator.event.CataloguePopupMenuListener;
 import Sirius.navigator.event.CatalogueSelectionListener;
 import Sirius.navigator.exception.ConnectionException;
 import Sirius.navigator.exception.ExceptionManager;
 import Sirius.navigator.method.MethodManager;
 import Sirius.navigator.plugin.PluginRegistry;
 import Sirius.navigator.resource.PropertyManager;
 import Sirius.navigator.resource.ResourceManager;
 import Sirius.navigator.search.CidsSearchInitializer;
 import Sirius.navigator.search.dynamic.FormDataBean;
 import Sirius.navigator.search.dynamic.SearchDialog;
 import Sirius.navigator.types.treenode.RootTreeNode;
 import Sirius.navigator.ui.*;
 import Sirius.navigator.ui.dnd.MetaTreeNodeDnDHandler;
 import Sirius.navigator.ui.attributes.AttributeViewer;
 import Sirius.navigator.ui.attributes.editor.AttributeEditor;
 import Sirius.navigator.ui.dialog.LoginDialog;
 import Sirius.navigator.ui.progress.ProgressObserver;
 import Sirius.navigator.ui.status.MutableStatusBar;
 import Sirius.navigator.ui.status.StatusChangeListener;
 import Sirius.navigator.ui.tree.MetaCatalogueTree;
 import Sirius.navigator.ui.tree.SearchResultsTree;
 import Sirius.navigator.ui.tree.SearchResultsTreePanel;
 import Sirius.navigator.ui.widget.FloatingFrameConfigurator;
 import Sirius.server.newuser.UserException;
 import Sirius.server.newuser.permission.*;
 import Sirius.server.middleware.types.*;
 import de.cismet.cids.editors.NavigatorAttributeEditorGui;
 import de.cismet.lookupoptions.options.ProxyOptionsPanel;
 import de.cismet.security.Proxy;
 import de.cismet.tools.CismetThreadPool;
 import de.cismet.tools.StaticDebuggingTools;
 import de.cismet.tools.gui.CheckThreadViolationRepaintManager;
 import de.cismet.tools.gui.EventDispatchThreadHangMonitor;
 
 import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
 
 import java.awt.event.*;
 import java.awt.BorderLayout;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.URL;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.swing.*;
 import java.util.*;
 import java.util.prefs.*;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Handler;
 import org.mortbay.jetty.HttpConnection;
 import org.mortbay.jetty.Request;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.handler.AbstractHandler;
 import org.mortbay.jetty.handler.HandlerCollection;
 import org.mortbay.jetty.nio.SelectChannelConnector;
 
 /**
  *
  * @author  pascal
  */
 public class Navigator extends JFrame {
 
     private final Logger logger;// = Logger.getLogger(Navigator.class);
     private final PropertyManager propertyManager;
     private static final ResourceManager resourceManager = ResourceManager.getManager();
     private final ExceptionManager exceptionManager;
     private final ProgressObserver progressObserver;
     private LoginDialog loginDialog;
     private LayoutedContainer container;
     private MutableMenuBar menuBar;
     private MutableToolBar toolBar;
     private MutableStatusBar statusBar;
     private MutablePopupMenu popupMenu;
     private MetaCatalogueTree metaCatalogueTree;
     private SearchResultsTree searchResultsTree;
     private AttributeViewer attributeViewer;
     private AttributeEditor attributeEditor;
     private SearchDialog searchDialog;
     private Preferences preferences;
     /**
      * Holds value of property disposed.
      */
     private boolean disposed = false;    //InfoNode
     //Panels
     private SearchResultsTreePanel searchResultsTreePanel;
     private DescriptionPane descriptionPane;
     private JPanel metaCatalogueTreePanel;
     private NavigatorSplashScreen splashScreen;
     public final static String NAVIGATOR_HOME = System.getProperty("user.home") + "/.navigator/";
 
     /** Creates a new instance of Navigator */
     public Navigator(ProgressObserver progressObserver, NavigatorSplashScreen splashScreen) throws Exception {
         this.logger = Logger.getLogger(this.getClass());
 
         this.progressObserver = progressObserver;
         this.splashScreen = splashScreen;
 
         this.propertyManager = PropertyManager.getManager();
 
         this.preferences = Preferences.userNodeForPackage(this.getClass());
 
         this.exceptionManager = ExceptionManager.getManager();
 
         this.init();
 
     }
     
 
     /** Creates a new instance of Navigator */
     public Navigator(ProgressObserver progressObserver) throws Exception {
         this(progressObserver, null);
     }
 
     public Navigator() throws Exception {
         this(new ProgressObserver());
     }
 
     private void init() throws Exception {
         //LAFManager.getManager().changeLookAndFeel(LAFManager.WINDOWS);
         if (StaticDebuggingTools.checkHomeForFile("cismetDebuggingInitEventDispatchThreadHangMonitor")) {  // NOI18N
             EventDispatchThreadHangMonitor.initMonitoring();
         }
         if (StaticDebuggingTools.checkHomeForFile("cismetBeansbindingDebuggingOn")) {  // NOI18N
             System.setProperty("cismet.beansdebugging", "true");  // NOI18N
         }
         if (StaticDebuggingTools.checkHomeForFile("cismetCheckForEDThreadVialoation")) {  // NOI18N
             RepaintManager.setCurrentManager(new CheckThreadViolationRepaintManager());
         }
 
         final ProxyOptionsPanel proxyOptions = new ProxyOptionsPanel();
         proxyOptions.setProxy(Proxy.fromPreferences());
 
         boolean inSplashScreen = false;
 
         // splashscreen gesetzt?
         if (splashScreen != null) {
             // ProxyOptions panel soll im SplashScreen integriert werden
             inSplashScreen = true;
 
             // panel übergeben
             splashScreen.setProxyOptionsPanel(proxyOptions);
             // panel noch nicht anzeigen
             splashScreen.setProxyOptionsVisible(false);
 
             // auf Anwenden-Button horchen
             splashScreen.addApplyButtonActionListener(new ActionListener() {
 
                 // Anwenden wurde gedrückt
                 @Override
                 public void actionPerformed(ActionEvent ae) {
                     // Proxy in den Preferences setzen
                     proxyOptions.getProxy().toPreferences();
                     // Panel wieder verstecken
                     splashScreen.setProxyOptionsVisible(false);
                 }
             });
 
         }
 
         while (!SessionManager.isConnected()) {
             try {
                 initConnection(Proxy.fromPreferences());
             } catch (final ConnectionException e) { // Verbinden fehlgeschlagen
 
                 if (inSplashScreen) { // das ProxyOptions panel soll im SplashScreen integriert werden
 
                     // ProxyOptions panel anzeigen
                     splashScreen.setProxyOptionsVisible(true);
 
                     // Solange nicht "Anwenden" gedrückt wurde
                     while (splashScreen.isProxyOptionsVisible()) {
                         // warten
                         Thread.sleep(100);
                     }
 
                 } else { // das ProxyOptions panel soll als Dialog angezeigt werden
                     final JOptionPane pane = new JOptionPane(
                             proxyOptions,
                             JOptionPane.QUESTION_MESSAGE,
                             JOptionPane.OK_CANCEL_OPTION);
                     final JDialog dialog = pane.createDialog(null, "Proxy");
                     dialog.setAlwaysOnTop(true);
                     dialog.setVisible(true);
                     dialog.toFront();
                     final Object answer = pane.getValue();
                     if (answer instanceof Integer && JOptionPane.OK_OPTION == ((Integer) answer).intValue()) {
                         proxyOptions.getProxy().toPreferences();
                     } else {
                         throw e;
                     }
                 }
             }
         }
 
         try {
             checkNavigatorHome();
             initUI();
             initWidgets();
             initDialogs();
             initPlugins();
             initEvents();
             initWindow();
            initSearch();
             //Not in EDT
             if (container instanceof LayoutedContainer) {
                 SwingUtilities.invokeLater(new Runnable() {
                     //UGLY WINNING
 
                     public void run() {
                         ((LayoutedContainer) container).loadLayout(LayoutedContainer.DEFAULT_LAYOUT, true, Navigator.this);
                     }
                 });
 
             }
             if (!StaticDebuggingTools.checkHomeForFile("cismetTurnOffInternalWebserver")) {  // NOI18N
                 initHttpServer();
             }
         } catch (InterruptedException iexp) {
             logger.error("navigator start interrupted: " + iexp.getMessage() + "\n disconnecting from server");  // NOI18N
             SessionManager.getSession().logout();
             SessionManager.getConnection().disconnect();
             this.progressObserver.reset();
         }
 
         //From Hell
         KeyStroke configLoggerKeyStroke = KeyStroke.getKeyStroke('L', InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK);
         Action configAction = new AbstractAction() {
 
             public void actionPerformed(ActionEvent e) {
                 java.awt.EventQueue.invokeLater(new Runnable() {
 
                     public void run() {
                         Log4JQuickConfig.getSingletonInstance().setVisible(true);
                     }
                 });
             }
         };
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(configLoggerKeyStroke, "CONFIGLOGGING");  // NOI18N
         getRootPane().getActionMap().put("CONFIGLOGGING", configAction);  // NOI18N
 
     }
 
     private void checkNavigatorHome() {
         try {
             File file = new File(NAVIGATOR_HOME);
             if (file.exists()) {
                 if(logger.isDebugEnabled())
                     logger.debug("Navigator Directory exists."); // NOI18N
             } else {
                 if(logger.isDebugEnabled())
                     logger.debug("Navigator Directory does not exist --> creating"); // NOI18N
                 file.mkdir();
                 if(logger.isDebugEnabled())
                     logger.debug("Navigator Directory successfully created");  // NOI18N
             }
         } catch (Exception ex) {
             logger.error("Error while checking/creating Navigator home directory", ex);  // NOI18N
         }
     }
 
     // #########################################################################
     private void initConnection(final Proxy proxyConfig) throws ConnectionException, InterruptedException {
         progressObserver.setProgress(25,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_25"));//NOI18N
         if (logger.isDebugEnabled()) {
             logger.debug("initialising connection using proxy: " + proxyConfig);
         }
         Connection connection = ConnectionFactory.getFactory().createConnection(propertyManager.getConnectionClass(), propertyManager.getConnectionInfo().getCallserverURL(), proxyConfig);
         ConnectionSession session = null;
         ConnectionProxy proxy = null;
 
         progressObserver.setProgress(50,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_50"));//NOI18N
         // autologin
         if (propertyManager.isAutoLogin()) {
             if(logger.isInfoEnabled())
                 logger.info("performing autologin of user '" + propertyManager.getConnectionInfo().getUsername() + "'"); // NOI18N
             try {
                 session = ConnectionFactory.getFactory().createSession(connection, propertyManager.getConnectionInfo(), true);
                 proxy = ConnectionFactory.getFactory().createProxy(propertyManager.getConnectionProxyClass(), session);
                 SessionManager.init(proxy);
             } catch (UserException uexp) {
                 logger.error("autologin failed", uexp); // NOI18N
                 session = null;
             }
         }
 
         // autologin = false || autologin failed
         if (!propertyManager.isAutoLogin() || session == null) {
             if(logger.isInfoEnabled())
                 logger.info("performing login"); // NOI18N
             try {
                 session = ConnectionFactory.getFactory().createSession(connection, propertyManager.getConnectionInfo(), false);
             } catch (UserException uexp) {
             } //should never happen
             proxy = ConnectionFactory.getFactory().createProxy(propertyManager.getConnectionProxyClass(), session);
             SessionManager.init(proxy);
 
             loginDialog = new LoginDialog(this);
             loginDialog.setLocationRelativeTo(null);
             loginDialog.show();
         }
 
         PropertyManager.getManager().setEditable(this.hasPermission(SessionManager.getProxy().getClasses(), PermissionHolder.WRITEPERMISSION));
         //PropertyManager.getManager().setEditable(true);
         if(logger.isInfoEnabled())
             logger.info("initConnection(): navigator editor enabled: " + PropertyManager.getManager().isEditable()); // NOI18N
     }
     // #########################################################################
 
     private void initUI() throws InterruptedException {
         progressObserver.setProgress(100,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_100"));  // NOI18N
 
         // vergiss es 2 GHz, keine sanduhr
         //Toolkit.getDefaultToolkit().getSystemEventQueue().push(new WaitCursorEventQueue(200));
 
         menuBar = new MutableMenuBar();
         toolBar = new MutableToolBar(propertyManager.isAdvancedLayout());
 //        JPanel innerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,5));
 //        innerPanel.add(new CidsSearchComboBar());
 //        toolBar.add(innerPanel, -1);
         container = new LayoutedContainer(toolBar, menuBar, propertyManager.isAdvancedLayout());
         if (container instanceof LayoutedContainer) {
             menuBar.registerLayoutManager((LayoutedContainer) container);
         }
         statusBar = new MutableStatusBar();
         popupMenu = new MutablePopupMenu();
 
 
         progressObserver.setProgress(150,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_150"));  // NOI18N
         this.setContentPane(new JPanel(new BorderLayout(), true));
         this.setJMenuBar(menuBar);
 
         //this.getContentPane().add(toolBar, BorderLayout.NORTH);
         //this.getContentPane().add(statusBar, BorderLayout.SOUTH);
         //this.getContentPane().add(container.getContainer() , BorderLayout.CENTER);
 
         JPanel panel = new JPanel(new BorderLayout());
         panel.add(toolBar, BorderLayout.NORTH);
         panel.add(container.getContainer(), BorderLayout.CENTER);
 
         this.getContentPane().add(panel, BorderLayout.CENTER);
         this.getContentPane().add(statusBar, BorderLayout.SOUTH);
 
 
     }
     // #########################################################################
 
     private void initWidgets() throws Exception {
         // MetaCatalogueTree ---------------------------------------------------
         progressObserver.setProgress(200, 
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_200"));  // NOI18N
         RootTreeNode rootTreeNode = new RootTreeNode(SessionManager.getProxy().getRoots());
         metaCatalogueTree = new MetaCatalogueTree(rootTreeNode, PropertyManager.getManager().isEditable(), true, propertyManager.getMaxConnections());
         // dnd
         MetaTreeNodeDnDHandler dndHandler = new MetaTreeNodeDnDHandler(metaCatalogueTree);
 
         MutableConstraints catalogueTreeConstraints = new MutableConstraints(propertyManager.isAdvancedLayout());
         catalogueTreeConstraints.addAsScrollPane(ComponentRegistry.CATALOGUE_TREE,
                 metaCatalogueTree,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.metaCatalogueTree.name"),  // NOI18N
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.metaCatalogueTree.tooltip"),  // NOI18N
                 resourceManager.getIcon("catalogue_tree_icon.gif"), // NOI18N
                 MutableConstraints.P1, MutableConstraints.ANY_INDEX, true);
         container.add(catalogueTreeConstraints);
 
         // SearchResultsTree ---------------------------------------------------
         progressObserver.setProgress(225,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_225"));  // NOI18N
         searchResultsTree = new SearchResultsTree();
         searchResultsTreePanel = new SearchResultsTreePanel(searchResultsTree, propertyManager.isAdvancedLayout());
         // dnd
         new MetaTreeNodeDnDHandler(searchResultsTree);
 
         MutableConstraints searchResultsTreeConstraints = new MutableConstraints(propertyManager.isAdvancedLayout());
         searchResultsTreeConstraints.addAsComponent(ComponentRegistry.SEARCHRESULTS_TREE,
                 searchResultsTreePanel,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.searchResultsTreePanel.name"), // NOI18N
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.searchResultsTreePanel.tooltip"),  // NOI18N
                 resourceManager.getIcon("searchresults_tree_icon.gif"), // NOI18N
                 MutableConstraints.P1, MutableConstraints.ANY_INDEX);
         container.add(searchResultsTreeConstraints);
 
         // AttributePanel ------------------------------------------------------
         progressObserver.setProgress(250,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_250"));  // NOI18N
 
         attributeViewer = new AttributeViewer();
         FloatingFrameConfigurator configurator = new FloatingFrameConfigurator(
                 ComponentRegistry.ATTRIBUTE_VIEWER,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.initWidgets().configurator.name.attributeViewer")); // NOI18N
         configurator.setTitleBarEnabled(false);
 
         MutableConstraints attributePanelConstraints = new MutableConstraints(propertyManager.isAdvancedLayout());
         attributePanelConstraints.addAsFloatingFrame(ComponentRegistry.ATTRIBUTE_VIEWER,
                 attributeViewer,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.attributeviewer.name"), // NOI18N
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.attributeviewer.tooltip"), // NOI18N
                 resourceManager.getIcon("attributetable_icon.gif"),
                 MutableConstraints.P2, 0, false, configurator, false);
         container.add(attributePanelConstraints);
 
         // AttributeEditor .....................................................
         if (PropertyManager.getManager().isEditable()) {
             progressObserver.setProgress(275,
                     org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_275"));  // NOI18N
             //HELL
 //            if (StaticDebuggingTools.checkHomeForFile("cidsExperimentalBeanEditorsEnabled")) {
             attributeEditor = new NavigatorAttributeEditorGui();
 //            } else {
 //                attributeEditor = new AttributeEditor();
 //            }
             configurator = new FloatingFrameConfigurator(ComponentRegistry.ATTRIBUTE_EDITOR,
                     org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.initWidgets().configurator.name.attributeEditor")); // NOI18N
             configurator.setTitleBarEnabled(false);
 
             final MutableConstraints attributeEditorConstraints = new MutableConstraints(true);
             attributeEditorConstraints.addAsFloatingFrame(ComponentRegistry.ATTRIBUTE_EDITOR,
                     attributeEditor,
                     org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.attributeeditor.name"), // NOI18N
                     org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.attributeeditor.tooltip"), // NOI18N
                     resourceManager.getIcon("attributetable_icon.gif"),
                     MutableConstraints.P3, 1, false, configurator, false);
             container.add(attributeEditorConstraints);
 
             // verschieben nach position 1 oder zwei beim Dr�cken von
             // SHIFT + F2 / F3
             InputMap inputMap = attributeEditor.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
             ActionMap actionMap = attributeEditor.getActionMap();
 
             inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.SHIFT_DOWN_MASK, true), MutableConstraints.P2);
             inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK, true), MutableConstraints.P3);
 
             actionMap.put(MutableConstraints.P2, new AbstractAction() {
 
                 public void actionPerformed(ActionEvent e) {
                     attributeEditorConstraints.setPosition(MutableConstraints.P2);
                 }
             });
 
             actionMap.put(MutableConstraints.P3, new AbstractAction() {
 
                 public void actionPerformed(ActionEvent e) {
                     attributeEditorConstraints.setPosition(MutableConstraints.P3);
                 }
             });
 
             if(logger.isInfoEnabled())
                 logger.info("attribute editor enabled");    // NOI18N
         } else {
             if(logger.isInfoEnabled())
                 logger.info("attribute editor disabled");  // NOI18N
         }
 
         // DescriptionPane -----------------------------------------------------
         progressObserver.setProgress(325, 
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_325"));  // NOI18N
 
         descriptionPane = new DescriptionPane();
 
 
 
         configurator = new FloatingFrameConfigurator(ComponentRegistry.DESCRIPTION_PANE,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.initWidgets().configurator.name.descriptionPane")); // NOI18N
         //configurator.setTitleBarEnabled(false);
 
         MutableConstraints descriptionPaneConstraints = new MutableConstraints(propertyManager.isAdvancedLayout());
         descriptionPaneConstraints.addAsFloatingFrame(ComponentRegistry.DESCRIPTION_PANE,
                 descriptionPane,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.descriptionpane.name"), // NOI18N
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.descriptionpane.tooltip"), // NOI18N
                 resourceManager.getIcon("descriptionpane_icon.gif"),
                 MutableConstraints.P3, 0, false, configurator, false);
         container.add(descriptionPaneConstraints);
     }
 
     private void initDialogs() throws Exception {
         progressObserver.setProgress(350,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_350"));  // NOI18N
 
         //searchDialog = new SearchDialog(this, this.searchResultsTree, "Suche", SessionManager.getProxy().getClassTreeNodes(SessionManager.getSession().getUser()), PropertyManager.getManager().getMaxSearchResults());
         searchDialog = new SearchDialog(this, SessionManager.getProxy().getSearchOptions(), SessionManager.getProxy().getClassTreeNodes());
 
         progressObserver.setProgress(550,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_550"));  // NOI18N
         ComponentRegistry.registerComponents(this, container, menuBar, toolBar, popupMenu, metaCatalogueTree, searchResultsTree, attributeViewer, attributeEditor, searchDialog, descriptionPane);
     }
     // #########################################################################
 
     private void initPlugins() throws Exception {
         progressObserver.setProgress(575,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_575"));  // NOI18N
         PluginRegistry.getRegistry().preloadPlugins();
 
         progressObserver.setProgress(650,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_650"));  // NOI18N
         PluginRegistry.getRegistry().loadPlugins();
 
         progressObserver.setProgress(850,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_850"));  // NOI18N
         PluginRegistry.getRegistry().activatePlugins();
     }
     // #########################################################################
 
     private void initEvents() throws InterruptedException {
         progressObserver.setProgress(900,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_900"));  // NOI18N
         StatusChangeListener statusChangeListener = new StatusChangeListener(statusBar);
 
         metaCatalogueTree.addStatusChangeListener(statusChangeListener);
         descriptionPane.addStatusChangeListener(statusChangeListener);
         searchDialog.addStatusChangeListener(statusChangeListener);
 
         CatalogueSelectionListener catalogueSelectionListener = new CatalogueSelectionListener(attributeViewer, descriptionPane);
         metaCatalogueTree.addTreeSelectionListener(catalogueSelectionListener);
         searchResultsTree.addTreeSelectionListener(catalogueSelectionListener);
 
         metaCatalogueTree.addComponentListener(new CatalogueActivationListener(metaCatalogueTree, attributeViewer, descriptionPane));
         searchResultsTree.addComponentListener(new CatalogueActivationListener(searchResultsTree, attributeViewer, descriptionPane));
 
         CataloguePopupMenuListener cataloguePopupMenuListener = new CataloguePopupMenuListener(popupMenu);
         metaCatalogueTree.addMouseListener(cataloguePopupMenuListener);
         searchResultsTree.addMouseListener(cataloguePopupMenuListener);
 
         //Runtime.getRuntime().addShutdownHook(new ShutdownListener());
     }
     // #########################################################################
 
     private void initWindow() throws InterruptedException {
         progressObserver.setProgress(950, 
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_950"));  // NOI18N
         this.setTitle(org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.title"));//NOI18N
         this.setIconImage(resourceManager.getIcon("navigator_icon.gif").getImage());//NOI18N
         this.restoreWindowState();
         this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         this.addWindowListener(new ClosingListener());
         progressObserver.setProgress(1000, 
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_1000"));  // NOI18N
     }
 
     private void initSearch() {
         new CidsSearchInitializer();
     }
     // .........................................................................
 
     private boolean hasPermission(MetaClass[] classes, Sirius.server.newuser.permission.Permission permission) {
 
         String key = SessionManager.getSession().getUser().getUserGroup().getKey().toString();
 
         //propertyManager.getConnectionInfo().getUsergroup()+"@"+propertyManager.getConnectionInfo().getUsergroupDomain();
 
 
         for (int i = 0; i < classes.length; i++) {
             try {
 
                 // falsch aufgerufen schlob SessionManager.getSession().getUser().getUserGroup().getKey()
                 PermissionHolder perm = classes[i].getPermissions();
                 if (logger.isDebugEnabled()) {
                     logger.debug(" usergroup can edit ?? " + key + " permissions :: " + perm);      // NOI18N          //logger.debug(perm +" \n" +key);
                 }
                 if (perm != null && perm.hasPermission(key, permission)) //xxxxxxxxxxxxxxxxxxxxxx user????
                 {
                     if (logger.isDebugEnabled()) {
                         logger.debug("permission '" + permission + "' found in class '" + classes[i] + "'");  // NOI18N
                     }
                     return true;
                 }
 
                 //                if(classes[i].getPermissions().hasPermission(permission)) //xxxxxxxxxxxxxxxxxxxxxx user????
                 //                {
                 //                    if(logger.isDebugEnabled())logger.debug("permission '" + permission + "' found in class '" + classes[i] + "'");
                 //                    return true;
                 //                }
             } catch (Exception exp) {
                 logger.error("hasPermission(): could not check permissions", exp); // NOI18N
             }
         }
 
         logger.warn("permission '" + permission + "' not found, disabling editor");  // NOI18N
         return false;
     }
 
     public void setVisible(final boolean visible) {
         if(logger.isInfoEnabled())
             logger.info("setting main window visible to '" + visible + "'");  // NOI18N
 
         if (SwingUtilities.isEventDispatchThread()) {
             doSetVisible(visible);
         } else {
             if(logger.isDebugEnabled())
                 logger.debug("doSetVisible(): synchronizing method");  // NOI18N
             SwingUtilities.invokeLater(new Runnable() {
 
                 public void run() {
                     doSetVisible(visible);
                 }
             });
         }
 
 
 
         /*if(SwingUtilities.isEventDispatchThread())
         {
         PluginRegistry.getRegistry().setPluginsVisible(visible);
         }
         else
         {
         logger.debug("setPluginsVisible(): synchronizing method");
         SwingUtilities.invokeLater(new Runnable()
         {
         public void run()
         {
         PluginRegistry               PluginRegistry.getRegistry().setPluginsVisible(visible);
         }
         });
         }*/
 
         //PluginRegistry.getRegistry().setPluginsVisible(visible);
     }
 
     private void doSetVisible(final boolean visible) {
         super.setVisible(visible);
 
         //PluginRegistry.getRegistry().setPluginsVisible(visible);
 
         if (visible) {
             this.searchResultsTreePanel.setButtonsEnabled();
             this.container.setDividerLocations(0.23, 0.60);
             this.menuBar.repaint();
             this.toolBar.repaint();
 
 
             //container.select(ComponentRegistry.SEARCHRESULTS_TREE);
             //descriptionPane.setPage("http://www.cismet.de");
 
             this.toFront();
         }
 
         SwingUtilities.invokeLater(new Runnable() {
 
             public void run() {
                 PluginRegistry.getRegistry().setPluginsVisible(visible);
             }
         });
     }
     // .........................................................................
 
     public void dispose() {
 
 
         if(logger.isInfoEnabled())
             logger.info("dispose() called"); // NOI18N
         if (container instanceof LayoutedContainer) {
             if(logger.isInfoEnabled())
                 logger.info("saving Layout"); // NOI18N
             ((LayoutedContainer) container).saveLayout(LayoutedContainer.DEFAULT_LAYOUT, this);
         }
         Navigator.this.saveWindowState();
 
         PluginRegistry.destroy();
 
         SessionManager.getConnection().disconnect();
         SessionManager.destroy();
         MethodManager.destroy();
         ComponentRegistry.destroy();
 
         if (!Navigator.this.isDisposed()) {
             Navigator.super.dispose();
             Navigator.this.setDisposed(true);
         } else {
             logger.warn("...............................");  // NOI18N
         }
     }
 
     private void saveWindowState() {
         int windowHeight = this.getHeight();
         int windowWidth = this.getWidth();
         int windowX = (int) this.getLocation().getX();
         int windowY = (int) this.getLocation().getY();
         boolean windowMaximised = (this.getExtendedState() == MAXIMIZED_BOTH);
 
         if(logger.isInfoEnabled())
             logger.info("saving window state: \nwindowHeight=" + windowHeight + ", windowWidth=" + windowWidth + ", windowX=" + windowX + ", windowY=" + windowY + ", windowMaximised=" + windowMaximised); // NOI18N
 
         this.preferences.putInt("windowHeight", windowHeight);  // NOI18N
         this.preferences.putInt("windowWidth", windowWidth);    // NOI18N
         this.preferences.putInt("windowX", windowX);            // NOI18N
         this.preferences.putInt("windowY", windowY);            // NOI18N
         this.preferences.putBoolean("windowMaximised", windowMaximised);    // NOI18N
         if(logger.isInfoEnabled())
             logger.info("saved window state");  // NOI18N
     }
 
     private void restoreWindowState() {
         if(logger.isInfoEnabled())
             logger.info("restoring window state ...");  // NOI18N
         int windowHeight = this.preferences.getInt("windowHeight", PropertyManager.getManager().getHeight());   // NOI18N
         int windowWidth = this.preferences.getInt("windowWidth", PropertyManager.getManager().getWidth());      // NOI18N
         int windowX = this.preferences.getInt("windowX", 0);        // NOI18N
         int windowY = this.preferences.getInt("windowY", 0);        // NOI18N
         boolean windowMaximised = this.preferences.getBoolean("windowMaximised", propertyManager.isMaximizeWindow());   // NOI18N
 
         if(logger.isInfoEnabled())
             logger.info("restoring window state: \nwindowHeight=" + windowHeight + ", windowWidth=" + windowWidth + ", windowX=" + windowX + ", windowY=" + windowY + ", windowMaximised=" + windowMaximised);  // NOI18N
 
         this.setSize(windowWidth, windowHeight);
         this.setLocation(windowX, windowY);
 
         if (windowMaximised) {
             this.setExtendedState(MAXIMIZED_BOTH);
         }
     }
 
     private class ClosingListener extends WindowAdapter {
 
         /** Invoked when the user attempts to close the window
          * from the window's system menu.  If the program does not
          * explicitly hide or dispose the window while processing
          * this event, the window close operation will be cancelled.
          *
          */
         public void windowClosing(WindowEvent e) {
             if (exceptionManager.showExitDialog(Navigator.this)) {
                 dispose();
                 if(logger.isInfoEnabled())
                     logger.info("closing navigator"); // NOI18N
                 System.exit(0);
             }
         }
     }
 
     /*private class ShutdownListener extends Thread
     {
     public void run()
     {
     if(Navigator.this.isDisposed())
     {
     Navigator.this.logger.info("ShutdownListener: clean shutdown initiated");
     }
     else
     {
     Navigator.this.logger.warn("ShutdownListener: unclean shutdown initiated, invokinbg dispose()");
     Navigator.this.dispose();
     }
     }
     }*/    // -------------------------------------------------------------------------
     public static void main(String args[]) {
         final boolean release = true;
 
         // For some unknown reason, the content of the user.language and the user.country properties
         // must explicitly set as default locale. This is requiered for the cids-navigator project, but
         // not for the other internationalized projects
         try {
             String lang = System.getProperty("user.language");//NOI18N
             String country = System.getProperty("user.country");//NOI18N
 
             if (lang != null && country != null) {
                 Locale.setDefault( new Locale(lang, country) );
             } else if (lang != null) {
                 Locale.setDefault( new Locale(lang) );
             }
         } catch (SecurityException e) {
             System.err.println("You have insufficient rights to set the default locale.");//NOI18N
         }
 
         try {
             // cmdline arguments ...............................................
 
             // <RELEASE>
             if (release) {
                 if (args.length < 5) {
                     String errorString = new String("\nusage: navigator %1 %2 %3 %4 %5 %6(%5)\n%1 = navigator config file \n%2 = navigator working directory \n%3 = plugin base directory \n%4 = navigator search forms base directory \n%5 navigator search profile store (optional, default: %1/profiles \n\nexample: java Sirius.navigator.Navigator c:\\programme\\cids\\navigator\\navigator.cfg c:\\programme\\cids\\navigator\\ c:\\programme\\cids\\navigator\\plugins\\ c:\\programme\\cids\\navigator\\search\\ c:\\programme\\cids\\navigator\\search\\profiles\\"); // NOI18N
                     System.out.println(errorString);
 
                     //System.exit(1);
                     throw new Exception(errorString);
                 } else {
                     System.out.println("-------------------------------------------------------");  // NOI18N
                     System.out.println("C I D S   N A V I G A T 0 R   C O N F I G U R A T I 0 N");  // NOI18N
                     System.out.println("-------------------------------------------------------");  // NOI18N
                     System.out.println("log4j.properties = " + args[0]);                            // NOI18N
                     System.out.println("navigator.cfg    = " + args[1]);                            // NOI18N
                     System.out.println("basedir          = " + args[2]);                            // NOI18N
                     System.out.println("plugindir        = " + args[3]);                            // NOI18N
                     System.out.println("searchdir        = " + args[4]);                            // NOI18N
                     if (args.length > 5) {
                         System.out.println("profilesdir      = " + args[5]);                        // NOI18N
                     }
                     System.out.println("-------------------------------------------------------");  // NOI18N
 
 
                     // log4j configuration .....................................
                     Properties properties = new Properties();
                     boolean l4jinited = false;
                     try {
                         URL log4jPropertiesURL = new URL(args[0]);
                         properties.load(log4jPropertiesURL.openStream());
 
                         l4jinited = true;
                     } catch (Throwable t) {
                         System.err.println("could not lode log4jproperties will try to load it from file" + t.getMessage());    // NOI18N
                         t.printStackTrace();
                     }
 
                     try {
                         if (!l4jinited) {
                             properties.load(new BufferedInputStream(new FileInputStream(new File(args[0]))));
                         }
                     } catch (Throwable t) {
 
                         System.err.println("could not lode log4jproperties " + t.getMessage()); // NOI18N
                         t.printStackTrace();
 
                     }
 
                     PropertyConfigurator.configure(properties);
 
                     // log4j configuration .....................................
 
                     PropertyManager.getManager().configure(args[1], args[2], args[3], args[4], (args.length > 5 ? args[5] : null));
                     resourceManager.setLocale(PropertyManager.getManager().getLocale());
                 }
             }//</RELEASE>
             else {
                 PropertyConfigurator.configure(ClassLoader.getSystemResource("Sirius/navigator/resource/cfg/log4j.debug.properties"));  // NOI18N
 
                 // ohne plugins:
                 //PropertyManager.getManager().configure("D:\\cids\\res\\Sirius\\navigator\\resource\\cfg\\navigator.cfg", System.getProperty("user.home") + "\\.navigator\\", System.getProperty("user.home") + "\\.navigator\\plugins\\", "D:\\cids\\dist\\client\\search\\", null);
 
                 // mit plugins:
                 PropertyManager.getManager().configure("D:\\cids\\res\\Sirius\\navigator\\resource\\cfg\\navigator.cfg", System.getProperty("user.home") + "\\.navigator\\", "D:\\cids\\dist\\client\\plugins\\", "D:\\cids\\dist\\client\\search\\", null);    // NOI18N
                 resourceManager.setLocale(PropertyManager.getManager().getLocale());
 
                 // Properties ausgeben:
                 PropertyManager.getManager().print();
             }
 
             // configuration ...................................................
 
 
             // look and feel ...................................................
             LAFManager.getManager().changeLookAndFeel(PropertyManager.getManager().getLookAndFeel());
 
 
 
 // look and feel ...................................................
 
             // configuration ...................................................
 
 
             // run .............................................................
             //Navigator navigator = new Navigator();
             //navigator.logger.debug("new navigator instance created");
             //navigator.setVisible(true);
 
             //logger.debug("SPLASH");
             NavigatorSplashScreen navigatorSplashScreen =
                     new NavigatorSplashScreen(PropertyManager.getManager().getSharedProgressObserver(),
                     resourceManager.getIcon("wundaLogo.png"));
 
             navigatorSplashScreen.pack();
             navigatorSplashScreen.setLocationRelativeTo(null);
             navigatorSplashScreen.toFront();
             navigatorSplashScreen.show();
 
             // run .............................................................
         } catch (Throwable t) {
             // error .............................................................
             Logger.getLogger(Navigator.class).fatal("could not create navigator instance", t);  // NOI18N
             ExceptionManager.getManager().showExceptionDialog(
                     ExceptionManager.FATAL,
                     org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.main(String[]).ExceptionManager_anon.name"),  // NOI18N
                     org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.main(String[]).ExceptionManager_anon.message"), t);//NOI18N
 
             System.exit(1);
             // error .............................................................
         }
     }
 
     /**
      * Getter for property disposed.
      * @return Value of property disposed.
      */
     public boolean isDisposed() {
 
         return this.disposed;
     }
 
     /**
      * Setter for property disposed.
      * @param disposed New value of property disposed.
      */
     private synchronized void setDisposed(boolean disposed) {
 
         this.disposed = disposed;
     }
 
     private void initHttpServer() {
         Thread t = new Thread(new Runnable() {
 
             public void run() {
                 try {
                     Server server = new Server();
                     Connector connector = new SelectChannelConnector();
                     connector.setPort(propertyManager.getHttpInterfacePort());
                     server.setConnectors(new Connector[]{connector});
 
                     Handler param = new AbstractHandler() {
 
                         public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
                             Request base_request = (request instanceof Request) ? (Request) request : HttpConnection.getCurrentConnection().getRequest();
                             base_request.setHandled(true);
                             response.setContentType("text/html");   // NOI18N
                             response.setStatus(HttpServletResponse.SC_OK);
                             response.getWriter().println("<html><head><title>HTTP interface</title></head><body><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"80%\"><tr><td width=\"30%\" align=\"center\" valign=\"middle\"><img border=\"0\" src=\"http://www.cismet.de/images/cismetLogo250M.png\" ><br></td><td width=\"%\">&nbsp;</td><td width=\"50%\" align=\"left\" valign=\"middle\"><font face=\"Arial\" size=\"3\" color=\"#1c449c\">... and <b><font face=\"Arial\" size=\"3\" color=\"#1c449c\">http://</font></b> just works</font><br><br><br></td></tr></table></body></html>");  // NOI18N
                         }
                     };
                     Handler hello = new AbstractHandler() {
 
                         public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
                             try {
                                 if (request.getLocalAddr().equals(request.getRemoteAddr())) {
 
                                     if(logger.isInfoEnabled())
                                         logger.info("HttpInterface asked");  // NOI18N
                                     if (target.equalsIgnoreCase("/executeSearch")) {    // NOI18N
                                         String query = request.getParameter("query");   // NOI18N
                                         String domain = request.getParameter("domain"); // NOI18N
                                         String classId = request.getParameter("classId");   // NOI18N
                                         HashMap dataBeans = ComponentRegistry.getRegistry().getSearchDialog().getSearchFormManager().getFormDataBeans();
                                         Object object = dataBeans.get(query + "@" + domain);    // NOI18N
                                         HashMap<String, String> params = new HashMap<String, String>();
                                         Set keys = request.getParameterMap().keySet();
                                         Iterator it = keys.iterator();
                                         while (it.hasNext()) {
                                             String key = it.next().toString();
                                             if (!(key.equalsIgnoreCase("query") || key.equalsIgnoreCase("domain") || key.equalsIgnoreCase("classId"))) {    // NOI18N
                                                 params.put(key, request.getParameter(key));
                                             }
                                         }
                                         if (object != null) {
                                             FormDataBean parambean = (FormDataBean) object;
                                             for (String key : params.keySet()) {
                                                 parambean.setBeanParameter(key, params.get(key));
                                             }
                                             Vector v = new Vector();
                                             String cid = classId + "@" + domain;    // NOI18N
                                             v.add(cid);
                                             LinkedList searchFormData = new LinkedList();
                                             searchFormData.add(parambean);
                                             ComponentRegistry.getRegistry().getSearchDialog().search(v, searchFormData, Navigator.this, false);
                                         }
                                     }
                                     if (target.equalsIgnoreCase("/showAkuk")) { // NOI18N
                                         String domain = request.getParameter("domain"); // NOI18N
                                         String classId = request.getParameter("classId");   // NOI18N
                                         String objectIds = request.getParameter("objectIds");   // NOI18N
                                     } else {
                                         logger.warn("Unknown Target: " + target);   // NOI18N
                                     }
 
                                 } else {
                                     logger.warn("Sombody tries to access the HTTP Interface from a different Terminal. Rejected."); // NOI18N
                                 }
                             } catch (Throwable t) {
                                 logger.error("Error while handling HttpRequests", t);   // NOI18N
                             }
                         }
                     };
 
                     HandlerCollection handlers = new HandlerCollection();
                     handlers.setHandlers(new Handler[]{param, hello});
                     server.setHandler(handlers);
 
                     server.start();
                     server.join();
                 } catch (Throwable t) {
                     logger.error("Error in  Navigator HttpInterface on port " + propertyManager.getHttpInterfacePort(), t); // NOI18N
                 }
             }
         });
         CismetThreadPool.execute(t);
     }
 }
