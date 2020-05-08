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
 package org.openmrs.module.hiepatientsearch.web.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.PersonAddress;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.addresshierarchyrwanda.AddressHierarchy;
 import org.openmrs.module.addresshierarchyrwanda.AddressHierarchyService;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  * The main controller.
  */
 @Controller
 public class HIEPatientSearchManageController {
 
 	protected final Log log = LogFactory.getLog(getClass());
 
 	@RequestMapping(value = "/findPatient.htm", method = RequestMethod.GET)
 	public void manage(HttpServletRequest request, HttpServletResponse response)
 			throws IOException {
 		System.out.println("Test ! triggered by the patient search action");
 
 		response.sendRedirect(request.getContextPath()
 				+ "/module/hiepatientsearch/manage.form");
 	}
 
 	@RequestMapping(value = "/module/hiepatientsearch/manage", method = RequestMethod.GET)
 	public String manager(ModelMap model, HttpServletRequest request,
 			HttpServletResponse response) throws IOException {
 		System.out.println("Test ! triggered by the patient search action");
 		model.addAttribute("user", Context.getAuthenticatedUser());
 		
 		model = populateModel(model);
 		return "/module/hiepatientsearch/manage";
 	}
 			
 	private ModelMap populateModel(ModelMap model) {
 		AddressHierarchyService service = (AddressHierarchyService) Context
 				.getService(AddressHierarchyService.class);
 
 		
 		List<AddressHierarchy> stateProvince = new ArrayList<AddressHierarchy>();
 		List<String> stateProvinceString = new ArrayList<String>();
 		stateProvinceString.add("select province");
 		List<AddressHierarchy> countryDistrict = new ArrayList<AddressHierarchy>();
 		List<String> countryDistrictString = new ArrayList<String>();
 		countryDistrictString.add("select district");
 		List<AddressHierarchy> sector = new ArrayList<AddressHierarchy>();
 		List<String> sectorString = new ArrayList<String>();
 		sectorString.add("select sector");
 		List<AddressHierarchy> cell = new ArrayList<AddressHierarchy>();
 		List<String> cellString = new ArrayList<String>();
 		cellString.add("select cell");
 		List<AddressHierarchy> village = new ArrayList<AddressHierarchy>();
 		List<String> villageString = new ArrayList<String>();
 		villageString.add("select village");
 		
 		stateProvince = service.searchHierarchy("%", 2);
 		countryDistrict = service.searchHierarchy("%", 3);
 		sector = service.searchHierarchy("%", 4);
 		cell = service.searchHierarchy("%", 5);
 		village = service.searchHierarchy("%", 6);
 
 		for(AddressHierarchy ah : stateProvince) {
 			stateProvinceString.add(ah.getLocationName());
 		}
 		
 		for(AddressHierarchy ah : countryDistrict) {
 			countryDistrictString.add(ah.getLocationName());
 		}
 		
 		for(AddressHierarchy ah : sector) {
 			sectorString.add(ah.getLocationName());
 		}
 		
 		for(AddressHierarchy ah : cell) {
 			cellString.add(ah.getLocationName());
 		}
 		
 		for(AddressHierarchy ah : village) {
 			villageString.add(ah.getLocationName());
 		}
 		
 		
 		model.addAttribute("stateProvince", stateProvinceString);
 		model.addAttribute("countryDistrict", countryDistrictString);
 		model.addAttribute("sector", sectorString);
 		model.addAttribute("cell", cellString);
 		model.addAttribute("village", villageString);
 		
 		return model;
 	}
 
 	@RequestMapping(value = "/module/hiepatientsearch/manage", method = RequestMethod.POST)
 	public String advancedSearch(@RequestParam(value="patientIds") String patientIds,
 			@RequestParam("stateProvince") String stateProvince,
 			@RequestParam("countryDistrict") String countryDistrict,
 			@RequestParam("sector") String sector,
 			@RequestParam("cell") String cell,
 			@RequestParam("village") String village,
 			@RequestParam("moreThanAge") String moreThanAge,
 			@RequestParam("lessThanAge") String lessThanAge,
 			@RequestParam("date") Date dateString,
 			@RequestParam(value="gender") String gender, ModelMap model) {
 		String message = "Patient Id's" + patientIds + gender;
 		System.out.println(stateProvince);
 		System.out.println(countryDistrict);
 
 		String[] patientIdList = patientIds.split(",");
 
 		System.out.println(message);
 
 		List<Patient> patients = new ArrayList<Patient>();
 		List<Patient> patientLists = new ArrayList<Patient>();
 
 
 		for (int i = 0; i < patientIdList.length; i++) {
 			patients = Context.getPatientService().getPatients(null,
 					patientIdList[i], null, false);
 			System.out.println("size is " + patients);
 			System.out.println("gender " + patients.get(0).getGender());
 			patientLists.add(patients.get(0));
 		}
 
 		System.out.println("Total size " + patientLists.size());
 
 		if (gender != null) {
 			patientLists = genderFilter(patientLists, gender);
 		}
 
 		if (moreThanAge != "" || lessThanAge != "") {
 			patientLists = ageFilter(patientLists, moreThanAge, lessThanAge);
 		}
 
 		if (dateString != null) {
 			Date date = null;
 			// patientLists = dobFilter(patientLists, date);
 		}
 		
 		if (stateProvince != null && stateProvince != ""){
 			patientLists = stateProvinceFilter(patientLists, stateProvince);
 		}
 		
 		if (countryDistrict != null && countryDistrict != ""){
 			patientLists = countryDistrictFilter(patientLists, countryDistrict);
 		}
 		
 		if (sector != null && sector != ""){
 			patientLists = sectorFilter(patientLists, sector);
 		}
 		
 		if (cell != null && cell != ""){
 			patientLists = cellFilter(patientLists, cell);
 		}
 		
 		if (village != null && village != ""){
 			patientLists = villageFilter(patientLists, village);
 		}
 
 		System.out.println("tesy final size = " + patientLists.size());
 		patientLists.add(Context.getPatientService().getPatient(2));
 		model.addAttribute("patientLists", patientLists);
 		
 		model = populateModel(model);
 		
 		return "/module/hiepatientsearch/manage";
 	}
 
 	private List<Patient> villageFilter(List<Patient> patientLists,
 			String village) {
 		Iterator<Patient> iterator = patientLists.iterator();
 		while (iterator.hasNext()) {
 			Patient patient = iterator.next();
 			PersonAddress pa = patient.getPersonAddress();
 			if (!pa.getAddress1().equals(village)){
 				iterator.remove();
 			}
 		}
 	return patientLists;
 	}
 
 	private List<Patient> cellFilter(List<Patient> patientLists, String cell) {
 		Iterator<Patient> iterator = patientLists.iterator();
 		while (iterator.hasNext()) {
 			Patient patient = iterator.next();
 			PersonAddress pa = patient.getPersonAddress();
 			if (!pa.getAddress3().equals(cell)){
 				iterator.remove();
 			}
 		}
 	return patientLists;
 	}
 
 	private List<Patient> sectorFilter(List<Patient> patientLists, String sector) {
 		Iterator<Patient> iterator = patientLists.iterator();
 		while (iterator.hasNext()) {
 			Patient patient = iterator.next();
 			PersonAddress pa = patient.getPersonAddress();
 			if (!pa.getCityVillage().equals(sector)){
 				iterator.remove();
 			}
 		}
 	return patientLists;
 	}
 
 	private List<Patient> countryDistrictFilter(List<Patient> patientLists,
 			String countryDistrict) {
 		Iterator<Patient> iterator = patientLists.iterator();
 		while (iterator.hasNext()) {
 			Patient patient = iterator.next();
 			PersonAddress pa = patient.getPersonAddress();
 			if (!pa.getCountyDistrict().equals(countryDistrict)){
 				iterator.remove();
 			}
 		}
 	return patientLists;
 	}
 
 	private List<Patient> stateProvinceFilter(List<Patient> patientLists,
 			String stateProvince) {
 			Iterator<Patient> iterator = patientLists.iterator();
 			while (iterator.hasNext()) {
 				Patient patient = iterator.next();
 				PersonAddress pa = patient.getPersonAddress();
 				if (!pa.getStateProvince().equals(stateProvince)){
 					iterator.remove();
 				}
 			}
 		return patientLists;
 	}
 
 	private List<Patient> ageFilter(List<Patient> patientLists,
 			String moreThanAge, String lessThanAge) {
 		if (moreThanAge != null && lessThanAge == null) {
 			Iterator<Patient> iterator = patientLists.iterator();
 			while (iterator.hasNext()) {
 				if (iterator.next().getAge() > Integer.parseInt(moreThanAge)) {
 
 				} else {
					System.out.println("equals");
 					iterator.remove();
					System.out.println("removed !");
 				}
 			}
 
 		} else if (moreThanAge == null && lessThanAge != null) {
 			Iterator<Patient> iterator = patientLists.iterator();
 			while (iterator.hasNext()) {
 				if (iterator.next().getAge() < Integer.parseInt(lessThanAge)) {
 
 				} else {
					System.out.println("equals");
 					iterator.remove();
					System.out.println("removed !");
 				}
 			}
 
 		} else if (moreThanAge != null && lessThanAge != null) {
 			Iterator<Patient> iterator = patientLists.iterator();
 			while (iterator.hasNext()) {
 				int age = iterator.next().getAge();
 				if (age > Integer.parseInt(moreThanAge)
 						&& age < Integer.parseInt(lessThanAge)) {
 
 				} else {
					System.out.println("equals");
 					iterator.remove();
					System.out.println("removed !");
 				}
 			}
 
 		}
 		return patientLists;
 	}
 
 	
 	   @RequestMapping(value = "/redirect", method = RequestMethod.GET)
 	   public String redirect() {
 	     
 	      return "manage";
 	   }
 	   
 	   @RequestMapping(value = "/manage", method = RequestMethod.GET)
 	   public String finalPage() {
 	     
 	      return "manage";
 	   }
 	   
 	private void dobFilter(List<Patient> patientLists, Date date) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private List<Patient> genderFilter(List<Patient> patientLists, String gender) {
 		Iterator<Patient> iterator = patientLists.iterator();
 		while (iterator.hasNext()) {
 			if (iterator.next().getGender().equals(gender)) {
				System.out.println("equals");
 				iterator.remove();
				System.out.println("removed !");
 			}
 		}
		System.out.println("D" + patientLists.size());
 		return patientLists;
 	}
 
 }
