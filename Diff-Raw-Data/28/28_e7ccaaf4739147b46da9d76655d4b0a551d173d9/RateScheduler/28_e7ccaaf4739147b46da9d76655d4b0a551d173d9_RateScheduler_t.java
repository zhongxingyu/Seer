 /**
  * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
  */
 package sim.monitor;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 /**
  * @author val
  *
  */
 public class RateScheduler {
 
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(Rate.class);

 	private Map<Long, Set<Rate>> ratesBySeconds = new HashMap<Long, Set<Rate>>();
 
 	private ScheduledExecutorService scheduler = Executors
 			.newScheduledThreadPool(1);
 
 	private Set<ScheduledFuture<?>> schedules = new HashSet<ScheduledFuture<?>>();
 
 	private static RateScheduler instance = new RateScheduler();
 
 	static RateScheduler instance() {
 		return instance;
 	}
 
 	private RateScheduler() {
 
 	}
 
 	void registerRate(Rate rate) {
 		Long seconds = rate.getRateTime().getSeconds();
 		if (!ratesBySeconds.containsKey(seconds)) {
 			ratesBySeconds.put(seconds, new HashSet<Rate>());
 			ScheduledFuture<?> scheduleFuture = scheduler.scheduleAtFixedRate(
 					new Task(seconds), 0, seconds, TimeUnit.SECONDS);
 			schedules.add(scheduleFuture);
 		}
 		ratesBySeconds.get(seconds).add(rate);
 	}
 
 	private class Task implements Runnable {
 
 		private Long seconds;
 
 		Task(Long seconds) {
 			this.seconds = seconds;
 		}
 
 		public void run() {
 			Set<Rate> rates = ratesBySeconds.get(seconds);
 			for (Rate rate : rates) {
				try {
					rate.update();
				} catch (Exception e) {
					logger.error("Error in rate update !", e);
				}
 			}
 		}
 	};
 
 	void shutdown() {
 		for (ScheduledFuture<?> schedule : schedules) {
 			schedule.cancel(false);
 		}
 	}
 }
