 package edacc.experiment;
 
 import edacc.satinstances.ConvertException;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import javax.swing.table.AbstractTableModel;
 import edacc.model.ExperimentResult;
 import edacc.model.ExperimentResultHasProperty;
 import edacc.model.ExperimentResultStatus;
 import edacc.model.GridQueue;
 import edacc.model.GridQueueDAO;
 import edacc.model.Instance;
 import edacc.model.InstanceDAO;
 import edacc.model.ParameterInstance;
 import edacc.model.ParameterInstanceDAO;
 import edacc.model.Solver;
 import edacc.model.SolverConfiguration;
 import edacc.model.SolverConfigurationDAO;
 import edacc.model.SolverDAO;
 import edacc.model.Property;
 import edacc.model.PropertyDAO;
 import java.util.ArrayList;
 import java.util.Formatter;
 import java.util.HashMap;
 import java.util.HashSet;
 import javax.swing.SwingUtilities;
 
 /**
  * In this class rowIndexes are always the visible rowIndexes and columnIndexes
  * are always the visible column indexes.
  * @author daniel, simon
  */
 public class ExperimentResultsBrowserTableModel extends AbstractTableModel {
 
     // constants for the columns
     public static final int COL_ID = 0;
     public static final int COL_PRIORITY = 1;
     public static final int COL_COMPUTEQUEUE = 2;
     public static final int COL_SOLVER = 3;
     public static final int COL_PARAMETERS = 4;
     public static final int COL_INSTANCE = 5;
     public static final int COL_RUN = 6;
     public static final int COL_TIME = 7;
     public static final int COL_SEED = 8;
     public static final int COL_STATUS = 9;
     public static final int COL_RESULTCODE = 10;
     public static final int COL_SOLVER_OUTPUT = 11;
     public static final int COL_LAUNCHER_OUTPUT = 12;
     public static final int COL_WATCHER_OUTPUT = 13;
     public static final int COL_VERIFIER_OUTPUT = 14;
     public static final int COL_PROPERTY = 15;
     private ArrayList<ExperimentResult> jobs;
     // the constant columns
     private String[] CONST_COLUMNS = {"ID", "Priority", "Compute Queue", "Solver", "Parameters", "Instance", "Run", "Time", "Seed", "Status", "Result Code", "Solver Output", "Launcher Output", "Watcher Output", "Verifier Output"};
     // the visibility of each column
     private boolean[] CONST_VISIBLE = {false, false, true, true, true, true, true, true, true, true, true, false, false, false, false};
     private String[] columns;
     private ArrayList<Property> solverProperties;
     private boolean[] visible;
     private HashMap<Integer, ArrayList<ParameterInstance>> parameterInstances;
     private HashMap<Integer, GridQueue> gridQueues;
     private HashMap<Integer, String> parameters;
 
     public ExperimentResultsBrowserTableModel() {
         columns = new String[CONST_COLUMNS.length];
         visible = new boolean[columns.length];
         for (int i = 0; i < columns.length; i++) {
             columns[i] = CONST_COLUMNS[i];
             visible[i] = CONST_VISIBLE[i];
         }
     }
 
     public void updateSolverProperties() {
         ArrayList<Property> tmp = new ArrayList<Property>();
         try {
             tmp.addAll(PropertyDAO.getAllResultProperties());
         } catch (Exception e) {
             if (edacc.ErrorLogger.DEBUG) {
                 e.printStackTrace();
             }
         }
         if (!tmp.equals(solverProperties)) {
             solverProperties = tmp;
 
             for (int i = solverProperties.size() - 1; i >= 0; i--) {
                 if (solverProperties.get(i).isMultiple()) {
                     solverProperties.remove(i);
                 }
             }
             columns = java.util.Arrays.copyOf(columns, CONST_COLUMNS.length + solverProperties.size());
             visible = java.util.Arrays.copyOf(visible, CONST_VISIBLE.length + solverProperties.size());
             int j = 0;
             for (int i = CONST_COLUMNS.length; i < columns.length; i++) {
                 columns[i] = solverProperties.get(j).getName();
                 visible[i] = true;
                 j++;
             }
             this.fireTableStructureChanged();
         }
 
     }
 
     /**
      * Returns the job id
      * @param row
      * @return the id of the job
      */
     public Integer getId(int row) {
         return jobs.get(row).getId();
     }
 
     /**
      * Returns the solver
      * @param row
      * @return null if there was an error
      */
     public Solver getSolver(int row) {
         try {
             SolverConfiguration sc = SolverConfigurationDAO.getSolverConfigurationById(jobs.get(row).getSolverConfigId());
             return SolverDAO.getById(sc.getSolver_id());
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Returns the instance
      * @param row
      * @return null if there was an error
      */
     public Instance getInstance(int row) {
         try {
             return InstanceDAO.getById(jobs.get(row).getInstanceId());
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Returns the run
      * @param row
      * @return the run
      */
     public Integer getRun(int row) {
         return jobs.get(row).getRun();
     }
 
     /**
      * Returns the seed
      * @param row
      * @return the seed
      */
     public Integer getSeed(int row) {
         return jobs.get(row).getSeed();
     }
 
     /**
      * Returns the status
      * @param row
      * @return the status
      */
     public ExperimentResultStatus getStatus(int row) {
         if (row < 0 || row >= getRowCount()) {
             return null;
         }
         return jobs.get(row).getStatus();
     }
 
     /**
      * Returns all parameter instances for that job
      * @param row
      * @return null, if there was an error
      */
     public ArrayList<ParameterInstance> getParameters(int row) {
         try {
             SolverConfiguration sc = SolverConfigurationDAO.getSolverConfigurationById(jobs.get(row).getSolverConfigId());
             ArrayList<ParameterInstance> params = parameterInstances.get(sc.getId());
             if (params == null) {
                 params = ParameterInstanceDAO.getBySolverConfigId(sc.getId());
                 parameterInstances.put(sc.getId(), params);
             }
             return params;
         } catch (Exception e) {
             return null;
         }
     }
 
     public ExperimentResult getExperimentResult(int row) {
         return jobs.get(row);
     }
 
     /**
      * Sets the jobs for that model
      * @param jobs
      * @throws SQLException
      */
     public void setJobs(final ArrayList<ExperimentResult> jobs) throws SQLException {
 
         Runnable updateTable = new Runnable() {
 
             @Override
             public void run() {
                 ExperimentResultsBrowserTableModel.this.jobs = jobs;
                 if (jobs != null) {
                     parameterInstances = new HashMap<Integer, ArrayList<ParameterInstance>>();
                     gridQueues = new HashMap<Integer, GridQueue>();
                     parameters = new HashMap<Integer, String>();
                     try {
                         ArrayList<GridQueue> queues = GridQueueDAO.getAll();
                         for (GridQueue q : queues) {
                             gridQueues.put(q.getId(), q);
                         }
                     } catch (Exception e) {
                     }
                 }
                 ExperimentResultsBrowserTableModel.this.fireTableDataChanged();
             }
         };
         if (SwingUtilities.isEventDispatchThread()) {
             // already in EDT
             updateTable.run();
         } else {
             // we have to run this in the EDT, otherwise sync exceptions
             try {
                 SwingUtilities.invokeAndWait(updateTable);
             } catch (Exception _) {
             }
         }
     }
 
     public int getIndexForColumn(int col) {
         for (int i = 0; i < visible.length; i++) {
             if (visible[i]) {
                 col--;
             }
             if (col == -1) {
                 return i;
             }
         }
         return 0;
     }
 
     /**
      * Returns an array of all column names, not only the visible ones.
      * @return array of string with all column names
      */
     public String[] getAllColumnNames() {
         return columns;
     }
 
     @Override
     public int getRowCount() {
         return jobs == null ? 0 : jobs.size();
     }
 
     @Override
     public int getColumnCount() {
         int res = 0;
         for (int i = 0; i < visible.length; i++) {
             if (visible[i]) {
                 res++;
             }
         }
         return res;
     }
 
     @Override
     public String getColumnName(int col) {
         return columns[getIndexForColumn(col)];
     }
 
     @Override
     public Class getColumnClass(int col) {
         return getRealColumnClass(getIndexForColumn(col));
     }
 
     public Class getRealColumnClass(int col) {
         if (getRowCount() == 0) {
             return String.class;
         } else {
             if (col >= COL_PROPERTY) {
                 int propertyIdx = col - COL_PROPERTY;
                 if (propertyIdx >= solverProperties.size()) {
                     return String.class;
                 }
                 if (solverProperties.get(propertyIdx).getPropertyValueType() == null) {
                     return String.class;
                 }
                 return solverProperties.get(propertyIdx).getPropertyValueType().getJavaType();
             } else {
                 return getRealValueAt(0, col).getClass();
             }
         }
     }
 
     /**
      * Sets the column visibility.
      * @param visible a boolean array - length must equal getAllCoulumnNames().length or this method does nothing.
      */
     public void setColumnVisibility(boolean[] visible, boolean updateTable) {
         if (columns.length != visible.length) {
             return;
         }
         this.visible = visible;
         if (updateTable) {
             this.fireTableStructureChanged();
         }
     }
 
     /**
      * Returns the visibility array
      * @return array of boolean, where entry i says whether column i is visible or not
      */
     public boolean[] getColumnVisibility() {
         return visible;
     }
 
     public Object getRealValueAt(int rowIndex, int columnIndex) {
         if (rowIndex < 0 || rowIndex >= getRowCount()) {
             return null;
         }
         ExperimentResult j = jobs.get(rowIndex);
         switch (columnIndex) {
             case COL_ID:
                 return j.getId();
             case COL_PRIORITY:
                 return j.getPriority();
             case COL_COMPUTEQUEUE:
                 GridQueue q = gridQueues.get(j.getComputeQueue());
                 return q == null ? "none" : q.getName();
             case COL_SOLVER:
                 Solver solver = getSolver(rowIndex);
                 return solver == null ? "" : solver.getName();
             case COL_PARAMETERS:
                 String params = parameters.get(j.getSolverConfigId());
                 if (params == null) {
                     params = Util.getParameterString(getParameters(rowIndex));
                     parameters.put(j.getSolverConfigId(), params);
                 }
                 return params;
             case COL_INSTANCE:
                 Instance instance = getInstance(rowIndex);
                 return instance == null ? "" : instance.getName();
             case COL_RUN:
                 return j.getRun();
             case COL_TIME:
                 return j.getResultTime();
             case COL_SEED:
                 return j.getSeed();
             case COL_STATUS:
                 String status = j.getStatus().toString();
                 if (j.getStatus() == ExperimentResultStatus.RUNNING) {
                     int hours = j.getRunningTime() / 3600;
                     int minutes = (j.getRunningTime() / 60) % 60;
                     int seconds = j.getRunningTime() % 60;
                     status += " (" + new Formatter().format("%02d:%02d:%02d", hours, minutes, seconds) + ")";
                 }
                 return status;
             case COL_RESULTCODE:
                 return j.getResultCode().toString();
             case COL_SOLVER_OUTPUT:
                 return j.getSolverOutputFilename();
             case COL_LAUNCHER_OUTPUT:
                 return j.getLauncherOutputFilename();
             case COL_WATCHER_OUTPUT:
                 return j.getWatcherOutputFilename();
             case COL_VERIFIER_OUTPUT:
                 return j.getVerifierOutputFilename();
             default:
                 int propertyIdx = columnIndex - COL_PROPERTY;
                 if (solverProperties.size() <= propertyIdx) {
                     return null;
                 }
                 ExperimentResultHasProperty erp = j.getPropertyValues().get(solverProperties.get(propertyIdx).getId());
                 if (erp != null && !erp.getValue().isEmpty()) {
                     try {
                         if (solverProperties.get(propertyIdx).getPropertyValueType() == null) {
                             return erp.getValue().get(0);
                         }
                         return solverProperties.get(propertyIdx).getPropertyValueType().getJavaTypeRepresentation(erp.getValue().get(0));
                     } catch (ConvertException ex) {
                         return null;
                     }
                 } else {
                     return null;
                 }
         }
     }
 
     @Override
     public Object getValueAt(int rowIndex, int columnIndex) {
         if (rowIndex < 0 || rowIndex >= getRowCount()) {
             return null;
         }
         ExperimentResult j = jobs.get(rowIndex);
 
         if (columnIndex != -1) {
             columnIndex = getIndexForColumn(columnIndex);
         }
         return getRealValueAt(rowIndex, columnIndex);
     }
 
     /**
      * Returns all disjunct instance names which are currently in that model
      * @return arraylist with the instance names
      */
     public ArrayList<String> getInstances() {
         ArrayList<String> res = new ArrayList<String>();
         if (getRowCount() == 0) {
             return res;
         }
         int experimentId = jobs.get(0).getExperimentId();
         try {
             LinkedList<Instance> instances = InstanceDAO.getAllByExperimentId(experimentId);
             HashSet<String> tmp = new HashSet<String>();
             for (Instance i : instances) {
                 if (!tmp.contains(i.getName())) {
                     tmp.add(i.getName());
                 }
             }
             res.addAll(tmp);
             return res;
         } catch (Exception ex) {
             return res;
         }
     }
 
     /**
      * Returns all disjunct status codes which are currently in that model
      */
     public ArrayList<ExperimentResultStatus> getStatusEnums() {
         ArrayList<ExperimentResultStatus> res = new ArrayList<ExperimentResultStatus>();
         HashSet<ExperimentResultStatus> tmp = new HashSet<ExperimentResultStatus>();
         for (int i = 0; i
                 < getRowCount(); i++) {
             if (!tmp.contains(getStatus(i))) {
                 tmp.add(getStatus(i));
             }
         }
         res.addAll(tmp);
         return res;
     }
 
     /**
      * Returns all disjunct solver names which are currently in that model
      */
     public ArrayList<String> getSolvers() {
         ArrayList<String> res = new ArrayList<String>();
         HashSet<String> tmp = new HashSet<String>();
         for (int i = 0; i
                 < getRowCount(); i++) {
             if (!tmp.contains(getSolver(i).getName())) {
                 tmp.add(getSolver(i).getName());
             }
         }
         res.addAll(tmp);
         return res;
     }
 
     public ArrayList<ExperimentResult> getJobs() {
         return jobs;
     }
 
     public int getJobsCount() {
         return jobs == null ? 0 : jobs.size();
     }
 
     public int getJobsCount(ExperimentResultStatus status) {
         if (jobs == null) {
             return 0;
         }
         int res = 0;
         for (ExperimentResult j : jobs) {
             if (j.getStatus().equals(status)) {
                 res++;
             }
         }
         return res;
     }
 
     public void resetColumnVisibility() {
         System.arraycopy(CONST_VISIBLE, 0, visible, 0, CONST_VISIBLE.length);
         for (int i = CONST_VISIBLE.length; i < visible.length; i++) {
             visible[i] = true;
         }
     }
 }
