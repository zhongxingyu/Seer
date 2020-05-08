 package com.clouway.asynctaskscheduler.gae;
 
 import com.clouway.asynctaskscheduler.spi.AsyncEvent;
 import com.clouway.asynctaskscheduler.spi.AsyncEventBus;
 import com.clouway.asynctaskscheduler.spi.AsyncTaskOptions;
 import com.clouway.asynctaskscheduler.spi.AsyncTaskScheduler;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 import java.util.logging.Logger;
 
 /**
  * @author Mihail Lesikov (mlesikov@gmail.com)
  */
 class TaskQueueEventBus implements AsyncEventBus {
   private final Logger log = Logger.getLogger(TaskQueueEventBus.class.getName());
 
   private final Provider<AsyncTaskScheduler> taskScheduler;
 
   @Inject
   public TaskQueueEventBus(Provider<AsyncTaskScheduler> taskScheduler) {
     this.taskScheduler = taskScheduler;
   }
 
   @Override
   public void fireEvent(AsyncEvent<?> event) {
     log.info("fired async event : " + event.getClass().getSimpleName());
     taskScheduler.get().add(AsyncTaskOptions.event(event)).now();
   }
 
   @Override
   public void fireEvent(AsyncEvent<?> event, long delayMills) {
     log.info("fired async event : " + event.getClass().getSimpleName());
     taskScheduler.get().add(AsyncTaskOptions.event(event).delay(delayMills)).now();
   }
 
   @Override
  public void fireTrasnactionlessEvent(AsyncEvent<?> event) {
     log.info("fired async event : " + event.getClass().getSimpleName());
     taskScheduler.get().add(AsyncTaskOptions.event(event).transactionless()).now();
   }
 
   @Override
  public void fireTrasnactionlessEvent(AsyncEvent<?> event, long delayMills) {
     log.info("fired async event : " + event.getClass().getSimpleName());
     taskScheduler.get().add(AsyncTaskOptions.event(event).delay(delayMills).transactionless()).now();
   }
 }
