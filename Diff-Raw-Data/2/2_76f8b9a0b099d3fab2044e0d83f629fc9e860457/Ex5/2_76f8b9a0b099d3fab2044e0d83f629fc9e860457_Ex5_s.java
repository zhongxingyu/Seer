 import java.util.Scanner;
 
 public class Ex5 {
     public static void main(String[] args) {
         Scanner scan = new Scanner(System.in);
 
         int input = scan.nextInt();
 
         // Dismantles the current integer given as input to its primes, and prints them.
         for (int i=2; i<input; i++) {
             while(input%i == 0) {
                 System.out.println(i);
 
                 input = input/i;
             }
         }
 
        // Don't print the special case where the last prime is 1 calculated is 1.
         if (input != 1) {
             System.out.println(input);
         }
     }
 }
