 package org.pentaho.pac.server;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.pentaho.pac.client.SubscriptionService;
 import org.pentaho.pac.client.scheduler.model.Schedule;
 import org.pentaho.pac.common.SchedulerServiceException;
 import org.pentaho.pac.server.common.AppConfigProperties;
import org.pentaho.pac.server.scheduler.SubscriptionAdminUIComponentProxy;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 public class SubscriptionServiceImpl extends RemoteServiceServlet implements SubscriptionService {
 
   /**
    * 
    */
   private static final long serialVersionUID = 420L;
   private static SubscriptionAdminUIComponentProxy subscriptionProxy = null;
   static {
     String userName = StringUtils.defaultIfEmpty( AppConfigProperties.getInstance().getProperty("pentaho.platform.userName"), System.getProperty("pentaho.platform.userName") ); //$NON-NLS-1$ //$NON-NLS-2$
     String biServerBaseURL = AppConfigProperties.getInstance().getProperty( "biServerBaseURL" ); //$NON-NLS-1$
     subscriptionProxy = new SubscriptionAdminUIComponentProxy( userName, biServerBaseURL );
   }
   
   /**
    *
    * @throws SchedulerServiceException 
    */
   public Map<String,Schedule> getJobNames() throws SchedulerServiceException {
     Map<String,Schedule> subscriptionMap = subscriptionProxy.getSubscriptionSchedules();
     
     return subscriptionMap;
   }
 
   /**
    * query string: schedulerAction=createJob&jobName=PentahoSystemVersionCheck&jobGroup=DEFAULT
    * @throws SchedulerServiceException 
    */
   public void createCronSchedule( String jobName, String jobGroup, String description,
       Date startDate, Date endDate,
       String cronString,
       String actionsList ) throws SchedulerServiceException {
     subscriptionProxy.createCronSchedule( jobName, jobGroup, description,
         startDate, endDate, cronString, actionsList );
   }
 
   public void createRepeatSchedule( String jobName, String jobGroup, String description,
       Date startDate, Date endDate,
       String strRepeatCount, String repeatInterval, 
       String actionsList ) throws SchedulerServiceException {
     subscriptionProxy.createRepeatSchedule( jobName, jobGroup, description,
         startDate, endDate, strRepeatCount, repeatInterval, actionsList );
   
   }
   
   public void updateCronSchedule( String oldJobName, String oldJobGroup, String schedId,
       String jobName, String jobGroup, String description,
       Date startDate, Date endDate,
       String cronString,
       String actionsList ) throws SchedulerServiceException {
     subscriptionProxy.updateCronSchedule( oldJobName, oldJobGroup, schedId, jobName, jobGroup, description,
         startDate, endDate, cronString, actionsList );
     
   }
   
   public void updateRepeatSchedule( String oldJobName, String oldJobGroup, String schedId,
       String jobName, String jobGroup, String description,
       Date startDate, Date endDate,
       String strRepeatCount, String repeatInterval, 
       String actionsList ) throws SchedulerServiceException {
     subscriptionProxy.updateRepeatSchedule( oldJobName, oldJobGroup, schedId, jobName, jobGroup, description,
         startDate, endDate, strRepeatCount, repeatInterval, actionsList );
   }
   
   public void deleteJobs( List<Schedule> scheduleList ) throws SchedulerServiceException {
     subscriptionProxy.deleteJobs( scheduleList );
   }
   
   public void resumeJobs( List<Schedule> scheduleList ) throws SchedulerServiceException {
     subscriptionProxy.resumeJobs( scheduleList );
   }
   
   public void pauseJobs( List<Schedule> scheduleList ) throws SchedulerServiceException {
     subscriptionProxy.pauseJobs( scheduleList );
   }
   
   public void executeJobs( List<Schedule> scheduleList ) throws SchedulerServiceException {
     subscriptionProxy.executeJobs( scheduleList );
   }
 }
