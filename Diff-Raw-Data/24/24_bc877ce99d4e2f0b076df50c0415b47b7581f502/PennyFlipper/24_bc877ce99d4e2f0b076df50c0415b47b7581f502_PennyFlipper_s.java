 /* created October 6, 2013
  * 
  * Flips pennies.
  */
 
 package penny;
 
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class PennyFlipper
 {
<<<<<<< HEAD
 	private double multiplier = 1;	// how many multiples of 64
 	private ArrayList<Penny> pennies;
 	
 	public PennyFlipper()
 	{
 		super();
		getUserInput();
=======
	private ArrayList<Penny> pennies;
	// add variables to store user input

	public PennyFlipper()
	{
		super();
 		// get user input
		//getUserInput();
>>>>>>> bdd090cd4bd7388d8b5fdb4fbdc51f32defa561e
 		createPennies(6);
 		
 		double totalTries = 64;	// out of 6 pennies, there's a 1/64 chance for all heads
 		double average = 0;
 		double headCount = 0;
 		
 		totalTries *= multiplier;
 		for(int i = 0; i < totalTries; i++)
 		{
 			// Flip all pennies
 			for(int j = 0; j < 6; j++)
 			{
 				pennies.get(j).flip();
 				
 				if(pennies.get(0).getSide().equalsIgnoreCase("heads") && pennies.get(1).getSide().equalsIgnoreCase("heads")
 						&& pennies.get(2).getSide().equalsIgnoreCase("heads") && pennies.get(3).getSide().equalsIgnoreCase("heads")
 						 && pennies.get(4).getSide().equalsIgnoreCase("heads") && pennies.get(5).getSide().equalsIgnoreCase("heads"))
 				{
 					headCount++;
 				}
 			}
 			
 			for (Penny penny : pennies)
 			{
 				penny.resetSide();
 			}
 		}
 		average = headCount/totalTries;
 		
 		System.out.print("Out of " + totalTries + " tries on " + pennies.size() + " pennies, ");
 		// display proper string if only one head was flipped
 		if(headCount == 1)
 			System.out.println("you got all heads " + headCount +" time.");
 		else
 			System.out.println("you got all heads " + headCount +" times.");
 		System.out.println();
 		System.out.println("Average: " + average);
 		System.out.println("Flipped all heads " + average*100 +"% of the time.");
 	}
 	
 	private void getUserInput()
 	{
<<<<<<< HEAD
 		Scanner s = new Scanner(System.in);
 		System.out.println("How many times would you like to run the simulation?");
 		multiplier = s.nextDouble();
 		s.close();
 	}
 
=======
		// set up buffer
		// prompt user
		// store values
	}	
		
>>>>>>> bdd090cd4bd7388d8b5fdb4fbdc51f32defa561e
 	/**
 	 * Adds pennies to an ArrayList for future use.
 	 * @param numberOfPennies - the number of pennies to add.
 	 */
 	private void createPennies(int numberOfPennies)
 	{
 		pennies = new ArrayList<Penny>();
 		for(int i = 1; i <= numberOfPennies; i++)
 			pennies.add(new Penny("p"+i));
 	}
 }
