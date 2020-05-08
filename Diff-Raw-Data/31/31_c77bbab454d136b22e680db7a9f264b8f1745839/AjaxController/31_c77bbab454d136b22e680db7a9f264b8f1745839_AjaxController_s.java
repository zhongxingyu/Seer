 /**
  *  Copyright 2011 Society for Health Information Systems Programmes, India (HISP India)
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
 
 package org.openmrs.module.laboratory.web.ajax;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.openmrs.Concept;
 import org.openmrs.ConceptWord;
 import org.openmrs.Order;
 import org.openmrs.Patient;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.hospitalcore.model.LabTest;
 import org.openmrs.module.hospitalcore.util.PatientUtil;
 import org.openmrs.module.laboratory.LaboratoryService;
 import org.openmrs.module.laboratory.util.LaboratoryConstants;
 import org.openmrs.module.laboratory.web.util.LaboratoryUtil;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller("LaboratoryAjaxController")
 public class AjaxController {
 
 	/**
 	 * Accept a test
 	 * 
 	 * @param orderId
 	 * @param model
 	 * @return id of accepted laboratory test
 	 */
 	@RequestMapping(value = "/module/laboratory/ajax/acceptTest.htm", method = RequestMethod.GET)
 	public String acceptTest(@RequestParam("orderId") Integer orderId,
 			@RequestParam("date") String dateStr,
 			@RequestParam("sampleId") String sampleId, Model model) {
 		Order order = Context.getOrderService().getOrder(orderId);
 		if (order != null) {
 			try {
 				LaboratoryService ls = (LaboratoryService) Context
 						.getService(LaboratoryService.class);
 				Integer acceptedTestId = ls.acceptTest(order, sampleId);
 				model.addAttribute("acceptedTestId", acceptedTestId);
 				if (acceptedTestId > 0) {
 					model.addAttribute("status", "success");
 				} else {
 					model.addAttribute("status", "fail");
 					if (acceptedTestId == LaboratoryConstants.ACCEPT_TEST_RETURN_ERROR_EXISTING_SAMPLEID) {
 						model.addAttribute("error", "Existing sample id found");
 					} else if (acceptedTestId == LaboratoryConstants.ACCEPT_TEST_RETURN_ERROR_EXISTING_TEST) {
 						model.addAttribute("error",
 								"Existing accepted test found");
 					}
 				}
 			} catch (Exception e) {
 				model.addAttribute("acceptedTestId", "0");
 			}
 		}
 		return "/module/laboratory/ajax/acceptTest";
 	}
 
 	/**
 	 * Complete a test
 	 * 
 	 * @param testId
 	 * @param model
 	 * @return
 	 */
 	@RequestMapping(value = "/module/laboratory/ajax/completeTest.htm", method = RequestMethod.GET)
 	public String completeTest(@RequestParam("testId") Integer testId,
 			Model model) {
 		LaboratoryService ls = (LaboratoryService) Context
 				.getService(LaboratoryService.class);
 		LabTest test = ls.getLaboratoryTest(testId);
 		String completeStatus = ls.completeTest(test);
 		model.addAttribute("completeStatus", completeStatus);
 		return "/module/laboratory/ajax/completeTest";
 	}
 
 	/**
 	 * Show patient/test information when showing on patient report page
 	 * 
 	 * @param patientIdentifier
 	 * @param orderId
 	 * @param model
 	 * @return
 	 */
 	@RequestMapping(value = "/module/laboratory/ajax/showTestInfo.htm", method = RequestMethod.GET)
 	public String showTestInfo(
			@RequestParam("patientIdentifier") String patientIdentifier,
 			@RequestParam(value = "orderId", required = false) Integer orderId,
 			Model model) {
		List<Patient> patients = Context.getPatientService().getPatients(
				patientIdentifier);
		if (!patients.isEmpty()) {
			Patient patient = patients.get(0);
 			model.addAttribute("patient_identifier", patient
 					.getPatientIdentifier().getIdentifier());
 			model.addAttribute("patient_age", patient.getAge());
 			model.addAttribute("patient_gender", patient.getGender());
 			model.addAttribute("patient_name", PatientUtil.getFullName(patient));
 		}
 		if (orderId != null) {
 			Order order = Context.getOrderService().getOrder(orderId);
 			if (order != null) {
 				model.addAttribute("test_orderDate",
 						LaboratoryUtil.formatDate(order.getDateCreated()));
 				model.addAttribute("test_name", order.getConcept().getName()
 						.getName());
 			}
 		}
 		return "/module/laboratory/ajax/showTestInfo";
 	}
 
 	@SuppressWarnings("unchecked")
 	@RequestMapping(value = "/module/laboratory/ajax/getDefaultSampleId.htm", method = RequestMethod.GET)
 	public void getDefaultSampleId(@RequestParam("orderId") Integer orderId,
 			HttpServletResponse response, HttpServletRequest request) throws IOException, ParseException {
 		response.setContentType("text/html;charset=UTF-8");
 		PrintWriter out = response.getWriter();
 		Map<Concept, Set<Concept>> testTreeMap = (Map<Concept, Set<Concept>>) request
 		.getSession().getAttribute(
 				LaboratoryConstants.SESSION_TEST_TREE_MAP);
 		Order order = Context.getOrderService().getOrder(orderId);
 		LaboratoryService ls = (LaboratoryService) Context
 				.getService(LaboratoryService.class);
 		out.print(ls.getDefaultSampleId(LaboratoryUtil.getInvestigationName(
 				order.getConcept(), testTreeMap)));
 	}
 	
 	/**
 	 * Concept search autocomplete for form
 	 * 
 	 * @param name
 	 * @param model
 	 * @return
 	 */
 	@SuppressWarnings("deprecation")
 	@RequestMapping(value = "/module/laboratory/ajax/autocompleteConceptSearch.htm", method = RequestMethod.GET)
 	public String autocompleteConceptSearch(
 			@RequestParam(value = "q", required = false) String name,
 			Model model) {
 		List<ConceptWord> cws = Context.getConceptService().findConcepts(name,
 				new Locale("en"), false);
 		Set<String> conceptNames = new HashSet<String>();
 		for (ConceptWord word : cws) {
 			String conceptName = word.getConcept().getName().getName();
 			conceptNames.add(conceptName);
 		}
 		List<String> concepts = new ArrayList<String>();
 		concepts.addAll(conceptNames);
 		Collections.sort(concepts, new Comparator<String>() {
 
 			public int compare(String o1, String o2) {
 				return o1.compareToIgnoreCase(o2);
 			}
 		});
 		model.addAttribute("conceptNames", concepts);
 		return "/module/laboratory/ajax/autocompleteConceptSearch";
 	}
 	
 	@RequestMapping(value = "/module/laboratory/ajax/validateRescheduleDate.htm", method = RequestMethod.GET)	
 	public void validateRescheduleDate(@RequestParam("rescheduleDate") String rescheduleDateStr, HttpServletResponse response) throws IOException, ParseException{
 		
 		response.setContentType("text/html;charset=UTF-8");
 		PrintWriter writer = response.getWriter();
 		
 		Date rescheduleDate = LaboratoryUtil.parseDate(rescheduleDateStr + " 00:00:00");
 		Date now = new Date();
 		String currentDateStr = LaboratoryUtil.formatDate(now) + " 00:00:00";
 		Date currentDate = LaboratoryUtil.parseDate(currentDateStr);
 		if(rescheduleDate.after(currentDate))
 			writer.print("success");
 		else
 			writer.print("fail");
 	}
 }
