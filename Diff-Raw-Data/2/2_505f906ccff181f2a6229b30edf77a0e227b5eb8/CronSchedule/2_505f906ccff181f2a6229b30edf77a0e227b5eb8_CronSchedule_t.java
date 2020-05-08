 package org.vcs.medmanage;
 
 import org.quartz.CronScheduleBuilder;
 import org.quartz.TriggerBuilder;
 import org.quartz.TriggerUtils;
 import org.quartz.impl.calendar.BaseCalendar;
 import org.quartz.spi.OperableTrigger;
 
 import java.util.Date;
 import java.util.List;
 
 public class CronSchedule {
 
     private String cronExpression;
 
     public CronSchedule(String cronExpression) {
         this.cronExpression = cronExpression;
     }
 
     public List<Date> getTimings(Date startDate, Date endDate) {
         OperableTrigger trigger = (OperableTrigger) TriggerBuilder
                 .newTrigger()
                 .withIdentity("trigger1", "group1")
                 .withSchedule(
                        CronScheduleBuilder.cronSchedule(this.cronExpression))
                 .build();
 
         return TriggerUtils.computeFireTimesBetween(trigger, new BaseCalendar(), startDate, endDate);
     }
 }
