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
 package org.openmrs.module.maternalsummary.web.controller;
 
import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.api.FormService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.maternalsummary.MaternalSummary;
 import org.openmrs.module.maternalsummary.MaternalSummaryService;
 import org.openmrs.web.controller.PortletController;
 
 /**
  * Maternal Summary Portlet Controller
  * 
  * @author Jembi Health Systems
  */
 public class MaternalSummaryPortletController extends PortletController {
 	
 	protected static final Log log = LogFactory.getLog(MaternalSummaryPortletController.class);
 	
 	private MaternalSummary summary;
 
 	@Override
 	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
 		MaternalSummaryService mss = Context.getService(MaternalSummaryService.class);
 		int patientId = (Integer)model.get("patientId");
 		summary = mss.getMaternalSummary(patientId);
 		
 		model.put("isPatientFemale", new Boolean(summary.getPatient().getGender().equalsIgnoreCase("F")));
 		model.put("deliverySummary", summary.getDeliverySummary());
 		model.put("obsHistory", summary.getObsHistory());
 		model.put("medicalHistory", summary.getMedicalHistory());
 		model.put("testsAndTreatment", summary.getTestsAndTreatment());
 		model.put("ANCVisits", summary.getANCVisits());
 		model.put("referrals", summary.getReferrals());
 		model.put("rapidsmsMessages", summary.getRapidSMSMessages());
		model.put("referralConfirmationFormId", getReferralConfirmationFormId());
	}

	private Integer getReferralConfirmationFormId() {
		FormService fs = Context.getFormService();
		Form res = fs.getForm("RHEA ANC 6: Referral Confirmation Form");
		
		if (res==null) {
			List<Form> fuzzyMatches = fs.getForms("Referral Confirmation", true);
			if (fuzzyMatches!=null && !fuzzyMatches.isEmpty())
				res = fuzzyMatches.get(0);
		}
		
		if (res==null) {
			EncounterType et = Context.getEncounterService().getEncounterType("ANC Referral Confirmation");
			for (Form f : fs.getAllForms()) {
				if (f.getEncounterType().equals(et)) {
					res = f;
					break;
				}
			}
		}
		
		return res!=null ? res.getFormId() : null;
 	}
 }
