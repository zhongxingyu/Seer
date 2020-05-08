 package generator;
 public class Period implements Comparable<Period> {
 	
 	public static final int HOMEROOM = -20;
 	public static final int LANG12 = -21;
 	public static final int LANG34 = -22;
 	public static final int GEOBASEBALL = -23;
 	
 	public static final int ROT1 = -31;
 	public static final int ROT2 = -32;
 	public static final int ROT3 = -33;
 	public static final int ROT4 = -34;
 	public static final int ROT5 = -35;
 	public static final int ROT6 = -36;
 	public static final int ROT7 = -37;
 	public static final int ROT8 = -38;
 	public static final int ROT9 = -39;
 	public static final int ROT10 = -40;
 	public static final int ROT11 = -41;
 	public static final int ROT12 = -42;
 	public static final int ROT13 = -43;
 	public static final int ROT14 = -44;
 	public static final int ROT15 = -45;
 	public static final int ROT16 = -46;
 	public static final int ADVISORY = -4;
 	public static final int COMMUNITYTIME = -10;
 	public static final int ASSEMBLY = -2;
 	public static final int CLASSMEETING = -3;
 	public static final int CLUBS = -5;
 	public static final int STUDYHALL = -6;
 	public static final int LUNCH = -7;
 	public static final int SPORTS = -8;
 	public static final int ELECTIVES = -9;
 	public static final int BREAK = -1;
	public static int[] sixthGradeClasses = {ROT1, ROT2, ROT3, ROT4, ROT5, ROT6, ROT7, ROT8, ROT9, ROT10, ROT11, ROT12, ROT13, ROT14, ROT15, ROT16, LANG12, LANG34, GEOBASEBALL, HOMEROOM};
 	
 	
 
 	private int number;
 	private int startTime;
 	private int endTime;
 
 	public Period() {
 
 	}
 
 	public Period(int start, int end, int number) {
 		this.startTime = start;
 		this.endTime = end;
 		this.number = number;
 	}
 
 	public int getNumber() {
 		return number;
 	}
 
 	public int getStartTime() {
 		return startTime;
 	}
 
 	public int getEndTime() {
 		return endTime;
 	}
 
 	public void setNumber(int number) {
 		this.number = number;
 	}
 
 	public void setStartTime(int startTime) {
 		this.startTime = startTime;
 	}
 
 	public void setEndTime(int endTime) {
 		this.endTime = endTime;
 	}
 
 	public String toString() {
 		return "st: " + startTime + " et: " + endTime + " num: " + number;
 	}
 
 	@Override
 	public int compareTo(Period o) {
 		int one = this.startTime;
 		int two = o.startTime;
 		if (one == two) {
 			return 0;
 		} else if (one > two) {
 			return 1;
 		} else {
 			return -1;
 		}
 	}
 }
