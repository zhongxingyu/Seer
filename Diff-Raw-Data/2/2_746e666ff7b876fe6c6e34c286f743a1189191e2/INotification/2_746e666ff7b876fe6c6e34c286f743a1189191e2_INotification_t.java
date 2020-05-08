 package org.smartsnip.core;
 
 /**
  * This class represents the access to a notification
  * 
  * @author phoenix
  * 
  */
 public interface INotification {
 	/**
 	 * Marks the notification as read
 	 */
 	public void markRead();
 
 	/**
 	 * Marks the notification as unread
 	 */
 	public void markUnread();
 
 	/**
 	 * Gets the read status of the notification
 	 * 
 	 * @return true if read, false if unread
 	 */
 	public boolean isRead();
 
 	/**
 	 * Gets the sender of the message.
 	 * 
 	 * @return the sender of the notification message
 	 */
 	public String getSource();
 
 	/**
	 * @return the notification message text
 	 */
 	public String getMessage();
 
 	/**
 	 * @return the send time of the notification message
 	 */
 	public String getTime();
 
 	/**
 	 * Deletes the notification message
 	 */
 	public void delete();
 }
