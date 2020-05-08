 package ch.kanti_wohlen.asteroidminer;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 public class TaskScheduler {
 
 	/**
 	 * The current and only instance of TaskScheduler. Is never null.
 	 */
 	public static final TaskScheduler INSTANCE = new TaskScheduler();
 
	private Map<Runnable, Long> tasks;
 
 	private TaskScheduler() {
		tasks = new TreeMap<Runnable, Long>();
 	}
 
 	public void dispose() {
 		tasks.clear();
 	}
 
 	/**
 	 * Run task after rendering and after simulating the world. This way the Runnable can be executed safely.
 	 * 
 	 * @param r
 	 *            the Runnable you want to run. Cannot be null.
 	 */
 	public void runTask(Runnable r) {
 		if (r == null) return;
		tasks.put(r, 0L);
 	}
 
 	/**
 	 * Runs a task after a delay in seconds.
 	 * 
 	 * @param r
 	 *            the Runnable you want to run. Cannot be null.
 	 * @param delay
 	 *            the time in seconds until the method should be ran.
 	 */
 	public void runTaskLater(Runnable r, double delay) {
 		if (r == null) return;
 		long executionTime = System.currentTimeMillis() + (long) (1000 * Math.max(0d, delay));
		tasks.put(r, executionTime);
 	}
 
 	/**
 	 * Method to actually execute the stored Runnables. Should only ever be called once in GameScreen.
 	 */
 	public void onGameTick() {
		Iterator<Entry<Runnable, Long>> i = tasks.entrySet().iterator();
 
 		while (i.hasNext()) {
			Entry<Runnable, Long> entry = i.next();
 
			if (entry.getValue() <= System.currentTimeMillis()) {
				entry.getKey().run();
 				i.remove();
 			} else {
 				break;
 			}
 		}
 	}
 }
