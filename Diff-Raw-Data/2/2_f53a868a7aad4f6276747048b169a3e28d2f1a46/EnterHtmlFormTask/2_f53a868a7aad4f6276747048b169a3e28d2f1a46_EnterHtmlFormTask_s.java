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
 
 package org.openmrs.module.emr.htmlform;
 
 import org.openmrs.Form;
 import org.openmrs.api.FormService;
 import org.openmrs.messagesource.MessageSourceService;
 import org.openmrs.module.emr.EmrContext;
 import org.openmrs.module.emr.task.BasePatientSpecificTaskDescriptor;
 import org.openmrs.module.htmlformentry.HtmlForm;
 import org.openmrs.module.htmlformentry.HtmlFormEntryService;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.resource.ResourceFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * A task (that requires a patient to be in the context to be enabled) for entering an HTML Form.
  *
  * You may point to an existing HTML Form (by setting the formUuid property) or else have one be dynamically created
  * on the fly whenever this is task is selected (by setting formDefinitionFromUiResource)
  */
 public class EnterHtmlFormTask extends BasePatientSpecificTaskDescriptor {
 
     @Autowired
     @Qualifier("messageSourceService")
     private MessageSourceService messageSourceService;
 
     @Autowired
     @Qualifier("htmlFormEntryService")
     private HtmlFormEntryService htmlFormEntryService;
 
     @Autowired
     @Qualifier("formService")
     private FormService formService;
 
     @Autowired
     @Qualifier("coreResourceFactory")
     private ResourceFactory resourceFactory;
 
     @Autowired
     @Qualifier("uiUtils")
     private UiUtils uiUtils;
 
     private String id;
 
     private String labelCode;
 
     private String iconUrl;
 
     private String tinyIconUrl;
 
     private String formUuid;
 
     private String formDefinitionFromUiResource;
 
     private Integer htmlFormId;
 
     private double priority = 0d;
 
     private EntryTiming timing = EntryTiming.REAL_TIME;
 
     public void setId(String id) {
         this.id = id;
     }
 
     public void setMessageSourceService(MessageSourceService messageSourceService) {
         this.messageSourceService = messageSourceService;
     }
 
     public void setHtmlFormEntryService(HtmlFormEntryService htmlFormEntryService) {
         this.htmlFormEntryService = htmlFormEntryService;
     }
 
     public void setFormService(FormService formService) {
         this.formService = formService;
     }
 
     public void setResourceFactory(ResourceFactory resourceFactory) {
         this.resourceFactory = resourceFactory;
     }
 
     public void setLabelCode(String labelCode) {
         this.labelCode = labelCode;
     }
 
     public void setIconUrl(String iconUrl) {
         this.iconUrl = iconUrl;
     }
 
     public void setTinyIconUrl(String tinyIconUrl) {
         this.tinyIconUrl = tinyIconUrl;
     }
 
     public void setFormUuid(String formUuid) {
         this.formUuid = formUuid;
     }
 
     public void setPriority(double priority) {
         this.priority = priority;
     }
 
     public void setTiming(EntryTiming timing) {
         this.timing = timing;
     }
 
     @Override
     public String getId() {
         return id;
     }
 
     @Override
     public String getLabel(EmrContext context) {
         if (labelCode != null) {
             return messageSourceService.getMessage(labelCode);
         } else {
             HtmlForm htmlForm = getHtmlForm();
             String[] messageArgs = { htmlForm.getName() };
             return messageSourceService.getMessage("emr.task.enterHtmlForm.label.default", messageArgs, context.getUserContext().getLocale());
         }
     }
 
     /**
      * Usually (unless formDefinitionFromUiResource is set and we are in UI development mode for that module) this will
      * cache the HTML Form's PK id, so subsequent loads are a bit faster.
      * @return
      */
     public HtmlForm getHtmlForm() {
         HtmlForm htmlForm = null;
         if (htmlFormId != null) {
             htmlForm = htmlFormEntryService.getHtmlForm(htmlFormId);
         }
         else if (formUuid != null) {
             Form form = formService.getFormByUuid(formUuid);
             htmlForm = htmlFormEntryService.getHtmlFormByForm(form);
             htmlFormId = htmlForm.getId();
         }
         else if (formDefinitionFromUiResource != null) {
             try {
                 htmlForm = HtmlFormUtil.getHtmlFormFromUiResource(resourceFactory, formService, htmlFormEntryService, formDefinitionFromUiResource);
             } catch (IOException e) {
                 throw new IllegalStateException("Error getting form from UI Resource: " + formDefinitionFromUiResource, e);
             }
             if (!resourceFactory.isResourceProviderInDevelopmentMode(formDefinitionFromUiResource.substring(0, formDefinitionFromUiResource.indexOf(':')))) {
                 htmlFormId = htmlForm.getId();
             }
         }
 
         if (htmlForm == null) {
             throw new IllegalStateException("Form isn't specified, or cannot find the specified one in the database");
         }
         return htmlForm;
     }
 
     @Override
     public String getIconUrl(EmrContext context) {
         return iconUrl;
     }
 
     @Override
     public String getTinyIconUrl(EmrContext context) {
         return tinyIconUrl;
     }
 
     @Override
     public String getUrl(EmrContext context) {
         HtmlForm htmlForm = getHtmlForm();
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("patientId", context.getCurrentPatient().getId());
         params.put("timing", timing);
         params.put("htmlFormId", htmlForm.getId());
        return uiUtils.pageLink("emr", "htmlform/enterHtmlForm", params);
     }
 
     @Override
     public boolean isAvailable(EmrContext context) {
         if (timing == EntryTiming.REAL_TIME && context.getActiveVisitSummary() == null) {
             return false;
         }
         return super.isAvailable(context);
     }
 
     /**
      * @param formDefinitionFromUiResource something like "emr:htmlforms/vitals.xml"
      */
     public void setFormDefinitionFromUiResource(String formDefinitionFromUiResource) {
         this.formDefinitionFromUiResource = formDefinitionFromUiResource;
     }
 
     public String getFormDefinitionFromUiResource() {
         return formDefinitionFromUiResource;
     }
 }
