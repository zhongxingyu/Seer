 /*
  * EDACCExperimentMode.java
  *
  * Created on 02.01.2010, 00:25:47
  */
 package edacc;
 
 import edacc.experiment.AnalysePanel;
 import edacc.experiment.ExperimentInstanceClassTableModel;
 import edacc.experiment.ExperimentController;
 import edacc.experiment.ExperimentResultsBrowserTableModel;
 import edacc.experiment.ExperimentResultsBrowserTableModelRowFilter;
 import edacc.experiment.ExperimentTableModel;
 import edacc.experiment.InstanceTableModel;
 import edacc.experiment.InstanceTableModelRowFilter;
 import edacc.experiment.SolverTableModel;
 import edacc.gridqueues.GridQueuesController;
 import edacc.model.DatabaseConnector;
 import edacc.model.Experiment;
 import edacc.model.GridQueue;
 import edacc.model.Solver;
 import edacc.model.TaskCancelledException;
 import edacc.model.Tasks;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Timer;
 import java.util.TimerTask;
 import javax.swing.JFileChooser;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.DefaultRowSorter;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.SwingUtilities;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.table.TableRowSorter;
 import org.jdesktop.application.Action;
 import javax.swing.KeyStroke;
 import javax.swing.RowSorter.SortKey;
 import javax.swing.SortOrder;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableModel;
 
 /**
  *
  * @author simon
  */
 public class EDACCExperimentMode extends javax.swing.JPanel implements EDACCTaskEvents {
 
     public ExperimentController expController;
     public ExperimentTableModel expTableModel;
     public InstanceTableModel insTableModel;
     public SolverTableModel solTableModel;
     public ExperimentInstanceClassTableModel instanceClassModel;
     public ExperimentResultsBrowserTableModel jobsTableModel;
     public EDACCSolverConfigPanel solverConfigPanel;
     public TableRowSorter<InstanceTableModel> sorter;
     public InstanceTableModelRowFilter rowFilter;
     public ExperimentResultsBrowserTableModelRowFilter resultBrowserRowFilter;
     private ResultsBrowserTableRowSorter resultsBrowserTableRowSorter;
     private EDACCInstanceFilter dialogFilter;
     private AnalysePanel analysePanel;
     private Timer jobsTimer = null;
     private boolean jobsTimerWasActive = false;
 
     /** Creates new form EDACCExperimentMode */
     public EDACCExperimentMode() {
 
         initComponents();
         expController = new ExperimentController(this, solverConfigPanel);
         /* -------------------------------- experiment tab -------------------------------- */
         expTableModel = new ExperimentTableModel();
         tableExperiments.setModel(expTableModel);
         tableExperiments.setRowSorter(new TableRowSorter<ExperimentTableModel>(expTableModel));
         tableExperiments.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "");
         tableExperiments.addKeyListener(new KeyListener() {
 
             public void keyTyped(KeyEvent e) {
             }
 
             public void keyPressed(KeyEvent e) {
             }
 
             public void keyReleased(KeyEvent e) {
                 if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                     btnLoadExperiment();
                 }
             }
         });
         tableExperiments.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 
             public void valueChanged(ListSelectionEvent e) {
                 boolean mod = false;
                 if (tableExperiments.getSelectedRow() != -1) {
                     mod = true;
                 }
                 btnRemoveExperiment.setEnabled(mod);
                 btnEditExperiment.setEnabled(mod);
                 btnLoadExperiment.setEnabled(mod);
             }
         });
         /* -------------------------------- end of experiment tab -------------------------------- */
         /* -------------------------------- solver tab -------------------------------- */
         solTableModel = new SolverTableModel();
         tableSolvers.setModel(solTableModel);
         solverConfigPanel.setParent(this);
         /* -------------------------------- end of solver tab -------------------------------- */
         /* -------------------------------- instances tab -------------------------------- */
         insTableModel = new InstanceTableModel();
         insTableModel.addTableModelListener(new TableModelListener() {
 
             @Override
             public void tableChanged(TableModelEvent e) {
                 setTitles();
             }
         });
         tableInstances.setModel(insTableModel);
         instanceClassModel = new ExperimentInstanceClassTableModel(tableInstances, expController);
         tableInstanceClasses.setModel(instanceClassModel);
         sorter = new TableRowSorter<InstanceTableModel>(insTableModel);
         rowFilter = new InstanceTableModelRowFilter();
         tableInstances.setRowSorter(sorter);
         sorter.setRowFilter(rowFilter);
         // center third column
         tableInstanceClasses.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
 
             @Override
             public Component getTableCellRendererComponent(JTable table, Object value,
                     boolean isSelected, boolean hasFocus, int row, int column) {
                 JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                 lbl.setHorizontalAlignment(JLabel.CENTER);
                 return lbl;
             }
         });
         /* -------------------------------- end of instances tab -------------------------------- */
         /* -------------------------------- generate jobs tab -------------------------------- */
 
         /* -------------------------------- end of generate jobs tab -------------------------------- */
         /* -------------------------------- jobs browser tab -------------------------------- */
         jobsTableModel = new ExperimentResultsBrowserTableModel();
         tableJobs.setModel(jobsTableModel);
         resultsBrowserTableRowSorter = new ResultsBrowserTableRowSorter(jobsTableModel);
         resultsBrowserTableRowSorter.setSortsOnUpdates(true);
         resultBrowserRowFilter = new ExperimentResultsBrowserTableModelRowFilter();
         resultsBrowserTableRowSorter.setRowFilter(resultBrowserRowFilter);
         tableJobs.setRowSorter(resultsBrowserTableRowSorter);
         tableJobs.setDefaultRenderer(Object.class, new EDACCExperimentModeJobsCellRenderer());
         tableJobs.setDefaultRenderer(String.class, new EDACCExperimentModeJobsCellRenderer());
         tableJobs.setDefaultRenderer(Integer.class, new EDACCExperimentModeJobsCellRenderer());
         tableJobs.setDefaultRenderer(Float.class, new EDACCExperimentModeJobsCellRenderer());
         /* -------------------------------- end of jobs browser tab -------------------------------- */
         /* -------------------------------- analyze tab -------------------------------- */
         analysePanel = new AnalysePanel(expController);
         panelAnalyse.setViewportView(analysePanel);
         /* -------------------------------- end of analyze tab -------------------------------- */
 
 
         manageExperimentPane.setEnabledAt(1, false);
         manageExperimentPane.setEnabledAt(2, false);
         manageExperimentPane.setEnabledAt(3, false);
         manageExperimentPane.setEnabledAt(4, false);
         manageExperimentPane.setEnabledAt(5, false);
         disableEditExperiment();
         manageExperimentPane.setTitleAt(0, "Experiments");
         btnDiscardExperiment.setEnabled(false);
 
         GridQueuesController.getInstance().addObserver(new Observer() {
 
             public void update(Observable o, Object arg) {
                 if (GridQueuesController.getInstance().getChosenQueue() == null) {
                     btnGenerateJobs.setEnabled(false);
                     btnGeneratePackage.setEnabled(false);
                 } else {
                     btnGenerateJobs.setEnabled(true);
                     btnGeneratePackage.setEnabled(true);
                 }
             }
         });
         if (GridQueuesController.getInstance().getChosenQueue() == null) {
             btnGenerateJobs.setEnabled(false);
             btnGeneratePackage.setEnabled(false);
         } else {
             btnGenerateJobs.setEnabled(true);
             btnGeneratePackage.setEnabled(true);
         }
         DatabaseConnector.getInstance().addObserver(new Observer() {
 
             public void update(Observable o, Object arg) {
                 if (!DatabaseConnector.getInstance().isConnected()) {
                     if (((EDACCView) EDACCApp.getApplication().getMainView()).getMode() == EDACCExperimentMode.this) {
                         reinitializeGUI();
                        if (!"disconnect".equals(arg)) {
                            ((EDACCView) EDACCApp.getApplication().getMainView()).noMode();
                            javax.swing.JOptionPane.showMessageDialog(null, "Database connection lost.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                        }
                     }
                 }
             }
         });
     }
 
     public void reinitializeGUI() {

         expController.unloadExperiment();
         /* experiment tab */
         expTableModel.setExperiments(null);
         /* end of experiment tab */
         /* instance tab */
         btnDeselectAllInstnaceClassesActionPerformed(null);
         rowFilter.filter_maxClauseLength = false;
         rowFilter.filter_name = false;
         rowFilter.filter_numAtoms = false;
         rowFilter.filter_numClauses = false;
         rowFilter.filter_ratio = false;
         lblFilterStatus.setText("");
         /* end of instance tab */
         /* job browser tab */
        try {
            jobsTableModel.setJobs(null);
        } catch (SQLException ex) {
        }
         resultBrowserRowFilter.setInstanceName(null);
         resultBrowserRowFilter.setSolverName(null);
         resultBrowserRowFilter.setStatusCode(null);
         boolean[] columnVis = jobsTableModel.getColumnVisibility();
         for (int i = 0; i < columnVis.length; i++) {
             columnVis[i] = true;
         }
         jobsTableModel.setColumnVisibility(columnVis);
         setJobsFilterStatus("");
         jobsTimerWasActive = false;
         /* end of job browser tab */
     }
 
     public void initialize() throws SQLException {
         btnRemoveExperiment.setEnabled(false);
         btnEditExperiment.setEnabled(false);
         btnLoadExperiment.setEnabled(false);
         expController.initialize();
     }
 
     public void disableEditExperiment() {
         pnlEditExperiment.setEnabled(false);
         txtMaxMem.setText("");
         txtMaxMem.setEnabled(false);
         txtTimeout.setText("");
         txtTimeout.setEnabled(false);
         txtMaxSeeds.setText("");
         txtMaxSeeds.setEnabled(false);
         chkGenerateSeeds.setSelected(false);
         chkGenerateSeeds.setEnabled(false);
         chkLinkSeeds.setSelected(false);
         chkLinkSeeds.setEnabled(false);
         btnEditExperimentSave.setEnabled(false);
         btnEditExperimentUndo.setEnabled(false);
     }
 
     public void enableEditExperiment(Integer maxMem, Integer timeout, Integer maxSeed, boolean generateSeeds, boolean linkSeeds) {
         pnlEditExperiment.setEnabled(true);
         txtMaxMem.setText(maxMem.toString());
         txtMaxMem.setEnabled(true);
         txtTimeout.setText(timeout.toString());
         txtTimeout.setEnabled(true);
         txtMaxSeeds.setText(maxSeed.toString());
         txtMaxSeeds.setEnabled(true);
         chkGenerateSeeds.setSelected(generateSeeds);
         chkGenerateSeeds.setEnabled(true);
         chkLinkSeeds.setSelected(linkSeeds);
         chkLinkSeeds.setEnabled(true);
         btnEditExperimentSave.setEnabled(true);
         btnEditExperimentUndo.setEnabled(true);
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         manageExperimentPane = new javax.swing.JTabbedPane();
         panelManageExperiment = new javax.swing.JPanel();
         scrollPaneExperimentsTable = new javax.swing.JScrollPane();
         tableExperiments = new javax.swing.JTable();
         pnlEditExperiment = new javax.swing.JPanel();
         jLabel2 = new javax.swing.JLabel();
         txtTimeout = new javax.swing.JTextField();
         txtMaxMem = new javax.swing.JTextField();
         jLabel4 = new javax.swing.JLabel();
         chkGenerateSeeds = new javax.swing.JCheckBox();
         chkLinkSeeds = new javax.swing.JCheckBox();
         jLabel3 = new javax.swing.JLabel();
         txtMaxSeeds = new javax.swing.JTextField();
         btnEditExperimentSave = new javax.swing.JButton();
         btnEditExperimentUndo = new javax.swing.JButton();
         jPanel7 = new javax.swing.JPanel();
         btnDiscardExperiment = new javax.swing.JButton();
         btnRemoveExperiment = new javax.swing.JButton();
         btnLoadExperiment = new javax.swing.JButton();
         btnCreateExperiment = new javax.swing.JButton();
         btnEditExperiment = new javax.swing.JButton();
         panelChooseSolver = new javax.swing.JPanel();
         jSplitPane1 = new javax.swing.JSplitPane();
         jPanel2 = new javax.swing.JPanel();
         jScrollPane3 = new javax.swing.JScrollPane();
         tableSolvers = new javax.swing.JTable();
         jScrollPane4 = new javax.swing.JScrollPane();
         btnSaveSolverConfigurations = new javax.swing.JButton();
         btnSelectAllSolvers = new javax.swing.JButton();
         btnDeselectAllSolvers = new javax.swing.JButton();
         btnReverseSolverSelection = new javax.swing.JButton();
         btnChooseSolvers = new javax.swing.JButton();
         btnUndoSolverConfigurations = new javax.swing.JButton();
         panelChooseInstances = new javax.swing.JPanel();
         jSplitPane2 = new javax.swing.JSplitPane();
         jPanel4 = new javax.swing.JPanel();
         jScrollPane5 = new javax.swing.JScrollPane();
         tableInstanceClasses = new javax.swing.JTable();
         jPanel6 = new javax.swing.JPanel();
         btnDeselectAllInstnaceClasses = new javax.swing.JButton();
         btnSelectAllInstanceClasses = new javax.swing.JButton();
         jPanel3 = new javax.swing.JPanel();
         jScrollPane2 = new javax.swing.JScrollPane();
         tableInstances = new javax.swing.JTable();
         jPanel5 = new javax.swing.JPanel();
         btnSaveInstances = new javax.swing.JButton();
         btnFilterInstances = new javax.swing.JButton();
         btnSelectAllInstances = new javax.swing.JButton();
         btnDeselectAllInstances = new javax.swing.JButton();
         btnInvertSelection = new javax.swing.JButton();
         btnUndoInstances = new javax.swing.JButton();
         btnSelectedInstances = new javax.swing.JButton();
         lblFilterStatus = new javax.swing.JLabel();
         panelExperimentParams = new javax.swing.JPanel();
         jPanel1 = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         txtNumRuns = new javax.swing.JTextField();
         btnGenerateJobs = new javax.swing.JButton();
         lblNumJobs = new javax.swing.JLabel();
         lblCurNumRuns = new javax.swing.JLabel();
         btnGeneratePackage = new javax.swing.JButton();
         btnSelectQueue = new javax.swing.JButton();
         panelJobBrowser = new javax.swing.JPanel();
         jScrollPane6 = new javax.swing.JScrollPane();
         tableJobs = new javax.swing.JTable();
         btnRefreshJobs = new javax.swing.JButton();
         btnBrowserColumnSelection = new javax.swing.JButton();
         btnFilterJobs = new javax.swing.JButton();
         btnCSVExport = new javax.swing.JButton();
         lblJobsFilterStatus = new javax.swing.JLabel();
         txtJobsTimer = new javax.swing.JTextField();
         chkJobsTimer = new javax.swing.JCheckBox();
         jLabel5 = new javax.swing.JLabel();
         panelAnalyse = new javax.swing.JScrollPane();
 
         setName("Form"); // NOI18N
         setPreferredSize(new java.awt.Dimension(500, 500));
 
         manageExperimentPane.setName("manageExperimentPane"); // NOI18N
         manageExperimentPane.setNextFocusableComponent(tableExperiments);
         manageExperimentPane.setPreferredSize(new java.awt.Dimension(0, 0));
         manageExperimentPane.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 manageExperimentPaneStateChanged(evt);
             }
         });
 
         panelManageExperiment.setName("panelManageExperiment"); // NOI18N
         panelManageExperiment.setPreferredSize(new java.awt.Dimension(0, 0));
 
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(edacc.EDACCApp.class).getContext().getResourceMap(EDACCExperimentMode.class);
         scrollPaneExperimentsTable.setToolTipText(resourceMap.getString("scrollPaneExperimentsTable.toolTipText")); // NOI18N
         scrollPaneExperimentsTable.setName("scrollPaneExperimentsTable"); // NOI18N
         scrollPaneExperimentsTable.setNextFocusableComponent(btnLoadExperiment);
         scrollPaneExperimentsTable.setPreferredSize(new java.awt.Dimension(0, 0));
 
         tableExperiments.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
 
             }
         ));
         tableExperiments.setName("tableExperiments"); // NOI18N
         tableExperiments.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         tableExperiments.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tableExperimentsMouseClicked(evt);
             }
         });
         scrollPaneExperimentsTable.setViewportView(tableExperiments);
 
         pnlEditExperiment.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlEditExperiment.border.title"))); // NOI18N
         pnlEditExperiment.setMinimumSize(new java.awt.Dimension(0, 0));
         pnlEditExperiment.setName("pnlEditExperiment"); // NOI18N
         pnlEditExperiment.setPreferredSize(new java.awt.Dimension(0, 0));
 
         jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
         jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
         jLabel2.setName("jLabel2"); // NOI18N
 
         txtTimeout.setText(resourceMap.getString("txtTimeout.text")); // NOI18N
         txtTimeout.setToolTipText(resourceMap.getString("txtTimeout.toolTipText")); // NOI18N
         txtTimeout.setName("txtTimeout"); // NOI18N
         txtTimeout.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txtTimeoutKeyReleased(evt);
             }
         });
 
         txtMaxMem.setEditable(false);
         txtMaxMem.setText(resourceMap.getString("txtMaxMem.text")); // NOI18N
         txtMaxMem.setToolTipText(resourceMap.getString("txtMaxMem.toolTipText")); // NOI18N
         txtMaxMem.setName("txtMaxMem"); // NOI18N
         txtMaxMem.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txtMaxMemKeyReleased(evt);
             }
         });
 
         jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
         jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
         jLabel4.setName("jLabel4"); // NOI18N
 
         chkGenerateSeeds.setToolTipText(resourceMap.getString("chkGenerateSeeds.toolTipText")); // NOI18N
         chkGenerateSeeds.setLabel(resourceMap.getString("chkGenerateSeeds.label")); // NOI18N
         chkGenerateSeeds.setName("chkGenerateSeeds"); // NOI18N
         chkGenerateSeeds.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 chkGenerateSeedsMouseReleased(evt);
             }
         });
 
         chkLinkSeeds.setToolTipText(resourceMap.getString("chkLinkSeeds.toolTipText")); // NOI18N
         chkLinkSeeds.setLabel(resourceMap.getString("chkLinkSeeds.label")); // NOI18N
         chkLinkSeeds.setName("chkLinkSeeds"); // NOI18N
         chkLinkSeeds.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 chkLinkSeedsMouseReleased(evt);
             }
         });
 
         jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
         jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
         jLabel3.setName("jLabel3"); // NOI18N
 
         txtMaxSeeds.setText(resourceMap.getString("txtMaxSeeds.text")); // NOI18N
         txtMaxSeeds.setToolTipText(resourceMap.getString("txtMaxSeeds.toolTipText")); // NOI18N
         txtMaxSeeds.setName("txtMaxSeeds"); // NOI18N
         txtMaxSeeds.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txtMaxSeedsKeyReleased(evt);
             }
         });
 
         javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(edacc.EDACCApp.class).getContext().getActionMap(EDACCExperimentMode.class, this);
         btnEditExperimentSave.setAction(actionMap.get("btnEditExperimentSave")); // NOI18N
         btnEditExperimentSave.setText(resourceMap.getString("btnEditExperimentSave.text")); // NOI18N
         btnEditExperimentSave.setToolTipText(resourceMap.getString("btnEditExperimentSave.toolTipText")); // NOI18N
         btnEditExperimentSave.setMaximumSize(new java.awt.Dimension(57, 25));
         btnEditExperimentSave.setMinimumSize(new java.awt.Dimension(57, 25));
         btnEditExperimentSave.setName("btnEditExperimentSave"); // NOI18N
         btnEditExperimentSave.setPreferredSize(new java.awt.Dimension(57, 25));
 
         btnEditExperimentUndo.setAction(actionMap.get("btnEditExperimentUndo")); // NOI18N
         btnEditExperimentUndo.setText(resourceMap.getString("btnEditExperimentUndo.text")); // NOI18N
         btnEditExperimentUndo.setToolTipText(resourceMap.getString("btnEditExperimentUndo.toolTipText")); // NOI18N
         btnEditExperimentUndo.setMaximumSize(new java.awt.Dimension(57, 25));
         btnEditExperimentUndo.setMinimumSize(new java.awt.Dimension(57, 25));
         btnEditExperimentUndo.setName("btnEditExperimentUndo"); // NOI18N
         btnEditExperimentUndo.setPreferredSize(new java.awt.Dimension(57, 25));
 
         javax.swing.GroupLayout pnlEditExperimentLayout = new javax.swing.GroupLayout(pnlEditExperiment);
         pnlEditExperiment.setLayout(pnlEditExperimentLayout);
         pnlEditExperimentLayout.setHorizontalGroup(
             pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlEditExperimentLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlEditExperimentLayout.createSequentialGroup()
                         .addGroup(pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(txtMaxSeeds, javax.swing.GroupLayout.DEFAULT_SIZE, 924, Short.MAX_VALUE)
                             .addComponent(txtTimeout, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 924, Short.MAX_VALUE)
                             .addComponent(txtMaxMem, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 924, Short.MAX_VALUE)
                             .addComponent(chkGenerateSeeds, javax.swing.GroupLayout.DEFAULT_SIZE, 924, Short.MAX_VALUE)
                             .addComponent(chkLinkSeeds, javax.swing.GroupLayout.DEFAULT_SIZE, 924, Short.MAX_VALUE)))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEditExperimentLayout.createSequentialGroup()
                         .addComponent(btnEditExperimentUndo, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(btnEditExperimentSave, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
 
         pnlEditExperimentLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnEditExperimentSave, btnEditExperimentUndo});
 
         pnlEditExperimentLayout.setVerticalGroup(
             pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlEditExperimentLayout.createSequentialGroup()
                 .addGroup(pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(jLabel2)
                     .addComponent(txtTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(jLabel4)
                     .addComponent(txtMaxMem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(jLabel3)
                     .addComponent(txtMaxSeeds, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(chkGenerateSeeds)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(chkLinkSeeds)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnEditExperimentUndo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnEditExperimentSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jPanel7.setName("jPanel7"); // NOI18N
         jPanel7.setPreferredSize(new java.awt.Dimension(0, 0));
 
         btnDiscardExperiment.setAction(actionMap.get("btnDiscardExperiment")); // NOI18N
         btnDiscardExperiment.setText(resourceMap.getString("btnDiscardExperiment.text")); // NOI18N
         btnDiscardExperiment.setToolTipText(resourceMap.getString("btnDiscardExperiment.toolTipText")); // NOI18N
         btnDiscardExperiment.setMaximumSize(new java.awt.Dimension(69, 23));
         btnDiscardExperiment.setMinimumSize(new java.awt.Dimension(69, 23));
         btnDiscardExperiment.setName("btnDiscardExperiment"); // NOI18N
         btnDiscardExperiment.setPreferredSize(new java.awt.Dimension(80, 25));
 
         btnRemoveExperiment.setAction(actionMap.get("btnRemoveExperiment")); // NOI18N
         btnRemoveExperiment.setText(resourceMap.getString("btnRemoveExperiment.text")); // NOI18N
         btnRemoveExperiment.setToolTipText(resourceMap.getString("btnRemoveExperiment.toolTipText")); // NOI18N
         btnRemoveExperiment.setName("btnRemoveExperiment"); // NOI18N
         btnRemoveExperiment.setPreferredSize(new java.awt.Dimension(80, 25));
 
         btnLoadExperiment.setAction(actionMap.get("btnLoadExperiment")); // NOI18N
         btnLoadExperiment.setText(resourceMap.getString("btnLoadExperiment.text")); // NOI18N
         btnLoadExperiment.setToolTipText(resourceMap.getString("btnLoadExperiment.toolTipText")); // NOI18N
         btnLoadExperiment.setName("btnLoadExperiment"); // NOI18N
         btnLoadExperiment.setPreferredSize(new java.awt.Dimension(80, 25));
 
         btnCreateExperiment.setAction(actionMap.get("btnCreateExperiment")); // NOI18N
         btnCreateExperiment.setText(resourceMap.getString("btnCreateExperiment.text")); // NOI18N
         btnCreateExperiment.setToolTipText(resourceMap.getString("btnCreateExperiment.toolTipText")); // NOI18N
         btnCreateExperiment.setName("btnCreateExperiment"); // NOI18N
         btnCreateExperiment.setPreferredSize(new java.awt.Dimension(80, 25));
 
         btnEditExperiment.setAction(actionMap.get("btnEditExperiment")); // NOI18N
         btnEditExperiment.setText(resourceMap.getString("btnEditExperiment.text")); // NOI18N
         btnEditExperiment.setToolTipText(resourceMap.getString("btnEditExperiment.toolTipText")); // NOI18N
         btnEditExperiment.setMaximumSize(new java.awt.Dimension(71, 23));
         btnEditExperiment.setMinimumSize(new java.awt.Dimension(71, 23));
         btnEditExperiment.setName("btnEditExperiment"); // NOI18N
         btnEditExperiment.setPreferredSize(new java.awt.Dimension(80, 25));
 
         javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
         jPanel7.setLayout(jPanel7Layout);
         jPanel7Layout.setHorizontalGroup(
             jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                 .addContainerGap(540, Short.MAX_VALUE)
                 .addComponent(btnCreateExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(btnRemoveExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(btnEditExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(btnDiscardExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(btnLoadExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         jPanel7Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCreateExperiment, btnDiscardExperiment, btnLoadExperiment, btnRemoveExperiment});
 
         jPanel7Layout.setVerticalGroup(
             jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel7Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnLoadExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnDiscardExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnEditExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnRemoveExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnCreateExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(13, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout panelManageExperimentLayout = new javax.swing.GroupLayout(panelManageExperiment);
         panelManageExperiment.setLayout(panelManageExperimentLayout);
         panelManageExperimentLayout.setHorizontalGroup(
             panelManageExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelManageExperimentLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelManageExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(pnlEditExperiment, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1050, Short.MAX_VALUE)
                     .addComponent(scrollPaneExperimentsTable, javax.swing.GroupLayout.DEFAULT_SIZE, 1050, Short.MAX_VALUE)
                     .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1050, Short.MAX_VALUE))
                 .addContainerGap())
         );
         panelManageExperimentLayout.setVerticalGroup(
             panelManageExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelManageExperimentLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(scrollPaneExperimentsTable, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(pnlEditExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         manageExperimentPane.addTab("Experiments", panelManageExperiment);
 
         panelChooseSolver.setName("panelChooseSolver"); // NOI18N
 
         jSplitPane1.setDividerLocation(0.5);
         jSplitPane1.setResizeWeight(0.4);
         jSplitPane1.setName("jSplitPane1"); // NOI18N
 
         jPanel2.setName("jPanel2"); // NOI18N
 
         jScrollPane3.setToolTipText(resourceMap.getString("jScrollPane3.toolTipText")); // NOI18N
         jScrollPane3.setName("jScrollPane3"); // NOI18N
 
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
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 996, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
         );
 
         jSplitPane1.setLeftComponent(jPanel2);
 
         jScrollPane4.setToolTipText(resourceMap.getString("jScrollPane4.toolTipText")); // NOI18N
         jScrollPane4.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         jScrollPane4.setName("jScrollPane4"); // NOI18N
         solverConfigPanel = new EDACCSolverConfigPanel();
         jScrollPane4.setViewportView(solverConfigPanel);
         jScrollPane4.getVerticalScrollBar().setUnitIncrement(30);
         jSplitPane1.setRightComponent(jScrollPane4);
 
         btnSaveSolverConfigurations.setAction(actionMap.get("btnSaveSolverConfigurations")); // NOI18N
         btnSaveSolverConfigurations.setText(resourceMap.getString("btnSaveSolverConfigurations.text")); // NOI18N
         btnSaveSolverConfigurations.setToolTipText(resourceMap.getString("btnSaveSolverConfigurations.toolTipText")); // NOI18N
         btnSaveSolverConfigurations.setName("btnSaveSolverConfigurations"); // NOI18N
         btnSaveSolverConfigurations.setPreferredSize(new java.awt.Dimension(109, 25));
 
         btnSelectAllSolvers.setAction(actionMap.get("btnSelectAllSolvers")); // NOI18N
         btnSelectAllSolvers.setText(resourceMap.getString("btnSelectAllSolvers.text")); // NOI18N
         btnSelectAllSolvers.setToolTipText(resourceMap.getString("btnSelectAllSolvers.toolTipText")); // NOI18N
         btnSelectAllSolvers.setName("btnSelectAllSolvers"); // NOI18N
         btnSelectAllSolvers.setPreferredSize(new java.awt.Dimension(109, 25));
 
         btnDeselectAllSolvers.setAction(actionMap.get("btnDeselectAll")); // NOI18N
         btnDeselectAllSolvers.setText(resourceMap.getString("btnDeselectAllSolvers.text")); // NOI18N
         btnDeselectAllSolvers.setToolTipText(resourceMap.getString("btnDeselectAllSolvers.toolTipText")); // NOI18N
         btnDeselectAllSolvers.setName("btnDeselectAllSolvers"); // NOI18N
         btnDeselectAllSolvers.setPreferredSize(new java.awt.Dimension(109, 25));
 
         btnReverseSolverSelection.setAction(actionMap.get("btnReverseSolverSelection")); // NOI18N
         btnReverseSolverSelection.setText(resourceMap.getString("btnReverseSolverSelection.text")); // NOI18N
         btnReverseSolverSelection.setToolTipText(resourceMap.getString("btnReverseSolverSelection.toolTipText")); // NOI18N
         btnReverseSolverSelection.setName("btnReverseSolverSelection"); // NOI18N
         btnReverseSolverSelection.setPreferredSize(new java.awt.Dimension(109, 25));
 
         btnChooseSolvers.setAction(actionMap.get("btnChooseSolvers")); // NOI18N
         btnChooseSolvers.setText(resourceMap.getString("btnChooseSolvers.text")); // NOI18N
         btnChooseSolvers.setToolTipText(resourceMap.getString("btnChooseSolvers.toolTipText")); // NOI18N
         btnChooseSolvers.setName("btnChooseSolvers"); // NOI18N
         btnChooseSolvers.setPreferredSize(new java.awt.Dimension(109, 25));
 
         btnUndoSolverConfigurations.setAction(actionMap.get("btnUndoSolverConfigurations")); // NOI18N
         btnUndoSolverConfigurations.setText(resourceMap.getString("btnUndoSolverConfigurations.text")); // NOI18N
         btnUndoSolverConfigurations.setToolTipText(resourceMap.getString("btnUndoSolverConfigurations.toolTipText")); // NOI18N
         btnUndoSolverConfigurations.setName("btnUndoSolverConfigurations"); // NOI18N
         btnUndoSolverConfigurations.setPreferredSize(new java.awt.Dimension(109, 25));
 
         javax.swing.GroupLayout panelChooseSolverLayout = new javax.swing.GroupLayout(panelChooseSolver);
         panelChooseSolver.setLayout(panelChooseSolverLayout);
         panelChooseSolverLayout.setHorizontalGroup(
             panelChooseSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelChooseSolverLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelChooseSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1050, Short.MAX_VALUE)
                     .addGroup(panelChooseSolverLayout.createSequentialGroup()
                         .addComponent(btnSelectAllSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnDeselectAllSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnReverseSolverSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnChooseSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 372, Short.MAX_VALUE)
                         .addComponent(btnUndoSolverConfigurations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnSaveSolverConfigurations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         panelChooseSolverLayout.setVerticalGroup(
             panelChooseSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelChooseSolverLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelChooseSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnSelectAllSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnDeselectAllSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnReverseSolverSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnSaveSolverConfigurations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnChooseSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnUndoSolverConfigurations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         manageExperimentPane.addTab("Solvers", panelChooseSolver);
 
         panelChooseInstances.setName("panelChooseInstances"); // NOI18N
         panelChooseInstances.setPreferredSize(new java.awt.Dimension(668, 623));
 
         jSplitPane2.setDividerLocation(0.4);
         jSplitPane2.setResizeWeight(0.4);
         jSplitPane2.setName("jSplitPane2"); // NOI18N
 
         jPanel4.setName("jPanel4"); // NOI18N
 
         jScrollPane5.setName("jScrollPane5"); // NOI18N
 
         tableInstanceClasses.setModel(new javax.swing.table.DefaultTableModel(
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
         tableInstanceClasses.setToolTipText(resourceMap.getString("tableInstanceClasses.toolTipText")); // NOI18N
         tableInstanceClasses.setName("tableInstanceClasses"); // NOI18N
         jScrollPane5.setViewportView(tableInstanceClasses);
 
         jPanel6.setName("jPanel6"); // NOI18N
 
         btnDeselectAllInstnaceClasses.setText(resourceMap.getString("btnDeselectAllInstnaceClasses.text")); // NOI18N
         btnDeselectAllInstnaceClasses.setToolTipText(resourceMap.getString("btnDeselectAllInstnaceClasses.toolTipText")); // NOI18N
         btnDeselectAllInstnaceClasses.setName("btnDeselectAllInstnaceClasses"); // NOI18N
         btnDeselectAllInstnaceClasses.setPreferredSize(new java.awt.Dimension(109, 25));
         btnDeselectAllInstnaceClasses.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnDeselectAllInstnaceClassesActionPerformed(evt);
             }
         });
 
         btnSelectAllInstanceClasses.setText(resourceMap.getString("btnSelectAllInstanceClasses.text")); // NOI18N
         btnSelectAllInstanceClasses.setToolTipText(resourceMap.getString("btnSelectAllInstanceClasses.toolTipText")); // NOI18N
         btnSelectAllInstanceClasses.setName("btnSelectAllInstanceClasses"); // NOI18N
         btnSelectAllInstanceClasses.setPreferredSize(new java.awt.Dimension(109, 25));
         btnSelectAllInstanceClasses.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSelectAllInstanceClassesActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
         jPanel6.setLayout(jPanel6Layout);
         jPanel6Layout.setHorizontalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnSelectAllInstanceClasses, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnDeselectAllInstnaceClasses, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel6Layout.setVerticalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnSelectAllInstanceClasses, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnDeselectAllInstnaceClasses, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(14, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addGap(10, 10, 10)
                 .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         jSplitPane2.setLeftComponent(jPanel4);
 
         jPanel3.setName("jPanel3"); // NOI18N
 
         jScrollPane2.setToolTipText(resourceMap.getString("jScrollPane2.toolTipText")); // NOI18N
         jScrollPane2.setName("jScrollPane2"); // NOI18N
 
         tableInstances.setModel(new javax.swing.table.DefaultTableModel(
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
         tableInstances.setName("tableInstances"); // NOI18N
         jScrollPane2.setViewportView(tableInstances);
 
         jPanel5.setName("jPanel5"); // NOI18N
 
         btnSaveInstances.setAction(actionMap.get("btnSaveInstances")); // NOI18N
         btnSaveInstances.setText(resourceMap.getString("btnSaveInstances.text")); // NOI18N
         btnSaveInstances.setToolTipText(resourceMap.getString("btnSaveInstances.toolTipText")); // NOI18N
         btnSaveInstances.setMaximumSize(new java.awt.Dimension(109, 23));
         btnSaveInstances.setMinimumSize(new java.awt.Dimension(109, 23));
         btnSaveInstances.setName("btnSaveInstances"); // NOI18N
         btnSaveInstances.setPreferredSize(new java.awt.Dimension(109, 25));
 
         btnFilterInstances.setAction(actionMap.get("btnInstanceFilter")); // NOI18N
         btnFilterInstances.setText(resourceMap.getString("btnFilterInstances.text")); // NOI18N
         btnFilterInstances.setToolTipText(resourceMap.getString("btnFilterInstances.toolTipText")); // NOI18N
         btnFilterInstances.setMaximumSize(new java.awt.Dimension(109, 23));
         btnFilterInstances.setMinimumSize(new java.awt.Dimension(109, 23));
         btnFilterInstances.setName("btnFilterInstances"); // NOI18N
         btnFilterInstances.setPreferredSize(new java.awt.Dimension(109, 25));
 
         btnSelectAllInstances.setAction(actionMap.get("btnSelectAllInstances")); // NOI18N
         btnSelectAllInstances.setText(resourceMap.getString("btnSelectAllInstances.text")); // NOI18N
         btnSelectAllInstances.setToolTipText(resourceMap.getString("btnSelectAllInstances.toolTipText")); // NOI18N
         btnSelectAllInstances.setMaximumSize(new java.awt.Dimension(109, 23));
         btnSelectAllInstances.setMinimumSize(new java.awt.Dimension(109, 23));
         btnSelectAllInstances.setName("btnSelectAllInstances"); // NOI18N
         btnSelectAllInstances.setPreferredSize(new java.awt.Dimension(109, 25));
 
         btnDeselectAllInstances.setAction(actionMap.get("btnDeselectAllInstances")); // NOI18N
         btnDeselectAllInstances.setText(resourceMap.getString("btnDeselectAllInstances.text")); // NOI18N
         btnDeselectAllInstances.setToolTipText(resourceMap.getString("btnDeselectAllInstances.toolTipText")); // NOI18N
         btnDeselectAllInstances.setMaximumSize(new java.awt.Dimension(109, 23));
         btnDeselectAllInstances.setMinimumSize(new java.awt.Dimension(109, 23));
         btnDeselectAllInstances.setName("btnDeselectAllInstances"); // NOI18N
         btnDeselectAllInstances.setPreferredSize(new java.awt.Dimension(109, 25));
 
         btnInvertSelection.setAction(actionMap.get("btnInvertSelection")); // NOI18N
         btnInvertSelection.setText(resourceMap.getString("btnInvertSelection.text")); // NOI18N
         btnInvertSelection.setToolTipText(resourceMap.getString("btnInvertSelection.toolTipText")); // NOI18N
         btnInvertSelection.setName("btnInvertSelection"); // NOI18N
         btnInvertSelection.setPreferredSize(new java.awt.Dimension(109, 25));
 
         btnUndoInstances.setAction(actionMap.get("btnUndoInstances")); // NOI18N
         btnUndoInstances.setText(resourceMap.getString("btnUndoInstances.text")); // NOI18N
         btnUndoInstances.setToolTipText(resourceMap.getString("btnUndoInstances.toolTipText")); // NOI18N
         btnUndoInstances.setName("btnUndoInstances"); // NOI18N
         btnUndoInstances.setPreferredSize(new java.awt.Dimension(109, 25));
 
         btnSelectedInstances.setAction(actionMap.get("btnSelectedInstances")); // NOI18N
         btnSelectedInstances.setText(resourceMap.getString("btnSelectedInstances.text")); // NOI18N
         btnSelectedInstances.setName("btnSelectedInstances"); // NOI18N
         btnSelectedInstances.setPreferredSize(new java.awt.Dimension(109, 25));
 
         javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
         jPanel5.setLayout(jPanel5Layout);
         jPanel5Layout.setHorizontalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnFilterInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnSelectAllInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnDeselectAllInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnInvertSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnSelectedInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(btnUndoInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnSaveInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         jPanel5Layout.setVerticalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnSaveInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnFilterInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnSelectAllInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnDeselectAllInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnInvertSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnUndoInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnSelectedInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(14, Short.MAX_VALUE))
         );
 
         lblFilterStatus.setText(resourceMap.getString("lblFilterStatus.text")); // NOI18N
         lblFilterStatus.setName("lblFilterStatus"); // NOI18N
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addGap(10, 10, 10)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 799, Short.MAX_VALUE)
                 .addGap(10, 10, 10))
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblFilterStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 799, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblFilterStatus)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         jSplitPane2.setRightComponent(jPanel3);
 
         javax.swing.GroupLayout panelChooseInstancesLayout = new javax.swing.GroupLayout(panelChooseInstances);
         panelChooseInstances.setLayout(panelChooseInstancesLayout);
         panelChooseInstancesLayout.setHorizontalGroup(
             panelChooseInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1070, Short.MAX_VALUE)
         );
         panelChooseInstancesLayout.setVerticalGroup(
             panelChooseInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
         );
 
         manageExperimentPane.addTab("Instances", panelChooseInstances);
 
         panelExperimentParams.setName("panelExperimentParams"); // NOI18N
 
         jPanel1.setName("jPanel1"); // NOI18N
 
         jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
         jLabel1.setName("jLabel1"); // NOI18N
 
         txtNumRuns.setText(resourceMap.getString("txtNumRuns.text")); // NOI18N
         txtNumRuns.setToolTipText(resourceMap.getString("txtNumRuns.toolTipText")); // NOI18N
         txtNumRuns.setName("txtNumRuns"); // NOI18N
         txtNumRuns.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txtNumRunsKeyReleased(evt);
             }
         });
 
         btnGenerateJobs.setAction(actionMap.get("btnGenerateJobs")); // NOI18N
         btnGenerateJobs.setText(resourceMap.getString("btnGenerateJobs.text")); // NOI18N
         btnGenerateJobs.setToolTipText(resourceMap.getString("btnGenerateJobs.toolTipText")); // NOI18N
         btnGenerateJobs.setName("btnGenerateJobs"); // NOI18N
         btnGenerateJobs.setPreferredSize(new java.awt.Dimension(197, 25));
 
         lblNumJobs.setText(resourceMap.getString("lblNumJobs.text")); // NOI18N
         lblNumJobs.setName("lblNumJobs"); // NOI18N
 
         lblCurNumRuns.setText(resourceMap.getString("lblCurNumRuns.text")); // NOI18N
         lblCurNumRuns.setToolTipText(resourceMap.getString("lblCurNumRuns.toolTipText")); // NOI18N
         lblCurNumRuns.setName("lblCurNumRuns"); // NOI18N
 
         btnGeneratePackage.setText(resourceMap.getString("generatePackage.text")); // NOI18N
         btnGeneratePackage.setToolTipText(resourceMap.getString("generatePackage.toolTipText")); // NOI18N
         btnGeneratePackage.setActionCommand(resourceMap.getString("generatePackage.actionCommand")); // NOI18N
         btnGeneratePackage.setName("generatePackage"); // NOI18N
         btnGeneratePackage.setPreferredSize(new java.awt.Dimension(155, 25));
         btnGeneratePackage.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnGeneratePackage(evt);
             }
         });
 
         btnSelectQueue.setAction(actionMap.get("btnSelectQueue")); // NOI18N
         btnSelectQueue.setText(resourceMap.getString("btnSelectQueue.text")); // NOI18N
         btnSelectQueue.setToolTipText(resourceMap.getString("btnSelectQueue.toolTipText")); // NOI18N
         btnSelectQueue.setName("btnSelectQueue"); // NOI18N
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addComponent(btnSelectQueue, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(btnGenerateJobs, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                     .addComponent(btnGeneratePackage, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                         .addComponent(jLabel1)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(txtNumRuns, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lblCurNumRuns)
                     .addComponent(lblNumJobs))
                 .addContainerGap(843, Short.MAX_VALUE))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel1)
                     .addComponent(txtNumRuns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lblCurNumRuns))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnSelectQueue)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnGeneratePackage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnGenerateJobs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lblNumJobs, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(328, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout panelExperimentParamsLayout = new javax.swing.GroupLayout(panelExperimentParams);
         panelExperimentParams.setLayout(panelExperimentParamsLayout);
         panelExperimentParamsLayout.setHorizontalGroup(
             panelExperimentParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         panelExperimentParamsLayout.setVerticalGroup(
             panelExperimentParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         manageExperimentPane.addTab(resourceMap.getString("panelExperimentParams.TabConstraints.tabTitle"), panelExperimentParams); // NOI18N
 
         panelJobBrowser.setName("panelJobBrowser"); // NOI18N
         panelJobBrowser.setPreferredSize(new java.awt.Dimension(10000, 10000));
 
         jScrollPane6.setName("jScrollPane6"); // NOI18N
 
         tableJobs.setModel(new javax.swing.table.DefaultTableModel(
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
         tableJobs.setToolTipText(resourceMap.getString("tableJobs.toolTipText")); // NOI18N
         tableJobs.setName("tableJobs"); // NOI18N
         jScrollPane6.setViewportView(tableJobs);
 
         btnRefreshJobs.setAction(actionMap.get("btnRefreshJobs")); // NOI18N
         btnRefreshJobs.setText(resourceMap.getString("btnRefreshJobs.text")); // NOI18N
         btnRefreshJobs.setToolTipText(resourceMap.getString("btnRefreshJobs.toolTipText")); // NOI18N
         btnRefreshJobs.setName("btnRefreshJobs"); // NOI18N
         btnRefreshJobs.setPreferredSize(new java.awt.Dimension(103, 25));
 
         btnBrowserColumnSelection.setAction(actionMap.get("btnBrowserColumnSelection")); // NOI18N
         btnBrowserColumnSelection.setText(resourceMap.getString("btnBrowserColumnSelection.text")); // NOI18N
         btnBrowserColumnSelection.setToolTipText(resourceMap.getString("btnBrowserColumnSelection.toolTipText")); // NOI18N
         btnBrowserColumnSelection.setName("btnBrowserColumnSelection"); // NOI18N
         btnBrowserColumnSelection.setPreferredSize(new java.awt.Dimension(103, 25));
 
         btnFilterJobs.setAction(actionMap.get("btnFilterJobs")); // NOI18N
         btnFilterJobs.setText(resourceMap.getString("btnFilterJobs.text")); // NOI18N
         btnFilterJobs.setToolTipText(resourceMap.getString("btnFilterJobs.toolTipText")); // NOI18N
         btnFilterJobs.setName("btnFilterJobs"); // NOI18N
         btnFilterJobs.setPreferredSize(new java.awt.Dimension(103, 25));
 
         btnCSVExport.setAction(actionMap.get("btnCSVExport")); // NOI18N
         btnCSVExport.setToolTipText(resourceMap.getString("btnCSVExport.toolTipText")); // NOI18N
         btnCSVExport.setLabel(resourceMap.getString("btnCSVExport.label")); // NOI18N
         btnCSVExport.setName("btnCSVExport"); // NOI18N
         btnCSVExport.setPreferredSize(new java.awt.Dimension(103, 25));
 
         lblJobsFilterStatus.setName("lblJobsFilterStatus"); // NOI18N
 
         txtJobsTimer.setText(resourceMap.getString("txtJobsTimer.text")); // NOI18N
         txtJobsTimer.setToolTipText(resourceMap.getString("txtJobsTimer.toolTipText")); // NOI18N
         txtJobsTimer.setName("txtJobsTimer"); // NOI18N
         txtJobsTimer.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txtJobsTimerKeyReleased(evt);
             }
         });
 
         chkJobsTimer.setText(resourceMap.getString("chkJobsTimer.text")); // NOI18N
         chkJobsTimer.setToolTipText(resourceMap.getString("chkJobsTimer.toolTipText")); // NOI18N
         chkJobsTimer.setName("chkJobsTimer"); // NOI18N
         chkJobsTimer.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 chkJobsTimerMouseReleased(evt);
             }
         });
 
         jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
         jLabel5.setName("jLabel5"); // NOI18N
 
         javax.swing.GroupLayout panelJobBrowserLayout = new javax.swing.GroupLayout(panelJobBrowser);
         panelJobBrowser.setLayout(panelJobBrowserLayout);
         panelJobBrowserLayout.setHorizontalGroup(
             panelJobBrowserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelJobBrowserLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnRefreshJobs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnBrowserColumnSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnFilterJobs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnCSVExport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 458, Short.MAX_VALUE)
                 .addComponent(jLabel5)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtJobsTimer, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(chkJobsTimer)
                 .addContainerGap())
             .addGroup(panelJobBrowserLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblJobsFilterStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 1050, Short.MAX_VALUE)
                 .addContainerGap())
             .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1070, Short.MAX_VALUE)
         );
         panelJobBrowserLayout.setVerticalGroup(
             panelJobBrowserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelJobBrowserLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblJobsFilterStatus)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(panelJobBrowserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnRefreshJobs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnBrowserColumnSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnFilterJobs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnCSVExport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(chkJobsTimer)
                     .addComponent(txtJobsTimer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel5))
                 .addContainerGap())
         );
 
         manageExperimentPane.addTab(resourceMap.getString("panelJobBrowser.TabConstraints.tabTitle"), panelJobBrowser); // NOI18N
 
         panelAnalyse.setName("panelAnalyse"); // NOI18N
         panelAnalyse.setViewportView(analysePanel);
         panelAnalyse.getVerticalScrollBar().setUnitIncrement(30);
         manageExperimentPane.addTab(resourceMap.getString("panelAnalyse.TabConstraints.tabTitle"), panelAnalyse); // NOI18N
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(manageExperimentPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1075, Short.MAX_VALUE)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(manageExperimentPane, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     public boolean hasUnsavedChanges() {
         return solverConfigPanel.isModified() || insTableModel.isModified() || experimentIsModified();
     }
 
     public void setTitles() {
         if (solverConfigPanel.isModified()) {
             manageExperimentPane.setTitleAt(1, "Solvers (modified)");
         } else {
             manageExperimentPane.setTitleAt(1, "Solvers");
         }
         if (insTableModel.isModified()) {
             manageExperimentPane.setTitleAt(2, "Instances (modified)");
         } else {
             manageExperimentPane.setTitleAt(2, "Instances");
         }
         if (expController.getActiveExperiment() == null) {
             manageExperimentPane.setTitleAt(0, "Experiments");
             ((EDACCView) EDACCApp.getApplication().getMainView()).setStatusText("MANAGE EXPERIMENT MODE - Connected to database: " + DatabaseConnector.getInstance().getDatabase() + " on host: " + DatabaseConnector.getInstance().getHostname());
         } else if (experimentIsModified()) {
             manageExperimentPane.setTitleAt(0, "Experiments (Active: " + expController.getActiveExperiment().getName() + ", modified)");
             ((EDACCView) EDACCApp.getApplication().getMainView()).setStatusText("MANAGE EXPERIMENT MODE (Active: " + expController.getActiveExperiment().getName() + ", modified) - Connected to database: " + DatabaseConnector.getInstance().getDatabase() + " on host: " + DatabaseConnector.getInstance().getHostname());
         } else {
             manageExperimentPane.setTitleAt(0, "Experiments (Active: " + expController.getActiveExperiment().getName() + ")");
             ((EDACCView) EDACCApp.getApplication().getMainView()).setStatusText("MANAGE EXPERIMENT MODE (Active: " + expController.getActiveExperiment().getName() + ") - Connected to database: " + DatabaseConnector.getInstance().getDatabase() + " on host: " + DatabaseConnector.getInstance().getHostname());
         }
         manageExperimentPane.invalidate();
     }
 
     public void setGenerateJobsTitle() {
         if (generateJobsIsModified()) {
             manageExperimentPane.setTitleAt(3, "Generate Jobs (modified)");
         } else {
             manageExperimentPane.setTitleAt(3, "Generate Jobs");
         }
     }
 
     public boolean experimentIsModified() {
         try {
             return expController.getActiveExperiment().getTimeOut() != Integer.parseInt(txtTimeout.getText()) || expController.getActiveExperiment().getMaxSeed() != Integer.parseInt(txtMaxSeeds.getText()) || expController.getActiveExperiment().getMemOut() != Integer.parseInt(txtMaxMem.getText()) || expController.getActiveExperiment().isAutoGeneratedSeeds() != chkGenerateSeeds.isSelected() || expController.getActiveExperiment().isLinkSeeds() != chkLinkSeeds.isSelected();
         } catch (Exception e) {
             return true;
         }
     }
 
     public boolean generateJobsIsModified() {
         int numRuns = 0;
         try {
             numRuns = Integer.parseInt(txtNumRuns.getText());
         } catch (NumberFormatException _) {
         }
         return expController.experimentResultsIsModified(numRuns);
     }
 
     private void manageExperimentPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_manageExperimentPaneStateChanged
         if (manageExperimentPane.getSelectedIndex() == 3) {
             // generate jobs tab
             lblCurNumRuns.setText("currently: " + String.valueOf(expController.getActiveExperiment().getNumRuns()));
             lblNumJobs.setText(String.valueOf(expController.getNumJobs()) + " jobs in the database");
         } else if (manageExperimentPane.getSelectedIndex() == 4) {
             try {
                 // job browser tab
                 jobsTableModel.setJobs(null);
             } catch (SQLException ex) {
                 Logger.getLogger(EDACCExperimentMode.class.getName()).log(Level.SEVERE, null, ex);
             }
             // first draw the results browser, then load the jobs (SwingUtilites)
             SwingUtilities.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     Tasks.startTask("loadJobs", expController, EDACCExperimentMode.this);
                 }
             });
         } else if (manageExperimentPane.getSelectedIndex() == 5) {
             // Analyse tab
             try {
                 expController.checkForR();
             } catch (Exception e) {
                 javax.swing.JOptionPane.showMessageDialog(null, "Error while initializing R: " + e.getMessage(), "Analyse", javax.swing.JOptionPane.ERROR_MESSAGE);
                 return;
             }
             analysePanel.initialize();
             if (analysePanel.comboType.getItemCount() > 0) {
                 analysePanel.comboType.setSelectedIndex(0);
             }
         }
 
         if (manageExperimentPane.getSelectedIndex() != 4) {
             jobsTimerWasActive = jobsTimer != null;
             stopJobsTimer();
         }
     }//GEN-LAST:event_manageExperimentPaneStateChanged
 
     private void btnGeneratePackage(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGeneratePackage
         JFileChooser packageFileChooser = new JFileChooser();
         packageFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         if (packageFileChooser.showDialog(this, "Select Package Location") != JFileChooser.APPROVE_OPTION) {
             return;
         }
         File folder = new File(packageFileChooser.getSelectedFile().getAbsolutePath());
         if (!folder.exists()) {
             int userInput = javax.swing.JOptionPane.showConfirmDialog(Tasks.getTaskView(), "The directory " + folder.getAbsolutePath() + " doesn't exist. Should it be created?", "Generate cluster package", javax.swing.JOptionPane.YES_NO_OPTION);
             if (userInput == 1) {
                 return;
             } else {
                 folder.mkdirs();
             }
         }
 
         File zipFile = new File(packageFileChooser.getSelectedFile().getAbsolutePath() + System.getProperty("file.separator") + expController.getActiveExperiment().getDate().toString() + " - " + expController.getActiveExperiment().getName() + ".zip");
         Tasks.startTask("generatePackage", new Class[]{File.class, edacc.model.Tasks.class}, new Object[]{zipFile, null}, expController, this);
     }//GEN-LAST:event_btnGeneratePackage
 
     private void btnSelectAllInstanceClassesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectAllInstanceClassesActionPerformed
         expController.selectAllInstanceClasses();
     }//GEN-LAST:event_btnSelectAllInstanceClassesActionPerformed
 
     private void btnDeselectAllInstnaceClassesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeselectAllInstnaceClassesActionPerformed
         expController.deselectAllInstanceClasses();
     }//GEN-LAST:event_btnDeselectAllInstnaceClassesActionPerformed
 
     private void txtTimeoutKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTimeoutKeyReleased
         txtTimeout.setText(getNumberText(txtTimeout.getText()));
         setTitles();
     }//GEN-LAST:event_txtTimeoutKeyReleased
 
     private void txtMaxMemKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMaxMemKeyReleased
         txtMaxMem.setText(getNumberText(txtMaxMem.getText()));
         setTitles();
     }//GEN-LAST:event_txtMaxMemKeyReleased
 
     private void txtMaxSeedsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMaxSeedsKeyReleased
         txtMaxSeeds.setText(getNumberText(txtMaxSeeds.getText()));
         setTitles();
     }//GEN-LAST:event_txtMaxSeedsKeyReleased
 
     private void chkGenerateSeedsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chkGenerateSeedsMouseReleased
         setTitles();
     }//GEN-LAST:event_chkGenerateSeedsMouseReleased
 
     private void chkLinkSeedsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chkLinkSeedsMouseReleased
         setTitles();
     }//GEN-LAST:event_chkLinkSeedsMouseReleased
 
     private void txtNumRunsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNumRunsKeyReleased
         txtNumRuns.setText(getNumberText(txtNumRuns.getText()));
         setGenerateJobsTitle();
     }//GEN-LAST:event_txtNumRunsKeyReleased
 
     private void chkJobsTimerMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chkJobsTimerMouseReleased
         if (chkJobsTimer.isSelected()) {
             if (jobsTimer == null) {
                 try {
                     int period = Integer.parseInt(txtJobsTimer.getText());
                     if (period <= 0) {
                         throw new NumberFormatException();
                     }
                     jobsTimer = new Timer();
                     jobsTimer.scheduleAtFixedRate(new TimerTask() {
 
                         @Override
                         public void run() {
                             Tasks.startTask("loadJobs", expController, EDACCExperimentMode.this, false);
                         }
                     }, 0, period * 1000);
                 } catch (NumberFormatException e) {
                     javax.swing.JOptionPane.showMessageDialog(null, "Expected positive integer for period.", "Invalid period", javax.swing.JOptionPane.ERROR_MESSAGE);
                     chkJobsTimer.setSelected(false);
                     return;
                 }
             }
         } else {
             stopJobsTimer();
         }
     }//GEN-LAST:event_chkJobsTimerMouseReleased
 
     private void tableExperimentsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableExperimentsMouseClicked
         if (evt.getClickCount() == 2) {
             this.btnLoadExperiment();
         }
     }//GEN-LAST:event_tableExperimentsMouseClicked
 
     private void txtJobsTimerKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtJobsTimerKeyReleased
         txtJobsTimer.setText(getNumberText(txtJobsTimer.getText()));
         if (chkJobsTimer.isSelected()) {
             chkJobsTimer.setSelected(false);
             stopJobsTimer();
         }
     }//GEN-LAST:event_txtJobsTimerKeyReleased
 
     public void stopJobsTimer() {
         if (jobsTimer != null) {
             jobsTimer.cancel();
             jobsTimer = null;
             chkJobsTimer.setSelected(false);
         }
     }
 
     public String getNumberText(String text) {
         String res = "";
         for (int i = 0; i < text.length(); i++) {
             if (text.charAt(i) >= '0' && text.charAt(i) <= '9') {
                 res += text.charAt(i);
             }
         }
         return res;
     }
 
     /**
      * Method to be called after an experiment is loaded.
      */
     public void afterExperimentLoaded() {
         manageExperimentPane.setEnabledAt(1, true);
         manageExperimentPane.setEnabledAt(2, true);
         manageExperimentPane.setEnabledAt(3, true);
         manageExperimentPane.setEnabledAt(4, true);
         manageExperimentPane.setEnabledAt(5, true);
         enableEditExperiment(expController.getActiveExperiment().getMemOut(), expController.getActiveExperiment().getTimeOut(), expController.getActiveExperiment().getMaxSeed(), expController.getActiveExperiment().isAutoGeneratedSeeds(), expController.getActiveExperiment().isLinkSeeds());
         setTitles();
         btnDiscardExperiment.setEnabled(true);
     }
 
     /**
      * Method to be call after an experiment ist unloaded.
      */
     public void afterExperimentUnloaded() {
         manageExperimentPane.setSelectedIndex(0);
         manageExperimentPane.setEnabledAt(1, false);
         manageExperimentPane.setEnabledAt(2, false);
         manageExperimentPane.setEnabledAt(3, false);
         manageExperimentPane.setEnabledAt(4, false);
         manageExperimentPane.setEnabledAt(5, false);
         disableEditExperiment();
         setTitles();
         btnDiscardExperiment.setEnabled(false);
     }
 
     private void createDatabaseErrorMessage(SQLException e) {
         javax.swing.JOptionPane.showMessageDialog(null, "There was an error while communicating with the database: " + e, "Connection error", javax.swing.JOptionPane.ERROR_MESSAGE);
     }
 
     @Action
     public void btnLoadExperiment() {
         if (tableExperiments.getSelectedRow() != -1) {
             if (expController.getActiveExperiment() != null && hasUnsavedChanges()) {
                 if (JOptionPane.showConfirmDialog(this,
                         "Loading an experiment will make you lose all unsaved changes of the current experiment. Continue loading the experiment?",
                         "Warning!",
                         JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                     Integer i = (Integer) expTableModel.getValueAt(tableExperiments.convertRowIndexToModel(tableExperiments.getSelectedRow()), 5);
                     Tasks.startTask("loadExperiment", new Class[]{int.class, edacc.model.Tasks.class}, new Object[]{i.intValue(), null}, expController, this);
                 }
             } else {
                 Integer i = (Integer) expTableModel.getValueAt(tableExperiments.convertRowIndexToModel(tableExperiments.getSelectedRow()), 5);
                 Tasks.startTask("loadExperiment", new Class[]{int.class, edacc.model.Tasks.class}, new Object[]{i.intValue(), null}, expController, this);
             }
         }
     }
 
     @Action
     public void btnCreateExperiment() {
         JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
         EDACCExperimentModeNewExp dialogNewExp = new EDACCExperimentModeNewExp(mainFrame, true);
         dialogNewExp.setLocationRelativeTo(mainFrame);
         try {
             while (true) {
                 dialogNewExp.setVisible(true);
                 if (dialogNewExp.canceled) {
                     break;
                 }
                 if ("".equals(dialogNewExp.ExpName)) {
                     javax.swing.JOptionPane.showMessageDialog(null, "The experiment must have a name.", "Create experiment", javax.swing.JOptionPane.ERROR_MESSAGE);
                 } else if (expController.getExperiment(dialogNewExp.ExpName) != null) {
                     javax.swing.JOptionPane.showMessageDialog(null, "There exists already an experiment with the same name.", "Create experiment", javax.swing.JOptionPane.ERROR_MESSAGE);
                 } else {
                     break;
                 }
             }
             if (!dialogNewExp.canceled) {
                 expController.createExperiment(dialogNewExp.ExpName, dialogNewExp.ExpDesc);
                 tableExperiments.getSelectionModel().setSelectionInterval(tableExperiments.getRowCount() - 1, tableExperiments.getRowCount() - 1);
                 tableExperiments.requestFocusInWindow();
             }
         } catch (SQLException ex) {
             createDatabaseErrorMessage(ex);
         } catch (Exception ex) {
             javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage(), "Create experiment", javax.swing.JOptionPane.ERROR_MESSAGE);
         } finally {
             dialogNewExp.dispose();
         }
     }
 
     @Action
     public void btnRemoveExperiment() {
         if (tableExperiments.getSelectedRow() != -1) {
             int removedIndex = tableExperiments.getSelectedRow();
             int userInput = javax.swing.JOptionPane.showConfirmDialog(Tasks.getTaskView(), "Do you really want to remove the experiment " + expTableModel.getValueAt(tableExperiments.convertRowIndexToModel(removedIndex), 0) + "?", "Remove experiment", javax.swing.JOptionPane.YES_NO_OPTION);
             if (userInput == 1) {
                 return;
             } else {
                 try {
 
                     Integer i = (Integer) expTableModel.getValueAt(tableExperiments.convertRowIndexToModel(removedIndex), 5);
                     expController.removeExperiment(i);
                     if (removedIndex > this.tableExperiments.getRowCount() - 1) {
                         removedIndex--;
                     }
                     if (tableExperiments.getRowCount() > 0) {
                         tableExperiments.getSelectionModel().setSelectionInterval(removedIndex, removedIndex);
                     }
                     btnRemoveExperiment.requestFocusInWindow();
                 } catch (SQLException ex) {
                     createDatabaseErrorMessage(ex);
                 }
             }
         }
 
     }
 
     @Action
     public void btnChooseSolvers() {
         solverConfigPanel.beginUpdate();
         for (int i = 0; i < solTableModel.getRowCount(); i++) {
             if ((Boolean) solTableModel.getValueAt(i, 4) && !solverConfigPanel.solverExists(((Solver) solTableModel.getValueAt(i, 5)).getId())) {
                 solverConfigPanel.addSolver(solTableModel.getValueAt(i, 5));
             } else if (!(Boolean) solTableModel.getValueAt(i, 4)) {
                 solverConfigPanel.removeSolver(solTableModel.getValueAt(i, 5));
             }
         }
         solverConfigPanel.endUpdate();
         setTitles();
     }
 
     @Action
     public void btnSaveSolverConfigurations() {
         Tasks.startTask("saveSolverConfigurations", new Class[]{Tasks.class}, new Object[]{null}, expController, this);
     }
 
     @Action
     public void btnUndoSolverConfigurations() {
         Tasks.startTask("undoSolverConfigurations", new Class[]{Tasks.class}, new Object[]{null}, expController, this);
     }
 
     @Action
     public void btnSelectAllSolvers() {
         for (int i = 0; i < solTableModel.getRowCount(); i++) {
             solTableModel.setValueAt(true, i, 4);
         }
     }
 
     @Action
     public void btnDeselectAll() {
         for (int i = 0; i < solTableModel.getRowCount(); i++) {
             solTableModel.setValueAt(false, i, 4);
         }
     }
 
     @Action
     public void btnReverseSolverSelection() {
         for (int i = 0; i < solTableModel.getRowCount(); i++) {
             solTableModel.setValueAt(!((Boolean) solTableModel.getValueAt(i, 4)), i, 4);
         }
     }
 
     @Action
     public void btnSaveInstances() {
         Tasks.startTask("saveExperimentHasInstances", new Class[]{Tasks.class}, new Object[]{null}, expController, this);
     }
 
     @Action
     public void btnSelectAllInstances() {
         for (int i = 0; i < insTableModel.getRowCount(); i++) {
             if (rowFilter.include((String) insTableModel.getValueAt(i, 0),
                     (Integer) insTableModel.getValueAt(i, 1),
                     (Integer) insTableModel.getValueAt(i, 2),
                     (Float) insTableModel.getValueAt(i, 3),
                     (Integer) insTableModel.getValueAt(i, 4))) {
                 insTableModel.setValueAt(true, i, 5);
             }
         }
     }
 
     @Action
     public void btnDeselectAllInstances() {
         for (int i = 0; i < insTableModel.getRowCount(); i++) {
             if (rowFilter.include((String) insTableModel.getValueAt(i, 0),
                     (Integer) insTableModel.getValueAt(i, 1),
                     (Integer) insTableModel.getValueAt(i, 2),
                     (Float) insTableModel.getValueAt(i, 3),
                     (Integer) insTableModel.getValueAt(i, 4))) {
                 insTableModel.setValueAt(false, i, 5);
             }
         }
     }
 
     @Action
     public void btnInvertSelection() {
         for (int i = 0; i < insTableModel.getRowCount(); i++) {
             if (rowFilter.include((String) insTableModel.getValueAt(i, 0),
                     (Integer) insTableModel.getValueAt(i, 1),
                     (Integer) insTableModel.getValueAt(i, 2),
                     (Float) insTableModel.getValueAt(i, 3),
                     (Integer) insTableModel.getValueAt(i, 4))) {
                 insTableModel.setValueAt(!((Boolean) insTableModel.getValueAt(i, 5)), i, 5);
             }
         }
     }
 
     @Action
     public void btnGenerateJobs() {
         if (hasUnsavedChanges()) {
             javax.swing.JOptionPane.showMessageDialog(null, "Please save all unsaved data first or reload the experiment before generating jobs.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
             return;
         }
         try {
             int numRuns = Integer.parseInt(txtNumRuns.getText());
             Tasks.startTask("generateJobs", new Class[]{int.class, edacc.model.Tasks.class}, new Object[]{numRuns, null}, expController, this);
 
             // TODO assignment of more than one queue/write extra method!
             // assign the default queue to this experiment
             GridQueue q = GridQueuesController.getInstance().getChosenQueue(); // TODO not very nice; will be changed
             if (q == null) {
                 throw new Exception("You have to specify the grid settings first!");
             }
             expController.assignQueueToExperiment(q);
         } catch (NumberFormatException ex) {
             SwingUtilities.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     javax.swing.JOptionPane.showMessageDialog(null, "Expected integers for number of runs, timeout and max seed", "invalid data", javax.swing.JOptionPane.ERROR_MESSAGE);
                 }
             });
         } catch (final SQLException ex) {
             SwingUtilities.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     javax.swing.JOptionPane.showMessageDialog(null, "An error occured while assigning a grid queue to the experiment: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                 }
             });
         } catch (final Exception ex) {
             SwingUtilities.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     javax.swing.JOptionPane.showMessageDialog(null, "An error occured while assigning a grid queue to the experiment: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                 }
             });
         }
     }
 
     public JTable getTableInstances() {
         return tableInstances;
     }
 
     @Action
     public void btnInstanceFilter() {
         if (dialogFilter == null) {
             JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
             dialogFilter = new EDACCInstanceFilter(mainFrame, true, this.rowFilter);
             dialogFilter.setLocationRelativeTo(mainFrame);
         }
         dialogFilter.loadValues();
         EDACCApp.getApplication().show(dialogFilter);
         insTableModel.fireTableDataChanged();
         if (rowFilter.filter_name || rowFilter.filter_numAtoms ||
                 rowFilter.filter_numClauses || rowFilter.filter_ratio ||
                 rowFilter.filter_maxClauseLength) {
             setFilterStatus("This list of instances has filters applied to it. Use the filter button below to modify.");
         } else {
             setFilterStatus("");
         }
 
     }
 
     @Action
     public void btnRefreshJobs() {
         Tasks.startTask("loadJobs", expController, this);
     }
 
     @Action
     public void btnBrowserColumnSelection() {
         EDACCResultsBrowserColumnSelection dialog = new EDACCResultsBrowserColumnSelection(EDACCApp.getApplication().getMainFrame(), true, jobsTableModel);
         dialog.setLocationRelativeTo(EDACCApp.getApplication().getMainFrame());
         dialog.setVisible(true);
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnBrowserColumnSelection;
     private javax.swing.JButton btnCSVExport;
     private javax.swing.JButton btnChooseSolvers;
     private javax.swing.JButton btnCreateExperiment;
     private javax.swing.JButton btnDeselectAllInstances;
     private javax.swing.JButton btnDeselectAllInstnaceClasses;
     private javax.swing.JButton btnDeselectAllSolvers;
     private javax.swing.JButton btnDiscardExperiment;
     private javax.swing.JButton btnEditExperiment;
     private javax.swing.JButton btnEditExperimentSave;
     private javax.swing.JButton btnEditExperimentUndo;
     private javax.swing.JButton btnFilterInstances;
     private javax.swing.JButton btnFilterJobs;
     private javax.swing.JButton btnGenerateJobs;
     private javax.swing.JButton btnGeneratePackage;
     private javax.swing.JButton btnInvertSelection;
     private javax.swing.JButton btnLoadExperiment;
     private javax.swing.JButton btnRefreshJobs;
     private javax.swing.JButton btnRemoveExperiment;
     private javax.swing.JButton btnReverseSolverSelection;
     private javax.swing.JButton btnSaveInstances;
     private javax.swing.JButton btnSaveSolverConfigurations;
     private javax.swing.JButton btnSelectAllInstanceClasses;
     private javax.swing.JButton btnSelectAllInstances;
     private javax.swing.JButton btnSelectAllSolvers;
     private javax.swing.JButton btnSelectQueue;
     private javax.swing.JButton btnSelectedInstances;
     private javax.swing.JButton btnUndoInstances;
     private javax.swing.JButton btnUndoSolverConfigurations;
     private javax.swing.JCheckBox chkGenerateSeeds;
     private javax.swing.JCheckBox chkJobsTimer;
     private javax.swing.JCheckBox chkLinkSeeds;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JScrollPane jScrollPane5;
     private javax.swing.JScrollPane jScrollPane6;
     private javax.swing.JSplitPane jSplitPane1;
     private javax.swing.JSplitPane jSplitPane2;
     private javax.swing.JLabel lblCurNumRuns;
     private javax.swing.JLabel lblFilterStatus;
     private javax.swing.JLabel lblJobsFilterStatus;
     private javax.swing.JLabel lblNumJobs;
     private javax.swing.JTabbedPane manageExperimentPane;
     private javax.swing.JScrollPane panelAnalyse;
     private javax.swing.JPanel panelChooseInstances;
     private javax.swing.JPanel panelChooseSolver;
     private javax.swing.JPanel panelExperimentParams;
     private javax.swing.JPanel panelJobBrowser;
     private javax.swing.JPanel panelManageExperiment;
     private javax.swing.JPanel pnlEditExperiment;
     private javax.swing.JScrollPane scrollPaneExperimentsTable;
     private javax.swing.JTable tableExperiments;
     private javax.swing.JTable tableInstanceClasses;
     private javax.swing.JTable tableInstances;
     private javax.swing.JTable tableJobs;
     private javax.swing.JTable tableSolvers;
     private javax.swing.JTextField txtJobsTimer;
     private javax.swing.JTextField txtMaxMem;
     private javax.swing.JTextField txtMaxSeeds;
     private javax.swing.JTextField txtNumRuns;
     private javax.swing.JTextField txtTimeout;
     // End of variables declaration//GEN-END:variables
 
     @Override
     public void onTaskSuccessful(String methodName, Object result) {
         if ("generateJobs".equals(methodName)) {
             int added_experiments = (Integer) result;
             lblNumJobs.setText(String.valueOf(expController.getNumJobs()) + " jobs in the database");
             lblCurNumRuns.setText("currently: " + String.valueOf(expController.getActiveExperiment().getNumRuns()));
             javax.swing.JOptionPane.showMessageDialog(null, "Added " + added_experiments + " new " + (added_experiments == 1 ? "job" : "jobs") + ".", "Jobs added", javax.swing.JOptionPane.INFORMATION_MESSAGE);
             setGenerateJobsTitle();
         } else if ("loadExperiment".equals(methodName)) {
             txtNumRuns.setText("" + expController.getActiveExperiment().getNumRuns());
             setTitles();
             setGenerateJobsTitle();
         } else if ("saveSolverConfigurations".equals(methodName) || "saveExperimentHasInstances".equals(methodName)) {
             setTitles();
             setGenerateJobsTitle();
         } else if ("loadJobs".equals(methodName)) {
             if (jobsTimerWasActive) {
                 chkJobsTimer.setSelected(true);
                 chkJobsTimerMouseReleased(null);
                 jobsTimerWasActive = false;
             }
         }
     }
 
     @Override
     public void onTaskStart(String methodName) {
     }
 
     @Override
     public void onTaskFailed(String methodName, Throwable e) {
         if (e instanceof TaskCancelledException) {
             javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
         } else if (e instanceof SQLException) {
             createDatabaseErrorMessage((SQLException) e);
         } else if (methodName.equals("exportCSV")) {
             if (e instanceof IOException) {
                 javax.swing.JOptionPane.showMessageDialog(null, "I/O Exception during CSV export:\n\n" + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
             }
         } else if (methodName.equals("generatePackage")) {
             javax.swing.JOptionPane.showMessageDialog(null, "Excpetion during package generation:\n\n" + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
         }
     }
 
     @Action
     public void btnFilterJobs() {
         EDACCJobsFilter jobsFilter = new EDACCJobsFilter(EDACCApp.getApplication().getMainFrame(), true, this);
         jobsFilter.setLocationRelativeTo(EDACCApp.getApplication().getMainFrame());
         jobsFilter.setVisible(true);
     }
 
     @Action
     public void btnCSVExport() {
         stopJobsTimer();
         JFileChooser fc = new JFileChooser();
         fc.setFileFilter(new FileFilter() {
 
             public boolean accept(File f) {
                 return f.getName().toLowerCase().endsWith(".csv") || f.isDirectory();
             }
 
             public String getDescription() {
                 return "CSV Files (comma separated values)";
             }
         });
         if (fc.showDialog(this, "Export CSV") != JFileChooser.APPROVE_OPTION) {
             return;
         }
         String filename = fc.getSelectedFile().getAbsolutePath();
         if (!filename.toLowerCase().endsWith(".csv")) {
             filename += ".csv";
         }
         Tasks.startTask("exportCSV", new Class[]{File.class, edacc.model.Tasks.class}, new Object[]{new File(filename), null}, expController, this);
     }
 
     public JTable getTableJobs() {
         return tableJobs;
     }
 
     @Action
     public void btnUndoInstances() {
         insTableModel.undo();
         this.setTitles();
     }
 
     @Action
     public void btnEditExperimentSave() {
         try {
             int timeout = Integer.parseInt(txtTimeout.getText());
             int maxMem = Integer.parseInt(txtMaxMem.getText());
             boolean generateSeeds = chkGenerateSeeds.isSelected();
             boolean linkSeeds = chkLinkSeeds.isSelected();
             int maxSeed = Integer.parseInt(txtMaxSeeds.getText());
             int selectedExperiment = tableExperiments.getSelectedRow();
             expController.saveExperimentParameters(maxMem, timeout, maxSeed, generateSeeds, linkSeeds);
             setTitles();
             tableExperiments.getSelectionModel().setSelectionInterval(selectedExperiment, selectedExperiment);
         } catch (NumberFormatException ex) {
             javax.swing.JOptionPane.showMessageDialog(null, "Expected integers for number of runs, timeout and max seed", "invalid data", javax.swing.JOptionPane.ERROR_MESSAGE);
         } catch (SQLException ex) {
             createDatabaseErrorMessage(ex);
         }
     }
 
     public void setFilterStatus(String status) {
         lblFilterStatus.setForeground(Color.red);
         lblFilterStatus.setText(status);
 
     }
 
     public void setJobsFilterStatus(String status) {
         lblJobsFilterStatus.setIcon(new ImageIcon("warning-icon.png"));
         lblJobsFilterStatus.setForeground(Color.red);
         lblJobsFilterStatus.setText(status);
     }
 
     @Action
     public void btnDiscardExperiment() {
         boolean unload = !hasUnsavedChanges() || (JOptionPane.showConfirmDialog(this,
                 "Discarding an experiment will make you lose all unsaved changes of the current experiment. Continue discarding the experiment?",
                 "Warning!",
                 JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION);
         if (unload) {
             expController.unloadExperiment();
             tableExperiments.requestFocusInWindow();
         }
     }
 
     @Action
     public void btnEditExperimentUndo() {
         txtTimeout.setText("" + expController.getActiveExperiment().getTimeOut());
         txtMaxMem.setText("" + expController.getActiveExperiment().getMemOut());
         txtMaxSeeds.setText("" + expController.getActiveExperiment().getMaxSeed());
         chkLinkSeeds.setSelected(expController.getActiveExperiment().isLinkSeeds());
         chkGenerateSeeds.setSelected(expController.getActiveExperiment().isAutoGeneratedSeeds());
         setTitles();
     }
 
     @Action
     public void btnSelectQueue() {
         ((EDACCView) EDACCApp.getApplication().getMainView()).btnGridSettings();
     }
 
     @Action
     public void btnEditExperiment() {
         JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
 
         Experiment exp = expTableModel.getExperimentAt(tableExperiments.getSelectedRow());
         EDACCExperimentModeNewExp dialogEditExp = new EDACCExperimentModeNewExp(mainFrame, true, exp.getName(), exp.getDescription());
         dialogEditExp.setLocationRelativeTo(mainFrame);
         try {
             while (true) {
                 dialogEditExp.setVisible(true);
                 if (dialogEditExp.canceled) {
                     break;
                 }
                 if ("".equals(dialogEditExp.ExpName)) {
                     javax.swing.JOptionPane.showMessageDialog(null, "The experiment must have a name.", "Edit experiment", javax.swing.JOptionPane.ERROR_MESSAGE);
                 } else if (expController.getExperiment(dialogEditExp.ExpName) != null && expController.getExperiment(dialogEditExp.ExpName) != exp) {
                     javax.swing.JOptionPane.showMessageDialog(null, "There exists already an experiment with the same name.", "Edit experiment", javax.swing.JOptionPane.ERROR_MESSAGE);
                 } else {
                     break;
                 }
             }
             if (!dialogEditExp.canceled) {
                 String oldName = exp.getName();
                 String oldDescr = exp.getDescription();
                 exp.setName(dialogEditExp.ExpName);
                 exp.setDescription(dialogEditExp.ExpDesc);
                 try {
                     expController.saveExperiment(exp);
                 } catch (SQLException ex) {
                     exp.setName(oldName);
                     exp.setDescription(oldDescr);
                     createDatabaseErrorMessage(ex);
                 }
                 for (int i = 0; i < expTableModel.getRowCount(); i++) {
                     if (expTableModel.getExperimentAt(i) == exp) {
                         expTableModel.fireTableRowsUpdated(i, i);
                     }
                 }
             }
         } catch (SQLException ex) {
             createDatabaseErrorMessage(ex);
         } catch (Exception ex) {
             javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage(), "Edit experiment", javax.swing.JOptionPane.ERROR_MESSAGE);
         } finally {
             dialogEditExp.dispose();
         }
     }
 
     @Action
     public void btnSelectedInstances() {
         btnSelectAllInstanceClassesActionPerformed(null);
         LinkedList<SortKey> sortKeys = new LinkedList<SortKey>();
         sortKeys.add(new SortKey(5, SortOrder.DESCENDING));
         tableInstances.getRowSorter().setSortKeys(sortKeys);
     }
 }
 
 class ResultsBrowserTableRowSorter extends TableRowSorter<ExperimentResultsBrowserTableModel> {
 
     ResultsBrowserTableRowSorter(ExperimentResultsBrowserTableModel jobsTableModel) {
         super(jobsTableModel);
     }
 
     @Override
     public void setModel(ExperimentResultsBrowserTableModel model) {
         super.setModel(model);
         setModelWrapper(new ExperimentResultsBrowserModelWrapper<ExperimentResultsBrowserTableModel>(getModelWrapper()));
     }
 
     class ExperimentResultsBrowserModelWrapper<M extends TableModel> extends DefaultRowSorter.ModelWrapper<M, Integer> {
 
         private DefaultRowSorter.ModelWrapper<M, Integer> delegate;
 
         public ExperimentResultsBrowserModelWrapper(DefaultRowSorter.ModelWrapper<M, Integer> delegate) {
             this.delegate = delegate;
         }
 
         @Override
         public M getModel() {
             return delegate.getModel();
         }
 
         @Override
         public int getColumnCount() {
             return delegate.getColumnCount();
         }
 
         @Override
         public int getRowCount() {
             return delegate.getRowCount();
         }
 
         @Override
         public Object getValueAt(int row, int column) {
             // this is the status column
             if (((ExperimentResultsBrowserTableModel) this.getModel()).getIndexForColumn(column) == 8) {
                 return "" + (char) (((ExperimentResultsBrowserTableModel) this.getModel()).getStatusCode(row) + 68);
             }
             return ((ExperimentResultsBrowserTableModel) this.getModel()).getValueAt(row, column);
         }
 
         @Override
         public Integer getIdentifier(int row) {
             return delegate.getIdentifier(row);
         }
     }
 }
