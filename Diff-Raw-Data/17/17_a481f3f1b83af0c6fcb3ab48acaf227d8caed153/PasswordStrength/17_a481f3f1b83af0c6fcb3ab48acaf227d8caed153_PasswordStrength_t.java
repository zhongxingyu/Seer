 /*
 * Program asks user for password, assumes password is random and all symbols were produced independently.
 *The program then calculates the entropy of the password based on number of possible characters and length
 *of the password.
 */
 
 import java.util.*;
 
 public class PasswordStrength {

         final static double POSSIBLECHARS = 62; //Upper and Lower Alphebet (52), Arabic Numberals (10)

 	public static void main( String[] args ){
 		double entropy = 0;
 		int pwlength = 0;
 		String password;
 
                 if( args.length > 0 ){
                         password = args[0];
                         pwlength = password.length();
                         entropy = entropy(pwlength);
                 }
                 else{
                        Scanner scan = new Scanner( System.in );
                        System.out.print( "Enter a password (numbers and characters, not case sensitive): ");
 
                        password = scan.next();
                        pwlength = password.length();
                        entropy = entropy(pwlength);
 		}
 		System.out.println( "The password entered has " + (int)entropy + " bits of entropy." );
 	}
 
 	//does base-2 logarithm
 	public static double log2(double d) {
 		return Math.log(d)/Math.log(2.0);
 	}
 	private static double entropy(int length){
 	        return length*(log2(POSSIBLECHARS));
 	}
 }
