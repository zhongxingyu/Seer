 package org.logparser;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
  
 /**
  * Provides a summary and descriptive statistics for a collection of
  * {@link ITimestampedEntry}s from a log.
  * 
  * @author jorge.decastro
  * 
  * @param <E> the type of log entries held.
  */
 public class StatsSnapshot<E extends ITimestampedEntry> implements IStatsView<E>, Serializable {
 	private static final long serialVersionUID = -3984417761751696213L;
 	private final List<E> entries;
 	private final ObjectMapper jsonMapper;
 	private E maxima;
 	private E minima;
 	private double mean;
 	private double std;
 	private final int groupBy;
 	private final Map<Integer, Integer> timeBreakdown;
 	private final Calendar calendar;
 	private DescriptiveStats ds = new DescriptiveStats();
  
 	public StatsSnapshot() {
 		entries = new ArrayList<E>();
 		jsonMapper = new ObjectMapper();
 		timeBreakdown = new TreeMap<Integer, Integer>();
 		calendar = Calendar.getInstance();
		groupBy = Calendar.HOUR_OF_DAY;
 	}
  
 	public void add(final E newEntry) {
 		entries.add(newEntry);
 		if (maxima == null || (newEntry.getDuration() > maxima.getDuration())) {
 			maxima = newEntry;
 		}
 		if (minima == null || (newEntry.getDuration() < minima.getDuration())) {
 			minima = newEntry;
 		}
 		ds.calculate(Double.valueOf(newEntry.getDuration()), ds.getMean(), ds.getVariance(), ds.getObservations());
 		this.mean = ds.getMean();
 		this.std = ds.getStd();
 		calendar.setTimeInMillis(newEntry.getTimestamp());
 		int key = calendar.get(groupBy);
 		if (timeBreakdown.containsKey(key)) {
 			int value = timeBreakdown.get(key);
 			value++;
 			timeBreakdown.put(key, value);
 		} else {
 			timeBreakdown.put(key, 1);
 		}
 	}
  
 	public List<E> getEntries() {
 		return entries;
 	}
  
 	@JsonIgnore
 	public E getEarliestEntry() {
 		if (!entries.isEmpty()) {
 			return entries.get(0);
 		}
 		return null;
 	}
  
 	@JsonIgnore
 	public E getLatestEntry() {
 		if (!entries.isEmpty()) {
 			return entries.get(entries.size() - 1);
 		}
 		return null;
 	}
  
 	public E getMaxima() {
 		return maxima;
 	}
  
 	public E getMinima() {
 		return minima;
 	}
  
 	public double getDeviation() {
 		return std;
 	}
  
 	public double getMean() {
 		return mean;
 	}
 	
 	public Map<Integer, Integer> getTimeBreakdown() {
 		return timeBreakdown;
 	}
  
 	public String toCsvString() {
 		return String.format("\"%s\", \"%s\", \"%s\", \"%s, \"%s\"", entries.size(), maxima, minima, mean, std);
 	}
  
 	@Override
 	public String toString() {
 		return String.format("%s, %s, %s, %s", mean, std, maxima, minima);
 	}
 	
 	public String toJsonString() {
 		try {
 			return jsonMapper.writeValueAsString(this);
 		} catch (JsonGenerationException jge) {
 		} catch (JsonMappingException jme) {
 		} catch (IOException ioe) {
 		}
 		return null;
 	}
  
 	// TODO refactor; move out & inject here
 	public static class DescriptiveStats implements Serializable {
 		private static final long serialVersionUID = 2268207471613686207L;
 		private static final String PRECISION_PATTERN = "##.##";
 		private int observations; // observations counter
 		private double mean; // arithmetic mean
 		private double variance; // partial variance calculation
 		private final DecimalFormat decimalFormatter;
  
 		public DescriptiveStats() {
 			observations = 0;
 			mean = 0D;
 			variance = 0D;
 			decimalFormatter = new DecimalFormat(PRECISION_PATTERN);
 		}
  
 		public void calculate(final double datapoint, final double oldMean, final double partialVar, final int obs) {
 			observations = obs + 1;
 			double delta = datapoint - oldMean;
 			mean = oldMean + (delta / observations);
 			variance = partialVar + (delta * (datapoint - mean));
 		}
  
 		public Double getMean() {
 			return Double.valueOf(decimalFormatter.format(mean));
 		}
  
 		public int getObservations() {
 			return observations;
 		}
  
 		public Double getStd() {
 			return Double.valueOf(decimalFormatter.format(Math.sqrt(variance)));
 		}
  
 		public Double getVariance() {
 			int obs = observations > 1 ? observations - 1 : 1;
 			return Double.valueOf(decimalFormatter.format(variance / obs));
 		}
 	}
 }
