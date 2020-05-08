 package com.infinitiessoft.btrs.attendance;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Logger;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.log.Log;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 
 import com.infinitiessoft.btrs.action.ReportList;
 import com.infinitiessoft.btrs.exceptions.BtrsRuntimeException;
 import com.infinitiessoft.btrs.model.Report;
 
 @Name("attendanceConnector")
 @Scope(ScopeType.CONVERSATION)
 public class AttendanceConnector {
 
 	private static final String IN_DATE_FORMAT = "yyyy-MM-dd HH:mm";
 	
 	private static final int FILTER_IN_MONTHS = 3;
 	
 	private static AttendanceRecordComparator attendanceRecordComparator  = new AttendanceRecordComparator();
 	
 	@Logger Log log;
 	
 	@In
 	ReportList reportList;
 	
 	@In("#{report}")
 	Report report;
 	
 	private String attendanceApiUrl;
 	
 	private HttpURLConnection conn;
 	
 	private Map<Long, AttendanceRecord> records;
 	private Set<Long> usedAttendanceRecordsIds;
 	
 	
 	public Map<Long, AttendanceRecord> requestAttendanceRecords(Long id) {
 		String urlString = attendanceApiUrl + "permitedrecord?employeeid=" + id;
 		records = new HashMap<Long, AttendanceRecord>();
 		
 		BufferedReader response = getResponse(urlString);
 		if (response != null) {
 			usedAttendanceRecordsIds = reportList.getUsedAttendanceRecordsIds();
 			log.debug("List of usedAttendanceRecordsIds has size: #0", usedAttendanceRecordsIds.size());
 			
 			JSONParser parser = new JSONParser();
 			try {
 				Object obj = parser.parse(response);
 				SimpleDateFormat sdf = new SimpleDateFormat(IN_DATE_FORMAT);
 				JSONArray objRecords = (JSONArray) obj;
 				
 				for (Object object : objRecords) {
 					JSONObject objRecord = (JSONObject) object;
 					AttendanceRecord record = createFromJSON(objRecord, sdf);

 					if (filter(record)) {
 						records.put(record.getId(), record);
 					}
 				}
 			} catch (Exception e) {
 				log.error("Error while parsing JSON response from Attendance system", e);
 			}
 		}
		log.debug("List of filtered Attendance Records has size: #0", records.size());
 		disconnect();
 		return records;
 	}
 	
 	public List<AttendanceRecord> getAsList(Long id) {
		Map<Long, AttendanceRecord> records = requestAttendanceRecords(id);
		List<AttendanceRecord> result = new ArrayList<AttendanceRecord>();
		if ( ! records.isEmpty()) {
			result.addAll(records.values());
			Collections.sort(result, attendanceRecordComparator);
		}
 		return result;
 	}
 	
 	/**
 	 * filter by 3 months period and by already used ones
 	 */
 	private boolean filter(AttendanceRecord record) {
 		Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.MONTH, - FILTER_IN_MONTHS);
 		Date filterDate = cal.getTime();
 		return record.getStartDate().after(filterDate) &&
 				( ! usedAttendanceRecordsIds.contains(record.getId())
 						|| record.getId().equals(report.getAttendanceRecordId()));
 	}
 	
 	public BufferedReader getResponse(String urlString) {
 		try {
 			URL url = new URL(urlString);
 			conn = (HttpURLConnection) url.openConnection();
 			conn.setRequestMethod("GET");
 			conn.setRequestProperty("Accept", "application/json");
 			if (conn.getResponseCode() != 200) {
 				throw new BtrsRuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
 			}
 			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
 			return br;
 		} catch (MalformedURLException e) {
 			log.error("Attendance API's URL is wrong: " + urlString, e);
 		} catch (IOException e) {
 			log.error("Error while getting response from Attendance system. URL: " + urlString, e);
 		}
 		return null;
 	}
 	
 	private AttendanceRecord createFromJSON(JSONObject record, SimpleDateFormat sdf) throws ParseException {
 		Long id = (Long) record.get("id");
 		
 		String bookDateString = (String) record.get("bookDate");
 		String startDateString = (String) record.get("startDate");
 		String endDateString = (String) record.get("endDate");
 		Date bookDate = sdf.parse(bookDateString);
 		Date startDate = sdf.parse(startDateString);
 		Date endDate = sdf.parse(endDateString);
 		
 		String type = (String) record.get("type");
 		String reason = (String) record.get("reason");
 		String employeeName = (String) record.get("employeeName");
 		Long employeeId = (Long) record.get("employeeId");
 		
 		return new AttendanceRecord(id, bookDate, startDate, endDate, type, reason, employeeName, employeeId);
 		
 	}
 	
 	public AttendanceRecord getRecord(Long id) {
 		if (records != null) {
 			return records.get(id);
 		}
 		return null;
 	}
 	
 	private void disconnect() {
 		if (conn != null) {
 			conn.disconnect();
 		}
 	}
 	
 }
