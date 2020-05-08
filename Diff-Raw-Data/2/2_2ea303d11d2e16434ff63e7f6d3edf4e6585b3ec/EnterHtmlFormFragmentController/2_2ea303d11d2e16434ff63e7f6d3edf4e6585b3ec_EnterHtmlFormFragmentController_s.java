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
 
 package org.openmrs.module.htmlformentryui.fragment.controller.htmlform;
 
 import org.joda.time.DateMidnight;
 import org.openmrs.Encounter;
 import org.openmrs.Form;
 import org.openmrs.Patient;
 import org.openmrs.Visit;
 import org.openmrs.api.EncounterService;
 import org.openmrs.api.FormService;
 import org.openmrs.api.context.Context;
 import org.openmrs.api.context.ContextAuthenticationException;
 import org.openmrs.module.appframework.feature.FeatureToggleProperties;
 import org.openmrs.module.appui.UiSessionContext;
 import org.openmrs.module.emrapi.adt.AdtService;
 import org.openmrs.module.emrapi.adt.exception.EncounterDateAfterVisitStopDateException;
 import org.openmrs.module.emrapi.adt.exception.EncounterDateBeforeVisitStartDateException;
 import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
 import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
 import org.openmrs.module.htmlformentry.FormEntryContext;
 import org.openmrs.module.htmlformentry.FormEntrySession;
 import org.openmrs.module.htmlformentry.FormSubmissionError;
 import org.openmrs.module.htmlformentry.HtmlForm;
 import org.openmrs.module.htmlformentry.HtmlFormEntryService;
 import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
 import org.openmrs.module.htmlformentryui.HtmlFormUtil;
 import org.openmrs.module.uicommons.UiCommonsConstants;
 import org.openmrs.ui.framework.SimpleObject;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.annotation.FragmentParam;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.ui.framework.fragment.FragmentConfiguration;
 import org.openmrs.ui.framework.fragment.FragmentModel;
 import org.openmrs.ui.framework.resource.ResourceFactory;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.StringUtils;
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
      * @param sessionContext
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
                            UiSessionContext sessionContext,
                            UiUtils ui,
                            @SpringBean("htmlFormEntryService") HtmlFormEntryService htmlFormEntryService,
                            @SpringBean("formService") FormService formService,
                            @SpringBean("coreResourceFactory") ResourceFactory resourceFactory,
                           @SpringBean("featureToggleProperties") FeatureToggleProperties featureToggles,
                            @FragmentParam("patient") Patient patient,
                            @FragmentParam(value = "htmlForm", required = false) HtmlForm hf,
                            @FragmentParam(value = "htmlFormId", required = false) Integer htmlFormId,
                            @FragmentParam(value = "formId", required = false) Form form,
                            @FragmentParam(value = "formUuid", required = false) String formUuid,
                            @FragmentParam(value = "definitionUiResource", required = false) String definitionUiResource,
                            @FragmentParam(value = "encounter", required = false) Encounter encounter,
                            @FragmentParam(value = "visit", required = false) VisitDomainWrapper visit,
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
             fes = new FormEntrySession(patient, encounter, FormEntryContext.Mode.EDIT, hf, null, httpSession, automaticValidation, !automaticValidation);
         }
         else {
             fes = new FormEntrySession(patient, hf, FormEntryContext.Mode.ENTER, null, httpSession, automaticValidation, !automaticValidation);
         }
         fes.setAttribute("uiSessionContext", sessionContext);
         fes.setAttribute("uiUtils", ui);
 
         if (StringUtils.hasText(returnUrl)) {
             fes.setReturnUrl(returnUrl);
         }
 
         fes.addToVelocityContext("visit", visit);
         fes.addToVelocityContext("sessionContext", sessionContext);
         fes.addToVelocityContext("ui", ui);
         fes.addToVelocityContext("featureToggles", featureToggles);
 
         model.addAttribute("currentDatetime", new Date());
         model.addAttribute("command", fes);
         model.addAttribute("visit", visit);
         if (createVisit!=null) {
             model.addAttribute("createVisit", createVisit.toString());
         } else {
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
     @Transactional
     public SimpleObject submit(UiSessionContext sessionContext,
                          @RequestParam("personId") Patient patient,
                          @RequestParam("htmlFormId") HtmlForm hf,
                          @RequestParam(value = "encounterId", required = false) Encounter encounter,
                          @RequestParam(value = "visitId", required = false) Visit visit,
                          @RequestParam(value = "createVisit", required = false) Boolean createVisit,
                          @RequestParam(value = "returnUrl", required = false) String returnUrl,
                          @SpringBean("encounterService") EncounterService encounterService,
                          @SpringBean("adtService") AdtService adtService,
                          @SpringBean("coreResourceFactory") ResourceFactory resourceFactory,
                          @SpringBean("featureToggleProperties") FeatureToggleProperties featureToggles,
                          UiUtils ui,
                          HttpServletRequest request) throws Exception {
 
         // TODO formModifiedTimestamp and encounterModifiedTimestamp
 
         boolean editMode = encounter != null;
 
         FormEntrySession fes;
         if (encounter != null) {
             fes = new FormEntrySession(patient, encounter, FormEntryContext.Mode.EDIT, hf, request.getSession());
         } else {
             fes = new FormEntrySession(patient, hf, FormEntryContext.Mode.ENTER, request.getSession());
         }
         fes.setAttribute("uiSessionContext", sessionContext);
         fes.setAttribute("uiUtils", ui);
 
         fes.addToVelocityContext("visit", visit);
         fes.addToVelocityContext("sessionContext", sessionContext);
         fes.addToVelocityContext("ui", ui);
         fes.addToVelocityContext("featureToggles", featureToggles);
 
         if (returnUrl != null) {
             fes.setReturnUrl(returnUrl);
         }
         fes.getHtmlToDisplay();
 
         // Validate and return with errors if any are found
         List<FormSubmissionError> validationErrors = fes.getSubmissionController().validateSubmission(fes.getContext(), request);
         if (validationErrors.size() > 0) {
             return returnHelper(validationErrors, fes.getContext(), null);
         }
 
         // No validation errors found so process form submission
         fes.prepareForSubmit();
         fes.getSubmissionController().handleFormSubmission(fes, request);
 
         // Check this form will actually create an encounter if its supposed to
         if (fes.getContext().getMode() == FormEntryContext.Mode.ENTER && fes.hasEncouterTag() && (fes.getSubmissionActions().getEncountersToCreate() == null || fes.getSubmissionActions().getEncountersToCreate().size() == 0)) {
             throw new IllegalArgumentException("This form is not going to create an encounter");
         }
 
         Encounter formEncounter = fes.getContext().getMode() == FormEntryContext.Mode.ENTER ? fes.getSubmissionActions().getEncountersToCreate().get(0) : encounter;
 
         // we don't want to lose any time information just because we edited it with a form that only collects date
         if (fes.getContext().getMode() == FormEntryContext.Mode.EDIT && hasNoTimeComponent(formEncounter.getEncounterDatetime())) {
             keepTimeComponentOfEncounterIfDateComponentHasNotChanged(fes.getContext().getPreviousEncounterDate(), formEncounter);
         }
 
         // create a visit if necessary
         // (note that this currently only works in real-time mode)
         if (createVisit != null && (createVisit) && visit == null) {
             visit = adtService.ensureActiveVisit(patient, sessionContext.getSessionLocation());
         }
 
         // attach to the visit if it exists
         if (visit != null) {
             try {
                 new EncounterDomainWrapper(formEncounter).attachToVisit(visit);
             }
             catch (EncounterDateBeforeVisitStartDateException e) {
                 validationErrors.add(new FormSubmissionError("general-form-error", "Encounter datetime should be after the visit start date"));
             }
             catch (EncounterDateAfterVisitStopDateException e) {
                 validationErrors.add(new FormSubmissionError("general-form-error", "Encounter datetime should be before the visit stop date"));
             }
 
             if (validationErrors.size() > 0) {
                 return returnHelper(validationErrors, fes.getContext(), null);
             }
         }
 
         // Do actual encounter creation/updating
         fes.applyActions();
 
         request.getSession().setAttribute(UiCommonsConstants.SESSION_ATTRIBUTE_INFO_MESSAGE,
                 ui.message(editMode ? "emr.editHtmlForm.successMessage" : "emr.task.enterHtmlForm.successMessage", ui.format(hf.getForm()), ui.format(patient)));
         request.getSession().setAttribute(UiCommonsConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");
 
         return returnHelper(null, null, formEncounter);
     }
 
     private SimpleObject returnHelper(List<FormSubmissionError> validationErrors, FormEntryContext context,
                                       Encounter encounter) {
         if (validationErrors == null || validationErrors.size() == 0) {
             return SimpleObject.create("success", true, "encounterId", encounter.getId());
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
 
     private boolean hasNoTimeComponent(Date date) {
         return new DateMidnight(date).toDate().equals(date);
     }
 
     private void keepTimeComponentOfEncounterIfDateComponentHasNotChanged(Date previousEncounterDate, Encounter formEncounter) {
 
         if (previousEncounterDate != null
                 && new DateMidnight(previousEncounterDate).equals(new DateMidnight(formEncounter.getEncounterDatetime()))) {
             formEncounter.setEncounterDatetime(previousEncounterDate);
         }
 
     }
 
 }
 
