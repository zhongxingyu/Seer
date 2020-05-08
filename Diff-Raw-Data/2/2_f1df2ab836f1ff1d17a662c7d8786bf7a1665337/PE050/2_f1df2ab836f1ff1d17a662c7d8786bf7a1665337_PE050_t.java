 public class PE050
 {
     public static void main(String[] args)
     {
         int[] a = new int[100000];
         int t = 0;
         for(int i = 2; i < 1000000; i++) {
             if(isPrime(i)) {
                 a[t++] = i;
             }
         }
         int length = 0;
         int result = 0;
         for(int i = 0; i < t; i++) {
             int sum = 0; 
            for(int j = i; j < t; j++) {
                 sum += a[j];
                 if(sum > 1000000)
                     break;
                 if(isPrime(sum) && j-i+1 > length) {
                     length = j-i+1;
                     result = sum;
                 }
             }
         }
         System.out.println(result);
     }
 
     private static boolean isPrime(int n)
     {
         int k = (int)Math.sqrt(n);
         for(int i = 2; i <= k; i++) {
             if(n%i == 0)
                 return false;
         }
         return true;
     }
 }
