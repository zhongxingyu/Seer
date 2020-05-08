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
 
 package org.openmrs.module.emr.consult;
 
 import org.openmrs.Concept;
 import org.openmrs.Encounter;
 import org.openmrs.Obs;
 import org.openmrs.api.EncounterService;
 import org.openmrs.api.VisitService;
import org.openmrs.api.handler.EncounterVisitHandler;
 import org.openmrs.api.impl.BaseOpenmrsService;
 import org.openmrs.module.emrapi.EmrApiProperties;
 import org.openmrs.module.emrapi.concept.EmrConceptService;
 import org.openmrs.module.emrapi.diagnosis.Diagnosis;
 import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
 import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
 import org.openmrs.module.emrapi.disposition.actions.DispositionAction;
 import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.StringUtils;
 
 import java.util.Date;
 
 /**
  *
  */
 public class ConsultServiceImpl extends BaseOpenmrsService implements ConsultService {
 
     @Autowired
     private ApplicationContext applicationContext;
 
     private EmrApiProperties emrApiProperties;
 
     private EncounterService encounterService;
 
     private VisitService visitService;
 
     private EmrConceptService emrConceptService;
 
     @Transactional
     @Override
     public Encounter saveConsultNote(ConsultNote consultNote) {
         if (consultNote.getPatient() == null || consultNote.getEncounterLocation() == null || consultNote.getDiagnoses(Diagnosis.Order.PRIMARY).size() == 0 || consultNote.getClinician() == null) {
             throw new IllegalArgumentException("Required: patient, encounter location, clinician provider, primary diagnosis");
         }
 
         DiagnosisMetadata diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();
         DispositionDescriptor dispositionDescriptor = emrApiProperties.getDispositionDescriptor();
 
         Encounter encounter = new Encounter();
         encounter.setEncounterDatetime(new Date());
         encounter.setLocation(consultNote.getEncounterLocation());
         encounter.setEncounterType(emrApiProperties.getConsultEncounterType());
         encounter.setPatient(consultNote.getPatient());
 
         encounter.addProvider(emrApiProperties.getClinicianEncounterRole(), consultNote.getClinician());
 
         for (Diagnosis diagnosis : consultNote.getDiagnoses()) {
             encounter.addObs(diagnosisMetadata.buildDiagnosisObsGroup(diagnosis));
         }
 
         if (StringUtils.hasText(consultNote.getComments())) {
             encounter.addObs(buildTextObs(emrApiProperties.getConsultFreeTextCommentsConcept(), consultNote.getComments()));
         }
 
         // normally we'd wait for encounterService.saveEncounter to assign a visit, but the actions may want to modify the visit,
         // so assign that now
        EncounterVisitHandler activeEncounterVisitHandler = encounterService.getActiveEncounterVisitHandler();
        if (activeEncounterVisitHandler != null) {
            activeEncounterVisitHandler.beforeCreateEncounter(encounter);
            if (encounter.getVisit() != null && encounter.getVisit().getVisitId() == null) {
                //If we have been assigned a new visit, persist it.
                visitService.saveVisit(encounter.getVisit());
            }
         }
 
         EncounterDomainWrapper encounterDomainWrapper = new EncounterDomainWrapper(encounter);
 
         if (consultNote.getDisposition() != null) {
             Obs dispositionGroup = dispositionDescriptor.buildObsGroup(consultNote.getDisposition(), emrConceptService);
             for (String actionBeanName : consultNote.getDisposition().getActions()) {
                 DispositionAction action = applicationContext.getBean(actionBeanName, DispositionAction.class);
                 action.action(encounterDomainWrapper, dispositionGroup, consultNote.getDispositionParameters());
             }
             encounter.addObs(dispositionGroup);
         }
 
         return encounterService.saveEncounter(encounter);
     }
 
     private Obs buildTextObs(Concept question, String answer) {
         Obs obs = new Obs();
         obs.setConcept(question);
         obs.setValueText(answer);
         return obs;
     }
 
     public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
         this.emrApiProperties = emrApiProperties;
     }
 
     public void setEncounterService(EncounterService encounterService) {
         this.encounterService = encounterService;
     }
 
     public void setVisitService(VisitService visitService) {
         this.visitService = visitService;
     }
 
     public void setEmrConceptService(EmrConceptService emrConceptService) {
         this.emrConceptService = emrConceptService;
     }
 
     public void setApplicationContext(ApplicationContext applicationContext) {
         this.applicationContext = applicationContext;
     }
 
 }
