 package no.fll.schedule;
 
 
 public class TimeIncrementor {
 	
 	private int hour;
 	private int min;
 	private int increment;
 
 	public TimeIncrementor(String start, int inc) {
 		hour = Integer.parseInt(start.substring(0, 2));
 		min = Integer.parseInt(start.substring(3, 5));
 		increment = inc;
 	}
 
 	public String getNextValue() {
 		String nextValue = String.format("%02d:%02d", hour, min);
 		min += increment;
		hour += min / 60;
		min %= 60;
 		return nextValue;
 	}
 }
