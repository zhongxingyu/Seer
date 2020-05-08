 package containers.p39;
 
 public class PrimeNumber {
 
     public static int getNextPrimeNumberAfter(int number) {
        if (number < 1) {
            throw new IllegalArgumentException("The argument must be greater than 0. The argument is: " + number);
         }
         int size;
         if (number > (int) (Integer.MAX_VALUE * 0.9)) {
            size = Integer.MAX_VALUE;
         } else {
             size = (int) (number * 1.1) + 1;

         }
         boolean[] ints = new boolean[size + 1];
         int factor = (int) Math.floor(Math.sqrt(size));
         for (int i = 2; i <= factor; i++) {
            for (int j = i * 2; j < size; j += i) {
                 ints[j] = true;
             }
         }
         for (int i = number + 1; i < ints.length; i++) {
             if (!ints[i]) {
                 return i;
             }
         }
         return getNextPrimeNumberAfter(size);
     }
 
     public static void main(String[] args) {
         for (int i = 1; i < 30; i++) {
             System.out.println("Next prime number after " + i + " is " + getNextPrimeNumberAfter(i));
         }
 
        int n = 10;
         long start = System.currentTimeMillis();
         System.out.println("Next prime number after " + n + " is " + getNextPrimeNumberAfter(n));
         long total = System.currentTimeMillis() - start;
         System.out.println("Time taken " + total + " ms");
     }
 }
