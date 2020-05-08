 /*
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
 
 package org.openmrs.module.htmlformentryui.page.controller.htmlform;
 
 import org.apache.commons.lang.StringUtils;
 import org.openmrs.Form;
 import org.openmrs.Patient;
 import org.openmrs.Visit;
 import org.openmrs.api.FormService;
 import org.openmrs.module.appui.UiSessionContext;
 import org.openmrs.module.htmlformentry.HtmlForm;
 import org.openmrs.module.htmlformentry.HtmlFormEntryService;
 import org.openmrs.module.htmlformentryui.HtmlFormUtil;
 import org.openmrs.ui.framework.SimpleObject;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.ui.framework.page.PageModel;
 import org.openmrs.ui.framework.resource.ResourceFactory;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  *
  */
 public abstract class BaseEnterHtmlFormPageController {
 
     public void get(UiSessionContext sessionContext,
                     @RequestParam("patientId") Patient currentPatient,
                     @RequestParam(value = "formUuid", required = false) String formUuid,
                     @RequestParam(value = "htmlFormId", required = false) HtmlForm htmlForm,
                     @RequestParam(value = "definitionUiResource", required = false) String definitionUiResource,
                     @RequestParam(value = "visitId", required = false) Visit visit,
                     @RequestParam(value = "createVisit", required = false) Boolean createVisit,
                     @RequestParam(value = "returnUrl", required = false) String returnUrl,
                     @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride,
                     @SpringBean("htmlFormEntryService") HtmlFormEntryService htmlFormEntryService,
                     @SpringBean("formService") FormService formService,
                     @SpringBean("coreResourceFactory") ResourceFactory resourceFactory,
                     UiUtils ui,
                     PageModel model) throws Exception {
 
         sessionContext.requireAuthentication();
 
         if (htmlForm == null && StringUtils.isNotEmpty(definitionUiResource)) {
             htmlForm = HtmlFormUtil.getHtmlFormFromUiResource(resourceFactory, formService, htmlFormEntryService, definitionUiResource);
         }
         if (htmlForm == null && formUuid != null) {
             Form form = formService.getFormByUuid(formUuid);
             if (form != null) {
                 htmlForm = htmlFormEntryService.getHtmlFormByForm(form);
             }
         }
 
         if (htmlForm == null) {
             throw new IllegalArgumentException("Couldn't find a form");
         }
 
         if (StringUtils.isEmpty(returnUrl)) {
             if (currentPatient != null) {
                returnUrl = ui.pageLink("coreapps", "patientdashboard/patientDashboard", SimpleObject.create("patientId", currentPatient.getId()));
            } else {
                 returnUrl = "/" + ui.contextPath() + "index.html";
             }
         }
 
         model.addAttribute("htmlForm", htmlForm);
         model.addAttribute("patient", currentPatient);
         model.addAttribute("visit", visit);
         model.addAttribute("createVisit", createVisit);
         model.addAttribute("returnUrl", returnUrl);
         model.addAttribute("breadcrumbOverride", breadcrumbOverride);
     }
 
 }
