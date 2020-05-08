 package infodoc.basic.scheduling;
 
 import infodoc.basic.activity.CreateAndScheduleCreate;
 import infodoc.core.container.InfodocContainerFactory;
 import infodoc.core.dto.Activity;
 import infodoc.core.dto.Case;
 import infodoc.core.ui.activity.ActivityExecutorHelper;
 
 import java.text.ParseException;
 import java.util.List;
 
 import org.quartz.JobBuilder;
 import org.quartz.JobDetail;
 import org.quartz.JobKey;
 import org.quartz.SchedulerException;
 import org.quartz.impl.triggers.CronTriggerImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import enterpriseapp.EnterpriseApplication;
 
 public class CreateActivityScheduler {
 	
 	private static final Logger logger = LoggerFactory.getLogger(CreateActivityScheduler.class);
 	
 	public static void schedulePending() {
 		try {
 			List<Activity> activities = InfodocContainerFactory.getActivityContainer().listAll();
 		
 			for(Activity activity : activities) {
 				Class<?> clazz = Class.forName(activity.getJavaClass());
 				
 				if(CreateAndScheduleCreate.class.isAssignableFrom(clazz)) {
 					
 					CreateAndScheduleCreate createAndScheduleActivity = (CreateAndScheduleCreate) ActivityExecutorHelper.getActivityExecutorComponent(activity, null);
 					createAndScheduleActivity.parseParams();
 					
 					List<Case> cases = InfodocContainerFactory.getCaseContainer().findByLastActivityId(activity.getId());
 					
 					for(Case caseDto : cases) {
 						String cronExpression = createAndScheduleActivity.getCronExpression(caseDto.getPropertyValues());
 						Long scheduleActivityId = createAndScheduleActivity.getScheduleActivityId();
						schedule(caseDto.getId(), scheduleActivityId, cronExpression, createAndScheduleActivity.getUser().getId());
 					}
 					
 				}
 			}
 			
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
	public static void schedule(Long caseId, Long scheduleActivityId, String cronExpression, Long userId) {
 		try {
 			logger.info("Scheduling job for case " + caseId + " and activity " + scheduleActivityId + " (" + cronExpression + ")");
 			
 			JobKey key = new JobKey(CreateActivityExecutionJob.class.getSimpleName() + "_" + caseId + "_" + scheduleActivityId);
 			JobDetail jobDetail = JobBuilder.newJob(CreateActivityExecutionJob.class)
 					.withIdentity(key)
 					.usingJobData("activityId", scheduleActivityId)
 					.usingJobData("caseId", caseId)
					.usingJobData("userId", userId)
 					.build();
 			
 			CronTriggerImpl trigger = new CronTriggerImpl();
 			trigger.setName(CreateActivityScheduler.class.getSimpleName() + "_" + caseId + "_" + scheduleActivityId);
 			trigger.setCronExpression(cronExpression);
 		
 			EnterpriseApplication.getScheduler().scheduleJob(jobDetail, trigger);
 			
 		} catch (SchedulerException e) {
 			throw new RuntimeException(e);
 		} catch (ParseException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public static void unschedule(Long caseId, Long scheduleActivityId) {
 		try {
 			logger.info("Unscheduling job for case " + caseId + " and activity " + scheduleActivityId);
 			JobKey key = new JobKey(CreateActivityExecutionJob.class.getSimpleName() + "_" + caseId + "_" + scheduleActivityId);
 			EnterpriseApplication.getScheduler().deleteJob(key);
 			
 		} catch (SchedulerException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 }
