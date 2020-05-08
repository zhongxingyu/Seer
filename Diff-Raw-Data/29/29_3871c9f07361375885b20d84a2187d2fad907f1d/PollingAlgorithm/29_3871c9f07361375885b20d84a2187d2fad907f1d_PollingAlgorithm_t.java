 package co.shoutbreak.polling;
 
 import java.util.Date;
 
 import co.shoutbreak.core.C;
 import co.shoutbreak.core.utils.SBLog;
 
 public class PollingAlgorithm {
 
 	private static final String TAG = "PollingAlgorithm";
 	
	private static final long DELAY_MIN_SECS = 20; // 20 secs
	private static final long DELAY_MAX_SECS = 1200; // 20 mins
	private static final long SECONDS_TILL_MAX_DELAY = 600; // 10 mins
 	
 	private static int consecutiveDroppedPackets = 0;
 	
 	private Date _lastActivity;
 	private float _delayPerSecondElapsed;
 	
 	public PollingAlgorithm() {
 		SBLog.constructor(TAG);
 		_lastActivity = new Date();
 		_delayPerSecondElapsed = (float)(DELAY_MAX_SECS - DELAY_MIN_SECS) / (float)SECONDS_TILL_MAX_DELAY;
 	}
 	
 	public synchronized void resetPollingDelay() {
 		_lastActivity = new Date();
 	}
 	
 	// returns milliseconds
 	public long getPollingDelay() {
 		Date now = new Date();
 		long elapsedMilliseconds = now.getTime() - _lastActivity.getTime();
		long elapsedSeconds = elapsedMilliseconds / 1000;
		long delay = DELAY_MAX_SECS;
		if (elapsedSeconds < SECONDS_TILL_MAX_DELAY) {
			delay = (long) (elapsedSeconds * _delayPerSecondElapsed); // seconds elapsed * delayPerSecondElapsed
		}
 		delay = DELAY_MIN_SECS + delay;
 		SBLog.polling(delay);
 		return delay * 1000;
 	}
 
 	public static boolean isDropCountAtLimit() {
 		consecutiveDroppedPackets++;
 		if (consecutiveDroppedPackets >= C.CONFIG_DROPPED_PACKET_LIMIT) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static void resetDropCount() {
 		consecutiveDroppedPackets = 0;		
 	}
 	
 }
