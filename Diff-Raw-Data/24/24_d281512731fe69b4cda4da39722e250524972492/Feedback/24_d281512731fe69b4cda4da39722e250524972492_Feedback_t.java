 package pleocmd.pipe;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import pleocmd.pipe.data.Data;
 
 abstract class Feedback {
 
 	private long startTime;
 
 	private long stopTime;
 
 	private final List<Throwable> temporaryErrors;
 
 	private final List<Throwable> permanentErrors;
 
 	private int interruptionCount;
 
 	private int dropCount;
 
 	private int behindCountSignificant;
 
 	private long behindCount;
 
 	private long behindMax;
 
 	private long behindSum;
 
 	Feedback() {
 		startTime = 0;
 		temporaryErrors = new ArrayList<Throwable>();
 		permanentErrors = new ArrayList<Throwable>();
 	}
 
 	/**
 	 * @return time at which the {@link Feedback} has been started
 	 */
 	public final synchronized long getStartTime() {
 		return startTime;
 	}
 
 	/**
 	 * @return time at which the {@link Feedback} has been stopped or <b>0</b>
 	 *         if it is still running
 	 */
 	public final synchronized long getStopTime() {
 		return stopTime;
 	}
 
 	/**
 	 * @return time which has elapsed since the {@link Feedback} has been
 	 *         started if it's currently running or the time it has run
 	 *         otherwise.
 	 */
 	public final synchronized long getElapsed() {
 		return (stopTime == 0 ? System.currentTimeMillis() : stopTime)
 				- startTime;
 	}
 
 	/**
 	 * @return a list of all {@link Exception}s that have been thrown and which
 	 *         only caused temporary failure (i.e. affected only one
 	 *         {@link Data})
 	 */
 	public final synchronized List<Throwable> getTemporaryErrors() {
 		return Collections.unmodifiableList(temporaryErrors);
 	}
 
 	/**
 	 * @return a list of all {@link Exception}s that have been thrown and which
 	 *         only caused permanent failure (i.e. possibly affected more than
 	 *         one {@link Data})
 	 */
 	public final synchronized List<Throwable> getPermanentErrors() {
 		return Collections.unmodifiableList(permanentErrors);
 	}
 
 	/**
 	 * @return number of {@link Data} that have been interrupted because a
 	 *         {@link Data} with a higher priority has to be executed
 	 */
 	public final synchronized int getInterruptionCount() {
 		return interruptionCount;
 	}
 
 	/**
 	 * @return number of {@link Data} that have been dropped because a
 	 *         {@link Data} with a higher priority is currently been executed or
 	 *         has been queued for execution
 	 */
 	public final synchronized int getDropCount() {
 		return dropCount;
 	}
 
 	public final synchronized long getSignificantBehindCount() {
 		return behindCountSignificant;
 	}
 
 	public final synchronized long getBehindCount() {
 		return behindCount;
 	}
 
 	public final synchronized long getBehindMax() {
 		return behindMax;
 	}
 
 	public final synchronized long getBehindSum() {
 		return behindSum;
 	}
 
 	public final synchronized long getBehindAverage() {
 		return behindCount == 0 ? 0 : behindSum / behindCount;
 	}
 
 	final synchronized void started() {
 		startTime = System.currentTimeMillis();
 	}
 
 	final synchronized void stopped() {
 		stopTime = System.currentTimeMillis();
 		((ArrayList<?>) temporaryErrors).trimToSize();
 		((ArrayList<?>) permanentErrors).trimToSize();
 	}
 
 	final synchronized void addError(final Throwable t, final boolean permanent) {
 		if (permanent)
 			permanentErrors.add(t);
 		else
 			temporaryErrors.add(t);
 	}
 
 	final synchronized void incInterruptionCount() {
 		++interruptionCount;
 	}
 
	public final synchronized void incDropCount() {
 		++dropCount;
 	}
 
 	final synchronized void incDropCount(final int increment) {
 		dropCount += increment;
 	}
 
 	final synchronized void incBehindCount(final long behind,
 			final boolean significant) {
 		if (significant) ++behindCountSignificant;
 		++behindCount;
 		if (behindMax < behind) behindMax = behind;
 		behindSum += behind;
 	}
 
 	private String getCurrentRunningState() {
 		if (stopTime != 0)
 			return String.format("has run %d milliseconds", getElapsed());
 		if (startTime != 0)
 			return String.format("is running since %d milliseconds",
 					getElapsed());
 		return "has never been run";
 	}
 
 	@Override
 	public final synchronized String toString() {
 		if (startTime == 0 && stopTime == 0)
 			return String.format("Pipe %s", getCurrentRunningState());
 		return String.format("Pipe %s,%s encountered %d "
 				+ "temporary and %d permanent error(s), "
 				+ "output has been %d time(s) interrupted "
 				+ "due to high-priority data, "
 				+ "%d time(s) been dropped due to low-priority and "
 				+ "it was %d time(s) behind (average %d - max %d - sum %d).%s",
 				getCurrentRunningState(), getAdditionalString1(),
 				temporaryErrors.size(), permanentErrors.size(),
 				interruptionCount, dropCount, behindCountSignificant,
 				getBehindAverage(), behindMax, behindSum,
 				getAdditionalString2());
 	}
 
 	protected abstract String getAdditionalString1();
 
 	protected abstract String getAdditionalString2();
 
 	protected final <E> void appendToHTMLTable(final StringBuilder sb,
 			final String name, final E value) {
 		sb.append("<tr><td align=right>");
 		sb.append(name);
 		sb.append("</td><td align=left>");
 		sb.append(String.valueOf(value));
 		sb.append("</td></tr>");
 	}
 
 	public final synchronized String getHTMLTable() {
 		final StringBuilder sb = new StringBuilder("<table border=1>");
 		appendToHTMLTable(sb, "State", getCurrentRunningState());
 		addAdditionalHTMLTable1(sb);
 		appendToHTMLTable(sb, "Temporary errors", temporaryErrors.size());
 		appendToHTMLTable(sb, "Permanent errors", permanentErrors.size());
 		appendToHTMLTable(sb, "Interrupted output", getInterruptionCount());
 		appendToHTMLTable(sb, "Dropped output", getDropCount());
 		appendToHTMLTable(sb, "Significantly behind",
 				getSignificantBehindCount());
 		appendToHTMLTable(sb, "Behind stats", String.format(
 				"average %d - max %d - sum %d", getBehindAverage(),
 				getBehindMax(), getBehindSum()));
 		addAdditionalHTMLTable2(sb);
 		sb.append("</table>");
 		return sb.toString();
 	}
 
 	protected abstract void addAdditionalHTMLTable1(final StringBuilder sb);
 
 	protected abstract void addAdditionalHTMLTable2(final StringBuilder sb);
 
 }
