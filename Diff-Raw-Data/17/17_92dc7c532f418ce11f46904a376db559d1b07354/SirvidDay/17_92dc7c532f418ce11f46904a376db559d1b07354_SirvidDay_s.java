 package ee.alkohol.juks.sirvid.containers.graphics;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 public class SirvidDay {
 	
	public GregorianCalendar day;
 	public int weekDay;
 	public int beginX;
 	
 	public SirvidDay(GregorianCalendar day, int beginX) {
		this.day = day;
 		this.beginX = beginX;
		this.weekDay = this.day.get(Calendar.DAY_OF_WEEK) - 1;
 	}
 	
 }
