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
 
 import java.io.PrintStream;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.TreeMap;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import radlab.rain.util.MetricWriter;
 import radlab.rain.util.PoissonSamplingStrategy;
 
 /**
  * The Scoreboard class implements the IScoreboard interface. Each Scoreboard is specific to a single instantiation of a track
  * (i.e. the statistical results of a a scoreboard pertain to the operations executed by only the. scenario track with which this
  * scoreboard is associated).<br />
  * <br />
  * The graphs we want to show/statistics we want to record:
  * <ol>
  * <li>Offered load timeline (in ops or requests per sec in a bucket of time)</li>
  * <li>Offered load during the run (in ops or requests per sec)</li>
  * <li>Effective load during the run (in ops or requests per sec) (avg number of operations/requests that completed successfully
  * during the run duration</li>
  * <li>Data distribution for each operation type - histogram of id's generated/used</li>
  * </ol>
  */
 public class Scoreboard implements Runnable, IScoreboard {
 	private static Logger log = LoggerFactory.getLogger(Scoreboard.class);
 
 	// Labels used for the results
 	public static String NO_TRACE_LABEL = "[NONE]";
 	public static String STEADY_STATE_TRACE_LABEL = "[STEADY-STATE]";
 	public static String LATE_LABEL = "[LATE]";
 	public static String RAMP_UP_LABEL = "[RAMP-UP]";
 	public static String RAMP_DOWN_LABEL = "[RAMP-DOWN]";
 
 	// Time in seconds to wait for worker thread to exit before interrupt
 	public static int WORKER_EXIT_TIMEOUT = 60;
 
 	// Who owns this scoreboard
 	private String _trackName;
 	private String _trackTargetHost;
 	private ScenarioTrack _owner = null;
 
 	// If true, this scoreboard will refuse any new results.
 	private boolean _closed = false;
 
 	// Random number generator
 	private Random _random = new Random();
 
 	// Response time sampling interval
 	private long _meanResponseTimeSamplingInterval = 500;
 
 	// Log (trace) sampling probability
 	private double _logSamplingProbability = 1.0;
 
 	// Time markers
 	private long _startTime = 0;
 	private long _endTime = 0;
 
 	// Counters for dropoffs and dropoff time (time waited for the lock to
 	// write a result to a processing queue)
 	private long _totalDropOffWaitTime = 0;
 	private long _maxDropOffWaitTime = 0;
 	private long _totalDropoffs = 0;
 
 	// Write metrics to the metric writer
 	private boolean _usingMetricSnapshots = false;
 	private MetricWriter _metricWriter = null;
 
 	// Final scorecard (aggregates all stat counters)
 	Scorecard finalCard = null;
 
 	// Scorecards: For each profile (= interval) there is one scorecard
 	// Each element of a workload profile is a profile in rain
 	private TreeMap<String, Scorecard> _profileScorecards = new TreeMap<String, Scorecard>();
 
 	// Summary reports for each operation
 	private TreeMap<String, ErrorSummary> _errorMap = new TreeMap<String, ErrorSummary>();
 	private TreeMap<String, WaitTimeSummary> _waitTimeMap = new TreeMap<String, WaitTimeSummary>();
 
 	// Dropoff and processing queues
 	private LinkedList<OperationExecution> _dropOffQ = new LinkedList<OperationExecution>();
 	private LinkedList<OperationExecution> _processingQ = new LinkedList<OperationExecution>();
 
 	// Lock objects
 	private Object _swapDropoffQueueLock = new Object();
 	private Object _waitTimeDropOffLock = new Object();
 	private Object _errorSummaryDropOffLock = new Object();
 
 	// Threads used to process the queues
 	private Thread _workerThread = null;
 	private SnapshotWriterThread _snapshotThread = null;
 
 	// Formatter is used for the output
 	private NumberFormat _formatter = new DecimalFormat("#0.0000");
 
 	/**
 	 * Creates a new Scoreboard with the track name specified. The Scoreboard returned must be initialized by calling
 	 * <code>initialize</code>.
 	 * 
 	 * @param trackName
 	 *            The track name to associate with this scoreboard.
 	 */
 	public Scoreboard(String trackName) {
 		this._trackName = trackName;
 	}
 
 	public void initialize(long startTime, long endTime) {
 		this._startTime = startTime;
 		this._endTime = endTime;
 
 		double runDuration = (double) (this._endTime - this._startTime) / 1000.0;
 		this.finalCard = new Scorecard("final", runDuration, this._trackName);
 
 		this.reset();
 	}
 
 	@Override
 	public void reset() {
 		// Clear the operation map
 		this.finalCard._operationMap.clear();
 		synchronized (this._swapDropoffQueueLock) {
 			this._dropOffQ.clear();
 		}
 		this._processingQ.clear();
 		synchronized (this._waitTimeDropOffLock) {
 			this._waitTimeMap.clear();
 		}
 		this.finalCard._totalActionsSuccessful = 0;
 		this._totalDropoffs = 0;
 		this._totalDropOffWaitTime = 0;
 		this.finalCard._totalOpsAsync = 0;
 		this.finalCard._totalOpsFailed = 0;
 		this.finalCard._totalOpsInitiated = 0;
 		this.finalCard._totalOpsSuccessful = 0;
 		this.finalCard._totalOpsSync = 0;
 		this._maxDropOffWaitTime = 0;
 		this.finalCard._totalOpsLate = 0;
 		this.finalCard._totalOpResponseTime = 0;
 	}
 
 	public void dropOffWaitTime(long time, String opName, long waitTime) {
 		if (isDone())
 			return;
 		if (!this.isSteadyState(time))
 			return;
 
 		synchronized (this._waitTimeDropOffLock) {
 			WaitTimeSummary waitTimeSummary = this._waitTimeMap.get(opName);
 
 			// Create wait time summary if it does not exist
 			if (waitTimeSummary == null) {
 				waitTimeSummary = new WaitTimeSummary(new PoissonSamplingStrategy(this._meanResponseTimeSamplingInterval));
 				this._waitTimeMap.put(opName, waitTimeSummary);
 			}
 
 			// Update wait time summary for this operation
 			waitTimeSummary.count++;
 			waitTimeSummary.totalWaitTime += waitTime;
 			if (waitTime < waitTimeSummary.minWaitTime)
 				waitTimeSummary.minWaitTime = waitTime;
 			if (waitTime > waitTimeSummary.maxWaitTime)
 				waitTimeSummary.maxWaitTime = waitTime;
 
 			// Drop sample
 			waitTimeSummary.acceptSample(waitTime);
 		}
 	}
 
 	public void dropOffOperation(OperationExecution result) {
 		if (isDone())
 			return;
 
 		// Set result label
 		if (this.isRampUp(result.getTimeStarted()))
 			result.setTraceLabel(Scoreboard.RAMP_UP_LABEL);
 		else if (this.isSteadyState(result.getTimeFinished()))
 			result.setTraceLabel(Scoreboard.STEADY_STATE_TRACE_LABEL);
 		else if (this.isSteadyState(result.getTimeStarted()))
 			result.setTraceLabel(Scoreboard.LATE_LABEL);
 		else if (this.isRampDown(result.getTimeStarted()))
 			result.setTraceLabel(Scoreboard.RAMP_DOWN_LABEL);
 
 		// Put all results into the dropoff queue
 		long lockStart = System.currentTimeMillis();
 		synchronized (this._swapDropoffQueueLock) {
 			long dropOffWaitTime = (System.currentTimeMillis() - lockStart);
 
 			// Update statistics
 			this._totalDropOffWaitTime += dropOffWaitTime;
 			this._totalDropoffs++;
 			if (dropOffWaitTime > this._maxDropOffWaitTime)
 				this._maxDropOffWaitTime = dropOffWaitTime;
 
 			// Put this result into the dropoff queue
 			this._dropOffQ.add(result);
 		}
 
 		// TODO: Log error reason
 
 		// Flip a coin to determine whether we log or not?
 		double randomVal = this._random.nextDouble();
 		if (this._logSamplingProbability == 1.0 || randomVal <= this._logSamplingProbability) {
 			// TODO: If needed file logging can be done here
 		} else // not logging
 		{
 			result.getOperation().disposeOfTrace();
 		}
 
 		// Important
 		// Return operation object to pool!
 		if (this._owner.getObjectPool().isActive())
 			this._owner.getObjectPool().returnObject(result.getOperation());
 	}
 
 	private final boolean isDone() {
 		return this._closed;
 	}
 
 	private final boolean isSteadyState(long time) {
 		return (time >= this._startTime && time <= this._endTime);
 	}
 
 	private final boolean isRampUp(long time) {
 		return (time < this._startTime);
 	}
 
 	private final boolean isRampDown(long time) {
 		return (time > this._endTime);
 	}
 
 	public void start() {
 		if (!this.isRunning()) {
 			this._closed = false;
 
 			// Start worker thread
 			this._workerThread = new Thread(this);
 			this._workerThread.setName("Scoreboard-Worker");
 			this._workerThread.start();
 
 			// Start snapshot thread
 			if (this._usingMetricSnapshots) {
 				this._snapshotThread = new SnapshotWriterThread(this._trackName);
 
 				if (this._metricWriter == null)
 					log.warn(this + " Metric snapshots disabled - No metric writer instance provided");
 				else
 					this._snapshotThread.setMetricWriter(this._metricWriter);
 
 				this._snapshotThread.setName("Scoreboard-Snapshot-Writer");
 				this._snapshotThread.start();
 			}
 		}
 	}
 
 	public void stop() {
 		if (this.isRunning()) {
 			// Set the finished flag
 			this._closed = true;
 
 			try {
 				// Stop worker thread
 				// Wait to join
 				log.debug(this + " waiting " + WORKER_EXIT_TIMEOUT + " seconds for worker thread to exit!");
 				this._workerThread.join(WORKER_EXIT_TIMEOUT * 1000);
 
 				// If its still alive try to interrupt
 				if (this._workerThread.isAlive()) {
 					log.debug(this + " interrupting worker thread.");
 					this._workerThread.interrupt();
 				}
 
 				// Stop snapshot thread
 				if (this._snapshotThread != null) {
 					// Set stop flag
 					this._snapshotThread.set_done(true);
 
 					// Wait to join
 					log.debug(this + " waiting " + WORKER_EXIT_TIMEOUT + " seconds for snapshot thread to exit!");
 					this._snapshotThread.join(WORKER_EXIT_TIMEOUT * 1000);
 
 					// If its still alive try to interrupt again
 					if (this._snapshotThread.isAlive()) {
 						log.debug(this + " interrupting snapshot thread.");
 						this._snapshotThread.interrupt();
 					}
 				}
 
 			} catch (InterruptedException ie) {
 				log.info(this + " Interrupted waiting on worker thread exit!");
 			}
 		}
 	}
 
 	/**
 	 * Checks whether the worker thread exists and is alive.
 	 * 
 	 * @return True if the worker thread exists and is alive.
 	 */
 	protected boolean isRunning() {
 		return (this._workerThread != null && this._workerThread.isAlive());
 	}
 
 	/**
 	 * Implements the worker thread that periodically grabs the results from the dropOffQ and copies it over to the processingQ to
 	 * be processed.
 	 */
 	public void run() {
 		log.debug(this + " starting worker thread");
 
 		while (!isDone() || this._dropOffQ.size() > 0) {
 			if (this._dropOffQ.size() > 0) {
 
 				// Queue swap (dropOffQ with processingQ)
 				synchronized (this._swapDropoffQueueLock) {
 					LinkedList<OperationExecution> temp = _processingQ;
 					_processingQ = _dropOffQ;
 					_dropOffQ = temp;
 				}
 
 				// Process all entries in the working queue
 				while (!this._processingQ.isEmpty()) {
 					OperationExecution result = this._processingQ.remove();
 					String traceLabel = result.getTraceLabel();
 
 					// Process this operation by its label
 					if (traceLabel.equals(Scoreboard.STEADY_STATE_TRACE_LABEL)) {
 						this.finalCard._totalOpsInitiated++;
 
 						// Process a steady state result
 						this.processSteadyStateResult(result);
 					} else if (traceLabel.equals(Scoreboard.LATE_LABEL)) {
 						this.finalCard._totalOpsInitiated++;
 						this.finalCard._totalOpsLate++;
 					}
 				}
 			} else {
 				// Wait some time, until the dropOffQ fills up
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException tie) {
 					log.info(this + " worker thread interrupted.");
 				}
 			}
 		}
 
 		// Debugging
 		log.debug(this + " drop off queue size: " + this._dropOffQ.size());
 		log.debug(this + " processing queue size: " + this._processingQ.size());
 		log.debug(this + " worker thread finished!");
 	}
 
 	/**
 	 * Processes a result (from the processingQ) if it was received during the steady state period.
 	 * 
 	 * @param result
 	 *            The operation execution result to process.
 	 */
 	private void processSteadyStateResult(OperationExecution result) {
 		String operationName = result._operationName;
 		LoadProfile activeProfile = result._generatedDuring;
 
 		// By default we don't save per-interval metrics
 		if (activeProfile != null) {
 			if ((activeProfile._name != null && activeProfile._name.length() > 0)) {
 				// Get scorecard for this interval
 				String profileName = activeProfile._name;
 				Scorecard profileScorecard = this._profileScorecards.get(profileName);
 				// Create a new scorecard if needed
 				if (profileScorecard == null) {
 					profileScorecard = new Scorecard(profileName, activeProfile._interval, this._trackName);
 					profileScorecard._numberOfUsers = activeProfile._numberOfUsers;
 					this._profileScorecards.put(profileName, profileScorecard);
 				}
 
 				// Operation summary
 				OperationSummary operationSummary = profileScorecard._operationMap.get(operationName);
 				// Create new operation summary if needed
 				if (operationSummary == null) {
 					operationSummary = new OperationSummary(new PoissonSamplingStrategy(this._meanResponseTimeSamplingInterval));
 					profileScorecard._operationMap.put(operationName, operationSummary);
 				}
 
 				// Update global counters counters
 				profileScorecard._activeCount = activeProfile._activeCount;
 				profileScorecard._totalOpsInitiated += 1;
 
 				// Failed result
 				if (result.isFailed()) {
 					profileScorecard._totalOpsFailed++;
 					operationSummary.failed++;
 				} else // Successful result
 				{
 					// Intervals passed in seconds, convert to msecs
 					long profileLengthMsecs = result._generatedDuring._interval * 1000;
 					long profileEndMsecs = result._profileStartTime + profileLengthMsecs;
 
 					// Result returned after profile interval ended
 					if (result.getTimeFinished() > profileEndMsecs) {
 						profileScorecard._totalOpsLate++;
 					} else { // Did the result occur before the profile interval ended
 					}
 					// Count operations
 					if (result.isAsynchronous()) {
 						profileScorecard._totalOpsAsync++;
 						operationSummary.totalAsyncInvocations++;
 					} else {
 						profileScorecard._totalOpsSync++;
 						operationSummary.totalSyncInvocations++;
 					}
 
 					profileScorecard._totalOpsSuccessful++;
 					operationSummary.succeeded++;
 					profileScorecard._totalActionsSuccessful += result.getActionsPerformed();
 					operationSummary.totalActions += result.getActionsPerformed();
 
 					// If interactive, look at the total response time.
 					if (result.isInteractive()) {
 						long responseTime = result.getExecutionTime();
 						operationSummary.acceptSample(responseTime);
 
 						// Response time
 						operationSummary.totalResponseTime += responseTime;
 						profileScorecard._totalOpResponseTime += responseTime;
 
 						// Update max and min response time
 						operationSummary.maxResponseTime = Math.max(operationSummary.maxResponseTime, responseTime);
 						operationSummary.minResponseTime = Math.min(operationSummary.minResponseTime, responseTime);
 					}
 				}
 			}
 		}
 
 		// Do the accounting for the final score card
 		OperationSummary operationSummary = this.finalCard._operationMap.get(operationName);
 
 		// Create operation summary if needed
 		if (operationSummary == null) {
 			operationSummary = new OperationSummary(new PoissonSamplingStrategy(this._meanResponseTimeSamplingInterval));
 			this.finalCard._operationMap.put(operationName, operationSummary);
 		}
 
 		// Update final card
 		if (result.isAsynchronous())
 			this.finalCard._totalOpsAsync++;
 		else
 			this.finalCard._totalOpsSync++;
 
 		// Result failed
 		if (result.isFailed()) {
 			this.finalCard._totalOpsFailed++;
 			operationSummary.failed++;
 		} else { // Result successful
 			this.finalCard._totalOpsSuccessful++;
 			operationSummary.succeeded++;
 
 			this.finalCard._totalActionsSuccessful += result.getActionsPerformed();
 			operationSummary.totalActions += result.getActionsPerformed();
 
 			if (result.isAsynchronous())
 				operationSummary.totalAsyncInvocations++;
 			else
 				operationSummary.totalSyncInvocations++;
 
 			// If interactive, look at the total response time.
 			if (result.isInteractive()) {
 				long responseTime = result.getExecutionTime();
 				operationSummary.acceptSample(responseTime);
 
 				// Response time
 				operationSummary.totalResponseTime += responseTime;
 				this.finalCard._totalOpResponseTime += responseTime;
 
 				// Update max and min response time
 				operationSummary.maxResponseTime = Math.max(operationSummary.maxResponseTime, responseTime);
 				operationSummary.minResponseTime = Math.min(operationSummary.minResponseTime, responseTime);
 
 				// Do metric SNAPSHOTS (record all response times)
 				// Only save response times if we're doing metric snapshots
 				// This reduces memory leakage
 				if (this._usingMetricSnapshots) {
 					ResponseTimeStat responseTimeStat = this._snapshotThread.provisionRTSObject();
 					if (responseTimeStat == null)
 						responseTimeStat = new ResponseTimeStat();
 
 					// Fill response time stat
 					responseTimeStat._timestamp = result.getTimeFinished();
 					responseTimeStat._responseTime = responseTime;
 					responseTimeStat._totalResponseTime = this.finalCard._totalOpResponseTime;
 					responseTimeStat._numObservations = this.finalCard._totalOpsSuccessful;
 					responseTimeStat._operationName = result._operationName;
 					responseTimeStat._trackName = this._trackName;
 					responseTimeStat._operationRequest = result._operationRequest;
 
 					if (result._generatedDuring != null)
 						responseTimeStat._generatedDuring = result._generatedDuring._name;
 
 					// Push this stat onto a Queue for the snapshot thread
 					this._snapshotThread.accept(responseTimeStat);
 				}
 			}
 		}
 	}
 
 	public JSONObject getJSONStatistics() throws JSONException {
 		// Run duration in seconds
 		double runDuration = (double) (this._endTime - this._startTime) / 1000.0;
 
 		// Total operations executed
 		long totalOperations = this.finalCard._totalOpsSuccessful + this.finalCard._totalOpsFailed;
 
 		// Operations initiated per second
 		double offeredLoadOps = (double) this.finalCard._totalOpsInitiated / runDuration;
 
 		// Operations successful per second
 		double effectiveLoadOps = (double) this.finalCard._totalOpsSuccessful / runDuration;
 
 		// Actions successful per second
 		double effectiveLoadRequests = (double) this.finalCard._totalActionsSuccessful / runDuration;
 
 		// Average response time of an operation in seconds
 		double averageOpResponseTimeSecs = 0.0;
 		if (this.finalCard._totalOpsSuccessful > 0)
 			averageOpResponseTimeSecs = ((double) this.finalCard._totalOpResponseTime / (double) this.finalCard._totalOpsSuccessful) / 1000.0;
 
 		JSONObject result = new JSONObject();
 		result.put("track", _trackName);
 		result.put("target_host", _trackTargetHost);
 		result.put("total_operations", totalOperations);
 		result.put("total_drop_offs", _totalDropoffs);
 		result.put("average_drop_off_q_time(ms)", (double) _totalDropOffWaitTime / (double) _totalDropoffs);
 		result.put("max_drop_off_q_time(ms)", _maxDropOffWaitTime);
 		result.put("offered_load(ops/sec)", offeredLoadOps);
 		result.put("effective_load(ops/sec)", effectiveLoadOps);
 		result.put("effective_load(req/sec)", effectiveLoadRequests);
 		result.put("operations_initiated", finalCard._totalOpsInitiated);
 		result.put("operations_successfully_complaeted", finalCard._totalOpsSuccessful);
 		result.put("average_operation_response_time(s)", averageOpResponseTimeSecs);
 		result.put("operations_late", finalCard._totalOpsLate);
 		result.put("operations_failed", finalCard._totalOpsFailed);
 		result.put("async_ops", finalCard._totalOpsAsync);
 		result.put("sycn_ops", finalCard._totalOpsSync);
 		result.put("mean_response_time_sample_interval", _meanResponseTimeSamplingInterval);
 
 		// Print other statistics
 		result.put("operation_stats", getJSONOperationStatistics(false));
 		result.put("wait_stats", getJSONWaitTimeStatistics(false));
 
 		return result;
 	}
 
 	public void printStatistics(PrintStream out) {
 		double runDuration = (double) (this._endTime - this._startTime) / 1000.0;
 		long totalOperations = this.finalCard._totalOpsSuccessful + this.finalCard._totalOpsFailed;
 		double offeredLoadOps = (double) this.finalCard._totalOpsInitiated / runDuration;
 		double effectiveLoadOps = (double) this.finalCard._totalOpsSuccessful / runDuration;
 		double effectiveLoadRequests = (double) this.finalCard._totalActionsSuccessful / runDuration;
 
 		double totalUsers = 0.0;
 		double totalIntervalActivations = 0.0;
 
 		out.println(this + " Interval results-------------------: ");
 
 		// Print all scorecard stats
 		for (Scorecard card : this._profileScorecards.values()) {
 			for (LoadProfile profile : this._owner._loadSchedule) {
 				// Skip profile if name does not match
 				if (!card._name.equals(profile._name))
 					continue;
 
 				// If the profile started after the end of a run then
 				// decrease the activation count accordingly
 				if (profile.getTimeStarted() > this._endTime) {
 					// Decrease activation count
 					double intervalSpillOver = (this._endTime - profile.getTimeStarted())
 							/ ((profile._interval * 1000) + (profile._transitionTime * 1000));
 					log.info(this + " Need to decrease activation count for: " + profile._name + " spillover: " + intervalSpillOver);
 					card._activeCount -= intervalSpillOver;
 					continue;
 				}
 
 				// Look at the diff between the last activation
 				// and the end of steady state
 				long intervalEndTime = profile.getTimeStarted() + (profile._interval * 1000) + (profile._transitionTime * 1000);
 
 				// Did the end of the run interrupt this interval
 				double diff = intervalEndTime - this._endTime;
 				if (diff > 0) {
 					double delta = (diff / (double) (profile._interval * 1000));
 					log.info(this + " " + card._name + " shortchanged (msecs): " + this._formatter.format(diff));
 					log.info(this + " " + card._name + " shortchanged (delta): " + this._formatter.format(delta));
 					// Interval truncated so revise activation count downwards
 					card._activeCount -= delta;
 				}
 			}
 
 			totalUsers += card._numberOfUsers * card._activeCount;
 			totalIntervalActivations += card._activeCount;
 			card.printStatistics(out);
 		}
 
 		double averageOpResponseTimeSecs = 0.0;
 
 		if (this.finalCard._totalOpsSuccessful > 0)
 			averageOpResponseTimeSecs = ((double) this.finalCard._totalOpResponseTime / (double) this.finalCard._totalOpsSuccessful) / 1000.0;
 
 		ScenarioTrack track = this.getScenarioTrack();
 		// Rough averaging of the additional time spent in the system due to think times/cycle times.
 		// Look at the proportion of time we would have waited based on think times and the proportion of times we would have
 		// waited based on cycle times
 		double thinkTimeDeltaSecs = ((1 - track._openLoopProbability) * track.getMeanThinkTime())
 				+ (track._openLoopProbability * track.getMeanCycleTime());
 
 		double averageNumberOfUsers = 0.0;
 		if (totalIntervalActivations != 0)
 			averageNumberOfUsers = totalUsers / totalIntervalActivations;
 		finalCard._numberOfUsers = averageNumberOfUsers;
 		out.println(this + " Final results----------------------: ");
 		out.println(this + " Target host                        : " + this._trackTargetHost);
 		out.println(this + " Total drop offs                    : " + this._totalDropoffs);
 		out.println(this + " Average drop off Q time (ms)       : "
 				+ this._formatter.format((double) this._totalDropOffWaitTime / (double) this._totalDropoffs));
 		out.println(this + " Max drop off Q time (ms)           : " + this._maxDropOffWaitTime);
 		out.println(this + " Total interval activations         : " + this._formatter.format(totalIntervalActivations));
 		out.println(this + " Average number of users            : " + this._formatter.format(averageNumberOfUsers));
 		out.println(this + " Offered load (ops/sec)             : " + this._formatter.format(offeredLoadOps));
 		out.println(this + " Effective load (ops/sec)           : " + this._formatter.format(effectiveLoadOps));
 
 		// Still a rough estimate, need to compute the bounds on this estimate
 		if (averageOpResponseTimeSecs > 0.0) {
 			// double opsPerUser = averageNumberOfUsers / this.finalCard._totalOpsSuccessful;
 
 			double littlesEstimate = averageNumberOfUsers / (averageOpResponseTimeSecs + thinkTimeDeltaSecs);
 			double littlesDelta = Math.abs((effectiveLoadOps - littlesEstimate) / littlesEstimate) * 100;
 			out.println(this + " Little's Law Estimate (ops/sec)    : " + this._formatter.format(littlesEstimate));
 			out.println(this + " Variation from Little's Law (%)    : " + this._formatter.format(littlesDelta));
 		} else
 			out.println(this + " Little's Law Estimate (ops/sec)    : 0");
 
 		out.println(this + " Effective load (requests/sec)      : " + this._formatter.format(effectiveLoadRequests));
 		out.println(this + " Operations initiated               : " + this.finalCard._totalOpsInitiated);
 		out.println(this + " Operations successfully completed  : " + this.finalCard._totalOpsSuccessful);
 		// Avg response time per operation
 		out.println(this + " Average operation response time (s): " + this._formatter.format(averageOpResponseTimeSecs));
 		out.println(this + " Operations late                    : " + this.finalCard._totalOpsLate);
 		out.println(this + " Operations failed                  : " + this.finalCard._totalOpsFailed);
 		out.println(this + " Async Ops                          : " + this.finalCard._totalOpsAsync + " "
 				+ this._formatter.format((((double) this.finalCard._totalOpsAsync / (double) totalOperations) * 100)) + "%");
 		out.println(this + " Sync Ops                           : " + this.finalCard._totalOpsSync + " "
 				+ this._formatter.format((((double) this.finalCard._totalOpsSync / (double) totalOperations) * 100)) + "%");
 
 		out.println(this + " Mean response time sample interval : " + this._meanResponseTimeSamplingInterval + " (using Poisson sampling)");
 
 		// Print other statistics
 		this.printOperationStatistics(out, false);
 		out.println("");
 		this.printErrorSummaryStatistics(out, false);
 		out.println("");
 		this.printWaitTimeStatistics(out, false);
 	}
 
 	private void printErrorSummaryStatistics(PrintStream out, boolean purgeStats) {
 		synchronized (this._errorSummaryDropOffLock) {
 			long totalFailures = 0;
 			out.println(this + " Error Summary Results              : " + this._errorMap.size() + " types of error(s)");
 			Iterator<String> errorNameIt = this._errorMap.keySet().iterator();
 			while (errorNameIt.hasNext()) {
 				ErrorSummary summary = this._errorMap.get(errorNameIt.next());
 				out.println(this + " " + summary.toString());
 				totalFailures += summary._errorCount;
 			}
 			out.println(this + " Total failures                     : " + totalFailures);
 		}
 	}
 
 	private JSONObject getJSONWaitTimeStatistics(boolean purgePercentileData) {
 		JSONObject result = new JSONObject();
 
 		synchronized (this.finalCard._operationMap) {
 			try {
 				// Show operation proportions, response time: avg, max, min, stdev (op1 = x%, op2 = y%...)
 				// Enumeration<String> keys = this.finalCard._operationMap.keys();
 				Iterator<String> keys = this.finalCard._operationMap.keySet().iterator();
 				while (keys.hasNext()) {
 					String opName = keys.next();
 					WaitTimeSummary summary = this._waitTimeMap.get(opName);
 
 					// If there were no values, then the min and max wait times would not have been set
 					// so make them to 0
 					if (summary.minWaitTime == Long.MAX_VALUE)
 						summary.minWaitTime = 0;
 
 					if (summary.maxWaitTime == Long.MIN_VALUE)
 						summary.maxWaitTime = 0;
 
 					// Print out the operation summary.
 					result.put("operation", opName);
 					result.put("avg_wait", summary.getAverageWaitTime() / 1000.0);
 					result.put("min_wait", summary.minWaitTime / 1000.0);
 					result.put("max_wait", summary.maxWaitTime / 1000.0);
 					result.put("90th(s)", summary.getNthPercentileResponseTime(90) / 1000.0);
 					result.put("99th(s)", summary.getNthPercentileResponseTime(99) / 1000.0);
 					result.put("samples_collected", summary.getSamplesCollected());
 					result.put("samples_seen", summary.getSamplesSeen());
 					result.put("sample_mean", summary.getSampleMean() / 1000.0);
 					result.put("std_dev", summary.getSampleStandardDeviation() / 1000.0);
 					result.put("t_val", summary.getTvalue(summary.getAverageWaitTime()));
 
 					if (purgePercentileData)
 						summary.resetSamples();
 				}
 			} catch (Exception e) {
 				log.info(this + " Error printing think/cycle time summary. Reason: " + e.toString());
 				e.printStackTrace();
 			}
 		}
 
 		return result;
 	}
 
 	private void printWaitTimeStatistics(PrintStream out, boolean purgePercentileData) {
 		synchronized (this.finalCard._operationMap) {
 			try {
 				// Make this thing "prettier", using fixed width columns
 				String outputFormatSpec = "|%20s|%12s|%12s|%12s|%10s|%10s|%50s|";
 
 				out.println(this + String.format(outputFormatSpec, "operation", "avg wait", "min wait", "max wait", "90th (s)", "99th (s)", "pctile"));
 				out.println(this + String.format(outputFormatSpec, "", "time (s)", "time (s)", "time (s)", "", "", "samples"));
 
 				// Show operation proportions, response time: avg, max, min, stdev (op1 = x%, op2 = y%...)
 				// Enumeration<String> keys = this.finalCard._operationMap.keys();
 				Iterator<String> keys = this.finalCard._operationMap.keySet().iterator();
 				while (keys.hasNext()) {
 					String opName = keys.next();
 					WaitTimeSummary summary = this._waitTimeMap.get(opName);
 
 					// If there were no values, then the min and max wait times would not have been set
 					// so make them to 0
 					if (summary.minWaitTime == Long.MAX_VALUE)
 						summary.minWaitTime = 0;
 
 					if (summary.maxWaitTime == Long.MIN_VALUE)
 						summary.maxWaitTime = 0;
 
 					// Print out the operation summary.
 					out.println(this
 							+ String.format(
 									outputFormatSpec,
 									opName,
 									// this._formatter.format( ( ( (double) ( summary.succeeded + summary.failed ) / (double)
 									// totalOperations ) * 100 ) ) + "% ",
 									// summary.succeeded,
 									// summary.failed,
 									this._formatter.format(summary.getAverageWaitTime() / 1000.0),
 									this._formatter.format(summary.minWaitTime / 1000.0), this._formatter.format(summary.maxWaitTime / 1000.0),
 									this._formatter.format(summary.getNthPercentileResponseTime(90) / 1000.0),
 									this._formatter.format(summary.getNthPercentileResponseTime(99) / 1000.0), summary.getSamplesCollected() + "/"
 											+ summary.getSamplesSeen() + " (mu: " + this._formatter.format(summary.getSampleMean() / 1000.0)
 											+ ", sd: " + this._formatter.format(summary.getSampleStandardDeviation() / 1000.0) + " t: "
 											+ this._formatter.format(summary.getTvalue(summary.getAverageWaitTime())) + ")"));
 
 					if (purgePercentileData)
 						summary.resetSamples();
 				}
 			} catch (Exception e) {
 				log.info(this + " Error printing think/cycle time summary. Reason: " + e.toString());
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private JSONObject getJSONOperationStatistics(boolean purgePercentileData) throws JSONException {
 		JSONObject result = new JSONObject();
 
 		long totalOperations = finalCard._totalOpsSuccessful + this.finalCard._totalOpsFailed;
 		double totalAvgResponseTime = 0.0;
 		double totalResponseTime = 0.0;
 		long totalSuccesses = 0;
 
 		synchronized (this.finalCard._operationMap) {
 			JSONArray operations = new JSONArray();
 			result.put("operations", operations);
 
			for (Iterator<String> keys = finalCard._operationMap.keySet().iterator();; keys.hasNext()) {
 				String opName = keys.next();
 				OperationSummary operationSummary = finalCard._operationMap.get(opName);
 
 				// Update global counters
 				totalAvgResponseTime += operationSummary.getAverageResponseTime();
 				totalResponseTime += operationSummary.totalResponseTime;
 				totalSuccesses += operationSummary.succeeded;
 
 				// If there were no successes, then the min and max response times would not have been set
 				// so make them to 0
 				if (operationSummary.minResponseTime == Long.MAX_VALUE)
 					operationSummary.minResponseTime = 0;
 
 				if (operationSummary.maxResponseTime == Long.MIN_VALUE)
 					operationSummary.maxResponseTime = 0;
 
 				// Print out the operation summary.
 				JSONObject operation = new JSONObject();
 				operations.put(operation);
 				operation.put("operation", opName);
 				operation.put("proportion", ((double) (operationSummary.succeeded + operationSummary.failed) / (double) totalOperations) * 100d);
 				operation.put("successes", operationSummary.succeeded);
 				operation.put("failures", operationSummary.failed);
 				operation.put("avg_response", operationSummary.getAverageResponseTime() / 1000.0);
 				operation.put("min_response", operationSummary.minResponseTime / 1000.0);
 				operation.put("max_response", operationSummary.maxResponseTime / 1000.0);
 				operation.put("90th(s)", operationSummary.getNthPercentileResponseTime(90) / 1000.0);
 				operation.put("99th(s)", operationSummary.getNthPercentileResponseTime(99) / 1000.0);
 				operation.put("samples_collected", operationSummary.getSamplesCollected());
 				operation.put("samples_seen", operationSummary.getSamplesSeen());
 				operation.put("sample_mean", operationSummary.getSampleMean() / 1000.0);
 				operation.put("sample_stdev", operationSummary.getSampleStandardDeviation() / 1000.0);
 				operation.put("avg_resp_time", operationSummary.getTvalue(operationSummary.getAverageResponseTime()));
 
 				if (purgePercentileData)
 					operationSummary.resetSamples();
 			}
 		}
 
 		result.put("total_operations", totalOperations);
 		result.put("total_avg_response_time", totalAvgResponseTime);
 		result.put("total_response_time", totalResponseTime);
 		result.put("total_successes", totalSuccesses);
 
 		return result;
 	}
 
 	@SuppressWarnings("unused")
 	private void printOperationStatistics(PrintStream out, boolean purgePercentileData) {
 		long totalOperations = this.finalCard._totalOpsSuccessful + this.finalCard._totalOpsFailed;
 		double totalAvgResponseTime = 0.0;
 		double totalResponseTime = 0.0;
 		long totalSuccesses = 0;
 
 		synchronized (this.finalCard._operationMap) {
 			try {
 				// Make this thing "prettier", using fixed width columns
 				String outputFormatSpec = "|%20s|%10s|%10s|%10s|%12s|%12s|%12s|%10s|%10s|%50s|";
 
 				out.println(this
 						+ String.format(outputFormatSpec, "operation", "proportion", "successes", "failures", "avg response", "min response",
 								"max response", "90th (s)", "99th (s)", "pctile"));
 				out.println(this + String.format(outputFormatSpec, "", "", "", "", "time (s)", "time (s)", "time(s)", "", "", "samples"));
 
 				// Show operation proportions, response time: avg, max, min, stdev (op1 = x%, op2 = y%...)
 				// Enumeration<String> keys = this.finalCard._operationMap.keys();
 				Iterator<String> keys = this.finalCard._operationMap.keySet().iterator();
 				while (keys.hasNext()) {
 					String opName = keys.next();
 					OperationSummary summary = this.finalCard._operationMap.get(opName);
 
 					totalAvgResponseTime += summary.getAverageResponseTime();
 					totalResponseTime += summary.totalResponseTime;
 					totalSuccesses += summary.succeeded;
 					// If there were no successes, then the min and max response times would not have been set
 					// so make them to 0
 					if (summary.minResponseTime == Long.MAX_VALUE)
 						summary.minResponseTime = 0;
 
 					if (summary.maxResponseTime == Long.MIN_VALUE)
 						summary.maxResponseTime = 0;
 
 					// Print out the operation summary.
 					out.println(this
 							+ String.format(
 									outputFormatSpec,
 									opName,
 									this._formatter.format((((double) (summary.succeeded + summary.failed) / (double) totalOperations) * 100)) + "% ",
 									summary.succeeded, summary.failed, this._formatter.format(summary.getAverageResponseTime() / 1000.0),
 									this._formatter.format(summary.minResponseTime / 1000.0),
 									this._formatter.format(summary.maxResponseTime / 1000.0),
 									this._formatter.format(summary.getNthPercentileResponseTime(90) / 1000.0),
 									this._formatter.format(summary.getNthPercentileResponseTime(99) / 1000.0), summary.getSamplesCollected() + "/"
 											+ summary.getSamplesSeen() + " (mu: " + this._formatter.format(summary.getSampleMean() / 1000.0)
 											+ ", sd: " + this._formatter.format(summary.getSampleStandardDeviation() / 1000.0) + " t: "
 											+ this._formatter.format(summary.getTvalue(summary.getAverageResponseTime())) + ")"));
 
 					if (purgePercentileData)
 						summary.resetSamples();
 				}
 
 			} catch (Exception e) {
 				log.info(this + " Error printing operation summary. Reason: " + e.toString());
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public long getMeanResponseTimeSamplingInterval() {
 		return this._meanResponseTimeSamplingInterval;
 	}
 
 	public void setMeanResponseTimeSamplingInterval(long val) {
 		if (val > 0)
 			this._meanResponseTimeSamplingInterval = val;
 	}
 
 	public long getStartTimestamp() {
 		return this._startTime;
 	}
 
 	public void setStartTimestamp(long val) {
 		this._startTime = val;
 	}
 
 	public long getEndTimestamp() {
 		return this._endTime;
 	}
 
 	public void setEndTimestamp(long val) {
 		this._endTime = val;
 	}
 
 	public String getTrackName() {
 		return this._trackName;
 	}
 
 	public void setTrackName(String val) {
 		this._trackName = val;
 	}
 
 	public boolean getDone() {
 		return this._closed;
 	}
 
 	public void setDone(boolean val) {
 		this._closed = val;
 	}
 
 	public void setLogSamplingProbability(double val) {
 		this._logSamplingProbability = val;
 	}
 
 	public void setMetricSnapshotInterval(long val) {
 		// not supported
 	}
 
 	public boolean getUsingMetricSnapshots() {
 		return this._usingMetricSnapshots;
 	}
 
 	public void setUsingMetricSnapshots(boolean val) {
 		this._usingMetricSnapshots = val;
 	}
 
 	public MetricWriter getMetricWriter() {
 		return this._metricWriter;
 	}
 
 	public void setMetricWriter(MetricWriter val) {
 		this._metricWriter = val;
 	}
 
 	public String getTargetHost() {
 		return this._trackTargetHost;
 	}
 
 	public void setTargetHost(String val) {
 		this._trackTargetHost = val;
 	}
 
 	public Scorecard getFinalScorecard() {
 		return this.finalCard;
 	}
 
 	public ScenarioTrack getScenarioTrack() {
 		return this._owner;
 	}
 
 	public void setScenarioTrack(ScenarioTrack owner) {
 		this._owner = owner;
 	}
 
 	public String toString() {
 		return "[SCOREBOARD TRACK: " + this._trackName + "]";
 	}
 }
