 /*
  * Copyright 2003 - 2013 Herb Bowie
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
 
 package com.powersurgepub.iwisdom;
 
   import com.powersurgepub.psfiles.*;
   import com.powersurgepub.psdatalib.ui.*;
   import com.powersurgepub.iwisdom.data.*;
   import com.powersurgepub.iwisdom.disk.*;
   import com.powersurgepub.psutils.*;
   import com.powersurgepub.xos2.*;
   import java.awt.*;
   import java.awt.event.*;
   import java.io.*;
   import java.net.*;
   import javax.swing.*;
   import java.text.*;
   import java.util.*;
 
 /**
    A computer program for managing quotations and other small bits
    of wisdom. 
  */
 
 public class iWisdom 
     extends javax.swing.JFrame 
       implements 
         com.powersurgepub.iwisdom.disk.FileOpener, 
         XHandler 
           {
   
   public static final String PROGRAM_NAME = "iWisdom";
   public static final String PROGRAM_VERSION = "2.50";
   
   public static final int    CHILD_WINDOW_X_OFFSET = 60;
   public static final int    CHILD_WINDOW_Y_OFFSET = 60;
 
   private             Appster appster;
 
   private             String  country = "  ";
   private             String  language = "  ";;
         
   XOS                 xos        = XOS.getShared();
         
   Logger              logger     = Logger.getShared();
 
   private             Home home;
   private             UserPrefs userPrefs;
         
   private static final String PROGRAM_HISTORY  
       = "versions.html";
   private static final String USER_GUIDE  
       = "iwisdom.html";
   private static final String HOME_PAGE   
       = "http://www.powersurgepub.com/products/iwisdom.html";
   private static final String WISDOM_SOURCES
       = "http://www.powersurgepub.com/products/iwisdom/wisdom.html";
   public  static final String FIND = "Find";
   public  static final String FIND_AGAIN = "Again";
   // private String              userGuide;
   private URL                 userGuideURL;
   private URL                 quickStartURL;
   private URL                 programHistoryURL;
   private int                 shortcutKeyMask;
   
   private iWisdomCommon   td;
   
   private DateFormat    longDateFormatter 
       = new SimpleDateFormat ("EEEE MMMM d, yyyy");
   
   // Hand-tailored GUI Elements
   private JMenu knownFilesMenu;
   
   private File startingFile;
   private boolean fileOpenedByHandler = false;
   private ProgramVersion      programVersion;
   private String              lastTextFound = "";
   
   private             StatusBar           statusBar = new StatusBar();
   
   // Debugging
   private int printCount = 0;
   
   /**
    * 
    *    Creates new form iWisdom.
    */
   public iWisdom() {
     
     // Perform Platform-Specific Initialization
     appster = new Appster
         ("powersurgepub", "com", 
           PROGRAM_NAME, PROGRAM_VERSION, 
           language, country,
           this, this);
     home = Home.getShared ();
     userPrefs = UserPrefs.getShared();
     programVersion = ProgramVersion.getShared ();
     
     initComponents();
 
     WindowMenuManager.getShared(windowMenu);
     
     td = new iWisdomCommon (this, tabs, tabs2, mainSplitPanel, itemOkButton);
     
     getContentPane().add(statusBar, java.awt.BorderLayout.SOUTH);
 
     try {
       userGuideURL = new URL (td.pageURL, USER_GUIDE);
     } catch (MalformedURLException e) {
     }
     try {
       quickStartURL = new URL (td.pageURL, td.QUICK_START);
     } catch (MalformedURLException e) {
     }
     try {
       programHistoryURL = new URL (td.pageURL, PROGRAM_HISTORY);
     } catch (MalformedURLException e) {
     }
     // userGuide = "file:/" + System.getProperty (GlobalConstants.USER_DIR) + USER_GUIDE;
     shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
     
     // navToolBar.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
     // initComponents();
     setBounds (
         td.userPrefs.getPrefAsInt (td.LEFT, 100),
         td.userPrefs.getPrefAsInt (td.TOP,  100),
         td.userPrefs.getPrefAsInt (td.WIDTH, 620),
         td.userPrefs.getPrefAsInt (td.HEIGHT, 540));
     CommonPrefs.getShared().setSplitPane(mainSplitPanel);
     CommonPrefs.getShared().setMainWindow(this);
     xos.setHelpMenuItem (helpUserGuideMenuItem);
     
     // td.tabs = tabs;
     td.files = new WisdomDiskDirectory(this, Logger.getShared());
     td.views = td.files.getViews();
     td.views.setCommon (td);
 
     td.editMenu = editMenu;
     td.initComponents();
     td.setStatusBar(statusBar);
     td.longDateFormatter = longDateFormatter;
     // Set Open Known Menu Items
     
     fileMenu.add (td.files.getRecentFilesMenu()); 
     td.prefsWindow.getViewPrefs().setMenu (viewMenu);
     // Localizer.getShared().localize(mainMenuBar);
     
     // Open default disk store for user if one exists
     if (fileOpenedByHandler) {
       // do nothing -- file already opened
     }
     else
     if (startingFile != null) {
       handleOpenFile (startingFile);
     } else {
       td.fileOpenDefault();
     }
     
     // Set About, Quit and other Handlers in platform-specific ways
     xos.setFileMenu (fileMenu);
     xos.setHelpMenu (helpMenu);
     xos.setXHandler (this);
     xos.setMainWindow (this);
     xos.enablePreferences();
     
     CommonPrefs.getShared().appLaunch();
     // findText.grabFocus();
     td.initComplete = true;
   }
 
   public boolean preferencesAvailable() {
     return true;
   }
   
   public void logSysProps () {
     Properties p = System.getProperties();
     Enumeration seq = p.propertyNames();
     StringBuffer sysPropLine = new StringBuffer();
     while (seq.hasMoreElements()) {
       String propName = (String)seq.nextElement();
       String property = p.getProperty(propName);
       sysPropLine = new StringBuffer();
       if (propName.equals ("line.separator")) {
         sysPropLine.append (propName + ": ");
         for (int i = 0; i < property.length(); i++) {
           if (i > 0) {
             sysPropLine.append ("/");
           }
           if (property.charAt(i) == GlobalConstants.CARRIAGE_RETURN) {
             sysPropLine.append ("CR (ASCII 13)");
           } else
           if (property.charAt(i) == GlobalConstants.LINE_FEED) {
             sysPropLine.append ("LF (ASCII 10)");
           } else {
             sysPropLine.append (String.valueOf (property.charAt(i)));
           }
         }
       } else {
         sysPropLine.append (propName + ": " + property);
       }
       logger.recordEvent (LogEvent.NORMAL, 
           sysPropLine.toString(),
           false);
     }  // end while more system properties
   } // end method
   
   public void handleOpenApplication (File inFile) {
     logger.recordEvent (LogEvent.NORMAL, 
         "Open Application Handler Invoked with file " + inFile.toString(),
         false);
     td.fileOpen (inFile);
   }
   
   public void handleOpenApplication() {
       
   }
   
   /**      
     Standard way to respond to a document being passed to this application on a Mac.
    
     @param inFile File to be processed by this application, generally
                   as a result of a file or directory being dragged
                   onto the application icon.
    */
   public void handleOpenFile (File inFile) {
     logger.recordEvent (LogEvent.NORMAL, 
         "Open File Handler Invoked with file " + inFile.toString(),
         false);
     fileOpenedByHandler = true;
     td.fileImport (inFile);
   }
   
   /**      
     Opens a file and sets its view options.
    
     @param inFile File to be opened.
     @param inView View options to be used with this file.
    
   public void handleOpenFile (File inFile, String inView) {
     td.fileOpen (inFile, inView, null, 0);
   }
    */
   
   /**      
     Opens a file and sets its view options and Web template.
    
     @param inFile File to be opened.
     @param inView View options to be used with this file.
     @param inTemplate Template file to be used for Web publishing.
    
   public void handleOpenFile (File inFile, String inView, File inTemplate) {
     td.fileOpen (inFile, inView, inTemplate, 0);
   }
    */
   
   /**      
     Opens a file and sets its view options and Web template 
     and publishing frequence.
    
     @param inFile File to be opened.
     @param inView View options to be used with this file.
     @param inTemplate Template file to be used for Web publishing.
     @param inPublishWhen Indicator of how often file should be published.
    
   public void handleOpenFile (File inFile, String inView, 
       File inTemplate, int inPublishWhen) {
     td.fileOpen (inFile, inView, inTemplate, inPublishWhen);
   }
    */
   
   /**      
     Standard way to respond to a document being passed to this application on a Mac.
    
     @param inFStore Disk store to be processed by this application.
    */
   public void handleOpenFile (WisdomDiskStore inStore) {
     td.fileOpen (inStore);
   }
 
   public void handleOpenURI (URI inURI) {
     
   }
   
   /**
    Standard way to respond to a print request. 
    */
   public void handlePrintFile (File printFile) {
     // not supported
   }
   
   /**
      We're out of here!
    */
   public void handleQuit() {
 
     td.modIfChanged();
     td.fileClose();
     if (td.diskStore != null
         && (! td.diskStore.isUnknown())) {
       // td.diskStore.setTemplate (td.webTab.getTemplate());
       td.rememberLastFile ();
     }
     td.files.saveToDisk();
     
     td.userPrefs.setPref (td.LEFT, td.frame.getX());
     td.userPrefs.setPref (td.TOP, td.frame.getY());
     td.userPrefs.setPref (td.WIDTH, td.frame.getWidth());
     td.userPrefs.setPref (td.HEIGHT, td.frame.getHeight());
     td.userPrefs.setPref 
         (td.DIVIDER_LOCATION, mainSplitPanel.getDividerLocation());
     
     td.savePrefs();
     
     boolean prefsOK = td.userPrefs.savePrefs();
 
     System.exit(0);
   }
   
   private void findItem () {
     String findString = findText.getText();
     if (findString != null) {
       if (findString.length() > 0) {
         if (findButton.getText().equals (FIND)) {
           td.findItem (findString);
           findButton.setText(FIND_AGAIN);
           lastTextFound = findString;
         } else {
           td.findAgain();
         } // end if not doing initial find
       } else {
         findText.grabFocus();
       }
     } else {
       findText.grabFocus();
     }
   }
   
   private void findAgain () {
     td.findAgain();
   }
   
   private void newItem() {
     // Capture current category selection, if any
     td.saveSelectedTags();
     td.modIfChanged();
     td.newItem();
     td.displayItem();
     td.activateItemTab();
   }
   
   private void deleteItem() {
 
     if (! td.newItem) {
       WisdomItem itemToDelete = td.getItem();
       boolean okToDelete = true;
       if (CommonPrefs.getShared().confirmDeletes()) {
         int userOption = JOptionPane.showConfirmDialog(tabs2, "Really delete item "
             + itemToDelete.getTitle() + "?",
             "Delete Confirmation",
             JOptionPane.YES_NO_OPTION,
             JOptionPane.QUESTION_MESSAGE);
         okToDelete = (userOption == JOptionPane.YES_OPTION);
       }
       if (okToDelete) {
         td.navigator.nextItem();
         WisdomItem nextItem = td.getItem();
         td.setItem(itemToDelete);
         td.items.remove (itemToDelete);
         td.setUnsavedChanges (true);
         td.setItem(nextItem);
         td.displayItem();
       } // end if user confirmed delete
     } // end if new item not yet saved
   } // end method
   
   /**
      Standard way to respond to an About Menu Item Selection on a Mac.
    */
   public void handleAbout() {
     td.displayAbout();
   }
   
   /**
      Standard way to respond to a Preferences Item Selection on a Mac.
    */
   public void handlePreferences() {
     td.displayPrefs();
   }  
   
   private void replaceCategory() {
     
     td.modIfChanged();
     CatChangeScreen catScreen = new CatChangeScreen 
         (this, true, td);
     catScreen.setLocation (
         this.getX() + CHILD_WINDOW_X_OFFSET, 
         this.getY() + CHILD_WINDOW_Y_OFFSET);
     catScreen.setVisible (true);
     // catScreen.show();
   }
   
   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {
 
     navToolBar = new javax.swing.JToolBar();
     itemOkButton = new javax.swing.JButton();
     itemNewButton = new javax.swing.JButton();
     itemDeleteButton = new javax.swing.JButton();
     itemFirstButton = new javax.swing.JButton();
     itemPriorButton = new javax.swing.JButton();
     itemNextButton = new javax.swing.JButton();
     itemLastButton = new javax.swing.JButton();
     itemAddQuotesButton = new javax.swing.JButton();
     itemRemoveQuotesButton = new javax.swing.JButton();
     rotateButton = new javax.swing.JButton();
     findText = new javax.swing.JTextField();
     findButton = new javax.swing.JButton();
     mainSplitPanel = new javax.swing.JSplitPane();
     tabs = new javax.swing.JTabbedPane();
     tabs2 = new javax.swing.JTabbedPane();
     mainMenuBar = new javax.swing.JMenuBar();
     fileMenu = new javax.swing.JMenu();
     fileNewMenuItem = new javax.swing.JMenuItem();
     fileOpenMenuItem = new javax.swing.JMenuItem();
     filePropertiesMenuItem = new javax.swing.JMenuItem();
     jSeparator1 = new javax.swing.JSeparator();
     fileImportMenuItem = new javax.swing.JMenuItem();
     fileImportWikiQuoteMenuItem = new javax.swing.JMenuItem();
     fileExportMenuItem = new javax.swing.JMenuItem();
     publishMenuItem = new javax.swing.JMenuItem();
     publishNowMenuItem = new javax.swing.JMenuItem();
     cleanUpMenuItem = new javax.swing.JMenuItem();
     jSeparator2 = new javax.swing.JSeparator();
     fileBackupMenuItem = new javax.swing.JMenuItem();
     fileRevertMenuItem = new javax.swing.JMenuItem();
     jSeparator3 = new javax.swing.JSeparator();
     editMenu = new javax.swing.JMenu();
     listMenu = new javax.swing.JMenu();
     recordFindMenuItem = new javax.swing.JMenuItem();
     recordFindAgainMenuItem = new javax.swing.JMenuItem();
     listReplaceCategoryMenuItem = new javax.swing.JMenuItem();
     listValidateURLs = new javax.swing.JMenuItem();
     listAddQuotesMenuItem = new javax.swing.JMenuItem();
     listRemoveQuotesMenuItem = new javax.swing.JMenuItem();
     recordMenu = new javax.swing.JMenu();
     recordDeleteMenuItem = new javax.swing.JMenuItem();
     recordNewMenuItem = new javax.swing.JMenuItem();
     recordNextMenuItem = new javax.swing.JMenuItem();
     recordPriorMenuItem = new javax.swing.JMenuItem();
     recordCopyPasteMenuSep = new javax.swing.JSeparator();
     recordLastMenuItem = new javax.swing.JMenuItem();
     recordCopyMenuItem = new javax.swing.JMenuItem();
     recordPasteNewMenuItem = new javax.swing.JMenuItem();
     recordDuplicateMenuItem = new javax.swing.JMenuItem();
     recordAddQuotesMenuItem = new javax.swing.JMenuItem();
     recordRemoveQuotesMenuItem = new javax.swing.JMenuItem();
     tabsMenu = new javax.swing.JMenu();
     tabsListMenuItem = new javax.swing.JMenuItem();
     tabsTreeMenuItem = new javax.swing.JMenuItem();
     tabsItemMenuItem = new javax.swing.JMenuItem();
     viewMenu = new javax.swing.JMenu();
     viewByDueDateMenuItem = new javax.swing.JMenuItem();
     viewByPriorityMenuItem = new javax.swing.JMenuItem();
     toolsMenu = new javax.swing.JMenu();
     optionsMenuItem = new javax.swing.JMenuItem();
     windowMenu = new javax.swing.JMenu();
     helpMenu = new javax.swing.JMenu();
     helpHistoryMenuItem = new javax.swing.JMenuItem();
     helpUserGuideMenuItem = new javax.swing.JMenuItem();
     helpWebSeparator = new javax.swing.JSeparator();
     helpSoftwareUpdatesMenuItem = new javax.swing.JMenuItem();
     helpHomePageMenuItem = new javax.swing.JMenuItem();
     submitFeedbackMenuItem = new javax.swing.JMenuItem();
     helpWisdomSourcesMenuItem = new javax.swing.JMenuItem();
     helpAppSeparator = new javax.swing.JSeparator();
     helpReduceWindowSizeMenuItem = new javax.swing.JMenuItem();
 
     setTitle("iWisdom");
     addWindowListener(new java.awt.event.WindowAdapter() {
       public void windowClosing(java.awt.event.WindowEvent evt) {
         exitForm(evt);
       }
     });
 
     itemOkButton.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
     itemOkButton.setText("OK");
     itemOkButton.setToolTipText("Add new piece of wisdom");
     itemOkButton.setFocusable(false);
     itemOkButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
     itemOkButton.setMargin(new java.awt.Insets(0, 4, 4, 4));
     itemOkButton.setMaximumSize(new java.awt.Dimension(60, 30));
     itemOkButton.setMinimumSize(new java.awt.Dimension(30, 26));
     itemOkButton.setPreferredSize(new java.awt.Dimension(40, 28));
     itemOkButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
     itemOkButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         itemOkButtonActionPerformed(evt);
       }
     });
     navToolBar.add(itemOkButton);
 
     itemNewButton.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
     itemNewButton.setText("+");
     itemNewButton.setToolTipText("Add new piece of wisdom");
     itemNewButton.setMargin(new java.awt.Insets(0, 4, 4, 4));
     itemNewButton.setMaximumSize(new java.awt.Dimension(60, 30));
     itemNewButton.setMinimumSize(new java.awt.Dimension(30, 26));
     itemNewButton.setPreferredSize(new java.awt.Dimension(40, 28));
     itemNewButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         itemNewButtonActionPerformed(evt);
       }
     });
     navToolBar.add(itemNewButton);
 
     itemDeleteButton.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
     itemDeleteButton.setText("-");
     itemDeleteButton.setToolTipText("Delete this piece of wisdom");
     itemDeleteButton.setMargin(new java.awt.Insets(0, 4, 4, 4));
     itemDeleteButton.setMaximumSize(new java.awt.Dimension(60, 30));
     itemDeleteButton.setMinimumSize(new java.awt.Dimension(30, 26));
     itemDeleteButton.setPreferredSize(new java.awt.Dimension(40, 28));
     itemDeleteButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         itemDeleteButtonActionPerformed(evt);
       }
     });
     navToolBar.add(itemDeleteButton);
 
     itemFirstButton.setText("<<");
     itemFirstButton.setToolTipText("Return to beginning of list");
     itemFirstButton.setMaximumSize(new java.awt.Dimension(60, 30));
     itemFirstButton.setMinimumSize(new java.awt.Dimension(30, 26));
     itemFirstButton.setPreferredSize(new java.awt.Dimension(40, 28));
     itemFirstButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         itemFirstButtonAction(evt);
       }
     });
     navToolBar.add(itemFirstButton);
 
     itemPriorButton.setText("<");
     itemPriorButton.setToolTipText("Return to prior piece of wisdom");
     itemPriorButton.setMaximumSize(new java.awt.Dimension(60, 30));
     itemPriorButton.setMinimumSize(new java.awt.Dimension(30, 26));
     itemPriorButton.setPreferredSize(new java.awt.Dimension(40, 28));
     itemPriorButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         itemPriorButtonAction(evt);
       }
     });
     navToolBar.add(itemPriorButton);
 
     itemNextButton.setText(">");
     itemNextButton.setToolTipText("Advance to next piece of wisdom");
     itemNextButton.setMaximumSize(new java.awt.Dimension(60, 30));
     itemNextButton.setMinimumSize(new java.awt.Dimension(30, 26));
     itemNextButton.setPreferredSize(new java.awt.Dimension(40, 28));
     itemNextButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         itemNextButtonAction(evt);
       }
     });
     navToolBar.add(itemNextButton);
 
     itemLastButton.setText(">>");
     itemLastButton.setToolTipText("Go to end of list");
     itemLastButton.setMaximumSize(new java.awt.Dimension(60, 30));
     itemLastButton.setMinimumSize(new java.awt.Dimension(30, 26));
     itemLastButton.setPreferredSize(new java.awt.Dimension(40, 28));
     itemLastButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         itemLastButtonAction(evt);
       }
     });
     navToolBar.add(itemLastButton);
 
     itemAddQuotesButton.setText("Add \"");
     itemAddQuotesButton.setToolTipText("Add quotation marks");
     itemAddQuotesButton.setMaximumSize(new java.awt.Dimension(72, 30));
     itemAddQuotesButton.setMinimumSize(new java.awt.Dimension(48, 26));
     itemAddQuotesButton.setPreferredSize(new java.awt.Dimension(60, 28));
     itemAddQuotesButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         itemAddQuotesButtonActionPerformed(evt);
       }
     });
     navToolBar.add(itemAddQuotesButton);
 
     itemRemoveQuotesButton.setText("Remove \"");
     itemRemoveQuotesButton.setToolTipText("Remove quotation marks");
     itemRemoveQuotesButton.setMaximumSize(new java.awt.Dimension(72, 30));
     itemRemoveQuotesButton.setMinimumSize(new java.awt.Dimension(48, 26));
     itemRemoveQuotesButton.setPreferredSize(new java.awt.Dimension(72, 28));
     itemRemoveQuotesButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         itemRemoveQuotesButtonActionPerformed(evt);
       }
     });
     navToolBar.add(itemRemoveQuotesButton);
 
     rotateButton.setText("Scan");
     rotateButton.setToolTipText("Scan through all wisdoms items at a specified pace");
     rotateButton.setMaximumSize(new java.awt.Dimension(72, 30));
     rotateButton.setMinimumSize(new java.awt.Dimension(48, 26));
     rotateButton.setPreferredSize(new java.awt.Dimension(60, 28));
     rotateButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         rotateButtonActionPerformed(evt);
       }
     });
     navToolBar.add(rotateButton);
 
     findText.setMaximumSize(new java.awt.Dimension(240, 30));
     findText.setMinimumSize(new java.awt.Dimension(40, 26));
     findText.setPreferredSize(new java.awt.Dimension(120, 28));
     findText.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         findTextActionPerformed(evt);
       }
     });
     findText.addKeyListener(new java.awt.event.KeyAdapter() {
       public void keyTyped(java.awt.event.KeyEvent evt) {
         findTextKeyTyped(evt);
       }
     });
     navToolBar.add(findText);
 
     findButton.setText("Find");
     findButton.setToolTipText("Search for the text entered to the left");
     findButton.setMaximumSize(new java.awt.Dimension(72, 30));
     findButton.setMinimumSize(new java.awt.Dimension(48, 26));
     findButton.setPreferredSize(new java.awt.Dimension(60, 28));
     findButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         findButtonActionPerformed(evt);
       }
     });
     navToolBar.add(findButton);
 
     getContentPane().add(navToolBar, java.awt.BorderLayout.NORTH);
 
     mainSplitPanel.setResizeWeight(0.5);
     mainSplitPanel.setContinuousLayout(true);
 
     tabs.setMinimumSize(new java.awt.Dimension(80, 80));
     tabs.setPreferredSize(new java.awt.Dimension(339, 300));
     mainSplitPanel.setLeftComponent(tabs);
     mainSplitPanel.setRightComponent(tabs2);
 
     getContentPane().add(mainSplitPanel, java.awt.BorderLayout.CENTER);
 
     fileMenu.setText("File");
 
     fileNewMenuItem.setText("New");
     fileNewMenuItem.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         fileNewMenuItemActionPerformed(evt);
       }
     });
     fileMenu.add(fileNewMenuItem);
 
     fileOpenMenuItem.setAccelerator(KeyStroke.getKeyStroke (KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
     fileOpenMenuItem.setText("Open...");
     fileOpenMenuItem.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         fileOpenMenuItemActionPerformed(evt);
       }
     });
     fileMenu.add(fileOpenMenuItem);
 
     filePropertiesMenuItem.setAccelerator(KeyStroke.getKeyStroke (KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
     filePropertiesMenuItem.setText("Get Info");
     filePropertiesMenuItem.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         filePropertiesMenuItemActionPerformed(evt);
       }
     });
     fileMenu.add(filePropertiesMenuItem);
     fileMenu.add(jSeparator1);
 
     fileImportMenuItem.setText("Import...");
     fileImportMenuItem.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         fileImportMenuItemActionPerformed(evt);
       }
     });
     fileMenu.add(fileImportMenuItem);
 
     fileImportWikiQuoteMenuItem.setText("Import from WikiQuote...");
     fileImportWikiQuoteMenuItem.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         fileImportWikiQuoteMenuItemActionPerformed(evt);
       }
     });
     fileMenu.add(fileImportWikiQuoteMenuItem);
 
     fileExportMenuItem.setAccelerator(KeyStroke.getKeyStroke (KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
     fileExportMenuItem.setText("Export...");
     fileExportMenuItem.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         fileExportMenuItemActionPerformed(evt);
       }
     });
     fileMenu.add(fileExportMenuItem);
 
     publishMenuItem.setAccelerator(KeyStroke.getKeyStroke (KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
     publishMenuItem.setText("Publish...");
     recordPasteNewMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_P,
       Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
   publishMenuItem.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       publishMenuItemActionPerformed(evt);
     }
   });
   fileMenu.add(publishMenuItem);
 
   publishNowMenuItem.setText("Publish Now");
   publishNowMenuItem.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       publishNowMenuItemActionPerformed(evt);
     }
   });
   fileMenu.add(publishNowMenuItem);
 
   cleanUpMenuItem.setText("Clean up...");
   cleanUpMenuItem.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       cleanUpMenuItemActionPerformed(evt);
     }
   });
   fileMenu.add(cleanUpMenuItem);
   fileMenu.add(jSeparator2);
 
   fileBackupMenuItem.setAccelerator(KeyStroke.getKeyStroke (KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
   fileBackupMenuItem.setText("Backup...");
   fileBackupMenuItem.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       fileBackupMenuItemActionPerformed(evt);
     }
   });
   fileMenu.add(fileBackupMenuItem);
 
   fileRevertMenuItem.setText("Revert to backup...");
   fileRevertMenuItem.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       fileRevertMenuItemActionPerformed(evt);
     }
   });
   fileMenu.add(fileRevertMenuItem);
   fileMenu.add(jSeparator3);
 
   mainMenuBar.add(fileMenu);
 
   editMenu.setText("Edit");
   mainMenuBar.add(editMenu);
 
   listMenu.setText("List");
   listMenu.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       listMenuActionPerformed(evt);
     }
   });
 
   recordFindMenuItem.setText("Find");
   recordFindMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_F,
     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 recordFindMenuItem.addActionListener(new java.awt.event.ActionListener() {
   public void actionPerformed(java.awt.event.ActionEvent evt) {
     recordFindMenuItemActionPerformed(evt);
   }
   });
   listMenu.add(recordFindMenuItem);
 
   recordFindAgainMenuItem.setText("Find Again");
   recordFindAgainMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_G,
     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 recordFindAgainMenuItem.addActionListener(new java.awt.event.ActionListener() {
   public void actionPerformed(java.awt.event.ActionEvent evt) {
     recordFindAgainMenuItemActionPerformed(evt);
   }
   });
   listMenu.add(recordFindAgainMenuItem);
 
   listReplaceCategoryMenuItem.setText("Add/Replace Category...");
   recordFindAgainMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_G,
     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 listReplaceCategoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
   public void actionPerformed(java.awt.event.ActionEvent evt) {
     listReplaceCategoryMenuItemActionPerformed(evt);
   }
   });
   listMenu.add(listReplaceCategoryMenuItem);
 
   listValidateURLs.setText("Validate Web Pages...");
   listValidateURLs.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       listValidateURLsActionPerformed(evt);
     }
   });
   listMenu.add(listValidateURLs);
 
   listAddQuotesMenuItem.setText("Add Quotes to All");
   listAddQuotesMenuItem.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       listAddQuotesMenuItemActionPerformed(evt);
     }
   });
   listMenu.add(listAddQuotesMenuItem);
 
   listRemoveQuotesMenuItem.setText("Remove Quotes from All");
   listRemoveQuotesMenuItem.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       listRemoveQuotesMenuItemActionPerformed(evt);
     }
   });
   listMenu.add(listRemoveQuotesMenuItem);
 
   mainMenuBar.add(listMenu);
 
   recordMenu.setText("Item");
 
   recordDeleteMenuItem.setAccelerator(KeyStroke.getKeyStroke (KeyEvent.VK_D,
     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 recordDeleteMenuItem.setText("Delete");
 recordDeleteMenuItem.addActionListener(new java.awt.event.ActionListener() {
   public void actionPerformed(java.awt.event.ActionEvent evt) {
     recordDeleteMenuItemActionPerformed(evt);
   }
   });
   recordMenu.add(recordDeleteMenuItem);
 
   recordNewMenuItem.setText("New");
   recordNewMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_N,
     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 recordNewMenuItem.addActionListener(new java.awt.event.ActionListener() {
   public void actionPerformed(java.awt.event.ActionEvent evt) {
     recordNewMenuItemActionPerformed(evt);
   }
   });
   recordMenu.add(recordNewMenuItem);
 
   recordNextMenuItem.setText("Next");
   recordNextMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_CLOSE_BRACKET,
     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 recordNextMenuItem.addActionListener(new java.awt.event.ActionListener() {
   public void actionPerformed(java.awt.event.ActionEvent evt) {
     recordNextMenuItemActionPerformed(evt);
   }
   });
   recordMenu.add(recordNextMenuItem);
 
   recordPriorMenuItem.setText("Prior");
   recordPriorMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_OPEN_BRACKET,
     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 recordPriorMenuItem.addActionListener(new java.awt.event.ActionListener() {
   public void actionPerformed(java.awt.event.ActionEvent evt) {
     recordPriorMenuItemActionPerformed(evt);
   }
   });
   recordMenu.add(recordPriorMenuItem);
   recordMenu.add(recordCopyPasteMenuSep);
 
   recordLastMenuItem.setText("Last Author and Work ");
   recordLastMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_L,
     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 recordLastMenuItem.addActionListener(new java.awt.event.ActionListener() {
   public void actionPerformed(java.awt.event.ActionEvent evt) {
     recordLastMenuItemActionPerformed(evt);
   }
   });
   recordMenu.add(recordLastMenuItem);
 
   recordCopyMenuItem.setText("Transfer");
   recordCopyMenuItem.setToolTipText("Transfer the selected item to the System Clipboard");
   recordCopyMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_T,
     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 recordCopyMenuItem.addActionListener(new java.awt.event.ActionListener() {
   public void actionPerformed(java.awt.event.ActionEvent evt) {
     recordCopyMenuItemActionPerformed(evt);
   }
   });
   recordMenu.add(recordCopyMenuItem);
 
   recordPasteNewMenuItem.setText("Update");
   recordPasteNewMenuItem.setToolTipText("Accept Transferred item(s) from the System Clipboard");
   recordPasteNewMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_U,
     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 recordPasteNewMenuItem.addActionListener(new java.awt.event.ActionListener() {
   public void actionPerformed(java.awt.event.ActionEvent evt) {
     recordPasteNewMenuItemActionPerformed(evt);
   }
   });
   recordMenu.add(recordPasteNewMenuItem);
 
   recordDuplicateMenuItem.setText("Duplicate");
   recordDuplicateMenuItem.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       recordDuplicateMenuItemActionPerformed(evt);
     }
   });
   recordMenu.add(recordDuplicateMenuItem);
 
   recordAddQuotesMenuItem.setText("Add Quotes");
   recordAddQuotesMenuItem.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       recordAddQuotesMenuItemActionPerformed(evt);
     }
   });
   recordMenu.add(recordAddQuotesMenuItem);
 
   recordRemoveQuotesMenuItem.setText("Remove Quotes");
   recordRemoveQuotesMenuItem.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       recordRemoveQuotesMenuItemActionPerformed(evt);
     }
   });
   recordMenu.add(recordRemoveQuotesMenuItem);
 
   mainMenuBar.add(recordMenu);
 
   tabsMenu.setText("Tabs");
 
   tabsListMenuItem.setText("List");
   tabsListMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_1,
     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 tabsListMenuItem.addActionListener(new java.awt.event.ActionListener() {
   public void actionPerformed(java.awt.event.ActionEvent evt) {
     tabsListMenuItemActionPerformed(evt);
   }
   });
   tabsMenu.add(tabsListMenuItem);
 
   tabsTreeMenuItem.setText("Categories");
   tabsTreeMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_2,
     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 tabsTreeMenuItem.addActionListener(new java.awt.event.ActionListener() {
   public void actionPerformed(java.awt.event.ActionEvent evt) {
     tabsTreeMenuItemActionPerformed(evt);
   }
   });
   tabsMenu.add(tabsTreeMenuItem);
 
   tabsItemMenuItem.setText("Edit");
   tabsItemMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_3,
     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 tabsItemMenuItem.addActionListener(new java.awt.event.ActionListener() {
   public void actionPerformed(java.awt.event.ActionEvent evt) {
     tabsItemMenuItemActionPerformed(evt);
   }
   });
   tabsMenu.add(tabsItemMenuItem);
 
   mainMenuBar.add(tabsMenu);
 
   viewMenu.setText("View");
 
   viewByDueDateMenuItem.setText("By Due Date");
   viewMenu.add(viewByDueDateMenuItem);
 
   viewByPriorityMenuItem.setText("By Priority");
   viewMenu.add(viewByPriorityMenuItem);
 
   mainMenuBar.add(viewMenu);
 
   toolsMenu.setText("Tools");
 
   optionsMenuItem.setText("Options");
   optionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       optionsMenuItemActionPerformed(evt);
     }
   });
   toolsMenu.add(optionsMenuItem);
 
   mainMenuBar.add(toolsMenu);
 
   windowMenu.setText("Window");
   mainMenuBar.add(windowMenu);
 
   helpMenu.setText("Help");
 
   helpHistoryMenuItem.setText("Program History");
   helpHistoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       helpHistoryMenuItemActionPerformed(evt);
     }
   });
   helpMenu.add(helpHistoryMenuItem);
 
   helpUserGuideMenuItem.setText("User Guide");
   helpUserGuideMenuItem.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       helpUserGuideMenuItemActionPerformed(evt);
     }
   });
   helpMenu.add(helpUserGuideMenuItem);
   helpMenu.add(helpWebSeparator);
 
   helpSoftwareUpdatesMenuItem.setText("Check for Updates...");
   helpSoftwareUpdatesMenuItem.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       helpSoftwareUpdatesMenuItemActionPerformed(evt);
     }
   });
   helpMenu.add(helpSoftwareUpdatesMenuItem);
 
   helpHomePageMenuItem.setText("iWisdom Home Page");
   helpHomePageMenuItem.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       helpHomePageMenuItemActionPerformed(evt);
     }
   });
   helpMenu.add(helpHomePageMenuItem);
 
   submitFeedbackMenuItem.setText("Submit Feedback");
   submitFeedbackMenuItem.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       submitFeedbackMenuItemActionPerformed(evt);
     }
   });
   helpMenu.add(submitFeedbackMenuItem);
 
   helpWisdomSourcesMenuItem.setText("iWisdom Import Sources");
   helpWisdomSourcesMenuItem.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
       helpWisdomSourcesMenuItemActionPerformed(evt);
     }
   });
   helpMenu.add(helpWisdomSourcesMenuItem);
   helpMenu.add(helpAppSeparator);
 
   helpReduceWindowSizeMenuItem.setText("Reduce Window Size");
   helpReduceWindowSizeMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_W,
     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 helpReduceWindowSizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
   public void actionPerformed(java.awt.event.ActionEvent evt) {
     helpReduceWindowSizeMenuItemActionPerformed(evt);
   }
   });
   helpMenu.add(helpReduceWindowSizeMenuItem);
 
   mainMenuBar.add(helpMenu);
 
   setJMenuBar(mainMenuBar);
 
   setSize(new java.awt.Dimension(675, 550));
   setLocationRelativeTo(null);
   }// </editor-fold>//GEN-END:initComponents
 
   private void fileRevertMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileRevertMenuItemActionPerformed
     td.fileRevert();
   }//GEN-LAST:event_fileRevertMenuItemActionPerformed
 
   private void fileBackupMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileBackupMenuItemActionPerformed
     td.promptForBackup();
   }//GEN-LAST:event_fileBackupMenuItemActionPerformed
 
   private void findTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findTextActionPerformed
     findItem();
   }//GEN-LAST:event_findTextActionPerformed
 
   private void findTextKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_findTextKeyTyped
     if (! findText.getText().equals (lastTextFound)) {
       findButton.setText(FIND);
     }
   }//GEN-LAST:event_findTextKeyTyped
 
   private void findButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findButtonActionPerformed
     findItem();
   }//GEN-LAST:event_findButtonActionPerformed
 
   private void optionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsMenuItemActionPerformed
     handlePreferences();
   }//GEN-LAST:event_optionsMenuItemActionPerformed
 
   private void rotateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateButtonActionPerformed
     if (rotateButton.getText().equals ("Scan")) {
       td.startRotation();
       rotateButton.setText ("End Scan");
     } else {
       td.endRotation();
       rotateButton.setText("Scan");
     }
   }//GEN-LAST:event_rotateButtonActionPerformed
 
   private void filePropertiesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filePropertiesMenuItemActionPerformed
     WindowMenuManager.getShared().makeVisible(td.collectionWindow);
   }//GEN-LAST:event_filePropertiesMenuItemActionPerformed
 
   private void helpWisdomSourcesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpWisdomSourcesMenuItemActionPerformed
     td.openURL (WISDOM_SOURCES);
   }//GEN-LAST:event_helpWisdomSourcesMenuItemActionPerformed
 
   private void listRemoveQuotesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listRemoveQuotesMenuItemActionPerformed
     td.removeQuotesFromAll();
   }//GEN-LAST:event_listRemoveQuotesMenuItemActionPerformed
 
   private void listAddQuotesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listAddQuotesMenuItemActionPerformed
     td.addQuotesToAll();
   }//GEN-LAST:event_listAddQuotesMenuItemActionPerformed
 
   private void recordRemoveQuotesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordRemoveQuotesMenuItemActionPerformed
     td.removeQuotes();
   }//GEN-LAST:event_recordRemoveQuotesMenuItemActionPerformed
 
   private void recordAddQuotesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordAddQuotesMenuItemActionPerformed
     td.addQuotes();
   }//GEN-LAST:event_recordAddQuotesMenuItemActionPerformed
 
   private void itemRemoveQuotesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemRemoveQuotesButtonActionPerformed
     td.removeQuotes();
   }//GEN-LAST:event_itemRemoveQuotesButtonActionPerformed
 
   private void itemAddQuotesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemAddQuotesButtonActionPerformed
     td.addQuotes();
   }//GEN-LAST:event_itemAddQuotesButtonActionPerformed
 
   private void recordDuplicateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordDuplicateMenuItemActionPerformed
     td.itemCopy(true);
     td.itemPasteNew(true);
   }//GEN-LAST:event_recordDuplicateMenuItemActionPerformed
 
   private void helpReduceWindowSizeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpReduceWindowSizeMenuItemActionPerformed
     setBounds(100, 100, 500, 500);
     // pack();
 
   }//GEN-LAST:event_helpReduceWindowSizeMenuItemActionPerformed
 
   private void recordPasteNewMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordPasteNewMenuItemActionPerformed
     td.itemPasteNew(false);
   }//GEN-LAST:event_recordPasteNewMenuItemActionPerformed
 
   private void recordCopyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordCopyMenuItemActionPerformed
     td.itemCopy(false);
   }//GEN-LAST:event_recordCopyMenuItemActionPerformed
 
   private void helpHistoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpHistoryMenuItemActionPerformed
     td.openURL (programHistoryURL);
   }//GEN-LAST:event_helpHistoryMenuItemActionPerformed
 
   private void tabsItemMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabsItemMenuItemActionPerformed
     td.activateItemTab();
   }//GEN-LAST:event_tabsItemMenuItemActionPerformed
 
   private void tabsTreeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabsTreeMenuItemActionPerformed
     td.activateTreeTab();
   }//GEN-LAST:event_tabsTreeMenuItemActionPerformed
 
   private void tabsListMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabsListMenuItemActionPerformed
     td.activateListTab();
   }//GEN-LAST:event_tabsListMenuItemActionPerformed
 
   private void fileImportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileImportMenuItemActionPerformed
     if (td.items.size() > 1) {
       td.handleMajorEvent();
     }
     WindowMenuManager.getShared().makeVisible(td.importWindow);
   }//GEN-LAST:event_fileImportMenuItemActionPerformed
 
   private void listReplaceCategoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listReplaceCategoryMenuItemActionPerformed
     replaceCategory();
   }//GEN-LAST:event_listReplaceCategoryMenuItemActionPerformed
 
   private void listValidateURLsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listValidateURLsActionPerformed
     td.validateURLs();
   }//GEN-LAST:event_listValidateURLsActionPerformed
 
   private void recordFindAgainMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordFindAgainMenuItemActionPerformed
     findAgain();
   }//GEN-LAST:event_recordFindAgainMenuItemActionPerformed
 
   private void recordFindMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordFindMenuItemActionPerformed
     findItem();
   }//GEN-LAST:event_recordFindMenuItemActionPerformed
 
   private void listMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listMenuActionPerformed
     // Add your handling code here:
   }//GEN-LAST:event_listMenuActionPerformed
 
   private void recordDeleteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordDeleteMenuItemActionPerformed
     deleteItem();
   }//GEN-LAST:event_recordDeleteMenuItemActionPerformed
 
   private void recordPriorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordPriorMenuItemActionPerformed
     td.priorItem();
   }//GEN-LAST:event_recordPriorMenuItemActionPerformed
 
   private void recordNextMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordNextMenuItemActionPerformed
     td.nextItem();
   }//GEN-LAST:event_recordNextMenuItemActionPerformed
 
   private void recordNewMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordNewMenuItemActionPerformed
     newItem();
   }//GEN-LAST:event_recordNewMenuItemActionPerformed
 
   private void helpUserGuideMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpUserGuideMenuItemActionPerformed
     td.openURL (userGuideURL);
   }//GEN-LAST:event_helpUserGuideMenuItemActionPerformed
 
   private void helpHomePageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpHomePageMenuItemActionPerformed
     td.openURL (HOME_PAGE);
   }//GEN-LAST:event_helpHomePageMenuItemActionPerformed
 
   private void itemDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemDeleteButtonActionPerformed
     deleteItem();
   }//GEN-LAST:event_itemDeleteButtonActionPerformed
 
   private void fileNewMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileNewMenuItemActionPerformed
     td.fileNew();
   }//GEN-LAST:event_fileNewMenuItemActionPerformed
 
   private void itemNewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemNewButtonActionPerformed
     newItem();
   }//GEN-LAST:event_itemNewButtonActionPerformed
 
   private void fileExportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileExportMenuItemActionPerformed
     td.exportWindow.setLocation (
         this.getX() + CHILD_WINDOW_X_OFFSET, 
         this.getY() + CHILD_WINDOW_Y_OFFSET);
     WindowMenuManager.getShared().makeVisible(td.exportWindow);
   }//GEN-LAST:event_fileExportMenuItemActionPerformed
 
   private void fileOpenMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileOpenMenuItemActionPerformed
     td.fileOpen();
   }//GEN-LAST:event_fileOpenMenuItemActionPerformed
 
   private void itemLastButtonAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemLastButtonAction
     td.lastItem();
   }//GEN-LAST:event_itemLastButtonAction
 
   private void itemNextButtonAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemNextButtonAction
     td.nextItem();
   }//GEN-LAST:event_itemNextButtonAction
 
   private void itemPriorButtonAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemPriorButtonAction
     td.priorItem();
   }//GEN-LAST:event_itemPriorButtonAction
 
   private void itemFirstButtonAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemFirstButtonAction
     td.firstItem();
   }//GEN-LAST:event_itemFirstButtonAction
   
   /** Exit the Application */
   private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
     handleQuit();
   }//GEN-LAST:event_exitForm
 
 private void recordLastMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordLastMenuItemActionPerformed
   td.lastItemCopy();
 }//GEN-LAST:event_recordLastMenuItemActionPerformed
 
 private void itemOkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemOkButtonActionPerformed
   td.doneEditing();
 }//GEN-LAST:event_itemOkButtonActionPerformed
 
 private void fileImportWikiQuoteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileImportWikiQuoteMenuItemActionPerformed
   WindowMenuManager.getShared().makeVisible(td.importWikiQuoteWindow);
 }//GEN-LAST:event_fileImportWikiQuoteMenuItemActionPerformed
 
 private void publishMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publishMenuItemActionPerformed
   td.displayPublishWindow();
 }//GEN-LAST:event_publishMenuItemActionPerformed
 
 private void helpSoftwareUpdatesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpSoftwareUpdatesMenuItemActionPerformed
   programVersion.informUserIfNewer();
   programVersion.informUserIfLatest();
 }//GEN-LAST:event_helpSoftwareUpdatesMenuItemActionPerformed
 
 private void submitFeedbackMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitFeedbackMenuItemActionPerformed
  td.openURL ("mailto:support@powersurgepub.com?subject=iWisdom Feedback");
 }//GEN-LAST:event_submitFeedbackMenuItemActionPerformed
 
 private void cleanUpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cleanUpMenuItemActionPerformed
   int response = JOptionPane.showConfirmDialog(
       this, 
       "Remove files and folders left over from older versions of iWisdom?", 
       "Clean up Wisdom folder", 
       JOptionPane.OK_CANCEL_OPTION, 
       JOptionPane.QUESTION_MESSAGE);
   if (response == 0) {
     td.cleanup();
   }
 }//GEN-LAST:event_cleanUpMenuItemActionPerformed
 
   private void publishNowMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publishNowMenuItemActionPerformed
     td.publishNow();
   }//GEN-LAST:event_publishNowMenuItemActionPerformed
   
   /**
    * @param args the command line arguments
    */
   public static void main(String args[]) {
     java.awt.EventQueue.invokeLater(new Runnable() {
         public void run() {
             new iWisdom().setVisible(true);
         }
     });
   }
   
   
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JMenuItem cleanUpMenuItem;
   private javax.swing.JMenu editMenu;
   private javax.swing.JMenuItem fileBackupMenuItem;
   private javax.swing.JMenuItem fileExportMenuItem;
   private javax.swing.JMenuItem fileImportMenuItem;
   private javax.swing.JMenuItem fileImportWikiQuoteMenuItem;
   private javax.swing.JMenu fileMenu;
   private javax.swing.JMenuItem fileNewMenuItem;
   private javax.swing.JMenuItem fileOpenMenuItem;
   private javax.swing.JMenuItem filePropertiesMenuItem;
   private javax.swing.JMenuItem fileRevertMenuItem;
   private javax.swing.JButton findButton;
   private javax.swing.JTextField findText;
   private javax.swing.JSeparator helpAppSeparator;
   private javax.swing.JMenuItem helpHistoryMenuItem;
   private javax.swing.JMenuItem helpHomePageMenuItem;
   private javax.swing.JMenu helpMenu;
   private javax.swing.JMenuItem helpReduceWindowSizeMenuItem;
   private javax.swing.JMenuItem helpSoftwareUpdatesMenuItem;
   private javax.swing.JMenuItem helpUserGuideMenuItem;
   private javax.swing.JSeparator helpWebSeparator;
   private javax.swing.JMenuItem helpWisdomSourcesMenuItem;
   private javax.swing.JButton itemAddQuotesButton;
   private javax.swing.JButton itemDeleteButton;
   private javax.swing.JButton itemFirstButton;
   private javax.swing.JButton itemLastButton;
   private javax.swing.JButton itemNewButton;
   private javax.swing.JButton itemNextButton;
   private javax.swing.JButton itemOkButton;
   private javax.swing.JButton itemPriorButton;
   private javax.swing.JButton itemRemoveQuotesButton;
   private javax.swing.JSeparator jSeparator1;
   private javax.swing.JSeparator jSeparator2;
   private javax.swing.JSeparator jSeparator3;
   private javax.swing.JMenuItem listAddQuotesMenuItem;
   private javax.swing.JMenu listMenu;
   private javax.swing.JMenuItem listRemoveQuotesMenuItem;
   private javax.swing.JMenuItem listReplaceCategoryMenuItem;
   private javax.swing.JMenuItem listValidateURLs;
   private javax.swing.JMenuBar mainMenuBar;
   private javax.swing.JSplitPane mainSplitPanel;
   private javax.swing.JToolBar navToolBar;
   private javax.swing.JMenuItem optionsMenuItem;
   private javax.swing.JMenuItem publishMenuItem;
   private javax.swing.JMenuItem publishNowMenuItem;
   private javax.swing.JMenuItem recordAddQuotesMenuItem;
   private javax.swing.JMenuItem recordCopyMenuItem;
   private javax.swing.JSeparator recordCopyPasteMenuSep;
   private javax.swing.JMenuItem recordDeleteMenuItem;
   private javax.swing.JMenuItem recordDuplicateMenuItem;
   private javax.swing.JMenuItem recordFindAgainMenuItem;
   private javax.swing.JMenuItem recordFindMenuItem;
   private javax.swing.JMenuItem recordLastMenuItem;
   private javax.swing.JMenu recordMenu;
   private javax.swing.JMenuItem recordNewMenuItem;
   private javax.swing.JMenuItem recordNextMenuItem;
   private javax.swing.JMenuItem recordPasteNewMenuItem;
   private javax.swing.JMenuItem recordPriorMenuItem;
   private javax.swing.JMenuItem recordRemoveQuotesMenuItem;
   private javax.swing.JButton rotateButton;
   private javax.swing.JMenuItem submitFeedbackMenuItem;
   private javax.swing.JTabbedPane tabs;
   private javax.swing.JTabbedPane tabs2;
   private javax.swing.JMenuItem tabsItemMenuItem;
   private javax.swing.JMenuItem tabsListMenuItem;
   private javax.swing.JMenu tabsMenu;
   private javax.swing.JMenuItem tabsTreeMenuItem;
   private javax.swing.JMenu toolsMenu;
   private javax.swing.JMenuItem viewByDueDateMenuItem;
   private javax.swing.JMenuItem viewByPriorityMenuItem;
   private javax.swing.JMenu viewMenu;
   private javax.swing.JMenu windowMenu;
   // End of variables declaration//GEN-END:variables
   
 }
