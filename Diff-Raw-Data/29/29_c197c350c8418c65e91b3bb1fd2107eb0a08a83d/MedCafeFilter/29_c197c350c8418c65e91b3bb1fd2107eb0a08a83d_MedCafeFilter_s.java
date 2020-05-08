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
 import org.mitre.medcafe.util.DbConnection;
 import org.mitre.medcafe.util.WebUtils;
 
 /**
  *  Representation of the text data
  *  @author: Gail Hamilton
  */
 public class MedCafeFilter
 {
 	public final static String KEY = MedCafeFilter.class.getName();
 	public final static Logger log = Logger.getLogger( KEY );
 	// static{log.setLevel(Level.FINER);}
 
 	//Parameters that are common to all Widgets
 	private int patientId =0;
 	private String startDate ="";
 	private String endDate ="";
 	private ArrayList<String> categories = new ArrayList<String>();
 	
 	public static final String ID = "patient_id";
 	public static final String START_DATE= "start_date";
 	public static final String END_DATE = "end_date";
 	public static final String DATE_FORMAT = "MM/dd/yyyy";
 	public static final String USER = "username";
 	public static final String CATEGORY = "filter";
 
 	public MedCafeFilter()
 	{
 		super();
 
 	}
 	
 	public JSONObject toJSON() throws JSONException
 	{
 		 JSONObject o = new JSONObject();
 		 o.put(MedCafeFilter.ID, this.getPatientId());
 		 o.put(MedCafeFilter.START_DATE, this.getStartDate());
 		 o.put(MedCafeFilter.END_DATE, this.getEndDate());
 		 o.put(MedCafeFilter.CATEGORY, this.catToString());
 				
 		 return o;
 
 	}
 
 	public String catToString(){
 		
 		StringBuffer strBuf = new StringBuffer();
		
 		for (String cat: this.getCategories())
 		{
			strBuf.append(cat + ",");
 		}
 		return strBuf.toString();
 		
 	}
 	public int getPatientId() {
 		return patientId;
 	}
 
 	public void setPatientId(int patientId) {
 		this.patientId = patientId;
 	}
 
 	public String getEndDate() {
 		return endDate;
 	}
 
 	public void setEndDate(String endDate) {
 		this.endDate = endDate;
 	}
 
 	public ArrayList<String> getCategories() {
 		return categories;
 	}
 
 	public void setCategories(ArrayList<String> categories) {
 		this.categories = categories;
 	}
 
 	public void addCategories(String category) {
		categories.add(category);
 	}
 
 	public void removeCategories(String category) {
 		categories.remove(category);
 	}
 	
 	public void clearCategories(String category) {
 		categories.clear();
 		
 	}
 	public String getStartDate() {
 		return startDate;
 	}
 	public void setStartDate(String startDate) {
 		this.startDate = startDate;
 	}
 
 }
