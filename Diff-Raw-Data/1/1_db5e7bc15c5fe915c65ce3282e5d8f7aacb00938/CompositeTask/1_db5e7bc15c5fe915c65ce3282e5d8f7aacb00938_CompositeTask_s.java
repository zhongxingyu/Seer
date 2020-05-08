 package com.github.marschall.punch;
 
 import java.util.Collection;
 
 abstract class CompositeTask extends RecoverableTask {
 
   final Collection<RecoverableTask> tasks;
 
   public CompositeTask(Collection<RecoverableTask> tasks) {
     this.tasks = tasks;
   }
 
   @Override
   void setTaskPath(TaskPath taskPath) {
     int i = 0;
     for (RecoverableTask task : this.tasks) {
       task.setTaskPath(taskPath.add(i));
       i += 1;
     }
   }
 
   void ensureTaskPathSet() {
     // check-then act is thread safe here because it's executed before
     // the first top level task
     if (this.taskPath == null) {
       this.setTaskPath(TaskPath.root());
     }
   }
 
   @Override
   public void recover(RecoveryService recoveryService) {
     this.ensureTaskPathSet();
     for (RecoverableTask task : this.tasks) {
       task.recover(recoveryService);
     }
   }
 
 }
