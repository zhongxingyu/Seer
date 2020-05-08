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
 
 package org.openmrs.module.mirebalaisreports.definitions;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.Concept;
 import org.openmrs.ConceptClass;
 import org.openmrs.ConceptDatatype;
 import org.openmrs.ConceptMapType;
 import org.openmrs.ConceptReferenceTerm;
 import org.openmrs.ConceptSource;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.Person;
 import org.openmrs.api.ConceptService;
 import org.openmrs.api.ObsService;
 import org.openmrs.api.PatientService;
 import org.openmrs.module.emr.EmrConstants;
 import org.openmrs.module.emr.TestUtils;
 import org.openmrs.module.emr.test.TestTimer;
 import org.openmrs.module.emrapi.EmrApiProperties;
 import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
 import org.openmrs.module.emrapi.diagnosis.Diagnosis;
 import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.test.builder.ConceptBuilder;
 import org.openmrs.module.reporting.common.DateUtil;
 import org.openmrs.module.reporting.dataset.DataSetRow;
 import org.openmrs.module.reporting.dataset.MapDataSet;
 import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
 import org.openmrs.module.reporting.evaluation.EvaluationContext;
 import org.openmrs.module.reporting.indicator.dimension.CohortIndicatorAndDimensionResult;
 import org.openmrs.module.reporting.report.ReportData;
 import org.openmrs.module.reporting.report.ReportDesign;
 import org.openmrs.module.reporting.report.ReportDesignResource;
 import org.openmrs.module.reporting.report.definition.ReportDefinition;
 import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
 import org.openmrs.module.reporting.report.renderer.ExcelTemplateRenderer;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.io.ByteArrayOutputStream;
 import java.util.Date;
 
 import static org.junit.Assert.assertThat;
 import static org.openmrs.module.emr.test.ReportingMatchers.isCohortWithExactlyIds;
 
 /**
  *
  */
 public class WeeklyDiagnosisSurveillanceReportManagerComponentTest extends BaseModuleContextSensitiveTest {
 
     @Autowired
     private WeeklyDiagnosisSurveillanceReportManager manager;
 
     @Autowired
     private ReportDefinitionService service;
 
     @Autowired
     private ConceptService conceptService;
 
     @Autowired
     private PatientService patientService;
 
     @Autowired
     private ObsService obsService;
 
     @Autowired
     private EmrApiProperties emrApiProperties;
 
     @Before
     public void setUp() throws Exception {
         TestTimer timer = new TestTimer();
 
         ConceptSource source = new ConceptSource();
         source.setUuid("947a1410-1987-4399-8017-c1ea70f242d1");
         source.setName("org.openmrs.module.mirebalaisreports");
         conceptService.saveConceptSource(source);
 
         // "hemorrFever" will be created when creating the diagnosis
         createTerms(source, "bactMeningitis", "diphtheria", "flassicParalysis", "measles", "rabies",
                 "suspMalaria", "confMalaria", "dengue", "fever", "jaundiceFever", "diarrhea", "bloodyDiarrhea",
                 "typhoid", "pertussis", "respiratoryInfection", "tuberculosis", "tetanus", "anthrax", "pregnancyComplications");
 
         ConceptMapType narrowerThan = conceptService.getConceptMapTypeByName("is-parent-to");
         narrowerThan.setUuid(EmrConstants.NARROWER_THAN_CONCEPT_MAP_TYPE_UUID);
         conceptService.saveConceptMapType(narrowerThan);
 
         DiagnosisMetadata diagnosisMetadata = TestUtils.setupDiagnosisMetadata(conceptService, emrApiProperties);
 
         ConceptDatatype naDatatype = conceptService.getConceptDatatypeByName("N/A");
         ConceptClass diagnosisClass = conceptService.getConceptClassByName("Diagnosis");
 
         Concept diag = new ConceptBuilder(conceptService, naDatatype, diagnosisClass)
                 .addName("Viral Haemmorhagic Fever")
                 .addMapping(narrowerThan, source, "hemorrFever").saveAndGet();
 
         Concept nothing = new ConceptBuilder(conceptService, naDatatype, diagnosisClass)
                 .addName("Doing fine").saveAndGet();
 
         Concept nonDiagnoses = new ConceptBuilder(conceptService, naDatatype, diagnosisClass) // really a set
                 .setUuid("a2d2124b-fc2e-4aa2-ac87-792d4205dd8d")
                 .addName("Non Diagnoses")
                 .addSetMember(nothing).saveAndGet();
 
         Date obsDatetime = DateUtil.parseDate("2013-01-04", "yyyy-MM-dd");
 
         Patient femalePatient = patientService.getPatient(7);
         Obs obs = diagnosisMetadata.buildDiagnosisObsGroup(new Diagnosis(new CodedOrFreeTextAnswer(diag), Diagnosis.Order.PRIMARY));
         setAndCascade(obs, femalePatient, obsDatetime);
         obsService.saveObs(obs, null);
 
         Patient malePatient = patientService.getPatient(6);
         obs = diagnosisMetadata.buildDiagnosisObsGroup(new Diagnosis(new CodedOrFreeTextAnswer(diag), Diagnosis.Order.PRIMARY));
         setAndCascade(obs, malePatient, obsDatetime);
         obsService.saveObs(obs, null);
 
         timer.println("Finished setup");
     }
 
     private void createTerms(ConceptSource source, String... terms) {
         for (String term : terms) {
             conceptService.saveConceptReferenceTerm(new ConceptReferenceTerm(source, term, null));
         }
     }
 
     private void setAndCascade(Obs obs, Person person, Date obsDatetime) {
         obs.setPerson(person);
         obs.setObsDatetime(obsDatetime);
         if (obs.isObsGrouping()) {
             for (Obs child : obs.getGroupMembers()) {
                 setAndCascade(child, person, obsDatetime);
             }
         }
     }
 
     @Test
     public void testReport() throws Exception {
         TestTimer timer = new TestTimer();
 
         timer.println("Started");
         ReportDefinition reportDefinition = manager.buildReportDefinition();
         CohortIndicatorDataSetDefinition dsd = manager.buildDataSetDefinition();
 
         timer.println("Built DSD");
 
         EvaluationContext evaluationContext = new EvaluationContext();
         evaluationContext.addParameterValue("startOfWeek", DateUtil.parseDate("2013-01-01", "yyyy-MM-dd"));
         ReportData reportData = service.evaluate(reportDefinition, evaluationContext);
         MapDataSet evaluated = (MapDataSet) reportData.getDataSets().values().iterator().next();
 
         timer.println("Evaluated");
 
         DataSetRow data = evaluated.getData();
         assertThat(((CohortIndicatorAndDimensionResult) data.getColumnValue("hemorrhagicFever.male.young")).getCohortIndicatorAndDimensionCohort(), isCohortWithExactlyIds());
         assertThat(((CohortIndicatorAndDimensionResult) data.getColumnValue("hemorrhagicFever.female.young")).getCohortIndicatorAndDimensionCohort(), isCohortWithExactlyIds());
         assertThat(((CohortIndicatorAndDimensionResult) data.getColumnValue("hemorrhagicFever.male.old")).getCohortIndicatorAndDimensionCohort(), isCohortWithExactlyIds(6));
         assertThat(((CohortIndicatorAndDimensionResult) data.getColumnValue("hemorrhagicFever.female.old")).getCohortIndicatorAndDimensionCohort(), isCohortWithExactlyIds(7));
 
         ReportDesignResource resource = new ReportDesignResource();
         resource.setName("template.xls");
         resource.setContents(manager.loadExcelTemplate());
 
         final ReportDesign design = new ReportDesign();
         design.setName("Excel report design (not persisted)");
         design.setReportDefinition(reportDefinition);
         design.setRendererType(ExcelTemplateRenderer.class);
         design.addResource(resource);
 
         ExcelTemplateRenderer renderer = new ExcelTemplateRenderer() {
             @Override
             public ReportDesign getDesign(String argument) {
                 return design;
             }
         };
 
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         renderer.render(reportData, "xxx:xls", out);
         System.out.println("Wrote an excel file with " + out.size() + " bytes");
     }
 
 }
