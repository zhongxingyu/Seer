 package watch;
 
 import java.util.Date;
 
 /**
  * The NotificationTimeStamp Class is used to keep track of the number of notifications
  * that have been sent in a certain period of time, and the time of the last one sent.
  */
 
 public class NotificationTimeStamp {
 	private long sentTime;
 	private long notificationCount;
 
 	/**
 	 * @return The last timeStamp
 	 */
 	public long getSentTime() {
 		return sentTime;
 	}
 
 	/**
 	 * @param sentTime
 	 * Set the sentTime to the current time
 	 */
 	public void setSentTime(long sentTime) {
 		this.sentTime = sentTime;
 	}
 
 	/**
 	 * @return number of notifications that have been sent
 	 */
 	public long getNotificationCount() {
 		return notificationCount;
 	}
 
 	/**
 	 * @param sentCount
 	 * Set the number of notifications that have been sent
 	 */
 	public void setSentCount(long sentCount) {
 		this.notificationCount = sentCount;
 	}
 
 	/**
 	 * Create a new NotificationTimeStamp and set the sentTime to current time
 	 */
 	public NotificationTimeStamp() {
 		this.sentTime = new Date().getTime();
 		this.notificationCount = 0;
 	}
 
 	/**
 	 * Increment the count of notifications sent by one
 	 */
 	public void incrementCount() {
 		this.notificationCount += 1;
 	}
 	
 	/**
 	 * Update the sentTime to the current time
	 * Set notificationCount to 0
 	 */
 	public void update(){
 		this.sentTime = new Date().getTime();
		this.notificationCount = 0;
 	}
 
 }
