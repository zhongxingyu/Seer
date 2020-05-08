 
 package bbc;
 
 import bbc.RomanNumeralGenerator;
 
 public class RomanNumeral implements RomanNumeralGenerator {
 
     /* This class is used to hold the roman symbols and the value
        that each one represents.
     */
     private class RomanSymbol {
         int value;
         String symbol;
         public RomanSymbol () {
             value = 0;
             symbol = "";
         };
         public RomanSymbol (int i, String s) {
             value = i;
             symbol = s;
         };
     };
 
     /* These are the variables that are used to represent the
        current string, how much it is worth and how much it needs
        to be worth eventually.
     */
     private String output;
     private int currentValue;
     private int targetValue;
 
     /* Constructor - make everything new.
     */
     public RomanNumeral () {
         currentValue = 0;
         targetValue = 0;
         output = new String();
     }
 
     /* This is an array containing the symbols that we are allowed
        to use and their associated values
     */
     private RomanSymbol possibleLetters[] = {
         new RomanSymbol( 1000, "M" ),
         new RomanSymbol( 900, "CM" ),
         new RomanSymbol( 500, "D" ),
         new RomanSymbol( 400, "CD" ),
         new RomanSymbol( 100, "C" ),
         new RomanSymbol( 90, "XC" ),
         new RomanSymbol( 50, "L" ),
         new RomanSymbol( 40, "XL" ),
         new RomanSymbol( 10, "X" ),
         new RomanSymbol( 9, "IX" ),
        new RomanSymbol( 5, "5" ),
         new RomanSymbol( 4, "IV" ),
         new RomanSymbol( 1, "I" )
     };
 
     /* This function attempts to add the largest value symbol that it can
        to our output string. If it cannot add a symbol, it returns false
        to indicate that the output is ready.
     */
     private boolean addSymbol() {
         for (int i = 0; i < possibleLetters.length; i++)
         {
             if (currentValue + possibleLetters[i].value <= targetValue)
             {
                 currentValue += possibleLetters[i].value;
                 output += possibleLetters[i].symbol;
                 return true;
             }
         }
         return false;
     }
 
     /* The generate function sets up the target value and calls the 
        AddSymbol function until the output is ready, indicated by a
        return of false.
     */
     public String generate(int target) {
         targetValue = target; 
         while (addSymbol() == true) {
             /* enable for debugging 
             System.out.println( "Current String is: "+output);
             System.out.println( "Current Value is: "+currentValue); 
             */
         }
         return output;
     }
 
     /* This is the function that I used to test the class.
     */
     public static void main(String[] args)
     {
         if (args.length < 1)
         {
             System.out.println("Please indicate the number you wish to convert to Roman Numerals");
             System.exit(1);
         }
         int numberToConvert = 0;
         try
         {
             numberToConvert = Integer.parseInt(args[0]);
         }
         catch (Exception e)
         {
             System.out.println("Please indicate the number you wish to convert as a decimal string e.g. 1649");
         }
         if ((numberToConvert < 1) ||
             (numberToConvert > 3999))
         {
             System.out.println("Please choose a number between 0 and 3999.");
             System.exit(2);
         }
         RomanNumeral rs = new RomanNumeral();
         System.out.println("Your number is: "+rs.generate(numberToConvert));
         System.exit(0); 
     }
 }
 
