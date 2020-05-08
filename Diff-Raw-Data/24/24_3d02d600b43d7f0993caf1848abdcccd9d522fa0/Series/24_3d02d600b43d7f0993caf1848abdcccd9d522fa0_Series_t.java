 package dna.series;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import dna.graph.Graph;
 import dna.graph.GraphGenerator;
 import dna.io.filesystem.Dir;
 import dna.metrics.Metric;
 import dna.series.data.BatchData;
 import dna.series.data.RunData;
 import dna.series.data.RunTime;
 import dna.series.data.SeriesData;
 import dna.series.data.Value;
 import dna.updates.Batch;
 import dna.updates.BatchGenerator;
 import dna.updates.BatchSanitizationStats;
 import dna.updates.Update;
 import dna.util.Log;
 import dna.util.Rand;
 import dna.util.Timer;
 
 @SuppressWarnings("rawtypes")
 public class Series {
 
 	public Series(GraphGenerator gg, BatchGenerator bg, Metric[] metrics,
 			String dir) {
 		this.gg = gg;
 		this.bg = bg;
 		this.metrics = metrics;
 		this.dir = dir;
 	}
 
 	public SeriesData generate(int runs, int batches)
 			throws AggregationException, IOException {
 		return this.generate(runs, batches, true, true);
 	}
 
 	public SeriesData generate(int runs, int batches, boolean compare,
 			boolean write) throws AggregationException, IOException {
 
 		Log.infoSep();
 		Timer timer = new Timer("seriesGeneration");
 		Log.info("generating series");
 		Log.infoSep();
 		Log.info("ds = " + this.gg.getDatastructures());
 		Log.info("gg = " + this.gg.getDescription());
 		Log.info("bg = " + this.bg.getDescription());
 		Log.info("p  = " + this.dir);
 		StringBuffer buff = new StringBuffer("");
 		for (Metric m : this.metrics) {
 			if (buff.length() > 0) {
 				buff.append("\n     ");
 			}
 			buff.append(m.getDescription());
 		}
 		Log.info("m  = " + buff.toString());
 
 		SeriesData sd = new SeriesData(this.dir, runs);
 
 		// generate all runs
 		for (int r = 0; r < runs; r++) {
 			sd.addRun(this.generateRun(r, batches, compare, write));
 		}
 
 		// aggregate all runs
 		RunData aggregation = Aggregation.aggregate(sd);
 		sd.setAggregation(aggregation);
 		if (write) {
 			aggregation.write(Dir.getAggregationDataDir(dir));
 		}
 
 		Log.infoSep();
 		timer.end();
 		Log.info("total time: " + timer.toString());
 		Log.infoSep();
 
 		return sd;
 	}
 
 	public RunData generateRun(int run, int batches, boolean compare,
 			boolean write) throws IOException {
 
 		Log.infoSep();
 		Timer timer = new Timer("runGeneration");
 		Log.info("run " + run + " (" + batches + " batches)");
 
 		RunData rd = new RunData(run, batches + 1);
 
 		// reset batch generator
 		this.bg.reset();
 
 		// generate initial data
 		BatchData initialData = this.generateInitialData();
 		if (compare) {
 			this.compareMetrics();
 		}
 		rd.getBatches().add(initialData);
 		if (write) {
 			initialData.write(Dir.getBatchDataDir(this.dir, run,
 					initialData.getTimestamp()));
 		}
 
 		// generate batch data
 		for (int i = 0; i < batches; i++) {
 			BatchData batchData = this.generateNextBatch(i + 1);
 			if (compare) {
 				this.compareMetrics();
 			}
 			rd.getBatches().add(batchData);
 			if (write) {
 				batchData.write(Dir.getBatchDataDir(this.dir, run,
 						batchData.getTimestamp()));
 			}
 		}
 
 		timer.end();
 		Log.info(timer.toString());
 
 		return rd;
 
 	}
 
 	private boolean compareMetrics() {
 		boolean ok = true;
 		for (int i = 0; i < this.metrics.length; i++) {
 			for (int j = i + 1; j < this.metrics.length; j++) {
 				if (i == j) {
 					continue;
 				}
 				if (!this.metrics[i].isComparableTo(this.metrics[j])) {
 					continue;
 				}
 				if (!this.metrics[i].equals(this.metrics[j])) {
 					Log.error(this.metrics[i].getDescription() + " != "
 							+ this.metrics[j].getDescription());
 					ok = false;
 				}
 			}
 		}
 		return ok;
 	}
 
 	public BatchData generateInitialData() {
 
 		Timer totalTimer = new Timer("total");
 
 		Log.info("    inital data");
 
 		long seed = System.currentTimeMillis();
 		seed = 0;
 		Rand.init(seed);
 
 		// generate graph
 		Timer graphGenerationTimer = new Timer("graphGeneration");
 		this.g = this.gg.generate();
 		graphGenerationTimer.end();
 		for (Metric m : this.metrics) {
 			m.setGraph(this.g);
 		}
 
 		// initialize data
 		BatchData initialData = new BatchData(this.g.getTimestamp(), 0, 4,
 				this.metrics.length, this.metrics.length);
 
 		// initial computation of all metrics
 		Timer allMetricsTimer = new Timer("metrics");
 		for (Metric m : metrics) {
 			Timer metricTimer = new Timer(m.getName());
 			m.init();
 			m.compute();
 			metricTimer.end();
 			initialData.getMetrics().add(m.getData());
 			initialData.getMetricRuntimes().add(metricTimer.getRuntime());
 		}
 		allMetricsTimer.end();
 
 		totalTimer.end();
 
 		// add general runtimes
 		initialData.getGeneralRuntimes().add(totalTimer.getRuntime());
 		initialData.getGeneralRuntimes().add(graphGenerationTimer.getRuntime());
 		initialData.getGeneralRuntimes().add(allMetricsTimer.getRuntime());
 		addSummaryRuntimes(initialData);
 
 		// add values
 		initialData.getValues().add(new Value("randomSeed", seed));
 
 		return initialData;
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public BatchData generateNextBatch(long timestamp) {
 
 		long seed = System.currentTimeMillis();
 		seed = 0;
 		Rand.init(seed);
 
 		int addedNodes = 0;
 		int removedNodes = 0;
 		int updatedNodeWeights = 0;
 		int addedEdges = 0;
 		int removedEdges = 0;
 		int updatedEdgeWeights = 0;
 
 		Timer totalTimer = new Timer("total");
 
 		Timer batchGenerationTimer = new Timer("batchGeneration");
 		Batch b = this.bg.generate(this.g);
 		batchGenerationTimer.end();
 
 		Log.info("    " + b.toString());
 
 		BatchData batchData = new BatchData(b.getTo(), 5, 5, metrics.length,
 				metrics.length);
 
 		Timer graphUpdateTimer = new Timer("graphUpdate");
 
 		// init metric timers
 		HashMap<Metric, Timer> timer = new HashMap<Metric, Timer>();
 		for (Metric m : this.metrics) {
 			Timer t = new Timer(m.getName());
 			t.end();
 			timer.put(m, t);
 		}
 		Timer metricsTotal = new Timer("metrics");
 		metricsTotal.end();
 
 		// apply before batch
 		metricsTotal.restart();
 		for (Metric m : this.metrics) {
 			if (m.isAppliedBeforeBatch()) {
 				timer.get(m).restart();
 				m.applyBeforeBatch(b);
 				timer.get(m).end();
 			}
 		}
 		metricsTotal.end();
 
 		BatchSanitizationStats sanitizationStats = b.sanitize();
 		if (sanitizationStats.getTotal() > 0) {
 			Log.info("      " + sanitizationStats);
 			Log.info("      => " + b.toString());
 		}
 
 		this.applyUpdates(b.getNodeRemovals(), graphUpdateTimer, metricsTotal,
 				timer);
 		this.applyUpdates(b.getEdgeRemovals(), graphUpdateTimer, metricsTotal,
 				timer);
 
 		this.applyUpdates(b.getNodeAdditions(), graphUpdateTimer, metricsTotal,
 				timer);
 		this.applyUpdates(b.getEdgeAdditions(), graphUpdateTimer, metricsTotal,
 				timer);
 
 		this.applyUpdates(b.getNodeWeightUpdates(), graphUpdateTimer,
 				metricsTotal, timer);
 		this.applyUpdates(b.getEdgeWeightUpdates(), graphUpdateTimer,
 				metricsTotal, timer);
 
 		this.g.setTimestamp(timestamp);
 
 		// apply after batch
 		metricsTotal.restart();
 		for (Metric m : this.metrics) {
 			if (m.isAppliedAfterBatch()) {
 				timer.get(m).restart();
 				m.applyAfterBatch(b);
 				timer.get(m).end();
 			}
 		}
 		metricsTotal.end();
 
 		// compute / cleanup
 		metricsTotal.restart();
 		for (Metric m : this.metrics) {
 			if (m.isRecomputed()) {
 				timer.get(m).restart();
 				m.init();
 				m.compute();
 				timer.get(m).end();
 			}
 		}
 		metricsTotal.end();
 
 		totalTimer.end();
 
 		// add values
 		batchData.getValues().add(
 				new Value("nodesToAdd", b.getNodeAdditionCount()));
 		batchData.getValues().add(new Value("addedNodes", addedNodes));
 		batchData.getValues().add(
 				new Value("nodesToRemove", b.getNodeRemovalCount()));
 		batchData.getValues().add(new Value("removedNodes", removedNodes));
 		batchData.getValues().add(
 				new Value("nodeWeightsToUpdate", b.getNodeWeightUpdateCount()));
 		batchData.getValues().add(
 				new Value("updatedNodeWeights", updatedNodeWeights));
 
 		batchData.getValues().add(
 				new Value("edgesToAdd", b.getEdgeAdditionCount()));
 		batchData.getValues().add(new Value("addedEdges", addedEdges));
 		batchData.getValues().add(
 				new Value("edgesToRemove", b.getEdgeRemovalCount()));
 		batchData.getValues().add(new Value("removedEdges", removedEdges));
 		batchData.getValues().add(
 				new Value("edgeWeightsToUpdate", b.getEdgeWeightUpdateCount()));
 		batchData.getValues().add(
 				new Value("updatedEdgeWeights", updatedEdgeWeights));
 
 		batchData.getValues().add(
 				new Value("deletedEdgeAdditions", sanitizationStats
 						.getDeletedEdgeAdditions()));
 		batchData.getValues().add(
 				new Value("deletedEdgeAdditions", sanitizationStats
 						.getDeletedEdgeRemovals()));
 		batchData.getValues().add(
 				new Value("deletedNodeWeightUpdates", sanitizationStats
 						.getDeletedNodeWeightUpdates()));
 		batchData.getValues().add(
 				new Value("deletedEdgeWeightUpdates", sanitizationStats
 						.getDeletedEdgeWeightUpdates()));
 
 		batchData.getValues().add(new Value("randomSeed", seed));
 
 		// add metric data
 		for (Metric m : this.metrics) {
 			batchData.getMetrics().add(m.getData());
 		}
 
 		// add metric runtimes
 		for (Metric m : this.metrics) {
 			batchData.getMetricRuntimes().add(timer.get(m).getRuntime());
 		}
 
 		// add general runtimes
 		batchData.getGeneralRuntimes().add(totalTimer.getRuntime());
 		batchData.getGeneralRuntimes().add(batchGenerationTimer.getRuntime());
 		batchData.getGeneralRuntimes().add(graphUpdateTimer.getRuntime());
 		batchData.getGeneralRuntimes().add(metricsTotal.getRuntime());
 		addSummaryRuntimes(batchData);
 
 		return batchData;
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private int applyUpdates(Iterable<Update> updates, Timer graphUpdateTimer,
 			Timer metricsTotal, HashMap<Metric, Timer> timer) {
 
 		int counter = 0;
 		for (Update u : updates) {
 
 			// apply update to metrics beforehand
 			metricsTotal.restart();
 			for (Metric m : this.metrics) {
 				if (m.isAppliedBeforeUpdate()) {
 					timer.get(m).restart();
 					m.applyBeforeUpdate(u);
 					timer.get(m).end();
 				}
 			}
 			metricsTotal.end();
 
 			// update graph datastructures
 			graphUpdateTimer.restart();
 			boolean success = u.apply(this.g);
 			graphUpdateTimer.end();
 			if (!success) {
				Log.error("could not apply update " + u.toString()
 						+ " (BUT metric before update already applied)");
 				continue;
 			}
 			counter++;
 
 			// apply update to metrics afterwards
 			metricsTotal.restart();
 			for (Metric m : this.metrics) {
 				if (m.isAppliedAfterUpdate()) {
 					timer.get(m).restart();
 					m.applyAfterUpdate(u);
 					timer.get(m).end();
 				}
 			}
 			metricsTotal.end();
 		}
 
 		return counter;
 
 	}
 
 	private static void addSummaryRuntimes(BatchData batchData) {
 		double total = batchData.getGeneralRuntimes().get("total").getRuntime();
 		double metrics = batchData.getGeneralRuntimes().get("metrics")
 				.getRuntime();
 		double sum = sumRuntimes(batchData) - total - metrics;
 		double overhead = total - sum;
 		batchData.getGeneralRuntimes().add(new RunTime("sum", sum));
 		batchData.getGeneralRuntimes().add(new RunTime("overhead", overhead));
 	}
 
 	private static long sumRuntimes(BatchData batchData) {
 		long sum = 0;
 		for (RunTime rt : batchData.getGeneralRuntimes().getList()) {
 			sum += rt.getRuntime();
 		}
 		for (RunTime rt : batchData.getMetricRuntimes().getList()) {
 			sum += rt.getRuntime();
 		}
 		return sum;
 	}
 
 	private GraphGenerator gg;
 
 	private BatchGenerator bg;
 
 	private Metric[] metrics;
 
 	private String dir;
 
 	private Graph g;
 
 	public GraphGenerator getGraphGenerator() {
 		return this.gg;
 	}
 
 	public BatchGenerator getBatchGenerator() {
 		return this.bg;
 	}
 
 	public Metric[] getMetrics() {
 		return this.metrics;
 	}
 
 	public String getDir() {
 		return this.dir;
 	}
 
 	public Graph getGraph() {
 		return this.g;
 	}
 
 }
