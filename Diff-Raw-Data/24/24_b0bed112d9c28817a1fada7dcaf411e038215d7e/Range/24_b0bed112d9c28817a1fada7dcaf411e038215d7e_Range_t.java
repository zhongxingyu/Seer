 package de.ptb.epics.eve.util.math.statistics;
 
 import org.apache.commons.math3.geometry.euclidean.oned.Interval;
 import org.apache.commons.math3.geometry.partitioning.Region;
 
 /**
  * @author Marcus Michalsky
  * @since 1.10
  */
public final class Range {
 	
 	/**
 	 * Checks whether a given value is in the interval constructed by the bounds 
 	 * given.
 	 * 
 	 * @param testValue the value to check
 	 * @param lowLimit the lower bound of the interval
 	 * @param highLimit the upper bound of the interval
 	 * @return <code>true</code> if the given value is contained in the 
 	 * 		interval, <code>false</code> otherwise
 	 */
 	public static boolean isInRange(double testValue, double lowLimit,
 			double highLimit) {
 		if (testValue == lowLimit || testValue == highLimit) {
 			return true;
 		}
 		Interval ival = new Interval(lowLimit, highLimit);
 		return ival.checkPoint(testValue, 0).equals(Region.Location.INSIDE);
 	}
 }
