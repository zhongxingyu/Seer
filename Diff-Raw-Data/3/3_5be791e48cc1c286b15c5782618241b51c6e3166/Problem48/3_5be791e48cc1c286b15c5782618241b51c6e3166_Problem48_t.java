 import java.math.BigInteger;
 
 /**
  * @author Anthony Gargiulo
  * @version 1.0
  *          Euler Project
  *          Problem 48
  *          ---------
  *          The series, 1^1 + 2^2 + 3^3 + ... + 10^10 = 10405071317.
  *          Find the last ten digits of the series, 1^1 + 2^2 + 3^3 + ... +
  *          1000^1000.
  */
 
 public class Problem48
 {
 
     /**
      * @param args
      */
     public static void main(String[] args)
     {
         BigInteger number;
         BigInteger sum = BigInteger.ZERO;
         for (int i = 1; i <= 1000; i++)
         {
             number = BigInteger.valueOf(i);
             sum = sum.add(number.pow(i));
         }
         String sumStr = sum.toString();
        System.out.println(sumStr.substring(sumStr.length() - 10));
     }
 }
