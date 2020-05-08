 package com.schedulerapp.jsonparser;
  
 import java.util.ArrayList;
 import java.util.List;
  
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
  
 import com.schedulerapp.models.Campus;
 import com.schedulerapp.models.Department;
 import com.schedulerapp.models.DepartmentTimeslotLinkage;
 import com.schedulerapp.models.Departmenttimeslot;
 import com.schedulerapp.models.User;
  
 public class ResponseParser {
 	
 	public User parseUser(String userJson) throws NumberFormatException, JSONException {		
 		JSONObject jObj = new JSONObject(userJson);		
 		return User.getUserFromJson(jObj);
 	}
 
 	public List<Campus> parseCampusList(String result) throws NumberFormatException, JSONException {
 		List<Campus> campuses = new ArrayList<Campus>();
 		campuses.add(Campus.getSelectPropmpt());
 		JSONArray jObj = new JSONArray(result);
 		for (int i=0; i<jObj.length(); i++) {
 			JSONObject obj = jObj.getJSONObject(i);
 			campuses.add(Campus.getCampusFromJson(obj));
 		}
 		return campuses;
 	}
  
 	public List<Department> parseDepartmentList(String result) throws NumberFormatException, JSONException {
 		List<Department> departments = new ArrayList<Department>();
 		departments.add(Department.getSelectPropmpt());
 		JSONArray jObj = new JSONArray(result);
 		for (int i=0; i<jObj.length(); i++) {
 			JSONObject obj = jObj.getJSONObject(i);
 			departments.add(Department.getDepartmentFromJson(obj));
 		}
 		return departments;
 	}
 	
 	public List<Departmenttimeslot> parseDepartmenttimeslotList(String result) throws NumberFormatException, JSONException {
 		List<Departmenttimeslot> timeSlots = new ArrayList<Departmenttimeslot>();
 		JSONArray jObj = new JSONArray(result);
 		for (int i=0; i<jObj.length(); i++) {
 			JSONObject obj = jObj.getJSONObject(i);
 			timeSlots.add(Departmenttimeslot.getDepartmenttimeslotFromJson(obj));
 		}
 		return timeSlots;
 	}
 	
 	public List<DepartmentTimeslotLinkage> parseDepartmenttimeslotLinkageList(String result) throws NumberFormatException, JSONException {
 		List<DepartmentTimeslotLinkage> timeSlots = new ArrayList<DepartmentTimeslotLinkage>();
 		timeSlots.add(DepartmentTimeslotLinkage.getSelectPropmpt());
 		JSONArray jObj = new JSONArray(result);
 		for (int i=0; i<jObj.length(); i++) {
 			JSONObject obj = jObj.getJSONObject(i);
 			timeSlots.add(DepartmentTimeslotLinkage.getDepartmentTimeslotLinkageFromJson(obj));
 		}
 		return timeSlots;
 	}	
}
