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
 
 package org.openmrs.module.clinicalsummary.web.controller.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import javax.servlet.http.HttpServletResponse;
 
 import com.thoughtworks.xstream.XStream;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.api.context.Context;
 import org.openmrs.api.context.ContextAuthenticationException;
 import org.openmrs.module.clinicalsummary.Index;
 import org.openmrs.module.clinicalsummary.Summary;
 import org.openmrs.module.clinicalsummary.service.IndexService;
 import org.openmrs.module.clinicalsummary.service.SummaryService;
 import org.openmrs.module.clinicalsummary.web.controller.service.xstream.PatientConverter;
 import org.openmrs.module.clinicalsummary.web.controller.service.xstream.PatientIdentifierConverter;
 import org.openmrs.module.clinicalsummary.web.controller.service.xstream.PersonNameConverter;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  */
 @Controller
 @RequestMapping("/module/clinicalsummary/service/location/cohort")
 public class LocationCohortController {
 
 	private static final Log log = LogFactory.getLog(LocationCohortController.class);
 
 	public static final String CLINICALSUMMARY_SERVICE_TIMEFRAME = "clinicalsummary.service.timeframe";
 
 	@RequestMapping(method = RequestMethod.GET)
 	public void searchCohort(@RequestParam(required = false, value = "username") String username,
 	                         @RequestParam(required = false, value = "password") String password,
 	                         @RequestParam(required = true, value = "locationId") Integer locationId,
 	                         @RequestParam(required = true, value = "summaryId") Integer summaryId,
 	                         HttpServletResponse response) throws IOException {
 
 		try {
 			if (!Context.isAuthenticated())
 				Context.authenticate(username, password);
 
 			String cohortTimeFrame = Context.getAdministrationService().getGlobalProperty(CLINICALSUMMARY_SERVICE_TIMEFRAME);
 			Integer timeFrame = NumberUtils.toInt(cohortTimeFrame, 5);
 
 			Location location = Context.getLocationService().getLocation(locationId);
 			Summary summary = Context.getService(SummaryService.class).getSummary(summaryId);
 
 			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, timeFrame);
 			Date startDate = calendar.getTime();
 
 			calendar.add(Calendar.DATE, timeFrame);
 			Date endDate = calendar.getTime();
 
 			List<Index> indexes = Context.getService(IndexService.class).getIndexes(location, summary, startDate, endDate);
 
 			List<Patient> patients = new ArrayList<Patient>();
 			for (Index index : indexes)
 				patients.add(index.getPatient());
 
 			// serialize the the search result
 			XStream xStream = new XStream();
 			xStream.alias("results", List.class);
 			xStream.alias("patient", Patient.class);
 			xStream.registerConverter(new PatientConverter());
 			xStream.registerConverter(new PatientIdentifierConverter());
 			xStream.registerConverter(new PersonNameConverter());
 			xStream.toXML(patients, response.getOutputStream());
 		} catch (ContextAuthenticationException e) {
 			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
 		}
 	}
 }
