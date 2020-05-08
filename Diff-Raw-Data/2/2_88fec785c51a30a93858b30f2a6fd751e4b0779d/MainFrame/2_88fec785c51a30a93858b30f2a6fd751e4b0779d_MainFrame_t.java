 package org.munta.gui;
 
 import java.awt.BorderLayout;
 import java.awt.FileDialog;
 import java.awt.GridLayout;
 import java.awt.HeadlessException;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.Map.Entry;
 import javax.swing.AbstractAction;
 import javax.swing.AbstractButton;
 import javax.swing.Action;
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultButtonModel;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JToolBar;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import org.munta.NatClassApp;
 import org.munta.model.Entity;
 import org.munta.model.Regularity;
 import org.munta.projectengine.ProjectManager;
 
 public class MainFrame extends JFrame {
 
     private NatClassApp app;
     //Colorer
     private AnalysisColorer colorer = new AnalysisColorer();
     //List models
     private EntityViewModel entityViewModel = null;
     private EntityDetailsViewModel entityDetailsViewModel = null;
     private RegularityViewModel regularityViewModel = null;
     private RegularityDetailsViewModel regularityDetailsViewModel = null;
     private ClassesViewModel classViewModel = null;
     private ClassesDetailsViewModel classDetailsViewModel = null;
     // Lists
     private JList entityList;
     private JList entityDetailsList;
     private JList regularityList;
     private JList regularityDetailsList;
     private JList classList;
     private JList classDetailsList;
     // FileDialog
     private FileDialog fileDialog;
     // Other stuff
     private JStatusBar statusBar;
     // Actions
     private Action exitAction = new AbstractAction("Exit") {
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             app.exitApplication();
         }
     };
     private Action newProjectAction = new AbstractAction("New Project") {
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             app.newProject();
         }
     };
     private Action openProjectAction = new AbstractAction("Open project...") {
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             //int rVal = fileChooser.showOpenDialog(MainFrame.this);
             //if (rVal == JFileChooser.APPROVE_OPTION) {
             //    app.openProject(fileChooser.getSelectedFile().getAbsolutePath());
             //}
 
             fileDialog.setMode(FileDialog.LOAD);
             fileDialog.setVisible(true);
 
             if (fileDialog.getFile() == null || fileDialog.getFile().isEmpty()) {
                 return;
             }
 
             app.openProject(new File(fileDialog.getDirectory(), fileDialog.getFile()).getAbsolutePath());
         }
     };
     private Action saveAsProjectAction = new AbstractAction("Save As...") {
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             //int rVal = fileChooser.showSaveDialog(MainFrame.this);
             //if (rVal == JFileChooser.APPROVE_OPTION) {
             //    app.saveAsProject(fileChooser.getSelectedFile().getAbsolutePath());
             //}
             fileDialog.setMode(FileDialog.SAVE);
             fileDialog.setVisible(true);
 
             if (fileDialog.getFile() == null || fileDialog.getFile().isEmpty()) {
                 return;
             }
 
             app.saveAsProject(new File(fileDialog.getDirectory(), fileDialog.getFile()).getAbsolutePath());
         }
     };
     private Action setOverviewModelAction = new AbstractAction("Overview") {
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             colorer.setOverviewMode();
             redrawLists();
         }
     };
     private Action setEntityAnalysisModelAction = new AbstractAction("Object Analysis") {
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             
             colorer.setEntityAnalysisMode();
             setEntityAnalysisModelSetEntityAction.actionPerformed(null);
             setEntityAnalysisModelSetClassAction.actionPerformed(null);
         }
     };
     private Action setEntityAnalysisModelSetEntityAction = new AbstractAction("Set master object") {
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             if(colorer.getMode() != AnalysisColorer.ENTITY_ANALYSIS)
                 return;
             
             int index = entityList.getSelectedIndex();
             if(index != -1) {
                 colorer.setEntity((Entity)entityViewModel.getModelObjectAt(index));
             }
             
             redrawLists();
         }
     };
     private Action setEntityAnalysisModelSetClassAction = new AbstractAction("Set master class") {
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             if(colorer.getMode() != AnalysisColorer.ENTITY_ANALYSIS)
                 return;
             
             int index = classList.getSelectedIndex();
             if(index != -1) {
                 colorer.setIdealClass((Entity)classViewModel.getModelObjectAt(index));
             }
             
             redrawLists();
         }
     };
     private Action setRegularityAnalysisModelAction = new AbstractAction("Regularity Analysis") {
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             colorer.setRegularityAnalysisMode();
             
             int index = regularityList.getSelectedIndex();
             if(index != -1) {
                 colorer.setRegularity(((Entry<String, Regularity>)regularityViewModel.getModelObjectAt(index)).getValue());
             }
             
             redrawLists();
         }
     };
     private ListSelectionListener regularityAnalysisModelListener = new ListSelectionListener() {
 
         @Override
         public void valueChanged(ListSelectionEvent lse) {
             
             if(lse.getValueIsAdjusting()) {
                 return;
             }
             
             int index = regularityList.getSelectedIndex();
             if(index == -1) {
                 return;
             }
             
             if(colorer.getMode() == AnalysisColorer.REGULARITY_ANALYSIS) {
                 
                 int entityIndex = entityList.getSelectedIndex();
                 int classIndex = classList.getSelectedIndex();
                 entityList.clearSelection();
                 classList.clearSelection();
                 
                 colorer.setRegularity(((Entry<String, Regularity>)regularityViewModel.getModelObjectAt(index)).getValue());
                 
                 redrawLists();
                 
                 if(entityIndex >= 0) { 
                     entityList.setSelectedIndex(entityIndex);
                 }
                 if(classIndex >= 0) { 
                     classList.setSelectedIndex(classIndex);
                 }
             }
         }
     };
     
     private Action setClassesAnalysisModelAction = new AbstractAction("Classes Analysis") {
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             colorer.setClassAnalysisMode();
             
             int index = classList.getSelectedIndex();
             if(index != -1) {
                 colorer.setIdealClass((Entity)classViewModel.getModelObjectAt(index));
             }
             redrawLists();
         }
     };
     
     private ListSelectionListener classAnalysisModelListener = new ListSelectionListener() {
 
         @Override
         public void valueChanged(ListSelectionEvent lse) {
             
             if(lse.getValueIsAdjusting()) {
                 return;
             }
             
             int index = classList.getSelectedIndex();
             if(index == -1) {
                 return;
             }
             
             if(colorer.getMode() == AnalysisColorer.CLASS_ANALYSIS) {
                 colorer.setIdealClass((Entity)classViewModel.getModelObjectAt(index));
                 redrawLists();
             }
         }
     };
     
     private Action startStopAction = new AbstractAction("StartStop") {
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             app.startStop();
         }
     };
     
     private Action buildReguilaritiesAction = new AbstractAction("Build Regularities") {
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             final ActionEvent fae = ae;
             
             new Thread(new Runnable() {
 
                 @Override
                 public void run() {
                     AbstractButton b = ((AbstractButton)fae.getSource());       
                     b.setAction(cancelProcessAction);
                     buildReguilaritiesAction.setEnabled(false);
                     buildIdealClassesAction.setEnabled(false);
                     app.buildRegularities();
                     buildReguilaritiesAction.setEnabled(true);
                     buildIdealClassesAction.setEnabled(true);
                     b.setAction(buildReguilaritiesAction);
                 }
             }).start();
         }
     };
     
     private Action buildIdealClassesAction = new AbstractAction("Build classes") {
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             final ActionEvent fae = ae;
             
             new Thread(new Runnable() {
 
                 @Override
                 public void run() {
                     AbstractButton b = ((AbstractButton)fae.getSource());       
                     b.setAction(cancelProcessAction);
                     buildIdealClassesAction.setEnabled(false);
                     buildReguilaritiesAction.setEnabled(false);
                     app.buildIdealClasses();
                     buildIdealClassesAction.setEnabled(true);
                     buildReguilaritiesAction.setEnabled(true);
                    b.setAction(buildIdealClassesAction);
                 }
             }).start();
         }
         
     };
     
     private Action cancelProcessAction = new AbstractAction("Stop") {
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             final ActionEvent fae = ae;
             
             int m = JOptionPane.showConfirmDialog(MainFrame.this,
                     "Do you want to stop current calculation process?",
                     "Are you sure?",
                     JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
             if(m == JOptionPane.YES_OPTION) {
                 app.stopAlgoritms();
             }
         }
     };
     
     private DefaultButtonModel overviewButtonModel = new AnalysisModeButtonModel(colorer, AnalysisColorer.OVERVIEW);
     private DefaultButtonModel entitiesButtonModel = new AnalysisModeButtonModel(colorer, AnalysisColorer.ENTITY_ANALYSIS);
     private DefaultButtonModel regularitiesButtonModel = new AnalysisModeButtonModel(colorer, AnalysisColorer.REGULARITY_ANALYSIS);
     private DefaultButtonModel classesButtonModel = new AnalysisModeButtonModel(colorer, AnalysisColorer.CLASS_ANALYSIS);
 
     private Icon getIconFromResource(String iconName) {
         String iconPath = String.format("images/%s.png", iconName);
         return new ImageIcon(MainFrame.class.getResource(iconPath));
     }
 
     private void initFileChoosers() {
         //FileNameExtensionFilter filter = new FileNameExtensionFilter("NatClass 2.0 Project Files (*.ncp)", "ncp");
         FilenameFilter ff = new FilenameFilter() {
 
             @Override
             public boolean accept(File file, String string) {
                 return string.toLowerCase().endsWith(".ncp");
             }
         };
 
         fileDialog = new FileDialog(this);
         fileDialog.setDirectory(new java.io.File(".").getAbsolutePath());
         fileDialog.setFilenameFilter(ff);
         fileDialog.setModal(true);
         //fileChooser = new JFileChooser();
         //fileChooser.setCurrentDirectory(new java.io.File("."));
         //fileChooser.setFileFilter(filter);
     }
 
     private void initMenuBar() {
         JMenuBar menuBar = new JMenuBar();
 
         JMenu fileMenu = new JMenu("File");
         JMenuItem newProjMenuItem = new JMenuItem();
         newProjMenuItem.setAction(newProjectAction);
         fileMenu.add(newProjMenuItem);
         JMenuItem openProjMenuItem = new JMenuItem();
         openProjMenuItem.setAction(openProjectAction);
         fileMenu.add(openProjMenuItem);
         JMenuItem saveProjMenuItem = new JMenuItem("Save");
         fileMenu.add(saveProjMenuItem);
         JMenuItem saveAsProjMenuItem = new JMenuItem();
         saveAsProjMenuItem.setAction(saveAsProjectAction);
         fileMenu.add(saveAsProjMenuItem);
         fileMenu.addSeparator();
         JMenuItem importMenuItem = new JMenuItem("Import...");
         fileMenu.add(importMenuItem);
         JMenuItem exportMenuItem = new JMenuItem("Export...");
         fileMenu.add(exportMenuItem);
 
         if (!NatClassApp.isMac()) {
             fileMenu.addSeparator();
             JMenuItem exitMenuItem = new JMenuItem();
             exitMenuItem.setAction(exitAction);
             fileMenu.add(exitMenuItem);
         }
         menuBar.add(fileMenu);
 
         ButtonGroup modeGroup = new ButtonGroup();
         JMenu modeMenu = new JMenu("View");
         JMenuItem menuItem;
         
         menuItem = new JRadioButtonMenuItem();
         menuItem.setAction(setOverviewModelAction);
         menuItem.setModel(overviewButtonModel);
         modeMenu.add(menuItem);
         modeGroup.add(menuItem);
         
         menuItem = new JRadioButtonMenuItem();
         menuItem.setAction(setEntityAnalysisModelAction);
         menuItem.setModel(entitiesButtonModel);
         modeMenu.add(menuItem);
         modeGroup.add(menuItem);
         
         menuItem = new JRadioButtonMenuItem();
         menuItem.setAction(setRegularityAnalysisModelAction);
         menuItem.setModel(regularitiesButtonModel);
         modeMenu.add(menuItem);
         modeGroup.add(menuItem);
         
         menuItem = new JRadioButtonMenuItem();
         menuItem.setAction(setClassesAnalysisModelAction);
         menuItem.setModel(classesButtonModel);
         modeMenu.add(menuItem);
         modeGroup.add(menuItem);
         
         menuBar.add(modeMenu);
         setJMenuBar(menuBar);
     }
 
     private void initToolBar() {
         JToolBar toolBar = new JToolBar();
         toolBar.setFloatable(false);
         toolBar.setRollover(true);
 
         JButton button;
         button = new JButton();
         button.setAction(newProjectAction);
         button.setIcon(getIconFromResource("new"));
         button.setText(null);
         toolBar.add(button);
 
         button = new JButton();
         button.setAction(openProjectAction);
         button.setIcon(getIconFromResource("open"));
         button.setText(null);
         toolBar.add(button);
 
         button = new JButton();
         button.setAction(newProjectAction);
         button.setIcon(getIconFromResource("save"));
         button.setText(null);
         toolBar.add(button);
 
         toolBar.addSeparator();
         button = new JButton();
         button.setAction(buildReguilaritiesAction);
         toolBar.add(button);
         
         button = new JButton();
         button.setAction(buildIdealClassesAction);
         toolBar.add(button);
         
         toolBar.addSeparator();
         
         ButtonGroup bg = new ButtonGroup();
         JRadioButton radioButton;
         
         radioButton = new JRadioButton();
         radioButton.setAction(setOverviewModelAction);
         radioButton.setModel(overviewButtonModel);
         bg.add(radioButton);
         toolBar.add(radioButton);
         
         radioButton = new JRadioButton();
         radioButton.setAction(setEntityAnalysisModelAction);
         radioButton.setModel(entitiesButtonModel);
         bg.add(radioButton);
         toolBar.add(radioButton);
         
         radioButton = new JRadioButton();
         radioButton.setAction(setRegularityAnalysisModelAction);
         radioButton.setModel(regularitiesButtonModel);
         bg.add(radioButton);
         toolBar.add(radioButton);
         
         radioButton = new JRadioButton();
         radioButton.setAction(setClassesAnalysisModelAction);
         radioButton.setModel(classesButtonModel);
         bg.add(radioButton);
         toolBar.add(radioButton);
         
         toolBar.addSeparator();
         button = new JButton();
         button.setAction(startStopAction);
         toolBar.add(button);
         
         add(toolBar, BorderLayout.PAGE_START);
     }
 
     private void initPanels() {
         entityViewModel = new EntityViewModel(colorer, ProjectManager.getInstance().getCollectionOfEntities());
         entityDetailsViewModel = new EntityDetailsViewModel(colorer, entityViewModel);
         ProjectManager.getInstance().getCollectionOfEntities().addCollectionChangedListener(entityViewModel);
 
         regularityViewModel = new RegularityViewModel(colorer, ProjectManager.getInstance().getCollectionOfRegularities());
         regularityDetailsViewModel = new RegularityDetailsViewModel(colorer, regularityViewModel);
         ProjectManager.getInstance().getCollectionOfRegularities().addCollectionChangedListener(regularityViewModel);
 
         classViewModel = new ClassesViewModel(colorer, ProjectManager.getInstance().getCollectionOfIdealClasses());
         classDetailsViewModel = new ClassesDetailsViewModel(colorer, classViewModel);
         ProjectManager.getInstance().getCollectionOfIdealClasses().addCollectionChangedListener(classViewModel);
 
         CellRenderer cr = new CellRenderer();
 
         // Entity list and details
         entityList = new JList();
         entityList.setCellRenderer(cr);
         entityList.setModel(entityViewModel);
         entityDetailsList = new JList();
         entityDetailsList.setCellRenderer(cr);
         entityDetailsList.setModel(entityDetailsViewModel);
         entityList.addListSelectionListener(entityDetailsViewModel);
         entityList.addMouseListener(new MouseAdapter() {
             
             @Override
             public void mouseClicked(MouseEvent me) {
                 if(me.getClickCount() == 2 || !colorer.isEntityAnalysisReady()) {
                     setEntityAnalysisModelSetEntityAction.actionPerformed(null);
                 }
                 super.mouseClicked(me);
             }
             
         });
         entityList.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent ke) {
                 if(ke.getKeyCode() == KeyEvent.VK_ENTER) {
                     setEntityAnalysisModelSetEntityAction.actionPerformed(null);
                 }
                 super.keyPressed(ke);
             }
             
         });
 
         JPanel entityPanel = new JPanel();
         entityPanel.setLayout(new GridLayout());
         entityPanel.add(new JScrollPane(entityList));
         entityPanel.add(new JScrollPane(entityDetailsList));
 
         // Regularity list and details
         regularityList = new JList();
         regularityList.setCellRenderer(cr);
         regularityList.setModel(regularityViewModel);
         regularityDetailsList = new JList();
         regularityDetailsList.setCellRenderer(cr);
         regularityDetailsList.setModel(regularityDetailsViewModel);
         regularityList.addListSelectionListener(regularityDetailsViewModel);
         regularityList.addListSelectionListener(regularityAnalysisModelListener);
 
         JPanel regularityPanel = new JPanel();
         regularityPanel.setLayout(new GridLayout());
         regularityPanel.add(new JScrollPane(regularityList));
         regularityPanel.add(new JScrollPane(regularityDetailsList));
 
         // Classes list and details
         classList = new JList();
         classList.setCellRenderer(cr);
         classList.setModel(classViewModel);
         classDetailsList = new JList();
         classDetailsList.setCellRenderer(cr);
         classDetailsList.setModel(classDetailsViewModel);
         classList.addListSelectionListener(classDetailsViewModel);
         classList.addListSelectionListener(classAnalysisModelListener);
         classList.addMouseListener(new MouseAdapter() {
             
             @Override
             public void mouseClicked(MouseEvent me) {
                 if(me.getClickCount() == 2 || !colorer.isClassAnalysisReady()) {
                     setEntityAnalysisModelSetClassAction.actionPerformed(null);
                 }
                 super.mouseClicked(me);
             }
             
         });
         classList.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent ke) {
                 if(ke.getKeyCode() == KeyEvent.VK_ENTER) {
                     setEntityAnalysisModelSetClassAction.actionPerformed(null);
                 }
                 super.keyPressed(ke);
             }
             
         });
 
         JPanel classPanel = new JPanel();
         classPanel.setLayout(new GridLayout());
         classPanel.add(new JScrollPane(classList));
         classPanel.add(new JScrollPane(classDetailsList));
 
         JSplitPane innerSplitPane =
                 new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, regularityPanel, classPanel);
         innerSplitPane.setBorder(null);
         innerSplitPane.setDividerLocation(0.5);
         innerSplitPane.setResizeWeight(0.5);
         JSplitPane outerSplitPane =
                 new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, entityPanel, innerSplitPane);
         outerSplitPane.setBorder(null);
         outerSplitPane.setDividerLocation(0.3);
         outerSplitPane.setResizeWeight(0.3);
 
         add(outerSplitPane, BorderLayout.CENTER);
     }
     
     private void redrawLists() {
         entityViewModel.redrawList();
         entityDetailsViewModel.redrawList();
         regularityViewModel.redrawList();
         regularityDetailsViewModel.redrawList();
         classViewModel.redrawList();
         classDetailsViewModel.redrawList();
     }
     
     private void initStatusBar() {
         statusBar = new JStatusBar();
         add(statusBar, BorderLayout.SOUTH);
     }
 
     @Override
     public void dispose() {
         entityViewModel.dispose();
         entityDetailsViewModel.dispose();
 
         regularityViewModel.dispose();
         regularityDetailsViewModel.dispose();
 
         classViewModel.dispose();
         classDetailsViewModel.dispose();
 
         ProjectManager.getInstance().getCollectionOfEntities().removeCollectionChangedListener(entityViewModel);
         ProjectManager.getInstance().getCollectionOfRegularities().removeCollectionChangedListener(regularityViewModel);
         ProjectManager.getInstance().getCollectionOfIdealClasses().removeCollectionChangedListener(classViewModel);
         super.dispose();
     }
 
     public MainFrame(NatClassApp app) throws HeadlessException {
         super("NatClass 2.0");
 
         setLayout(new BorderLayout());
 
         this.app = app;
         initFileChoosers();
         initMenuBar();
         initToolBar();
         initPanels();
         initStatusBar();
 
         pack();
         setSize(800, 480);
         setLocationRelativeTo(null);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         setVisible(true);
         setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
     }
 }
