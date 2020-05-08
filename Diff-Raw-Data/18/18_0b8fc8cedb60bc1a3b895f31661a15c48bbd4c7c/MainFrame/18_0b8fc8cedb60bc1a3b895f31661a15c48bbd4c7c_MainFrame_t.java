 package org.munta.gui;
 
 import java.awt.Frame;
 import java.awt.GridLayout;
 import java.awt.HeadlessException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import org.munta.NatClassApp;
 import org.munta.projectengine.ProjectManager;
 
 public class MainFrame extends JFrame {
 
     private NatClassApp app;
     private EntityViewModel entityViewModel = null;
     private EntityDetailsViewModel entityDetailsViewModel = null;
     private RegularityViewModel regularityViewModel = null;
     private RegularityDetailsViewModel regularityDetailsViewModel = null;
     private EntityViewModel classViewModel = null;
     private EntityDetailsViewModel classDetailsViewModel = null;
     private ActionListener exitActionListener = new ActionListener() {
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             app.exitApplication();
         }
     };
     
     private JFileChooser fileChooser;
     private ActionListener openProjectActionListener = new ActionListener() {
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             int rVal = fileChooser.showOpenDialog(MainFrame.this);
             if (rVal == JFileChooser.APPROVE_OPTION) {
                 app.openProject(fileChooser.getSelectedFile().getAbsolutePath());
             }
         }
     };
     
     private ActionListener saveAsProjectActionListener = new ActionListener() {
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             int rVal = fileChooser.showSaveDialog(MainFrame.this);
             if (rVal == JFileChooser.APPROVE_OPTION) {
                 app.saveAsProject(fileChooser.getSelectedFile().getAbsolutePath());
             }
         }
     };
     
     private void initFileChoosers() {
         FileNameExtensionFilter filter = new FileNameExtensionFilter("NatClass 2.0 Project Files (*.ncp)", "ncp");
         
         fileChooser = new JFileChooser();
         fileChooser.setCurrentDirectory(new java.io.File( "." ));
         fileChooser.setFileFilter(filter);
     }
 
     private void initMenuBar() {
         JMenuBar menuBar = new JMenuBar();
         JMenu fileMenu = new JMenu("File");
         JMenuItem newProjMenuItem = new JMenuItem("New Project");
         fileMenu.add(newProjMenuItem);
         JMenuItem openProjMenuItem = new JMenuItem("Open project...");
         openProjMenuItem.addActionListener(openProjectActionListener);
         fileMenu.add(openProjMenuItem);
         JMenuItem saveProjMenuItem = new JMenuItem("Save");
         fileMenu.add(saveProjMenuItem);
         JMenuItem saveAsProjMenuItem = new JMenuItem("Save As...");
         saveAsProjMenuItem.addActionListener(saveAsProjectActionListener);
         fileMenu.add(saveAsProjMenuItem);
         fileMenu.addSeparator();
         JMenuItem importMenuItem = new JMenuItem("Import...");
         fileMenu.add(importMenuItem);
         JMenuItem exportMenuItem = new JMenuItem("Export...");
         fileMenu.add(exportMenuItem);
         fileMenu.addSeparator();
         JMenuItem exitMenuItem = new JMenuItem("Exit");
         exitMenuItem.addActionListener(exitActionListener);
         fileMenu.add(exitMenuItem);
         menuBar.add(fileMenu);
         setJMenuBar(menuBar);
     }
 
     private void initPanels() {
         entityViewModel = new EntityViewModel(ProjectManager.getInstance().getCollectionOfEntities());
         entityDetailsViewModel = new EntityDetailsViewModel(entityViewModel);
         ProjectManager.getInstance().getCollectionOfEntities().addCollectionChangedListener(entityViewModel);
 
         regularityViewModel = new RegularityViewModel(ProjectManager.getInstance().getCollectionOfRegularities());
         regularityDetailsViewModel = new RegularityDetailsViewModel(regularityViewModel);
         ProjectManager.getInstance().getCollectionOfRegularities().addCollectionChangedListener(regularityViewModel);
 
         classViewModel = new EntityViewModel(ProjectManager.getInstance().getCollectionOfIdealClasses());
         classDetailsViewModel = new EntityDetailsViewModel(classViewModel);
         ProjectManager.getInstance().getCollectionOfIdealClasses().addCollectionChangedListener(classViewModel);
 
         // Entity list and details
         JList entityList = new JList();
         entityList.setModel(entityViewModel);
         JList entityDetailsList = new JList();
         entityDetailsList.setModel(entityDetailsViewModel);
         entityList.addListSelectionListener(entityDetailsViewModel);
 
         JPanel entityPanel = new JPanel();
         entityPanel.setLayout(new GridLayout(1, 2));
         entityPanel.add(new JScrollPane(entityList));
         entityPanel.add(new JScrollPane(entityDetailsList));
 
         // Regularity list and details
         JList regularityList = new JList();
         regularityList.setModel(regularityViewModel);
         JList regularityDetailsList = new JList();
         regularityDetailsList.setModel(regularityDetailsViewModel);
         regularityList.addListSelectionListener(regularityDetailsViewModel);
 
         JPanel regularityPanel = new JPanel();
         regularityPanel.setLayout(new GridLayout(1, 2));
         regularityPanel.add(new JScrollPane(regularityList));
         regularityPanel.add(new JScrollPane(regularityDetailsList));
 
         // Classes list and details
         JList classList = new JList();
         classList.setModel(classViewModel);
         JList classDetailsList = new JList();
         classDetailsList.setModel(classDetailsViewModel);
         classList.addListSelectionListener(classDetailsViewModel);
 
         JPanel classPanel = new JPanel();
         classPanel.setLayout(new GridLayout(1, 2));
         classPanel.add(new JScrollPane(classList));
         classPanel.add(new JScrollPane(classDetailsList));
 
         JSplitPane innerSplitPane =
                 new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, regularityPanel, classPanel);
         innerSplitPane.setBorder(null);
 
         JSplitPane outerSplitPane =
                 new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, entityPanel, innerSplitPane);
 
         add(outerSplitPane);
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
 
         this.app = app;
         initFileChoosers();
         initMenuBar();
         initPanels();
 
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
         setLocationRelativeTo(null);
         setVisible(true);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     }
 }
