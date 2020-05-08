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
 
 package org.openmrs.module.kenyacore.form;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Encounter;
 import org.openmrs.Form;
 import org.openmrs.Patient;
 import org.openmrs.Program;
 import org.openmrs.Visit;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.appframework.AppDescriptor;
 import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
 import org.openmrs.module.htmlformentry.handler.TagHandler;
 import org.openmrs.module.kenyacore.ContentManager;
 import org.openmrs.module.kenyacore.CoreUtils;
 import org.openmrs.module.kenyacore.form.FormDescriptor.Gender;
 import org.openmrs.module.kenyacore.program.ProgramDescriptor;
 import org.openmrs.module.kenyacore.program.ProgramManager;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * Forms manager
  */
 @Component
 public class FormManager implements ContentManager {
 
 	protected static final Log log = LogFactory.getLog(FormManager.class);
 
 	protected static final String tagHandlerClassSuffix = "TagHandler";
 
 	private Map<String, FormDescriptor> forms = new LinkedHashMap<String, FormDescriptor>();
 
 	private List<FormDescriptor> commonPatientForms = new ArrayList<FormDescriptor>();
 	private List<FormDescriptor> commonVisitForms = new ArrayList<FormDescriptor>();
 
 	@Autowired
 	private ProgramManager programManager;
 
 	/**
 	 * @see org.openmrs.module.kenyacore.ContentManager#getPriority()
 	 */
 	@Override
 	public int getPriority() {
 		return 70;
 	}
 
 	/**
 	 * @see org.openmrs.module.kenyacore.ContentManager#refresh()
 	 */
 	@Override
 	public synchronized void refresh() {
 		forms.clear();
 		commonPatientForms.clear();
 		commonVisitForms.clear();
 
 		List<FormDescriptor> descriptors = Context.getRegisteredComponents(FormDescriptor.class);
 
 		// Sort by form descriptor order
 		Collections.sort(descriptors);
 
 		// Process form descriptor beans
 		for (FormDescriptor formDescriptor : descriptors) {
 			Form form = Context.getFormService().getFormByUuid(formDescriptor.getTargetUuid());
 
 			if (form == null) {
 				throw new RuntimeException("No such form with UUID: " + formDescriptor.getTargetUuid());
 			}
 
 			if (forms.containsKey(formDescriptor.getTargetUuid())) {
 				throw new RuntimeException("Form " + formDescriptor.getTargetUuid() + " already registered");
 			}
 
 			forms.put(form.getUuid(), formDescriptor);
 
 			// Attach form resource if descriptor specifies one
 			if (formDescriptor.getHtmlform() != null) {
 				FormUtils.setFormXmlResource(form, formDescriptor.getHtmlform());
 			}
 
 			log.debug("Registered form '" + form.getName() + "' (" + form.getUuid() + ")");
 		}
 
 		// Process form configuration beans
 		for (FormConfiguration configuration : Context.getRegisteredComponents(FormConfiguration.class)) {
 			// Register general per-patient forms
 			commonPatientForms.addAll(configuration.getCommonPatientForms());
 
 			// Register general per-visit forms
 			commonVisitForms.addAll(configuration.getCommonVisitForms());
 
 			// Register additional program specific per-patient forms
 			for (ProgramDescriptor programDescriptor : configuration.getProgramPatientForms().keySet()) {
 				for (FormDescriptor form : configuration.getProgramPatientForms().get(programDescriptor)) {
 					programDescriptor.addPatientForm(form);
 				}
 			}
 
 			// Register additional program specific per-visit forms
 			for (ProgramDescriptor programDescriptor : configuration.getProgramVisitForms().keySet()) {
 				for (FormDescriptor form : configuration.getProgramVisitForms().get(programDescriptor)) {
 					programDescriptor.addVisitForm(form);
 				}
 			}
 
 			// Disable forms which should be disabled
 			for (FormDescriptor descriptor : configuration.getDisabledForms()) {
 				descriptor.setEnabled(false);
 			}
 		}
 
 		commonPatientForms = CoreUtils.merge(commonPatientForms); // Sorts and removes duplicates
 		commonVisitForms = CoreUtils.merge(commonVisitForms);
 
 		refreshTagHandlers();
 	}
 
 	/**
 	 * Refreshes tag handler components
 	 */
 	private void refreshTagHandlers() {
 		for (TagHandler tagHandler : Context.getRegisteredComponents(TagHandler.class)) {
 			String className = tagHandler.getClass().getSimpleName();
 
 			if (className.endsWith(tagHandlerClassSuffix)) {
 				String tagName = StringUtils.uncapitalize(className.substring(0, className.length() - tagHandlerClassSuffix.length()));
 				HtmlFormEntryUtil.getService().addHandler(tagName, tagHandler);
 				log.info("Registered tag handler class " + className + " for tag <" + tagName + ">");
 			}
 			else {
 				log.warn("Not registering tag handler class " + className + ". Name does not end with " + tagHandlerClassSuffix);
 			}
 		}
 	}
 
 	/**
 	 * Gets the form descriptor for the given form
 	 * @param form the form
 	 * @return the form descriptor
 	 */
 	public FormDescriptor getFormDescriptor(Form form) {
 		return forms.get(form.getUuid());
 	}
 
 	/**
 	 * Gets all registered form descriptors
 	 * @return the form descriptors
 	 */
 	public Collection<FormDescriptor> getAllFormDescriptors() {
 		return forms.values();
 	}
 
 	/**
 	 * Gets all per-patient forms
 	 * @param app the current application
 	 * @param patient the patient
 	 * @return the form descriptors
 	 */
 	public List<FormDescriptor> getCommonFormsForPatient(AppDescriptor app, Patient patient) {
 		return filterForms(commonPatientForms, app, patient);
 	}
 
 	/**
 	 * Gets the program specific per-patient forms
 	 * @param app the current application
 	 * @param program the program
 	 * @param patient the patient
 	 * @return the form descriptors
 	 */
 	public List<FormDescriptor> getProgramFormsForPatient(AppDescriptor app, Program program, Patient patient) {
 		ProgramDescriptor programDescriptor = programManager.getProgramDescriptor(program);
 		if (programDescriptor.getPatientForms() != null) {
 			return filterForms(programDescriptor.getPatientForms(), app, patient);
 		}
 		return Collections.emptyList();
 	}
 
 	/**
 	 * Gets all uncompleted per-visit forms appropriate for the given visit
 	 * @param app the current application
 	 * @param visit the visit
 	 * @return the form descriptors
 	 */
 	public List<FormDescriptor> getAllUncompletedFormsForVisit(AppDescriptor app, Visit visit) {
 		List<FormDescriptor> uncompletedForms = new ArrayList<FormDescriptor>();
 		Set<Form> completedForms = new HashSet<Form>();
 
 		// Gather up all completed forms
 		if (visit.getEncounters() != null) {
 			for (Encounter encounter : visit.getEncounters()) {
				if (encounter.getForm() != null) {
 					completedForms.add(encounter.getForm());
 				}
 			}
 		}
 
 		// Include only forms that haven't been completed for this visit
 		for (FormDescriptor suitableForms : getAllFormsForVisit(app, visit)) {
 			if (!completedForms.contains(suitableForms.getTarget())) {
 				uncompletedForms.add(suitableForms);
 			}
 		}
 
 		return uncompletedForms;
 	}
 
 	/**
 	 * Gets all per-visit forms appropriate for the given visit
 	 * @param app the current application
 	 * @param visit the visit
 	 * @return the form descriptors
 	 */
 	public List<FormDescriptor> getAllFormsForVisit(AppDescriptor app, Visit visit) {
 		Set<FormDescriptor> forms = new TreeSet<FormDescriptor>();
 
 		forms.addAll(commonVisitForms);
 
 		// Consider all programs active on the visit stop date, or if visit is still open, active now
 		Date activeOnDate = (visit.getStopDatetime() != null) ? visit.getStopDatetime() : new Date();
 
 		for (ProgramDescriptor activeProgram : programManager.getPatientActivePrograms(visit.getPatient(), activeOnDate)) {
 			if (activeProgram.getVisitForms() != null) {
 				forms.addAll(activeProgram.getVisitForms());
 			}
 		}
 
 		return filterForms(forms, app, visit.getPatient());
 	}
 
 	/**
 	 * Gets all completed per-visit forms appropriate for the given visit
 	 * @param app the current application
 	 * @param visit the visit
 	 * @return the form descriptors
 	 */
 	public List<FormDescriptor> getCompletedFormsForVisit(AppDescriptor app, Visit visit) {
 		List<FormDescriptor> completedForms = new ArrayList<FormDescriptor>();
 
 		for (Encounter encounter : visit.getEncounters()) {
 			if (encounter.getForm() != null) {
 				FormDescriptor descriptor = getFormDescriptor(encounter.getForm());
 
 				// Filter by app and ignore forms with no descriptor
 				if (descriptor != null && descriptor.getApps().contains(app)) {
 					completedForms.add(descriptor);
 				}
 			}
 		}
 
 		return completedForms;
 	}
 
 	/**
 	 * Filters the given collection of enabled forms to those applicable for the given application and patient
 	 * @param app the application
 	 * @param patient the patient
 	 * @return the filtered forms
 	 */
 	protected List<FormDescriptor> filterForms(Collection<FormDescriptor> descriptors, AppDescriptor app, Patient patient) {
 		List<FormDescriptor> filtered = new ArrayList<FormDescriptor>();
 		for (FormDescriptor descriptor : descriptors) {
 			if (!descriptor.isEnabled()) {
 				continue;
 			}
 
 			// Filter by app id
 			if (app != null && descriptor.getApps() != null && !descriptor.getApps().contains(app)) {
 				continue;
 			}
 
 			// Filter by patient gender
 			if (patient.getGender() != null) {
 				if (patient.getGender().equals("F") && descriptor.getGender() == Gender.MALE)
 					continue;
 				else if (patient.getGender().equals("M") && descriptor.getGender() == Gender.FEMALE)
 					continue;
 			}
 
 			filtered.add(descriptor);
 		}
 
 		return filtered;
 	}
 }
