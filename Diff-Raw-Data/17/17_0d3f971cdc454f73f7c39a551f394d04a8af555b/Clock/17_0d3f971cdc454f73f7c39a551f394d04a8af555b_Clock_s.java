 package catdog.mine;
 
 public class Clock {
 	private static long start;
 	
 	/**
 	 * Ϸ  (: )
 	 */
	public static final float DAY = 150; 
 	
 	static {
 		reset();
 	}
 	
 	public static float getElapsed() {
 		return (System.currentTimeMillis() - start) / 1000f;
 	}
 	
 	public static void reset() {
 		start = System.currentTimeMillis();
 	}
 	
 	public static boolean isNight() {
 		return (getElapsed() % DAY) > DAY/2f;
 	}
 	
 	public static int getDay() {
 		return (int)Math.ceil(getElapsed() / DAY);
 	}
 }
