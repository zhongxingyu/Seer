 public class javasample {
 	
 	public static void main(String[] args) {
 		System.out.println(factorial(10));
 	}
 
 	/**
 		Get a factorial *recursively*
 
 		Standard factorial method using recursion, returns 1 if x is less than
 		or equal to 1, otherwise it returns `factorial(x-1)`.
 
 		@param 	x	integer to get the factorial of.
 		@return x!
 	*/
 	public static int factorial(int x) {
 		if (x <= 1)
 			return 1;
 		else
 			return factorial(x-1);
 	}

	/**
		Jumble up a string

		Currently it doesn't do anything, as the whole method is

			public static String jumble(String toJumble) {
				return toJumble;
			}

		However in the possible future, it might do something, something
		really cool like. YEAH.

		@param 	toJumble	the string you want to jumble
		@return toJumble, jumbled up all fancy like.
	*/
	public static String jumble(String toJumble) {
		return toJumble;
	}
 }
