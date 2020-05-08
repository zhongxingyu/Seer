 package misc;
 /* This program is licensed under the terms of the GPL V3 or newer*/
 /* Written by Johannes Putzke*/
 /* eMail: die_eule@gmx.net*/
 
 
 import java.text.DateFormat;
 import java.util.Date;
 
 /**
  * This class creates a new job for recording a stream
  * You can select between weekly, once ....
  * 
  * @author Schmoffel	
  *
  */
 public class SchedulJob {
 	//witch stream should be started? ID of stream in DB
 	private int streamID = 0;
 	private int schedulID = 0;
 	private long startTime = 0;
 	private long stopTime = 0;
 	private long oldStopTime = 0;
 	private boolean useOldStopTime = false;
 	private boolean daily = false;
 	private boolean weekly = false;
 	private boolean monthly = false;
 	private boolean enableJob = false;
 	private boolean isRecording = false;
 	private boolean atStart = false;
 	private String comment = "";
 
 	public static int lastID = 1;
 	
 	/**
 	 * 
 	 * @param schedulJobID: the job ID of this job in xml-file
 	 * @param streamID: the ID in xml-file for the recording stream
 	 * @param startTime: start time in mills
 	 * @param stopTime: stop time in mills
 	 * @param howOftenID: 0=daily; 1=weekly; 2=monthly; else once
 	 * @param enable: enable this schedulJob
 	 * @param comment: the comment for this stream
 	 * @param streamName: the streamName for the streamID
 	 */
 	public SchedulJob(int schedulJobID,int streamID, long startTime,
 			long stopTime, int howOftenID, boolean enable, String comment) {
 		this.schedulID = schedulJobID;
 		this.streamID = streamID;
 		this.startTime = startTime;
 		this.stopTime = stopTime;
 		this.enableJob = enable;
 		this.comment = comment;
 
 		if(howOftenID == 0) {
 			daily = true;
 		} else if (howOftenID == 1) {
 			weekly = true;
 		} else if (howOftenID == 2) {
 			monthly = true;
 		} else if (howOftenID == 3) {
 			atStart = true;
 		}
 	}
 	
 	
 	//
 	public static int getNewID() {
 		lastID++;
 		return lastID-1;
 	}
 	
 	/**
 	 * 
 	 * @param newSchedulID: the ID that this schedulJob has in the xml-file
 	 */
 	public void setSchedulJobID(int newSchedulID) {
 		schedulID = newSchedulID;
 	}
 	
 	/**
 	 * Set an new value for the recording stream id
 	 * @param id: the ID witch representing the stream
 	 * witch should be recorded in the xml-file
 	 */
 	public void setStreamID(int newStreamID) {
 		streamID = newStreamID;
 	}
 	
 	/**
 	 * set the startTime to an new value
 	 * @param startTime: The new startTime in mills
 	 */
 	public void setStartTime(long newStartTime) {
 		startTime = newStartTime;
 	}
 	
 	/**
 	 * set the stopTime to an new va
 	 * @param newStopTime: the new stoptime in mills
 	 */
 	public void setStopTime(long newStopTime) {
 		stopTime = newStopTime;
 	}
 	
 	/**
 	 * sets the new oldStopTime, where the stream should be stoped
 	 * @param oldStopTime: time in milliseconds
 	 */
 	public void setOldStopTime(long oldStopTime) {
 		this.oldStopTime = oldStopTime;
 	}
 	
 	/**
 	 * The a new status. If false the job will not record
 	 * if the specific time.
 	 * @param enable: true, if the stream should start recording
 	 */
 	public void setJobEnabled(boolean enable) {
 		enableJob = enable;
 	}
 	
 	/**
 	 * set the right parameter for the job
 	 * @param howOftenID: 0=daily; 1=weekly; 2=monthly; else once
 	 */
 	public void setJobCounts(int howOftenID) {
 		if(howOftenID == 0) {
 			daily = true;
 		} else if (howOftenID == 1) {
 			weekly = true;
 		} else if (howOftenID == 2) {
 			monthly = true;
 		} 
 	}
 	
 	/**
 	 * the the comment for the scheduljob
 	 * @param newComment: the new comment
 	 */
 	public void setComment(String newComment) {
 		comment = newComment;
 	}
 
 	
 	/**
 	 * 
 	 * @return the ID in xml-file for the recording stream. 
 	 */
 	public int getStreamID() {
 		return streamID;
 	}
 	
 	/**
 	 * 
 	 * @return: the ID in xml-file of this job
 	 */
 	public int getSchedulID() {
 		return schedulID;
 	}
 	
 	/**
 	 * 
 	 * @return the startTime for this job in mills
 	 */
 	public long getStartTime() {
 		return startTime;
 	}
 	
 	/**
 	 * 
 	 * @return the stopTime for this job in mills
 	 */
 	public long getStopTime() {
 		return stopTime;
 	}
 	
 	/**
 	 * gets the time, where the stream should be stoped
 	 * @return the old stop time
 	 */
 	public long getOldStopTime() {
 		return oldStopTime;
 	}
 	
 	/**
 	 * return the start time as a located date+time string
 	 * @return: date+time as a string
 	 */
 	public String getStartTimeAsLocaleTime() {
 		String date = DateFormat.getDateInstance(DateFormat.SHORT).format(new Date(startTime));
 		String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(startTime));
 		return date+" - "+time;
 	}
 	
 	/**
 	 * return the stop time as a located date+time string
 	 * @return: date+time as a string
 	 */
 	public String getStopTimeAsLocaleTime() {
 		String date = DateFormat.getDateInstance(DateFormat.SHORT).format(new Date(stopTime));
 		String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(stopTime));
 		return date+" - "+time;
 		
 	}
 	
 	/**
 	 * 
 	 * @return: -1=once; 0=daily; 1=weekly; 2=monthly ;3=at Start 
 	 */
 	public int getJobCount() {
 		int count = -1;
 		if(daily) {
 			count = 0;
 		} else if (weekly) {
 			count = 1;
 		} else if (monthly) {
 			count = 2;
 		} else if (atStart) {
 			count = 3;
 		}
 		return count;
 	}
 	
 	/**
 	 * 
 	 * @return: true if job is enabled
 	 */
 	public boolean isJobenabled() {
 		return enableJob;
 	}
 	
 	/**
 	 * 
 	 * @return: the comment for this job as String
 	 */
 	public String getComment() {
 		return comment;
 	}
 	
 	/**
 	 * return true, if the job only record once
 	 * else false
 	 */
 	public Boolean isOnceJob() {
 		if(daily  || weekly  || monthly || atStart) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	/**
	 * If the jobs just starts when StreamRipStar starts, then its a "atStartJob"
	 * @return true, if its one, else false
	 */
	public boolean isAtStartJob()
	{
		return atStart;
	}
	
	/**
 	 * 
 	 * @return true, if the old stoptime should be used
 	 */
 	public boolean useOldStopTime() {
 		return useOldStopTime;
 	}
 	
 	/**
 	 * set the status for the oldStopTime
 	 * @param use, if the old start time should be used
 	 */
 	public void setUseOldStopTime(boolean use) {
 		useOldStopTime = use;
 	}
 	
 	/**
 	 * Set the new status for a schedul job
 	 * @param newStatus: true for "is currently recording", else false
 	 */
 	public void setStatus(boolean newStatus) {
 		isRecording = newStatus;
 	}
 	
 	/**
 	 * get the current status for the job
 	 * @return true, if the stream records
 	 */
 	public boolean getstatus() {
 		return isRecording;
 	}
 	
 	/**
 	 * changes the status of this job to the other one
 	 * from disable -> enable; enable -> disable
 	 */
 	public void toggleEnableJob()
 	{
 		if(this.enableJob) {
 			enableJob = false;
 		} else {
 			enableJob = true;
 		}
 	}
 }
