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
 package org.openmrs.module.emr.api;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentMatcher;
 import org.openmrs.Concept;
 import org.openmrs.ConceptClass;
 import org.openmrs.ConceptDatatype;
 import org.openmrs.ConceptMap;
 import org.openmrs.ConceptMapType;
 import org.openmrs.ConceptName;
 import org.openmrs.ConceptReferenceTerm;
 import org.openmrs.ConceptSearchResult;
 import org.openmrs.ConceptSource;
 import org.openmrs.Location;
 import org.openmrs.LocationTag;
 import org.openmrs.Patient;
 import org.openmrs.Privilege;
 import org.openmrs.Role;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.ConceptNameType;
 import org.openmrs.api.ConceptService;
 import org.openmrs.api.LocationService;
 import org.openmrs.api.UserService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.emr.EmrConstants;
 import org.openmrs.module.emr.EmrProperties;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.util.StringUtils;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsNull.nullValue;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 import static org.openmrs.module.emr.TestUtils.assertContainsElementWithProperty;
 
 public class EmrServiceComponentTest extends BaseModuleContextSensitiveTest {
 	
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	EmrService service;
 
     AdministrationService administrationService;
 	
 	Location where;
 
     @Autowired
     EmrProperties emrProperties;
 
     @Autowired
     ConceptService conceptService;
 
 	@Before
 	public void before() throws Exception{
 		executeDataSet("privilegeTestDataset.xml");
 		service = Context.getService(EmrService.class);
         administrationService = Context.getAdministrationService();
 
         LocationService locationService = Context.getLocationService();
 
         LocationTag supportsVisits = new LocationTag(EmrConstants.LOCATION_TAG_SUPPORTS_VISITS, "no description");
         locationService.saveLocationTag(supportsVisits);
 
         where = locationService.getLocation(1);
         where.addTag(supportsVisits);
         locationService.saveLocation(where);
 	}
 	
 	@Test
 	public void testFindPatientsWithActiveVisits() throws Exception {
 		List<Patient> patients = service.findPatients("", where, null, null);
 		assertEquals(1, patients.size());
 		assertContainsElementWithProperty(patients, "patientId", 2);
 	}
 
     @Test
     public void testFindPatientsByPrimaryIdentifier() throws Exception {
         administrationService.setGlobalProperty(EmrConstants.PRIMARY_IDENTIFIER_TYPE, "1");
         List<Patient> patients = service.findPatients("6TS-4", null, null, null);
         assertEquals(1, patients.size());
         assertContainsElementWithProperty(patients, "patientId", 7);
     }
 
     @Test
     public void testFindPatientsByPaperRecordNumber() throws Exception {
         administrationService.setGlobalProperty(EmrConstants.PRIMARY_IDENTIFIER_TYPE, "1");
         administrationService.setGlobalProperty(EmrConstants.GP_PAPER_RECORD_IDENTIFIER_TYPE, "2");
         List<Patient> patients = service.findPatients("12345K", null, null, null);
         assertEquals(1, patients.size());
         assertContainsElementWithProperty(patients, "patientId", 6);
     }
 	
 	@Test
 	public void testFindPatientsByName() throws Exception {
 		List<Patient> patients = service.findPatients("Test", null, null, null);
 		assertEquals(4, patients.size());
 		assertContainsElementWithProperty(patients, "patientId", 2);
 		assertContainsElementWithProperty(patients, "patientId", 6);
 		assertContainsElementWithProperty(patients, "patientId", 7);
 		assertContainsElementWithProperty(patients, "patientId", 8);
 	}
 	
 	@Test
 	public void testFindPatientsByNameWithActiveVisits() throws Exception {
 		List<Patient> patients = service.findPatients("Hora", where, null, null);
 		assertEquals(1, patients.size());
 		assertContainsElementWithProperty(patients, "patientId", 2);
 	}
 	
 	@Test
 	public void testFindAPIPrivileges() throws Exception{
 		UserService userService = Context.getUserService();
 		List<Role> roles= userService.getAllRoles();
 		if(roles!=null && roles.size()>0){
 			for(Role role : roles){
 				log.debug("roleName:" +  role.getName());
 			}
 		}
 		Role fullRole = userService.getRole(EmrConstants.PRIVILEGE_LEVEL_FULL_ROLE);
 		if(fullRole==null){
 			fullRole = new Role();
 			fullRole.setName(EmrConstants.PRIVILEGE_LEVEL_FULL_ROLE);
 			fullRole.setRole(EmrConstants.PRIVILEGE_LEVEL_FULL_ROLE);
 			fullRole.setDescription(EmrConstants.PRIVILEGE_LEVEL_FULL_ROLE);
 			userService.saveRole(fullRole);
 		}
 		
 		List<Privilege> allPrivileges = userService.getAllPrivileges();
 		if(allPrivileges!=null && allPrivileges.size()>0){
 			for(Privilege privilege : allPrivileges){
 				log.debug("" + privilege.getName());
 				String privilegeName = privilege.getName();
 				if(!fullRole.hasPrivilege(privilegeName)){
 					if(!StringUtils.startsWithIgnoreCase(privilegeName, EmrConstants.PRIVILEGE_PREFIX_APP) && 
 							!StringUtils.startsWithIgnoreCase(privilegeName, EmrConstants.PRIVILEGE_PREFIX_TASK)){
 						fullRole.addPrivilege(privilege);
 					}
 				}
 			}
 		}
 		userService.saveRole(fullRole);
 		
 		assertEquals(fullRole.getName(), EmrConstants.PRIVILEGE_LEVEL_FULL_ROLE);
 	}
 
     @Test
     public void testConceptSearchByName() throws Exception {
         Map<String, Concept> concepts = setupConcepts();
         ConceptClass diagnosis = conceptService.getConceptClassByName("Diagnosis");
 
         List<ConceptSearchResult> searchResults = service.conceptSearch("malaria", Locale.ENGLISH, Collections.singleton(diagnosis), null, null);
         ConceptSearchResult firstResult = searchResults.get(0);
        List<ConceptSearchResult> otherResults = searchResults.subList(1, searchResults.size());
 
        assertThat(searchResults.size(), is(3));
         assertThat(firstResult.getConcept(), is(concepts.get("malaria")));
         assertThat(firstResult.getConceptName().getName(), is("Malaria"));
 
        assertThat(otherResults, containsInAnyOrder(
                searchResultMatcher(concepts.get("malaria"), "Clinical Malaria"),
                searchResultMatcher(concepts.get("cerebral malaria"), "Cerebral Malaria")));
     }
 
     @Test
     public void testConceptSearchInAnotherLocale() throws Exception {
         Map<String, Concept> concepts = setupConcepts();
         ConceptClass diagnosis = conceptService.getConceptClassByName("Diagnosis");
 
         List<ConceptSearchResult> searchResults = service.conceptSearch("malaria", Locale.FRENCH, Collections.singleton(diagnosis), null, null);
         ConceptSearchResult firstResult = searchResults.get(0);
 
         assertThat(searchResults.size(), is(1));
         assertThat(firstResult.getConcept(), is(concepts.get("cerebral malaria")));
         assertThat(firstResult.getConceptName().getName(), is("Malaria célébrale"));
     }
 
     @Test
     public void testConceptSearchByIcd10Code() throws Exception {
         ConceptClass diagnosis = conceptService.getConceptClassByName("Diagnosis");
         ConceptSource icd10 = conceptService.getConceptSourceByName("ICD-10");
 
         Map<String, Concept> concepts = setupConcepts();
 
         List<ConceptSearchResult> searchResults = service.conceptSearch("E11.9", Locale.ENGLISH, Collections.singleton(diagnosis), Collections.singleton(icd10), null);
         ConceptSearchResult firstResult = searchResults.get(0);
 
         assertThat(searchResults.size(), is(1));
         assertThat(firstResult.getConcept(), is(concepts.get("diabetes")));
         assertThat(firstResult.getConceptName(), nullValue());
     }
 
     private Map<String, Concept> setupConcepts() {
         Map<String, Concept> concepts = new HashMap<String, Concept>();
 
         ConceptMapType sameAs = conceptService.getConceptMapTypeByName("same-as");
         ConceptSource icd10 = conceptService.getConceptSourceByName("ICD-10");
 
         ConceptDatatype na = conceptService.getConceptDatatypeByName("N/A");
         ConceptClass diagnosis = conceptService.getConceptClassByName("Diagnosis");
 
         concepts.put("malaria", conceptService.saveConcept(new ConceptBuilder(na, diagnosis)
                 .add(new ConceptName("Malaria", Locale.ENGLISH))
                 .add(new ConceptName("Clinical Malaria", Locale.ENGLISH))
                 .add(new ConceptName("Paludisme", Locale.FRENCH))
                 .addMapping(sameAs, icd10, "B54").get()));
 
         concepts.put("cerebral malaria", conceptService.saveConcept(new ConceptBuilder(na, diagnosis)
                 .add(new ConceptName("Cerebral Malaria", Locale.ENGLISH))
                 .add(new ConceptName("Malaria célébrale", Locale.FRENCH))
                 .addMapping(sameAs, icd10, "B50.0").get()));
 
         concepts.put("diabetes", conceptService.saveConcept(new ConceptBuilder(na, diagnosis)
                 .add(new ConceptName("Diabetes Mellitus, Type II", Locale.ENGLISH))
                 .addVoidedName(new ConceptName("Malaria", Locale.ENGLISH))
                 .addMapping(sameAs, icd10, "E11.9").get()));
 
         return concepts;
     }
 
     private ArgumentMatcher<ConceptSearchResult> searchResultMatcher(final Concept concept, final String nameMatched) {
         return new ArgumentMatcher<ConceptSearchResult>() {
             @Override
             public boolean matches(Object o) {
                 ConceptSearchResult actual = (ConceptSearchResult) o;
                 return actual.getConcept().equals(concept) && actual.getConceptName().getName().equals(nameMatched);
             }
         };
     }
 
     class ConceptBuilder {
 
         private Concept concept;
 
         public ConceptBuilder(ConceptDatatype datatype, ConceptClass conceptClass) {
             concept = new Concept();
             concept.setDatatype(datatype);
             concept.setConceptClass(conceptClass);
         }
 
         public Concept get() {
             return concept;
         }
 
         public ConceptBuilder add(ConceptName conceptName) {
             if (concept.getNames().size() == 0) {
                 conceptName.setLocalePreferred(true);
                 conceptName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
             }
             concept.addName(conceptName);
             return this;
         }
 
         public ConceptBuilder addMapping(ConceptMapType mapType, ConceptSource source, String code) {
             ConceptReferenceTerm term = new ConceptReferenceTerm(source, code, null);
             conceptService.saveConceptReferenceTerm(term);
             ConceptMap conceptMap = new ConceptMap(term, mapType);
             concept.addConceptMapping(conceptMap);
             return this;
         }
 
         public ConceptBuilder addVoidedName(ConceptName voidedName) {
             voidedName.setVoided(true);
             return add(voidedName);
         }
     }
 }
