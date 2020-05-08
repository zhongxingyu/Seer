 package sort;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 
 import members.Competitor;
 import members.NullTime;
 import members.Time;
 
 /**
  * @author Andrée & Victor
  * 
  *         Prints according to first iteration race.
  */
 public class StdCompetitorPrinter extends Printer {
 
 	/**
 	 * This method is needed because this printer does not consider laps and
 	 * therefore the method 'getTotalTime()' in competitor returns an incorrect
 	 * time.
 	 * 
 	 * @return Total time elapsed, or Null time string
 	 */
 	private Time totalTime(Competitor c) {
 		return (c.getStartTimes().isEmpty() || c.getFinishTimes().isEmpty()) ? new NullTime()
 				: c.getStartTimes().get(0)
 						.difference(c.getFinishTimes().get(0));
 	}
 
 	@Override
 	public String row(Competitor c) {
 		StringBuilder sb = new StringBuilder();
 
 		appendCompetitorInfo(sb, c);
 
 		boolean failed = false;
 
 		failed = appendMultipleTimes(sb, MULTIPLE_STARTS, c.getStartTimes(),
 				failed);
 
 		failed = appendMultipleTimes(sb, MULTIPLE_ENDS, c.getFinishTimes(),
 				failed);
 
 		failed = appendImpossibleTotalTime(sb, c, failed);
 
 		return sb.toString();
 	}
 
 	/**
 	 * Appends an error message if the total time is less than the value of
 	 * MINIMUM_TOTAL_TIME. If some other error message has been appended
 	 * already, then no extra ';' is needed
 	 * 
 	 * @param sb
 	 *            the stringbuilder to append to
 	 * @param c
 	 *            the competitor
 	 * @param failed
 	 *            status flag if another error message has already been appended
 	 * @return the, possibly changed, status flag
 	 */
 	private boolean appendImpossibleTotalTime(StringBuilder sb, Competitor c,
 			boolean failed) {
 		Time totalTime = totalTime(c);
 
 		if (totalTime.compareTo(MINIMUM_TOTAL_TIME) <= 0) {
 			if (!failed) {
 				sb.append(";");
 				failed = true;
 			}
 			sb.append(" ");
 			sb.append(IMPOSSIBLE_TOTAL_TIME);
 		}
 		return failed;
 	}
 
 	/**
 	 * If list of times contains more than one time, append error message
 	 * followed by list of the extra times. If some other error message has been
 	 * appended already, then no extra ';' is needed
 	 * 
 	 * @param sb
 	 *            the stringbuilder to append to
 	 * @param msg
 	 *            the error message
 	 * @param times
 	 *            the list of times
 	 * @param failed
 	 *            status flag if another error message has already been appended
 	 * @return the, possibly changed, status flag
 	 */
 	private boolean appendMultipleTimes(StringBuilder sb, String msg,
 			List<Time> times, boolean failed) {
 		if (times.size() > 1) {
 			if (!failed) {
 				sb.append(";");
 				failed = true;
 			}
 			sb.append(" ");
 			sb.append(Formater.formatList(msg, times.toArray()));
 		}
 		return failed;
 	}
 
 	/**
 	 * Append the competitors start nbr, name, total time and if any missing
 	 * start/finish times to the stringbuilder.
 	 * 
 	 * @param sb
 	 *            the stringbuilder to append to
 	 * @param c
 	 *            the competitor which info to append
 	 */
 	private void appendCompetitorInfo(StringBuilder sb, Competitor c) {
 		Time totalTime = totalTime(c);
 
 		sb.append(Formater.formatColumns(c.getIndex(), c.getName(), totalTime,
 				(c.getStartTimes().isEmpty() ? NO_START : c.getStartTimes()
 						.get(0).toString()),
 				(c.getFinishTimes().isEmpty() ? NO_END : c.getFinishTimes()
 						.get(0).toString())));
 	}
 
 	@Override
 	protected void appendRows(FileWriter fileWriter,
 			List<Competitor> competitors) throws IOException {
 		fileWriter.append(Formater.formatColumns(Formater.START_NR,
 				Formater.NAME, Formater.TOTAL_TIME, Formater.START_TIME,
 				Formater.FINISH_TIME));
 		fileWriter.append("\n");
 	}
 }
