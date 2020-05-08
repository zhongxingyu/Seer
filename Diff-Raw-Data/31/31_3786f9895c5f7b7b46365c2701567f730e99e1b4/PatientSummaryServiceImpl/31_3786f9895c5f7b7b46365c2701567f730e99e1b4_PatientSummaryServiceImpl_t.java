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
 package org.openmrs.module.patientsummary.api;
 
 import java.io.ByteArrayOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Cohort;
 import org.openmrs.api.context.Context;
 import org.openmrs.api.impl.BaseOpenmrsService;
 import org.openmrs.module.patientsummary.PatientSummaryReportDefinition;
 import org.openmrs.module.patientsummary.PatientSummaryResult;
 import org.openmrs.module.patientsummary.PatientSummaryTemplate;
 import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
 import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
 import org.openmrs.module.reporting.definition.service.DefinitionService;
 import org.openmrs.module.reporting.evaluation.EvaluationContext;
 import org.openmrs.module.reporting.report.ReportData;
 import org.openmrs.module.reporting.report.ReportDesign;
 import org.openmrs.module.reporting.report.definition.ReportDefinition;
 import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
 import org.openmrs.module.reporting.report.renderer.ReportRenderer;
 import org.openmrs.module.reporting.report.service.ReportService;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * The core implementation of {@link PatientSummaryService}.
  */
 public class PatientSummaryServiceImpl extends BaseOpenmrsService implements PatientSummaryService {
 	
 	protected final Log log = LogFactory.getLog(this.getClass());
 	
 	/**
 	 * @see PatientSummaryService#getPatientSummaryReportDefinition(Integer)
 	 */
 	@Override
 	public PatientSummaryReportDefinition getPatientSummaryReportDefinition(Integer id) {
 		return (PatientSummaryReportDefinition)getReportDefinitionService().getDefinition(id);
 	}
 	
 	/**
 	 * @see PatientSummaryService#getPatientSummaryReportDefinitionByUuid(String)
 	 */
 	@Override
 	public PatientSummaryReportDefinition getPatientSummaryReportDefinitionByUuid(String uuid) {
 		return (PatientSummaryReportDefinition)getReportDefinitionService().getDefinitionByUuid(uuid);
 	}
 	
 	/**
 	 * @see PatientSummaryService#getAllPatientSummaryDefinitions(boolean)
 	 */
 	@Override
 	public List<PatientSummaryReportDefinition> getAllPatientSummaryReportDefinitions(boolean includeRetired) {
 		List<PatientSummaryReportDefinition> l = new ArrayList<PatientSummaryReportDefinition>();
 		for (ReportDefinition d : getReportDefinitionService().getAllDefinitions(includeRetired)) {
 			if (d instanceof PatientSummaryReportDefinition) {
 				l.add((PatientSummaryReportDefinition)d);
 			}
 		}
 		return l;
 	}
 	
 	
 	/**
      * @see PatientSummaryService#purgePatientSummaryReportDefinition(PatientSummaryReportDefinition)
      */
     @Override
     public void purgePatientSummaryReportDefinition(PatientSummaryReportDefinition reportDefinition) {
     	getReportDefinitionService().purgeDefinition(reportDefinition);
     }
     
 	/**
 	 * @see PatientSummaryService#getPatientSummaryTemplates(ReportDefinition, boolean)
 	 */
 	@Override
 	public List<PatientSummaryTemplate> getPatientSummaryTemplates(PatientSummaryReportDefinition reportDefinition, boolean includeRetired) {
 		List<PatientSummaryTemplate> ret = new ArrayList<PatientSummaryTemplate>();
 		for (ReportDesign d : getReportService().getReportDesigns(reportDefinition, null, includeRetired)) {
 			ret.add(new PatientSummaryTemplate(d));
 		}
 		return ret;
 	}
 
 	/**
 	 * @see PatientSummaryService#getPatientSummaryTemplate(Integer)
 	 */
 	@Override
 	public PatientSummaryTemplate getPatientSummaryTemplate(Integer id) {
 		ReportDesign d = getReportService().getReportDesign(id);
 		return new PatientSummaryTemplate(d);
 	}
 
 	/**
 	 * @see PatientSummaryService#getPatientSummaryTemplateByUuid(String)
 	 */
 	@Override
 	public PatientSummaryTemplate getPatientSummaryTemplateByUuid(String uuid) {
 		ReportDesign d = getReportService().getReportDesignByUuid(uuid);
 		return new PatientSummaryTemplate(d);
 	}
 
 	/**
 	 * @see PatientSummaryService#getAllPatientSummaryTemplates(boolean)
 	 */
 	@Override
 	public List<PatientSummaryTemplate> getAllPatientSummaryTemplates(boolean includeRetired) {
 		List<PatientSummaryTemplate> l = new ArrayList<PatientSummaryTemplate>();
 		for (ReportDesign d : getReportService().getAllReportDesigns(includeRetired)) {
 			if (d.getReportDefinition() instanceof PatientSummaryReportDefinition) {
 				l.add(new PatientSummaryTemplate(d));
 			}
 		}
 		return l;
 	}
 	
 	/**
 	 * @see PatientSummaryService#evaluatePatientSummaryTemplate(PatientSummaryTemplate, Integer, Map)
 	 */
 	@Override
	public PatientSummaryResult evaluatePatientSummaryTemplate(PatientSummaryTemplate patientSummaryTemplate, Integer patientId, Map<String, Object> parameters) {
		PatientSummaryResult result = new PatientSummaryResult(patientSummaryTemplate, patientId, parameters);
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		try {
 			// Populate a new EvaluationContext with the patient and parameters passed in
 			EvaluationContext context = new EvaluationContext();
 			Cohort baseCohort = new Cohort();
 			baseCohort.addMember(patientId);
 			context.setBaseCohort(baseCohort);
 			if (parameters != null) {
 				for (Map.Entry<String, Object> paramEntry : parameters.entrySet()) {
 					context.addParameterValue(paramEntry.getKey(), paramEntry.getValue());
 				}
 			}
 			
 			// Evaluate the PatientSummary with this context to produce the data to use to populate the summary
 			ReportDefinitionService rds = Context.getService(ReportDefinitionService.class);
			ReportData data = rds.evaluate(patientSummaryTemplate.getReportDesign().getReportDefinition(), context);
 			
 			// Render the template with this data to produce the raw data result
			Class<? extends ReportRenderer> rendererType = patientSummaryTemplate.getReportDesign().getRendererType();
 			ReportRenderer renderer = rendererType.newInstance();
			String rendererArg = patientSummaryTemplate.getReportDesign().getUuid();
 			renderer.render(data, rendererArg, baos);
 			
 			// Return a PatientSummaryResult which contains the raw output and contextual data
			result.setContentType(patientSummaryTemplate.getContentType());
 			result.setRawContents(baos.toByteArray());
 		}
 		catch (Throwable t) {
 			result.setErrorDetails(t);
 		}
 		finally {
 			IOUtils.closeQuietly(baos);
 		}
 		return result;
 	}
 	
 	/**
      * @see PatientSummaryService#purgePatientSummaryTemplate(PatientSummaryTemplate)
      */
     @Override
    public void purgePatientSummaryTemplate(PatientSummaryTemplate patientSummaryTemplate) {
    	getReportService().purgeReportDesign(patientSummaryTemplate.getReportDesign());
     }
 
 	/**
 	 * @return the underlying ReportService used to manage the patient summary report definitions
 	 */
 	protected ReportDefinitionService getReportDefinitionService() {
 		return Context.getService(ReportDefinitionService.class);
 	}
 	
 	/**
 	 * @return the underlying ReportService used to manage the patient summaries
 	 */
 	protected ReportService getReportService() {
 		return Context.getService(ReportService.class);
 	}
 
 	/**
      * @see org.openmrs.module.patientsummary.api.PatientSummaryService#savePatientSummaryReportDefinition(org.openmrs.module.patientsummary.PatientSummaryReportDefinition)
      */
     @Override
     @Transactional
     public PatientSummaryReportDefinition savePatientSummaryReportDefinition(PatientSummaryReportDefinition rd) {
     	PatientDataSetDefinition patientDataSetDefinition = rd.getPatientDataSetDefinition();
     	if (StringUtils.isBlank(patientDataSetDefinition.getName())) {
     		patientDataSetDefinition.setName("Patient Dataset for " + rd.getName());
     	}
     	
     	Context.getService(DataSetDefinitionService.class).saveDefinition(patientDataSetDefinition);
     	
     	return getReportDefinitionService().saveDefinition(rd);
     }
 }
