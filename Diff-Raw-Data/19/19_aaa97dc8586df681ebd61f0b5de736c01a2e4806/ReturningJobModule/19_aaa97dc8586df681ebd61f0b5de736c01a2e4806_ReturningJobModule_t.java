 package org.nebulostore.appcore;
 
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 
 import org.nebulostore.appcore.exceptions.NebuloException;
 
 /**
  * ReturningJobModule - base class for JobModules that return a result (e.g. all API methods).
  * @param <R>
  *    return type.
  */
 public abstract class ReturningJobModule<R> extends JobModule {
   private R result_;
   private NebuloException error_;
   private Semaphore resultReady_;
 
   protected ReturningJobModule() {
     resultReady_ = new Semaphore(0);
   }
 
   /*
    * This method is BLOCKING!
    * It blocks for at most timeoutSec seconds and throws exception when the result is not ready
    * within this time limit.
    */
   public R getResult(int timeoutSec) throws NebuloException {
     try {
       if (!resultReady_.tryAcquire(timeoutSec, TimeUnit.SECONDS)) {
        throw new NebuloException("Timeout in getResult().");
       }
     } catch (InterruptedException exception) {
       throw new NebuloException("Interrupted while waiting for result", exception);
     }
     if (error_ != null) {
       throw error_;
     } else {
       return result_;
     }
   }
 
   protected void endWithSuccess(R result) {
     result_ = result;
     // Make result available.
     resultReady_.release();
     // End thread and remove from Dispatcher's queue.
     endJobModule();
   }
 
   protected void endWithError(NebuloException error) {
     error_ = error;
     // Make result available.
     resultReady_.release();
     // End thread and remove from Dispatcher's queue.
     endJobModule();
   }
 }
