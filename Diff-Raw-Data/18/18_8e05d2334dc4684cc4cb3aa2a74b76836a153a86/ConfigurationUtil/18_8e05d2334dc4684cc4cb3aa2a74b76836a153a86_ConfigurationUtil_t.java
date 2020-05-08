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
 package org.openmrs.module.patientsummary.util;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.GlobalProperty;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.patientsummary.PatientSummaryTemplate;
 import org.openmrs.module.patientsummary.api.PatientSummaryService;
 import org.openmrs.util.OpenmrsUtil;
 
 /**
  * This class encapsulates all of the configuration setting management needed for the module
  */
 public class ConfigurationUtil {
 	
 	private static final Log log = LogFactory.getLog(ConfigurationUtil.class);
 	
 	//**** CONFIGURATION CONSTANTS *****
 	public static final String MODULE_ID = "patientsummary";
 	public static final String DEFAULT_TAB_NAME_OR_MSG_CODE = MODULE_ID + ".defaultTabNameMessageCode";
 	public static final String GP_PATIENT_SUMMARY_TAB_NAME = MODULE_ID + ".tabNameOrMessageCode";
	public static final String GP_REPORT_DESIGN_UUIDS = MODULE_ID + ".reportDesignUuids";
 	
 	/**
 	 * Returns the label or message code for the patient summary tab as defined by the value of the
 	 * PatientSummaryConstants.GP_PATIENT_SUMMARY_TAB_NAME {@link GlobalProperty} otherwise defaults
 	 * to PatientSummaryConstants.DEFAULT_TAB_NAME_OR_MSG_CODE
 	 * 
 	 * @return the text or message code
 	 */
 	public static String getPatientSummaryTabName() {
 		String code = Context.getAdministrationService().getGlobalProperty(GP_PATIENT_SUMMARY_TAB_NAME);
 		if (StringUtils.isBlank(code)) {
 			code = DEFAULT_TAB_NAME_OR_MSG_CODE;
 		}
 		return code;
 	}
 	
 	/**
 	 * @param name the name to set the Patient Summary Tab to
 	 */
 	public static void setPatientSummaryTabName(String name) {
 		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(GP_PATIENT_SUMMARY_TAB_NAME);
 		if (gp == null) {
 			gp = new GlobalProperty(GP_PATIENT_SUMMARY_TAB_NAME);
 		}
 		gp.setPropertyValue(name);
 		Context.getAdministrationService().saveGlobalProperty(gp);
 	}
 	
 	/**
 	 * Returns the label or message code for the patient summary tab as defined by the value of the
 	 * PatientSummaryConstants.GP_PATIENT_SUMMARY_TAB_NAME {@link GlobalProperty} otherwise defaults
 	 * to PatientSummaryConstants.DEFAULT_TAB_NAME_OR_MSG_CODE
 	 * 
 	 * @return the text or message code
 	 */
 	public static List<PatientSummaryTemplate> getPatientSummaryTemplatesForDashboard() {
 		List<PatientSummaryTemplate> ret = new ArrayList<PatientSummaryTemplate>();
		String gpValue = Context.getAdministrationService().getGlobalProperty(GP_REPORT_DESIGN_UUIDS);
 		if (StringUtils.isNotBlank(gpValue)) {
 			String[] uuids = StringUtils.split(gpValue, ",");
 			for (String uuid : uuids) {
 				PatientSummaryTemplate ps = Context.getService(PatientSummaryService.class).getPatientSummaryTemplateByUuid(uuid);
 				if (ps != null) {
 					ret.add(ps);
 				}
 				else {
					log.warn("Unable to find a Patient Summary with uuid " + uuid + ". Please check your " + GP_REPORT_DESIGN_UUIDS + " global property.");
 				}
 			}
 		}
 		return ret;
 	}
 	
 	/**
 	 * 
	 * @param patientSummary the summary to add to the patient dashboard
 	 * @param position 0-index position to add the patient summary to, if null will add to the end
 	 */
 	public static void addPatientSummaryTemplateToDashboard(PatientSummaryTemplate patientSummary, Integer position) {
		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(GP_REPORT_DESIGN_UUIDS);
 		if (gp == null) {
			gp = new GlobalProperty(GP_REPORT_DESIGN_UUIDS);
 		}
 		String uuidToAdd = patientSummary.getReportDesign().getUuid();
 		List<String> uuids = new ArrayList<String>();
 		if (StringUtils.isNotBlank(gp.getPropertyValue())) {
 			uuids.addAll(Arrays.asList(StringUtils.split(gp.getPropertyValue(), ",")));
 			if (position != null && position < uuids.size()) {
 				uuids.add(position, uuidToAdd);
 			}
 			else {
 				uuids.add(uuidToAdd);
 			}
 		}
 		gp.setPropertyValue(OpenmrsUtil.join(uuids, ","));
 		Context.getAdministrationService().saveGlobalProperty(gp);
 	}
 }
