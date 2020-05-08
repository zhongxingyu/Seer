 /**
  * 
  */
 package edu.berkeley.gamesman.util;
 
 import com.sun.tools.javac.util.FatalError;
 
 /**
  * Various utility functions accessible from any class
  * 
  * @author Steven Schlansker
  *
  */
 public final class Util {
 	
 	private Util() {}
 
 	protected static class AssertionFailedError extends Error {
 		private static final long serialVersionUID = 2545784238123111405L;
 	}
 	
 	protected static class FatalError extends Error {
 		private static final long serialVersionUID = -5642903706572262719L;
 	}
 	
 	/**
 	 * Throws a fatal Error if a required condition is not satisfied
 	 * @param b The boolean (expression) that must be true
 	 */
 	public static void assertTrue(boolean b){
 		if(!b){
 			System.err.println("Assertion failed: backtrace forthcoming");
 			throw new AssertionFailedError();
 		}
 	}
 	
 	public static void fatalError(String s){
 		System.err.println("FATAL: "+s);
 		System.err.println("Stack trace follows:");
 		throw new FatalError();
 	}
 	
 	public static void warn(String s){
 		System.err.println("WARN: "+s);
 		System.err.println("Stack trace follows:");
 		try {
 			throw new FatalError();
 		}catch(FatalError e){
 			e.printStackTrace(System.err);
 		}
 	}
 	
 	static boolean debugInit = true, debugOn = true;
 	public static void debug(String s){
 		if(!debugOn) return;
 		if(debugInit){
 			debugInit = false;
 			debugOn = OptionProcessor.checkOption("d") != null;
 			if(!debugOn) return;
 		}
 		System.err.println("DEBUG: "+s);
 	}
 	
 	public static String millisToETA(long millis){
 		long sec = (millis/1000) % 60;
 		long min = (millis/1000/60) % 60;
 		long hr = (millis/1000/60/60);
 		return String.format("%02d:%02d:%02d",hr,min,sec);
 	}
 	
 	/**
 	 * Convenience function to calculate linear offset for two dimensional coordinates
 	 * 
 	 * @param x X position
 	 * @param y Y position
 	 * @param w Board width
 	 * @return Linear offset into 1-d array
 	 */
 	public static int index(int x, int y, int w){
 		return x+y*w;
 	}
 	
 	/**
 	 * Calculate b^e for integers.
 	 * Relatively fast - O(log e).
 	 * Not well defined for e < 0 or b^e > MAX_INT.
 	 * 
 	 * @param b Base
 	 * @param e Exponent
 	 * @return b^e
 	 */
 	public static int intpow(int b, int e){
 		if(e <= 0) return 1;
 		if(e % 2 == 0){
 			int s = intpow(b,e/2);
 			return s*s;
 		}
 		return b*intpow(b,e-1);
 	}
 	
 	/**
 	 * Calculate binomial coefficient (n k)
 	 * Shamelessly stolen from http://en.wikipedia.org/w/index.php?title=Binomial_coefficient&oldid=250717842
 	 * 
 	 * @note You could memoize this if it turns out to be a big performance problem
 	 * 
 	 * @param n n
 	 * @param k k
 	 * @return n choose k
 	 */
 	public static long nCr(int n, int k){
		if(n < 0 || k < 0) return _nCr(n,k);
 		if(n < 50 && k < 50){
 			if(nCr_cache[n][k] != 0)
 				return nCr_cache[n][k];
 			nCr_cache[n][k] = _nCr(n,k);
 			return nCr_cache[n][k];
 		}
 		return _nCr(n,k);
 	}
 	
 	private static long[][] nCr_cache = new long[50][50]; // 50 is a made-up number, you're free to adjust as necessary...
 	
 	
 	private static long _nCr(int n, int mk){
 		int k = mk;
 	    if (k > n)
 	        return 0;
 
 	    if (k > n/2)
 	        k = n-k; // go faster
 
 	    double accum = 1;
 	    for (long i = 1; i <= k; i++)
 	         accum = accum * (n-k+i) / i;
 
 	    return (long)(accum + 0.5); // avoid rounding error
 	}
 }
