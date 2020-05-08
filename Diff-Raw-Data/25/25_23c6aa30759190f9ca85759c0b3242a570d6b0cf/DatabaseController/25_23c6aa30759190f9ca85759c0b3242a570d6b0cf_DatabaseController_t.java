 package server.database;
 
 import java.util.ArrayList;
 import java.util.Date;
 import chronos.Person;
 import chronos.Singleton;
 
 import events.AuthEvent;
 import events.CalEvent;
 import events.NetworkEvent.EventType;
 import events.QueryEvent.QueryType;
 import events.QueryEvent;
 
 /**
  * Acts as a link between server and DB
  */
 public class DatabaseController implements DatabaseControllerInterface {
 
 	private DatabaseQueries dbQueries;
 
 	public DatabaseController() {
 		DatabaseConnection dbConnection = new DatabaseConnection();
 		dbConnection.initialize();
 		dbQueries = new DatabaseQueries(dbConnection);
 	}
 
 	@Override
 	public AuthEvent authenticateUser(AuthEvent event) {
 		Singleton.log("Authenticating " + event.getUsername());
 		if(dbQueries.isUsernameAndPassword(event)){
 			event.setAccessGranted(true);		
 			dbQueries.setTimestampOfUser(-1L);
 			event.setSender(dbQueries.getUserByUsername(event.getUsername()));
 
 		}
 		return event;
 	}
 
 	@Override
 	public QueryEvent getAvailableRooms(QueryEvent event) {
 		return null;
 	}
 
 	@Override
 	public QueryEvent getUsers(QueryEvent event) {
 		return new QueryEvent(EventType.QUERY, QueryType.PERSON).setResults(dbQueries.getUsers());
 	}
 
 
 	@Override
 	public void addCalEvent(CalEvent event) {
 		dbQueries.addEvent(event);
 
 	}
 
 	@Override
 	public void updateCalEvent(CalEvent event) {
 		dbQueries.updateCalEvent(event);
 	}
 
 	@Override
 	public void removeCalEvent(CalEvent event, Person person) {
 		dbQueries.removeCalEvent(event);
 
 	}
 	@Override
 	public QueryEvent getNewCalEvents(Person person) {
 		return new QueryEvent(EventType.QUERY, QueryType.CALEVENT).setResults(dbQueries.getEventsByParticipant(person, true));
 	}
 
 	@Override
 	public QueryEvent getConfirmedEvents(Person person) {
 		return new QueryEvent(EventType.QUERY, QueryType.CALEVENT).setResults(dbQueries.getEventsByParticipant(person, true));
 	}
 
 	@Override
 	public void logout(Person person) {
		dbQueries.setTimestampOfUser(new Date().getTime());
 		
 	}
 }
