 package dna.io.filesystem;
 
 import java.io.File;
 
 import dna.io.filter.PrefixFilenameFilter;
 
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
 		return dir + Names.runAggregation + Dir.delimiter;
 	}
 
 	public static String getAggregationBatchDir(String dir, long timestamp) {
 		return Dir.getAggregationDataDir(dir) + Prefix.batchDataDir + timestamp
 				+ Dir.delimiter;
 	}
 
 	public static String getAggregatedMetricDataDir(String dir, long timestamp,
 			String name) {
		return Dir.getAggregationBatchDir(dir, timestamp) + name
 				+ Dir.delimiter;
 	}
 
 	/*
 	 * RUN data
 	 */
 
 	public static String getRunDataDir(String dir, int run) {
 		return dir + Prefix.runDataDir + run + Dir.delimiter;
 	}
 
 	public static String[] getRuns(String dir) {
 		return (new File(dir))
 				.list(new PrefixFilenameFilter(Prefix.runDataDir));
 	}
 
 	public static int getRun(String runFolderName) {
 		return Integer.parseInt(runFolderName.replaceFirst(Prefix.runDataDir,
 				""));
 	}
 
 	/*
 	 * BATCH data
 	 */
 
 	public static String getBatchDataDir(String dir, long timestamp) {
 		return dir + Prefix.batchDataDir + timestamp + Dir.delimiter;
 	}
 
 	public static String getBatchDataDir(String dir, int run, long timestamp) {
 		return Dir.getRunDataDir(dir, run) + Prefix.batchDataDir + timestamp
 				+ Dir.delimiter;
 	}
 
 	public static String[] getBatches(String dir) {
 		return (new File(dir)).list(new PrefixFilenameFilter(
 				Prefix.batchDataDir));
 	}
 
 	public static long getTimestamp(String batchFolderName) {
 		return Long.parseLong(batchFolderName.replaceFirst(Prefix.batchDataDir,
 				""));
 	}
 
 	/*
 	 * METRIC data
 	 */
 
 	public static String getMetricDataDir(String dir, String name) {
 		return dir + Prefix.metricDataDir + name + Dir.delimiter;
 	}
 
 	public static String getMetricDataDir(String dir, int run, long timestamp,
 			String name) {
 		return Dir.getBatchDataDir(dir, run, timestamp) + Prefix.metricDataDir
 				+ name + Dir.delimiter;
 	}
 
 	public static String[] getMetrics(String dir) {
 		return (new File(dir)).list(new PrefixFilenameFilter(
 				Prefix.metricDataDir));
 	}
 
 	public static String getMetricName(String metricFolderName) {
 		return metricFolderName.replaceFirst(Prefix.metricDataDir, "");
 	}
 
 }
