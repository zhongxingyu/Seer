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
 package org.openmrs.module.registrationapp.page.controller;
 
import javax.servlet.http.HttpServletRequest;

 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Patient;
 import org.openmrs.PersonName;
 import org.openmrs.api.PatientService;
 import org.openmrs.layout.web.name.NameTemplate;
 import org.openmrs.messagesource.MessageSourceService;
 import org.openmrs.module.appui.UiSessionContext;
 import org.openmrs.module.uicommons.UiCommonsConstants;
 import org.openmrs.module.uicommons.util.InfoErrorMessageUtil;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.annotation.BindParams;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.ui.framework.page.PageModel;
 import org.openmrs.ui.framework.session.Session;
 import org.openmrs.validator.PatientValidator;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.ObjectError;
 import org.springframework.web.bind.annotation.RequestParam;
 
 public class EditPatientDemographicsPageController {
 	
 	protected final Log log = LogFactory.getLog(EditPatientDemographicsPageController.class);
 	
 	public void get(UiSessionContext sessionContext, PageModel model, @RequestParam("patientId") Patient patient,
 	                @SpringBean("nameTemplateGivenFamily") NameTemplate nameTemplate) throws Exception {
 		
 		sessionContext.requireAuthentication();
 		model.addAttribute("patient", patient);
 		model.addAttribute("nameTemplate", nameTemplate);
 	}
 	
 	/**
 	 * @should void the old person name and replace it with a new one when it is edited
 	 * @should not void the existing name if there are no changes in the name
 	 */
 	public String post(UiSessionContext sessionContext, PageModel model,
 	                   @SpringBean("patientService") PatientService patientService,
 	                   @RequestParam("patientId") @BindParams Patient patient, @BindParams PersonName name,
 	                   @SpringBean("nameTemplateGivenFamily") NameTemplate nameTemplate, HttpServletRequest request,
 	                   @SpringBean("messageSourceService") MessageSourceService messageSourceService, Session session,
 	                   @SpringBean("patientValidator") PatientValidator patientValidator, UiUtils ui) throws Exception {
 		
 		sessionContext.requireAuthentication();
 		
 		if (patient.getPersonName() != null && name != null) {
 			PersonName currentName = patient.getPersonName();
 			if (!currentName.equalsContent(name)) {
 				//void the old name and replace it with the new one
 				patient.addName(name);
 				currentName.setVoided(true);
 			}
 		}
 		
 		BindingResult errors = new BeanPropertyBindingResult(patient, "patient");
 		patientValidator.validate(patient, errors);
 		
 		if (!errors.hasErrors()) {
 			try {
 				patientService.savePatient(patient);
 				InfoErrorMessageUtil.flashInfoMessage(request.getSession(),
 				    ui.message("registrationapp.editDemographicsMessage.success", patient.getPersonName()));
 				
 				return "redirect:coreapps/patientdashboard/patientDashboard.page?patientId=" + patient.getPatientId();
 			}
 			catch (Exception e) {
 				log.warn("Error occurred while saving patient demographics", e);
 			}
 			
 		} else {
 			model.addAttribute("errors", errors);
 			StringBuffer errorMessage = new StringBuffer(messageSourceService.getMessage("error.failed.validation"));
 			errorMessage.append("<ul>");
 			for (ObjectError error : errors.getAllErrors()) {
 				errorMessage.append("<li>");
 				errorMessage.append(messageSourceService.getMessage(error.getCode(), error.getArguments(),
 				    error.getDefaultMessage(), null));
 				errorMessage.append("</li>");
 			}
 			errorMessage.append("</ul>");
 			session.setAttribute(UiCommonsConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE, errorMessage.toString());
 		}
 		
 		model.addAttribute("patient", patient);
 		model.addAttribute("nameTemplate", nameTemplate);
 		//redisplay the form
 		return null;
 	}
 	
 }
