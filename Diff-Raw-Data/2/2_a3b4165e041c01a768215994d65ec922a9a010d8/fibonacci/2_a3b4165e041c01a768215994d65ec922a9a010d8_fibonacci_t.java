 public class fibonacci {
 	
 	public static void main(String[] args) {
 		
 		if (args.length < 1) {
 			System.out.println("Please type a number after fibonacci the next time you run this program.");
 			System.exit(0);
 		}
 
 		System.out.println("The first " + args[0] + " fibonacci numbers are:");
 		
 		iterativeFibonacci(Integer.parseInt(args[0]));
 		
 		// recursiveFibonacci(args[1]);
 	}
 
 
 	// prints the first n fibonacci numbers using an iterative process
 	public static void iterativeFibonacci(int n) {
 
 	}
 
	// prints the first n fibonacci numbers using a recursive process
 	public static void recursiveFibonacci(int n) {
 
 	}
 }
