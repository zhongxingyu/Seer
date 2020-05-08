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
 package org.openmrs.module.updatecss.web.controller;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.updatecss.UpdateCSS;
 import org.openmrs.module.updatecss.api.UpdateCSSService;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.bind.annotation.ModelAttribute;
 
 /**
  * The main controller.
  */
 @Controller
 @SessionAttributes({"updateCSS"})
 public class  UpdateCSSManageController {
 	
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	//this method creates an UpdateCSS object the first time this controller is loaded
 	@ModelAttribute("updateCSS")
 	public UpdateCSS getPatientSearchObject(){
		System.out.println("CREATING NEW UpdateCSS OBJECT");
		return new UpdateCSS();
 	}
 	
 	//auto generated method from the initial creation of this module
 	@RequestMapping(value = "/module/updatecss/manage", method = RequestMethod.GET)
 	public void manage(ModelMap model) {
 		model.addAttribute("user", Context.getAuthenticatedUser());
 	}
 	
 	//class added by yuri to show how the manage.jsp form links to the controller
 	@RequestMapping(value = "module/updatecss/updateCSS", method = RequestMethod.POST)
 	public String updateCSSData(@ModelAttribute("updateCSS") UpdateCSS updateCSS){
 		UpdateCSSService cssService = Context.getService(UpdateCSSService.class);
 		//examples of how to call service methods
 		cssService.saveData(updateCSS);
		cssService.getData();
 		updateCSS.copyCssDataToTomcatDirectory(updateCSS.getHomeDirectory(), updateCSS.getCssData());
 		return "redirect:/module/updatecss/manage.form";
 	}
 }
