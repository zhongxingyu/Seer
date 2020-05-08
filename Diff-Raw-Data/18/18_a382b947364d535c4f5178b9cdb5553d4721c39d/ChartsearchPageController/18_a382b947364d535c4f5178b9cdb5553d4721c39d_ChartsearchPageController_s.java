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
 package org.openmrs.module.chartsearch.page.controller;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.openmrs.Patient;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.appui.UiSessionContext;
 import org.openmrs.module.chartsearch.ChartListItem;
 import org.openmrs.module.chartsearch.EncounterItem;
 import org.openmrs.module.chartsearch.FormItem;
 import org.openmrs.module.chartsearch.ObsItem;
 import org.openmrs.module.chartsearch.SearchAPI;
 import org.openmrs.module.chartsearch.SearchPhrase;
 import org.openmrs.module.chartsearch.solr.ChartSearchIndexer;
 import org.openmrs.module.chartsearch.web.dwr.DWRChartSearchService;
 import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
 import org.openmrs.ui.framework.annotation.BindParams;
 import org.openmrs.ui.framework.annotation.InjectBeans;
 import org.openmrs.ui.framework.page.PageModel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.bind.annotation.RequestParam;
 
 public class ChartsearchPageController {
 	
 	private static final Logger log = LoggerFactory.getLogger(ChartsearchPageController.class);
 	
 	private ChartSearchIndexer chartSearchIndexer = getComponent(ChartSearchIndexer.class);
 	
	public void controller(PageModel model, @BindParams SearchPhrase search_phrase, UiSessionContext sessionContext,
	                       @RequestParam("patientId") Patient patient,
 	                       @InjectBeans PatientDomainWrapper patientDomainWrapper, HttpServletRequest request) {
 		
		patientDomainWrapper.setPatient(patient);
		model.addAttribute("patient", patientDomainWrapper);
		
		SearchAPI searchAPIInstance = SearchAPI.getInstance();
		indexPatientData(patient);
		searchAndReturnResults(search_phrase, patient, request, searchAPIInstance);
 	}
 	
 	private void indexPatientData(Patient patient) {
 		log.info("getting patient ID :" + patient);
 		log.info("trying to index a patient");
 		if (chartSearchIndexer != null && patient != null) {
 			chartSearchIndexer.indexPatientData(patient.getPatientId());
 		}
 		log.info("indexed patient");
 	}
 	
 	public static void searchAndReturnResults(SearchPhrase search_phrase, Patient patient, HttpServletRequest request,
 	                                          SearchAPI searchAPIInstance) {
 		//get all checked categories from the UI and pass them into categories array
 		String[] categories = request.getParameterValues("categories");
 		if (categories == null) {
 			categories = new String[0];
 		}
 		List<String> selectedCategories = Arrays.asList(categories);
 		List<ChartListItem> items = searchAPIInstance.search(patient.getPatientId(), search_phrase, selectedCategories);
 		List<ChartListItem> updatedItems = new ArrayList<ChartListItem>();
 		//loop to get full details about observations.
 		for (ChartListItem chartListItem : items) {
 			if (chartListItem instanceof ObsItem) {
 				int itemObsId = ((ObsItem) chartListItem).getObsId();
 				ChartListItem updatedObservation = DWRChartSearchService.getObservationDetails(itemObsId);
 				updatedItems.add(updatedObservation);
 			}
 			if (chartListItem instanceof FormItem) {
 				updatedItems.add(chartListItem);
 			}
 			
 			if (chartListItem instanceof EncounterItem) {
 				updatedItems.add(chartListItem);
 			}
 			
 		}
 		//setting results to show.
 		searchAPIInstance.setResults(updatedItems);
 	}
 	
 	private <T> T getComponent(Class<T> clazz) {
 		List<T> list = Context.getRegisteredComponents(clazz);
 		if (list == null || list.size() == 0)
 			throw new RuntimeException("Cannot find component of " + clazz);
 		return list.get(0);
 	}
 	
 }
