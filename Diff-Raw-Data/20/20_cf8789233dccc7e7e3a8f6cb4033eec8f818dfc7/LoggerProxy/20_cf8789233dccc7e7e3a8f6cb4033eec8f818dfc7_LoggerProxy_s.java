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
 package org.openmrs.module.logmanager.log4j;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Appender;
 import org.apache.log4j.Category;
 import org.apache.log4j.Level;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.openmrs.module.logmanager.AbstractProxy;
 
 /**
  * This class contains the parameters required to create a logger and is used
  * with the logger form because logger objects cannot be created directly
  */
 public class LoggerProxy extends AbstractProxy<Logger> {
 	
 	// Proxied properties
 	protected String name;
 	protected Level level;
 	protected Set<AppenderProxy> appenders = new HashSet<AppenderProxy>();
 	
 	/**
 	 * Creates a logger proxy for a new logger
 	 */
 	public LoggerProxy(String name, Level level) {
 		this.name = name;
 		this.level = level;
 	}
 	
 	/**
 	 * Creates a logger proxy object from an existing logger
 	 * @param logger the logger
 	 */
 	@SuppressWarnings("unchecked")
 	public LoggerProxy(Logger target) {
 		this.target = target;
 		
 		this.name = target.getName();
 		this.level = target.getLevel();
 		
 		Enumeration<Appender> appEnum = target.getAllAppenders();
 		while (appEnum.hasMoreElements())
 			this.appenders.add(new AppenderProxy(appEnum.nextElement()));
 	}
 	
 	/**
 	 * Gets a proxy of the specified logger
 	 * @param name the logger name
 	 * @param force forces logger creation if it doesn't exist
 	 * @return the proxy logger or null if logger doesn't exist
 	 */
 	public static LoggerProxy getLogger(String name, boolean force) {
 		if (force)
 			return new LoggerProxy(LogManager.getLogger(name));
 		
 		Logger target = LogManager.exists(name);	
 		return (target != null) ? new LoggerProxy(target) : null;
 	}
 	
 	/**
 	 * Gets a proxy of the root logger
 	 * @return the proxy root logger
 	 */
 	public static LoggerProxy getRootLogger() {
 		return new LoggerProxy(LogManager.getRootLogger());
 	}
 	
 	/**
 	 * Returns true if proxy references the root logger
 	 * @return true if this logger is root
 	 */
 	public boolean isRoot() {
 		return target == LogManager.getRootLogger();
 	}
 	
 	/**
 	 * Updates the actual logger referenced by this proxy object
 	 */
 	@SuppressWarnings("unchecked")
 	public void updateTarget() {
 		// Create target if it doesn't exist already
 		if (target == null)
 			target = LogManager.getLogger(name);
 		
 		target.setLevel(level);
 		
 		// For some unknown reason... calling removeAllAppenders() on the
 		// the root logger breaks it, but this is fine
		List<Appender> apps = Collections.list(target.getAllAppenders());
		for (Appender appender : apps)
 			target.removeAppender(appender);
 		
		for (AppenderProxy appender : appenders)
 			target.addAppender(appender.getTarget());
 	}
 	
 	/**
 	 * Gets the name of the logger
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * Sets the name of the logger
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	/**
 	 * Gets the level of the logger
 	 * @return the level
 	 */
 	public Level getLevel() {
 		return level;
 	}
 	
 	/**
 	 * Gets the level of the logger as an integer or null if
 	 * level is undefined
 	 * @return the level as an integer
 	 */
 	public Integer getLevelInt() {
 		return (level != null) ? level.toInt() : null;
 	}
 	
 	/**
 	 * Gets the effective level of the logger
 	 * @return the effective level
 	 */
 	public Level getEffectiveLevel() {
 		if (target != null) 
 			return target.getEffectiveLevel();
 		else if (level != null)
 			return level;
 		else
 			return null;
 	}
 	
 	/**
 	 * Sets the name of the logger
 	 * @param level the level to set
 	 */
 	public void setLevel(Level level) {
 		this.level = level;
 	}
 
 	/**
 	 * Gets whether this proxy references an actual logger
 	 * @return true if logger exists in log4j
 	 */
 	public boolean isExisting() {
 		return (target != null);
 	}
 	
 	/**
 	 * Gets all the appenders explicitly attached to this logger
 	 * @return the collection of appenders
 	 */
 	public Collection<AppenderProxy> getAppenders() {
 		return appenders;
 	}
 	
 	/**
 	 * Gets all the appenders attached to this logger including inherited appenders
 	 * @return the collection of appenders
 	 */
 	@SuppressWarnings("unchecked")
 	public Collection<AppenderProxy> getEffectiveAppenders() {
 		Set<AppenderProxy> effAppenders = new HashSet<AppenderProxy>(getAppenders());
 		
 		if (target != null) {
 			// TODO this probably won't work with log4j 1.3 !!!
 			Category cat = target;
 			while ((cat = cat.getParent()) != null) {
 				Enumeration<Appender> appEnum = cat.getAllAppenders();
 				while (appEnum.hasMoreElements())
 					effAppenders.add(new AppenderProxy(appEnum.nextElement()));
 			}
 		}
 		// Even non-existent loggers inherit from the root... in theory
 		else {
 			Enumeration<Appender> appEnum = LogManager.getRootLogger().getAllAppenders();
 			while (appEnum.hasMoreElements())
 				effAppenders.add(new AppenderProxy(appEnum.nextElement()));
 		}
 		
 		return effAppenders;
 	}
 	
 	/**
 	 * Adds an appender to this logger
 	 * @param appender the appender to add
 	 */
 	public void addAppender(AppenderProxy appender) {
 		appenders.add(appender);
 	}
 	
 	/**
 	 * Removes all appenders from this logger
 	 */
 	public void removeAllAppenders() {
 		appenders.clear();
 	}
 	
 	/**
 	 * Gets whether this is an implicit logger, i.e. null level and no appenders
 	 * If the target exists, decision is based on the target
 	 * @return true if logger is implicit
 	 */
 	public boolean isImplicit() {
 		if (target != null)
 			return (target.getLevel() == null && !target.getAllAppenders().hasMoreElements());
 		else
 			return (level == null && appenders.isEmpty());
 	}
 	
 	/**
 	 * There is no mechanism for removing loggers in log4j 1.2
 	 * so instead we nullify its level and remove its appenders 
 	 * @param removeAppenders true if appenders should be removed
 	 */
 	public void makeImplicit(boolean removeAppenders) {
 		level = null;
 		if (removeAppenders)
 			appenders.clear();
 		
 		if (target != null) {
 			target.setLevel(null);
 			if (removeAppenders)
 				target.removeAllAppenders();
 		}
 	}
 	
 	/**
 	 * Sends a logging event through this logger
 	 * @param level the level
 	 * @param message the message
 	 */
 	public void log(Level level, String message) {
 		target.log(level, message);
 	}
 }
