 /**
  * 
  */
 package ro.inf.p2.uebung01;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 /**
  * @author felix
  *
  */
 public class Sekunden {
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 		throws NumberFormatException, IOException {
 		
 		int h, m, s;
 		
 		BufferedReader in =
 			new BufferedReader(new InputStreamReader(System.in));
 
 		// Input Hour
 		System.out.println("Hours: ");
 		h = Integer.parseInt(in.readLine());
 		
 		// Input Minute
 		System.out.println("Minutes: ");
 		m = Integer.parseInt(in.readLine());
 		
 		s = h * 3600 + m * 60;

 		
 		System.out.println("Seconds = " + s);
 	}
 }
