 /**
  * Computer Science 210 - Introduction to Computer Programming - Java
  * Chang-Shyh Peng <peng@clunet.org> <http://public.clunet.org/~peng>
  *
  * @author Erik Youngren <artanis.00@gmail.com>
  *
  */
 
 import java.util.Scanner;
 import javax.swing.JOptionPane;
 
public class ISBN_CLI {
     public static void main (String[] args) {
         System.out.println(
             "Please run either of the \"Text\" or \"Pane\" classes instead.");
         
        System.exit(0)
     }
     
     public static long get_isbn(boolean console) {
         String message = "Enter the first 9 digits of the ISBN";
         String error   = "Error: Input is not a number";
         
         long input = 0;
         
         try {
             if (console) {
                 Scanner in = new Scanner(System.in);
                 System.out.print(message + ": ");
                 input = in.nextLong();
             } else {
                 input = Long.parseLong(JOptionPane.showInputDialog(null,
                     message, "ISBN",
                     JOptionPane.QUESTION_MESSAGE));
             }
         } catch (java.lang.NumberFormatException e) {
             // JOptionPane bad input
             JOptionPane.showMessageDialog(null, error);
             System.exit(0);
         } catch (java.util.InputMismatchException e) {
             // Scanner bad input
             System.out.println(error);
             System.exit(0);
         }
         
         return input;
     }
     
     public static void display_isbn(String isbn, boolean console) {
         String output = String.format("The full ISBN is %s", isbn);
         if (console) {
             System.out.println(output);
         } else {
             JOptionPane.showMessageDialog(null, output);
         }
     }
     
     /**
      * Calculates the checksum of the given ISBN
      * Or any other number using the same algorithm...
      * 
      * @param long isbn number to check
      * 
      * @return String checksum
      */
     public static String checksum(long isbn) {
         // Going to use a non-destructive algorithm here.
         // No important data is discarded.
         
         // First, the result holder and the length of the ISBN.
         // I was determining the *actual* length of the ISBN, but
         // this introduced a bug in ISBNs with preceeding zeros.
         int sum = 0;
         int length = 9;
         
         // 1-index counting, so we also need to bump length up one.
         for(int i=1; i<length+1; i++) {
             // This is the multiplier of the iteration. Need this
             // because the loop starts at the least significant digit.
             // If I made the loop go down, I'd be inverting to find the
             // power I need.
             int multiplier = -i+length+1;
             
             // Here's the digit I'm working on this iteration.
             // Divide the ISBN by 10^i to cut off the lesser digits,
             // then grab the wanted digit with mod 10
             int digit = (int)(isbn/Math.pow(10,i-1)) % 10;
             
            // Extra step to enable debug
             int new_sum = sum + (digit*multiplier);
             
            System.out.printf(
                "%d: %4d = %-4d + (%d * %d)\n",
                i, new_sum, sum, digit, multiplier);
             
             sum = new_sum;
         }
         
         sum = sum % 11;
         
         // Convert to String, or replace with "X" as needed, and return
         return (sum < 10)? Long.toString(sum): "X";
     }
 }
