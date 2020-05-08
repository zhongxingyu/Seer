 package com.stackframe.sarariman;
 
 import java.sql.Date;
 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author mcculley
  */
 public class WeeknightTask extends TimerTask {
 
     private final Sarariman sarariman;
     private final Directory directory;
     private final EmailDispatcher emailDispatcher;
     private final Logger logger = Logger.getLogger(getClass().getName());
 
     public WeeknightTask(Sarariman sarariman, Directory directory, EmailDispatcher emailDispatcher) {
         this.sarariman = sarariman;
         this.directory = directory;
         this.emailDispatcher = emailDispatcher;
     }
 
     public void run() {
         Calendar today = Calendar.getInstance();
         int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
         if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
             java.util.Date todayDate = today.getTime();
             try {
                 Date week = new Date(DateUtils.weekStart(todayDate).getTime());
                 for (Employee employee : directory.getByUserName().values()) {
                    Timesheet timesheet = new Timesheet(sarariman, employee.getNumber(), week);
                    if (!timesheet.isSubmitted()) {
                         if (dayOfWeek == Calendar.FRIDAY) {
                            emailDispatcher.send(employee.getEmail(), Collections.singleton(sarariman.getApprover().getEmail()),
                                    "timesheet", "Please submit your timesheet for the week of " + week + ".");
                         } else {
                             double hoursRecorded = timesheet.getHours(new Date(todayDate.getTime()));
                            if (hoursRecorded == 0.0 && employee.isFulltime()) {
                                 emailDispatcher.send(employee.getEmail(), Collections.singleton(sarariman.getApprover().getEmail()),
                                         "timesheet", "Please record your time if you worked today.");
                             }
                         }
                     }
                 }
             } catch (SQLException se) {
                 logger.log(Level.SEVERE, "could not get hours for " + today, se);
             }
         }
     }
 
 }
