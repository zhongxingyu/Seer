 /**************************************************************
  * 
  * 	fibonacci
  * 
  * 		Computes the first n fibonacci numbers
  * 
  **************************************************************/
 
 public class fibonacci {
 	
 	/**
 	 * main
 	 * 
 	 * 
 	 * @param args - args[0] is the number of fibonacci numbers to find
 	 */
 	public static void main(String[] args) {
 		
 		if (args.length < 1) {
 			System.out.println("Please type a number after fibonacci the next time you run this program.");
 			System.exit(0);
 		}
 
 		int n = 0;
 		try {
 			n = Integer.parseInt(args[0]);
 		} catch (NumberFormatException e) {
 			System.out.println(args[0] + " is not a number.");
 			System.exit(0);
 		}
 		if (n < 1) {
 			System.out.println("The number must be greater than 0.");
 			System.exit(0);
 		}
 
 		System.out.println("The first " + args[0] + " fibonacci numbers are:");
 		
 		iterativeFibonacci(Integer.parseInt(args[0]));
 		
 		// recursiveFibonacci(args[1]);
 	}
 
 
 	/**
 	 * iterativeFibonacci
 	 * 		iteratively computes the first n fibonacci numbers
 	 * 
 	 * @param n - the number of fibonacci numbers to find. must be non-negative.
 	 */
 	public static void iterativeFibonacci(int n) {
 
 	}
 
 	/**
 	 * recursiveFibonacci
 	 * 		recursively computes the first n fibonacci numbers
 	 * 
 	 * @param n - the number of fibonacci numbers to find. must be non-negative.
 	 */
	public static void recursiveFibonacci(int n) {
 
	}
 
 }
