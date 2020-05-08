 import java.util.*;
 import java.io.*;
 
public class linkedlist
 {
     public static void main( String[] args )
     {
         LinkedList<Integer> list = new LinkedList<Integer>();
         int num1 = 11, num2 = 22, num3 = 33, num4 = 44;
 
         list.add( num2 );
         list.add( num3 );
         list.addLast( num4 );
         list.addFirst( num1 );
         list.addFirst( 1234 );
 
         System.out.println( Arrays.toString( list.toArray() ) );
 
         if ( list.isEmpty() )
             System.out.println( "List is empty" );
         else
             System.out.printf( "Size: %d\n", list.size() );
     }
 }
