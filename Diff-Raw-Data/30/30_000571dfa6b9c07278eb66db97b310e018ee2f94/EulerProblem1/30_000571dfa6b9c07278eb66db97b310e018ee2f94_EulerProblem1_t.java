 // http://projecteuler.net/problem=1
 // Problem 1 
 // if we list all the natural numbers below 10 that are multiples of 3 or 5, we get 3, 5, 6 and 9. 
 //The sum of these multiples is 23.
 //Find the sum of all the multiples of 3 or 5 below 1000.
 
import java.util.*;
 
 public class EulerProblem1 {
     public static void main( String[] args ){
        boolean withTest = false;
         ArrayList<Integer> allMultiples = multiplesOf5and3WithSumLimitOf1000();
         System.out.println("multiples of 3 or 5 which sum is maximum 1000: " + allMultiples.toString() );
        System.out.println("The Sum is: " + sumDigits(allMultiples) );
 
         if ( withTest ) {
             testFunctions();
         }
     }
 
    public static Integer sumDigits (ArrayList<Integer> array){
        int sum = 0;
        for (int index = (array.size() -1 ); index >= 0 ; index-- ){
            sum = sum + array.get(index);
        }
        return sum;
    }

     public static ArrayList<Integer> multiplesOf5and3WithSumLimitOf1000 (){
         int sum = 0;
         ArrayList<Integer> multiples = new ArrayList<Integer>();
         int limit = 1000;
 
        for ( int index = limit; index > 0; index-- ) {
             if ( isThisNumberMultipleOf5(index) || isThisNumberMultipleOf3(index) ){
                if (index != 1000) {
                    sum = sum + index;
                     multiples.add( index );
                 }
             }
         }
 
         return multiples;
     }
 
     public static boolean isThisNumberMultipleOf5( int number ){
         if ( ( number % 5 ) == 0 ){
             return true;
         }
         else {
             return false;
         }
     }
 
     public static boolean isThisNumberMultipleOf3( int number ){
         if ( ( number % 3 ) == 0 ){
             return true;
         }
         else {
             return false;
         }
     }
 
     public static void testFunctions (){
         // tests isThisNumberMultipleOf5
         System.out.println(" ------------------------------------------------------------------ ");
         System.out.println("RUNING TESTS ...");
         System.out.println(" ------------------------------------------------------------------ ");
         System.out.println("Tests - isThisNumberMultipleOf5:");
         System.out.println(" ---------------------------------------- ");
         boolean A = isThisNumberMultipleOf5(10);
         System.out.println("is 10 multiple of 5?: " + A);
         boolean B = isThisNumberMultipleOf5(6);
         System.out.println("is 6 multiple of 5?: " + B);
         System.out.println(" ---------------------------------------- ");
 
 
         // tests isThisNumberMultipleOf3
         System.out.println("Tests - isThisNumberMultipleOf3:");
         System.out.println(" ---------------------------------------- ");
         boolean C = isThisNumberMultipleOf3(10);
         System.out.println("is 10 multiple of 3?: " + C);
         boolean D = isThisNumberMultipleOf3(6);
         System.out.println("is 6 multiple of 3?: " + D);
         System.out.println(" ---------------------------------------- ");
 
         // tests multiplesOf5and3WithSumLimit
         System.out.println("Tests - multiplesOf5and3WithSumLimitOf1000");
         System.out.println(" ---------------------------------------- ");
 
         ArrayList<Integer> ArrayOfMultiplesToGet = new ArrayList<Integer>();
         Collections.addAll( ArrayOfMultiplesToGet , 3,5,6,9,10,12,15,18,20,21,24,25,27,30,33,35,
                 36,39,40,42,45,48,50,51,54,55,57,60,63,65 );
         System.out.println( "list to get: " + ArrayOfMultiplesToGet.toString() );
 
         ArrayList<Integer> ArrayOfMultiplesReceived = new ArrayList<Integer>();
         ArrayOfMultiplesReceived = multiplesOf5and3WithSumLimitOf1000();
         System.out.println( "List received: " + ArrayOfMultiplesToGet.toString() );
 
         if ( ArrayOfMultiplesToGet.equals(ArrayOfMultiplesReceived) ) {
             System.out.println("correct!");
         }
         else {
             System.out.println("wrong result!");
         }
 
         System.out.println(" ---------------------------------------- ");
     }
 
 }
