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
 package org.openmrs.module.registrationcore.api;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openmrs.GlobalProperty;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.Person;
 import org.openmrs.PersonName;
 import org.openmrs.Relationship;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.LocationService;
 import org.openmrs.api.PatientService;
 import org.openmrs.api.PersonService;
 import org.openmrs.api.context.Context;
 import org.openmrs.event.Event;
 import org.openmrs.module.registrationcore.RegistrationCoreConstants;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 import org.openmrs.test.Verifies;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 
 /**
  * Tests {@link $ RegistrationCoreService} .
  */
 public class RegistrationCoreServiceTest extends BaseModuleContextSensitiveTest {
 	
 	private RegistrationCoreService service;
 	
 	@Autowired
 	@Qualifier("patientService")
 	private PatientService patientService;
 	
 	@Autowired
 	@Qualifier("personService")
 	private PersonService personService;
 	
 	@Autowired
 	@Qualifier("adminService")
 	private AdministrationService adminService;
 	
 	@Autowired
 	@Qualifier("locationService")
 	private LocationService locationService;
 	
 	@Before
 	public void before() throws Exception {
 		executeDataSet("org/openmrs/module/idgen/include/TestData.xml");
 		service = Context.getService(RegistrationCoreService.class);
 		adminService.saveGlobalProperty(new GlobalProperty(RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID, "1"));
 	}
 	
 	private Patient createBasicPatient() {
 		Patient patient = new Patient();
 		PersonName pName = new PersonName();
 		pName.setGivenName("Demo");
 		pName.setFamilyName("Patient");
 		patient.addName(pName);
 		
 		patient.setBirthdate(new Date());
 		patient.setDeathDate(new Date());
 		patient.setBirthdateEstimated(true);
 		patient.setGender("M");
 		
 		return patient;
 	}
 	
 	/**
 	 * @see {@link RegistrationCoreService#registerPatient(Patient, java.util.List, Location)}
 	 */
 	@Test
 	@Verifies(value = "should create a patient from record with relationships", method = "registerPatient(Patient,List<Relationship>, Location)")
 	public void registerPatient_shouldCreateAPatientFromRecordWithRelationships() throws Exception {
 		Relationship r1 = new Relationship(null, personService.getPerson(2), personService.getRelationshipType(2));
 		Relationship r2 = new Relationship(personService.getPerson(7), null, personService.getRelationshipType(2));
 		Location location = locationService.getLocation(2);
 		Patient createdPatient = service.registerPatient(createBasicPatient(), Arrays.asList(r1, r2), location);
 		assertNotNull(createdPatient.getPatientId());
 		assertNotNull(createdPatient.getPatientIdentifier());
 		assertEquals(location, createdPatient.getPatientIdentifier().getLocation());
 		assertNotNull(patientService.getPatient(createdPatient.getPatientId()));
 		assertNotNull(createdPatient.getPersonId());
 		assertNotNull(personService.getPerson(createdPatient.getPersonId()));
 		assertEquals(2, personService.getRelationshipsByPerson(createdPatient).size());
 	}
 	
 	/**
 	 * @see {@link RegistrationCoreService#registerPatient(Patient,List<Relationship>)}
 	 */
 	@Test
 	@Verifies(value = "should fire an event when a patient is registered", method = "registerPatient(Patient,List<Relationship>)")
 	public void registerPatient_shouldFireAnEventWhenAPatientIsRegistered() throws Exception {
 		MockRegistrationEventListener listener = new MockRegistrationEventListener(1);
 		Event.subscribe(RegistrationCoreConstants.TOPIC_NAME, listener);
 		
 		Relationship r1 = new Relationship(null, personService.getPerson(2), personService.getRelationshipType(2));
 		Relationship r2 = new Relationship(personService.getPerson(7), null, personService.getRelationshipType(2));
 		Patient createdPatient = service.registerPatient(createBasicPatient(), Arrays.asList(r1, r2), null);
 		
		listener.waitForEvents(15, TimeUnit.SECONDS);
 		
 		assertEquals(createdPatient.getUuid(), listener.getPatientUuid());
 		assertEquals(createdPatient.getCreator().getUuid(), listener.getRegistererUuid());
 		assertEquals(
 		    new SimpleDateFormat(RegistrationCoreConstants.DATE_FORMAT_STRING).format(createdPatient.getDateCreated()),
 		    listener.getDateRegistered());
 		assertFalse(listener.getWasAPerson());
 		List<String> relationshipUuids = new ArrayList<String>();
 		for (Relationship r : Arrays.asList(r1, r2)) {
 			relationshipUuids.add(r.getUuid());
 		}
 		assertEquals(relationshipUuids, listener.getRelationshipUuids());
 	}
 	
 	/**
 	 * @see {@link RegistrationCoreService#registerPatient(Patient,List<Relationship>)} This needs
 	 *      to be fixed after fixing https://tickets.openmrs.org/browse/RC-9
 	 */
 	@Test
 	@Ignore
 	@Verifies(value = "should set wasPerson field to true for an existing person on the registration event", method = "registerPatient(Patient,List<Relationship>)")
 	public void registerPatient_shouldSetWasPersonFieldToTrueForAnExistingPersonOnTheRegistrationEvent() throws Exception {
 		MockRegistrationEventListener listener = new MockRegistrationEventListener(1);
 		Event.subscribe(RegistrationCoreConstants.TOPIC_NAME, listener);
 		
 		Relationship r1 = new Relationship(null, personService.getPerson(2), personService.getRelationshipType(2));
 		Relationship r2 = new Relationship(personService.getPerson(7), null, personService.getRelationshipType(2));
 		final Integer personId = 9;
 		assertNull(patientService.getPatient(personId));
 		Person person = personService.getPerson(personId);
 		assertNotNull(person);
 		service.registerPatient(createBasicPatient(), Arrays.asList(r1, r2), null);
 		
 		listener.waitForEvents(10, TimeUnit.SECONDS);
 		assertTrue(listener.getWasAPerson());
 	}
 }
