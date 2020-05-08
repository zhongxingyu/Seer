 package server.database;
 
 import java.util.Date;
 
 import chronos.Person;
 import events.AuthEvent;
 import events.CalEvent;
 import events.QueryEvent;
 
 public interface DatabaseControllerInterface {
 	/**
 	 * Returns a query event with an array of all available rooms in the DB
 	 */
 	public QueryEvent getAvailableRooms(QueryEvent event);
 
 	/**
 	 * Retruns a query event with an array of all users in the DB
 	 */
 	public QueryEvent getUsers(QueryEvent event);
 
 	/**
 	 * Get events the user already accepted and viewed
 	 */
 	public QueryEvent getConfirmedEvents(Person person);
 
 	/**
 	 * Get calendar events added since last login. Acts as a cache.
 	 */
 	public QueryEvent getNewCalEvents(Person person);
 
 	/**
 	 * adds a new calendar event for the person with a "neutral" (IE. waiting,
 	 * person hasn't yet accepted/declined) state
 	 */
 	public void addCalEvent(CalEvent event);
 
 	/**
 	 * Updates a calendar event, IE. the state from "accepted" to "declined"
 	 */
 	public void updateCalEvent(CalEvent event);
 
 	/**
 	 * Removes a calendar event for the specified person
 	 */
 	public void removeCalEvent(CalEvent event, Person person);
 
 	/**
 	 * Used for login.
 	 * 
 	 * @return sets accessGranted true/false and returns event
 	 */
 	public AuthEvent authenticateUser(AuthEvent event);
 
 	/**
 	 * Logout the specified user and set the timestamp for lastLoggedIn
 	 */
 	public void logout(Person person);
 }
