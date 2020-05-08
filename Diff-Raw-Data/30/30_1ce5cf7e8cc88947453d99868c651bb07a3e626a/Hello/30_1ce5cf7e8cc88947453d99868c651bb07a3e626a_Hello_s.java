 /**
  * Hello is a class that greets the world.
  * 
  * @author dr
  * @since 1.0
  */
 public class Hello {
 
 	/**
 	 * The main method is the special method that the JVM calls when the program
 	 * is run.
 	 * 
 	 * @param args
 	 *            the list of command-line arguments that you ran the program
 	 *            with
 	 */
 	public static void main(String[] args) {
		boolean bo = false; // one bit, true or false
		byte by = 0; // signed 8 bit integral number
		short s = 0; // signed 16 bit integral number
		int i = 0; // signed 32 bit integral number
		long l = 0; // signed 64 bit integral number
		float f = 0.0f; // 32 bit floating point number
		double d = 0.0; // 64 bit floating point number

		char c = '\0'; // unicode (ucs-2) character, 16 bit

		// direct array initialization
		char[] chars = { 'h', 'e', 'l', 'l', 'o' };
		
		// array initialization with 'new'
		char[] moreChars = new char[5];
		moreChars[0] = 'h';
		moreChars[1] = 'e';
		// ...

		// multi-dimensional arrays
		char whatever[][] = { { 'h', 'i' }, { 'b', 'y', 'e' } };
				
		// array indexing
		System.out.println(chars[0]);
		System.out.println(chars[1]);
		
		String hello = "Hello, World";
		System.out.println(hello);
 	}
 
 }
