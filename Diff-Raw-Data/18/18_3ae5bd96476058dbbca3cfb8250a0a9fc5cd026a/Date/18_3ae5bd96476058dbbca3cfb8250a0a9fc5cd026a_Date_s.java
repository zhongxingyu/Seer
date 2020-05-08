 package uk.ac.gla.dcs.tp3.w.parser;
 
 public class Date {
 
 	private int day;
 	private int month;
 	private int year;
 
 	public Date() {
 		day = 1;
 		month = 1;
 		year = 2012;
 	}
 
 	public Date(int d, int m, int y) {
 		day = d;
 		month = m;
 		year = y;
 	}
 
 	public int getDay() {
 		return day;
 	}
 
 	public void setDate(int d) {
 		day = d;
 	}
 
 	public int getMonth() {
 		return month;
 	}
 
 	public void setMonth(int m) {
 		month = m;
 	}
 
 	public int getYear() {
 		return year;
 	}
 
 	public void setYear(int y) {
 		year = y;
 	}
 
 	public boolean before(Date date) {
		return year < date.getYear() ? true : beforeMonth(date);
	}

	private boolean beforeMonth(Date date) {
		return month < date.getMonth() ? true : beforeDay(date);
	}

	private boolean beforeDay(Date date) {
		return day < date.getDay();
 	}
 
 	public static void main(String[] args) {
 		Date d1 = new Date(1, 1, 2011);
 		Date d2 = new Date(1, 1, 2011);
 		System.out.println(d1.before(d2));
 	}
 
 }
