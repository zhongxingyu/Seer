 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 /**
  * A class to test the Worker class and its subclasses
  * @author Ian Thompson
  * @version 10.10.2012
  */
 public class WorkerTester
 {
 	/**
 	 * A helper method which asks for the parameters for a Worker
 	 * @param sc The Scanner to pull the input from
 	 * @return A Worker, constructed based on the user input
 	 */
 	private static Worker askForWorker(Scanner sc)
 	{
 		try
 		{
 			System.out.print("What is the name of the worker? ");
 			String name = sc.nextLine();
 			System.out.print("What is the worker's hourly wage? ");
 			double wage = sc.nextDouble();
 			sc.nextLine(); // Make sure to skip any extra text
 			System.out.print("H for hourly worker, S for salaried worker: ");
 			String type = sc.next();
 			sc.nextLine();
 
 			if (type.equalsIgnoreCase("H"))
 			{
 				return new HourlyWorker(name, wage);
 			}
 			else if (type.equalsIgnoreCase("S"))
 			{
 				return new SalariedWorker(name, wage);
 			}
 			else
 			{
				System.out.println("That's not a valid type!");
 				return null;
 			}
 		}
 		catch (NoSuchElementException e)
 		{
 			System.out.println("Failed to parse input: " + e.getMessage());
 			sc.nextLine();
 			return null;
 		}
 		catch (IllegalArgumentException e)
 		{
 			System.out.println("Invalid arguments: " + e.getMessage());
 			sc.nextLine();
 			return null;
 		}
 	}
 
 	public static void main(String[] args)
 	{
 		Scanner sc = new Scanner(System.in);
 		Worker work;
 		boolean stop = false;
 		
 		while (!stop)
 		{
 			work = askForWorker(sc);
 			
 			if (work == null)
 			{
 				System.out.println("Failed to create worker");
 			}
 			else
 			{
 				System.out.println("The " + work.getClass().getName() + " "
 					+ work.getName() + "'s pay for 35 hours is " + work.computePay(35));
 				System.out.println("The " + work.getClass().getName() + " "
 					+ work.getName() + "'s pay for 55 hours is " + work.computePay(55));
 			}
			System.out.println("Type C to continue, or anything else to stop");
			String reply = sc.nextLine();
			if (!reply.equalsIgnoreCase("C"))
				stop = true;
 		}
 		System.out.println("Goodbye!");
 	}
 
 }
 
 // Class.forName("HourlyWorker").getMethod("computePay", int.class).invoke(Class.forName("HourlyWorker").getConstructor(String.class, double.class).newInstance("John Doe", 35.0), 45);
