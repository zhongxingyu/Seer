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
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.patientsummary.PatientSummaryReportDefinition;
 import org.openmrs.module.patientsummary.PatientSummaryResult;
 import org.openmrs.module.patientsummary.PatientSummaryTemplate;
 import org.openmrs.module.patientsummary.api.PatientSummaryService;
 import org.openmrs.module.patientsummary.util.ConfigurationUtil;
 import org.openmrs.module.reporting.report.ReportDesignResource;
 import org.openmrs.module.reporting.report.renderer.TextTemplateRenderer;
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
 		
 		List<PatientSummary> patientSummaries = new ArrayList<PatientSummary>();
 		
 		List<PatientSummaryReportDefinition> patientSummaryDefinitions = getService().getAllPatientSummaryReportDefinitions(includeRetired);
 		for (PatientSummaryReportDefinition psd : patientSummaryDefinitions) {
 			PatientSummary summary = new PatientSummary(psd.getUuid(), psd.getName(), psd.getDescription());
 			summary.setTemplates(getService().getPatientSummaryTemplates(psd, includeRetired));
 			patientSummaries.add(summary);
 		}
 		
 		model.addAttribute("summaries", patientSummaries);
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
 							  @RequestParam(value="download",required=false) boolean download,
 							  @RequestParam(value="print",required=false) boolean print) throws IOException {		
 		try {
 			PatientSummaryService pss = Context.getService(PatientSummaryService.class);
 			PatientSummaryTemplate ps = pss.getPatientSummaryTemplate(summaryId);
 			
 			HashMap<String, Object> parameters = new HashMap<String, Object>();
 			parameters.put("patientSummaryMode", print ? "print" : "download");
 			PatientSummaryResult result = pss.evaluatePatientSummaryTemplate(ps, patientId, parameters);
 			if (result.getErrorDetails() != null) {
 				result.getErrorDetails().printStackTrace(response.getWriter());
 			} 
 			else {
 				if (download) {
 					response.setHeader("Content-Type", ps.getContentType());
 					response.setHeader("Content-Disposition", "attachment; filename=\"" + ps.getExportFilename() + "\"");
 				}
 				else if (print) {
 					response.setHeader("Content-Type", ps.getContentType());
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
 	public void previewSummaries(ModelMap model, @RequestParam(required = false, value = "summaryId") Integer summaryId,
 	                             @RequestParam(required = false, value = "patientId") Integer patientId,
 	                             @RequestParam(required = false) String script,
 	                             @RequestParam(required = false) String scriptType,
 	                             @RequestParam(required = false) String iframe, HttpSession session) throws Exception {
		PatientSummaryService pss = Context.getService(PatientSummaryService.class);
 
 		if (patientId == null) {
 			//Get patientId from session to avoid picking it all the time when returning to preview.
 			patientId = (Integer) session.getAttribute("patientId");
 		} else {
 			session.setAttribute("patientId", patientId);
 		}
 		
		if (script == null) {
 			//Don't display patient summary templates if previewing an unsaved script template.
 			List<PatientSummaryTemplate> patientSummaries = pss.getAllPatientSummaryTemplates(false);
 			model.addAttribute("patientSummaries", patientSummaries);
 		}
 		
 		PatientSummaryTemplate summaryToPreview = (summaryId == null ? null :  pss.getPatientSummaryTemplate(summaryId));
 		model.addAttribute("summaryToPreview", summaryToPreview);
 		model.addAttribute("patientId", patientId);
 		model.addAttribute("iframe", iframe);
 		model.addAttribute("script", script);
 		model.addAttribute("scriptType", scriptType);
 		
 		String errorDetails = null;
 		
 		if (summaryToPreview != null && patientId != null) {
 			PatientSummaryTemplate ps = pss.getPatientSummaryTemplate(summaryId);
 			
 			if (script != null) {
 				//Preview an unsaved script template
 				ps.getReportDesign().setRendererType(TextTemplateRenderer.class);
 				ps.getReportDesign().getProperties().clear();
 				ps.getReportDesign().getResources().clear();
 				
 				ReportDesignResource designResource = new ReportDesignResource();
 				designResource.setReportDesign(ps.getReportDesign());
 				designResource.setName("template");
 				designResource.setContents(script.getBytes("UTF-8"));
 				
 				ps.getReportDesign().addResource(designResource);
 				
 				ps.getReportDesign().addPropertyValue(TextTemplateRenderer.TEMPLATE_TYPE, scriptType);
 			}
 			
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
 	
 	
 	public class PatientSummary {
 		private String uuid;
 		private String name;
 		private String description;
 		private List<PatientSummaryTemplate> templates;
 		
         public PatientSummary(String uuid, String name, String description) {
 	        super();
 	        this.uuid = uuid;
 	        this.name = name;
 	        this.description = description;
         }
 
 		public String getUuid() {
         	return uuid;
         }
 		
         public void setUuid(String uuid) {
         	this.uuid = uuid;
         }
 		
         public String getName() {
         	return name;
         }
 		
         public void setName(String name) {
         	this.name = name;
         }
 		
         public String getDescription() {
         	return description;
         }
 		
         public void setDescription(String description) {
         	this.description = description;
         }
 		
         public List<PatientSummaryTemplate> getTemplates() {
         	return templates;
         }
 		
         public void setTemplates(List<PatientSummaryTemplate> templates) {
         	this.templates = templates;
         }
 	}
 }
