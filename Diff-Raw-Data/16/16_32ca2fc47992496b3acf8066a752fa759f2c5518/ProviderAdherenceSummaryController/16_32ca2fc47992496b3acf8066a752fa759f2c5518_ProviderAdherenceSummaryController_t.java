 package org.motechproject.whp.reports.webservice.controller;
 
 import org.motechproject.whp.reports.contract.adherence.ProviderAdherenceSummaries;
 import org.motechproject.whp.reports.service.ProviderAdherenceDataService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
 
 @Controller
@RequestMapping(value = "/providerAdherenceSummary", produces = APPLICATION_JSON_VALUE)
 public class ProviderAdherenceSummaryController {
 
     private ProviderAdherenceDataService providerAdherenceDataService;
 
     @Autowired
     public ProviderAdherenceSummaryController(ProviderAdherenceDataService providerAdherenceDataService) {
         this.providerAdherenceDataService = providerAdherenceDataService;
     }
 
     @RequestMapping(method = RequestMethod.GET, value = "/{district}")
     @ResponseBody
     public ProviderAdherenceSummaries adherenceSummaries(@PathVariable String district){
         return providerAdherenceDataService.getAdherenceSummary(district);
     }
 }
