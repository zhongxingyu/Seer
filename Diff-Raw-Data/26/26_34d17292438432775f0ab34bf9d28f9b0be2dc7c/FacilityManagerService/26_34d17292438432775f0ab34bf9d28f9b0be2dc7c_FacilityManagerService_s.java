 package at.edu.uas.fm.service;
 
 import java.util.Date;
 
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
 		return DBHelper.authenticate((Long) inputParams[0],
 				(String) inputParams[1], (String) inputParams[2]);
 	}
 
 	public Boolean insertWorkItem(Object[] inputParams) {
 		return DBHelper.insertWorkItem((Long) inputParams[0],
 				(String) inputParams[1], (Date) inputParams[2]);
 	}
 
 	public Boolean insertAdditionalWorkItem(Object[] inputParams) {
 		return DBHelper.insertAdditionalWorkItem((String) inputParams[0],
 				(String) inputParams[1], (String) inputParams[2],
 				(String) inputParams[3], (String) inputParams[4],
 				(Long) inputParams[5], (Long) inputParams[6]);
 	}
 }
