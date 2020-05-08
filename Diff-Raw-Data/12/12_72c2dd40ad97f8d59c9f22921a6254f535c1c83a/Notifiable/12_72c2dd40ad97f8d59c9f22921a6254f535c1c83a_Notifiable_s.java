 /**
 *	Notifiable.java
 *
 *	Interface for describing classes that may be used in
 *	notifications on a specific date.
 *
 *	@author Johan Brook
 *	@copyright (c) 2012 Johan Brook, Robin Andersson, Lisa Stenberg, Mattias Henriksson
 *	@license MIT
 */
 
 package se.chalmers.watchme.notifications;
 
 public interface Notifiable {
 	
 	/**
 	 * The notification id for this object.
 	 * 
 	 * Must be unique within the system (suggestion is to use hashCode()).
 	 * 
 	 * @return An id
 	 */
 	public int getNotificationId();
 	
 	/**
 	 * Get the date in milliseconds. Used to set the timestamp
 	 * of the notification.
 	 * 
 	 * @return The date to trigger
 	 */
 	public long getDateInMilliSeconds();
 	
 	/**
	 * The string representation of this notifiable object.
 	 * 
 	 * @return The title to represent in the notification
 	 */
	public String toString();
 }
