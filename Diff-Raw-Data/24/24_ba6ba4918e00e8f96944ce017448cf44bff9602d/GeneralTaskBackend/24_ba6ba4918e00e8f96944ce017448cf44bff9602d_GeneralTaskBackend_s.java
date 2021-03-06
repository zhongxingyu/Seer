 package ru.taskurotta.backend.storage;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ru.taskurotta.backend.checkpoint.CheckpointService;
 import ru.taskurotta.backend.checkpoint.TimeoutType;
 import ru.taskurotta.backend.checkpoint.model.Checkpoint;
 import ru.taskurotta.backend.checkpoint.model.CheckpointQuery;
 import ru.taskurotta.backend.console.model.GenericPage;
 import ru.taskurotta.backend.console.retriever.TaskInfoRetriever;
 import ru.taskurotta.transport.model.ArgContainer;
 import ru.taskurotta.transport.model.DecisionContainer;
 import ru.taskurotta.transport.model.TaskContainer;
 import ru.taskurotta.transport.model.TaskType;
 
 import java.util.List;
 import java.util.UUID;
 
 /**
  * User: romario
  * Date: 4/1/13
  * Time: 9:34 PM
  */
 public class GeneralTaskBackend implements TaskBackend, TaskInfoRetriever {
 
     private final static Logger logger = LoggerFactory.getLogger(GeneralTaskBackend.class);
 
     private TaskDao taskDao;
 
     private CheckpointService checkpointService;
 
     public GeneralTaskBackend(TaskDao taskDao, CheckpointService checkpointService) {
         this.taskDao = taskDao;
         this.checkpointService = checkpointService;
     }
 
     @Override
     public void startProcess(TaskContainer taskContainer) {
         taskDao.addTask(taskContainer);
     }
 
     @Override
     public TaskContainer getTaskToExecute(UUID taskId, UUID processId) {
 
         logger.debug("getTaskToExecute() taskId = [{}]", taskId);
 
         TaskContainer task = getTask(taskId, processId);
 
         // WARNING: "task" object is the same instance as In memory data storage. So we should use  it deep clone
         // due guarantees for its immutability.
 
         if (task != null) {
             ArgContainer[] args = task.getArgs();
 
             if (args != null) {
 
                 for (int i = 0; i < args.length; i++) {
                     ArgContainer arg = args[i];
 
                     if (arg.isPromise()) {
 
                         if (arg.isReady() && !task.getType().equals(TaskType.DECIDER_ASYNCHRONOUS)) {
 
                             // set real value to Actor tasks
                             args[i] = arg.updateType(ArgContainer.ValueType.PLAIN);
                             continue;
                         }
 
                         ArgContainer taskValue = getTaskValue(arg.getTaskId(), processId);
                         if (taskValue == null) {
                             // value may be null for NoWait promises
                             // leave it in peace...
                             continue;
                         }
 
                         if (task.getType().equals(TaskType.DECIDER_ASYNCHRONOUS)) {
                             // set real value into promise for Decider tasks
                             args[i] = arg.updateValue(taskValue);
                         } else {
                             // swap promise with real value for Actor tasks
                            args[i] = taskValue;
                         }
                     } else if (arg.isObjectArray()) {
 
                         ArgContainer[] compositeValue = arg.getCompositeValue();
                         for (int j = 0; j < compositeValue.length; j++) {
                             ArgContainer innerArg = compositeValue[j];
                             ArgContainer taskValue = getTaskValue(innerArg.getTaskId(), processId);
 
                             if (task.getType().equals(TaskType.DECIDER_ASYNCHRONOUS)) {
                                 // set real value into promise for Decider tasks
                                 compositeValue[j] = innerArg.updateValue(taskValue);
                             } else {
                                 // swap promise with real value for Actor tasks
                                 compositeValue[j] = taskValue;
                             }
                         }
                     }
                 }
             }
 
             //Setting TASK_START checkpoint
             Checkpoint startCheckpoint = new Checkpoint(TimeoutType.TASK_START_TO_CLOSE, taskId, task.getProcessId(), task.getActorId(), System.currentTimeMillis());
             checkpointService.addCheckpoint(startCheckpoint);
 
         }
 
         return task;
     }
 
     private ArgContainer getTaskValue(UUID taskId, UUID processId) {
 
         logger.debug("getTaskValue() taskId = [{}]", taskId);
         if (taskId == null) {
             throw new IllegalStateException("Cannot find value for NULL taskId");
         }
 
         DecisionContainer taskDecision = taskDao.getDecision(taskId, processId);
 
         if (taskDecision == null) {
             logger.debug("getTaskValue() taskDecision == null");
             return null;
         }
 
         ArgContainer argContainer = taskDecision.getValue();
 
         if (argContainer == null) {
             logger.debug("getTaskValue() argContainer == null");
             return null;
         }
 
         if (!argContainer.isPromise()) {
             logger.debug("getTaskValue() !argContainer.isPromise()");
             return argContainer;
         }
 
         if (argContainer.isPromise() && !argContainer.isReady()) {
             logger.debug("getTaskValue() argContainer.isPromise() && !argContainer.isReady()");
             return getTaskValue(argContainer.getTaskId(), processId);
         }
 
         logger.debug("getTaskValue() returns argContainer = [{}]", argContainer);
         return argContainer;
     }
 
 
     @Override
     public TaskContainer getTask(UUID taskId, UUID processId) {
         TaskContainer task = taskDao.getTask(taskId, processId);
         logger.debug("Task getted by uuid[{}], process uuid[{}] is[{}]", taskId, processId, task);
         return task;
     }
 
     @Override
     public List<TaskContainer> getProcessTasks(UUID processId) {
         return taskDao.getProcessTasks(processId);
     }
 
     @Override
     public GenericPage<TaskContainer> listTasks(int pageNumber, int pageSize) {
         return taskDao.listTasks(pageNumber, pageSize);
     }
 
     @Override
     public List<TaskContainer> getRepeatedTasks(int iterationCount) {
         return taskDao.getRepeatedTasks(iterationCount);
     }
 
     @Override
     public void addDecision(DecisionContainer taskDecision) {
 
         logger.debug("addDecision() taskDecision [{}]", taskDecision);
         TaskContainer task = taskDao.getTask(taskDecision.getTaskId(), taskDecision.getProcessId());
         checkpointService.addCheckpoint(new Checkpoint(TimeoutType.TASK_RELEASE_TO_COMMIT, task.getTaskId(), task.getProcessId(),task.getActorId(), System.currentTimeMillis()));
 
         taskDao.addDecision(taskDecision);
 
         // increment number of attempts for error tasks with retry policy
         if (taskDecision.containsError() && taskDecision.getRestartTime() != -1) {
 
             task.incrementNumberOfAttempts();
             taskDao.updateTask(task);
         }
 
         TaskContainer[] taskContainers = taskDecision.getTasks();
         if (taskContainers != null) {
             for (TaskContainer taskContainer : taskContainers) {
                 taskDao.addTask(taskContainer);
             }
         }
     }
 
     @Override
     public DecisionContainer getDecision(UUID taskId, UUID processId) {
         return taskDao.getDecision(taskId, processId);
     }
 
     @Override
     public void addDecisionCommit(DecisionContainer taskDecision) {
         //Removing checkpoints
         checkpointService.removeTaskCheckpoints(taskDecision.getTaskId(), taskDecision.getProcessId(), TimeoutType.TASK_START_TO_CLOSE);
         checkpointService.removeTaskCheckpoints(taskDecision.getTaskId(), taskDecision.getProcessId(), TimeoutType.TASK_SCHEDULE_TO_CLOSE);
         checkpointService.removeTaskCheckpoints(taskDecision.getTaskId(), taskDecision.getProcessId(), TimeoutType.TASK_RELEASE_TO_COMMIT);
     }
 
     @Override
     public List<TaskContainer> getAllRunProcesses() {
         return null;
     }
 
     @Override
     public List<DecisionContainer> getAllTaskDecisions(UUID processId) {
         return null;
     }
 
     @Override
     public void finishProcess(UUID processId) {
        taskDao.getProcessTasks(processId);
        for (TaskContainer taskContainer : taskDao.getProcessTasks(processId)) {
            taskDao.removeTask(taskContainer.getTaskId(), processId);
        }
     }
 
     
     public boolean isTaskInProgress(UUID taskId, UUID processId) {
         List<Checkpoint> checkpoints = getCheckpointService().listCheckpoints(new CheckpointQuery(TimeoutType.TASK_START_TO_CLOSE, taskId, processId, null, -1, -1));
         return checkpoints != null && !checkpoints.isEmpty();
     }
 
     public boolean isTaskReleased(UUID taskId, UUID processId) {
         return taskDao.isTaskReleased(taskId, processId);
     }
 
     @Override
     public CheckpointService getCheckpointService() {
         return checkpointService;
     }
 
     public void setCheckpointService(CheckpointService checkpointService) {
         this.checkpointService = checkpointService;
     }
 
 
 }
