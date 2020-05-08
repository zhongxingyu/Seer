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
 package org.openmrs.module.drawing;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.ModuleActivator;
 import org.openmrs.module.ModuleFactory;
 import org.openmrs.module.drawing.handlers.DrawingTagHandler;
 import org.openmrs.module.htmlformentry.HtmlFormEntryService;
 
 /**
  * This class contains the logic that is run every time this module is either started or stopped.
  */
 public class DrawingActivator implements ModuleActivator {
 	
 	protected Log log = LogFactory.getLog(getClass());
 	
 	/**
 	 * @see ModuleActivator#willRefreshContext()
 	 */
 	public void willRefreshContext() {
 		log.info("Refreshing Drawing Module");
 		
 	}
 	
 	/**
 	 * @see ModuleActivator#contextRefreshed()
 	 */
 	public void contextRefreshed() {
 		log.info("Drawing Module refreshed");
 		
 	}
 	
 	/**
 	 * @see ModuleActivator#willStart()
 	 */
 	public void willStart() {
 		log.info("Starting Drawing Module");
 		
 	}
 	
 	/**
 	 * @see ModuleActivator#started()
 	 */
 	public void started() {
 		log.info("Drawing Module started");
 		if (ModuleFactory.isModuleStarted("htmlformentry")) {
 			try {
 				HtmlFormEntryService hfes = Context.getService(HtmlFormEntryService.class);
 				hfes.addHandler("drawing", new DrawingTagHandler());
 				log.info("drawing : drawing tag registered");
 			}
 			catch (Exception ex) {
				
 				log.error("failed to register drawing tag in drawing", ex);
 			}
 		}
 	}
 	
 	/**
 	 * @see ModuleActivator#willStop()
 	 */
 	public void willStop() {
 		log.info("Stopping Drawing Module");
 	}
 	
 	/**
 	 * @see ModuleActivator#stopped()
 	 */
 	public void stopped() {
 		log.info("Drawing Module stopped");
 	}
 	
 }
