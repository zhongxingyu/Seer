 package homework;
 
 public class CreditCardNumberValidator {
     /** Return true if the card number is valid */
     public static boolean isValid(long number) {
         boolean valid = false;
         int sumEven   = sumOfDoubleEvenPlace(number);
         int sumOdd    = sumOfOddPlace(number);
         int size      = getSize(number);
         long prefix   = getPrefix(number, 1);
 
        if (prefix == 4L || prefix == 5L || prefix == 6L || prefixMatched(number, 37)) {
             if (size > 12 && size < 17) {
                 if ((sumOfDoubleEvenPlace(number) + sumOfOddPlace(number)) % 10 == 0) {
                     valid = true;
                 }
             }
         }
 
         return valid;
     }
 
     /** Get the result from Step 2 */
     public static int sumOfDoubleEvenPlace(long number) {
         int sum   = 0;
         int place = 1;
         String stringified = String.valueOf(number);
 
         for (int i = stringified.length()-1; i > -1; i--) {
             if (place % 2 == 0) {
                 sum += getDigit(Character.getNumericValue(stringified.charAt(i)) * 2);
             }
 
             place++;
         }
 
         return sum;
     }
 
     /** Return this number if it is a single digit, otherwise,
      *    * return the sum of the two digits */
     public static int getDigit(int number) {
         String parsed = String.valueOf(number);
         int val;
 
         if (parsed.length() > 1) {
             val = Integer.parseInt(parsed.substring(0, 1)) + Integer.parseInt(parsed.substring(1, 2));
         }
         else {
             val = number;
         }
 
         return val;
     }
 
     /** Return sum of odd-place digits in number */
     public static int sumOfOddPlace(long number) {
         int sum = 0;
         int place = 1;
         String stringified = String.valueOf(number);
         for (int i = stringified.length()-1; i > -1; i--) {
             if (place % 2 > 0) {
                 sum += Character.getNumericValue(stringified.charAt(i));
             }
 
             place++;
         }
 
         return sum;
     }
 
     /** Return true if the digit d is a prefix for number */
     public static boolean prefixMatched(long number, int d) {
         return String.valueOf(number).startsWith(String.valueOf(d));
     }
 
     /** Return the number of digits in d */
     public static int getSize(long d) {
         return String.valueOf(d).length();
     }
 
     /** Return the first k number of digits from number. If the
      * * number of digits in number is less than k, return number. */
     public static long getPrefix(long number, int k) {
        long prefix = number;

        if (number >= Math.pow(10, k)) {
            prefix = Long.parseLong(String.valueOf(number).substring(0, k));
        }

        return prefix;
     }
 }
