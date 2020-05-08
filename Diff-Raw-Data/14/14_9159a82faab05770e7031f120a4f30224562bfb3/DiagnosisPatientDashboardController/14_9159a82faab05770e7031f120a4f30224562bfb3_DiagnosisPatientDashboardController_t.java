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
 package org.openmrs.module.diagnosiscapturerwanda;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Encounter;
 import org.openmrs.Patient;
 import org.openmrs.Visit;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller
 public class DiagnosisPatientDashboardController {
 	
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	//TODO:
 	@RequestMapping(value="/module/diagnosiscapturerwanda/diagnosisPatientDashboard",method=RequestMethod.GET)
     public String processDashboardPageGet(@RequestParam(value="patientId") Patient patient,  
     		@RequestParam(required=false, value="encounterUuid") String encounterUuid,
     		@RequestParam(required=false, value="encounterId") Integer encounterId,
     		@RequestParam(required=false, value="visitId") Integer visitId,
     		HttpSession session, ModelMap map){
 		map.put("patient", patient);
 		//find the Visit
 		Encounter registrationEnc = null;
 		if (encounterUuid != null){
 			registrationEnc = Context.getEncounterService().getEncounterByUuid(encounterUuid);
 		}
 		if (registrationEnc == null && encounterId != null){
 			registrationEnc = Context.getEncounterService().getEncounter(encounterId);
 		}
 		//this will throw exception if visit is not found.
 		Visit visit = null;
 		if (visitId != null)
 			visit = Context.getVisitService().getVisit(visitId);
 		if (visit == null)
 			visit = DiagnosisUtil.findVisit(registrationEnc, patient, session); //null encounter is handled by method 
 		map.put("visit", visit);
 		map.put("vitalsEncounterType", MetadataDictionary.ENCOUNTER_TYPE_VITALS);
 		map.put("diagnosisEncounterType", MetadataDictionary.ENCOUNTER_TYPE_DIAGNOSIS);
 		map.put("labEncounterType", MetadataDictionary.ENCOUNTER_TYPE_LABS);
 		map.put("registrationEncounterType", MetadataDictionary.ENCOUNTER_TYPE_REGISTRATION);
 		map.put("findingsEncounterType", MetadataDictionary.ENCOUNTER_TYPE_FINDINGS);
		
		
 		return null;
     }
 
     
 	//TODO:
     @RequestMapping(value="/module/diagnosiscapturerwanda/diagnosisPatientDashboard",method=RequestMethod.POST)
     public String processDashboardSubmit(){
     	return null;
     }
 
 }
