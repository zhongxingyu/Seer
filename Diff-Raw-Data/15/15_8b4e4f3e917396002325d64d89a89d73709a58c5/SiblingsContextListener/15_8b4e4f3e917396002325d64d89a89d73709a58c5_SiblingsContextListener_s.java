 /**
  * Copyright (c) Members of the EGEE Collaboration. 2006-2009.
  * See http://www.eu-egee.org/partners/ for details on the copyright holders.
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
  *
  * Authors:
  * 	Andrea Ceccanti (INFN)
  */
 package org.glite.security.voms.admin.servlets;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import org.glite.security.voms.admin.configuration.VOMSConfigurationException;
 import org.glite.security.voms.admin.util.ExceptionUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 
 
 public class SiblingsContextListener implements ServletContextListener {
 
 	private static final Logger log = LoggerFactory
 			.getLogger(SiblingsContextListener.class);
 
 	public void contextDestroyed(ServletContextEvent ev) {
 		
 		
 	}
 
 	public void contextInitialized(ServletContextEvent ev) {
 
 		try {
 			
 			initializeVomsProperties(ev);
 			findConfiguredVOs(ev);
 			loadVersionProperties(ev);
 			configureLogging(ev);
 			log.info("Siblings context initialized.");
 		
 		
 		} catch (NamingException e) {
 			throw new RuntimeException("Error initalizing VOMS properties!", e); 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			throw new RuntimeException("Error configuring loggin system!", e);
 		}
 		
 
 	}
 	
 	
 	public void initializeVomsProperties(ServletContextEvent ev) throws NamingException{
 		// Obtain our environment naming context
 		Context initCtx = new InitialContext();
 		Context envCtx = (Context) initCtx.lookup("java:comp/env");
 
		String vomsAdminLocationVar = (String ) envCtx.lookup("VOMS_ADMIN_LOCATION_VAR");
		
		if (vomsAdminLocationVar == null)
		    log.warn("VOMS_ADMIN_LOCATION_VAR not defined in naming context...");
		else
 		    ev.getServletContext().setAttribute("VOMS_ADMIN_LOCATION_VAR", vomsAdminLocationVar);
 		
 		String gliteLocationVar = (String) envCtx.lookup("GLITE_LOCATION_VAR");
 		
 		if (gliteLocationVar == null)
 			throw new NamingException("GLITE_LOCATION_VAR property not found in JNDI context!");
 		
 		ev.getServletContext().setAttribute("GLITE_LOCATION_VAR", gliteLocationVar);
 		
 	}
 	
 	
 	
 	protected void configureLogging(ServletContextEvent ev) throws IOException{
 		
 		InputStream is = ev.getServletContext().getResourceAsStream("/WEB-INF/classes/logback.siblings.xml");
 		
 		if (is == null){
 			throw new VOMSConfigurationException("Error configuring logging! Logback configuration file not found in context!");
 		}
 		
 		try{
 			LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory(); 
 			JoranConfigurator configurator = new JoranConfigurator();
 			configurator.setContext(lc);
 
 			lc.reset();
 			configurator.doConfigure(is);
 		
 		}catch(JoranException e){
 			
 			ExceptionUtils.logError(log, e);
 			throw new VOMSConfigurationException("Error configuring logging system: "+e.getMessage(), e);
 			
 		}
 		
 	
 	}
 	
 	public void loadVersionProperties(ServletContextEvent ev) throws IOException{
 		
 		Properties versionProps = new Properties();
 		InputStream is = ev.getServletContext()
 		.getResourceAsStream("/WEB-INF/classes/version.properties");
 		
 		versionProps.load(is);
 		
 		ev.getServletContext().setAttribute("version", versionProps.get("voms-admin.server.version"));
 		
 		
 	}
 	
 	public void findConfiguredVOs(ServletContextEvent ev) {
 
 		String configDirPath = (String) ev.getServletContext().getAttribute("VOMS_ADMIN_LOCATION_VAR");
 		if (configDirPath == null)
 		    configDirPath = (String) ev.getServletContext().getAttribute("GLITE_LOCATION_VAR");
 
 		if (configDirPath == null)
 			throw new RuntimeException(
 					"No value found for VOMS_ADMIN_LOCATION_VAR or GLITE_LOCATION_VAR!");
 
 		
 		List voList = new ArrayList();
 
 		File configDir = new File(configDirPath + File.separator + "etc"
 				+ File.separator + "voms-admin");
 
 		if (!configDir.exists())
 			throw new RuntimeException(
 					"Voms configuration directory does not exist");
 
 		File[] filez = configDir.listFiles();
 
 		if (filez != null)
 			for (int i = 0; i < filez.length; i++) {
 
 				if (filez[i].isDirectory()) {
 					log.debug("Found vo: " + filez[i].getName());
 					voList.add(filez[i].getName());
 				}
 
 			}
 		
 		ev.getServletContext().setAttribute("configuredVOs", voList);
 		
 	}
 }
 
 
