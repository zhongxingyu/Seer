 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.atlas.impl;
 
 import org.openmrs.module.Module;
 import org.openmrs.module.ModuleFactory;
 import org.openmrs.module.atlas.AtlasData;
 import org.openmrs.module.atlas.AtlasService;
 import org.openmrs.module.atlas.AtlasConstants;
 import org.openmrs.module.atlas.ImplementationType;
 import org.openmrs.module.atlas.PostAtlasDataQueueTask;
 import org.openmrs.scheduler.SchedulerException;
 import org.openmrs.scheduler.Task;
 import org.openmrs.scheduler.TaskDefinition;
 import org.openmrs.util.OpenmrsConstants;
 import org.openmrs.module.atlas.db.StatisticsDAO;
 
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.UUID;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.GlobalProperty;
 import org.openmrs.OpenmrsData;
 import org.openmrs.api.APIException;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.context.Context;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.Console;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.HashMap;
 
 import org.openmrs.GlobalProperty;
 import org.openmrs.api.APIException;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.context.Context;
 import org.openmrs.scheduler.SchedulerService;
 import org.openmrs.util.DatabaseUpdater.OpenMRSChangeSet;
 import org.openmrs.util.OpenmrsConstants;
 
 /**
  * AtlasService Implementation
  */
 public class AtlasServiceImpl implements AtlasService {
 	
 	private final static Long DEFAULT_ATLAS_DATA_TASK_INTERVAL = 60l * 60 * 24 * 7; //7 days (in seconds)
 	
 	private Log log = LogFactory.getLog(this.getClass());
 	
 	private StatisticsDAO dao;
 	
 	private StatisticsDAO getStatisticsDAO() {
 		return dao;
 	}
 	
 	public void setStatisticsDAO(StatisticsDAO dao) {
 		this.dao = dao;
 	}
 	
 	/**
      * 
      */
 	public AtlasServiceImpl() {
 	}
 	
 	/**
 	 * @see org.openmrs.module.atlas.AtlasService#getAtlasData()
 	 */
 	@Override
 	public AtlasData getAtlasData() throws APIException {
 		AdministrationService svc = null;
 		AtlasData atlasData = new AtlasData();
 		try {
 			svc = Context.getAdministrationService();
 			String globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_ID);
 			
 			atlasData.setId(UUID.fromString(globalProperty));
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_WEBSITE)) != null) {
 				atlasData.setWebsite(globalProperty);
 			}
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_NAME)) != null) {
 				atlasData.setName(globalProperty);
 			}
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_IMPLEMENTATION_TYPE)) != null) {
 				atlasData.setImplementationType(Integer.valueOf(globalProperty));
 			}
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_IMAGE_URL)) != null) {
 				atlasData.setImageURL(globalProperty);
 			}
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_LATITUDE)) != null) {
 				atlasData.setLatitude(Double.valueOf(globalProperty));
 			}
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_LONGITUDE)) != null) {
 				atlasData.setLongitude(Double.valueOf(globalProperty));
 			}
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_ZOOM)) != null) {
 				atlasData.setZoom(Integer.valueOf(globalProperty));
 			}
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_NOTES)) != null) {
 				atlasData.setNotes(globalProperty);
 			}
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_CONTACT_EMAIL_ADDRESS)) != null) {
 				atlasData.setContactEmailAddress(globalProperty);
 			}
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_CONTACT_NAME)) != null) {
 				atlasData.setContactName(globalProperty);
 			}
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_INCLUDE_NUMBER_OF_OBSERVATIONS)) != null) {
 				atlasData.setIncludeNumberOfObservations(Boolean.parseBoolean(globalProperty));
 			}
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_INCLUDE_NUMBER_OF_PATIENTS)) != null) {
 				atlasData.setIncludeNumberOfPatients(Boolean.parseBoolean(globalProperty));
 			}
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_INCLUDE_NUMBER_OF_ENCOUNTERS)) != null) {
 				atlasData.setIncludeNumberOfEncounters(Boolean.parseBoolean(globalProperty));
 			}
 			
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_NUMBER_OF_OBSERVATIONS)) != null) {
 				atlasData.setNumberOfObservations(globalProperty);
 			}
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_NUMBER_OF_PATIENTS)) != null) {
 				atlasData.setNumberOfPatients(globalProperty);
 			}
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_NUMBER_OF_ENCOUNTERS)) != null) {
 				atlasData.setNumberOfEncounters(globalProperty);
 			}
 			
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_INCLUDE_SYSTEM_CONFIGURATION)) != null) {
 				atlasData.setIncludeSystemConfiguration(Boolean.parseBoolean(globalProperty));
 			}
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_MODULE_ENABLED)) != null) {
 				atlasData.setModuleEnabled(Boolean.parseBoolean(globalProperty));
 			}
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_USAGE_DISCLAIMER_ACCEPTED)) != null) {
 				atlasData.setUsageDisclamerAccepted(Boolean.parseBoolean(globalProperty));
 			}
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_IS_DIRTY)) != null) {
 				atlasData.setIsDirty(Boolean.parseBoolean(globalProperty));
 			}
 			
 		}
 		catch (APIException apiEx) {
 			if (log.isErrorEnabled())
 				log.error("Could not get atlas data. Exception:" + apiEx.getMessage());
 		}
 		
 		return atlasData;
 	}
 	
 	/**
 	 * @see org.openmrs.module.atlas.AtlasService#getIsDirty()
 	 */
 	@Override
 	public Boolean getIsDirty() throws APIException {
 		AdministrationService svc = null;
 		AtlasData atlasData = new AtlasData();
 		try {
 			svc = Context.getAdministrationService();
 			String globalProperty;
 			
 			if ((globalProperty = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_IS_DIRTY)) != null) {
 				return Boolean.parseBoolean(globalProperty);
 			} else {
 				return false;
 			}
 		}
 		catch (APIException apiEx) {
 			if (log.isErrorEnabled())
 				log.error("Could get atlasData.isDirty. Exception:" + apiEx.getMessage());
 			return false;
 		}
 	}
 	
 	/**
 	 * @see org.openmrs.module.atlas.AtlasService#setAtlasData(org.openmrs.module.atlas.AtlasData)
 	 */
 	@Override
 	public void setAtlasData(AtlasData data) throws APIException {
 		AdministrationService svc = null;
 		try {
 			svc = Context.getAdministrationService();
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_NAME, data.getName(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_IMPLEMENTATION_TYPE, data.getImplementationType().toString(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_WEBSITE, data.getWebsite(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_IMAGE_URL, data.getImageURL(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_CONTACT_EMAIL_ADDRESS, data.getContactEmailAddress(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_CONTACT_NAME, data.getContactName(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_LATITUDE, data.getLatitude().toString(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_LONGITUDE, data.getLongitude().toString(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_ZOOM, data.getZoom().toString(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_INCLUDE_NUMBER_OF_OBSERVATIONS, data.getIncludeNumberOfObservations().toString(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_INCLUDE_NUMBER_OF_PATIENTS, data.getIncludeNumberOfPatients().toString(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_INCLUDE_NUMBER_OF_ENCOUNTERS, data.getIncludeNumberOfEncounters().toString(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_USAGE_DISCLAIMER_ACCEPTED, data.getUsageDisclamerAccepted().toString(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_INCLUDE_SYSTEM_CONFIGURATION, data.getIncludeSystemConfiguration().toString(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_MODULE_ENABLED, data.getModuleEnabled().toString(), svc);
 			
 			setIsDirty(true);
 		}
 		catch (APIException apiEx) {
 			if (log.isErrorEnabled())
 				log.error("Could not set atlas data. Exception:" + apiEx.getMessage());
 		}
 	}
 	
 	/**
 	 * @see org.openmrs.module.atlas.AtlasService#updateAndGetStatistics()
 	 */
 	@Override
 	public String[] updateAndGetStatistics() throws APIException {
 		AdministrationService svc = null;
 		String[] statsList = new String[3];
 		try {
 			svc = Context.getAdministrationService();
 			//get statistics from global properties
 			GlobalProperty nrOfObsProp = svc.getGlobalPropertyObject(AtlasConstants.GLOBALPROPERTY_NUMBER_OF_OBSERVATIONS);
 			GlobalProperty nrOfEncountersProp = svc
 			        .getGlobalPropertyObject(AtlasConstants.GLOBALPROPERTY_NUMBER_OF_ENCOUNTERS);
 			GlobalProperty nrOfPatientsProp = svc.getGlobalPropertyObject(AtlasConstants.GLOBALPROPERTY_NUMBER_OF_PATIENTS);
 			
 			String nrOfObservations = nrOfObsProp.getPropertyValue();
 			String nrOfEncounters = nrOfEncountersProp.getPropertyValue();
 			String nrOfPatients = nrOfPatientsProp.getPropertyValue();
 			
 			if (nrOfObservations.equals("?") || nrOfEncounters.equals("?") || nrOfPatients.equals("?")) {
 				
 				//get updated statistics
 				nrOfObservations = String.valueOf(getStatisticsDAO().getNumberOfObservations());
 				nrOfEncounters = String.valueOf(getStatisticsDAO().getNumberOfEncounters());
 				nrOfPatients = String.valueOf(getStatisticsDAO().getNumberOfPatients());
 				
 				//update statistics
 				nrOfObsProp.setPropertyValue(nrOfObservations);
 				nrOfEncountersProp.setPropertyValue(nrOfEncounters);
 				nrOfPatientsProp.setPropertyValue(nrOfPatients);
 				
 				svc.saveGlobalProperty(nrOfObsProp);
 				svc.saveGlobalProperty(nrOfEncountersProp);
 				svc.saveGlobalProperty(nrOfPatientsProp);
 			}
 			
 			statsList[0] = String.valueOf(nrOfPatients);
 			statsList[1] = String.valueOf(nrOfEncounters);
 			statsList[2] = String.valueOf(nrOfObservations);
 		}
 		catch (APIException apiEx) {
 			if (log.isErrorEnabled())
 				log.error("Could not set atlas data. Exception:" + apiEx.getMessage());
 		}
 		finally {
 			return statsList;
 		}
 	}
 	
 	/**
 	 * @see org.openmrs.module.atlas.AtlasService#enableAtlasModule()
 	 */
 	@Override
 	public void enableAtlasModule() throws APIException {
 		registerPostAtlasDataTask(DEFAULT_ATLAS_DATA_TASK_INTERVAL); //7 days(in seconds)
 		setModuleEnabled(true);
 		setUsageDisclaimerAccepted(true);
 		postAtlasData();
 	}
 	
 	/**
 	 * @see org.openmrs.module.atlas.AtlasService#disableAtlasModule()
 	 */
 	@Override
 	public void disableAtlasModule(Boolean usageDisclaimerAccepted) throws APIException {
 		unregisterTask(AtlasConstants.POST_ATLAS_DATA_TASK_NAME);
 		setModuleEnabled(false);
 		setUsageDisclaimerAccepted(usageDisclaimerAccepted);
 		sendDeleteMessageToServer();
 	}
 	
 	/**
 	 * @see org.openmrs.module.atlas.AtlasService#setAtlasBubbleData()
 	 */
 	@Override
 	public void setAtlasBubbleData(AtlasData data) throws APIException {
 		AdministrationService svc = null;
 		try {
 			svc = Context.getAdministrationService();
 			String idString = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_ID);
 			
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_NAME, data.getName(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_IMPLEMENTATION_TYPE, data.getImplementationType().toString(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_WEBSITE, data.getWebsite(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_IMAGE_URL, data.getImageURL(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_CONTACT_EMAIL_ADDRESS, data.getContactEmailAddress(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_CONTACT_NAME, data.getContactName(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_INCLUDE_NUMBER_OF_OBSERVATIONS, data.getIncludeNumberOfObservations().toString(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_INCLUDE_NUMBER_OF_PATIENTS, data.getIncludeNumberOfPatients().toString(), svc);
 			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_INCLUDE_NUMBER_OF_ENCOUNTERS, data.getIncludeNumberOfEncounters().toString(), svc);
 			
 			setIsDirty(true);
 		}
 		catch (APIException apiEx) {
 			if (log.isErrorEnabled())
 				log.error("Could not set atlas data. Exception:" + apiEx.getMessage());
 		}
 	}
 	
 	/**
 	 * @see org.openmrs.module.atlas.AtlasService#setIncludeSystemConfiguration()
 	 */
 	@Override
 	public void setIncludeSystemConfiguration(Boolean includeSystemConfiguration) throws APIException {
 		setGlobalProperty(AtlasConstants.GLOBALPROPERTY_INCLUDE_SYSTEM_CONFIGURATION, includeSystemConfiguration.toString());
 	}
 	
 	/**
 	 * @see org.openmrs.module.atlas.AtlasService#setUsageDisclaimerAccepted()
 	 */
 	@Override
 	public void setUsageDisclaimerAccepted(Boolean usageDisclaimerAccepted) throws APIException {
 		setGlobalProperty(AtlasConstants.GLOBALPROPERTY_USAGE_DISCLAIMER_ACCEPTED, usageDisclaimerAccepted.toString());
 	}
 	
 	/**
 	 * @see org.openmrs.module.atlas.AtlasService#setPosition()
 	 */
 	@Override
 	public void setPosition(Double lat, Double lng) throws APIException {
 		AdministrationService svc = null;
 		try {
 			svc = Context.getAdministrationService();
			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_LATITUDE, lat.toString(), svc);
			setGlobalProperty(AtlasConstants.GLOBALPROPERTY_LONGITUDE, lng.toString(), svc);
 			setIsDirty(true);
 		}
 		catch (APIException apiEx) {
 			if (log.isErrorEnabled())
 				log.error("Could not set position global properties. Exception:" + apiEx.getMessage());
 		}
 	}
 	
 	/**
 	 * @see org.openmrs.module.atlas.AtlasService#setZoom()
 	 */
 	@Override
 	public void setZoom(Integer zoom) throws APIException {
 		setGlobalProperty(AtlasConstants.GLOBALPROPERTY_ZOOM, zoom.toString());
 	}
 	
 	/**
 	 * @see org.openmrs.module.atlas.AtlasService#postAtlasData()
 	 */
 	@Override
 	public void postAtlasData() throws APIException {
 		//update statistics
 		Long nrOfObservations = getStatisticsDAO().getNumberOfObservations();
 		Long nrOfEncounters = getStatisticsDAO().getNumberOfEncounters();
 		Long nrOfPatients = getStatisticsDAO().getNumberOfPatients();
 		//reset dirty flag whenever data is sent to server
 		setIsDirty(false);
 		AdministrationService svc = null;
 		try {
 			svc = Context.getAdministrationService();
 			svc.saveGlobalProperty(new GlobalProperty(AtlasConstants.GLOBALPROPERTY_NUMBER_OF_OBSERVATIONS, String
 			        .valueOf(nrOfObservations)));
 			svc.saveGlobalProperty(new GlobalProperty(AtlasConstants.GLOBALPROPERTY_NUMBER_OF_ENCOUNTERS, String
 			        .valueOf(nrOfEncounters)));
 			svc.saveGlobalProperty(new GlobalProperty(AtlasConstants.GLOBALPROPERTY_NUMBER_OF_PATIENTS, String
 			        .valueOf(nrOfPatients)));
 			
 			//get data
 			String jsonData = getJson(false);
 			
 			URL u = new URL(AtlasConstants.SERVER_URL);
 			HttpURLConnection connection = (HttpURLConnection) u.openConnection();
 			connection.setConnectTimeout(30000);
 			connection.setReadTimeout(30000);
 			connection.setDoOutput(true);
 			connection.setRequestMethod("POST");
 			connection.setRequestProperty("Content-Type", "application/json");
 			
 			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
 			writer.write(jsonData);
 			writer.close();
 			
 			log.info("Trying to post the following data:" + jsonData);
 			int status = connection.getResponseCode();
 			if (status == 200) {
 				log.info("Atlas data sent to server at " + AtlasConstants.SERVER_URL + " successfully (status 200)");
 			} else {
 				log.info("The atlas data failed to reach the server at " + AtlasConstants.SERVER_URL + " (status " + status
 				        + ").");
 			}
 			
 		}
 		catch (APIException apiEx) {
 			if (log.isErrorEnabled())
 				log.error("Could not set atlas data. Exception:" + apiEx.getMessage());
 		}
 		catch (Exception e) {
 			if (log.isErrorEnabled())
 				log.error("Could not post atlas data. Exception:" + e.getMessage());
 		}
 	}
 	
 	/**
 	 * @see org.openmrs.module.atlas.AtlasService#getJson()
 	 */
 	@Override
 	public String getJson(Boolean isPreview) throws APIException {
 		AtlasData data = getAtlasData();
 		StringBuilder sb = new StringBuilder();
 		String name = data.getName();
 		if (name == "" && isPreview) {
 			name = "Preview Name";
 		}
 		sb.append("{\"id\" : \"" + data.getId() + "\", ");
 		sb.append("\"geolocation\" :  {\"latitude\" : " + data.getLatitude() + ", \"longitude\" : " + data.getLongitude()
 		        + "}, ");
 		sb.append("\"name\" : \"" + name + "\",");
 		sb.append("\"type\" : \"" + ImplementationType.values()[data.getImplementationType()] + "\",");
 		sb.append("\"website\" : \"" + data.getWebsite() + "\",");
 		sb.append("\"notes\" : \"" + data.getNotes() + "\",");
 		sb.append("\"imageURL\" : \"" + data.getImageURL() + "\",");
 		if (data.getIncludeNumberOfPatients()) {
 			sb.append("\"patients\" : \"" + data.getNumberOfPatients() + "\",");
 		}
 		if (data.getIncludeNumberOfObservations()) {
 			sb.append("\"observations\" : \"" + data.getNumberOfObservations() + "\",");
 		}
 		if (data.getIncludeNumberOfEncounters()) {
 			sb.append("\"encounters\" : \"" + data.getNumberOfEncounters() + "\",");
 		}
 		//sb.append("\"contact_details\" :  {");
 		sb.append("\"email\" : \"" + data.getContactEmailAddress() + "\",");
 		sb.append("\"contact\" : \"" + data.getContactName() + "\"");
 		//sb.append("}");
 		
 		if (data.getIncludeSystemConfiguration()) {
 			sb.append(", \"data\" : {");
 			sb.append("\"version\" : \"" + OpenmrsConstants.OPENMRS_VERSION + "\",");
 			Collection<Module> modules = ModuleFactory.getLoadedModules();
 			sb.append("\"modules\" : [");
 			for (Module mod : modules) {
 				sb.append("{\"id\": \"" + mod.getModuleId() + "\",");
 				sb.append("\"name\": \"" + mod.getName() + "\",");
 				sb.append("\"version\": \"" + mod.getVersion() + "\",");
 				sb.append("\"active\" : \"" + mod.isStarted() + "\"},");
 			}
 			//delete last "," 
 			sb.deleteCharAt(sb.length() - 1);
 			sb.append("]}");
 			
 		}
 		sb.append("}");
 		return sb.toString();
 	}
 	
 	/**
 	 * Method that registers that registers an OpenMRS task that will periodically post the atlas
 	 * data to the OpenMRS server
 	 * 
 	 * @param interval The number of seconds between posts
 	 * @return True if the task was registered, false otherwise
 	 */
 	private boolean registerPostAtlasDataTask(long interval) {
 		return registerTask(AtlasConstants.POST_ATLAS_DATA_TASK_NAME, AtlasConstants.POST_ATLAS_DATA_TASK_DESCRIPTION,
 		    PostAtlasDataQueueTask.class, interval);
 	}
 	
 	/**
 	 * Method that registers that registers an OpenMRS task that will periodically post the atlas
 	 * data to the OpenMRS server
 	 * 
 	 * @param name The name of the task
 	 * @param description The tasks description
 	 * @param clazz The class that will do the actual posting
 	 * @param interval The number of seconds between posts
 	 * @return True if the task was registered, false otherwise
 	 */
 	private boolean registerTask(String name, String description, Class<? extends Task> clazz, long interval) {
 		try {
 			Context.addProxyPrivilege("Manage Scheduler");
 			
 			TaskDefinition taskDef = Context.getSchedulerService().getTaskByName(name);
 			if (taskDef == null) {
 				taskDef = new TaskDefinition();
 				taskDef.setTaskClass(clazz.getCanonicalName());
 				taskDef.setStartOnStartup(true);
 				taskDef.setRepeatInterval(interval);
 				taskDef.setStarted(true);
 				taskDef.setStartTime(getPreviousMidnight(null));
 				taskDef.setName(name);
 				taskDef.setUuid(UUID.randomUUID().toString());
 				taskDef.setDescription(description);
 				Context.getSchedulerService().scheduleTask(taskDef);
 			}
 			
 		}
 		catch (SchedulerException ex) {
 			log.warn("Unable to register task '" + name + "' with scheduler", ex);
 			return false;
 		}
 		finally {
 			Context.removeProxyPrivilege("Manage Scheduler");
 		}
 		return true;
 	}
 	
 	/**
 	 * Unregisters the named task
 	 * 
 	 * @param name The task name
 	 */
 	private void unregisterTask(String name) {
 		TaskDefinition taskDef = Context.getSchedulerService().getTaskByName(name);
 		if (taskDef != null)
 			Context.getSchedulerService().deleteTask(taskDef.getId());
 	}
 	
 	/**
 	 * Method that sets the atlas.moduleEnabled global property
 	 * 
 	 * @param moduleEnabled
 	 */
 	private void setModuleEnabled(Boolean moduleEnabled) {
 		setGlobalProperty(AtlasConstants.GLOBALPROPERTY_MODULE_ENABLED, moduleEnabled.toString());
 	}
 	
 	/**
 	 * Method that sets the atlas.isDirty global property
 	 * 
 	 * @param isDirty
 	 */
 	private void setIsDirty(Boolean isDirty) {
 		setGlobalProperty(AtlasConstants.GLOBALPROPERTY_IS_DIRTY, isDirty.toString());
 	}
 	
 	/**
 	 * Method that sends a delete message to the server
 	 */
 	private void sendDeleteMessageToServer() {
 		AdministrationService svc = null;
 		try {
 			svc = Context.getAdministrationService();
 			String id = svc.getGlobalProperty(AtlasConstants.GLOBALPROPERTY_ID);
 			
 			URL u = new URL(AtlasConstants.SERVER_URL + "?id=" + id + "&secret=victor");
 			HttpURLConnection connection = (HttpURLConnection) u.openConnection();
 			connection.setConnectTimeout(30000);
 			connection.setReadTimeout(30000);
 			connection.setDoOutput(true);
 			connection.setRequestMethod("DELETE");
 			
 			int status = connection.getResponseCode();
 			if (status == 200) {
 				log.debug("An atlas data delete message has been recieved by the server at " + AtlasConstants.SERVER_URL
 				        + " (status 200).");
 			} else {
 				log.debug("The atlas data delete message failed to reach the server at " + AtlasConstants.SERVER_URL
 				        + " (status " + status + ").");
 			}
 			
 		}
 		catch (Exception apiEx) {
 			if (log.isErrorEnabled())
 				log.error("Could not delete atlas data. Exception:" + apiEx.getMessage());
 		}
 	}
 	
 	private void setGlobalProperty(String name, String value) {
 			AdministrationService svc = null;
 			try {
 				svc = Context.getAdministrationService();
 				GlobalProperty prop = svc.getGlobalPropertyObject(name);
 				if (prop == null) {
 					svc.saveGlobalProperty(new GlobalProperty(name, value));
 				} else {
 					prop.setPropertyValue(value.toString());
 					svc.saveGlobalProperty(prop);
 				}
 			}
 			catch (APIException apiEx) {
 				if (log.isErrorEnabled())
 					log.error("Could not set global property: (" + name +" - "+value+"). Exception:" + apiEx.getMessage());
 			}
 		}
 	
 	private void setGlobalProperty(String name, String value, AdministrationService svc) {
 			GlobalProperty prop = svc.getGlobalPropertyObject(name);
 			if (prop == null) {
 				svc.saveGlobalProperty(new GlobalProperty(name, value));
 			} else {
 				prop.setPropertyValue(value.toString());
 				svc.saveGlobalProperty(prop);
 			}
 	}
 	/**
 	 * Method that returns the midnight before the date specified as a parameter
 	 * 
 	 * @param date
 	 * @return
 	 */
 	private static Date getPreviousMidnight(Date date) {
 		// Initialize with date if it was specified
 		Calendar cal = new GregorianCalendar();
 		if (date != null)
 			cal.setTime(date);
 		
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		return cal.getTime();
 	}
 	
 }
