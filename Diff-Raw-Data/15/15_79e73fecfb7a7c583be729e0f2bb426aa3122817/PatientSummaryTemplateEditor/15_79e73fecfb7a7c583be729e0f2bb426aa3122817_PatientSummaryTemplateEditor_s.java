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
 import java.io.UnsupportedEncodingException;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.htmlwidgets.web.handler.WidgetHandler;
 import org.openmrs.module.patientsummary.PatientSummaryReportDefinition;
 import org.openmrs.module.patientsummary.PatientSummaryTemplate;
 import org.openmrs.module.patientsummary.api.PatientSummaryService;
 import org.openmrs.module.patientsummary.web.controller.util.DataSchemaUtil;
 import org.openmrs.module.reporting.report.ReportDesign;
 import org.openmrs.module.reporting.report.ReportDesignResource;
 import org.openmrs.module.reporting.report.renderer.ReportRenderer;
 import org.openmrs.module.reporting.report.renderer.TextTemplateRenderer;
 import org.openmrs.module.reporting.report.renderer.template.TemplateEngineManager;
 import org.openmrs.util.HandlerUtil;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 
 /**
  *
  */
 @Controller
 @RequestMapping(PatientSummaryWebConstants.MODULE_URL + "editTemplate")
 public class PatientSummaryTemplateEditor {
 	
 	@RequestMapping(value = "/newTemplate", method = RequestMethod.GET)
 	public String newTemplate(String reportDefinitionUuid, ModelMap model) {
 		PatientSummaryReportDefinition reportDefinition = getService().getPatientSummaryReportDefinitionByUuid(
 		    reportDefinitionUuid);
 		
 		ReportDesign reportDesign = new ReportDesign();
 		reportDesign.setName("Untitled");
 		reportDesign.setRendererType(TextTemplateRenderer.class);
 		reportDesign.setReportDefinition(reportDefinition);
 		
 		PatientSummaryTemplate template = new PatientSummaryTemplate(reportDesign);
 		
 		template = getService().savePatientSummaryTemplate(template);
 		
 		model.put("templateUuid", template.getUuid());
 		
 		return "redirect:" + PatientSummaryWebConstants.MODULE_URL + "editTemplate.form";
 	}
 	
 	@RequestMapping(method = RequestMethod.GET)
 	public void editTemplate(String templateUuid, ModelMap model) throws UnsupportedEncodingException {
 		PatientSummaryTemplate template = getService().getPatientSummaryTemplateByUuid(templateUuid);
 		
 		populateModel(model, template);
 	}
 
 	private void populateModel(ModelMap model, PatientSummaryTemplate template) throws UnsupportedEncodingException {
 	    model.put("template", template);
 	    model.put("dataSchema", DataSchemaUtil.getDataSchema(template.getReportDefinition()));
 	    model.put("scriptType", template.getReportDesign().getPropertyValue(TextTemplateRenderer.TEMPLATE_TYPE, ""));
 		model.put("scriptTypes", TemplateEngineManager.getAvailableTemplateEngineNames());
 		
 		model.put("enableOnPatientDashboard", PatientSummaryWebConfiguration.enableOnPatientDashboard(template));
 		
 		if (template.getReportDesign().getRendererType().equals(TextTemplateRenderer.class)) {
 			ReportDesignResource resource = template.getReportDesign().getResourceByName("template");
 			if (resource != null) {
 				model.put("script", new String(resource.getContents(), "UTF-8"));
 			}
 		}
     }
 	
 	@RequestMapping(method = RequestMethod.POST)
 	public String saveTemplate(String templateUuid, String name, Class<? extends ReportRenderer> rendererType,
 	                         String properties, String script, String scriptType, boolean enableOnPatientDashboard, HttpServletRequest request, ModelMap model)
 	    throws IOException {
 
 		PatientSummaryTemplate template = getService().getPatientSummaryTemplateByUuid(templateUuid);
 		
 		PatientSummaryWebConfiguration.saveEnableTemplateOnPatientDashboard(template, enableOnPatientDashboard);
 		
 		template.getReportDesign().setName(name);
 		template.getReportDesign().setRendererType(rendererType);
 		
 		if (!template.getReportDesign().getRendererType().equals(TextTemplateRenderer.class)) {
 			MultipartHttpServletRequest mpr = (MultipartHttpServletRequest) request;
 			Map<String, MultipartFile> files = mpr.getFileMap();
 			
 			MultipartFile resource = files.values().iterator().next();
 			
 			if (resource != null && !resource.isEmpty()) {
 				ReportDesignResource designResource = new ReportDesignResource();
 				designResource.setReportDesign(template.getReportDesign());
 				designResource.setContents(resource.getBytes());
 				designResource.setContentType(resource.getContentType());
 				
 				String fileName = resource.getOriginalFilename();
 				int index = fileName.lastIndexOf(".");
 				designResource.setName(fileName.substring(0, index));
 				designResource.setExtension(fileName.substring(index + 1));
 				
 				template.getReportDesign().addResource(designResource);
 			}
 			
 			WidgetHandler propHandler = HandlerUtil.getPreferredHandler(WidgetHandler.class, Properties.class);
 			Properties props = (Properties) propHandler.parse(properties, Properties.class);
 			template.getReportDesign().setProperties(props);
 		} else {
 			template.getReportDesign().getProperties().clear();
 			template.getReportDesign().getResources().clear();
 			
 			ReportDesignResource designResource = new ReportDesignResource();
 			designResource.setReportDesign(template.getReportDesign());
 			designResource.setName("template");
 			designResource.setContents(script.getBytes("UTF-8"));
 			
 			template.getReportDesign().addResource(designResource);
 			
 			template.getReportDesign().addPropertyValue(TextTemplateRenderer.TEMPLATE_TYPE, scriptType);
 		}
 		
 		getService().savePatientSummaryTemplate(template);
 		
		model.put("uuid", template.getReportDefinition().getUuid());
 		
		return "redirect:" + PatientSummaryWebConstants.MODULE_URL + "editSummary.form";
 	}
 	
 	@RequestMapping(value = "/deleteResource", method = RequestMethod.GET)
 	public String deleteResource(String templateUuid, String resourceUuid, ModelMap model) {
 		PatientSummaryTemplate template = getService().getPatientSummaryTemplateByUuid(templateUuid);
 		
 		Set<ReportDesignResource> resources = template.getReportDesign().getResources();
 		for (Iterator<ReportDesignResource> it = resources.iterator(); it.hasNext();) {
 			ReportDesignResource resource = it.next();
 			if (resourceUuid.equals(resource.getUuid())) {
 				it.remove();
 				break;
 			}
 		}
 		
 		getService().savePatientSummaryTemplate(template);
 		
 		model.put("templateUuid", template.getUuid());
 		
 		return "redirect:" + PatientSummaryWebConstants.MODULE_URL + "editTemplate.form";
 	}
 	
 	private PatientSummaryService getService() {
 		return Context.getService(PatientSummaryService.class);
 	}
 }
