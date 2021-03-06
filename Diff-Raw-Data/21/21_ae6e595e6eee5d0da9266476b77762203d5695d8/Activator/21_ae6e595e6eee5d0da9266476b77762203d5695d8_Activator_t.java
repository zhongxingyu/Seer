 /* 
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.felix.upnp.basedriver;
 
 import org.cybergarage.upnp.UPnP;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceRegistration;
 
 import org.apache.felix.upnp.basedriver.controller.DevicesInfo;
 import org.apache.felix.upnp.basedriver.controller.DriverController;
 import org.apache.felix.upnp.basedriver.controller.impl.DriverControllerImpl;
 import org.apache.felix.upnp.basedriver.export.RootDeviceExportingQueue;
 import org.apache.felix.upnp.basedriver.export.RootDeviceListener;
 import org.apache.felix.upnp.basedriver.export.ThreadExporter;
 import org.apache.felix.upnp.basedriver.importer.core.MyCtrlPoint;
 import org.apache.felix.upnp.basedriver.importer.core.event.structs.Monitor;
 import org.apache.felix.upnp.basedriver.importer.core.event.structs.NotifierQueue;
 import org.apache.felix.upnp.basedriver.importer.core.event.structs.SubscriptionQueue;
 import org.apache.felix.upnp.basedriver.importer.core.event.thread.Notifier;
 import org.apache.felix.upnp.basedriver.importer.core.event.thread.SubScriber;
 import org.apache.felix.upnp.basedriver.tool.Logger;
 import org.apache.felix.upnp.basedriver.util.Constants;
 
 /* 
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
 public class Activator implements BundleActivator {
 	
 	
 	public static BundleContext bc;
     public static Logger logger;        
 	private RootDeviceExportingQueue queue;
 	private RootDeviceListener producerDeviceToExport;
 	private ThreadExporter consumerDeviceToExport;
 
 	private MyCtrlPoint ctrl;
 	private SubScriber subScriber;
 	private Notifier notifier;
 	private NotifierQueue notifierQueue;
 	private SubscriptionQueue subQueue;
 	private Monitor monitor;
     private DriverControllerImpl drvController;
     private ServiceRegistration drvControllerRegistrar;
     
 	
 	/**
 	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		//Setting basic variabile used by everyone
         
  		Activator.bc = context;				
 		
  		doInitLogger();
  		
  		doInitUPnPStack();
  		
  		doInitExporter();
  		
  		doInitImporter();
  		
         doControllerRegistration();
         
 	}
 
 
     /**
 	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
         
         drvControllerRegistrar.unregister();
         
 		//Base Driver Exporter
         if (consumerDeviceToExport != null) {
 			consumerDeviceToExport.end();
 			consumerDeviceToExport.cleanUp();
 			producerDeviceToExport.deactive();
         }
 
 		//Base Driver Importer
         if (ctrl != null){
 			ctrl.stop();
 			subScriber.close();
 			notifier.close();
         }
         
 		Activator.logger.close();
 		Activator.logger=null;
 		Activator.bc = null;
 	}
 	
 	public final String getPropertyDefault(BundleContext bc, String propertyName, String defaultValue ){
 		String value = bc.getProperty(propertyName);
 		if(value == null)
 			return defaultValue;
 		return value;
 	}
 
 	/**
 	 * Method used for initilizing the general properties of the UPnP Base Driver
 	 * 
 	 * @since 0.3
 	 */
 	private void doInitLogger() {
 		
  	    String levelStr = getPropertyDefault(Activator.bc,Constants.BASEDRIVER_LOG_PROP,"2");	    
 		Activator.logger = new Logger(levelStr);
 		
 	    String cyberLog = getPropertyDefault(Activator.bc,Constants.CYBERDOMO_LOG_PROP,"false");
 	    Activator.logger.setCyberDebug(cyberLog);	    
 
 	}
 
 	/**
 	 * Method used for initilizing the UPnP SDK component used by the UPnP Base Driver
 	 * 
 	 * @since 0.3
 	 */
 	private void doInitUPnPStack() {
		boolean useOnlyIPV4 = Boolean.valueOf(getPropertyDefault(Activator.bc,Constants.NET_ONLY_IPV4_PROP,"true")).booleanValue();
 	    if (useOnlyIPV4) UPnP.setEnable(UPnP.USE_ONLY_IPV4_ADDR);
     	else UPnP.setDisable(UPnP.USE_ONLY_IPV4_ADDR);
 
		boolean useOnlyIPV6 = Boolean.valueOf(getPropertyDefault(Activator.bc,Constants.NET_ONLY_IPV6_PROP,"true")).booleanValue();
 	    if (useOnlyIPV6) UPnP.setEnable(UPnP.USE_ONLY_IPV6_ADDR);
     	else UPnP.setDisable(UPnP.USE_ONLY_IPV6_ADDR);
 
		boolean useLoopback = Boolean.valueOf(getPropertyDefault(Activator.bc,Constants.NET_USE_LOOPBACK_PROP,"true")).booleanValue();
     	if (useLoopback) UPnP.setEnable(UPnP.USE_LOOPBACK_ADDR);
     	else UPnP.setDisable(UPnP.USE_LOOPBACK_ADDR);
     	
 	}
 
 	/**
 	/**
 	 * Method used for initilizing the Exporter component of the UPnP Base Driver
 	 * @throws InvalidSyntaxException 
 	 * 
 	 * @since 0.3
 	 * @throws InvalidSyntaxException
 	 */
 	private void doInitExporter() throws InvalidSyntaxException {		
		boolean useExporter = Boolean.valueOf(getPropertyDefault(Activator.bc,Constants.EXPORTER_ENABLED_PROP,"true")).booleanValue();
       	if (!useExporter) return;
    		      	
 		this.queue = new RootDeviceExportingQueue();
 		this.producerDeviceToExport = new RootDeviceListener(queue);
 		producerDeviceToExport.activate();
 		consumerDeviceToExport = new ThreadExporter(queue);
 		new Thread(consumerDeviceToExport, "upnp.basedriver.Exporter").start();
        	
 	}
 	
 	/**
 	 * Method used for initilizing the Import component of the UPnP Base Driver
 	 * 
 	 * @since 0.3
 	 */
 	private void doInitImporter() {
		boolean useImporter = Boolean.valueOf(getPropertyDefault(Activator.bc,Constants.IMPORTER_ENABLED_PROP,"true")).booleanValue();
       	if (!useImporter) return;
    		
    		
 		//Setting up Base Driver Importer
 		this.notifierQueue = new NotifierQueue();
 		this.subQueue = new SubscriptionQueue();
 		ctrl = new MyCtrlPoint(Activator.bc, subQueue, notifierQueue);
 		
 		//Enable CyberLink re-new for Event
 		ctrl.setNMPRMode(true);
 			
 		this.monitor=new Monitor();
 		this.notifier = new Notifier(notifierQueue,monitor);
 		this.subScriber = new SubScriber(ctrl, subQueue,monitor);
 		
 		ctrl.start();
 		subScriber.start();
 		notifier.start();
 	}
 
 
 
 	private void doControllerRegistration() {
         drvController = new DriverControllerImpl(ctrl);
         drvControllerRegistrar = bc.registerService(
             new String[]{
             		DriverController.class.getName(),
             		DevicesInfo.class.getName()},
             drvController,
             null
         );       
     }
 	
 	
 }
