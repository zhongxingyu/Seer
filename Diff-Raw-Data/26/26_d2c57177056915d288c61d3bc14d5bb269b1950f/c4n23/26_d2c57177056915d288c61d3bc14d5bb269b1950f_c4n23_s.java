 // Aaron Zeng 20120307
 // Chapter 04 Exercise 23
 
 import java.util.*;
 import java.io.*;
 
 public class c4n23
 {
     public static void main( String[] args )
     {
         // declare and initialize variables
         Scanner input = new Scanner( System.in );
        int counter = 1, number, max1, max2; // max1 is largest, max2 second
 
         // repeat ten-fold
         while ( counter <= 10 )
         {
             // prompt for and accept number
             System.out.println( "Enter number: " );
             number = input.nextInt();
 
             // if this is the first time through, initialize max1 and max2
             if ( counter == 1 )
             {
                 max1 = number;
                 max2 = number;
             }
 
             // change max1 or max2 if necessary
             if ( number > max1 )
             {
                 max2 = max1; // largest becomes second-largest
                 max1 = number;
             }
             else if ( number > max2 )
                 max2 = number;
 
             // increment counter
             counter++;
         }
 
         // print out largest and second-largest
         System.out.printf( "Largest: %d\n", max1 );
         System.out.printf( "Second-largest: %d\n", max2 );
     }
 }
