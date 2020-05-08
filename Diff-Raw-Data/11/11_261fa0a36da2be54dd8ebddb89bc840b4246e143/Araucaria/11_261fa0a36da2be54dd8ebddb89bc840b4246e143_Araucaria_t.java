 package org.simonwells.monkeypuzzle;
 
 /*
  * Araucaria.java
  *
  * Created on 16 March 2004, 13:12
  */
 
 /**
  *
  * @author  growe
  */
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.filechooser.*;
 import java.awt.event.*;
 import java.awt.*;
 import java.awt.image.*;
 import java.io.*;
 import java.sql.*;
 import java.util.*;
 import javax.imageio.*;
 import javax.media.jai.*;
 import com.sun.media.jai.codec.*;
 import org.xml.sax.*;
 import org.xml.sax.helpers.*;
 //import com.sun.image.codec.jpeg.*;
 import javax.imageio.ImageIO;
 
 public class Araucaria extends javax.swing.JFrame implements Runnable, ControlFrame
 {
   public static String mainFrameTitle = "Araucaria 3.2 ";
   // Undo/redo stack
   public UndoStack undoStack = new UndoStack();
   // Store various diagram types in an array of the abstract base class
   private final int numDiagrams = 9;
   public DisplayFrame[] displays;
   // Labels for the various diagram types
   public static final int DIAGRAM_FULL = 0;
   public static final int DIAGRAM_FULL_SIZE = 1;
   public static final int DIAGRAM_FULL_TEXT = 2;
   public static final int DIAGRAM_TOULMIN_SCALED = 3;
   public static final int DIAGRAM_TOULMIN_FULL_SIZE = 4;
   public static final int DIAGRAM_TOULMIN_FULL_TEXT = 5;
   public static final int DIAGRAM_WIGMORE_SCALED = 6;
   public static final int DIAGRAM_WIGMORE_FULL_SIZE = 7;
   public static final int DIAGRAM_WIGMORE_FULL_TEXT = 8;
   public static final int DIAGRAM_STANDARD = 0;
   public static final int DIAGRAM_TOULMIN = 1;
   public static final int DIAGRAM_WIGMORE = 2;
   public static final int DIAGRAM_STYLES = 3;
   public int diagramStyle = DIAGRAM_STANDARD;
   public static final int ZOOM_SCALED = 0;
   public static final int ZOOM_FULLSIZE = 1;
   public static final int ZOOM_FULLTEXT = 2;
   public static final int ZOOM_STATES = 3;
   public int zoomState = ZOOM_FULLTEXT;
   private DisplayFrame currentDiagram;    // The currently displayed diagram in the tabbed pane
   // Argument properties
   private int wordCount;
   // Main GUI components
   public final int ARAU_HEIGHT = 650;
   public final int ARAU_WIDTH = 900;
   private SelectText selectText;
   private javax.swing.JScrollPane selectTextScroll;
   private javax.swing.JTabbedPane displayTabbedPane;
   private JRadioButton scaledRadio,  fullSizeRadio,  fullTextRadio;
   // Menu
   private javax.swing.JMenuBar mainMenuBar = new javax.swing.JMenuBar();
   // File menu
   private javax.swing.JMenu fileMenu = new javax.swing.JMenu();
   private javax.swing.JMenuItem openTextMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JSeparator menuSeparator = new javax.swing.JSeparator();
   private javax.swing.JMenuItem openArgumentMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem saveArgumentMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem saveAsArgumentMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem saveDiagramMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem closeAllMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem propertiesMenu = new javax.swing.JMenuItem();
   PropertiesDialog propertiesDialog;
   String source, comments, author, date;
   private javax.swing.JMenuItem preferencesMenuItem = new javax.swing.JMenuItem();
   private JMenu recentTextFilesMenu,  recentAmlFilesMenu,  recentSchemeFilesMenu;
   private javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
   // Edit menu
   private javax.swing.JMenu editMenu = new javax.swing.JMenu();
   public javax.swing.JMenuItem undoMenuItem = new javax.swing.JMenuItem();
   public javax.swing.JMenuItem redoMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem clearDiagramMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem flipDiagramMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem missingPremiseMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem refutationMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem deleteMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem linkMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem unlinkMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem selectAllMenuItem = new javax.swing.JMenuItem();
   // View menu
   private javax.swing.JMenu viewMenu = new javax.swing.JMenu();
   private javax.swing.JMenu zoomMenu = new javax.swing.JMenu();
   private javax.swing.JMenuItem scaledMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem fullSizeMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem fullTextMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenu styleMenu = new javax.swing.JMenu();
   private javax.swing.JMenuItem standardMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem toulminMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem wigmoreMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem pollockMenuItem = new javax.swing.JMenuItem();
   // Labels menu
   private javax.swing.JMenu labelsMenu = new javax.swing.JMenu();
   private javax.swing.JMenuItem modifyOwnershipMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem showOwnersMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem modifyEvaluationMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem showEvaluationMenuItem = new javax.swing.JMenuItem();
   // Required as a class field since caption changes
   Action showOwnersAction = new FileActionHandler("");
   Action showEvaluationAction = new FileActionHandler("");
   // Schemes menu
   private javax.swing.JMenu schemesMenu = new javax.swing.JMenu();
   private javax.swing.JMenuItem selectSchemeMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem addEditSchemeMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem openSchemesetMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem saveSchemesetMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem showCQsMenuItem = new javax.swing.JMenuItem();
   Action showCQsAction = new FileActionHandler("");
   // AraucariaDB menu
   private javax.swing.JMenu araucariaDBMenu = new javax.swing.JMenu();
   private javax.swing.JMenuItem loginMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem registerMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem saveDBMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem searchDBMenuItem = new javax.swing.JMenuItem();
   // Tutoring menu
   public javax.swing.JMenu tutorMenu = new javax.swing.JMenu();
   private javax.swing.JMenuItem premiseEndpointsMenu = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem markingMenu = new javax.swing.JMenuItem();
   public static boolean tutorModeOn = false;
   // Help menu
   private javax.swing.JMenu helpMenu = new javax.swing.JMenu();
   private javax.swing.JMenuItem helpMenuItem = new javax.swing.JMenuItem();
   private javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
   // Toolbar
   // File toolbar
   private javax.swing.JToolBar mainToolBar = new javax.swing.JToolBar();
   private javax.swing.JButton openTextToolBar = new javax.swing.JButton();
   private javax.swing.JButton openArgumentToolBar = new javax.swing.JButton();
   private javax.swing.JButton saveArgumentToolBar = new javax.swing.JButton();
   private javax.swing.JButton saveDiagramToolBar = new javax.swing.JButton();
   // Edit toolbar
   public javax.swing.JButton undoToolBar = new javax.swing.JButton();
   public javax.swing.JButton redoToolBar = new javax.swing.JButton();
   private javax.swing.JButton clearDiagramToolBar = new javax.swing.JButton();
   private javax.swing.JButton flipDiagramToolBar = new javax.swing.JButton();
   private javax.swing.JButton missingPremiseToolBar = new javax.swing.JButton();
   private javax.swing.JButton refutationToolBar = new javax.swing.JButton();
   private javax.swing.JButton deleteToolBar = new javax.swing.JButton();
   private javax.swing.JButton linkToolBar = new javax.swing.JButton();
   private javax.swing.JButton unlinkToolBar = new javax.swing.JButton();
   // Scheme toolbar
   private javax.swing.JButton selectSchemeToolBar = new javax.swing.JButton();
   // Database toolbar
   private javax.swing.JButton saveDBToolBar = new javax.swing.JButton();
   private javax.swing.JButton searchDBToolBar = new javax.swing.JButton();
   private JComboBox searchResultCombo;
   // Help toolbar
   private javax.swing.JButton helpToolBar = new javax.swing.JButton();
   // Status bar
   public static Color STATUSBAR_BACKGROUND = new Color(1.0f, 1.0f, 0.7f);
   private javax.swing.JLabel messageLabel = new javax.swing.JLabel();
   public javax.swing.JPanel messagePanel = new javax.swing.JPanel();
   // File dialog stuff
   JFileChooser fileChoice, amlChoice, schemeChoice, tiffChoice;
   public static String textDirectory,  amlDirectory,  schemeDirectory;
   File lastDirectory, araucariaHome;
   File currentOpenXMLFile = null;
   public static int numRecentTextFiles = 4,  numRecentAmlFiles = 4,  numRecentSchemeFiles = 4;
   public Stack recentTextFiles,  recentAmlFiles,  recentSchemeFiles;
   // Database objects
   Connection dbConnection;
   Statement statement;
   static final int ARGID = 0;
   SearchFrame searchFrame = new SearchFrame(this, false);
   RegistrationDialog registrationDialog = new RegistrationDialog(this);
   LoginDialog loginDialog = new LoginDialog(this);
   String loggedInUser = null;
   public static String databaseName,  username,  password;
   public static int databaseType;
   public static final int MYSQL = 0,  SQLSERVER = 1;
   boolean doComboEvents = false;
   public static final int IMAGE_JPG = 0;
   public static final int IMAGE_TIF = 1;
   public static int imageType = IMAGE_TIF;
   Preferences preferencesDialog;
   // The main argument object - stores data on current argument
   Argument argument;
 
   /** Creates new form Araucaria */
   public Araucaria()
   {
     Thread splashThread = new Thread(this, "Main");
     splashThread.start();
     createIcon(this);
     argument = new Argument();
     displays = new DisplayFrame[numDiagrams];
     initComps();
     initDialogs();
     initDatabase();
     // Set up the preferences dialog - the prefs.dat file is loaded
     // in the Preferences constructor
     preferencesDialog = new Preferences(this, true);
     addKeyResponses();
     loadRecentFiles();
   }
 
   private void loadRecentFiles()
   {
     try
     {
       FileInputStream recentStream = new FileInputStream("recentFiles.dat");
       ObjectInputStream objStream = new ObjectInputStream(recentStream);
       recentTextFiles = (Stack) objStream.readObject();
       recentAmlFiles = (Stack) objStream.readObject();
       recentSchemeFiles = (Stack) objStream.readObject();
       objStream.close();
     } catch (Exception ex)
     {  // If we get an error, redefine recentFiles.dat from defaults
       recentTextFiles = new Stack();
       recentAmlFiles = new Stack();
       recentSchemeFiles = new Stack();
       writeRecentFiles();
     }
     buildRecentFileMenus();
   }
 
   private void writeRecentFiles()
   {
     try
     {
       FileOutputStream prefStream = new FileOutputStream("recentFiles.dat");
       ObjectOutputStream objStream = new ObjectOutputStream(prefStream);
       objStream.writeObject(recentTextFiles);
       objStream.writeObject(recentAmlFiles);
       objStream.writeObject(recentSchemeFiles);
       objStream.close();
     } catch (Exception ex)
     {
       System.out.println("Exception in writeRecentFiles: " + ex.toString());
     }
   }
 
   public void buildRecentFileMenus()
   {
     buildRecent(recentTextFilesMenu, recentTextFiles, preferencesDialog.params.numRecentTextFiles,
             RecentFilesMenuItem.FileType.TEXT);
     buildRecent(recentAmlFilesMenu, recentAmlFiles, preferencesDialog.params.numRecentAmlFiles,
             RecentFilesMenuItem.FileType.AML);
     buildRecent(recentSchemeFilesMenu, recentSchemeFiles, preferencesDialog.params.numRecentSchemeFiles,
             RecentFilesMenuItem.FileType.SCHEMESET);
     writeRecentFiles();
   }
 
   private void buildRecent(JMenu menu, Stack fileStack, int maxFiles, RecentFilesMenuItem.FileType type)
   {
     menu.removeAll();
     if (fileStack.size() == 0)
     {
       JMenuItem emptyMenu = new JMenuItem("(empty)");
       emptyMenu.setEnabled(false);
       emptyMenu.setFont(menuFont);
       menu.add(emptyMenu);
     } else
     {
       // Trim the stack to size specified in preferences
       for (int i = 0; i < fileStack.size() - maxFiles; i++)
       {
         fileStack.remove(0);
       }
       int fileCount = 0;
       for (int i = fileStack.size() - 1; i >= 0 && fileCount < maxFiles; i--, fileCount++)
       {
         String fileName = (String) fileStack.elementAt(i);
         RecentFilesMenuItem item = new RecentFilesMenuItem(fileName, type);
         item.setFont(menuFont);
         FileActionHandler handler = new FileActionHandler(fileName);
         item.setAction(handler);
         menu.add(item);
       }
     }
   }
 
   /**
    * Sets up the correct diagram when the tabbed pane is switched.
    * Also updates enabled/disabled state of menu & toolbar buttons.
    */
   private int setCurrentDiagram()
   {
     switch (displayTabbedPane.getSelectedIndex())
     {
       case DIAGRAM_STANDARD:
         linkMenuItem.setEnabled(true);
         unlinkMenuItem.setEnabled(true);
         refutationMenuItem.setEnabled(true);
         linkToolBar.setEnabled(true);
         unlinkToolBar.setEnabled(true);
         refutationToolBar.setEnabled(true);
         flipDiagramMenuItem.setEnabled(true);
         flipDiagramToolBar.setEnabled(true);
         modifyOwnershipMenuItem.setEnabled(true);
         modifyEvaluationMenuItem.setEnabled(true);
         showEvaluationMenuItem.setEnabled(true);
         showOwnersMenuItem.setEnabled(true);
         labelsMenu.setEnabled(true);
         schemesMenu.setEnabled(true);
         selectSchemeToolBar.setEnabled(true);
         break;
       case DIAGRAM_TOULMIN:
         linkMenuItem.setEnabled(false);
         unlinkMenuItem.setEnabled(false);
         refutationMenuItem.setEnabled(false);
         linkToolBar.setEnabled(false);
         unlinkToolBar.setEnabled(false);
         refutationToolBar.setEnabled(false);
         flipDiagramMenuItem.setEnabled(false);
         flipDiagramToolBar.setEnabled(false);
         modifyOwnershipMenuItem.setEnabled(false);
         modifyEvaluationMenuItem.setEnabled(true);
         showEvaluationMenuItem.setEnabled(false);
         showOwnersMenuItem.setEnabled(false);
         labelsMenu.setEnabled(false);
         schemesMenu.setEnabled(false);
         selectSchemeToolBar.setEnabled(false);
         break;
       case DIAGRAM_WIGMORE:
         linkMenuItem.setEnabled(false);
         unlinkMenuItem.setEnabled(false);
         refutationMenuItem.setEnabled(false);
         linkToolBar.setEnabled(false);
         unlinkToolBar.setEnabled(false);
         refutationToolBar.setEnabled(false);
         flipDiagramMenuItem.setEnabled(false);
         flipDiagramToolBar.setEnabled(false);
         modifyOwnershipMenuItem.setEnabled(false);
         modifyEvaluationMenuItem.setEnabled(false);
         showEvaluationMenuItem.setEnabled(false);
         showOwnersMenuItem.setEnabled(false);
         labelsMenu.setEnabled(false);
         schemesMenu.setEnabled(false);
         selectSchemeToolBar.setEnabled(false);
         break;
     }
     diagramStyle = displayTabbedPane.getSelectedIndex();
     if (zoomState == ZOOM_SCALED)
     {
       currentDiagram = displays[3 * displayTabbedPane.getSelectedIndex()];
       return 3 * displayTabbedPane.getSelectedIndex();
     } else if (zoomState == ZOOM_FULLSIZE)
     {
       currentDiagram = displays[3 * displayTabbedPane.getSelectedIndex() + 1];
       return 3 * displayTabbedPane.getSelectedIndex() + 1;
     } else if (zoomState == ZOOM_FULLTEXT)
     {
       currentDiagram = displays[3 * displayTabbedPane.getSelectedIndex() + 2];
       return 3 * displayTabbedPane.getSelectedIndex() + 2;
     }
     return -1;
   }
   JPanel standardPanel = new JPanel();
   JPanel toulminPanel = new JPanel();
   JPanel wigmorePanel = new JPanel();
   CardLayout standardCard = new CardLayout();
   CardLayout toulminCard = new CardLayout();
   CardLayout wigmoreCard = new CardLayout();
 
   private void initComps()
   {
     // Main GUI components
     displayTabbedPane = new javax.swing.JTabbedPane();
     // Diagram panels
     //
     for (int i = 0; i < numDiagrams; i++)
     {
       displays[i] = new DisplayFrame();
       displays[i].setFreeVertexPanel(new FreeVertexPanel());
       displays[i].setControlFrame(this);
     }
     displays[0].setMainDiagramPanel(new FullPanel());
     displays[1].setMainDiagramPanel(new FullSizePanel());
     displays[2].setMainDiagramPanel(new FullTextPanel());
     displays[3].setMainDiagramPanel(new ToulminScaledPanel());
     displays[4].setMainDiagramPanel(new ToulminFullSizePanel());
     displays[5].setMainDiagramPanel(new ToulminFullTextPanel());
     displays[6].setMainDiagramPanel(new WigmoreScaledPanel());
     displays[7].setMainDiagramPanel(new WigmoreFullSizePanel());
     displays[8].setMainDiagramPanel(new WigmoreFullTextPanel());
     for (int i = 0; i < numDiagrams; i++)
     {
       displays[i].setAraucaria(this);
     }
 
     // Set starting diagram to Standard diagram
     currentDiagram = displays[0];
 
     //
     // Display panel
     //
     standardPanel.setLayout(standardCard);
     standardPanel.add("StandardScaled", displays[0]);
     standardPanel.add("StandardFullSize", displays[1]);
     standardPanel.add("StandardFullText", displays[2]);
     standardCard.show(standardPanel, "StandardFullText");
 
     toulminPanel.setLayout(toulminCard);
     toulminPanel.add("ToulminScaled", displays[3]);
     toulminPanel.add("ToulminFullSize", displays[4]);
     toulminPanel.add("ToulminFullText", displays[5]);
     toulminCard.show(toulminPanel, "ToulminFullText");
 
     wigmorePanel.setLayout(wigmoreCard);
     wigmorePanel.add("WigmoreScaled", displays[6]);
     wigmorePanel.add("WigmoreFullSize", displays[7]);
     wigmorePanel.add("WigmoreFullText", displays[8]);
     wigmoreCard.show(wigmorePanel, "WigmoreFullText");
 
 
     displayTabbedPane.addTab("Standard", null, standardPanel, "Standard diagrams");
     displayTabbedPane.addTab("Toulmin", null, toulminPanel, "Toulmin diagrams");
     displayTabbedPane.addTab("Wigmore", null, wigmorePanel, "Wigmore diagrams");
     displayTabbedPane.setSelectedIndex(0);
     setCurrentDiagram();
 
     JPanel displayPanel = new JPanel(new BorderLayout());
     JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
     scaledRadio = new JRadioButton("Scaled");
     fullSizeRadio = new JRadioButton("Full size");
     fullTextRadio = new JRadioButton("Full text");
     fullTextRadio.setSelected(true);
     ButtonGroup displayGroup = new ButtonGroup();
     displayGroup.add(scaledRadio);
     displayGroup.add(fullSizeRadio);
     displayGroup.add(fullTextRadio);
     radioPanel.add(scaledRadio);
     radioPanel.add(fullSizeRadio);
     radioPanel.add(fullTextRadio);
     Action scaledRadioAction = new FileActionHandler("Scaled");
     scaledRadioAction.putValue(Action.SHORT_DESCRIPTION, "Display scaled diagram");
     scaledRadio.setAction(scaledRadioAction);
     Action fullSizeRadioAction = new FileActionHandler("Full size");
     fullSizeRadioAction.putValue(Action.SHORT_DESCRIPTION, "Display full size diagram");
     fullSizeRadio.setAction(fullSizeRadioAction);
     Action fullTextRadioAction = new FileActionHandler("Full text");
     fullTextRadioAction.putValue(Action.SHORT_DESCRIPTION, "Display full text diagram");
     fullTextRadio.setAction(fullTextRadioAction);
 
     //displayPanel.add(radioPanel, BorderLayout.NORTH);
     displayPanel.add(displayTabbedPane, BorderLayout.CENTER);
     getContentPane().add(displayPanel, java.awt.BorderLayout.CENTER);
     // Each time the diagram is changed, the currentDiagram is updated
     displayTabbedPane.addChangeListener(new ChangeListener()
     {
       public void stateChanged(ChangeEvent e)
       {
         setCurrentDiagram();
         updateDisplays(true);
       }
     });
 
 
     // Text panel
     selectTextScroll = new javax.swing.JScrollPane();
     selectText = new SelectText();
     selectText.setPreferredSize(new Dimension(SelectText.PREFERRED_WIDTH, SelectText.PREFERRED_HEIGHT));
 
     setTitle(mainFrameTitle + " (no file loaded)");
     setName("mainFrame");
     setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
     addWindowListener(new java.awt.event.WindowAdapter()
     {
       @Override
       public void windowClosing(java.awt.event.WindowEvent evt)
       {
         exitApplication();
       }
     });
 
     //
     // Toolbar initialization
     //
     mainToolBar.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
     mainToolBar.setFloatable(false);
     // Open text
     Action openTextAction = new FileActionHandler("", new ImageIcon("images/OpenDoc.gif"));
     openTextAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_T, ActionEvent.CTRL_MASK));
     openTextAction.putValue(Action.SHORT_DESCRIPTION, "Open text file");
     openTextToolBar.setAction(openTextAction);
     mainToolBar.add(openTextToolBar);
     // Open argument
     Action openArgAction = new FileActionHandler("", new ImageIcon("images/OpenArrow.gif"));
     openArgAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_O, ActionEvent.CTRL_MASK));
     openArgAction.putValue(Action.SHORT_DESCRIPTION, "Open argument");
     openArgumentToolBar.setAction(openArgAction);
     mainToolBar.add(openArgumentToolBar);
     // Save argument
     Action saveArgAction = new FileActionHandler("", new ImageIcon("images/FolderIn.gif"));
     saveArgAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_S, ActionEvent.CTRL_MASK));
     saveArgAction.putValue(Action.SHORT_DESCRIPTION, "Save argument");
     saveArgumentToolBar.setAction(saveArgAction);
     mainToolBar.add(saveArgumentToolBar);
     // Save As argument
     Action saveAsArgAction = new FileActionHandler("");
     saveAsArgAction.putValue(Action.SHORT_DESCRIPTION, "Save argument as new file");
     // Save diagram
     Action saveDiagramAction = new FileActionHandler("", new ImageIcon("images/DocumentIn.gif"));
     saveDiagramAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_D, ActionEvent.CTRL_MASK));
     saveDiagramAction.putValue(Action.SHORT_DESCRIPTION, "Save image file");
     saveDiagramToolBar.setAction(saveDiagramAction);
     mainToolBar.add(saveDiagramToolBar);
     // Close all
     Action closeAllAction = new FileActionHandler("");
     closeAllAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_N, ActionEvent.CTRL_MASK));
     closeAllAction.putValue(Action.SHORT_DESCRIPTION, "Clear text and displays");
     // Properties
     Action propertiesAction = new FileActionHandler("");
     propertiesAction.putValue(Action.SHORT_DESCRIPTION, "Properties");
     // Preferences
     Action preferencesAction = new FileActionHandler("");
     preferencesAction.putValue(Action.SHORT_DESCRIPTION, "Set preferences");
     // Exit
     Action exitAction = new FileActionHandler("");
     exitAction.putValue(Action.SHORT_DESCRIPTION, "Exit program");
 
     mainToolBar.add(new JToolBar.Separator());
 
     // Edit toolbar
     // Undo
     Action undoAction = new FileActionHandler("", new ImageIcon("images/Undo20.gif"));
     undoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
     undoAction.putValue(Action.SHORT_DESCRIPTION, "Undo edits");
     undoToolBar.setAction(undoAction);
     undoToolBar.setEnabled(false);
     mainToolBar.add(undoToolBar);
     // Redo
     Action redoAction = new FileActionHandler("", new ImageIcon("images/Redo20.gif"));
     redoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
     redoAction.putValue(Action.SHORT_DESCRIPTION, "Redo undone edits");
     redoToolBar.setAction(redoAction);
     redoToolBar.setEnabled(false);
     mainToolBar.add(redoToolBar);
     // Clear diagram
     Action clearDiagramAction = new FileActionHandler("", new ImageIcon("images/Document.gif"));
     clearDiagramAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_C, ActionEvent.CTRL_MASK));
     clearDiagramAction.putValue(Action.SHORT_DESCRIPTION, "Clear argument diagram - retain text");
     clearDiagramToolBar.setAction(clearDiagramAction);
     mainToolBar.add(clearDiagramToolBar);
     // Flip diagram
     Action flipDiagramAction = new FileActionHandler("", new ImageIcon("images/DocumentDiagram.gif"));
     flipDiagramAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_F, ActionEvent.CTRL_MASK));
     flipDiagramAction.putValue(Action.SHORT_DESCRIPTION, "Invert the diagram");
     flipDiagramToolBar.setAction(flipDiagramAction);
     mainToolBar.add(flipDiagramToolBar);
     // Missing premise
     Action missingPremiseAction = new FileActionHandler("", new ImageIcon("images/RotCCRight.gif"));
     missingPremiseAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_M, ActionEvent.CTRL_MASK));
     missingPremiseAction.putValue(Action.SHORT_DESCRIPTION, "Add a missing premise (enthymeme)");
     missingPremiseToolBar.setAction(missingPremiseAction);
     mainToolBar.add(missingPremiseToolBar);
     // Refutation
     Action refutationAction = new FileActionHandler("", new ImageIcon("images/Widen.gif"));
     refutationAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_R, ActionEvent.CTRL_MASK));
     refutationAction.putValue(Action.SHORT_DESCRIPTION, "Toggle refutation");
     refutationToolBar.setAction(refutationAction);
     mainToolBar.add(refutationToolBar);
     // Delete selected items
     Action deleteAction = new FileActionHandler("", new ImageIcon("images/Error.gif"));
     deleteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_DELETE, 0));
     deleteAction.putValue(Action.SHORT_DESCRIPTION, "Delete selected items");
     deleteToolBar.setAction(deleteAction);
     mainToolBar.add(deleteToolBar);
     // Link premises
     Action linkAction = new FileActionHandler("", new ImageIcon("images/Pin.gif"));
     linkAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_L, ActionEvent.CTRL_MASK));
     linkAction.putValue(Action.SHORT_DESCRIPTION, "Link selected statements");
     linkToolBar.setAction(linkAction);
     mainToolBar.add(linkToolBar);
     // Unlink premises
     Action unlinkAction = new FileActionHandler("", new ImageIcon("images/PinLeft.gif"));
     unlinkAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_U, ActionEvent.CTRL_MASK));
     unlinkAction.putValue(Action.SHORT_DESCRIPTION, "Unlink selected statements");
     unlinkToolBar.setAction(unlinkAction);
     mainToolBar.add(unlinkToolBar);
     // Select all vertices
     Action selectAllAction = new FileActionHandler("");
     selectAllAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_A, ActionEvent.CTRL_MASK));
     selectAllAction.putValue(Action.SHORT_DESCRIPTION, "Select all nodes");
 
     // Labels menu
     //
     // Modify ownership
     Action modifyOwnershipAction = new FileActionHandler("");
     modifyOwnershipAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_W, ActionEvent.CTRL_MASK));
     modifyOwnershipAction.putValue(Action.SHORT_DESCRIPTION, "Add or edit owners of statements");
     // Show owners
     showOwnersAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_H, ActionEvent.CTRL_MASK));
     showOwnersAction.putValue(Action.SHORT_DESCRIPTION, "Toggle display of owners");
     // Modify evaluation
     Action modifyEvaluationAction = new FileActionHandler("");
     modifyEvaluationAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_K, ActionEvent.CTRL_MASK));
     modifyEvaluationAction.putValue(Action.SHORT_DESCRIPTION, "Add or edit evaluation");
     // Show evaluation
     showEvaluationAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_G, ActionEvent.CTRL_MASK));
     showEvaluationAction.putValue(Action.SHORT_DESCRIPTION, "Toggle display of evaluation");
 
     // Schemes menu
     //
     // Select schemes
     mainToolBar.add(new JToolBar.Separator());
     Action selectSchemeAction = new FileActionHandler("", new ImageIcon("images/FlowGraph.gif"));
     selectSchemeAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_E, ActionEvent.CTRL_MASK));
     selectSchemeAction.putValue(Action.SHORT_DESCRIPTION, "Select a scheme");
     selectSchemeToolBar.setAction(selectSchemeAction);
     mainToolBar.add(selectSchemeToolBar);
     // Add/edit schemes
     Action addEditSchemeAction = new FileActionHandler("");
     addEditSchemeAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_I, ActionEvent.CTRL_MASK));
     addEditSchemeAction.putValue(Action.SHORT_DESCRIPTION, "Add or edit a scheme");
     // Open schemeset
     Action openSchemesetAction = new FileActionHandler("");
     openSchemesetAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_P, ActionEvent.CTRL_MASK));
     openSchemesetAction.putValue(Action.SHORT_DESCRIPTION, "Open schemeset");
     // Save schemeset
     Action saveSchemesetAction = new FileActionHandler("");
     saveSchemesetAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_V, ActionEvent.CTRL_MASK));
     saveSchemesetAction.putValue(Action.SHORT_DESCRIPTION, "Save schemeset");
     // Show evaluation
     showCQsAction.putValue(Action.SHORT_DESCRIPTION, "Toggle display of CQs answered");
 
     // AraucariaDB menu
     //
     // Login
     Action loginAction = new FileActionHandler("");
     loginAction.putValue(Action.SHORT_DESCRIPTION, "Login to database");
     // Register
     Action registerAction = new FileActionHandler("");
     registerAction.putValue(Action.SHORT_DESCRIPTION, "Register in database");
     // Save to DB
     mainToolBar.add(new JToolBar.Separator());
     Action saveDBAction = new FileActionHandler("", new ImageIcon("images/DataStore.gif"));
     saveDBAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_B, ActionEvent.CTRL_MASK));
     saveDBAction.putValue(Action.SHORT_DESCRIPTION, "Save to database");
     saveDBToolBar.setAction(saveDBAction);
     mainToolBar.add(saveDBToolBar);
     // Search DB
     Action searchDBAction = new FileActionHandler("", new ImageIcon("images/DataQuery2.gif"));
     searchDBAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
     searchDBAction.putValue(Action.SHORT_DESCRIPTION, "Search database");
     searchDBToolBar.setAction(searchDBAction);
     mainToolBar.add(searchDBToolBar);
 
     // ComboBox for displaying search results
     searchResultCombo = new JComboBox();
     searchResultCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
     searchResultCombo.addActionListener(new FileActionHandler());
     String comboSizeString = "2. If any journalists learn about the invasion,";
     searchResultCombo.setPrototypeDisplayValue(comboSizeString);
     mainToolBar.add(searchResultCombo);
 
     // Tutoring
     // Premise endpoint selection
     Action premiseEndpointsAction = new FileActionHandler("");
     premiseEndpointsAction.putValue(Action.SHORT_DESCRIPTION, "Premise endpoints");
     // Marking
     Action markingAction = new FileActionHandler("");
     markingAction.putValue(Action.SHORT_DESCRIPTION, "Marking");
 
     // Help
     Action helpAction = new FileActionHandler("", new ImageIcon("images/Help.gif"));
     helpAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
             KeyEvent.VK_F1, 0));
     helpAction.putValue(Action.SHORT_DESCRIPTION, "Help");
     helpToolBar.setAction(helpAction);
     mainToolBar.add(helpToolBar);
     // About
     Action aboutAction = new FileActionHandler("");
     aboutAction.putValue(Action.SHORT_DESCRIPTION, "Help");
 
     getContentPane().add(mainToolBar, java.awt.BorderLayout.NORTH);
 
     //
     // Menu initialization
     //
     // File menu
     fileMenu.setMnemonic('F');
     fileMenu.setText("File");
     // Open text menu
     openTextMenuItem.setAction(openTextAction);
     openTextMenuItem.setMnemonic('T');
     openTextMenuItem.setText("Open text file");
     fileMenu.add(openTextMenuItem);
     // Open argument menu
     openArgumentMenuItem.setAction(openArgAction);
     openArgumentMenuItem.setMnemonic('O');
     openArgumentMenuItem.setText("Open argument");
     fileMenu.add(openArgumentMenuItem);
     // Save argument menu
     saveArgumentMenuItem.setAction(saveArgAction);
     saveArgumentMenuItem.setMnemonic('S');
     saveArgumentMenuItem.setText("Save argument");
     fileMenu.add(saveArgumentMenuItem);
     // Save As argument menu
     saveAsArgumentMenuItem.setAction(saveAsArgAction);
     saveAsArgumentMenuItem.setMnemonic('A');
     saveAsArgumentMenuItem.setText("Save argument as...");
     fileMenu.add(saveAsArgumentMenuItem);
     // Diagram menu
     saveDiagramMenuItem.setAction(saveDiagramAction);
     saveDiagramMenuItem.setMnemonic('D');
     saveDiagramMenuItem.setText("Save diagram");
     fileMenu.add(saveDiagramMenuItem);
     // Close all menu
     closeAllMenuItem.setAction(closeAllAction);
     closeAllMenuItem.setMnemonic('C');
     closeAllMenuItem.setText("Close all");
     fileMenu.add(closeAllMenuItem);
     // Properties
     propertiesMenu.setAction(propertiesAction);
     propertiesMenu.setMnemonic('r');
     propertiesMenu.setText("Properties");
     fileMenu.add(propertiesMenu);
     // Preferences
     preferencesMenuItem.setAction(preferencesAction);
     preferencesMenuItem.setMnemonic('P');
     preferencesMenuItem.setText("Preferences");
     fileMenu.add(preferencesMenuItem);
     // Recent file menus
     recentTextFilesMenu = new JMenu("Open recent text");
     recentAmlFilesMenu = new JMenu("Open recent argument");
     fileMenu.add(new JSeparator());
     fileMenu.add(recentTextFilesMenu);
     fileMenu.add(recentAmlFilesMenu);
     // Exit menu
     fileMenu.add(new JSeparator());
     exitMenuItem.setAction(exitAction);
     exitMenuItem.setMnemonic('x');
     exitMenuItem.setText("Exit");
     fileMenu.add(exitMenuItem);
 
     // Edit menu
     //
     editMenu.setMnemonic('E');
     editMenu.setText("Edit");
     // Undo menu
     undoMenuItem.setAction(undoAction);
     undoMenuItem.setMnemonic('n');
     undoMenuItem.setText("Undo");
     undoMenuItem.setEnabled(false);
     editMenu.add(undoMenuItem);
     // Redo menu
     redoMenuItem.setAction(redoAction);
     redoMenuItem.setMnemonic('o');
     redoMenuItem.setText("Redo");
     redoMenuItem.setEnabled(false);
     editMenu.add(redoMenuItem);
     // Clear diagram menu
     editMenu.add(new JSeparator());
     clearDiagramMenuItem.setAction(clearDiagramAction);
     clearDiagramMenuItem.setMnemonic('C');
     clearDiagramMenuItem.setText("Clear diagram");
     editMenu.add(clearDiagramMenuItem);
     // Flip diagram menu
     flipDiagramMenuItem.setAction(flipDiagramAction);
     flipDiagramMenuItem.setMnemonic('F');
     flipDiagramMenuItem.setText("Flip diagram");
     editMenu.add(flipDiagramMenuItem);
     // Missing premise menu
     missingPremiseMenuItem.setAction(missingPremiseAction);
     missingPremiseMenuItem.setMnemonic('M');
     missingPremiseMenuItem.setText("Missing premise");
     editMenu.add(missingPremiseMenuItem);
     // Refutation menu
     refutationMenuItem.setAction(refutationAction);
     refutationMenuItem.setMnemonic('R');
     refutationMenuItem.setText("Refutation");
     editMenu.add(refutationMenuItem);
     // Delete selected items menu
     deleteMenuItem.setAction(deleteAction);
     deleteMenuItem.setMnemonic('D');
     deleteMenuItem.setText("Delete");
     editMenu.add(deleteMenuItem);
     // Link menu
     linkMenuItem.setAction(linkAction);
     linkMenuItem.setMnemonic('L');
     linkMenuItem.setText("Link statements");
     editMenu.add(linkMenuItem);
     // Unlink menu
     unlinkMenuItem.setAction(unlinkAction);
     unlinkMenuItem.setMnemonic('U');
     unlinkMenuItem.setText("Unlink statements");
     editMenu.add(unlinkMenuItem);
     // Select all menu
     selectAllMenuItem.setAction(selectAllAction);
     selectAllMenuItem.setMnemonic('a');
     selectAllMenuItem.setText("Select all nodes");
     editMenu.add(selectAllMenuItem);
 
     // View menu
     //
     viewMenu.setMnemonic('V');
     viewMenu.setText("View");
     // Zoom menu (scaled / full size / full text)
 //    zoomMenu.setMnemonic('Z');
     zoomMenu.setText("Zoom");
     Action scaledMenuAction = new FileActionHandler("");
     scaledMenuAction.putValue(Action.SHORT_DESCRIPTION, "Scale diagram to window");
     scaledMenuItem.setAction(scaledMenuAction);
 //    scaledMenuItem.setMnemonic('S');
     scaledMenuItem.setText("Scaled");
     zoomMenu.add(scaledMenuItem);
 
     Action fullSizeMenuAction = new FileActionHandler("");
     fullSizeMenuAction.putValue(Action.SHORT_DESCRIPTION, "Fixed size diagram");
     fullSizeMenuItem.setAction(fullSizeMenuAction);
 //    fullSizeMenuItem.setMnemonic('Z');
     fullSizeMenuItem.setText("Full size");
     zoomMenu.add(fullSizeMenuItem);
 
     Action fullTextMenuAction = new FileActionHandler("");
     fullTextMenuAction.putValue(Action.SHORT_DESCRIPTION, "Full text diagram");
     fullTextMenuItem.setAction(fullTextMenuAction);
 //    fullTextMenuItem.setMnemonic('T');
     fullTextMenuItem.setText("Full text");
     zoomMenu.add(fullTextMenuItem);
 
     viewMenu.add(zoomMenu);
     //
     // Style menu (standard / toulmin / etc)
 //    styleMenu.setMnemonic('S');
     styleMenu.setText("Style");
     Action standardAction = new FileActionHandler("");
     standardAction.putValue(Action.SHORT_DESCRIPTION, "Show standard diagram");
     standardMenuItem.setAction(standardAction);
 //    standardMenuItem.setMnemonic('S');
     standardMenuItem.setText("Standard");
     styleMenu.add(standardMenuItem);
 
     Action toulminAction = new FileActionHandler("");
     toulminAction.putValue(Action.SHORT_DESCRIPTION, "Show Toulmin diagram");
     toulminMenuItem.setAction(toulminAction);
 //    toulminMenuItem.setMnemonic('T');
     toulminMenuItem.setText("Toulmin");
     styleMenu.add(toulminMenuItem);
 
     Action wigmoreAction = new FileActionHandler("");
     wigmoreAction.putValue(Action.SHORT_DESCRIPTION, "Show Wigmore diagram");
     wigmoreMenuItem.setAction(wigmoreAction);
 //    wigmoreMenuItem.setMnemonic('W');
     wigmoreMenuItem.setText("Wigmore");
     styleMenu.add(wigmoreMenuItem);
 
 //    pollockMenuItem.setMnemonic('P');
     pollockMenuItem.setText("Pollock");
     //styleMenu.add(pollockMenuItem);
     pollockMenuItem.setEnabled(false);
     viewMenu.add(styleMenu);
 
 
     // Labels menu
     //
     labelsMenu.setMnemonic('L');
     labelsMenu.setText("Labels");
     // Modify ownership menu
     modifyOwnershipMenuItem.setAction(modifyOwnershipAction);
     modifyOwnershipMenuItem.setMnemonic('w');
     modifyOwnershipMenuItem.setText("Modify ownership");
     labelsMenu.add(modifyOwnershipMenuItem);
     // Show owners menu
     showOwnersMenuItem.setAction(showOwnersAction);
     showOwnersMenuItem.setMnemonic('H');
     showOwnersMenuItem.setText("Hide owners");
     labelsMenu.add(showOwnersMenuItem);
     // Modify evaluation menu
     labelsMenu.add(new JSeparator());
     modifyEvaluationMenuItem.setAction(modifyEvaluationAction);
     modifyEvaluationMenuItem.setMnemonic('o');
     modifyEvaluationMenuItem.setText("Modify evaluation");
     labelsMenu.add(modifyEvaluationMenuItem);
     // Show evaluation menu
     showEvaluationMenuItem.setAction(showEvaluationAction);
     showEvaluationMenuItem.setMnemonic('i');
     showEvaluationMenuItem.setText("Hide evaluation");
     labelsMenu.add(showEvaluationMenuItem);
 
     // Schemes menu
     //
     schemesMenu.setMnemonic('S');
     schemesMenu.setText("Schemes");
     // Select scheme menu
     selectSchemeMenuItem.setAction(selectSchemeAction);
     selectSchemeMenuItem.setMnemonic('e');
     selectSchemeMenuItem.setText("Select scheme");
     schemesMenu.add(selectSchemeMenuItem);
     // Add/edit scheme menu
     addEditSchemeMenuItem.setAction(addEditSchemeAction);
     addEditSchemeMenuItem.setMnemonic('i');
     addEditSchemeMenuItem.setText("Add/edit scheme");
     schemesMenu.add(addEditSchemeMenuItem);
     // Open schemeset menu
     schemesMenu.add(new JSeparator());
     openSchemesetMenuItem.setAction(openSchemesetAction);
     openSchemesetMenuItem.setMnemonic('p');
     openSchemesetMenuItem.setText("Open schemeset");
     schemesMenu.add(openSchemesetMenuItem);
     recentSchemeFilesMenu = new JMenu("Open recent schemeset");
     schemesMenu.add(recentSchemeFilesMenu);
     // Save schemeset menu
     saveSchemesetMenuItem.setAction(saveSchemesetAction);
     saveSchemesetMenuItem.setMnemonic('v');
     saveSchemesetMenuItem.setText("Save schemeset");
     schemesMenu.add(saveSchemesetMenuItem);
     // Show CQs menu
     showCQsMenuItem.setAction(showCQsAction);
     showCQsMenuItem.setMnemonic('q');
     showCQsMenuItem.setText("Show critical questions");
     schemesMenu.add(showCQsMenuItem);
 
     // AraucariaDB menu
     //
     araucariaDBMenu.setMnemonic('D');
     araucariaDBMenu.setText("AraucariaDB");
     // Login menu
     loginMenuItem.setAction(loginAction);
     loginMenuItem.setMnemonic('L');
     loginMenuItem.setText("Login");
     araucariaDBMenu.add(loginMenuItem);
     // Register menu
     registerMenuItem.setAction(registerAction);
     registerMenuItem.setMnemonic('R');
     registerMenuItem.setText("Register");
     araucariaDBMenu.add(registerMenuItem);
     // Save to DB menu
     araucariaDBMenu.add(new JSeparator());
     saveDBMenuItem.setAction(saveDBAction);
     saveDBMenuItem.setMnemonic('S');
     saveDBMenuItem.setText("Save to database");
     araucariaDBMenu.add(saveDBMenuItem);
     // Search DB menu
     searchDBMenuItem.setAction(searchDBAction);
     searchDBMenuItem.setMnemonic('e');
     searchDBMenuItem.setText("Search database");
     araucariaDBMenu.add(searchDBMenuItem);
 
     // Tutoring menu
     tutorMenu.setText("Tutoring");
     tutorMenu.setMnemonic('T');
     // Premise endpoints
     premiseEndpointsMenu.setAction(premiseEndpointsAction);
     premiseEndpointsMenu.setText("Premise endpoints");
     premiseEndpointsMenu.setMnemonic('P');
     tutorMenu.add(premiseEndpointsMenu);
     // Marking
     markingMenu.setAction(markingAction);
     markingMenu.setText("Marking");
     markingMenu.setMnemonic('M');
     tutorMenu.add(markingMenu);
 
     // Help menu
     //
     helpMenu.setMnemonic('H');
     helpMenu.setText("Help");
     // Help menu item
     helpMenuItem.setAction(helpAction);
     helpMenuItem.setText("Help");
     helpMenu.add(helpMenuItem);
     // About menu item
     aboutMenuItem.setAction(aboutAction);
     aboutMenuItem.setMnemonic('A');
     aboutMenuItem.setText("About");
     helpMenu.add(aboutMenuItem);
 
     // Add menus to menu bar
     mainMenuBar.add(fileMenu);
     mainMenuBar.add(editMenu);
     mainMenuBar.add(viewMenu);
     mainMenuBar.add(labelsMenu);
     mainMenuBar.add(schemesMenu);
     mainMenuBar.add(araucariaDBMenu);
     mainMenuBar.add(tutorMenu);
     tutorMenu.setVisible(tutorModeOn);
     mainMenuBar.add(helpMenu);
     // Add menu bar to main frame
     setJMenuBar(mainMenuBar);
     for (int i = 0; i < mainMenuBar.getMenuCount(); i++)
     {
       JMenu menu = mainMenuBar.getMenu(i);
       setMenuFont(menu);
     }
 
     //
     // Text panel
     //
     selectTextScroll.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
     selectTextScroll.setViewportBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
     selectTextScroll.setViewportView(selectText);
     Dimension selectTextDim = selectText.getPreferredSize();
     Dimension selectTextScrollDim = new Dimension(selectTextDim.width +
             selectTextScroll.getVerticalScrollBar().getMaximumSize().width, selectTextDim.height);
     selectTextScroll.setPreferredSize(selectTextScrollDim);
     // Scrolls the text faster with mouse wheel
     selectTextScroll.getVerticalScrollBar().setUnitIncrement(30);
 
     getContentPane().add(selectTextScroll, java.awt.BorderLayout.WEST);
 
     // Status bar
     messagePanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
     messagePanel.setBackground(new java.awt.Color(255, 255, 153));
     messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
     messageLabel.setText("Messages will be displayed here.");
     messageLabel.setToolTipText("Messages are displayed here");
     messagePanel.add(messageLabel);
     getContentPane().add(messagePanel, java.awt.BorderLayout.SOUTH);
 
     // General main frame properties
     java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
     setBounds((screenSize.width - ARAU_WIDTH) / 2, (screenSize.height - ARAU_HEIGHT) / 2, ARAU_WIDTH, ARAU_HEIGHT);
   }
 
   /** Exit the Application */
   private void exitForm(java.awt.event.WindowEvent evt)//GEN-FIRST:event_exitForm
   {
     System.exit(0);
   }//GEN-LAST:event_exitForm
   public void changeDisplay(int displayType)
   {
     switch (displayType)
     {
       case DIAGRAM_FULL:
         standardCard.show(standardPanel, "StandardScaled");
         toulminCard.show(toulminPanel, "ToulminScaled");
         wigmoreCard.show(wigmorePanel, "WigmoreScaled");
         break;
       case DIAGRAM_FULL_SIZE:
         standardCard.show(standardPanel, "StandardFullSize");
         toulminCard.show(toulminPanel, "ToulminFullSize");
         wigmoreCard.show(wigmorePanel, "WigmoreFullSize");
         break;
       case DIAGRAM_FULL_TEXT:
         standardCard.show(standardPanel, "StandardFullText");
         toulminCard.show(toulminPanel, "ToulminFullText");
         wigmoreCard.show(wigmorePanel, "WigmoreFullText");
         break;
     }
     setCurrentDiagram();
     updateDisplays(true);
   }
   Font menuFont = new Font("Sansserif", Font.PLAIN, 12);
 
   public void setMenuFont(JMenu menu)
   {
     menu.setFont(menuFont);
     for (int j = 0; j < menu.getItemCount(); j++)
     {
       Component item = menu.getItem(j);
       if (item != null)
       {
         item.setFont(menuFont);
         if (item instanceof JMenu)
         {
           setMenuFont((JMenu) item);
         }
       }
     }
 
   }
   public static final String ipAddress = "babbage.computing.dundee.ac.uk";
 
   public void resetDefaultParams()
   {
     STATUSBAR_BACKGROUND = new Color(1.0f, 1.0f, 0.7f);
     DiagramBase.DIAGRAM_BACKGROUND = Color.white;
     SelectText.TEXT_BACKGROUND = new Color(0.75f, 1.0f, 1.0f);
     DiagramBase.FREE_VERTEX_BACKGROUND = new Color(1.0f, 1.0f, 0.8f);
     String homePath = araucariaHome.getAbsolutePath();
     int homeDot = homePath.indexOf(File.separator + ".");
     if (homeDot > 0)
     {
       homePath = homePath.substring(0, homeDot);
     }
     textDirectory = homePath + File.separator + "TextFiles";
     amlDirectory = homePath + File.separator + "AMLFiles";
     schemeDirectory = homePath + File.separator + "SchemeFiles";
     databaseName = "araucaria_v1_0";
     username = "araucaria_client";
     password = "";
     databaseType = MYSQL;
     initDatabase();
   }
 
   public JScrollPane getSelectTextScroll()
   {
     return selectTextScroll;
   }
 
   public SelectText getSelectText()
   {
     return selectText;
   }
 
   public Argument getArgument()
   {
     return argument;
   }
 
   public void setMessageLabelText(String s)
   {
     messageLabel.setText(s);
   }
 
   private String trimPathName(String path)
   {
     int homeDot = path.indexOf(File.separator + ".");
     if (homeDot > 0)
     {
       path = path.substring(0, homeDot);
     }
     return path;
   }
   ExtensionFileFilter extFilter = new ExtensionFileFilter();
   ExtensionFileFilter jpgFilter = new ExtensionFileFilter("jpg", "JPEG Images");
   ExtensionFileFilter tifFilter = new ExtensionFileFilter("tif", "TIFF Images");
 
   private void initDialogs()
   {
     File currentDir = new File(".");
     lastDirectory = new File(currentDir.getAbsolutePath());
     araucariaHome = new File(currentDir.getAbsolutePath());
     String homePath = trimPathName(araucariaHome.getAbsolutePath());
     textDirectory = homePath + File.separator + "TextFiles";
     amlDirectory = homePath + File.separator + "AMLFiles";
     schemeDirectory = homePath + File.separator + "SchemeFiles";
 
     // Add extension filters for the file dialogs
     tiffChoice = new JFileChooser();
     tiffChoice.addChoosableFileFilter(jpgFilter);
     tiffChoice.addChoosableFileFilter(tifFilter);
     tiffChoice.setDialogTitle("Save image");
 
     extFilter.addExtension("txt");
     fileChoice = new JFileChooser();
     fileChoice.setFileFilter(extFilter);
 
     extFilter = new ExtensionFileFilter();
     extFilter.addExtension("aml");
     extFilter.addExtension("aif");
     amlChoice = new JFileChooser();
     amlChoice.setFileFilter(extFilter);
 
 
     extFilter = new ExtensionFileFilter();
     extFilter.addExtension("scm");
     schemeChoice = new JFileChooser();
     schemeChoice.setFileFilter(extFilter);
   }
 
   /**
    * Set up database parameters - default DB is MySQL on Chris's machine
    */
   public void initDatabase()
   {
     //ipAddress = "babbage.computing.dundee.ac.uk";
     databaseName = "araucaria_v1_0";
     username = "araucaria_client";
     password = "";
     databaseType = MYSQL;
     this.dbAddress = null;
     if (databaseType == MYSQL)
     {
       try
       {
         Class.forName("org.gjt.mm.mysql.Driver").newInstance();
       } catch (Exception ex)
       {
         ex.printStackTrace();
       }
     } else if (databaseType == SQLSERVER)
     {
       try
       {
         Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
       } catch (Exception ex)
       {
         ex.printStackTrace();
       }
     }
   }
 
   /**
    * Opens a text file for display in a SelectText panel. The text should be in standard UTF-8
    * format.
    */
   public void openTextFile()
   {
     File chosenFile = null;
     FileInputStream chosenInput = null;
 
     if (argument.getTree().getRoots().size() > 0)
     {
       int action = JOptionPane.showConfirmDialog(Araucaria.this,
               "<html><center><font color=red face=helvetica><b>Loading new text will erase the current tree.<br> " +
               "THIS ACTION CANNOT BE UNDONE<br>" +
               "Do you want to continue?</b></face></center></html>", "Delete current tree?",
               JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
       if (action == 1)
       {
         messageLabel.setText("Text not loaded.");
         return;
       }
     }
 
     try
     {
       fileChoice.setDialogTitle("Select text file");
       fileChoice.setFont(new Font("Courier New", Font.ITALIC, 12));
       // Sets directory to text directory specified in preferences
       fileChoice.setCurrentDirectory(new File(textDirectory));
       if (fileChoice.showOpenDialog(Araucaria.this) ==
               JFileChooser.APPROVE_OPTION)
       {
         textDirectory = fileChoice.getCurrentDirectory().getAbsolutePath();
         chosenFile = fileChoice.getSelectedFile();
         // See if user has typed in the .txt suffix. If not, add it.
         if (chosenFile.getPath().indexOf(".txt") == -1)
         {
           String newPath = chosenFile.getPath() + ".txt";
           chosenFile = new File(newPath);
         }
         if (!chosenFile.exists())
         {
           messageLabel.setText("Text file " + chosenFile.getName() + " not found.");
           return;
         }
         chosenInput = new FileInputStream(chosenFile);
         recentTextFiles.push(chosenFile.getAbsolutePath());
         buildRecentFileMenus();
       } else
       {
         return;
       }
     } catch (Exception e)
     {
       System.out.println(e.getMessage());
     }
 //    wordCount = selectText.readText(chosenInput, (int)chosenFile.length());
     argument.emptyTree(true);
     selectText.readText(chosenInput, (int) chosenFile.length());
     argument.setText(selectText.getText());
     updateSelectText();
     updateDisplays(true);
 
     clearUndoStack();
     // Clear current AML file to prevent overwriting with new text.
     currentOpenXMLFile = null;
     messageLabel.setText("Text file " + chosenFile.getName() + " opened successfully");
   }
 
   private void openRecentTextFile(String fileName)
   {
     File chosenFile = new File(fileName);
     if (!chosenFile.exists())
     {
       messageLabel.setText("Text file " + chosenFile.getName() + " not found.");
       recentTextFiles.remove(fileName);
       buildRecentFileMenus();
       return;
     }
     try
     {
       FileInputStream chosenInput = new FileInputStream(chosenFile);
       argument.emptyTree(true);
       selectText.readText(chosenInput, (int) chosenFile.length());
       argument.setText(selectText.getText());
       updateSelectText();
       updateDisplays(true);
 
       clearUndoStack();
       // Clear current AML file to prevent overwriting with new text.
       currentOpenXMLFile = null;
       messageLabel.setText("Text file " + chosenFile.getName() + " opened successfully");
       recentTextFiles.remove(fileName);
       recentTextFiles.push(fileName);
       buildRecentFileMenus();
     } catch (Exception e)
     {
       System.out.println(e.getMessage());
     }
   }
 
   /**
    * Reads in an AML file containing data for markup of a single argument.
    */
   public void readAML()
   {
     File saveDirectory = null;
     String fileName = "";
     File chosenFile = null;
     amlChoice = new JFileChooser();
     FileNameExtensionFilter aifFilter = new FileNameExtensionFilter("AIF files", aifFileSuffix);
     amlChoice.addChoosableFileFilter(aifFilter);
     FileNameExtensionFilter amlFilter = new FileNameExtensionFilter("AML files", amlFileSuffix);
     amlChoice.addChoosableFileFilter(amlFilter);
     amlChoice.setDialogTitle("Open argument");
     amlChoice.setCurrentDirectory(new File(amlDirectory));
     try
     {
 
       if (amlChoice.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
       {
         return;
       }
       amlDirectory = amlChoice.getCurrentDirectory().getAbsolutePath();
       chosenFile = amlChoice.getSelectedFile();
       // See if user has typed in the .aml suffix. If not, add it.
       // Check that a file type has been specified. If not, append correct suffix
       if (amlChoice.getFileFilter() == amlFilter && chosenFile.getPath().indexOf(".aml") == -1)
       {
         String newPath = chosenFile.getPath() + ".aml";
         chosenFile = new File(newPath);
       } else if (amlChoice.getFileFilter() == aifFilter && chosenFile.getPath().indexOf(".aif") == -1)
       {
         String newPath = chosenFile.getPath() + ".aif";
         chosenFile = new File(newPath);
       }
       if (!chosenFile.exists())
       {
         messageLabel.setText("File does not exist.");
         return;
       }
       fileName = chosenFile.getAbsolutePath();
     } catch (Exception e)
     {
     }
     if (amlChoice.getFileFilter() == amlFilter)
     {
       openAmlFile(fileName);
     } else if (amlChoice.getFileFilter() == aifFilter)
     {
       openAifFile(fileName);
     }
   }
 
   public void openAifFile(String fileName)
   {
     OpenArgumentAif openArgumentAif = new OpenArgumentAif();
     argument = new Argument();
     openArgumentAif.loadArgument(fileName, argument);
     argument.setShowOwners(true);
     updateSelectText();
     updateDisplays(true);
     messageLabel.setText("File " + fileName + " read successfully.");
     setTitle(mainFrameTitle + " - " + fileName);
 //    currentOpenXMLFile = chosenFile;
     //     m_wordCount = SelectText.wordCount(selectText.text);
     clearUndoStack();
     recentAmlFiles.remove(fileName);
     recentAmlFiles.push(fileName);
     buildRecentFileMenus();
   }
 
   public void openAmlFile(String fileName)
   {
     try
     {
       File chosenFile = new File(fileName);
       if (!chosenFile.exists())
       {
         messageLabel.setText("AML file " + chosenFile.getName() + " not found.");
         recentAmlFiles.remove(fileName);
         buildRecentFileMenus();
         return;
       }
       argument = new Argument();
       FileInputStream fileStream = new FileInputStream(fileName);
       char inBuffer[] = new char[(int) chosenFile.length()];
 
       InputStreamReader isReader = new InputStreamReader(fileStream, "UTF-8");
       BufferedReader r = new BufferedReader(isReader);
       int charsRead = r.read(inBuffer, 0, inBuffer.length);
       String text = new String(inBuffer);
       text = text.substring(0, charsRead);
       inBuffer = text.toCharArray();
       CharArrayReader charReader = new CharArrayReader(inBuffer);
       fileStream.close();
       InputSource saxInput = new InputSource(charReader);
       parseXMLwithSAX(saxInput);
       argument.setShowOwners(true);
       updateSelectText();
       updateDisplays(true);
       messageLabel.setText("File " + fileName + " read successfully.");
       setTitle(mainFrameTitle + " - " + fileName);
       currentOpenXMLFile = chosenFile;
       //     m_wordCount = SelectText.wordCount(selectText.text);
       clearUndoStack();
       recentAmlFiles.remove(fileName);
       recentAmlFiles.push(fileName);
       buildRecentFileMenus();
     } catch (IOException e)
     {
       messageLabel.setText("Error reading URI: " + e.getMessage());
     } catch (SAXException e)
     {
       messageLabel.setText("Error in parsing " + fileName + ": " + e.getMessage());
     } catch (Exception e)
     {
       messageLabel.setText("Exception: " + fileName);
       e.printStackTrace();
     } catch (Error e)
     {
       messageLabel.setText("Error: " + fileName);
       e.printStackTrace();
     }
   }
 
   /**
    * This version is called by doTreeSearch() and uses a TreeSearchContentHandler
    * to do the parsing. This builds the tree but ignores all schemesets, text, etc.
    */
   public void parseXMLwithSAX(InputSource source, Tree tree) throws Exception
   {
     org.xml.sax.ContentHandler contentHandler =
             new TreeSearchContentHandler(tree);
     org.xml.sax.ErrorHandler errorHandler = new XMLErrorHandler();
     XMLReader parser =
             XMLReaderFactory.createXMLReader();
 //              "org.apache.xerces.parsers.SAXParser");
     parser.setContentHandler(contentHandler);
     parser.setErrorHandler(errorHandler);
     parser.setFeature("http://xml.org/sax/features/validation",
             true);
     parser.setFeature("http://xml.org/sax/features/namespaces",
             false);
     parser.parse(source);
   }
 
   public void parseXMLwithSAX(InputSource source) throws Exception
   {
     org.xml.sax.ContentHandler contentHandler =
             new XMLContentHandler(argument);
     org.xml.sax.ErrorHandler errorHandler = new XMLErrorHandler();
     XMLReader parser =
             XMLReaderFactory.createXMLReader();
 //              "org.apache.xerces.parsers.SAXParser");
 //      System.out.println("org.xml.sax.driver: " + System.getProperty("org.xml.sax.driver"));
     parser.setContentHandler(contentHandler);
     parser.setErrorHandler(errorHandler);
     parser.setFeature("http://xml.org/sax/features/validation",
             true);
     parser.setFeature("http://xml.org/sax/features/namespaces",
             false);
     parser.parse(source);
   }
 
   public void cancelSearch()
   {
     try
     {
       statement.close();
       statement = null;
       dbConnection.close();
       dbConnection = null;
       dbAddress = null;
       JOptionPane.showMessageDialog(this,
               "Search cancelled", "Search cancelled",
               JOptionPane.ERROR_MESSAGE);
       setMessageLabelText("Search cancelled.");
     } catch (Exception ex)
     {
     }
   }
   static String aifFileSuffix = "aif";
 
   public static String getAifFileSuffix()
   {
     return aifFileSuffix;
   }
 
   public static String getAmlFileSuffix()
   {
     return amlFileSuffix;
   }
   static String amlFileSuffix = "aml";
 
   public void saveAML(boolean doSaveAs)
   {
     if (treeError())
     {
       return;
     }
     try
     {
       if (doSaveAs)
       {
         amlChoice = new JFileChooser();
         FileNameExtensionFilter aifFilter = new FileNameExtensionFilter("AIF files", aifFileSuffix);
         amlChoice.addChoosableFileFilter(aifFilter);
         FileNameExtensionFilter amlFilter = new FileNameExtensionFilter("AML files", amlFileSuffix);
         amlChoice.addChoosableFileFilter(amlFilter);
         amlChoice.setDialogTitle("Save argument");
         amlChoice.setCurrentDirectory(new File(amlDirectory));
         if (amlChoice.showSaveDialog(Araucaria.this) ==
                 JFileChooser.APPROVE_OPTION)
         {
           lastDirectory = amlChoice.getCurrentDirectory();
           File chosenFile = amlChoice.getSelectedFile();
           // Check that a file type has been specified. If not, append correct suffix
           if (amlChoice.getFileFilter() == amlFilter && chosenFile.getPath().indexOf(".aml") == -1)
           {
             String newPath = chosenFile.getPath() + ".aml";
             chosenFile = new File(newPath);
           } else if (amlChoice.getFileFilter() == aifFilter && chosenFile.getPath().indexOf(".aif") == -1)
           {
             String newPath = chosenFile.getPath() + ".aif";
             chosenFile = new File(newPath);
           }
           if (chosenFile.exists())
           {
             int action = JOptionPane.showConfirmDialog(Araucaria.this,
                     "<html><center><font color=red face=helvetica><b>Overwrite existing file?</b></font></center></html>", "Overwrite?",
                     JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
             if (action == 1)
             {
               saveAML(doSaveAs);
               return;
             }
           }
           currentOpenXMLFile = chosenFile;
           if (argument.doSaveXMLFile(currentOpenXMLFile))
           {
             messageLabel.setText("File " + currentOpenXMLFile.getAbsolutePath() + " saved successfully.");
             setTitle(mainFrameTitle + " - " + currentOpenXMLFile.getAbsolutePath());
             diagramModified = false;
           } else
           {
             messageLabel.setText("Error writing XML file.\n");
           }
         } else
         {
           messageLabel.setText("Argument not saved.");
           return;
         }
       } else
       {
         if (argument.doSaveXMLFile(currentOpenXMLFile))
         {
           messageLabel.setText("File " + currentOpenXMLFile.getAbsolutePath() + " saved successfully.");
           setTitle(mainFrameTitle + " - " + currentOpenXMLFile.getAbsolutePath());
           diagramModified = false;
         } else
         {
           messageLabel.setText("Error writing XML file.\n");
         }
       }
     } catch (Exception e)
     {
     }
   }
 
   /**
    * If tree is empty or incomplete, return true, else false.
    */
   private boolean treeError()
   {
     if (argument.getTree().getRoots().size() == 0)
     {
       messageLabel.setText("No argument - nothing to save.");
       return true;
     } else if (argument.getTree().getRoots().size() > 1)
     {
       messageLabel.setText("Argument has more than one conclusion - please complete the diagram.");
       return true;
     }
     return false;
   }
 
   public void saveImageFile()
   {
     File chosenFile = null;
     BufferedImage image = currentDiagram.getMainDiagramPanel().getJpegImage();
     if (image == null)
     {
       return;
     }
     tiffChoice.setCurrentDirectory(lastDirectory);
     if (tiffChoice.showSaveDialog(this) ==
             JFileChooser.APPROVE_OPTION)
     {
       lastDirectory = tiffChoice.getCurrentDirectory();
       chosenFile = tiffChoice.getSelectedFile();
       // See if user has typed in the .tif or .jpg suffix. If not, find which type they selected and add it.
       if (chosenFile.getPath().indexOf(".tif") >= 0)
       {
         imageType = IMAGE_TIF;
       } else if (chosenFile.getPath().indexOf(".jpg") >= 0)
       {
         imageType = IMAGE_JPG;
       } else
       {
         javax.swing.filechooser.FileFilter selectedFilter = tiffChoice.getFileFilter();
         if (selectedFilter == jpgFilter)
         {
           imageType = IMAGE_JPG;
           String newPath = chosenFile.getPath() + ".jpg";
           chosenFile = new File(newPath);
         } else if (selectedFilter == tifFilter)
         {
           imageType = IMAGE_TIF;
           String newPath = chosenFile.getPath() + ".tif";
           chosenFile = new File(newPath);
         } else
         { // If All files chooser is selected, default to a tif file
           imageType = IMAGE_TIF;
           String newPath = chosenFile.getPath() + ".tif";
           chosenFile = new File(newPath);
         }
       }
     }
     try
     {
       switch (imageType)
       {
         case IMAGE_JPG:
           saveJpegFile(image, chosenFile);
           break;
         case IMAGE_TIF:
           saveTiffFile(image, chosenFile);
           break;
       }
     } catch (Exception ex)
     {
     }
   }
 
   public void saveTiffFile(BufferedImage image, File chosenFile)
   {
     if (chosenFile.exists())
     {
       int action = JOptionPane.showConfirmDialog(this,
               "<html><center><font color=red face=helvetica><b>Overwrite existing file?</b></face></center></html>", "Overwrite?",
               JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
       if (action == 1)
       {
         saveImageFile();
         return;
       }
     }
     try
     {
       FileOutputStream chosenOutput = new FileOutputStream(chosenFile);
       TIFFEncodeParam params = new TIFFEncodeParam();
       params.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE);
       ImageEncoder encoder = ImageCodec.createImageEncoder("TIFF", chosenOutput, params);
       if (encoder == null)
       {
         System.out.println("imageEncoder is null");
         System.exit(0);
       }
       encoder.encode(image);
       chosenOutput.flush();
       chosenOutput.close();
       setMessageLabelText("TIFF image " + chosenFile.getAbsolutePath() + " saved successfully.");
     } catch (Exception e)
     {
       System.out.println(e.toString());
       setMessageLabelText("Error writing TIFF file." + chosenFile.getAbsolutePath());
     }
   }
 
   public void saveJpegFile(BufferedImage image, File chosenFile)
   {
     if (chosenFile.exists())
     {
       int action = JOptionPane.showConfirmDialog(this,
               "<html><center><font color=red face=helvetica><b>Overwrite existing file?</b></face></center></html>", "Overwrite?",
               JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
       if (action == 1)
       {
         saveImageFile();
         return;
       }
     }
     try
     {
       FileOutputStream chosenOutput = new FileOutputStream(chosenFile);
      ImageIO.write(image, "jpg", chosenOutput);
       chosenOutput.flush();
      chosenOutput.close();
      
       setMessageLabelText("JPG image " + chosenFile.getAbsolutePath() + " saved successfully.");
     } catch (Exception e)
     {
       System.out.println(e.toString());
       setMessageLabelText("Error writing JPG file." + chosenFile.getAbsolutePath());
     }
   }
 
   public void updateSelectText()
   {
     selectText.setText(argument.getText());
     selectText.constructTextLayout();
     selectTextScroll.setViewportView(selectText);
     selectText.assignShapes(argument.getTree());
     selectText.repaint();
   }
 
   public DisplayFrame getCurrentDiagram()
   {
     return currentDiagram;
   }
 
   public void updateDisplays(boolean updateCurrent)
   {
     updateDisplays(updateCurrent, true);
   }
 
   public void updateDisplays(boolean updateCurrent, boolean clearSelections)
   {
     if (clearSelections)
     {
       argument.clearAllSelections();
     }
     for (int i = 0; i < numDiagrams; i++)
     {
       if (displays[i] != currentDiagram && displays[i] != null)
       {
         displays[i].setArgument(argument);
         displays[i].refreshPanels(true);
       }
       if (updateCurrent)
       {
         currentDiagram.setArgument(argument);
         currentDiagram.refreshPanels(true);
       }
     }
   }
 
   /**
    * Clears the tree and the text pane
    */
   public void closeAll()
   {
     if (argument.getTree().getRoots().size() > 0)
     {
       int action = JOptionPane.showConfirmDialog(Araucaria.this,
               "<html><center><font color=red face=helvetica><b>This will erase the current tree and text.<br> " +
               "THIS ACTION CANNOT BE UNDONE!<br>" +
               "Do you want to continue?</b></face></center></html>", "Delete current tree?",
               JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
       if (action == 1)
       {
         messageLabel.setText("Action cancelled.");
         return;
       }
     }
     argument.emptyTree(true);
     argument.setText(" ");
     clearUndoStack();
     updateSelectText();
     updateDisplays(true);
     messageLabel.setText("All data cleared.");
     setTitle(mainFrameTitle + " (no diagram loaded)");
   }
 
   public void clearUndoStack()
   {
     undoStack = new UndoStack();
     EditAction action = new EditAction(this, "starting state");
     undoStack.push(action);
     redoMenuItem.setEnabled(false);
     redoToolBar.setEnabled(false);
     undoMenuItem.setEnabled(false);
     undoToolBar.setEnabled(false);
   }
   public static boolean diagramModified = false;
 
   void exitApplication()
   {
     // If the tree has been modified and isn't empty, ask if you want to save it.
     // Test if modified by examining the undo stack.
     if (undoStack.undoPointer > 0 && diagramModified && argument.getTree().getRoots().size() > 0)
     {
       int action = JOptionPane.showConfirmDialog(Araucaria.this,
               "<html><center><font color=red face=helvetica><b>Save argument before exiting?</b></font></center></html>", "Save argument?",
               JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null);
       if (action == JOptionPane.YES_OPTION)
       {
         saveAML(currentOpenXMLFile == null);
       } else if (action == JOptionPane.CANCEL_OPTION)
       {
         return;
       }
     }
     this.setVisible(false);    // hide the Frame
     System.exit(0);            // close the application
   }
 
   public void doRedo()
   {
     doRedo(true);
   }
 
   public void doRedo(boolean showMessage)
   {
     if (undoStack.undoPointer == undoStack.size() - 1)
     {
       setMessageLabelText("Nothing more to redo");
       return;
     }
     EditAction action = (EditAction) undoStack.elementAt(++undoStack.undoPointer);
     if (undoStack.undoPointer == undoStack.size() - 1)
     {
       redoMenuItem.setEnabled(false);
       redoToolBar.setEnabled(false);
     }
     undoMenuItem.setEnabled(true);
     undoToolBar.setEnabled(true);
     action.restore(false, action.description, showMessage);
     updateDisplays(true);
   }
 
   public void doUndo()
   {
     doUndo(true, true);
   }
 
   public void doUndo(boolean updateDisplay)
   {
     doUndo(updateDisplay, true);
   }
 
   public void doUndo(boolean updateDisplay, boolean showMessage)
   {
     // If we've undone everything, return
     if (undoStack.undoPointer <= 0)
     {
       setMessageLabelText("Nothing more to undo");
       return;
     }
     EditAction undoingAction = (EditAction) undoStack.elementAt(undoStack.undoPointer);
     EditAction action = (EditAction) undoStack.elementAt(--undoStack.undoPointer);
     if (undoStack.undoPointer <= 0)
     {
       undoMenuItem.setEnabled(false);
       undoToolBar.setEnabled(false);
     }
     redoMenuItem.setEnabled(true);
     redoToolBar.setEnabled(true);
     action.restore(true, undoingAction.description, showMessage);
     if (updateDisplay)
     {
       updateDisplays(true);
     }
   }
 
   public String getUndoAML()
   {
     byte[] outBuffer = argument.writeXMLAsBytes();
     return new String(outBuffer);
   }
 
   public void doClearDiagram()
   {
     int action = JOptionPane.showConfirmDialog(Araucaria.this,
             "<html><center><font color=red face=helvetica><b>This command will erase the current tree<br> " +
             "but retain the text. IT CANNOT BE UNDONE.<br>" +
             "Do you want to continue?</b></font></center></html>", "New argument tree?",
             JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
     if (action == 1)
     {
       setMessageLabelText("Tree not erased.");
       return;
     }
     argument.topWigmoreIndex = 0;
     argument.emptyTree(true);
     updateDisplays(true);
     updateSelectText();
     clearUndoStack();
     setTitle(mainFrameTitle + " (no diagram loaded)");
   }
 
   public void doInvertDiagram()
   {
     argument.setInvertedTree(!argument.getInvertedTree());
     updateDisplays(true);
   }
 
   public void addMissingPremise()
   {
     String missingPremise = JOptionPane.showInputDialog(Araucaria.this,
             "Enter text for missing premise.                                                                                ",
             "Missing premise", JOptionPane.QUESTION_MESSAGE);
     if (missingPremise != null)
     {
       //          missingPremise = SelectText.stripWhiteSpace(missingPremise);
       //          missingPremise = SelectText.stripWeirdChars(missingPremise);
       argument.addFreeVertex(missingPremise, null, -1, currentDiagram.getMainDiagramPanel());
       updateDisplays(true);
       undoStack.push(new EditAction(Araucaria.this, "adding missing premise"));
     }
   }
 
   public void doRefutation()
   {
     String result = argument.setRefutations();
     if (result.equals("Refutation status toggled"))
     {
       updateDisplays(true);
       undoStack.push(new EditAction(this, "toggling refutation"));
       doUndo(false, false);
       doRedo(false);
     }
     setMessageLabelText(result);
   }
 
   public void doDeletion()
   {
     if (argument.deleteSelectedItems())
     {
       undoStack.push(new EditAction(this, "deleting selected items"));
       doUndo(false, false);
       doRedo(false);
       updateDisplays(true);
       updateSelectText();
 
       // Update the Wigmore index after deletion
       argument.topWigmoreIndex = 0;
       for (Object vertex : argument.tree.getVertexList())
       {
         String label = ((TreeVertex) vertex).getShortLabelString();
         argument.updateWigmoreIndex(label);
       }
       setMessageLabelText("Selected item(s) deleted");
     } else
     {
       setMessageLabelText("Nothing selected for deletion");
     }
   }
 
   public void doLink()
   {
     Vector selectedVerts = null;
     try
     {
       selectedVerts = argument.linkVertices();
     } catch (LinkException e)
     {
       setMessageLabelText(e.getMessage());
       return;
     }
     // If the link was successful, we need to adjust the Toulmin
     // diagram as well. We make the first node in the link the Data
     // node and all the others are warrants of that Data
 
     // claim is the parent of the virtual node
     argument.clearAllSelections();
     TreeVertex data = (TreeVertex) selectedVerts.elementAt(0);
     TreeVertex claim = data.getParent().getParent();
     // Since all data edges were removed in forming the LA we need to
     // add them back in again for Toulmin data list
     /*
     for (int i = 0; i < selectedVerts.size(); i++)
     {
     claim.toulminDataEdges.add(new TreeEdge(claim, (TreeVertex)selectedVerts.elementAt(i)));
     }
     TreeEdge dataEdge = claim.getEdge(claim.toulminDataEdges, data);
     if (dataEdge != null)
     {
     dataEdge.setSelected(true);
     for (int i = 1; i < selectedVerts.size(); i++)
     {
     TreeVertex warrant = (TreeVertex)selectedVerts.elementAt(i);
     warrant.setSelected(true);
     }
     ToulminFullTextPanel toulPanel =
     (ToulminFullTextPanel)displays[DIAGRAM_TOULMIN_FULL_TEXT].getMainDiagramPanel();
     toulPanel.createWarrantFromLink(selectedVerts, dataEdge);
     }*/
     updateDisplays(true);
     setMessageLabelText("Statements linked.");
     undoStack.push(new EditAction(this, "linking premises"));
     doUndo(false, false);
     doRedo(false);
   }
 
   public void doUnlink()
   {
     Vector selectedVerts = null;
     try
     {
       selectedVerts = argument.unlinkVertices();
     } catch (LinkException e)
     {
       setMessageLabelText(e.getMessage());
       return;
     }
 
     // Update Toulmin diagram
     // All unlinked vertices are converted to data nodes and linked to
     // the parent of any one of the vertices.
 
     // Select the vertices that were unlinked above.
     argument.clearAllSelections();
     for (int i = 0; i < selectedVerts.size(); i++)
     {
       TreeVertex vertex = (TreeVertex) selectedVerts.elementAt(i);
       vertex.setSelected(true);
     }
     // Since unlinked vertices can come from different LAs, we need to
     // make sure that each vertex is directed back to the correct claim
     //
     // Check each selected vertex and if it's a Data node then redirect
     // all the *selected* warrants from that Data node to point to the
     // Data node's parent.
     for (int i = 0; i < selectedVerts.size(); i++)
     {
       TreeVertex vertex = (TreeVertex) selectedVerts.elementAt(i);
       if (vertex.roles.get("toulmin").equals("data"))
       {
         vertex.setSelected(false);
         TreeVertex claim = vertex.getParent();
         Vector warrantList = vertex.toulminWarrantEdges;
         for (int j = 0; j < warrantList.size(); j++)
         {
           TreeVertex warrant = ((TreeEdge) warrantList.elementAt(j)).getDestVertex();
           if (warrant.isSelected())
           {
             warrant.setSelected(false);
             claim.toulminDataEdges.add(new TreeEdge(claim, warrant));
             warrantList.remove(vertex.getEdge(warrantList, warrant));
             warrant.roles.put("toulmin", "data");
             j--;
           }
         }
       }
     }
     // It is possible that only some of the elements in an LA have been unlinked
     // and that these elements do not include the Data node from the Toulmin diagram.
     // If this is so, then there will still be some vertexes that are selected.
     // For such a vertex, we find its claim, then find the data node of that claim
     // that contained the vertex as a warrant, then delete that warrant edge.
     /*
     for (int i = 0; i < selectedVerts.size(); i++)
     {
     TreeVertex vertex = (TreeVertex)selectedVerts.elementAt(i);
     if (vertex.isSelected())
     {
     TreeVertex claim = vertex.getParent();
     claim.toulminDataEdges.add(new TreeEdge(claim, vertex));
     Vector dataList = claim.toulminDataEdges;
     for(int j = 0; j < dataList.size(); j++)
     {
     TreeVertex data = ((TreeEdge)dataList.elementAt(j)).getDestVertex();
     TreeEdge remEdge = data.getEdge(data.toulminWarrantEdges, vertex);
     if(remEdge != null)
     {
     data.toulminWarrantEdges.remove(remEdge);
     vertex.roles.put("toulmin", "data");
     break;
     }
     }
     }
     } */
     updateDisplays(true);
     setMessageLabelText("Statements unlinked.");
     undoStack.push(new EditAction(this, "unlinking premises"));
     doUndo(false, false);
     doRedo(false);
   }
 
   public void doSelectAll()
   {
     argument.selectAllNodes();
     updateDisplays(true, false);
   }
 
   public void doModifyOwnership()
   {
     OwnerDialog ownerDialog = new OwnerDialog(this);
     ownerDialog.setVisible(true);
     showOwnersAction.putValue(Action.SHORT_DESCRIPTION, "Hide owners");
     showOwnersMenuItem.setText("Hide owners");
     argument.setShowOwners(true);
     // Redrawing an empty tree deletes the owners list
     if (argument.getTree().getRoots().size() > 0)
     {
       updateDisplays(true);
     }
   }
 
   public void doShowOwners()
   {
     if (argument.isShowOwners())
     {
       argument.setShowOwners(false);
       showOwnersAction.putValue(Action.SHORT_DESCRIPTION, "Show owners");
       showOwnersMenuItem.setText("Show owners");
     } else
     {
       argument.setShowOwners(true);
       showOwnersAction.putValue(Action.SHORT_DESCRIPTION, "Hide owners");
       showOwnersMenuItem.setText("Hide owners");
     }
     updateDisplays(true);
   }
 
   public void doModifyEvaluation()
   {
     if (this.getArgument().getSelectedEdges().size() +
             this.getArgument().getSelectedVertices().size() == 0)
     {
       JOptionPane.showMessageDialog(this,
               "<html><center><font color=red face=helvetica><b>Please select some nodes or supports first.</b>" +
               "</font></center></html>", "Nothing selected",
               JOptionPane.ERROR_MESSAGE);
       return;
     }
     showEvaluationAction.putValue(Action.SHORT_DESCRIPTION, "Hide evaluations");
     showEvaluationMenuItem.setText("Hide evaluations");
     argument.buildSupportLabelList();
     LabelDialog labelDialog = new LabelDialog(this);
     labelDialog.setVisible(true);
     argument.setShowSupportLabels(true);
     if (argument.getTree().getRoots().size() > 0)
     {
       updateDisplays(true);
     }
   }
   String[] nodeBeliefs = {"Doubt", "Belief", "Strong belief", "Disbelief", "Strong disbelief"};
 
   public void doWigmoreNodeBelief()
   {
     if (getArgument().getSelectedVertices().size() == 0)
     {
       JOptionPane.showMessageDialog(this,
               "<html><center><font color=red face=helvetica><b>Please select some nodes first.</b>" +
               "</font></center></html>", "Nothing selected",
               JOptionPane.ERROR_MESSAGE);
       return;
     }
     showEvaluationAction.putValue(Action.SHORT_DESCRIPTION, "Hide evaluations");
     showEvaluationMenuItem.setText("Hide evaluations");
     LabelDialog labelDialog = new LabelDialog(this, true, nodeBeliefs, "Select a belief level",
             LabelDialog.EdgeType.UNSPECIFIED);
     labelDialog.setVisible(true);
     argument.setShowSupportLabels(true);
     if (argument.getTree().getRoots().size() > 0)
     {
       updateDisplays(true);
     }
   }
 
   public void doWigmoreEdgeForce(String[] choices, boolean negatoryVisible, boolean forcedNegatory, LabelDialog.EdgeType edgeType)
   {
     Vector wigEdges = getArgument().getSelectedWigmoreEdges();
     if (wigEdges.size() == 0)
     {
       JOptionPane.showMessageDialog(this,
               "<html><center><font color=red face=helvetica><b>Please select some edges first.</b>" +
               "</font></center></html>", "Nothing selected",
               JOptionPane.ERROR_MESSAGE);
       return;
     }
     showEvaluationAction.putValue(Action.SHORT_DESCRIPTION, "Hide evaluations");
     showEvaluationMenuItem.setText("Hide evaluations");
     LabelDialog labelDialog = new LabelDialog(this, true, choices, "Select a force", edgeType);
     labelDialog.setEdgeList(wigEdges);
     labelDialog.createUndoPoint = false;
     labelDialog.getNegatoryCheckBox().setVisible(negatoryVisible);
     if (forcedNegatory)
     {
       labelDialog.getNegatoryCheckBox().setEnabled(false);
       labelDialog.getNegatoryCheckBox().setSelected(true);
       labelDialog.getDeleteButton().setEnabled(false);
     }
     labelDialog.setVisible(true);
     if (!labelDialog.cancelled)
     {
       checkNewNegatory(wigEdges);
       checkCancelNegatory(wigEdges);
       undoStack.push(new EditAction(this, "Wigmore force"));
       doUndo(false, false);
       doRedo(false);
     }
     if (argument.getTree().getRoots().size() > 0)
     {
       updateDisplays(true);
     }
   }
 
   /**
    * Checks vertexes that have just had their Wigmore force changed. If a vertex is newly
    * negatory, add an addedNegation node.
    */
   public void checkNewNegatory(Vector wigEdges)
   {
     Enumeration nodeEnum = wigEdges.elements();
     while (nodeEnum.hasMoreElements())
     {
       TreeEdge edge = (TreeEdge) nodeEnum.nextElement();
       TreeVertex vertex = edge.getDestVertex();
       if (vertex.getSupportLabel() == null)
       {
         continue;
       }
       if (!vertex.isVirtual() && !vertex.previousRefutation && vertex.isRefutation())
       {
         TreeVertex parent = vertex.getParent();
         String oppositeText = "It is not the case that \"" + (String) vertex.getLabel() + "\"";
         TreeVertex opposite = argument.addAuxiliaryVertex(oppositeText);
         opposite.roles.put("addedNegation", "yes");
         opposite.isHiddenTable.put("toulmin", "true");
         opposite.isHiddenTable.put("wigmore", "true");
         argument.getTree().addVertex(opposite);
         parent.getEdgeList().remove(parent.getEdge(vertex));
         parent.addEdge(opposite);
         opposite.setParent(parent);
         opposite.setHasParent(true);
         opposite.addEdge(vertex);
         vertex.setParent(opposite);
       }
     }
   }
 
   public void checkCancelNegatory(Vector wigEdges)
   {
     Enumeration nodeEnum = wigEdges.elements();
     while (nodeEnum.hasMoreElements())
     {
       TreeEdge edge = (TreeEdge) nodeEnum.nextElement();
       TreeVertex vertex = edge.getDestVertex();
       if (!vertex.isVirtual() && vertex.previousRefutation && !vertex.isRefutation() &&
               vertex.hasAddedNegationNoSupport())
       {
         TreeVertex addedNegation = vertex.getParent();
         TreeVertex negationParent = addedNegation.getParent();
         negationParent.deleteEdge(addedNegation);
         negationParent.addEdge(vertex);
         argument.getTree().getVertexList().remove(addedNegation);
         vertex.setParent(negationParent);
       }
     }
   }
 
   public void doShowEvaluation()
   {
     if (argument.isShowSupportLabels())
     {
       argument.setShowSupportLabels(false);
       showEvaluationAction.putValue(Action.SHORT_DESCRIPTION, "Show evaluation");
       showEvaluationMenuItem.setText("Show evaluation");
     } else
     {
       argument.setShowSupportLabels(true);
       showEvaluationAction.putValue(Action.SHORT_DESCRIPTION, "Hide evaluation");
       showEvaluationMenuItem.setText("Hide evaluation");
     }
     updateDisplays(true);
   }
 
   public void doShowCQs()
   {
     if (argument.isShowCQsAnswered())
     {
       argument.setShowCQsAnswered(false);
       showCQsAction.putValue(Action.SHORT_DESCRIPTION, "Show critical questions answered");
       showCQsMenuItem.setText("Show CQs answered");
     } else
     {
       argument.setShowCQsAnswered(true);
       showCQsAction.putValue(Action.SHORT_DESCRIPTION, "Hide critical questions answered");
       showCQsMenuItem.setText("Hide CQs answered");
     }
     updateDisplays(true);
   }
 
   public void doSelectScheme()
   {
     SubtreeFrame subtreeFrame = new SubtreeFrame(this, argument);
     argument.setSubtreeFrame(subtreeFrame);
     try
     {
       argument.addSubtree(null);
     } catch (SubtreeException e)
     {
       subtreeFrame.dispose();
       setMessageLabelText(e.getMessage());
       return;
     }
     updateDisplays(true, false);
     undoStack.push(new EditAction(this, "adding scheme"));
     setMessageLabelText("Scheme added");
   }
 
   public void doAddEditScheme()
   {
     ArgInfoFrame argInfoFrame = new ArgInfoFrame(argument);
     argInfoFrame.setVisible(true);
   }
 
   public void doOpenSchemeset()
   {
     FileInputStream inputStream = null;
     String fileName = "";
     try
     {
       schemeChoice.setDialogTitle("Read schemeset");
       schemeChoice.setCurrentDirectory(new File(schemeDirectory));
       if (schemeChoice.showOpenDialog(this) ==
               JFileChooser.APPROVE_OPTION)
       {
         schemeDirectory = schemeChoice.getCurrentDirectory().getAbsolutePath();
         int action = JOptionPane.showConfirmDialog(this,
                 "<html><center><font color=red face=helvetica><b>Do you want to replace the existing schemeset?<br> " +
                 "(This will erase any schemes in the current tree.)</b></font></center></html>", "Replace schemeset?",
                 JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
         if (action == 1)
         {
           setMessageLabelText("Schemeset not replaced.");
           return;
         }
         lastDirectory = schemeChoice.getCurrentDirectory();
         File chosenFile = schemeChoice.getSelectedFile();
         // See if user has typed in the .scm suffix. If not, add it.
         if (chosenFile.getPath().indexOf(".scm") == -1)
         {
           String newPath = chosenFile.getPath() + ".scm";
           chosenFile = new File(newPath);
         }
         if (!chosenFile.exists())
         {
           setMessageLabelText("File does not exist.");
           return;
         }
         inputStream = new FileInputStream(chosenFile);
         fileName = chosenFile.getAbsolutePath();
       } else
       {
         setMessageLabelText("Schemeset not read.");
         return;
       }
     } catch (Exception e)
     {   // Java Web Start bit
     }
     openSchemeset(fileName);
   }
 
   public void openSchemeset(String fileName)
   {
     // Parse the schemeset XML
     try
     {
       File chosenFile = new File(fileName);
       if (!chosenFile.exists())
       {
         messageLabel.setText("Schemeset file " + chosenFile.getName() + " not found.");
         recentSchemeFiles.remove(fileName);
         buildRecentFileMenus();
         return;
       }
       FileInputStream fileStream = new FileInputStream(fileName);
       byte[] bytes = new byte[1024];
       StringBuffer byteString = new StringBuffer();
       do
       {
         int count = fileStream.read(bytes);
         if (count == -1)
         {
           break;
         }
         for (int i = 0; i < count; i++)
         {
           byte[] b = new byte[1];
           if (bytes[i] == 13 || bytes[i] == 10)
           {
             bytes[i] = 32;
           }
           b[0] = bytes[i];
           byteString.append(new String(b));
         }
       } while (true);
       ByteArrayInputStream byteStream = new ByteArrayInputStream(byteString.toString().getBytes());
       InputSource saxInput = new InputSource(byteStream);
       parseXMLwithSAX(saxInput);
       setMessageLabelText("Schemeset " + fileName + " read successfully.");
       updateDisplays(true);
       recentSchemeFiles.remove(fileName);
       recentSchemeFiles.push(fileName);
       buildRecentFileMenus();
     } catch (Exception e)
     {
       System.out.println(e.toString());
       setMessageLabelText("Error reading schemeset.");
     } catch (Error e)
     {
       System.out.println(e.toString());
       setMessageLabelText("Error reading schemeset.");
     }
     // TODO: restore this for database searching
 //    searchFrame.updateSchemesetSearchCombo();
     updateDisplays(true);
   }
 
   public void doSaveSchemeset()
   {
     if (argument.getSchemeList().size() == 0)
     {
       setMessageLabelText("No schemes in schemeset - nothing to save.");
       return;
     }
     try
     {
       schemeChoice.setDialogTitle("Save schemeset");
       schemeChoice.setCurrentDirectory(new File(schemeDirectory));
       if (schemeChoice.showSaveDialog(Araucaria.this) ==
               JFileChooser.APPROVE_OPTION)
       {
         lastDirectory = schemeChoice.getCurrentDirectory();
         File chosenFile = schemeChoice.getSelectedFile();
         // See if user has typed in the .scm suffix. If not, add it.
         if (chosenFile.getPath().indexOf(".scm") == -1)
         {
           String newPath = chosenFile.getPath() + ".scm";
           chosenFile = new File(newPath);
         }
         if (chosenFile.exists())
         {
           int action = JOptionPane.showConfirmDialog(Araucaria.this,
                   "<html><center><font color=red face=helvetica><b>Overwrite existing file?</b></font></center></html>", "Overwrite?",
                   JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
           if (action == 1)
           {
             doSaveSchemeset();
             return;
           }
         }
         try
         {
 //      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 //      documentBuilder = factory.newDocumentBuilder();
 //      Document doc = documentBuilder.newDocument();
 //      DOMImplementation impl = documentBuilder.getDOMImplementation();
 //      DocumentType argDTD = impl.createDocumentType("ARG", "SYSTEM", "argument.dtd");
 //      doc.appendChild(argDTD);
 //      Element root = doc.createElement("ARG");
 //      doc.appendChild(root);
 //      root.appendChild(doc.createProcessingInstruction("Araucaria", encoding));
 //      // Schemeset
 //      root.appendChild(jdomSchemeset(doc));
           FileOutputStream chosenOutput = new FileOutputStream(chosenFile);
           String outString = new String(argument.writeSchemeset());
           byte[] outBuffer = outString.getBytes();
           chosenOutput.write(outBuffer, 0, outBuffer.length);
           chosenOutput.flush();
           chosenOutput.close();
           setMessageLabelText("Schemeset " + chosenFile.getAbsolutePath() + " saved successfully.");
         } catch (Exception e)
         {
           System.out.println(e.toString());
           setMessageLabelText("Error writing schemeset.\n" + e.toString());
         }
       } else
       {
         setMessageLabelText("Schemeset not saved.");
       }
     } catch (Exception e)
     {
     }
   }
 
   public boolean doLogin()
   {
     try
     {
       Thread messageThread = new Thread(this, "Messages");
       messageThread.start();
       Thread dbThread = new Thread(this, "OpenDatabase");
       dbThread.start();
       dbThread.join(10000);
       if (dbThread.isAlive())
       {
         setMessageLabelText("Unable to connect to database. Please try later.");
         dbThread.interrupt();
         dbThread = null;
         return false;
       }
       String userText = "user";
       if (databaseType == SQLSERVER)
       {
         userText = "[user]";
       }
 
       String newUser = loginDialog.usernameTextField.getText();
       String sql = "SELECT * FROM araucaria_users \nWHERE " + userText + " LIKE " +
               "'" + BaseRecord.escapeQuotes(newUser) + "'";
       ResultSet resultSet = statement.executeQuery(sql);
 
       // If resultSet has any entries, username exists
       while (resultSet.next())
       {
         setMessageLabelText("User " + newUser + " logged in.");
         loggedInUser = newUser;
         return true;
       }
       resultSet.close();
       statement.close();
       statement = null;
       dbConnection.close();
     } catch (Exception ex)
     {
       System.out.println("loginUser: " + ex.toString());
       try
       {
         if (dbConnection != null)
         {
           dbConnection.close();
         }
       } catch (Exception e)
       {
       }
     }
     return false;
   }
 
   public boolean doRegister()
   {
     Connection connection = null;
     try
     {
       connection = openDatabase();
       if (connection == null)
       {
         return false;
       }
       String newUser = registrationDialog.usernameTextField.getText();
       String userText = "user";
       if (databaseType == SQLSERVER)
       {
         userText = "[user]";
       }
       String sql = "SELECT * FROM araucaria_users \nWHERE " + userText + " LIKE " +
               "'" + BaseRecord.escapeQuotes(newUser) + "'";
       ResultSet resultSet = statement.executeQuery(sql);
 
       // If resultSet has any entries, username already exists
       while (resultSet.next())
       {
         return false;
       }
       resultSet.close();
 
       String fullName = registrationDialog.fullNameTextField.getText();
       String address = registrationDialog.addressTextField.getText();
       String email = registrationDialog.emailTextField.getText();
       sql = "INSERT INTO araucaria_users (" + userText + ", fullname, address, email) VALUES(" +
               "'" + BaseRecord.escapeQuotes(newUser) + "'," +
               "'" + BaseRecord.escapeQuotes(fullName) + "'," +
               "'" + BaseRecord.escapeQuotes(address) + "'," +
               "'" + BaseRecord.escapeQuotes(email) + "')";
       statement.executeUpdate(sql);
       statement.close();
       statement = null;
       connection.close();
     } catch (Exception ex)
     {
       System.out.println("registerNewUser: " + ex.toString());
       ex.printStackTrace();
       try
       {
         if (connection != null)
         {
           connection.close();
         }
       } catch (Exception e)
       {
       }
     }
     return true;
   }
 
   private void doPremiseEndpoints()
   {
     if (getArgument().getSelectedVertices().size() != 1)
     {
       setMessageLabelText("Must select precisely one node to edit.");
       return;
     } else if (((TreeVertex) getArgument().getSelectedVertices().elementAt(0)).isMissing())
     {
       setMessageLabelText("Cannot edit boundaries in a missing premise.");
       return;
     }
     TutorDialog tutorDialog = new TutorDialog(Araucaria.this, true);
     tutorDialog.setVisible(true);
     if (tutorDialog.okPressed)
     {
       TreeVertex selectedVertex = (TreeVertex) getArgument().getSelectedVertices().elementAt(0);
       selectedVertex.setTutorEnd(tutorDialog.tutorEnd);
       selectedVertex.setTutorStart(tutorDialog.tutorStart);
     }
   }
 
   private void doMarking()
   {
     MarkingDialog markingDialog = new MarkingDialog(this, true);
     markingDialog.setVisible(true);
   }
 
   public void run()
   {
     Thread currThread = Thread.currentThread();
     try
     {
       if (currThread.getName().equals("Main"))
       {
         new SplashScreen();
       } else if (currThread.getName().equals("OpenDatabase"))
       {
         openDatabase();
       } else if (currThread.getName().equals("Messages"))
       {
         setMessageLabelText("Connecting to database...");
       }
     } catch (Exception ex)
     {
       System.out.println("run: " + currThread.getName() + ": " + ex.toString());
     }
   }
   String dbAddress = null;
 
   public Connection openDatabase() throws SQLException
   {
     dbConnection = null;
     // Get the dbAddress on the first attempt to connect to the DB.
     if (dbAddress == null)
     {
       if (databaseType == SQLSERVER)
       {
         try
         {
           dbConnection = DriverManager.getConnection(
                   "jdbc:odbc:Driver={SQL Server};Server=" + ipAddress + ";Database=" + databaseName,
                   username, password);
           if (dbConnection != null && statement == null)
           {
             statement = dbConnection.createStatement();
           }
         } catch (Exception ex)
         {
           System.out.println("Cannot connect to SQL Server: " + ex.toString());
         }
       } else if (databaseType == MYSQL)
       {
         try
         {
           if (dbConnection == null)
           {
             dbAddress = "jdbc:mysql://" + ipAddress + "/" + databaseName + "?user=" + username;
           }
           dbConnection = DriverManager.getConnection(dbAddress);
           if (dbConnection != null && statement == null)
           {
             statement = dbConnection.createStatement();
           }
         } catch (Exception ex2)
         {
           JOptionPane.showMessageDialog(Araucaria.this,
                   "<html><center><font color=red face=helvetica><b>Unable to connect to database.<br>Please try later.</b></font></center></html>", "Unable to connect",
                   JOptionPane.ERROR_MESSAGE);
           setMessageLabelText("Unable to connect to database. Please try later.");
           dbAddress = null;
         }
       /*
       try {
       URL url = new URL("http://www.computing.dundee.ac.uk/staff/creed/araucaria/redirect.address");
       URLConnection urlConnection = url.openConnection();
       InputStream input = urlConnection.getInputStream();
       byte[] bytes = new byte[100];
       input.read(bytes);
       input.close();
       dbAddress = new String(bytes);
       int endString = dbAddress.indexOf("\n");
       dbAddress = dbAddress.substring(0, endString);
       DriverManager.setLoginTimeout(30);
       dbConnection = DriverManager.getConnection(dbAddress);
       if (dbConnection != null && statement == null)
       statement = dbConnection.createStatement();
       }
       catch (Exception ex) {
       try {
       if (dbConnection == null)
       dbAddress = "jdbc:mysql://134.36.34.192/araucaria_v1_0?user=araucaria_client";
       dbConnection = DriverManager.getConnection(dbAddress);
       if (dbConnection != null && statement == null)
       statement = dbConnection.createStatement();
       } catch (Exception ex2) {
       JOptionPane.showMessageDialog(Araucaria.this,
       "<html><center><font color=red face=helvetica><b>Unable to connect to database.<br>Please try later.</b></font></center></html>", "Unable to connect",
       JOptionPane.ERROR_MESSAGE);
       setMessageLabelText("Unable to connect to database. Please try later.");
       dbAddress = null;
       }
       }
        */
       }
     // We've already determined the working address, so just go ahead and use it.
     } else
     {
       try
       {
         if (dbConnection == null)
         {
           dbConnection = DriverManager.getConnection(dbAddress);
         }
         if (dbConnection != null && statement == null)
         {
           statement = dbConnection.createStatement();
         }
       } catch (Exception ex2)
       {
         setMessageLabelText("Unable to connect to database. Please try later.");
       }
     }
     return dbConnection;
   }
 
   /**
    * Saves the currently displayed argument to a database.
    * So far, it will save the AML and JPEG images (both as OLE objects
    * or long binary values), but needs to be expanded to ask for
    * username, etc.
    */
   public void doSaveDB()
   {
     if (loggedInUser == null)
     {
       setMessageLabelText("You must login to save to the database.");
       return;
     }
     if (treeError())
     {
       return;
     }
     Connection connection = null;
     try
     {
       connection = openDatabase();
       if (connection == null || statement == null)
       {
         return;
       }
 
       argument.setAuthor(loggedInUser);
       argument.setDate(easyDateFormat("yyyy-MM-dd"));
       getSourceComments(true);
       byte[] xmlBytes = argument.writeXMLAsBytes();
       String xmlString = BaseRecord.escapeQuotes(argument.writeXML());
       String userText = "user";
       if (databaseType == SQLSERVER)
       {
         System.out.println("Using SQL Server");
         userText = "[user]";
       }
       // Get the key value to be used for the new record and
       // create the statement
       int maxKey = getMaxKey(ARGID);
       String sql = "INSERT INTO arguments (id, " + userText +
               ", submitted, source, comments, aml, diagram, amlBytes) VALUES(" + ++maxKey +
               ", '" + BaseRecord.escapeQuotes(argument.getAuthor()) + "'" +
               ", '" + argument.getDate() + "'" +
               ", '" + BaseRecord.escapeQuotes(argument.getSource()) + "'" +
               ", '" + BaseRecord.escapeQuotes(argument.getComments()) + "'" +
               ", '" + xmlString + "', ?, ? )";
       PreparedStatement prep = connection.prepareStatement(sql);
       // Get the AML byte array and connect it to the PreparedStatement
       prep.setBytes(2, xmlBytes);
       // Get the JPEG image
       // Here we use the full diagram image as the one to save
       // May want to change this later
       BufferedImage image = ((FullPanel) displays[DIAGRAM_FULL].getMainDiagramPanel()).getJpegImage();
       if (image == null)
       {
         setMessageLabelText("Error creating JPEG image.");
         return;
       }
       // Get the JPEG encoder to write its output into a byte[] array
       ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
       /*
       JPEGImageEncoder encoder =
       JPEGCodec.createJPEGEncoder(byteOutput);
       encoder.encode(image);
        **/
 
       // Create an input stream from the byte array to copy the JPEG
       // file to the database.
       byte[] jpegBytesCopy = byteOutput.toByteArray();
       ByteArrayInputStream jpegStream = new ByteArrayInputStream(jpegBytesCopy);
       prep.setBinaryStream(1, jpegStream, jpegBytesCopy.length);
 
       // Add the new record
       prep.executeUpdate();
 
       // Clean up everything and shut down the connection
       prep.close();
       jpegStream.close();
       byteOutput.close();
       statement.close();
       statement = null;
       connection.close();
       setMessageLabelText("AML & JPEG saved to database.");
     } catch (Exception e)
     {
       try
       {
         if (connection != null)
         {
           connection.close();
         }
       } catch (Exception ee)
       {
       }
       setMessageLabelText("Error saving to database.");
       e.printStackTrace();
     }
   }
 
   int getMaxKey(int table)
   {
     String tableTitle = "";
     switch (table)
     {
       case ARGID:
         tableTitle = "arguments";
         break;
     }
     try
     {
       String sql = "SELECT MAX(id)\nFROM " + tableTitle;
       ResultSet resultSet = statement.executeQuery(sql);
       resultSet.next();
       int maxKey = resultSet.getInt(1);
       resultSet.close();
       return maxKey;
     } catch (Exception e)
     {
       System.out.println(e.toString());
       return -1;
     }
   }
 
   public String easyDateFormat(String format)
   {
     java.util.Date today = new java.util.Date();
     java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(format);
     String datenewformat = formatter.format(today);
     return datenewformat;
   }
 
   public void getSourceComments(boolean loadExisting)
   {
     DBSourceComments sourceDialog = new DBSourceComments(this, true, true);
     if (loadExisting)
     {
       sourceDialog.sourceText.setText(argument.getSource());
       sourceDialog.commentsText.setText(argument.getComments());
     }
     sourceDialog.setVisible(true);
     if (!sourceDialog.okPressed)
     {
       return;
     }
     argument.setSource(sourceDialog.sourceText.getText());
     if (argument.getSource().length() < 1)
     {
       argument.setSource("");
     }
     argument.setComments(sourceDialog.commentsText.getText());
     if (argument.getComments().length() < 1)
     {
       argument.setComments("");
     }
   }
 
   private void displayTreeFromCombo()
   {
     String comboString = (String) searchResultCombo.getSelectedItem();
     int firstPeriod = comboString.indexOf(".");
     int key = Integer.parseInt(comboString.substring(0, firstPeriod));
     setMessageLabelText("Reading from database...");
     openFromDB(key);
     updateDisplays(true);
     updateSelectText();
   }
 
   void doSearchDB()
   {
     Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
     // Position the search dialog so that it is centred in the window
     // but offset slightly from the main Araucaria window
     searchFrame.setLocation(d.width / 2 - searchFrame.getSize().width / 2 + 20,
             d.height / 2 - searchFrame.getSize().height / 2 + 20);
     searchFrame.setVisible(true);
   }
 
   void doTextSearchOnDB(String text, String startTag, String endTag,
           TextSearchTableModel textSearchTableModel,
           JTable textSearchTable)
   {
     int matchFound = 0;
 
     // Need to turn off combo box events until the combo box items have been added.
     doComboEvents = false;
     argument.emptyTree(true);
     searchResultCombo.removeAllItems();
     try
     {
       Connection connection = openDatabase();
       if (connection == null || statement == null)
       {
         return;
       }
       String userText = "user";
       if (databaseType == SQLSERVER)
       {
         userText = "[user]";
       }
 
       String sql = "SELECT id, " + userText + ", submitted, aml FROM arguments \nWHERE aml LIKE " +
               "'%" + startTag + "%" + BaseRecord.escapeQuotes(text) + "%" + endTag + "%' " +
               "\nORDER BY id";
       ResultSet resultSet = statement.executeQuery(sql);
       textSearchTableModel.updateTable(textSearchTable, resultSet);
       resultSet.close();
 
       resultSet = statement.executeQuery(sql);
       while (resultSet.next())
       {
         int key = resultSet.getInt(1);
         String rsString = resultSet.getString(4);
         matchFound++;
         int textStart = rsString.indexOf("<TEXT>");
         int textEnd = rsString.indexOf("</TEXT>");
         try
         {
           String subText = rsString.substring(textStart + "<TEXT>".length(), textEnd);
           if (subText.length() > 80)
           {
             subText = subText.substring(0, 80);
           }
           searchResultCombo.addItem("" + key + ". " + subText);
         } catch (StringIndexOutOfBoundsException e)
         {
           System.out.println("Error loading combo box: " + e.toString());
         }
       }
 
       resultSet.close();
       statement.close();
       statement = null;
       connection.close();
     } catch (SQLException e)
     {
       System.out.println(" In doTextSearchOnDB: " + e.toString());
       e.printStackTrace();
     } catch (Exception e)
     {
       System.out.println(" In doTextSearchOnDB: " + e.toString());
       e.printStackTrace();
     }
     doComboEvents = true;
   }
 
   /**
    * Uses the Reed algorithm to search the database for pattern
    * matches with patternTree.
    * Returns the number of matches found.
    */
   public int doTreeSearch(Tree patternTree,
           TextSearchTableModel textSearchTableModel,
           JTable textSearchTable)
   {
     int matchFound = 0;
     if (patternTree == null || patternTree.getRoots().size() == 0)
     {
       return 0;
     }
     // Need to turn off combo box events until the combo box items have been added.
     doComboEvents = false;
     argument.emptyTree(true);
     searchResultCombo.removeAllItems();
     try
     {
       Connection connection = openDatabase();
       if (connection == null || statement == null)
       {
         return 0;
       }
       String userText = "user";
       if (databaseType == SQLSERVER)
       {
         userText = "[user]";
       }
       String sql = "SELECT COUNT(id) FROM arguments";
       ResultSet resultSet = statement.executeQuery(sql);
       resultSet.next();
       int numEntries = resultSet.getInt(1);
       if (numEntries > 0)
       {
         int yesNo = JOptionPane.showConfirmDialog(this,
                 "This search will require downloading " + numEntries +
                 " \nrecords to your computer. This could take some time." +
                 "\nDo you want to continue?", "Number of database records",
                 JOptionPane.YES_NO_OPTION);
         if (yesNo == 1)
         {
           resultSet.close();
           statement.close();
           statement = null;
           connection.close();
           return 0;
         }
       }
       sql = "SELECT id, " + userText + ", submitted, aml, amlBytes FROM arguments ORDER BY id";
       resultSet = statement.executeQuery(sql);
 
       LinkedList matchedList = new LinkedList();
       while (resultSet.next())
       {
         int key = resultSet.getInt(1);
         Object keyObj = new Integer(key);
         Object user = resultSet.getObject(2);
         Object submitted = resultSet.getObject(3);
         String rsString = resultSet.getString(4);
         byte[] retrievedBytes = resultSet.getBytes(5);
         if (retrievedBytes == null)
         {
           continue;
         }
         ByteArrayInputStream byteStream = new ByteArrayInputStream(retrievedBytes);
         InputSource saxInput = new InputSource(byteStream);
         // Call SAX parser to parse the AML and build the tree
         Tree targetTree = new Tree(this);
         try
         {
           parseXMLwithSAX(saxInput, targetTree);
           if (targetTree.matchSubtree(patternTree))
           {
             matchFound++;
             int textStart = rsString.indexOf("<TEXT>");
             int textEnd = rsString.indexOf("</TEXT>");
             String text = rsString.substring(textStart + "<TEXT>".length(), textEnd);
             try
             {
               text = text.substring(0, 80);
             } catch (StringIndexOutOfBoundsException e)
             {
             }
             searchResultCombo.addItem("" + key + ". " + text);
             Vector matchedItem = new Vector();
             matchedItem.add(keyObj);
             matchedItem.add(user);
             matchedItem.add(submitted);
             matchedItem.add(rsString);
             matchedList.addLast(matchedItem);
           }
         } catch (SAXException ex)
         {
         //System.out.println ("Entry " + key + " has syntax error");
         }
         // Clean up and shut down the database connection
         byteStream.close();
       }
       textSearchTableModel.updateTable(textSearchTable, matchedList);
 
       resultSet.close();
       statement.close();
       statement = null;
       connection.close();
     } catch (SQLException e)
     {
     } catch (Exception e)
     {
       System.out.println("doTreeSearch: ");
       e.printStackTrace();
     }
     doComboEvents = true;
     return matchFound;
   }
 
   /**
    * Loads in the AML from a database.
    */
   public void openFromDB(int amlKey)
   {
     Connection connection = null;
     ResultSet rs = null;
     ByteArrayInputStream byteStream = null;
     try
     {
       argument.emptyTree(true);
       connection = openDatabase();
       if (connection == null || statement == null)
       {
         return;
       }
 
       // If the amlKey is negative, select the last entry in the database
       if (amlKey < 0)
       {
         amlKey = getMaxKey(ARGID);
       }
       String userText = "user";
       if (databaseType == SQLSERVER)
       {
         userText = "[user]";
       }
       // We read the amlBytes column rather than aml, since the only way
       // to retrieve non-ASCII chars seems to be by storing it as a byte array
       rs = statement.executeQuery("SELECT amlBytes, " + userText +
               ", submitted, source, comments FROM arguments WHERE id = " + amlKey);
       rs.next();
 
       // Read the AML as a byte string from the record set, then convert to
       // an input stream, and then to an InputSource, so the SAX parser can
       // read from it.
       byte[] retrievedBytes = rs.getBytes(1);
 //      saveToDiskFile("openFromDB before string", retrievedBytes);
 //      String rsString = rs.getString(1);
 //      saveToDiskFile("openFromDB after string", rsString.getBytes());
       String user = rs.getString(2);
       java.sql.Date submitted = rs.getDate(3);
       argument.setSource(rs.getString(4));
       argument.setComments(rs.getString(5));
 //      for (int i = 0; i < retrievedBytes.length; i++)
 //        if (retrievedBytes[i] == 13 || retrievedBytes[i] == 10) retrievedBytes[i] = 32;
       byteStream = new ByteArrayInputStream(retrievedBytes);
       InputSource saxInput = new InputSource(byteStream);
 
       // Call SAX parser to parse the AML and build the tree
       parseXMLwithSAX(saxInput);
       wordCount = SelectText.wordCount(selectText.text);
 
       // Clean up and shut down the database connection
       byteStream.close();
       rs.close();
       statement.close();
       statement = null;
       connection.close();
       java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("d MMM yyyy");
       String datenewformat = formatter.format(submitted);
       setMessageLabelText("Read entry " + amlKey + " submitted by " +
               user + " on " + datenewformat);
       setTitle(mainFrameTitle + " (loaded diagram from database)");
       argument.setDate(datenewformat);
       argument.setAuthor(user);
       clearUndoStack();
     } catch (Exception e)
     {
       try
       {
         byteStream.close();
         rs.close();
         statement.close();
         statement = null;
         if (connection != null)
         {
           connection.close();
         }
       } catch (Exception ee)
       {
       }
       setMessageLabelText("Error reading database entry " + amlKey +
               ": " + e.toString());
       e.printStackTrace();
     }
   }
 
   public static void createIcon(Frame frame)
   {
     try
     {
 //      ClassLoader loader = frame.getClass().getClassLoader();
 //      if (loader == null) return;
       ImageIcon icon = new ImageIcon("images/AraucariaIcon.jpg");
       frame.setIconImage(icon.getImage());
     } catch (Exception ex)
     {
       System.out.println("ImageIcon Problem: " + ex);
     }
   }
 
   public static void checkVersion()
   {
     String version = System.getProperty("java.version");
     int[] versionComp = new int[3];
     StringTokenizer versionTok = new StringTokenizer(version, ".");
     int i = 0;
     while (versionTok.hasMoreTokens())
     {
       versionComp[i++] = Integer.parseInt(versionTok.nextToken().substring(0, 1));
     }
     int yesNo = 0;
     int versionNumber = versionComp[0] * 100 + versionComp[1] * 10 + versionComp[2];
     if (versionNumber < 141)
     {
       yesNo = JOptionPane.showConfirmDialog(null, "Araucaria requires Java Runtime Environment\n" +
               "version 1.4.1 or later to run. Your computer is using version " + version + ".\n" +
               "Araucaria may not work properly with this version.\nDo you want to continue?",
               "Old JRE version",
               JOptionPane.YES_NO_OPTION);
     }
     if (yesNo != 0)
     {
       System.exit(1);
     }
   }
 
   /**
    * For auto-marking with command line. The arguments required are:
    * -t <tutor AML filename>
    * -s <student AML filename>
    *
    * Assumed that these files are in the same directory as the Araucaria class files.
    */
   public static boolean processArgs(String[] args)
   {
     // If no args, start Araucaria as a GUI application
     if (args.length == 0)
     {
       return false;
     }
     Araucaria arau = new Araucaria();
     String tag = null, tutorFilename = null, studentFilename = null;
     try
     {
       for (int i = 0; i < args.length; i++)
       {
         if (args[i].equals("-t"))
         {
           tag = "-t";
         } else if (args[i].equals("-s"))
         {
           tag = "-s";
         } else if (tag.equals("-t"))
         {
           tutorFilename = args[i];
           tag = null;
         } else if (tag.equals("-s"))
         {
           studentFilename = args[i];
           tag = null;
         }
       }
     } catch (Exception ex)
     {
       System.out.println("Error in processArgs: " + ex.getMessage());
       System.exit(1);
     }
     if (tutorFilename == null || studentFilename == null)
     {
       System.out.println("Error in processArgs: tutorFile and/or studentFile is null.");
       System.exit(1);
     }
     MarkingDialog markingDialog = new MarkingDialog();
     markingDialog.tutorArg = new Argument();
     markingDialog.studentArg = new Argument();
     //studentFilename = Araucaria.amlDirectory + File.separator + studentFilename;
     File studentFile = new File(studentFilename);
     String processResult = markingDialog.processFile(markingDialog.studentArg, studentFile);
     if (!processResult.equals("OK"))
     {
       System.out.println("Error in processing student file " + studentFilename + ":\n" +
               processResult);
       System.exit(1);
     }
     //tutorFilename = Araucaria.amlDirectory + File.separator + tutorFilename;
     File tutorFile = new File(tutorFilename);
     processResult = markingDialog.processFile(markingDialog.tutorArg, tutorFile);
     if (!processResult.equals("OK"))
     {
       System.out.println("Error in processing tutor file " + tutorFilename + ":\n" +
               processResult);
       System.exit(1);
     }
     String results = markingDialog.buildPremiseTable();
     System.out.println("Results for tutor file " + tutorFilename + " and student file " +
             studentFilename + ":\n" + results);
     return true;
   }
 
   /**
    * @param args the command line arguments
    */
   public static void main(String args[])
   {
     if (processArgs(args))
     {
       System.exit(0);
     }
     checkVersion();
     new Araucaria().setVisible(true);
   }
 
   public UndoStack getUndoStack()
   {
     return undoStack;
   }
 
   public void doProperties()
   {
     propertiesDialog = new PropertiesDialog(Araucaria.this);
     propertiesDialog.setVisible(true);
     if (propertiesDialog.okPressed)
     {
       argument.author = propertiesDialog.author.getText();
       argument.source = propertiesDialog.sourceText.getText();
       argument.comments = propertiesDialog.commentsText.getText();
     }
   }
 
   public void addKeyResponses()
   {
     AbstractAction zoomDownAction = new AbstractAction()
     {
       public void actionPerformed(ActionEvent e)
       {
         zoomState--;
         if (zoomState < 0)
         {
           zoomState = ZOOM_STATES - 1;
         }
         changeDisplay(zoomState);
       }
     };
 
     AbstractAction zoomUpAction = new AbstractAction()
     {
       public void actionPerformed(ActionEvent e)
       {
         zoomState = (zoomState + 1) % ZOOM_STATES;
         changeDisplay(zoomState);
       }
     };
 
     AbstractAction styleDownAction = new AbstractAction()
     {
       public void actionPerformed(ActionEvent e)
       {
         diagramStyle--;
         if (diagramStyle < 0)
         {
           diagramStyle = DIAGRAM_STYLES - 1;
         }
         displayTabbedPane.setSelectedIndex(diagramStyle);
       }
     };
 
     AbstractAction styleUpAction = new AbstractAction()
     {
       public void actionPerformed(ActionEvent e)
       {
         diagramStyle = (diagramStyle + 1) % DIAGRAM_STYLES;
         displayTabbedPane.setSelectedIndex(diagramStyle);
       }
     };
 
     // Then create a keystroke to use for it
     KeyStroke controlMinus = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_MASK);
     KeyStroke controlEquals = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_MASK);
     KeyStroke controlComma = KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.CTRL_MASK);
     KeyStroke controlPeriod = KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, InputEvent.CTRL_MASK);
 
     // Finally, bind the keystroke and the action to *any* component
     // within the dialog. Note the WHEN_IN_FOCUSED bit...this is what
     // stops you having to do it for all components
 
     displayTabbedPane.getInputMap(
             JComponent.WHEN_IN_FOCUSED_WINDOW).put(controlMinus, "zoomDown");
     displayTabbedPane.getActionMap().put("zoomDown", zoomDownAction);
 
     displayTabbedPane.getInputMap(
             JComponent.WHEN_IN_FOCUSED_WINDOW).put(controlEquals, "zoomUp");
     displayTabbedPane.getActionMap().put("zoomUp", zoomUpAction);
 
     displayTabbedPane.getInputMap(
             JComponent.WHEN_IN_FOCUSED_WINDOW).put(controlComma, "styleDown");
     displayTabbedPane.getActionMap().put("styleDown", styleDownAction);
 
     displayTabbedPane.getInputMap(
             JComponent.WHEN_IN_FOCUSED_WINDOW).put(controlPeriod, "styleUp");
     displayTabbedPane.getActionMap().put("styleUp", styleUpAction);
   }
 
   /**
    * Handles events generated by the Araucaria components. FileActionHandler
    * extends AbstractAction, which allows an Action to be defined. This is most
    * useful for linking menu items and toolbar buttons.
    */
   class FileActionHandler extends AbstractAction
   {
     /**
      * Default constructor - does nothing.
      */
     FileActionHandler()
     {
     }
 
     /**
      * Used for defining a menu item or a toolbar button with both text and an
      * icon.
      * @param name The text to be displayed on the menu item
      * @param icon The icon to be displayed on the menu item or toolbar button
      */
     FileActionHandler(String name, Icon icon)
     {
       super(name, icon);
     }
 
     /**
      * Used for defining an action with a string only (no icon).
      * @param name The text to be displayed on the component.
      */
     FileActionHandler(String name)
     {
       super(name);
     }
 
     /**
      * The standard event handler for ActionEvents
      * @param event The event to be handled.
      */
     public void actionPerformed(java.awt.event.ActionEvent event)
     {
       if (event.getSource() instanceof RecentFilesMenuItem)
       {
         RecentFilesMenuItem item = (RecentFilesMenuItem) event.getSource();
         switch (item.fileType)
         {
           case TEXT:
             openRecentTextFile(item.getText());
             break;
           case AML:
             openAmlFile(item.getText());
             break;
           case SCHEMESET:
             openSchemeset(item.getText());
             break;
         }
         return;
       }
       if (event.getSource() == openTextMenuItem ||
               event.getSource() == openTextToolBar)
       {
         openTextFile();
       } else if (event.getSource() == openArgumentMenuItem ||
               event.getSource() == openArgumentToolBar)
       {
         readAML();
       } else if (event.getSource() == saveArgumentMenuItem ||
               event.getSource() == saveArgumentToolBar)
       {
         saveAML(currentOpenXMLFile == null);
       } else if (event.getSource() == saveAsArgumentMenuItem)
       {
         saveAML(true);
       } else if (event.getSource() == saveDiagramMenuItem ||
               event.getSource() == saveDiagramToolBar)
       {
         saveImageFile();
       } else if (event.getSource() == closeAllMenuItem)
       {
         closeAll();
       } else if (event.getSource() == propertiesMenu)
       {
         doProperties();
       } else if (event.getSource() == preferencesMenuItem)
       {
         preferencesDialog.setVisible(true);
         updateDisplays(true);
       } else if (event.getSource() == exitMenuItem)
       {
         exitApplication();
       } else if (event.getSource() == undoMenuItem ||
               event.getSource() == undoToolBar)
       {
         doUndo();
       } else if (event.getSource() == redoMenuItem ||
               event.getSource() == redoToolBar)
       {
         doRedo();
       } else if (event.getSource() == clearDiagramMenuItem ||
               event.getSource() == clearDiagramToolBar)
       {
         doClearDiagram();
       } else if (event.getSource() == flipDiagramMenuItem ||
               event.getSource() == flipDiagramToolBar)
       {
         doInvertDiagram();
       } else if (event.getSource() == missingPremiseMenuItem ||
               event.getSource() == missingPremiseToolBar)
       {
         addMissingPremise();
       } else if (event.getSource() == refutationMenuItem ||
               event.getSource() == refutationToolBar)
       {
         doRefutation();
       } else if (event.getSource() == deleteMenuItem ||
               event.getSource() == deleteToolBar)
       {
         doDeletion();
       } else if (event.getSource() == linkMenuItem ||
               event.getSource() == linkToolBar)
       {
         doLink();
       } else if (event.getSource() == unlinkMenuItem ||
               event.getSource() == unlinkToolBar)
       {
         doUnlink();
       } else if (event.getSource() == selectAllMenuItem)
       {
         doSelectAll();
       } else if (event.getSource() == modifyOwnershipMenuItem)
       {
         doModifyOwnership();
       } else if (event.getSource() == showOwnersMenuItem)
       {
         doShowOwners();
       } else if (event.getSource() == modifyEvaluationMenuItem)
       {
         doModifyEvaluation();
       } else if (event.getSource() == showEvaluationMenuItem)
       {
         doShowEvaluation();
       } else if (event.getSource() == selectSchemeMenuItem ||
               event.getSource() == selectSchemeToolBar)
       {
         doSelectScheme();
       } else if (event.getSource() == addEditSchemeMenuItem)
       {
         doAddEditScheme();
       } else if (event.getSource() == openSchemesetMenuItem)
       {
         doOpenSchemeset();
       } else if (event.getSource() == saveSchemesetMenuItem)
       {
         doSaveSchemeset();
       } else if (event.getSource() == showCQsMenuItem)
       {
         doShowCQs();
       } else if (event.getSource() == loginMenuItem)
       {
         loginDialog.setVisible(true);
       } else if (event.getSource() == registerMenuItem)
       {
         registrationDialog.setVisible(true);
       } else if (event.getSource() == saveDBMenuItem ||
               event.getSource() == saveDBToolBar)
       {
         doSaveDB();
       } else if (event.getSource() == searchDBMenuItem ||
               event.getSource() == searchDBToolBar)
       {
         doSearchDB();
       } else if (event.getSource() == searchResultCombo && doComboEvents)
       {
         displayTreeFromCombo();
       } else if (event.getSource() == premiseEndpointsMenu)
       {
         doPremiseEndpoints();
       } else if (event.getSource() == markingMenu)
       {
         doMarking();
       } else if (event.getSource() == helpMenuItem ||
               event.getSource() == helpToolBar)
       {
         (new HelpHTMLDialog(Araucaria.this)).setVisible(true);
       } else if (event.getSource() == aboutMenuItem)
       {
         new AboutDialog(Araucaria.this);
       } else if (event.getSource() == scaledMenuItem || event.getSource() == scaledRadio)
       {
         zoomState = ZOOM_SCALED;
         changeDisplay(DIAGRAM_FULL);
       } else if (event.getSource() == fullSizeMenuItem || event.getSource() == fullSizeRadio)
       {
         zoomState = ZOOM_FULLSIZE;
         changeDisplay(DIAGRAM_FULL_SIZE);
       } else if (event.getSource() == fullTextMenuItem || event.getSource() == fullTextRadio)
       {
         zoomState = ZOOM_FULLTEXT;
         changeDisplay(DIAGRAM_FULL_TEXT);
       } else if (event.getSource() == standardMenuItem)
       {
         displayTabbedPane.setSelectedIndex(0);
       } else if (event.getSource() == toulminMenuItem)
       {
         displayTabbedPane.setSelectedIndex(1);
       } else if (event.getSource() == wigmoreMenuItem)
       {
         displayTabbedPane.setSelectedIndex(2);
       }
     }
   }
 }
 
 /**
  * Utility class to provide a data type for recent file menu items.
  */
 class RecentFilesMenuItem extends JMenuItem
 {
   public enum FileType
   {
     TEXT, AML, SCHEMESET
   }
 
   
   
    ;
 
    FileType fileType ;  
   
     
       
   
 
 public RecentFilesMenuItem(String s, FileType t)
   {
     super(s);
     fileType = t;
   }
 }
 
