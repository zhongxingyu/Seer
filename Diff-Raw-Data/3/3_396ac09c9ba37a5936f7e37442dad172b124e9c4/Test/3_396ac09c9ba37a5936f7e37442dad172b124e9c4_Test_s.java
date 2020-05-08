 package pl.cougy.MonteCarlo;
 
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class Test {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		@SuppressWarnings("resource")
 		Scanner scanner = new Scanner(System.in);
 		System.out.println("Please enter your expresion to be integrated with MonteCarlo method");
		System.out.println("you can use standard notation for example (x*x+y*y) :");
 		String expression = scanner.next();
 		
 		System.out.println("How many variables have you entered : ");
 		Integer numberOfVariables = scanner.nextInt();
 		
 		ArrayList <Double> min = new ArrayList<>();
 		ArrayList <Double> max = new ArrayList<>();
  		for(int i=0; i < numberOfVariables.intValue();i++) {
 			System.out.print("Please enter " + Integer.toString(i+1) + " interval [a"+ Integer.toString(i+1) +";b"
 							+ Integer.toString(i+1) + "]: ");
 			min.add(scanner.nextDouble());
 			max.add(scanner.nextDouble());
 		}
 
 		
 		MonteCarlo test = new MonteCarlo(expression, 1000000, new Intervals(min,max)).generateValues();
 		
 		
 		System.out.println("Integral " + Double.toString(test.getIntegral()));
 		System.out.println("Error    " + Double.toString(test.getError()));
 	}
 
 }
