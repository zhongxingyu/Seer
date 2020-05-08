 package cmu.ds.mr.mapred;
 
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 
 import cmu.ds.mr.conf.JobConf;
 import cmu.ds.mr.util.Log;
 import cmu.ds.mr.util.Util;
 
 /**
  * Class for running mappaer and reducer in a separate thread
  * 
  * */
 public class TaskRunner implements Runnable {
   
   public static final Log LOG = new Log("TaskRunner.class");
   
   private Task task;
   private TaskTracker taskTrackerProxy;
   
   
   public TaskRunner(Task task, TaskTracker taskTrackerProxy) {
     super();
     this.task = task;
     this.taskTrackerProxy = taskTrackerProxy;
   }
 
   @Override
   public void run() {
     try {
       task.startTask(task, taskTrackerProxy);
       LOG.info(String.format("Task %s successful.", task.taskId.toString()));
       
     } catch (Exception e) {
       try {
         taskTrackerProxy.fail(task.getTaskStatus().getTaskId());
       } catch (IOException e1) {
        LOG.error("Task fails. IOException: ");
       } catch (InterruptedException e1) {
        LOG.error("Task fails. InterruptedException: ");
       }
      LOG.error("Task fails. Exception: ");
     } 
   }
 
 }
