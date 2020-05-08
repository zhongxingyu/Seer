 package ro.igstan.fuzzy;
 
 /**
  * @author Ionut G. Stan <ionut.g.stan@gmail.com>
  */
 
 public interface FuzzyDateMessages {
 
     public String oneSecondAgo();
 
     public String someSecondsAgo(long diff);
 
     public String oneMinuteAgo();
 
     public String someMinutesAgo(long diff);
 
     public String oneHourAgo();
 
     public String someHoursAgo(long numberOfHours);
 
     public String oneDayAgo();
 
     public String someDaysAgo(long numberOfDays);
 
     public String oneWeekAgo();
 
     public String someWeeksAgo(long numberOfWeeks);
 
     public String oneMonthAgo();
 
    public String someMonthsAgo(long numberOfMonths);
 
     public String oneYearAgo();
 
     public String someYearsAgo(long numberOfYears);
 
 }
