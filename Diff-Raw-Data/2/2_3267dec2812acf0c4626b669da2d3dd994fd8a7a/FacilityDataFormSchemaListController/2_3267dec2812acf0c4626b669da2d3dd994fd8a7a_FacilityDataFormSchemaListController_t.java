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
 
 import org.openmrs.module.facilitydata.service.FacilityDataService;
 import org.openmrs.module.facilitydata.util.FacilityDataUtil;
 import org.openmrs.web.WebConstants;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.servlet.http.HttpServletRequest;
 
 @Controller
 @RequestMapping("/module/facilitydata/schema.list")
 public class FacilityDataFormSchemaListController {
 
     @RequestMapping(method = RequestMethod.GET)
     public String homepage(ModelMap map) {
         FacilityDataService svc = FacilityDataUtil.getService();
         map.addAttribute("schemas",svc.getAllFacilityDataFormSchemas());
         return "/module/facilitydata/schemaList";
     }
 
     @RequestMapping(method = RequestMethod.POST)
     public String deleteQuestion(@RequestParam Integer id, ModelMap map, HttpServletRequest request) {
         FacilityDataService svc = FacilityDataUtil.getService();
         svc.deleteFacilityDataFormSchema(svc.getFacilityDataFormSchema(id));
         request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "facilitydata.deleted-form-schema");
        map.addAttribute("schemas", svc.getAllFacilityDataFormSchemas());
         return "/module/facilitydata/schemaList";
     }
 }
