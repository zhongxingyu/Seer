 package ch.unibe.ese.calendar;
 
 import java.util.Iterator;
 
 /**
  * Used to indicate the visibility of an event
  * 
  */
 public enum Visibility {
 	/**
 	 * The event is private, only a user with PrivilegedAccess to the calendar can see it.
 	 */
	PRIVATE,
 	/**
 	 * The event is public, everybody can see it.
 	 */
	PUBLIC,
 	/**
 	 * Without privileged calendar access one can only seen the time span of an event 
 	 * to be busy but no other event details.
 	 */
	BUSY,
 	/**
 	 * The event is visible for users that are in myContacts. All other users see the 
 	 * event as busy.
 	 */
	CONTACTSONLY;
 }
