 package eu.neq.mais.domain.gnuhealth;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 public class TimeGnuShort {
 
 	private int month, year, day;
 	
 	private String __class__ = "date";
 	
 	public TimeGnuShort(long time){
 		GregorianCalendar cal = new GregorianCalendar();
 		cal.setTimeInMillis(time);
		this.month = cal.get(Calendar.MONTH);
 		this.year = cal.get(Calendar.YEAR);
 		this.day = cal.get(Calendar.DAY_OF_MONTH);
 	}
 
 }
