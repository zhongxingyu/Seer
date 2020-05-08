 package jobs;
 
 import java.text.ParseException;
 import java.util.Date;
 
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 import ch.unibe.ese.calendar.CalendarManager;
 import ch.unibe.ese.calendar.EseCalendar;
 import ch.unibe.ese.calendar.User;
 import ch.unibe.ese.calendar.UserManager;
 import ch.unibe.ese.calendar.Visibility;
 import ch.unibe.ese.calendar.EventSeries.Repetition;
 import ch.unibe.ese.calendar.User.DetailedProfileVisibility;
 import ch.unibe.ese.calendar.exceptions.CalendarAlreadyExistsException;
 import ch.unibe.ese.calendar.impl.CalendarManagerImpl;
 import ch.unibe.ese.calendar.impl.UserManagerImpl;
 import ch.unibe.ese.calendar.util.EseDateFormat;
 
 @OnApplicationStart
 public class Bootstrap extends Job {
 
 	public void doJob() {
 		UserManager um = UserManagerImpl.getInstance();
 		final CalendarManager cm = CalendarManagerImpl.getInstance();
 		try {
 			createSomeCalendars(um, cm);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void createSomeCalendars(UserManager um, final CalendarManager cm) throws ParseException {
 		
 		Date aaronBirthday = EseDateFormat.getInstance().parse("13.05.1966 00:00");
 		User aaron = um.createUser("aaron", "ese", aaronBirthday, DetailedProfileVisibility.PUBLIC);
 		
 		EseCalendar aaroncal;
 		try {
 			aaroncal = cm.createCalendar(aaron, "Aarons Calendar");
 			cm.createCalendar(aaron, "Aarons secondary Calendar");
 		} catch (CalendarAlreadyExistsException e) {
 			aaroncal = cm.getCalendar("Aarons Calendar");
 		}
 		java.util.Calendar juc = java.util.Calendar.getInstance();
 		juc.set(2011, 10, 23, 20, 15);
 		Date start = juc.getTime();
 		juc.set(2011, 10, 23, 23, 00);
 		Date end = juc.getTime();
 		aaroncal.addEvent(User.ADMIN, start, end, "Toller Film", Visibility.PUBLIC, "der Film ist wirklich super");
 		
 		juc.set(2011, 11, 23, 20, 15);
 		start = juc.getTime();
 		juc.set(2011, 11, 23, 23, 00);
 		end = juc.getTime();
 		aaroncal.addEvent(User.ADMIN, start, end, "Tolle Party", Visibility.PUBLIC, "Die Fetzen werden fliegen");
 		
 		juc.set(2011, 12, 23, 20, 15);
 		start = juc.getTime();
 		juc.set(2011, 12, 24, 04, 00);
 		end = juc.getTime();
 		aaroncal.addEvent(User.ADMIN, start, end, "MOAR PARTY!", Visibility.PUBLIC,"random Kommentar1");
 		
 		juc.set(2011, 10, 7, 12, 00);
 		start = juc.getTime();
 		juc.set(2011, 10, 7, 13, 00);
 		end = juc.getTime();
 		aaroncal.addEventSeries(User.ADMIN, start, end, "Mondays lunch meeting", 
 				Visibility.PUBLIC, Repetition.WEEKLY, "with my mates");
 
 		Date judithBirthday = EseDateFormat.getInstance().parse("10.06.1985 00:00");
 		User judith = um.createUser("judith", "ese", judithBirthday, DetailedProfileVisibility.PUBLIC);
 
 		EseCalendar judithcal;
 		try {
 			judithcal = cm.createCalendar(judith, "Judiths Calendar");
 		} catch (CalendarAlreadyExistsException e) {
 			judithcal = cm.getCalendar("Judiths Calendar");
 		}
 		juc.set(2011, 11, 23, 22, 15);
 		start = juc.getTime();
 		juc.set(2011, 11, 23, 23, 00);
 		end = juc.getTime();
 		judithcal.addEvent(User.ADMIN, start, end, "Movienight", Visibility.PUBLIC, "random Kommentar2");
 
 		Date erwannBirthday = EseDateFormat.getInstance().parse("12.02.1832 00:00");
 		User erwann = um.createUser("erwann", "ese", erwannBirthday, DetailedProfileVisibility.PRIVATE);
 
 		EseCalendar erwanncal;
 		try {
 			erwanncal = cm.createCalendar(erwann, "Erwanns Calendar");
 		} catch (CalendarAlreadyExistsException e) {
 			erwanncal = cm.getCalendar("Erwanns Calendar");
 		}
 		juc.set(2011, 11, 21, 20, 15);
 		start = juc.getTime();
 		juc.set(2011, 11, 21, 23, 00);
 		end = juc.getTime();
 		erwanncal.addEvent(User.ADMIN, start, end, "Standardlager", Visibility.PUBLIC, "random Kommentar3");
 		
 		
 		juc.set(2011, 11, 3, 20, 15);
 		start = juc.getTime();
 		juc.set(2011, 11, 4, 5, 00);
 		end = juc.getTime();
 		erwanncal.addEventSeries(User.ADMIN, start, end, "Full moon", 
				Visibility.PUBLIC, Repetition.MONTHLY, "careful, werewolves might appear");
 
 		erwann.addToMyContacts(judith);
 		erwann.addToMyContacts(aaron);
 		aaron.addToMyContacts(erwann);
 	}
 }
