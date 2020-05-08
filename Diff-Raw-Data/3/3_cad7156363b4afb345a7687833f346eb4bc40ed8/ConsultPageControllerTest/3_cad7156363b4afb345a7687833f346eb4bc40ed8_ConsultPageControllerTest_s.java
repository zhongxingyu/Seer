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
 
 package org.openmrs.module.emr.page.controller.consult;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentMatcher;
 import org.mockito.MockitoAnnotations;
 import org.openmrs.Concept;
 import org.openmrs.ConceptDatatype;
 import org.openmrs.ConceptName;
 import org.openmrs.Encounter;
 import org.openmrs.Location;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.PersonName;
 import org.openmrs.Provider;
 import org.openmrs.Visit;
 import org.openmrs.api.ConceptService;
 import org.openmrs.module.appframework.domain.Extension;
 import org.openmrs.module.emr.EmrConstants;
 import org.openmrs.module.emr.EmrContext;
 import org.openmrs.module.emr.consult.ConsultNote;
 import org.openmrs.module.emr.consult.ConsultService;
 import org.openmrs.module.emr.test.TestUiUtils;
 import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
 import org.openmrs.module.emrapi.diagnosis.Diagnosis;
 import org.openmrs.module.emrapi.disposition.DispositionFactory;
 import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpSession;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.CoreMatchers.startsWith;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.argThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.Mock;
 
 /**
  *
  */
 public class ConsultPageControllerTest {
 
     @Mock
     private ConsultService consultService;
 
     @Mock
     private ConceptService conceptService;
 
     @Before
     public void initMocks() {
         MockitoAnnotations.initMocks(this);
     }
 
     @Test
     public void shouldSaveConsultNoteAndRedirect() throws Exception {
         int primaryConceptNameId = 2460;
         int secondaryConceptId = 3;
         final String secondaryText = "Fatigue from too much testing";
         final String freeTextComments = "30 year old male, presenting with...";
 
         String diagnosisJson1 = "{ \"certainty\": \"PRESUMED\", \"diagnosisOrder\": \"PRIMARY\", \"diagnosis\": \"" + CodedOrFreeTextAnswer.CONCEPT_NAME_PREFIX + primaryConceptNameId + "\" }";
         String diagnosisJson2 = "{ \"certainty\": \"PRESUMED\", \"diagnosisOrder\": \"SECONDARY\", \"diagnosis\": \"" + CodedOrFreeTextAnswer.CONCEPT_PREFIX + secondaryConceptId + "\" }";
         String diagnosisJson3 = "{ \"certainty\": \"PRESUMED\", \"diagnosisOrder\": \"SECONDARY\", \"diagnosis\": \"" + CodedOrFreeTextAnswer.NON_CODED_PREFIX + secondaryText + "\" }";
         List<String> diagnoses = asList(diagnosisJson1, diagnosisJson2, diagnosisJson3);
 
         Concept conceptFor2460 = new Concept();
         final ConceptName conceptName2460 = new ConceptName();
         conceptName2460.setConcept(conceptFor2460);
 
         final Concept concept3 = new Concept();
 
         when(conceptService.getConceptName(primaryConceptNameId)).thenReturn(conceptName2460);
         when(conceptService.getConcept(secondaryConceptId)).thenReturn(concept3);
 
         MockHttpSession httpSession = new MockHttpSession();
         final Location consultLocation = new Location();
         final Provider consultProvider = new Provider();
 
         final Date consultDate = new Date();
 
         verifySaveConsultNote(argThat(new ArgumentMatcher<ConsultNote>() {
             @Override
             public boolean matches(Object o) {
                 ConsultNote actual = (ConsultNote) o;
                 return containsInAnyOrder(new Diagnosis(new CodedOrFreeTextAnswer(conceptName2460), Diagnosis.Order.PRIMARY),
                         new Diagnosis(new CodedOrFreeTextAnswer(concept3), Diagnosis.Order.SECONDARY),
                         new Diagnosis(new CodedOrFreeTextAnswer(secondaryText), Diagnosis.Order.SECONDARY)).matches(actual.getDiagnoses()) &&
                         actual.getComments().equals(freeTextComments) &&
                         actual.getEncounterLocation().equals(consultLocation) &&
                         actual.getClinician().equals(consultProvider) &&
                        actual.getEncounterDate().equals(consultDate);
             }
         }));
 
         String result = post(freeTextComments, diagnoses, httpSession, consultLocation, consultProvider, consultDate, "", new VisitDomainWrapper(new Visit()), true);
 
         assertThat(result, startsWith("redirect:"));
         assertThat(result, containsString("visitId=1"));
         assertThat(httpSession.getAttribute(EmrConstants.SESSION_ATTRIBUTE_INFO_MESSAGE), notNullValue());
     }
 
     @Test
     public void shouldSubmitEDConsultNoteWithAdditionalObservationsOfTypeCoded() throws Exception {
         final Concept conceptForAdditionalObs = createConcept("uuid-123", "Coded");
 
         final Concept answerForAdditionalObs = new Concept();
         answerForAdditionalObs.setUuid("uuid-answer-123");
         when(conceptService.getConceptByUuid("uuid-answer-123")).thenReturn(answerForAdditionalObs);
 
         verifySaveConsultNote(argThat(new ArgumentMatcher<ConsultNote>() {
             @Override
             public boolean matches(Object o) {
                 ConsultNote actual = (ConsultNote) o;
                 Obs actualObs = actual.getAdditionalObs().get(0);
                 return actual.getAdditionalObs().size() == 1 && actualObs.getConcept() == conceptForAdditionalObs &&
                         actualObs.getValueCoded() == answerForAdditionalObs;
             }
         }));
 
         post("", Collections.<String>emptyList(), new MockHttpSession(), new Location(), new Provider(), new Date(), "uuid-answer-123", new VisitDomainWrapper(new Visit()), true);
     }
 
     @Test
     public void shouldSubmitEDConsultNoteWithAdditionalObservationsOfTypeDate() throws Exception {
         final Concept conceptForAdditionalObs = createConcept("uuid-123", "Date");
 
         Calendar calendar = new GregorianCalendar(2013, 04, 21, 17, 23, 47);
         final Date dateForAdditionalObs = calendar.getTime();
 
         verifySaveConsultNote(argThat(new ArgumentMatcher<ConsultNote>() {
             @Override
             public boolean matches(Object o) {
                 ConsultNote actual = (ConsultNote) o;
                 Obs actualObs = actual.getAdditionalObs().get(0);
                 return actual.getAdditionalObs().size() == 1 && actualObs.getConcept() == conceptForAdditionalObs &&
                         actualObs.getValueDate().equals(dateForAdditionalObs);
             }
         }));
 
         post("", Collections.<String>emptyList(), new MockHttpSession(), new Location(), new Provider(), new Date(), "2013-05-21 17:23:47", new VisitDomainWrapper(new Visit()), true);
     }
 
     @Test
     public void shouldSubmitConsultNoteWithOptionalAdditionalObservationsWithoutValue() throws Exception {
         verifySaveConsultNote(argThat(new ArgumentMatcher<ConsultNote>() {
             @Override
             public boolean matches(Object o) {
                 ConsultNote actual = (ConsultNote) o;
                 return actual.getAdditionalObs().size() == 0;
             }
         }));
 
         post("", Collections.<String>emptyList(), new MockHttpSession(), new Location(), new Provider(), new Date(), "", new VisitDomainWrapper(new Visit()), true);
     }
 
     @Test
     public void shouldSubmitRetrospectiveConsultNoteAndSetConsultDatetimeToStartVisitDatetime() throws Exception {
         Visit visit = new Visit();
         visit.setId(1);
         final Date startVisitDatetime = (new DateTime(2013, 6, 18, 17, 4, 32)).toDate();
         visit.setStartDatetime(startVisitDatetime);
 
         final Date encounterDate = (new DateTime(2013, 6, 18, 0, 0, 0)).toDate();
 
         verifySaveConsultNote(argThat(new ArgumentMatcher<ConsultNote>() {
             @Override
             public boolean matches(Object o) {
                 ConsultNote actual = (ConsultNote) o;
                 return actual.getEncounterDate().equals(startVisitDatetime);
             }
         }));
 
         post("", Collections.<String>emptyList(), new MockHttpSession(), new Location(), new Provider(), encounterDate, "", new VisitDomainWrapper(visit), false);
     }
 
     @Test
     public void shouldSubmitRetrospectiveConsultNoteAndSetConsultDatetimeToBeginningOfSubmittedDayOnMultiDayVisit() throws Exception {
         Visit visit = new Visit();
         final Date startVisitDatetime = (new DateTime(2013, 6, 18, 17, 4, 32)).toDate();
         visit.setStartDatetime(startVisitDatetime);
 
         final Date submittedEncounterDate = (new DateTime(2013, 6, 19, 18, 32, 23)).toDate();
         final Date encounterDate = (new DateTime(2013, 6, 19, 0, 0, 0)).toDate();
 
         verifySaveConsultNote(argThat(new ArgumentMatcher<ConsultNote>() {
             @Override
             public boolean matches(Object o) {
                 ConsultNote actual = (ConsultNote) o;
                 return actual.getEncounterDate().equals(encounterDate);
             }
         }));
 
         post("", Collections.<String>emptyList(), new MockHttpSession(), new Location(), new Provider(), submittedEncounterDate, "", new VisitDomainWrapper(visit), false);
     }
 
     private Concept createConcept(String conceptUUID, String dataType) {
         ConceptDatatype type = new ConceptDatatype();
         type.setName(dataType);
 
         Concept concept = new Concept();
         concept.setUuid(conceptUUID);
         concept.setDatatype(type);
 
         when(conceptService.getConceptByUuid(conceptUUID)).thenReturn(concept);
 
         return concept;
     }
 
     private String post(String freeTextComments, List<String> diagnoses, MockHttpSession httpSession,
                         Location consultLocation, Provider consultProvider, Date consultDate, String fieldNameParam,
                         VisitDomainWrapper visitWrapper, boolean isVisitActive) throws IOException {
         Patient patient = new Patient();
         patient.addName(new PersonName("Jean", "Paul", "Marie"));
         patient.setId(1);
 
         DispositionFactory dispositionFactory = mock(DispositionFactory.class);
 
         EmrContext emrContext = mock(EmrContext.class);
         when(emrContext.getActiveVisit()).thenReturn(isVisitActive ? visitWrapper : null);
 
         MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
         ConsultPageController controller = new ConsultPageController();
 
         httpServletRequest.addParameter("fieldName", fieldNameParam);
 
         return controller.post(patient,
                 visitWrapper,
                 diagnoses,
                 "",
                 freeTextComments,
                 consultProvider, consultLocation, consultDate,
                 buildAdditionalObsConfig(),
                 consultService, conceptService, dispositionFactory, httpSession,
                 httpServletRequest,
                 emrContext,
                 new TestUiUtils()
         );
     }
 
     private Extension buildAdditionalObsConfig() {
         Extension extension = new Extension();
         extension.setExtensionParams(new HashMap<String, Object>());
         extension.getExtensionParams().put("successMessage", "message");
 
         List<Map<String, String>> additionalObsConfig = new LinkedList<Map<String, String>>();
         extension.getExtensionParams().put("additionalObservationsConfig", additionalObsConfig);
 
         additionalObsConfig.add(new HashMap<String, String>());
         additionalObsConfig.get(0).put("formFieldName", "fieldName");
         additionalObsConfig.get(0).put("concept", "uuid-123");
 
         return extension;
     }
 
     private void verifySaveConsultNote(ConsultNote matcher) {
         Encounter encounter = new Encounter();
         Visit visit = new Visit();
         visit.setId(1);
         encounter.setVisit(visit);
         when(consultService.saveConsultNote(matcher)).thenReturn(encounter);
     }
 }
