 package ro.igstan.fuzzy;
 
 /**
  * @author Ionut G. Stan <ionut.g.stan@gmail.com>
  */
 
 public class FuzzyDateRomanianMessages implements FuzzyDateMessages {
 
     public String oneSecondAgo() {
         return "acum 1 secundă";
     }
 
     public String someSecondsAgo(int numberOfSeconds) {
         return "acum " + numberOfSeconds + " secunde";
     }
 
     public String oneMinuteAgo() {
         return "acum 1 minut";
     }
 
     public String someMinutesAgo(int numberOfMinutes) {
         return "acum " + numberOfMinutes + " minute";
     }
 
     public String oneHourAgo() {
         return "acum 1 oră";
     }
 
     public String someHoursAgo(int numberOfHours) {
         return "acum " + numberOfHours + " ore";
     }
 
     public String oneDayAgo() {
         return "acum 1 zi";
     }
 
     public String someDaysAgo(int numberOfDays) {
         return "acum " + numberOfDays + " zile";
     }
 
     public String oneWeekAgo() {
         return "acum 1 săptămână";
     }
 
     public String someWeeksAgo(int numberOfWeeks) {
         return "acum " + numberOfWeeks + " săptămâni";
     }
 
     public String oneMonthAgo() {
         return "acum 1 lună";
     }
 
     public String someMonthsAgo(int numberOfMonths) {
         return "acum 2 luni";
     }
 
     public String oneYearAgo() {
         return "acum 1 an";
     }
 
     public String someYearsAgo(int numberOfYears) {
        return "acum " + numberOfYears + " ani";
     }
 }
