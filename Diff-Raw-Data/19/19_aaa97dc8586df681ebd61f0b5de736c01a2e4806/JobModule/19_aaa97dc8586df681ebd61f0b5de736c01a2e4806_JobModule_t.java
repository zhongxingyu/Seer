 package org.nebulostore.appcore;
 
 import java.util.concurrent.BlockingQueue;
 
 import org.apache.log4j.Logger;
 import org.nebulostore.dispatcher.messages.JobEndedMessage;
 import org.nebulostore.dispatcher.messages.JobInitMessage;
 
 /**
  * @author bolek
  * Base class for all job handlers - modules that are managed by dispatcher.
  * Message queues outQueue_ and inQueue_ are always connected to dispatcher.
  */
 public abstract class JobModule extends Module {
   private static Logger logger_ = Logger.getLogger(JobModule.class);
 
   protected BlockingQueue<Message> networkQueue_;
   protected String jobId_;
   protected boolean isStarted_;
 
   public JobModule() {
     jobId_ = null;
   }
 
   public JobModule(String jobId) {
     jobId_ = jobId;
   }
 
   public void setNetworkQueue(BlockingQueue<Message> networkQueue) {
     networkQueue_ = networkQueue;
   }
 
   /**
    * Run this module through a JobInitMessage (with new random ID) sent to Dispatcher.
    */
   public void runThroughDispatcher(BlockingQueue<Message> dispatcherQueue) {
     if (isStarted_) {
       logger_.error("Module already ran.");
       return;
     }
 
     isStarted_ = true;
    dispatcherQueue.add(new JobInitMessage(this));
   }
 
   /**
    * IMPORTANT: This should be the very LAST method to call for a dying thread.
    */
   protected void endJobModule() {
     // Inform run() (in base class) that this thread is ready to die.
     endModule();
 
     // Inform dispatcher that we are going to die.
     outQueue_.add(new JobEndedMessage(jobId_));
   }
 
   public String getJobId() {
     return jobId_;
   }
 }
