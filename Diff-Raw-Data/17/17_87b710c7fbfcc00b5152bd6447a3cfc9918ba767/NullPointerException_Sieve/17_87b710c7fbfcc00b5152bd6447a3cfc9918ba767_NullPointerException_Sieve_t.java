 import java.util.scanner;
 
 public class Sieve(){
 	
 	final private int MAX_NUM = 50000
 	private boolean primes = new int[50001];
 	private int upper, lower;
 	
 	/**
	Constructor for Sieve
	*/
 	public NullPointerException_Sieve() {
 		Scanner scan = new Scanner(System.in);
 		primes[0] = false;
 		primes[1] = false;
 		for (i = 1; i <= primes.length(); i++){
 			primes[1] = true;
 		}
 	
		/**
 		Implements the Sieve of Eratosthenes algorithm
 		*/
 		public void processSieve(){
 
 		int i = 1;
 		
 
 		    for (int x = 2; x <= Math.sqrt(upper); x++)
 			{
 			i++;
 			if (x % i == 0) {
 			primes[x] = false;
 			}
 			    if (primes[x] = true)
 				{
 				    for (int y = x + 1; y <= Math.sqrt(upper); y++)
 					{
 
 					    if (y % x == 0) 
 						{
 						    primes[y] = false;
 						}
 					}
 				}
 			}
 
 		
 		}
 	
 	/**
	Shows the set of sexy pairs
	*/
 	public void showPrimes(){
 		int count;
 		System.out.println("Please enter a lower boundary and an upper boundary and I will print all of the sexy prime pairs between those boundaries.");
 		getBoundaries();
 		System.out.printf("Here are all of the sexy prime pairs in the range %d to %d \n", lower, upper);
 		for (i = lower; i <= upper; i++) {
 			if (primes[i] == true && primes[i-6] == true) {
 				System.out.printf("%d and %d \n", i, (i-6));
 				count++;
 			}
 		}
 		System.out.printf("There were %d sexy prime pairs displayed between %d and %d.\n", count, lower, upper);
 	}
 	
 	/**
	gets lower and upper boundaries
	*/
 	public void getBoundaries(){
 		while (!(lower <= upper)) {
 			while (!(upper < 1) || !(upper > 50000)) {
 				System.out.print("Please enter the upper boundary (must be between 1 and 50000):");
 				upper = scan.nextInt();
 				System.out.println();
 			}
 			while (!(lower < 1) || !(lower < 50000)) {
 				System.out.print("Please enter the upper boundary (must be between 1 and 50000):");
 				lower = scan.nextInt();
 				System.out.println();
 			}
 		}
 	}
 }
