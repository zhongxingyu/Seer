 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ecn.pappl.visedsim.view;
 
 import ecn.pappl.visedsim.controller.criteriapreselection.CriteriaPreselectionController;
 import ecn.pappl.visedsim.controller.projectlist.ProjectListController;
 import ecn.pappl.visedsim.controller.projectviewers.SwingProjectViewerController;
 import ecn.pappl.visedsim.struct.Project;
 import java.awt.Dimension;
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 
 /**
  * Content the public actions for the two main frames.
  *
  * @author Denis
  */
 public abstract class AbstractMainFrame extends JFrame {
 
     protected JComboBox projectsComboBox;
     protected JLabel projectTitle;
     protected JTable projectTable;
     protected DefaultTableModel tableModel;
     protected JScrollPane scrollpane;
     protected JPanel middlePanel;
     private static final int MIN_HEIGHT = 300;
     protected static final int MIN_WIDTH = 580;
     protected static final int MIN_BAR_HEIGHT = 30;
 
     /**
      * Build the frame
      */
     protected void build() {
         setTitle(Labels.MAIN_FRAME_TITLE);
         setLocationRelativeTo(null);
         setResizable(true);
         setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
         setMaximumSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setContentPane(buildContentPane());
         pack();
     }
 
     /**
      * Build the main panel
      * 
      * @return the main panel
      */
     protected abstract JPanel buildContentPane();
 
     /**
      * Get the projectTable
      * 
      * @return the projectTable 
      */
     public JTable getProjectTable() {
         return projectTable;
     }
 
     /**
      * Set the projectTable
      * 
      * @param projectTable 
      */
     public void setProjectTable(JTable projectTable) {
         this.projectTable = projectTable;
     }
 
     /**
      * Get the projectTitle
      * 
      * @return the projectTitle 
      */
     public JLabel getProjectTitle() {
         return projectTitle;
     }
 
     /**
      * Set the projectTitle
      * 
      * @param projectTitle 
      */
     public void setProjectTitle(JLabel projectTitle) {
         this.projectTitle = projectTitle;
     }
 
     /**
      * Get the projectsComboBox
      * 
      * @return the projectsComboBox 
      */
     public JComboBox getProjectsComboBox() {
         return projectsComboBox;
     }
 
     /**
      * Set a projectsComboBox
      * 
      * @param projectsComboBox 
      */
     public void setProjectsComboBox(JComboBox projectsComboBox) {
         this.projectsComboBox = projectsComboBox;
     }
 
     /**
      * Allow the user to modify criteria
      * <p/>
      * @param evt
      */
     protected void chooseCriteriaActionEvent(java.awt.event.ActionEvent evt) {
         ChooseCriteria chooseCriteria = new ChooseCriteria(this);
         chooseCriteria.setVisible(true);
     }
 
     /**
      * Open a new JDialog to give a name to the new criteria preselection
      * 
      * @param evt 
      */
     protected void preselectionSavedItemActionEvent(
             java.awt.event.ActionEvent evt) {
         PreselectionSaving ps = new PreselectionSaving();
         ps.setVisible(true);
     }
 
     /**
      * Launch the project selected in the comboBox
      * 
      * @param evt 
      */
     protected void validateButtonActionEvent(java.awt.event.ActionEvent evt) {
         String fileName = (String) this.projectsComboBox.getSelectedItem();
         Project project = ProjectListController.getInstance().
                 getProjectByAcronym(fileName);
         if (project != null) {
             SwingProjectViewerController.getInstance().loadProject(project);
             this.updateProjectView();
         }
     }
 
     /**
      * Launch the first frame of the program to load a new project list
      * 
      * @param evt 
      */
     protected void newListProjectItemActionButton(java.awt.event.ActionEvent evt) {
         FileLoader fileLoader = new FileLoader();
         fileLoader.setVisible(true);
         this.dispose();
     }
 
     /**
      * Launch a new JDialog to delete some preselections
      * 
      * @param evt 
      */
     protected void preselectionManagementItemActionButton(java.awt.event.ActionEvent evt) {
         CriteriaManagement preselectionManagement = new CriteriaManagement();
         preselectionManagement.setVisible(true);
     }
 
     /**
      * Reload the element of the project table
      */
     public void updateProjectView() {
         SwingProjectViewerController spvc = SwingProjectViewerController.getInstance();
         this.projectTitle.setText(spvc.getAcronym() + " : " + spvc.getTitle());
         Object[][] tableContent =
                 spvc.getCriteria(CriteriaPreselectionController.getInstance().
                 getCriteriaPreselection());
         Object[] columnsName = new Object[]{"Critère", "Valeur"};
         this.tableModel.setDataVector(tableContent, columnsName);
         this.tableModel.fireTableStructureChanged();
        this.projectTable.revalidate();
         this.scrollpane.repaint();
     }
 }
