 package uk.ac.gla.dcs.tp3.w.league;
 
 public class DateTime extends Date {
 
 	private int hour;
 	private int min;
 
 	public DateTime(int d, int m, int y, int h, int min) {
 		super(d, m, y);
 		if (!validateTime(h, min)) {
 			System.out.println("Erroneous time.");
 			return;
 		}
 		hour = h;
 		this.min = min;
 	}
 
 	public DateTime(int d, String m, int y, int h, int min) {
 		super(d, m, y);
 		if (!validateTime(h, min)) {
 			System.out.println("Erroneous time.");
 			return;
 		}
 		hour = h;
 		this.min = min;
 	}
 	
 	public DateTime(Date D, int h, int m){
 		super(D.getDay(), D.getMonth(), D.getYear());
 		if (!validateTime(h, min)) {
 			System.out.println("Erroneous time.");
 			return;
 		}
 		hour = h;
		this.min = min;
 	}
 
 	public int getHour() {
 		return hour;
 	}
 
 	public int getMinute() {
 		return min;
 	}
 
 	private boolean validateTime(int h, int m) {
 		return (0 <= h && h < 24 && 0 <= m && h < 60);
 	}
 
 	public boolean before(DateTime DT) {
 		if (this.getYear() < DT.getYear())
 			return true;
 		else if (this.getYear() == DT.getYear())
 			if (this.getMonth() < DT.getMonth())
 				return true;
 			else if (this.getMonth() == DT.getMonth())
 				if (this.getDay() < DT.getDay())
 					return true;
 				else if (this.getHour() == DT.getHour())
 					if (this.getMinute() < DT.getMinute())
 						return true;
 		return false;
 	}
 }
