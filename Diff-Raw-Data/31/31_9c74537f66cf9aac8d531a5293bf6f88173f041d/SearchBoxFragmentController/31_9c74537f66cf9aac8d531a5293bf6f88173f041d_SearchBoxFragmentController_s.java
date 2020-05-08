 package org.openmrs.module.chartsearch.fragment.controller;
 
 
 
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.openmrs.module.chartsearch.SearchAPI;
 import org.openmrs.module.chartsearch.SearchPhrase;
 import org.openmrs.ui.framework.fragment.FragmentModel;
 import org.openmrs.ui.framework.annotation.BindParams;
 public class SearchBoxFragmentController {
 
 
 	public void get() {
 	}
 	
 	
 	
 	
 	
 	
	public String post(@BindParams SearchPhrase searchPhrase) {
 		//SearchAPI.setSearchPhrase(searchPhrase);
 		//SearchAPI.search();
 		List<String> temp = new ArrayList<String>();
 		temp.add(searchPhrase.getPhrase());
 		SearchAPI.setResults(temp);
		return "redirect:chartsearch/chartsearch.page";
 	}
 	
 	
 	
 	
 	
 	
 }
