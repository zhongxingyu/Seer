 /**
  * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
  */
 package sim.monitor;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import sim.monitor.subscribers.SubscribeUpdater;
 import sim.monitor.timing.TimePeriod;
 
 /**
  * @author val
  *
  */
 public class Rate extends Publisher {
 
 	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
 			.getLogger(Rate.class);
 
 	private Aggregation aggregation;
 	private TimePeriod rateTime;
 
 	private long rateTimeInMillis;
 
 	private Long count = new Long(0);
 	private Object sum = new Long(0);
 
 	private Object result;
 
 	private Map<Long, Hit> hits = new ConcurrentHashMap<Long, Hit>();
 
 	private long lastTimestampMark = 0;
 
 	protected Collection<Hit> resultHits = new ArrayList<Hit>();
 
 	public Rate(Aggregation aggregation, TimePeriod rateTime, String name,
 			String description) {
 		this.rateTime = rateTime;
 		this.aggregation = aggregation;
 		this.rateTimeInMillis = this.rateTime.getSeconds() * 1000;
 
 		RateScheduler.instance().registerRate(this);
 	}
 
 	/**
 	 * @return the rateTime
 	 */
 	public TimePeriod getRateTime() {
 		return rateTime;
 	}
 
 	/**
 	 * @param rateTime the rateTime to set
 	 */
 	public void setRateTime(TimePeriod rateTime) {
 		this.rateTime = rateTime;
 		this.rateTimeInMillis = this.rateTime.getSeconds() * 1000;
 	}
 
 	/**
 	 * @return the aggregate
 	 */
 	public Aggregation getAggregation() {
 		return aggregation;
 	}
 
 	/**
 	 * @param aggregate
 	 *            the aggregate to set
 	 */
 	public void setAggregation(Aggregation aggregation) {
 		this.aggregation = aggregation;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see sim.monitor.Statistic#getSuffix()
 	 */
 	@Override
 	public String getSuffix() {
 		return rateTime.toString();
 	}
 
 	public void hit(Collection<Hit> hits) {
 		for (Hit hit : hits) {
 			if (lastTimestampMark == 0) {
 				lastTimestampMark = hit.getTimestamp();
 			}
 			if ((hit.getTimestamp() - lastTimestampMark) > rateTimeInMillis) {
 				System.out.println("RESET");
 				lastTimestampMark = lastTimestampMark + rateTimeInMillis;
 				// this.timestamps.add(timestamp);
 				count = new Long(0);
 				sum = new Long(0);
 				result = new Long(0);
 				// continue;
 			}
 			long timestamp = lastTimestampMark + rateTimeInMillis;
 			if (aggregation instanceof Sum) {
 				result = MeasureUtil.sum(result, hit.getValue());
 			} else if (aggregation instanceof Sum) {
 				result = MeasureUtil.sum(result, new Long(1));
 			} else if (aggregation instanceof Min) {
 				result = MeasureUtil.min(result, hit.getValue());
 			} else if (aggregation instanceof Max) {
 				result = MeasureUtil.max(result, hit.getValue());
 			} else if (aggregation instanceof Average) {
 				count = count + 1;
 				sum = MeasureUtil.sum(sum, hit.getValue());
 				result = MeasureUtil.divide(sum, count);
				logger.info("test : " + count + "," + sum + "," + result);
 			}
 			this.hits.put(timestamp, new Hit(timestamp, result));
 			logger.info("put in map : " + timestamp + "," + result);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see sim.monitor.publishers.Publisher#publish()
 	 */
 	@Override
 	public void publish() {
 		String name = null;
 		if (this.getName() == null) {
 			if (aggregation.getName() == null) {
 				name = aggregation.getMonitor().getName() + " "
 						+ this.getSuffix();
 			} else {
 				name = aggregation.getName() + " " + this.getSuffix();
 			}
 		} else {
 			name = this.getName();
 		}
 		SubscribeUpdater.instance().updateAllSubscribers(resultHits,
 				aggregation.getMonitor().getTags(), name, getDescription());
 		resultHits.clear();
 	}
 
 	void update() {
 		long currentTime = System.currentTimeMillis();
 
 		if (lastTimestampMark == 0) {
 			logger.info("skipping scheduler no mark found yet in hits!");
 			return;
 		}
 
 		Long hitTimestamp = null;
 		Hit resultHit = null;
 		while (hitTimestamp == null || currentTime >= hitTimestamp) {
 			if (hitTimestamp != null) {
				if (!hits.containsKey(hitTimestamp)) {
					hitTimestamp = hitTimestamp + rateTimeInMillis;
					continue;
				}
 				resultHit = hits.get(hitTimestamp);
 				hits.remove(hitTimestamp);
 				logger.info("got from map value : " + resultHit.getValue()
 						+ " and remove timestamp : " + hitTimestamp);
 				hitTimestamp = hitTimestamp + rateTimeInMillis;
 			} else {
 				// find oldest timestamp in map
 				Long mark = lastTimestampMark + rateTimeInMillis;
 				while (hits.containsKey(mark)) {
 					hitTimestamp = mark;
 					mark = mark - rateTimeInMillis;
 				}
 				logger.info("got oldest timestamp in map : " + hitTimestamp);
 				if (hitTimestamp == null) {
 					// FIXME should update sbuscribers with value 0, no
 					// data
 					break;
 				}
 			}
 		}
 		if (resultHit != null) {
 			resultHits.add(resultHit);
 			publish();
 		}
 	}
 
 }
