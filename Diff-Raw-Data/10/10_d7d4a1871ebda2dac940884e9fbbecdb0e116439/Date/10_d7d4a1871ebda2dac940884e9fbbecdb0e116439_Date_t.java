 /**
  * Class to represent a date.
  *
  * @author cmcl
  * @version 1.0
  */
 public class Date implements Comparable<Date> {
     public static final int JAN = 1, FEB = 2, MAR = 3, APR = 4, MAY = 5,
                              JUN = 6, JUL = 7, AUG = 8, SEP = 9, OCT = 10,
                              NOV = 11, DEC = 12;
     public static final Integer[] days = getDays();
     public static final Integer[] months = getMonths();
     public static final Integer[] years = getYears();
     public static final String[] monthNames = {"January", "February", 
                             "March", "April", "May", "June", "July",
                             "August", "September", "October",
                             "November", "December"};
    public static final String[] dayNames = { "Monday", "Tuesday", 
        "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
     
     private int day, month, year;
     
     public Date(int d, int m, int y) {
         day = d;
         month = m;
         year = y;
     }
     
     public Date() {
         this(1, 1, 1970);
     }
     
     public Date(Date d) {
         day = d.day;
         month = d.month;
         year = d.year;
     }
     
     public int getDay() { return day; }
     public void setDay(int d) { day = d; }
     
     public int getMonth() { return month; }
     public void setMonth(int m) { month = m; }
     
     public int getYear() { return year; }
     public void setYear(int y) { year = y; }
     
     public boolean isLeapYear() {
         return ((year % 4 == 0 && year % 100 != 0) ||
                 (year % 100 == 0 && year % 400 == 0));
     }
     
     public boolean isValid() {
         return year > 0 && month > 0 && month < 13 && day > 0 &&
             day <= getDaysInMonth(year)[month-1];
     }
     
     /**
      * Increment the date by one day.
      */
     public void increment() {
         Integer[] daysInMonth = getDaysInMonth(year);
         
         if (day == daysInMonth[month-1]) {
             if (month == DEC) {
                 month = JAN;
                 year++;
             } else month++;
             day = 1;
         } else day++;
     }
     
     /**
      * Decrement the date by one day.
      */
     public void decrement() {
         Integer[] daysInMonth = getDaysInMonth(year);
         if (day == 1) {
             if (month == JAN) {
                 month = DEC;
                 year--;
             } else
                 month--;
             day = daysInMonth[month - 1];
         } else
             day--;
     }
     
     public int compareTo(Date d) {
         if (year == d.year)
             if (month == d.month) return day - d.day;
             else return month - d.month;
         else return year - d.year;
     }
     
     public boolean equals(Object o) {
         return (o instanceof Date && ((Date)o).day == day &&
             ((Date)o).month == month && ((Date)o).year == year);
     }
 
     public String dayName(){
 	Date temp = new Date(1,1,1970);
 	int count = 0;
 	while(getYear()>temp.getYear()){
 		if(temp.isLeapYear())
 			count+=366;
 		else count +=365;
 		temp.setYear(temp.getYear()+1);
 	}
 	Integer[] daysInMonth = getDaysInMonth(year);
 	while(getMonth()>temp.getMonth()){		
 		count+=daysInMonth[temp.getMonth()];
 		temp.setMonth(temp.getMonth()+1);
 	}
 	while(getDay()>temp.getDay()){
 		count+=1;
 		temp.increment();
 	}
 	switch (count%7) {
                 case 0: return dayNames[3];
                 case 1: return dayNames[4];
                 case 2: return dayNames[5];
                 case 3: return dayNames[6];
                 case 4: return dayNames[0];
                 case 5: return dayNames[1];
                 case 6: return dayNames[2];
             }
 	return "Mathematical impossibility has occurred. Congrats.";	
     }
             
     
     public String toString() {
         return day + "/" + month + "/" + year;
     }
     
     
     /**
      * Test method with primitive test harness.
      */
     public static void main(String[] args) {
         Date d1 = new Date(12, 1, 2012);
         Date d2 = new Date(23, 4, 2012);
         Date d3 = new Date(9, 1, -1);
         Date d4 = new Date(2, 12, 2011);
         Date nonLeapYear = new Date(29, 2, 2011);
         Date validDate = new Date(1, 1, 2011);
         
         Date endOfYear = new Date(31, 12, 2012);
         endOfYear.increment();
         Date notEndOfFeb = new Date(28, 2, 2012);
         notEndOfFeb.increment();
         int passes = 0, fails = 0;
         
         Pair[] tests = {
             new Pair("d1.compareTo(d2) < 0", d1.compareTo(d2) < 0),
             new Pair("!d3.isValid()", !d3.isValid()),
             new Pair("d1.compareTo(d4) > 0", d1.compareTo(d4) > 0),
             new Pair("d2.compareTo(d4) > 0", d2.compareTo(d4) > 0),
             new Pair("d2.equals(d2)", d2.equals(d2)),
             new Pair("!d2.equals(d1)", !d2.equals(d1)),
             new Pair("!nonLeapYear.isValid()", !nonLeapYear.isValid()),
             new Pair("validDate.isValid()", validDate.isValid()),
             new Pair("endOfYear.increment().equals(newYear)",
                      endOfYear.equals(new Date(1, 1, 2013))),
             new Pair("!notEndOfFeb.increment().equals(march)",
                      !notEndOfFeb.equals(new Date(1, 3, 2012))),
         };
         
         for (Pair p : tests) {
             testDate(p.getKey(), p.getValue());
             if (p.getValue()) passes++;
             else fails++;
         }
         
         System.out.println("Passes : " + passes);
         System.out.println("Failures : " + fails);
     }
     
     /**
      * Helper method for checking the result of a test case
      * and providing diagnostics for failures.
      */
     private static void testDate(String s, boolean b) {
         if (!b) System.out.println("Failed : " + s);
         else System.out.println("Passed : " + s);
     }
     
     /**
      * Helper class for test case generation.
      */
     private static class Pair {
         private String key;
         private Boolean value;
         
         public Pair(String k, Boolean v) {
             key = k;
             value = v;
         }
         
         public String getKey() { return key; }
         
         public Boolean getValue() { return value; }
     }
     
     private static Integer[] getDays() {
         Integer[] days = new Integer[31];
         for (int i = 0; i < days.length; i++) days[i] = i+1;
         return days;
     }
     
     private static Integer[] getMonths() {
         Integer[] months = new Integer[12];
         for (int i = 0; i < months.length; i++) months[i] = i+1;
         return months;
     }
     
     private static Integer[] getYears() {
         Integer[] years = new Integer[2];
         years[0] = 2011;
         years[1] = 2012;
         return years;
     }
     
     /**
      * Obtain the days in every month of the year passed as a argument.
      *
      * @returns a list of month lengths indexed by month (starting
      * from zero).
      */
     public static Integer[] getDaysInMonth(int year) {
         if (year < 0) return null;
         
         Integer[] daysInMonth = new Integer[months.length];
         
         Date d = new Date(1, 1, year);
         
         for (int i = 0; i < daysInMonth.length; i++) {
             switch (i+1) {
                 case FEB:
                     if (d.isLeapYear()) daysInMonth[i] = 29;
                     else daysInMonth[i] = 28;
                     break;
                 case JAN:
                 case MAR:
                 case MAY:
                 case JUL:
                 case AUG:
                 case OCT:
                 case DEC:
                     daysInMonth[i] = 31;
                     break;
                 case APR:
                 case JUN:
                 case SEP:
                 case NOV:
                     daysInMonth[i] = 30;
                     break;
             }
         }
         return daysInMonth;
     }
 }
