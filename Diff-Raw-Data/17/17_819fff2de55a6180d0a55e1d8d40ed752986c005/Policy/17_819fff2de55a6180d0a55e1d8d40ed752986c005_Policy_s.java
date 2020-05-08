 package ch.unibe.ese.calendar.security;
 
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.SortedSet;
 
 import ch.unibe.ese.calendar.CalendarManager;
 import ch.unibe.ese.calendar.EseCalendar;
 import ch.unibe.ese.calendar.User;
 import ch.unibe.ese.calendar.UserManager;
 import ch.unibe.ese.calendar.impl.EseCalendarImpl;
 
 /**
  * When this policy is in place a subjects with a CalendarPrincipal (i.e. the Subject returned by 
  * <code>User.getSubject</code> has privileged calendar access to the calendar they own.
  */
 public class Policy {
 
 	private static Policy instance = new Policy();
 
 	private Policy() {
 		
 	}
 
 	public boolean hasPermission(User user, Permission permission) {
 		if (User.ADMIN.equals(user)) {
 			return true;
 		}
 		return getUserPermissions(user).contains(permission);
 	}
 
 	private Set<Permission> getUserPermissions(User user) {
 		Set<Permission> result = new HashSet<Permission>();
 		SortedSet<EseCalendar> userCalendars = CalendarManager.getInstance().getCalendarsOf(user);
 		for (EseCalendar cal : userCalendars) {
 			result.add(new PrivilegedCalendarAccessPermission(cal.getName()));
 		}
		for(User u : UserManager.getInstance().getAllUsers()) {
			if (u.getSortedContacts().contains(user)) {
 				for (EseCalendar cal : CalendarManager.getInstance().getCalendarsOf(u)) {
 					result.add(new MyContactAccessPermission(cal.getName()));
 				}
 			}
 		}
 		return result;
 	}
 	
 	public static Policy getInstance() {
 		return instance;
 	}
 
 	public void checkPermission(User user,
 			Permission permission) {
 		if (!hasPermission(user, permission))	{
 			throw new PermissionDeniedException(permission);
 		}
 		
 	}
 
 }
