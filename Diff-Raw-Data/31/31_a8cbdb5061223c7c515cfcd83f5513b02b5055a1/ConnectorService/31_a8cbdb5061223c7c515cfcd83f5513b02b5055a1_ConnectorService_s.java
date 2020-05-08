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
 
 package org.openmrs.module.odkconnector.api;
 
 import java.util.List;
 
 import org.openmrs.Cohort;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.api.APIException;
 import org.openmrs.api.OpenmrsService;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * This service exposes module's core functionality. It is a Spring managed bean which is configured in moduleApplicationContext.xml.
  * <p/>
  * It can be accessed only via Context:<br>
  * <code>
  * Context.getService(ConnectorServiceService.class).someMethod();
  * </code>
  *
  * @see org.openmrs.api.context.Context
  */
 @Transactional
 public interface ConnectorService extends OpenmrsService {
 
 	/*
 	 * Add service methods here
 	 *
 	 */
 
 	/**
 	 * Service methods to get all patients inside the cohort
 	 *
 	 * @param cohort the cohort
 	 * @return all patients in the cohort or empty list when no patient match the patient id in the cohort
 	 * @throws APIException when the process failed
 	 */
 	@Transactional(readOnly = true)
 	List<Patient> getCohortPatients(Cohort cohort) throws APIException;
 
 	/**
 	 * Service methods to get all observations for all patients in the cohort
 	 *
 	 * @param cohort the cohort
 	 * @return all observations for patients in the cohort or empty list when no observations for the patient ids in the cohort exists
	 * @throws APIException
 	 */
 	@Transactional(readOnly = true)
 	List<Obs> getCohortObservations(Cohort cohort) throws APIException;
 }
