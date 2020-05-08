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
 
 package org.openmrs.module.emr.api;
 
 import junit.framework.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.api.LocationService;
 import org.openmrs.api.PatientService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.emr.domain.PaperRecordRequest;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.List;
 
 public class PaperRecordServiceComponentTest extends BaseModuleContextSensitiveTest {
 
     @Autowired
     PaperRecordService paperRecordService;
 
     @Autowired
     PatientService patientService;
 
     @Autowired
     LocationService locationService;
 
     @Before
     public void beforeAllTests() throws Exception {
         executeDataSet("paperRecordServiceComponentTestDataset.xml");
     }
 
     @Test
     public void testThatServiceIsConfiguredCorrectly() {
         Assert.assertNotNull("Couldn't autowire PaperRecordService", paperRecordService);
         Assert.assertNotNull("Couldn't get PaperRecordService from Context", Context.getService(PaperRecordService.class));
     }
 
     @Test
     public void testCreatePaperRecordRequest() {
 
         // all these are from the standard test dataset
         Patient patient = patientService.getPatient(2) ;
         Location medicalRecordLocation = locationService.getLocation(1);
         Location requestLocation = locationService.getLocation(3);
 
         paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);
 
         // make sure the record is in the database
         List<PaperRecordRequest> requests = paperRecordService.getOpenPaperRecordRequests();
         Assert.assertEquals(1, requests.size());
         PaperRecordRequest request = requests.get(0);
         Assert.assertEquals(new Integer(2), request.getPatient().getId());
         Assert.assertEquals(new Integer(1), request.getRecordLocation().getId());
         Assert.assertEquals(new Integer(3), request.getRequestLocation().getId());
         Assert.assertEquals("101", request.getIdentifier());
         Assert.assertEquals(PaperRecordRequest.Status.OPEN, request.getStatus());
         Assert.assertNull(request.getAssignee());
 
     }
 
 
 
 
 }
