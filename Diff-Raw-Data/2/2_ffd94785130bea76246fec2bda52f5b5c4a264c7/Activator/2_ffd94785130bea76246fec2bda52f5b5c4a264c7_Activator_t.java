 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2008 the original author or authors.
  * 
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  * 
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 
 package org.paxle.data.impl;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.event.EventConstants;
 import org.osgi.service.event.EventHandler;
 import org.osgi.service.monitor.Monitorable;
 import org.paxle.core.data.IDataProvider;
 import org.paxle.core.data.IDataSink;
 import org.paxle.core.filter.IFilter;
 import org.paxle.core.queue.CommandEvent;
 import org.paxle.core.queue.ICommand;
 import org.paxle.core.queue.ICommandProfile;
 import org.paxle.core.queue.ICommandProfileManager;
 import org.paxle.core.queue.ICommandTracker;
 import org.paxle.data.db.ICommandDB;
 import org.paxle.data.db.impl.CommandDB;
 import org.paxle.data.db.impl.CommandProfileDB;
 import org.paxle.data.db.impl.CommandProfileFilter;
 import org.paxle.data.db.impl.UrlExtractorFilter;
 
 public class Activator implements BundleActivator {
 
 	/**
 	 * Logger
 	 */
 	private Log logger = null;
 	
 	/**
 	 * A DB to store {@link ICommand commands}
 	 */
 	private CommandDB commandDB = null;
 	
 	/**
 	 * A DB to store {@link ICommandProfile command-profiles}
 	 */
 	private CommandProfileDB profileDB = null;
 	
 	/**
 	 * A {@link org.paxle.core.filter.IFilter} used to extract {@link URI} from 
 	 * the {@link org.paxle.core.doc.IParserDocument} and to store them into
 	 * the {@link #commandDB command-DB}
 	 */
 	private UrlExtractorFilter urlExtractor = null;
 	
 	private CommandProfileFilter profileFilter = null;
 	
 	/**
 	 * This function is called by the osgi-framework to start the bundle.
 	 * @see BundleActivator#start(BundleContext) 
 	 */		
 	public void start(BundleContext context) throws Exception {
 		// init logger
 		this.logger = LogFactory.getLog(this.getClass());
 
 		/* =========================================================
 		 * INIT DATABASES
 		 * ========================================================= */		
 		// determine the hibernate configuration file to use
 		URL hibernateConfigFile = this.getConfigURL(context);
 
 		// init the command-DB
 		this.createAndRegisterCommandDB(hibernateConfigFile, context);
 
 		// init the command-profile-DB
 		this.createAndRegisterProfileDB(hibernateConfigFile, context);
 
 		/* =========================================================
 		 * INIT COMMAND-FILTERS
 		 * ========================================================= */		
 		this.createAndRegisterUrlExtractorFilter(context);
 		this.createAndRegisterCommandProfileFilter(context);
 
 		/* =========================================================
 		 * DATABASE-STARTUP
 		 * ========================================================= */
 		this.commandDB.start();
 		
 		/*
 			// fill the crawler queue with URLs
 			TextCommandReader fileReader = new TextCommandReader(this.getClass().getResourceAsStream("/resources/data.txt"));
 			Hashtable<String,String> readerProps = new Hashtable<String, String>();
 			readerProps.put(IDataProvider.PROP_DATAPROVIDER_ID, "org.paxle.crawler.sink");
 			context.registerService(IDataProvider.class.getName(), fileReader, readerProps);
 		*/
 	}
 	
 	/**
 	 * We may have multiple datalayer-fragment-bundles installed. We try to find all available
 	 * config files here
 	 */
 	@SuppressWarnings("unchecked")
 	private HashMap<String, URL> getAvailableConfigFiles(BundleContext context) {		
 		HashMap<String, URL> availableConfigs = new HashMap<String, URL>();
 		
 		this.logger.info("Trying to find db config files ...");
 		Enumeration<URL> configFileEnum = context.getBundle().findEntries("/resources/hibernate/", "*.cfg.xml", true);
 		if (configFileEnum != null) {				
 			ArrayList<URL> configFileURLs = Collections.list(configFileEnum);			
 			this.logger.info(String.format("%d config-file(s) found.", Integer.valueOf(configFileURLs.size())));
 			
 			for (URL configFileURL : configFileURLs) {
 				String file = configFileURL.getFile();
 				int idx = file.lastIndexOf("/");
 				if (idx != -1) file = file.substring(idx+1);
 				idx = file.indexOf(".");
 				if (idx != -1) file = file.substring(0,idx);
 				
 				availableConfigs.put(file, configFileURL);
 			}
 		}
 		
 		return availableConfigs;
 	}
 		
 	private URL getConfigURL(BundleContext context) throws MalformedURLException {
 		URL config = null;		
 
 		HashMap<String, URL> availableConfigs = this.getAvailableConfigFiles(context);
 		if (availableConfigs.size() == 0) {
 			String errorMsg = "No config files found";
 			this.logger.error(errorMsg);
 			throw new ExceptionInInitializerError(errorMsg);
 		}
 
 		/* 
 		 * Getting the config file to use 
 		 */
 		String configStr = System.getProperty("CommandDB.configFile");
 		if (configStr != null) {
 			// the user has choosen a custom config file via system-properties
 			if (availableConfigs.containsKey(configStr)) {
 				// the props value is a key (e.g. "derby")
 				config = availableConfigs.get(configStr);
 			} else {
 				// the props value is an URL
 				config = new URL(configStr);	
 			}
 		} else {						
 			// using the default config file
 			if (availableConfigs.containsKey("derby")) {
 				config = availableConfigs.get("derby");
 			} else {
 				// just use the first found file
 				config = availableConfigs.values().iterator().next();
 			}
 		}		
 		
 		this.logger.info(String.format(
 				"Loading db configuration from '%s' ...",
 				config.toString()
 		));
 		return config;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void createAndRegisterCommandDB(URL hibernateConfigFile, BundleContext context) {
         // getting the Event-Admin service
         ServiceReference commandTrackerRef = context.getServiceReference(ICommandTracker.class.getName());
         ICommandTracker commandTracker = (commandTrackerRef == null) ? null :  (ICommandTracker) context.getService(commandTrackerRef);
         if (commandTracker == null) {
         	this.logger.warn("No CommandTracker-service found. Command-tracking will not work.");
         }		
 		
 		// getting the mapping files to use
 		Enumeration<URL> mappingFileEnum = context.getBundle().findEntries("/resources/hibernate/mapping/command/", "command.hbm.xml", true);
 		ArrayList<URL> mappings = Collections.list(mappingFileEnum);
 
 		// init command DB
 		this.commandDB = new CommandDB(hibernateConfigFile, mappings, commandTracker);		
 		
 		final Hashtable<String,Object> props = new Hashtable<String,Object>();
 		props.put(IDataSink.PROP_DATASINK_ID, ICommandDB.PROP_URL_ENQUEUE_SINK);
 		props.put(IDataProvider.PROP_DATAPROVIDER_ID, "org.paxle.crawler.sink");
 		props.put(EventConstants.EVENT_TOPIC, new String[] { CommandEvent.TOPIC_OID_REQUIRED });
 		props.put(Constants.SERVICE_PID, CommandDB.PID);
 		context.registerService(new String[]{
 				IDataSink.class.getName(),
 				IDataProvider.class.getName(),
 				ICommandDB.class.getName(),
 				EventHandler.class.getName(),
 				Monitorable.class.getName()
 		}, this.commandDB, props);		
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void createAndRegisterProfileDB(URL hibernateConfigFile, BundleContext context) {
 		// getting the mapping files to use
 		Enumeration<URL> mappingFileEnum = context.getBundle().findEntries("/resources/hibernate/mapping/profile/", "*.hbm.xml", true);
 		ArrayList<URL> mappings = Collections.list(mappingFileEnum);
 		
 		// create the profile-DB
 		this.profileDB = new CommandProfileDB(hibernateConfigFile, mappings);
 		
 		// register it to the framework
 		context.registerService(ICommandProfileManager.class.getName(), this.profileDB, null);
 	}
 	
 	private void createAndRegisterUrlExtractorFilter(BundleContext context) {		
 		Hashtable<String,Object> urlExtractorFilterProps = new Hashtable<String,Object>();
 		urlExtractorFilterProps.put(IFilter.PROP_FILTER_TARGET, new String[]{
 				String.format("org.paxle.parser.out; %s=%d",IFilter.PROP_FILTER_TARGET_POSITION,Integer.valueOf(Integer.MAX_VALUE))
 		});
 		urlExtractorFilterProps.put(IDataProvider.PROP_DATAPROVIDER_ID, ICommandDB.PROP_URL_ENQUEUE_SINK);
 		
 		this.urlExtractor = new UrlExtractorFilter();
 		
 		context.registerService(new String[] {
 				IFilter.class.getName(),
 				IDataProvider.class.getName()
 		}, this.urlExtractor, urlExtractorFilterProps);
 	}
 	
 	private void createAndRegisterCommandProfileFilter(BundleContext context) {		
 		Hashtable<String, String[]> profileFilterProps = new Hashtable<String, String[]>();
 		profileFilterProps.put(IFilter.PROP_FILTER_TARGET, new String[]{
 				String.format("org.paxle.crawler.in; %s=%d",IFilter.PROP_FILTER_TARGET_POSITION,Integer.valueOf(Integer.MIN_VALUE)),
 				String.format("org.paxle.parser.out; %s=%d",IFilter.PROP_FILTER_TARGET_POSITION,Integer.valueOf(65))
 		});
 		
 		this.profileFilter = new CommandProfileFilter(this.profileDB);
 		context.registerService(IFilter.class.getName(), this.profileFilter, profileFilterProps);	
 	}
 	
 	/**
 	 * This function is called by the osgi-framework to stop the bundle.
 	 * @see BundleActivator#stop(BundleContext)
 	 */		
 	public void stop(BundleContext context) throws Exception {
 		// shutdown URL-extractor
 		if (this.urlExtractor != null) {
 			this.urlExtractor.terminate();
 		}
 		
 		// shutdown command DB
 		if (this.commandDB != null) {
 			this.commandDB.close();
 		}		
 	}
 
 }
