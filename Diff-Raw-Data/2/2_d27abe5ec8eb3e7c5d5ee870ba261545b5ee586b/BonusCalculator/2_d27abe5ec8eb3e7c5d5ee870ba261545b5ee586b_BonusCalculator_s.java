 package net.diegolemos.bonuscalculator;
 
 import java.util.Set;
 
 import static java.lang.String.format;
 import static java.lang.System.out;
 
 public class BonusCalculator
 {
     private final Set<Employee> employees;
 
     public BonusCalculator(Set<Employee> employees)
     {
         assert employees != null : "Employees should not be null.";
 
         this.employees = employees;
     }
 
     public double payEveryone(double turnover)
     {
         out.println(format("Turnover: $ %.2f.", turnover));
         double leftover = turnover;
 
         for(Employee employee : employees)
         {
             double employeeBonus = employee.calculateBonus(turnover);
             leftover -= employeeBonus;
 
             if(employeeBonus > 0.0)
                out.println(format("Yeah! %s got payed $ %.2f.", employee.getName(), employeeBonus));
             else
                 out.println(format("Oops! %s does not have bonus.", employee.getName()));
         }
 
         out.println(format("Leftover: $ %.2f.", leftover));
 
         return leftover;
     }
 }
