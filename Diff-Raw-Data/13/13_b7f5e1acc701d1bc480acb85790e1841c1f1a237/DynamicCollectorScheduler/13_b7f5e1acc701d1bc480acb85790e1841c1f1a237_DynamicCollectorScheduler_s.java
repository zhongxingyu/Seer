 package com.ranger.collector;
 
 import org.apache.log4j.Logger;
 
 import com.ranger.common.RequestRateLimit;
 
 /*
  * get real time request rate limit by using API, and schedule the collector accordingly
  * 
  */
 
 public class DynamicCollectorScheduler {
 	static Logger log = Logger.getLogger(DynamicCollectorScheduler.class.getName());
 	private Collector collector;
 	private RequestRateLimit rateLimit;
 
 	
 	// if there is plenty of time from the time point of request limit reset to the time point when request reaches the max request number in last hour
 	// we consider this a performance bottleneck. log a warning message
 	// also if there is much time after the request reaches the max request number and before request limit is reset
 	// we consider that a performance bottleneck too
 	// here we setup the threshold for the time gap between the time point of request limit is reset and time point of  request reaches the max request number
 	// default to 20 minutes
 	private long timeGapThreshold = 20*60*1000;
 
 	
 	public DynamicCollectorScheduler(Collector collector, long timeGapThreshold) {
 		this.collector = collector;
 		this.timeGapThreshold = timeGapThreshold;
 		// initialize rateLimit with remainingHits = 0 and resetTime = 1 second
 		// the intention of doing so is to force the collector to get real time RequestRateLimit immediately(after 1 second) , so that the scheduler can schedule the collector correctly.
 		rateLimit = new RequestRateLimit(0, 0, 3);
 	}
 	
 	
 	/*
 	 * schedule the collector according to the policy and restriction of weibo API.
 	 * return 
 	 * 1. -1 if collector can proceed
 	 * 2. 0 if collection need to suspend forever for e.g, a command issued from daemon thread, NOT supported yet.
 	 * 3. positive number if the collector need to sleep some time because of the request limit
 	 * 
 	 */
 	
 	public long schedule() {
 		long result  = 0;
 		long periodStartTime = System.currentTimeMillis();
 		long timeElapsed = 0;
 		long currentRequestTimes = 0;
 		// reverse latest request for RateLimit request
 		while(currentRequestTimes < rateLimit.getRemainingUserHits() - 1) {
 			collector.collect();
 			currentRequestTimes ++;
 			if(collector.isReadyToFlush()) {
 				collector.flush2DB();
 			}
 		} 
 		timeElapsed = (System.currentTimeMillis() - periodStartTime);
 		// it's after the reset time point, the request limit has been reset
 		if(timeElapsed >= rateLimit.getResetTimeInSecond() * 1000) {
 			result = -1;	
 			// if request times reaches the request limit,  and request limit was reset  20(or more than 20) minutes ago 
 			// considering multiple thread to improve performance 
			if(timeElapsed -rateLimit.getResetTimeInSecond() > timeGapThreshold) {
 				log.info("It has been more than 20 minutes since reqeust limit was reset. Suffering performance issue, considering multiple threads" + " time =" + timeElapsed + " request=" + currentRequestTimes);
 			}
 		} else {
 			// more one second for weibo system to finish the reset action
 			result = (rateLimit.getResetTimeInSecond() + 1) * 1000 - timeElapsed;
 			
 			// if request times has reached the request limit, and after 20 or more than 20 minutes, the request limit was reset is then reset
 			if(result > timeGapThreshold) {
 				log.info("It will be more than 20 minutes before reqeust limit is reset., Suffering performance issue, considering multiple token" + " time =" + timeElapsed + " request=" + currentRequestTimes);
 			}
 		}
 		rateLimit = collector.getRateLimit();
 		return result;
 	}
 	
 }
