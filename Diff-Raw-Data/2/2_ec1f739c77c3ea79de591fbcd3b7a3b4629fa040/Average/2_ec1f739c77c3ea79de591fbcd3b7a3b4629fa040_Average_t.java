 package btwmods.measure;
 
 public class Average implements Comparable<Average> {
 	
 	/**
 	 * The number of values to use to calculate the average.
 	 */
 	public static final int RESOLUTION = 100;
 	
 	private int resolution;
 	private int tick = -1;
 	private long[] history;
 	private long total = 0;
 	
 	public Average() {
 		this(RESOLUTION);
 	}
 	
 	public Average(int resolution) {
 		this.resolution = resolution;
 		history = new long[resolution];
 	}
 	
 	
 	/**
 	 * Get the number of history entries used to calculate the average.
 	 * If {@link #Average()} was used then this will be {@link #RESOLUTION}.
 	 * 
 	 * @return The number of history entries.
 	 */
 	public int getResolution() {
 		return resolution;
 	}
 	
 	/**
 	 * The number of times a value has been recorded using {@link #resetCurrent()} or {@link #record(long)}.
 	 * 
 	 * @return The number of times that a value has been recorded.
 	 */
 	public int getTick() {
 		return tick;
 	}
 	
 	/**
 	 * Get the values used to calculate the average. This is a round-robin history and the position is based on
 	 * <code>average.codegetTick() % average.getResolution()</code>.
 	 * 
 	 * <p><strong>WARNING: Do not modify the returned long. It is <em>not</em> a clone.</strong></p>
 	 * 
 	 * @return An array of longs representing the values used to make the average..
 	 * @see #getTick()
 	 * @see #getResolution()
 	 */
 	public long[] getHistory() {
 		return history;
 	}
 	
 	/**
 	 * Get the total of the history entries.
 	 * 
 	 * @return The total.
 	 */
 	public long getTotal() { 
 		return total;
 	}
 	
 	/**
 	 * Get the average based on the history entries.
 	 * 
 	 * @return The average.
 	 */
 	public double getAverage() {
		return (double)total / (double)Math.max(1, Math.min(tick + 1, resolution));
 	}
 	
 	/**
 	 * Move to the next history entry and set its value to 0.
 	 * This is used with {@link #incrementCurrent(long)}.
 	 */
 	public void resetCurrent() {
 		tick++;
 		
 		// Remove the old value from the total.
 		if (tick >= resolution) {
 			total -= history[tick % resolution];
 		}
 
 		history[tick % resolution] = 0;
 	}
 	
 	/**
 	 * Move to the next history entry and set its value.
 	 * 
 	 * @param value The value to include in the history. 
 	 */
 	public void record(long value) {
 		resetCurrent();
 
 		// Add the new value to the total and store it in the history.
 		total += (history[tick % resolution] = value);
 	}
 	
 	/**
 	 * Add a value to the current history entry.
 	 * 
 	 * @param value
 	 */
 	public void incrementCurrent(long value) {
 		total += value;
 		history[tick % resolution] += value;
 	}
 	
 	public static Average[] createInitializedArray(int size, int resolution) {
 		Average[] averages = new Average[size];
 		for (int i = 0; i < size; i++) {
 			averages[i] = new Average(resolution);
 		}
 		return averages;
 	}
 	
 	public static Average[] createInitializedArray(int size) {
 		return createInitializedArray(size, RESOLUTION);
 	}
 
 	@Override
 	public int compareTo(Average o) {
 		return new Long(total).compareTo(o.total);
 	}
 }
