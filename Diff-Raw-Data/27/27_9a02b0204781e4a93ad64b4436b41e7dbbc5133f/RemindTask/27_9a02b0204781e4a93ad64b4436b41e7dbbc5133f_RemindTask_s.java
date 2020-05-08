 package core;
 
 
 
 import java.util.TimerTask;
 
 
 import entities.RemindEntity;
 
 public class RemindTask extends TimerTask {
 
     private RemindEntity remind;
     private RemindManager remindManager;
     
    
    public RemindTask(RemindEntity remind, RemindManager remindManager) {
         this.remind = remind;
         this.remindManager = remindManager;
     }
     
     
     
     @Override
     public void run() {
        this.remindManager.deliverRemind(this.remind);
     }
     
 }
