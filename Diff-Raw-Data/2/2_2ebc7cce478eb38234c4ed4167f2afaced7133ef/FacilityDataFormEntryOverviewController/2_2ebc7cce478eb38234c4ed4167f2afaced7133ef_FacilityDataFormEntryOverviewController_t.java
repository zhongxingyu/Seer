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
 package org.openmrs.module.facilitydata.web.controller;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.facilitydata.model.FacilityDataForm;
 import org.openmrs.module.facilitydata.model.FacilityDataFormSchema;
 import org.openmrs.module.facilitydata.model.enums.Frequency;
 import org.openmrs.module.facilitydata.propertyeditor.FacilityDataFormEditor;
 import org.openmrs.module.facilitydata.service.FacilityDataService;
 import org.openmrs.module.facilitydata.util.FacilityDataConstants;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller
 public class FacilityDataFormEntryOverviewController {
 	
     @InitBinder
     public void initBinder(WebDataBinder binder) {
         binder.registerCustomEditor(FacilityDataForm.class, new FacilityDataFormEditor());
         binder.registerCustomEditor(Date.class, new CustomDateEditor(Context.getDateFormat(), true));
     }
 
     @RequestMapping("/module/facilitydata/formEntryOverview.form")
     public void formEntryOverview(ModelMap map, 
     								@RequestParam(required = true) FacilityDataForm form,
     								@RequestParam(required = false) Integer yearIncrement,
     								@RequestParam(required = false) Integer monthIncrement) throws Exception {
     	
     	FacilityDataService service = Context.getService(FacilityDataService.class);
 
     	Calendar cal = Calendar.getInstance();
     	cal.add(Calendar.DATE, 1);
     	
     	if (yearIncrement != null) {
     		cal.add(Calendar.YEAR, yearIncrement);
     	}
     	if (monthIncrement != null) {
     		cal.add(Calendar.DATE, monthIncrement*21);
     	}
 
     	Date endDate = cal.getTime();
     	if (form.getFrequency() == Frequency.MONTHLY) {  // For monthly reports, display last year
     		cal.set(Calendar.DATE, 1);
     		cal.add(Calendar.YEAR, -1);
     	}
     	else if (form.getFrequency() == Frequency.DAILY) {  // For daily reports, display last 3 weeks
     		cal.add(Calendar.DATE, -21);
     	}
     	else {
     		throw new RuntimeException("Unable to handle a report with frequency: " + form.getFrequency());
     	}
     	Date startDate = cal.getTime();
     	
     	Map<Integer, Map<String, Integer>> questionsAnswered = service.getNumberOfQuestionsAnswered(form, startDate, endDate);
     	
     	DateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd");
     	DateFormat monthFormat = new SimpleDateFormat("MMM");
     	List<Integer> daysOfWeekSupported = FacilityDataConstants.getDailyReportDaysOfWeek();
     	
     	Map<Integer, Integer> yearCols = new LinkedHashMap<Integer, Integer>(); // Year -> Number of columns
     	Map<String, Integer> monthCols = new LinkedHashMap<String, Integer>();  // Month -> Number of columns
     	Map<String, Date> dayCols = new LinkedHashMap<String, Date>();
     	Map<Integer, Map<String, Integer>> dayData = new HashMap<Integer, Map<String, Integer>>();    // LocationId -> Day -> Number of questions
     	Map<Object, String> displayKeys = new HashMap<Object, String>();  // Map key -> Display format
     	Set<String> datesSupported = new HashSet<String>(); // Dates support entry
     	
     	while (cal.getTime().before(endDate)) {
     		
     		String dateStr = ymdFormat.format(cal.getTime());
     		Integer year = cal.get(Calendar.YEAR);
     		String month = monthFormat.format(cal.getTime());
     		Integer day = cal.get(Calendar.DAY_OF_MONTH);
     		
     		yearCols.put(year, yearCols.get(year) == null ? 1 : yearCols.get(year) + 1);
     		monthCols.put(year+month, monthCols.get(year+month) == null ? 1 : monthCols.get(year+month) + 1);
     		dayCols.put(dateStr, cal.getTime());
     		
    		if (form.getFrequency() == Frequency.MONTHLY || daysOfWeekSupported.contains(cal.get(Calendar.DAY_OF_WEEK))) {
     			datesSupported.add(dateStr);
     		}
     		
     		for (Integer locationId : questionsAnswered.keySet()) {
     			Map<String, Integer> questionsAnsweredAtLocation = questionsAnswered.get(locationId);
     			Integer numAnswered = questionsAnsweredAtLocation == null ? null : questionsAnsweredAtLocation.get(dateStr);
     			Map<String, Integer> locationData = dayData.get(locationId);
     			if (locationData == null) {
     				locationData = new HashMap<String, Integer>();
     				dayData.put(locationId, locationData);
     			}
     			locationData.put(dateStr, numAnswered == null ? 0 : numAnswered);
     		}
     		
     		displayKeys.put(year, year.toString());
     		displayKeys.put(year+month, month);
     		displayKeys.put(dateStr, day.toString());
     		
     		cal.add(form.getFrequency().getCalendarField(), form.getFrequency().getCalendarIncrement());
     	}
     	
     	Map<FacilityDataFormSchema, Integer> numQuestionsBySchema = new HashMap<FacilityDataFormSchema, Integer>();
     	for (FacilityDataFormSchema schema : form.getSchemas()) {
     		numQuestionsBySchema.put(schema, schema.getTotalNumberOfQuestions());
     	}
     	
     	map.addAttribute("today", new Date());
     	map.addAttribute("form", form);
     	map.addAttribute("yearIncrement", yearIncrement);
     	map.addAttribute("monthIncrement", monthIncrement);
     	map.addAttribute("yearCols", yearCols);
     	map.addAttribute("monthCols", monthCols);
     	map.addAttribute("dayCols", dayCols);
     	map.addAttribute("dayData", dayData);
     	map.addAttribute("displayKeys", displayKeys);
     	map.addAttribute("numQuestionsBySchema", numQuestionsBySchema);
     	map.addAttribute("questionsAnswered", questionsAnswered);
     	map.addAttribute("locations", FacilityDataConstants.getSupportedFacilities());
     	map.addAttribute("datesSupported", datesSupported);
     }
 }
