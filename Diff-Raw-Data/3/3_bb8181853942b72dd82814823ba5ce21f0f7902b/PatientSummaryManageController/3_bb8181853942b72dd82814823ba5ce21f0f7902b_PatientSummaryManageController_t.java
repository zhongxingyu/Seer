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
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.patientsummary.PatientSummaryTemplate;
 import org.openmrs.module.patientsummary.PatientSummaryReportDefinition;
 import org.openmrs.module.patientsummary.PatientSummaryResult;
 import org.openmrs.module.patientsummary.api.PatientSummaryService;
 import org.openmrs.module.patientsummary.util.ConfigurationUtil;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  * The main controller.
  */
 @Controller
 public class PatientSummaryManageController {
 	
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	@RequestMapping(value = "/module/" + ConfigurationUtil.MODULE_ID + "/manageSummaries")
 	public void manageSummaries(ModelMap model, @RequestParam(required=false, value="includeRetired") boolean includeRetired) {
 		
 		List<PatientSummaryTemplate> summaries = getService().getAllPatientSummaryTemplates(includeRetired);
 		List<PatientSummaryReportDefinition> schemas = getService().getAllPatientSummaryReportDefinitions(includeRetired);
 		
 		model.addAttribute("summaries", summaries);
 		model.addAttribute("schemas", schemas);
 	}
 	
 	/**
 	 * Deletes a patient summary report definition and all associated patient summary designs
 	 */
 	@RequestMapping("/module/" + ConfigurationUtil.MODULE_ID + "/purgeSummary")
 	public String purgeSummary(@RequestParam("uuid") String uuid) {
 		PatientSummaryTemplate patientSummary = getService().getPatientSummaryTemplateByUuid(uuid);
 		PatientSummaryReportDefinition reportDefinition = patientSummary.getReportDefinition();
 		getService().purgePatientSummaryTemplate(patientSummary);
 		if (getService().getPatientSummaryTemplates(reportDefinition, true).isEmpty()) {
 			getService().purgePatientSummaryReportDefinition(reportDefinition);
 		}
 		return "redirect:manageSummaries.list";
 	}
 	
 	/**
 	 * Receives requests to run a patient summary.
 	 * @param patientId the id of patient whose summary you wish to view
 	 * @param summaryId the id of the patientsummary you wish to view
 	 */
 	@RequestMapping("/module/" + ConfigurationUtil.MODULE_ID + "/renderSummary")
 	public void renderSummary(ModelMap model, HttpServletRequest request, HttpServletResponse response,
 							  @RequestParam("patientId") Integer patientId,                       
 							  @RequestParam("summaryId") Integer summaryId,
 							  @RequestParam(value="download",required=false) boolean download) throws IOException {		
 		try {
 			PatientSummaryService pss = Context.getService(PatientSummaryService.class);
 			PatientSummaryTemplate ps = pss.getPatientSummaryTemplate(summaryId);
 			PatientSummaryResult result = pss.evaluatePatientSummaryTemplate(ps, patientId, new HashMap<String, Object>());
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
 	
 	@RequestMapping(value = "/module/" + ConfigurationUtil.MODULE_ID + "/previewSummaries")
 	public void previewSummaries(ModelMap model,
 					   @RequestParam(required=false, value="summaryId") Integer summaryId,
 					   @RequestParam(required=false, value="patientId") Integer patientId) throws Exception {
 		
 		PatientSummaryService pss = Context.getService(PatientSummaryService.class);
 		List<PatientSummaryTemplate> patientSummaries = pss.getAllPatientSummaryTemplates(false);
 		PatientSummaryTemplate summaryToPreview = (summaryId == null ? null :  pss.getPatientSummaryTemplate(summaryId));
 
 		model.addAttribute("patientSummaries", patientSummaries);
 		model.addAttribute("summaryToPreview", summaryToPreview);
 		model.addAttribute("patientId", patientId);
 		
 		String errorDetails = null;
 		
 		if (summaryToPreview != null && patientId == null) {
 			errorDetails = "Please select a patient to preview a Patient Summary";
 		}
 		
 		if (summaryToPreview != null && patientId != null) {
 			PatientSummaryTemplate ps = pss.getPatientSummaryTemplate(summaryId);
 			PatientSummaryResult result = pss.evaluatePatientSummaryTemplate(ps, patientId, new HashMap<String, Object>());
			String generatedSummary = (result.getRawContents() != null ? new String(result.getRawContents(), "UTF-8") : "");
			model.addAttribute("generatedSummary", generatedSummary);
 			errorDetails = ObjectUtils.toString(result.getErrorDetails());
 		}
 		
 		model.addAttribute("errorDetails", errorDetails);
 	}
 	
 	private PatientSummaryService getService() {
 		return Context.getService(PatientSummaryService.class);
 	}
 }
