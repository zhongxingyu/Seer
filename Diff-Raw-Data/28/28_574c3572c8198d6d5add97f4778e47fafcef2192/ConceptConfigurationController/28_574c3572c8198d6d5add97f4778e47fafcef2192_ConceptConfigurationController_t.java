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
 
 package org.openmrs.module.odkconnector.web.controller.concept;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.odkconnector.api.ConceptConfiguration;
 import org.openmrs.module.odkconnector.api.service.ConnectorService;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller
 @RequestMapping("/module/odkconnector/concept/conceptConfiguration")
 public class ConceptConfigurationController {
 
 	private static final Log log = LogFactory.getLog(ConceptConfigurationController.class);
 
 	@RequestMapping(method = RequestMethod.GET)
 	public void preparePage(final @RequestParam(value = "uuid", required = false) String uuid,
 	                        final Model model) {
 		ConnectorService service = Context.getService(ConnectorService.class);
 		ConceptConfiguration conceptConfiguration = service.getConceptConfigurationByUuid(uuid);
 		model.addAttribute("configuration", conceptConfiguration);
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	public String process(final @RequestParam(value = "name", required = true) String name,
	                      final @RequestParam(value = "description", required = true) String description) {
 
 		ConceptConfiguration conceptConfiguration = new ConceptConfiguration();
 		conceptConfiguration.setName(name);
 		conceptConfiguration.setDescription(description);
 
 		ConnectorService service = Context.getService(ConnectorService.class);
 		service.saveConceptConfiguration(conceptConfiguration);
 
 		return "redirect:manageConcept.form?uuid=" + conceptConfiguration.getUuid();
 	}
 }
