 /*
  * EDACCExperimentMode.java
  *
  * Created on 02.01.2010, 00:25:47
  */
 package edacc;
 
 import edacc.events.TaskEvents;
 import edacc.experiment.AnalysisController;
 import edacc.experiment.AnalysisPanel;
 import edacc.experiment.ClientTableModel;
 import edacc.experiment.ClientUpdateThread;
 import edacc.experiment.ExperimentController;
 import edacc.experiment.ExperimentResultsBrowserTableModel;
 import edacc.experiment.ExperimentTableModel;
 import edacc.experiment.ExperimentUpdateThread;
 import edacc.experiment.GenerateJobsTableModel;
 import edacc.experiment.InstanceTableModel;
 import edacc.experiment.ResultsBrowserTableRowSorter;
 import edacc.experiment.SolverConfigUpdateThread;
 import edacc.experiment.SolverConfigurationTableModel;
 import edacc.experiment.SolverConfigurationTableRowFilter;
 import edacc.experiment.SolverTableModel;
 import edacc.experiment.TableColumnSelector;
 import edacc.experiment.Util;
 import edacc.gridqueues.GridQueuesController;
 import edacc.model.Client;
 import edacc.model.ClientDAO;
 import edacc.model.ComputationMethodDoesNotExistException;
 import edacc.model.DatabaseConnector;
 import edacc.model.Experiment;
 import edacc.model.ExperimentResult;
 import edacc.model.StatusCode;
 import edacc.model.InstanceClassMustBeSourceException;
 import edacc.model.NoConnectionToDBException;
 import edacc.model.ObjectCache;
 import edacc.model.PropertyNotInDBException;
 import edacc.model.SolverBinaries;
 import edacc.model.SolverConfiguration;
 import edacc.model.SolverConfigurationDAO;
 import edacc.model.TaskCancelledException;
 import edacc.model.TaskRunnable;
 import edacc.model.Tasks;
 import edacc.properties.PropertyTypeNotExistException;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import javax.swing.JFileChooser;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.JViewport;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.table.TableRowSorter;
 import javax.swing.RowSorter.SortKey;
 import javax.swing.SortOrder;
 import javax.swing.event.CellEditorListener;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 /**
  *
  * @author simon
  */
 public class EDACCExperimentMode extends javax.swing.JPanel implements TaskEvents {
 
     /**
      * The experiment tab index.
      */
     public static final int TAB_EXPERIMENTS = 0;
     /**
      * The client bowser tab index.
      */
     public static final int TAB_CLIENTBROWSER = 1;
     /**
      * The solvers tab index.
      */
     public static final int TAB_SOLVERS = 2;
     /**
      * The instances tab index.
      */
     public static final int TAB_INSTANCES = 3;
     /**
      * The generate jobs tab index.
      */
     public static final int TAB_GENERATEJOBS = 4;
     /**
      * The job browser tab index.
      */
     public static final int TAB_JOBBROWSER = 5;
     /**
      * The analysis tab index.
      */
     public static final int TAB_ANALYSIS = 6;
     private ExperimentController expController;
     /**
      * The table model for the experiment table. Will be created on object constructions and never be recreated.
      */
     public ExperimentTableModel expTableModel;
     /**
      * The table model for the instance table. Will be created on object construction and never be recreated.
      */
     public InstanceTableModel insTableModel;
     /**
      * The table model for the solver table. Will be created on object construction and never be recreated.
      */
     public SolverTableModel solTableModel;
     /**
      * The table model for the jobs table. Will be created on object construction and never be recreated.
      */
     public ExperimentResultsBrowserTableModel jobsTableModel;
     /**
      * The table model for the generate jobs table. Will be created on object construction and never be recreated.
      */
     public GenerateJobsTableModel generateJobsTableModel;
     private EDACCSolverConfigPanel solverConfigPanel;
     private TableRowSorter<InstanceTableModel> sorter;
     /**
      * The filter for the job browser. Will be created on object construction and never be recreated.
      */
     public EDACCFilter resultBrowserRowFilter;
     /**
      * The tree model for the instance classes. Will be created on object construction and never be recreated.
      */
     public DefaultTreeModel instanceClassTreeModel;
     private EDACCInstanceFilter instanceFilter;
     private EDACCOutputViewer outputViewer;
     private EDACCExperimentModeJobsCellRenderer tableJobsStringRenderer;
     private ResultsBrowserTableRowSorter resultsBrowserTableRowSorter;
     private ClientTableModel clientTableModel;
     /**
      * The panel for the solver configurations in the solvers tab. Will be created on object construction and never be recreated.
      */
     public EDACCExperimentModeSolverConfigurationTablePanel solverConfigTablePanel;
     private AnalysisPanel analysePanel;
     private Timer jobsTimer = null;
     private Integer resultBrowserETA;
     private boolean jobsTimerWasActive = false;
     private boolean updateJobsTableColumnWidth = false;
     private boolean tableExperimentsWasEditing = false;
     private ExperimentUpdateThread experimentUpdateThread;
     private ClientUpdateThread clientUpdateThread;
     private SolverConfigUpdateThread solverConfigUpdateThread;
     /**
      * The table model for the solver configuration table. Will be created on object construction and never be recreated.
      */
     public SolverConfigurationTableModel solverConfigTableModel;
     /**
      * The filter for the solver configuration table. Will be created on object construction and never be recreated.
      */
     public SolverConfigurationTableRowFilter solverConfigurationTableRowFilter;
     private TableColumnSelector jobsColumnSelector;
     private TableColumnSelector instanceColumnSelector;
 
     /** Creates new form EDACCExperimentMode */
     @SuppressWarnings("LeakingThisInConstructor")
     public EDACCExperimentMode() {
         initComponents();
         expController = new ExperimentController(this, solverConfigPanel);
         /* -------------------------------- experiment tab -------------------------------- */
         expTableModel = new ExperimentTableModel(false);
         tableExperiments.setModel(expTableModel);
         tableExperiments.setRowSorter(new TableRowSorter<ExperimentTableModel>(expTableModel));
         //tableExperiments.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "");
         tableExperiments.getDefaultEditor(Integer.class).addCellEditorListener(new CellEditorListener() {
 
             @Override
             public void editingStopped(ChangeEvent e) {
                 tableExperimentsWasEditing = true;
             }
 
             @Override
             public void editingCanceled(ChangeEvent e) {
                 tableExperimentsWasEditing = true;
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
 
         experimentUpdateThread = new ExperimentUpdateThread(expTableModel);
 
         /* -------------------------------- end of experiment tab -------------------------------- */
         /* -------------------------------- client browser tab -------------------------------- */
         clientTableModel = new ClientTableModel();
         tblClients.setModel(clientTableModel);
         tblClients.setDefaultRenderer(Object.class, new EDACCExperimentModeClientCellRenderer());
         tblClients.setDefaultRenderer(String.class, new EDACCExperimentModeClientCellRenderer());
         tblClients.setDefaultRenderer(Integer.class, new EDACCExperimentModeClientCellRenderer());
         tblClients.setDefaultRenderer(Float.class, new EDACCExperimentModeClientCellRenderer());
         tblClients.setDefaultRenderer(Long.class, new EDACCExperimentModeClientCellRenderer());
         tblClients.setDefaultRenderer(Boolean.class, new EDACCExperimentModeClientCellRenderer());
         clientUpdateThread = new ClientUpdateThread(clientTableModel);
         /* -------------------------------- end of client browser tab -------------------------------- */
         /* -------------------------------- solver tab -------------------------------- */
         jScrollPane4.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
         solverConfigTablePanel = new EDACCExperimentModeSolverConfigurationTablePanel(solverConfigPanel);
         solTableModel = new SolverTableModel();
         tableSolvers.setModel(solTableModel);
         Util.addSpaceSelection(tableSolvers, SolverTableModel.COL_SELECTED);
         solverConfigPanel.setParent(this);
         solverConfigTableModel = new SolverConfigurationTableModel();
 
         TableRowSorter tableSolverConfigurationsRowSorter = new TableRowSorter<SolverConfigurationTableModel>(solverConfigTableModel);
         solverConfigurationTableRowFilter = new SolverConfigurationTableRowFilter();
         tableSolverConfigurationsRowSorter.setRowFilter(solverConfigurationTableRowFilter);
         tblSolverConfigs.setRowSorter(tableSolverConfigurationsRowSorter);
         tblSolverConfigs.setModel(solverConfigTableModel);
         Util.addSpaceSelection(tblSolverConfigs, SolverConfigurationTableModel.COL_SEL);
         tableSolvers.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 solverConfigurationTableRowFilter.clearSolverBinaryIds();
                 for (int rowView : tableSolvers.getSelectedRows()) {
                     int rowModel = tableSolvers.convertRowIndexToModel(rowView);
                     for (SolverBinaries sb : solTableModel.getSolver(rowModel).getSolverBinaries()) {
                         solverConfigurationTableRowFilter.addSolverBinaryId(sb.getId());
                     }
 
                 }
                 solverConfigTableModel.fireTableDataChanged();
                 tblSolverConfigs.repaint();
             }
         });
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
         instanceColumnSelector = new TableColumnSelector(tableInstances);
         Util.addSpaceSelection(tableInstances, InstanceTableModel.COL_SELECTED);
         /* -------------------------------- end of instances tab -------------------------------- */
         /* -------------------------------- generate jobs tab -------------------------------- */
         generateJobsTableModel = new GenerateJobsTableModel(expController);
         tblGenerateJobs.setModel(generateJobsTableModel);
         tblGenerateJobs.setDefaultRenderer(Integer.class, new EDACCExperimentModeGenerateJobsTableCellRenderer());
         generateJobsTableModel.addTableModelListener(new TableModelListener() {
 
             @Override
             public void tableChanged(TableModelEvent e) {
                 setGenerateJobsTitle();
             }
         });
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
                 resultBrowserRowFilter = new EDACCFilter(EDACCApp.getApplication().getMainFrame(), true, tableJobs, false);
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
                 if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_R) {
                     btnRefreshJobsActionPerformed(null);
                 }
             }
         });
         jobsColumnSelector = new TableColumnSelector(tableJobs);
         resetJobsColumnVisibility();
         //jScrollPane6.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
         /* -------------------------------- end of jobs browser tab -------------------------------- */
         /* -------------------------------- analyze tab -------------------------------- */
         analysePanel = new AnalysisPanel(expController);
         panelAnalysis.setViewportView(analysePanel);
         /* -------------------------------- end of analyze tab -------------------------------- */
 
 
         manageExperimentPane.setEnabledAt(TAB_SOLVERS, false);
         manageExperimentPane.setEnabledAt(TAB_INSTANCES, false);
         manageExperimentPane.setEnabledAt(TAB_GENERATEJOBS, false);
         manageExperimentPane.setEnabledAt(TAB_JOBBROWSER, false);
         manageExperimentPane.setEnabledAt(TAB_ANALYSIS, false);
         manageExperimentPane.setTitleAt(TAB_EXPERIMENTS, "Experiments");
         btnDiscardExperiment.setEnabled(false);
         btnImport.setEnabled(false);
 
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
 
     private void jobsTableRepaintRow(int rowIndex) {
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
 
     private void tableJobsProcessMouseEvent(MouseEvent e) {
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
             int col = tableJobs.convertColumnIndexToModel(col_view);
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
 
     private void tableJobsProcessMouseMotionEvent(MouseEvent e) {
         int col_view = tableJobs.columnAtPoint(e.getPoint());
         int col = tableJobs.convertColumnIndexToModel(col_view);
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
 
     /**
      * Reinitializes the gui. Stops all running threads and calls the reinitialization methods.
      * @see reinitialize*()
      */
     public void reinitializeGUI() {
         if (experimentUpdateThread != null) {
             experimentUpdateThread.cancel(true);
         }
         if (clientUpdateThread != null) {
             clientUpdateThread.cancel(true);
         }
         if (solverConfigUpdateThread != null) {
             solverConfigUpdateThread.cancel(true);
         }
         expController.unloadExperiment();
         reinitializeExperiments();
         reinitializeInstances();
         reinitializeSolvers();
         reinitializeJobBrowser();
     }
 
     /**
      * Reinitializes the experiment tab.
      */
     public void reinitializeExperiments() {
         expTableModel.setExperiments(null);
     }
 
     /**
      * Reinitializes the instances tab, i.e. clears all filters and instance class selections. It also resets the visible columns.
      */
     public void reinitializeInstances() {
         instanceFilter.clearFilters();
         jTreeInstanceClass.setSelectionPath(null);
         for (int i = jTreeInstanceClass.getRowCount() - 1; i >= 0; i--) {
             jTreeInstanceClass.collapseRow(i);
         }
         lblFilterStatus.setText("");
         instanceColumnSelector = new TableColumnSelector(tableInstances);
         resetInstanceColumnVisibility();
         instanceFilter.setFilterInstanceClasses(false);
         insTableModel.fireTableDataChanged();
         edacc.experiment.Util.updateTableColumnWidth(tableInstances);
         instanceFilter.setFilterInstanceClasses(true);
         insTableModel.fireTableDataChanged();
     }
 
     /**
      * Reinitializes the solvers tab.
      */
     public void reinitializeSolvers() {
         jScrollPane4.setViewportView(solverConfigPanel);
     }
 
     /**
      * Reinitializes the job browser, i.e. clears the jobs table, clears the filters and resets the column visibility.
      */
     public void reinitializeJobBrowser() {
         try {
             jobsTableModel.setJobs(null);
             jobsTableModel.fireTableStructureChanged();
         } catch (SQLException ex) {
         }
         resultBrowserRowFilter.clearFilters();
         resetJobsColumnVisibility();
         updateJobsFilterStatus();
         jobsTimerWasActive = false;
     }
 
     /**
      * initializes the experiment mode. Should be called when changing from manage db mode to experiment mode.<br/>
      * Will throw an exception on errors.
      * @throws SQLException
      * @throws InstanceClassMustBeSourceException
      * @throws IOException
      * @throws NoConnectionToDBException
      * @throws PropertyNotInDBException
      * @throws PropertyTypeNotExistException
      * @throws ComputationMethodDoesNotExistException 
      */
     public void initialize() throws SQLException, InstanceClassMustBeSourceException, IOException, NoConnectionToDBException, PropertyNotInDBException, PropertyTypeNotExistException, ComputationMethodDoesNotExistException {
         btnRemoveExperiment.setEnabled(false);
         btnEditExperiment.setEnabled(false);
         btnLoadExperiment.setEnabled(false);
         expController.initialize();
         if (experimentUpdateThread == null || experimentUpdateThread.isDone()) {
             experimentUpdateThread = new ExperimentUpdateThread(expTableModel);
         }
         experimentUpdateThread.execute();
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 edacc.experiment.Util.updateTableColumnWidth(tableExperiments);
                 reinitializeInstances();
             }
         });
 
         // reload all columns
         solTableModel.fireTableStructureChanged();
 
         boolean isCompetition = DatabaseConnector.getInstance().isCompetitionDB();
         // if it is no competition db, then remove the competition columns
         if (!isCompetition) {
             tableSolvers.removeColumn(tableSolvers.getColumnModel().getColumn(SolverTableModel.COL_CATEGORIES));
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         popupTblClients = new javax.swing.JPopupMenu();
         menuSendMessage = new javax.swing.JMenuItem();
         jSeparator1 = new javax.swing.JPopupMenu.Separator();
         menuKillSoft = new javax.swing.JMenuItem();
         menuKillHard = new javax.swing.JMenuItem();
         jSeparator2 = new javax.swing.JPopupMenu.Separator();
         menuRemoveDeadClients = new javax.swing.JMenuItem();
         manageExperimentPane = new javax.swing.JTabbedPane();
         panelManageExperiment = new javax.swing.JPanel();
         scrollPaneExperimentsTable = new javax.swing.JScrollPane();
         tableExperiments = new javax.swing.JTable();
         jPanel7 = new javax.swing.JPanel();
         btnDiscardExperiment = new javax.swing.JButton();
         btnRemoveExperiment = new javax.swing.JButton();
         btnLoadExperiment = new javax.swing.JButton();
         btnCreateExperiment = new javax.swing.JButton();
         btnEditExperiment = new javax.swing.JButton();
         btnImport = new javax.swing.JButton();
         panelClientBrowser = new javax.swing.JPanel();
         jScrollPane7 = new javax.swing.JScrollPane();
         tblClients = new javax.swing.JTable();
         panelChooseSolver = new javax.swing.JPanel();
         splitPaneSolverSolverConfigs = new javax.swing.JSplitPane();
         jPanel2 = new javax.swing.JPanel();
         splitPaneSolver = new javax.swing.JSplitPane();
         jPanel8 = new javax.swing.JPanel();
         jScrollPane3 = new javax.swing.JScrollPane();
         tableSolvers = new javax.swing.JTable();
         btnSelectAllSolvers = new javax.swing.JButton();
         btnDeselectAllSolvers = new javax.swing.JButton();
         btnReverseSolverSelection = new javax.swing.JButton();
         btnChooseSolvers = new javax.swing.JButton();
         jPanel9 = new javax.swing.JPanel();
         jScrollPane8 = new javax.swing.JScrollPane();
         tblSolverConfigs = new javax.swing.JTable();
         btnChooseSolverConfigs = new javax.swing.JButton();
         btnSelectAllSolverConfigs = new javax.swing.JButton();
         btnDeselectAllSolverConfigs = new javax.swing.JButton();
         btnInvertSolverConfigSelection = new javax.swing.JButton();
         jScrollPane4 = new javax.swing.JScrollPane();
         btnSaveSolverConfigurations = new javax.swing.JButton();
         btnUndoSolverConfigurations = new javax.swing.JButton();
         btnChangeView = new javax.swing.JButton();
         btnImportSolverConfigs = new javax.swing.JButton();
         panelChooseInstances = new javax.swing.JPanel();
         jSplitPane2 = new javax.swing.JSplitPane();
         jPanel4 = new javax.swing.JPanel();
         jScrollPane5 = new javax.swing.JScrollPane();
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
         btnInstanceColumnSelection = new javax.swing.JButton();
         lblFilterStatus = new javax.swing.JLabel();
         panelExperimentParams = new javax.swing.JPanel();
         jPanel1 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         tblGenerateJobs = new MultipleCellSelectionTable();
         jPanel6 = new javax.swing.JPanel();
         btnSelectQueue = new javax.swing.JButton();
         btnGeneratePackage = new javax.swing.JButton();
         btnGenerateJobs = new javax.swing.JButton();
         btnSetNumRuns = new javax.swing.JButton();
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
 
         popupTblClients.setName("popupTblClients"); // NOI18N
 
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(edacc.EDACCApp.class).getContext().getResourceMap(EDACCExperimentMode.class);
         menuSendMessage.setText(resourceMap.getString("menuSendMessage.text")); // NOI18N
         menuSendMessage.setName("menuSendMessage"); // NOI18N
         menuSendMessage.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuSendMessageActionPerformed(evt);
             }
         });
         popupTblClients.add(menuSendMessage);
 
         jSeparator1.setName("jSeparator1"); // NOI18N
         popupTblClients.add(jSeparator1);
 
         menuKillSoft.setText(resourceMap.getString("menuKillSoft.text")); // NOI18N
         menuKillSoft.setName("menuKillSoft"); // NOI18N
         menuKillSoft.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuKillSoftActionPerformed(evt);
             }
         });
         popupTblClients.add(menuKillSoft);
 
         menuKillHard.setText(resourceMap.getString("menuKillHard.text")); // NOI18N
         menuKillHard.setName("menuKillHard"); // NOI18N
         menuKillHard.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuKillHardActionPerformed(evt);
             }
         });
         popupTblClients.add(menuKillHard);
 
         jSeparator2.setName("jSeparator2"); // NOI18N
         popupTblClients.add(jSeparator2);
 
         menuRemoveDeadClients.setText(resourceMap.getString("menuRemoveDeadClients.text")); // NOI18N
         menuRemoveDeadClients.setName("menuRemoveDeadClients"); // NOI18N
         menuRemoveDeadClients.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuRemoveDeadClientsActionPerformed(evt);
             }
         });
         popupTblClients.add(menuRemoveDeadClients);
 
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
         tableExperiments.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 tableExperimentsKeyReleased(evt);
             }
         });
         scrollPaneExperimentsTable.setViewportView(tableExperiments);
 
         jPanel7.setName("jPanel7"); // NOI18N
         jPanel7.setPreferredSize(new java.awt.Dimension(0, 0));
 
         btnDiscardExperiment.setText(resourceMap.getString("btnDiscardExperiment.text")); // NOI18N
         btnDiscardExperiment.setToolTipText(resourceMap.getString("btnDiscardExperiment.toolTipText")); // NOI18N
         btnDiscardExperiment.setMaximumSize(new java.awt.Dimension(69, 23));
         btnDiscardExperiment.setMinimumSize(new java.awt.Dimension(69, 23));
         btnDiscardExperiment.setName("btnDiscardExperiment"); // NOI18N
         btnDiscardExperiment.setPreferredSize(new java.awt.Dimension(80, 25));
         btnDiscardExperiment.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnDiscardExperimentActionPerformed(evt);
             }
         });
 
         btnRemoveExperiment.setText(resourceMap.getString("btnRemoveExperiment.text")); // NOI18N
         btnRemoveExperiment.setToolTipText(resourceMap.getString("btnRemoveExperiment.toolTipText")); // NOI18N
         btnRemoveExperiment.setName("btnRemoveExperiment"); // NOI18N
         btnRemoveExperiment.setPreferredSize(new java.awt.Dimension(80, 25));
         btnRemoveExperiment.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemoveExperimentActionPerformed(evt);
             }
         });
 
         btnLoadExperiment.setText(resourceMap.getString("btnLoadExperiment.text")); // NOI18N
         btnLoadExperiment.setToolTipText(resourceMap.getString("btnLoadExperiment.toolTipText")); // NOI18N
         btnLoadExperiment.setName("btnLoadExperiment"); // NOI18N
         btnLoadExperiment.setPreferredSize(new java.awt.Dimension(80, 25));
         btnLoadExperiment.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnLoadExperimentActionPerformed(evt);
             }
         });
 
         btnCreateExperiment.setText(resourceMap.getString("btnCreateExperiment.text")); // NOI18N
         btnCreateExperiment.setToolTipText(resourceMap.getString("btnCreateExperiment.toolTipText")); // NOI18N
         btnCreateExperiment.setName("btnCreateExperiment"); // NOI18N
         btnCreateExperiment.setPreferredSize(new java.awt.Dimension(80, 25));
         btnCreateExperiment.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCreateExperimentActionPerformed(evt);
             }
         });
 
         btnEditExperiment.setText(resourceMap.getString("btnEditExperiment.text")); // NOI18N
         btnEditExperiment.setToolTipText(resourceMap.getString("btnEditExperiment.toolTipText")); // NOI18N
         btnEditExperiment.setMaximumSize(new java.awt.Dimension(71, 23));
         btnEditExperiment.setMinimumSize(new java.awt.Dimension(71, 23));
         btnEditExperiment.setName("btnEditExperiment"); // NOI18N
         btnEditExperiment.setPreferredSize(new java.awt.Dimension(80, 25));
         btnEditExperiment.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnEditExperimentActionPerformed(evt);
             }
         });
 
         btnImport.setText(resourceMap.getString("btnImport.text")); // NOI18N
         btnImport.setName("btnImport"); // NOI18N
         btnImport.setPreferredSize(new java.awt.Dimension(80, 25));
         btnImport.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnImportActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
         jPanel7.setLayout(jPanel7Layout);
         jPanel7Layout.setHorizontalGroup(
             jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 520, Short.MAX_VALUE)
                 .addComponent(btnCreateExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnRemoveExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnEditExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(26, 26, 26)
                 .addComponent(btnDiscardExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnLoadExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         jPanel7Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCreateExperiment, btnDiscardExperiment, btnLoadExperiment, btnRemoveExperiment});
 
         jPanel7Layout.setVerticalGroup(
             jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel7Layout.createSequentialGroup()
                 .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(btnCreateExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(btnRemoveExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(btnEditExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(btnImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(btnLoadExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(btnDiscardExperiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout panelManageExperimentLayout = new javax.swing.GroupLayout(panelManageExperiment);
         panelManageExperiment.setLayout(panelManageExperimentLayout);
         panelManageExperimentLayout.setHorizontalGroup(
             panelManageExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelManageExperimentLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(scrollPaneExperimentsTable, javax.swing.GroupLayout.DEFAULT_SIZE, 1096, Short.MAX_VALUE)
                 .addGap(10, 10, 10))
             .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1116, Short.MAX_VALUE)
         );
         panelManageExperimentLayout.setVerticalGroup(
             panelManageExperimentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelManageExperimentLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(scrollPaneExperimentsTable, javax.swing.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         manageExperimentPane.addTab("Experiments", panelManageExperiment);
 
         panelClientBrowser.setName("panelClientBrowser"); // NOI18N
 
         jScrollPane7.setName("jScrollPane7"); // NOI18N
 
         tblClients.setAutoCreateRowSorter(true);
         tblClients.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
 
             }
         ));
         tblClients.setComponentPopupMenu(popupTblClients);
         tblClients.setName("tblClients"); // NOI18N
         tblClients.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tblClientsMouseClicked(evt);
             }
         });
         jScrollPane7.setViewportView(tblClients);
 
         javax.swing.GroupLayout panelClientBrowserLayout = new javax.swing.GroupLayout(panelClientBrowser);
         panelClientBrowser.setLayout(panelClientBrowserLayout);
         panelClientBrowserLayout.setHorizontalGroup(
             panelClientBrowserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 1116, Short.MAX_VALUE)
         );
         panelClientBrowserLayout.setVerticalGroup(
             panelClientBrowserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
         );
 
         manageExperimentPane.addTab(resourceMap.getString("panelClientBrowser.TabConstraints.tabTitle"), panelClientBrowser); // NOI18N
 
         panelChooseSolver.setName("panelChooseSolver"); // NOI18N
 
         splitPaneSolverSolverConfigs.setDividerLocation(0.5);
         splitPaneSolverSolverConfigs.setName("splitPaneSolverSolverConfigs"); // NOI18N
         splitPaneSolverSolverConfigs.setPreferredSize(new java.awt.Dimension(0, 0));
 
         jPanel2.setName("jPanel2"); // NOI18N
 
         splitPaneSolver.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
         splitPaneSolver.setName("splitPaneSolver"); // NOI18N
 
         jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel8.border.title"))); // NOI18N
         jPanel8.setName("jPanel8"); // NOI18N
         jPanel8.setPreferredSize(new java.awt.Dimension(0, 0));
 
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
 
         btnSelectAllSolvers.setText(resourceMap.getString("btnSelectAllSolvers.text")); // NOI18N
         btnSelectAllSolvers.setToolTipText(resourceMap.getString("btnSelectAllSolvers.toolTipText")); // NOI18N
         btnSelectAllSolvers.setName("btnSelectAllSolvers"); // NOI18N
         btnSelectAllSolvers.setPreferredSize(new java.awt.Dimension(125, 25));
         btnSelectAllSolvers.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSelectAllSolversActionPerformed(evt);
             }
         });
 
         btnDeselectAllSolvers.setText(resourceMap.getString("btnDeselectAllSolvers.text")); // NOI18N
         btnDeselectAllSolvers.setToolTipText(resourceMap.getString("btnDeselectAllSolvers.toolTipText")); // NOI18N
         btnDeselectAllSolvers.setName("btnDeselectAllSolvers"); // NOI18N
         btnDeselectAllSolvers.setPreferredSize(new java.awt.Dimension(125, 25));
         btnDeselectAllSolvers.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnDeselectAllSolversActionPerformed(evt);
             }
         });
 
         btnReverseSolverSelection.setText(resourceMap.getString("btnReverseSolverSelection.text")); // NOI18N
         btnReverseSolverSelection.setToolTipText(resourceMap.getString("btnReverseSolverSelection.toolTipText")); // NOI18N
         btnReverseSolverSelection.setName("btnReverseSolverSelection"); // NOI18N
         btnReverseSolverSelection.setPreferredSize(new java.awt.Dimension(125, 25));
         btnReverseSolverSelection.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnReverseSolverSelectionActionPerformed(evt);
             }
         });
 
         btnChooseSolvers.setText(resourceMap.getString("btnChooseSolvers.text")); // NOI18N
         btnChooseSolvers.setToolTipText(resourceMap.getString("btnChooseSolvers.toolTipText")); // NOI18N
         btnChooseSolvers.setName("btnChooseSolvers"); // NOI18N
         btnChooseSolvers.setPreferredSize(new java.awt.Dimension(125, 25));
         btnChooseSolvers.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnChooseSolversActionPerformed(evt);
             }
         });
 
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
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnChooseSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(534, Short.MAX_VALUE))
             .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 1052, Short.MAX_VALUE)
         );
         jPanel8Layout.setVerticalGroup(
             jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnSelectAllSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnDeselectAllSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnReverseSolverSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnChooseSolvers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
         );
 
         splitPaneSolver.setLeftComponent(jPanel8);
 
         jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel9.border.title"))); // NOI18N
         jPanel9.setName("jPanel9"); // NOI18N
         jPanel9.setPreferredSize(new java.awt.Dimension(0, 0));
 
         jScrollPane8.setName("jScrollPane8"); // NOI18N
 
         tblSolverConfigs.setModel(new javax.swing.table.DefaultTableModel(
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
         tblSolverConfigs.setName("tblSolverConfigs"); // NOI18N
         jScrollPane8.setViewportView(tblSolverConfigs);
 
         btnChooseSolverConfigs.setText(resourceMap.getString("btnChooseSolverConfigs.text")); // NOI18N
         btnChooseSolverConfigs.setName("btnChooseSolverConfigs"); // NOI18N
         btnChooseSolverConfigs.setPreferredSize(new java.awt.Dimension(125, 25));
         btnChooseSolverConfigs.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnChooseSolverConfigsActionPerformed(evt);
             }
         });
 
         btnSelectAllSolverConfigs.setText(resourceMap.getString("btnSelectAllSolverConfigs.text")); // NOI18N
         btnSelectAllSolverConfigs.setName("btnSelectAllSolverConfigs"); // NOI18N
         btnSelectAllSolverConfigs.setPreferredSize(new java.awt.Dimension(125, 25));
         btnSelectAllSolverConfigs.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSelectAllSolverConfigsActionPerformed(evt);
             }
         });
 
         btnDeselectAllSolverConfigs.setText(resourceMap.getString("btnDeselectAllSolverConfigs.text")); // NOI18N
         btnDeselectAllSolverConfigs.setName("btnDeselectAllSolverConfigs"); // NOI18N
         btnDeselectAllSolverConfigs.setPreferredSize(new java.awt.Dimension(125, 25));
         btnDeselectAllSolverConfigs.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnDeselectAllSolverConfigsActionPerformed(evt);
             }
         });
 
         btnInvertSolverConfigSelection.setText(resourceMap.getString("btnInvertSolverConfigSelection.text")); // NOI18N
         btnInvertSolverConfigSelection.setName("btnInvertSolverConfigSelection"); // NOI18N
         btnInvertSolverConfigSelection.setPreferredSize(new java.awt.Dimension(125, 25));
         btnInvertSolverConfigSelection.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnInvertSolverConfigSelectionActionPerformed(evt);
             }
         });
 
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
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnChooseSolverConfigs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(534, Short.MAX_VALUE))
             .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 1052, Short.MAX_VALUE)
         );
         jPanel9Layout.setVerticalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnSelectAllSolverConfigs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnDeselectAllSolverConfigs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnInvertSolverConfigSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnChooseSolverConfigs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
         );
 
         splitPaneSolver.setRightComponent(jPanel9);
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(splitPaneSolver, javax.swing.GroupLayout.DEFAULT_SIZE, 1066, Short.MAX_VALUE)
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(splitPaneSolver, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
         );
 
         splitPaneSolverSolverConfigs.setLeftComponent(jPanel2);
 
         jScrollPane4.setToolTipText(resourceMap.getString("jScrollPane4.toolTipText")); // NOI18N
         jScrollPane4.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         jScrollPane4.setName("jScrollPane4"); // NOI18N
         solverConfigPanel = new EDACCSolverConfigPanel();
         jScrollPane4.setViewportView(solverConfigPanel);
         jScrollPane4.getVerticalScrollBar().setUnitIncrement(30);
         splitPaneSolverSolverConfigs.setRightComponent(jScrollPane4);
 
         btnSaveSolverConfigurations.setText(resourceMap.getString("btnSaveSolverConfigurations.text")); // NOI18N
         btnSaveSolverConfigurations.setToolTipText(resourceMap.getString("btnSaveSolverConfigurations.toolTipText")); // NOI18N
         btnSaveSolverConfigurations.setName("btnSaveSolverConfigurations"); // NOI18N
         btnSaveSolverConfigurations.setPreferredSize(new java.awt.Dimension(109, 25));
         btnSaveSolverConfigurations.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSaveSolverConfigurationsActionPerformed(evt);
             }
         });
 
         btnUndoSolverConfigurations.setText(resourceMap.getString("btnUndoSolverConfigurations.text")); // NOI18N
         btnUndoSolverConfigurations.setToolTipText(resourceMap.getString("btnUndoSolverConfigurations.toolTipText")); // NOI18N
         btnUndoSolverConfigurations.setName("btnUndoSolverConfigurations"); // NOI18N
         btnUndoSolverConfigurations.setPreferredSize(new java.awt.Dimension(109, 25));
         btnUndoSolverConfigurations.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnUndoSolverConfigurationsActionPerformed(evt);
             }
         });
 
         btnChangeView.setText(resourceMap.getString("btnChangeView.text")); // NOI18N
         btnChangeView.setName("btnChangeView"); // NOI18N
         btnChangeView.setPreferredSize(new java.awt.Dimension(109, 25));
         btnChangeView.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnChangeViewActionPerformed(evt);
             }
         });
 
         btnImportSolverConfigs.setText(resourceMap.getString("btnImportSolverConfigs.text")); // NOI18N
         btnImportSolverConfigs.setName("btnImportSolverConfigs"); // NOI18N
         btnImportSolverConfigs.setPreferredSize(new java.awt.Dimension(125, 25));
         btnImportSolverConfigs.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnImportSolverConfigsActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout panelChooseSolverLayout = new javax.swing.GroupLayout(panelChooseSolver);
         panelChooseSolver.setLayout(panelChooseSolverLayout);
         panelChooseSolverLayout.setHorizontalGroup(
             panelChooseSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelChooseSolverLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelChooseSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(splitPaneSolverSolverConfigs, javax.swing.GroupLayout.DEFAULT_SIZE, 1096, Short.MAX_VALUE)
                     .addGroup(panelChooseSolverLayout.createSequentialGroup()
                         .addComponent(btnImportSolverConfigs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 632, Short.MAX_VALUE)
                         .addComponent(btnChangeView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnUndoSolverConfigurations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnSaveSolverConfigurations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         panelChooseSolverLayout.setVerticalGroup(
             panelChooseSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelChooseSolverLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(splitPaneSolverSolverConfigs, javax.swing.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelChooseSolverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnSaveSolverConfigurations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnUndoSolverConfigurations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnChangeView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnImportSolverConfigs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         manageExperimentPane.addTab("Solvers", panelChooseSolver);
 
         panelChooseInstances.setName("panelChooseInstances"); // NOI18N
         panelChooseInstances.setPreferredSize(new java.awt.Dimension(668, 623));
 
         jSplitPane2.setDividerLocation(0.4);
         jSplitPane2.setName("jSplitPane2"); // NOI18N
 
         jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel4.border.title"))); // NOI18N
         jPanel4.setMinimumSize(new java.awt.Dimension(200, 0));
         jPanel4.setName("jPanel4"); // NOI18N
 
         jScrollPane5.setName("jScrollPane5"); // NOI18N
 
         jTreeInstanceClass.setName("jTreeInstanceClass"); // NOI18N
         jTreeInstanceClass.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
             public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                 jTreeInstanceClassValueChanged(evt);
             }
         });
         jScrollPane5.setViewportView(jTreeInstanceClass);
 
         javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
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
 
         btnSaveInstances.setText(resourceMap.getString("btnSaveInstances.text")); // NOI18N
         btnSaveInstances.setToolTipText(resourceMap.getString("btnSaveInstances.toolTipText")); // NOI18N
         btnSaveInstances.setMaximumSize(new java.awt.Dimension(109, 23));
         btnSaveInstances.setMinimumSize(new java.awt.Dimension(109, 23));
         btnSaveInstances.setName("btnSaveInstances"); // NOI18N
         btnSaveInstances.setPreferredSize(new java.awt.Dimension(125, 25));
         btnSaveInstances.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSaveInstancesActionPerformed(evt);
             }
         });
 
         btnFilterInstances.setText(resourceMap.getString("btnFilterInstances.text")); // NOI18N
         btnFilterInstances.setToolTipText(resourceMap.getString("btnFilterInstances.toolTipText")); // NOI18N
         btnFilterInstances.setMaximumSize(new java.awt.Dimension(109, 23));
         btnFilterInstances.setMinimumSize(new java.awt.Dimension(109, 23));
         btnFilterInstances.setName("btnFilterInstances"); // NOI18N
         btnFilterInstances.setPreferredSize(new java.awt.Dimension(125, 25));
         btnFilterInstances.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnFilterInstancesActionPerformed(evt);
             }
         });
 
         btnSelectAllInstances.setText(resourceMap.getString("btnSelectAllInstances.text")); // NOI18N
         btnSelectAllInstances.setToolTipText(resourceMap.getString("btnSelectAllInstances.toolTipText")); // NOI18N
         btnSelectAllInstances.setMaximumSize(new java.awt.Dimension(109, 23));
         btnSelectAllInstances.setMinimumSize(new java.awt.Dimension(109, 23));
         btnSelectAllInstances.setName("btnSelectAllInstances"); // NOI18N
         btnSelectAllInstances.setPreferredSize(new java.awt.Dimension(125, 25));
         btnSelectAllInstances.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSelectAllInstancesActionPerformed(evt);
             }
         });
 
         btnDeselectAllInstances.setText(resourceMap.getString("btnDeselectAllInstances.text")); // NOI18N
         btnDeselectAllInstances.setToolTipText(resourceMap.getString("btnDeselectAllInstances.toolTipText")); // NOI18N
         btnDeselectAllInstances.setMaximumSize(new java.awt.Dimension(109, 23));
         btnDeselectAllInstances.setMinimumSize(new java.awt.Dimension(109, 23));
         btnDeselectAllInstances.setName("btnDeselectAllInstances"); // NOI18N
         btnDeselectAllInstances.setPreferredSize(new java.awt.Dimension(125, 25));
         btnDeselectAllInstances.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnDeselectAllInstancesActionPerformed(evt);
             }
         });
 
         btnInvertSelection.setText(resourceMap.getString("btnInvertSelection.text")); // NOI18N
         btnInvertSelection.setToolTipText(resourceMap.getString("btnInvertSelection.toolTipText")); // NOI18N
         btnInvertSelection.setName("btnInvertSelection"); // NOI18N
         btnInvertSelection.setPreferredSize(new java.awt.Dimension(125, 25));
         btnInvertSelection.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnInvertSelectionActionPerformed(evt);
             }
         });
 
         btnUndoInstances.setText(resourceMap.getString("btnUndoInstances.text")); // NOI18N
         btnUndoInstances.setToolTipText(resourceMap.getString("btnUndoInstances.toolTipText")); // NOI18N
         btnUndoInstances.setName("btnUndoInstances"); // NOI18N
         btnUndoInstances.setPreferredSize(new java.awt.Dimension(125, 25));
         btnUndoInstances.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnUndoInstancesActionPerformed(evt);
             }
         });
 
         btnSelectedInstances.setText(resourceMap.getString("btnSelectedInstances.text")); // NOI18N
         btnSelectedInstances.setName("btnSelectedInstances"); // NOI18N
         btnSelectedInstances.setPreferredSize(new java.awt.Dimension(125, 25));
         btnSelectedInstances.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSelectedInstancesActionPerformed(evt);
             }
         });
 
         btnRandomSelection.setText(resourceMap.getString("btnRandomSelection.text")); // NOI18N
         btnRandomSelection.setName("btnRandomSelection"); // NOI18N
         btnRandomSelection.setPreferredSize(new java.awt.Dimension(125, 25));
         btnRandomSelection.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRandomSelectionActionPerformed(evt);
             }
         });
 
         btnInstanceColumnSelection.setText(resourceMap.getString("btnInstanceColumnSelection.text")); // NOI18N
         btnInstanceColumnSelection.setName("btnInstanceColumnSelection"); // NOI18N
         btnInstanceColumnSelection.setPreferredSize(new java.awt.Dimension(125, 25));
         btnInstanceColumnSelection.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnInstanceColumnSelectionActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
         jPanel5.setLayout(jPanel5Layout);
         jPanel5Layout.setHorizontalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel5Layout.createSequentialGroup()
                         .addComponent(btnFilterInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(btnInstanceColumnSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 349, Short.MAX_VALUE)
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
                     .addComponent(btnSaveInstances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnInstanceColumnSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                         .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 889, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(lblFilterStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 889, Short.MAX_VALUE))
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
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         jSplitPane2.setRightComponent(jPanel3);
 
         javax.swing.GroupLayout panelChooseInstancesLayout = new javax.swing.GroupLayout(panelChooseInstances);
         panelChooseInstances.setLayout(panelChooseInstancesLayout);
         panelChooseInstancesLayout.setHorizontalGroup(
             panelChooseInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1116, Short.MAX_VALUE)
         );
         panelChooseInstancesLayout.setVerticalGroup(
             panelChooseInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
         );
 
         manageExperimentPane.addTab("Instances", panelChooseInstances);
 
         panelExperimentParams.setName("panelExperimentParams"); // NOI18N
 
         jPanel1.setName("jPanel1"); // NOI18N
 
         jScrollPane1.setName("jScrollPane1"); // NOI18N
 
         tblGenerateJobs.setAutoCreateRowSorter(true);
         tblGenerateJobs.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
 
             }
         ));
         tblGenerateJobs.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
         tblGenerateJobs.setAutoscrolls(false);
         tblGenerateJobs.setDoubleBuffered(true);
         tblGenerateJobs.setName("tblGenerateJobs"); // NOI18N
         tblGenerateJobs.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         jScrollPane1.setViewportView(tblGenerateJobs);
 
         jPanel6.setName("jPanel6"); // NOI18N
 
         btnSelectQueue.setText(resourceMap.getString("btnSelectQueue.text")); // NOI18N
         btnSelectQueue.setToolTipText(resourceMap.getString("btnSelectQueue.toolTipText")); // NOI18N
         btnSelectQueue.setName("btnSelectQueue"); // NOI18N
         btnSelectQueue.setPreferredSize(new java.awt.Dimension(190, 25));
         btnSelectQueue.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSelectQueueActionPerformed(evt);
             }
         });
 
         btnGeneratePackage.setText(resourceMap.getString("generatePackage.text")); // NOI18N
         btnGeneratePackage.setToolTipText(resourceMap.getString("generatePackage.toolTipText")); // NOI18N
         btnGeneratePackage.setName("generatePackage"); // NOI18N
         btnGeneratePackage.setPreferredSize(new java.awt.Dimension(190, 25));
         btnGeneratePackage.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnGeneratePackage(evt);
             }
         });
 
         btnGenerateJobs.setText(resourceMap.getString("btnGenerateJobs.text")); // NOI18N
         btnGenerateJobs.setToolTipText(resourceMap.getString("btnGenerateJobs.toolTipText")); // NOI18N
         btnGenerateJobs.setName("btnGenerateJobs"); // NOI18N
         btnGenerateJobs.setPreferredSize(new java.awt.Dimension(190, 25));
         btnGenerateJobs.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnGenerateJobsActionPerformed(evt);
             }
         });
 
         btnSetNumRuns.setText(resourceMap.getString("btnSetNumRuns.text")); // NOI18N
         btnSetNumRuns.setMaximumSize(new java.awt.Dimension(190, 25));
         btnSetNumRuns.setMinimumSize(new java.awt.Dimension(190, 25));
         btnSetNumRuns.setName("btnSetNumRuns"); // NOI18N
         btnSetNumRuns.setPreferredSize(new java.awt.Dimension(190, 25));
         btnSetNumRuns.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSetNumRunsActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
         jPanel6.setLayout(jPanel6Layout);
         jPanel6Layout.setHorizontalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addComponent(btnSelectQueue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnGeneratePackage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnSetNumRuns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 334, Short.MAX_VALUE)
                 .addComponent(btnGenerateJobs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         jPanel6Layout.setVerticalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnSelectQueue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnGenerateJobs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnGeneratePackage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnSetNumRuns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1116, Short.MAX_VALUE)
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
         jScrollPane6.setViewportView(tableJobs);
 
         btnRefreshJobs.setText(resourceMap.getString("btnRefreshJobs.text")); // NOI18N
         btnRefreshJobs.setToolTipText(resourceMap.getString("btnRefreshJobs.toolTipText")); // NOI18N
         btnRefreshJobs.setName("btnRefreshJobs"); // NOI18N
         btnRefreshJobs.setPreferredSize(new java.awt.Dimension(103, 25));
         btnRefreshJobs.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRefreshJobsActionPerformed(evt);
             }
         });
 
         btnBrowserColumnSelection.setText(resourceMap.getString("btnBrowserColumnSelection.text")); // NOI18N
         btnBrowserColumnSelection.setToolTipText(resourceMap.getString("btnBrowserColumnSelection.toolTipText")); // NOI18N
         btnBrowserColumnSelection.setName("btnBrowserColumnSelection"); // NOI18N
         btnBrowserColumnSelection.setPreferredSize(new java.awt.Dimension(103, 25));
         btnBrowserColumnSelection.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBrowserColumnSelectionActionPerformed(evt);
             }
         });
 
         btnFilterJobs.setText(resourceMap.getString("btnFilterJobs.text")); // NOI18N
         btnFilterJobs.setToolTipText(resourceMap.getString("btnFilterJobs.toolTipText")); // NOI18N
         btnFilterJobs.setName("btnFilterJobs"); // NOI18N
         btnFilterJobs.setPreferredSize(new java.awt.Dimension(103, 25));
         btnFilterJobs.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnFilterJobsActionPerformed(evt);
             }
         });
 
         btnExport.setText(resourceMap.getString("btnExport.text")); // NOI18N
         btnExport.setToolTipText(resourceMap.getString("btnExport.toolTipText")); // NOI18N
         btnExport.setName("btnExport"); // NOI18N
         btnExport.setPreferredSize(new java.awt.Dimension(103, 25));
         btnExport.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnExportActionPerformed(evt);
             }
         });
 
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
 
         btnSetPriority.setText(resourceMap.getString("btnSetPriority.text")); // NOI18N
         btnSetPriority.setName("btnSetPriority"); // NOI18N
         btnSetPriority.setPreferredSize(new java.awt.Dimension(103, 25));
         btnSetPriority.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSetPriorityActionPerformed(evt);
             }
         });
 
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
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 286, Short.MAX_VALUE)
                 .addComponent(jLabel5)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtJobsTimer, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(chkJobsTimer)
                 .addContainerGap())
             .addGroup(panelJobBrowserLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblJobsFilterStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 1096, Short.MAX_VALUE)
                 .addContainerGap())
             .addGroup(panelJobBrowserLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblETA)
                 .addContainerGap(1072, Short.MAX_VALUE))
             .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1116, Short.MAX_VALUE)
         );
 
         panelJobBrowserLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnBrowserColumnSelection, btnComputeResultProperties, btnExport, btnFilterJobs, btnRefreshJobs});
 
         panelJobBrowserLayout.setVerticalGroup(
             panelJobBrowserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelJobBrowserLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblJobsFilterStatus)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
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
                 .addComponent(manageExperimentPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1121, Short.MAX_VALUE)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(manageExperimentPane, javax.swing.GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE)
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Checks for unsaved changes.
      * @return true, iff there are some unsaved changes.
      */
     public boolean hasUnsavedChanges() {
         if (expController.getActiveExperiment() == null) {
             return false;
         }
         return solverConfigPanel.isModified() || insTableModel.isModified();
     }
 
     /**
      * Updates the tab titles and the status text according to the state of the GUI. 
      */
     public void setTitles() {
         if (solverConfigPanel.isModified()) {
             manageExperimentPane.setTitleAt(TAB_SOLVERS, "Solvers (modified)");
         } else {
             manageExperimentPane.setTitleAt(TAB_SOLVERS, "Solvers");
         }
         if (insTableModel.isModified()) {
             manageExperimentPane.setTitleAt(TAB_INSTANCES, "Instances (modified)");
         } else {
             manageExperimentPane.setTitleAt(TAB_INSTANCES, "Instances");
         }
         if (expController.getActiveExperiment() == null) {
             manageExperimentPane.setTitleAt(TAB_EXPERIMENTS, "Experiments");
             ((EDACCView) EDACCApp.getApplication().getMainView()).setStatusText("MANAGE EXPERIMENT MODE - Connected to database: " + DatabaseConnector.getInstance().getDatabase() + " on host: " + DatabaseConnector.getInstance().getHostname());
         } else {
             manageExperimentPane.setTitleAt(TAB_EXPERIMENTS, "Experiments (Active: " + expController.getActiveExperiment().getName() + ")");
             ((EDACCView) EDACCApp.getApplication().getMainView()).setStatusText("MANAGE EXPERIMENT MODE (Active: " + expController.getActiveExperiment().getName() + ") - Connected to database: " + DatabaseConnector.getInstance().getDatabase() + " on host: " + DatabaseConnector.getInstance().getHostname());
         }
         manageExperimentPane.invalidate();
     }
 
     /**
      * Updates the generate jobs tab title according to the state of this tab.
      */
     public void setGenerateJobsTitle() {
         if (generateJobsIsModified()) {
             manageExperimentPane.setTitleAt(TAB_GENERATEJOBS, "Generate Jobs (modified)");
         } else {
             manageExperimentPane.setTitleAt(TAB_GENERATEJOBS, "Generate Jobs");
         }
     }
 
     /**
      * Checks if the generate jobs table data is modified.
      * @return true, iff some data is modified.
      */
     public boolean generateJobsIsModified() {
         return expController.experimentResultsIsModified();
     }
 
     private void manageExperimentPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_manageExperimentPaneStateChanged
         switch (manageExperimentPane.getSelectedIndex()) {
             case TAB_EXPERIMENTS:
                 if (experimentUpdateThread != null) {
                     if (experimentUpdateThread.isDone()) {
                         experimentUpdateThread = new ExperimentUpdateThread(expTableModel);
                     }
                     experimentUpdateThread.execute();
                 }
                 break;
             case TAB_CLIENTBROWSER:
                 tblClients.getRowSorter().setSortKeys(new LinkedList());
                 if (clientUpdateThread != null) {
                     if (clientUpdateThread.isDone()) {
                         clientUpdateThread = new ClientUpdateThread(clientTableModel);
                     }
                     clientUpdateThread.execute();
                 }
                 break;
             case TAB_SOLVERS:
                 splitPaneSolverSolverConfigs.setDividerLocation(0.5);
                 splitPaneSolver.setDividerLocation(0.5);
 
                 edacc.experiment.Util.updateTableColumnWidth(tableSolvers);
                 if (solverConfigUpdateThread == null || solverConfigUpdateThread.isDone()) {
                     solverConfigUpdateThread = new SolverConfigUpdateThread(expController.solverConfigCache);
                 }
                 solverConfigUpdateThread.execute();
                 break;
             case TAB_INSTANCES:
                 instanceFilter.setFilterInstanceClasses(false);
                 insTableModel.fireTableDataChanged();
                 edacc.experiment.Util.updateTableColumnWidth(tableInstances);
                 instanceFilter.setFilterInstanceClasses(true);
                 insTableModel.fireTableDataChanged();
                 break;
             case TAB_GENERATEJOBS:
                 try {
                     if (GridQueuesController.getInstance().getChosenQueuesByExperiment(expController.getActiveExperiment()).isEmpty()) {
                         btnGeneratePackage.setEnabled(false);
                     } else {
                         btnGeneratePackage.setEnabled(true);
                     }
                 } catch (SQLException ex) {
                     btnGeneratePackage.setEnabled(false);
                 }
                 break;
             case TAB_JOBBROWSER:
                 final Rectangle rect = tableJobs.getVisibleRect();
 
                 resultBrowserETA = null;
                 lblETA.setText("");
                 try {
                     jobsTableModel.setJobs(null);
                 } catch (SQLException ex) {
                 }
                 if (jobsTableModel.updateProperties()) {
                     jobsColumnSelector = new TableColumnSelector(tableJobs);
                     resetJobsColumnVisibility();
                 }
 
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
                 break;
             case TAB_ANALYSIS:
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
                 break;
         }
         if (manageExperimentPane.getSelectedIndex() != TAB_JOBBROWSER) {
             jobsTimerWasActive = jobsTimer != null;
             stopJobsTimer();
         }
 
         if (experimentUpdateThread != null && manageExperimentPane.getSelectedIndex() != TAB_EXPERIMENTS) {
             experimentUpdateThread.cancel(true);
         }
         if (clientUpdateThread != null && manageExperimentPane.getSelectedIndex() != TAB_CLIENTBROWSER) {
             clientUpdateThread.cancel(true);
         }
         if (solverConfigUpdateThread != null && manageExperimentPane.getSelectedIndex() != TAB_SOLVERS) {
             solverConfigUpdateThread.cancel(true);
         }
     }//GEN-LAST:event_manageExperimentPaneStateChanged
 
     private ArrayList<Client> getSelectedClients() {
         ArrayList<Client> res = new ArrayList<Client>();
         if (tblClients.getSelectedRowCount() > 0) {
             for (int row : tblClients.getSelectedRows()) {
                 res.add(clientTableModel.getClientAt(tblClients.convertRowIndexToModel(row)));
             }
         }
         return res;
     }
 
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
             this.btnLoadExperimentActionPerformed(null);
         }
     }//GEN-LAST:event_tableExperimentsMouseClicked
 
     private void txtJobsTimerKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtJobsTimerKeyReleased
         txtJobsTimer.setText(Util.getNumberText(txtJobsTimer.getText()));
         if (chkJobsTimer.isSelected()) {
             chkJobsTimer.setSelected(false);
             stopJobsTimer();
         }
     }//GEN-LAST:event_txtJobsTimerKeyReleased
 
     private void btnComputeResultPropertiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnComputeResultPropertiesActionPerformed
         EDACCComputeResultProperties compute = new EDACCComputeResultProperties(EDACCApp.getApplication().getMainFrame(), true, expController.getActiveExperiment());
         compute.setLocationRelativeTo(EDACCApp.getApplication().getMainFrame());
         compute.setVisible(true);
         resultBrowserETA = null;
         Tasks.startTask("loadJobs", expController, this, false);
     }//GEN-LAST:event_btnComputeResultPropertiesActionPerformed
 
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
 
     private void btnGeneratePackage(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGeneratePackage
         EDACCGeneratePackageFileChooser packageFileChooser = new EDACCGeneratePackageFileChooser(EDACCApp.getApplication().getMainFrame(), true, expController);
         packageFileChooser.setLocationRelativeTo(EDACCApp.getApplication().getMainFrame());
         packageFileChooser.setName("EDACCGeneratePackageFileChooser");
         EDACCApp.getApplication().show(packageFileChooser);
 }//GEN-LAST:event_btnGeneratePackage
 
     private void btnSetNumRunsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetNumRunsActionPerformed
         MultipleCellSelectionTable table = (MultipleCellSelectionTable) tblGenerateJobs;
         EDACCExperimentModeNumRunsSetter dialogNumRuns = new EDACCExperimentModeNumRunsSetter(EDACCApp.getApplication().getMainFrame(), true, table.getSelectedCount() == 0);
         dialogNumRuns.setLocationRelativeTo(EDACCApp.getApplication().getMainFrame());
         EDACCApp.getApplication().show(dialogNumRuns);
         if (!dialogNumRuns.isCanceled()) {
             for (int row = 0; row < table.getRowCount(); row++) {
                 for (int col = 1; col < table.getColumnCount(); col++) {
                     if (dialogNumRuns.isAll() || table.isCellSelected(row, col)) {
                         generateJobsTableModel.setNumRuns(generateJobsTableModel.getInstance(tblGenerateJobs.convertRowIndexToModel(row)), generateJobsTableModel.getSolverConfiguration(tblGenerateJobs.convertColumnIndexToModel(col)), dialogNumRuns.getNumRuns());
                     }
                 }
             }
             generateJobsTableModel.fireTableDataChanged();
         }
     }//GEN-LAST:event_btnSetNumRunsActionPerformed
 
     private void btnChangeViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeViewActionPerformed
         if (jScrollPane4.getViewport().getView() == solverConfigTablePanel) {
             jScrollPane4.setViewportView(solverConfigPanel);
         } else {
             solverConfigTablePanel.update();
             jScrollPane4.setViewportView(solverConfigTablePanel);
         }
     }//GEN-LAST:event_btnChangeViewActionPerformed
 
     private void btnImportSolverConfigsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportSolverConfigsActionPerformed
         ObjectCache<SolverConfiguration> cache = SolverConfigurationDAO.cache;
         SolverConfigurationDAO.cache = new ObjectCache<SolverConfiguration>();
         EDACCExperimentModeImportSolverConfigs dialog = new EDACCExperimentModeImportSolverConfigs(EDACCApp.getApplication().getMainFrame(), true, expController);
         dialog.setName("EDACCExperimentModeImportSolverConfigs");
         dialog.setLocationRelativeTo(EDACCApp.getApplication().getMainFrame());
         EDACCApp.getApplication().show(dialog);
         SolverConfigurationDAO.cache = cache;
         if (!dialog.isCancelled()) {
             try {
                 for (SolverConfiguration sc : dialog.getSelectedSolverConfigurations()) {
                     solverConfigPanel.addSolverConfiguration(sc, false);
                 }
                 solverConfigTablePanel.update();
             } catch (Exception e) {
                 javax.swing.JOptionPane.showMessageDialog(null, "Could not import solver configurations: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
             }
         }
 
     }//GEN-LAST:event_btnImportSolverConfigsActionPerformed
 
     private void tableExperimentsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableExperimentsKeyReleased
         if (!tableExperimentsWasEditing && evt.getKeyCode() == KeyEvent.VK_ENTER) {
             btnLoadExperimentActionPerformed(null);
         }
         tableExperimentsWasEditing = false;
     }//GEN-LAST:event_tableExperimentsKeyReleased
 
     private void tblClientsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblClientsMouseClicked
         if (evt.getClickCount() == 2) {
             if (tblClients.getSelectedRow() != -1) {
                 Client client = clientTableModel.getClientAt(tblClients.convertRowIndexToModel(tblClients.getSelectedRow()));
                 EDACCExperimentModeClientDialog dialog = new EDACCExperimentModeClientDialog(EDACCApp.getApplication().getMainFrame(), true, client);
                 dialog.setName("EDACCExperimentModeClientDialog");
                 EDACCApp.getApplication().show(dialog);
             }
         }
     }//GEN-LAST:event_tblClientsMouseClicked
 
     private void menuSendMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSendMessageActionPerformed
         ArrayList<Client> clients = getSelectedClients();
         if (clients.isEmpty()) {
             JOptionPane.showMessageDialog(this, "You have to select some clients.", "Error", JOptionPane.ERROR_MESSAGE);
         } else {
             String message = JOptionPane.showInputDialog(this, "Message:", "Send Message", JOptionPane.QUESTION_MESSAGE);
             if (message != null) {
                 try {
                     for (Client c : clients) {
                         ClientDAO.sendMessage(c, message);
                     }
                 } catch (Exception ex) {
                     JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
     }//GEN-LAST:event_menuSendMessageActionPerformed
 
     private void menuKillSoftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuKillSoftActionPerformed
         ArrayList<Client> clients = getSelectedClients();
         if (clients.isEmpty()) {
             JOptionPane.showMessageDialog(this, "You have to select some clients.", "Error", JOptionPane.ERROR_MESSAGE);
         } else {
             if (JOptionPane.showConfirmDialog(this, "This will kill the selected clients after their current jobs are finished. Are you sure you want to continue?", "Kill Clients", JOptionPane.YES_NO_OPTION) == 0) {
                 try {
                     for (Client c : clients) {
                         ClientDAO.sendMessage(c, "kill_client soft");
                     }
                 } catch (Exception ex) {
                     JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
     }//GEN-LAST:event_menuKillSoftActionPerformed
 
     private void menuKillHardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuKillHardActionPerformed
         ArrayList<Client> clients = getSelectedClients();
         if (clients.isEmpty()) {
             JOptionPane.showMessageDialog(this, "You have to select some clients.", "Error", JOptionPane.ERROR_MESSAGE);
         } else {
             if (JOptionPane.showConfirmDialog(this, "This will kill the selected clients and their current jobs will be cancelled. Are you sure you want to continue?", "Kill Clients", JOptionPane.YES_NO_OPTION) == 0) {
                 try {
                     for (Client c : clients) {
                         ClientDAO.sendMessage(c, "kill_client hard");
                     }
                 } catch (Exception ex) {
                     JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
     }//GEN-LAST:event_menuKillHardActionPerformed
 
     private void menuRemoveDeadClientsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuRemoveDeadClientsActionPerformed
         if (JOptionPane.showConfirmDialog(this, "This will remove all dead clients from the client table. Are you sure you want to continue?", "Delete Clients", JOptionPane.YES_NO_OPTION) == 0) {
             try {
                 ClientDAO.removeDeadClients();
             } catch (Exception ex) {
                 JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             }
         }
     }//GEN-LAST:event_menuRemoveDeadClientsActionPerformed
 
     private void btnChooseSolverConfigsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseSolverConfigsActionPerformed
         try {
             for (int row = 0; row < tblSolverConfigs.getRowCount(); row ++) {
                 if (solverConfigTableModel.isSelected(tblSolverConfigs.convertRowIndexToModel(row))) {
                     solverConfigPanel.addSolverConfiguration(solverConfigTableModel.getSolverConfigurationAt(tblSolverConfigs.convertRowIndexToModel(row)), false);
                 }
             }
             solverConfigTablePanel.update();
         } catch (SQLException ex) {
             JOptionPane.showMessageDialog(this, "Error while saving solver configurations: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         }
     }//GEN-LAST:event_btnChooseSolverConfigsActionPerformed
 
     private void btnSelectAllSolverConfigsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectAllSolverConfigsActionPerformed
        for (int row = 0; row < solverConfigTableModel.getRowCount(); row++) {
            solverConfigTableModel.setValueAt(true, row, SolverConfigurationTableModel.COL_SEL);
         }
         solverConfigTableModel.fireTableDataChanged();
     }//GEN-LAST:event_btnSelectAllSolverConfigsActionPerformed
 
     private void btnDeselectAllSolverConfigsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeselectAllSolverConfigsActionPerformed
        for (int row = 0; row < solverConfigTableModel.getRowCount(); row++) {
            solverConfigTableModel.setValueAt(false, row, SolverConfigurationTableModel.COL_SEL);
         }
         solverConfigTableModel.fireTableDataChanged();
     }//GEN-LAST:event_btnDeselectAllSolverConfigsActionPerformed
 
     private void btnInvertSolverConfigSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInvertSolverConfigSelectionActionPerformed
        for (int row = 0; row < solverConfigTableModel.getRowCount(); row++) {
            solverConfigTableModel.setValueAt(!(Boolean) solverConfigTableModel.getValueAt(row, SolverConfigurationTableModel.COL_SEL), row, SolverConfigurationTableModel.COL_SEL);
         }
         solverConfigTableModel.fireTableDataChanged();
     }//GEN-LAST:event_btnInvertSolverConfigSelectionActionPerformed
 
     private void btnImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportActionPerformed
         if (expController.getActiveExperiment() == null) {
             JOptionPane.showMessageDialog(this, "You have to load an experiment.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
         if (this.hasUnsavedChanges()) {
             JOptionPane.showMessageDialog(this, "You have to save all unsaved data before importing data from another experiment.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
         final EDACCExperimentModeImport dialog = new EDACCExperimentModeImport(EDACCApp.getApplication().getMainFrame(), true, expController.getActiveExperiment());
         dialog.initializeData();
         dialog.setLocationRelativeTo(EDACCApp.getApplication().getMainFrame());
         dialog.setName("EDACCExperimentModeImportDialog");
         EDACCApp.getApplication().show(dialog);
         if (!dialog.isCancelled()) {
             Tasks.startTask(new TaskRunnable() {
 
                 @Override
                 public void run(Tasks task) {
                     try {
                         expController.importData(task, dialog.getSelectedSolverConfigs(), dialog.getSelectedInstances(), dialog.getImportFinishedRuns());
                     } catch (final Exception ex) {
                         SwingUtilities.invokeLater(new Runnable() {
 
                             @Override
                             public void run() {
                                 JOptionPane.showMessageDialog(EDACCExperimentMode.this, "Error while importing data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                             }
                         });
                     }
                 }
             });
         }
     }//GEN-LAST:event_btnImportActionPerformed
 
     private void btnInstanceColumnSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInstanceColumnSelectionActionPerformed
         List<SortKey> sortKeys = (List<SortKey>) tableInstances.getRowSorter().getSortKeys();
         List<String> columnNames = new ArrayList<String>();
         for (SortKey sk : sortKeys) {
             columnNames.add(tableInstances.getColumnName(tableInstances.convertColumnIndexToView(sk.getColumn())));
         }
         EDACCInstanceColumnSelection dialog = new EDACCInstanceColumnSelection(EDACCApp.getApplication().getMainFrame(), true, instanceColumnSelector);
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
         instanceFilter.setFilterInstanceClasses(false);
         insTableModel.fireTableDataChanged();
         edacc.experiment.Util.updateTableColumnWidth(tableInstances);
         instanceFilter.setFilterInstanceClasses(true);
         insTableModel.fireTableDataChanged();
         try {
             int col = tableInstances.getColumnModel().getColumnIndex("Selected");
             tableInstances.moveColumn(col, tableInstances.getColumnCount() - 1);
         } catch (Exception e) {
         }
     }//GEN-LAST:event_btnInstanceColumnSelectionActionPerformed
 
     private void btnLoadExperimentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadExperimentActionPerformed
         if (tableExperiments.getSelectedRow() != -1) {
             if (expController.getActiveExperiment() == null || !hasUnsavedChanges() || JOptionPane.showConfirmDialog(this,
                     "Loading an experiment will make you lose all unsaved changes of the current experiment. Continue loading the experiment?",
                     "Warning!",
                     JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                 Experiment selectedExperiment = expTableModel.getExperimentAt(tableExperiments.convertRowIndexToModel(tableExperiments.getSelectedRow()));
                 updateJobsTableColumnWidth = true;
                 Tasks.startTask("loadExperiment", new Class[]{Experiment.class, edacc.model.Tasks.class
                         },
                         new Object[]{
                             selectedExperiment, null}, expController,
                         this);
             }
         }
     }//GEN-LAST:event_btnLoadExperimentActionPerformed
 
     private void btnCreateExperimentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateExperimentActionPerformed
         btnDiscardExperimentActionPerformed(null);
         if (expController.getActiveExperiment() != null) {
             return;
         }
         JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
         final EDACCExperimentModeNewExp dialogNewExp = new EDACCExperimentModeNewExp(mainFrame, true);
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
                 Tasks.startTask(new TaskRunnable() {
 
                     @Override
                     public void run(Tasks task) {
                         Experiment newExp;
                         try {
                             newExp = expController.createExperiment(dialogNewExp.expName, dialogNewExp.expDesc);
                             if (experimentUpdateThread != null) {
                                 experimentUpdateThread.cancel(true);
                             }
                             if (experimentUpdateThread == null || experimentUpdateThread.isDone()) {
                                 experimentUpdateThread = new ExperimentUpdateThread(expTableModel);
                             }
                             experimentUpdateThread.execute();
                         } catch (SQLException ex) {
                             createDatabaseErrorMessage(ex);
                             return;
                         } catch (Exception ex) {
                             javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage(), "Create experiment", javax.swing.JOptionPane.ERROR_MESSAGE);
                             return;
                         }
                         try {
                             expController.loadExperiment(newExp, task);
                         } catch (Exception e) {
                             javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(), "Error while importing data", javax.swing.JOptionPane.ERROR_MESSAGE);
                         } finally {
                             tableExperiments.getSelectionModel().setSelectionInterval(tableExperiments.getRowCount() - 1, tableExperiments.getRowCount() - 1);
                             tableExperiments.requestFocusInWindow();
                         }
                     }
                 });
             }
         } catch (SQLException ex) {
             createDatabaseErrorMessage(ex);
         } finally {
             dialogNewExp.dispose();
         }
     }//GEN-LAST:event_btnCreateExperimentActionPerformed
 
     private void btnRemoveExperimentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveExperimentActionPerformed
         if (tableExperiments.getSelectedRow() != -1) {
             int removedIndex = tableExperiments.getSelectedRow();
             int userInput = javax.swing.JOptionPane.showConfirmDialog(Tasks.getTaskView(), "Do you really want to remove the experiment " + expTableModel.getValueAt(tableExperiments.convertRowIndexToModel(removedIndex), ExperimentTableModel.COL_NAME) + "?", "Remove experiment", javax.swing.JOptionPane.YES_NO_OPTION);
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
                     if (experimentUpdateThread != null) {
                         experimentUpdateThread.cancel(true);
                     }
                     if (experimentUpdateThread == null || experimentUpdateThread.isDone()) {
                         experimentUpdateThread = new ExperimentUpdateThread(expTableModel);
                     }
                     experimentUpdateThread.execute();
                 } catch (SQLException ex) {
                     createDatabaseErrorMessage(ex);
                 } catch (Exception e) {
                     javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
     }//GEN-LAST:event_btnRemoveExperimentActionPerformed
 
     private void btnChooseSolversActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseSolversActionPerformed
         solverConfigPanel.beginUpdate();
         for (int i = 0; i < solTableModel.getRowCount(); i++) {
             if (solTableModel.isSelected(i) && !solverConfigPanel.solverExists(solTableModel.getSolver(i).getId())) {
                 solverConfigPanel.addSolver(solTableModel.getSolver(i));
             } else if (!solTableModel.isSelected(i)) {
                 solverConfigPanel.removeSolver(solTableModel.getSolver(i), true);
             }
         }
         solverConfigPanel.endUpdate();
         if (jScrollPane4.getViewport().getView() == solverConfigTablePanel) {
             solverConfigTablePanel.update();
         }
         setTitles();
     }//GEN-LAST:event_btnChooseSolversActionPerformed
 
     private void btnSaveSolverConfigurationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveSolverConfigurationsActionPerformed
         Tasks.startTask(new TaskRunnable() {
 
             @Override
             public void run(Tasks task) {
                 try {
                     expController.saveSolverConfigurations(task);
                 } catch (final Exception e) {
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             EDACCExperimentMode.this.onTaskFailed("", e);
                         }
                     });
 
                 } finally {
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             setTitles();
                             setGenerateJobsTitle();
                         }
                     });
 
                 }
             }
         });
     }//GEN-LAST:event_btnSaveSolverConfigurationsActionPerformed
 
     private void btnUndoSolverConfigurationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUndoSolverConfigurationsActionPerformed
         Tasks.startTask(new TaskRunnable() {
 
             @Override
             public void run(Tasks task) {
                 try {
                     expController.undoSolverConfigurations(task);
                 } catch (final SQLException e) {
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             createDatabaseErrorMessage(e);
                         }
                     });
                 } finally {
                     if (jScrollPane4.getViewport().getView() == solverConfigTablePanel) {
                         SwingUtilities.invokeLater(new Runnable() {
 
                             @Override
                             public void run() {
                                 solverConfigTablePanel.update();
                             }
                         });
 
                     }
                 }
             }
         });
     }//GEN-LAST:event_btnUndoSolverConfigurationsActionPerformed
 
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
 
     private void btnSaveInstancesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveInstancesActionPerformed
         Tasks.startTask("saveExperimentHasInstances", new Class[]{Tasks.class}, new Object[]{null}, expController, this);
     }//GEN-LAST:event_btnSaveInstancesActionPerformed
 
     private void btnSelectAllInstancesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectAllInstancesActionPerformed
         for (int i = 0; i < tableInstances.getRowCount(); i++) {
             tableInstances.setValueAt(true, i, tableInstances.convertColumnIndexToView(InstanceTableModel.COL_SELECTED));
         }
     }//GEN-LAST:event_btnSelectAllInstancesActionPerformed
 
     private void btnDeselectAllInstancesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeselectAllInstancesActionPerformed
         for (int i = 0; i < tableInstances.getRowCount(); i++) {
             tableInstances.setValueAt(false, i, tableInstances.convertColumnIndexToView(InstanceTableModel.COL_SELECTED));
         }
     }//GEN-LAST:event_btnDeselectAllInstancesActionPerformed
 
     private void btnInvertSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInvertSelectionActionPerformed
         for (int i = 0; i < tableInstances.getRowCount(); i++) {
             tableInstances.setValueAt(!((Boolean) tableInstances.getValueAt(i, tableInstances.convertColumnIndexToView(InstanceTableModel.COL_SELECTED))), i, tableInstances.convertColumnIndexToView(InstanceTableModel.COL_SELECTED));
         }
     }//GEN-LAST:event_btnInvertSelectionActionPerformed
 
     private void btnGenerateJobsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateJobsActionPerformed
         if (hasUnsavedChanges()) {
             javax.swing.JOptionPane.showMessageDialog(null, "Please save all unsaved data first or reload the experiment before generating jobs.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
             return;
         }
         try {
             HashMap<String, Integer> limits = expController.getMaxLimits();
             EDACCExperimentModeGenerateJobs dialog = new EDACCExperimentModeGenerateJobs(EDACCApp.getApplication().getMainFrame(), true, limits.get("cpuTimeLimit"), limits.get("memoryLimit"), limits.get("wallClockTimeLimit"), limits.get("stackSizeLimit"), limits.get("outputSizeLimitFirst"), limits.get("outputSizeLimitLast"));
             dialog.setLocationRelativeTo(EDACCApp.getApplication().getMainFrame());
             dialog.setVisible(true);
             if (!dialog.isCancelled()) {
                 Tasks.startTask("generateJobs", new Class[]{edacc.model.Tasks.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class
                         },
                         new Object[]{
                             null, dialog.getCpuTimeLimit(), dialog.getMemoryLimit(), dialog.getWallClockTimeLimit(), dialog.getStackSizeLimit(), dialog.getOutputSizeLimitFirst(), dialog.getOutputSizeLimitLast(), dialog.getMaxSeed()
                         }, expController,
                         this);
             } else {
                 javax.swing.JOptionPane.showMessageDialog(null, "No jobs have been generated.", "Cancelled", javax.swing.JOptionPane.ERROR_MESSAGE);
             }
         } catch (final Exception ex) {
             SwingUtilities.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                 }
             });
         }
     }//GEN-LAST:event_btnGenerateJobsActionPerformed
 
     private void btnFilterInstancesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterInstancesActionPerformed
         EDACCApp.getApplication().show(instanceFilter);
         insTableModel.fireTableDataChanged();
         if (instanceFilter.hasFiltersApplied()) {
             setFilterStatus("This list of instances has filters applied to it. Use the filter button below to modify.");
         } else {
             setFilterStatus("");
         }
     }//GEN-LAST:event_btnFilterInstancesActionPerformed
 
     private void btnRefreshJobsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshJobsActionPerformed
         resultBrowserETA = null;
         Tasks.startTask("loadJobs", expController, this, false);
     }//GEN-LAST:event_btnRefreshJobsActionPerformed
 
     private void btnBrowserColumnSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowserColumnSelectionActionPerformed
         List<SortKey> sortKeys = (List<SortKey>) tableJobs.getRowSorter().getSortKeys();
         List<String> columnNames = new ArrayList<String>();
         for (SortKey sk : sortKeys) {
             columnNames.add(tableJobs.getColumnName(tableJobs.convertColumnIndexToView(sk.getColumn())));
         }
         EDACCResultsBrowserColumnSelection dialog = new EDACCResultsBrowserColumnSelection(EDACCApp.getApplication().getMainFrame(), true, jobsColumnSelector, jobsTableModel);
         dialog.setLocationRelativeTo(EDACCApp.getApplication().getMainFrame());
         dialog.setVisible(true);
         List<SortKey> newSortKeys = new ArrayList<SortKey>();
         for (int k = 0; k < columnNames.size(); k++) {
             String col = columnNames.get(k);
             for (int i = 0; i < tableJobs.getColumnCount(); i++) {
                 if (tableJobs.getColumnName(i).equals(col)) {
                     newSortKeys.add(new SortKey(tableJobs.convertColumnIndexToModel(i), sortKeys.get(k).getSortOrder()));
                 }
             }
         }
         tableJobs.getRowSorter().setSortKeys(newSortKeys);
         Util.updateTableColumnWidth(tableJobs);
     }//GEN-LAST:event_btnBrowserColumnSelectionActionPerformed
 
     private void btnFilterJobsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterJobsActionPerformed
         EDACCApp.getApplication().show(resultBrowserRowFilter);
         jobsTableModel.fireTableDataChanged();
         updateJobsFilterStatus();
     }//GEN-LAST:event_btnFilterJobsActionPerformed
 
     private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
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
     }//GEN-LAST:event_btnExportActionPerformed
 
     private void btnUndoInstancesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUndoInstancesActionPerformed
         insTableModel.undo();
         this.setTitles();
     }//GEN-LAST:event_btnUndoInstancesActionPerformed
 
     private void btnDiscardExperimentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiscardExperimentActionPerformed
         boolean unload = !hasUnsavedChanges() || (JOptionPane.showConfirmDialog(this,
                 "Discarding an experiment will make you lose all unsaved changes of the current experiment. Continue discarding the experiment?",
                 "Warning!",
                 JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION);
         if (unload) {
             expController.unloadExperiment();
             tableExperiments.requestFocusInWindow();
         }
     }//GEN-LAST:event_btnDiscardExperimentActionPerformed
 
     private void btnSelectQueueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectQueueActionPerformed
         try {
             JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
             EDACCManageGridQueuesDialog manageGridQueues = new EDACCManageGridQueuesDialog(mainFrame, true, expController);
             manageGridQueues.setLocationRelativeTo(mainFrame);
             manageGridQueues.setVisible(true);
         } catch (NoConnectionToDBException ex) {
             JOptionPane.showMessageDialog(this, "You have to establish a connection to the database first!", "Error!", JOptionPane.ERROR_MESSAGE);
         } catch (SQLException ex) {
         }
     }//GEN-LAST:event_btnSelectQueueActionPerformed
 
     private void btnEditExperimentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditExperimentActionPerformed
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
     }//GEN-LAST:event_btnEditExperimentActionPerformed
 
     private void btnSelectedInstancesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectedInstancesActionPerformed
         LinkedList<SortKey> sortKeys = new LinkedList<SortKey>();
         jTreeInstanceClass.setSelectionInterval(0, jTreeInstanceClass.getRowCount() - 1);
         sortKeys.add(new SortKey(InstanceTableModel.COL_SELECTED, SortOrder.DESCENDING));
         tableInstances.getRowSorter().setSortKeys(sortKeys);
     }//GEN-LAST:event_btnSelectedInstancesActionPerformed
 
     private void btnRandomSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRandomSelectionActionPerformed
         JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
         EDACCExperimentModeRandomInstanceSelection random = new EDACCExperimentModeRandomInstanceSelection(mainFrame, true, this);
         random.setLocationRelativeTo(mainFrame);
         random.setVisible(true);
     }//GEN-LAST:event_btnRandomSelectionActionPerformed
 
     private void btnSetPriorityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetPriorityActionPerformed
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
     }//GEN-LAST:event_btnSetPriorityActionPerformed
 
     /**
      * Stops the jobs timer.
      */
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
         manageExperimentPane.setEnabledAt(TAB_SOLVERS, true);
         manageExperimentPane.setEnabledAt(TAB_INSTANCES, true);
         manageExperimentPane.setEnabledAt(TAB_GENERATEJOBS, true);
         manageExperimentPane.setEnabledAt(TAB_JOBBROWSER, true);
         manageExperimentPane.setEnabledAt(TAB_ANALYSIS, true);
 
         setTitles();
         btnDiscardExperiment.setEnabled(true);
         btnImport.setEnabled(true);
     }
 
     /**
      * Method to be call after an experiment ist unloaded.
      */
     public void afterExperimentUnloaded() {
         manageExperimentPane.setSelectedIndex(0);
         manageExperimentPane.setEnabledAt(TAB_SOLVERS, false);
         manageExperimentPane.setEnabledAt(TAB_INSTANCES, false);
         manageExperimentPane.setEnabledAt(TAB_GENERATEJOBS, false);
         manageExperimentPane.setEnabledAt(TAB_JOBBROWSER, false);
         manageExperimentPane.setEnabledAt(TAB_ANALYSIS, false);
         setTitles();
         btnDiscardExperiment.setEnabled(false);
     }
 
     private void createDatabaseErrorMessage(SQLException e) {
         javax.swing.JOptionPane.showMessageDialog(EDACCApp.getApplication().getMainFrame(), "There was an error while communicating with the database: " + e, "Connection error", javax.swing.JOptionPane.ERROR_MESSAGE);
     }
 
     /**
      * Returns the instance table.
      * @return the instance table
      */
     public JTable getTableInstances() {
         return tableInstances;
     }
 
     /**
      * Updates the runtime estimation in the job browser tab.
      */
     public void updateRuntimeEstimation() {
         if (jobsTableModel.getJobs() == null) {
             lblETA.setText("");
             return;
         }
         int jobsCount = jobsTableModel.getJobsCount();
         int jobsSuccessful = jobsTableModel.getJobsCount(StatusCode.SUCCESSFUL);
         int jobsWaiting = jobsTableModel.getJobsCount(StatusCode.NOT_STARTED);
         int jobsRunning = jobsTableModel.getJobsCount(StatusCode.RUNNING);
 
         int jobsCrashed = jobsTableModel.getJobsCount(StatusCode.LAUNCHERCRASH)
                 + jobsTableModel.getJobsCount(StatusCode.SOLVERCRASH)
                 + jobsTableModel.getJobsCount(StatusCode.VERIFIERCRASH)
                 + jobsTableModel.getJobsCount(StatusCode.WATCHERCRASH);
 
         int jobsNotSuccessful = jobsCount - jobsSuccessful - jobsWaiting - jobsRunning;
         double percentage = (double) (jobsSuccessful + jobsNotSuccessful) / jobsCount;
         percentage = Math.round(percentage * 100 * 100) / 100.;
 
         int count = 0;
         double avgTime = 0.;
         int curRunningTime = 0;
         for (ExperimentResult er : jobsTableModel.getJobs()) {
             if (er.getStatus().getStatusCode() >= 1) {
                 count++;
                 avgTime += er.getResultTime();
             } else if (er.getStatus().equals(StatusCode.RUNNING) && er.getRunningTime() > 0) {
                 avgTime += er.getCPUTimeLimit(); // worst case
                 count++;
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
         String tooltip = "<html>Crashed: " + jobsCrashed + "<br/>Waiting: " + jobsWaiting + "</html>";
         if (ETA != null) {
             text += " ETA: " + ETA;
         }
         lblETA.setText(text);
         lblETA.setToolTipText(tooltip);
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnBrowserColumnSelection;
     private javax.swing.JButton btnChangeView;
     private javax.swing.JButton btnChooseSolverConfigs;
     private javax.swing.JButton btnChooseSolvers;
     private javax.swing.JButton btnComputeResultProperties;
     private javax.swing.JButton btnCreateExperiment;
     private javax.swing.JButton btnDeselectAllInstances;
     private javax.swing.JButton btnDeselectAllSolverConfigs;
     private javax.swing.JButton btnDeselectAllSolvers;
     private javax.swing.JButton btnDiscardExperiment;
     private javax.swing.JButton btnEditExperiment;
     private javax.swing.JButton btnExport;
     private javax.swing.JButton btnFilterInstances;
     private javax.swing.JButton btnFilterJobs;
     private javax.swing.JButton btnGenerateJobs;
     private javax.swing.JButton btnGeneratePackage;
     private javax.swing.JButton btnImport;
     private javax.swing.JButton btnImportSolverConfigs;
     private javax.swing.JButton btnInstanceColumnSelection;
     private javax.swing.JButton btnInvertSelection;
     private javax.swing.JButton btnInvertSolverConfigSelection;
     private javax.swing.JButton btnLoadExperiment;
     private javax.swing.JButton btnRandomSelection;
     private javax.swing.JButton btnRefreshJobs;
     private javax.swing.JButton btnRemoveExperiment;
     private javax.swing.JButton btnReverseSolverSelection;
     private javax.swing.JButton btnSaveInstances;
     private javax.swing.JButton btnSaveSolverConfigurations;
     private javax.swing.JButton btnSelectAllInstances;
     private javax.swing.JButton btnSelectAllSolverConfigs;
     private javax.swing.JButton btnSelectAllSolvers;
     private javax.swing.JButton btnSelectQueue;
     private javax.swing.JButton btnSelectedInstances;
     private javax.swing.JButton btnSetNumRuns;
     private javax.swing.JButton btnSetPriority;
     private javax.swing.JButton btnUndoInstances;
     private javax.swing.JButton btnUndoSolverConfigurations;
     private javax.swing.JCheckBox chkJobsTimer;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JPanel jPanel8;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JScrollPane jScrollPane5;
     private javax.swing.JScrollPane jScrollPane6;
     private javax.swing.JScrollPane jScrollPane7;
     private javax.swing.JScrollPane jScrollPane8;
     private javax.swing.JPopupMenu.Separator jSeparator1;
     private javax.swing.JPopupMenu.Separator jSeparator2;
     private javax.swing.JSplitPane jSplitPane2;
     private javax.swing.JTree jTreeInstanceClass;
     private javax.swing.JLabel lblETA;
     private javax.swing.JLabel lblFilterStatus;
     private javax.swing.JLabel lblJobsFilterStatus;
     private javax.swing.JTabbedPane manageExperimentPane;
     private javax.swing.JMenuItem menuKillHard;
     private javax.swing.JMenuItem menuKillSoft;
     private javax.swing.JMenuItem menuRemoveDeadClients;
     private javax.swing.JMenuItem menuSendMessage;
     private javax.swing.JScrollPane panelAnalysis;
     private javax.swing.JPanel panelChooseInstances;
     private javax.swing.JPanel panelChooseSolver;
     private javax.swing.JPanel panelClientBrowser;
     private javax.swing.JPanel panelExperimentParams;
     private javax.swing.JPanel panelJobBrowser;
     private javax.swing.JPanel panelManageExperiment;
     private javax.swing.JPopupMenu popupTblClients;
     private javax.swing.JScrollPane scrollPaneExperimentsTable;
     private javax.swing.JSplitPane splitPaneSolver;
     private javax.swing.JSplitPane splitPaneSolverSolverConfigs;
     private javax.swing.JTable tableExperiments;
     public javax.swing.JTable tableInstances;
     public javax.swing.JTable tableJobs;
     private javax.swing.JTable tableSolvers;
     private javax.swing.JTable tblClients;
     public javax.swing.JTable tblGenerateJobs;
     private javax.swing.JTable tblSolverConfigs;
     private javax.swing.JTextField txtJobsTimer;
     // End of variables declaration//GEN-END:variables
 
     @Override
     public void onTaskSuccessful(String methodName, Object result) {
         if ("generateJobs".equals(methodName)) {
             int added_experiments = (Integer) result;
             javax.swing.JOptionPane.showMessageDialog(this, "Added " + added_experiments + " new " + (added_experiments == 1 ? "job" : "jobs") + ".", "Jobs added", javax.swing.JOptionPane.INFORMATION_MESSAGE);
             setGenerateJobsTitle();
         } else if ("loadExperiment".equals(methodName)) {
             setTitles();
             setGenerateJobsTitle();
         } else if ("saveExperimentHasInstances".equals(methodName)) {
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
             javax.swing.JOptionPane.showMessageDialog(this, e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
         } else if (e instanceof SQLException) {
             createDatabaseErrorMessage((SQLException) e);
         } else if (e instanceof IOException && (methodName.equals("exportCSV") || methodName.equals("exportTeX"))) {
             javax.swing.JOptionPane.showMessageDialog(this, "I/O Exception during export:\n\n" + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
         } else {
             javax.swing.JOptionPane.showMessageDialog(this, e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
         }
     }
 
     /**
      * Updates the jobs filter status in the job browser tab. Also sets a warning message if no grid queue is assigned.
      */
     public void updateJobsFilterStatus() {
         lblJobsFilterStatus.setIcon(new ImageIcon("warning-icon.png"));
         lblJobsFilterStatus.setForeground(Color.red);
         String status = "";
         if (!expController.hasGridQueuesAssigned()) {
             status += "Warning: no grid queue assigned! ";
         }
         if (resultBrowserRowFilter.hasFiltersApplied()) {
             status += "This list of jobs has filters applied to it. Use the filter button below to modify. Showing " + tableJobs.getRowCount() + " Jobs.";
         }
         lblJobsFilterStatus.setText(status);
     }
 
     /**
      * Returns the jobs table of the job browser tab.
      * @return the jobs table
      */
     public JTable getTableJobs() {
         return tableJobs;
     }
 
     /**
      * Updates the filter status of the instances tab.
      * @param status 
      */
     public void setFilterStatus(String status) {
         lblFilterStatus.setForeground(Color.red);
         lblFilterStatus.setText(status);
 
     }
 
     /**
      * Randomly selects <code>count</code> instances in the instance table.
      * @param count the number of instances to be selected
      * @throws Exception if their are less then instances than count in the instances table an exception is thrown.
      */
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
 
     /**
      * Deinitializes the experiment mode.
      */
     public void deinitialize() {
         reinitializeGUI();
     }
 
     private void resetJobsColumnVisibility() {
         Runnable runnable = new Runnable() {
 
             @Override
             public void run() {
                 jobsColumnSelector.setColumnVisiblity(jobsTableModel.getDefaultVisibility());
             }  
         };
         if (SwingUtilities.isEventDispatchThread()) {
             runnable.run();
         } else {
             SwingUtilities.invokeLater(runnable);
         }
     }
 
     private void resetInstanceColumnVisibility() {
         Runnable runnable = new Runnable() {
 
             @Override
             public void run() {
                 insTableModel.fireTableStructureChanged();
                 boolean[] tmp = insTableModel.getDefaultVisibility();
                 boolean isCompetition;
                 try {
                     isCompetition = DatabaseConnector.getInstance().isCompetitionDB();
                 } catch (Exception ex) {
                     isCompetition = false;
                 }
                 // if it is no competition db, then remove the competition columns
                 if (!isCompetition) {
                     tableInstances.removeColumn(tableInstances.getColumnModel().getColumn(InstanceTableModel.COL_BENCHTYPE));
                 }
                 boolean[] visibility = new boolean[tableInstances.getColumnCount()];
 
                 int k = 0;
                 for (int i = 0; i < tmp.length; i++) {
                     if (isCompetition || i != InstanceTableModel.COL_BENCHTYPE) {
                         visibility[k++] = tmp[i];
                     }
                 }
                 instanceColumnSelector = new TableColumnSelector(tableInstances);
                 instanceColumnSelector.setColumnVisiblity(visibility);
             }
         };
         if (SwingUtilities.isEventDispatchThread()) {
             runnable.run();
         } else {
             SwingUtilities.invokeLater(runnable);
         }
     }
 }
