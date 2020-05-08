 import html_output.ConflictingEventsPage.ConflictingEventsPageFactory;
 import html_output.DayCalendarPage;
 import html_output.DayCalendarPage.DayCalendarPageFactory;
 import html_output.DetailPage;
 import html_output.DetailPage.DetailPageFactory;
 import html_output.HtmlPage;
 import html_output.HtmlPageFactory;
 import html_output.MonthCalendarPage;
 import html_output.MonthCalendarPage.MonthCalendarPageFactory;
 import html_output.SortedEventsPage;
 import html_output.SortedEventsPage.SortedEventsPageFactory;
 import html_output.SummaryPage;
 import html_output.SummaryPage.SummaryPageFactory;
 import html_output.WeekCalendarPage;
 import html_output.WeekCalendarPage.WeekCalendarPageFactory;
 
 
 import org.joda.time.DateTime;
 
 public class Main {
 	
 	public static void main (String args[]) {
 		TivooSystem s = new TivooSystem();
 		//s.loadCal("http://www.cs.duke.edu/courses/cps108/current/assign/02_tivoo/data/dukecal.xml");
 		//s.loadCal("http://www.cs.duke.edu/courses/cps108/current/assign/02_tivoo/data/googlecal.xml");
 		//s.loadCal("http://www.cs.duke.edu/courses/cps108/current/assign/02_tivoo/data/DukeBasketBall.xml");
		s.loadCal("http://www.cs.duke.edu/courses/cps108/current/assign/02_tivoo/data/NFL.xml");
		//s.loadCal("tv.xml");
 		//s.filterByKeyword("a");
 		
 		s.outputHtmlPage(new SummaryPageFactory(), System.getProperty("user.home") + "/Desktop/", new DateTime(2011, 9, 17, 0, 0));
 		s.outputHtmlPage(new DetailPageFactory(), System.getProperty("user.home") + "/Desktop/", new DateTime(2011, 9, 17, 0, 0));
         
 		s.outputHtmlPage(new WeekCalendarPageFactory(), System.getProperty("user.home") + "/Desktop/", new DateTime(2011, 9, 17, 0, 0));
 		
 		//s.outputConflictingEventsPage(System.getProperty("user.home") + "/Desktop/");
 		//s.outputSortedEventsPage(System.getProperty("user.home") + "/Desktop/");
 		//s.outputSummaryAndDetailsPages(System.getProperty("user.home") + "/Desktop/");
 
 		//System.out.println(s.myEvents.toString());
 		//DateTime dt=new DateTime("2011-11-16T13:30:00");
 		//s.filterByTime(dt);
 	}
 }
