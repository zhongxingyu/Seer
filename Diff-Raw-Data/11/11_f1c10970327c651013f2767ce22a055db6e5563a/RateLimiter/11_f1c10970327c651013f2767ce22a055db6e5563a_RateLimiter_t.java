 package com.awesomecat.jslogger;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Calendar;
 
 public class RateLimiter {
 	private class RateDataSet {
 		public final Map<Integer, Integer> userToDataMap;
 		public RateDataSet(){
 			userToDataMap = new HashMap<Integer, Integer>();
 		}
 
 		public int getUserUsage(int sessionId){
 			if(userToDataMap.containsKey(sessionId)){
 				return userToDataMap.get(sessionId);
 			}
 			return 0;
 		}
 
 		public int addUserUsage(int sessionId, int usage){
 			int current = 0;
 			if(userToDataMap.containsKey(sessionId)){
 				current = userToDataMap.get(sessionId);
 			}
 			userToDataMap.put(sessionId, current+usage);
 			return current+usage;
 		}
 	}
 	
 	private final Map<Integer, RateDataSet> logsList;
 	private final Map<Integer, RateDataSet> dataList;
 	
 	public static final int pastMinutesToKeep = JavaScriptLogger.getConfig().getInt("rateLimit.pastMinutesToKeep");
	public static final int dataLimit = JavaScriptLogger.getConfig().getInt("rateLimit.dataLimit")*1024;
 	public static final int logsLimit = JavaScriptLogger.getConfig().getInt("rateLimit.logsLimit");
 	
 	public RateLimiter(){
 		logsList = new HashMap<Integer, RateDataSet>();
 		dataList = new HashMap<Integer, RateDataSet>();
 	}
 
 	/**
 	 * Add a record of a user sending in a log message + some data
 	 * @param sessionId Identity of user
 	 * @param logs The number of log messages received
	 * @param dataInBytes The amount of data, in bytes, that has been used
 	 */
	public void addData(int sessionId, int logs, int dataInBytes){
 		// NOTE: We use logs (instead of assuming 1) because in the future, 
 		// the JS portion might build a bulk of messages before sending them
 		// Roll over to new minute
 		int newMinute = handleRollover();
		dataList.get(newMinute).addUserUsage(sessionId, dataInBytes);
 		logsList.get(newMinute).addUserUsage(sessionId, logs);
 	}
 
 	/**
 	 * Is the given user rate limited?
 	 * @param sessionId Identify user by session ID
 	 * @return True if so, false otherwise
 	 */
 	public boolean isUserLimited(int sessionId){
 		// Keep return on one line.  This short-circuits if they're over datalimit quickly
 		return 
 			dataOverLastXMinutes(dataList, pastMinutesToKeep, sessionId) >= dataLimit
 			|| dataOverLastXMinutes(logsList, pastMinutesToKeep, sessionId) >= logsLimit;
 	}
 	
 	/**
 	 * Determines the amount of data in dataSet used over the last X minutes by user identifier by sessionId
 	 * @param dataSet The dataSet to examine.
 	 * @param x The last x minutes to consider. 1 returns current minute only
 	 * @param sessionId How we're identifying the user. Do not use static session ID
 	 * @return
 	 */
 	private int dataOverLastXMinutes(Map<Integer, RateDataSet> dataSet, int x, int sessionId){
 		assert(x > 0 && sessionId >= 0);
 		int currentMinute = handleRollover();
 		int currentData = 0;
 		int minutesExamined = 0; // Average over number examined, not last x potentially
 		for(int i=0;i<x;i++){
 			if(dataSet.containsKey(currentMinute)){
 				RateDataSet data = dataSet.get(currentMinute);
 				currentData += data.getUserUsage(sessionId);
 				minutesExamined++;
 			}
 		}
 		return currentData / minutesExamined;
 	}
 
 	private int lastMinuteHandled = -1;
 	/**
 	 * Rollsover to new minute if it's changed 
 	 * @return The new minute to use
 	 */
 	private int handleRollover(){
 		int newMinute = getCurrentMinute();
 		if(newMinute != lastMinuteHandled){
 			// Changed the current minute.
 			// We need to clear the minutes between lastMinuteHandled and newMinute (not including lastMinuteHandled.)
 			while((++lastMinuteHandled % 60) <= newMinute){
 				addDataSet(lastMinuteHandled % 60);
 			}
 
 			lastMinuteHandled = newMinute;
 		}
 		return newMinute;
 	}
 	
 	/**
 	 * Removes and puts in a new data set for given minute
 	 * @param minute
 	 */
 	private void addDataSet(int minute){
 		logsList.put(minute, new RateDataSet());
 		dataList.put(minute, new RateDataSet());
 	}
 
 	/**
 	 * Gets the current minute to use as an index
 	 * @return
 	 */
 	public static int getCurrentMinute(){
 		return Calendar.getInstance().get(Calendar.MINUTE);
 	}
 }
