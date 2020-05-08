 package commissioncalculation2;
 
 /**
  *
  * @author Your Name <J. Marie Daake>
  *
  * This class calculates a salesperson's gross
  * pay based on the amount of sales.
  */
 
 
 public class EmployeeCommission2
 {
 	private double  sales, 		// Monthly sales
 			rate, 		// rate of commission
 			commission, 	// Amount of commission
                         salary,         // Base salary
                         pay,            // Commission + Salary
                         target;         // Target Sales amount
                 char    reached;        // y/n for target reached
 	/**
 	* The constructor uses a parameter to accept
 	* argument: s. The value in s is assigned to
 	* the sales field. The calculatePay method is called.
 	*/
 
         public EmployeeCommission2(double s)
 	{
             sales = s;
            calculatePay();  
        }
        
 	/**
 	* The setRate method sets the rate of commission.
         * This method is called from the calculatePay method.
 	*/
 
 	private void setRate()
 	{
 		rate = .06;
 	}
 
         /**
 	* The setSalary method sets the Base Salary.
       	*/
 
 	private void setSalary()
 	{
 		salary = 1600 * 12;
 	}
         
       
         
         /**
 	* The setSalary method sets the Base Salary.
       	*/
 
         private boolean setReached()
         {
            if(sales >= 500000)
             {reached = 'y';}
            else 
             {reached = 'n';}
             return false;
         }
         
         /**
 	* The setSalary method sets the Base Salary.
       	*/
         
        	private void setTarget()
 	{
 		target = 500000;
 	}
         /**
 	* The setSalary method sets the Base Salary.
       	*/
 
 	private void setCommission()
 	{
 		commission = sales * rate;
 	}  
         
         /**
 	* The calculatePay method calculates the salesperson's
 	* commission and amount of actual pay.
 	*/
 
 	private void calculatePay()
 	{
 		setRate();
                 setSalary();
                 setCommission();
                 setTarget();
                 setReached();
                 pay = commission + salary;
                 
         }
 
 	/**
 	* The getCommission method returns the commission field.
 	*/
 
 	public double getCommission()
 	{
 		return commission;
 	}
 
 	/**
 	* The getRate method returns the rate field.
 	*/
 
 	public double getRate()
 	{
 		return rate;
 	}
 
 
 	/**
 	* The getSales method returns the sales field.
 	*/
 
 	public double getSales()
 	{
 		return sales;
 	}
         
         /**
          * The getPay method returns gross pay
          */
         
         public double getPay()
 	{
 		return pay;
 	}
         
         /**
          * The getPay method returns annual salary
          */
         
         public double getSalary()
 	{
 		return salary;
 	}
         
         /**
          * The getTarget method returns annual salary
          */
         
         public double getTarget()
 	{
 		return target;
 	}
         
         public char getReached()
         {
                 return reached;
         }
         
 
         final double compRate = .5;
         
         public double getComp()
         {
             return sales * compRate;
         }
 }
