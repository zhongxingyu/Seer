 /*
  * Copyright 1999 - 2013 Herb Bowie
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.powersurgepub.pstextmerge;
 
   import com.powersurgepub.psdatalib.pslist.*;
   import com.powersurgepub.psdatalib.textmerge.*;
   import com.powersurgepub.psdatalib.script.*;
   import com.powersurgepub.psdatalib.ui.*;
   import com.powersurgepub.psdatalib.psdata.*;
   import com.powersurgepub.psutils.*;
   import com.powersurgepub.xos2.*;
   import java.awt.*;
   import java.awt.event.*;
   import java.io.*;
   import java.net.*;
   import java.text.*;
   import javax.swing.*;
   import javax.swing.border.*;
   import javax.swing.table.*;
   
 /**
    An application with a GUI interface to do all sorts of things 
    with tabular data. <p>
   
    This code is copyright (c) 1999-2013 by Herb Bowie of PowerSurge Publishing. 
    All rights reserved. <p>
   
    @author Herb Bowie
  */
 
 public class PSTextMerge 
     implements 
       TextMergeController, 
       PSFileOpener, 
       XHandler {
 
 	/*
 	   Constants
 	 */
 	
 	// String associated with a logging threshold of normal severity. 
   public    static final String  LOG_NORMAL_STRING = "Normal";
   
   // String associated with a logging threshold of minor severity. 
   public    static final String  LOG_MINOR_STRING  = "Minor";
   
   // String associated with a logging threshold of medium severity. 
   public    static  final String  LOG_MEDIUM_STRING = "Medium";
   
   // String associated with a logging threshold of major severity. 
   public    static  final String  LOG_MAJOR_STRING  = "Major";    
 
 	// Default values for tab configurations. 
 	private   static  final String  DEFAULT_TABS = "IVSFOTCLA";
   
   // Maximum number of records that can be processed in demo mode.
   private		static	final	int			DEMO_MAX_RECORDS = 20;
   
   /** Program Name */
   public    static  final String  PROGRAM_NAME = "PSTextMerge";
  public    static  final String  PROGRAM_VERSION = "4.10";
   
   private   static  final String  USER_GUIDE
       = "userguide/pstextmerge.html";
   
   private   static  final String  PROGRAM_HISTORY
       = "versions.html";
   
   /**
      Command line arguments.
    */
   private boolean quietMode = false;
   private String  startingScript = "";
 
   private ScriptExecutor scriptExecutor = null;
   
   // Flag to indicate this is not the main program
   private boolean mainClass = true;
   
   private     static  final int   DEFAULT_WIDTH = 680;
   private     static  final int   DEFAULT_HEIGHT = 450;
   
   private                   int   defaultX = 0;
   private                   int   defaultY = 0;
   
 	/*
 	   Fields to control the tabs that are displayed.
 	 */
 	// Tab configuration. 
 	private     String              tabConfig = DEFAULT_TABS;
 	
 	// Should Input Tab be displayed? 
 	private     boolean             inputTabDisplay = false;
 	
 	// Should View Tab be displayed? 
 	private     boolean             viewTabDisplay = false;
 	
 	// Should Sort Tab be displayed? 
 	private     boolean             sortTabDisplay = false;
 	
 	// Should Filter Tab be displayed? 
 	private     boolean             filterTabDisplay = false;
 	
 	// Should Output Tab be displayed? 
 	private     boolean             outputTabDisplay = false;
 	
 	// Should Template Tab be displayed? 
 	private     boolean             templateTabDisplay = false;
 	
 	// Should Script Tab be displayed? 
 	private     boolean             scriptTabDisplay = false;
 	
 	// Should Logging Tab be displayed? 
 	private     boolean             logTabDisplay = false;
   
   private     DataRecList           list = new DataRecList();
   
   private     TextMergeInput        textMergeInput = null;
   private     TextMergeScript       textMergeScript = null;
   private     TextMergeFilter       textMergeFilter = null;
   private     TextMergeSort         textMergeSort = null;
   private     TextMergeTemplate     textMergeTemplate = null;
   private     TextMergeOutput       textMergeOutput = null;
 	
   // A log juggler to create other logging objects and perform logging operations. 
   private     LogJuggler          logJuggler;
   
   // The output destination for log messages. 
   private     LogOutput           logout;
   
   // The object that performs the logging operations. 
   private     Logger              log;
   
   // An object that can define an event worth logging. 
   private     LogEvent            logEvent;
   
   private     Trouble             trouble;
   
   // Debugging Instance
   private			Debug								debug = new Debug (false);
   
   /** Various system properties. */
   // private			String							mrjVersion;
   private     File                appFolder;
   private     String              userDirString;
   private     String              fileSeparatorString;
   private     File                userDirFile;
   private     URL                 pageURL;
   private     URL                 userGuideURL;
   private     URL                 programHistoryURL;
   // private			File								userGuideFile;
   // private			String							userGuideString;
   
   private     File                currentDirectory = null;
   
   private     boolean             listAvailable = false;
 	            
 	private     int                 numberOfFields = 0;
   
   private     String              possibleFileName = "";
   private     String              fileName = "";
 
   private     String              lastTabNameOutput = "";
   /** File name of file that contains basic information about PSTextMerge. */
   private     String              aboutFileName = "about.html";
 
   private     DataSource          dataSource;
 	
   // Miscellaneous Fields
 	private     String              iconFileName = "pstextmerge_icon_32.gif";
 	private     URL                 iconURL;
 	private     ImageIcon           iconImage;
   
   // Fields used for tab-delimited data files, whether input or output
   private     FileName            tabFileName;
   
   // Cross-Platform Support
   private     XOS                 xos = XOS.getShared();
   
   private     Home                home;
   private     UserPrefs           userPrefs;
   
   // Fields used for the User Interface
   private			JFrame							mainFrame;
   private     Border              raisedBevel;
   private     Border              etched;
   
   // Menu Objects
   private			JMenuBar						menuBar;
   
   private			JMenu								fileMenu;
   private			JMenuItem						fileOpen;
   private			JMenuItem						fileExit;
   
   private     JMenu               windowMenu;
   
   private			JMenu								helpMenu;
   private     JMenuItem           helpHistory;
   private			JMenuItem						helpUserGuide;
   private     JSeparator          helpSeparator2;
   private			JMenuItem						helpAbout;
   
   private			JMenuItem						helpPSPubWebSite;
   private     JSeparator          helpSeparator3;
   private     JMenuItem           helpReduceWindowSize;
 
 	
 	private     GridBagger          gb = new GridBagger();
 	
 	private     JPanel              header;
 	private     JLabel              programNameLabel;
 	private     JLabel              fileNameLiteral   = new JLabel ("File Name:");
 	private     JLabel              tabNameLabel      = new JLabel();
 	private     JLabel              iconLabel;
 	
 	private     JTabbedPane         tabs;
 	
 	private     Insets              insetsTLBR0 = new Insets (0, 0, 0, 0);
 	private     Insets              insetsTLBR1 = new Insets (1, 1, 1, 1);
 	private     Insets              insetsTB2LR1 = new Insets (2, 1, 2, 1);
 	private     Insets              insetsTB1LR3 = new Insets (1, 3, 1, 3);
 	
 	private     int                 tabPosition = 0;
 	private     int                 viewTabPosition = 0;
               
   // View Panel Objects
   private     JPanel              viewTab;
   private     JScrollPane         tableScrollPane;
   private     JTable              tabTable;
   private     boolean             tabTableBuilt = false;
   
   private     boolean             sortTabBuilt      = false;  
   private     boolean             filterTabBuilt    = false;
   private     boolean             templateTabBuilt  = false;
   
   // Logging Panel objects
   private    JPanel               logTab;
   
   // Column 1 - Logging Output
   private    JLabel               logOutputLabel;
   private    ButtonGroup          logOutputGroup;
   private    JRadioButton         logOutputNoneButton;
   private    JRadioButton         logOutputWindowButton;
   private		 JRadioButton					logOutputTextButton;
   private    JRadioButton         logOutputDiskButton;
   
   // Column 2 - Logging Threshold
   private    JLabel               logThresholdLabel;
   private    ButtonGroup          logThresholdGroup;
   private    JRadioButton         logThresholdNormalButton;
   private    JRadioButton         logThresholdMinorButton;
   private    JRadioButton         logThresholdMediumButton;
   private    JRadioButton         logThresholdMajorButton;
   
   // Column 3 - Log All Data?
   private    JLabel               logAllDataLabel;
   private    JCheckBox            logAllDataCkBox;
   
   // Bottom of Panel
   private     JScrollPane         logTextScrollPane;
   private     JTextArea           logText;
   
   private     AboutWindow         aboutWindow;
   
 	/**
      Main method.
    */
 	public static void main(String[] args) {
 		PSTextMerge merge = new PSTextMerge(args);
     merge.setMainClass (true);
     merge.run(null);
 	} // end main method
   
 	/**
      Play a script.
    
      @param script    Name of script file to be played.
      @param logOutput Output destination for log messages.
    */
 	public static void execScript (String script, Logger logIn) {
     String[] args = new String[2];
     args[0] = "-q";
     args[1] = script;
 		PSTextMerge merge = new PSTextMerge(args);
     merge.setMainClass (false);
     logIn.recordEvent (LogEvent.NORMAL, 
         PROGRAM_NAME + " " + PROGRAM_VERSION + " invoked by another program",
         false);
     merge.run(logIn);
 	} // end execScript method
 
 	/**
      Play a script.
 
      @param script    Name of script file to be played.
      @param logOutput Output destination for log messages.
      @param scriptExecutor A class that is prepared to execute requested
                            script callbacks.
    */
 	public static void execScript (String script, Logger logIn,
       ScriptExecutor scriptExecutor) {
     String[] args = new String[2];
     args[0] = "-q";
     args[1] = script;
 		PSTextMerge merge = new PSTextMerge(args);
     merge.setMainClass (false);
     merge.setScriptExecutor(scriptExecutor);
     logIn.recordEvent (LogEvent.MEDIUM,
         PROGRAM_NAME + " " + PROGRAM_VERSION + " invoked by another program",
         false);
     merge.run(logIn);
 	} // end execScript method
 
 	/**
      Constructor.
    */
   public PSTextMerge(String[] args) {
     for (int i = 0; i < args.length; i++) {
       String arg = args[i];
       if (arg.startsWith ("-")) {
         arg = arg.toLowerCase();
         for (int j = 1; j < arg.length(); j++) {
           char opt = arg.charAt (j);
           if (opt == 'q') {
             quietMode = true;
           }
         }
       } else {
         startingScript = arg;
       }
     } // end argument processing
     
 	}
   
   /**
     Indicate whether this is the main class.
    
     @param mainClass True if this is the main class, false if called from another.
    */
   public void setMainClass (boolean mainClass) {
     this.mainClass = mainClass;
   }
 
   /**
    Set a class to be used for callbacks. 
   
    @param scriptExecutor The class to be used for callbacks.
   */
   public void setScriptExecutor(ScriptExecutor scriptExecutor) {
     this.scriptExecutor = scriptExecutor;
     if (textMergeScript != null) {
       textMergeScript.setScriptExecutor(scriptExecutor);
     }
   }
   
   /**
      Get the user interface up and running.
    */
   private void run(Logger logIn) {
     trouble = Trouble.getShared();
     if (mainClass) {
       xos.setDomainLevel1 ("powersurgepub");
       xos.setDomainLevel2 ("com");
       xos.setProgramName (PROGRAM_NAME);
       xos.initialize();
     }
 
     mainFrame = new JFrame(PROGRAM_NAME);
     if (mainClass) {
       xos.setMainWindow (mainFrame);
       xos.setXHandler (this);
       home = Home.getShared(PROGRAM_NAME, PROGRAM_VERSION);
     } else {
       home = Home.getShared();
     }
     appFolder = home.getAppFolder();
     
     list = new DataRecList();
     textMergeScript   = new TextMergeScript   (list);
     textMergeInput    = new TextMergeInput    (list, this, textMergeScript);
     textMergeFilter   = new TextMergeFilter   (list, textMergeScript);
     textMergeSort     = new TextMergeSort     (list, textMergeScript);
     textMergeTemplate = new TextMergeTemplate (list, textMergeScript);
     textMergeOutput   = new TextMergeOutput   (list, textMergeScript);
     textMergeScript.allowAutoplay(mainClass);
     textMergeScript.setInputModule(textMergeInput);
     textMergeScript.setFilterModule(textMergeFilter);
     textMergeScript.setOutputModule(textMergeOutput);
     textMergeScript.setSortModule(textMergeSort);
     textMergeScript.setTemplateModule(textMergeTemplate);
     textMergeScript.setScriptExecutor(scriptExecutor);
     
 	  if (mainClass) {
       mainFrame.getRootPane().putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);
       mainFrame.addWindowListener (new WindowAdapter()
         {
           public void windowClosing (WindowEvent e) {
             handleQuit();
           } // end ActionPerformed method
         } // end action listener for filter fields combo box
       ); 
     }
 
 		try {
 			initComponents(logIn);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 
     if (mainClass) {
       xos.setFileMenu (fileMenu);
       xos.setHelpMenu (helpMenu);
       WindowMenuManager.getShared().addWindowMenu(windowMenu);
       try {
         userGuideURL = new URL (pageURL, USER_GUIDE);
       } catch (MalformedURLException e) {
       }
       try {
         programHistoryURL = new URL(pageURL, PROGRAM_HISTORY);
       } catch (MalformedURLException e) {
         // shouldn't happen
       }
       xos.setHelpMenuItem (helpUserGuide);
       Trouble.getShared().setParent(mainFrame);
     }
     calcDefaultScreenLocation();
     mainFrame.setBounds (
         userPrefs.getPrefAsInt (UserPrefs.LEFT, defaultX),
         userPrefs.getPrefAsInt (UserPrefs.TOP,  defaultY),
         userPrefs.getPrefAsInt (UserPrefs.WIDTH, DEFAULT_WIDTH),
         userPrefs.getPrefAsInt (UserPrefs.HEIGHT, DEFAULT_HEIGHT));
     
     // Open script file if it was passed as a parameter
     possibleFileName = System.getProperty ("scriptfile", "");
     if ((possibleFileName != null) && (! possibleFileName.equals (""))) {
       fileName = possibleFileName;
     }
     else
     if (! startingScript.equals ("")) {
       fileName = startingScript;
     } 
     if (! fileName.equals("")) {
       File sFile = new File (fileName);
       boolean scriptFound = (sFile.exists() && sFile.isFile());
       if (! scriptFound) {
         log.recordEvent (LogEvent.MEDIUM, 
           "MSG001 " + sFile.toString() + " could not be opened as a valid Script File",
           true);
         sFile = new File (currentDirectory, fileName);
         scriptFound = (sFile.exists() && sFile.isFile());
       }
       if (scriptFound) {
         textMergeScript.playScript(sFile);
       } 
       else {
         log.recordEvent (LogEvent.MEDIUM, 
           sFile.toString() + " could not be opened as a valid Script File",
           true);
       } 
     } // end if non-blank file name
     
     if (quietMode) {
       handleQuit();
     } else {
       mainFrame.setVisible (true);
       textMergeScript.checkAutoPlay();
     } // end if not quiet mode
   }
   
   private void setDefaultScreenSizeAndLocation() {
 
 		mainFrame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
     mainFrame.setResizable (true);
 		calcDefaultScreenLocation();
 		mainFrame.setLocation (defaultX, defaultY);
   }
   
   private void calcDefaultScreenLocation() {
     Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
     defaultX = (d.width - mainFrame.getSize().width) / 2;
     defaultY = (d.height - mainFrame.getSize().height) / 2;
   }
 
 	public void initComponents(Logger logIn) throws Exception {  
     
     // Get System Properties
     userDirString = System.getProperty (GlobalConstants.USER_DIR);
     if ((userDirString != null) && (! userDirString.equals (""))) {
       userDirFile = new File (userDirString);
       currentDirectory = userDirFile;
       textMergeScript.setNormalizerPath(userDirString);
      // System.out.println ("User Directory = " + userDirString);
     }
     currentDirectory = home.getUserHome();
     fileSeparatorString = System.getProperty (GlobalConstants.FILE_SEPARATOR, "/");
     try {
       pageURL = appFolder.toURI().toURL(); 
     } catch (MalformedURLException e) {
       trouble.report ("Trouble forming pageURL from " + appFolder.toString(), 
           "URL Problem");
     }
     
     // Set up logging stuff
     logJuggler = new LogJuggler("PSTextMerge");
     if (logIn != null) {
       logJuggler.setLog (logIn.getLog());
     }
     else
     if (quietMode) {
       logJuggler.setLog (LogJuggler.LOG_DISK_STRING);
     } 
     else {
       logJuggler.setLog (LogJuggler.LOG_TEXT_STRING);
     }
     logout = logJuggler.getLog();
     log = logJuggler.getLogger(); 
     if (logIn == null) {
       log.setLogAllData (false);
       log.setLogThreshold (LogEvent.NORMAL);
     } else {
       log.setLogAllData (logIn.getLogAllData());
       log.setLogThreshold (logIn.getLogThreshold());
     }
     logEvent = new LogEvent();
    // System.out.println ("Log Threshold is " + String.valueOf (log.getLogThreshold()));
    // log.recordEvent (LogEvent.NORMAL, 
    //     "PSTextMerge Log File established",
    //    false);
     
     // Determine which Tabs are to be displayed
     tabConfig = System.getProperty ("tabs", DEFAULT_TABS);
     for (int i = 0; i < tabConfig.length(); i++) {
       char t = Character.toUpperCase (tabConfig.charAt (i));
       switch (t) {
         case 'I':
           inputTabDisplay = true;
           break;
         case 'V':
           viewTabDisplay = true;
           break;
         case 'S':
           sortTabDisplay = true;
           break;
         case 'F':
           filterTabDisplay = true;
           break;
         case 'O':
         	outputTabDisplay = true;
         	break;
         case 'T':
           templateTabDisplay = true;
           break;
         case 'C':
           scriptTabDisplay = true;
           break;
         case 'L':
           logTabDisplay = true;
           break;
         case 'A':
           // aboutTabDisplay = true;
           break;
         default:
           log.recordEvent (LogEvent.MINOR, 
             String.valueOf (t) + " is not a valid Tab Identifier",
             false);
           break;
       } // end switch
     } // end for loop
     
     // Get info from Parms file
     if (mainClass) {
       home = Home.getShared (PROGRAM_NAME, PROGRAM_VERSION);
     } else {
       home = Home.getShared();
     }
     userPrefs = UserPrefs.getShared();
     
     // Create common interface components
     raisedBevel = BorderFactory.createRaisedBevelBorder();
     etched      = BorderFactory.createEtchedBorder();
     
     // Create Menu Bar
     menuBar = new JMenuBar();
     mainFrame.setJMenuBar (menuBar);
     
     fileMenu = new JMenu("File");
     menuBar.add (fileMenu);
         
     // Create Header
     header = new JPanel();
     iconURL = new URL (pageURL, iconFileName);
     iconImage = new ImageIcon (iconURL);
     iconLabel = new JLabel (iconImage, JLabel.CENTER);
     iconLabel.setVisible (true);
     fileNameLiteral.setVisible (true);
     tabNameLabel.setVisible (true);
 		tabNameLabel.setText (textMergeInput.getFileNameToDisplay());
 		gb.startLayout (header, 5, 1);
 		gb.setDefaultColumnWeight (0.0);
 		gb.setAllInsets (1);
 		gb.add (iconLabel);
 		gb.setLeftRightInsets (10);
 		gb.add (fileNameLiteral);
 		gb.setColumnWeight (1.0);
 		gb.add (tabNameLabel);
 		gb.setAllInsets (1);
     mainFrame.getContentPane().add (header, BorderLayout.NORTH);
     
 		// Create tabbed pane
 		tabs = new JTabbedPane();
     
     if (inputTabDisplay) {
       textMergeInput.setTabs(tabs);
       textMergeInput.setMenus(menuBar);
       tabPosition++;
     }
 		
 		// Create view Tab with Table
 		if (viewTabDisplay) {
 		  addViewTab();
 		  viewTabPosition = tabPosition;
 		  tabPosition++;
 		}
 		
 		// Create Sort Pane
 		if (sortTabDisplay) {
 		  textMergeSort.setTabs(tabs, true);
 		  sortTabBuilt = true;
 		  tabPosition++;
 		}
 		
 		// Create Filter Pane
 		if (filterTabDisplay) {
       textMergeFilter.setTabs(tabs);
       filterTabBuilt = true;
 		  tabPosition++;
 		}
 		
 		// Create Output Pane
 		if (outputTabDisplay) {
       textMergeOutput.setTabs(tabs);
       textMergeOutput.setMenus(menuBar);
 			tabPosition++;
 		}
 		
 		// Create a Template Pane
 		if (templateTabDisplay) {
       textMergeTemplate.setTabs(tabs);
       textMergeTemplate.setMenus(menuBar);
       templateTabBuilt = true;
 		  tabPosition++;
 		}
 		
 		// Create Script Pane
 		if (scriptTabDisplay) {
       textMergeScript.setTabs(tabs);
       textMergeScript.setMenus(menuBar);
 		  tabPosition++;
 		}
 		
 		// Create Logging Pane
 		if (logTabDisplay) {
 		  addLoggingTab();
 		  tabPosition++;
 		}
 		
 		// Create About Pane
     aboutWindow = new AboutWindow (false);
     
     // Finish off File Menu
     if (! xos.isRunningOnMacOS()) {
       fileExit = new JMenuItem ("Exit/Quit");
       fileExit.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_Q,
         Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
       fileMenu.add (fileExit);
       fileExit.addActionListener (new ActionListener ()
         {
           public void actionPerformed (ActionEvent event) {
             handleQuit();
           } // end actionPerformed method
         } // end action listener
       );
     }
     
     // Window menu
     windowMenu = new JMenu("Window");
     menuBar.add (windowMenu);
     
     // Help Menu 
     helpMenu = new JMenu("Help");
     menuBar.add (helpMenu);
     
     /* if (! xos.isRunningOnMacOS()) {
       helpAbout = new JMenuItem ("About " + PROGRAM_NAME);
       helpMenu.add (helpAbout);
       helpAbout.addActionListener (new ActionListener ()
         {
           public void actionPerformed (ActionEvent event) {
             handleAbout();
           } // end actionPerformed method
         } // end action listener
       );
     } */
     
     helpHistory = new JMenuItem ("Program History");
     helpMenu.add (helpHistory);
     helpHistory.addActionListener(new ActionListener()
     {
       public void actionPerformed (ActionEvent event) {
         openURL (programHistoryURL);
       }
     });
     
     helpUserGuide = new JMenuItem ("User Guide");
     helpMenu.add (helpUserGuide);
     helpUserGuide.addActionListener (new ActionListener() 
       {
         public void actionPerformed (ActionEvent event) {
           openURL (userGuideURL);
         } // end ActionPerformed method
       } // end action listener
     );
     
     helpSeparator2 = new JSeparator();
     helpMenu.add (helpSeparator2);
 
     helpPSPubWebSite = new JMenuItem (PROGRAM_NAME + " Home Page");
     helpMenu.add (helpPSPubWebSite);
     helpPSPubWebSite.addActionListener (new ActionListener() 
       {
         public void actionPerformed (ActionEvent event) {
           openURL ("http://www.powersurgepub.com/");
         } // end ActionPerformed method
       } // end action listener
     );
     
     helpSeparator3 = new JSeparator();
     helpMenu.add (helpSeparator3);
     
     helpReduceWindowSize = new JMenuItem ("Reduce Window Size");
     helpReduceWindowSize.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_W,
         Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
     helpMenu.add (helpReduceWindowSize);
     helpReduceWindowSize.addActionListener(new ActionListener()
       {
         public void actionPerformed (ActionEvent event) {
           setDefaultScreenSizeAndLocation();
         }
       });
     
     log.recordEvent (LogEvent.NORMAL,
         "Application Folder = " + home.getAppFolder().toString(),
         false);
     log.recordEvent (LogEvent.NORMAL,
         "Java Virtual Machine = " + System.getProperty("java.vm.name") + 
         " version " + System.getProperty("java.vm.version") +
         " from " + StringUtils.removeQuotes(System.getProperty("java.vm.vendor")),
         false);
     Runtime runtime = Runtime.getRuntime();
     runtime.gc();
     NumberFormat numberFormat = NumberFormat.getInstance();
     log.recordEvent (LogEvent.NORMAL,
         "Available Memory = " + numberFormat.format (Runtime.getRuntime().freeMemory()),
         false);
 		
 		// Set the starting Tab that will be visible
 		if (listAvailable) {
 		  tabs.setSelectedIndex (viewTabPosition);
 		}
 		else {
 		  tabs.setSelectedIndex (0);
 		}
 		
 		// Add the Tabbed Pane to the Applet
 		mainFrame.getContentPane().add (tabs, BorderLayout.CENTER);
 
 	} // end initComponents method	  
   
   /**
      Throw a URL to the local Web Browser for display.
    */
   public boolean openURL (URL url) {
     return home.openURL(url);
   }
   
   private boolean openURL (String url) {
     return home.openURL(url);
   }
 	
 	/**
 	   Add the View tab to the interface. 
 	 */
 	private void addViewTab() {
 		viewTab = new JPanel();
 
 		tabTable = new JTable(list);
 		tabTable.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
 		tabTableBuilt = true;
     setTableModels();
 		tabTable.setVisible(true);
 		
 		tableScrollPane = new JScrollPane(tabTable, 
 		  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 		  JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		tableScrollPane.setVisible(true);
 
 		mainFrame.setLocation(new java.awt.Point(0, 0));
 
 		gb.startLayout (viewTab, 1, 1);
 		gb.setAllInsets (0);
 		gb.add (tableScrollPane);
 		
 		tabs.addTab ("View", viewTab);
 	} // end method addViewTab
   
 	/**
 	   Add the Logging tab to the interface. 
 	 */
 	private void addLoggingTab () {
 	
     // create the Logging pane
     logTab = new JPanel();
     
     // create column 1 - Logging Output
     logOutputLabel = new JLabel ("Log Destination", JLabel.LEFT);
     logOutputLabel.setBorder (etched);
 
     logOutputGroup = new ButtonGroup();
   
     logOutputNoneButton = new JRadioButton (LogJuggler.LOG_NONE_STRING);
     logOutputNoneButton.setActionCommand (LogJuggler.LOG_NONE_STRING);
     logOutputGroup.add (logOutputNoneButton);
     logOutputNoneButton.addActionListener  (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           logJuggler.setLog (event.getActionCommand());
 		    } // end ActionPerformed method
 		  } // end action listener
 		);
     
     logOutputWindowButton = new JRadioButton (LogJuggler.LOG_WINDOW_STRING);
     logOutputWindowButton.setActionCommand (LogJuggler.LOG_WINDOW_STRING);
     logOutputGroup.add (logOutputWindowButton);
     logOutputWindowButton.addActionListener  (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           logJuggler.setLog (event.getActionCommand());
 		    } // end ActionPerformed method
 		  } // end action listener
 		);
 		
     logOutputTextButton = new JRadioButton (LogJuggler.LOG_TEXT_STRING);
     logOutputTextButton.setActionCommand (LogJuggler.LOG_TEXT_STRING);
     logOutputTextButton.setSelected (true);
     // logJuggler.setLog (LogJuggler.LOG_TEXT_STRING);
     logOutputGroup.add (logOutputTextButton);
     logOutputTextButton.addActionListener  (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           logJuggler.setLog (event.getActionCommand());
 		    } // end ActionPerformed method
 		  } // end action listener
 		);
     
     logOutputDiskButton = new JRadioButton (LogJuggler.LOG_DISK_STRING);
     logOutputDiskButton.setActionCommand (LogJuggler.LOG_DISK_STRING);
     logOutputGroup.add (logOutputDiskButton);
     logOutputDiskButton.addActionListener  (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           logJuggler.setLog (event.getActionCommand());
 		    } // end ActionPerformed method
 		  } // end action listener
 		);
             
     // create column 2 - Logging Threshold
     logThresholdLabel = new JLabel ("Logging Threshold", JLabel.LEFT);
     logThresholdLabel.setBorder (etched);
   
     logThresholdGroup = new ButtonGroup();
 
     logThresholdNormalButton = new JRadioButton (LOG_NORMAL_STRING);
     logThresholdNormalButton.setActionCommand (LOG_NORMAL_STRING);
     logThresholdNormalButton.setSelected (true);
     logThresholdGroup.add (logThresholdNormalButton);
     logThresholdNormalButton.addActionListener (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           log.setLogThreshold (LogEvent.NORMAL);
 		    } // end ActionPerformed method
 		  } // end action listener
 		);
     
     logThresholdMinorButton = new JRadioButton (LOG_MINOR_STRING);
     logThresholdMinorButton.setActionCommand (LOG_MINOR_STRING);
     // log.setLogThreshold (LogEvent.MINOR);
     logThresholdGroup.add (logThresholdMinorButton);
     logThresholdMinorButton.addActionListener (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           log.setLogThreshold (LogEvent.MINOR);
 		    } // end ActionPerformed method
 		  } // end action listener
 		);
     
     logThresholdMediumButton = new JRadioButton (LOG_MEDIUM_STRING);
     logThresholdMediumButton.setActionCommand (LOG_MEDIUM_STRING);
     logThresholdGroup.add (logThresholdMediumButton);
     logThresholdMediumButton.addActionListener (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           log.setLogThreshold (LogEvent.MEDIUM);
 		    } // end ActionPerformed method
 		  } // end action listener
 		);
     
     logThresholdMajorButton = new JRadioButton (LOG_MAJOR_STRING);
     logThresholdMajorButton.setActionCommand (LOG_MAJOR_STRING);
     logThresholdGroup.add (logThresholdMajorButton);
     logThresholdMajorButton.addActionListener (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           log.setLogThreshold (LogEvent.MAJOR);
 		    } // end ActionPerformed method
 		  } // end action listener
 		);
     
     // create column 3 - Log All Data? 
     logAllDataLabel = new JLabel ("Data Logging", JLabel.LEFT);
     logAllDataLabel.setBorder (etched);
     
     logAllDataCkBox = new JCheckBox ("Log All Data?");
     logAllDataCkBox.setSelected (false);
     // log.setLogAllData (false);
     logAllDataCkBox.addItemListener (new ItemListener()
 		  {
 		    public void itemStateChanged (ItemEvent event) {
           boolean logAllData = true;
           if (event.getStateChange() == ItemEvent.DESELECTED) {
             logAllData = false;
           }
           log.setLogAllData (logAllData);
 		    } // end itemStateChanged method
 		  } // end action listener
 		);
 		
 		// Bottom of Screen
 		logText = new JTextArea("");
 		logText.setLineWrap (true);
 		logText.setEditable (false);
 		logText.setWrapStyleWord (true);
 		logText.setVisible (true);
     
     // Finish up the Logging Pane
 		gb.startLayout (logTab, 3, 6);
 		gb.setByRows (false);
 		gb.setAllInsets (0);
 		gb.setDefaultRowWeight (0.0);
 		
 		// Column 1
 		gb.add (logOutputLabel);
     gb.add (logOutputNoneButton);
     gb.add (logOutputWindowButton);
 		gb.add (logOutputTextButton);
     gb.add (logOutputDiskButton);
     
     // Column 2
     gb.nextColumn();
     gb.add (logThresholdLabel);
     gb.add (logThresholdNormalButton);
     gb.add (logThresholdMinorButton);
     gb.add (logThresholdMediumButton);
     gb.add (logThresholdMajorButton);
     
     // Column 3
     gb.nextColumn();
     gb.add (logAllDataLabel);
     gb.add (logAllDataCkBox);
     
     // Bottom of Panel
 		logTextScrollPane = new JScrollPane(logText, 
 		  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 		  JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		logTextScrollPane.setVisible(true);
 		
     gb.setColumn (0);
     gb.setRow (5);
     gb.setWidth (3);
     gb.setRowWeight (1.0);
     gb.add (logTextScrollPane);
 
     tabs.addTab("Logging",logTab);
 		
     if (mainClass) {
       logJuggler.setTextArea (logText);
       logJuggler.setLog (LogJuggler.LOG_TEXT_STRING);
       logout = logJuggler.getLog();
     }
 
 	} // end addLoggingTab method
   
   public void handleOpenApplication () {
     // do nothing
   }
   
   public void handleOpenApplication (File inFile) {
     handleOpenFile (inFile);
   }
   
   /**
      Standard way to respond to an About Menu Item Selection on a Mac.
    */
   public void handleAbout() {
     WindowMenuManager.getShared().locateUpperLeftAndMakeVisible
         (mainFrame, aboutWindow);
   }
   
   /**
     Standard way to respond to a document being passed to this 
     application on a Mac.
    */
   public void handleOpenFile (File inFile) {
     FileName inFileName = new FileName (inFile);
     String inFileNameExt = inFileName.getExt();
     if (inFileNameExt.equals (TextMergeScript.SCRIPT_EXT)) {
       textMergeScript.setNormalizerPath(currentDirectory.getPath());
       textMergeScript.playScript(inFile);
       textMergeScript.selectTab();
     } else
     if (inFileNameExt.equals ("txt")
         || inFileNameExt.equals ("tab")
         || inFileNameExt.equals ("csv")) {
       textMergeInput.openFileOrDirectory(inFile);
       textMergeInput.selectTab();
     }
   }
   
   public void handleOpenFile (PSFile inFile) {
     
     if (inFile.exists()
         && inFile.canRead()
         && inFile.isFile()) {
       FileName inFileName = new FileName (inFile);
       String inFileNameExt = inFileName.getExt();
       if (inFileNameExt.equals (TextMergeScript.SCRIPT_EXT)) {
         textMergeScript.setNormalizerPath(currentDirectory.getPath());
         textMergeScript.playScript(inFile);
         textMergeScript.selectTab();
       } else
       if (inFileNameExt.equals ("txt")
           || inFileNameExt.equals ("tab")
           || inFileNameExt.equals ("csv")) {
         textMergeInput.openFileOrDirectory(inFile);
         textMergeInput.selectTab();
       }
     } else {
       JOptionPane.showMessageDialog (tabs, 
         "Recent Script File " + inFile.toString() + " is not available",
         "Script File Error",
         JOptionPane.ERROR_MESSAGE);
     }
   }
   
   public void handleOpenURI (URI inURI) {
     
   }
 
   public boolean preferencesAvailable() {
     return false;
   }
   
   public void handlePreferences () {
     
   }
   
   public void handlePrintFile (File printFile) {
     
   }
 
   /**
      Standard way to respond to a Quit Menu Item on a Mac.
    */
   public void handleQuit() {	
     textMergeScript.stopScriptRecording();
     if (mainClass) {
       textMergeTemplate.savePrefs();
       userPrefs.setPref (UserPrefs.LEFT, mainFrame.getX());
       userPrefs.setPref (UserPrefs.TOP, mainFrame.getY());
       userPrefs.setPref (UserPrefs.WIDTH, mainFrame.getWidth());
       userPrefs.setPref (UserPrefs.HEIGHT, mainFrame.getHeight());
       userPrefs.savePrefs();
     }
     if (mainClass) {
       System.exit(0);
     }
   }
   
   /**
      Set preferred widths for table columns.
    */
   private void setTableModels() {
     DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
     TableColumn tc = null;
     for (int i = 0; i < numberOfFields; i++) {
       tc = new TableColumn(i);
       tc.setHeaderValue(list.getRecDef().getDef(i).getProperName());
       columnModel.addColumn(tc);
     }
     tabTable.setColumnModel(columnModel);
     int avg = 0;
     int max = 0;
     int pwc = 0;
     int pw = 0;
     for (int i = 0; i < numberOfFields; i++) { 
       avg = list.getRecDef().getAverageLength (i);
       max = list.getRecDef().getMaximumLength (i);
       pwc = avg * 3 / 2;
       if (pwc > max) {
         pwc = max;
       }
       if ((max - pwc) <= 2) {
         pwc = max;
       }
       if (pwc < 5) {
         pwc = 5;
       }
       pw = pwc * 9;
       tc = tabTable.getColumnModel().getColumn(i);
       tc.setPreferredWidth (pw);
     }
   }
   
   private void setCurrentDirectoryFromFile (File inFile) {
     currentDirectory = new File (inFile.getParent());
   }
   
   private void setCurrentDirectoryFromDir (File inFile) {
     currentDirectory = inFile;
   }
   
   /**
    Indicate whether or not a list has been loaded. 
   
    @param listAvailable True if a list has been loaded, false if the list
                         is not available. 
   */
   public void setListAvailable (boolean listAvailable) {
    listAvailable = listAvailable;
     if (textMergeOutput != null) {
       textMergeOutput.setListAvailable(listAvailable);
     }
     if (listAvailable) {
       // dataTable = new DataTable (filteredDataSet);
       numberOfFields = list.getRecDef().getNumberOfFields();
       if (tabTableBuilt) {
         tabTable.setModel (list);
         tabNameLabel.setText (textMergeInput.getFileNameToDisplay());
         setTableModels();
       }
       if (sortTabBuilt) {
         textMergeSort.setPSList(list);
       }
       if (filterTabBuilt) {
         textMergeFilter.setPSList(list);
       }
       if (templateTabBuilt) {
         textMergeTemplate.setPSList(list);
       }
     }
 
   }
   
   /**
    Indicate whether or not a list has been loaded. 
   
    @return True if a list has been loaded, false if the list is not 
            available. 
   */
   public boolean isListAvailable() {
     return listAvailable;
   }
 
 } // end PSTextMerge class
