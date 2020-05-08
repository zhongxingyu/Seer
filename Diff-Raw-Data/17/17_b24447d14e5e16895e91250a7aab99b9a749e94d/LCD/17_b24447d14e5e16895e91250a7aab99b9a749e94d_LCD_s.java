 package katas.tdd.test;
 
import java.io.IOException;
 
 public class LCD {
 
 	final String ENTER = "\n";
 	final String MODE1 = " - ";
 	final String MODE2 = "| |";
 	final String MODE3 = " - ";
 	final String MODE4 = "| |";
 
 	final String MODE6 = "|  ";
 	final String MODE7 = "  |";
 	final String MODE8 = "   ";
 
 	final String howTheNumberisFormed[][] = {
 			{ MODE1, MODE2, MODE8, MODE2, MODE1 },// 0
 			{ MODE8, MODE7, MODE8, MODE7, MODE8 },// 1
 			{ MODE1, MODE7, MODE3, MODE6, MODE1 },// 2
 			{ MODE1, MODE7, MODE1, MODE7, MODE1 },// 3
			{ MODE2, MODE3, MODE7 },// 4
 			{ MODE1, MODE6, MODE1, MODE7, MODE1 },// 5
 			{ MODE1, MODE6, MODE1, MODE4, MODE1 },// 6
 			{ MODE1, MODE7, MODE8, MODE7, MODE8 },// 7
 			{ MODE1, MODE2, MODE1, MODE2, MODE1 },// 8
 			{ MODE1, MODE2, MODE1, MODE7, MODE1 } // 9
 
 	};
 	final String SPACE =" " ;
	public String getLCDNumberRepresentation(Integer number) throws NumberFormatException, IOException {
 
 		String representationOfTheNumberOnLCDWay = "";
 		char[] numbers = number.toString().toCharArray();
 		for (int i = 1; i <= 5; i++) {
 
 			for (char character : numbers) {
 				
				representationOfTheNumberOnLCDWay += howTheNumberisFormed[Integer.parseInt(String.valueOf(character))][(i-1)]
						+ SPACE;
 			}
 
 			representationOfTheNumberOnLCDWay += ENTER;
 		}
 
 		System.out.println("REPRESENTADO :");
 		System.out.println(representationOfTheNumberOnLCDWay);
 
 		return representationOfTheNumberOnLCDWay;
 
 	}
 
 }
