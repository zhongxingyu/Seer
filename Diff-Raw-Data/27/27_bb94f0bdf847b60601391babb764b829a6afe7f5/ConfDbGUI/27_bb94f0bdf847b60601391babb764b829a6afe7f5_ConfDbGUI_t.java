 package confdb.gui;
 
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.*;
 import javax.swing.table.*;
 import javax.swing.border.*;
 import javax.swing.plaf.basic.*;
 import java.awt.*;
 import java.awt.event.*;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.concurrent.ExecutionException;
 
 import java.io.FileWriter;
 import java.io.IOException;
 
 import confdb.gui.treetable.*;
 
 import confdb.db.ConfDB;
 import confdb.db.DatabaseException;
 
 import confdb.migrator.DatabaseMigrator;
 import confdb.migrator.ReleaseMigrator;
 
 import confdb.parser.PythonParser;
 import confdb.parser.ParserException;
 
 import confdb.converter.ConverterFactory;
 import confdb.converter.ConverterEngine;
 import confdb.converter.OfflineConverter;
 import confdb.converter.ConverterException;
 
 import confdb.data.*;
 
 
 /**
  * ConfDbGUI
  * ---------
  * @author Philipp Schieferdecker
  *
  * Graphical User Interface to create and manipulate CMSSW job
  * configurations stored in the relational configuration database,
  * ConfDB.
  */
 public class ConfDbGUI
 {
     //
     // member data
     //
 
     /** current user */
     private String userName = "";
     
     /** access to the ConfDB database */
     private ConfDB database = null;
     
     /** current software release (collection of all templates) */
     private SoftwareRelease currentRelease = null;
     
     /** the current configuration */
     private Configuration currentConfig = null;
     
     /** the current software release for imports */
     private SoftwareRelease importRelease = null;
 
     /** the import configuration */
     private Configuration importConfig = null;
     
     /** current instance and respective list of parameters currently displayed */
     private Object               currentInstance   = null;
     private ArrayList<Parameter> currentParameters = new ArrayList<Parameter>(); 
 
     /** ascii converter engine, to display configuration snippets (right-lower) */
     private ConverterEngine cnvEngine = null;
     
 
     /** TREE- & TABLE-MODELS */
     private ConfigurationTreeModel treeModelCurrentConfig;
     private ConfigurationTreeModel treeModelImportConfig;
     private StreamTreeModel        treeModelStreams;
     //private PrescaleTreeModel    treeModelPrescales;
     private ParameterTreeModel     treeModelParameters;
 
     /** GUI COMPONENTS */
     private JFrame        frame;
 
     private MenuBar       menuBar;
     private ToolBar       toolBar;
 
     private JPanel        jPanelContentPane         = new JPanel();
     private JMenuBar      jMenuBar                  = new JMenuBar();
     private JToolBar      jToolBar                  = new JToolBar();
     private JPanel        jPanelDbConnection        = new JPanel();
     private JSplitPane    jSplitPane                = new JSplitPane();
     private JSplitPane    jSplitPaneRight           = new JSplitPane();
 
     private JPanel        jPanelLeft                = new JPanel();
     private JTextField    jTextFieldCurrentConfig   = new JTextField();
     private JLabel        jLabelLock                = new JLabel();
     private JTextField    jTextFieldProcess         = new JTextField();     // AL
     private JButton       jButtonRelease            = new JButton();        // AL
     private JTextField    jTextFieldCreated         = new JTextField();
     private JTextField    jTextFieldCreator         = new JTextField();
     private JTabbedPane   jTabbedPaneLeft           = new JTabbedPane();
 
     private JPanel        jPanelCurrentConfig       = new JPanel();
     private JLabel        jLabelSearch              = new JLabel();        // ML
     private JPopupMenu    jPopupMenuSearch          = new JPopupMenu();
     private ButtonGroup   buttonGroupSearch1;
     private ButtonGroup   buttonGroupSearch2;
     private JTextField    jTextFieldSearch          = new JTextField();    // KL
     private JButton       jButtonCancelSearch       = new JButton();       // AL
     private JToggleButton jToggleButtonImport       = new JToggleButton(); // AL
     private JSplitPane    jSplitPaneCurrentConfig   = new JSplitPane();
     private JScrollPane   jScrollPaneCurrentConfig  = new JScrollPane();
     private JTree         jTreeCurrentConfig;                              //TML+TSL
 
     private JPanel        jPanelImportConfig        = new JPanel();
     private JLabel        jLabelImportSearch        = new JLabel();        // ML
     private JPopupMenu    jPopupMenuImportSearch    = new JPopupMenu();
     private ButtonGroup   buttonGroupImportSearch1;
     private ButtonGroup   buttonGroupImportSearch2;
     private JTextField    jTextFieldImportSearch    = new JTextField();    // KL
     private JButton       jButtonImportCancelSearch = new JButton();       // AL
     private JScrollPane   jScrollPaneImportConfig   = new JScrollPane();
     private JTree         jTreeImportConfig;                               //TML+TSL
 
     private JPanel        jPanelStreams             = new JPanel();
     private JComboBox     jComboBoxDefaultStream    = new JComboBox();     // AL
     private JScrollPane   jScrollPaneStreams        = new JScrollPane();
     private JTree         jTreeStreams;
     private JButton       jButtonAddStream          = new JButton();       // AL
     private JButton       jButtonDeleteStream       = new JButton();       // AL
     private JTextField    jTextFieldUnassignedPaths = new JTextField();
 
     private JPanel        jPanelPrescales           = new JPanel();
     private JScrollPane   jScrollPanePrescales      = new JScrollPane();
     private JTable        jTablePrescales           = new JTable();
     
     private JPanel        jPanelRightUpper          = new JPanel();
     private JSplitPane    jSplitPaneRightUpper      = new JSplitPane();
     private JPanel        jPanelPlugin              = new JPanel();
     private JTextField    jTextFieldPackage         = new JTextField();
     private JTextField    jTextFieldCVS             = new JTextField();
     private JLabel        jLabelPlugin              = new JLabel();
     private JTextField    jTextFieldPlugin          = new JTextField();
     private JTextField    jTextFieldLabel           = new JTextField();
     private JComboBox     jComboBoxPaths            = new JComboBox();
     private JScrollPane   jScrollPaneParameters     = new JScrollPane();
     private TreeTable     jTreeTableParameters;
     
     private JPanel        jPanelRightLower          = new JPanel();
     private JTabbedPane   jTabbedPaneRightLower     = new JTabbedPane();
     private JScrollPane   jScrollPaneRightLower     = new JScrollPane();
     private JEditorPane   jEditorPaneSnippet        = new JEditorPane();
 
     
     private JProgressBar  jProgressBar              = new JProgressBar(); 
     
     
     
 
     //
     // construction
     //
     
     /** standard constructor */
     public ConfDbGUI(JFrame frame)
     {
 	this.userName = System.getProperty("user.name");
 	this.frame    = frame;
 	
 	this.database         = new ConfDB();
 	this.currentRelease   = new SoftwareRelease();
 	this.currentConfig    = new Configuration();
 	this.importRelease    = new SoftwareRelease();
 	this.importConfig     = new Configuration();
 	
 	try {
 	    this.cnvEngine = ConverterFactory.getConverterEngine("ascii");
 	}
 	catch (Exception e) {
 	    System.out.println("failed to initialize converter engine: " +
 			       e.getMessage());
 	}
 	
 	createTreesAndTables();
 	createContentPane();
 	hideImportTree();
 	
 	frame.setContentPane(jPanelContentPane);
 	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     
 	jTextFieldProcess.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jButtonProcessActionPerformed(e);
 		}
 	    });
 	jButtonRelease.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jButtonReleaseActionPerformed(e);
 		}
 	    });
 	jLabelSearch.addMouseListener(new MouseAdapter() {
 		public void mousePressed(MouseEvent e)  { maybeShowPopup(e); }
 		public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
 		public void maybeShowPopup(MouseEvent e) {
 		    if (e.isPopupTrigger())
 			jPopupMenuSearch.show(e.getComponent(),e.getX(),e.getY());
 		}
 	    });
 	jTextFieldSearch.getDocument().addDocumentListener(new DocumentListener() {
 		public void insertUpdate(DocumentEvent e) {
 		    jTextFieldSearchInsertUpdate(e);
 		}
 		public void removeUpdate(DocumentEvent e) {
 		    jTextFieldSearchRemoveUpdate(e);
 		}
 		public void changedUpdate(DocumentEvent e) {}
 	    });
 	jButtonCancelSearch.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jButtonCancelSearchActionPerformed(e);
 		}
 	    });
 	jToggleButtonImport.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jToggleButtonImportActionPerformed(e);
 		}
 	    });
 	jTreeCurrentConfig.
 	    getModel().addTreeModelListener(new TreeModelListener() {
 		    public void treeNodesChanged(TreeModelEvent e) {
 			jTreeCurrentConfigTreeNodesChanged(e);
 		    }
 		    public void treeNodesInserted(TreeModelEvent e) {
 			jTreeCurrentConfigTreeNodesInserted(e);
 		    }
 		    public void treeNodesRemoved(TreeModelEvent e) {
 			jTreeCurrentConfigTreeNodesRemoved(e);
 		    }
 		    public void treeStructureChanged(TreeModelEvent e) {
 			jTreeCurrentConfigTreeStructureChanged(e);
 		    }
 		});
 	jTreeCurrentConfig.addTreeSelectionListener(new TreeSelectionListener() {
 		public void valueChanged(TreeSelectionEvent e) {
 		    jTreeCurrentConfigValueChanged(e);
 		}
 	    });
 	jLabelImportSearch.addMouseListener(new MouseAdapter() {
 		public void mousePressed(MouseEvent e)  { maybeShowPopup(e); }
 		public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
 		public void maybeShowPopup(MouseEvent e) {
 		    if (e.isPopupTrigger())
 			jPopupMenuImportSearch.show(e.getComponent(),e.getX(),e.getY());
 		}
 	    });
 	jTextFieldImportSearch.getDocument().addDocumentListener(new DocumentListener() {
 		public void insertUpdate(DocumentEvent e) {
 		    jTextFieldImportSearchInsertUpdate(e);
 		}
 		public void removeUpdate(DocumentEvent e) {
 		    jTextFieldImportSearchRemoveUpdate(e);
 		}
 		public void changedUpdate(DocumentEvent e) {}
 	    });
 	jButtonImportCancelSearch.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jButtonImportCancelSearchActionPerformed(e);
 		}
 	    });
 	jComboBoxDefaultStream.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jComboBoxDefaultStreamActionPerformed(e);
 		}
 	    });
 	jButtonAddStream.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jButtonAddStreamActionPerformed(e);
 		}
 	    });
 	jButtonDeleteStream.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jButtonDeleteStreamActionPerformed(e);
 		}
 	    });
 	jTreeStreams.
 	    getModel().addTreeModelListener(new TreeModelListener() {
 		    public void treeNodesChanged(TreeModelEvent e) {
 			jTreeStreamsTreeNodesChanged(e);
 		    }
 		    public void treeNodesInserted(TreeModelEvent e) {
 			jTreeStreamsTreeNodesInserted(e);
 		    }
 		    public void treeNodesRemoved(TreeModelEvent e) {
 			jTreeStreamsTreeNodesRemoved(e);
 		    }
 		    public void treeStructureChanged(TreeModelEvent e) {
 			jTreeStreamsTreeStructureChanged(e);
 		    }
 		});
 	jTreeStreams.addTreeSelectionListener(new TreeSelectionListener() {
 		public void valueChanged(TreeSelectionEvent e) {
 		    jTreeStreamsValueChanged(e);
 		}
 	    });
 	// jTablePrescales
 	jTreeTableParameters.
 	    getModel().addTableModelListener(new TableModelListener() {
 		    public void tableChanged(TableModelEvent e) {
 			jTreeTableParametersTableChanged(e);
 		    }
 		});
 	
 	((BasicSplitPaneDivider)((BasicSplitPaneUI)jSplitPaneRight.
 				 getUI()).getDivider()).
 	    addComponentListener(new ComponentListener() {
 		    public void componentHidden(ComponentEvent e) {}
 		    public void componentMoved(ComponentEvent e) {
 			jSplitPaneRightComponentMoved(e);
 		    }
 		    public void componentResized(ComponentEvent e) {}
 		    public void componentShown(ComponentEvent e) {}
 		});
 	    
 	    frame.addWindowListener(new WindowAdapter() {
 		public void windowClosing(WindowEvent e)
 		{
 		    closeConfiguration(false);
 		    disconnectFromDatabase();
 		}
 	    });
 	Runtime.getRuntime().addShutdownHook(new Thread() {
 		public void run()
 		{
 		    closeConfiguration(false);
 		    disconnectFromDatabase();
 		}
 	    });
     }
     
     
     //
     // main
     //
     
     /** main method, thread-safe call to createAndShowGUI */
     public static void main(String[] args)
     {
 	javax.swing.SwingUtilities.invokeLater(new Runnable() {
 		public void run() { createAndShowGUI(); }
 	    });
     }
     
     /** create the GUI and show it */
     private static void createAndShowGUI()
     {
 	JFrame frame = new JFrame("ConfDbGUI");
 	ConfDbGUI gui = new ConfDbGUI(frame);
 	
 	int frameWidth  = (int)(0.75*frame.getToolkit().getScreenSize().getWidth());
 	int frameHeight = (int)(0.75*frame.getToolkit().getScreenSize().getHeight());
 	int frameX      = (int)(0.125*frame.getToolkit().getScreenSize().getWidth());
 	int frameY      = (int)(0.10*frame.getToolkit().getScreenSize().getHeight());
 
 	frame.pack();
 	frame.setSize(frameWidth,frameHeight);
 	frame.setLocation(frameX,frameY);
 	frame.setVisible(true);
 	
 	gui.connectToDatabase();
     }
     
 
     //
     // member functions
     //
     
     /** get the main frame */
     public JFrame getFrame() { return frame; }
 
     /** show the 'about' dialog */
     public void showAboutDialog()
     {
 	AboutDialog dialog = new AboutDialog(frame);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
     }
     
     /** quit the GUI application */
     public void quitApplication()
     {
 	if (closeConfiguration()) {
 	    disconnectFromDatabase();
 	    System.exit(0);
 	}
     }
 
     /** create a new configuration */
     public void newConfiguration()
     {
 	if (!closeConfiguration()) return;
 	
 	NewConfigurationDialog dialog = new NewConfigurationDialog(frame,database);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    String name       = dialog.name();
 	    String process    = dialog.process();
 	    String releaseTag = dialog.releaseTag();
 	    
 	    NewConfigurationThread worker =
 		new NewConfigurationThread(name,process,releaseTag);
 	    worker.start();
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Loading Templates for Release " +
 				  dialog.releaseTag() + " ... ");
 	    menuBar.configurationIsOpen();
 	    toolBar.configurationIsOpen();
 	}
     }
 
     /** parse a configuration from a *.py file */
     public void parseConfiguration()
     {
 	if (!closeConfiguration()) return;
 	
 	ParseConfigurationDialog dialog =
 	    new ParseConfigurationDialog(frame,database);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    String fileName   = dialog.fileName();
 	    String releaseTag = dialog.releaseTag();
 	    
 	    ParseConfigurationThread worker =
 		new ParseConfigurationThread(fileName,releaseTag);
 	    worker.start();
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Parsing '"+fileName+"' against Release " +
 				  releaseTag + " ... ");
 	    menuBar.configurationIsOpen();
 	    toolBar.configurationIsOpen();
 	}
     }
 
     /** open an existing configuration */
     public void openConfiguration()
     {
 	if (database.dbUrl().equals(new String())) return;
 	if (!closeConfiguration()) return;
 	
 	OpenConfigurationDialog dialog =
 	    new OpenConfigurationDialog(frame,database);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    OpenConfigurationThread worker =
 		new OpenConfigurationThread(dialog.configInfo());
 	    worker.start();
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Loading Configuration ...");
 	    menuBar.configurationIsOpen();
 	    toolBar.configurationIsOpen();
 	}
     }
 
     /** close the current configuration */
     public boolean closeConfiguration()
     {
 	return closeConfiguration(true);
     } 
 
     /** close the current configuration */
     public boolean closeConfiguration(boolean showDialog)
     {
 	if (currentConfig.isEmpty()) return true;
 	
 	if (currentConfig.hasChanged()&&showDialog) {
 	    Object[] options = { "OK", "CANCEL" };
 	    int answer = 
 		JOptionPane.showOptionDialog(null,
 					     "The current configuration contains "+
 					     "unsaved changes, really close?",
 					     "Warning",
 					     JOptionPane.DEFAULT_OPTION,
 					     JOptionPane.WARNING_MESSAGE,
 					     null, options, options[1]);
 	    if (answer==1) return false;
 	}
 	
 	if (!currentConfig.isLocked()&&currentConfig.version()>0)
 	    database.unlockConfiguration(currentConfig);
 
 	resetConfiguration();
 	
 	return true;
     } 
     
     /** save a new version of the current configuration */
     public void saveConfiguration(boolean askForComment)
     {
 	if (currentConfig.isEmpty()||!currentConfig.hasChanged()||
 	    currentConfig.isLocked()||!checkConfiguration()) return;	
 	
 	if (currentConfig.version()==0) {
 	    saveAsConfiguration();
 	    return;
 	}
 	else database.unlockConfiguration(currentConfig);
 	
 	String processName = jTextFieldProcess.getText();
 	String comment = "";
 
 	if (askForComment) {
 	    String fullConfigName =
 		currentConfig.parentDir().name()+"/"+currentConfig.name();
 	    comment = (String)JOptionPane
 		.showInputDialog(frame,"Enter comment for the new version of " +
 				 fullConfigName+":","Enter comment",
 				 JOptionPane.PLAIN_MESSAGE,
 				 null,null,"");
 	}
 	
 	SaveConfigurationThread worker =
 	    new SaveConfigurationThread(processName,comment);
 	worker.start();
 	jProgressBar.setIndeterminate(true);
 	jProgressBar.setString("Save Configuration ...");
 	jProgressBar.setVisible(true);
     }
     
     /** save the current configuration under a new name */
     public void saveAsConfiguration()
     {
 	if (!checkConfiguration()) return;
 	
 	boolean isLocked = currentConfig.isLocked();
 	if (currentConfig.version()!=0&&!isLocked)
 	    database.unlockConfiguration(currentConfig);
 	
 	String processName = jTextFieldProcess.getText();
 	String comment = (currentConfig.version()==0) ?
 	    "first import" : "saveAs "+currentConfig+" ["+currentConfig.dbId()+"]";
 	
 	SaveConfigurationDialog dialog =
 	    new SaveConfigurationDialog(frame,database,currentConfig,comment);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    SaveConfigurationThread worker =
 		new SaveConfigurationThread(processName,dialog.comment());
 	    worker.start();
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setString("Save Configuration ...");
 	    jProgressBar.setVisible(true);
 	}
 	else if (currentConfig.version()!=0&&!isLocked)
 	    database.lockConfiguration(currentConfig,userName);
     }
     
     /** one another configuration to import components */
     public void importConfiguration()
     {
 	ImportConfigurationDialog dialog =
 	    new ImportConfigurationDialog(frame,database,
 					  currentRelease.releaseTag());
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    ImportConfigurationThread worker =
 		new ImportConfigurationThread(dialog.configInfo());
 	    worker.start();
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Importing Configuration ...");
 	}	
     }
     
     /** migrate the current configuration to a new release */
     public void migrateConfiguration()
     {
 	if (!checkConfiguration()) return;
 	
 	MigrateConfigurationDialog dialog =
 	    new MigrateConfigurationDialog(frame,database);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	String releaseTag = dialog.releaseTag();	
 	
 	if (releaseTag.length()>0) {
 	    MigrateConfigurationThread worker =
 		new MigrateConfigurationThread(releaseTag);
 	    worker.start();
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Migrate configuration to release '" +
 				  releaseTag + "' ... ");
 	}
     }
     
     /** convert the current configuration to a text file (ascii, python, or html) */
     public void convertConfiguration()
     {
 	if (!checkConfiguration()) return;
 	
 	ConvertConfigurationDialog dialog =
 	    new ConvertConfigurationDialog(frame,currentConfig);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (!dialog.isCanceled()) {
 	    ConvertConfigurationThread worker = 
 		new ConvertConfigurationThread(dialog.configToConvert(),
 					       dialog.fileName(),
 					       dialog.format(),
 					       dialog.asFragment());
 	    worker.start();
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Convert configuration '"+currentConfig.name()+
 				  "' ... ");
 	}
     }
     
     /** set option 'Track InputTags' */
     public void setOptionTrackInputTags(boolean doTrack)
     {
 	ConfigurationTreeRenderer renderer =
 	    (ConfigurationTreeRenderer)jTreeCurrentConfig.getCellRenderer();
 	renderer.displayUnresolvedInputTags(doTrack);
 	
 	int pathIndices[] = new int[currentConfig.pathCount()];
 	for (int i=0;i<currentConfig.pathCount();i++) pathIndices[i]=i;
 	treeModelCurrentConfig.childNodesChanged(treeModelCurrentConfig.pathsNode(),
 						 pathIndices);
     }
 
     /** connect to the database */
     public void connectToDatabase()
     {
 	disconnectFromDatabase();
 	
 	DatabaseConnectionDialog dbDialog = new DatabaseConnectionDialog(frame);
 	dbDialog.pack();
 	dbDialog.setLocationRelativeTo(frame);
 	dbDialog.setVisible(true);
 	
 	if (!dbDialog.validChoice()) return;
 	String dbType = dbDialog.getDbType();
 	String dbHost = dbDialog.getDbHost();
 	String dbPort = dbDialog.getDbPort();
 	String dbName = dbDialog.getDbName();
 	String dbUrl  = dbDialog.getDbUrl();
 	String dbUser = dbDialog.getDbUser();
 	String dbPwrd = dbDialog.getDbPassword();
 	
 	try {
 	    database.connect(dbType,dbUrl,dbUser,dbPwrd);
 	    ((DatabaseInfoPanel)jPanelDbConnection).connectedToDatabase(dbType,
 									dbHost,
 									dbPort,
 									dbName,
 									dbUser);
 	}
 	catch (DatabaseException e) {
 	    String msg = "Failed to connect to DB: " + e.getMessage();
 	    JOptionPane.showMessageDialog(frame,msg,"",JOptionPane.ERROR_MESSAGE);
 	}
 	menuBar.dbConnectionIsEstablished();
 	toolBar.dbConnectionIsEstablished();
     }
 
     /** disconnect from the  database */
     public void disconnectFromDatabase()
     {
 	if (!closeConfiguration()) return;
 	
 	try {
 	    database.disconnect();
 	    ((DatabaseInfoPanel)jPanelDbConnection).disconnectedFromDatabase();
 	    currentRelease.clear("");
 	}
 	catch (DatabaseException e) {
 	    String msg = "Failed to disconnect from DB: " + e.getMessage();
 	    JOptionPane.showMessageDialog(frame,msg,"",JOptionPane.ERROR_MESSAGE);
 	}
 	menuBar.dbConnectionIsNotEstablished();
 	toolBar.dbConnectionIsNotEstablished();
     }
 
     /** export the current configuration to a new database */
     public void exportConfiguration()
     {
 	if (!checkConfiguration()) return;
 	
 	ExportConfigurationDialog dialog =
 	    new ExportConfigurationDialog(frame,currentConfig.name());
 	
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    ConfDB      targetDB   = dialog.targetDB();
 	    String      targetName = dialog.targetName();
 	    Directory   targetDir  = dialog.targetDir();
 	    
 	    ExportConfigurationThread worker =
 		new ExportConfigurationThread(targetDB,targetName,targetDir);
 	    worker.start();
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Migrate Configuration to " +
 				  targetDB.dbUrl() + " ... ");
 	}
     }
     
     
     /** reset current and import configuration */
     private void resetConfiguration()
     {
 	currentRelease.clearInstances();
 	
 	currentConfig.reset();
 	treeModelCurrentConfig.setConfiguration(currentConfig);
 	treeModelStreams.setConfiguration(currentConfig);
 	
 	jTextFieldCurrentConfig.setText("");
 	jTextFieldCurrentConfig.setToolTipText("");
 	jLabelLock.setIcon(null);
 	jTextFieldProcess.setText("");
 	jButtonRelease.setText("");
 	jTextFieldCreated.setText("");
 	jTextFieldCreator.setText("");
 	
 	clearParameters();
 	clearSnippet();
 	
 	menuBar.configurationIsNotOpen();
 	toolBar.configurationIsNotOpen();
 
 	importConfig.reset();
 	treeModelImportConfig.setConfiguration(importConfig);
 	hideImportTree();
 
 	jTextFieldProcess.setEditable(false);
 	jToggleButtonImport.setEnabled(false);
 	jButtonAddStream.setEnabled(false);
     }
 
     /** check that the current configuration is in a valid state for save/convert*/
     private boolean checkConfiguration()
     {
 	if (currentConfig.isEmpty()) return false;
 	
 	int unsetParamCount = currentConfig.unsetTrackedParameterCount();
 	if (unsetParamCount>0) {
 	    String msg =
 		"current configuration contains " + unsetParamCount +
 		" unset tracked parameters. They must be set before " +
 		"saving/converting!";
 	    JOptionPane.showMessageDialog(frame,msg,"",JOptionPane.ERROR_MESSAGE);
 	    return false;
 	}
 
 	int emptyContainerCount = currentConfig.emptyContainerCount();
 	if (emptyContainerCount>0) {
 	    String msg =
 		"current configuration contains " + emptyContainerCount +
 		"empty containers (paths/sequences). They must be filled before " +
 		"saving/converting!";
 	    JOptionPane.showMessageDialog(frame,msg,"",JOptionPane.ERROR_MESSAGE);
 	    return false;
 	}
 	
 	return true;
     }
 
 
     /** set the current configuration */
     private void setCurrentConfig(Configuration config)
     {
	TreePath tp = jTreeCurrentConfig.getSelectionPath();
 	currentConfig = config;
 	treeModelCurrentConfig.setConfiguration(currentConfig);
 	treeModelStreams.setConfiguration(currentConfig);
 	currentRelease = currentConfig.release();
	jTreeCurrentConfig.scrollPathToVisible(tp);
	jTreeCurrentConfig.setSelectionPath(tp);

 	jTextFieldCurrentConfig.setText(currentConfig.toString());
 	if (currentConfig.version()>0)
 	    jTextFieldCurrentConfig.setToolTipText("id:"+
 						   currentConfig.dbId()+
 						   "  comment:"+
 						   currentConfig.comment());
 	
 	if (currentConfig.isLocked()) {
 	    jLabelLock.setIcon(new ImageIcon(getClass().
 					     getResource("/LockedIcon.png")));
 	    jLabelLock.setToolTipText("locked by user " +
 				      currentConfig.lockedByUser());
 	}
 	else {
 	    jLabelLock.setIcon(new ImageIcon(getClass().
 					     getResource("/UnlockedIcon.png")));
 	    jLabelLock.setToolTipText("It's all yours, nobody else can "+
 				      "modify this configuration until closed!");
 	}
 	
 	jTextFieldProcess.setText(currentConfig.processName());
 	jButtonRelease.setText(currentRelease.releaseTag());
 	jTextFieldCreated.setText(currentConfig.created());
 	jTextFieldCreator.setText(currentConfig.creator());
 
 	jTextFieldProcess.setEditable(true);
 	jButtonAddStream.setEnabled(true);
     }
     
     
     //
     // THREADS
     //
     
     /** migrate current configuration to another database  */
     private class ExportConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private ConfDB           targetDB   = null;
 	private String           targetName = null;
 	private Directory        targetDir  = null;
 	private DatabaseMigrator migrator   = null;
 	private long             startTime;
 	
 	/** standard constructor */
 	public ExportConfigurationThread(ConfDB targetDB,
 					 String targetName,Directory targetDir)
 	{
 	    this.targetDB   = targetDB;
 	    this.targetName = targetName;
 	    this.targetDir  = targetDir;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws InterruptedException
 	{
 	    startTime = System.currentTimeMillis();
 	    migrator = new DatabaseMigrator(currentConfig,database,targetDB);
 	    migrator.migrate(targetName,targetDir);
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		targetDB.disconnect();
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString() +
 				      get() + " (" + elapsedTime + " ms)");
 		MigrationReportDialog dialog =
 		    new MigrationReportDialog(frame,migrator.releaseMigrator());
 		dialog.setTitle("Configuration Export Report");
 		dialog.pack();
 		dialog.setLocationRelativeTo(frame);
 		dialog.setVisible(true);
 	    }
 	    catch (Exception e) {
 		System.out.println("EXCEPTION: "+ e.getMessage());
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString() + "FAILED!");	
 	    }
 	    jProgressBar.setIndeterminate(false);
 	}
     }
     
     
     /** load release templates from the database */
     private class NewConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private String name       = null;
 	private String process    = null;
 	private String releaseTag = null;
 	private long   startTime;
 	
 	/** standard constructor */
 	public NewConfigurationThread(String name,String process,String releaseTag)
 	{
 	    this.name       = name;
 	    this.process    = process;
 	    this.releaseTag = releaseTag;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws InterruptedException
 	{
 	    startTime = System.currentTimeMillis();
 	    if (!releaseTag.equals(currentRelease.releaseTag()))
 		database.loadSoftwareRelease(releaseTag,currentRelease);
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		Configuration config = new Configuration();
 		config.initialize(new ConfigInfo(name,null,releaseTag),
 				  currentRelease);
 		setCurrentConfig(config);
 		jTextFieldProcess.setText(process);
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString() +
 				      get() + " (" + elapsedTime + " ms)");
 	    }
 	    catch (Exception e) {
 		System.out.println("EXCEPTION: "+ e.getMessage());
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString() + "FAILED!");	
 	    }
 	    jProgressBar.setIndeterminate(false);
 
 	    jTreeCurrentConfig.setEditable(true);
 	    jTreeTableParameters.getTree().setEditable(true);
 	}
     }
     
 
     /** load release templates from the database and parse config from *.py */
     private class ParseConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private String fileName   = null;
 	private String releaseTag = null;
 	private long   startTime;
 	
 	/** standard constructor */
 	public ParseConfigurationThread(String fileName,String releaseTag)
 	{
 	    this.fileName   = fileName;
 	    this.releaseTag = releaseTag;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws InterruptedException
 	{
 	    startTime = System.currentTimeMillis();
 	    if (!releaseTag.equals(currentRelease.releaseTag()))
 		database.loadSoftwareRelease(releaseTag,currentRelease);
 
 	    try {
 		PythonParser parser = new PythonParser(currentRelease);
 		parser.parseFile(fileName);
 		setCurrentConfig(parser.createConfiguration());
 		if (parser.closeProblemStream())
 		    System.out.println("problems encountered, " +
 				       "see problems.txt.");
 	    }
 	    catch (ParserException e) {
 		System.err.println("Error parsing "+fileName+": "+
 				   e.getMessage());
 		return new String("FAILED!");
 	    }
 	    
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString() +
 				      get() + " (" + elapsedTime + " ms)");
 	    }
 	    catch (Exception e) {
 		System.err.println("EXCEPTION: "+ e.getMessage());
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString() + "FAILED!");	
 	    }
 	    jProgressBar.setIndeterminate(false);
 
 	    jTreeCurrentConfig.setEditable(true);
 	    jTreeTableParameters.getTree().setEditable(true);
 	}
     }
     
 
     /** load a configuration from the database  */
     private class OpenConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private ConfigInfo configInfo = null;
 	private long       startTime;
 	
 	/** standard constructor */
 	public OpenConfigurationThread(ConfigInfo configInfo)
 	{
 	    this.configInfo = configInfo;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws InterruptedException
 	{
 	    startTime = System.currentTimeMillis();
 	    setCurrentConfig(database.loadConfiguration(configInfo,currentRelease));
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString() +
 				      get() + " (" + elapsedTime + " ms)");
 	    
 	    }
 	    catch (ExecutionException e) {
 		System.out.println("EXECUTION-EXCEPTION: "+ e.getCause());
 		e.printStackTrace();
 	    }
 	    catch (Exception e) {
 		System.out.println("EXCEPTION: "+ e.getMessage());
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString() + "FAILED!");
 	    }
 	    jProgressBar.setIndeterminate(false);
 
 	    if (currentConfig.isLocked()) {
 		jTreeCurrentConfig.setEditable(false);
 		jTreeTableParameters.getTree().setEditable(false);
 		String msg =
 		    "The configuration '" +
 		    currentConfig.parentDir().name() + "/" +
 		    currentConfig.name() + " is locked by user '" +
 		    currentConfig.lockedByUser() + "'!\n" +
 		    "You can't manipulate any of its versions " +
 		    "until it is released.";
 		JOptionPane.showMessageDialog(frame,msg,"READ ONLY!",
 					      JOptionPane.WARNING_MESSAGE,
 					      null);
 	    }
 	    else {
 		jTreeCurrentConfig.setEditable(true);
 		jTreeTableParameters.getTree().setEditable(true);
 		database.lockConfiguration(currentConfig,userName);
 	    }
 	}
     }
 
     
     /** import a configuration from the database */
     private class ImportConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private ConfigInfo configInfo = null;
 	private long       startTime;
 	
 	/** standard constructor */
 	public ImportConfigurationThread(ConfigInfo configInfo)
 	{
 	    this.configInfo = configInfo;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws InterruptedException
 	{
 	    startTime = System.currentTimeMillis();
 	    if (importRelease.releaseTag()!=currentRelease.releaseTag()) {
 		importRelease = new SoftwareRelease(currentRelease);
 	    }
 	    importConfig = database.loadConfiguration(configInfo,importRelease);
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		treeModelImportConfig.setConfiguration(importConfig);
 		showImportTree();
 		jToggleButtonImport.setEnabled(true);
 		jToggleButtonImport.setSelected(true);
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString() +
 				      get() + " (" + elapsedTime + " ms)");
 	    }
 	    catch (ExecutionException e) {
 		System.out.println("EXECUTION-EXCEPTION: " + e.getCause());
 		e.printStackTrace();
 	    }
 	    catch (Exception e) {
 		System.out.println("EXCEPTION: "+ e.getMessage());
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString() + "FAILED!");
 	    }
 	    jProgressBar.setIndeterminate(false);
 	}
     }
     
     /** save a configuration in the database */
     private class SaveConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private long   startTime;
 	private String processName;
 	private String comment;
 	
 	/** standard constructor */
 	public SaveConfigurationThread(String processName,String comment)
 	{
 	    this.processName = processName;
 	    this.comment     = comment;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws InterruptedException
 	{
 	    startTime = System.currentTimeMillis();
 	    database.insertConfiguration(currentConfig,
 					 userName,processName,comment);
 	    if (!currentConfig.isLocked())
 		database.lockConfiguration(currentConfig,userName);
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		setCurrentConfig(currentConfig);
 		currentConfig.setHasChanged(false);
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString() +
 				      get() + " (" + elapsedTime + " ms)");
 	    }
 	    catch (Exception e) {
 		System.out.println("EXCEPTION: "+ e.getMessage());
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString() + "FAILED!");
 	    }
 	    jProgressBar.setIndeterminate(false);
 	}
     }
 
     /** migrate a configuration in the database to a new release */
     private class MigrateConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private String          targetReleaseTag = null;
 	private ReleaseMigrator migrator         = null;
 	private long            startTime;
 	
 	/** standard constructor */
 	public MigrateConfigurationThread(String targetReleaseTag)
 	{
 	    this.targetReleaseTag = targetReleaseTag;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws InterruptedException
 	{
 	    startTime = System.currentTimeMillis();
 	    
 	    SoftwareRelease targetRelease = new SoftwareRelease();
 	    database.loadSoftwareRelease(targetReleaseTag,targetRelease);
 
 	    String targetProcessName = currentConfig.processName();
 
 	    ConfigInfo targetConfigInfo =
 		new ConfigInfo(currentConfig.name(),currentConfig.parentDir(),
 			       -1,currentConfig.version(),"",userName,
 			       targetReleaseTag,targetProcessName,
 			       "migrated from external database");
 
 	    Configuration targetConfig = new Configuration(targetConfigInfo,
 							   targetRelease);
 	    
 	    migrator = new ReleaseMigrator(currentConfig,targetConfig);
 	    migrator.migrate();
 	    
 	    setCurrentConfig(targetConfig);
 	    //database.insertConfiguration(targetConfig,userName,targetProcessName);
 	    //currentRelease.clearInstances();
 	    //currentConfig =
 	    //	database.loadConfiguration(targetConfigInfo,currentRelease);
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		//currentConfig.setHasChanged(false);
 		//treeModelCurrentConfig.setConfiguration(currentConfig);
 		//treeModelStreams.setConfiguration(currentConfig);
 		//configurationPanel.setCurrentConfig(currentConfig);
 
 		clearParameters();
 		
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString() +
 				      get() + " (" + elapsedTime + " ms)");
 		MigrationReportDialog dialog =
 		    new MigrationReportDialog(frame,migrator);
 		dialog.setTitle("Release-Migration Report");
 		dialog.pack();
 		dialog.setLocationRelativeTo(frame);
 		dialog.setVisible(true);
 	    }
 	    catch (Exception e) {
 		System.out.println("EXCEPTION: "+ e.getMessage());
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString() + "FAILED!");
 	    }
 	    jProgressBar.setIndeterminate(false);
 	}
     }
 
     /** convert configuration to a text file */
     private class ConvertConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private IConfiguration config     = null;
 	private String         fileName   = null;
 	private String         format     = null;
 	private boolean        asFragment = false;
 	private long           startTime;
 	
 	/** standard constructor */
 	public ConvertConfigurationThread(IConfiguration config,
 					  String  fileName,
 					  String  format,
 					  boolean asFragment)
 	{
 	    this.config     = config;
 	    this.fileName   = fileName;
 	    this.format     = format;
 	    this.asFragment = asFragment;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws InterruptedException
 	{
 	    startTime = System.currentTimeMillis();
 	    String configAsString = "";
 	    try {
 		OfflineConverter cnv = new OfflineConverter(format);
 		configAsString = cnv.getConfigString(config,null,asFragment);
 	    }
 	    catch (ConverterException e) {
 		System.err.println("Conversion failed: " + e.getMessage());
 		return new String("FAILED");
 	    }
 	    
 	    if (configAsString.length()>0) {
 		FileWriter outputStream=null;
 		try {
 		    outputStream = new FileWriter(fileName);
 		    outputStream.write(configAsString,0,configAsString.length());
 		    outputStream.close();
 		}
 		catch (Exception e) {
 		    String msg =
 			"Failed to convert configuration: " + e.getMessage();
 		    System.err.println(msg);
 		    return new String("FAILED!");
 		}
 	    }
 	    return new String("Done!");
 	}
 	    
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString() +
 				      get() + " (" + elapsedTime + " ms)");
 	    }
 	    catch (Exception e) {
 		System.out.println("EXCEPTION: "+ e.getMessage());
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString() + "FAILED!");
 	    }
 	    jProgressBar.setIndeterminate(false);
 	}
     }
     
     
     /** load release templates from the database */
     private class UpdateTemplatesThread extends SwingWorker<String>
     {
 	/** member data */
 	private String releaseTag = null;
 	private long   startTime;
 	
 	/** standard constructor */
 	public UpdateTemplatesThread(String releaseTag)
 	{
 	    this.releaseTag = releaseTag;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws InterruptedException
 	{
 	    startTime = System.currentTimeMillis();
 	    if (!releaseTag.equals(currentRelease.releaseTag()))
 		database.loadSoftwareRelease(releaseTag,currentRelease);
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString() +
 				      get() + " (" + elapsedTime + " ms)");
 	    }
 	    catch (Exception e) {
 		System.out.println("EXCEPTION: "+ e.getMessage());
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString() + "FAILED!");	
 	    }
 	    jProgressBar.setIndeterminate(false);
 	}
     }
     
     
     //------------------------------------------------------------------------------
     //
     // private member functions
     //
     //------------------------------------------------------------------------------
     
     /** create trees and tables, including models */
     private void createTreesAndTables()
     {
 	// current configuration tree
 	treeModelCurrentConfig = new ConfigurationTreeModel(currentConfig);
 	jTreeCurrentConfig     = new JTree(treeModelCurrentConfig) {
 		public String getToolTipText(MouseEvent evt) {
 		    String text = null;
 		    if (getRowForLocation(evt.getX(),evt.getY()) == -1) return text;
 		    TreePath tp = getPathForLocation(evt.getX(),evt.getY());
 		    Object selectedNode = tp.getLastPathComponent();
 		    if (selectedNode instanceof Path) {
 			Path path = (Path)selectedNode;
 			if (path.streamCount()>0) {
 			    text = path.name()+" assigned to stream(s): ";
 			    for (int i=0;i<path.streamCount();i++)
 				text += path.stream(i) + " ";
 			}
 		    }
 		    else if (selectedNode instanceof ESSourceInstance||
 			     selectedNode instanceof ESModuleInstance||
 			     selectedNode instanceof ModuleInstance) {
 			Instance instance = (Instance)selectedNode;
 			text = instance.template().name();
 		    }
 		    else if (selectedNode instanceof ModuleReference) {
 			ModuleReference reference=(ModuleReference)selectedNode;
 			ModuleInstance  instance =(ModuleInstance)reference.parent();
 			text = instance.template().name();
 		    }
 		    return text;
 		}
 	    };
 	jTreeCurrentConfig.setToolTipText("");
 	jTreeCurrentConfig.setRootVisible(false);
 	jTreeCurrentConfig.setShowsRootHandles(true);
 	jTreeCurrentConfig.setEditable(true);
 	jTreeCurrentConfig.getSelectionModel()
 	    .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 	
 	
 	jTreeCurrentConfig
 	    .setCellRenderer(new ConfigurationTreeRenderer());
 	jTreeCurrentConfig
 	    .setCellEditor(new ConfigurationTreeEditor(jTreeCurrentConfig,
 						       new ConfigurationTreeRenderer()));
 	
 	ConfigurationTreeMouseListener mouseListener =
 	    new ConfigurationTreeMouseListener(jTreeCurrentConfig,frame);
 	jTreeCurrentConfig.addMouseListener(mouseListener);
 	
 	ConfigurationTreeTransferHandler currentDndHandler =
 	    new ConfigurationTreeTransferHandler(jTreeCurrentConfig,currentRelease,
 						 treeModelParameters);
 	jTreeCurrentConfig.setTransferHandler(currentDndHandler);
 	jTreeCurrentConfig.setDropTarget(new ConfigurationTreeDropTarget());
 	jTreeCurrentConfig.setDragEnabled(true);
 	
 	// import tree
 	Color defaultTreeBackground = UIManager.getColor("Tree.textBackground");
 	Color importTreeBackground  = UIManager.getColor("Button.background");
 	UIManager.put("Tree.textBackground",importTreeBackground);
 	treeModelImportConfig = new ConfigurationTreeModel(importConfig);
 	jTreeImportConfig      = new JTree(treeModelImportConfig);
         jTreeImportConfig.setBackground(importTreeBackground);
 
 	jTreeImportConfig.setRootVisible(true);
 	jTreeImportConfig.setEditable(false);
 	jTreeImportConfig.getSelectionModel()
 	    .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 	jTreeImportConfig.setCellRenderer(new ConfigurationTreeRenderer());
 	
 	
 	ConfigurationTreeTransferHandler importDndHandler =
 	    new ConfigurationTreeTransferHandler(jTreeImportConfig,null,null);
 	jTreeImportConfig.setTransferHandler(importDndHandler);
 	jTreeImportConfig.setDropTarget(new ConfigurationTreeDropTarget());
 	jTreeImportConfig.setDragEnabled(true);
 	
 	UIManager.put("Tree.textBackground",defaultTreeBackground);
 	
 	// stream tree
 	treeModelStreams = new StreamTreeModel(currentConfig);
 	jTreeStreams     = new JTree(treeModelStreams);
 	jTreeStreams.setEditable(true);
 	jTreeStreams.setRootVisible(false);
 	jTreeStreams.getSelectionModel().setSelectionMode(TreeSelectionModel
 							  .SINGLE_TREE_SELECTION);
 
 	
 	jTreeStreams.setCellRenderer(new StreamTreeRenderer());
 	jTreeStreams.setCellEditor(new StreamTreeEditor(jTreeStreams,
 							new StreamTreeRenderer()));
 	
 	StreamTreeMouseListener streamTreeMouseListener =
 	    new StreamTreeMouseListener(jTreeStreams);
 	jTreeStreams.addMouseListener(streamTreeMouseListener);
 	treeModelStreams.addTreeModelListener(streamTreeMouseListener);
 	
 
 	// prescales table
 
 	// parameter table
 	treeModelParameters  = new ParameterTreeModel();
 	jTreeTableParameters = new TreeTable(treeModelParameters);
 	jTreeTableParameters.setTreeCellRenderer(new ParameterTreeCellRenderer());
 	
 	jTreeTableParameters.getColumnModel().getColumn(0).setPreferredWidth(120);
 	jTreeTableParameters.getColumnModel().getColumn(1).setPreferredWidth(90);
 	jTreeTableParameters.getColumnModel().getColumn(2).setPreferredWidth(180);
 	jTreeTableParameters.getColumnModel().getColumn(3).setPreferredWidth(30);
 	jTreeTableParameters.getColumnModel().getColumn(4).setPreferredWidth(30);
 
 	jTreeTableParameters
 	    .addMouseListener(new ParameterTableMouseListener(frame,
 							      jTreeTableParameters));
     }
     
     /** show/hide the import-tree pane */
     private void showImportTree()
     {
 	jSplitPaneCurrentConfig.setRightComponent(jPanelImportConfig);
 	jSplitPaneCurrentConfig.setDividerLocation(0.5);
 	jSplitPaneCurrentConfig.setDividerSize(8);
     }
     private void hideImportTree()
     {
 	jSplitPaneCurrentConfig.setRightComponent(null);
 	jSplitPaneCurrentConfig.setDividerLocation(1);
 	jSplitPaneCurrentConfig.setDividerSize(1);
     }
     
     /** display parameters of the instance in right upper area */
     private void displayParameters()
     {
 	TitledBorder border = (TitledBorder)jScrollPaneParameters.getBorder();
 
 	if (currentInstance instanceof Instance) {
 	    jSplitPaneRightUpper.setDividerLocation(-1);
 	    jSplitPaneRightUpper.setDividerSize(8);
 
 	    Instance inst = (Instance)currentInstance;
 
 	    String subName = inst.template().parentPackage().subsystem().name();
 	    String pkgName = inst.template().parentPackage().name();
 	    String cvsTag  = inst.template().cvsTag();
 	    String type    = inst.template().type();
 	    String plugin  = inst.template().name();
 	    String label   = inst.name();
 	    
 	    DefaultComboBoxModel cbModel =
 		(DefaultComboBoxModel)jComboBoxPaths.getModel();
 	    cbModel.removeAllElements();
 
 	    if (inst instanceof ModuleInstance) {
 		ModuleInstance module = (ModuleInstance)inst;
 		jComboBoxPaths.setEnabled(true);
 		Path[] paths = module.parentPaths();
 		for (Path p : paths) cbModel.addElement(p.name());
 	    }
 	    else {
 		jComboBoxPaths.setEnabled(false);
 	    }
 	    
 	    jTextFieldPackage.setText(subName+"/"+pkgName);
 	    jTextFieldCVS.setText(cvsTag);
 	    jLabelPlugin.setText(type + ":");
 	    jTextFieldPlugin.setText(plugin);
 	    jTextFieldLabel.setText(label);
 	    
 	    currentParameters.clear();
 	    Iterator<Parameter> itP = inst.parameterIterator();
 	    while (itP.hasNext()) currentParameters.add(itP.next());
 	    treeModelParameters.setParameters(currentParameters);
 	    border.setTitle(inst.name() + " Parameters");
 	}
 	else {
 	    clearParameters();
 	    currentParameters.clear();
 	    Iterator<PSetParameter> itPSet = currentConfig.psetIterator();
 	    while (itPSet.hasNext()) currentParameters.add(itPSet.next());
 	    treeModelParameters.setParameters(currentParameters);
 	    border.setTitle("Global PSets");
 	}
     }
 
     /** clear the right upper area */
     private void clearParameters()
     {
 	jSplitPaneRightUpper.setDividerLocation(0);
 	jSplitPaneRightUpper.setDividerSize(1);
 
 	jTextFieldPackage.setText("");
 	jTextFieldCVS.setText("");
 	jLabelPlugin.setText("Plugin:");
 	jTextFieldPlugin.setText("");
 	jTextFieldLabel.setText("");
 
 	((DefaultComboBoxModel)jComboBoxPaths.getModel()).removeAllElements();
 	jComboBoxPaths.setEnabled(false);
 
	currentInstance = null;
 	currentParameters.clear();
 	treeModelParameters.setParameters(currentParameters);
 
 	((TitledBorder)jScrollPaneParameters.getBorder()).setTitle("Parameters");
     }
     
     /** display the configuration snippet for the currently selected component */
     private void displaySnippet()
     {
 	if (currentInstance==treeModelCurrentConfig.psetsNode()) {
 	    String s="";
 	    Iterator<PSetParameter> itPSet = currentConfig.psetIterator();
 	    while (itPSet.hasNext())
 		s+= cnvEngine.getParameterWriter().toString(itPSet.next(),
 							    cnvEngine,"");
 	    jEditorPaneSnippet.setText(s);
 	}
 	else if (currentInstance instanceof EDSourceInstance) {
 	    EDSourceInstance edsource = (EDSourceInstance)currentInstance;
 	    jEditorPaneSnippet.setText(cnvEngine.getEDSourceWriter().
 				       toString(edsource,cnvEngine,"  "));
 	}
 	else if (currentInstance instanceof ESSourceInstance) {
 	    ESSourceInstance essource = (ESSourceInstance)currentInstance;
 	    jEditorPaneSnippet.setText(cnvEngine.getESSourceWriter().
 				       toString(essource,cnvEngine,"  "));
 	}
 	else if (currentInstance instanceof ESModuleInstance) {
 	    ESModuleInstance esmodule = (ESModuleInstance)currentInstance;
 	    jEditorPaneSnippet.setText(cnvEngine.getESModuleWriter().
 				       toString(esmodule,cnvEngine,"  "));
 	}
 	else if (currentInstance instanceof ServiceInstance) {
 	    ServiceInstance service = (ServiceInstance)currentInstance;
 	    jEditorPaneSnippet.setText(cnvEngine.getServiceWriter().
 				       toString(service,cnvEngine,"  "));
 	}
 	else if (currentInstance instanceof ModuleInstance) {
 	    ModuleInstance module = (ModuleInstance)currentInstance;
 	    jEditorPaneSnippet.setText(cnvEngine.getModuleWriter().
 				       toString(module));
 	}
 	else if (currentInstance instanceof Path) {
 	    Path path = (Path)currentInstance;
 	    jEditorPaneSnippet.setText(cnvEngine.getPathWriter().
 				       toString(path,cnvEngine,"  "));
 	}
 	else if (currentInstance instanceof Sequence) {
 	    Sequence sequence = (Sequence)currentInstance;
 	    jEditorPaneSnippet.setText(cnvEngine.getSequenceWriter().
 				       toString(sequence,cnvEngine,"  "));
 	}
 	else {
 	    clearSnippet();
 	}
 	jEditorPaneSnippet.setCaretPosition(0);
     }
 
     /** clear snippet pane (right-lower) */
     private void clearSnippet()
     {
 	jEditorPaneSnippet.setText("");
     }
 
     /** update the default-stream combobox */
     private void updateDefaultStreamComboBox()
     {
 	jComboBoxDefaultStream.setEnabled(true);
 	DefaultComboBoxModel comboBoxModel =
 	    (DefaultComboBoxModel)jComboBoxDefaultStream.getModel();
 	comboBoxModel.removeAllElements();
 	comboBoxModel.addElement(new String());
 	Iterator<Stream> it = currentConfig.streamIterator();
 	while (it.hasNext()) comboBoxModel.addElement(it.next());
 	if (currentConfig.defaultStream()==null)
 	    jComboBoxDefaultStream.setSelectedIndex(0);
 	else {
 	    Stream defaultStream = currentConfig.defaultStream();
 	    int    index = currentConfig.indexOfStream(defaultStream);
 	    jComboBoxDefaultStream.setSelectedIndex(index+1);
 	}
     }
     
     //
     // ACTIONLISTENER CALLBACKS
     //
 
     private void jButtonProcessActionPerformed(ActionEvent e)
     {
 	String processName = jTextFieldProcess.getText();
 	if (processName.length()>0||processName.indexOf('_')>=0)
 	    jTextFieldProcess.setText(currentConfig.processName());
 	else
 	    currentConfig.setHasChanged(true);
     }
     private void jButtonReleaseActionPerformed(ActionEvent e)
     {
 	if (currentConfig.isEmpty()) return;
 	SoftwareReleaseDialog dialog = new SoftwareReleaseDialog(frame,
 								 currentRelease);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
     }
     private void jButtonCancelSearchActionPerformed(ActionEvent e)
     {
 	TreePath tp = jTreeCurrentConfig.getSelectionPath();
 	jTextFieldSearch.setText("");
 	setCurrentConfig(currentConfig);
 	if (tp!=null) {
 	    Object   obj  = tp.getLastPathComponent();
 	    Object[] objs = tp.getPath();
 	    objs[0]=currentConfig;
 	    tp = new TreePath(objs);
 	    jTreeCurrentConfig.scrollPathToVisible(tp);
 	    jTreeCurrentConfig.setSelectionPath(tp);
 	}
     }
     private void jToggleButtonImportActionPerformed(ActionEvent e)
     {
 	AbstractButton b = (AbstractButton)e.getSource();
 	if (b.isSelected()) showImportTree();
 	else hideImportTree();
     }
     private void jButtonImportCancelSearchActionPerformed(ActionEvent e)
     {
 	TreePath tp = jTreeImportConfig.getSelectionPath();
 	Object   obj = tp.getLastPathComponent();
 	jTextFieldImportSearch.setText("");
 	treeModelImportConfig.setConfiguration(importConfig);
 	if (tp!=null) {
 	    Object[] objs = tp.getPath();
 	    objs[0]=importConfig;
 	    tp = new TreePath(objs);
 	    jTreeImportConfig.scrollPathToVisible(tp);
 	    jTreeImportConfig.setSelectionPath(tp);
 	}
     }
     private void jComboBoxDefaultStreamActionPerformed(ActionEvent e)
     {
 	Object selectedItem = jComboBoxDefaultStream.getSelectedItem();
 	if (selectedItem instanceof Stream) {
 	    Stream stream = (Stream)selectedItem;
 	    currentConfig.setDefaultStream(stream);
 	}
 	else {
 	    currentConfig.setDefaultStream(null);
 	}
     }
     private void jButtonAddStreamActionPerformed(ActionEvent e)
     {
 	StreamTreeActions.insertStream(jTreeStreams);
     }
     private void jButtonDeleteStreamActionPerformed(ActionEvent e)
     {
 	StreamTreeActions.removeStream(jTreeStreams);
     }
     private void jSplitPaneRightComponentMoved(ComponentEvent e)
     {
 	if (!(currentInstance instanceof Instance)) {
 	    jSplitPaneRightUpper.setDividerLocation(0);
 	    jSplitPaneRightUpper.setDividerSize(1);
 	}
     }
     
 
     //
     // DOCUMENTLISTENER CALLBACKS
     //
     private void jTextFieldSearchInsertUpdate(DocumentEvent e)
     {
 	try {
 	    String search = e.getDocument().getText(0,e.getDocument().getLength());
 	    jTreeCurrentConfigUpdateSearch(search);
 	}
 	catch (Exception ex) {}
     }
     private void jTextFieldSearchRemoveUpdate(DocumentEvent e)
     {
 	try {
 	    String search = e.getDocument().getText(0,e.getDocument().getLength());
 	    jTreeCurrentConfigUpdateSearch(search);
 	}
 	catch (Exception ex) {}
     }
     private void jTreeCurrentConfigUpdateSearch(String search)
     {
 	if (search.length()>0) {
 	    String mode = 
 		buttonGroupSearch1.getSelection().getActionCommand()+":"+
 		buttonGroupSearch2.getSelection().getActionCommand();
 	    jButtonCancelSearch.setEnabled(true);
 	    ModifierInstructions modifications = new ModifierInstructions();
 	    modifications.interpretSearchString(search,mode,currentConfig);
 	    ConfigurationModifier modifier = 
 		new ConfigurationModifier(currentConfig);
 	    modifier.modify(modifications);
 	    treeModelCurrentConfig.setConfiguration(modifier);
 	    jTreeConfigExpandLevel1Nodes(jTreeCurrentConfig);
 	}
 	else {
 	    setCurrentConfig(currentConfig);
 	    jButtonCancelSearch.setEnabled(false);
 	}
     }
     private void jTreeConfigExpandLevel1Nodes(JTree t)
     {
 	ConfigurationTreeModel m = (ConfigurationTreeModel)t.getModel();
 	
 	TreePath tpPSets = new TreePath(m.getPathToRoot(m.psetsNode()));
 	t.expandPath(tpPSets);
 	TreePath tpEDSources = new TreePath(m.getPathToRoot(m.edsourcesNode()));
 	t.expandPath(tpEDSources);
 	TreePath tpESSources = new TreePath(m.getPathToRoot(m.essourcesNode()));
 	t.expandPath(tpESSources);
 	TreePath tpESModules = new TreePath(m.getPathToRoot(m.esmodulesNode()));
 	t.expandPath(tpESModules);
 	TreePath tpServices = new TreePath(m.getPathToRoot(m.servicesNode()));
 	t.expandPath(tpESSources);
 	TreePath tpPaths = new TreePath(m.getPathToRoot(m.pathsNode()));
 	t.expandPath(tpPaths);
 	TreePath tpSequences = new TreePath(m.getPathToRoot(m.sequencesNode()));
 	t.expandPath(tpSequences);
 	TreePath tpModules = new TreePath(m.getPathToRoot(m.modulesNode()));
 	t.expandPath(tpModules);
     }
 
     private void jTextFieldImportSearchInsertUpdate(DocumentEvent e)
     {
 	try {
 	    String search = e.getDocument().getText(0,e.getDocument().getLength());
 	    jTreeImportConfigUpdateSearch(search);
 	}
 	catch (Exception ex) {}
     }
     private void jTextFieldImportSearchRemoveUpdate(DocumentEvent e)
     {
 	try {
 	    String search = e.getDocument().getText(0,e.getDocument().getLength());
 	    jTreeImportConfigUpdateSearch(search);
 	}
 	catch (Exception ex) {}
     }
     private void jTreeImportConfigUpdateSearch(String search)
     {
 	if (search.length()>0) {
 	    String mode = 
 		buttonGroupImportSearch1.getSelection().getActionCommand()+":"+
 		buttonGroupImportSearch2.getSelection().getActionCommand();
 	    jButtonImportCancelSearch.setEnabled(true);
 	    ModifierInstructions modifications = new ModifierInstructions();
 	    modifications.interpretSearchString(search,mode,importConfig);
 	    ConfigurationModifier modifier = 
 		new ConfigurationModifier(importConfig);
 	    modifier.modify(modifications);
 	    treeModelImportConfig.setConfiguration(modifier);
 	    jTreeConfigExpandLevel1Nodes(jTreeImportConfig);
 	}
 	else {
 	    treeModelImportConfig.setConfiguration(importConfig);
 	    jButtonImportCancelSearch.setEnabled(false);
 	}
     }
     
     //
     // TREEMODELLISTENER CALLBACKS
     //
     
     private void jTreeCurrentConfigTreeNodesChanged(TreeModelEvent e)
     {
 	if (currentConfig==null) return;
 
 	if (currentConfig.streamCount()>0) {
 	    Object changedNode = e.getChildren()[0];
 	    if (changedNode instanceof Path) {
 		Path path = (Path)changedNode;
 		if (path.streamCount()>0) treeModelStreams.nodeChanged(path); // :(
 	    }
 	}
 	displayParameters();
 	displaySnippet();
     }
     private void jTreeCurrentConfigTreeNodesInserted(TreeModelEvent e)
     {
 	if (currentConfig.streamCount()>0&&currentConfig.defaultStream()!=null) {
 	    TreePath treePath = e.getTreePath();
 	    Object parentNode = treePath.getLastPathComponent();
 	    if (parentNode==treeModelCurrentConfig.pathsNode())
 		treeModelStreams.nodeInserted(currentConfig.defaultStream(),
 					      currentConfig.defaultStream()
 					      .pathCount()-1);
 	}
     }
     private void jTreeCurrentConfigTreeNodesRemoved(TreeModelEvent e)
     {
 	if (currentConfig.streamCount()>0) {
 	    Object removedNode = e.getChildren()[0];
 	    if (removedNode instanceof Path) {
 		Path             path = (Path)removedNode;
 		Iterator<Stream> it   = path.streamIterator();
 		while (it.hasNext()) {
 		    Stream stream = it.next();
 		    int    index  = stream.indexOfPath(path);
 		    treeModelStreams.nodeRemoved(stream,index,path);
 		    stream.removePath(path);
 		}
 	    }
 	}
     }
     private void jTreeCurrentConfigTreeStructureChanged(TreeModelEvent e) {}
 
 
     private void jTreeStreamsTreeNodesChanged(TreeModelEvent e)
     {
 	Object changedNode = e.getChildren()[0];
 	if (changedNode instanceof Stream) {
 	    updateDefaultStreamComboBox();
 	    currentConfig.setHasChanged(true);
 	}
     }
     private void jTreeStreamsTreeNodesInserted(TreeModelEvent e)
     {
 	TreePath treePath   = e.getTreePath();
 	Object   parentNode = treePath.getLastPathComponent();
 	
 	if (parentNode == treeModelStreams.getRoot()) updateDefaultStreamComboBox();
 	
 	jTextFieldUnassignedPaths.setEnabled(true);
 	jTextFieldUnassignedPaths
 	    .setText(""+currentConfig.pathNotAssignedToStreamCount());
 	if (currentConfig.pathNotAssignedToStreamCount()>0)
 	    jTextFieldUnassignedPaths.setForeground(Color.RED);
 	else
 	    jTextFieldUnassignedPaths.setForeground(Color.GREEN);
 	
 	currentConfig.setHasChanged(true);
     }
     private void jTreeStreamsTreeNodesRemoved(TreeModelEvent e)
     {
 	if (currentConfig.streamCount()==0) {
 	    jTextFieldUnassignedPaths.setEnabled(false);
 	    jComboBoxDefaultStream.setEnabled(false);
 	    jTextFieldUnassignedPaths.setText("0");
 	    jComboBoxDefaultStream.setSelectedIndex(0);
 	}
 	else {
 	    Object removedNode = e.getChildren()[0];
 	    if (removedNode instanceof Stream) updateDefaultStreamComboBox();
 	    
 	    jTextFieldUnassignedPaths
 		.setText(""+currentConfig.pathNotAssignedToStreamCount());
 	    if (currentConfig.pathNotAssignedToStreamCount()>0)
 		jTextFieldUnassignedPaths.setForeground(Color.RED);
 	    else
 		jTextFieldUnassignedPaths.setForeground(Color.GREEN);
 	}
 	currentConfig.setHasChanged(true);
     }
     private void jTreeStreamsTreeStructureChanged(TreeModelEvent e) {}
     
 
 
     //
     // TREESELECTIONLISTENER CALLBACKS
     //
     
     private void jTreeCurrentConfigValueChanged(TreeSelectionEvent e)
     {
 	TreePath treePath=e.getNewLeadSelectionPath();
 	if (treePath==null) {
 	    clearParameters();
 	    clearSnippet();
 	    return;
 	}
 
 	Object node=treePath.getLastPathComponent();
 	if(node==null) {
 	    clearParameters();
 	    clearSnippet();
 	    return;
 	}
 
 	while (node instanceof Parameter) {
 	    Parameter p = (Parameter)node;
 	    node = p.parent();
 	}
 	
 	if (node instanceof Reference) {
 	    node = ((Reference)node).parent();
 	}
 	
 	if (node instanceof Instance) {
 	    currentInstance = node;
 	    displayParameters();
 	    displaySnippet();
 	}
 	else if (node==null||node==treeModelCurrentConfig.psetsNode()) {
 	    currentInstance = treeModelCurrentConfig.psetsNode();
 	    displayParameters();
 	    displaySnippet();
 	}
 	else if (node instanceof ReferenceContainer) {
 	    currentInstance = node;
 	    clearParameters();
 	    displaySnippet();
 	}
 	else {
 	    clearParameters();
 	    clearSnippet();
 	}
     }
 
     private void jTreeStreamsValueChanged(TreeSelectionEvent e)
     {
 	TreePath treePath = e.getNewLeadSelectionPath(); if (treePath==null) return;
 	Object   selectedNode = treePath.getLastPathComponent();
 	if (selectedNode instanceof Stream) {
 	    jButtonAddStream.setEnabled(true);
 	    jButtonDeleteStream.setEnabled(true);
 	}
 	else if (selectedNode instanceof Path) {
 	    jButtonAddStream.setEnabled(false);
 	    jButtonDeleteStream.setEnabled(false);
 	}
 	else {
 	    jButtonAddStream.setEnabled(true);
 	    jButtonDeleteStream.setEnabled(false);
 	}
     }
     
     
     //
     // TABLEMODELLISTENER CALLBACKS
     //
     
     private void jTreeTableParametersTableChanged(TableModelEvent e)
     {
 	Object source = e.getSource();
 	if (source instanceof TreeTableTableModel) {
 	    TreeTableTableModel tableModel = (TreeTableTableModel)source;
 
 	    Parameter node         = (Parameter)tableModel.changedNode();
 	    Object    childNode    = tableModel.childNode();
 	    int       childIndex   = tableModel.childIndex();
 	    String    typeOfChange = tableModel.typeOfChange();
 	    
 	    if (node!=null) {
 		Object parent = node.parent();
 		while (parent instanceof Parameter) {
 		    Parameter p = (Parameter)parent;
 		    parent = p.parent();
 		}
 		
 		if (childNode==null)
 		    treeModelCurrentConfig.nodeChanged(node);
 		else if (typeOfChange.equals("REMOVE"))
 		    treeModelCurrentConfig.nodeRemoved(node,childIndex,childNode);
 		else if (typeOfChange.equals("INSERT"))
 		    treeModelCurrentConfig.nodeInserted(node,childIndex);
 		
 		if (parent instanceof ModuleInstance) jTreeCurrentConfig.updateUI();
 		treeModelCurrentConfig.updateLevel1Nodes();
 		currentConfig.setHasChanged(true);
 	    }
 	}
     }
     
     
     //
     // CREATE GUI COMPONENTS
     //
     
     /** create the  menubar */
     private void createMenuBar()
     {
 	ArrayList<String> admins = new ArrayList<String>();
 	admins.add("schiefer");
 	admins.add("meschi");
 	admins.add("mzanetti");
 	
 	menuBar = new MenuBar(jMenuBar,this,admins.contains(userName));
 	frame.setJMenuBar(jMenuBar);
     }
 
     /** create the toolbar */
     private void createToolBar()
     {
 	jToolBar.setFloatable(false);
 	jToolBar.setRollover(true);
 	toolBar = new ToolBar(jToolBar,this);
     }
 
     /** create the database connection panel */
     private void createDbConnectionPanel()
     {
 	jPanelDbConnection = new DatabaseInfoPanel();
     }
 
     /** create the left panel */
     private void createLeftPanel()
     {
 	createConfigurationPanel(); // -> tab 1
 	createStreamsPanel();       // -> tab 2
 	createPrescalesPanel();     // -> tab 3
 
         JLabel jLabelConfig  = new javax.swing.JLabel();
 	JLabel jLabelProcess = new javax.swing.JLabel();
         JLabel jLabelRelease = new javax.swing.JLabel();
         JLabel jLabelCreated = new javax.swing.JLabel();
         JLabel jLabelCreator = new javax.swing.JLabel();
 	
         jLabelConfig.setText("Configuration:");
 
         jTextFieldCurrentConfig.setBackground(new java.awt.Color(255, 255, 255));
         jTextFieldCurrentConfig.setEditable(false);
         jTextFieldCurrentConfig.setFont(new java.awt.Font("Dialog", 1, 12));
         jTextFieldCurrentConfig.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         jLabelProcess.setText("Process:");
 
         jTextFieldProcess.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         jLabelRelease.setText("Release:");
 
         jButtonRelease.setBackground(new java.awt.Color(255, 255, 255));
         jButtonRelease.setForeground(new java.awt.Color(0, 0, 204));
         //jButtonRelease.setText("-");
         jButtonRelease.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         jLabelCreated.setText("Created:");
 
         jTextFieldCreated.setBackground(new java.awt.Color(255, 255, 255));
         jTextFieldCreated.setEditable(false);
         jTextFieldCreated.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         jLabelCreator.setText("Creator:");
 
         jTextFieldCreator.setBackground(new java.awt.Color(255, 255, 255));
         jTextFieldCreator.setEditable(false);
         jTextFieldCreator.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
 	jTabbedPaneLeft.addTab("Configuration", jPanelCurrentConfig);
         jTabbedPaneLeft.addTab("Streams",       jPanelStreams);
         jTabbedPaneLeft.addTab("Prescales",     jPanelPrescales);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanelLeft);
         jPanelLeft.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				  .add(layout.createSequentialGroup()
 				       .addContainerGap()
 				       .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 					    .add(jTabbedPaneLeft, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
 					    .add(layout.createSequentialGroup()
 						 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 						      .add(jLabelConfig)
 						      .add(jLabelProcess)
 						      .add(jLabelCreated))
 						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 						 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 						      .add(layout.createSequentialGroup()
 							   .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
 								.add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldCreated, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
 								.add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldProcess, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE))
 							   .add(22, 22, 22)
 							   .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 								.add(layout.createSequentialGroup()
 								     .add(jLabelRelease)
 								     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 								     .add(jButtonRelease, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE))
 								.add(layout.createSequentialGroup()
 								     .add(jLabelCreator)
 								     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 								     .add(jTextFieldCreator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE))))
 						      .add(layout.createSequentialGroup()
 							   .add(jTextFieldCurrentConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
 							   .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 							   .add(jLabelLock, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
 						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
 				       .addContainerGap())
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				.add(layout.createSequentialGroup()
 				     .addContainerGap()
 				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 					  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 					       .add(jLabelConfig)
 					       .add(jTextFieldCurrentConfig, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 					  .add(jLabelLock, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 					  .add(jLabelProcess)
 					  .add(jLabelRelease)
 					  .add(jTextFieldProcess, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 					  .add(jButtonRelease, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 					  .add(jLabelCreated)
 					  .add(jTextFieldCreated, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 					  .add(jLabelCreator)
 					  .add(jTextFieldCreator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				     .add(jTabbedPaneLeft, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
 				     .addContainerGap())
 				);
 
         layout.linkSize(new java.awt.Component[] {jButtonRelease, jLabelRelease, jTextFieldProcess}, org.jdesktop.layout.GroupLayout.VERTICAL);
         layout.linkSize(new java.awt.Component[] {jLabelLock, jTextFieldCurrentConfig}, org.jdesktop.layout.GroupLayout.VERTICAL);
     }
     
     /** create the Import Configuration part of the configuration panel */
     private void createImportConfigPanel()
     {
 	createImportSearchPopupMenu();
 	jButtonImportCancelSearch.setIcon(new ImageIcon(getClass().
 							getResource("/CancelSearchIcon.png")));
 
         jLabelImportSearch.setText("Search:");
 
         jButtonImportCancelSearch.setBorder(null);
 
         jScrollPaneImportConfig.setViewportView(jTreeImportConfig);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanelImportConfig);
         jPanelImportConfig.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				  .add(layout.createSequentialGroup()
 				       .add(jLabelImportSearch)
 				       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				       .add(jTextFieldImportSearch, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
 				       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				       .add(jButtonImportCancelSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 				  .add(jScrollPaneImportConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				.add(layout.createSequentialGroup()
 				     .addContainerGap()
 				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
 					  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 					       .add(jLabelImportSearch)
 					       .add(jTextFieldImportSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 					  .add(jButtonImportCancelSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				     .add(jScrollPaneImportConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE))
 				);
 
 	layout.linkSize(new java.awt.Component[] {jButtonImportCancelSearch, jTextFieldImportSearch}, org.jdesktop.layout.GroupLayout.VERTICAL);
     }
     
     /** create the 'Configuration' panel (tab1 in left panel) */
     private void createConfigurationPanel()
     {
 	createImportConfigPanel();
 	createSearchPopupMenu();
 	jButtonCancelSearch.
 	    setIcon(new ImageIcon(getClass().
 				  getResource("/CancelSearchIcon.png")));
 	jToggleButtonImport.
 	    setIcon(new ImageIcon(getClass().
 				  getResource("/ImportToggleIcon.png")));
 
 	jButtonCancelSearch.setEnabled(false);
 	jToggleButtonImport.setEnabled(false);
 	
 	jLabelSearch.setText("Search:");
 
         jSplitPaneCurrentConfig.setResizeWeight(0.5);
         jScrollPaneCurrentConfig.setViewportView(jTreeCurrentConfig);
 	
         jSplitPaneCurrentConfig.setLeftComponent(jScrollPaneCurrentConfig);
 	
 	jSplitPaneCurrentConfig.setRightComponent(jPanelImportConfig);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanelCurrentConfig);
         jPanelCurrentConfig.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				  .add(layout.createSequentialGroup()
 				       .addContainerGap()
 				       .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 					    .add(jSplitPaneCurrentConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
 					    .add(layout.createSequentialGroup()
 						 .add(jLabelSearch)
 						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 						 .add(jTextFieldSearch, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
 						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 						 .add(jButtonCancelSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 						 .add(63, 63, 63)
 						 .add(jToggleButtonImport, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
 				       .addContainerGap())
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				.add(layout.createSequentialGroup()
 				     .addContainerGap()
 				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 					  .add(jToggleButtonImport, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 					  .add(jButtonCancelSearch)
 					  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 					       .add(jLabelSearch)
 					       .add(jTextFieldSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
 				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				     .add(jSplitPaneCurrentConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
 				     .addContainerGap())
 				);
 
         layout.linkSize(new java.awt.Component[] {jButtonCancelSearch, jTextFieldSearch, jToggleButtonImport}, org.jdesktop.layout.GroupLayout.VERTICAL);
 
     }
 
     /** create the  'Streams' panel (tab2 in left panel) */
     private void createStreamsPanel()
     {
         JLabel jLabelDefaultStream   = new javax.swing.JLabel();
         JLabel jLabelUnassignedPaths = new javax.swing.JLabel();
 	
 	jButtonAddStream.setIcon(new ImageIcon(getClass().
 					       getResource("/AddIcon.png")));
         jButtonDeleteStream.setIcon(new ImageIcon(getClass().
 						  getResource("/DeleteIcon.png")));
 	
 	jButtonAddStream.setToolTipText("create a new stream");
 	jButtonDeleteStream.setToolTipText("delete the currently selected stream");
 	
         jScrollPaneStreams.setViewportView(jTreeStreams);
 	
         jLabelDefaultStream.setText("Default Stream:");
 	jLabelDefaultStream.setToolTipText("new paths will be automatically " +
 					   "added to this stream");
 	
         jComboBoxDefaultStream.setModel(new DefaultComboBoxModel());
 	
         jLabelUnassignedPaths.setText("Unassigned Paths:");
 	
 	jTextFieldUnassignedPaths.setHorizontalAlignment(JTextField.RIGHT);
 	jTextFieldUnassignedPaths.setEditable(false);
 	jTextFieldUnassignedPaths.setBackground(new java.awt.Color(255,255,255));
 	
         jButtonAddStream.setBackground(new java.awt.Color(238,238,238));
         jButtonAddStream.setBorder(null);
         jButtonAddStream.setEnabled(false);
 	
         jButtonDeleteStream.setBackground(new java.awt.Color(238,238,238));
         jButtonDeleteStream.setBorder(null);
         jButtonDeleteStream.setEnabled(false);
 	
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanelStreams);
         jPanelStreams.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPaneStreams, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                     .add(layout.createSequentialGroup()
                         .add(jLabelDefaultStream)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jComboBoxDefaultStream, 0, 232, Short.MAX_VALUE))
                     .add(layout.createSequentialGroup()
                         .add(jButtonAddStream, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jButtonDeleteStream, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 110, Short.MAX_VALUE)
                         .add(jLabelUnassignedPaths)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jTextFieldUnassignedPaths, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
 
         layout.linkSize(new java.awt.Component[] {jButtonAddStream, jButtonDeleteStream}, org.jdesktop.layout.GroupLayout.HORIZONTAL);
 
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabelDefaultStream)
                     .add(jComboBoxDefaultStream, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jScrollPaneStreams, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 			 .add(jLabelUnassignedPaths)
 			 .add(jButtonAddStream)
 			 .add(jTextFieldUnassignedPaths, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 		     .add(jButtonDeleteStream))
 		 .addContainerGap())
 	    );
 	
         layout.linkSize(new java.awt.Component[] {jButtonAddStream, jButtonDeleteStream, jTextFieldUnassignedPaths}, org.jdesktop.layout.GroupLayout.VERTICAL);
 	
     }
 
     /** create the 'Search:' popup menu */
     private void createSearchPopupMenu()
     {
 	buttonGroupSearch1 = new ButtonGroup();
 	buttonGroupSearch2 = new ButtonGroup();
 	
 	JRadioButtonMenuItem rbMenuItem;
 	
 	rbMenuItem = new JRadioButtonMenuItem("startsWith");
 	rbMenuItem.setActionCommand("startsWith");
 	rbMenuItem.setSelected(true);
 	buttonGroupSearch1.add(rbMenuItem);
 	jPopupMenuSearch.add(rbMenuItem);
 	rbMenuItem = new JRadioButtonMenuItem("contains");
 	rbMenuItem.setActionCommand("contains");
 	buttonGroupSearch1.add(rbMenuItem);
 	jPopupMenuSearch.add(rbMenuItem);
 	jPopupMenuSearch.addSeparator();
 	rbMenuItem = new JRadioButtonMenuItem("labels");
 	rbMenuItem.setActionCommand("matchLabels");
 	rbMenuItem.setSelected(true);
 	buttonGroupSearch2.add(rbMenuItem);
 	jPopupMenuSearch.add(rbMenuItem);
 	rbMenuItem = new JRadioButtonMenuItem("plugins");
 	rbMenuItem.setActionCommand("matchPlugins");
 	buttonGroupSearch2.add(rbMenuItem);
 	jPopupMenuSearch.add(rbMenuItem);
     }
 
     /** create the 'Search:' popup menu for the importConfig panel */
     private void createImportSearchPopupMenu()
     {
 	buttonGroupImportSearch1 = new ButtonGroup();
 	buttonGroupImportSearch2 = new ButtonGroup();
 	
 	JRadioButtonMenuItem rbMenuItem;
 	
 	rbMenuItem = new JRadioButtonMenuItem("startsWith");
 	rbMenuItem.setActionCommand("startsWith");
 	rbMenuItem.setSelected(true);
 	buttonGroupImportSearch1.add(rbMenuItem);
 	jPopupMenuImportSearch.add(rbMenuItem);
 	rbMenuItem = new JRadioButtonMenuItem("contains");
 	rbMenuItem.setActionCommand("contains");
 	buttonGroupImportSearch1.add(rbMenuItem);
 	jPopupMenuImportSearch.add(rbMenuItem);
 	jPopupMenuImportSearch.addSeparator();
 	rbMenuItem = new JRadioButtonMenuItem("labels");
 	rbMenuItem.setActionCommand("matchLabels");
 	rbMenuItem.setSelected(true);
 	buttonGroupImportSearch2.add(rbMenuItem);
 	jPopupMenuImportSearch.add(rbMenuItem);
 	rbMenuItem = new JRadioButtonMenuItem("plugins");
 	rbMenuItem.setActionCommand("matchPlugins");
 	buttonGroupImportSearch2.add(rbMenuItem);
 	jPopupMenuImportSearch.add(rbMenuItem);
     }
 
     /** create the 'Prescales' panel (tab3 in left panel) */
     private void createPrescalesPanel()
     {
         jScrollPanePrescales.setBackground(new java.awt.Color(255, 255, 255));
 	
         jScrollPanePrescales.setViewportView(jTablePrescales);
 	
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanelPrescales);
         jPanelPrescales.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				  .add(layout.createSequentialGroup()
 				       .addContainerGap()
 				       .add(jScrollPanePrescales, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
 				       .addContainerGap())
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				.add(layout.createSequentialGroup()
 				     .addContainerGap()
 				     .add(jScrollPanePrescales, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
 				     .addContainerGap())
 				);
     }
 
     /** create the right upper panel */
     private void createRightUpperPanel()
     {
         JLabel jLabelPackage = new javax.swing.JLabel();
         JLabel jLabelCVS     = new javax.swing.JLabel();
 
         JLabel jLabelLabel   = new javax.swing.JLabel();
         JLabel jLabelPaths   = new javax.swing.JLabel();
 	
         jSplitPaneRightUpper.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
         jLabelPackage.setFont(new java.awt.Font("Dialog", 0, 12));
         jLabelPackage.setText("Package:");
 
         jTextFieldPackage.setBackground(new java.awt.Color(250, 250, 250));
         jTextFieldPackage.setEditable(false);
         jTextFieldPackage.setFont(new java.awt.Font("Dialog", 0, 10));
         jTextFieldPackage.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         jLabelCVS.setFont(new java.awt.Font("Dialog", 0, 12));
         jLabelCVS.setText("CVS:");
 
         jTextFieldCVS.setBackground(new java.awt.Color(250, 250, 250));
         jTextFieldCVS.setEditable(false);
         jTextFieldCVS.setFont(new java.awt.Font("Dialog", 0, 10));
         jTextFieldCVS.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         jLabelPlugin.setFont(new java.awt.Font("Dialog", 0, 12));
         jLabelPlugin.setText("Plugin:");
 
         jTextFieldPlugin.setBackground(new java.awt.Color(250, 250, 250));
         jTextFieldPlugin.setEditable(false);
         jTextFieldPlugin.setFont(new java.awt.Font("Dialog", 0, 10));
         jTextFieldPlugin.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         jLabelLabel.setText("Label:");
 
         jTextFieldLabel.setBackground(new java.awt.Color(255, 255, 255));
         jTextFieldLabel.setEditable(false);
         jTextFieldLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         jLabelPaths.setText("Paths:");
 
 	jComboBoxPaths.setModel(new DefaultComboBoxModel());
         jComboBoxPaths.setBackground(new java.awt.Color(255, 255, 255));
 	
         org.jdesktop.layout.GroupLayout jPanelPluginLayout = new org.jdesktop.layout.GroupLayout(jPanelPlugin);
         jPanelPlugin.setLayout(jPanelPluginLayout);
         jPanelPluginLayout.setHorizontalGroup(
 					      jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 					      .add(jPanelPluginLayout.createSequentialGroup()
 						   .addContainerGap()
 						   .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 							.add(jPanelPluginLayout.createSequentialGroup()
 							     .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
 								  .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
 								  .add(jPanelPluginLayout.createSequentialGroup()
 								       .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 									    .add(jLabelPackage)
 									    .add(jTextFieldPackage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))
 								       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 								       .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 									    .add(jLabelCVS)
 									    .add(jTextFieldCVS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
 							     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
 							.add(jPanelPluginLayout.createSequentialGroup()
 							     .add(jLabelLabel)
 							     .add(219, 219, 219)))
 						   .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 							.add(jComboBoxPaths, 0, 131, Short.MAX_VALUE)
 							.add(jLabelPaths)
 							.add(jTextFieldPlugin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
 							.add(jLabelPlugin))
 						   .addContainerGap())
 					      );
         jPanelPluginLayout.setVerticalGroup(
 					    jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 					    .add(jPanelPluginLayout.createSequentialGroup()
 						 .addContainerGap()
 						 .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 						      .add(jLabelPackage)
 						      .add(jLabelCVS)
 						      .add(jLabelPlugin))
 						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 						 .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 						      .add(jTextFieldPackage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 						      .add(jTextFieldCVS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 						      .add(jTextFieldPlugin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 						 .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 						      .add(jLabelLabel)
 						      .add(jLabelPaths))
 						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 						 .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 						      .add(jTextFieldLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 						      .add(jComboBoxPaths, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 						 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 					    );
         jSplitPaneRightUpper.setTopComponent(jPanelPlugin);
 	
 	jScrollPaneParameters.setBackground(new java.awt.Color(255, 255, 255));
         jScrollPaneParameters.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameters"));
 	
         jScrollPaneParameters.setViewportView(jTreeTableParameters);
 	
         jSplitPaneRightUpper.setRightComponent(jScrollPaneParameters);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanelRightUpper);
         jPanelRightUpper.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				  .add(jSplitPaneRightUpper, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				.add(jSplitPaneRightUpper, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
 				);
 	
 	jTreeTableParameters.getParent().setBackground(new Color(255,255,255));//PS
     }
     
     /** create the right lower panel */
     private void createRightLowerPanel()
     {
 	jEditorPaneSnippet.setEditable(false);
         jScrollPaneRightLower.setViewportView(jEditorPaneSnippet);
 	
         jTabbedPaneRightLower.addTab("Snippet", jScrollPaneRightLower);
 	
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanelRightLower);
         jPanelRightLower.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				  .add(jTabbedPaneRightLower, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				.add(jTabbedPaneRightLower, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
 				);
     }
     
     /** create the content pane */
     private void createContentPane()
     {
 	createMenuBar();
 	createToolBar();
 	createDbConnectionPanel();
 	createLeftPanel();
 	createRightUpperPanel();
 	createRightLowerPanel();
 	
 	
 	jSplitPane.setDividerLocation(0.55);
         jSplitPane.setResizeWeight(0.5);
 	jSplitPaneRight.setDividerLocation(0.5);
 	jSplitPaneRight.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
         jSplitPaneRight.setResizeWeight(0.5);
         jSplitPane.setRightComponent(jSplitPaneRight);
 	
 	// PS
 	jSplitPane.setLeftComponent(jPanelLeft);
 	jSplitPaneRight.setLeftComponent(jPanelRightUpper);
 	jSplitPaneRight.setRightComponent(jPanelRightLower);
 
 	jProgressBar.setStringPainted(true);
 	// END PS
 	
 	
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanelContentPane);
         jPanelContentPane.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				  .add(jProgressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1019, Short.MAX_VALUE)
 				  .add(jSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1019, Short.MAX_VALUE)
 				  .add(jPanelDbConnection, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				  .add(jToolBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1019, Short.MAX_VALUE)
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				.add(layout.createSequentialGroup()
 				     .add(jToolBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				     .add(jPanelDbConnection, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				     .add(jSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
 				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				     .add(jProgressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 				);
     }
     
 }
