 /*
  * EDACCSolverConfigPanel.java
  *
  * Created on 30.12.2009, 21:31:12
  */
 package edacc;
 
 import edacc.experiment.SolverTableModel;
 import edacc.model.Solver;
 import edacc.model.SolverConfiguration;
 import edacc.model.SolverConfigurationDAO;
 import edacc.model.SolverDAO;
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 /**
  * A JPanel which serves as container for <code>EDACCSolverConfigPanelSolver</code> objects.
  * @author simon
  * @see edacc.EDACCSolverConfigPanelSolver
  */
 public class EDACCSolverConfigPanel extends javax.swing.JPanel {
 
     private GridBagConstraints gridBagConstraints;
     private GridBagLayout layout;
     private EDACCExperimentMode parent;
     private boolean update;
 
     /** Creates new form EDACCSolverConfigPanel */
     public EDACCSolverConfigPanel() {
         //  initComponents();
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new Insets(5, 5, 5, 5);
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
 
         this.parent = null;
         layout = new GridBagLayout();
         this.setLayout(layout);
         this.update = false;
     }
 
     public void setParent(EDACCExperimentMode parent) {
         this.parent = parent;
     }
 
     public void setTitles() {
         if (!update && parent != null) {
             parent.setTitles();
         }
     }
 
     private void doRepaint() {
         if (!update) {
             this.repaint();
             this.revalidate();
             setTitles();
         }
     }
 
     private int getIndex(int solverId) {
         int solverIndex = 0;
         int solverOrder[] = new int[parent.solTableModel.getRowCount()];
         for (int i = 0; i < parent.solTableModel.getRowCount(); i++) {
             solverOrder[i] = parent.solTableModel.getSolver(i).getId();
             if (solverId == parent.solTableModel.getSolver(i).getId()) {
                 solverIndex = i;
             }
         }
         int currentIndex = 0;
         for (int i = 0; i < this.getComponents().length; i++) {
             EDACCSolverConfigPanelSolver entry = (EDACCSolverConfigPanelSolver) this.getComponents()[i];
             for (int j = currentIndex; j < solverOrder.length; j++) {
                 if (entry.getSolver().getId() == solverOrder[j]) {
                     currentIndex = j;
                     break;
                 }
             }
             if (currentIndex > solverIndex) {
                 return i;
             }
         }
         return this.getComponentCount();
     }
 
     /**
      * Generates a new EDACCSolverConfigEntry for a solver.
      * @param o Solver
      */
     /*  public void addSolver(Object o) {
     if (o instanceof Solver) {
     Solver solver = (Solver) o;
     try {
     EDACCSolverConfigEntry entry = new EDACCSolverConfigEntry(solver);
     entry.setParent(this);
     gridBagConstraints.gridy++;
     this.add(entry, getIndex(solver.getId()));
     setGridBagConstraints();
     parent.solTableModel.setSolverSelected(solver.getId(), true);
     doRepaint();
     } catch (Exception e) {
     e.printStackTrace();
     }
     }
     }*/
     public void addSolver(Object o) {
         if (o instanceof Solver) {
             Solver solver = (Solver) o;
             if (solverExists(solver.getId())) {
                 return;
             }
             try {
                 EDACCSolverConfigPanelSolver entry = new EDACCSolverConfigPanelSolver(solver, this);
                 gridBagConstraints.gridy++;
                 this.add(entry, getIndex(solver.getId()));
                 setGridBagConstraints();
                 parent.solTableModel.setSolverSelected(solver.getId(), true);
 
                 doRepaint();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Generates a new <code>EDACCSolverConfigPanelSolver</code> for a solver configuration or uses an existing one to add the solver configuration.
      * @param solverConfiguration
      * @throws SQLException
      */
     public void addSolverConfiguration(SolverConfiguration solverConfiguration) throws SQLException {
         addSolverConfiguration(solverConfiguration, true);
     }
 
     public void addSolverConfiguration(SolverConfiguration solverConfiguration, boolean useSolverConfiguration) throws SQLException {
         for (int i = 0; i < this.getComponentCount(); i++) {
             if (((EDACCSolverConfigPanelSolver) this.getComponent(i)).getSolver().getId() == solverConfiguration.getSolver_id()) {
                 ((EDACCSolverConfigPanelSolver) this.getComponent(i)).addSolverConfiguration(solverConfiguration, useSolverConfiguration);
                 return;
             }
         }
         EDACCSolverConfigPanelSolver entry = new EDACCSolverConfigPanelSolver(solverConfiguration, this, useSolverConfiguration);
         this.add(entry, getIndex(solverConfiguration.getSolver_id()));
         parent.solTableModel.setSolverSelected(solverConfiguration.getSolver_id(), true);
         setGridBagConstraints();
         doRepaint();
     }
 
     private void setGridBagConstraints() {
         gridBagConstraints.gridy = 0;
         gridBagConstraints.weighty = 1;
         for (int i = 0; i < this.getComponentCount(); i++) {
             gridBagConstraints.gridy++;
             gridBagConstraints.weighty *= 1000;
             layout.setConstraints(this.getComponent(i), gridBagConstraints);
         }
     }
 
     /**
      * Removes the <code>EDACCSolverConfigPanelSolver</code> and every <code>EDACCSolverConfigEntry</code> which was generated with this solver.
      * @param o solver to be removed
      */
     public void removeSolver(Object o) {
         if (!(o instanceof Solver)) {
             return;
         }
         Solver solver = (Solver) o;
 
         for (int i = 0; i < this.getComponentCount(); i++) {
             if (((EDACCSolverConfigPanelSolver) this.getComponent(i)).getSolver().getId() == solver.getId()) {
                 ((EDACCSolverConfigPanelSolver) this.getComponent(i)).removeAll();
                 doRepaint();
                 return;
             }
         }
     }
 
     /**
      * Returns true if a EDACCSolverConfigPanelSolver exists with this solverId
      * @param solverId
      * @return boolean
      */
     public boolean solverExists(int solverId) {
         for (int i = 0; i < this.getComponentCount(); i++) {
             if (((EDACCSolverConfigPanelSolver) this.getComponent(i)).getSolver().getId() == solverId) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Prevents from updating the GUI until endUpdate() is called.
      */
     public void beginUpdate() {
         this.update = true;
     }
 
     /**
      * GUI will be updated after every single change.
      */
     public void endUpdate() {
         this.update = false;
         doRepaint();
     }
 
     /**
      * Checks for unsaved solver configurations
      * @return true, if and only if there is a unsaved solver configuration, false otherwise
      */
     public boolean isModified() {
         // checks for deleted entries
         if (SolverConfigurationDAO.isModified()) {
             return true;
         }
         // checks for changed entries
         for (Component comp : this.getComponents()) {
             if (comp instanceof EDACCSolverConfigPanelSolver) {
                 EDACCSolverConfigPanelSolver entry = (EDACCSolverConfigPanelSolver) comp;
                 if (entry.isModified()) {
                     return true;
                 }
             }
         }
         // ... unchanged
         return false;
     }
 
     /**
      * Returns an <code>ArrayList</code> of all modified solver configurations and solver configurations for which the seed group has been changed. <br><br>
      * <b>Note:</b> Here a modified solver configuration doesn't mean a new/deleted solver configuration.
      * @return ArrayList of all modified solver configurations
      */
     public ArrayList<SolverConfiguration> getModifiedSolverConfigurations() {
         ArrayList<SolverConfiguration> res = new ArrayList<SolverConfiguration>();
         for (Component comp : this.getComponents()) {
             if (comp instanceof EDACCSolverConfigPanelSolver) {
                 EDACCSolverConfigPanelSolver entry = (EDACCSolverConfigPanelSolver) comp;
                 res.addAll(entry.getModifiedSolverConfigurations());
             }
         }
         return res;
     }
 
     @Override
     public void removeAll() {
         while (this.getComponents().length > 0) {
             EDACCSolverConfigPanelSolver entry = (EDACCSolverConfigPanelSolver) this.getComponent(0);
             entry.removeAll();
             this.remove(entry);
         }
         doRepaint();
     }
 
     public SolverTableModel getSolTableModel() {
         return parent.solTableModel;
     }
 
     public ArrayList<EDACCSolverConfigPanelSolver> getAllSolverConfigSolverPanels() {
         ArrayList<EDACCSolverConfigPanelSolver> res = new ArrayList<EDACCSolverConfigPanelSolver>();
         for (Component c : this.getComponents()) {
             if (c instanceof EDACCSolverConfigPanelSolver) {
                 res.add((EDACCSolverConfigPanelSolver) c);
             }
         }
         return res;
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         setName("Form"); // NOI18N
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 400, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 300, Short.MAX_VALUE)
         );
     }// </editor-fold>//GEN-END:initComponents
     // Variables declaration - do not modify//GEN-BEGIN:variables
     // End of variables declaration//GEN-END:variables
 }
