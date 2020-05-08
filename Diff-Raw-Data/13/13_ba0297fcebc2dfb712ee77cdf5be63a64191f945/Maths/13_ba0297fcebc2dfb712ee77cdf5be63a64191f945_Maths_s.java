 package mikera.util;
 
 /**
  * Helpful maths functions
  * 
  * Focus on using floats rather than doubles for speed
  * 
  * @author Mike
  *
  */
 public final class Maths {
 	public static final double ROOT_TWO=(float)Math.sqrt(2);
 	public static final double ROOT_THREE=(float)Math.sqrt(3);
 	
 	/** mathematical constant e */
 	public static final double E=java.lang.Math.E;
 	
 	/** Mathematical constant Pi = 3.1415926... */
 	public static final double PI=(float)Math.PI;
 	public static final double TWO_PI=2.0*PI;
 
 	/** Mathematical constant Tau = 2 * Pi */
 	public static final double TAU=TWO_PI;
 	public static final double HALF_PI=0.5*PI;
 	public static final double QUARTER_PI=0.25*PI;
 	private static final double EPSILON = 0.00001;
 	
 	public static float sqrt(float a) {
 		return (float)Math.sqrt(a);
 	}
 	
 	public static double sqrt(double a) {
 		return Math.sqrt(a);
 	}
 
 	
 
 	
 	/**
 	 * Clamp a double value to an integer range
 	 */
 	public static int clampToInteger(double value, int min, int max) {
 		int v=(int)value;
 		if (v<min) return min;
 		if (v>max) return max;
 		return v;
 	}
 	
 	/**
 	 * Clamp a float value to an integer range
 	 */
 	public static int clampToInteger(float value, int min, int max) {
 		int v=(int)value;
 		if (v<min) return min;
 		if (v>max) return max;
 		return v;
 	}
 	
 	/**
 	 * Return the middle value of 3 numbers
 	 * 
 	 * Can use faster "bound" method if first and last parameters are in order
 	 */
 	public static int middle(int a, int b, int c) {
 		if (a<b) {
 			if (b<c) {
 				return b;
 			}
 			return (a<c)?c:a;
 		}
 		if (a<c) {
 			return a;
 		}
 		return (b<c)?c:b;
 	}
 	
 	/**
 	 * Return the middle value of 3 numbers
 	 * 
 	 * Can use faster "bound" method if first and last parameters are in order
 	 */
 	public static float middle(float a, float b, float c) {
 		if (a<b) {
 			if (b<c) {
 				return b;
 			}
 			return (a<c)?c:a;
 		}
 		if (a<c) {
 			return a;
 		}
 		return (b<c)?c:b;
 	}
 	
 	/**
 	 * Return the sign of a double value
 	 */
 	public static int sign(double a) {
 		if (a==0.0) return 0;
 		return (a>0)?1:-1;
 	}
 
 	/**
 	 * Return the sign of a float value
 	 */
 	public static int sign(float a) {
 		if (a==0.0f) return 0;
 		return (a>0)?1:-1;
 	}
 	
 	/**
 	 * Return the sign of an int value
 	 */
 	public static final int sign(int a) {
 		return (a==0) ? 0 : ( (a>0)?1:-1 );
 	}
 	
 	/**
 	 * Mike's fast integer sign algorithm
 	 * @param a
 	 * @return Sign of the given number (-1, 0 or 1)
 	 */
 	public static final int sign2(int a) {
 		return (a>>31)+((a>0)?1:0);
 	}
 	
 
 	
 	/** Return the sign of a long value */
 	public static int sign(long a) {
 		if (a==0) return 0;
 		return (a>0)?1:-1;
 	}
 
 	
 	/**
 	 * Integer modulus function
 	 * @param n number
 	 * @param d divisor
 	 * @return
 	 */
 	public static int mod(int number, int divisor) {
 		int r= (number%divisor);
 		if (r<0) r+=divisor;
 		return r;
 	}
 	
 	/**
 	 * Detects the number of times that boundary is passed when adding increase to base
 	 * @param increase
 	 * @param boundary
 	 * @param base
 	 * @return
 	 */
 	public static long quantize(long increase, long boundary, long base) {
 		return ((base+increase)/boundary)-(base/boundary);
 	}
 	
 	/** Return the minimum of three values */
 	public static double min(double a, double b, double c) {
 		double result=a;
 		if (b<result) result=b;
 		if (c<result) result=c;
 		return result;
 	}
 	
 	/** Return the maximum of three values */
 	public static double max(double a, double b, double c) {
 		double result=a;
 		if (b>result) result=b;
 		if (c>result) result=c;
 		return result;
 	}
 	
 	/** Return the minimum of three values */	
 	public static float min(float a, float b, float c) {
 		float result=a;
 		if (b<result) result=b;
 		if (c<result) result=c;
 		return result;
 	}
 	
 	/** Return the maximum of three values */
 	public static float max(float a, float b, float c) {
 		float result=a;
 		if (b>result) result=b;
 		if (c>result) result=c;
 		return result;
 	}
 	
 	/** Return the minimum of four values */	
 	public static final float min(float a, float b, float c, float d) {
 	    float result=a;
 		if (result > b) result = b;
 	    if (result > c) result = c;
 	    if (result > d) result = d;
 	    return result;
 	}
 
 	/** Return the maximum of four values */	
 	public static final float max(float a, float b, float c, float d) {
 		float result=a;
 		if (result < b) result = b;
 	    if (result < c) result = c;
 	    if (result < d) result = d;
 	    return result;
 	}
 
 	/** Return the minimum of two numbers */
 	public static int min(final int a, final int b) {
 		// oddly, this seems to outperform java.lang.Math.min by about 10% on caliper tests.....
 		return (a<b)?a:b;
 	}
 	
 	/** Return the maximum of two numbers */
 	public static int max(final int a, final int b) {
 		return (a>b)?a:b;
 	}
 	
 	
 	/** Return the minimum of two numbers */
 	public static float min(float a, float b) {
 		return (a<b)?a:b;
 	}
 	
 	/** Return the maximum of two numbers */
 	public static float max(float a, float b) {
 		return (a>b)?a:b;
 	}
 	
 	/** Return the minimum of three numbers */
 	public static int min(int a, int b, int c) {
 		int result=a;
 		if (b<result) result=b;
 		if (c<result) result=c;
 		return result;
 	}
 	
 	/** Return the maximum of three numbers */
 	public static int max(int a, int b, int c) {
 		int result=a;
 		if (b>result) result=b;
 		if (c>result) result=c;
 		return result;
 	}
 	
 	public static float sigmoid (float a) {
 		double ea=Math.exp(-a);
 		float df=(float)(1/(1.0f+ea));
 		if (Float.isNaN(df)) return (a>0)?1:0;
 		return df;
 	}
 	
 	public static double sigmoid (double a) {
 		double ea=Math.exp(-a);
 		double df=(1/(1.0f+ea));
 		if (Double.isNaN(df)) return (a>0)?1:0;
 		return df;
 	}
 	
 	public static double softplus (double a) {
 		if (a>100) return a;
 		if (a<-100) return 0.0;
 		return Math.log(1+Math.exp(a));
 	}
 	
 	public static float tanh (float a) {
 		double ex=Math.exp(2*a);
 		float df=(float)((ex-1)/(ex+1));
 		if (Float.isNaN(df)) return (a>0)?1:-1;
 		return df;
 	}
 	
 	public static double tanh (double a) {
 		double ex=Math.exp(2*a);
 		double df=((ex-1)/(ex+1));
 		if (Double.isNaN(df)) return (a>0)?1:-1;
 		return df;
 	}
 	
 	public static double tanhScaled(double a) {
 		return 1.7159*tanh((2.0/3.0)*a);
 	}
 	
 	public static double tanhScaledDerivative(double a) {
 		double ta=tanh((2.0/3.0)*a);
 		return (1.7159*(2.0/3.0))*(ta*(1-ta));
 	}
 	
 	public static float inverseSigmoid (float a) {
 		if (a>=1) return 800;
 		if (a<=0) return -800;
 		double ea=a/(1.0-a);
 		return (float)Math.log(ea);
 	}
 	
 	public static float sigmoidDerivative (float a) {
 		float sa=sigmoid(a);
 		return sa*(1-sa);
 	}
 	
 	public static float tanhDerivative (float a) {
 		float sa=tanh(a);
 		return 1-sa*sa;
 	}
 	
 
 
 	
 	/** Return the (always non-negative) fractional part of a number */
 	public static float frac(float a) {
 		return a-roundDown(a);
 	}
 	
 	/** Return the (always non-negative) fractional part of a number */
 	public static double frac(double a) {
 		return a-Math.floor(a);
 	}
 
 
 	/** Return the square of a number */
 	public static int square(byte b) {
 		return b*b;
 	}
 	
 	/** Return the square of a number */
 	public static int square(int x) {
 		return x*x;
 	}
 	
 	/** Return the square of a number */
 	public static float square(float x) {
 		return x*x;
 	}
 	
 	/** Return the square of a number */
 	public static double square(double x) {
 		return x*x;
 	}
 	
 	/** Round up to next integer */
 	public static int roundUp(double d) {
 		int i=(int) d;
 		return (i==d)?i:(i+1);
 	}
 	
 	
 	
 	/** Round up to next integer */
 	public static int roundUp(Number d) {
 		return roundUp(d.doubleValue());
 	}
 	
 	/** Round up to next integer */
 	public static int roundUp(float d) {
 		int i=(int) d;
 		return (i==d)?i:(i+1);
 	}
 
 	/** Rounds down (integer floor) of a double value */	
 	public static int roundDown(double a) {
 		if (a>=0) return (int)a;
 		int x=(int)a;
 		return (a==x)?x:x-1;
 	}
 	
 	/** Round down to next smallest integer (towards negative infinity) */
 	public static int roundDown(float a) {
 		if (a>=0) return (int)a;
 		int x=(int)a;
 		return (a==x)?x:x-1;
 	}
 	
 	/**
 	 * Soft maximum function
 	 */
 	public static double softMaximum(double x, double y) {
 		double max=Math.max(x, y);
 		double min=Math.min(x, y);
 		return max+Math.log(1.0+Math.exp(max-min));
 	}
 	
 	/** Bound a value within a given range */
 	public static final double bound(double v, double min, double max) {
 		if (v<min) return min;
 		if (v>max) return max;
 		return v;
 	}
 	
 	/** Bound a value within a given range */
	public static final int bound(int min, int v, int max) {
 		if (v<min) return min;
 		if (v>max) return max;
 		return v;
 	}
 
 	/** Tests whether a value is near zero, to a default tolerance level */
 	public static boolean notNearZero(double d) {
 		return (d<-EPSILON)||(d>EPSILON);
 	}
 	
     /** Double mod function */
 	public static double mod(double num, double div) {
 		double result=num%div;
 		if (result<0) result+=div;
 		return result;
 	}
 	
     /** Triangle wave with wavelength 1.0, and range 0.0 - 1.0 */
 	public static double triangleWave(double a) {
 		a-=Math.floor(a);
 		return (a<0.5)?a*2:(2-a*2);
 	}
 
 	
 }
