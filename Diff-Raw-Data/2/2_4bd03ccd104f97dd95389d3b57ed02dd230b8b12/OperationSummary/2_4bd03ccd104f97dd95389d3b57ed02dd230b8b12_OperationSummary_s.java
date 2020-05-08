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
 
 package radlab.rain.scoreboard;
 
 import java.util.LinkedList;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import radlab.rain.RainConfig;
 import radlab.rain.operation.OperationExecution;
 import de.tum.in.dss.psquare.PSquared;
 
 public class OperationSummary {
 	private static Logger logger = LoggerFactory.getLogger(OperationSummary.class);
 
 	// Information recorded about one operation type
 	private long opsSuccessful = 0;
 	private long opsFailed = 0;
 	private long actionsSuccessful = 0;
 
 	private long opsAsync = 0;
 	private long opsSync = 0;
 
 	private long minResponseTime = Long.MAX_VALUE;
 	private long maxResponseTime = Long.MIN_VALUE;
 
 	private long totalResponseTime = 0;
 	private long opsFailedRtimeThreshold = 0;
 
 	// Sample the response times so that we can give a "reasonable"
 	// estimate of the 90th and 99th percentiles.
 	private IMetricSampler responseTimeSampler;
 
 	// Percentile estimation based on the P-square algorithm
 	private PSquared rtime99th = new PSquared(0.99f);
 	private PSquared rtime95th = new PSquared(0.95f);
 	private PSquared rtime90th = new PSquared(0.90f);
 	private PSquared rtime50th = new PSquared(0.50f);
 
 	public OperationSummary(IMetricSampler strategy) {
 		responseTimeSampler = strategy;
 	}
 
 	void resetSamples() {
 		responseTimeSampler.reset();
 	}
 
 	void processResult(OperationExecution result) {
 		if (result.failed) {
 			opsFailed++;
 		} else { // Result successful
 			opsSuccessful++;
 
 			actionsSuccessful += result.actionsPerformed;
 
 			// Count operations
 			if (result.async) {
 				opsAsync++;
 			} else {
 				opsSync++;
 			}
 
 			// Update response time sample
 			long responseTime = result.getExecutionTime();
 			responseTimeSampler.accept(responseTime);
 			totalResponseTime += responseTime;
 			if (responseTime > RainConfig.rtime_T)
 				opsFailedRtimeThreshold++;
 
 			// Update response time percentile estimations
 			rtime99th.accept(responseTime);
 			rtime95th.accept(responseTime);
 			rtime90th.accept(responseTime);
 			rtime50th.accept(responseTime);
 
 			// Update max and min response time
 			maxResponseTime = Math.max(maxResponseTime, responseTime);
 			minResponseTime = Math.min(minResponseTime, responseTime);
 		}
 	}
 
 	JSONObject getStatistics(double runDuration) throws JSONException {
 		// Total operations executed
 		long totalOperations = opsSuccessful + opsFailed;
 
 		double effectiveLoadOperations = 0;
 		double effectiveLoadRequests = 0;
 		double averageRTime = 0;
 
 		// Calculations (per second)
 		if (runDuration > 0) {
 			effectiveLoadOperations = (double) opsSuccessful / toSeconds(runDuration);
 			effectiveLoadRequests = (double) actionsSuccessful / toSeconds(runDuration);
 		} else {
 			logger.warn("run duration <= 0");
 		}
 
 		if (opsSuccessful > 0) {
 			averageRTime = (double) totalResponseTime / (double) opsSuccessful;
 		} else {
 			logger.warn("total ops successfull <= 0");
 		}
 
 		// Results
 		JSONObject operation = new JSONObject();
 
 		operation.put("ops_successful", opsSuccessful);
 		operation.put("ops_failed", opsFailed);
 		operation.put("ops_seen", totalOperations);
 		operation.put("actions_successful", actionsSuccessful);
 		operation.put("ops_async", opsAsync);
 		operation.put("ops_sync", opsSync);
 
 		operation.put("effective_load_ops", effectiveLoadOperations);
 		operation.put("effective_load_req", effectiveLoadRequests);
 
 		operation.put("rtime_total", totalResponseTime);
 		operation.put("rtime_average", nNaN(averageRTime));
 		operation.put("rtime_max", maxResponseTime);
 		operation.put("rtime_min", minResponseTime);
 		operation.put("rtime_50th", nNaN(rtime50th.getPValue()));
 		operation.put("rtime_90th", nNaN(rtime90th.getPValue()));
 		operation.put("rtime_95th", nNaN(rtime95th.getPValue()));
 		operation.put("rtime_99th", nNaN(rtime99th.getPValue()));
 		operation.put("rtime_thr_failed", opsFailedRtimeThreshold);
 
 		operation.put("sampler_samples_collected", responseTimeSampler.getSamplesCollected());
 		operation.put("sampler_samples_seen", responseTimeSampler.getSamplesSeen());
 		operation.put("sampler_rtime_50th", nNaN(responseTimeSampler.getNthPercentile(50)));
 		operation.put("sampler_rtime_90th", nNaN(responseTimeSampler.getNthPercentile(90)));
 		operation.put("sampler_rtime_95th", nNaN(responseTimeSampler.getNthPercentile(95)));
 		operation.put("sampler_rtime_99th", nNaN(responseTimeSampler.getNthPercentile(99)));
 		operation.put("sampler_rtime_mean", nNaN(responseTimeSampler.getSampleMean()));
 		operation.put("sampler_rtime_stdev", nNaN(responseTimeSampler.getSampleStandardDeviation()));
		operation.put("sampelr_rtime_tvalue", nNaN(responseTimeSampler.getTvalue(averageRTime)));
 
 		return operation;
 	}
 
 	private double nNaN(double val) {
 		if (Double.isNaN(val))
 			return 0;
 		else if (Double.isInfinite(val))
 			return 0;
 
 		return val;
 	}
 
 	private final double toSeconds(double timestamp) {
 		return timestamp / 1000d;
 	}
 
 	private IMetricSampler getResponseTimeSampler() {
 		return responseTimeSampler;
 	}
 
 	public void merge(OperationSummary from) {
 		opsSuccessful += from.opsSuccessful;
 		opsFailed += from.opsFailed;
 		actionsSuccessful += from.actionsSuccessful;
 
 		opsAsync += from.opsAsync;
 		opsSync += from.opsSync;
 
 		minResponseTime = Math.min(minResponseTime, from.minResponseTime);
 		maxResponseTime = Math.max(maxResponseTime, from.maxResponseTime);
 
 		totalResponseTime += from.totalResponseTime;
 		opsFailedRtimeThreshold += from.opsFailedRtimeThreshold;
 
 		// TODO: How to combine two separate percentiles?
 		rtime99th.accept(from.rtime99th.getPValue());
 		rtime95th.accept(from.rtime95th.getPValue());
 		rtime90th.accept(from.rtime90th.getPValue());
 		rtime50th.accept(from.rtime50th.getPValue());
 
 		// Accept all response time samples
 		LinkedList<Long> rhsRawSamples = from.getResponseTimeSampler().getRawSamples();
 		for (Long obs : rhsRawSamples)
 			responseTimeSampler.accept(obs);
 
 	}
 
 	public long getOpsSuccessful() {
 		return opsSuccessful;
 	}
 
 	public long getOpsFailed() {
 		return opsFailed;
 	}
 
 	public long getTotalResponseTime() {
 		return totalResponseTime;
 	}
 }
