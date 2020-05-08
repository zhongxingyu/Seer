 package mi.task;
 
 /**
 * Scheduler that execute tasks in current thread.
  * @author goldolphin
  *         2014-09-06 15:10
  */
 public class SynchronizedScheduler implements IScheduler {
     @Override
     public void schedule(ITask<?> task, Object state, IContinuation cont, ITask<?> previous) {
         System.out.format("Run: task=%s, state=%s, cont=%s, previous=%s, scheduler=%s\n",
                 task, state, cont, previous, this);
         task.onExecute(state, cont, previous, this);
     }
 }
