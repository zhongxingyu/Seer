 /*
  * EDACCExperimentMode.java
  *
  * Created on 02.01.2010, 00:25:47
  */
 package edacc;
 
 import edacc.events.TaskEvents;
 import edacc.experiment.AnalysisController;
 import edacc.experiment.AnalysisPanel;
 import edacc.experiment.ExperimentController;
 import edacc.experiment.ExperimentResultsBrowserTableModel;
 import edacc.experiment.ExperimentTableModel;
 import edacc.experiment.InstanceTableModel;
 import edacc.experiment.SolverTableModel;
 import edacc.experiment.Util;
 import edacc.gridqueues.GridQueuesController;
 import edacc.model.ComputationMethodDoesNotExistException;
 import edacc.model.DatabaseConnector;
 import edacc.model.Experiment;
 import edacc.model.ExperimentResult;
 import edacc.model.ExperimentResultStatus;
 import edacc.model.InstanceClass;
 import edacc.model.InstanceClassMustBeSourceException;
 import edacc.model.NoConnectionToDBException;
 import edacc.model.PropertyNotInDBException;
 import edacc.model.TaskCancelledException;
 import edacc.model.TaskRunnable;
 import edacc.model.Tasks;
 import edacc.properties.PropertyTypeNotExistException;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import javax.swing.JFileChooser;
 import javax.swing.DefaultRowSorter;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
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
 import javax.swing.table.TableModel;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 /**
  *
  * @author simon
  */
 public class EDACCExperimentMode extends javax.swing.JPanel implements TaskEvents {
 
     public ExperimentController expController;
     public ExperimentTableModel expTableModel;
     public InstanceTableModel insTableModel;
     public SolverTableModel solTableModel;
     public ExperimentResultsBrowserTableModel jobsTableModel;
     public EDACCSolverConfigPanel solverConfigPanel;
     public TableRowSorter<InstanceTableModel> sorter;
     public EDACCJobsFilter resultBrowserRowFilter;
     public DefaultTreeModel instanceClassTreeModel;
     //  private EDACCExperimentModeVirtualExperimentSettings pnlVirtualExperimentSettings = new EDACCExperimentModeVirtualExperimentSettings();
     private EDACCInstanceFilter instanceFilter;
     private EDACCOutputViewer outputViewer;
     private EDACCExperimentModeJobsCellRenderer tableJobsStringRenderer;
     private ResultsBrowserTableRowSorter resultsBrowserTableRowSorter;
     private AnalysisPanel analysePanel;
     private Timer jobsTimer = null;
     private Integer resultBrowserETA;
     private boolean jobsTimerWasActive = false;
     private boolean updateJobsTableColumnWidth = false;
 
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
 
             @Override
             public void keyTyped(KeyEvent e) {
             }
 
             @Override
             public void keyPressed(KeyEvent e) {
             }
 
             @Override
             public void keyReleased(KeyEvent e) {
                 if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                     btnLoadExperiment();
                 }
             }
         });
         tableExperiments.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 
             @Override
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
         sorter = new TableRowSorter<InstanceTableModel>(insTableModel);
         tableInstances.setRowSorter(sorter);
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 instanceFilter = new EDACCInstanceFilter(EDACCApp.getApplication().getMainFrame(), true, tableInstances, true);
             }
         });
 
         instanceClassTreeModel = new DefaultTreeModel(null);
         jTreeInstanceClass.setModel(instanceClassTreeModel);
         jTreeInstanceClass.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
         jTreeInstanceClass.setRootVisible(false);
         jTreeInstanceClass.setShowsRootHandles(true);
         /* -------------------------------- end of instances tab -------------------------------- */
         /* -------------------------------- generate jobs tab -------------------------------- */
 
         /* -------------------------------- end of generate jobs tab -------------------------------- */
         /* -------------------------------- jobs browser tab -------------------------------- */
         jobsTableModel = new ExperimentResultsBrowserTableModel();
 
         tableJobs.setModel(jobsTableModel);
         resultsBrowserTableRowSorter = new ResultsBrowserTableRowSorter(jobsTableModel);
         resultsBrowserTableRowSorter.setSortsOnUpdates(true);
         tableJobs.setRowSorter(resultsBrowserTableRowSorter);
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 resultBrowserRowFilter = new EDACCJobsFilter(EDACCApp.getApplication().getMainFrame(), true, tableJobs, false);
             }
         });
 
         tableJobsStringRenderer = new EDACCExperimentModeJobsCellRenderer();
         tableJobs.setDefaultRenderer(Object.class, new EDACCExperimentModeJobsCellRenderer());
         tableJobs.setDefaultRenderer(String.class, tableJobsStringRenderer);
         tableJobs.setDefaultRenderer(Integer.class, new EDACCExperimentModeJobsCellRenderer());
         tableJobs.setDefaultRenderer(Float.class, new EDACCExperimentModeJobsCellRenderer());
         tableJobs.addKeyListener(new java.awt.event.KeyAdapter() {
 
             @Override
             public void keyReleased(KeyEvent e) {
                 if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_U) {
                     JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
                     EDACCExperimentModeUpdateStatus updateStatusDialog = new EDACCExperimentModeUpdateStatus(mainFrame, true, expController);
                     updateStatusDialog.setLocationRelativeTo(mainFrame);
                     updateStatusDialog.setVisible(true);
                 }
             }
         });
         /* -------------------------------- end of jobs browser tab -------------------------------- */
         /* -------------------------------- analyze tab -------------------------------- */
         analysePanel = new AnalysisPanel(expController);
         panelAnalysis.setViewportView(analysePanel);
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
 
             @Override
             public void update(Observable o, Object arg) {
                 try {
                     if (GridQueuesController.getInstance().getChosenQueuesByExperiment(expController.getActiveExperiment()).isEmpty()) {
                         btnGeneratePackage.setEnabled(false);
                     } else {
                         btnGeneratePackage.setEnabled(true);
                     }
                 } catch (SQLException ex) {
                     btnGeneratePackage.setEnabled(false);
                 }
             }
         });
         DatabaseConnector.getInstance().addObserver(new Observer() {
 
             @Override
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
 
     protected void jobsTableRepaintRow(int rowIndex) {
         final int cols1 = tableJobs.getColumnCount();
         Rectangle rect;
         if (cols1 > 0) {
             rect = tableJobs.getCellRect(rowIndex, 0, true);
             if (cols1 > 1) {
                 rect = rect.union(tableJobs.getCellRect(rowIndex, cols1 - 1, true));
             }
             tableJobs.repaint(rect);
         }
     }
 
     protected void tableJobsProcessMouseEvent(MouseEvent e) {
         final int eventId = e.getID();
         if (eventId == MouseEvent.MOUSE_EXITED || eventId == MouseEvent.MOUSE_ENTERED) {
             tableJobs.setCursor(Cursor.getDefaultCursor());
             if (tableJobsStringRenderer.markRow != -1) {
                 int row = tableJobs.convertRowIndexToView(tableJobsStringRenderer.markRow);
                 jobsTableRepaintRow(row);
                 tableJobsStringRenderer.markRow = -1;
             }
         } else if (eventId == MouseEvent.MOUSE_CLICKED) {
             int col_view = tableJobs.columnAtPoint(e.getPoint());
             int col = jobsTableModel.getIndexForColumn(tableJobs.convertColumnIndexToModel(col_view));
             if (col == ExperimentResultsBrowserTableModel.COL_SOLVER_OUTPUT
                     || col == ExperimentResultsBrowserTableModel.COL_LAUNCHER_OUTPUT
                     || col == ExperimentResultsBrowserTableModel.COL_WATCHER_OUTPUT
                     || col == ExperimentResultsBrowserTableModel.COL_VERIFIER_OUTPUT) {
                 int row = tableJobs.convertRowIndexToModel(tableJobs.rowAtPoint(e.getPoint()));
                 if (outputViewer == null) {
                     outputViewer = new EDACCOutputViewer(EDACCApp.getApplication().getMainFrame(), false, expController);
                     outputViewer.setLocationRelativeTo(EDACCApp.getApplication().getMainFrame());
                 }
                 if (!outputViewer.isVisible()) {
                     outputViewer.setVisible(true);
                 }
                 outputViewer.updateContent(col, jobsTableModel.getExperimentResult(row));
             }
         }
     }
 
     protected void tableJobsProcessMouseMotionEvent(MouseEvent e) {
         int col_view = tableJobs.columnAtPoint(e.getPoint());
         int col = jobsTableModel.getIndexForColumn(tableJobs.convertColumnIndexToModel(col_view));
         if (col == ExperimentResultsBrowserTableModel.COL_SOLVER_OUTPUT
                 || col == ExperimentResultsBrowserTableModel.COL_LAUNCHER_OUTPUT
                 || col == ExperimentResultsBrowserTableModel.COL_WATCHER_OUTPUT
                 || col == ExperimentResultsBrowserTableModel.COL_VERIFIER_OUTPUT) {
             int row_view = tableJobs.rowAtPoint(e.getPoint());
             int row = tableJobs.convertRowIndexToModel(row_view);
             tableJobs.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
             if (tableJobsStringRenderer.markRow != -1) {
                 int old_row = tableJobs.convertRowIndexToView(tableJobsStringRenderer.markRow);
                 jobsTableRepaintRow(old_row);
             }
             tableJobsStringRenderer.markCol = col_view;
             tableJobsStringRenderer.markRow = row;
             jobsTableRepaintRow(row_view);
 
         } else {
             tableJobs.setCursor(Cursor.getDefaultCursor());
             if (tableJobsStringRenderer.markRow != -1) {
                 int row = tableJobs.convertRowIndexToView(tableJobsStringRenderer.markRow);
                 jobsTableRepaintRow(row);
             }
             tableJobsStringRenderer.markRow = -1;
         }
     }
 
     public void reinitializeGUI() {
         expController.unloadExperiment();
         reinitializeExperiments();
         reinitializeInstances();
         reinitializeJobBrowser();
     }
 
     public void reinitializeExperiments() {
         expTableModel.setExperiments(null);
     }
 
     public void reinitializeInstances() {
         instanceFilter.clearFilters();
         jTreeInstanceClass.setSelectionPath(null);
         for (int i = jTreeInstanceClass.getRowCount()-1; i >= 0; i--) {
             jTreeInstanceClass.collapseRow(i);
         }
         lblFilterStatus.setText("");
     }
 
     public void reinitializeJobBrowser() {
         try {
             jobsTableModel.setJobs(null);
             jobsTableModel.fireTableStructureChanged();
         } catch (SQLException ex) {
         }
         resultBrowserRowFilter.clearFilters();
         jobsTableModel.resetColumnVisibility();
         setJobsFilterStatus("");
         jobsTimerWasActive = false;
     }
 
     public void initialize() throws SQLException, InstanceClassMustBeSourceException, IOException, NoConnectionToDBException, PropertyNotInDBException, PropertyTypeNotExistException, ComputationMethodDoesNotExistException {
         btnRemoveExperiment.setEnabled(false);
         btnEditExperiment.setEnabled(false);
         btnLoadExperiment.setEnabled(false);
         expController.initialize();
         tableInstances.moveColumn(InstanceTableModel.COL_SELECTED, insTableModel.getColumnCount() - 1);
         edacc.experiment.Util.updateTableColumnWidth(tableExperiments);
     }
 
     private void disableEditExperiment() {
         pnlEditExperiment.setEnabled(false);
         txtMemoryLimit.setText("");
         txtMemoryLimit.setEnabled(false);
         txtCPUTimeLimit.setText("");
         txtCPUTimeLimit.setEnabled(false);
         txtWallClockTimeLimit.setText("");
         txtWallClockTimeLimit.setEnabled(false);
         txtStackSizeLimit.setText("");
         txtStackSizeLimit.setEnabled(false);
         txtOutputSizeLimit.setText("");
         txtOutputSizeLimit.setEnabled(false);
         txtMaxSeeds.setText("");
         txtMaxSeeds.setEnabled(false);
         chkGenerateSeeds.setSelected(false);
         chkGenerateSeeds.setEnabled(false);
         chkLinkSeeds.setSelected(false);
         chkLinkSeeds.setEnabled(false);
         btnEditExperimentSave.setEnabled(false);
         btnEditExperimentUndo.setEnabled(false);
     }
 
     public void enableEditExperiment(Integer CPUTimeLimit, Integer wallClockTimeLimit, Integer memoryLimit, Integer stackSizeLimit, Integer outputSizeLimit, Integer maxSeed, boolean generateSeeds, boolean linkSeeds) {
         /* try {
         javax.swing.GroupLayout layout = (javax.swing.GroupLayout) panelManageExperiment.getLayout();
         layout.replace(pnlVirtualExperimentSettings, pnlEditExperiment);
         } catch (java.lang.IllegalArgumentException e) {
         // happens if we have already the edit experiment settings panel
         }*/
         pnlEditExperiment.setEnabled(true);
         txtMemoryLimit.setText(memoryLimit.toString());
         txtMemoryLimit.setEnabled(true);
         txtCPUTimeLimit.setText(CPUTimeLimit.toString());
         txtCPUTimeLimit.setEnabled(true);
         txtWallClockTimeLimit.setText(wallClockTimeLimit.toString());
         txtWallClockTimeLimit.setEnabled(true);
         txtStackSizeLimit.setText(stackSizeLimit.toString());
         txtStackSizeLimit.setEnabled(true);
         txtOutputSizeLimit.setText(outputSizeLimit.toString());
         txtOutputSizeLimit.setEnabled(true);
         txtMaxSeeds.setText(maxSeed.toString());
         txtMaxSeeds.setEnabled(true);
         chkGenerateSeeds.setSelected(generateSeeds);
         chkGenerateSeeds.setEnabled(true);
         chkLinkSeeds.setSelected(linkSeeds);
         chkLinkSeeds.setEnabled(true);
         btnEditExperimentSave.setEnabled(true);
         btnEditExperimentUndo.setEnabled(true);
     }
 
     /*public void enableVirtualExperiment() {
     try {
     javax.swing.GroupLayout layout = (javax.swing.GroupLayout) panelManageExperiment.getLayout();
     layout.replace(pnlEditExperiment, pnlVirtualExperimentSettings);
     } catch (java.lang.IllegalArgumentException e) {
     // happens if we have already the virtual experiment settings panel
     }
     }*/
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
         txtCPUTimeLimit = new javax.swing.JTextField();
         txtMemoryLimit = new javax.swing.JTextField();
         jLabel4 = new javax.swing.JLabel();
         chkGenerateSeeds = new javax.swing.JCheckBox();
         chkLinkSeeds = new javax.swing.JCheckBox();
         jLabel3 = new javax.swing.JLabel();
         txtMaxSeeds = new javax.swing.JTextField();
         btnEditExperimentSave = new javax.swing.JButton();
         btnEditExperimentUndo = new javax.swing.JButton();
         txtOutputSizeLimit = new javax.swing.JTextField();
         txtStackSizeLimit = new javax.swing.JTextField();
         txtWallClockTimeLimit = new javax.swing.JTextField();
         jLabel6 = new javax.swing.JLabel();
         jLabel7 = new javax.swing.JLabel();
         jLabel8 = new javax.swing.JLabel();
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
         jTreeInstanceClass = new javax.swing.JTree();
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
         btnRandomSelection = new javax.swing.JButton();
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
         tableJobs = tableJobs = new JTable() {
 
             @Override
             protected void processMouseEvent(MouseEvent e) {
                 super.processMouseEvent(e);
                 tableJobsProcessMouseEvent(e);
             }
 
             @Override
             protected void processMouseMotionEvent(MouseEvent e) {
                 super.processMouseMotionEvent(e);
                 tableJobsProcessMouseMotionEvent(e);
             }
         };
         btnRefreshJobs = new javax.swing.JButton();
         btnBrowserColumnSelection = new javax.swing.JButton();
         btnFilterJobs = new javax.swing.JButton();
         btnExport = new javax.swing.JButton();
         lblJobsFilterStatus = new javax.swing.JLabel();
         txtJobsTimer = new javax.swing.JTextField();
         chkJobsTimer = new javax.swing.JCheckBox();
         jLabel5 = new javax.swing.JLabel();
         lblETA = new javax.swing.JLabel();
         btnComputeResultProperties = new javax.swing.JButton();
         btnSetPriority = new javax.swing.JButton();
         panelAnalysis = new javax.swing.JScrollPane();
 
         setName("Form"); // NOI18N
 
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
 
         txtCPUTimeLimit.setText(resourceMap.getString("txtCPUTimeLimit.text")); // NOI18N
         txtCPUTimeLimit.setToolTipText(resourceMap.getString("txtCPUTimeLimit.toolTipText")); // NOI18N
         txtCPUTimeLimit.setName("txtCPUTimeLimit"); // NOI18N
         txtCPUTimeLimit.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txtCPUTimeLimitKeyReleased(evt);
             }
         });
 
         txtMemoryLimit.setText(resourceMap.getString("txtMemoryLimit.text")); // NOI18N
         txtMemoryLimit.setToolTipText(resourceMap.getString("txtMemoryLimit.toolTipText")); // NOI18N
         txtMemoryLimit.setName("txtMemoryLimit"); // NOI18N
         txtMemoryLimit.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txtMemoryLimitKeyReleased(evt);
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
 
         txtOutputSizeLimit.setText(resourceMap.getString("txtOutputSizeLimit.text")); // NOI18N
         txtOutputSizeLimit.setName("txtOutputSizeLimit"); // NOI18N
         txtOutputSizeLimit.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txtOutputSizeLimitKeyReleased(evt);
             }
         });
 
         txtStackSizeLimit.setText(resourceMap.getString("txtStackSizeLimit.text")); // NOI18N
         txtStackSizeLimit.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txtStackSizeLimitKeyReleased(evt);
             }
         });
 
         txtWallClockTimeLimit.setText(resourceMap.getString("txtWallClockTimeLimit.text")); // NOI18N
         txtWallClockTimeLimit.setName("txtWallClockTimeLimit"); // NOI18N
         txtWallClockTimeLimit.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txtWallClockTimeLimitKeyReleased(evt);
             }
         });
 
         jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
         jLabel6.setName("jLabel6"); // NOI18N
 
         jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
         jLabel7.setName("jLabel7"); // NOI18N
 
         jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
         jLabel8.setName("jLabel8"); // NOI18N
 
         javax.swing.GroupLayout pnlEditExperimentLayout = new javax.swing.GroupLayout(pnlEditExperiment);
         pnlEditExperiment.setLayout(pnlEditExperimentLayout);
         pnlEditExperimentLayout.setHorizontalGroup(
             pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEditExperimentLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                     .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(chkGenerateSeeds, javax.swing.GroupLayout.DEFAULT_SIZE, 918, Short.MAX_VALUE)
                     .addComponent(chkLinkSeeds, javax.swing.GroupLayout.DEFAULT_SIZE, 918, Short.MAX_VALUE)
                     .addComponent(txtMemoryLimit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 918, Short.MAX_VALUE)
                     .addComponent(txtMaxSeeds, javax.swing.GroupLayout.DEFAULT_SIZE, 918, Short.MAX_VALUE)
                     .addComponent(txtWallClockTimeLimit, javax.swing.GroupLayout.DEFAULT_SIZE, 918, Short.MAX_VALUE)
                     .addComponent(txtStackSizeLimit, javax.swing.GroupLayout.DEFAULT_SIZE, 918, Short.MAX_VALUE)
                     .addComponent(txtOutputSizeLimit, javax.swing.GroupLayout.DEFAULT_SIZE, 918, Short.MAX_VALUE)
                     .addComponent(txtCPUTimeLimit, javax.swing.GroupLayout.DEFAULT_SIZE, 918, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEditExperimentLayout.createSequentialGroup()
                         .addComponent(btnEditExperimentUndo, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(btnEditExperimentSave, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
 
         pnlEditExperimentLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnEditExperimentSave, btnEditExperimentUndo});
 
         pnlEditExperimentLayout.setVerticalGroup(
             pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlEditExperimentLayout.createSequentialGroup()
                 .addGroup(pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel2)
                     .addComponent(txtCPUTimeLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(jLabel4)
                     .addComponent(txtMemoryLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(6, 6, 6)
                 .addGroup(pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txtWallClockTimeLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel6))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txtStackSizeLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel7))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txtOutputSizeLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel8))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txtMaxSeeds, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel3))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(chkGenerateSeeds)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(chkLinkSeeds)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(pnlEditExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnEditExperimentSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnEditExperimentUndo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
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
                 .addContainerGap(620, Short.MAX_VALUE)
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
             .addGroup(panelManageExperimentLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelManageExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(scrollPaneExperimentsTable, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1130, Short.MAX_VALUE)
                     .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 1130, Short.MAX_VALUE)
                     .addComponent(pnlEditExperiment, javax.swing.GroupLayout.DEFAULT_SIZE, 1130, Short.MAX_VALUE))
                 .addContainerGap())
         );
         panelManageExperimentLayout.setVerticalGroup(
             panelManageExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelManageExperimentLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(scrollPaneExperimentsTable, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(32, 32, 32)
                 .addComponent(pnlEditExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                 .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 1090, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
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
                     .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1130, Short.MAX_VALUE)
                     .addGroup(panelChooseSolverLayout.createSequentialGroup()
                         .addComponent(btnSelectAllSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnDeselectAllSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnReverseSolverSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnChooseSolvers)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 492, Short.MAX_VALUE)
                         .addComponent(btnUndoSolverConfigurations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnSaveSolverConfigurations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         panelChooseSolverLayout.setVerticalGroup(
             panelChooseSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelChooseSolverLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelChooseSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnSelectAllSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnDeselectAllSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnReverseSolverSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnSaveSolverConfigurations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnChooseSolvers)
                     .addComponent(btnUndoSolverConfigurations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         manageExperimentPane.addTab("Solvers", panelChooseSolver);
 
         panelChooseInstances.setName("panelChooseInstances"); // NOI18N
         panelChooseInstances.setPreferredSize(new java.awt.Dimension(668, 623));
 
         jSplitPane2.setDividerLocation(0.4);
         jSplitPane2.setResizeWeight(0.4);
         jSplitPane2.setName("jSplitPane2"); // NOI18N
 
         jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel4.border.title"))); // NOI18N
         jPanel4.setName("jPanel4"); // NOI18N
 
         jTreeInstanceClass.setName("jTreeInstanceClass"); // NOI18N
         jTreeInstanceClass.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
             public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                 jTreeInstanceClassValueChanged(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 295, Short.MAX_VALUE)
             .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(jTreeInstanceClass, javax.swing.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE))
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 557, Short.MAX_VALUE)
             .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(jTreeInstanceClass, javax.swing.GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE))
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
         btnSaveInstances.setPreferredSize(new java.awt.Dimension(117, 25));
 
         btnFilterInstances.setAction(actionMap.get("btnInstanceFilter")); // NOI18N
         btnFilterInstances.setText(resourceMap.getString("btnFilterInstances.text")); // NOI18N
         btnFilterInstances.setToolTipText(resourceMap.getString("btnFilterInstances.toolTipText")); // NOI18N
         btnFilterInstances.setMaximumSize(new java.awt.Dimension(109, 23));
         btnFilterInstances.setMinimumSize(new java.awt.Dimension(109, 23));
         btnFilterInstances.setName("btnFilterInstances"); // NOI18N
         btnFilterInstances.setPreferredSize(new java.awt.Dimension(117, 25));
 
         btnSelectAllInstances.setAction(actionMap.get("btnSelectAllInstances")); // NOI18N
         btnSelectAllInstances.setText(resourceMap.getString("btnSelectAllInstances.text")); // NOI18N
         btnSelectAllInstances.setToolTipText(resourceMap.getString("btnSelectAllInstances.toolTipText")); // NOI18N
         btnSelectAllInstances.setMaximumSize(new java.awt.Dimension(109, 23));
         btnSelectAllInstances.setMinimumSize(new java.awt.Dimension(109, 23));
         btnSelectAllInstances.setName("btnSelectAllInstances"); // NOI18N
         btnSelectAllInstances.setPreferredSize(new java.awt.Dimension(117, 25));
 
         btnDeselectAllInstances.setAction(actionMap.get("btnDeselectAllInstances")); // NOI18N
         btnDeselectAllInstances.setText(resourceMap.getString("btnDeselectAllInstances.text")); // NOI18N
         btnDeselectAllInstances.setToolTipText(resourceMap.getString("btnDeselectAllInstances.toolTipText")); // NOI18N
         btnDeselectAllInstances.setMaximumSize(new java.awt.Dimension(109, 23));
         btnDeselectAllInstances.setMinimumSize(new java.awt.Dimension(109, 23));
         btnDeselectAllInstances.setName("btnDeselectAllInstances"); // NOI18N
         btnDeselectAllInstances.setPreferredSize(new java.awt.Dimension(117, 25));
 
         btnInvertSelection.setAction(actionMap.get("btnInvertSelection")); // NOI18N
         btnInvertSelection.setText(resourceMap.getString("btnInvertSelection.text")); // NOI18N
         btnInvertSelection.setToolTipText(resourceMap.getString("btnInvertSelection.toolTipText")); // NOI18N
         btnInvertSelection.setName("btnInvertSelection"); // NOI18N
         btnInvertSelection.setPreferredSize(new java.awt.Dimension(117, 25));
 
         btnUndoInstances.setAction(actionMap.get("btnUndoInstances")); // NOI18N
         btnUndoInstances.setText(resourceMap.getString("btnUndoInstances.text")); // NOI18N
         btnUndoInstances.setToolTipText(resourceMap.getString("btnUndoInstances.toolTipText")); // NOI18N
         btnUndoInstances.setName("btnUndoInstances"); // NOI18N
         btnUndoInstances.setPreferredSize(new java.awt.Dimension(117, 25));
 
         btnSelectedInstances.setAction(actionMap.get("btnSelectedInstances")); // NOI18N
         btnSelectedInstances.setText(resourceMap.getString("btnSelectedInstances.text")); // NOI18N
         btnSelectedInstances.setName("btnSelectedInstances"); // NOI18N
         btnSelectedInstances.setPreferredSize(new java.awt.Dimension(117, 25));
 
         btnRandomSelection.setAction(actionMap.get("btnRandomSelection")); // NOI18N
         btnRandomSelection.setText(resourceMap.getString("btnRandomSelection.text")); // NOI18N
         btnRandomSelection.setName("btnRandomSelection"); // NOI18N
         btnRandomSelection.setPreferredSize(new java.awt.Dimension(117, 25));
 
         javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
         jPanel5.setLayout(jPanel5Layout);
         jPanel5Layout.setHorizontalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel5Layout.createSequentialGroup()
                         .addComponent(btnFilterInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 435, Short.MAX_VALUE)
                         .addComponent(btnUndoInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(btnSaveInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel5Layout.createSequentialGroup()
                         .addComponent(btnSelectAllInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(btnDeselectAllInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(btnInvertSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(btnRandomSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(btnSelectedInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         jPanel5Layout.setVerticalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnSelectAllInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnDeselectAllInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnInvertSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnRandomSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnSelectedInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnFilterInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnUndoInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnSaveInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         lblFilterStatus.setText(resourceMap.getString("lblFilterStatus.text")); // NOI18N
         lblFilterStatus.setName("lblFilterStatus"); // NOI18N
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                         .addGap(10, 10, 10)
                         .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 816, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(lblFilterStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 816, Short.MAX_VALUE))
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblFilterStatus)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         jSplitPane2.setRightComponent(jPanel3);
 
         javax.swing.GroupLayout panelChooseInstancesLayout = new javax.swing.GroupLayout(panelChooseInstances);
         panelChooseInstances.setLayout(panelChooseInstancesLayout);
         panelChooseInstancesLayout.setHorizontalGroup(
             panelChooseInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1150, Short.MAX_VALUE)
         );
         panelChooseInstancesLayout.setVerticalGroup(
             panelChooseInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
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
                 .addContainerGap(923, Short.MAX_VALUE))
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
                 .addContainerGap(464, Short.MAX_VALUE))
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
         tableJobs.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tableJobsMouseClicked(evt);
             }
         });
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
 
         btnExport.setAction(actionMap.get("btnCSVExport")); // NOI18N
         btnExport.setText(resourceMap.getString("btnExport.text")); // NOI18N
         btnExport.setToolTipText(resourceMap.getString("btnExport.toolTipText")); // NOI18N
         btnExport.setName("btnExport"); // NOI18N
         btnExport.setPreferredSize(new java.awt.Dimension(103, 25));
 
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
 
         lblETA.setText(resourceMap.getString("lblETA.text")); // NOI18N
         lblETA.setName("lblETA"); // NOI18N
 
         btnComputeResultProperties.setText(resourceMap.getString("btnComputeResultProperties.text")); // NOI18N
         btnComputeResultProperties.setName("btnComputeResultProperties"); // NOI18N
         btnComputeResultProperties.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnComputeResultPropertiesActionPerformed(evt);
             }
         });
 
         btnSetPriority.setAction(actionMap.get("btnSetPriority")); // NOI18N
         btnSetPriority.setText(resourceMap.getString("btnSetPriority.text")); // NOI18N
         btnSetPriority.setName("btnSetPriority"); // NOI18N
         btnSetPriority.setPreferredSize(new java.awt.Dimension(103, 25));
 
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
                 .addComponent(btnComputeResultProperties)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnSetPriority, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 320, Short.MAX_VALUE)
                 .addComponent(jLabel5)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtJobsTimer, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(chkJobsTimer)
                 .addContainerGap())
             .addGroup(panelJobBrowserLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblJobsFilterStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 1130, Short.MAX_VALUE)
                 .addContainerGap())
             .addGroup(panelJobBrowserLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblETA)
                 .addContainerGap(1106, Short.MAX_VALUE))
             .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1150, Short.MAX_VALUE)
         );
 
         panelJobBrowserLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnBrowserColumnSelection, btnComputeResultProperties, btnExport, btnFilterJobs, btnRefreshJobs});
 
         panelJobBrowserLayout.setVerticalGroup(
             panelJobBrowserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelJobBrowserLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblJobsFilterStatus)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(lblETA)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelJobBrowserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnRefreshJobs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnBrowserColumnSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(chkJobsTimer)
                     .addComponent(txtJobsTimer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel5)
                     .addComponent(btnFilterJobs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnComputeResultProperties)
                     .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnSetPriority, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         panelJobBrowserLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnBrowserColumnSelection, btnComputeResultProperties, btnExport, btnFilterJobs, btnRefreshJobs});
 
         manageExperimentPane.addTab(resourceMap.getString("panelJobBrowser.TabConstraints.tabTitle"), panelJobBrowser); // NOI18N
 
         panelAnalysis.setName("panelAnalysis"); // NOI18N
         panelAnalysis.setViewportView(analysePanel);
         panelAnalysis.getVerticalScrollBar().setUnitIncrement(30);
         manageExperimentPane.addTab(resourceMap.getString("panelAnalysis.TabConstraints.tabTitle"), panelAnalysis); // NOI18N
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(manageExperimentPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1155, Short.MAX_VALUE)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(manageExperimentPane, javax.swing.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE)
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
         if (expController.getActiveExperiment() == null) {
             return false;
         }
         try {
             return expController.getActiveExperiment().getCPUTimeLimit() != Integer.parseInt(txtCPUTimeLimit.getText()) || expController.getActiveExperiment().getMaxSeed() != Integer.parseInt(txtMaxSeeds.getText()) || expController.getActiveExperiment().getMemoryLimit() != Integer.parseInt(txtMemoryLimit.getText()) || expController.getActiveExperiment().isAutoGeneratedSeeds() != chkGenerateSeeds.isSelected() || expController.getActiveExperiment().isLinkSeeds() != chkLinkSeeds.isSelected() || expController.getActiveExperiment().getWallClockTimeLimit() != Integer.parseInt(txtWallClockTimeLimit.getText()) || expController.getActiveExperiment().getStackSizeLimit() != Integer.parseInt(txtStackSizeLimit.getText()) || expController.getActiveExperiment().getOutputSizeLimit() != Integer.parseInt(txtOutputSizeLimit.getText());
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
         if (manageExperimentPane.getSelectedIndex() == 1) {
             edacc.experiment.Util.updateTableColumnWidth(tableSolvers);
         } else if (manageExperimentPane.getSelectedIndex() == 2) {
             // instances tab
             instanceFilter.setFilterInstanceClasses(false);
             insTableModel.fireTableDataChanged();
             edacc.experiment.Util.updateTableColumnWidth(tableInstances);
             instanceFilter.setFilterInstanceClasses(true);
             insTableModel.fireTableDataChanged();
         } else if (manageExperimentPane.getSelectedIndex() == 3) {
             // generate jobs tab
             lblCurNumRuns.setText("currently: " + String.valueOf(expController.getActiveExperiment().getNumRuns()));
             lblNumJobs.setText(String.valueOf(expController.getNumJobs()) + " jobs in the database");
             try {
                 if (GridQueuesController.getInstance().getChosenQueuesByExperiment(expController.getActiveExperiment()).isEmpty()) {
                     btnGeneratePackage.setEnabled(false);
                 } else {
                     btnGeneratePackage.setEnabled(true);
                 }
             } catch (SQLException ex) {
                 btnGeneratePackage.setEnabled(false);
             }
         } else if (manageExperimentPane.getSelectedIndex() == 4) {
             // job browser tab
             final Rectangle rect = tableJobs.getVisibleRect();
 
             resultBrowserETA = null;
             lblETA.setText("");
             try {
                 jobsTableModel.setJobs(null);
             } catch (SQLException ex) {
             }
             jobsTableModel.updateSolverProperties();
 
             // first draw the results browser, then load the jobs (SwingUtilites)
             SwingUtilities.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     Tasks.startTask(new TaskRunnable() {
 
                         @Override
                         public void run(Tasks task) {
                             try {
                                 expController.loadJobs();
                             } catch (Throwable e) {
                                 EDACCExperimentMode.this.onTaskFailed("loadJobs", e);
                             }
                             tableJobs.scrollRectToVisible(rect);
                             SwingUtilities.invokeLater(new Runnable() {
 
                                 @Override
                                 public void run() {
                                     EDACCExperimentMode.this.onTaskSuccessful("loadJobs", null);
                                 }
                             });
 
                         }
                     });
                 }
             });
         } else if (manageExperimentPane.getSelectedIndex() == 5) {
             // Analysis tab
             try {
                 AnalysisController.checkForR();
             } catch (Exception e) {
                 javax.swing.JOptionPane.showMessageDialog(null, "Error while initializing R: " + e.getMessage(), "Analyse", javax.swing.JOptionPane.ERROR_MESSAGE);
                 return;
             }
             analysePanel.removeAll();
             Tasks.startTask(new TaskRunnable() {
 
                 @Override
                 public void run(Tasks task) {
                     analysePanel.initialize();
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             if (analysePanel.comboType.getItemCount() > 0) {
                                 analysePanel.comboType.setSelectedIndex(0);
                             }
                         }
                     });
 
                 }
             }, false);
         }
 
         if (manageExperimentPane.getSelectedIndex() != 4) {
             jobsTimerWasActive = jobsTimer != null;
             stopJobsTimer();
         }
     }//GEN-LAST:event_manageExperimentPaneStateChanged
 
     private void btnGeneratePackage(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGeneratePackage
         EDACCGeneratePackageFileChooser packageFileChooser = new EDACCGeneratePackageFileChooser(EDACCApp.getApplication().getMainFrame(), true, expController);
         packageFileChooser.setVisible(true);
     }//GEN-LAST:event_btnGeneratePackage
 
     private void txtCPUTimeLimitKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCPUTimeLimitKeyReleased
         int ss = txtCPUTimeLimit.getSelectionStart();
         int se = txtCPUTimeLimit.getSelectionEnd();
         int dot = txtCPUTimeLimit.getCaret().getDot();
         txtCPUTimeLimit.setText(Util.getNumberText(txtCPUTimeLimit.getText()));
         txtCPUTimeLimit.getCaret().setDot(dot);
         txtCPUTimeLimit.setSelectionStart(ss);
         txtCPUTimeLimit.setSelectionEnd(se);
         setTitles();
     }//GEN-LAST:event_txtCPUTimeLimitKeyReleased
 
     private void txtMemoryLimitKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMemoryLimitKeyReleased
         int ss = txtMemoryLimit.getSelectionStart();
         int se = txtMemoryLimit.getSelectionEnd();
         int dot = txtMemoryLimit.getCaret().getDot();
         txtMemoryLimit.setText(Util.getNumberText(txtMemoryLimit.getText()));
         txtMemoryLimit.getCaret().setDot(dot);
         txtMemoryLimit.setSelectionStart(ss);
         txtMemoryLimit.setSelectionEnd(se);
         setTitles();
     }//GEN-LAST:event_txtMemoryLimitKeyReleased
 
     private void txtMaxSeedsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMaxSeedsKeyReleased
         int ss = txtMaxSeeds.getSelectionStart();
         int se = txtMaxSeeds.getSelectionEnd();
         int dot = txtMaxSeeds.getCaret().getDot();
         txtMaxSeeds.setText(Util.getNumberText(txtMaxSeeds.getText()));
         if (txtMaxSeeds.getText().equals("-") || txtMaxSeeds.getText().equals("-1")) {
             txtMaxSeeds.setText("");
         }
         txtMaxSeeds.getCaret().setDot(dot);
         txtMaxSeeds.setSelectionStart(ss);
         txtMaxSeeds.setSelectionEnd(se);
         setTitles();
     }//GEN-LAST:event_txtMaxSeedsKeyReleased
 
     private void chkGenerateSeedsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chkGenerateSeedsMouseReleased
         setTitles();
     }//GEN-LAST:event_chkGenerateSeedsMouseReleased
 
     private void chkLinkSeedsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chkLinkSeedsMouseReleased
         setTitles();
     }//GEN-LAST:event_chkLinkSeedsMouseReleased
 
     private void txtNumRunsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNumRunsKeyReleased
         int ss = txtNumRuns.getSelectionStart();
         int se = txtNumRuns.getSelectionEnd();
         int dot = txtNumRuns.getCaret().getDot();
         txtNumRuns.setText(Util.getNumberText(txtNumRuns.getText()));
         txtNumRuns.getCaret().setDot(dot);
         txtNumRuns.setSelectionStart(ss);
         txtNumRuns.setSelectionEnd(se);
         setGenerateJobsTitle();
     }//GEN-LAST:event_txtNumRunsKeyReleased
 
     private void chkJobsTimerMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chkJobsTimerMouseReleased
         if (chkJobsTimer.isSelected()) {
             resultBrowserETA = null;
             if (jobsTimer == null) {
                 try {
                     int period = Integer.parseInt(txtJobsTimer.getText());
                     if (period <= 0) {
                         throw new NumberFormatException();
                     }
                     jobsTimer = new Timer();
                     final ExperimentResultsBrowserTableModel sync = jobsTableModel;
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
         txtJobsTimer.setText(Util.getNumberText(txtJobsTimer.getText()));
         if (chkJobsTimer.isSelected()) {
             chkJobsTimer.setSelected(false);
             stopJobsTimer();
         }
     }//GEN-LAST:event_txtJobsTimerKeyReleased
 
     private void tableJobsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableJobsMouseClicked
     }//GEN-LAST:event_tableJobsMouseClicked
 
     private void btnComputeResultPropertiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnComputeResultPropertiesActionPerformed
         EDACCComputeResultProperties compute = new EDACCComputeResultProperties(EDACCApp.getApplication().getMainFrame(), true, expController.getActiveExperiment());
         compute.setLocationRelativeTo(EDACCApp.getApplication().getMainFrame());
         compute.setVisible(true);
         resultBrowserETA = null;
         Tasks.startTask("loadJobs", expController, this, false);
     }//GEN-LAST:event_btnComputeResultPropertiesActionPerformed
 
     private void txtWallClockTimeLimitKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtWallClockTimeLimitKeyReleased
         int ss = txtWallClockTimeLimit.getSelectionStart();
         int se = txtWallClockTimeLimit.getSelectionEnd();
         int dot = txtWallClockTimeLimit.getCaret().getDot();
         txtWallClockTimeLimit.setText(Util.getNumberText(txtWallClockTimeLimit.getText()));
         txtWallClockTimeLimit.getCaret().setDot(dot);
         txtWallClockTimeLimit.setSelectionStart(ss);
         txtWallClockTimeLimit.setSelectionEnd(se);
         setTitles();
     }//GEN-LAST:event_txtWallClockTimeLimitKeyReleased
 
     private void txtStackSizeLimitKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtStackSizeLimitKeyReleased
         int ss = txtStackSizeLimit.getSelectionStart();
         int se = txtStackSizeLimit.getSelectionEnd();
         int dot = txtStackSizeLimit.getCaret().getDot();
         txtStackSizeLimit.setText(Util.getNumberText(txtStackSizeLimit.getText()));
         txtStackSizeLimit.getCaret().setDot(dot);
         txtStackSizeLimit.setSelectionStart(ss);
         txtStackSizeLimit.setSelectionEnd(se);
         setTitles();
     }//GEN-LAST:event_txtStackSizeLimitKeyReleased
 
     private void txtOutputSizeLimitKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtOutputSizeLimitKeyReleased
         int ss = txtOutputSizeLimit.getSelectionStart();
         int se = txtOutputSizeLimit.getSelectionEnd();
         int dot = txtOutputSizeLimit.getCaret().getDot();
         txtOutputSizeLimit.setText(Util.getNumberText(txtOutputSizeLimit.getText()));
         txtOutputSizeLimit.getCaret().setDot(dot);
         txtOutputSizeLimit.setSelectionStart(ss);
         txtOutputSizeLimit.setSelectionEnd(se);
         setTitles();
     }//GEN-LAST:event_txtOutputSizeLimitKeyReleased
 
     private void jTreeInstanceClassValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTreeInstanceClassValueChanged
         instanceFilter.clearInstanceClassIds();
         if (jTreeInstanceClass.getSelectionPaths() != null) {
             for (TreePath path : jTreeInstanceClass.getSelectionPaths()) {
                 for (Integer id : Util.getInstanceClassIdsFromPath((DefaultMutableTreeNode) (path.getLastPathComponent()))) {
                     instanceFilter.addInstanceClassId(id);
                 }
             }
             insTableModel.fireTableDataChanged();
         }
     }//GEN-LAST:event_jTreeInstanceClassValueChanged
 
     public void stopJobsTimer() {
         if (jobsTimer != null) {
             jobsTimer.cancel();
             jobsTimer = null;
             chkJobsTimer.setSelected(false);
         }
     }
 
     /**
      * Method to be called after an experiment is loaded.
      */
     public void afterExperimentLoaded() {
         reinitializeInstances();
         reinitializeJobBrowser();
         manageExperimentPane.setEnabledAt(1, true);
         manageExperimentPane.setEnabledAt(2, true);
         manageExperimentPane.setEnabledAt(3, true);
         manageExperimentPane.setEnabledAt(4, true);
         manageExperimentPane.setEnabledAt(5, true);
         /*   if (expController.getActiveExperiment() instanceof VirtualExperiment) {
         enableVirtualExperiment();
 
         } else {*/
         enableEditExperiment(expController.getActiveExperiment().getCPUTimeLimit(), expController.getActiveExperiment().getWallClockTimeLimit(),
                 expController.getActiveExperiment().getMemoryLimit(), expController.getActiveExperiment().getStackSizeLimit(),
                 expController.getActiveExperiment().getOutputSizeLimit(), expController.getActiveExperiment().getMaxSeed(),
                 expController.getActiveExperiment().isAutoGeneratedSeeds(), expController.getActiveExperiment().isLinkSeeds());
         //    }
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
             if (expController.getActiveExperiment() == null || !hasUnsavedChanges() || JOptionPane.showConfirmDialog(this,
                     "Loading an experiment will make you lose all unsaved changes of the current experiment. Continue loading the experiment?",
                     "Warning!",
                     JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                 Experiment selectedExperiment = expTableModel.getExperimentAt(tableExperiments.convertRowIndexToModel(tableExperiments.getSelectedRow()));
                 updateJobsTableColumnWidth = true;
                 Tasks.startTask("loadExperiment", new Class[]{Experiment.class, edacc.model.Tasks.class}, new Object[]{selectedExperiment, null}, expController, this);
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
                 if ("".equals(dialogNewExp.expName)) {
                     javax.swing.JOptionPane.showMessageDialog(null, "The experiment must have a name.", "Create experiment", javax.swing.JOptionPane.ERROR_MESSAGE);
                 } else if (expController.getExperiment(dialogNewExp.expName) != null) {
                     javax.swing.JOptionPane.showMessageDialog(null, "There exists already an experiment with the same name.", "Create experiment", javax.swing.JOptionPane.ERROR_MESSAGE);
                 } else {
                     break;
                 }
             }
             if (!dialogNewExp.canceled) {
                 expController.createExperiment(dialogNewExp.expName, dialogNewExp.expDesc);
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
 
                     Integer i = expTableModel.getExperimentAt(tableExperiments.convertRowIndexToModel(tableExperiments.getSelectedRow())).getId();
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
                 } catch (Exception e) {
                     javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
 
     }
 
     @Action
     public void btnChooseSolvers() {
         solverConfigPanel.beginUpdate();
         for (int i = 0; i < solTableModel.getRowCount(); i++) {
             if (solTableModel.isSelected(i) && !solverConfigPanel.solverExists(solTableModel.getSolver(i).getId())) {
                 solverConfigPanel.addSolver(solTableModel.getSolver(i));
             } else if (!solTableModel.isSelected(i)) {
                 solverConfigPanel.removeSolver(solTableModel.getSolver(i));
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
             solTableModel.setSelected(i, true);
         }
     }
 
     @Action
     public void btnDeselectAll() {
         for (int i = 0; i < solTableModel.getRowCount(); i++) {
             solTableModel.setSelected(i, false);
         }
     }
 
     @Action
     public void btnReverseSolverSelection() {
         for (int i = 0; i < solTableModel.getRowCount(); i++) {
             solTableModel.setSelected(i, !solTableModel.isSelected(i));
         }
     }
 
     @Action
     public void btnSaveInstances() {
         Tasks.startTask("saveExperimentHasInstances", new Class[]{Tasks.class}, new Object[]{null}, expController, this);
     }
 
     @Action
     public void btnSelectAllInstances() {
         for (int i = 0; i < tableInstances.getRowCount(); i++) {
             tableInstances.setValueAt(true, i, tableInstances.convertColumnIndexToView(InstanceTableModel.COL_SELECTED));
         }
     }
 
     @Action
     public void btnDeselectAllInstances() {
         for (int i = 0; i < tableInstances.getRowCount(); i++) {
             tableInstances.setValueAt(false, i, tableInstances.convertColumnIndexToView(InstanceTableModel.COL_SELECTED));
         }
     }
 
     @Action
     public void btnInvertSelection() {
         for (int i = 0; i < tableInstances.getRowCount(); i++) {
             tableInstances.setValueAt(!((Boolean) tableInstances.getValueAt(i, tableInstances.convertColumnIndexToView(InstanceTableModel.COL_SELECTED))), i, tableInstances.convertColumnIndexToView(InstanceTableModel.COL_SELECTED));
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
         } catch (NumberFormatException ex) {
             SwingUtilities.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     javax.swing.JOptionPane.showMessageDialog(null, "Expected integers for number of runs, timeout and max seed", "invalid data", javax.swing.JOptionPane.ERROR_MESSAGE);
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
         EDACCApp.getApplication().show(instanceFilter);
         insTableModel.fireTableDataChanged();
         if (instanceFilter.hasFiltersApplied()) {
             setFilterStatus("This list of instances has filters applied to it. Use the filter button below to modify.");
         } else {
             setFilterStatus("");
         }
     }
 
     @Action
     public void btnRefreshJobs() {
         resultBrowserETA = null;
         Tasks.startTask("loadJobs", expController, this, false);
     }
 
     @Action
     public void btnBrowserColumnSelection() {
         List sortKeys = tableJobs.getRowSorter().getSortKeys();
         EDACCResultsBrowserColumnSelection dialog = new EDACCResultsBrowserColumnSelection(EDACCApp.getApplication().getMainFrame(), true, jobsTableModel);
         dialog.setLocationRelativeTo(EDACCApp.getApplication().getMainFrame());
         dialog.setVisible(true);
         tableJobs.getRowSorter().setSortKeys(sortKeys);
         Util.updateTableColumnWidth(tableJobs);
     }
 
     public void updateRuntimeEstimation() {
         if (jobsTableModel.getJobs() == null) {
             lblETA.setText("");
             return;
         }
         int jobsCount = jobsTableModel.getJobsCount();
         int jobsSuccessful = jobsTableModel.getJobsCount(ExperimentResultStatus.SUCCESSFUL);
         int jobsWaiting = jobsTableModel.getJobsCount(ExperimentResultStatus.NOTSTARTED);
         int jobsRunning = jobsTableModel.getJobsCount(ExperimentResultStatus.RUNNING);
         int jobsNotSuccessful = jobsCount - jobsSuccessful - jobsWaiting - jobsRunning;
         double percentage = (double) (jobsSuccessful + jobsNotSuccessful) / jobsCount;
         percentage = Math.round(percentage * 100 * 100) / 100.;
 
         int count = 0;
         double avgTime = 0.;
         int curRunningTime = 0;
         for (ExperimentResult er : jobsTableModel.getJobs()) {
             if (er.getStatus().getValue() >= 1) {
                 count++;
                 avgTime += er.getResultTime();
             } else if (er.getStatus().equals(ExperimentResultStatus.RUNNING) && er.getRunningTime() > 0) {
                 curRunningTime += er.getRunningTime();
             }
         }
         String ETA = null;
         if (count > 0 && jobsRunning > 0) {
             avgTime /= count;
             int timeleft = (int) (Math.round((jobsWaiting + jobsRunning) * avgTime / jobsRunning) - curRunningTime / jobsRunning);
 
             if (resultBrowserETA != null) {
                 int tmp = timeleft - resultBrowserETA;
                 timeleft = (int) Math.round(resultBrowserETA + (tmp * 0.2));
             }
             resultBrowserETA = timeleft;
             if (timeleft < 0) {
                 timeleft = 0;
             }
             int seconds = timeleft % 60;
             timeleft /= 60;
             int minutes = timeleft % 60;
             timeleft /= 60;
             int hours = timeleft % 24;
             timeleft /= 24;
             int days = timeleft;
             ETA = "" + days + "d " + (hours < 10 ? "0" + hours : "" + hours) + "h " + (minutes < 10 ? "0" + minutes : "" + minutes) + "m " + (seconds < 10 ? "0" + seconds : "" + seconds) + "s";
         }
         String text = "" + (jobsSuccessful + jobsNotSuccessful) + " (" + jobsNotSuccessful + ") / " + jobsCount + " jobs (" + percentage + "%) finished. " + jobsRunning + " jobs are running.";
         if (ETA != null) {
             text += " ETA: " + ETA;
         }
         lblETA.setText(text);
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnBrowserColumnSelection;
     private javax.swing.JButton btnChooseSolvers;
     private javax.swing.JButton btnComputeResultProperties;
     private javax.swing.JButton btnCreateExperiment;
     private javax.swing.JButton btnDeselectAllInstances;
     private javax.swing.JButton btnDeselectAllSolvers;
     private javax.swing.JButton btnDiscardExperiment;
     private javax.swing.JButton btnEditExperiment;
     private javax.swing.JButton btnEditExperimentSave;
     private javax.swing.JButton btnEditExperimentUndo;
     private javax.swing.JButton btnExport;
     private javax.swing.JButton btnFilterInstances;
     private javax.swing.JButton btnFilterJobs;
     private javax.swing.JButton btnGenerateJobs;
     private javax.swing.JButton btnGeneratePackage;
     private javax.swing.JButton btnInvertSelection;
     private javax.swing.JButton btnLoadExperiment;
     private javax.swing.JButton btnRandomSelection;
     private javax.swing.JButton btnRefreshJobs;
     private javax.swing.JButton btnRemoveExperiment;
     private javax.swing.JButton btnReverseSolverSelection;
     private javax.swing.JButton btnSaveInstances;
     private javax.swing.JButton btnSaveSolverConfigurations;
     private javax.swing.JButton btnSelectAllInstances;
     private javax.swing.JButton btnSelectAllSolvers;
     private javax.swing.JButton btnSelectQueue;
     private javax.swing.JButton btnSelectedInstances;
     private javax.swing.JButton btnSetPriority;
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
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JScrollPane jScrollPane6;
     private javax.swing.JSplitPane jSplitPane1;
     private javax.swing.JSplitPane jSplitPane2;
     private javax.swing.JTree jTreeInstanceClass;
     private javax.swing.JLabel lblCurNumRuns;
     private javax.swing.JLabel lblETA;
     private javax.swing.JLabel lblFilterStatus;
     private javax.swing.JLabel lblJobsFilterStatus;
     private javax.swing.JLabel lblNumJobs;
     private javax.swing.JTabbedPane manageExperimentPane;
     private javax.swing.JScrollPane panelAnalysis;
     private javax.swing.JPanel panelChooseInstances;
     private javax.swing.JPanel panelChooseSolver;
     private javax.swing.JPanel panelExperimentParams;
     private javax.swing.JPanel panelJobBrowser;
     private javax.swing.JPanel panelManageExperiment;
     private javax.swing.JPanel pnlEditExperiment;
     private javax.swing.JScrollPane scrollPaneExperimentsTable;
     private javax.swing.JTable tableExperiments;
     private javax.swing.JTable tableInstances;
     public javax.swing.JTable tableJobs;
     private javax.swing.JTable tableSolvers;
     private javax.swing.JTextField txtCPUTimeLimit;
     private javax.swing.JTextField txtJobsTimer;
     private javax.swing.JTextField txtMaxSeeds;
     private javax.swing.JTextField txtMemoryLimit;
     private javax.swing.JTextField txtNumRuns;
     private javax.swing.JTextField txtOutputSizeLimit;
     private javax.swing.JTextField txtStackSizeLimit;
     private javax.swing.JTextField txtWallClockTimeLimit;
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
             updateRuntimeEstimation();
             if (updateJobsTableColumnWidth) {
                 updateJobsTableColumnWidth = false;
                 edacc.experiment.Util.updateTableColumnWidth(tableJobs);
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
         } else if (e instanceof IOException && (methodName.equals("exportCSV") || methodName.equals("exportTeX"))) {
             javax.swing.JOptionPane.showMessageDialog(null, "I/O Exception during export:\n\n" + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
         } else {
             e.printStackTrace();
             javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
         }
     }
 
     @Action
     public void btnFilterJobs() {
         // EDACCJobsFilter jobsFilter = new EDACCJobsFilter(EDACCApp.getApplication().getMainFrame(), true, this);
         // jobsFilter.setLocationRelativeTo(EDACCApp.getApplication().getMainFrame());
         EDACCApp.getApplication().show(resultBrowserRowFilter);
         if (resultBrowserRowFilter.hasFiltersApplied()) {
             setJobsFilterStatus("This list of jobs has filters applied to it. Use the filter button below to modify.");
         } else {
             setJobsFilterStatus("");
         }
         jobsTableModel.fireTableDataChanged();
         // jobsFilter.setVisible(true);
     }
 
     @Action
     public void btnCSVExport() {
         stopJobsTimer();
         JFileChooser fc = new JFileChooser();
         FileFilter CSVFilter = new FileFilter() {
 
             @Override
             public boolean accept(File f) {
                 return f.getName().toLowerCase().endsWith(".csv") || f.isDirectory();
             }
 
             @Override
             public String getDescription() {
                 return "CSV Files (comma separated values)";
             }
         };
         FileFilter TeXFilter = new FileFilter() {
 
             @Override
             public boolean accept(File f) {
                 return f.getName().toLowerCase().endsWith(".tex") || f.isDirectory();
             }
 
             @Override
             public String getDescription() {
                 return "TeX Files (*.tex)";
             }
         };
         fc.setFileFilter(CSVFilter);
         fc.setFileFilter(TeXFilter);
         if (fc.showDialog(this, "Export") != JFileChooser.APPROVE_OPTION) {
             return;
         }
         String filename = fc.getSelectedFile().getAbsolutePath();
         if (fc.getFileFilter() == CSVFilter && !filename.toLowerCase().endsWith(".csv")) {
             filename += ".csv";
         }
         if (fc.getFileFilter() == TeXFilter && !filename.toLowerCase().endsWith(".tex")) {
             filename += ".tex";
         }
         if (filename.toLowerCase().endsWith(".csv")) {
             Tasks.startTask("exportCSV", new Class[]{File.class, edacc.model.Tasks.class}, new Object[]{new File(filename), null}, expController, this);
         } else if (filename.toLowerCase().endsWith(".tex")) {
             Tasks.startTask("exportTeX", new Class[]{File.class, edacc.model.Tasks.class}, new Object[]{new File(filename), null}, expController, this);
         }
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
             int CPUTimeLimit = Integer.parseInt(txtCPUTimeLimit.getText());
             int wallClockTimeLimit = Integer.parseInt(txtWallClockTimeLimit.getText());
             int memoryLimit = Integer.parseInt(txtMemoryLimit.getText());
             int stackSizeLimit = Integer.parseInt(txtStackSizeLimit.getText());
             int outputSizeLimit = Integer.parseInt(txtOutputSizeLimit.getText());
             boolean generateSeeds = chkGenerateSeeds.isSelected();
             boolean linkSeeds = chkLinkSeeds.isSelected();
             int maxSeed = Integer.parseInt(txtMaxSeeds.getText());
             int selectedExperiment = tableExperiments.getSelectedRow();
             expController.saveExperimentParameters(CPUTimeLimit, wallClockTimeLimit, memoryLimit, stackSizeLimit, outputSizeLimit, maxSeed, generateSeeds, linkSeeds);
             setTitles();
             tableExperiments.getSelectionModel().setSelectionInterval(selectedExperiment, selectedExperiment);
         } catch (NumberFormatException ex) {
             javax.swing.JOptionPane.showMessageDialog(null, "Expected integers for seed and limits.", "Invalid data", javax.swing.JOptionPane.ERROR_MESSAGE);
         } catch (SQLException ex) {
             createDatabaseErrorMessage(ex);
         } catch (Exception e) {
             javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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
         txtCPUTimeLimit.setText("" + expController.getActiveExperiment().getCPUTimeLimit());
         txtMemoryLimit.setText("" + expController.getActiveExperiment().getMemoryLimit());
         txtWallClockTimeLimit.setText("" + expController.getActiveExperiment().getWallClockTimeLimit());
         txtStackSizeLimit.setText("" + expController.getActiveExperiment().getStackSizeLimit());
         txtOutputSizeLimit.setText("" + expController.getActiveExperiment().getOutputSizeLimit());
         txtMaxSeeds.setText("" + expController.getActiveExperiment().getMaxSeed());
         chkLinkSeeds.setSelected(expController.getActiveExperiment().isLinkSeeds());
         chkGenerateSeeds.setSelected(expController.getActiveExperiment().isAutoGeneratedSeeds());
         setTitles();
     }
 
     @Action
     public void btnSelectQueue() {
         try {
             JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
             EDACCManageGridQueuesDialog manageGridQueues = new EDACCManageGridQueuesDialog(mainFrame, true, expController);
             manageGridQueues.setLocationRelativeTo(mainFrame);
             manageGridQueues.setVisible(true);
         } catch (NoConnectionToDBException ex) {
             JOptionPane.showMessageDialog(this, "You have to establish a connection to the database first!", "Error!", JOptionPane.ERROR_MESSAGE);
         } catch (SQLException ex) {
         }
     }
 
     @Action
     public void btnEditExperiment() {
         JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
 
         Experiment exp = expTableModel.getExperimentAt(tableExperiments.convertRowIndexToModel(tableExperiments.getSelectedRow()));
         EDACCExperimentModeNewExp dialogEditExp = new EDACCExperimentModeNewExp(mainFrame, true, exp.getName(), exp.getDescription());
         dialogEditExp.setLocationRelativeTo(mainFrame);
         try {
             while (true) {
                 dialogEditExp.setVisible(true);
                 if (dialogEditExp.canceled) {
                     break;
                 }
                 if ("".equals(dialogEditExp.expName)) {
                     javax.swing.JOptionPane.showMessageDialog(null, "The experiment must have a name.", "Edit experiment", javax.swing.JOptionPane.ERROR_MESSAGE);
                 } else if (expController.getExperiment(dialogEditExp.expName) != null && expController.getExperiment(dialogEditExp.expName) != exp) {
                     javax.swing.JOptionPane.showMessageDialog(null, "There exists already an experiment with the same name.", "Edit experiment", javax.swing.JOptionPane.ERROR_MESSAGE);
                 } else {
                     break;
                 }
             }
             if (!dialogEditExp.canceled) {
                 String oldName = exp.getName();
                 String oldDescr = exp.getDescription();
                 exp.setName(dialogEditExp.expName);
                 exp.setDescription(dialogEditExp.expDesc);
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
         LinkedList<SortKey> sortKeys = new LinkedList<SortKey>();
         sortKeys.add(new SortKey(InstanceTableModel.COL_SELECTED, SortOrder.DESCENDING));
         tableInstances.getRowSorter().setSortKeys(sortKeys);
     }
 
     @Action
     public void btnRandomSelection() {
         JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
         EDACCExperimentModeRandomInstanceSelection random = new EDACCExperimentModeRandomInstanceSelection(mainFrame, true, this);
         random.setLocationRelativeTo(mainFrame);
         random.setVisible(true);
     }
 
     public void randomInstanceSelection(int count) throws Exception {
         LinkedList<Integer> idxs = new LinkedList<Integer>();
         for (int i = 0; i < tableInstances.getRowCount(); i++) {
             if (!(Boolean) tableInstances.getValueAt(i, tableInstances.convertColumnIndexToView(InstanceTableModel.COL_SELECTED))) {
                 idxs.add(i);
             }
         }
         if (idxs.size() < count) {
             throw new Exception("The number of instances which are not selected in the current view is less than count.");
         }
         Random random = new Random();
         while (count-- > 0) {
             int sel = random.nextInt(idxs.size());
             tableInstances.setValueAt(true, idxs.get(sel), tableInstances.convertColumnIndexToView(InstanceTableModel.COL_SELECTED));
             idxs.remove(sel);
         }
         insTableModel.fireTableDataChanged();
     }
 
     @Action
     public void btnSetPriority() {
         JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
         EDACCExperimentModePrioritySelection priorityDialog = new EDACCExperimentModePrioritySelection(mainFrame, true);
         priorityDialog.setLocationRelativeTo(mainFrame);
         priorityDialog.setVisible(true);
         final Integer priority = priorityDialog.getPriority();
         if (priority != null) {
             Tasks.startTask(new TaskRunnable() {
 
                 @Override
                 public void run(Tasks task) {
                     try {
                         expController.setPriority(priority);
                     } catch (Exception ex) {
                         javax.swing.JOptionPane.showMessageDialog(Tasks.getTaskView(), ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                     }
                 }
             });
 
         }
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
 
     @Override
     public void toggleSortOrder(int column) {
         synchronized (getModel()) {
             super.toggleSortOrder(column);
         }
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
             ExperimentResultsBrowserTableModel model = ((ExperimentResultsBrowserTableModel) this.getModel());
             int col = model.getIndexForColumn(column);
             if (col == ExperimentResultsBrowserTableModel.COL_STATUS) {
                 return "" + (char) (model.getStatus(row).getValue() + 68);
             }
             return model.getValueAt(row, column);
         }
 
         @Override
         public Integer getIdentifier(int row) {
             return delegate.getIdentifier(row);
         }
     }
 }
