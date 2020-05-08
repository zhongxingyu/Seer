 /*=====================================================*/
 /*                                                     */
 /* Copyright (c) University of Warwick 2006            */
 /*                                                     */
 /* Author T.R.Marsh                                    */
 /*=====================================================*/
 
 package warwick.marsh.ultracam.usdriver;
 
 import java.io.*;
 import javax.swing.*;
 import javax.swing.border.*;
 import javax.swing.table.AbstractTableModel;
 
 import java.lang.Integer;
 import java.util.StringTokenizer;
 import java.util.Properties;
 import java.util.Map;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import java.net.URL;
 import java.net.HttpURLConnection;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.net.MalformedURLException;
 import java.net.SocketException;
 import java.net.Socket;
 import java.net.ServerSocket;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import java.text.DecimalFormat;
 
 import org.w3c.dom.*;
 import org.xml.sax.*;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 
 import warwick.marsh.util.*;
 import warwick.marsh.ultracam.LogPanel;
 import warwick.marsh.ultracam.ReplyPanel;
 import warwick.marsh.ultracam.Telescope;
 
 /** Usdriver is a program to edit applications for ULTRASPEC and to drive it
  * The program has the following functionality:
  * <ul>
  * <li> Application definition (drift, 1 window pair etc) through a GUI
  * <li> Set binning factors and readout speed
  * <li> Set window positions and sizes
  * <li> Dump the application & window definition to local XML files that can
  * be later re-loaded to drive ULTRACAM/SPEC runs
  * <li> Act as a server for 'rtplot' so that the latter can grab the current
  * set of windows.
  * <li> Post XML applications to the ultracam camera and data servers
  * </ul>
  *
  * Various aspects of the program can be defined using the configuration file which can be set when
  * running Usdriver as a system property CONFIG_FILE. For example you can add -DCONFIG_FILE=my_setup_file
  * If you don't it defaults to usdriver.conf in whichever directory you run the program in.
  * 
  * @author  Tom Marsh
  *
  */
 
 public class Usdriver extends JFrame {
 
     // Telescope data. See the class for a full description of the fields
     // ZPs are taken from VSD's online calculator. At present they are for NTT. Need correction for
     // ESO3.6 and TNO (SL - 14/12/2011)
     // NOTE THAT VIK's SN CALCULATOR SEEMS TO SUGGEST THAT THESE ZPs give 1e-/s, not 1ADU/s. CHECK - SL
     private static final Telescope[] TELESCOPE_DATA = {
 	new Telescope("ESO3.6", new double[] {23.53, 26.07, 25.83, 25.51, 24.63}, 0.1055, "eso3.6.xml"),
 	new Telescope("NTT",    new double[] {23.53, 26.07, 25.83, 25.51, 24.63}, 0.1055, "ntt.xml"),
 	new Telescope("TNO",    new double[] {22.71, 25.25, 25.01, 24.69, 23.81}, 0.45,    "tno.xml")
     };
 
     // Timing parameters from Vik (units of seconds, or seconds/pixel)
     // VCLOCK changed from 9.12 to 14.4 musec 12/6/09 after changes made during NTT run (TRM)
     // updated with new timing parameters from Vik (SL 26/10/12)
     // further updated with new timing parameters from Vik (SL 22/02/13)
     public static final double VCLOCK = 14.4e-6; 
     public static final double HCLOCK_NORM = 0.48e-6;
     public static final double HCLOCK_AV   = 0.96e-6;
     public static final double VIDEO_NORM_SLOW = 11.20e-6;
     public static final double VIDEO_NORM_MED  =  6.24e-6;
     public static final double VIDEO_NORM_FAST =  3.20e-6;
     public static final double VIDEO_AV_SLOW   = 11.20e-6;
     public static final double VIDEO_AV_MED    =  6.24e-6;
     public static final double VIDEO_AV_FAST   =  3.20e-6;
     public static final int    FFX             = 1072;
     public static final int    FFY             = 1072;
     public static final int    IFY             = 1072;
     public static final int    IFX             = 1072;
     public static final int    AVALANCHE_PIXELS= 1072;
 
     // Instrument Noise/Gain Characteristics: from Vik's O/L signal/noise calculator - 14/12/2011 (SL)
     // Updated by SL (25/02/2013) to use new noise numbers from VSD
     private static final double   AVALANCHE_GAIN_9    = 1200.0;        // dimensionless gain, hvgain=9
     private static final double   AVALANCHE_SATURATE  = 80000;       // electrons
 
     // Note - avalanche gains assume HVGain = 9. We can adapt this later when we decide how
     // gain should be set at TNO. Might be better to make gain a function if we allow 0 < HVgain < 9 (SL)
     private static final double   GAIN_NORM_FAST = 0.8;             // electrons per count
     private static final double   GAIN_NORM_MED  = 0.7;             // electrons per count
     private static final double   GAIN_NORM_SLOW = 0.8;             // electrons per count
     private static final double   GAIN_AV_FAST   = 0.0034;           // electrons per count
     private static final double   GAIN_AV_MED    = 0.0013;           // electrons per count
     private static final double   GAIN_AV_SLOW   = 0.0016;           // electrons per count
 
     // Note - avalanche RNO assume HVGain = 9. We can adapt this later when we decide how
     // gain should be set at TNO. Might be better to make RNO a function if we allow 0 < HVgain < 9 (SL)
     private static final double   RNO_NORM_FAST  =  4.8;           // electrons per pixel
     private static final double   RNO_NORM_MED   =  2.8;           // electrons per pixel
     private static final double   RNO_NORM_SLOW  =  2.2;           // electrons per pixel
     private static final double   RNO_AV_FAST    = 16.5;           // electrons per pixel
     private static final double   RNO_AV_MED     =  7.8;           // electrons per pixel
     private static final double   RNO_AV_SLOW    =  5.6;           // electrons per pixel
 
     // other noise sources
     private static final double   DARK_E         =  0.001;         // e-/pix/sec
     private static final double   CIC            =  0.010;         // Clock induced charge, e-/pix
     
     //------------------------------------------------------------------------------------------
 
     // Sky parameters
 
     // Extinction, mags per unit airmass
     private static final double[] EXTINCTION = {0.50, 0.19, 0.09, 0.05, 0.04};
 
     // Sky brightness, mags/arcsec**2, dark, grey, bright, in ugriz
     private static final double[][] SKY_BRIGHT = {
 	{22.4, 22.2, 21.4, 20.7, 20.3},
 	{21.4, 21.2, 20.4, 20.1, 19.9},
 	{18.4, 18.2, 17.4, 17.9, 18.3}
     };
 
     //------------------------------------------------------------------------------------------
 
     // Generic application to initialise servers
     private static final String   GENERIC_APP  = "ultraspec.xml";
 
     // The following is used to pass the telescope data around
     private Telescope _telescope = null;
 
     // Colours
     public static final Color DEFAULT_COLOUR    = new Color(220, 220, 255);
     public static final Color SEPARATOR_BACK    = new Color(100, 100, 100);
     public static final Color SEPARATOR_FORE    = new Color(150, 150, 200);
     public static final Color LOG_COLOUR        = new Color(240, 230, 255);
     public static final Color ERROR_COLOUR      = new Color(255, 0,   0  );
     public static final Color WARNING_COLOUR    = new Color(255, 100, 0  );
     public static final Color GO_COLOUR         = new Color(0,   255, 0  );
     public static final Color STOP_COLOUR       = new Color(255, 0,   0  );
 
     // Font
     public static final Font DEFAULT_FONT = new Font("Dialog", Font.BOLD, 12);
 
     // Width for horizontal separator
     public static final int SEPARATOR_WIDTH = 5;
 
     private boolean _validStatus = true; 
     private boolean _magInfo     = true;
 
     // Exposure timer, active run timer, disk space display, checkRun
     // text fields for timing details
     // ActionListener that checks for run numbers
     private Timer      _exposureMeter    = null;
     private Timer      _runActive        = null;
     private JTextField _frameRate        = new JTextField("", 7);
     private JTextField _cycleTime        = new JTextField("", 7);
     private JTextField _expTime          = new JTextField("", 7);
     private JTextField _dutyCycle        = new JTextField("", 7);
     private JTextField _totalCounts      = new JTextField("", 7);
     private JTextField _peakCounts       = new JTextField("", 7);
     private JTextField _signalToNoise    = new JTextField("", 7);
     private JTextField _signalToNoiseOne = new JTextField("", 7);
     private JTextField _exposureTime     = new JTextField("0", 7);
     private JTextField _spaceUsed        = new JTextField("0", 7);
     private JTextField _runNumber        = new JTextField("", 7);
     private ActionListener _checkRun     = null;
     
     // Thresholds for changing colour of disk space 
     public static final int DISK_SPACE_WARN   = 1500;
     public static final int DISK_SPACE_DANGER = 1800;
     
     // These are used to store values from last posted application
     private int _nbytesPerImage  = 0;
     private int _nexposures      = 1;
 
     private int _filterIndex    = 1;
     private int _skyBrightIndex = 1;
 
     // run type variables
     private String _runType = "data";
     private boolean _acquisitionState = false;
 
     // Configuration file
     public static final String CONFIG_FILE = System.getProperty("CONFIG_FILE", "usdriver.conf");
 
     // Configurable values
     public static boolean RTPLOT_SERVER_ON;
     public static boolean ULTRACAM_SERVERS_ON;
     public static boolean OBSERVING_MODE;
     public static boolean DEBUG;
     public static boolean FILE_LOGGING_ON;
     public static String  TELESCOPE             = null;
     public static String  HTTP_CAMERA_SERVER    = null;
     public static String  HTTP_DATA_SERVER      = null;
     public static String  HTTP_PATH_GET         = null;
     public static String  HTTP_PATH_EXEC        = null;
     public static String  HTTP_PATH_CONFIG      = null;
     public static String  HTTP_SEARCH_ATTR_NAME = null;
 
     public static String  APP_DIRECTORY         = null;
     public static boolean XML_TREE_VIEW;
     public static boolean TEMPLATE_FROM_SERVER;
     public static String  TEMPLATE_DIRECTORY    = null;
     public static boolean EXPERT_MODE;
     public static String  LOG_FILE_DIRECTORY    = null;
     public static boolean CONFIRM_ON_CHANGE;
     public static boolean TEMP_FROM_LAKESHORE   = true;
     public static boolean FILTER_WHEEL_ON       = true;
     public static boolean SLIDE_ON       = true;
     public static boolean CONFIRM_HV_GAIN_ON;
     public static int SERVER_READBACK_VERSION	= 0;
     public static boolean USE_UAC_DB		= true;
     public static String  UAC_DATABASE_HOST;
 
     public static String   WINDOW_NAME          = new String("window");
     public static String[] TEMPLATE_LABEL       = null;
     public static String[] TEMPLATE_PAIR        = null;
     public static String[] TEMPLATE_APP         = null;
     public static String[] TEMPLATE_ID          = null;
     public static String   POWER_ON             = null;
 
     // Binning factors
     private int xbin    = 1;
     private int ybin    = 1;
     public static final int[] POSSIBLE_BIN_FACTORS = {1,2,3,4,5,6,8};
     private IntegerTextField xbinText = new IntegerTextField(xbin, 1, 8, 1, "X bin factor", true, DEFAULT_COLOUR, ERROR_COLOUR, 2);
     private IntegerTextField ybinText = new IntegerTextField(ybin, 1, 8, 1, "Y bin factor", true, DEFAULT_COLOUR, ERROR_COLOUR, 2);
 
     private JComboBox templateChoice;
     private int numEnable;
     
     private String applicationTemplate    = new String("Window mode");
     private String oldApplicationTemplate = new String("Window mode");
 
     // Readout speeds
     private static final String[] SPEED_LABELS = {
 	"Slow",
 	"Medium",
 	"Fast"
     };
     private JComboBox speedChoice;
     private String readSpeed = "Slow";
 
     // Exposure delay measured in 1 millisecond intervals
     // Like ULTRACAM allowing zero here crashes the system
     private int expose = 500;
     private IntegerTextField exposeText     = new IntegerTextField(expose, 0, 16777207, 1, "exposure, milliseconds", true, DEFAULT_COLOUR, ERROR_COLOUR, 6);
     private IntegerTextField tinyExposeText = new IntegerTextField(7, 0, 9, 1, "exposure increment, 0.1 milliseconds", true, DEFAULT_COLOUR, ERROR_COLOUR, 2);
     private int numExpose = -1;
     private IntegerTextField numExposeText = new IntegerTextField(numExpose, -1, 100000, 1, "Number of exposures", true, DEFAULT_COLOUR, ERROR_COLOUR, 6);
 
     // Standard number of windows 
     private int numWin = 2;
     private IntegerTextField numWinText = new IntegerTextField(numWin, 1, 4, 1, "Number of windows", true, DEFAULT_COLOUR, ERROR_COLOUR, 2);
 
     // High voltage gain
     private int hvGain = 0;
     private IntegerTextField hvGainText = new IntegerTextField(hvGain, 0, 9, 1, "Avalanche gain", true, DEFAULT_COLOUR, ERROR_COLOUR, 2);
     private static final String[] CCD_OUTPUT_LABELS = {
 	"Normal",
 	"Avalanche"
     };
     private JComboBox ccdOutputChoice;
     private String ccdOutput = "Normal";
 
     private JCheckBox clearEnabled = new JCheckBox();
     // stores old setting of clear for switching to/from drift mode
     private boolean  _wasClearEnabled = false;
     private JCheckBox driftModeEnabled = new JCheckBox();
 
     private int ledIntensity = 0;
     private IntegerTextField ledIntensityText = new IntegerTextField(ledIntensity, 0, 4095, 1, "LED setting", true, DEFAULT_COLOUR, ERROR_COLOUR, 4);
 
     // Labels for Single Windows
     private static JLabel windowsLabel = new JLabel("Windows");
     private static JLabel ystartLabel  = new JLabel("ystart");
     private static JLabel xstartLabel  = new JLabel("xstart");
     private static JLabel nxLabel      = new JLabel("nx");
     private static JLabel nyLabel      = new JLabel("ny");
 
     // Labels for WindowPairs(drift mode)
     private static JLabel ystartLabel2  = new JLabel("ystart");
     private static JLabel xleftLabel  = new JLabel("xleft");
     private static JLabel xrightLabel  = new JLabel("xright");
     private static JLabel nxLabel2      = new JLabel("nx");
     private static JLabel nyLabel2      = new JLabel("ny");
 
     private static JLabel hvGainLabel  = new JLabel("Avalanche gain");
 
     // Fields for user information
     private static String[] FILTER_NAMES = null;
     private static String[] FILTER_IDS   = null;
     private static String[] ACTIVE_FILTER_NAMES = null;
     private JComboBox _filter;
     private String defaultFilter = null;
     
     //private static JTextField _filtersText    = new JTextField("", 15);
     private static JTextField _objectText     = new JTextField("", 15);
     private static JTextField _gratingText    = new JTextField("", 15);
     private static JTextField _slitWidthText  = new JTextField("", 15);
     private static JTextField _slitAngleText  = new JTextField("", 15);
     private static JTextField _progidText     = new JTextField("", 15);
     private static JTextField _piText         = new JTextField("", 15);
     private static JTextField _observerText   = new JTextField("", 15);
     private static JTextField _commentText    = new JTextField("", 25);
     private static JRadioButton _dataButton = new JRadioButton("data");
     private static JRadioButton _acqButton = new JRadioButton("acquire");
     private static JRadioButton _biasButton = new JRadioButton("bias");
     private static JRadioButton _flatButton = new JRadioButton("flat");
     private static JRadioButton _darkButton = new JRadioButton("dark");
     private static JRadioButton _techButton = new JRadioButton("tech");
 
     // Fields for signal-to-noise estimates
     private static DoubleTextField _magnitudeText  = new DoubleTextField(18.0, 5.,  35., 0.1,  "Target magnitude",    true, DEFAULT_COLOUR, ERROR_COLOUR, 6);
     private static DoubleTextField _seeingText     = new DoubleTextField( 1.0, 0.2, 20., 0.1,  "Seeing, FWHM arcsec", true, DEFAULT_COLOUR, ERROR_COLOUR, 6);
     private static DoubleTextField _airmassText    = new DoubleTextField(1.5, 1.0, 5.0,  0.05, "Airmass", true, DEFAULT_COLOUR, ERROR_COLOUR, 6);
 
     // ULTRASPEC works with single windows
     private static SingleWindows _singleWindows;
     // or WindowPairs in the new drift mode app
     private static WindowPairs _windowPairs;
     public static final int[] specialNy = {15,17,22,23,24,28,45,61,69,94,115,148,207,344,345};
 
     private static JFileChooser _rtplotFileChooser;
     private static JFileChooser _xmlFileChooser;
     private static File        _rtplotFile = null;
     private static File        _xmlFile    = null;
     private static ReplyPanel  _replyPanel = null;
     public  static LogPanel     logPanel   = null;
 
     private DocumentBuilder _documentBuilder;
     private Transformer     _transformer;
     
     // Use this a fair bit, so just make one
     private static GridBagLayout gbLayout = new GridBagLayout();
 
     // To switch between setup & observing panels
     JTabbedPane _actionPanel = null;
     JPanel _expertSetupPanel = null;
     JPanel _noddySetupPanel  = null;
 
     // Action buttons associated with ULTRACAM servers
     private static JButton loadApp          = new JButton("Load application");
     private static JButton enableChanges    = new JButton("Unfreeze Usdriver");
     private static JButton syncWindows      = new JButton("Sync windows");
     private static JButton postApp          = new JButton("Post application");
     private static JButton startRun         = new JButton("Start exposure");
     private static JButton stopRun          = new JButton("Stop exposure");
     private static JButton setupAll         = new JButton("Initialise");
     private static JButton resetSDSU        = new JButton("Reset SDSU");
     private static JButton resetPCI         = new JButton("Reset PCI board");
     private static JButton setupServer      = new JButton("Setup the servers");
     private static JButton powerOn          = new JButton("Power on");
     private static JButton slideCon         = new JButton("Focal Plane Mask");
 
     // GUI components for a custom server command in expert mode
     private static JTextField _commandText  = new JTextField("",15);
     private static JButton execExpertCmd = new JButton("EXEC");
 
     // Maintain a record of whether buttons are or are not enabled independent
     // of whether they actually are to allow switching between expert and
     // non-expert modes
     private boolean postApp_enabled         = false;
     private boolean startRun_enabled        = false;
     private boolean stopRun_enabled         = false;
     private boolean resetSDSU_enabled       = true;
     private boolean resetPCI_enabled        = false;
     private boolean setupServer_enabled     = false;
     private boolean powerOn_enabled         = false;
     private boolean expertCmd_enabled       = false;
 
     // Settings menu items
     private JCheckBoxMenuItem _setExpert;
     private JCheckBoxMenuItem _templatesFromServer;
     private JCheckBoxMenuItem _ucamServersOn;
     private JCheckBoxMenuItem _fileLogging;
     private JCheckBoxMenuItem _responseAsText;
     private JCheckBoxMenuItem _confirmOnChange;
     private JCheckBoxMenuItem _tempFromLakeshore;
     private JCheckBoxMenuItem _confirmHvGainOn;
     private JCheckBoxMenuItem _filterWheelOn;
     private JCheckBoxMenuItem _slideOn;
     private JCheckBoxMenuItem _enforceSave;
     private JCheckBoxMenuItem _useUACdb;
 
 
     // Member class to check for changes in format
     private CheckFormat _format;
 
     // Check for change of settings.
     private Settings  _dataFormat;
 
     // The flag says that the current setup has been been used for a run but not saved
     private static boolean _unsavedSettings = false;
 
     // The flag says that no run has yet been posted (hence version number not written to SDSU memory)
     private static boolean _firstPosting = true;
 
     // Name of target used in the application last posted to the servers
     private String _postedTarget = "UNDEFINED";
 
     // Properties object
     CommentedProperties properties = null;
 
     // FilterWheel Controller object (for manual control)
     WheelController wheelControl = null;
     // FilterWheel object (for programmatic control - both cannot be connected at once!!!)
     FilterWheel wheel = new FilterWheel();
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** This is the constructor which does the hard work of setting up the GUI, define
      * the actions panel, menu bar, timing panel, windows pansl etc. The individual
      * components are hived off into one-off methods to make it easier to see what does
      * what.
      */
     public Usdriver () {
 
 	try {
 
 	    // Set the colours & fonts
 
 	    UIManager.put("OptionPane.background",         DEFAULT_COLOUR);
 	    UIManager.put("Panel.background",              DEFAULT_COLOUR);
 	    UIManager.put("Button.background",             DEFAULT_COLOUR);
 	    UIManager.put("CheckBoxMenuItem.background",   DEFAULT_COLOUR);
 	    UIManager.put("SplitPane.background",          DEFAULT_COLOUR);
 	    UIManager.put("Table.background",              DEFAULT_COLOUR);
 	    UIManager.put("Menu.background",               DEFAULT_COLOUR);
 	    UIManager.put("MenuItem.background",           DEFAULT_COLOUR);
 	    UIManager.put("TextField.background",          DEFAULT_COLOUR);
 	    UIManager.put("ComboBox.background",           DEFAULT_COLOUR);
 	    UIManager.put("TabbedPane.background",         DEFAULT_COLOUR);
 	    UIManager.put("TabbedPane.selected",           DEFAULT_COLOUR);
 	    UIManager.put("MenuBar.background",            DEFAULT_COLOUR);
 	    UIManager.put("window.background",             DEFAULT_COLOUR);  
 	    UIManager.put("TextPane.background",           LOG_COLOUR);
 	    UIManager.put("Tree.background",               LOG_COLOUR);
 	    UIManager.put("RadioButtonMenuItem.background",DEFAULT_COLOUR);
 	    UIManager.put("RadioButton.background",        DEFAULT_COLOUR);
 
 	    UIManager.put("Table.font",                    DEFAULT_FONT);
 	    UIManager.put("TabbedPane.font",               DEFAULT_FONT);
 	    UIManager.put("OptionPane.font",               DEFAULT_FONT);
 	    UIManager.put("Menu.font",                     DEFAULT_FONT);
 	    UIManager.put("MenuItem.font",                 DEFAULT_FONT);
 	    UIManager.put("Button.font",                   DEFAULT_FONT);
 	    UIManager.put("ComboBox.font",                 DEFAULT_FONT);
 	    UIManager.put("RadioButtonMenuItem.font",      DEFAULT_FONT);
 	    UIManager.put("RadioButton.font",              DEFAULT_FONT);
 
 	    // Load configuration file
 	    loadConfig();
 
 	    //-----------------------------------------------------------------------------------------------------
 	    // Information panels setup
 	    _replyPanel = new ReplyPanel();
 	    logPanel    = new LogPanel(LOG_FILE_DIRECTORY);
 	    if(FILE_LOGGING_ON)
 		logPanel.startLog();
 
 	    //-----------------------------------------------------------------------------------------------------
 	    // Define the rtplot and XML file choosers
 
 	    _rtplotFileChooser = new JFileChooser();
 	    _rtplotFileChooser.setFileFilter(new FileFilterDat());
 
 	    _xmlFileChooser    = new JFileChooser();
 	    _xmlFileChooser.setFileFilter(new FileFilterXML());
 	    _xmlFileChooser.setCurrentDirectory(new File(APP_DIRECTORY));
 
 	    //-----------------------------------------------------------------------------------------------------
 	    // Create an XML document builder & transformer
 	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	    dbf.setValidating(false);
 	    _documentBuilder = dbf.newDocumentBuilder();
 	    
 	    TransformerFactory factory = TransformerFactory.newInstance();
 	    _transformer = factory.newTransformer();
 
 
 	    //-----------------------------------------------------------------------------------------------------
 	    // Enable the window destruct signal
 	    this.addWindowListener(
 				   new WindowAdapter() {
 				       public void windowClosing(WindowEvent e){
 					   if(logPanel.loggingEnabled())
 					       logPanel.stopLog();
 					   try{
 					       if(wheel.isConnected()) wheel.close();
 					       String path = System.getProperty("user.home");
 					       Document document = _createXML(false);
 					       FileWriter fwriter = new FileWriter(path + "/.usdriver.xml");
 					       _transformer.transform(new DOMSource(document),new StreamResult(fwriter));
 					   } catch (Exception ex) {System.out.println(ex);System.exit(0);}
 					   System.exit(0);
 				       }
 
 				       public void windowOpened(WindowEvent e){
 					   try {
 					       Class.forName("com.mysql.jdbc.Driver").newInstance();
 					   } catch (Exception ie) {
 					       USE_UAC_DB = false;
 					       _useUACdb.setState(false);
 					       _useUACdb.setEnabled(false);
 					       System.out.println("Couldn't import jdbc, turning UAC db lookup off.");
 					   }
 					   try{
 					       String path = System.getProperty("user.home");
 					       _xmlFile = new File(path + "/.usdriver.xml");
 					       if(_xmlFile.exists()) _loadApp();
 					   } catch (Exception ex) {System.out.println(ex);};
 				       }
 
 				   }
 				   );	
 
 	    //-----------------------------------------------------------------------------------------------------
 	    // Set up basic frame
 	    // If you change the next string, you must change Makefile as well where
 	    // a sed operation changes the version number.
 	    this.setTitle("ULTRASPEC window creator and driver, version 2");
 	    this.setSize( 800, 400);
 	    
 	    // The basic layout is to have action buttons on the top-left, parameter controls on the top-right, 
 	    // timing information panels on the middle-left, and target info in the middle-right 
 	    // Finally information back from the servers etc goes along the bottom. All these panels
 	    // have their own methods or classes to avoid this constructor from becoming excessively long.
 	    // GridBagLayout manager is used to arrange the panels.
 
 	    Container container = this.getContentPane();
 	    container.setBackground(DEFAULT_COLOUR);
 	    container.setLayout(gbLayout);
 	    
 	    // Menu bar
 	    JMenuBar menubar = new JMenuBar();
 	    this.setJMenuBar(menubar);
 	    
 	    // File menu
 	    menubar.add(createFileMenu());
 
 	    // Settings menu
 	    menubar.add(createSettingsMenu());
 
 	    // Filter menu
 	    menubar.add(createFilterMenu());
 
 	    // Now the main panels of the GUI. 
 
 	    // Action panel in top-left
 	    addComponent( container, createActionsPanel(),  0, 0,  1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 
 	    // Some horizontal space between the left- and right-hand panels
 	    addComponent( container, Box.createHorizontalStrut(15), 1, 0,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	    addComponent( container, Box.createHorizontalStrut(15), 1, 2,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	    addComponent( container, Box.createHorizontalStrut(15), 1, 4,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	    // Some horizontal space to the right of all panels (gives space for extra drift mode UI elements to occupy)
 	    addComponent( container, Box.createHorizontalStrut(15), 3, 0,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	    addComponent( container, Box.createHorizontalStrut(15), 3, 2,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	    addComponent( container, Box.createHorizontalStrut(15), 3, 4,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	    // Top-right panel defines the parameters
 	    addComponent( container, createWindowPanel(),  2, 0,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	    // Middle-left panel defines the user info
 	    addComponent( container, createUserPanel(),   0, 2,  1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 
 	    //Middle-right panel defines the instrument info (filters, gratings, slit)
 	    addComponent( container, createInstPanel(),   2, 2,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 
 	    // Bottom-left panel for displaying target and s-to-n information
 	    addComponent( container, createTimingPanel(),   0, 4,  1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 
 	    // Lower-right panel defines the target info
 	    addComponent( container, createTargetPanel(),  2, 4,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 
 	    // When observing we have XML information and logger panels
 	    if(OBSERVING_MODE){
 		
 		JSplitPane infoPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logPanel, _replyPanel);
 		infoPanel.setBorder(new EmptyBorder(15,15,15,15));
 		
 		addComponent( container, infoPanel, 0, 5,  3, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	    }
 
 	    // Horizontal separator across whole GUI to separate essential (above) from nice-to-have (below)
 	    JSeparator hsep = new JSeparator();
 	    hsep.setBackground(SEPARATOR_BACK);
 	    hsep.setForeground(SEPARATOR_FORE);
 	    addComponent( container, hsep, 0, 1,  3, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	    Dimension hdim = container.getPreferredSize();
 	    hsep.setPreferredSize(new Dimension(hdim.width, SEPARATOR_WIDTH));
 
 	    JSeparator hsep2 = new JSeparator();
 	    hsep2.setBackground(SEPARATOR_BACK);
 	    hsep2.setForeground(SEPARATOR_FORE);
 	    addComponent( container, hsep2, 0, 3,  3, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	    hsep2.setPreferredSize(new Dimension(hdim.width, SEPARATOR_WIDTH));
 
 	    // Ensure correct configuration of enabled buttons
 	    _setEnabledActions();
 
 	    // Update the colours while ensuring that paste operations remain disabled in numeric fields
 	    updateGUI();
 
 	    // Make the whole GUI visible
 	    pack();
 	    setVisible(true);
 
 	    // Define timer to provide regular updating of timing information
 	    // and to check whether windows are synchronised
 	    // Task to perform
 	    ActionListener taskPerformer = new ActionListener() {
 
 		    public void actionPerformed(ActionEvent event) {
 
 
 			// Moved these outside the validity check otherwise invalid
 			// setting prevent one from goint back to 2 windows 15/06/09
 			setNumEnable();
 			_singleWindows.setNwin(numEnable);
 
 			// Validity check that reports an error once
 			if(isValid(_validStatus)){
 
 			    if(_areSynchronised()){
 				syncWindows.setEnabled(false);
 				syncWindows.setBackground(DEFAULT_COLOUR);
 			    }else{
 				syncWindows.setEnabled(true);
 				syncWindows.setBackground(WARNING_COLOUR);
 			    }
 			    speed();
 			}
 		    }
 		};	
 	
 	    // Checks once per second
 	    Timer tinfoTimer = new Timer(1000, taskPerformer);	
 	    tinfoTimer.start();
 
 	    // Store current format in order to check for changes.
 	    _format     = new CheckFormat();
 	    _dataFormat = new Settings();
 
 	}
 	catch(Exception e){
 	    e.printStackTrace();
 	    System.out.println(e);
 	    try{
 		if(wheel.isConnected())wheel.close();
 	    }catch(Exception ex){
 		System.out.println("Couldn't relinquish FW control.");
 	    }
 	    System.out.println("Usdriver exiting.");
 	    System.exit(0);
 	}
     }
 
     // End of constructor
 
     //------------------------------------------------------------------------------------------------------------------------------------------------
 
     // Series of commands which define the enabled/disabled states of buttons following various commands
 
     // Carries out operations needed when SDSU is reset
     private void onResetSDSU(){
 	startRun_enabled        = false;
 	stopRun_enabled         = false;
 	postApp_enabled         = false;
 	resetSDSU_enabled       = false;
 	resetPCI_enabled        = true;
 	setupServer_enabled     = false;
 	powerOn_enabled         = false;
 	logPanel.add("Reset SDSU", LogPanel.OK, true);
 	_setEnabledActions();
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     // Carries out operations needed when PCI is reset
     private void onResetPCI(){
 	startRun_enabled        = false;
 	stopRun_enabled         = false;
 	postApp_enabled         = true;
 	resetSDSU_enabled       = true;
 	resetPCI_enabled        = false;
 	setupServer_enabled     = true;
 	powerOn_enabled         = false;
 	logPanel.add("Reset PCI", LogPanel.OK, true);
 	_setEnabledActions();
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     // Carries out operations needed after servers setup
     private void onServersSet(){
 	startRun_enabled        = false;
 	stopRun_enabled         = false;
 	postApp_enabled         = false;
 	resetSDSU_enabled       = true;
 	resetPCI_enabled        = false;
 	setupServer_enabled     = false;
 	powerOn_enabled         = true;
 	logPanel.add("Servers setup", LogPanel.OK, true);
 	_setEnabledActions();
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     // Carries out operations needed after power on
     private void onPowerOn(){
 	startRun_enabled        = false;
 	stopRun_enabled         = false;
 	postApp_enabled         = true;
 	resetSDSU_enabled       = true;
 	resetPCI_enabled        = false;
 	setupServer_enabled     = false;
 	powerOn_enabled         = false;
 	logPanel.add("Powered on SDSU", LogPanel.OK, true);
 	_setEnabledActions();
 
 	boolean active = true;
 	int n = 0;
 	while(n < 5){
 	    n++;
 	    if((active = isRunActive(false)) && n < 5) 
 		try { Thread.sleep(1000); } catch(Exception e){};
 	} 
 	if(active)
 	    logPanel.add("Timed out waiting for 'power on' run to de-activate; cannot initialise run number. Stu, please tell me if this happens", LogPanel.ERROR, false);
 	else
 	    getRunNumber();
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     // Carries out operations needed when an application has been posted
     private void onPostApp(){
 	startRun_enabled        = true;
 	stopRun_enabled         = false;
 	postApp_enabled         = true;
 	resetSDSU_enabled       = false;
 	resetPCI_enabled        = false;
 	setupServer_enabled     = false;
 	powerOn_enabled         = false;
 	
 	// Compute number of bytes and time per image for later use by exposure 
 	// meters 
 	_nbytesPerImage = nbytesPerImage();
 	_nexposures     = numExpose;
 	
 	_postedTarget = _objectText.getText().trim();
 	logPanel.add("Posted <strong>" + _postedTarget + "</strong> to servers", LogPanel.OK, true);
 	_setEnabledActions();
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     // Carries out operations needed when a run is started
     private void onStartRun(){
 
 	logPanel.add("Started exposing on <strong>" + _postedTarget + "</strong>", LogPanel.OK, true);
 
 	if(_dataFormat.hasChanged()) _unsavedSettings = true;
 	if(_unsavedSettings && !EXPERT_MODE) _disableAll();
 
 	startRun_enabled        = false;
 	stopRun_enabled         = true;
 	postApp_enabled         = false;
 	resetSDSU_enabled       = false;
 	resetPCI_enabled        = false;
 	setupServer_enabled     = false;
 	powerOn_enabled         = false;
 	
 	_exposureTime.setText("0");
 	_spaceUsed.setText("0");
 	
 	incrementRunNumber();
 	
 	// This is just a safety measure in case the program has been restarted and
 	// a run started without an application being posted
 	if(_nbytesPerImage == 0){
 	    // Compute number of bytes and time per image for later use by exposure 
 	    // meters 
 	    _nbytesPerImage = nbytesPerImage();
 	    _nexposures     = numExpose;
 	}
 	
 	_exposureMeter.restart();
 	
 	if(_nexposures > 0){
 	    
 	    // In this case we want to start a timer to check whether a run
 	    // is still active. Start by estimating length of time to avoid
 	    // polling the server more than necessary
 	    // Timer is activated once per second
 	    
 	    int pollInterval = 1000;
 	    int initialDelay = 2000;
 	    if(DEBUG){
 		System.out.println("Run polling Interval = " + pollInterval + " millseconds");
 		System.out.println("Initial delay        = " + initialDelay + " milliseconds");
 	    }
 	    if(_runActive != null) _runActive.stop();
 	    _runActive = new Timer(pollInterval, _checkRun);
 	    _runActive.setInitialDelay(initialDelay);
 	    _runActive.start();
 	    
 	}	
 	_setEnabledActions();
 	_ucamServersOn.setEnabled(false);
 
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     // Carries out operations needed when a run is stopped
     private void onStopRun(){
 	startRun_enabled        = false;
 	stopRun_enabled         = false;
 	postApp_enabled         = true;
 	resetSDSU_enabled       = true;
 	resetPCI_enabled        = false;
 	setupServer_enabled     = false;
 	powerOn_enabled         = false;
 	_exposureMeter.stop();
 	if(_runActive != null) _runActive.stop();
 	logPanel.add("Stopped exposing on <strong>" + _postedTarget + "</strong>", LogPanel.OK, true);
 	_setEnabledActions();
 	_ucamServersOn.setEnabled(true);
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     // GUI update. It seems that updateComponentTree method re-enables the pastes on numeric fields where 
     // it was disabled. Thus we need to re-disable them all.
     public void updateGUI(){
 
 	// Update colours
 	SwingUtilities.updateComponentTreeUI(this);
 
 	xbinText.setTransferHandler(null);
 	ybinText.setTransferHandler(null);
 	exposeText.setTransferHandler(null);
 	tinyExposeText.setTransferHandler(null);
 	hvGainText.setTransferHandler(null);
 	ledIntensityText.setTransferHandler(null);
 	numExposeText.setTransferHandler(null);
 	numWinText.setTransferHandler(null);
 	_singleWindows.disablePaste();
 	_windowPairs.disablePaste();
 
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     // Sets the enabled state of any action buttons according to current values of equivalent boolean variables
     private void _setEnabledActions() {
 	// Set the fine increment for expert mode
 	tinyExposeText.setEnabled(EXPERT_MODE);
 
 	enableChanges.setEnabled(EXPERT_MODE || _unsavedSettings);
 
 	postApp.setEnabled(ULTRACAM_SERVERS_ON && (EXPERT_MODE || postApp_enabled));
 
 	startRun.setEnabled(ULTRACAM_SERVERS_ON && (EXPERT_MODE || startRun_enabled));
 	if(ULTRACAM_SERVERS_ON && (EXPERT_MODE || startRun_enabled))
 	    startRun.setBackground(GO_COLOUR);
 	else
 	    startRun.setBackground(DEFAULT_COLOUR);
 	
 	stopRun.setEnabled(ULTRACAM_SERVERS_ON && (EXPERT_MODE || stopRun_enabled));
 	if(ULTRACAM_SERVERS_ON && (EXPERT_MODE || stopRun_enabled))
 	    stopRun.setBackground(STOP_COLOUR);
 	else
 	    stopRun.setBackground(DEFAULT_COLOUR);
 	
 	resetSDSU.setEnabled(ULTRACAM_SERVERS_ON && (EXPERT_MODE || resetSDSU_enabled));
 
 	resetPCI.setEnabled(ULTRACAM_SERVERS_ON && (EXPERT_MODE || resetPCI_enabled));
 
 	execExpertCmd.setEnabled(ULTRACAM_SERVERS_ON && EXPERT_MODE);
 
 	setupServer.setEnabled(ULTRACAM_SERVERS_ON && (EXPERT_MODE || setupServer_enabled));
 
 	powerOn.setEnabled(ULTRACAM_SERVERS_ON && (EXPERT_MODE || powerOn_enabled));
 
 	if(_actionPanel != null){
 	    _actionPanel.setEnabledAt(0, ULTRACAM_SERVERS_ON);
 	    if(!ULTRACAM_SERVERS_ON)
 		_actionPanel.setSelectedIndex(1);
 	}
 
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     // Enables the labels for the windows/window pairs
     private void _setWinLabels(boolean enable){
 	if(driftModeEnabled.isSelected()){
 	    xleftLabel.setEnabled(enable);
 	    xrightLabel.setEnabled(enable);
 	    ystartLabel2.setEnabled(enable);
 	    nxLabel2.setEnabled(enable);
 	    nyLabel2.setEnabled(enable);
 	}else{
 	    xstartLabel.setEnabled(enable);
 	    ystartLabel.setEnabled(enable);
 	    nxLabel.setEnabled(enable);
 	    nyLabel.setEnabled(enable);
 	}
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 	    
     /** Retrieves the values from the various fields and checks whether the currently 
      *  selected values represent a valid set of windows. This should be called by any 
      *  routine that needs the most up-to-date values of the parameters.
      */
     public boolean isValid(boolean loud) {
 
 	_validStatus = true;
 
 	try{
 
 	    xbin         = xbinText.getValue();	
 	    ybin         = ybinText.getValue();	
 	    expose       = _getExpose();
 	    numExpose    = numExposeText.getValue();
 	    hvGain       = hvGainText.getValue();
 	    ledIntensity = ledIntensityText.getValue();
 	    numWin       = numWinText.getValue();
 	    setNumEnable();
 	    if(driftModeEnabled.isSelected()){
 		// note number of windows hard coded to one for drift mode!
 		_validStatus = _windowPairs.isValid(xbin, ybin, 1, loud);
 	    }else{
 		_validStatus = _singleWindows.isValid(xbin, ybin, numEnable, loud);
 	    }
 	    
 	}
 	catch(Exception e){
 	    if(loud)
 		logPanel.add(e.toString(), LogPanel.ERROR, false);
 	    _validStatus = false;
 	}
 	return _validStatus;
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Get the exposure time from the two fields, one which gives the millsecond part, the other the 0.1 millsecond part
      * The application expect an integer number of 0.1 milliseconds
      */
     private int _getExpose() throws Exception {
 		// In the case of ULTRACAM, the exposure time is specified in 0.1 milliseconds increments, but
 		// this is too fine, so it is prompted for in terms of milliseconds.
 		// This little program returns the value that must be sent to the servers
		// In non-expert mode ensure expose is at least 1 (to get round a rare problem where it seems that
 		// the tiny part can be set to zero)
 		expose  = 10*exposeText.getValue() + tinyExposeText.getValue();
		if(!EXPERT_MODE) expose  = Math.max(1, expose);
 		return expose;
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Writes out a file which can be loaded into rtplot in order to define
      * windows. 'rtplot' can be set to load a file every new frame. This routine
      * writes one out in the correct format. It is rather superceded by the server
      * option which allows rtplot to interrogate this client directly.
      */
     public void saveToRtplot() {
 	try{
 	    if(_rtplotFile == null)
 		throw new Exception("_rtplotFile is null in saveToRtplot");
 
 	    if(isValid(true)){
 		FileWriter fwriter = new FileWriter(_rtplotFile);
 		fwriter.write("#\n# File written by Usdriver\n#\n\n");
 		fwriter.write("# xbin ybin\n" + xbin + " " + ybin + "\n");
 		if(driftModeEnabled.isSelected()){
 		    fwriter.write("\n# Window 1, llx lly nx ny\n");
 		    fwriter.write(_windowPairs.getXleftText(0) + " " + _windowPairs.getYstartText(0) + " " + 
 				  _windowPairs.getNxText(0)     + " " + _windowPairs.getNyText(0) + "\n");
 		    fwriter.write("\n# Window 2, llx lly nx ny\n");
 		    fwriter.write(_windowPairs.getXrightText(0) + " " + _windowPairs.getYstartText(0) + " " + 
 				  _windowPairs.getNxText(0)     + " " + _windowPairs.getNyText(0) + "\n");			
 		}else{
 		    for(int i=0; i<numEnable; i++){
 			fwriter.write("\n# Window " + (i+1) + ", llx lly nx ny\n");
 			fwriter.write(_singleWindows.getXstartText(i) + " " + _singleWindows.getYstartText(i) + " " + 
 				      _singleWindows.getNxText(i)     + " " + _singleWindows.getNyText(i) + "\n");
 		    }
 		}
 		fwriter.close();
 
 		logPanel.add("Written rtplot windows to " + _rtplotFile.getName(), LogPanel.OK, false);
 
 	    }else{
 		logPanel.add("No rtplot windows written", LogPanel.ERROR, false);
 	    }
 	}
 	catch(Exception e){
 	    logPanel.add(e.toString(), LogPanel.ERROR, false);
 	    _showExceptionDialog(e);
 	}
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Writes out XML file suitable for defining the application and windows.
      * This requires there to be suitable example XML files available for each
      * application.
      */
     private void _saveApp() {
 
 	try{
 	    if(_xmlFile == null)
 		throw new Exception("_saveApp: _xmlFile is null");
 	    
 	    if(isValid(true)){
 
 		if(_xmlFile.canWrite()){
 		    int result = JOptionPane.showConfirmDialog(this, 
 							       "Application file = " + _xmlFile.getName() + " already exists. Loss of data could occur if it is overwritten.\nDo you want to continue?", 
 							       "Confirm file overwrite", JOptionPane.YES_NO_OPTION);
 		    if(result == JOptionPane.NO_OPTION){
 			logPanel.add("Application was not written to disk", LogPanel.WARNING, false);
 			return;
 		    }
 		}
 
 		Document document = _createXML(false);
 
 		// Transform to write out
 		FileWriter fwriter = new FileWriter(_xmlFile);
 
 		_transformer.transform(new DOMSource(document), new StreamResult(fwriter));
 
 		logPanel.add("Written application to <strong>" + _xmlFile.getName() + "</strong>", LogPanel.OK, false);
 		_enableAll();
 		_dataFormat.update();
 		_unsavedSettings = false;
 
 	    }else{
 		throw new Exception("Invalid parameters; no XML file written");
 	    }
 	}
 	catch(Exception e){
 	    logPanel.add(e.toString(), LogPanel.ERROR, false);
 	    _showExceptionDialog(e);
 	}
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Load an application */
     private void _loadApp() {
 
 	try {
 
 	    if(_xmlFile == null)
 		throw new Exception("_loadApp: _xmlFile is null");
 
 	    // Read file
 	    Document document = _documentBuilder.parse(_xmlFile);
 	    
 	    // Try to determine application type
 	    String appValue = null;
 	    NodeList nlist = document.getElementsByTagName("executablecode");
 	    for(int i=0; i<nlist.getLength(); i++){
 		Element elem = (Element)nlist.item(i);
 		if(elem.hasAttribute("xlink:href")) 
 		    appValue = elem.getAttribute("xlink:href");
 	    }
 	    if(appValue == null)
 		throw new Exception("Failed to locate application name from " + _xmlFile.getAbsolutePath());
 	    
 	    int iapp = 0;
 	    for(iapp=0; iapp<TEMPLATE_ID.length; iapp++)
 		if(TEMPLATE_ID[iapp].equals(appValue)) break;
 	    if(iapp == TEMPLATE_ID.length)
 		throw new Exception("Application type = \"" + appValue + "\" was not recognised in " + _xmlFile.getAbsolutePath());
 
 	    applicationTemplate = TEMPLATE_LABEL[iapp];
 	    templateChoice.setSelectedItem(applicationTemplate);
 	    boolean isDriftMode=false;
 	    if(applicationTemplate.equals("Drift mode")){
 		// check box, and fire _enableDriftMode
 		driftModeEnabled.setSelected(true);
 		_enableDriftMode();
 		isDriftMode=true;
 	    }else{
 		_disableDriftMode();
 		driftModeEnabled.setSelected(false);
 	    }
 
 
 	    // SL - edited here to allow greater than 2 windows (18/7/2012)
 	    if(! isDriftMode){
 		numEnable = 4;
 		_singleWindows.setNwin(numEnable);
 	    }
 	    
 	    // Now extract parameter values
 	    boolean found_xbin     = false,   found_ybin = false;
 	    boolean found_speed    = false,   found_expose = false;
 	    boolean found_num_expose = false, found_en_clr = false;
 	    boolean found_output   = false,   found_hv_gain = false;
 	    boolean found_led_intensity = false;
 
 	    // for windowed mode
 	    // SL - edited here to allow greater than 2 windows (18/7/2012)
 	    boolean[] found_xstart = {false, false, false, false};
 	    boolean[] found_ystart = {false, false, false, false};
 	    boolean[] found_nx     = {false, false, false, false};
 	    boolean[] found_ny     = {false, false, false, false};
 	    // Needed to pick up null windows
 	    // SL - edited here to allow greater than 2 windows (18/7/2012)
 	    int[] xstart = new int[4];
 	    int[] ystart = new int[4];
 	    int[] nx     = new int[4];
 	    int[] ny     = new int[4];
 		
 	    // for drift mode
 	    boolean found_dystart = false;
 	    boolean found_dxleft  = false;
 	    boolean found_dxright = false;
 	    boolean found_dnx     = false;
 	    boolean found_dny     = false;
 	    // store values
 	    int dxleft=0, dxright=0, dnx=0, dny=0, dystart=0;
 
 	    nlist = document.getElementsByTagName("set_parameter");
 	    for(int i=0; i<nlist.getLength(); i++){
 		Element elem = (Element)nlist.item(i);
 		if(elem.hasAttribute("ref") && elem.hasAttribute("value")){
 		    
 		    if(elem.getAttribute("ref").equals("X_BIN")) {
 			xbinText.setText(elem.getAttribute("value"));
 			found_xbin = true;
 			
 		    }else if(elem.getAttribute("ref").equals("Y_BIN")) {
 			ybinText.setText(elem.getAttribute("value"));
 			found_ybin = true;
 			
 		    }else if(elem.getAttribute("ref").equals("EN_CLR")) {
 			if(elem.getAttribute("value").equals("1")){
 			    clearEnabled.setSelected(true);
 			}else if(elem.getAttribute("value").equals("0")){
 			    clearEnabled.setSelected(false);
 			}else{
 			    throw new Exception("EN_CLR in " + _xmlFile.getAbsolutePath() + " has an unrecognised value");
 			}
 			found_en_clr = true;
 			
 		    }else if(elem.getAttribute("ref").equals("OUTPUT")) {
 			if(elem.getAttribute("value").equals("0")){
 			    ccdOutputChoice.setSelectedItem(CCD_OUTPUT_LABELS[0]);
 			}else if(elem.getAttribute("value").equals("1")){
 			    ccdOutputChoice.setSelectedItem(CCD_OUTPUT_LABELS[1]);
 			}else{
 			    throw new Exception("OUTPUT in " + _xmlFile.getAbsolutePath() + " has an unrecognised value");
 			}
 			found_output = true;
 			
 		    }else if(elem.getAttribute("ref").equals("HV_GAIN")) {
 			hvGainText.setText(elem.getAttribute("value"));
 			found_hv_gain = true;
 			
 		    }else if(elem.getAttribute("ref").equals("LED_FLSH")) {
 			ledIntensityText.setText(elem.getAttribute("value"));
 			found_led_intensity = true;
 			
 		    }else if(elem.getAttribute("ref").equals("SPEED")) {
 			if(elem.getAttribute("value").equals("0")){
 			    speedChoice.setSelectedItem(SPEED_LABELS[0]);
 			}else if(elem.getAttribute("value").equals("1")){
 			    speedChoice.setSelectedItem(SPEED_LABELS[1]);
 			}else if(elem.getAttribute("value").equals("2")){
 			    speedChoice.setSelectedItem(SPEED_LABELS[2]);
 			}else{
 			    throw new Exception("SPEED in " + _xmlFile.getAbsolutePath() + " has an unrecognised value");
 			}
 			found_speed = true;
 			
 		    }else if(elem.getAttribute("ref").equals("DWELL")) {
 			String evalue = elem.getAttribute("value").trim();
 			
 			if(evalue.length() > 1)
 			    exposeText.setText(evalue.substring(0,evalue.length()-1));
 			else
 			    exposeText.setText("0");
 			tinyExposeText.setText(evalue.substring(evalue.length()-1));
 			found_expose = true;
 			
 			
 		    }else if(elem.getAttribute("ref").equals("NUM_EXPS")) {
 			numExposeText.setText(elem.getAttribute("value"));
 			found_num_expose = true;
 			
 		    }else{
 			// WINDOW PARAMETERS
 
 			if(isDriftMode){
 			    // drift mode
 			    if(elem.getAttribute("ref").equals("X1_START")){
 				dxleft = Integer.parseInt(elem.getAttribute("value"));
 				found_dxleft = true;
 			    }
 			    if(elem.getAttribute("ref").equals("X2_START")){
 				dxright = Integer.parseInt(elem.getAttribute("value"));
 				found_dxright = true;
 			    }
 			    if(elem.getAttribute("ref").equals("Y1_START")){
 				dystart = Integer.parseInt(elem.getAttribute("value"));
 				found_dystart = true;
 			    }
 			    if(elem.getAttribute("ref").equals("X1_SIZE")){
 				dnx = Integer.parseInt(elem.getAttribute("value"));
 				found_dnx = true;
 			    }
 			    if(elem.getAttribute("ref").equals("Y1_SIZE")){
 				dny = Integer.parseInt(elem.getAttribute("value"));
 				found_dny = true;
 			    }
 			}else{
 			    // windowed mode
 			    for(int np=0; np<numEnable; np++){			    
 				if(elem.getAttribute("ref").equals("X" + (np+1) + "_START")){
 				    xstart[np] = Integer.parseInt(elem.getAttribute("value"));
 				    found_xstart[np] = true;
 				    
 				}else if(elem.getAttribute("ref").equals("Y" + (np+1) + "_START")){
 				    ystart[np] = Integer.parseInt(elem.getAttribute("value"));
 				    found_ystart[np] = true;
 				    
 				}else if(elem.getAttribute("ref").equals("X" + (np+1) + "_SIZE")){
 				    nx[np] = Integer.parseInt(elem.getAttribute("value"));
 				    found_nx[np]     = true;
 				    
 				}else if(elem.getAttribute("ref").equals("Y" + (np+1) + "_SIZE")){
 				    ny[np] = Integer.parseInt(elem.getAttribute("value"));
 				    found_ny[np]     = true;
 				}			    
 			    }
 			}
 			// end of window read section
 		    }
 		}
 	    }
 		
 	    
 	    // Check that all necessary parameters have been found
 	    
 	    if(!found_xbin)
 		throw new Exception("Failed to find X_BIN in " + _xmlFile.getAbsolutePath());
 	    if(!found_ybin)
 		throw new Exception("Failed to find Y_BIN in " + _xmlFile.getAbsolutePath());
 	    if(!found_en_clr && !isDriftMode)
 		throw new Exception("Failed to find EN_CLR in " + _xmlFile.getAbsolutePath());
 	    if(!found_speed)
 		throw new Exception("Failed to find SPEED in " + _xmlFile.getAbsolutePath());
 	    if(!found_output)
 		throw new Exception("Failed to find OUTPUT in " + _xmlFile.getAbsolutePath());
 	    if(!found_hv_gain)
 		throw new Exception("Failed to find HV_GAIN in " + _xmlFile.getAbsolutePath());
 	    if(!found_led_intensity)
 		throw new Exception("Failed to find LED_FLSH in " + _xmlFile.getAbsolutePath());
 	    if(!found_expose)
 		throw new Exception("Failed to find DWELL in " + _xmlFile.getAbsolutePath());
 	    if(!found_num_expose)
 		throw new Exception("Failed to find NUM_EXPS in " + _xmlFile.getAbsolutePath());
 	    if(isDriftMode){
 		if(!found_dxleft)
 		    throw new Exception("Failed to find X1_START in "+ _xmlFile.getAbsolutePath());
 		if(!found_dxright)
 		    throw new Exception("Failed to find X2_START in "+ _xmlFile.getAbsolutePath());
 		if(!found_dystart)
 		    throw new Exception("Failed to find Y1_START in "+ _xmlFile.getAbsolutePath());
 		if(!found_dnx)
 		    throw new Exception("Failed to find X1_SIZE in "+ _xmlFile.getAbsolutePath());
 		if(!found_dny)
 		    throw new Exception("Failed to find Y1_SIZE in "+ _xmlFile.getAbsolutePath());
 	    }else{
 		for(int i=0; i<numEnable; i++){
 		    if(!found_xstart[i]) 
 			throw new Exception("Failed to find X" + (i+1) + "_START in " + _xmlFile.getAbsolutePath());
 		    if(!found_ystart[i]) 
 			throw new Exception("Failed to find Y" + (i+1) + "_START in " + _xmlFile.getAbsolutePath());
 		    if(!found_nx[i]) 
 			throw new Exception("Failed to find X" + (i+1) + "_SIZE in " + _xmlFile.getAbsolutePath());
 		    if(!found_ny[i]) 
 			throw new Exception("Failed to find Y" + (i+1) + "_SIZE in " + _xmlFile.getAbsolutePath());
 		}
 	    }
 
 	    // Now can update number of windows and correct for unbinned sizes of L3CCD
 	    xbin = xbinText.getValue();
 	    ybin = ybinText.getValue();
 	    if(!isDriftMode){
 		numWin = 0;
 		for(int np=0; np<numEnable; np++){
 		    if(nx[np] > 0 && ny[np] > 0){
 			nx[np] *= xbin;
 			ny[np] *= ybin;
 			numWin++;
 		    }
 		}
 	    }else{
 		// SL CHECK - do we do this for drift mode?
 		dnx *= xbin;
 		dny *= ybin;
 	    }
 
 	    // 15/06/09. Convert from Derek coordinates in xml file to the output-independent 
 	    // shown by the usdriver gui. Output independent coords are defined so that the
 	    // the same region is read out independent of the output port used given a fixed 
 	    // window definition in the GUI. To do so the 16 pixels overscan have to be ignored 
 	    // because it turns out that these cannot be consistently read out given what can 
 	    // be sent to the camera via xml. Derek coords are those used in the xml. The conversion
 	    // formulae are xd = xoi + 16 (normal), xd = 1073 - xoi (avalanche). When getting the
 	    // xstart value in avalanche mode one actually wants the rightmost pixel of the window
 	    // i.e. xstart+nx-1, thus in Derek coords this is xstart(derek) = 1074-xstart(oi)-nx 
 	    // where is the number of unbinned pixels.
 	    
 	    // Also code here to make sure that no part of a binned pixel overlaps the overscan.
 	    if(ccdOutputChoice.getSelectedItem().equals("Avalanche")){
 		if(isDriftMode){
 		    int nchop = Math.max(0,17-dxleft); //SL CHECK. xright?
 		    if(nchop % xbin != 0) nchop = xbin * (nchop / xbin + 1);
 		    dxleft  = Math.max(1,1074-dxleft-dnx);
 		    dxright = Math.max(1,1074-dxright-dnx);
 		    dnx    -= nchop;
 		    // now we have to check that dxleft and dxright are in right order
 		    if(dxleft > dxright){
 			int tmpStore = dxleft;
 			dxleft = dxright;
 			dxright = tmpStore;
 		    }
 		}else{
 		    for(int np=0; np<numEnable; np++){						
 			int nchop = Math.max(0,17-xstart[np]);
 			if(nchop % xbin != 0) nchop = xbin * (nchop / xbin + 1);
 			xstart[np] = Math.max(1, 1074 - xstart[np] - nx[np]);
 			nx[np] -= nchop;
 		    }
 		}
 	    }else{
 		if(isDriftMode){
 		    int nchop = Math.max(0,17-dxleft); //SL CHECK. xright?
 		    if(nchop % xbin != 0) nchop = xbin * (nchop / xbin + 1);
 		    dxleft  = Math.max(1,dxleft+nchop-16);
 		    dxright = Math.max(1,dxright+nchop-16);
 		    dnx    -= nchop;
 		    //now we have to check that dxleft and dxright are in right order
 		    if(dxleft > dxright){
 			int tmpStore = dxleft;
 			dxleft = dxright;
 			dxright = tmpStore;
 		    }
 		}else{
 		    for(int np=0; np<numEnable; np++){
 			int nchop = Math.max(0,17-xstart[np]);
 			if(nchop % xbin != 0) nchop = xbin * (nchop / xbin + 1);
 			xstart[np] = Math.max(1, xstart[np] + nchop - 16);
 			nx[np] -= nchop;
 		    }
 		}
 	    }
 	    if(isDriftMode){
 		_windowPairs.setYstartText(0,Integer.toString(dystart));
 		_windowPairs.setXleftText(0,Integer.toString(dxleft));
 		_windowPairs.setXrightText(0,Integer.toString(dxright));
 		_windowPairs.setNxText(0,Integer.toString(dnx));
 		_windowPairs.setNyText(0,Integer.toString(dny));
 	    }else{
 		numWinText.setText(Integer.toString(numWin));
 		setNumEnable();
 		_singleWindows.setNwin(numEnable);
 	    
 		for(int np=0; np<numEnable; np++){
 		    _singleWindows.setXstartText(np, Integer.toString(xstart[np]));
 		    _singleWindows.setYstartText(np, Integer.toString(ystart[np]));
 		    _singleWindows.setNxText(np,     Integer.toString(nx[np]));
 		    _singleWindows.setNyText(np,     Integer.toString(ny[np]));
 		}
 	    }
 
 	    // Load user defined stuff
 	    _setFromUser(document, "target",     _objectText);
 	    _setFromUser(document, "grating",    _gratingText);
 	    _setFromUserCombo(document, "filters",    _filter);
 	    _setFromUser(document, "slit_width", _slitWidthText);
 	    _setFromUser(document, "slit_angle", _slitAngleText);
 	    _setFromUser(document, "ID",         _progidText);
 	    _setFromUser(document, "PI",         _piText);
 	    _setFromUser(document, "Observers",  _observerText);
 	    _setRunType(document, "flags");
 	    //	    _setFromUser(document, "Comment",    _commentText);
 
 	    logPanel.add("Loaded <strong>" + _xmlFile.getName() + "</strong>", LogPanel.OK, true);
 	    _dataFormat.update();
 	    _unsavedSettings = false;
 
 	}
 	catch(Exception e){
 	    if(DEBUG) e.printStackTrace();
 	    _showExceptionDialog(e);
 	}
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /* Sets the value of a JTextfield corresponding to 'item' from the 'user' section of 
      * an XML document. If not found, the JTextField is blanked.
      */
     private void _setFromUser(Document document, String item, JTextField field){
 	NodeList nlist = document.getElementsByTagName("user");
 	if(nlist.getLength() > 0){
 	    Element  user = (Element)nlist.item(0);
 	    NodeList newlist = user.getElementsByTagName(item);
 	    if(newlist.getLength() > 0){
 		Node node = newlist.item(0).getFirstChild();
 		if(node != null){
 		    String value = node.getNodeValue();
 		    if(value != null){
 			field.setText(value);
 			return;
 		    }
 		}
 	    }
 	}
 
 	// Failed somewhere, blank before returning
 	field.setText("");
 
     }
     //------------------------------------------------------------------------------------------------------------------------------------------
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /* Sets the value of a JComboBox corresponding to 'item' from the 'user' section of 
      * an XML document. If not found, the JTextField is blanked.
      */
     private void _setFromUserCombo(Document document, String item, JComboBox field){
 	NodeList nlist = document.getElementsByTagName("user");
 	if(nlist.getLength() > 0){
 	    Element  user = (Element)nlist.item(0);
 	    NodeList newlist = user.getElementsByTagName(item);
 	    if(newlist.getLength() > 0){
 		Node node = newlist.item(0).getFirstChild();
 		if(node != null){
 		    String value = node.getNodeValue();
 		    if(value != null){
 			field.setSelectedItem(value);
 			return;
 		    }
 		}
 	    }
 	}
 
 	// Failed somewhere, blank before returning
 	//field.setSelectedItem("");
 
     }
     //------------------------------------------------------------------------------------------------------------------------------------------
     //
     private void _setRunType(Document document, String item) {
 	NodeList nlist = document.getElementsByTagName("user");
 	if (nlist.getLength() > 0) {
 	    Element user = (Element)nlist.item(0);
 	    NodeList newlist = user.getElementsByTagName(item);
 	    if (newlist.getLength() > 0) {
 		Node node = newlist.item(0).getFirstChild();
 		if (node != null) {
 		    String value = node.getNodeValue();
 		    _acquisitionState = false;
 		    if (value.indexOf("data") > -1) {
 			_dataButton.setSelected(true);
 			_runType = "data";
 		    }
 		    if (value.indexOf("caution") > -1) {
 			_acqButton.setSelected(true);
 			_runType = "data";
 			_acquisitionState = true;
 		    }
 		    if (value.indexOf("bias") > -1) {
 			_runType = "bias";
 			_biasButton.setSelected(true);
 		    }
 		    if (value.indexOf("flat") > -1) {
 			_runType = "flat";
 			_flatButton.setSelected(true);
 		    }
 		    if (value.indexOf("dark") > -1) {
 			_runType = "dark";
 			_darkButton.setSelected(true);
 		    }
 		    if (value.indexOf("technical") > -1) {
 			_runType = "technical";
 			_techButton.setSelected(true);
 		    }
 		    _checkEnabledFields();
 		}
 	    }
 	}
     }
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Posts application corresponding to current settings to servers
      * This requires there to be suitable example XML files available for each
      * application.
      */
     private boolean _postApp() {
 	
 	try{
 	    if(isValid(true)){
 		Document document = _createXML(true);
 
 		// First to the camera server
 		HttpURLConnection httpConnection = (HttpURLConnection)(new URL(HTTP_CAMERA_SERVER + HTTP_PATH_CONFIG).openConnection());
 		httpConnection.setRequestMethod("POST");
 		httpConnection.setRequestProperty("Content-Type", "text/xml");
 		httpConnection.setDoOutput(true);
 		httpConnection.connect();
 		
 		OutputStream outputStream = httpConnection.getOutputStream();
 		_transformer.transform(new DOMSource(document), new StreamResult(outputStream));
 		
 		// Get reply back from server
 		InputStream  inputStream  = httpConnection.getInputStream();
 		String       xmlString    = _readText(inputStream).trim();
 		
 		Document xmlDoc = _documentBuilder.parse(new InputSource(new StringReader(xmlString)));
 		_replyPanel.showReply(xmlDoc, HTTP_CAMERA_SERVER, true);
 
 		if(!isResponseOK(xmlDoc))
 		    throw new Exception("XML response from camera server = " + HTTP_CAMERA_SERVER + " was not OK");
 
 		// Now to the data server
 		httpConnection = (HttpURLConnection)(new URL(HTTP_DATA_SERVER + HTTP_PATH_CONFIG).openConnection());
 		httpConnection.setRequestMethod("POST");
 		httpConnection.setRequestProperty("Content-Type", "text/xml");
 		httpConnection.setDoOutput(true);
 		httpConnection.connect();
 			
 		outputStream = httpConnection.getOutputStream();
 		_transformer.transform(new DOMSource(document), new StreamResult(outputStream));
 		
 		// Get reply back from server
 		inputStream  = httpConnection.getInputStream();
 		xmlString    = _readText(inputStream).trim();
 		
 		xmlDoc = _documentBuilder.parse(new InputSource(new StringReader(xmlString)));
 		_replyPanel.showReply(xmlDoc, HTTP_DATA_SERVER, false);
 
 		if(!isResponseOK(xmlDoc))
 		    throw new Exception("XML response from data server = " + HTTP_DATA_SERVER + " was not OK");
 
 	    }else{
 		throw new Exception("Windows invalid; application was not posted to the servers");
 	    }	
 	    return true;
 	}
 	catch(Exception e) {
 	    logPanel.add(e.toString(), LogPanel.ERROR, false);
 	    _showExceptionDialog(e);
 	    return false;
 	}
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Fetches an application from the server, returning a Document as the result.
      *  If it fails, error messages will be printed and the Document returned will
      * be null
      */
     private Document _fetchApp(String name) {
 
 	String xmlString = null;
 
 	try {
 	    URL url = new URL(HTTP_CAMERA_SERVER + HTTP_PATH_GET + "?" + HTTP_SEARCH_ATTR_NAME + "=" + name);
 
 	    xmlString = _readText(url.openStream()).trim();
 
 	    Document document = _documentBuilder.parse(new InputSource(new StringReader((xmlString))));
 	    
 	    logPanel.add("Application = <strong>" + name + "</strong> fetched from server.", LogPanel.OK, true);
 
 	    return document;
 	}
 	catch(SocketException e) {
 	    if(DEBUG) e.printStackTrace();
 	    String message = "A SocketException can be caused if an invalid CGI argument is used after \"" +
 		HTTP_PATH_GET + "?\" in the URL.\n" +
 		"Make sure \"" + HTTP_CAMERA_SERVER + HTTP_PATH_GET + "?" + HTTP_SEARCH_ATTR_NAME + "=<file>\" is supported by the server.\n" +
 		"Check whether server is on\n" + 
 		"Check whether \"" +  HTTP_CAMERA_SERVER + HTTP_PATH_GET + "\" is supported by the server.";
 	    
 	    JOptionPane.showMessageDialog(this, message, "SocketException", JOptionPane.WARNING_MESSAGE);
 	}
 	catch(SAXParseException e) {
 	    if(DEBUG) e.printStackTrace();
 	    System.out.println("XML start\n" + xmlString + "\nXML end");
 	    JOptionPane.showMessageDialog(this, e + "\nTry again.", e.getClass().getName(), JOptionPane.WARNING_MESSAGE);
 	}
 	catch(Exception e) {
 	    if(DEBUG) e.printStackTrace();
 	    JOptionPane.showMessageDialog(this, e + "\nTry again.", e.getClass().getName(), JOptionPane.WARNING_MESSAGE);
 	}
 	return null;
 	
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Read from an InputStream into a string. Go via an InputStreamReader to translate bytes
      * to chars */
     private static String _readText(InputStream inputStream) {
 	try{
 	    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
 	    char[]       cbuff = new char[2048];
 	    StringBuffer sbuff = new StringBuffer();
 	    int len;
 	    while((len = inputStreamReader.read(cbuff)) != -1){
 		sbuff.append(cbuff, 0, len);
 	    }
 	    return sbuff.toString();
 	}
 	catch(Exception e){
 	    logPanel.add(e.toString(), LogPanel.ERROR, false);
 	    return "_readText failed";
 	}
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** This loads an XML template file and updates it according to the 
      *  values of the settings of the window panel. The template file
      * must match the current application.
      */
     private Document _createXML(boolean posting) throws Exception {
 	
 	try{
 
 	    if(isValid(true)){
 		
 		Document document = null;
 		if(TEMPLATE_FROM_SERVER && ULTRACAM_SERVERS_ON){
 		    
 		    document = _fetchApp(TEMPLATE_APP[_whichTemplate()]);
 		    
 		} else {
 		    
 		    // Read & parse example file
 		    File templateFile = new File(TEMPLATE_DIRECTORY + TEMPLATE_APP[_whichTemplate()]);
 		    document = _documentBuilder.parse(templateFile);
 
 		}
 		if(document == null)
 		    throw new Exception("document = null");
 		
 		// Try to determine application type
 		String appValue = null;
 		NodeList nlist = document.getElementsByTagName("executablecode");
 		for(int i=0; i<nlist.getLength(); i++){
 		    Element elem = (Element)nlist.item(i);
 		    if(elem.hasAttribute("xlink:href")) 
 			appValue = elem.getAttribute("xlink:href");
 		}
 		if(appValue == null)
 		    throw new Exception("failed to locate application name");
 		
 		if(!appValue.equals(TEMPLATE_ID[_whichTemplate()]))
 		    throw new Error("application name = \"" + appValue + 
 				    "\" does not match the current template value = " +
 				    TEMPLATE_ID[_whichTemplate()]);
 
 		boolean isDriftMode=false;
 		if(applicationTemplate.equals("Drift mode"))
 		    isDriftMode = true;
 
 		// Modify the window values. NB although this code looks the 
 		// same as that in _loadApp, it differs in that it is the DOM
 		// document parameters that are changed to match the window GUI
 		// settings rather than the other way around. The input XML
 		// is used to define the basic structure that can be edited in this
 		// manner.
 		
 		boolean found_xbin       = false, found_ybin    = false;
 		boolean found_speed      = false, found_hv_gain = false;
 		boolean found_expose     = false, found_output  = false;
 		boolean found_num_expose = false, found_en_clr  = false;
 		boolean found_xsize      = false, found_ysize   = false;
 		boolean found_led_intensity = false;
 		// SL - edited here to allow greater than 2 windows (18/7/2012)
 		boolean[] found_xstart   = {false, false, false, false};
 		boolean[] found_ystart   = {false, false, false, false};
 		boolean[] found_nx       = {false, false, false, false};
 		boolean[] found_ny       = {false, false, false, false};
 		// for drift mode
 		boolean found_dystart = false;
 		boolean found_dxleft  = false;
 		boolean found_dxright = false;
 		boolean found_dnx     = false;
 		boolean found_dnx2    = false;
 		boolean found_dny     = false;
 
 		// Compute total number of binned pixels
 		int total_pix = 0;
 		if(isDriftMode){
 		    // 2 windows of Nx by Ny
 		    total_pix += 2*(_windowPairs.getNx(0) / xbin) * (_windowPairs.getNy(0) / ybin);
 		}else{
 		    for(int i=0; i<numEnable; i++)
 			total_pix += (_singleWindows.getNx(i) / xbin) * (_singleWindows.getNy(i) / ybin);
 		}
 
 		NodeList inst = document.getElementsByTagName("set_parameter");
 		for(int i=0; i<inst.getLength(); i++){
 		    Element elem = (Element)inst.item(i);
 		    if(elem.hasAttribute("ref") && elem.hasAttribute("value")){
 
 			if(elem.getAttribute("ref").equals("X_BIN")) {
 			    elem.setAttribute("value",  String.valueOf(xbin));
 			    found_xbin = true;
 
 			}else if(elem.getAttribute("ref").equals("Y_BIN")) {
 			    elem.setAttribute("value",  String.valueOf(ybin));
 			    found_ybin = true;
 
 			}else if(elem.getAttribute("ref").equals("EN_CLR")) {
 			    if(clearEnabled.isSelected())
 				elem.setAttribute("value",  "1");
 			    else
 				elem.setAttribute("value",  "0");
 			    found_en_clr = true;
 
 			}else if(elem.getAttribute("ref").equals("OUTPUT")) {
 			    if(isnormal()){
 				elem.setAttribute("value",  "0");
 			    }else{
 				elem.setAttribute("value",  "1");
 			    }
 			    found_output = true;
 
 			}else if(elem.getAttribute("ref").equals("SPEED")) {
 			    if(speedChoice.getSelectedItem().equals("Slow")){
 				elem.setAttribute("value",  "0");
 			    }else if(speedChoice.getSelectedItem().equals("Medium")){
 				elem.setAttribute("value",  "1");
 			    }else if(speedChoice.getSelectedItem().equals("Fast")){
 				elem.setAttribute("value",  "2");
 			    }else{
 				throw new Exception("current speed choice = " + speedChoice.getSelectedItem() + " is not valid.");
 			    }
 			    found_speed = true;
 
 
 			}else if(elem.getAttribute("ref").equals("HV_GAIN")) {
 			    elem.setAttribute("value",  String.valueOf(hvGain) );
 			    found_hv_gain = true;
 
 			}else if(elem.getAttribute("ref").equals("LED_FLSH")) {
 			    elem.setAttribute("value",  String.valueOf(ledIntensity) );
 			    found_led_intensity = true;
 
 			}else if(elem.getAttribute("ref").equals("DWELL")) {
 			    elem.setAttribute("value",  String.valueOf(expose) );
 			    found_expose = true;
 
 			}else if(elem.getAttribute("ref").equals("NUM_EXPS")) {
 			    elem.setAttribute("value",  String.valueOf(numExpose));
 			    found_num_expose = true;
 
 			}else if(elem.getAttribute("ref").equals("X_SIZE")){
 			    elem.setAttribute("value",  Integer.toString(total_pix));
 			    found_xsize     = true;
 			    
 			}else if(elem.getAttribute("ref").equals("Y_SIZE")){
 			    elem.setAttribute("value",  "1");
 			    found_ysize     = true;
 			}else{
 			    // WINDOW PARAMETERS
 			    if(isDriftMode){
 				boolean normal = isnormal();
 				// make sure windows are correct way round (device independent)				
 				int dxleft, dxright;
 				if(normal){				    
 				    dxleft  = _windowPairs.getXleft(0) + 16;
 				    dxright = _windowPairs.getXright(0) + 16;
 				}else{
 				    dxleft  = 1074 - _windowPairs.getXleft(0) - _windowPairs.getNx(0);
 				    dxright = 1074 - _windowPairs.getXright(0) - _windowPairs.getNx(0);
 				}
 				if(dxleft > dxright){
 				  int tmpStore=dxleft;
 				  dxleft=dxright;
 				  dxright=tmpStore;
 			        }
 				if(elem.getAttribute("ref").equals("X1_START")){
 				    elem.setAttribute("value", String.valueOf(dxleft));
 				    found_dxleft = true;
 				}else if(elem.getAttribute("ref").equals("X2_START")){
 				    elem.setAttribute("value", String.valueOf(dxright));
 				    found_dxright = true;
 				}else if(elem.getAttribute("ref").equals("Y1_START")){
 				    elem.setAttribute("value", String.valueOf(_windowPairs.getYstart(0)));
 				    found_dystart = true;
 				}else if(elem.getAttribute("ref").equals("X1_SIZE")){
 				    elem.setAttribute("value", String.valueOf(_windowPairs.getNx(0) / xbin));
 				    found_dnx = true;
 				}else if(elem.getAttribute("ref").equals("Y1_SIZE")){
 				    elem.setAttribute("value", String.valueOf(_windowPairs.getNy(0) / ybin));
 				    found_dny = true;
 				}else if(elem.getAttribute("ref").equals("X2_SIZE")){
 				    elem.setAttribute("value", String.valueOf(_windowPairs.getNx(0) / xbin));
 				    found_dnx2 = true;
 				}
 			    }else{
 				// 15/06/09 conversion from output-independent to Derek coords
 				boolean normal = isnormal(); 
 				for(int np=0; np<numEnable; np++){
 				    if(elem.getAttribute("ref").equals("X" + (np+1) + "_START")){
 					if(normal){
 					    elem.setAttribute("value",  String.valueOf(_singleWindows.getXstart(np) + 16));
 					}else{
 					    elem.setAttribute("value", String.valueOf(1074 - _singleWindows.getXstart(np) - _singleWindows.getNx(np)));
 					}
 					found_xstart[np] = true;
 					
 				    }else if(elem.getAttribute("ref").equals("Y" + (np+1) + "_START")){
 					elem.setAttribute("value",  _singleWindows.getYstartText(np));
 					found_ystart[np] = true;
 					
 				    }else if(elem.getAttribute("ref").equals("X" + (np+1) + "_SIZE")){
 					elem.setAttribute("value",  String.valueOf(_singleWindows.getNx(np) / xbin));
 					found_nx[np]     = true;
 					
 				    }else if(elem.getAttribute("ref").equals("Y" + (np+1) + "_SIZE")){
 					elem.setAttribute("value",  String.valueOf(_singleWindows.getNy(np) / ybin));
 					found_ny[np]     = true;
 					
 				    }
 				}
 				// Nullify excess windows
 				// SL - edited here to allow greater than 2 windows (18/7/2012)
 				for(int np=numEnable; np<4; np++){
 				    if(elem.getAttribute("ref").equals("X" + (np+1) + "_SIZE")){
 					elem.setAttribute("value",  "0");
 					found_nx[np]     = true;
 					
 				    }else if(elem.getAttribute("ref").equals("Y" + (np+1) + "_SIZE")){
 					elem.setAttribute("value",  "0");
 					found_ny[np]     = true;
 					
 				    }
 				}		      	
 			    }
 			}
 		    }
 		}
 		
 		// Check that all necessary parameters have been found
 		if(!found_xbin)
 		    throw new Exception("failed to find & modify X_BIN");
 		if(!found_ybin)
 		    throw new Exception("failed to find & modify Y_BIN");
 		if(!found_en_clr && !isDriftMode)
 		    throw new Exception("failed to find & modify EN_CLR");
 		if(!found_speed)
 		    throw new Exception("failed to find & modify SPEED");
 		if(!found_expose)
 		    throw new Exception("failed to find & modify DWELL");
 		if(!found_num_expose)
 		    throw new Exception("failed to find & modify NUM_EXPS");
 		if(!found_hv_gain)
 		    throw new Exception("failed to find & modify HV_GAIN");
 		if(!found_led_intensity)
 		    throw new Exception("failed to find & modify LED_INTENSITY");
 		if(!found_output)
 		    throw new Exception("failed to find & modify OUTPUT");
 		if(!found_xsize)
 		    throw new Exception("failed to find & modify X_SIZE");
 		if(!found_ysize)
 		    throw new Exception("failed to find & modify Y_SIZE");
 		if(isDriftMode){
 		    if(!found_dnx)
 			throw new Exception("failed to find & modify X1_SIZE");
 		    if(!found_dnx2)
 			throw new Exception("failed to find & modify X2_SIZE");
 		    if(!found_dny)
 			throw new Exception("failed to find & modify Y1_SIZE");
 		    if(!found_dxleft)
 			throw new Exception("failed to find & modify X1_START");
 		    if(!found_dxright)
 			throw new Exception("failed to find & modify X2_START");
 		    if(!found_dystart)
 			throw new Exception("failed to find & modify Y1_START");
 		}else{
 		    for(int i=0; i<numEnable; i++){
 			if(!found_xstart[i]) 
 			    throw new Exception("failed to find & modify X" + (i+1) + "_START");
 			if(!found_ystart[i]) 
 			    throw new Exception("failed to find & modify Y" + (i+1) + "_START");
 			if(!found_nx[i]) 
 			    throw new Exception("failed to find & modify X" + (i+1) + "_SIZE");
 			if(!found_ny[i]) 
 			    throw new Exception("failed to find & modify Y" + (i+1) + "_SIZE");
 		    }
 		    // SL - edited here to allow greater than 2 windows (18/7/2012)
 		    for(int i=numEnable; i<4; i++){
 			if(!found_nx[i]) 
 			    throw new Exception("failed to find & modify X" + (i+1) + "_SIZE");
 			if(!found_ny[i]) 
 			    throw new Exception("failed to find & modify Y" + (i+1) + "_SIZE");
 		    }
 		}
 		// Now add user stuff allowing for a user element being present or not
 		Element rootElement = document.getDocumentElement();
 		NodeList userNodes  = document.getElementsByTagName("user");
 		Element userElement = userNodes.getLength() == 0 ? document.createElement("user") : (Element) userNodes.item(0);
 
 
 		String target = "";
 		String progid = "";
 		String pi = "";
 		if (_runType == "data" || _runType == "technical") {
 			target = _objectText.getText();
 			progid = _progidText.getText();
 			pi = _piText.getText();
 		} else {
 			if (_runType.length() > 0) {
 				target = _runType.substring(0,1).toUpperCase() + _runType.substring(1);
 			}
 			progid = pi = "Calib";
 		}
 		_addToUser(document, userElement, "target",     target);
 		_addToUser(document, userElement, "grating",    _gratingText.getText());
 		_addToUser(document, userElement, "filters",    _filter.getSelectedItem()+"");
 		_addToUser(document, userElement, "slit_width", _slitWidthText.getText());
 		_addToUser(document, userElement, "slit_angle", _slitAngleText.getText());
 		_addToUser(document, userElement, "ID",         progid);
 		_addToUser(document, userElement, "PI",         pi);
 		_addToUser(document, userElement, "Observers",  _observerText.getText());
 		String flags = _runType;
 		if (_acquisitionState) {
 			flags = flags + " " + "caution";
 		}
 		_addToUser(document, userElement, "flags", flags);
 
 		// now use readback to try and get the current VERSION/REVISION
 		// only do this if we don't have a version yet
 		// this gets around limitation in the camera server which will
 		// STOP a run if it receives another command while running (!)
 		if (ULTRACAM_SERVERS_ON && posting && SERVER_READBACK_VERSION == 0) {
 			int verDecimal = -1;
 			String verReadback = "";
 			try {
 				URL url = new URL(HTTP_CAMERA_SERVER + HTTP_PATH_EXEC + "?RM,X,0x2E");
 				// readback is an xml attribute of command_status
 				String xmlString = _readText(url.openStream()).trim();
 				Document xmlDoc = _documentBuilder.parse(new InputSource(new StringReader(xmlString)));
 				Node commandNode = xmlDoc.getElementsByTagName("command_status").item(0);
 				NamedNodeMap commandAttributes = commandNode.getAttributes();
 				for (int i = 0; i < commandAttributes.getLength(); i++) {
 						String nodeName = commandAttributes.item(i).getNodeName().trim();
 						String nodeValue = commandAttributes.item(i).getNodeValue();
 						if (nodeName.equalsIgnoreCase("readback")) {
 							verReadback = nodeValue;
 							break;
 						}
 				}
 				if (verReadback.equals(""))
 					System.out.println("Didn't find readback in camera XML?");
 			} catch (Exception e) { System.out.println("Couldn't interrogate camera server for version readback.");}
 
 			// readback is in hex ie. "0xFF", convert to decimal
 			try {
 			    System.out.println(verReadback);
 				if (verReadback.substring(0,2).equals("0x"))
 					verReadback = verReadback.substring(2);
 				SERVER_READBACK_VERSION = Integer.parseInt(verReadback,16);
 			} catch (Exception e) { System.out.println("Couldn't convert readback version to decimal."); }
 		}
 
 		if (posting)
 		    _addToUser(document,userElement,"revision",Integer.toString(SERVER_READBACK_VERSION));
 
 
 		// Grab Temperature from Lakeshore
 		if (TEMP_FROM_LAKESHORE){
 			try{
 
 				File dirPath  = new File("/home/observer/Lakeshore/");
 				File[] files = dirPath.listFiles();
 
 				Arrays.sort(files, 
 					    new Comparator<File>(){
 						public int compare(File f1, File f2)
 						{
 						    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
 						} 
 					    });
 				
 				FileReader fr = new FileReader(files[files.length-1]);
 				BufferedReader br = new BufferedReader(fr);
 				String currentRecord=null;
 				
 				// get last line of file
 				while(br.ready()){
 				    currentRecord = br.readLine();
 				}
 				br.close();
 
 				String[] entries = currentRecord.split(",");
 				String date = entries[0]+","+entries[1]+","+entries[2]+","+entries[3]+" "+entries[4] + " GMT";
 				String ccdTemp = entries[5];
 
 				DateUtils dateTool = new DateUtils();
 				double elapsedTime = dateTool.elapsed(dateTool.parse(date));
 				if(elapsedTime < 0)
 				    throw new Exception("Lakeshore timestamp is in future!!");
 				if(elapsedTime > 100)
 				    throw new Exception("Lakeshore timestamp is more than 100 secs old (= " + elapsedTime + ")");
 
 				_addToUser(document, userElement, "ccd_temp", ccdTemp);	
 
 			}catch(Exception e){
 			    // warn
 			    JOptionPane.showMessageDialog(this,
 							  "Failed to get CCD temperatures from Lakeshore\n" + e,
 							  "USdriver Warning",
 							  JOptionPane.WARNING_MESSAGE);
 			}	
 		}
 
 		// 15/06/09 feed through output-inpendent values as seen in GUI
 		// SL - edited here to allow greater than 2 windows (18/7/2012)
 		if(isDriftMode){
 		    _addToUser(document, userElement, "X1_START", _windowPairs.getXleftText(0));
 		    _addToUser(document, userElement, "X2_START", _windowPairs.getXrightText(0));
 		}else{
 		    if(numEnable > 0)
 			_addToUser(document, userElement, "X1_START",  _singleWindows.getXstartText(0));
 		    if(numEnable > 1)
 			_addToUser(document, userElement, "X2_START",  _singleWindows.getXstartText(1));
 		    if(numEnable > 2)
 			_addToUser(document, userElement, "X3_START",  _singleWindows.getXstartText(2));
 		    if(numEnable > 3)
 			_addToUser(document, userElement, "X4_START",  _singleWindows.getXstartText(3));
 		}
 
 
 		// get slide position
 		try{
 		    if(SLIDE_ON){
 			String slideString = sendToSlide("position");
 			int slideStart = slideString.lastIndexOf(",");
 			int slideEnd   = slideString.indexOf("pixels",slideStart);
 			_addToUser(document, userElement, "SlidePos", slideString.substring(slideStart+2, slideEnd+6));  
 		    }
 		}catch(Exception e){
 		    JOptionPane.showMessageDialog(this,
 						  "Failed to get slide position from imedia PC\n"+e.toString(),
 						  "Usdriver Warning",
 						  JOptionPane.WARNING_MESSAGE);
 		}
 
 		_addToUser(document, userElement, "Program", "ULTRASPEC window creator and driver, version 2");
 
 		//		_addToUser(document, userElement, "Comment",    _commentText.getText());
 		_addBlankLine(document, userElement);
 
 
 
 		if(userNodes.getLength() == 0){
 		    rootElement.appendChild(userElement);
 		    _addBlankLine(document, rootElement);
 		}
 
 		return document;
 		
 	    }else{
 		throw new Exception("current settings are invalid; application was not edited");
 	    }	
 	}
 	catch(Exception e){
 	    throw new Exception("_createXML: " + e);
 	}
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /* Adds another XML tag of form <item>value</item> below element in the
      * XML document document. A blank line is inserted before each item */
     private void _addToUser(Document document, Element element, String item, String value){
 
 	_addBlankLine(document, element);
 	_addSpace(document, element, 4);
 
 	Element newElement = document.createElement(item);
 	Text elementText   = document.createTextNode(value);
 	newElement.appendChild(elementText);
 	element.appendChild(newElement);
 
     }
     //------------------------------------------------------------------------------------------------------------------------------------------
 	//
     private void _checkEnabledFields() {
 	boolean enable = true;
 	if (_runType == "data" || _runType == "technical") {
 	    enable = true;
 	} else {
 	    enable = false;
 	}
 	if (_acquisitionState == true) {
 	    _objectText.setBackground(WARNING_COLOUR);
 	} else {
 	    _objectText.setBackground(DEFAULT_COLOUR);
 	}
 	_piText.setEnabled(enable);
 	_progidText.setEnabled(enable);
 	_objectText.setEnabled(enable);
     }
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /* Adds a blank line to an element to give a nicer looking format */
     private void _addBlankLine(Document document, Element element) {
 	Text blankLine = document.createTextNode("\n\n");
 	element.appendChild(blankLine);
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /* Adds nspace spaces an element to give a nicer looking format */
     private void _addSpace(Document document, Element element, int nspace) {
 	StringBuffer buff = new StringBuffer();
 	for(int i=0; i<nspace; i++)
 	    buff.append(" ");
 	Text spaces = document.createTextNode(buff.toString());
 	element.appendChild(spaces);
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Execute a remote application This sends over the name of an application which 
      * must be located in a standard directory on the server side. This will then be executed.
      */
     private boolean _execRemoteApp(String application, boolean reset) {
 
 	String xmlString = null;
 
 	try {
 
 	    _execServer(HTTP_CAMERA_SERVER, application, "camera", reset);
 
 	    _execServer(HTTP_DATA_SERVER, application, "data", reset);
 
 	    logPanel.add("Executed <strong>" + application + "</strong> on both servers", LogPanel.OK, true);
 
 	    return true;
 	}
 	catch(SocketException e) {
        	    if(DEBUG) e.printStackTrace();
 	    String message = "A SocketException can be caused if an invalid CGI argument is used after \"" +
 		HTTP_PATH_CONFIG + "?\" in the URL.\n" +
 		"Make sure \"" + HTTP_PATH_CONFIG + "?" + application + "\" is supported by the servers.\n" +
 		"And check whether \"" + HTTP_PATH_CONFIG + "\" is supported by the servers.";
 	    
 	    JOptionPane.showMessageDialog(this, message, "SocketException", JOptionPane.WARNING_MESSAGE);
 	}
 	catch(SAXParseException e) {
        	    if(DEBUG) e.printStackTrace();
 	    System.out.println("XML start\n" + xmlString + "\nXML end");
 	    JOptionPane.showMessageDialog(this, e + "\nTry again.", e.getClass().getName(), JOptionPane.WARNING_MESSAGE);
 	}
 	catch(Exception e) {
 	    if(DEBUG) e.printStackTrace();
 	    JOptionPane.showMessageDialog(this, e + "\nTry again.", e.getClass().getName(), JOptionPane.WARNING_MESSAGE);
 	}
 	logPanel.add("Failed to execute <strong>" + application + "</strong>", LogPanel.ERROR, false);
 	return false;
 	
     }
     //------------------------------------------------------------------------------------------------------------------------------------------
     private boolean _verifyTarget(String target) {
 	boolean exists = false;
 	if (USE_UAC_DB) {
 	    exists = _uacExists(target);
 	}
 	if (!exists) {
 	    exists = _simbadExists(target);
 	}
 	if (!exists) {
 	    if (USE_UAC_DB) {
 		logPanel.add("Could not find target in UAC database or SIMBAD.",LogPanel.ERROR,false);
 	    } else {
 		logPanel.add("SIMBAD lookup <strong>failed.</strong>",LogPanel.ERROR,false);
 	    }
 	}
 	return exists;
     }
     //------------------------------------------------------------------------------------------------------------------------------------------
     private boolean _uacExists(String target) {
 	Connection conn = null;
 	int count = 0;
 	try {
 	    //Class.forName("com.mysql.jdbc.Driver").newInstance();
 	    String usern = "ultracam";
 	    String passw = "nogales";
 	    String url = "jdbc:mysql://" + UAC_DATABASE_HOST + "/uac";
 	    conn = DriverManager.getConnection(url,usern,passw);
 	    Statement s = conn.createStatement();
 	    s.executeQuery("SELECT id FROM objects WHERE names LIKE '%" + target + "%' LIMIT 1;");
 	    ResultSet rs = s.getResultSet();
 	    while (rs.next()) {
 		++count;
 		logPanel.add("Found UAC id <strong>" + rs.getString("id") + ".</strong>",LogPanel.OK,true);
 	    }
 	} catch (SQLException e) { System.out.println(e);}
 	finally {
 	    if (conn != null) {
 		try {
 		    conn.close();
 		} catch (Exception e) {}
 	    }
 	}
 	if (count > 0) {
 	    return true;
 	} else {
 	    return false;
 	}
     }
     //------------------------------------------------------------------------------------------------------------------------------------------
     //
     private boolean _simbadExists(String target) {
 	String script = null;
 	String result = null;
 	script = "set limit 1\n";
 	script = script + "format object form1 \"%IDLIST(1) : %COO(A) : %COO(D)\"\n";
 	script = script + "echodata ** UDRIVER QUERY\n";
 	script = script + "query id " + target + "\n";
 	try{
 	    script = URLEncoder.encode(script,"ISO-8859-1");
 	    URL simbadurl = new URL("http://simbad.u-strasbg.fr/simbad/sim-script?submit=submit+script&script=" + script);
 	    result = _readText(simbadurl.openStream());
 	    //System.out.println(result);
 	} catch(UnsupportedEncodingException uee) {
 	    System.out.println(uee);
 	} catch(MalformedURLException mue) {
 	    System.out.println(mue);
 	} catch(IOException ioe) {
 	    System.out.println(ioe);
 	}
 	String [] simbad = result.split("\n");
 	int startline = 0;
 	for (int i = 0 ; i < simbad.length ; i++) {
 	    if (simbad[i].indexOf("::data::") > -1) {
 		startline = i;
 	    }
 	    if (simbad[i].indexOf("::error::") > -1) {
 		System.out.println("Encountered simbad error");
 		return false;
 	    }
 	}
 	for (int i = startline ; i < simbad.length ; i++) {
 	    if (simbad[i].indexOf("** UDRIVER QUERY") > -1) {
 		if (simbad.length > (i+1)) {
 		    if (simbad[i+1].split(":").length == 3) {
 			logPanel.add("SIMBAD lookup <strong>success.</strong>",LogPanel.OK,true);
 		    } else {
 			return false;
 		    }
 		} else {
 		    return false;
 		}
 	    }
 	}
 	return true;
 	
     }
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Execute a command. This is method that sends the requests to start and stop
      * runs etc.
      * @param command "GO" to start a run, "ST" to stop it, "RCO" to reset timing board,
      * "RST" to reset the PCI board  
      */
     private boolean _execCommand(String command, boolean reset) {
 	// execute command 'quietly'
 	return _execCommand(command,reset,false);
     }
 
     private boolean _execCommand(String command, boolean reset, boolean loud) {
 
 	logPanel.add("Sent command <strong>" + command + "</strong>", LogPanel.OK, true);
 
 	String xmlString = null;
 
 	try {
 
 	    URL url = new URL(HTTP_CAMERA_SERVER + HTTP_PATH_EXEC + "?" + command);
 
 	    xmlString = _readText(url.openStream()).trim();
 	    if(loud){
 		JOptionPane.showMessageDialog(this, xmlString, "Server Response", JOptionPane.INFORMATION_MESSAGE);
 	    }
 	    Document xmlDoc = _documentBuilder.parse(new InputSource(new StringReader(xmlString)));
 
 	    _replyPanel.showReply(xmlDoc, "Response to command = " + command, reset);
 	    if(!isResponseOK(xmlDoc))
 		throw new Exception("XML response from camera server " + HTTP_CAMERA_SERVER + " was not OK");
 
 	    logPanel.add("Executed command <strong>" + command + "</strong>", LogPanel.OK, true);
 
 	    return true;
 
 	}
 	catch(SocketException e) {
 	    if(DEBUG) e.printStackTrace();
 	    JOptionPane.showMessageDialog(this, "Check that the server = " + HTTP_CAMERA_SERVER + " is active", "SocketException", JOptionPane.WARNING_MESSAGE);
 	}
 	catch(SAXParseException e) {
 	    if(DEBUG) e.printStackTrace();
 	    System.out.println("XML start\n" + xmlString + "\nXML end");
 	    JOptionPane.showMessageDialog(this, e + "\nTry again.", e.getClass().getName(), JOptionPane.WARNING_MESSAGE);
 	}
 	catch(Exception e) {
 	    if(DEBUG) e.printStackTrace();
 	    JOptionPane.showMessageDialog(this, e + "\nTry again.", e.getClass().getName(), JOptionPane.WARNING_MESSAGE);
 	}
 	logPanel.add("Failed to execute command <strong>" + command + "</strong>", LogPanel.ERROR, false);
 	return false;
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Sends an application to a server **/
     void _execServer(String server, String application, String name, boolean reset) throws Exception {
 	URL      url       =  new URL(server + HTTP_PATH_CONFIG + "?" + application);
 	String   xmlString = _readText(url.openStream()).trim();
 	Document document  = _documentBuilder.parse(new InputSource(new StringReader(xmlString)));
 	_replyPanel.showReply(document, "Response to " + application + " from " + name + " server", reset);
 	if(!isResponseOK(document))
 	    throw new Exception("Response from " + name + " server = " + server + " to application " + application + " was not OK");
     }
 
     /** Initialises the servers using remotely stored applications */
     private boolean _setupServers(boolean reset) {
 	try {
 	    
 	    _execServer(HTTP_CAMERA_SERVER, _telescope.application, "camera", reset);
 
 	    _execServer(HTTP_CAMERA_SERVER, GENERIC_APP, "camera", false);
 
 	    _execServer(HTTP_DATA_SERVER, _telescope.application, "data", false);
 
 	    _execServer(HTTP_DATA_SERVER, GENERIC_APP, "data", false);
 
 	    return true;
 	}
 	catch(Exception e) {
 	    logPanel.add(e.toString(), LogPanel.ERROR, false);
 	    logPanel.add("Failed to setup servers", LogPanel.ERROR, false);
 	    _showExceptionDialog(e);
 	    return false;
 	}
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Polls the data server to see if a run is active */
     public boolean isRunActive(boolean quiet) {
 	try { 
 	    URL url           = new URL(HTTP_DATA_SERVER + "status");
 	    String reply      = _readText(url.openStream()).trim();
 	    Document document = _documentBuilder.parse(new InputSource(new StringReader(reply)));
 	    NodeList nlist    = document.getElementsByTagName("state");
 	    if(nlist.getLength() == 0)
 		throw new Exception("Could not find 'state' element in XML returned from the server");
 	    
 	    Element element   = (Element)nlist.item(0);
 	    if(element.hasAttribute("server")){
 		if(element.getAttribute("server").equals("IDLE")){
 		    return false;
 		}else if(element.getAttribute("server").equals("BUSY")){
 		    return true;
 		}else{
 		    throw new Exception("Failed to interpret 'state' value from server = " + element.getAttribute("server"));
 		}
 	    }else{
 		throw new Exception("'state' element in XML from server did not have 'server' attribute");
 	    }
 	}
 	catch(Exception e){
 	    if(!quiet){
 		logPanel.add(e.toString(), LogPanel.ERROR, false);
 		logPanel.add("Will assume that a run IS active", LogPanel.ERROR, false);
 	    }
 	    return true;
 	}
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Gets the run number */
     public void getRunNumber() {
 	try {
 	    URL url           = new URL(HTTP_DATA_SERVER + "fstatus");
 	    String reply      = _readText(url.openStream()).trim();
 	    Document document = _documentBuilder.parse(new InputSource(new StringReader(reply)));
 	    NodeList nlist    = document.getElementsByTagName("lastfile");
 	    if(nlist.getLength() == 0)
 		throw new Exception("Could not find 'lastfile' element in XML returned from the server");
 	    
 	    Element element   = (Element)nlist.item(0);
 	    if(element.hasAttribute("path")){
 		String path   = element.getAttribute("path").trim();
 		if(path.length() > 2){
 		    String numString = path.substring(path.length()-3);
 		    int number = Integer.parseInt(numString);
 		    _runNumber.setText(String.valueOf(number));
 		}else{
 		    throw new Exception("Path = " + path + " not long enough for 3 digit run number");
 		}
 	    }else{
 		throw new Exception("'lastfile' element in XML from server does not have 'path' attribute");
 	    }
 	}
 	catch(Exception e) {
 	    logPanel.add("Failed to determine run number; will be set blank", LogPanel.ERROR, false);
 	    System.out.println(e);
 	    _runNumber.setText("");
 	}
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Increment run number */
     public void incrementRunNumber(){	
 	try {
 	    String numString = _runNumber.getText();
 	    if(numString.equals(""))
 		throw new Exception("Run number is blank, which means that it cannot be incremented");
 	    int number = Integer.parseInt(numString);
 	    number++;
 	    _runNumber.setText(String.valueOf(number));
 	}
 	catch(Exception e) {
 	    logPanel.add(e.toString(), LogPanel.ERROR, false);
 	    _runNumber.setText("");
 	}
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Tests whether XML response from server is OK or not
      * It does so by looking for an element of the form
      * <status> and then looking for 'software' and possibly
      * 'camera' attrributes depending upon the source
      */
     public boolean isResponseOK(Document document){
 	
 	try{
 
 	    NodeList nlist = document.getElementsByTagName("source");
 	    if(nlist.getLength() == 0)
 		throw new Exception("Could not find 'source' element in XML returned from the server");   
 	    Node node = nlist.item(0).getFirstChild();
 	    if(node == null)
 		throw new Exception("'source' had no children in XML returned from the server");   
 
 	    String source = node.getNodeValue().trim();
 	    if(source == null)
 		throw new Exception("'source' value was null in XML returned from the server");   
 
 	    // Need software="OK" in <status> tag
 	    nlist    = document.getElementsByTagName("status");
 	    if(nlist.getLength() == 0)
 		throw new Exception("Could not find 'status' element in XML returned from the server");
 	    Element element   = (Element)nlist.item(0);
 	    if(element.hasAttribute("software")){
 		if(!element.getAttribute("software").equals("OK"))
 		    throw new Exception("'software' attribute of 'status' element = " + element.getAttribute("software") + " not = OK from source = " + source);
 	    }else{
 		throw new Exception("Could not find 'software' attribute of 'status' element from source = " + source);
 	    }
 
 	    if(source.equals("Camera server")){
 
 		// Need camera="OK" in <status> tag
 		if(element.hasAttribute("camera")){
 		    if(!element.getAttribute("camera").equals("OK"))
 			throw new Exception("'camera' attribute of 'status' element = " + element.getAttribute("camera") + " not = OK from source = " + source);
 		}else{
 		    throw new Exception("Could not find 'camera' attribute of 'status' element from source = " + source);
 		}
 
 	    }else if(!source.equals("Filesave data handler")){
 
 		   throw new Exception("source = " + source + " not recognised. Expected either 'Camera server' or 'Filesave data handler'");
 	    }
 	    
 	    return true;
 	}
 	catch(Exception e){
 	    logPanel.add(e.toString(), LogPanel.ERROR, false);
 	    return false;
 	}
     }
 	    
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Returns the index of the current application. Should be done with a map
      * but this will have to do for now.
      */
     private int _whichTemplate(){
 	int iapp = 0;
 	for(iapp=0; iapp<TEMPLATE_LABEL.length; iapp++)
 	    if(applicationTemplate.equals(TEMPLATE_LABEL[iapp])) break;
 	if(iapp == TEMPLATE_LABEL.length){
 	    System.out.println("Template = " + applicationTemplate + " not recognised.");
 	    System.out.println("This is a programming or configuration file error and the program will terminate.");
 	    try{
 		if(wheel.isConnected())wheel.close();
 	    }catch(Exception e){
 		System.out.println("Couldn't relinquish FW control.");
 	    }
 	    System.exit(0);
 	}
 	return iapp;
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 		
     /** Sets the number of windows/window pairs in use */
     public void setNumEnable(){
 	try{
 	    numEnable = Math.min(numWin, Integer.parseInt(TEMPLATE_PAIR[_whichTemplate()]));
 	}
 	catch(Exception e){
 	    e.printStackTrace();
 	    System.out.println(e);
 	    System.out.println("Probable error in TEMPLATE_PAIR in configuration file = " + CONFIG_FILE);
 	    System.out.println("This is a programming or configuration file error and the program will terminate.");
 	    try{
 		if(wheel.isConnected())wheel.close();
 	    }catch(Exception ex){
 		System.out.println("Couldn't relinquish FW control.");
 	    }
 	    System.exit(0);
 	}
     }
 
     public boolean isnormal(){
 	// avalanche mode y/n?
 	ccdOutput = (String) ccdOutputChoice.getSelectedItem();
 	boolean lnormal = false;
 	if(ccdOutput.equals("Avalanche")){
 	    lnormal=false;
 	}else if(ccdOutput.equals("Normal")){
 	    lnormal=true;
 	}else{
 	    throw new Error("isnormal(): ccd mode = \"" + ccdOutput + "\" is unrecognised. Programming error");
 	}
 	return lnormal;
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** This routine implements's Vik's speed computations and reports
      *	the frame rate in Hertz, the cycle time (e.g. sampling time),
      * exposure time (time on source per cycle), the dead time and readout
      * time, all in seconds. Finally it also reports the duty cycle, and
      * in the case of drift mode, the number of windows in the storage area
      * along with the pipe shift in pixels.
      */
 
     public void speed() {
 
 	try{
 
 	    if(isValid(_validStatus)){
 		
 		// avalanche mode y/n?
 		boolean lnormal = isnormal();
 		double HCLOCK = lnormal ? HCLOCK_NORM : HCLOCK_AV;
 		
 		// drift mode y/n?
 		boolean isDriftMode = driftModeEnabled.isSelected();
 
 		// Set the readout speed
 		readSpeed = (String) speedChoice.getSelectedItem();
 
 		double video;		    
 		if(readSpeed.equals("Fast")){
 		    if(lnormal){
 				video = VIDEO_NORM_FAST;
 		    }else{
 				video = VIDEO_AV_FAST;
 		    }
 		}else if(readSpeed.equals("Medium")){
 		    if(lnormal){
 				video = VIDEO_NORM_MED;
 		    }else{
 				video = VIDEO_AV_MED;
 		    }
 		}else if(readSpeed.equals("Slow")){
 		    if(lnormal){
 				video = VIDEO_NORM_SLOW;
 		    }else{
 				video = VIDEO_AV_SLOW;
 		    }
 		}else{
 		    throw new Error("readSpeed = \"" + readSpeed + "\" is unrecognised. Programming error");
 		}
 
 		// clear chip on/off?
 		boolean lclear = false;
 		if(! isDriftMode){	  
 		    if(clearEnabled.isSelected()) lclear=true;
 		} 
 
 		// get exposure delay (in units of 0.1 ms) and binning factors
 		expose = _getExpose();
 		xbin   = xbinText.getValue();	
 		ybin   = ybinText.getValue();	
 
 		// window parameters
 		// SL - edited here to allow greater than 2 windows (18/7/2012)
 		double[] xstart = new double[4];
 		double[] ystart = new double[4];
 		double[] xsize  = new double[4];
 		double[] ysize  = new double[4];
 		for(int np=0; np<numEnable; np++){
 		    xstart[np] = _singleWindows.getXstart(np);
 		    ystart[np] = _singleWindows.getYstart(np);
 		    xsize[np]  = _singleWindows.getNx(np);
 		    ysize[np]  = _singleWindows.getNy(np);
 		}
 		// alternatives for drift mode
 		double dxleft, dxright, dystart, dxsize, dysize;
 		dxleft  = _windowPairs.getXleft(0);
 		dxright = _windowPairs.getXright(0);
 		dystart = _windowPairs.getYstart(0);
 		dxsize = _windowPairs.getNx(0);
 		dysize = _windowPairs.getNy(0);
 
 		if(lnormal){
 		    // normal mode convert xstart by ignoring 16 overscan pixels
 		    for(int np=0; np<numEnable; np++){
 				xstart[np] += 16;
             }
 		    dxleft += 16;
 		    dxright += 16;
 		}else{
 		    // in avalanche mode, need to swap windows around
 		    for(int np=0; np<numEnable; np++){
 				xstart[np] = FFX - (xstart[np]-1) - (xsize[np]-1);
 		    }
 		    dxright = FFX - (dxright-1) - (dxsize-1);
 		    dxleft = FFX - (dxleft-1) - (dxsize-1);
 		    // in drift mode, also need to swap the windows around
 		    double tmp = dxright;
 		    dxright=dxleft;
 		    dxleft=tmp;
 		}
 
 		// convert timing parameters to seconds
 		double expose_delay = expose*1.0e-4;
 
 		// clear chip by VCLOCK-ing the image and storage areas
 		double clear_time;
 		if(lclear){
 		    // changed to accomodate changes to clearing made by DA to fix dark current
 		    // when clearing charge along normal output
 		    // SL - 2013/03/11
 		    //clear_time = 2.0*(FFY*VCLOCK+39.e-6);
 		    clear_time = 2.0*(FFY*VCLOCK+39.e-6) + FFX*HCLOCK_NORM + 2162.0*HCLOCK_AV;
 		}else{
 		    clear_time = 0.0;
 		}
 
 
 		// for drift mode, we need the number of windows in the pipeline and the pipeshift
 		int pnwin = (int)(((1037. / dysize) + 1.)/2.);
 		double pshift = 1037.- (2.*pnwin-1.)*dysize;
 		   
 
 		/** If not drift mode, move entire image into storage area
 		    the -35 component is because Derek only shifts 1037 pixels
 		    (composed of 1024 active rows, 5 dark reference rows, 2 transition rows
 		    and 6 extra overscan rows for good measure) 
 
 		    If drift mode, just move the window into the storage area
 		**/
 		double frame_transfer = (FFY-35)*VCLOCK + 49.0e-6;
 		if(isDriftMode)
 		    frame_transfer = (dysize+dystart-1.)*VCLOCK + 49.0e-6;
 
 		// calculate the yshift, which places windows adjacent to the serial register
 		// edited here to allow >2 windows - SL (18/7/2012)
         double[] yshift = new double[numEnable];
 		for (int np=0; np<numEnable; np++) yshift[np]=0.0;
                 if(isDriftMode){
                         yshift[0]=(dystart-1.0)*VCLOCK;
                 }else{
                         yshift[0]=(ystart[0]-1.0)*VCLOCK;
                         for (int np=1; np<numEnable; np++){
                                 yshift[np] = (ystart[np]-ystart[np-1]-ysize[np-1])*VCLOCK;
                         }
                 }
 		
 		
 		/* After placing the window adjacent to the serial register, the register must be cleared
 		   by clocking out the entire register, taking FFX hclocks (we no longer open the dump gates,
 		   which took only 8 hclock cycles to complete, but gave ramps and bright rows
 		   in the bias). We think dave does 2*FFX hclocks in avalanche mode, but need
 		   to check this with him.
 		*/
 		double[] line_clear = new double[numEnable];
 		for(int np=0; np<numEnable; np++) line_clear[np]=0.0;
 		double hclockFactor = 1.0;
 		if(! lnormal){
 		    hclockFactor = 2.0;
 		}
                 if(isDriftMode){
 		    if(yshift[0] != 0) line_clear[0] = hclockFactor*FFX*HCLOCK;
                 }else{
 		    for(int np=0; np<numEnable; np++){
 			if(yshift[np] != 0) line_clear[np] = hclockFactor*FFX*HCLOCK;
 		    }
                 }
 
 		// calculate how long it takes to shift one row into the serial register
 		// shift along serial register and then read out the data. The charge in a row
 		// after a window used to be dumped, taking 8 HCLOCK cycles. This created ramps 
 		// and bright rows/columns in the images, so was removed.
 		int[] numhclocks = new int[numEnable];
 		for(int np=0; np<numEnable; np++) numhclocks[np] = 0;		  
 		if(isDriftMode){
 		    numhclocks[0] = FFX;
 		    if (!lnormal) numhclocks[0] += AVALANCHE_PIXELS;
 		}else{
 		    for(int np=0; np<numEnable; np++){
 			numhclocks[np] = FFX;
 			if(! lnormal) numhclocks[np] += AVALANCHE_PIXELS;
 		    }
 		}
 
 		// edited here to allow >2 windows - SL (18/7/2012)
                 double[] line_read  = new double[numEnable];
 		for(int np=0; np<numEnable; np++) line_read[np]=0.0;
                 if(isDriftMode){
                         line_read[0] = VCLOCK*ybin + numhclocks[0]*HCLOCK + video*2.0*dxsize/xbin;
                 }else{
                         for(int np=0; np<numEnable; np++){
                                 line_read[np] = (VCLOCK*ybin) + (numhclocks[np]*HCLOCK) + (video*xsize[np]/xbin);
                         }
                 }
 
 		// edited here to allow >2 windows - SL (18/7/2012)
 		// multiply time to shift one row into serial register by number of rows for total readout time
                 double[] readout = new double[numEnable];
 		for(int np=0; np<numEnable; np++) readout[np]=0.0;
                 if(isDriftMode){
                         readout[0] = (dysize/ybin) * line_read[0];
                 }else{
                         for(int np=0; np<numEnable; np++){
 			    readout[np] = (ysize[np]/ybin) * line_read[np];
                         }
                 }
 
 		// now get the total time to read out one exposure.
 		// edited here to allow >2 windows - SL (18/7/2012)
 		double cycleTime, frameRate, expTime, deadTime, dutyCycle;
 		cycleTime = expose_delay + clear_time + frame_transfer;
 		if(isDriftMode){
 		    cycleTime += pshift*VCLOCK+yshift[0]+line_clear[0]+readout[0];
 		}else{
 		    for(int np=0; np<numEnable; np++){
 			cycleTime += yshift[np] + line_clear[np] + readout[np];
 		    }
 		}
 
 		frameRate = 1.0/cycleTime;
 		if(lclear){
 		    expTime = expose_delay;
 		}else{
 		    expTime   = cycleTime - frame_transfer;
 		}
 		deadTime  = cycleTime - expTime;
 		dutyCycle = 100.0*expTime/cycleTime;
 
 		_frameRate.setText(round(frameRate,3));
 		_cycleTime.setText(round(cycleTime,3));
 		_dutyCycle.setText(round(dutyCycle,2));
 		_expTime.setText(round(expTime,3));
 
 		// calculate SN info. Not a disaster if this fails, so make
 		// sure failures are non-fatal with a try block
 		final double AP_SCALE = 1.5;
 		double zero = 0., sky = 0., skyTot = 0., gain = 0., read = 0., darkTot = 0.;
 		double total = 0., peak = 0., correct = 0., signal = 0., readTot = 0., seeing = 0.;
 		double noise = 1., skyPerPixel = 0., narcsec = 0., npix = 0., signalToNoise = 0.;
 		try {
 		    // Get the parameters for magnitudes
 		    zero    = _telescope.zeroPoint[_filterIndex];
 		    double mag     = _magnitudeText.getValue();
 		    seeing  = _seeingText.getValue();
 		    sky     = SKY_BRIGHT[_skyBrightIndex][_filterIndex];
 		    double airmass = _airmassText.getValue();
 		    //GAIN, RNO
 		    if(readSpeed.equals("Fast")){
 			if(lnormal){
 			    gain = GAIN_NORM_FAST;
 			    read = RNO_NORM_FAST;
 			}else{
 			    gain = GAIN_AV_FAST;
 			    read = RNO_AV_FAST;
 			}
 		    }else if(readSpeed.equals("Medium")){
 			if(lnormal){
 			    gain = GAIN_NORM_MED;
 			    read = RNO_NORM_MED;
 			}else{
 			    gain = GAIN_AV_MED;
 			    read = RNO_AV_MED;
 			}
 		    }else if(readSpeed.equals("Slow")){
 			if(lnormal){
 			    read = RNO_NORM_SLOW;
 			    gain = GAIN_NORM_SLOW;
 			}else{
 			    read = RNO_AV_SLOW;
 			    gain = GAIN_AV_SLOW;
 			}
 		    }else{
 			throw new Error("readSpeed = \"" + readSpeed + "\" is unrecognised. Programming error");
 		    }
 
 		    double plateScale = _telescope.plateScale;
 
 		    // Now calculate expected electrons 
 		    total = Math.pow(10.,(zero-mag-airmass*EXTINCTION[_filterIndex])/2.5)*expTime;
 		    peak  = total*xbin*ybin*Math.pow(plateScale/(seeing/2.3548),2)/(2.*Math.PI);
 
 		    // Work out fraction of flux in aperture with radius AP_SCALE*seeing
 		    correct      = 1. - Math.exp(-Math.pow(2.3548*AP_SCALE, 2)/2.);
 		    
 		    // expected sky e- per arcsec
 		    double skyPerArcsec = Math.pow(10.,(zero-sky)/2.5)*expTime;
 		    skyPerPixel = skyPerArcsec*Math.pow(plateScale,2)*xbin*ybin;
 		    narcsec     = Math.PI*Math.pow(AP_SCALE*seeing,2);
 		    skyTot      = skyPerArcsec*narcsec;
 		    npix        = Math.PI*Math.pow(AP_SCALE*seeing/plateScale,2)/xbin/ybin;
 
 		    signal      = correct*total; // in electrons
 		    darkTot     = npix*DARK_E*expTime; // in electrons
 		    readTot     = npix*Math.pow(read, 2); // in electrons
 		    double cic = 0.0;
 		    if (! lnormal){
 			cic = CIC;
 		    }
 		    if(lnormal){
 			noise = Math.sqrt(readTot + darkTot + skyTot + signal + cic); // in electrons
 		    }else{
 			// assume high gain observations in proportional mode
 			noise = Math.sqrt(readTot/Math.pow(AVALANCHE_GAIN_9,2) + 2.0*(darkTot + skyTot + signal) + cic); // in electrons
 		    }
 		    // Now compute signal-to-noise in 3 hour seconds run
 		    signalToNoise = signal/noise*Math.sqrt(3*3600./cycleTime);
 
 		    //if using the avalanche mode, check that the signal level is safe. 
 		    //A single electron entering the avalanche register results in a distribution
 		    //of electrons at the output with mean value given by the parameter
 		    //avalanche_gain. The distribution is an exponential, hence the probability
 		    //of obtaining an amplification n times higher than the mean is given
 		    //by e**-n. A value of 3/5 for n is adopted here for warning/safety, which will occur
 		    //once in every ~20/100 amplifications
 
 		    // convert from electrons to counts
 		    total /= gain;
 		    peak /= gain;
 
 		    _totalCounts.setText(round(total,1));
 		    
 		    peak = (int)(100.*peak+0.5)/100.;
 		    _peakCounts.setText(round(peak,2));
 		    double warn=25000;
 		    double sat=60000;
 		    if(! lnormal){
 			sat = AVALANCHE_SATURATE/AVALANCHE_GAIN_9/5/gain;
 			warn = AVALANCHE_SATURATE/AVALANCHE_GAIN_9/3/gain;
 		    }
 		    if(peak > sat){
 			_peakCounts.setBackground(ERROR_COLOUR);
 		    }else if(peak > warn){
 			_peakCounts.setBackground(WARNING_COLOUR);
 		    }else{
 			_peakCounts.setBackground(DEFAULT_COLOUR);
 		    }
 		    
 		    _signalToNoise.setText(round(signalToNoise,1));
 		    _signalToNoiseOne.setText(round(signal/noise,2));
 		    
 		    _magInfo = true;
 		    
 		}
 		catch(Exception e){
 		    _totalCounts.setText("");
 		    _peakCounts.setText("");
 		    if(_magInfo)
 			System.out.println(e.toString());
 		    _magInfo = false;
 		}    
 	    }
 	}
 
 	catch(Exception e){
 	    _frameRate.setText("UNDEFINED");
 	    _cycleTime.setText("UNDEFINED");
 	    _dutyCycle.setText("UNDEFINED");
 	    _expTime.setText("UNDEFINED");
 	    logPanel.add(e.toString(), LogPanel.ERROR, false);
 	}
 
     }
 
     /** Computes the number of bytes per image as needed to estimate the disk space used. */
     public int nbytesPerImage(){
 
 	try{
 
 	    if(isValid(_validStatus)){
 
 		// time bytes
 		int n = 24;
 
 		if(applicationTemplate.equals("Window mode")){
 
 		    for(int i=0; i<numEnable; i++)
 			n += 2*(_singleWindows.getNx(i) / xbin ) * (_singleWindows.getNy(i) / ybin );
 
 		}else{
 		    // 2 bytes per pixel, and 2 windows of nx by ny
 		    n+= 2 * 2 *(_windowPairs.getNx(0) / xbin) * (_windowPairs.getNy(0) / ybin);
 		}
 		
 		return n;
 		    
 	    }
 	}
 	catch(Exception e){
 	    logPanel.add(e.toString(), LogPanel.ERROR, false);
 	}
 	return 1;
     }
 
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** This routine sets up a server for rtplot so that rtplot can grab the current window values.
      *  It attempts to sends the current values over whether they are OK or not, and leaves rtplot to check them.
      *  Note that the port number used here must match the one used in rtplot.
      */
     public void runRtplotServer() {
 
 	try {
 
 	    ServerSocket ss = new ServerSocket(5100);
 	    
 	    for(;;){
 		Socket     client = ss.accept();
 		BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
 		PrintWriter   out = new PrintWriter(client.getOutputStream());
 		
 		out.print("HTTP/1.0 200 OK\r\n");
 		out.print("Content-Type: text/plain\r\n");
 
 		// OK, now we send the windows information if we can get it	
 		try {
 		    if(driftModeEnabled.isSelected()){
 			// Get everything first before attempting to respond
 			xbin = xbinText.getValue();
 			ybin = ybinText.getValue();	
 			String binFactors  = new String(xbin + " " + ybin + " " + 2 + "\r\n");
 			int content_length = binFactors.length();
 			String[] window = new String[2];
 			int xstart, ystart, nx, ny;
 			xstart = _windowPairs.getXleft(0);
 			ystart = _windowPairs.getYstart(0);
 			nx     = _windowPairs.getNx(0);
 			ny     = _windowPairs.getNy(0);
 			window[0]   = new String(xstart + " " + ystart + " " + nx + " " + ny + "\r\n");
 			content_length += window[0].length();
 			xstart = _windowPairs.getXright(0);
 			ystart = _windowPairs.getYstart(0);
 			nx     = _windowPairs.getNx(0);
 			ny     = _windowPairs.getNy(0);
 			window[1]   = new String(xstart + " " + ystart + " " + nx + " " + ny + "\r\n");
 			content_length += window[1].length();
 
 			out.print("Content-Length: " + content_length + "\r\n\r\n");
       			// Now the content
 			out.print(binFactors);
 			for(int i=0; i<2; i++)
 			    out.print(window[i]);
 			
 		    }else{
 			// Get everything first before attempting to respond
 			xbin = xbinText.getValue();
 			ybin = ybinText.getValue();	
 			setNumEnable();
 			String binFactors  = new String(xbin + " " + ybin + " " + Math.max(1, numEnable) + "\r\n");
 			int content_length = binFactors.length();
 			String[] window    = new String[Math.max(1,numEnable)];
 			if(numEnable > 0){
 			    int xstart, ystart, nx, ny;
 			    for(int i=0; i<numEnable; i++){
 				xstart = _singleWindows.getXstart(i);
 				ystart = _singleWindows.getYstart(i);
 				nx     = _singleWindows.getNx(i);
 				ny     = _singleWindows.getNy(i);
 				window[i]   = new String(xstart + " " + ystart + " " + nx + " " + ny + "\r\n");
 				content_length += window[i].length();
 			    }
 			}
 
 			out.print("Content-Length: " + content_length + "\r\n\r\n");
 			
 			// Now the content
 			out.print(binFactors);
 			for(int i=0; i<numEnable; i++)
 			    out.print(window[i]);
 		    }
 		    if(DEBUG) System.out.println("Have just responded to a request from rtplot");
 		}
 		catch(Exception e){
 		    out.print("Content-Length: 26\r\n\r\n");
 		    out.print("No valid data available\r\n");
 		    System.out.println(e);
 		    System.out.println("Failed to respond to a request from rtplot");
 		    _showExceptionDialog(e);
 		}
 		out.close();
 		in.close();
 		client.close();
 	    }	    
 	}
 	catch(Exception e) {
 	    if(DEBUG) e.printStackTrace();
 	    System.err.println(e);
 	    _showExceptionDialog(e);
 	}
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Main program. Calls constructor and starts rtplot server */
 
     public static void main(String[] args) {
 	Usdriver cw = new Usdriver();
 	if(RTPLOT_SERVER_ON){
 	    logPanel.add("Starting rtplot server", LogPanel.WARNING, false);
 	    cw.runRtplotServer();
 	}
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /* Choose an XML file name for saving an application */
     private boolean _chooseSaveApp() {
 	try {
 	    int result = _xmlFileChooser.showSaveDialog(null);
 	    if(result == JFileChooser.APPROVE_OPTION){
 		_xmlFile = _xmlFileChooser.getSelectedFile();
 		if (_xmlFile.getPath().indexOf(".xml") != _xmlFile.getPath().length() - 4 ){
 		    String newFilePath = _xmlFile.getPath() + ".xml";
 		    _xmlFile = new File(newFilePath);
 		}
 		return true;
 	    }else{
 		throw new Exception("No XML file name chosen for saving application");
 	    }
 	}
 	catch(Exception e){
 	    logPanel.add(e.toString(), LogPanel.ERROR, false);
 	    return false;
 	}
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /* Choose an XML file name for loading an application */
     private boolean _chooseLoadApp() {
 	try {
 	    int result = _xmlFileChooser.showOpenDialog(null);
 	    if(result == JFileChooser.APPROVE_OPTION){
 		_xmlFile = _xmlFileChooser.getSelectedFile();
 		return true;
 	    }else{
 		throw new Exception("No XML file name chosen for loading application");
 	    }
 	}
 	catch(Exception e){
 	    logPanel.add(e.toString(), LogPanel.ERROR, false);
 	    return false;
 	}
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     // Method for adding components to GridBagLayout for the window panel
     private static void addComponent (Container cont, Component comp, int gridx, int gridy, 
 				      int gridwidth, int gridheight, int fill, int anchor){
 
 	GridBagConstraints gbc = new GridBagConstraints ();
 	gbc.gridx      = gridx;
 	gbc.gridy      = gridy;
 	gbc.gridwidth  = gridwidth;
 	gbc.gridheight = gridheight;
 	gbc.fill       = fill;
 	gbc.anchor     = anchor;
 	gbLayout.setConstraints(comp, gbc);
 	cont.add (comp);
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     // Method for adding components to GridBagLayout for the action panel
     private static void addActionComponent (Container cont, Component comp, int gridx, int gridy){
 
 	GridBagConstraints gbc = new GridBagConstraints ();
 	gbc.gridx      = gridx;
 	gbc.gridy      = gridy;
 	gbc.gridwidth  = 1;
 	gbc.gridheight = 1;
 	gbc.insets     = new Insets(0,5,0,5);
 	gbc.fill       = GridBagConstraints.HORIZONTAL;
 	gbc.anchor     = GridBagConstraints.CENTER;
 	gbLayout.setConstraints(comp, gbc);
 	cont.add (comp);
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Splits up multiple arguments from configuration file */
     private String[] _loadSplitProperty(Properties properties, String key) throws Exception {
 	String propString = _loadProperty(properties, key);
 	StringTokenizer stringTokenizer = new StringTokenizer(propString, ";\n");
 	String[] multiString = new String[stringTokenizer.countTokens()];
 	int i = 0;
 	while(stringTokenizer.hasMoreTokens())
 	    multiString[i++] = stringTokenizer.nextToken().trim();
 	return multiString;
     }
     private void _updateSplitProperty(Properties properties, String key, String[] values) throws Exception{
 	if(! properties.containsKey(key))
 	    throw new Exception("Could not find " + key + " in configration file " + CONFIG_FILE);
 	if(values.length == 0){
 	    properties.setProperty(key,"");
 	}else{
 	    StringBuilder out = new StringBuilder();	
 	    out.append(values[0]);
 	    for(int i=1; i<values.length; ++i)
 		out.append("; ").append(values[i]);
 	    properties.setProperty(key,out.toString());
 	}
     }
 	
     
     private void _updateProperty(Properties properties, String key, String value) throws Exception{
 	if(! properties.containsKey(key))
 	    throw new Exception("Could not find " + key + " in configration file " + CONFIG_FILE);
 	properties.setProperty(key,value);
     }
     private String _loadProperty(Properties properties, String key) throws Exception {
 	String value = properties.getProperty(key);
 	if(value == null)
 	    throw new Exception("Could not find " + key + " in configration file " + CONFIG_FILE);
 	return value;
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
     /** Checks that a property has value YES or NO and returns true if yes. It throws an exception
      * if it neither yes nor no
      */
     private boolean _loadBooleanProperty(Properties properties, String key) throws Exception {
 	String value = properties.getProperty(key);
 	if(value == null)
 	    throw new Exception("Could not find " + key + " in configration file " + CONFIG_FILE);
 
 	if(value.equalsIgnoreCase("YES") || value.equalsIgnoreCase("TRUE")){
 	    return true;
 	}else if(value.equalsIgnoreCase("NO") || value.equalsIgnoreCase("FALSE")){
 	    return false;
 	}else{
 	    throw new Exception("Key " + key + " has value = " + value + " which does not match yes/no/true/false");
 	}
     }
 	private void _updateBooleanProperty(Properties properties, String key, boolean value) throws Exception{
 		if(! properties.containsKey(key))
 			throw new Exception("Could not find " + key + " in configration file " + CONFIG_FILE);
 		if(value){
 			properties.setProperty(key,"YES");
 		}else{
 			properties.setProperty(key,"NO");
 		}
 	} 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Handles display of exception messages which require acknowledgement from user */
     private void _showExceptionDialog(Exception e) {
 	JOptionPane.showMessageDialog(this, "" + e, e.getMessage(), JOptionPane.ERROR_MESSAGE);
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Member class for checking whether setup has changed. This is needed to see whether
      *  the user should be prompted to confirm the target name after a format change */
     private class CheckFormat extends Settings {
 
 	private String target;
 	private String grating;
 	private String filters;
 	private String slitWidth;
 	private String slitAngle;
 
 	// Constructor, stores current values, with no attempt at
 	// checking validity.
 
 	public CheckFormat() {
 
 	    super();
 
 	    target    = _objectText.getText();
 	    grating   = _gratingText.getText();
 	    filters   = _filter.getSelectedItem()+"";
 	    slitWidth = _slitWidthText.getText();
 	    slitAngle = _slitAngleText.getText();
 
 	}
 
 	// Check whether there has been a change of format without the
 	// target name changing or if the target name is blank
 	public boolean hasChanged() {
 
 	    if(!target.equals(_objectText.getText())){
 		update();
 		return false;
 	    }
 	    if(!grating.equals(_gratingText.getText())) return true;
 	    if(!filters.equals(_filter.getSelectedItem()+"")) return true;
 	    if(!slitWidth.equals(_slitWidthText.getText())) return true;
 	    if(!slitAngle.equals(_slitAngleText.getText())) return true;
 
 	    return super.hasChanged();
 	}
 
 	// Check whether the HV gain is set
 	public boolean gainOn() {
 	    if(getCurrentCcdOutput().equals("Normal") || getCurrentHvGain() == 0) return false;
 	    return true;
 	}
 
 	// Updates the stored format
 	public void update() {
 	    super.update();
 
 	    target    = _objectText.getText();
 	    grating   = _gratingText.getText();
 	    filters   = _filter.getSelectedItem()+"";
 	    slitWidth = _slitWidthText.getText();
 	    slitAngle = _slitAngleText.getText();
 	}
 
     }    
 
     /** Member class for storage of the settings that can make a difference
      * to the data **/
     private class Settings {
 
 	private String ccdOutput;
 	private int    hvGain;
 	private boolean clrEnabled;
 	private int    led;
 	private int    xbin;
 	private int    ybin;
 	private int    expose;
 	private int    numEnable;
 	private String readSpeed;
 	private int    nTemplate;
 
 	// SL - edited here to allow greater than 2 windows (18/7/2012)
 	private int[]  xstart = new int [4];
 	private int[]  ystart = new int [4];
 	private int[]  nx     = new int [4];
 	private int[]  ny     = new int [4];
 
 	// added drift mode
 	private int     dxleft;
 	private int     dxright;
 	private int     dystart;
 	private int     dnx;
 	private int     dny;
 	
 	// Constructor, stores current values, with no attempt at
 	// checking validity.
 
 	public Settings() {
 	    update();
 	}
 
 	// Check whether there has been a change of format without the
 	// target name changing or if the target name is blank
 	public boolean hasChanged() {
 	    
 	    boolean isDriftMode = driftModeEnabled.isSelected();
 	    if(!ccdOutput.equals(getCurrentCcdOutput())) return true;
 	    if(ccdOutput.equals("Avalanche") && hvGain    != getCurrentHvGain()) return true;
 	    // don't care about clear setting if drift mode
 	    if(clrEnabled!= getCurrentClrEnabled() && !isDriftMode) return true;
 	    if(led       != getCurrentLed()) return true;
 	    if(xbin      != getCurrentXbin())          return true;
 	    if(ybin      != getCurrentYbin())          return true;
 	    if(expose    != getCurrentExpose())        return true;
 	    if(isDriftMode){
 		if(dxleft  != getCurrentDxleft()) return true;
 		if(dxright != getCurrentDxright()) return true;
 		if(dystart != getCurrentDystart()) return true;
 		if(dnx     != getCurrentDnx()) return true;
 		if(dny     != getCurrentDny()) return true;
 	    }else{
 		if(numEnable != getCurrentNumEnable())     return true;
 		for(int i=0; i<numEnable; i++){
 		    if(xstart[i] != getCurrentXstart(i))    return true;
 		    if(ystart[i] != getCurrentYstart(i))   return true;
 		    if(nx[i]     != getCurrentNx(i))       return true;
 		    if(ny[i]     != getCurrentNy(i))       return true;
 		}
 	    }
 	    if(nTemplate != _whichTemplate()) return true;
 	    if(!readSpeed.equals(getCurrentReadSpeed())) return true;
 
 	    // All tests passed, there has been no change
 	    return false;
 	}
 
 	// Updates the stored format
 	public void update() {
 
 	    // Get current values
 	    ccdOutput = getCurrentCcdOutput();
 	    hvGain    = getCurrentHvGain();
 	    clrEnabled= getCurrentClrEnabled();
 	    led       = getCurrentLed();
 	    xbin      = getCurrentXbin();
 	    ybin      = getCurrentYbin();
 	    expose    = getCurrentExpose();
 	    numEnable = getCurrentNumEnable();
 	    for(int i=0; i<numEnable; i++){
 		xstart[i] = getCurrentXstart(i);
 		ystart[i] = getCurrentYstart(i);
 		nx[i]     = getCurrentNx(i);
 		ny[i]     = getCurrentNy(i);
 	    }
 	    nTemplate  = _whichTemplate();
 	    readSpeed  = getCurrentReadSpeed();
 	    // drift mode
 	    dxleft  = getCurrentDxleft();
 	    dxright = getCurrentDxright();
 	    dystart = getCurrentDystart();
 	    dnx     = getCurrentDnx();
 	    dny     = getCurrentDny();
 	    
 	}
 
 	// Series of routines for getting current settings
 	public String getCurrentCcdOutput(){
 	    return (String)ccdOutputChoice.getSelectedItem();
 	}
 
 	public String getCurrentReadSpeed(){
 	    return (String)speedChoice.getSelectedItem();
 	}
 
 	public int getCurrentHvGain(){
 	    try{
 		return hvGainText.getValue();
 	    } 
 	    catch(Exception e){
 		return 0;
 	    }
 	}
 
 	public boolean getCurrentClrEnabled(){
 	    return clearEnabled.isSelected();
 	}
 
 	public int getCurrentLed(){
 	    try{
 		return ledIntensityText.getValue();
 	    } 
 	    catch(Exception e){
 		return 0;
 	    }
 	}
 
 	public int getCurrentXbin(){
 	    try{
 		return xbinText.getValue();
 	    } 
 	    catch(Exception e){
 		return 1;
 	    }
 	}
 
 	public int getCurrentYbin(){
 	    try{
 		return ybinText.getValue();
 	    } 
 	    catch(Exception e){
 		return 1;
 	    }
 	}
 
 	public int getCurrentExpose(){
 	    try{
 		return _getExpose();
 	    } 
 	    catch(Exception e){
		return 5;
 	    }
 	}
 
 	public int getCurrentNumEnable(){
 	    try{
 		return Integer.parseInt(TEMPLATE_PAIR[_whichTemplate()]);
 	    }
 	    catch(Exception e){
 		return 1;
 	    }
 	}
 
 	public int getCurrentXstart(int nwin){
 	    try{
 		return _singleWindows.getXstart(nwin);
 	    }
 	    catch(Exception e){
 		return 1;
 	    }
 	}
 
 	public int getCurrentYstart(int nwin){
 	    try{
 		return _singleWindows.getYstart(nwin);
 	    }
 	    catch(Exception e){
 		return 1;
 	    }
 	}
 
 	public int getCurrentNx(int nwin){
 	    try{
 		return _singleWindows.getNx(nwin);
 	    }
 	    catch(Exception e){
 		return 1;
 	    }
 	}
 
 	public int getCurrentNy(int nwin){
 	    try{
 		return _singleWindows.getNy(nwin);
 	    }
 	    catch(Exception e){
 		return 1;
 	    }
 	}
 
 	public int getCurrentDxleft(){
 	    try{
 		return _windowPairs.getXleft(0);
 	    }catch(Exception e){
 		return 1;
 	    }
 	}
 
 	public int getCurrentDxright(){
 	    try{
 		return _windowPairs.getXright(0);
 	    }catch(Exception e){
 		return 1;
 	    }
 	}
 
 	public int getCurrentDystart(){
 	    try{
 		return _windowPairs.getYstart(0);
 	    }catch(Exception e){
 		return 1;
 	    }
 	}
 
 	public int getCurrentDnx(){
 	    try{
 		return _windowPairs.getNx(0);
 	    }catch(Exception e){
 		return 1;
 	    }
 	}
 
 	public int getCurrentDny(){
 	    try{
 		return _windowPairs.getNy(0);
 	    }catch(Exception e){
 		return 1;
 	    }
 	}
 
     }    
 
     //------------------------------------------------------------------------------------------------------------------------------------------
     //
     // Disable all settings buttons. This is in order to prevent the user changing a setup that has not been saved
 
     private void _disableAll() {
 	loadApp.setEnabled(false);
 	_singleWindows.setNwin(0);
 	templateChoice.setEnabled(false);
 	speedChoice.setEnabled(false);
 	exposeText.setEnabled(false);
 	tinyExposeText.setEnabled(false);
 	numExposeText.setEnabled(false);
 	xbinText.setEnabled(false);
 	ybinText.setEnabled(false);
 	if(OBSERVING_MODE){
 	    _objectText.setEnabled(false);
 	    _gratingText.setEnabled(false);
 	    _filter.setEnabled(false);
 	    _slitWidthText.setEnabled(false);
 	    _slitAngleText.setEnabled(false);
 	    _progidText.setEnabled(false);
 	    _piText.setEnabled(false);
 	    _observerText.setEnabled(false);
 	    //	    _commentText.setEnabled(false);
 	}
 	logPanel.add("The window settings now disabled. You must save them to disk before you can change them.", LogPanel.WARNING, false);
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
     //
     // Enable all settings buttons.
 
     private void _enableAll() {
 	enableChanges.setEnabled(EXPERT_MODE || _unsavedSettings);
 	loadApp.setEnabled(true);
 	_singleWindows.setNwin(numEnable);
 	templateChoice.setEnabled(true);
 	speedChoice.setEnabled(true);
 	exposeText.setEnabled(true);
 	tinyExposeText.setEnabled(EXPERT_MODE);
 	numExposeText.setEnabled(true);
 	xbinText.setEnabled(true);
 	ybinText.setEnabled(true);
 	if(OBSERVING_MODE){
 	    _objectText.setEnabled(true);
 	    _filter.setEnabled(true);
 	    if(! _telescope.name.equals("TNO")){
 		    _gratingText.setEnabled(true);
 		    _slitWidthText.setEnabled(true);
 		    _slitAngleText.setEnabled(true);
 	    }
 	    _progidText.setEnabled(true);
 	    _piText.setEnabled(true);
 	    _observerText.setEnabled(true);
 	    //	    _commentText.setEnabled(true);
 	}
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
     //
     // Disable drift mode. Switch GUI back to setup for normal mode
 
     private void _disableDriftMode() {	
 
 	applicationTemplate = "Window mode";
 	_singleWindows.setVisible(true);
 	_windowPairs.setVisible(false);
 	xstartLabel.setVisible(true);
 	ystartLabel.setVisible(true);
 	nxLabel.setVisible(true);
 	nyLabel.setVisible(true);
 	xleftLabel.setVisible(false);
 	xrightLabel.setVisible(false);
 	ystartLabel2.setVisible(false);
 	nxLabel2.setVisible(false);
 	nyLabel2.setVisible(false);
 	numWinText.setEnabled(true);
 	// restore previous clear settings
 	clearEnabled.setEnabled(true);
 	clearEnabled.setSelected(_wasClearEnabled);
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
     //
     // Enable drift mode. Switch GUI to setup for drift mode
 
     private void _enableDriftMode() {
 	applicationTemplate = "Drift mode";
 
 	// remember whether clear was enabled before we switched
 	_wasClearEnabled = clearEnabled.isSelected();
 	// disable clear mode
 	clearEnabled.setSelected(false);
 	// and prevent user from fiddling!
 	clearEnabled.setEnabled(false);
 	// disable numWindows
 	numWinText.setEnabled(false);
 
 	_singleWindows.setVisible(false);
 	_windowPairs.setVisible(true);
 	xstartLabel.setVisible(false);
 	ystartLabel.setVisible(false);
 	nxLabel.setVisible(false);
 	nyLabel.setVisible(false);
 	xleftLabel.setVisible(true);
 	xrightLabel.setVisible(true);
 	ystartLabel2.setVisible(true);
 	nxLabel2.setVisible(true);
 	nyLabel2.setVisible(true);
 
     }
 
     // Modifies window locations so that a full frame NxM binned window can
     // be used as a bias by ensuring the correct registration of binned windows
     // Also makes all X starts the same, if telescope is other than TNO
     private boolean _syncWindows() {
 	if(isValid(true)){
 	    try {
 		if(driftModeEnabled.isSelected()){
 		    // 544 and 514 are based on the start pixel of 33,3 (+512) given by Derek, modified to the new output-independent
 		    // coords, 544 becomes 528 15/06/09
 		    // y value of 3 is because bottom two rows are overscan
 		    // x position is synced to middle of the chip, since this drops columns symmetrically from L and R
 		    // y position is synced to row 3, because it can be important to have windows start at chip bottom
 		    _windowPairs.setXleftText( 0,Integer.toString(_syncStart(_windowPairs.getXleft(0),  xbin, 1, 1056, 528)) );
 		    _windowPairs.setXrightText(0,Integer.toString(_syncStart(_windowPairs.getXright(0), xbin, 1, 1056, 528)) );
 		    _windowPairs.setYstartText(0,Integer.toString(_syncStart(_windowPairs.getYstart(0), ybin, 1, 1072, 2)) );
 		}else{
 		    // 544 and 514 are based on the start pixel of 33,3 (+512) given by Derek, modified to the new output-independent
 		    // coords, 544 becomes 528 15/06/09
 		    for(int i=0; i<numEnable; i++){
 			_singleWindows.setXstartText(i, Integer.toString(_syncStart(_singleWindows.getXstart(i), xbin, 1,  1056, 528)) );
 			_singleWindows.setYstartText(i, Integer.toString(_syncStart(_singleWindows.getYstart(i), ybin, 1,  1072, 2)) );
 		    }
 		    // lines up all windows - have disabled this at Thai 2.4m - SL (29/8/2012)
 		    if(!_telescope.name.equals("TNO")){
 			for(int i=1; i<numEnable; i++){
 			    _singleWindows.setXstartText(i, Integer.toString(_singleWindows.getXstart(0)));
 			    _singleWindows.setNxText(i, Integer.toString(_singleWindows.getNx(0)));
 			}
 		    }
 		}
 		return true;
 	    }
 	    catch(Exception e){
 		logPanel.add(e.toString(), LogPanel.ERROR, false);
 		return false;
 	    }
 	}
 	return true;
     }
 
     // Synchronises window so that the binned pixels end at ref and start at ref+1
     private int _syncStart(int start, int bin, int min, int max, int ref){
 		int n = Math.round((float)((ref+1-start))/bin);
 		start = ref + 1 - bin*n;
 		if(start < min) start += bin;
 		if(start > max) start -= bin;
 	return start;
     }
 
 
     // Send a command to slide and return response
     
     private String sendToSlide(final String cmd) throws Exception{                
         // send slide command on it's own process
 	String stdout=null;
 	String stderr=null;
 	String line=null;
 
 	Process p = Runtime.getRuntime().exec("/home/observer/slide/slide "+cmd);
 	BufferedReader bri = new BufferedReader
 	    (new InputStreamReader(p.getInputStream()));
 	BufferedReader bre = new BufferedReader
 	    (new InputStreamReader(p.getErrorStream()));
 	while ((line = bri.readLine()) != null) {
 	    if(stdout == null){
 		stdout = line;
 	    }else{
 		stdout += ("\n"+line) ;
 	    }
 	}
 	bri.close();
 	while ((line = bre.readLine()) != null) {
 	    if(stderr == null){
 		stderr = line;
 	    }else{
 		stderr += ("\n"+line);
 	    }
 	}
 	bre.close();
 	p.waitFor();
 	if(stderr != null)
 	    throw new Exception(stderr);
         return stdout;
     }
  
 
     // Checks whether windows are synchronised
     private boolean _areSynchronised(){
 
 	if(isValid(false)){
 	    try{ 
 		if(driftModeEnabled.isSelected()){
 		    // Numbers here come from the 33,3 start pixel from Derek + 512, modified to output independent coords
 		    // 15/06/09
 		    if((529 - _windowPairs.getXleft(0))  % xbin != 0) return false;
 		    if((529 - _windowPairs.getXright(0)) % xbin != 0) return false;
 		    if((3 - _windowPairs.getYstart(0)) % ybin != 0) return false;
 		}else{
 		    // Numbers here come from the 33,3 start pixel from Derek + 512, modified to output independent coords
 		    // 15/06/09
 		    for(int i=0; i<numEnable; i++){
 			if((529 - _singleWindows.getXstart(i)) % xbin != 0) return false;
 			if((3 - _singleWindows.getYstart(i)) % ybin != 0) return false;
 		    }
 		    // returns false if all windows are not at same xstart and Nx - disabled for TNO - SL (29/8/2012)
 		    if(!_telescope.name.equals("TNO")){
 			for(int i=1; i<numEnable; i++){
 			    if(_singleWindows.getXstart(i) != _singleWindows.getXstart(0)) return false;
 			    if(_singleWindows.getNx(i)     != _singleWindows.getNx(0))     return false;
 			}
 		    }
 		}
 		return true;
 	    }
 	    catch(Exception e){
 		logPanel.add(e.toString(), LogPanel.ERROR, false);
 		return false;
 	    }
 	}
 	return true;
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** This class is for the display of the detailed timing information in 'speed' */
     class TableModel extends AbstractTableModel {
 	
 	private Object[][] data;
 
 	public TableModel(Object[][] data){
 	    this.data = data;
 	}
 			
 	public int getColumnCount() {
 	    return data[0].length;
 	}
 		    
 	public int getRowCount() {
 	    return data.length;
 	}
 
 	public Object getValueAt(int row, int col) {
 	    return data[row][col];
 	}
 
     }
 
     // Converts a double to a string rounding to specified number of decimals
     public String round(double f, int ndp){
 	final DecimalFormat form = new DecimalFormat();
 	form.setMaximumFractionDigits(ndp);
 	return form.format(f);
     }
 
     //--------------------------------------------------------------------------------------------------------------
     // Save the configuration file
     public void saveConfig() throws Exception {
 	System.out.println(CONFIG_FILE);
     	properties.save(new FileOutputStream(CONFIG_FILE),"");
     }
     //--------------------------------------------------------------------------------------------------------------
     // Load the configuration file
     public void loadConfig() throws Exception {
 
 	properties = new CommentedProperties();
 	properties.load(new FileInputStream(CONFIG_FILE));
 
 	RTPLOT_SERVER_ON     = _loadBooleanProperty(properties, "RTPLOT_SERVER_ON");
 	FILE_LOGGING_ON      = _loadBooleanProperty(properties, "FILE_LOGGING_ON");
 	ULTRACAM_SERVERS_ON  = _loadBooleanProperty(properties, "ULTRACAM_SERVERS_ON");
 	OBSERVING_MODE       = _loadBooleanProperty(properties, "OBSERVING_MODE");
 	DEBUG                = _loadBooleanProperty(properties, "DEBUG");
 	TELESCOPE            = _loadProperty(properties,        "TELESCOPE");
 	UAC_DATABASE_HOST	 = _loadProperty(properties, "UAC_DATABASE_HOST");
 
 	// Set the current telescope 
 	for(int i=0; i<TELESCOPE_DATA.length; i++){
 	    if(TELESCOPE_DATA[i].name.equals(TELESCOPE)){
 		_telescope = TELESCOPE_DATA[i];
 		break;
 	    }
 	}
 
 	if(_telescope == null){
 	    String MESSAGE = "TELESCOPE = " + TELESCOPE + " was not found amongst the list of supported telescopes:\n";
 	    for(int i=0; i<TELESCOPE_DATA.length-1; i++)
 		MESSAGE += TELESCOPE_DATA[i].name + ", ";
 	    MESSAGE += TELESCOPE_DATA[TELESCOPE_DATA.length-1].name;
 	    throw new Exception(MESSAGE);
 	}
 
 	HTTP_CAMERA_SERVER   = _loadProperty(properties, "HTTP_CAMERA_SERVER");
 	if(!HTTP_CAMERA_SERVER.trim().endsWith("/"))
 	    HTTP_CAMERA_SERVER = HTTP_CAMERA_SERVER.trim() + "/";
 	
 	HTTP_DATA_SERVER    = _loadProperty(properties, "HTTP_DATA_SERVER");
 	if(!HTTP_DATA_SERVER.trim().endsWith("/"))
 	    HTTP_DATA_SERVER = HTTP_DATA_SERVER.trim() + "/";
 	
 	HTTP_PATH_GET         = _loadProperty(properties,        "HTTP_PATH_GET");
 	HTTP_PATH_EXEC        = _loadProperty(properties,        "HTTP_PATH_EXEC");
 	HTTP_PATH_CONFIG      = _loadProperty(properties,        "HTTP_PATH_CONFIG");
 	HTTP_SEARCH_ATTR_NAME = _loadProperty(properties,        "HTTP_SEARCH_ATTR_NAME");
 	APP_DIRECTORY         = _loadProperty(properties,        "APP_DIRECTORY");
 	XML_TREE_VIEW         = _loadBooleanProperty(properties, "XML_TREE_VIEW");
 	
 	TEMPLATE_FROM_SERVER  = OBSERVING_MODE && _loadBooleanProperty(properties, "TEMPLATE_FROM_SERVER");
 	String dsep = System.getProperty("file.separator");
 	
 	FILTER_NAMES = _loadSplitProperty(properties, "FILTER_NAMES");
 	FILTER_IDS   = _loadSplitProperty(properties, "FILTER_IDS");
 	if(FILTER_IDS.length != FILTER_NAMES.length)
 		throw new Exception("Number of FILTER_NAMES = " + FILTER_NAMES.length + 
 			" does not equal the number of FILTER_IDS = " + FILTER_IDS.length);
 	ACTIVE_FILTER_NAMES = _loadSplitProperty(properties, "ACTIVE_FILTER_NAMES");
 	if(ACTIVE_FILTER_NAMES.length < 6){
 		ACTIVE_FILTER_NAMES = Arrays.copyOf(ACTIVE_FILTER_NAMES,6);
 		for(int i=ACTIVE_FILTER_NAMES.length-1; i<6; i++){
 			ACTIVE_FILTER_NAMES[i] = "blank";
 		}
 		_updateSplitProperty(properties,"ACTIVE_FILTER_NAMES",ACTIVE_FILTER_NAMES);
 	}
 	defaultFilter = ACTIVE_FILTER_NAMES[0];
 		
 	TEMPLATE_DIRECTORY   = _loadProperty(properties, "TEMPLATE_DIRECTORY");
 	if(!TEMPLATE_DIRECTORY.trim().endsWith(dsep))
 	    TEMPLATE_DIRECTORY = TEMPLATE_DIRECTORY.trim() + dsep;
 	
 	EXPERT_MODE        = _loadBooleanProperty(properties, "EXPERT_MODE");
 	LOG_FILE_DIRECTORY = _loadProperty(properties, "LOG_FILE_DIRECTORY");
 	CONFIRM_ON_CHANGE  =  OBSERVING_MODE && _loadBooleanProperty(properties, "CONFIRM_ON_CHANGE");
 	CONFIRM_HV_GAIN_ON =  OBSERVING_MODE && _loadBooleanProperty(properties, "CONFIRM_HV_GAIN_ON");
 
 	TEMPLATE_LABEL     = _loadSplitProperty(properties, "TEMPLATE_LABEL");
 	
 	TEMPLATE_PAIR      = _loadSplitProperty(properties, "TEMPLATE_PAIR");
 	if(TEMPLATE_PAIR.length != TEMPLATE_LABEL.length)
 	    throw new Exception("Number of TEMPLATE_PAIR = " + TEMPLATE_PAIR.length + 
 				" does not equal the number of TEMPLATE_LABEL = " + TEMPLATE_LABEL.length);
 	
 	TEMPLATE_APP       = _loadSplitProperty(properties, "TEMPLATE_APP");
 	if(TEMPLATE_APP.length != TEMPLATE_LABEL.length)
 	    throw new Exception("Number of TEMPLATE_APP = " + TEMPLATE_APP.length + 
 				" does not equal the number of TEMPLATE_LABEL = " + TEMPLATE_LABEL.length);
 	
 	TEMPLATE_ID        = _loadSplitProperty(properties, "TEMPLATE_ID");
 	if(TEMPLATE_ID.length != TEMPLATE_LABEL.length)
 	    throw new Exception("Number of TEMPLATE_ID = " + TEMPLATE_ID.length + 
 				" does not equal the number of TEMPLATE_LABEL = " + TEMPLATE_LABEL.length);
 	
 	POWER_ON  = _loadProperty(properties, "POWER_ON");
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     // Create the "Filter" menu
     private JMenu createFilterMenu(){
 	JMenu filtMenu = new JMenu("Filters");
 	// Allow user to edit active filters
 	JMenuItem _filterEdit = new JMenuItem("Edit filters");
 	_filterEdit.addActionListener(
 				      // here we update the filters in the wheel, so update choices in the 
 				      // drop-down box, and update the values in the config file so the new
 				      // filters are saved for next load
 
 				      new ActionListener(){
 					  public void actionPerformed(ActionEvent e){
 					      FilterChooser fc = null;
 					      try{
 						  fc = new FilterChooser(FILTER_NAMES, FILTER_IDS, ACTIVE_FILTER_NAMES);
 					      }catch(Exception ex1){
 						  _showExceptionDialog(ex1);
 					      }
 					      int result = JOptionPane.showConfirmDialog(null, 
 											 fc.getComponent(),
 											 "Filters",
 											 JOptionPane.OK_CANCEL_OPTION,
 											 JOptionPane.PLAIN_MESSAGE);
 					      // Unless user cancelled, update current filters
 					      if(result == JOptionPane.OK_OPTION){
 						  String[] newFilters = fc.getFilts();
 						  // check length is OK
 						  if(newFilters.length == ACTIVE_FILTER_NAMES.length){
 						      // empty combo box
 						      _filter.removeAllItems();						      
 						      for(int i=0; i<newFilters.length; i++){
 							  // add in new filter
 							  _filter.addItem(newFilters[i]);
 							  ACTIVE_FILTER_NAMES[i] = newFilters[i];
 						      }
 						      try{
 							  _updateSplitProperty(properties,"ACTIVE_FILTER_NAMES",ACTIVE_FILTER_NAMES);
 							  saveConfig();
 						      }catch(Exception ex){
 							  //System.out.println(ex.toString());
 							  _showExceptionDialog(new Exception("Couldn't save updated filters to config file")); 
 						      }
 						  }else{
 						      _showExceptionDialog(new Exception("Filter name array has wrong length"));
 						  }
 					      }
 
 					  }
 				      }
 				      );
 
 	JMenuItem _filterCtrl = new JMenuItem("FW Control Box");
 	_filterCtrl.addActionListener(new ActionListener(){
 					  public void actionPerformed(ActionEvent e){
 					      if(FILTER_WHEEL_ON){
 						  // destroy any previous version
 						  wheelControl = null;
 						  // spawn new version
 						  wheelControl = new WheelController(ACTIVE_FILTER_NAMES, wheel);
 					      }else{
 						  _showExceptionDialog(new Exception("Filter wheel is not enabled; enable in settings"));
 					      }
 					  }
 				      }
 				      );
 	
 	filtMenu.add(_filterEdit);
 	filtMenu.add(_filterCtrl);
 	return filtMenu;
     }
 
     // Create the "File" menu
     private JMenu createFileMenu() {
 	
 	JMenu fileMenu = new JMenu("File");
 	
 	// Add actions to the "File" menu
 	
 	// Allow user to save a standard rtplot windows file
 	JMenuItem _rtplotSave = new JMenuItem("Save rtplot file");
 	_rtplotSave.addActionListener(
 				      new ActionListener(){
 					  public void actionPerformed(ActionEvent e){
 					      int result = _rtplotFileChooser.showSaveDialog(null);
 					      if(result == JFileChooser.APPROVE_OPTION){
 						  _rtplotFile = _rtplotFileChooser.getSelectedFile();
 						  if (_rtplotFile.getPath().indexOf(".dat") != _rtplotFile.getPath().length() - 4 ){
 						      String newFilePath = _rtplotFile.getPath() + ".dat";
 						      _rtplotFile = new File(newFilePath);
 						  }
 						  saveToRtplot();
 					      }else{
 						  System.out.println("No rtplot file chosen.");
 					      }
 					  }
 				      });
 	
 	// Quit the program
 	JMenuItem _quit = new JMenuItem("Quit");
 	_quit.addActionListener(
 				new ActionListener(){
 				    public void actionPerformed(ActionEvent e){
 					if(logPanel.loggingEnabled())
 					    logPanel.stopLog();
 					try{
 					    if(wheel.isConnected())wheel.close();
 					    String path = System.getProperty("user.home");
 					    Document document = _createXML(false);
 					    FileWriter fwriter = new FileWriter(path + "/.usdriver.xml");
 					    _transformer.transform(new DOMSource(document),new StreamResult(fwriter));
 					} catch (Exception ex) {System.out.println(ex);System.exit(0);}
 					System.exit(0);
 				    }
 				});
 	
 	fileMenu.add(_rtplotSave);
 	fileMenu.add(_quit);
 	return fileMenu;
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     // Create the "Settings" menu
     private JMenu createSettingsMenu() throws Exception {
 	
 	JMenu settingsMenu = new JMenu("Settings");
 	
 	if(OBSERVING_MODE){
 	    
 	    _setExpert = new JCheckBoxMenuItem("Expert mode");
 	    _setExpert.setState(EXPERT_MODE);
 	    _setExpert.addActionListener(
 		new ActionListener(){
 		    public void actionPerformed(ActionEvent e){
 			EXPERT_MODE = _setExpert.getState();
 			if(OBSERVING_MODE){
 			    int selIndex = _actionPanel.getSelectedIndex();
 			    _actionPanel.removeTabAt(0);
 			    if(EXPERT_MODE)							     
 				_actionPanel.insertTab("Setup", null, _expertSetupPanel, null, 0);
 			    else
 				_actionPanel.insertTab("Setup", null, _noddySetupPanel, null, 0);
 			    _actionPanel.setSelectedIndex(selIndex);
 			}
 			if(EXPERT_MODE){
 			    _enableAll();
 			}else{
 			    if(_unsavedSettings) _disableAll();
 			    try{
				tinyExposeText.setText("7");
 			    }catch(Exception er){
				tinyExposeText.setText("7");
 			    }
 			}
 			_setEnabledActions();
 			updateGUI();
 		    }
 		});
 	    
 	    _templatesFromServer = new JCheckBoxMenuItem("Templates from server");
 	    _templatesFromServer.setState(TEMPLATE_FROM_SERVER);
 	    _templatesFromServer.addActionListener(
 		new ActionListener(){
 		    public void actionPerformed(ActionEvent e){
 			Usdriver.TEMPLATE_FROM_SERVER = _templatesFromServer.getState();
 		    }
 		});
 	    
 	    _ucamServersOn = new JCheckBoxMenuItem("ULTRACAM servers on");
 	    _ucamServersOn.setState(ULTRACAM_SERVERS_ON);
 	    _ucamServersOn.addActionListener(
 		new ActionListener(){
 		    public void actionPerformed(ActionEvent e){
 			Usdriver.ULTRACAM_SERVERS_ON = _ucamServersOn.getState();
 			if(Usdriver.ULTRACAM_SERVERS_ON)
 			    getRunNumber();
 			_setEnabledActions();
 		    }
 		});
 	    
 	    _fileLogging = new JCheckBoxMenuItem("File logging");
 	    _fileLogging.setState(logPanel.loggingEnabled());
 	    _fileLogging.addActionListener(
 		new ActionListener(){
 		    public void actionPerformed(ActionEvent e){
 			if(_fileLogging.getState())
 			    logPanel.startLog();
 			else
 			    logPanel.stopLog();
 		    }
 		});
 	    
 	    _responseAsText = new JCheckBoxMenuItem("Show responses as text");
 	    _responseAsText.setState(true);
 	    _responseAsText.addActionListener(
 		new ActionListener(){
 		    public void actionPerformed(ActionEvent e){
 			_replyPanel.setTreeView(!_responseAsText.getState());
 		    }
 		});
 	    
 	    _confirmOnChange = new JCheckBoxMenuItem("Confirm target name");
 	    _confirmOnChange.setState(CONFIRM_ON_CHANGE);
 	    _confirmOnChange.addActionListener(
 		new ActionListener(){
 		    public void actionPerformed(ActionEvent e){
 			Usdriver.CONFIRM_ON_CHANGE = _confirmOnChange.getState();
 		    }
 		});
 
 		_tempFromLakeshore = new JCheckBoxMenuItem("Lakeshore temp monitor on");
 	    _tempFromLakeshore.setState(TEMP_FROM_LAKESHORE);
 	    _tempFromLakeshore.addActionListener(
 		new ActionListener(){
 		    public void actionPerformed(ActionEvent e){
 			Usdriver.TEMP_FROM_LAKESHORE = _tempFromLakeshore.getState();
 		    }
 		});
 		
 	    _confirmHvGainOn = new JCheckBoxMenuItem("Confirm HV gain on");
 	    _confirmHvGainOn.setState(CONFIRM_HV_GAIN_ON);
 	    _confirmHvGainOn.addActionListener(
 		new ActionListener(){
 		    public void actionPerformed(ActionEvent e){
 			Usdriver.CONFIRM_HV_GAIN_ON = _confirmHvGainOn.getState();
 		    }
 		});
 	    
 	    _filterWheelOn = new JCheckBoxMenuItem("Filter Wheel on");
 	    _filterWheelOn.setState(FILTER_WHEEL_ON);
 	    _filterWheelOn.addActionListener(
 					     new ActionListener(){
 						 public void actionPerformed(ActionEvent e){
 						     Usdriver.FILTER_WHEEL_ON = _filterWheelOn.getState();
 						 }
 					     }
 					     );
 
 	    _slideOn = new JCheckBoxMenuItem("Focal Plane Slide on");
 	    _slideOn.setState(SLIDE_ON);
 	    _slideOn.addActionListener(
 				       new ActionListener(){
 					   public void actionPerformed(ActionEvent e){
 					       Usdriver.SLIDE_ON = _slideOn.getState();
 					       slideCon.setEnabled(Usdriver.SLIDE_ON);
 					   }
 				       }
 				       );
 
 	    _useUACdb = new JCheckBoxMenuItem("Use UAC db for lookup");
 	    _useUACdb.setState(USE_UAC_DB);
 	    _useUACdb.addActionListener(
 					new ActionListener() {
 					    public void actionPerformed(ActionEvent e){
 						Usdriver.USE_UAC_DB = _useUACdb.getState();
 					    }
 					});
 
 	    // Add actions to the "Settings" menu	
 	    settingsMenu.add(_setExpert);
 	    settingsMenu.add(_templatesFromServer);
 	    settingsMenu.add(_ucamServersOn);
 	    settingsMenu.add(_fileLogging);
 	    settingsMenu.add(_responseAsText);
 	    settingsMenu.add(_confirmOnChange);
 	    settingsMenu.add(_confirmHvGainOn);
 	    settingsMenu.add(_tempFromLakeshore);
 	    settingsMenu.add(_filterWheelOn);
 	    settingsMenu.add(_slideOn);
 	    settingsMenu.addSeparator();
 	    settingsMenu.add(_useUACdb);
 	    settingsMenu.addSeparator();
 	}
 	
 	// Telescope choices
 	JRadioButtonMenuItem[] telescopeMenuItem = new JRadioButtonMenuItem[TELESCOPE_DATA.length];
 	ButtonGroup telescopeGroup = new ButtonGroup();
 	for(int ntel=0; ntel<TELESCOPE_DATA.length; ntel++){
 	    telescopeMenuItem[ntel] = new JRadioButtonMenuItem(TELESCOPE_DATA[ntel].name);
 	    
 	    telescopeMenuItem[ntel].addActionListener(
 						      new ActionListener(){
 							  public void actionPerformed(ActionEvent e){
 							      TELESCOPE = ((JRadioButtonMenuItem)e.getSource()).getText();
 							      // Set the current telescope 
 							      for(int i=0; i<TELESCOPE_DATA.length; i++){
 								  if(TELESCOPE_DATA[i].name.equals(TELESCOPE)){
 								      _telescope = TELESCOPE_DATA[i];
 								      if (_telescope.name.equals("TNO")){
 									      _gratingText.setEnabled(false);
 									      _slitWidthText.setEnabled(false);
 									      _slitAngleText.setEnabled(false);
 								      }else{
 									      _gratingText.setEnabled(true);
 									      _slitWidthText.setEnabled(true);
 									      _slitAngleText.setEnabled(true);
 								      }
 								      break;
 								  }
 							      }
 							  }});
 	    telescopeGroup.add(telescopeMenuItem[ntel]);
 	    settingsMenu.add(telescopeMenuItem[ntel]);
 	}
 
 	// Select the current telescope 
 	for(int i=0; i<TELESCOPE_DATA.length; i++){
 	    if(TELESCOPE.equals("TNO")){
 		_gratingText.setEnabled(false);
 		_slitWidthText.setEnabled(false);
 		_slitAngleText.setEnabled(false);
 	    }
 	    if(TELESCOPE_DATA[i].name.equals(TELESCOPE)){
 		telescopeMenuItem[i].setSelected(true);
 		break;
 	    }
 	}
 	return settingsMenu;
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
     /** Creates the actions panel to send commands to the servers, save applications etc */
     public Component createActionsPanel(){
 
 	// Left-hand action panels
 	int ypos, xpos;
 	if(OBSERVING_MODE){
 
 	    // Now the actions panel 
 	    _actionPanel = new JTabbedPane();
 
 	    // Setup panel, two alternatives depending upon whether
 	    // we are in expert mode or not. The easy one first
 	    _noddySetupPanel = new JPanel(gbLayout);
 	    _noddySetupPanel.setLayout(gbLayout);
 	    
 	    // Top-left
 	    ypos = 0;
 	    xpos = 0;
 	    
 	    // Sets up everything, checking each command along the way
 	    setupAll.addActionListener(
 				       new ActionListener(){
 					   public void actionPerformed(ActionEvent e){
 					       boolean ok = true;
 					       
 					       if(ok && (ok = _execCommand("RCO", true)))
 						   onResetSDSU();
 					       
 					       if(ok && (ok = _execCommand("RST", false)))
 						   onResetPCI();
 					       
 					       if(ok && (ok = _setupServers(false)))
 						   onServersSet();
 					       
 					       if(ok && (ok = (_execRemoteApp(POWER_ON, false) && _execCommand("GO", false))))
 						   onPowerOn();
 					       
 					       if(!ok)
 						   logPanel.add("Combination ULTRASPEC setup failed; if any of the commands were reported to be successful, you may want to " + 
 								" switch to expert mode to try to continue with individual commands", LogPanel.WARNING, false);
 					       else
 						   logPanel.add("ULTRASPEC successfully setup.", LogPanel.OK, true);
 					   }
 				       });
 	    addActionComponent( _noddySetupPanel, setupAll, xpos, ypos++);
 	    
 	    // Top-right for the power off command.
 	    ypos = 0;
 	    xpos = 1;
 	    
 	    // Now the expert one
 	    _expertSetupPanel = new JPanel(gbLayout);
 	    _expertSetupPanel.setLayout(gbLayout);
 	    
 	    // Top-left
 	    ypos = 0;
 	    xpos = 0;
 	    
 	    // Reset controller
 	    resetSDSU.addActionListener(
 					new ActionListener(){
 					    public void actionPerformed(ActionEvent e){
 						if(_execCommand("RCO", true))
 						    onResetSDSU();
 					    }
 					});
 	    addActionComponent( _expertSetupPanel, resetSDSU, xpos, ypos++);
 	    
 	    // Reset PCI board
 	    resetPCI.addActionListener(
 				       new ActionListener(){
 					   public void actionPerformed(ActionEvent e){
 					       if(_execCommand("RST", true))
 						   onResetPCI();
 					   }
 				       });
 	    addActionComponent( _expertSetupPanel, resetPCI, xpos, ypos++);
 	    
 	    // Setup servers
 	    setupServer.addActionListener(
 					  new ActionListener(){
 					      public void actionPerformed(ActionEvent e) {
 						  if(_setupServers(true))
 						      onServersSet();
 					      }
 					  });
 	    addActionComponent( _expertSetupPanel, setupServer, xpos, ypos++);
 	    
 	    // Power on SDSU
 	    powerOn.addActionListener(
 				      new ActionListener(){
 					  public void actionPerformed(ActionEvent e) {
 					      if(_execRemoteApp(POWER_ON, true) && _execCommand("GO", false))
 						  onPowerOn();
 					  }
 				      });
 	    addActionComponent( _expertSetupPanel, powerOn, xpos, ypos++);
 
 	    // Expert command text
 	    addActionComponent(_expertSetupPanel,_commandText,xpos++,ypos);
 	    // Send command to server
 	    execExpertCmd.addActionListener(
 					    new ActionListener () {
 						public void actionPerformed(ActionEvent e){
 						    _execCommand(_commandText.getText(),true,true);
 						}
 					    });
 	    addActionComponent(_expertSetupPanel, execExpertCmd, xpos, ypos++);
 	    
 	    // Top-right 
 	    ypos = 0;
 	    xpos = 1;
 	    	    
 	    // Add in to the card layout
 	    if(EXPERT_MODE)
 		_actionPanel.insertTab("Setup", null, _expertSetupPanel, null, 0);
 	    else
 		_actionPanel.insertTab("Setup", null, _noddySetupPanel, null, 0);
 	    
 	}
 	
 	// Observing panel
 	JPanel _obsPanel = new JPanel(gbLayout);
 	
 	// Top-left
 	ypos = 0;
 	xpos = 0;
 	
 	// Load application to local disk file
 	loadApp.addActionListener(
 				  new ActionListener(){
 				      public void actionPerformed(ActionEvent e) {
 					  if(_chooseLoadApp())
 					      _loadApp();
 				      }
 				  });
 	addActionComponent( _obsPanel, loadApp, xpos, ypos++);
 	
 	// Save application to local disk file
 	JButton saveApp = new JButton("Save application");
 	saveApp.addActionListener(
 				  new ActionListener(){
 				      public void actionPerformed(ActionEvent e){
 					  if(_chooseSaveApp())
 					      _saveApp();
 				      }
 				  });
 	addActionComponent( _obsPanel, saveApp, xpos, ypos++);
 	
 	// By-pass the enforced save
 	enableChanges.setEnabled(EXPERT_MODE || _unsavedSettings);
 	enableChanges.addActionListener(
 					new ActionListener(){
 					    public void actionPerformed(ActionEvent e) {
 						_unsavedSettings = false;
 						_enableAll();
 					    }
 					});
 	addActionComponent( _obsPanel, enableChanges, xpos, ypos++);
 
 	// Ensure that binned windows match a standard phasing (designed so that there are no gaps
 	// in the middle of the chip
 	syncWindows.setEnabled(false);
 	syncWindows.addActionListener(
 				      new ActionListener(){
 					  public void actionPerformed(ActionEvent e) {
 					      if(_syncWindows()){
 						  syncWindows.setEnabled(false);						  
 						  syncWindows.setBackground(DEFAULT_COLOUR);
 					      }
 					  }
 				      });
 	addActionComponent( _obsPanel, syncWindows, xpos, ypos++);
 
 	
 	if(OBSERVING_MODE){
 	    
 	    // Top-right
 	    ypos = 0;
 	    xpos = 1;
 	    
 	    // Post application to the servers
 	    postApp.addActionListener(
 		new ActionListener(){
 		    public void actionPerformed(ActionEvent e){
 			
 			if(CONFIRM_ON_CHANGE && (
 			       _objectText.getText().equals("")  || _gratingText.getText().equals("")   || _filter.getSelectedItem().equals("") || 
 			       _slitWidthText.getText().equals("") || _slitAngleText.getText().equals("") || _progidText.getText().equals("") ||
 			       //			       _piText.getText().equals("") || _observerText.getText().equals("") || _commentText.getText().equals(""))
 			       _piText.getText().equals("") || _observerText.getText().equals(""))
 			    ){
 			    int result = JOptionPane.showConfirmDialog(Usdriver.this, 
 								       "At least one of target, grating etc fields is blank. Are you happy to proceed?", 
 								       "Confirm blank field(s)", JOptionPane.YES_NO_OPTION);
 			    if(result == JOptionPane.NO_OPTION){
 				logPanel.add("Application was not posted to the servers", LogPanel.WARNING, false);
 				return;
 			    }
 
 			}else if(CONFIRM_ON_CHANGE && _format.hasChanged()){
 			    int result = JOptionPane.showConfirmDialog(Usdriver.this, 
 								       "Format has changed with no target name change; is the current target (" + 
 								       _objectText.getText() + ") correct?", "Confirm target name", JOptionPane.YES_NO_OPTION);
 			    if(result == JOptionPane.NO_OPTION){
 				logPanel.add("Application was not posted to the servers", LogPanel.WARNING, false);
 				return;
 			    }
 			    _format.update();
 			}
 
 			if(CONFIRM_HV_GAIN_ON && _format.gainOn()){
 			    UIManager.put("OptionPane.background",         WARNING_COLOUR);
 			    int result = JOptionPane.showConfirmDialog(Usdriver.this, 
 								       "The avalanche gain is ON. Are you happy to proceed?", 
 								       "Confirm high gain OK", JOptionPane.YES_NO_OPTION);
 			    UIManager.put("OptionPane.background",         DEFAULT_COLOUR);
 			    if(result == JOptionPane.NO_OPTION){
 				logPanel.add("Application was not posted to the servers", LogPanel.WARNING, false);
 				return;
 			    }
 			    _format.update();
 			}
 
 			if(_postApp()){
 			    // second call to _postApp only occurs if firstPosting is true
 			    // this is to ensure all runs have a version number in xml
 			    // version number only available in memory after first successful posting!
 			    if(_firstPosting && _postApp()){
 				_firstPosting = false;
 			    }
 			    onPostApp();
 			}else{
 			    logPanel.add("Failed to post application to servers", LogPanel.ERROR, false);
 			}
 		    }
 		});
 	    addActionComponent( _obsPanel, postApp, xpos, ypos++);
 	    
 	    // Start a run
 	    startRun.addActionListener(
 		new ActionListener(){
 		    public void actionPerformed(ActionEvent e){
 			if(isRunActive(true)){
 			    int result = JOptionPane.showConfirmDialog(Usdriver.this, 
 								       "A run may already be active. Do you really want to try to start another?", 
 								       "Confirm start of run", JOptionPane.YES_NO_OPTION);
 			    if(result == JOptionPane.NO_OPTION){
 				logPanel.add("Failed to post application to servers", LogPanel.ERROR, false);
 				return;
 			    }
 			    
 			}else{
 			    // Update run number, will be incremented by 'onStartRun' if start of run is successful.
 			    getRunNumber();
 			}
 			
 			// try and move filter wheel
 			if(FILTER_WHEEL_ON){
 			    try{
 				if(!wheel.isConnected()){
 				    wheel.connect();
 				    wheel.init();
 				}
 				int desiredPos = _filter.getSelectedIndex()+1;
 				if(wheel.getPos() != desiredPos)
 				    wheel.move(desiredPos);
 			    }catch(Exception ex){
 				int result = JOptionPane.showConfirmDialog(Usdriver.this,
 									   "Couldn't move filter wheel to requested position\n"+ex.toString()+"\nDo you want to GO anyway?",
 									   "Confirm start of run", JOptionPane.YES_NO_OPTION);
 				if(result == JOptionPane.NO_OPTION){
 				    logPanel.add("Filter wheel move failed: run aborted", LogPanel.ERROR, false);
 				    return;
 				}
 			    }
 			}
 
 			if(_execCommand("GO", true))
 			    onStartRun();			       
 		    }
 		});
 	    addActionComponent( _obsPanel, startRun, xpos, ypos++);
 	    
 	    // Stop a run. 
 	    stopRun.addActionListener(
 		new ActionListener(){
 		    public void actionPerformed(ActionEvent e){
 			if(_execCommand("ST", true)){
 			    onStopRun();
 			}else{
 			    if(_runActive != null) _runActive.stop();
 			    _exposureMeter.stop();
 			}
 		    }
 		});
 	    addActionComponent( _obsPanel, stopRun, xpos, ypos++);
 	    
 	    // slide control
 	    slideCon.addActionListener(
 				   new ActionListener(){
 				       public void actionPerformed(ActionEvent e) {
 					   SwingUtilities.invokeLater(new Runnable() {
 						   public void run() {
 						       SlideController sc = new SlideController();
 						   }
 					       });
 				       }
 				   });
 	    addActionComponent( _obsPanel, slideCon, xpos, ypos++);
 
 
 	    // Add in to the card layout
 	    _actionPanel.insertTab("Observing", null, _obsPanel, null, 1);
 	    _actionPanel.setBorder(new EmptyBorder(15,15,15,15));
 	    
 	    return _actionPanel;
 	    
 	}else{
 	    
 	    _obsPanel.setBorder(new EmptyBorder(15,15,15,15));
 	    return _obsPanel;
 	    
 	}
     }
     
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Creates the panel displaying the timing & signal-to-noise information */
     public Component createTimingPanel(){
 	
 	// Timing info panel
 	JPanel _timingPanel = new JPanel(gbLayout);
 	
 	int ypos = 0;
 	
 	addComponent( _timingPanel, new JLabel("Frame rate (Hz)"), 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	_frameRate.setEditable(false);
 	addComponent( _timingPanel, _frameRate, 1, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	JLabel cycle = new JLabel("Cycle time (s)");
 	cycle.setToolTipText("Time from start of one exposure to the start of the next");
 	addComponent( _timingPanel, cycle, 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	_cycleTime.setEditable(false);
 	addComponent( _timingPanel, _cycleTime, 1, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	JLabel exp = new JLabel("Exposure time (s)");
 	addComponent( _timingPanel, exp, 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	_expTime.setEditable(false);
 	addComponent( _timingPanel, _expTime, 1, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	JLabel duty = new JLabel("Duty cycle (%)");
 	duty.setToolTipText("Percentage of time spent gathering photons");
 	addComponent( _timingPanel, duty, 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	_dutyCycle.setEditable(false);
 	addComponent( _timingPanel, _dutyCycle, 1, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	addComponent( _timingPanel, Box.createVerticalStrut(10), 0, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 
 	JLabel totalLabel = new JLabel("Total counts/exposure");
 	totalLabel.setToolTipText("Total counts/exposure in object, for an infinite radius photometric aperture");
 	addComponent( _timingPanel, totalLabel, 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	_totalCounts.setEditable(false);
 	addComponent( _timingPanel, _totalCounts, 1, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	JLabel peakLabel = new JLabel("Peak counts/exposure  ");
 	peakLabel.setToolTipText("In a binned pixel");
 	addComponent( _timingPanel,  peakLabel, 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	_peakCounts.setEditable(false);
 	addComponent( _timingPanel, _peakCounts, 1, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	JLabel stonLabelOne = new JLabel("S-to-N");
 	stonLabelOne.setToolTipText("Signal-to-noise in one exposure, 1.5*seeing aperture");
 	addComponent( _timingPanel,  stonLabelOne, 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	_signalToNoiseOne.setEditable(false);
 	addComponent( _timingPanel, _signalToNoiseOne, 1, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	JLabel stonLabel = new JLabel("S-to-N, 3 hr");
 	stonLabel.setToolTipText("Total signal-to-noise in a 3 hour run, 1.5*seeing aperture");
 	addComponent( _timingPanel,  stonLabel, 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	_signalToNoise.setEditable(false);
 	addComponent( _timingPanel, _signalToNoise, 1, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	// Extras if we are observing
 
 	if(OBSERVING_MODE){
 	    
 	    addComponent( _timingPanel, Box.createVerticalStrut(15), 0, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	    
 	    JLabel expStart = new JLabel("Exposure time  ");
 	    expStart.setToolTipText("Time since 'Start exposure' was pressed");
 	    addComponent( _timingPanel, expStart, 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	    _exposureTime.setEditable(false);
 	    addComponent( _timingPanel, _exposureTime, 1, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	    
 	    JLabel runNumber = new JLabel("Run number");
 	    runNumber.setToolTipText("Last run number or current run if one is in progress");
 	    addComponent( _timingPanel, runNumber, 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	    _runNumber.setEditable(false);
 	    addComponent( _timingPanel, _runNumber, 1, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	    // Define timer to provide an exposure meter
 	    
 	    // Add to the seconds and the amount of space fields
 	    ActionListener addSecond = new ActionListener() {
 		    public void actionPerformed(ActionEvent event) {
 			int nexpose = Integer.parseInt(Usdriver.this._exposureTime.getText());
 			nexpose++;
 			Usdriver.this._exposureTime.setText(String.valueOf(nexpose));
 		    }
 		};	
 	    
 	    // Timer is activated once per second
 	    _exposureMeter = new Timer(1000, addSecond);
 
 	    // Define the action for a timer to check for an active run. This is needed in the case
 	    // of a finite number of exposures since otherwise there is no way to tell
 	    // that the run has stopped
 	    _checkRun = new ActionListener() {
 		    public void actionPerformed(ActionEvent event) {
 			if(isRunActive(true)){
 			    startRun_enabled        = false;
 			    stopRun_enabled         = true;
 			    postApp_enabled         = false;
 			    resetSDSU_enabled       = false;
 			    resetPCI_enabled        = false;
 			    setupServer_enabled     = false;
 			    powerOn_enabled         = false;
 			}else{
 			    onStopRun();
 			}
 		    }
 		};
 	}    
 	_timingPanel.setBorder(new EmptyBorder(15,15,15,15));	
 	return _timingPanel;
     }
 
     //------------------------------------------------------------------------------------------------------------------------------------------
 
     /** Creates the panel which defines the window parameters */
     public Component createWindowPanel(){
 
 	int ypos = 0;
 
 	JPanel _windowPanel     = new JPanel( gbLayout );
 	_windowPanel.setBorder(new EmptyBorder(15,15,15,15));
 	    
 	// Application (drift etc)
 	// Get rid of this for now; Vik prefers a checkbox to switch between drift mode/standard mode
 	// have left it for future purposes, but disabled so only works with >2 templates
 	if(TEMPLATE_LABEL.length > 2){
 
 	    JLabel templateLabel = new JLabel("Template type");
 	    addComponent( _windowPanel, templateLabel, 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	}
 
 	templateChoice = new JComboBox(TEMPLATE_LABEL);
 	templateChoice.setSelectedItem(applicationTemplate);
 	templateChoice.setMaximumRowCount(TEMPLATE_LABEL.length);
 
 	if(TEMPLATE_LABEL.length > 2){
 
 	    // The main thing to do here is disable irrelevant parts according to 
 	    // the application
 	    templateChoice.addActionListener(
 		new ActionListener(){
 		    public void actionPerformed(ActionEvent e){
 			
 			applicationTemplate = (String) templateChoice.getSelectedItem();
 			setNumEnable();
 			_singleWindows.setNwin(numEnable);
 			if(numEnable == 0)
 			    _setWinLabels(false);
 			else
 			    _setWinLabels(true);
 			
 			oldApplicationTemplate = applicationTemplate;
 		    }
 		});
 	    
 	    // Add to the panel
 	    addComponent( _windowPanel, templateChoice, 1, ypos++,  5, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	}
 
 	// Readout mode selection
 	addComponent( _windowPanel,  new JLabel("CCD output"), 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	ccdOutputChoice = new JComboBox(CCD_OUTPUT_LABELS);
 	ccdOutputChoice.setSelectedItem(ccdOutput);
 	ccdOutputChoice.addActionListener(
 	    new ActionListener(){
 		public void actionPerformed(ActionEvent e){
 		    ccdOutput = (String) ccdOutputChoice.getSelectedItem();
 		    if(ccdOutputChoice.getSelectedItem().equals("Normal")){
 			hvGainText.setText("0");
 			hvGainText.setEnabled(false);
 			hvGainLabel.setEnabled(false);
 		    }else{
 			hvGainText.setEnabled(true);
 			hvGainLabel.setEnabled(true);
 		    }
 		}
 	    });
 	addComponent( _windowPanel, ccdOutputChoice, 1, ypos++,  5, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	// Avalanche gain setting
 	addComponent( _windowPanel,  hvGainLabel, 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _windowPanel, hvGainText, 1, ypos++,  5, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	if(ccdOutputChoice.getSelectedItem().equals("Normal")){
 	    hvGainText.setText("0");
 	    hvGainText.setEnabled(false);
 	    hvGainLabel.setEnabled(false);
 	}else{
 	    hvGainText.setEnabled(true);
 	    hvGainLabel.setEnabled(true);
 	}
 	hvGainLabel.setToolTipText("High-voltage gain setting from no gain (0) to the highest (9)");
 
 	// Readout speed selection
 	addComponent( _windowPanel,  new JLabel("Readout speed"), 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	speedChoice = new JComboBox(SPEED_LABELS);
 	speedChoice.setSelectedItem(readSpeed);
 	addComponent( _windowPanel, speedChoice, 1, ypos++,  5, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	// A little space
 	addComponent( _windowPanel, Box.createVerticalStrut(5), 0, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	JLabel clearEnabledLabel = new JLabel("Clear enabled");
 	clearEnabledLabel.setToolTipText("clear the CCD before each exposure or not");
 	addComponent( _windowPanel,  clearEnabledLabel, 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	clearEnabled.setSelected(false);
 	clearEnabled.setBackground(DEFAULT_COLOUR);
 	addComponent( _windowPanel, clearEnabled, 1, ypos++,  5, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	// Drift mode on/off
 	JLabel driftModeEnabledLabel = new JLabel("Drift Mode");
 	driftModeEnabledLabel.setToolTipText("enable Drift Mode");
 	addComponent( _windowPanel,  driftModeEnabledLabel, 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	driftModeEnabled.setSelected(false);
 	driftModeEnabled.setBackground(DEFAULT_COLOUR);
 	addComponent( _windowPanel, driftModeEnabled, 1, ypos++,  5, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	driftModeEnabled.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    boolean driftMode = driftModeEnabled.isSelected();
 		    if(driftMode){
 			_enableDriftMode();
 		    }else{
 			_disableDriftMode();
 		    }
 		}
 	    }
 	    );
 	
 	JLabel ledLabel = new JLabel("LED setting");
 	ledLabel.setToolTipText("setting of the LED (0 for when on target!)");
 	addComponent( _windowPanel,  ledLabel, 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _windowPanel, ledIntensityText, 1, ypos++,  5, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	// A little space
 	addComponent( _windowPanel, Box.createVerticalStrut(5), 0, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	// Exposure time
 	addComponent( _windowPanel, new JLabel("Exposure delay (millisecs)     "), 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	// Might need to adjust fine increment after a change of exposure time
 	exposeText.addActionListener(new ActionListener(){
 		public void actionPerformed(ActionEvent e){
 		    if(!EXPERT_MODE){
 			try {
 			    int n = exposeText.getValue();
 			    if(n == 0)
 				tinyExposeText.setText("7");
			    expose = 1;
 
 			} 
 			catch (Exception er) {
 			    tinyExposeText.setText("7");
 			}
 		    }
 		}
 	    });
 
 	JPanel exp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
 	exp.add(exposeText);
 
 	exp.add(new JLabel(" . "));
 	tinyExposeText.setEnabled(EXPERT_MODE);
 	exp.add(tinyExposeText);
 	addComponent( _windowPanel, exp, 1, ypos++,  5, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	// Number of exposures
 	addComponent( _windowPanel, new JLabel("Number of exposures"), 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _windowPanel, numExposeText, 1, ypos++,  5, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	// The binning factors
 	xbinText.setAllowed(POSSIBLE_BIN_FACTORS);
 	ybinText.setAllowed(POSSIBLE_BIN_FACTORS);
 	addComponent( _windowPanel, new JLabel("Binning factors (X, Y)"), 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _windowPanel, xbinText, 1, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _windowPanel, ybinText, 2, ypos++,  2, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	// Add some space before we get onto the window definitions
 	addComponent( _windowPanel, Box.createVerticalStrut(10), 0, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	// Number of windows for CCD 
 	addComponent( _windowPanel, new JLabel("Number of windows"), 0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _windowPanel, numWinText, 1, ypos++,  5, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	// Window definition lines
 	setNumEnable();
 	
 	// First the labels for each column
 	xstartLabel.setToolTipText("X value of first column of window");
 	ystartLabel.setToolTipText("Y value of lowest row of window");
 	nxLabel.setToolTipText("Number of unbinned pixels in X of window");
 	nyLabel.setToolTipText("Number of unbinned pixels in Y of window");
 
 	int xpos = 0;
 	addComponent( _windowPanel, windowsLabel, xpos++, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _windowPanel, xstartLabel,  xpos++, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	addComponent( _windowPanel, ystartLabel,  xpos++, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	addComponent( _windowPanel, nxLabel,      xpos++, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	addComponent( _windowPanel, nyLabel,      xpos++, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	//ypos++;
 
 	// add drift mode labels and make them invisible
 	xpos = 1;
 	addComponent( _windowPanel, ystartLabel2, xpos++, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	addComponent( _windowPanel, xleftLabel,   xpos++, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	addComponent( _windowPanel, xrightLabel,  xpos++, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	addComponent( _windowPanel, nxLabel2,     xpos++, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	addComponent( _windowPanel, nyLabel2,     xpos++, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	ystartLabel2.setVisible(false);
 	xleftLabel.setVisible(false);
 	xrightLabel.setVisible(false);
 	nxLabel2.setVisible(false);
 	nyLabel2.setVisible(false);
 	ypos++;
 			
 	// Then the row labels and fields for integer input
 	_singleWindows = new SingleWindows(gbLayout, _windowPanel, ypos, xbin, ybin, DEFAULT_COLOUR, ERROR_COLOUR);
 	_singleWindows.setNwin(numEnable);
 	// SL - edited here to allow greater than 2 windows (18/7/2012)
 	ypos += numEnable;
 	
 	// or windowPairs for drift mode
 	_windowPairs = new WindowPairs(gbLayout, _windowPanel, ypos, xbin, ybin, DEFAULT_COLOUR, ERROR_COLOUR, specialNy);
 	_windowPairs.setNpair(1);
 	_windowPairs.setVisible(false);
 
 	// Add some space between window definitions and the user-defined stuff
 	addComponent( _windowPanel, Box.createVerticalStrut(20), 0, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	// Add a border
 	_windowPanel.setBorder(new EmptyBorder(15,15,15,15));	
 	return _windowPanel;
     }
 
     /** Creates the panel for instrument setup (filters/gratings) */
     public Component createInstPanel(){
 	int ypos=0;
 	JPanel _instPanel = new JPanel(gbLayout); 
 
 	_filter = new JComboBox(ACTIVE_FILTER_NAMES);
 	_filter.setSelectedItem(defaultFilter);
 
 	addComponent( _instPanel, new JLabel("Filters"),     0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _instPanel, _filter,                   1, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	addComponent( _instPanel, new JLabel("Grating"),     0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _instPanel, _gratingText,              1, ypos++,  3, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 
 
 
 	JLabel slitWidthLabel = new JLabel("Slit width");
 	slitWidthLabel.setToolTipText("Slit width in arcseconds");
 	addComponent( _instPanel, slitWidthLabel,     0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _instPanel, _slitWidthText,     1, ypos++,  3, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	JLabel slitAngleLabel = new JLabel("Slit PA");
 	slitAngleLabel.setToolTipText("Slit angle, measured North through East");
 	addComponent( _instPanel, slitAngleLabel,     0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _instPanel, _slitAngleText,     1, ypos++,  3, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	_instPanel.setBorder(new EmptyBorder(15,15,15,15));	
 	return _instPanel;
 
     }
 
     /** Creates the panel for user-supplied information for the L3CCD */
     public Component createUserPanel(){
 
 	int ypos = 0;
 	
 	// Target info panel
 	JPanel _userPanel = new JPanel(gbLayout);
 
 	addComponent( _userPanel, new JLabel("Target name  "),     0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _userPanel, _objectText,     1, ypos,  3, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	final JButton lookupButton = new JButton("Verify");
 	addComponent( _userPanel, lookupButton, 4, ypos++, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	lookupButton.addActionListener(new ActionListener(){
 		public void actionPerformed(ActionEvent e)
 		{ 
 		    if (_verifyTarget(_objectText.getText())) { lookupButton.setBackground(GO_COLOUR); } else { lookupButton.setBackground(ERROR_COLOUR); } 
 		}
 	    }
 	    );
 	_objectText.addKeyListener(new KeyListener()
 	    {
 		public void keyPressed(KeyEvent e){ lookupButton.setBackground(DEFAULT_COLOUR); }
 		public void keyReleased(KeyEvent e) {}
 		public void keyTyped(KeyEvent e) {}
 	    });
 
 	addComponent( _userPanel, new JLabel("Programme ID"),     0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _userPanel, _progidText,     1, ypos++,  3, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	addComponent( _userPanel, new JLabel("Principal Investigator "),     0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _userPanel, _piText,     1, ypos++,  3, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	addComponent( _userPanel, new JLabel("Observer(s)"),     0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _userPanel, _observerText,     1, ypos++,  3, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	// run type selection box
 	addComponent( _userPanel, new JLabel("Run type"), 0, ypos, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	_dataButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){
 	    _runType = "data"; _acquisitionState = false; _checkEnabledFields();}});
 	addComponent( _userPanel, _dataButton,     1, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	_dataButton.setSelected(true);
 	_acqButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){
 	    _runType = "data"; _acquisitionState = true; _checkEnabledFields();}});
 	addComponent( _userPanel, _acqButton,     2, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	_biasButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){
 	    _runType = "bias"; _acquisitionState = false; _checkEnabledFields();}});
 	addComponent( _userPanel, _biasButton,     3, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	_flatButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){
 	    _runType = "flat"; _acquisitionState = false; _checkEnabledFields();}});
 	addComponent( _userPanel, _flatButton,     1, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	_darkButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){
 	    _runType = "dark"; _acquisitionState = false; _checkEnabledFields();}});
 	addComponent( _userPanel, _darkButton,     2, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	_techButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){
 	    _runType = "technical"; _acquisitionState = false; _checkEnabledFields();}});
 	addComponent( _userPanel, _techButton,     3, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	ButtonGroup runTypeGroup = new ButtonGroup();
 	runTypeGroup.add(_dataButton);
 	runTypeGroup.add(_biasButton);
 	runTypeGroup.add(_flatButton);
 	runTypeGroup.add(_darkButton);
 	runTypeGroup.add(_techButton);
 	runTypeGroup.add(_acqButton);
 
 
 	//	addComponent( _userPanel, new JLabel("Comment"),         0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	//	addComponent( _userPanel, _commentText,     1, ypos++,  5, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	_userPanel.setBorder(new EmptyBorder(15,15,15,15));	
 	return _userPanel;
 
     }
 
     /** Creates the panel defining the target information */
     public Component createTargetPanel(){
     
 	int ypos = 0;
 	
 	// Target info panel
 	JPanel _targetPanel = new JPanel(gbLayout);
 	
 	JLabel bandLabel = new JLabel("Bandpass");
 	bandLabel.setToolTipText("Bandpass for estimating counts and signal-to-noise");
 	addComponent( _targetPanel, bandLabel,     0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	// Create radio buttons for the filters
 	JRadioButton uButton = new JRadioButton("u'     ");
 	uButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){_filterIndex = 0;}});
 	addComponent( _targetPanel, uButton,     1, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	JRadioButton gButton = new JRadioButton("g'     ");
 	gButton.setSelected(true);
 	gButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){_filterIndex = 1;}});
 	addComponent( _targetPanel, gButton,     2, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	JRadioButton rButton = new JRadioButton("r'     ");
 	rButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){_filterIndex = 2;}});
 	addComponent( _targetPanel, rButton,     3, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	JRadioButton iButton = new JRadioButton("i'     ");
 	iButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){_filterIndex = 3;}});
 	addComponent( _targetPanel, iButton,     4, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	JRadioButton zButton = new JRadioButton("z'");
 	zButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){_filterIndex = 4;}});
 	addComponent( _targetPanel, zButton,     5, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	// Group the radio buttons.
 	ButtonGroup fGroup = new ButtonGroup();
 	fGroup.add(uButton);
 	fGroup.add(gButton);
 	fGroup.add(rButton);
 	fGroup.add(iButton);
 	fGroup.add(zButton);
 
 	JLabel magLabel = new JLabel("Magnitude");
 	magLabel.setToolTipText("Magnitude at airmass=0 for estimating counts and signal-to-noise");
 	addComponent( _targetPanel, magLabel,     0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _targetPanel, _magnitudeText,     1, ypos++,  5, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	JLabel seeingLabel = new JLabel("Seeing (FWHM, arcsec)     ");
 	seeingLabel.setToolTipText("FWHM seeing. Aperture assumed to be 1.5 times this.");
 	addComponent( _targetPanel, seeingLabel,     0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _targetPanel, _seeingText,     1, ypos++,  5, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	JLabel skyBackLabel = new JLabel("Sky brightness");
 	addComponent( _targetPanel, skyBackLabel,     0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	// Create radio buttons for the sky brightness
 	JRadioButton darkButton = new JRadioButton("dark");
 	darkButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){_skyBrightIndex = 0;}});
 	addComponent( _targetPanel, darkButton,     1, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	JRadioButton greyButton = new JRadioButton("grey");
 	greyButton.setSelected(true);
 	greyButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){_skyBrightIndex = 1;}});
 	addComponent( _targetPanel, greyButton,     2, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	JRadioButton brightButton = new JRadioButton("bright");
 	brightButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){_skyBrightIndex = 2;}});
 	addComponent( _targetPanel, brightButton,     3, ypos++,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	
 	// Group the radio buttons.
 	ButtonGroup sGroup = new ButtonGroup();
 	sGroup.add(darkButton);
 	sGroup.add(greyButton);
 	sGroup.add(brightButton);
 
 	JLabel airmassLabel = new JLabel("Airmass");
 	addComponent( _targetPanel, airmassLabel,     0, ypos,  1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 	addComponent( _targetPanel, _airmassText,     1, ypos++,  5, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
 
 	_targetPanel.setBorder(new EmptyBorder(15,15,15,15));	
 	return _targetPanel;
     }
 }
 
     
