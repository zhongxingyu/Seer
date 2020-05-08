 package net.brainvitamins.presencepoll.server;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.taskqueue.Queue;
 import com.google.appengine.api.taskqueue.TaskHandle;
 import com.google.appengine.api.taskqueue.TaskOptions;
 import com.google.appengine.api.users.User;
 
/*
 * A service designed to log and record user activity to a datastore.
 */
 public class ActivityService
 {
 	private DatastoreService datastore;
 
 	public DatastoreService getDatastore()
 	{
 		return datastore;
 	}
 
 	private Queue queue;
 
 	public Queue getQueue()
 	{
 		return queue;
 	}
 
 	private String activityKindIdentifier;
 
 	public String getActivityKindIdentifier()
 	{
 		return activityKindIdentifier;
 	}
 
 	private SimpleDateFormat dateFormat;
 
 	public ActivityService(DatastoreService datastore, Queue queue,
 			String activityKindIdentifier)
 	{
 		this.datastore = datastore;
 		this.queue = queue;
 		this.activityKindIdentifier = activityKindIdentifier;
 
 		dateFormat = new SimpleDateFormat(Constants.DATEFORMAT);
 		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
 	}
 
 	// returns an activity log sorted in descending order by time.
 	public List<Activity> getActivityLog(User user, int sizeLimit)
 	{
 		Key activityStoreKey = KeyFactory.createKey(activityKindIdentifier,
 				user.getUserId().toString());
 
 		Query query = new Query(activityKindIdentifier, activityStoreKey)
 				.addSort("time", Query.SortDirection.DESCENDING);
 
 		List<Entity> activityEntries = datastore.prepare(query).asList(
 				FetchOptions.Builder.withLimit(sizeLimit));
 
 		List<Activity> activityLog = new ArrayList<Activity>();
 
 		for (Entity entity : activityEntries)
 		{
 			Activity activity = reconstituteActivity(user, entity);
 			if (activity != null) activityLog.add(activity);
 		}
 
 		return activityLog;
 	}
 
 	private Activity reconstituteActivity(User user, Entity entity)
 	{
 		if (entity.getProperty("type").equals("checkin"))
 		{
 			Date timestamp = null;
 			try
 			{
 				timestamp = dateFormat.parse((String) entity
 						.getProperty("time"));
 			}
 			catch (ParseException e)
 			{
 				System.out.println("Error parsing date.");
 				return null;
 			}
 
 			return new Checkin(user, timestamp,
 					(Long) entity.getProperty("timeout"));
 		}
 		else if (entity.getProperty("type").equals("timeout"))
 		{
 			Date timestamp = null;
 			Date startTime = null;
 			try
 			{
 				timestamp = dateFormat.parse((String) entity
 						.getProperty("time"));
 				startTime = dateFormat.parse((String) entity
 						.getProperty("startTime"));
 			}
 			catch (ParseException e)
 			{
 				System.out.println("Error parsing date.");
 				return null;
 			}
 
 			return new Timeout(timestamp, (Long) entity.getProperty("timeout"),
 					startTime, (String) entity.getProperty("userId"),
 					(String) entity.getProperty("userEmail"));
 		}
 		else
 			return null;
 	}
 
 	public void logActivity(Checkin checkin)
 	{
 		User user = checkin.getUser();
 		Date timestamp = checkin.getTimestamp();
 		long timeout = checkin.getTimeout();
 
 		Key activityStoreKey = KeyFactory.createKey(activityKindIdentifier,
 				checkin.getUser().getUserId().toString());
 		Entity entry = new Entity(activityKindIdentifier, activityStoreKey);
 
 		String time = dateFormat.format(timestamp);
 
 		entry.setProperty("type", "checkin");
 		entry.setProperty("time", time);
 		entry.setProperty("timeout", timeout);
 
 		// cancel active timeout
 		queue.deleteTask(user.getUserId());
 
 		// TODO: verify the task was actually deleted.
 
 		TaskOptions taskOptions = TaskOptions.Builder
 				.withUrl("/presence_poll/timeout").countdownMillis(timeout)
 				.param("userId", user.getUserId())
 				.param("userEmail", user.getEmail()).param("startTime", time)
 				.param("timeout", Long.toString(timeout))
 				.taskName(user.getUserId());
 		TaskHandle taskHandle = queue.add(taskOptions);
 
 		entry.setProperty("taskName", taskHandle.getName());
 
 		datastore.put(entry);
 	}
 
 	public void logActivity(Timeout timeout)
 	{
 		Key activityStoreKey = KeyFactory.createKey(activityKindIdentifier,
 				timeout.getUserId());
 
 		Entity timeoutEntry = new Entity(activityKindIdentifier,
 				activityStoreKey);
 
 		timeoutEntry.setProperty("type", "timeout");
 		timeoutEntry.setProperty("time",
 				dateFormat.format(timeout.getTimestamp()));
 		timeoutEntry.setProperty("startTime",
 				dateFormat.format(timeout.getStartTime()));
 		timeoutEntry.setProperty("timeout", timeout.getTimeout());
 
 		datastore.put(timeoutEntry);
 
 		System.out.println("Timeout! " + timeout.getUserEmail() + "|"
 				+ timeout.getStartTime() + "|" + timeout.getTimeout());
 	}
 }
