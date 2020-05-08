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
 package org.openmrs.module.dispensing.api.impl;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Location;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.Person;
 import org.openmrs.api.ConceptService;
 import org.openmrs.api.LocationService;
 import org.openmrs.api.ObsService;
 import org.openmrs.api.impl.BaseOpenmrsService;
 import org.openmrs.module.dispensing.DispensedMedication;
 import org.openmrs.module.dispensing.DispensingRequest;
 import org.openmrs.module.dispensing.api.DispensingService;
 import org.openmrs.module.dispensing.api.db.DispensingDAO;
 import org.openmrs.module.dispensing.descriptor.DispensingConceptSetDescriptor;
 import org.openmrs.module.emrapi.EmrApiProperties;
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

 /**
  * It is a default implementation of {@link DispensingService}.
  */
 public class DispensingServiceImpl extends BaseOpenmrsService implements DispensingService {
 	
 	protected final Log log = LogFactory.getLog(this.getClass());
 	
 	private DispensingDAO dao;
 
     private EmrApiProperties emrApiProperties;
     private ObsService obsService;
     private ConceptService conceptService;
     private LocationService locationService;
 	/**
      * @param dao the dao to set
      */
     public void setDao(DispensingDAO dao) {
 	    this.dao = dao;
     }
 
     public void setObsService(ObsService obsService) {
         this.obsService = obsService;
     }
 
     public void setConceptService(ConceptService conceptService) {
         this.conceptService = conceptService;
     }
 
     public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
         this.emrApiProperties = emrApiProperties;
     }
 
     public LocationService getLocationService() {
         return locationService;
     }
 
     public void setLocationService(LocationService locationService) {
         this.locationService = locationService;
     }
 
     @Override
     public void dispense(DispensingRequest dispensingRequest) {
         //TODO: implement
     }
 
     @Override
     public List<DispensedMedication> getDispensedMedication(Patient patient, List<Location> locations, Date fromDate, Date toDate, Integer index, Integer count) {
         List<DispensedMedication> dispensedMedications = null;
         DispensingConceptSetDescriptor dispensingConceptSetDescriptor = new DispensingConceptSetDescriptor(conceptService, locationService);
         List<Person> persons = patient == null ? null : Arrays.asList((Person) patient);
         List<Obs> observations = obsService.getObservations(persons
                 , null
                 , Arrays.asList(dispensingConceptSetDescriptor.getDispensingSetConcept())
                 , null, null
                 , locations, Arrays.asList("obsDatetime"), null, null
                 , fromDate, toDate, false);
         if (observations != null && observations.size() > 0 ){
             dispensedMedications = new ArrayList<DispensedMedication>();
             for (Obs observation : observations) {
                log.error("obsId" + observation.getId().toString());

                 DispensedMedication dispensedMedication;
                 try {
                     dispensedMedication = dispensingConceptSetDescriptor.toDispensedMedication(observation);
                 }
                 catch (Exception e) {
                     log.warn("Error trying to interpret " + observation + " as a dispensed medication");
                     if (log.isDebugEnabled()) {
                         log.debug("Error trying to interpret obs as dispensed medication", e);
                     }
                     continue;
                 }
                 if (dispensedMedication != null){
                     dispensedMedications.add(dispensedMedication);
                 }
             }
         }
 
         return dispensedMedications;
     }
 }
