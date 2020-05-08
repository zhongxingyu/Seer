 package at.edu.uas.fm.service;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import at.edu.uas.fm.db.DBHelper;
 
 public class FacilityManagerService {
 
 	public FacilityManagerService() {
 		// nothing to do
 	}
 
 	public Object[] getWorkObjectList(Object[] inputParams) {
 		Long userId = (Long) inputParams[0];
 		return DBHelper.getWorkObjectsForUser(userId);
 	}
 
 	public Object[] getTaskList() {
 		return DBHelper.getTasks();
 	}
 
 	public Object[] getTaskAssignmentList(Object[] inputParams) {
 		Long userId = (Long) inputParams[0];
 		return DBHelper.getAssignmentsForUser(userId);
 	}
 
 	public Object[] getWorkerList() {
 		return DBHelper.getAllWorkers();
 	}
 
	public Boolean authenticate(Object[] inputParams) {
 		return DBHelper.authenticate((String) inputParams[0],
 				(String) inputParams[1]);
 	}
 
 	public Object[] getWorkItemListForAssignments(Object[] inputParams) {
 		List<Long> assignmentIds = new ArrayList<Long>();
 		for (Object object : inputParams) {
 			assignmentIds.add((Long) object);
 		}
 		return DBHelper.getWorkItemListForAssignments(assignmentIds);
 	}
 
 	public Object[] insertOrUpdateWorkItem(Object[] inputParams) {
 		return DBHelper.insertOrUpdateWorkItem((Long) inputParams[0],
 				(Long) inputParams[1], (String) inputParams[2],
 				(Date) inputParams[3]);
 	}
 
 	public Boolean insertAdditionalWorkItem(Object[] inputParams) {
 		return DBHelper.insertAdditionalWorkItem((String) inputParams[0],
 				(String) inputParams[1], (String) inputParams[2],
 				(String) inputParams[3], (String) inputParams[4],
 				(Long) inputParams[5], (Long) inputParams[6]);
 	}
 }
