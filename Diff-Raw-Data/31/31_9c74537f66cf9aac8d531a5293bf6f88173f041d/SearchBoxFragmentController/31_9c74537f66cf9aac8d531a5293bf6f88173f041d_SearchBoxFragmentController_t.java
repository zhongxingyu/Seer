 package org.openmrs.module.chartsearch.fragment.controller;
 
 
 
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.openmrs.module.chartsearch.SearchAPI;
 import org.openmrs.module.chartsearch.SearchPhrase;
 import org.openmrs.ui.framework.fragment.FragmentModel;
 import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

 public class SearchBoxFragmentController {
 
 
 	public void get() {
 	}
 	
 	
 	
 	
 	
 	
	public String post(PageModel model, @BindParams SearchPhrase searchPhrase ,@RequestParam("patientId") Integer patient) {
        model.addAttribute("patientID_from_get", patient);
 		//SearchAPI.setSearchPhrase(searchPhrase);
 		//SearchAPI.search();
 		List<String> temp = new ArrayList<String>();
 		temp.add(searchPhrase.getPhrase());
 		SearchAPI.setResults(temp);
		return "redirect:chartsearch/chartsearch.page?patientId=" + patient;
 	}
 	
 	
 	
 	
 	
 	
 }
