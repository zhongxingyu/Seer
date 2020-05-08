 package algorithms;
 
 public class Math {
     public static void main (String[] args) {
         System.out.println ("Test pow..");
         System.out.println (pow(0,0));
         System.out.println (pow(1,5));
         System.out.println (pow(5,2));
         System.out.println ();
         
         System.out.println ("Test abs..");
         System.out.println (abs(0));
         System.out.println (abs(1.5));
         System.out.println (abs(-2));
         System.out.println ();
         
         System.out.println ("Test sqrt..");
         System.out.println ("1 : " + sqrt(1));
         System.out.println ("2 : " + sqrt(2));
         System.out.println ("3 : " + sqrt(3));
         System.out.println ("4 : " + sqrt(4));
         System.out.println ("9 : " + sqrt(9));
         System.out.println ("16 : " + sqrt(16));
         System.out.println ("25 : " + sqrt(25));
         System.out.println ("100 : " + sqrt(100));
         System.out.println ("1000 : " + sqrt(1000));
         System.out.println ("10000 : " + sqrt(10000));
         System.out.println ();
         
         System.out.println ("Test isPrime..");
         System.out.println ("1 : " + isPrime(1));
         System.out.println ("2 : " + isPrime(2));
         System.out.println ("3 : " + isPrime(3));
         System.out.println ("14 : " + isPrime(14));
         System.out.println ("17 : " + isPrime(17));
         System.out.println ("37 : " + isPrime(37));
         System.out.println ("36 : " + isPrime(36));
         System.out.println ();
         
         System.out.println ("Test nextPrime..");
         System.out.println ("1 : " + nextPrime(1));
         System.out.println ("2 : " + nextPrime(2));
         System.out.println ("3 : " + nextPrime(3));
         System.out.println ("4 : " + nextPrime(4));
         System.out.println ("10 : " + nextPrime(10));
         System.out.println ("100 : " + nextPrime(100));
         System.out.println ("10000 : " + nextPrime(10000));
         System.out.println ();
         
         System.out.println ("Test gcd..");
         System.out.println ("1\t2:\t" + gcd(1,2));
         System.out.println ("5\t2:\t" + gcd(5,2));
         System.out.println ("36\t48:\t" + gcd(36,48));
         System.out.println ("48\t36:\t" + gcd(48,36));
         System.out.println ("37\t111:\t" + gcd(37,111));
         System.out.println ("111\t37:\t" + gcd(111,37));
         System.out.println ();
     }
     
     public static double pow (double n, double p) {
     	if (p == 0)
     		return 1;
     	return n * pow (n, p-1);
     }
     
     public static double abs (double n) {
         return (n >= 0)? n : -n;
     }
     
     public static double sqrt (double n) {
         if (n < 0)
             return 0;
         double THRESH = 0.0001;
         double estimate = n/2, last = n, original = n;
         while (abs(last-estimate) > THRESH) {
             last = estimate;
             estimate = (estimate + original/estimate)/2;
         }
         return estimate;
     }
     
     public static boolean isPrime (int n) {
         if (n < 2)
             return false;
         if (n < 4)
             return true;
         boolean isPrime = true;
         int upperCheck = (int)(sqrt(n)) + 1;
         for (int i = 2; i <= upperCheck; ++i)
             if (n % i == 0) {
                 isPrime = false;
                 break;
             }
         return isPrime;
     }
     
     public static int nextPrime (int n) {
         int current = n+1;
         while (!isPrime(current))
             ++current;
         return current;
     }
     
     public static int gcd (int x, int y) {
         int mod = x%y;
         return (mod == 0)? y : gcd (y, mod);
     }
    
    public static double random () {
        return java.lang.Math.random();
    }
 
 }
