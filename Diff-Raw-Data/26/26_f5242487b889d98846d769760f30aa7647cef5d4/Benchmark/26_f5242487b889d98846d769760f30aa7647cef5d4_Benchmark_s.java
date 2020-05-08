 /*
  * Copyright (c) 2010, Regents of the University of California
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  *  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of California, Berkeley
  * nor the names of its contributors may be used to endorse or promote
  * products derived from this software without specific prior written
  * permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package radlab.rain;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.util.LinkedList;
 import java.util.TreeMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.LogManager;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import radlab.rain.communication.RainPipe;
 import radlab.rain.communication.thrift.ThriftService;
 import radlab.rain.util.ConfigUtil;
 
 /**
  * The Benchmark class provides a framework to initialize and run a benchmark specified by a provided scenario.
  */
 public class Benchmark {
 
 	private static Logger logger = LoggerFactory.getLogger(Benchmark.class);
 
 	public static String ZK_PATH_SEPARATOR = "/";
 	public static Benchmark BenchmarkInstance = null;
 	public static Scenario BenchmarkScenario = null;
 
 	/** Name of the main benchmark thread. */
 	public String threadName = "Benchmark-thread";
 	/**
 	 * Amount of time (in milliseconds) to wait before threads start issuing requests. This allows all of the threads to start
 	 * synchronously.
 	 */
 	public long timeToStart = 5000;
 
 	public boolean waitingForStartSignal = false;
 
 	public static Benchmark getBenchmarkInstance() {
 		if (BenchmarkInstance == null)
 			BenchmarkInstance = new Benchmark();
 
 		return BenchmarkInstance;
 	}
 
 	public static Scenario getBenchmarkScenario() {
 		return BenchmarkScenario;
 	}
 
 	/**
 	 * Initializes the benchmark as specified by the provided scenario. This includes creating the n-threads needed, giving them
 	 * the scenario, and starting them.
 	 * 
 	 * @param scenario
 	 *            The scenario specifying parameters for the benchmark.
 	 */
 	public void start(Scenario scenario) throws Exception {
 		Thread.currentThread().setName(threadName);
 
 		// Create a new thread pool -- either a fixed or an unbounded thread
 		// pool.
 		// (In practice, we should put a limit on the size of the thread pool).
 		// The thread pool will be used to hand off asynchronous requests.
 
 		int sharedThreads = scenario.getMaxSharedThreads();
 		ExecutorService pool = Executors.newFixedThreadPool(sharedThreads);
 		logger.debug("Creating " + sharedThreads + " shared threads.");
 
 		LinkedList<LoadGenerationStrategy> threads = new LinkedList<LoadGenerationStrategy>();
 
 		// Calculate the run timings that will be used for all threads.
 		// start startS.S. endS.S. end
 		// | ramp up |------ duration ------| ramp down |
 		long start = System.currentTimeMillis() + timeToStart;
 		long startSteadyState = start + (scenario.getRampUp() * 1000);
 		long endSteadyState = startSteadyState + (scenario.getDuration() * 1000);
 
 		logger.info("Initializing " + scenario.getTracks().size() + " track(s).");
 		for (ScenarioTrack track : scenario.getTracks().values()) {
 			// Start the scoreboard. It needs to know the timings because we
 			// only
 			// want to retain metrics generated during the steady state
 			// interval.
 			IScoreboard scoreboard = track.createScoreboard(null);
 			if (scoreboard != null) {
 				scoreboard.initialize(startSteadyState, endSteadyState);
 				scoreboard.setMetricSnapshotInterval((long) (track.getMetricSnapshotInterval() * 1000));
 				scoreboard.setMetricWriter(track.getMetricWriter());
 				scoreboard.start();
 			}
 			track.setScoreboard(scoreboard);
 
 			// Let track register to receive messages from the Pipe
 
 			// Need some configuration parameters to indidcate:
 			// 1) whether to wait here for controller to contact us
 			// 2) whether to forge ahead
 
 			long maxUsers = track.getMaxUsers();
 			logger.info("Creating " + maxUsers + " threads for track.");
 			// Create enough threads for maximum users needed by the scenario.
 			for (int i = 0; i < maxUsers; i++) {
 				Generator generator = track.createWorkloadGenerator(track.getGeneratorClassName(), track.getGeneratorParams());
 				generator.setScoreboard(scoreboard);
 				generator.setMeanCycleTime((long) (track.getMeanCycleTime() * 1000));
 				generator.setMeanThinkTime((long) (track.getMeanThinkTime() * 1000));
 				// Allow the load generation strategy to be configurable
 				LoadGenerationStrategy lgThread = track.createLoadGenerationStrategy(track.getLoadGenerationStrategyClassName(),
 						track.getLoadGenerationStrategyParams(), generator, i);
 				generator.setName(lgThread.getName());
 				generator.initialize();
 				lgThread.setInteractive(track.getInteractive());
 				lgThread.setSharedWorkPool(pool);
 				lgThread.setTimeStarted(start);
 
 				threads.add(lgThread);
 
 				lgThread.start();
 			}
 		}
 
 		// Wait for all of the threads to finish.
 		for (LoadGenerationStrategy lgThread : threads) {
 			try {
 				lgThread.join();
 			} catch (InterruptedException ie) {
 				logger.error("Main thread interrupted... exiting!");
 			} finally {
 				lgThread.dispose();
 			}
 		}
 
 		// Purge threads.
 		logger.info("Purging threads and shutting down... exiting!");
 		threads.clear();
 
 		// Set up for stats aggregation across tracks based on the generators
 		// used
 		TreeMap<String, Scorecard> aggStats = new TreeMap<String, Scorecard>();
 
 		// Shutdown the scoreboards and tally up the results.
 		for (ScenarioTrack track : scenario.getTracks().values()) {
 			// Aggregate stats across track based on the generator class name.
 			// If the generator
 			// class names are identical then there is potentially overlap in
 			// the operations issued
 			// based on the mix matrix used (if any)
 			// Stop the scoreboard
 			track.getScoreboard().stop();
 			// Print out the stats for this track
 			track.getScoreboard().printStatistics(System.out);
 
 			// Get the name of the generator active for this track
 			String generatorClassName = track.getGeneratorClassName();
 			// Get the final scorecard for this track
 			Scorecard finalScorecard = track.getScoreboard().getFinalScorecard();
 			if (!aggStats.containsKey(generatorClassName)) {
 				StringBuffer buf = new StringBuffer();
 				buf.append(generatorClassName).append(" (agg)");
 				Scorecard aggCard = new Scorecard("aggregated", finalScorecard._intervalDuration, buf.toString());
 				aggStats.put(generatorClassName, aggCard);
 			}
 			// Get the current aggregated scorecard for this generator
 			Scorecard aggCard = aggStats.get(generatorClassName);
 			// Merge the final card for this track with the current per-driver
 			// aggregated scorecard
 			aggCard.merge(finalScorecard);
 			aggStats.put(generatorClassName, aggCard);
 			// Collect scoreboard results
 			// Collect object pool results
 			track.getObjectPool().shutdown();
 		}
 
 		// Check whether we're printing out aggregated stats
 		if (scenario.getAggregateStats()) {
 			// Print aggregated stats
 			if (aggStats.size() > 0)
 				logger.info("# aggregated stats: " + aggStats.size());
 
 			for (String generatorName : aggStats.keySet()) {
 				Scorecard card = aggStats.get(generatorName);
 
 				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
 				PrintStream stream = new PrintStream(buffer);
 				card.printStatistics(stream);
 
 				// If needed
 				// System.out.write(buffer.toByteArray());
 
 				// Sonar output
 				logger.info(buffer.toString());
 
 			}
 		}
 
 		// Shutdown the shared threadpool.
 		pool.shutdown();
 		try {
 			logger.debug("waiting up to 10 seconds for shared threadpool to shutdown!");
 			pool.awaitTermination(10000, TimeUnit.MILLISECONDS);
 			if (!pool.isTerminated()) {
 				pool.shutdownNow();
 			}
 		} catch (InterruptedException ie) {
 			logger.debug("INTERRUPTED while waiting for shared threadpool to shutdown!");
 		}
 
 		// Close down the pipe
 		if (RainConfig.getInstance()._usePipe) {
 			logger.debug("Shutting down the communication pipe!");
 			RainPipe.getInstance().stop();
 		}
 
 		if (RainConfig.getInstance()._useThrift) {
 			logger.debug("Shutting down the thrift communication!");
 			ThriftService.getInstance().stop();
 		}
 
 		logger.info("load end");
 	}
 
 	/**
 	 * Runs the benchmark. The only required argument is the configuration file path (e.g. config/rain.config.sample.json).
 	 */
 	public static void main(String[] args) throws Exception {
 		try {
 			StringBuffer configData = new StringBuffer();
 
 			if (args.length < 1) {
 				logger.info("Unspecified name/path to configuration file!");
 				System.exit(1);
 			}
 
 			// If we got 2 arguments expect a zookeeper pointer as the second
 			// parameter
 			if (args.length == 2) {
 				String zkString = args[1];
 				String zkPrefix = "zk://";
 				int zkIndex = zkString.indexOf(zkPrefix);
 				int pathIndex = -1;
 
 				if (zkIndex == -1)
 					pathIndex = zkString.indexOf(ZK_PATH_SEPARATOR);
 				else
 					pathIndex = zkString.indexOf(ZK_PATH_SEPARATOR, zkPrefix.length());
 
 				// Example ZK address
 				// zk://ec2-50-16-2-36.compute-1.amazonaws.com,ec2-174-129-105-138.compute-1.amazonaws.com/demo/apps/scadr/webServerList
 
 				if (zkIndex == -1) {
 					if (pathIndex == -1) {
 						RainConfig.getInstance()._zooKeeper = zkString;
 					} else {
 						RainConfig.getInstance()._zooKeeper = zkString.substring(0, pathIndex);
 						RainConfig.getInstance()._zkPath = zkString.substring(pathIndex);
 					}
 				} else {
 					if (pathIndex == -1) {
 						RainConfig.getInstance()._zooKeeper = zkString.substring(zkIndex + zkPrefix.length());
 					} else {
 						RainConfig.getInstance()._zooKeeper = zkString.substring(zkIndex + zkPrefix.length(), pathIndex);
 						RainConfig.getInstance()._zkPath = zkString.substring(pathIndex);
 					}
 				}
 			}
 
 			String filename = args[0];
 			JSONObject jsonConfig = null;
 
 			try {
 				String fileContents = "";
 				// Try to load the config file as a resource first
 				InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(filename);
 				if (in != null) {
 					logger.info("Reading config file from resource stream.");
 					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 					String line = "";
 					// Read in the entire file and append to the string buffer
 					while ((line = reader.readLine()) != null)
 						configData.append(line);
 					fileContents = configData.toString();
 				} else {
 					logger.info("Reading config file from file system.");
 					fileContents = ConfigUtil.readFileAsString(filename);
 				}
 
 				jsonConfig = new JSONObject(fileContents);
 			} catch (IOException e) {
				logger.info("ERROR loading configuration file " + filename + ". Reason: " + e.toString());
 				System.exit(1);
 			} catch (JSONException e) {
				logger.info("ERROR parsing configuration file " + filename + " as JSON. Reason: " + e.toString());
 				System.exit(1);
 			}
 
 			// Create a scenario from the json config
 			Scenario scenario = new Scenario(jsonConfig);
 			// Set the global Scenario instance for the Driver
 			Benchmark.BenchmarkScenario = scenario;
 			Benchmark benchmark = Benchmark.getBenchmarkInstance();
 			benchmark.waitingForStartSignal = RainConfig.getInstance()._waitForStartSignal;
 
 			// Now that the Benchmark and Scenario singletons are set up
 
 			// Don't start up a RainPipe by default, the user needs to ask (via
 			// a
 			// setting in the config file)
 			if (RainConfig.getInstance()._usePipe) {
 				RainPipe pipe = RainPipe.getInstance();
 				logger.info("Starting communication pipe! Using port: " + pipe.getPort() + " and running: "
 						+ pipe.getNumThreads() + " communication threads.");
 				pipe.start();
 			}
 
 			if (RainConfig.getInstance()._useThrift) {
 				ThriftService thrift = ThriftService.getInstance();
 				logger.info("Starting thrift communication! Using port: " + thrift.getPort());
 				thrift.start();
 			}
 
 			if (benchmark.waitingForStartSignal)
 				logger.info("Waiting for start signal...");
 
 			while (benchmark.waitingForStartSignal) {
 				logger.trace("Sleeping for 1sec...");
 				Thread.sleep(1000);
 				logger.trace("Checking for wakeup");
 			}
 
 			logger.info("Starting...");
 
 			scenario.start();
 			benchmark.start(scenario);
 
 			scenario.end();
 		} finally {
 			LogManager.shutdown();
 		}
 	}
 }
