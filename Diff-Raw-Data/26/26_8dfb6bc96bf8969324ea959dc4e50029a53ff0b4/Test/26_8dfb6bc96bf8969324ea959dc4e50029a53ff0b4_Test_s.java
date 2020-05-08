<<<<<<< HEAD
 public class Test {
 	
 	public void howAwesomeAreYou(String name) {
 		System.out.println("Is " + name + " awesome?" );
		if ( name[0] == 'T' )
 			System.out.println(name + " is awesome!");
 		else
 			System.out.println(name + " is not very awesome...");
 	}
 	
	public static void main(String[] args) {
	    Test test = new test();
		System.out.println("hello world");
	    test.howAwesomeAreYou(Tim);
	    test.howAwesomeAreYou(Taylor);
	    test.howAwesomeAreYou(Katie);

=======
import java.util.Random;


public class Test {
	
 	public void randomNumber() {
 		final int MAXINT = 10;
 		System.out.println("Generating a random number between 1 and " + MAXINT + ": ");
 		Random rand = new Random();
 		System.out.println("You're random number is: " + rand.nextInt(MAXINT));
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 	    System.out.println("hello world");
 	    Test test = new Test();
 	    test.randomNumber();
>>>>>>> e333bcf851e85f051b902955177c729b21c12750
 	}
 
 }
