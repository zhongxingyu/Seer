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
 package org.openmrs.module.dispensing;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.FormService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.Module;
 import org.openmrs.module.ModuleActivator;
 import org.openmrs.module.ModuleFactory;
 import org.openmrs.module.htmlformentry.HtmlFormEntryService;
 import org.openmrs.module.htmlformentryui.HtmlFormUtil;
 import org.openmrs.ui.framework.resource.ResourceFactory;
 
 /**
  * This class contains the logic that is run every time this module is either started or stopped.
  */
 public class DispensingActivator implements ModuleActivator {
 	
 	protected Log log = LogFactory.getLog(getClass());
 		
 	/**
 	 * @see ModuleActivator#willRefreshContext()
 	 */
 	public void willRefreshContext() {
 		log.info("Refreshing Dispensing Module");
 	}
 	
 	/**
 	 * @see ModuleActivator#contextRefreshed()
 	 */
 	public void contextRefreshed() {
 		log.info("Dispensing Module refreshed");
 	}
 	
 	/**
 	 * @see ModuleActivator#willStart()
 	 */
 	public void willStart() {
 		log.info("Starting Dispensing Module");
 	}
 	
 	/**
 	 * @see ModuleActivator#started()
 	 */
 	public void started() {
 
         try {
             // load the dispensing form
             setupHtmlForms();
         }
         catch (Exception e) {
             Module mod = ModuleFactory.getModuleById(DispensingApiConstants.DISPENSING_MODULE_ID);
             ModuleFactory.stopModule(mod);
             throw new RuntimeException("failed to setup the required modules", e);
         }
 
		log.info("Dispensing Module started");
	}
	
	/**
	 * @see ModuleActivator#willStop()
	 */
	public void willStop() {
 		log.info("Stopping Dispensing Module");
 	}
 	
 	/**
 	 * @see ModuleActivator#stopped()
 	 */
 	public void stopped() {
 		log.info("Dispensing Module stopped");
 	}
 
     private void setupHtmlForms() throws Exception {
         try {
             ResourceFactory resourceFactory = ResourceFactory.getInstance();
             FormService formService = Context.getFormService();
             HtmlFormEntryService htmlFormEntryService = Context.getService(HtmlFormEntryService.class);
             HtmlFormUtil.getHtmlFormFromUiResource(resourceFactory, formService, htmlFormEntryService, DispensingApiConstants.DISPENSING_HTML_FORM);
         }
         catch (Exception e) {
             // this is a hack to get component test to pass until we find the proper way to mock this
             if (ResourceFactory.getInstance().getResourceProviders() == null) {
                 log.error("Unable to load HTML forms--this error is expected when running component tests");
             }
             else {
                 throw e;
             }
         }
     }
 		
 }
