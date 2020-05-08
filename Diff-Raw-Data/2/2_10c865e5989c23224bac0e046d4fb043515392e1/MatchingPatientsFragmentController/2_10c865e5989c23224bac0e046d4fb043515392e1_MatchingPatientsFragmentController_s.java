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
 package org.openmrs.module.registrationapp.fragment.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.openmrs.Patient;
 import org.openmrs.PersonAddress;
 import org.openmrs.PersonName;
 import org.openmrs.module.appframework.domain.AppDescriptor;
 import org.openmrs.module.registrationapp.form.RegisterPatientFormBuilder;
 import org.openmrs.module.registrationapp.model.NavigableFormStructure;
 import org.openmrs.module.registrationcore.api.RegistrationCoreService;
 import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
 import org.openmrs.ui.framework.SimpleObject;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.annotation.BindParams;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  *
  */
 public class MatchingPatientsFragmentController {
 	
 	public List<SimpleObject> getSimilarPatients(@RequestParam("appId") AppDescriptor app,
 	                                             @SpringBean("registrationCoreService") RegistrationCoreService service,
 	                                             @ModelAttribute("patient") @BindParams Patient patient,
 	                                             @ModelAttribute("personName") @BindParams PersonName name,
 	                                             @ModelAttribute("personAddress") @BindParams PersonAddress address,
 	                                             HttpServletRequest request, UiUtils ui) throws Exception {
 		NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app);
 		
 		patient.addName(name);
 		patient.addAddress(address);
 		
 		if (formStructure != null) {
 			RegisterPatientFormBuilder.resolvePersonAttributeFields(formStructure, patient, request.getParameterMap());
 		}
		
 		List<PatientAndMatchQuality> matches = service.findFastSimilarPatients(patient, null, 2.0, 10);
 		
 		List<Patient> similarPatients = new ArrayList<Patient>();
 		for (PatientAndMatchQuality match : matches) {
 			similarPatients.add(match.getPatient());
 		}
 		
 		String[] propertiesToInclude = new String[] { "patientId", "givenName", "familyName",
 		        "patientIdentifier.identifier", "gender", "birthdate", "personAddress" };
 		
 		return SimpleObject.fromCollection(similarPatients, ui, propertiesToInclude);
 	}
 }
