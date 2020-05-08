 package org.logparser;
 
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 
 import net.jcip.annotations.Immutable;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.annotate.JsonPropertyOrder;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import com.google.common.base.Preconditions;
 
 /**
  * Represents a log file snapshot, containing filtered log {@code E}ntries and
  * log summaries.
  * 
  * @author jorge.decastro
  * 
  * @param <E> the type of log entries held.
  */
 @Immutable
 @JsonPropertyOrder( { "totalEntries", "summary", "timeBreakdown", "stats" })
 public class LogSnapshot<E extends ITimestampedEntry> implements IJsonSerializable, ICsvSerializable {
 	private final DecimalFormat decimalFormat;
 	private final List<E> filteredEntries;
 	private final Map<String, Integer> summary;
 	private final Map<Integer, Integer> timeBreakdown;
 	private final Map<String, IStatsView<E>> groupedByAction;
 	private final int groupBy;
 	private final Calendar calendar;
 	private final ObjectMapper jsonMapper;
 	private final Config config;
 	private int totalEntries;
 
 	public LogSnapshot(final Config config) {
 		Preconditions.checkNotNull(config);
 		this.filteredEntries = new ArrayList<E>();
 		this.summary = new TreeMap<String, Integer>();
 		this.timeBreakdown = new TreeMap<Integer, Integer>();
 		this.groupedByAction = new TreeMap<String, IStatsView<E>>();
 		this.config = config;
 		this.groupBy = config.groupByToCalendar();
 		this.calendar = Calendar.getInstance();
 		this.jsonMapper = new ObjectMapper();
 		this.decimalFormat = new DecimalFormat("####.##%");
 		this.totalEntries = 0;
 	}
 
 	public void consume(final E entry) {
 		totalEntries++;
 		if (entry != null) {
 			filteredEntries.add(entry);
 			updateStats(entry);
 			updateSummary(entry);
 			updateTimeBreakdown(entry);
 		}
 	}
 
 	private void updateStats(final E entry) {
 		String key = entry.getAction();
 		// new request? create a new stats wrapper for it
 		if (!groupedByAction.containsKey(key)) {
			IStatsView<E> stats = new StatsSnapshot<E>();
 			stats.add(entry);
 			groupedByAction.put(key, stats);
 		} else {
 			IStatsView<E> existingEntriesList = groupedByAction.get(key);
 			existingEntriesList.add(entry);
 		}
 	}
 
 	private void updateSummary(final E entry) {
 		String key = entry.getAction();
 		if (summary.containsKey(key)) {
 			Integer value = summary.get(key);
 			value++;
 			summary.put(key, value);
 		} else {
 			summary.put(key, 1);
 		}
 	}
 
 	private void updateTimeBreakdown(final E entry) {
 		calendar.setTimeInMillis(entry.getTimestamp());
 		int key = calendar.get(groupBy);
 		if (timeBreakdown.containsKey(key)) {
 			int value = timeBreakdown.get(key);
 			value++;
 			timeBreakdown.put(key, value);
 		} else {
 			timeBreakdown.put(key, 1);
 		}
 	}
 
 	public List<E> getFilteredEntries() {
 		return Collections.unmodifiableList(filteredEntries);
 	}
 
 	public int getTotalEntries() {
 		return totalEntries;
 	}
 
 	public Map<String, Integer> getSummary() {
 		return Collections.unmodifiableMap(summary);
 	}
 
 	public Map<Integer, Integer> getTimeBreakdown() {
 		return Collections.unmodifiableMap(timeBreakdown);
 	}
 
 	public Map<String, IStatsView<E>> getStats() {
 		return Collections.unmodifiableMap(groupedByAction);
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder("Action,\t # Total Entries,\t # Filtered Entries,\t Mean,\t Deviation,\t Maxima,\t Minima,\t");
 		sb.append(NEWLINE);
 		int filteredSize = getFilteredEntries().size();
 
 		IStatsView<E> stats = null;
 		for (Entry<String, IStatsView<E>> entries : groupedByAction.entrySet()) {
 			stats = entries.getValue();
 			sb.append(String.format("%s,\t %s,\t %s,\t %s,\t %s,\t %s,\t %s,\t", 
 					entries.getKey(), 
 					totalEntries, 
 					stats.getEntries().size(), 
 					stats.getMean(), 
 					stats.getDeviation(), 
 					stats.getMaxima().getText(), 
 					stats.getMinima().getText()));
 			sb.append(NEWLINE);
 
 			if (!entries.getValue().getTimeBreakdown().isEmpty()) {
 				sb.append(String.format("\t %s Breakdown,\t # Entries,\t %% Of Filtered,\t %% Of Total\t", config.getGroupBy()));
 				sb.append(NEWLINE);
 				sb.append(summarizeAsString(entries.getValue().getTimeBreakdown(), filteredSize, totalEntries, "\t %s,\t %s,\t %s,\t %s\t"));
 			}
 			sb.append(NEWLINE);
 		}
 
 		if (!getSummary().isEmpty()) {
 			sb.append(NEWLINE);
 			sb.append("Action,\t # Entries,\t % Of Filtered,\t % Of Total\t");
 			sb.append(NEWLINE);
 			sb.append(summarizeAsString(summary, filteredSize, totalEntries, "%s,\t %s,\t %s,\t %s\t"));
 		}
 
 		if (!getTimeBreakdown().isEmpty()) {
 			sb.append(NEWLINE);
 			sb.append(String.format("%s Breakdown,\t # Entries,\t %% Of Filtered,\t %% Of Total\t", config.getGroupBy()));
 			sb.append(NEWLINE);
 			sb.append(summarizeAsString(timeBreakdown, filteredSize, totalEntries, "%s,\t %s,\t %s,\t %s\t"));
 		}
 
 		sb.append(NEWLINE);
 		return sb.toString();
 	}
 
 	public String toJsonString() {
 		try {
 			return jsonMapper.writeValueAsString(this);
 			// TODO proper exception handling
 		} catch (JsonGenerationException jge) {
 		} catch (JsonMappingException jme) {
 		} catch (IOException ioe) {
 		}
 		return null;
 	}
 
 	public String toCsvString() {
 		StringBuilder sb = new StringBuilder("Action, # Total Entries, # Filtered Entries, Mean, Deviation, Maxima, Minima");
 		sb.append(NEWLINE);
 		int filteredSize = getFilteredEntries().size();
 		IStatsView<E> stats = null;
 		for (Entry<String, IStatsView<E>> entries : groupedByAction.entrySet()) {
 			stats = entries.getValue();
 			sb.append(String.format("\"%s\", %s, %s, \"%s\", \"%s\", \"%s\", \"%s\"", 
 					entries.getKey(), 
 					totalEntries, 
 					stats.getEntries().size(), 
 					stats.getMean(), 
 					stats.getDeviation(), 
 					stats.getMaxima().getText(), 
 					stats.getMinima().getText()));
 			sb.append(NEWLINE);
 
 			if (!entries.getValue().getTimeBreakdown().isEmpty()) {
 				sb.append(NEWLINE);
 				sb.append(String.format(", %s Breakdown, # Entries, %% Of Filtered, %% Of Total", config.getGroupBy()));
 				sb.append(NEWLINE);
 				sb.append(summarizeAsString(entries.getValue().getTimeBreakdown(), filteredSize, totalEntries, ", %s, %s, \"%s\", \"%s\""));
 			}
 			sb.append(NEWLINE);
 		}
 
 		if (!getSummary().isEmpty()) {
 			sb.append(NEWLINE);
 			sb.append("Action, # Entries, % Of Filtered, % Of Total");
 			sb.append(NEWLINE);
 			sb.append(summarizeAsString(summary, filteredSize, totalEntries, "\"%s\", %s, \"%s\", \"%s\""));
 		}
 
 		if (!getTimeBreakdown().isEmpty()) {
 			sb.append(NEWLINE);
 			sb.append(String.format("%s Breakdown, # Entries, %% Of Filtered, %% Of Total", config.getGroupBy()));
 			sb.append(NEWLINE);
 			sb.append(summarizeAsString(timeBreakdown, filteredSize, totalEntries, "%s, %s, \"%s\", \"%s\""));
 		}
 
 		sb.append(NEWLINE);
 		return sb.toString();
 	}
 
 	private String asPercentOf(final int value, final int total) {
 		return asPercentOf(value, total, decimalFormat);
 	}
 
 	private String asPercentOf(final int value, final int total, final DecimalFormat df) {
 		double percent = value > 0 ? value / (double) total : 0D;
 		return df.format(percent);
 	}
 
 	private <K> String summarizeAsString(final Map<K, Integer> summary, final int filteredEntries, final int totalEntries, final String formatString) {
 		int value = 0;
 		StringBuilder sb = new StringBuilder();
 		for (Entry<K, Integer> entries : summary.entrySet()) {
 			value = entries.getValue();
 			sb.append(String.format(formatString, entries.getKey(), entries.getValue(), asPercentOf(value, filteredEntries), asPercentOf(value, totalEntries)));
 			sb.append(NEWLINE);
 		}
 		return sb.toString();
 	}
 }
