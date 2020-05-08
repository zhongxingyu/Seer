 package net.brainvitamins.presencepoll.server;
 
 import java.util.Date;
 
 import com.google.appengine.api.users.User;
 
// somehow this data structure needs to be related to a task in the task queue
 public class Checkin extends Activity
 {
 	private User user;
 	public User getUser()
 	{
 		return user;
 	}
 
 	private long timeout;
 	public long getTimeout()
 	{
 		return timeout;
 	}
 
 	private Date timestamp;
 	public Date getTimestamp()
 	{
 		return timestamp;
 	}
 	
 	public Checkin(User user, Date timestamp, long timeout)
 	{
 		this.user = user;
 		this.timestamp = timestamp;
 		this.timeout = timeout;
 	}
 }
