 package booking;
 
 public class TimePoint {
 	 
 	public static final int MONDAY 		= 0;
 	public static final int TUESDAY 	= 1;
 	public static final int WEDNESDAY 	= 2;
 	public static final int THURSDAY	= 3;
 	public static final int FRIDAY 		= 4;
 	public static final int SATURDAY 	= 5;
 	public static final int SUNDAY 		= 6;
 	
 	private int date;
 	private int hour;
 	private int min;
 	
 	public TimePoint(int date, int hour, int min) {
 		this.date = date;
 		this.hour = hour;
 		this.min  = min;
 	}
 	
 	public TimePoint(TimePoint tPoint, Duration interval) {
 		this.date = tPoint.getDate() + interval.getDay();
 		this.hour = tPoint.getHour() + interval.getHour();
 		this.min = tPoint.getMin() + interval.getMin();
 		if(this.min >= 60) {
 			this.min -= 60;
 			this.hour++;
 		}
 		if(this.hour > 24) {
 			this.hour -= 24;
 			this.date++;
 		}
 		if(this.date > TimePoint.SUNDAY) {
 			this.date = TimePoint.SUNDAY;
 			this.hour = 24;
 			this.min = 0;
 		}
 	}
 	
 	// comparison method
 	public int compareTime(TimePoint tp) {
 		if(this.date < tp.getDate()) 
 			return -1;
 		else if(this.date > tp.getDate()) 
 			return +1;
 		else if(this.hour < tp.getHour())
 			return -1;
 		else if(this.hour > tp.getHour())
 			return +1;
 		else if(this.min < tp.getMin())
 			return -1;
 		else if(this.min > tp.getMin())
 			return +1;
 		return 0;
 	}
 	// get and set method
 	public int getDate() {
 		return this.date;
 	}
 	
 	public int getHour() {
 		return this.hour;
 	}
 	
 	public int getMin() {
 		return this.min;
 	}
 	
 	public String toString() {
 		String printStr = "";
 		switch(this.date) {
 		case TimePoint.MONDAY:
 			printStr +="Monday, ";
 			break;
 		case TimePoint.TUESDAY:
 			printStr +="TUESDAY, ";
 			break;
 		case TimePoint.WEDNESDAY:
 			printStr +="WEDNESDAY, ";
 			break;
 		case TimePoint.THURSDAY:
 			printStr +="THURSDAY, ";
 			break;
 		case TimePoint.FRIDAY:
 			printStr +="FRIDAY, ";
 			break;
 		case TimePoint.SATURDAY:
 			printStr +="SATURDAY, ";
 			break;
 		case TimePoint.SUNDAY:
 			printStr +="SUNDAY, ";
 			break;
 		}
 		printStr += this.hour + "hour(s), " + this.min + "min(s)";
 		return printStr;
 	}
 	
 	public static void main(String [] args) {
 		
//		TimePoint tp1 = new TimePoint(TimePoint.MONDAY, 14, 59);
//		Duration dr = new Duration(0, 0, 1);
//		TimePoint tp2 = new TimePoint(tp1, dr);
//		System.out.println(tp2.toString());
//		System.out.println(tp1.compareTime(new TimePoint(TimePoint.MONDAY, 15,0)));
//		BookingSlot slot = new BookingSlot(tp1, dr);
		System.out.println("HI"); 
 	}
 }
 
