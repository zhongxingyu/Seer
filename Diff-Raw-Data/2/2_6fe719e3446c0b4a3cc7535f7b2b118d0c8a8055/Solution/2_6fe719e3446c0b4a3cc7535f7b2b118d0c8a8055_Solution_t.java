 import java.lang.Math;
 
 public class Solution {
     public double pow(double x, int n) {
         if ( Math.abs(x - 1) < 1e-6) {
             return 1;
         } else if ( Math.abs(x + 1) < 1e-6) {
            return Math.abs(n) % 2 == 0 ? 1 : -1;
         }
         
         if (n < 0) {
             return 1 / pow(x, -n);
         } else if (n == 0) {
             return 1;
         } else if (n % 2 == 0) {
             double val = pow(x, n/2);
             return val * val;
         } else {
             double val = pow(x, n/2);
             return val * val * x;    
         }
     }
 }
