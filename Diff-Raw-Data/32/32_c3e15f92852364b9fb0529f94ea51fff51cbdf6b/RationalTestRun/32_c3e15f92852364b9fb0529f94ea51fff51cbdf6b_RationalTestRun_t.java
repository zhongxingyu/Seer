 package CS565;
 
 public class RationalTestRun {
 	
 	public static void main(String[] args) {
 		// default ctor returns 1/1
 		Rational r = new Rational();
		puts("default ctor returns 1/1: \n\t"+ r.toFractionString() + " or " + r.toFloatString());
 
 		// constructor can take numerator, denominator to initialize
 		Rational half = new Rational(1, 2);
		puts("ctor takes numerator, denominator to initialize: \n\tnew Rational(1, 2) == "+ half.toFractionString() + " or " + half.toFloatString());
 		
 		// we can print the value as a fraction
		puts("we can print the value as a fraction: \n\tnew Rational(1, 2) == "+ half.toFractionString());
 
 		// we can print the value as a float
		puts("we can print the value as a float: \n\tnew Rational(1, 2) == "+ half.toFloatString());
 
 		// we can specify number of decimals when printing float
		puts("we can specify number of decimals when printing float: \n\tnew Rational(1, 2).toFloat(4) == "+ half.toFloatString(4));
 
 		// it is automatically reduced when initialized
 		Rational third = new Rational(17, 51);
		puts("it is automatically reduced when initialized: \n\tnew Rational(17, 51) == "+ new Rational(17, 51).toFractionString());
 
 		// we can add two rationals to produce a new rational
		puts("we can add two rationals to produce a new rational: \n\t1/2 plus 1/3 == "+ Rational.add(half, third).toFractionString());
 
 		// Add is commutative
		puts("add is commutative: \n\t1/3 + 1/2 == "+ Rational.add(third, half).toFractionString());
 
 		// we can subtract two rationals
		puts("we can subtract two rationals: \n\t1/2 - 1/3 == "+ Rational.subtract(half, third).toFractionString());
 
 		// we can multiply two rationals to produce a new rational
 		Rational quarter = new Rational(1, 4);
		puts("we can multiply two rationals to produce a new rational: \n\t1/4 * 1/3 == "+ Rational.multiply(quarter, third).toFractionString());
 
 		// we can divide two rationals
		puts("we can divide two rationals: \n\t1/4 / 1/3 == "+ Rational.divide(quarter, third).toFractionString());
 
 		// divide by zero is handled
 		puts("divide by zero is handled:");
 		Rational zero = new Rational(0, 1);
 		try	{
 			Rational.divide(quarter, zero);
 		} catch (RuntimeException ex) {
 			puts("\tthrew and handled exception since divide by zero occured");
 		}
 	}
 	
 	// shortcut functions to simplify and shorten above code
 	private static void puts(String out) {
 		System.out.println(out + "\n");
 	}
 
 }
