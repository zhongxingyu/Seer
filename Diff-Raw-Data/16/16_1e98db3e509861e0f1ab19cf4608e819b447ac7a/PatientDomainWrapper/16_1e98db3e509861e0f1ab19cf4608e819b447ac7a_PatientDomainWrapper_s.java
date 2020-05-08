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
 
 package org.openmrs.module.emr.patient;
 
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.Visit;
 import org.openmrs.api.EncounterService;
 import org.openmrs.api.VisitService;
 import org.openmrs.module.emr.EmrProperties;
 import org.openmrs.module.emr.adt.AdtService;
 import org.openmrs.module.emr.visit.VisitDomainWrapper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 /**
  * A rich-domain-model class that wraps a Patient, and lets you perform common queries.
  */
 public class PatientDomainWrapper {
 
     private Patient patient;
 
     @Qualifier("emrProperties")
     @Autowired
     protected EmrProperties emrProperties;
 
     @Qualifier("adtService")
     @Autowired
     protected AdtService adtService;
 
     @Qualifier("visitService")
     @Autowired
     protected VisitService visitService;
 
     @Qualifier("encounterService")
     @Autowired
     protected EncounterService encounterService;
 
     public PatientDomainWrapper() {
     }
 
     public PatientDomainWrapper(Patient patient, EmrProperties emrProperties, AdtService adtService,
                                 VisitService visitService, EncounterService encounterService) {
         this.patient = patient;
         this.emrProperties = emrProperties;
         this.adtService = adtService;
         this.visitService = visitService;
         this.encounterService = encounterService;
     }
 
     public void setPatient(Patient patient) {
         this.patient = patient;
     }
 
     public Patient getPatient() {
         return patient;
     }
 
     public String getGender() {
         return patient.getGender();
     }
 
     public Integer getAge() {
         return patient.getAge();
     }
 
     public Boolean getBirthdateEstimated() {
         return patient.getBirthdateEstimated();
     }
 
     public Date getBirthdate() {
         return patient.getBirthdate();
     }
 
     public PatientIdentifier getPrimaryIdentifier() {
         List<PatientIdentifier> primaryIdentifiers = getPrimaryIdentifiers();
         if (primaryIdentifiers.size() == 0) {
             return null;
         } else {
             return primaryIdentifiers.get(0);
         }
     }
 
     public List<PatientIdentifier> getPrimaryIdentifiers() {
         return patient.getPatientIdentifiers(emrProperties.getPrimaryIdentifierType());
     }
 
     public PatientIdentifier getPaperRecordIdentifier() {
         List<PatientIdentifier> paperRecordIdentifiers = getPaperRecordIdentifiers();
         if (paperRecordIdentifiers.size() == 0) {
             return null;
         } else {
             return paperRecordIdentifiers.get(0);
         }
     }
 
     public List<PatientIdentifier> getPaperRecordIdentifiers() {
         PatientIdentifierType paperRecordIdentifierType = emrProperties.getPaperRecordIdentifierType();
         if (paperRecordIdentifierType == null) {
             return new ArrayList<PatientIdentifier>();
         }
         return patient.getPatientIdentifiers(paperRecordIdentifierType);
     }
 
     public Encounter getLastEncounter() {
         return adtService.getLastEncounter(patient);
     }
 
     public Visit getActiveVisit(Location location) {
         return adtService.getActiveVisit(patient, location);
     }
 
     public int getCountOfEncounters() {
         return adtService.getCountOfEncounters(patient);
     }
 
     public int getCountOfVisits() {
         return adtService.getCountOfVisits(patient);
     }
 
     public List<Encounter> getAllEncounters() {
         return encounterService.getEncountersByPatient(patient);
     }
 
     public List<Visit> getAllVisits() {
         return visitService.getVisitsByPatient(patient, true, false);
     }
 
     public boolean hasOverlappingVisitsWith(Patient otherPatient) {
         List<Visit> otherVisits = visitService.getVisitsByPatient(otherPatient, true, false);
         List<Visit> myVisits = getAllVisits();
 
         for (Visit v : myVisits) {
             for (Visit o : otherVisits) {
                 if (adtService.visitsOverlap(v, o)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public boolean isUnknownPatient() {
         boolean unknownPatient = false;
         PersonAttributeType unknownPatientAttributeType = emrProperties.getUnknownPatientPersonAttributeType();
         if(patient!=null){
             PersonAttribute att = patient.getAttribute(unknownPatientAttributeType);
             if (att != null && "true".equals(att.getValue())) {
                 unknownPatient = true;
             }
         }
         return unknownPatient;
     }
 
     public List<VisitDomainWrapper> getAllVisitsUsingWrappers() {
         List<VisitDomainWrapper> visitDomainWrappers = new ArrayList<VisitDomainWrapper>();
 
         for (Visit visit : getAllVisits()) {
             visitDomainWrappers.add(new VisitDomainWrapper(visit));
         }
 
         return visitDomainWrappers;
     }
 
     public String getFormattedName() {
         return getPersonName().getFamilyName() + ", " + getPersonName().getGivenName();
     }
 
     public PersonName getPersonName() {
         Set<PersonName> names = patient.getNames();
         if (names != null && names.size() > 0) {
             for (PersonName name : names) {
                 if (name.isPreferred() )
                     return name;
             }
             for (PersonName name : names) {
                 return name;
             }
 
         }
         return null;
     }
 }
