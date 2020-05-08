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
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Patient;
 import org.openmrs.PersonAddress;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.PatientService;
 import org.openmrs.layout.web.address.AddressSupport;
 import org.openmrs.messagesource.MessageSourceService;
 import org.openmrs.module.appframework.domain.AppDescriptor;
 import org.openmrs.module.appui.UiSessionContext;
 import org.openmrs.module.registrationapp.RegistrationAppUiUtils;
 import org.openmrs.module.registrationapp.form.RegisterPatientFormBuilder;
 import org.openmrs.module.registrationapp.model.NavigableFormStructure;
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
 
 import javax.servlet.http.HttpServletRequest;
 
 public class EditPatientContactInfoPageController {
 	
 	protected final Log log = LogFactory.getLog(EditPatientContactInfoPageController.class);
 	
 	public void get(UiSessionContext sessionContext, PageModel model, @RequestParam("patientId") Patient patient,
 	                @RequestParam("appId") AppDescriptor app, @RequestParam(value = "returnUrl", required = false) String returnUrl,
 	                @SpringBean("adminService") AdministrationService administrationService) throws Exception {
 		
 		sessionContext.requireAuthentication();
 		
 		NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app);
		addModelAttributes(model, patient, formStructure, administrationService, returnUrl);
 	}
 	
 	/**
 	 * @should void the old person address and replace it with a new one when it is edited
 	 * @should void the old person address and replace it with a new one when it is edited
 	 * @should not void the existing address if there are no changes
 	 */
 	public String post(UiSessionContext sessionContext, PageModel model,
 	                   @RequestParam("patientId") @BindParams Patient patient, @BindParams PersonAddress address,
 	                   @SpringBean("patientService") PatientService patientService,
 	                   @RequestParam("appId") AppDescriptor app,
 					   @RequestParam("returnUrl") String returnUrl,
 	                   @SpringBean("adminService") AdministrationService administrationService, HttpServletRequest request,
 	                   @SpringBean("messageSourceService") MessageSourceService messageSourceService, Session session,
 	                   @SpringBean("patientValidator") PatientValidator patientValidator, UiUtils ui) throws Exception {
 		
 		sessionContext.requireAuthentication();
 		
 		if (patient.getPersonAddress() != null && address != null) {
 			PersonAddress currentAddress = patient.getPersonAddress();
 			if (!currentAddress.equalsContent(address)) {
 				//void the old address and replace it with the new one
 				patient.addAddress(address);
 				currentAddress.setVoided(true);
 			}
 		}
 		
 		NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app);
 		
 		BindingResult errors = new BeanPropertyBindingResult(patient, "patient");
 		patientValidator.validate(patient, errors);
 		RegistrationAppUiUtils.validateLatitudeAndLongitudeIfNecessary(address, errors);
 		
 		if (formStructure != null) {
 			RegisterPatientFormBuilder.resolvePersonAttributeFields(formStructure, patient, request.getParameterMap());
 		}
 		
 		if (!errors.hasErrors()) {
 			try {
 				//The person address changes get saved along as with the call to save patient
 				patientService.savePatient(patient);
 				InfoErrorMessageUtil.flashInfoMessage(request.getSession(),
 				    ui.message("registrationapp.editContactInfoMessage.success", patient.getPersonName()));
 				
 				return "redirect:" + returnUrl;
 			}
 			catch (Exception e) {
 				log.warn("Error occurred while saving patient's contact info", e);
 				session.setAttribute(UiCommonsConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE, "registrationapp.save.fail");
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
 		
		addModelAttributes(model, patient, formStructure, administrationService, returnUrl);
 		//redisplay the form
 		return null;
 	}
 	
 	private void addModelAttributes(PageModel model, Patient patient, NavigableFormStructure formStructure,
	                                AdministrationService adminService, String returnUrl) throws Exception {
 		
		model.addAttribute("returnUrl", returnUrl);
 		model.put("uiUtils", new RegistrationAppUiUtils());
 		model.addAttribute("patient", patient);
 		model.addAttribute("addressTemplate", AddressSupport.getInstance().getAddressTemplate().get(0));
 		model.addAttribute("formStructure", formStructure);
 		model.addAttribute("enableOverrideOfAddressPortlet",
 		    adminService.getGlobalProperty("addresshierarchy.enableOverrideOfAddressPortlet", "false"));
 	}
 }
