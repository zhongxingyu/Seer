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
 package org.openmrs.calculation.web.controller;
 
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Cohort;
 import org.openmrs.api.context.Context;
 import org.openmrs.calculation.CalculationRegistration;
 import org.openmrs.calculation.api.CalculationRegistrationService;
 import org.openmrs.calculation.api.patient.PatientCalculationService;
 import org.openmrs.calculation.patient.PatientCalculation;
 import org.openmrs.calculation.result.CohortResult;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  * Primary Controller for listing, deleting, testing Calculation Registrations
  */
 @Controller
 public class CalculationRegistrationController {
 	
 	/** Logger for this class and subclasses */
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	/**
 	 * Shows the page to list token registrations
 	 */
 	@RequestMapping(value = "/module/calculation/calculationRegistrations")
 	public void listCalculationRegistrations(Model model) {
 		CalculationRegistrationService calculationRegistrationService = Context.getService(CalculationRegistrationService.class);
 		model.addAttribute("calculationRegistrations", calculationRegistrationService.getAllCalculationRegistrations());
 	}
 	
 	/**
 	 * Page which tests patient calculations
 	 */
 	@RequestMapping(value = "/module/calculation/patientCalculationTest")
 	public void patientCalculationTest(Model model,
 									   @RequestParam(value="id", required=true) Integer id,
 									   @RequestParam(value="patientIds", required=false) String patientIds,
 									   @RequestParam(value="randomIds", required=false) Integer randomIds) {
 		model.addAttribute("id", id);
 		model.addAttribute("patientIds", patientIds);
 		model.addAttribute("randomIds", randomIds);
 		
 		try {
 			CalculationRegistrationService service = Context.getService(CalculationRegistrationService.class);
 			CalculationRegistration r = service.getCalculationRegistration(id);
 			model.addAttribute("calculationRegistration", r);
 			
 			PatientCalculation calculation = service.getCalculation(r.getToken(), PatientCalculation.class);
 			model.addAttribute("calculation", calculation);
 			
 			Cohort cohort = null;
 			if (StringUtils.isNotBlank(patientIds)) {
 				cohort = new Cohort(patientIds);
 			}
 			else if (randomIds != null) {
 				cohort = new Cohort();
 				String sql = "select patient_id from patient where voided = 0 limit " + randomIds;
 				for (List<Object> row : Context.getAdministrationService().executeSQL(sql, true)) {
 					cohort.addMember((Integer)row.get(0));
 				}
 				model.addAttribute("cohort", cohort);
 				
 				long startTime = System.currentTimeMillis();
 				CohortResult result = Context.getService(PatientCalculationService.class).evaluate(cohort, calculation);
 				long endTime = System.currentTimeMillis();
 				
 				model.addAttribute("result", result);
 				model.addAttribute("evaluationTime", endTime - startTime);
 			}
 		}
 		catch (Exception e) {
 			model.addAttribute("error", e);
 		}
 	}
 }
