 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: michael
  * Date: 14/09/2013
  * Time: 14:43
  * To change this template use File | Settings | File Templates.
  */
 public class RomanNumeralGenerator {
 
     int numberAsInt;
     String numberAsString;
 
 public void convertAndDisplayNumber() {
 
    while (true) {
 
         System.out.println("Can you give me a number between 1 and 1,000 inclusive, or press 0 to stop the program?");
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         try {
             numberAsString = br.readLine();
         } catch (IOException e) {
             e.printStackTrace();
         }
         try {
             numberAsInt = Integer.parseInt(numberAsString);
         } catch (NumberFormatException nfe) {
             System.out.println("Not a valid number I'm afraid, please try again");
         }
 
         if (numberAsInt < 0) {
             System.out.println("Negative numbers not allowed.");
             convertAndDisplayNumber();
         }
 
         if (numberAsInt == 0) {
             System.exit(0);
         }
 
         if (numberAsInt > 1000) {
             System.out.println("That number is too large I'm afraid.");
             convertAndDisplayNumber();
         }
 
         TreeMap<Integer, String> romanNumerals = new TreeMap<Integer, String>();
         romanNumerals.put(1000, "M");
         romanNumerals.put(900, "CM");
         romanNumerals.put(500, "D");
         romanNumerals.put(400, "CD");
         romanNumerals.put(100, "C");
         romanNumerals.put(90, "XC");
         romanNumerals.put(50, "L");
         romanNumerals.put(40, "XL");
         romanNumerals.put(10, "X");
         romanNumerals.put(9, "IX");
         romanNumerals.put(5, "V");
         romanNumerals.put(4, "IV");
         romanNumerals.put(1, "I");
 
     int nextKeyDown = romanNumerals.floorKey(numberAsInt);
 
     while (true) {
 
     String nextCharDown = romanNumerals.get(nextKeyDown);
     System.out.print(nextCharDown);
    int remainder = numberAsInt - nextKeyDown;
 
         if (remainder > 0 ) {
 
     nextKeyDown = romanNumerals.floorKey(remainder);
     numberAsInt = remainder;
 
     }
 
         else {
             System.out.println();
             break;
         }
     }
 
   }
}
 
 
 
 
    public static void main(String[] arguments) {

        RomanNumeralGenerator rng = new RomanNumeralGenerator();
        rng.convertAndDisplayNumber();
 
     }
 }
 
 
 
 
 
 
 
 
 
