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
 package org.openmrs.module.clinicalsummary.rule;
 
 import org.apache.commons.lang.time.DateUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.logic.result.Result;
 import org.openmrs.logic.result.Result.Datatype;
 import org.openmrs.util.OpenmrsUtil;
 
 /**
  *
  */
 public class RuleUtils {
 	
 	private static final Log log = LogFactory.getLog(RuleUtils.class);
 	
 	/**
 	 * This class will never gets instantiated
 	 */
 	private RuleUtils() {
 	}
 	
 	/**
 	 * Cut a result to a certain size
 	 * 
 	 * @param obsResults original result object
 	 * @param codedValues optional parameter for coded obs to only include obs with the specified
 	 *            values
 	 * @param maxElement total number element in the output
 	 * @return sliced result. Slicing will happen in the middle. So, the element are: first ...
 	 *         E(maxElement - 2), E(maxElement - 1)
 	 */
 	public static Result sliceResult(Result obsResults, int maxElement) {
 		
 		Result slicedObservations = new Result();
 		
 		// get (maxElement - 1) number of observations from the descending observations list
 		int descCounter = 0;
 		while (descCounter < obsResults.size() && slicedObservations.size() < maxElement - 1) {
 			Result obsResult = obsResults.get(descCounter++);
 			slicedObservations.add(obsResult);
 		}
 		
 		if (!obsResults.isEmpty()) {
 			// get one from the descending observations list (get the earliest)
 			int ascCounter = obsResults.size();
 			while (ascCounter > 0 && slicedObservations.size() < maxElement) {
 				Result obsResult = obsResults.get(--ascCounter);
 				if (!slicedObservations.isEmpty()) {
 					Result lastResult = slicedObservations.get(slicedObservations.size() - 1);
 					// remove if we already have the earliest
 					if (OpenmrsUtil.nullSafeEquals(obsResult.toNumber(), lastResult.toNumber())
 					        && OpenmrsUtil.nullSafeEquals(obsResult.getResultDate(), lastResult.getResultDate())
 					        && OpenmrsUtil.nullSafeEquals(obsResult.toString(), lastResult.toString())
 					        && OpenmrsUtil.nullSafeEquals(obsResult.toConcept(), lastResult.toConcept()))
 						slicedObservations.remove(slicedObservations.size() - 1);
 				}
 				slicedObservations.add(obsResult);
 				// if we found the earliest obs that we needed than we should stop and get out
 				break;
 			}
 		}
 		
 		return slicedObservations;
 	}
 	
 	/**
 	 * @param obsResults
 	 * @return
 	 */
 	public static Result consolidate(Result obsResults) {
 		Result consolidatedResult = new Result();
 		if (!obsResults.isEmpty()) {
 			Result prevResult = obsResults.get(0);
 			for (int i = 1; i < obsResults.size(); i++) {
 				Result currentResult = obsResults.get(i);
 				// skip when they are the same result :)
 				if (log.isDebugEnabled())
 					log.debug("current: " + currentResult.getResultDate() + " prev: " + prevResult.getResultDate());
 				
 				if (DateUtils.isSameDay(prevResult.getResultDate(), currentResult.getResultDate())
 				        && OpenmrsUtil.nullSafeEquals(prevResult.getDatatype(), currentResult.getDatatype())) {
 					
 					if (log.isDebugEnabled())
 						log.debug("prev number: " + prevResult.toNumber() + ", current number: " + currentResult.toNumber());
 					
 					if (OpenmrsUtil.nullSafeEquals(currentResult.getDatatype(), Datatype.NUMERIC)
 					        && OpenmrsUtil.nullSafeEquals(prevResult.toNumber(), currentResult.toNumber()))
 						continue;
 					
 					if (log.isDebugEnabled())
 						log.debug("prev concept: " + prevResult.toConcept() + ", current concept: " + currentResult.toConcept());
 					
					if (OpenmrsUtil.nullSafeEquals(currentResult.getDatatype(), Datatype.CODED)
 					        && OpenmrsUtil.nullSafeEquals(prevResult.toConcept(), currentResult.toConcept()))
 						continue;
 				}
 				consolidatedResult.add(prevResult);
 				prevResult = currentResult;
 			}
 			consolidatedResult.add(prevResult);
 		}
 		return consolidatedResult;
 	}
 	
 }
