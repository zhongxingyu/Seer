 package com.exadel.borsch.checker;
 
 import com.exadel.borsch.dao.OrderChange;
 import com.exadel.borsch.managers.ManagerFactory;
 import com.exadel.borsch.managers.OrderChangeManager;
 import com.exadel.borsch.util.DateTimeUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 
 /**
  * @author Andrew Zhilka
  */
 public class OrderChangeChecker extends Checker {
     @Autowired
     private ManagerFactory managerFactory;
     private static final int SCHEDULE_HOURS = 1;
 
     @Override
    @Scheduled(cron = "0 0 */2 * * *")
     public void runPeriodCheck() {
         OrderChangeManager changeManager = managerFactory.getChangeManager();
 
         for (OrderChange change : OrderChangesHolder.returnChanges()) {
             changeManager.addNewChange(change);
         }
 
         OrderChangesHolder.resetHolder();
     }
 
     public void resetOldChanges() {
         OrderChangeManager changeManager = managerFactory.getChangeManager();
 
         changeManager.resetOldChanges();
     }
 }
