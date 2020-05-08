 package uk.ac.ebi.fgpt.conan.dao;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.util.Assert;
 import uk.ac.ebi.fgpt.conan.core.pipeline.DefaultConanPipeline;
 import uk.ac.ebi.fgpt.conan.core.process.DefaultProcessRun;
 import uk.ac.ebi.fgpt.conan.core.task.AbstractConanTask;
 import uk.ac.ebi.fgpt.conan.core.task.ConanTaskListener;
 import uk.ac.ebi.fgpt.conan.core.task.DatabaseRecoveredConanTask;
 import uk.ac.ebi.fgpt.conan.model.*;
 
 import javax.sql.DataSource;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * An implementation of {@link uk.ac.ebi.fgpt.conan.dao.ConanTaskDAO} that stores and retrieves task information from a
  * backing database.
  *
  * @author Tony Burdett
  * @date 19-Oct-2010
  */
 public class DatabaseConanTaskDAO implements ConanTaskDAO {
     /**
      * SQL queries required for Conan oracle DB
      */
     public static final String SEQUENCE_SELECT =
             "select SEQ_CONAN.NEXTVAL from dual";
 
     public static final String TASK_SELECT =
             "select ID, NAME, START_DATE, END_DATE, USER_ID, PIPELINE_NAME, PRIORITY, FIRST_PROCESS_INDEX, STATE, STATUS_MESSAGE, CURRENT_EXECUTED_INDEX, CREATION_DATE " +
                     "from CONAN_TASKS";
     public static final String TASK_SELECT_BY_ID = TASK_SELECT + " " +
             "where ID = ?";
     public static final String TASK_SELECT_BY_DATE = TASK_SELECT + " " +
             "where ROWNUM <= ? and ROWNUM >= ? order by START_DATE";
     public static final String TASK_SELECT_BY_PARAM = TASK_SELECT + " " +
             "where ROWNUM <= ? and ROWNUM >= ? order by ?";
     public static final String TASK_SELECT_PENDING = TASK_SELECT + " " +
             "where STATE = 'CREATED' or STATE = 'SUBMITTED' or STATE = 'RECOVERED' or STATE = 'PAUSED' or STATE = 'FAILED'";
     public static final String TASK_SELECT_RUNNING = TASK_SELECT + " " +
             "where STATE = 'RUNNING'";
     public static final String TASK_SELECT_COMPLETED = TASK_SELECT + " " +
             "where (STATE = 'COMPLETED' or STATE = 'ABORTED')";
     public static final String TASK_SELECT_COMPLETED_PAGED =
             "select ID, NAME, START_DATE, END_DATE, USER_ID, PIPELINE_NAME, PRIORITY, FIRST_PROCESS_INDEX, STATE, STATUS_MESSAGE, CURRENT_EXECUTED_INDEX, CREATION_DATE " +
                     "from (" + TASK_SELECT + " where (STATE = 'COMPLETED' or STATE = 'ABORTED') order by END_DATE desc) " +
                     "where ROWNUM <= ? and ROWNUM >= ?";
     public static final String TASK_SEARCH_NAME = TASK_SELECT_COMPLETED + " " +
             "and NAME like ?";
     public static final String TASK_SEARCH_NAME_USER = TASK_SEARCH_NAME + " " +
            "and USER_ID = ?";
     public static final String TASK_SEARCH_NAME_FROM_DATE = TASK_SEARCH_NAME + " " +
             "and END_DATE > ?";
     public static final String TASK_SEARCH_NAME_TO_DATE = TASK_SEARCH_NAME + " " +
             "and END_DATE < ?";
     public static final String TASK_SEARCH_NAME_FROM_TO_DATE = TASK_SEARCH_NAME_FROM_DATE + " " +
             "and END_DATE < ?";
     public static final String TASK_SEARCH_NAME_USER_FROM_DATE = TASK_SEARCH_NAME_USER + " " +
             "and END_DATE > ?";
     public static final String TASK_SEARCH_NAME_USER_TO_DATE = TASK_SEARCH_NAME_USER + " " +
             "and END_DATE < ?";
     public static final String TASK_SEARCH_NAME_USER_FROM_TO_DATE = TASK_SEARCH_NAME_USER_FROM_DATE + " " +
             "and END_DATE < ?";
     public static final String TASK_INSERT =
             "insert into CONAN_TASKS (" +
                     "ID, NAME, START_DATE, END_DATE, USER_ID, PIPELINE_NAME, PRIORITY, FIRST_PROCESS_INDEX, STATE, STATUS_MESSAGE, CURRENT_EXECUTED_INDEX, CREATION_DATE) " +
                     "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
     public static final String TASK_UPDATE =
             "update CONAN_TASKS " +
                     "set NAME = ?, START_DATE = ?, END_DATE = ?, USER_ID = ?, PIPELINE_NAME = ?, PRIORITY = ?, FIRST_PROCESS_INDEX = ?, STATE = ?, STATUS_MESSAGE = ?, CURRENT_EXECUTED_INDEX = ? , CREATION_DATE = ? " +
                     "where ID = ?";
 
     public static final String PROCESS_SELECT =
             "select ID, NAME, START_DATE, END_DATE, USER_ID, EXIT_CODE, TASK_ID " +
                     "from CONAN_PROCESSES";
     public static final String PROCESS_SELECT_BY_TASK_ID = PROCESS_SELECT + " " +
             "where TASK_ID = ?";
     public static final String PROCESS_SELECT_BY_TASKS = PROCESS_SELECT + " " +
             "where TASK_ID in (:taskids)";
     public static final String PROCESS_SELECT_FOR_PENDING_TASKS =
             "select p.ID, p.NAME, p.START_DATE, p.END_DATE, p.USER_ID, p.EXIT_CODE, p.TASK_ID " +
                     "from CONAN_PROCESSES p, CONAN_TASKS t " +
                     "where p.TASK_ID = t.ID and (t.STATE = 'CREATED' or t.STATE = 'SUBMITTED' or t.STATE = 'RECOVERED' or t.STATE = 'PAUSED' or t.STATE = 'FAILED')";
     public static final String PROCESS_SELECT_FOR_RUNNING_TASKS =
             "select p.ID, p.NAME, p.START_DATE, p.END_DATE, p.USER_ID, p.EXIT_CODE, p.TASK_ID " +
                     "from CONAN_PROCESSES p, CONAN_TASKS t " +
                     "where p.TASK_ID = t.ID and t.STATE = 'RUNNING'";
     public static final String PROCESS_SELECT_FOR_COMPLETED_TASKS =
             "select p.ID, p.NAME, p.START_DATE, p.END_DATE, p.USER_ID, p.EXIT_CODE, p.TASK_ID " +
                     "from CONAN_PROCESSES p, CONAN_TASKS t " +
                     "where p.TASK_ID = t.ID and (t.STATE = 'COMPLETED' or t.STATE = 'ABORTED')";
     public static final String PROCESS_INSERT =
             "insert into CONAN_PROCESSES (" +
                     "ID, NAME, START_DATE, END_DATE, USER_ID, EXIT_CODE, TASK_ID) " +
                     "values (?, ?, ?, ?, ?, ?, ?)";
     public static final String PROCESS_UPDATE =
             "update CONAN_PROCESSES set NAME = ?, START_DATE = ?, END_DATE = ?, USER_ID = ?, EXIT_CODE = ?, TASK_ID = ? " +
                     "where ID = ?";
 
     public static final String PARAMETER_SELECT =
             "select ID, PARAMETER_NAME, PARAMETER_VALUE, TASK_ID " +
                     "from CONAN_PARAMETERS";
     public static final String PARAMETER_SELECT_BY_TASK_ID = PARAMETER_SELECT + " " +
             "where TASK_ID = ?";
     public static final String PARAMETER_SELECT_BY_TASKS = PARAMETER_SELECT + " " +
             "where TASK_ID in (:taskids)";
     public static final String PARAMETER_SELECT_FOR_PENDING_TASKS =
             "select p.ID, p.PARAMETER_NAME, p.PARAMETER_VALUE, p.TASK_ID " +
                     "from CONAN_PARAMETERS p, CONAN_TASKS t " +
                     "where p.TASK_ID = t.ID and (t.STATE = 'CREATED' or t.STATE = 'SUBMITTED' or t.STATE = 'RECOVERED' or t.STATE = 'PAUSED' or t.STATE = 'FAILED')";
     public static final String PARAMETER_SELECT_FOR_RUNNING_TASKS =
             "select p.ID, p.PARAMETER_NAME, p.PARAMETER_VALUE, p.TASK_ID " +
                     "from CONAN_PARAMETERS p, CONAN_TASKS t " +
                     "where p.TASK_ID = t.ID and t.STATE = 'RUNNING'";
     public static final String PARAMETER_SELECT_FOR_COMPLETED_TASKS =
             "select p.ID, p.PARAMETER_NAME, p.PARAMETER_VALUE, p.TASK_ID " +
                     "from CONAN_PARAMETERS p, CONAN_TASKS t " +
                     "where p.TASK_ID = t.ID and (t.STATE = 'COMPLETED' or t.STATE = 'ABORTED')";
     public static final String PARAMETER_INSERT =
             "insert into CONAN_PARAMETERS (" +
                     "PARAMETER_NAME, PARAMETER_VALUE, TASK_ID) " +
                     "values (?, ?, ?)";
     public static final String PARAMETER_DELETE =
             "delete from CONAN_PARAMETERS where TASK_ID = ?";
 
     private ConanPipelineDAO pipelineDAO;
     private ConanUserDAO userDAO;
 
     private JdbcTemplate jdbcTemplate;
 
     private Set<ConanTaskListener> conanTaskListeners;
 
     private Map<String, String> propertyToColumnMap;
     private int maxQueryParams = 500;
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     protected Logger getLog() {
         return log;
     }
 
     public void setDataSource(DataSource dataSource) {
         this.jdbcTemplate = new JdbcTemplate(dataSource);
         this.jdbcTemplate.setLazyInit(true);
     }
 
     public ConanPipelineDAO getPipelineDAO() {
         return pipelineDAO;
     }
 
     public void setPipelineDAO(ConanPipelineDAO pipelineDAO) {
         this.pipelineDAO = pipelineDAO;
     }
 
     public ConanUserDAO getUserDAO() {
         return userDAO;
     }
 
     public void setUserDAO(ConanUserDAO userDAO) {
         this.userDAO = userDAO;
     }
 
     public JdbcTemplate getJdbcTemplate() {
         return jdbcTemplate;
     }
 
     public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
         this.jdbcTemplate = jdbcTemplate;
     }
 
     public Set<ConanTaskListener> getConanTaskListeners() {
         return conanTaskListeners;
     }
 
     public void setConanTaskListeners(Set<ConanTaskListener> conanTaskListeners) {
         this.conanTaskListeners = conanTaskListeners;
     }
 
     public boolean supportsAutomaticIDAssignment() {
         return true;
     }
 
     public ConanTask<? extends ConanPipeline> getTask(String taskID) {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         ConanTask<? extends ConanPipeline> taskDB = getJdbcTemplate().queryForObject(TASK_SELECT_BY_ID,
                                                                                      new Object[]{taskID},
                                                                                      new ConanTaskMapper());
 
         //additional sets
         List<ConanTask<? extends ConanPipeline>> singleList =
                 new ArrayList<ConanTask<? extends ConanPipeline>>();
         singleList.add(taskDB);
         addConanTaskChildren(singleList);
 
         return taskDB;
     }
 
     public <P extends ConanPipeline> ConanTask<P> saveTask(ConanTask<P> conanTask) {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         if (conanTask.getSubmitter().getId() == null) {
             userDAO.saveUser(conanTask.getSubmitter());
         }
 
         int currentExecutedIndex = conanTask.getPipeline().getProcesses().indexOf(conanTask.getLastProcess());
         if (currentExecutedIndex == -1) {
             currentExecutedIndex = 0;
         }
         int firstExecutedIndex = conanTask.getPipeline().getProcesses().indexOf(conanTask.getFirstProcess());
         if (firstExecutedIndex == -1) {
             firstExecutedIndex = 0;
         }
 
         if (conanTask.getId() == null) {
             int taskID = getJdbcTemplate().queryForInt(SEQUENCE_SELECT);
             getJdbcTemplate().update(TASK_INSERT,
                                      taskID,
                                      conanTask.getName(),
                                      javaDateToSQLDate(conanTask.getStartDate()),
                                      javaDateToSQLDate(conanTask.getCompletionDate()),
                                      conanTask.getSubmitter().getId(),
                                      conanTask.getPipeline().getName(),
                                      conanTask.getPriority().toString(),
                                      firstExecutedIndex,
                                      conanTask.getCurrentState().toString(),
                                      conanTask.getStatusMessage(),
                                      currentExecutedIndex,
                                      javaDateToSQLDate(conanTask.getCreationDate()));
             conanTask.setId(Integer.toString(taskID));
             //save parameters
             Map<ConanParameter, String> params = conanTask.getParameterValues();
             for (ConanParameter conanParameter : params.keySet()) {
                 getJdbcTemplate().update(PARAMETER_INSERT,
                                          conanParameter.getName(),
                                          params.get(conanParameter),
                                          taskID);
             }
 
         }
         else {
             getJdbcTemplate().update(TASK_UPDATE,
                                      conanTask.getName(),
                                      javaDateToSQLDate(conanTask.getStartDate()),
                                      javaDateToSQLDate(conanTask.getCompletionDate()),
                                      conanTask.getSubmitter().getId(),
                                      conanTask.getPipeline().getName(),
                                      conanTask.getPriority().toString(),
                                      firstExecutedIndex,
                                      conanTask.getCurrentState().toString(),
                                      conanTask.getStatusMessage(),
                                      currentExecutedIndex,
                                      javaDateToSQLDate(conanTask.getCreationDate()),
                                      conanTask.getId());
             //delete and save parameters
             getJdbcTemplate().update(PARAMETER_DELETE,
                                      conanTask.getId());
             Map<ConanParameter, String> params = conanTask.getParameterValues();
             for (ConanParameter conanParameter : params.keySet()) {
                 getJdbcTemplate().update(PARAMETER_INSERT,
                                          conanParameter.getName(), params.get(conanParameter), conanTask.getId());
             }
 
         }
         return conanTask;
     }
 
     public <P extends ConanPipeline> ConanTask<P> updateTask(ConanTask<P> conanTask)
             throws IllegalArgumentException {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         // current position
         int currentExecutedIndex;
         if (conanTask.getCurrentProcess() != null) {
             // executing, get index of current process
             currentExecutedIndex = conanTask.getPipeline().getProcesses().indexOf(conanTask.getCurrentProcess());
         }
         else {
             if (conanTask.getNextProcess() != null) {
                 // not running - not finished yet?
                 currentExecutedIndex = conanTask.getPipeline().getProcesses().indexOf(conanTask.getNextProcess());
             }
             else {
                 // already finished, so current executed index should be index of last process + 1
                 currentExecutedIndex = conanTask.getPipeline().getProcesses().size();
             }
         }
         int firstExecutedIndex = conanTask.getPipeline().getProcesses().indexOf(conanTask.getFirstProcess());
         if (firstExecutedIndex == -1) {
             firstExecutedIndex = 0;
         }
 
         getJdbcTemplate().update(TASK_UPDATE,
                                  conanTask.getName(),
                                  javaDateToSQLDate(conanTask.getStartDate()),
                                  javaDateToSQLDate(conanTask.getCompletionDate()),
                                  conanTask.getSubmitter().getId(),
                                  conanTask.getPipeline().getName(),
                                  conanTask.getPriority().toString(),
                                  firstExecutedIndex,
                                  conanTask.getCurrentState().toString(),
                                  conanTask.getStatusMessage(),
                                  currentExecutedIndex,
                                  javaDateToSQLDate(conanTask.getCreationDate()),
                                  conanTask.getId());
 
         return conanTask;
     }
 
     public <P extends ConanPipeline> ConanTask<P> saveProcessRun(
             String conanTaskID, ConanProcessRun conanProcessRun)
             throws IllegalArgumentException {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
 
         ConanTask taskDB = getTask(conanTaskID);
         if (conanProcessRun.getUser().getId() == null) {
             userDAO.saveUser(conanProcessRun.getUser());
         }
 
         if (conanProcessRun.getId() == null) {
             int processRunID = getJdbcTemplate().queryForInt(SEQUENCE_SELECT);
             getJdbcTemplate().update(PROCESS_INSERT,
                                      processRunID,
                                      conanProcessRun.getProcessName(),
                                      javaDateToSQLDate(conanProcessRun.getStartDate()),
                                      javaDateToSQLDate(conanProcessRun.getEndDate()),
                                      conanProcessRun.getUser().getId(),
                                      conanProcessRun.getExitValue(),
                                      conanTaskID);
 
             conanProcessRun.setId(Integer.toString(processRunID));
         }
         else {
             getJdbcTemplate().update(PROCESS_UPDATE,
                                      conanProcessRun.getProcessName(),
                                      javaDateToSQLDate(conanProcessRun.getStartDate()),
                                      javaDateToSQLDate(conanProcessRun.getEndDate()),
                                      conanProcessRun.getUser().getId(),
                                      conanProcessRun.getExitValue(),
                                      conanTaskID,
                                      conanProcessRun.getId());
         }
 
         return taskDB;
     }
 
     public List<ConanTask<? extends ConanPipeline>> getAllTasks() {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         List<ConanTask<? extends ConanPipeline>> conanTasks =
                 getJdbcTemplate().query(TASK_SELECT, new ConanTaskMapper());
 
         //additional sets
         addConanTaskChildren(conanTasks);
         return conanTasks;
     }
 
     public List<ConanTask<? extends ConanPipeline>> getAllTasksSummary() {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         return getJdbcTemplate().query(TASK_SELECT, new ConanTaskMapper());
     }
 
     public List<ConanTask<? extends ConanPipeline>> getAllTasks(int maxRecords, int startingFrom) {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
 
         List<ConanTask<? extends ConanPipeline>> conanTasks =
                 getJdbcTemplate().query(TASK_SELECT_BY_DATE,
                                         new ConanTaskMapper(),
                                         startingFrom + maxRecords,
                                         startingFrom);
 
         //additional sets
         addConanTaskChildren(conanTasks);
         return conanTasks;
     }
 
     public List<ConanTask<? extends ConanPipeline>> getAllTasks(int maxRecords, int startingFrom, String orderBy) {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
 
         List<ConanTask<? extends ConanPipeline>> conanTasks =
                 getJdbcTemplate().query(TASK_SELECT_BY_PARAM,
                                         new ConanTaskMapper(),
                                         startingFrom + maxRecords,
                                         startingFrom,
                                         orderBy);
 
         //additional sets
         addConanTaskChildren(conanTasks);
         return conanTasks;
     }
 
     public List<ConanTask<? extends ConanPipeline>> getPendingTasks() {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         List<ConanTask<? extends ConanPipeline>> conanTasks =
                 getJdbcTemplate().query(TASK_SELECT_PENDING, new ConanTaskMapper());
 
         //additional sets
         addConanTaskChildren(conanTasks, TaskType.PENDING);
         return conanTasks;
     }
 
     public List<ConanTask<? extends ConanPipeline>> getPendingTasksSummary() {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         return getJdbcTemplate().query(TASK_SELECT_PENDING, new ConanTaskMapper());
     }
 
     public List<ConanTask<? extends ConanPipeline>> getRunningTasks() {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         List<ConanTask<? extends ConanPipeline>> conanTasks =
                 getJdbcTemplate().query(TASK_SELECT_RUNNING, new ConanTaskMapper());
 
         //additional sets
         addConanTaskChildren(conanTasks, TaskType.RUNNING);
         return conanTasks;
     }
 
     public List<ConanTask<? extends ConanPipeline>> getRunningTasksSummary() {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         return getJdbcTemplate().query(TASK_SELECT_RUNNING, new ConanTaskMapper());
     }
 
     public List<ConanTask<? extends ConanPipeline>> getCompletedTasks() {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         List<ConanTask<? extends ConanPipeline>> conanTasks =
                 getJdbcTemplate().query(TASK_SELECT_COMPLETED, new ConanTaskMapper());
 
         //additional sets
         addConanTaskChildren(conanTasks, TaskType.COMPLETED);
         return conanTasks;
     }
 
     public List<ConanTask<? extends ConanPipeline>> getCompletedTasksSummary() {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         return getJdbcTemplate().query(TASK_SELECT_COMPLETED, new ConanTaskMapper());
     }
 
     public List<ConanTask<? extends ConanPipeline>> getCompletedTasks(int maxRecords, int startingFrom) {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         List<ConanTask<? extends ConanPipeline>> conanTasks =
                 getJdbcTemplate().query(TASK_SELECT_COMPLETED_PAGED,
                                         new ConanTaskMapper(),
                                         startingFrom + maxRecords,
                                         startingFrom);
 
         //additional sets
         addConanTaskChildren(conanTasks, TaskType.COMPLETED);
         return conanTasks;
     }
 
     public List<ConanTask<? extends ConanPipeline>> getCompletedTasksSummary(int maxRecords, int startingFrom) {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         return getJdbcTemplate().query(TASK_SELECT_COMPLETED_PAGED,
                                        new ConanTaskMapper(),
                                        startingFrom + maxRecords,
                                        startingFrom);
     }
 
     public List<ConanTask<? extends ConanPipeline>> searchCompletedTasks(String name) {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         getLog().debug("Searching completed tasks by task name {" + name + "}");
         return getJdbcTemplate().query(TASK_SEARCH_NAME,
                                        new ConanTaskMapper(),
                                        "%" + name + "%");
     }
 
     public List<ConanTask<? extends ConanPipeline>> searchCompletedTasks(String name, String userID) {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         getLog().debug("Searching completed tasks by task name and user id {" + name + ", " + userID + "}");
         return getJdbcTemplate().query(TASK_SEARCH_NAME_USER,
                                        new ConanTaskMapper(),
                                        "%" + name + "%",
                                        userID);
     }
 
     public List<ConanTask<? extends ConanPipeline>> searchCompletedTasks(String name, Date fromDate) {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         getLog().debug(
                 "Searching completed tasks by task name and from date {" + name + ", " + fromDate.toString() + "}");
         return getJdbcTemplate().query(TASK_SEARCH_NAME_FROM_DATE,
                                        new ConanTaskMapper(),
                                        "%" + name + "%",
                                        new java.sql.Date(fromDate.getTime()));
     }
 
     public List<ConanTask<? extends ConanPipeline>> searchCompletedTasks(String name, Date fromDate, Date toDate) {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         if (fromDate == null) {
             getLog().debug(
                     "Searching completed tasks by task name and to date {" + name + ", " + toDate.toString() + "}");
             return getJdbcTemplate().query(TASK_SEARCH_NAME_TO_DATE,
                                            new ConanTaskMapper(),
                                            "%" + name + "%",
                                            new java.sql.Date(toDate.getTime()));
         }
         else {
             getLog().debug("Searching completed tasks by task name, from date and to date {" + name + ", " +
                                    fromDate.toString() + "," + toDate.toString() + "}");
             return getJdbcTemplate().query(TASK_SEARCH_NAME_FROM_TO_DATE,
                                            new ConanTaskMapper(),
                                            "%" + name + "%",
                                            new java.sql.Date(fromDate.getTime()),
                                            new java.sql.Date(toDate.getTime()));
         }
     }
 
     public List<ConanTask<? extends ConanPipeline>> searchCompletedTasks(String name,
                                                                          String userID,
                                                                          Date fromDate) {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         getLog().debug("Searching completed tasks by task name, user ID and from date {" + name + ", " +
                                userID + "," + fromDate.toString() + "}");
         return getJdbcTemplate().query(TASK_SEARCH_NAME_USER_FROM_DATE,
                                        new ConanTaskMapper(),
                                        userID,
                                        "%" + name + "%",
                                        new java.sql.Date(fromDate.getTime()));
     }
 
     public List<ConanTask<? extends ConanPipeline>> searchCompletedTasks(String name,
                                                                          String userID,
                                                                          Date fromDate,
                                                                          Date toDate) {
         Assert.notNull(getJdbcTemplate(), getClass().getSimpleName() + " must have a valid JdbcTemplate set");
         if (fromDate == null) {
             getLog().debug("Searching completed tasks by task name, user ID and to date {" + name + ", " +
                                    userID + "," + toDate.toString() + "}");
             return getJdbcTemplate().query(TASK_SEARCH_NAME_USER_TO_DATE,
                                            new ConanTaskMapper(),
                                            "%" + name + "%",
                                            userID,
                                            new java.sql.Date(toDate.getTime()));
         }
         else {
             getLog().debug("Searching completed tasks by task name, user ID, from date and to date {" + name + ", " +
                                    userID + ", " + fromDate.toString() + "," + toDate.toString() + "}");
             return getJdbcTemplate().query(TASK_SEARCH_NAME_USER_FROM_TO_DATE,
                                            new ConanTaskMapper(),
                                            "%" + name + "%",
                                            userID,
                                            new java.sql.Date(fromDate.getTime()),
                                            new java.sql.Date(toDate.getTime()));
         }
     }
 
     protected enum TaskType {
         PENDING,
         RUNNING,
         COMPLETED,
         OTHER
     }
 
     protected void addConanTaskChildren(List<ConanTask<? extends ConanPipeline>> tasks) {
         addConanTaskChildren(tasks, TaskType.OTHER);
     }
 
     protected void addConanTaskChildren(List<ConanTask<? extends ConanPipeline>> tasks,
                                         TaskType type) {
         getLog().debug("Fetching associated data for " + tasks.size() + " tasks - this will be batched if possible");
 
         // map tasks to task ids
         Map<String, DatabaseRecoveredConanTask> tasksByID = new HashMap<String, DatabaseRecoveredConanTask>();
         for (ConanTask task : tasks) {
             // index this task
             tasksByID.put(task.getId(), (DatabaseRecoveredConanTask) task);
         }
 
         // add parameters in batches
         addParametersToTasks(tasksByID, type);
         // add processes in batches
         addProcessesToTasks(tasksByID, type);
 
         getLog().debug("All associated task data fetched, tasks should now be fully populated");
     }
 
     protected void addParametersToTasks(Map<String, DatabaseRecoveredConanTask> tasksByID, TaskType type) {
         // map of genes and their properties
         ConanParameterMapper parameterMapper = new ConanParameterMapper(tasksByID);
 
         if (tasksByID.entrySet().size() == 1) {
             // only requesting params for a single task, don't fetch all
             getJdbcTemplate().query(PARAMETER_SELECT_BY_TASK_ID, parameterMapper, tasksByID.keySet().iterator().next());
         }
         else {
             switch (type) {
                 case PENDING:
                     getJdbcTemplate().query(PARAMETER_SELECT_FOR_PENDING_TASKS, parameterMapper);
                     break;
                 case RUNNING:
                     getJdbcTemplate().query(PARAMETER_SELECT_FOR_RUNNING_TASKS, parameterMapper);
                     break;
                 case COMPLETED:
                     getJdbcTemplate().query(PARAMETER_SELECT_FOR_COMPLETED_TASKS, parameterMapper);
                     break;
                 case OTHER:
                 default:
                     getJdbcTemplate().query(PARAMETER_SELECT, parameterMapper);
             }
         }
     }
 
     protected void addProcessesToTasks(Map<String, DatabaseRecoveredConanTask> tasksByID, TaskType type) {
         // map of genes and their properties
         ConanProcessMapper processMapper = new ConanProcessMapper(tasksByID);
 
         if (tasksByID.entrySet().size() == 1) {
             // only requesting processes for a single task, don't fetch all
             getJdbcTemplate().query(PROCESS_SELECT_BY_TASK_ID, processMapper, tasksByID.keySet().iterator().next());
         }
         else {
             switch (type) {
                 case PENDING:
                     getJdbcTemplate().query(PROCESS_SELECT_FOR_PENDING_TASKS, processMapper);
                     break;
                 case RUNNING:
                     getJdbcTemplate().query(PROCESS_SELECT_FOR_RUNNING_TASKS, processMapper);
                     break;
                 case COMPLETED:
                     getJdbcTemplate().query(PROCESS_SELECT_FOR_COMPLETED_TASKS, processMapper);
                     break;
                 case OTHER:
                 default:
                     getJdbcTemplate().query(PROCESS_SELECT, processMapper);
             }
         }
     }
 
     /**
      * Gets the column name to use in SQL queries given a task property name
      *
      * @param orderBy the ConanTask property name to order by
      * @return the field name in the database to order by
      */
     private String getOrderingColumnName(String orderBy) {
         if (propertyToColumnMap == null) {
             propertyToColumnMap = new HashMap<String, String>();
             propertyToColumnMap.put("name", "NAME");
             propertyToColumnMap.put("pipeline", "PIPELINE_NAME");
             propertyToColumnMap.put("submitter", "USER_ID");
             propertyToColumnMap.put("completionDate", "END_DATE");
             propertyToColumnMap.put("priority", "PRIORITY");
         }
 
         if (propertyToColumnMap.containsKey(orderBy)) {
             return propertyToColumnMap.get(orderBy);
         }
         else {
             return "END_DATE";
         }
     }
 
     /**
      * Maps database rows to ConanTask objects
      */
     private class ConanTaskMapper implements RowMapper<ConanTask<? extends ConanPipeline>> {
         private Map<String, ConanUser> userCache = new HashMap<String, ConanUser>();
 
         public ConanTask<? extends ConanPipeline> mapRow(ResultSet resultSet, int i)
                 throws SQLException {
             ConanUser submitter;
             String userID = resultSet.getString(5);
             if (userCache.containsKey(userID)) {
                 submitter = userCache.get(userID);
             }
             else {
                 submitter = getUserDAO().getUser(userID);
                 userCache.put(userID, submitter);
             }
 
             ConanPipeline conanPipeline = getPipelineDAO().getPipeline(resultSet.getString(6));
             if (conanPipeline == null) {
                 // never set a null, update to private "unrecognised pipeline"
                 conanPipeline = new DefaultConanPipeline("Unknown pipeline '" + resultSet.getString(6) + "'",
                                                          submitter,
                                                          true);
             }
 
             DatabaseRecoveredConanTask<ConanPipeline> task = new DatabaseRecoveredConanTask<ConanPipeline>();
             task.setPriority(ConanTask.Priority.valueOf(resultSet.getString(7)));
             task.setPipeline(conanPipeline);
             task.setSubmitter(submitter);
             task.setId(resultSet.getString(1));
             task.setName(resultSet.getString(2));
 
             //Dates
             task.setStartDate(sqlDateToJavaDate(resultSet.getString(3)));
             task.setCompletionDate(sqlDateToJavaDate(resultSet.getString(4)));
             //State and status message
             task.setCurrentState(ConanTask.State.valueOf(resultSet.getString(9)));
             task.setCurrentStatusMessage(resultSet.getString(10));
             //First process and executed process indexes
             task.setCurrentExecutionIndex(resultSet.getInt(11));
             task.setFirstTaskIndex(resultSet.getInt(8));
             task.setCreationDate(sqlDateToJavaDate(resultSet.getString(12)));
 
             // register listeners
             registerListeners(task);
 
             return task;
         }
     }
 
     /**
      * Maps database rows to ConanProcess objects, attaching them to one of the supplied tasks where possible
      */
     private class ConanProcessMapper implements RowMapper<ConanProcessRun> {
         private Map<String, DatabaseRecoveredConanTask> tasksByID;
         private Map<String, ConanUser> userCache = new HashMap<String, ConanUser>();
 
         private ConanProcessMapper(Map<String, DatabaseRecoveredConanTask> tasksByID) {
             this.tasksByID = tasksByID;
         }
 
         public DefaultProcessRun mapRow(ResultSet resultSet, int i) throws SQLException {
             ConanUser submitter;
             String userID = resultSet.getString(5);
             if (userCache.containsKey(userID)) {
                 submitter = userCache.get(userID);
             }
             else {
                 submitter = getUserDAO().getUser(userID);
                 userCache.put(userID, submitter);
             }
 
             String taskID = Long.toString(resultSet.getLong(7));
 
             // build the process
             DefaultProcessRun process = new DefaultProcessRun(resultSet.getString(2),
                                                               sqlDateToJavaDate(resultSet.getString(3)),
                                                               sqlDateToJavaDate(resultSet.getString(4)),
                                                               submitter);
             process.setId(resultSet.getString(1));
             process.setExitValue(resultSet.getInt(6));
 
             // add to appropriate task
             if (tasksByID.containsKey(taskID)) {
                 tasksByID.get(taskID).addConanProcessRun(process);
             }
 
             // and return process (although normally we don't do anything with this result)
             return process;
         }
     }
 
     /**
      * Maps database rows to ConanParameter objects, attaching them to one of the supplied tasks wherever possible
      */
     private class ConanParameterMapper implements RowMapper<ConanParameter> {
         private Map<String, DatabaseRecoveredConanTask> tasksByID;
 
         private ConanParameterMapper(Map<String, DatabaseRecoveredConanTask> tasksByID) {
             this.tasksByID = tasksByID;
         }
 
         public DatabaseConanParameter mapRow(ResultSet resultSet, int i) throws SQLException {
             String taskID = Long.toString(resultSet.getLong(4));
 
             // build the parameter
             DatabaseConanParameter parameter =
                     new DatabaseConanParameter(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3));
 
             // add parameter to appropriate task, but only after validating this is a parameter for this pipeline
             if (tasksByID.containsKey(taskID)) {
                 DatabaseRecoveredConanTask taskDB = tasksByID.get(taskID);
                 if (taskDB.getPipeline() != null) {
                     for (ConanParameter nextParam : taskDB.getPipeline().getAllRequiredParameters()) {
                         if (nextParam.getName().equals(parameter.getName())) {
                             taskDB.addParameterValue(nextParam, parameter.getValue());
                         }
                     }
                 }
             }
 
             // and return parameter (although normally we don't do anything with this result)
             return parameter;
         }
     }
 
     public class DatabaseConanParameter implements ConanParameter {
         private String name;
         private String value;
         private String ID;
 
         DatabaseConanParameter(String ID, String name, String value) {
             this.name = name;
             this.value = value;
             this.ID = ID;
         }
 
         public String getDescription() {
             return "Parameter stored in database";
         }
 
         public boolean isBoolean() {
             return false;
         }
 
         public boolean validateParameterValue(String value) {
             // always returns true - this value should not have been inserted without being validated first
             // todo - maybe check this assumption?
             return true;
         }
 
         public String getName() {
             return name;
         }
 
         public String getID() {
             return ID;
         }
 
         private String getValue() {
             return value;
         }
     }
 
     private Timestamp javaDateToSQLDate(Date date) {
         if (date == null) {
             return null;
         }
         return new Timestamp(date.getTime());
     }
 
     private java.util.Date sqlDateToJavaDate(String date) {
         if (date == null) {
             return null;
         }
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         try {
             return df.parse(date);
         }
         catch (Exception e) {
             return null;
         }
     }
 
     protected void registerListeners(ConanTask<? extends ConanPipeline> conanTask) {
         // add listeners, if possible
         if (conanTask instanceof AbstractConanTask) {
             for (ConanTaskListener listener : getConanTaskListeners()) {
                 ((AbstractConanTask) conanTask).addConanTaskListener(listener);
             }
         }
     }
 }
