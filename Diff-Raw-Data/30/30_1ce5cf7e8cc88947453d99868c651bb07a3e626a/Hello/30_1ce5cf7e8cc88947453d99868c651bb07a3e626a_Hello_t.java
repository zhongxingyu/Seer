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
		String name = args[0];
		System.out.println("Hello, " + name);
 	}
 
 }
