 /*
 * http://projecteuler.net/problem=9
 *
  * A Pythagorean triplet is a set of three natural numbers, a < b < c,
  * for which,
  *
  *      a^2 + b^2 = c^2
  *
  * For example, 3^2 + 4^2 = 9 + 16 = 25 = 5^2.
  *
  * There exists exactly one Pythagorean triplet for which
  * a + b + c = 1000.
  *
  * Find the product abc.
  *
  */
 
 class ProjectEulerP9 {
     /* This function returns whether the number passed to it
      * is a perfect square */
     public static boolean IsPerfectSquare(double Product) {
         /* Get the floor of the square root of the number */
         double I = Math.floor(Math.sqrt(Product));
 
         /* If the number is equal to the square of the floor of
          * its square root, return true */
         if(I * I == Product) {
             return true;
         }
         else {
             return false;
         }
     }
 
     public static void main(String[] args) {
         double Product = 0, Result = 0;
 
         for(double I = 1; I < 1000; I++) {
             for(double J = 1; J < 1000; J++) {
                 Product = (I*I) + (J*J);
 
                 if(IsPerfectSquare(Product) == true) {
                     if(I + J + Math.sqrt(Product) == 1000) {
                         Result = I * J * Math.sqrt(Product);
                         I = 1000;
                         J = 1000;
                     }
                 }
             }
         }
 
         System.out.format("%d%n", (int)Result);
 
         return;
     }
 }
