 /*
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
 
 package org.openmrs.module.patientregistration.controller.workflow;
 
 import org.junit.Test;
 import org.openmrs.Patient;
 import org.openmrs.Visit;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.emr.adt.AdtService;
 import org.openmrs.module.patientregistration.PatientRegistrationGlobalProperties;
 import org.openmrs.module.patientregistration.util.PatientRegistrationWebUtil;
 import org.springframework.ui.ExtendedModelMap;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.Calendar;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 
 public class PrimaryCareReceptionEncounterControllerTest extends BasePatientRegistrationControllerTest {
 	
 	@Test
 	public void processPayment_shouldCreateVisitAndCheckInPatient() throws Exception {
 		Patient patient = Context.getPatientService().getPatient(7);
 		String listOfObs = "CODED,11,Medical certificate without diagnosis,1000;NUMERIC,50,50 Gourdes,1001;NON-CODED,0,12345,1002;";
 		
 		PrimaryCareReceptionEncounterController controller = new PrimaryCareReceptionEncounterController();
         controller.setAdtService(Context.getService(AdtService.class));
 
         Calendar now = Calendar.getInstance();
         String year = "" + now.get(Calendar.YEAR);
         String month = "" + (1 + now.get(Calendar.MONTH));
         String day = "" + now.get(Calendar.DAY_OF_MONTH);
 
		ModelAndView modelAndView = controller.processPayment(patient, listOfObs, false, year, month, day, false, null, session,
 		    new ExtendedModelMap());
 		
 		Visit activeVisit = Context.getService(AdtService.class).getActiveVisit(patient,
 		    PatientRegistrationWebUtil.getRegistrationLocation(session));
 		
 		assertNotNull(activeVisit);
 		assertThat(activeVisit.getEncounters().size(), is(1));
 		assertThat(activeVisit.getEncounters().iterator().next().getEncounterType(),
 		    is(PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_RECEPTION_ENCOUNTER_TYPE()));
 	}
 
 }
