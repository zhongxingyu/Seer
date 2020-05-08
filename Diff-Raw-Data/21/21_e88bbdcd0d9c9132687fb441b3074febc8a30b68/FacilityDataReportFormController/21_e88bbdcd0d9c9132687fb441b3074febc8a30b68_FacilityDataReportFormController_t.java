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
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Maps;
 import org.apache.log4j.Logger;
 import org.openmrs.Location;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.facilitydata.model.FacilityDataFormQuestion;
 import org.openmrs.module.facilitydata.model.FacilityDataFormSchema;
 import org.openmrs.module.facilitydata.model.FacilityDataFormSection;
 import org.openmrs.module.facilitydata.model.FacilityDataQuestion;
 import org.openmrs.module.facilitydata.model.FacilityDataReportFormData;
 import org.openmrs.module.facilitydata.model.FacilityDataValue;
 import org.openmrs.module.facilitydata.util.FacilityDataUtil;
 import org.openmrs.module.facilitydata.util.FacilityDataDateUtils;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.Map;
 
 @Controller
 @RequestMapping("/module/facilitydata/report.form")
 public class FacilityDataReportFormController {
     private static final Logger log = Logger.getLogger(FacilityDataReportFormController.class);
 
     @InitBinder
     public void initBinder(WebDataBinder binder) {
         DateFormat dateFormat = FacilityDataDateUtils.getDateFormat();
         dateFormat.setLenient(false);
         binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
     }
     // a map with the UUID for the corresponding as the key and the FacilityDataValue instance as the value.
     public Map<String,FacilityDataValue> getValues(FacilityDataFormSchema schema, FacilityDataReportFormData formData) {
         Map<String, FacilityDataValue> values = Maps.newHashMap();
         for (FacilityDataValue value : formData.getValues()) {
             for (FacilityDataFormSection section : schema.getFormSections())
                 for (FacilityDataFormQuestion question : section.getQuestions()) {
                     if (question.getQuestion().getUuid().equals(value.getQuestion().getUuid()))
                         values.put(question.getQuestion().getUuid(), value);
                 }
         }
         return values;
     }
     @RequestMapping(method = RequestMethod.GET)
     public String homepage(ModelMap map, @RequestParam(required=true)Integer id,HttpServletRequest request) throws ParseException {
         if(Context.isAuthenticated()) {
             FacilityDataFormSchema schema = FacilityDataUtil.getService().getFacilityDataFormSchema(id);
             map.addAttribute("schema", schema);
             Date startDate = FacilityDataDateUtils.getDateFormat().parse(request.getParameter("startDate"));
             Date endDate = FacilityDataDateUtils.getDateFormat().parse(request.getParameter("endDate"));
             Location location = Context.getLocationService().getLocation(Integer.parseInt(request.getParameter("site")));
             FacilityDataReportFormData formData = FacilityDataUtil.getService().getFacilityDataReportFormData(schema,startDate,endDate, location);
             map.addAttribute("values",getValues(schema,formData));
         }
         return "/module/facilitydata/reportForm";
     }
     @RequestMapping(method = RequestMethod.POST)
     public String saveReport(ModelMap map, @RequestParam("startDate") Date startDate,
                              @RequestParam("endDate") Date endDate,
                              HttpServletRequest request, HttpServletResponse response) throws ParseException {
         map.addAttribute("schema", FacilityDataUtil.createMockSchema());
         Location site = Context.getLocationService().getLocation(Integer.parseInt(request.getParameter("site")));
         FacilityDataFormSchema schema = (FacilityDataFormSchema)map.get("schema");
         Map<String,String[]> parameterMap = request.getParameterMap();
         // make a copy
         Map<String,String[]> paramCopy = Maps.newHashMap(parameterMap);
         // filter out the questions -- we only need them, we have site, frequency, and the validity period.
         Map<String, String[]> questions = Maps.filterKeys(paramCopy, new Predicate<String>() {
             public boolean apply(String s) {
                 // question.<UUID HERE>
                 // for comments: uuid_comments; so all that is needed is the UUID; excluding everything else.
                 return s.startsWith("question.");
             }
         });
         FacilityDataReportFormData formData = FacilityDataUtil.getService()
                     .getFacilityDataReportFormData(schema,startDate,endDate,site);
         //TODO: look to see if this can be cleaned up more.
         //TODO: validate! 
         for(Map.Entry<String, String[]> entry : questions.entrySet()) {
             String uuid = entry.getKey().substring("question.".length()); // will always pass -- no need for extra checks. 
             FacilityDataQuestion question = FacilityDataUtil.getService().getQuestionByUUID(uuid);
             String answer = questions.containsKey("question."+uuid) ? questions.get("question."+uuid)[0] : null;
             String comment = request.getParameter(uuid+"_comment");
             String days = request.getParameter(uuid + "_days");
             String reason = request.getParameter(uuid + "_reason");            
             // only thing we need to check before it hits the method to save the report is stock
             // questions; and set the comment field approriately.
             // comment field is *ALWAYS* in the form: 'days:reason' even if one or both components is missing,
             // the order NEVER changes; this provides for consistency in parsing. 
             if(FacilityDataUtil.isStockQuestion(question)) {
                 // days but no reason -- then days but no reason -- both given -- finally neither.
                 if((days != null && !"".equals(days)) && reason != null && "".equals(reason)) {
                     comment = days+":";
                 } else if((days != null && "".equals(days)) && reason != null && !"".equals(reason)) {
                     comment = ":"+reason;
                 } else if(days != null && !"".equals(days) && reason != null && !"".equals(reason)) {
                     comment = days+":"+reason;
                 } else {
                     comment = ":";
                 }
             }
             FacilityDataValue value = null;
             for(FacilityDataValue dataValue : formData.getValues()) {
                 if(dataValue.getQuestion().getUuid().equals(question.getUuid())) {
                     value = dataValue;
                     break; // look no further
                 }
            }         
             map.addAttribute("values", getValues(schema, formData));
                FacilityDataUtil.getService().processReportAnswers(question,value,answer,comment,site,
                        startDate, endDate);
         }
         return String.format("redirect:report.form?id=%d&startDate=%s&endDate=%s&site=%s",
                 schema.getId(), FacilityDataDateUtils.getDateFormat().format(startDate),
                 FacilityDataDateUtils.getDateFormat().format(endDate),request.getParameter("site"));
     }
 }
