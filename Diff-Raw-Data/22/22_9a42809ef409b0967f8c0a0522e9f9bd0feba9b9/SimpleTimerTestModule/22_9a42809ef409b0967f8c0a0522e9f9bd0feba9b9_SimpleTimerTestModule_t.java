 package org.nebulostore.timer;
 
 import org.nebulostore.appcore.Message;
 import org.nebulostore.appcore.ReturningJobModule;
 import org.nebulostore.appcore.exceptions.NebuloException;
 
 /**
  * Module to test Timer.
  */
 class SimpleTimerTestModule extends ReturningJobModule<Void> {
 
   /* required precision of system time */
  static final Long PRECISION = 20L;
 
   private TimerTestVisitor<Void> visitor_ = new STTVisitor();
 
   @Override
   protected void processMessage(Message message) throws NebuloException {
     ((AbstractTimerTestMessage) message).accept(visitor_);
   }
 
   /**
    * Visitor.
    */
   private class STTVisitor extends TimerTestVisitor<Void> {
     private Long startTime_ = System.currentTimeMillis();
 
     @Override
     public Void visit(InitSimpleTimerTestMessage message) {
       jobId_ = message.getId();
 
       TimerContext.getInstance().addDelayedMessage(startTime_ + 3000,
           new TimerTestMessage(message.getId(), 3));
 
       TimerContext.getInstance().addDelayedMessage(startTime_ + 1000,
           new TimerTestMessage(message.getId(), 0));
 
       // messages doubled
       TimerContext.getInstance().addDelayedMessage(startTime_ + 2000,
           new TimerTestMessage(message.getId(), 1));
       TimerContext.getInstance().addDelayedMessage(startTime_ + 2000,
           new TimerTestMessage(message.getId(), 2));
       return null;
     }
 
     @Override
     public Void visit(TimerTestMessage message) {
       switch (message.code_) {
         case 0:
           assert Math.abs(System.currentTimeMillis() - startTime_ - 1000) < PRECISION;
           break;
         case 1:
         case 2:
           assert Math.abs(System.currentTimeMillis() - startTime_ - 2000) < PRECISION;
           break;
         case 3:
           assert Math.abs(System.currentTimeMillis() - startTime_ - 3000) < PRECISION;
           endWithSuccess(null);
           break;
         default:
           break;
       }
       return null;
     }
   }
 }
