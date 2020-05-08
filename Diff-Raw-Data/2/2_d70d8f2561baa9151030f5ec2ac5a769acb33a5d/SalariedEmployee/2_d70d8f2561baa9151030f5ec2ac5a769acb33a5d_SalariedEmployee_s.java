 package dip.lab1.student.solution1;
 
 
 
 /**
  * A simple implementation sub-class of Employee. These are low-level classes
  * in the DIP. Does it meet the rules of DIP? If not fix it.
  *
  * @author your name goes here
  */
 public class SalariedEmployee extends Employee 
 {
     
     private double annualSalary;
     private double annualBonus;
 
     /** default constructor. Is this the best way to go? */
     public SalariedEmployee() {}
 
     /**
      * Convenience constructor. Is this the best way to go?
      * @param annualSalary - the employee's annual salary
      * @param annualBonus - a bonus benefit, if any
      */
     public SalariedEmployee(double annualSalary, double annualBonus) 
     {
         setAnnualSalary(annualSalary);
         setAnnualBonus(annualBonus);
     }
     
     public final double getAnnualBonus() 
     {
         return annualBonus;
     }
      
     public final void setAnnualBonus(double annualBonus) 
     {
         this.annualBonus = annualBonus;
     }
     
     public final double getAnnualSalary() 
     {
         return annualSalary;
     }
     
     public final void setAnnualSalary(double annualSalary) 
     {
         this.annualSalary = annualSalary;
     }
      
 
     @Override
     public final double getTotalAnnualWage() 
     {
        return this.annualBonus + this.annualSalary;
     }
 
     
 
     
 }
