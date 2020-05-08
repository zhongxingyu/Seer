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
 
 package org.openmrs.module.emr.paperrecord;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.ArgumentMatcher;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.Person;
 import org.openmrs.User;
 import org.openmrs.api.PatientService;
 import org.openmrs.api.context.Context;
 import org.openmrs.messagesource.MessageSourceService;
 import org.openmrs.module.emr.EmrProperties;
 import org.openmrs.module.emr.IsExpectedRequest;
 import org.openmrs.module.emr.paperrecord.PaperRecordRequest.Status;
 import org.openmrs.module.emr.paperrecord.db.PaperRecordMergeRequestDAO;
 import org.openmrs.module.emr.paperrecord.db.PaperRecordRequestDAO;
 import org.openmrs.module.emr.printer.Printer;
 import org.openmrs.module.emr.printer.PrinterService;
 import org.openmrs.module.idgen.service.IdentifierSourceService;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.argThat;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.openmrs.module.emr.paperrecord.PaperRecordRequest.PENDING_STATUSES;
 import static org.powermock.api.mockito.PowerMockito.mockStatic;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(Context.class)
 public class PaperRecordServiceTest {
 
     private PaperRecordServiceImpl paperRecordService;
 
     private PaperRecordRequestDAO mockPaperRecordDAO;
 
     private PaperRecordMergeRequestDAO mockPaperRecordMergeRequestDAO;
 
     private IdentifierSourceService mockIdentifierSourceService;
 
     private PatientService mockPatientService;
 
     private PrinterService mockPrinterService;
 
     private EmrProperties mockEmrProperties;
 
     private PaperRecordLabelTemplate mockPaperRecordLabelTemplate;
 
     private User authenticatedUser;
 
     private PatientIdentifierType paperRecordIdentifierType;
 
 
     @Before
     public void setup() {
         mockStatic(Context.class);
 
         authenticatedUser = new User();
         when(Context.getAuthenticatedUser()).thenReturn(authenticatedUser);
 
         mockPaperRecordDAO = mock(PaperRecordRequestDAO.class);
         mockPaperRecordMergeRequestDAO = mock(PaperRecordMergeRequestDAO.class);
         mockIdentifierSourceService = mock(IdentifierSourceService.class);
         mockPatientService = mock(PatientService.class);
         mockPrinterService = mock(PrinterService.class);
         mockEmrProperties = mock(EmrProperties.class);
         mockPaperRecordLabelTemplate = mock(PaperRecordLabelTemplate.class);
 
         paperRecordIdentifierType = new PatientIdentifierType();
         paperRecordIdentifierType.setId(2);
         when(mockEmrProperties.getPaperRecordIdentifierType()).thenReturn(paperRecordIdentifierType);
 
         paperRecordService = new PaperRecordServiceStub(paperRecordIdentifierType);
         paperRecordService.setPaperRecordRequestDAO(mockPaperRecordDAO);
         paperRecordService.setPaperRecordMergeRequestDAO(mockPaperRecordMergeRequestDAO);
         paperRecordService.setIdentifierSourceService(mockIdentifierSourceService);
         paperRecordService.setPatientService(mockPatientService);
         paperRecordService.setPrinterService(mockPrinterService);
         paperRecordService.setEmrProperties(mockEmrProperties);
         paperRecordService.setPaperRecordLabelTemplate(mockPaperRecordLabelTemplate);
 
         // so we handle the hack in PaperRecordServiceImpl to make sure assignRequestsInternal is transactional
         when(Context.getService(PaperRecordService.class)).thenReturn(paperRecordService);
 
     }
 
     @Test
     public void testPaperRecordExistsShouldReturnTrueIfPaperMedicalRecordExists() {
 
         Location medicalRecordLocation = createMedicalRecordLocation();
         PatientIdentifier identifier = createIdentifier(medicalRecordLocation, "ABCZYX");
 
         when(mockPatientService.getPatientIdentifiers("ABCZYX", Collections.singletonList(paperRecordIdentifierType),
                 Collections.singletonList(medicalRecordLocation), null, null))
                 .thenReturn(Collections.singletonList(identifier));
 
         assertTrue(paperRecordService.paperRecordExists("ABCZYX", medicalRecordLocation));
     }
 
     @Test
     public void testPaperRecordExistsShouldReturnFalseIfPaperMedicalRecordDoesNotExists() {
 
         Location medicalRecordLocation = createMedicalRecordLocation();
 
         when(mockPatientService.getPatientIdentifiers("ABCZYX", Collections.singletonList(paperRecordIdentifierType),
                 Collections.singletonList(medicalRecordLocation), null, null))
                 .thenReturn(new ArrayList<PatientIdentifier>());
 
         assertFalse(paperRecordService.paperRecordExists("ABCZYX", medicalRecordLocation));
     }
 
     @Test
     public void testRequestPaperRecord() throws Exception {
 
         Patient patient = new Patient();
         patient.setId(15);
 
         Location medicalRecordLocation = createMedicalRecordLocation();
         Location requestLocation = createLocation(4, "Outpatient Clinic");
 
         PatientIdentifier identifer = createIdentifier(medicalRecordLocation, "ABCZYX");
         patient.addIdentifier(identifer);
 
         PaperRecordRequest expectedRequest = createPaperRecordRequest(patient, medicalRecordLocation, "ABCZYX");
         IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(expectedRequest);
 
         PaperRecordRequest returnedRequest = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);
         verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
         expectedRequestMatcher.matches(returnedRequest);
     }
 
     private Location createMedicalRecordLocation() {
         return createLocation(3,"Mirebalais");
     }
 
    @Test(expected = IllegalArgumentException.class)
     public void testRequestPaperRecordShouldThrowExceptionIfPatientNull() throws Exception {
 
         Location medicalRecordLocation = createMedicalRecordLocation();
 
         Location requestLocation = createLocation(4, "Outpatient Clinic");
 
         paperRecordService.requestPaperRecord(null, medicalRecordLocation, requestLocation);
 
     }
 
    @Test(expected = IllegalArgumentException.class)
     public void testRequestPaperRecordShouldThrowExceptionIfRecordLocationNull() throws Exception {
 
         Patient patient = new Patient();
         patient.setId(15);
 
         Location requestLocation = createLocation(4, "Outpatient Clinic");
 
         paperRecordService.requestPaperRecord(patient, null, requestLocation);
 
     }
 
 
    @Test(expected = IllegalArgumentException.class)
     public void testRequestPaperRecordShouldThrowExceptionIfRequestLocationNull() throws Exception {
 
         Patient patient = new Patient();
         patient.setId(15);
 
         Location medicalRecordLocation = createMedicalRecordLocation();
 
         paperRecordService.requestPaperRecord(patient, medicalRecordLocation, null);
 
     }
 
 
     @Test
     public void testRequestPaperRecordForPatientWithMultipleIdentifiersOfSameTypeAtDifferentLocations() throws Exception {
 
         Patient patient = new Patient();
         patient.setId(15);
 
         Location medicalRecordLocation = createMedicalRecordLocation();
         Location otherLocation = createLocation(5, "Cange");
         Location requestLocation = createLocation(4, "Outpatient Clinic");
 
         PatientIdentifier wrongIdentifer = createIdentifier(otherLocation, "ZYXCBA");
         patient.addIdentifier(wrongIdentifer);
 
         PatientIdentifier identifer = createIdentifier(medicalRecordLocation, "ABCZYX");
         patient.addIdentifier(identifer);
 
         PaperRecordRequest expectedRequest = createPaperRecordRequest(patient, medicalRecordLocation, "ABCZYX");
 
         IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(expectedRequest);
 
         PaperRecordRequest returnedRequest = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);
         verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
         expectedRequestMatcher.matches(returnedRequest);
     }
 
     @Test
     public void testRequestPaperRecordWhenPatientHasNoValidIdentifier() throws Exception {
 
         MessageSourceService messageSourceService = mock(MessageSourceService.class);
         when(messageSourceService.getMessage("emr.missingPaperRecordIdentifierCode")).thenReturn("UNKNOWN");
         ((PaperRecordServiceImpl) paperRecordService).setMessageSourceService(messageSourceService);
 
         Patient patient = new Patient();
         patient.setId(15);
 
         Location medicalRecordLocation = createMedicalRecordLocation();
 
         Location requestLocation = createLocation(4, "Outpatient Clinic");
 
         PaperRecordRequest expectedRequest = createPaperRecordRequest(patient, medicalRecordLocation, null);
 
         IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(expectedRequest);
 
         PaperRecordRequest returnedRequest = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);
         verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
         expectedRequestMatcher.matches(returnedRequest);
     }
 
     @Test
     public void testAssignRequestsWithoutIdentifiers() throws Exception {
         Person assignTo = new Person(15);
 
         List<PaperRecordRequest> requests = new ArrayList<PaperRecordRequest>();
         requests.add(buildPaperRecordRequestWithoutIdentifier());
         requests.add(buildPaperRecordRequestWithoutIdentifier());
         requests.add(buildPaperRecordRequestWithoutIdentifier());
 
         paperRecordService.assignRequests(requests, assignTo, null);
 
         verify(mockPaperRecordDAO, times(3)).saveOrUpdate(argThat(new IsAssignedTo(assignTo, PaperRecordRequest.Status.ASSIGNED_TO_CREATE)));
     }
 
     @Test
     public void testAssignRequestsWithIdentifiersShouldReturnErrors() throws Exception {
         Person assignTo = new Person(15);
 
         List<PaperRecordRequest> requests = new ArrayList<PaperRecordRequest>();
         requests.add(buildPaperRecordRequestWithIdentifier());
         requests.add(buildPaperRecordRequestWithIdentifier());
         requests.add(buildPaperRecordRequestWithIdentifier());
 
         Map<String,List<String>> response = paperRecordService.assignRequests(requests, assignTo, null);
 
         assertThat(response.get("success").size(), is(3));
 
         verify(mockPaperRecordDAO, times(3)).saveOrUpdate(argThat(new IsAssignedTo(assignTo, PaperRecordRequest.Status.ASSIGNED_TO_PULL)));
     }
 
 
    @Test(expected = IllegalArgumentException.class)
     public void testAssignRequestsShouldFailIfRequestsNull() throws Exception {
 
         Person assignTo = new Person(15);
         paperRecordService.assignRequests(null, assignTo, null);
     }
 
    @Test(expected = IllegalArgumentException.class)
     public void testAssignRequestsShouldFailIfAssigneeNull() throws Exception {
 
         List<PaperRecordRequest> requests = new ArrayList<PaperRecordRequest>();
         requests.add(buildPaperRecordRequestWithoutIdentifier());
         requests.add(buildPaperRecordRequestWithoutIdentifier());
         requests.add(buildPaperRecordRequestWithoutIdentifier());
 
         paperRecordService.assignRequests(requests, null, null);
     }
 
     @Test
     public void testAssignRequestsShouldReturnErrorIfPatientHasValidIdentifierEvenIfRequestDoesNot() throws Exception {
         Person assignTo = new Person(15);
 
         List<PaperRecordRequest> requests = new ArrayList<PaperRecordRequest>();
         requests.add(buildPaperRecordRequestWithoutIdentifier());
 
         // add an identifier to this patient
         Patient patient = requests.get(0).getPatient();
         PatientIdentifier patientIdentifier = new PatientIdentifier();
         patientIdentifier.setIdentifier("ABC");
         patientIdentifier.setIdentifierType(paperRecordIdentifierType);
         patientIdentifier.setLocation(requests.get(0).getRecordLocation());
         patient.addIdentifier(patientIdentifier);
 
         Map<String, List<String>> response = paperRecordService.assignRequests(requests, assignTo, null);
 
         assertThat(response.get("error").size(), is(1));
 
         verify(mockPaperRecordDAO, never()).saveOrUpdate(argThat(new IsAssignedTo(assignTo, PaperRecordRequest.Status.ASSIGNED_TO_PULL, "ABC")));
     }
 
     @Test
     public void whenPatientDoesNotHaveAnPaperMedicalRecordIdentifierShouldCreateAnPaperMedicalRecordNumberAndAssignToHim(){
         String paperMedicalRecordNumberAsExpected = "A000001";
         when(mockIdentifierSourceService.generateIdentifier(paperRecordIdentifierType,"generating a new dossier number")).thenReturn(paperMedicalRecordNumberAsExpected);
 
         Patient patient = new Patient();
 
         PatientIdentifier identifier = new PatientIdentifier(paperMedicalRecordNumberAsExpected, paperRecordIdentifierType, createMedicalRecordLocation());
 
         String paperMedicalRecordNumber = paperRecordService.createPaperMedicalRecordNumberFor(patient, createMedicalRecordLocation());
 
         // cannot compare using one identifier because the equals is not implemented correctly
         verify(mockPatientService).savePatientIdentifier(any(PatientIdentifier.class));
 
         assertEquals(paperMedicalRecordNumberAsExpected, paperMedicalRecordNumber);
     }
 
 
     @Test
     public void whenDuplicateRequestIsMadeNoNewRequestShouldBeGenerated() throws Exception {
 
         Patient patient = new Patient();
         patient.setId(15);
 
         Location medicalRecordLocation = createMedicalRecordLocation();
         Location requestLocation = createLocation(4, "Outpatient Clinic");
 
         // generate an existing paper record request
         PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, "");
         request.setId(10);
         request.setRequestLocation(requestLocation);
         request.setDateCreated(new Date());
 
         when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(PENDING_STATUSES)),
                 eq(patient), eq(medicalRecordLocation), argThat(new NullString()), argThat(new NullBoolean()))).thenReturn(Collections.singletonList(request));
         IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);
 
         // the returned request should be the existing request
         PaperRecordRequest returnedRequest = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);
         verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
         expectedRequestMatcher.matches(request);
 
     }
 
     @Test
     public void whenDuplicateRequestIsMadeLocationShouldBeUpdated() throws Exception {
 
         Patient patient = new Patient();
         patient.setId(15);
 
         Location medicalRecordLocation = createMedicalRecordLocation();
         Location requestLocation = createLocation(4, "Outpatient Clinic");
         Location newRequestLocation = createLocation(5, "ER");
 
         // generate an existing paper record request
         PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, "ABC123");
         request.setId(10);
         request.setRequestLocation(requestLocation);
         request.setDateCreated(new Date());
 
         // expected request is the same, but with the new location
         PaperRecordRequest expectedRequest = createPaperRecordRequest(patient, medicalRecordLocation, "ABC123");
         expectedRequest.setId(10);
         expectedRequest.setRequestLocation(newRequestLocation);
         expectedRequest.setDateCreated(new Date());
 
         when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(PENDING_STATUSES)),
                 eq(patient), eq(medicalRecordLocation), argThat(new NullString()), argThat(new NullBoolean()))).thenReturn(Collections.singletonList(request));
         IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);
 
         // the returned request should be the existing request
         PaperRecordRequest returnedRequest = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, newRequestLocation);
         verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
         expectedRequestMatcher.matches(request);
 
     }
 
     @Test
     public void getPendingPaperRecordRequestByIdentifierShouldRetrieveRequestByIdentifier() {
 
         Patient patient = new Patient();
         patient.setId(15);
 
         Location medicalRecordLocation = createMedicalRecordLocation();
         Location requestLocation = createLocation(4, "Outpatient Clinic");
         Location newRequestLocation = createLocation(5, "ER");
 
         // generate an existing paper record request
         String identifier = "ABC123";
         PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, identifier);
         request.setId(10);
         request.setRequestLocation(requestLocation);
         request.setDateCreated(new Date());
 
         when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(PENDING_STATUSES)),
                 argThat(new NullPatient()), argThat(new NullLocation()), eq(identifier), argThat(new NullBoolean()))).thenReturn(Collections.singletonList(request));
         IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);
 
         PaperRecordRequest returnedRequest = paperRecordService.getPendingPaperRecordRequestByIdentifier(identifier);
         expectedRequestMatcher.matches(request);
 
     }
 
 
     @Test
     public void getPendingPaperRecordRequestByIdentifierShouldReturnNullIfNoActiveRequestWithThatIdentifier() {
         String identifier = "ABC123";
         when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(PENDING_STATUSES)),
                 argThat(new NullPatient()), argThat(new NullLocation()), eq(identifier),  argThat(new NullBoolean()))).thenReturn(null);
         assertNull(paperRecordService.getPendingPaperRecordRequestByIdentifier(identifier));
     }
 
     @Test(expected = IllegalStateException.class)
     public void getPendingPaperRecordRequestByIdentifierShouldThrowIllegalStateExceptionIfMultipleActiveRequestsFound() {
 
         Patient patient = new Patient();
         patient.setId(15);
 
         Location medicalRecordLocation = createMedicalRecordLocation();
         Location requestLocation = createLocation(4, "Outpatient Clinic");
 
         // generate an existing paper record request
         String identifier = "ABC123";
         PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, identifier);
         request.setId(10);
         request.setRequestLocation(requestLocation);
         request.setDateCreated(new Date());
 
         PaperRecordRequest anotherRequest = createPaperRecordRequest(patient, medicalRecordLocation, identifier);
         request.setId(11);
         request.setRequestLocation(requestLocation);
         request.setDateCreated(new Date());
 
         when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(PENDING_STATUSES)),
                 argThat(new NullPatient()), argThat(new NullLocation()), eq(identifier),  argThat(new NullBoolean())))
                 .thenReturn(Arrays.asList(request, anotherRequest));
         paperRecordService.getPendingPaperRecordRequestByIdentifier(identifier);
     }
 
     @Test
     public void getSentPaperRecordRequestByIdentifierShouldRetrieveRequestByIdentifier() {
 
         Patient patient = new Patient();
         patient.setId(15);
 
         Location medicalRecordLocation = createMedicalRecordLocation();
         Location requestLocation = createLocation(4, "Outpatient Clinic");
         Location newRequestLocation = createLocation(5, "ER");
 
         // generate an existing paper record request
         String identifier = "ABC123";
         PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, identifier);
         request.setId(10);
         request.setRequestLocation(requestLocation);
         request.setDateCreated(new Date());
         request.updateStatus(Status.SENT);
 
         when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.SENT))),
                 argThat(new NullPatient()), argThat(new NullLocation()), eq(identifier), argThat(new NullBoolean()))).thenReturn(Collections.singletonList(request));
         IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);
 
         PaperRecordRequest returnedRequest = paperRecordService.getSentPaperRecordRequestByIdentifier(identifier);
         expectedRequestMatcher.matches(request);
 
     }
 
 
     @Test
     public void getSentPaperRecordRequestByIdentifierShouldReturnNullIfNoActiveRequestWithThatIdentifier() {
         String identifier = "ABC123";
         when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.SENT))),
                 argThat(new NullPatient()), argThat(new NullLocation()), eq(identifier),  argThat(new NullBoolean()))).thenReturn(null);
         assertNull(paperRecordService.getSentPaperRecordRequestByIdentifier(identifier));
     }
 
     @Test(expected = IllegalStateException.class)
     public void getSentPaperRecordRequestByIdentifierShouldThrowIllegalStateExceptionIfMultipleActiveRequestsFound() {
 
         Patient patient = new Patient();
         patient.setId(15);
 
         Location medicalRecordLocation = createMedicalRecordLocation();
         Location requestLocation = createLocation(4, "Outpatient Clinic");
         Location newRequestLocation = createLocation(5, "ER");
 
         // generate an existing paper record request
         String identifier = "ABC123";
         PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, identifier);
         request.setId(10);
         request.setRequestLocation(requestLocation);
         request.setDateCreated(new Date());
         request.updateStatus(Status.SENT);
 
         PaperRecordRequest anotherRequest = createPaperRecordRequest(patient, medicalRecordLocation, identifier);
         anotherRequest.setId(11);
         anotherRequest.setRequestLocation(requestLocation);
         anotherRequest.setDateCreated(new Date());
         anotherRequest.updateStatus(Status.SENT);
 
         when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.SENT))),
                 argThat(new NullPatient()), argThat(new NullLocation()), eq(identifier), argThat(new NullBoolean())))
                 .thenReturn(Arrays.asList(request, anotherRequest));
         paperRecordService.getSentPaperRecordRequestByIdentifier(identifier);
     }
 
     @Test
     public void testMarkRequestAsSentShouldMarkRequestAsSent() throws Exception {
         Patient patient = new Patient();
         patient.setId(15);
 
         Location medicalRecordLocation = createMedicalRecordLocation();
 
         PatientIdentifier identifier = createIdentifier(medicalRecordLocation, "ABCZYX");
         patient.addIdentifier(identifier);
 
         PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, "ABCZYX");
         request.setDateCreated(new Date());
 
         paperRecordService.markPaperRecordRequestAsSent(request);
 
         assertThat(request.getStatus(), is(PaperRecordRequest.Status.SENT));
         IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);
         verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
     }
 
     @Test
     public void shouldMarkRequestAsCancelled() throws Exception {
         Patient patient = new Patient();
         patient.setId(15);
 
         Location medicalRecordLocation = createMedicalRecordLocation();
 
         PatientIdentifier identifier = createIdentifier(medicalRecordLocation, "ABCZYX");
         patient.addIdentifier(identifier);
 
         PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, "ABCZYX");
         request.setDateCreated(new Date());
 
         paperRecordService.markPaperRecordRequestAsCancelled(request);
 
         assertThat(request.getStatus(), is(Status.CANCELLED));
         IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);
         verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
     }
 
     @Test
     public void testMarkPaperRecordRequestsAsReturnedShouldMarkSentPaperRecordRequestsAsReturned()
         throws Exception {
 
         Patient patient = new Patient();
         patient.setId(15);
 
         Location medicalRecordLocation = createMedicalRecordLocation();
 
         PatientIdentifier identifier = createIdentifier(medicalRecordLocation, "ABCZYX");
         patient.addIdentifier(identifier);
 
         PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, "ABCZYX");
         request.setDateCreated(new Date());
         request.updateStatus(Status.SENT);
 
         when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.SENT))),
                 argThat(new NullPatient()), argThat(new NullLocation()), eq(identifier.toString()), argThat(new NullBoolean())))
                 .thenReturn(Collections.singletonList(request));
 
         paperRecordService.markPaperRecordRequestsAsReturned("ABCZYX");
 
         assertThat(request.getStatus(), is(Status.RETURNED));
         IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);
         verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
     }
 
     @Test(expected = NoMatchingPaperMedicalRequestException.class)
     public void testMarkPaperRecordRequestsAsReturnedShouldThrowExceptionIfNoMatchingRequest()
             throws Exception {
 
         when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.SENT))),
                 argThat(new NullPatient()), argThat(new NullLocation()), eq("ABCZYX"), argThat(new NullBoolean())))
                 .thenReturn(new ArrayList<PaperRecordRequest>());
 
         paperRecordService.markPaperRecordRequestsAsReturned("ABCZYX");
     }
 
 
     @Test
     public void testMarkPapersRecordForMergeShouldCreatePaperRecordMergeRequest() throws Exception {
 
         Location medicalRecordLocation = createMedicalRecordLocation();
 
         Patient patient1 = new Patient();
         Patient patient2 = new Patient();
 
         PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
         PatientIdentifier identifier2 = createIdentifier(medicalRecordLocation, "EFG");
 
         patient1.addIdentifier(identifier1);
         patient2.addIdentifier(identifier2);
 
         paperRecordService.markPaperRecordsForMerge(identifier1, identifier2);
 
         IsExpectedMergeRequest expectedMergeRequestMatcher = new IsExpectedMergeRequest(createExpectedMergeRequest(patient1,
                 patient2, identifier1.getIdentifier(), identifier2.getIdentifier(), medicalRecordLocation));
 
         verify(mockPaperRecordMergeRequestDAO).saveOrUpdate(argThat(expectedMergeRequestMatcher));
         verify(mockPatientService).voidPatientIdentifier(identifier2, "voided during paper record merge");
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testMarkPaperRecordsForMergeShouldFailIfLocationsDiffer() throws Exception {
 
         Location medicalRecordLocation = createMedicalRecordLocation();
         Location anotherLocation = new Location();
 
         Patient patient1 = new Patient();
         Patient patient2 = new Patient();
 
         PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
         PatientIdentifier identifier2 = createIdentifier(anotherLocation, "EFG");
 
         patient1.addIdentifier(identifier1);
         patient2.addIdentifier(identifier2);
 
         paperRecordService.markPaperRecordsForMerge(identifier1, identifier2);
 
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testMarkPaperRecordsForMergeShouldFailIfFirstIdentifierNotProperType() throws Exception {
 
         Location medicalRecordLocation = createMedicalRecordLocation();
         PatientIdentifierType someOtherIdentifierType = new PatientIdentifierType();
 
         Patient patient1 = new Patient();
         Patient patient2 = new Patient();
 
         PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
         identifier1.setIdentifierType(someOtherIdentifierType);
         PatientIdentifier identifier2 = createIdentifier(medicalRecordLocation, "EFG");
 
         patient1.addIdentifier(identifier1);
         patient2.addIdentifier(identifier2);
 
         paperRecordService.markPaperRecordsForMerge(identifier1, identifier2);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testMarkPaperRecordsForMergeShouldFailIfSecondIdentifierNotProperType() throws Exception {
 
         Location medicalRecordLocation = createMedicalRecordLocation();
         PatientIdentifierType someOtherIdentifierType = new PatientIdentifierType();
 
         Patient patient1 = new Patient();
         Patient patient2 = new Patient();
 
         PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
         PatientIdentifier identifier2 = createIdentifier(medicalRecordLocation, "EFG");
         identifier2.setIdentifierType(someOtherIdentifierType);
 
         patient1.addIdentifier(identifier1);
         patient2.addIdentifier(identifier2);
 
         paperRecordService.markPaperRecordsForMerge(identifier1, identifier2);
     }
 
     @Test
     public void testMarkPaperRecordsAsMergedShouldMarkPaperRecordsAsMerged() throws Exception {
 
         Location medicalRecordLocation = createMedicalRecordLocation();
 
         Patient patient1 = new Patient();
         Patient patient2 = new Patient();
 
         PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
         PatientIdentifier identifier2 = createIdentifier(medicalRecordLocation, "EFG");
 
         patient1.addIdentifier(identifier1);
         patient2.addIdentifier(identifier2);
 
         PaperRecordMergeRequest mergeRequest = new PaperRecordMergeRequest();
         mergeRequest.setPreferredPatient(patient1);
         mergeRequest.setNotPreferredPatient(patient2);
         mergeRequest.setPreferredIdentifier(identifier1.getIdentifier());
         mergeRequest.setNotPreferredIdentifier(identifier2.getIdentifier());
         mergeRequest.setDateCreated(new Date());
         paperRecordService.markPaperRecordsAsMerged(mergeRequest);
 
         assertThat(mergeRequest.getStatus(), is(PaperRecordMergeRequest.Status.MERGED));
         IsExpectedMergeRequest expectedMergeRequestMatcher = new IsExpectedMergeRequest(mergeRequest);
         verify(mockPaperRecordMergeRequestDAO).saveOrUpdate(argThat(expectedMergeRequestMatcher));
     }
 
     @Test
     public void testMarkPaperRecordsAsMergedShouldMergeExistingPendingPaperRecordRequests() throws Exception {
 
         Location medicalRecordLocation = createMedicalRecordLocation();
         Location location1 = new Location(1);
         Location location2 = new Location(2);
 
         Patient patient = new Patient();
         Patient notPreferredPatient = new Patient();
 
         // create the merge request
         PaperRecordMergeRequest mergeRequest = new PaperRecordMergeRequest();
         mergeRequest.setPreferredPatient(patient);
         mergeRequest.setNotPreferredPatient(notPreferredPatient);
         mergeRequest.setPreferredIdentifier("ABC");
         mergeRequest.setNotPreferredIdentifier("XYZ");
         mergeRequest.setRecordLocation(medicalRecordLocation);
         mergeRequest.setDateCreated(new Date());
 
         // create some existing paper record requests (all should be for the preferred patient at this point)
         PaperRecordRequest request1 = createPaperRecordRequest(patient, medicalRecordLocation, "XYZ",
                 location1, Status.OPEN);
         request1.setDateCreated(new Date());
         PaperRecordRequest request2 = createPaperRecordRequest(patient, medicalRecordLocation, "ABC",
                 location2, Status.ASSIGNED_TO_PULL);
         request2.setDateCreated(new Date());
 
         when(mockPaperRecordDAO.findPaperRecordRequests(PENDING_STATUSES, null, medicalRecordLocation, "XYZ", null))
                 .thenReturn(Collections.singletonList(request1));
 
         when(mockPaperRecordDAO.findPaperRecordRequests(PENDING_STATUSES, null, medicalRecordLocation, "ABC", null))
                 .thenReturn(Collections.singletonList(request2));
 
         paperRecordService.markPaperRecordsAsMerged(mergeRequest);
 
         // the "winning" request should be the request with the preferred identifier, but should be updated with
         // the more recent request location
         PaperRecordRequest expectedWinningRequest = createPaperRecordRequest(patient, medicalRecordLocation, "ABC",
                 location2, Status.ASSIGNED_TO_PULL);
         expectedWinningRequest.setId(request2.getId());
 
         PaperRecordRequest expectedLosingRequest = createPaperRecordRequest(patient, medicalRecordLocation, "XYZ",
                 location1, Status.CANCELLED);
         expectedLosingRequest.setId(request1.getId());
 
         ArgumentCaptor<PaperRecordRequest> paperRecordRequestArgumentCaptor = ArgumentCaptor.forClass(PaperRecordRequest.class);
         verify(mockPaperRecordDAO, times(2)).saveOrUpdate(paperRecordRequestArgumentCaptor.capture());
 
         IsExpectedRequest expectedWinningRequestMatcher = new IsExpectedRequest(expectedWinningRequest);
         assertThat(paperRecordRequestArgumentCaptor.getAllValues().get(0), is(expectedWinningRequestMatcher));
 
         IsExpectedRequest expectedLosingRequestMatcher = new IsExpectedRequest(expectedLosingRequest);
         assertThat(paperRecordRequestArgumentCaptor.getAllValues().get(1), is(expectedLosingRequestMatcher));
     }
 
     @Test
     public void testMarkPaperRecordsAsMergedShouldUpdatePendingRecordRequestForNotPreferredRecord() throws Exception {
 
         Location medicalRecordLocation = createMedicalRecordLocation();
         Location location1 = new Location(1);
 
         Patient patient = new Patient();
         Patient notPreferredPatient = new Patient();
 
         // create the merge request
         PaperRecordMergeRequest mergeRequest = new PaperRecordMergeRequest();
         mergeRequest.setPreferredPatient(patient);
         mergeRequest.setNotPreferredPatient(notPreferredPatient);
         mergeRequest.setPreferredIdentifier("ABC");
         mergeRequest.setNotPreferredIdentifier("XYZ");
         mergeRequest.setRecordLocation(medicalRecordLocation);
         mergeRequest.setDateCreated(new Date());
 
         // create some existing paper record request for the "non-preferred" identifier
         PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, "XYZ",
                 location1, Status.OPEN);
         request.setId(1);
         request.setDateCreated(new Date());
 
         when(mockPaperRecordDAO.findPaperRecordRequests(PENDING_STATUSES, null, medicalRecordLocation, "XYZ", null))
                 .thenReturn(Collections.singletonList(request));
 
         paperRecordService.markPaperRecordsAsMerged(mergeRequest);
 
         // the request should be updated with the preferred identifier
         PaperRecordRequest expectedRequest = createPaperRecordRequest(patient, medicalRecordLocation, "ABC",
                 location1, Status.OPEN);
         expectedRequest.setId(request.getId());
 
         IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(expectedRequest);
         verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
     }
 
     @Test
     public void testMarkPaperRecordsShouldMarkAnyNotPreferredRecordRequestsInSentStateAsReturned() throws Exception {
 
         Location medicalRecordLocation = createMedicalRecordLocation();
         Location location1 = new Location(1);
 
         Patient patient = new Patient();
         Patient notPreferredPatient = new Patient();
 
         // create the merge request
         PaperRecordMergeRequest mergeRequest = new PaperRecordMergeRequest();
         mergeRequest.setPreferredPatient(patient);
         mergeRequest.setNotPreferredPatient(notPreferredPatient);
         mergeRequest.setPreferredIdentifier("ABC");
         mergeRequest.setNotPreferredIdentifier("XYZ");
         mergeRequest.setRecordLocation(medicalRecordLocation);
         mergeRequest.setDateCreated(new Date());
 
         // create some existing paper record request for the "non-preferred" identifier
         PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, "XYZ",
                 location1, Status.SENT);
         request.setId(1);
         request.setDateCreated(new Date());
 
         when(mockPaperRecordDAO.findPaperRecordRequests(Collections.singletonList(Status.SENT),
                 null, medicalRecordLocation, "XYZ", null)).thenReturn(Collections.singletonList(request));
 
         paperRecordService.markPaperRecordsAsMerged(mergeRequest);
 
         // the request should be marked as returned with the preferred identifier
         PaperRecordRequest expectedRequest = createPaperRecordRequest(patient, medicalRecordLocation, "XYZ",
                 location1, Status.RETURNED);
         expectedRequest.setId(request.getId());
 
         IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(expectedRequest);
         verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
     }
 
     @Test
     public void testPrintPaperRecordLabelShouldPrintSingleLabel() throws Exception {
 
         Location location = new Location(1);
         Patient patient = new Patient(1);
 
         when(mockPaperRecordLabelTemplate.generateLabel(patient, "ABC")).thenReturn("data\nlines\n");
         when(mockPaperRecordLabelTemplate.getEncoding()).thenReturn("UTF-8");
 
         PaperRecordRequest request = new PaperRecordRequest();
         request.setPatient(patient);
         request.setIdentifier("ABC");
 
         paperRecordService.printPaperRecordLabel(request, location);
 
         verify(mockPrinterService).printViaSocket("data\nlines\n", Printer.Type.LABEL, location, "UTF-8");
 
     }
 
     @Test
     public void testPrintPaperRecordLabelsShouldPrintThreeLabelIfCountSetToThree() throws Exception {
 
         Location location = new Location(1);
         Patient patient = new Patient(1);
 
         when(mockPaperRecordLabelTemplate.generateLabel(patient, "ABC")).thenReturn("data\nlines\n");
         when(mockPaperRecordLabelTemplate.getEncoding()).thenReturn("UTF-8");
 
         PaperRecordRequest request = new PaperRecordRequest();
         request.setPatient(patient);
         request.setIdentifier("ABC");
 
         paperRecordService.printPaperRecordLabels(request, location, 3);
 
         verify(mockPrinterService).printViaSocket("data\nlines\ndata\n" +
                 "lines\ndata\nlines\n", Printer.Type.LABEL, location, "UTF-8");
 
     }
 
     @Test
     public void testPrintPaperRecordLabelByPatientShouldPrintSingleLabel() throws Exception {
 
         Patient patient = new Patient(1);
         Location location = new Location(1);
 
         PatientIdentifier paperRecordIdentifier = new PatientIdentifier();
         paperRecordIdentifier.setIdentifierType(paperRecordIdentifierType);
         paperRecordIdentifier.setIdentifier("ABC");
         paperRecordIdentifier.setLocation(location);
         patient.addIdentifier(paperRecordIdentifier);
 
         when(mockPaperRecordLabelTemplate.generateLabel(patient, "ABC")).thenReturn("data\nlines\n");
         when(mockPaperRecordLabelTemplate.getEncoding()).thenReturn("UTF-8");
 
         paperRecordService.printPaperRecordLabels(patient, location, 1);
 
         verify(mockPrinterService).printViaSocket("data\nlines\n", Printer.Type.LABEL, location, "UTF-8");
     }
 
 
     private PatientIdentifier createIdentifier(Location medicalRecordLocation, String identifier) {
         PatientIdentifier identifer = new PatientIdentifier();
         identifer.setIdentifier(identifier);
         identifer.setIdentifierType(paperRecordIdentifierType);
         identifer.setLocation(medicalRecordLocation);
         return identifer;
     }
 
     private Location createLocation(int locationId, String locationName) {
         Location requestLocation = new Location();
         requestLocation.setId(locationId);
         requestLocation.setName(locationName);
         return requestLocation;
     }
 
     private PaperRecordRequest createPaperRecordRequest(Patient patient, Location medicalRecordLocation, String identifier,
                                                         Location requestLocation, Status status) {
         PaperRecordRequest expectedRequest = new PaperRecordRequest();
         expectedRequest.setAssignee(null);
         expectedRequest.setCreator(authenticatedUser);
         expectedRequest.setIdentifier(identifier);
         expectedRequest.setRequestLocation(requestLocation);
         expectedRequest.setRecordLocation(medicalRecordLocation);
         expectedRequest.setPatient(patient);
         expectedRequest.updateStatus(status);
         return expectedRequest;
     }
 
 
     private PaperRecordRequest createPaperRecordRequest(Patient patient, Location medicalRecordLocation, String identifier) {
         return createPaperRecordRequest(patient, medicalRecordLocation, identifier, null, Status.OPEN);
     }
 
     private PaperRecordMergeRequest createExpectedMergeRequest(Patient preferredPatient, Patient notPreferredPatient,
                                                                String preferredIdentifier, String notPreferredIdentifier,
                                                                Location recordLocation) {
         PaperRecordMergeRequest expectedMergeRequest = new PaperRecordMergeRequest();
         expectedMergeRequest.setPreferredPatient(preferredPatient);
         expectedMergeRequest.setNotPreferredPatient(notPreferredPatient);
         expectedMergeRequest.setPreferredIdentifier(preferredIdentifier);
         expectedMergeRequest.setNotPreferredIdentifier(notPreferredIdentifier);
         expectedMergeRequest.setStatus(PaperRecordMergeRequest.Status.OPEN);
         expectedMergeRequest.setCreator(authenticatedUser);
         expectedMergeRequest.setRecordLocation(recordLocation);
        return expectedMergeRequest;
     }
 
     private PaperRecordRequest buildPaperRecordRequestWithoutIdentifier() {
         Patient patient = new Patient(1);
         Location location = new Location(1);
         PaperRecordRequest request = new PaperRecordRequest();
         request.setPatient(patient);
         request.updateStatus(PaperRecordRequest.Status.OPEN);
         request.setRecordLocation(location);
         return request;
     }
 
     private PaperRecordRequest buildPaperRecordRequestWithIdentifier() {
         Patient patient = new Patient(1);
         PatientIdentifier patientIdentifier = new PatientIdentifier();
         patientIdentifier.setIdentifierType(paperRecordIdentifierType);
         patientIdentifier.setIdentifier("ABC");
         patient.addIdentifier(patientIdentifier);
         Location location = new Location(1);
         PaperRecordRequest request = new PaperRecordRequest();
         request.setPatient(patient);
         request.updateStatus(PaperRecordRequest.Status.OPEN);
         request.setRecordLocation(location);
         request.setIdentifier("ABC");
         return request;
     }
 
 
     private class PaperRecordServiceStub extends PaperRecordServiceImpl {
 
         private PatientIdentifierType paperRecordIdentifierType;
 
         public PaperRecordServiceStub(PatientIdentifierType paperRecordIdentifierType) {
             this.paperRecordIdentifierType = paperRecordIdentifierType;
         }
 
         @Override
         protected Location getMedicalRecordLocationAssociatedWith(Location location) {
             return location;
         }
 
     }
 
     private class IsExpectedMergeRequest extends ArgumentMatcher<PaperRecordMergeRequest> {
 
         private PaperRecordMergeRequest expectedRequest;
 
         public IsExpectedMergeRequest(PaperRecordMergeRequest expectedRequest) {
             this.expectedRequest = expectedRequest;
         }
 
         @Override
         public boolean matches(Object o) {
 
             PaperRecordMergeRequest actualRequest = (PaperRecordMergeRequest) o;
 
             assertThat(actualRequest.getId(), is(expectedRequest.getId()));
             assertThat(actualRequest.getPreferredPatient(),is(expectedRequest.getPreferredPatient()));
             assertThat(actualRequest.getNotPreferredPatient(), is(expectedRequest.getNotPreferredPatient()));
             assertThat(actualRequest.getPreferredIdentifier(), is(expectedRequest.getPreferredIdentifier()));
             assertThat(actualRequest.getNotPreferredIdentifier(), is(expectedRequest.getNotPreferredIdentifier()));
             assertThat(actualRequest.getStatus(), is(expectedRequest.getStatus()));
             assertThat(actualRequest.getRecordLocation(), is(expectedRequest.getRecordLocation()));
             assertThat(actualRequest.getCreator(), is(expectedRequest.getCreator()));
             assertNotNull(actualRequest.getDateCreated());
 
             return true;
         }
     }
 
     private class IsAssignedTo extends ArgumentMatcher<PaperRecordRequest> {
 
         private Person shouldBeAssignedTo;
 
         private PaperRecordRequest.Status assignmentStatus;
 
         private String identifier;
 
         public IsAssignedTo(Person shouldBeAssignedTo, PaperRecordRequest.Status assignmentStatus) {
             this.shouldBeAssignedTo = shouldBeAssignedTo;
             this.assignmentStatus = assignmentStatus;
         }
 
 
         public IsAssignedTo(Person shouldBeAssignedTo, PaperRecordRequest.Status assignmentStatus, String identifier) {
             this.shouldBeAssignedTo = shouldBeAssignedTo;
             this.assignmentStatus = assignmentStatus;
             this.identifier = identifier;
         }
 
         @Override
         public boolean matches(Object o) {
             PaperRecordRequest request = (PaperRecordRequest) o;
             assertThat(request.getStatus(), is(assignmentStatus));
             assertThat(request.getAssignee(), is(shouldBeAssignedTo));
 
             if (identifier != null) {
                 assertThat(request.getIdentifier(), is(identifier));
             }
 
             return true;
         }
     }
 
     private class NullBoolean extends ArgumentMatcher<Boolean> {
         public boolean matches(Object o)  {
             return o == null ? true : false;
         }
     }
 
     private class NullLocation extends ArgumentMatcher<Location> {
         public boolean matches(Object o) {
             return o == null ? true : false;
         }
     }
 
     private class NullString extends ArgumentMatcher<String> {
         public boolean matches(Object o) {
             return o == null ? true : false;
         }
     }
 
     private class NullPatient extends ArgumentMatcher<Patient> {
         public boolean matches(Object o) {
             return o == null ? true : false;
         }
     }
 
 
     private class StatusListOf extends ArgumentMatcher<List<PaperRecordRequest.Status>> {
 
         private List<PaperRecordRequest.Status>  expectedStatusList;
 
         public StatusListOf(List<PaperRecordRequest.Status> expectedStatusList) {
             this.expectedStatusList = expectedStatusList;
         }
 
         @Override
         public boolean matches(Object o) {
             List<PaperRecordRequest.Status> statusList = (List<PaperRecordRequest.Status>) o;
 
             if (statusList.size() != expectedStatusList.size()) {
                 return false;
             }
 
             if (statusList.containsAll(expectedStatusList)) {
                 return true;
             }
             else {
                 return false;
             }
 
         }
 
     }
 }
