 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package myconcrete;
 
 /**
  *
  * @author Michael
  */
 public class HourlyEmployee extends Employee{
     private double hourlyRate;
     private double grossPay;
 
     public HourlyEmployee(double hourlyRate, String firstName, String lastName, int hoursWorked, String badgeID) {
         super(firstName, lastName, hoursWorked, badgeID);
         this.hourlyRate = hourlyRate;
     }
 
     /**
      * @return the hourlyRate
      */
     public double getHourlyRate() {
         return hourlyRate;
     }
 
     /**
      * @param hourlyRate the hourlyRate to set
      */
     public void setHourlyRate(double hourlyRate) {
         this.hourlyRate = hourlyRate;
     }
     
    public double getGrossPay() {
        return hourlyRate * super.getHoursWorked();
     }
     
     @Override
     public String toString() {
         String str;
         
        str = super.toString() + 
                "Hourly Rate: " + hourlyRate
                 + "\nGross Pay: " + grossPay;
         
         return str;
     }
 }
