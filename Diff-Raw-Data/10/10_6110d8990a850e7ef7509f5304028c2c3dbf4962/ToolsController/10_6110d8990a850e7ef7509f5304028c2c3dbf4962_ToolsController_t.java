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
 package org.openmrs.module.logmanager.web.controller;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.openmrs.module.Module;
 import org.openmrs.module.ModuleFactory;
import org.openmrs.module.logmanager.AppenderProxy;
 import org.openmrs.module.logmanager.Constants;
 import org.openmrs.module.logmanager.web.util.WebUtils;
import org.openmrs.util.MemoryAppender;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.ParameterizableViewController;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * Controller for tools page
  */
 public class ToolsController extends ParameterizableViewController {
 	
 	protected static final Log log = LogFactory.getLog(ToolsController.class);
 	
 	/**
 	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	protected ModelAndView handleRequestInternal(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		
 		Map<String, Object> model = new HashMap<String, Object>();
 		
 		// Reset log4j configuration
 		if (request.getParameter("clear") != null) {		
 			clearConfiguration();
 			WebUtils.setInfoMessage(request, Constants.MODULE_ID + ".tools.clearSuccess", null);
 		}
 		else if (request.getParameter("reload") != null) {
 			String[] configs = request.getParameterValues("configs");
 			reloadConfiguration(configs);
 			WebUtils.setInfoMessage(request, Constants.MODULE_ID + ".tools.reloadSuccess", null);
 		}
 		// Special logger switches
 		else if (request.getParameter("startAPIProfiling") != null)		
 			setProfilingLogging(true);
 		else if (request.getParameter("stopAPIProfiling") != null)		
 			setProfilingLogging(false);
 		else if (request.getParameter("startHibernateSQL") != null)		
 			setHibernateSQLLogging(true);
 		else if (request.getParameter("stopHibernateSQL") != null)		
 			setHibernateSQLLogging(false);
 		
 		List<Map<String, Object>> log4jConfigs = getLog4jConfigs();
 		model.put("log4jConfigs", log4jConfigs);
 		
 		Logger profilingLogger = LogManager.exists(Constants.LOGGER_API_PROFILING);
 		Level profilingLoggerLevel = (profilingLogger != null) ? profilingLogger.getEffectiveLevel() : null;
 		
 		model.put("apiProfilingLoggerName", Constants.LOGGER_API_PROFILING);
 		model.put("apiProfilingStarted", (profilingLoggerLevel != null) ? (profilingLoggerLevel.toInt() <= Level.TRACE.toInt()) : false);
 		
 		Logger sqlLogger = LogManager.exists(Constants.LOGGER_HIBERNATE_SQL);
 		Level sqlLoggerLevel = (sqlLogger != null) ? sqlLogger.getEffectiveLevel() : null;
 		
 		model.put("hibernateSQLLoggerName", Constants.LOGGER_HIBERNATE_SQL);
 		model.put("hibernateSQLStarted", (sqlLoggerLevel != null) ? (sqlLoggerLevel.toInt() <= Level.DEBUG.toInt()) : false);
 		
 		return new ModelAndView(getViewName(), model);
 	}
 	
 	/**
 	 * Gets a list of log4j config sources
 	 * @return
 	 */
 	private List<Map<String, Object>> getLog4jConfigs() {
 		List<Map<String, Object>> log4jConfigs = new ArrayList<Map<String, Object>>();
 		
 		Map<String, Object> mainConfig = new HashMap<String, Object>();
 		mainConfig.put("display", "Main (log4j.xml)");
 		mainConfig.put("moduleId", "$");
 		log4jConfigs.add(mainConfig);
 		
 		// Load log4j config files from each module if they exist
 		Collection<Module> modules = ModuleFactory.getLoadedModules();
 		for (Module module : modules) {
 			if (module.getLog4j() != null) {
 				Map<String, Object> modConfig = new HashMap<String, Object>();
 				modConfig.put("display", "Module: " + module.getModuleId() + " (log4j.xml)");
 				modConfig.put("moduleId", module.getModuleId());
 				modConfig.put("usesRoot", isModuleModifyingRoot(module));
 				modConfig.put("outsideNS", isModuleModifyingLoggerOutsideNS(module));
 				log4jConfigs.add(modConfig);
 			}
 		}
 		
 		return log4jConfigs;
 	}
 	
 	/**
 	 * Checks to see if the given module is modifying log4j's root logger
 	 * @param module the module to check
 	 * @return true if root logger is being modified
 	 */
 	private boolean isModuleModifyingRoot(Module module) {
 		Element log4jDocElm = module.getLog4j().getDocumentElement();
 		NodeList roots = log4jDocElm.getElementsByTagName("root");
 		return roots.getLength() > 0;
 	}
 	
 	/**
 	 * Checks to see if the given module is modifying loggers outside of its namespace
 	 * @param module the module to check
 	 * @return true if loggers are being modified
 	 */
 	private boolean isModuleModifyingLoggerOutsideNS(Module module) {
 		String moduleNS = "org.openmrs.module." + module.getModuleId();
 		
 		Element log4jDocElm = module.getLog4j().getDocumentElement();
 		NodeList loggers = log4jDocElm.getElementsByTagName("logger");
 		for (int n = 0; n < loggers.getLength(); n++) {
 			NamedNodeMap attrs = loggers.item(n).getAttributes();
 			Node nameNode = attrs.getNamedItem("name");
 			if (!nameNode.getNodeValue().startsWith(moduleNS))
 				return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Clears the log4j configuration
 	 */
 	private void clearConfiguration() {
 		BasicConfigurator.resetConfiguration();
 	}
 	
 	/**
 	 * Reloads the log4j configuration
 	 * @param the list of module ids ($ means the main OpenMRS config)
 	 */
 	private void reloadConfiguration(String[] moduleIds) {
 		if (moduleIds == null)
 			return;
 		
 		for (String moduleId : moduleIds) {
 			if (moduleId.equals("$")) {
 				// Load main OpenMRS log4j.xml
 				URL url = ToolsController.class.getResource("/log4j.xml");
 				DOMConfigurator.configure(url);
				
				// Reloading the OpenMRS log4j.xml file will likely create MEMORY_APPENDER
				// again so switch the system logger to the new MEMORY_APPENDER
				MemoryAppender sysApp = (MemoryAppender)LogManager.getRootLogger().getAppender(Constants.SYSTEM_APPENDER_NAME);
				if (sysApp != null)
					AppenderProxy.setSystemAppender(new AppenderProxy(sysApp));
 			}
 			else {
 				try {
 					Module module = ModuleFactory.getModuleById(moduleId);
 					if (module.getLog4j() != null)
 						DOMConfigurator.configure(module.getLog4j().getDocumentElement());
 				} catch (Exception e) {
 					log.error(e.getMessage());
 				}
 			}
 		}	
 	}
 	
 	/**
 	 * Toggles profiling of API service methods
 	 * @param on true to enable logging, else false
 	 */
 	private void setProfilingLogging(boolean on) {
 		LogManager.getLogger("org.openmrs.api").setLevel(on ? Level.TRACE : Level.WARN);
 	}
 	
 	/**
 	 * Toggles logging of SQL in Hibernate
 	 * @param on true to enable logging, else false
 	 */
 	private void setHibernateSQLLogging(boolean on) {
 		LogManager.getLogger("org.hibernate.SQL").setLevel(on ? Level.DEBUG : Level.OFF);
 	}
 }
