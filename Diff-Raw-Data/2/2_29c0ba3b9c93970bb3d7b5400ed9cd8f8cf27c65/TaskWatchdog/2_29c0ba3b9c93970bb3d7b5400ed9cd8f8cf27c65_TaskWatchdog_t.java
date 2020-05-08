 package org.jenkinsci.plugins.beakerbuilder;
 
 import java.util.TimerTask;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.fedorahosted.beaker4j.remote_model.BeakerTask;
 import org.fedorahosted.beaker4j.remote_model.BeakerTask.TaskInfo;
 import org.fedorahosted.beaker4j.remote_model.TaskStatus;
 
 public class TaskWatchdog extends TimerTask {
     
     public static final int DEFAULT_DELAY = 1000 * 60 * 5; // 5 minutes
     public static final int DEFAULT_PERIOD = 1000 * 60 * 5; // 5 minutes
 
     
     private BeakerTask task;
     private TaskStatus status;
     private TaskStatus oldStatus;
     private boolean isFinished;
     
     public TaskWatchdog(BeakerTask task, TaskStatus status) {
         this.task = task;
         this.status = status;
         this.oldStatus = status;
         this.isFinished = false;
     }
     
     public synchronized void run() {
         try {
             TaskInfo info = task.getInfo();
            oldStatus = status;
             status = info.getState();
             isFinished = info.isFinished();
             if(oldStatus != status) {
                 notifyAll();
             }
         } catch(XmlRpcException e) {
             
         }
     }
     
     public TaskStatus getStatus() {
         return status;
     }
 
     public TaskStatus getOldStatus() {
         return oldStatus;
     }
 
     public boolean isFinished() {
         return isFinished;
     }
 
 
 }
