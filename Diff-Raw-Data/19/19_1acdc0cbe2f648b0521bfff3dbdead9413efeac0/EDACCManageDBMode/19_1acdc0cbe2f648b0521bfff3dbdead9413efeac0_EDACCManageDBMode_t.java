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
 
 import edacc.manageDB.*;
 import edacc.model.InstaceNotInDBException;
 import edacc.model.InstanceSourceClassHasInstance;
 import edacc.model.NoConnectionToDBException;
 import edacc.model.Parameter;
 import edacc.model.ParameterDAO;
 import edacc.model.Solver;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.table.TableRowSorter;
 import org.jdesktop.application.Action;
 
 /**
  *
  * @author rretz
  */
 public class EDACCManageDBMode extends javax.swing.JPanel {
 
     public ManageDBInstances manageDBInstances;
     public InstanceTableModel instanceTableModel;
     public InstanceClassTableModel instanceClassTableModel;
     public TableRowSorter<InstanceTableModel> sorter;
     private ManageDBSolvers manageDBSolvers;
     private SolverTableModel solverTableModel;
     private ManageDBParameters manageDBParameters;
     private ParameterTableModel parameterTableModel;
     public EDACCCreateInstanceClassDialog createInstanceClassDialog;
     public EDACCAddNewInstanceSelectClassDialog addInstanceDialog;
 
     public EDACCManageDBMode() {
         initComponents();
 
 
         manageDBInstances = new ManageDBInstances(this, panelManageDBInstances,
                 jFileChooserManageDBInstance, jFileChooserManageDBExportInstance);
 
         // initialize instance table
         instanceTableModel = new InstanceTableModel();
         tableInstances.setModel(instanceTableModel);
         tableInstances.setDefaultRenderer(Object.class, new InstanceTableCellRenderer());
         sorter = new TableRowSorter<InstanceTableModel>(instanceTableModel);
 
         // initialize instance class table
         instanceClassTableModel = new InstanceClassTableModel(tableInstances);
         tableInstanceClass.setModel(instanceClassTableModel);
 
         // initialize solver table
         solverTableModel = new SolverTableModel();
         manageDBSolvers = new ManageDBSolvers(this, solverTableModel);
         tableSolver.setModel(solverTableModel);
 
         // initialize parameter table
         parameterTableModel = new ParameterTableModel();
         manageDBParameters = new ManageDBParameters(this, parameterTableModel);
         tableParameters.setModel(parameterTableModel);
 
         tableSolver.getSelectionModel().addListSelectionListener(new SolverTableSelectionListener(tableSolver, manageDBSolvers));
         tableParameters.getSelectionModel().addListSelectionListener(new ParameterTableSelectionListener(tableParameters, manageDBParameters));
         showSolverDetails(null);
     }
 
     void initialize() throws NoConnectionToDBException, SQLException {
         manageDBSolvers.loadSolvers();
         manageDBParameters.loadParametersOfSolvers(solverTableModel.getSolvers());
         manageDBInstances.loadInstanceClasses();
     }
 
     public void addDocumentListener(javax.swing.JTextField tf) {
         tf.getDocument().addDocumentListener(
                 new DocumentListener() {
 
                     public void changedUpdate(DocumentEvent e) {
                         manageDBInstances.newFilter(tfInstanceFilterName.getText(),
                                 tfInstanceFilterNumAtomsMin.getText(), tfInstanceFilterNumAtomsMax.getText(),
                                 tfInstanceFilterNumClausesMin.getText(), tfInstanceFilterNumClausesMax.getText(),
                                 tfInstanceFilterRatioMin.getText(), tfInstanceFilterRatioMax.getText(),
                                 tfInstanceFilterMaxClauseLengthMin.getText(), tfInstanceFilterMaxClauseLengthMax.getText());
                     }
 
                     public void insertUpdate(DocumentEvent e) {
                         manageDBInstances.newFilter(tfInstanceFilterName.getText(),
                                 tfInstanceFilterNumAtomsMin.getText(), tfInstanceFilterNumAtomsMax.getText(),
                                 tfInstanceFilterNumClausesMin.getText(), tfInstanceFilterNumClausesMax.getText(),
                                 tfInstanceFilterRatioMin.getText(), tfInstanceFilterRatioMax.getText(),
                                 tfInstanceFilterMaxClauseLengthMin.getText(), tfInstanceFilterMaxClauseLengthMax.getText());
                     }
 
                     public void removeUpdate(DocumentEvent e) {
                         manageDBInstances.newFilter(tfInstanceFilterName.getText(),
                                 tfInstanceFilterNumAtomsMin.getText(), tfInstanceFilterNumAtomsMax.getText(),
                                 tfInstanceFilterNumClausesMin.getText(), tfInstanceFilterNumClausesMax.getText(),
                                 tfInstanceFilterRatioMin.getText(), tfInstanceFilterRatioMax.getText(),
                                 tfInstanceFilterMaxClauseLengthMin.getText(), tfInstanceFilterMaxClauseLengthMax.getText());
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
 
         jFileChooserManageDBInstance = new javax.swing.JFileChooser();
         jFileChooserManageDBExportInstance = new javax.swing.JFileChooser();
         manageDBPane = new javax.swing.JTabbedPane();
         panelManageDBSolver = new javax.swing.JPanel();
         panelSolver = new javax.swing.JPanel();
         jScrollPane2 = new javax.swing.JScrollPane();
         tableSolver = new javax.swing.JTable();
         jlSolverName = new javax.swing.JLabel();
         jlSolverDescription = new javax.swing.JLabel();
         tfSolverName = new javax.swing.JTextField();
         jScrollPane3 = new javax.swing.JScrollPane();
         taSolverDescription = new javax.swing.JTextArea();
         jlSolverBinary = new javax.swing.JLabel();
         btnSolverAddBinary = new javax.swing.JButton();
         jlSolverCode = new javax.swing.JLabel();
         btnSolverAddCode = new javax.swing.JButton();
         panelParameters = new javax.swing.JPanel();
         jlParametersName = new javax.swing.JLabel();
         tfParametersName = new javax.swing.JTextField();
         jlParametersPrefix = new javax.swing.JLabel();
         tfParametersPrefix = new javax.swing.JTextField();
         jlParametersOrder = new javax.swing.JLabel();
         tfParametersOrder = new javax.swing.JTextField();
         btnParametersNew = new javax.swing.JButton();
         btnParametersDelete = new javax.swing.JButton();
         btnParametersRefresh = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         tableParameters = new javax.swing.JTable();
         panelSolverButtons = new javax.swing.JPanel();
         btnSolverRefresh = new javax.swing.JButton();
         btnSolverDelete = new javax.swing.JButton();
         btnSolverNew = new javax.swing.JButton();
         btnSolverSaveToDB = new javax.swing.JButton();
         btnSolverExport = new javax.swing.JButton();
         panelManageDBInstances = new javax.swing.JPanel();
         panelInstanceClass = new javax.swing.JPanel();
         panelInstanceClassTable = new javax.swing.JScrollPane();
         tableInstanceClass = new javax.swing.JTable();
         panelButtonsInstanceClass = new javax.swing.JPanel();
         btnNewInstanceClass = new javax.swing.JButton();
         btnRemoveInstanceClass = new javax.swing.JButton();
         btnSelectAllInstanceClasses = new javax.swing.JButton();
         panelInstance = new javax.swing.JPanel();
         panelInstanceTable = new javax.swing.JScrollPane();
         tableInstances = new javax.swing.JTable();
         panelFilterInstances = new javax.swing.JPanel();
         jlInstanceFilterName = new javax.swing.JLabel();
         tfInstanceFilterName = new javax.swing.JTextField();
         tfInstanceFilterNumAtomsMin = new javax.swing.JTextField();
         jlInstanceFilterNumAtoms = new javax.swing.JLabel();
         jlInstanceFilterNumClauses = new javax.swing.JLabel();
         tfInstanceFilterNumClausesMin = new javax.swing.JTextField();
         jlInstanceFilterRatio = new javax.swing.JLabel();
         tfInstanceFilterRatioMin = new javax.swing.JTextField();
         tfInstanceFilterMaxClauseLengthMin = new javax.swing.JTextField();
         jlInstanceFilterMaxClauseLength = new javax.swing.JLabel();
         tfInstanceFilterNumAtomsMax = new javax.swing.JTextField();
         tfInstanceFilterNumClausesMax = new javax.swing.JTextField();
         tfInstanceFilterRatioMax = new javax.swing.JTextField();
         tfInstanceFilterMaxClauseLengthMax = new javax.swing.JTextField();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         jLabel7 = new javax.swing.JLabel();
         jLabel9 = new javax.swing.JLabel();
         panelButtonsInstances = new javax.swing.JPanel();
         btnAddInstances = new javax.swing.JButton();
         btnRemoveInstances = new javax.swing.JButton();
         btnRefreshTableInstances = new javax.swing.JButton();
         btnFilterInstances = new javax.swing.JButton();
         btnExportInstances = new javax.swing.JButton();
         btnAddToClass = new javax.swing.JButton();
         btnRemoveFromClass = new javax.swing.JButton();
 
         jFileChooserManageDBInstance.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
         jFileChooserManageDBInstance.setName("jFileChooserManageDBInstance"); // NOI18N
 
         jFileChooserManageDBExportInstance.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
         jFileChooserManageDBExportInstance.setName("jFileChooserManageDBExportInstance"); // NOI18N
 
         setName("Form"); // NOI18N
         setPreferredSize(new java.awt.Dimension(500, 591));
 
         manageDBPane.setName("manageDBPane"); // NOI18N
         manageDBPane.setPreferredSize(new java.awt.Dimension(10005, 10026));
         manageDBPane.setRequestFocusEnabled(false);
 
         panelManageDBSolver.setName("panelManageDBSolver"); // NOI18N
 
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(edacc.EDACCApp.class).getContext().getResourceMap(EDACCManageDBMode.class);
         panelSolver.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelSolver.border.title"))); // NOI18N
         panelSolver.setName("panelSolver"); // NOI18N
 
         jScrollPane2.setName("jScrollPane2"); // NOI18N
 
         tableSolver.setName("tableSolver"); // NOI18N
         jScrollPane2.setViewportView(tableSolver);
 
         jlSolverName.setText(resourceMap.getString("jlSolverName.text")); // NOI18N
         jlSolverName.setName("jlSolverName"); // NOI18N
 
         jlSolverDescription.setText(resourceMap.getString("jlSolverDescription.text")); // NOI18N
         jlSolverDescription.setName("jlSolverDescription"); // NOI18N
 
         tfSolverName.setText(resourceMap.getString("tfSolverName.text")); // NOI18N
         tfSolverName.setName("tfSolverName"); // NOI18N
         tfSolverName.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 solverChangedOnFocusLost(evt);
             }
         });
         tfSolverName.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 solverChangedOnKey(evt);
             }
         });
 
         jScrollPane3.setName("jScrollPane3"); // NOI18N
 
         taSolverDescription.setColumns(20);
         taSolverDescription.setRows(5);
         taSolverDescription.setName("taSolverDescription"); // NOI18N
         taSolverDescription.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 solverChangedOnFocusLost(evt);
             }
         });
         taSolverDescription.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 solverChangedOnKey(evt);
             }
         });
         jScrollPane3.setViewportView(taSolverDescription);
 
         jlSolverBinary.setText(resourceMap.getString("jlSolverBinary.text")); // NOI18N
         jlSolverBinary.setName("jlSolverBinary"); // NOI18N
 
         btnSolverAddBinary.setText(resourceMap.getString("btnSolverAddBinary.text")); // NOI18N
         btnSolverAddBinary.setName("btnSolverAddBinary"); // NOI18N
         btnSolverAddBinary.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSolverAddBinaryActionPerformed(evt);
             }
         });
 
         jlSolverCode.setText(resourceMap.getString("jlSolverCode.text")); // NOI18N
         jlSolverCode.setName("jlSolverCode"); // NOI18N
 
         btnSolverAddCode.setText(resourceMap.getString("btnSolverAddCode.text")); // NOI18N
         btnSolverAddCode.setName("btnSolverAddCode"); // NOI18N
         btnSolverAddCode.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSolverAddCodeActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout panelSolverLayout = new javax.swing.GroupLayout(panelSolver);
         panelSolver.setLayout(panelSolverLayout);
         panelSolverLayout.setHorizontalGroup(
             panelSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelSolverLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 747, Short.MAX_VALUE)
                     .addGroup(panelSolverLayout.createSequentialGroup()
                         .addGroup(panelSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jlSolverDescription)
                             .addComponent(jlSolverName)
                             .addComponent(jlSolverBinary)
                             .addComponent(jlSolverCode))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(panelSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(btnSolverAddBinary)
                             .addGroup(panelSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                 .addComponent(btnSolverAddCode, javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(tfSolverName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                                 .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING)))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 344, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         panelSolverLayout.setVerticalGroup(
             panelSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSolverLayout.createSequentialGroup()
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                 .addGap(18, 18, 18)
                 .addGroup(panelSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jlSolverName)
                     .addComponent(tfSolverName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(panelSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jlSolverDescription)
                     .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(panelSolverLayout.createSequentialGroup()
                         .addGap(9, 9, 9)
                         .addComponent(jlSolverBinary))
                     .addComponent(btnSolverAddBinary))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(panelSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jlSolverCode)
                     .addComponent(btnSolverAddCode))
                 .addContainerGap())
         );
 
         panelParameters.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelParameters.border.title"))); // NOI18N
         panelParameters.setName("panelParameters"); // NOI18N
 
         jlParametersName.setText(resourceMap.getString("jlParametersName.text")); // NOI18N
         jlParametersName.setName("jlParametersName"); // NOI18N
 
         tfParametersName.setText(resourceMap.getString("tfParametersName.text")); // NOI18N
         tfParametersName.setName("tfParametersName"); // NOI18N
         tfParametersName.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 parameterChangedOnFocusLost(evt);
             }
         });
         tfParametersName.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 parameterChangedOnKeyPressed(evt);
             }
         });
 
         jlParametersPrefix.setText(resourceMap.getString("jlParametersPrefix.text")); // NOI18N
         jlParametersPrefix.setName("jlParametersPrefix"); // NOI18N
 
         tfParametersPrefix.setText(resourceMap.getString("tfParametersPrefix.text")); // NOI18N
         tfParametersPrefix.setName("tfParametersPrefix"); // NOI18N
         tfParametersPrefix.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 parameterChangedOnFocusLost(evt);
             }
         });
         tfParametersPrefix.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 parameterChangedOnKeyPressed(evt);
             }
         });
 
         jlParametersOrder.setText(resourceMap.getString("jlParametersOrder.text")); // NOI18N
         jlParametersOrder.setName("jlParametersOrder"); // NOI18N
 
         tfParametersOrder.setText(resourceMap.getString("tfParametersOrder.text")); // NOI18N
         tfParametersOrder.setName("tfParametersOrder"); // NOI18N
         tfParametersOrder.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 parameterChangedOnFocusLost(evt);
             }
         });
         tfParametersOrder.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 parameterChangedOnKeyPressed(evt);
             }
         });
 
         btnParametersNew.setText(resourceMap.getString("btnParametersNew.text")); // NOI18N
         btnParametersNew.setName("btnParametersNew"); // NOI18N
         btnParametersNew.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnParametersNewActionPerformed(evt);
             }
         });
 
         btnParametersDelete.setText(resourceMap.getString("btnParametersDelete.text")); // NOI18N
         btnParametersDelete.setName("btnParametersDelete"); // NOI18N
         btnParametersDelete.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnParametersDeleteActionPerformed(evt);
             }
         });
 
         btnParametersRefresh.setText(resourceMap.getString("btnParametersRefresh.text")); // NOI18N
         btnParametersRefresh.setName("btnParametersRefresh"); // NOI18N
         btnParametersRefresh.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnParametersRefreshActionPerformed(evt);
             }
         });
 
         jScrollPane1.setName("jScrollPane1"); // NOI18N
 
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
         tableParameters.setName("tableParameters"); // NOI18N
         jScrollPane1.setViewportView(tableParameters);
 
         javax.swing.GroupLayout panelParametersLayout = new javax.swing.GroupLayout(panelParameters);
         panelParameters.setLayout(panelParametersLayout);
         panelParametersLayout.setHorizontalGroup(
             panelParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelParametersLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
                     .addGroup(panelParametersLayout.createSequentialGroup()
                         .addComponent(btnParametersNew)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnParametersDelete)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnParametersRefresh))
                     .addGroup(panelParametersLayout.createSequentialGroup()
                         .addGroup(panelParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jlParametersPrefix)
                             .addComponent(jlParametersName))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(panelParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addComponent(tfParametersName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(tfParametersOrder, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                                 .addComponent(tfParametersPrefix))))
                     .addComponent(jlParametersOrder))
                 .addContainerGap())
         );
         panelParametersLayout.setVerticalGroup(
             panelParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelParametersLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(panelParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jlParametersName)
                     .addComponent(tfParametersName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jlParametersPrefix)
                     .addComponent(tfParametersPrefix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jlParametersOrder)
                     .addComponent(tfParametersOrder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(panelParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnParametersNew)
                     .addComponent(btnParametersDelete)
                     .addComponent(btnParametersRefresh))
                 .addContainerGap())
         );
 
         panelSolverButtons.setName("panelSolverButtons"); // NOI18N
 
         btnSolverRefresh.setText(resourceMap.getString("btnSolverRefresh.text")); // NOI18N
         btnSolverRefresh.setName("btnSolverRefresh"); // NOI18N
         btnSolverRefresh.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSolverRefreshActionPerformed(evt);
             }
         });
 
         btnSolverDelete.setText(resourceMap.getString("btnSolverDelete.text")); // NOI18N
         btnSolverDelete.setName("btnSolverDelete"); // NOI18N
         btnSolverDelete.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSolverDeleteActionPerformed(evt);
             }
         });
 
         btnSolverNew.setText(resourceMap.getString("btnNew.text")); // NOI18N
         btnSolverNew.setName("btnNew"); // NOI18N
         btnSolverNew.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSolverNewActionPerformed(evt);
             }
         });
 
         btnSolverSaveToDB.setText(resourceMap.getString("btnSolverSaveToDB.text")); // NOI18N
         btnSolverSaveToDB.setName("btnSolverSaveToDB"); // NOI18N
         btnSolverSaveToDB.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSolverSaveToDBActionPerformed(evt);
             }
         });
 
         btnSolverExport.setText(resourceMap.getString("exportSolver.text")); // NOI18N
         btnSolverExport.setName("exportSolver"); // NOI18N
         btnSolverExport.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnExport(evt);
             }
         });
 
         javax.swing.GroupLayout panelSolverButtonsLayout = new javax.swing.GroupLayout(panelSolverButtons);
         panelSolverButtons.setLayout(panelSolverButtonsLayout);
         panelSolverButtonsLayout.setHorizontalGroup(
             panelSolverButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelSolverButtonsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnSolverNew)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnSolverDelete)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnSolverRefresh)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnSolverExport)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 767, Short.MAX_VALUE)
                 .addComponent(btnSolverSaveToDB)
                 .addContainerGap())
         );
         panelSolverButtonsLayout.setVerticalGroup(
             panelSolverButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelSolverButtonsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelSolverButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnSolverNew)
                     .addComponent(btnSolverDelete)
                     .addComponent(btnSolverRefresh)
                     .addComponent(btnSolverSaveToDB)
                     .addComponent(btnSolverExport))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout panelManageDBSolverLayout = new javax.swing.GroupLayout(panelManageDBSolver);
         panelManageDBSolver.setLayout(panelManageDBSolverLayout);
         panelManageDBSolverLayout.setHorizontalGroup(
             panelManageDBSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelManageDBSolverLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelManageDBSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(panelSolverButtons, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelManageDBSolverLayout.createSequentialGroup()
                         .addComponent(panelSolver, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(panelParameters, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                 .addGap(12, 12, 12))
         );
         panelManageDBSolverLayout.setVerticalGroup(
             panelManageDBSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelManageDBSolverLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelManageDBSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(panelSolver, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(panelParameters, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(panelSolverButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         panelParameters.getAccessibleContext().setAccessibleName(resourceMap.getString("panelParameters.AccessibleContext.accessibleName")); // NOI18N
 
         manageDBPane.addTab("Solvers", panelManageDBSolver);
 
         panelManageDBInstances.setName("panelManageDBInstances"); // NOI18N
         panelManageDBInstances.setPreferredSize(new java.awt.Dimension(500, 471));
 
         panelInstanceClass.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelInstanceClass.border.title"))); // NOI18N
         panelInstanceClass.setName("panelInstanceClass"); // NOI18N
 
         panelInstanceClassTable.setName("panelInstanceClassTable"); // NOI18N
 
         tableInstanceClass.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null},
                 {null, null},
                 {null, null},
                 {null, null}
             },
             new String [] {
                 "Class", "Select"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class, java.lang.Boolean.class
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
         });
         tableInstanceClass.setName("tableInstanceClass"); // NOI18N
         panelInstanceClassTable.setViewportView(tableInstanceClass);
         tableInstanceClass.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tableInstanceClass.columnModel.title0")); // NOI18N
         tableInstanceClass.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tableInstanceClass.columnModel.title1")); // NOI18N
 
         panelButtonsInstanceClass.setName("panelButtonsInstanceClass"); // NOI18N
 
         btnNewInstanceClass.setText(resourceMap.getString("btnNewInstanceClass.text")); // NOI18N
         btnNewInstanceClass.setName("btnNewInstanceClass"); // NOI18N
         btnNewInstanceClass.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnNewInstanceClassActionPerformed(evt);
             }
         });
 
         btnRemoveInstanceClass.setText(resourceMap.getString("btnRemoveInstanceClass.text")); // NOI18N
         btnRemoveInstanceClass.setName("btnRemoveInstanceClass"); // NOI18N
         btnRemoveInstanceClass.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemoveInstanceClassActionPerformed(evt);
             }
         });
 
         btnSelectAllInstanceClasses.setText(resourceMap.getString("btnSelectAllInstanceClasses.text")); // NOI18N
         btnSelectAllInstanceClasses.setName("btnSelectAllInstanceClasses"); // NOI18N
         btnSelectAllInstanceClasses.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSelectAllInstanceClassesActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout panelButtonsInstanceClassLayout = new javax.swing.GroupLayout(panelButtonsInstanceClass);
         panelButtonsInstanceClass.setLayout(panelButtonsInstanceClassLayout);
         panelButtonsInstanceClassLayout.setHorizontalGroup(
             panelButtonsInstanceClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelButtonsInstanceClassLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnNewInstanceClass)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(btnRemoveInstanceClass)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(btnSelectAllInstanceClasses)
                 .addContainerGap(408, Short.MAX_VALUE))
         );
         panelButtonsInstanceClassLayout.setVerticalGroup(
             panelButtonsInstanceClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelButtonsInstanceClassLayout.createSequentialGroup()
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(panelButtonsInstanceClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnNewInstanceClass)
                     .addComponent(btnRemoveInstanceClass)
                     .addComponent(btnSelectAllInstanceClasses))
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout panelInstanceClassLayout = new javax.swing.GroupLayout(panelInstanceClass);
         panelInstanceClass.setLayout(panelInstanceClassLayout);
         panelInstanceClassLayout.setHorizontalGroup(
             panelInstanceClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelInstanceClassLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelInstanceClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(panelButtonsInstanceClass, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(panelInstanceClassTable, javax.swing.GroupLayout.DEFAULT_SIZE, 677, Short.MAX_VALUE))
                 .addContainerGap())
         );
         panelInstanceClassLayout.setVerticalGroup(
             panelInstanceClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelInstanceClassLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(panelInstanceClassTable, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                 .addGap(11, 11, 11)
                 .addComponent(panelButtonsInstanceClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         panelInstance.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelInstance.border.border.title")))); // NOI18N
         panelInstance.setName("panelInstance"); // NOI18N
         panelInstance.setPreferredSize(new java.awt.Dimension(663, 596));
 
         panelInstanceTable.setName("panelInstanceTable"); // NOI18N
 
         tableInstances.setAutoCreateRowSorter(true);
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
         tableInstances.setMaximumSize(new java.awt.Dimension(2147483647, 8000));
         tableInstances.setName("tableInstances"); // NOI18N
         panelInstanceTable.setViewportView(tableInstances);
         tableInstances.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tableInstances.columnModel.title0")); // NOI18N
         tableInstances.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tableInstances.columnModel.title1")); // NOI18N
         tableInstances.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("tableInstances.columnModel.title2")); // NOI18N
         tableInstances.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("tableInstances.columnModel.title3")); // NOI18N
         tableInstances.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("tableInstances.columnModel.title4")); // NOI18N
 
         panelFilterInstances.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelFilterInstances.border.title"))); // NOI18N
         panelFilterInstances.setName("panelFilterInstances"); // NOI18N
         panelFilterInstances.addContainerListener(new java.awt.event.ContainerAdapter() {
             public void componentAdded(java.awt.event.ContainerEvent evt) {
                 panelFilterInstancesComponentAdded(evt);
             }
         });
 
         jlInstanceFilterName.setText(resourceMap.getString("jlInstanceFilterName.text")); // NOI18N
         jlInstanceFilterName.setName("jlInstanceFilterName"); // NOI18N
 
         tfInstanceFilterName.setName("tfInstanceFilterName"); // NOI18N
         tfInstanceFilterName.addInputMethodListener(new java.awt.event.InputMethodListener() {
             public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
             }
             public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                 tfInstanceFilterNameInputMethodTextChanged(evt);
             }
         });
 
         tfInstanceFilterNumAtomsMin.setName("tfInstanceFilterNumAtomsMin"); // NOI18N
 
         jlInstanceFilterNumAtoms.setText(resourceMap.getString("jlInstanceFilterNumAtoms.text")); // NOI18N
         jlInstanceFilterNumAtoms.setName("jlInstanceFilterNumAtoms"); // NOI18N
 
         jlInstanceFilterNumClauses.setText(resourceMap.getString("jlInstanceFilterNumClauses.text")); // NOI18N
         jlInstanceFilterNumClauses.setName("jlInstanceFilterNumClauses"); // NOI18N
 
         tfInstanceFilterNumClausesMin.setName("tfInstanceFilterNumClausesMin"); // NOI18N
 
         jlInstanceFilterRatio.setText(resourceMap.getString("jlInstanceFilterRatio.text")); // NOI18N
         jlInstanceFilterRatio.setName("jlInstanceFilterRatio"); // NOI18N
 
         tfInstanceFilterRatioMin.setName("tfInstanceFilterRatioMin"); // NOI18N
 
         tfInstanceFilterMaxClauseLengthMin.setName("tfInstanceFilterMaxClauseLengthMin"); // NOI18N
 
         jlInstanceFilterMaxClauseLength.setText(resourceMap.getString("jlInstanceFilterMaxClauseLength.text")); // NOI18N
         jlInstanceFilterMaxClauseLength.setName("jlInstanceFilterMaxClauseLength"); // NOI18N
 
         tfInstanceFilterNumAtomsMax.setName("tfInstanceFilterNumAtomsMax"); // NOI18N
 
         tfInstanceFilterNumClausesMax.setName("tfInstanceFilterNumClausesMax"); // NOI18N
 
         tfInstanceFilterRatioMax.setName("tfInstanceFilterRatioMax"); // NOI18N
 
         tfInstanceFilterMaxClauseLengthMax.setName("tfInstanceFilterMaxClauseLengthMax"); // NOI18N
 
         jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
         jLabel1.setName("jLabel1"); // NOI18N
 
         jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
         jLabel2.setName("jLabel2"); // NOI18N
 
         jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
         jLabel3.setName("jLabel3"); // NOI18N
 
         jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
         jLabel4.setName("jLabel4"); // NOI18N
 
         jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
         jLabel5.setName("jLabel5"); // NOI18N
 
         jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
         jLabel6.setName("jLabel6"); // NOI18N
 
         jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
         jLabel7.setName("jLabel7"); // NOI18N
 
         jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
         jLabel9.setName("jLabel9"); // NOI18N
 
         javax.swing.GroupLayout panelFilterInstancesLayout = new javax.swing.GroupLayout(panelFilterInstances);
         panelFilterInstances.setLayout(panelFilterInstancesLayout);
         panelFilterInstancesLayout.setHorizontalGroup(
             panelFilterInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelFilterInstancesLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelFilterInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(panelFilterInstancesLayout.createSequentialGroup()
                         .addComponent(jlInstanceFilterNumAtoms)
                         .addGap(50, 50, 50)
                         .addComponent(jLabel1))
                     .addComponent(jlInstanceFilterName, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(panelFilterInstancesLayout.createSequentialGroup()
                         .addComponent(jlInstanceFilterNumClauses)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(jLabel3))
                     .addGroup(panelFilterInstancesLayout.createSequentialGroup()
                         .addComponent(jlInstanceFilterRatio, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(jLabel4))
                     .addGroup(panelFilterInstancesLayout.createSequentialGroup()
                         .addComponent(jlInstanceFilterMaxClauseLength)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(jLabel5)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelFilterInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(tfInstanceFilterNumAtomsMin, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(tfInstanceFilterNumClausesMin, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(tfInstanceFilterMaxClauseLengthMin, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(tfInstanceFilterRatioMin, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(tfInstanceFilterName, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(panelFilterInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel2)
                     .addComponent(jLabel6)
                     .addComponent(jLabel7)
                     .addComponent(jLabel9))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelFilterInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(tfInstanceFilterNumAtomsMax, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(tfInstanceFilterRatioMax, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(tfInstanceFilterMaxClauseLengthMax, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(tfInstanceFilterNumClausesMax, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         panelFilterInstancesLayout.setVerticalGroup(
             panelFilterInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelFilterInstancesLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelFilterInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(tfInstanceFilterName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jlInstanceFilterName))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelFilterInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(tfInstanceFilterNumAtomsMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel1)
                     .addComponent(jlInstanceFilterNumAtoms))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelFilterInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(tfInstanceFilterNumClausesMin, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jlInstanceFilterNumClauses)
                     .addComponent(jLabel3))
                 .addGap(8, 8, 8)
                 .addGroup(panelFilterInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jlInstanceFilterRatio)
                     .addGroup(panelFilterInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(tfInstanceFilterRatioMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel4)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelFilterInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jlInstanceFilterMaxClauseLength)
                     .addComponent(tfInstanceFilterMaxClauseLengthMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel5)))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelFilterInstancesLayout.createSequentialGroup()
                 .addContainerGap(38, Short.MAX_VALUE)
                 .addGroup(panelFilterInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel2)
                     .addComponent(tfInstanceFilterNumAtomsMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelFilterInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(tfInstanceFilterNumClausesMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel6))
                 .addGap(8, 8, 8)
                 .addGroup(panelFilterInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(tfInstanceFilterRatioMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel7))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelFilterInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(tfInstanceFilterMaxClauseLengthMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel9)))
         );
 
         panelButtonsInstances.setName("panelButtonsInstances"); // NOI18N
 
         btnAddInstances.setText(resourceMap.getString("btnAddInstances.text")); // NOI18N
         btnAddInstances.setName("btnAddInstances"); // NOI18N
         btnAddInstances.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddInstancesActionPerformed(evt);
             }
         });
 
         btnRemoveInstances.setText(resourceMap.getString("btnRemoveInstances.text")); // NOI18N
         btnRemoveInstances.setName("btnRemoveInstances"); // NOI18N
         btnRemoveInstances.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemoveInstancesActionPerformed(evt);
             }
         });
 
         btnRefreshTableInstances.setText(resourceMap.getString("btnRefreshTableInstances.text")); // NOI18N
         btnRefreshTableInstances.setName("btnRefreshTableInstances"); // NOI18N
         btnRefreshTableInstances.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRefreshTableInstancesActionPerformed(evt);
             }
         });
 
         btnFilterInstances.setText(resourceMap.getString("btnFilterInstances.text")); // NOI18N
         btnFilterInstances.setName("btnFilterInstances"); // NOI18N
         btnFilterInstances.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnFilterInstancesActionPerformed(evt);
             }
         });
 
         btnExportInstances.setText(resourceMap.getString("btnExportInstances.text")); // NOI18N
         btnExportInstances.setName("btnExportInstances"); // NOI18N
         btnExportInstances.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnExportInstancesActionPerformed(evt);
             }
         });
 
         btnAddToClass.setText(resourceMap.getString("btnAddToClass.text")); // NOI18N
         btnAddToClass.setName("btnAddToClass"); // NOI18N
         btnAddToClass.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddToClassActionPerformed(evt);
             }
         });
 
         btnRemoveFromClass.setText(resourceMap.getString("btnRemoveFromClass.text")); // NOI18N
         btnRemoveFromClass.setName("btnRemoveFromClass"); // NOI18N
         btnRemoveFromClass.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemoveFromClassActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout panelButtonsInstancesLayout = new javax.swing.GroupLayout(panelButtonsInstances);
         panelButtonsInstances.setLayout(panelButtonsInstancesLayout);
         panelButtonsInstancesLayout.setHorizontalGroup(
             panelButtonsInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelButtonsInstancesLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnAddInstances)
                 .addGap(10, 10, 10)
                 .addComponent(btnRemoveInstances)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(btnRefreshTableInstances)
                 .addGap(10, 10, 10)
                 .addComponent(btnFilterInstances)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(btnExportInstances)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnAddToClass)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnRemoveFromClass)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         panelButtonsInstancesLayout.setVerticalGroup(
             panelButtonsInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelButtonsInstancesLayout.createSequentialGroup()
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(panelButtonsInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnRemoveInstances)
                     .addComponent(btnFilterInstances)
                     .addComponent(btnAddInstances)
                     .addComponent(btnExportInstances)
                     .addComponent(btnAddToClass)
                     .addComponent(btnRemoveFromClass)
                     .addComponent(btnRefreshTableInstances))
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout panelInstanceLayout = new javax.swing.GroupLayout(panelInstance);
         panelInstance.setLayout(panelInstanceLayout);
         panelInstanceLayout.setHorizontalGroup(
             panelInstanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelInstanceLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelInstanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(panelInstanceLayout.createSequentialGroup()
                         .addComponent(panelInstanceTable, javax.swing.GroupLayout.PREFERRED_SIZE, 653, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap())
                     .addGroup(panelInstanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(panelInstanceLayout.createSequentialGroup()
                             .addComponent(panelButtonsInstances, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addGap(26, 26, 26))
                         .addGroup(panelInstanceLayout.createSequentialGroup()
                             .addComponent(panelFilterInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addContainerGap(293, Short.MAX_VALUE)))))
         );
         panelInstanceLayout.setVerticalGroup(
             panelInstanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelInstanceLayout.createSequentialGroup()
                 .addComponent(panelInstanceTable, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(panelButtonsInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(11, 11, 11)
                 .addComponent(panelFilterInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         javax.swing.GroupLayout panelManageDBInstancesLayout = new javax.swing.GroupLayout(panelManageDBInstances);
         panelManageDBInstances.setLayout(panelManageDBInstancesLayout);
         panelManageDBInstancesLayout.setHorizontalGroup(
             panelManageDBInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelManageDBInstancesLayout.createSequentialGroup()
                 .addComponent(panelInstance, javax.swing.GroupLayout.DEFAULT_SIZE, 697, Short.MAX_VALUE)
                 .addGap(18, 18, 18)
                 .addComponent(panelInstanceClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         panelManageDBInstancesLayout.setVerticalGroup(
             panelManageDBInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelManageDBInstancesLayout.createSequentialGroup()
                 .addGroup(panelManageDBInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(panelManageDBInstancesLayout.createSequentialGroup()
                         .addGap(11, 11, 11)
                         .addComponent(panelInstanceClass, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addComponent(panelInstance, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE))
                 .addContainerGap())
         );
 
         manageDBPane.addTab("Instances", panelManageDBInstances);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(manageDBPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1271, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGap(18, 18, 18)
                 .addComponent(manageDBPane, javax.swing.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnAddInstancesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddInstancesActionPerformed
         manageDBInstances.addInstances();
         tableInstances.updateUI();
         tableInstanceClass.updateUI();
     }//GEN-LAST:event_btnAddInstancesActionPerformed
 
     private void btnRemoveInstancesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveInstancesActionPerformed
         try {
             manageDBInstances.removeInstances(tableInstances.getSelectedRows());
         } catch (Exception e) {
         }
         tableInstances.updateUI();
     }//GEN-LAST:event_btnRemoveInstancesActionPerformed
 
     private void btnRefreshTableInstancesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshTableInstancesActionPerformed
         tableInstances.updateUI();
     }//GEN-LAST:event_btnRefreshTableInstancesActionPerformed
 
     private void btnFilterInstancesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterInstancesActionPerformed
         if (panelFilterInstances.isVisible()) {
             clearFilter();
         } else {
             tableInstances.setRowSorter(sorter);
             addDocumentListener(tfInstanceFilterName);
             addDocumentListener(tfInstanceFilterNumAtomsMin);
             addDocumentListener(tfInstanceFilterNumAtomsMax);
             addDocumentListener(tfInstanceFilterNumClausesMin);
             addDocumentListener(tfInstanceFilterNumClausesMax);
             addDocumentListener(tfInstanceFilterRatioMin);
             addDocumentListener(tfInstanceFilterRatioMax);
             addDocumentListener(tfInstanceFilterMaxClauseLengthMin);
             addDocumentListener(tfInstanceFilterMaxClauseLengthMax);
             panelFilterInstances.setVisible(true);
         }
     }//GEN-LAST:event_btnFilterInstancesActionPerformed
 
     private void panelFilterInstancesComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_panelFilterInstancesComponentAdded
         panelFilterInstances.setVisible(false);
     }//GEN-LAST:event_panelFilterInstancesComponentAdded
 
     private void tfInstanceFilterNameInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_tfInstanceFilterNameInputMethodTextChanged
     }//GEN-LAST:event_tfInstanceFilterNameInputMethodTextChanged
 
     private void btnSolverSaveToDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSolverSaveToDBActionPerformed
         try {
             manageDBSolvers.saveSolvers();
             for (Solver s : solverTableModel.getSolvers()) {
                 manageDBParameters.saveParameters(s);
             }
         } catch (SQLException ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "Solvers cannot be saved. There is a problem with the Database: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         } catch (FileNotFoundException ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "Solvers cannot be saved because a file couldn't be found: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         } catch (NoSolverBinarySpecifiedException ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "You have to specify a binary file for all solvers!",
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
     }//GEN-LAST:event_btnSolverSaveToDBActionPerformed
 
     private void btnSolverNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSolverNewActionPerformed
         manageDBSolvers.newSolver();
         tableSolver.getSelectionModel().setSelectionInterval(tableSolver.getRowCount() - 1, tableSolver.getRowCount() - 1);
         tableSolver.updateUI();
     }//GEN-LAST:event_btnSolverNewActionPerformed
     JFileChooser binaryFileChooser;
     private void btnSolverAddBinaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSolverAddBinaryActionPerformed
         try {
             if (binaryFileChooser == null) {
                 binaryFileChooser = new JFileChooser();
             }
             if (binaryFileChooser.showDialog(this, "Add Solver Binary") == JFileChooser.APPROVE_OPTION) {
                 manageDBSolvers.addSolverBinary(binaryFileChooser.getSelectedFile());
             }
         } catch (FileNotFoundException ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "The binary of the solver couldn't be found: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         } catch (Exception ex) {
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
     }//GEN-LAST:event_btnParametersNewActionPerformed
 
     /**
      * Handles the key pressed events of the textfields "solver name" and "solver description".
      * @param evt
      */
     private void solverChangedOnKey(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_solverChangedOnKey
         solverChanged();
     }//GEN-LAST:event_solverChangedOnKey
 
     /**
      * Applies the solver name and description and updates the UI of the table.
      */
     private void solverChanged() {
         manageDBSolvers.applySolver(tfSolverName.getText(), taSolverDescription.getText());
         tableSolver.updateUI();
     }
     private JFileChooser codeFileChooser;
     private void btnSolverAddCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSolverAddCodeActionPerformed
         try {
             if (codeFileChooser == null) {
                 codeFileChooser = new JFileChooser();
             }
             if (codeFileChooser.showDialog(this, "Add Solver Binary") == JFileChooser.APPROVE_OPTION) {
                 manageDBSolvers.addSolverCode(codeFileChooser.getSelectedFile());
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
         try {
             manageDBSolvers.removeSolver();
             manageDBParameters.removeParameters();
             tableSolver.getSelectionModel().clearSelection();
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "A solver couldn't be removed: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
         tableSolver.updateUI();
         tableParameters.updateUI();
     }//GEN-LAST:event_btnSolverDeleteActionPerformed
 
     private void btnExportInstancesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportInstancesActionPerformed
         try {
             if (tableInstances.getSelectedRowCount() == 0) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "No instances are selected: ",
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             } else {
                 manageDBInstances.exportInstances(tableInstances.getSelectedRows());
             }
         } catch (IOException ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "The instances couldn't be written: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         } catch (NoConnectionToDBException ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "No connection to database: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         } catch (SQLException ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "SQL-Exception: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         } catch (InstaceNotInDBException ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "There is a problem with the data consistency ",
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
     }//GEN-LAST:event_btnExportInstancesActionPerformed
 
     private void btnSelectAllInstanceClassesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectAllInstanceClassesActionPerformed
         manageDBInstances.SelectAllInstanceClass();
 
     }//GEN-LAST:event_btnSelectAllInstanceClassesActionPerformed
 
     private void btnRemoveInstanceClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveInstanceClassActionPerformed
         try {
             manageDBInstances.RemoveInstanceClass(tableInstanceClass.getSelectedRows());
             tableInstanceClass.updateUI();
         } catch (NoConnectionToDBException ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "No connection to database: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         } catch (SQLException ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "SQL-Exception: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         } catch (InstanceSourceClassHasInstance ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "Some of the selected classes couldn't be removed, because they are sourceclasses and"
                     + "contain still any instances.",
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
     }//GEN-LAST:event_btnRemoveInstanceClassActionPerformed
 
     private void btnParametersDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParametersDeleteActionPerformed
         if (tableParameters.getSelectedRow() == -1) {
             return;
         }
         Parameter p = parameterTableModel.getParameter(tableParameters.getSelectedRow());
         manageDBParameters.removeParameter(p);
         tableParameters.clearSelection();
         showParameterDetails(null);
         tableParameters.updateUI();
     }//GEN-LAST:event_btnParametersDeleteActionPerformed
 
     private void btnSolverRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSolverRefreshActionPerformed
         if (JOptionPane.showConfirmDialog(this,
                 "This will reload all data from DB. You are going to lose all your unsaved changes. Do you wish to continue?",
                 "Warning!",
                 JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
             try {
                 manageDBSolvers.loadSolvers();
                 manageDBParameters.loadParametersOfSolvers(solverTableModel.getSolvers());
                 tableSolver.updateUI();
                 tableParameters.updateUI();
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
 
     private void btnParametersRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParametersRefreshActionPerformed
         tableParameters.updateUI();
     }//GEN-LAST:event_btnParametersRefreshActionPerformed
 
     private void btnNewInstanceClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewInstanceClassActionPerformed
         manageDBInstances.addInstanceClasses();
         tableInstanceClass.updateUI();
     }//GEN-LAST:event_btnNewInstanceClassActionPerformed
 
     private void parameterChangedOnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_parameterChangedOnKeyPressed
         parameterChanged();
     }//GEN-LAST:event_parameterChangedOnKeyPressed
 
     private void parameterChangedOnFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_parameterChangedOnFocusLost
         parameterChanged();
     }//GEN-LAST:event_parameterChangedOnFocusLost
 
     private void btnAddToClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddToClassActionPerformed
         manageDBInstances.addInstancesToClass(tableInstances.getSelectedRows());
     }//GEN-LAST:event_btnAddToClassActionPerformed
 
     private void btnRemoveFromClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveFromClassActionPerformed
         manageDBInstances.RemoveInstanceFromInstanceClass(tableInstances.getSelectedRows());
     }//GEN-LAST:event_btnRemoveFromClassActionPerformed
     private JFileChooser exportFileChooser;
     private void btnExport(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExport
         if (exportFileChooser == null) {
             exportFileChooser = new JFileChooser();
             exportFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         }
         try {
             if (exportFileChooser.showDialog(this, "Export Solver to Directory") == JFileChooser.APPROVE_OPTION) {
                 if (tableSolver.getSelectedRow() > -1) {
                     manageDBSolvers.exportSolver(solverTableModel.getSolver(tableSolver.getSelectedRow()), exportFileChooser.getSelectedFile());
                     System.out.println("Selected: " + exportFileChooser.getSelectedFile());
                 }
             }
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "An error occured while exporting solver binary: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
     }//GEN-LAST:event_btnExport
 
     private void parameterChanged() {
         if (tableParameters.getSelectedRow() == -1) {
             return;
         }
         Parameter p = parameterTableModel.getParameter(tableParameters.getSelectedRow());
         p.setName(tfParametersName.getText());
         try {
             p.setOrder(Integer.parseInt(tfParametersOrder.getText()));
         } catch (NumberFormatException e) {
             if (!tfParametersOrder.getText().equals("")) {
                 tfParametersOrder.setText(Integer.toString(p.getOrder()));
             }
         }
         p.setPrefix(tfParametersPrefix.getText());
         tableParameters.updateUI();
     }
 
     public void showSolverDetails(Solver currentSolver) {
         boolean enabled = false;
         if (currentSolver != null) {
             enabled = true;
             tfSolverName.setText(currentSolver.getName());
             taSolverDescription.setText(currentSolver.getDescription());
             manageDBParameters.setCurrentSolver(currentSolver);
             tableParameters.updateUI();
         } else {
             tfSolverName.setText("");
             taSolverDescription.setText("");
             manageDBParameters.setCurrentSolver(currentSolver);
             tableParameters.updateUI();
         }
         jlSolverName.setEnabled(enabled);
         jlSolverDescription.setEnabled(enabled);
         jlSolverBinary.setEnabled(enabled);
         jlSolverCode.setEnabled(enabled);
         tfSolverName.setEnabled(enabled);
         taSolverDescription.setEnabled(enabled);
         btnSolverAddBinary.setEnabled(enabled);
         btnSolverAddCode.setEnabled(enabled);
         if (currentSolver != null) {
             parameterTableModel.setCurrentSolver(currentSolver);
             parameterTableModel.fireTableDataChanged();
         }
         btnParametersNew.setEnabled(enabled);
         btnParametersDelete.setEnabled(enabled);
         btnParametersRefresh.setEnabled(enabled);
         tableParameters.getSelectionModel().clearSelection();
         showParameterDetails(null);
     }
 
     public void showParameterDetails(Parameter currentParameter) {
         boolean enabled = false;
         if (currentParameter != null) {
             enabled = true;
             tfParametersName.setText(currentParameter.getName());
             tfParametersOrder.setText(Integer.toString(currentParameter.getOrder()));
             tfParametersPrefix.setText(currentParameter.getPrefix());
         } else {
             tfParametersName.setText("");
             tfParametersOrder.setText("");
             tfParametersPrefix.setText("");
         }
         tfParametersName.setEnabled(enabled);
         tfParametersPrefix.setEnabled(enabled);
         tfParametersOrder.setEnabled(enabled);
     }
 
     private void clearFilter() {
         if (panelFilterInstances.isVisible()) {
             manageDBInstances.removeFilter(tableInstances);
             panelFilterInstances.setVisible(false);
             tfInstanceFilterName.setText("");
             tfInstanceFilterNumAtomsMin.setText("");
             tfInstanceFilterNumClausesMin.setText("");
             tfInstanceFilterRatioMin.setText("");
             tfInstanceFilterMaxClauseLengthMin.setText("");
         }
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
     private javax.swing.JButton btnAddInstances;
     private javax.swing.JButton btnAddToClass;
     private javax.swing.JButton btnExportInstances;
     private javax.swing.JButton btnFilterInstances;
     private javax.swing.JButton btnNewInstanceClass;
     private javax.swing.JButton btnParametersDelete;
     private javax.swing.JButton btnParametersNew;
     private javax.swing.JButton btnParametersRefresh;
     private javax.swing.JButton btnRefreshTableInstances;
     private javax.swing.JButton btnRemoveFromClass;
     private javax.swing.JButton btnRemoveInstanceClass;
     private javax.swing.JButton btnRemoveInstances;
     private javax.swing.JButton btnSelectAllInstanceClasses;
     private javax.swing.JButton btnSolverAddBinary;
     private javax.swing.JButton btnSolverAddCode;
     private javax.swing.JButton btnSolverDelete;
     private javax.swing.JButton btnSolverExport;
     private javax.swing.JButton btnSolverNew;
     private javax.swing.JButton btnSolverRefresh;
     private javax.swing.JButton btnSolverSaveToDB;
     private javax.swing.JFileChooser jFileChooserManageDBExportInstance;
     private javax.swing.JFileChooser jFileChooserManageDBInstance;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JLabel jlInstanceFilterMaxClauseLength;
     private javax.swing.JLabel jlInstanceFilterName;
     private javax.swing.JLabel jlInstanceFilterNumAtoms;
     private javax.swing.JLabel jlInstanceFilterNumClauses;
     private javax.swing.JLabel jlInstanceFilterRatio;
     private javax.swing.JLabel jlParametersName;
     private javax.swing.JLabel jlParametersOrder;
     private javax.swing.JLabel jlParametersPrefix;
     private javax.swing.JLabel jlSolverBinary;
     private javax.swing.JLabel jlSolverCode;
     private javax.swing.JLabel jlSolverDescription;
     private javax.swing.JLabel jlSolverName;
     private javax.swing.JTabbedPane manageDBPane;
     private javax.swing.JPanel panelButtonsInstanceClass;
     private javax.swing.JPanel panelButtonsInstances;
     private javax.swing.JPanel panelFilterInstances;
     private javax.swing.JPanel panelInstance;
     private javax.swing.JPanel panelInstanceClass;
     private javax.swing.JScrollPane panelInstanceClassTable;
     private javax.swing.JScrollPane panelInstanceTable;
     private javax.swing.JPanel panelManageDBInstances;
     private javax.swing.JPanel panelManageDBSolver;
     private javax.swing.JPanel panelParameters;
     private javax.swing.JPanel panelSolver;
     private javax.swing.JPanel panelSolverButtons;
     private javax.swing.JTextArea taSolverDescription;
     private javax.swing.JTable tableInstanceClass;
     private javax.swing.JTable tableInstances;
     private javax.swing.JTable tableParameters;
     private javax.swing.JTable tableSolver;
     private javax.swing.JTextField tfInstanceFilterMaxClauseLengthMax;
     private javax.swing.JTextField tfInstanceFilterMaxClauseLengthMin;
     private javax.swing.JTextField tfInstanceFilterName;
     private javax.swing.JTextField tfInstanceFilterNumAtomsMax;
     private javax.swing.JTextField tfInstanceFilterNumAtomsMin;
     private javax.swing.JTextField tfInstanceFilterNumClausesMax;
     private javax.swing.JTextField tfInstanceFilterNumClausesMin;
     private javax.swing.JTextField tfInstanceFilterRatioMax;
     private javax.swing.JTextField tfInstanceFilterRatioMin;
     private javax.swing.JTextField tfParametersName;
     private javax.swing.JTextField tfParametersOrder;
     private javax.swing.JTextField tfParametersPrefix;
     private javax.swing.JTextField tfSolverName;
     // End of variables declaration//GEN-END:variables
 }
