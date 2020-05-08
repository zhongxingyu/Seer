 /*
  * Copyright 2012 - 2013 Herb Bowie
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
 
 package com.powersurgepub.linktweaker;
 
   import com.powersurgepub.psdatalib.ui.*;
   import com.powersurgepub.psutils.*;
   import com.powersurgepub.xos2.*;
   import java.awt.*;
   import java.awt.event.*;
   import java.io.*;
   import java.net.*;
   import javax.swing.*;
   import javax.swing.event.*;
 
 /**
  Tweaks URLs for simplicity and clarity. 
 
  @author Herb Bowie
  */
 public class LinkTweaker 
     extends javax.swing.JFrame
         implements
             HyperlinkListener,
             LinkTweakerInterface,
             WindowToManage,
             XHandler {
   
   public static final String PROGRAM_NAME = "LinkTweaker";
  public static final String PROGRAM_VERSION = "1.10";
   
   public static final String SP_SITES = "/sites";
   
   public static final boolean PREFS_AVAILABLE = true;
   
   public static final String DEFAULT_LINK_ID = "Link";
   
   private boolean runningAsMainApp = true;
   
   private     static  final int   DEFAULT_WIDTH = 620;
   private     static  final int   DEFAULT_HEIGHT = 540;
   
   private                   int   defaultX = 0;
   private                   int   defaultY = 0;
   
   private             Appster appster;
 
   private             String  country = "  ";
   private             String  language = "  ";
 
   private             Home                home;
   private             ProgramVersion      programVersion;
   private             XOS                 xos;
   private             Trouble             trouble = Trouble.getShared();
   
   // About window
   private             AboutWindow         aboutWindow;
   
   private             LogWindow           logWindow;
   private             Logger              logger     = Logger.getShared();
   private             LogOutput           logOutput;
   
   private             PrefsWindow         prefsWindow;
   
   private             TweakerPrefs        tweakerPrefs;
   
   private             LinkTweakerApp      linkTweakerApp = null;
   
   // Menu Objects
   private			JMenuBar						menuBar;
   
   private			JMenu								fileMenu;
   private			JMenuItem						fileOpen;
   private			JMenuItem						fileExit;
   
   private     JMenu               toolsMenu;
   private     JMenuItem           toolsOptions;
   
   private     JMenu               windowMenu;
   
   private			JMenu								helpMenu;
 
   private     JSeparator          helpSeparator2;
   private			JMenuItem						helpAbout;
   
   private			JMenuItem						helpPSPubWebSite;
   private     JSeparator          helpSeparator3;
   private     JMenuItem           helpReduceWindowSize;
   
   private     String              linkID = DEFAULT_LINK_ID;
 
   /**
    Creates new form LinkTweaker
    */
   public LinkTweaker() {
     constructApp();
   }
   
   public LinkTweaker (LinkTweakerApp linkTweakerApp, JTabbedPane prefsTabs) {
     this.runningAsMainApp = false;
     this.linkTweakerApp = linkTweakerApp;
     home = Home.getShared ();
     xos = XOS.getShared();
     tweakerPrefs = new TweakerPrefs();
     if (runningAsMainApp) {
       constructApp();
     } else {
       constructWindow();
     }
   }
   
   private void constructApp() {
     
     appster = new Appster
         ("powersurgepub", "com",
           PROGRAM_NAME, PROGRAM_VERSION,
           language, country,
           this, this);
     
     home = Home.getShared ();
     xos = XOS.getShared();
     
     logWindow = new LogWindow ();
     logOutput = new LogOutputText(logWindow.getTextArea());
     Logger.getShared().setLog (logOutput);
     Logger.getShared().setLogAllData (false);
     Logger.getShared().setLogThreshold (LogEvent.NORMAL);
     WindowMenuManager.getShared().add(logWindow);
     
     aboutWindow = new AboutWindow(false, true, false, false, false, false, 
         "2012");
     
     tweakerPrefs = new TweakerPrefs();
     
     prefsWindow = new PrefsWindow (tweakerPrefs);
     
     Trouble.getShared().setParent(this);
     if (PREFS_AVAILABLE) {
       xos.enablePreferences();
     }
 
     constructWindow();
     
     xos.setXHandler (this);
     xos.setMainWindow (this);
     
     // Create Menus for the app
     menuBar = new JMenuBar();
     this.setJMenuBar (menuBar);
     
     // File menu
     fileMenu = new JMenu("File");
     if (! xos.isRunningOnMacOS()) {
       menuBar.add (fileMenu);
       /*
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
       */
     }
     xos.setFileMenu (fileMenu);
     
     // Tools menu
     if (! xos.isRunningOnMacOS()) {
       toolsMenu = new JMenu("Tools");
       menuBar.add (toolsMenu);
       toolsOptions = new JMenuItem ("Options");
       toolsMenu.add (toolsOptions);
       toolsOptions.addActionListener (new ActionListener ()
         {
           public void actionPerformed (ActionEvent event) {
             handlePreferences();
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
     xos.setHelpMenu (helpMenu);
     home.setHelpMenu(helpMenu);
     xos.setHelpMenuItem (home.getHelpMenuItem());
     /*
     if (! xos.isRunningOnMacOS()) {
       helpAbout = new JMenuItem ("About " + PROGRAM_NAME);
       helpMenu.add (helpAbout);
       helpAbout.addActionListener (new ActionListener ()
         {
           public void actionPerformed (ActionEvent event) {
             handleAbout();
           } // end actionPerformed method
         } // end action listener
       );
     } 
     helpSeparator3 = new JSeparator();
     helpMenu.add (helpSeparator3);
     */
     
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
     
 
     
     
     WindowMenuManager.getShared().addWindowMenu(windowMenu);
     /* 
     try {
       userGuideURL = new URL (pageURL, USER_GUIDE);
     } catch (MalformedURLException e) {
     } 
     */
     /* 
     try {
       programHistoryURL = new URL(pageURL, PROGRAM_HISTORY);
     } catch (MalformedURLException e) {
       // shouldn't happen
     } 
     */
     // xos.setHelpMenuItem (helpUserGuideMenuItem);
 
   }
   
   private void constructWindow() {
     
     programVersion = ProgramVersion.getShared ();
     initComponents();
     setBounds (100, 100, DEFAULT_WIDTH, DEFAULT_HEIGHT);
     try {
       javax.swing.UIManager.setLookAndFeel
         (javax.swing.UIManager.getSystemLookAndFeelClassName());
     } catch (Exception e) {
       // Let's hope this doesn't happen!!
     }
     SwingUtilities.updateComponentTreeUI (this);
   }
   
   private void setDefaultScreenSizeAndLocation() {
 
 		this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
     this.setResizable (true);
 		calcDefaultScreenLocation();
 		this.setLocation (defaultX, defaultY);
   }
   
   private void calcDefaultScreenLocation() {
     Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
     defaultX = (d.width - this.getSize().width) / 2;
     defaultY = (d.height - this.getSize().height) / 2;
   }
   
   /**
    Set the input link to the passed value, when using this class as an
    auxiliary window to a main program. 
   
    @param passedLink The link to be tweaked. 
   */
   public void setLink(String passedLink) {
     inputTextArea.setText(passedLink);
     outputTextArea.setText(passedLink);
     linkID = "Link";
   }
   
   public void setLink(String passedLink, String LinkID) {
     inputTextArea.setText(passedLink);
     outputTextArea.setText(passedLink);
     this.linkID = linkID;
   }
   
   /**
    Let's straighten out the URL submitted by the user. 
   */
   private void tweakLink() {
     
     StringBuilder link = new StringBuilder(inputTextArea.getText().trim());
     
     // If it's a file path, insert the proper URL protocol (aka scheme)
    // and make sure it's got five slashes following. 
     if (link.length() > 0 && 
         (link.charAt(0) == '\\' || link.charAt(0) == '/')) {
       link.insert(0, "file:");  
     }
    if (link.length() >= 5
        && link.substring(0,5).equalsIgnoreCase("file:")) {
      while (link.length() > 5
          && (link.charAt(5) == '/' || link.charAt(5) == '\\')) {
        link.deleteCharAt(5);
      }
      link.delete(0, 5);
      link.insert(0, "file://///");
    }
     
     // Remove a trailing period, in case one got attached somehow along the way
     if (link.length() > 0 &&
         link.charAt(link.length() - 1) == '.') {
       link.deleteCharAt(link.length() - 1);
     }
     
     // Let's make one pass through the link to normalize special characters
     int i = 0;  // Point to current position being examined
     int j = 0;  // After change, point to new position to be examined
     
     while (i < link.length()) {
       j = i;
       
       // If user wants to see spaces, then let's show them spaces
       if (j == i
           && spacesCheckBox.isSelected()) {
         j = ifMatchReplaceWith (link, i, "%20", " ");
       }
       if (j == i
           && spacesCheckBox.isSelected()) {
         j = ifMatchReplaceWith (link, i, "%2520", " ");
       }
       
       // Let's return other common characters proxies to their benign real selves
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%26", "&");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%28", "(");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%29", ")");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%2d", "-");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%2D", "-");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%2e", ".");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%2E", ".");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%2f", "/");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%2F", "/");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%252F", "/");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%3a", ":");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%3A", ":");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%3d", "=");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%3D", "=");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%3f", "?");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%3F", "?");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%2520", "%20");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%5f", "_");
       }
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "%5F", "_");
       }
       
      // If we've got a backslash being used to escape a space, then let's
      // remove the escape character and leave the space. 
      if (j == i) {
        if (spacesCheckBox.isSelected()) {
          j = ifMatchReplaceWith (link, i, "\\ ", " ");
        } else {
          j = ifMatchReplaceWith (link, i, "\\ ", "%20");
        }
      }
       // If we've got backslashes, let's turn them around
       if (j == i) {
         j = ifMatchReplaceWith (link, i, "\\", "/");
       }
       
       // If user doesn't want to see spaces, then let's encode them
       if (j == i
           && (! spacesCheckBox.isSelected())) {
         j = ifMatchReplaceWith (link, i, " ", "%20");
       }
       
       // Remove carriage returns, tabs, line feeds, etc.
       if (j == i) {
         if (link.charAt(i) == '\r' 
             || link.charAt(i) == '\n'
             || link.charAt(i) == '\t'
             || link.charAt(i) == '\"') {
           link.deleteCharAt(i);
         } else {
           i++;
         }
       } else {
         i = j;
       }
     }
     
     // Remove email quote identifiers
     if (j == i) {
       j = ifMatchReplaceWith (link, i, "> ", "");
     }
     
     // Let's make another pass through the link to clean up SharePoint cruft
     if (spCruftCheckBox.isSelected()) {
       i = 0;
       int sites1Index = -1;
 
       while (i < link.length()) {
         j = i;
 
         // Delete '&Folder' and anything that follows
         if (j == i) {
           if (link.charAt(i) == '&'
               && (i + 7) < link.length()
               && link.substring(i, i + 7).equals("&Folder")) {
             link.delete(i, link.length());
             i--;
           }
         }
         
         // Delete '&Source=' and anything that follows
         if (j == i) {
           if (link.charAt(i) == '&'
               && (i + 8) < link.length()
               && link.substring(i, i + 8).equals("&Source=")) {
             link.delete(i, link.length());
             i--;
           }
         }
 
         if (j == i) {
           j = ifMatchReplaceWith (link, i, 
               "/Forms/AllItems.aspx?RootFolder=/", 
               "/");
           if (j > i) {
             int path2start = i;
             int path2end = link.indexOf("/", j);
             if (path2end < 0) {
               path2end = link.length();
             }
             String path2 = link.substring(path2start, path2end);
             // System.out.println ("path2 = " + path2);
             int path1start = link.indexOf(path2);
             if (path1start >= 0 && path1start < path2start) {
               int path1end = path1start + path2.length();
               while (path2end < link.length()
                   && path1end < path2start
                   && link.charAt(path1end) == link.charAt(path2end)) {
                 path1end++;
                 path2end++;
               } // end while matching path characters
               link.delete(path2start, path2end);
             } // end if we found duplicate paths
           } // end if we found the rootfolder string
         } 
         if (j == i) {
           j = ifMatchReplaceWith (link, i, 
               "/Forms/AllItems.aspx", 
               "/");
         }
         if (j == i) {
           j = ifMatchReplaceWith (link, i, "/SitePages/Home.aspx", "/");
         }
         
         if (j == i) {
           j = ifMatchReplaceWith (link, i, "/Pages/Default.aspx", "/");
         }
         
         // If we find '/sites' repeated, let's delete the duplication
         if ((i + SP_SITES.length()) < link.length()
             && link.substring(i, i + SP_SITES.length()).equals(SP_SITES)) {
           if (sites1Index < 0) {
             sites1Index = i;
           } else {
             link.delete(sites1Index, i);
             j = sites1Index + SP_SITES.length();
           }
         }
         
         if (j == i) {
           i++;
         } else {
           i = j;
         }
       }
     }
     
     // Insert a redirect, if user has so specified
     if (redirectCheckBox.isSelected()) {
       link.insert(0, tweakerPrefs.getRedirectURL());
     }
     
     outputTextArea.setText(link.toString());
     msgLabel.setText(" ");
     if (linkTweakerApp != null) {
       linkTweakerApp.setTweakedLink (link.toString(), linkID);
     }
   }
   
   /**
    If the given before string matches the next sequence of characters, then 
    replace it with the after string. 
   
    @param str    The StringBuffer to possibly be changed.
    @param i      The current position in the StringBuffer to be examined. 
    @param before The matching string we are looking for. 
    @param after  The replacement string, if a match is found.
   
    @return The resulting next index position to be examined. If no replacement
            was made, this will be equal to the i param. If a replacement was 
            made, it will be equal to i + the length of the to string. 
   */
   private int ifMatchReplaceWith 
       (StringBuilder str, int i, String before, String after) {
     if ((i + before.length()) <= str.length()
         && str.substring(i, i + before.length()).equals(before)) {
       str.delete(i, i + before.length());
       str.insert(i, after);
       return (i + after.length());
     } else {
       return i;
     }
   }
   
   public void hyperlinkUpdate (HyperlinkEvent e) {
     HyperlinkEvent.EventType type = e.getEventType();
     if (type == HyperlinkEvent.EventType.ACTIVATED) {
       home.openURL(e.getURL()); 
     }
   }
   
   /**
      Standard way to respond to an About Menu Item Selection on a Mac.
    */
   public void handleAbout() {
     displayAuxiliaryWindow(aboutWindow);
   }
   
   public void displayAuxiliaryWindow(WindowToManage window) {
     window.setLocation(
         this.getX() + 60,
         this.getY() + 60);
     WindowMenuManager.getShared().makeVisible(window);
   }
   
   /**      
     Standard way to respond to a document being passed to this application on a Mac.
    
     @param inFile File to be processed by this application, generally
                   as a result of a file or directory being dragged
                   onto the application icon.
    */
   public void handleOpenFile (File inFile) {
     // Not supported
   }
   
   /**
    Open the passed URI. 
    
    @param inURI The URI to open. 
   */
   public void handleOpenURI(URI inURI) {
     // Not supported
   }
   
   public boolean preferencesAvailable() {
     return PREFS_AVAILABLE;
   }
   
   /**
      Standard way to respond to a Preferences Item Selection on a Mac.
    */
   public void handlePreferences() {
     displayPrefs ();
   }
 
   public void displayPrefs () {
     displayAuxiliaryWindow(prefsWindow);
   }
   
   /**
    Standard way to respond to a print request.
    */
   public void handlePrintFile (File printFile) {
     // not supported
   }
   
   private void windowClose() {
 
     if (runningAsMainApp) {
       // System.out.println("  Running as Main App = true");
       handleQuit();
     } else {
       // System.out.println("  Running as Main App = false");
       WindowMenuManager.getShared().hide(this);
     }
   }
   
   /**
      We're out of here!
    */
   public void handleQuit() {
     // System.out.println("LinkTweaker.handleQuit");
     savePrefs();
     System.exit(0);
   }
 
   private void savePrefs() {
     tweakerPrefs.savePrefs();
   }
 
   /**
    This method is called from within the constructor to initialize the form.
    WARNING: Do NOT modify this code. The content of this method is always
    regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {
     java.awt.GridBagConstraints gridBagConstraints;
 
     inputLabel = new javax.swing.JLabel();
     inputScrollPane = new javax.swing.JScrollPane();
     inputTextArea = new javax.swing.JTextArea();
     outputLabel = new javax.swing.JLabel();
     outputScrollPane = new javax.swing.JScrollPane();
     outputTextArea = new javax.swing.JTextArea();
     msgLabel = new javax.swing.JLabel();
     buttonPanel = new javax.swing.JPanel();
     tweakButton = new javax.swing.JButton();
     launchButton = new javax.swing.JButton();
     copyButton = new javax.swing.JButton();
     optionPanel = new javax.swing.JPanel();
     spCruftCheckBox = new javax.swing.JCheckBox();
     redirectCheckBox = new javax.swing.JCheckBox();
     spacesCheckBox = new javax.swing.JCheckBox();
 
     setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
     setTitle("LinkTweaker");
     addWindowListener(new java.awt.event.WindowAdapter() {
       public void windowClosing(java.awt.event.WindowEvent evt) {
         formWindowClosing(evt);
       }
       public void windowClosed(java.awt.event.WindowEvent evt) {
         formWindowClosed(evt);
       }
     });
     getContentPane().setLayout(new java.awt.GridBagLayout());
 
     inputLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
     inputLabel.setText("Input Link:");
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 0;
     gridBagConstraints.gridwidth = 2;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     getContentPane().add(inputLabel, gridBagConstraints);
 
     inputTextArea.setColumns(20);
     inputTextArea.setLineWrap(true);
     inputTextArea.setRows(5);
     inputTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
       public void focusGained(java.awt.event.FocusEvent evt) {
         inputTextAreaFocusGained(evt);
       }
       public void focusLost(java.awt.event.FocusEvent evt) {
         inputTextAreaFocusLost(evt);
       }
     });
     inputScrollPane.setViewportView(inputTextArea);
 
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 1;
     gridBagConstraints.gridwidth = 2;
     gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
     gridBagConstraints.ipadx = 221;
     gridBagConstraints.ipady = 61;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
     gridBagConstraints.weightx = 1.0;
     gridBagConstraints.weighty = 0.5;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     getContentPane().add(inputScrollPane, gridBagConstraints);
 
     outputLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
     outputLabel.setText("Output Link:");
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 2;
     gridBagConstraints.gridwidth = 2;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     getContentPane().add(outputLabel, gridBagConstraints);
 
     outputTextArea.setColumns(20);
     outputTextArea.setLineWrap(true);
     outputTextArea.setRows(5);
     outputTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
       public void focusGained(java.awt.event.FocusEvent evt) {
         outputTextAreaFocusGained(evt);
       }
     });
     outputScrollPane.setViewportView(outputTextArea);
 
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 3;
     gridBagConstraints.gridwidth = 2;
     gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
     gridBagConstraints.ipadx = 221;
     gridBagConstraints.ipady = 61;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
     gridBagConstraints.weightx = 1.0;
     gridBagConstraints.weighty = 0.5;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     getContentPane().add(outputScrollPane, gridBagConstraints);
 
     msgLabel.setText(" ");
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 7;
     gridBagConstraints.gridwidth = 2;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     getContentPane().add(msgLabel, gridBagConstraints);
 
     buttonPanel.setLayout(new java.awt.GridBagLayout());
 
     tweakButton.setText("Tweak");
     tweakButton.setMaximumSize(new java.awt.Dimension(120, 29));
     tweakButton.setMinimumSize(new java.awt.Dimension(89, 29));
     tweakButton.setPreferredSize(new java.awt.Dimension(100, 29));
     tweakButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         tweakButtonActionPerformed(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 4;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     buttonPanel.add(tweakButton, gridBagConstraints);
 
     launchButton.setText("Launch");
     launchButton.setMaximumSize(new java.awt.Dimension(120, 29));
     launchButton.setPreferredSize(new java.awt.Dimension(100, 29));
     launchButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         launchButtonActionPerformed(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 5;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     buttonPanel.add(launchButton, gridBagConstraints);
 
     copyButton.setText("Copy");
     copyButton.setMaximumSize(new java.awt.Dimension(120, 29));
     copyButton.setMinimumSize(new java.awt.Dimension(89, 29));
     copyButton.setPreferredSize(new java.awt.Dimension(100, 29));
     copyButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         copyButtonActionPerformed(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 6;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     buttonPanel.add(copyButton, gridBagConstraints);
 
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 4;
     gridBagConstraints.gridheight = 3;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.weightx = 0.5;
     getContentPane().add(buttonPanel, gridBagConstraints);
 
     optionPanel.setLayout(new java.awt.GridBagLayout());
 
     spCruftCheckBox.setText("Remove SharePoint Cruft?");
     spCruftCheckBox.setMaximumSize(new java.awt.Dimension(192, 29));
     spCruftCheckBox.setMinimumSize(new java.awt.Dimension(192, 29));
     spCruftCheckBox.setPreferredSize(new java.awt.Dimension(192, 29));
     spCruftCheckBox.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         spCruftCheckBoxActionPerformed(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 2;
     gridBagConstraints.gridy = 4;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     optionPanel.add(spCruftCheckBox, gridBagConstraints);
 
     redirectCheckBox.setText("Insert Redirect?");
     redirectCheckBox.setMaximumSize(new java.awt.Dimension(128, 29));
     redirectCheckBox.setMinimumSize(new java.awt.Dimension(128, 29));
     redirectCheckBox.setPreferredSize(new java.awt.Dimension(128, 29));
     redirectCheckBox.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         redirectCheckBoxActionPerformed(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 2;
     gridBagConstraints.gridy = 5;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     optionPanel.add(redirectCheckBox, gridBagConstraints);
 
     spacesCheckBox.setText("Show spaces as spaces?");
     spacesCheckBox.setMaximumSize(new java.awt.Dimension(182, 29));
     spacesCheckBox.setMinimumSize(new java.awt.Dimension(182, 29));
     spacesCheckBox.setPreferredSize(new java.awt.Dimension(182, 29));
     spacesCheckBox.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         spacesCheckBoxActionPerformed(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 2;
     gridBagConstraints.gridy = 6;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     optionPanel.add(spacesCheckBox, gridBagConstraints);
 
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 1;
     gridBagConstraints.gridy = 4;
     gridBagConstraints.gridheight = 3;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.weightx = 0.5;
     getContentPane().add(optionPanel, gridBagConstraints);
 
     pack();
   }// </editor-fold>//GEN-END:initComponents
 
   private void inputTextAreaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputTextAreaFocusLost
     tweakLink();
   }//GEN-LAST:event_inputTextAreaFocusLost
 
   private void redirectCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redirectCheckBoxActionPerformed
     tweakLink();
   }//GEN-LAST:event_redirectCheckBoxActionPerformed
 
   private void tweakButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tweakButtonActionPerformed
     tweakLink();
   }//GEN-LAST:event_tweakButtonActionPerformed
 
   private void launchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_launchButtonActionPerformed
     tweakLink();
     boolean ok = home.openURL(outputTextArea.getText());
     if (! ok) {
       msgLabel.setText("Error in launching link in Web browser");
     }
   }//GEN-LAST:event_launchButtonActionPerformed
 
   private void spCruftCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spCruftCheckBoxActionPerformed
     tweakLink();
   }//GEN-LAST:event_spCruftCheckBoxActionPerformed
 
   private void inputTextAreaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputTextAreaFocusGained
     inputTextArea.selectAll();
   }//GEN-LAST:event_inputTextAreaFocusGained
 
   private void outputTextAreaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_outputTextAreaFocusGained
     tweakLink();
     outputTextArea.selectAll();
   }//GEN-LAST:event_outputTextAreaFocusGained
 
   private void copyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyButtonActionPerformed
     tweakLink();
     outputTextArea.selectAll();
     outputTextArea.copy();
   }//GEN-LAST:event_copyButtonActionPerformed
 
   private void spacesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spacesCheckBoxActionPerformed
     outputTextArea.setWrapStyleWord(spacesCheckBox.isSelected());
     tweakLink();
   }//GEN-LAST:event_spacesCheckBoxActionPerformed
 
   private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
     // System.out.println("LinkTweaker.formWindowClosed");
     windowClose();
   }//GEN-LAST:event_formWindowClosed
 
   private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
     // System.out.println("LinkTweaker.formWindowClosing");
     windowClose();
   }//GEN-LAST:event_formWindowClosing
 
   /**
    @param args the command line arguments
    */
   public static void main(String args[]) {
     /* Set the Nimbus look and feel */
     //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
      * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
      */
     try {
       for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
         if ("Nimbus".equals(info.getName())) {
           javax.swing.UIManager.setLookAndFeel(info.getClassName());
           break;
         }
       }
     } catch (ClassNotFoundException ex) {
       java.util.logging.Logger.getLogger(LinkTweaker.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
     } catch (InstantiationException ex) {
       java.util.logging.Logger.getLogger(LinkTweaker.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
     } catch (IllegalAccessException ex) {
       java.util.logging.Logger.getLogger(LinkTweaker.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
     } catch (javax.swing.UnsupportedLookAndFeelException ex) {
       java.util.logging.Logger.getLogger(LinkTweaker.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
     }
     //</editor-fold>
 
     /* Create and display the form */
     java.awt.EventQueue.invokeLater(new Runnable() {
       public void run() {
         new LinkTweaker().setVisible(true);
       }
     });
   }
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JPanel buttonPanel;
   private javax.swing.JButton copyButton;
   private javax.swing.JLabel inputLabel;
   private javax.swing.JScrollPane inputScrollPane;
   private javax.swing.JTextArea inputTextArea;
   private javax.swing.JButton launchButton;
   private javax.swing.JLabel msgLabel;
   private javax.swing.JPanel optionPanel;
   private javax.swing.JLabel outputLabel;
   private javax.swing.JScrollPane outputScrollPane;
   private javax.swing.JTextArea outputTextArea;
   private javax.swing.JCheckBox redirectCheckBox;
   private javax.swing.JCheckBox spCruftCheckBox;
   private javax.swing.JCheckBox spacesCheckBox;
   private javax.swing.JButton tweakButton;
   // End of variables declaration//GEN-END:variables
 }
