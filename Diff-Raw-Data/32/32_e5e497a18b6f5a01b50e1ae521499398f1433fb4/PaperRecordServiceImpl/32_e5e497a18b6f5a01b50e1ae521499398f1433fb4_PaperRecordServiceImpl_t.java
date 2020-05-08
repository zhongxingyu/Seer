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
 
 package org.openmrs.module.emr.api.impl;
 
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.PatientService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.emr.EmrConstants;
 import org.openmrs.module.emr.api.PaperRecordService;
 import org.openmrs.module.emr.api.db.PaperRecordRequestDAO;
 import org.openmrs.module.emr.domain.PaperRecordRequest;
 import org.openmrs.module.emr.utils.GeneralUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.Date;
 import java.util.List;
 
 public class PaperRecordServiceImpl implements PaperRecordService {
 
     private PaperRecordRequestDAO paperRecordRequestDAO;
 
     @Autowired
     @Qualifier("adminService")
     private AdministrationService administrationService;
 
     @Autowired
     @Qualifier("patientService")
     private PatientService patientService;
 
     public void setPaperRecordRequestDAO(PaperRecordRequestDAO paperRecordRequestDAO) {
         this.paperRecordRequestDAO = paperRecordRequestDAO;
     }
 
     @Override
    public PaperRecordRequest getPaperRecordRequestById(Integer id) {
        return paperRecordRequestDAO.getById(id);
    }

    @Override
     @Transactional
     public void requestPaperRecord(Patient patient, Location medicalRecordLocation, Location requestLocation) {
 
         //  TODO:  handle bogus parameters;
         // TODO: handle null case if no patient identifier found or patient has multiple identifiers
 
         // fetch the appropriate identifier
         PatientIdentifier paperRecordIdentifier = GeneralUtils.getPatientIdentifier(patient, getPaperRecordIdentifierType(), medicalRecordLocation);
 
         PaperRecordRequest request = new PaperRecordRequest();
         request.setCreator(Context.getAuthenticatedUser());
         request.setDateCreated(new Date());
         request.setIdentifier(paperRecordIdentifier.getIdentifier());
         request.setRecordLocation(medicalRecordLocation);
         request.setPatient(patient);
         request.setRequestLocation(requestLocation);
 
         paperRecordRequestDAO.saveOrUpdate(request);
     }
 
     @Override
     public List<PaperRecordRequest> getOpenPaperRecordRequests() {
         return paperRecordRequestDAO.getOpenPaperRecordRequests();
     }
 
     protected PatientIdentifierType getPaperRecordIdentifierType() {
         String uuid = administrationService.getGlobalProperty(EmrConstants.GP_PAPER_RECORD_IDENTIFIER_TYPE);
         PatientIdentifierType paperRecordIdentifierType = patientService.getPatientIdentifierTypeByUuid(uuid);
         if (paperRecordIdentifierType == null) {
             throw new IllegalStateException("Configuration required: " + EmrConstants.GP_PAPER_RECORD_IDENTIFIER_TYPE);
         }
         return paperRecordIdentifierType;
     }
 }
