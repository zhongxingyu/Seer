 package edu.ycp.cs320.chronos.server;
 
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import edu.ycp.cs320.chronos.shared.Account;
 import edu.ycp.cs320.chronos.shared.Event;
 import edu.ycp.cs320.chronos.shared.EventInvitation;
 import edu.ycp.cs320.chronos.shared.IDatabase;
 
 public class FakeDatabase implements IDatabase {
 	private Map<String, Event> nameToEventMap;
 	private Map<String, Account> accountMap;
 	//Use Maps to organize events to accounts
 	
 	/*Each key(event) will have the value of which user is the author
 	 * Every time an event is created, this Hashmap MUST be updated to insure
 	 * proper data retrieval from the FakeDatabase		
 	*/
 	private int accountIDCount;	//Handles current account id when making a new account
 	private int eventIDCount;	//Handles current event id when making a new Event
 	private ArrayList<Account> accountList;
 	private ArrayList<Event> eventList;
 	private ArrayList<EventInvitation> eventInvitationList;
 	
 	public FakeDatabase() {
 		accountIDCount = 0;
 		eventList = new ArrayList<Event>();
 		accountList = new ArrayList<Account>();
 		
 		//Create test accounts
 		createAccount("Spongebob", "Squarepants", "x@y.z");
 		createAccount("Patric", "Star", "a@b.c");
 		createAccount("Sandy", "Cheeks", "q@r.s");
 		//Create test events to work with
 		createEvent(getAccount("Spongebob").getID(), "Christmas", 12, 25, 2013, 1200, 2400, "Christmas");
 		createEvent(getAccount("Spongebob").getID(), "New Years", 1, 1, 2014, 1200, 2400, "New Years day!");
 		createEvent(getAccount("Spongebob").getID(), "Thanksgiving", 11, 28, 2013, 1200, 2400, "turkey turkey turkey");
 		
 	}
 
 	//@Override
 	/**
 	 * Methods for handling event info.
 	 * 
 	 */
 
 	/**
 	 * Sifts through an arraylist of today's events and returns the Event that will occur next
 	 * 
 	 *            Note: Replace current method of doing this with recursion
 	 */
 	public Event getNextEvent(Account user, int month, int day, int year){
 		String usr = user.getUserName();
 		ArrayList<Event> events = user.getTodaysEvents(month, day, year);
 		Event nextEvent = events.get(0); //Set the "nextEvent] to the first Event in the arrayList "events"
 		//Sift through the array list to find the next coming event
 		for(int i = 1; i < events.size(); i++){
 			//if the current "nextEvent"'s start time is greater, it is not the next event 
 			if(nextEvent.getStartTime() > events.get(i).getStartTime()){
 				nextEvent = events.get(i);
 			}
 		}
 		return nextEvent;
 	}
 	
 	
 	/**
 	 * Uses the given account id (UserID) to sift through the database's
 	 * arrayList of events and collects all user's events in an arrayList
 	 * @param userID
 	 * @return userEvents
 	 */
 	public ArrayList<Event> getAccountEvents(int userID){
 		ArrayList<Event> userEvents = new ArrayList<Event>();
 		for(int i = 0; i < eventList.size(); i++){
 			if(eventList.get(i).getOwnerID() == userID){
 				userEvents.add(eventList.get(i));
 			}
 			
 		}
 		return userEvents;		
 	}
 		
 	/**
 	 * Uses the given int eventID to return
 	 * the specified event object. Returns null if
 	 * the event cannot be found.	
 	 * @param eventID
 	 * @return
 	 */
 	public Event findEvent(int eventID) {
 		for(int i = 0; i < eventList.size(); i++){
 			if(eventList.get(i).getID() == eventID){
 				return eventList.get(i);
 			}
 		}
 		return null;
 	}
 	public int getMonth(int eventID){
 		return findEvent(eventID).getMonth();
 	}
 	public int getDay(int eventID){
 		return findEvent(eventID).getDay();
 	}
 	public int getYear(int eventID){
 		return findEvent(eventID).getYear();
 	}
 	public int getStartTime(int eventID){
 		return findEvent(eventID).getStartTime();
 	}
 	public int getEndTime(int eventID){
 		return findEvent(eventID).getEndTime();
 	}
 	public String getDetails(int eventID){
 		return findEvent(eventID).getDetails();
 	}
 	
 	/**
 	 * Create an event using the specified values.
 	 * Add the new event to eventList (ArrayList<Event>)
 	 * @param ownerID	Account ID of event's creator
 	 * @param eventName
 	 * @param month
 	 * @param day
 	 * @param year
 	 * @param startTime
 	 * @param endTime
 	 * @param details
 	 */
 	public void createEvent(int ownerID, String eventName, int month, int day, int year, int startTime, int endTime, String details){
 		Event e = new Event(eventIDCount, ownerID, month, day, year, startTime, endTime, details, eventName);
 		eventIDCount++;
 		eventList.add(e);
 	}
 	/*The below function may be useless with newly organized database
 	 */
 	public boolean isDupEvent(String eventName){
 		if(!nameToEventMap.containsKey(eventName)){
 			return false;
 		}
 		else{
 			return true;
 		}
 	}
 	
 	/**
 	 * Removes specified event from "nameToEventMap"
 	 * @param eventName
 	 */
 	public void removeEvent(String eventName){
 		nameToEventMap.remove(eventName);
 	}
 	
 	/**
 	 * Methods for handling Account info.
 	 */
 	
 	/**
 	 * Uses the user inputed values to
 	 * create an new Account and add it to 
 	 * accountList.Uses the database's accountIDCount value to associate the new
 	 * account with a unique ID. Updates accountIDCount
 	 * @param usr
 	 * @param password
 	 * @param email
 	 */
 	public void createAccount(String usr, String password, String email){
 		System.out.println("Creating account for user=" + usr + ", pass=" + password);
 		Account a = new Account(accountIDCount, usr, password, email);
 		accountIDCount++;
 		System.out.println("Account password: " + a.getPassword());
 		accountMap.put(usr, a);	
 	}
 	/**
 	 * Uses the given accountID
 	 * to remove an Account from accountList
 	 * @param accountID
 	 */
 	public void removeAccount(int accountID){
 		for(int i = 0; i < accountList.size(); i++){
 			if(accountList.get(i).getID() == accountID){
 				accountList.remove(i);				
 			}
 			
 		}
 	}
 	/**
 	 * Uses the given String username to find
 	 * the specified account
 	 * @param username
 	 */
 	public Account getAccount(String username){
 		for(int i = 0; i < accountList.size(); i++){
 			if(accountList.get(i).getUserName().compareTo(username) == 0){
 				return accountList.get(i);
 			}
 		}
 		return null;
 	}
 	/**
 	 * Method for verifying account username and password.
 	 * @param usr
 	 * @param password
 	 * @return true if password matches with the account; false otherwise
 	 */
 	public boolean verifyAccount(String usr, String password){
		Account account = accountMap.get(usr);
		if (account == null) {
			System.out.println("No such account: " + usr);
			return false;
		}
		if(account.getPassword().equals(password)){
			return true;
 		}
 		else{
			System.out.println("Password does not match");
 			return false;
 		}
 	}
 	
 	public boolean isDupAccount(String account){
 		if(!accountMap.containsKey(account)){
 			return false;
 		}
 		else{ 
 			return true;
 		}
 	}
 	
 
 }
