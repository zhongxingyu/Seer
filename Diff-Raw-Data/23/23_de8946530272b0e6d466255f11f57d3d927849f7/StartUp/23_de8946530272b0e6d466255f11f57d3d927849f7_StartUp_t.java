 
 package myconcrete;
 import java.util.Scanner;
 
 /**
  *
  * @author Michael Grinker
  */
 public class StartUp {
     
     public static void main(String[] args) {
         String fName,
                 lName;
         String empID;
         int hours;
         double hrRate;
         
         
         //Create scanner object
         Scanner keyboard = new Scanner(System.in);
         
         //Create HourlyEmployee object
         //Get info & display
         
         System.out.println("You are going to enter the info for an hourly employee");
         System.out.print("What is the first name of the employee? ");
         fName = keyboard.nextLine();
         System.out.print("What is the last name of the employee? ");
         lName = keyboard.nextLine();
         System.out.print("What is the employee ID? ");
         empID = keyboard.nextLine();
         System.out.print("How many hours did the employee work? ");
         hours = keyboard.nextInt();
         System.out.print("What is the hourly rate for this emploee? ");
         hrRate = keyboard.nextDouble();
         
         HourlyEmployee emp1 = new HourlyEmployee(hrRate, fName, lName, hours, empID);
        emp1.pay();
         System.out.println(emp1);
         
         
         
 
     }
 }
