 
 package axirassa.services.runners;
 
 import org.hibernate.Session;
 import org.hornetq.api.core.client.ClientSession;
import org.quartz.Job;
 import org.quartz.JobDataMap;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 
 import axirassa.services.FeedbackAggregationService;
 import axirassa.services.Service;
 
public class FeedbackAggregationServiceJob implements Job {
 
 	public static final String DATABASE_SESSION = "session.database";
 	public static final String MESSAGING_SESSION = "session.messaging";
 
 
	@Override
 	public void execute(JobExecutionContext context) throws JobExecutionException {
 		try {
 			JobDataMap datamap = context.getJobDetail().getJobDataMap();
 
 			ClientSession messaging = (ClientSession) datamap.get(MESSAGING_SESSION);
 			Session database = (Session) datamap.get(DATABASE_SESSION);
 
 			Service service = new FeedbackAggregationService(database, messaging);
 			service.execute();
 		} catch (Exception e) {
 			throw new JobExecutionException(e);
 		}
 	}
 }
