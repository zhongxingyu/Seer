 /*
  * EDACCExperimentModeImportSolverConfigs.java
  *
  * Created on 05.04.2011, 15:56:17
  */
 package edacc;
 
 import edacc.experiment.ExperimentController;
 import edacc.experiment.ExperimentTableModel;
 import edacc.experiment.SolverConfigurationTableModel;
 import edacc.experiment.SolverConfigurationTableRowFilter;
 import edacc.experiment.SolverTableModel;
 import edacc.experiment.Util;
 import edacc.model.Experiment;
 import edacc.model.ParameterInstanceDAO;
 import edacc.model.Solver;
 import edacc.model.SolverBinaries;
 import edacc.model.SolverConfiguration;
 import edacc.model.SolverConfigurationDAO;
 import edacc.model.SolverDAO;
 import java.awt.Color;
 import java.awt.Component;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableRowSorter;
 
 /**
  *
  * @author simon
  */
 public class EDACCExperimentModeImportSolverConfigs extends javax.swing.JDialog {
 
     private ExperimentTableModel experimentTableModel;
     private SolverConfigurationTableModel solverConfigTableModelByExperiments;
     private SolverConfigurationTableModel solverConfigTableModelBySolvers;
     private ArrayList<SolverConfiguration> selectedSolverConfigs;
     private boolean cancelled;
     private SolverTableModel solTableModel;
     private EDACCFilter existingSolverConfigFilter;
     private SolverConfigurationTableRowFilter solverConfigurationTableRowFilterByExperiments;
     private SolverConfigurationTableRowFilter solverConfigurationTableRowFilterBySolvers;
     private EDACCFilter solverFilter;
 
     /** Creates new form EDACCExperimentModeImportSolverConfigs */
     public EDACCExperimentModeImportSolverConfigs(java.awt.Frame parent, boolean modal, ExperimentController expController) {
         super(parent, modal);
 
         ArrayList<SolverConfiguration> scs = null;
         ArrayList<Solver> solvers = new ArrayList<Solver>();
         try {
             scs = SolverConfigurationDAO.getAll();
             ParameterInstanceDAO.cacheParameterInstances(scs);
             LinkedList<Solver> tmp = SolverDAO.getAll();
             solvers.addAll(tmp);
         } catch (SQLException ex) {
         }
 
         cancelled = true;
         selectedSolverConfigs = new ArrayList<SolverConfiguration>();
         experimentTableModel = new ExperimentTableModel(true);
         solverConfigTableModelByExperiments = new SolverConfigurationTableModel();
         solverConfigTableModelBySolvers = new SolverConfigurationTableModel();
         experimentTableModel.setExperiments(expController.getExperiments());
 
         initComponents();
 
         tblExperiments.setDefaultRenderer(char.class, new DefaultTableCellRenderer() {
 
             @Override
             public Component getTableCellRendererComponent(JTable table, Object value,
                     boolean isSelected, boolean hasFocus, int row, int column) {
                 JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                 lbl.setHorizontalAlignment(JLabel.CENTER);
                 return lbl;
             }
         });
 
         tblExperiments.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 solverConfigurationTableRowFilterByExperiments.clearIncludedExperiments();
                 for (int row : tblExperiments.getSelectedRows()) {
                     Experiment exp = experimentTableModel.getExperimentAt(tblExperiments.convertRowIndexToModel(row));
                     solverConfigurationTableRowFilterByExperiments.includeExperiment(exp.getId());
                 }
                 solverConfigTableModelByExperiments.fireTableDataChanged();
             }
         });
 
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                existingSolverConfigFilter = new EDACCFilter(EDACCApp.getApplication().getMainFrame(), true, tblSolverConfigsByExperiments, true);
                //  solverFilter = new EDACCFilter(EDACCApp.getApplication().getMainFrame(), true, tableSolvers, true);
             }
         });
         TableRowSorter tableSolverConfigurationsRowSorterByExperiments = new TableRowSorter<SolverConfigurationTableModel>(solverConfigTableModelByExperiments);
         solverConfigurationTableRowFilterByExperiments = new SolverConfigurationTableRowFilter();
         tableSolverConfigurationsRowSorterByExperiments.setRowFilter(solverConfigurationTableRowFilterByExperiments);
         tblSolverConfigsByExperiments.setRowSorter(tableSolverConfigurationsRowSorterByExperiments);
         tblSolverConfigsByExperiments.setModel(solverConfigTableModelByExperiments);
         Util.addSpaceSelection(tblSolverConfigsByExperiments, SolverConfigurationTableModel.COL_SEL);
         solverConfigTableModelByExperiments.setSolverConfigurations(scs);
 
         solTableModel = new SolverTableModel();
         tableSolvers.setModel(solTableModel);
         tableSolvers.removeColumn(tableSolvers.getColumnModel().getColumn(SolverTableModel.COL_SELECTED));
         solTableModel.setSolvers(solvers);
         TableRowSorter tableSolverConfigurationsRowSorterBySolvers = new TableRowSorter<SolverConfigurationTableModel>(solverConfigTableModelBySolvers);
         solverConfigurationTableRowFilterBySolvers = new SolverConfigurationTableRowFilter();
         tableSolverConfigurationsRowSorterBySolvers.setRowFilter(solverConfigurationTableRowFilterBySolvers);
         tblSolverConfigsBySolvers.setRowSorter(tableSolverConfigurationsRowSorterBySolvers);
         tblSolverConfigsBySolvers.setModel(solverConfigTableModelBySolvers);
         Util.addSpaceSelection(tblSolverConfigsBySolvers, SolverConfigurationTableModel.COL_SEL);
         solverConfigTableModelBySolvers.setSolverConfigurations(scs);
 
         tableSolvers.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 solverConfigurationTableRowFilterBySolvers.clearSolverBinaryIds();
                 for (int rowView : tableSolvers.getSelectedRows()) {
                     int rowModel = tableSolvers.convertRowIndexToModel(rowView);
                     for (SolverBinaries sb : solTableModel.getSolver(rowModel).getSolverBinaries()) {
                         solverConfigurationTableRowFilterBySolvers.addSolverBinaryId(sb.getId());
                     }
 
                 }
                 solverConfigTableModelBySolvers.fireTableDataChanged();
             }
         });
 
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jTabbedPane = new javax.swing.JTabbedPane();
         jPanel3 = new javax.swing.JPanel();
         jSplitPane1 = new javax.swing.JSplitPane();
         jPanel1 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         tblExperiments = tblExperiments = new JTableTooltipInformation();
         jPanel2 = new javax.swing.JPanel();
         jScrollPane2 = new javax.swing.JScrollPane();
         tblSolverConfigsByExperiments = tblSolverConfigsByExperiments = new JTableTooltipInformation();
         jPanel4 = new javax.swing.JPanel();
         jSplitPane2 = new javax.swing.JSplitPane();
         jPanel8 = new javax.swing.JPanel();
         jScrollPane3 = new javax.swing.JScrollPane();
         tableSolvers = new JTableTooltipInformation();
         btnSelectAllSolvers = new javax.swing.JButton();
         btnDeselectAllSolvers = new javax.swing.JButton();
         btnReverseSolverSelection = new javax.swing.JButton();
         btnSolverTabFilterSolvers = new javax.swing.JButton();
         lblSolverFilterStatus = new javax.swing.JLabel();
         jPanel9 = new javax.swing.JPanel();
         jScrollPane8 = new javax.swing.JScrollPane();
         tblSolverConfigsBySolvers = new JTableTooltipInformation();
         btnSelectAllSolverConfigs = new javax.swing.JButton();
         btnDeselectAllSolverConfigs = new javax.swing.JButton();
         btnInvertSolverConfigSelection = new javax.swing.JButton();
         btnSolverTabFilterExistingSolverConfigs = new javax.swing.JButton();
         lblExistingSolverConfigFilterStatus = new javax.swing.JLabel();
         btnImport = new javax.swing.JButton();
         btnCancel = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(edacc.EDACCApp.class).getContext().getResourceMap(EDACCExperimentModeImportSolverConfigs.class);
         setTitle(resourceMap.getString("Form.title")); // NOI18N
         setName("Form"); // NOI18N
 
         jTabbedPane.setName("jTabbedPane"); // NOI18N
 
         jPanel3.setName("jPanel3"); // NOI18N
 
         jSplitPane1.setDividerLocation(400);
         jSplitPane1.setName("jSplitPane1"); // NOI18N
 
         jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
         jPanel1.setName("jPanel1"); // NOI18N
 
         jScrollPane1.setName("jScrollPane1"); // NOI18N
 
         tblExperiments.setAutoCreateRowSorter(true);
         tblExperiments.setModel(experimentTableModel);
         tblExperiments.setName("tblExperiments"); // NOI18N
         tblExperiments.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         jScrollPane1.setViewportView(tblExperiments);
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
         );
 
         jSplitPane1.setLeftComponent(jPanel1);
 
         jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
         jPanel2.setName("jPanel2"); // NOI18N
 
         jScrollPane2.setName("jScrollPane2"); // NOI18N
 
         tblSolverConfigsByExperiments.setAutoCreateRowSorter(true);
         tblSolverConfigsByExperiments.setModel(solverConfigTableModelByExperiments);
         tblSolverConfigsByExperiments.setName("tblSolverConfigsByExperiments"); // NOI18N
         jScrollPane2.setViewportView(tblSolverConfigsByExperiments);
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 871, Short.MAX_VALUE)
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
         );
 
         jSplitPane1.setRightComponent(jPanel2);
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1289, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPane.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N
 
         jPanel4.setName("jPanel4"); // NOI18N
 
         jSplitPane2.setName("jSplitPane2"); // NOI18N
 
         jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel8.border.title"))); // NOI18N
         jPanel8.setName("jPanel8"); // NOI18N
 
         jScrollPane3.setToolTipText(resourceMap.getString("jScrollPane3.toolTipText")); // NOI18N
         jScrollPane3.setName("jScrollPane3"); // NOI18N
 
         tableSolvers.setAutoCreateRowSorter(true);
         tableSolvers.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         tableSolvers.setName("tableSolvers"); // NOI18N
         jScrollPane3.setViewportView(tableSolvers);
 
         btnSelectAllSolvers.setText(resourceMap.getString("btnSelectAllSolvers.text")); // NOI18N
         btnSelectAllSolvers.setToolTipText(resourceMap.getString("btnSelectAllSolvers.toolTipText")); // NOI18N
         btnSelectAllSolvers.setName("btnSelectAllSolvers"); // NOI18N
         btnSelectAllSolvers.setPreferredSize(new java.awt.Dimension(110, 23));
         btnSelectAllSolvers.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSelectAllSolversActionPerformed(evt);
             }
         });
 
         btnDeselectAllSolvers.setText(resourceMap.getString("btnDeselectAllSolvers.text")); // NOI18N
         btnDeselectAllSolvers.setToolTipText(resourceMap.getString("btnDeselectAllSolvers.toolTipText")); // NOI18N
         btnDeselectAllSolvers.setName("btnDeselectAllSolvers"); // NOI18N
         btnDeselectAllSolvers.setPreferredSize(new java.awt.Dimension(110, 23));
         btnDeselectAllSolvers.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnDeselectAllSolversActionPerformed(evt);
             }
         });
 
         btnReverseSolverSelection.setText(resourceMap.getString("btnReverseSolverSelection.text")); // NOI18N
         btnReverseSolverSelection.setToolTipText(resourceMap.getString("btnReverseSolverSelection.toolTipText")); // NOI18N
         btnReverseSolverSelection.setName("btnReverseSolverSelection"); // NOI18N
         btnReverseSolverSelection.setPreferredSize(new java.awt.Dimension(110, 23));
         btnReverseSolverSelection.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnReverseSolverSelectionActionPerformed(evt);
             }
         });
 
         btnSolverTabFilterSolvers.setText(resourceMap.getString("btnSolverTabFilterSolvers.text")); // NOI18N
         btnSolverTabFilterSolvers.setName("btnSolverTabFilterSolvers"); // NOI18N
         btnSolverTabFilterSolvers.setPreferredSize(new java.awt.Dimension(110, 23));
         btnSolverTabFilterSolvers.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSolverTabFilterSolversActionPerformed(evt);
             }
         });
 
         lblSolverFilterStatus.setName("lblSolverFilterStatus"); // NOI18N
 
         javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
         jPanel8.setLayout(jPanel8Layout);
         jPanel8Layout.setHorizontalGroup(
             jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel8Layout.createSequentialGroup()
                 .addComponent(btnSelectAllSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnDeselectAllSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnReverseSolverSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(6, 6, 6)
                 .addComponent(btnSolverTabFilterSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(237, Short.MAX_VALUE))
             .addComponent(lblSolverFilterStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 695, Short.MAX_VALUE)
             .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 695, Short.MAX_VALUE)
         );
         jPanel8Layout.setVerticalGroup(
             jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                 .addComponent(lblSolverFilterStatus)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnSelectAllSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnDeselectAllSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnReverseSolverSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnSolverTabFilterSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
         );
 
         jSplitPane2.setLeftComponent(jPanel8);
 
         jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel9.border.title"))); // NOI18N
         jPanel9.setName("jPanel9"); // NOI18N
 
         jScrollPane8.setName("jScrollPane8"); // NOI18N
 
         tblSolverConfigsBySolvers.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         tblSolverConfigsBySolvers.setName("tblSolverConfigsBySolvers"); // NOI18N
         jScrollPane8.setViewportView(tblSolverConfigsBySolvers);
 
         btnSelectAllSolverConfigs.setText(resourceMap.getString("btnSelectAllSolverConfigs.text")); // NOI18N
         btnSelectAllSolverConfigs.setName("btnSelectAllSolverConfigs"); // NOI18N
         btnSelectAllSolverConfigs.setPreferredSize(new java.awt.Dimension(110, 23));
         btnSelectAllSolverConfigs.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSelectAllSolverConfigsActionPerformed(evt);
             }
         });
 
         btnDeselectAllSolverConfigs.setText(resourceMap.getString("btnDeselectAllSolverConfigs.text")); // NOI18N
         btnDeselectAllSolverConfigs.setName("btnDeselectAllSolverConfigs"); // NOI18N
         btnDeselectAllSolverConfigs.setPreferredSize(new java.awt.Dimension(110, 23));
         btnDeselectAllSolverConfigs.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnDeselectAllSolverConfigsActionPerformed(evt);
             }
         });
 
         btnInvertSolverConfigSelection.setText(resourceMap.getString("btnInvertSolverConfigSelection.text")); // NOI18N
         btnInvertSolverConfigSelection.setName("btnInvertSolverConfigSelection"); // NOI18N
         btnInvertSolverConfigSelection.setPreferredSize(new java.awt.Dimension(110, 23));
         btnInvertSolverConfigSelection.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnInvertSolverConfigSelectionActionPerformed(evt);
             }
         });
 
         btnSolverTabFilterExistingSolverConfigs.setText(resourceMap.getString("btnSolverTabFilterExistingSolverConfigs.text")); // NOI18N
         btnSolverTabFilterExistingSolverConfigs.setName("btnSolverTabFilterExistingSolverConfigs"); // NOI18N
         btnSolverTabFilterExistingSolverConfigs.setPreferredSize(new java.awt.Dimension(110, 23));
         btnSolverTabFilterExistingSolverConfigs.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSolverTabFilterExistingSolverConfigsActionPerformed(evt);
             }
         });
 
         lblExistingSolverConfigFilterStatus.setName("lblExistingSolverConfigFilterStatus"); // NOI18N
 
         javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
         jPanel9.setLayout(jPanel9Layout);
         jPanel9Layout.setHorizontalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel9Layout.createSequentialGroup()
                 .addComponent(btnSelectAllSolverConfigs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnDeselectAllSolverConfigs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnInvertSolverConfigSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(6, 6, 6)
                 .addComponent(btnSolverTabFilterExistingSolverConfigs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(125, Short.MAX_VALUE))
             .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
             .addComponent(lblExistingSolverConfigFilterStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
         );
         jPanel9Layout.setVerticalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                 .addComponent(lblExistingSolverConfigFilterStatus)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnSelectAllSolverConfigs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnDeselectAllSolverConfigs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnInvertSolverConfigSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnSolverTabFilterExistingSolverConfigs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
         );
 
         jSplitPane2.setRightComponent(jPanel9);
 
         javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1309, Short.MAX_VALUE)
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
         );
 
         jTabbedPane.addTab(resourceMap.getString("jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N
 
         btnImport.setText(resourceMap.getString("btnImport.text")); // NOI18N
         btnImport.setName("btnImport"); // NOI18N
         btnImport.setPreferredSize(new java.awt.Dimension(65, 25));
         btnImport.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnImportActionPerformed(evt);
             }
         });
 
         btnCancel.setText(resourceMap.getString("btnCancel.text")); // NOI18N
         btnCancel.setName("btnCancel"); // NOI18N
         btnCancel.setPreferredSize(new java.awt.Dimension(65, 25));
         btnCancel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCancelActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap(1168, Short.MAX_VALUE)
                 .addComponent(btnImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
             .addComponent(jTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1314, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addComponent(jTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
         this.setVisible(false);
     }//GEN-LAST:event_btnCancelActionPerformed
 
     private void btnImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportActionPerformed
         cancelled = false;
         if (jTabbedPane.getSelectedIndex() == 0) {
             selectedSolverConfigs = solverConfigTableModelByExperiments.getSelectedSolverConfigurations();
         } else {
             selectedSolverConfigs = solverConfigTableModelBySolvers.getSelectedSolverConfigurations();
         }
         this.setVisible(false);
     }//GEN-LAST:event_btnImportActionPerformed
 
     private void btnSelectAllSolverConfigsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectAllSolverConfigsActionPerformed
         for (int row = 0; row < tblSolverConfigsBySolvers.getRowCount(); row++) {
             solverConfigTableModelBySolvers.setValueAt(true, tblSolverConfigsBySolvers.convertRowIndexToModel(row), SolverConfigurationTableModel.COL_SEL);
         }
         solverConfigTableModelBySolvers.fireTableDataChanged();
 	}//GEN-LAST:event_btnSelectAllSolverConfigsActionPerformed
 
     private void btnDeselectAllSolverConfigsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeselectAllSolverConfigsActionPerformed
         for (int row = 0; row < tblSolverConfigsBySolvers.getRowCount(); row++) {
             solverConfigTableModelBySolvers.setValueAt(false, tblSolverConfigsBySolvers.convertRowIndexToModel(row), SolverConfigurationTableModel.COL_SEL);
         }
         solverConfigTableModelBySolvers.fireTableDataChanged();
 	}//GEN-LAST:event_btnDeselectAllSolverConfigsActionPerformed
 
     private void btnInvertSolverConfigSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInvertSolverConfigSelectionActionPerformed
         for (int row = 0; row < tblSolverConfigsBySolvers.getRowCount(); row++) {
             solverConfigTableModelBySolvers.setValueAt(!(Boolean) solverConfigTableModelBySolvers.getValueAt(tblSolverConfigsBySolvers.convertRowIndexToModel(row), SolverConfigurationTableModel.COL_SEL), tblSolverConfigsBySolvers.convertRowIndexToModel(row), SolverConfigurationTableModel.COL_SEL);
         }
         solverConfigTableModelBySolvers.fireTableDataChanged();
 	}//GEN-LAST:event_btnInvertSolverConfigSelectionActionPerformed
 
     private void btnSolverTabFilterExistingSolverConfigsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSolverTabFilterExistingSolverConfigsActionPerformed
         EDACCApp.getApplication().show(existingSolverConfigFilter);
         solverConfigTableModelBySolvers.fireTableDataChanged();
         updateExistingSolverConfigFilterStatus();
 	}//GEN-LAST:event_btnSolverTabFilterExistingSolverConfigsActionPerformed
 
     private void btnSelectAllSolversActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectAllSolversActionPerformed
         for (int i = 0; i < solTableModel.getRowCount(); i++) {
             solTableModel.setSelected(i, true);
         }
 	}//GEN-LAST:event_btnSelectAllSolversActionPerformed
 
     private void btnDeselectAllSolversActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeselectAllSolversActionPerformed
         for (int i = 0; i < solTableModel.getRowCount(); i++) {
             solTableModel.setSelected(i, false);
         }
 	}//GEN-LAST:event_btnDeselectAllSolversActionPerformed
 
     private void btnReverseSolverSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReverseSolverSelectionActionPerformed
 
         for (int i = 0; i < solTableModel.getRowCount(); i++) {
             solTableModel.setSelected(i, !solTableModel.isSelected(i));
         }
 	}//GEN-LAST:event_btnReverseSolverSelectionActionPerformed
 
     private void btnSolverTabFilterSolversActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSolverTabFilterSolversActionPerformed
 
         EDACCApp.getApplication().show(solverFilter);
         solTableModel.fireTableDataChanged();
         updateSolverFilterStatus();
 	}//GEN-LAST:event_btnSolverTabFilterSolversActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnCancel;
     private javax.swing.JButton btnDeselectAllSolverConfigs;
     private javax.swing.JButton btnDeselectAllSolvers;
     private javax.swing.JButton btnImport;
     private javax.swing.JButton btnInvertSolverConfigSelection;
     private javax.swing.JButton btnReverseSolverSelection;
     private javax.swing.JButton btnSelectAllSolverConfigs;
     private javax.swing.JButton btnSelectAllSolvers;
     private javax.swing.JButton btnSolverTabFilterExistingSolverConfigs;
     private javax.swing.JButton btnSolverTabFilterSolvers;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel8;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane8;
     private javax.swing.JSplitPane jSplitPane1;
     private javax.swing.JSplitPane jSplitPane2;
     private javax.swing.JTabbedPane jTabbedPane;
     private javax.swing.JLabel lblExistingSolverConfigFilterStatus;
     private javax.swing.JLabel lblSolverFilterStatus;
     private javax.swing.JTable tableSolvers;
     private javax.swing.JTable tblExperiments;
     private javax.swing.JTable tblSolverConfigsByExperiments;
     private javax.swing.JTable tblSolverConfigsBySolvers;
     // End of variables declaration//GEN-END:variables
 
     public boolean isCancelled() {
         return cancelled;
     }
 
     public ArrayList<SolverConfiguration> getSelectedSolverConfigurations() {
         return selectedSolverConfigs;
     }
 
     public void updateExistingSolverConfigFilterStatus() {
         lblExistingSolverConfigFilterStatus.setForeground(Color.red);
         String status = "";
         if (existingSolverConfigFilter.hasFiltersApplied()) {
            status += "This list of solver configurations has filters applied to it. Use the filter button below to modify. Showing " + tblSolverConfigsByExperiments.getRowCount() + " solver configurations.";
         }
         lblExistingSolverConfigFilterStatus.setText(status);
     }
 
     public void updateSolverFilterStatus() {
         lblSolverFilterStatus.setForeground(Color.red);
         String status = "";
         if (solverFilter.hasFiltersApplied()) {
             status += "This list of solvers has filters applied to it. Use the filter button below to modify. Showing " + tableSolvers.getRowCount() + " solvers.";
         }
         lblSolverFilterStatus.setText(status);
     }
 }
