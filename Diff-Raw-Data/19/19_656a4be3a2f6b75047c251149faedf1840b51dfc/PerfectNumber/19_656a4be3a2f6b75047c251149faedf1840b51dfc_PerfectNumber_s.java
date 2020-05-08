 /* John Martin
  * CS55
  * 03/14/13
  * PerfectNumber Assignment
  */
 
 
 public class PerfectNumber
 {
     
     public static void main(String[] args)
     {
         //  main method, while my count is less than 10, check if prime,
         //  if is prime, get its perfect number.
         int count = 0;
         while (count < 10)
         {
             if (isPrime(count))
             {
                 // invokes Math.round to get rid of the trailing '.0'
                 System.out.println(Math.round(getPerfect(count)));
             }
             count++;
         }
     }
 
    public static boolean isPrime(int n)
     {
         // returns boolean if n is a prime number
 
        if (n % 2 == 0) return false;
 
        for (int i=3; i*i <= n; i+=2)
         {
            if (n % i == 0) return false;
         }
         return true;
     }
 
    public static double getPerfect(int x)
     {
         // returns answer for perfect number algorithm 2**(x-1)(2**x - 1)
        double set_one = Math.pow(2, (x - 1));
        double set_two = Math.pow(2, x);
         double answer = set_one * (set_two - 1);
         return answer;
     }
 
 }
