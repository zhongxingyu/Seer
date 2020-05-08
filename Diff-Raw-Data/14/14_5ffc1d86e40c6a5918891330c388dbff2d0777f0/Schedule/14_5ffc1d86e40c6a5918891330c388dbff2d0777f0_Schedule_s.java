 /*
  *  Copyright 2010 The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.mitre.medcafe.model;
 
 import java.io.*;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex .*;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.*;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.mitre.medcafe.restlet.PatientListResource;
 import org.mitre.medcafe.restlet.Repositories;
 import org.mitre.medcafe.util.Constants;
 import org.mitre.medcafe.util.DbConnection;
 import org.mitre.medcafe.util.WebUtils;
 import org.restlet.ext.json.JsonRepresentation;
 
 /**
  *  Representation of the text data
  *  @author: Gail Hamilton
  */
 public class Schedule
 {
 	public final static String KEY = Schedule.class.getName();
 	public final static Logger log = Logger.getLogger( KEY );
 	static{log.setLevel(Level.FINER);}
 
 	//Parameters that are common to all Widgets
 	private int patientId =0;
 	private String startTime ="";
 	private String startDate ="";
 	private String endTime ="";
 	private String duration ="";
 
 	 
 	//All the other parameters
 	private  HashMap<String, String > params = new HashMap<String, String >();
 
 	public static final String APPT_DURATION = "30";
 	public static final String ID = "patient_id";
 	public static final String APPT_TIME = "appoint_time";
 	public static final String APPT_DATE = "appoint_date";
 	public static final String END_TIME = "end_time";
 	public static final String DURATION = "duration";
 	public static final String DATE_FORMAT = "yyyy-MM-dd";
 	public static final String TIME_FORMAT = "HH:mm:ss";
 	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
 	
 	public static final String DEFAULT_TIME = "08:00:00";
 	public static final String SELECT_AVAILABLE_APPOINTMENT= "select id,  last_name || ', ' || first_name as name, appoint_time, end_time from schedule order by appoint_date, appoint_time where appoint_date = ? ";
 	public static final String INSERT_APPOINTMENT = "INSERT INTO schedule  ( patient_id, first_name, last_name, appoint_date, appoint_time, end_time ) values (?,?,?,?,?,?) ";
 	public static final String DELETE_APPOINTMENT= "DELETE FROM schedule where ( patient_id=? AND appoint_date=? AND appoint_time=? ) ";
 	
 	public static final int DATE_ONLY_FORMAT_TYPE = 0;
 	public static final int TIME_ONLY_FORMAT_TYPE = 1;
 	public static final int DATE_TIME_FORMAT_TYPE = 2;
 	
 	private static DbConnection dbConn = null;
 	public Schedule()	
 	{
 		super();
 	
 	}
 	public Schedule(DbConnection conn)	
 	{
 		super();
 		dbConn = conn;
 	
 	}
 	public static DbConnection setConnection() throws SQLException
 	{
 		if (dbConn == null)
 			dbConn= new DbConnection();
 		return dbConn;
 	}
 	
 	public static DbConnection getConnection() throws SQLException
 	{
 		return dbConn;
 	}
 	
 	public static void closeConnection() throws SQLException
 	{
 		dbConn.close();
 	}
 	
 	public JSONObject toJSON() throws JSONException
 	{
 		 JSONObject o = new JSONObject();
 		 o.put(Schedule.ID, this.getPatientId());
 		 o.put(Schedule.APPT_TIME, this.getStartTime());
 		 o.put(Schedule.APPT_DATE, this.getStartDate());
 		 o.put(Schedule.END_TIME, this.getEndTime());
 		 o.put(Schedule.DURATION, this.getDuration());
 				 
 		 return o;
 		 
 	}
 	
 	public static JSONObject addAppointment( String patientId, String date, String dateTime, String endTime) throws SQLException
 	{
 		JSONObject ret = new JSONObject();
 		setConnection();
 		
 		//Get the patient Name
 		try 
 		{
 			String insertQuery = Schedule.INSERT_APPOINTMENT;
 			int rtn = 0;
 			int patient_id = Integer.parseInt(patientId);
 			String err_mess = "Could not insert appointment for patient  " + patient_id;
 			
 			rtn = dbConn.psExecuteUpdate(insertQuery, err_mess , patient_id, dateTime);	
 			
 			if (rtn < 0 )
 				return WebUtils.buildErrorJson( "Problem on deleting widget data from database ." );
 			
 		}
 		finally
 		{
 		
 		}
 		return ret;
 	}
 	
 	public static JSONObject addAppointment( JSONObject appointment) throws SQLException
 	{
 		JSONObject ret = new JSONObject();
 		setConnection();
 		
 		//Get the patient Name
 		try 
 		{
 			String insertQuery = Schedule.INSERT_APPOINTMENT;
 			int rtn = 0;
 			int patient_id = Integer.parseInt(appointment.getString(Constants.PATIENT_ID));
 			String err_mess = "Could not insert appointment for patient  " + patient_id;
 			
 			rtn = dbConn.psExecuteUpdate(insertQuery, err_mess , patient_id);	
 			
 			if (rtn < 0 )
 				return WebUtils.buildErrorJson( "Problem on deleting widget data from database ." );
 			
 		} catch (NumberFormatException e) {
 			// TODO Auto-generated catch block
 			return WebUtils.buildErrorJson( "Problem on patient id" );
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		finally
 		{
 		
 		}
 		return ret;
 	}
 	public static JSONObject getNextAvailAppointment( String patientId, String dateStr, String timeStr) throws SQLException
 	{
 		JSONObject o = new JSONObject();
 		setConnection();
 		
 		//Get the patient Name
 		try 
 		{
 			//public static final String SELECT_AVAILABLE_APPOINTMENT= "select id,  last_name || ', ' || first_name as name, appoint_time, end_time from schedule order by appoint_date, appoint_time where appoint_date = ? and appoint_time > ?";
 			
 			String selectSql = Schedule.SELECT_AVAILABLE_APPOINTMENT;
 			
 			PreparedStatement prep = dbConn.prepareStatement(selectSql);
 	        
 			Date date = new java.util.Date();
 			Date time = new java.util.Date();
 			try 
 			{
 				date = Schedule.parseDate(dateStr, DATE_ONLY_FORMAT_TYPE);
 				time = Schedule.parseDate(timeStr, TIME_ONLY_FORMAT_TYPE);
 				java.sql.Date sqlDate = Schedule.convertSQLDate(date);
 				java.sql.Date sqlTime = Schedule.convertSQLDate(time);
 				prep.setDate(1, sqlDate);
 				prep.setDate(2, sqlTime);
 				ResultSet rs = prep.executeQuery();
 				
 				while (rs.next())
 				{
 					 o.put( "id", rs.getString("id") );
 				     o.put( "title", rs.getString("name") );
 				     o.put( "start", date + " "  + rs.getString( "appoint_time" ) );
 				     o.put( "end",  date + " "  + rs.getString("end_time") );
 				     o.put( "allDay", false );
 				   
 				}
 				
 			} catch (ParseException e) 
 			{
 				// TODO Auto-generated catch block
 				return WebUtils.buildErrorJson( "Problem on parsing the date " + dateStr  + " for next available appointment ");
 				
 			} 
 			catch (JSONException e) {
 				// TODO Auto-generated catch block
 				return WebUtils.buildErrorJson( "Problem on creatig JSON data for ext available appointment " );
 				
 			}
 			
 			int rtn = 0;
 			int patient_id = Integer.parseInt(patientId);
 			String err_mess = "Could not get next available appointment for patient  " + patient_id;
 			
 			rtn = dbConn.psExecuteUpdate(selectSql, err_mess , patient_id, date);	
 			
 			if (rtn < 0 )
 				return WebUtils.buildErrorJson( "Problem on getting appointment data from database." );
 			
 		}
 		finally
 		{
 		
 		}
 		return o;
 	}
 	public static JSONObject deleteAppointment( String patientId, String dateTime) throws SQLException
 	{
 		JSONObject ret = new JSONObject();
 		setConnection();
 
 		try 
 		{
 			String deleteQuery = Schedule.DELETE_APPOINTMENT;
 			int rtn = 0;
 			int patient_id = Integer.parseInt(patientId);
 			String err_mess = "Could not delete the widgets for patient  " + patient_id;
 			
 			rtn = dbConn.psExecuteUpdate(deleteQuery, err_mess , patient_id, dateTime);	
 			
 			if (rtn < 0 )
 				return WebUtils.buildErrorJson( "Problem on deleting widget data from database ." );
 			
 		}
 		finally
 		{
 		
 		}
 		return ret;
 	}
 
 	public int getPatientId() {
 		return patientId;
 	}
 
 	public void setPatientId(int patientId) {
 		this.patientId = patientId;
 	}
 	public String getStartTime() {
 		return startTime;
 	}
 	public void setStartTime(String startTime) {
 		this.startTime = startTime;
 	}
 	public String getEndTime() {
 		return endTime;
 	}
 	public void setEndTime(String endTime) {
 		this.endTime = endTime;
 	}
 	public String getDuration() {
 		return duration;
 	}
 	public void setDuration(String duration) {
 		this.duration = duration;
 	}
 	public String getStartDate() {
 		return startDate;
 	}
 	public void setStartDate(String startDate) {
 		this.startDate = startDate;
 	}
 
 	public static java.sql.Date convertSQLDate(Date date) throws ParseException
 	{
 		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
 		return sqlDate;
 	}
 	
	public static java.util.Date addDuration(Date startTime, String durationStr) throws ParseException
 	{
 		GregorianCalendar cal = new GregorianCalendar();
 		cal.setTime(startTime);
 		int duration = Integer.parseInt(durationStr);
 		cal.add(Calendar.MINUTE, duration);
		System.out.println("Schedule addDuration calendar " + cal.toString());
		   
		return cal.getTime();
 	}
 	
 	public static Date parseDate(String dateStr, int dateFormat) throws ParseException
 	{
 		if (dateFormat == DATE_ONLY_FORMAT_TYPE)
 		{
 			SimpleDateFormat format = new SimpleDateFormat(Schedule.DATE_FORMAT);
 			Date date = format.parse(dateStr);
 			return date;
 		}
 		else if (dateFormat == TIME_ONLY_FORMAT_TYPE)
 		{
 			SimpleDateFormat format = new SimpleDateFormat(Schedule.TIME_FORMAT);
 			Date date = format.parse(dateStr);
 			return date;
 		}
 		else
 		{
 			SimpleDateFormat format = new SimpleDateFormat(Schedule.DATE_TIME_FORMAT);
 			Date date = format.parse(dateStr);
 			return date;
 		}
 	}
 }
