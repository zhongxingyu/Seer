 public class CCV
 {
     public static void main(String[] args)
     {
         long lng = 3734123412341234L;
         if (isValid(lng))
         {
             System.out.println(sumOfDoubleEvenPlace(lng));
         } else {
             System.out.println("losing");
         }
     }
 
     public static boolean isValid(long number)
     {
         // convert to string and check length
 
         if (getSize(number) < 13 || getSize(number) > 16)
         {
             return false;
         }
         
         if (prefixMatched(number, 4) ||
             prefixMatched(number, 5) ||
             prefixMatched(number, 6) ||
             prefixMatched(number, 37)) {
             return true;
         }
 
         return false;
         
         
     }
 
     public static int sumOfDoubleEvenPlace(long number)
     {
        //total = 0
         int total = 0;
        // for x in number.length()
        for (int x = Long.toString(number).length() - 1; x > 0; x--)
         {
             if (x % 2 == 0)
             {
                 System.out.println(x);
                 String digit = Long.toString(number).substring(x - 1, x);
                 total += getDigit(Integer.parseInt(digit));
          // num *= 2;
          // if num.length() == 2
            // a = num.substring(0,1)
            // b = num.substring(1,2)
            // num = a + b
          // total += num
             }
         }
         return total;
 
     }
 
     public static int getDigit(int number)
     {
         number *= 2;
 
         if (Integer.toString(number).length() == 2)
         {
             int d1 = Integer.parseInt(Integer.toString(number).substring(0, 1));
             int d2 = Integer.parseInt(Integer.toString(number).substring(1, 2));
             return d1 + d2;
         }
         return number;
     }
 /*
     public static int sumOfOddPlace(long number)
     {
     }
 */
     public static boolean prefixMatched(long number, int d)
     {
         if (getPrefix(number, 1) == d) return true;
         if (getPrefix(number, 2) == d) return true;
         return false;
     }
 
     public static int getSize(long d)
     {
         int length = Long.toString(d).length();
         return length;
     }
 
     public static int getPrefix(long number, int k)
     {
         String prefix = Long.toString(number).substring(0, k);
         return Integer.parseInt(prefix);
     }
 
 }
