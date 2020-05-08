 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.core.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.Locale;
 import java.util.Properties;
 import java.util.ResourceBundle;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.Filter;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedService;
 import org.osgi.service.event.EventAdmin;
 import org.osgi.service.event.EventConstants;
 import org.osgi.service.event.EventHandler;
 import org.osgi.service.metatype.MetaTypeProvider;
 import org.osgi.service.monitor.Monitorable;
 import org.osgi.service.prefs.PreferencesService;
 import org.paxle.core.ICryptManager;
 import org.paxle.core.IMWComponentFactory;
 import org.paxle.core.data.IDataConsumer;
 import org.paxle.core.data.IDataProvider;
 import org.paxle.core.data.IDataSink;
 import org.paxle.core.data.IDataSource;
 import org.paxle.core.data.impl.DataListener;
 import org.paxle.core.data.impl.DataManager;
 import org.paxle.core.filter.IFilter;
 import org.paxle.core.filter.IFilterManager;
 import org.paxle.core.filter.impl.AscendingPathUrlExtractionFilter;
 import org.paxle.core.filter.impl.FilterListener;
 import org.paxle.core.filter.impl.FilterManager;
 import org.paxle.core.io.IOTools;
 import org.paxle.core.io.IResourceBundleTool;
 import org.paxle.core.io.impl.ResourceBundleTool;
 import org.paxle.core.io.impl.ResourceBundleToolFactory;
 import org.paxle.core.io.temp.ITempFileManager;
 import org.paxle.core.io.temp.impl.CommandTempReleaser;
 import org.paxle.core.io.temp.impl.TempFileManager;
 import org.paxle.core.monitorable.observer.impl.MonitorableObserver;
 import org.paxle.core.monitorable.observer.impl.ObserverEventSenderConcequence;
 import org.paxle.core.monitorable.observer.impl.ObserverFilterCondition;
 import org.paxle.core.monitorable.observer.impl.ObserverMethodExecutorConcequence;
 import org.paxle.core.monitorable.observer.impl.ObserverRule;
 import org.paxle.core.norm.IReferenceNormalizer;
 import org.paxle.core.norm.impl.ReferenceNormalizer;
 import org.paxle.core.norm.impl.URLStreamHandlerListener;
 import org.paxle.core.prefs.IPropertiesStore;
 import org.paxle.core.prefs.impl.PropertiesStore;
 import org.paxle.core.queue.CommandEvent;
 import org.paxle.core.queue.ICommand;
 import org.paxle.core.queue.ICommandTracker;
 import org.paxle.core.queue.impl.CommandTracker;
 
 public class Activator implements BundleActivator, InvocationHandler {
 	
 	/**
 	 * A class to manage {@link IFilter filters}
 	 */
 	private FilterManager filterManager = null;
 	
 	/**
 	 * A class to manage:
 	 * <ul>
 	 * 	<li>{@link IDataSource}</li>
 	 * 	<li>{@link IDataSink}</li>
 	 * 	<li>{@link IDataProvider}</li>
 	 * 	<li>{@link IDataConsumer}</li>
 	 * </ul>
 	 */
 	private DataManager<ICommand> dataManager = null;
 	
 	/**
 	 * A component providing cypt-functions, e.g. 
 	 * to genererate MD5 checksums, etc.
 	 */
 	private CryptManager cryptManager = null;
 	
 	/**
 	 * A component to create (and cleanup) temp-files
 	 */
 	private TempFileManager tempFileManager = null;
 	
 	/**
 	 * A component to normalize URLs
 	 */
 	private ReferenceNormalizer referenceNormalizer = null;
 	
 	/**
 	 * A component used to track {@link org.paxle.core.queue.Command commands}
 	 */
 	private CommandTracker commandTracker = null;
 	
 	/**
 	 * This component releases all the temporary files associated with a command on desctruction of the latter object
 	 */
 	private CommandTempReleaser commandReleaser = null;
 	
 	/**
 	 * For logging
 	 */
 	private Log logger;
 	
 	/**
 	 * A tool to get all {@link Locale} for which a translation file exists for a given
 	 * {@link ResourceBundle} basename.
 	 */
 	private IResourceBundleTool rbTool = null;
 	
 	/**
 	 * A wrapper around the OSGI {@link PreferencesService}
 	 */
 	private IPropertiesStore propertyStore = null;
 	
 	/**
 	 * This function is called by the osgi-framework to start the bundle.
 	 * @see BundleActivator#start(BundleContext) 
 	 */	
 	@SuppressWarnings("serial")
 	public void start(BundleContext bc) throws Exception {
 		// init logger
 		this.logger = LogFactory.getLog(this.getClass());
 		
 		// configuring some system properties
 		System.setProperty("paxle.version", (String) bc.getBundle().getHeaders().get(Constants.BUNDLE_VERSION));
 		
 		// starting paxle
 		logger.info("Starting ...");
 		System.out.println(
 				"\t    ____             __    \r\n" +
 				"\t   / __ \\____ __  __/ /__  \r\n" +
 				"\t  / /_/ / __ `/ |/_/ / _ \\ \r\n" +
 				"\t / ____/ /_/ />  </ /  __/ \r\n" +
 			    "\t/_/    \\__,_/_/|_/_/\\___/ \r\n" +
 			    "\r\n" +
 				"\tVersion: " + bc.getBundle().getHeaders().get(Constants.BUNDLE_VERSION)
 		);		
 				
 		// processing data-path
 		DataPathSettings.validateDataPathSettings();
 		this.logger.info("Using data-path: " + System.getProperty("paxle.data"));
 		
 		this.rbTool = new ResourceBundleTool(bc.getBundle());
 		
 		this.propertyStore = this.createAndRegisterPropertyStore(bc);
 		
 		this.filterManager = this.createAndRegisterFilterManager(bc, this.rbTool, this.propertyStore);
 		
 		this.createAndRegisterRuntimeSettings(bc);
 		
 		dataManager = new DataManager<ICommand>();
		tempFileManager = new TempFileManager(false);
 		cryptManager = new CryptManager();
 		referenceNormalizer = new ReferenceNormalizer();
 
 		
 		/* ==========================================================
 		 * Register Service Listeners
 		 * ========================================================== */
 		// register the filter listener
 		bc.addServiceListener(new FilterListener(this.filterManager,this.tempFileManager,this.referenceNormalizer,bc),FilterListener.FILTER);		
 		
 		// register a data-source/sink- and data-producer/consumer-listener
 		DataListener dataListener = new DataListener(dataManager,bc);
 		bc.addServiceListener(dataListener);
 //		bc.addServiceListener(dataListener,DataListener.DATASOURCE_FILTER);
 //		bc.addServiceListener(dataListener,DataListener.DATASINK_FILTER);
 //		bc.addServiceListener(dataListener,DataListener.DATAPROVIDER_FILTER);
 //		bc.addServiceListener(dataListener,DataListener.DATACONSUMER_FILTER);
 		
 		final CryptListener cryptListener = new CryptListener(bc, this.cryptManager);
 		bc.addServiceListener(cryptListener, CryptListener.FILTER);
 		
 		/* ==========================================================
 		 * Register Services
 		 * ========================================================== */
 		// register runtime-memory monitorable
 		this.createAndRegisterMonitorableObservers(bc);
 		
 		Hashtable<String, String> tempFileManagerProps = new Hashtable<String, String>();
 		tempFileManagerProps.put(Constants.SERVICE_PID, TempFileManager.MONITOR_PID);
 		tempFileManagerProps.put("Monitorable-Localization", "/OSGI-INF/l10n/TempFileManager");
 		bc.registerService(new String[]{Monitorable.class.getName(), ITempFileManager.class.getName()}, this.tempFileManager, tempFileManagerProps);
 		
 		// register the master-worker-factory as a service
 		bc.registerService(IMWComponentFactory.class.getName(), new MWComponentServiceFactory(
 				rbTool.getLocaleArray(MWComponent.class.getSimpleName(), Locale.ENGLISH)), null);
 		
 		// register crypt-manager
 		bc.registerService(ICryptManager.class.getName(), this.cryptManager, null);
 		IOTools.setTempFileManager(this.tempFileManager);
 		
 		// register protocol-handlers listener which updates the table of known protocols for the reference normalization filter below
 		final ServiceListener protocolUpdater = new URLStreamHandlerListener(bc, ReferenceNormalizer.DEFAULT_PORTS);
 		bc.addServiceListener(protocolUpdater, URLStreamHandlerListener.FILTER);
 		
 		// add reference normalizer service
         bc.registerService(IReferenceNormalizer.class.getName(), this.referenceNormalizer, null);
         
         // register rb-tool
         bc.registerService(IResourceBundleTool.class.getName(), new ResourceBundleToolFactory(), null);
         
         // add AscendingPathUrlExtraction filter
         bc.registerService(IFilter.class.getName(), new AscendingPathUrlExtractionFilter(), new Hashtable<String, Object>(){{
         	// service ID
         	put(Constants.SERVICE_PID, AscendingPathUrlExtractionFilter.class.getName());
         	
         	// filter properties
         	put(IFilter.PROP_FILTER_TARGET, new String[] {
             		// apply filter to the parser-output-queue at position 60
             		String.format("org.paxle.parser.out; %s=%d;",IFilter.PROP_FILTER_TARGET_POSITION, Integer.valueOf(60))
             });
         	
         	// meta-data service properties
             put("org.paxle.metadata",Boolean.TRUE);
             put("org.paxle.metadata.localization","/OSGI-INF/l10n/AscendingPathUrlExtractionFilter");        	
         }});
         
         // getting the Event-Admin service
         ServiceReference eventAdminRef = bc.getServiceReference(EventAdmin.class.getName());
         if (eventAdminRef != null) {
         	EventAdmin eventAdmin = (EventAdmin) bc.getService(eventAdminRef);
         	
 	        // the command-tracker
 	        final Hashtable<String, Object> trackerProps = new Hashtable<String, Object>();
 	        trackerProps.put(EventConstants.EVENT_TOPIC, new String[]{CommandEvent.TOPIC_ALL});
 	        bc.registerService(new String[]{EventHandler.class.getName(),ICommandTracker.class.getName()}, this.commandTracker = new CommandTracker(eventAdmin), trackerProps);
 	        
 	        // the command temp releaser
 	        commandReleaser = new CommandTempReleaser(tempFileManager, commandTracker);
 	        final Hashtable<String,Object> releaserProps = new Hashtable<String,Object>();
 	        releaserProps.put(EventConstants.EVENT_TOPIC, new String[] { CommandEvent.TOPIC_DESTROYED });
 	        bc.registerService(EventHandler.class.getName(), commandReleaser, releaserProps);
         } else {
         	this.logger.warn("No EventAdmin-service found. Command-tracking will not work.");
         }
         
         this.initEclipseApplication(bc);
 	}
 		
 	@SuppressWarnings("serial")
 	private void createAndRegisterMonitorableObservers(BundleContext bc) throws InvalidSyntaxException, SecurityException, NoSuchMethodException {
 		/*
 		 * CPU usage observer
 		 */
 		/*
         Hashtable<String, Object> observerProps = new Hashtable<String, Object>();
         observerProps.put("mon.observer.listener.id","org.paxle.crawler");
         observerProps.put("org.paxle.crawler.master.delay.delta", new Integer(100));
         
         new MonitorableObserverEventSender(bc, "(&(org.paxle.crawler/status.paused=false)(os.usage.cpu/cpu.usage.total >= 0.8))", observerProps);
         */
 		
 		/*
 		 * Memory observer
 		 * XXX: should be configurable via CM
 		 */
         Filter gcFilter = bc.createFilter("(java.lang.runtime/memory.free <= " + 20*1024*1024 + ")");
         Filter oomFilter = bc.createFilter("(&(org.paxle.crawler/status.paused=false)(|(java.lang.runtime/memory.free <= " + 10*1024*1024 + ")(os.disk/disk.space.free<=1024)))");
         Filter oomResolvedFilter = bc.createFilter("(&(org.paxle.crawler/status.paused=true)(org.paxle.crawler/state.code=PAUSED_BY_OOM_CHECK)(java.lang.runtime/memory.free >= " + 11*1024*1024 + ")(os.disk/disk.space.free>=1024))");
         
         new MonitorableObserver(
         		bc, 
         		new ObserverRule(
         				// Filter based condition
         				new ObserverFilterCondition(gcFilter),
         				// triggers an event
         				new ObserverMethodExecutorConcequence(System.class.getMethod("gc", (Class[])null), null, null)
         		),
         		new ObserverRule(
         				// Filter based condition
         				new ObserverFilterCondition(oomFilter),
         				// triggers an event
         				new ObserverEventSenderConcequence(bc, new Hashtable<String, Object>(){{
         			        put("mon.observer.listener.id","org.paxle.crawler");
         			        put("org.paxle.crawler.state.active", Boolean.FALSE);
         			        put("org.paxle.crawler.state.code","PAUSED_BY_OOM_CHECK");
         				}})
         		),
         		new ObserverRule(
         				// Filter based condition
         				new ObserverFilterCondition(oomResolvedFilter),
         				// triggers an event
         				new ObserverEventSenderConcequence(bc, new Hashtable<String, Object>(){{
         			        put("mon.observer.listener.id","org.paxle.crawler");
         			        put("org.paxle.crawler.state.active", Boolean.TRUE);
         			        put("org.paxle.crawler.state.code","OK");
         				}})
         		)
         );      
 	}
 	
 	private IPropertiesStore createAndRegisterPropertyStore(BundleContext bc) {
 		// create a new store
 		IPropertiesStore propStore = new PropertiesStore();
 		
 		// register as OSGI service
 		bc.registerService(IPropertiesStore.class.getName(), propStore, null);
 				
 		return propStore;
 	}
 	
 	private FilterManager createAndRegisterFilterManager(BundleContext bc, IResourceBundleTool rbTool, IPropertiesStore propStore) throws IOException, ConfigurationException {
 		// getting the CM service
 		final ServiceReference cmRef = bc.getServiceReference(ConfigurationAdmin.class.getName());
 		final ConfigurationAdmin cm = (ConfigurationAdmin) bc.getService(cmRef);		
 		
 		// getting all locale for the manager
 		String[] localeArray = rbTool.getLocaleArray(IFilterManager.class.getSimpleName(),Locale.ENGLISH);
 		
 		// getting the core-bundle properties
 		Properties props = propStore.getProperties(bc);
 		
 		// creating filter-manager
 		FilterManager fManager = new FilterManager(
 				localeArray, 
 				cm.getConfiguration(FilterManager.PID),
 				bc,
 				props
 		);
 		
 		// managed- and metatype-provider-service properties
 		Hashtable<String, Object> fManagerProps = new Hashtable<String, Object>();
 		fManagerProps.put(Constants.SERVICE_PID, FilterManager.PID);		
 		
 		// register the filter-manager as service
 		bc.registerService(IFilterManager.class.getName(), fManager, null);
 		bc.registerService(new String[]{ManagedService.class.getName(), MetaTypeProvider.class.getName()}, fManager, fManagerProps);
 		
 		return fManager;
 	}
 	
 	private void createAndRegisterRuntimeSettings(BundleContext bc) throws IOException {
 		File iniFile = new File("start.ini");
 		
 		// getting all locale for the manager
 		String[] localeArray = rbTool.getLocaleArray(RuntimeSettings.class.getSimpleName(),Locale.ENGLISH);
 		
 		// creating component
 		final RuntimeSettings rts = new RuntimeSettings(localeArray, iniFile);
 		
 		// managed- and metatype-provider-service properties
 		Hashtable<String, Object> props = new Hashtable<String, Object>();
 		props.put(Constants.SERVICE_PID, RuntimeSettings.PID);		
 		
 		// register as service
 		bc.registerService(new String[]{ManagedService.class.getName(), MetaTypeProvider.class.getName()}, rts, props);
 		
 		// getting the CM service
 		final ServiceReference cmRef = bc.getServiceReference(ConfigurationAdmin.class.getName());
 		final ConfigurationAdmin cm = (ConfigurationAdmin) bc.getService(cmRef);
 		
 		// updating the props with the current ini-file settings
 		final Dictionary<?,?> iniProps = rts.getCurrentIniSettings();
 		cm.getConfiguration(RuntimeSettings.PID).update(iniProps);
 	}
 
 	private void initEclipseApplication(BundleContext bc) {
 		String osgiFrameworkVendor = bc.getProperty(Constants.FRAMEWORK_VENDOR);
 		if (osgiFrameworkVendor.equalsIgnoreCase("Eclipse")) {
 			 final Hashtable<String,Object> props = new Hashtable<String,Object>();
 			 props.put("eclipse.application", "org.paxle.app");
 			
 			 try {
 				 // we need to load the inferface classes we will implement from the system bundle
 				 Class<?> applicationRunnable = bc.getBundle(0).loadClass("org.eclipse.osgi.service.runnable.ApplicationRunnable");
 				 Class<?> parameterizedRunnable = bc.getBundle(0).loadClass("org.eclipse.osgi.service.runnable.ParameterizedRunnable");
 				 
 				 // now we play the role of an eclipse application
 				 Object proxy = Proxy.newProxyInstance(
 						 bc.getClass().getClassLoader(),
 						 new Class[]{applicationRunnable, parameterizedRunnable},
 						 this);
 			        				 
 				 // registering as Equinox application	        
 				 bc.registerService(new String[]{
 						 "org.eclipse.osgi.service.runnable.ApplicationRunnable",
 						 "org.eclipse.osgi.service.runnable.ParameterizedRunnable"
 				 }, proxy, props);
 			 } catch (Exception e) {
 				 e.printStackTrace();
 			 }
 		}
 	}
 	
 	/**
 	 * This function is called by the osgi-framework to stop the bundle.
 	 * @see BundleActivator#stop(BundleContext)
 	 */		
 	public void stop(BundleContext context) throws Exception {
 		IOTools.setTempFileManager(null);
 		// cleanup
 		this.tempFileManager = null;
 		this.referenceNormalizer = null;
 		this.dataManager.close();
 		this.dataManager = null;
 		this.filterManager.close();
 		this.filterManager = null;
 		if (this.commandTracker != null) {
 			this.commandTracker.terminate();
 		}
 		this.commandReleaser = null;
 	}
 
 	/**
 	 * @see {@link org.eclipse.osgi.service.runnable.ParameterizedRunnable#run(Object)}
 	 * @see {@link org.eclipse.osgi.service.runnable.ApplicationRunnable#stop()}
 	 */
 	public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 		if (method.getName().equals("run")) {
 			// wait until we need to shutdown
 			this.wait();
 			
 			// return exit code
 			return Integer.valueOf(0);
 		} else if (method.getName().equals("stop")) {
 			// signal equinox to shutdown
 			System.setProperty("osgi.noShutdown", "false");
 			
 			// wakeup main thread
 			this.notifyAll();
 			
 			// return void
 			return null;
 		} else {
 			throw new IllegalArgumentException(String.format("Unknown function call %s", method.toString()));
 		}
 	}
 }
