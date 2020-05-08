 package org.openmrs.module.chartsearch.page.controller;
 
 /**
  * Created by Eli on 10/03/14.
  */
 
import org.apache.solr.handler.dataimport.custom.IndexClearStrategies;
 import org.openmrs.Patient;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.appui.UiSessionContext;
 import org.openmrs.module.chartsearch.ChartListItem;
 import org.openmrs.module.chartsearch.ObsItem;
 import org.openmrs.module.chartsearch.SearchAPI;
 import org.openmrs.module.chartsearch.SearchPhrase;
 import org.openmrs.module.chartsearch.solr.ChartSearchIndexer;
 import org.openmrs.module.chartsearch.solr.ChartSearchSearcher;
 import org.openmrs.module.chartsearch.web.dwr.DWRChartSearchService;
 import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
 import org.openmrs.ui.framework.annotation.BindParams;
 import org.openmrs.ui.framework.annotation.InjectBeans;
 import org.openmrs.ui.framework.page.PageModel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ChartsearchPageController {
 	
 	private static final Logger log = LoggerFactory.getLogger(ChartsearchPageController.class);
 	
 	private ChartSearchIndexer chartSearchIndexer = getComponent(ChartSearchIndexer.class);
 	
 	private ChartSearchSearcher searcher = getComponent(ChartSearchSearcher.class);
 	
 	public void controller(PageModel model,@BindParams SearchPhrase search_phrase, UiSessionContext sessionContext, @RequestParam("patientId") Patient patient,
 	                       @InjectBeans PatientDomainWrapper patientDomainWrapper) {
 
         //indexing the patient on enter
 
 
 		patientDomainWrapper.setPatient(patient);
 		model.addAttribute("patient", patientDomainWrapper);
 
         SearchAPI searchAPIInstance = SearchAPI.getInstance();
 		
 		log.info("getting patient ID :" + patient);
 		log.info("trying to index a patient");
 		
 		if (chartSearchIndexer != null && patient != null) {
			//chartSearchIndexer.clearIndex(IndexClearStrategies.IDS.toString(), patient.getPatientId()+"", 0, 0);
 			chartSearchIndexer.indexPatientData(patient.getPatientId());
 		}
 		log.info("indexed patient");
 
         //Searching an empty phrase to get all results to show at start
 
 
         SearchPhrase emptyPhrase = new SearchPhrase("");
 
         List<ChartListItem> items = searchAPIInstance.search(patient.getPatientId() , search_phrase);
         List<ChartListItem> updatedItems = new ArrayList<ChartListItem>();
 
         //loop to get full details about observations.
 
         for (ChartListItem observation : items)
         {
             int itemObsId = -1;
             if (observation instanceof ObsItem) {
                 itemObsId = ((ObsItem) observation).getObsId();
             }
             ChartListItem updatedObservation = DWRChartSearchService.getObservationDetails(itemObsId);
             updatedItems.add(updatedObservation);
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
