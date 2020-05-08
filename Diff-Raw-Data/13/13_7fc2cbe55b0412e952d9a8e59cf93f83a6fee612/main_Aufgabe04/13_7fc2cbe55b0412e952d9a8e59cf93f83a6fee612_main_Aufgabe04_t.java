 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.regex.Pattern;
 import java.lang.Math;
 
 
 public class main_Aufgabe04 {
 
 	/**
 	 * Werte für A, B und C verlangen
 	 * 
 	 * Berechen x
 	 * 
 	 * x = ( -B +-(B^2 - 4AC )^(1/2)) / ( 2A )
 	 */
 	public static void main(String[] args) {
 		System.out.println("Aufgabe04\n");		
 
         double a = readDblInput("--- A ---");
         double b = readDblInput("--- B ---");
         double c = readDblInput("--- C ---");               
         
        double dblSqr = Math.sqrt(Math.pow( b, 2 ) - 4 * a * c);        
         
        if ( dblSqr < 0 ) {
         	System.out.println("Kein reeles Resultat...");
        } else if (dblSqr == 0) {
        	System.out.println("x:" + ( b + dblSqr ) / ( 2 * a));
         } else {
        	double x1 = ( -b + dblSqr ) / ( 2 * a);
        	double x2 = ( -b - dblSqr ) / ( 2 * a);
 	        System.out.println("x1:" + x1 + " x2:" + x2);    	
         }        
 	}
 
 	// Reads from standard Input and return value if it is a number.
 	private static double readDblInput(String strMessage) {
 		double dblInput;
 		String strInput = "";
 		// Creating a BufferReader from standard input.
 		BufferedReader buffRead = new BufferedReader(new InputStreamReader(System.in));
 
 		do {
 			System.out.println(strMessage);
 			// Read input
 			try {
 				strInput = buffRead.readLine();
 			} catch (IOException e) {}			
 
 		} while (!Pattern.matches("-?[0-9]+[.]?[0-9]*", strInput));
 			
 		try { // convert String to double
 			dblInput = Double.valueOf(strInput);
 		} catch (NumberFormatException e) {
 			dblInput = 0;
 		} // error handling
 		
 		return dblInput;
 	}	
 
 }
