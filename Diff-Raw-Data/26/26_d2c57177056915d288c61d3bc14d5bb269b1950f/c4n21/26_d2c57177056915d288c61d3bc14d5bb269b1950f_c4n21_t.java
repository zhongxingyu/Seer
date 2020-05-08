 // Aaron Zeng 20120307
 // Chapter 04 Exercise 21
 
 import java.util.*;
 import java.io.*;
 
 public class c4n21
 {
     public static void main( String[] args )
     {
         // declare variables
         Scanner input = new Scanner( System.in );
        int counter = 2, number, largest;

        // prompt for and accept number
        System.out.print( "Enter number: " );
        number = input.nextInt();
        largest = number;
 
         // loop this ten times
         while ( counter <= 10 )
         {
             // prompt for and accept number
             System.out.print( "Enter number: " );
             number = input.nextInt();
 
             // if this number is larger than largest, change largest
             if ( number > largest )
                 largest = number;
 
             // increment counter
             counter++;
         }
 
         // print out the largest
         System.out.printf( "The largest number is %d.\n", largest );
     }
 }
