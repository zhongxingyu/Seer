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
    timer.schedule(task, periodMillis, periodMillis);
   }
 
   @Override
   public void cancel() {
     timer.cancel();
   }
 
 }
