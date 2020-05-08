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
 import java.text.DateFormat;
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
 public class MedCafeFile
 {
 	public final static String KEY = MedCafeFile.class.getName();
 	public final static Logger log = Logger.getLogger( KEY );
 	static{log.setLevel(Level.FINER);}
 
 	//Parameters that are common to all MedCafe Files
 	private int patientId =0;
 	private int id =0;
 	private String thumbnail ="";
 	private String title ="";
 	private String fileUrl ="";
 	private Date fileDate =new Date();
 	
 	public static int levenshtienVal = 5;
 	public static final String PATIENT_ID = "patient_id";
 	public static final String TITLE = "title";
 	public static final String THUMBNAIL = "thumbnail";
 	public static final String DATE = "file_date";
 	public static final String FILENAME = "filename";
 	
 	public static final String SELECT_FILES = "SELECT id, filename, thumbnail, title, file_date from file where patient_id = ?  ";
 	public static final String SELECT_CATEGORY_FILES = "SELECT file.id, filename, thumbnail, title, file_date, category from file, category, file_category " +
 													  	 " where file.id = file_category.file_id and file_category.category_id = category.id " +
 														" and  patient_id = ? and category IN (<%category%>) ";
 	public static final String SELECT_CATEGORY_FILES_SOUNDEX = "SELECT file.id, filename, thumbnail, title, file_date, category from file, category, file_category " +
  	 														" where file.id = file_category.file_id and file_category.category_id = category.id " +
  	 														" and patient_id = ? and SOUNDEX(category) IN (<%category%>) ";
 
 	public static final String SELECT_CATEGORY_FILES_LEVEN = "SELECT file.id, filename, thumbnail, title, file_date, category from file, category, file_category " +
 															" where file.id = file_category.file_id and file_category.category_id = category.id " +
 															" and patient_id = ? and (<%category%>) ";
 
 	public static final String SORT_BY = " ORDER BY file_date DESC ";
 	
 	public static final String INSERT_FILES= "INSERT INTO widget_params  ( widget_id, patient_id, username, param, value ) values (?,?,?,?,?) ";
 	public static final String DELETE_FILES = "DELETE FROM widget_params where ( patient_id=? AND username=?) ";
 	public static final String DATE_FORMAT = "yyyy-MM-dd";
 	public static final String SQL_DATE_FORMAT = "YYYY-MM-DD";
 	
 	private static DbConnection dbConn = null;
 	public MedCafeFile()	
 	{
 		super();
 	
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
 		dbConn = null;
 	}
 	
 	public JSONObject toJSON() throws JSONException
 	{
 		 JSONObject o = new JSONObject();
 		 o.put(MedCafeFile.PATIENT_ID, this.getPatientId());
 		 		 
 		 return o;
 		 
 	}
 	
 	public static JSONObject deleteFiles( String patientId, String userName) throws SQLException
 	{
 		JSONObject ret = new JSONObject();
 		setConnection();
 		
 		try 
 		{
 			String deleteQuery = MedCafeFile.DELETE_FILES;
 			int rtn = 0;
 			int patient_id = Integer.parseInt(patientId);
 			String err_mess = "Could not delete the FILES for patient  " + patient_id;
 			
 			rtn = dbConn.psExecuteUpdate(deleteQuery, err_mess , patient_id, userName);	
 			
 			if (rtn < 0 )
 				return WebUtils.buildErrorJson( "Problem on deleting file data from database ." );
 			
 		}
 		finally
 		{
 		
 		}
 		return ret;
 	}
 	
 	public static JSONObject saveFiles( String userName, JSONObject widgetJSON) throws SQLException
 	{
 		JSONObject ret = new JSONObject();
 		setConnection();
 		
 		try 
 		{
 			
 			String patient_idStr = widgetJSON.getString(MedCafeFile.PATIENT_ID);
 			int patient_id = Integer.parseInt(patient_idStr);
 			String err_mess = "Could not update the files for patient  " + patient_id;
 			//public static final String DELETE_WIDGETS = "DELETE FROM widget_params where ( patient_id=?, username=?) ";
 			
 			String updateQuery = MedCafeFile.INSERT_FILES;
 			
 			PreparedStatement prep= dbConn.prepareStatement(updateQuery);
 			
 			//INSERT_WIDGETS = "INSERT INTO widget_params  ( widget_id, patient_id, username, param, value ) values (?,?,?,?,?) ";
 		
 			Iterator iter = widgetJSON.keys();
 			while (iter.hasNext())
 			{
 				String key = iter.next().toString();
 				//Skip the key values
 				String value = widgetJSON.getString(key);		
 				//System.out.println("Widget : About to update id " + id + " patient_id " + patient_id + " userName " + userName + " key " + key + " value " + value);
 				//dbConn.psExecuteUpdate(updateQuery, err_mess , id, patient_id, userName, key, value);	
 				//prep.setInt(1, id);
 				prep.setInt(2, patient_id);
 				prep.setString(3, userName);
 				
 				prep.addBatch();
 				
 			}
 			prep.executeBatch();
 		}
 		catch (JSONException e) {
 			// TODO Auto-generated catch block
 			return WebUtils.buildErrorJson( "Problem on updating widget data from database ." + e.getMessage());
 			
 		}
 		finally
 		{
 		
 		}
 		return ret;
 	}
 	
 
 	public static ArrayList<MedCafeFile> retrieveFiles(String userName, String patientId, String startDateStr, String endDateStr, String categoryList, boolean levenshtein) throws SQLException, ParseException
 	{
 		ArrayList<MedCafeFile> fileList = new ArrayList<MedCafeFile>();
 		
 		try 
 		 {
 			int patId = Integer.valueOf(patientId);
 			if (dbConn == null)
 				dbConn= new DbConnection();
 	
 			DateFormat df = new SimpleDateFormat(DATE_FORMAT);
 			Date startDate = null;
 			Date endDate = null;
 			String sqlQuery = MedCafeFile.SELECT_FILES;
 			int startDatePos = 1, endDatePos = 1;
 				
 			
 			if (categoryList != null)
 			{
 				String[] categoryArr = categoryList.split(",");
 				StringBuffer catBuf = new StringBuffer();
 				
 				
 				if (levenshtein)
 				{
 					 sqlQuery = MedCafeFile.SELECT_CATEGORY_FILES_LEVEN;
 					 String OR_STR = "";
 					 for (String category: categoryArr)
 					 {
 						 catBuf.append( OR_STR + " (levenshtein(category, '" + category + "') < " + levenshtienVal + " ) ");
						 			
 					 }
 					 sqlQuery = sqlQuery.replaceAll("<%category%>", catBuf.toString());
 				}
 				else
 				{	
 					 String comma = "";
 					 sqlQuery = MedCafeFile.SELECT_CATEGORY_FILES;
 					 
 					 for (String category: categoryArr)
 					 {
 						 catBuf.append(comma + "'" + category + "'");
 						 comma = ",";
 					 }
 					 sqlQuery = sqlQuery.replaceAll("<%category%>", catBuf.toString());
 				}
 			} 
 	
 			if (startDateStr != null)
 			{
 					 sqlQuery = sqlQuery + " AND file_date > ? ";			 
 					 startDate = df.parse(startDateStr);
 					 startDatePos = 2; 
 			}
 				 
 			if (endDateStr != null)
 			{
 					 endDate = df.parse(endDateStr);
 					 sqlQuery = sqlQuery + " AND file_date < ? ";
 					 endDatePos = startDatePos + 1;
 			}
 				 
 			sqlQuery = sqlQuery + SORT_BY;
 			
 			System.out.println("MedCafeFile JSON Files retrieveFiles sql " + sqlQuery);
             
 			PreparedStatement prep= dbConn.prepareStatement(sqlQuery);
 					
 			if (startDate != null)
 			{
 					 java.sql.Date sqlStartDate = new java.sql.Date(startDate.getTime());			 
 					 prep.setDate(startDatePos, sqlStartDate);
 			}
 				 
 			if (endDate != null)
 			{
 					 java.sql.Date sqlEndDate = new java.sql.Date(endDate.getTime());
 					 prep.setDate(endDatePos, sqlEndDate);
 			}
 
 			prep.setInt(1, patId);
 			ResultSet rs =  prep.executeQuery();
 
			System.out.println("MedCafeFile JSON Files retrieveFiles sql " + prep.toString());
            
 			//This lists all the paramaters - gather together into a HashMap - keyed on id
 			MedCafeFile file = new MedCafeFile();
 			while (rs.next())
 			{
 				
 				String thumbnail = rs.getString(MedCafeFile.THUMBNAIL);
 				String title = rs.getString(MedCafeFile.TITLE);
 				String fileUrl = rs.getString(MedCafeFile.FILENAME);
 				int id = rs.getInt("id");
 				Date date = rs.getDate(MedCafeFile.DATE);
 				
 				file = new MedCafeFile();		
 				file.setPatientId(patId);
 				file.setId(id);
 				file.setFileDate(date);
 				file.setThumbnail(thumbnail);
 				file.setTitle(title);	
 				file.setFileUrl(fileUrl);
 				
 				fileList.add( file);				
 			}
 
 		 }
 		 catch (SQLException e) 
 		 {
 			//dbConn.close();
 			throw e;
 		 } 
 		 catch (ParseException e1) {
 			// TODO Auto-generated catch block
 			 
 			 throw e1;
 		 }
 		 finally
 		 {
 			 
 			 //dbConn.close();
 		 }
 		 return fileList;
 		
 	}
 
 
 	public int getPatientId() {
 		return patientId;
 	}
 
 	public void setPatientId(int patientId) {
 		this.patientId = patientId;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getThumbnail() {
 		return thumbnail;
 	}
 
 	public void setThumbnail(String thumbnail) {
 		this.thumbnail = thumbnail;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public Date getFileDate() {
 		return fileDate;
 	}
 
 	public void setFileDate(Date fileDate) {
 		this.fileDate = fileDate;
 	}
 
 	public String getFileUrl() {
 		return fileUrl;
 	}
 
 	public void setFileUrl(String fileUrl) {
 		this.fileUrl = fileUrl;
 	}
 
 	public int getLevenshtienVal() {
 		return levenshtienVal;
 	}
 
 	public void setLevenshtienVal(int levenshtienVal) {
 		this.levenshtienVal = levenshtienVal;
 	}
 
 	
 }
