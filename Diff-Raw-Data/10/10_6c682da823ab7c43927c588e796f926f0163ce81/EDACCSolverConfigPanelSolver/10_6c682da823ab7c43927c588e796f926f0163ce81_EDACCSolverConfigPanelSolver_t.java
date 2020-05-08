 /*
  * EDACCSolverConfigPanelSolver.java
  *
  * Created on 31.01.2011, 14:37:53
  */
 package edacc;
 
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
 import javax.swing.border.TitledBorder;
 
 /**
  *
  * @author simon
  */
 public class EDACCSolverConfigPanelSolver extends javax.swing.JPanel {
 
     private Solver solver;
     private EDACCSolverConfigPanel parent;
     private GridBagConstraints gridBagConstraints;
     private GridBagLayout layout;
 
     private EDACCSolverConfigPanelSolver(Solver solver) {
         initComponents();
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new Insets(5, 5, 5, 5);
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
 
         layout = new GridBagLayout();
         this.setLayout(layout);
         this.solver = solver;
         ((TitledBorder) this.getBorder()).setTitle(solver.toString());
     }
 
     /** Creates new form EDACCSolverConfigPanelSolver */
     @SuppressWarnings("LeakingThisInConstructor")
     public EDACCSolverConfigPanelSolver(Solver solver, EDACCSolverConfigPanel parent) throws SQLException {
         this(solver);
         this.parent = parent;
         EDACCSolverConfigEntry entry = new EDACCSolverConfigEntry(solver, 1);
         entry.setParent(this);
         this.add(entry, 0);
         parent.getSolTableModel().setSolverSelected(solver.getId(), true);
         setGridBagConstraints();
         doRepaint();
     }
 
     public EDACCSolverConfigPanelSolver(SolverConfiguration solverConfiguration, EDACCSolverConfigPanel parent, boolean useSolverConfiguration) throws SQLException {
         this(SolverDAO.getById(solverConfiguration.getSolverBinary().getIdSolver()));
         this.parent = parent;
         addSolverConfiguration(solverConfiguration, useSolverConfiguration);
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
 
     private void doRepaint() {
         this.revalidate();
        this.invalidate();
        this.repaint();
         parent.setTitles();
     }
 
     /**
      * Generates a new EDACCSolverConfigEntry for a solver configuration.
      * @param solverConfiguration
      * @throws SQLException
      */
     public void addSolverConfiguration(SolverConfiguration solverConfiguration) throws SQLException {
         addSolverConfiguration(solverConfiguration, true);
     }
 
     /**
      * Generates a new EDACCSolverConfigEntry for a solver configuration.
      * @param solverConfiguration
      * @param useSolverConfiguration will create a new solver configuration when saving this entry if the flag is false
      * @throws SQLException
      */
     public final void addSolverConfiguration(SolverConfiguration solverConfiguration, boolean useSolverConfiguration) throws SQLException {
         EDACCSolverConfigEntry entry = new EDACCSolverConfigEntry(solverConfiguration);
         entry.setParent(this);
         if (!useSolverConfiguration) {
             entry.solver = SolverDAO.getById(entry.solverConfiguration.getSolverBinary().getIdSolver());
             entry.solverConfiguration = null;
             entry.solverConfigEntryTableModel.setParameterInstances(null);
         }
         this.add(entry, getIndex(solverConfiguration.getSolverBinary().getIdSolver()));
         parent.getSolTableModel().setSolverSelected(solverConfiguration.getSolverBinary().getIdSolver(), true);
         setGridBagConstraints();
         doRepaint();
     }
 
     /**
      * Copies an EDACCSolverConfigEntry and adds it after that entry.
      * @param entry
      * @throws SQLException
      */
     public void replicateEntry(EDACCSolverConfigEntry entry) throws SQLException {
         EDACCSolverConfigEntry repl = new EDACCSolverConfigEntry(SolverDAO.getById(entry.getSolverId()), this.getComponentCount()+1);
         repl.setParent(this);
         repl.assign(entry);
         int pos = 0;
         for (int i = 0; i < this.getComponentCount(); i++) {
             if (this.getComponent(i) == entry) {
                 pos = i;
                 break;
             }
         }
         this.add(repl, pos + 1);
         setGridBagConstraints();
         doRepaint();
     }
 
     public void addEntryAfterEntry(EDACCSolverConfigEntry toAdd, EDACCSolverConfigEntry afterEntry) {
         int pos = 0;
         for (int i = 0; i < this.getComponentCount(); i++) {
             if (this.getComponent(i) == afterEntry) {
                 pos = i;
                 break;
             }
         }
         this.add(toAdd, pos+1);
         setGridBagConstraints();
         doRepaint();
     }
 
     /**
      * Removes an EDACCSolverConfigEntry. If the solver configuration exists in the database
      * it is marked as deleted.
      * @param entry
      */
     public void removeEntry(EDACCSolverConfigEntry entry) {
         if (entry.getSolverConfiguration() != null) {
             SolverConfigurationDAO.removeSolverConfiguration(entry.getSolverConfiguration());
         }
         this.remove(entry);
 
         // if this was the last solver configuration for the corresponding solver
         // we will deselect the solver in the solvers table
         boolean lastSolver = true;
         for (Component c : this.getComponents()) {
             if (((EDACCSolverConfigEntry) c).getSolverId() == entry.getSolverId()) {
                 lastSolver = false;
                 break;
             }
         }
         if (lastSolver) {
             parent.getSolTableModel().setSolverSelected(entry.getSolverId(), false);
             parent.remove(this);
         }
         doRepaint();
        // repaint parent
        parent.invalidate();
        parent.revalidate();
        parent.repaint();
     }
 
     public void setTitles() {
         parent.setTitles();
     }
 
     public Solver getSolver() {
         return solver;
     }
 
     private int getIndex(int solverId) {
         int solverIndex = 0;
         int solverOrder[] = new int[parent.getSolTableModel().getRowCount()];
         for (int i = 0; i < parent.getSolTableModel().getRowCount(); i++) {
             solverOrder[i] = parent.getSolTableModel().getSolver(i).getId();
             if (solverId == parent.getSolTableModel().getSolver(i).getId()) {
                 solverIndex = i;
             }
         }
         int currentIndex = 0;
         for (int i = 0; i < this.getComponents().length; i++) {
             EDACCSolverConfigEntry entry = (EDACCSolverConfigEntry) this.getComponents()[i];
             for (int j = currentIndex; j < solverOrder.length; j++) {
                 if (entry.getSolverId() == solverOrder[j]) {
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
      * Checks for unsaved solver configurations
      * @return true, if and only if there is a unsaved solver configuration, false otherwise
      */
     public boolean isModified() {
         // checks for deleted entries
         if (SolverConfigurationDAO.isModified()) {
             return true;
         }
         int idx = 0;
         // checks for changed entries
         for (Component comp : this.getComponents()) {
             if (comp instanceof EDACCSolverConfigEntry) {
                 EDACCSolverConfigEntry entry = (EDACCSolverConfigEntry) comp;
                 if (entry.isModified(idx++)) {
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
             if (comp instanceof EDACCSolverConfigEntry) {
                 EDACCSolverConfigEntry entry = (EDACCSolverConfigEntry) comp;
                 if (entry.isModified(-1) && entry.getSolverConfiguration() != null && !SolverConfigurationDAO.isDeleted(entry.getSolverConfiguration())) {
                     res.add(entry.getSolverConfiguration());
                 }
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
 
         setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         setName("Form"); // NOI18N
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 388, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 287, Short.MAX_VALUE)
         );
     }// </editor-fold>//GEN-END:initComponents
 
     @Override
     public void removeAll() {
         while (this.getComponents().length > 0) {
             removeEntry((EDACCSolverConfigEntry) this.getComponent(0));
         }
     }
 
     public ArrayList<EDACCSolverConfigEntry> getAllSolverConfigEntries() {
         ArrayList<EDACCSolverConfigEntry> res = new ArrayList<EDACCSolverConfigEntry>();
         for (Component c : this.getComponents()) {
             if (c instanceof EDACCSolverConfigEntry) {
                 res.add((EDACCSolverConfigEntry)c);
             }
         }
         return res;
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     // End of variables declaration//GEN-END:variables
 }
