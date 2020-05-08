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
 package org.openmrs.module.patientsummary.web.controller;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.patientsummary.PatientSummary;
 import org.openmrs.module.patientsummary.PatientSummaryResult;
 import org.openmrs.module.patientsummary.api.PatientSummaryService;
 import org.openmrs.module.patientsummary.util.ConfigurationUtil;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  * Processes request for viewing patient summaries
  */
 @Controller
 public class ViewPatientSummaryFormController {
 	
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	/**
 	 * Receives requests to run a patient summary.
 	 * @param patientId the id of patient whose summary you wish to view
 	 * @param summaryId the id of the patientsummary you wish to view
 	 */
 	@RequestMapping("/module/" + ConfigurationUtil.MODULE_ID + "/viewPatientSummary")
 	public void viewPatientSummary(ModelMap model, HttpServletRequest request, HttpServletResponse response,
 									@RequestParam("patientId") Integer patientId,                       
									@RequestParam("summaryId") Integer summaryId,
									@RequestParam(value="download",required=false) boolean download) throws IOException {		
 		try {
 			PatientSummaryService pss = Context.getService(PatientSummaryService.class);
 			PatientSummary ps = pss.getPatientSummary(summaryId);
 			PatientSummaryResult result = pss.evaluatePatientSummary(ps, patientId, new HashMap<String, Object>());
 			if (result.getErrorDetails() != null) {
 				result.getErrorDetails().printStackTrace(response.getWriter());
 			} 
 			else {
				if (download) {
					response.setHeader("Content-Type", ps.getContentType());
					response.setHeader("Content-Disposition", "attachment; filename=\"" + ps.getExportFilename() + "\"");
				}
				response.setContentType(ps.getContentType());
 				response.getOutputStream().write(result.getRawContents());
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace(response.getWriter());
 		}
 	}
 }
