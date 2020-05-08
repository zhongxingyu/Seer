 package de.hpi.fgis.util;
 
 import java.text.DateFormat;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Logger;
 
 /**
  * 
  * Format a progress of some operation, i.e. dictionary rebuild
  * 
  * this class is inspired by the implementation of rainman of the MediaWiki - lucene searcher project:</br>
  * http://www.mediawiki.org/wiki/Extension:Lucene-search 
  * @author tongr
  *
  */
 public class ProgressReport {
	private static final Logger logger =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
 	protected long start;
 	protected long max;
 	protected long count = 0;
 	protected long report;
 	protected String what;
 	protected String unit;
 	protected MessageFormat startNoMax = new MessageFormat("Started: {0}");
 	protected MessageFormat noMax = new MessageFormat("Processed {0} {3} - {1} ({2} {3}/sec)");
 	protected MessageFormat withMax = new MessageFormat("Processed {0} / {1} {4} - {2} ({3} {4}/sec)");
 	protected MessageFormat finished = new MessageFormat("Finished {0} {4} - {1} ({2} {4}/sec) in {3}");
 	protected final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
 	private long lastMessageTime = Long.MIN_VALUE;
 	/**
 	 * minimal delay between two status messages (default 5seconds)
 	 */
 	public static long MIN_MESSAGE_DELAY = 5 * 1000;
 	
 	/**
 	 * creates a new {@link ProgressReport} instance
 	 * @param what short msg about what is being processed (e.g. terms, titles, documents.. )
 	 */
 	public ProgressReport(String what){
 		this.max = -1;
 		this.report = 1;
 		this.what = what;
 		this.unit = "";
 		this.lastMessageTime = this.start = System.currentTimeMillis();
 		logger.info(startNoMax.format(new Object[] {what}));
 	}
 	
 	/**
 	 * sets the estimated max number of events
 	 * @param max estimated max number of events
 	 * @return the {@link ProgressReport} instance
 	 */
 	public ProgressReport setMax(long max) {
 		this.max = max;
 		return this;
 	}
 
 	/**
 	 * sets the feedback report limit
 	 * @param report after how many events to print out a report
 	 * @return the {@link ProgressReport} instance
 	 */
 	public ProgressReport setReport(long report) {
 		this.report = report<=0?1:report;
 		return this;
 	}
 
 	/**
 	 * sets the unit for the steps (e.g. bytes -> progress X bytes/sec)
 	 * @param unit the unit for the steps (e.g. bytes -> progress X bytes/sec)
 	 * @return the {@link ProgressReport} instance
 	 */
 	public ProgressReport setUnit(String unit) {
 		this.unit = unit;
 		return this;
 	}
 	
 	/**
 	 * progress increase
 	 */
 	public synchronized void inc(){
 		count++;
 		if(count % report == 0){
 			print(count);
 		}
 	}
 	
 	public synchronized void set(long newCount){
 		long oldCount = count;
 		count = newCount;
 		if(oldCount / report != newCount / report){
 			print(newCount);
 		}
 	}
 	
 	private void print(long currentCount) {
 		long now = System.currentTimeMillis();
 		if((now - lastMessageTime) >=  MIN_MESSAGE_DELAY) {
 			if(max == -1) {
 				logger.info(noMax.format(new Object[] {currentCount, what, rate(now), unit}));
 			} else {
 				logger.info(withMax.format(new Object[] {currentCount, max, what, rate(now), unit}));
 			}
 			lastMessageTime = now;
 		}
 	}
 	
 	private double rate(long now){
 		if(now==start) {
 			return (count*1000.0);
 		}
 		return (count*1000.0)/(now-start);
 	}
 	
 	public void finish() {
 		long now = System.currentTimeMillis();
 		logger.info(finished.format(new Object[]{count, what, rate(now), formatTime(now-start), unit}));
 	}
 	
 	public static String formatTime(long ms) {
 		if(ms >= 3600000) return ms/3600000+"h "+(ms%3600000)/60000+"m "+(ms%60000)/1000+"s";
 		else if(ms >= 60000) return (ms%3600000)/60000+"m "+(ms%60000)/1000+"s";
 		else if(ms >= 1000) return ms/1000+"s";
 		else return ms+"ms";
 	}
 	
 	public static String now() {
 		return DATE_FORMAT.format(new Date());
 	}
 	public long getProgress() {
 		return count;
 	}
 }
