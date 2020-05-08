 /*
  * Basic IO and simple int arithmetic
  */
 public class Sample1
 {
     public static void main( String[] args )
     {
         int i = 0;
         int j = 0;
 
         i = SimpleIO.readInt();                // get i
         j = 9 + i * 8;                         // evaluate j
         SimpleIO.printString( "Result is " );  // print the label
         SimpleIO.printInt( j );                // print j
         SimpleIO.println();                    // print the newline
         return;                                // return from method (last stmt)
     }
 }
 
 /*
  * Basic IO and simple mixed type arithmetic
  */
 public class Sample1R
 {
     public static void main( String[] args )
     {
         float x = 0.0f;
         float z = 0.0f;
         int   y = 0;
 
 
         x = SimpleIO.readFloat();          // read x
         z = 9 + x * 8;                     // assign to z (mixed types)
         y = (int) (9 + x * 8);             // assign to y with a cast
 
         SimpleIO.printString( "Result (int) is " );
         SimpleIO.printInt( y );            // print y
         SimpleIO.println();                // print newline
         SimpleIO.printString( "Result (float) is " );
         SimpleIO.printFloat( z );          // print z 
         SimpleIO.println();                // print newline
         return;                            // return from method (last stmt)
     }
 }
 
 /*
  * Basic method invocation and a looping statement
  */
 public class Sample2
 {
     static int count( int n )
     {
         int i = 0;
         int sum = 0;
 
         i = 1;
         sum = 0;
         while ( i <= n ) {
             sum = sum + i;
             i = i + 1;
         }
         return sum;
     }
 
     public static void main( String[] args )
     {
         int i = 0;
         int sum = 0;
 
         i = SimpleIO.readInt();    // read i
         sum = count( i );          // call count
         SimpleIO.printInt( sum );  // print the result
         SimpleIO.println();        // print the newline
         return;                    // return from method (last stmt)
     }
 }
 
 /*
  * recursive factorial calculation
  */
 
 public class FactorialRec
 {
 
     static int n = 0;
     static int fact = 0;
 
     static int factorial( int n )
     {
         int retValue = 1;
 
         if ( n <= 1 )
             retValue = 1;
         else
             retValue = n * factorial( n - 1 );
 
         return retValue;
     }
 
     public static void main( String[] args )
     {
 
         SimpleIO.printString( "Enter an integer: " );
         n =  SimpleIO.readInt();                  // read n
         fact = factorial( n );                    // call factorial
         SimpleIO.printString( "Factorial of " );
         SimpleIO.printInt( n );                   // print n
        SimpleIO.printString( " = " );
         SimpleIO.printInt( fact );                // print the result
         SimpleIO.println();
         return;                                   // return from method (last stmt)
     }
 }
