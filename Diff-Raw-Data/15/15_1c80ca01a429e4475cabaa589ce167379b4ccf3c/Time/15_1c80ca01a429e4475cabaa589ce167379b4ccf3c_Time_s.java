 package org.powerbot.game.api.util;
 
 /**
  * A utility for manipulating time.
  *
  * @author Timer
  */
 public class Time {
	
 	/**
 	 * @param time The number of milliseconds to ensure sleeping for.
 	 */
 	public static void sleep(final int time) {
 		try {
 			final long start = System.currentTimeMillis();
 			Thread.sleep(time);
 			long now;
 			while (start + time > (now = System.currentTimeMillis())) {
 				Thread.sleep(start + time - now);
 			}
 		} catch (final InterruptedException ignored) {
 		}
 	}
	
 	/**
 	 * Sleeps for a random number of milliseconds.
	 * @param the minimum sleep time.
	 * @param the maximum sleep time.
 	 */
	public static void sleep(final int min, final int max) 
		sleep(Random.nextInt(min, max + 1));
 	}
 
 	/**
 	 * Converts milliseconds to a String in the format
 	 * hh:mm:ss.
 	 *
 	 * @param time The number of milliseconds.
 	 * @return The formatted String.
 	 */
 	public static String format(final long time) {
 		final StringBuilder t = new StringBuilder();
 		final long total_secs = time / 1000;
 		final long total_mins = total_secs / 60;
 		final long total_hrs = total_mins / 60;
 		final int secs = (int) total_secs % 60;
 		final int mins = (int) total_mins % 60;
 		final int hrs = (int) total_hrs % 60;
 		if (hrs < 10) {
 			t.append("0");
 		}
 		t.append(hrs);
 		t.append(":");
 		if (mins < 10) {
 			t.append("0");
 		}
 		t.append(mins);
 		t.append(":");
 		if (secs < 10) {
 			t.append("0");
 		}
 		t.append(secs);
 		return t.toString();
 	}
 }
