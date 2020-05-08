 package uk.ac.ox.oucs.search.queueing;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ox.oucs.search.indexing.Task;
 
 import java.util.concurrent.ExecutorService;
 
 import static uk.ac.ox.oucs.search.queueing.DefaultTask.Type.INDEX_DOCUMENT;
 import static uk.ac.ox.oucs.search.queueing.DefaultTask.Type.REMOVE_DOCUMENT;
 
 /**
  * @author Colin Hebert
  */
public class IndexQueueingImpl extends WaitingTaskRunner implements IndexQueueing{
     private static final Logger logger = LoggerFactory.getLogger(IndexQueueingImpl.class);
     private ExecutorService taskSplittingExecutor;
     private ExecutorService indexingExecutor;
 
     public IndexQueueingImpl() {
         setIndexQueueing(this);
     }
 
     public void destroy() {
         indexingExecutor.shutdownNow();
         taskSplittingExecutor.shutdownNow();
     }
 
     @Override
     public void addTaskToQueue(Task task) {
         if (INDEX_DOCUMENT.getTypeName().equals(task.getType()) || REMOVE_DOCUMENT.getTypeName().equals(task.getType())) {
             if (logger.isDebugEnabled())
                 logger.debug("Add task '" + task + "' to the indexing executor");
             indexingExecutor.execute(new RunnableTask(task));
         } else {
             if (logger.isDebugEnabled())
                 logger.debug("Add task '" + task + "' to the task splitting executor");
             taskSplittingExecutor.execute(new RunnableTask(task));
         }
     }
 
     public void setIndexingExecutor(ExecutorService indexingExecutor) {
         this.indexingExecutor = indexingExecutor;
     }
 
     public void setTaskSplittingExecutor(ExecutorService taskSplittingExecutor) {
         this.taskSplittingExecutor = taskSplittingExecutor;
     }
 
     private class RunnableTask implements Runnable {
         private final Task task;
 
         private RunnableTask(Task task) {
             this.task = task;
         }
 
         @Override
         public void run() {
             runTask(task);
         }
     }
 }
