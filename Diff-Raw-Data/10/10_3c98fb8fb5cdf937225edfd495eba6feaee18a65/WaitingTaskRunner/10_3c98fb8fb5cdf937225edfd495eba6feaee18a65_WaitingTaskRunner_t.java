 package org.sakaiproject.search.queueing;
 
 import org.sakaiproject.authz.api.SecurityAdvisor;
 import org.sakaiproject.authz.api.SecurityService;
 import org.sakaiproject.component.cover.ComponentManager;
 import org.sakaiproject.search.indexing.Task;
 import org.sakaiproject.search.indexing.TaskHandler;
 import org.sakaiproject.search.indexing.exception.NestedTaskHandlingException;
 import org.sakaiproject.search.indexing.exception.TaskHandlingException;
 import org.sakaiproject.search.indexing.exception.TemporaryTaskHandlingException;
 import org.sakaiproject.thread_local.api.ThreadLocalManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  * TaskRunner putting itself on lockdown if a TemporaryTaskHandlingException has been caught.
  * <p>
  * Assuming that every {@link Task} should either be successfully executed or completely fail,
  * a {@link TemporaryTaskHandlingException} means that the {@link TaskHandler} shouldn't process new tasks
  * for a short period of time.
  * </p>
  * <p>
  * This TaskRunner will put every thread in charge of running tasks on hold each time a
  * {@code TemporaryTaskHandlingException} is caught.<br />
  * The waiting time is doubled each time a Task fails with a {@code TemporaryTaskHandlingException}
  * until it reaches the {@link #maximumWaitingTime}.<br />
  * The waiting time is reset once a task has been successfully executed.
  * </p>
  *
  * @author Colin Hebert
  */
 public abstract class WaitingTaskRunner implements TaskRunner {
     private static final long BASE_WAITING_TIME = 10;
     private static final long DEFAULT_MAXIMUM_WAITING_TIME = 5 * 60 * BASE_WAITING_TIME;
     private static final Logger logger = LoggerFactory.getLogger(WaitingTaskRunner.class);
     private static final SecurityAdvisor OPEN_SECURITY_ADVISOR = new SecurityAdvisor() {
         @Override
         public SecurityAdvice isAllowed(String userId, String function, String reference) {
             return SecurityAdvice.ALLOWED;
         }
     };
     private final ReentrantLock taskRunnerLock = new ReentrantLock();
     /**
      * Maximum period of lockdown.
      * <p>
      * To avoid an over reaction of the lockdown (which doubles in time for every
      * {@code TemporaryTaskHandlingException}), a maximum can be reached.<br />
      * The maximum period defaults to 5 minutes.
      * </p>
      */
     private long maximumWaitingTime = DEFAULT_MAXIMUM_WAITING_TIME;
     private long waitingTime = BASE_WAITING_TIME;
     private TaskHandler taskHandler;
     private SecurityService securityService;
     private IndexQueueing indexQueueing;
 
     @Override
     public void runTask(Task task) {
         try {
             checkLockdown();
 
             // Unlock permissions so every resource is accessible
             securityService.pushAdvisor(OPEN_SECURITY_ADVISOR);
 
             try {
                 taskHandler.executeTask(task);
                // If there is no exceptions, reset the timer
                waitingTime = BASE_WAITING_TIME;
             } catch (NestedTaskHandlingException e) {
                 logger.warn("Some exceptions happened during the execution of '" + task + "'.");
                 unfoldNestedTaskException(e);
             } catch (TemporaryTaskHandlingException e) {
                 logger.warn("Couldn't execute task '" + task + "'.", e);
                 handleTemporaryTaskHandlingException(e);
             } catch (Exception e) {
                 logger.error("Couldn't execute task '" + task + "'.", e);
             }
 
            // A TemporaryTaskException occurred, stop everything for a while (so the search server can recover)
             if (taskRunnerLock.isHeldByCurrentThread())
                 initiateLockdown();
 
         } catch (InterruptedException e) {
             logger.error("Thread interrupted while trying to do '" + task + "'.", e);
             indexQueueing.addTaskToQueue(task);
         } finally {
             // Lock permissions to avoid security issues
             securityService.popAdvisor(OPEN_SECURITY_ADVISOR);
 
             // Clean up the localThread after each task
             ThreadLocalManager threadLocalManager = (ThreadLocalManager) ComponentManager.get(ThreadLocalManager.class);
             threadLocalManager.clear();
 
             // A TemporaryTaskException occurred and the waiting time is now passed (or an exception killed it)
             // unlock everything and get back to work
             if (taskRunnerLock.isHeldByCurrentThread())
                 terminateLockdown();
         }
     }
 
     /**
      * Checks if the lockdown has been initiated, wait until it has been terminated if it's the case.
      *
      * @throws InterruptedException
      */
     private void checkLockdown() throws InterruptedException {
         // Stop for a while because some tasks failed and should be run again.
         synchronized (taskRunnerLock) {
             while (taskRunnerLock.isLocked()) {
                 if (logger.isDebugEnabled())
                     logger.debug("Indexation system on lockdown due to a temporary failure of the system.");
                 taskRunnerLock.wait();
                 if (logger.isDebugEnabled())
                     logger.debug("Lockdown terminated, ready to process new tasks.");
             }
         }
     }
 
     /**
      * Initiates the lockdown preventing new tasks to be executed.
      *
      * @throws InterruptedException
      */
     private void initiateLockdown() throws InterruptedException {
         logger.warn("A temporary exception has been caught, "
                 + "put the indexation system on lockdown for " + waitingTime + "ms.");
         Thread.sleep(waitingTime);
         // Multiply the waiting time by two
         if (waitingTime <= maximumWaitingTime)
             waitingTime <<= 1;
     }
 
     /**
      * Terminates the lockdown, resuming tasks running.
      */
     private void terminateLockdown() {
         if (logger.isDebugEnabled())
             logger.info("Lockdown terminated, restart all the indexing threads.");
         synchronized (taskRunnerLock) {
             taskRunnerLock.notifyAll();
             taskRunnerLock.unlock();
         }
     }
 
     /**
      * Handles the content of a NestedTaskHandlingException exception.
      *
      * @param e NestedTaskHandlingException to unfold.
      */
     private void unfoldNestedTaskException(NestedTaskHandlingException e) {
         for (TaskHandlingException t : e.getTaskHandlingExceptions()) {
             if (t instanceof TemporaryTaskHandlingException) {
                 handleTemporaryTaskHandlingException((TemporaryTaskHandlingException) t);
             } else {
                 logger.error("An exception occurred during the task execution.", t);
             }
         }
     }
 
     /**
      * Handles a handleTemporaryTaskHandlingException by obtaining a lock to initiate the lockdown and add the new tasks
      * to the queue.
      *
      * @param tthe TemporaryTaskHandlingException to handle.
      */
     private void handleTemporaryTaskHandlingException(TemporaryTaskHandlingException tthe) {
         // A TemporaryTaskHandlingException means that the locking system must be initialised
         // If it's already initialised, carry on
         taskRunnerLock.tryLock();
         logger.info("A task failed because of a temporary exception. "
                 + "'" + tthe.getNewTask() + "' will be executed later", tthe);
         indexQueueing.addTaskToQueue(tthe.getNewTask());
     }
 
     public void setMaximumWaitingTime(long maximumWaitingTime) {
         this.maximumWaitingTime = maximumWaitingTime;
     }
 
     public void setSecurityService(SecurityService securityService) {
         this.securityService = securityService;
     }
 
     public void setTaskHandler(TaskHandler taskHandler) {
         this.taskHandler = taskHandler;
     }
 
     public void setIndexQueueing(IndexQueueing indexQueueing) {
         this.indexQueueing = indexQueueing;
     }
 }
