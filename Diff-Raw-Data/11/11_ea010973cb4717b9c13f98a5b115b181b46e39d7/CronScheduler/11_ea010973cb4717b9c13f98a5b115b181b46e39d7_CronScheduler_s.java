 package org.motechproject.carereporting.scheduler;
 
 import org.apache.log4j.Logger;
 import org.motechproject.carereporting.domain.CronTaskEntity;
 import org.motechproject.carereporting.scheduler.jobs.IndicatorValueCalculatorJob;
 import org.motechproject.carereporting.service.CronService;
 import org.quartz.CronTrigger;
 import org.quartz.JobDataMap;
 import org.quartz.JobDetail;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.Trigger;
 import org.quartz.impl.StdSchedulerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.annotation.PostConstruct;
 import java.text.ParseException;
 import java.util.Set;
 
 public class CronScheduler {
 
     private static final String DEFAULT_GROUP_NAME = "DEFAULT";
 
     @Autowired
     private CronService cronService;
 
     private Scheduler scheduler;
 
     private  static final Logger LOGGER = Logger.getLogger(CronScheduler.class);
 
     @PostConstruct
     public void setupTrigger() throws SchedulerException {
         scheduler = new StdSchedulerFactory().getScheduler();
 
         Set<CronTaskEntity> allCronTasks = cronService.getAllCronTasks();
 
         for (CronTaskEntity cronTaskEntity : allCronTasks) {
             JobDataMap jobDataMap = new JobDataMap();
             jobDataMap.put("frequency", cronTaskEntity.getFrequency());
 
             JobDetail job = new JobDetail();
             job.setName(cronTaskEntity.getFrequency().getFrequencyName());
             job.setJobClass(IndicatorValueCalculatorJob.class);
             job.setJobDataMap(jobDataMap);
 
             Trigger trigger = prepareTrigger(cronTaskEntity);
             scheduler.scheduleJob(job, trigger);
         }
 
         scheduler.start();
     }
 
     public void updateJob(CronTaskEntity cronTaskEntity) {
         Trigger trigger = prepareTrigger(cronTaskEntity);
 
         try {
             scheduler.rescheduleJob(cronTaskEntity.getFrequency().getFrequencyName(), DEFAULT_GROUP_NAME, trigger);
         } catch (SchedulerException e) {
             LOGGER.trace(e.getMessage());
         }
     }
 
     private Trigger prepareTrigger(CronTaskEntity cronTaskEntity) {
         CronTrigger trigger = new CronTrigger();
         trigger.setJobName(cronTaskEntity.getFrequency().getFrequencyName());
         trigger.setName(cronTaskEntity.getFrequency().getFrequencyName());
 
         try {
             trigger.setCronExpression(cronTaskEntity.toString());
         } catch (ParseException e) {
             LOGGER.trace(e.getMessage());
         }
 
         return trigger;
     }
 
     public Scheduler getScheduler() {
         return scheduler;
     }
 
     public void setScheduler(Scheduler scheduler) {
         this.scheduler = scheduler;
     }
 }
