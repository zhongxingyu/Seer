 public class Solution {
     public int divide(int dividend, int divisor) {
         if (divisor == 0)
             throw new IllegalArgumentException("Invalid divisor 0");
         if (divisor == 1)
             return dividend;
         if (divisor == -1) {
             if (dividend == Integer.MAX_VALUE)
                 throw new IllegalArgumentException("Overflows!");
             return 0 - dividend;
         }
         boolean isNeg = false;
         long longDividend = dividend;
         long longDivisor = divisor;
 
         if (dividend < 0 && divisor > 0) {
            longDividend = 0 -(long) dividend;
             isNeg = true;
         }
         if (dividend > 0 && divisor < 0) {
            longDivisor = 0 - (long) divisor;
             isNeg = true;
         }
         if (dividend < 0 && divisor < 0) {
            longDividend = 0 - (long) dividend;
            longDivisor = 0 - (long) divisor;
         }
         int result = divideLong(longDividend, longDivisor).result;
         if (isNeg)
             return (0 - result);
         return result;
     }
 
     class DivideRes {
         int result;
         int left;
 
         public DivideRes(int result, int left) {
             this.result = result;
             this.left = left;
         }
     }
 
     private DivideRes divideLong(long dividend, long divisor) {
        if (dividend >= divisor + divisor) {
             DivideRes prev = divideLong(dividend, divisor + divisor);
             DivideRes curr = divideLong(prev.left, divisor);
             return new DivideRes(prev.result + prev.result + curr.result,
                     curr.left);
         } else if (dividend >= divisor) {
             return new DivideRes(1, (int) (dividend - divisor));
         } else
             return new DivideRes(0, (int) (dividend));
     }
 
  /*   public static void main(String[] args) {
         Solution sol = new Solution();
         System.out.println(sol.divide(2147483647, 2));
     }*/
 }
 
