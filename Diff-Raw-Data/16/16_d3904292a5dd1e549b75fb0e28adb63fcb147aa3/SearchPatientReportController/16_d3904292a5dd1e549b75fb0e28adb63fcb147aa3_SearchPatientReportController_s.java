 /**
  *  Copyright 2011 Health Information Systems Project of India
  *
  *  This file is part of Laboratory module.
  *
  *  Laboratory module is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
 
  *  Laboratory module is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Laboratory module.  If not, see <http://www.gnu.org/licenses/>.
  *
  **/
 
 package org.openmrs.module.laboratory.web.controller.patientreport;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.openmrs.Concept;
 import org.openmrs.ConceptNumeric;
 import org.openmrs.Encounter;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.hospitalcore.model.LabTest;
 import org.openmrs.module.laboratory.LaboratoryService;
 import org.openmrs.module.laboratory.util.LaboratoryConstants;
 import org.openmrs.module.laboratory.web.util.LaboratoryUtil;
 import org.openmrs.module.laboratory.web.util.TestResultModel;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller("LaboratorySearchPatientReportController")
 @RequestMapping("/module/laboratory/searchPatientReport.form")
 public class SearchPatientReportController {
 
 	@SuppressWarnings("unchecked")
 	@RequestMapping(method = RequestMethod.GET)
 	public String searchTest(
 			@RequestParam(value = "date", required = false) String dateStr,
			@RequestParam(value = "patientIdentifier") String patientIdentifier,
 			HttpServletRequest request, Model model) {
 		LaboratoryService ls = (LaboratoryService) Context
 				.getService(LaboratoryService.class);
 		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
 		Date date = null;
 		try {
 			date = sdf.parse(dateStr);
			List<Patient> patients = Context.getPatientService().getPatients(
					patientIdentifier);
			if (!patients.isEmpty()) {
				Patient patient = patients.get(0);
 				List<LabTest> tests = ls
 						.getLaboratoryTestsByDateAndPatient(date, patient);
 				if ((tests != null) && (!tests.isEmpty())) {
 					Map<Concept, Set<Concept>> testTreeMap = (Map<Concept, Set<Concept>>) request
 							.getSession().getAttribute(
 									LaboratoryConstants.SESSION_TEST_TREE_MAP);
 					List<TestResultModel> trms = renderTests(tests,
 							testTreeMap);
 					trms = formatTestResult(trms);
 					model.addAttribute("tests", trms);
 				}
 			}
 		} catch (ParseException e) {
 			e.printStackTrace();
 			System.out.println("Error when parsing order date!");
 			return null;
 		}
 		return "/module/laboratory/patientreport/search";
 	}
 
 	private List<TestResultModel> renderTests(List<LabTest> tests,
 			Map<Concept, Set<Concept>> testTreeMap) {
 		List<TestResultModel> trms = new ArrayList<TestResultModel>();
 		for (LabTest test : tests) {
 			if (test.getEncounter() != null) {
 				Encounter encounter = test.getEncounter();
 				for (Obs obs : encounter.getAllObs()) {
 					TestResultModel trm = new TestResultModel();
 					Concept investigation = getInvestigationByTest(test,
 							testTreeMap);
 					trm.setInvestigation(LaboratoryUtil.getConceptName(investigation));
 					trm.setSet(test.getConcept().getName().getName());
 					Concept concept = Context.getConceptService().getConcept(obs.getConcept().getConceptId());
 					trm.setTest(concept.getName().getName());
 					trm.setConcept(test.getConcept());					
 					setTestResultModelValue(obs, trm);
 					trms.add(trm);
 				}
 			}
 		}
 		return trms;
 	}
 
 	private Concept getInvestigationByTest(LabTest test,
 			Map<Concept, Set<Concept>> investigationTests) {
 		for (Concept investigation : investigationTests.keySet()) {
 			if (investigationTests.get(investigation).contains(
 					test.getConcept()))
 				return investigation;
 		}
 		return null;
 	}
 	
 	private void setTestResultModelValue(Obs obs, TestResultModel trm) {
 		Concept concept = Context.getConceptService().getConcept(obs.getConcept().getConceptId());
 		trm.setTest(concept.getName().getName());
 		if (concept != null) {
 			String datatype = concept.getDatatype().getName();
 			if (datatype.equalsIgnoreCase("Text")) {
 				trm.setValue(obs.getValueText());
 			} else if (datatype.equalsIgnoreCase("Numeric")) {
 				trm.setValue(obs.getValueNumeric().toString());
 				ConceptNumeric cn = Context.getConceptService()
 						.getConceptNumeric(concept.getConceptId());
 				trm.setUnit(cn.getUnits());
 				if(cn.getLowNormal()!=null)
 					trm.setLowNormal(cn.getLowNormal().toString());
 				if(cn.getHiNormal()!=null)
 					trm.setHiNormal(cn.getHiNormal().toString());
 			} else if (datatype.equalsIgnoreCase("Coded")) {
 				trm.setValue(obs.getValueCoded().getName().getName());
 			}
 		}
 	}
 
 	private List<TestResultModel> formatTestResult(
 			List<TestResultModel> testResultModels) {
 		Collections.sort(testResultModels);
 		List<TestResultModel> trms = new ArrayList<TestResultModel>();
 		String investigation = null;
 		String set = null;
 		for (TestResultModel trm : testResultModels) {
 			if (!trm.getInvestigation().equalsIgnoreCase(investigation)) {
 				investigation = trm.getInvestigation();
 				TestResultModel t = new TestResultModel();
 				t.setInvestigation(investigation);
 				t.setLevel(TestResultModel.LEVEL_INVESTIGATION);
 				set = null;
 				trms.add(t);
 			}
 
 			if (!trm.getSet().equalsIgnoreCase(set)) {
 				set = trm.getSet();
 				if (!trm.getConcept().getConceptClass().getName()
 						.equalsIgnoreCase("LabSet")) {
 					trm.setLevel(TestResultModel.LEVEL_SET);
 					trms.add(trm);
 				} else {
 					TestResultModel t = new TestResultModel();
 					t.setSet(set);
 					t.setLevel(TestResultModel.LEVEL_SET);
 					t.setEncounterId(trm.getEncounterId());
 					t.setTestId(trm.getTestId());
 					trms.add(t);
 				}
 			}
 
 			if (trm.getConcept().getConceptClass().getName()
 					.equalsIgnoreCase("LabSet")) {
 				trms.add(trm);
 			}
 		}
 		return trms;
 	}
 }
