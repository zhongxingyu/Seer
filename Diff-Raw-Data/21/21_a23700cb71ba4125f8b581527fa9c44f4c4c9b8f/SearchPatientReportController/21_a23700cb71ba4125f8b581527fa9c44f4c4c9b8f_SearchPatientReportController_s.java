 /**
  *  Copyright 2010 Health Information Systems Project of India
  *
  *  This file is part of Radiology module.
  *
  *  Radiology module is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
 
  *  Radiology module is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Radiology module.  If not, see <http://www.gnu.org/licenses/>.
  *
  **/
 
 package org.openmrs.module.radiology.web.controller.patientreport;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.openmrs.Concept;
 import org.openmrs.Patient;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.hospitalcore.concept.TestTree;
 import org.openmrs.module.hospitalcore.model.RadiologyDepartment;
 import org.openmrs.module.radiology.RadiologyService;
 import org.openmrs.module.radiology.model.RadiologyTest;
 import org.openmrs.module.radiology.util.RadiologyConstants;
 import org.openmrs.module.radiology.web.util.RadiologyUtil;
 import org.openmrs.module.radiology.web.util.TestResultModel;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller("RadiologySearchPatientReportController")
 @RequestMapping("/module/radiology/searchPatientReport.form")
 public class SearchPatientReportController {
 
 	public Map<Concept, Set<Integer>> getAllInvestigationTests() {
 		RadiologyService rs = (RadiologyService) Context
 				.getService(RadiologyService.class);
 		RadiologyDepartment department = rs.getCurrentRadiologyDepartment();
 		Map<Concept, Set<Integer>> investigationTests = new HashMap<Concept, Set<Integer>>();
 		if (department != null) {
 			Set<Concept> investigations = department.getInvestigations();
 			for (Concept investigation : investigations) {
 				TestTree tree = new TestTree(investigation);
 				if (tree.getRootNode() != null) {
 					investigationTests.put(tree.getRootNode().getConcept(),
 							tree.getConceptIDSet());
 				}
 			}
 		}
 		return investigationTests;
 	}
 
 	@SuppressWarnings("unchecked")
 	@RequestMapping(method = RequestMethod.GET)
 	public String searchTest(
 			@RequestParam(value = "date", required = false) String dateStr,
			@RequestParam(value = "patientIdentifier") String patientIdentifier,
 			HttpServletRequest request, Model model) {
 		RadiologyService rs = (RadiologyService) Context
 				.getService(RadiologyService.class);
 		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
 		Date date = null;
 		try {
 			date = sdf.parse(dateStr);
			List<Patient> patients = Context.getPatientService().getPatients(
					patientIdentifier);
			if (!patients.isEmpty()) {
				Patient patient = patients.get(0);
 				List<RadiologyTest> radiologyTests = rs
 						.getRadiologyTestsByDateAndPatient(date, patient);
 				if ((radiologyTests != null) && (!radiologyTests.isEmpty())) {
 					Map<Concept, Set<Concept>> testTreeMap = (Map<Concept, Set<Concept>>) request
 							.getSession().getAttribute(
 									RadiologyConstants.SESSION_TEST_TREE_MAP);
 					List<TestResultModel> trms = renderTests(radiologyTests,
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
 		return "/module/radiology/patientreport/search";
 	}
 
 	private List<TestResultModel> renderTests(List<RadiologyTest> tests,
 			Map<Concept, Set<Concept>> investigationTests) {
 		List<TestResultModel> trms = new ArrayList<TestResultModel>();
 		for (RadiologyTest test : tests) {
 			if (test.getEncounter() != null) {
 				TestResultModel trm = new TestResultModel();
				trm.setInvestigation(RadiologyUtil.getConceptName(getInvestigationByTest(test,
						investigationTests)));
 				trm.setSet(test.getConcept().getName().getName());
 				trm.setLevel(TestResultModel.LEVEL_SET);
 				trm.setRadiologyTestId(test.getId());
 				trms.add(trm);
 			}
 		}
 		return trms;
 	}
 
 	private Concept getInvestigationByTest(RadiologyTest test,
 			Map<Concept, Set<Concept>> investigationTests) {
 		for (Concept investigation : investigationTests.keySet()) {
 			if (investigationTests.get(investigation).contains(
 					test.getConcept()))
 				return investigation;
 		}
 		return null;
 	}
 
 	private List<TestResultModel> formatTestResult(
 			List<TestResultModel> testResultModels) {
 		Collections.sort(testResultModels);
 		List<TestResultModel> trms = new ArrayList<TestResultModel>();
 		String investigation = null;
 		for (TestResultModel trm : testResultModels) {
 			if (!trm.getInvestigation().equalsIgnoreCase(investigation)) {
 				investigation = trm.getInvestigation();
 				TestResultModel t = new TestResultModel();
 				t.setInvestigation(investigation);
 				t.setLevel(TestResultModel.LEVEL_INVESTIGATION);
 				trms.add(t);
 			}
 			trms.add(trm);
 		}
 		return trms;
 	}
 }
