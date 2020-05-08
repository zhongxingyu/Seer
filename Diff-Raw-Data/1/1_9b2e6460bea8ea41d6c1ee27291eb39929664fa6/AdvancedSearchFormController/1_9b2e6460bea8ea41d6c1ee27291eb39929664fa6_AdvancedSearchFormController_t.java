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
 package org.openmrs.module.conceptmanagement.web.controller;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.ConceptClass;
 import org.openmrs.ConceptDatatype;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.conceptmanagement.ConceptComparator;
 import org.openmrs.module.conceptmanagement.ConceptPageCount;
 import org.openmrs.module.conceptmanagement.ConceptSearch;
 import org.openmrs.module.conceptmanagement.ConceptSearchResult;
 import org.openmrs.module.conceptmanagement.ConceptSearchService;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.context.request.WebRequest;
 
 /**
  * Controller to handle all searches done by the advancedsearch.jsp. All search criteria will be
  * stored in a ConceptSearch object and the results will be kept in session, because other methods
  * will need them.
  */
 @Controller
 public class AdvancedSearchFormController {
 	
 	/** Logger for this class and subclasses */
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	@ModelAttribute("dataTypes")
 	public List<ConceptDatatype> populateDataTypes() {
 		ConceptSearchService service = (ConceptSearchService) Context.getService(ConceptSearchService.class);
 		return service.getAllConceptDatatypes();
 	}
 	
 	@ModelAttribute("conceptClasses")
 	public List<ConceptClass> populateConceptClasses() {
 		ConceptSearchService service = (ConceptSearchService) Context.getService(ConceptSearchService.class);
 		return service.getAllConceptClasses();
 	}
 	
 	@RequestMapping(value = "/module/conceptmanagement/advancedSearch", method = RequestMethod.GET)
 	public void showAdvancedSearch(ModelMap model, WebRequest request, HttpSession session) {
 		
 		//reset all session objects used by this controller
 		session.removeAttribute("searchResult");
 		session.removeAttribute("sortResults");
 		session.removeAttribute("conceptSearch");
 		session.removeAttribute("countConcept");
 		
 		ConceptPageCount conCount = new ConceptPageCount();
 		session.setAttribute("countConcept", conCount);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@RequestMapping(value = "/module/conceptmanagement/advancedSearch", method = RequestMethod.GET, params = "count")
 	public void setConceptsPerPage(ModelMap model, WebRequest request, HttpSession session) {
 		ConceptPageCount conCount = new ConceptPageCount();
 		
 		//set count
 		String count = request.getParameter("count");
 		
 		if (session.getAttribute("countConcept") == null) {
 			session.setAttribute("countConcept", conCount);
 			conCount.setConceptsPerPage(Integer.parseInt(count));
 		} else {
 			conCount = (ConceptPageCount) session.getAttribute("countConcept");
 			int cCount = Integer.parseInt(count);
 			if (cCount == -1)
 				cCount = 10000;
 			conCount.setConceptsPerPage(cCount);
 			conCount.setCurrentPage(1);
 		}
 		model.addAttribute("countConcept", conCount);
 		
 		//add other elements (search words and results) to the view, so they are displayed
 		ConceptSearch cs = (ConceptSearch) session.getAttribute("conceptSearch");
 		if (cs != null) {
 			model.addAttribute("conceptSearch", cs);
 		}
 		Collection<Concept> conList = (Collection<Concept>) session.getAttribute("sortResults");
 		if (conList != null) {
 			model.addAttribute("searchResult", conList);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	@RequestMapping(value = "/module/conceptmanagement/advancedSearch", method = RequestMethod.GET, params = "page")
 	public void switchToPage(ModelMap model, WebRequest request, HttpSession session) {
 		//set page
 		String page = request.getParameter("page");
 		
 		ConceptPageCount conCount = (ConceptPageCount) session.getAttribute("countConcept");
 		if (conCount != null) {
 			conCount.setCurrentPage(Integer.parseInt(page));
 			model.addAttribute("countConcept", conCount);
 		}
 		
 		//add other elements (search words and results) to the view, so they are displayed
 		ConceptSearch cs = (ConceptSearch) session.getAttribute("conceptSearch");
 		if (cs != null) {
 			model.addAttribute("conceptSearch", cs);
 		}
 		Collection<Concept> conList = (Collection<Concept>) session.getAttribute("sortResults");
 		if (conList != null) {
 			model.addAttribute("searchResult", conList);
 		} else {
 			System.err.println("Results are gone");
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	@RequestMapping(value = "/module/conceptmanagement/advancedSearch", method = RequestMethod.GET, params = "sort")
 	public void sortResultsView(ModelMap model, WebRequest request, HttpSession session) {
 		String sortFor = request.getParameter("sort");
 		boolean asc = true;
 		
 		if (request.getParameter("order") != null && request.getParameter("order").equals("desc"))
 			asc = false;
 		
 		Collection<ConceptSearchResult> conList = (Collection<ConceptSearchResult>) session.getAttribute("sortResults");
 		ConceptSearch cs = (ConceptSearch) session.getAttribute("conceptSearch");
 		if (cs != null)
 			model.addAttribute("conceptSearch", cs);
 		
 		if (conList != null) {
 			Collections.sort((List<ConceptSearchResult>) conList, new ConceptComparator(sortFor, asc));
 			model.addAttribute("searchResult", conList);
 		}
 	}
 	
 	@RequestMapping(value = "/module/conceptmanagement/advancedSearch", method = RequestMethod.POST)
 	public void performAdvancedSearch(ModelMap model, WebRequest request, HttpSession session) {
 		ConceptSearchService searchService = (ConceptSearchService) Context.getService(ConceptSearchService.class);
 		Collection<Concept> rslt = new Vector<Concept>();
 		ConceptSearch cs = new ConceptSearch("");
 		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
 		Date dateFrom = null;
 		Date dateTo = null;
 		
 		//get all search parameters
 		String searchName = request.getParameter("conceptQuery");
 		String searchDescription = request.getParameter("conceptDescription");
 		String[] searchDatatypes = request.getParameterValues("conceptDatatype");
 		String[] searchClassesString = request.getParameterValues("conceptClasses");
 		String searchIsSet = request.getParameter("conceptIsSet");
 		
 		String searchDateFrom = request.getParameter("dateFrom");
 		String searchDateTo = request.getParameter("dateTo");
 		String[] searchUsedAs = request.getParameterValues("conceptUsedAs");
 		
 		try {
 			if (searchDateFrom != null && !searchDateFrom.isEmpty())
 				dateFrom = df.parse(searchDateFrom);
 			if (searchDateTo != null && !searchDateTo.isEmpty())
 				dateTo = df.parse(searchDateTo);
 		}
 		catch (ParseException ex) {
 			ex.printStackTrace();
 			dateFrom = null;
 			dateTo = null;
 		}
 		;
 		
 		//check for correct selections
 		if (searchDatatypes == null) {
 			searchDatatypes = null;
 			cs.setDataTypes(new Vector<ConceptDatatype>());
 		}
 		
 		if (searchClassesString == null) {
 			searchClassesString = null;
 			cs.setConceptClasses(new Vector<ConceptClass>());
 		}
 		
 		if (searchIsSet == null) {
 			searchIsSet = null;
 			cs.setIsSet(-1);
 		} else {
 			cs.setIsSet(Integer.parseInt(searchIsSet));
 		}
 		
 		if (searchDateFrom == null || searchDateFrom.isEmpty()) {
 			searchDateFrom = null;
 		} else {
 			cs.setDateFrom(dateFrom);
 		}
 		
 		if (searchDateTo == null || searchDateTo.isEmpty()) {
 			searchDateTo = null;
 		} else {
 			cs.setDateTo(dateTo);
 		}
 		
 		if (searchUsedAs == null) {
 			cs.setConceptUsedAs(null);
 		} else {
 			List<String> usedAsList = Arrays.asList(searchUsedAs);
 			cs.setConceptUsedAs(usedAsList);
 		}
 		
 		//maintain cs object: keep track of all entered information
 		cs.setSearchQuery(searchName);
 		
 		if (searchDescription != null) {
 			String[] searchTerms = searchDescription.split(" ");
 			List<String> searchTermsList = Arrays.asList(searchTerms);
 			cs.setSearchTerms(searchTermsList);
 		}
 		
 		if (searchDatatypes != null) {
 			List<String> searchDatatypesList = Arrays.asList(searchDatatypes);
 			List<ConceptDatatype> dataTypesList = new Vector<ConceptDatatype>();
 			
 			for (String s : searchDatatypesList) {
 				dataTypesList.add(searchService.getConceptDatatypeById(Integer.parseInt(s)));
 			}
 			cs.setDataTypes(dataTypesList);
 		}
 		
 		if (searchClassesString != null) {
 			List<String> searchClassesList = Arrays.asList(searchClassesString);
 			List<ConceptClass> classesList = new Vector<ConceptClass>();
 			
 			for (String s : searchClassesList) {
 				classesList.add(searchService.getConceptClassById(Integer.parseInt(s)));
 			}
 			cs.setConceptClasses(classesList);
 		}
 		
 		//perform search using ConceptSearchService
 		rslt = searchService.getConcepts(cs);
 		
 		//add the results to a DTO to avoid Hibernate's lazy loading
 		Collection<ConceptSearchResult> resList = new Vector<ConceptSearchResult>();
 		for (Concept c : rslt) {
 			if (cs.getConceptUsedAs() == null || searchService.isConceptUsedAs(c, cs)) {
 				ConceptSearchResult res = new ConceptSearchResult(c);
 				res.setNumberOfObs(searchService.getNumberOfObsForConcept(c.getConceptId()));
 				resList.add(res);
 			}
 		}
 		
 		//add results to view
 		model.addAttribute("conceptSearch", cs);
 		model.addAttribute("searchResult", resList);
 		
 		//add search results to session to make them available for other methods
 		session.setAttribute("conceptSearch", cs);
 		session.setAttribute("searchResult", resList);
 		session.setAttribute("sortResults", resList);
 		
 		//reset currentPage when performing a new search
 		ConceptPageCount conCount = (ConceptPageCount) session.getAttribute("countConcept");
 		if (conCount != null) {
 			conCount.setCurrentPage(1);
 		}
 	}
 	
 	@RequestMapping(value = "/module/conceptmanagement/autocomplete", method = RequestMethod.GET)
 	public void doAutocomplete(ModelMap model, WebRequest request, HttpSession session) {
 		//ConceptSearchService searchService = (ConceptSearchService) Context.getService(ConceptSearchService.class);
 		//String searchFor = request.getParameter("q");
 		//List<String> autoResults = searchService.getAutocompleteConcepts(searchFor);
 		//model.addAttribute("autoComplete", autoResults);
 		
 		// -- Autocompletehelper is used to avoid some problems -- 
		System.out.println("Accessing autocomplete");
 		
 	}
 	
 }
