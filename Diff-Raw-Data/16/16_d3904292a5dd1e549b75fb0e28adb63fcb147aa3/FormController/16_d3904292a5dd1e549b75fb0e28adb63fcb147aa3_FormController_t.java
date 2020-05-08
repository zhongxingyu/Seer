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
 
 package org.openmrs.module.laboratory.web.controller.form;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang.StringUtils;
 import org.openmrs.Concept;
 import org.openmrs.ConceptAnswer;
 import org.openmrs.ConceptNumeric;
 import org.openmrs.ConceptSet;
 import org.openmrs.Encounter;
 import org.openmrs.Obs;
import org.openmrs.Patient;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.hospitalcore.model.LabTest;
 import org.openmrs.module.laboratory.LaboratoryService;
 import org.openmrs.module.laboratory.web.util.LaboratoryUtil;
 import org.openmrs.module.laboratory.web.util.ParameterModel;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller("LaboratoryShowFormController")
 @RequestMapping("/module/laboratory/showForm.form")
 public class FormController {
 
 	/**
 	 * Show for to enter result
 	 * 
 	 * @param encounterId
 	 * @param conceptId
 	 * @param model
 	 * @return
 	 */
 	@RequestMapping(method = RequestMethod.GET)
 	public String showForm(
 			@RequestParam(value = "encounterId") Integer encounterId,
 			@RequestParam(value = "conceptId") Integer conceptId, Model model) {
 
 		Concept concept = Context.getConceptService().getConcept(conceptId);
 		List<ParameterModel> parameters = generateParameterModels(concept);
 
 		Encounter encounter = Context.getEncounterService().getEncounter(
 				encounterId);
 
 		model.addAttribute("parameters", parameters);
 		model.addAttribute("encounterId", encounter.getEncounterId());
 		LaboratoryUtil.generateDataFromEncounter(model, encounter);
 		return "/module/laboratory/form/show";
 	}
 
 	/*
 	 * Generate parameter list
 	 */
 	private List<ParameterModel> generateParameterModels(Concept concept) {
 		List<ParameterModel> parameters = new ArrayList<ParameterModel>();
 		if (concept.getConceptClass().getName().equalsIgnoreCase("LabSet")) {
 			List<Concept> concepts = getParameterConcepts(concept);
 			for (Concept c : concepts) {
 				ParameterModel parameter = generateParameterModel(c);
 				if (!StringUtils.isBlank(parameter.getId())) {
 					parameters.add(parameter);
 				}
 			}
 		} else {
 			ParameterModel parameter = generateParameterModel(concept);
 			parameters.add(parameter);
 		}
 		return filterParameters(parameters);
 	}
 
 	private List<ParameterModel> filterParameters(
 			List<ParameterModel> parameters) {
 		List<ParameterModel> filteredParameters = new ArrayList<ParameterModel>();
 		for (ParameterModel parameter : parameters) {
 			if (parameter != null)
 				System.out.println(String.format("parameter %s %s",
 						parameter.getTitle(), parameter.getType()));
 			filteredParameters.add(parameter);
 		}
 		return filteredParameters;
 	}
 
 	private List<Concept> getParameterConcepts(Concept concept) {
 
 		List<Concept> concepts = new ArrayList<Concept>();
 		for (ConceptSet cs : concept.getConceptSets()) {
 			Concept c = cs.getConcept();
 			concepts.add(c);
 		}
 		return concepts;
 	}
 
 	private ParameterModel generateParameterModel(Concept concept) {
 		ParameterModel parameter = new ParameterModel();
 		if (concept.getDatatype().getName().equalsIgnoreCase("Text")) {
 			parameter.setId(concept.getName().getName());
 			parameter.setType("text");
 		} else if (concept.getDatatype().getName().equalsIgnoreCase("Numeric")) {
 			parameter.setId(concept.getName().getName());
 			parameter.setType("text");
 			parameter.setUnit(getUnit(concept));		
 			parameter.setValidator("number");
 		} else if (concept.getDatatype().getName().equalsIgnoreCase("Coded")) {
 			parameter.setId(concept.getName().getName());
 			parameter.setType("select");
 			parameter.getOptionValues().add("");
 			parameter.getOptionLabels().add("");
 			Set<Concept> options = new HashSet<Concept>();
 
 			for (ConceptAnswer ca : concept.getAnswers()) {
 				Concept c = ca.getAnswerConcept();
 				options.add(c);
 			}
 
 			for (Concept option : options) {
 				parameter.getOptionValues().add(option.getName().getName());
 				parameter.getOptionLabels().add(option.getName().getName());
 			}
 		}
 		parameter.setValidator(parameter.getValidator() + " required");
 		parameter.setTitle(concept.getDatatype().getName());
 		return parameter;
 	}
 	
 	private String getUnit(Concept concept){
 		ConceptNumeric cn = Context.getConceptService()
 		.getConceptNumeric(concept.getConceptId());
 		return cn.getUnits();
 	}
 
 	/**
 	 * Save form values into an existing encounter
 	 * 
 	 * @param request
 	 * @param encounterId
 	 * @param model
 	 * @return
 	 */
 	@RequestMapping(method = RequestMethod.POST)
 	public String saveEncounter(HttpServletRequest request,
 			@RequestParam("encounterId") Integer encounterId, Model model) {
 		Map<String, String> parameters = buildParameterList(request);
 		Encounter encounter = Context.getEncounterService().getEncounter(
 				encounterId);
 		
 		LabTest test = Context.getService(LaboratoryService.class).getLaboratoryTest(encounter);
 		
 		if (encounter != null) {
 			for (String key : parameters.keySet()) {
 				Concept concept = LaboratoryUtil.searchConcept(key);
 				Obs obs = insertValue(encounter, concept, parameters.get(key), test);
 				if (obs.getId() == null)
 					encounter.addObs(obs);
 			}
 			Context.getEncounterService().saveEncounter(encounter);
 			model.addAttribute("status", "success");
 			return "/module/laboratory/form/enterForm";
 		}
 		model.addAttribute("status", "fail");
 		return "/module/laboratory/form/enterForm";
 
 	}
 
 	@SuppressWarnings("rawtypes")
 	private Map<String, String> buildParameterList(HttpServletRequest request) {
 		Map<String, String> parameters = new HashMap<String, String>();
 		for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
 			String parameterName = (String) e.nextElement();
 			if (!parameterName.equalsIgnoreCase("id"))
 				if (!parameterName.equalsIgnoreCase("mode"))
 					if (!parameterName.equalsIgnoreCase("encounterId"))
 						parameters.put(parameterName,
 								request.getParameter(parameterName));
 
 		}
 		return parameters;
 	}
 
 	private Obs insertValue(Encounter encounter, Concept concept, String value, LabTest test) {
 
 		Obs obs = getObs(encounter, concept);
 		obs.setConcept(concept);
 		obs.setOrder(test.getOrder());
 		if (concept.getDatatype().getName().equalsIgnoreCase("Text")) {
 			obs.setValueText(value);
 		} else if (concept.getDatatype().getName().equalsIgnoreCase("Numeric")) {
 			obs.setValueNumeric(new Double(value));
 		} else if (concept.getDatatype().getName().equalsIgnoreCase("Coded")) {
 			Concept answerConcept = LaboratoryUtil.searchConcept(value);
 			obs.setValueCoded(answerConcept);
 		}
 		return obs;
 	}
 
 	private Obs getObs(Encounter encounter, Concept concept) {
 		for (Obs obs : encounter.getAllObs()) {
 			if (obs.getConcept().equals(concept))
 				return obs;
 		}
 		return new Obs();
 	}
 
 }
