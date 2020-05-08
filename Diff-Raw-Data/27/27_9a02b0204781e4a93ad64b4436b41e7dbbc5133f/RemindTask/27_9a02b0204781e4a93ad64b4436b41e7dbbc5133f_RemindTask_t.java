 package core;
 
 
 
 import java.util.TimerTask;
 
import org.apache.log4j.Logger;

 
 import entities.RemindEntity;
 
 public class RemindTask extends TimerTask {
 
     private RemindEntity remind;
     private RemindManager remindManager;
    private Logger logger;
     
    public RemindTask(RemindEntity remind, RemindManager remindManager, Logger logger) {
         this.remind = remind;
         this.remindManager = remindManager;
        this.logger = logger;
     }
     
     
     
     @Override
     public void run() {
        try {
            this.remindManager.deliverRemind(this.remind);
        } catch (Exception e) {
            logger.error("Error while delivering remind", e);
        }
     }
     
 }
