 package common;
 
import utils.ExampleRunner;

 /**
  * Created with IntelliJ IDEA.
  * User: smalex
  * Date: 2/8/13
  * Time: 5:00 AM
  */
 public class Divisor {
   public static void main(String[] args) {
    ExampleRunner.eq(0, 2, new Divisor().divide(7, 3));
    ExampleRunner.eq(1, 1, new Divisor().divide(4, 3));
    ExampleRunner.eq(2, 5, new Divisor().divide(16, 3));
    ExampleRunner.eq(3, 17822, new Divisor().divide(53468, 3));
   }
 
   //  - Implement divide operator for two integers returning quotient, without using the divide operator
   //  7/3 = 2
   public int divide(final int p, final int q) {
     if (p == q) {
       return 1;
     }
     if (p < q) {
       return 0;
     }
 
     int res = 1;
     int tmpq = q;
     while (p >= tmpq) {
       tmpq <<= 1;
       res <<= 1;
     }
     tmpq >>= 1;
     res >>= 1;
    return res + divide(p - tmpq, q);
   }
 }
