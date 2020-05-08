 package org.alt60m.ministry.model.dbio;
 
 import com.kenburcham.framework.dbio.DBIOEntity;
 import com.kenburcham.framework.dbio.DBIOEntityException;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 //import org.alt60m.ministry.Strategy;
 
 import org.alt60m.ministry.Status;
 import org.alt60m.util.DBConnectionFactory;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class ActivityHistory extends DBIOEntity {
 
 	static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
 
 	public ActivityHistory(String id) {
 		this.id = id;
 //		activityId = id;
 		select();
 	}
 
 	public ActivityHistory() {
 	}
 
 	// IDENTITY
 	private String id = "";
 	
 	// KEY
 	private String activity_id = "";
 	
 	private Status from_status;
 	
 	private Status to_status;
 
 	private Date period_end = null;
 
 	private Date period_begin = null;
 
 	private String trans_username = "";
 	
 	private static Log log = LogFactory.getLog(ActivityHistory.class);
 
 	public boolean isPKEmpty() {
 		return id.equals("");
 	}
 
 	public boolean persist() {
 		return isPKEmpty() ? insert() : update();
 	}
 
 	public void localinit() throws DBIOEntityException {
 		String table = "ministry_activity_history";
 
 		setMetadata("Id", "id", "IDENTITY");
 		setMetadata("Activity_id", "activity_id", table);
 		//status info fields
 		setMetadata("FromStatus", "from_status", table);
 		setMetadata("ToStatus", "to_status", table);
 		//start and end date fields
 		setMetadata("PeriodBegin", "period_begin", table);
 		setMetadata("PeriodEnd", "period_end", table);
 		setMetadata("TransUsername", "trans_username", table);
 
 		setAutodetectProperties(false);
 	}
 
 	public String getFromStatusFullName() {
 		return from_status.getName();
 	}
 	public String getToStatusFullName() {
 		return to_status.getName();
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 	
 	public void setActivity_id(String activity_id) {
 		this.activity_id = activity_id;
 	}
 	public String getActivity_id() {
 		return activity_id;
 	}
 
 
 	public String getFromStatus() {
 		if (from_status == null) {
 			return null;
 		}
 		return from_status.toString();
 	}
 	public void setFromStatus(String status) {
 		if (status != null && !status.equals("")) {
 			this.from_status = Status.valueOf(status);
 		}
 	}
 	
 	public String getToStatus() {
 		if (to_status == null) {
 			return null;
 		}
 		return to_status.toString();
 	}
 	public void setToStatus(String status) {
 		this.to_status = Status.valueOf(status);
 	}
 
 	public boolean isActive() {    // doing this for tostatus only.  It represents current.
 		return (Status.IN != to_status);
 	}
 
 	public Date getPeriodEnd() {
 		return period_end;
 	}
 
 	public void setPeriodEnd(Date periodEnd) {
 		  
 		//Putting the timestamp back on to the date field for finer granularity
 		// this.periodEnd = org.alt60m.util.DateUtils.clearTimeFromDate(periodEnd);
 		this.period_end = periodEnd;
 	}
 
 	public Date getPeriodBegin() {
 		return period_begin;
 	}
 
 	public void setPeriodBegin(Date periodBegin) {
 		//Putting the timestamp back on to the date field for finer granularity
 		//this.periodBegin = org.alt60m.util.DateUtils.clearTimeFromDate(periodBegin);
 		this.period_begin = periodBegin;
 	}
 
 	public void setPeriodEndString(String periodEnd) {
 		try {
 			this.period_end = dateFormat.parse(periodEnd);
 		} catch (ParseException e) {
 			log.error(e, e);
 		}
 	}
 
 	public void setPeriodBeginString(String periodBegin) {
 		try {
 			this.period_begin = dateFormat.parse(periodBegin);
 		} catch (ParseException e) {
 			log.error(e, e);
 		}
 	}
 
 	public String getTransUsername() {
 		return trans_username;
 	}
 
 	public void setTransUsername(String trans_username) {
 		this.trans_username = trans_username;
 	}
 
 	public void getFieldvalue() {
 	}
 
 	public String getFieldvalue(String foo) {
 		return "";
 	}
 
 	public void getHeaderName() {
 	}
 
 	public String getHeaderName(String foo) {
 		return "";
 	}
 
 	public void getColumnName() {
 	}
 
 	public String getColumnName(String foo) {
 		return "";
 	}
     public String getLastActivityHistoryID(String ActivityID) {
     	ActivityHistory last = new ActivityHistory();
    	last.select("id in (SELECT MAX(id) FROM ministry_activity_history " +
 					   "WHERE activity_id = " + ActivityID + ")");
     	String lastHistoryID = last.getId();
         return(lastHistoryID);
     }
 }
