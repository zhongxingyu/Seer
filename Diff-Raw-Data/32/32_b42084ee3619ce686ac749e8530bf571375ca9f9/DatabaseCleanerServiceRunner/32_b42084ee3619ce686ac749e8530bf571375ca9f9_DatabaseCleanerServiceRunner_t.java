 
 package axirassa.services.runners;
 
 import org.quartz.CronTrigger;
 import org.quartz.JobDataMap;
 import org.quartz.JobDetail;
 import org.quartz.Scheduler;
 import org.quartz.Trigger;
 import org.quartz.impl.StdSchedulerFactory;
 
 import axirassa.util.HibernateTools;
 import axirassa.util.MessagingTools;
 
 public class DatabaseCleanerServiceRunner {
 	public static String JOB_GROUP = "AxDatabaseCleaner";
	public static String DB_CLEAN = "DB_CLEAN";
	public static String FEEDBACK_SEND = "FEEDBACK_SEND";
 
 
 	public static void main(String[] args) throws Exception {
 		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
 		scheduler.start();
 
		JobDetail dbCleanJob = new JobDetail(DB_CLEAN, JOB_GROUP, DatabaseCleanerServiceJob.class);
		Trigger dbCleanTrigger = new CronTrigger(DB_CLEAN, JOB_GROUP, "0 0 * * * ?");
 		scheduler.scheduleJob(dbCleanJob, dbCleanTrigger);
 
 		JobDataMap datamap = new JobDataMap();
 		datamap.put(FeedbackAggregationServiceJob.DATABASE_SESSION, HibernateTools.getLightweightSession());
 		datamap.put(FeedbackAggregationServiceJob.MESSAGING_SESSION, MessagingTools.getEmbeddedSession());
 
		JobDetail feedbackJob = new JobDetail(FEEDBACK_SEND, JOB_GROUP, FeedbackAggregationServiceJob.class);
 		feedbackJob.setJobDataMap(datamap);
 
		Trigger feedbackTrigger = new CronTrigger(FEEDBACK_SEND, JOB_GROUP, "0 0/1 * * * ?");
 		scheduler.scheduleJob(feedbackJob, feedbackTrigger);
 
 		return;
 	}
 }
