 package models.timetable;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Contains the list of scheduled entries for the timetable. The entried are
  * used for rendering in the calendar.
  * 
  */
 public class ScheduledTimetableEntryList {
 
 	/**
 	 * Describes the status of the current schedule.
 	 * 
 	 */
 	public enum ScheduleStatus {
 		SUCCESS, ERROR_REACHED_DEADLINE, ERROR_NO_FREETIME_SLOTS, ERROR_DEADLINE_IN_PAST, 
 		ERROR_MAX_WORKLOAD_HOURS_REACHED
 	}
 
 	private List<TimetableEntry> scheduledTimetableEntryList;
 	private Map<String, ScheduleStatus> courseScheduleStatusList;
 
 	public ScheduledTimetableEntryList() {
 		courseScheduleStatusList = new HashMap<String, ScheduledTimetableEntryList.ScheduleStatus>();
 	}
 
 	public List<TimetableEntry> getScheduledTimeSlotList() {
 		return this.scheduledTimetableEntryList;
 	}
 
 	public void setScheduledTimeSlotList(List<TimetableEntry> scheduledTimetableEntryList) {
 		this.scheduledTimetableEntryList = scheduledTimetableEntryList;
 	}
 
 	public void addCourseScheduleStatus(String courseName, ScheduleStatus status) {
 		courseScheduleStatusList.put(courseName, status);
 	}
 
 	public Map<String, ScheduleStatus> getCourseScheduleStatusList() {
 		return this.courseScheduleStatusList;
 	}
 }
