 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ecn.pappl.visedsim.view;
 
 import ecn.pappl.visedsim.controller.criteriapreselection.CriteriaPreselectionController;
 import ecn.pappl.visedsim.controller.projectlist.ProjectListController;
 import ecn.pappl.visedsim.controller.projectviewers.SwingProjectViewerController;
 import ecn.pappl.visedsim.struct.Project;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 
 /**
  *
  * The main frame of the program for a simple user
  *
  * @author Denis
  */
 public class MainFrameUser extends AbstractMainFrame {
 
     private static final int TEXT_FIELD_LENGTH = 10;
     private JMenuBar menuBar;
     private JMenu projectMenu, criteriaMenu, helpMenu;
     private JMenuItem newListProjectItem, preselectionSavedItem, preselectionManagementItem, chooseCriteriaItem;
     private JButton chooseCriteriaButton, validateButton, searchButton;
     private JTextField searchProjectField;
     
     //Integers used in the compact grids
     private static final int PANEL_NUMBER_OF_COLUMN = 1;
     private static final int PANEL_NUMBER_OF_ROW = 3;
     private static final int MIDDLEPANEL_NUMBER_OF_COLUMN = 1;
     private static final int MIDDLEPANEL_NUMBER_OF_ROW = 2;
     private static final int BUTTONPANEL_NUMBER_OF_COLUMN = 3;
     private static final int BUTTONPANEL_NUMBER_OF_ROW = 1;
     private static final int GRID_INITIAL_X = 5;
     private static final int GRID_INITIAL_Y = 5;
 
     /**
      * The constructor of the main frame
      */
     public MainFrameUser() {
         super();
         SwingProjectViewerController spvc = SwingProjectViewerController.getInstance();
         Object[][] tableContent =
                 spvc.getCriteria(CriteriaPreselectionController.getInstance().
                 getCriteriaPreselection());
         Object[] columnsName = new Object[]{"Critère", "Valeur"};
         tableModel = new DefaultTableModel(tableContent, columnsName) {
 
             @Override
             public boolean isCellEditable(int iRowIndex, int iColumnIndex) {
                 return false;
             }
         };
         build();
     }
 
     @Override
     protected JPanel buildContentPane() {
         JPanel panelCenter = new JPanel();
         panelCenter.setLayout(new BorderLayout());
         panelCenter.setBackground(Color.white);
 
         JPanel panel = new JPanel(new SpringLayout());
         panel.setBackground(Color.white);
 
         //Menu
         menuBar = new JMenuBar();
         menuBar.setMinimumSize(new Dimension(MIN_WIDTH, MIN_BAR_HEIGHT));
         projectMenu = new JMenu(Labels.MENU_PROJECT);
 
         newListProjectItem = new JMenuItem(Labels.MENU_PROJECT_NEW_LIST);
         newListProjectItem.addActionListener(new java.awt.event.ActionListener() {
 
             /**
              * Launch the FileLoader to let the user choosing a new XML file
              */
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 newListProjectItemActionButton(evt);
             }
         });
         projectMenu.add(newListProjectItem);
 
         menuBar.add(projectMenu);
 
         criteriaMenu = new JMenu(Labels.MENU_CRITERIA);
 
         preselectionSavedItem = new JMenuItem(Labels.MENU_CRITERIA_SAVE);
         preselectionSavedItem.addActionListener(new java.awt.event.ActionListener() {
 
             /**
              * Launch the PreselectionSaving to let the user saving the current
              * preselection
              */
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 preselectionSavedItemActionEvent(evt);
             }
         });
         criteriaMenu.add(preselectionSavedItem);
 
         preselectionManagementItem = new JMenuItem(Labels.MENU_CRITERIA_PRESELECTION_MANAGEMENT);
         preselectionManagementItem.addActionListener(new java.awt.event.ActionListener() {
             /**
              * Launch the CriteriaManagement to let the user delete a
              * preselection
              */
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 preselectionManagementItemActionButton(evt);
             }
         });
         criteriaMenu.add(preselectionManagementItem);
 
         chooseCriteriaItem = new JMenuItem(Labels.MENU_CRITERIA_CHOOSE);
         chooseCriteriaItem.addActionListener(new java.awt.event.ActionListener() {
 
             /**
              * Open the ChooseCriteria to choose the criteria selection
              */
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chooseCriteriaActionEvent(evt);
             }
         });
         criteriaMenu.add(chooseCriteriaItem);
 
         menuBar.add(criteriaMenu);
 
         helpMenu = new JMenu(Labels.MENU_HELP);
         menuBar.add(helpMenu);
 
         panel.add(menuBar);
 
         //2nd line with buttons
         JPanel buttonPanel = new JPanel(new SpringLayout());
         buttonPanel.setBackground(Color.white);
 
         JPanel criteriaPanel = new JPanel(new FlowLayout());
         criteriaPanel.setBackground(Color.white);
 
         chooseCriteriaButton = new JButton(
                 Labels.MAIN_FRAME_CHOOSE_CRITERIA_BUTTON);
         chooseCriteriaButton.addActionListener(new java.awt.event.ActionListener() {
 
             /**
              * Open the ChooseCriteria to choose the criteria selection
              */
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chooseCriteriaActionEvent(evt);
             }
         });
         criteriaPanel.add(chooseCriteriaButton);
 
         buttonPanel.add(criteriaPanel);
 
         JPanel chooseProjectPanel = new JPanel(new FlowLayout());
         chooseProjectPanel.setBackground(Color.white);
 
         ProjectListController plc = ProjectListController.getInstance();
         projectsComboBox = new JComboBox(plc.getProjectsAcronyms().toArray(
                 new String[0]));
         chooseProjectPanel.add(projectsComboBox);
 
         validateButton = new JButton(Labels.MAIN_FRAME_VALIDATE_PROJECT_BUTTON);
         validateButton.addActionListener(new java.awt.event.ActionListener() {
 
             /**
              * Launch the selected project
              */
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 validateButtonActionEvent(evt);
             }
         });
         chooseProjectPanel.add(validateButton);
 
         buttonPanel.add(chooseProjectPanel);
 
         JPanel searchProjectPanel = new JPanel(new FlowLayout());
         searchProjectPanel.setBackground(Color.white);
 
         searchProjectField = new JTextField();
         searchProjectField.setColumns(TEXT_FIELD_LENGTH);
         searchProjectPanel.add(searchProjectField);
 
         searchButton = new JButton(Labels.MAIN_FRAME_SEARCH_PROJECT_BUTTON);
         searchButton.addActionListener(new java.awt.event.ActionListener() {
 
             /**
              * Launch the given project or the SeekingProject if there are
              * several projects with the same first letters in their acronyme
              */
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 searchButtonActionEvent(evt);
             }
         });
         searchProjectPanel.add(searchButton);
 
         buttonPanel.add(searchProjectPanel);
 
         SpringUtilities.makeCompactGrid(buttonPanel, BUTTONPANEL_NUMBER_OF_ROW, BUTTONPANEL_NUMBER_OF_COLUMN, GRID_INITIAL_X, GRID_INITIAL_Y, 5, 5);
 
         panel.add(buttonPanel);
 
         //Representation of the project
        middlePanel = new JPanel(new SpringLayout());
 
         SwingProjectViewerController spvc = SwingProjectViewerController.getInstance();
 
         projectTitle = new JLabel(spvc.getAcronym() + " : " + spvc.getTitle());
        middlePanel.add(projectTitle);
 
         projectTable = new JTable(tableModel);
 
         projectTable.addMouseListener(new MouseListener() {
 
             @Override
             public void mouseClicked(MouseEvent arg0) {
 
                 if (arg0.getClickCount() == 2) {
                     int rowNumb = projectTable.rowAtPoint(arg0.getPoint());
                     int colNumb = projectTable.columnAtPoint(arg0.getPoint());
                     if (rowNumb != -1 && colNumb > 0) {
                         String criteria = projectTable.getValueAt(rowNumb, 0).
                                 toString();
                         String value = projectTable.getValueAt(rowNumb, colNumb).
                                 toString();
                         JOptionPane.showMessageDialog(null, value, criteria,
                                 JOptionPane.INFORMATION_MESSAGE);
                     }
                 }
             }
 
             public void mousePressed(MouseEvent me) {
             }
 
             public void mouseReleased(MouseEvent me) {
             }
 
             public void mouseEntered(MouseEvent me) {
             }
 
             public void mouseExited(MouseEvent me) {
             }
         });
         
        middlePanel.add(projectTable);
 
        SpringUtilities.makeCompactGrid(middlePanel, MIDDLEPANEL_NUMBER_OF_ROW, MIDDLEPANEL_NUMBER_OF_COLUMN, GRID_INITIAL_X,GRID_INITIAL_Y, 5, 5);
 
         scrollpane = new JScrollPane(middlePanel);
         panel.add(scrollpane);
 
         SpringUtilities.makeCompactGrid(panel, PANEL_NUMBER_OF_ROW, PANEL_NUMBER_OF_COLUMN, GRID_INITIAL_X, GRID_INITIAL_Y, 10, 10);
 
         panelCenter.add(panel, BorderLayout.CENTER);
 
         return panelCenter;
     }
 
     /**
      * Launch the given project or the SeekingProject if there are several
      * projects with the same first letters in their acronyme
      *
      * @param evt
      */
     protected void searchButtonActionEvent(java.awt.event.ActionEvent evt) {
         ProjectListController plc = ProjectListController.getInstance();
         String fileName = searchProjectField.getText();
 
         if (plc.getProjectsAcronymsByFirstLetters(fileName).
                 isEmpty()) {
             JOptionPane.showMessageDialog(this, Labels.SEEKING_PROJECTS_POPUP);
         } else if (plc.getProjectsAcronymsByFirstLetters(fileName).size() == 1) {
             Project project = plc.getProjectByAcronym(fileName);
             if (project != null) {
                 SwingProjectViewerController.getInstance().loadProject(project);
                 this.updateProjectView();
             }
         } else {
             SeekingProjects sp = new SeekingProjects(this, fileName);
             sp.setVisible(true);
         }
     }
 }
