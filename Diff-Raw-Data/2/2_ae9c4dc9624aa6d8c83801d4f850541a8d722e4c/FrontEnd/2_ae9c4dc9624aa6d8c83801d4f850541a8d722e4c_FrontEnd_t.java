 /**
  * Created on Oct 15, 2006
  */
 package bias.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.ComponentOrientation;
 import java.awt.Cursor;
 import java.awt.Dialog;
 import java.awt.Dialog.ModalityType;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.GridLayout;
 import java.awt.SystemTray;
 import java.awt.TrayIcon;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.InputEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.management.ManagementFactory;
 import java.lang.management.MemoryMXBean;
 import java.lang.reflect.Field;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.GeneralSecurityException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Stack;
 import java.util.UUID;
 import java.util.concurrent.Executor;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.DefaultListModel;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JSpinner;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.JToolBar;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.RowFilter;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 import javax.swing.border.LineBorder;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import bias.Constants;
 import bias.Constants.ADDON_STATUS;
 import bias.Constants.TRANSFER_TYPE;
 import bias.Preferences;
 import bias.Preferences.PreferenceChoiceProvider;
 import bias.Preferences.PreferenceValidator;
 import bias.Splash;
 import bias.annotation.Preference;
 import bias.annotation.PreferenceChoice;
 import bias.annotation.PreferenceEnable;
 import bias.annotation.PreferenceProtect;
 import bias.annotation.PreferenceValidation;
 import bias.core.AddOnInfo;
 import bias.core.BackEnd;
 import bias.core.DataCategory;
 import bias.core.DataEntry;
 import bias.core.ExportConfiguration;
 import bias.core.ImportConfiguration;
 import bias.core.Recognizable;
 import bias.core.ToolData;
 import bias.core.TransferData;
 import bias.core.pack.Dependency;
 import bias.core.pack.ObjectFactory;
 import bias.core.pack.Pack;
 import bias.core.pack.PackType;
 import bias.core.pack.Repository;
 import bias.event.AfterSaveEventListener;
 import bias.event.BeforeExitEventListener;
 import bias.event.BeforeSaveEventListener;
 import bias.event.EventListener;
 import bias.event.ExitEvent;
 import bias.event.SaveEvent;
 import bias.event.StartUpEvent;
 import bias.event.StartUpEventListener;
 import bias.event.TransferEvent;
 import bias.event.TransferEventListener;
 import bias.extension.EntryExtension;
 import bias.extension.Extension;
 import bias.extension.ExtensionFactory;
 import bias.extension.MissingExtensionInformer;
 import bias.extension.ObservableTransferExtension;
 import bias.extension.ToolExtension;
 import bias.extension.ToolRepresentation;
 import bias.extension.TransferExtension;
 import bias.extension.TransferProgressListener;
 import bias.gui.VisualEntryDescriptor.ENTRY_TYPE;
 import bias.i18n.I18nService;
 import bias.skin.GUIIcons;
 import bias.skin.Skin;
 import bias.utils.AppManager;
 import bias.utils.ArchUtils;
 import bias.utils.CommonUtils;
 import bias.utils.Downloader;
 import bias.utils.Downloader.DownloadListener;
 import bias.utils.FSUtils;
 import bias.utils.FormatUtils;
 import bias.utils.PropertiesUtils;
 import bias.utils.Validator;
 import bias.utils.VersionComparator;
 
 
 /**
  * @author kion
  */
 public class FrontEnd extends JFrame {
     
     private static final long serialVersionUID = 1L;
     
     private static final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
 
     private static final Map<String, String> MESSAGES = I18nService.getInstance().getMessages();
     
     private static final String DEFAULT_SKIN = "DefaultSkin";
 
     // TODO [P2] implement language-selection-on-first-start feature (or, maybe, choose language automatically based on default system locale ?...) 
     private static final String LOCALE = I18nService.getInstance().getLanguageLocale(Preferences.getInstance().preferredLanguage);
     
     /**
      * Application icon
      */
     private static final ImageIcon ICON_APP = new ImageIcon(FrontEnd.class.getResource("/bias/res/app_icon.png"));
 
     private static final ImageIcon ICON_LOGO = new ImageIcon(FrontEnd.class.getResource("/bias/res/app_logo.png"));
     
     private static final ImageIcon ICON_CLOSE = new ImageIcon(FrontEnd.class.getResource("/bias/res/close.png"));
 
     private static final ImageIcon ICON_PROCESS = new ImageIcon(FrontEnd.class.getResource("/bias/res/process.gif"));
 
     private static final Placement[] PLACEMENTS = new Placement[] { 
             new Placement(JTabbedPane.TOP),
             new Placement(JTabbedPane.LEFT), 
             new Placement(JTabbedPane.RIGHT),
             new Placement(JTabbedPane.BOTTOM)
         };
     
     private static final JLabel exportInfoLabel = new JLabel(
             Constants.HTML_PREFIX + 
             "<b><i>" + Constants.HTML_COLOR_HIGHLIGHT_INFO + getMessage("export.info") + Constants.HTML_COLOR_SUFFIX + "</i></b>" + 
             Constants.HTML_SUFFIX);
     
     private static final UUID rootID = UUID.fromString("e83310de-3a52-454b-940c-78ce65bd51ec");
 
     private static AddOnFileChooser addOnFileChooser = new AddOnFileChooser();
 
     private static IconsFileChooser iconsFileChooser = new IconsFileChooser();
     
     private static FrontEnd instance;
     
     private static Map<String, ImageIcon> icons = new HashMap<String, ImageIcon>();
     
     private static Map<Class<? extends ToolExtension>, ToolExtension> tools;
 
     private static Map<Class<? extends TransferExtension>, TransferExtension> transferrers;
 
     private static Map<Class<? extends ToolExtension>, JPanel> indicatorAreas = new HashMap<Class<? extends ToolExtension>, JPanel>();
 
     // use default control icons initially
     private static GUIIcons guiIcons = new GUIIcons();
 
     private static Properties config;
     
     private static String activeSkin = null;
     
     private static Map<String, DataEntry> dataEntries = new HashMap<String, DataEntry>();
 
     private static Map<UUID, EntryExtension> entryExtensions = new LinkedHashMap<UUID, EntryExtension>();
     
     private static Stack<UUID> navigationHistory = new Stack<UUID>();
     
     private static int navigationHistoryIndex = -1;
 
     private static boolean navigating = false;
 
     private static Map<DefaultMutableTreeNode, Recognizable> nodeEntries;
 
     private static Map<DefaultMutableTreeNode, Collection<DefaultMutableTreeNode>> categoriesToExportRecursively;
 
     private static boolean sysTrayIconVisible = false;
     
     private static TrayIcon trayIcon = null;
     
     private static SimpleDateFormat dateFormat;
     
     private TabMoveListener tabMoveListener = new TabMoveListener();
     
     private int opt;
     
     private boolean hotKeysBindingsChanged = true;
 
     private boolean tabsInitialized;
     
     private String lastAddedEntryType = null;
     
     private Map<String, Integer> depCounters;
     
     private Map<String, Boolean> states;
 
     private JCheckBox onlineShowAllPackagesCB;
     
     private JTable onlineList;
     
     private DefaultTableModel extModel;
     
     private DefaultTableModel skinModel;
 
     private DefaultTableModel libModel;
     
     private DefaultTableModel icSetModel;
     
     private DefaultListModel icModel;
     
     private DefaultTableModel onlineModel;
     
     private JProgressBar transferProgressBar;
     
     private JProgressBar onlineSingleProgressBar;
     
     private JProgressBar onlineTotalProgressBar;
     
     private JTable extList;
     
     private JTable skinList;
     
     private JTable icSetList;
     
     private JList icList;
     
     private JTable libList;
     
     private JTabbedPane addOnsPane = null;
     
     private JLabel addOnsManagementScreenProcessLabel = null;
 
     private JProgressBar memUsageProgressBar = null;
     
     private JList statusBarMessagesList = null;
 
     private JFrame addOnsManagementDialog = null;
     
     private JLabel dependenciesLabel = null;
 
     private JScrollPane detailsPane = null;
 
     private JTextPane detailsTextPane = null;
     
     private JTabbedPane currentTabPane = null;
     
     private JPanel jContentPane = null;
 
     private JTabbedPane jTabbedPane = null;
 
     private JToolBar jToolBar = null;
 
     private JToolBar jToolBar3 = null;
 
     private JButton jButton1 = null;
 
     private JButton jButton2 = null;
 
     private JButton jButton12 = null;
 
     private JButton jButton4 = null;
 
     private JButton jButton5 = null;
 
     private JButton jButton17 = null;
 
     private JButton jButton18 = null;
 
     private JButton jButton19 = null;
 
     private JButton jButton11 = null;
 
     private JButton jButton13 = null;
 
     private JButton jButton14 = null;
 
     private JButton jButton15 = null;
 
     private JButton jButton16 = null;
 
     private JPanel jPanel = null;
 
     private JPanel jPanel4 = null;
 
     private JPanel jPanelStatusBar = null;
 
     private JPanel jPanelIndicators = null;
 
     private JPanel jPanelProcessIndicators = null;
 
     private JPanel addOnsManagementScreenProcessPanel = null;
 
     private JLabel processLabel = null;
     
     private JLabel jLabelStatusBarMsg = null;
 
     private JPanel jPanel5 = null;
 
     private JPanel jPanel15 = null;
 
     private JPanel jPanel16 = null;
 
     private JPanel jPanel2 = null;
 
     private JPanel jPanel3 = null;
     
     private JButton bottomPanelCloseButton;
 
     private JButton sideBarCloseButton;
 
     private JSplitPane jSplitPane = null;
 
     private JSplitPane jSplitPane1 = null;
 
     private JToolBar jToolBar2 = null;
 
     private JButton jButton3 = null;
 
     private JButton jButton6 = null;
 
     private JButton jButton7 = null;
 
     private JButton jButton8 = null;
 
     private JButton jButton9 = null;
 
     private JButton jButton10 = null;
 
     private static boolean unusedAddOnDataAndConfigFilesCleanedUp = false;
     
     private static Map<String, Pack> availableOnlinePackages;
     
     private static Map<String, Pack> getAvailableOnlinePackages() {
         if (availableOnlinePackages == null) {
             availableOnlinePackages = new HashMap<String, Pack>();
         }
         return availableOnlinePackages;
     }
     
     private static Unmarshaller unmarshaller;
 
     private static Unmarshaller getUnmarshaller() throws JAXBException {
         if (unmarshaller == null) {
             unmarshaller = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName()).createUnmarshaller();
         }
         return unmarshaller;
     }
     
     public static GUIIcons getGUIIcons() {
         return guiIcons;
     }
 
     private static Map<Class<? extends StartUpEventListener>, StartUpEventListener> startUpEventListeners;
     public static void addStartUpEventListener(StartUpEventListener l) {
         if (startUpEventListeners == null) {
             startUpEventListeners = new HashMap<Class<? extends StartUpEventListener>, StartUpEventListener>();
         }
         addEventListener(startUpEventListeners, l);
     }
     public static void removeStartUpEventListener(StartUpEventListener l) {
         removeEventListener(startUpEventListeners, l);
     }
     private static void fireStartUpEvent(StartUpEvent e) {
         if (startUpEventListeners != null) {
             for (StartUpEventListener l : startUpEventListeners.values()) {
                 try {
                     l.onEvent(e);
                 } catch (Throwable t) {
                     displayErrorMessage("start-up event listener '" + l.getClass().getSimpleName() + "' failed!", t);
                 }
             }
         }
     }
     
     private static Map<Class<? extends TransferEventListener>, TransferEventListener> transferEventListeners;
     public static void addTransferEventListener(TransferEventListener l) {
         if (transferEventListeners == null) {
             transferEventListeners = new HashMap<Class<? extends TransferEventListener>, TransferEventListener>();
         }
         addEventListener(transferEventListeners, l);
     }
     public static void removeTransferEventListener(TransferEventListener l) {
         removeEventListener(transferEventListeners, l);
     }
     private static void fireTransferEvent(TransferEvent e) {
         if (transferEventListeners != null) {
             for (TransferEventListener l : transferEventListeners.values()) {
                 try {
                     l.onEvent(e);
                 } catch (Throwable t) {
                     displayErrorMessage("transfer event listener (" + e.getTransferType().name() + ") '" + l.getClass().getSimpleName() + "' failed!", t);
                 }
             }
         }
     }
     
     private static Map<Class<? extends BeforeSaveEventListener>, BeforeSaveEventListener> beforeSaveEventListeners;
     public static void addBeforeSaveEventListener(BeforeSaveEventListener l) {
         if (beforeSaveEventListeners == null) {
             beforeSaveEventListeners = new HashMap<Class<? extends BeforeSaveEventListener>, BeforeSaveEventListener>();
         }
         addEventListener(beforeSaveEventListeners, l);
     }
     public static void removeBeforeSaveEventListener(StartUpEventListener l) {
         removeEventListener(beforeSaveEventListeners, l);
     }
     private static void fireBeforeSaveEvent(SaveEvent e) {
         if (beforeSaveEventListeners != null) {
             for (BeforeSaveEventListener l : beforeSaveEventListeners.values()) {
                 try {
                     l.onEvent(e);
                 } catch (Throwable t) {
                     displayErrorMessage("before-save event listener '" + l.getClass().getSimpleName() + "' failed!", t);
                 }
             }
         }
     }
     
     private static Map<Class<? extends AfterSaveEventListener>, AfterSaveEventListener> afterSaveEventListeners;
     public static void addAfterSaveEventListener(AfterSaveEventListener l) {
         if (afterSaveEventListeners == null) {
             afterSaveEventListeners = new HashMap<Class<? extends AfterSaveEventListener>, AfterSaveEventListener>();
         }
         addEventListener(afterSaveEventListeners, l);
     }
     public static void removeAfterSaveEventListener(StartUpEventListener l) {
         removeEventListener(afterSaveEventListeners, l);
     }
     private static void fireAfterSaveEvent(SaveEvent e) {
         if (afterSaveEventListeners != null) {
             for (AfterSaveEventListener l : afterSaveEventListeners.values()) {
                 try {
                     l.onEvent(e);
                 } catch (Throwable t) {
                     displayErrorMessage("after-save event listener '" + l.getClass().getSimpleName() + "' failed!", t);
                 }
             }
         }
     }
     
     private static Map<Class<? extends BeforeExitEventListener>, BeforeExitEventListener> beforeExitEventListeners;
     public static void addBeforeExitEventListener(BeforeExitEventListener l) {
         if (beforeExitEventListeners == null) {
             beforeExitEventListeners = new HashMap<Class<? extends BeforeExitEventListener>, BeforeExitEventListener>();
         }
         addEventListener(beforeExitEventListeners, l);
     }
     public static void removeBeforeExitEventListener(StartUpEventListener l) {
         removeEventListener(beforeExitEventListeners, l);
     }
     private static void fireBeforeExitEvent(ExitEvent e) {
         if (beforeExitEventListeners != null) {
             for (BeforeExitEventListener l : beforeExitEventListeners.values()) {
                 try {
                     l.onEvent(e);
                 } catch (Throwable t) {
                     displayErrorMessage("before exit event listener '" + l.getClass().getSimpleName() + "' failed!", t);
                 }
             }
         }
     }
     
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private static void addEventListener(Map listeners, EventListener l) {
         if (listeners.get(l.getClass()) == null) {
             listeners.put(l.getClass(), l);
         }
     }
     
     @SuppressWarnings("rawtypes")
     private static void removeEventListener(Map listeners, EventListener l) {
         if (listeners != null) {
             listeners.remove(l.getClass());
         }
     }
     
     private static Collection<ComponentListener> mainWindowComponentListeners;
     public static void addMainWindowComponentListener(ComponentListener l) {
         if (mainWindowComponentListeners == null) {
             mainWindowComponentListeners = new ArrayList<ComponentListener>();
         }
         if (instance != null) {
             instance.addComponentListener(l);
         } else {
             mainWindowComponentListeners.add(l);
         }
     }
     public static void removeMainWindowComponentListener(ComponentListener l) {
         if (instance != null) {
             instance.removeComponentListener(l);
         } else if (mainWindowComponentListeners != null) {
             mainWindowComponentListeners.remove(l);
         }
     }
     
     /**
      * Default singleton's hidden constructor without parameters
      */
     private FrontEnd() {
         super();
         initialize();
     }
 
     /**
      * This method initializes this
      * 
      * @return void
      */
     private void initialize() {
         try {
             this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
             this.setMinimumSize(new Dimension(640, 480));
             this.setTitle(getMessage("app.title") + " [ " + getMessage("version") + Constants.BLANK_STR + BackEnd.getInstance().getAppCoreVersion() + " ]");
             this.setIconImage(ICON_APP.getImage());
             this.setContentPane(getJContentPane());
 
             representData(BackEnd.getInstance().getData());
             
             applyGlobalSettings();
 
             this.addWindowListener(new WindowAdapter() {
             	
                 @Override
                 public void windowClosing(WindowEvent e) {
                     try {
                         if (Preferences.getInstance().remainInSysTrayOnWindowClose) {
                             showSysTrayIcon();
                             if (sysTrayIconVisible) {
                                 instance.setVisible(false);
                             }
                         } else {
                             exit();
                         }
                     } catch (Throwable t) {
                         displayErrorMessage(t);
                     }
                 }
                 
             });
             
         	if (new File(Constants.ROOT_DIR, Constants.UPDATE_FILE_PREFIX + Constants.APP_LAUNCHER_FILE_NAME).exists()) {
         		Splash.hideSplash();
                 JOptionPane.showMessageDialog(
                         getActiveWindow(), 
                         new JLabel(
                                 Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_INFO + 
                                 getMessage("info.message.launcher.updated", Constants.ROOT_DIR.toString()) + 
                                 Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX));
         	}
         	
         	Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
 				@Override
 				public void run() {
 					emergencyExit();
 				}
 			}));
 
         } catch (Exception ex) {
             displayErrorMessage(ex);
         }
     }
     
     public static void startup() {
         initInstance();
         displayStatusBarMessage(getMessage("loaded.ready"));
         if (Preferences.getInstance().startHidden) {
             showSysTrayIcon();
             if (!sysTrayIconVisible) {
                 instance.setVisible(true);
             }
         } else {
             instance.setVisible(true);
         }
         fireStartUpEvent(new StartUpEvent(!instance.isVisible()));
     }
     
     private static FrontEnd initInstance() {
         if (instance == null) {
             preInit();
             activateSkin();
             instance = new FrontEnd();
             Preferences.getInstance().init();
             instance.applyPreferences(true);
             initTools();
             initTransferrers();
             registerMainWindowComponentListeners();
         }
         return instance;
     }
     
     private static void registerMainWindowComponentListeners() {
         if (mainWindowComponentListeners != null) {
             for (ComponentListener l : mainWindowComponentListeners) {
                 instance.addComponentListener(l);
             }
         }
     }
     
     private void applyPreferences() {
         applyPreferences(false);
     }
     
     private void applyPreferences(boolean isStartingUp) {
         if (Preferences.getInstance().useSysTrayIcon) {
             showSysTrayIcon();
         } else {
             hideSysTrayIcon();
         }
         dateFormat = new SimpleDateFormat(Preferences.getInstance().preferredDateTimeFormat);
         if (instance.hotKeysBindingsChanged) {
             instance.bindHotKeys();
             instance.hotKeysBindingsChanged = false;
         }
         if (memUsageIndicatorPanel == null) {
             memUsageIndicatorPanel = instance.createStatusBarIndicatorArea(null);
             instance.getJPanelIndicators().add(memUsageIndicatorPanel, BorderLayout.EAST);
         }
         if (Preferences.getInstance().showMemoryUsage) {
             memUsageProgressBar = new JProgressBar();
             memUsageProgressBar.setMinimum(0);
             memUsageProgressBar.setStringPainted(true);
             memUsageIndicatorPanel.add(memUsageProgressBar, BorderLayout.CENTER);
             memUsageIndicatorPanel.setVisible(true);
             startMemoryUsageMonitoring();
         } else {
             if (memUsageIndicatorPanel != null) {
                 memUsageIndicatorPanel.setVisible(false);
             }
         }
         instance.repaint(); // can be helpful when, for example, toggling antialiasing
         BackEnd.initProxySettings();
         handleAutoUpdate(isStartingUp);
         displayStatusBarMessage(getMessage("preferences.applied"));
     }
     
     private static JPanel memUsageIndicatorPanel = null;
     
     private void startMemoryUsageMonitoring() {
         execute(new Runnable() {
             public void run() {
                 while (Preferences.getInstance().showMemoryUsage) {
                     MemoryMXBean mmxb = ManagementFactory.getMemoryMXBean();
                     long bytes = mmxb.getHeapMemoryUsage().getUsed() + mmxb.getNonHeapMemoryUsage().getUsed();
                     long bytes2 = mmxb.getHeapMemoryUsage().getCommitted() + mmxb.getNonHeapMemoryUsage().getCommitted();
                     memUsageProgressBar.setMaximum((int) bytes2/1024);
                     memUsageProgressBar.setValue((int) bytes/1024);
                     memUsageProgressBar.setString(Constants.BLANK_STR + getMessage("memory.usage", "" + bytes/1024/1024, "" + bytes2/1024/1024) + Constants.BLANK_STR);
                     try {
                         Thread.sleep(5000);
                     } catch (InterruptedException e) {
                         // ignore
                     }
                 }
             }
         });
     }
     
     // TODO [P2] GUI should contain information about hot-key-bindings (for example, in tooltip-text for button with appropriate action set)
     // TODO [P3] hot-keys-bindings should be customizable
     private void bindHotKeys() {
         
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), saveAction.getValue(Action.NAME));
         getRootPane().getActionMap().put(saveAction.getValue(Action.NAME), saveAction);
         
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK), importAction.getValue(Action.NAME));
         getRootPane().getActionMap().put(importAction.getValue(Action.NAME), importAction);
         
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), exportAction.getValue(Action.NAME));
         getRootPane().getActionMap().put(exportAction.getValue(Action.NAME), exportAction);
         
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK), preferencesAction.getValue(Action.NAME));
         getRootPane().getActionMap().put(preferencesAction.getValue(Action.NAME), preferencesAction);
         
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK), manageAddOnsAction.getValue(Action.NAME));
         getRootPane().getActionMap().put(manageAddOnsAction.getValue(Action.NAME), manageAddOnsAction);
         
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK), closeAction.getValue(Action.NAME));
         getRootPane().getActionMap().put(closeAction.getValue(Action.NAME), closeAction);
         
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK + InputEvent.ALT_DOWN_MASK), exitAction.getValue(Action.NAME));
         getRootPane().getActionMap().put(exitAction.getValue(Action.NAME), exitAction);
         
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), fullScreenAction.getValue(Action.NAME));
         getRootPane().getActionMap().put(fullScreenAction.getValue(Action.NAME), fullScreenAction);
         
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK), backAction.getValue(Action.NAME));
         getRootPane().getActionMap().put(backAction.getValue(Action.NAME), backAction);
         
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK), forwardAction.getValue(Action.NAME));
         getRootPane().getActionMap().put(forwardAction.getValue(Action.NAME), forwardAction);
         
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.ALT_DOWN_MASK), backToFirstAction.getValue(Action.NAME));
         getRootPane().getActionMap().put(backToFirstAction.getValue(Action.NAME), backToFirstAction);
         
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.ALT_DOWN_MASK), forwardToLastAction.getValue(Action.NAME));
         getRootPane().getActionMap().put(forwardToLastAction.getValue(Action.NAME), forwardToLastAction);
         
     }
     
     private static boolean sysTrayIconNotSupportedInformed = false;
     private static void showSysTrayIcon() {
         if (!SystemTray.isSupported()) {
             if (sysTrayIconNotSupportedInformed == false) {
                 displayErrorMessage("System tray API is not available on this platform!");
                 sysTrayIconNotSupportedInformed = true;
             }
         } else if (!sysTrayIconVisible) {
             try {
                 // initialize tray icon
                 if (trayIcon == null) {
                     trayIcon = new TrayIcon(ICON_APP.getImage(), getMessage("app.title"));
                     trayIcon.setImageAutoSize(true);
                     trayIcon.addMouseListener(new MouseAdapter(){
                         @Override
                         public void mouseClicked(MouseEvent e) {
                             if (!Preferences.getInstance().useSysTrayIcon) {
                                 hideSysTrayIcon();
                             }
                             instance.setVisible(!instance.isVisible());
                         }
                     });
                 }
                 // add icon to system tray
                 if (SystemTray.getSystemTray().getTrayIcons().length == 0) {
                     SystemTray.getSystemTray().add(trayIcon);
                 }
                 sysTrayIconVisible = true;
             } catch (Exception ex) {
                 displayErrorMessage("Failed to initialize system tray!", ex);
             }
         }
     }
     
     private static void hideSysTrayIcon() {
         if (sysTrayIconVisible == true && trayIcon != null) {
             SystemTray.getSystemTray().remove(trayIcon);
             sysTrayIconVisible = false;
         }
     }
     
     public static void restoreMainWindow() {
         if (instance != null) {
             instance.setVisible(true);
             instance.setExtendedState(JFrame.NORMAL);
             if (!Preferences.getInstance().useSysTrayIcon) {
                 hideSysTrayIcon();
             }
             instance.requestFocusInWindow();
         }
     }
     
     private static void preInit() {
         try {
             BackEnd.getInstance().load();
             initGlobalSettings();
         } catch (GeneralSecurityException gse) {
             Splash.hideSplash();
             displayErrorMessage(
                     getMessage("error.data.load.failure") + Constants.NEW_LINE +
                     getMessage("wrong.password"), gse);
             BackEnd.getInstance().shutdown(-1);
         } catch (Throwable t) {
             Splash.hideSplash();
             displayErrorMessage(getMessage("error.data.load.failure") + (Validator.isNullOrBlank(t.getMessage()) ? Constants.EMPTY_STR : Constants.NEW_LINE.concat(t.getMessage())), t);
             BackEnd.getInstance().shutdown(-1);
         }
     }
     
     private static void initGlobalSettings() {
         config = new Properties();
         config.putAll(BackEnd.getInstance().getConfig());
     }
     
     private void applyGlobalSettings() {
         String lsid = config.getProperty(Constants.PROPERTY_LAST_SELECTED_ID);
         if (lsid != null) {
             this.switchToVisualEntry(getJTabbedPane(), UUID.fromString(lsid), new LinkedList<Component>());
         }
         
         // TODO [P2] would be nice to have window state (maximized: both/vert/horiz) restored on load
         int wpxValue;
         int wpyValue;
         int wwValue;
         int whValue;
         String wpx = config.getProperty(Constants.PROPERTY_WINDOW_COORDINATE_X);
         if (wpx == null) {
             wpxValue = getToolkit().getScreenSize().width / 4;
         } else {
             wpxValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getWidth() * Double.valueOf(wpx))));
         }
         String wpy = config.getProperty(Constants.PROPERTY_WINDOW_COORDINATE_Y);
         if (wpy == null) {
             wpyValue = getToolkit().getScreenSize().height / 4;
         } else {
             wpyValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wpy))));
         }
         String ww = config.getProperty(Constants.PROPERTY_WINDOW_WIDTH);
         if (ww == null) {
             wwValue = (getToolkit().getScreenSize().width / 4) * 2;
         } else {
             wwValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getWidth() * Double.valueOf(ww))));
         }
         String wh = config.getProperty(Constants.PROPERTY_WINDOW_HEIGHT);
         if (wh == null) {
             whValue = (getToolkit().getScreenSize().height / 4) * 2;
         } else {
             whValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wh))));
         }
 
         this.setLocation(wpxValue, wpyValue);
         this.setSize(wwValue, whValue);
     }
     
     private Runnable updateCompleteInformer = new Runnable(){
         public void run() {
             // remember last update date
             config.setProperty(Constants.PROPERTY_LAST_UPDATE_DATE, "" + System.currentTimeMillis());
             // inform user about update complete
             JOptionPane.showMessageDialog(
                     getActiveWindow(), 
                     Constants.HTML_PREFIX + 
                     getMessage("info.message.auto.update.complete") + "<br/><br/>" +
                     "<i>" +
                     getMessage("auto.update.disable.note") + "<br>" +
                     getMessage("auto.update.adjust.interval.note") + 
                     "</i>" + 
                     Constants.HTML_SUFFIX);
         }
     };
 
     private Runnable updateCommand = new Runnable(){
         public void run() {
             long delay = 300000 /* 1000 * 60 * 5 */; // 5 minutes
             if (Preferences.getInstance().autoUpdateInterval == 0 || isTimeToUpdate()) {
                 try {
                     Thread.sleep(delay);
                 } catch (InterruptedException ex) {
                     // ignore, update just won't be performed this time
                 }
                 downloadAndInstallAllUpdates(updateCompleteInformer);
             }
         }
     };
     
     private ScheduledExecutorService updateService;
     
     private void handleAutoUpdate(boolean isStartingUp) {
         if (Preferences.getInstance().enableAutoUpdate) {
             if (updateService != null) {
                 updateService.shutdownNow();
             }
             updateService = new ScheduledThreadPoolExecutor(1);                
             if (Preferences.getInstance().autoUpdateInterval == 0) {
                 if (isStartingUp) {
                     updateService.schedule(updateCommand, 0, TimeUnit.DAYS);
                 }
             } else {
                 updateService.scheduleAtFixedRate(updateCommand, 0, Preferences.getInstance().autoUpdateInterval, TimeUnit.DAYS);
             }
         }
     }
     
     private boolean isTimeToUpdate() {
         String timeStr = config.getProperty(Constants.PROPERTY_LAST_UPDATE_DATE);
         if (!Validator.isNullOrBlank(timeStr)) {
             long lastUpdateTime = Long.valueOf(timeStr);
             long currentTime = System.currentTimeMillis();
             int interval = (int) ((currentTime - lastUpdateTime) / 86400000 /* 1000 / 60 / 60 / 24 */);
             // check if specified number of days from last update date have passed
             if (interval >= Preferences.getInstance().autoUpdateInterval) {
                 return true;
             }
         } else {
             return true;
         }
         return false;
     }
     
     @SuppressWarnings("unchecked")
     private static void activateSkin() {
         String skin = config.getProperty(Constants.PROPERTY_SKIN);
         if (skin == null) {
         	skin = DEFAULT_SKIN;
         }
         try {
             String skinFullClassName = Constants.SKIN_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR + skin + Constants.PACKAGE_PATH_SEPARATOR + skin;
             Class<Skin> skinClass = (Class<Skin>) Class.forName(skinFullClassName);
             Skin skinInstance = skinClass.newInstance();
             byte[] skinSettings = BackEnd.getInstance().loadAddOnSettings(skinFullClassName, PackType.SKIN);
             skinInstance.activate(skinSettings);
             // use control icons defined by Skin if available
             if (skinInstance.getUIIcons() != null) {
                 guiIcons = skinInstance.getUIIcons();
             }
             if (activeSkin == null) {
                 activeSkin = skin;
             }
         } catch (Throwable t) {
             activeSkin = DEFAULT_SKIN;
             config.remove(Constants.PROPERTY_SKIN);
             System.err.println(
                     "Current Skin '" + skin + "' failed to initialize!" + Constants.NEW_LINE +
                     "(Preferences will be auto-modified to use default Skin)" + Constants.NEW_LINE + 
                     "Error details: " + Constants.NEW_LINE);
             t.printStackTrace(System.err);
         }
     }
     
     private boolean setActiveSkin(String skin) throws Throwable {
         boolean skinChanged = false;
         String currentSkin = config.getProperty(Constants.PROPERTY_SKIN);
         if (skin != null) {
             String skinName = skin.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
             if (!skinName.equals(currentSkin)) {
                 currentSkin = skinName;
                 config.setProperty(Constants.PROPERTY_SKIN, currentSkin);
                 configureSkin(skin);
                 skinChanged = true;
             } else {
                 skinChanged = configureSkin(skin);
             }
             if (skinChanged) {
                 if (activeSkin.equals(currentSkin)) {
                     // if skin is the same, but configuration changed,
                     // apply settings immediately
                     activateSkin();
                     SwingUtilities.updateComponentTreeUI(this);
                     if (addOnsManagementDialog != null) SwingUtilities.updateComponentTreeUI(addOnsManagementDialog);
                     skinChanged = false;
                 } else {
                     displayMessage(getMessage("info.message.restart.to.apply.changes"));
                 }
             }
         } else if (currentSkin != null) {
             config.remove(Constants.PROPERTY_SKIN);
             skinChanged = true;
         }
         return skinChanged;
     }
     
     @SuppressWarnings("unchecked")
     private boolean configureSkin(String skin) throws Throwable {
         boolean skinChanged = false;
         if (skin != null) {
             Class<Skin> skinClass = (Class<Skin>) Class.forName(skin);
             Skin skinInstance = skinClass.newInstance();
             byte[] skinSettings = BackEnd.getInstance().loadAddOnSettings(skin, PackType.SKIN);
             byte[] settings = skinInstance.configure(skinSettings);
             // store if differs from stored version
             if (!PropertiesUtils.deserializeProperties(settings).equals(PropertiesUtils.deserializeProperties(skinSettings))) {
                 BackEnd.getInstance().storeAddOnSettings(skin, PackType.SKIN, settings);
                 skinChanged = true;
             }
         }
         return skinChanged;
     }
     
     @SuppressWarnings("unchecked")
     private void configureExtension(String extension, boolean showFirstTimeUsageMessage) throws Exception {
         if (extension != null) {
             String extName = extension.replaceFirst(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
             try {
                 Class<? extends Extension> extensionClass = (Class<? extends Extension>) Class.forName(extension);
                 Extension extensionInstance = null;
                 byte[] extSettings = BackEnd.getInstance().loadAddOnSettings(extension, PackType.EXTENSION);
                 byte[] settings = null;
                 if (ToolExtension.class.isAssignableFrom(extensionClass)) {
                     extensionInstance = tools.get(extensionClass);
                     settings = ((ToolExtension) extensionInstance).configure();
                     ((ToolExtension) extensionInstance).setSettings(settings);
                 } else if (TransferExtension.class.isAssignableFrom(extensionClass)) {
                     extensionInstance = transferrers.get(extensionClass);
                     settings = ((TransferExtension) extensionInstance).configure();
                     ((TransferExtension) extensionInstance).setSettings(settings);
                 } else if (EntryExtension.class.isAssignableFrom(extensionClass)) {
                     extensionInstance = ExtensionFactory.newEntryExtension(extensionClass);
                     if (extSettings == null) {
                         extSettings = new byte[]{};
                     }
                     if (showFirstTimeUsageMessage) {
                         if (isEntryExtensionConfigurable((Class<? extends EntryExtension>) extensionClass)) {
                             displayMessage(Constants.HTML_PREFIX + getMessage("info.message.entry.extension.first.usage", extName) + Constants.HTML_SUFFIX);
                         }
                     }
                     settings = ((EntryExtension) extensionInstance).configure(extSettings);
                     if (settings == null && extSettings.length > 0) settings = extSettings;
                     ((EntryExtension) extensionInstance).setSettings(settings);
                 }
                 if (!Arrays.equals(extSettings, settings)) {
                     if (settings == null) {
                         settings = new byte[]{};
                     }
                     BackEnd.getInstance().storeAddOnSettings(extension, PackType.EXTENSION, settings);
                     if (extensionInstance instanceof ToolExtension) {
                         representTool((ToolExtension) extensionInstance);
                     }
                 }
             } catch (Throwable t) {
                 displayErrorMessage(
                         Constants.HTML_PREFIX + 
                         getMessage("error.message.extension.settings.serialization.failure", extName) + 
                         Constants.HTML_SUFFIX, t);
             }
         }
     }
     
     private boolean isEntryExtensionConfigurable(Class<? extends EntryExtension> eeClass) {
         boolean isConfigurable = false;
         try {
             eeClass.getDeclaredMethod("configure", byte[].class);
             isConfigurable = true;
         } catch (NoSuchMethodException nsme) {
             // there's no configuration method for the extension class
         }
         return isConfigurable;
     }
     
     private static Map<Class<? extends ToolExtension>, JButton> toolButtons;
 
     private void representTool(ToolExtension tool) throws Throwable {
         JPanel panel = indicatorAreas.get(tool.getClass());
         JButton toolButt = null;
         ToolRepresentation tr = tool.getRepresentation();
         boolean removeIndicator = false;
         boolean removeButton = false;
         if (tr != null) {
             toolButt = tr.getButton();
             if (toolButt != null) {
                 if (Validator.isNullOrBlank(toolButt.getToolTipText())) {
                     toolButt.setToolTipText(ExtensionFactory.getAnnotatedToolExtensions().get(tool));
                 }
                 if (toolButtons == null) {
                     toolButtons = new HashMap<Class<? extends ToolExtension>, JButton>();
                 }
                 JButton button = toolButtons.get(tool.getClass());
                 if (button != null) {
                     getJToolBar3().remove(button);
                 }
                 toolButtons.remove(tool.getClass());
                 getJToolBar3().add(toolButt);
                 toolButtons.put(tool.getClass(), toolButt);
             } else {
                 removeButton = true;
             }
             JComponent indicator = tr.getIndicator();
             if (indicator != null) {
                 if (panel == null) {
                     panel = createStatusBarIndicatorArea((Class<? extends ToolExtension>) tool.getClass());
                 }
                 panel.add(indicator);
                 getJPanelIndicators().add(panel);
             } else {
                 removeIndicator = true;
             }
         } else {
             removeIndicator = true;
             removeButton = true;
         }
         if (panel != null && removeIndicator) {
             panel.remove(1);
             getJPanelIndicators().remove(panel);
         }
         if (removeButton) {
             if (toolButtons != null) {
                 JButton button = toolButtons.get(tool.getClass());
                 if (button != null) {
                     getJToolBar3().setVisible(false);
                     getJToolBar3().remove(button);
                     getJToolBar3().setVisible(true);
                 }
                 toolButtons.remove(tool.getClass());
             }
         }
     }
     
     public static Dimension getMainWindowSize() {
         if (instance == null) return null;
         return instance.getSize();
     }
     
     public static Window getActiveWindow() {
         if (instance == null) return null;
         if (instance.addOnsManagementDialog == null) return instance;
         return instance.addOnsManagementDialog.isVisible() ? instance.addOnsManagementDialog : instance;
     }
     
     private static void initTools() {
         Map<ToolExtension, String> extensions = null;
         try {
             extensions = ExtensionFactory.getAnnotatedToolExtensions();
         } catch (Throwable t) {
             displayErrorMessage(
                     getMessage("error.message.tools.initialization.failure") + 
                     Constants.NEW_LINE + 
                     CommonUtils.getFailureDetails(t));
         }
         if (extensions != null) {
             instance.getJPanelIndicators().setVisible(false);
             instance.getJToolBar3().removeAll();
             tools = new LinkedHashMap<Class<? extends ToolExtension>, ToolExtension>();
             for (Entry<ToolExtension, String> ext : extensions.entrySet()) {
                 ToolExtension tool = ext.getKey();
                 try {
                     String fullExtName = Constants.EXTENSION_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR + tool.getClass().getSimpleName() 
                                             + Constants.PACKAGE_PATH_SEPARATOR + tool.getClass().getSimpleName();
                     tool.setSettings(BackEnd.getInstance().loadAddOnSettings(fullExtName, PackType.EXTENSION));
                     tool.setData(BackEnd.getInstance().getToolData(fullExtName));
                     instance.representTool(tool);
                     tools.put(tool.getClass(), tool);
                 } catch (Throwable t) {
                     displayErrorMessage(
                             getMessage("error.message.tool.initialization.failure", tool.getClass().getCanonicalName()) + 
                             Constants.NEW_LINE + 
                             CommonUtils.getFailureDetails(t));
                 }
             }
             instance.getJPanelIndicators().setVisible(true);
             if (instance.getJToolBar3().getComponentCount() > 0) {
                 instance.getJPanel5().setVisible(true);
             }
         }
     }
     
     private static void initTransferrers() {
         Map<String, TransferExtension> extensions = null;
         try {
             extensions = ExtensionFactory.getAnnotatedTransferExtensions();
         } catch (Throwable t) {
             displayErrorMessage(
                     getMessage("error.message.transferrers.initialization.failure") + 
                     Constants.NEW_LINE + 
                     CommonUtils.getFailureDetails(t));
         }
         if (extensions != null) {
             transferrers = new LinkedHashMap<Class<? extends TransferExtension>, TransferExtension>();
             for (Entry<String, TransferExtension> ext : extensions.entrySet()) {
                 TransferExtension transferrer = ext.getValue();
                 try {
                     String fullExtName = Constants.EXTENSION_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR + transferrer.getClass().getSimpleName() 
                                             + Constants.PACKAGE_PATH_SEPARATOR + transferrer.getClass().getSimpleName();
                     transferrer.setSettings(BackEnd.getInstance().loadAddOnSettings(fullExtName, PackType.EXTENSION));
                     transferrers.put(transferrer.getClass(), transferrer);
                 } catch (Throwable t) {
                     displayErrorMessage(
                             getMessage("error.message.transferrer.initialization.failure", transferrer.getClass().getCanonicalName()) + 
                             Constants.NEW_LINE + 
                             CommonUtils.getFailureDetails(t));
                 }
             }
         }
     }
     
     private JPanel createStatusBarIndicatorArea(Class<? extends ToolExtension> ext) {
         JPanel panel = new JPanel(new BorderLayout(5, 0));
         JSeparator separator = new JSeparator(JSeparator.VERTICAL);
         separator.setPreferredSize(new Dimension(2, 18));
         panel.add(separator, BorderLayout.WEST);
         getJPanelIndicators().add(panel);
         if (ext != null) {
             indicatorAreas.put(ext, panel);
         }
         return panel;
     }
     
     private void representData(DataCategory data) {
         int rootActiveIdx = getJTabbedPane().getSelectedIndex();
         UUID id = getSelectedVisualEntryID();
         if (data.getPlacement() != null) {
             getJTabbedPane().setTabPlacement(data.getPlacement());
         }
         tabsInitialized = false;
         recursivelyExportedEntries.clear();
         data.setId(rootID);
         representData(getJTabbedPane(), data, data.isRecursivelyExported());
         if (recursivelyExportedEntries != null && !recursivelyExportedEntries.isEmpty()) {
             for (Entry<UUID, Collection<UUID>> entry : recursivelyExportedEntries.entrySet()) {
                 JTabbedPane tabPane = entry.getKey().equals(rootID) ? getJTabbedPane() : (JTabbedPane) getComponentById(entry.getKey());
                 if (tabPane != null) {
                     for (UUID eid : getFirstLevelVisualEntriesIDs(tabPane)) {
                         if (!entry.getValue().contains(eid)) {
                             removeVisualEntryByID(tabPane, eid);
                         }
                     }
                 }
             }
         }
         tabsInitialized = true;
         if (data.getActiveIndex() != null) {
             try {
                 getJTabbedPane().setSelectedIndex(data.getActiveIndex());
             } catch (IndexOutOfBoundsException ioobe) {
                 // simply ignore incorrect index settings
             }
             currentTabPane = getJTabbedPane();
         } else if (rootActiveIdx != -1) { 
             try {
                 getJTabbedPane().setSelectedIndex(rootActiveIdx);
             } catch (IndexOutOfBoundsException ioobe) {
                 // simply ignore incorrect index settings
             }
             currentTabPane = getJTabbedPane();
         }
         if (id != null && !id.equals(getSelectedVisualEntryID())) {
             switchToVisualEntry(id, false);
         }
         initTabContent();
         handleNavigationHistory();
     }
 
     private Map<UUID, Collection<UUID>> recursivelyExportedEntries = new HashMap<UUID, Collection<UUID>>();
     private void representData(JTabbedPane tabbedPane, DataCategory data, boolean recursivelyExported) {
         try {
             Collection<UUID> reEntries = null;
             if (recursivelyExported) {
                 reEntries = new ArrayList<UUID>();
                 recursivelyExportedEntries.put(data.getId(), reEntries);
             }
             for (Recognizable item : data.getData()) {
                 if (item instanceof DataEntry) {
                     DataEntry de = (DataEntry) item;
                     String caption = de.getCaption();
                     dataEntries.put(de.getId().toString(), de);
                     putTab(tabbedPane, caption, item.getIcon(), getEntryExtensionPanel(de.getId(), null));
                     if (reEntries != null) {
                         reEntries.add(item.getId());
                     }
                 } else if (item instanceof DataCategory) {
                     String caption = item.getCaption();
                     JTabbedPane categoryTabPane = new JTabbedPane();
                     if (item.getId() != null) {
                         categoryTabPane.setName(item.getId().toString());
                     }
                     DataCategory dc = (DataCategory) item;
                     categoryTabPane.setTabPlacement(dc.getPlacement());
                     addTabPaneListeners(categoryTabPane);
                     categoryTabPane = (JTabbedPane) putTab(tabbedPane, caption, item.getIcon(), categoryTabPane);
                     currentTabPane = categoryTabPane;
                     if (reEntries != null) {
                         reEntries.add(item.getId());
                     }
                     representData(categoryTabPane, dc, (recursivelyExported || dc.isRecursivelyExported()));
                     if (dc.getActiveIndex() != null) {
                         if (categoryTabPane.getTabCount() - 1 < dc.getActiveIndex()) {
                             categoryTabPane.setSelectedIndex(Integer.valueOf(categoryTabPane.getTabCount() - 1));
                         } else {
                             categoryTabPane.setSelectedIndex(dc.getActiveIndex());
                         }
                     }
                 }
             }
         } catch (Exception ex) {
             displayErrorMessage(getMessage("error.message.data.representation.failure"), ex);
         }
     }
     
     private Component putTab(JTabbedPane tabbedPane, String caption, Icon icon, Component cmp) {
         String putId = cmp.getName();
         boolean overwrite = false;
         Component c = null;
         int i;
         for (i = 0; i < tabbedPane.getTabCount(); i++) {
             c = tabbedPane.getComponent(i);
             if (c != null) {
                 String id = c.getName();
                 if (putId.equals(id)) {
                     overwrite = true;
                     break;
                 }
             }
         }
         if (overwrite) {
             if (cmp instanceof JTabbedPane) {
                 tabbedPane.setTitleAt(i, caption);
                 tabbedPane.setIconAt(i, icon);
                 cmp = c;
             } else if (cmp instanceof JPanel) {
                 tabbedPane.removeTabAt(i);
                 tabbedPane.addTab(caption, icon, cmp);
                 TabMoveUtil.moveTab(tabbedPane, tabbedPane.getTabCount() - 1, i);
             }
         } else {
             tabbedPane.addTab(caption, icon, cmp);
         }
         return cmp;
     }
     
     private JPanel getEntryExtensionPanel(UUID entryId, EntryExtension extension) {
         JPanel p = new JPanel(new BorderLayout());
         p.setName(entryId.toString());
         if (extension != null) {
             p.add(extension, BorderLayout.CENTER);
         }
         return p;
     }
     
     private static EntryExtension initEntryExtension(String entryId) throws Throwable {
         DataEntry de = dataEntries.get(entryId.toString());
         if (de.getData() == null) {
             BackEnd.getInstance().loadDataEntryData(de);
         }
         EntryExtension extension;
         try {
             extension = ExtensionFactory.newEntryExtension(de);
         } catch (Throwable t) {
             t.printStackTrace(System.err);
             extension = new MissingExtensionInformer(de);
         }
         return extension;
     }
     
     private void store(boolean showFinalizeUI) {
     	store(showFinalizeUI, true);
     }
     
     private void emergencyStore() {
     	store(false, false);
     }
     
     private void store(final boolean showFinalizeUI, boolean fireEvents) {
         if (showFinalizeUI) finalizeUI();
         if (fireEvents) fireBeforeSaveEvent(new SaveEvent(showFinalizeUI));
         syncExecute(new Runnable(){
             public void run() { 
                 instance.displayProcessNotification(getMessage("data.saving"), false);
             }
         });
         syncExecute(new Runnable(){
             public void run() { 
                 BackEnd.getInstance().setConfig(collectProperties());
             }
         });
         syncExecute(new Runnable(){
             public void run() { 
                 try {
                     BackEnd.getInstance().setData(collectData());
                 } catch (Throwable t) {
                     displayErrorMessage(getMessage("error.message.data.collect.failure"), t);
                 }
             }
         });
         syncExecute(new Runnable(){
             public void run() { 
                 try {
                     BackEnd.getInstance().setToolsData(collectToolsData());
                 } catch (Throwable t) {
                     displayErrorMessage(getMessage("error.message.toolsdata.collect.failure"), t);
                 }
             }
         });
         syncExecute(new Runnable(){
             public void run() { 
                 try {
                     BackEnd.getInstance().store();
                 } catch (Throwable t) {
                     displayErrorMessage(getMessage("error.message.data.save.failure"), t);
                 }
             }
         });
         syncExecute(new Runnable(){
             public void run() { 
                 instance.hideProcessNotification();
             }
         });
         displayStatusBarMessage(getMessage("data.saved"));
         if (fireEvents) fireAfterSaveEvent(new SaveEvent(showFinalizeUI));
     }
     
     private Map<String, ToolData> collectToolsData() throws Throwable {
         Map<String, ToolData> toolsData = new HashMap<String, ToolData>();
         if (tools != null) {
             for (ToolExtension tool : tools.values()) {
                 toolsData.put(tool.getClass().getName(), new ToolData(tool.getId(), tool.serializeData()));
                 BackEnd.getInstance().storeAddOnSettings(tool.getClass().getName(), PackType.EXTENSION, tool.serializeSettings());
             }
         }
         return toolsData;
     }
 
     private Properties collectProperties() {
         config.setProperty(Constants.PROPERTY_WINDOW_COORDINATE_X, 
                 Constants.EMPTY_STR + getLocation().getX() / getToolkit().getScreenSize().getWidth());
         config.setProperty(Constants.PROPERTY_WINDOW_COORDINATE_Y, 
                 Constants.EMPTY_STR + getLocation().getY() / getToolkit().getScreenSize().getHeight());
         config.setProperty(Constants.PROPERTY_WINDOW_WIDTH, 
                 Constants.EMPTY_STR + getSize().getWidth() / getToolkit().getScreenSize().getWidth());
         config.setProperty(Constants.PROPERTY_WINDOW_HEIGHT, 
                 Constants.EMPTY_STR + getSize().getHeight() / getToolkit().getScreenSize().getHeight());
         config.setProperty(Constants.PROPERTY_SHOW_ALL_ONLINE_PACKS, 
                 Constants.EMPTY_STR + getOnlineShowAllPackagesCheckBox().isSelected());
         config.setProperty(Constants.PROPERTY_FORCE_TEXT_ANTIALIASING_MODE, 
                 Constants.EMPTY_STR + Preferences.getInstance().forceTextAntialiasingMode);
         config.setProperty(Constants.PROPERTY_CUSTOM_TEXT_ANTIALIASING_MODE, 
                 Constants.EMPTY_STR + Preferences.getInstance().customTextAntialiasingMode);
         UUID lsid = getSelectedVisualEntryID();
         if (lsid != null) {
             config.setProperty(Constants.PROPERTY_LAST_SELECTED_ID, lsid.toString());
         }
         return config;
     }
     
     private DataCategory collectData() throws Exception {
         DataCategory data = collectData("root", getJTabbedPane());
         data.setPlacement(getJTabbedPane().getTabPlacement());
         if (getJTabbedPane().getSelectedIndex() != -1) {
             data.setActiveIndex(getJTabbedPane().getSelectedIndex());
         }
         return data;
     }
 
     private DataCategory collectData(String caption, JTabbedPane tabPane) throws Exception {
         DataCategory data = new DataCategory();
         data.setCaption(caption);
         for (int i = 0; i < tabPane.getTabCount(); i++) {
             caption = tabPane.getTitleAt(i);
             Component c = tabPane.getComponent(i);
             Icon icon = tabPane.getIconAt(i);
             if (c instanceof JTabbedPane) {
                 JTabbedPane tp = (JTabbedPane) c;
                 DataCategory dc = collectData(caption, tp);
                 if (tp.getName() != null) {
                     dc.setId(UUID.fromString(tp.getName()));
                     dc.setIcon(icon);
                     data.addDataItem(dc);
                     if (tp.getSelectedIndex() != -1) {
                         dc.setActiveIndex(tp.getSelectedIndex());
                     }
                 }
                 dc.setPlacement(tp.getTabPlacement());
             } else if (c instanceof JPanel) {
                 EntryExtension extension = null;
                 JPanel p = (JPanel) c;
                 byte[] serializedData = null;
                 byte[] serializedSettings = null;
                 if (p.getComponentCount() != 0) {
                     extension = (EntryExtension) p.getComponent(0);
                     try {
                         serializedData = extension.serializeData();
                     } catch (Throwable t) {
                         displayErrorMessage(
                                 Constants.HTML_PREFIX + 
                                 getMessage("error.message.extension.data.serialization.failure", extension.getClass().getSimpleName()) +
                                 Constants.HTML_SUFFIX, t);
                     }
                     try {
                         serializedSettings = extension.serializeSettings();
                     } catch (Throwable t) {
                         displayErrorMessage(
                                 Constants.HTML_PREFIX + 
                                 getMessage("error.message.extension.settings.serialization.failure", extension.getClass().getSimpleName()) +
                                 Constants.HTML_SUFFIX, t);
                     }
                 }
                 DataEntry dataEntry;
                 if (extension != null) {
                     String type;
                     if (extension instanceof MissingExtensionInformer) {
                         type = ((MissingExtensionInformer) extension).getDataEntry().getType();
                     } else {
                         type = extension.getClass().getPackage().getName().replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
                     }
                     dataEntry = new DataEntry(extension.getId(), caption, icon, type, serializedData, serializedSettings);
                 } else {
                     dataEntry = dataEntries.get(p.getName());
                 }
                 data.addDataItem(dataEntry);
             }
         }
         return data;
     }
     
     private JPanel getFinalizingProcessPanel() {
         JPanel processPanel = new JPanel(new BorderLayout());
         JPanel p = new JPanel(new FlowLayout());
         p.add(new JLabel(
                 Constants.HTML_PREFIX +
                 Constants.HTML_COLOR_HIGHLIGHT_INFO + 
                 getMessage("running.tasks.wait") +
                 Constants.HTML_COLOR_SUFFIX +
                 Constants.HTML_SUFFIX
                 ));
         processPanel.add(p, BorderLayout.NORTH);
         processPanel.add(new JLabel(ICON_PROCESS), BorderLayout.CENTER);
         JPanel p2 = new JPanel(new FlowLayout());
         JLabel fql = new JLabel(
                 Constants.HTML_PREFIX + 
                 "<u>" + 
                 Constants.HTML_COLOR_HIGHLIGHT_WARNING + 
                 getMessage("running.tasks.force.exit") +
                 Constants.HTML_COLOR_SUFFIX + 
                 "</u>" + 
                 Constants.HTML_SUFFIX
                 );
         fql.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         fql.addMouseListener(new MouseAdapter(){
             @Override
             public void mouseClicked(MouseEvent e) {
                 singleThreadExecutor.shutdownNow();
                 BackEnd.getInstance().shutdown(0);
             }
         });
         p2.add(fql);
         processPanel.add(p2, BorderLayout.SOUTH);
         processPanel.setBorder(new LineBorder(Color.GRAY, 7, true));
         return processPanel;
     }
     
     private void finalizeUI() {
         JDialog finalizeDialog = new JDialog(getActiveWindow(), ModalityType.MODELESS);
         finalizeDialog.setUndecorated(true);
         finalizeDialog.setContentPane(getFinalizingProcessPanel());
         finalizeDialog.pack();
         finalizeDialog.setLocation(
                 getActiveWindow().getX() + (getActiveWindow().getWidth() - finalizeDialog.getWidth()) / 2, 
                 getActiveWindow().getY() + (getActiveWindow().getHeight() - finalizeDialog.getHeight()) / 2);
         finalizeDialog.setVisible(true);
     }
     
     private static ExecutorService cachedThreadPool;
     
     /**
      * This method should be used for non-critical tasks execution.
      * It does not guarantee that task main application thread will wait until this task is completed.
      * Thus, if user requests application to shutdown, all tasks executed via this method will be interrupted. 
      * 
      * @param task task to be executed
      */
     public static void execute(Runnable task) {
         // TODO [P2] process visualization for asynchronous tasks should be present as well ?...
         if (cachedThreadPool == null) {
             cachedThreadPool = Executors.newCachedThreadPool();
         }
         cachedThreadPool.execute(task);
     }
     
     private static ExecutorService singleThreadExecutor;
     
     public static void syncExecute(Runnable task) {
         syncExecute(task, null, false);
     }
     
     public static void syncExecute(Runnable task, final String message) {
         syncExecute(task, message, false);
     }
     
     /**
      * This is synchronized tasks execution method, that should be used for critical tasks,
      * which need to be completed before application shuts down by user's request,
      * as well as for ones that have to be executed one by one (single task at a time).
      * Thus, even if user does request shutdown, application will wait until all tasks
      * executed via this method are complete, or until user forces shutdown, whichever happen first.
      * All tasks are queued and executed one by one, shutdown-task is the last in the queue.
      * 
      * @param task task to be executed before application shutdown
      * @param message message to display in status bar while task is being executed
      * @param displayOnAllScreens boolean defines whether processing indicator should be displayed in all GUI screens (true) or in main window only (false)
      */
     public static void syncExecute(Runnable task, final String message, final boolean displayOnAllScreens) {
         if (singleThreadExecutor == null) {
             singleThreadExecutor = Executors.newSingleThreadExecutor();
         }
         boolean displayProcessIndicator = instance != null && !Validator.isNullOrBlank(message);
         if (displayProcessIndicator) {
             singleThreadExecutor.execute(new Runnable(){
                 public void run() {
                     instance.displayProcessNotification(message, displayOnAllScreens);
                 }
             });
         }
         singleThreadExecutor.execute(task);
         if (displayProcessIndicator) {
             singleThreadExecutor.execute(new Runnable(){
                 public void run() {
                     instance.hideProcessNotification();
                 }
             });
         }
     }
     
     private boolean shutdownFlag = false;
     private void shutdown() {
         shutdownFlag = true;
         finalizeUI();
         fireBeforeExitEvent(new ExitEvent());
         syncExecute(new Runnable(){
             public void run() {
                 BackEnd.getInstance().shutdown(0);
             }
         });
     }
     
     private void emergencyExit() {
     	if (!shutdownFlag) {
         	emergencyStore();
         	BackEnd.getInstance().emergencyShutdown();
     	}
     }
     
     private void exitWithOptionalAutoSave() {
         if (Preferences.getInstance().autoSaveOnExit) {
             store(true);
         }
         shutdown();
     }
     
     private void exit() {
         if (Downloader.getTotalActiveDownloadsCount() > 0) {
             JLabel message = new JLabel(getMessage("exit.confirmation.active.downloads.present"));
             JButton button = new JButton(getMessage("show.active.downloads"));
             button.addActionListener(new ActionListener(){
                 public void actionPerformed(ActionEvent e) {
                     addOnsManagementDialog.setVisible(true);
                     addOnsPane.setSelectedIndex(3);
                 }
             });
             int opt = JOptionPane.showConfirmDialog(
                     getActiveWindow(),
                     new Component[] {message, button},
                     getMessage("confirmation.cancel.active.downloads"), 
                     JOptionPane.YES_NO_OPTION,
                     JOptionPane.QUESTION_MESSAGE);
             if (opt == JOptionPane.NO_OPTION) {
                 return;
             }
         }
         if (!Preferences.getInstance().displayConfirmationDialogs) {
             exitWithOptionalAutoSave();
         } else {
             Component[] cs = null;
             JLabel l = new JLabel();
             StringBuffer caption = new StringBuffer();
             if (!Preferences.getInstance().autoSaveOnExit) {
                 JLabel note = new JLabel(
                         Constants.HTML_PREFIX + 
                         "<br/><br/>" + 
                         "<i>(" + getMessage("confirmation.disable.note") + " '" + getMessage("display.confirmation.dialogs.preference.title") + "' / '" + getMessage("auto.save.on.exit.preference.title") + "')</i>" + 
                         Constants.HTML_SUFFIX); 
                 caption.append(getMessage("confirmation.exit.unsaved.changes.message") + Constants.BLANK_STR);
                 JButton b = new JButton(getMessage("save.changes.before.exit"));
                 b.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         store(true);
                         shutdown();
                     }
                 });
                 cs = new Component[]{l,b,note};
             } else {
                 cs = new Component[]{l};
             }
             caption.append(getMessage("confirmation.exit"));
             l.setText(caption.toString());
             if (JOptionPane.showConfirmDialog(FrontEnd.this, 
                     cs,
                     getMessage("exit.confirmation"),
                     JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                 exitWithOptionalAutoSave();
             }
         }
     }
 
     public static UUID getSelectedVisualEntryID() {
         if (instance != null) {
             return instance.getSelectedVisualEntryID(instance.getJTabbedPane());
         }
         return null;
     }
 
     private UUID getSelectedVisualEntryID(JTabbedPane tabPane) {
         if (tabPane.getTabCount() > 0) {
             if (tabPane.getSelectedIndex() != -1) {
                 Component c = tabPane.getSelectedComponent();
                 if (c instanceof JTabbedPane) {
                     return getSelectedVisualEntryID((JTabbedPane) c);
                 } else if (c instanceof JPanel) {
                     return UUID.fromString(((JPanel) c).getName());
                 }
             } else {
                 String idStr = tabPane.getName();
                 if (idStr != null) {
                     return UUID.fromString(idStr);
                 } else {
                     return null;
                 }
             }
         }
         return tabPane.getName() == null ? null : UUID.fromString(tabPane.getName());
     }
 
     public static EntryExtension getSelectedExtensionEntry() {
         if (instance != null) {
             return instance.getSelectedExtensionEntry(instance.getJTabbedPane());
         }
         return null;
     }
 
     private EntryExtension getSelectedExtensionEntry(JTabbedPane tabPane) {
         if (tabPane.getTabCount() > 0) {
             if (tabPane.getSelectedIndex() != -1) {
                 Component c = tabPane.getSelectedComponent();
                 if (c instanceof JTabbedPane) {
                     return getSelectedExtensionEntry((JTabbedPane) c);
                 } else if (c instanceof JPanel) {
                     return (EntryExtension) ((JPanel) c).getComponent(0);
                 }
             }
         }
         return null;
     }
 
     public static String getSelectedVisualEntryCaption() {
         if (instance != null) {
             return instance.getSelectedVisualEntryCaption(instance.getJTabbedPane());
         }
         return null;
     }
 
     private String getSelectedVisualEntryCaption(JTabbedPane tabPane) {
         if (tabPane.getTabCount() > 0) {
             if (tabPane.getSelectedIndex() != -1) {
                 Component c = tabPane.getSelectedComponent();
                 if (c instanceof JTabbedPane) {
                     return getSelectedVisualEntryCaption((JTabbedPane) c);
                 } else if (c instanceof JPanel) {
                     return tabPane.getTitleAt(tabPane.getSelectedIndex());
                 }
             }
         }
         return null;
     }
 
     // TODO [P2] optimization: this method can reuse getVisualEntryDescriptors and get keys from map returned by it
     //           alternatively, it can be optimized the same way as mentioned method (is LinkedList really needed here?)
     
     private Collection<UUID> getVisualEntriesIDs() {
         return getVisualEntriesIDs(getJTabbedPane());
     }
 
     private Collection<UUID> getVisualEntriesIDs(JTabbedPane rootTabPane) {
         Collection<UUID> ids = new LinkedList<UUID>();
         String idStr = rootTabPane.getName();
         if (idStr != null) {
             ids.add(UUID.fromString(idStr));
         }
         for (Component c : rootTabPane.getComponents()) {
             if (c instanceof JTabbedPane) {
                 ids.addAll(getVisualEntriesIDs((JTabbedPane) c));
             } else if (c instanceof JPanel) {
                 JPanel p = (JPanel) c;
                 if (p.getName() != null) {
                     ids.add(UUID.fromString(p.getName()));
                 }
             }
         }
         return ids;
     }
 
     private Collection<UUID> getFirstLevelVisualEntriesIDs(JTabbedPane rootTabPane) {
         Collection<UUID> ids = new LinkedList<UUID>();
         for (Component c : rootTabPane.getComponents()) {
             if (c.getName() != null) {
                 ids.add(UUID.fromString(c.getName()));
             }
         }
         return ids;
     }
 
     // TODO [P2] optimization: do not iterate over all tabs (to get full extensions list) each time, some caching would be nice
     
     public static Map<UUID, EntryExtension> getEntryExtensions() throws Throwable {
         if (instance != null) {
             entryExtensions.clear();
             return instance.getEntryExtensions(instance.getJTabbedPane(), null);
         }
         return null;
     }
 
     public static Map<UUID, EntryExtension> getEntryExtensions(Class<? extends EntryExtension> filterClass) throws Throwable {
         if (instance != null) {
             entryExtensions.clear();
             return instance.getEntryExtensions(instance.getJTabbedPane(), filterClass);
         }
         return null;
     }
 
     private Map<UUID, EntryExtension> getEntryExtensions(JTabbedPane tabPane, Class<? extends EntryExtension> filterClass) throws Throwable {
         for (int i = 0; i < tabPane.getTabCount(); i++) {
             Component c = tabPane.getComponent(i);
             if (c instanceof JTabbedPane) {
                 entryExtensions.putAll(getEntryExtensions((JTabbedPane) c, filterClass));
             } else if (c instanceof JPanel) {
                 UUID id = UUID.fromString(c.getName());
                 JPanel p = ((JPanel) c);
                 EntryExtension ext;
                 if (p.getComponentCount() == 0) {
                     ext = initEntryExtension(id.toString());
                     p.add(ext);
                 } else {
                     ext = (EntryExtension) p.getComponent(0);
                 }
                 if (filterClass == null) {
                     entryExtensions.put(id, ext);
                 } else if (ext.getClass().getName().equals(filterClass.getName())) {
                     entryExtensions.put(id, ext);
                 }
             }
         }
         return entryExtensions;
     }
 
     // TODO [P2] optimization: visual entries map should be cached until collection of categories/entries is not changed
     
     public static Map<UUID, VisualEntryDescriptor> getVisualEntryDescriptors() {
         if (instance != null) {
             return instance.getVisualEntryDescriptors(instance.getJTabbedPane(), null, new LinkedList<Recognizable>());
         }
         return null;
     }
 
     public static Map<UUID, VisualEntryDescriptor> getVisualEntryDescriptors(Class<? extends EntryExtension> filterClass) {
         if (instance != null) {
             return instance.getVisualEntryDescriptors(instance.getJTabbedPane(), filterClass, new LinkedList<Recognizable>());
         }
         return null;
     }
 
     private Map<UUID, VisualEntryDescriptor> getVisualEntryDescriptors(JTabbedPane tabPane, Class<? extends EntryExtension> filterClass, LinkedList<Recognizable> entryPath) {
         Map<UUID, VisualEntryDescriptor> entries = new LinkedHashMap<UUID, VisualEntryDescriptor>();
         for (int i = 0; i < tabPane.getTabCount(); i++) {
             Component c = tabPane.getComponent(i);
             String caption = tabPane.getTitleAt(i);
             Icon icon = tabPane.getIconAt(i);
             if (c instanceof JTabbedPane) {
                 UUID id = UUID.fromString(c.getName());
                 Recognizable entry = new Recognizable(id, caption, icon);
                 entryPath.addLast(entry);
                 entries.put(id, new VisualEntryDescriptor(entry, new LinkedList<Recognizable>(entryPath), ENTRY_TYPE.CATEGORY));
                 entries.putAll(getVisualEntryDescriptors((JTabbedPane) c, filterClass, new LinkedList<Recognizable>(entryPath)));
                 entryPath.removeLast();
             } else if (c instanceof JPanel) {
                 UUID id = UUID.fromString(c.getName());
                 Recognizable entry = new Recognizable(id, caption, icon);
                 entryPath.addLast(entry);
                 if (filterClass == null) {
                     entries.put(id, new VisualEntryDescriptor(entry, new LinkedList<Recognizable>(entryPath), ENTRY_TYPE.ENTRY));
                 } else {
                     JPanel p = ((JPanel) c);
                     try {
                         initEntryPanelContent(p);
                     } catch (Throwable t) {
                         // skip broken entries, just print error message
                         t.printStackTrace(System.err);
                     }
                     if (p.getComponent(0).getClass().getName().equals(filterClass.getName())) {
                         entries.put(id, new VisualEntryDescriptor(entry, new LinkedList<Recognizable>(entryPath), ENTRY_TYPE.ENTRY));
                     }
                 }
                 entryPath.removeLast();
             }
         }
         return entries;
     }
 
     // TODO [P2] optimization: categories map should be cached until collection of categories is not changed
     
     public static Map<UUID, VisualEntryDescriptor> getCategoryDescriptors() {
         if (instance != null) {
             return instance.getCategoryDescriptors(instance.getJTabbedPane(), new LinkedList<Recognizable>());
         }
         return null;
     }
 
     private Map<UUID, VisualEntryDescriptor> getCategoryDescriptors(JTabbedPane tabPane, LinkedList<Recognizable> entryPath) {
         Map<UUID, VisualEntryDescriptor> entries = new LinkedHashMap<UUID, VisualEntryDescriptor>();
         for (int i = 0; i < tabPane.getTabCount(); i++) {
             Component c = tabPane.getComponent(i);
             String caption = tabPane.getTitleAt(i);
             Icon icon = tabPane.getIconAt(i);
             if (c instanceof JTabbedPane) {
                 UUID id = UUID.fromString(c.getName());
                 Recognizable entry = new Recognizable(id, caption, icon);
                 entryPath.addLast(entry);
                 entries.put(id, new VisualEntryDescriptor(entry, new LinkedList<Recognizable>(entryPath), ENTRY_TYPE.CATEGORY));
                 entries.putAll(getCategoryDescriptors((JTabbedPane) c, new LinkedList<Recognizable>(entryPath)));
                 entryPath.removeLast();
             }
         }
         return entries;
     }
 
     public static boolean switchToVisualEntry(UUID id) {
         return switchToVisualEntry(id, true);
     }
 
     private static boolean switchToVisualEntry(UUID id, boolean addToNavigationHistory) {
         if (instance != null) {
             navigating = true;
             boolean switched = instance.switchToVisualEntry(instance.getJTabbedPane(), id, new LinkedList<Component>());
             navigating = false;
             if (addToNavigationHistory) {
                 instance.handleNavigationHistory();
             }
             instance.handleNavigationActionsStates();
             return switched;
         }
         return false;
     }
 
     private boolean switchToVisualEntry(JTabbedPane rootTabPane, UUID id, LinkedList<Component> path) {
         String idStr = rootTabPane.getName();
         if (idStr != null && UUID.fromString(idStr).equals(id)) {
             switchToVisualEntry(getJTabbedPane(), path.iterator());
             return true;
         }
         for (Component c : rootTabPane.getComponents()) {
             path.addLast(c);
             if (c instanceof JTabbedPane) {
                 JTabbedPane tabPane = (JTabbedPane) c;
                 if (switchToVisualEntry(tabPane, id, path)) {
                     return true;
                 } else {
                     path.removeLast();
                 }
             } else if (c instanceof JPanel) {
                 JPanel p = (JPanel) c;
                 if (p.getName().equals(id.toString())) {
                     switchToVisualEntry(getJTabbedPane(), path.iterator());
                     return true;
                 } else {
                     path.removeLast();
                 }
             }
         }
         return false;
     }
 
     private void switchToVisualEntry(JTabbedPane tabPane, Iterator<Component> pathIterator) {
         if (pathIterator.hasNext()) {
             Component selComp = pathIterator.next();
             tabPane.setSelectedComponent(selComp);
             if (selComp instanceof JTabbedPane) {
                 switchToVisualEntry((JTabbedPane) selComp, pathIterator);
                 currentTabPane = (JTabbedPane) selComp;
             }
         }
     }
     
     private boolean removeVisualEntryByID(JTabbedPane tabPane, UUID id) {
         for (Component c : tabPane.getComponents()) {
             String idStr = c.getName();
             if (idStr != null && UUID.fromString(idStr).equals(id)) {
                 tabPane.remove(c);
                 return true;
             }
         }
         return false;
     }
 
     public static JTabbedPane getActiveTabPane() {
         if (instance != null) {
             return instance.getActiveTabPane(instance.getJTabbedPane());
         }
         return null;
     }
     
     private JTabbedPane getActiveTabPane(JTabbedPane rootTabPane) {
         if (rootTabPane.getTabCount() > 0) {
             if (rootTabPane.getSelectedIndex() != -1) {
                 Component c = rootTabPane.getSelectedComponent();
                 if (c instanceof JTabbedPane) {
                     return getActiveTabPane((JTabbedPane) c);
                 } else {
                     return rootTabPane;
                 }
             } else {
                 return rootTabPane;
             }
         }
         return rootTabPane;
     }
     
     private Component getComponentById(UUID id) {
         return getComponentById(getJTabbedPane(), id);
     }
     
     private Component getComponentById(JTabbedPane rootTabPane, UUID id) {
         if (rootTabPane.getTabCount() > 0) {
             for (Component c : rootTabPane.getComponents()) {
                 if (!Validator.isNullOrBlank(c.getName()) && UUID.fromString(c.getName()).equals(id)) {
                     return c;
                 } else {
                     if (c instanceof JTabbedPane) {
                         Component cmp = getComponentById((JTabbedPane) c, id);
                         if (cmp != null) {
                             return cmp;
                         }
                     }
                 }
             }
         }
         return null;
     }
     
     /**
      * This method initializes bottomPanelCloseButton
      * 
      * @return javax.swing.JButton
      */
     public JButton getBottomPanelCloseButton() {
         if (bottomPanelCloseButton == null) {
             bottomPanelCloseButton  = new JButton(new AbstractAction(){
                 private static final long serialVersionUID = 1L;
                 public void actionPerformed(ActionEvent e) {
                     getJPanel2().setVisible(false);
                 }
             });
             bottomPanelCloseButton .setIcon(ICON_CLOSE);
             bottomPanelCloseButton .setPreferredSize(new Dimension(18, 18));
         }
         return bottomPanelCloseButton;
     }
 
     public static void displayBottomPanel(JLabel title, JPanel content) {
         if (instance != null) {
             int dl = instance.getJPanel2().isVisible() ? instance.getJSplitPane().getDividerLocation() : instance.getHeight()/5*3;
             instance.getJPanel2().setVisible(false);
             instance.getJPanel2().removeAll();
             instance.getJPanel3().setVisible(false);
             instance.getJPanel3().removeAll();
             instance.getJPanel3().add(title, BorderLayout.CENTER);
             instance.getJPanel3().add(instance.getBottomPanelCloseButton(), BorderLayout.EAST);
             instance.getJPanel3().setVisible(true);
             instance.getJPanel2().add(instance.getJPanel3(), BorderLayout.NORTH);
             instance.getJPanel2().add(new JScrollPane(content), BorderLayout.CENTER);
             instance.getJPanel2().setVisible(true);
             instance.getJSplitPane().setDividerLocation(dl);
         }
     }
     
     public static void hideBottomPanel() {
         if (instance != null) {
             instance.getJPanel2().setVisible(false);
         }
     }
     
     /**
      * This method initializes sideBarCloseButton
      * 
      * @return javax.swing.JButton
      */
     public JButton getSideBarCloseButton() {
         if (sideBarCloseButton == null) {
             sideBarCloseButton  = new JButton(new AbstractAction(){
                 private static final long serialVersionUID = 1L;
                 public void actionPerformed(ActionEvent e) {
                     getJPanel16().setVisible(false);
                 }
             });
             sideBarCloseButton .setIcon(ICON_CLOSE);
             sideBarCloseButton .setPreferredSize(new Dimension(18, 18));
         }
         return sideBarCloseButton;
     }
 
     public static void displaySideBar(JLabel title, JPanel content) {
         if (instance != null) {
             int dl = instance.getJPanel16().isVisible() ? instance.getJSplitPane1().getDividerLocation() : instance.getWidth()/5*4;
             instance.getJPanel16().setVisible(false);
             instance.getJPanel16().removeAll();
             instance.getJPanel15().setVisible(false);
             instance.getJPanel15().removeAll();
             instance.getJPanel15().add(title, BorderLayout.CENTER);
             instance.getJPanel15().add(instance.getSideBarCloseButton(), BorderLayout.EAST);
             instance.getJPanel15().setVisible(true);
             instance.getJPanel16().add(instance.getJPanel15(), BorderLayout.NORTH);
             instance.getJPanel16().add(new JScrollPane(content), BorderLayout.CENTER);
             instance.getJPanel16().setVisible(true);
             instance.getJSplitPane1().setDividerLocation(dl);
         }
     }
     
     public static void hideSideBar() {
         if (instance != null) {
             instance.getJPanel16().setVisible(false);
         }
     }
     
     public static void displayErrorMessage(Throwable t) {
         Splash.hideSplash();
         t.printStackTrace(System.err);
         JOptionPane.showMessageDialog(getActiveWindow(), CommonUtils.getFailureDetails(t), getMessage("error"), JOptionPane.ERROR_MESSAGE);
     }
 
     public static void displayErrorMessage(String message, Throwable t) {
         Splash.hideSplash();
         t.printStackTrace(System.err);
         JOptionPane.showMessageDialog(getActiveWindow(), message, getMessage("error"), JOptionPane.ERROR_MESSAGE);
     }
 
     public static void displayErrorMessage(String message) {
         Splash.hideSplash();
         JOptionPane.showMessageDialog(getActiveWindow(), message, getMessage("error"), JOptionPane.ERROR_MESSAGE);
     }
     
     public static void displayMessage(String message) {
         JOptionPane.showMessageDialog(getActiveWindow(), message, getMessage("information"), JOptionPane.INFORMATION_MESSAGE);
     }
 
     private void addTabPaneListeners(JTabbedPane tabPane) {
         tabPane.addMouseListener(tabClickListener);
         tabPane.addChangeListener(tabChangeListener);
         tabPane.addMouseListener(tabMoveListener);
         tabPane.addMouseMotionListener(tabMoveListener);
     }
 
     private MouseListener tabClickListener = new MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
             currentTabPane = (JTabbedPane) e.getSource();
             if (currentTabPane.getSelectedIndex() != -1) {
                 if (currentTabPane.getSelectedComponent() instanceof JTabbedPane) {
                     currentTabPane = (JTabbedPane) currentTabPane.getSelectedComponent();
                 }
             }
             if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                 JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
                 int index = tabbedPane.getSelectedIndex();
                 String caption = tabbedPane.getTitleAt(index);
                 
                 JLabel icLabel = new JLabel(getMessage("icon"));
                 ImageIcon ic = (ImageIcon) tabbedPane.getIconAt(tabbedPane.getSelectedIndex());
                 IconChooserComboBox iconChooser = new IconChooserComboBox(ic.getDescription());
                 JLabel cLabel = new JLabel(getMessage("caption"));
                 
                 caption = JOptionPane.showInputDialog(
                         FrontEnd.this, 
                         new Component[] { icLabel, iconChooser, cLabel },
                         caption);
                 if (caption != null) {
                 	tabbedPane.setTitleAt(index, caption);
                     ImageIcon icon = iconChooser.getSelectedIcon();
                     if (icon != null) {
                     	tabbedPane.setIconAt(tabbedPane.getSelectedIndex(), icon);
                     }
                 }
             }
         }
     };
 
     private ChangeListener tabChangeListener = new ChangeListener() {
         public void stateChanged(ChangeEvent e) {
             currentTabPane = getActiveTabPane((JTabbedPane) e.getSource());
             initTabContent();
             handleNavigationHistory();
         }
     };
     
     private void handleNavigationHistory() {
         if (tabsInitialized && currentTabPane != null) {
             Component c = currentTabPane.getSelectedComponent();
             if (c == null) {
                 c = currentTabPane;
             }
             if (c.getName() != null) {
                 UUID id = UUID.fromString(c.getName());
                 if (navigationHistory.isEmpty() || (!navigating && !navigationHistory.get(navigationHistoryIndex).equals(id))) {
                     while (!navigationHistory.isEmpty() && (navigationHistory.size() > navigationHistoryIndex + 1)) {
                         navigationHistory.pop();
                     }
                     navigationHistory.push(id);
                     navigationHistoryIndex++;
                 }
             }
             handleNavigationActionsStates();
         }
     }
     
     private void handleNavigationActionsStates() {
         if (!navigationHistory.isEmpty()) {
             if (navigationHistory.size() == 1) {
                 backToFirstAction.setEnabled(false);
                 backAction.setEnabled(false);
                 forwardAction.setEnabled(false);
                 forwardToLastAction.setEnabled(false);
             } else {
                 if (navigationHistoryIndex == 0) {
                     backToFirstAction.setEnabled(false);
                     backAction.setEnabled(false);
                 } else {
                     backToFirstAction.setEnabled(true);
                     backAction.setEnabled(true);
                 }
                 if (navigationHistoryIndex == navigationHistory.size() - 1) {
                     forwardAction.setEnabled(false);
                     forwardToLastAction.setEnabled(false);
                 } else {
                     forwardAction.setEnabled(true);
                     forwardToLastAction.setEnabled(true);
                 }
             }
         }
     }
     
     private void initTabContent() {
         if (tabsInitialized && currentTabPane != null && currentTabPane.getSelectedIndex() != -1) {
             Component c = currentTabPane.getSelectedComponent();
             if (c instanceof JPanel) {
                 try {
                     final JPanel p = ((JPanel) c);
                     initEntryPanelContent(p);
                 } catch (Throwable t) {
                     displayErrorMessage(getMessage("error.message.entryextension.initialization.failure"), t);
                 }
             } else if (c instanceof JTabbedPane) {
                 currentTabPane = (JTabbedPane) c;
                 initTabContent();
             }
         }
     }
     
     private void initEntryPanelContent(JPanel p) throws Throwable {
         if (p.getComponentCount() == 0) {
             final String id = p.getName();
             EntryExtension ee = initEntryExtension(id);
             p.add(ee, BorderLayout.CENTER);
         }
     }
     
     private void autoscrollList(final JList list) {
         SwingUtilities.invokeLater(new Runnable(){
             public void run() {
                 list.ensureIndexIsVisible(list.getModel().getSize() - 1);
             }
         });
     }
     
     private static String getMessage(String key, String...vars) {
         String modifiedMsg = MESSAGES.get(key);
         for (String var : vars) {
             modifiedMsg = modifiedMsg.replaceFirst("\\$", var);
         }
         return modifiedMsg;
     }
     
     private JList getStatusBarMessagesList() {
         if (statusBarMessagesList == null) {
             statusBarMessagesList = new JList(new DefaultListModel());
         }
         return statusBarMessagesList;
     }
     
     private static ExecutorService statusBarExecutor;
     
     private static Executor getStatusBarExecutor() {
         if (statusBarExecutor == null) {
             statusBarExecutor = Executors.newSingleThreadExecutor();
         }
         return statusBarExecutor;
     }
     
     public static void displayStatusBarErrorMessage(String message) {
         displayStatusBarMessage(message, true);
     }
     
     public static void displayStatusBarMessage(String message) {
         displayStatusBarMessage(message, false);
     }
     
     private static void displayStatusBarMessage(final String message, final boolean isError) {
         getStatusBarExecutor().execute(new Runnable(){
             public void run() {
                 if (instance != null) {
                     final String timestamp = dateFormat.format(new Date()) + " # ";
                     String iaText = Constants.BLANK_STR + timestamp;
                     for (int i = 0; i < iaText.length(); i++) {
                         instance.getJLabelStatusBarMsg().setText(iaText.substring(0, i));
                         try {
                             Thread.sleep(25);
                         } catch (InterruptedException e) {
                             // ignore
                         }
                     }
                     String msg = Constants.HTML_PREFIX + "&nbsp;" + timestamp + (isError ? Constants.HTML_COLOR_HIGHLIGHT_ERROR : Constants.HTML_COLOR_HIGHLIGHT_OK) + message + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX;
                     instance.getJLabelStatusBarMsg().setText(msg);
                     try {
                         Thread.sleep(250);
                     } catch (InterruptedException e) {
                         // ignore
                     }
                     ((DefaultListModel) instance.getStatusBarMessagesList().getModel()).addElement(msg);
                     if (instance.getJPanel2().isVisible()) {
                         instance.autoscrollList(instance.getStatusBarMessagesList());
                     }
                 }
             }
         });
     }
 
     private JPanel progressBarPanel;
     private JLabel progressLabel;
     
     private void displayStatusBarProgressBar(final String message) {
         hideStatusBarProgressBar(false);
         if (progressBarPanel == null) {
             progressBarPanel = new JPanel(new BorderLayout());
             progressLabel = new JLabel();
             progressBarPanel.add(progressLabel, BorderLayout.WEST);
             progressBarPanel.add(getStatusBarProgressBar(), BorderLayout.CENTER);
             getJPanelStatusBar().add(progressBarPanel, BorderLayout.SOUTH);
         }
         progressLabel.setText(Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_INFO + "&nbsp;" + message + "&nbsp;" + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
         getStatusBarProgressBar().setMinimum(0);
         getStatusBarProgressBar().setValue(0);
         getStatusBarProgressBar().setString(Constants.EMPTY_STR);
         progressBarPanel.setVisible(true);
         shortPause();
     }
     
     private void hideStatusBarProgressBar() {
         hideStatusBarProgressBar(true);
     }
     
     private void hideStatusBarProgressBar(boolean pause) {
         if (progressBarPanel != null) {
             if (pause) shortPause();
             progressBarPanel.setVisible(false);
         }
     }
 
     private void displayProcessNotification(String message, boolean displayOnAllScreens) {
         message = Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_INFO + "&nbsp;" + message + "&nbsp;" + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX;
         processLabel.setText(message);
         getJPanelProcessIndicators().setVisible(true);
         if (displayOnAllScreens) {
             getAddOnsManagementScreenProcessPanel().setVisible(true);
             getAddOnsManagementScreenProcessLabel().setText(message);
         }
     }    
     
     private void hideProcessNotification() {
         processLabel.setText(null);
         getJPanelProcessIndicators().setVisible(false);
         if (addOnsManagementScreenProcessPanel != null) {
             getAddOnsManagementScreenProcessPanel().setVisible(false);
             getAddOnsManagementScreenProcessLabel().setText(null);
         }
     }
     
     /**
      * This method is helpful if there's a need to ensure some GUI related action
      * was visible at least some noticeable amount of time,
      * because some GUI related actions happen too quickly to be noticed by user.
      */
     private void shortPause() {
         try {
             // short pause 
             Thread.sleep(1000); 
         } catch (InterruptedException e) {
             // ignore, there just won't be any pause (not critical at all) 
         }
     }
     
     /**
      * This method initializes jContentPane
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getJContentPane() {
         if (jContentPane == null) {
             jContentPane = new JPanel();
             jContentPane.setLayout(new BorderLayout());
             jContentPane.add(getJPanel(), BorderLayout.NORTH);
             jContentPane.add(getJPanel4(), BorderLayout.CENTER);
             jContentPane.add(getJPanelStatusBar(), BorderLayout.SOUTH);
         }
         return jContentPane;
     }
     
     /**
      * This method initializes jPanelStatusBar
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getJPanelStatusBar() {
         if (jPanelStatusBar == null) {
             jPanelStatusBar = new JPanel();
             jPanelStatusBar.setLayout(new BorderLayout());
             JPanel alwaysVisiblePanel = new JPanel(new BorderLayout());
             alwaysVisiblePanel.add(getJLabelStatusBarMsg(), BorderLayout.CENTER);
             alwaysVisiblePanel.add(getJPanelIndicators(), BorderLayout.EAST);
             jPanelStatusBar.add(alwaysVisiblePanel, BorderLayout.NORTH);
             jPanelStatusBar.add(getJPanelProcessIndicators(), BorderLayout.CENTER);
         }
         return jPanelStatusBar;
     }
 
     /**
      * This method initializes jPanelIndicators
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getJPanelIndicators() {
         if (jPanelIndicators == null) {
             jPanelIndicators = new JPanel(new FlowLayout());
             jPanelIndicators.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
         }
         return jPanelIndicators;
     }
 
     /**
      * This method initializes jPanelProcessIndicators
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getJPanelProcessIndicators() {
         if (jPanelProcessIndicators == null) {
             jPanelProcessIndicators = new JPanel(new BorderLayout());
             jPanelProcessIndicators.add((processLabel = new JLabel()), BorderLayout.CENTER);
             jPanelProcessIndicators.add(new JLabel(ICON_PROCESS), BorderLayout.EAST);
             jPanelProcessIndicators.setVisible(false);
         }
         return jPanelProcessIndicators;
     }
     
     /**
      * This method initializes addOnsManagementScreenProcessLabel
      * 
      * @return javax.swing.JLabel
      */
     private JLabel getAddOnsManagementScreenProcessLabel() {
         if (addOnsManagementScreenProcessLabel == null) {
             addOnsManagementScreenProcessLabel = new JLabel();
         }
         return addOnsManagementScreenProcessLabel;
     }
 
     /**
      * This method initializes addOnsManagementScreenProcessPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getAddOnsManagementScreenProcessPanel() {
         if (addOnsManagementScreenProcessPanel == null) {
             addOnsManagementScreenProcessPanel = new JPanel(new BorderLayout());
             addOnsManagementScreenProcessPanel.add(getAddOnsManagementScreenProcessLabel(), BorderLayout.CENTER);
             addOnsManagementScreenProcessPanel.add(new JLabel(ICON_PROCESS), BorderLayout.WEST);
             addOnsManagementScreenProcessPanel.setVisible(false);
         }
         return addOnsManagementScreenProcessPanel;
     }
     
     /**
      * This method initializes jLabelStatusBarMsg
      * 
      * @return javax.swing.JLabel
      */
     private JLabel getJLabelStatusBarMsg() {
         if (jLabelStatusBarMsg == null) {
             jLabelStatusBarMsg = new JLabel();
             final JLabel title = new JLabel(getMessage("messages.history"));
             final JPanel panel = new JPanel(new BorderLayout());
             panel.add(getStatusBarMessagesList(), BorderLayout.CENTER);
             jLabelStatusBarMsg.addMouseListener(new MouseAdapter(){
                 @Override
                 public void mouseClicked(MouseEvent e) {
                     displayBottomPanel(title, panel);
                     autoscrollList(getStatusBarMessagesList());
                 }
             });
         }
         return jLabelStatusBarMsg;
     }
 
     /**
      * This method initializes onlineShowAllPackagesCB
      * 
      * @return javax.swing.JCheckBox
      */
     private JCheckBox getOnlineShowAllPackagesCheckBox() {
         if (onlineShowAllPackagesCB == null) {
             onlineShowAllPackagesCB = new JCheckBox(getMessage("show.all.packages"));
             String showAll = config.getProperty(Constants.PROPERTY_SHOW_ALL_ONLINE_PACKS);
             onlineShowAllPackagesCB.setSelected(!Validator.isNullOrBlank(showAll) && Boolean.valueOf(showAll));
         }
         return onlineShowAllPackagesCB;
     }
 
     /**
      * This method initializes jTabbedPane
      * 
      * @return javax.swing.JTabbedPane
      */
     private JTabbedPane getJTabbedPane() {
         if (jTabbedPane == null) {
             jTabbedPane = new JTabbedPane();
             jTabbedPane.setBackground(null);
             jTabbedPane.setTabPlacement(JTabbedPane.LEFT);
             addTabPaneListeners(jTabbedPane);
         }
         return jTabbedPane;
     }
 
     /**
      * This method initializes jToolBar
      * 
      * @return javax.swing.JToolBar
      */
     private JToolBar getJToolBar() {
         if (jToolBar == null) {
             jToolBar = new JToolBar();
             jToolBar.setFloatable(false);
             jToolBar.add(getJButton7());
             jToolBar.add(getJButton2());
             jToolBar.add(getJButton12());
             jToolBar.addSeparator();
             jToolBar.add(getJButton4());
             jToolBar.add(getJButton17());
             jToolBar.add(getJButton5());
             jToolBar.add(getJButton19());
             jToolBar.add(getJButton18());
             jToolBar.add(getJButton1());
             jToolBar.addSeparator();
             jToolBar.add(getJButton11());
             jToolBar.addSeparator();
             jToolBar.add(getJButton13());
             jToolBar.add(getJButton14());
             jToolBar.add(getJButton15());
             jToolBar.add(getJButton16());
         }
         return jToolBar;
     }
 
     /**
      * This method initializes jToolBar3
      * 
      * @return javax.swing.JToolBar
      */
     private JToolBar getJToolBar3() {
         if (jToolBar3 == null) {
             jToolBar3 = new JToolBar(JToolBar.VERTICAL);
             jToolBar3.setFloatable(false);
         }
         return jToolBar3;
     }
 
     /**
      * This method initializes jButton5
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton5() {
         if (jButton5 == null) {
             jButton5 = new JButton(addEntryAction);
             jButton5.setText(Constants.EMPTY_STR);
         }
         return jButton5;
     }
 
     /**
      * This method initializes jButton17
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton17() {
         if (jButton17 == null) {
             jButton17 = new JButton(configCategoryAction);
             jButton17.setText(Constants.EMPTY_STR);
         }
         return jButton17;
     }
 
     /**
      * This method initializes jButton18
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton18() {
         if (jButton18 == null) {
             jButton18 = new JButton(relocateEntryOrCategoryAction);
             jButton18.setText(Constants.EMPTY_STR);
         }
         return jButton18;
     }
 
     /**
      * This method initializes jButton19
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton19() {
         if (jButton19 == null) {
             jButton19 = new JButton(configEntryAction);
             jButton19.setText(Constants.EMPTY_STR);
         }
         return jButton19;
     }
 
     /**
      * This method initializes jButton11
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton11() {
         if (jButton11 == null) {
             jButton11 = new JButton(changePasswordAction);
             jButton11.setText(Constants.EMPTY_STR);
         }
         return jButton11;
     }
 
     /**
      * This method initializes jButton13
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton13() {
         if (jButton13 == null) {
             jButton13 = new JButton(backToFirstAction);
             jButton13.setText(Constants.EMPTY_STR);
         }
         return jButton13;
     }
 
     /**
      * This method initializes jButton14
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton14() {
         if (jButton14 == null) {
             jButton14 = new JButton(backAction);
             jButton14.setText(Constants.EMPTY_STR);
         }
         return jButton14;
     }
 
     /**
      * This method initializes jButton11
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton15() {
         if (jButton15 == null) {
             jButton15 = new JButton(forwardAction);
             jButton15.setText(Constants.EMPTY_STR);
         }
         return jButton15;
     }
 
     /**
      * This method initializes jButton16
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton16() {
         if (jButton16 == null) {
             jButton16 = new JButton(forwardToLastAction);
             jButton16.setText(Constants.EMPTY_STR);
         }
         return jButton16;
     }
 
     /**
      * This method initializes jButton1
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton1() {
         if (jButton1 == null) {
             jButton1 = new JButton(deleteEntryOrCategoryAction);
             jButton1.setText(Constants.EMPTY_STR);
         }
         return jButton1;
     }
 
     /**
      * This method initializes jButton2
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton2() {
         if (jButton2 == null) {
             jButton2 = new JButton(importAction);
             jButton2.setText(Constants.EMPTY_STR);
         }
         return jButton2;
     }
 
     /**
      * This method initializes jButton12
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton12() {
         if (jButton12 == null) {
             jButton12 = new JButton(exportAction);
             jButton12.setText(Constants.EMPTY_STR);
         }
         return jButton12;
     }
 
     /**
      * This method initializes jPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getJPanel() {
         if (jPanel == null) {
             jPanel = new JPanel();
             jPanel.setLayout(new BorderLayout());
             jPanel.add(getJToolBar(), BorderLayout.CENTER);
             jPanel.add(getJToolBar2(), BorderLayout.EAST);
         }
         return jPanel;
     }
 
     /**
      * This method initializes jPanel5
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getJPanel5() {
         if (jPanel5 == null) {
             jPanel5 = new JPanel();
             jPanel5.setVisible(false);
             jPanel5.setLayout(new BorderLayout());
             jPanel5.add(getJToolBar3(), BorderLayout.WEST);
         }
         return jPanel5;
     }
 
     /**
      * This method initializes jPanel15
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getJPanel15() {
         if (jPanel15 == null) {
             jPanel15 = new JPanel();
             jPanel15.setVisible(false);
             jPanel15.setLayout(new BorderLayout());
         }
         return jPanel15;
     }
 
     /**
      * This method initializes jPanel16
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getJPanel16() {
         if (jPanel16 == null) {
             jPanel16 = new JPanel();
             jPanel16.setVisible(false);
             jPanel16.setLayout(new BorderLayout());
         }
         return jPanel16;
     }
 
     /**
      * This method initializes jPanel4
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getJPanel4() {
         if (jPanel4 == null) {
             jPanel4 = new JPanel();
             jPanel4.setLayout(new BorderLayout());
             jPanel4.add(getJPanel5(), BorderLayout.WEST);
             jPanel4.add(getJSplitPane1(), BorderLayout.CENTER);
         }
         return jPanel4;
     }
 
     /**
      * This method initializes jSplitPane
      * 
      * @return javax.swing.JSplitPane
      */
     private JSplitPane getJSplitPane() {
         if (jSplitPane == null) {
             jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
             jSplitPane.setDividerSize(3);
             jSplitPane.setTopComponent(getJTabbedPane());
             jSplitPane.setBottomComponent(getJPanel2());
         }
         return jSplitPane;
     }
 
     /**
      * This method initializes jSplitPane1
      * 
      * @return javax.swing.JSplitPane
      */
     private JSplitPane getJSplitPane1() {
         if (jSplitPane1 == null) {
             jSplitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
             jSplitPane1.setDividerSize(3);
             jSplitPane1.setLeftComponent(getJSplitPane());
             jSplitPane1.setRightComponent(getJPanel16());
         }
         return jSplitPane1;
     }
 
     /**
      * This method initializes jPanel2
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getJPanel2() {
         if (jPanel2 == null) {
             jPanel2 = new JPanel();
             jPanel2.setVisible(false);
             jPanel2.setLayout(new BorderLayout());
         }
         return jPanel2;
     }
 
     /**
      * This method initializes jPanel3
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getJPanel3() {
         if (jPanel3 == null) {
             jPanel3 = new JPanel();
             jPanel3.setLayout(new BorderLayout());
         }
         return jPanel3;
     }
 
     /**
      * This method initializes jToolBar2
      * 
      * @return javax.swing.JToolBar
      */
     private JToolBar getJToolBar2() {
         if (jToolBar2 == null) {
             jToolBar2 = new JToolBar();
             jToolBar2.setFloatable(false);
             jToolBar2.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
             jToolBar2.add(getJButton10());
             jToolBar2.add(getJButton6());
             jToolBar2.add(getJButton3());
             jToolBar2.add(getJButton8());
             jToolBar2.add(getJButton9());
         }
         return jToolBar2;
     }
 
     /**
      * This method initializes jButton3
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton3() {
         if (jButton3 == null) {
             jButton3 = new JButton(displayHelpAction);
             jButton3.setText(Constants.EMPTY_STR);
         }
         return jButton3;
     }
 
     /**
      * This method initializes jButton6
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton6() {
         if (jButton6 == null) {
             jButton6 = new JButton(displayAboutInfoAction);
             jButton6.setText(Constants.EMPTY_STR);
         }
         return jButton6;
     }
 
     /**
      * This method initializes jButton7
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton7() {
         if (jButton7 == null) {
             jButton7 = new JButton(saveAction);
             jButton7.setText(Constants.EMPTY_STR);
         }
         return jButton7;
     }
 
     /**
      * This method initializes jButton8
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton8() {
         if (jButton8 == null) {
             jButton8 = new JButton(manageAddOnsAction);
             jButton8.setText(Constants.EMPTY_STR);
         }
         return jButton8;
     }
 
     /**
      * This method initializes jButton9
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton9() {
         if (jButton9 == null) {
             jButton9 = new JButton(preferencesAction);
             jButton9.setText(Constants.EMPTY_STR);
         }
         return jButton9;
     }
 
     /**
      * This method initializes jButton10
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButton10() {
         if (jButton10 == null) {
             jButton10 = new JButton(exitAction);
             jButton10.setText(Constants.EMPTY_STR);
         }
         return jButton10;
     }
 
     private JButton getJButton4() {
         if (jButton4 == null) {
             jButton4 = new JButton(addCategoryAction);
             jButton4.setText(Constants.EMPTY_STR);
         }
         return jButton4;
     }
 
     private boolean confirmedDelete() {
         return confirmed(getMessage("confirmation.delete.title"), getMessage("confirmation.delete.message"));
     }
     
     private boolean confirmedUninstall() {
         return confirmed(getMessage("confirmation.uninstall.title"), getMessage("confirmation.uninstall.message"));
     }
     
     private boolean confirmed(String title, String message) {
         if (Preferences.getInstance().displayConfirmationDialogs) {
             int opt = JOptionPane.showConfirmDialog(
                     getActiveWindow(), 
                     Constants.HTML_PREFIX + message + "<br/><br/>" +
                     		"<i>(" + getMessage("confirmation.disable.note") + " '" + getMessage("display.confirmation.dialogs.preference.title") + "')</i>" + Constants.HTML_SUFFIX, 
                     title, 
                     JOptionPane.YES_NO_OPTION);
             return opt == JOptionPane.YES_OPTION;
         }
         return true;
     }
     
     private boolean autoConfirmed(String title, String message) {
         if (!Preferences.getInstance().autoMode) {
             int opt = JOptionPane.showConfirmDialog(
                     getActiveWindow(), 
                     Constants.HTML_PREFIX + message + "<br/><br/>" +
                             "<i>(" + getMessage("confirmation.disable.note") + " '" + getMessage("auto.mode.preference.title") + "')</i>" + Constants.HTML_SUFFIX, 
                     title, 
                     JOptionPane.YES_NO_OPTION);
             return opt == JOptionPane.YES_OPTION;
         }
         return true;
     }
     
     private boolean defineRootPlacement() {
         boolean result = false;
         Placement placement = (Placement) JOptionPane.showInputDialog(FrontEnd.this, getMessage("choose.placement"),
                 getMessage("choose.placement"), JOptionPane.QUESTION_MESSAGE, null, PLACEMENTS, PLACEMENTS[0]);
         if (placement != null) {
             getJTabbedPane().setTabPlacement(placement.getInteger());
             result = true;
         }
         return result;
     }
 
     private ChangePasswordAction changePasswordAction = new ChangePasswordAction();
     private class ChangePasswordAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public ChangePasswordAction() {
             putValue(Action.NAME, "changePassword");
             putValue(Action.SHORT_DESCRIPTION, getMessage("change.password"));
             putValue(Action.SMALL_ICON, guiIcons.getIconChangePassword());
         }
         
         public void actionPerformed(ActionEvent evt) {
             JLabel currPassLabel = new JLabel(getMessage("current.password"));
             final JPasswordField currPassField = new JPasswordField();
             JLabel newPassLabel = new JLabel(getMessage("new.password"));
             final JPasswordField newPassField = new JPasswordField();
             JLabel newPassConfirmLabel = new JLabel(getMessage("password.confirmation"));
             final JPasswordField newPassConfirmField = new JPasswordField();
             ActionListener al = new ActionListener(){
                 public void actionPerformed(ActionEvent ae){
                     currPassField.requestFocusInWindow();
                 }
             };
             Timer timer = new Timer(500,al);
             timer.setRepeats(false);
             timer.start();
             if (JOptionPane.showConfirmDialog(
                     null, 
                     new Component[]{
                             currPassLabel, currPassField,
                             newPassLabel, newPassField,
                             newPassConfirmLabel, newPassConfirmField
                             }, 
                             getMessage("change.password"), 
                     JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                 String currPass = new String(currPassField.getPassword());            
                 String newPass = new String(newPassField.getPassword()); 
                 String newPassConfirmation = new String(newPassConfirmField.getPassword()); 
                 if (!newPass.equals(newPassConfirmation)) {
                     displayErrorMessage(Constants.HTML_PREFIX + getMessage("password.confirmation.failure") + Constants.HTML_SUFFIX);
                 } else {
                     try {
                         BackEnd.setPassword(currPass, newPass);
                         displayMessage(getMessage("change.password.success"));
                         displayStatusBarMessage(getMessage("password.changed"));
                     } catch (Exception ex) {
                         displayErrorMessage(Constants.HTML_PREFIX + getMessage("change.password.failure") + "<br/>" + ex.getMessage() + Constants.HTML_SUFFIX, ex);
                     }
                 }
             }
         }
     };
 
     private AddEntryAction addEntryAction = new AddEntryAction();
     private class AddEntryAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public AddEntryAction() {
             putValue(Action.NAME, "addEntry");
             putValue(Action.SHORT_DESCRIPTION, getMessage("add.entry"));
             putValue(Action.SMALL_ICON, guiIcons.getIconEntry());
         }
         
         public void actionPerformed(ActionEvent evt) {
             try {
                 Map<String, Class<? extends EntryExtension>> extensions = ExtensionFactory.getAnnotatedEntryExtensionClasses();
                 if (extensions.isEmpty()) {
                     displayMessage(
                             Constants.HTML_PREFIX + getMessage("no.entry.extensions.installed") + Constants.HTML_SUFFIX);
                 } else {
                     if (getJTabbedPane().getTabCount() == 0) {
                         if (!defineRootPlacement()) {
                             return;
                         }
                     }
                     JCheckBox addToRootCB = null;
                     if (getJTabbedPane().getTabCount() == 0 || getJTabbedPane().getSelectedIndex() == -1) {
                         currentTabPane = getJTabbedPane();
                     } else {
                         addToRootCB = new JCheckBox(getMessage("add.entry.to.rootlevel"));
                     }
                     JLabel entryTypeLabel = new JLabel(getMessage("type"));
                     JComboBox entryTypeComboBox = new JComboBox();
                     for (String entryType : extensions.keySet()) {
                         entryTypeComboBox.addItem(entryType);
                     }
                     if (lastAddedEntryType != null) {
                         entryTypeComboBox.setSelectedItem(lastAddedEntryType);
                     }
                     entryTypeComboBox.setEditable(false);
                     JLabel icLabel = new JLabel(getMessage("icon"));
                     IconChooserComboBox iconChooser = new IconChooserComboBox();
                     JLabel cLabel = new JLabel(getMessage("caption"));
                     String caption = JOptionPane.showInputDialog(
                             FrontEnd.this, 
                             new Component[] { addToRootCB, entryTypeLabel, entryTypeComboBox, icLabel, iconChooser, cLabel },
                             getMessage("add.entry"), 
                             JOptionPane.QUESTION_MESSAGE);
                     if (caption != null) {
                         String typeDescription = (String) entryTypeComboBox.getSelectedItem();
                         lastAddedEntryType = typeDescription;
                         Class<? extends EntryExtension> type = extensions.get(typeDescription);
                         byte[] defSettings = BackEnd.getInstance().loadAddOnSettings(type.getName(), PackType.EXTENSION);
                         if (defSettings == null) {
                             // extension's first time usage
                             configureExtension(type.getName(), true);
                         }
                         EntryExtension extension = ExtensionFactory.newEntryExtension(type);
                         if (extension != null) {
                             JPanel p = getEntryExtensionPanel(extension.getId(), extension);
                             JTabbedPane tabPane = (addToRootCB != null && addToRootCB.isSelected()) || currentTabPane == null ? getJTabbedPane() : currentTabPane;
                             tabPane.addTab(caption, p);
                             tabPane.setSelectedComponent(p);
                             ImageIcon icon = iconChooser.getSelectedIcon();
                             if (icon != null) {
                                 tabPane.setIconAt(currentTabPane.getSelectedIndex(), icon);
                             }
                             displayStatusBarMessage(getMessage("entry.added", caption));
                         }
                     }
                 }
             } catch (Throwable t) {
                 displayErrorMessage(Constants.HTML_PREFIX + getMessage("add.entry.failure") + Constants.HTML_SUFFIX, t);
             }
         }
     };
 
     private ConfigCategoryAction configCategoryAction = new ConfigCategoryAction();
     private class ConfigCategoryAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public ConfigCategoryAction() {
             putValue(Action.NAME, "configCategory");
             putValue(Action.SHORT_DESCRIPTION, getMessage("configure.category"));
             putValue(Action.SMALL_ICON, guiIcons.getIconConfigureCategory());
         }
         
         public void actionPerformed(ActionEvent evt) {
             if (currentTabPane != null) {
                 try {
                     JLabel pLabel = new JLabel(getMessage("active.category.tabs.placement"));
                     JLabel rpLabel = new JLabel(getMessage("root.category.tabs.placement"));
                     JComboBox placementsChooser = new JComboBox();
                     JComboBox rootPlacementsChooser = new JComboBox();
                     for (Placement placement : PLACEMENTS) {
                         placementsChooser.addItem(placement);
                         rootPlacementsChooser.addItem(placement);
                     }
                     for (int i = 0; i < placementsChooser.getItemCount(); i++) {
                         if (((Placement) placementsChooser.getItemAt(i)).getInteger().equals(currentTabPane.getTabPlacement())) {
                             placementsChooser.setSelectedIndex(i);
                             break;
                         }
                     }
                     for (int i = 0; i < rootPlacementsChooser.getItemCount(); i++) {
                         if (((Placement) rootPlacementsChooser.getItemAt(i)).getInteger().equals(getJTabbedPane().getTabPlacement())) {
                             rootPlacementsChooser.setSelectedIndex(i);
                             break;
                         }
                     }
                     int opt = JOptionPane.showConfirmDialog(
                             FrontEnd.this, 
                             new Component[]{ pLabel, placementsChooser, rpLabel, rootPlacementsChooser }, 
                             getMessage("configure.category"), 
                             JOptionPane.OK_CANCEL_OPTION);
                     if (opt == JOptionPane.OK_OPTION) {
                         currentTabPane.setTabPlacement(((Placement) placementsChooser.getSelectedItem()).getInteger());
                         getJTabbedPane().setTabPlacement(((Placement) rootPlacementsChooser.getSelectedItem()).getInteger());
                     }
                 } catch (Exception ex) {
                     displayErrorMessage(getMessage("category.configuration.error"), ex);
                 }
             }
         }
     };
     
     private ConfigEntryAction configEntryAction = new ConfigEntryAction();
     private class ConfigEntryAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public ConfigEntryAction() {
             putValue(Action.NAME, "configEntry");
             putValue(Action.SHORT_DESCRIPTION, getMessage("configure.entry"));
             putValue(Action.SMALL_ICON, guiIcons.getIconConfigureEntry());
         }
         
         public void actionPerformed(ActionEvent evt) {
             try {
                 EntryExtension ext = getSelectedExtensionEntry();
                 if (ext != null) {
                     if (!isEntryExtensionConfigurable(ext.getClass())) {
                         displayMessage(Constants.HTML_PREFIX + 
                                        getMessage("info.message.entry.not.configurable") + "<br/>" +
                                        "<i>" + getMessage("info.message.entry.not.configurable.details") + "</i>" +
                                        Constants.HTML_SUFFIX);
                         return;
                     }
                     DataEntry de = new DataEntry();
                     de.setId(ext.getId());
                     de.setType(ext.getClass().getSimpleName());
                     BackEnd.getInstance().loadDataEntrySettings(de);
                     // check if runtime configuration differs from stored one...
                     byte[] runtimeSettings = ext.serializeSettings();
                     if (!Arrays.equals(de.getSettings(), runtimeSettings)) {
                         // ... and if yes, store it before proceeding further
                         de.setSettings(runtimeSettings);
                         BackEnd.getInstance().storeDataEntrySettings(de);
                     }
                     byte[] oldSettings = de.getSettings();
                     byte[] newSettings = ext.configure(oldSettings);
                     if (newSettings != null) {
                         ext.applySettings(newSettings);
                         de.setSettings(newSettings);
                         BackEnd.getInstance().storeDataEntrySettings(de);
                     }
                 }
             } catch (Throwable t) {
                 displayErrorMessage(getMessage("entry.configuration.error") + Constants.BLANK_STR + CommonUtils.getFailureDetails(t), t);
             }
         }
     };
 
     private RelocateEntryOrCategoryAction relocateEntryOrCategoryAction = new RelocateEntryOrCategoryAction();
     private class RelocateEntryOrCategoryAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public RelocateEntryOrCategoryAction() {
             putValue(Action.NAME, "relocateEntryOrCategory");
             putValue(Action.SHORT_DESCRIPTION, getMessage("relocate.entry.or.category"));
             putValue(Action.SMALL_ICON, guiIcons.getIconRelocate());
         }
         
         public void actionPerformed(ActionEvent evt) {
             try {
                 UUID activeItemId = getSelectedVisualEntryID();
                 JLabel sourceL = new JLabel(getMessage("relocated.item"));
                 JComboBox sourceCB = new JComboBox();
                 Map<UUID, VisualEntryDescriptor> veds = getVisualEntryDescriptors();
                 for (VisualEntryDescriptor veDescriptor : veds.values()) {
                     sourceCB.addItem(veDescriptor);
                     if (activeItemId != null && veDescriptor.getEntry().getId().equals(activeItemId)) {
                         sourceCB.setSelectedItem(veDescriptor);
                     }
                 }
 
                 JLabel targetL = new JLabel(getMessage("target.category"));
                 final JComboBox targetCB = new JComboBox();
                 VisualEntryDescriptor ved = (VisualEntryDescriptor) sourceCB.getSelectedItem();
                 populateRelocationTargetComboBox(targetCB, ved.getEntry().getId(), ved.getEntryType());
                 
                 sourceCB.addItemListener(new ItemListener(){
                     public void itemStateChanged(ItemEvent e) {
                         if (ItemEvent.SELECTED == e.getStateChange()) {
                             VisualEntryDescriptor ved = (VisualEntryDescriptor) e.getItem();
                             populateRelocationTargetComboBox(targetCB, ved.getEntry().getId(), ved.getEntryType());
                         }
                     }
                 });
 
                 int opt = JOptionPane.showConfirmDialog(
                         FrontEnd.this, 
                         new Component[]{ sourceL, sourceCB, targetL, targetCB }, 
                         getMessage("relocate.entry.or.category"), 
                         JOptionPane.OK_CANCEL_OPTION);
                 if (opt == JOptionPane.OK_OPTION) {
                     JTabbedPane dstTabPane;
                     if (Validator.isNullOrBlank(targetCB.getSelectedItem())) {
                         dstTabPane = getJTabbedPane();
                     } else {
                         dstTabPane = (JTabbedPane) getComponentById(((VisualEntryDescriptor) targetCB.getSelectedItem()).getEntry().getId());
                     }
                     Component cmp = getComponentById(((VisualEntryDescriptor) sourceCB.getSelectedItem()).getEntry().getId());
                     JTabbedPane srcTabPane = (JTabbedPane) cmp.getParent();
                     for (int i = 0; i < srcTabPane.getTabCount(); i++) {
                         if (srcTabPane.getComponent(i).equals(cmp)) {
                             TabMoveUtil.moveTab(srcTabPane, i, dstTabPane);
                             break;
                         }
                     }
                 }
 
             } catch (Exception ex) {
                 displayErrorMessage(getMessage("relocate.failed"), ex);
             }
         }
     };
     
     private void populateRelocationTargetComboBox(JComboBox targetCB, UUID sourceComponentId, ENTRY_TYPE type) {
         Component cmp = getComponentById(sourceComponentId);
         Collection<UUID> skipCategoriesIds = new ArrayList<UUID>();
         if (ENTRY_TYPE.CATEGORY == type) {
             skipCategoriesIds.addAll(getVisualEntriesIDs((JTabbedPane) cmp));
         }
         if (cmp.getParent().getName() != null) {
             targetCB.addItem(Constants.EMPTY_STR);
             skipCategoriesIds.add(UUID.fromString(cmp.getParent().getName()));
         }
         Map<UUID, VisualEntryDescriptor> veds = getCategoryDescriptors();
         for (VisualEntryDescriptor veDescriptor : veds.values()) {
             if (!skipCategoriesIds.contains(veDescriptor.getEntry().getId())) {
                 targetCB.addItem(veDescriptor);
             }
         }
     }
 
     private DeleteAction deleteEntryOrCategoryAction = new DeleteAction();
     private class DeleteAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public DeleteAction() {
             putValue(Action.NAME, "delete");
             putValue(Action.SHORT_DESCRIPTION, getMessage("delete.active.entry.or.category"));
             putValue(Action.SMALL_ICON, guiIcons.getIconDelete());
         }
         
         public void actionPerformed(ActionEvent evt) {
             if (getJTabbedPane().getTabCount() > 0) {
                 try {
                     if (currentTabPane.getTabCount() > 0) {
                         if (confirmedDelete()) {
                             String caption = currentTabPane.getTitleAt(currentTabPane.getSelectedIndex());
                             boolean isCategory = currentTabPane.getSelectedComponent() instanceof JTabbedPane;
                             currentTabPane.remove(currentTabPane.getSelectedIndex());
                             displayStatusBarMessage(getMessage(isCategory ? "category.deleted" : "entry.deleted", caption));
                             currentTabPane = getActiveTabPane(currentTabPane);
                         }
                     } else {
                         JTabbedPane parentTabPane = (JTabbedPane) currentTabPane.getParent();
                         if (parentTabPane != null) {
                             if (confirmedDelete()) {
                                 String caption = parentTabPane.getTitleAt(parentTabPane.getSelectedIndex());
                                 parentTabPane.remove(currentTabPane);
                                 displayStatusBarMessage(getMessage("category.deleted", caption));
                                 currentTabPane = getActiveTabPane(parentTabPane);
                             }
                         }
                     }
                 } catch (Exception ex) {
                     displayErrorMessage(getMessage("delete.active.entry.or.category.failure"), ex);
                 }
             }
         }
     };
     
     private void createDependentCheckboxChangeListener(final JCheckBox main, final JCheckBox dependent) {
         dependent.setEnabled(main.isEnabled() && main.isSelected());
         main.addChangeListener(new ChangeListener(){
             public void stateChanged(ChangeEvent e) {
                 dependent.setEnabled(main.isEnabled() && main.isSelected());
             }
         });
         main.addPropertyChangeListener("enabled", new PropertyChangeListener(){
             public void propertyChange(PropertyChangeEvent evt) {
                 dependent.setEnabled(main.isEnabled() && main.isEnabled());
             }
         });
     }
     
     private ImportAction importAction = new ImportAction();
     private class ImportAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public ImportAction() {
             putValue(Action.NAME, "import");
             putValue(Action.SHORT_DESCRIPTION, getMessage("data.import").concat(" {Ctrl+I}"));
             putValue(Action.SMALL_ICON, guiIcons.getIconImport());
         }
         
         public void actionPerformed(ActionEvent evt) {
             try {
                 final JComboBox configsCB = new JComboBox();
                 configsCB.addItem(Constants.EMPTY_STR);
                 for (String configName : BackEnd.getInstance().getPopulatedImportConfigurations().keySet()) {
                     configsCB.addItem(configName);
                 }
                 final JButton delButt = new JButton(getMessage("delete"));
                 delButt.setEnabled(false);
                 delButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         try {
                             String name = (String) configsCB.getSelectedItem();
                             BackEnd.getInstance().removeImportConfiguration(name);
                             configsCB.removeItem(name);
                         } catch (Exception ex) {
                             displayErrorMessage(getMessage("transfer.configuration.delete.failure"), ex);
                         }
                     }
                 });
                 final JButton renButt = new JButton(getMessage("rename"));
                 renButt.setEnabled(false);
                 renButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         try {
                             String oldName = (String) configsCB.getSelectedItem();
                             String newName = JOptionPane.showInputDialog(FrontEnd.this, getMessage("new.name"), oldName);
                             if (!Validator.isNullOrBlank(newName)) {
                                 BackEnd.getInstance().renameImportConfiguration(oldName, newName);
                                 configsCB.removeItem(oldName);
                                 configsCB.addItem(newName);
                                 configsCB.setSelectedItem(newName);
                             }
                         } catch (Exception ex) {
                             displayErrorMessage(getMessage("transfer.configuration.rename.failure"), ex);
                         }
                     }
                 });
                 configsCB.addItemListener(new ItemListener(){
                     public void itemStateChanged(ItemEvent e) {
                         if (!Constants.EMPTY_STR.equals(configsCB.getSelectedItem())) {
                             delButt.setEnabled(true);
                             renButt.setEnabled(true);
                         } else {
                             delButt.setEnabled(false);
                             renButt.setEnabled(false);
                         }
                     }
                 });
                 final JCheckBox importUnchangedDataCB = new JCheckBox(getMessage("transfer.force"));
                 JPanel p = new JPanel(new BorderLayout());
                 p.add(configsCB, BorderLayout.NORTH);
                 JPanel pb = new JPanel(new GridLayout(1, 2));
                 pb.add(renButt);
                 pb.add(delButt);
                 p.add(pb, BorderLayout.CENTER);
                 p.add(importUnchangedDataCB, BorderLayout.SOUTH);
                 Component[] c = new Component[] {
                         new JLabel(Constants.HTML_PREFIX + getMessage("transfer.configuration.select") + Constants.HTML_SUFFIX),
                         p          
                 };
                 int opt = JOptionPane.showConfirmDialog(FrontEnd.this, c, getMessage("data.import"), JOptionPane.OK_CANCEL_OPTION);
                 if (opt == JOptionPane.OK_OPTION) {
                     if (!Validator.isNullOrBlank(configsCB.getSelectedItem())) {
                         final String configName = (String) configsCB.getSelectedItem();
                         syncExecute(new Runnable(){
                             public void run() {
                                 autoImport(configName, importUnchangedDataCB.isSelected(), true);
                             }
                         });
                     } else {
                         final JComboBox cb = new JComboBox();
                         for (String annotation : ExtensionFactory.getAnnotatedTransferExtensions().keySet()) {
                             cb.addItem(annotation);
                         }
                         opt = JOptionPane.showConfirmDialog(FrontEnd.this, cb, getMessage("transfer.type"), JOptionPane.OK_CANCEL_OPTION);
                         if (opt == JOptionPane.OK_OPTION) {
                             syncExecute(new Runnable(){
                                 public void run() {
                                     JPanel panel = new JPanel(new BorderLayout());
                                     DefaultListModel processModel = new DefaultListModel();
                                     JList processList = new JList(processModel);
                                     panel.add(processList, BorderLayout.CENTER);
                                     JLabel label = new JLabel(getMessage("data.import"));
                                     processModel.addElement(getMessage("import.data.transferring"));
                                     displayBottomPanel(label, panel);
                                     autoscrollList(processList);
                                     try {
                                         final String annotation = (String) cb.getSelectedItem();
                                         final TransferExtension transferrer = ExtensionFactory.getAnnotatedTransferExtensions().get(annotation);
                                         final byte[] transferOptions = transferrer.configure(TRANSFER_TYPE.IMPORT);
                                         if (transferOptions == null || transferOptions.length == 0) {
                                             hideBottomPanel();
                                             throw new Exception(getMessage("transfer.options.missing.error"));
                                         }
                                         byte[] metaBytes = transferrer.readData(transferOptions, true);
                                         // check if checksum of data to be imported has changed since last import (or if import is forced)...
                                         if (!importUnchangedDataCB.isSelected() && !transferrer.importCheckSumChanged(transferOptions, metaBytes)) {
                                             // ... if no, do not import and inform user about that, if in verbose mode
                                             label.setText(Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_OK + getMessage("import.completed") + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                                             processModel.addElement(getMessage("transfer.discarded.no.data.changes"));
                                             autoscrollList(processList);
                                         } else {
                                             // ... if yes, do perform import
                                             instance.displayStatusBarProgressBar(getMessage("importing.data"));
                                             if (metaBytes != null) {
                                                 Properties metaData = PropertiesUtils.deserializeProperties(metaBytes);
                                                 String sizeStr = metaData.getProperty(Constants.META_DATA_FILESIZE);
                                                 if (!Validator.isNullOrBlank(sizeStr) && transferrer instanceof ObservableTransferExtension) {
                                                     final int size = Integer.valueOf(sizeStr);
                                                     instance.getStatusBarProgressBar().setMaximum(size);
                                                     ((ObservableTransferExtension) transferrer).setListener(new TransferProgressListener(){
                                                         public void onProgress(long transferredBytesNum, long elapsedTime) {
                                                             instance.getStatusBarProgressBar().setValue((int) transferredBytesNum);
                                                             double estimationCoef = ((double) size) / ((double) transferredBytesNum);
                                                             long estimationTime = (long) (elapsedTime * estimationCoef - elapsedTime);
                                                             instance.getStatusBarProgressBar().setString( 
                                                                     FormatUtils.formatByteSize(transferredBytesNum) + " / " + FormatUtils.formatByteSize(size)
                                                                     + ", " + getMessage("elapsed.time") + ": " + FormatUtils.formatTimeDuration(elapsedTime) 
                                                                     + ", " + getMessage("estimated.time.left") + ": " + FormatUtils.formatTimeDuration(estimationTime));
                                                         }
                                                     });
                                                 }
                                             }
                                             TransferData td = transferrer.importData(transferOptions, importUnchangedDataCB.isSelected());
                                             byte[] importedData = td.getData();
                                             if (importedData == null) {
                                                 processModel.addElement(getMessage("import.failure.no.data.retrieved"));
                                                 autoscrollList(processList);
                                                 label.setText(Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_ERROR + getMessage("import.failure") + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                                             } else {
                                                 processModel.addElement(getMessage("import.data.retrieved"));
                                                 autoscrollList(processList);
                                                 String oe = getMessage("overwrite.existing");
                                                 
                                                 JPanel p1 = new JPanel(new GridLayout(5, 2));
                                                 
                                                 JCheckBox importDataEntriesCB = new JCheckBox(getMessage("import.data.entries"));
                                                 p1.add(importDataEntriesCB);
                                                 JCheckBox overwriteDataEntriesCB = new JCheckBox(oe);
                                                 p1.add(overwriteDataEntriesCB);
                                                 createDependentCheckboxChangeListener(importDataEntriesCB, overwriteDataEntriesCB);
 
                                                 JCheckBox importDataEntryConfigsCB = new JCheckBox(getMessage("import.data.entry.configs")); 
                                                 p1.add(importDataEntryConfigsCB);
                                                 JCheckBox overwriteDataEntryConfigsCB = new JCheckBox(oe); 
                                                 p1.add(overwriteDataEntryConfigsCB);
                                                 createDependentCheckboxChangeListener(importDataEntryConfigsCB, overwriteDataEntryConfigsCB);
                                                 
                                                 JCheckBox importPreferencesCB = new JCheckBox(getMessage("import.preferences"));
                                                 p1.add(importPreferencesCB);
                                                 JCheckBox overwritePreferencesCB = new JCheckBox(oe); 
                                                 p1.add(overwritePreferencesCB);
                                                 createDependentCheckboxChangeListener(importPreferencesCB, overwritePreferencesCB);
                                                 
                                                 JCheckBox importToolsDataCB = new JCheckBox(getMessage("import.tools.data")); 
                                                 p1.add(importToolsDataCB);
                                                 JCheckBox overwriteToolsDataCB = new JCheckBox(oe); 
                                                 p1.add(overwriteToolsDataCB);
                                                 createDependentCheckboxChangeListener(importToolsDataCB, overwriteToolsDataCB);
                                                 
                                                 JCheckBox importIconsCB = new JCheckBox(getMessage("import.icons"));
                                                 p1.add(importIconsCB);
                                                 JCheckBox overwriteIconsCB = new JCheckBox(oe);
                                                 p1.add(overwriteIconsCB);
                                                 createDependentCheckboxChangeListener(importIconsCB, overwriteIconsCB);
 
                                                 JCheckBox importAppCoreCB = new JCheckBox(getMessage("import.and.update.app.core"));
                                                 
                                                 JPanel p2 = new JPanel(new GridLayout(3, 2));
                                                 
                                                 JCheckBox importAddOnsAndLibsCB = new JCheckBox(getMessage("import.addons.and.libs"));
                                                 p2.add(importAddOnsAndLibsCB);
                                                 JCheckBox updateAddOnsAndLibsCB = new JCheckBox(getMessage("update.installed"));
                                                 p2.add(updateAddOnsAndLibsCB);
                                                 createDependentCheckboxChangeListener(importAddOnsAndLibsCB, updateAddOnsAndLibsCB);
                                                 
                                                 JCheckBox importAddOnConfigsCB = new JCheckBox(getMessage("import.addon.configs"));
                                                 p2.add(importAddOnConfigsCB);
                                                 JCheckBox overwriteAddOnConfigsCB = new JCheckBox(oe);
                                                 p2.add(overwriteAddOnConfigsCB);
                                                 createDependentCheckboxChangeListener(importAddOnConfigsCB, overwriteAddOnConfigsCB);
                                                 
                                                 JCheckBox importImportExportConfigsCB = new JCheckBox(getMessage("import.import.export.configs"));
                                                 p2.add(importImportExportConfigsCB);
                                                 JCheckBox overwriteImportExportConfigsCB = new JCheckBox(oe);
                                                 p2.add(overwriteImportExportConfigsCB);
                                                 createDependentCheckboxChangeListener(importImportExportConfigsCB, overwriteImportExportConfigsCB);
                                                 
                                                 JLabel passwordL = new JLabel(getMessage("import.use.password"));
                                                 JPasswordField passwordTF = new JPasswordField();
                                                 if (JOptionPane.showConfirmDialog(
                                                         FrontEnd.this,
                                                         new Component[] {
                                                                 p1,
                                                                 importAppCoreCB,
                                                                 p2,
                                                                 passwordL,
                                                                 passwordTF
                                                         },
                                                         getMessage("data.import"), 
                                                         JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                                                     hideBottomPanel();
                                                 } else {    
                                                     processModel.addElement(getMessage("import.data.extracting"));
                                                     autoscrollList(processList);
                                                     File importDir = new File(Constants.TMP_DIR, UUID.randomUUID().toString());
                                                     FSUtils.delete(importDir);
                                                     ArchUtils.extract(importedData, importDir);
                                                     processModel.addElement(getMessage("import.data.extracted"));
                                                     autoscrollList(processList);
                                                     String password = new String(passwordTF.getPassword());            
                                                     try {
                                                         ImportConfiguration importConfig = new ImportConfiguration();
                                                         importConfig.setTransferProvider(transferrer.getClass().getSimpleName());
                                                         importConfig.setImportDataEntries(importDataEntriesCB.isSelected());
                                                         importConfig.setOverwriteDataEntries(overwriteDataEntriesCB.isSelected());
                                                         importConfig.setImportDataEntryConfigs(importDataEntryConfigsCB.isSelected());
                                                         importConfig.setOverwriteDataEntryConfigs(overwriteDataEntryConfigsCB.isSelected());
                                                         importConfig.setImportPrefs(importPreferencesCB.isSelected());
                                                         importConfig.setOverwritePrefs(overwritePreferencesCB.isSelected());
                                                         importConfig.setImportToolsData(importToolsDataCB.isSelected());
                                                         importConfig.setOverwriteToolsData(overwriteToolsDataCB.isSelected());
                                                         importConfig.setImportIcons(importIconsCB.isSelected());
                                                         importConfig.setOverwriteIcons(overwriteIconsCB.isSelected());
                                                         importConfig.setImportAndUpdateAppCore(importAppCoreCB.isSelected());
                                                         importConfig.setImportAddOnsAndLibs(importAddOnsAndLibsCB.isSelected());
                                                         importConfig.setUpdateInstalledAddOnsAndLibs(updateAddOnsAndLibsCB.isSelected());
                                                         importConfig.setImportAddOnConfigs(importAddOnConfigsCB.isSelected());
                                                         importConfig.setOverwriteAddOnConfigs(overwriteAddOnConfigsCB.isSelected());
                                                         importConfig.setImportImportExportConfigs(importImportExportConfigsCB.isSelected());
                                                         importConfig.setOverwriteImportExportConfigs(overwriteImportExportConfigsCB.isSelected());
                                                         importConfig.setPassword(password);                                                        
                                                         DataCategory data = BackEnd.getInstance().importData(importDir, getVisualEntriesIDs(), importConfig);
                                                         representData(data);
                                                         if (importToolsDataCB.isSelected()) {
                                                             initTools();
                                                         }
                                                         if (importImportExportConfigsCB.isSelected()) {
                                                             initTransferrers();
                                                         }
                                                         if (importPreferencesCB.isSelected()) {
                                                             Preferences.getInstance().init();
                                                             applyPreferences();
                                                         }
                                                         if (importAddOnsAndLibsCB.isSelected()) {
                                                             listExtensions();
                                                             listSkins();
                                                             listLibs();
                                                         }
                                                         if (importIconsCB.isSelected()) {
                                                             listIcons();
                                                         }
                                                         configsCB.setEditable(true);
                                                         label.setText(Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_OK + getMessage("import.completed") + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                                                         processModel.addElement(getMessage("import.success"));
                                                         autoscrollList(processList);
                                                         StringBuffer sb = new StringBuffer(getMessage("import.done"));
                                                         Properties meta = td.getMetaData();
                                                         if (meta != null && !meta.isEmpty()) {
                                                             sb.append(" (");
                                                             String size = meta.getProperty(Constants.META_DATA_FILESIZE);
                                                             if (!Validator.isNullOrBlank(size)) {
                                                                 sb.append(FormatUtils.formatByteSize(Long.valueOf(size)));
                                                             }
                                                             String user = meta.getProperty(Constants.META_DATA_USERNAME);
                                                             if (!Validator.isNullOrBlank(user)) {
                                                                 sb.append(", ");
                                                                 sb.append(getMessage("modified.by") + Constants.BLANK_STR + user);
                                                             }
                                                             String timestamp = meta.getProperty(Constants.META_DATA_TIMESTAMP);
                                                             if (!Validator.isNullOrBlank(timestamp)) {
                                                                 sb.append(", ");
                                                                 sb.append(dateFormat.format(new Date(Long.valueOf(timestamp))));
                                                             }
                                                             sb.append(")");
                                                         }
                                                         displayStatusBarMessage(sb.toString());
                                                         fireTransferEvent(new TransferEvent(TRANSFER_TYPE.IMPORT, transferrer.getClass()));
                                                         Component[] c = new Component[] {
                                                                 new JLabel(getMessage("import.success")),
                                                                 new JLabel(Constants.HTML_PREFIX + getMessage("transfer.configuration.save") + Constants.HTML_SUFFIX),
                                                                 configsCB          
                                                         };
                                                         JOptionPane.showMessageDialog(FrontEnd.this, c);
                                                         if (!Validator.isNullOrBlank(configsCB.getSelectedItem())) {
                                                             String configName = configsCB.getSelectedItem().toString();
                                                             BackEnd.getInstance().storeImportConfigurationAndOptions(configName, importConfig, transferOptions);
                                                             processModel.addElement(getMessage("transfer.configuration.saved", configName));
                                                             autoscrollList(processList);
                                                         }
                                                     } catch (GeneralSecurityException gse) {
                                                         processModel.addElement(getMessage("import.failure"));
                                                         processModel.addElement(getMessage("error.details") + ": " + getMessage("wrong.password"));
                                                         autoscrollList(processList);
                                                         label.setText(Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_ERROR + getMessage("import.failure") + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                                                         gse.printStackTrace(System.err);
                                                     } catch (Exception ex) {
                                                         processModel.addElement(getMessage("import.failure"));
                                                         if (ex.getMessage() != null) {
                                                             processModel.addElement(getMessage("error.details") + ": " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                                                         }
                                                         autoscrollList(processList);
                                                         label.setText(Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_ERROR + getMessage("import.failure") + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                                                         ex.printStackTrace(System.err);
                                                     }
                                                 }
                                             }
                                         }    
                                     } catch (Throwable ex) {
                                         processModel.addElement(getMessage("import.failure"));
                                         if (ex.getMessage() != null) {
                                             processModel.addElement(getMessage("error.details") + ": " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                                         }
                                         autoscrollList(processList);
                                         label.setText(Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_ERROR + getMessage("import.failure") + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                                         ex.printStackTrace(System.err);
                                     } finally {
                                         instance.hideStatusBarProgressBar();
                                     }
                                 }
                             }, getMessage("importing.data") + "...");
                         }
                     }
                 }
             } catch (Throwable ex) {
                 displayErrorMessage(getMessage("import.failure") + ": " + CommonUtils.getFailureDetails(ex), ex);
             }
         }
     };
     
     public static void autoImport(final String configName, final boolean force, final boolean verbose) {
         if (instance != null) {
             JPanel panel = verbose ? new JPanel(new BorderLayout()) : null;
             JLabel processLabel = verbose ? new JLabel(getMessage("importing.data") +  " ('" + configName + "')...") : null;
             JLabel label = verbose ? new JLabel(getMessage("data.import")) : null;
             if (verbose) panel.add(processLabel, BorderLayout.CENTER);
             if (verbose) displayBottomPanel(label, panel);
             try {
                 instance.displayProcessNotification(getMessage("importing.data") + " ('" + configName + "')...", false);
                 ImportConfiguration importConfig = BackEnd.getInstance().getPopulatedImportConfigurations().get(configName);
                 final TransferExtension transferrer = ExtensionFactory.getTransferExtension(importConfig.getTransferProvider());
                 if (transferrer == null) {
                     throw new Exception(getMessage("transfer.type.no.longer.available"));
                 }
                 byte[] transferOptions = BackEnd.getInstance().getImportOptions(configName);
                 byte[] metaBytes = transferrer.readData(transferOptions, true);
                 // check if checksum of data to be imported has changed since last import (or if import is forced)...
                 if (!force && !transferrer.importCheckSumChanged(transferOptions, metaBytes)) {
                     // ... if no, do not import...
                     if (verbose) {
                         // ... and inform user about that, if in verbose mode
                         label.setText(Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_OK + getMessage("import.completed") + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                         processLabel.setText(getMessage("transfer.discarded.no.data.changes"));
                     }
                 } else {
                     // ... if yes, do perform import
                     instance.displayStatusBarProgressBar(getMessage("importing.data") + " ('" + configName + "')...");
                     if (metaBytes != null) {
                         Properties metaData = PropertiesUtils.deserializeProperties(metaBytes);
                         String sizeStr = metaData.getProperty(Constants.META_DATA_FILESIZE);
                         if (!Validator.isNullOrBlank(sizeStr) && transferrer instanceof ObservableTransferExtension) {
                             final int size = Integer.valueOf(sizeStr);
                             instance.getStatusBarProgressBar().setMaximum(size);
                             ((ObservableTransferExtension) transferrer).setListener(new TransferProgressListener(){
                                 public void onProgress(long transferredBytesNum, long elapsedTime) {
                                     instance.getStatusBarProgressBar().setValue((int) transferredBytesNum);
                                     double estimationCoef = ((double) size) / ((double) transferredBytesNum);
                                     long estimationTime = (long) (elapsedTime * estimationCoef - elapsedTime);
                                     instance.getStatusBarProgressBar().setString( 
                                             FormatUtils.formatByteSize(transferredBytesNum) + " / " + FormatUtils.formatByteSize(size)
                                             + ", " + getMessage("elapsed.time") + ": " + FormatUtils.formatTimeDuration(elapsedTime) 
                                             + ", " + getMessage("estimated.time.left") + ": " + FormatUtils.formatTimeDuration(estimationTime));
                                 }
                             });
                         }
                     }
                     TransferData td = transferrer.importData(transferOptions, force);
                     byte[] importedData = td.getData();
                     File importDir = new File(Constants.TMP_DIR, UUID.randomUUID().toString());
                     FSUtils.delete(importDir);
                     ArchUtils.extract(importedData, importDir);
                     DataCategory data = BackEnd.getInstance().importData(importDir, instance.getVisualEntriesIDs(), importConfig);
                     instance.representData(data);
                     if (importConfig.isImportToolsData()) {
                         initTools();
                     }
                     if (importConfig.isImportImportExportConfigs()) {
                         initTransferrers();
                     }
                     if (importConfig.isImportPrefs()) {
                         Preferences.getInstance().init();
                         instance.applyPreferences();
                     }
                     if (importConfig.isImportAddOnsAndLibs()) {
                         instance.listExtensions();
                         instance.listSkins();
                         instance.listLibs();
                     }
                     if (importConfig.isImportIcons()) {
                         instance.listIcons();
                     }
                     if (verbose) {
                         label.setText(Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_OK + getMessage("import.completed") + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                         processLabel.setText(getMessage("import.success"));
                     }
                     StringBuffer sb = new StringBuffer(getMessage("import.done") + " ('" + configName + "'");
                     Properties meta = td.getMetaData();
                     if (meta != null && !meta.isEmpty()) {
                         String size = meta.getProperty(Constants.META_DATA_FILESIZE);
                         if (!Validator.isNullOrBlank(size)) {
                             sb.append(", ");
                             sb.append(FormatUtils.formatByteSize(Long.valueOf(size)));
                         }
                         String user = meta.getProperty(Constants.META_DATA_USERNAME);
                         if (!Validator.isNullOrBlank(user)) {
                             sb.append(", ");
                             sb.append(getMessage("modified.by") + Constants.BLANK_STR + user);
                         }
                         String timestamp = meta.getProperty(Constants.META_DATA_TIMESTAMP);
                         if (!Validator.isNullOrBlank(timestamp)) {
                             sb.append(", ");
                             sb.append(dateFormat.format(new Date(Long.valueOf(timestamp))));
                         }
                     }
                     sb.append(")");
                     displayStatusBarMessage(sb.toString());
                     fireTransferEvent(new TransferEvent(TRANSFER_TYPE.IMPORT, transferrer.getClass(), configName));
                 }
             } catch (GeneralSecurityException gse) {
                 if (verbose) {
                     processLabel.setText(getMessage("import failed") + getMessage("error details") + ": " + getMessage("wrong.password"));
                     label.setText(Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_ERROR + getMessage("import.failure") + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                 }
                 displayStatusBarErrorMessage(getMessage("import failed") + Constants.BLANK_STR + getMessage("wrong.password"));
                 gse.printStackTrace(System.err);
             } catch (Throwable t) {
                 String errMsg = getMessage("import.failure");
                 if (t.getMessage() != null) {
                     errMsg += Constants.BLANK_STR + getMessage("error.details") + ": " + t.getClass().getSimpleName() + ": " + t.getMessage();
                 }
                 if (verbose) {
                     processLabel.setText(errMsg);
                     label.setText(Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_ERROR + getMessage("import.failure") + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                 }
                 displayStatusBarErrorMessage(errMsg);
                 t.printStackTrace(System.err);
             } finally {
                 instance.hideStatusBarProgressBar();
                 instance.hideProcessNotification();
             }
         }
     }
     
     private ExportAction exportAction = new ExportAction();
     private class ExportAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public ExportAction() {
             putValue(Action.NAME, "export");
             putValue(Action.SHORT_DESCRIPTION, getMessage("data.export").concat(" {Ctrl+E}"));
             putValue(Action.SMALL_ICON, guiIcons.getIconExport());
         }
 
         public void actionPerformed(ActionEvent e) {
             try {
                 if (!autoConfirmed(getMessage("export.save.data.title"), getMessage("export.save.data.message"))) {
                     return;
                 }
                 // force (auto) save before export
                 instance.store(false);
                 // now perform export actually
                 final JComboBox configsCB = new JComboBox();
                 configsCB.addItem(Constants.EMPTY_STR);
                 for (String configName : BackEnd.getInstance().getPopulatedExportConfigurations().keySet()) {
                     configsCB.addItem(configName);
                 }
                 final JButton delButt = new JButton(getMessage("delete"));
                 delButt.setEnabled(false);
                 delButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         try {
                             String name = (String) configsCB.getSelectedItem();
                             BackEnd.getInstance().removeExportConfiguration(name);
                             configsCB.removeItem(name);
                         } catch (Exception ex) {
                             displayErrorMessage(getMessage("transfer.configuration.delete.failure"), ex);
                         }
                     }
                 });
                 final JButton renButt = new JButton(getMessage("rename"));
                 renButt.setEnabled(false);
                 renButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         try {
                             String oldName = (String) configsCB.getSelectedItem();
                             String newName = JOptionPane.showInputDialog(FrontEnd.this, getMessage("new.name"), oldName);
                             if (!Validator.isNullOrBlank(newName)) {
                                 BackEnd.getInstance().renameExportConfiguration(oldName, newName);
                                 configsCB.removeItem(oldName);
                                 configsCB.addItem(newName);
                                 configsCB.setSelectedItem(newName);
                             }
                         } catch (Exception ex) {
                             displayErrorMessage(getMessage("transfer.configuration.rename.failure"), ex);
                         }
                     }
                 });
                 configsCB.addItemListener(new ItemListener(){
                     public void itemStateChanged(ItemEvent e) {
                         if (!Constants.EMPTY_STR.equals(configsCB.getSelectedItem())) {
                             delButt.setEnabled(true);
                             renButt.setEnabled(true);
                         } else {
                             delButt.setEnabled(false);
                             renButt.setEnabled(false);
                         }
                     }
                 });
                 final JCheckBox exportUnchangedDataCB = new JCheckBox(getMessage("transfer.force"));
                 JPanel p = new JPanel(new BorderLayout());
                 p.add(configsCB, BorderLayout.NORTH);
                 JPanel pb = new JPanel(new GridLayout(1, 2));
                 pb.add(renButt);
                 pb.add(delButt);
                 p.add(pb, BorderLayout.CENTER);
                 p.add(exportUnchangedDataCB, BorderLayout.SOUTH);
                 Component[] c = new Component[] {
                         new JLabel(Constants.HTML_PREFIX + getMessage("transfer.configuration.select") + Constants.HTML_SUFFIX),
                         p          
                 };
                 opt = JOptionPane.showConfirmDialog(FrontEnd.this, c, getMessage("data.export"), JOptionPane.OK_CANCEL_OPTION);
                 if (opt == JOptionPane.OK_OPTION) {
                     if (!Validator.isNullOrBlank(configsCB.getSelectedItem())) {
                         final String configName = (String) configsCB.getSelectedItem();
                         syncExecute(new Runnable(){
                             public void run() {
                                 autoExport(configName, exportUnchangedDataCB.isSelected(), true);
                             }
                         });
                     } else {
                         JComboBox cb = new JComboBox();
                         for (String annotation : ExtensionFactory.getAnnotatedTransferExtensions().keySet()) {
                             cb.addItem(annotation);
                         }
                         opt = JOptionPane.showConfirmDialog(FrontEnd.this, cb, getMessage("transfer.type"), JOptionPane.OK_CANCEL_OPTION);
                         if (opt != JOptionPane.OK_OPTION) {
                             hideBottomPanel();
                         } else {
                             String annotation = (String) cb.getSelectedItem();
                             final TransferExtension transferrer = ExtensionFactory.getAnnotatedTransferExtensions().get(annotation);
                             final byte[] transferOptions = transferrer.configure(TRANSFER_TYPE.IMPORT);
                             if (transferOptions == null || transferOptions.length == 0) {
                                 hideBottomPanel();
                                 throw new Exception(getMessage("transfer.options.missing.error"));
                             }
                             transferrer.checkConnection(transferOptions);
                             final DataCategory data = collectData();
                             filterData(data, BackEnd.getInstance().getStoredDataEntryIDs());
                             final Collection<UUID> selectedEntries = new LinkedList<UUID>();
                             final Collection<UUID> selectedRecursiveEntries = new LinkedList<UUID>();
                             final JTree dataTree;
                             final CheckTreeManager checkTreeManager;
                             if (!data.getData().isEmpty()) {
                                 dataTree = buildDataTree(data);
                                 checkTreeManager = new CheckTreeManager(dataTree);
                                 checkTreeManager.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener(){
                                     public void valueChanged(TreeSelectionEvent e) {
                                         TreePath selectedPath = dataTree.getSelectionPath();
                                         if (selectedPath != null) {
                                             DefaultMutableTreeNode node = ((DefaultMutableTreeNode) selectedPath.getLastPathComponent());
                                             if (dataTree.isCollapsed(selectedPath) && !node.isLeaf()) {
                                                 boolean isSelected = checkTreeManager.getSelectionModel().isPathSelected(selectedPath, true);
                                                 if (isSelected) {
                                                     Collection<DefaultMutableTreeNode> childs = new LinkedList<DefaultMutableTreeNode>();
                                                     for (int i = 0; i < node.getChildCount(); i++) {
                                                         childs.add((DefaultMutableTreeNode) node.getChildAt(i));
                                                     }
                                                     categoriesToExportRecursively.put(node, childs);
                                                     node.removeAllChildren();
                                                 }
                                             }
                                         }
                                         if (selectedPath != null) {
                                             boolean isSelected = checkTreeManager.getSelectionModel().isPathSelected(selectedPath, true);
                                             if (!isSelected) {
                                                 DefaultMutableTreeNode node = ((DefaultMutableTreeNode) selectedPath.getLastPathComponent());
                                                 if (dataTree.isCollapsed(selectedPath) && node.isLeaf()) {
                                                     Collection<DefaultMutableTreeNode> childs = categoriesToExportRecursively.get(node);
                                                     if (childs != null) {
                                                         for (DefaultMutableTreeNode child : childs) {
                                                             node.add(child);
                                                         }
                                                         categoriesToExportRecursively.remove(node);
                                                     }
                                                 }
                                             }
                                         }
                                     }
                                 });
                             } else {
                                 dataTree = null;
                                 checkTreeManager = null;
                             }
                             final JCheckBox exportPreferencesCB = new JCheckBox(getMessage("export.preferences")); 
                             final JCheckBox exportDataEntryConfigsCB = new JCheckBox(getMessage("export.data.entry.configs")); 
                             final JCheckBox exportOnlyRelatedDataEntryConfigsCB = new JCheckBox(getMessage("export.data.entry.configs.related.only")); 
                             createDependentCheckboxChangeListener(exportDataEntryConfigsCB, exportOnlyRelatedDataEntryConfigsCB);
                             final JCheckBox exportToolsDataCB = new JCheckBox(getMessage("export.tools.data")); 
                             final JCheckBox exportIconsCB = new JCheckBox(getMessage("export.icons"));
                             final JCheckBox exportOnlyRelatedIconsCB = new JCheckBox(getMessage("export.icons.related.only"));
                             createDependentCheckboxChangeListener(exportIconsCB, exportOnlyRelatedIconsCB);
                             final JCheckBox exportAppCoreCB = new JCheckBox(getMessage("export.app.core")); 
                             final JCheckBox exportAddOnsCB = new JCheckBox(getMessage("export.addons.and.libs")); 
                             final JCheckBox exportAddOnConfigsCB = new JCheckBox(getMessage("export.addon.configs"));
                             final JCheckBox exportImportExportConfigsCB = new JCheckBox(getMessage("export.import.export.configs"));
                             final JLabel passwordL1 = new JLabel(getMessage("export.use.password"));
                             final JPasswordField passwordTF1 = new JPasswordField();
                             final String cpText = getMessage("password.confirmation");
                             final JLabel passwordL2 = new JLabel(cpText);
                             final JPasswordField passwordTF2 = new JPasswordField();
                             passwordTF2.addFocusListener(new FocusListener(){
                                 public void focusGained(FocusEvent e) {
                                     if (!Arrays.equals(passwordTF1.getPassword(), passwordTF2.getPassword())) {
                                         passwordL2.setText(cpText + Constants.BLANK_STR + getMessage("password.confirmation.invalid"));
                                         passwordL2.setForeground(Color.RED);
                                     } else {
                                         passwordL2.setText(cpText + Constants.BLANK_STR + getMessage("password.confirmation.confirmed"));
                                         passwordL2.setForeground(Color.BLUE);
                                     }
                                 }
                                 public void focusLost(FocusEvent e) {}
                             });
                             passwordTF2.addCaretListener(new CaretListener(){
                                 public void caretUpdate(CaretEvent e) {
                                     if (!Arrays.equals(passwordTF1.getPassword(), passwordTF2.getPassword())) {
                                         passwordL2.setText(cpText + Constants.BLANK_STR + getMessage("password.confirmation.invalid"));
                                         passwordL2.setForeground(Color.RED);
                                     } else {
                                         passwordL2.setText(cpText + Constants.BLANK_STR + getMessage("password.confirmation.confirmed"));
                                         passwordL2.setForeground(Color.BLUE);
                                     }
                                 }
                             });
                             JPanel cbPanel = new JPanel(new GridLayout(10, 1));
                             cbPanel.add(exportPreferencesCB);
                             cbPanel.add(exportDataEntryConfigsCB);
                             cbPanel.add(exportOnlyRelatedDataEntryConfigsCB);
                             cbPanel.add(exportToolsDataCB);
                             cbPanel.add(exportIconsCB);
                             cbPanel.add(exportOnlyRelatedIconsCB);
                             cbPanel.add(exportAppCoreCB);
                             cbPanel.add(exportAddOnsCB);
                             cbPanel.add(exportAddOnConfigsCB);
                             cbPanel.add(exportImportExportConfigsCB);
                             JPanel passPanel = new JPanel(new GridLayout(4, 1));
                             passPanel.add(passwordL1);
                             passPanel.add(passwordTF1);
                             passPanel.add(passwordL2);
                             passPanel.add(passwordTF2);
                             JPanel exportPanel = new JPanel(new BorderLayout());
                             exportPanel.add(cbPanel, BorderLayout.CENTER);
                             exportPanel.add(passPanel, BorderLayout.SOUTH);
                             if (dataTree != null) {
                                 JPanel treePanel = new JPanel(new BorderLayout());
                                 treePanel.add(exportInfoLabel, BorderLayout.NORTH);
                                 treePanel.add(new JScrollPane(dataTree), BorderLayout.CENTER);
                                 exportPanel.add(treePanel, BorderLayout.EAST);
                             }
                             opt = JOptionPane.showConfirmDialog(
                                     FrontEnd.this, 
                                     exportPanel,
                                     getMessage("data.export"),
                                     JOptionPane.OK_CANCEL_OPTION);
                             if (opt == JOptionPane.OK_OPTION) {
                                 if (!Arrays.equals(passwordTF1.getPassword(), passwordTF2.getPassword())) {
                                     throw new Exception(getMessage("password.confirmation.failure"));
                                 }
                                 syncExecute(new Runnable(){
                                     public void run() {
                                         JPanel panel = new JPanel(new BorderLayout());
                                         DefaultListModel processModel = new DefaultListModel();
                                         JList processList = new JList(processModel);
                                         panel.add(processList, BorderLayout.CENTER);
                                         JLabel label = new JLabel(getMessage("data.export"));
                                         processModel.addElement(getMessage("export.data.compressing"));
                                         displayBottomPanel(label, panel);
                                         autoscrollList(processList);
                                         try {
                                             boolean exportAll = false;
                                             if (checkTreeManager != null) {
                                                 TreePath[] checkedPaths = checkTreeManager.getSelectionModel().getSelectionPaths();
                                                 if (checkedPaths != null) {
                                                     Iterator<DefaultMutableTreeNode> it = categoriesToExportRecursively.keySet().iterator();
                                                     while (it.hasNext()) {
                                                         DefaultMutableTreeNode node = it.next();
                                                         if (node.isRoot()) {
                                                             exportAll = true;
                                                             data.setRecursivelyExported(true);
                                                             break;
                                                         } else {
                                                             selectedRecursiveEntries.add(nodeEntries.get(node).getId());
                                                         }
                                                     }
                                                     if (!exportAll) {
                                                         for (TreePath tp : checkedPaths) {
                                                             DefaultMutableTreeNode lastNodeInPath = (DefaultMutableTreeNode) tp.getLastPathComponent();
                                                             for (Object o : tp.getPath()) {
                                                                 DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
                                                                 Recognizable entry = nodeEntries.get(node);
                                                                 if (entry != null) {
                                                                     selectedEntries.add(entry.getId());
                                                                 }
                                                                 if (node.equals(lastNodeInPath)) {
                                                                     selectDescenantEntries(node, selectedEntries);
                                                                 }
                                                             }
                                                         }
                                                         filterData(data, selectedEntries, selectedRecursiveEntries);
                                                     }
                                                 }
                                             }
                                             ExportConfiguration exportConfig = new ExportConfiguration();
                                             exportConfig.setTransferProvider(transferrer.getClass().getSimpleName());
                                             exportConfig.setExportPreferences(exportPreferencesCB.isSelected()); 
                                             exportConfig.setExportDataEntryConfigs(exportDataEntryConfigsCB.isSelected());
                                             exportConfig.setExportOnlyRelatedDataEntryConfigs(exportOnlyRelatedDataEntryConfigsCB.isSelected());
                                             exportConfig.setExportToolsData(exportToolsDataCB.isSelected());
                                             exportConfig.setExportIcons(exportIconsCB.isSelected());
                                             exportConfig.setExportOnlyRelatedIcons(exportOnlyRelatedIconsCB.isSelected());
                                             exportConfig.setExportAppCore(exportAppCoreCB.isSelected());
                                             exportConfig.setExportAddOnsAndLibs(exportAddOnsCB.isSelected());
                                             exportConfig.setExportAddOnConfigs(exportAddOnConfigsCB.isSelected());
                                             exportConfig.setExportImportExportConfigs(exportImportExportConfigsCB.isSelected());
                                             exportConfig.setPassword(new String(passwordTF1.getPassword()));
                                             final TransferData td = BackEnd.getInstance().exportData(data, exportConfig);
                                             try {
                                                 processModel.addElement(getMessage("export.data.compressed"));
                                                 autoscrollList(processList);
                                                 // check if checksum of data to be exported has changed since last export (or if export is forced)...
                                                 if (!exportUnchangedDataCB.isSelected() && !transferrer.exportCheckSumChanged(transferOptions, td)) {
                                                     // ... if no, do not export and inform user about that
                                                     label.setText(Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_OK + getMessage("export.completed") + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                                                     processModel.addElement(getMessage("transfer.discarded.no.data.changes"));
                                                     autoscrollList(processList);
                                                 } else {
                                                     // ... if yes, do perform export
                                                     processModel.addElement(getMessage("transfer.data.transferring"));
                                                     autoscrollList(processList);
                                                     instance.displayStatusBarProgressBar(getMessage("exporting.data"));
                                                     if (transferrer instanceof ObservableTransferExtension) {
                                                         instance.getStatusBarProgressBar().setMaximum(td.getData().length);
                                                         ((ObservableTransferExtension) transferrer).setListener(new TransferProgressListener(){
                                                             public void onProgress(long transferredBytesNum, long elapsedTime) {
                                                                 instance.getStatusBarProgressBar().setValue((int) transferredBytesNum);
                                                                 double estimationCoef = ((double) td.getData().length) / ((double) transferredBytesNum);
                                                                 long estimationTime = (long) (elapsedTime * estimationCoef - elapsedTime);
                                                                 instance.getStatusBarProgressBar().setString( 
                                                                         FormatUtils.formatByteSize(transferredBytesNum) + " / " + FormatUtils.formatByteSize(td.getData().length)
                                                                         + ", " + getMessage("elapsed.time") + ": " + FormatUtils.formatTimeDuration(elapsedTime) 
                                                                         + ", " + getMessage("estimated.time.left") + ": " + FormatUtils.formatTimeDuration(estimationTime));
                                                             }
                                                         });
                                                     }    
                                                     boolean exported = transferrer.exportData(td, transferOptions, exportUnchangedDataCB.isSelected());
                                                     if (exported) {
                                                         label.setText(Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_OK + getMessage("export.completed") + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                                                         processModel.addElement(getMessage("export.success"));
                                                         autoscrollList(processList);
                                                         StringBuffer sb = new StringBuffer(getMessage("export.done"));
                                                         Properties meta = td.getMetaData();
                                                         if (meta != null && !meta.isEmpty()) {
                                                             sb.append(" (");
                                                             String size = meta.getProperty(Constants.META_DATA_FILESIZE);
                                                             if (!Validator.isNullOrBlank(size)) {
                                                                 sb.append(FormatUtils.formatByteSize(Long.valueOf(size)));
                                                             }
                                                             String timestamp = meta.getProperty(Constants.META_DATA_TIMESTAMP);
                                                             if (!Validator.isNullOrBlank(timestamp)) {
                                                                 sb.append(", ");
                                                                 sb.append(dateFormat.format(new Date(Long.valueOf(timestamp))));
                                                             }
                                                             sb.append(")");
                                                         }
                                                         displayStatusBarMessage(sb.toString());
                                                         fireTransferEvent(new TransferEvent(TRANSFER_TYPE.EXPORT, transferrer.getClass()));
                                                         configsCB.setEditable(true);
                                                         Component[] c = new Component[] {
                                                                 new JLabel(
                                                                         Constants.HTML_PREFIX + 
                                                                         getMessage("export.success") + "<br/>" + 
                                                                         getMessage("transfer.configuration.save") +
                                                                         Constants.HTML_SUFFIX),
                                                                 configsCB          
                                                         };
                                                         JOptionPane.showMessageDialog(FrontEnd.this, c);
                                                         if (!Validator.isNullOrBlank(configsCB.getSelectedItem())) {
                                                             String configName = configsCB.getSelectedItem().toString();
                                                             if (!exportAll) {
                                                                 if (!selectedEntries.isEmpty()) {
                                                                     Collection<UUID> ids = new ArrayList<UUID>();
                                                                     for (UUID id : selectedEntries) {
                                                                         ids.add(id);
                                                                     }
                                                                     exportConfig.setSelectedIds(ids);
                                                                 }
                                                                 if (!selectedRecursiveEntries.isEmpty()) {
                                                                     Collection<UUID> ids = new ArrayList<UUID>();
                                                                     for (UUID id : selectedRecursiveEntries) {
                                                                         ids.add(id);
                                                                     }
                                                                     exportConfig.setSelectedRecursiveIds(ids);
                                                                 }
                                                                 exportConfig.setExportAll(false);
                                                             } else {
                                                                 exportConfig.setExportAll(true);
                                                             }
                                                             BackEnd.getInstance().storeExportConfigurationAndOptions(configName, exportConfig, transferOptions);
                                                             processModel.addElement(getMessage("transfer.configuration.saved", configName));
                                                             autoscrollList(processList);
                                                         }
                                                     }
                                                 }    
                                             } finally {
                                                 // memory usage optimization
                                                 if (td != null) {
                                                     td.setData(null);
                                                     td.setMetaData(null);
                                                 }
                                             }
                                         } catch (Throwable ex) {
                                             processModel.addElement(getMessage("export.failure"));
                                             if (ex.getMessage() != null) {
                                                 processModel.addElement(getMessage("error.details") + Constants.BLANK_STR + ": " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                                             }
                                             autoscrollList(processList);
                                             label.setText(Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_ERROR + getMessage("export.failure") + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                                             ex.printStackTrace(System.err);
                                         } finally {
                                             instance.hideStatusBarProgressBar();
                                         }
                                     }
                                 }, getMessage("exporting.data") + "...");
                             }
                         }
                     }
                 }
             } catch (Throwable t) {
                 displayErrorMessage(getMessage("export.failure") + ": " + CommonUtils.getFailureDetails(t), t);
             }
         }
     };
     
     public static void autoExport(final String configName, final boolean force, final boolean verbose) {
         if (instance != null) {
             JPanel panel = verbose ? new JPanel(new BorderLayout()) : null;
             JLabel processLabel = verbose ? new JLabel(getMessage("exporting.data") + " ('" + configName + "')...") : null;
             JLabel label = verbose ? new JLabel(getMessage("data.export")) : null;
             if (verbose) panel.add(processLabel, BorderLayout.CENTER);
             if (verbose) displayBottomPanel(label, panel);
             try {
                 instance.displayProcessNotification(getMessage("exporting.data") + " ('" + configName + "')...", false);
                 DataCategory data = instance.collectData();
                 final ExportConfiguration exportConfig = BackEnd.getInstance().getPopulatedExportConfigurations().get(configName);
                 // FIXME [P1] exportConfig can be null here (check and add appropriate error message)
                 if (!exportConfig.isExportAll()) {
                     instance.filterData(data, exportConfig.getSelectedIds(), exportConfig.getSelectedRecursiveIds());
                 } else {
                     data.setRecursivelyExported(true);
                 }
                 byte[] transferOptions = BackEnd.getInstance().getExportOptions(configName);
                 final TransferExtension transferrer = ExtensionFactory.getTransferExtension(exportConfig.getTransferProvider());
                 if (transferrer == null) {
                     throw new Exception(getMessage("transfer.type.no.longer.available"));
                 }
                 // check connection before performing export
                 transferrer.checkConnection(transferOptions);
                 // if no exceptions thrown, proceed further
                 final TransferData td = BackEnd.getInstance().exportData(data, exportConfig);
                 try {
                     // check if checksum of data to be exported has changed since last export (or if export is forced)...
                     if (!force && !transferrer.exportCheckSumChanged(transferOptions, td)) {
                         // ... if no, do not export...
                         if (verbose) {
                             // ... and inform user about that, if in verbose mode
                             label.setText(Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_OK + getMessage("export.completed") + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                             processLabel.setText(getMessage("transfer.discarded.no.data.changes"));
                         }
                     } else {
                         // ... if yes, do perform export
                         instance.displayStatusBarProgressBar(getMessage("exporting.data") + " ('" + configName + "')...");
                         if (transferrer instanceof ObservableTransferExtension) {
                             instance.getStatusBarProgressBar().setMaximum(td.getData().length);
                             ((ObservableTransferExtension) transferrer).setListener(new TransferProgressListener(){
                                 public void onProgress(long transferredBytesNum, long elapsedTime) {
                                     instance.getStatusBarProgressBar().setValue((int) transferredBytesNum);
                                     double estimationCoef = ((double) td.getData().length) / ((double) transferredBytesNum);
                                     long estimationTime = (long) (elapsedTime * estimationCoef - elapsedTime);
                                     instance.getStatusBarProgressBar().setString( 
                                             FormatUtils.formatByteSize(transferredBytesNum) + " / " + FormatUtils.formatByteSize(td.getData().length)
                                             + ", " + getMessage("elapsed.time") + ": " + FormatUtils.formatTimeDuration(elapsedTime) 
                                             + ", " + getMessage("estimated.time.left") + ": " + FormatUtils.formatTimeDuration(estimationTime));
                                 }
                             });
                         }    
                         boolean exported = transferrer.exportData(td, transferOptions, force);
                         if (exported) {
                             if (verbose) {
                                 label.setText(Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_OK + getMessage("export.completed") + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                                 processLabel.setText(getMessage("export.success"));
                             }
                             StringBuffer sb = new StringBuffer(getMessage("export.done") + " ('" + configName + "'");
                             Properties meta = td.getMetaData();
                             if (meta != null && !meta.isEmpty()) {
                                 String size = meta.getProperty(Constants.META_DATA_FILESIZE);
                                 if (!Validator.isNullOrBlank(size)) {
                                     sb.append(", ");
                                     sb.append(FormatUtils.formatByteSize(Long.valueOf(size)));
                                 }
                                 String timestamp = meta.getProperty(Constants.META_DATA_TIMESTAMP);
                                 if (!Validator.isNullOrBlank(timestamp)) {
                                     sb.append(", ");
                                     sb.append(dateFormat.format(new Date(Long.valueOf(timestamp))));
                                 }
                             }
                             sb.append(")");
                             displayStatusBarMessage(sb.toString());
                             fireTransferEvent(new TransferEvent(TRANSFER_TYPE.EXPORT, transferrer.getClass(), configName));
                         }
                     }
                 } finally {
                     // memory usage optimization
                     if (td != null) {
                         td.setData(null);
                         td.setMetaData(null);
                     }
                 }
             } catch (Throwable ex) {
                 String errMsg = getMessage("export.failure");
                 if (ex.getMessage() != null) {
                     errMsg += Constants.BLANK_STR + getMessage("error.details") + ": " + ex.getClass().getSimpleName() + ": " + ex.getMessage();
                 }
                 if (verbose) {
                     processLabel.setText(errMsg);
                     label.setText(Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_ERROR + getMessage("export.failure") + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                 }
                 displayStatusBarErrorMessage(errMsg);
                 ex.printStackTrace(System.err);
             } finally {
                 instance.hideStatusBarProgressBar();
                 instance.hideProcessNotification();
             }
         }
     }
     
     @SuppressWarnings("unchecked")
     private void selectDescenantEntries(DefaultMutableTreeNode node, Collection<UUID> selectedEntries) {
         if (node.getChildCount() != 0) {
             Enumeration<DefaultMutableTreeNode> childs = node.children();
             while (childs.hasMoreElements()) {
                 DefaultMutableTreeNode childNode = childs.nextElement();
                 Recognizable childEntry = nodeEntries.get(childNode);
                 if (childEntry != null) {
                     selectedEntries.add(childEntry.getId());
                     selectDescenantEntries(childNode, selectedEntries);
                 }
             }
         }
     }
     
     private void filterData(DataCategory data, Collection<UUID> filterEntries) {
         Collection<Recognizable> initialData = new ArrayList<Recognizable>(data.getData());
         for (Recognizable r : initialData) {
             if (!filterEntries.contains(r.getId())) {
                 data.removeDataItem(r);
             } else if (r instanceof DataCategory) {
                 filterData((DataCategory) r, filterEntries);
             }
         }
     }
     
     private void filterData(DataCategory data, Collection<UUID> filterEntries, Collection<UUID> selectedRecursiveEntries) {
         if (selectedRecursiveEntries.contains(data.getId())) {
             data.setRecursivelyExported(true);
         }
         Collection<Recognizable> initialData = new ArrayList<Recognizable>(data.getData());
         for (Recognizable r : initialData) {
             if (!filterEntries.contains(r.getId())) {
                 data.removeDataItem(r);
             } else if (r instanceof DataCategory) {
                 if (!selectedRecursiveEntries.contains(r.getId())) {
                     filterData((DataCategory) r, filterEntries, selectedRecursiveEntries);
                 } else {
                     ((DataCategory) r).setRecursivelyExported(true);
                 }
             }
         }
     }
     
     public class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
         private static final long serialVersionUID = 1L;
         public Component getTreeCellRendererComponent(
                             JTree tree,
                             Object value,
                             boolean sel,
                             boolean expanded,
                             boolean leaf,
                             int row,
                             boolean hasFocus) {
             super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
             DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
             Recognizable r = nodeEntries.get(node);
             if (r != null && r.getIcon() != null) {
                 setIcon(r.getIcon());
             }
             return this;
         }
     }
     
     private JTree buildDataTree(DataCategory data) throws Throwable {
         nodeEntries = new HashMap<DefaultMutableTreeNode, Recognizable>();
         categoriesToExportRecursively = new HashMap<DefaultMutableTreeNode, Collection<DefaultMutableTreeNode>>();
         DefaultMutableTreeNode root = new DefaultMutableTreeNode(Constants.DATA_TREE_ROOT_NODE_CAPTION);
         DefaultTreeModel model = new DefaultTreeModel(root);
         final JTree dataTree = new JTree(model);
         buildDataTree(root, data);
         expandTreeNodes(dataTree, root);
         dataTree.setCellRenderer(new CustomTreeCellRenderer());
         return dataTree;
     }
     
     private void buildDataTree(DefaultMutableTreeNode node, DataCategory data) throws Throwable {
         for (Recognizable item : data.getData()) {
             DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
             if (item instanceof DataEntry) {
                 DataEntry de = (DataEntry) item;
                 childNode.setUserObject(de.getCaption());
                 nodeEntries.put(childNode, de);
                 node.add(childNode);
             } else if (item instanceof DataCategory) {
                 DataCategory dc = (DataCategory) item;
                 childNode.setUserObject(dc.getCaption());
                 nodeEntries.put(childNode, dc);
                 node.add(childNode);
                 buildDataTree(childNode, dc);
             }
         }
     }
     
     @SuppressWarnings("unchecked")
     private void expandTreeNodes(JTree tree, DefaultMutableTreeNode node) {
         if (node.getChildCount() != 0) {
             tree.expandPath(new TreePath(node.getPath()));
             Enumeration<DefaultMutableTreeNode> e = node.children();
             while (e.hasMoreElements()) {
                 DefaultMutableTreeNode n = e.nextElement();
                 expandTreeNodes(tree, n);
             }
         }
     }
     
     private AddCategoryAction addCategoryAction = new AddCategoryAction();
     private class AddCategoryAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public AddCategoryAction() {
             putValue(Action.NAME, "addCategory");
             putValue(Action.SHORT_DESCRIPTION, getMessage("add.category"));
             putValue(Action.SMALL_ICON, guiIcons.getIconCategory());
         }
         
         public void actionPerformed(ActionEvent evt) {
             try {
                 if (getJTabbedPane().getTabCount() == 0) {
                     if (!defineRootPlacement()) {
                         return;
                     }
                 }
                 JCheckBox addToRootCB = null;
                 if (getJTabbedPane().getTabCount() == 0 || getJTabbedPane().getSelectedIndex() == -1) {
                     currentTabPane = getJTabbedPane();
                 } else {
                     addToRootCB = new JCheckBox(getMessage("add.category.to.rootlevel"));
                 }
                 JLabel pLabel = new JLabel(getMessage("tabs.placement"));
                 JComboBox placementsChooser = new JComboBox();
                 for (Placement placement : PLACEMENTS) {
                     placementsChooser.addItem(placement);
                 }
                 JLabel icLabel = new JLabel(getMessage("icon"));
                 IconChooserComboBox iconChooser = new IconChooserComboBox();
                 JLabel cLabel = new JLabel(getMessage("caption"));
                 String categoryCaption = JOptionPane.showInputDialog(
                         FrontEnd.this, 
                         new Component[] { addToRootCB, pLabel, placementsChooser, icLabel, iconChooser, cLabel },
                         getMessage("add.category"), 
                         JOptionPane.QUESTION_MESSAGE);
                 if (categoryCaption != null) {
                     JTabbedPane categoryTabPane = new JTabbedPane();
                     UUID id = UUID.randomUUID();
                     categoryTabPane.setName(id.toString());
                     categoryTabPane.setTabPlacement(((Placement) placementsChooser.getSelectedItem()).getInteger());
                     addTabPaneListeners(categoryTabPane);
                     JTabbedPane tabPane = (addToRootCB != null && addToRootCB.isSelected()) || currentTabPane == null ? getJTabbedPane() : currentTabPane;
                     tabPane.addTab(categoryCaption, categoryTabPane);
                     JTabbedPane parentTabPane = ((JTabbedPane) categoryTabPane.getParent());
                     parentTabPane.setSelectedComponent(categoryTabPane);
                     ImageIcon icon = iconChooser.getSelectedIcon();
                     if (icon != null) {
                         parentTabPane.setIconAt(parentTabPane.getSelectedIndex(), icon);
                     }
                     displayStatusBarMessage(getMessage("category.added", categoryCaption));
                     currentTabPane = (JTabbedPane) categoryTabPane.getParent();
                 }
             } catch (Exception ex) {
                 displayErrorMessage(getMessage("add.category.failure"), ex);
             }
         }
     };
 
     private Action saveAction = new SaveAction();
     private class SaveAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public SaveAction() {
             putValue(Action.NAME, "save");
             putValue(Action.SHORT_DESCRIPTION, getMessage("save").concat(" {Ctrl+S}"));
             putValue(Action.SMALL_ICON, guiIcons.getIconSave());
         }
 
         public void actionPerformed(ActionEvent evt) {
             store(false);
         }
     };
     
     private FullScreenAction fullScreenAction = new FullScreenAction();
     private class FullScreenAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public FullScreenAction() {
             putValue(Action.NAME, "full-screen");
             putValue(Action.SHORT_DESCRIPTION, getMessage("switch full-screen mode"));
         }
 
         public void actionPerformed(ActionEvent evt) {
             switchDisplayMode();
         }
     };
     
     private void switchToFullScreenMode() {
         boolean failure = false;
         try {
             // enter full-screen mode
             gd.setFullScreenWindow(this);
             validate();
         } catch (Throwable t) {
             displayErrorMessage("Failed to enter full-screen mode!", t);
             failure = true;
         } finally {
             if (failure) {
                 switchToWindowedMode();
             }
         }
     }
 
     private void switchToWindowedMode() {
         // enter windowed mode
         gd.setFullScreenWindow(null);
     }
 
     private void switchDisplayMode() {
         Window w = gd.getFullScreenWindow();
         if (w != null && w.equals(this)) {
             switchToWindowedMode();
         } else {
             switchToFullScreenMode();
         }
     }
 
     private CloseAction closeAction = new CloseAction();
     private class CloseAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public CloseAction() {
             putValue(Action.NAME, "close");
             putValue(Action.SHORT_DESCRIPTION, getMessage("close window"));
         }
 
         public void actionPerformed(ActionEvent evt) {
             FrontEnd.this.dispatchEvent(new WindowEvent(FrontEnd.this, WindowEvent.WINDOW_CLOSING));
         }
     };
     
     private ExitAction exitAction = new ExitAction();
     private class ExitAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public ExitAction() {
             putValue(Action.NAME, "exit");
             putValue(Action.SHORT_DESCRIPTION, getMessage("exit").concat(" {Ctrl+Alt+Q}"));
             putValue(Action.SMALL_ICON, guiIcons.getIconExit());
         }
 
         public void actionPerformed(ActionEvent evt) {
             exit();
         }
     };
     
     private BackToFirstAction backToFirstAction = new BackToFirstAction();
     private class BackToFirstAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public BackToFirstAction() {
             putValue(Action.NAME, "backToFirst");
             putValue(Action.SHORT_DESCRIPTION, getMessage("history.back.to.first").concat(" {Alt+Home}"));
             putValue(Action.SMALL_ICON, guiIcons.getIconBackToFirst());
             setEnabled(false);
         }
 
         public void actionPerformed(ActionEvent evt) {
             navigationHistoryIndex = 0;
             UUID id = navigationHistory.get(navigationHistoryIndex);
             switchToVisualEntry(id, false);
         }
     };
     
     private BackAction backAction = new BackAction();
     private class BackAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public BackAction() {
             putValue(Action.NAME, "back");
             putValue(Action.SHORT_DESCRIPTION, getMessage("history.back").concat(" {Alt+←}"));
             putValue(Action.SMALL_ICON, guiIcons.getIconBack());
             setEnabled(false);
         }
 
         public void actionPerformed(ActionEvent evt) {
             if (navigationHistoryIndex > 0) {
                 navigationHistoryIndex--;
                 UUID id = navigationHistory.get(navigationHistoryIndex);
                 switchToVisualEntry(id, false);
             }
         }
     };
     
     private ForwardAction forwardAction = new ForwardAction();
     private class ForwardAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public ForwardAction() {
             putValue(Action.NAME, "forward");
             putValue(Action.SHORT_DESCRIPTION, getMessage("history.forward").concat(" {Alt+→}"));
             putValue(Action.SMALL_ICON, guiIcons.getIconForward());
             setEnabled(false);
         }
 
         public void actionPerformed(ActionEvent evt) {
             if (!navigationHistory.isEmpty() && navigationHistory.size() > navigationHistoryIndex + 1) {
                 navigationHistoryIndex++;
                 UUID id = navigationHistory.get(navigationHistoryIndex);
                 switchToVisualEntry(id, false);
             }
         }
     };
     
     private ForwardToLastAction forwardToLastAction = new ForwardToLastAction();
     private class ForwardToLastAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public ForwardToLastAction() {
             putValue(Action.NAME, "forwardToLast");
             putValue(Action.SHORT_DESCRIPTION, getMessage("history.forward.to.last").concat(" {Alt+End}"));
             putValue(Action.SMALL_ICON, guiIcons.getIconForwardToLast());
             setEnabled(false);
         }
 
         public void actionPerformed(ActionEvent evt) {
             navigationHistoryIndex = navigationHistory.size() - 1;
             UUID id = navigationHistory.get(navigationHistoryIndex);
             switchToVisualEntry(id, false);
         }
     };
     
     private PreferencesAction preferencesAction = new PreferencesAction();
     private class PreferencesAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public PreferencesAction() {
             putValue(Action.NAME, "preferences");
             putValue(Action.SHORT_DESCRIPTION, getMessage("preferences").concat(" {Ctrl+P}"));
             putValue(Action.SMALL_ICON, guiIcons.getIconPreferences());
         }
 
         boolean prefsErr;
         
         @SuppressWarnings("unchecked")
         public void actionPerformed(ActionEvent e) {
             try {
                 prefsErr = false;
                 byte[] before = Preferences.getInstance().serialize();
                 JPanel prefsPanel = null;
                 Map<Component, Field> prefEntries = new HashMap<Component, Field>();
                 Collection<JPanel> prefPanels = new LinkedList<JPanel>();
                 Field[] fields = Preferences.class.getDeclaredFields();
                 try {
                     for (final Field field : fields) {
                         Preference prefAnn = field.getAnnotation(Preference.class);
                         if (prefAnn != null) {
                             JPanel prefPanel = null;
                             Component prefControl = null;
                             String type = field.getType().getSimpleName().toLowerCase();
                             if ("string".equals(type)) {
                                 prefPanel = new JPanel(new GridLayout(2, 1));
                                 JLabel prefTitle = new JLabel(getMessage(prefAnn.title()) + Constants.BLANK_STR);
                                 if (!Validator.isNullOrBlank(prefAnn.description())) {
                                     prefTitle.setToolTipText(getMessage(prefAnn.description()));
                                 }
                                 prefPanel.add(prefTitle);
                                 if (field.isAnnotationPresent(PreferenceChoice.class)) {
                                     prefControl = new JComboBox();
                                     PreferenceChoice choiceAnn = field.getAnnotation(PreferenceChoice.class);
                                     ((JComboBox) prefControl).setEditable(choiceAnn.isEditable());
                                     PreferenceChoiceProvider choiceProvider = choiceAnn.providerClass().newInstance();
                                     for (String choice : choiceProvider.getPreferenceChoices()) {
                                         ((JComboBox) prefControl).addItem(choice);
                                     }
                                 } else if (field.isAnnotationPresent(PreferenceProtect.class)) {
                                     prefControl = new JPasswordField();
                                 } else {
                                     prefControl = new JTextField();
                                 }
                                 prefControl.setPreferredSize(new Dimension(150, 20));
                                 PreferenceValidation prefValAnn = field.getAnnotation(PreferenceValidation.class);
                                 if (prefValAnn != null) {
                                     // TODO [P3] optimization: there should be only one instance of certain validation class
                                     final PreferenceValidator<String> validator = (PreferenceValidator<String>) prefValAnn.validationClass().newInstance();
                                     final JTextComponent textControl = ((JTextComponent) prefControl);
                                     final Color normal = textControl.getForeground();
                                     textControl.addCaretListener(new CaretListener(){
                                         public void caretUpdate(CaretEvent e) {
                                             String value = textControl.getText();
                                             try {
                                                 validator.validate(value);
                                                 textControl.setForeground(normal);
                                                 textControl.setToolTipText(null);
                                                 prefsErr = prefsErr | false;
                                             } catch (Exception ex) {
                                                 String errorMsg = "Invalid field value: " + ex.getMessage();
                                                 textControl.setForeground(Color.RED);
                                                 textControl.setToolTipText(errorMsg);
                                                 prefsErr = prefsErr | true;
                                             }
                                         }
                                     });
                                     
                                 }
                                 String text = (String) field.get(Preferences.getInstance());
                                 if (text == null) {
                                     text = Constants.EMPTY_STR;
                                 }
                                 if (prefControl instanceof JTextField) {
                                     ((JTextField) prefControl).setText(text);
                                 } else if (prefControl instanceof JComboBox) {
                                     ((JComboBox) prefControl).setSelectedItem(text);
                                 }
                             } else if ("boolean".equals(type)) {
                                 prefPanel = new JPanel(new GridLayout(1, 1));
                                 prefControl = new JCheckBox(getMessage(prefAnn.title()));
                                 if (!Validator.isNullOrBlank(prefAnn.description())) {
                                     ((JCheckBox) prefControl).setToolTipText(getMessage(prefAnn.description()));
                                 }
                                 ((JCheckBox) prefControl).setSelected(field.getBoolean(Preferences.getInstance()));
                             } else if ("int".equals(type)) {
                                 prefPanel = new JPanel(new GridLayout(2, 1));
                                 JLabel prefTitle = new JLabel(getMessage(prefAnn.title()) + Constants.BLANK_STR);
                                 if (!Validator.isNullOrBlank(prefAnn.description())) {
                                     prefTitle.setToolTipText(getMessage(prefAnn.description()));
                                 }
                                 prefPanel.add(prefTitle);
                                 SpinnerNumberModel sm = new SpinnerNumberModel();
                                 sm.setMinimum(0);
                                 sm.setStepSize(1);
                                 sm.setValue(field.getInt(Preferences.getInstance()));
                                 prefControl = new JSpinner(sm);
                                 
                                 // FIXME [P3] // disable number formatting
 //                                JFormattedTextField tf = ((JSpinner.DefaultEditor) ((JSpinner) prefControl).getEditor()).getTextField();
 //                                DefaultFormatterFactory factory = (DefaultFormatterFactory) tf.getFormatterFactory();
 //                                NumberFormatter formatter = (NumberFormatter) factory.getDefaultFormatter();
 //                                formatter.setFormat(null);
                                 
                                 prefPanel.add(prefControl);
                             }
                             if (prefPanel != null && prefControl != null) {
                                 prefEntries.put(prefControl, field);
                                 prefPanel.add(prefControl);
                                 prefPanels.add(prefPanel);
                             }
                         }
                     }
                     prefsPanel = new JPanel(new GridLayout(prefPanels.size(), 1));
                     for (JPanel prefPanel : prefPanels) {
                         prefsPanel.add(prefPanel);
                     }
                     for (Entry<Component, Field> pref : prefEntries.entrySet()) {
                         PreferenceEnable prefEnableAnn = pref.getValue().getAnnotation(PreferenceEnable.class);
                         if (prefEnableAnn != null) {
                             createPrefChangeListener(pref.getKey(), prefEnableAnn, prefEntries);
                         }
                     }
                 } catch (Exception ex) {
                     displayErrorMessage(getMessage("error.message.preferences.load.failure"), ex);
                 }
                 JScrollPane sp = new JScrollPane(prefsPanel);
                 sp.setPreferredSize(new Dimension(FrontEnd.this.getWidth()/20*17, FrontEnd.this.getHeight()/4*3));
                 int opt = JOptionPane.showConfirmDialog(FrontEnd.this, sp, getMessage("preferences"), JOptionPane.OK_CANCEL_OPTION);
                 if (opt == JOptionPane.OK_OPTION) {
                     if (prefsErr) {
                         displayErrorMessage(getMessage("warning.message.preferences.contain.error"));
                     } else {
                         try {
                             for (Entry<Component, Field> pref : prefEntries.entrySet()) {
                                 if (pref.getKey() instanceof JTextField) {
                                     pref.getValue().set(Preferences.getInstance(), ((JTextField) pref.getKey()).getText());
                                 } else if (pref.getKey() instanceof JCheckBox) {
                                     pref.getValue().setBoolean(Preferences.getInstance(), ((JCheckBox) pref.getKey()).isSelected());
                                 } else if (pref.getKey() instanceof JComboBox) {
                                     pref.getValue().set(Preferences.getInstance(), ((JComboBox) pref.getKey()).getSelectedItem());
                                 } else if (pref.getKey() instanceof JSpinner) {
                                     pref.getValue().set(Preferences.getInstance(), ((JSpinner) pref.getKey()).getValue());
                                 }
                             }
                             byte[] after = Preferences.getInstance().serialize();
                             if (!Arrays.equals(after, before)) {
                                 BackEnd.getInstance().storePreferences();
                                 applyPreferences();
                             }
                         } catch (Exception ex) {
                             displayErrorMessage(getMessage("error.message.preferences.save.failure"), ex);
                         }
                     }
                 }
             } catch (Exception ex) {
                 displayErrorMessage(ex);
             }
         }
     };
     
     private void createPrefChangeListener(final Component c, final PreferenceEnable ann, Map<Component, Field> prefEntries) {
         for (final Entry<Component, Field> pref : prefEntries.entrySet()) {
             if (ann.enabledByField().equals(pref.getValue().getName())) {
                 if (pref.getKey() instanceof JTextField) {
                     c.setEnabled(pref.getKey().isEnabled() && ann.enabledByValue().equals(((JTextField) pref.getKey()).getText()));
                     if (!c.isEnabled() && c instanceof JCheckBox) {
                         ((JCheckBox) c).setSelected(false);
                     }
                     ((JTextField) pref.getKey()).addPropertyChangeListener("text", new PropertyChangeListener(){
                         public void propertyChange(PropertyChangeEvent evt) {
                             c.setEnabled(pref.getKey().isEnabled() && ann.enabledByValue().equals(((JTextField) pref.getKey()).getText()));
                             if (!c.isEnabled() && c instanceof JCheckBox) {
                                 ((JCheckBox) c).setSelected(false);
                             }
                         }
                     });
                     ((JTextField) pref.getKey()).addPropertyChangeListener("enabled", new PropertyChangeListener(){
                         public void propertyChange(PropertyChangeEvent evt) {
                             c.setEnabled(pref.getKey().isEnabled() && ann.enabledByValue().equals(((JTextField) pref.getKey()).getText()));
                             if (!c.isEnabled() && c instanceof JCheckBox) {
                                 ((JCheckBox) c).setSelected(false);
                             }
                         }
                     });
                 } else if (pref.getKey() instanceof JCheckBox) {
                     c.setEnabled(pref.getKey().isEnabled() && ann.enabledByValue().equals("" + ((JCheckBox) pref.getKey()).isSelected()));
                     if (!c.isEnabled() && c instanceof JCheckBox) {
                         ((JCheckBox) c).setSelected(false);
                     }
                     ((JCheckBox) pref.getKey()).addChangeListener(new ChangeListener(){
                         public void stateChanged(ChangeEvent e) {
                             c.setEnabled(pref.getKey().isEnabled() && ann.enabledByValue().equals("" + ((JCheckBox) pref.getKey()).isSelected()));
                             if (!c.isEnabled() && c instanceof JCheckBox) {
                                 ((JCheckBox) c).setSelected(false);
                             }
                         }
                     });
                     ((JCheckBox) pref.getKey()).addPropertyChangeListener("enabled", new PropertyChangeListener(){
                         public void propertyChange(PropertyChangeEvent evt) {
                             c.setEnabled(pref.getKey().isEnabled() && ann.enabledByValue().equals("" + ((JCheckBox) pref.getKey()).isSelected()));
                             if (!c.isEnabled() && c instanceof JCheckBox) {
                                 ((JCheckBox) c).setSelected(false);
                             }
                         }
                     });
                 } else if (pref.getKey() instanceof JComboBox) {
                     c.setEnabled(pref.getKey().isEnabled() && ((JComboBox) pref.getKey()).getSelectedItem() != null && ann.enabledByValue().equals(((JComboBox) pref.getKey()).getSelectedItem().toString()));
                     if (!c.isEnabled() && c instanceof JCheckBox) {
                         ((JCheckBox) c).setSelected(false);
                     }
                     ((JComboBox) pref.getKey()).addItemListener(new ItemListener(){
                         public void itemStateChanged(ItemEvent e) {
                             c.setEnabled(pref.getKey().isEnabled() && ann.enabledByValue().equals(((JComboBox) pref.getKey()).getSelectedItem().toString()));
                             if (!c.isEnabled() && c instanceof JCheckBox) {
                                 ((JCheckBox) c).setSelected(false);
                             }
                         }
                     });
                     ((JComboBox) pref.getKey()).addPropertyChangeListener("enabled", new PropertyChangeListener(){
                         public void propertyChange(PropertyChangeEvent evt) {
                             c.setEnabled(pref.getKey().isEnabled() && ((JComboBox) pref.getKey()).getSelectedItem() != null && ann.enabledByValue().equals(((JComboBox) pref.getKey()).getSelectedItem().toString()));
                             if (!c.isEnabled() && c instanceof JCheckBox) {
                                 ((JCheckBox) c).setSelected(false);
                             }
                         }
                     });
                 }
                 break;
             }
         }
     }
     
     private ManageAddOnsAction manageAddOnsAction = new ManageAddOnsAction();
     private class ManageAddOnsAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public ManageAddOnsAction() {
             putValue(Action.NAME, "manageAddOns");
             putValue(Action.SHORT_DESCRIPTION, getMessage("manage.addons").concat(" {Ctrl+M}"));
             putValue(Action.SMALL_ICON, guiIcons.getIconAddOns());
         }
 
         public void actionPerformed(ActionEvent e) {
             initAddOnsManagementDialog();
             addOnsManagementDialog.setVisible(true);
         }
         
     };
     
     private Object[] getInstallAddOnInfoRow(AddOnInfo addOnInfo) {
         return new Object[] {
                 Boolean.FALSE,
                 addOnInfo.getName(),
                 addOnInfo.getVersion(),
                 addOnInfo.getAuthor() != null ? addOnInfo.getAuthor() : Constants.ADDON_FIELD_VALUE_NA,
                 addOnInfo.getDescription() != null ? addOnInfo.getDescription() : Constants.ADDON_FIELD_VALUE_NA };
     }
     
     private Object[] getAddOnInfoRow(Boolean selected, AddOnInfo addOnInfo, ADDON_STATUS status) {
         if (selected != null) {
             return new Object[] {
                     selected,
                     addOnInfo.getName(),
                     addOnInfo.getVersion(),
                     addOnInfo.getAuthor() != null ? addOnInfo.getAuthor() : Constants.ADDON_FIELD_VALUE_NA,
                     addOnInfo.getDescription() != null ? addOnInfo.getDescription() : Constants.ADDON_FIELD_VALUE_NA,
                     status };
         } else {
             return new Object[] {
                     addOnInfo.getName(),
                     addOnInfo.getVersion(),
                     addOnInfo.getAuthor() != null ? addOnInfo.getAuthor() : Constants.ADDON_FIELD_VALUE_NA,
                     addOnInfo.getDescription() != null ? addOnInfo.getDescription() : Constants.ADDON_FIELD_VALUE_NA,
                     status };
         }
     }
     
     private Object[] getPackRow(Pack pack, ADDON_STATUS status) {
         return new Object[]{
                 Boolean.FALSE,
                 pack.getType().value(), 
                 pack.getName(), 
                 pack.getVersion(), 
                 pack.getAuthor() != null ? pack.getAuthor() : Constants.ADDON_FIELD_VALUE_NA, 
                 pack.getDescription() != null ? pack.getDescription() : Constants.ADDON_FIELD_VALUE_NA,
                 FormatUtils.formatByteSize(pack.getFileSize()),
                 status};
     }
     
     private Map<String, Integer> getDepCounters() {
         if (depCounters == null) {
             depCounters = new HashMap<String, Integer>();
         }
         return depCounters;
     }
     
     private void listExtensions() throws Throwable {
         // ensure entry extensions map has been initialized even if no data-entries have been added so far;
         ExtensionFactory.getAnnotatedEntryExtensionClasses();
         // ... now extensions can be listed
         Map<AddOnInfo, ADDON_STATUS> statuses = BackEnd.getInstance().getNewAddOns(PackType.EXTENSION);
         for (AddOnInfo extension : BackEnd.getInstance().getAddOns(PackType.EXTENSION)) {
             if (statuses != null && statuses.get(extension) != null) {
                 addOrReplaceTableModelAddOnRow(getExtensionsModel(), extension, true, 1, statuses.get(extension));
             } else {
                 addOrReplaceTableModelAddOnRow(getExtensionsModel(), extension, true, 1, ExtensionFactory.getExtensionStatus(extension.getName()));
             }
         }
     }
     
     @SuppressWarnings("unchecked")
     private void listSkins() throws Throwable {
         if (getSkinModel().getRowCount() == 0) {
             getSkinModel().insertRow(0, new Object[]{Boolean.FALSE, DEFAULT_SKIN, Constants.EMPTY_STR, Constants.EMPTY_STR, getMessage("default.skin")});
         }
         Map<AddOnInfo, ADDON_STATUS> statuses = BackEnd.getInstance().getNewAddOns(PackType.SKIN);
         for (AddOnInfo skin : BackEnd.getInstance().getAddOns(PackType.SKIN)) {
             ADDON_STATUS status;
             try {
                 if (statuses != null && statuses.get(skin) != null) {
                     status = statuses.get(skin);
                 } else {
                     String fullSkinName = Constants.SKIN_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR + skin.getName() 
                                             + Constants.PACKAGE_PATH_SEPARATOR + skin.getName();
                     // skin class load test
                     Class<Skin> skinClass = (Class<Skin>) Class.forName(fullSkinName);
                     // skin instantiation test
                     skinClass.newInstance();
                     status = Constants.ADDON_STATUS.Loaded;
                 }
             } catch (Throwable t) {
                 // skin is broken
                 System.err.println(getMessage("error.message.skin.initialization.failure", skin.getName()));
                 t.printStackTrace(System.err);
                 status = BackEnd.getInstance().unresolvedAddOnDependenciesPresent(skin) ? Constants.ADDON_STATUS.BrokenDependencies : Constants.ADDON_STATUS.Broken; 
             }
             addOrReplaceTableModelAddOnRow(getSkinModel(), skin, true, 1, status);
         }
     }
     
     private void listIcons() throws Throwable {
         for (AddOnInfo iconSetInfo : BackEnd.getInstance().getIconSets()) {
             addOrReplaceTableModelAddOnRow(getIconSetModel(), iconSetInfo, true, 1, null);
         }
         for (ImageIcon icon : BackEnd.getInstance().getIcons()) {
             if (icons.keySet().contains(icon.getDescription())) {
                 icons.remove(icon.getDescription());
             }
         }
         getIconListModel().removeAllElements();
         for (ImageIcon icon : BackEnd.getInstance().getIcons()) {
             icons.put(icon.getDescription(), icon);
             getIconListModel().addElement(icon);
         }
     }
     
     private void listLibs() throws Throwable {
         Collection<AddOnInfo> addOnInfos = BackEnd.getInstance().getAddOns(PackType.LIBRARY);
         for (AddOnInfo addOnInfo : addOnInfos) {
             ADDON_STATUS status;
             Map<AddOnInfo, ADDON_STATUS> libStatuses = BackEnd.getInstance().getNewAddOns(PackType.LIBRARY);
             if (libStatuses != null) {
                 status = libStatuses.get(addOnInfo);
             } else {
                 status = Constants.ADDON_STATUS.Loaded;
             }
             addOrReplaceTableModelAddOnRow(getLibModel(), addOnInfo, false, 0, status);
         }
     }
     
     private static class StatusCellRenderer extends DefaultTableCellRenderer {
         private static final long serialVersionUID = 1L;
         public StatusCellRenderer() {
             super();
         }
         public void setValue(Object value) {
             if (value != null) {
                 value = Constants.getAddOnStatusCaption((ADDON_STATUS) value);
             }
             super.setValue(value);
         }
     }
 
     private void initAddOnsManagementDialog() {
         if (addOnsManagementDialog == null) {
             try {
                 // extensions
                 extList = new JTable(getExtensionsModel());
                 extList.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());
                 final TableRowSorter<TableModel> extSorter = new TableRowSorter<TableModel>(getExtensionsModel());
                 extSorter.setSortsOnUpdates(true);
                 extList.setRowSorter(extSorter);
                 extList.getColumnModel().getColumn(0).setPreferredWidth(30);
                 extList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                 listExtensions();
                 JButton extDetailsButt = new JButton(getMessage("details") + "...");
                 extDetailsButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         int idx = extList.getSelectedRow();
                         if (idx != -1) {
                             try {
                                 String extension = (String) extList.getValueAt(idx, 1);
                                 try {
                                     File addOnInfoFile = new File(new File(Constants.ADDON_INFO_DIR, extension), LOCALE + Constants.ADDON_INFO_FILENAME_SUFFIX);
                                     if (!addOnInfoFile.exists()) {
                                         addOnInfoFile = new File(
                                                 new File(Constants.ADDON_INFO_DIR, extension), 
                                                 Constants.DEFAULT_LOCALE + Constants.ADDON_INFO_FILENAME_SUFFIX);
                                     }
                                     if (addOnInfoFile.exists()) {
                                         URL baseURL = addOnInfoFile.getParentFile().toURI().toURL();
                                         URL addOnURL = addOnInfoFile.toURI().toURL();
                                         AddOnInfo extInfo = BackEnd.getInstance().getAddOnInfo(extension, PackType.EXTENSION);
                                         loadAndDisplayPackageDetails(baseURL, addOnURL, extInfo);
                                     } else {
                                         displayMessage(getMessage("info.message.extension.does.not.provide.detailed.info"));
                                     }
                                 } catch (MalformedURLException ex) {
                                     displayErrorMessage(getMessage("error.message.invalid.url") + Constants.BLANK_STR + CommonUtils.getFailureDetails(ex), ex);
                                 }
                             } catch (Throwable t) {
                                 displayErrorMessage(getMessage("error.message.extension.details.display.failure"), t);
                             }
                         }
                     }
                 });
                 JButton extConfigButt = new JButton(getMessage("configure"));
                 extConfigButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         int idx = extList.getSelectedRow();
                         if (idx != -1) {
                             try {
                                 String ext = (String) extList.getValueAt(idx, 1);
                                 Map<AddOnInfo, ADDON_STATUS> newExts = BackEnd.getInstance().getNewAddOns(PackType.EXTENSION);
                                 if (newExts != null && newExts.containsKey(new AddOnInfo(ext))) {
                                     displayMessage(
                                             getMessage("info.message.extension.is.not.yet.configurable") + Constants.NEW_LINE +
                                             getMessage("info.message.restart.bias.first"));
                                 } else {
                                     String extFullClassName = 
                                         Constants.EXTENSION_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                                 + ext + Constants.PACKAGE_PATH_SEPARATOR + ext;
                                     configureExtension(extFullClassName, false);
                                 }
                             } catch (Exception ex) {
                                 displayErrorMessage(ex);
                             }
                         }
                     }
                 });
                 JButton extInstButt = new JButton(getMessage("install.or.update") + "...");
                 extInstButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         installLocalPackages(addOnFileChooser, PackType.EXTENSION, getExtensionsModel());
                     }
                 });
                 JButton extUninstButt = new JButton(getMessage("uninstall"));
                 extUninstButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         try {
                             boolean selected = false;
                             for (int i = 0; i < getExtensionsModel().getRowCount(); i++) {
                                 if ((Boolean) getExtensionsModel().getValueAt(i, 0)) {
                                     selected = true;
                                     break;
                                 }
                             }
                             if (selected) {
                                 if (confirmedUninstall()) {
                                     int i = 0;
                                     while  (i < getExtensionsModel().getRowCount()) {
                                         if ((Boolean) getExtensionsModel().getValueAt(i, 0)) {
                                             String extension = (String) getExtensionsModel().getValueAt(i, 1);
                                             String extFullClassName = 
                                                 Constants.EXTENSION_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                                         + extension + Constants.PACKAGE_PATH_SEPARATOR + extension;
                                             BackEnd.getInstance().uninstallAddOn(extFullClassName, PackType.EXTENSION);
                                             getExtensionsModel().removeRow(i);
                                             i = 0;
                                         } else {
                                             i++;
                                         }
                                     }
                                 }
                             }
                         } catch (Throwable ex) {
                             displayErrorMessage(getMessage("error.message.extensions.uninstall.failure") + Constants.BLANK_STR + CommonUtils.getFailureDetails(ex), ex);
                         }
                     }
                 });
 
                 // skins
                 skinList = new JTable(getSkinModel()) {
                     private static final long serialVersionUID = 1L;
                     @Override
                     public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                         Component c = super.prepareRenderer(renderer, row, column);
                         String currSkinName = config.getProperty(Constants.PROPERTY_SKIN);
                         if (currSkinName == null) {
                             currSkinName = DEFAULT_SKIN;
                         }
                         String name = (String) getModel().getValueAt(row, 1);
                         if (name.equals(activeSkin)) {
                             c.setForeground(Color.BLUE);
                             Font f = super.getFont();
                             f = new Font(f.getName(), Font.BOLD, f.getSize());
                             c.setFont(f);
                         } else if (!activeSkin.equals(currSkinName) && name.equals(currSkinName)) {
                             c.setForeground(Color.BLUE);
                         } else {
                             c.setForeground(super.getForeground());
                         }
                         return c;
                     }
                 };
                 skinList.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());
                 final TableRowSorter<TableModel> skinSorter = new TableRowSorter<TableModel>(getSkinModel());
                 skinSorter.setSortsOnUpdates(true);
                 skinList.setRowSorter(skinSorter);
                 skinList.getColumnModel().getColumn(0).setPreferredWidth(30);
                 skinList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                 listSkins();
                 JButton skinDetailsButt = new JButton(getMessage("details") + "...");
                 skinDetailsButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         int idx = skinList.getSelectedRow();
                         if (idx != -1) {
                             try {
                                 String skin = (String) skinList.getValueAt(idx, 1);
                                 if (DEFAULT_SKIN.equals(skin)) {
                                     displayMessage(getMessage("default.skin.description"));
                                 } else {
                                     try {
                                         File addOnInfoFile = new File(new File(Constants.ADDON_INFO_DIR, skin), LOCALE + Constants.ADDON_INFO_FILENAME_SUFFIX);
                                         if (!addOnInfoFile.exists()) {
                                             addOnInfoFile = new File(
                                                     new File(Constants.ADDON_INFO_DIR, skin), 
                                                     Constants.DEFAULT_LOCALE + Constants.ADDON_INFO_FILENAME_SUFFIX);
                                         }
                                         if (addOnInfoFile.exists()) {
                                             URL baseURL = addOnInfoFile.getParentFile().toURI().toURL();
                                             URL addOnURL = addOnInfoFile.toURI().toURL();
                                             AddOnInfo skinInfo = BackEnd.getInstance().getAddOnInfo(skin, PackType.SKIN);
                                             loadAndDisplayPackageDetails(baseURL, addOnURL, skinInfo);
                                         } else {
                                             displayMessage(getMessage("info.message.skin.does.not.provide.detailed.info"));
                                         }
                                     } catch (MalformedURLException ex) {
                                         displayErrorMessage(getMessage("error.message.invalid.url") + Constants.BLANK_STR + CommonUtils.getFailureDetails(ex), ex);
                                     }
                                 }
                             } catch (Throwable t) {
                                 displayErrorMessage(getMessage("error.message.skin.details.display.failure"), t);
                             }
                         }
                     }
                 });
                 JButton skinActivateButt = new JButton(getMessage("reactivate.skin"));
                 skinActivateButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         int idx = skinList.getSelectedRow();
                         if (idx != -1) {
                             String skin = (String) skinList.getValueAt(idx, 1);
                             if (DEFAULT_SKIN.equals(skin)) {
                                 try {
                                     String fullSkinClassName = 
                                         Constants.SKIN_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                                 + DEFAULT_SKIN + Constants.PACKAGE_PATH_SEPARATOR + DEFAULT_SKIN;
                                     setActiveSkin(fullSkinClassName);
                                     skinList.repaint();
                                 } catch (Throwable t) {
                                     displayErrorMessage(getMessage("error.message.skin.reactivation.failure"), t);
                                 }
                             } else {
                                 Map<AddOnInfo, ADDON_STATUS> newSkins = BackEnd.getInstance().getNewAddOns(PackType.SKIN);
                                 if (newSkins != null && newSkins.containsKey(new AddOnInfo(skin))) {
                                     displayMessage(
                                             getMessage("info.message.skin.is.not.yet.activable") + Constants.NEW_LINE +
                                             getMessage("info.message.restart.bias.first"));
                                 } else {
                                     try {
                                         String fullSkinClassName = 
                                             Constants.SKIN_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                                     + skin + Constants.PACKAGE_PATH_SEPARATOR + skin;
                                         setActiveSkin(fullSkinClassName);
                                         skinList.repaint();
                                     } catch (Throwable t) {
                                         displayErrorMessage(getMessage("error.message.skin.reactivation.failure"), t);
                                     }
                                 }
                             }
                         }
                     }
                 });
                 JButton skinInstButt = new JButton(getMessage("install.or.update") + "...");
                 skinInstButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         installLocalPackages(addOnFileChooser, PackType.SKIN, getSkinModel());
                     }
                 });
                 JButton skinUninstButt = new JButton(getMessage("uninstall"));
                 skinUninstButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         try {
                             boolean selected = false;
                             for (int i = 0; i < getSkinModel().getRowCount(); i++) {
                                 if ((Boolean) getSkinModel().getValueAt(i, 0)) {
                                     selected = true;
                                     break;
                                 }
                             }
                             if (selected) {
                                 if (confirmedUninstall()) {
                                     String currentSkin = config.getProperty(Constants.PROPERTY_SKIN);
                                     int i = 0;
                                     while  (i < getSkinModel().getRowCount()) {
                                         String skin = (String) getSkinModel().getValueAt(i, 1);
                                         if ((Boolean) getSkinModel().getValueAt(i, 0)) {
                                             String fullSkinClassName = 
                                                 Constants.SKIN_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                                         + skin + Constants.PACKAGE_PATH_SEPARATOR + skin;
                                             BackEnd.getInstance().uninstallAddOn(fullSkinClassName, PackType.SKIN);
                                             getSkinModel().removeRow(i);
                                             // if skin that has been uninstalled was active one...
                                             if (skin.equals(currentSkin)) {
                                                 //... unset it (default one will be used)
                                                 config.remove(Constants.PROPERTY_SKIN);
                                             }
                                             i = 0;
                                         } else {
                                             i++;
                                         }
                                     }
                                 }
                             }
                         } catch (Throwable ex) {
                             displayErrorMessage(getMessage("error.message.skins.uninstall.failure") + Constants.BLANK_STR + CommonUtils.getFailureDetails(ex), ex);
                         }
                     }
                 });
                 
                 // icons
                 icSetList = new JTable(getIconSetModel());
                 final TableRowSorter<TableModel> icSetSorter = new TableRowSorter<TableModel>(getIconSetModel());
                 icSetSorter.setSortsOnUpdates(true);
                 icSetList.setRowSorter(icSetSorter);
                 icSetList.getColumnModel().getColumn(0).setPreferredWidth(30);
                 icSetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                 listIcons();
                 JScrollPane jsp = new JScrollPane(getIconList());
                 jsp.setPreferredSize(new Dimension(200,200));
                 jsp.setMinimumSize(new Dimension(200,200));
                 JButton icSetDetailsButt = new JButton(getMessage("details") + "...");
                 icSetDetailsButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         int idx = icSetList.getSelectedRow();
                         if (idx != -1) {
                             try {
                                 String ic = (String) icSetList.getValueAt(idx, 1);
                                 try {
                                     File addOnInfoFile = new File(new File(Constants.ADDON_INFO_DIR, ic), LOCALE + Constants.ADDON_INFO_FILENAME_SUFFIX);
                                     if (!addOnInfoFile.exists()) {
                                         addOnInfoFile = new File(
                                                 new File(Constants.ADDON_INFO_DIR, ic), 
                                                 Constants.DEFAULT_LOCALE + Constants.ADDON_INFO_FILENAME_SUFFIX);
                                     }
                                     if (addOnInfoFile.exists()) {
                                         URL baseURL = addOnInfoFile.getParentFile().toURI().toURL();
                                         URL addOnURL = addOnInfoFile.toURI().toURL();
                                         AddOnInfo icInfo = BackEnd.getInstance().getAddOnInfo(ic, PackType.ICON_SET);
                                         loadAndDisplayPackageDetails(baseURL, addOnURL, icInfo);
                                     } else {
                                         displayMessage(getMessage("info.message.iconset.does.not.provide.detailed.info"));
                                     }
                                 } catch (MalformedURLException ex) {
                                     displayErrorMessage(getMessage("error.message.invalid.url") + Constants.BLANK_STR + CommonUtils.getFailureDetails(ex), ex);
                                 }
                             } catch (Throwable t) {
                                 displayErrorMessage(getMessage("error.message.iconset.details.display.failure"), t);
                             }
                         }
                     }
                 });
                 JButton addIconButt = new JButton(getMessage("add.or.install") + "...");
                 addIconButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         if (iconsFileChooser.showOpenDialog(getActiveWindow()) == JFileChooser.APPROVE_OPTION) {
                             syncExecute(new Runnable(){
                                 public void run() {
                                     try {
                                         boolean added = false;
                                         for (File file : iconsFileChooser.getSelectedFiles()) {
                                             Collection<ImageIcon> icons = BackEnd.getInstance().addIcons(file);
                                             if (!icons.isEmpty()) {
                                                 for (ImageIcon icon : icons) {
                                                     getIconListModel().addElement(icon);
                                                     FrontEnd.icons.put(icon.getDescription(), icon);
                                                 }
                                                 Collection<AddOnInfo> iconSets = BackEnd.getInstance().getIconSets();
                                                 while (getIconSetModel().getRowCount() > 0) {
                                                     getIconSetModel().removeRow(0);
                                                 }
                                                 for (AddOnInfo iconSetInfo : iconSets) {
                                                     addOrReplaceTableModelAddOnRow(getIconSetModel(), iconSetInfo, true, 1, null);
                                                 }
                                                 added = true;
                                             }
                                         }
                                         if (added) {
                                             getIconList().repaint();
                                             displayMessage(getMessage("success.message.icons.installed"));
                                         } else {
                                             displayErrorMessage(getMessage("warning.message.nothing.to.install"));
                                         }
                                     } catch (Throwable t) {
                                         displayErrorMessage(getMessage("error.message.icons.install.failure") + Constants.BLANK_STR + CommonUtils.getFailureDetails(t), t);
                                     }
                                 }
                             }, getMessage("info.message.icons.installation"), true);
                         }
                     }
                 });
                 JButton removeIconButt = new JButton(getMessage("remove.selected.icons"));
                 removeIconButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         try {
                             if (getIconList().getSelectedValues().length > 0) {
                                 Collection<String> removeIds = new ArrayList<String>();
                                 for (Object icon : getIconList().getSelectedValues()) {
                                     removeIds.add(((ImageIcon) icon).getDescription());
                                 }
                                 BackEnd.getInstance().removeIcons(removeIds);
                                 for (Object icon : getIconList().getSelectedValues()) {
                                     getIconListModel().removeElement(icon);
                                 }
                             }
                         } catch (Throwable t) {
                             displayErrorMessage(getMessage("error.message.icons.remove.failure") + Constants.BLANK_STR + CommonUtils.getFailureDetails(t), t);
                         }
                     }
                 });
                 JButton removeIconSetButt = new JButton(getMessage("uninstall.selected.iconsets"));
                 removeIconSetButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         try {
                             boolean selected = false;
                             for (int i = 0; i < getIconSetModel().getRowCount(); i++) {
                                 if ((Boolean) getIconSetModel().getValueAt(i, 0)) {
                                     selected = true;
                                     break;
                                 }
                             }
                             if (selected) {
                                 if (confirmedUninstall()) {
                                     boolean changed = false;
                                     int i = 0;
                                     while  (i < getIconSetModel().getRowCount()) {
                                         if ((Boolean) getIconSetModel().getValueAt(i, 0)) {
                                             String icSet = (String) getIconSetModel().getValueAt(i, 1);
                                             Collection<String> removedIds = BackEnd.getInstance().removeIconSet(icSet);
                                             for (String removedId : removedIds) {
                                                 getIconListModel().removeElement(icons.get(removedId));
                                             }
                                             getIconSetModel().removeRow(i);
                                             changed = true;
                                             i = 0;
                                         } else {
                                             i++;
                                         }
                                     }
                                     if (changed) {
                                         getIconList().repaint();
                                     }
                                 }
                             }
                         } catch (Throwable ex) {
                             displayErrorMessage(getMessage("error.message.iconsets.uninstall.failure") + Constants.BLANK_STR + CommonUtils.getFailureDetails(ex), ex);
                         }
                     }
                 });
 
                 // list of loaded libs
                 libList = getLibsList();
                 JButton libDetailsButt = new JButton(getMessage("details") + "...");
                 libDetailsButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         int idx = libList.getSelectedRow();
                         if (idx != -1) {
                             try {
                                 String lib = (String) libList.getValueAt(idx, 0);
                                 try {
                                     File addOnInfoFile = new File(new File(Constants.ADDON_INFO_DIR, lib), LOCALE + Constants.ADDON_INFO_FILENAME_SUFFIX);
                                     if (!addOnInfoFile.exists()) {
                                         addOnInfoFile = new File(
                                                 new File(Constants.ADDON_INFO_DIR, lib), 
                                                 Constants.DEFAULT_LOCALE + Constants.ADDON_INFO_FILENAME_SUFFIX);
                                     }
                                     if (addOnInfoFile.exists()) {
                                         URL baseURL = addOnInfoFile.getParentFile().toURI().toURL();
                                         URL addOnURL = addOnInfoFile.toURI().toURL();
                                         AddOnInfo libInfo = BackEnd.getInstance().getAddOnInfo(lib, PackType.LIBRARY);
                                         loadAndDisplayPackageDetails(baseURL, addOnURL, libInfo);
                                     } else {
                                         displayMessage(getMessage("info.message.library.does.not.provide.detailed.info"));
                                     }
                                 } catch (MalformedURLException ex) {
                                     displayErrorMessage(getMessage("error.message.invalid.url") + Constants.BLANK_STR + CommonUtils.getFailureDetails(ex), ex);
                                 }
                             } catch (Throwable t) {
                                 displayErrorMessage(getMessage("error.message.library.details.display.failure"), t);
                             }
                         }
                     }
                 });
                 JButton libInstButt = new JButton(getMessage("install.or.update") + "...");
                 libInstButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         installLocalPackages(addOnFileChooser, PackType.LIBRARY, getLibModel());
                     }
                 });
                 
                 // online list of available addons
                 onlineList = new JTable(getOnlineModel());
                 onlineList.getColumnModel().getColumn(7).setCellRenderer(new StatusCellRenderer());
                 final TableRowSorter<TableModel> onlineSorter = new TableRowSorter<TableModel>(getOnlineModel());
                 onlineSorter.setSortsOnUpdates(true);
                 onlineList.setRowSorter(onlineSorter);
                 onlineList.getColumnModel().getColumn(0).setPreferredWidth(30);
                 states = new HashMap<String, Boolean>();
                 TableModelListener dependencyResolver = new TableModelListener(){
                     public void tableChanged(TableModelEvent e) {
                         if (e.getColumn() == 0) {
                             try {
                                 Boolean lastState = states.get(getOnlineModel().getValueAt(e.getFirstRow(), 2));
                                 if (lastState == null) lastState = false;
                                 if ((Boolean) getOnlineModel().getValueAt(e.getFirstRow(), 0) != lastState) {
                                     Pack pack = getAvailableOnlinePackages().get((String) getOnlineModel().getValueAt(e.getFirstRow(), 2));
                                     if (pack.getDependency() != null && !pack.getDependency().isEmpty()) {
                                         for (Dependency dep : pack.getDependency()) {
                                         	Boolean isDependencyInstalledAndUpToDate = BackEnd.getInstance().isDependencyInstalledAndUpToDate(dep);
                                            if (isDependencyInstalledAndUpToDate != Boolean.TRUE) {
                                                 int idx = findDataRowIndex(getOnlineModel(), 2, dep.getName());
                                                 if (idx == -1) {
                                                     throw new Exception(
                                                             getMessage("error.message.dependency.resolution.failure", 
                                                                     pack.getName(), 
                                                                     dep.getType().value(), 
                                                                     dep.getName(), 
                                                                     dep.getVersion() != null ? getMessage("version.x.or.higher", dep.getVersion()) : Constants.EMPTY_STR)
                                                             );
                                                 } else {
                                                     synchronized (FrontEnd.this) {
                                                         Integer counter = getDepCounters().get(dep.getName());
                                                         if (counter == null) {
                                                             counter = 0;
                                                         }
                                                         if ((Boolean) getOnlineModel().getValueAt(e.getFirstRow(), 0)) {
                                                             counter++;
                                                             if (counter > 0) {
                                                                 getOnlineModel().setValueAt(Boolean.TRUE, idx, 0);
                                                             }
                                                         } else {
                                                             if (counter > 0) {
                                                                 counter--;
                                                                 if (counter == 0) {
                                                                     getOnlineModel().setValueAt(Boolean.FALSE, idx, 0);
                                                                 }
                                                             }
                                                         }
                                                         getDepCounters().put(dep.getName(), counter);
                                                     }
                                                 }
                                                 states.put((String) getOnlineModel().getValueAt(e.getFirstRow(), 2), (Boolean) getOnlineModel().getValueAt(e.getFirstRow(), 0));
                                             }
                                         }
                                     }
                                 }
                             } catch (Throwable t) {
                                 displayErrorMessage(getMessage("error.message.dependencies.handle.resolve.failure") + Constants.BLANK_STR + CommonUtils.getFailureDetails(t), t);
                             }
                         }
                     }
                 };
                 getOnlineModel().addTableModelListener(dependencyResolver);
                 onlineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                 final JPanel onlineProgressPanel = new JPanel(new GridLayout(2,1));
                 onlineProgressPanel.add(getOnlineSingleProgressBar());
                 onlineProgressPanel.add(getOnlineTotalProgressBar());
                 JButton onlineRefreshButt = new JButton(getMessage("refresh"));
                 onlineRefreshButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         refreshOnlinePackagesList(null, getOnlineShowAllPackagesCheckBox().isSelected());
                     }
                 });
                 JButton onlineDetailsButt = new JButton(getMessage("details") + "...");
                 onlineDetailsButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         int idx = onlineList.getSelectedRow();
                         if (idx != -1) {
                             String addOnName = (String) onlineList.getValueAt(idx, 2);
                             try {
                                 Pack pack = getAvailableOnlinePackages().get(addOnName);
                                 String fileName = pack.getName() + Constants.PATH_SEPARATOR + LOCALE + Constants.ADDON_INFO_FILENAME_SUFFIX;
                                 URL addOnURL = new URL(BackEnd.getInstance().getRepositoryBaseURL() + fileName);
                                 try {
                                     // try to load addon-info file for preferred locale first...
                                     loadAndDisplayPackageDetails(new URL(BackEnd.getInstance().getRepositoryBaseURL() + pack.getName() + Constants.PATH_SEPARATOR), addOnURL, pack);
                                 } catch (Throwable t) {
                                     // ... if failed, try to load it for default locale
                                     fileName = pack.getName() + Constants.PATH_SEPARATOR + 
                                                     Constants.DEFAULT_LOCALE + Constants.ADDON_INFO_FILENAME_SUFFIX;
                                     addOnURL = new URL(BackEnd.getInstance().getRepositoryBaseURL() + fileName);
                                     try {
                                         loadAndDisplayPackageDetails(new URL(BackEnd.getInstance().getRepositoryBaseURL() + pack.getName() + Constants.PATH_SEPARATOR), addOnURL, pack);
                                     } catch (Throwable t2) {
                                         displayErrorMessage(getMessage("error.message.pack.details.page.load.failure"), t2);
                                     }
                                 }
                             } catch (Exception ex) {
                                 displayErrorMessage(getMessage("error.message.repository.url.resolution.failure") + Constants.BLANK_STR + CommonUtils.getFailureDetails(ex), ex);
                             }
                         }
                     }
                 });
                 JButton onlineInstallButt = new JButton(getMessage("download.and.install"));
                 onlineInstallButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         downloadAndInstallOnlinePackages(null);
                     }
                 });
                 JButton onlineSelectAllUpdatesButt = new JButton(getMessage("select.all.updates"));
                 onlineSelectAllUpdatesButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         selectAllUpdates();
                     }
                 });
                 JButton onlineCancelInstallButt = new JButton(getMessage("cancel.download.and.installation"));
                 onlineCancelInstallButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         Downloader.cancelAll();
                     }
                 });
                 
                 // dialog
                 addOnsPane = new JTabbedPane();
 
                 JPanel extControlsPanel = new JPanel(new GridLayout(1,4));
                 extControlsPanel.add(extDetailsButt);
                 extControlsPanel.add(extConfigButt);
                 extControlsPanel.add(extInstButt);
                 extControlsPanel.add(extUninstButt);
                 JPanel extTopPanel = new JPanel(new BorderLayout());
                 extTopPanel.add(new JLabel(getMessage("filter")), BorderLayout.CENTER);
                 final JTextField extFilterText = new JTextField();
                 extFilterText.addCaretListener(new CaretListener(){
                     public void caretUpdate(CaretEvent e) {
                         extSorter.setRowFilter(RowFilter.regexFilter("(?i)" + extFilterText.getText()));
                     }
                 });
                 extTopPanel.add(extFilterText, BorderLayout.SOUTH);
                 JPanel extPanel = new JPanel(new BorderLayout());
                 extPanel.add(extTopPanel, BorderLayout.NORTH);
                 extPanel.add(new JScrollPane(extList), BorderLayout.CENTER);
                 extPanel.add(extControlsPanel, BorderLayout.SOUTH);
                 
                 addOnsPane.addTab(getMessage("extensions"), guiIcons.getIconExtensions(), extPanel);
                 
                 JPanel skinControlsPanel = new JPanel(new GridLayout(1,4));
                 skinControlsPanel.add(skinDetailsButt);
                 skinControlsPanel.add(skinActivateButt);
                 skinControlsPanel.add(skinInstButt);
                 skinControlsPanel.add(skinUninstButt);
                 JPanel skinTopPanel = new JPanel(new BorderLayout());
                 skinTopPanel.add(new JLabel(getMessage("filter")), BorderLayout.CENTER);
                 final JTextField skinFilterText = new JTextField();
                 skinFilterText.addCaretListener(new CaretListener(){
                     public void caretUpdate(CaretEvent e) {
                         skinSorter.setRowFilter(RowFilter.regexFilter("(?i)" + skinFilterText.getText()));
                     }
                 });
                 skinTopPanel.add(skinFilterText, BorderLayout.SOUTH);
                 JPanel skinPanel = new JPanel(new BorderLayout());
                 skinPanel.add(skinTopPanel, BorderLayout.NORTH);
                 skinPanel.add(new JScrollPane(skinList), BorderLayout.CENTER);
                 skinPanel.add(skinControlsPanel, BorderLayout.SOUTH);
                 
                 addOnsPane.addTab(getMessage("skins"), guiIcons.getIconSkins(), skinPanel);
                 
                 JPanel icControlsPanel = new JPanel(new GridLayout(1,4));
                 icControlsPanel.add(icSetDetailsButt);
                 icControlsPanel.add(addIconButt);
                 icControlsPanel.add(removeIconSetButt);
                 icControlsPanel.add(removeIconButt);
                 JPanel icTopPanel = new JPanel(new BorderLayout());
                 icTopPanel.add(new JLabel(getMessage("filter")), BorderLayout.NORTH);
                 final JTextField icSetFilterText = new JTextField();
                 icSetFilterText.addCaretListener(new CaretListener(){
                     public void caretUpdate(CaretEvent e) {
                         icSetSorter.setRowFilter(RowFilter.regexFilter("(?i)" + icSetFilterText.getText()));
                     }
                 });
                 icTopPanel.add(icSetFilterText, BorderLayout.CENTER);
                 JPanel icPanel = new JPanel(new BorderLayout());
                 icPanel.add(icTopPanel, BorderLayout.NORTH);
                 icPanel.add(new JScrollPane(icSetList), BorderLayout.CENTER);
                 icPanel.add(jsp, BorderLayout.EAST);
                 icPanel.add(icControlsPanel, BorderLayout.SOUTH);
                 
                 addOnsPane.addTab(getMessage("icons"), guiIcons.getIconIcons(), icPanel);
                 
                 JPanel onlineControlsPanel = new JPanel(new GridLayout(1,5));
                 onlineControlsPanel.add(onlineRefreshButt);
                 onlineControlsPanel.add(onlineDetailsButt);
                 onlineControlsPanel.add(onlineInstallButt);
                 onlineControlsPanel.add(onlineSelectAllUpdatesButt);
                 onlineControlsPanel.add(onlineCancelInstallButt);
                 JPanel onlinePanel = new JPanel(new BorderLayout());
                 JPanel onlineTopPanel = new JPanel(new BorderLayout());
                 onlineTopPanel.add(new JLabel(getMessage("filter")), BorderLayout.NORTH);
                 final JTextField onlineFilterText = new JTextField();
                 onlineFilterText.addCaretListener(new CaretListener(){
                     public void caretUpdate(CaretEvent e) {
                         onlineSorter.setRowFilter(RowFilter.regexFilter("(?i)" + onlineFilterText.getText()));
                     }
                 });
                 onlineTopPanel.add(onlineFilterText, BorderLayout.CENTER);
                 onlinePanel.add(onlineTopPanel, BorderLayout.NORTH);
                 onlinePanel.add(new JScrollPane(onlineList), BorderLayout.CENTER);
                 JPanel p = new JPanel(new BorderLayout());
                 p.add(onlineProgressPanel, BorderLayout.NORTH);
                 p.add(getOnlineShowAllPackagesCheckBox(), BorderLayout.CENTER);
                 p.add(onlineControlsPanel, BorderLayout.SOUTH);
                 onlinePanel.add(p, BorderLayout.SOUTH);
                 
                 addOnsPane.addTab(getMessage("online"), guiIcons.getIconOnline(), onlinePanel);
                 
                 JPanel advPanel = new JPanel(new BorderLayout());
 
                 JPanel libsPanel = new JPanel(new BorderLayout());
                 libsPanel.add(new JLabel(getMessage("registered.libraries")), BorderLayout.NORTH);
                 libsPanel.add(new JScrollPane(libList), BorderLayout.CENTER);
                 advPanel.add(libsPanel, BorderLayout.CENTER);
                 
                 JPanel advBottomPanel = new JPanel();
                 JPanel cleanPanel = null;
                 if (BackEnd.getInstance().unusedAddOnDataAndConfigFilesFound() && !unusedAddOnDataAndConfigFilesCleanedUp) {
                     cleanPanel = new JPanel(new BorderLayout());
                     final JButton cleanButt = new JButton(getMessage("clean.unused.data.and.config.files"));
                     JLabel cleanLabel = new JLabel(
                             Constants.HTML_PREFIX +
                             Constants.HTML_COLOR_HIGHLIGHT_WARNING +
                             getMessage("info.message.clean.unused.data.and.config.files") +
                             Constants.HTML_COLOR_SUFFIX +
                             Constants.HTML_SUFFIX);
                     cleanButt.addActionListener(new ActionListener(){
                         public void actionPerformed(ActionEvent e) {
                             BackEnd.getInstance().removeUnusedAddOnDataAndConfigFiles();
                             cleanButt.setText(getMessage("clean.unused.data.and.config.files") + " [ " + getMessage("done") + " ]");
                             cleanButt.setEnabled(false);
                             unusedAddOnDataAndConfigFilesCleanedUp = true;
                         }
                     });
                     cleanPanel.add(cleanButt, BorderLayout.NORTH);
                     cleanPanel.add(cleanLabel, BorderLayout.CENTER);
                 }
 
                 JPanel uninstLisbPanel = new JPanel(new BorderLayout());
                 final JButton detectButt = new JButton(getMessage("detect.unused.libraries"));
                 final JButton cleanButt = new JButton(getMessage("uninstall.unused.libraries"));
                 cleanButt.setEnabled(false);
                 final JLabel cleanLabel = new JLabel(
                         Constants.HTML_PREFIX +
                         Constants.HTML_COLOR_HIGHLIGHT_WARNING +
                         getMessage("info.message.uninstall.unused.libraries") +
                         Constants.HTML_COLOR_SUFFIX +
                         Constants.HTML_SUFFIX);
                 cleanLabel.setVisible(false);
                 detectButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         try {
                             Collection<String> deps = new ArrayList<String>();
                             for (AddOnInfo addOnInfo : BackEnd.getInstance().getAddOns()) {
                                 if (addOnInfo.getDependencies() != null) {
                                     for (Dependency dep : addOnInfo.getDependencies()) {
                                         deps.add(dep.getName());
                                     }
                                 }
                             }
                             cleanButt.setEnabled(false);
                             cleanLabel.setVisible(false);
                             for (int i = 0; i < getLibModel().getRowCount(); i++) {
                                 String libName = (String) getLibModel().getValueAt(i, 0);
                                 if (!deps.contains(libName)) {
                                     getLibModel().setValueAt(Constants.ADDON_STATUS.Unused, i, 4);
                                     cleanButt.setText(getMessage("uninstall.unused.libraries"));
                                     cleanButt.setEnabled(true);
                                     cleanLabel.setVisible(true);
                                 }
                             }
                         } catch (Throwable t) {
                             displayErrorMessage(getMessage("error.message.unused.libraries.detection.failure") + Constants.BLANK_STR + CommonUtils.getFailureDetails(t), t);
                         }
                     }
                 });
                 cleanButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         try {
                             int i = 0;
                             while  (i < getLibModel().getRowCount()) {
                                 if (getLibModel().getValueAt(i, 4).equals(Constants.ADDON_STATUS.Unused)) {
                                     String lib = (String) getLibModel().getValueAt(i, 0);
                                     BackEnd.getInstance().uninstallAddOn(lib, PackType.LIBRARY);
                                     getLibModel().removeRow(i);
                                     i = 0;
                                 } else {
                                     i++;
                                 }
                             }
                             cleanButt.setText(getMessage("uninstall.unused.libraries") + " [ " + getMessage("done") + " ]");
                             cleanButt.setEnabled(false);
                             cleanLabel.setVisible(false);
                         } catch (Throwable t) {
                             displayErrorMessage(getMessage("error.message.unused.libraries.uninstall.failure") + Constants.BLANK_STR + CommonUtils.getFailureDetails(t), t);
                         }
                     }
                 });
                 JPanel bp = new JPanel(new GridLayout(1,4));
                 bp.add(libDetailsButt);
                 bp.add(libInstButt);
                 bp.add(detectButt);
                 bp.add(cleanButt);
                 uninstLisbPanel.add(bp, BorderLayout.CENTER);
                 uninstLisbPanel.add(cleanLabel, BorderLayout.SOUTH);
                 
                 advBottomPanel.setLayout(new GridLayout(cleanPanel != null ? 2 : 1, 1));
                 advBottomPanel.add(uninstLisbPanel);
                 if (cleanPanel != null) advBottomPanel.add(cleanPanel);
                 
                 advPanel.add(advBottomPanel, BorderLayout.SOUTH);
                 
                 addOnsPane.addTab(getMessage("advanced.options"), guiIcons.getIconPreferences(), advPanel);
                 
                 JButton doneButt = new JButton(getMessage("done"));
                 doneButt.addActionListener(new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         addOnsManagementDialog.setVisible(false);
                     }
                 });
 
                 JPanel bottomPanel = new JPanel(new BorderLayout());
                 bottomPanel.add(getAddOnsManagementScreenProcessPanel(), BorderLayout.SOUTH);
                 bottomPanel.add(doneButt, BorderLayout.CENTER);
 
                 JPanel contentPane = new JPanel(new BorderLayout());
                 contentPane.add(addOnsPane, BorderLayout.CENTER);
                 contentPane.add(bottomPanel, BorderLayout.SOUTH);
                 
                 addOnsManagementDialog = new JFrame(getMessage("addons.management.title"));
                 addOnsManagementDialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                 addOnsManagementDialog.setContentPane(contentPane);
                 addOnsManagementDialog.pack();
                 int x = (getToolkit().getScreenSize().width - addOnsManagementDialog.getWidth()) / 2;
                 int y = (getToolkit().getScreenSize().height - addOnsManagementDialog.getHeight()) / 2;
                 addOnsManagementDialog.setLocation(x, y);
                 addOnsManagementDialog.setVisible(true);
                 
             } catch (Throwable t) {
                 displayErrorMessage(getMessage("error.message.addons.management.screen.initialization.failure"), t);
             }
         }
     }
     
     private JList getIconList() {
         if (icList == null) {
             icList = new JList(getIconListModel());
         }
         return icList;
     }
     
     private DefaultListModel getIconListModel() {
         if (icModel == null) {
             icModel = new DefaultListModel();
         }
         return icModel;
     }
     
     private DefaultTableModel getIconSetModel() {
         if (icSetModel == null) {
             icSetModel = new DefaultTableModel() {
                 private static final long serialVersionUID = 1L;
                 @Override
                 public boolean isCellEditable(int rowIndex, int mColIndex) {
                     return mColIndex == 0 ? true : false;
                 }
                 @Override
                 public Class<?> getColumnClass(int columnIndex) {
                     if (columnIndex == 0) {
                         return Boolean.class;
                     } else {
                         return super.getColumnClass(columnIndex);
                     }
                 }
             };
             icSetModel.addColumn(Constants.EMPTY_STR);
             icSetModel.addColumn(getMessage("name"));
             icSetModel.addColumn(getMessage("version"));
             icSetModel.addColumn(getMessage("author"));
             icSetModel.addColumn(getMessage("description"));
         }
         return icSetModel;
     }
     
     private DefaultTableModel getSkinModel() {
         if (skinModel == null) {
             skinModel = new DefaultTableModel() {
                 private static final long serialVersionUID = 1L;
                 @Override
                 public boolean isCellEditable(int rowIndex, int mColIndex) {
                     return mColIndex == 0 && !getValueAt(rowIndex, 1).equals(DEFAULT_SKIN) ? true : false;
                 }
                 @Override
                 public Class<?> getColumnClass(int columnIndex) {
                     if (columnIndex == 0) {
                         return Boolean.class;
                     } else {
                         return super.getColumnClass(columnIndex);
                     }
                 }
             };
             skinModel.addColumn(Constants.EMPTY_STR);
             skinModel.addColumn(getMessage("name"));
             skinModel.addColumn(getMessage("version"));
             skinModel.addColumn(getMessage("author"));
             skinModel.addColumn(getMessage("description"));
             skinModel.addColumn(getMessage("status"));
         }
         return skinModel;
     }
     
     private DefaultTableModel getExtensionsModel() {
         if (extModel == null) {
             extModel = new DefaultTableModel() {
                 private static final long serialVersionUID = 1L;
                 @Override
                 public boolean isCellEditable(int rowIndex, int mColIndex) {
                     return mColIndex == 0 ? true : false;
                 }
                 @Override
                 public Class<?> getColumnClass(int columnIndex) {
                     if (columnIndex == 0) {
                         return Boolean.class;
                     } else {
                         return super.getColumnClass(columnIndex);
                     }
                 }
             };
             extModel.addColumn(Constants.EMPTY_STR);
             extModel.addColumn(getMessage("name"));
             extModel.addColumn(getMessage("version"));
             extModel.addColumn(getMessage("author"));
             extModel.addColumn(getMessage("description"));
             extModel.addColumn(getMessage("status"));
         }
         return extModel;
     }
     
     private DefaultTableModel getLibModel() {
         if (libModel == null) {
             libModel = new DefaultTableModel() {
                 private static final long serialVersionUID = 1L;
                 @Override
                 public boolean isCellEditable(int rowIndex, int mColIndex) {
                     return false;
                 }
             };
             libModel.addColumn(getMessage("name"));
             libModel.addColumn(getMessage("version"));
             libModel.addColumn(getMessage("author"));
             libModel.addColumn(getMessage("description"));
             libModel.addColumn(getMessage("status"));
         }
         return libModel;
     }
     
     private DefaultTableModel getOnlineModel() {
         if (onlineModel == null) {
             onlineModel = new DefaultTableModel() {
                 private static final long serialVersionUID = 1L;
                 @Override
                 public boolean isCellEditable(int rowIndex, int mColIndex) {
                     return mColIndex == 0 
                                     && (!getValueAt(rowIndex, 1).equals(PackType.LIBRARY.value()) 
                                     || getValueAt(rowIndex, 7).equals(Constants.ADDON_STATUS.Update)) ? true : false;
                 }
                 @Override
                 public Class<?> getColumnClass(int columnIndex) {
                     if (columnIndex == 0) {
                         return Boolean.class;
                     } else {
                         return super.getColumnClass(columnIndex);
                     }
                 }
             };
             onlineModel.addColumn(Constants.EMPTY_STR);
             onlineModel.addColumn(getMessage("type"));
             onlineModel.addColumn(getMessage("name"));
             onlineModel.addColumn(getMessage("version"));
             onlineModel.addColumn(getMessage("author"));
             onlineModel.addColumn(getMessage("description"));
             onlineModel.addColumn(getMessage("size"));
             onlineModel.addColumn(getMessage("status"));
         }
         return onlineModel;
     }
     
     private JProgressBar getStatusBarProgressBar() {
         if (transferProgressBar == null) {
             transferProgressBar = new JProgressBar(SwingConstants.HORIZONTAL);
             transferProgressBar.setStringPainted(true);
             transferProgressBar.setMinimum(0);
             transferProgressBar.setString(Constants.EMPTY_STR);
         }
         return transferProgressBar;
     }
     
     private JProgressBar getOnlineSingleProgressBar() {
         if (onlineSingleProgressBar == null) {
             onlineSingleProgressBar = new JProgressBar(SwingConstants.HORIZONTAL);
             onlineSingleProgressBar.setStringPainted(true);
             onlineSingleProgressBar.setMinimum(0);
             onlineSingleProgressBar.setString(Constants.EMPTY_STR);
         }
         return onlineSingleProgressBar;
     }
     
     private JProgressBar getOnlineTotalProgressBar() {
         if (onlineTotalProgressBar == null) {
             onlineTotalProgressBar = new JProgressBar(SwingConstants.HORIZONTAL);
             onlineTotalProgressBar.setStringPainted(true);
             onlineTotalProgressBar.setMinimum(0);
             onlineTotalProgressBar.setString(Constants.EMPTY_STR);
         }
         return onlineTotalProgressBar;
     }
     
     private void downloadAndInstallAllUpdates(final Runnable onFinishAction) {
         Runnable updateTask = new Runnable(){
             public void run() {
                 if (updatesAvailable()) {
                     selectAllUpdates();
                     downloadAndInstallOnlinePackages(onFinishAction);
                 }
             }
         };
         refreshOnlinePackagesList(updateTask, getOnlineShowAllPackagesCheckBox().isSelected());
     }
     
     private boolean updatesAvailable() {
         for (int i = 0; i < getOnlineModel().getRowCount(); i++) {
             if (Constants.ADDON_STATUS.Update.equals(getOnlineModel().getValueAt(i, 7))) {
                 return true;
             }
         }
         return false;
     }
     
     private void selectAllUpdates() {
         for (int i = 0; i < getOnlineModel().getRowCount(); i++) {
             if (Constants.ADDON_STATUS.Update.equals(getOnlineModel().getValueAt(i, 7))) {
                 getOnlineModel().setValueAt(Boolean.TRUE, i, 0);
             }
         }
     }
 
     private void installLocalPackages(final AddOnFileChooser addOnFileChooser, final PackType addOnType, final DefaultTableModel addOnModel) {
         if (addOnFileChooser.showOpenDialog(getActiveWindow()) == JFileChooser.APPROVE_OPTION) {
             syncExecute(new Runnable(){
                 public void run() {
                     try {
                         displayProcessNotification(getMessage("info.message.packages.installation"), true);
                         final Map<AddOnInfo, File> proposedAddOnsToInstall = new HashMap<AddOnInfo, File>();
                         StringBuffer sb = new StringBuffer(Constants.HTML_PREFIX + "<ul>");
                         boolean error = false;
                         for (File file : addOnFileChooser.getSelectedFiles()) {
                             try {
                                 AddOnInfo installedAddOn = BackEnd.getInstance().getAddOnInfoAndDependencies(file, addOnType);
                                 proposedAddOnsToInstall.put(installedAddOn, file);
                             } catch (Throwable t) {
                                 error = true;
                                 sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_ERROR + getMessage("error.message.addon.info.read.from.file.failure") + " '" + file.getName() + "': " + CommonUtils.getFailureDetails(t) + Constants.HTML_COLOR_SUFFIX + "</li>");
                                 t.printStackTrace(System.err);
                             }
                         }
                         if (error) {
                             sb.append("</ul>" + Constants.HTML_SUFFIX);
                             JOptionPane.showMessageDialog(getActiveWindow(), new JScrollPane(new JLabel(sb.toString())));
                         }
                         if (!proposedAddOnsToInstall.isEmpty()) {
                             Collection<AddOnInfo> confirmedAddOnsToInstall = confirmAddOnsInstallation(proposedAddOnsToInstall.keySet());
                             proposedAddOnsToInstall.keySet().retainAll(confirmedAddOnsToInstall);
                             if (!proposedAddOnsToInstall.isEmpty()) {
                                 // check if there're dependency-packages present...
                                 boolean depsPresent = false;
                                 for (Integer i : getDepCounters().values()) {
                                     if (i > 0) {
                                         depsPresent = true;
                                         break;
                                     }
                                 }
                                 // ... if yes...
                                 if (depsPresent) {
                                     // ... check if dependencies should be installed from online list
                                     boolean onlineDeps = false;
                                     for (int i = 0; i < getOnlineModel().getRowCount(); i++) {
                                         if ((Boolean) getOnlineModel().getValueAt(i, 0)) {
                                             onlineDeps = true;
                                             break;
                                         }
                                     }
                                     // ... if yes...
                                     if (onlineDeps) {
                                         // ... remember currently active tab...
                                         final int activeTabIdx = addOnsPane.getSelectedIndex();
                                         // ... then switch to "Online" tab...
                                         addOnsPane.setSelectedIndex(3);
                                         // ... download dependency-packages...
                                         downloadAndInstallOnlinePackages(new Runnable(){
                                             public void run() {
                                                 // ... then switch back to the previously active tab...
                                                 addOnsPane.setSelectedIndex(activeTabIdx);
                                                 // ... and finally, install local packages...
                                                 installLocalPackages(proposedAddOnsToInstall, addOnType, addOnModel);
                                             }
                                         });
                                     } else {
                                         // ... otherwise, just install local packages
                                         installLocalPackages(proposedAddOnsToInstall, addOnType, addOnModel);
                                     }
                                 } else {
                                     // ... otherwise, just install local packages
                                     installLocalPackages(proposedAddOnsToInstall, addOnType, addOnModel);
                                 }
                             }
                         }
                     } finally {
                         hideProcessNotification();
                     }
                 }
             }, getMessage("info.message.local.packages.installation"), true);
         }
     }
     
     private void installLocalPackages(Map<AddOnInfo, File> proposedAddOnsToInstall, PackType addOnType, DefaultTableModel addOnModel) {
         StringBuffer sb = new StringBuffer(Constants.HTML_PREFIX + "<ul>");
         for (Entry<AddOnInfo, File> addons : proposedAddOnsToInstall.entrySet()) {
             try {
                 boolean lib = addOnModel.equals(getLibModel());
                 AddOnInfo installedAddOn = BackEnd.getInstance().installAddOn(addons.getValue(), addOnType);
                 ADDON_STATUS status = BackEnd.getInstance().getNewAddOns(addOnType).get(installedAddOn);
                 int idx = findDataRowIndex(addOnModel, lib ? 0 : 1, installedAddOn.getName());
                 if (idx != -1) {
                     addOnModel.removeRow(idx);
                     addOnModel.insertRow(idx, getAddOnInfoRow(lib ? null : Boolean.FALSE, installedAddOn, status));
                 } else {
                     addOnModel.addRow(getAddOnInfoRow(lib ? null : Boolean.FALSE, installedAddOn, status));
                 }
                 sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_OK + getMessage("success.message.addon.installed", installedAddOn.getName(), installedAddOn.getVersion()) + Constants.HTML_COLOR_SUFFIX + "</li>");
             } catch (Throwable t) {
                 sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_ERROR + getMessage("error.message.addon.installation.failure", addons.getKey().getName(), addons.getKey().getVersion()) + Constants.HTML_COLOR_SUFFIX + "</li>");
                 t.printStackTrace(System.err);
             }
         }
         sb.append("</ul>" + Constants.HTML_SUFFIX);
         JOptionPane.showMessageDialog(getActiveWindow(), new JScrollPane(new JLabel(sb.toString())));
     }
     
     private boolean onlineListRefreshed = false;
     
     private void refreshOnlinePackagesList(final Runnable onCompleteAction, final boolean showAll) {
         try {
             while (getOnlineModel().getRowCount() > 0) {
                 getOnlineModel().removeRow(0);
             }
             URL addonsListURL = new URL(BackEnd.getInstance().getRepositoryBaseURL().toString() + Constants.ONLINE_REPOSITORY_DESCRIPTOR_FILE_NAME);
             final File file = new File(Constants.TMP_DIR, Constants.ONLINE_REPOSITORY_DESCRIPTOR_FILE_NAME);
             Downloader d = Downloader.createSingleFileDownloader(addonsListURL, file, Preferences.getInstance().preferredTimeOut);
             displayProcessNotification(getMessage("info.message.refreshing.online.packages.list"), true);
             d.setDownloadListener(new DownloadListener(){
                 @Override
                 public void onComplete(URL url, File file, long downloadedBytesNum, long elapsedTime) {
                     try {
                         for (Pack pack : ((Repository) getUnmarshaller().unmarshal(file)).getPack()) {
                             if (pack.getType() != null 
                                     && !Validator.isNullOrBlank(pack.getName()) 
                                     && pack.getFileSize() != null
                                     && pack.getVersion() != null 
                                     && pack.getVersion().matches(VersionComparator.VERSION_PATTERN)) {
                                 Boolean isInstalledAndUpToDate = BackEnd.getInstance().isAddOnInstalledAndUpToDate(pack);
                                 Object[] row = null;
                                 if (isInstalledAndUpToDate == null) {
                                     row = getPackRow(pack, Constants.ADDON_STATUS.New);
                                 } else if (!isInstalledAndUpToDate) {
                                     row = getPackRow(pack, Constants.ADDON_STATUS.Update);
                                 } else if (showAll) {
                                     row = getPackRow(pack, null);
                                 }
                                 if (row != null) {
                                     getAvailableOnlinePackages().put(pack.getName(), pack);
                                     getOnlineModel().addRow(row);
                                 }
                             }
                         }
                         onlineListRefreshed = true;
                         if (states != null) states.clear();
                         if (onCompleteAction != null) {
                             onCompleteAction.run();
                         }
                     } catch (Throwable t) {
                         displayErrorMessage(getMessage("error.message.packages.list.parse.failure"), t);
                     }
                 }
                 @Override
                 public void onFailure(URL url, File file, Throwable failure) {
                     displayErrorMessage(getMessage("error.message.packages.list.retrieve.failure"), failure);
                 }
                 @Override
                 public void onCancel(URL url, File file, long downloadedBytesNum, long elapsedTime) {
                     JOptionPane.showMessageDialog(getActiveWindow(), getMessage("info.message.packages.list.refresh.canceled.by.user"));
                 }
                 @Override
                 public void onFinish(long downloadedBytesNum, long elapsedTime) {
                     hideProcessNotification();
                 }
             });
             d.start();
         } catch (Exception ex) {
             displayErrorMessage(getMessage("error.message.repository.url.resolution.failure") + Constants.BLANK_STR + CommonUtils.getFailureDetails(ex), ex);
         }
     }
     
     // TODO [P2] discard addon installation if at least one of it's dependencies failed to install ?...
     private void downloadAndInstallOnlinePackages(final Runnable onFinishAction) {
         try {
             final Map<URL, Pack> urlPackageMap = new HashMap<URL, Pack>();
             final Map<URL, File> urlFileMap = new LinkedHashMap<URL, File>();
             Collection<Integer> depIndexes = new ArrayList<Integer>();
             long tSize = 0;
             for (String dep : getDepCounters().keySet()) {
                 int idx = findDataRowIndex(getOnlineModel(), 2, dep);
                 if (idx != -1) {
                     if ((Boolean) getOnlineModel().getValueAt(idx, 0)) {
                         Pack pack = getAvailableOnlinePackages().get((String) getOnlineModel().getValueAt(idx, 2));
                         String fileName = pack.getName() + (pack.getVersion() != null ? Constants.VALUES_SEPARATOR + pack.getVersion() : Constants.EMPTY_STR) + Constants.JAR_FILE_SUFFIX;
                         URL url;
                         if (!Validator.isNullOrBlank(pack.getUrl())) {
                             url = new URL(pack.getUrl());
                         } else {
                             url = new URL(BackEnd.getInstance().getRepositoryBaseURL() + pack.getName() + Constants.PATH_SEPARATOR + fileName);
                         }
                         File file = new File(Constants.TMP_DIR, fileName);
                         urlFileMap.put(url, file);
                         urlPackageMap.put(url, pack);
                         tSize += pack.getFileSize();
                         depIndexes.add(idx);
                     }
                 }
             }
             for (int i = 0; i < getOnlineModel().getRowCount(); i++) {
                 if ((Boolean) getOnlineModel().getValueAt(i, 0) && !depIndexes.contains(i)) {
                     Pack pack = getAvailableOnlinePackages().get((String) getOnlineModel().getValueAt(i, 2));
                     String fileName = pack.getName() + (pack.getVersion() != null ? Constants.VALUES_SEPARATOR + pack.getVersion() : Constants.EMPTY_STR) + Constants.JAR_FILE_SUFFIX;
                     URL url;
                     if (!Validator.isNullOrBlank(pack.getUrl())) {
                         url = new URL(pack.getUrl());
                     } else {
                         url = new URL(BackEnd.getInstance().getRepositoryBaseURL() + pack.getName() + Constants.PATH_SEPARATOR + fileName);
                     }
                     File file = new File(Constants.TMP_DIR, fileName);
                     urlFileMap.put(url, file);
                     urlPackageMap.put(url, pack);
                     tSize += pack.getFileSize();
                 }
             }
             final Long totalSize = new Long(tSize);
             if (!urlFileMap.isEmpty()) {
                 Downloader d = Downloader.createMultipleFilesDownloader(urlFileMap, Preferences.getInstance().preferredTimeOut);
                 displayProcessNotification(getMessage("info.message.downloading.and.installing.packages"), true);
                 d.setDownloadListener(new DownloadListener(){
                     private StringBuffer sb = new StringBuffer();
                     boolean launcherUpdated = false;
                     boolean success = true;
                     @Override
                     public void onStart(URL url, File file) {
                         Pack pack = urlPackageMap.get(url);
                         if (pack.getFileSize() != null) {
                             getOnlineSingleProgressBar().setMaximum(pack.getFileSize().intValue());
                         }
                     };
                     @Override
                     public void onSingleProgress(URL url, File file, long downloadedBytesNum, long elapsedTime) {
                         Pack pack = urlPackageMap.get(url);
                         getOnlineSingleProgressBar().setValue((int) downloadedBytesNum);
                         getOnlineSingleProgressBar().setString(pack.getName() + Constants.BLANK_STR + pack.getVersion() 
                                 + " (" + FormatUtils.formatByteSize(downloadedBytesNum) + " / " + FormatUtils.formatByteSize(pack.getFileSize()) + ")");
                     };
                     @Override
                     public void onTotalProgress(int itemNum, long downloadedBytesNum, long elapsedTime) {
                         getOnlineTotalProgressBar().setValue((int) downloadedBytesNum);
                         double estimationCoef = ((double) totalSize) / ((double) downloadedBytesNum);
                         long estimationTime = (long) (elapsedTime * estimationCoef - elapsedTime);
                         getOnlineTotalProgressBar().setString(itemNum + " / " + urlFileMap.size() 
                                 + " (" + FormatUtils.formatByteSize(downloadedBytesNum) + " / " + FormatUtils.formatByteSize(totalSize) + ")"
                                 + ", " + getMessage("elapsed.time") + ": " + FormatUtils.formatTimeDuration(elapsedTime) 
                                 + ", " + getMessage("estimated.time.left") + ": " + FormatUtils.formatTimeDuration(estimationTime));
                     };
                     @Override
                     public void onComplete(URL url, File file, long downloadedBytesNum, long elapsedTime) {
                         Pack pack = urlPackageMap.get(url);
                         try {
                             if (pack.getType() == PackType.ICON_SET) {
                                 Collection<ImageIcon> icons = BackEnd.getInstance().addIcons(file);
                                 if (!icons.isEmpty()) {
                                     for (ImageIcon icon : icons) {
                                         getIconListModel().addElement(icon);
                                         FrontEnd.icons.put(icon.getDescription(), icon);
                                     }
                                     Collection<AddOnInfo> iconSets = BackEnd.getInstance().getIconSets();
                                     while (getIconSetModel().getRowCount() > 0) {
                                         getIconSetModel().removeRow(0);
                                     }
                                     for (AddOnInfo iconSetInfo : iconSets) {
                                         addOrReplaceTableModelAddOnRow(getIconSetModel(), iconSetInfo, true, 1, null);
                                     }
                                     sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_OK + getMessage("success.message.package.downloaded.and.installed", getMessage(pack.getType().value().toLowerCase()), pack.getName(), pack.getVersion()) + Constants.HTML_COLOR_SUFFIX + "</li>");
                                     getIconList().repaint();
                                 } else {
                                     sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_ERROR + getMessage(PackType.ICON_SET.value().toLowerCase()) + " '" + pack.getName() + Constants.BLANK_STR + pack.getVersion() + "' - " + getMessage("warning.message.nothing.to.install") + Constants.HTML_COLOR_SUFFIX + "</li>");
                                 }
                             } else if (pack.getType() == PackType.LIBRARY) {
                                 AddOnInfo libInfo = new AddOnInfo();
                                 libInfo.setName(pack.getName());
                                 libInfo.setVersion(pack.getVersion());
                                 libInfo.setDescription(pack.getDescription());
                                 libInfo.setAuthor(pack.getAuthor());
                                 if (pack.getDependency() != null) {
                                     libInfo.addAllDependencies(pack.getDependency());
                                 }
                                 BackEnd.getInstance().installAddOn(file, PackType.LIBRARY);
                                 ADDON_STATUS status = BackEnd.getInstance().getNewAddOns(PackType.LIBRARY).get(libInfo);
                                 addOrReplaceTableModelAddOnRow(getLibModel(), libInfo, false, 0, status);
                                 sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_OK + getMessage("success.message.package.downloaded.and.installed", getMessage(pack.getType().value().toLowerCase()), pack.getName(), pack.getVersion()) + Constants.HTML_COLOR_SUFFIX + "</li>");
                             } else if (pack.getType() == PackType.APP_CORE) {
                                 BackEnd.getInstance().installAppCoreUpdate(file, pack.getVersion());
                                 sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_OK + getMessage("success.message.package.downloaded.and.installed", getMessage(pack.getType().value().toLowerCase()), pack.getName(), pack.getVersion()) + Constants.HTML_COLOR_SUFFIX + "</li>");
                             } else if (pack.getType() == PackType.APP_LAUNCHER) {
                                 BackEnd.getInstance().installAppLauncherUpdate(file, pack.getVersion());
                                 sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_OK + getMessage("success.message.package.downloaded.and.installed", getMessage(pack.getType().value().toLowerCase()), pack.getName(), pack.getVersion()) + Constants.HTML_COLOR_SUFFIX + "</li>");
                                 launcherUpdated = true;
                             } else {
                                 AddOnInfo installedAddOn = BackEnd.getInstance().installAddOn(file, pack.getType());
                                 ADDON_STATUS status = BackEnd.getInstance().getNewAddOns(pack.getType()).get(installedAddOn);
                                 DefaultTableModel model = pack.getType() == PackType.EXTENSION ? getExtensionsModel() : getSkinModel();
                                 int idx = findDataRowIndex(model, 1, installedAddOn.getName());
                                 if (idx != -1) {
                                     model.removeRow(idx);
                                     model.insertRow(idx, getAddOnInfoRow(Boolean.FALSE, installedAddOn, status));
                                 } else {
                                     model.addRow(getAddOnInfoRow(Boolean.FALSE, installedAddOn, status));
                                 }
                                 sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_OK + getMessage("success.message.package.downloaded.and.installed", getMessage(pack.getType().value().toLowerCase()), pack.getName(), pack.getVersion()) + Constants.HTML_COLOR_SUFFIX + "</li>");
                             }
                         } catch (Throwable t) {
                             sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_ERROR + getMessage("error.message.package.download.or.installation.failure", getMessage(pack.getType().value().toLowerCase()), pack.getName(), pack.getVersion()) + Constants.HTML_COLOR_SUFFIX + "</li>");
                             t.printStackTrace(System.err);
                         }
                     }
                     @Override
                     public void onFailure(URL url, File file, Throwable failure) {
                         success = false;
                         Pack pack = urlPackageMap.get(url);
                         sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_ERROR + "'" + pack.getName() + "' - " + getMessage("error.message.failed.to.retrieve.package") + Constants.HTML_COLOR_SUFFIX + "</li>");
                         failure.printStackTrace(System.err);
                     }
                     @Override
                     public void onFinish(long downloadedBytesNum, long elapsedTime) {
                         hideProcessNotification();
                         if (!Validator.isNullOrBlank(sb)) {
                             JOptionPane.showMessageDialog(
                                     getActiveWindow(), 
                                     new JScrollPane(new JLabel(Constants.HTML_PREFIX + "<ul>" + sb.toString() + "</ul>" + Constants.HTML_SUFFIX)));
                             if (launcherUpdated) {
                                 JOptionPane.showMessageDialog(
                                         getActiveWindow(), 
                                         new JLabel(
                                                 Constants.HTML_PREFIX + Constants.HTML_COLOR_HIGHLIGHT_INFO + 
                                                 getMessage("info.message.launcher.updated", Constants.ROOT_DIR.toString()) + 
                                                 Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX));
                             }
                             if (success && onFinishAction != null) {
                                 onFinishAction.run();
                             }
                         }
                         getDepCounters().clear();
                         refreshOnlinePackagesList(null, getOnlineShowAllPackagesCheckBox().isSelected());
                     }
                     @Override
                     public void onCancel(URL url, File file, long downloadedBytesNum, long elapsedTime) {
                         success = false;
                         Pack pack = urlPackageMap.get(url);
                         sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_ERROR + getMessage("info.message.package.download.and.installation.canceled.by.user", pack.getName(), pack.getVersion()) + Constants.HTML_COLOR_SUFFIX + "</li>");
                         JOptionPane.showMessageDialog(getActiveWindow(), getMessage("info.message.packages.download.and.installation.canceled.by.user"));
                     }
                 });
                 getOnlineTotalProgressBar().setMaximum(totalSize.intValue());
                 d.start();
             }    
         } catch (Exception ex) {
             displayErrorMessage(getMessage("error.message.repository.url.resolution.failure") + Constants.BLANK_STR + CommonUtils.getFailureDetails(ex), ex);
         }
     }
     
     private Collection<AddOnInfo> confirmAddOnsInstallation(Collection<AddOnInfo> addOnInfos) {
         final Map<String, AddOnInfo> proposedAddOnsToInstall = new HashMap<String, AddOnInfo>();
         Collection<AddOnInfo> addOnsToInstall = new ArrayList<AddOnInfo>();
         final DefaultTableModel addOnModel = new DefaultTableModel() {
             private static final long serialVersionUID = 1L;
             @Override
             public boolean isCellEditable(int rowIndex, int mColIndex) {
                 return mColIndex == 0 ? true : false;
             }
             @Override
             public Class<?> getColumnClass(int columnIndex) {
                 if (columnIndex == 0) {
                     return Boolean.class;
                 } else {
                     return super.getColumnClass(columnIndex);
                 }
             }
         };
         final JTable addOnList = new JTable(addOnModel);
         
         onlineListRefreshed = false;
         final Map<String, Boolean> states = new HashMap<String, Boolean>();
         TableModelListener dependencyResolver = new TableModelListener(){
             private DefaultTableModel model;
             private int idx;
             public void tableChanged(final TableModelEvent e) {
                 if (e.getColumn() == 0) {
                     try {
                         Boolean lastState = states.get(addOnModel.getValueAt(e.getFirstRow(), 1));
                         if (lastState == null) lastState = false;
                         if ((Boolean) addOnModel.getValueAt(e.getFirstRow(), 0) != lastState) {
                             final AddOnInfo pack = proposedAddOnsToInstall.get(addOnModel.getValueAt(e.getFirstRow(), 1));
                             if (pack.getDependencies() != null && !pack.getDependencies().isEmpty()) {
                                 for (final Dependency dep : pack.getDependencies()) {
                                     if (!BackEnd.getInstance().getAddOns().contains(new AddOnInfo(dep.getName()))) {
                                         model = addOnModel;
                                         // search for dependency in list of addons to be installed...
                                         idx = findDataRowIndex(model, 1, dep.getName());
                                         
                                         Runnable task = new Runnable(){
                                             public void run() {
                                                 try {
                                                     if (idx == -1) {
                                                         model = getOnlineModel();
                                                         idx = findDataRowIndex(model, 2, dep.getName());
                                                     }
                                                     if (idx == -1) {
                                                         throw new Exception(
                                                                 getMessage("error.message.dependency.resolution.failure", 
                                                                         pack.getName(), 
                                                                         dep.getType().value(), 
                                                                         dep.getName(), 
                                                                         dep.getVersion() != null ? getMessage("version.x.or.higher", dep.getVersion()) : Constants.EMPTY_STR)
                                                                 );
                                                     } else {
                                                         synchronized (FrontEnd.this) {
                                                             Integer counter = getDepCounters().get(dep.getName());
                                                             if (counter == null) {
                                                                 counter = 0;
                                                             }
                                                             if ((Boolean) addOnModel.getValueAt(e.getFirstRow(), 0)) {
                                                                 counter++;
                                                                 if (counter > 0) {
                                                                     model.setValueAt(Boolean.TRUE, idx, 0);
                                                                 }
                                                             } else {
                                                                 if (counter > 0) {
                                                                     counter--;
                                                                     if (counter == 0) {
                                                                         model.setValueAt(Boolean.FALSE, idx, 0);
                                                                     }
                                                                 }
                                                             }
                                                             getDepCounters().put(dep.getName(), counter);
                                                         }
                                                     }
                                                     states.put((String) addOnModel.getValueAt(e.getFirstRow(), 1), (Boolean) addOnModel.getValueAt(e.getFirstRow(), 0));
                                                 } catch (Throwable t) {
                                                     displayErrorMessage(getMessage("error.message.dependencies.handle.resolve.failure") + Constants.BLANK_STR + CommonUtils.getFailureDetails(t), t);
                                                 }
                                             }
                                         };
                                         
                                         if (idx == -1) {
                                             // ... if not found - search in online list 
                                             // (refresh online packages list, if needed, first, then try to resolve dependencies)
                                             if (!onlineListRefreshed) {
                                                 refreshOnlinePackagesList(task, getOnlineShowAllPackagesCheckBox().isSelected());
                                             } else {
                                                 task.run();
                                             }
                                         } else {
                                             task.run();
                                         }
                                     }
                                 }
                             }
                         }
                     } catch (Throwable t) {
                         displayErrorMessage(getMessage("error.message.dependencies.handle.resolve.failure") + Constants.BLANK_STR + CommonUtils.getFailureDetails(t), t);
                     }
                 }
             }
         };
         addOnModel.addTableModelListener(dependencyResolver);
         
         final TableRowSorter<TableModel> addOnSorter = new TableRowSorter<TableModel>(addOnModel);
         addOnSorter.setSortsOnUpdates(true);
         addOnList.setRowSorter(addOnSorter);
         addOnModel.addColumn(Constants.EMPTY_STR);
         addOnModel.addColumn(getMessage("name"));
         addOnModel.addColumn(getMessage("version"));
         addOnModel.addColumn(getMessage("author"));
         addOnModel.addColumn(getMessage("description"));
         for (AddOnInfo addOnInfo : addOnInfos) {
             addOnModel.addRow(getInstallAddOnInfoRow(addOnInfo));
             proposedAddOnsToInstall.put(addOnInfo.getName(), addOnInfo);
         }
         int opt = JOptionPane.showConfirmDialog(getActiveWindow(), new JScrollPane(addOnList), getMessage("addons.installation.confirmation"), JOptionPane.OK_CANCEL_OPTION);
         if (opt == JOptionPane.OK_OPTION) {
             for (int i = 0; i < addOnList.getRowCount(); i++) {
                 if ((Boolean) addOnList.getValueAt(i, 0)) {
                     addOnsToInstall.add(proposedAddOnsToInstall.get(addOnList.getValueAt(i, 1)));
                 }
             }
         }
         return addOnsToInstall;
     }
     
     private JTable getLibsList() throws Throwable {
         final JTable addOnList = new JTable(getLibModel());
         addOnList.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
         listLibs();
         addOnList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         final TableRowSorter<TableModel> addOnSorter = new TableRowSorter<TableModel>(getLibModel());
         addOnSorter.setSortsOnUpdates(true);
         addOnList.setRowSorter(addOnSorter);
         return addOnList;
     }
     
     private void addOrReplaceTableModelAddOnRow(DefaultTableModel addOnModel, AddOnInfo addOnInfo, boolean withCheckBox, int searchIdx, ADDON_STATUS status) {
         int idx = findDataRowIndex(addOnModel, searchIdx, addOnInfo.getName());
         if (idx != -1) {
             addOnModel.removeRow(idx);
             if (status != null 
                     && !status.equals(Constants.ADDON_STATUS.Installed) 
                     && !status.equals(Constants.ADDON_STATUS.Imported)) status = Constants.ADDON_STATUS.Updated;
             addOnModel.insertRow(idx, getAddOnInfoRow((withCheckBox ? Boolean.FALSE : null), addOnInfo, status));
         } else {
             addOnModel.addRow(getAddOnInfoRow((withCheckBox ? Boolean.FALSE : null), addOnInfo, status));
         }
     }
 
     private void loadAndDisplayPackageDetails(final URL baseURL, final URL addOnURL, final AddOnInfo addOnInfo) throws Throwable {
         loadAndDisplayPackageDetails(baseURL, addOnURL, addOnInfo.getName(), addOnInfo.getDependencies());
     }
     
     private void loadAndDisplayPackageDetails(final URL baseURL, final URL addOnURL, final Pack pack) throws Throwable {
         loadAndDisplayPackageDetails(baseURL, addOnURL, pack.getName(), pack.getDependency());
     }
     
     private void loadAndDisplayPackageDetails(final URL baseURL, final URL addOnURL, final String addOnName, final Collection<Dependency> dependencies) throws Throwable {
         boolean loaded = false;
         InputStream is = null;
         ByteArrayOutputStream baos = null;
         try {
             is = addOnURL.openStream();
             baos = new ByteArrayOutputStream();
             byte[] buffer = new byte[1024];
             int readBytesNum;
             while ((readBytesNum = is.read(buffer)) != -1) {
                 baos.write(buffer, 0, readBytesNum);
             }
             loaded = true;
         } finally {
                 try {
                     if (is != null) is.close();
                     if (baos != null) baos.close();
                 } catch (IOException e) {
                     // ignore
                 }
             if (loaded) {
                 try {
                     JPanel p = new JPanel(new BorderLayout());
                     p.add(getDetailsPane(new String(baos.toByteArray()), baseURL), BorderLayout.CENTER);
                     p.add(getDependenciesPanel(dependencies), BorderLayout.SOUTH);
                     displayDialog(p, addOnName + " :: " + getMessage("details"));
                 } catch (Throwable t) {
                     displayErrorMessage(getMessage("error.message.pack.details.page.load.failure") + Constants.BLANK_STR + CommonUtils.getFailureDetails(t), t);
                 }
             }
         }
     }
     
     public static int displayDialog(Object message, String title) {
         return displayDialog(message, title, -1);
     }
     
     public static int displayDialog(Object message, String title, int optionType) {
         JOptionPane op = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE);
         if (optionType != -1) op.setOptionType(optionType);
         final Dialog d = op.createDialog(getActiveWindow(), title);
         d.setLocation(getActiveWindow().getLocation());
         d.setSize(getActiveWindow().getSize());
         d.setVisible(true);
         return (Integer) op.getValue();
     }
     
     private int findDataRowIndex(DefaultTableModel model, int colIdx, String data) {
         for (int i = 0; i < model.getRowCount(); i++) {
             if (data.equals(model.getValueAt(i, colIdx))) {
                 return i;
             }
         }
         return -1;
     }
     
     private JScrollPane getDetailsPane(String detailsInfo) {
     	return getDetailsPane(detailsInfo, null);
     }
     
     private JScrollPane getDetailsPane(String detailsInfo, URL baseURL) {
         if (detailsPane == null) {
             detailsTextPane = new JTextPane();
             detailsTextPane.setEditable(false);
             detailsTextPane.setEditorKit(new CustomHTMLEditorKit());
             detailsTextPane.addHyperlinkListener(new HyperlinkListener() {
                 public void hyperlinkUpdate(HyperlinkEvent e) {
                     if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                         try {
                             AppManager.getInstance().handleAddress(e.getDescription());
                         } catch (Exception ex) {
                             FrontEnd.displayErrorMessage(ex);
                         }
                     }
                 }
             });
             detailsPane = new JScrollPane(detailsTextPane);
         }
         if (baseURL != null) {
         	((HTMLDocument) detailsTextPane.getDocument()).setBase(baseURL);
         }
         detailsTextPane.setText(detailsInfo);
         return detailsPane;
     }
     
     private JLabel getDependenciesPanel(Collection<Dependency> dependencies) throws Throwable {
         if (dependenciesLabel == null) {
             dependenciesLabel = new JLabel();
         }
         StringBuffer text = new StringBuffer();
         if (dependencies != null && !dependencies.isEmpty()) {
             text.append(getMessage("addon.dependencies") + ":<br/><ul>");
             for (Dependency dep : dependencies) {
                 AddOnInfo dependentAddOnInfo = BackEnd.getInstance().getAddOnInfo(dep.getName(), dep.getType());
                 ADDON_STATUS status = dep.getType() == PackType.APP_CORE ?
                 		(VersionComparator.getInstance().compare(BackEnd.getInstance().getAppCoreVersion(), dep.getVersion()) >= 0 ? 
                 				Constants.ADDON_STATUS.RegisteredInstalled : Constants.ADDON_STATUS.NotRegisteredInstalled) 
                 		:
                 		BackEnd.getInstance().isAddOnRegisteredAndInstalled(dep.getName(), dep.getType()) 
                         && (dep.getVersion() == null || VersionComparator.getInstance().compare(dependentAddOnInfo.getVersion(), dep.getVersion()) >= 0) ? 
                                 Constants.ADDON_STATUS.RegisteredInstalled : Constants.ADDON_STATUS.NotRegisteredInstalled; 
                 text.append(
                         "<li>" + 
                         getMessage(dep.getType().value().toLowerCase()) + " '" + dep.getName() + "' " + 
                         (Validator.isNullOrBlank(dep.getVersion()) ? Constants.BLANK_STR : getMessage("version.x.or.higher", dep.getVersion()) + Constants.BLANK_STR) + 
                         Constants.getAddOnStatusCaption(status) + 
                         "</li>");
             }
             text.append("</ul>");
         }
         if (Validator.isNullOrBlank(text)) {
             dependenciesLabel.setText(getMessage("addon.has.no.dependencies"));
         } else {
             dependenciesLabel.setText(Constants.HTML_PREFIX + text.toString() + Constants.HTML_SUFFIX);
         }
         return dependenciesLabel;
     }
     
     private HelpAction displayHelpAction = new HelpAction();
     private class HelpAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public HelpAction() {
             putValue(Action.NAME, "help");
             putValue(Action.SHORT_DESCRIPTION, getMessage("help"));
             putValue(Action.SMALL_ICON, guiIcons.getIconHelp());
         }
 
         public void actionPerformed(ActionEvent evt) {
             JPanel p = new JPanel(new BorderLayout());
             p.add(getDetailsPane(
             		"<html>" +
             		"<body>" +
             			getMessage("help.info") +
             		"</body>" +
             		"</html>"
             		), 
             		BorderLayout.CENTER);
             displayDialog(p, "Bias :: " + getMessage("help"));
         }
     }
 
     private AboutAction displayAboutInfoAction = new AboutAction();
     private class AboutAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         
         public AboutAction() {
             putValue(Action.NAME, "about");
             putValue(Action.SHORT_DESCRIPTION, getMessage("about.bias"));
             putValue(Action.SMALL_ICON, guiIcons.getIconAbout());
         }
 
         public void actionPerformed(ActionEvent evt) {
             JLabel title1Label = new JLabel(getMessage("app.title") + " [ " + getMessage("version") + Constants.BLANK_STR + BackEnd.getInstance().getAppCoreVersion() + " ]");
             JLabel link1Label = new LinkLabel("http://bias.sourceforge.net/");
             JLabel title2Label = new JLabel("© Roman Kasianenko ( kion )");
         	JLabel link2Label = new LinkLabel("http://kion.name/");
             JOptionPane.showMessageDialog(
                     FrontEnd.this, 
                     new Component[]{
                             title1Label, link1Label, 
                             title2Label, link2Label
                             },
                     getMessage("about.bias") + "...",
                     JOptionPane.PLAIN_MESSAGE,
                     ICON_LOGO);
         }
     }
 
 }
