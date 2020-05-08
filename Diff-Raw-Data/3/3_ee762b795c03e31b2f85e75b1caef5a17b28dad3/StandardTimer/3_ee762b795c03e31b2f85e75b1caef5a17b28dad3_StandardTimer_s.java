 package org.codeswarm.timerfactory;
 
 class StandardTimer implements Timer {
 
   private final java.util.TimerTask task;
   private final java.util.Timer timer = new java.util.Timer();
 
   public StandardTimer(final TimerTask task) {
     this.task = new java.util.TimerTask() {
       @Override
       public void run() {
         task.run();
       }
     };
   }
 
   @Override
   public void run() {
     task.run();
   }
 
   @Override
   public void schedule(int delayMillis) {
     timer.schedule(task, delayMillis);
   }
 
   @Override
   public void scheduleRepeating(int periodMillis) {
    timer.scheduleAtFixedRate(task, 0, periodMillis);
    // TODO this is probably wrong - read the javadoc for scheduleAtFixedRate once I get off the airplane
   }
 
   @Override
   public void cancel() {
     timer.cancel();
   }
 
 }
