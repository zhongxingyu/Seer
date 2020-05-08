 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.pitt.isp.sverchkov.math;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Objects;
 
 /**
  *
  * @author YUS24
  */
 public class MathTools {
     
     // Public constants
     public static final MathDoubleOp ADD = new Add();
     public static final MathDoubleOp MULTIPLY = new Multiply();
     
     // Private static caching variables
     private static final List<Double> logFactorials = new ArrayList<>( Arrays.asList( 0.0, 0.0 ) );
     
     // Public static functions
     
     /**
      * Sums a (presumably finite) iterable of doubles.
      * @param nums the things to sum
      * @return the sum as a double
      * @throws NullPointerException if any element in nums is null.
      */
     public static double sum( Iterable<? extends Number> nums ){
         double sum = 0;
         for( Number num : nums ) sum += num.doubleValue();
         return sum;
     }
     
     /**
      * Sums a (varargs) array of <tt>int</tt>s.
      * @param array of <tt>int</tt>s
      * @return the sum
      */
     public static int sum( int... array ){
         int sum = 0;
         for( int n : array ) sum += n;
         return sum;
     }
     
     public static double sum( final double[] array, final int firstIndex, final int length ){
        double sum = 0;
         for( int i = firstIndex; i < firstIndex+length; i++ )
             sum += array[i];
         return sum;
     }
     
     public static double sum( double... array ){
         return sum( array, 0, array.length );
     }
     
     /**
      * Memoized ln( n! )
      * @param n
      * @return ln( n! )
      */
     public static double lnFactorial( int n ){
         int last = logFactorials.size()-1;
         if( n > last ){
             double lf = logFactorials.get( last );
             while( last < n )
                 logFactorials.add( lf += Math.log( ++last ) );
             return lf;
         }
         return logFactorials.get(n);
     }
     
     /**
      * Returns ln( x + y ) given ln x and ln y. More numerically stable than ln( exp( lnX ) + exp( lnY ) ).
      * @param lnX ln x
      * @param lnY ln y
      * @return ln( x + y )
      */
     public static double lnXplusY( double lnX, double lnY ){
         if( Double.isNaN(lnX) || Double.isNaN(lnY) ) return Double.NaN;
         if( lnX == Double.NEGATIVE_INFINITY ) return lnY;
         if( lnY == Double.NEGATIVE_INFINITY ) return lnX;
         if( lnX == Double.POSITIVE_INFINITY || lnY == Double.POSITIVE_INFINITY ) return Double.POSITIVE_INFINITY;
         // wlog x <= y
         if( lnX > lnY ) return lnXplusY( lnY, lnX );
         // ln( x + y ) = ln(x) + ln( 1 + exp( ln(y) - ln(x) ) )
         return lnX + Math.log1p( Math.exp( lnY - lnX ) );
     }
     
     public static double[] arrayOperate( double[] a, MathDoubleOp op, double... b ){
         Objects.requireNonNull( a, "'a' cannot be null." );
         Objects.requireNonNull( b, "'b' cannot be null." );
         if( b.length == 1 ){
             double[] result = new double[a.length];
             for( int i = 0; i < a.length; i++ )
                 result[i] = op.operate(a[i], b[0]);
             return result;
         }
         if( a.length == 1 ){
             double[] result = new double[b.length];
             for( int i = 0; i < b.length; i++ )
                 result[i] = op.operate(a[0], b[i]);
             return result;
         }
         if( a.length != b.length ) throw new IllegalArgumentException("'a' and 'b' must have the same length if neither has length 1.");
         double[] result = new double[a.length];
         for( int i = 0; i < a.length; i++ )
             result[i] = op.operate(a[i], b[i]);
         return result;
     }
     
     public static int[] range( int first, int afterLast ){
         int[] range = new int[afterLast - first];
         int i = 0;
         for( int x = first; x < afterLast; x++, i++ )
             range[i] = x;
         return range;
     }
     
     public static List<Integer> listRange( int first, int afterLast ){
         List<Integer> range = new ArrayList<>(afterLast-first);
         for( int x = first; x < afterLast; x++ )
             range.add(x);
         return range;
     }
     
     // Classes
     
     private final static class Add implements MathDoubleOp {
         private Add(){};
         @Override
         public double operate(double a, double b) {
             return a + b;
         }
     }
     
     private final static class Multiply implements MathDoubleOp {
         private Multiply(){};
         @Override
         public double operate(double a, double b) {
             return a*b;
         }
     }
 }
