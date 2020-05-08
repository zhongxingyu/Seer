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
 
 import org.hamcrest.Matcher;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentMatcher;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.openmrs.Concept;
 import org.openmrs.ConceptAnswer;
 import org.openmrs.ConceptMap;
 import org.openmrs.ConceptMapType;
 import org.openmrs.ConceptName;
 import org.openmrs.ConceptReferenceTerm;
 import org.openmrs.ConceptSource;
 import org.openmrs.Encounter;
 import org.openmrs.EncounterRole;
 import org.openmrs.Location;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.Provider;
 import org.openmrs.User;
 import org.openmrs.api.EncounterService;
 import org.openmrs.api.PatientService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.emrapi.EmrApiConstants;
 import org.openmrs.module.emrapi.EmrApiProperties;
 import org.openmrs.module.emrapi.concept.EmrConceptService;
 import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
 import org.openmrs.module.emrapi.diagnosis.Diagnosis;
 import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
 import org.openmrs.module.emrapi.disposition.Disposition;
 import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
 import org.openmrs.module.emrapi.disposition.actions.DispositionAction;
 import org.openmrs.module.emrapi.disposition.actions.MarkPatientDeadDispositionAction;
 import org.openmrs.module.reporting.common.DateUtil;
 import org.openmrs.util.OpenmrsUtil;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 import org.springframework.context.ApplicationContext;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import static org.hamcrest.CoreMatchers.hasItem;
 import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.powermock.api.mockito.PowerMockito.mockStatic;
 
 /**
  *
  */
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(Context.class)
 public class ConsultServiceTest {
 
     private ConsultServiceImpl consultService;
 
     private EmrApiProperties emrApiProperties;
     private EncounterService encounterService;
     private PatientService patientService;
     private Patient patient;
     private Concept diabetes;
     private Concept malaria;
     private ConceptName malariaSynonym;
     private Location mirebalaisHospital;
     private EncounterRole clinician;
     private Provider drBob;
     private User currentUser;
 
     private Concept diagnosisGroupingConcept;
     private Concept codedDiagnosis;
     private Concept nonCodedDiagnosis;
     private Concept diagnosisOrder;
     private Concept primary;
     private Concept secondary;
     private Concept freeTextComments;
     private Concept diagnosisCertainty;
     private Concept confirmed;
     private Concept presumed;
     private Concept dispositionGroupingConcept;
     private Concept disposition;
     private Concept patientDied;
     private Disposition death;
     private EmrConceptService emrConceptService;
 
     @Before
     public void setUp() throws Exception {
         currentUser = new User();
         mockStatic(Context.class);
         PowerMockito.when(Context.getAuthenticatedUser()).thenReturn(currentUser);
 
         patient = new Patient(123);
         diabetes = buildConcept(1, "Diabetes");
         malaria = buildConcept(2, "Malaria");
         malariaSynonym = new ConceptName();
         malaria.addName(malariaSynonym);
         mirebalaisHospital = new Location();
         clinician = new EncounterRole();
         drBob = new Provider();
         freeTextComments = buildConcept(3, "Comments");
 
         ConceptSource emrConceptSource = new ConceptSource();
         emrConceptSource.setName(EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
 
         ConceptMapType sameAs = new ConceptMapType();
 
         primary = buildConcept(4, "Primary");
         primary.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(emrConceptSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER_PRIMARY, null), sameAs));
 
         secondary = buildConcept(5, "Secondary");
         secondary.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(emrConceptSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER_SECONDARY, null), sameAs));
 
         diagnosisOrder = buildConcept(6, "Diagnosis Order");
         diagnosisOrder.addAnswer(new ConceptAnswer(primary));
         diagnosisOrder.addAnswer(new ConceptAnswer(secondary));
 
         codedDiagnosis = buildConcept(7, "Diagnosis (Coded)");
         nonCodedDiagnosis = buildConcept(8, "Diagnosis (Non-Coded)");
 
         confirmed = buildConcept(11, "Confirmed");
         confirmed.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(emrConceptSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY_CONFIRMED, null), sameAs));
 
         presumed = buildConcept(12, "Presumed");
         presumed.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(emrConceptSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY_PRESUMED, null), sameAs));
 
         diagnosisCertainty = buildConcept(10, "Diagnosis Certainty");
         diagnosisCertainty.addAnswer(new ConceptAnswer(confirmed));
         diagnosisCertainty.addAnswer(new ConceptAnswer(presumed));
 
         diagnosisGroupingConcept = buildConcept(9, "Grouping for Diagnosis");
         diagnosisGroupingConcept.addSetMember(diagnosisOrder);
         diagnosisGroupingConcept.addSetMember(codedDiagnosis);
         diagnosisGroupingConcept.addSetMember(nonCodedDiagnosis);
         diagnosisGroupingConcept.addSetMember(diagnosisCertainty);
 
         DiagnosisMetadata diagnosisMetadata = new DiagnosisMetadata();
         diagnosisMetadata.setDiagnosisSetConcept(diagnosisGroupingConcept);
         diagnosisMetadata.setCodedDiagnosisConcept(codedDiagnosis);
         diagnosisMetadata.setNonCodedDiagnosisConcept(nonCodedDiagnosis);
         diagnosisMetadata.setDiagnosisOrderConcept(diagnosisOrder);
         diagnosisMetadata.setDiagnosisCertaintyConcept(diagnosisCertainty);
 
         patientDied = buildConcept(13, "Patient Died");
 
         disposition = buildConcept(14, "Disposition");
         disposition.addAnswer(new ConceptAnswer(patientDied));
 
         dispositionGroupingConcept = buildConcept(15, "Grouping for Disposition");
         dispositionGroupingConcept.addSetMember(disposition);
 
         DispositionDescriptor dispositionDescriptor = new DispositionDescriptor();
         dispositionDescriptor.setDispositionSetConcept(dispositionGroupingConcept);
         dispositionDescriptor.setDispositionConcept(disposition);
 
         emrApiProperties = mock(EmrApiProperties.class);
         when(emrApiProperties.getConsultFreeTextCommentsConcept()).thenReturn(freeTextComments);
         when(emrApiProperties.getDiagnosisMetadata()).thenReturn(diagnosisMetadata);
         when(emrApiProperties.getDispositionDescriptor()).thenReturn(dispositionDescriptor);
         when(emrApiProperties.getClinicianEncounterRole()).thenReturn(clinician);
        when(emrApiProperties.getUnknownCauseOfDeathConcept()).thenReturn(new Concept());
 
         encounterService = mock(EncounterService.class);
         when(encounterService.saveEncounter(any(Encounter.class))).thenAnswer(new Answer<Object>() {
             @Override
             public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                 return invocationOnMock.getArguments()[0];
             }
         });
 
         patientService = mock(PatientService.class);
         when(patientService.savePatient(any(Patient.class))).thenAnswer(new Answer<Object>() {
             @Override
             public Object answer(InvocationOnMock invocation) throws Throwable {
                 return invocation.getArguments()[0];
             }
         });
 
         String snomedDiedCode = "SNOMED CT:397709008";
         emrConceptService = mock(EmrConceptService.class);
         when(emrConceptService.getConcept(snomedDiedCode)).thenReturn(patientDied);
 
         MarkPatientDeadDispositionAction markPatientDeadAction = new MarkPatientDeadDispositionAction();
         markPatientDeadAction.setPatientService(patientService);
        markPatientDeadAction.setEmrApiProperties(emrApiProperties);
 
         ApplicationContext applicationContext = mock(ApplicationContext.class);
         when(applicationContext.getBean("markPatientDeadAction", DispositionAction.class)).thenReturn(markPatientDeadAction);
 
         consultService = new ConsultServiceImpl();
         consultService.setEncounterService(encounterService);
         consultService.setEmrApiProperties(emrApiProperties);
         consultService.setEmrConceptService(emrConceptService);
         consultService.setApplicationContext(applicationContext);
 
         death = new Disposition("patientDied", "Patient Died", snomedDiedCode, Arrays.asList("markPatientDeadAction"), null);
 
         PowerMockito.when(Context.getPatientService()).thenReturn(patientService);
     }
 
     private Concept buildConcept(int conceptId, String name) {
         Concept concept = new Concept();
         concept.setConceptId(conceptId);
         concept.addName(new ConceptName(name, Locale.ENGLISH));
         return concept;
     }
 
     @Test
     public void saveConsultNote_shouldHandleCodedPrimaryDiagnosis() {
         ConsultNote consultNote = buildConsultNote();
         consultNote.addPrimaryDiagnosis(new Diagnosis(new CodedOrFreeTextAnswer(malaria)));
         Encounter encounter = consultService.saveConsultNote(consultNote);
 
         assertNotNull(encounter);
         verify(encounterService).saveEncounter(encounter);
 
         Set<Obs> obsAtTopLevel = encounter.getObsAtTopLevel(false);
 
         assertThat(obsAtTopLevel.size(), is(1));
         Obs primaryDiagnosis = obsAtTopLevel.iterator().next();
         assertThat(primaryDiagnosis, diagnosisMatcher(primary, presumed, malaria, null));
     }
 
     @Test
     public void saveConsultNote_shouldHandleCodedPrimaryDiagnosisWithSpecificName() {
         ConsultNote consultNote = buildConsultNote();
         Diagnosis diag = new Diagnosis(new CodedOrFreeTextAnswer(malariaSynonym));
         diag.setCertainty(Diagnosis.Certainty.CONFIRMED);
         consultNote.addPrimaryDiagnosis(diag);
         Encounter encounter = consultService.saveConsultNote(consultNote);
 
         assertNotNull(encounter);
         verify(encounterService).saveEncounter(encounter);
 
         Set<Obs> obsAtTopLevel = encounter.getObsAtTopLevel(false);
         assertThat(obsAtTopLevel.size(), is(1));
         Obs primaryDiagnosis = obsAtTopLevel.iterator().next();
         assertThat(primaryDiagnosis, diagnosisMatcher(primary, confirmed, malaria, malariaSynonym));
     }
 
     @Test
     public void saveConsultNote_shouldHandleNonCodedPrimaryDiagnosis() {
         String nonCodedAnswer = "New disease we've never heard of";
         ConsultNote consultNote = buildConsultNote();
         consultNote.addPrimaryDiagnosis(new Diagnosis(new CodedOrFreeTextAnswer(nonCodedAnswer)));
         Encounter encounter = consultService.saveConsultNote(consultNote);
 
         assertNotNull(encounter);
         verify(encounterService).saveEncounter(encounter);
 
         Set<Obs> obsAtTopLevel = encounter.getObsAtTopLevel(false);
         assertThat(obsAtTopLevel.size(), is(1));
         Obs primaryDiagnosis = obsAtTopLevel.iterator().next();
         assertThat(primaryDiagnosis, diagnosisMatcher(primary, presumed, nonCodedAnswer));
     }
 
     @Test
     public void saveConsultNote_shouldHandleDisposition() {
         Map<String, String[]> requestParameters = new HashMap<String, String[]>();
         String deathDate = "2013-04-05";
         requestParameters.put("deathDate", new String[]{deathDate});
 
         ConsultNote consultNote = buildConsultNote();
         consultNote.addPrimaryDiagnosis(new Diagnosis(new CodedOrFreeTextAnswer("Doesn't matter for this test")));
         consultNote.setDisposition(death);
         consultNote.setDispositionParameters(requestParameters);
 
         Encounter encounter = consultService.saveConsultNote(consultNote);
 
         assertNotNull(encounter);
         verify(encounterService).saveEncounter(encounter);
         Set<Obs> obsAtTopLevel = encounter.getObsAtTopLevel(false);
         assertThat(obsAtTopLevel, hasItem((Matcher) dispositionMatcher(patientDied)));
 
         verify(patientService).savePatient(patient);
         assertThat(patient.isDead(), is(true));
         assertThat(patient.getDeathDate(), is(DateUtil.parseDate(deathDate, "yyyy-MM-dd")));
     }
 
     @Test
     public void saveConsultNote_shouldHandleAllFields() {
         String nonCodedAnswer = "New disease we've never heard of";
         final String comments = "This is a very interesting case";
 
         ConsultNote consultNote = buildConsultNote();
         consultNote.addPrimaryDiagnosis(new Diagnosis(new CodedOrFreeTextAnswer(malaria)));
         Diagnosis diag = new Diagnosis(new CodedOrFreeTextAnswer(diabetes));
         diag.setCertainty(Diagnosis.Certainty.CONFIRMED);
         consultNote.addSecondaryDiagnosis(diag);
         consultNote.addSecondaryDiagnosis(new Diagnosis(new CodedOrFreeTextAnswer(nonCodedAnswer)));
         consultNote.setComments(comments);
         Encounter encounter = consultService.saveConsultNote(consultNote);
 
         assertNotNull(encounter);
         verify(encounterService).saveEncounter(encounter);
 
         Set<Obs> obsAtTopLevel = encounter.getObsAtTopLevel(false);
         assertThat(obsAtTopLevel.size(), is(4));
 
         assertThat(obsAtTopLevel, containsInAnyOrder(
                 diagnosisMatcher(primary, presumed, malaria, null),
                 diagnosisMatcher(secondary, confirmed, diabetes, null),
                 diagnosisMatcher(secondary, presumed, nonCodedAnswer),
                 new ArgumentMatcher<Obs>() {
                     @Override
                     public boolean matches(Object o) {
                         Obs obs = (Obs) o;
                         return obs.getConcept().equals(freeTextComments) && obs.getValueText().equals(comments);
                     }
                 }));
     }
 
     private ConsultNote buildConsultNote() {
         ConsultNote consultNote = new ConsultNote();
         consultNote.setPatient(patient);
         consultNote.setEncounterLocation(mirebalaisHospital);
         consultNote.setClinician(drBob);
 
         return consultNote;
     }
 
     private ArgumentMatcher<Obs> diagnosisMatcher(final Concept order, final Concept certainty, final Concept diagnosis, final ConceptName specificName) {
         return new ArgumentMatcher<Obs>() {
             @Override
             public boolean matches(Object o) {
                 Obs obsGroup = (Obs) o;
                 return obsGroup.getConcept().equals(diagnosisGroupingConcept) &&
                         containsInAnyOrder(new CodedObsMatcher(diagnosisOrder, order),
                                 new CodedObsMatcher(diagnosisCertainty, certainty),
                                 new CodedObsMatcher(codedDiagnosis, diagnosis, specificName)).matches(obsGroup.getGroupMembers());
             }
 
             @Override
             public String toString() {
                 return "Diagnosis matcher for " + order + " = (coded) " + diagnosis;
             }
         };
     }
 
     private ArgumentMatcher<Obs> diagnosisMatcher(final Concept order, final Concept certainty, final String nonCodedAnswer) {
         return new ArgumentMatcher<Obs>() {
             @Override
             public boolean matches(Object o) {
                 Obs obsGroup = (Obs) o;
                 return obsGroup.getConcept().equals(diagnosisGroupingConcept) &&
                         containsInAnyOrder(new CodedObsMatcher(diagnosisOrder, order),
                                 new CodedObsMatcher(diagnosisCertainty, certainty),
                                 new TextObsMatcher(nonCodedDiagnosis, nonCodedAnswer)).matches(obsGroup.getGroupMembers());
             }
         };
     }
 
     private ArgumentMatcher<Obs> dispositionMatcher(Concept dispositionConcept) {
         return new ArgumentMatcher<Obs>() {
             @Override
             public boolean matches(Object o) {
                 Obs obsGroup = (Obs) o;
                 return obsGroup.getConcept().equals(dispositionGroupingConcept) &&
                         containsInAnyOrder(new CodedObsMatcher(disposition, patientDied))
                                 .matches(obsGroup.getGroupMembers());
             }
         };
     }
 
     private class CodedObsMatcher extends ArgumentMatcher<Obs> {
         private Concept question;
         private Concept answer;
         private ConceptName specificAnswer;
 
         public CodedObsMatcher(Concept question, Concept answer) {
             this.question = question;
             this.answer = answer;
         }
 
         public CodedObsMatcher(Concept question, Concept answer, ConceptName specificAnswer) {
             this.question = question;
             this.answer = answer;
             this.specificAnswer = specificAnswer;
         }
 
         @Override
         public boolean matches(Object o) {
             Obs obs = (Obs) o;
             return obs.getConcept().equals(question) && obs.getValueCoded().equals(answer) && OpenmrsUtil.nullSafeEquals(obs.getValueCodedName(), specificAnswer);
         }
     }
 
     private class TextObsMatcher extends ArgumentMatcher<Obs> {
         private Concept question;
         private String answer;
 
         public TextObsMatcher(Concept question, String answer) {
             this.question = question;
             this.answer = answer;
         }
 
         @Override
         public boolean matches(Object o) {
             Obs obs = (Obs) o;
             return obs.getConcept().equals(question) && obs.getValueText().equals(answer);
         }
     }
 }
