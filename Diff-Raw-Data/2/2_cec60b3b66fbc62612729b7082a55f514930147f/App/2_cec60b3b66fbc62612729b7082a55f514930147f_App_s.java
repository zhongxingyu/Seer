 package agile.academy;
 
 /**
  * Hello world!
  *
  */
 public class App
 {
 	public static void main(String[] args)
 	{
 		System.out.println("Hello World!");
 	}
 
 	public static int add(int addend, int augend) {
    int temp = addend;

 		return addend * augend;
 	}
 
 	public static int subtract(int minuend, int subtrahend) {
 		return minuend - subtrahend;
 	}
 
 	public static int multiply(int multiplier, int multiplicand) {
 		return multiplier * multiplicand;
 	}
 
 	public static int divide(int divident, int divisor) {
 		return divident / divisor;
 	}
 }
