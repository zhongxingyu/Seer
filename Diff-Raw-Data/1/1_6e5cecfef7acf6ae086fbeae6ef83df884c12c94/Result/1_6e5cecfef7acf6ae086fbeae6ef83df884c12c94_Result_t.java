 package ch.hsr.objectCaching.action.result;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class Result implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private List<TimeRecord> listOfAttempt;
 	private TimeRecord currentTry;
 	private TimeRecord totalTimeRecord;
 	
 	public enum BasicAction {
 		READ, WRITE, TOTAL_ACTION_TIME;
 	}
 	
 	public enum ActionResult {
 		SUCCESSFUL, FAILED;
 	}
 
 	public Result() {
 		listOfAttempt = new ArrayList<TimeRecord>();
 	}
 
 	public void stopTimeMeasurement() {
 		currentTry.setStopTime(System.nanoTime());
 		currentTry.setActionResult(ActionResult.SUCCESSFUL);
 		listOfAttempt.add(currentTry);
 	}
 
 	public List<TimeRecord> getAllIntermediateResult() {
 		return listOfAttempt;
 	}
 
 	public int getNumberOfTry() {
 		return listOfAttempt.size();
 	}
 
 	public void startTimeMeasurement(BasicAction type) {
 		currentTry = new TimeRecord(type);
 		currentTry.setStartTime(System.nanoTime());
 	}
 
 	public void stopTimeMeasurement(ActionResult result) {
 		currentTry.setStopTime(System.nanoTime());
 		currentTry.setActionResult(result);
 		listOfAttempt.add(currentTry);	
 	}
 	
 	public void startTotalTimeMeasurement(){
 		totalTimeRecord = new TimeRecord(BasicAction.TOTAL_ACTION_TIME);
 		totalTimeRecord.setStartTime(System.nanoTime());
 	}
 	
 	public void stopTotalTimeMeasurement(){
 		totalTimeRecord.setStopTime(System.nanoTime());
		totalTimeRecord.setActionResult(ActionResult.SUCCESSFUL);
 	}
 
 	public TimeRecord getTotalTimeRecord() {
 		return totalTimeRecord;
 	}
 }
