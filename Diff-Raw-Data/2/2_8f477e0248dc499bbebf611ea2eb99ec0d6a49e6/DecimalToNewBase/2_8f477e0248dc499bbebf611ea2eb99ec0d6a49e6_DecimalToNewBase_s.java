 public class DecimalToNewBase {
     private int decimal;
 
     public DecimalToNewBase(int num) {
         decimal = num;
     }
 
     public String DecToN(int base) {
        return performBaseConversion(base, decimal / base, decimal / base);
     }
 
     private String performBaseConversion(int base, int quotient, int remainder) {
         if (quotient < 1) {
             return integerToLetter(remainder);
         } else {
             return performBaseConversion(base, quotient / base, quotient % base) + integerToLetter(remainder);
         }
     }
 
     public String decToBaseTwo() {
         return (decToBaseTwo(decimal / 2, decimal % 2));    
     }
     private String decToBaseTwo(int quotient, int remainder) {
         //System.out.println("fuck: " + quotient + " | " + remainder);
         if (quotient < 1) {
             //System.out.println("break: " + quotient + " | " + remainder);
             return integerToLetter(remainder);
         } else {
             return decToBaseTwo(quotient / 2, quotient % 2) + integerToLetter(remainder);
             //System.out.println("continue: " + quotient/2 + " | " + remainder%2);
         }
     }
     public String decToBaseThree() {
         return (decToBaseThree(decimal / 3, decimal % 3));
     }
     private String decToBaseThree(int quotient, int remainder) {
         if (quotient < 1) {
             return integerToLetter(remainder);
         } else {
             return decToBaseThree(quotient / 3, quotient % 3) + integerToLetter(remainder);
         }
     }
     public String decToBaseFive() {
         return (decToBaseFive(decimal / 5, decimal % 5));
     }
     private String decToBaseFive(int quotient, int remainder) {
         if (quotient < 1) {
             return integerToLetter(remainder);
         } else {
             return decToBaseFive(quotient / 5, quotient % 5) + integerToLetter(remainder);
         }
     }
     public String decToBaseEight() {
         return (decToBaseEight(decimal / 8, decimal % 8));
     }
     private String decToBaseEight(int quotient, int remainder) {
         if (quotient < 1) {
             return integerToLetter(remainder);
         } else {
             return decToBaseEight(quotient / 8, quotient % 8) + integerToLetter(remainder);
         }
     }
     public String decToBaseNine() {
         return (decToBaseNine(decimal / 9, decimal % 9));
     }
     private String decToBaseNine(int quotient, int remainder) {
         if (quotient < 1) {
             return integerToLetter(remainder);
         } else {
             return decToBaseNine(quotient / 9, quotient % 9) + integerToLetter(remainder);
         }
     }
     public String decToBaseTwelve() {
         return (decToBaseTwelve(decimal / 12, decimal % 12));
     }
     private String decToBaseTwelve(int quotient, int remainder) {
         if (quotient < 1) {
             return integerToLetter(remainder);
         } else {
             return decToBaseTwelve(quotient / 12, quotient % 12) + integerToLetter(remainder);
         }
     }
     public String decToBaseSixteen() {
         return (decToBaseSixteen(decimal / 16, decimal % 16));
     }
     private String decToBaseSixteen(int quotient, int remainder) {
         if (quotient < 1) {
             return integerToLetter(remainder);
         } else {
             return decToBaseSixteen(quotient / 16, quotient % 16) + integerToLetter(remainder);
         }
     }
     private String integerToLetter(int integer) {
         if (integer < 10)
             return String.valueOf(integer);
         else if (integer == 10) 
             return "A";
         else if (integer == 11)
             return "B";
         else if (integer == 12)
             return "C";
         else if (integer == 13)
             return "D";
         else if (integer == 14)
             return "E";
         else if (integer == 15)
             return "F";
         else if (integer == 16)
             return "G";
         else if (integer == 17)
             return "H";
         else if (integer == 18)
             return "I";
         else if (integer == 19)
             return "J";
         else if (integer == 20)
             return "K";
         else if (integer == 21)
             return "L";
         else if (integer == 22)
             return "M";
         else if (integer == 23)
             return "N";
         else if (integer == 24)
             return "O";
         else if (integer == 25)
             return "P";
         else if (integer == 26)
             return "Q";
         else if (integer == 27)
             return "R";
         else if (integer == 28)
             return "S";
         else if (integer == 29)
             return "T";
         else if (integer == 30)
             return "U";
         else if (integer == 31)
             return "V";
         else if (integer == 32)
             return "W";
         else if (integer == 33)
             return "X";
         else if (integer == 34)
             return "Y";
         else if (integer == 35)
             return "Z";
         else return "OMGWTFBBQ";
 
     }
 }
