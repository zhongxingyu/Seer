 package org.logparser.config;
 
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.lang.builder.ReflectionToStringBuilder;
 import org.logparser.ILogFilter;
 import org.logparser.io.LogFiles;
 
 /**
  * Represents {@link ILogFilter} configuration obtained from the JSON file.
  * Contains configuration parameters for the participants.
  * 
  * @author jorge.decastro
 * TODO split it, as it became the 'The Bloated One'
  */
 public class Config {
	public static final String DEFAULT_DECIMAL_FORMAT = "#.#####";
 
 	private String friendlyName;
 	private FilterParams filterParams;
 	private StatsParams statsParams;
 	private ChartParams chartParams;
 	private SamplerConfig samplerConfig;
 	private String decimalFormat;
 	private LogFiles logFiles;
 	private boolean filteredEntriesStored;
 
 	public Config() {
 		decimalFormat = DEFAULT_DECIMAL_FORMAT;
 		filteredEntriesStored = true;
 	}
 
 	public String getFriendlyName() {
 		return friendlyName;
 	}
 
 	public void setFriendlyName(final String name) {
 		this.friendlyName = name;
 	}
 
 	public FilterParams getFilterParams() {
 		return filterParams;
 	}
 
 	public void setFilterParams(final FilterParams filterParams) {
 		this.filterParams = filterParams;
 	}
 
 	public StatsParams getStatsParams() {
 		return statsParams;
 	}
 
 	public void setStatsParams(final StatsParams statsParams) {
 		this.statsParams = statsParams;
 	}
 
 	public SamplerConfig getSampler() {
 		return samplerConfig;
 	}
 
 	public void setSampler(final SamplerConfig samplerConfig) {
 		this.samplerConfig = samplerConfig;
 	}
 
 	public LogFiles getLogFiles() {
 		return logFiles;
 	}
 
 	public void setLogFiles(final LogFiles logFiles) {
 		this.logFiles = logFiles;
 	}
 
 	public String getDecimalFormat() {
 		return decimalFormat;
 	}
 
 	public void setDecimalFormat(final String decimalFormat) {
 		this.decimalFormat = decimalFormat;
 	}
 
 	public boolean isFilteredEntriesStored() {
 		return filteredEntriesStored;
 	}
 
 	public void setFilteredEntriesStored(final boolean filteredEntriesStored) {
 		this.filteredEntriesStored = filteredEntriesStored;
 	}
 
 	public ChartParams getChartParams() {
 		return chartParams;
 	}
 
 	public void setChartParams(final ChartParams chartParams) {
 		this.chartParams = chartParams;
 	}
 
 	public void validate() {
 		if (filterParams == null) {
 			throw new IllegalArgumentException("'filterParams' property is required. Check configuration file.");
 		}
 		if (logFiles == null) {
 			throw new IllegalArgumentException("'logFiles' property is required. Check configuration file.");
 		}
 	}
 
 	@Override
 	public String toString() {
 		return ReflectionToStringBuilder.toString(this);
 	}
 
 	// TODO Replace tagged class w/ class hierarchy
 	public static class SamplerConfig {
 		public enum SampleBy {
 			TIME, FREQUENCY
 		};
 
 		public SampleBy sampleBy;
 		public int value;
 		public TimeUnit timeUnit;
 
 		@Override
 		public String toString() {
 			return ReflectionToStringBuilder.toString(this);
 		}
 	}
 }
