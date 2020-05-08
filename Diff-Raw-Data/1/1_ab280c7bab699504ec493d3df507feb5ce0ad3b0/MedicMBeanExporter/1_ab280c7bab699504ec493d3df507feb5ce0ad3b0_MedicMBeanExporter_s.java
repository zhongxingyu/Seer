 /*******************************************************************************
  * Copyright (c) 2008, 2011 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 package org.eclipse.virgo.medic.management;
 
 import java.lang.management.ManagementFactory;
 
 import javax.management.MBeanServer;
 import javax.management.ObjectInstance;
 import javax.management.ObjectName;
 
 import org.eclipse.virgo.medic.dump.DumpGenerator;
 import org.eclipse.virgo.medic.impl.config.ConfigurationProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 
  * 
  * 
  * This class is Thread Safe
  *
  */
 public class MedicMBeanExporter {
 	
     private final Logger logger = LoggerFactory.getLogger(MedicMBeanExporter.class);
 
     private static final String DOMAIN = "org.eclipse.virgo.kernel";
     
     private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
 
 	private ObjectInstance registeredMBean;
 
     /**
      * 
      * @param serverHome
      */
 	public MedicMBeanExporter(ConfigurationProvider configurationProvider, DumpGenerator dumpGenerator) {
 		try {
 			ObjectName dumpMBeanName = new ObjectName(String.format("%s:type=Medic,name=DumpInspector", DOMAIN));
 			String dumpDirectory = configurationProvider.getConfiguration().get(ConfigurationProvider.KEY_DUMP_ROOT_DIRECTORY);
			System.out.println("dump dir" + dumpDirectory);
 			registeredMBean = this.server.registerMBean(new FileSystemDumpInspector(dumpGenerator, dumpDirectory), dumpMBeanName);
 		} catch (Exception e) {
 			logger.error("Unable to register the DumpInspectorMBean", e);
 		} 
 	}
 	
 	/**
 	 * 
 	 */
 	public void close(){
 		ObjectInstance localRegisteredMBean = this.registeredMBean;
 		if(localRegisteredMBean != null){
 			try {
 				this.server.unregisterMBean(localRegisteredMBean.getObjectName());
 				this.registeredMBean = null;
 			} catch (Exception e) {
 				logger.error("Unable to unregister MBean", e);
 			} 
 		}
 	}
 	
 }
