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
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentMatcher;
 import org.mockito.MockitoAnnotations;
 import org.openmrs.Concept;
 import org.openmrs.ConceptDatatype;
 import org.openmrs.ConceptName;
 import org.openmrs.Location;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.PersonName;
 import org.openmrs.Provider;
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
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpSession;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.CoreMatchers.startsWith;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.argThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.Mock;
 
 /**
  *
  */
 public class ConsultPageControllerTest {
 
     @Mock
     ConsultService consultService;
 
     @Mock
     ConceptService conceptService;
 
     @Before
     public void initMocks() {
         MockitoAnnotations.initMocks(this);
     }
 
     @Test
     public void testSubmit() throws Exception {
         int primaryConceptNameId = 2460;
         int secondaryConceptId = 3;
         final String secondaryText = "Fatigue from too much testing";
         final String freeTextComments = "30 year old male, presenting with...";
 
         String diagnosisJson1 = "{ \"certainty\": \"PRESUMED\", \"diagnosisOrder\": \"PRIMARY\", \"diagnosis\": \"" + CodedOrFreeTextAnswer.CONCEPT_NAME_PREFIX + primaryConceptNameId + "\" }";
         String diagnosisJson2 = "{ \"certainty\": \"PRESUMED\", \"diagnosisOrder\": \"SECONDARY\", \"diagnosis\": \"" + CodedOrFreeTextAnswer.CONCEPT_PREFIX + secondaryConceptId + "\" }";
         String diagnosisJson3 = "{ \"certainty\": \"PRESUMED\", \"diagnosisOrder\": \"SECONDARY\", \"diagnosis\": \"" + CodedOrFreeTextAnswer.NON_CODED_PREFIX + secondaryText + "\" }";
 
         Concept conceptFor2460 = new Concept();
         final ConceptName conceptName2460 = new ConceptName();
         conceptName2460.setConcept(conceptFor2460);
 
         final Concept concept3 = new Concept();
 
         when(conceptService.getConceptName(primaryConceptNameId)).thenReturn(conceptName2460);
         when(conceptService.getConcept(secondaryConceptId)).thenReturn(concept3);
 
         Patient patient = new Patient();
         patient.addName(new PersonName("Jean", "Paul", "Marie"));
         final Location sessionLocation = new Location();
         final Provider currentProvider = new Provider();
 
         EmrContext emrContext = new EmrContext();
         emrContext.setSessionLocation(sessionLocation);
         emrContext.setCurrentProvider(currentProvider);
 
         DispositionFactory dispositionFactory = mock(DispositionFactory.class);
 
         MockHttpSession httpSession = new MockHttpSession();
         MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
         ConsultPageController controller = new ConsultPageController();
 
         Extension extension = new Extension();
        HashMap<String, Object> extensionParams = new HashMap<String, Object>();
        extensionParams.put("successMessage", "message");
        extension.setExtensionParams(extensionParams);
 
         String result = controller.post(patient,
                 asList(diagnosisJson1, diagnosisJson2, diagnosisJson3),
                 "", // no disposition
                 freeTextComments,
                 extension,
                 httpSession,
                 httpServletRequest,
                 consultService,
                 conceptService,
                 dispositionFactory,
                 emrContext, new TestUiUtils());
 
         assertThat(result, startsWith("redirect:"));
         assertThat(httpSession.getAttribute(EmrConstants.SESSION_ATTRIBUTE_INFO_MESSAGE), notNullValue());
 
         verify(consultService).saveConsultNote(argThat(new ArgumentMatcher<ConsultNote>() {
             @Override
             public boolean matches(Object o) {
                 ConsultNote actual = (ConsultNote) o;
                 return containsInAnyOrder(new Diagnosis(new CodedOrFreeTextAnswer(conceptName2460), Diagnosis.Order.PRIMARY),
                         new Diagnosis(new CodedOrFreeTextAnswer(concept3), Diagnosis.Order.SECONDARY),
                         new Diagnosis(new CodedOrFreeTextAnswer(secondaryText), Diagnosis.Order.SECONDARY)).matches(actual.getDiagnoses()) &&
                         actual.getComments().equals(freeTextComments) &&
                         actual.getEncounterLocation().equals(sessionLocation) &&
                         actual.getClinician().equals(currentProvider);
             }
         }));
     }
 
     @Test
     public void shouldSubmitEDConsultNoteWithAdditionalObservationsOfTypeCoded() throws Exception {
         int primaryConceptNameId = 2460;
 
         String diagnosisJson = "{ \"certainty\": \"PRESUMED\", \"diagnosisOrder\": \"PRIMARY\", \"diagnosis\": \"" + CodedOrFreeTextAnswer.CONCEPT_NAME_PREFIX + primaryConceptNameId + "\" }";
 
         Concept conceptFor2460 = new Concept();
         final ConceptName conceptName2460 = new ConceptName();
         conceptName2460.setConcept(conceptFor2460);
 
         when(conceptService.getConceptName(primaryConceptNameId)).thenReturn(conceptName2460);
 
         final Concept conceptForAdditionalObs = new Concept();
         conceptForAdditionalObs.setUuid("uuid-123");
         ConceptDatatype type = new ConceptDatatype();
         type.setName("Coded");
         conceptForAdditionalObs.setDatatype(type);
 
         final Concept answerForAdditionalObs = new Concept();
         answerForAdditionalObs.setUuid("uuid-answer-123");
 
         when(conceptService.getConceptByUuid("uuid-123")).thenReturn(conceptForAdditionalObs);
         when(conceptService.getConceptByUuid("uuid-answer-123")).thenReturn(answerForAdditionalObs);
 
         Patient patient = new Patient();
         patient.addName(new PersonName("Jean", "Paul", "Marie"));
         final Location sessionLocation = new Location();
         final Provider currentProvider = new Provider();
 
         DispositionFactory dispositionFactory = mock(DispositionFactory.class);
 
         EmrContext emrContext = new EmrContext();
         emrContext.setSessionLocation(sessionLocation);
         emrContext.setCurrentProvider(currentProvider);
 
         MockHttpSession httpSession = new MockHttpSession();
         MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
         ConsultPageController controller = new ConsultPageController();
 
         httpServletRequest.addParameter("fieldName", "uuid-answer-123");
 
         String result = controller.post(patient,
             asList(diagnosisJson),
             "",
             "",
             buildAdditionalObsConfig(),
             httpSession,
             httpServletRequest,
             consultService,
             conceptService,
             dispositionFactory,
             emrContext, new TestUiUtils());
 
         final Obs traumaObs = new Obs();
         traumaObs.setConcept(conceptForAdditionalObs);
         traumaObs.setValueCoded(answerForAdditionalObs);
 
         verify(consultService).saveConsultNote(argThat(new ArgumentMatcher<ConsultNote>() {
             @Override
             public boolean matches(Object o) {
                 ConsultNote actual = (ConsultNote) o;
                 Obs actualObs = actual.getAdditionalObs().get(0);
                 return containsInAnyOrder(new Diagnosis(new CodedOrFreeTextAnswer(conceptName2460), Diagnosis.Order.PRIMARY)).matches(actual.getDiagnoses()) &&
                     actual.getAdditionalObs().size() == 1 && actualObs.getConcept() == conceptForAdditionalObs &&
                     actualObs.getValueCoded() == answerForAdditionalObs;
             }
         }));
     }
 
     @Test
     public void shouldSubmitEDConsultNoteWithAdditionalObservationsOfTypeDate() throws Exception {
         int primaryConceptNameId = 2460;
 
         String diagnosisJson = "{ \"certainty\": \"PRESUMED\", \"diagnosisOrder\": \"PRIMARY\", \"diagnosis\": \"" + CodedOrFreeTextAnswer.CONCEPT_NAME_PREFIX + primaryConceptNameId + "\" }";
 
         Concept conceptFor2460 = new Concept();
         final ConceptName conceptName2460 = new ConceptName();
         conceptName2460.setConcept(conceptFor2460);
 
         when(conceptService.getConceptName(primaryConceptNameId)).thenReturn(conceptName2460);
 
         final Concept conceptForAdditionalObs = new Concept();
         conceptForAdditionalObs.setUuid("uuid-123");
         ConceptDatatype type = new ConceptDatatype();
         type.setName("Date");
         conceptForAdditionalObs.setDatatype(type);
 
         Calendar calendar = new GregorianCalendar(2013, 04, 21, 17, 23, 47);
         final Date dateForAdditionalObs = calendar.getTime();
 
         when(conceptService.getConceptByUuid("uuid-123")).thenReturn(conceptForAdditionalObs);
 
         Patient patient = new Patient();
         patient.addName(new PersonName("Jean", "Paul", "Marie"));
         final Location sessionLocation = new Location();
         final Provider currentProvider = new Provider();
 
         DispositionFactory dispositionFactory = mock(DispositionFactory.class);
 
         EmrContext emrContext = new EmrContext();
         emrContext.setSessionLocation(sessionLocation);
         emrContext.setCurrentProvider(currentProvider);
 
         MockHttpSession httpSession = new MockHttpSession();
         MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
         ConsultPageController controller = new ConsultPageController();
 
         httpServletRequest.addParameter("fieldName", "2013-05-21 17:23:47");
 
         String result = controller.post(patient,
             asList(diagnosisJson),
             "",
             "",
             buildAdditionalObsConfig(),
             httpSession,
             httpServletRequest,
             consultService,
             conceptService,
             dispositionFactory,
             emrContext, new TestUiUtils());
 
         final Obs traumaObs = new Obs();
         traumaObs.setConcept(conceptForAdditionalObs);
         traumaObs.setValueDate(dateForAdditionalObs);
 
         verify(consultService).saveConsultNote(argThat(new ArgumentMatcher<ConsultNote>() {
             @Override
             public boolean matches(Object o) {
                 ConsultNote actual = (ConsultNote) o;
                 Obs actualObs = actual.getAdditionalObs().get(0);
                     return containsInAnyOrder(new Diagnosis(new CodedOrFreeTextAnswer(conceptName2460), Diagnosis.Order.PRIMARY)).matches(actual.getDiagnoses()) &&
                     actual.getAdditionalObs().size() == 1 && actualObs.getConcept() == conceptForAdditionalObs &&
                     actualObs.getValueDate().equals(dateForAdditionalObs);
             }
         }));
     }
 
     @Test
     public void shouldSubmitConsultNoteWithOptionalAdditionalObservationsWithoutValue() throws Exception {
         int primaryConceptNameId = 2460;
 
         String diagnosisJson = "{ \"certainty\": \"PRESUMED\", \"diagnosisOrder\": \"PRIMARY\", \"diagnosis\": \"" + CodedOrFreeTextAnswer.CONCEPT_NAME_PREFIX + primaryConceptNameId + "\" }";
 
         Concept conceptFor2460 = new Concept();
         final ConceptName conceptName2460 = new ConceptName();
         conceptName2460.setConcept(conceptFor2460);
 
         when(conceptService.getConceptName(primaryConceptNameId)).thenReturn(conceptName2460);
 
         Patient patient = new Patient();
         patient.addName(new PersonName("Jean", "Paul", "Marie"));
         final Location sessionLocation = new Location();
         final Provider currentProvider = new Provider();
 
         DispositionFactory dispositionFactory = mock(DispositionFactory.class);
 
         EmrContext emrContext = new EmrContext();
         emrContext.setSessionLocation(sessionLocation);
         emrContext.setCurrentProvider(currentProvider);
 
         MockHttpSession httpSession = new MockHttpSession();
         MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
         ConsultPageController controller = new ConsultPageController();
 
         httpServletRequest.addParameter("fieldName", "");
 
         String result = controller.post(patient,
             asList(diagnosisJson),
             "",
             "",
             buildAdditionalObsConfig(),
             httpSession,
             httpServletRequest,
             consultService,
             conceptService,
             dispositionFactory,
             emrContext, new TestUiUtils());
 
         verify(consultService).saveConsultNote(argThat(new ArgumentMatcher<ConsultNote>() {
             @Override
             public boolean matches(Object o) {
                 ConsultNote actual = (ConsultNote) o;
                 return containsInAnyOrder(new Diagnosis(new CodedOrFreeTextAnswer(conceptName2460), Diagnosis.Order.PRIMARY)).matches(actual.getDiagnoses()) &&
                     actual.getAdditionalObs().size() == 0;
             }
         }));
     }
 
     private Extension buildAdditionalObsConfig() {
         Extension extension = new Extension();
         extension.setExtensionParams(new HashMap<String, Object>());
         extension.getExtensionParams().put("additionalObservationsConfig", new LinkedList<Map<String, String>>());
         ((List<Map<String, String>>) extension.getExtensionParams().get("additionalObservationsConfig")).add(new HashMap<String, String>());
         ((List<Map<String, String>>) extension.getExtensionParams().get("additionalObservationsConfig")).get(0).put("formFieldName", "fieldName");
         ((List<Map<String, String>>) extension.getExtensionParams().get("additionalObservationsConfig")).get(0).put("concept", "uuid-123");
 
         return extension;
     }
 
 }
