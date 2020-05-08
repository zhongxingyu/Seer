 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.splash.job;
 
 import java.util.concurrent.SynchronousQueue;
 import java.util.concurrent.locks.LockSupport;
 
 import org.amanzi.splash.job.SplashJobTask.SplashJobTaskResult;
 import org.amanzi.splash.ui.SplashPlugin;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 
 /**
  * Job that execute tasks in additional thread
  *
  * @author Lagutko_N
  * @since 1.0.0
  */
 public class SplashJob extends Job {
     
 	// Name of this Job
     private static final String SPLASH_JOB_NAME = "Splash Job";
     
     // a Thread where Job was called
     private Thread currentThread;
     
     // queue of Tasks
     private SynchronousQueue<SplashJobTask> taskQueue = new SynchronousQueue<SplashJobTask>();
     
     /**
      * Creates a SplashJob
      */
     public SplashJob() {
         super(SPLASH_JOB_NAME);
         //Lagutko, 12.10.2009, set this Job as System to have no Monitor 
         setSystem(true);
     }
     
     /**
      * Add task that should be executed in Job's Thread
      *  
      * @param task task to execute
      */
     public void addTask(SplashJobTask task) {        
         try {
             taskQueue.put(task);
             
             //save current thread
             currentThread = Thread.currentThread();
             //lock current thread
             LockSupport.park();
         }
         catch (InterruptedException e) {
         	//in case of exception unlock thread
         	//otherwise it will be unlocked in run method
             LockSupport.unpark(currentThread);
         }     
     }
 
     @Override
     protected IStatus run(IProgressMonitor monitor) {
         //Job will be runned until it not execute Task that returns EXIT result 
         SplashJobTaskResult result = SplashJobTaskResult.CONTINUE;
         do {
             try {
                 result = execute();
             }
             catch (InterruptedException e) {
             	//in case of exception unlock thread
             	//otherwise it will be unlocked in run method
                 LockSupport.unpark(currentThread);
             }            
         } while (!result.equals(SplashJobTaskResult.EXIT));
         
         return Status.OK_STATUS;
     }
     
     /**
      * Synchronized method that gets Task and execute it
      * 
      * @return result of execution
      * @throws InterruptedException
      */
     private synchronized SplashJobTaskResult execute() throws InterruptedException {
         SplashJobTaskResult result = SplashJobTaskResult.CONTINUE;
         try {
         	//get a Task from queue
             SplashJobTask eventToRun = taskQueue.take();
             
             //try to execute
             result = eventToRun.execute();            
         }
         catch (Exception e) {
         	//if there was an exception than Plugin will handle it
             SplashPlugin.error(null, e);
         }
         finally {
         	//in any case unlock thread
             LockSupport.unpark(currentThread);
         }
         
         return result;
     }
 
 }
