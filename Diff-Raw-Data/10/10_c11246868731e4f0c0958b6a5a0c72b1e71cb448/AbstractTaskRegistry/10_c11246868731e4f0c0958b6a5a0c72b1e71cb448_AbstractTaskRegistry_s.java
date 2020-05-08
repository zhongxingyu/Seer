 package ddth.dasp.framework.scheduletask;
 
 import java.text.MessageFormat;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ddth.dasp.common.id.IdGenerator;
 
 public abstract class AbstractTaskRegistry implements ITaskRegistry {
 
     private final Logger LOGGER = LoggerFactory.getLogger(AbstractTaskRegistry.class);
     private boolean valid = true;
     private BlockingQueue<TaskSchedulingInfo> taskSchedulingBuffer = new LinkedBlockingQueue<TaskSchedulingInfo>(
             1024);
 
     public void init() {
         Thread bufferWatcherThread = new Thread() {
             public void run() {
                try {
                    while (!interrupted() && valid) {
                         TaskSchedulingInfo schedulingInfo = taskSchedulingBuffer.poll();
                         if (schedulingInfo != null) {
                             if (!_scheduleTask(schedulingInfo)) {
                                 taskSchedulingBuffer.offer(schedulingInfo);
                             }
                         }
                         Thread.yield();
                         // try {
                         // Thread.sleep(5000);
                         // } catch (InterruptedException e) {
                         // }
                     }
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage(), e);
                 }
             }
         };
         bufferWatcherThread.setPriority(Thread.MIN_PRIORITY);
         bufferWatcherThread.setName(HazelcastLocalRunClusterStatusTaskRegistry.class.getName());
         bufferWatcherThread.setDaemon(true);
         bufferWatcherThread.start();
     }
 
     public void destroy() {
         valid = false;
     }
 
     /*---------- storage methods ----------*/
     protected abstract boolean storeTaskSchedulingInfo(TaskSchedulingInfo schedulingInfo);
 
     protected abstract TaskSchedulingInfo retrieveTaskSchedulingInfo(String taskId);
 
     protected abstract boolean removeTaskSchedulingInfo(String taskId);
 
     protected abstract ITask.Status getTaskStatus(String taskId);
 
     protected abstract void preRunning(String taskId);
 
     protected abstract void postRunning(String taskId);
 
     /*---------- storage methods ----------*/
 
     // protected abstract void scheduleToRun(TaskSchedulingInfo schedulingInfo,
     // boolean firstTime);
     protected abstract void scheduleToRun(String taskId, boolean firstTime);
 
     private boolean _scheduleTask(TaskSchedulingInfo schedulingInfo) {
         try {
             ITask task = schedulingInfo.getTask();
             String taskId = task.getId();
             TaskSchedulingInfo oldSchedulingInfo = retrieveTaskSchedulingInfo(taskId);
             if (storeTaskSchedulingInfo(schedulingInfo)) {
                 if (oldSchedulingInfo == null) {
                     // new task registration, schedule to run
                     // scheduleToRun(schedulingInfo, true);
                     scheduleToRun(taskId, true);
                 }
                 return true;
             } else {
                 return false;
             }
         } catch (Exception e) {
             return false;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean scheduleTask(TaskSchedulingInfo schedulingInfo) {
         if (!valid) {
             return false;
         }
         return taskSchedulingBuffer.offer(schedulingInfo);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean unscheduleTask(String taskId) {
         return removeTaskSchedulingInfo(taskId);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean unscheduleTask(ITask task) {
         return removeTaskSchedulingInfo(task.getId());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public TaskSchedulingInfo getTaskSchedulingInfo(String taskId) {
         return retrieveTaskSchedulingInfo(taskId);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public TaskSchedulingInfo getTaskSchedulingInfo(ITask task) {
         return retrieveTaskSchedulingInfo(task.getId());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isRunning(ITask task) {
         return isRunning(task.getId());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isRunning(String taskId) {
         ITask.Status taskStatus = getTaskStatus(taskId);
         return taskStatus != null && taskStatus == ITask.Status.RUNNING;
     }
 
     protected abstract class TaskRunner implements Runnable {
         private TaskSchedulingInfo schedulingInfo;
 
         public TaskRunner(TaskSchedulingInfo schedulingInfo) {
             this.schedulingInfo = schedulingInfo;
         }
 
         protected TaskSchedulingInfo getSchedulingInfo() {
             return schedulingInfo;
         }
 
         protected void logTaskExecution(String runId, ITask task, Object params, Object output,
                 long startTimestamp, long endTimestamp, Throwable exception) {
             String taskId = task.getId();
             long duration = endTimestamp - startTimestamp;
             String msg = "Run: {0}\n- Task {1} finished in {2} ms; exception: {3}.\n- Input: {4}\n- Output: {5}\n";
             msg = MessageFormat.format(msg, runId, taskId, duration,
                     exception != null ? exception.getMessage() : null, params, output);
             if (exception != null) {
                 LOGGER.debug(msg, exception);
             } else {
                 LOGGER.debug(msg);
             }
         }
 
         @Override
         public void run() {
             ITask task = schedulingInfo.getTask();
             String taskId = task.getId();
             if (isRunning(taskId) && !task.isAllowConcurrent()) {
                 return;
             }
 
             long startTimestamp = System.currentTimeMillis();
             String runId = IdGenerator.getInstance(IdGenerator.getMacAddr()).generateId64Hex();
             Throwable exception = null;
             Object params = schedulingInfo.getParams();
             Object output = null;
             preRunning(taskId);
             try {
                 output = internalRunTask(task, params);
             } catch (Throwable t) {
                 exception = t;
             } finally {
                 postRunning(taskId);
                 long endTimestamp = System.currentTimeMillis();
                 logTaskExecution(runId, task, params, output, startTimestamp, endTimestamp,
                         exception);
                 switch (schedulingInfo.getScheduling()) {
                 case CONTINUOUS:
                 case REPEATED:
                     // scheduleToRun(schedulingInfo, false);
                     scheduleToRun(taskId, false);
                 case RUNONCE:
                     break;
                 }
             }
         }
 
         /**
          * Sub-class overrides this method to implement its own business.
          * 
          * @param task
          * @param params
          * @return
          */
         protected abstract Object internalRunTask(ITask task, Object params);
     }
 }
