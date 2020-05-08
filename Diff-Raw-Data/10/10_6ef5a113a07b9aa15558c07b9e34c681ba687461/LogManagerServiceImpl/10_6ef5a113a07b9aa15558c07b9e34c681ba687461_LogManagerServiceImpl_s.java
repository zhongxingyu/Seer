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
 package org.openmrs.module.logmanager.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.Appender;
 import org.apache.log4j.Level;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.apache.log4j.spi.LoggingEvent;
 import org.openmrs.api.APIException;
 import org.openmrs.api.impl.BaseOpenmrsService;
 import org.openmrs.module.logmanager.AppenderProxy;
 import org.openmrs.module.logmanager.Constants;
 import org.openmrs.module.logmanager.LogManagerService;
 import org.openmrs.module.logmanager.LoggerProxy;
 import org.openmrs.module.logmanager.Preset;
 import org.openmrs.module.logmanager.QueryField;
 import org.openmrs.module.logmanager.db.LogManagerDAO;
 import org.openmrs.module.logmanager.util.PagingInfo;
 
 /**
  * Implementation of the log manager service
  */
 public class LogManagerServiceImpl extends BaseOpenmrsService implements LogManagerService {
 
 	protected static final Log log = LogFactory.getLog(LogManagerServiceImpl.class);
 	
 	protected LogManagerDAO dao;
 	
 	/**
 	 * Sets the database access object for this service
 	 * @param dao the database access object
 	 */
 	public void setLogManagerDAO(LogManagerDAO dao) {
 		this.dao = dao;
 	}
 	
 	/**
 	 * @see org.openmrs.module.logmanager.LogManagerService#getLoggers(boolean)
 	 */
 	@SuppressWarnings("unchecked")
 	public List<LoggerProxy> getLoggers(boolean incImplicit) {
 		Enumeration<Logger> loggersEnum = (Enumeration<Logger>)LogManager.getCurrentLoggers();
 		
 		// Convert enum to a list
 		List<Logger> loggers = incImplicit ? Collections.list(loggersEnum) : getExplicitLoggersFromEnum(loggersEnum);
 		
 		// Sort list by logger name
 		Collections.sort(loggers, new Comparator<Logger>() {
 			public int compare(Logger log1, Logger log2) {
 				return log1.getName().compareTo(log2.getName());
 			}
 		});
 		
 		// Convert to proxy objects
 		List<LoggerProxy> proxies = new ArrayList<LoggerProxy>();
 		for (Logger logger : loggers)
 			proxies.add(new LoggerProxy(logger));
 		
 		return proxies;
 	}
 	
 	/**
 	 * @see org.openmrs.module.logmanager.LogManagerService#getAppender(int)
 	 */
 	public AppenderProxy getAppender(int id) throws APIException {
 		Collection<AppenderProxy> appenders = getAppenders(false);
 		for (AppenderProxy appender : appenders)
 			if (appender.getId() == id)
 				return appender;
 		return null;
 	}
 	
 	/**
 	 * @see org.openmrs.module.logmanager.LogManagerService#getAppenders()
 	 */
 	@SuppressWarnings("unchecked")
 	public Collection<AppenderProxy> getAppenders(boolean sorted) {
 		Set<AppenderProxy> appenders = new HashSet<AppenderProxy>();
 		
 		// Add system appender
		appenders.add(AppenderProxy.getSystemAppender());
 		
 		// Add appenders attached to the root logger
 		Enumeration<Appender> rootAppenders = LogManager.getRootLogger().getAllAppenders();
 		while (rootAppenders.hasMoreElements())
 			appenders.add(new AppenderProxy(rootAppenders.nextElement()));
 		
 		// Search for appenders on all other loggers
 		Enumeration<Logger> loggersEnum = LogManager.getCurrentLoggers();
 		while (loggersEnum.hasMoreElements()) {
 			Logger logger = loggersEnum.nextElement();
 			Enumeration<Appender> appendersEnum = logger.getAllAppenders();
 			
 			while (appendersEnum.hasMoreElements())
 				appenders.add(new AppenderProxy(appendersEnum.nextElement()));
 		}
 		
 		// Optionally sort into a list
 		if (sorted) {
 			List<AppenderProxy> appenderList = new ArrayList<AppenderProxy>(appenders);
 			Collections.sort(appenderList, new Comparator<AppenderProxy>() {
 				public int compare(AppenderProxy ap1, AppenderProxy ap2) {
 					String s1 = ap1.getName() != null ? ap1.getName() : "";
 					String s2 = ap2.getName() != null ? ap2.getName() : "";
 					return s1.compareToIgnoreCase(s2);
 				}	
 			});
 			return appenderList;
 		}
 		else
 			return appenders;
 	}
 	
 	/**
 	 * @see org.openmrs.module.logmanager.LogManagerService#deleteAppender(AppenderProxy)
 	 */
 	@SuppressWarnings("unchecked")
 	public void deleteAppender(AppenderProxy appender) throws APIException {
 		// Get all loggers
 		Enumeration<Logger> loggersEnum = (Enumeration<Logger>)LogManager.getCurrentLoggers();
 		
 		// Remove from root logger
 		LogManager.getRootLogger().removeAppender(appender.getTarget());
 		
 		// Remove appender from all other loggers
 		while (loggersEnum.hasMoreElements()) {
 			Logger logger = loggersEnum.nextElement();
 			logger.removeAppender(appender.getTarget());
 		}
 	}
 	
 	/**
 	 * @see org.openmrs.module.logmanager.LogManagerService#getAppenderEvents(Appender, Level, int, QueryField, String, Paging)
 	 */
 	public List<LoggingEvent> getAppenderEvents(AppenderProxy appender, Level level, int levelOp, QueryField queryField, String queryValue, PagingInfo paging) throws APIException {
 
 		Collection<LoggingEvent> eventsAll = appender.getLoggingEvents();
 		List<LoggingEvent> events = new LinkedList<LoggingEvent>();
 		
 		for (LoggingEvent event : eventsAll) {			
 			if (level != null && !testLevel(event.getLevel(), levelOp, level))
 				continue;		
 			
 			if (queryValue != null) {
 				switch (queryField) {
 				case LOGGER_NAME:
 					if (!event.getLoggerName().contains(queryValue))
 						continue;
 					break;
 				case CLASS_NAME:
 					if (!event.locationInformationExists() || !event.getLocationInformation().getClassName().contains(queryValue))
 						continue;
 					break;
 				case FILE_NAME:
 					if (!event.locationInformationExists() || !event.getLocationInformation().getFileName().contains(queryValue))
 						continue;
 					break;
 				} 
 			}
 			
 			events.add(0, event);
 		}
 		
 		if (paging != null)
 			// Select only the events for this page
 			return selectListPage(events, paging);
 		
 		return events;
 	}
 	
 	/**
 	 * @see LogManagerService#getAppenderEvent(org.openmrs.module.logmanager.AppenderProxy, int)
 	 */
 	public LoggingEvent getAppenderEvent(AppenderProxy appender, int id) {
 		return getAppenderEvent(appender, id, null, 0);
 	}
 	
 	/**
 	 * @see LogManagerService#getAppenderEvent(AppenderProxy, int, List, int)
 	 */
 	public LoggingEvent getAppenderEvent(AppenderProxy appender, int id, List<LoggingEvent> contextEvents, int contextCount) throws APIException {
 		List<LoggingEvent> events = getAppenderEvents(appender, null, 0, null, null, null);
 		LoggingEvent prevEvent = null;
 		
 		for (Iterator<LoggingEvent> iter = events.iterator(); iter.hasNext(); ) {
 			LoggingEvent e = iter.next();
 			
 			if (e.hashCode() == id) {
 				if (contextEvents != null) {
 					// Add next n events in list
 					if (contextEvents != null && contextCount > 0)
 						for (; iter.hasNext() && contextEvents.size() <= contextCount; )
 							contextEvents.add(iter.next());
 					// And previous and next events
 					else if (contextCount == -1) {
 						contextEvents.add(iter.hasNext() ? iter.next() : null);
 						contextEvents.add(prevEvent);
 					}
 				}
 				
 				return e;
 			}
 			
 			prevEvent = e;
 		}
 		return null;
 	}
 
 	/**
 	 * @see LogManagerService#getMySQLVersion()
 	 */
 	public String getMySQLVersion() throws APIException {
 		return dao.getMySQLVersion();
 	}
 	
 	/**
 	 * @see LogManagerService#getPreset(int)
 	 */
 	public Preset getPreset(int presetId) throws APIException {
 		return dao.getPreset(presetId);
 	}
 
 	/**
 	 * @see LogManagerService#getPresets()
 	 */
 	public List<Preset> getPresets() throws APIException {
 		return dao.getPresets();
 	}
 
 	/**
 	 * @see LogManagerService#savePreset(Preset)
 	 */
 	public void saveCurrentLoggersAsPreset(Preset preset) throws APIException {	
 		Map<String, Integer> loggerMap = preset.getLoggerMap();
 		loggerMap.clear();
 		
 		// Add root logger to map
 		LoggerProxy rootLogger = LoggerProxy.getRootLogger();
 		loggerMap.put("ROOT", rootLogger.getLevelInt());
 		
 		// Add all other loggers
 		List<LoggerProxy> loggers = getLoggers(false);
 		for (LoggerProxy logger : loggers)
 			loggerMap.put(logger.getName(), logger.getLevelInt());
 		
 		dao.savePreset(preset);
 	}
 
 	/**
 	 * @see LogManagerService#deletePreset(Preset)
 	 */
 	public void deletePreset(Preset preset) throws APIException {
 		dao.deletePreset(preset);
 	}
 	
 	/**
 	 * Gets all loggers with explicit level or appenders from an enumeration of loggers
 	 * @param loggersEnum the enumeration of loggers
 	 * @return the list of loggers
 	 */
 	private static List<Logger> getExplicitLoggersFromEnum(Enumeration<Logger> loggersEnum) {
 		List<Logger> loggers = new ArrayList<Logger>();
 		
 		while (loggersEnum.hasMoreElements()) {
 			Logger logger = loggersEnum.nextElement();
 			if (logger.getLevel() != null || logger.getAllAppenders().hasMoreElements())
 				loggers.add(logger);
 		}
 		
 		return loggers;
 	}
 	
 	/**
 	 * Selects a range of elements from a list
 	 * @param <T> the type of each list element
 	 * @param list the list to select from
 	 * @return a new list containing only those list elements in the current page
 	 */
 	private static <T> List<T> selectListPage(List<T> list, PagingInfo paging) {
 		List<T> selection = new ArrayList<T>();
 		
 		int lStart = Math.max(0, Math.min(paging.getPageOffset(), list.size()));
 		int lEnd = Math.max(0, Math.min(paging.getPageOffset() + paging.getPageSize(), list.size()));
 		for (int l = lStart; l < lEnd; l++)
 			selection.add(list.get(l));
 		
 		paging.setResultsTotal(list.size());
 		
 		return selection;
 	}
 	
 	/**
 	 * Compares two level values with a given boolean operator 
 	 * @param level1 the first level value
 	 * @param levelOp the boolean operator (-1: LE, 0: EQ, 1: GE)
 	 * @param level2 the second level value
 	 * @return value of the comparison
 	 */
 	private boolean testLevel(Level level1, int levelOp, Level level2) {
 		if (level1 == Level.ALL || level2 == Level.ALL)
 			return true;
 		
 		switch (levelOp) {
 		case Constants.BOOL_OP_LE:
 			return (level1.toInt() <= level2.toInt());
 		case Constants.BOOL_OP_GE:
 			return (level1.toInt() >= level2.toInt());
 		default:
 			return (level1.toInt() == level2.toInt());
 		}
 	}
 }
