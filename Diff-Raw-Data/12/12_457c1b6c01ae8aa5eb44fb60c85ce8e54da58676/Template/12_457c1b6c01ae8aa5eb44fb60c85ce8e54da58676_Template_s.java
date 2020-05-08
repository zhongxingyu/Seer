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
 import org.mitre.medcafe.util.DatabaseUtility;
 import org.mitre.medcafe.util.WebUtils;
 import org.restlet.ext.json.JsonRepresentation;
 
 /**
  *  Representation of the text data
  *  @author: Gail Hamilton
  */
 public class Template extends Widget
 {
 	public final static String KEY = Template.class.getName();
 	public final static Logger log = Logger.getLogger( KEY );
 	// static{log.setLevel(Level.FINER);}
 
 	//Parameters that are common to all Widgets
 	private int templateId =0;
 	
	public static final String SELECT_TEMPLATE_WIDGET_PARAMS = "SELECT widget_id, param, value from template_widget_params where username = ? and template_id = ? and widget_id =? ";
	public static final String SELECT_TEMPLATE_WIDGETS = "SELECT id, widget_id, param, value from template_widget_params where username = ? and template_id = ? ORDER BY widget_id ";
 	public static final String INSERT_TEMPLATE_WIDGETS = "INSERT INTO template_widget_params  ( widget_id, template_id, patient_id, username, param, value ) values (?,?,-1,?,?,?) ";
 	public static final String DELETE_TEMPLATE_WIDGETS = "DELETE FROM template_widget_params where ( template_id = ?  AND username=?) ";
 
 	public static final String CREATE_TEMPLATES = "INSERT INTO template_widget_params (template_id, widget_id,patient_id,username, param, value)  ( SELECT ?, widget_id, patient_id ,username, param, value from widget_params where username = ? and patient_id = ? ) ";
 	public static final String CREATE_TEMPLATE = "INSERT INTO template (name,creator, description)  values (?,?,?) ";
 	
	public static final String COPY_TEMPLATES = "INSERT INTO widget_params (widget_id,patient_id,username, param, value)  ( SELECT widget_id, ? ,username, param, value from template_widget_params where username = ? and template_id = ? ) ";
 	public static final String SELECT_TEMPLATE = "SELECT template_id, name from template where name = ? ";
 	public static final String SELECT_TEMPLATES = "SELECT template_id, name from template ";
 	public static final String INSERT_TEMPLATES = "INSERT INTO template  ( widget_id, template_id, patient_id, username, param, value ) values (?,?,-1,?,?,?) ";
 	public static final String DELETE_TEMPLATES = "DELETE FROM template where ( template_id = ?  AND username=?) ";
 	
 	public Template()
 	{
 		super();
 
 	}
 
 
 	public JSONObject toJSON() throws JSONException
 	{
 		 JSONObject o = super.toJSON();
 		 o.put(Template.ID, this.getTemplateId());		
 		 return o;
 	}
 
 	public static JSONObject getTemplates(String userName) throws SQLException 
 	{
 		JSONObject o = new JSONObject();
 		ArrayList<String> templateList = new ArrayList<String>();
 		
 		DbConnection dbConn = null;
 		PreparedStatement prep = null;
 		
 		try
 		{
 			dbConn = setConnection();
 			
 			String selectQuery = Template.SELECT_TEMPLATES;
 			prep= dbConn.prepareStatement(selectQuery);	
 			
 			ResultSet rs = prep.executeQuery();
 			while (rs.next())
 			{
 				String templateName = rs.getString(2);
 				templateList.add(templateName);
 				
 			}
 			o.put("Templates", templateList);
 			
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			return WebUtils.buildErrorJson( "Problem on retrieving template from database ." );
 		
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			return WebUtils.buildErrorJson( "Problem on retrieving template from database ." );
 			
 		}
 		finally
 		{
 
 			closeConnections(dbConn, prep, null);
 			dbConn = null;
 			prep = null;
 		}
 		return o;
 	}
 	
 	private static int getTemplateId(DbConnection dbConn, PreparedStatement prep, String templateName) throws SQLException
 	{
 		String selectQuery = Template.SELECT_TEMPLATE;
 		prep= dbConn.prepareStatement(selectQuery);	
 		prep.setString(1, templateName);
 		ResultSet rs = prep.executeQuery();
 		int template_id = 0;
 		if (rs.next())
 		{
 			template_id = rs.getInt(1);
 		}
 		else
 		{
 			throw new SQLException("Template SQL Error: Could not find template by name " + templateName);
 		}
 		return template_id;
 	}
 	
 	public static JSONObject copyToTemplate(String patientId, String userName, String templateName, String description) throws SQLException 
 	{
 		//Copy the template table data into widget table 
 		System.out.println("Template : copyTemplate about to execute copy  " );
 
 		JSONObject o = new JSONObject();
 		DbConnection dbConn = null;
 		PreparedStatement prep = null;
 		/**
 		key id value 1
 		patient_id value 1
 		server value http://127.0.0.1
 		clickUrl value http://127.0.0.1:8080
 		repository value OurVista
 		type value images
 		location value center
 		tab_num value 1
 		**/
 		try
 		{
 			dbConn = setConnection();
 			
 			if (description == null)
 				description = "";
 			
 			int patient_id = Integer.parseInt(patientId);
 			String err_mess = "Could not create the template using  " + patient_id;
 			//INSERT INTO widget_params (widget_id,patient_id,username, param, value)  ( SELECT widget_id, ? ,username, param, value from template_widget_params where username = ? and template_id = ? ) ";
 			
 			String createTemplate = Template.CREATE_TEMPLATE;
 			prep= dbConn.prepareStatement(createTemplate);
 			prep.setString(1, templateName);
 			prep.setString(2, userName);
 			prep.setString(3, description);
 			int rtn = prep.executeUpdate();
 			if (rtn < 0 )
 			{
 				System.out.println("Template : copyToTemplate " + err_mess);
 
 				closeConnections(dbConn, null, null);
 				dbConn = null;
 				return WebUtils.buildErrorJson( "Problem on creating template " + templateName  );
 			}
 			String copyQuery = Template.CREATE_TEMPLATES;
 			int template_id = getTemplateId(dbConn, prep, templateName);
 			
 			prep= dbConn.prepareStatement(copyQuery);
 			prep.setInt(1, template_id);
 			prep.setString(2, userName);
 			prep.setInt(3, patient_id);
 			
 			System.out.println("Template : copyToTemplate about to execute batch copy " + prep.toString());
 
 			rtn = prep.executeUpdate();
 			if (rtn < 0 )
 			{
 				System.out.println("Template : copyToTemplate template widgets " + err_mess);
 
 				closeConnections(dbConn, null, null);
 				dbConn = null;
 				return WebUtils.buildErrorJson( "Problem on copying template widget data from database ." );
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			return WebUtils.buildErrorJson( "Problem on copying template widget data from database ." );
 			
 		}
 		finally
 		{
 
 			closeConnections(dbConn, prep, null);
 			dbConn = null;
 			prep = null;
 		}
 		return o;
 	}
 	
 	public static JSONObject copyTemplate(String templateId, String patientId, String userName) throws SQLException 
 	{
 		//Copy the template table data into widget table 
 		System.out.println("Template : copyTemplate about to execute copy  " );
 
 		JSONObject o = new JSONObject();
 		DbConnection dbConn = null;
 		PreparedStatement prep = null;
 		/**
 		key id value 1
 		patient_id value 1
 		server value http://127.0.0.1
 		clickUrl value http://127.0.0.1:8080
 		repository value OurVista
 		type value images
 		location value center
 		tab_num value 1
 		**/
 		try
 		{
 			dbConn = setConnection();
 			
 
 			int patient_id = Integer.parseInt(patientId);
 			String err_mess = "Could not update the widgets for patient  " + patient_id;
 			//INSERT INTO widget_params (widget_id,patient_id,username, param, value)  ( SELECT widget_id, ? ,username, param, value from template_widget_params where username = ? and template_id = ? ) ";
 			String copyQuery = Template.COPY_TEMPLATES;
 			int template_id = getTemplateId(dbConn, prep, templateId);
 			
 			prep= dbConn.prepareStatement(copyQuery);
 			prep.setInt(1, patient_id);
 			prep.setString(2, userName);
 			prep.setInt(3, template_id);
 			
 			System.out.println("Template : copyTemplate about to execute batch copy " + prep.toString());
 
 			int rtn = prep.executeUpdate();
 			if (rtn < 0 )
 			{
 				closeConnections(dbConn, null, null);
 				dbConn = null;
 				return WebUtils.buildErrorJson( "Problem on copying template widget data from database ." );
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			return WebUtils.buildErrorJson( "Problem on copying template widget data from database ." );
 			
 		}
 		finally
 		{
 
 			closeConnections(dbConn, prep, null);
 			dbConn = null;
 			prep = null;
 		}
 		return o;
 	}
 	
 	public static JSONObject retrieveTemplateCopy(String templateId, String patientId, String userName) throws SQLException
 	{
 		try
 		{
 			//Delete any existing widgets from widget table
 			deleteWidgets(patientId,userName);
 		
 			//Copy the template table data into widget table 
 			JSONObject successObj = copyTemplate(templateId, patientId, userName);
 			
 			//Retrieve the new widget list
 			//JSONObject widgets = listWidgets( userName, patientId);
 			
 			//Retrieve the widgets
 			return successObj;
 		} 
 		catch (SQLException e) {
 			// TODO Auto-generated catch block 
 			return WebUtils.buildErrorJson( "Problem on copying template widget data from database ."  + e.getMessage());
 			
 		}
 		finally
 		{
 
 			
 		}
 	}
 	
 	public static JSONObject retrieveTemplate(String templateId, String patientId, String userName)
 	{
 		//Select the template Object
 		//Copy the template table data into widget table 
 		//Retrieve the widgets
 		return null;
 	}
 	
 	public static JSONObject deleteTemplateWidgets( String patientId, String userName) throws SQLException
 	{
 		JSONObject ret = new JSONObject();
 
 		DbConnection dbConn = null;
 		/**
 		key id value 1
 		patient_id value 1
 		server value http://127.0.0.1
 		clickUrl value http://127.0.0.1:8080
 		repository value OurVista
 		type value images
 		location value center
 		tab_num value 1
 		**/
 		try
 		{
 			dbConn = setConnection();
 			String deleteQuery = Template.DELETE_WIDGETS;
 			int rtn = 0;
 			int patient_id = Integer.parseInt(patientId);
 			String err_mess = "Could not delete the widgets for patient  " + patient_id;
 
 			rtn = dbConn.psExecuteUpdate(deleteQuery, err_mess , patient_id, userName);
 
 			if (rtn < 0 )
 			{
 				closeConnections(dbConn, null, null);
 				dbConn = null;
 				return WebUtils.buildErrorJson( "Problem on deleting widget data from database ." );
 			}
 		}
 		finally
 		{
 		closeConnections(dbConn, null, null);
 		dbConn = null;
 		}
 
 		return ret;
 	}
 
 	public static JSONObject saveTemplateWidgets( String userName, JSONObject widgetJSON) throws SQLException
 	{
 		System.out.println("Widget : saveWidgets about to execute for JSONObject  " + widgetJSON.toString());
 
 		JSONObject ret = new JSONObject();
 		DbConnection dbConn = null;
       PreparedStatement prep = null;
 		/**
 		key id value 1
 		patient_id value 1
 		server value http://127.0.0.1
 		clickUrl value http://127.0.0.1:8080
 		repository value OurVista
 		type value images
 		location value center
 		tab_num value 1
 		**/
 		try
 		{
 			dbConn = setConnection();
 			String idStr = widgetJSON.getString(Template.WIDGET_ID);
 			int id = Integer.parseInt(idStr);
 
 			String patient_idStr = widgetJSON.getString(Template.ID);
 			int patient_id = Integer.parseInt(patient_idStr);
 			String err_mess = "Could not update the widgets for patient  " + patient_id;
 			//public static final String DELETE_WIDGETS = "DELETE FROM widget_params where ( patient_id=?, username=?) ";
 
 			String remove = widgetJSON.getString(Template.REMOVE);
 
 			//This widget has been marked for removal
 			if (remove.equals(Template.TRUE))
 				return ret;
 
 			String updateQuery = Template.INSERT_WIDGETS;
 
 			prep= dbConn.prepareStatement(updateQuery);
 
 			//INSERT_WIDGETS = "INSERT INTO widget_params  ( widget_id, patient_id, username, param, value ) values (?,?,?,?,?) ";
 
 			Iterator iter = widgetJSON.keys();
 			while (iter.hasNext())
 			{
 				String key = iter.next().toString();
 				//Skip the key values
 				if (key.equals(Template.WIDGET_ID) || key.equals(Template.ID) )
 					continue;
 				String value = widgetJSON.getString(key);
 				//System.out.println("Widget : About to update id " + id + " patient_id " + patient_id + " userName " + userName + " key " + key + " value " + value);
 				//dbConn.psExecuteUpdate(updateQuery, err_mess , id, patient_id, userName, key, value);
 				prep.setInt(1, id);
 				prep.setInt(2, patient_id);
 				prep.setString(3, userName);
 				prep.setString(4, key);
 				prep.setString(5, value);
 				prep.addBatch();
 
 			}
 			System.out.println("Widget : saveWidgets about to execute batch " + prep.toString());
 
 			prep.executeBatch();
 		}
 		catch (JSONException e) {
 
 
 			// TODO Auto-generated catch block
 			System.out.println("Widget : saveWidgets Problem on updating widget data from database ." + e.getMessage());
 
 			return WebUtils.buildErrorJson( "Problem on updating widget data from database ." + e.getMessage());
 
 		}
 		finally
 		{
 
 			closeConnections(dbConn, prep, null);
 			dbConn = null;
 			prep = null;
 		}
 
 		return ret;
 	}
 
 	
 
 	public static HashMap<String, Template> retrieveTemplateWidgets(String userName, String templateId, String patientId) throws SQLException
 	{
 		 HashMap<String, Template> widgetList = new HashMap<String, Template>();
 	//	 DbConnection dbConn = null;
 		 
 		 int patId = Integer.valueOf(patientId);
 		
 		 DbConnection dbConn = null;
 		 ResultSet rs = null;
 		 PreparedStatement prep = null;
 		 try
 		 {
 		   log.severe("Connection being set . . .  waiting");
 		   dbConn = setConnection();
 		   log.severe("Connection is set, preparing statement");
 			
 		   int tempId = getTemplateId(dbConn, prep, templateId);
 			
 		   	prep= dbConn.prepareStatement(Template.SELECT_TEMPLATE_WIDGETS);
			prep.setString(1, userName);
			prep.setInt(2, tempId);
 
 			System.out.println("Widget : retrieveWidgets : query " + prep.toString());
 
 			rs =  prep.executeQuery();
 			int lastId = 0;
 			//This lists all the paramaters - gather together into a HashMap - keyed on id
 			Template widget = new Template();
 			HashMap<String, String> params = new HashMap<String, String>() ;
 
 			while (rs.next())
 			{
 				int widgetId = rs.getInt("widget_id");
 				int serial_id = rs.getInt("id");
 
 				if (lastId != widgetId)
 				{
 					//Create new Widget
 					widget = new Template();
 					widget.setPatientId(patId);
 					widget.setId(widgetId);
 					params = new HashMap<String, String>();
 					widget.setParams(params);
 					widgetList.put(widgetId + "", widget);
 
 				}
 				
 				String param = rs.getString("param");
 				String value = rs.getString("value");
 				
 				//System.out.println("Widget ID: " + widgetId + " param: " + param + " value: " + value);
 				
 				if (param.equals(Template.TAB_ORDER))
 				{
 					int tabOrdInt = Integer.parseInt(value);
 					widget.setTabOrder( tabOrdInt );
 				}
 				else if (param.equals(Template.TYPE))
 					widget.setType(value);
 				else if (param.equals(Template.LOCATION))
 					widget.setLocation(value);
 			//	else if (param.equals(Widget.REPOSITORY))
 			//		widget.setRepository(value);
 				else if (param.equals(Template.NAME))
 					widget.setName(value);
 				else if (param.equals(Template.SERVER))
 					widget.setServer(value);
 				else if (param.equals(Template.REP_PATIENT_ID))
 					widget.setRepPatientId(value);
 				else if (param.equals(Template.COLUMN))
 					widget.setColumn(value);
 				else if (param.equals(Template.TAB_NUMBER))
 					widget.setTab_num(value);
 				else if (param.equals(Template.INETTUTS))
 					widget.setINettuts(value);
 				else if (param.equals(Template.COLOR_NUM))
 					widget.setColorNum(value);
 				else if (param.equals(Template.LABEL))
 					widget.setLabel(value);
 				else if (param.equals(Template.COLLAPSED))
 					widget.setCollapsed(value);
 				else if (param.equals(Template.IMAGE))
 					widget.setImage(value);
 				else if (param.equals(Template.IN_FOCUS))
 					widget.setInFocus(value);
 				else
 					params.put(param, value);
 
 				lastId = widgetId;
 			}
 
 		 }
 		 catch (SQLException e)
 		 {
 		   log.severe("Databse Connection ERROR: Can't make connection " + e.getMessage());
 
 			throw e;
 		 }
 		 finally
 		 {
 
 		 	closeConnections(dbConn, prep, rs);
 		 	rs = null;
 		 	prep = null;
 		 	dbConn = null;
 		 }
 		 return widgetList;
 
 	}
 
 	public static JSONObject listTemplateWidgets(String userName, String templateId, String patientId) throws SQLException
 	{
 		JSONObject ret = new JSONObject();
 		try
 		{
 		
 
 			HashMap<String, Template> widgetList = retrieveTemplateWidgets(userName, templateId, patientId);
 			//split out the tabs
 
 			HashMap<String, MedCafeComponent> compList = MedCafeComponent.getComponentHash();
 
 			//Sort by the tab_order
 			TreeMap<Integer, Template> sortedWidgets = new TreeMap<Integer, Template>();
 			for (Template widget: widgetList.values())
 				sortedWidgets.put(widget.getTabOrder(), widget);
 			for (Template widget: sortedWidgets.values())
 			{
 				JSONObject widgetJSON = widget.toJSON();
 				if( widget.getType().equals("tab") )
                     ret.append("tabs", widgetJSON);
                 else
                 {
 						  MedCafeComponent comp = compList.get(widget.getName());
 						  if (comp!= null) {
 						  widgetJSON.put(MedCafeComponent.SCRIPT, comp.getScript());
 						  widgetJSON.put(MedCafeComponent.SCRIPT_FILE, comp.getScriptFile());
 						  widgetJSON.put(MedCafeComponent.TEMPLATE, comp.getTemplate());
 						  widgetJSON.put(MedCafeComponent.CLICK_URL, comp.getClickUrl());
 						  widgetJSON.put(MedCafeComponent.JSON_PROCESS, comp.getJsonProcess());
 						  widgetJSON.put(MedCafeComponent.INETTUTS, comp.getINettuts());
 						  widgetJSON.put(MedCafeComponent.PARAMS, comp.getParams());
 						  widgetJSON.put(MedCafeComponent.CACHE_KEY, comp.getCacheKey());
 						  }                		
                     ret.append("widgets", widgetJSON);
 					 }
 			}
 		}
 		catch (SQLException e)
 		{
 			return WebUtils.buildErrorJson( "Problem on selecting data from database ." + e.getMessage());
 		}
 		catch (JSONException e)
 		{
 			return WebUtils.buildErrorJson( "Problem on building JSON Data ." + e.getMessage());
 		}
 
 		return ret;
 	}
 
 
 	public int getTemplateId() {
 		return templateId;
 	}
 
 
 
 	public void setTemplateId(int templateId) {
 		this.templateId = templateId;
 	}
 
 
 
 }
