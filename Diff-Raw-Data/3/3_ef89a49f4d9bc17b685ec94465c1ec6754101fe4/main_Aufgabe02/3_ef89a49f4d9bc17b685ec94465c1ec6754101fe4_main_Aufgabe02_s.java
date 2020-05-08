 import java.io.*;
 import java.util.regex.*;
 
 public class main_Aufgabe02 {
 
 	/**
 	 * Wiederholen bis input negative Zahl { 
 	 *    input                    prozent 
 	 *      | 
 	 *      + wenn < 10000 ->	      0% 
 	 *      + wenn 10000 bis 39999 -> 2% 
 	 *      + wenn 40000 bis 99999 -> 5% 
 	 *      + wenn >= 100000 ->       8%
 	 * 
 	 *    resultat = input * prozent / 100 
 	 * }
 	 */
 	public static void main(String[] args) {
 		System.out.println("Aufgabe 02");
 
 		double dblIncome = -1;
 		double dblResult = 0;
 		String strInput = "";
 		boolean quit = false;
 
 		do {
 			System.out.println("\nBitte Einkommen eingeben: ");
 
 			strInput = readInput();
 
 			if (Pattern.matches("[0-9]+[.]{0,1}[0-9]*", strInput)) {
 
 				try { // convert String to double
 					dblIncome = Double.valueOf(strInput);
 				} catch (NumberFormatException e) {
 				} // error handling
 
 				int intPercent = taxPercent(dblIncome);
 				System.out.println("Steuernsatz: " + intPercent + "%");
 
 				// calculate the tax according to the percentage
 				if (intPercent > 0) {
 					dblResult = dblIncome * intPercent / 100;
 					System.out.println("Steuern zu zahlen: " + dblResult);
 				} else {
 					System.out.println("Keine Steuern zu zahlen");
 				}
 
 			} else {
 				dblResult = 0;
 				if (Pattern.matches("[-q]+[0-9]*", strInput)) {
 					quit = true;
 				} else {
 					System.out.println("Bitte eine positive Zahl eingeben.");
					System.out
							.println("Negative Zahl oder 'q' um Programm zu beenden.");
 				}
 			}
 
 		} while (!quit);
 
 		System.out.println("Auf wiedersehen.");
 	}
 
 	// Gets the percentage of the tax
 	private static int taxPercent(double dblIncome) {
 		int intPercent = 0;
 
 		if (dblIncome >= 0 && dblIncome < 10000) {
 			intPercent = 0;
 
 		} else if (dblIncome >= 10000 && dblIncome < 40000) {
 			intPercent = 2;
 
 		} else if (dblIncome >= 40000 && dblIncome < 100000) {
 			intPercent = 5;
 
 		} else if (dblIncome >= 100000) {
 			intPercent = 8;
 
 		} else {
 			intPercent = -1;
 		}
 		return intPercent;
 	}
     
 	// Reads from standard Input.
 	private static String readInput() {
 		String strInput;
 		// Creating a BufferReader from standard input.
 		BufferedReader buffRead = new BufferedReader(new InputStreamReader(
 				System.in));
 		// Read input
 		try {
 			strInput = buffRead.readLine();
 		} catch (IOException e) {
 			strInput = "";
 		}
 		return strInput;
 	}
 }
