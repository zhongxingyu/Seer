 package dip.lab1.student.solution1;
 
 
 
 /**
  * An implementation sub-class of an Employee. These are low-level classes
  * in the DIP. Does it meet the rules of DIP? If not fix it.
  *
  * @author your name goes here
  */
 public class HourlyEmployee extends Employee 
 {
     
     /** default constructor. Is this the best way to go? */
     public HourlyEmployee() {}
 
     /**
      * Convenience constructor. Is this the best way to go?
      * @param hourlyRate - the rate per hour that the employee is paid
      * @param totalHrsForYear - total hours worked or predicted per year
      */
     public HourlyEmployee(double hourlyRate, double totalHrsForYear) 
     {
         setHourlyRate(hourlyRate);
         setTotalHrsForYear(totalHrsForYear);
     }
 
 }
