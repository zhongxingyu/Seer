 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * EDACCManageDBMode.java
  * @author
  * Created on 03.01.2010, 16:02:23
  */
 package edacc;
 
 import edacc.events.TaskEvents;
 import edacc.manageDB.*;
 import edacc.manageDB.InstanceException;
 import edacc.model.Instance;
 import edacc.model.InstanceIsInExperimentException;
 import edacc.model.InstanceNotInDBException;
 import edacc.model.InstanceClass;
 import edacc.model.InstanceDAO;
 import edacc.model.InstanceSourceClassHasInstance;
 import edacc.model.MD5CheckFailedException;
 import edacc.model.NoConnectionToDBException;
 import edacc.model.Parameter;
 import edacc.model.Solver;
 import edacc.model.SolverBinaries;
 import edacc.model.TaskCancelledException;
 import edacc.model.Tasks;
 import java.awt.*;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.sound.midi.ControllerEventListener;
 import javax.swing.ImageIcon;
 import javax.swing.InputVerifier;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.table.*;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.RowSorter.SortKey;
 import javax.swing.SwingUtilities;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.TableRowSorter;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 import org.jdesktop.application.Action;
 
 /**
  *
  * @author rretz
  */
 public class EDACCManageDBMode extends javax.swing.JPanel implements TaskEvents {
 
     public boolean unsavedChanges;
     public ManageDBInstances manageDBInstances;
     public InstanceTableModel instanceTableModel;
     public DefaultTreeModel instanceClassTreeModel;
     public TableRowSorter<InstanceTableModel> sorter;
     private ManageDBSolvers manageDBSolvers;
     private SolverTableModel solverTableModel;
     private ManageDBParameters manageDBParameters;
     private ParameterTableModel parameterTableModel;
     public EDACCCreateEditInstanceClassDialog createInstanceClassDialog;
     public EDACCAddNewInstanceSelectClassDialog addInstanceDialog;
     public EDACCInstanceGeneratorUnifKCNF instanceGenKCNF;
     public EDACCInstanceFilter instanceFilter;
     private SolverBinariesTableModel solverBinariesTableModel;
 
     public EDACCManageDBMode() {
         initComponents();
 
         unsavedChanges = false;
 
         manageDBInstances = new ManageDBInstances(this, panelManageDBInstances,
                 jFileChooserManageDBInstance, jFileChooserManageDBExportInstance, tableInstances);
 
         // initialize instance table
         instanceTableModel = new InstanceTableModel();
         sorter = new TableRowSorter<InstanceTableModel>(instanceTableModel);
         tableInstances.setModel(instanceTableModel);
         tableInstances.setRowSorter(sorter);
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 instanceFilter = new EDACCInstanceFilter(EDACCApp.getApplication().getMainFrame(), true, tableInstances, true);
             }
         });
         tableInstances.getSelectionModel().addListSelectionListener(new InstanceTableSelectionListener(tableInstances, manageDBInstances));
         tableInstances.addMouseListener(new InstanceTableMouseListener(jPMInstanceTable));
 
 
         // initialize instance class table
         instanceClassTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode("test1"));
         jTreeInstanceClass.setModel(instanceClassTreeModel);
         jTreeInstanceClass.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
         jTreeInstanceClass.addTreeSelectionListener(new InstanceClassTreeSelectionListener(manageDBInstances, jTreeInstanceClass));
         jTreeInstanceClass.addMouseListener(new InstanceClassTreeMouseListener(jPMInstanceTreeInstanceClass));
 
 
 
         // initialize parameter table
         parameterTableModel = new ParameterTableModel();
         manageDBParameters = new ManageDBParameters(this, parameterTableModel);
         tableParameters.setModel(parameterTableModel);
         tableParameters.setRowSorter(new TableRowSorter<ParameterTableModel>(parameterTableModel));
 
         // initialize the solver binaries table
         solverBinariesTableModel = new SolverBinariesTableModel();
         tableSolverBinaries.setModel(solverBinariesTableModel);
 
         // initialize solver table
         solverTableModel = new SolverTableModel();
         manageDBSolvers = new ManageDBSolvers(this, solverTableModel, manageDBParameters, solverBinariesTableModel);
         tableSolver.setModel(solverTableModel);
         tableSolver.setRowSorter(new TableRowSorter<SolverTableModel>(solverTableModel));
 
         tableSolver.getSelectionModel().addListSelectionListener(new SolverTableSelectionListener(tableSolver, manageDBSolvers));
         tableParameters.getSelectionModel().addListSelectionListener(new ParameterTableSelectionListener(tableParameters, manageDBParameters));
         showSolverDetails(null);
         tableParameters.setDefaultRenderer(tableParameters.getColumnClass(2), new DefaultTableCellRenderer() {
 
             @Override
             public Component getTableCellRendererComponent(JTable table, Object value,
                     boolean isSelected, boolean hasFocus, int row, int column) {
                 JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                 lbl.setHorizontalAlignment(JLabel.CENTER);
                 return lbl;
             }
         });
 
 
 
         //TODO: FontMetrics verwenden!!!
 
         //tableParameters.getColumnModel().getColumn(0).setMaxWidth(metric.stringWidth(tableParameters.getModel().getColumnName(0))+10);
         //tableParameters.getColumnModel().getColumn(0).setMinWidth(metric.stringWidth(tableParameters.getModel().getColumnName(0))+5);
         /*tableParameters.getColumnModel().getColumn(3).setMaxWidth(50);
         tableInstanceClass.getColumnModel().getColumn(2).setMaxWidth(55);
         tableInstanceClass.getColumnModel().getColumn(3).setMaxWidth(55);
         tableInstanceClass.getColumnModel().getColumn(2).setMinWidth(40);
         tableInstanceClass.getColumnModel().getColumn(3).setMinWidth(40);
         this.jSplitPane2.setDividerLocation(-1);*/
     }
 
     public void initialize() throws NoConnectionToDBException, SQLException {
         manageDBSolvers.loadSolvers();
         manageDBParameters.loadParametersOfSolvers(solverTableModel.getSolvers());
         manageDBInstances.loadInstanceClasses();
         manageDBInstances.loadInstances();
         instanceTableModel.updateProperties();
         instanceTableModel.fireTableDataChanged();
         jTreeInstanceClass.updateUI();
         unsavedChanges = false;
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jFileChooserManageDBInstance = new javax.swing.JFileChooser();
         jFileChooserManageDBExportInstance = new javax.swing.JFileChooser();
         jPMInstanceTable = new javax.swing.JPopupMenu();
         jMIAddInstance = new javax.swing.JMenuItem();
         jMIRemoveInstance = new javax.swing.JMenuItem();
         jMIExportInstance = new javax.swing.JMenuItem();
         jPMInstanceTreeInstanceClass = new javax.swing.JPopupMenu();
         jMINewInstanceClass = new javax.swing.JMenuItem();
         jMIEditInstanceClass = new javax.swing.JMenuItem();
         jMIRemoveInstanceClass = new javax.swing.JMenuItem();
         jMIExportInstanceClass = new javax.swing.JMenuItem();
         manageDBPane = new javax.swing.JTabbedPane();
         panelManageDBSolver = new javax.swing.JPanel();
         jSplitPane2 = new javax.swing.JSplitPane();
         panelParametersOverall = new javax.swing.JPanel();
         panelParameters = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         tableParameters = new javax.swing.JTable();
         jPanel2 = new javax.swing.JPanel();
         jlParametersName = new javax.swing.JLabel();
         tfParametersName = new javax.swing.JTextField();
         jlParametersPrefix = new javax.swing.JLabel();
         tfParametersPrefix = new javax.swing.JTextField();
         jlParametersOrder = new javax.swing.JLabel();
         tfParametersOrder = new javax.swing.JTextField();
         jLabel1 = new javax.swing.JLabel();
         chkHasNoValue = new javax.swing.JCheckBox();
         lMandatory = new javax.swing.JLabel();
         chkMandatory = new javax.swing.JCheckBox();
         chkSpace = new javax.swing.JCheckBox();
         lSpace = new javax.swing.JLabel();
         jlParametersDefaultValue = new javax.swing.JLabel();
         tfParametersDefaultValue = new javax.swing.JTextField();
         jLabel2 = new javax.swing.JLabel();
         chkAttachToPrevious = new javax.swing.JCheckBox();
         panelParametersButons = new javax.swing.JPanel();
         btnParametersDelete = new javax.swing.JButton();
         btnParametersNew = new javax.swing.JButton();
         panelSolverOverall = new javax.swing.JPanel();
         panelSolver = new javax.swing.JPanel();
         jScrollPane2 = new javax.swing.JScrollPane();
         tableSolver = new javax.swing.JTable();
         jPanel1 = new javax.swing.JPanel();
         jlSolverName = new javax.swing.JLabel();
         tfSolverName = new javax.swing.JTextField();
         jlSolverDescription = new javax.swing.JLabel();
         jScrollPane3 = new javax.swing.JScrollPane();
         taSolverDescription = new javax.swing.JTextArea();
         jlSolverBinary = new javax.swing.JLabel();
         jlSolverCode = new javax.swing.JLabel();
         btnSolverAddCode = new javax.swing.JButton();
         tfSolverAuthors = new javax.swing.JTextField();
         tfSolverVersion = new javax.swing.JTextField();
         jlSolverAuthors = new javax.swing.JLabel();
         jlSolverVersion = new javax.swing.JLabel();
         btnSolverAddBinary = new javax.swing.JButton();
         jScrollPane4 = new javax.swing.JScrollPane();
         tableSolverBinaries = new javax.swing.JTable();
         btnSolverEditBinary = new javax.swing.JButton();
         btnSolverDeleteBinary = new javax.swing.JButton();
         panelSolverButtons = new javax.swing.JPanel();
         btnSolverDelete = new javax.swing.JButton();
         btnSolverNew = new javax.swing.JButton();
         jPanel3 = new javax.swing.JPanel();
         btnSolverSaveToDB = new javax.swing.JButton();
         btnSolverRefresh = new javax.swing.JButton();
         btnSolverExport = new javax.swing.JButton();
         panelManageDBInstances = new javax.swing.JPanel();
         jSplitPane1 = new javax.swing.JSplitPane();
         panelInstanceClass = new javax.swing.JPanel();
         panelButtonsInstanceClass = new javax.swing.JPanel();
         btnNewInstanceClass = new javax.swing.JButton();
         btnEditInstanceClass = new javax.swing.JButton();
         btnRemoveInstanceClass = new javax.swing.JButton();
         btnExportInstanceClass = new javax.swing.JButton();
         panelInstanceClassTable = new javax.swing.JScrollPane();
         jTreeInstanceClass = new javax.swing.JTree();
         panelInstance = new javax.swing.JPanel();
         panelInstanceTable = new javax.swing.JScrollPane();
         tableInstances = new javax.swing.JTable();
         panelButtonsInstances = new javax.swing.JPanel();
         btnAddInstances = new javax.swing.JButton();
         btnRemoveInstances = new javax.swing.JButton();
         btnExportInstances = new javax.swing.JButton();
         btnAddToClass = new javax.swing.JButton();
         btnRemoveFromClass = new javax.swing.JButton();
         btnAddInstances1 = new javax.swing.JButton();
         bComputeProperty = new javax.swing.JButton();
         btnGenerate = new javax.swing.JButton();
         lblFilterStatus = new javax.swing.JLabel();
         btnFilterInstances = new javax.swing.JButton();
         btnSelectInstanceColumns = new javax.swing.JButton();
 
         jFileChooserManageDBInstance.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
         jFileChooserManageDBInstance.setName("jFileChooserManageDBInstance"); // NOI18N
 
         jFileChooserManageDBExportInstance.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
         jFileChooserManageDBExportInstance.setName("jFileChooserManageDBExportInstance"); // NOI18N
 
         jPMInstanceTable.setBorderPainted(false);
         jPMInstanceTable.setComponentPopupMenu(jPMInstanceTable);
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(edacc.EDACCApp.class).getContext().getResourceMap(EDACCManageDBMode.class);
         jPMInstanceTable.setLabel(resourceMap.getString("jPMInstanceTable.label")); // NOI18N
         jPMInstanceTable.setMaximumSize(new java.awt.Dimension(10, 10));
         jPMInstanceTable.setMinimumSize(new java.awt.Dimension(10, 10));
         jPMInstanceTable.setName("jPMInstanceTable"); // NOI18N
 
         jMIAddInstance.setText(resourceMap.getString("jMIAddInstance.text")); // NOI18N
         jMIAddInstance.setName("jMIAddInstance"); // NOI18N
         jMIAddInstance.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMIAddInstanceActionPerformed(evt);
             }
         });
         jPMInstanceTable.add(jMIAddInstance);
 
         jMIRemoveInstance.setText(resourceMap.getString("jMIRemoveInstance.text")); // NOI18N
         jMIRemoveInstance.setName("jMIRemoveInstance"); // NOI18N
         jMIRemoveInstance.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMIRemoveInstanceActionPerformed(evt);
             }
         });
         jPMInstanceTable.add(jMIRemoveInstance);
 
         jMIExportInstance.setText(resourceMap.getString("jMIExportInstance.text")); // NOI18N
         jMIExportInstance.setName("jMIExportInstance"); // NOI18N
         jMIExportInstance.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMIExportInstanceActionPerformed(evt);
             }
         });
         jPMInstanceTable.add(jMIExportInstance);
 
         jPMInstanceTreeInstanceClass.setName("jPMInstanceTreeInstanceClass"); // NOI18N
 
         jMINewInstanceClass.setText(resourceMap.getString("jMINewInstanceClass.text")); // NOI18N
         jMINewInstanceClass.setName("jMINewInstanceClass"); // NOI18N
         jMINewInstanceClass.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMINewInstanceClassActionPerformed(evt);
             }
         });
         jPMInstanceTreeInstanceClass.add(jMINewInstanceClass);
 
         jMIEditInstanceClass.setText(resourceMap.getString("jMIEditInstanceClass.text")); // NOI18N
         jMIEditInstanceClass.setName("jMIEditInstanceClass"); // NOI18N
         jMIEditInstanceClass.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMIEditInstanceClassActionPerformed(evt);
             }
         });
         jPMInstanceTreeInstanceClass.add(jMIEditInstanceClass);
 
         jMIRemoveInstanceClass.setText(resourceMap.getString("jMIRemoveInstanceClass.text")); // NOI18N
         jMIRemoveInstanceClass.setName("jMIRemoveInstanceClass"); // NOI18N
         jMIRemoveInstanceClass.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMIRemoveInstanceClassActionPerformed(evt);
             }
         });
         jPMInstanceTreeInstanceClass.add(jMIRemoveInstanceClass);
 
         jMIExportInstanceClass.setText(resourceMap.getString("jMIExportInstanceClass.text")); // NOI18N
         jMIExportInstanceClass.setName("jMIExportInstanceClass"); // NOI18N
         jMIExportInstanceClass.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMIExportInstanceClassActionPerformed(evt);
             }
         });
         jPMInstanceTreeInstanceClass.add(jMIExportInstanceClass);
 
         setMinimumSize(new java.awt.Dimension(0, 0));
         setName("Form"); // NOI18N
         setPreferredSize(new java.awt.Dimension(500, 591));
 
         manageDBPane.setMinimumSize(new java.awt.Dimension(0, 0));
         manageDBPane.setName("manageDBPane"); // NOI18N
         manageDBPane.setRequestFocusEnabled(false);
 
         panelManageDBSolver.setName("panelManageDBSolver"); // NOI18N
         panelManageDBSolver.setPreferredSize(new java.awt.Dimension(0, 0));
 
         jSplitPane2.setDividerLocation(0.6);
         jSplitPane2.setResizeWeight(0.5);
         jSplitPane2.setName("jSplitPane2"); // NOI18N
 
         panelParametersOverall.setName("panelParametersOverall"); // NOI18N
         panelParametersOverall.setPreferredSize(new java.awt.Dimension(0, 0));
 
         panelParameters.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelParameters.border.title"))); // NOI18N
         panelParameters.setName("panelParameters"); // NOI18N
         panelParameters.setPreferredSize(new java.awt.Dimension(0, 0));
 
         jScrollPane1.setMinimumSize(new java.awt.Dimension(0, 0));
         jScrollPane1.setName("jScrollPane1"); // NOI18N
         jScrollPane1.setPreferredSize(new java.awt.Dimension(0, 0));
 
         tableParameters.setModel(new javax.swing.table.DefaultTableModel(
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
         tableParameters.setToolTipText(resourceMap.getString("tableParameters.toolTipText")); // NOI18N
         tableParameters.setName("tableParameters"); // NOI18N
         tableParameters.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jScrollPane1.setViewportView(tableParameters);
 
         jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
         jPanel2.setName("jPanel2"); // NOI18N
 
         jlParametersName.setText(resourceMap.getString("jlParametersName.text")); // NOI18N
         jlParametersName.setName("jlParametersName"); // NOI18N
 
         tfParametersName.setText(resourceMap.getString("tfParametersName.text")); // NOI18N
         tfParametersName.setToolTipText(resourceMap.getString("tfParametersName.toolTipText")); // NOI18N
         tfParametersName.setInputVerifier(new ParameterNameVerifier());
         tfParametersName.setName("tfParametersName"); // NOI18N
         tfParametersName.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 parameterChangedOnFocusLost(evt);
             }
         });
         tfParametersName.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 parameterChangedOnKeyReleased(evt);
             }
         });
 
         jlParametersPrefix.setText(resourceMap.getString("jlParametersPrefix.text")); // NOI18N
         jlParametersPrefix.setName("jlParametersPrefix"); // NOI18N
 
         tfParametersPrefix.setText(resourceMap.getString("tfParametersPrefix.text")); // NOI18N
         tfParametersPrefix.setToolTipText(resourceMap.getString("tfParametersPrefix.toolTipText")); // NOI18N
         tfParametersPrefix.setName("tfParametersPrefix"); // NOI18N
         tfParametersPrefix.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 parameterChangedOnFocusLost(evt);
             }
         });
         tfParametersPrefix.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 parameterChangedOnKeyReleased(evt);
             }
         });
 
         jlParametersOrder.setText(resourceMap.getString("jlParametersOrder.text")); // NOI18N
         jlParametersOrder.setName("jlParametersOrder"); // NOI18N
 
         tfParametersOrder.setText(resourceMap.getString("tfParametersOrder.text")); // NOI18N
         tfParametersOrder.setToolTipText(resourceMap.getString("tfParametersOrder.toolTipText")); // NOI18N
         tfParametersOrder.setName("tfParametersOrder"); // NOI18N
         tfParametersOrder.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 parameterChangedOnFocusLost(evt);
             }
         });
         tfParametersOrder.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 parameterChangedOnKeyReleased(evt);
             }
         });
 
         jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
         jLabel1.setToolTipText(resourceMap.getString("jLabel1.toolTipText")); // NOI18N
         jLabel1.setName("jLabel1"); // NOI18N
 
         chkHasNoValue.setText(resourceMap.getString("chkHasNoValue.text")); // NOI18N
         chkHasNoValue.setToolTipText(resourceMap.getString("chkHasNoValue.toolTipText")); // NOI18N
         chkHasNoValue.setName("chkHasNoValue"); // NOI18N
         chkHasNoValue.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 chkHasNoValueStateChanged(evt);
             }
         });
 
         lMandatory.setText(resourceMap.getString("lMandatory.text")); // NOI18N
         lMandatory.setToolTipText(resourceMap.getString("lMandatory.toolTipText")); // NOI18N
         lMandatory.setName("lMandatory"); // NOI18N
 
         chkMandatory.setToolTipText(resourceMap.getString("chkMandatory.toolTipText")); // NOI18N
         chkMandatory.setName("chkMandatory"); // NOI18N
         chkMandatory.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 chkMandatoryStateChanged(evt);
             }
         });
 
         chkSpace.setToolTipText(resourceMap.getString("chkSpace.toolTipText")); // NOI18N
         chkSpace.setName("chkSpace"); // NOI18N
         chkSpace.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 chkSpaceChanged(evt);
             }
         });
 
         lSpace.setText(resourceMap.getString("lSpace.text")); // NOI18N
         lSpace.setToolTipText(resourceMap.getString("lSpace.toolTipText")); // NOI18N
         lSpace.setName("lSpace"); // NOI18N
 
         jlParametersDefaultValue.setText(resourceMap.getString("jlParametersDefaultValue.text")); // NOI18N
         jlParametersDefaultValue.setName("jlParametersDefaultValue"); // NOI18N
 
         tfParametersDefaultValue.setText(resourceMap.getString("tfParametersDefaultValue.text")); // NOI18N
         tfParametersDefaultValue.setName("tfParametersDefaultValue"); // NOI18N
         tfParametersDefaultValue.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 parameterChangedOnFocusLost(evt);
             }
         });
         tfParametersDefaultValue.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 parameterChangedOnKeyReleased(evt);
             }
         });
 
         jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
         jLabel2.setToolTipText(resourceMap.getString("jLabel2.toolTipText")); // NOI18N
         jLabel2.setName("jLabel2"); // NOI18N
 
         chkAttachToPrevious.setToolTipText(resourceMap.getString("chkAttachToPrevious.toolTipText")); // NOI18N
         chkAttachToPrevious.setName("chkAttachToPrevious"); // NOI18N
         chkAttachToPrevious.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 chkAttachToPreviousStateChanged(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(jLabel2)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                     .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(jPanel2Layout.createSequentialGroup()
                             .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(jlParametersName, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                                 .addComponent(jlParametersPrefix, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                                 .addComponent(jlParametersOrder, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE))
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                         .addGroup(jPanel2Layout.createSequentialGroup()
                             .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(jLabel1)
                                 .addComponent(lMandatory)
                                 .addComponent(lSpace)
                                 .addComponent(jlParametersDefaultValue))
                             .addGap(12, 12, 12))))
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addComponent(tfParametersName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
                         .addComponent(tfParametersPrefix, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
                         .addComponent(tfParametersOrder, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
                         .addComponent(tfParametersDefaultValue, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
                         .addComponent(chkSpace))
                     .addComponent(chkAttachToPrevious, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(chkMandatory, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(chkHasNoValue, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel1, jlParametersName, jlParametersOrder, jlParametersPrefix});
 
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jlParametersName)
                     .addComponent(tfParametersName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jlParametersPrefix, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(tfParametersPrefix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jlParametersOrder)
                     .addComponent(tfParametersOrder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(tfParametersDefaultValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jlParametersDefaultValue))
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addGap(8, 8, 8)
                         .addComponent(jLabel1))
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(chkHasNoValue)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addGap(8, 8, 8)
                         .addComponent(lMandatory))
                     .addComponent(chkMandatory))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(lSpace)
                     .addComponent(chkSpace))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel2)
                     .addComponent(chkAttachToPrevious))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         panelParametersButons.setName("panelParametersButons"); // NOI18N
 
         btnParametersDelete.setText(resourceMap.getString("btnParametersDelete.text")); // NOI18N
         btnParametersDelete.setToolTipText(resourceMap.getString("btnParametersDelete.toolTipText")); // NOI18N
         btnParametersDelete.setName("btnParametersDelete"); // NOI18N
         btnParametersDelete.setPreferredSize(new java.awt.Dimension(81, 25));
         btnParametersDelete.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnParametersDeleteActionPerformed(evt);
             }
         });
 
         btnParametersNew.setText(resourceMap.getString("btnParametersNew.text")); // NOI18N
         btnParametersNew.setToolTipText(resourceMap.getString("btnParametersNew.toolTipText")); // NOI18N
         btnParametersNew.setName("btnParametersNew"); // NOI18N
         btnParametersNew.setPreferredSize(new java.awt.Dimension(81, 25));
         btnParametersNew.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnParametersNewActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout panelParametersButonsLayout = new javax.swing.GroupLayout(panelParametersButons);
         panelParametersButons.setLayout(panelParametersButonsLayout);
         panelParametersButonsLayout.setHorizontalGroup(
             panelParametersButonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelParametersButonsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnParametersNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnParametersDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(237, Short.MAX_VALUE))
         );
 
         panelParametersButonsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnParametersDelete, btnParametersNew});
 
         panelParametersButonsLayout.setVerticalGroup(
             panelParametersButonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelParametersButonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(btnParametersNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(btnParametersDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         javax.swing.GroupLayout panelParametersLayout = new javax.swing.GroupLayout(panelParameters);
         panelParameters.setLayout(panelParametersLayout);
         panelParametersLayout.setHorizontalGroup(
             panelParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelParametersLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                     .addComponent(panelParametersButons, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         panelParametersLayout.setVerticalGroup(
             panelParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelParametersLayout.createSequentialGroup()
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(panelParametersButons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout panelParametersOverallLayout = new javax.swing.GroupLayout(panelParametersOverall);
         panelParametersOverall.setLayout(panelParametersOverallLayout);
         panelParametersOverallLayout.setHorizontalGroup(
             panelParametersOverallLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(panelParameters, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
         );
         panelParametersOverallLayout.setVerticalGroup(
             panelParametersOverallLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelParametersOverallLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(panelParameters, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE))
         );
 
         jSplitPane2.setRightComponent(panelParametersOverall);
         panelParametersOverall.getAccessibleContext().setAccessibleName(resourceMap.getString("panelParameters.AccessibleContext.accessibleName")); // NOI18N
 
         panelSolverOverall.setName("panelSolverOverall"); // NOI18N
         panelSolverOverall.setPreferredSize(new java.awt.Dimension(500, 489));
 
         panelSolver.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelSolver.border.title"))); // NOI18N
         panelSolver.setAutoscrolls(true);
         panelSolver.setName("panelSolver"); // NOI18N
 
         jScrollPane2.setToolTipText(resourceMap.getString("jScrollPane2.toolTipText")); // NOI18N
         jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         jScrollPane2.setAutoscrolls(true);
         jScrollPane2.setEnabled(false);
         jScrollPane2.setMinimumSize(new java.awt.Dimension(100, 100));
         jScrollPane2.setName("jScrollPane2"); // NOI18N
         jScrollPane2.setPreferredSize(new java.awt.Dimension(100, 100));
 
         tableSolver.setAutoCreateRowSorter(true);
         tableSolver.setMinimumSize(new java.awt.Dimension(50, 0));
         tableSolver.setName("tableSolver"); // NOI18N
         tableSolver.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jScrollPane2.setViewportView(tableSolver);
 
         jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
         jPanel1.setName("jPanel1"); // NOI18N
 
         jlSolverName.setText(resourceMap.getString("jlSolverName.text")); // NOI18N
         jlSolverName.setName("jlSolverName"); // NOI18N
 
         tfSolverName.setText(resourceMap.getString("tfSolverName.text")); // NOI18N
         tfSolverName.setToolTipText(resourceMap.getString("tfSolverName.toolTipText")); // NOI18N
         tfSolverName.setName("tfSolverName"); // NOI18N
         tfSolverName.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 solverChangedOnFocusLost(evt);
             }
         });
         tfSolverName.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 solverChangedOnKey(evt);
             }
         });
 
         jlSolverDescription.setText(resourceMap.getString("jlSolverDescription.text")); // NOI18N
         jlSolverDescription.setName("jlSolverDescription"); // NOI18N
 
         jScrollPane3.setName("jScrollPane3"); // NOI18N
 
         taSolverDescription.setColumns(20);
         taSolverDescription.setLineWrap(true);
         taSolverDescription.setRows(5);
         taSolverDescription.setToolTipText(resourceMap.getString("taSolverDescription.toolTipText")); // NOI18N
         taSolverDescription.setName("taSolverDescription"); // NOI18N
         taSolverDescription.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 solverChangedOnFocusLost(evt);
             }
         });
         taSolverDescription.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 solverChangedOnKey(evt);
             }
         });
         jScrollPane3.setViewportView(taSolverDescription);
 
         jlSolverBinary.setText(resourceMap.getString("jlSolverBinary.text")); // NOI18N
         jlSolverBinary.setName("jlSolverBinary"); // NOI18N
 
         jlSolverCode.setText(resourceMap.getString("jlSolverCode.text")); // NOI18N
         jlSolverCode.setName("jlSolverCode"); // NOI18N
 
         btnSolverAddCode.setText(resourceMap.getString("btnSolverAddCode.text")); // NOI18N
         btnSolverAddCode.setToolTipText(resourceMap.getString("btnSolverAddCode.toolTipText")); // NOI18N
         btnSolverAddCode.setName("btnSolverAddCode"); // NOI18N
         btnSolverAddCode.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSolverAddCodeActionPerformed(evt);
             }
         });
 
         tfSolverAuthors.setText(resourceMap.getString("tfSolverAuthors.text")); // NOI18N
         tfSolverAuthors.setName("tfSolverAuthors"); // NOI18N
         tfSolverAuthors.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 solverChangedOnFocusLost(evt);
             }
         });
         tfSolverAuthors.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 solverChangedOnKey(evt);
             }
         });
 
         tfSolverVersion.setText(resourceMap.getString("tfSolverVersion.text")); // NOI18N
         tfSolverVersion.setName("tfSolverVersion"); // NOI18N
         tfSolverVersion.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 solverChangedOnFocusLost(evt);
             }
         });
         tfSolverVersion.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 solverChangedOnKey(evt);
             }
         });
 
         jlSolverAuthors.setText(resourceMap.getString("jlSolverAuthors.text")); // NOI18N
         jlSolverAuthors.setName("jlSolverAuthors"); // NOI18N
 
         jlSolverVersion.setText(resourceMap.getString("jlSolverVersion.text")); // NOI18N
         jlSolverVersion.setName("jlSolverVersion"); // NOI18N
 
         btnSolverAddBinary.setText(resourceMap.getString("btnSolverAddBinary.text")); // NOI18N
         btnSolverAddBinary.setToolTipText(resourceMap.getString("btnSolverAddBinary.toolTipText")); // NOI18N
         btnSolverAddBinary.setName("btnSolverAddBinary"); // NOI18N
         btnSolverAddBinary.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSolverAddBinaryActionPerformed(evt);
             }
         });
 
         jScrollPane4.setName("jScrollPane4"); // NOI18N
 
         tableSolverBinaries.setModel(new javax.swing.table.DefaultTableModel(
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
         tableSolverBinaries.setName("tableSolverBinaries"); // NOI18N
         jScrollPane4.setViewportView(tableSolverBinaries);
 
         btnSolverEditBinary.setText(resourceMap.getString("btnSolverEditBinary.text")); // NOI18N
         btnSolverEditBinary.setActionCommand(resourceMap.getString("btnSolverEditBinary.actionCommand")); // NOI18N
         btnSolverEditBinary.setName("btnSolverEditBinary"); // NOI18N
         btnSolverEditBinary.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSolverEditBinaryActionPerformed(evt);
             }
         });
 
         btnSolverDeleteBinary.setText(resourceMap.getString("btnSolverDeleteBinary.text")); // NOI18N
         btnSolverDeleteBinary.setActionCommand(resourceMap.getString("btnSolverDeleteBinary.actionCommand")); // NOI18N
         btnSolverDeleteBinary.setName("btnSolverDeleteBinary"); // NOI18N
         btnSolverDeleteBinary.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSolverDeleteBinaryActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addComponent(jlSolverBinary)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel1Layout.createSequentialGroup()
                                 .addComponent(btnSolverAddBinary, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(btnSolverEditBinary, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 221, Short.MAX_VALUE)
                                 .addComponent(btnSolverDeleteBinary, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addComponent(jScrollPane4)))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel1Layout.createSequentialGroup()
                                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jlSolverName, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                                     .addComponent(jlSolverDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                             .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                 .addGroup(jPanel1Layout.createSequentialGroup()
                                     .addComponent(jlSolverVersion)
                                     .addGap(56, 56, 56))
                                 .addGroup(jPanel1Layout.createSequentialGroup()
                                     .addComponent(jlSolverCode)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                             .addComponent(jlSolverAuthors))
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(tfSolverAuthors, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                             .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                             .addComponent(tfSolverName, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                             .addGroup(jPanel1Layout.createSequentialGroup()
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(btnSolverAddCode)
                                     .addComponent(tfSolverVersion, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))))))
                 .addContainerGap())
         );
 
         jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jlSolverBinary, jlSolverCode, jlSolverDescription, jlSolverName});
 
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(tfSolverName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jlSolverName))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jlSolverDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jlSolverAuthors)
                     .addComponent(tfSolverAuthors, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jlSolverVersion)
                     .addComponent(tfSolverVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jlSolverCode)
                     .addComponent(btnSolverAddCode))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jlSolverBinary)
                     .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnSolverAddBinary)
                     .addComponent(btnSolverEditBinary)
                     .addComponent(btnSolverDeleteBinary))
                 .addContainerGap())
         );
 
         panelSolverButtons.setName("panelSolverButtons"); // NOI18N
 
         btnSolverDelete.setText(resourceMap.getString("btnSolverDelete.text")); // NOI18N
         btnSolverDelete.setToolTipText(resourceMap.getString("btnSolverDelete.toolTipText")); // NOI18N
         btnSolverDelete.setName("btnSolverDelete"); // NOI18N
         btnSolverDelete.setPreferredSize(new java.awt.Dimension(81, 25));
         btnSolverDelete.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSolverDeleteActionPerformed(evt);
             }
         });
 
         btnSolverNew.setText(resourceMap.getString("btnNew.text")); // NOI18N
         btnSolverNew.setToolTipText(resourceMap.getString("btnNew.toolTipText")); // NOI18N
         btnSolverNew.setMaximumSize(new java.awt.Dimension(81, 25));
         btnSolverNew.setMinimumSize(new java.awt.Dimension(81, 25));
         btnSolverNew.setName("btnNew"); // NOI18N
         btnSolverNew.setPreferredSize(new java.awt.Dimension(81, 25));
         btnSolverNew.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSolverNewActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout panelSolverButtonsLayout = new javax.swing.GroupLayout(panelSolverButtons);
         panelSolverButtons.setLayout(panelSolverButtonsLayout);
         panelSolverButtonsLayout.setHorizontalGroup(
             panelSolverButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelSolverButtonsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnSolverNew, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnSolverDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(393, Short.MAX_VALUE))
         );
 
         panelSolverButtonsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnSolverDelete, btnSolverNew});
 
         panelSolverButtonsLayout.setVerticalGroup(
             panelSolverButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelSolverButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(btnSolverNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(btnSolverDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         javax.swing.GroupLayout panelSolverLayout = new javax.swing.GroupLayout(panelSolver);
         panelSolver.setLayout(panelSolverLayout);
         panelSolverLayout.setHorizontalGroup(
             panelSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSolverLayout.createSequentialGroup()
                 .addGroup(panelSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelSolverLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelSolverLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE))
                     .addGroup(panelSolverLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(panelSolverButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         panelSolverLayout.setVerticalGroup(
             panelSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSolverLayout.createSequentialGroup()
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(panelSolverButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         jScrollPane2.getAccessibleContext().setAccessibleParent(manageDBPane);
 
         javax.swing.GroupLayout panelSolverOverallLayout = new javax.swing.GroupLayout(panelSolverOverall);
         panelSolverOverall.setLayout(panelSolverOverallLayout);
         panelSolverOverallLayout.setHorizontalGroup(
             panelSolverOverallLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(panelSolver, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         panelSolverOverallLayout.setVerticalGroup(
             panelSolverOverallLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelSolverOverallLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(panelSolver, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jSplitPane2.setLeftComponent(panelSolverOverall);
 
         jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jPanel3.setName("jPanel3"); // NOI18N
 
         btnSolverSaveToDB.setText(resourceMap.getString("btnSolverSaveToDB.text")); // NOI18N
         btnSolverSaveToDB.setToolTipText(resourceMap.getString("btnSolverSaveToDB.toolTipText")); // NOI18N
         btnSolverSaveToDB.setName("btnSolverSaveToDB"); // NOI18N
         btnSolverSaveToDB.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSolverSaveToDBActionPerformed(evt);
             }
         });
 
         btnSolverRefresh.setText(resourceMap.getString("btnSolverRefresh.text")); // NOI18N
         btnSolverRefresh.setToolTipText(resourceMap.getString("btnSolverRefresh.toolTipText")); // NOI18N
         btnSolverRefresh.setName("btnSolverRefresh"); // NOI18N
         btnSolverRefresh.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSolverRefreshActionPerformed(evt);
             }
         });
 
         btnSolverExport.setText(resourceMap.getString("exportSolver.text")); // NOI18N
         btnSolverExport.setToolTipText(resourceMap.getString("exportSolver.toolTipText")); // NOI18N
         btnSolverExport.setName("exportSolver"); // NOI18N
         btnSolverExport.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnExport(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnSolverRefresh)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 744, Short.MAX_VALUE)
                 .addComponent(btnSolverExport, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(btnSolverSaveToDB)
                 .addContainerGap())
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnSolverRefresh)
                     .addComponent(btnSolverSaveToDB)
                     .addComponent(btnSolverExport))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout panelManageDBSolverLayout = new javax.swing.GroupLayout(panelManageDBSolver);
         panelManageDBSolver.setLayout(panelManageDBSolverLayout);
         panelManageDBSolverLayout.setHorizontalGroup(
             panelManageDBSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelManageDBSolverLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelManageDBSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jSplitPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1057, Short.MAX_VALUE))
                 .addContainerGap())
         );
         panelManageDBSolverLayout.setVerticalGroup(
             panelManageDBSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelManageDBSolverLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 543, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         manageDBPane.addTab("Solvers", panelManageDBSolver);
 
         panelManageDBInstances.setName("panelManageDBInstances"); // NOI18N
         panelManageDBInstances.setPreferredSize(new java.awt.Dimension(0, 0));
 
         jSplitPane1.setDividerLocation(0.6);
         jSplitPane1.setResizeWeight(0.4);
         jSplitPane1.setName("jSplitPane1"); // NOI18N
 
         panelInstanceClass.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelInstanceClass.border.title"))); // NOI18N
         panelInstanceClass.setName("panelInstanceClass"); // NOI18N
         panelInstanceClass.setPreferredSize(new java.awt.Dimension(0, 0));
 
         panelButtonsInstanceClass.setName("panelButtonsInstanceClass"); // NOI18N
 
         btnNewInstanceClass.setText(resourceMap.getString("btnNewInstanceClass.text")); // NOI18N
         btnNewInstanceClass.setToolTipText(resourceMap.getString("btnNewInstanceClass.toolTipText")); // NOI18N
         btnNewInstanceClass.setName("btnNewInstanceClass"); // NOI18N
         btnNewInstanceClass.setPreferredSize(new java.awt.Dimension(89, 25));
         btnNewInstanceClass.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnNewInstanceClassActionPerformed(evt);
             }
         });
 
         btnEditInstanceClass.setText(resourceMap.getString("btnEditInstanceClass.text")); // NOI18N
         btnEditInstanceClass.setToolTipText(resourceMap.getString("btnEditInstanceClass.toolTipText")); // NOI18N
         btnEditInstanceClass.setEnabled(false);
         btnEditInstanceClass.setName("btnEditInstanceClass"); // NOI18N
         btnEditInstanceClass.setPreferredSize(new java.awt.Dimension(89, 25));
         btnEditInstanceClass.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnEditInstanceClassActionPerformed(evt);
             }
         });
 
         btnRemoveInstanceClass.setText(resourceMap.getString("btnRemoveInstanceClass.text")); // NOI18N
         btnRemoveInstanceClass.setToolTipText(resourceMap.getString("btnRemoveInstanceClass.toolTipText")); // NOI18N
         btnRemoveInstanceClass.setEnabled(false);
         btnRemoveInstanceClass.setName("btnRemoveInstanceClass"); // NOI18N
         btnRemoveInstanceClass.setPreferredSize(new java.awt.Dimension(89, 25));
         btnRemoveInstanceClass.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemoveInstanceClassActionPerformed(evt);
             }
         });
 
         btnExportInstanceClass.setText(resourceMap.getString("btnExportInstanceClass.text")); // NOI18N
         btnExportInstanceClass.setName("btnExportInstanceClass"); // NOI18N
         btnExportInstanceClass.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnExportInstanceClassActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout panelButtonsInstanceClassLayout = new javax.swing.GroupLayout(panelButtonsInstanceClass);
         panelButtonsInstanceClass.setLayout(panelButtonsInstanceClassLayout);
         panelButtonsInstanceClassLayout.setHorizontalGroup(
             panelButtonsInstanceClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelButtonsInstanceClassLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnNewInstanceClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(btnEditInstanceClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(btnRemoveInstanceClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(btnExportInstanceClass)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         panelButtonsInstanceClassLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnEditInstanceClass, btnExportInstanceClass, btnNewInstanceClass, btnRemoveInstanceClass});
 
         panelButtonsInstanceClassLayout.setVerticalGroup(
             panelButtonsInstanceClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelButtonsInstanceClassLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelButtonsInstanceClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnNewInstanceClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnEditInstanceClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnRemoveInstanceClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnExportInstanceClass))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         panelButtonsInstanceClassLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnEditInstanceClass, btnExportInstanceClass, btnNewInstanceClass, btnRemoveInstanceClass});
 
         panelInstanceClassTable.setToolTipText(resourceMap.getString("panelInstanceClassTable.toolTipText")); // NOI18N
         panelInstanceClassTable.setName("panelInstanceClassTable"); // NOI18N
 
         jTreeInstanceClass.setName("jTreeInstanceClass"); // NOI18N
         panelInstanceClassTable.setViewportView(jTreeInstanceClass);
 
         javax.swing.GroupLayout panelInstanceClassLayout = new javax.swing.GroupLayout(panelInstanceClass);
         panelInstanceClass.setLayout(panelInstanceClassLayout);
         panelInstanceClassLayout.setHorizontalGroup(
             panelInstanceClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelInstanceClassLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelInstanceClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(panelButtonsInstanceClass, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(panelInstanceClassTable, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE))
                 .addContainerGap())
         );
         panelInstanceClassLayout.setVerticalGroup(
             panelInstanceClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelInstanceClassLayout.createSequentialGroup()
                 .addComponent(panelInstanceClassTable, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(panelButtonsInstanceClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         jSplitPane1.setLeftComponent(panelInstanceClass);
 
         panelInstance.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelInstance.border.border.title")))); // NOI18N
         panelInstance.setName("panelInstance"); // NOI18N
         panelInstance.setPreferredSize(new java.awt.Dimension(0, 0));
 
         panelInstanceTable.setName("panelInstanceTable"); // NOI18N
 
         tableInstances.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null}
             },
             new String [] {
                 "Name", "numAtoms", "numClauses", "ratio", "maxClauseLength"
             }
         ));
         tableInstances.setToolTipText(resourceMap.getString("tableInstances.toolTipText")); // NOI18N
         tableInstances.setMaximumSize(new java.awt.Dimension(2147483647, 8000));
         tableInstances.setName("tableInstances"); // NOI18N
         tableInstances.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tableInstancesMouseClicked(evt);
             }
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 tableInstancesMousePressed(evt);
             }
         });
         panelInstanceTable.setViewportView(tableInstances);
         tableInstances.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tableInstances.columnModel.title0")); // NOI18N
         tableInstances.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tableInstances.columnModel.title1")); // NOI18N
         tableInstances.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("tableInstances.columnModel.title2")); // NOI18N
         tableInstances.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("tableInstances.columnModel.title3")); // NOI18N
         tableInstances.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("tableInstances.columnModel.title4")); // NOI18N
 
         panelButtonsInstances.setName("panelButtonsInstances"); // NOI18N
 
         btnAddInstances.setText(resourceMap.getString("btnAddInstances.text")); // NOI18N
         btnAddInstances.setToolTipText(resourceMap.getString("btnAddInstances.toolTipText")); // NOI18N
         btnAddInstances.setName("btnAddInstances"); // NOI18N
         btnAddInstances.setPreferredSize(new java.awt.Dimension(83, 25));
         btnAddInstances.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddInstancesActionPerformed(evt);
             }
         });
 
         btnRemoveInstances.setText(resourceMap.getString("btnRemoveInstances.text")); // NOI18N
         btnRemoveInstances.setToolTipText(resourceMap.getString("btnRemoveInstances.toolTipText")); // NOI18N
         btnRemoveInstances.setEnabled(false);
         btnRemoveInstances.setName("btnRemoveInstances"); // NOI18N
         btnRemoveInstances.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemoveInstancesActionPerformed(evt);
             }
         });
 
         btnExportInstances.setText(resourceMap.getString("btnExportInstances.text")); // NOI18N
         btnExportInstances.setToolTipText(resourceMap.getString("btnExportInstances.toolTipText")); // NOI18N
         btnExportInstances.setEnabled(false);
         btnExportInstances.setName("btnExportInstances"); // NOI18N
         btnExportInstances.setPreferredSize(new java.awt.Dimension(83, 25));
         btnExportInstances.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnExportInstancesActionPerformed(evt);
             }
         });
 
         btnAddToClass.setText(resourceMap.getString("btnAddToClass.text")); // NOI18N
         btnAddToClass.setToolTipText(resourceMap.getString("btnAddToClass.toolTipText")); // NOI18N
         btnAddToClass.setEnabled(false);
         btnAddToClass.setName("btnAddToClass"); // NOI18N
         btnAddToClass.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddToClassActionPerformed(evt);
             }
         });
 
         btnRemoveFromClass.setToolTipText(resourceMap.getString("btnRemoveFromClass.toolTipText")); // NOI18N
         btnRemoveFromClass.setActionCommand(resourceMap.getString("btnRemoveFromClass.actionCommand")); // NOI18N
         btnRemoveFromClass.setEnabled(false);
         btnRemoveFromClass.setLabel(resourceMap.getString("btnRemoveFromClass.label")); // NOI18N
         btnRemoveFromClass.setName("btnRemoveFromClass"); // NOI18N
         btnRemoveFromClass.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemoveFromClassActionPerformed(evt);
             }
         });
 
         btnAddInstances1.setText(resourceMap.getString("btnAddInstances1.text")); // NOI18N
         btnAddInstances1.setToolTipText(resourceMap.getString("btnAddInstances1.toolTipText")); // NOI18N
         btnAddInstances1.setName("btnAddInstances1"); // NOI18N
         btnAddInstances1.setPreferredSize(new java.awt.Dimension(83, 25));
         btnAddInstances1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddInstances1ActionPerformed(evt);
             }
         });
 
         bComputeProperty.setText(resourceMap.getString("bComputeProperty.text")); // NOI18N
         bComputeProperty.setName("bComputeProperty"); // NOI18N
         bComputeProperty.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 bComputePropertyActionPerformed(evt);
             }
         });
 
         btnGenerate.setText(resourceMap.getString("btnGenerateInstances.text")); // NOI18N
         btnGenerate.setName("btnGenerateInstances"); // NOI18N
         btnGenerate.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnGenerateActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout panelButtonsInstancesLayout = new javax.swing.GroupLayout(panelButtonsInstances);
         panelButtonsInstances.setLayout(panelButtonsInstancesLayout);
         panelButtonsInstancesLayout.setHorizontalGroup(
             panelButtonsInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelButtonsInstancesLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelButtonsInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(panelButtonsInstancesLayout.createSequentialGroup()
                         .addComponent(btnAddToClass, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(btnRemoveFromClass)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 99, Short.MAX_VALUE)
                         .addComponent(bComputeProperty))
                     .addGroup(panelButtonsInstancesLayout.createSequentialGroup()
                         .addComponent(btnAddInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(btnRemoveInstances)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(btnAddInstances1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(btnGenerate)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 113, Short.MAX_VALUE)
                         .addComponent(btnExportInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
 
         panelButtonsInstancesLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnAddInstances, btnRemoveInstances});
 
         panelButtonsInstancesLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnAddToClass, btnRemoveFromClass});
 
         panelButtonsInstancesLayout.setVerticalGroup(
             panelButtonsInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelButtonsInstancesLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelButtonsInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnAddInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnRemoveInstances)
                     .addComponent(btnExportInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnAddInstances1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnGenerate))
                 .addGap(18, 18, 18)
                 .addGroup(panelButtonsInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnAddToClass)
                     .addComponent(btnRemoveFromClass)
                     .addComponent(bComputeProperty))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         lblFilterStatus.setText(resourceMap.getString("lblFilterStatus.text")); // NOI18N
         lblFilterStatus.setName("lblFilterStatus"); // NOI18N
 
         btnFilterInstances.setText(resourceMap.getString("btnFilterInstances.text")); // NOI18N
         btnFilterInstances.setToolTipText(resourceMap.getString("btnFilterInstances.toolTipText")); // NOI18N
         btnFilterInstances.setName("btnFilterInstances"); // NOI18N
         btnFilterInstances.setPreferredSize(new java.awt.Dimension(83, 25));
         btnFilterInstances.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnFilterInstancesActionPerformed(evt);
             }
         });
 
         btnSelectInstanceColumns.setText(resourceMap.getString("btnSelectInstanceColumns.text")); // NOI18N
         btnSelectInstanceColumns.setName("btnSelectInstanceColumns"); // NOI18N
         btnSelectInstanceColumns.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSelectInstanceColumnsActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout panelInstanceLayout = new javax.swing.GroupLayout(panelInstance);
         panelInstance.setLayout(panelInstanceLayout);
         panelInstanceLayout.setHorizontalGroup(
             panelInstanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelInstanceLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelInstanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(panelInstanceTable, javax.swing.GroupLayout.DEFAULT_SIZE, 582, Short.MAX_VALUE)
                     .addGroup(panelInstanceLayout.createSequentialGroup()
                         .addComponent(btnFilterInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(panelInstanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblFilterStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE)
                             .addComponent(btnSelectInstanceColumns, javax.swing.GroupLayout.Alignment.TRAILING)))
                     .addGroup(panelInstanceLayout.createSequentialGroup()
                         .addComponent(panelButtonsInstances, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addContainerGap())))
         );
         panelInstanceLayout.setVerticalGroup(
             panelInstanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelInstanceLayout.createSequentialGroup()
                 .addComponent(lblFilterStatus)
                 .addGroup(panelInstanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnSelectInstanceColumns)
                     .addComponent(btnFilterInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(panelInstanceTable, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(panelButtonsInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         panelInstanceLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnFilterInstances, btnSelectInstanceColumns});
 
         jSplitPane1.setRightComponent(panelInstance);
 
         javax.swing.GroupLayout panelManageDBInstancesLayout = new javax.swing.GroupLayout(panelManageDBInstances);
         panelManageDBInstances.setLayout(panelManageDBInstancesLayout);
         panelManageDBInstancesLayout.setHorizontalGroup(
             panelManageDBInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelManageDBInstancesLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1057, Short.MAX_VALUE)
                 .addContainerGap())
         );
         panelManageDBInstancesLayout.setVerticalGroup(
             panelManageDBInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelManageDBInstancesLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 597, Short.MAX_VALUE))
         );
 
         manageDBPane.addTab("Instances", panelManageDBInstances);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(manageDBPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1082, Short.MAX_VALUE)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(manageDBPane, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     public void btnAddInstancesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddInstancesActionPerformed
 
         //Starts the dialog at which the user has to choose a instance source class or the autogeneration.
 
         try {
             if (addInstanceDialog == null) {
                 JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
                 this.addInstanceDialog = new EDACCAddNewInstanceSelectClassDialog(mainFrame, true, null);
                 this.addInstanceDialog.setLocationRelativeTo(mainFrame);
             } else{
                 addInstanceDialog.refresh();
             }
             
             EDACCApp.getApplication().show(this.addInstanceDialog);
             Boolean compress = this.addInstanceDialog.isCompress();
             InstanceClass input = this.addInstanceDialog.getInput();
             String fileExtension = this.addInstanceDialog.getFileExtension();
             Boolean autoClass = this.addInstanceDialog.isAutoClass();
 
             //If the user doesn't cancel the dialog above, the fileChooser is shown.
             if (input != null) {
                 if (fileExtension.isEmpty()) {
 
                     JOptionPane.showMessageDialog(panelManageDBInstances,
                             "No fileextension is given.",
                             "Warning",
                             JOptionPane.WARNING_MESSAGE);
                     return;
                 }
                 //When the user choos autogenerate only directorys can be choosen, else files and directorys.
                 if (input.getName().equals("")) {
                     jFileChooserManageDBInstance.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                 } else {
                     jFileChooserManageDBInstance.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                 }
 
                 int returnVal = jFileChooserManageDBInstance.showOpenDialog(panelManageDBInstances);
                 File ret = jFileChooserManageDBInstance.getSelectedFile();
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     Tasks.startTask("addInstances", new Class[]{edacc.model.InstanceClass.class, java.io.File.class, edacc.model.Tasks.class, String.class, Boolean.class, Boolean.class}, new Object[]{input, ret, null, fileExtension, compress, autoClass}, manageDBInstances, EDACCManageDBMode.this);
                 }
             }
             input = null;
             unsavedChanges = true;
 
         } catch (NoConnectionToDBException ex) {
             Logger.getLogger(EDACCManageDBMode.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
             Logger.getLogger(EDACCManageDBMode.class.getName()).log(Level.SEVERE, null, ex);
         }
         jTreeInstanceClass.clearSelection();
         tableInstances.clearSelection();
         this.instanceClassTreeModel.reload();
         this.instanceTableModel.fireTableDataChanged();
 
     }//GEN-LAST:event_btnAddInstancesActionPerformed
 
     private void btnRemoveInstancesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveInstancesActionPerformed
         if (tableInstances.getSelectedRows().length == 0) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "No instances selected.",
                     "Warning",
                     JOptionPane.WARNING_MESSAGE);
         } else {
             try {
                 manageDBInstances.removeInstances(tableInstances.getSelectedRows());
                 tableInstances.clearSelection();
                 instanceTableModel.fireTableDataChanged();
             } catch (NoConnectionToDBException ex) {
                 Logger.getLogger(EDACCManageDBMode.class.getName()).log(Level.SEVERE, null, ex);
             } catch (SQLException ex) {
                 Logger.getLogger(EDACCManageDBMode.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         this.tableInstances.requestFocus();
 
     }//GEN-LAST:event_btnRemoveInstancesActionPerformed
 
     private void btnFilterInstancesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterInstancesActionPerformed
         EDACCApp.getApplication().show(instanceFilter);
         instanceTableModel.fireTableDataChanged();
         if (instanceFilter.hasFiltersApplied()) {
             setFilterStatus("This list of instances has filters applied to it. Use the filter button below to modify.");
         } else {
             setFilterStatus("");
         }
     }//GEN-LAST:event_btnFilterInstancesActionPerformed
 
     private void btnSolverSaveToDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSolverSaveToDBActionPerformed
             manageDBSolvers.saveSolvers();
             unsavedChanges = false;
     }//GEN-LAST:event_btnSolverSaveToDBActionPerformed
 
     private void btnSolverNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSolverNewActionPerformed
         manageDBSolvers.newSolver();
        tableSolver.getRowSorter().setSortKeys(null);
         tableSolver.getSelectionModel().setSelectionInterval(tableSolver.getRowCount() - 1, tableSolver.getRowCount() - 1);
         tableSolver.updateUI();
         unsavedChanges = true;
         tfSolverName.requestFocus();
     }//GEN-LAST:event_btnSolverNewActionPerformed
     JFileChooser binaryFileChooser;
     private void btnSolverAddBinaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSolverAddBinaryActionPerformed
         try {
             if (binaryFileChooser == null) {
                 binaryFileChooser = new JFileChooser();
                 binaryFileChooser.setMultiSelectionEnabled(true);
                 binaryFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
             }
 
             if (binaryFileChooser.showDialog(this, "Add Solver Binaries") == JFileChooser.APPROVE_OPTION) {
                 manageDBSolvers.addSolverBinary(binaryFileChooser.getSelectedFiles());
                 unsavedChanges = true;
             }
         } catch (FileNotFoundException ex) {
             ex.printStackTrace();
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "The binary of the solver couldn't be found: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         } catch (Exception ex) {
             ex.printStackTrace();
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "An error occured while adding the binary of the solver: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
         tableSolver.updateUI();
     }//GEN-LAST:event_btnSolverAddBinaryActionPerformed
 
     private void btnParametersNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParametersNewActionPerformed
         manageDBParameters.newParam();
         tableParameters.getSelectionModel().setSelectionInterval(tableParameters.getRowCount() - 1, tableParameters.getRowCount() - 1);
         tableParameters.updateUI();
         unsavedChanges = true;
         this.tfParametersName.requestFocus();
     }//GEN-LAST:event_btnParametersNewActionPerformed
 
     /**
      * Handles the key released events of the textfields "solver name" and "solver description".
      * @param evt
      */
     private void solverChangedOnKey(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_solverChangedOnKey
         solverChanged();
     }//GEN-LAST:event_solverChangedOnKey
 
     /**
      * Applies the solver name and description and updates the UI of the table.
      */
     private void solverChanged() {
         manageDBSolvers.applySolver(tfSolverName.getText(), taSolverDescription.getText(), tfSolverAuthors.getText(), tfSolverVersion.getText());
         tableSolver.updateUI();
         unsavedChanges = true;
     }
     private JFileChooser codeFileChooser;
     private void btnSolverAddCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSolverAddCodeActionPerformed
         try {
             if (codeFileChooser == null) {
                 codeFileChooser = new JFileChooser();
                 codeFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                 codeFileChooser.setMultiSelectionEnabled(true);
             }
             if (codeFileChooser.showDialog(this, "Choose code") == JFileChooser.APPROVE_OPTION) {
                 manageDBSolvers.addSolverCode(codeFileChooser.getSelectedFiles());
                 unsavedChanges = true;
             }
         } catch (FileNotFoundException ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "The code of the solver couldn't be found: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
         tableSolver.updateUI();
     }//GEN-LAST:event_btnSolverAddCodeActionPerformed
 
     /**
      * Handles the focus lost event of the solver textfields "name" and "description".
      * @param evt
      */
     private void solverChangedOnFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_solverChangedOnFocusLost
         solverChanged();
     }//GEN-LAST:event_solverChangedOnFocusLost
 
     private void btnSolverDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSolverDeleteActionPerformed
         // show warning first
         final int userAnswer = JOptionPane.showConfirmDialog(panelSolver,
                 "The selected solvers will be deleted. Do you wish to continue?",
                 "Delete selected solvers",
                 JOptionPane.YES_NO_OPTION);
         if (userAnswer == JOptionPane.NO_OPTION) {
             return;
         }
 
         int[] rows = tableSolver.getSelectedRows();
         LinkedList<Solver> selectedSolvers = new LinkedList<Solver>();
         int lastSelectedIndex = -1;
         for (int i : rows) {
             selectedSolvers.add(solverTableModel.getSolver(tableSolver.convertRowIndexToModel(i)));
             lastSelectedIndex = i;
         }
         if (selectedSolvers.isEmpty()) {
             JOptionPane.showMessageDialog(panelSolver, "No solver selected!", "Warning", JOptionPane.WARNING_MESSAGE);
         } else {
             while (!selectedSolvers.isEmpty()) { // are there remaining solvers to delete?
                 try {
                     Solver s = selectedSolvers.removeFirst();
                     manageDBSolvers.removeSolver(s);
                 } catch (Exception ex) {
                     JOptionPane.showMessageDialog(this,
                             "An error occured while deleting a solver: " + ex.getMessage(),
                             "Error",
                             JOptionPane.ERROR_MESSAGE);
                 } finally {
 
                     tableSolver.getSelectionModel().clearSelection();
                     solverTableModel.fireTableDataChanged();
                     tableSolver.updateUI();
                     tableParameters.updateUI();
 
                     // try to select the solver which stood one row over the last deleted solver
                     if (lastSelectedIndex >= tableSolver.getRowCount()) {
                         lastSelectedIndex = tableSolver.getRowCount() - 1;
                     }
                     if (lastSelectedIndex >= 0) {
                         tableSolver.getSelectionModel().setSelectionInterval(lastSelectedIndex, lastSelectedIndex);
                     }
                 }
             }
         }
         tfSolverName.setText("");
         taSolverDescription.setText("");
         tfSolverAuthors.setText("");
         tfSolverVersion.setText("");
     }//GEN-LAST:event_btnSolverDeleteActionPerformed
     private void btnExportInstancesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportInstancesActionPerformed
 
         if (tableInstances.getSelectedRowCount() == 0) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "No instances are selected. ",
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         } else {
             int returnVal = jFileChooserManageDBExportInstance.showOpenDialog(panelManageDBInstances);
             String path = jFileChooserManageDBExportInstance.getSelectedFile().getAbsolutePath();
             Tasks.startTask("exportInstances", new Class[]{int[].class, String.class, edacc.model.Tasks.class}, new Object[]{tableInstances.getSelectedRows(), path, null}, manageDBInstances, EDACCManageDBMode.this);
         }
 
     }//GEN-LAST:event_btnExportInstancesActionPerformed
 
     private void btnEditInstanceClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditInstanceClassActionPerformed
         if (jTreeInstanceClass.getSelectionCount() == 0) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "Please select an instance class to edit!",
                     "Warning",
                     JOptionPane.WARNING_MESSAGE);
         } else if (jTreeInstanceClass.getSelectionCount() > 1) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "Please select only one instance class to edit!",
                     "Warning",
                     JOptionPane.WARNING_MESSAGE);
         } else {
 
             try {
                 manageDBInstances.editInstanceClass();
                 this.manageDBInstances.loadInstanceClasses();
             } catch (SQLException ex) {
                 Logger.getLogger(EDACCManageDBMode.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
     }//GEN-LAST:event_btnEditInstanceClassActionPerformed
 
     private void btnParametersDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParametersDeleteActionPerformed
         if (tableParameters.getSelectedRow() == -1) {
             JOptionPane.showMessageDialog(this,
                     "No parameters selected!",
                     "Warning",
                     JOptionPane.WARNING_MESSAGE);
             return;
         }
         int selectedIndex = tableParameters.getSelectedRow();
         Parameter p = parameterTableModel.getParameter(tableParameters.convertRowIndexToModel(selectedIndex));
         try {
             manageDBParameters.removeParameter(p);
         } catch (NoConnectionToDBException ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "No connection to database: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         } catch (SQLException ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "SQL-Exception while deleting parameter: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
         // try select the parameter which stood on row over the deleted param
         if (selectedIndex >= tableParameters.getRowCount()) {
             selectedIndex--;
         }
         Parameter selected = null;
         tableParameters.clearSelection();
         if (selectedIndex >= 0) {
             selected = parameterTableModel.getParameter(tableParameters.convertRowIndexToModel(selectedIndex));
             tableParameters.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
         }
 
         showParameterDetails(
                 selected);
         tableParameters.updateUI();
     }//GEN-LAST:event_btnParametersDeleteActionPerformed
 
     private void btnSolverRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSolverRefreshActionPerformed
         //if (this.unsavedChanges)
         if ((JOptionPane.showConfirmDialog(this,
                 "This will reload all data from DB. You are going to lose all your unsaved changes. Do you wish to continue?",
                 "Warning!",
                 JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)) {
             try {
                 int row = tableSolver.getSelectedRow();
                 manageDBSolvers.loadSolvers();
                 manageDBParameters.loadParametersOfSolvers(solverTableModel.getSolvers());
                 tableSolver.updateUI();
                 panelSolverOverall.updateUI();
                 tableParameters.updateUI();
                 tableSolver.clearSelection();
                 unsavedChanges = false;
             } catch (NoConnectionToDBException ex) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "No connection to database: " + ex.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             } catch (SQLException ex) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "SQL-Exception while refreshing tables: " + ex.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             }
         }
     }//GEN-LAST:event_btnSolverRefreshActionPerformed
 
     private void btnNewInstanceClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewInstanceClassActionPerformed
         saveExpandedState();
         jTreeInstanceClass.setSelectionPath(null);
         manageDBInstances.addInstanceClasses();
         manageDBInstances.UpdateInstanceClasses();
         jTreeInstanceClass.setExpandsSelectedPaths(true);
         restoreExpandedState();
         /*tableInstanceClass.updateUI();
         unsavedChanges = true;*/
     }//GEN-LAST:event_btnNewInstanceClassActionPerformed
     private void parameterChangedOnFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_parameterChangedOnFocusLost
         parameterChanged();
     }//GEN-LAST:event_parameterChangedOnFocusLost
     private void btnAddToClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddToClassActionPerformed
         try {
             int[] selectedRowsInstance = tableInstances.getSelectedRows();
             for (int i = 0; i < selectedRowsInstance.length; i++) {
                 selectedRowsInstance[i] = tableInstances.convertRowIndexToModel(selectedRowsInstance[i]);
             }
             manageDBInstances.addInstancesToClass(selectedRowsInstance);
             unsavedChanges = true;
             tableInstances.requestFocus();
             if (instanceTableModel.getRowCount() != 0) {
                 tableInstances.addRowSelectionInterval(0, 0);
             }
         } catch (IOException ex) {
             Logger.getLogger(EDACCManageDBMode.class.getName()).log(Level.SEVERE, null, ex);
         }
     }//GEN-LAST:event_btnAddToClassActionPerformed
     private void btnRemoveFromClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveFromClassActionPerformed
         manageDBInstances.RemoveInstanceFromInstanceClass(tableInstances.getSelectedRows(), jTreeInstanceClass.getSelectionPaths());
         this.instanceTableModel.fireTableDataChanged();
     }//GEN-LAST:event_btnRemoveFromClassActionPerformed
     private JFileChooser exportFileChooser;
     private void btnExport(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExport
         if (exportFileChooser == null) {
             exportFileChooser = new JFileChooser();
             exportFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         }
 
         if (exportFileChooser.showDialog(this, "Export code and binary of selected solvers to directory") == JFileChooser.APPROVE_OPTION) {
             int[] rows = tableSolver.getSelectedRows();
 
             for (int i : rows) {
                 try {
                     manageDBSolvers.exportSolver(solverTableModel.getSolver(tableSolver.convertRowIndexToModel(i)), exportFileChooser.getSelectedFile());
                     manageDBSolvers.exportSolverCode(solverTableModel.getSolver(tableSolver.convertRowIndexToModel(i)), exportFileChooser.getSelectedFile());
                 } catch (Exception ex) {
                     JOptionPane.showMessageDialog(this,
                             "An error occured while exporting solver \"" + solverTableModel.getSolver(tableSolver.convertRowIndexToModel(i)).getName() + "\": " + ex.getMessage(),
                             "Error",
                             JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
     }//GEN-LAST:event_btnExport
 
     private void parameterChangedOnKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_parameterChangedOnKeyReleased
         parameterChanged();
     }//GEN-LAST:event_parameterChangedOnKeyReleased
 
     private void chkHasNoValueStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkHasNoValueStateChanged
         parameterChanged();
     }//GEN-LAST:event_chkHasNoValueStateChanged
 
     private void tableInstancesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableInstancesMouseClicked
     }//GEN-LAST:event_tableInstancesMouseClicked
 
     private void btnRemoveInstanceClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveInstanceClassActionPerformed
         if (jTreeInstanceClass.getSelectionCount() == 0) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "No instance class selected.",
                     "Warning",
                     JOptionPane.WARNING_MESSAGE);
         } else {
             try {
                 manageDBInstances.RemoveInstanceClass((DefaultMutableTreeNode) jTreeInstanceClass.getSelectionPath().getLastPathComponent());
                 tableInstances.clearSelection();
                 instanceTableModel.fireTableDataChanged();
             } catch (InstanceIsInExperimentException ex) {
                 Logger.getLogger(EDACCManageDBMode.class.getName()).log(Level.SEVERE, null, ex);
                 //instanceClassTableModel.fireTableDataChanged();                 ;
             } catch (NoConnectionToDBException ex) {
                 Logger.getLogger(EDACCManageDBMode.class.getName()).log(Level.SEVERE, null, ex);
             } catch (InstanceSourceClassHasInstance ex) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "The selected instance class cannot be removed. Because it is a source class with"
                         + " related instances.",
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             } catch (SQLException ex) {
                 Logger.getLogger(EDACCManageDBMode.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }//GEN-LAST:event_btnRemoveInstanceClassActionPerformed
 
     private void tableInstancesMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableInstancesMousePressed
     }//GEN-LAST:event_tableInstancesMousePressed
 
     private void btnAddInstances1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddInstances1ActionPerformed
         if (this.tableInstances.getSelectedRowCount() == 0) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "No instances selected.",
                     "Warning",
                     JOptionPane.WARNING_MESSAGE);
             return;
         }
         this.manageDBInstances.showInstanceInfoDialog(this.tableInstances.getSelectedRows());
     }//GEN-LAST:event_btnAddInstances1ActionPerformed
     private EDACCComputeInstancePropertyDialog computeInstancePropertyDlg;
     private void bComputePropertyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bComputePropertyActionPerformed
         Vector<Instance> instances = new Vector<Instance>();
         for (int i : tableInstances.getSelectedRows()) {
             instances.add(instanceTableModel.getInstance(i));
         }
         computeInstancePropertyDlg = new EDACCComputeInstancePropertyDialog(EDACCApp.getApplication().getMainFrame(), manageDBInstances, instances);
         computeInstancePropertyDlg.setLocationRelativeTo(this);
         computeInstancePropertyDlg.setVisible(true);
     }//GEN-LAST:event_bComputePropertyActionPerformed
 
     private void btnGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateActionPerformed
 
         if (instanceGenKCNF == null) {
             JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
             this.instanceGenKCNF = new EDACCInstanceGeneratorUnifKCNF(mainFrame, true);
             this.instanceGenKCNF.setLocationRelativeTo(mainFrame);
         }
         EDACCApp.getApplication().show(this.instanceGenKCNF);
         try {
             manageDBInstances.loadInstanceClasses();
             jTreeInstanceClass.updateUI();
             //try {
             /* } catch (NoConnectionToDBException ex) {
             Logger.getLogger(EDACCManageDBMode.class.getName()).log(Level.SEVERE, null, ex);
             } catch (SQLException ex) {
             Logger.getLogger(EDACCManageDBMode.class.getName()).log(Level.SEVERE, null, ex);
             }*/
         } catch (SQLException ex) {
             Logger.getLogger(EDACCManageDBMode.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         //try {
 
 
         /* } catch (NoConnectionToDBException ex) {
         Logger.getLogger(EDACCManageDBMode.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
         Logger.getLogger(EDACCManageDBMode.class.getName()).log(Level.SEVERE, null, ex);
         }*/
 
     }//GEN-LAST:event_btnGenerateActionPerformed
 
     private void btnExportInstanceClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportInstanceClassActionPerformed
         if (jTreeInstanceClass.getSelectionCount() == 0) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "No instance class selected.",
                     "Warning",
                     JOptionPane.WARNING_MESSAGE);
             return;
         } else if (jTreeInstanceClass.getSelectionCount() > 1) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "Only select one instance class to export.",
                     "Warning",
                     JOptionPane.WARNING_MESSAGE);
             return;
         } else {
             int returnVal = jFileChooserManageDBExportInstance.showOpenDialog(panelManageDBInstances);
             String path = jFileChooserManageDBExportInstance.getSelectedFile().getAbsolutePath();
 
             Tasks.startTask("exportInstanceClass", new Class[]{DefaultMutableTreeNode.class, String.class, edacc.model.Tasks.class}, new Object[]{(DefaultMutableTreeNode) jTreeInstanceClass.getSelectionPath().getLastPathComponent(), path, null}, manageDBInstances, EDACCManageDBMode.this);
         }
     }//GEN-LAST:event_btnExportInstanceClassActionPerformed
 
     private void jMIAddInstanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIAddInstanceActionPerformed
         btnAddInstancesActionPerformed(evt);
     }//GEN-LAST:event_jMIAddInstanceActionPerformed
 
     private void jMIRemoveInstanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIRemoveInstanceActionPerformed
         btnRemoveInstancesActionPerformed(evt);
     }//GEN-LAST:event_jMIRemoveInstanceActionPerformed
 
     private void jMIExportInstanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIExportInstanceActionPerformed
         btnExportInstancesActionPerformed(evt);
     }//GEN-LAST:event_jMIExportInstanceActionPerformed
 
     private void jMINewInstanceClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMINewInstanceClassActionPerformed
         btnNewInstanceClassActionPerformed(evt);
     }//GEN-LAST:event_jMINewInstanceClassActionPerformed
 
     private void jMIEditInstanceClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIEditInstanceClassActionPerformed
         btnEditInstanceClassActionPerformed(evt);
     }//GEN-LAST:event_jMIEditInstanceClassActionPerformed
 
     private void jMIRemoveInstanceClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIRemoveInstanceClassActionPerformed
         btnRemoveInstanceClassActionPerformed(evt);
     }//GEN-LAST:event_jMIRemoveInstanceClassActionPerformed
 
     private void jMIExportInstanceClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIExportInstanceClassActionPerformed
         btnExportInstanceClassActionPerformed(evt);
     }//GEN-LAST:event_jMIExportInstanceClassActionPerformed
 
     private void chkMandatoryStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkMandatoryStateChanged
         parameterChanged();
     }//GEN-LAST:event_chkMandatoryStateChanged
 
     private void chkSpaceChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkSpaceChanged
         parameterChanged();
     }//GEN-LAST:event_chkSpaceChanged
 
     private void btnSelectInstanceColumnsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectInstanceColumnsActionPerformed
         List<SortKey> sortKeys = (List<SortKey>) tableInstances.getRowSorter().getSortKeys();
         List<String> columnNames = new ArrayList<String>();
         for (SortKey sk : sortKeys) {
             columnNames.add(tableInstances.getColumnName(tableInstances.convertColumnIndexToView(sk.getColumn())));
         }
         EDACCManageInstanceColumnSelection dialog = new EDACCManageInstanceColumnSelection(EDACCApp.getApplication().getMainFrame(), true, instanceTableModel);
         dialog.setLocationRelativeTo(EDACCApp.getApplication().getMainFrame());
         dialog.setVisible(true);
         List<SortKey> newSortKeys = new ArrayList<SortKey>();
         for (int k = 0; k < columnNames.size(); k++) {
             String col = columnNames.get(k);
             for (int i = 0; i < tableInstances.getColumnCount(); i++) {
                 if (tableInstances.getColumnName(i).equals(col)) {
                     newSortKeys.add(new SortKey(tableInstances.convertColumnIndexToModel(i), sortKeys.get(k).getSortOrder()));
                 }
             }
         }
         tableInstances.getRowSorter().setSortKeys(newSortKeys);
         edacc.experiment.Util.updateTableColumnWidth(tableInstances);
         tableInstances.updateUI();
     }//GEN-LAST:event_btnSelectInstanceColumnsActionPerformed
 
     private void btnSolverEditBinaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSolverEditBinaryActionPerformed
         // get selected binary which the user wants to edit
         int selectedIndex = tableSolverBinaries.getSelectedRow();
         if (selectedIndex < 0) // no binary selected
             return;
         SolverBinaries selectedBinary = solverBinariesTableModel.
                 getSolverBinaries(tableSolverBinaries.
                 convertRowIndexToModel(selectedIndex));
         
         try {
             if (binaryFileChooser == null) {
                 binaryFileChooser = new JFileChooser();
                 binaryFileChooser.setMultiSelectionEnabled(true);
                 binaryFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
             }
 
             // show file chooser for binaries and save result
             int fileChooserResult = binaryFileChooser.showDialog(this, "Edit Solver Binaries");
             if (fileChooserResult == JFileChooser.APPROVE_OPTION) { 
                 // OK clicked -> change files to selected binaries
                 // show edit dialogs and perform modification (by calling controller)
                 manageDBSolvers.editSolverBinary(binaryFileChooser.getSelectedFiles(), selectedBinary);
                 unsavedChanges = true;
             } else if (fileChooserResult == JFileChooser.CANCEL_OPTION) {
                 // cancel clicked -> use old files and only edit details
                 manageDBSolvers.editSolverBinaryDetails(selectedBinary);
             }
         } catch (FileNotFoundException ex) {
             ex.printStackTrace();
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "The binary of the solver couldn't be found: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         } catch (Exception ex) {
             ex.printStackTrace();
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "An error occured while adding the binary of the solver: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
         tableSolver.updateUI();
     }//GEN-LAST:event_btnSolverEditBinaryActionPerformed
 
     private void btnSolverDeleteBinaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSolverDeleteBinaryActionPerformed
         // show warning first
         final int userAnswer = JOptionPane.showConfirmDialog(panelSolver,
                 "The selected solver binaries will be deleted. Do you wish to continue?",
                 "Delete selected solver binaries",
                 JOptionPane.YES_NO_OPTION);
         if (userAnswer == JOptionPane.NO_OPTION) {
             return;
         }
 
         int[] rows = tableSolverBinaries.getSelectedRows();
         LinkedList<SolverBinaries> selectedSolverBinaries = new LinkedList<SolverBinaries>();
         int lastSelectedIndex = -1;
         for (int i : rows) {
             selectedSolverBinaries.add(solverBinariesTableModel.getSolverBinaries(tableSolverBinaries.convertRowIndexToModel(i)));
             lastSelectedIndex = i;
         }
         if (selectedSolverBinaries.isEmpty()) {
             JOptionPane.showMessageDialog(panelSolver, "No solver binary selected!", "Warning", JOptionPane.WARNING_MESSAGE);
         } else {
             while (!selectedSolverBinaries.isEmpty()) { // are there remaining solvers to delete?
                 try {
                     SolverBinaries s = selectedSolverBinaries.removeFirst();
                     manageDBSolvers.removeSolverBinary(s);
                 } catch (Exception ex) {
                     JOptionPane.showMessageDialog(this,
                             "An error occured while deleting a solver binary: " + ex.getMessage(),
                             "Error",
                             JOptionPane.ERROR_MESSAGE);
                 } finally {
                     tableSolverBinaries.getSelectionModel().clearSelection();
                     solverBinariesTableModel.fireTableDataChanged();
                     tableSolverBinaries.updateUI();
 
                     // try to select the solver which stood one row over the last deleted solver
                     if (lastSelectedIndex >= tableSolverBinaries.getRowCount()) {
                         lastSelectedIndex = tableSolverBinaries.getRowCount() - 1;
                     }
                     if (lastSelectedIndex >= 0) {
                         tableSolverBinaries.getSelectionModel().setSelectionInterval(lastSelectedIndex, lastSelectedIndex);
                     }
                 }
             }
         }
     }//GEN-LAST:event_btnSolverDeleteBinaryActionPerformed
 
     private void chkAttachToPreviousStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkAttachToPreviousStateChanged
         parameterChanged();
     }//GEN-LAST:event_chkAttachToPreviousStateChanged
 
     private void parameterChanged() {
         int selectedRow = tableParameters.getSelectedRow();
 
         if (selectedRow == -1) {
             return;
         }
         selectedRow = tableParameters.convertRowIndexToModel(selectedRow);
         Parameter p = parameterTableModel.getParameter(selectedRow);
         p.setName(tfParametersName.getText());
 
 
         try {
             p.setOrder(Integer.parseInt(tfParametersOrder.getText()));
         } catch (NumberFormatException e) {
             if (!tfParametersOrder.getText().equals("")) {
                 tfParametersOrder.setText(Integer.toString(p.getOrder()));
             }
         }
         p.setDefaultValue(tfParametersDefaultValue.getText());
         p.setPrefix(tfParametersPrefix.getText());
         p.setHasValue(!chkHasNoValue.isSelected());
         p.setMandatory(chkMandatory.isSelected());
         p.setSpace(chkSpace.isSelected());
         p.setAttachToPrevious(chkAttachToPrevious.isSelected());
         tableParameters.updateUI();
         unsavedChanges = true;
         // show error message if necessary
         tfParametersName.getInputVerifier().shouldYieldFocus(tfParametersName);
     }
 
     public void showSolverDetails(Solver currentSolver) {
         boolean enabled = false;
 
 
         if (currentSolver != null) {
             enabled = true;
             tfSolverName.setText(currentSolver.getName());
             taSolverDescription.setText(currentSolver.getDescription());
             tfSolverAuthors.setText(currentSolver.getAuthors());
             tfSolverVersion.setText(currentSolver.getVersion());
             manageDBParameters.setCurrentSolver(currentSolver);
             tableParameters.updateUI();
         } else {
             tfSolverName.setText("");
             taSolverDescription.setText("");
             tfSolverAuthors.setText("");
             tfSolverVersion.setText("");
             manageDBParameters.setCurrentSolver(currentSolver);
             tableParameters.updateUI();
         }
         jlSolverName.setEnabled(enabled);
         jlSolverDescription.setEnabled(enabled);
         jlSolverAuthors.setEnabled(enabled);
         jlSolverVersion.setEnabled(enabled);
         jlSolverBinary.setEnabled(enabled);
         jlSolverCode.setEnabled(enabled);
         tfSolverName.setEnabled(enabled);
         taSolverDescription.setEnabled(enabled);
         tfSolverAuthors.setEnabled(enabled);
         tfSolverVersion.setEnabled(enabled);
         btnSolverAddBinary.setEnabled(enabled);
         btnSolverEditBinary.setEnabled(enabled); 
         btnSolverDeleteBinary.setEnabled(enabled);
         btnSolverAddCode.setEnabled(enabled);
         btnSolverExport.setEnabled(enabled);
 
 
         if (currentSolver != null) {
             parameterTableModel.setCurrentSolver(currentSolver);
             parameterTableModel.fireTableDataChanged();
         }
         btnParametersNew.setEnabled(enabled);
         btnParametersDelete.setEnabled(enabled);
         tableParameters.getSelectionModel().clearSelection();
         showParameterDetails(
                 null);
     }
 
     public void showParameterDetails(Parameter currentParameter) {
         boolean enabled = false;
 
         if (currentParameter != null) {
             enabled = true;
             tfParametersName.setText(currentParameter.getName());
             tfParametersOrder.setText(Integer.toString(currentParameter.getOrder()));
             tfParametersPrefix.setText(currentParameter.getPrefix());
             tfParametersDefaultValue.setText(currentParameter.getDefaultValue());
             chkHasNoValue.setSelected(!currentParameter.getHasValue());
             chkMandatory.setSelected(currentParameter.isMandatory());
             chkSpace.setSelected(currentParameter.getSpace());
             chkAttachToPrevious.setSelected(currentParameter.isAttachToPrevious());
             tfParametersName.getInputVerifier().shouldYieldFocus(tfParametersName);
         } else {
             tfParametersName.setText("");
             tfParametersOrder.setText("");
             tfParametersPrefix.setText("");
             tfParametersDefaultValue.setText("");
             chkHasNoValue.setSelected(false);
             chkMandatory.setSelected(false);
             chkSpace.setSelected(false);
             chkAttachToPrevious.setSelected(false);
             showInvalidParameterNameError(false);
             showInvalidParameterNameError(false);
         }
         tfParametersName.setEnabled(enabled);
         tfParametersPrefix.setEnabled(enabled);
         tfParametersOrder.setEnabled(enabled);
         tfParametersDefaultValue.setEnabled(enabled);
         chkHasNoValue.setEnabled(enabled);
         chkMandatory.setEnabled(enabled);
         chkSpace.setEnabled(enabled);
         chkAttachToPrevious.setEnabled(enabled);
     }
 
     @Action
     public void btnSaveParam() {
         if (tableParameters.getSelectedRow() == -1) {
             return;
         }
         Parameter p = parameterTableModel.getParameter(tableParameters.getSelectedRow());
         p.setName(tfParametersName.getText());
         p.setOrder(Integer.parseInt(tfParametersOrder.getText()));
         p.setPrefix(tfParametersPrefix.getText());
         parameterTableModel.fireTableDataChanged();
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton bComputeProperty;
     private javax.swing.JButton btnAddInstances;
     private javax.swing.JButton btnAddInstances1;
     private javax.swing.JButton btnAddToClass;
     private javax.swing.JButton btnEditInstanceClass;
     private javax.swing.JButton btnExportInstanceClass;
     private javax.swing.JButton btnExportInstances;
     private javax.swing.JButton btnFilterInstances;
     private javax.swing.JButton btnGenerate;
     private javax.swing.JButton btnNewInstanceClass;
     private javax.swing.JButton btnParametersDelete;
     private javax.swing.JButton btnParametersNew;
     private javax.swing.JButton btnRemoveFromClass;
     private javax.swing.JButton btnRemoveInstanceClass;
     private javax.swing.JButton btnRemoveInstances;
     private javax.swing.JButton btnSelectInstanceColumns;
     private javax.swing.JButton btnSolverAddBinary;
     private javax.swing.JButton btnSolverAddCode;
     private javax.swing.JButton btnSolverDelete;
     private javax.swing.JButton btnSolverDeleteBinary;
     private javax.swing.JButton btnSolverEditBinary;
     private javax.swing.JButton btnSolverExport;
     private javax.swing.JButton btnSolverNew;
     private javax.swing.JButton btnSolverRefresh;
     private javax.swing.JButton btnSolverSaveToDB;
     private javax.swing.JCheckBox chkAttachToPrevious;
     private javax.swing.JCheckBox chkHasNoValue;
     private javax.swing.JCheckBox chkMandatory;
     private javax.swing.JCheckBox chkSpace;
     private javax.swing.JFileChooser jFileChooserManageDBExportInstance;
     private javax.swing.JFileChooser jFileChooserManageDBInstance;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JMenuItem jMIAddInstance;
     private javax.swing.JMenuItem jMIEditInstanceClass;
     private javax.swing.JMenuItem jMIExportInstance;
     private javax.swing.JMenuItem jMIExportInstanceClass;
     private javax.swing.JMenuItem jMINewInstanceClass;
     private javax.swing.JMenuItem jMIRemoveInstance;
     private javax.swing.JMenuItem jMIRemoveInstanceClass;
     private javax.swing.JPopupMenu jPMInstanceTable;
     private javax.swing.JPopupMenu jPMInstanceTreeInstanceClass;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JSplitPane jSplitPane1;
     private javax.swing.JSplitPane jSplitPane2;
     private javax.swing.JTree jTreeInstanceClass;
     private javax.swing.JLabel jlParametersDefaultValue;
     private javax.swing.JLabel jlParametersName;
     private javax.swing.JLabel jlParametersOrder;
     private javax.swing.JLabel jlParametersPrefix;
     private javax.swing.JLabel jlSolverAuthors;
     private javax.swing.JLabel jlSolverBinary;
     private javax.swing.JLabel jlSolverCode;
     private javax.swing.JLabel jlSolverDescription;
     private javax.swing.JLabel jlSolverName;
     private javax.swing.JLabel jlSolverVersion;
     private javax.swing.JLabel lMandatory;
     private javax.swing.JLabel lSpace;
     private javax.swing.JLabel lblFilterStatus;
     private javax.swing.JTabbedPane manageDBPane;
     private javax.swing.JPanel panelButtonsInstanceClass;
     private javax.swing.JPanel panelButtonsInstances;
     private javax.swing.JPanel panelInstance;
     private javax.swing.JPanel panelInstanceClass;
     private javax.swing.JScrollPane panelInstanceClassTable;
     private javax.swing.JScrollPane panelInstanceTable;
     private javax.swing.JPanel panelManageDBInstances;
     private javax.swing.JPanel panelManageDBSolver;
     private javax.swing.JPanel panelParameters;
     private javax.swing.JPanel panelParametersButons;
     private javax.swing.JPanel panelParametersOverall;
     private javax.swing.JPanel panelSolver;
     private javax.swing.JPanel panelSolverButtons;
     private javax.swing.JPanel panelSolverOverall;
     private javax.swing.JTextArea taSolverDescription;
     private javax.swing.JTable tableInstances;
     private javax.swing.JTable tableParameters;
     private javax.swing.JTable tableSolver;
     private javax.swing.JTable tableSolverBinaries;
     private javax.swing.JTextField tfParametersDefaultValue;
     private javax.swing.JTextField tfParametersName;
     private javax.swing.JTextField tfParametersOrder;
     private javax.swing.JTextField tfParametersPrefix;
     private javax.swing.JTextField tfSolverAuthors;
     private javax.swing.JTextField tfSolverName;
     private javax.swing.JTextField tfSolverVersion;
     // End of variables declaration//GEN-END:variables
 
     public void onTaskStart(String methodName) {
     }
 
     public void onTaskFailed(String methodName, Throwable e) {
 
         if (methodName.equals("exportInstanceClass")) {
             if (e instanceof IOException) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "The instances couldn't be written: " + e.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             } else if (e instanceof NoConnectionToDBException) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "No connection to database: " + e.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             } else if (e instanceof SQLException) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "SQL-Exception: " + e.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             } else if (e instanceof InstanceNotInDBException) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "There is a problem with the data consistency ",
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             } else if (e instanceof MD5CheckFailedException) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         e,
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             } else if (e instanceof NoSuchAlgorithmException) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "An error occured while exporting solver binary: " + e.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             }
 
         } else if (methodName.equals("exportInstances")) {
             if (e instanceof IOException) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "The instances couldn't be written: " + e.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             } else if (e instanceof NoConnectionToDBException) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "No connection to database: " + e.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             } else if (e instanceof SQLException) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "SQL-Exception: " + e.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             } else if (e instanceof InstanceNotInDBException) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "There is a problem with the data consistency ",
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             } else if (e instanceof MD5CheckFailedException) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         e,
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             } else if (e instanceof NoSuchAlgorithmException) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "An error occured while exporting solver binary: " + e.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             }
 
 
         } else if (methodName.equals("addInstances")) {
             if (e instanceof NoConnectionToDBException) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "No connection to database: " + e.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             } else if (e instanceof SQLException) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "SQL-Exception: " + e.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             } else if (e instanceof InstanceException) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "No Instances have been found.", "Error",
                         JOptionPane.WARNING_MESSAGE);
             } else if (e instanceof TaskCancelledException) {
                 InstanceTableModel tableModel = new InstanceTableModel();
                 tableModel.addInstances(manageDBInstances.getTmp());
                 if (EDACCExtendedWarning.showMessageDialog(EDACCExtendedWarning.OK_CANCEL_OPTIONS,
                         EDACCApp.getApplication().getMainFrame(),
                         "Do you  want to remove the already added instances in the list?",
                         new JTable(tableModel))
                         == EDACCExtendedWarning.RET_OK_OPTION) {
                     try {
                         InstanceDAO.deleteAll(manageDBInstances.getTmp());
                     } catch (SQLException ex) {
                         Logger.getLogger(EDACCManageDBMode.class.getName()).log(Level.SEVERE, null, ex);
                     }
                     manageDBInstances.setTmp(new Vector<Instance>());
                 }
             }
 
         }
 
     }
 
     public void onTaskSuccessful(String methodName, Object result) {
         if (methodName.equals("addInstances")) {
             this.instanceTableModel.fireTableDataChanged();
             this.instanceClassTreeModel.reload();
         } else if (methodName.equals("exportInstancnes")) {
         } else if (methodName.equals("TryToRemoveInstances")) {
             this.instanceTableModel.fireTableDataChanged();
             this.instanceClassTreeModel.reload();
         }
 
 
     }
 
     public void setFilterStatus(String status) {
 
         lblFilterStatus.setForeground(Color.red);
         lblFilterStatus.setText(status);
         lblFilterStatus.setIcon(new ImageIcon("warning-icon.png"));
         lblFilterStatus.updateUI();
     }
 
     public void showInstanceClassButtons(boolean enable) {
         btnEditInstanceClass.setEnabled(enable);
         btnRemoveInstanceClass.setEnabled(enable);
         btnExportInstanceClass.setEnabled(enable);
         jMIEditInstanceClass.setEnabled(enable);
         jMIRemoveInstanceClass.setEnabled(enable);
         jMIExportInstanceClass.setEnabled(enable);
     }
 
     public void showInstanceButtons(boolean enable) {
         btnRemoveInstances.setEnabled(enable);
         btnAddToClass.setEnabled(enable);
         btnRemoveFromClass.setEnabled(enable);
         btnExportInstances.setEnabled(enable);
         jMIAddInstance.setEnabled(enable);
         jMIExportInstance.setEnabled(enable);
         jMIRemoveInstance.setEnabled(enable);
     }
 
     public void showSolverBinariesDetails(Vector<SolverBinaries> solverBinaries) {
         solverBinariesTableModel.setSolverBinaries(solverBinaries);
     }
 
     public void updateInstanceTable() {
         instanceFilter.clearInstanceClassIds();
         if (jTreeInstanceClass.getSelectionPaths() != null) {
             for (TreePath path : jTreeInstanceClass.getSelectionPaths()) {
                 for (Integer id : edacc.experiment.Util.getInstanceClassIdsFromPath((DefaultMutableTreeNode) (path.getLastPathComponent()))) {
                     instanceFilter.addInstanceClassId(id);
                 }
             }
             instanceTableModel.fireTableDataChanged();
         }
     }
 
     public void reinitialize() {
         tableInstances.clearSelection();
         instanceTableModel.clearTable();
         jTreeInstanceClass.clearSelection();
     }
 
     public void JTreeStateChanged() {
         instanceFilter.clearInstanceClassIds();
         if (jTreeInstanceClass.getSelectionPaths() != null) {
             for (TreePath path : jTreeInstanceClass.getSelectionPaths()) {
                 for (Integer id : edacc.experiment.Util.getInstanceClassIdsFromPath((DefaultMutableTreeNode) (path.getLastPathComponent()))) {
                     instanceFilter.addInstanceClassId(id);
                 }
             }
             instanceTableModel.fireTableDataChanged();
         }
     }
 
     /**
      * Verifies the input of the Parameter name TextField.
      */
     class ParameterNameVerifier extends InputVerifier {
 
         @Override
         public boolean verify(JComponent input) {
             String text = ((JTextField) input).getText();
             try {
                 return !text.equals("") && !manageDBParameters.parameterExists(text);
             } catch (Exception ex) {
                 return false;
             }
         }
 
         @Override
         public boolean shouldYieldFocus(javax.swing.JComponent input) {
             boolean valid = verify(input);
             showInvalidParameterNameError(!valid);
             return valid;
         }
     }
 
     private void showInvalidParameterNameError(boolean show) {
         if (show) {
             // set the color of the TextField to a nice red
             tfParametersName.setBackground(new Color(255, 102, 102));
         } else {
             tfParametersName.setBackground(Color.white);
         }
     }
 
     public JTree getInstanceClassTree() {
         return jTreeInstanceClass;
     }
     private Enumeration descendantExpandedPathsBeforeDrag;
     private TreePath selectedPathStored;
 
     public void saveExpandedState() {
         descendantExpandedPathsBeforeDrag = jTreeInstanceClass.getExpandedDescendants(new TreePath(((DefaultMutableTreeNode) instanceClassTreeModel.getRoot()).getPath()));
         selectedPathStored = jTreeInstanceClass.getSelectionModel().getSelectionPath();
     }
 
     public void restoreExpandedState() {
         if (descendantExpandedPathsBeforeDrag != null) {
             for (Enumeration e = descendantExpandedPathsBeforeDrag; e.hasMoreElements();) {
                 TreePath tmpPath = (TreePath) (e.nextElement());
                 jTreeInstanceClass.expandPath(
                         new TreePath(((DefaultMutableTreeNode) tmpPath.getLastPathComponent()).getPath()));
             }
         }
         jTreeInstanceClass.getSelectionModel().setSelectionPath(selectedPathStored);
     }
 }
