 package ee.alkohol.juks.sirvid.containers.graphics;
 
 import java.util.Calendar;
import java.util.Date;
 import java.util.GregorianCalendar;
 
 public class SirvidDay {
 	
	public Date date;
 	public int weekDay;
 	public int beginX;
 	
 	public SirvidDay(GregorianCalendar day, int beginX) {
		this.date = day.getTime();
 		this.beginX = beginX;
		this.weekDay = day.get(Calendar.DAY_OF_WEEK) - 1;
 	}
 	
 }
