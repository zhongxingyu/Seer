 /*
 * http://projecteuler.net/problem=10
 *
  * The sum of the primes below 10 is 2 + 3 + 5 + 7 = 17.
  *
  * Find the sum of all the primes below two million.
  *
  */
 
 class ProjectEulerP10 {
     /* This function determines if an odd number is prime */
     public static boolean IsPrime(int Number) {
         /* Check every odd number until the square root of
          * Number is reached */
         int J = (int)Math.floor(Math.sqrt(Number));
         for(int I = 3; I <= J; I+= 2) {
             if(Number % I == 0) {
                 return false;
             }
         }
 
         return true;
     }
 
     public static void main(String[] args) {
         long Sum = 5;
 
         /* Add all the primes under 2,000,000 */
         for(int I = 5; I < 2000000; I += 2) {
             if(IsPrime(I) == true) {
                 Sum += I;
             }
         }
 
         /* Print the answer */
         System.out.format("%d%n", Sum);
 
         return;
     }
 }
