 package dna.io.filesystem;
 
 import java.io.File;
 
 import dna.io.filter.PrefixFilenameFilter;
 import dna.metrics.Metric.MetricType;
 import dna.util.Config;
 
 /**
  * 
  * Gives the default storage path for data objects.
  * 
  * @author benni
  * 
  */
 public class Dir {
 	public static final String delimiter = "/";
 
 	/*
 	 * AGGREGATION
 	 */
 
 	public static String getAggregationDataDir(String dir) {
 		return dir + Config.get("RUN_AGGREGATION") + Dir.delimiter;
 	}
 
 	public static String getAggregationBatchDir(String dir, long timestamp) {
 		return Dir.getAggregationDataDir(dir)
 				+ Config.get("PREFIX_BATCHDATA_DIR") + timestamp
 				+ Dir.delimiter;
 	}
 
 	public static String getAggregatedMetricDataDir(String dir, long timestamp,
 			String name) {
 		return Dir.getAggregationBatchDir(dir, timestamp)
 				+ Config.get("PREFIX_METRICDATA_DIR") + name + Dir.delimiter;
 	}
 
 	/*
 	 * RUN data
 	 */
 
 	public static String getRunDataDir(String dir, int run) {
 		return dir + Config.get("PREFIX_RUNDATA_DIR") + run + Dir.delimiter;
 	}
 
 	public static String[] getRuns(String dir) {
 		return (new File(dir)).list(new PrefixFilenameFilter(Config
 				.get("PREFIX_RUNDATA_DIR")));
 	}
 
 	public static int getRun(String runFolderName) {
 		return Integer.parseInt(runFolderName.replaceFirst(
 				Config.get("PREFIX_RUNDATA_DIR"), ""));
 	}
 
 	/*
 	 * BATCH data
 	 */
 
 	public static String getBatchDataDir(String dir, long timestamp) {
 		return dir + Config.get("PREFIX_BATCHDATA_DIR") + timestamp
 				+ Dir.delimiter;
 	}
 
 	public static String getBatchDataDir(String dir, int run, long timestamp) {
 		return Dir.getRunDataDir(dir, run) + Config.get("PREFIX_BATCHDATA_DIR")
 				+ timestamp + Dir.delimiter;
 	}
 
 	public static String[] getBatches(String dir) {
 		return (new File(dir)).list(new PrefixFilenameFilter(Config
 				.get("PREFIX_BATCHDATA_DIR")));
 	}
 
 	public static long getTimestamp(String batchFolderName) {
 		return Long.parseLong(batchFolderName.replaceFirst(
 				Config.get("PREFIX_BATCHDATA_DIR"), ""));
 	}
 
 	/*
 	 * METRIC data
 	 */
 
 	public static String getMetricDataDir(String dir, String name) {
 		return dir + Config.get("PREFIX_METRICDATA_DIR") + name + Dir.delimiter;
 	}
 
 	public static String getMetricDataDir(String dir, String name,
 			MetricType type) {
 		switch (type) {
 		case exact:
 			return dir + Config.get("PREFIX_METRICDATA_DIR") + name
 					+ Config.get("SUFFIX_METRIC_EXACT") + Dir.delimiter;
 		case heuristic:
 			return dir + Config.get("PREFIX_METRICDATA_DIR") + name
 					+ Config.get("SUFFIX_METRIC_HEURISTIC") + Dir.delimiter;
 		case quality:
 			return dir + Config.get("PREFIX_METRICDATA_DIR") + name
					+ Dir.delimiter;
 		default:
 			return dir + Config.get("PREFIX_METRICDATA_DIR") + name
 					+ Dir.delimiter;
 		}
 	}
 
 	public static String getMetricDataDir(String dir, int run, long timestamp,
 			String name) {
 		return Dir.getBatchDataDir(dir, run, timestamp)
 				+ Config.get("PREFIX_METRICDATA_DIR") + name + Dir.delimiter;
 	}
 
 	public static String getMetricDataDir(String dir, int run, long timestamp,
 			String name, MetricType type) {
 		switch (type) {
 		case exact:
 			return Dir.getBatchDataDir(dir, run, timestamp)
 					+ Config.get("PREFIX_METRICDATA_DIR") + name
 					+ Config.get("SUFFIX_METRIC_EXACT") + Dir.delimiter;
 		case heuristic:
 			return Dir.getBatchDataDir(dir, run, timestamp)
 					+ Config.get("PREFIX_METRICDATA_DIR") + name
 					+ Config.get("SUFFIX_METRIC_HEURISTIC") + Dir.delimiter;
 		case quality:
 			return Dir.getBatchDataDir(dir, run, timestamp)
 					+ Config.get("PREFIX_METRICDATA_DIR") + name
 					+ Config.get("SUFFIX_METRIC_QUALITY") + Dir.delimiter;
 		default:
 			return Dir.getBatchDataDir(dir, run, timestamp)
 					+ Config.get("PREFIX_METRICDATA_DIR") + name
 					+ Dir.delimiter;
 		}
 	}
 
 	public static String[] getMetrics(String dir) {
 		return (new File(dir)).list(new PrefixFilenameFilter(Config
 				.get("PREFIX_METRICDATA_DIR")));
 	}
 
 	public static String getMetricName(String metricFolderName) {
 		return metricFolderName.replaceFirst(
 				Config.get("PREFIX_METRICDATA_DIR"), "");
 	}
 
 }
