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
 
 package org.openmrs.module.emr.fragment.controller.htmlform;
 
 import org.openmrs.Encounter;
 import org.openmrs.Form;
 import org.openmrs.Patient;
 import org.openmrs.Visit;
 import org.openmrs.api.EncounterService;
 import org.openmrs.api.FormService;
 import org.openmrs.api.context.Context;
 import org.openmrs.api.context.ContextAuthenticationException;
 import org.openmrs.module.emr.EmrConstants;
 import org.openmrs.module.emr.EmrContext;
 import org.openmrs.module.emr.htmlform.HtmlFormUtil;
 import org.openmrs.module.emrapi.adt.AdtService;
 import org.openmrs.module.htmlformentry.FormEntryContext;
 import org.openmrs.module.htmlformentry.FormEntrySession;
 import org.openmrs.module.htmlformentry.FormSubmissionError;
 import org.openmrs.module.htmlformentry.HtmlForm;
 import org.openmrs.module.htmlformentry.HtmlFormEntryService;
 import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
 import org.openmrs.ui.framework.SimpleObject;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.annotation.FragmentParam;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.ui.framework.fragment.FragmentConfiguration;
 import org.openmrs.ui.framework.fragment.FragmentModel;
 import org.openmrs.ui.framework.resource.ResourceFactory;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  */
 public class EnterHtmlFormFragmentController {
 
     /**
      * @param config
      * @param emrContext
      * @param htmlFormEntryService
      * @param formService
      * @param resourceFactory
      * @param patient
      * @param hf
      * @param form
      * @param formUuid
      * @param definitionUiResource
      * @param encounter
      * @param visit
      * @param returnUrl
      * @param automaticValidation defaults to true. If you don't want HFE's automatic validation, set it to false
      * @param model
      * @param httpSession
      * @throws Exception
      */
     public void controller(FragmentConfiguration config,
                            EmrContext emrContext,
                            @SpringBean("htmlFormEntryService") HtmlFormEntryService htmlFormEntryService,
                            @SpringBean("formService") FormService formService,
                            @SpringBean("coreResourceFactory") ResourceFactory resourceFactory,
                            @FragmentParam("patient") Patient patient,
                            @FragmentParam(value = "htmlForm", required = false) HtmlForm hf,
                            @FragmentParam(value = "htmlFormId", required = false) Integer htmlFormId,
                            @FragmentParam(value = "formId", required = false) Form form,
                            @FragmentParam(value = "formUuid", required = false) String formUuid,
                            @FragmentParam(value = "definitionUiResource", required = false) String definitionUiResource,
                            @FragmentParam(value = "encounter", required = false) Encounter encounter,
                            @FragmentParam(value = "visit", required = false) Visit visit,
                            @FragmentParam(value = "createVisit", required = false) Boolean createVisit,
                            @FragmentParam(value = "returnUrl", required = false) String returnUrl,
                            @FragmentParam(value = "automaticValidation", defaultValue = "true") boolean automaticValidation,
                            FragmentModel model,
                            HttpSession httpSession) throws Exception {
 
         config.require("patient", "htmlForm | htmlFormId | formId | formUuid | definitionResource | encounter");
 
         if (hf == null) {
             if (htmlFormId != null) {
                 hf = htmlFormEntryService.getHtmlForm(htmlFormId);
             } else if (form != null) {
                 hf = htmlFormEntryService.getHtmlFormByForm(form);
             } else if (formUuid != null) {
                 form = formService.getFormByUuid(formUuid);
                 hf = htmlFormEntryService.getHtmlFormByForm(form);
             } else if (definitionUiResource != null) {
                 hf = HtmlFormUtil.getHtmlFormFromUiResource(resourceFactory, formService, htmlFormEntryService, definitionUiResource);
             }
         }
         if (hf == null && encounter != null) {
             form = encounter.getForm();
             if (form == null) {
                 throw new IllegalArgumentException("Cannot view a form-less encounter unless you specify which form to use");
             }
             hf = HtmlFormEntryUtil.getService().getHtmlFormByForm(encounter.getForm());
             if (hf == null)
                 throw new IllegalArgumentException("The form for the specified encounter (" + encounter.getForm() + ") does not have an HtmlForm associated with it");
         }
         if (hf == null)
             throw new RuntimeException("Could not find HTML Form");
 
         // the code below doesn't handle the HFFS case where you might want to _add_ data to an existing encounter
         FormEntrySession fes;
         if (encounter != null) {
             fes = new FormEntrySession(patient, encounter, FormEntryContext.Mode.EDIT, hf, emrContext.getSessionLocation(), httpSession, automaticValidation, !automaticValidation);
         }
         else {
             fes = new FormEntrySession(patient, hf, FormEntryContext.Mode.ENTER, emrContext.getSessionLocation(), httpSession, automaticValidation, !automaticValidation);
         }
 
        if (returnUrl != null) {
             fes.setReturnUrl(returnUrl);
         }
 
         model.addAttribute("command", fes);
         model.addAttribute("visit", visit);
         if(createVisit!=null){
             model.addAttribute("createVisit", createVisit.toString());
         }else{
             model.addAttribute("createVisit", "false");
         }
     }
 
     /**
      * Creates a simple object to record if there is an authenticated user
      * @return the simple object
      */
     public SimpleObject checkIfLoggedIn() {
         return SimpleObject.create("isLoggedIn", Context.isAuthenticated());
     }
 
     /**
      * Tries to authenticate with the given credentials
      * @param user the username
      * @param pass the password
      * @return a simple object to record if successful
      */
     public SimpleObject authenticate(@RequestParam("user") String user, @RequestParam("pass") String pass) {
         try {
             Context.authenticate(user, pass);
         } catch (ContextAuthenticationException ex) {
             // do nothing
         }
         return checkIfLoggedIn();
     }
 
     /**
      * Handles a form submit request
      * @param patient
      * @param hf
      * @param encounter
      * @param visit
      * @param returnUrl
      * @param request
      * @return
      * @throws Exception
      */
     public SimpleObject submit(
                           EmrContext emrContext,
                          @RequestParam("personId") Patient patient,
                          @RequestParam("htmlFormId") HtmlForm hf,
                          @RequestParam(value = "encounterId", required = false) Encounter encounter,
                          @RequestParam(value = "visitId", required = false) Visit visit,
                          @RequestParam(value = "createVisit", required = false) Boolean createVisit,
                          @RequestParam(value = "returnUrl", required = false) String returnUrl,
                          @SpringBean("encounterService") EncounterService encounterService,
                          @SpringBean("adtService") AdtService adtService,
                          @SpringBean("coreResourceFactory") ResourceFactory resourceFactory,
                          UiUtils ui,
                          HttpServletRequest request) throws Exception {
 
         // TODO formModifiedTimestamp and encounterModifiedTimestamp
         // TODO support for real-time mode (i.e. automatically set encounterDatetime=now, and put in the current visit)
 
         FormEntrySession fes;
         if (encounter != null) {
             fes = new FormEntrySession(patient, encounter, FormEntryContext.Mode.EDIT, hf, request.getSession());
         } else {
             fes = new FormEntrySession(patient, hf, FormEntryContext.Mode.ENTER, request.getSession());
         }
 
         if (returnUrl != null) {
             fes.setReturnUrl(returnUrl);
         }
 
         // Validate and return with errors if any are found
         List<FormSubmissionError> validationErrors = fes.getSubmissionController().validateSubmission(fes.getContext(), request);
         if (validationErrors.size() > 0) {
             return returnHelper(validationErrors, fes.getContext());
         }
 
         // No validation errors found so process form submission
         fes.prepareForSubmit();
         fes.getSubmissionController().handleFormSubmission(fes, request);
 
         // Check this form will actually create an encounter if its supposed to
         if (fes.getContext().getMode() == FormEntryContext.Mode.ENTER && fes.hasEncouterTag() && (fes.getSubmissionActions().getEncountersToCreate() == null || fes.getSubmissionActions().getEncountersToCreate().size() == 0)) {
             throw new IllegalArgumentException("This form is not going to create an encounter");
         }
 
         // If encounter is for a specific visit then check encounter date is valid for that visit
         Encounter formEncounter = fes.getContext().getMode() == FormEntryContext.Mode.ENTER ? fes.getSubmissionActions().getEncountersToCreate().get(0) : encounter;
         Date formEncounterDateTime = formEncounter.getEncounterDatetime();
         if (visit != null) {
             if (formEncounterDateTime.before(visit.getStartDatetime())) {
                 validationErrors.add(new FormSubmissionError("general-form-error", "Encounter datetime should be after the visit start date"));
             }
             if (visit.getStopDatetime() != null && formEncounterDateTime.after(visit.getStopDatetime())) {
                 validationErrors.add(new FormSubmissionError("general-form-error", "Encounter datetime should be before the visit stop date"));
             }
 
             if (validationErrors.size() > 0) {
                 return returnHelper(validationErrors, fes.getContext());
             }
         }else if(createVisit!=null && (createVisit)){
             visit= adtService.ensureActiveVisit(patient, emrContext.getSessionLocation());
             visit.setStartDatetime(formEncounterDateTime);
         }
 
         // Do actual encounter creation/updating
         fes.applyActions();
 
         // Add created encounter to the specified visit
         if (encounter == null && visit != null) {
             encounter = fes.getEncounter();
             encounter.setVisit(visit);
             encounterService.saveEncounter(encounter);
         }
 
         request.getSession().setAttribute(EmrConstants.SESSION_ATTRIBUTE_INFO_MESSAGE,
                ui.message("emr.task.enterHtmlForm.successMessage", ui.format(hf.getForm()), ui.format(patient)));
         request.getSession().setAttribute(EmrConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");
 
         return returnHelper(null, null);
     }
 
     private SimpleObject returnHelper(List<FormSubmissionError> validationErrors, FormEntryContext context) {
         if (validationErrors == null || validationErrors.size() == 0) {
             return SimpleObject.create("success", true);
         } else {
             Map<String, String> errors = new HashMap<String, String>();
             for (FormSubmissionError err : validationErrors) {
                 if (err.getSourceWidget() != null)
                     errors.put(context.getErrorFieldId(err.getSourceWidget()), err.getError());
                 else
                     errors.put(err.getId(), err.getError());
             }
             return SimpleObject.create("success", false, "errors", errors);
         }
     }
 
 }
 
