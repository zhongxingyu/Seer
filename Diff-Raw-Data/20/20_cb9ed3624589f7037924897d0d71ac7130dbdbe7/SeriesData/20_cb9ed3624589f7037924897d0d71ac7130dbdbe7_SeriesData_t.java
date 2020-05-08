 package dna.series.data;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import dna.io.filesystem.Dir;
 import dna.metrics.Metric.MetricType;
 import dna.series.aggdata.AggregatedSeries;
 import dna.series.lists.MetricDataList;
 import dna.util.Config;
 import dna.util.Log;
 
 public class SeriesData {
 
 	public SeriesData(String dir, String name) {
 		this.dir = dir;
 		this.name = name;
 		this.runs = new ArrayList<RunData>();
 		this.aggregation = null;
 	}
 
 	public SeriesData(String dir, String name, int size) {
 		this.dir = dir;
 		this.name = name;
 		this.runs = new ArrayList<RunData>(size);
 		this.aggregation = null;
 	}
 
 	public SeriesData(String dir, String name, RunData[] runs) {
 		this(dir, name, runs.length);
 		for (RunData run : runs) {
 			this.runs.add(run);
 		}
 		this.aggregation = null;
 	}
 
 	public SeriesData(String dir, String name, RunData[] runs,
 			AggregatedSeries aggregation) {
 		this(dir, name, runs);
 		// this.aggregation = aggregation;
 	}
 
 	private String dir;
 
 	public String getDir() {
 		return this.dir;
 	}
 
 	private String name;
 
 	public String getName() {
 		return this.name;
 	}
 
 	private ArrayList<RunData> runs;
 
 	public ArrayList<RunData> getRuns() {
 		return this.runs;
 	}
 
 	public RunData getRun(int index) {
 		return this.runs.get(index);
 	}
 
 	public void addRun(RunData run) {
 		this.runs.add(run);
 	}
 
 	private AggregatedSeries aggregation;
 
 	public AggregatedSeries getAggregation() {
 		return this.aggregation;
 	}
 
 	public void setAggregation(AggregatedSeries aggregation) {
 		this.aggregation = aggregation;
 	}
 
 	/**
 	 * This method tests if two different SeriesData objects are from the same
 	 * type and can be compared. Checks: - same amount of runs - same runs (uses
 	 * RunData.sameType())
 	 * 
 	 * @author Rwilmes
 	 * @date 15.07.2013
 	 */
 	public static boolean isSameType(SeriesData s1, SeriesData s2) {
 		Log.debug("Comparing SeriesData " + s1.getName() + " and SeriesData "
 				+ s2.getName());
 
 		if (s1.getRuns().size() != s2.getRuns().size()) {
 			Log.warn("different amount of runs on series " + s1.getName()
 					+ " and series " + s2.getName());
 			return false;
 		}
 
 		for (int i = 0; i < s1.getRuns().size(); i++) {
 			RunData.isSameType(s1.getRun(i), s2.getRun(i));
 		}
 
 		return true;
 	}
 
 	public static SeriesData read(String dir, String name,
 			boolean readAggregation, boolean readValues) throws IOException {
 		String[] runs = Dir.getRuns(dir);
 		RunData[] runList = new RunData[runs.length];
 
 		for (String run : runs) {
 			int runId = Dir.getRun(run);
 			runList[runId] = RunData.read(Dir.getRunDataDir(dir, runId), runId,
 					readValues);
 		}
 		if (readAggregation) {
 			AggregatedSeries aggr = AggregatedSeries
 					.read(dir, name, readValues);
 			return new SeriesData(dir, name, runList, aggr);
 		} else {
 			return new SeriesData(dir, name, runList);
 		}
 
 	}
 
 	/**
 	 * This method compares every metric that is flagged with 'heuristic' with
 	 * an 'exact' metric that is comparable, thus calculating a 'quality'
 	 * MetricData object.
 	 * 
 	 * @param writeValues
 	 *            Flag that indicates if the resulting 'quality' MetricData
 	 *            object will be written on the filesystem.
 	 * 
 	 * @author Rwilmes
 	 * @date 15.07.2013
 	 */
 	public void compareMetrics(boolean writeValues) throws IOException,
 			InterruptedException {
 		Log.info("comparing metrics");
 		MetricDataList exacts = new MetricDataList();
 		MetricDataList heuristics = new MetricDataList();
 
 		for (MetricData metric : this.getRuns().get(0).getBatches().get(0)
 				.getMetrics().getList()) {
 			if (metric.getType() != null) {
 				if (metric.getType().equals(MetricType.exact))
 					exacts.add(metric);
 				if (metric.getType().equals(MetricType.heuristic))
 					heuristics.add(metric);
 			}
 		}
 		for (MetricData heuristic : heuristics.getList()) {
 			boolean compared = false;
 
 			int similarities = 0;
 			String bestMatch = "";
 			for (MetricData exactMetric : exacts.getList()) {
 				if (MetricData.countSimilarities(heuristic, exactMetric) > similarities) {
 					similarities = MetricData.countSimilarities(heuristic,
 							exactMetric);
 					bestMatch = exactMetric.getName();
 				}
 			}
 
 			for (String exact : exacts.getNames()) {
 				if (!compared && exact.equals(bestMatch)) {
 					Log.info("  => heuristic \"" + heuristic.getName()
 							+ "\" with exact \"" + exacts.get(exact).getName()
 							+ "\"");
 					if (MetricData.isComparable(heuristic, exacts.get(exact))) {
 						for (int run = 0; run < this.getRuns().size(); run++) {
 							for (int batch = 0; batch < this.getRun(run)
 									.getBatches().size(); batch++) {
 								MetricData exactTemp = MetricData.read(Dir
 										.getMetricDataDir(
 												Dir.getBatchDataDir(Dir
 														.getRunDataDir(
 																this.dir, run),
 														batch),
 												exacts.get(exact).getName(),
 												MetricType.exact),
 										exacts.get(exact).getName(), true);
 								exactTemp.setType(MetricType.exact);
 
 								MetricData heuristicTemp = MetricData.read(Dir
 										.getMetricDataDir(
 												Dir.getBatchDataDir(Dir
 														.getRunDataDir(
 																this.dir, run),
 														batch), heuristic
 														.getName(),
 												MetricType.heuristic),
 										heuristic.getName(), true);
 								heuristicTemp.setType(MetricType.heuristic);
 								MetricData quality = MetricData.compare(
 										exactTemp, heuristicTemp);
 								this.getRuns().get(run).getBatches().get(batch)
 										.getMetrics().add(quality);
 								if (writeValues)
 									quality.write(Dir.getMetricDataDir(
 											Dir.getBatchDataDir(
 													Dir.getRunDataDir(dir, run),
 													batch),
											quality.getName()));
 							}
 						}
 					}
 					compared = true;
 				}
 			}
 		}
 	}
 }
