 /**
  *
  */
 package ca.eandb.jmist.math;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 
 /**
  * Static mathematical utility methods.
  * @author Brad Kimmel
  */
 public final class MathUtil {
 
 	/** Java specification version. */
 	private static final double JAVA_SPEC = Double.parseDouble(System.getProperty("java.specification.version"));
 
 	/**
 	 * Implementation of methods from <code>java.lang.Math</code> not available
 	 * in Java 1.5.
 	 */
 	private static final MathImpl impl = JAVA_SPEC >= 1.6 ? new MathImplJava6() : new MathImplJava5();
 
 	/**
 	 * An interface for methods from <code>java.lang.Math</code> that are not
 	 * available in Java 1.5.
 	 * @author Brad Kimmel
 	 */
 	private static interface MathImpl {
 
 		/* (non-Javadoc)
 		 * @see java.lang.Math#nextUp(double)
 		 */
 		double nextUp(double d);
 
 		/* (non-Javadoc)
 		 * @see java.lang.Math#nextAfter(double, double)
 		 */
 		double nextAfter(double start, double direction);
 
 		/* (non-Javadoc)
 		 * @see java.lang.Math#nextUp(float)
 		 */
 		float nextUp(float f);
 
 		/* (non-Javadoc)
 		 * @see java.lang.Math#nextAfter(float, double)
 		 */
 		float nextAfter(float start, double direction);
 
 	}
 
 	/**
 	 * An implementation of methods from <code>java.lang.Math</code> that are
 	 * not available in Java 1.5.  This implementation calls these methods
 	 * directly, and as such is not compatible with Java 1.5.
 	 * @author Brad Kimmel
 	 */
 	private static final class MathImplJava6 implements MathImpl {
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.math.MathUtil.MathImpl#nextAfter(double, double)
 		 */
 		public double nextAfter(double start, double direction) {
 			return Math.nextAfter(start, direction);
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.math.MathUtil.MathImpl#nextUp(double)
 		 */
 		public double nextUp(double d) {
 			return Math.nextUp(d);
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.math.MathUtil.MathImpl#nextAfter(float, double)
 		 */
 		public float nextAfter(float start, double direction) {
 			return Math.nextAfter(start, direction);
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.math.MathUtil.MathImpl#nextUp(float)
 		 */
 		public float nextUp(float f) {
 			return Math.nextUp(f);
 		}
 
 	}
 
 	/**
 	 * A direct implementation of methods from <code>java.lang.Math</code> that
 	 * are not available in Java 1.5.  The implementation of these methods come
 	 * from <code>sun.misc.FpUtils</code> (available at
	 * <code>http://hg.openjdk.java.net/jdk7/tl/jdk/</code>.
 	 * @author Brad Kimmel
 	 */
 	private static final class MathImplJava5 implements MathImpl {
 
 	    /**
 	     * Returns the floating-point number adjacent to the first
 	     * argument in the direction of the second argument.  If both
 	     * arguments compare as equal the second argument is returned.
 	     *
 	     * <p>
 	     * Special cases:
 	     * <ul>
 	     * <li> If either argument is a NaN, then NaN is returned.
 	     *
 	     * <li> If both arguments are signed zeros, <code>direction</code>
 	     * is returned unchanged (as implied by the requirement of
 	     * returning the second argument if the arguments compare as
 	     * equal).
 	     *
 	     * <li> If <code>start</code> is
 	     * &plusmn;<code>Double.MIN_VALUE</code> and <code>direction</code>
 	     * has a value such that the result should have a smaller
 	     * magnitude, then a zero with the same sign as <code>start</code>
 	     * is returned.
 	     *
 	     * <li> If <code>start</code> is infinite and
 	     * <code>direction</code> has a value such that the result should
 	     * have a smaller magnitude, <code>Double.MAX_VALUE</code> with the
 	     * same sign as <code>start</code> is returned.
 	     *
 	     * <li> If <code>start</code> is equal to &plusmn;
 	     * <code>Double.MAX_VALUE</code> and <code>direction</code> has a
 	     * value such that the result should have a larger magnitude, an
 	     * infinity with same sign as <code>start</code> is returned.
 	     * </ul>
 	     *
 	     * @param start     starting floating-point value
 	     * @param direction value indicating which of
 	     * <code>start</code>'s neighbors or <code>start</code> should
 	     * be returned
 	     * @return The floating-point number adjacent to <code>start</code> in the
 	     * direction of <code>direction</code>.
 	     * @author Joseph D. Darcy
 	     */
 		public double nextAfter(double start, double direction) {
 			/*
 			 * The cases:
 			 *
 			 * nextAfter(+infinity, 0) == MAX_VALUE nextAfter(+infinity,
 			 * +infinity) == +infinity nextAfter(-infinity, 0) == -MAX_VALUE
 			 * nextAfter(-infinity, -infinity) == -infinity
 			 *
 			 * are naturally handled without any additional testing
 			 */
 
 			// First check for NaN values
 			if (Double.isNaN(start) || Double.isNaN(direction)) {
 				// return a NaN derived from the input NaN(s)
 				return start + direction;
 			} else if (start == direction) {
 				return direction;
 			} else { // start > direction or start < direction
 				// Add +0.0 to get rid of a -0.0 (+0.0 + -0.0 => +0.0)
 				// then bitwise convert start to integer.
 				long transducer = Double.doubleToRawLongBits(start + 0.0d);
 
 				/*
 				 * IEEE 754 floating-point numbers are lexicographically ordered
 				 * if treated as signed- magnitude integers . Since Java's
 				 * integers are two's complement, incrementing" the two's
 				 * complement representation of a logically negative
 				 * floating-point value *decrements* the signed-magnitude
 				 * representation. Therefore, when the integer representation of
 				 * a floating-point values is less than zero, the adjustment to
 				 * the representation is in the opposite direction than would be
 				 * expected at first .
 				 */
 				if (direction > start) { // Calculate next greater value
 					transducer = transducer + (transducer >= 0L ? 1L : -1L);
 				} else { // Calculate next lesser value
 					assert direction < start;
 					if (transducer > 0L)
 						--transducer;
 					else if (transducer < 0L)
 						++transducer;
 					/*
 					 * transducer==0, the result is -MIN_VALUE
 					 *
 					 * The transition from zero (implicitly positive) to the
 					 * smallest negative signed magnitude value must be done
 					 * explicitly.
 					 */
 					else
 						transducer = 0x8000000000000000L | 1L;
 				}
 
 				return Double.longBitsToDouble(transducer);
 			}
 		}
 
 	    /**
 	     * Returns the floating-point value adjacent to <code>d</code> in
 	     * the direction of positive infinity.  This method is
 	     * semantically equivalent to <code>nextAfter(d,
 	     * Double.POSITIVE_INFINITY)</code>; however, a <code>nextUp</code>
 	     * implementation may run faster than its equivalent
 	     * <code>nextAfter</code> call.
 	     *
 	     * <p>Special Cases:
 	     * <ul>
 	     * <li> If the argument is NaN, the result is NaN.
 	     *
 	     * <li> If the argument is positive infinity, the result is
 	     * positive infinity.
 	     *
 	     * <li> If the argument is zero, the result is
 	     * <code>Double.MIN_VALUE</code>
 	     *
 	     * </ul>
 	     *
 	     * @param d  starting floating-point value
 	     * @return The adjacent floating-point value closer to positive
 	     * infinity.
 	     * @author Joseph D. Darcy
 	     */
 		public double nextUp(double d) {
 			if (Double.isNaN(d) || d == Double.POSITIVE_INFINITY)
 				return d;
 			else {
 				d += 0.0d;
 				return Double.longBitsToDouble(Double.doubleToRawLongBits(d)
 						+ ((d >= 0.0d) ? +1L : -1L));
 			}
 		}
 
 	    /**
 	     * Returns the floating-point value adjacent to <code>f</code> in
 	     * the direction of positive infinity.  This method is
 	     * semantically equivalent to <code>nextAfter(f,
 	     * Double.POSITIVE_INFINITY)</code>; however, a <code>nextUp</code>
 	     * implementation may run faster than its equivalent
 	     * <code>nextAfter</code> call.
 	     *
 	     * <p>Special Cases:
 	     * <ul>
 	     * <li> If the argument is NaN, the result is NaN.
 	     *
 	     * <li> If the argument is positive infinity, the result is
 	     * positive infinity.
 	     *
 	     * <li> If the argument is zero, the result is
 	     * <code>Float.MIN_VALUE</code>
 	     *
 	     * </ul>
 	     *
 	     * @param f  starting floating-point value
 	     * @return The adjacent floating-point value closer to positive
 	     * infinity.
 	     * @author Joseph D. Darcy
 	     */
 		public float nextUp(float f) {
 			if (Float.isNaN(f) || f == Float.POSITIVE_INFINITY)
 				return f;
 			else {
 				f += 0.0f;
 				return Float.intBitsToFloat(Float.floatToRawIntBits(f)
 						+ ((f >= 0.0f) ? +1 : -1));
 			}
 		}
 
 	    /**
 	     * Returns the floating-point number adjacent to the first
 	     * argument in the direction of the second argument.  If both
 	     * arguments compare as equal, the second argument is returned.
 	     *
 	     * <p>
 	     * Special cases:
 	     * <ul>
 	     * <li> If either argument is a NaN, then NaN is returned.
 	     *
 	     * <li> If both arguments are signed zeros, a <code>float</code>
 	     * zero with the same sign as <code>direction</code> is returned
 	     * (as implied by the requirement of returning the second argument
 	     * if the arguments compare as equal).
 	     *
 	     * <li> If <code>start</code> is
 	     * &plusmn;<code>Float.MIN_VALUE</code> and <code>direction</code>
 	     * has a value such that the result should have a smaller
 	     * magnitude, then a zero with the same sign as <code>start</code>
 	     * is returned.
 	     *
 	     * <li> If <code>start</code> is infinite and
 	     * <code>direction</code> has a value such that the result should
 	     * have a smaller magnitude, <code>Float.MAX_VALUE</code> with the
 	     * same sign as <code>start</code> is returned.
 	     *
 	     * <li> If <code>start</code> is equal to &plusmn;
 	     * <code>Float.MAX_VALUE</code> and <code>direction</code> has a
 	     * value such that the result should have a larger magnitude, an
 	     * infinity with same sign as <code>start</code> is returned.
 	     * </ul>
 	     *
 	     * @param start     starting floating-point value
 	     * @param direction value indicating which of
 	     * <code>start</code>'s neighbors or <code>start</code> should
 	     * be returned
 	     * @return The floating-point number adjacent to <code>start</code> in the
 	     * direction of <code>direction</code>.
 	     * @author Joseph D. Darcy
 	     */
 		public float nextAfter(float start, double direction) {
 			/*
 			 * The cases:
 			 *
 			 * nextAfter(+infinity, 0) == MAX_VALUE nextAfter(+infinity,
 			 * +infinity) == +infinity nextAfter(-infinity, 0) == -MAX_VALUE
 			 * nextAfter(-infinity, -infinity) == -infinity
 			 *
 			 * are naturally handled without any additional testing
 			 */
 
 			// First check for NaN values
 			if (Float.isNaN(start) || Double.isNaN(direction)) {
 				// return a NaN derived from the input NaN(s)
 				return start + (float) direction;
 			} else if (start == direction) {
 				return (float) direction;
 			} else { // start > direction or start < direction
 				// Add +0.0 to get rid of a -0.0 (+0.0 + -0.0 => +0.0)
 				// then bitwise convert start to integer.
 				int transducer = Float.floatToRawIntBits(start + 0.0f);
 
 				/*
 				 * IEEE 754 floating-point numbers are lexicographically ordered
 				 * if treated as signed- magnitude integers . Since Java's
 				 * integers are two's complement, incrementing" the two's
 				 * complement representation of a logically negative
 				 * floating-point value *decrements* the signed-magnitude
 				 * representation. Therefore, when the integer representation of
 				 * a floating-point values is less than zero, the adjustment to
 				 * the representation is in the opposite direction than would be
 				 * expected at first.
 				 */
 				if (direction > start) {// Calculate next greater value
 					transducer = transducer + (transducer >= 0 ? 1 : -1);
 				} else { // Calculate next lesser value
 					assert direction < start;
 					if (transducer > 0)
 						--transducer;
 					else if (transducer < 0)
 						++transducer;
 					/*
 					 * transducer==0, the result is -MIN_VALUE
 					 *
 					 * The transition from zero (implicitly positive) to the
 					 * smallest negative signed magnitude value must be done
 					 * explicitly.
 					 */
 					else
 						transducer = 0x80000000 | 1;
 				}
 
 				return Float.intBitsToFloat(transducer);
 			}
 		}
 
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Math#nextUp(double)
 	 */
 	public static double nextUp(double d) {
 		return impl.nextUp(d);
 	}
 
     /**
      * Returns the floating-point value adjacent to <code>d</code> in
      * the direction of negative infinity.  This method is
      * semantically equivalent to <code>nextAfter(d,
      * Double.NEGATIVE_INFINITY)</code>; however, a
      * <code>nextDown</code> implementation may run faster than its
      * equivalent <code>nextAfter</code> call.
      *
      * <p>Special Cases:
      * <ul>
      * <li> If the argument is NaN, the result is NaN.
      *
      * <li> If the argument is negative infinity, the result is
      * negative infinity.
      *
      * <li> If the argument is zero, the result is
      * <code>-Double.MIN_VALUE</code>
      *
      * </ul>
      *
      * @param d  starting floating-point value
      * @return The adjacent floating-point value closer to negative
      * infinity.
      * @author Joseph D. Darcy
      */
     public static double nextDown(double d) {
 		if (Double.isNaN(d) || d == Double.NEGATIVE_INFINITY)
 			return d;
 		else {
 			if (d == 0.0)
 				return -Double.MIN_VALUE;
 			else
 				return Double.longBitsToDouble(Double.doubleToRawLongBits(d)
 						+ ((d > 0.0d) ? -1L : +1L));
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see java.lang.Math#nextAfter(double, double)
 	 */
 	public static double nextAfter(double start, double direction) {
 		return impl.nextAfter(start, direction);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Math#nextUp(float)
 	 */
 	public static float nextUp(float d) {
 		return impl.nextUp(d);
 	}
 
     /**
      * Returns the floating-point value adjacent to <code>f</code> in
      * the direction of negative infinity.  This method is
      * semantically equivalent to <code>nextAfter(f,
      * Float.NEGATIVE_INFINITY)</code>; however, a
      * <code>nextDown</code> implementation may run faster than its
      * equivalent <code>nextAfter</code> call.
      *
      * <p>Special Cases:
      * <ul>
      * <li> If the argument is NaN, the result is NaN.
      *
      * <li> If the argument is negative infinity, the result is
      * negative infinity.
      *
      * <li> If the argument is zero, the result is
      * <code>-Float.MIN_VALUE</code>
      *
      * </ul>
      *
      * @param f  starting floating-point value
      * @return The adjacent floating-point value closer to negative
      * infinity.
      * @author Joseph D. Darcy
      */
     public static float nextDown(float f) {
 		if (Float.isNaN(f) || f == Float.NEGATIVE_INFINITY)
 			return f;
 		else {
 			if (f == 0.0f)
 				return -Float.MIN_VALUE;
 			else
 				return Float.intBitsToFloat(Float.floatToRawIntBits(f)
 						+ ((f > 0.0f) ? -1 : +1));
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Math#nextAfter(float, double)
 	 */
 	public static float nextAfter(float start, double direction) {
 		return impl.nextAfter(start, direction);
 	}
 
 	/**
 	 * Returns the minimum value in an array of <code>double</code>s.
 	 * @param array The array of <code>double</code>s of which to find the
 	 * 		minimum value.
 	 * @return The minimum value in <code>array</code>.
 	 */
 	public static double min(double[] array) {
 		double min = Double.POSITIVE_INFINITY;
 		for (int i = 0; i < array.length; i++) {
 			if (array[i] < min) {
 				min = array[i];
 			}
 		}
 		return min;
 	}
 
 	/**
 	 * Returns the minimum value in an collection of <code>double</code>s.
 	 * @param values The collection of <code>double</code>s of which to find
 	 * 		the	minimum value.
 	 * @return The minimum value in <code>values</code>.
 	 */
 	public static double min(Iterable<Double> values) {
 		double min = Double.POSITIVE_INFINITY;
 		for (double x : values) {
 			if (x < min) {
 				min = x;
 			}
 		}
 		return min;
 	}
 
 	/**
 	 * Returns the maximum value in an array of <code>double</code>s.
 	 *
 	 * @param array
 	 *            The array of <code>double</code>s of which to find the
 	 *            maximum value.
 	 * @return The maximum value in <code>array</code>.
 	 */
 	public static double max(double[] array) {
 		double max = Double.NEGATIVE_INFINITY;
 		for (int i = 0; i < array.length; i++) {
 			if (array[i] > max) {
 				max = array[i];
 			}
 		}
 		return max;
 	}
 
 	/**
 	 * Returns the maximum value in an collection of <code>double</code>s.
 	 * @param values The collection of <code>double</code>s of which to find
 	 * 		the	maximum value.
 	 * @return The maximum value in <code>values</code>.
 	 */
 	public static double max(Iterable<Double> values) {
 		double max = Double.NEGATIVE_INFINITY;
 		for (double x : values) {
 			if (x > max) {
 				max = x;
 			}
 		}
 		return max;
 	}
 
 	/**
 	 * Computes the mean of the values in the given array of
 	 * <code>double</code>s.
 	 * @param array The array of <code>double</code>s to compute the mean of.
 	 * @return The mean of the values in <code>array</code>.
 	 */
 	public static double mean(double[] array) {
 		return MathUtil.sum(array) / (double) array.length;
 	}
 
 	/**
 	 * Computes the mean of the values in the given collection of
 	 * <code>double</code>s.
 	 * @param values The collection of <code>double</code>s to compute the mean
 	 * 		of.
 	 * @return The mean of the values in <code>values</code>.
 	 */
 	public static double mean(Collection<Double> values) {
 		return MathUtil.sum(values) / values.size();
 	}
 
 	/**
 	 * Determines whether the values in the given array of <code>double</code>s
 	 * are all equal within a tolerance of {@value MathUtil#EPSILON}
 	 * @param array The array of <code>double</code>s to compare.
 	 * @return A value indicating whether all values in <code>array</code> are
 	 * 		within {@value MathUtil#EPSILON} of one another.
 	 * @see MathUtil#EPSILON
 	 */
 	public static boolean areEqual(double[] array) {
 		return areEqual(array, MathUtil.EPSILON);
 	}
 
 	/**
 	 * Determines whether the values in the given collection of
 	 * <code>double</code>s are all equal within a tolerance of
 	 * {@value MathUtil#EPSILON}.
 	 * @param values The collection of <code>double</code>s to compare.
 	 * @return A value indicating whether all values in <code>values</code> are
 	 * 		within {@value MathUtil#EPSILON} of one another.
 	 * @see MathUtil#EPSILON
 	 */
 	public static boolean areEqual(Iterable<Double> values) {
 		return areEqual(values, MathUtil.EPSILON);
 	}
 
 	/**
 	 * Determines whether the values in the given array of <code>double</code>s
 	 * are all equal within a specified tolerance.
 	 * @param array The array of <code>double</code>s to compare.
 	 * @param epsilon The tolerance.
 	 * @return A value indicating whether all values in <code>array</code> are
 	 * 		within <code>epsilon</code> of one another.
 	 */
 	public static boolean areEqual(double[] array, double epsilon) {
 
 		if (array.length < 2) {
 			return true;
 		}
 
 		double min = array[0];
 		double max = array[0];
 
 		for (int i = 1; i < array.length; i++) {
 			if (array[i] < min) {
 				min = array[i];
 			}
 			if (array[i] > max) {
 				max = array[i];
 			}
 			if (!equal(min, max, epsilon)) {
 				return false;
 			}
 		}
 
 		return true;
 
 	}
 
 	/**
 	 * Determines whether the values in the given collection of
 	 * <code>double</code>s are all equal within a specified tolerance.
 	 * @param values The collection of <code>double</code>s to compare.
 	 * @param epsilon The tolerance.
 	 * @return A value indicating whether all values in <code>values</code> are
 	 * 		within <code>epsilon</code> of one another.
 	 */
 	public static boolean areEqual(Iterable<Double> values, double epsilon) {
 
 		double min = Double.POSITIVE_INFINITY;
 		double max = Double.NEGATIVE_INFINITY;
 
 		for (double x : values) {
 			if (x < min) {
 				min = x;
 			}
 			if (x > max) {
 				max = x;
 			}
 			if (!equal(min, max, epsilon)) {
 				return false;
 			}
 		}
 
 		return true;
 
 	}
 
 	/**
 	 * Computes the sum of the values in an array of <code>double</code>s.
 	 * @param array The array of <code>double</code>s of which to compute the
 	 * 		sum.
 	 * @return The sum of the values in <code>array</code>.
 	 */
 	public static double sum(double[] array) {
 		double sum = 0.0;
 		for (int i = 0; i < array.length; i++) {
 			sum += array[i];
 		}
 		return sum;
 	}
 
 	/**
 	 * Computes the sum of the values in a collection of <code>double</code>s.
 	 * @param values The collection of <code>double</code>s of which to compute
 	 * 		the	sum.
 	 * @return The sum of the values in <code>values</code>.
 	 */
 	public static double sum(Iterable<Double> values) {
 		double sum = 0.0;
 		for (double x : values) {
 			sum += x;
 		}
 		return sum;
 	}
 
 	/**
 	 * Scales the specified array of <code>double</code>s so that they sum to
 	 * one.
 	 * @param weights The array of <code>double</code>s to normalize (this
 	 * 		array will be modified).
 	 * @return A reference to <code>weights</code>.
 	 */
 	public static double[] normalize(double[] weights) {
 		double sum = MathUtil.sum(weights);
 		return MathUtil.scale(weights, 1.0 / sum);
 	}
 
 	/**
 	 * Adds the elements of one array of <code>double</code>s to another. The
 	 * lengths of <code>accumulator</code> and <code>summand</code> must be
 	 * equal.
 	 * @param accumulator The array of <code>double</code>s to add the
 	 * 		elements of <code>summand</code> to (the elements of this array
 	 * 		will be modified).  This may be <code>null</code>, in which case a
 	 * 		new array will be created of the same length of
 	 * 		<code>summand</code> and will be initialized to zeros before adding
 	 * 		the values of <code>summand</code>.
 	 * @param summand The array of <code>double</code>s to add to each
 	 * 		corresponding element of <code>accumulator</code>.
 	 * @return A reference to <code>accumulator</code>.
 	 */
 	public static double[] add(double[] accumulator, double[] summand) {
 		if (accumulator == null) {
 			return summand.clone();
 		}
 		assert(accumulator.length == summand.length);
 		for (int i = 0; i < accumulator.length; i++) {
 			accumulator[i] += summand[i];
 		}
 		return accumulator;
 	}
 
 	/**
 	 * Subtracts the elements of one array of <code>double</code>s from
 	 * another. The lengths of <code>accumulator</code> and <code>values</code>
 	 * must be equal.
 	 * @param accumulator The array of <code>double</code>s to subtract the
 	 * 		elements of <code>values</code> from (the elements of this array
 	 * 		will be modified).  This may be <code>null</code>, in which case a
 	 * 		new array will be created and initialized to zeros before
 	 * 		subtracting the values from <code>values</code>.
 	 * @param values The array of <code>double</code>s to subtract from each
 	 * 		corresponding element of <code>accumulator</code>.
 	 * @return A reference to <code>accumulator</code>.
 	 */
 	public static double[] subtract(double[] accumulator, double[] values) {
 		if (accumulator == null) {
 			accumulator = new double[values.length];
 		}
 		assert(accumulator.length == values.length);
 		for (int i = 0; i < accumulator.length; i++) {
 			accumulator[i] -= values[i];
 		}
 		return accumulator;
 	}
 
 	/**
 	 * Multiplies all the elements in an array of <code>double</code>s by the
 	 * elements of an equally sized array of <code>double</code>s.  The lengths
 	 * of <code>accumulator</code> and <code>modulator</code> must be equal.
 	 * @param accumulator The array of <code>double</code>s that is to have its
 	 * 		elements scaled (the elements of this array will be modified).
 	 * 		This may be <code>null</code>, in which case a new array will be
 	 * 		created and initialized to ones before multiplying by the values of
 	 * 		<code>modulator</code>.
 	 * @param modulator The array of <code>double</code>s by which to multiply
 	 * 		each corresponding element of <code>accumulator</code>.
 	 * @return A reference to <code>accumulator</code>.
 	 */
 	public static double[] modulate(double[] accumulator, double[] modulator) {
 		if (accumulator == null) {
 			return modulator.clone();
 		}
 		assert(accumulator.length == modulator.length);
 		for (int i = 0; i < accumulator.length; i++) {
 			accumulator[i] *= modulator[i];
 		}
 		return accumulator;
 	}
 
 	/**
 	 * Multiplies all of the values in the specified array of
 	 * <code>double</code>s by the same value.
 	 * @param array The array of <code>double</code>s the elements of which are
 	 * 		to be multiplied by a constant factor (the elements of this array
 	 * 		will be modified).
 	 * @param factor The value by which to multiply each element of
 	 * 		<code>array</code>.
 	 * @return A reference to <code>array</code>.
 	 */
 	public static double[] scale(double[] array, double factor) {
 		for (int i = 0; i < array.length; i++) {
 			array[i] *= factor;
 		}
 		return array;
 	}
 
 	/**
 	 * Returns x if it is within the specified range, or the closest
 	 * value within the specified range if x is outside the range.
 	 * @param x The value to threshold.
 	 * @param min The minimum of the range to threshold to.
 	 * @param max The maximum of the range to threshold to.
 	 * @return x, if min <= x <= max.  min, if x < min.  max, if x > max.
 	 */
 	public static double threshold(double x, double min, double max) {
 		if (x < min) {
 			return min;
 		} else if (x > max) {
 			return max;
 		} else {
 			return x;
 		}
 	}
 
 	/**
 	 * Returns x if it is within the specified range, or the closest
 	 * value within the specified range if x is outside the range.
 	 * @param x The value to threshold.
 	 * @param min The minimum of the range to threshold to.
 	 * @param max The maximum of the range to threshold to.
 	 * @return x, if min <= x <= max.  min, if x < min.  max, if x > max.
 	 */
 	public static int threshold(int x, int min, int max) {
 		if (x < min) {
 			return min;
 		} else if (x > max) {
 			return max;
 		} else {
 			return x;
 		}
 	}
 
 	/**
 	 * Determines whether two floating point values are close enough
 	 * to be considered "equal" (i.e., the difference may be attributed
 	 * to rounding errors).
 	 * @param x The first value to compare.
 	 * @param y The second value to compare.
 	 * @param epsilon The minimum difference required for the two values
 	 * 		to be considered distinguishable.
 	 * @return A value indicating whether the difference between x and y
 	 * 		is less than the given threshold.
 	 */
 	public static boolean equal(double x, double y, double epsilon) {
 		return Math.abs(x - y) < epsilon;
 	}
 
 	/**
 	 * Determines whether two floating point values are close enough
 	 * to be considered "equal" (i.e., the difference may be attributed
 	 * to rounding errors).
 	 * @param x The first value to compare.
 	 * @param y The second value to compare.
 	 * @return A value indicating whether the difference between x and y
 	 * 		is less than MathUtil.EPSILON.
 	 * @see #EPSILON
 	 */
 	public static boolean equal(double x, double y) {
 		return Math.abs(x - y) < MathUtil.EPSILON;
 	}
 
 	/**
 	 * Determines whether a floating point value is close enough to zero to be
 	 * considered "equal" to zero (i.e., the difference may be attributed to
 	 * rounding errors).
 	 * @param x The value to compare to zero.
 	 * @param epsilon The minimum absolute value of {@code x} for it to be
 	 * 		considered non-zero.
 	 * @return A value indicates whether the difference between {@code x} and
 	 * 		0.0 is less than {@code epsilon}.
 	 */
 	public static boolean isZero(double x, double epsilon) {
 		return Math.abs(x) < epsilon;
 	}
 
 	/**
 	 * Determines whether a floating point value is close enough to zero to be
 	 * considered "equal" to zero (i.e., the difference may be attributed to
 	 * rounding errors).
 	 * @param x The value to compare to zero.
 	 * @param epsilon The minimum absolute value of {@code x} for it to be
 	 * 		considered non-zero.
 	 * @return A value indicates whether the difference between {@code x} and
 	 * 		0.0 is less than {@link #EPSILON}.
 	 * @see {@link #EPSILON}.
 	 */
 	public static boolean isZero(double x) {
 		return Math.abs(x) < MathUtil.EPSILON;
 	}
 
 	/**
 	 * Determines whether {@code x} falls within the open interval
 	 * {@code (minimum, maximum)}.
 	 * @param x The value to check.
 	 * @param minimum The lower bound of the interval to check against.
 	 * @param maximum The upper bound of the interval to check against.
 	 * @return A value indicating whether {@code x} is contained in the open
 	 * 		interval {@code (minimum, maximum)}.
 	 */
 	public static boolean inRangeOO(double x, double minimum, double maximum) {
 		return minimum < x && x < maximum;
 	}
 
 	/**
 	 * Determines whether {@code x} falls within the interval
 	 * {@code [minimum, maximum)}.
 	 * @param x The value to check.
 	 * @param minimum The lower bound of the interval to check against.
 	 * @param maximum The upper bound of the interval to check against.
 	 * @return A value indicating whether {@code x} is contained in the
 	 * 		interval {@code [minimum, maximum)}.
 	 */
 	public static boolean inRangeCO(double x, double minimum, double maximum) {
 		return minimum <= x && x < maximum;
 	}
 
 	/**
 	 * Determines whether {@code x} falls within the closed interval
 	 * {@code [minimum, maximum]}.
 	 * @param x The value to check.
 	 * @param minimum The lower bound of the interval to check against.
 	 * @param maximum The upper bound of the interval to check against.
 	 * @return A value indicating whether {@code x} is contained in the closed
 	 * 		interval {@code [minimum, maximum]}.
 	 */
 	public static boolean inRangeCC(double x, double minimum, double maximum) {
 		return minimum <= x && x <= maximum;
 	}
 
 	/**
 	 * Determines whether {@code x} falls within the interval
 	 * {@code (minimum, maximum]}.
 	 * @param x The value to check.
 	 * @param minimum The lower bound of the interval to check against.
 	 * @param maximum The upper bound of the interval to check against.
 	 * @return A value indicating whether {@code x} is contained in the
 	 * 		interval {@code (minimum, maximum]}.
 	 */
 	public static boolean inRangeOC(double x, double minimum, double maximum) {
 		return minimum < x && x <= maximum;
 	}
 
 	/**
 	 * Returns the value of the integer of highest magnitude (farthest from
 	 * zero) whose absolute value is at most that of <code>x</code>.
 	 * @param x The value to truncate.
 	 * @return The integer portion of <code>x</code>.
 	 */
 	public static double truncate(double x) {
 		return x > 0.0 ? Math.floor(x) : Math.ceil(x);
 	}
 
 	/**
 	 * Interpolates between two end points.
 	 * @param a The end point at <code>t = 0</code>.
 	 * @param b The end point at <code>t = 1</code>.
 	 * @param t The value at which to interpolate.
 	 * @return The value that is the fraction <code>t</code> of the way from
 	 * 		<code>a</code> to <code>b</code>: <code>(1-t)a + tb</code>.
 	 */
 	public static double interpolate(double a, double b, double t) {
 		return a + t * (b - a);
 	}
 
 	/**
 	 * Interpolates between two points on a line.
 	 * @param x0 The x-coordinate of the first point.
 	 * @param y0 The y-coordinate of the first point.
 	 * @param x1 The x-coordinate of the second point.
 	 * @param y1 The y-coordinate of the second point.
 	 * @param x The x-coordinate at which to interpolate.
 	 * @return The y-coordinate corresponding to <code>x</code>.
 	 */
 	public static double interpolate(double x0, double y0, double x1, double y1, double x) {
 		double t = (x - x0) / (x1 - x0);
 		return interpolate(y0, y1, t);
 	}
 
 	/**
 	 * Interpolates a piecewise linear curve.
 	 * @param x An array of x-coordinates (this must be sorted in ascending
 	 * 		order).
 	 * @param y An array of the y-coordinates (must be of the same length as
 	 * 		<code>x</code>).
 	 * @param x0 The x-coordinate at which to interpolate.
 	 * @return The y-coordinate corresponding to <code>x0</code>.
 	 */
 	public static double interpolate(double[] x, double[] y, double x0) {
 
 		if (x0 <= x[0]) {
 			return y[0];
 		}
 		if (x0 >= x[x.length - 1]) {
 			return y[x.length - 1];
 		}
 
 		int index = Arrays.binarySearch(x, x0);
 		if (index < 0) {
 			index = -(index + 1);
 		}
 		while (index < x.length - 1 && !(x0 < x[index + 1])) {
 			index++;
 		}
 
 		assert(index < x.length - 1);
 
 		return interpolate(x[index - 1], y[index - 1], x[index], y[index], x0);
 
 	}
 
 	/**
 	 * Interpolates a piecewise linear curve.
 	 * @param x A <code>Tuple</code> of x-coordinates (this must be sorted in
 	 * 		ascending order).
 	 * @param y A <code>Tuple</code> of y-coordinates (must be of the same
 	 * 		length as <code>x</code>).
 	 * @param x0 The x-coordinate at which to interpolate.
 	 * @return The y-coordinate corresponding to <code>x0</code>.
 	 */
 	public static double interpolate(Tuple x, Tuple y, double x0) {
 
 		if (x0 <= x.at(0)) {
 			return y.at(0);
 		}
 		int n = x.size();
 		if (x0 >= x.at(n - 1)) {
 			return y.at(n - 1);
 		}
 
 		int index = binarySearch(x, x0);
 		if (index < 0) {
 			index = -(index + 1);
 		}
 		while (index < n - 1 && !(x0 < x.at(index + 1))) {
 			index++;
 		}
 
 		assert(index < n - 1);
 
 		return interpolate(x.at(index - 1), y.at(index - 1), x.at(index), y.at(index), x0);
 
 	}
 
 	/**
 	 * Searches the specified <code>Tuple</code> for the specified value using
 	 * the binary search algorithm. The <code>Tuple</code> must be sorted prior
 	 * to making this call. If it is not sorted, the results are undefined. If
 	 * the <code>Tuple</code> contains multiple elements with the specified
 	 * value, there is no guarantee which one will be found. This method
 	 * considers all NaN values to be equivalent and equal.
 	 * @param a The <code>Tuple</code> to be searched.
 	 * @param key The value to be searched for.
 	 * @return index of the search key, if it is contained in the
 	 * 		<code>Tuple</code>;	otherwise, (-(insertion point) - 1). The
 	 * 		insertion point is defined as the point at which the key would be
 	 * 		inserted into the <code>Tuple</code>: the index of the first
 	 * 		element greater than the key, or a.size() if all elements in the
 	 * 		<code>Tuple</code> are less than the specified key. Note that this
 	 * 		guarantees that the return value will be >= 0 if and only if the
 	 * 		key is found.
 	 */
 	public static final int binarySearch(Tuple a, double key) {
 		int low = 0;
 	    int hi = a.size() - 1;
 	    int mid = 0;
 	    while (low <= hi) {
 	    	mid = (low + hi) >> 1;
 	    	final int r = Double.compare(a.at(mid), key);
 	        if (r == 0) {
 	        	return mid;
 	        } else if (r > 0) {
 	        	hi = mid - 1;
 	        } else {
 	        	low = ++mid;
 	        }
 	    }
 	    return -mid - 1;
 	}
 
 	/**
 	 * Performs a bilinear interpolation between four values.
 	 * @param _00 The value at <code>(t, u) = (0, 0)</code>.
 	 * @param _10 The value at <code>(t, u) = (1, 0)</code>.
 	 * @param _01 The value at <code>(t, u) = (0, 1)</code>.
 	 * @param _11 The value at <code>(t, u) = (1, 1)</code>.
 	 * @param t The first value at which to interpolate.
 	 * @param u The second value at which to interpolate.
 	 * @return The interpolated value at <code>(t, u)</code>.
 	 */
 	public static double bilinearInterpolate(double _00, double _10,
 			double _01, double _11, double t, double u) {
 
 		return interpolate(
 				interpolate(_00, _10, t),
 				interpolate(_01, _11, t),
 				u
 		);
 
 	}
 
 	/**
 	 * Performs a trilinear interpolation between eight values.
 	 * @param _000 The value at <code>(t, u, v) = (0, 0, 0)</code>.
 	 * @param _100 The value at <code>(t, u, v) = (1, 0, 0)</code>.
 	 * @param _010 The value at <code>(t, u, v) = (0, 1, 0)</code>.
 	 * @param _110 The value at <code>(t, u, v) = (1, 1, 0)</code>.
 	 * @param _001 The value at <code>(t, u, v) = (0, 0, 1)</code>.
 	 * @param _101 The value at <code>(t, u, v) = (1, 0, 1)</code>.
 	 * @param _011 The value at <code>(t, u, v) = (0, 1, 1)</code>.
 	 * @param _111 The value at <code>(t, u, v) = (1, 1, 1)</code>.
 	 * @param t The first value at which to interpolate.
 	 * @param u The second value at which to interpolate.
 	 * @param v The third value at which to interpolate.
 	 * @return The interpolated value at <code>(t, u, v)</code>.
 	 */
 	public static double trilinearInterpolate(double _000, double _100,
 			double _010, double _110, double _001, double _101, double _011,
 			double _111, double t, double u, double v) {
 
 		return interpolate(
 				bilinearInterpolate(_000, _001, _010, _011, u, v),
 				bilinearInterpolate(_100, _101, _110, _111, u, v),
 				t
 		);
 
 	}
 
 	/**
 	 * Computes the integral of the piecewise linear function through the given
 	 * points (<code>i</code>, <code>y[i]</code>) using the trapezoidal
 	 * method.
 	 * @param y The y-coordinates of the vertices of the piecewise linear
 	 * 		function to integrate.
 	 * @return The integral of the piecewise linear function.
 	 * @throws IllegalArgumentException if <code>y.length == 0</code>.
 	 */
 	public static double trapz(double[] y) {
 		if (y.length == 0) {
 			throw new IllegalArgumentException("y.length == 0");
 		}
 		double value = 0.0;
 		double y0 = y[0];
 		for (int i = 0; i < y.length; i++) {
 			double y1 = y[i];
 			double ym = y0 + y1;
 			value += ym;
 			y0 = y1;
 		}
 		return value / 2.0;
 	}
 
 	/**
 	 * Computes the integral of the piecewise linear function through the given
 	 * points (<code>x[i]</code>, <code>y[i]</code>) using the trapezoidal
 	 * method.
 	 * @param x The x-coordinates of the vertices of the piecewise linear
 	 * 		function to integrate.
 	 * @param y The y-coordinates of the vertices of the piecewise linear
 	 * 		function to integrate.
 	 * @return The integral of the piecewise linear function.
 	 * @throws IllegalArgumentException if <code>x.length != y.length</code>.
 	 * @throws IllegalArgumentException if <code>y.length == 0</code>.
 	 */
 	public static double trapz(double[] x, double[] y) {
 		if (x.length != y.length) {
 			throw new IllegalArgumentException("x.length != y.length");
 		}
 		if (y.length == 0) {
 			throw new IllegalArgumentException("y.length == 0");
 		}
 		double value = 0.0;
 		double x0 = x[0];
 		double y0 = y[0];
 		for (int i = 1; i < y.length; i++) {
 			double x1 = x[i];
 			double y1 = y[i];
 			double dx = x1 - x0;
 			double ym = y0 + y1;
 			value += dx * ym;
 			x0 = x1;
 			y0 = y1;
 		}
 		return value / 2.0;
 	}
 
 	/**
 	 * Computes the integral of the piecewise linear function through the given
 	 * points (<code>i</code>, <code>y[i]</code>) using the trapezoidal
 	 * method.
 	 * @param y The y-coordinates of the vertices of the piecewise linear
 	 * 		function to integrate.
 	 * @return The integral of the piecewise linear function.
 	 * @throws IllegalArgumentException if <code>y</code> is empty.
 	 */
 	public static double trapz(Iterable<Double> y) {
 		Iterator<Double> ys = y.iterator();
 		if (!ys.hasNext()) {
 			throw new IllegalArgumentException("y is empty.");
 		}
 		double value = 0.0;
 		double y0 = ys.next();
 		while (ys.hasNext()) {
 			double y1 = ys.next();
 			double ym = y0 + y1;
 			value += ym;
 			y0 = y1;
 		}
 		return value / 2.0;
 	}
 
 	/**
 	 * Computes the integral of the piecewise linear function through the given
 	 * points (<code>x[i]</code>, <code>y[i]</code>) using the trapezoidal
 	 * method.
 	 * @param x The x-coordinates of the vertices of the piecewise linear
 	 * 		function to integrate.
 	 * @param y The y-coordinates of the vertices of the piecewise linear
 	 * 		function to integrate.
 	 * @return The integral of the piecewise linear function.
 	 * @throws IllegalArgumentException if the lengths of <code>x</code> and
 	 * 		<code>y</code> differ.
 	 * @throws IllegalArgumentException if <code>y</code> is empty.
 	 */
 	public static double trapz(Iterable<Double> x, Iterable<Double> y) {
 		Iterator<Double> xs = x.iterator();
 		Iterator<Double> ys = y.iterator();
 		if (!ys.hasNext()) {
 			throw new IllegalArgumentException("y is empty.");
 		}
 		if (!xs.hasNext()) {
 			throw new IllegalArgumentException("Lengths of x and y differ.");
 		}
 		double value = 0.0;
 		double x0 = xs.next();
 		double y0 = ys.next();
 		while (ys.hasNext()) {
 			if (!xs.hasNext()) {
 				throw new IllegalArgumentException("Lengths of x and y differ.");
 			}
 			double x1 = xs.next();
 			double y1 = ys.next();
 			double dx = x1 - x0;
 			double ym = y0 + y1;
 			value += dx * ym;
 			x0 = x1;
 			y0 = y1;
 		}
 		if (xs.hasNext()) {
 			throw new IllegalArgumentException("Lengths of x and y differ.");
 		}
 		return value / 2.0;
 	}
 
 	/**
 	 * A comparison threshold value to be used when a very high degree
 	 * of precision is expected.
 	 */
 	public static final double TINY_EPSILON		= 1e-12;
 
 	/**
 	 * A comparison threshold value to be used when a high degree of
 	 * precision is expected.
 	 */
 	public static final double SMALL_EPSILON	= 1e-9;
 
 	/**
 	 * A comparison threshold value to be used when a normal degree of
 	 * precision is expected.
 	 */
 	public static final double EPSILON			= 1e-6;
 
 	/**
 	 * A comparison threshold value to be used when a low degree of
 	 * precision is expected.
 	 */
 	public static final double BIG_EPSILON		= 1e-4;
 
 	/**
 	 * The difference between 1.0 and the next highest representable value.
 	 */
 	public static final double MACHINE_EPSILON	= Math.ulp(1.0);
 
 	/**
 	 * This class contains only static utility methods and static constants,
 	 * and therefore should not be creatable.
 	 */
 	private MathUtil() {}
 
 }
