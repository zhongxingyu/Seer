 package eu.neq.mais.domain.gnuhealth;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 public class TimeGnu {
 	
 
 	private int hour, month, second, year, day, minute;
 	
 	private GregorianCalendar cal;
 	
 	private String __class__ = "datetime";
 	
 	public TimeGnu(long time){
 		cal = new GregorianCalendar();
 		cal.setTimeInMillis(time);
 		this.hour = cal.get(Calendar.HOUR_OF_DAY);
		this.month = cal.get(Calendar.MONTH)+1;
 		this.second = cal.get(Calendar.SECOND);
 		this.year = cal.get(Calendar.YEAR);
 		this.day = cal.get(Calendar.DAY_OF_MONTH);
 		this.minute = cal.get(Calendar.MINUTE);
 	}
 
 	
 	public long getTimeInMillis(){
 		if(cal == null){
 			cal = new GregorianCalendar();
 			cal.set(year, month-1, day, hour, minute, second);
 		}
 		
 		return cal.getTimeInMillis();
 	}
 	
 	
 }
