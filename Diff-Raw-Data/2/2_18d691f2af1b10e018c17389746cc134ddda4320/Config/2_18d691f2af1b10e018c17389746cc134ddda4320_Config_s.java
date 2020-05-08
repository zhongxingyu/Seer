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
 package org.openmrs.module.logmanager;
 
 import org.openmrs.GlobalProperty;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.context.Context;
 
 /**
  * Holds the configuration options for this module
  */
 public class Config {
 	
 	protected static Config config;
 	
 	protected String defaultAppenderName;
 	protected boolean recreateDefaultAppender;
 	protected boolean logUncaughtExceptions;
 	
 	/**
 	 * The default constructor
 	 */
 	protected Config() {
 		load(); 
 	}
 	
 	/**
 	 * Gets the singleton instance of this class
 	 * @return the config instance
 	 */
 	public static Config getInstance() {
 		if (config == null)
 			config = new Config();
 		return config;
 	}
 	
 	/**
 	 * Loads the configuration from global properties
 	 */
 	public void load() {
 		defaultAppenderName = loadStringOption(Constants.PROP_DEFAULT_APPENDER_NAME, Constants.DEF_DEFAULT_APPENDER_NAME); 
 		recreateDefaultAppender = loadBooleanOption(Constants.PROP_RECREATE_DEFAULT_APPENDER, Constants.DEF_RECREATE_DEFAULT_APPENDER);
		recreateDefaultAppender = loadBooleanOption(Constants.PROP_LOG_UNCAUGHT_EXCEPTIONS, Constants.DEF_LOG_UNCAUGHT_EXCEPTIONS);
 	}
 	
 	/**
 	 * Saves the configuration to global properties
 	 */
 	public void save() {
 		saveOption(Constants.PROP_DEFAULT_APPENDER_NAME, defaultAppenderName);
 		saveOption(Constants.PROP_RECREATE_DEFAULT_APPENDER, recreateDefaultAppender);
 		saveOption(Constants.PROP_LOG_UNCAUGHT_EXCEPTIONS, logUncaughtExceptions);
 	}
 
 	/**
 	 * Gets default appender used by the log viewer
 	 * @return the appender name
 	 */
 	public String getDefaultAppenderName() {
 		return defaultAppenderName;
 	}
 
 	/**
 	 * Sets default appender used by the log viewer
 	 * @param defaultAppender the appender name
 	 */
 	public void setDefaultAppenderName(String defaultAppenderName) {
 		this.defaultAppenderName = defaultAppenderName;
 	}
 	
 	/**
 	 * Gets whether the default appender should be recreated if missing on module startup
 	 * @return true if appender should be recreated
 	 */
 	public boolean isRecreateDefaultAppender() {
 		return recreateDefaultAppender;
 	}
 
 	/**
 	 * Sets whether the default appender should be recreated if missing on module startup
 	 * @param recreateDefaultAppender true if appender should be recreated
 	 */
 	public void setRecreateDefaultAppender(boolean recreateDefaultAppender) {
 		this.recreateDefaultAppender = recreateDefaultAppender;
 	}
 
 	/**
 	 * Gets whether the module should log uncaught exceptions
 	 * @return true to enable logging of uncaught exceptions
 	 */
 	public boolean isLogUncaughtExceptions() {
 		return logUncaughtExceptions;
 	}
 
 	/**
 	 * Sets whether the module should log uncaught exceptions
 	 * @param logUncaughtExceptions true to enable logging of uncaught exceptions
 	 */
 	public void setLogUncaughtExceptions(boolean logUncaughtExceptions) {
 		this.logUncaughtExceptions = logUncaughtExceptions;
 	}
 
 	/**
 	 * Utility method to load a string option from global properties
 	 * @param name the name of the global property
 	 * @param def the default value if global property is invalid
 	 * @return the string value
 	 */
 	private static String loadStringOption(String name, String def) {
 		AdministrationService svc = Context.getAdministrationService();
 		String s = svc.getGlobalProperty(name);
 		return (s != null) ? s : def;
 	}
 	
 	/**
 	 * Utility method to load an integer option from global properties
 	 * @param name the name of the global property
 	 * @param def the default value if global property is invalid
 	 * @return the integer value
 	 */
 	@SuppressWarnings("unused")
 	private static int loadIntOption(String name, int def) {
 		AdministrationService svc = Context.getAdministrationService();
 		String s = svc.getGlobalProperty(name);
 		try {
 			return Integer.parseInt(s);
 		}
 		catch (NumberFormatException ex) {
 			return def;
 		}
 	}
 	
 	/**
 	 * Utility method to load an boolean option from global properties
 	 * @param name the name of the global property
 	 * @return the boolean value
 	 */
 	private static boolean loadBooleanOption(String name, boolean def) {
 		AdministrationService svc = Context.getAdministrationService();
 		String s = svc.getGlobalProperty(name);
 		try {
 			return Boolean.parseBoolean(s);
 		}
 		catch (NumberFormatException ex) {
 			return def;
 		}
 	}
 	
 	/**
 	 * Utility method to save an option to global properties
 	 * @param name the name of the global property
 	 * @param value the value of the global property
 	 */
 	private static void saveOption(String name, Object value) {
 		AdministrationService svc = (AdministrationService)Context.getAdministrationService();
 		GlobalProperty property = svc.getGlobalPropertyObject(name);
 		property.setPropertyValue(String.valueOf(value));
 		svc.saveGlobalProperty(property);
 	}
 }
