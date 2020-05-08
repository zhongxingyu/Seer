 
 package com.crs.service;
 
import static org.quartz.CronScheduleBuilder.cronSchedule;
 import static org.quartz.JobBuilder.newJob;
 import static org.quartz.TriggerBuilder.newTrigger;
 
 import java.util.Date;
 
 import org.quartz.CronTrigger;
 import org.quartz.JobDetail;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerFactory;
 import org.quartz.SchedulerMetaData;
 import org.quartz.impl.StdSchedulerFactory;
 
import com.crs.dao.CrsDAO;
import com.crs.model.CarPoolMemberForm;

 public class SchedulerService {
 	
 	public SchedulerService(){
 		dao = new CrsDAO();
 	}
 	
 	private CrsDAO dao;
 	
 	/**
 	 * Getter method for CrsDAO instance variable 
 	 * @return CrsDAO
 	 */
 	public CrsDAO getDao() {
 		return dao;
 	}
 
 	/**
 	 * Setter method for CrsDAO
 	 * @param dao
 	 */
 	public void setDao(CrsDAO dao) {
 		this.dao = dao;
 	}
 	
 	//Cancelling pick up request for that day
	public void cancelPickUpRequest(CarPoolMemberForm carPoolMember){
 			System.out.println("Cancelling car pool request");
 			
 	}
 	
 		
 	public void run() throws Exception{
         System.out.println("------- Initializing -------------------");
         // First we must get a reference to a scheduler
         SchedulerFactory sf = new StdSchedulerFactory();
         Scheduler sched = sf.getScheduler();
         
         System.out.println("------- Initialization Complete --------");
         System.out.println("------- Scheduling Jobs ----------------");
         // jobs can be scheduled before sched.start() has been called
         // get a "nice round" time a few seconds in the future...
         // job1 will only fire once at date/time "ts"
         JobDetail job = newJob(EmailService.class).withIdentity("Email job", "Email Service").build();
         CronTrigger trigger =  newTrigger().withIdentity("trigger5", "group1").withSchedule(cronSchedule("0 0 8,17 * * ? * MON-FRI")).build();
             // schedule it to run!
         Date ft = sched.scheduleJob(job, trigger);
             
         System.out.println("------- Starting Scheduler ----------------");
 
             // All of the jobs have been added to the scheduler, but none of the jobs
             // will run until the scheduler has been started
             
             System.out.println(job.getKey() + " has been scheduled to run at: " + ft
                     + " and repeat based on expression: "
                     + trigger.getCronExpression());
             
             sched.start();
             
             System.out.println("------- Started Scheduler -----------------");
             System.out.println("------- Waiting 30 seconds... --------------");
 
             /*try {
                 // wait 30 seconds to show jobs
                 Thread.sleep(30L * 1000L); 
                 // executing...
             } catch (Exception e) {
             }*/
 
 
             // display some stats about the schedule that just ran
             SchedulerMetaData metaData = sched.getMetaData();
             System.out.println("Executed " + metaData.getNumberOfJobsExecuted() + " jobs.");
 	}
 }
