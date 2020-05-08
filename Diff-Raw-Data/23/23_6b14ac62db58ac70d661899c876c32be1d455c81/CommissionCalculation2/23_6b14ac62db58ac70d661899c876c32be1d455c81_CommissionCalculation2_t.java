 package commissioncalculation2;
 
 /**
  *
  * @author Your Name <J. Marie Daake>
  */
 import java.text.DecimalFormat;
 import java.util.Scanner;
 
 /**
 * This program calculates a salesperson's gross
 * pay.
 */
 
 public class CommissionCalculation2
 {
     @SuppressWarnings("empty-statement")
     public static void main(String[] args)
     {     
         double sales;    // To hold amount of sales
 
 	// Create DecimalFormat objects for dollar amounts
 	// and percentages.
 	DecimalFormat dollar = new DecimalFormat("#,##0.00");
 	DecimalFormat percent = new DecimalFormat("#0%");
 
 	// Create a Scanner object to read input.
 	Scanner keyboard = new Scanner(System.in);
 
 	System.out.println("This program will display a "
 			+ "pay report for a salesperson.");
 	System.out.println("Enter the following information:");
 
         // Ask the user for sales
 	System.out.print("Annual sales: $");
 	sales = keyboard.nextDouble();
 
 	// Create an instance of the SalesCommission
 	// class and pass the data to the constructor.
 	EmployeeCommission2 payInfo =
             new EmployeeCommission2(sales);
         
         // Display the pay report for the salesperson.
 	System.out.println("\nPay Report");
 	System.out.println("-------------------------");
         System.out.println("Annual Salary: $"
                 + dollar.format (payInfo.getSalary()));
        	System.out.println("Sales: $"
 		+ dollar.format(payInfo.getSales()));
         System.out.println("\tSales Target: $"
                 + dollar.format (payInfo.getTarget()));
         System.out.println("\tTarget Reached? "
                 + (payInfo.getReached()));
 	System.out.println("Commission rate: "
 		+ percent.format(payInfo.getRate()));
 	System.out.println("Commission Amount: $"
 		+ dollar.format(payInfo.getCommission()));
         System.out.println("Total Annual Pay: $"
                 + dollar.format(payInfo.getPay()));
         System.out.println("\nPossible Compensations "
                 + "for reaching target");
         System.out.println("-------------------------------");
         System.out.println("| Total Sales\t|\tCompensation |");
                 
         //Compensation Table Loop//
                
          // Constants
        //final double maxComp = payInfo.getSales() * .5;
        
        // range is from annual sales this year to 50% ABOVE
        // the annual sales.  ie annual sales * 150% or 1.5
        final double maxComp = payInfo.getSales() * 1.5;
        
         final double increment = 5000; 
 
         // Variables
         double sale;
         double comp; 
 
         for (sale = payInfo.getSales(); sale <= maxComp; sale += increment)
         {
            // need to update/set thew new sales target
            
              comp = payInfo.modifySales( sale );
 
              System.out.println("| " + sale + "\t|\t" + comp + " |");     
         }
     }
 }
