 package org.openmrs.module.chartsearch.fragment.controller;
 
 /**
  * Created by Tal on 3/19/14.
  */

 import org.openmrs.api.context.Context;
 import org.openmrs.module.chartsearch.ChartListItem;
 import org.openmrs.module.chartsearch.SearchAPI;
 import org.openmrs.module.chartsearch.SearchPhrase;
 import org.openmrs.module.chartsearch.solr.ChartSearchSearcher;
 import org.openmrs.module.chartsearch.web.dwr.DWRChartSearchService;
 import org.openmrs.ui.framework.annotation.BindParams;
 import org.openmrs.ui.framework.page.PageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class LeftPaneFragmentController
 {
    private static final Logger log = LoggerFactory.getLogger(LeftPaneFragmentController.class);
     private ChartSearchSearcher searcher = getComponent(ChartSearchSearcher.class);
 
     public void get()
     {
         //TODO - Eli - what's need to be here?
     }
 
     /*
     *   //TODO - Eli please fill it
     *   Function post
     *   @param model -the model of the page
     *   @param searchPhrase is the phrase we want to search
     *   @param patient - requests the patient's id
     *   @return redirection to the same page with the same patient's id.
      */
    //public String post(PageModel model, @RequestParam("search_phrase") SearchPhrase searchPhrase ,@RequestParam("patientId") Integer patient)
    public String post(PageModel model, @BindParams SearchPhrase search_phrase ,@RequestParam("patientId") Integer patient)

     {
         SearchAPI searchAPI =SearchAPI.getInstance();
         model.addAttribute("patientID_from_get", patient); //get patient

         Integer length = Integer.valueOf(999999999); //amount of obs we want - all of them
         Integer start = Integer.valueOf(0);//starting from first obs.
         List<ChartListItem> items = new ArrayList<ChartListItem>();
         try
         {
            items = searcher.getDocumentList(patient, search_phrase.getPhrase(), start, length); //searching for the phrase.
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
         if(items == null)
         {
             items = new ArrayList<ChartListItem>();
             ChartListItem itemsIsNull = new ChartListItem();
             itemsIsNull.setConceptName("items list returned from search is null");
             items.add(itemsIsNull);
         }
         List<ChartListItem> updatedItems = new ArrayList<ChartListItem>();
         for(ChartListItem observation : items) //loop to get full details about observations.
         {
             ChartListItem updatedObservation = DWRChartSearchService.getObservationDetails(observation.getObsId());
             updatedItems.add(updatedObservation);
         }
         searchAPI.setResults(updatedItems); //setting results to show.
         return "redirect:chartsearch/chartsearch.page?patientId=" + patient;
     }
 
     // TODO - ELI - add javadoc please
     private <T> T getComponent(Class<T> clazz)
     {
         List<T> list = Context.getRegisteredComponents(clazz);
         if (list == null || list.size() == 0)
             throw new RuntimeException("Cannot find component of " + clazz);
         return list.get(0);
     }
 }
