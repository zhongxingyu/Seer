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
 package org.openmrs.module.conceptsearch.web.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.servlet.http.HttpSession;
 import org.openmrs.Concept;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.conceptsearch.ConceptSearch;
 import org.openmrs.module.conceptsearch.ConceptSearchResult;
 import org.openmrs.module.conceptsearch.ConceptSearchService;
 import org.springframework.beans.support.PagedListHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.context.request.WebRequest;
 
 /**
  * Controller to handle all searches performed by basicsearch.jsp.
  */
 @Controller
 public class BasicSearchFormController extends AbstractSearchFormController {
 
 	@ModelAttribute("conceptQuery")
 	public String getConceptQuery(@RequestParam(value="conceptQuery", required=false) String conceptQuery) {
 		return (conceptQuery == null ? "" : conceptQuery);
     }
 	
 	@InitBinder("conceptQuery")
 	public void initBinder(WebDataBinder dataBinder) {
 		dataBinder.setAllowedFields(new String[] { "conceptQuery" });
 		dataBinder.setRequiredFields(new String[] { "conceptQuery" });
 	}
 	
 	@RequestMapping(value = "/module/conceptsearch/basicSearch", method = RequestMethod.GET)
 	public void showBasicSearch(ModelMap model, WebRequest request, HttpSession session) {
 		//display basicSearch.jsp	
 		session.removeAttribute("searchResult");
 		session.removeAttribute("sortResults");
 		session.removeAttribute("conceptSearch");
 		
 	}
 	
 	@RequestMapping(value = "/module/conceptsearch/basicSearch", method = RequestMethod.POST)
 	public void performBasicSearch(@ModelAttribute("conceptQuery") String searchQuery, BindingResult errors, ModelMap model, WebRequest request, HttpSession session) {
 		ConceptSearchService searchService = (ConceptSearchService) Context.getService(ConceptSearchService.class);
 		List<Concept> rslt = new ArrayList<Concept>();
 		ConceptSearch cs = new ConceptSearch("");
 		
 		if (searchQuery != null && searchQuery.length()>0) {
 			cs.setSearchQuery(searchQuery);
			rslt = searchService.getConcepts(cs);
 			
 			//add the results to a DTO to avoid Hibernate's lazy loading
 			List<ConceptSearchResult> resList = new ArrayList<ConceptSearchResult>();
 			for (Concept c : rslt) {
 				if (cs.getConceptUsedAs() == null || searchService.isConceptUsedAs(c, cs)) {
 					ConceptSearchResult res = new ConceptSearchResult(c);
 					res.setNumberOfObs(searchService.getNumberOfObsForConcept(c.getConceptId()));
 					resList.add(res);
 				}
 			}
 			
 			// add results to ListHolder
 			PagedListHolder resListHolder = new PagedListHolder(resList);
 			resListHolder.setPageSize(DEFAULT_RESULT_PAGE_SIZE);
 			
 			model.addAttribute("searchResult", resListHolder);
 			model.addAttribute("conceptSearch", cs);
 			
 			session.setAttribute("sortResults", resListHolder);
 			session.setAttribute("conceptSearch", cs);
 		}
 	}
 	
 	@RequestMapping(value = "/module/conceptsearch/basicSearch", method = RequestMethod.GET, params = "count")
 	public void setConceptsPerPage(ModelMap model, WebRequest request, HttpSession session) {
 		super.setConceptsPerPage(model, request, session);
 	}
 	
 	@RequestMapping(value = "/module/conceptsearch/basicSearch", method = RequestMethod.GET, params = "page")
 	public void switchToPage(@RequestParam("page") String page, ModelMap model, WebRequest request, HttpSession session) {
 		super.switchToPage(page, model, request, session);
 	}
 	
 	@RequestMapping(value = "/module/conceptsearch/basicSearch", method = RequestMethod.GET, params = "sort")
     public void sortResultsView(ModelMap model, WebRequest request, HttpSession session) {
 		super.sortResultsView(model, request, session);
 	}
 	
 }
