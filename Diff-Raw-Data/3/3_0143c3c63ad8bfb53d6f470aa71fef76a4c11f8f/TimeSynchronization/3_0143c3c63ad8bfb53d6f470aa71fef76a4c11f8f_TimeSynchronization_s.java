 package di.kdd.smartmonitor.framework;
 
 public class TimeSynchronization {
 	private static long timeDifferenceSum = 0;
 	private static long differencesCounted = 0;
 	
 	private static final String TAG = "time synchronization";
 		
 	public static void timeReference(long time) {
		timeDifferenceSum += System.currentTimeMillis() - time;	
 	}
 	
 	public static long getTime() {
 		return System.currentTimeMillis() + (timeDifferenceSum / differencesCounted);
 	}
 }
