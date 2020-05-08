 //Opgave 2.4, bogen side 137
 package tempconverter;
 import java.util.Scanner;
 
 /**
  *
  * @author Kim Vammen
  */
 public class TempConverter
 {
     public static void main(String[] args)
     {
         final int BASE = 32; 
         final double CONVERSION_FACTOR = 9.0 / 5.0;
         
         double fahrenheitTemp;
         String celsiusTemp;
         
        System.out.println("Enter the temperature in celsius: ");
         
         Scanner scan = new Scanner (System.in);
         celsiusTemp = scan.nextLine();
         int ctemp = Integer.parseInt(celsiusTemp);
         
         System.out.println("You entered the following temerature in celsius: " + ctemp);
         
         fahrenheitTemp = ctemp * CONVERSION_FACTOR + BASE;
         
         System.out.println("Celsius Temperature: " + celsiusTemp);
         System.out.println("Fahrenheit Equivalent: " + fahrenheitTemp);
     }
 }
 
 
