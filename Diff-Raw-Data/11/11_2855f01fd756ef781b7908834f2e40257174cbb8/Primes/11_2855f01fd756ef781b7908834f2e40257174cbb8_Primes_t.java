 import java.lang.System;
 
 public class Primes {
     private int[] sieve;
     private int max;
 
     public Primes(int max) 
     {
         this.sieve = new int[max+1];
         int i = 2;
         while (max + 1 > i)
         {
             this.sieve[i] = 1;
             i+=1;
         }
         this.max = max;
     }
 
     public void erastosthene() {
         int i = 2;
         int j;
 
         while (this.max+1 > i*i)
         {
             if (this.sieve[i] == 1)
             {
                 j = i+i;
                 while (this.max +1 > j)
                 {
                     this.sieve[j] = 0;
                     j+=i;
                 }
             }
             i+=1;
         }
 
         i = 2;
         while (this.max + 1 > i)
         {   
             if (this.sieve[i] == 1)
                 System.out.print(i + " ");
             i+=1;
         }
         System.out.println("");
     }
 
     public static int toInt(String str) {
         int len = str.length();
         int pos = 0;
         int res = 0;
         while (len > pos) {
             if (str.charAt(pos) == '0') 
             {
                 res = res* 10;
             }
             else if (str.charAt(pos) == '1') 
             {
                 res = res* 10;
                 res+= 1;
             }
             else if (str.charAt(pos) == '2') 
             {
                 res = res* 10;
                 res+= 2;
             }
             else if (str.charAt(pos) == '3') 
             {
                 res = res* 10;
                 res+= 3;
             }
             else if (str.charAt(pos) == '4') 
             {
                 res = res* 10;
                 res+= 4;
             }
             else if (str.charAt(pos) == '5') 
             {
                 res = res* 10;
                 res+= 5;
             }
             else if (str.charAt(pos) == '6') 
             {
                 res = res* 10;
                 res+= 6;
             }
             else if (str.charAt(pos) == '7') 
             {
                 res = res* 10;
                 res+= 7;
             }
             else if (str.charAt(pos) == '8') 
             {
                 res = res* 10;
                 res+= 8;
             }
             else if (str.charAt(pos) == '9') 
             {
                 res = res* 10;
                 res+= 9;
             }
             else 
                 return 0;
             
             pos+=1;
         }
         return res;
     }
 
 
     public static void main(String[] args) {
        if (args.length() == 0) {
            System.out.println("Missing argument");
            return;
        }
         int i = Primes.toInt(args[0]);
         Primes primes = new Primes(i);
         primes.erastosthene(); 
     }
 
 
 }
