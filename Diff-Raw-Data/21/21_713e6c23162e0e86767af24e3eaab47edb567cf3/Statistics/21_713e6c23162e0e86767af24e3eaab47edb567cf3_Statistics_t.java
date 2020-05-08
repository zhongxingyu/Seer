 /*
 
 Copyright: 2010 Purdue University
 
 This file is distributed under the following terms (MIT/X11 License):
 
 	Permission is hereby granted, free of charge, to any person
 	obtaining a copy of this file and associated documentation
 	files (the "Software"), to deal in the Software without
 	restriction, including without limitation the rights to use,
 	copy, modify, merge, publish, distribute, sublicense, and/or sell
 	copies of the Software, and to permit persons to whom the
 	Software is furnished to do so, subject to the following
 	conditions:
 
 	The above copyright notice and this permission notice shall be
 	included in all copies or substantial portions of the Software.
 
 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 	EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 	OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 	NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 	HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 	WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 	FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 	OTHER DEALINGS IN THE SOFTWARE.
 
 */
 package edu.purdue.bbc.util;
 
 import edu.purdue.bbc.util.equation.*;
 
 import java.util.Arrays;
 
 import org.apache.log4j.Logger;
 
 /**
  * A class for various statistical calculations. 
  *
  * @author Thomas McGrew
  */
 public class Statistics {
 
 	private Statistics( ){ }
 
 	/**
 	 * Returns the Pearson correlation coefficient of the 2 sets of values.
 	 * 
 	 * <PRE>
 	 *    x = sample values in the first set
 	 *    y = sample values in the second set
 	 *    n = number of values in each set
 	 *    Sx = standard deviation of x
 	 *    Sy = standard deviation of y
 	 * 
 	 *                                               _              _
 	 *                     sum( i=0 to n-1, ( x[i] - x ) * ( y[i] - y ))
 	 * correlationValue = -----------------------------------------------
 	 *                                ( n - 1 ) Sx * Sy
 	 * 
 	 * References:
 	 * <CITE>Wikipedia, the free encyclopedia (2010, April 13).
 	 *     <I>Correlation and Dependence</I>.
 	 *     Retrieved April 14, 2010, from http://en.wikipedia.org/wiki/Correlation#Pearson.27s_product-moment_coefficient</CITE>
 	 * </PRE>
 	 *
 	 * @param x The first set of values to use for the calculation.
 	 * @param y The second set of values to use for the calculation.
 	 * @return    The Pearson correlation value. 
 	 */
 	public static double getPearsonCorrelation( double[] x, double[] y ) {
		if ( x.length != y.length || x.length < 3 )
 			return Double.NaN;
 
 		double Sx=0, Sy=0, meanX=0, meanY=0, thisX, thisY, numerator=0;
 		double sumX=0, sumY=0, sumXSq=0, sumYSq=0;
 
 		for( double currentX : x ){
 			meanX += currentX;
 		}
 		for ( double currentY : y ) {
 			meanY += currentY;
 		}
 		int n = x.length;
 		meanX /= n;
 		meanY /= n;
 		for( int i=0; i < n; i++ ) {
 			thisX = x[ i ];
 			thisY = y[ i ];
 			numerator += ( thisX - meanX ) * ( thisY - meanY );
 			sumX   += thisX;
 			sumY   += thisY;
 			sumXSq += thisX * thisX;
 			sumYSq += thisY * thisY;
 		}
 		Sx = Math.sqrt(( sumXSq - sumX * sumX / n ) / (n-1));
 		Sy = Math.sqrt(( sumYSq - sumY * sumY / n ) / (n-1));
 
 		return ( numerator / (( n-1 ) * Sx * Sy ));
 
 	}
 
 	/**
 	 * Returns the Spearman rank correlation coefficient of the 2 sets of data.
 	 *
 	 * <PRE>
 	 *    x = values in the first set
 	 *    y = values in the second set
 	 *    n = number of values in each set
 	 *    Rx = rank array of x, ie. the new locations of each element of x if x were sorted
 	 *    Ry = rank array of y, ie. the new locations of each element of y if y were sorted
 	 * 
 	 *                         6 * sum( i=0 to n-1, ( Rx[i] - Ry[i] )^2 )
 	 * correlationValue = 1 - --------------------------------------------
 	 *                                       n * ( n^2 - 1 )
 	 * 
 	 * References: 
 	 * <CITE>Wikipedia, the free encyclopedia (2010, April 1). 
 	 *     <I>Spearman's rank correlation coefficient</I>. 
 	 *     Retrieved April 14, 2010, from http://en.wikipedia.org/wiki/Spearman's_rank_correlation_coefficient</CITE>
 	 * <CITE>Spiegel, Murray R. (1961). Statistics, 2nd Edition (pp 376,391-393). New York.</CITE>
 	 * </PRE>
 	 * 
 	 * @param x The first set of data to use for the calculation.
 	 * @param y The second set of data to use for the calculation.
 	 * @return       The Spearman correlation value.
 	 */
 	public static double getSpearmanCorrelation( double[] x, double[] y ){
		if ( x.length != y.length || x.length < 3 )
 			return Double.NaN;
 
 		int n = x.length;
 		double [] Rx = getRank( x );
 		double [] Ry = getRank( y );
 
 		double numerator=0;
 		for ( int i=0; i < n; i++ ) {
 			numerator += Math.pow( Rx[ i ] - Ry[ i ], 2 );
 		}
 		
 		return 1 - ( 6 * numerator ) / ( n * ( n*n - 1 ));
 	}
 
 	/**
 	 * Returns the Kendall tau rank correlation coefficient of the 2 sets of data.
 	 * 
 	 * <PRE>
 	 *    x = values in the first set
 	 *    y = values in the second set
 	 *    n = number of values in each set
 	 *    Rx = the rank of the values in the first set
 	 *    Ry = the rank of the values in the second set
 	 *    Tx = the number of ties in x.
 	 *    Ty = the number of ties in y.
 	 *    concordant = The number of concordant pairs
 	 *    discordant = The number of discordant pairs
 	 *
 	 *                                 concordant - discordant
 	 * correlationValue = ---------------------------------------------
 	 *                              n*(n-1)            n*(n-1)         
 	 *                     sqrt( ( ------- - Tx ) * (  ------- - Ty ) )
 	 *                                2                   2            
 	 *
 	 * <CITE>Wikipedia, the free encyclopedia (2010, March 25). 
 	 *     <I>Kendall tau rank correlation coefficient</I>. 
 	 *     Retrieved April 14, 2010, from http://en.wikipedia.org/wiki/Kendall_tau_rank_correlation_coefficient</CITE>
 	 * </PRE>
 	 *
 	 * @param x The first set of data to use for the calculation.
 	 * @param y The second set of data to use for the calculation.
 	 * @return The Kendall tau correlation value.
 	 */
 	public static double getKendallCorrelation( double[] x, double[] y ) {
		if ( x.length != y.length || x.length < 3 )
 			return Double.NaN;
 
 		int n = x.length;
 		double [] Rx = getRank( x );
 		double [] Ry = getRank( y );
 
 		// Count the number of concordant and discordant pairs, and ties.
 		int concordant = 0, discordant = 0, xTies=0, yTies=0, xRel, yRel;
 		for ( int i=0; i < n-1; i++ ) {
 			for ( int j=i+1; j < n; j++ ) {
 				xRel = (int)Math.signum( Rx[ j ] - Rx[ i ] );
 				yRel = (int)Math.signum( Ry[ j ] - Ry[ i ] );
 				if ( xRel == 0 || yRel == 0 ) {	// check for ties first
 					if ( xRel == 0 )
 						xTies++;
 					if( yRel == 0 )
 						yTies++;
 				}
 				else if ( xRel ==  yRel ) {
 					concordant++;
 				}
 			
 				else if ( xRel == -yRel ) {
 					discordant++;
 				}
 			}
 		}
 		double denominator = ( n * ( n-1 ) / 2.0 );
 		return ( concordant - discordant ) / Math.sqrt(( denominator - xTies ) * ( denominator - yTies ));
 			
 	}
 
 	/** 
 	 * Finds the rank of each element in an array.
 	 *	
 	 * @param list The arrayList to get the rank of.
 	 * @return An array containing the rank order of each element.
 	 */
 	public static double [] getRank( double[] list ) {
 		int size = list.length;
 		double [] returnValue = new double[ size ];
 		RankTriplet [] rankedArray = new RankTriplet[size];
 		int i=0;
 		for ( double d : list ) {
 			rankedArray[ i ] = new RankTriplet( d, i++ );
 		}
 		Arrays.sort( rankedArray );
 
 		i=0;
 		for ( RankTriplet t : rankedArray ){
 			t.rank = i++;
 		}
 
 		// look for tied ranks.
 		int lastDiff = 0;
 		for ( i=0; i < size; i++ ) {
 			if ( rankedArray[ i ].compareTo( rankedArray[ lastDiff ] ) != 0 ) {
 				if ( i - lastDiff > 1 ) {
 					double newRank = lastDiff + (((i-1) - lastDiff ) / 2.0 );
 					for( int j=lastDiff; j < i; j++ ) {
 						rankedArray[ j ].rank = newRank;
 					}
 				}
 				lastDiff = i;
 			}
 		}
 
 		for ( RankTriplet t : rankedArray ) {
 			returnValue[ t.order ] = t.rank;
 	 }
 	 return returnValue;
 	}
 
 	/**
 	 * A class for holding a value along with its original order and rank
 	 */
 	private static class RankTriplet implements Comparable<RankTriplet> {
 		public double value;
 		public int order;
 		public double rank;
 
 		public RankTriplet ( double value ) {
 			this( value, 0, 0.0 );
 		}
 
 		public RankTriplet ( double value, int order ) {
 			this( value, order, 0.0 );
 		}
 
 		public RankTriplet ( double value, int order, double rank ) {
 			this.value = value;
 			this.order = order;
 			this.rank = rank;
 		}
 
 		public boolean equals( RankTriplet p ) {
 			return this.compareTo( p ) == 0;
 		}
 
 		public int compareTo( RankTriplet p ) {
 			return (int)Math.signum( this.value - p.value );
 		}
 
 		public String toString ( ) {
 			return "< "+ this.value +", "+ this.order +", "+ this.rank +" >";
 		}
 	}
 
 	/**
 	 * Returns the standard deviation of a set of values.
 	 * 
 	 * @param values The set to find the standard deviation of.
 	 * @return The standard deviation as a double.
 	 */
 	public static double standardDeviation( double [] values ) {
 		double sum = 0.0, sumSq = 0.0;
 		for ( double d : values ) {
 			sum += d;
 			sumSq += d*d;
 		}
 		return Math.sqrt((( sumSq - sum * sum ) / values.length ) / ( values.length - 1 ));
 	}
 
 	/**
 	 * Finds the median of a set of values. NaN values are omitted.
 	 * 
 	 * @param values The set to find the median of.
 	 * @return The median of those values.
 	 */
 	public static double median( double [] values ) {
 		double [] sorted = Arrays.copyOf( values, values.length );
 		Arrays.sort( sorted );
 		int count = 0;
 		// count the number of Non-NaN values.
 		for ( double d : values ){
 			if ( !Double.isNaN( d )) {
 				count++;
 			}
 		}
 		if (( count & 1 ) == 0 ) {
 			return ( values[ count / 2 ] + values[ count / 2 + 1 ]) / 2;
 		} else {
 			return values[ count / 2 ];
 		}
 	}
 
 	/**
 	 * Finds the mean of a set of values. NaN values are omitted.
 	 * 
 	 * @param values The set of values.
 	 * @return The mean of those values.
 	 */
 	public static double mean( double [] values ) {
 		double sum = 0.0;
 		int count = 0;
 		for ( double d : values ) {
 			if ( !Double.isNaN( d )) {
 				sum += d;
 				count++;
 			}
 		}
 		return sum / count;
 	}
 
 	/**
 	 * Finds the sum of a set of values. NaN values are omitted.
 	 * 
 	 * @param values The set of values.
 	 * @return The sum of those values.
 	 */
 	public static double sum( double [] values ) {
 		double sum = 0.0;
 		for ( double d : values ) {
 			if ( !Double.isNaN( d ))
 				sum += d;
 		}
 		return sum;
 	}
 
 	/**
 	 * Returns the minimum of a set of values. NaN values are omitted.
 	 * 
 	 * @param values The set of values to find the minimum for.
 	 * @return The minimum value in the array.
 	 */
 	public static double min( double [] values ) {
 		double returnValue = Double.POSITIVE_INFINITY;;
 		for ( double d : values ) {
 			returnValue = Math.min( returnValue, d );
 		}
 		return returnValue;
 	}
 
 	/**
 	 * Returns the maximum of a set of values. NaN values are omitted.
 	 * 
 	 * @param values the set of values to find the maximum for.
 	 * @return The maximum value in the array.
 	 */
 	public static double max( double [] values ) {
 		double returnValue = Double.NEGATIVE_INFINITY;
 		for ( double d : values ) {
 			if ( !Double.isNaN( d )) {
 				returnValue = Math.max( returnValue, d );
 			}
 		}
 		return returnValue;
 	}
 }
 
