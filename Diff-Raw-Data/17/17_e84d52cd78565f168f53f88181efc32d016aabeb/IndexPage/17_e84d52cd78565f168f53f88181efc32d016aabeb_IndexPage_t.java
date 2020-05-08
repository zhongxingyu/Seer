 package controllers;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.regex.PatternSyntaxException;
 
 import play.mvc.Controller;
 import play.mvc.With;
 import ch.unibe.ese.calendar.CalendarManager;
 import ch.unibe.ese.calendar.EseCalendar;
 import ch.unibe.ese.calendar.User;
 import ch.unibe.ese.calendar.UserManager;
 import ch.unibe.ese.calendar.util.UserComparator;
 
 @With(Secure.class)
 public class IndexPage extends Controller {
 	
 	public static SortedSet<User> foundUsers;
 	
 	public static void index() {
 		String userName = Security.connected();
 		UserManager um = UserManager.getInstance();
 		User connectedUser = um.getUserByName(userName);
 		CalendarManager calendarManager = CalendarManager.getInstance();
 		SortedSet<EseCalendar> connectedUserCalendars = calendarManager.getCalendarsOf(connectedUser);
		Set<User> foundUsers = IndexPage.foundUsers; //no idea why this is necessary
		EseCalendar mainCal = null;
		String token = "";
 		//done this way to display a calendar directly. triedo will probably change this later
		if (connectedUserCalendars.iterator().hasNext()){
			 mainCal = connectedUserCalendars.iterator().next();
			token = "You (" + connectedUser + ") own: " + connectedUserCalendars.size()
 				+ " calendars";
			render(token, connectedUser, mainCal, calendarManager, foundUsers);
		}
		else{
			render(token, connectedUser, mainCal, calendarManager, foundUsers);
		}
 	}
 	
 	public static void searchUser(String searchRegex) {
 		String userName = Security.connected();
 		UserManager um = UserManager.getInstance();
 		User user = um.getUserByName(userName);
 		try {
 			foundUsers = new TreeSet<User>(new UserComparator(user));
 			foundUsers.addAll(UserManager.getInstance().getUserByRegex(searchRegex).values());
 		} catch (PatternSyntaxException e) {
 			//TODO error handling
 			error(e.getMessage());
 		}
 		System.out.println(foundUsers);
 		index();
 	}
 
 	public static void deleteAccount() throws Throwable{
 		UserManager um = UserManager.getInstance();
 		String userName = Security.connected();
 		um.deleteUser(userName);
 		Secure.logout();
 	}
 	
 }
