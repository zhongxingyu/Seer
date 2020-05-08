 package program;
 
 import java.util.ArrayList;
 
 /**
  * Implements a pseudo-RNG to customer specification.
  * @author JOH
 * @version 0.11
  *
  */
 public class RandGenerator {
 
	private long seed;		//	The sequence of random number seeds
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param seed the initial seed for the RNG
 	 */
 	public RandGenerator(long seed)
 	{
		this.seed = seed;
 		//	Add the first three values
 		for (int i = 1; i < 4; i++)
 		{
			this.seed = this.seed * 22695477 + 1;
 		}
 	}
 	
 	/**
 	 * Returns a new pseudo-random integer between 0 and n-1 inclusive.
 	 * @param n the range of the number to generate
 	 * @return the random number
 	 */
 	public long randomInt(int n)
 	{
		seed = seed * 22695477 + 1;
 
		long x = seed / 65536 % 16384;
 
 		//	Account for Java modulus behavior 
 		if (x % n >= 0)
 		{
 			return x % n;
 		}
		return (x % n) + (n-1);
 	}
 }
