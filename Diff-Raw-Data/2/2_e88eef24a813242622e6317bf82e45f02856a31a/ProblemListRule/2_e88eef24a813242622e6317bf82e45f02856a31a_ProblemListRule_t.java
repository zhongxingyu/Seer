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
 
 package org.openmrs.module.clinicalsummary.rule.problem;
 
 import java.util.*;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.ConceptSet;
 import org.openmrs.Obs;
 import org.openmrs.logic.LogicContext;
 import org.openmrs.logic.LogicException;
 import org.openmrs.logic.result.Result;
 import org.openmrs.module.clinicalsummary.cache.CacheUtils;
 import org.openmrs.module.clinicalsummary.enumeration.FetchOrdering;
 import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
 import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
 import org.openmrs.module.clinicalsummary.rule.EvaluableParameter;
 import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
 import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithRestrictionRule;
 import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
 import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
 import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
 import org.openmrs.util.OpenmrsUtil;
 
 /**
  */
 public class ProblemListRule extends EvaluableRule {
 
 	private static final Log log = LogFactory.getLog(ProblemListRule.class);
 
 	public static final String TOKEN = "Problem List";
 
 	/**
 	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
 	 */
 	@Override
 	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
 		Result result = new Result();
 
 		// see if we can get the reference encounter
 		Date referenceDate = null;
 		if (parameters.containsKey(EvaluableConstants.ENCOUNTER_TYPE)) {
 			parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);
 			EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();
 			Result encounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);
 			if (CollectionUtils.isNotEmpty(encounterResults))
 				referenceDate = encounterResults.getResultDate();
 		}
 
 		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
 		// get the list of all problem resolved
 		parameters.put(EvaluableConstants.OBS_FETCH_ORDER, FetchOrdering.ORDER_ASCENDING.getValue());
 		parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.PROBLEM_RESOLVED, EvaluableNameConstants.PROBLEM_ADDED));
 		Result problemResults = obsWithRestrictionRule.eval(context, patientId, parameters);
 
 		// get the problem resolved concept
 		Concept resolvedConcept = CacheUtils.getConcept(EvaluableNameConstants.PROBLEM_RESOLVED);
 
 		// search for the unresolved problem list
 		Concept concept = CacheUtils.getConcept(EvaluableNameConstants.PROBLEM_LIST_FOR_CLINICAL_SUMMARY);
 		Collection<ConceptSet> conceptSets = (concept == null ? new HashSet<ConceptSet>() : concept.getConceptSets());
 		// process the unresolved problems
 		List<Concept> unresolvedConcepts = new ArrayList<Concept>();
 		for (ConceptSet conceptSet : conceptSets)
 			unresolvedConcepts.add(conceptSet.getConcept());
 
 		// create a map of problems added to the system
 		Integer counter = 0;
 		Map<Concept, Result> addedMap = new HashMap<Concept, Result>();
 		while (counter < problemResults.size()) {
 			Result problemResult = problemResults.get(counter++);
 			if (OpenmrsUtil.compareWithNullAsLatest(referenceDate, problemResult.getResultDate()) == 1) {
 				Obs obs = (Obs) problemResult.getResultObject();
 				if (OpenmrsUtil.nullSafeEquals(obs.getConcept(), resolvedConcept)) {
					if (!OpenmrsUtil.collectionContains(unresolvedConcepts, obs.getValueCoded()))
 						addedMap.remove(obs.getValueCoded());
 				} else {
 					// add the added into the map
 					Result addedMapEntry = addedMap.get(problemResult.toConcept());
 					if (CollectionUtils.isEmpty(addedMapEntry)) {
 						addedMapEntry = new Result();
 						addedMap.put(problemResult.toConcept(), addedMapEntry);
 					}
 					addedMapEntry.add(problemResult);
 				}
 			}
 		}
 
 		// format it to list of list of problem added
 		for (Concept addedMapConcept : addedMap.keySet()) {
 			Result addedMapEntry = addedMap.get(addedMapConcept);
 			Collections.reverse(addedMapEntry);
 			result.add(addedMapEntry);
 		}
 
 		return result;
 	}
 
 	/**
 	 * @see org.openmrs.logic.Rule#getDependencies()
 	 */
 	@Override
 	public String[] getDependencies() {
 		return new String[]{ObsWithStringRestrictionRule.TOKEN};
 	}
 
 	/**
 	 * Get the token name of the rule that can be used to reference the rule from LogicService
 	 *
 	 * @return the token name
 	 */
 	@Override
 	protected String getEvaluableToken() {
 		return TOKEN;
 	}
 
 	/**
 	 * Whether the result of the rule should be cached or not
 	 *
 	 * @return true if the system should put the result into the caching system
 	 */
 	@Override
 	protected Boolean cacheable() {
 		return Boolean.TRUE;
 	}
 
 	/**
 	 * Get the definition of each parameter that should be passed to this rule execution
 	 *
 	 * @return all parameter that applicable for each rule execution
 	 */
 	@Override
 	public Set<EvaluableParameter> getEvaluationParameters() {
 		Set<EvaluableParameter> evaluableParameters = new HashSet<EvaluableParameter>();
 		evaluableParameters.add(EvaluableConstants.OPTIONAL_ENCOUNTER_TYPE_PARAMETER_DEFINITION);
 		return evaluableParameters;
 	}
 }
