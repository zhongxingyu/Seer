 package util;
 
 /**
  * This class has a static utility method used for converting an 
  * <code>int</code> to a <code>String</code> representing the number
  * in roman numerals.
  * @author dagen
  */
 public class RomanNumber {
     /**
      * Converts a number greater than or equal to 1 to a string formated 
      * as the corresponding roman numeral. Roman numerals is not good for 
      * representing numbers greater than a couple of thousands.
      * <br /><br />
      * Examples: <br />
      * ! => I
      * 2 => II
      * 3 => III
      * 4 => IV
      * 5 => V
      * ...
      * 10 => X
      * 50 => L
      * 100 => C
      * 500 => D
      * 1000 => M
      * <br/><br/>
      * 994 => CMXCIV
      * @param number number to convert. Must be positive. To great numbers will 
      * yeild long strings.
      * @return Roman numeral
      */
     public static String romanNumberString(int number){
         if(number <= 0 ){
             return "";
         }
         int countDown = number;
         StringBuilder romanNumberBuilder = new StringBuilder();
         int[] onesNumbers = {1000,100,10,1};
         char[] onesChars = {'M','C','X','I'};
         int[] fivesNumbers = {500,50,5};
         char[] fivesChars = {'D','L','V'};
         
         for(int j=0; j<=3; j++){
             // Fill string with M, C, X or I until countDown is small enough.
             while (countDown >= onesNumbers[j]) {
                 romanNumberBuilder.append(onesChars[j]);
                 countDown -= onesNumbers[j];
             }
             
             // If countDown is zero we are done.
             // The break is nessesary if the string was just filled with I
             // to avoid index out of bounds.
             if(countDown == 0){
                 break;
             }
             
             // Checks for the case of nines which yields IX, XC or CM
             if (countDown >= onesNumbers[j] - onesNumbers[j + 1]) 
             {
                 romanNumberBuilder.append(onesChars[j + 1]);
                 romanNumberBuilder.append(onesChars[j]);
                 countDown -= (onesNumbers[j] - onesNumbers[j + 1]);
             }
             // Checks for the case of fives which yields V, L or D
             else if (countDown >= fivesNumbers[j]) 
             {
                 romanNumberBuilder.append(fivesChars[j]);
                 countDown -= fivesNumbers[j];
             } 
             // Checks for the case of fours which yields IV, XL or DM
            else if (countDown == fivesNumbers[j] - onesNumbers[j + 1]) 
             {
                 romanNumberBuilder.append(onesChars[j + 1]);
                 romanNumberBuilder.append(fivesChars[j]);
                 countDown -= fivesNumbers[j] - onesNumbers[j + 1];
             }
             // Other cases are handled in the filling stage at 
             // the top of the loop.
         }       
         return romanNumberBuilder.toString();
     }
     
     /*
     /**
      * Main method for demonstration purposes
      * @param args 
      *
     public static void main(String []args){
         for(int i=1;i<10000;i++){
             System.out.println(RomanNumber.romanNumberString(i));
         }
     }
     */
 }
 
